package com.example.engine

import com.example.model.Club
import com.example.model.DynamicCareerDilemma
import com.example.model.DynamicStoryChoice
import com.example.model.LifestylePurchaseItem
import com.example.model.LifestyleVenueAction
import com.example.model.PlayerAppearance
import com.example.model.PlayerAttributes
import com.example.model.PlayerProfile
import com.example.model.Position
import com.example.model.PreferredFoot
import com.example.model.SeasonStatsRecord
import com.example.model.SquadMemberInfo

data class LeagueDefinition(
    val id: String,
    val name: String,
    val country: String,
    val tier: Int,
    val description: String,
    val isAmateurOrLowerTier: Boolean,
    val dataLabel: String
)

data class RealPlayerPreset(
    val id: String,
    val firstName: String,
    val lastName: String,
    val birthDate: String,
    val age: Int,
    val nationality: String,
    val birthCity: String,
    val primaryPosition: Position,
    val secondaryPositions: List<Position>,
    val preferredFoot: PreferredFoot,
    val heightCm: Int,
    val weightKg: Int,
    val kitName: String,
    val kitNumber: Int,
    val defaultLeagueId: String,
    val defaultClubId: String,
    val currentClubDisplayName: String,
    val contractEndYear: Int,
    val weeklyWageEur: Long,
    val marketValueEur: Long,
    val potential: Int,
    val attributes: PlayerAttributes,
    val appearance: PlayerAppearance,
    val careerSummary: String,
    val verifiedSeasonSource: String,
    val archiveSeasons: List<SeasonStatsRecord>
)

data class TrainingDrillDefinition(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val energyCost: Int,
    val injuryRiskPct: Int,
    val primaryAttributeName: String,
    val secondaryAttributeName: String,
    val recommendedPositions: List<Position>
)

object WorldDataCatalog {

    val leagues: List<LeagueDefinition> = listOf(
        LeagueDefinition(
            id = "TR_BAL",
            name = "Türkiye Bölgesel Amatör Lig (BAL)",
            country = "Türkiye",
            tier = 5,
            description = "Anadolu ve metropol amatör kulüplerinden profesyonel liglere yükselme mücadelesi.",
            isAmateurOrLowerTier = true,
            dataLabel = "Bölgesel Amatör Lig Simülasyon Kataloğu (2026/27)"
        ),
        LeagueDefinition(
            id = "TR_IL_AMATOR",
            name = "İstanbul Süper Amatör Ligi",
            country = "Türkiye",
            tier = 6,
            description = "Mahalle ve ilçe kulüplerinden başlayan en gerçekçi sıfırdan yükseliş hikayesi.",
            isAmateurOrLowerTier = true,
            dataLabel = "Yerel Amatör Küme Simülasyon Kataloğu (2026/27)"
        ),
        LeagueDefinition(
            id = "TR_3_LIG",
            name = "Türkiye 3. Lig",
            country = "Türkiye",
            tier = 4,
            description = "Profesyonel futbolun ilk basamağı. Genç yeteneklerin vitrine çıktığı sert lig.",
            isAmateurOrLowerTier = true,
            dataLabel = "TFF 3. Lig Yapılandırılmış Simülasyon (2026/27)"
        ),
        LeagueDefinition(
            id = "TR_2_LIG",
            name = "Türkiye 2. Lig",
            country = "Türkiye",
            tier = 3,
            description = "Köklü şehir takımları ve ateşli deplasman atmosferleri.",
            isAmateurOrLowerTier = false,
            dataLabel = "TFF 2. Lig Yapılandırılmış Simülasyon (2026/27)"
        ),
        LeagueDefinition(
            id = "TR_1_LIG",
            name = "Türkiye 1. Lig",
            country = "Türkiye",
            tier = 2,
            description = "Süper Lig kapısındaki yüksek tempolu şampiyonluk yarışı.",
            isAmateurOrLowerTier = false,
            dataLabel = "TFF 1. Lig Yapılandırılmış Simülasyon (2026/27)"
        ),
        LeagueDefinition(
            id = "TR_SUPER_LIG",
            name = "Türkiye Süper Lig",
            country = "Türkiye",
            tier = 1,
            description = "Türkiye'nin en üst düzey profesyonel futbol ligi.",
            isAmateurOrLowerTier = false,
            dataLabel = "Süper Lig Kulüp & Fikstür Simülasyonu (2026/27)"
        ),
        LeagueDefinition(
            id = "ENG_L1",
            name = "İngiltere Premier Division",
            country = "İngiltere",
            tier = 1,
            description = "Dünyanın en yüksek tempolu ve en yüksek bütçeli futbol ligi.",
            isAmateurOrLowerTier = false,
            dataLabel = "Avrupa Elit Lig Kataloğu (2026/27)"
        ),
        LeagueDefinition(
            id = "ESP_L1",
            name = "İspanya Primera División",
            country = "İspanya",
            tier = 1,
            description = "Teknik kapasite, pas oyunu ve taktiksel ustalığın zirvesi.",
            isAmateurOrLowerTier = false,
            dataLabel = "Avrupa Elit Lig Kataloğu (2026/27)"
        ),
        LeagueDefinition(
            id = "GER_L1",
            name = "Almanya Bundesliga",
            country = "Almanya",
            tier = 1,
            description = "Dolu tribünler, yüksek pres ve genç yıldız gelişimi.",
            isAmateurOrLowerTier = false,
            dataLabel = "Avrupa Elit Lig Kataloğu (2026/27)"
        ),
        LeagueDefinition(
            id = "ITA_L1",
            name = "İtalya Serie A",
            country = "İtalya",
            tier = 1,
            description = "Taktiksel disiplin, savunma sanatı ve tutkulu derbiler.",
            isAmateurOrLowerTier = false,
            dataLabel = "Avrupa Elit Lig Kataloğu (2026/27)"
        ),
        LeagueDefinition(
            id = "FRA_L1",
            name = "Fransa Ligue 1",
            country = "Fransa",
            tier = 1,
            description = "Atletizm, hız ve dinamik hücum hattı yıldızları.",
            isAmateurOrLowerTier = false,
            dataLabel = "Avrupa Elit Lig Kataloğu (2026/27)"
        ),
        LeagueDefinition(
            id = "POR_L1",
            name = "Portekiz Primeira Liga",
            country = "Portekiz",
            tier = 1,
            description = "Avrupa devlerine sıçrama tahtası olan teknik yetenek ligi.",
            isAmateurOrLowerTier = false,
            dataLabel = "Avrupa Elit Lig Kataloğu (2026/27)"
        )
    )

    fun clubsForLeague(leagueId: String): List<Club> {
        val rawClubs = when (leagueId) {
            "TR_IL_AMATOR" -> listOf(
                club("IL_KADIKOY", "Kadıköy İdman Yurdu", "KİY", "Türkiye", "İstanbul", leagueId, "İstanbul Süper Amatör Ligi", 6, "#103E2D", "#F7FAF8", 65_000L, 1, 46, "Orhan Çelik", "4-3-3 Mücadeleci", "Fikirtepe Semt Sahası", 1500, true),
                club("IL_USKUDAR", "Üsküdar Çengelköy SK", "ÜSK", "Türkiye", "İstanbul", leagueId, "İstanbul Süper Amatör Ligi", 6, "#1B4F72", "#F7FAF8", 70_000L, 1, 47, "Ahmet Yaman", "4-4-2 Dengeli", "Beylerbeyi 75. Yıl Stadı", 2500, true),
                club("IL_BAKIRKOY", "Bakırköy Gençlik", "BKR", "Türkiye", "İstanbul", leagueId, "İstanbul Süper Amatör Ligi", 6, "#145A32", "#1C2833", 75_000L, 1, 48, "Serkan Doğan", "4-2-3-1 Hücum", "Bakırköy Belediye Stadı", 3000, true),
                club("IL_BESIKTAS_AM", "Ortaköy Boğaziçi SK", "ORT", "Türkiye", "İstanbul", leagueId, "İstanbul Süper Amatör Ligi", 6, "#B03A2E", "#F7FAF8", 60_000L, 1, 45, "Kemal Aksoy", "4-3-3 Kontra", "Çilekli Sahası", 1200, true),
                club("IL_ZEYTINBURNU", "Zeytinburnu Мерkez SK", "ZБN", "Türkiye", "İstanbul", leagueId, "İstanbul Süper Amatör Ligi", 6, "#1F618D", "#F4D03F", 68_000L, 1, 46, "Metin Kaya", "4-4-2 Sert Savunma", "Hasan Doğan Sahası", 1800, true),
                club("IL_MALTEPE", "Maltepe Sahilspor", "MLT", "Türkiye", "İstanbul", leagueId, "İstanbul Süper Amatör Ligi", 6, "#117A65", "#F7FAF8", 72_000L, 1, 47, "Tuncay Erdem", "4-2-3-1 Kanat", "Maltepe Hasan Polat", 2800, true),
                club("IL_KARTAL", "Kartal Anadolu SK", "KRT", "Türkiye", "İstanbul", leagueId, "İstanbul Süper Amatör Ligi", 6, "#78281F", "#F7FAF8", 74_000L, 2, 49, "Bülent Şahin", "4-3-3 Pres", "Bulvar Mimar Kubilay", 2200, true),
                club("IL_BEYOGLU", "Beyoğlu Haliç SK", "BEY", "Türkiye", "İstanbul", leagueId, "İstanbul Süper Amatör Ligi", 6, "#D4AC0D", "#17202A", 64_000L, 1, 45, "Hakan Özkan", "4-5-1 Kapalı", "Beyoğlu Stadı", 1600, true)
            )
            "TR_BAL" -> listOf(
                club("BAL_ESKISEHIR", "Eskişehir Anadolu SK", "ESK", "Türkiye", "Eskişehir", leagueId, "Türkiye Bölgesel Amatör Lig (BAL)", 5, "#B03A2E", "#17202A", 220_000L, 2, 55, "İbrahim Baş", "4-2-3-1 Hücum", "Yeni Eskişehir Stadyumu", 18000, false),
                club("BAL_GEBZE", "Gebze Körfez FK", "GBZ", "Türkiye", "Kocaeli", leagueId, "Türkiye Bölgesel Amatör Lig (BAL)", 5, "#5B2C6F", "#F7FAF8", 180_000L, 2, 53, "Cihan Erdil", "4-3-3 Dengeli", "Alaettin Kurt Stadı", 8500, false),
                club("BAL_IZMIR", "İzmir Bornova Yeşilova", "BRN", "Türkiye", "İzmir", leagueId, "Türkiye Bölgesel Amatör Lig (BAL)", 5, "#1E8449", "#F4D03F", 160_000L, 2, 52, "Serdar Sabuncu", "4-3-3 Pas Oyunu", "Bornova Yusuf Tırpancı", 4500, false),
                club("BAL_BURSA", "Nilüfer Belediye FK", "NİL", "Türkiye", "Bursa", leagueId, "Türkiye Bölgesel Amatör Lig (BAL)", 5, "#1F618D", "#2ECC71", 195_000L, 2, 54, "Pablo Martin", "4-2-3-1 Önde Baskı", "İbrahim Yazıcı Stadı", 6200, false),
                club("BAL_ANKARA", "Polatlı 1926 SK", "PLT", "Türkiye", "Ankara", leagueId, "Türkiye Bölgesel Amatör Lig (BAL)", 5, "#922B21", "#F7FAF8", 150_000L, 1, 51, "Erkan Sözeri", "4-4-2 Kontra", "Polatlı Şehir Stadı", 4000, false),
                club("BAL_TRABZON", "Akçaabat Sebat Gençlik", "SBT", "Türkiye", "Trabzon", leagueId, "Türkiye Bölgesel Amatör Lig (BAL)", 5, "#78281F", "#2874A6", 170_000L, 2, 53, "Adem Çak", "4-3-3 Hızlı Kanat", "Ortahisar Yavuz Selim", 5000, false),
                club("BAL_ADANA", "Ceyhan 1967 FK", "CEY", "Türkiye", "Adana", leagueId, "Türkiye Bölgesel Amatör Lig (BAL)", 5, "#145A32", "#F7FAF8", 145_000L, 1, 50, "Mustafa Alper", "4-4-2 Dirençli", "Ceyhan Şehir Stadı", 4200, false),
                club("BAL_EDIRNE", "Uzunköprü Trakya SK", "UZK", "Türkiye", "Edirne", leagueId, "Türkiye Bölgesel Amatör Lig (BAL)", 5, "#B7950B", "#1E8449", 140_000L, 1, 49, "Kadir Demir", "4-5-1 Savunma", "Ergene Sahası", 3100, false)
            )
            "TR_3_LIG" -> listOf(
                club("T3_BURSA", "Bursa Yeşil-Beyaz FK", "BRS", "Türkiye", "Bursa", leagueId, "Türkiye 3. Lig", 4, "#1E8449", "#F7FAF8", 650_000L, 3, 61, "Batalla Özlüce", "4-2-3-1 Baskılı Hücum", "Yüzüncü Yıl Atatürk Stadı", 43000, false),
                club("T3_KARSIYAKA", "Karşıyaka Körfez SK", "KSK", "Türkiye", "İzmir", leagueId, "Türkiye 3. Lig", 4, "#922B21", "#1E8449", 580_000L, 2, 60, "Mustafa Reşit", "4-3-3 Kanat", "Alsancak Stadyumu", 15000, false),
                club("T3_ORDUSPOR", "Ordu 1967 SK", "ORD", "Türkiye", "Ordu", leagueId, "Türkiye 3. Lig", 4, "#6C3483", "#F7FAF8", 480_000L, 2, 58, "Ramazan Çelik", "4-2-3-1 Dengeli", "Yeni Ordu Stadyumu", 16000, false),
                club("T3_DUZCE", "Düzce Cam Düzcespor", "DÜZ", "Türkiye", "Düzce", leagueId, "Türkiye 3. Lig", 4, "#922B21", "#1F618D", 430_000L, 2, 57, "Kadir Kar", "4-4-2 Fiziksel", "Düzce Şehir Stadı", 7500, false),
                club("T3_TOKAT", "Tokat Belediye Plevne", "TKT", "Türkiye", "Tokat", leagueId, "Türkiye 3. Lig", 4, "#78281F", "#F7FAF8", 390_000L, 2, 56, "Semih Tokatlı", "4-3-3 Kontra", "Gaziosmanpaşa Stadı", 6000, false),
                club("T3_MUGLA", "Muğlaspor", "MĞL", "Türkiye", "Muğla", leagueId, "Türkiye 3. Lig", 4, "#1E8449", "#F7FAF8", 410_000L, 2, 57, "Besim Durmuş", "4-2-3-1 Pas", "Muğla Atatürk Stadı", 6500, false),
                club("T3_SILIFKE", "Silifke Belediye SK", "SLF", "Türkiye", "Mersin", leagueId, "Türkiye 3. Lig", 4, "#1F618D", "#922B21", 370_000L, 2, 55, "İlker Avcıbay", "4-4-2 Dengeli", "Silifke Şehir Stadı", 5000, false),
                club("T3_HOPASPOR", "Artvin Hopaspor", "HOP", "Türkiye", "Artvin", leagueId, "Türkiye 3. Lig", 4, "#6C3483", "#F7FAF8", 360_000L, 2, 55, "Osman Özdemir", "4-5-1 Sert", "Hopa İlçe Stadı", 4000, false)
            )
            "TR_2_LIG" -> listOf(
                club("T2_ALTAY", "Altay İzmir SK", "ALT", "Türkiye", "İzmir", leagueId, "Türkiye 2. Lig", 3, "#17202A", "#F7FAF8", 1_100_000L, 3, 64, "Gökhan Karaaslan", "4-2-3-1 Genç Dinamik", "Alsancak Mustafa Denizli", 15000, false),
                club("T2_ELAZIG", "Elazığspor", "ELZ", "Türkiye", "Elazığ", leagueId, "Türkiye 2. Lig", 3, "#78281F", "#F7FAF8", 1_250_000L, 3, 65, "Bülent Yenihayat", "4-3-3 Baskılı", "Elazığ Atatürk Stadyumu", 18400, false),
                club("T2_BATMAN", "Batman Petrolspor", "BTM", "Türkiye", "Batman", leagueId, "Türkiye 2. Lig", 3, "#922B21", "#F7FAF8", 1_300_000L, 3, 65, "Şenol Fidan", "4-2-3-1 Hücum", "Batman Yeni Şehir Stadı", 15000, false),
                club("T2_VANSPR", "Vanspor FK", "VAN", "Türkiye", "Van", leagueId, "Türkiye 2. Lig", 3, "#922B21", "#17202A", 1_200_000L, 3, 64, "Tahsin Tam", "4-3-3 Kanat", "Van Atatürk Stadı", 10500, false),
                club("T2_KASTAMONU", "Kastamonuspor", "KST", "Türkiye", "Kastamonu", leagueId, "Türkiye 2. Lig", 3, "#922B21", "#17202A", 1_150_000L, 2, 64, "Adem Çağlayan", "4-2-3-1 Dengeli", "Gazi Stadı", 7000, false),
                club("T2_SARIYER", "Sarıyer SK", "SRY", "Türkiye", "İstanbul", leagueId, "Türkiye 2. Lig", 3, "#1B4F72", "#F7FAF8", 1_180_000L, 3, 64, "Mustafa Sarıgül", "4-3-3 Pas", "Yusuf Ziya Öniş", 6500, false),
                club("T2_ISKENDERUN", "İskenderunspor", "İSK", "Türkiye", "Hatay", leagueId, "Türkiye 2. Lig", 3, "#D35400", "#1F618D", 1_050_000L, 2, 63, "Gürkan Aslan", "4-2-3-1 Kontra", "Kırıkhan Stadı", 6000, false),
                club("T2_ADANA01", "Adana 01 FK", "A01", "Türkiye", "Adana", leagueId, "Türkiye 2. Lig", 3, "#F4D03F", "#17202A", 980_000L, 2, 62, "Volkan Bekiroğlu", "4-4-2 Mücadeleci", "Tarsus Şehir Stadı", 6200, false)
            )
            "TR_1_LIG" -> listOf(
                club("T1_KOCAELI", "Kocaelispor", "KOC", "Türkiye", "Kocaeli", leagueId, "Türkiye 1. Lig", 2, "#1E8449", "#17202A", 3_800_000L, 4, 71, "Ertuğrul Sağlam", "4-2-3-1 Güçlü Hücum", "Yıldız Entegre Kocaeli Stadı", 34712, false),
                club("T1_SAKARYA", "Sakaryaspor", "SAK", "Türkiye", "Sakarya", leagueId, "Türkiye 1. Lig", 2, "#1E8449", "#17202A", 3_200_000L, 3, 70, "Mesut Bakkal", "4-3-3 Kanat Organizasyonu", "Yeni Sakarya Atatürk", 28154, false),
                club("T1_ANKARAGUCU", "MKE Ankaragücü", "ANK", "Türkiye", "Ankara", leagueId, "Türkiye 1. Lig", 2, "#F4D03F", "#1B4F72", 3_600_000L, 4, 71, "Kenan Koçak", "4-2-3-1 Dengeli", "Eryaman Stadyumu", 20560, false),
                club("T1_KARAGUMRUK", "Fatih Karagümrük", "FKG", "Türkiye", "İstanbul", leagueId, "Türkiye 1. Lig", 2, "#922B21", "#17202A", 3_100_000L, 3, 69, "Şenol Can", "4-3-3 Pas Oyunu", "Atatürk Olimpiyat", 40000, false),
                club("T1_BANDIRMA", "Bandırmaspor", "BAN", "Türkiye", "Balıkesir", leagueId, "Türkiye 1. Lig", 2, "#78281F", "#F7FAF8", 2_700_000L, 3, 68, "Mustafa Gürsel", "4-2-3-1 Geçiş Oyunu", "Bandırma 17 Eylül", 12725, false),
                club("T1_GENCLER", "Gençlerbirliği", "GBİ", "Türkiye", "Ankara", leagueId, "Türkiye 1. Lig", 2, "#922B21", "#17202A", 2_600_000L, 4, 68, "Recep Karatepe", "4-3-3 Akademi Odaklı", "Eryaman Stadyumu", 20560, false),
                club("T1_CORUM", "Çorum FK", "ÇOR", "Türkiye", "Çorum", leagueId, "Türkiye 1. Lig", 2, "#922B21", "#17202A", 2_800_000L, 3, 69, "Serkan Özbalta", "4-2-3-1 Kompakt", "Çorum Şehir Stadyumu", 15000, false),
                club("T1_ERZURUM", "Erzurumspor FK", "ERZ", "Türkiye", "Erzurum", leagueId, "Türkiye 1. Lig", 2, "#1F618D", "#F7FAF8", 2_400_000L, 3, 67, "Hakan Kutlu", "4-4-2 Dirençli Savunma", "Kazım Karabekir Stadı", 21374, false)
            )
            "TR_SUPER_LIG" -> listOf(
                club("TSL_GS", "Galatasaray", "GS", "Türkiye", "İstanbul", leagueId, "Türkiye Süper Lig", 1, "#A93226", "#F39C12", 42_000_000L, 5, 83, "Okan Buruk", "4-2-3-1 Önde Şok Pres", "RAMS Park", 52600, false),
                club("TSL_FB", "Fenerbahçe", "FB", "Türkiye", "İstanbul", leagueId, "Türkiye Süper Lig", 1, "#1B4F72", "#F4D03F", 40_000_000L, 5, 82, "José Mourinho", "4-2-3-1 Taktiksel Geçiş", "Ülker Stadyumu", 47834, false),
                club("TSL_BJK", "Beşiktaş", "BJK", "Türkiye", "İstanbul", leagueId, "Türkiye Süper Lig", 1, "#17202A", "#F7FAF8", 32_000_000L, 5, 80, "Giovanni van Bronckhorst", "4-3-3 Dinamik Hücum", "Tüpraş Stadyumu", 42590, false),
                club("TSL_TS", "Trabzonspor", "TS", "Türkiye", "Trabzon", leagueId, "Türkiye Süper Lig", 1, "#78281F", "#2874A6", 24_000_000L, 4, 78, "Şenol Güneş", "4-2-3-1 Topa Sahip Olma", "Papara Park", 40782, false),
                club("TSL_IBFK", "Başakşehir FK", "İBF", "Türkiye", "İstanbul", leagueId, "Türkiye Süper Lig", 1, "#D35400", "#1B4F72", 16_000_000L, 4, 76, "Çağdaş Atan", "4-3-3 Geriden Oyun Kurma", "Başakşehir Fatih Terim", 17156, false),
                club("TSL_SAM", "Samsunspor", "SAM", "Türkiye", "Samsun", leagueId, "Türkiye Süper Lig", 1, "#922B21", "#F7FAF8", 12_000_000L, 4, 75, "Thomas Reis", "4-2-3-1 Yüksek Tempo", "Samsun 19 Mayıs Stadı", 33919, false),
                club("TSL_GOZ", "Göztepe", "GÖZ", "Türkiye", "İzmir", leagueId, "Türkiye Süper Lig", 1, "#F4D03F", "#922B21", 11_500_000L, 4, 74, "Stanimir Stoilov", "3-4-1-2 Dinamik Pres", "Gürsel Aksel Stadyumu", 20040, false),
                club("TSL_EYU", "Eyüpspor", "EYÜ", "Türkiye", "İstanbul", leagueId, "Türkiye Süper Lig", 1, "#6C3483", "#F4D03F", 11_000_000L, 3, 74, "Arda Turan", "4-3-3 Pas ve Pozisyon", "Recep Tayyip Erdoğan Stadı", 14234, false)
            )
            "FRA_L1" -> listOf(
                club("FRA_OM", "Olympique Marseille", "OM", "Fransa", "Marsilya", leagueId, "Fransa Ligue 1", 1, "#2E86C1", "#F7FAF8", 68_000_000L, 5, 83, "Roberto De Zerbi", "4-2-3-1 Akıcı Hücum", "Stade Vélodrome", 67394, false),
                club("FRA_PSG", "Paris Saint-Germain", "PSG", "Fransa", "Paris", leagueId, "Fransa Ligue 1", 1, "#1B4F72", "#922B21", 145_000_000L, 5, 88, "Luis Enrique", "4-3-3 Top Hakimiyeti", "Parc des Princes", 47929, false),
                club("FRA_ASM", "AS Monaco", "ASM", "Fransa", "Monako", leagueId, "Fransa Ligue 1", 1, "#922B21", "#F7FAF8", 62_000_000L, 5, 82, "Adi Hütter", "4-2-2-2 Dikey Pres", "Stade Louis-II", 16360, false),
                club("FRA_LIL", "LOSC Lille", "LIL", "Fransa", "Lille", leagueId, "Fransa Ligue 1", 1, "#922B21", "#1B4F72", 54_000_000L, 4, 81, "Bruno Genesio", "4-2-3-1 Dengeli", "Decathlon Arena", 50186, false),
                club("FRA_OL", "Olympique Lyonnais", "OL", "Fransa", "Lyon", leagueId, "Fransa Ligue 1", 1, "#F7FAF8", "#1B4F72", 58_000_000L, 5, 81, "Pierre Sage", "4-3-3 Hücum", "Groupama Stadium", 59186, false),
                club("FRA_NIC", "OGC Nice", "NIC", "Fransa", "Nice", leagueId, "Fransa Ligue 1", 1, "#922B21", "#17202A", 45_000_000L, 4, 79, "Franck Haise", "3-4-2-1 Kompakt", "Allianz Riviera", 36178, false),
                club("FRA_LEN", "RC Lens", "LEN", "Fransa", "Lens", leagueId, "Fransa Ligue 1", 1, "#922B21", "#F4D03F", 40_000_000L, 4, 78, "Will Still", "3-4-1-2 Yüksek Enerji", "Stade Bollaert-Delelis", 38223, false),
                club("FRA_REN", "Stade Rennais", "REN", "Fransa", "Rennes", leagueId, "Fransa Ligue 1", 1, "#922B21", "#17202A", 42_000_000L, 4, 78, "Jorge Sampaoli", "4-3-3 Dinamik", "Roazhon Park", 29778, false)
            )
            "ESP_L1" -> listOf(
                club("ESP_RMA", "Real Madrid", "RMA", "İspanya", "Madrid", leagueId, "İspanya Primera División", 1, "#F7FAF8", "#D4AC0D", 180_000_000L, 5, 91, "Carlo Ancelotti", "4-3-1-2 Elit Geçiş", "Santiago Bernabéu", 81044, false),
                club("ESP_BAR", "FC Barcelona", "BAR", "İspanya", "Barselona", leagueId, "İspanya Primera División", 1, "#1B4F72", "#922B21", 130_000_000L, 5, 89, "Hansi Flick", "4-2-3-1 Yüksek Hat Pres", "Estadi Olímpic / Camp Nou", 55000, false),
                club("ESP_ATM", "Atlético Madrid", "ATM", "İspanya", "Madrid", leagueId, "İspanya Primera División", 1, "#922B21", "#F7FAF8", 95_000_000L, 5, 86, "Diego Simeone", "3-5-2 Taktik Disiplin", "Metropolitano", 70460, false),
                club("ESP_RSO", "Real Sociedad", "RSO", "İspanya", "San Sebastián", leagueId, "İspanya Primera División", 1, "#1F618D", "#F7FAF8", 55_000_000L, 4, 81, "Imanol Alguacil", "4-3-3 Teknik Pas", "Reale Arena", 39313, false),
                club("ESP_ATH", "Athletic Club", "ATH", "İspanya", "Bilbao", leagueId, "İspanya Primera División", 1, "#922B21", "#F7FAF8", 58_000_000L, 5, 82, "Ernesto Valverde", "4-2-3-1 Dikey Tempo", "San Mamés", 53289, false),
                club("ESP_VIL", "Villarreal CF", "VIL", "İspanya", "Villarreal", leagueId, "İspanya Primera División", 1, "#F4D03F", "#1B4F72", 50_000_000L, 4, 80, "Marcelino", "4-4-2 Dengeli", "Estadio de la Cerámica", 23008, false),
                club("ESP_BET", "Real Betis", "BET", "İspanya", "Sevilla", leagueId, "İspanya Primera División", 1, "#1E8449", "#F7FAF8", 46_000_000L, 4, 79, "Manuel Pellegrini", "4-2-3-1 Yaratıcı", "Benito Villamarín", 60721, false),
                club("ESP_SEV", "Sevilla FC", "SEV", "İspanya", "Sevilla", leagueId, "İspanya Primera División", 1, "#F7FAF8", "#922B21", 44_000_000L, 4, 78, "García Pimienta", "4-3-3 Kanat", "Ramón Sánchez-Pizjuán", 42714, false)
            )
            "ENG_L1" -> listOf(
                club("ENG_MCI", "Manchester City", "MCI", "İngiltere", "Manchester", leagueId, "İngiltere Premier Division", 1, "#5DADE2", "#F7FAF8", 185_000_000L, 5, 91, "Pep Guardiola", "3-2-4-1 Pozisyonel", "Etihad Stadium", 53400, false),
                club("ENG_ARS", "Arsenal", "ARS", "İngiltere", "Londra", leagueId, "İngiltere Premier Division", 1, "#922B21", "#F7FAF8", 150_000_000L, 5, 89, "Mikel Arteta", "4-3-3 Kontrollü Pres", "Emirates Stadium", 60704, false),
                club("ENG_LIV", "Liverpool", "LIV", "İngiltere", "Liverpool", leagueId, "İngiltere Premier Division", 1, "#922B21", "#D4AC0D", 155_000_000L, 5, 89, "Arne Slot", "4-2-3-1 Dinamik Geçiş", "Anfield", 61276, false),
                club("ENG_CHE", "Chelsea", "CHE", "İngiltere", "Londra", leagueId, "İngiltere Premier Division", 1, "#1B4F72", "#F7FAF8", 135_000_000L, 5, 84, "Enzo Maresca", "4-2-3-1 Topa Sahip Olma", "Stamford Bridge", 40341, false),
                club("ENG_MUN", "Manchester United", "MUN", "İngiltere", "Manchester", leagueId, "İngiltere Premier Division", 1, "#922B21", "#17202A", 145_000_000L, 5, 83, "Rúben Amorim", "3-4-2-1 Modern Geçiş", "Old Trafford", 74310, false),
                club("ENG_TOT", "Tottenham Hotspur", "TOT", "İngiltere", "Londra", leagueId, "İngiltere Premier Division", 1, "#F7FAF8", "#1B4F72", 115_000_000L, 5, 83, "Ange Postecoglou", "4-3-3 Yüksek Hat", "Tottenham Hotspur Stadium", 62850, false),
                club("ENG_BHA", "Brighton & Hove Albion", "BHA", "İngiltere", "Brighton", leagueId, "İngiltere Premier Division", 1, "#1F618D", "#F7FAF8", 78_000_000L, 4, 80, "Fabian Hürzeler", "4-2-3-1 Modern Kurulum", "Amex Stadium", 31876, false),
                club("ENG_AVL", "Aston Villa", "AVL", "İngiltere", "Birmingham", leagueId, "İngiltere Premier Division", 1, "#6C3483", "#5DADE2", 92_000_000L, 5, 83, "Unai Emery", "4-2-2-2 Taktik Blok", "Villa Park", 42640, false)
            )
            "ITA_L1" -> listOf(
                club("ITA_INT", "Inter Milano", "INT", "İtalya", "Milano", leagueId, "İtalya Serie A", 1, "#1B4F72", "#17202A", 115_000_000L, 5, 88, "Simone Inzaghi", "3-5-2 Akışkan Rotasyon", "San Siro", 75817, false),
                club("ITA_JUV", "Juventus", "JUV", "İtalya", "Torino", leagueId, "İtalya Serie A", 1, "#17202A", "#F7FAF8", 110_000_000L, 5, 85, "Thiago Motta", "4-2-3-1 Kompakt", "Allianz Stadium", 41507, false),
                club("ITA_MIL", "AC Milan", "MIL", "İtalya", "Milano", leagueId, "İtalya Serie A", 1, "#922B21", "#17202A", 105_000_000L, 5, 85, "Paulo Fonseca", "4-2-3-1 Hızlı Kanat", "San Siro", 75817, false),
                club("ITA_NAP", "SSC Napoli", "NAP", "İtalya", "Napoli", leagueId, "İtalya Serie A", 1, "#2E86C1", "#F7FAF8", 95_000_000L, 4, 85, "Antonio Conte", "3-4-2-1 Yoğun Taktik", "Diego Armando Maradona", 54726, false),
                club("ITA_ATA", "Atalanta BC", "ATA", "İtalya", "Bergamo", leagueId, "İtalya Serie A", 1, "#1B4F72", "#17202A", 75_000_000L, 4, 84, "Gian Piero Gasperini", "3-4-1-2 Adam Adama Pres", "Gewiss Stadium", 24950, false),
                club("ITA_ROM", "AS Roma", "ROM", "İtalya", "Roma", leagueId, "İtalya Serie A", 1, "#78281F", "#D4AC0D", 80_000_000L, 4, 82, "Claudio Ranieri", "4-3-2-1 Dengeli", "Stadio Olimpico", 70634, false),
                club("ITA_LAZ", "SS Lazio", "LAZ", "İtalya", "Roma", leagueId, "İtalya Serie A", 1, "#5DADE2", "#F7FAF8", 68_000_000L, 4, 81, "Marco Baroni", "4-2-3-1 Hücum", "Stadio Olimpico", 70634, false),
                club("ITA_FIO", "ACF Fiorentina", "FIO", "İtalya", "Floransa", leagueId, "İtalya Serie A", 1, "#6C3483", "#F7FAF8", 62_000_000L, 4, 80, "Raffaele Palladino", "4-2-3-1 Teknik", "Artemio Franchi", 43147, false)
            )
            "GER_L1" -> listOf(
                club("GER_BAY", "Bayern München", "BAY", "Almanya", "Münih", leagueId, "Almanya Bundesliga", 1, "#922B21", "#F7FAF8", 165_000_000L, 5, 90, "Vincent Kompany", "4-2-3-1 Dominant Pres", "Allianz Arena", 75024, false),
                club("GER_B04", "Bayer Leverkusen", "B04", "Almanya", "Leverkusen", leagueId, "Almanya Bundesliga", 1, "#922B21", "#17202A", 110_000_000L, 5, 87, "Xabi Alonso", "3-4-2-1 Modern Pas", "BayArena", 30210, false),
                club("GER_BVB", "Borussia Dortmund", "BVB", "Almanya", "Dortmund", leagueId, "Almanya Bundesliga", 1, "#F4D03F", "#17202A", 105_000_000L, 5, 85, "Nuri Şahin", "4-2-3-1 Tempo Hücum", "Signal Iduna Park", 81365, false),
                club("GER_RBL", "RB Leipzig", "RBL", "Almanya", "Leipzig", leagueId, "Almanya Bundesliga", 1, "#F7FAF8", "#922B21", 92_000_000L, 5, 84, "Marco Rose", "4-2-2-2 Gegenpressing", "Red Bull Arena", 47069, false),
                club("GER_SGE", "Eintracht Frankfurt", "SGE", "Almanya", "Frankfurt", leagueId, "Almanya Bundesliga", 1, "#17202A", "#922B21", 68_000_000L, 4, 81, "Dino Toppmöller", "3-4-2-1 Hızlı Geçiş", "Deutsche Bank Park", 58000, false),
                club("GER_VFB", "VfB Stuttgart", "VFB", "Almanya", "Stuttgart", leagueId, "Almanya Bundesliga", 1, "#F7FAF8", "#922B21", 64_000_000L, 4, 81, "Sebastian Hoeneß", "4-2-3-1 Akıcı Pas", "MHPArena", 60058, false),
                club("GER_SCF", "SC Freiburg", "SCF", "Almanya", "Freiburg", leagueId, "Almanya Bundesliga", 1, "#922B21", "#17202A", 48_000_000L, 4, 78, "Julian Schuster", "4-2-3-1 Disiplinli", "Europa-Park Stadion", 34700, false),
                club("GER_SVW", "Werder Bremen", "SVW", "Almanya", "Bremen", leagueId, "Almanya Bundesliga", 1, "#1E8449", "#F7FAF8", 44_000_000L, 4, 77, "Ole Werner", "3-5-2 Dengeli", "Weserstadion", 42100, false)
            )
            else -> listOf(
                club("POR_SLB", "SL Benfica", "SLB", "Portekiz", "Lizbon", leagueId, "Portekiz Primeira Liga", 1, "#922B21", "#F7FAF8", 75_000_000L, 5, 83, "Bruno Lage", "4-2-3-1 Hücum", "Estádio da Luz", 64642, false),
                club("POR_SCP", "Sporting CP", "SCP", "Portekiz", "Lizbon", leagueId, "Portekiz Primeira Liga", 1, "#1E8449", "#F7FAF8", 72_000_000L, 5, 84, "João Pereira", "3-4-2-1 Dinamik", "Estádio José Alvalade", 50095, false),
                club("POR_FCP", "FC Porto", "FCP", "Portekiz", "Porto", leagueId, "Portekiz Primeira Liga", 1, "#1B4F72", "#F7FAF8", 70_000_000L, 5, 83, "Vítor Bruno", "4-2-3-1 Yoğun Pres", "Estádio do Dragão", 50033, false),
                club("POR_BRA", "SC Braga", "BRA", "Portekiz", "Braga", leagueId, "Portekiz Primeira Liga", 1, "#922B21", "#F7FAF8", 42_000_000L, 4, 79, "Carlos Carvalhal", "4-3-3 Teknik", "Estádio Municipal de Braga", 30286, false),
                club("POR_VIT", "Vitória SC", "VIT", "Portekiz", "Guimarães", leagueId, "Portekiz Primeira Liga", 1, "#17202A", "#F7FAF8", 28_000_000L, 4, 76, "Rui Borges", "4-3-3 Mücadeleci", "Dom Afonso Henriques", 30029, false),
                club("POR_FAM", "FC Famalicão", "FAM", "Portekiz", "Famalicão", leagueId, "Portekiz Primeira Liga", 1, "#1B4F72", "#F7FAF8", 22_000_000L, 3, 74, "Armando Evangelista", "4-2-3-1 Genç Yetenek", "Municipal 22 de Junho", 5186, false),
                club("POR_GIL", "Gil Vicente", "GIL", "Portekiz", "Barcelos", leagueId, "Portekiz Primeira Liga", 1, "#922B21", "#1B4F72", 18_000_000L, 3, 72, "Bruno Pinheiro", "4-3-3 Kontra", "Cidade de Barcelos", 12046, false),
                club("POR_RIO", "Rio Ave FC", "RIO", "Portekiz", "Vila do Conde", leagueId, "Portekiz Primeira Liga", 1, "#1E8449", "#F7FAF8", 18_000_000L, 3, 72, "Petit", "3-4-3 Dengeli", "Estádio dos Arcos", 5300, false)
            )
        }
        return rawClubs
    }

    private fun club(
        id: String,
        name: String,
        shortName: String,
        country: String,
        city: String,
        leagueId: String,
        leagueName: String,
        tier: Int,
        primaryHex: String,
        secondaryHex: String,
        budgetEur: Long,
        facilityQuality: Int,
        strength: Int,
        managerName: String,
        managerStyle: String,
        stadiumName: String,
        capacity: Int,
        isDemo: Boolean
    ): Club {
        val squad = generateSquadForClub(id, shortName, strength)
        return Club(
            id = id,
            name = name,
            shortName = shortName,
            country = country,
            city = city,
            leagueId = leagueId,
            leagueName = leagueName,
            tier = tier,
            primaryColorHex = primaryHex,
            secondaryColorHex = secondaryHex,
            budgetEur = budgetEur,
            facilityQuality = facilityQuality,
            teamStrength = strength,
            managerName = managerName,
            managerTacticalStyle = managerStyle,
            stadiumName = stadiumName,
            stadiumCapacity = capacity,
            isDemoClub = isDemo,
            keySquadMembers = squad
        )
    }

    private fun generateSquadForClub(clubId: String, shortName: String, strength: Int): List<SquadMemberInfo> {
        val firstNames = listOf("Emre", "Burak", "Kerem", "Mert", "Kaan", "Oğuz", "Yusuf", "Barış", "Eren", "Hakan", "Semih")
        val lastNames = listOf("Yılmaz", "Kaya", "Demir", "Çelik", "Şahin", "Öztürk", "Aydın", "Arslan", "Doğan", "Kılıç", "Koç")
        val positions = listOf(
            Position.GK, Position.CB, Position.CB, Position.FB, Position.FB,
            Position.DM, Position.CM, Position.AM, Position.WG, Position.WG, Position.ST
        )
        val numbers = listOf(1, 4, 5, 2, 3, 6, 8, 10, 7, 11, 9)
        val hash = clubId.hashCode().let { if (it < 0) -it else it }
        return positions.mapIndexed { idx, pos ->
            val fn = firstNames[(hash + idx * 3) % firstNames.size]
            val ln = lastNames[(hash + idx * 5) % lastNames.size]
            val ovr = (strength - 2 + (idx % 5)).coerceIn(42, 92)
            SquadMemberInfo(
                id = "${clubId}_P$idx",
                name = "$fn $ln",
                position = pos,
                kitNumber = numbers[idx],
                overall = ovr,
                age = 20 + ((hash + idx) % 12),
                isCaptain = idx == 5
            )
        }
    }

    // Verified Real Professional Footballer Presets (2026/27 Reference Snapshot)
    val realPlayerPresets: List<RealPlayerPreset> = listOf(
        RealPlayerPreset(
            id = "REAL_MASON_GREENWOOD",
            firstName = "Mason",
            lastName = "Greenwood",
            birthDate = "01.10.2001",
            age = 24,
            nationality = "İngiltere",
            birthCity = "Bradford",
            primaryPosition = Position.WG,
            secondaryPositions = listOf(Position.ST, Position.AM),
            preferredFoot = PreferredFoot.BOTH,
            heightCm = 181,
            weightKg = 75,
            kitName = "GREENWOOD",
            kitNumber = 10,
            defaultLeagueId = "FRA_L1",
            defaultClubId = "FRA_OM",
            currentClubDisplayName = "Olympique Marseille",
            contractEndYear = 2029,
            weeklyWageEur = 85_000L,
            marketValueEur = 35_000_000L,
            potential = 87,
            attributes = PlayerAttributes(
                shooting = 84,
                passing = 77,
                dribbling = 83,
                firstTouch = 82,
                pace = 83,
                agility = 82,
                stamina = 78,
                strength = 72,
                defending = 38,
                tactics = 76,
                vision = 78,
                setPieces = 76,
                goalkeeping = 15
            ),
            appearance = PlayerAppearance(
                skinToneHex = "#A36E49",
                hairStyle = "Kıvırcık Kısa",
                hairColorHex = "#181513",
                facialPreset = "Atletik Keskin",
                kitCollarStyle = "V-Yaka",
                bootColorHex = "#20C773"
            ),
            careerSummary = "Manchester United altyapısından yetişti; Getafe kiralık döneminin ardından 2024 yazında Olympique Marseille'e transfer oldu. Her iki ayağını da etkili kullanan bitirici kanat/forvet.",
            verifiedSeasonSource = "Doğrulanmış Transfer & Kadro Verisi (Sezon Bazı: 2024/25 - 2026/27 Projeksiyonu)",
            archiveSeasons = listOf(
                SeasonStatsRecord(2023, "ESP_GET", "Getafe CF", "İspanya Primera División", 33, 30, 2650, 8, 6, 237.6, 5, 1),
                SeasonStatsRecord(2024, "FRA_OM", "Olympique Marseille", "Fransa Ligue 1", 32, 31, 2740, 16, 5, 243.2, 3, 0)
            )
        ),
        RealPlayerPreset(
            id = "REAL_ARDA_GULER",
            firstName = "Arda",
            lastName = "Güler",
            birthDate = "25.02.2005",
            age = 21,
            nationality = "Türkiye",
            birthCity = "Ankara",
            primaryPosition = Position.AM,
            secondaryPositions = listOf(Position.WG, Position.CM),
            preferredFoot = PreferredFoot.LEFT,
            heightCm = 175,
            weightKg = 70,
            kitName = "ARDA GÜLER",
            kitNumber = 15,
            defaultLeagueId = "ESP_L1",
            defaultClubId = "ESP_RMA",
            currentClubDisplayName = "Real Madrid",
            contractEndYear = 2029,
            weeklyWageEur = 95_000L,
            marketValueEur = 45_000_000L,
            potential = 91,
            attributes = PlayerAttributes(
                shooting = 80,
                passing = 85,
                dribbling = 86,
                firstTouch = 88,
                pace = 78,
                agility = 86,
                stamina = 75,
                strength = 66,
                defending = 48,
                tactics = 81,
                vision = 88,
                setPieces = 85,
                goalkeeping = 14
            ),
            appearance = PlayerAppearance(
                skinToneHex = "#E5B592",
                hairStyle = "Dalgalı Modern",
                hairColorHex = "#241C16",
                facialPreset = "Genç Yıldız",
                kitCollarStyle = "Klasik Yaka",
                bootColorHex = "#FFD166"
            ),
            careerSummary = "Gençlerbirliği ve Fenerbahçe altyapısından yetişerek 2023'te Real Madrid'e transfer oldu. Olağanüstü oyun görüşü, sol ayak tekniği ve duran top ustalığıyla tanınır.",
            verifiedSeasonSource = "Doğrulanmış Transfer & Kadro Verisi (Sezon Bazı: 2024/25 - 2026/27 Projeksiyonu)",
            archiveSeasons = listOf(
                SeasonStatsRecord(2022, "TSL_FB", "Fenerbahçe", "Türkiye Süper Lig", 20, 11, 912, 4, 3, 146.0, 2, 0, trophiesWon = listOf("Türkiye Kupası")),
                SeasonStatsRecord(2023, "ESP_RMA", "Real Madrid", "İspanya Primera División", 10, 4, 373, 6, 0, 76.0, 1, 0, trophiesWon = listOf("La Liga", "UEFA Şampiyonlar Ligi"))
            )
        ),
        RealPlayerPreset(
            id = "REAL_KENAN_YILDIZ",
            firstName = "Kenan",
            lastName = "Yıldız",
            birthDate = "04.05.2005",
            age = 21,
            nationality = "Türkiye",
            birthCity = "Regensburg",
            primaryPosition = Position.WG,
            secondaryPositions = listOf(Position.AM, Position.ST),
            preferredFoot = PreferredFoot.BOTH,
            heightCm = 187,
            weightKg = 77,
            kitName = "YILDIZ",
            kitNumber = 10,
            defaultLeagueId = "ITA_L1",
            defaultClubId = "ITA_JUV",
            currentClubDisplayName = "Juventus",
            contractEndYear = 2029,
            weeklyWageEur = 75_000L,
            marketValueEur = 42_000_000L,
            potential = 90,
            attributes = PlayerAttributes(
                shooting = 79,
                passing = 78,
                dribbling = 85,
                firstTouch = 84,
                pace = 84,
                agility = 83,
                stamina = 79,
                strength = 75,
                defending = 42,
                tactics = 77,
                vision = 80,
                setPieces = 74,
                goalkeeping = 15
            ),
            appearance = PlayerAppearance(
                skinToneHex = "#E5B592",
                hairStyle = "Modern Kısa",
                hairColorHex = "#2A2018",
                facialPreset = "Atletik Keskin",
                kitCollarStyle = "V-Yaka",
                bootColorHex = "#F7FAF8"
            ),
            careerSummary = "Bayern Münih altyapısından Juventus'a geçti ve kulübün ikonik 10 numaralı formasını devraldı. Hızı, çalım yeteneği ve iki ayağıyla şut tehdidiyle öne çıkar.",
            verifiedSeasonSource = "Doğrulanmış Transfer & Kadro Verisi (Sezon Bazı: 2024/25 - 2026/27 Projeksiyonu)",
            archiveSeasons = listOf(
                SeasonStatsRecord(2023, "ITA_JUV", "Juventus", "İtalya Serie A", 27, 7, 975, 2, 1, 186.3, 3, 0, trophiesWon = listOf("Coppa Italia"))
            )
        ),
        RealPlayerPreset(
            id = "REAL_SEMIH_KILICSOY",
            firstName = "Semih",
            lastName = "Kılıçsoy",
            birthDate = "15.08.2005",
            age = 21,
            nationality = "Türkiye",
            birthCity = "İstanbul",
            primaryPosition = Position.ST,
            secondaryPositions = listOf(Position.WG),
            preferredFoot = PreferredFoot.RIGHT,
            heightCm = 178,
            weightKg = 78,
            kitName = "SEMİH",
            kitNumber = 9,
            defaultLeagueId = "TR_SUPER_LIG",
            defaultClubId = "TSL_BJK",
            currentClubDisplayName = "Beşiktaş",
            contractEndYear = 2028,
            weeklyWageEur = 32_000L,
            marketValueEur = 16_000_000L,
            potential = 86,
            attributes = PlayerAttributes(
                shooting = 79,
                passing = 71,
                dribbling = 78,
                firstTouch = 77,
                pace = 79,
                agility = 78,
                stamina = 76,
                strength = 81,
                defending = 36,
                tactics = 74,
                vision = 72,
                setPieces = 68,
                goalkeeping = 14
            ),
            appearance = PlayerAppearance(
                skinToneHex = "#DDA682",
                hairStyle = "Kısa Klasik",
                hairColorHex = "#1B1612",
                facialPreset = "Güçlü Forvet",
                kitCollarStyle = "Klasik Yaka",
                bootColorHex = "#FF4D5E"
            ),
            careerSummary = "Beşiktaş Özkaynak düzeninden yetişen güçlü ve patlayıcı santrfor. Sırtı dönük oyun ve ceza sahası içi sert şutlarıyla tanınır.",
            verifiedSeasonSource = "Doğrulanmış Transfer & Kadro Verisi (Sezon Bazı: 2024/25 - 2026/27 Projeksiyonu)",
            archiveSeasons = listOf(
                SeasonStatsRecord(2023, "TSL_BJK", "Beşiktaş", "Türkiye Süper Lig", 23, 18, 1650, 11, 4, 165.6, 4, 0, trophiesWon = listOf("Türkiye Kupası"))
            )
        ),
        RealPlayerPreset(
            id = "REAL_BARIS_ALPER",
            firstName = "Barış Alper",
            lastName = "Yılmaz",
            birthDate = "23.05.2000",
            age = 26,
            nationality = "Türkiye",
            birthCity = "Rize",
            primaryPosition = Position.WG,
            secondaryPositions = listOf(Position.WB, Position.ST),
            preferredFoot = PreferredFoot.RIGHT,
            heightCm = 186,
            weightKg = 82,
            kitName = "BARIŞ ALPER",
            kitNumber = 53,
            defaultLeagueId = "TR_SUPER_LIG",
            defaultClubId = "TSL_GS",
            currentClubDisplayName = "Galatasaray",
            contractEndYear = 2028,
            weeklyWageEur = 42_000L,
            marketValueEur = 21_000_000L,
            potential = 84,
            attributes = PlayerAttributes(
                shooting = 76,
                passing = 73,
                dribbling = 79,
                firstTouch = 76,
                pace = 88,
                agility = 82,
                stamina = 90,
                strength = 86,
                defending = 68,
                tactics = 77,
                vision = 73,
                setPieces = 66,
                goalkeeping = 15
            ),
            appearance = PlayerAppearance(
                skinToneHex = "#E5B592",
                hairStyle = "Modern Sarı Röfle",
                hairColorHex = "#C29B48",
                facialPreset = "Atletik Keskin",
                kitCollarStyle = "V-Yaka",
                bootColorHex = "#20C773"
            ),
            careerSummary = "Ankara Demirspor ve Keçiörengücü'nden Galatasaray'a uzanan gerçek bir alt liglerden zirveye yükseliş öyküsü. Yüksek fizik gücü ve patlayıcı hızıyla çok yönlü kanat oyuncusu.",
            verifiedSeasonSource = "Doğrulanmış Transfer & Kadro Verisi (Sezon Bazı: 2024/25 - 2026/27 Projeksiyonu)",
            archiveSeasons = listOf(
                SeasonStatsRecord(2023, "TSL_GS", "Galatasaray", "Türkiye Süper Lig", 37, 26, 2520, 6, 7, 266.4, 6, 0, trophiesWon = listOf("Süper Lig Şampiyonluğu"))
            )
        ),
        RealPlayerPreset(
            id = "REAL_FERDI_KADIOGLU",
            firstName = "Ferdi",
            lastName = "Kadıoğlu",
            birthDate = "07.10.1999",
            age = 26,
            nationality = "Türkiye",
            birthCity = "Arnhem",
            primaryPosition = Position.WB,
            secondaryPositions = listOf(Position.FB, Position.CM, Position.WG),
            preferredFoot = PreferredFoot.RIGHT,
            heightCm = 174,
            weightKg = 68,
            kitName = "FERDİ",
            kitNumber = 24,
            defaultLeagueId = "ENG_L1",
            defaultClubId = "ENG_BHA",
            currentClubDisplayName = "Brighton & Hove Albion",
            contractEndYear = 2028,
            weeklyWageEur = 78_000L,
            marketValueEur = 30_000_000L,
            potential = 85,
            attributes = PlayerAttributes(
                shooting = 73,
                passing = 81,
                dribbling = 84,
                firstTouch = 83,
                pace = 85,
                agility = 86,
                stamina = 89,
                strength = 71,
                defending = 77,
                tactics = 81,
                vision = 80,
                setPieces = 72,
                goalkeeping = 15
            ),
            appearance = PlayerAppearance(
                skinToneHex = "#DDA682",
                hairStyle = "Dalgalı Modern",
                hairColorHex = "#241C16",
                facialPreset = "Atletik Keskin",
                kitCollarStyle = "V-Yaka",
                bootColorHex = "#43F293"
            ),
            careerSummary = "NEC Nijmegen'den Fenerbahçe'ye, oradan rekor bonservisle İngiltere Premier Division ekibi Brighton'a transfer olan dinamik kanat bek.",
            verifiedSeasonSource = "Doğrulanmış Transfer & Kadro Verisi (Sezon Bazı: 2024/25 - 2026/27 Projeksiyonu)",
            archiveSeasons = listOf(
                SeasonStatsRecord(2023, "TSL_FB", "Fenerbahçe", "Türkiye Süper Lig", 37, 36, 3310, 1, 4, 273.8, 5, 0)
            )
        ),
        RealPlayerPreset(
            id = "REAL_HAKAN_CALHANOGLU",
            firstName = "Hakan",
            lastName = "Çalhanoğlu",
            birthDate = "08.02.1994",
            age = 32,
            nationality = "Türkiye",
            birthCity = "Mannheim",
            primaryPosition = Position.DM,
            secondaryPositions = listOf(Position.CM, Position.AM),
            preferredFoot = PreferredFoot.RIGHT,
            heightCm = 178,
            weightKg = 76,
            kitName = "ÇALHANOĞLU",
            kitNumber = 20,
            defaultLeagueId = "ITA_L1",
            defaultClubId = "ITA_INT",
            currentClubDisplayName = "Inter Milano",
            contractEndYear = 2027,
            weeklyWageEur = 135_000L,
            marketValueEur = 40_000_000L,
            potential = 87,
            attributes = PlayerAttributes(
                shooting = 85,
                passing = 90,
                dribbling = 82,
                firstTouch = 87,
                pace = 73,
                agility = 78,
                stamina = 84,
                strength = 75,
                defending = 80,
                tactics = 89,
                vision = 91,
                setPieces = 92,
                goalkeeping = 15
            ),
            appearance = PlayerAppearance(
                skinToneHex = "#DDA682",
                hairStyle = "Kısa Klasik",
                hairColorHex = "#1B1612",
                facialPreset = "Olgun Kaptan",
                kitCollarStyle = "Klasik Yaka",
                bootColorHex = "#E6B84D"
            ),
            careerSummary = "A Milli Takım kaptanı ve Inter Milano'nun oyun kurucu registası. Uzaktan şutları, penaltıları ve uzun pas isabetiyle dünya çapında orta saha.",
            verifiedSeasonSource = "Doğrulanmış Transfer & Kadro Verisi (Sezon Bazı: 2024/25 - 2026/27 Projeksiyonu)",
            archiveSeasons = listOf(
                SeasonStatsRecord(2023, "ITA_INT", "Inter Milano", "İtalya Serie A", 32, 32, 2670, 13, 3, 252.8, 5, 0, trophiesWon = listOf("Serie A Scudetto"), individualAwards = listOf("Serie A En İyi Orta Saha"))
            )
        )
    )

    val trainingDrills: List<TrainingDrillDefinition> = listOf(
        TrainingDrillDefinition("DRILL_SHOOTING", "Bitiricilik ve Köşe Şutları", "HÜCUM", "Ceza sahası içi ve çevresinden kaleciye karşı plase ve sert şut tekrarları.", 18, 4, "Şut", "İlk Kontrol", listOf(Position.ST, Position.WG, Position.AM)),
        TrainingDrillDefinition("DRILL_PASSING", "Dar Alan Pas ve Oyun Görüşü", "TEKNİK", "Baskı altında tek pas, ara pasları ve üçüncü bölge kilit pas organizasyonu.", 15, 2, "Pas", "Oyun Görüşü", listOf(Position.CM, Position.AM, Position.DM)),
        TrainingDrillDefinition("DRILL_DRIBBLING", "1'e 1 Adam Eksiltme & Çeviklik", "TEKNİK", "Slalom konileri ve savunmacıya karşı hızlanarak çalım çalışmaları.", 17, 4, "Dribbling", "Çeviklik", listOf(Position.WG, Position.AM, Position.WB)),
        TrainingDrillDefinition("DRILL_FIRST_TOUCH", "Yüksek Top & İlk Kontrol", "TEKNİK", "Zor açılardan gelen uzun topları tek dokunuşla yönlendirme idmanı.", 13, 2, "İlk Kontrol", "Dribbling", listOf(Position.ST, Position.AM, Position.CM)),
        TrainingDrillDefinition("DRILL_PACE", "Patlayıcı Sprint & Hızlanma", "FİZİKSEL", "30 metre fotoselli sprint ve savunma arkası koşu zamanlaması.", 22, 7, "Hız", "Çeviklik", listOf(Position.WG, Position.FB, Position.WB, Position.ST)),
        TrainingDrillDefinition("DRILL_STAMINA", "Yüksek Yoğunluklu Dayanıklılık", "FİZİKSEL", "90 dakika pres temposunu koruyan aerobik koşu ve nabız kontrolü.", 24, 6, "Dayanıklılık", "Fiziksel Güç", Position.entries),
        TrainingDrillDefinition("DRILL_STRENGTH", "İkili Mücadele & Kuvvet Salonu", "FİZİKSEL", "Omuz omuza mücadele, korakor top saklama ve core güçlendirme.", 21, 6, "Fiziksel Güç", "Dayanıklılık", listOf(Position.CB, Position.DM, Position.ST)),
        TrainingDrillDefinition("DRILL_DEFENDING", "Alan Kapatma & Müdahale", "SAVUNMA", "Kayarak müdahale zamanlaması, pas arası ve bire bir savunma duruşu.", 17, 4, "Savunma", "Taktik", listOf(Position.CB, Position.FB, Position.WB, Position.DM)),
        TrainingDrillDefinition("DRILL_TACTICS", "Video Analiz & Taktik Pozisyon", "TAKTİK", "Teknik direktör ekibiyle kayma, ofsayt hattı ve geçiş pozisyonlanması.", 10, 1, "Taktik", "Oyun Görüşü", Position.entries),
        TrainingDrillDefinition("DRILL_SET_PIECES", "Serbest Vuruş, Korner & Penaltı", "ÖZEL", "Baraj üstü frikik, falsolu ortalar ve soğukkanlı penaltı vuruşları.", 12, 2, "Duran Toplar", "Şut", Position.entries),
        TrainingDrillDefinition("DRILL_GK", "Kaleci Refleks & Bire Bir Kapatma", "ÖZEL", "Yakın mesafe refleks kurtarışları ve yan top hakimiyeti.", 16, 3, "Kalecilik", "Çeviklik", listOf(Position.GK)),
        TrainingDrillDefinition("DRILL_RECOVERY", "Aktif Rejenerasyon & Buz Banyosu", "DİNLENME", "Fizyoterapist eşliğinde kas yenileme, esneme ve enerji toplama.", -26, 0, "Kondisyon", "Moral", Position.entries)
    )

    val lifestyleVenueActions: List<LifestyleVenueAction> = listOf(
        LifestyleVenueAction("V_HOME_REST", "Ev", "EV", "Evde Dinlen ve Maç Kaseti İzle", "Kaliteli uyku ve zihinsel odaklanma ile enerjini yenile.", 0L, +24, +6, +2, "Aile", +4, 0),
        LifestyleVenueAction("V_HOME_FAMILY", "Ev", "EV", "Ailenle Akşam Yemeği", "Ailenle vakit geçirerek stresini azalt ve moral depola.", 180L, +10, +14, +1, "Aile", +10, 20),
        LifestyleVenueAction("V_FACILITY_EXTRA", "Kulüp Tesisi", "TESİS", "Teknik Direktörle Taktik Sohbet", "Hocanın beklentilerini dinle ve ilk 11 şansını artır.", 0L, -6, +4, +8, "Teknik Direktör", +8, 15),
        LifestyleVenueAction("V_FACILITY_TEAM", "Kulüp Tesisi", "TESİS", "Takım Arkadaşlarıyla Masa Tenisi", "Tesislerde takım içi uyumu ve arkadaşlığı güçlendir.", 0L, -4, +9, +2, "Takım Kaptanı", +9, 30),
        LifestyleVenueAction("V_RESTAURANT_TEAM", "Restoran", "RESTORAN", "Takım Yemeği Düzenle", "Takım arkadaşlarını seçkin bir restoranda ağırlayarak liderlik göster.", 1_250L, -5, +12, +4, "Takım Kaptanı", +12, 120),
        LifestyleVenueAction("V_RESTAURANT_DATE", "Restoran", "RESTORAN", "Özel Akşam Yemeği", "Sosyal çevrenle veya partnerinle şık bir akşam geçir.", 850L, -6, +15, 0, "Sosyal Çevre", +12, 85),
        LifestyleVenueAction("V_CAFE_AGENT", "Kafe", "KAFE", "Menajerinle Kariyer Toplantısı", "Sözleşme şartlarını ve kulüplerin sana olan ilgisini değerlendir.", 150L, -3, +5, 0, "Menajer", +9, 40),
        LifestyleVenueAction("V_MALL_STYLE", "Alışveriş Merkezi", "AVM", "Stil ve Gardırop Yenileme", "Medya önünde dikkat çekecek modern kombinler satın al.", 2_400L, -8, +11, 0, null, 0, 380),
        LifestyleVenueAction("V_GYM_PERSONAL", "Spor Salonu", "SPOR_SALONU", "Özel Atletik Performans Koçu", "Kulüp dışı bireysel core ve patlayıcı kuvvet çalışması.", 900L, -14, +5, +3, "Teknik Direktör", +4, 90, "PHY"),
        LifestyleVenueAction("V_STADIUM_FANS", "Stadyum", "SOSYAL", "Taraftar İmza Günü ve Forma Etkinliği", "Kulüp mağazasında genç taraftarlarla buluş.", 0L, -8, +10, +3, null, 0, 520),
        LifestyleVenueAction("V_NIGHT_LOUNGE", "Sosyal Etkinlik Alanı", "SOSYAL", "Gece Geç Saatte Eğlenceye Katıl", "Yüksek görünürlük sağlar ancak yorgunluk ve disiplin riski taşır!", 3_500L, -28, +8, -11, "Teknik Direktör", -9, 640),
        LifestyleVenueAction("V_HOLIDAY_COAST", "Tatil & Seyahat", "TATİL", "Kısa Sahil Kaçamağı & Spa", "Yoğun fikstür arasında zihinsel ve fiziksel tam yenilenme.", 4_800L, +32, +22, -2, "Aile", +8, 410)
    )

    val lifestylePurchaseItems: List<LifestylePurchaseItem> = listOf(
        LifestylePurchaseItem("ITEM_BOOTS_PRO", "KRAMPON", "Karbon Taban Elit Krampon", "Özel kalıp profesyonel maç kramponu (+Moral & sahada özgüven).", 1_800L, 4, 2, 0L),
        LifestylePurchaseItem("ITEM_BOOTS_GOLD", "KRAMPON", "Altın Serisi Özel Üretim Krampon", "İsminin yazılı olduğu ultra hafif hız kramponu.", 3_400L, 7, 3, 0L),
        LifestylePurchaseItem("ITEM_STYLE_SUIT", "KIYAFET", "İtalyan Kesim Maç Günü Takımı", "Deplasman yolculukları ve ödül geceleri için özel dikim.", 2_200L, 6, 1, 0L),
        LifestylePurchaseItem("ITEM_STYLE_STREET", "KIYAFET", "Premium Sokak Stili Koleksiyonu", "Sosyal medya paylaşımlarında yüksek etkileşim sağlar.", 1_450L, 5, 1, 0L),
        LifestylePurchaseItem("ITEM_RECOVERY_CRYO", "EKİPMAN", "Ev Tipi Soğuk Terapi & Masaj Sistemi", "Her gün sonunda ekstra +6 kondisyon yenilenmesi sağlar.", 8_500L, 5, 6, 0L),
        LifestylePurchaseItem("ITEM_EQUIP_HOMEGYM", "EKİPMAN", "Profesyonel Ev Kondisyon & Core İstasyonu", "Bireysel kuvvet ve esneklik çalışmalarını evde sürdür.", 5_200L, 5, 4, 0L),
        LifestylePurchaseItem("ITEM_TECH_ANALYZER", "ELEKTRONİK", "Giyilebilir GPS & Nabız Performans Sensörü", "Antrenman yükünü ve uyku kalitesini takip eder (+3 enerji yenilenmesi).", 1_650L, 4, 3, 0L),
        LifestylePurchaseItem("ITEM_TECH_CINEMA", "ELEKTRONİK", "85\" OLED Taktik Analiz & Oyun Odası Sistemi", "Dinlenme günlerinde moral ve maç analizi keyfi.", 4_200L, 8, 2, 0L),
        LifestylePurchaseItem("ITEM_CAR_HATCH", "ARAÇ", "Modern Şehir Otomobili", "Antrenman tesislerine konforlu ulaşım sağlar.", 24_000L, 6, 2, 0L),
        LifestylePurchaseItem("ITEM_CAR_SPORT", "ARAÇ", "Premium Spor Coupe", "Yüksek prestij ve sosyal medya görünürlüğü kazandırır.", 115_000L, 12, 3, 0L),
        LifestylePurchaseItem("ITEM_HOME_RESIDENCE", "EV", "Tesis Yakını Modern Rezidans Dairesi", "Özel dinlenme alanı ile her gün +5 enerji ve moral katkısı.", 85_000L, 10, 5, 0L),
        LifestylePurchaseItem("ITEM_HOME_VILLA", "EV", "Özel Havuzlu ve Spor Salonlu Villa", "Elit futbolcu yaşam standardı (+10 enerji yenilenmesi).", 340_000L, 18, 10, 0L),
        LifestylePurchaseItem("ITEM_INV_ACADEMY", "YATIRIM", "Yerel Futbol Akademisi Ortaklığı", "Haftalık düzenli 1.200 € pasif gelir ve yüksek toplumsal saygınlık.", 45_000L, 5, 0, 1_200L),
        LifestylePurchaseItem("ITEM_INV_REALESTATE", "YATIRIM", "Ticari Gayrimenkul Fonu", "Haftalık düzenli 3.500 € kira ve temettü geliri sağlar.", 130_000L, 6, 0, 3_500L)
    )

    fun buildDynamicDilemmas(clubName: String, managerName: String): List<DynamicCareerDilemma> = listOf(
        DynamicCareerDilemma(
            id = "DILEMMA_MEDICAL_CHECKUP",
            dayIndex = 2,
            category = "SAĞLIK & PERFORMANS",
            title = "Kulüp Sağlık Kontrolü ve Efor Testi",
            narrative = "$clubName Sağlık Merkezi'nde Başhekim Dr. Selim Arslan seni EKG, laktat ve VO2 Max efor testine aldı. Monitörde kalp ritmin ve kas yorgunluk değerlerin inceleniyor. Doktor hangi protokolü uygulamak istediğini soruyor.",
            speakerName = "Dr. Selim Arslan",
            speakerRole = "Kulüp Başhekimi",
            sceneType = "MEDICAL_CHECKUP",
            locationBadge = "$clubName SAĞLIK MERKEZİ • BİYOMETRİK TARAMA",
            choices = listOf(
                DynamicStoryChoice(
                    id = "CH_MED_MAX_EFFORT",
                    label = "Maksimum Kapasite Efor Testine Gir",
                    description = "Sınırlarını zorla; teknik heyetin gözünde fiziksel hazırbulunuşluğun tavan yapar.",
                    moraleDelta = +5,
                    energyDelta = -6,
                    managerTrustDelta = +9,
                    teammateAffinityDelta = +3,
                    fanSupportDelta = +2,
                    consequenceHeadline = "Sağlık kontrolündeki rekor VO2 Max değerlerin teknik direktör $managerName'i büyüledi!"
                ),
                DynamicStoryChoice(
                    id = "CH_MED_CRYO_RECOVERY",
                    label = "Kriyoterapi & Kas Yenileme Protokolü Seç",
                    description = "Soğuk terapi ve fizyoterapi ile maç öncesi enerjini maksimuma çıkar.",
                    moraleDelta = +6,
                    energyDelta = +18,
                    managerTrustDelta = +4,
                    teammateAffinityDelta = 0,
                    fanSupportDelta = 0,
                    consequenceHeadline = "Sağlık merkezindeki kriyoterapi seansı sonrası kondisyonun %100'e yaklaştı."
                ),
                DynamicStoryChoice(
                    id = "CH_MED_HIDE_FATIGUE",
                    label = "Kas Ağrılarını Gizle, 'Tamamen Hazırım' De",
                    description = "İlk 11 şansını korursun ancak enerji kaybı yaşarsın.",
                    moraleDelta = +2,
                    energyDelta = -10,
                    managerTrustDelta = +6,
                    teammateAffinityDelta = 0,
                    fanSupportDelta = +3,
                    consequenceHeadline = "Sağlık heyeti maç kadrosunda yer almana şartlı onay verdi."
                )
            )
        ),
        DynamicCareerDilemma(
            id = "DILEMMA_BENCH_CRISIS",
            dayIndex = 4,
            category = "TEKNİK DİREKTÖR GÖRÜŞMESİ",
            title = "Teknik Direktör Seni Yanına Çağırdı!",
            narrative = "$clubName antrenmanında çift kale maç sırasında aynı mevkideki kıdemli takım arkadaşın sert bir müdahalede bulundu. Teknik direktör $managerName düdüğü çalıp seni saha kenarına, taktik tahtasının önüne çağırdı.",
            speakerName = managerName,
            speakerRole = "Teknik Direktör",
            sceneType = "MANAGER_OFFICE",
            locationBadge = "$clubName TESİSLERİ • SAHA KENARI BİREBİR GÖRÜŞME",
            choices = listOf(
                DynamicStoryChoice(
                    id = "CH_PRO",
                    label = "Elini uzat ve taktik disipline odaklan",
                    description = "Profesyonel kalıp hocanın gözünde olgunluk ve İlk 11 puanı kazan.",
                    moraleDelta = +5,
                    energyDelta = 0,
                    managerTrustDelta = +10,
                    teammateAffinityDelta = +7,
                    fanSupportDelta = +3,
                    consequenceHeadline = "Antrenmandaki soğukkanlı tavrın teknik heyetten tam not aldı."
                ),
                DynamicStoryChoice(
                    id = "CH_FIERCE",
                    label = "Formayı sahada söke söke alacağını söyle",
                    description = "Hırsını ortaya koy; hocayı etkiler ama takım arkadaşınla rekabeti kızıştırır.",
                    moraleDelta = +8,
                    energyDelta = -4,
                    managerTrustDelta = +7,
                    teammateAffinityDelta = -5,
                    fanSupportDelta = +6,
                    consequenceHeadline = "İdmandaki iddialı çıkışın kulüp kulislerinde konuşuluyor."
                ),
                DynamicStoryChoice(
                    id = "CH_COMPLAIN",
                    label = "Karara ve sert müdahaleye yüksek sesle itiraz et",
                    description = "Tepkini yüksek sesle dile getir.",
                    moraleDelta = -5,
                    energyDelta = -3,
                    managerTrustDelta = -9,
                    teammateAffinityDelta = -8,
                    fanSupportDelta = -3,
                    consequenceHeadline = "Antrenmandaki tartışma teknik direktör $managerName'i kızdırdı."
                )
            )
        ),
        DynamicCareerDilemma(
            id = "DILEMMA_TACTICAL_ROLE",
            dayIndex = 6,
            category = "TAKTİK & KADRO SEÇİMİ",
            title = "Teknik Direktör Odasında Taktik Toplantı",
            narrative = "$managerName seni odasına davet etti ve analiz ekranında gelecek maçın dizilişini açtı: \"Sana hücum geçişlerinde serbest rol vermeyi düşünüyorum, yoksa savunma yardımını da üstlenip çift yönlü mü oynarsın?\"",
            speakerName = managerName,
            speakerRole = "Teknik Direktör",
            sceneType = "MANAGER_OFFICE",
            locationBadge = "TEKNİK DİREKTÖR ODASI • MAÇ PLANI",
            choices = listOf(
                DynamicStoryChoice(
                    id = "CH_FREE_ROLE",
                    label = "Hücumda serbest yaratıcı rol iste",
                    description = "Skor katkısı ve taraftar coşkusu artar.",
                    moraleDelta = +8,
                    energyDelta = -3,
                    managerTrustDelta = +6,
                    teammateAffinityDelta = +2,
                    fanSupportDelta = +7,
                    consequenceHeadline = "$managerName hücum anahtarını sana teslim etmeye hazırlanıyor!"
                ),
                DynamicStoryChoice(
                    id = "CH_BOX_TO_BOX",
                    label = "Takım için çift yönlü pres sözü ver",
                    description = "Hocanın güveni ve takım kimyası maksimuma çıkar.",
                    moraleDelta = +4,
                    energyDelta = -7,
                    managerTrustDelta = +11,
                    teammateAffinityDelta = +8,
                    fanSupportDelta = +4,
                    consequenceHeadline = "Takım oyununa bağlılığın soyunma odasında alkış topladı."
                )
            )
        ),
        DynamicCareerDilemma(
            id = "DILEMMA_SPONSOR_SHOOT",
            dayIndex = 8,
            category = "MENAJER & TRANSFER MASASI",
            title = "Menajer Kemal Soylu ile Özel Kariyer Zirvesi",
            narrative = "Menajerin Kemal Soylu kulüp binasının VIP locasında dosyaları masaya serdi: \"Transfer dönemi kapanmadan önce hem sponsorluk çekimi (4.500 €) hem de üst lig gözlemcilerine özel portföy sunumu var. Nasıl ilerleyelim?\"",
            speakerName = "Kemal Soylu",
            speakerRole = "Menajer",
            sceneType = "TRANSFER_NEGOTIATION",
            locationBadge = "VIP LOCA • MENAJER KARİYER ZİRVESİ",
            choices = listOf(
                DynamicStoryChoice(
                    id = "CH_ACCEPT_SHOOT",
                    label = "Sponsorluk & PR Paketi İmzala (+4.500 €)",
                    description = "Anında nakit gelir ve sosyal medya patlaması sağlar.",
                    moraleDelta = +7,
                    energyDelta = -10,
                    managerTrustDelta = -2,
                    teammateAffinityDelta = 0,
                    fanSupportDelta = +7,
                    moneyDeltaEur = 4_500L,
                    consequenceHeadline = "Yeni reklam anlaşman banka hesabına +4.500 € kazandırdı."
                ),
                DynamicStoryChoice(
                    id = "CH_DECLINE_FOCUS",
                    label = "Tamamen sahaya ve İlk 11'e odaklan",
                    description = "Dinlenmeni koru ve teknik direktörün takdirini kazan.",
                    moraleDelta = +4,
                    energyDelta = +10,
                    managerTrustDelta = +9,
                    teammateAffinityDelta = +5,
                    fanSupportDelta = +4,
                    moneyDeltaEur = 0L,
                    consequenceHeadline = "Reklam çekimini erteleyip idmana odaklanman hocadan tam not aldı."
                )
            )
        ),
        DynamicCareerDilemma(
            id = "DILEMMA_LOCKER_LEADERSHIP",
            dayIndex = 11,
            category = "SOYUNMA ODASI",
            title = "Soyunma Odasında Kritik Konuşma",
            narrative = "Yoğun fikstür temposunda takım kaptanı soyunma odasında sözü sana bıraktı. Tüm takım gözlerinin içine bakıyor.",
            speakerName = "Takım Kaptanı",
            speakerRole = "Soyunma Odası Lideri",
            sceneType = "LOCKER_ROOM",
            locationBadge = "$clubName SOYUNMA ODASI",
            choices = listOf(
                DynamicStoryChoice(
                    id = "CH_LOCKER_UNITE",
                    label = "\"Bu formanın hakkını birlikte vereceğiz!\"",
                    description = "Takım kimyasını ve morali ateşler.",
                    moraleDelta = +9,
                    energyDelta = +4,
                    managerTrustDelta = +7,
                    teammateAffinityDelta = +10,
                    fanSupportDelta = +5,
                    consequenceHeadline = "Soyunma odasındaki liderlik konuşman takımı kenetledi!"
                ),
                DynamicStoryChoice(
                    id = "CH_LOCKER_DEMAND",
                    label = "\"Herkes sahada %100'ünü vermek zorunda!\"",
                    description = "Ciddiyeti artırır ve teknik direktörün hoşuna gider.",
                    moraleDelta = +4,
                    energyDelta = 0,
                    managerTrustDelta = +9,
                    teammateAffinityDelta = +2,
                    fanSupportDelta = +4,
                    consequenceHeadline = "Disiplin vurgulu konuşman teknik heyetten destek gördü."
                )
            )
        )
    )

    fun buildOnDemandCinematicDilemma(
        type: String,
        dayIndex: Int,
        clubName: String,
        managerName: String
    ): DynamicCareerDilemma {
        val all = buildDynamicDilemmas(clubName, managerName)
        return when (type) {
            "MEDICAL_CHECKUP" -> all.first { it.sceneType == "MEDICAL_CHECKUP" }.copy(
                id = "CIN_MED_D$dayIndex",
                dayIndex = dayIndex
            )
            "MANAGER_OFFICE" -> {
                val mgrDilemmas = all.filter { it.sceneType == "MANAGER_OFFICE" }
                mgrDilemmas[dayIndex % mgrDilemmas.size].copy(
                    id = "CIN_MGR_D$dayIndex",
                    dayIndex = dayIndex
                )
            }
            else -> all[dayIndex % all.size].copy(
                id = "CIN_GEN_D$dayIndex",
                dayIndex = dayIndex
            )
        }
    }
}
