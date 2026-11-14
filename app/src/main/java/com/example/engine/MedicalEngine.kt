package com.example.engine

import com.example.model.*
import kotlin.random.Random

/** Fictional gameplay model, not medical advice. Recovery is advanced only by the career clock. */
object MedicalEngine {
    fun trainingRisk(state: CareerSaveState, baseRisk: Int, energyCost: Int): Int {
        if (energyCost <= 0) return 0
        val m = state.livingWorld.medical
        return (baseRisk / 2 + m.fatigue / 15 + m.trainingLoad / 25 + m.matchLoad / 35 +
            (100 - state.player.energy) / 20 + state.injuryHistory.size.coerceAtMost(4) - state.player.attributes.stamina / 30).coerceIn(1, 25)
    }

    fun afterTraining(state: CareerSaveState, cost: Int): CareerSaveState {
        val m = state.livingWorld.medical
        return state.copy(livingWorld = state.livingWorld.copy(medical = m.copy(
            fatigue = (m.fatigue + cost / 2).coerceIn(0, 100), trainingLoad = (m.trainingLoad + cost.coerceAtLeast(0)).coerceAtMost(200)
        )))
    }

    fun afterMatch(before: CareerSaveState, after: CareerSaveState, report: CompletedMatchReport): CareerSaveState {
        val id = "MEDICAL_MATCH:${report.fixtureId}"
        if (id in after.livingWorld.appliedEventIds) return after
        val m = after.livingWorld.medical
        val contact = report.events.count { it.type in setOf("FAUL", "MÜDAHALE", "SAKATLIK") }
        val risk = (1 + m.fatigue / 12 + report.userPlayedMinutes / 30 + contact.coerceAtMost(5) + before.injuryHistory.size.coerceAtMost(4) - before.player.attributes.stamina / 35).coerceIn(1, 18)
        val rng = Random(report.fixtureId.hashCode().toLong() + before.player.id.hashCode())
        val injury = before.player.injuryDaysRemaining == 0 && report.userPlayedMinutes > 0 && rng.nextInt(100) < risk
        val days = if (injury) rng.nextInt(3, 15) else after.player.injuryDaysRemaining
        val injuryEvent = MatchEventRecord(report.userPlayedMinutes.coerceIn(1, 90), "SAKATLIK", before.contract.clubId,
            before.player.fullName, "Maç yükü / temas sonrasında kas zorlanması; kulüp doktoru değerlendirmesi gerekiyor.", true)
        val next = after.copy(player = after.player.copy(injuryDaysRemaining = days,
                currentSeasonStats = after.player.currentSeasonStats.copy(injuriesCount = after.player.currentSeasonStats.injuriesCount + if (injury) 1 else 0)),
            lastMatchReport = if (injury) after.lastMatchReport?.copy(events = after.lastMatchReport.events + injuryEvent) else after.lastMatchReport,
            nextMatchStartingStatus = if (injury) StartingStatus.OUT_OF_SQUAD else after.nextMatchStartingStatus,
            injuryHistory = if (injury) listOf(InjuryRecord(id, after.currentDayIndex, GameClock.full(after.currentDayIndex, after.currentSeasonYear), "Maç yükü / temas kaynaklı kas zorlanması", days, 0)) + after.injuryHistory else after.injuryHistory,
            eventHistory = if (injury) listOf(GameEvent(id, after.currentDayIndex, 0, "INJURY", injuryEvent.description, "$days gün iyileşme")) + after.eventHistory else after.eventHistory,
            livingWorld = after.livingWorld.copy(appliedEventIds = after.livingWorld.appliedEventIds + id,
                medical = m.copy(matchLoad = (m.matchLoad + report.userPlayedMinutes).coerceAtMost(240), fatigue = (m.fatigue + report.energySpent / 2).coerceIn(0, 100))))
        return LivingWorldEngine.reconcile(next)
    }

    fun afterDay(state: CareerSaveState): CareerSaveState {
        val m = state.livingWorld.medical
        if (m.lastRecoveryDay >= state.currentDayIndex) return state
        val remaining = state.player.injuryDaysRemaining
        val progress = if (m.initialRecoveryDays == 0) 100 else ((m.initialRecoveryDays - remaining) * 100 / m.initialRecoveryDays).coerceIn(0, 100)
        return state.copy(livingWorld = state.livingWorld.copy(medical = m.copy(
            fatigue = (m.fatigue - 18).coerceAtLeast(0), trainingLoad = (m.trainingLoad - 20).coerceAtLeast(0),
            matchLoad = (m.matchLoad - 25).coerceAtLeast(0), lastRecoveryDay = state.currentDayIndex,
            rehabilitationProgress = progress, clearedForFullTraining = remaining == 0
        )))
    }
}
