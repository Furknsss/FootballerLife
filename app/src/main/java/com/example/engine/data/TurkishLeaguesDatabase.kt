package com.example.engine.data

import com.example.engine.LeagueDefinition
import com.example.model.Club

object TurkishLeaguesDatabase {

    val turkishLeagues: List<LeagueDefinition> = listOf(
        LeagueDefinition(
            id = SuperLigData.LEAGUE_ID,
            name = SuperLigData.LEAGUE_NAME,
            country = "Türkiye",
            tier = 1,
            description = "Türkiye'nin en üst düzey profesyonel futbol ligi. 19 seçkin kulüp, Avrupa kupaları ve yıldız futbolcular.",
            isAmateurOrLowerTier = false,
            dataLabel = "Trendyol Süper Lig Resmi Kataloğu (2025/26)"
        ),
        LeagueDefinition(
            id = TFF1LigData.LEAGUE_ID,
            name = TFF1LigData.LEAGUE_NAME,
            country = "Türkiye",
            tier = 2,
            description = "Süper Lig kapısındaki 20 iddialı kulübün kıyasıya şampiyonluk ve play-off savaşı.",
            isAmateurOrLowerTier = false,
            dataLabel = "Trendyol 1. Lig Resmi Kataloğu (2025/26)"
        ),
        LeagueDefinition(
            id = TFF2LigData.LEAGUE_ID_ALL,
            name = TFF2LigData.LEAGUE_NAME_ALL,
            country = "Türkiye",
            tier = 3,
            description = "Beyaz ve Kırmızı Grup'ta mücadele eden köklü şehir takımları ve ateşli deplasman atmosferleri.",
            isAmateurOrLowerTier = false,
            dataLabel = "Nesine 2. Lig Tüm Gruplar Kataloğu (2025/26)"
        ),
        LeagueDefinition(
            id = TFF2LigData.LEAGUE_ID_BEYAZ,
            name = TFF2LigData.LEAGUE_NAME_BEYAZ,
            country = "Türkiye",
            tier = 3,
            description = "Batman Petrolspor, Sarıyer, Kastamonuspor, Altınordu ve Altay gibi devlerin yer aldığı 18 takımlı grup.",
            isAmateurOrLowerTier = false,
            dataLabel = "Nesine 2. Lig Beyaz Grup (2025/26)"
        ),
        LeagueDefinition(
            id = TFF2LigData.LEAGUE_ID_KIRMIZI,
            name = TFF2LigData.LEAGUE_NAME_KIRMIZI,
            country = "Türkiye",
            tier = 3,
            description = "Serik Bld, Aksarayspor, Vanspor, Elazığspor ve Bucaspor'un şampiyonluk yarışı verdiği 18 takımlı grup.",
            isAmateurOrLowerTier = false,
            dataLabel = "Nesine 2. Lig Kırmızı Grup (2025/26)"
        ),
        LeagueDefinition(
            id = TFF3LigData.LEAGUE_ID_ALL,
            name = TFF3LigData.LEAGUE_NAME_ALL,
            country = "Türkiye",
            tier = 4,
            description = "Profesyonel futbolun ilk basamağı. Bursaspor ve Karşıyaka gibi devlerle genç yeteneklerin vitrine çıktığı sert lig.",
            isAmateurOrLowerTier = true,
            dataLabel = "Nesine 3. Lig Tüm Gruplar Kataloğu (2025/26)"
        ),
        LeagueDefinition(
            id = TFF3LigData.LEAGUE_ID_G1,
            name = TFF3LigData.LEAGUE_NAME_G1,
            country = "Türkiye",
            tier = 4,
            description = "Bursaspor ve Karşıyaka derbisiyle tarihi Süper Lig şampiyonunun yer aldığı 1. Grup.",
            isAmateurOrLowerTier = true,
            dataLabel = "Nesine 3. Lig 1. Grup (2025/26)"
        ),
        LeagueDefinition(
            id = TFF3LigData.LEAGUE_ID_G2,
            name = TFF3LigData.LEAGUE_NAME_G2,
            country = "Türkiye",
            tier = 4,
            description = "Balıkesirspor, Uşakspor ve Etimesgut gibi köklü camiaların yer aldığı 2. Grup.",
            isAmateurOrLowerTier = true,
            dataLabel = "Nesine 3. Lig 2. Grup (2025/26)"
        ),
        LeagueDefinition(
            id = TFF3LigData.LEAGUE_ID_G3,
            name = TFF3LigData.LEAGUE_NAME_G3,
            country = "Türkiye",
            tier = 4,
            description = "Küçükçekmece Sinop, Ayvalıkgücü, 52 Orduspor ve Çorluspor'un mücadele ettiği 3. Grup.",
            isAmateurOrLowerTier = true,
            dataLabel = "Nesine 3. Lig 3. Grup (2025/26)"
        ),
        LeagueDefinition(
            id = TFF3LigData.LEAGUE_ID_G4,
            name = TFF3LigData.LEAGUE_NAME_G4,
            country = "Türkiye",
            tier = 4,
            description = "Orduspor 1967, Sebat Gençlik, Zonguldak Kömürspor ve Maraş İstiklal'in yer aldığı 4. Grup.",
            isAmateurOrLowerTier = true,
            dataLabel = "Nesine 3. Lig 4. Grup (2025/26)"
        ),
        LeagueDefinition(
            id = AmateurLeaguesData.LEAGUE_ID_BAL,
            name = AmateurLeaguesData.LEAGUE_NAME_BAL,
            country = "Türkiye",
            tier = 5,
            description = "Eskişehirspor, Gebzespor, Diyarbakırspor gibi efsanelerle Anadolu ve metropollerin profesyonel lige yükselme kapısı.",
            isAmateurOrLowerTier = true,
            dataLabel = "Bölgesel Amatör Lig (BAL) Seçkin Takımlar Kataloğu (2025/26)"
        ),
        LeagueDefinition(
            id = AmateurLeaguesData.LEAGUE_ID_AMATOR,
            name = AmateurLeaguesData.LEAGUE_NAME_AMATOR,
            country = "Türkiye",
            tier = 6,
            description = "Vefa (1908), Feriköy (1927), Alibeyköy (1936), İzmirspor (1923), Bakırköyspor gibi tarihi çınarlarla sıfırdan zirveye kariyer başlangıcı.",
            isAmateurOrLowerTier = true,
            dataLabel = "Süper Amatör Lig (SAL) Tarihi Kulüpler Kataloğu (2025/26)"
        )
    )

    // Lazy memory cache for fast indexing
    private val allClubsList: List<Club> by lazy {
        SuperLigData.getClubs() +
                TFF1LigData.getClubs() +
                TFF2LigData.getAllClubs() +
                TFF3LigData.getAllClubs() +
                AmateurLeaguesData.getBalClubs() +
                AmateurLeaguesData.getAmatorClubs()
    }

    private val clubsByIdMap: Map<String, Club> by lazy {
        allClubsList.associateBy { it.id }
    }

    fun getAllTurkishClubs(): List<Club> = allClubsList

    fun getClubsForLeague(leagueId: String): List<Club> = when (leagueId) {
        SuperLigData.LEAGUE_ID -> SuperLigData.getClubs()
        TFF1LigData.LEAGUE_ID -> TFF1LigData.getClubs()
        TFF2LigData.LEAGUE_ID_ALL -> TFF2LigData.getAllClubs()
        TFF2LigData.LEAGUE_ID_BEYAZ -> TFF2LigData.getBeyazClubs()
        TFF2LigData.LEAGUE_ID_KIRMIZI -> TFF2LigData.getKirmiziClubs()
        TFF3LigData.LEAGUE_ID_ALL -> TFF3LigData.getAllClubs()
        TFF3LigData.LEAGUE_ID_G1 -> TFF3LigData.getGroup1Clubs()
        TFF3LigData.LEAGUE_ID_G2 -> TFF3LigData.getGroup2Clubs()
        TFF3LigData.LEAGUE_ID_G3 -> TFF3LigData.getGroup3Clubs()
        TFF3LigData.LEAGUE_ID_G4 -> TFF3LigData.getGroup4Clubs()
        AmateurLeaguesData.LEAGUE_ID_BAL -> AmateurLeaguesData.getBalClubs()
        AmateurLeaguesData.LEAGUE_ID_AMATOR -> AmateurLeaguesData.getAmatorClubs()
        else -> emptyList()
    }

    fun findClubById(clubId: String): Club? = clubsByIdMap[clubId]

    fun getClubsByTier(tier: Int): List<Club> = allClubsList.filter { it.tier == tier }

    fun getLeagueById(leagueId: String): LeagueDefinition? =
        turkishLeagues.find { it.id == leagueId }

    fun searchClubs(
        query: String,
        leagueId: String? = null,
        tier: Int? = null
    ): List<Club> {
        val cleanQuery = query.trim().lowercase()
        var stream = allClubsList
        if (!leagueId.isNullOrBlank()) {
            val leagueClubs = getClubsForLeague(leagueId)
            stream = if (leagueClubs.isNotEmpty()) leagueClubs else stream.filter { it.leagueId == leagueId }
        }
        if (tier != null) {
            stream = stream.filter { it.tier == tier }
        }
        if (cleanQuery.isEmpty()) {
            return stream
        }
        return stream.filter { club ->
            club.name.lowercase().contains(cleanQuery) ||
                    club.shortName.lowercase().contains(cleanQuery) ||
                    club.city.lowercase().contains(cleanQuery) ||
                    club.stadiumName.lowercase().contains(cleanQuery) ||
                    club.managerName.lowercase().contains(cleanQuery)
        }
    }

    fun getLeagueReputation(leagueId: String): Int = when (leagueId) {
        SuperLigData.LEAGUE_ID -> 82
        TFF1LigData.LEAGUE_ID -> 68
        TFF2LigData.LEAGUE_ID_ALL, TFF2LigData.LEAGUE_ID_BEYAZ, TFF2LigData.LEAGUE_ID_KIRMIZI -> 56
        TFF3LigData.LEAGUE_ID_ALL, TFF3LigData.LEAGUE_ID_G1, TFF3LigData.LEAGUE_ID_G2, TFF3LigData.LEAGUE_ID_G3, TFF3LigData.LEAGUE_ID_G4 -> 45
        AmateurLeaguesData.LEAGUE_ID_BAL -> 32
        AmateurLeaguesData.LEAGUE_ID_AMATOR -> 20
        else -> 50
    }

    fun getTierTransferBudgetRange(tier: Int): LongRange = when (tier) {
        1 -> 6_000_000L..45_000_000L
        2 -> 1_200_000L..4_200_000L
        3 -> 750_000L..1_550_000L
        4 -> 420_000L..850_000L
        5 -> 130_000L..280_000L
        6 -> 60_000L..90_000L
        else -> 500_000L..2_000_000L
    }

    fun getTierWageBudgetRange(tier: Int): LongRange = when (tier) {
        1 -> 8_000L..95_000L    // Weekly wage range per player
        2 -> 2_500L..15_000L
        3 -> 800L..4_000L
        4 -> 300L..1_500L
        5 -> 100L..600L
        6 -> 50L..250L
        else -> 500L..5_000L
    }

    fun getTierOverallRange(tier: Int): IntRange = when (tier) {
        1 -> 65..85
        2 -> 58..72
        3 -> 50..64
        4 -> 44..56
        5 -> 35..48
        6 -> 35..46
        else -> 40..80
    }
}
