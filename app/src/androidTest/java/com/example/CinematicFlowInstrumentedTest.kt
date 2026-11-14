package com.example

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.engine.*
import com.example.model.*
import com.example.ui.screens.CinematicSceneScreen
import com.example.ui.theme.FootballLifeTheme
import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CinematicFlowInstrumentedTest {
    @get:Rule val compose = createComposeRule()
    private var state by mutableStateOf<CareerSaveState?>(null)

    private fun open(type: SceneType, action: String = type.name): CareerSaveState {
        val initial = CareerEngine.createNewCareerFromCustomPlayer(1, "Arda", "Kaya", 2006,
            "Türkiye", "Eskişehir", "Türkiye", "KAYA", 10, Position.AM, emptyList(), PreferredFoot.RIGHT,
            180, 74, PlayerAppearance(), "Altyapı Akademisi Çıkışlı", "TR_BAL", "BAL_ESKISEHIR")
        val npc = when (type) { SceneType.MEDICAL -> "NPC_DOCTOR"; SceneType.TRANSFER -> "NPC_AGENT"; SceneType.PRESS -> "NPC_PRESS"; SceneType.SOCIAL -> "NPC_FAMILY"; else -> "NPC_MANAGER" }
        val target = initial.clubs.first { it.id != initial.contract.clubId }
        val offer = TransferOffer("QA_OFFER", target.id, target.name, target.leagueId, target.leagueName, target.tier,
            target.teamStrength, target.primaryColorHex, 1500, 200, 300, 3, SquadRole.ROTATION, expiresDayIndex = 15)
        val task = WorldTask("QA:$type", "QA_SOURCE:$type", type.title, 1, action, sceneType = type, npcId = npc,
            entityId = if (type == SceneType.TRANSFER) offer.id else null)
        val prepared = when (type) {
            SceneType.MEDICAL -> initial.copy(player = initial.player.copy(injuryDaysRemaining = 5))
            SceneType.PRESS -> initial.copy(pressCompletedForLastMatch = false)
            SceneType.TRANSFER -> initial.copy(transferOffers = listOf(offer), clubs = initial.clubs.map { it.copy(budgetEur = 100_000_000) })
            else -> initial
        }
        val world = LivingWorldEngine.reconcile(prepared)
        val withTask = world.copy(livingWorld = world.livingWorld.copy(tasks = world.livingWorld.tasks + task))
        state = LivingWorldEngine.startScene(withTask, task.id)
        compose.setContent {
            FootballLifeTheme {
                val current = state!!
                val scene = current.livingWorld.activeScene!!
                CinematicSceneScreen(current, scene,
                    onAdvance = { state = LivingWorldEngine.advanceScene(state!!) },
                    onSkip = { state = LivingWorldEngine.advanceScene(state!!, true) },
                    onSuspend = { state = LivingWorldEngine.suspendScene(state!!) },
                    onMessage = { state = NpcDialogueEngine.send(state!!, npc, it) },
                    onChoose = { state = CinematicEngine.choose(state!!, it).first },
                    onContinue = {}, onNegotiate = { state = TransferEngine.negotiate(state!!, offer.id, it).first })
            }
        }
        compose.onNodeWithTag("cinematic_screen").assertIsDisplayed()
        return state!!
    }

    private fun screenshot(name: String) {
        compose.mainClock.advanceTimeBy(1000)
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val image = compose.onNodeWithTag("cinematic_screen").captureToImage().asAndroidBitmap()
        val dir = File(instrumentation.targetContext.getExternalFilesDir(null), "qa")
        dir.mkdirs()
        File(dir, "$name.png").outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
        image.recycle()
    }

    @Test fun coachSceneSupportsFreeTextSkipAndSingleDecision() {
        val before = open(SceneType.COACH)
        screenshot("coach_scene")
        compose.onNodeWithTag("scene_message_input").performTextInput("Hocam daha fazla süre istiyorum.")
        compose.onNodeWithTag("scene_send").performClick()
        compose.onNodeWithTag("scene_skip").performClick()
        compose.onNodeWithTag("scene_choice_work").performScrollTo().performClick()
        compose.onNodeWithTag("scene_continue").performScrollTo().assertIsDisplayed()
        compose.runOnIdle {
            assertEquals(before.managerTrust + 2, state!!.managerTrust)
            assertEquals(SceneStatus.COMPLETED, state!!.livingWorld.activeScene!!.status)
            assertFalse(state!!.livingWorld.tasks.first { it.id == "QA:COACH" }.isActive)
        }
    }

    @Test fun medicalSceneAppliesRecoveryPlan() {
        open(SceneType.MEDICAL, "MEDICAL")
        screenshot("medical_scene")
        compose.onNodeWithTag("scene_skip").performClick()
        compose.onNodeWithTag("scene_choice_safe").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(RecoveryPlan.CONSERVATIVE, state!!.livingWorld.medical.plan)
            assertEquals(7, state!!.player.injuryDaysRemaining)
        }
    }

    @Test fun transferSceneNegotiatesAndOpensMedicalWithoutMovingClub() {
        val before = open(SceneType.TRANSFER, "TRANSFER")
        screenshot("transfer_scene")
        compose.onNodeWithTag("scene_skip").performClick()
        compose.onNodeWithTag("negotiation_submit").performScrollTo().performClick()
        compose.onNodeWithTag("scene_dialogue").performScrollToNode(hasTestTag("scene_choice_agree"))
        compose.onNodeWithTag("scene_choice_agree").performClick()
        compose.runOnIdle {
            assertEquals(before.contract, state!!.contract)
            assertEquals(TransferStage.MEDICAL, state!!.livingWorld.transfers.first().stage)
            assertEquals(1, state!!.livingWorld.transfers.first().rounds.size)
            assertTrue(state!!.livingWorld.tasks.any { it.action == "TRANSFER_MEDICAL" && it.isActive })
        }
    }

    @Test fun pressScenePublishesPlayersActualWords() {
        open(SceneType.PRESS, "PRESS")
        screenshot("press_scene")
        val quote = "Takım arkadaşlarıma teşekkür ediyorum."
        compose.onNodeWithTag("scene_message_input").performTextInput(quote)
        compose.onNodeWithTag("scene_send").performClick()
        compose.onNodeWithTag("scene_skip").performClick()
        compose.onNodeWithTag("scene_choice_publish").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(quote, state!!.socialPosts.first().content) }
    }

    @Test fun socialScenePaysOnceAndClosesTask() {
        val before = open(SceneType.SOCIAL)
        screenshot("social_scene")
        compose.onNodeWithTag("scene_skip").performClick()
        compose.onNodeWithTag("scene_choice_dinner").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(before.balanceEur - 80, state!!.balanceEur)
            assertEquals(before.currentHour + 3, state!!.currentHour)
            assertEquals(1, state!!.transactions.count { it.description == "Aileyle akşam yemeği" })
        }
    }

    @Test fun prematchPresentationPreservesSelectedSquadRole() {
        val before = open(SceneType.PRE_MATCH, "MATCH")
        screenshot("prematch_scene")
        compose.onNodeWithTag("scene_skip").performClick()
        compose.onNodeWithTag("scene_choice_ready").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(before.nextMatchStartingStatus, state!!.nextMatchStartingStatus) }
    }
}
