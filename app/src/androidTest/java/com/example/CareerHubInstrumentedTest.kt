package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.CareerSaveRepository
import com.example.data.FootballLifeDatabase
import com.example.engine.CareerEngine
import com.example.engine.WorldDataCatalog
import com.example.model.*
import com.example.viewmodel.FootballLifeViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the actual activity, view model, modal scene and Room-backed save queue. */
@RunWith(AndroidJUnit4::class)
class CareerHubInstrumentedTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun actualCareerHubTrainingMeetingAndReloadPreserveDecision() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val repository = CareerSaveRepository(FootballLifeDatabase.getInstance(context).careerSaveDao())
        val career = CareerEngine.createNewCareerFromCustomPlayer(3, "Arda", "Kaya", 2006, "Türkiye", "Eskişehir", "Türkiye", "KAYA", 10,
            Position.AM, emptyList(), PreferredFoot.RIGHT, 180, 74, PlayerAppearance(), "Altyapı Akademisi Çıkışlı", "TR_BAL", "BAL_ESKISEHIR")
        repository.saveCareer(career, "test")
        lateinit var viewModel: FootballLifeViewModel
        compose.runOnUiThread {
            viewModel = ViewModelProvider(compose.activity, FootballLifeViewModel.Factory(context))[FootballLifeViewModel::class.java]
            viewModel.loadCareerSlot(3)
        }
        compose.waitUntil(10_000) { viewModel.activeCareer.value?.saveSlotId == 3 }
        compose.onNodeWithTag("player_hub_screen").assertIsDisplayed()
        compose.runOnUiThread { viewModel.runTrainingDrill(WorldDataCatalog.trainingDrills.first { it.id == "DRILL_PASSING" }) }
        compose.waitUntil(10_000) { viewModel.activeCareer.value?.trainedToday == true }
        val coachTask = viewModel.activeCareer.value!!.livingWorld.tasks.first { it.action == "COACH" }
        val trust = viewModel.activeCareer.value!!.managerTrust
        compose.runOnUiThread { viewModel.openWorldTask(coachTask.id) }
        compose.onNodeWithTag("cinematic_screen").assertIsDisplayed()
        compose.onNodeWithTag("scene_message_input").performTextInput("Hocam daha fazla süre istiyorum.")
        compose.onNodeWithTag("scene_send").performClick()
        compose.onNodeWithTag("scene_skip").performClick()
        compose.onNodeWithTag("scene_choice_work").performScrollTo().performClick()
        compose.onNodeWithTag("scene_continue").performScrollTo().performClick()
        compose.onNodeWithTag("player_hub_screen").assertIsDisplayed()
        assertEquals(trust + 2, viewModel.activeCareer.value!!.managerTrust)
        assertFalse(viewModel.activeCareer.value!!.livingWorld.tasks.first { it.id == coachTask.id }.isActive)
        // Loading through the view model waits for pending saves, then reads Room again.
        compose.runOnUiThread { viewModel.loadCareerSlot(3) }
        compose.waitUntil(10_000) {
            viewModel.activeCareer.value!!.livingWorld.sceneHistory.any { it.taskId == coachTask.id && it.selectedChoiceId == "work" }
        }
        val stored = repository.loadCareerSlot(3)!!
        assertEquals(trust + 2, stored.managerTrust)
        assertFalse(stored.livingWorld.tasks.first { it.id == coachTask.id }.isActive)
        assertTrue(stored.livingWorld.memories.any { it.sourceEventId == "DECISION:SCENE:${coachTask.id}" })
    }
}
