package com.example

import com.example.datapack.DataPackDocument
import com.example.datapack.DataPackParser
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DataPackParserTest {
    private val sampleBytes by lazy {
        javaClass.classLoader!!.getResourceAsStream("datapacks/tff-super-lig-2026-27-sample.json")!!.use { it.readBytes() }
    }
    private val sampleZipBytes by lazy {
        javaClass.classLoader!!.getResourceAsStream("datapacks/tff-super-lig-2026-27-sample.zip")!!.use { it.readBytes() }
    }
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val documentAdapter = moshi.adapter(DataPackDocument::class.java)

    @Test fun officialSampleValidatesWithoutInventedPlayersOrMedia() {
        val result = DataPackParser.parse("sample.json", sampleBytes)
        assertTrue(result.report.issues.joinToString { it.message }, result.report.accepted)
        assertEquals(3, result.document!!.records.size)
        assertEquals(0, result.report.moduleReports.single { it.module == "Players" }.rows)
        assertEquals(0, result.document!!.mediaAssets.size)
        assertEquals("2026-11-14", result.document!!.metadata.snapshotDate)
        assertEquals(2, result.report.moduleReports.single { it.module == "Clubs" }.rows)
    }

    @Test fun checksumTamperingIsRejected() {
        val tampered = sampleBytes.toString(Charsets.UTF_8).replace("ARCA ÇORUM FK", "FAKE ÇORUM FK").toByteArray()
        val result = DataPackParser.parse("tampered.json", tampered)
        assertFalse(result.report.accepted)
        assertTrue(result.report.issues.any { it.code == "CHECKSUM_MISMATCH" })
        assertEquals(null, result.document)
    }

    @Test fun unmatchedInternalIdIsReported() {
        val tampered = sampleBytes.toString(Charsets.UTF_8).replace("tff-league-tr-superlig\",\n        \"country", "unknown-league\",\n        \"country")
        val result = DataPackParser.parse("unmatched.json", tampered.toByteArray())
        assertFalse(result.report.accepted)
        assertTrue(result.report.issues.any { it.code == "UNMATCHED_INTERNAL_ID" })
        assertTrue(result.report.moduleReports.single { it.module == "Clubs" }.unmatchedIds.isNotEmpty())
    }

    @Test fun missingRequiredGameplayFieldsAreRejectedAndReported() {
        val original = documentAdapter.fromJson(sampleBytes.toString(Charsets.UTF_8))!!
        val changedRecords = original.records.mapIndexed { index, record ->
            if (index != 1) record else record.copy(data = record.data - "name")
        }
        val changed = original.copy(records = changedRecords)
        val checksum = MessageDigest.getInstance("SHA-256").digest(DataPackParser.canonicalPayload(changed).toByteArray()).joinToString("") { "%02x".format(it) }
        val bytes = documentAdapter.toJson(changed.copy(metadata = changed.metadata.copy(checksum = checksum))).toByteArray()
        val result = DataPackParser.parse("missing-field.json", bytes)
        assertFalse(result.report.accepted)
        assertTrue(result.report.issues.any { it.code == "REQUIRED_FIELD_MISSING" && it.path.endsWith("data.name") })
        assertEquals(1, result.report.moduleReports.single { it.module == "Clubs" }.errors)
    }

    @Test fun csvUsesTheSameCanonicalChecksumAndProvenanceRules() {
        val doc = documentAdapter.fromJson(sampleBytes.toString(Charsets.UTF_8))!!
        val out = StringBuilder()
        out.appendLine("#packId=${doc.metadata.packId}")
        out.appendLine("#season=${doc.metadata.season}")
        out.appendLine("#snapshotDate=${doc.metadata.snapshotDate}")
        out.appendLine("#source=${doc.metadata.source}")
        out.appendLine("#version=${doc.metadata.version}")
        out.appendLine("#checksum=${doc.metadata.checksum}")
        out.appendLine("module,internalId,parentInternalId,source,validatedAt,missingValues,dataJson")
        val mapAdapter = moshi.adapter<Map<String, Any?>>(Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java))
        doc.records.forEach { record ->
            val cells = listOf(record.module, record.internalId, record.parentInternalId.orEmpty(), record.source, record.validatedAt, record.missingValues.joinToString("|"), mapAdapter.toJson(record.data))
            out.appendLine(cells.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" })
        }
        val result = DataPackParser.parse("sample.csv", out.toString().toByteArray())
        assertTrue(result.report.issues.joinToString { it.message }, result.report.accepted)
        assertEquals(3, result.document!!.records.size)
    }

    @Test fun zipPackageReadsItsManifestAndRejectsPathTraversal() {
        val shippedZip = DataPackParser.parse("tff-sample.zip", sampleZipBytes)
        assertTrue(shippedZip.report.issues.joinToString { it.message }, shippedZip.report.accepted)
        assertEquals(3, shippedZip.document!!.records.size)
        val validZip = zipOf("datapack.json" to sampleBytes)
        val valid = DataPackParser.parse("sample.zip", validZip)
        assertTrue(valid.report.issues.joinToString { it.message }, valid.report.accepted)
        val hostile = zipOf("datapack.json" to sampleBytes, "../outside.txt" to byteArrayOf(1))
        val rejected = DataPackParser.parse("hostile.zip", hostile)
        assertFalse(rejected.report.accepted)
        assertTrue(rejected.report.issues.any { it.code == "ZIP_PATH" })
    }

    @Test fun reportsAllModulesAndDeclaredMissingValues() {
        val document = documentAdapter.fromJson(sampleBytes.toString(Charsets.UTF_8))!!
        assertEquals(12, DataPackParser.parse("sample.json", sampleBytes).report.moduleReports.size)
        assertEquals(2.0 / 3.0, DataPackParser.parse("sample.json", sampleBytes).report.moduleReports.single { it.module == "Clubs" }.missingValueRate, 0.0001)
        assertEquals(2.0 / 3.0, DataPackParser.parse("sample.json", sampleBytes).report.moduleReports.single { it.module == "Clubs" }.missingValueRate, 0.0001)
        assertEquals("TFF 2026-2027 Süper Lig 1. hafta fikstürü", document.records[1].source)
    }

    private fun zipOf(vararg entries: Pair<String, ByteArray>): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip -> entries.forEach { (name, bytes) -> zip.putNextEntry(ZipEntry(name)); zip.write(bytes); zip.closeEntry() } }
        return output.toByteArray()
    }
}

