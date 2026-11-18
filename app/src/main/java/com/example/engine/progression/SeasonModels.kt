package com.example.engine.progression

import com.example.model.Club
import com.example.model.SeasonStatsRecord
import com.example.model.StandingRow
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AwardRecipient(
    val awardTitle: String,
    val playerName: String,
    val clubName: String,
    val statSummary: String,
    val isUser: Boolean,
    val trophyIconName: String = "TROPHY_GOLD"
)

@JsonClass(generateAdapter = true)
data class BallonDorCandidate(
    val rank: Int,
    val playerName: String,
    val clubName: String,
    val votesPercent: Double,
    val isUser: Boolean,
    val goalsAndAssists: String,
    val ratingAverage: Double
)

@JsonClass(generateAdapter = true)
data class LeaguePromotionRelegationSummary(
    val leagueId: String,
    val leagueName: String,
    val tier: Int,
    val promotedToHigherTier: List<Club> = emptyList(),
    val relegatedToLowerTier: List<Club> = emptyList(),
    val userClubStatus: String // "CHAMPION_PROMOTED", "RELEGATED", "STAYED", "CHAMPION_SUPER_LIG"
)

@JsonClass(generateAdapter = true)
data class SeasonEndSummary(
    val seasonYear: Int,
    val leagueId: String,
    val leagueName: String,
    val championClub: Club,
    val isUserClubChampion: Boolean,
    val userClubStanding: StandingRow,
    val userLeagueRank: Int,
    val totalTeamsInLeague: Int,
    val promotedClubs: List<Club>,
    val relegatedClubs: List<Club>,
    val promotionRelegationHierarchy: List<LeaguePromotionRelegationSummary>,
    val goldenBootWinner: AwardRecipient,
    val assistKingWinner: AwardRecipient,
    val youngPlayerOfYear: AwardRecipient,
    val ballonDorWinner: AwardRecipient,
    val ballonDorTop3: List<BallonDorCandidate>,
    val userWonBallonDor: Boolean,
    val userWonGoldenBoot: Boolean,
    val userWonYoungPlayer: Boolean,
    val playerTrophiesWon: List<String>,
    val playerAwardsWon: List<String>,
    val userSeasonStats: SeasonStatsRecord,
    val pressCelebrationHeadline: String,
    val managerCelebrationQuote: String,
    val fanCelebrationMessage: String
)
