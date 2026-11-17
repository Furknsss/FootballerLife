package com.example.engine.data

import com.example.model.Club
import com.example.model.Position
import com.example.model.SquadMemberInfo

object TFF2LigData {

    const val LEAGUE_ID_BEYAZ = "TR_2_LIG_BEYAZ"
    const val LEAGUE_NAME_BEYAZ = "Nesine 2. Lig - Beyaz Grup"

    const val LEAGUE_ID_KIRMIZI = "TR_2_LIG_KIRMIZI"
    const val LEAGUE_NAME_KIRMIZI = "Nesine 2. Lig - Kırmızı Grup"

    const val LEAGUE_ID_ALL = "TR_2_LIG"
    const val LEAGUE_NAME_ALL = "Nesine 2. Lig"

    fun getBeyazClubs(): List<Club> = listOf(
        createClub("T2B_BATMAN", "Batman Petrolspor", "BTM", "Batman", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#922B21", "#F7FAF8", 1_450_000L, 3, 64, "Şenol Fidan", "4-2-3-1 Tempolu Hücum", "Batman Yeni Şehir Stadyumu", 15000, "Atabey Çiçek", 64, Position.ST, 9),
        createClub("T2B_SARIYER", "Sarıyer SK", "SRY", "İstanbul", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#1B4F72", "#F7FAF8", 1_500_000L, 3, 64, "Servet Çetin", "4-3-3 Pas ve Pozisyon", "Yusuf Ziya Öniş Stadyumu", 6500, "Kemal Rüzgar", 64, Position.ST, 10),
        createClub("T2B_KASTAMONU", "GMG Kastamonuspor", "KST", "Kastamonu", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#922B21", "#17202A", 1_350_000L, 3, 64, "Adem Çağlayan", "4-2-3-1 Disiplinli Blok", "Gazi Stadyumu", 7000, "Erhan Kartal", 63, Position.FB, 2),
        createClub("T2B_ALTINORDU", "Altınordu", "ATO", "İzmir", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#922B21", "#1B4F72", 1_250_000L, 4, 63, "Olcay Şahan", "4-3-3 Genç Akademi Presi", "Bornova Aziz Kocaoğlu", 15000, "Kaan Baysal", 63, Position.CM, 8),
        createClub("T2B_ISKENDERUN", "İskenderunspor", "İSK", "Hatay", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#D35400", "#1F618D", 1_200_000L, 3, 63, "Gürkan Aslan", "4-2-3-1 Hızlı Geçiş", "Kırıkhan İlçe Stadyumu", 6000, "Ahmet Sivri", 63, Position.ST, 9),
        createClub("T2B_ADANA01", "Adana 01 FK", "A01", "Adana", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#F4D03F", "#17202A", 1_150_000L, 2, 63, "Volkan Bekiroğlu", "4-4-2 Mücadeleci & Sert", "Tarsus Şehir Stadyumu", 6200, "Samet Ali Kaya", 62, Position.ST, 11),
        createClub("T2B_ANKARASPOR", "Sincan Bld Ankaraspor", "ANK", "Ankara", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#1B4F72", "#F7FAF8", 1_100_000L, 3, 62, "Bayram Toysal", "4-2-3-1 Dengeli", "Etimesgut Atatürk Stadı", 5000, "Batuhan Altıntaş", 62, Position.ST, 9),
        createClub("T2B_24ERZINCAN", "Anagold 24Erzincanspor", "ERZ", "Erzincan", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#922B21", "#17202A", 1_050_000L, 3, 62, "Gökhan Ünal", "4-3-3 Yüksek Enerji", "Erzincan 13 Şubat Şehir", 12500, "Yasin Dülger", 62, Position.WG, 7),
        createClub("T2B_1461TRABZON", "1461 Trabzon FK", "146", "Trabzon", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#78281F", "#2874A6", 1_100_000L, 3, 62, "Ulaş Ulusan", "4-2-3-1 Dinamik Geçiş", "Ortahisar Yavuz Selim", 4500, "Enes Karakuş", 62, Position.ST, 10),
        createClub("T2B_KIRKLARELI", "Kırklarelispor", "KRK", "Kırklareli", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#1E8449", "#F7FAF8", 980_000L, 2, 61, "Bilgin Erdem", "4-4-2 Kompakt Savunma", "Kırklareli Atatürk Stadı", 4000, "Okan Derici", 61, Position.AM, 10),
        createClub("T2B_FETHIYE", "Fethiyespor", "FTH", "Muğla", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#1B4F72", "#F7FAF8", 1_000_000L, 2, 61, "Selahaddin Dinçel", "4-2-3-1 Kanat Bindirmeleri", "Fethiye İlçe Stadyumu", 6000, "Ali Ceylan", 61, Position.WG, 7),
        createClub("T2B_ALTAY", "Altay", "ALT", "İzmir", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#17202A", "#F7FAF8", 950_000L, 3, 61, "Gökhan Karaaslan", "4-3-3 Gençlerle Var Olma", "Alsancak Mustafa Denizli", 15000, "Ceyhun Gülselam", 62, Position.DM, 6),
        createClub("T2B_INEGOL", "İnegölspor", "İNG", "Bursa", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#78281F", "#1B4F72", 960_000L, 2, 61, "Sinan Kolukısa", "4-4-2 Sert & Fiziksel", "İnegöl İlçe Stadyumu", 4500, "Fatih Aktay", 61, Position.ST, 9),
        createClub("T2B_ISPARTA32", "Isparta 32 Spor", "ISP", "Isparta", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#1E8449", "#922B21", 940_000L, 2, 61, "Koray Palaz", "4-2-3-1 Savunma Güvenliği", "Isparta Atatürk Şehir", 8000, "Zihni Temelci", 60, Position.CM, 8),
        createClub("T2B_KEPEZ", "Kepezspor", "KPZ", "Antalya", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#922B21", "#1B4F72", 900_000L, 2, 60, "Çağdaş Mavioğlu", "4-3-3 Kontraatak", "Hasan Doğan Stadyumu", 3500, "Ahmet Teker", 60, Position.WG, 11),
        createClub("T2B_KARAKOPRU", "Karaköprü Belediyespor", "KKP", "Şanlıurfa", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#922B21", "#17202A", 850_000L, 2, 60, "İbrahim Ülüm", "4-4-2 Alan Daraltma", "Faruk Çelik Stadyumu", 3000, "Halil Köçer", 59, Position.CM, 5),
        createClub("T2B_BEYKOZ", "Beykoz Anadolu Spor", "BYK", "İstanbul", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#F4D03F", "#17202A", 880_000L, 2, 59, "Eren Şafak", "4-2-3-1 Dengeli", "Tuzla Belediye Stadı", 5000, "Ozan Papaker", 60, Position.ST, 19),
        createClub("T2B_AFYONSPOR", "Hes İlaç Afyonspor", "AFY", "Afyon", LEAGUE_ID_BEYAZ, LEAGUE_NAME_BEYAZ, "#6C3483", "#F7FAF8", 750_000L, 2, 58, "Fatih Şen", "4-5-1 Genç Kadro Mücadelesi", "Zafer Stadyumu", 15000, "Mustafa Erkasap", 58, Position.ST, 9)
    )

    fun getKirmiziClubs(): List<Club> = listOf(
        createClub("T2K_SERIK", "Serik Belediyespor", "SRK", "Antalya", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#1E8449", "#F7FAF8", 1_550_000L, 3, 64, "Sergey Biryulin", "4-2-3-1 Agresif Pres & Hücum", "İsmail Oğan Stadyumu", 3500, "Erkan Eyibil", 64, Position.AM, 10),
        createClub("T2K_AKSARAY", "Motolux 68 Aksarayspor", "AKS", "Aksaray", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#17202A", "#F7FAF8", 1_450_000L, 3, 64, "Şaban Yıldırım", "4-3-3 Direkt Kanat Akını", "Dağılgan Stadyumu", 7000, "İbrahim Yılmaz", 64, Position.ST, 9),
        createClub("T2K_ELAZIG", "Çimentaş Elazığspor", "ELZ", "Elazığ", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#78281F", "#F7FAF8", 1_400_000L, 3, 64, "Bülent Yenihayat", "4-2-3-1 Yüksek Tempo & Taraftar Baskısı", "Elazığ Atatürk Stadyumu", 18400, "Beykan Şimşek", 64, Position.AM, 10),
        createClub("T2K_VANSPOR", "Artı Değer Vanspor FK", "VAN", "Van", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#922B21", "#17202A", 1_380_000L, 3, 64, "Ümit Bozkurt", "4-3-3 Kanat Geçişleri", "Van Atatürk Stadyumu", 10500, "Fuat Bavuk", 64, Position.ST, 9),
        createClub("T2K_MENEMEN", "Menemen FK", "MNM", "İzmir", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#F4D03F", "#1B4F72", 1_350_000L, 3, 63, "Burhanettin Basatemür", "4-2-3-1 Pozisyonel Pas", "Menemen İlçe Stadyumu", 5000, "Mertçan Açıkgöz", 63, Position.WG, 7),
        createClub("T2K_BUCA", "Bucaspor 1928", "BUC", "İzmir", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#F4D03F", "#1B4F72", 1_300_000L, 3, 63, "Uğur Balcıoğlu", "4-2-3-1 Hızlı Kanat Baskısı", "Yeni Buca Stadyumu", 8000, "Yasin Öztekin", 64, Position.WG, 11),
        createClub("T2K_KARACABEY", "Karacabey Belediyespor", "KRC", "Bursa", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#1B4F72", "#17202A", 1_100_000L, 2, 61, "Erdoğan Yılmaz", "4-4-2 Fiziksel Baskı", "M. Fehmi Gerçeker Stadı", 3000, "Ali Aydemir", 61, Position.ST, 9),
        createClub("T2K_BEYOGLU", "Beyoğlu Yeni Çarşı", "BYC", "İstanbul", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#1B4F72", "#922B21", 1_120_000L, 2, 61, "Efe İnanç", "4-3-3 Hızlı Kontra", "Beyoğlu Stadyumu", 4000, "Sedat Komi", 61, Position.CM, 8),
        createClub("T2K_ANKARADEMIR", "Ankara Demirspor", "AND", "Ankara", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#1B4F72", "#17202A", 1_080_000L, 3, 61, "Abdul Fettah Kaldırım", "4-2-3-1 Kompakt & Sert", "TCDD Ankara Demirspor Stadı", 3000, "Mert Örnek", 61, Position.WG, 7),
        createClub("T2K_KARAMAN", "Karaman FK", "KRM", "Karaman", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#922B21", "#F7FAF8", 1_020_000L, 2, 60, "Mehmet Yıldırım", "4-4-2 Dengeli", "Kemal Kaynaş Stadyumu", 5000, "İlke Tankul", 60, Position.ST, 9),
        createClub("T2K_ERBAA", "Merkür Jet Erbaaspor", "ERB", "Tokat", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#1B4F72", "#F7FAF8", 980_000L, 2, 60, "Ahmet Cingöz", "4-3-3 Dinamik Pres", "Erbaa İlçe Stadyumu", 3500, "Oktay Balcı", 60, Position.ST, 11),
        createClub("T2K_ARNAVUTKOY", "Kuzey Marmara Arnavutköy Bld", "ARN", "İstanbul", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#1E8449", "#1B4F72", 1_050_000L, 2, 60, "İlker Püren", "4-2-3-1 Geçiş Oyunu", "Bolluca Stadyumu", 3000, "Ömer Faruk Ermeç", 60, Position.ST, 9),
        createClub("T2K_DIYARBEKIR", "Diyarbekirspor", "DYR", "Diyarbakır", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#1E8449", "#922B21", 950_000L, 2, 60, "Mustafa Kalafatoğlu", "4-4-2 Mücadeleci", "Seyrantepe Spor Kompleksi", 10000, "Mansur Çalar", 60, Position.CM, 8),
        createClub("T2K_DERINCE", "Belediye Derincespor", "DER", "Kocaeli", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#F4D03F", "#17202A", 900_000L, 2, 59, "Nuri Çolak", "4-5-1 Savunma Güvenliği", "Derince Doğal Çim Stadı", 2500, "Berat Genç", 59, Position.CM, 6),
        createClub("T2K_SOMASPOR", "Somaspor", "SOM", "Manisa", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#17202A", "#F7FAF8", 880_000L, 2, 59, "Erman Güraçar", "4-3-3 Genç Oyuncularla Pres", "Soma Atatürk Stadyumu", 3000, "Yusuf Delen", 59, Position.CB, 4),
        createClub("T2K_MERSINIDMAN", "Yeni Mersin İdmanyurdu", "MİD", "Mersin", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#922B21", "#1B4F72", 920_000L, 2, 59, "Ergün Penbe", "4-2-3-1 Taraftar Gücü & Kontra", "Mersin Stadyumu", 10000, "Ferhan Evren", 59, Position.AM, 10),
        createClub("T2K_NAZILLI", "Nazillispor", "NAZ", "Aydın", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#17202A", "#F7FAF8", 820_000L, 2, 58, "Yüksel Yeşilova", "4-4-2 Alan Kapatma", "Nazilli İlçe Stadyumu", 4500, "Miraç Furkan Türköz", 58, Position.ST, 9),
        createClub("T2K_GIRESUN", "Giresunspor", "GRS", "Giresun", LEAGUE_ID_KIRMIZI, LEAGUE_NAME_KIRMIZI, "#1E8449", "#F7FAF8", 850_000L, 3, 58, "Metin Aydın", "4-5-1 Altyapı Direnci", "Çotanak Spor Kompleksi", 21500, "Şahin Dik", 59, Position.WG, 7)
    )

    fun getAllClubs(): List<Club> = getBeyazClubs() + getKirmiziClubs()

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
            tier = 3,
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
            Pair("Tolga", "Yılmaz"), Pair("Emre", "Demir"), Pair("Burak", "Kaya"),
            Pair("Oğuzhan", "Çelik"), Pair("Fatih", "Şahin"), Pair("Alperen", "Öztürk"),
            Pair("Caner", "Arslan"), Pair("Serhat", "Aydın"), Pair("Yusuf", "Koç"),
            Pair("Berkay", "Doğan"), Pair("Mert", "Kurt")
        )
        val positions = listOf(
            Position.GK, Position.FB, Position.CB, Position.CB, Position.FB,
            Position.DM, Position.CM, Position.WG, Position.AM, Position.WG, Position.ST
        )
        val numbers = listOf(1, 2, 4, 5, 3, 6, 8, 7, 10, 11, 9)
        val hash = clubId.hashCode().let { if (it < 0) -it else it }

        val list = mutableListOf<SquadMemberInfo>()
        // First add star
        list.add(
            SquadMemberInfo(
                id = "${clubId}_STAR",
                name = starName,
                position = starPos,
                kitNumber = starNum,
                overall = starOvr,
                age = 26 + (hash % 6),
                isCaptain = true
            )
        )

        positions.forEachIndexed { i, pos ->
            if (pos != starPos || list.size < 2) {
                val pair = baseNames[(hash + i) % baseNames.size]
                val ovr = (strength - 2 + (i % 4)).coerceIn(50, 64)
                val kit = numbers[i]
                if (kit != starNum && list.none { it.kitNumber == kit }) {
                    list.add(
                        SquadMemberInfo(
                            id = "${clubId}_P$i",
                            name = "${pair.first} ${pair.second}",
                            position = pos,
                            kitNumber = kit,
                            overall = ovr,
                            age = 21 + ((hash + i) % 11)
                        )
                    )
                }
            }
        }
        return list.take(11)
    }
}
