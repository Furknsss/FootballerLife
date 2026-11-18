package com.example.engine.tournament

import com.example.model.CalendarEvent
import com.example.model.CareerSaveState
import com.example.model.Club
import com.example.model.FinancialTransaction
import com.example.model.GameEvent
import com.example.model.LeagueFixture
import com.example.model.NewsEvent
import com.example.model.PlayerProfile
import com.example.model.Position
import com.example.model.SocialMediaPost
import com.example.model.XPostItem
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Ziraat Türkiye Kupası, Süper Kupa, UEFA Avrupa Kupaları ve A Millî Takım Simülasyon Motoru
 */
object CupAndNationalTeamEngine {

    // =========================================================================
    // 1. ZİRAAT TÜRKİYE KUPASI & SÜPER KUPA ELEME SİSTEMİ
    // =========================================================================

    private val turkishCupRounds = listOf(
        Pair(TournamentStage.ROUND_OF_32, 65),   // Ekim / Kasım
        Pair(TournamentStage.ROUND_OF_16, 120),  // Ocak
        Pair(TournamentStage.QUARTER_FINAL, 175),// Mart
        Pair(TournamentStage.SEMI_FINAL, 215),   // Nisan
        Pair(TournamentStage.FINAL, 255)         // Mayıs
    )

    private val potentialCupOpponents = listOf(
        Triple("ZTK_GENCLER", "Gençlerbirliği", "#C0392B"),
        Triple("ZTK_SAKARYA", "Sakaryaspor", "#1E8449"),
        Triple("ZTK_KOCAELI", "Kocaelispor", "#145A32"),
        Triple("ZTK_BURSA", "Bursaspor", "#27AE60"),
        Triple("ZTK_ALTAY", "Altay", "#17202A"),
        Triple("ZTK_ANKARAGUCU", "MKE Ankaragücü", "#F39C12"),
        Triple("ZTK_SIVAS", "Sivasspor", "#C0392B"),
        Triple("ZTK_ANTALYA", "Antalyaspor", "#E74C3C"),
        Triple("ZTK_KONYA", "Konyaspor", "#196F3D"),
        Triple("ZTK_TRABZON", "Trabzonspor", "#78281F"),
        Triple("ZTK_BESIKTAS", "Beşiktaş JK", "#17202A"),
        Triple("ZTK_FENER", "Fenerbahçe", "#1B4F72"),
        Triple("ZTK_GALA", "Galatasaray", "#A93226")
    )

    /**
     * Sezon başında veya kupa başladığında Ziraat Türkiye Kupası fikstürlerini üretir
     */
    fun createTurkishCupFixtures(
        userClub: Club,
        allClubs: List<Club>,
        seasonYear: Int = 2026
    ): List<LeagueFixture> {
        val fixtures = mutableListOf<LeagueFixture>()

        turkishCupRounds.forEachIndexed { index, (stage, dayIdx) ->
            val opponent = potentialCupOpponents
                .filter { it.first != userClub.id && it.second != userClub.name }
                .shuffled()
                .firstOrNull() ?: Triple("ZTK_RAKIP", "Anadolu FK", "#34495E")

            val isHome = index % 2 == 0
            val homeClubId = if (isHome) userClub.id else opponent.first
            val homeClubName = if (isHome) userClub.name else opponent.second
            val awayClubId = if (isHome) opponent.first else userClub.id
            val awayClubName = if (isHome) opponent.second else userClub.name
            val stadium = if (isHome) userClub.stadiumName else if (stage == TournamentStage.FINAL) "Atatürk Olimpiyat Stadyumu" else "${opponent.second} Stadyumu"

            val fixture = LeagueFixture(
                id = "CUP_ZTK_${stage.name}_$dayIdx",
                leagueId = TournamentType.ZIRAAT_TURKIYE_KUPASI.id,
                week = index + 1,
                dayIndex = dayIdx,
                dateLabel = "Kupa: ${stage.stageNameTr}",
                kickOffTime = "20:30",
                homeClubId = homeClubId,
                homeClubName = homeClubName,
                awayClubId = awayClubId,
                awayClubName = awayClubName,
                stadiumName = stadium,
                played = false,
                homeScore = 0,
                awayScore = 0,
                isCupMatch = true,
                goalSummary = emptyList()
            )
            fixtures.add(fixture)
        }

        return fixtures
    }

    /**
     * Turkcell Süper Kupa Tek Maç Eleme Fikstürü
     */
    fun createSuperCupFixture(
        leagueChampion: Club,
        cupWinner: Club,
        seasonYear: Int = 2026,
        dayIndex: Int = 5
    ): LeagueFixture {
        return LeagueFixture(
            id = "CUP_SUPER_KUPA_${seasonYear}",
            leagueId = TournamentType.TURKCELL_SUPER_KUPA.id,
            week = 1,
            dayIndex = dayIndex,
            dateLabel = "Turkcell Süper Kupa Finali",
            kickOffTime = "21:00",
            homeClubId = leagueChampion.id,
            homeClubName = leagueChampion.name,
            awayClubId = cupWinner.id,
            awayClubName = cupWinner.name,
            stadiumName = "Atatürk Olimpiyat Stadyumu (İstanbul)",
            played = false,
            homeScore = 0,
            awayScore = 0,
            isCupMatch = true,
            goalSummary = emptyList()
        )
    }

    /**
     * Berabere biten kupa maçlarında uzatmalar ve penaltı atışlarını çözer
     */
    fun resolveCupMatchTieBreaker(
        homeScore: Int,
        awayScore: Int,
        isUserHome: Boolean,
        userClutchRating: Int
    ): Pair<Int, Int> {
        if (homeScore != awayScore) return Pair(homeScore, awayScore)

        // Penaltı atışları (Genelde 4-3, 5-4, 6-5 vb.)
        val userPenaltyAdvantage = (userClutchRating - 50) / 25
        val baseUserPen = 4 + if (Random.nextInt(100) < (60 + userPenaltyAdvantage * 10)) 1 else 0
        val baseOppPen = if (baseUserPen == 5) Random.nextInt(3, 5) else 5

        return if (isUserHome) Pair(baseUserPen, baseOppPen) else Pair(baseOppPen, baseUserPen)
    }

    // =========================================================================
    // 2. AVRUPA KUPALARI (UEFA ŞAMPİYONLAR LİGİ, AVRUPA LİGİ, KONFERANS LİGİ)
    // =========================================================================

    private val uclOpponents = listOf(
        Triple("ESP_RMA", "Real Madrid", "#F7FAF8"),
        Triple("ENG_MCI", "Manchester City", "#5DADE2"),
        Triple("GER_BAY", "Bayern München", "#922B21"),
        Triple("ESP_BAR", "FC Barcelona", "#1B4F72"),
        Triple("FRA_PSG", "Paris Saint-Germain", "#1B4F72"),
        Triple("ENG_ARS", "Arsenal", "#922B21"),
        Triple("ITA_INT", "Inter Milano", "#1B4F72"),
        Triple("GER_B04", "Bayer Leverkusen", "#922B21")
    )

    private val uelOpponents = listOf(
        Triple("ENG_MUN", "Manchester United", "#922B21"),
        Triple("ENG_TOT", "Tottenham Hotspur", "#F7FAF8"),
        Triple("ITA_ROM", "AS Roma", "#78281F"),
        Triple("POR_FCP", "FC Porto", "#1B4F72"),
        Triple("POR_SLB", "SL Benfica", "#922B21"),
        Triple("GER_BVB", "Borussia Dortmund", "#F4D03F"),
        Triple("ESP_ATH", "Athletic Club", "#922B21"),
        Triple("FRA_OL", "Olympique Lyonnais", "#F7FAF8")
    )

    private val ueclOpponents = listOf(
        Triple("ENG_CHE", "Chelsea", "#1B4F72"),
        Triple("ESP_BET", "Real Betis", "#1E8449"),
        Triple("ITA_FIO", "ACF Fiorentina", "#6C3483"),
        Triple("POR_BRA", "SC Braga", "#922B21"),
        Triple("FRA_NIC", "OGC Nice", "#922B21"),
        Triple("GER_SCF", "SC Freiburg", "#922B21")
    )

    private val europeanMatchDays = listOf(28, 56, 84, 112, 140, 168, 196, 224)

    /**
     * Lig sıralamasına veya kulübün statüsüne göre Avrupa fikstürleri üretir
     */
    fun createEuropeanFixtures(
        userClub: Club,
        leagueFinishRank: Int,
        seasonYear: Int = 2026
    ): List<LeagueFixture> {
        val tournamentType = when {
            leagueFinishRank in 1..2 -> TournamentType.UEFA_CHAMPIONS_LEAGUE
            leagueFinishRank == 3 -> TournamentType.UEFA_EUROPA_LEAGUE
            leagueFinishRank in 4..5 -> TournamentType.UEFA_CONFERENCE_LEAGUE
            userClub.name in listOf("Galatasaray", "Fenerbahçe") -> TournamentType.UEFA_CHAMPIONS_LEAGUE
            userClub.name in listOf("Beşiktaş JK", "Trabzonspor", "Başakşehir") -> TournamentType.UEFA_EUROPA_LEAGUE
            else -> return emptyList()
        }

        val opponentPool = when (tournamentType) {
            TournamentType.UEFA_CHAMPIONS_LEAGUE -> uclOpponents
            TournamentType.UEFA_EUROPA_LEAGUE -> uelOpponents
            TournamentType.UEFA_CONFERENCE_LEAGUE -> ueclOpponents
            else -> uclOpponents
        }

        val fixtures = mutableListOf<LeagueFixture>()
        val shuffledOpponents = opponentPool.shuffled()

        europeanMatchDays.forEachIndexed { idx, dayIdx ->
            val opponent = shuffledOpponents[idx % shuffledOpponents.size]
            val isHome = idx % 2 == 0
            val homeClubId = if (isHome) userClub.id else opponent.first
            val homeClubName = if (isHome) userClub.name else opponent.second
            val awayClubId = if (isHome) opponent.first else userClub.id
            val awayClubName = if (isHome) opponent.second else userClub.name
            val stadium = if (isHome) userClub.stadiumName else "${opponent.second} Stadium"

            val fixture = LeagueFixture(
                id = "EURO_${tournamentType.shortName}_W${idx + 1}_$dayIdx",
                leagueId = tournamentType.id,
                week = idx + 1,
                dayIndex = dayIdx,
                dateLabel = "${tournamentType.shortName} ${idx + 1}. Maç",
                kickOffTime = if (tournamentType == TournamentType.UEFA_CHAMPIONS_LEAGUE) "22:00" else "19:45",
                homeClubId = homeClubId,
                homeClubName = homeClubName,
                awayClubId = awayClubId,
                awayClubName = awayClubName,
                stadiumName = stadium,
                played = false,
                homeScore = 0,
                awayScore = 0,
                isCupMatch = true,
                goalSummary = emptyList()
            )
            fixtures.add(fixture)
        }

        return fixtures
    }

    // =========================================================================
    // 3. A MİLLÎ TAKIM SİSTEMİ (DAVET MEKTUBU, RİVA KAMPI, AY-YILDIZ MAÇ EKRANI)
    // =========================================================================

    private val nationalOpponents = listOf(
        Triple("Portekiz", "🇵🇹", 89),
        Triple("Hırvatistan", "🇭🇷", 86),
        Triple("Hollanda", "🇳🇱", 88),
        Triple("Almanya", "🇩🇪", 90),
        Triple("İspanya", "🇪🇸", 91),
        Triple("Galler", "🏴󠁧󠁢󠁷󠁬󠁳󠁿", 82),
        Triple("Çekya", "🇨🇿", 81),
        Triple("İtalya", "🇮🇹", 87)
    )

    /**
     * Oyuncunun genel reytingi, formu ve performansına göre A Millî Takım daveti uygunluğunu değerlendirir.
     */
    fun evaluateNationalTeamCallUp(
        player: PlayerProfile,
        currentDayIndex: Int,
        existingCallUp: NationalTeamCallUp? = null
    ): NationalTeamCallUp? {
        if (existingCallUp != null && existingCallUp.status != CallUpStatus.COMPLETED) {
            return existingCallUp
        }

        // Davet koşulları: OVR >= 72 VEYA form >= 76 VEYA 4+ gol/asist
        val isSkillfulEnough = player.overallRating >= 72 || (player.form >= 76 && (player.currentSeasonStats.goals + player.currentSeasonStats.assists >= 4))
        if (!isSkillfulEnough) return null

        val opponent = nationalOpponents.shuffled().first()
        val matchDayIndex = currentDayIndex + 4

        val letterBody = """
Sayın ${player.fullName},

Türkiye Futbol Federasyonu ve A Millî Futbol Takımımız Teknik Direktörü Vincenzo Montella adına;
Kulübünüz bünyesinde sergilediğiniz üstün profesyonellik, sahadaki yüksek formunuz ve ${player.primaryPosition.titleTr} pozisyonundaki liderlik potansiyeliniz teknik heyetimiz tarafından titizlikle takip edilmiştir.

Ay-Yıldızlı şanlı bayrağımızı uluslararası arenada temsil etmek üzere A Millî Takımımızın aday kadrosuna davet edilmiş bulunmaktasınız.

Milli kampa katılarak tüm milletimizin kalbinin sizinle atacağı bu kutsal görevi gururla yerine getireceğinize inancımız tamdır.

Vincenzo MONTELLA
A Millî Takım Teknik Direktörü

İbrahim HACIOSMANOĞLU
TFF Başkanı
        """.trimIndent()

        val squad = generateNationalSquad(player, player.kitNumber.coerceIn(1, 99))
        val dialogues = getCampDialogues(player)

        val nextMatch = NationalMatchFixture(
            id = "NAT_MATCH_${currentDayIndex}",
            tournamentType = TournamentType.NATIONAL_WORLD_CUP_QUALIFIERS,
            competitionName = "FIFA 2026 Dünya Kupası Avrupa Elemeleri",
            opponentCountry = opponent.first,
            opponentFlagEmoji = opponent.second,
            opponentStrength = opponent.third,
            stadiumName = "Atatürk Olimpiyat Stadyumu",
            stadiumCity = "İstanbul",
            dayIndex = matchDayIndex,
            dateLabel = "Millî Maç Günü",
            kickOffTime = "21:45",
            isHome = true
        )

        return NationalTeamCallUp(
            id = "CALL_UP_${currentDayIndex}",
            seasonYear = 2026,
            callUpDayIndex = currentDayIndex,
            officialDate = "Resmi Tebliğ",
            invitationTitle = "TÜRKİYE FUTBOL FEDERASYONU - A MİLLÎ TAKIM ADAY KADRO RESMİ DAVETİ",
            invitationBody = letterBody,
            invitationReason = "Süper Lig'deki ${player.overallRating} OVR ve ${player.form} form performansı",
            assignedKitNumber = if (player.kitNumber in 1..26) player.kitNumber else 10,
            status = CallUpStatus.INVITED,
            campLocation = "TFF Hasan Doğan Millî Takımlar Kamp ve Eğitim Tesisleri (Riva)",
            squad = squad,
            nextMatch = nextMatch,
            campDialogues = dialogues,
            isAccepted = false
        )
    }

    /**
     * A Millî Takım 23 kişilik resmi kadrosunu üretir
     */
    fun generateNationalSquad(userPlayer: PlayerProfile, userKitNumber: Int): List<NationalTeamPlayer> {
        val userNationalPlayer = NationalTeamPlayer(
            id = "NAT_USER",
            name = userPlayer.fullName,
            position = userPlayer.primaryPosition,
            kitNumber = userKitNumber,
            clubName = "Kulübün",
            overall = userPlayer.overallRating,
            isCaptain = false,
            isUser = true,
            formStatus = "Mükemmel (A Millîye Hazır)"
        )

        val realStars = listOf(
            NationalTeamPlayer("NAT_HAKAN", "Hakan Çalhanoğlu", Position.CM, 10, "Inter Milano", 85, isCaptain = true),
            NationalTeamPlayer("NAT_ARDA", "Arda Güler", Position.AM, 8, "Real Madrid", 83),
            NationalTeamPlayer("NAT_KENAN", "Kenan Yıldız", Position.WG, 19, "Juventus", 82),
            NationalTeamPlayer("NAT_FERDI", "Ferdi Kadıoğlu", Position.FB, 20, "Brighton & Hove Albion", 81),
            NationalTeamPlayer("NAT_KEREM", "Kerem Aktürkoğlu", Position.WG, 7, "SL Benfica", 82),
            NationalTeamPlayer("NAT_BARIS", "Barış Alper Yılmaz", Position.WG, 21, "Galatasaray", 82),
            NationalTeamPlayer("NAT_ORKUN", "Orkun Kökçü", Position.CM, 6, "SL Benfica", 81),
            NationalTeamPlayer("NAT_MERIH", "Merih Demiral", Position.CB, 3, "Al-Ahli", 79),
            NationalTeamPlayer("NAT_ABDULKERIM", "Abdülkerim Bardakcı", Position.CB, 14, "Galatasaray", 80),
            NationalTeamPlayer("NAT_UGURCAN", "Uğurcan Çakır", Position.GK, 1, "Trabzonspor", 79),
            NationalTeamPlayer("NAT_SEMIH", "Semih Kılıçsoy", Position.ST, 9, "Beşiktaş JK", 78),
            NationalTeamPlayer("NAT_ISMAIL", "İsmail Yüksek", Position.DM, 16, "Fenerbahçe", 78),
            NationalTeamPlayer("NAT_KAAN", "Kaan Ayhan", Position.CB, 22, "Galatasaray", 78),
            NationalTeamPlayer("NAT_YUNUS", "Yunus Akgün", Position.WG, 17, "Galatasaray", 79),
            NationalTeamPlayer("NAT_MERT", "Mert Günok", Position.GK, 12, "Beşiktaş JK", 78)
        )

        val squad = mutableListOf<NationalTeamPlayer>()
        squad.add(userNationalPlayer)
        realStars.forEach { star ->
            if (star.kitNumber != userKitNumber) {
                squad.add(star)
            } else {
                squad.add(star.copy(kitNumber = if (star.kitNumber == 10) 18 else (star.kitNumber + 11) % 26 + 1))
            }
        }
        return squad
    }

    /**
     * Riva Hasan Doğan Tesislerindeki kamp diyaloglarını üretir
     */
    fun getCampDialogues(userPlayer: PlayerProfile): List<CampDialogueItem> {
        return listOf(
            CampDialogueItem(
                speakerName = "Vincenzo Montella",
                speakerRole = "A Millî Takım Teknik Direktörü",
                avatarColorHex = "#C0392B",
                quoteText = "${userPlayer.firstName}, seni uzun zamandır analiz ediyoruz. Sahadaki dikine oyun vizyonun ve soğukkanlılığın tam da milli takımımızın aradığı dinamizm. Bugün formayı hak ettin, sahada özgür oyna ve milletini gururlandır!",
                responsePrompt = "Hocam bu güveniniz için minnettarım. Ay-Yıldız için kanımın son damlasına kadar savaşacağım!",
                tacticalAdvice = "3. bölgede üçüncü adam koşularını ara. Kenan ve Arda ile sürekli üçgenler kurarak rakip savunmayı dağıtın."
            ),
            CampDialogueItem(
                speakerName = "Hakan Çalhanoğlu",
                speakerRole = "A Millî Takım Kaptanı",
                avatarColorHex = "#1B4F72",
                quoteText = "Kardeşim, A Millî Takım ailesine hoş geldin! Bu formanın ağırlığı büyüktür ama yeteneğin de o kadar büyük. Sahada her zaman arkandayım, rahat ol ve topu istediğin an sana aktaracağız.",
                responsePrompt = "Kaptanım seninle aynı sahada olmak büyük onur. Liderliğin arkasında zafere gideceğiz!",
                tacticalAdvice = "Duran toplarda ve ceza sahası yayında bana alan aç, savunmayı üzerine çektiğinde araya kaç."
            ),
            CampDialogueItem(
                speakerName = "Arda Güler",
                speakerRole = "A Millî Takım Oyun Kurucusu",
                avatarColorHex = "#F39C12",
                quoteText = "Hoş geldin dostum! Seninle birlikte hücum hattında çok eğleneceğiz. Bana sadece göz teması ver, savunmanın arkasına koştuğun anda top ayağında!",
                responsePrompt = "Harika bir ikili olacağız Arda, paslarını bekliyorum!",
                tacticalAdvice = "Hızlı paslaşmalarla tek top oynayalım, savunma dengesini bozduğumuzda direkt kaleyi yokla."
            )
        )
    }

    /**
     * Millî Maç Simülasyonu ve Oyuncu Performans Çözümlemesi
     */
    fun resolveNationalMatch(
        callUp: NationalTeamCallUp,
        userPlayer: PlayerProfile,
        userSelectedTactics: String = "Hücum ve Yüksek Baskı",
        boostMultiplier: Double = 1.0
    ): Pair<NationalMatchPerformance, NationalMatchFixture> {
        val opponentStrength = callUp.nextMatch.opponentStrength
        val userOverall = userPlayer.overallRating
        val userShooting = userPlayer.attributes.shooting
        val userVision = userPlayer.attributes.vision

        // Gol & Asist hesaplama
        val goalChance = ((userShooting * 0.4 + userOverall * 0.3 + userPlayer.form * 0.3) * boostMultiplier).roundToInt()
        val userGoals = when {
            goalChance > 82 && Random.nextInt(100) < 65 -> 2
            goalChance > 68 && Random.nextInt(100) < 55 -> 1
            Random.nextInt(100) < 30 -> 1
            else -> 0
        }

        val assistChance = ((userVision * 0.4 + userOverall * 0.3 + 20) * boostMultiplier).roundToInt()
        val userAssists = when {
            assistChance > 75 && Random.nextInt(100) < 50 -> 2
            assistChance > 60 && Random.nextInt(100) < 45 -> 1
            else -> 0
        }

        // Türkiye Skoru ve Rakip Skoru
        val turkeyBaseGoals = userGoals + Random.nextInt(1, 3)
        val opponentGoals = when {
            opponentStrength >= 88 -> Random.nextInt(1, 3)
            opponentStrength >= 84 -> Random.nextInt(0, 3)
            else -> Random.nextInt(0, 2)
        }

        val ratingBase = 6.6 + (userGoals * 1.3) + (userAssists * 0.9) + if (turkeyBaseGoals > opponentGoals) 0.5 else 0.0
        val finalRating = (ratingBase + Random.nextDouble(-0.3, 0.4)).coerceIn(5.5, 9.9)
        val roundedRating = (finalRating * 10.0).roundToInt() / 10.0

        val matchWon = turkeyBaseGoals > opponentGoals
        val matchResultTitle = when {
            matchWon -> "ZAFER BİZİM! Ay-Yıldızlılar Sahadan Galip Ayrıldı!"
            turkeyBaseGoals == opponentGoals -> "Kıran Kırana Mücadele! Millilerimiz 1 Puanı Kurtardı!"
            else -> "Mücadele Ettik Fakat Sonuç Gelmedi"
        }

        val headline = when {
            userGoals >= 2 -> "DESTAN YAZDI! ${userPlayer.fullName} A Millî Formayla Şov Yaptı!"
            userGoals == 1 && matchWon -> "MİLLİ COŞKU! ${userPlayer.fullName} Attı, Türkiye Kazandı!"
            userAssists >= 1 -> "ASİSTLERİN EFENDİSİ! ${userPlayer.fullName} Millî Takımı Sırtladı!"
            else -> "Ay-Yıldızlı Formayla İlk Gurur! ${userPlayer.fullName} Sahada Terinin Son Damlasına Kadar Savaştı!"
        }

        val fanReaction = if (matchWon) {
            "Tüm Türkiye sokaklara döküldü! 'Kırmızı-Beyaz En Büyük Türkiye!' tezahüratları her yerde yankılanıyor!"
        } else {
            "Taraftarlar takımı alkışlarla uğurladı: 'Bu mücadeleyle Dünya Kupası bileti bizim olacak!'"
        }

        val coachPraise = "Montella: '${userPlayer.fullName} bugün kalbiyle oynadı. Takımımıza kattığı enerji ve zeka muazzamdı.'"

        val performance = NationalMatchPerformance(
            capsEarned = 1,
            minutesPlayed = 90,
            goals = userGoals,
            assists = userAssists,
            shots = userGoals + Random.nextInt(1, 4),
            rating = roundedRating,
            userPerformanceDescription = "Ay-Yıldızlı formayla ${callUp.nextMatch.opponentCountry} karşısında $userGoals gol, $userAssists asist ve $roundedRating maç puanı ile oynadı.",
            matchResultTitle = matchResultTitle,
            finalScore = "Türkiye $turkeyBaseGoals - $opponentGoals ${callUp.nextMatch.opponentCountry}",
            matchBonusEur = if (matchWon) 35_000L else 20_000L,
            famePointsGained = if (matchWon) 30 else 18,
            fanReactionTr = fanReaction,
            pressHeadlineTr = headline,
            coachPraiseTr = coachPraise,
            isDebut = true,
            isFirstGoal = userGoals > 0
        )

        val completedFixture = callUp.nextMatch.copy(
            turkeyScore = turkeyBaseGoals,
            opponentScore = opponentGoals,
            played = true,
            summaryText = "$headline (Maç Skoru: $turkeyBaseGoals-$opponentGoals)"
        )

        return Pair(performance, completedFixture)
    }

    /**
     * Millî maç sonucunu oyuncunun genel kariyer durumuna (bakiye, şöhret, haberler, sosyal medya) uygular
     */
    fun applyNationalMatchToCareer(
        career: CareerSaveState,
        performance: NationalMatchPerformance,
        callUp: NationalTeamCallUp
    ): Pair<CareerSaveState, String> {
        val updatedBalance = career.balanceEur + performance.matchBonusEur
        val updatedFanSupport = min(100, career.player.fanSupport + performance.famePointsGained)
        val updatedMorale = min(100, career.player.morale + 15)
        val updatedForm = min(100, career.player.form + 12)

        val updatedPlayer = career.player.copy(
            fanSupport = updatedFanSupport,
            morale = updatedMorale,
            form = updatedForm
        )

        val transaction = FinancialTransaction(
            id = "TX_NAT_BONUS_${career.currentDayIndex}",
            dayIndex = career.currentDayIndex,
            dateLabel = career.currentDayIndex.toString(),
            category = "MAÇ_PRİMİ",
            amountEur = performance.matchBonusEur,
            description = "A Millî Takım Maç Primi & Başarı Teşviki",
            balanceAfterEur = updatedBalance
        )

        val newsItem = NewsEvent(
            id = "NEWS_NAT_${career.currentDayIndex}",
            dayIndex = career.currentDayIndex,
            dateLabel = "Millî Manşet",
            category = "A_MILLI_TAKIM",
            headline = performance.pressHeadlineTr,
            body = "${career.player.fullName}, A Millî Takım formasıyla ${callUp.nextMatch.opponentCountry} karşısında parladı. ${performance.userPerformanceDescription}",
            relatedClubName = "Türkiye A Millî",
            sourceGameEventId = "",
            fanReaction = "Büyük Gurur ve Coşku"
        )

        val xPost = XPostItem(
            id = "X_NAT_${career.currentDayIndex}",
            dayIndex = career.currentDayIndex,
            dateLabel = "Az önce",
            authorName = "A Millî Takım 🇹🇷",
            handle = "@MilliTakimlar",
            category = "KULÜP",
            isVerified = true,
            text = "Bizim Çocuklar sahada tarih yazıyor! ${career.player.fullName} Ay-Yıldızlı formamıza gururla hizmet etti. Tebrikler! 🇹🇷❤️ #BizimÇocuklar #MilliGurur",
            likes = 84_500,
            reposts = 14_200,
            likedByUser = false,
            repostedByUser = false
        )

        val updatedTransactions = career.transactions + transaction
        val updatedNews = listOf(newsItem) + career.newsEvents
        val updatedXPosts = listOf(xPost) + career.xPosts

        val updatedState = career.copy(
            balanceEur = updatedBalance,
            player = updatedPlayer,
            transactions = updatedTransactions,
            newsEvents = updatedNews,
            xPosts = updatedXPosts
        )

        val feedbackMsg = "🇹🇷 Millî Görev Başarıyla Tamamlandı! +${performance.famePointsGained} Şöhret, +€${performance.matchBonusEur} Prim Hesabına Yattı!"
        return Pair(updatedState, feedbackMsg)
    }
}
