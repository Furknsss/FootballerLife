package com.example.datapack

import com.squareup.moshi.JsonClass

/** Stable vocabulary used by packs and stored verbatim in the local world database. */
enum class DataModule {
    Leagues, Clubs, Players, Squads, Coaches, Contracts, Transfers,
    PlayerAttributes, PlayerValuations, ClubLogos, PlayerPortraits, Kits
}

@JsonClass(generateAdapter = true)
data class DataPackMetadata(
    val packId: String,
    val season: String,
    val snapshotDate: String,
    val source: String,
    val version: String,
    /** SHA-256 of canonical `records` + `mediaAssets` content, excluding this metadata field. */
    val checksum: String,
    val importedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class DataPackRecord(
    val module: String,
    val internalId: String,
    val parentInternalId: String? = null,
    val source: String,
    val validatedAt: String,
    val missingValues: List<String> = emptyList(),
    val data: Map<String, Any?>
)

@JsonClass(generateAdapter = true)
data class DataPackMedia(
    val module: String,
    val internalId: String,
    val kind: String,
    val path: String,
    val sha256: String
)

@JsonClass(generateAdapter = true)
data class DataPackDocument(
    val metadata: DataPackMetadata,
    val records: List<DataPackRecord>,
    val mediaAssets: List<DataPackMedia> = emptyList(),
    val externalIds: List<ExternalIdInput> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ExternalIdInput(
    val module: String,
    val internalId: String,
    val sourceSystem: String,
    val externalId: String
)

enum class ImportSeverity { ERROR, WARNING }

@JsonClass(generateAdapter = true)
data class DataPackIssue(
    val severity: ImportSeverity,
    val code: String,
    val path: String,
    val message: String
)

@JsonClass(generateAdapter = true)
data class ModuleImportReport(
    val module: String,
    val rows: Int,
    val missingValueRate: Double,
    val unmatchedIds: List<String> = emptyList(),
    val errors: Int = 0
)

@JsonClass(generateAdapter = true)
data class DataPackImportReport(
    val packId: String?,
    val accepted: Boolean,
    val checksum: String?,
    val moduleReports: List<ModuleImportReport>,
    val issues: List<DataPackIssue>,
    val importedAt: String? = null
) {
    val errorCount: Int get() = issues.count { it.severity == ImportSeverity.ERROR }
    val warningCount: Int get() = issues.count { it.severity == ImportSeverity.WARNING }
}

data class ParsedDataPack(
    val document: DataPackDocument?,
    val report: DataPackImportReport,
    /** Archive-relative paths. Bytes are held only during validated import; images are not decoded. */
    val mediaBytes: Map<String, ByteArray> = emptyMap()
)

data class InstalledDataPackSummary(
    val packKey: String,
    val packId: String,
    val season: String,
    val snapshotDate: String,
    val source: String,
    val version: String,
    val checksum: String,
    val importedAt: String,
    val recordCount: Int,
    val missingValueRate: Double,
    val reportJson: String
)

