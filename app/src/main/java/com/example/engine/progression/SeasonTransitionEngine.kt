package com.example.engine.progression

import com.example.engine.CareerEngine
import com.example.engine.WorldDataCatalog
import com.example.engine.data.AmateurLeaguesData
import com.example.engine.data.SuperLigData
import com.example.engine.data.TFF1LigData
import com.example.engine.data.TFF2LigData
import com.example.engine.data.TFF3LigData
import com.example.model.CareerAchievement
import com.example.model.CareerSaveState
import com.example.model.ChatMessage
import com.example.model.Club
import com.example.model.LeagueScorerEntry
import com.example.model.NewsEvent
import com.example.model.SeasonStatsRecord
import com.example.model.StandingRow
import kotlin.math.roundToInt

object SeasonTransitionEngine {

    /**
     * Checks if all league fixtures for the current season have been played.
     */
    fun isSeasonFinished(state: CareerSaveState): Boolean {
        if (state.fixtures.isEmpty()) return false
        val userClubId = state.contract.clubId
        val userFixtures = state.fixtures.filter { it.homeClubId == userClubId || it.awayClubId == userClubId }
        val allUserMatchesPlayed = userFixtures.isNotEmpty() && userFixtures.all { it.played }
        val maxFixtureDay = state.fixtures.maxOfOrNull { it.dayIndex } ?: 270
        val isSeasonTimelineEnded = state.currentDayIndex >= maxFixtureDay
        return allUserMatchesPlayed || isSeasonTimelineEnded
    }

    /**
     * Evaluates season standings, champions, promotion/relegation, individual awards (Golden Boot, Young Player, Ballon d'Or).
     */
    fun evaluateSeasonEnd(state: CareerSaveState): SeasonEndSummary {
        val sortedStandings = state.standings.sortedWith(
            compareByDescending<StandingRow> { it.points }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor }
        )

        val championStanding = sortedStandings.firstOrNull() ?: state.standings.first()
        val championClub = state.clubs.find { it.id == championStanding.clubId } ?: state.currentClub
        val isUserClubChampion = championClub.id == state.contract.clubId
        val userRank = sortedStandings.indexOfFirst { it.clubId == state.contract.clubId } + 1
        val totalTeams = state.clubs.size

        // Promotion & Relegation
        val (promotedClubs, relegatedClubs, hierarchyList) = calculatePromotionAndRelegation(
            state = state,
            sortedStandings = sortedStandings
        )

        // Golden Boot (Gol Krallığı)
        val userStats = state.player.currentSeasonStats
        val leagueTopScorer = state.topScorers.filter { !it.isUserPlayer }.maxByOrNull { it.goals }
        val userGoals = userStats.goals
        val topAiGoals = leagueTopScorer?.goals ?: 0
        val userWonGoldenBoot = userGoals > 0 && userGoals >= topAiGoals

        val goldenBootWinner = if (userWonGoldenBoot) {
            AwardRecipient(
                awardTitle = "Süper Lig / Lig Altın Ayakkabı",
                playerName = state.player.fullName,
                clubName = state.currentClub.name,
                statSummary = "$userGoals Gol (${userStats.appearances} Maç)",
                isUser = true,
                trophyIconName = "GOLDEN_BOOT"
            )
        } else {
            AwardRecipient(
                awardTitle = "Süper Lig / Lig Altın Ayakkabı",
                playerName = leagueTopScorer?.playerName ?: "Victor Osimhen",
                clubName = leagueTopScorer?.clubName ?: "Galatasaray",
                statSummary = "$topAiGoals Gol",
                isUser = false,
                trophyIconName = "GOLDEN_BOOT"
            )
        }

        // Assist King
        val leagueTopAssists = state.topScorers.filter { !it.isUserPlayer }.maxByOrNull { it.assists }
        val userAssists = userStats.assists
        val topAiAssists = leagueTopAssists?.assists ?: 0
        val userWonAssistKing = userAssists > 0 && userAssists >= topAiAssists

        val assistKingWinner = if (userWonAssistKing) {
            AwardRecipient(
                awardTitle = "Lig Asist Kralı",
                playerName = state.player.fullName,
                clubName = state.currentClub.name,
                statSummary = "$userAssists Asist",
                isUser = true,
                trophyIconName = "ASSIST_CROWN"
            )
        } else {
            AwardRecipient(
                awardTitle = "Lig Asist Kralı",
                playerName = leagueTopAssists?.playerName ?: "Gabriel Sara",
                clubName = leagueTopAssists?.clubName ?: "Galatasaray",
                statSummary = "$topAiAssists Asist",
                isUser = false,
                trophyIconName = "ASSIST_CROWN"
            )
        }

        // Young Player of the Year (Yılın Genç Futbolcusu)
        val isYoung = state.player.age <= 21
        val userWonYoungPlayer = isYoung && userStats.appearances >= 6 &&
            (userStats.goals + userStats.assists >= 6 || userStats.averageRating >= 6.85)

        val youngPlayerWinner = if (userWonYoungPlayer) {
            AwardRecipient(
                awardTitle = "Yılın Genç Futbolcusu (Kopa Trophy)",
                playerName = state.player.fullName,
                clubName = state.currentClub.name,
                statSummary = "${userStats.goals} Gol, ${userStats.assists} Asist • Ort. ${userStats.averageRating}",
                isUser = true,
                trophyIconName = "KOPA_TROPHY"
            )
        } else {
            AwardRecipient(
                awardTitle = "Yılın Genç Futbolcusu (Kopa Trophy)",
                playerName = if (state.currentLeagueId == SuperLigData.LEAGUE_ID) "Semih Kılıçsoy" else "Genç Yetenek",
                clubName = if (state.currentLeagueId == SuperLigData.LEAGUE_ID) "Beşiktaş" else "Lig Akademisi",
                statSummary = "12 Gol, 5 Asist • 19 Yaş",
                isUser = false,
                trophyIconName = "KOPA_TROPHY"
            )
        }

        // Ballon d'Or / Süper Lig Yılın Futbolcusu Galası
        val (ballonDorWinner, ballonDorCandidates, userWonBallonDor) = evaluateBallonDor(
            state = state,
            isUserClubChampion = isUserClubChampion,
            userWonGoldenBoot = userWonGoldenBoot
        )

        // Collected Player Trophies and Individual Awards
        val playerTrophiesWon = mutableListOf<String>()
        val playerAwardsWon = mutableListOf<String>()

        if (isUserClubChampion) {
            playerTrophiesWon.add("${state.currentClub.leagueName} Şampiyonluk Kupası")
        }
        if (userWonGoldenBoot) {
            playerAwardsWon.add("Altın Ayakkabı (Gol Krallığı)")
        }
        if (userWonAssistKing) {
            playerAwardsWon.add("Asist Krallığı")
        }
        if (userWonYoungPlayer) {
            playerAwardsWon.add("Yılın Genç Futbolcusu")
        }
        if (userWonBallonDor) {
            playerAwardsWon.add("Ballon d'Or (Yılın En İyi Futbolcusu)")
        }

        val pressHeadline = when {
            userWonBallonDor -> "DÜNYA ONU KONUŞUYOR! Ballon d'Or ${state.player.fullName}'ın Ellerinde Yükseldi! 🌟"
            isUserClubChampion -> "${championClub.name.uppercase()} ŞAMPİYON! ${state.player.fullName} ve Takım Arkadaşları Kupayı Kaldırdı! 🏆"
            userWonGoldenBoot -> "GOL MAKİNESİ! ${state.player.fullName} ${userStats.goals} Golle Altın Ayakkabı'yı Kazandı! ⚽"
            userRank <= 3 -> "${state.currentClub.name} Sezonu Zirve Yarışında Tamamladı! ${state.player.fullName}'dan Büyük Katkı."
            else -> "2026/27 Futbol Sezonu Tamamlandı! Büyük Ödüller ve Şampiyonlar Belli Oldu."
        }

        val managerQuote = when {
            isUserClubChampion -> "Tarih yazdık evlat! Sezon başından beri verdiğimiz tüm emeklerin karşılığı bu kupa. Seninle gurur duyuyorum!"
            userRank <= 4 -> "Harika bir mücadele sergiledik. Gelecek sezon bu ligin mutlak şampiyonu biz olacağız."
            else -> "Zorlu bir sezonu geride bıraktık. Gösterdiğin karakter ve gelişim gelecek sezon için en büyük teminatımız."
        }

        val fanMessage = if (isUserClubChampion) {
            "ŞAMPİYONLUK BİZİM! Sokaklar ${championClub.name} bayraklarıyla donatıldı! Teşekkürler ${state.player.fullName}! 🔴🟡⚪"
        } else {
            "Sezon boyunca formanın hakkını verdin. Gelecek sezon daha güçlü döneceğiz!"
        }

        return SeasonEndSummary(
            seasonYear = state.currentSeasonYear,
            leagueId = state.currentLeagueId,
            leagueName = state.currentClub.leagueName,
            championClub = championClub,
            isUserClubChampion = isUserClubChampion,
            userClubStanding = sortedStandings.find { it.clubId == state.contract.clubId } ?: championStanding,
            userLeagueRank = userRank,
            totalTeamsInLeague = totalTeams,
            promotedClubs = promotedClubs,
            relegatedClubs = relegatedClubs,
            promotionRelegationHierarchy = hierarchyList,
            goldenBootWinner = goldenBootWinner,
            assistKingWinner = assistKingWinner,
            youngPlayerOfYear = youngPlayerWinner,
            ballonDorWinner = ballonDorWinner,
            ballonDorTop3 = ballonDorCandidates,
            userWonBallonDor = userWonBallonDor,
            userWonGoldenBoot = userWonGoldenBoot,
            userWonYoungPlayer = userWonYoungPlayer,
            playerTrophiesWon = playerTrophiesWon,
            playerAwardsWon = playerAwardsWon,
            userSeasonStats = userStats,
            pressCelebrationHeadline = pressHeadline,
            managerCelebrationQuote = managerQuote,
            fanCelebrationMessage = fanMessage
        )
    }

    private fun calculatePromotionAndRelegation(
        state: CareerSaveState,
        sortedStandings: List<StandingRow>
    ): Triple<List<Club>, List<Club>, List<LeaguePromotionRelegationSummary>> {
        val userLeagueId = state.currentLeagueId
        val userClubId = state.contract.clubId
        val userRank = sortedStandings.indexOfFirst { it.clubId == userClubId } + 1
        val total = sortedStandings.size

        val hierarchyList = mutableListOf<LeaguePromotionRelegationSummary>()
        var promoted = emptyList<Club>()
        var relegated = emptyList<Club>()

        when (userLeagueId) {
            SuperLigData.LEAGUE_ID -> {
                // Süper Lig: Bottom 3 relegated to 1. Lig. Top 3 from 1. Lig promoted to Süper Lig.
                val bottom3Standings = sortedStandings.takeLast(3)
                relegated = state.clubs.filter { c -> bottom3Standings.any { it.clubId == c.id } }

                val tff1Clubs = TFF1LigData.getClubs()
                promoted = tff1Clubs.take(3)

                val userStatus = when {
                    userRank == 1 -> "CHAMPION_SUPER_LIG"
                    userRank > total - 3 -> "RELEGATED"
                    else -> "STAYED"
                }

                hierarchyList.add(
                    LeaguePromotionRelegationSummary(
                        leagueId = SuperLigData.LEAGUE_ID,
                        leagueName = SuperLigData.LEAGUE_NAME,
                        tier = 1,
                        promotedToHigherTier = emptyList(),
                        relegatedToLowerTier = relegated,
                        userClubStatus = userStatus
                    )
                )
                hierarchyList.add(
                    LeaguePromotionRelegationSummary(
                        leagueId = TFF1LigData.LEAGUE_ID,
                        leagueName = TFF1LigData.LEAGUE_NAME,
                        tier = 2,
                        promotedToHigherTier = promoted,
                        relegatedToLowerTier = emptyList(),
                        userClubStatus = "LOWER_TIER"
                    )
                )
            }
            TFF1LigData.LEAGUE_ID -> {
                // TFF 1. Lig: Top 3 promoted to Süper Lig. Bottom 3 relegated to 2. Lig.
                val top3Standings = sortedStandings.take(3)
                promoted = state.clubs.filter { c -> top3Standings.any { it.clubId == c.id } }

                val bottom3Standings = sortedStandings.takeLast(3)
                relegated = state.clubs.filter { c -> bottom3Standings.any { it.clubId == c.id } }

                val userStatus = when {
                    userRank <= 3 -> "CHAMPION_PROMOTED"
                    userRank > total - 3 -> "RELEGATED"
                    else -> "STAYED"
                }

                hierarchyList.add(
                    LeaguePromotionRelegationSummary(
                        leagueId = TFF1LigData.LEAGUE_ID,
                        leagueName = TFF1LigData.LEAGUE_NAME,
                        tier = 2,
                        promotedToHigherTier = promoted,
                        relegatedToLowerTier = relegated,
                        userClubStatus = userStatus
                    )
                )
            }
            AmateurLeaguesData.LEAGUE_ID_BAL -> {
                // BAL: Champions (e.g. Eskişehirspor) promoted to TFF 3. Lig!
                val topBalStandings = sortedStandings.take(2)
                promoted = state.clubs.filter { c -> topBalStandings.any { it.clubId == c.id } }
                relegated = emptyList()

                val userStatus = if (userRank <= 2) "CHAMPION_PROMOTED" else "STAYED"
                hierarchyList.add(
                    LeaguePromotionRelegationSummary(
                        leagueId = AmateurLeaguesData.LEAGUE_ID_BAL,
                        leagueName = AmateurLeaguesData.LEAGUE_NAME_BAL,
                        tier = 5,
                        promotedToHigherTier = promoted,
                        relegatedToLowerTier = emptyList(),
                        userClubStatus = userStatus
                    )
                )
            }
            else -> {
                // Generic lower tier: Top 2 promoted, bottom 2 relegated
                val topStandings = sortedStandings.take(2)
                promoted = state.clubs.filter { c -> topStandings.any { it.clubId == c.id } }
                val bottomStandings = sortedStandings.takeLast(2)
                relegated = state.clubs.filter { c -> bottomStandings.any { it.clubId == c.id } }

                val userStatus = when {
                    userRank <= 2 -> "CHAMPION_PROMOTED"
                    userRank > total - 2 -> "RELEGATED"
                    else -> "STAYED"
                }
                hierarchyList.add(
                    LeaguePromotionRelegationSummary(
                        leagueId = userLeagueId,
                        leagueName = state.currentClub.leagueName,
                        tier = state.currentClub.tier,
                        promotedToHigherTier = promoted,
                        relegatedToLowerTier = relegated,
                        userClubStatus = userStatus
                    )
                )
            }
        }

        return Triple(promoted, relegated, hierarchyList)
    }

    private fun evaluateBallonDor(
        state: CareerSaveState,
        isUserClubChampion: Boolean,
        userWonGoldenBoot: Boolean
    ): Triple<AwardRecipient, List<BallonDorCandidate>, Boolean> {
        val userStats = state.player.currentSeasonStats
        val gPlusA = userStats.goals + userStats.assists
        val avgRating = userStats.averageRating

        val userBallonDorScore = (gPlusA * 2.8) +
            (avgRating * 9.0) +
            (if (isUserClubChampion) 24.0 else 0.0) +
            (if (userWonGoldenBoot) 18.0 else 0.0)

        val osimhenScore = 78.0
        val rafaSilvaScore = 69.0
        val dzekoScore = 62.0

        val candidates = mutableListOf<BallonDorCandidate>()
        val userWins = userBallonDorScore > osimhenScore

        if (userWins) {
            candidates.add(
                BallonDorCandidate(
                    rank = 1,
                    playerName = state.player.fullName,
                    clubName = state.currentClub.name,
                    votesPercent = 46.8,
                    isUser = true,
                    goalsAndAssists = "${userStats.goals} Gol, ${userStats.assists} Asist",
                    ratingAverage = avgRating
                )
            )
            candidates.add(
                BallonDorCandidate(
                    rank = 2,
                    playerName = "Victor Osimhen",
                    clubName = "Galatasaray",
                    votesPercent = 31.4,
                    isUser = false,
                    goalsAndAssists = "26 Gol, 7 Asist",
                    ratingAverage = 7.7
                )
            )
            candidates.add(
                BallonDorCandidate(
                    rank = 3,
                    playerName = "Rafa Silva",
                    clubName = "Beşiktaş",
                    votesPercent = 14.2,
                    isUser = false,
                    goalsAndAssists = "18 Gol, 14 Asist",
                    ratingAverage = 7.5
                )
            )
        } else {
            val userIsTop3 = userBallonDorScore > dzekoScore
            candidates.add(
                BallonDorCandidate(
                    rank = 1,
                    playerName = "Victor Osimhen",
                    clubName = "Galatasaray",
                    votesPercent = 41.5,
                    isUser = false,
                    goalsAndAssists = "28 Gol, 6 Asist",
                    ratingAverage = 7.8
                )
            )
            if (userIsTop3) {
                candidates.add(
                    BallonDorCandidate(
                        rank = 2,
                        playerName = state.player.fullName,
                        clubName = state.currentClub.name,
                        votesPercent = 29.8,
                        isUser = true,
                        goalsAndAssists = "${userStats.goals} Gol, ${userStats.assists} Asist",
                        ratingAverage = avgRating
                    )
                )
                candidates.add(
                    BallonDorCandidate(
                        rank = 3,
                        playerName = "Rafa Silva",
                        clubName = "Beşiktaş",
                        votesPercent = 18.2,
                        isUser = false,
                        goalsAndAssists = "17 Gol, 12 Asist",
                        ratingAverage = 7.4
                    )
                )
            } else {
                candidates.add(
                    BallonDorCandidate(
                        rank = 2,
                        playerName = "Rafa Silva",
                        clubName = "Beşiktaş",
                        votesPercent = 28.5,
                        isUser = false,
                        goalsAndAssists = "19 Gol, 13 Asist",
                        ratingAverage = 7.5
                    )
                )
                candidates.add(
                    BallonDorCandidate(
                        rank = 3,
                        playerName = "Edin Džeko",
                        clubName = "Fenerbahçe",
                        votesPercent = 16.4,
                        isUser = false,
                        goalsAndAssists = "21 Gol, 5 Asist",
                        ratingAverage = 7.3
                    )
                )
            }
        }

        val winnerCandidate = candidates.first()
        val winnerAward = AwardRecipient(
            awardTitle = "Ballon d'Or (Yılın En İyi Futbolcusu)",
            playerName = winnerCandidate.playerName,
            clubName = winnerCandidate.clubName,
            statSummary = "${winnerCandidate.goalsAndAssists} • %${winnerCandidate.votesPercent} Oy",
            isUser = winnerCandidate.isUser,
            trophyIconName = "BALLON_DOR"
        )

        return Triple(winnerAward, candidates, userWins)
    }

    /**
     * Executes the complete season transition:
     * - Advances player age (player.age + 1)
     * - Archives season stats into careerArchive
     * - Applies Promotion & Relegation to clubs and user's contract/club
     * - Advances seasonYear (currentSeasonYear + 1)
     * - Generates full 38-week dynamic fixtures & 300-day calendar events
     * - Resets standings, scorers, energy, form and adds welcoming season events
     */
    fun finalizeSeasonAndTransition(
        state: CareerSaveState,
        summary: SeasonEndSummary
    ): Pair<CareerSaveState, String> {
        val nextSeasonYear = state.currentSeasonYear + 1
        val newAge = state.player.age + 1

        // 1. Archive current season
        val archivedSeason = state.player.currentSeasonStats.copy(
            seasonYear = state.currentSeasonYear,
            clubId = state.currentClub.id,
            clubName = state.currentClub.name,
            leagueName = state.currentClub.leagueName,
            trophiesWon = summary.playerTrophiesWon,
            individualAwards = summary.playerAwardsWon
        )
        val updatedArchive = state.player.careerArchive + archivedSeason

        // 2. Promotion & Relegation resolution for the player's club and league roster
        var targetLeagueId = state.currentLeagueId
        var targetLeagueName = state.currentClub.leagueName
        var targetTier = state.currentClub.tier

        val isUserPromoted = summary.promotedClubs.any { it.id == state.contract.clubId }
        val isUserRelegated = summary.relegatedClubs.any { it.id == state.contract.clubId }

        if (isUserPromoted) {
            when (state.currentLeagueId) {
                TFF1LigData.LEAGUE_ID -> {
                    targetLeagueId = SuperLigData.LEAGUE_ID
                    targetLeagueName = SuperLigData.LEAGUE_NAME
                    targetTier = 1
                }
                AmateurLeaguesData.LEAGUE_ID_BAL -> {
                    targetLeagueId = TFF3LigData.LEAGUE_ID_ALL
                    targetLeagueName = TFF3LigData.LEAGUE_NAME_ALL
                    targetTier = 4
                }
            }
        } else if (isUserRelegated) {
            when (state.currentLeagueId) {
                SuperLigData.LEAGUE_ID -> {
                    targetLeagueId = TFF1LigData.LEAGUE_ID
                    targetLeagueName = TFF1LigData.LEAGUE_NAME
                    targetTier = 2
                }
            }
        }

        // Updated clubs roster: Exchange relegated and promoted clubs
        val updatedClubs = state.clubs.toMutableList()
        if (summary.relegatedClubs.isNotEmpty() && summary.promotedClubs.isNotEmpty()) {
            val relegatedIds = summary.relegatedClubs.map { it.id }.toSet()
            updatedClubs.removeAll { it.id in relegatedIds }
            for (pClub in summary.promotedClubs) {
                if (updatedClubs.none { it.id == pClub.id }) {
                    updatedClubs.add(pClub.copy(leagueId = targetLeagueId, leagueName = targetLeagueName, tier = targetTier))
                }
            }
        }

        val updatedCurrentClub = state.currentClub.copy(
            leagueId = targetLeagueId,
            leagueName = targetLeagueName,
            tier = targetTier
        )
        val clubIdxInList = updatedClubs.indexOfFirst { it.id == updatedCurrentClub.id }
        if (clubIdxInList >= 0) {
            updatedClubs[clubIdxInList] = updatedCurrentClub
        } else {
            updatedClubs.add(updatedCurrentClub)
        }

        // 3. New contract with current club updated
        val updatedContract = state.contract.copy(
            clubId = updatedCurrentClub.id,
            clubName = updatedCurrentClub.name,
            startSeasonYear = nextSeasonYear,
            weeklyWageEur = if (isUserPromoted || summary.isUserClubChampion) {
                (state.contract.weeklyWageEur * 1.30).toLong()
            } else {
                state.contract.weeklyWageEur
            }
        )

        // 4. Boost player market value and followers from trophies and awards
        val marketValueBoost = (summary.playerAwardsWon.size * 250_000L) +
            (if (summary.isUserClubChampion) 500_000L else 0L) +
            (if (summary.userWonBallonDor) 2_500_000L else 0L)
        val followersBoost = (summary.playerAwardsWon.size * 5_000) +
            (if (summary.isUserClubChampion) 12_000 else 0L).toInt() +
            (if (summary.userWonBallonDor) 50_000 else 0L).toInt()

        val updatedPlayer = state.player.copy(
            age = newAge,
            energy = 100,
            morale = 88,
            form = 78,
            injuryDaysRemaining = 0,
            marketValueEur = state.player.marketValueEur + marketValueBoost,
            followersCount = state.player.followersCount + followersBoost,
            fanSupport = (state.player.fanSupport + if (summary.isUserClubChampion) 15 else 4).coerceIn(0, 100),
            careerArchive = updatedArchive,
            currentSeasonStats = SeasonStatsRecord(
                seasonYear = nextSeasonYear,
                clubId = updatedCurrentClub.id,
                clubName = updatedCurrentClub.name,
                leagueName = targetLeagueName
            )
        )

        // 5. Generate brand new full-season fixtures (38-week dynamic schedule)
        val newFixtures = CareerEngine.generateLeagueSchedule(targetLeagueId, updatedClubs)

        // 6. Generate dynamic full 300-day calendar events
        val newCalendarEvents = CareerEngine.buildCalendarFromFixtures(
            fixtures = newFixtures,
            userClubId = updatedCurrentClub.id,
            seasonYear = nextSeasonYear
        )

        // 7. Reset Standings for new season
        val resetStandings = updatedClubs.map { c ->
            StandingRow(
                clubId = c.id,
                clubName = c.name,
                shortName = c.shortName,
                primaryColorHex = c.primaryColorHex,
                played = 0,
                won = 0,
                drawn = 0,
                lost = 0,
                goalsFor = 0,
                goalsAgainst = 0,
                points = 0,
                recentForm = emptyList()
            )
        }

        // 8. Reset Top Scorers pool
        val resetScorers = listOf(
            LeagueScorerEntry(
                playerId = updatedPlayer.id,
                playerName = updatedPlayer.fullName,
                clubId = updatedCurrentClub.id,
                clubName = updatedCurrentClub.name,
                goals = 0,
                assists = 0,
                matches = 0,
                averageRating = 6.8,
                isUserPlayer = true
            )
        )

        // 9. Welcoming messages & News for the new season
        val seasonDateLabel = CareerEngine.formatGameDate(1, nextSeasonYear)
        val seasonStartNews = NewsEvent(
            id = "NEWS_NEW_SEASON_$nextSeasonYear",
            dayIndex = 1,
            dateLabel = seasonDateLabel,
            category = "LİG",
            headline = "$nextSeasonYear/${nextSeasonYear + 1} Futbol Sezonu Başladı! $targetLeagueName Fikstürü Açıklandı",
            body = "${updatedCurrentClub.name}, teknik direktör ${updatedCurrentClub.managerName} yönetiminde yeni sezon hazırlıklarına başladı. $newAge yaşındaki yıldız ${updatedPlayer.fullName}, taraftarların en büyük umudu.",
            relatedClubName = updatedCurrentClub.name
        )

        val updatedNpcs = state.npcs.map { npc ->
            if (npc.id == "NPC_MANAGER") {
                npc.copy(
                    messages = npc.messages + ChatMessage(
                        id = "MSG_SEASON_WELCOME_$nextSeasonYear",
                        senderName = npc.name,
                        isFromUser = false,
                        text = "Yeni sezonumuz hayırlı olsun ${updatedPlayer.firstName}! Bu sezon $targetLeagueName'de hedeflerimiz çok daha yüksek. Kamp programı hazır, sahada liderliğini bekliyorum.",
                        dayIndex = 1,
                        timeLabel = "09:30",
                        emotionTag = "Yeni Sezon"
                    ),
                    unreadCount = npc.unreadCount + 1
                )
            } else npc
        }

        // 10. Achievements
        val newAchievements = state.achievements.toMutableList()
        if (summary.isUserClubChampion) {
            newAchievements.add(
                CareerAchievement(
                    id = "ACH_CHAMPION_${state.currentSeasonYear}",
                    title = "${state.currentClub.leagueName} Şampiyonu",
                    description = "${state.currentClub.name} ile ${state.currentSeasonYear} sezonunda şampiyonluk kupasını kaldırdı.",
                    dateLabel = seasonDateLabel,
                    category = "KUPA"
                )
            )
        }
        if (summary.userWonBallonDor) {
            newAchievements.add(
                CareerAchievement(
                    id = "ACH_BALLON_DOR_${state.currentSeasonYear}",
                    title = "Ballon d'Or Sahibi",
                    description = "${state.currentSeasonYear} yılında dünyanın en iyi futbolcusu seçilerek Ballon d'Or kazandı.",
                    dateLabel = seasonDateLabel,
                    category = "BİREYSEL"
                )
            )
        }

        val nextState = state.copy(
            updatedAtMs = System.currentTimeMillis(),
            currentSeasonYear = nextSeasonYear,
            currentDayIndex = 1,
            currentHour = 9,
            currentLeagueId = targetLeagueId,
            player = updatedPlayer,
            contract = updatedContract,
            clubs = updatedClubs,
            standings = resetStandings,
            fixtures = newFixtures,
            calendarEvents = newCalendarEvents,
            topScorers = resetScorers,
            npcs = updatedNpcs,
            newsEvents = listOf(seasonStartNews) + state.newsEvents.take(15),
            achievements = newAchievements,
            lastMatchReport = null
        )

        val summaryMessage = buildString {
            append("$nextSeasonYear/${nextSeasonYear + 1} yeni futbol sezonuna başarıyla geçildi!")
            append(" Yaş: $newAge • Lig: $targetLeagueName")
            if (isUserPromoted) append(" (YÜKSELDİ! ⬆️)")
            if (isUserRelegated) append(" (DÜŞTÜ ⬇️)")
        }

        return nextState to summaryMessage
    }
}
