package com.example.datapack

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.security.MessageDigest
import java.util.zip.ZipInputStream

/** Strict, side-effect-free parser. No database or career state is touched until accepted=true. */
object DataPackParser {
    const val MAX_INPUT_BYTES = 32 * 1024 * 1024
    private const val MAX_ZIP_EXPANDED_BYTES = 50 * 1024 * 1024
    private const val MAX_ZIP_ENTRIES = 5_000
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val documentAdapter = moshi.adapter(DataPackDocument::class.java)
    private val stringAdapter = moshi.adapter(String::class.java)

    private val requiredFields = mapOf(
        DataModule.Leagues to listOf("name", "country"),
        DataModule.Clubs to listOf("name", "leagueInternalId"),
        DataModule.Players to listOf("firstName", "lastName", "birthDate", "nationality", "primaryPosition"),
        DataModule.Squads to listOf("playerInternalId", "clubInternalId"),
        DataModule.Coaches to listOf("name", "clubInternalId"),
        DataModule.Contracts to listOf("playerInternalId", "clubInternalId", "validFrom", "validTo"),
        DataModule.Transfers to listOf("playerInternalId", "fromClubInternalId", "toClubInternalId", "transferDate"),
        DataModule.PlayerAttributes to listOf("playerInternalId", "attributes"),
        DataModule.PlayerValuations to listOf("playerInternalId", "value", "currency", "valuationDate"),
        DataModule.ClubLogos to listOf("clubInternalId", "assetPath"),
        DataModule.PlayerPortraits to listOf("playerInternalId", "assetPath"),
        DataModule.Kits to listOf("clubInternalId", "kind", "assetPath")
    )
    private val referenceFields = mapOf(
        DataModule.Clubs to listOf("leagueInternalId" to DataModule.Leagues),
        DataModule.Squads to listOf("playerInternalId" to DataModule.Players, "clubInternalId" to DataModule.Clubs),
        DataModule.Coaches to listOf("clubInternalId" to DataModule.Clubs),
        DataModule.Contracts to listOf("playerInternalId" to DataModule.Players, "clubInternalId" to DataModule.Clubs),
        DataModule.Transfers to listOf("playerInternalId" to DataModule.Players, "fromClubInternalId" to DataModule.Clubs, "toClubInternalId" to DataModule.Clubs),
        DataModule.PlayerAttributes to listOf("playerInternalId" to DataModule.Players),
        DataModule.PlayerValuations to listOf("playerInternalId" to DataModule.Players),
        DataModule.ClubLogos to listOf("clubInternalId" to DataModule.Clubs),
        DataModule.PlayerPortraits to listOf("playerInternalId" to DataModule.Players),
        DataModule.Kits to listOf("clubInternalId" to DataModule.Clubs)
    )

    fun parse(fileName: String, bytes: ByteArray): ParsedDataPack {
        if (bytes.isEmpty() || bytes.size > MAX_INPUT_BYTES) return failed("SIZE_LIMIT", fileName, "Dosya boş veya 32 MB sınırını aşıyor.")
        return try {
            when (fileName.substringAfterLast('.', "").lowercase()) {
                "json" -> validate(documentAdapter.fromJson(bytes.toString(Charsets.UTF_8)), emptyMap(), fileName)
                "csv" -> validate(parseCsv(bytes.toString(Charsets.UTF_8)), emptyMap(), fileName)
                "zip" -> parseZip(bytes, fileName)
                else -> failed("UNSUPPORTED_FILE", fileName, "Yalnızca JSON, CSV ve ZIP içe aktarılabilir.")
            }
        } catch (failure: Exception) {
            failed("PARSE_ERROR", fileName, failure.message?.take(240) ?: "Paket okunamadı.")
        }
    }

    private fun parseZip(bytes: ByteArray, fileName: String): ParsedDataPack {
        val entries = linkedMapOf<String, ByteArray>()
        var expanded = 0L
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var count = 0
            while (true) {
                val entry = zip.nextEntry ?: break
                count++
                if (count > MAX_ZIP_ENTRIES) return failed("ZIP_ENTRY_LIMIT", fileName, "ZIP içinde izin verilen dosya sayısı aşıldı.")
                val safe = safeArchivePath(entry.name)
                    ?: return failed("ZIP_PATH", entry.name, "ZIP içinde geçersiz dosya yolu var.")
                if (!entry.isDirectory) {
                    val contentOut = ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    while (true) {
                        val read = zip.read(buffer)
                        if (read < 0) break
                        expanded += read
                        if (expanded > MAX_ZIP_EXPANDED_BYTES) return failed("ZIP_SIZE_LIMIT", fileName, "ZIP açılmış boyut sınırını aşıyor.")
                        contentOut.write(buffer, 0, read)
                    }
                    val content = contentOut.toByteArray()
                    if (entries.put(safe, content) != null) return failed("ZIP_DUPLICATE", safe, "ZIP aynı yolu birden fazla içeriyor.")
                }
                zip.closeEntry()
            }
        }
        val manifestPath = listOf("datapack.json", "manifest.json").firstOrNull(entries::containsKey)
            ?: return failed("ZIP_MANIFEST_MISSING", fileName, "ZIP kökünde datapack.json veya manifest.json bulunamadı.")
        val document = documentAdapter.fromJson(entries.getValue(manifestPath).toString(Charsets.UTF_8))
        val referenced = document?.mediaAssets.orEmpty().associate { it.path to (entries[it.path] ?: ByteArray(0)) }
        return validate(document, referenced, fileName, zipFiles = entries)
    }

    private fun parseCsv(text: String): DataPackDocument {
        val lines = text.lineSequence().toList()
        val metadataLines = lines.takeWhile { it.startsWith("#") }
        val metadata = metadataLines.associate { line ->
            val pair = line.drop(1).split('=', limit = 2)
            require(pair.size == 2) { "CSV metadata satırı #alan=değer biçiminde olmalı." }
            pair[0].trim() to pair[1].trim()
        }
        val meta = DataPackMetadata(
            packId = metadata["packId"] ?: error("CSV #packId gerekli."),
            season = metadata["season"] ?: error("CSV #season gerekli."),
            snapshotDate = metadata["snapshotDate"] ?: error("CSV #snapshotDate gerekli."),
            source = metadata["source"] ?: error("CSV #source gerekli."),
            version = metadata["version"] ?: error("CSV #version gerekli."),
            checksum = metadata["checksum"] ?: error("CSV #checksum gerekli.")
        )
        val csvText = lines.drop(metadataLines.size).joinToString("\n")
        val rows = parseRfc4180(csvText)
        require(rows.isNotEmpty()) { "CSV başlık satırı bulunamadı." }
        val header = rows.first().map(String::trim)
        val required = setOf("module", "internalId", "parentInternalId", "source", "validatedAt", "missingValues", "dataJson")
        require(header.toSet().containsAll(required)) { "CSV başlığında module, internalId, parentInternalId, source, validatedAt, missingValues ve dataJson alanları gerekli." }
        val index = header.withIndex().associate { it.value to it.index }
        val records = rows.drop(1).filter { row -> row.any(String::isNotBlank) }.mapIndexed { rowIndex, cells ->
            fun value(field: String) = cells.getOrElse(index.getValue(field)) { "" }.trim()
            val data = moshi.adapter(Map::class.java).fromJson(value("dataJson"))?.mapKeys { it.key.toString() }
                ?: error("CSV satır ${rowIndex + 2}: dataJson geçersiz.")
            DataPackRecord(
                module = value("module"), internalId = value("internalId"),
                parentInternalId = value("parentInternalId").ifBlank { null },
                source = value("source"), validatedAt = value("validatedAt"),
                missingValues = value("missingValues").let { raw -> if (raw.isBlank()) emptyList() else raw.split('|').map(String::trim).filter(String::isNotEmpty) },
                data = data
            )
        }
        val recordJson = moshi.adapter(List::class.java).toJson(records)
        val checksum = metadata.getValue("checksum")
        // CSV checksum is defined over canonical parsed record objects, identical to the JSON pack rule.
        require(checksum.matches(Regex("(?i)^[0-9a-f]{64}$"))) { "CSV checksum 64 haneli SHA-256 olmalı." }
        return DataPackDocument(meta, records)
    }

    private fun validate(
        document: DataPackDocument?,
        mediaBytes: Map<String, ByteArray>,
        sourcePath: String,
        zipFiles: Map<String, ByteArray> = emptyMap()
    ): ParsedDataPack {
        val issues = mutableListOf<DataPackIssue>()
        if (document == null) return parsedFailure(issues + issue("ERROR", "EMPTY_DOCUMENT", sourcePath, "Paket içeriği boş."))
        val meta = document.metadata
        if (!meta.packId.matches(Regex("^[A-Za-z0-9][A-Za-z0-9._-]{2,79}$"))) issues += issue("ERROR", "INVALID_PACK_ID", "metadata.packId", "packId 3-80 karakterlik kararlı bir kimlik olmalı.")
        if (!meta.season.matches(Regex("^\\d{4}/\\d{2,4}$"))) issues += issue("ERROR", "INVALID_SEASON", "metadata.season", "Sezon YYYY/YY biçiminde olmalı.")
        if (!isIsoDate(meta.snapshotDate)) issues += issue("ERROR", "INVALID_SNAPSHOT_DATE", "metadata.snapshotDate", "snapshotDate YYYY-MM-DD biçiminde olmalı.")
        if (meta.source.isBlank()) issues += issue("ERROR", "SOURCE_REQUIRED", "metadata.source", "Veri kaynağı belirtilmeli.")
        if (!meta.version.matches(Regex("^[0-9][A-Za-z0-9._+-]{0,39}$"))) issues += issue("ERROR", "INVALID_VERSION", "metadata.version", "Sürüm 1.2.3 benzeri 1-40 karakterlik güvenli bir biçimde olmalı.")

        val normalized = document.records.mapIndexedNotNull { index, record ->
            val module = moduleOf(record.module)
            if (module == null) {
                issues += issue("ERROR", "UNKNOWN_MODULE", "records[$index].module", "Desteklenmeyen modül: ${record.module.take(80)}")
                null
            } else module to record
        }
        val expectedChecksum = sha256(canonicalPayload(document))
        if (!meta.checksum.matches(Regex("(?i)^[0-9a-f]{64}$")) || !expectedChecksum.equals(meta.checksum, ignoreCase = true)) {
            issues += issue("ERROR", "CHECKSUM_MISMATCH", "metadata.checksum", "Paket checksum değeri kayıtlar ve medya tanımlarıyla uyuşmuyor.")
        }

        val idsByModule = normalized.groupBy({ it.first }, { it.second.internalId })
        val allIds = mutableSetOf<Pair<DataModule, String>>()
        normalized.forEachIndexed { index, (module, record) ->
            val path = "records[$index]"
            if (!record.internalId.matches(Regex("^[A-Za-z0-9][A-Za-z0-9._:-]{1,127}$"))) issues += issue("ERROR", "INVALID_INTERNAL_ID", "$path.internalId", "internalId geçersiz veya kararlı biçimde değil.")
            if (!allIds.add(module to record.internalId)) issues += issue("ERROR", "DUPLICATE_INTERNAL_ID", "$path.internalId", "${module.name} içinde yinelenen internalId: ${record.internalId}")
            if (record.source.isBlank()) issues += issue("ERROR", "RECORD_SOURCE_REQUIRED", "$path.source", "Her satırın kaynak bilgisi korunmalı.")
            if (!isIsoDate(record.validatedAt)) issues += issue("ERROR", "INVALID_VALIDATION_DATE", "$path.validatedAt", "Doğrulama tarihi YYYY-MM-DD biçiminde olmalı.")
            if (!record.parentInternalId.isNullOrBlank() && normalized.none { it.second.internalId == record.parentInternalId }) {
                issues += issue("ERROR", "UNMATCHED_PARENT_ID", "$path.parentInternalId", "parentInternalId paketteki hiçbir internalId ile eşleşmedi.")
            }
            requiredFields.getValue(module).filter { missing(record.data[it]) }.forEach { field ->
                issues += issue("ERROR", "REQUIRED_FIELD_MISSING", "$path.data.$field", "${module.name} kaydında gerekli alan eksik.")
            }
        }

        normalized.forEachIndexed { index, (module, record) ->
            referenceFields[module].orEmpty().forEach { (field, targetModule) ->
                val ref = record.data[field]?.toString()?.takeIf(String::isNotBlank) ?: return@forEach
                if ((idsByModule[targetModule] ?: emptyList()).none { it == ref }) {
                    issues += issue("ERROR", "UNMATCHED_INTERNAL_ID", "records[$index].data.$field", "$field değeri ${targetModule.name} modülünde eşleşmedi: $ref")
                }
            }
        }

        document.mediaAssets.forEachIndexed { index, asset ->
            val module = moduleOf(asset.module)
            val targetModule = when (module) { DataModule.ClubLogos -> DataModule.Clubs; DataModule.PlayerPortraits -> DataModule.Players; DataModule.Kits -> DataModule.Clubs; else -> null }
            if (targetModule == null || (idsByModule[targetModule] ?: emptyList()).none { it == asset.internalId }) issues += issue("ERROR", "MEDIA_ENTITY_UNMATCHED", "mediaAssets[$index].internalId", "Medya internalId değeri kendi modülünde bulunamadı.")
            if (safeArchivePath(asset.path) == null || !asset.path.startsWith("media/")) issues += issue("ERROR", "MEDIA_PATH_INVALID", "mediaAssets[$index].path", "Medya yolu media/ altında güvenli bir göreli yol olmalı.")
            val content = mediaBytes[asset.path]
            if (content == null || content.isEmpty()) issues += issue("ERROR", "MEDIA_MISSING", "mediaAssets[$index].path", "ZIP içinde medya dosyası bulunamadı.")
            else if (!sha256(content).equals(asset.sha256, true)) issues += issue("ERROR", "MEDIA_CHECKSUM_MISMATCH", "mediaAssets[$index].sha256", "Medya checksum doğrulaması başarısız.")
            if (module != null && targetModule != null && moduleOf(asset.module) == module && module in setOf(DataModule.ClubLogos, DataModule.PlayerPortraits, DataModule.Kits)) {
                val mediaRecord = normalized.firstOrNull { it.first == module && it.second.internalId == asset.internalId }?.second
                if (mediaRecord?.data?.get("assetPath") != asset.path) issues += issue("ERROR", "MEDIA_REFERENCE_MISMATCH", "mediaAssets[$index]", "Medya yolu ilgili kayıtla eşleşmiyor.")
            }
        }
        val declaredPaths = document.mediaAssets.map { it.path }.toSet()
        zipFiles.keys.filter { it.startsWith("media/") && it !in declaredPaths }.forEach { path ->
            issues += issue("WARNING", "UNREFERENCED_MEDIA", path, "Dosya pakette var ama hiçbir medya kaydı bunu kullanmıyor.")
        }
        document.externalIds.forEachIndexed { index, mapping ->
            val module = moduleOf(mapping.module)
            if (module == null || idsByModule[module].orEmpty().none { it == mapping.internalId }) issues += issue("ERROR", "EXTERNAL_ID_UNMATCHED", "externalIds[$index]", "External ID eşlemesi için internalId bulunamadı.")
            if (mapping.sourceSystem.isBlank() || mapping.externalId.isBlank()) issues += issue("ERROR", "EXTERNAL_ID_INVALID", "externalIds[$index]", "Kaynak sistem ve externalId boş olamaz.")
        }

        val moduleReports = DataModule.entries.associateWith { module ->
            val rows = normalized.filter { it.first == module }.map { it.second }
            val denominator = rows.size * requiredFields.getValue(module).size
            val missingCount = rows.sumOf { row ->
                requiredFields.getValue(module).count { missing(row.data[it]) } + row.missingValues.distinct().size
            }
            val unmatched = issues.filter { it.code == "UNMATCHED_INTERNAL_ID" && it.path.startsWith("records[") }
                .mapNotNull { item ->
                    val recordIndex = Regex("^records\\[(\\d+)]").find(item.path)?.groupValues?.getOrNull(1)?.toIntOrNull()
                    if (recordIndex != null && normalized.getOrNull(recordIndex)?.first == module) item.path else null
                }
            // Missing-value rate is absent required/declared values over present records; empty modules report 0%.
            val rate = if (rows.isEmpty()) 0.0 else (missingCount.toDouble() / (denominator + rows.sumOf { it.missingValues.distinct().size }).coerceAtLeast(1)).coerceIn(0.0, 1.0)
            val errorCount = issues.count { item ->
                val recordIndex = Regex("^records\\[(\\d+)]").find(item.path)?.groupValues?.getOrNull(1)?.toIntOrNull()
                item.severity == ImportSeverity.ERROR && item.code in setOf("REQUIRED_FIELD_MISSING", "UNMATCHED_INTERNAL_ID") && recordIndex != null && normalized.getOrNull(recordIndex)?.first == module
            }
            ModuleImportReport(module.name, rows.size, rate, unmatched, errorCount)
        }
        val accepted = issues.none { it.severity == ImportSeverity.ERROR }
        val report = DataPackImportReport(meta.packId, accepted, expectedChecksum, DataModule.entries.map { moduleReports.getValue(it) }, issues)
        return ParsedDataPack(if (accepted) document else null, report, mediaBytes)
    }

    fun canonicalPayload(document: DataPackDocument): String = canonical(mapOf("records" to document.records, "mediaAssets" to document.mediaAssets))

    fun canonical(value: Any?): String = when (value) {
        null -> "null"
        is String -> stringAdapter.toJson(value)
        is Boolean -> value.toString()
        is Number -> BigDecimal(value.toString()).stripTrailingZeros().toPlainString()
        is Map<*, *> -> value.entries.sortedBy { it.key.toString() }.joinToString(",", "{", "}") { "${stringAdapter.toJson(it.key.toString())}:${canonical(it.value)}" }
        is Iterable<*> -> value.joinToString(",", "[", "]") { canonical(it) }
        is Array<*> -> value.joinToString(",", "[", "]") { canonical(it) }
        is DataPackRecord -> canonical(mapOf("module" to value.module, "internalId" to value.internalId, "parentInternalId" to value.parentInternalId, "source" to value.source, "validatedAt" to value.validatedAt, "missingValues" to value.missingValues, "data" to value.data))
        is DataPackMedia -> canonical(mapOf("module" to value.module, "internalId" to value.internalId, "kind" to value.kind, "path" to value.path, "sha256" to value.sha256))
        else -> error("Unsupported canonical value: ${value::class.java.simpleName}")
    }

    private fun parseRfc4180(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>(); val cell = StringBuilder(); var quoted = false; var i = 0
        while (i < text.length) {
            val c = text[i]
            if (quoted) {
                if (c == '"' && i + 1 < text.length && text[i + 1] == '"') { cell.append('"'); i++ }
                else if (c == '"') quoted = false else cell.append(c)
            } else when (c) {
                '"' -> { require(cell.isEmpty()) { "CSV tırnak beklenmeyen yerde." }; quoted = true }
                ',' -> { row += cell.toString(); cell.setLength(0) }
                '\n' -> { row += cell.toString().removeSuffix("\r"); cell.setLength(0); rows += row; row = mutableListOf() }
                else -> cell.append(c)
            }
            i++
        }
        require(!quoted) { "CSV kapanmamış tırnak içeriyor." }
        if (cell.isNotEmpty() || row.isNotEmpty()) { row += cell.toString().removeSuffix("\r"); rows += row }
        return rows
    }

    private fun moduleOf(value: String): DataModule? = DataModule.entries.firstOrNull { it.name.equals(value.trim(), true) }
    private fun missing(value: Any?): Boolean = value == null || value is String && value.isBlank() || value is Collection<*> && value.isEmpty() || value is Map<*, *> && value.isEmpty()
    private fun safeArchivePath(path: String): String? {
        if (path.isBlank() || path.startsWith('/') || path.startsWith('\\') || path.contains('\\') || Regex("^[A-Za-z]:").containsMatchIn(path)) return null
        val parts = path.split('/')
        if (parts.any { it.isBlank() || it == "." || it == ".." }) return null
        return path
    }
    private fun isIsoDate(date: String): Boolean = runCatching { java.time.LocalDate.parse(date).toString() == date }.getOrDefault(false)
    private fun sha256(value: String): String = sha256(value.toByteArray(Charsets.UTF_8))
    private fun sha256(value: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(value).joinToString("") { "%02x".format(it) }
    private fun issue(severity: String, code: String, path: String, message: String) = DataPackIssue(ImportSeverity.valueOf(severity), code, path, message)
    private fun failed(code: String, path: String, message: String) = parsedFailure(listOf(issue("ERROR", code, path, message)))
    private fun parsedFailure(issues: List<DataPackIssue>) = ParsedDataPack(null, DataPackImportReport(null, false, null, DataModule.entries.map { ModuleImportReport(it.name, 0, 0.0) }, issues))
}

