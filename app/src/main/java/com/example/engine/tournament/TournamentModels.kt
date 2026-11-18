package com.example.engine.tournament

import com.example.model.Position
import com.squareup.moshi.JsonClass

/**
 * Turnuva ve Kupa Tipleri
 */
enum class TournamentType(
    val id: String,
    val title: String,
    val shortName: String,
    val iconEmoji: String,
    val tierLabel: String,
    val primaryColorHex: String,
    val isCupMatch: Boolean = true
) {
    ZIRAAT_TURKIYE_KUPASI(
        id = "TR_CUP",
        title = "Ziraat Türkiye Kupası",
        shortName = "ZTK",
        iconEmoji = "🏆",
        tierLabel = "Ulusal Eleme Kupası",
        primaryColorHex = "#C0392B",
        isCupMatch = true
    ),
    TURKCELL_SUPER_KUPA(
        id = "TR_SUPER_CUP",
        title = "Turkcell Süper Kupa",
        shortName = "Süper Kupa",
        iconEmoji = "🛡️",
        tierLabel = "Şampiyonlar Karşılaşması",
        primaryColorHex = "#D4AC0D",
        isCupMatch = true
    ),
    UEFA_CHAMPIONS_LEAGUE(
        id = "UEFA_UCL",
        title = "UEFA Şampiyonlar Ligi",
        shortName = "UCL",
        iconEmoji = "⭐",
        tierLabel = "Avrupa 1 Numaralı Kupa",
        primaryColorHex = "#1B4F72",
        isCupMatch = true
    ),
    UEFA_EUROPA_LEAGUE(
        id = "UEFA_UEL",
        title = "UEFA Avrupa Ligi",
        shortName = "UEL",
        iconEmoji = "🟠",
        tierLabel = "Avrupa 2 Numaralı Kupa",
        primaryColorHex = "#E67E22",
        isCupMatch = true
    ),
    UEFA_CONFERENCE_LEAGUE(
        id = "UEFA_UECL",
        title = "UEFA Konferans Ligi",
        shortName = "UECL",
        iconEmoji = "🟢",
        tierLabel = "Avrupa 3 Numaralı Kupa",
        primaryColorHex = "#27AE60",
        isCupMatch = true
    ),
    NATIONAL_WORLD_CUP_QUALIFIERS(
        id = "NAT_WCQ",
        title = "FIFA Dünya Kupası Elemeleri",
        shortName = "DK Elemeleri",
        iconEmoji = "🇹🇷",
        tierLabel = "A Millî Takım",
        primaryColorHex = "#E74C3C",
        isCupMatch = false
    ),
    NATIONAL_EURO(
        id = "NAT_EURO",
        title = "UEFA Avrupa Şampiyonası",
        shortName = "EURO 2028",
        iconEmoji = "🇹🇷",
        tierLabel = "A Millî Takım",
        primaryColorHex = "#E74C3C",
        isCupMatch = false
    ),
    NATIONAL_NATIONS_LEAGUE(
        id = "NAT_UNL",
        title = "UEFA Uluslar Ligi",
        shortName = "Uluslar Ligi",
        iconEmoji = "🇹🇷",
        tierLabel = "A Millî Takım",
        primaryColorHex = "#E74C3C",
        isCupMatch = false
    ),
    NATIONAL_FRIENDLY(
        id = "NAT_FRIENDLY",
        title = "Uluslararası Hazırlık Maçı",
        shortName = "Millî Hazırlık",
        iconEmoji = "🇹🇷",
        tierLabel = "A Millî Takım",
        primaryColorHex = "#E74C3C",
        isCupMatch = false
    );

    val isNationalTeam: Boolean
        get() = this in listOf(
            NATIONAL_WORLD_CUP_QUALIFIERS,
            NATIONAL_EURO,
            NATIONAL_NATIONS_LEAGUE,
            NATIONAL_FRIENDLY
        )

    val isEuropeanCup: Boolean
        get() = this in listOf(
            UEFA_CHAMPIONS_LEAGUE,
            UEFA_EUROPA_LEAGUE,
            UEFA_CONFERENCE_LEAGUE
        )
}

/**
 * Kupa ve Turnuva Eleme Aşamaları
 */
enum class TournamentStage(
    val stageNameTr: String,
    val stageOrder: Int
) {
    PRE_ROUND("Ön Eleme Turu", 1),
    GROUP_STAGE("Lig / Grup Aşaması", 2),
    ROUND_OF_32("Son 32 Turu", 3),
    ROUND_OF_16("Son 16 Turu", 4),
    QUARTER_FINAL("Çeyrek Final", 5),
    SEMI_FINAL("Yarı Final", 6),
    FINAL("Büyük Final", 7),
    SUPER_CUP_FINAL("Süper Kupa Finali", 8)
}

/**
 * Tek bir kupa eleme maçı kaydı (Bracket)
 */
@JsonClass(generateAdapter = true)
data class CupBracketMatch(
    val matchId: String,
    val tournamentType: TournamentType,
    val stage: TournamentStage,
    val homeClubId: String,
    val homeClubName: String,
    val homeClubColorHex: String = "#A93226",
    val awayClubId: String,
    val awayClubName: String,
    val awayClubColorHex: String = "#1B4F72",
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val homePenalties: Int? = null,
    val awayPenalties: Int? = null,
    val isExtraTimePlayed: Boolean = false,
    val winnerClubId: String? = null,
    val isUserClubMatch: Boolean = false,
    val dayIndex: Int,
    val played: Boolean = false,
    val stadiumName: String = "Atatürk Olimpiyat Stadyumu"
) {
    val scoreDisplay: String
        get() = if (!played) "- : -"
        else if (homePenalties != null && awayPenalties != null) {
            "$homeScore ($homePenalties) - $awayScore ($awayPenalties) Pen."
        } else if (isExtraTimePlayed) {
            "$homeScore - $awayScore (Uzt.)"
        } else {
            "$homeScore - $awayScore"
        }
}

/**
 * Avrupa Kupaları Puan Tablosu Satırı
 */
@JsonClass(generateAdapter = true)
data class EuropeanGroupStanding(
    val clubId: String,
    val clubName: String,
    val country: String,
    val played: Int = 0,
    val won: Int = 0,
    val drawn: Int = 0,
    val lost: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val points: Int = 0,
    val isUserClub: Boolean = false
) {
    val goalDifference: Int get() = goalsFor - goalsAgainst
}

/**
 * A Millî Takım Davet Aşaması Durumu
 */
enum class CallUpStatus(val titleTr: String) {
    INVITED("Resmi Davet Gönderildi"),
    IN_CAMP("Riva Millî Takım Kampında"),
    MATCH_DAY("Maç Günü Kadrosu"),
    COMPLETED("Millî Görev Başarıyla Tamamlandı")
}

/**
 * Millî Takım Kadrosu Üyesi
 */
@JsonClass(generateAdapter = true)
data class NationalTeamPlayer(
    val id: String,
    val name: String,
    val position: Position,
    val kitNumber: Int,
    val clubName: String,
    val overall: Int,
    val isCaptain: Boolean = false,
    val isUser: Boolean = false,
    val formStatus: String = "Yüksek Form"
)

/**
 * Millî Takım Kampı İçi Karakter Diyaloğu
 */
@JsonClass(generateAdapter = true)
data class CampDialogueItem(
    val speakerName: String,
    val speakerRole: String,
    val avatarColorHex: String,
    val quoteText: String,
    val responsePrompt: String,
    val tacticalAdvice: String
)

/**
 * Millî Maç Fikstürü
 */
@JsonClass(generateAdapter = true)
data class NationalMatchFixture(
    val id: String,
    val tournamentType: TournamentType = TournamentType.NATIONAL_WORLD_CUP_QUALIFIERS,
    val competitionName: String = "FIFA 2026 Dünya Kupası Avrupa Elemeleri",
    val opponentCountry: String,
    val opponentFlagEmoji: String,
    val opponentStrength: Int = 85,
    val stadiumName: String = "Atatürk Olimpiyat Stadyumu",
    val stadiumCity: String = "İstanbul",
    val dayIndex: Int,
    val dateLabel: String,
    val kickOffTime: String = "21:45",
    val isHome: Boolean = true,
    val turkeyScore: Int = 0,
    val opponentScore: Int = 0,
    val played: Boolean = false,
    val summaryText: String = ""
) {
    val fixtureTitle: String get() = if (isHome) "Türkiye 🇹🇷 vs $opponentFlagEmoji $opponentCountry" else "$opponentFlagEmoji $opponentCountry vs 🇹🇷 Türkiye"
}

/**
 * A Millî Takım Resmi Davet Mektubu ve Kamp Verisi
 */
@JsonClass(generateAdapter = true)
data class NationalTeamCallUp(
    val id: String,
    val seasonYear: Int = 2026,
    val callUpDayIndex: Int,
    val officialDate: String,
    val headCoachName: String = "Vincenzo Montella",
    val assistantCoachName: String = "Selçuk Şahin & Daniele Daniele",
    val tffPresidentName: String = "İbrahim Hacıosmanoğlu",
    val invitationTitle: String = "TÜRKİYE FUTBOL FEDERASYONU - A MİLLÎ TAKIM ADAY KADRO RESMİ DAVETİ",
    val invitationBody: String,
    val invitationReason: String,
    val assignedKitNumber: Int = 10,
    val status: CallUpStatus = CallUpStatus.INVITED,
    val campLocation: String = "TFF Hasan Doğan Millî Takımlar Kamp ve Eğitim Tesisleri (Riva / Beykoz)",
    val squad: List<NationalTeamPlayer> = emptyList(),
    val nextMatch: NationalMatchFixture,
    val campDialogues: List<CampDialogueItem> = emptyList(),
    val isAccepted: Boolean = false
)

/**
 * Millî Maç Sonrası Oyuncu Performans ve Manşet Raporu
 */
@JsonClass(generateAdapter = true)
data class NationalMatchPerformance(
    val capsEarned: Int = 1,
    val minutesPlayed: Int = 90,
    val goals: Int = 0,
    val assists: Int = 0,
    val shots: Int = 0,
    val rating: Double = 7.5,
    val userPerformanceDescription: String,
    val matchResultTitle: String,
    val finalScore: String,
    val matchBonusEur: Long = 25_000L,
    val famePointsGained: Int = 25,
    val fanReactionTr: String,
    val pressHeadlineTr: String,
    val coachPraiseTr: String,
    val isDebut: Boolean = false,
    val isFirstGoal: Boolean = false
)

/**
 * Oyuncunun Kariyer Turnuva ve Kupa Başarı Arşivi
 */
@JsonClass(generateAdapter = true)
data class TournamentCareerArchive(
    val turkishCupTitles: Int = 0,
    val superCupTitles: Int = 0,
    val championsLeagueTitles: Int = 0,
    val europaLeagueTitles: Int = 0,
    val conferenceLeagueTitles: Int = 0,
    val nationalCaps: Int = 0,
    val nationalGoals: Int = 0,
    val nationalAssists: Int = 0,
    val bestNationalCompetition: String = "A Millî Takım Aday Havuzu",
    val trophyHonorsList: List<String> = emptyList()
)

/**
 * Resmi Gerçek Turnuva Logo Drawable Eşleyicisi (Takvim & Fikstürler İçin)
 */
fun getTournamentDrawableRes(
    leagueId: String?,
    fixtureId: String? = null,
    dateLabel: String? = null
): Int? {
    val lid = leagueId.orEmpty().uppercase()
    val fid = fixtureId.orEmpty().uppercase()
    val dLabel = dateLabel.orEmpty().uppercase()

    return when {
        lid.contains("TR_CUP") || fid.contains("ZTK") || fid.contains("CUP_ZTK") || dLabel.contains("ZİRAAT") || dLabel.contains("ZIRAAT") || dLabel.contains("TÜRKİYE KUPASI") || dLabel.contains("TURKIYE KUPASI") -> {
            com.example.R.drawable.logo_ziraat_turkiye_kupasi
        }
        lid.contains("SUPER_CUP") || fid.contains("SUPER_KUPA") || dLabel.contains("SÜPER KUPA") || dLabel.contains("SUPER KUPA") -> {
            com.example.R.drawable.logo_super_kupa
        }
        lid.contains("UCL") || fid.contains("UCL") || dLabel.contains("CHAMPIONS") || dLabel.contains("ŞAMPİYONLAR") -> {
            com.example.R.drawable.logo_uefa_champions_league
        }
        lid.contains("UEL") || fid.contains("UEL") || dLabel.contains("EUROPA") || dLabel.contains("AVRUPA LİGİ") -> {
            com.example.R.drawable.logo_uefa_europa_league
        }
        lid.contains("UECL") || fid.contains("UECL") || dLabel.contains("CONFERENCE") || dLabel.contains("KONFERANS") -> {
            com.example.R.drawable.logo_uefa_conference_league
        }
        lid.startsWith("NAT_") || fid.startsWith("NAT_") || dLabel.contains("MİLLÎ") || dLabel.contains("MILLI") -> {
            com.example.R.drawable.logo_milli_takim
        }
        lid.contains("SUPER_LIG") || lid.contains("TSL") || dLabel.contains("SÜPER LİG") || dLabel.contains("SUPER LIG") -> {
            com.example.R.drawable.logo_super_lig
        }
        else -> null
    }
}

