package com.example.datapack

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import android.content.Context
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "installed_data_packs")
data class InstalledDataPackEntity(
    @PrimaryKey val packKey: String,
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

@Entity(
    tableName = "world_data_records",
    primaryKeys = ["packKey", "module", "internalId"],
    indices = [Index("packKey"), Index("packKey", "module"), Index("module", "internalId")]
)
data class WorldDataRecordEntity(
    val packKey: String,
    val module: String,
    val internalId: String,
    val parentInternalId: String?,
    val payloadJson: String,
    val source: String,
    val validatedAt: String,
    val missingValuesJson: String
)

@Entity(
    tableName = "world_external_id_mappings",
    primaryKeys = ["module", "internalId", "sourceSystem", "externalId"],
    indices = [Index("internalId"), Index("sourceSystem", "externalId")]
)
data class WorldExternalIdMappingEntity(
    val module: String,
    val internalId: String,
    val sourceSystem: String,
    val externalId: String,
    val addedInPackKey: String
)

@Entity(
    tableName = "world_data_media",
    primaryKeys = ["packKey", "module", "internalId", "kind"],
    indices = [Index("packKey", "module", "internalId")]
)
data class WorldDataMediaEntity(
    val packKey: String,
    val module: String,
    val internalId: String,
    val kind: String,
    val relativePath: String,
    val sha256: String
)

@Entity(tableName = "data_pack_import_issues", indices = [Index("packKey")])
data class DataPackIssueEntity(
    @PrimaryKey val issueId: String,
    val packKey: String?,
    val severity: String,
    val code: String,
    val path: String,
    val message: String,
    val importedAt: String
)

@Dao
interface WorldDataDao {
    @Query("SELECT * FROM installed_data_packs ORDER BY importedAt DESC")
    fun observeInstalledPacks(): Flow<List<InstalledDataPackEntity>>

    @Query("SELECT * FROM installed_data_packs ORDER BY importedAt DESC")
    suspend fun getInstalledPacks(): List<InstalledDataPackEntity>

    @Query("SELECT * FROM installed_data_packs WHERE packKey = :packKey LIMIT 1")
    suspend fun getPack(packKey: String): InstalledDataPackEntity?

    @Query("SELECT * FROM world_data_records WHERE packKey = :packKey ORDER BY module, internalId")
    suspend fun recordsForPack(packKey: String): List<WorldDataRecordEntity>

    @Query("SELECT * FROM world_data_records WHERE packKey = :packKey AND module = :module AND internalId = :internalId LIMIT 1")
    suspend fun getRecord(packKey: String, module: String, internalId: String): WorldDataRecordEntity?

    @Query("SELECT * FROM world_data_media WHERE packKey = :packKey AND module = :module AND internalId = :internalId AND kind = :kind LIMIT 1")
    suspend fun getMedia(packKey: String, module: String, internalId: String, kind: String): WorldDataMediaEntity?

    @Query("SELECT * FROM world_external_id_mappings WHERE sourceSystem = :sourceSystem AND externalId = :externalId LIMIT 1")
    suspend fun findByExternalId(sourceSystem: String, externalId: String): WorldExternalIdMappingEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPack(pack: InstalledDataPackEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRecords(records: List<WorldDataRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExternalMappings(mappings: List<WorldExternalIdMappingEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertMedia(media: List<WorldDataMediaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun replaceIssues(issues: List<DataPackIssueEntity>)

    @Query("DELETE FROM data_pack_import_issues WHERE packKey = :packKey")
    suspend fun deleteIssuesForPack(packKey: String)
}

/** Intentionally separate from the career save DB: DataPack installs cannot migrate or rewrite careers. */
@Database(
    entities = [InstalledDataPackEntity::class, WorldDataRecordEntity::class, WorldExternalIdMappingEntity::class, WorldDataMediaEntity::class, DataPackIssueEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WorldDataDatabase : RoomDatabase() {
    abstract fun worldDataDao(): WorldDataDao

    companion object {
        @Volatile private var instance: WorldDataDatabase? = null
        fun getInstance(context: Context): WorldDataDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, WorldDataDatabase::class.java, "football_life_world_data.db")
                .build().also { instance = it }
        }
    }
}

