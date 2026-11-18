package com.example.engine.progression

import com.example.engine.CareerEngine
import com.example.engine.data.SuperLigData
import com.example.engine.data.TFF1LigData
import com.example.model.Club
import com.example.model.Contract
import com.example.model.LeagueFixture
import com.example.model.LeagueScorerEntry
import com.example.model.PlayerAppearance
import com.example.model.PlayerAttributes
import com.example.model.PlayerProfile
import com.example.model.Position
import com.example.model.PreferredFoot
import com.example.model.SeasonStatsRecord
import com.example.model.SquadRole
import com.example.model.StandingRow
import com.example.model.StartingStatus
import com.example.model.CareerSaveState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SeasonTransitionEngineTest {

    private fun createDummyClub(id: String, name: String, leagueId: String = SuperLigData.LEAGUE_ID, tier: Int = 1): Club {
        return Club(
            id = id,
            name = name,
            shortName = name.take(3).uppercase(),
            country = "Türkiye",
            city = "İstanbul",
            leagueId = leagueId,
            leagueName = if (tier == 1) SuperLigData.LEAGUE_NAME else TFF1LigData.LEAGUE_NAME,
            tier = tier,
            primaryColorHex = "#A93226",
            secondaryColorHex = "#F39C12",
            budgetEur = 10_000_000L,
            facilityQuality = 4,
            teamStrength = 75,
            managerName = "Teknik Direktör",
            managerTacticalStyle = "4-3-3",
            stadiumName = "Stadyum",
            stadiumCapacity = 30000
        )
    }

    private fun createMockState(
        userClubId: String = "C_USER",
        leagueId: String = SuperLigData.LEAGUE_ID,
        tier: Int = 1,
        playerAge: Int = 19,
        playerGoals: Int = 24,
        playerAssists: Int = 10,
        userClubPoints: Int = 85
    ): CareerSaveState {
        val clubs = (1..8).map { i ->
            val id = if (i == 1) userClubId else "C_$i"
            val name = if (i == 1) "Şampiyon Kulüp" else "Kulüp $i"
            createDummyClub(id, name, leagueId, tier)
        }

        val player = PlayerProfile(
            id = "P_USER",
            firstName = "Arda",
            lastName = "Kaya",
            birthDate = "2007-04-12",
            age = playerAge,
            country = "Türkiye",
            city = "İstanbul",
            nationality = "Türkiye",
            kitName = "KAYA",
            kitNumber = 10,
            primaryPosition = Position.AM,
            secondaryPositions = listOf(Position.WG),
            preferredFoot = PreferredFoot.RIGHT,
            heightCm = 178,
            weightKg = 72,
            appearance = PlayerAppearance(),
            attributes = PlayerAttributes(),
            potential = 88,
            currentSeasonStats = SeasonStatsRecord(
                seasonYear = 2026,
                clubId = userClubId,
                clubName = clubs.first().name,
                leagueName = clubs.first().leagueName,
                appearances = 28,
                starts = 26,
                minutesPlayed = 2400,
                goals = playerGoals,
                assists = playerAssists,
                totalRatingSum = 28 * 7.7
            )
        )

        val contract = Contract(
            clubId = userClubId,
            clubName = clubs.first().name,
            weeklyWageEur = 15_000L,
            matchAppearanceBonusEur = 2_000L,
            goalBonusEur = 3_000L,
            startSeasonYear = 2026,
            endSeasonYear = 2029,
            role = SquadRole.STAR
        )

        val standings = clubs.mapIndexed { idx, c ->
            val pts = if (c.id == userClubId) userClubPoints else (70 - idx * 6)
            StandingRow(
                clubId = c.id,
                clubName = c.name,
                shortName = c.shortName,
                primaryColorHex = c.primaryColorHex,
                played = 14,
                won = pts / 3,
                drawn = 1,
                lost = 14 - (pts / 3) - 1,
                goalsFor = 30 + pts / 2,
                goalsAgainst = 15,
                points = pts
            )
        }

        val fixtures = CareerEngine.generateLeagueSchedule(leagueId, clubs).map {
            it.copy(played = true, homeScore = 2, awayScore = 1)
        }

        val topScorers = listOf(
            LeagueScorerEntry(
                playerId = "P_USER",
                playerName = "Arda Kaya",
                clubId = userClubId,
                clubName = clubs.first().name,
                goals = playerGoals,
                assists = playerAssists,
                matches = 28,
                averageRating = 7.7,
                isUserPlayer = true
            ),
            LeagueScorerEntry(
                playerId = "P_AI_1",
                playerName = "Victor Osimhen",
                clubId = "C_2",
                clubName = "Kulüp 2",
                goals = 20,
                assists = 5,
                matches = 28,
                averageRating = 7.6,
                isUserPlayer = false
            )
        )

        val calendar = CareerEngine.buildCalendarFromFixtures(fixtures, userClubId, 2026)

        return CareerSaveState(
            saveSlotId = 1,
            careerTitle = "Kariyer",
            createdAtMs = 1000L,
            updatedAtMs = 1000L,
            currentDayIndex = fixtures.maxOf { it.dayIndex },
            currentSeasonYear = 2026,
            currentLeagueId = leagueId,
            player = player,
            contract = contract,
            balanceEur = 150_000L,
            clubs = clubs,
            standings = standings,
            fixtures = fixtures,
            topScorers = topScorers,
            npcs = emptyList(),
            calendarEvents = calendar,
            transactions = emptyList(),
            newsEvents = emptyList(),
            socialPosts = emptyList()
        )
    }

    @Test
    fun testCalendarEventsSpanFullSeasonDynamically() {
        val clubs = (1..6).map { createDummyClub("C_$it", "Kulüp $it") }
        val fixtures = CareerEngine.generateLeagueSchedule(SuperLigData.LEAGUE_ID, clubs)

        // Fixtures should span multiple weeks
        assertTrue(fixtures.isNotEmpty())
        val maxFixtureDay = fixtures.maxOf { it.dayIndex }

        val events = CareerEngine.buildCalendarFromFixtures(fixtures, "C_1", 2026)
        // Should far exceed the old 45-day hardcoded cap
        assertTrue("Events count should be at least 300 days", events.size >= 300)
        assertTrue(events.maxOf { it.dayIndex } >= maxFixtureDay)

        // Verify match days, tactics, recovery and rest
        val matchEvents = events.filter { it.eventType == "MAÇ" }
        assertTrue("Match events should exist", matchEvents.isNotEmpty())
        val trainEvents = events.filter { it.eventType == "ANTRENMAN" }
        assertTrue("Training events should exist", trainEvents.isNotEmpty())

        val state = createMockState()
        // Month calendar grid check for 10 months (offset 0 to 9)
        for (m in 0..9) {
            val grid = CareerEngine.getMonthCalendarGrid(state, m)
            assertEquals(42, grid.cells.size)
            assertNotNull(grid.monthTitleTr)
        }
    }

    @Test
    fun testSeasonEndEvaluationAwardsAndChampions() {
        val state = createMockState(
            playerAge = 19,
            playerGoals = 25,
            playerAssists = 12,
            userClubPoints = 90
        )

        assertTrue(SeasonTransitionEngine.isSeasonFinished(state))

        val summary = SeasonTransitionEngine.evaluateSeasonEnd(state)
        assertTrue("User club should be champion", summary.isUserClubChampion)
        assertEquals(1, summary.userLeagueRank)
        assertTrue("User should win Golden Boot", summary.userWonGoldenBoot)
        assertEquals("Arda Kaya", summary.goldenBootWinner.playerName)
        assertTrue("User should win Young Player award", summary.userWonYoungPlayer)
        assertTrue("User should win Ballon d'Or", summary.userWonBallonDor)
        assertEquals(3, summary.ballonDorTop3.size)
        assertEquals(1, summary.ballonDorTop3.first().rank)
        assertTrue(summary.playerTrophiesWon.isNotEmpty())
        assertTrue(summary.playerAwardsWon.contains("Ballon d'Or (Yılın En İyi Futbolcusu)"))
    }

    @Test
    fun testPromotionAndRelegationHierarchy() {
        val tff1State = createMockState(
            leagueId = TFF1LigData.LEAGUE_ID,
            tier = 2,
            userClubPoints = 88
        )
        val summary = SeasonTransitionEngine.evaluateSeasonEnd(tff1State)
        assertTrue("User club in 1st place should be promoted", summary.promotedClubs.any { it.id == "C_USER" })
        assertTrue("Relegated clubs should not be empty", summary.relegatedClubs.isNotEmpty())
        assertTrue("Promotion hierarchy should be populated", summary.promotionRelegationHierarchy.isNotEmpty())
    }

    @Test
    fun testSeasonTransitionAndAgeIncrease() {
        val state = createMockState(
            playerAge = 19,
            userClubPoints = 90
        )
        val summary = SeasonTransitionEngine.evaluateSeasonEnd(state)

        val (nextState, message) = SeasonTransitionEngine.finalizeSeasonAndTransition(state, summary)

        // Age increases by 1
        assertEquals(20, nextState.player.age)
        // Season year advances by 1
        assertEquals(2027, nextState.currentSeasonYear)
        // Day index resets to 1
        assertEquals(1, nextState.currentDayIndex)
        // Previous season is archived
        assertEquals(1, nextState.player.careerArchive.size)
        val archive = nextState.player.careerArchive.first()
        assertEquals(2026, archive.seasonYear)
        assertEquals(24, archive.goals)
        assertTrue(archive.trophiesWon.isNotEmpty())

        // Current season stats reset to 0
        assertEquals(0, nextState.player.currentSeasonStats.goals)
        assertEquals(0, nextState.player.currentSeasonStats.appearances)
        assertEquals(2027, nextState.player.currentSeasonStats.seasonYear)

        // New season fixtures and calendar generated
        assertTrue(nextState.fixtures.isNotEmpty())
        assertTrue(nextState.calendarEvents.size >= 300)
        assertTrue(nextState.standings.all { it.played == 0 && it.points == 0 })
        assertTrue(message.contains("2027/2028"))
    }
}
