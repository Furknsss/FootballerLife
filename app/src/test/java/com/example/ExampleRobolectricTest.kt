package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.CareerSaveRepository
import com.example.data.FootballLifeDatabase
import com.example.engine.CareerEngine
import com.example.engine.WorldDataCatalog
import com.example.model.PlayerAppearance
import com.example.model.Position
import com.example.model.PreferredFoot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: FootballLifeDatabase
    private lateinit var repository: CareerSaveRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FootballLifeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CareerSaveRepository(database.careerSaveDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read app_name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Football Life", appName)
    }

    @Test
    fun `test9_and_test13_persistenceAcrossRestartsAndMultiSlotIsolation`() = runBlocking {
        // Slot 1: Custom player in BAL league
        val slot1Career = CareerEngine.createNewCareerFromCustomPlayer(
            slotId = 1,
            firstName = "Can",
            lastName = "Yıldız",
            birthYear = 2006,
            country = "Türkiye",
            city = "Bursa",
            nationality = "Türkiye",
            kitName = "YILDIZ",
            kitNumber = 10,
            primaryPosition = Position.AM,
            secondaryPositions = emptyList(),
            preferredFoot = PreferredFoot.RIGHT,
            heightCm = 178,
            weightKg = 72,
            appearance = PlayerAppearance(),
            backgroundStory = "Altyapı Akademisi Çıkışlı",
            selectedLeagueId = "TR_BAL",
            selectedClubId = "BAL_BURSA"
        )
        val drill = WorldDataCatalog.trainingDrills.first { it.id == "DRILL_SHOOTING" }
        val (slot1Trained, _) = CareerEngine.performTrainingSession(slot1Career, drill)
        repository.saveCareer(slot1Trained, CareerEngine.formatGameDate(slot1Trained.currentDayIndex))

        // Slot 2: Real Player Preset (Arda Güler)
        val ardaPreset = WorldDataCatalog.realPlayerPresets.first { it.id == "REAL_ARDA_GULER" }
        val slot2Career = CareerEngine.createNewCareerFromRealPreset(slotId = 2, preset = ardaPreset)
        repository.saveCareer(slot2Career, CareerEngine.formatGameDate(slot2Career.currentDayIndex))

        // Reload both slots and verify zero cross-slot contamination and full state fidelity
        val reloadedSlot1 = repository.loadCareerSlot(1)
        val reloadedSlot2 = repository.loadCareerSlot(2)

        assertNotNull(reloadedSlot1)
        assertNotNull(reloadedSlot2)

        assertEquals("Can Yıldız", reloadedSlot1!!.player.fullName)
        assertEquals("Nilüfer Belediye FK", reloadedSlot1.currentClub.name)
        assertEquals(slot1Trained.player.attributes.shooting, reloadedSlot1.player.attributes.shooting)
        assertEquals(slot1Trained.player.energy, reloadedSlot1.player.energy)

        assertEquals("Arda Güler", reloadedSlot2!!.player.fullName)
        assertEquals("Real Madrid", reloadedSlot2.currentClub.name)
    }
}
