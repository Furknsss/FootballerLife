package com.example.model

import com.squareup.moshi.JsonClass
import kotlin.math.roundToInt

enum class Position(val code: String, val titleTr: String, val category: String) {
    GK("KL", "Kaleci", "Kaleci"),
    CB("STP", "Stoper", "Savunma"),
    FB("BEK", "Bek", "Savunma"),
    WB("K.BEK", "Kanat Bek", "Savunma"),
    DM("DOS", "Ön Libero", "Orta Saha"),
    CM("MO", "Merkez Orta Saha", "Orta Saha"),
    AM("OOS", "On Numara", "Orta Saha"),
    WG("KNT", "Kanat", "Hücum"),
    ST("SNT", "Santrfor", "Hücum")
}

enum class PreferredFoot(val labelTr: String) {
    RIGHT("Sağ"),
    LEFT("Sol"),
    BOTH("Her İkisi")
}

enum class SquadRole(val labelTr: String, val minTrustRequired: Int) {
    STAR("Takım Yıldızı", 65),
    STARTER("İlk 11 Oyuncusu", 48),
    ROTATION("Rotasyon Oyuncusu", 35),
    PROSPECT("Genç Yetenek", 25),
    RESERVE("Yedek / Kadro Adayı", 0)
}

enum class StartingStatus(val labelTr: String) {
    STARTER("İlk 11"),
    BENCH("Yedek Kulübesi"),
    OUT_OF_SQUAD("Kadro Dışı")
}

enum class GraphicsQuality(val labelTr: String, val particleCount: Int) {
    HIGH("Yüksek (60 FPS + Efektler)", 36),
    BALANCED("Dengeli", 18),
    PERFORMANCE("Performans", 6)
}

@JsonClass(generateAdapter = true)
data class PlayerAppearance(
    val skinToneHex: String = "#D9A17C",
    val hairStyle: String = "Modern Kısa",
    val hairColorHex: String = "#1F1B18",
    val facialPreset: String = "Atletik Keskin",
    val kitCollarStyle: String = "V-Yaka",
    val bootColorHex: String = "#20C773"
)

@JsonClass(generateAdapter = true)
data class PlayerAttributes(
    val shooting: Int = 58,
    val passing: Int = 58,
    val dribbling: Int = 58,
    val firstTouch: Int = 58,
    val pace: Int = 62,
    val agility: Int = 60,
    val stamina: Int = 62,
    val strength: Int = 56,
    val defending: Int = 45,
    val tactics: Int = 54,
    val vision: Int = 56,
    val setPieces: Int = 52,
    val goalkeeping: Int = 20
) {
    val cardPace: Int get() = ((pace * 0.65) + (agility * 0.35)).roundToInt().coerceIn(1, 99)
    val cardShooting: Int get() = ((shooting * 0.75) + (setPieces * 0.25)).roundToInt().coerceIn(1, 99)
    val cardPassing: Int get() = ((passing * 0.55) + (vision * 0.30) + (setPieces * 0.15)).roundToInt().coerceIn(1, 99)
    val cardDribbling: Int get() = ((dribbling * 0.50) + (firstTouch * 0.30) + (agility * 0.20)).roundToInt().coerceIn(1, 99)
    val cardDefending: Int get() = ((defending * 0.70) + (tactics * 0.30)).roundToInt().coerceIn(1, 99)
    val cardPhysical: Int get() = ((stamina * 0.55) + (strength * 0.45)).roundToInt().coerceIn(1, 99)

    fun calculateOverall(position: Position): Int {
        val weighted = when (position) {
            Position.GK -> goalkeeping * 0.65 + agility * 0.12 + positioningScore() * 0.13 + passing * 0.10
            Position.CB -> defending * 0.42 + strength * 0.22 + tactics * 0.16 + pace * 0.10 + passing * 0.10
            Position.FB -> defending * 0.28 + pace * 0.24 + stamina * 0.18 + passing * 0.16 + dribbling * 0.14
            Position.WB -> pace * 0.24 + stamina * 0.20 + passing * 0.20 + dribbling * 0.18 + defending * 0.18
            Position.DM -> defending * 0.30 + passing * 0.22 + tactics * 0.20 + stamina * 0.15 + strength * 0.13
            Position.CM -> passing * 0.28 + vision * 0.20 + stamina * 0.16 + firstTouch * 0.14 + dribbling * 0.12 + defending * 0.10
            Position.AM -> vision * 0.24 + passing * 0.24 + dribbling * 0.20 + firstTouch * 0.16 + shooting * 0.16
            Position.WG -> pace * 0.26 + dribbling * 0.26 + agility * 0.16 + shooting * 0.16 + passing * 0.16
            Position.ST -> shooting * 0.38 + firstTouch * 0.16 + pace * 0.16 + dribbling * 0.15 + strength * 0.15
        }
        return weighted.roundToInt().coerceIn(40, 99)
    }

    private fun positioningScore(): Double = (tactics + strength) / 2.0
}

@JsonClass(generateAdapter = true)
data class SeasonStatsRecord(
    val seasonYear: Int = 2026,
    val clubId: String = "",
    val clubName: String = "",
    val leagueName: String = "",
    val appearances: Int = 0,
    val starts: Int = 0,
    val minutesPlayed: Int = 0,
    val goals: Int = 0,
    val assists: Int = 0,
    val totalRatingSum: Double = 0.0,
    val yellowCards: Int = 0,
    val redCards: Int = 0,
    val injuriesCount: Int = 0,
    val trophiesWon: List<String> = emptyList(),
    val individualAwards: List<String> = emptyList()
) {
    val averageRating: Double
        get() = if (appearances > 0) (totalRatingSum / appearances * 10.0).roundToInt() / 10.0 else 6.5
}

@JsonClass(generateAdapter = true)
data class PlayerProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val birthDate: String,
    val age: Int,
    val country: String,
    val city: String,
    val nationality: String,
    val kitName: String,
    val kitNumber: Int,
    val primaryPosition: Position,
    val secondaryPositions: List<Position>,
    val preferredFoot: PreferredFoot,
    val heightCm: Int,
    val weightKg: Int,
    val appearance: PlayerAppearance,
    val attributes: PlayerAttributes,
    val potential: Int,
    val energy: Int = 92, // 0..100
    val morale: Int = 78, // 0..100
    val form: Int = 70, // 0..100
    val injuryDaysRemaining: Int = 0,
    val marketValueEur: Long = 125_000L,
    val followersCount: Int = 1_450,
    val fanSupport: Int = 58, // 0..100
    val backgroundStory: String = "Altyapı Akademisi Çıkışlı",
    val isRealPlayer: Boolean = false,
    val dataSourceInfo: String = "Football Life Simülasyon Verisi (2026/27)",
    val currentSeasonStats: SeasonStatsRecord = SeasonStatsRecord(),
    val careerArchive: List<SeasonStatsRecord> = emptyList(),
    val trainingCountThisWeek: Int = 0,
    val tacticalRole: String = "Klasik 10 Numara",
    val unlockedTraits: List<String> = emptyList(),
    val nationalTeamCaps: Int = 0,
    val nationalTeamGoals: Int = 0,
    val activeCleatBrand: String = "Standart",
    val activeCleatModel: String = "Klasik Deri",
    val activeCleatBonusStat: String = "",
    val activeSponsorshipBonusEur: Long = 0L,
    val trophyCabinet: List<String> = emptyList(),
    val romanticPartnerName: String? = null,
    val relationshipStage: String = "BEKAR",
    val relationshipAffection: Int = 50
) {
    val fullName: String get() = "$firstName $lastName".trim()
    val overallRating: Int get() = attributes.calculateOverall(primaryPosition)
    val fame: Int get() = fanSupport
}

@JsonClass(generateAdapter = true)
data class SquadMemberInfo(
    val id: String,
    val name: String,
    val position: Position,
    val kitNumber: Int,
    val overall: Int,
    val age: Int,
    val isCaptain: Boolean = false
)

@JsonClass(generateAdapter = true)
data class Club(
    val id: String,
    val name: String,
    val shortName: String,
    val country: String,
    val city: String,
    val leagueId: String,
    val leagueName: String,
    val tier: Int, // 1 = Süper Lig / Premier, 2 = 1.Lig, 3 = 2.Lig, 4 = 3.Lig, 5 = BAL / Amatör
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val budgetEur: Long,
    val facilityQuality: Int, // 1..5
    val teamStrength: Int, // 42..92
    val managerName: String,
    val managerTacticalStyle: String,
    val stadiumName: String,
    val stadiumCapacity: Int,
    val isDemoClub: Boolean = false,
    val keySquadMembers: List<SquadMemberInfo> = emptyList(),
    val foundationYear: Int = 1923,
    val seasonObjective: String = "Ligde Kalma"
)

@JsonClass(generateAdapter = true)
data class Contract(
    val clubId: String,
    val clubName: String,
    val weeklyWageEur: Long,
    val matchAppearanceBonusEur: Long,
    val goalBonusEur: Long,
    val startSeasonYear: Int,
    val endSeasonYear: Int,
    val role: SquadRole,
    val agentName: String = "Kemal Soylu"
)

@JsonClass(generateAdapter = true)
data class StandingRow(
    val clubId: String,
    val clubName: String,
    val shortName: String,
    val primaryColorHex: String,
    val played: Int = 0,
    val won: Int = 0,
    val drawn: Int = 0,
    val lost: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val points: Int = 0,
    val recentForm: List<String> = emptyList() // "G", "B", "M"
) {
    val goalDifference: Int get() = goalsFor - goalsAgainst
}

@JsonClass(generateAdapter = true)
data class LeagueFixture(
    val id: String,
    val leagueId: String,
    val week: Int,
    val dayIndex: Int,
    val dateLabel: String,
    val kickOffTime: String = "20:00",
    val homeClubId: String,
    val homeClubName: String,
    val awayClubId: String,
    val awayClubName: String,
    val stadiumName: String,
    val played: Boolean = false,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val isCupMatch: Boolean = false,
    val goalSummary: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class LeagueScorerEntry(
    val playerId: String,
    val playerName: String,
    val clubId: String,
    val clubName: String,
    val goals: Int = 0,
    val assists: Int = 0,
    val matches: Int = 0,
    val averageRating: Double = 6.8,
    val isUserPlayer: Boolean = false
)

@JsonClass(generateAdapter = true)
data class MatchEventRecord(
    val minute: Int,
    val type: String, // GOL, ASİST, SARI_KART, KIRMIZI_KART, ŞUT, KURTARIŞ, DEĞİŞİKLİK, KRİTİK_KARAR
    val teamId: String,
    val playerName: String,
    val description: String,
    val involvesUser: Boolean = false
)

@JsonClass(generateAdapter = true)
data class CompletedMatchReport(
    val fixtureId: String,
    val week: Int,
    val dateLabel: String,
    val homeClubId: String,
    val homeClubName: String,
    val awayClubId: String,
    val awayClubName: String,
    val homeScore: Int,
    val awayScore: Int,
    val homePossessionPct: Int,
    val homeShots: Int,
    val awayShots: Int,
    val homeShotsOnTarget: Int,
    val awayShotsOnTarget: Int,
    val userPlayedMinutes: Int,
    val userStarted: Boolean,
    val userGoals: Int,
    val userAssists: Int,
    val userShots: Int,
    val userPassesCompleted: Int,
    val userPassesAttempted: Int,
    val userKeyDecisionsMade: Int,
    val userSuccessfulDecisions: Int,
    val userRating: Double,
    val managerFeedback: String,
    val teammateReaction: String,
    val pressHeadline: String,
    val energySpent: Int,
    val wageAndBonusEarnedEur: Long,
    val events: List<MatchEventRecord>
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    val id: String,
    val senderName: String,
    val isFromUser: Boolean,
    val text: String,
    val dayIndex: Int,
    val timeLabel: String,
    val emotionTag: String = "Doğal",
    val impactSummary: String? = null
)

@JsonClass(generateAdapter = true)
data class NpcCharacter(
    val id: String,
    val name: String,
    val roleTitle: String, // Teknik Direktör, Menajer, Aile, Takım Kaptanı, Takım Arkadaşı, Partner, Basın Mensubu
    val avatarColorHex: String,
    val personality: String,
    val affinity: Int, // 0..100 Yakınlık
    val trust: Int, // 0..100 Güven
    val currentMood: String = "Sakin",
    val shortTermGoal: String,
    val longTermGoal: String,
    val importantMemories: List<String> = emptyList(),
    val quickReplyOptions: List<String> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val unreadCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class CalendarEvent(
    val id: String,
    val dayIndex: Int,
    val dateLabel: String,
    val timeLabel: String,
    val location: String,
    val eventType: String, // MAÇ, ANTRENMAN, DİNLENME, KULÜP_TOPLANTISI, BASIN, SOSYAL, TRANSFER
    val title: String,
    val description: String,
    val opponentClubId: String? = null,
    val fixtureId: String? = null,
    val completed: Boolean = false,
    val outcomeSummary: String? = null
)

@JsonClass(generateAdapter = true)
data class FinancialTransaction(
    val id: String,
    val dayIndex: Int,
    val dateLabel: String,
    val category: String, // MAAŞ, MAÇ_PRİMİ, GOL_PRİMİ, SPONSORLUK, ALIŞVERİŞ, YAŞAM_TARZI, YATIRIM
    val amountEur: Long, // positive for income, negative for expense
    val description: String,
    val balanceAfterEur: Long
)

@JsonClass(generateAdapter = true)
data class NewsEvent(
    val id: String,
    val dayIndex: Int,
    val dateLabel: String,
    val category: String, // MAÇ SONUCU, TRANSFER, KULÜP, RÖPORTAJ, LİG
    val headline: String,
    val body: String,
    val relatedClubName: String,
    val sourceGameEventId: String = "",
    val fanReaction: String = "Destekleyici"
)

@JsonClass(generateAdapter = true)
data class InstaCommentItem(
    val id: String,
    val authorHandle: String,
    val authorName: String,
    val isVerified: Boolean = false,
    val avatarHex: String = "#E1306C",
    val text: String,
    val timeLabel: String = "2s",
    val likesCount: Int = 12,
    val likedByUser: Boolean = false
)

@JsonClass(generateAdapter = true)
data class InstaDmThread(
    val id: String,
    val senderName: String,
    val senderHandle: String,
    val senderRole: String,
    val avatarHex: String,
    val isVerified: Boolean = true,
    val followersLabel: String = "48,2B takipçi",
    val isPendingRequest: Boolean = false,
    val unreadCount: Int = 0,
    val messages: List<ChatMessage> = emptyList()
) {
    val authorName: String get() = senderName
    val handle: String get() = senderHandle
    val roleLabel: String get() = senderRole
    val followerCountLabel: String get() = followersLabel
}

@JsonClass(generateAdapter = true)
data class SocialMediaPost(
    val id: String,
    val dayIndex: Int,
    val dateLabel: String,
    val authorName: String,
    val authorHandle: String,
    val isUserPost: Boolean,
    val content: String,
    val likesCount: Int,
    val commentsCount: Int,
    val sentiment: String, // Destekleyici, Eleştirel, Tarafsız
    val topFanComments: List<String> = emptyList(),
    val instaComments: List<InstaCommentItem> = emptyList(),
    val isVerifiedAuthor: Boolean = true,
    val locationLabel: String = "",
    val imageSceneType: String = "STADIUM",
    val likedByUser: Boolean = false,
    val savedByUser: Boolean = false
)

@JsonClass(generateAdapter = true)
data class TransferOffer(
    val id: String,
    val clubId: String,
    val clubName: String,
    val leagueId: String,
    val leagueName: String,
    val clubTier: Int,
    val clubStrength: Int,
    val primaryColorHex: String,
    val offeredWeeklyWageEur: Long,
    val offeredAppearanceBonusEur: Long,
    val offeredGoalBonusEur: Long,
    val contractYears: Int,
    val promisedRole: SquadRole,
    val transferType: String = "Bonservisli Transfer", // or "Kiralık"
    val expiresDayIndex: Int,
    val negotiationCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class LifestyleVenueAction(
    val id: String,
    val venueName: String,
    val category: String, // EV, TESİS, RESTORAN, KAFE, AVM, SPOR_SALONU, SOSYAL, TATİL
    val title: String,
    val subtitle: String,
    val costEur: Long,
    val energyDelta: Int,
    val moraleDelta: Int,
    val managerTrustDelta: Int,
    val targetNpcRole: String?,
    val relationshipDelta: Int,
    val followersDelta: Int = 0,
    val attributeBonusType: String? = null
)

@JsonClass(generateAdapter = true)
data class LifestylePurchaseItem(
    val id: String,
    val category: String, // EV, ARAÇ, EKİPMAN, YATIRIM, STİL
    val name: String,
    val description: String,
    val priceEur: Long,
    val passiveMoraleBonus: Int,
    val passiveEnergyRecoveryBonus: Int,
    val weeklyDividendEur: Long = 0L
)

@JsonClass(generateAdapter = true)
data class DynamicStoryChoice(
    val id: String,
    val label: String,
    val description: String,
    val moraleDelta: Int = 0,
    val energyDelta: Int = 0,
    val managerTrustDelta: Int = 0,
    val teammateAffinityDelta: Int = 0,
    val fanSupportDelta: Int = 0,
    val moneyDeltaEur: Long = 0L,
    val consequenceHeadline: String
)

@JsonClass(generateAdapter = true)
data class DynamicCareerDilemma(
    val id: String,
    val dayIndex: Int,
    val category: String,
    val title: String,
    val narrative: String,
    val speakerName: String,
    val speakerRole: String,
    val choices: List<DynamicStoryChoice>,
    val sceneType: String = "MANAGER_OFFICE", // MANAGER_OFFICE, MEDICAL_CHECKUP, TRANSFER_NEGOTIATION, LOCKER_ROOM
    val locationBadge: String = "KULÜP TESİSLERİ • ÖZEL GÖRÜŞME"
)

@JsonClass(generateAdapter = true)
data class GameEvent(
    val id: String,
    val dayIndex: Int,
    val timestampMs: Long,
    val eventType: String,
    val contextDescription: String,
    val appliedEffects: String
)

// =========================================================================
// FOOTBALL LIFE 2.0 — CAREER ARCHIVE & LIVING PHONE OS MODELS
// =========================================================================

@JsonClass(generateAdapter = true)
data class MarketValuePoint(
    val dayIndex: Int,
    val dateLabel: String,
    val valueEur: Long,
    val clubName: String,
    val reason: String
)

@JsonClass(generateAdapter = true)
data class TransferHistoryEntry(
    val id: String,
    val seasonLabel: String,
    val dateLabel: String,
    val fromClubName: String,
    val toClubName: String,
    val toClubShortName: String,
    val toClubPrimaryHex: String,
    val transferType: String, // Altyapıdan A Takıma, Bonservisli Transfer, Kiralık, Bedelsiz
    val feeEur: Long,
    val marketValueAtTransferEur: Long
)

@JsonClass(generateAdapter = true)
data class AttributeHistoryPoint(
    val dayIndex: Int,
    val dateLabel: String,
    val overall: Int,
    val pace: Int,
    val shooting: Int,
    val passing: Int,
    val dribbling: Int,
    val defending: Int,
    val physical: Int,
    val averageMatchRating: Double = 6.6
)

@JsonClass(generateAdapter = true)
data class InjuryRecord(
    val id: String,
    val dayIndex: Int,
    val dateLabel: String,
    val injuryType: String,
    val recoveryDays: Int,
    val matchesMissed: Int
)

@JsonClass(generateAdapter = true)
data class CareerAchievement(
    val id: String,
    val title: String,
    val description: String,
    val dateLabel: String,
    val category: String // KUPA, BİREYSEL, KARİYER_TAŞI
)

@JsonClass(generateAdapter = true)
data class StoryItem(
    val id: String,
    val authorName: String,
    val authorRole: String,
    val avatarHex: String,
    val title: String,
    val caption: String,
    val sceneType: String, // STADIUM, TRAINING, LOCKER, TRANSFER
    val dayIndex: Int,
    val viewed: Boolean = false,
    val reacted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class XPostItem(
    val id: String,
    val dayIndex: Int,
    val dateLabel: String,
    val authorName: String,
    val handle: String,
    val category: String, // GAZETECİ, TARAFTAR, KULÜP, OYUNCU, YORUMCU
    val isVerified: Boolean,
    val text: String,
    val likes: Int,
    val reposts: Int,
    val likedByUser: Boolean = false,
    val repostedByUser: Boolean = false
)

@JsonClass(generateAdapter = true)
data class GalleryPhotoItem(
    val id: String,
    val dayIndex: Int,
    val dateLabel: String,
    val album: String, // Maçlar, Kutlamalar, Antrenman, Sosyal Hayat, Paylaşımlar
    val title: String,
    val caption: String,
    val sceneDrawableType: String, // STADIUM, TRAINING, LOCKER, TRANSFER
    val sharedOnSocial: Boolean = false
)

@JsonClass(generateAdapter = true)
data class PhoneCallRecord(
    val id: String,
    val npcId: String,
    val callerName: String,
    val callerRole: String,
    val dayIndex: Int,
    val dateLabel: String,
    val topic: String,
    val transcript: String,
    val options: List<String> = emptyList(),
    val completed: Boolean = false,
    val conversationTurns: List<ChatMessage> = emptyList()
)

@JsonClass(generateAdapter = true)
data class CareerSaveState(
    val saveSlotId: Int,
    val saveVersion: Int = 3,
    val careerTitle: String,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val currentDayIndex: Int = 1, // Day 1 = 10 Ağustos 2026 Pazartesi
    val currentHour: Int = 9, // Synchronized clock: 9 (09:00), 13 (13:00), 17 (17:00), 21 (21:00)
    val currentSeasonYear: Int = 2026,
    val currentLeagueId: String,
    val player: PlayerProfile,
    val contract: Contract,
    val balanceEur: Long,
    val managerTrust: Int = 56, // 0..100
    val teamChemistry: Int = 62, // 0..100
    val nextMatchStartingStatus: StartingStatus = StartingStatus.BENCH,
    val trainedToday: Boolean = false,
    val socialActionCountToday: Int = 0,
    val pressCompletedForLastMatch: Boolean = true,
    val clubs: List<Club>,
    val standings: List<StandingRow>,
    val fixtures: List<LeagueFixture>,
    val topScorers: List<LeagueScorerEntry>,
    val npcs: List<NpcCharacter>,
    val calendarEvents: List<CalendarEvent>,
    val transactions: List<FinancialTransaction>,
    val newsEvents: List<NewsEvent>,
    val socialPosts: List<SocialMediaPost>,
    val transferOffers: List<TransferOffer> = emptyList(),
    val ownedLifestyleItemIds: List<String> = emptyList(),
    val activeDilemma: DynamicCareerDilemma? = null,
    val lastMatchReport: CompletedMatchReport? = null,
    val eventHistory: List<GameEvent> = emptyList(),
    // Football Life 2.0 extended state (all with defaults for seamless v1/v2 save loading)
    val marketValueHistory: List<MarketValuePoint> = emptyList(),
    val transferHistory: List<TransferHistoryEntry> = emptyList(),
    val attributeHistory: List<AttributeHistoryPoint> = emptyList(),
    val injuryHistory: List<InjuryRecord> = emptyList(),
    val achievements: List<CareerAchievement> = emptyList(),
    val stories: List<StoryItem> = emptyList(),
    val xPosts: List<XPostItem> = emptyList(),
    val galleryPhotos: List<GalleryPhotoItem> = emptyList(),
    val callRecords: List<PhoneCallRecord> = emptyList(),
    val instaDmThreads: List<InstaDmThread> = emptyList(),
    val phoneWallpaperId: String = "STADIUM_NIGHT",
    val completedDailyTaskIds: List<String> = emptyList(),
    val hasPendingSeasonCeremony: Boolean = false,
    val pendingNationalCallUp: Boolean = false,
    val activeSponsorshipOfferCount: Int = 0,
    val activeCrisisSeverity: Int = 0
) {
    val currentClub: Club
        get() = clubs.find { it.id == contract.clubId } ?: clubs.first()

    val formattedClockTime: String
        get() = "${currentHour.coerceIn(0, 23).toString().padStart(2, '0')}:00"

    val seasonDayInCycle: Int
        get() = ((currentDayIndex - 1).coerceAtLeast(0) % 300) + 1

    val isTransferWindowOpen: Boolean
        get() {
            val d = seasonDayInCycle
            // Days 1..30: Yaz Transfer Dönemi (Ağustos - 10 Eylül)
            // Days 140..169: Ocak Ara Transfer Dönemi (1 Ocak - 30 Ocak)
            // Days 260..300: Sezon Sonu Transfer Dönemi (Mayıs)
            return d in 1..30 || d in 140..169 || d in 260..300
        }

    val transferWindowNameTr: String
        get() {
            val d = seasonDayInCycle
            return when {
                d in 1..30 -> "Yaz Transfer Dönemi"
                d in 140..169 -> "Ocak Ara Transfer Dönemi"
                d in 260..300 -> "Sezon Sonu Transfer Dönemi"
                d in 31..139 -> "Transfer Dönemi Kapalı (Ocak Ara Transfer'e ${140 - d} Gün)"
                else -> "Transfer Dönemi Kapalı (Yaz Dönemine ${260 - d} Gün)"
            }
        }

    val transferWindowDaysRemaining: Int
        get() {
            val d = seasonDayInCycle
            return when {
                d in 1..30 -> 31 - d
                d in 140..169 -> 170 - d
                d in 260..300 -> 301 - d
                d in 31..139 -> 140 - d
                else -> 260 - d
            }
        }

    val nextUnplayedUserFixture: LeagueFixture?
        get() = fixtures
            .filter { !it.played && (it.homeClubId == contract.clubId || it.awayClubId == contract.clubId) }
            .minByOrNull { it.dayIndex }

    val todayUserFixture: LeagueFixture?
        get() = fixtures.find {
            !it.played &&
                it.dayIndex == currentDayIndex &&
                (it.homeClubId == contract.clubId || it.awayClubId == contract.clubId)
        }

    val totalUnreadMessages: Int
        get() = npcs.sumOf { it.unreadCount }

    val totalUnreadInstaDms: Int
        get() = instaDmThreads.sumOf { it.unreadCount }

    val totalPendingInstaRequests: Int
        get() = instaDmThreads.count { it.isPendingRequest }

    val totalUnreadStories: Int
        get() = stories.count { !it.viewed }

    val totalPendingCalls: Int
        get() = callRecords.count { !it.completed }

    val totalUnreadPhoneItems: Int
        get() = totalUnreadMessages + totalUnreadInstaDms + totalPendingCalls
}
