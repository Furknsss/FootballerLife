package com.example

import com.example.engine.CareerEngine
import com.example.engine.MatchPhase
import com.example.engine.MatchSimulationEngine
import com.example.model.PlayerAppearance
import com.example.model.Position
import com.example.model.PreferredFoot
import com.example.model.StartingStatus
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * "Sürekli aynı pozisyon / aynı yerden gol" şikayetlerinin tekrar etmediğini kanıtlayan testler.
 */
class MatchDiversityTest {

    private fun newCareer() = CareerEngine.createNewCareerFromCustomPlayer(
        slotId = 1,
        firstName = "Test",
        lastName = "Oyuncu",
        birthYear = 2005,
        country = "Türkiye",
        city = "İstanbul",
        nationality = "Türkiye",
        kitName = "TEST",
        kitNumber = 9,
        primaryPosition = Position.ST,
        secondaryPositions = listOf(Position.CM),
        preferredFoot = PreferredFoot.RIGHT,
        heightCm = 180,
        weightKg = 75,
        appearance = PlayerAppearance(),
        backgroundStory = "Test",
        selectedLeagueId = "TR_3_LIG",
        selectedClubId = "T3_BURSA"
    )

    @Test
    fun scenariosVaryAcrossDifferentMatches() {
        val seen = mutableSetOf<String>()
        for (seed in 1L..14L) {
            val career = newCareer()
            val engine = MatchSimulationEngine(baseSeed = seed)
            val fixture = career.nextUnplayedUserFixture ?: continue
            val home = career.clubs.first { it.id == fixture.homeClubId }
            val away = career.clubs.first { it.id == fixture.awayClubId }
            var match = engine.createInitialMatchState(
                fixture = fixture,
                homeClub = home,
                awayClub = away,
                userPlayer = career.player,
                userClubId = career.contract.clubId,
                startingStatus = StartingStatus.STARTER
            )
            match = engine.startKickOff(match)
            var guard = 0
            while (match.phase != MatchPhase.CRITICAL_DECISION && guard < 400) {
                match = engine.stepAnimationFrame(match, career.player)
                guard++
            }
            match.activeDecision?.let { seen += it.scenarioType }
        }
        assertTrue("Farklı maçlarda en az 4 farklı senaryo bekleniyor, bulundu: $seen", seen.size >= 4)
    }

    @Test
    fun goalsAreNotAlwaysAtTheSameSpot() {
        val goalYs = mutableSetOf<Float>()
        for (seed in 1L..26L) {
            val career = newCareer()
            val engine = MatchSimulationEngine(baseSeed = seed)
            val fixture = career.nextUnplayedUserFixture ?: continue
            val home = career.clubs.first { it.id == fixture.homeClubId }
            val away = career.clubs.first { it.id == fixture.awayClubId }
            var match = engine.createInitialMatchState(
                fixture = fixture,
                homeClub = home,
                awayClub = away,
                userPlayer = career.player,
                userClubId = career.contract.clubId,
                startingStatus = StartingStatus.STARTER
            )
            match = engine.startKickOff(match)
            var guard = 0
            while (match.phase != MatchPhase.FULL_TIME && guard < 4000) {
                if (match.phase == MatchPhase.GOAL_CELEBRATION) {
                    goalYs += match.ball.targetY
                    match = match.copy(
                        phase = MatchPhase.LIVE_PLAY,
                        goalScorerBanner = null,
                        resolutionTicksRemaining = 0
                    )
                } else if (match.phase == MatchPhase.HALF_TIME) {
                    match = engine.resumeSecondHalf(match, career.player)
                } else if (match.phase == MatchPhase.CRITICAL_DECISION) {
                    val opt = match.activeDecision?.options?.firstOrNull()
                    match = if (opt != null) engine.resolveCriticalDecision(match, opt, career.player)
                    else match.copy(phase = MatchPhase.LIVE_PLAY)
                } else {
                    match = engine.stepAnimationFrame(match, career.player)
                }
                guard++
            }
        }
        assertTrue(
            "Goller farklı noktalara gitmeli (sabit Y=50 hatası geri gelmemeli). Bulunan: $goalYs",
            goalYs.size >= 3 && goalYs.any { (it - 50f) > 1.5f }
        )
    }
}
