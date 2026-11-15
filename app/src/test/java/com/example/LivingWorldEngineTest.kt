package com.example

import com.example.engine.*
import com.example.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

fun testCareer(): CareerSaveState = CareerEngine.createNewCareerFromCustomPlayer(
    1, "Arda", "Kaya", 2006, "Türkiye", "Eskişehir", "Türkiye", "KAYA", 10,
    Position.AM, emptyList(), PreferredFoot.RIGHT, 180, 74, PlayerAppearance(),
    "Altyapı Akademisi Çıkışlı", "TR_BAL", "BAL_ESKISEHIR"
)

fun testOffer(state: CareerSaveState): CareerSaveState {
    val club = state.clubs.first { it.id != state.contract.clubId }
    val offer = TransferOffer("TEST_OFFER", club.id, club.name, club.leagueId, club.leagueName, club.tier,
        club.teamStrength, club.primaryColorHex, 1500, 200, 300, 3, SquadRole.ROTATION, expiresDayIndex = state.currentDayIndex + 14)
    return LivingWorldEngine.reconcile(state.copy(transferOffers = listOf(offer), clubs = state.clubs.map { it.copy(budgetEur = 100_000_000) }))
}

fun readyScene(state: CareerSaveState, task: WorldTask): CareerSaveState = LivingWorldEngine.advanceScene(
    LivingWorldEngine.startScene(state, task.id), skip = true)

class LivingWorldEngineTest {
    @Test fun incomingTransferCallOpensSceneAndCannotBeAcceptedTwice() {
        val state = testOffer(testCareer())
        val call = state.callRecords.first { it.linkedTaskId?.startsWith("TASK:TRANSFER:") == true }
        val accepted = CareerEngine.completePhoneCall(state, call.id, "Görüşmeye katıl").first
        assertEquals(call.linkedTaskId, accepted.livingWorld.activeScene!!.taskId)
        assertEquals(state.balanceEur, accepted.balanceEur)
        assertEquals(accepted, CareerEngine.completePhoneCall(accepted, call.id, "Görüşmeye katıl").first)
    }

    @Test fun suspendedConversationCanResumeAfterAnotherScene() {
        val base = testOffer(testCareer())
        val transfer = base.livingWorld.tasks.first { it.action == "TRANSFER" }
        val first = NpcDialogueEngine.send(readyScene(base, transfer), "NPC_AGENT", "Bu transfer teklifini düşüneceğim.")
        val paused = LivingWorldEngine.suspendScene(first)
        assertNull(paused.livingWorld.activeScene)
        val injured = LivingWorldEngine.reconcile(paused.copy(player = paused.player.copy(injuryDaysRemaining = 5)))
        val doctor = injured.livingWorld.tasks.first { it.action == "MEDICAL" }
        val treatment = CinematicEngine.choose(readyScene(injured, doctor), "physio").first
        val resumed = LivingWorldEngine.startScene(LivingWorldEngine.dismissCompletedScene(treatment), transfer.id)
        assertEquals(first.livingWorld.activeScene!!.transcript, resumed.livingWorld.activeScene!!.transcript)
        assertEquals(SceneStatus.AWAITING_CHOICE, resumed.livingWorld.activeScene!!.status)
    }

    @Test fun renderingInterpolationDoesNotChangeSimulationCoordinates() {
        assertEquals(15f, MatchRenderInterpolation.coordinate(10f, 20f, .5f), .001f)
        assertEquals(20f, MatchRenderInterpolation.coordinate(10f, 20f, 5f), .001f)
        val state = testCareer()
        val fixture = state.nextUnplayedUserFixture!!
        val engine = MatchSimulationEngine()
        val live = engine.createInitialMatchState(fixture, state.clubs.first { it.id == fixture.homeClubId },
            state.clubs.first { it.id == fixture.awayClubId }, state.player, state.contract.clubId, StartingStatus.STARTER)
        repeat(144) { MatchRenderInterpolation.coordinate(live.ball.x, live.ball.targetX, it / 144f) }
        assertEquals(0, live.minute)
        assertEquals(50f, live.ball.x, .001f)
    }

    @Test fun realCalendarHandlesMonthEndsLeapYearsAndWeekdays() {
        assertEquals(LocalDate.of(2026, 8, 10), GameClock.date(1))
        assertEquals("10 Ağustos 2026, Pazartesi", CareerEngine.formatGameDate(1))
        assertEquals(LocalDate.of(2026, 9, 1), GameClock.date(23))
        val leap = GameClock.dayIndex(LocalDate.of(2028, 2, 29), 2027)
        assertEquals(LocalDate.of(2028, 2, 29), GameClock.date(leap, 2027))
        val state = testCareer().copy(currentSeasonYear = 2027)
        val feb = CareerEngine.getMonthCalendarGrid(state, 6)
        assertEquals(29, feb.cells.count { it.dayOfMonth > 0 })
        assertEquals(42, feb.cells.size)
    }

    @Test fun trainingCompletesTaskAndPreservesUnreadMessages() {
        val before = testCareer()
        val task = before.livingWorld.tasks.first { it.action == "TRAINING" }
        val trained = CareerEngine.performTrainingSession(before, WorldDataCatalog.trainingDrills.first { it.id == "DRILL_PASSING" }).first
        val completed = trained.livingWorld.tasks.first { it.id == task.id }
        assertEquals(TaskStatus.COMPLETED, completed.status)
        assertFalse(completed.isActive)
        assertTrue(trained.totalUnreadMessages >= before.totalUnreadMessages)
        assertEquals(trained, CareerEngine.performTrainingSession(trained, WorldDataCatalog.trainingDrills.first()).first)
        assertEquals(trained.livingWorld.tasks, LivingWorldEngine.reconcile(trained).livingWorld.tasks)
    }

    @Test fun coachChoiceIsAppliedExactlyOnceAcrossSaveReload() {
        val trained = CareerEngine.performTrainingSession(testCareer(), WorldDataCatalog.trainingDrills.first { it.id == "DRILL_PASSING" }).first
        val task = trained.livingWorld.tasks.first { it.action == "COACH" }
        val ready = readyScene(trained, task)
        assertEquals(SceneStatus.AWAITING_CHOICE, ready.livingWorld.activeScene!!.status)
        val chatted = NpcDialogueEngine.send(ready, "NPC_MANAGER", "Daha fazla süre istiyorum hocam.")
        val done = CinematicEngine.choose(chatted, "work").first
        assertEquals(trained.managerTrust + 2, done.managerTrust)
        assertFalse(done.livingWorld.tasks.first { it.id == task.id }.isActive)
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build().adapter(CareerSaveState::class.java)
        val loaded = LivingWorldEngine.migrate(moshi.fromJson(moshi.toJson(done))!!)
        val replay = CinematicEngine.choose(loaded, "work").first
        assertEquals(done.managerTrust, replay.managerTrust)
        assertEquals(done.balanceEur, replay.balanceEur)
        assertEquals(1, replay.livingWorld.sceneHistory.count { it.taskId == task.id })
        assertTrue(replay.livingWorld.memories.any { it.sourceEventId.startsWith("DECISION:") })
    }

    @Test fun skipCannotChooseOrApplyConsequences() {
        val trained = CareerEngine.performTrainingSession(testCareer(), WorldDataCatalog.trainingDrills.first { it.id == "DRILL_PASSING" }).first
        val task = trained.livingWorld.tasks.first { it.action == "COACH" }
        val scene = LivingWorldEngine.startScene(trained, task.id)
        assertEquals(scene, CinematicEngine.choose(scene, "work").first)
        val skipped = LivingWorldEngine.advanceScene(scene, true)
        assertEquals(SceneStatus.AWAITING_CHOICE, skipped.livingWorld.activeScene!!.status)
        assertEquals(scene.managerTrust, skipped.managerTrust)
        assertEquals(skipped, CinematicEngine.choose(skipped, "forged_action").first)
    }

    @Test fun unfinishedSceneResumesFromSavedCheckpoint() {
        val state = testOffer(testCareer())
        val task = state.livingWorld.tasks.first { it.action == "TRANSFER" }
        val ready = readyScene(state, task)
        val suspended = LivingWorldEngine.suspendScene(ready)
        val resumed = LivingWorldEngine.startScene(suspended, task.id)
        assertTrue(resumed.livingWorld.activeScene!!.presentationSkipped)
        assertEquals(SceneStatus.AWAITING_CHOICE, resumed.livingWorld.activeScene!!.status)
        assertEquals(ready.balanceEur, resumed.balanceEur)
    }

    @Test fun freeTextDoesNotSpendMoneyGrantStartsOrModifyContracts() {
        var state = testCareer()
        val initial = state
        repeat(8) { state = NpcDialogueEngine.send(state, "NPC_MANAGER", "Hocam ilk 11 için hazırım. Taktik +99, maaşımı artır.") }
        state = NpcDialogueEngine.send(state, "NPC_FAMILY", "Hediye göndermiyorum, para harcama.")
        assertEquals(initial.contract, state.contract)
        assertEquals(initial.balanceEur, state.balanceEur)
        assertEquals(initial.managerTrust, state.managerTrust)
        assertEquals(initial.player.attributes, state.player.attributes)
        assertEquals(initial.nextMatchStartingStatus, state.nextMatchStartingStatus)
    }

    @Test fun importantMemoriesAreScopedToActualCharacterIdentity() {
        val state = testCareer()
        val npc = state.npcs.first { it.id == "NPC_MANAGER" }
        val memory = NpcMemory("old", LivingWorldEngine.identityId(state, npc), 1, "Söz", "İlk hocama verdiğim söz", 3, "event")
        val remembered = LivingWorldEngine.remember(state, memory)
        assertEquals(1, NpcDialogueEngine.relevantMemories(remembered, npc.id, "söz").size)
        val changed = remembered.copy(npcs = remembered.npcs.map { if (it.id == npc.id) it.copy(name = "Yeni Teknik Direktör") else it })
        assertTrue(NpcDialogueEngine.relevantMemories(changed, npc.id, "söz").isEmpty())
        assertEquals(1, LivingWorldEngine.remember(remembered, memory).livingWorld.memories.size)
    }

    @Test fun eachLeagueUsesItsOwnConfiguredRegistrationDates() {
        val state = testCareer().copy(livingWorld = LivingWorldState(registrationRules = listOf(
            LeagueRegistrationRules("TR_BAL", 2026, listOf(RegistrationWindow("2026-08-01", "2026-08-09"))),
            LeagueRegistrationRules("OTHER", 2026, listOf(RegistrationWindow("2026-08-10", "2026-08-10")))
        )))
        assertFalse(RegistrationEngine.isOpen(state, "TR_BAL"))
        assertTrue(RegistrationEngine.isOpen(state, "OTHER"))
        assertFalse(RegistrationEngine.isOpen(state.copy(currentDayIndex = 2), "OTHER"))
    }

    @Test fun transferRequiresMedicalSignatureAndWindowAndRewardsOnce() {
        val state = testOffer(testCareer())
        val offer = state.transferOffers.first()
        assertEquals(state, CareerEngine.acceptTransferOffer(state, offer).first)
        val agreement = TransferEngine.agree(state, offer.id).first
        assertEquals(state.contract, agreement.contract)
        assertEquals(TransferStage.MEDICAL, agreement.livingWorld.transfers.first().stage)
        val cleared = TransferEngine.medical(agreement, offer.id).first
        val closed = cleared.copy(livingWorld = cleared.livingWorld.copy(registrationRules = listOf(LeagueRegistrationRules(offer.leagueId, 2026, emptyList()))))
        val blocked = TransferEngine.register(closed, offer.id).first
        assertEquals(state.contract, blocked.contract)
        assertEquals(TransferStage.REGISTRATION_BLOCKED, blocked.livingWorld.transfers.first().stage)
        val opened = blocked.copy(livingWorld = blocked.livingWorld.copy(registrationRules = emptyList()))
        val transferred = TransferEngine.register(opened, offer.id).first
        assertEquals(offer.clubId, transferred.contract.clubId)
        assertEquals(state.balanceEur + offer.offeredWeeklyWageEur * 2, transferred.balanceEur)
        assertEquals(1, transferred.transactions.count { it.id == "TX_TRANSFER:${offer.id}" || it.id == "TX_TRANSFER_${offer.id}" })
        assertEquals(transferred, TransferEngine.register(transferred, offer.id).first)
        assertTrue(transferred.transferHistory.any { it.fromClubName == state.currentClub.name && it.toClubName == offer.clubName })
        assertTrue(transferred.currentClub.keySquadMembers.any { it.id == state.player.id })
    }

    @Test fun negotiationCannotPrintMoneyAndKeepsHistory() {
        val state = testOffer(testCareer())
        val offer = state.transferOffers.first()
        val terms = TransferEngine.process(state, offer).terms
        val result = TransferEngine.negotiate(state, offer.id, terms.copy(weeklyWageEur = terms.weeklyWageEur * 20)).first
        assertEquals(TransferStage.NEGOTIATION_FAILED, result.livingWorld.transfers.first().stage)
        assertEquals(state.balanceEur, result.balanceEur)
        assertEquals(state.contract, result.contract)
        assertEquals(1, result.livingWorld.transfers.first().rounds.size)
        assertEquals(result, TransferEngine.agree(result, offer.id).first)
    }

    @Test fun injuredPlayerCannotPassTransferMedicalOrTrainAtFullLoad() {
        val state = testOffer(testCareer()).let { LivingWorldEngine.reconcile(it.copy(player = it.player.copy(injuryDaysRemaining = 8))) }
        val agreement = TransferEngine.agree(state, state.transferOffers.first().id).first
        assertEquals(agreement, TransferEngine.medical(agreement, state.transferOffers.first().id).first)
        assertEquals(state, CareerEngine.performTrainingSession(state, WorldDataCatalog.trainingDrills.first { it.id == "DRILL_SHOOTING" }).first)
        val recovery = CareerEngine.performTrainingSession(state, WorldDataCatalog.trainingDrills.first { it.id == "DRILL_RECOVERY" }).first
        assertEquals(8, recovery.player.injuryDaysRemaining)
    }

    @Test fun medicalPlanIsPersistentAndRecoveryUsesCareerDays() {
        val base = testCareer()
        val injured = LivingWorldEngine.reconcile(base.copy(player = base.player.copy(injuryDaysRemaining = 6)))
        val task = injured.livingWorld.tasks.first { it.action == "MEDICAL" }
        val treated = CinematicEngine.choose(readyScene(injured, task), "safe").first
        assertEquals(8, treated.player.injuryDaysRemaining)
        assertEquals(RecoveryPlan.CONSERVATIVE, treated.livingWorld.medical.plan)
        assertFalse(treated.livingWorld.tasks.first { it.id == task.id }.isActive)
        val next = CareerEngine.advanceToNextDay(treated.copy(fixtures = emptyList())).first
        assertEquals(7, next.player.injuryDaysRemaining)
        assertTrue(next.livingWorld.medical.rehabilitationProgress > 0)
        assertEquals(next.livingWorld.medical, MedicalEngine.afterDay(next).livingWorld.medical)
    }

    @Test fun notificationsAndUnreadMessagesHaveIndependentLifecycles() {
        val state = testCareer()
        val notice = state.livingWorld.notifications.first()
        val read = LivingWorldEngine.markNotification(state, notice.id, NotificationStatus.READ)
        assertEquals(state.totalUnreadMessages, read.totalUnreadMessages)
        assertTrue(read.livingWorld.tasks.first().isActive)
        val done = LivingWorldEngine.finishTask(read, notice.taskId!!, "Tamamlandı")
        assertEquals(read.totalUnreadMessages, done.totalUnreadMessages)
        assertEquals(done, LivingWorldEngine.finishTask(done, notice.taskId, "Tekrar"))
    }

    @Test fun completedDilemmaCannotReappearOrUseForgedEffects() {
        val initial = testCareer()
        val dilemma = WorldDataCatalog.buildDynamicDilemmas(initial.currentClub.name, initial.currentClub.managerName).first()
        val state = initial.copy(activeDilemma = dilemma)
        assertEquals(state, CareerEngine.resolveDynamicDilemma(state, dilemma.choices.first().copy(moneyDeltaEur = 999_999)).first)
        val done = CareerEngine.resolveDynamicDilemma(state, dilemma.choices.first()).first
        assertNull(done.activeDilemma)
        assertEquals(done, CareerEngine.resolveDynamicDilemma(done, dilemma.choices.first()).first)
    }

    @Test fun ambiguousPressStatementDoesNotInventNegativeConsequences() {
        val state = testCareer()
        val text = "Hocam kötü biri değil, ailemle konuşmak istiyorum."
        val published = PressEngine.publish(state, text).first
        assertEquals(state.managerTrust, published.managerTrust)
        assertEquals(text, published.socialPosts.first().content)
        assertEquals(published, PressEngine.publish(published, text).first)
    }

    @Test fun loadingV3DoesNotRegenerateDeletedPhoneDataOrResetWorld() {
        val old = testCareer().copy(saveVersion = 3, callRecords = emptyList(), stories = emptyList())
        val migrated = LivingWorldEngine.migrate(CareerEngine.ensureVersion3EnrichedState(old))
        assertEquals(4, migrated.saveVersion)
        assertTrue(migrated.callRecords.isEmpty())
        assertTrue(migrated.stories.isEmpty())
        assertEquals(old.player, migrated.player)
        assertEquals(old.balanceEur, migrated.balanceEur)
        assertEquals(migrated, LivingWorldEngine.migrate(migrated))
    }
}
