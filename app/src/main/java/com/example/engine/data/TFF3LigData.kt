package com.example.engine.data

import com.example.model.Club
import com.example.model.Position
import com.example.model.SquadMemberInfo

object TFF3LigData {

    const val LEAGUE_ID_ALL = "TR_3_LIG"
    const val LEAGUE_NAME_ALL = "Nesine 3. Lig"

    const val LEAGUE_ID_G1 = "TR_3_LIG_G1"
    const val LEAGUE_NAME_G1 = "Nesine 3. Lig - 1. Grup"

    const val LEAGUE_ID_G2 = "TR_3_LIG_G2"
    const val LEAGUE_NAME_G2 = "Nesine 3. Lig - 2. Grup"

    const val LEAGUE_ID_G3 = "TR_3_LIG_G3"
    const val LEAGUE_NAME_G3 = "Nesine 3. Lig - 3. Grup"

    const val LEAGUE_ID_G4 = "TR_3_LIG_G4"
    const val LEAGUE_NAME_G4 = "Nesine 3. Lig - 4. Grup"

    fun getGroup1Clubs(): List<Club> = listOf(
        Club(
            id = "T3_BURSASPOR",
            name = "Bursaspor",
            shortName = "BRS",
            country = "Türkiye",
            city = "Bursa",
            leagueId = LEAGUE_ID_G1,
            leagueName = LEAGUE_NAME_G1,
            tier = 4,
            primaryColorHex = "#1E8449",
            secondaryColorHex = "#F7FAF8",
            budgetEur = 850_000L,
            facilityQuality = 4,
            teamStrength = 59,
            managerName = "Pablo Martin Batalla",
            managerTacticalStyle = "4-2-3-1 Dominant Pres & Şampiyon Ruhu",
            stadiumName = "Yüzüncü Yıl Atatürk Stadyumu",
            stadiumCapacity = 43361,
            isDemoClub = false,
            keySquadMembers = listOf(
                SquadMemberInfo("BRS_AHMETILHAN", "Ahmet İlhan Özek", Position.WG, 7, 59, 37, isCaptain = true),
                SquadMemberInfo("BRS_MUHAMMET", "Muhammet Demir", Position.ST, 9, 60, 33),
                SquadMemberInfo("BRS_ILHAN", "İlhan Depe", Position.WG, 11, 58, 33),
                SquadMemberInfo("BRS_ANIL", "Anıl Atağ", Position.GK, 1, 57, 34),
                SquadMemberInfo("BRS_MEHMET", "Mehmet Yiğit", Position.CB, 4, 58, 33),
                SquadMemberInfo("BRS_TAHA", "Taha Can Velioğlu", Position.CB, 5, 57, 31),
                SquadMemberInfo("BRS_ABDULLAH", "Abdullah Tazgel", Position.FB, 3, 56, 25),
                SquadMemberInfo("BRS_MUSA", "Musa Çağıran", Position.DM, 6, 58, 33),
                SquadMemberInfo("BRS_YIGITALI", "Yiğitali Bayrak", Position.CM, 8, 57, 29),
                SquadMemberInfo("BRS_SEDAT", "Sedat Cengiz", Position.AM, 10, 58, 32),
                SquadMemberInfo("BRS_HAMZA", "Hamza Gür", Position.FB, 2, 56, 31)
            )
        ),
        Club(
            id = "T3_KARSIYAKA",
            name = "Karşıyaka",
            shortName = "KSK",
            country = "Türkiye",
            city = "İzmir",
            leagueId = LEAGUE_ID_G1,
            leagueName = LEAGUE_NAME_G1,
            tier = 4,
            primaryColorHex = "#1E8449",
            secondaryColorHex = "#922B21",
            budgetEur = 750_000L,
            facilityQuality = 3,
            teamStrength = 58,
            managerName = "Mustafa Reşit Akçay",
            managerTacticalStyle = "4-3-3 Hızlı Kanat & Hücum Presi",
            stadiumName = "Alsancak Mustafa Denizli Stadyumu",
            stadiumCapacity = 15000,
            isDemoClub = false,
            keySquadMembers = listOf(
                SquadMemberInfo("KSK_ENESTAYFUN", "Enes Nalbantoğlu", Position.WG, 7, 58, 30),
                SquadMemberInfo("KSK_ISHAK", "İshak Kurt", Position.ST, 9, 58, 32),
                SquadMemberInfo("KSK_HAKAN", "Hakan Barış", Position.CM, 8, 57, 31, isCaptain = true),
                SquadMemberInfo("KSK_HAYDAR", "Haydar Yılmaz", Position.GK, 1, 56, 41),
                SquadMemberInfo("KSK_ISMAIL", "İsmail Güner", Position.CB, 4, 56, 27),
                SquadMemberInfo("KSK_ABDULKADIR", "Abdülkadir Öksüz", Position.CB, 5, 56, 26),
                SquadMemberInfo("KSK_FERDI", "Ferdi Burgaz", Position.FB, 3, 55, 22),
                SquadMemberInfo("KSK_SADAN", "Sefa Küpeli", Position.DM, 6, 56, 31),
                SquadMemberInfo("KSK_ALI", "Ali Sinan Gayla", Position.WG, 11, 56, 28),
                SquadMemberInfo("KSK_CAGATAY", "Çağatay Yılmaz", Position.AM, 10, 57, 25),
                SquadMemberInfo("KSK_BEDIRHAN", "Bedirhan Altunbaş", Position.FB, 2, 55, 25)
            )
        ),
        createClub("T3_DUZCESPOR", "Düzce Cam Düzcespor", "DÜZ", "Düzce", LEAGUE_ID_G1, LEAGUE_NAME_G1, "#922B21", "#1B4F72", 520_000L, 2, 55, "Kadir Kar", "4-4-2 Sert Savunma & Kontra", "Düzce Şehir Stadyumu", 7500, "Berkay Ekici", 55, Position.ST, 9),
        createClub("T3_TOKAT", "Tokat Belediye Plevnespor", "TKT", "Tokat", LEAGUE_ID_G1, LEAGUE_NAME_G1, "#78281F", "#F7FAF8", 480_000L, 2, 54, "Cüneyt Biçer", "4-3-3 Dengeli", "Gaziosmanpaşa Stadyumu", 6000, "Muhammed Emir Özbilen", 54, Position.ST, 9),
        createClub("T3_MUGLASPOR", "Muğlaspor", "MĞL", "Muğla", LEAGUE_ID_G1, LEAGUE_NAME_G1, "#1E8449", "#F7FAF8", 500_000L, 2, 55, "Besim Durmuş", "4-2-3-1 Pas Oyunu", "Muğla Atatürk Stadyumu", 6500, "Furkan Demir", 55, Position.WG, 7),
        createClub("T3_SILIFKE", "Silifke Belediye Spor", "SLF", "Mersin", LEAGUE_ID_G1, LEAGUE_NAME_G1, "#1F618D", "#922B21", 460_000L, 2, 54, "İlker Avcıbay", "4-4-2 Mücadeleci", "Silifke Şehir Stadyumu", 5000, "Burak Bilgen", 54, Position.CM, 8),
        createClub("T3_HOPASPOR", "Artvin Hopaspor", "HOP", "Artvin", LEAGUE_ID_G1, LEAGUE_NAME_G1, "#6C3483", "#F7FAF8", 440_000L, 2, 53, "Osman Özdemir", "4-5-1 Dar Alan", "Hopa İlçe Stadyumu", 4000, "Gökhan Günel", 53, Position.CB, 4),
        createClub("T3_KUSADASI", "Kuşadasıspor", "KŞD", "Aydın", LEAGUE_ID_G1, LEAGUE_NAME_G1, "#17202A", "#F7FAF8", 420_000L, 2, 52, "Bayram Sevindik", "4-3-3 Kontra", "Özer Türk Stadyumu", 4000, "Batuhan Önal", 52, Position.ST, 11)
    )

    fun getGroup2Clubs(): List<Club> = listOf(
        createClub("T3_BALIKESIR", "Balıkesirspor", "BAL", "Balıkesir", LEAGUE_ID_G2, LEAGUE_NAME_G2, "#922B21", "#F7FAF8", 580_000L, 3, 56, "Levent Eriş", "4-2-3-1 Tecrübeli Kadro & Pres", "Balıkesir Atatürk Stadyumu", 15000, "Sedat Yiğit Kurnaz", 56, Position.ST, 9),
        createClub("T3_USAKSPOR", "Uşakspor", "UŞK", "Uşak", LEAGUE_ID_G2, LEAGUE_NAME_G2, "#922B21", "#17202A", 540_000L, 2, 55, "Efrahim Alkın", "4-3-3 Kanat Akınları", "Uşak 1 Eylül Stadyumu", 8000, "Murat Torun", 55, Position.ST, 11),
        createClub("T3_FATSA", "Fatsa Belediyespor", "FAT", "Ordu", LEAGUE_ID_G2, LEAGUE_NAME_G2, "#1E8449", "#1B4F72", 480_000L, 2, 54, "Bekir Ozan Has", "4-4-2 Sert Savunma", "Fatsa İlçe Stadyumu", 4000, "Hasan Çavdar", 54, Position.AM, 10),
        createClub("T3_CAYELI", "Çayelispor", "ÇAY", "Rize", LEAGUE_ID_G2, LEAGUE_NAME_G2, "#1E8449", "#F7FAF8", 450_000L, 2, 53, "Şevki Tonyalı", "4-2-3-1 Tempolu", "Çayeli İlçe Stadyumu", 3000, "Sinan Kalaycı", 53, Position.CM, 8),
        createClub("T3_AMASYA", "Amasyaspor FK", "AMS", "Amasya", LEAGUE_ID_G2, LEAGUE_NAME_G2, "#1E8449", "#F7FAF8", 460_000L, 2, 53, "Zafer Uysal", "4-3-3 Geçiş", "12 Haziran Stadyumu", 7000, "Ahmet Buğrahan Tuncay", 53, Position.WG, 7),
        createClub("T3_ETIMESGUT", "Etimesgut Belediyespor", "ETM", "Ankara", LEAGUE_ID_G2, LEAGUE_NAME_G2, "#17202A", "#F7FAF8", 500_000L, 3, 54, "Murat Şenvardar", "4-4-2 Kompakt", "Etimesgut Atatürk", 3500, "Turan Yılmaz", 54, Position.ST, 9),
        createClub("T3_SILIVRI", "Silivrispor", "SLV", "İstanbul", LEAGUE_ID_G2, LEAGUE_NAME_G2, "#1B4F72", "#922B21", 490_000L, 2, 54, "Özgür Ergün", "4-2-3-1 Disiplinli", "Silivri Müjdat Gürsu", 4000, "Mert Aktaş", 54, Position.WG, 11),
        createClub("T3_TIRE", "Tire 2021 FK", "TİR", "İzmir", LEAGUE_ID_G2, LEAGUE_NAME_G2, "#F4D03F", "#922B21", 450_000L, 2, 53, "Galip Gündoğdu", "4-3-3 Pas", "Tire Gazi Mustafa Kemal", 15000, "Murat Elkatmış", 53, Position.AM, 10)
    )

    fun getGroup3Clubs(): List<Club> = listOf(
        createClub("T3_KUCUKCEKMECE", "Küçükçekmece Sinopspor", "KÇS", "İstanbul", LEAGUE_ID_G3, LEAGUE_NAME_G3, "#F4D03F", "#1B4F72", 520_000L, 2, 55, "Galip Aslıyüksek", "4-2-3-1 Agresif Pres", "Küçükçekmece Metin Oktay", 3500, "Enes İslam İlkin", 55, Position.WG, 7),
        createClub("T3_AYVALIK", "Ayvalıkgücü Belediyespor", "AYV", "Balıkesir", LEAGUE_ID_G3, LEAGUE_NAME_G3, "#922B21", "#F7FAF8", 500_000L, 2, 54, "Mesut Toros", "4-3-3 Hızlı Kanat", "Hüsnü Uğural Stadyumu", 3500, "Tugay Yıldırım", 54, Position.CM, 8),
        createClub("T3_52ORDU", "52 Orduspor FK", "52O", "Ordu", LEAGUE_ID_G3, LEAGUE_NAME_G3, "#6C3483", "#F7FAF8", 550_000L, 3, 55, "Cahit Erçevik", "4-2-3-1 Hücum Ağırlıklı", "Yeni Ordu Stadyumu", 16000, "Nevzat Bilen", 55, Position.WG, 11),
        createClub("T3_CORLU", "Çorluspor 1947", "ÇRL", "Tekirdağ", LEAGUE_ID_G3, LEAGUE_NAME_G3, "#922B21", "#17202A", 510_000L, 2, 54, "Ersin Aka", "4-4-2 Fizik Mücadelesi", "General Basri Saran", 5000, "Yasin Abdioğlu", 54, Position.ST, 9),
        createClub("T3_KARABUK", "Karabük İdman Yurdu", "KBK", "Karabük", LEAGUE_ID_G3, LEAGUE_NAME_G3, "#1B4F72", "#922B21", 470_000L, 2, 53, "Metin İlhan", "4-3-3 Kontra", "Dr. Necmettin Şeyhoğlu", 5000, "Yusuf Akbulut", 53, Position.ST, 9),
        createClub("T3_PAZARSPOR", "Pazarspor", "PAZ", "Rize", LEAGUE_ID_G3, LEAGUE_NAME_G3, "#1F618D", "#F7FAF8", 460_000L, 2, 53, "Ramazan Öztürk", "4-5-1 Savunma", "Pazar İlçe Stadyumu", 3500, "Emre Turan", 53, Position.CB, 4),
        createClub("T3_OSMANIYE", "Osmaniyespor FK", "OSM", "Osmaniye", LEAGUE_ID_G3, LEAGUE_NAME_G3, "#F4D03F", "#1E8449", 450_000L, 2, 53, "Gürkan Çavdar", "4-4-2 Mücadele", "7 Ocak Stadyumu", 6000, "Taylan Özgün", 53, Position.ST, 9),
        createClub("T3_YOZGAT", "Yozgat Bld Bozokspor", "YOZ", "Yozgat", LEAGUE_ID_G3, LEAGUE_NAME_G3, "#922B21", "#17202A", 440_000L, 2, 52, "İlhan Özbay", "4-2-3-1 Kapalı", "Bozok Şehir Stadyumu", 5000, "Murat Kara", 52, Position.ST, 10)
    )

    fun getGroup4Clubs(): List<Club> = listOf(
        createClub("T3_ORDU1967", "Orduspor 1967 SK", "ORD", "Ordu", LEAGUE_ID_G4, LEAGUE_NAME_G4, "#6C3483", "#F7FAF8", 560_000L, 3, 56, "Ramazan Çelik", "4-2-3-1 Yüksek Tempo Hücum", "Yeni Ordu Stadyumu", 16000, "Cevatcan Ekinci", 56, Position.WG, 7),
        createClub("T3_SEBAT", "Sebat Gençlik Spor", "SBT", "Trabzon", LEAGUE_ID_G4, LEAGUE_NAME_G4, "#922B21", "#1B4F72", 550_000L, 3, 55, "Adem Çak", "4-3-3 Dinamik Karadeniz Presi", "Ortahisar Yavuz Selim", 5000, "Veysel Sönmezsoy", 55, Position.AM, 10),
        createClub("T3_ZONGULDAK", "Zonguldak Kömürspor", "ZNG", "Zonguldak", LEAGUE_ID_G4, LEAGUE_NAME_G4, "#922B21", "#1B4F72", 530_000L, 2, 55, "Can Güven", "4-4-2 Madenci Ruhu & Mücadele", "Karaelmas Kemal Köksal", 13795, "Mehmet Gürkan Öztürk", 55, Position.ST, 9),
        createClub("T3_MARASISTIKLAL", "K.Maraş İstiklalspor", "İST", "Kahramanmaraş", LEAGUE_ID_G4, LEAGUE_NAME_G4, "#922B21", "#F7FAF8", 540_000L, 2, 55, "Ahmet Taşyürek", "4-2-3-1 Deprem Sonrası Kenetlenme", "Batıpark Sahası", 5000, "Ali Han Tunçer", 55, Position.WG, 11),
        createClub("T3_MARDIN", "Mardin 1969 Spor", "MAR", "Mardin", LEAGUE_ID_G4, LEAGUE_NAME_G4, "#922B21", "#17202A", 500_000L, 2, 54, "Ulaş Şengel", "4-3-3 Sert Pres", "21 Kasım Stadyumu", 6000, "Serdar Yiğit", 54, Position.ST, 9),
        createClub("T3_KIRIKKALE", "Kırıkkale FK", "KRK", "Kırıkkale", LEAGUE_ID_G4, LEAGUE_NAME_G4, "#1B4F72", "#922B21", 470_000L, 2, 53, "Ercümend Coşkundere", "4-4-2 Alan Daraltma", "Başpınar Stadyumu", 5000, "Canberk Özçakır", 53, Position.CM, 8),
        createClub("T3_BORNOVA1877", "Bornova 1877", "BRN", "İzmir", LEAGUE_ID_G4, LEAGUE_NAME_G4, "#1E8449", "#17202A", 480_000L, 2, 54, "Sercan Yıldırım", "4-3-3 Genç Dinamizm", "Bornova Aziz Kocaoğlu", 4000, "Yasin Uzunoğlu", 54, Position.ST, 9),
        createClub("T3_EDIRNE", "Edirnespor", "EDR", "Edirne", LEAGUE_ID_G4, LEAGUE_NAME_G4, "#F4D03F", "#922B21", 440_000L, 2, 52, "Kadir Kar", "4-5-1 Savunma", "25 Kasım Stadyumu", 4000, "Uğur Erk", 52, Position.FB, 3)
    )

    fun getAllClubs(): List<Club> = getGroup1Clubs() + getGroup2Clubs() + getGroup3Clubs() + getGroup4Clubs()

    private fun createClub(
        id: String,
        name: String,
        shortName: String,
        city: String,
        leagueId: String,
        leagueName: String,
        primaryHex: String,
        secondaryHex: String,
        budget: Long,
        facility: Int,
        strength: Int,
        manager: String,
        style: String,
        stadium: String,
        capacity: Int,
        starName: String,
        starOvr: Int,
        starPos: Position,
        starNum: Int
    ): Club {
        val squad = buildSquad(id, strength, starName, starOvr, starPos, starNum)
        return Club(
            id = id,
            name = name,
            shortName = shortName,
            country = "Türkiye",
            city = city,
            leagueId = leagueId,
            leagueName = leagueName,
            tier = 4,
            primaryColorHex = primaryHex,
            secondaryColorHex = secondaryHex,
            budgetEur = budget,
            facilityQuality = facility,
            teamStrength = strength,
            managerName = manager,
            managerTacticalStyle = style,
            stadiumName = stadium,
            stadiumCapacity = capacity,
            isDemoClub = false,
            keySquadMembers = squad
        )
    }

    private fun buildSquad(
        clubId: String,
        strength: Int,
        starName: String,
        starOvr: Int,
        starPos: Position,
        starNum: Int
    ): List<SquadMemberInfo> {
        val baseNames = listOf(
            Pair("Serkan", "Acar"), Pair("Onur", "Yıldız"), Pair("Batuhan", "Tekin"),
            Pair("Oğuz", "Erdoğan"), Pair("Kaan", "Güneş"), Pair("Barış", "Çetin"),
            Pair("Umut", "Korkmaz"), Pair("Eren", "Yalçın"), Pair("Tolgahan", "Avcı"),
            Pair("Semih", "Polat"), Pair("Samet", "Gökçe")
        )
        val positions = listOf(
            Position.GK, Position.FB, Position.CB, Position.CB, Position.FB,
            Position.DM, Position.CM, Position.WG, Position.AM, Position.WG, Position.ST
        )
        val numbers = listOf(1, 2, 4, 5, 3, 6, 8, 7, 10, 11, 9)
        val hash = clubId.hashCode().let { if (it < 0) -it else it }

        val list = mutableListOf<SquadMemberInfo>()
        list.add(
            SquadMemberInfo(
                id = "${clubId}_STAR",
                name = starName,
                position = starPos,
                kitNumber = starNum,
                overall = starOvr,
                age = 25 + (hash % 8),
                isCaptain = true
            )
        )

        positions.forEachIndexed { i, pos ->
            if (pos != starPos || list.size < 2) {
                val pair = baseNames[(hash + i) % baseNames.size]
                val ovr = (strength - 2 + (i % 4)).coerceIn(44, 56)
                val kit = numbers[i]
                if (kit != starNum && list.none { it.kitNumber == kit }) {
                    list.add(
                        SquadMemberInfo(
                            id = "${clubId}_P$i",
                            name = "${pair.first} ${pair.second}",
                            position = pos,
                            kitNumber = kit,
                            overall = ovr,
                            age = 20 + ((hash + i) % 12)
                        )
                    )
                }
            }
        }
        return list.take(11)
    }
}
