package com.example.engine.social

import com.example.model.CareerSaveState
import com.example.model.ChatMessage
import com.example.model.Club
import com.example.model.InstaDmThread
import com.example.model.PlayerProfile
import com.example.model.SocialMediaPost
import com.example.model.StoryItem
import com.example.model.XPostItem
import com.example.ui.components.FLFormatters

/**
 * Lig Kademesi (Tier 1: Süper Lig .. Tier 5: BAL / Amatör)
 */
enum class LeagueCompetitionTier(
    val tierNumber: Int,
    val codeName: String,
    val titleTr: String,
    val isLowerTier: Boolean
) {
    SUPER_LIG(1, "TR_SUPER_LIG", "Trendyol Süper Lig", false),
    TFF_1_LIG(2, "TR_1_LIG", "Trendyol 1. Lig", true),
    TFF_2_LIG(3, "TR_2_LIG", "Nesine 2. Lig", true),
    TFF_3_LIG(4, "TR_3_LIG", "Nesine 3. Lig", true),
    BAL_AMATEUR(5, "TR_BAL", "Bölgesel Amatör Lig", true);

    companion object {
        fun fromLeagueId(leagueId: String): LeagueCompetitionTier {
            val upper = leagueId.uppercase()
            return when {
                upper == "TR_SUPER_LIG" || upper.contains("SUPER_LIG") -> SUPER_LIG
                upper == "TR_1_LIG" || upper.contains("1_LIG") -> TFF_1_LIG
                upper.startsWith("TR_2_LIG") || upper.contains("2_LIG") -> TFF_2_LIG
                upper.startsWith("TR_3_LIG") || upper.contains("3_LIG") -> TFF_3_LIG
                else -> BAL_AMATEUR
            }
        }
    }
}

/**
 * Yerel Medya Bilgisi
 */
data class LocalMediaOutletInfo(
    val name: String,
    val handle: String,
    val avatarHex: String,
    val bio: String,
    val city: String,
    val followersText: String
)

/**
 * Denetim ve Test Sonucu Raporu
 */
data class SocialRealismAuditReport(
    val passed: Boolean,
    val currentLeagueTier: LeagueCompetitionTier,
    val canAccessSuperstars: Boolean,
    val violationsFound: List<String>
)

/**
 * Sosyal Medya ve DM Gerçekçilik Motoru (Strict Tier Gating).
 * 
 * Kural 1: Alt Liglerde (Tier 5 BAL/Amatör, Tier 4 3. Lig, Tier 3 2. Lig, Tier 2 1. Lig):
 * - Arda Güler, Real Madrid, Fabrizio Romano, beIN Sports ana yayıncıları veya global yabancı modeller
 *   ASLA kullanıcıya DM atamaz, etiketleyemez veya onun hakkında konuşamaz.
 * - Alt liglerde sadece yerel il/ilçe gazetecileri, takım arkadaşları, kulüp amigosu,
 *   alt lig scoutları ve yerel taraftarlar mesaj atabilir/konuşabilir.
 * - Kız arkadaş ve DM havuzunda: Mahalleden lise aşkı, üniversiteden arkadaşı veya yerel esnaf kızı olabilir;
 *   dünyaca ünlü modeller (Chloé Dubois vb.) ASLA alt ligde yazılamaz.
 * 
 * Kural 2: Sadece Süper Lig'e çıkıldığında, A Milli Takım'a davet alındığında veya şöhret kademesi
 * SUPER_LIG_STAR / ICON seviyesine ulaştığında ulusal yıldızlar, Arda Güler ve ünlü modeller açılır.
 */
object SocialRealismEngine {

    val RESTRICTED_GLOBAL_HANDLES = setOf(
        "ardaguler",
        "realmadrid",
        "fabrizioromano",
        "beinsportstr",
        "beinsports_tr",
        "chloedubois",
        "pumafootball",
        "marcorossi_scout",
        "selinderin_official",
        "melisvural",
        "derinkarahan",
        "hakancalhanoglu",
        "kenanyildiz_official",
        "keremakturkoglu",
        "niketurkiye",
        "elvinyildirim",
        "433",
        "yagosabuncuoglu"
    )

    fun getLeagueTier(leagueId: String): LeagueCompetitionTier {
        return LeagueCompetitionTier.fromLeagueId(leagueId)
    }

    fun isLowerTier(leagueId: String): Boolean {
        return LeagueCompetitionTier.fromLeagueId(leagueId).isLowerTier
    }

    /**
     * Kullanıcının dünya çapında veya ulusal süperstarlarla (Arda Güler, Fabrizio Romano, Chloé Dubois vb.)
     * doğrudan etkileşime girip giremeyeceğini denetler.
     */
    fun canAccessSuperstarsAndGlobalMedia(state: CareerSaveState): Boolean {
        val leagueTier = getLeagueTier(state.currentClub.leagueId)
        val p = state.player
        val hasNationalTeamHonor = p.nationalTeamCaps > 0 || p.nationalTeamGoals > 0
        val fameTier = SocialWorldGenerator.calculatePlayerFameTier(state)
        val isSuperLigStarOrIcon = fameTier == PlayerFameTier.SUPER_LIG_STAR ||
                fameTier == PlayerFameTier.NATIONAL_HERO ||
                fameTier == PlayerFameTier.GLOBAL_SUPERSTAR

        // Alt liglerdeyken (BAL, 3. Lig, 2. Lig, 1. Lig) milli takım daveti veya Süper Lig starı olmadan ASLA açılamaz!
        if (leagueTier.isLowerTier) {
            return hasNationalTeamHonor || isSuperLigStarOrIcon
        }

        // Süper Lig'deyse ve yeterli kademeye ulaştıysa açık
        return true
    }

    /**
     * Belirtilen hesabın kullanıcının mevcut kariyer kademesinde kullanıcıya DM atıp atamayacağını kontrol eder.
     */
    fun isHandleAllowedToMessageUser(handle: String, state: CareerSaveState): Boolean {
        val clean = handle.removePrefix("@").lowercase().trim()
        if (canAccessSuperstarsAndGlobalMedia(state)) {
            return true
        }
        return clean !in RESTRICTED_GLOBAL_HANDLES
    }

    /**
     * Belirtilen kulübün bulunduğu il veya ilçeye özel gerçekçi yerel medya organını üretir.
     */
    fun resolveLocalMediaForClub(club: Club): LocalMediaOutletInfo {
        val cityClean = club.city.trim()
        val short = club.shortName.trim()

        return when {
            cityClean.contains("Kütahya", ignoreCase = true) -> LocalMediaOutletInfo(
                name = "Kütahya Spor Postası",
                handle = "@kutahya_spor_postasi",
                avatarHex = "#1E3A8A",
                bio = "Kütahya ili ve ilçeleri futbol haberleri, Belediye Kütahyaspor günlüğü 🗞️⚽",
                city = "Kütahya",
                followersText = "6.420"
            )
            cityClean.contains("Çorum", ignoreCase = true) -> LocalMediaOutletInfo(
                name = "Çorum Hakimiyet Spor",
                handle = "@corum_hakimiyet",
                avatarHex = "#B91C1C",
                bio = "Çorum ve çevre iller yerel spor basını, Çorum FK maç analizleri 📰🔴",
                city = "Çorum",
                followersText = "8.150"
            )
            cityClean.contains("Sakarya", ignoreCase = true) || cityClean.contains("Adapazarı", ignoreCase = true) -> LocalMediaOutletInfo(
                name = "Sakarya Yerel Basın",
                handle = "@sakarya_yerel_basin",
                avatarHex = "#047857",
                bio = "Sakarya amatör ve alt lig futbol nabzı, Tatangalar ve yeşil-siyah sevda 🗞️💚",
                city = "Sakarya",
                followersText = "14.200"
            )
            cityClean.contains("Bursa", ignoreCase = true) -> LocalMediaOutletInfo(
                name = "Bursa Şehir Gazetesi",
                handle = "@bursa_sehir_spor",
                avatarHex = "#15803D",
                bio = "Bursa futbol kültürü, Timsahlar ve amatör lig panoraması 🗞️🐊",
                city = "Bursa",
                followersText = "22.600"
            )
            cityClean.contains("Kocaeli", ignoreCase = true) || cityClean.contains("İzmit", ignoreCase = true) -> LocalMediaOutletInfo(
                name = "Kocaeli Spor Gazetesi",
                handle = "@kocaeli_spor_haber",
                avatarHex = "#1E40AF",
                bio = "Kocaeli bölge spor bülteni ve yerel lig değerlendirmeleri 📰⚽",
                city = "Kocaeli",
                followersText = "12.800"
            )
            cityClean.contains("Eskişehir", ignoreCase = true) -> LocalMediaOutletInfo(
                name = "Eskişehir Spor Postası",
                handle = "@eskisehir_spor_postasi",
                avatarHex = "#DC2626",
                bio = "Eskişehir futbol bülteni, Anadolu yıldızları ve amatör küme 📰🔴",
                city = "Eskişehir",
                followersText = "16.400"
            )
            cityClean.contains("Batman", ignoreCase = true) -> LocalMediaOutletInfo(
                name = "Batman Petrol Basın",
                handle = "@batman_petrol_basin",
                avatarHex = "#B45309",
                bio = "Batman ve Güneydoğu alt lig futbol haberleri 🗞️⚽",
                city = "Batman",
                followersText = "9.200"
            )
            cityClean.contains("Ordu", ignoreCase = true) -> LocalMediaOutletInfo(
                name = "Karadeniz Ordu Spor",
                handle = "@ordu_spor_basin",
                avatarHex = "#7C3AED",
                bio = "Ordu ili spor basını, Karadeniz alt ligleri ve maç yorumları 🗞️🌊",
                city = "Ordu",
                followersText = "7.800"
            )
            cityClean.contains("Balıkesir", ignoreCase = true) -> LocalMediaOutletInfo(
                name = "Balıkesir Haber Spor",
                handle = "@balikesir_spor_haber",
                avatarHex = "#BE123C",
                bio = "Kuvayımilliye şehri Balıkesir yerel spor günlüğü 🗞️⚽",
                city = "Balıkesir",
                followersText = "8.900"
            )
            cityClean.contains("İzmir", ignoreCase = true) -> LocalMediaOutletInfo(
                name = "Ege Spor Postası",
                handle = "@ege_spor_postasi",
                avatarHex = "#0284C7",
                bio = "İzmir ve Ege bölgesi alt ligleri, amatör futbol ve genç yetenekler 🗞️🌊",
                city = "İzmir",
                followersText = "18.300"
            )
            else -> LocalMediaOutletInfo(
                name = "$cityClean Spor Postası",
                handle = "@${cityClean.lowercase().replace(" ", "_").replace("ç", "c").replace("ğ", "g").replace("ı", "i").replace("ö", "o").replace("ş", "s").replace("ü", "u")}_spor_postasi",
                avatarHex = club.primaryColorHex,
                bio = "$cityClean yerel ligleri ve ${club.name} haftalık maç raporları 🗞️⚽",
                city = cityClean,
                followersText = "5.100"
            )
        }
    }

    /**
     * Alt ligler için gerçekçi kulüp amigosu / tribün lideri hesabı
     */
    fun resolveClubAmigoProfile(club: Club): SocialAccountProfile {
        val shortLower = club.shortName.lowercase().replace(" ", "_")
        return SocialAccountProfile(
            id = "ACC_AMIGO_${club.id}",
            handle = "@amigo_murat_$shortLower",
            displayName = "Amigo Murat (${club.shortName} Tribün Lideri)",
            category = SocialAccountCategory.FAN_ULTRAS,
            isVerified = false,
            avatarHex = club.primaryColorHex,
            bannerHex = club.secondaryColorHex,
            bio = "${club.name} sevdalısı. 20 yıldır yağmur çamur her deplasmandayız! 🧣🔥 Armanın peşinde son nefese kadar.",
            location = club.city,
            followersCountText = "4,2B",
            followingCount = 310,
            postsCount = 1_280,
            isFollowedByDefault = true
        )
    }

    /**
     * Alt ligler için gerçekçi TFF bölge scoutu hesabı
     */
    fun resolveLowerTierScoutProfile(): SocialAccountProfile {
        return SocialAccountProfile(
            id = "ACC_SCOUT_HASAN",
            handle = "@hasan_tff_scout",
            displayName = "Hasan Hoca (TFF Alt Lig Scoutu)",
            category = SocialAccountCategory.ANALYST,
            isVerified = true,
            badgeType = VerifiedBadgeType.BLUE,
            avatarHex = "#0D9488",
            bannerHex = "#134E4A",
            bio = "TFF Bölge ve Alt Ligler Genç Oyuncu Gözlemcisi 📋⚽ 1. Lig, 2. Lig & 3. Lig yetenek raporları.",
            location = "Ankara / Türkiye",
            followersCountText = "12,4B",
            followingCount = 450,
            postsCount = 1_650,
            isFollowedByDefault = true
        )
    }

    /**
     * Alt liglerde oynayan kullanıcıya DM atan gerçekçi karakter havuzu
     */
    fun generateLowerTierDmPool(
        state: CareerSaveState,
        dayIndex: Int
    ): List<InstaDmThread> {
        val p = state.player
        val club = state.currentClub
        val localMedia = resolveLocalMediaForClub(club)
        val captain = club.keySquadMembers.find { it.isCaptain } ?: club.keySquadMembers.firstOrNull()

        return listOf(
            // 1. Yerel İl/İlçe Gazetecisi
            InstaDmThread(
                id = "IDM_REQ_LOCAL_JOURNALIST_$dayIndex",
                senderName = localMedia.name,
                senderHandle = localMedia.handle,
                senderRole = "Yerel Spor Muhabiri",
                avatarHex = localMedia.avatarHex,
                isVerified = false,
                followersLabel = "${localMedia.followersText} takipçi",
                isPendingRequest = true,
                unreadCount = 1,
                messages = listOf(
                    ChatMessage(
                        id = "IDM_LOC_J_$dayIndex",
                        senderName = localMedia.name,
                        isFromUser = false,
                        text = "Selam ${p.firstName}! ${localMedia.city} Spor Postası olarak dünkü mücadeleci oyununu çok beğendik. Haftalık yerel gazetemizin 'Haftanın Genç Yeteneği' köşesi için kısa bir röportaj yapabilir miyiz? 🗞️📸",
                        dayIndex = dayIndex,
                        timeLabel = "18:40"
                    )
                )
            ),
            // 2. Kulüp Amigosu
            InstaDmThread(
                id = "IDM_REQ_AMIGO_$dayIndex",
                senderName = "Amigo Murat (${club.shortName})",
                senderHandle = "@amigo_murat_${club.shortName.lowercase()}",
                senderRole = "Tribün Lideri",
                avatarHex = club.primaryColorHex,
                isVerified = false,
                followersLabel = "4,2B takipçi",
                isPendingRequest = true,
                unreadCount = 1,
                messages = listOf(
                    ChatMessage(
                        id = "IDM_AMG_M1_$dayIndex",
                        senderName = "Amigo Murat",
                        isFromUser = false,
                        text = "Koçum benim! 🧣 Dün sahada formanın hakkını verdin, tribünden senin adını haykırdık. Yarınki idmandan sonra tesislerde çocuklarla tatlı ikram edeceğiz, sakın disiplini bozma! 🔥⚽",
                        dayIndex = dayIndex,
                        timeLabel = "19:15"
                    )
                )
            ),
            // 3. TFF Alt Lig Scoutu
            InstaDmThread(
                id = "IDM_REQ_LOWER_SCOUT_$dayIndex",
                senderName = "Hasan Hoca (TFF Bölge Scoutu)",
                senderHandle = "@hasan_tff_scout",
                senderRole = "Alt Lig Yetenek Avcısı",
                avatarHex = "#0D9488",
                isVerified = true,
                followersLabel = "12,4B takipçi",
                isPendingRequest = true,
                unreadCount = 1,
                messages = listOf(
                    ChatMessage(
                        id = "IDM_SCT_ALT_$dayIndex",
                        senderName = "Hasan Hoca",
                        isFromUser = false,
                        text = "Tebrikler ${p.firstName}. ${club.name} maçındaki pozisyon bilgin ve top tekniğin dikkat çekiciydi. Üst lig kulüpleri için hazırladığım havuz raporuna notunu aldım, aynen devam! 📋⚽",
                        dayIndex = dayIndex,
                        timeLabel = "20:10"
                    )
                )
            ),
            // 4. Takım Arkadaşı (Kaptan / Yedek Kaleci)
            InstaDmThread(
                id = "IDM_REQ_TEAMMATE_$dayIndex",
                senderName = captain?.name ?: "Kaptan Erhan",
                senderHandle = "@${(captain?.name ?: "kaptan").lowercase().replace(" ", "_")}",
                senderRole = "Takım Kaptanı",
                avatarHex = club.secondaryColorHex,
                isVerified = false,
                followersLabel = "1,8B takipçi",
                isPendingRequest = false,
                unreadCount = 1,
                messages = listOf(
                    ChatMessage(
                        id = "IDM_CAPT_M1_$dayIndex",
                        senderName = captain?.name ?: "Kaptan Erhan",
                        isFromUser = false,
                        text = "Kardeşim bugünkü taktik antrenmanda harikaydın. Hafta sonu deplasmanda 3 puanı alıyoruz, soyunma odasında aynı hırsı bekliyorum senden! 💪⚽",
                        dayIndex = dayIndex,
                        timeLabel = "21:00"
                    )
                )
            )
        )
    }

    /**
     * Alt ligler için gerçekçi kız arkadaş adayları (Lise aşkı, üniversite arkadaşı, yerel esnaf kızı)
     */
    fun getLowerTierRomanceCandidates(club: Club): List<GirlfriendProfile> {
        val localMedia = resolveLocalMediaForClub(club)

        return listOf(
            // 1. Mahalleden Lise Aşkı
            GirlfriendProfile(
                id = "partner_lise_elif",
                fullName = "Elif Demir",
                handle = "@elifdemir",
                archetype = PartnerArchetype.HIGH_SCHOOL_SWEETHEART,
                professionTr = "Lise Aşkı & Çocukluk Arkadaşı",
                age = 20,
                city = club.city,
                avatarHex = "#F43F5E",
                secondaryHex = "#881337",
                bio = "Öğretmenlik öğrencisi 📚☕ | Mahallemizin çocukları büyüyor ✨",
                followersCount = 850L,
                minFameTier = PlayerFameTier.AMATEUR,
                minCharisma = 20,
                statusQuote = "Nerede olursan ol, kalbini ve geçmişini asla unutma 🌸"
            ),
            // 2. Üniversiteden Arkadaşı
            GirlfriendProfile(
                id = "partner_uni_busra",
                fullName = "Büşra Yılmaz",
                handle = "@busrayilmaz",
                archetype = PartnerArchetype.UNIVERSITY_FRIEND,
                professionTr = "Üniversite & Kampüs Arkadaşı",
                age = 21,
                city = club.city,
                avatarHex = "#0EA5E9",
                secondaryHex = "#0369A1",
                bio = "Mimarlık & Tasarım 📐☕ | Kampüs günleri & futbol maçları",
                followersCount = 1_450L,
                minFameTier = PlayerFameTier.AMATEUR,
                minCharisma = 25,
                statusQuote = "Hayallerine giden yolda ilk adımı atmaktan korkma 🎓"
            ),
            // 3. Yerel Esnaf / Mahalle Kızı
            GirlfriendProfile(
                id = "partner_esnaf_zeynep",
                fullName = "Zeynep Çelik",
                handle = "@zeynepcelik",
                archetype = PartnerArchetype.LOCAL_TOWN_GIRL,
                professionTr = "Mahalle Esnaf Kızı & Butik İşletmecisi",
                age = 21,
                city = club.city,
                avatarHex = "#10B981",
                secondaryHex = "#064E3B",
                bio = "Tatlılar, sıcak kahveler & samimi sohbetler 🧁🍰 | Mahallenin en tatlı durağı",
                followersCount = 2_100L,
                minFameTier = PlayerFameTier.AMATEUR,
                minCharisma = 22,
                statusQuote = "Samimiyet ve sevgi en büyük zenginliktir 🍰"
            ),
            // 4. Yerel Spor Medyası Muhabiri
            GirlfriendProfile(
                id = "partner_yerel_cansu",
                fullName = "Cansu Kurt",
                handle = "@cansukurt",
                archetype = PartnerArchetype.LOCAL_REPORTER,
                professionTr = "${club.city} Yerel Spor Muhabiri",
                age = 23,
                city = club.city,
                avatarHex = "#F59E0B",
                secondaryHex = "#78350F",
                bio = "${localMedia.name} saha kenarı muhabiri 🎙️⚽ | Alt liglerin kalbi",
                followersCount = 4_800L,
                minFameTier = PlayerFameTier.SEMI_PRO,
                minCharisma = 35,
                statusQuote = "Futbol sadece 90 dakika değil, bir şehrin tutkusudur 🎙️"
            )
        )
    }

    /**
     * Alt liglerdeyken Chloé Dubois gibi dünya çapında modelleri ve ünlü dizi oyuncularını kesinlikle engeller.
     */
    fun filterCandidatePartnersForRealism(
        state: CareerSaveState,
        allCandidates: List<GirlfriendProfile>
    ): List<GirlfriendProfile> {
        val canAccess = canAccessSuperstarsAndGlobalMedia(state)
        val club = state.currentClub
        val lowerTierPool = getLowerTierRomanceCandidates(club)

        if (!canAccess) {
            // Sadece alt liglere uygun yerel kız arkadaş adayları görünebilir!
            // Chloé Dubois, Melis Vural, Derin Karahan KESİNLİKLE elenir!
            val safeBase = allCandidates.filter { candidate ->
                candidate.id !in setOf(
                    "partner_chloe_dubois",
                    "partner_melis_vural",
                    "partner_derin_karahan",
                    "partner_selin_aksoy"
                ) && candidate.archetype !in setOf(
                    PartnerArchetype.SUPERMODEL,
                    PartnerArchetype.ACTRESS,
                    PartnerArchetype.FASHION_INFLUENCER
                )
            }
            val existingIds = safeBase.map { it.id }.toSet()
            val missingLower = lowerTierPool.filter { it.id !in existingIds }
            return safeBase + missingLower
        }

        // Süper Lig / Milli Takım seviyesinde hepsi erişilebilir
        val existingIds = allCandidates.map { it.id }.toSet()
        val missingLower = lowerTierPool.filter { it.id !in existingIds }
        return allCandidates + missingLower
    }

    /**
     * Alt ligler için gerçekçi Instagram Notları üretir.
     */
    fun generateRealismInstaNotes(
        state: CareerSaveState,
        userCustomNote: String?
    ): List<InstaNoteItem> {
        val p = state.player
        val club = state.currentClub
        val captain = club.keySquadMembers.find { it.isCaptain } ?: club.keySquadMembers.firstOrNull()
        val localMedia = resolveLocalMediaForClub(club)
        val userNote = userCustomNote ?: "Maç günü odaklıyız ⚽🔥"

        val list = mutableListOf(
            InstaNoteItem(
                id = "NOTE_USER",
                handle = "@${p.firstName.lowercase()}${p.kitNumber}",
                authorName = "Notun",
                avatarHex = club.primaryColorHex,
                noteText = userNote,
                isVerified = true,
                dmThreadId = null
            )
        )

        if (!canAccessSuperstarsAndGlobalMedia(state)) {
            // Alt ligler: Takım arkadaşları, yerel gazeteci, kulüp amigosu
            list.add(
                InstaNoteItem(
                    id = "NOTE_CAPTAIN",
                    handle = "@${(captain?.name ?: "kaptan").lowercase().replace(" ", "_")}",
                    authorName = captain?.name?.substringBefore(" ") ?: "Kaptan",
                    avatarHex = club.secondaryColorHex,
                    noteText = "Hafta sonu 3 puan bizimdir beyler 💪⚽",
                    isVerified = false,
                    dmThreadId = "IDM_REQ_TEAMMATE_${state.currentDayIndex}"
                )
            )
            list.add(
                InstaNoteItem(
                    id = "NOTE_LOCAL_PRESS",
                    handle = localMedia.handle,
                    authorName = localMedia.name.take(12),
                    avatarHex = localMedia.avatarHex,
                    noteText = "${club.shortName} haftalık maç analizi yayında 🗞️",
                    isVerified = false,
                    dmThreadId = "IDM_REQ_LOCAL_JOURNALIST_${state.currentDayIndex}"
                )
            )
            list.add(
                InstaNoteItem(
                    id = "NOTE_AMIGO",
                    handle = "@amigo_murat_${club.shortName.lowercase()}",
                    authorName = "Amigo Murat",
                    avatarHex = club.primaryColorHex,
                    noteText = "Deplasman otobüsleri hazır! 🧣🚌",
                    isVerified = false,
                    dmThreadId = "IDM_REQ_AMIGO_${state.currentDayIndex}"
                )
            )
            list.add(
                InstaNoteItem(
                    id = "NOTE_SCOUT",
                    handle = "@hasan_tff_scout",
                    authorName = "Hasan Scout",
                    avatarHex = "#0D9488",
                    noteText = "Ligde dikkat çeken gençler var 📋⚽",
                    isVerified = true,
                    dmThreadId = "IDM_REQ_LOWER_SCOUT_${state.currentDayIndex}"
                )
            )
        } else {
            // Süper Lig / Milli Takım: Arda Güler, Fabrizio, Nike vb.
            list.add(
                InstaNoteItem(
                    id = "NOTE_ARDA",
                    handle = "@ardaguler",
                    authorName = "Arda Güler",
                    avatarHex = "#F59E0B",
                    noteText = "Hala Madrid! 🤍✨",
                    isVerified = true,
                    dmThreadId = "IDM_ARDA"
                )
            )
            list.add(
                InstaNoteItem(
                    id = "NOTE_FAB",
                    handle = "@fabrizioromano",
                    authorName = "Fabrizio",
                    avatarHex = "#1D9BF0",
                    noteText = "Here we go soon... ⏳🚨",
                    isVerified = true,
                    dmThreadId = "IDM_REQ_FABRIZIO"
                )
            )
            list.add(
                InstaNoteItem(
                    id = "NOTE_CAPTAIN",
                    handle = "@${(captain?.name ?: "kaptan").lowercase().replace(" ", "_")}",
                    authorName = captain?.name?.substringBefore(" ") ?: "Kaptan",
                    avatarHex = "#20CF79",
                    noteText = "3 puan bizimdir 💪⚽",
                    isVerified = true,
                    dmThreadId = "IDM_CLUB_MEDIA"
                )
            )
            list.add(
                InstaNoteItem(
                    id = "NOTE_NIKE",
                    handle = "@niketurkiye",
                    authorName = "Nike TR",
                    avatarHex = "#111827",
                    noteText = "Mercurial 16 ⚡👟",
                    isVerified = true,
                    dmThreadId = "IDM_REQ_NIKE"
                )
            )
        }

        return list
    }

    /**
     * Alt ligler için gerçekçi X (Twitter) Gündem ve Trendleri üretir.
     */
    fun generateRealismTrendingTopics(state: CareerSaveState): List<XTrendTopic> {
        val p = state.player
        val club = state.currentClub
        val localMedia = resolveLocalMediaForClub(club)
        val rivalClub = state.clubs.firstOrNull { it.id != club.id && it.leagueId == club.leagueId }
            ?: state.clubs.lastOrNull() ?: club
        val topScorer = state.topScorers.firstOrNull()

        if (!canAccessSuperstarsAndGlobalMedia(state)) {
            // Alt ligler gerçekçi yerel gündem:
            return listOf(
                XTrendTopic(
                    id = "TR_1",
                    rank = 1,
                    categoryLabel = "${club.city} · Yerel Gündem",
                    topicTitle = "#${club.shortName}",
                    postsCountLabel = "1.840 gönderi",
                    liveContextSubtitle = "${localMedia.name} ve yerel taraftarlar hafta sonu oynanacak maçı konuşuyor",
                    filterKeyword = club.shortName
                ),
                XTrendTopic(
                    id = "TR_2",
                    rank = 2,
                    categoryLabel = "Futbol · ${club.leagueName}",
                    topicTitle = "#${club.leagueName.replace(" ", "")}",
                    postsCountLabel = "3.250 gönderi",
                    liveContextSubtitle = "Gruptaki puan durumu ve hafta sonu fikstürü tartışılıyor",
                    filterKeyword = club.leagueName
                ),
                XTrendTopic(
                    id = "TR_3",
                    rank = 3,
                    categoryLabel = "${club.city} Spor · Canlı",
                    topicTitle = p.fullName,
                    postsCountLabel = "460 gönderi",
                    liveContextSubtitle = "${localMedia.name}: \"${club.name} hücumunda parlayan genç yetenek: #${p.kitNumber}\"",
                    filterKeyword = p.lastName
                ),
                XTrendTopic(
                    id = "TR_4",
                    rank = 4,
                    categoryLabel = "Alt Ligler · Gündem",
                    topicTitle = "#AltLigler",
                    postsCountLabel = "5.120 gönderi",
                    liveContextSubtitle = "TFF 2. Lig, 3. Lig ve BAL gruplarında haftanın kritik hakem kararları",
                    filterKeyword = "AltLigler"
                ),
                XTrendTopic(
                    id = "TR_5",
                    rank = 5,
                    categoryLabel = "Derbi Rekabeti · Gündem",
                    topicTitle = rivalClub.name,
                    postsCountLabel = "980 gönderi",
                    liveContextSubtitle = "${rivalClub.shortName} taraftarları hafta sonu deplasman hazırlığı yapıyor",
                    filterKeyword = rivalClub.shortName
                ),
                XTrendTopic(
                    id = "TR_6",
                    rank = 6,
                    categoryLabel = "Futbol Kültürü · Amatör Ruh",
                    topicTitle = "Deplasman Otobüsü",
                    postsCountLabel = "2.340 gönderi",
                    liveContextSubtitle = "Alt lig deplasman otobüslerinde söylenen besteler viral oldu",
                    filterKeyword = "Deplasman"
                ),
                XTrendTopic(
                    id = "TR_7",
                    rank = 7,
                    categoryLabel = "Yerel Spor · ${club.city}",
                    topicTitle = "#${club.city}Spor",
                    postsCountLabel = "780 gönderi",
                    liveContextSubtitle = "${club.city} ili amatör saha şartları ve altyapı tesisleri gündemde",
                    filterKeyword = club.city
                ),
                XTrendTopic(
                    id = "TR_8",
                    rank = 8,
                    categoryLabel = "İstatistik · Alt Ligler",
                    topicTitle = topScorer?.playerName ?: "Gol Krallığı",
                    postsCountLabel = "620 gönderi",
                    liveContextSubtitle = "${club.leagueName}'de gol krallığı mücadelesi",
                    filterKeyword = "Gol"
                )
            )
        }

        // Süper Lig / Milli Takım Seviyesi
        return listOf(
            XTrendTopic(
                id = "TR_1",
                rank = 1,
                categoryLabel = "Spor · Türkiye gündeminde",
                topicTitle = "#${club.shortName}",
                postsCountLabel = "42,8 B gönderi",
                liveContextSubtitle = "${club.name} taraftarları gelecek maç kadrosunu ve taktiği tartışıyor",
                filterKeyword = club.shortName
            ),
            XTrendTopic(
                id = "TR_2",
                rank = 2,
                categoryLabel = "Futbol · Canlı Gündem",
                topicTitle = p.fullName,
                postsCountLabel = "19,4 B gönderi",
                liveContextSubtitle = "Avrupa scoutları ve transfer kulisleri sonrası gündemde",
                filterKeyword = p.lastName
            ),
            XTrendTopic(
                id = "TR_3",
                rank = 3,
                categoryLabel = "Transfer · Gündemdekiler",
                topicTitle = "Fabrizio Romano",
                postsCountLabel = "96,2 B gönderi",
                liveContextSubtitle = "Here we go! Avrupa ve Türkiye transfer gelişmeleri",
                filterKeyword = "Fabrizio"
            ),
            XTrendTopic(
                id = "TR_4",
                rank = 4,
                categoryLabel = "Futbol · Türkiye gündeminde",
                topicTitle = "#Transfermarkt",
                postsCountLabel = "31,5 B gönderi",
                liveContextSubtitle = "${club.leagueName} güncel piyasa değeri değişimleri açıklandı",
                filterKeyword = "Transfermarkt"
            ),
            XTrendTopic(
                id = "TR_5",
                rank = 5,
                categoryLabel = "Avrupa Futbolu · Gündemdekiler",
                topicTitle = "Arda Güler",
                postsCountLabel = "118,0 B gönderi",
                liveContextSubtitle = "Santiago Bernabéu'daki performansı İspanya basınında manşet",
                filterKeyword = "Arda"
            ),
            XTrendTopic(
                id = "TR_6",
                rank = 6,
                categoryLabel = "Spor · Gündemdekiler",
                topicTitle = rivalClub.name,
                postsCountLabel = "24,1 B gönderi",
                liveContextSubtitle = "${rivalClub.shortName} cephesinde transfer ve zirve yarışı tartışmaları",
                filterKeyword = rivalClub.shortName
            ),
            XTrendTopic(
                id = "TR_7",
                rank = 7,
                categoryLabel = "Futbol Mizah · Gündemdekiler",
                topicTitle = "VAR Kayıtları",
                postsCountLabel = "54,9 B gönderi",
                liveContextSubtitle = "Haftanın tartışmalı pozisyonları sosyal medyayı ikiye böldü",
                filterKeyword = "VAR"
            ),
            XTrendTopic(
                id = "TR_8",
                rank = 8,
                categoryLabel = "İstatistik · Gündemdekiler",
                topicTitle = topScorer?.playerName ?: "Gol Krallığı",
                postsCountLabel = "14,2 B gönderi",
                liveContextSubtitle = "${club.leagueName}'de gol krallığı yarışı kızışıyor",
                filterKeyword = "Gol"
            )
        )
    }

    /**
     * Alt ligler için gerçekçi bildirimleri üretir.
     * Arda Güler, Fabrizio Romano, 433 veya global fenomenlerin alt lig oyuncusunu
     * etiketlemesi / beğenmesi / takip etmesi kesinlikle engellenir!
     */
    fun generateRealismNotifications(
        state: CareerSaveState,
        platform: String
    ): List<SocialNotificationItem> {
        val p = state.player
        val club = state.currentClub
        val day = state.currentDayIndex
        val userHandle = "@${p.firstName.lowercase()}${p.kitNumber}"
        val localMedia = resolveLocalMediaForClub(club)
        val captain = club.keySquadMembers.find { it.isCaptain } ?: club.keySquadMembers.firstOrNull()

        if (!canAccessSuperstarsAndGlobalMedia(state)) {
            // Alt ligler için tamamen yerel ve gerçekçi bildirim akışı
            return if (platform == "INSTAGRAM") {
                listOf(
                    SocialNotificationItem(
                        id = "NOTIF_IG_LOC_1",
                        platform = "INSTAGRAM",
                        type = "LIKE",
                        actorName = captain?.name ?: "Kaptan Erhan",
                        actorHandle = "@${(captain?.name ?: "kaptan").lowercase().replace(" ", "_")}",
                        actorAvatarHex = club.secondaryColorHex,
                        actorVerified = false,
                        text = "antrenman fotoğrafını beğendi. 💪⚽",
                        timeLabel = "25d",
                        dayIndex = day
                    ),
                    SocialNotificationItem(
                        id = "NOTIF_IG_LOC_2",
                        platform = "INSTAGRAM",
                        type = "MENTION",
                        actorName = localMedia.name,
                        actorHandle = localMedia.handle,
                        actorAvatarHex = localMedia.avatarHex,
                        actorVerified = false,
                        text = "bir gönderide senden bahsetti: \"Haftanın 11'ine seçilen genç oyuncumuz $userHandle tebrikler! 🗞️👏\"",
                        timeLabel = "1s",
                        dayIndex = day
                    ),
                    SocialNotificationItem(
                        id = "NOTIF_IG_LOC_3",
                        platform = "INSTAGRAM",
                        type = "COMMENT",
                        actorName = "${club.shortName} Tribün Sevdalıları",
                        actorHandle = "@${club.shortName.lowercase()}_tribun",
                        actorAvatarHex = club.primaryColorHex,
                        actorVerified = false,
                        text = "gönderine yorum yaptı: \"Formanın hakkını veren koçumuz, hafta sonu golünü bekliyoruz! 🧣🔥\"",
                        timeLabel = "2s",
                        dayIndex = day
                    ),
                    SocialNotificationItem(
                        id = "NOTIF_IG_LOC_4",
                        platform = "INSTAGRAM",
                        type = "FOLLOW",
                        actorName = "Hasan Hoca (TFF Alt Lig Scoutu)",
                        actorHandle = "@hasan_tff_scout",
                        actorAvatarHex = "#0D9488",
                        actorVerified = true,
                        text = "seni takip etmeye başladı ve profilini inceledi. 📋",
                        timeLabel = "3s",
                        dayIndex = day
                    ),
                    SocialNotificationItem(
                        id = "NOTIF_IG_LOC_5",
                        platform = "INSTAGRAM",
                        type = "LIKE",
                        actorName = "Amigo Murat",
                        actorHandle = "@amigo_murat_${club.shortName.lowercase()}",
                        actorAvatarHex = club.primaryColorHex,
                        actorVerified = false,
                        text = "ve 84 diğer mahalleli ve taraftar gönderini beğendi.",
                        timeLabel = "4s",
                        dayIndex = day
                    )
                )
            } else {
                listOf(
                    SocialNotificationItem(
                        id = "NOTIF_X_LOC_1",
                        platform = "X",
                        type = "MENTION",
                        actorName = localMedia.name,
                        actorHandle = localMedia.handle,
                        actorAvatarHex = localMedia.avatarHex,
                        actorVerified = false,
                        text = "ÖZEL | ${club.name} altyapısından vitrine çıkan $userHandle (#${p.kitNumber}), son haftalardaki performansıyla yerel taraftarın sevgilisi oldu. 🗞️⚽",
                        timeLabel = "40d",
                        dayIndex = day
                    ),
                    SocialNotificationItem(
                        id = "NOTIF_X_LOC_2",
                        platform = "X",
                        type = "QUOTE",
                        actorName = "${club.shortName} Taraftarlar Derneği",
                        actorHandle = "@${club.shortName.lowercase()}_dernek",
                        actorAvatarHex = club.primaryColorHex,
                        actorVerified = false,
                        text = "hakkındaki haberi alıntıladı: \"Bu çocuğun enerjisi takımı ateşliyor, sözleşmesi mutlaka uzatılmalı!\"",
                        timeLabel = "25d",
                        dayIndex = day
                    ),
                    SocialNotificationItem(
                        id = "NOTIF_X_LOC_3",
                        platform = "X",
                        type = "REPOST",
                        actorName = "Hasan Hoca (TFF Alt Lig Scoutu)",
                        actorHandle = "@hasan_tff_scout",
                        actorAvatarHex = "#0D9488",
                        actorVerified = true,
                        text = "maç sonu paylaşımını yeniden yayınladı.",
                        timeLabel = "2s",
                        dayIndex = day
                    ),
                    SocialNotificationItem(
                        id = "NOTIF_X_LOC_4",
                        platform = "X",
                        type = "REPLY",
                        actorName = "Mahalle Çay Ocağı Ahmet Abi",
                        actorHandle = "@ahmet_cayocagi",
                        actorAvatarHex = "#F59E0B",
                        actorVerified = false,
                        text = "gönderine yanıt verdi: \"Akşam maçın kritiğini çay ocağında yapıyoruz koçum, çaylar benden! ☕⚽\"",
                        timeLabel = "3s",
                        dayIndex = day
                    )
                )
            }
        }

        // Süper Lig / Milli Takım bildirimleri
        return if (platform == "INSTAGRAM") {
            listOf(
                SocialNotificationItem(
                    id = "NOTIF_IG_1",
                    platform = "INSTAGRAM",
                    type = "LIKE",
                    actorName = "Arda Güler",
                    actorHandle = "@ardaguler",
                    actorAvatarHex = "#F59E0B",
                    actorVerified = true,
                    text = "son antrenman gönderini ve hikayeni beğendi. ❤️",
                    timeLabel = "18d",
                    dayIndex = day
                ),
                SocialNotificationItem(
                    id = "NOTIF_IG_2",
                    platform = "INSTAGRAM",
                    type = "MENTION",
                    actorName = "Fabrizio Romano",
                    actorHandle = "@fabrizioromano",
                    actorAvatarHex = "#1D9BF0",
                    actorVerified = true,
                    text = "bir gönderide senden bahsetti: \"🚨✨ ${club.name} kadrosunda dikkat çeken genç yetenek: $userHandle\"",
                    timeLabel = "1s",
                    dayIndex = day
                ),
                SocialNotificationItem(
                    id = "NOTIF_IG_3",
                    platform = "INSTAGRAM",
                    type = "COMMENT",
                    actorName = "433",
                    actorHandle = "@433",
                    actorAvatarHex = "#FF3B30",
                    actorVerified = true,
                    text = "gönderine yorum yaptı: \"Future star in the making 🔥⚽\"",
                    timeLabel = "2s",
                    dayIndex = day
                ),
                SocialNotificationItem(
                    id = "NOTIF_IG_4",
                    platform = "INSTAGRAM",
                    type = "FOLLOW",
                    actorName = "Elvin Yıldırım",
                    actorHandle = "@elvinyildirim",
                    actorAvatarHex = "#E1306C",
                    actorVerified = true,
                    text = "seni takip etmeye başladı ve bir DM isteği gönderdi. ✨",
                    timeLabel = "3s",
                    dayIndex = day
                ),
                SocialNotificationItem(
                    id = "NOTIF_IG_5",
                    platform = "INSTAGRAM",
                    type = "MENTION",
                    actorName = "Transfermarkt Türkiye",
                    actorHandle = "@transfermarkt_tr",
                    actorAvatarHex = "#00193F",
                    actorVerified = true,
                    text = "piyasa değeri güncellemesinde seni etiketledi: \"${FLFormatters.formatMarketValue(p.marketValueEur)} 📈\"",
                    timeLabel = "4s",
                    dayIndex = day
                )
            )
        } else {
            listOf(
                SocialNotificationItem(
                    id = "NOTIF_X_1",
                    platform = "X",
                    type = "MENTION",
                    actorName = "Yağız Sabuncuoğlu",
                    actorHandle = "@yagosabuncuoglu",
                    actorAvatarHex = "#0EA5E9",
                    actorVerified = true,
                    text = "ÖZEL | Avrupa'dan iki kulübün scout ekibi $userHandle (#${p.kitNumber}) için canlı maç raporu hazırladı. 🔎",
                    timeLabel = "35d",
                    dayIndex = day
                ),
                SocialNotificationItem(
                    id = "NOTIF_X_2",
                    platform = "X",
                    type = "QUOTE",
                    actorName = "Uğur Karakullukçu",
                    actorHandle = "@ugurkarakullukcu",
                    actorAvatarHex = "#DC2626",
                    actorVerified = true,
                    text = "hakkındaki haberi alıntıladı: \"${p.age} yaşında bu fizik kalite ve top tekniğiyle fark yaratmaması imkansızdı.\"",
                    timeLabel = "22d",
                    dayIndex = day
                ),
                SocialNotificationItem(
                    id = "NOTIF_X_3",
                    platform = "X",
                    type = "LIKE",
                    actorName = "Sokrates Dergi",
                    actorHandle = "@sokratesdergi",
                    actorAvatarHex = "#EC4899",
                    actorVerified = true,
                    text = "maç sonu açıklamanı beğendi.",
                    timeLabel = "1s",
                    dayIndex = day
                )
            )
        }
    }

    /**
     * Alt liglerdeyken Arda Güler veya Real Madrid Valdebebas gibi yabancı hikayeler yerine
     * alt lige uygun yerel hikayeler gösterir.
     */
    fun filterRealismStories(
        baseStories: List<StoryItem>,
        state: CareerSaveState
    ): List<StoryItem> {
        val canAccess = canAccessSuperstarsAndGlobalMedia(state)
        val club = state.currentClub
        val localMedia = resolveLocalMediaForClub(club)
        val day = state.currentDayIndex

        if (!canAccess) {
            val localStories = listOf(
                StoryItem(
                    id = "ST_LOC_CLUB",
                    authorName = club.shortName.lowercase(),
                    authorRole = "${club.name} • Resmi",
                    avatarHex = club.primaryColorHex,
                    title = "Taktik Hazırlıklar Tamamlandı ⚽💪",
                    caption = "${club.stadiumName} tesislerinde çift kale maç ile hazırlıklarımızı tamamladık.",
                    sceneType = "TRAINING",
                    dayIndex = day
                ),
                StoryItem(
                    id = "ST_LOC_PRESS",
                    authorName = localMedia.handle.removePrefix("@"),
                    authorRole = localMedia.name,
                    avatarHex = localMedia.avatarHex,
                    title = "Maç Öncesi Son Durum 🗞️🎤",
                    caption = "Teknik direktörün ilk 11 tercihi ve maç öncesi son kulisler haberimizde.",
                    sceneType = "STADIUM",
                    dayIndex = day
                ),
                StoryItem(
                    id = "ST_LOC_AMIGO",
                    authorName = "amigo_murat_${club.shortName.lowercase()}",
                    authorRole = "${club.shortName} Tribün Lideri",
                    avatarHex = club.primaryColorHex,
                    title = "Otobüsler Doldu, Geliyoruz! 🧣🚌",
                    caption = "90 dakika boyunca takımı yalnız bırakmayacağız. Saldır ${club.shortName}!",
                    sceneType = "STADIUM",
                    dayIndex = day
                ),
                StoryItem(
                    id = "ST_LOC_MEME",
                    authorName = "altliglerinsonsozu",
                    authorRole = "Alt Ligler Mizah",
                    avatarHex = "#EAB308",
                    title = "Deplasman Çamurlu Saha Klasiği 😂⚽",
                    caption = "Hakem düdüğü çaldığında formanın rengini tanıyamadığımız o kutsal pazar günleri!",
                    sceneType = "TRAINING",
                    dayIndex = day
                )
            )
            // Arda Güler, Elvin, Nike, Fabrizio, 433 hikayelerini tamamen temizle
            val filteredBase = baseStories.filter { story ->
                val lowerAuthor = story.authorName.lowercase().removePrefix("@")
                val isRestrictedAuthor = lowerAuthor in RESTRICTED_GLOBAL_HANDLES ||
                        lowerAuthor in setOf("ardaguler", "elvinyildirim", "niketurkiye", "sportsdigitale", "fabrizioromano", "433", "hakancalhanoglu")
                val isRestrictedId = story.id in setOf("ST_LIV_ARDA", "ST_433", "ST_FABRIZIO")
                !isRestrictedAuthor && !isRestrictedId
            }
            return localStories + filteredBase
        }

        return baseStories
    }

    /**
     * Alt liglerdeyken X tweet akışını filtreler:
     * Arda Güler ve Yağız'ın oyuncu hakkında Avrupa kulüplerinin scout raporu hazırladığına dair
     * gerçek dışı tweetleri engeller; yerine yerel basın haberleri koyar.
     */
    fun filterRealismXFeed(
        posts: List<XPostItem>,
        state: CareerSaveState
    ): List<XPostItem> {
        val canAccess = canAccessSuperstarsAndGlobalMedia(state)
        val club = state.currentClub
        val localMedia = resolveLocalMediaForClub(club)
        val p = state.player
        val day = state.currentDayIndex

        if (!canAccess) {
            val sanitized = posts.filterNot { post ->
                val handle = post.handle.lowercase().removePrefix("@")
                handle in RESTRICTED_GLOBAL_HANDLES && post.text.contains(p.lastName, ignoreCase = true)
            }.filterNot { post ->
                // Arda Güler'in Bernabeu tweeti veya beIN Sports ana yayıncı tweeti alt ligde alakasızsa filtrele
                post.handle.equals("@ardaguler", ignoreCase = true)
            }

            // Yerel basın tweeti ekle
            val localTweet = XPostItem(
                id = "XP_LOC_PRESS_$day",
                dayIndex = day,
                dateLabel = "35d",
                authorName = localMedia.name,
                handle = localMedia.handle,
                category = "YEREL BASIN",
                isVerified = false,
                text = "ÖZEL | ${club.name} hücumunda formayı kapan ${p.fullName} (#${p.kitNumber}), gösterdiği mücadeleci ruhla ${club.city} futbolseverlerinin takdirini topluyor. Teknik heyet oyuncunun çalışma azminden oldukça memnun. 🗞️⚽",
                likes = 340,
                reposts = 48
            )

            return listOf(localTweet) + sanitized
        }

        return posts
    }

    /**
     * Alt liglerdeyken Instagram akışını filtreler:
     * Arda Güler Santiago Bernabéu paylaşımı alt ligde dönmemeli, yerine lig maçları dönmeli.
     */
    fun filterRealismInstaFeed(
        posts: List<SocialMediaPost>,
        state: CareerSaveState
    ): List<SocialMediaPost> {
        val canAccess = canAccessSuperstarsAndGlobalMedia(state)
        val club = state.currentClub
        val localMedia = resolveLocalMediaForClub(club)
        val p = state.player
        val day = state.currentDayIndex

        if (!canAccess) {
            val sanitized = posts.filterNot { post ->
                val handle = post.authorHandle.lowercase().removePrefix("@")
                handle == "ardaguler" || (handle in RESTRICTED_GLOBAL_HANDLES && post.content.contains(p.lastName, ignoreCase = true))
            }

            val localPost = SocialMediaPost(
                id = "LIV_IG_LOC_PRESS_$day",
                dayIndex = day,
                dateLabel = "2 saat önce",
                authorName = localMedia.name,
                authorHandle = localMedia.handle,
                isUserPost = false,
                content = "🗞️ ${club.city.uppercase()} SPOR MANŞETİ | ${club.name} altyapısından parlayan genç cevher: ${p.fullName}!\n\n${club.leagueName}'de bu hafta oynanacak kritik maç öncesi takımda moraller yüksek. Sizce ${p.firstName} ilk 11 başlamalı mı? Yorumlarınızı bekliyoruz! 👇⚽",
                likesCount = 890,
                commentsCount = 42,
                sentiment = "Destekleyici",
                instaComments = listOf(
                    com.example.model.InstaCommentItem(
                        id = "CMT_LOC_1",
                        authorHandle = "amigo_murat_${club.shortName.lowercase()}",
                        authorName = "Amigo Murat",
                        isVerified = false,
                        avatarHex = club.primaryColorHex,
                        text = "İlk 11 başlarsa bu çocuk sırtlar takımı, yürekli oynuyor!",
                        timeLabel = "1s",
                        likesCount = 28
                    ),
                    com.example.model.InstaCommentItem(
                        id = "CMT_LOC_2",
                        authorHandle = "${club.shortName.lowercase()}_fan_ahmet",
                        authorName = "${club.shortName}li Ahmet",
                        isVerified = false,
                        avatarHex = club.secondaryColorHex,
                        text = "Maşallah nazar değmesin, aynen devam koçum!",
                        timeLabel = "45d",
                        likesCount = 14
                    )
                ),
                isVerifiedAuthor = false,
                locationLabel = club.city,
                imageSceneType = "STADIUM"
            )

            return listOf(localPost) + sanitized
        }

        return posts
    }

    /**
     * Kapsamlı Sosyal Medya ve DM Gerçekçilik Denetimi (Test & QA için).
     * @sef-sosyal ve @testci-sosyal tarafından çağrılıp doğrudan doğrulanabilir.
     */
    fun auditSocialRealism(state: CareerSaveState): SocialRealismAuditReport {
        val tier = getLeagueTier(state.currentClub.leagueId)
        val canAccess = canAccessSuperstarsAndGlobalMedia(state)
        val violations = mutableListOf<String>()

        if (!canAccess && tier.isLowerTier) {
            // 1. DMs kontrolü
            val dms = state.instaDmThreads
            for (th in dms) {
                val clean = th.senderHandle.removePrefix("@").lowercase().trim()
                if (clean in RESTRICTED_GLOBAL_HANDLES) {
                    violations.add("İHLAL: Alt ligde (${tier.titleTr}) kısıtlı global hesap DM listesinde: ${th.senderName} (${th.senderHandle})")
                }
            }

            // 2. Trendler kontrolü
            val trends = generateRealismTrendingTopics(state)
            if (trends.any { it.topicTitle.contains("Arda Güler", ignoreCase = true) || it.topicTitle.contains("Fabrizio", ignoreCase = true) }) {
                violations.add("İHLAL: Alt ligde Arda Güler veya Fabrizio Romano X trendlerinde yer alıyor.")
            }

            // 3. Bildirimler kontrolü
            val notifs = generateRealismNotifications(state, "INSTAGRAM") + generateRealismNotifications(state, "X")
            for (notif in notifs) {
                val clean = notif.actorHandle.removePrefix("@").lowercase().trim()
                if (clean in RESTRICTED_GLOBAL_HANDLES) {
                    violations.add("İHLAL: Alt ligde kısıtlı hesap bildirim gönderdi: ${notif.actorName} (${notif.actorHandle})")
                }
            }
        }

        return SocialRealismAuditReport(
            passed = violations.isEmpty(),
            currentLeagueTier = tier,
            canAccessSuperstars = canAccess,
            violationsFound = violations
        )
    }
}
