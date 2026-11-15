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
    fun sceneCheckpointMemoryAndDecisionsSurviveRepositoryRoundTrip() = runBlocking {
        val trained = CareerEngine.performTrainingSession(testCareer(), WorldDataCatalog.trainingDrills.first { it.id == "DRILL_PASSING" }).first
        val task = trained.livingWorld.tasks.first { it.action == "COACH" }
        val ready = readyScene(trained, task)
        val chatted = com.example.engine.NpcDialogueEngine.send(ready, "NPC_MANAGER", "Hocam daha fazla süre istiyorum.")
        repository.saveCareer(chatted, "test")
        val firstLoad = repository.loadCareerSlot(1)!!
        assertEquals(chatted.livingWorld.activeScene, firstLoad.livingWorld.activeScene)
        val decided = com.example.engine.CinematicEngine.choose(firstLoad, "work").first
        repository.saveCareer(decided, "test")
        val restarted = repository.loadCareerSlot(1)!!
        assertEquals(decided.managerTrust, restarted.managerTrust)
        assertEquals(decided.livingWorld.memories, restarted.livingWorld.memories)
        assertEquals(decided.livingWorld.tasks, restarted.livingWorld.tasks)
        assertEquals(restarted, com.example.engine.CinematicEngine.choose(restarted, "work").first)
    }

    @Test
    fun corruptedPrimaryRecoversPreviousSnapshotWithoutDeletingCareer() = runBlocking {
        val state = testCareer()
        repository.saveCareer(state, "test")
        repository.saveCareer(state.copy(balanceEur = state.balanceEur + 100), "test")
        val dao = database.careerSaveDao()
        val entity = dao.getSaveBySlot(1)!!
        dao.upsertSave(entity.copy(stateJson = "{corrupted"))
        val recovered = repository.loadCareerSlot(1)!!
        assertEquals(state.player, recovered.player)
        assertEquals(state.balanceEur, recovered.balanceEur)
    }

    @Test
    fun careerTrainingCoachMatchInjuryTreatmentTransferAndReloadStayConsistent() = runBlocking {
        var world = CareerEngine.performTrainingSession(testCareer(), WorldDataCatalog.trainingDrills.first { it.id == "DRILL_PASSING" }).first
        val coach = world.livingWorld.tasks.first { it.action == "COACH" }
        world = com.example.engine.NpcDialogueEngine.send(readyScene(world, coach), "NPC_MANAGER", "Forma için daha çok çalışacağım.")
        world = com.example.engine.CinematicEngine.choose(world, "work").first
        world = com.example.engine.LivingWorldEngine.dismissCompletedScene(world)
        val fixture = world.nextUnplayedUserFixture!!
        // Select a deterministic seed that exercises the injury branch, rather than flaky RNG.
        val injuryId = (1..1000).map { "INJURY_FIXTURE:$it" }.first {
            kotlin.random.Random(it.hashCode().toLong() + world.player.id.hashCode()).nextInt(100) == 0
        }
        val seededFixture = fixture.copy(id = injuryId)
        world = world.copy(currentDayIndex = fixture.dayIndex,
            fixtures = world.fixtures.map { if (it.id == fixture.id) seededFixture else it },
            calendarEvents = world.calendarEvents.map { if (it.fixtureId == fixture.id) it.copy(fixtureId = injuryId) else it })
        val engine = com.example.engine.MatchSimulationEngine()
        val home = world.clubs.first { it.id == fixture.homeClubId }
        val away = world.clubs.first { it.id == fixture.awayClubId }
        val live = engine.createInitialMatchState(seededFixture, home, away, world.player, world.contract.clubId, com.example.model.StartingStatus.STARTER)
            .copy(phase = com.example.engine.MatchPhase.FULL_TIME, minute = 90, userGoals = 1, userLiveRating = 8.2)
        val report = engine.buildFinalMatchReport(live, world.player)
        world = CareerEngine.applyCompletedMatch(world, report)
        org.junit.Assert.assertTrue(world.player.injuryDaysRemaining > 0)
        val medical = world.livingWorld.tasks.first { it.action == "MEDICAL" && it.isActive }
        world = com.example.engine.CinematicEngine.choose(readyScene(world, medical), "physio").first
        world = com.example.engine.LivingWorldEngine.dismissCompletedScene(world)
        org.junit.Assert.assertFalse(world.livingWorld.tasks.first { it.id == medical.id }.isActive)
        // Exercise calendar recovery without requiring another playable fixture during treatment.
        world = world.copy(fixtures = world.fixtures.filter { it.played })
        repeat(world.player.injuryDaysRemaining) { world = CareerEngine.advanceToNextDay(world).first }
        assertEquals(0, world.player.injuryDaysRemaining)
        world = testOffer(world)
        val offer = world.transferOffers.first()
        val process = com.example.engine.TransferEngine.process(world, offer)
        world = com.example.engine.TransferEngine.negotiate(world, offer.id, process.terms.copy(weeklyWageEur = 1600)).first
        world = com.example.engine.TransferEngine.agree(world, offer.id).first
        world = com.example.engine.TransferEngine.medical(world, offer.id).first
        world = com.example.engine.TransferEngine.register(world, offer.id).first
        assertEquals(offer.clubId, world.contract.clubId)
        repository.saveCareer(world, "integration")
        val restarted = repository.loadCareerSlot(1)!!
        assertEquals(world.player, restarted.player)
        assertEquals(world.contract, restarted.contract)
        assertEquals(world.balanceEur, restarted.balanceEur)
        assertEquals(world.livingWorld.memories, restarted.livingWorld.memories)
        assertEquals(world.livingWorld.medical, restarted.livingWorld.medical)
        assertEquals(world.transferHistory, restarted.transferHistory)
        assertEquals(world.calendarEvents, restarted.calendarEvents)
        assertEquals(world.livingWorld.tasks, restarted.livingWorld.tasks)
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
