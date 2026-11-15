package com.example.datapack

import android.content.ContentResolver
import android.net.Uri
import androidx.room.withTransaction
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.time.Instant

class DataPackRepository(
    private val resolver: ContentResolver,
    private val database: WorldDataDatabase,
    private val filesDir: File
) {
    private val dao = database.worldDataDao()
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val reportAdapter = moshi.adapter(DataPackImportReport::class.java)
    private val anyMapAdapter = moshi.adapter<Map<String, Any?>>(Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java))
    private val stringListAdapter = moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java))

    val installedPacks: Flow<List<InstalledDataPackEntity>> = dao.observeInstalledPacks()

    suspend fun importUri(uri: Uri): DataPackImportReport = withContext(Dispatchers.IO) {
        val name = resolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        } ?: "datapack"
        val bytes = resolver.openInputStream(uri)?.use { input ->
            val out = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(8192); var total = 0
            while (true) {
                val count = input.read(buffer); if (count < 0) break
                total += count
                if (total > DataPackParser.MAX_INPUT_BYTES) return@withContext DataPackImportReport(null, false, null, emptyList(), listOf(DataPackIssue(ImportSeverity.ERROR, "SIZE_LIMIT", name, "Dosya 32 MB sınırını aşıyor.")))
                out.write(buffer, 0, count)
            }
            out.toByteArray()
        } ?: return@withContext DataPackImportReport(null, false, null, emptyList(), listOf(DataPackIssue(ImportSeverity.ERROR, "FILE_READ", name, "Seçilen dosya okunamadı.")))
        install(DataPackParser.parse(name, bytes))
    }

    suspend fun install(parsed: ParsedDataPack): DataPackImportReport = withContext(Dispatchers.IO) {
        val now = Instant.now().toString()
        val document = parsed.document
        if (document == null || !parsed.report.accepted) {
            persistIssues(parsed.report, now)
            return@withContext parsed.report
        }
        val metadata = document.metadata
        val packKey = packKey(metadata.packId, metadata.version)
        val previous = dao.getPack(packKey)
        if (previous != null) {
            val duplicate = previous.checksum.equals(metadata.checksum, true) && previous.version == metadata.version
            val issue = DataPackIssue(
                if (duplicate) ImportSeverity.WARNING else ImportSeverity.ERROR,
                if (duplicate) "ALREADY_INSTALLED" else "PACK_ID_CONFLICT",
                "metadata.packId",
                if (duplicate) "Bu paket sürümü zaten kurulu; mevcut kurulum korundu." else "packId mevcut bir checksum/sürümle kullanılıyor. Güncelleme için yeni sürüm veya packId kullanın."
            )
            val result = parsed.report.copy(accepted = duplicate, issues = parsed.report.issues + issue)
            persistIssues(result, now)
            return@withContext result
        }

        val records = document.records.map { record ->
            val module = DataModule.entries.first { it.name.equals(record.module, true) }
            WorldDataRecordEntity(
                packKey = packKey, module = module.name, internalId = record.internalId,
                parentInternalId = record.parentInternalId,
                payloadJson = anyMapAdapter.toJson(record.data), source = record.source,
                validatedAt = record.validatedAt, missingValuesJson = stringListAdapter.toJson(record.missingValues)
            )
        }
        val mediaEntities = document.mediaAssets.map { asset ->
            WorldDataMediaEntity(packKey, DataModule.entries.first { it.name.equals(asset.module, true) }.name, asset.internalId, asset.kind, asset.path, asset.sha256)
        }
        val mappings = document.externalIds.map { mapping ->
            WorldExternalIdMappingEntity(DataModule.entries.first { it.name.equals(mapping.module, true) }.name, mapping.internalId, mapping.sourceSystem, mapping.externalId, packKey)
        }
        for (mapping in mappings) {
            val previousMapping = dao.findByExternalId(mapping.sourceSystem, mapping.externalId)
            if (previousMapping != null && (previousMapping.module != mapping.module || previousMapping.internalId != mapping.internalId)) {
                val result = parsed.report.copy(accepted = false, issues = parsed.report.issues + DataPackIssue(
                    ImportSeverity.ERROR, "EXTERNAL_MAPPING_CONFLICT", "externalIds.${mapping.sourceSystem}.${mapping.externalId}",
                    "Bu external ID daha önce farklı bir internal ID ile eşleştirilmiş."
                ))
                persistIssues(result, now)
                return@withContext result
            }
        }

        val packRoot = File(filesDir, "world-datapacks/$packKey")
        try {
            parsed.mediaBytes.forEach { (relative, content) ->
                val safe = File(packRoot, relative.removePrefix("media/")).canonicalFile
                require(safe.path.startsWith(packRoot.canonicalPath + File.separator)) { "Unsafe media destination" }
                safe.parentFile?.mkdirs()
                safe.writeBytes(content)
            }
            val moduleReports = parsed.report.moduleReports
            val totalRecords = records.size
            val missingRate = if (totalRecords == 0) 0.0 else moduleReports.sumOf { it.missingValueRate * it.rows } / totalRecords
            val reportJson = reportAdapter.toJson(parsed.report)
            val pack = InstalledDataPackEntity(
                packKey = packKey, packId = metadata.packId, season = metadata.season, snapshotDate = metadata.snapshotDate,
                source = metadata.source, version = metadata.version, checksum = metadata.checksum.lowercase(),
                importedAt = now, recordCount = totalRecords, missingValueRate = missingRate, reportJson = reportJson
            )
            val issues = parsed.report.issues.mapIndexed { index, issue ->
                DataPackIssueEntity("$packKey:$index:${issue.code}", packKey, issue.severity.name, issue.code, issue.path, issue.message, now)
            }
            database.withTransaction {
                dao.insertPack(pack)
                if (records.isNotEmpty()) dao.insertRecords(records)
                if (mappings.isNotEmpty()) dao.insertExternalMappings(mappings)
                if (mediaEntities.isNotEmpty()) dao.insertMedia(mediaEntities)
                if (issues.isNotEmpty()) dao.replaceIssues(issues)
            }
            parsed.report.copy(importedAt = now)
        } catch (failure: Exception) {
            packRoot.deleteRecursively()
            val result = parsed.report.copy(accepted = false, issues = parsed.report.issues + DataPackIssue(
                ImportSeverity.ERROR, "INSTALL_FAILED", "database", failure.message?.take(240) ?: "Paket kurulumu tamamlanamadı."
            ))
            persistIssues(result, now)
            result
        }
    }

    suspend fun packSummaries(): List<InstalledDataPackSummary> = dao.getInstalledPacks().map { pack ->
        InstalledDataPackSummary(pack.packKey, pack.packId, pack.season, pack.snapshotDate, pack.source, pack.version, pack.checksum, pack.importedAt, pack.recordCount, pack.missingValueRate, pack.reportJson)
    }

    suspend fun recordsForNewCareer(packKey: String): List<WorldDataRecordEntity> = dao.recordsForPack(packKey)

    /** Resolves only the requested image by stable IDs. Call from a lazy/visible UI item. */
    suspend fun openMedia(packKey: String, module: String, internalId: String, kind: String): java.io.InputStream? = withContext(Dispatchers.IO) {
        val media = dao.getMedia(packKey, module, internalId, kind) ?: return@withContext null
        val root = File(filesDir, "world-datapacks/$packKey").canonicalFile
        val image = File(root, media.relativePath.removePrefix("media/")).canonicalFile
        if (!image.path.startsWith(root.path + File.separator) || !image.isFile) return@withContext null
        val bytes = image.readBytes()
        if (!sha256(bytes).equals(media.sha256, true)) return@withContext null
        image.inputStream()
    }

    private suspend fun persistIssues(report: DataPackImportReport, now: String) {
        val rows = report.issues.mapIndexed { index, issue ->
            DataPackIssueEntity("${report.packId ?: "unknown"}:$now:$index:${issue.code}", report.packId, issue.severity.name, issue.code, issue.path, issue.message, now)
        }
        if (rows.isNotEmpty()) dao.replaceIssues(rows)
    }

    private fun sha256(value: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(value).joinToString("") { "%02x".format(it) }

    private fun packKey(packId: String, version: String): String = "$packId@$version"
}

