package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.CareerSaveState
import com.example.model.GraphicsQuality
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "football_life_settings")

data class UserSettings(
    val stadiumAmbienceEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val graphicsQuality: GraphicsQuality = GraphicsQuality.HIGH,
    val skipIntroAnimation: Boolean = false,
    val lastActiveSlotId: Int = 1,
    val accountDisplayName: String = "Misafir Oyuncu (Yerel Kariyer)",
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-3.8-flash",
    val aiDialoguesEnabled: Boolean = true,
    val autoFailoverEnabled: Boolean = true
)

class SettingsRepository(private val context: Context) {
    private object Keys {
        val AMBIENCE = booleanPreferencesKey("stadium_ambience")
        val SFX = booleanPreferencesKey("sound_effects")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val GRAPHICS = stringPreferencesKey("graphics_quality")
        val SKIP_INTRO = booleanPreferencesKey("skip_intro")
        val LAST_SLOT = intPreferencesKey("last_active_slot")
        val ACCOUNT_NAME = stringPreferencesKey("account_display_name")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val GEMINI_MODEL = stringPreferencesKey("gemini_model")
        val AI_ENABLED = booleanPreferencesKey("ai_dialogues_enabled")
        val AUTO_FAILOVER = booleanPreferencesKey("gemini_auto_failover")
    }

    val settingsFlow: Flow<UserSettings> = context.settingsDataStore.data.map { prefs ->
        val gfxName = prefs[Keys.GRAPHICS] ?: GraphicsQuality.HIGH.name
        val gfx = runCatching { GraphicsQuality.valueOf(gfxName) }.getOrDefault(GraphicsQuality.HIGH)
        UserSettings(
            stadiumAmbienceEnabled = prefs[Keys.AMBIENCE] ?: true,
            soundEffectsEnabled = prefs[Keys.SFX] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS] ?: true,
            graphicsQuality = gfx,
            skipIntroAnimation = prefs[Keys.SKIP_INTRO] ?: false,
            lastActiveSlotId = prefs[Keys.LAST_SLOT] ?: 1,
            accountDisplayName = prefs[Keys.ACCOUNT_NAME] ?: "Misafir Oyuncu (Yerel Kariyer)",
            geminiApiKey = prefs[Keys.GEMINI_API_KEY] ?: "",
            geminiModel = prefs[Keys.GEMINI_MODEL] ?: "gemini-3.8-flash",
            aiDialoguesEnabled = prefs[Keys.AI_ENABLED] ?: true,
            autoFailoverEnabled = prefs[Keys.AUTO_FAILOVER] ?: true
        )
    }

    suspend fun updateAmbience(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.AMBIENCE] = enabled }
    }

    suspend fun updateSfx(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.SFX] = enabled }
    }

    suspend fun updateHaptics(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.HAPTICS] = enabled }
    }

    suspend fun updateGraphicsQuality(quality: GraphicsQuality) {
        context.settingsDataStore.edit { it[Keys.GRAPHICS] = quality.name }
    }

    suspend fun updateSkipIntro(skip: Boolean) {
        context.settingsDataStore.edit { it[Keys.SKIP_INTRO] = skip }
    }

    suspend fun setLastActiveSlot(slotId: Int) {
        context.settingsDataStore.edit { it[Keys.LAST_SLOT] = slotId }
    }

    suspend fun updateAccountName(name: String) {
        context.settingsDataStore.edit { it[Keys.ACCOUNT_NAME] = name }
    }

    suspend fun updateGeminiApiKey(key: String) {
        context.settingsDataStore.edit { it[Keys.GEMINI_API_KEY] = key.trim() }
    }

    suspend fun updateGeminiModel(model: String) {
        context.settingsDataStore.edit { it[Keys.GEMINI_MODEL] = model.trim() }
    }

    suspend fun updateAiDialoguesEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.AI_ENABLED] = enabled }
    }

    suspend fun updateAutoFailoverEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.AUTO_FAILOVER] = enabled }
    }
}

@Entity(tableName = "career_saves")
data class CareerSaveEntity(
    @PrimaryKey val slotId: Int,
    val saveVersion: Int,
    val careerTitle: String,
    val playerName: String,
    val positionCode: String,
    val clubName: String,
    val leagueName: String,
    val overallRating: Int,
    val currentDayIndex: Int,
    val dateLabel: String,
    val balanceEur: Long,
    val updatedAtMs: Long,
    val stateJson: String,
    val backupStateJson: String? = null
)

data class CareerSaveSummary(
    val slotId: Int,
    val saveVersion: Int,
    val careerTitle: String,
    val playerName: String,
    val positionCode: String,
    val clubName: String,
    val leagueName: String,
    val overallRating: Int,
    val currentDayIndex: Int,
    val dateLabel: String,
    val balanceEur: Long,
    val updatedAtMs: Long
)

@Dao
interface CareerSaveDao {
    @Query("SELECT * FROM career_saves ORDER BY slotId ASC")
    fun observeAllSaves(): Flow<List<CareerSaveEntity>>

    @Query("SELECT * FROM career_saves WHERE slotId = :slotId LIMIT 1")
    suspend fun getSaveBySlot(slotId: Int): CareerSaveEntity?

    @Query("SELECT * FROM career_saves ORDER BY updatedAtMs DESC LIMIT 1")
    suspend fun getMostRecentSave(): CareerSaveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSave(entity: CareerSaveEntity)

    @Query("DELETE FROM career_saves WHERE slotId = :slotId")
    suspend fun deleteSlot(slotId: Int)
}

@Database(entities = [CareerSaveEntity::class], version = 1, exportSchema = false)
abstract class FootballLifeDatabase : RoomDatabase() {
    abstract fun careerSaveDao(): CareerSaveDao

    companion object {
        @Volatile
        private var INSTANCE: FootballLifeDatabase? = null

        fun getInstance(context: Context): FootballLifeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FootballLifeDatabase::class.java,
                    "football_life_career.db"
                ).fallbackToDestructiveMigration(true).build().also { INSTANCE = it }
            }
        }
    }
}

class CareerSaveRepository(private val dao: CareerSaveDao) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val stateAdapter = moshi.adapter(CareerSaveState::class.java)

    val saveSummariesFlow: Flow<List<CareerSaveSummary>> = dao.observeAllSaves().map { list ->
        list.map { entity ->
            CareerSaveSummary(
                slotId = entity.slotId,
                saveVersion = entity.saveVersion,
                careerTitle = entity.careerTitle,
                playerName = entity.playerName,
                positionCode = entity.positionCode,
                clubName = entity.clubName,
                leagueName = entity.leagueName,
                overallRating = entity.overallRating,
                currentDayIndex = entity.currentDayIndex,
                dateLabel = entity.dateLabel,
                balanceEur = entity.balanceEur,
                updatedAtMs = entity.updatedAtMs
            )
        }
    }

    suspend fun saveCareer(state: CareerSaveState, dateLabel: String) = withContext(Dispatchers.IO) {
        val existing = dao.getSaveBySlot(state.saveSlotId)
        val json = stateAdapter.toJson(state)
        val club = state.currentClub
        val entity = CareerSaveEntity(
            slotId = state.saveSlotId,
            saveVersion = state.saveVersion,
            careerTitle = state.careerTitle,
            playerName = state.player.fullName,
            positionCode = state.player.primaryPosition.code,
            clubName = club.name,
            leagueName = club.leagueName,
            overallRating = state.player.overallRating,
            currentDayIndex = state.currentDayIndex,
            dateLabel = dateLabel,
            balanceEur = state.balanceEur,
            updatedAtMs = System.currentTimeMillis(),
            stateJson = json,
            backupStateJson = existing?.stateJson ?: json
        )
        dao.upsertSave(entity)
    }

    suspend fun loadCareerSlot(slotId: Int): CareerSaveState? = withContext(Dispatchers.IO) {
        val entity = dao.getSaveBySlot(slotId) ?: return@withContext null
        decodeWithRecovery(entity)
    }

    suspend fun loadMostRecentCareer(): CareerSaveState? = withContext(Dispatchers.IO) {
        val entity = dao.getMostRecentSave() ?: return@withContext null
        decodeWithRecovery(entity)
    }

    suspend fun deleteCareerSlot(slotId: Int) = withContext(Dispatchers.IO) {
        dao.deleteSlot(slotId)
    }

    suspend fun getAllSummariesSnapshot(): List<CareerSaveSummary> = withContext(Dispatchers.IO) {
        saveSummariesFlow.first()
    }

    private fun decodeWithRecovery(entity: CareerSaveEntity): CareerSaveState? {
        val primary = runCatching { stateAdapter.fromJson(entity.stateJson) }.getOrNull()
        if (primary != null) {
            return migrateIfNeeded(primary)
        }
        val backupJson = entity.backupStateJson ?: return null
        val backup = runCatching { stateAdapter.fromJson(backupJson) }.getOrNull()
        return backup?.let { migrateIfNeeded(it) }
    }

    private fun migrateIfNeeded(state: CareerSaveState): CareerSaveState {
        return com.example.engine.CareerEngine.ensureVersion3EnrichedState(state)
    }
}
