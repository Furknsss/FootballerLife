package com.example.engine.social

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.R
import com.example.model.CareerSaveState
import com.example.model.ChatMessage
import com.example.model.Club
import com.example.model.InstaCommentItem
import com.example.model.InstaDmThread
import com.example.model.SocialMediaPost
import com.example.model.StoryItem
import com.example.model.XPostItem
import com.example.ui.components.FLFormatters
import kotlin.math.abs

/**
 * Living Social Media World Generator & Interactive State Engine.
 * Produces a rich, persistent Turkish + global football social ecosystem for
 * Instagram, X (Twitter), WhatsApp Status, and Transfermarkt.
 */
object SocialWorldGenerator {

    // Interactive overlay states so every post (both persisted in CareerSaveState and living-world generated)
    // supports instant likes, saves, nested comment replies, X replies, X quotes, and account follow toggles.
    private val extraInstaLikes = mutableStateMapOf<String, Boolean>()
    private val extraInstaSaves = mutableStateMapOf<String, Boolean>()
    private val extraInstaComments = mutableStateMapOf<String, List<InstaCommentItem>>()
    private val extraCommentLikes = mutableStateMapOf<String, Boolean>()
    private val nestedCommentReplies = mutableStateMapOf<String, List<InstaCommentReply>>()
    private val extraReplyLikes = mutableStateMapOf<String, Boolean>()

    private val extraXLikes = mutableStateMapOf<String, Boolean>()
    private val extraXReposts = mutableStateMapOf<String, Boolean>()
    private val extraXBookmarks = mutableStateMapOf<String, Boolean>()
    private val extraXReplies = mutableStateMapOf<String, List<XReplyItem>>()
    private val extraReplyXLikes = mutableStateMapOf<String, Boolean>()
    private val userPublishedXPosts = mutableStateListOf<XPostItem>()
    private val quotedPostEmbeds = mutableStateMapOf<String, XQuotedPostEmbed>()

    private val accountFollowOverrides = mutableStateMapOf<String, Boolean>()
    private val customUserNotes = mutableStateMapOf<Int, String>()
    private val viewedStoryIds = mutableStateMapOf<String, Boolean>()
    private val reactedStoryIds = mutableStateMapOf<String, Boolean>()
    private val viewedWaStatusIds = mutableStateMapOf<String, Boolean>()
    private val extraDmThreads = mutableStateMapOf<String, InstaDmThread>()

    // Dynamic Infinite Scroll & Feed Pagination state
    private val appendedInstaPosts = mutableStateListOf<SocialMediaPost>()
    private val prependedInstaPosts = mutableStateListOf<SocialMediaPost>()
    private val currentInstaPage = mutableStateOf(0)

    private val appendedXPosts = mutableStateListOf<XPostItem>()
    private val prependedXPosts = mutableStateListOf<XPostItem>()
    private val currentXPage = mutableStateOf(0)

    // Dynamic comment like counters (which grow over time based on fame)
    private val dynamicCommentLikeCounts = mutableStateMapOf<String, Int>()
    private val extraNotifications = mutableStateListOf<SocialNotificationItem>()

    // =========================================================================
    // 0. FAME & REPUTATION ENGINE
    // =========================================================================

    fun calculatePlayerFameTier(state: CareerSaveState): PlayerFameTier {
        val p = state.player
        val leagueId = state.currentClub.leagueId
        val overall = p.overallRating
        val followers = p.followersCount.toLong()

        return when {
            followers >= 4_000_000L || overall >= 86 || p.marketValueEur >= 40_000_000L -> PlayerFameTier.GLOBAL_SUPERSTAR
            followers >= 800_000L || overall >= 80 || p.marketValueEur >= 15_000_000L -> PlayerFameTier.NATIONAL_HERO
            followers >= 120_000L || (overall >= 73 && (leagueId == "TR_SUPER_LIG" || leagueId.contains("L1"))) || p.marketValueEur >= 4_000_000L -> PlayerFameTier.SUPER_LIG_STAR
            followers >= 20_000L || overall >= 65 || leagueId == "TR_1_LIG" || p.marketValueEur >= 750_000L -> PlayerFameTier.RISING_STAR
            followers >= 2_000L || overall >= 54 || leagueId.startsWith("TR_2_LIG") || leagueId.startsWith("TR_3_LIG") -> PlayerFameTier.SEMI_PRO
            else -> PlayerFameTier.AMATEUR
        }
    }

    fun calculatePlayerCommentLikes(state: CareerSaveState): Int {
        val tier = calculatePlayerFameTier(state)
        val base = (tier.minLikes + tier.maxLikes) / 2
        val delta = (tier.maxLikes - tier.minLikes) / 3
        val variance = if (delta > 0) (abs(state.currentDayIndex.hashCode() + state.player.followersCount) % delta) else 5
        return (base + variance).coerceAtLeast(tier.minLikes)
    }

    fun calculatePlayerPostEngagement(state: CareerSaveState): Pair<Int, Int> {
        val tier = calculatePlayerFameTier(state)
        val likes = (tier.minLikes + (tier.maxLikes - tier.minLikes) * 0.65).toInt()
        val reposts = (tier.minReposts + (tier.maxReposts - tier.minReposts) * 0.60).toInt()
        return Pair(likes, reposts)
    }

    fun growCommentEngagement(commentId: String, state: CareerSaveState): Int {
        val tier = calculatePlayerFameTier(state)
        val current = dynamicCommentLikeCounts[commentId] ?: calculatePlayerCommentLikes(state)
        val growth = when (tier) {
            PlayerFameTier.AMATEUR -> (2..8).random()
            PlayerFameTier.SEMI_PRO -> (15..45).random()
            PlayerFameTier.RISING_STAR -> (120..450).random()
            PlayerFameTier.SUPER_LIG_STAR -> (850..3200).random()
            PlayerFameTier.NATIONAL_HERO -> (4500..18000).random()
            PlayerFameTier.GLOBAL_SUPERSTAR -> (25000..95000).random()
        }
        val updated = current + growth
        dynamicCommentLikeCounts[commentId] = updated
        return updated
    }

    // =========================================================================
    // 1. RICH ACCOUNT CATALOG (35+ REALISTIC ACCOUNTS)
    // =========================================================================

    fun getAccounts(state: CareerSaveState): List<SocialAccountProfile> {
        val p = state.player
        val club = state.currentClub
        val captain = club.keySquadMembers.find { it.isCaptain } ?: club.keySquadMembers.firstOrNull()
        val secondTeammate = club.keySquadMembers.getOrNull(1)
        val rivalClub = state.clubs.firstOrNull { it.id != club.id && it.leagueId == club.leagueId }
            ?: state.clubs.lastOrNull() ?: club
        val userHandle = "@${p.firstName.lowercase()}${p.kitNumber}"

        val baseList = mutableListOf(
            SocialAccountProfile(
                id = "ACC_USER",
                handle = userHandle,
                displayName = p.fullName,
                category = SocialAccountCategory.PLAYER,
                isVerified = true,
                badgeType = VerifiedBadgeType.BLUE,
                avatarHex = club.primaryColorHex,
                bannerHex = club.secondaryColorHex,
                bio = "⚽ Profesyonel Futbolcu | @${club.shortName.lowercase()}resmi #${p.kitNumber}\n📍 ${club.city}, ${club.country} • ${p.primaryPosition.titleTr}\n🎯 ${p.backgroundStory} | Menajerlik: @kemalsoylu_mgmt",
                location = "${club.city}, Türkiye",
                website = "transfermarkt.com.tr/${p.firstName.lowercase()}-${p.lastName.lowercase()}",
                followersCountText = formatCompactCount(p.followersCount),
                followingCount = 348,
                postsCount = state.galleryPhotos.size + state.socialPosts.count { it.isUserPost },
                isFollowedByDefault = true,
                mutualFollowersText = "ardaguler, 433 ve ${club.shortName.lowercase()}resmi takip ediyor"
            ),
            SocialAccountProfile(
                id = "ACC_CLUB_OWN",
                handle = "@${club.shortName.lowercase()}resmi",
                displayName = "${club.name} SK",
                category = SocialAccountCategory.CLUB,
                isVerified = true,
                badgeType = VerifiedBadgeType.GOLD_ORG,
                avatarHex = club.primaryColorHex,
                bannerHex = club.secondaryColorHex,
                bio = "${club.name} Spor Kulübü Resmi Hesabı 🏆\n🏟️ ${club.stadiumName} (${club.stadiumCapacity} Kapasite)\n👔 Teknik Direktör: ${club.managerName} • #${club.shortName}",
                location = club.city,
                website = "${club.shortName.lowercase()}.org.tr",
                followersCountText = formatCompactCount(club.stadiumCapacity * 28 + 145_000),
                followingCount = 84,
                postsCount = 4_820,
                isFollowedByDefault = true,
                mutualFollowersText = "Takım arkadaşların ve ${formatCompactCount(p.followersCount)} taraftar takip ediyor"
            ),
            SocialAccountProfile(
                id = "ACC_CLUB_RIVAL",
                handle = "@${rivalClub.shortName.lowercase()}resmi",
                displayName = "${rivalClub.name} SK",
                category = SocialAccountCategory.CLUB,
                isVerified = true,
                badgeType = VerifiedBadgeType.GOLD_ORG,
                avatarHex = rivalClub.primaryColorHex,
                bannerHex = rivalClub.secondaryColorHex,
                bio = "${rivalClub.name} Resmi Sosyal Medya Hesabı ⚽\n🏟️ ${rivalClub.stadiumName} • ${rivalClub.leagueName}",
                location = rivalClub.city,
                website = "${rivalClub.shortName.lowercase()}.com.tr",
                followersCountText = formatCompactCount(rivalClub.stadiumCapacity * 24 + 110_000),
                followingCount = 62,
                postsCount = 3_910,
                isFollowedByDefault = false
            ),
            SocialAccountProfile(
                id = "ACC_FABRIZIO",
                handle = "@fabrizioromano",
                displayName = "Fabrizio Romano",
                category = SocialAccountCategory.JOURNALIST,
                isVerified = true,
                badgeType = VerifiedBadgeType.BLUE,
                avatarHex = "#1D9BF0",
                bannerHex = "#0B2545",
                bio = "Here we go! 🚨 Global Football Transfer Journalist.\nSky Sport | The Guardian | CaughtOffside 🇮🇹⚽",
                location = "Milano, Italia",
                website = "youtube.com/c/FabrizioRomano",
                followersCountText = "32,8M",
                followingCount = 1_420,
                postsCount = 19_450,
                isFollowedByDefault = true,
                mutualFollowersText = "ardaguler, 433 ve hakancalhanoglu takip ediyor"
            ),
            SocialAccountProfile(
                id = "ACC_433",
                handle = "@433",
                displayName = "433",
                category = SocialAccountCategory.MEDIA,
                isVerified = true,
                badgeType = VerifiedBadgeType.GOLD_ORG,
                avatarHex = "#FF3B30",
                bannerHex = "#111111",
                bio = "The Home of Football ⚽🌍\nSoyunma odasından dünyanın en büyük stadyumlarına futbolun kalbi.",
                location = "Amsterdam / Worldwide",
                website = "433football.com",
                followersCountText = "74,6M",
                followingCount = 2_190,
                postsCount = 34_100,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_TRANSFERMARKT",
                handle = "@transfermarkt_tr",
                displayName = "Transfermarkt Türkiye",
                category = SocialAccountCategory.ANALYST,
                isVerified = true,
                badgeType = VerifiedBadgeType.GOLD_ORG,
                avatarHex = "#00193F",
                bannerHex = "#00A6FF",
                customLogoRes = R.drawable.logo_transfermarkt,
                bio = "📊 Futbol dünyasının en büyük veri tabanı: Piyasa değerleri, transfer söylentileri, istatistikler ve kadro analizleri. 🇹🇷📈",
                location = "İstanbul / Hamburg",
                website = "transfermarkt.com.tr",
                followersCountText = "1,9M",
                followingCount = 410,
                postsCount = 14_800,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_YAGIZ",
                handle = "@yagosabuncuoglu",
                displayName = "Yağız Sabuncuoğlu",
                category = SocialAccountCategory.JOURNALIST,
                isVerified = true,
                badgeType = VerifiedBadgeType.BLUE,
                avatarHex = "#0EA5E9",
                bannerHex = "#0F172A",
                bio = "Sports Digitale | Özel Haber & Transfer Kulis 🔎🇹🇷\n\"ÖZEL | Resmi teklif yapıldı.\"",
                location = "İstanbul, Türkiye",
                website = "youtube.com/@SportsDigitale",
                followersCountText = "1,6M",
                followingCount = 890,
                postsCount = 28_400,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_ERTAN",
                handle = "@ertansuzgun",
                displayName = "Ertan Süzgün",
                category = SocialAccountCategory.JOURNALIST,
                isVerified = true,
                badgeType = VerifiedBadgeType.BLUE,
                avatarHex = "#F59E0B",
                bannerHex = "#1E293B",
                bio = "Spor Gazetecisi • Süper Lig & Alt Ligler Transfer Kulisleri 🎯\nKontraspor & Dijital Medya",
                location = "İstanbul",
                followersCountText = "640B",
                followingCount = 720,
                postsCount = 16_300,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_SPORTS_DIGITALE",
                handle = "@sportsdigitale",
                displayName = "Sports Digitale",
                category = SocialAccountCategory.MEDIA,
                isVerified = true,
                badgeType = VerifiedBadgeType.GOLD_ORG,
                avatarHex = "#10B981",
                bannerHex = "#064E3B",
                bio = "Yeni Nesil Spor Medyası 🎙️📺\nMaç sonu analizleri, özel röportajlar, transfer dosyaları ve taktik programlar.",
                location = "İstanbul, Türkiye",
                website = "sportsdigitale.com",
                followersCountText = "1,2M",
                followingCount = 185,
                postsCount = 21_900,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_SOKRATES",
                handle = "@sokratesdergi",
                displayName = "Sokrates Dergi",
                category = SocialAccountCategory.MEDIA,
                isVerified = true,
                badgeType = VerifiedBadgeType.GOLD_ORG,
                avatarHex = "#EC4899",
                bannerHex = "#31102F",
                bio = "Düşünen Spor Dergisi 📚🎙️\nSokrates Podcasts, futbol kültürü, derin analizler ve spor hikayeleri.",
                location = "İstanbul",
                website = "sokratesdergi.com",
                followersCountText = "580B",
                followingCount = 230,
                postsCount = 8_700,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_OPTACAN",
                handle = "@optacan",
                displayName = "OptaCan",
                category = SocialAccountCategory.ANALYST,
                isVerified = true,
                badgeType = VerifiedBadgeType.GOLD_ORG,
                avatarHex = "#6366F1",
                bannerHex = "#1E1B4B",
                bio = "Stats Perform'un resmi Türkçe futbol veri hesabı. 📊\nTürk futbolunun en detaylı istatistikleri. Tek kelime.",
                location = "İstanbul / Londra",
                website = "statsperform.com",
                followersCountText = "495B",
                followingCount = 120,
                postsCount = 31_200,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_BEIN",
                handle = "@beinsportstr",
                displayName = "beIN SPORTS Türkiye",
                category = SocialAccountCategory.MEDIA,
                isVerified = true,
                badgeType = VerifiedBadgeType.GOLD_ORG,
                avatarHex = "#7C3AED",
                bannerHex = "#2E1065",
                bio = "Futbolun Kalbi Burada Atıyor! 📺⚽\nMaç özetleri, goller, flaş röportajlar ve Trio tartışmaları.",
                location = "İstanbul",
                website = "beinsports.com.tr",
                followersCountText = "2,8M",
                followingCount = 95,
                postsCount = 42_000,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_MACKOLIK",
                handle = "@mackolik",
                displayName = "Mackolik",
                category = SocialAccountCategory.MEDIA,
                isVerified = true,
                badgeType = VerifiedBadgeType.GOLD_ORG,
                avatarHex = "#2563EB",
                bannerHex = "#1E3A8A",
                bio = "Canlı skorlar, puan durumları, ilk 11'ler ve son dakika spor haberleri! 📱⚽",
                location = "İstanbul",
                website = "mackolik.com",
                followersCountText = "2,1M",
                followingCount = 110,
                postsCount = 54_000,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_NOCONTEXT",
                handle = "@nocontextsuperlig",
                displayName = "No Context Türk Futbolu",
                category = SocialAccountCategory.MEME_PAGE,
                isVerified = true,
                badgeType = VerifiedBadgeType.BLUE,
                avatarHex = "#EAB308",
                bannerHex = "#422006",
                bio = "Türk futbolunun açıklanamayan anları, deplasman otobüsleri ve 90+7 kaosları 😂🔥\nDM'den kaos gönderin 👇",
                location = "Anadolu Deplasmanı",
                followersCountText = "910B",
                followingCount = 45,
                postsCount = 6_420,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_KRAMPON",
                handle = "@kramponsports",
                displayName = "Krampon Sports",
                category = SocialAccountCategory.MEME_PAGE,
                isVerified = true,
                badgeType = VerifiedBadgeType.BLUE,
                avatarHex = "#22C55E",
                bannerHex = "#052E16",
                bio = "Futbol gündemi, tribün mizahı, sıcak gelişmeler ve soyunma odası geyikleri ⚡⚽",
                location = "Türkiye",
                followersCountText = "1,1M",
                followingCount = 210,
                postsCount = 18_900,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_MIZAH",
                handle = "@futbolmizahtr",
                displayName = "Futbol Mizah TR",
                category = SocialAccountCategory.MEME_PAGE,
                isVerified = false,
                badgeType = VerifiedBadgeType.NONE,
                avatarHex = "#F97316",
                bannerHex = "#431407",
                bio = "Hocam vallahi topa müdahale (rakibin kaval kemiği kırıldı) 🤣⚽\nMizah amaçlıdır.",
                location = "Halı Saha",
                followersCountText = "380B",
                followingCount = 140,
                postsCount = 4_120,
                isFollowedByDefault = false
            ),
            SocialAccountProfile(
                id = "ACC_ARDA",
                handle = "@ardaguler",
                displayName = "Arda Güler",
                category = SocialAccountCategory.PLAYER,
                isVerified = true,
                badgeType = VerifiedBadgeType.BLUE,
                avatarHex = "#F59E0B",
                bannerHex = "#1E293B",
                bio = "Real Madrid C.F. & @millitakimlar 🇹🇷🤍 #15\nTevekkül.",
                location = "Madrid, España",
                followersCountText = "14,8M",
                followingCount = 412,
                postsCount = 186,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_HAKAN",
                handle = "@hakancalhanoglu",
                displayName = "Hakan Çalhanoğlu",
                category = SocialAccountCategory.PLAYER,
                isVerified = true,
                badgeType = VerifiedBadgeType.BLUE,
                avatarHex = "#1D4ED8",
                bannerHex = "#0F172A",
                bio = "FC Internazionale Milano #20 ⚫🔵 | A Milli Takım Kaptanı 🇹🇷",
                location = "Milano, Italia",
                followersCountText = "4,9M",
                followingCount = 520,
                postsCount = 640,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_KENAN",
                handle = "@kenanyildiz_official",
                displayName = "Kenan Yıldız",
                category = SocialAccountCategory.PLAYER,
                isVerified = true,
                badgeType = VerifiedBadgeType.BLUE,
                avatarHex = "#111827",
                bannerHex = "#374151",
                bio = "Juventus #10 ⚪⚫ | @millitakimlar 🇹🇷👅",
                location = "Torino, Italia",
                followersCountText = "3,4M",
                followingCount = 390,
                postsCount = 142,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_KEREM",
                handle = "@keremakturkoglu",
                displayName = "Kerem Aktürkoğlu",
                category = SocialAccountCategory.PLAYER,
                isVerified = true,
                badgeType = VerifiedBadgeType.BLUE,
                avatarHex = "#EF4444",
                bannerHex = "#450A0A",
                bio = "SL Benfica 🦅 & Türkiye A Milli Futbol Takımı 🇹🇷🪄",
                location = "Lisboa, Portugal",
                followersCountText = "3,1M",
                followingCount = 460,
                postsCount = 315,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_SEMIH",
                handle = "@semihkilicsoy",
                displayName = "Semih Kılıçsoy",
                category = SocialAccountCategory.PLAYER,
                isVerified = true,
                badgeType = VerifiedBadgeType.BLUE,
                avatarHex = "#27272A",
                bannerHex = "#09090B",
                bio = "Profesyonel Futbolcu ⚽🇹🇷 | Gol & Hücum",
                location = "İstanbul",
                followersCountText = "1,4M",
                followingCount = 310,
                postsCount = 94,
                isFollowedByDefault = false
            ),
            SocialAccountProfile(
                id = "ACC_NIKE",
                handle = "@niketurkiye",
                displayName = "Nike Football Türkiye",
                category = SocialAccountCategory.SPONSOR,
                isVerified = true,
                badgeType = VerifiedBadgeType.GOLD_ORG,
                avatarHex = "#111827",
                bannerHex = "#000000",
                bio = "Just Do It. ✔️ Mercurial, Phantom & Tiempo ile sahada hızını konuştur. ⚡👟",
                location = "İstanbul / Beaverton",
                website = "nike.com/tr",
                followersCountText = "2,4M",
                followingCount = 65,
                postsCount = 1_850,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_PUMA",
                handle = "@pumafootball",
                displayName = "PUMA Football",
                category = SocialAccountCategory.SPONSOR,
                isVerified = true,
                badgeType = VerifiedBadgeType.GOLD_ORG,
                avatarHex = "#10B981",
                bannerHex = "#064E3B",
                bio = "FOREVER. FASTER. 🐆⚽ Future, Ultra & King krampon serisi.",
                location = "Global",
                website = "puma.com",
                followersCountText = "8,1M",
                followingCount = 140,
                postsCount = 3_420,
                isFollowedByDefault = false
            ),
            SocialAccountProfile(
                id = "ACC_ELVIN",
                handle = "@elvinyildirim",
                displayName = "Elvin Yıldırım",
                category = SocialAccountCategory.INFLUENCER,
                isVerified = true,
                badgeType = VerifiedBadgeType.BLUE,
                avatarHex = "#E1306C",
                bannerHex = "#831843",
                bio = "✨ Fashion, Lifestyle & Istanbul Diaries ☕📸\n📍 Nişantaşı / Milano • İş birlikleri için DM 💌",
                location = "İstanbul",
                followersCountText = "420B",
                followingCount = 615,
                postsCount = 540,
                isFollowedByDefault = false
            ),
            SocialAccountProfile(
                id = "ACC_ULTRAS",
                handle = "@${club.shortName.lowercase()}_ultras",
                displayName = "${club.shortName} Tribün & Ultras",
                category = SocialAccountCategory.FAN_ULTRAS,
                isVerified = false,
                badgeType = VerifiedBadgeType.NONE,
                avatarHex = club.primaryColorHex,
                bannerHex = club.secondaryColorHex,
                bio = "Yağmur çamur demeden ${club.name} peşinde! 🧣🔥\n${club.stadiumName} Kuzey Kale Arkası • #Saldır${club.shortName}",
                location = club.city,
                followersCountText = "68,4B",
                followingCount = 190,
                postsCount = 2_890,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_UGUR",
                handle = "@ugurkarakullukcu",
                displayName = "Uğur Karakullukçu",
                category = SocialAccountCategory.ANALYST,
                isVerified = true,
                badgeType = VerifiedBadgeType.BLUE,
                avatarHex = "#DC2626",
                bannerHex = "#450A0A",
                bio = "Futbol Yorumcusu & Scout Analiz 🎙️⚽\nTaktik, genç oyuncular ve Süper Lig dosyaları.",
                location = "İstanbul",
                followersCountText = "510B",
                followingCount = 680,
                postsCount = 24_100,
                isFollowedByDefault = true
            ),
            SocialAccountProfile(
                id = "ACC_AGENT",
                handle = "@kemalsoylu_mgmt",
                displayName = "Kemal Soylu Sports Management",
                category = SocialAccountCategory.AGENT,
                isVerified = true,
                badgeType = VerifiedBadgeType.BLUE,
                avatarHex = "#5DADE2",
                bannerHex = "#1B4F72",
                bio = "FIFA Lisanslı Futbolcu Temsilcisi & Kariyer Yönetimi 💼⚽\nAvrupa ve Türkiye transfer danışmanlığı.",
                location = "İstanbul / Frankfurt",
                followersCountText = "42,5B",
                followingCount = 310,
                postsCount = 415,
                isFollowedByDefault = true
            )
        )

        if (captain != null) {
            baseList.add(
                SocialAccountProfile(
                    id = "ACC_CAPTAIN",
                    handle = "@${captain.name.lowercase().replace(" ", "_")}",
                    displayName = captain.name,
                    category = SocialAccountCategory.PLAYER,
                    isVerified = true,
                    badgeType = VerifiedBadgeType.BLUE,
                    avatarHex = "#20CF79",
                    bannerHex = club.primaryColorHex,
                    bio = "⚽ @${club.shortName.lowercase()}resmi Kaptanı ©️ #${captain.kitNumber} | ${captain.position.titleTr}",
                    location = club.city,
                    followersCountText = formatCompactCount(captain.overall * 1850),
                    followingCount = 290,
                    postsCount = 128,
                    isFollowedByDefault = true
                )
            )
        }
        if (secondTeammate != null) {
            baseList.add(
                SocialAccountProfile(
                    id = "ACC_TEAMMATE_2",
                    handle = "@${secondTeammate.name.lowercase().replace(" ", "_")}",
                    displayName = secondTeammate.name,
                    category = SocialAccountCategory.PLAYER,
                    isVerified = secondTeammate.overall >= 70,
                    badgeType = if (secondTeammate.overall >= 70) VerifiedBadgeType.BLUE else VerifiedBadgeType.NONE,
                    avatarHex = "#3B82F6",
                    bannerHex = club.primaryColorHex,
                    bio = "Profesyonel Futbolcu • @${club.shortName.lowercase()}resmi #${secondTeammate.kitNumber} ⚽💪",
                    location = club.city,
                    followersCountText = formatCompactCount(secondTeammate.overall * 920),
                    followingCount = 410,
                    postsCount = 74,
                    isFollowedByDefault = true
                )
            )
        }

        return baseList
    }

    fun resolveAccount(handleOrName: String, state: CareerSaveState): SocialAccountProfile {
        val accounts = getAccounts(state)
        val clean = handleOrName.trim().removePrefix("@").lowercase()
        accounts.find {
            it.cleanHandle.lowercase() == clean ||
                it.displayName.lowercase() == clean ||
                it.cleanHandle.lowercase().contains(clean)
        }?.let { return it }

        val club = state.currentClub
        return SocialAccountProfile(
            id = "ACC_DYN_$clean",
            handle = "@${clean.replace(" ", "_")}",
            displayName = handleOrName.removePrefix("@"),
            category = SocialAccountCategory.FAN_ULTRAS,
            isVerified = clean.contains("resmi") || clean.contains("spor") || clean.contains("tv"),
            avatarHex = club.primaryColorHex,
            bio = "⚽ Futbol tutkunu • ${club.leagueName} & Avrupa futbolu takipçisi.",
            location = "Türkiye",
            followersCountText = "${(abs(clean.hashCode()) % 45 + 2)},${(abs(clean.hashCode()) % 9)}B",
            followingCount = 240 + (abs(clean.hashCode()) % 300),
            postsCount = 45 + (abs(clean.hashCode()) % 120),
            isFollowedByDefault = false
        )
    }

    fun isFollowingAccount(handle: String, state: CareerSaveState): Boolean {
        val key = handle.trim().removePrefix("@").lowercase()
        accountFollowOverrides[key]?.let { return it }
        return resolveAccount(handle, state).isFollowedByDefault
    }

    fun toggleFollowAccount(handle: String, state: CareerSaveState): Boolean {
        val key = handle.trim().removePrefix("@").lowercase()
        val current = isFollowingAccount(handle, state)
        val next = !current
        accountFollowOverrides[key] = next
        return next
    }

    // =========================================================================
    // 2. LIVING INSTAGRAM FEED (30+ VARIED POSTS: WORLD, LEAGUE, MEMES, US)
    // =========================================================================

    fun getLivingInstaFeed(state: CareerSaveState): List<SocialMediaPost> {
        val p = state.player
        val club = state.currentClub
        val day = state.currentDayIndex
        val rivalClub = state.clubs.firstOrNull { it.id != club.id && it.leagueId == club.leagueId }
            ?: state.clubs.lastOrNull() ?: club
        val thirdClub = state.clubs.filter { it.id != club.id && it.id != rivalClub.id }.firstOrNull() ?: rivalClub
        val leaderRow = state.standings.firstOrNull()
        val topScorer = state.topScorers.firstOrNull()
        val recentPlayedFixtures = state.fixtures.filter { it.played }.sortedByDescending { it.dayIndex }
        val otherClubMatch = recentPlayedFixtures.firstOrNull {
            it.homeClubId != club.id && it.awayClubId != club.id
        }
        val captain = club.keySquadMembers.find { it.isCaptain } ?: club.keySquadMembers.firstOrNull()

        val generatedWorldPosts = mutableListOf<SocialMediaPost>()

        // 1. Other League Match Result (Mackolik / beIN SPORTS) — NOT about us!
        if (otherClubMatch != null) {
            generatedWorldPosts.add(
                SocialMediaPost(
                    id = "LIV_IG_OTHER_MATCH_${otherClubMatch.id}",
                    dayIndex = otherClubMatch.dayIndex,
                    dateLabel = "3 saat önce",
                    authorName = "Mackolik",
                    authorHandle = "@mackolik",
                    isUserPost = false,
                    content = "🏁 MAÇ SONUCU | ${club.leagueName} ${otherClubMatch.week}. Hafta mücadelesinde ${otherClubMatch.homeClubName} ile ${otherClubMatch.awayClubName} karşı karşıya geldi!\n\n⚽ ${otherClubMatch.homeClubName} ${otherClubMatch.homeScore} - ${otherClubMatch.awayScore} ${otherClubMatch.awayClubName}\n🏟️ ${otherClubMatch.stadiumName}\n\nBu sonuçla ligde dengeler yeniden değişti! Sizce haftanın takımı kimdi? 👇",
                    likesCount = 18_420,
                    commentsCount = 492,
                    sentiment = "Tarafsız",
                    instaComments = listOf(
                        InstaCommentItem(
                            id = "CMT_OTH_1_${otherClubMatch.id}",
                            authorHandle = "${rivalClub.shortName.lowercase()}_taraftar",
                            authorName = "${rivalClub.shortName} Sevdalısı",
                            isVerified = false,
                            avatarHex = rivalClub.primaryColorHex,
                            text = "Hakem yine 90 dakika boyunca her şeye düdük çaldı, tempo yükselmedi bile!",
                            timeLabel = "2s",
                            likesCount = 148
                        ),
                        InstaCommentItem(
                            id = "CMT_OTH_2_${otherClubMatch.id}",
                            authorHandle = "optacan",
                            authorName = "OptaCan",
                            isVerified = true,
                            avatarHex = "#6366F1",
                            text = "Bu sezon ${club.leagueName}'de ikinci yarılarda en fazla puan toplayan iki takımın mücadelesiydi. Taktik.",
                            timeLabel = "2s",
                            likesCount = 312
                        ),
                        InstaCommentItem(
                            id = "CMT_OTH_3_${otherClubMatch.id}",
                            authorHandle = "nocontextsuperlig",
                            authorName = "No Context Türk Futbolu",
                            isVerified = true,
                            avatarHex = "#EAB308",
                            text = "88. dakikada taç atışı için 4 dakika tartışılan o efsane sekans 😂",
                            timeLabel = "1s",
                            likesCount = 890
                        )
                    ),
                    isVerifiedAuthor = true,
                    locationLabel = otherClubMatch.stadiumName,
                    imageSceneType = "STADIUM"
                )
            )
        }

        // 2. Arda Güler / Real Madrid Post (World Football)
        generatedWorldPosts.add(
            SocialMediaPost(
                id = "LIV_IG_ARDA_MADRID",
                dayIndex = day,
                dateLabel = "1 saat önce",
                authorName = "Arda Güler",
                authorHandle = "@ardaguler",
                isUserPost = false,
                content = "Santiago Bernabéu'da özel bir gece daha. 🤍✨ Destekleyen herkese teşekkürler! ¡Hala Madrid! 🇹🇷⚽ #AG15",
                likesCount = 1_485_200,
                commentsCount = 24_810,
                sentiment = "Destekleyici",
                instaComments = listOf(
                    InstaCommentItem(
                        id = "CMT_ARDA_1",
                        authorHandle = "433",
                        authorName = "433",
                        isVerified = true,
                        avatarHex = "#FF3B30",
                        text = "Turkish Starboy 🌟🇹🇷",
                        timeLabel = "55d",
                        likesCount = 14_200
                    ),
                    InstaCommentItem(
                        id = "CMT_ARDA_2",
                        authorHandle = "hakancalhanoglu",
                        authorName = "Hakan Çalhanoğlu",
                        isVerified = true,
                        avatarHex = "#1D4ED8",
                        text = "Maşallah kardeşim benim 👏🔥",
                        timeLabel = "42d",
                        likesCount = 9_450
                    ),
                    InstaCommentItem(
                        id = "CMT_ARDA_3",
                        authorHandle = "kenanyildiz_official",
                        authorName = "Kenan Yıldız",
                        isVerified = true,
                        avatarHex = "#111827",
                        text = "Baller 🪄🤍",
                        timeLabel = "30d",
                        likesCount = 6_180
                    )
                ),
                isVerifiedAuthor = true,
                locationLabel = "Estadio Santiago Bernabéu, Madrid",
                imageSceneType = "STADIUM"
            )
        )

        // 3. No Context Türk Futbolu — Meme Page Post
        generatedWorldPosts.add(
            SocialMediaPost(
                id = "LIV_IG_MEME_NOCONTEXT_1",
                dayIndex = day,
                dateLabel = "2 saat önce",
                authorName = "No Context Türk Futbolu",
                authorHandle = "@nocontextsuperlig",
                isUserPost = false,
                content = "Alt lig deplasmanında 1-0 öndeyken 82. dakikada hafif rüzgar esince kalecinin yerde geçirdiği süre: 😂⏱️🛌\n\n\"Hocam kramp girdi vallahi bak iki bacağıma birden!\"",
                likesCount = 64_310,
                commentsCount = 1_420,
                sentiment = "Tarafsız",
                instaComments = listOf(
                    InstaCommentItem(
                        id = "CMT_MEME_1",
                        authorHandle = "kramponsports",
                        authorName = "Krampon Sports",
                        isVerified = true,
                        avatarHex = "#22C55E",
                        text = "Yedek kulübesinden sahaya 3 tane top atılması detayı unutulmuş 😭",
                        timeLabel = "2s",
                        likesCount = 2_140
                    ),
                    InstaCommentItem(
                        id = "CMT_MEME_2",
                        authorHandle = "futbolmizahtr",
                        authorName = "Futbol Mizah TR",
                        isVerified = false,
                        avatarHex = "#F97316",
                        text = "Hakemin +4 uzatma verip onu da 93:45'te bitirmesi peki? 😂",
                        timeLabel = "1s",
                        likesCount = 645
                    )
                ),
                isVerifiedAuthor = true,
                locationLabel = "Anadolu Deplasmanı",
                imageSceneType = "STADIUM"
            )
        )

        // 4. Transfermarkt Türkiye — League Market Value & Top Scorers Update
        val scorerName = topScorer?.playerName ?: p.fullName
        val scorerClub = topScorer?.clubName ?: club.name
        val scorerGoals = topScorer?.goals ?: p.currentSeasonStats.goals
        generatedWorldPosts.add(
            SocialMediaPost(
                id = "LIV_IG_TM_UPDATE_D$day",
                dayIndex = day,
                dateLabel = "3 saat önce",
                authorName = "Transfermarkt Türkiye",
                authorHandle = "@transfermarkt_tr",
                isUserPost = false,
                content = "📊 GÜNCELLEME | ${club.leagueName}'de piyasa değeri en hızlı yükselen genç yetenekler ve gol krallığı yarışı!\n\n🥇 Gol Krallığı Lideri: $scorerName ($scorerClub) — $scorerGoals Gol\n💎 Vitrin Oyuncusu: ${p.fullName} (${club.shortName}) — ${FLFormatters.formatMarketValue(p.marketValueEur)} (Potansiyel: ${p.potential})\n🔥 Takip Edilen Kulüp: ${leaderRow?.clubName ?: club.name} (${leaderRow?.points ?: 0} Puan)\n\nSizce sezon sonunda piyasa değerini en çok kim katlar? 📈👇",
                likesCount = 34_890,
                commentsCount = 980,
                sentiment = "Destekleyici",
                instaComments = listOf(
                    InstaCommentItem(
                        id = "CMT_TM_1",
                        authorHandle = "${club.shortName.lowercase()}_ultras",
                        authorName = "${club.shortName} Tribün",
                        isVerified = false,
                        avatarHex = club.primaryColorHex,
                        text = "${p.fullName} en az 5 Milyon € eder admin, gözünüzü açın izleyin maçları! 🔥",
                        timeLabel = "2s",
                        likesCount = 412
                    ),
                    InstaCommentItem(
                        id = "CMT_TM_2",
                        authorHandle = "ugurkarakullukcu",
                        authorName = "Uğur Karakullukçu",
                        isVerified = true,
                        avatarHex = "#DC2626",
                        text = "${p.firstName}'in oyun içi karar kalitesi ve atletizmi bu ligin çok üzerinde. Devre arası teklif yağar.",
                        timeLabel = "1s",
                        likesCount = 875
                    )
                ),
                isVerifiedAuthor = true,
                locationLabel = "Transfermarkt Veri Merkezi",
                imageSceneType = "TRANSFER"
            )
        )

        // 5. Latest Match Reaction Post if player played a match recently
        state.lastMatchReport?.let { rep ->
            val won = (rep.homeClubId == club.id && rep.homeScore > rep.awayScore) ||
                (rep.awayClubId == club.id && rep.awayScore > rep.homeScore)
            val drew = rep.homeScore == rep.awayScore
            generatedWorldPosts.add(
                SocialMediaPost(
                    id = "LIV_IG_LAST_MATCH_${rep.fixtureId}",
                    dayIndex = day,
                    dateLabel = "4 saat önce",
                    authorName = "Sports Digitale",
                    authorHandle = "@sportsdigitale",
                    isUserPost = false,
                    content = "🎙️ MAÇ SONU ANALİZ | ${rep.homeClubName} ${rep.homeScore} - ${rep.awayScore} ${rep.awayClubName}\n\n📊 ${p.fullName} (#${p.kitNumber}) Maç Karnesi:\n• Süre: ${rep.userPlayedMinutes}' dk\n• Gol / Asist: ${rep.userGoals}G / ${rep.userAssists}A\n• İsabetli Pas: ${rep.userPassesCompleted}/${rep.userPassesAttempted}\n• Reyting: ⭐ ${rep.userRating}\n\n${if (won) "Kritik 3 puanda başrolü oynadı! 👏" else if (drew) "Zorlu maçta puanlar paylaşıldı." else "Mağlubiyete rağmen mücadelesiyle dikkat çekti."}",
                    likesCount = 41_200,
                    commentsCount = 740,
                    sentiment = if (rep.userRating >= 7.0) "Destekleyici" else "Tarafsız",
                    instaComments = listOf(
                        InstaCommentItem(
                            id = "CMT_SD_M1_${rep.fixtureId}",
                            authorHandle = "${club.shortName.lowercase()}_12numara",
                            authorName = "12. Numara",
                            isVerified = false,
                            avatarHex = club.primaryColorHex,
                            text = if (rep.userRating >= 7.0) "Sahada basmadık yer bırakmadı helal olsun sana çocuk! ❤️🔥" else "Daha fazla sorumluluk alması lazım, yeteneği var!",
                            timeLabel = "3s",
                            likesCount = 290
                        ),
                        InstaCommentItem(
                            id = "CMT_SD_M2_${rep.fixtureId}",
                            authorHandle = "beinsportstr",
                            authorName = "beIN SPORTS Türkiye",
                            isVerified = true,
                            avatarHex = "#7C3AED",
                            text = "Maçın özet görüntüleri ve ${p.fullName} özel klibi yayında! 📺⚽",
                            timeLabel = "2s",
                            likesCount = 540
                        )
                    ),
                    isVerifiedAuthor = true,
                    locationLabel = club.stadiumName,
                    imageSceneType = "STADIUM"
                )
            )
        }

        // 6. Kenan Yıldız & Hakan Çalhanoğlu Serie A Post (World Football)
        generatedWorldPosts.add(
            SocialMediaPost(
                id = "LIV_IG_SERIEA_TURKS",
                dayIndex = day,
                dateLabel = "5 saat önce",
                authorName = "433",
                authorHandle = "@433",
                isUserPost = false,
                content = "İtalya Serie A'da Türk rüzgarı esmeye devam ediyor! 🇹🇷🔥 @hakancalhanoglu (#20) vs @kenanyildiz_official (#10) — Derby d'Italia'da orta saha ve hücumun patronları! ⚽✨",
                likesCount = 612_400,
                commentsCount = 8_920,
                sentiment = "Destekleyici",
                instaComments = listOf(
                    InstaCommentItem(
                        id = "CMT_SERIEA_1",
                        authorHandle = "hakancalhanoglu",
                        authorName = "Hakan Çalhanoğlu",
                        isVerified = true,
                        avatarHex = "#1D4ED8",
                        text = "Kardeşimle güzel rekabet 🇹🇷💪 @kenanyildiz_official",
                        timeLabel = "4s",
                        likesCount = 11_300
                    ),
                    InstaCommentItem(
                        id = "CMT_SERIEA_2",
                        authorHandle = "kenanyildiz_official",
                        authorName = "Kenan Yıldız",
                        isVerified = true,
                        avatarHex = "#111827",
                        text = "Abim ❤️🇹🇷",
                        timeLabel = "3s",
                        likesCount = 8_420
                    )
                ),
                isVerifiedAuthor = true,
                locationLabel = "San Siro, Milano",
                imageSceneType = "STADIUM"
            )
        )

        // 7. Teammate / Captain Locker Room Selfie Post
        if (captain != null) {
            generatedWorldPosts.add(
                SocialMediaPost(
                    id = "LIV_IG_CAPTAIN_LOCKER",
                    dayIndex = day,
                    dateLabel = "6 saat önce",
                    authorName = captain.name,
                    authorHandle = "@${captain.name.lowercase().replace(" ", "_")}",
                    isUserPost = false,
                    content = "Aile ortamı, sıkı idman ve yüksek konsantrasyon! 💪⚽ Soyunma odasında yüzler gülüyor, hafta sonu ${club.stadiumName} tribünlerini kapalı gişe bekliyoruz! @${p.firstName.lowercase()}${p.kitNumber} #${club.shortName}",
                    likesCount = 9_840,
                    commentsCount = 264,
                    sentiment = "Destekleyici",
                    instaComments = listOf(
                        InstaCommentItem(
                            id = "CMT_CAP_1",
                            authorHandle = "${club.shortName.lowercase()}resmi",
                            authorName = "${club.name} SK",
                            isVerified = true,
                            avatarHex = club.primaryColorHex,
                            text = "Kaptan ve genç yıldızımız! 🔥👏",
                            timeLabel = "5s",
                            likesCount = 420
                        ),
                        InstaCommentItem(
                            id = "CMT_CAP_2",
                            authorHandle = "${club.shortName.lowercase()}_ultras",
                            authorName = "${club.shortName} Tribün",
                            isVerified = false,
                            avatarHex = club.primaryColorHex,
                            text = "Omuz omuza şampiyonluğa! Meşaleleri hazırladık bekliyoruz 🧣🔥",
                            timeLabel = "4s",
                            likesCount = 155
                        )
                    ),
                    isVerifiedAuthor = true,
                    locationLabel = "${club.name} Tesisleri",
                    imageSceneType = "LOCKER"
                )
            )
        }

        // 8. Krampon Sports — Turkish Football Culture & Banter
        generatedWorldPosts.add(
            SocialMediaPost(
                id = "LIV_IG_KRAMPON_CULTURE",
                dayIndex = day,
                dateLabel = "7 saat önce",
                authorName = "Krampon Sports",
                authorHandle = "@kramponsports",
                isUserPost = false,
                content = "🗣 Teknik direktörlerin antrenmanda genç oyunculara söylediği 3 klasik cümle:\n\n1️⃣ \"Oğlum basit oyna, ikiye bir yap!\"\n2️⃣ \"Kafanı kaldır da pas ver!\"\n3️⃣ \"88'de oyuna girip maçı çeviriyorsun hadi göreyim seni!\"\n\nSizin hocanın favori repliği hangisiydi? 👇😂⚽",
                likesCount = 52_900,
                commentsCount = 1_640,
                sentiment = "Tarafsız",
                instaComments = listOf(
                    InstaCommentItem(
                        id = "CMT_KRM_1",
                        authorHandle = "semihkilicsoy",
                        authorName = "Semih Kılıçsoy",
                        isVerified = true,
                        avatarHex = "#27272A",
                        text = "😂😂😂",
                        timeLabel = "6s",
                        likesCount = 3_410
                    ),
                    InstaCommentItem(
                        id = "CMT_KRM_2",
                        authorHandle = "sokratesdergi",
                        authorName = "Sokrates Dergi",
                        isVerified = true,
                        avatarHex = "#EC4899",
                        text = "\"Çizgiye in orta kes\" klasiğini de unutmayalım.",
                        timeLabel = "5s",
                        likesCount = 720
                    )
                ),
                isVerifiedAuthor = true,
                locationLabel = "Türkiye",
                imageSceneType = "TRAINING"
            )
        )

        // 9. Nike Football Türkiye — Boots & Sponsor Campaign
        generatedWorldPosts.add(
            SocialMediaPost(
                id = "LIV_IG_NIKE_CAMPAIGN",
                dayIndex = day,
                dateLabel = "9 saat önce",
                authorName = "Nike Football Türkiye",
                authorHandle = "@niketurkiye",
                isUserPost = false,
                content = "⚡ HIZIN YENİ TANIMI: Nike Mercurial Vapor 16 Elite.\nSahada ilk adımı atan kazanır. Yeni nesil yıldızlar hızıyla fark yaratmaya hazır! 👟🔥 #Mercurial #JustDoIt",
                likesCount = 78_500,
                commentsCount = 610,
                sentiment = "Destekleyici",
                instaComments = listOf(
                    InstaCommentItem(
                        id = "CMT_NIKE_1",
                        authorHandle = "elvinyildirim",
                        authorName = "Elvin Yıldırım",
                        isVerified = true,
                        avatarHex = "#E1306C",
                        text = "Renk geçişleri inanılmaz olmuş 🔥✨",
                        timeLabel = "7s",
                        likesCount = 310
                    ),
                    InstaCommentItem(
                        id = "CMT_NIKE_2",
                        authorHandle = "kemalsoylu_mgmt",
                        authorName = "Kemal Soylu Management",
                        isVerified = true,
                        avatarHex = "#5DADE2",
                        text = "Genç yıldızımız @${p.firstName.lowercase()}${p.kitNumber} ayağında çok yakışacak! 🤝⚡",
                        timeLabel = "6s",
                        likesCount = 195
                    )
                ),
                isVerifiedAuthor = true,
                locationLabel = "İstanbul, Türkiye",
                imageSceneType = "TRAINING"
            )
        )

        // 10. Rival Club Post — Matchday / Training Hype
        generatedWorldPosts.add(
            SocialMediaPost(
                id = "LIV_IG_RIVAL_CLUB_${rivalClub.id}",
                dayIndex = day,
                dateLabel = "11 saat önce",
                authorName = "${rivalClub.name} SK",
                authorHandle = "@${rivalClub.shortName.lowercase()}resmi",
                isUserPost = false,
                content = "🔥 Hedef mutlak 3 puan! Takımımız ${rivalClub.stadiumName} tesislerinde taktik antrenmanını tamamladı. Bu ligde son sözü biz söyleyeceğiz! #${rivalClub.shortName} ⚽💪",
                likesCount = 16_240,
                commentsCount = 528,
                sentiment = "Eleştirel",
                instaComments = listOf(
                    InstaCommentItem(
                        id = "CMT_RIV_1",
                        authorHandle = "${club.shortName.lowercase()}_ultras",
                        authorName = "${club.shortName} Tribün",
                        isVerified = false,
                        avatarHex = club.primaryColorHex,
                        text = "Bizim maça kadar konuşun bakalım, ${club.stadiumName}'da görüşeceğiz! 😎🔥",
                        timeLabel = "9s",
                        likesCount = 490
                    ),
                    InstaCommentItem(
                        id = "CMT_RIV_2",
                        authorHandle = "${rivalClub.shortName.lowercase()}_taraftar",
                        authorName = "${rivalClub.shortName} Sevdalısı",
                        isVerified = false,
                        avatarHex = rivalClub.primaryColorHex,
                        text = "Şampiyon ${rivalClub.shortName} yazdıracağız!",
                        timeLabel = "8s",
                        likesCount = 215
                    )
                ),
                isVerifiedAuthor = true,
                locationLabel = rivalClub.stadiumName,
                imageSceneType = "TRAINING"
            )
        )

        // 11. Sokrates Dergi — Deep Football Culture Post
        generatedWorldPosts.add(
            SocialMediaPost(
                id = "LIV_IG_SOKRATES_CULTURE",
                dayIndex = day,
                dateLabel = "14 saat önce",
                authorName = "Sokrates Dergi",
                authorHandle = "@sokratesdergi",
                isUserPost = false,
                content = "📚 \"Futbolda bir genç oyuncunun sahaya ilk adım attığı o 10 saniye, bütün bir çocukluğun özetidir.\"\n\nBu ayki sayımızda alt liglerden Avrupa vitrinine uzanan genç futbolcu hikayelerini ve soyunma odası psikolojisini inceledik. 🎙️⚽",
                likesCount = 22_400,
                commentsCount = 310,
                sentiment = "Destekleyici",
                instaComments = listOf(
                    InstaCommentItem(
                        id = "CMT_SOK_1",
                        authorHandle = "ugurkarakullukcu",
                        authorName = "Uğur Karakullukçu",
                        isVerified = true,
                        avatarHex = "#DC2626",
                        text = "Harika bir dosya olmuş, özellikle altyapı geçiş bölümü muazzam 👏",
                        timeLabel = "12s",
                        likesCount = 380
                    )
                ),
                isVerifiedAuthor = true,
                locationLabel = "İstanbul",
                imageSceneType = "LOCKER"
            )
        )

        // 12. Elvin Yıldırım — Istanbul Lifestyle Post
        generatedWorldPosts.add(
            SocialMediaPost(
                id = "LIV_IG_ELVIN_LIFESTYLE",
                dayIndex = day,
                dateLabel = "16 saat önce",
                authorName = "Elvin Yıldırım",
                authorHandle = "@elvinyildirim",
                isUserPost = false,
                content = "Boğaz'da gün batımı ve haftanın yorgunluğunu atan bir kahve molası ☕✨ Hafta sonu stadyum atmosferini yerinde izlemeye karar verdim, hangi maça gidilmeli? 👀⚽",
                likesCount = 38_750,
                commentsCount = 845,
                sentiment = "Destekleyici",
                instaComments = listOf(
                    InstaCommentItem(
                        id = "CMT_ELV_1",
                        authorHandle = "${club.shortName.lowercase()}_ultras",
                        authorName = "${club.shortName} Tribün",
                        isVerified = false,
                        avatarHex = club.primaryColorHex,
                        text = "${club.stadiumName}'a gel abla, @${p.firstName.lowercase()}${p.kitNumber} şov izleyelim! 🔥⚽",
                        timeLabel = "14s",
                        likesCount = 610
                    )
                ),
                isVerifiedAuthor = true,
                locationLabel = "Bebek, İstanbul",
                imageSceneType = "TRANSFER"
            )
        )

        // Merge prependedInstaPosts + state.socialPosts + generatedWorldPosts + appendedInstaPosts without duplicate IDs
        val existingIds = state.socialPosts.map { it.id }.toSet()
        val combined = prependedInstaPosts + state.socialPosts + generatedWorldPosts.filter { it.id !in existingIds } + appendedInstaPosts

        // Apply interactive overrides (likes, saves, extra user comments)
        return combined.map { post ->
            val likedOverride = extraInstaLikes[post.id]
            val savedOverride = extraInstaSaves[post.id]
            val addedComments = extraInstaComments[post.id].orEmpty()
            val finalLiked = likedOverride ?: post.likedByUser
            val likeDelta = when {
                likedOverride == true && !post.likedByUser -> 1
                likedOverride == false && post.likedByUser -> -1
                else -> 0
            }
            val mergedComments = (addedComments + post.instaComments).map { cmt ->
                val cLiked = extraCommentLikes[cmt.id] ?: cmt.likedByUser
                val cDelta = when {
                    extraCommentLikes[cmt.id] == true && !cmt.likedByUser -> 1
                    extraCommentLikes[cmt.id] == false && cmt.likedByUser -> -1
                    else -> 0
                }
                val baseLikes = dynamicCommentLikeCounts[cmt.id] ?: cmt.likesCount
                cmt.copy(
                    likedByUser = cLiked,
                    likesCount = (baseLikes + cDelta).coerceAtLeast(0)
                )
            }
            post.copy(
                likedByUser = finalLiked,
                savedByUser = savedOverride ?: post.savedByUser,
                likesCount = (post.likesCount + likeDelta).coerceAtLeast(0),
                commentsCount = post.commentsCount + addedComments.size,
                instaComments = mergedComments
            )
        }
    }

    // =========================================================================
    // 3. NESTED INSTAGRAM COMMENT REPLIES
    // =========================================================================

    fun getRepliesForComment(postId: String, comment: InstaCommentItem, state: CareerSaveState): List<InstaCommentReply> {
        val userAdded = nestedCommentReplies[comment.id].orEmpty()
        val p = state.player
        val club = state.currentClub

        val isPlayerComment = comment.authorHandle.contains(p.firstName.lowercase()) ||
            comment.id.startsWith("IC_LIV_USR_") ||
            userAdded.any { it.authorHandle.contains(p.firstName.lowercase()) }

        val defaultReplies = when {
            comment.authorHandle.contains("433") -> listOf(
                InstaCommentReply(
                    id = "REP_${comment.id}_1",
                    parentCommentId = comment.id,
                    postId = postId,
                    authorHandle = "@${club.shortName.lowercase()}_ultras",
                    authorName = "${club.shortName} Tribün",
                    isVerified = false,
                    avatarHex = club.primaryColorHex,
                    text = "@433 Admin knows ball! 🇹🇷🔥",
                    timeLabel = "1s",
                    likesCount = 84,
                    reactionType = CommentReactionType.FAN_SUPPORT
                ),
                InstaCommentReply(
                    id = "REP_${comment.id}_2",
                    parentCommentId = comment.id,
                    postId = postId,
                    authorHandle = "@kramponsports",
                    authorName = "Krampon Sports",
                    isVerified = true,
                    avatarHex = "#22C55E",
                    text = "@433 yakında her hafta paylaşacaksınız bu ismi 👀",
                    timeLabel = "45d",
                    likesCount = 42,
                    reactionType = CommentReactionType.MEME_CULTURE
                )
            )
            comment.authorHandle.contains("arda") -> listOf(
                InstaCommentReply(
                    id = "REP_${comment.id}_ARDA",
                    parentCommentId = comment.id,
                    postId = postId,
                    authorHandle = "@${club.shortName.lowercase()}_12numara",
                    authorName = "12. Numara",
                    isVerified = false,
                    avatarHex = club.primaryColorHex,
                    text = "@ardaguler Milli takımda ikinizi yan yana izlemek hayalimiz! 🇹🇷❤️",
                    timeLabel = "25d",
                    likesCount = 310,
                    reactionType = CommentReactionType.FAN_SUPPORT
                )
            )
            comment.authorHandle.contains("transfermarkt") -> listOf(
                InstaCommentReply(
                    id = "REP_${comment.id}_TM",
                    parentCommentId = comment.id,
                    postId = postId,
                    authorHandle = "@kemalsoylu_mgmt",
                    authorName = "Kemal Soylu Management",
                    isVerified = true,
                    avatarHex = "#5DADE2",
                    text = "@transfermarkt_tr Bir sonraki güncellemede en az 2 katına çıkacaktır 📈💼",
                    timeLabel = "30d",
                    likesCount = 56,
                    reactionType = CommentReactionType.PRESS_CRITIC
                )
            )
            else -> listOf(
                InstaCommentReply(
                    id = "REP_${comment.id}_DEF",
                    parentCommentId = comment.id,
                    postId = postId,
                    authorHandle = "@${club.shortName.lowercase()}_ruhu",
                    authorName = "${club.shortName} Ruhu",
                    isVerified = false,
                    avatarHex = club.primaryColorHex,
                    text = "${comment.authorHandle} Kesinlikle katılıyorum, bu sezon çok farklı olacak! 👏⚽",
                    timeLabel = "20d",
                    likesCount = 14,
                    reactionType = CommentReactionType.FAN_SUPPORT
                )
            )
        }

        val allReplies = if (isPlayerComment && userAdded.isNotEmpty()) {
            userAdded
        } else {
            defaultReplies + userAdded
        }

        return allReplies.map { rep ->
            val liked = extraReplyLikes[rep.id] ?: rep.likedByUser
            val delta = when {
                extraReplyLikes[rep.id] == true && !rep.likedByUser -> 1
                extraReplyLikes[rep.id] == false && rep.likedByUser -> -1
                else -> 0
            }
            val baseLikes = dynamicCommentLikeCounts[rep.id] ?: rep.likesCount
            rep.copy(likedByUser = liked, likesCount = (baseLikes + delta).coerceAtLeast(0))
        }
    }

    fun addInstaCommentOrReply(
        state: CareerSaveState,
        postId: String,
        text: String,
        replyToComment: InstaCommentItem? = null
    ) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        val p = state.player
        val club = state.currentClub
        val rivalClub = state.clubs.firstOrNull { it.id != club.id } ?: club
        val captain = club.keySquadMembers.find { it.isCaptain } ?: club.keySquadMembers.firstOrNull()
        val userHandle = "@${p.firstName.lowercase()}${p.kitNumber}"
        val fameTier = calculatePlayerFameTier(state)
        val initialPlayerLikes = calculatePlayerCommentLikes(state)

        if (replyToComment != null) {
            val userReplyId = "REP_USR_${System.currentTimeMillis()}"
            val likes = (initialPlayerLikes * 0.65).toInt().coerceAtLeast(3)
            dynamicCommentLikeCounts[userReplyId] = likes

            val userReply = InstaCommentReply(
                id = userReplyId,
                parentCommentId = replyToComment.id,
                postId = postId,
                authorHandle = userHandle,
                authorName = p.fullName,
                isVerified = true,
                avatarHex = club.primaryColorHex,
                text = if (clean.startsWith("@")) clean else "${replyToComment.authorHandle} $clean",
                timeLabel = "Şimdi",
                likesCount = likes,
                reactionType = CommentReactionType.TEAMMATE
            )
            val fanReaction = InstaCommentReply(
                id = "REP_FAN_${System.currentTimeMillis() + 1}",
                parentCommentId = replyToComment.id,
                postId = postId,
                authorHandle = "@${club.shortName.lowercase()}_ultras",
                authorName = "${club.shortName} Tribün",
                isVerified = false,
                avatarHex = club.primaryColorHex,
                text = "$userHandle Kraldan beklenen cevap geldi! Sahada cevabını ver geç aslanım 👏🔥",
                timeLabel = "Şimdi",
                likesCount = (likes * 0.40).toInt().coerceAtLeast(2),
                reactionType = CommentReactionType.FAN_SUPPORT
            )
            val trollReaction = InstaCommentReply(
                id = "REP_TRL_${System.currentTimeMillis() + 2}",
                parentCommentId = replyToComment.id,
                postId = postId,
                authorHandle = "@anti_${club.shortName.lowercase()}",
                authorName = "Tribün Gerçekleri",
                isVerified = false,
                avatarHex = rivalClub.primaryColorHex,
                text = "$userHandle Bahane üretmeyi bırak da takıma katkı ver, boş yapma!",
                timeLabel = "Şimdi",
                likesCount = (likes * 0.20).toInt().coerceAtLeast(1),
                reactionType = CommentReactionType.RIVAL_TROLL
            )
            val existing = nestedCommentReplies[replyToComment.id].orEmpty()
            nestedCommentReplies[replyToComment.id] = existing + listOf(userReply, fanReaction, trollReaction)

            extraNotifications.add(
                0,
                SocialNotificationItem(
                    id = "NOTIF_IG_REP_${System.currentTimeMillis()}",
                    platform = "INSTAGRAM",
                    type = "COMMENT_REPLY",
                    actorName = "${club.shortName} Tribün",
                    actorHandle = "@${club.shortName.lowercase()}_ultras",
                    actorAvatarHex = club.primaryColorHex,
                    actorVerified = false,
                    text = "yorumunuza yanıt verdi: 'Kraldan beklenen cevap geldi! 👏🔥'",
                    timeLabel = "Şimdi",
                    targetPostId = postId,
                    dayIndex = state.currentDayIndex
                )
            )
        } else {
            val cmtId = "IC_LIV_USR_${System.currentTimeMillis()}"
            dynamicCommentLikeCounts[cmtId] = initialPlayerLikes

            val newCmt = InstaCommentItem(
                id = cmtId,
                authorHandle = userHandle,
                authorName = p.fullName,
                isVerified = true,
                avatarHex = club.primaryColorHex,
                text = clean,
                timeLabel = "Şimdi",
                likesCount = initialPlayerLikes
            )
            val existing = extraInstaComments[postId].orEmpty()
            extraInstaComments[postId] = listOf(newCmt) + existing

            // Generate 3 to 4 realistic replies reacting to player's top-level comment
            val generatedReplies = mutableListOf<InstaCommentReply>()

            // 1. Loyal Fan Reaction
            val fanQuotes = listOf(
                "Kral sen sahadaki futboluna bak, arkandayız sonuna kadar! 🦁🔥",
                "Bu sezon forma arkasına senin adını yazdırdım, sakın moralini bozma!",
                "Geleceğin kaptanı! Milli takımda banko oynaman lazım 👏⚽",
                "İşte görmek istediğimiz aidiyet duygusu! Helal olsun sana!",
                "Senin gibi yürekli oynayanlar bu kulübe 10 yılda bir gelir! 💪"
            )
            generatedReplies.add(
                InstaCommentReply(
                    id = "REP_LIV_FAN_${System.currentTimeMillis() + 1}",
                    parentCommentId = cmtId,
                    postId = postId,
                    authorHandle = "@${club.shortName.lowercase()}_fanatik",
                    authorName = "${club.shortName} Sevdalısı",
                    isVerified = false,
                    avatarHex = club.primaryColorHex,
                    text = "$userHandle ${fanQuotes.random()}",
                    timeLabel = "Şimdi",
                    likesCount = (initialPlayerLikes * 0.42).toInt().coerceAtLeast(3),
                    reactionType = CommentReactionType.FAN_SUPPORT
                )
            )

            // 2. Rival Troll Reaction
            val trollQuotes = listOf(
                "Sen önce boş kaleye gol atmasını öğren de sonra buralarda ahkâm kes 😂",
                "Bizim stada gelince bacakların titriyor ama klavye başında aslansın haha",
                "Balon patladı! 3 haftadır sahada yürüyor gelmiş burada şov yapıyor 🤣",
                "Halı sahada bile yedek kulübesinde üşürsün birader boş yapma!",
                "Gözyaşlarınızı peçeteyle silin beyler, bu sene de hüsran sksksk"
            )
            generatedReplies.add(
                InstaCommentReply(
                    id = "REP_LIV_TRL_${System.currentTimeMillis() + 2}",
                    parentCommentId = cmtId,
                    postId = postId,
                    authorHandle = "@superlig_troll",
                    authorName = "Futbolun Gerçekleri",
                    isVerified = false,
                    avatarHex = rivalClub.primaryColorHex,
                    text = "$userHandle ${trollQuotes.random()}",
                    timeLabel = "Şimdi",
                    likesCount = (initialPlayerLikes * 0.22).toInt().coerceAtLeast(2),
                    reactionType = CommentReactionType.RIVAL_TROLL
                )
            )

            // 3. Meme / Pop Culture Reaction
            val memeQuotes = listOf(
                "Admin bunu gördü ve taktik tahtasını çöpe attı sksksk 😂",
                "Hocan bunu görürse yarın çift idman yazdırır, rez al yerimizi alalım 😂🍿",
                "Arkada Müslüm Gürses - Affet çalıyor şu an 🎵",
                "Kramponları çıkarıp direkt klavyeye geçmiş kral saygılar 😂🔥",
                "Yorumu okurken fonda Şampiyonlar Ligi müziği çaldı aniden"
            )
            generatedReplies.add(
                InstaCommentReply(
                    id = "REP_LIV_MEME_${System.currentTimeMillis() + 3}",
                    parentCommentId = cmtId,
                    postId = postId,
                    authorHandle = "@nocontextsuperlig",
                    authorName = "No Context Türk Futbolu",
                    isVerified = true,
                    avatarHex = "#EAB308",
                    text = "$userHandle ${memeQuotes.random()}",
                    timeLabel = "Şimdi",
                    likesCount = (initialPlayerLikes * 0.35).toInt().coerceAtLeast(2),
                    reactionType = CommentReactionType.MEME_CULTURE
                )
            )

            // 4. Teammate or Press Reaction (if fame >= RISING_STAR or random chance)
            if (fameTier.ordinal >= PlayerFameTier.RISING_STAR.ordinal || captain != null) {
                val isCaptainReply = captain != null
                generatedReplies.add(
                    InstaCommentReply(
                        id = "REP_LIV_PEER_${System.currentTimeMillis() + 4}",
                        parentCommentId = cmtId,
                        postId = postId,
                        authorHandle = if (isCaptainReply) "@${captain!!.name.lowercase().replace(" ", "_")}" else "@yagosabuncuoglu",
                        authorName = if (isCaptainReply) captain!!.name else "Yağız Sabuncuoğlu",
                        isVerified = true,
                        avatarHex = if (isCaptainReply) club.primaryColorHex else "#0EA5E9",
                        text = if (isCaptainReply) "$userHandle Bro yarın sabah 08:30'da salondayız, yorumlarda mesaiyi bırak dinlen haha 💪⚽"
                               else "$userHandle Bu açıklamanın ardından soyunma odası kulisleri hareketlendi 🔎🇹🇷",
                        timeLabel = "Şimdi",
                        likesCount = (initialPlayerLikes * 0.65).toInt().coerceAtLeast(8),
                        reactionType = if (isCaptainReply) CommentReactionType.TEAMMATE else CommentReactionType.PRESS_CRITIC
                    )
                )
            }

            nestedCommentReplies[cmtId] = generatedReplies

            // Add notifications for player's comment activity
            extraNotifications.add(
                0,
                SocialNotificationItem(
                    id = "NOTIF_IG_REP_${System.currentTimeMillis()}",
                    platform = "INSTAGRAM",
                    type = "COMMENT_REPLY",
                    actorName = "${club.shortName} Sevdalısı",
                    actorHandle = "@${club.shortName.lowercase()}_fanatik",
                    actorAvatarHex = club.primaryColorHex,
                    actorVerified = false,
                    text = "yorumunuza yanıt verdi: 'Kral sen sahada konuş, arkandayız!'",
                    timeLabel = "Şimdi",
                    targetPostId = postId,
                    dayIndex = state.currentDayIndex
                )
            )
            extraNotifications.add(
                0,
                SocialNotificationItem(
                    id = "NOTIF_IG_LIKE_${System.currentTimeMillis() + 1}",
                    platform = "INSTAGRAM",
                    type = "LIKE",
                    actorName = "${club.shortName} Taraftarları",
                    actorHandle = "@${club.shortName.lowercase()}_ultras",
                    actorAvatarHex = club.primaryColorHex,
                    actorVerified = true,
                    text = "yorumunuz ${FLFormatters.formatCompactNumber(initialPlayerLikes.toLong())} beğeniye ulaştı! 🔥",
                    timeLabel = "Şimdi",
                    targetPostId = postId,
                    dayIndex = state.currentDayIndex
                )
            )
        }
    }

    fun toggleInstaPostLikeLocal(postId: String, currentLiked: Boolean) {
        extraInstaLikes[postId] = !currentLiked
    }

    fun toggleInstaPostSaveLocal(postId: String, currentSaved: Boolean) {
        extraInstaSaves[postId] = !currentSaved
    }

    fun toggleInstaCommentLikeLocal(commentId: String, currentLiked: Boolean) {
        extraCommentLikes[commentId] = !currentLiked
    }

    fun toggleInstaReplyLikeLocal(replyId: String, currentLiked: Boolean) {
        extraReplyLikes[replyId] = !currentLiked
    }

    // =========================================================================
    // 4. LIVING STORIES & INSTAGRAM NOTES
    // =========================================================================

    fun getLivingStories(state: CareerSaveState): List<StoryItem> {
        val p = state.player
        val club = state.currentClub
        val day = state.currentDayIndex

        val extraStories = listOf(
            StoryItem(
                id = "ST_LIV_ARDA",
                authorName = "ardaguler",
                authorRole = "Real Madrid • #15",
                avatarHex = "#F59E0B",
                title = "Valdebebas Antrenman Günü 🤍⚽",
                caption = "Şampiyonlar Ligi haftası hazırlıkları tam gaz devam ediyor. #HalaMadrid",
                sceneType = "TRAINING",
                dayIndex = day
            ),
            StoryItem(
                id = "ST_LIV_TM",
                authorName = "transfermarkt_tr",
                authorRole = "Piyasa Değerleri",
                avatarHex = "#00193F",
                title = "Günün Piyasa Değeri Vitrin Oyuncusu 📈",
                caption = "${p.fullName} (${club.shortName}) • Güncel Değer: ${FLFormatters.formatMarketValue(p.marketValueEur)} • Potansiyel: ${p.potential}",
                sceneType = "TRANSFER",
                dayIndex = day
            ),
            StoryItem(
                id = "ST_LIV_SD",
                authorName = "sportsdigitale",
                authorRole = "Canlı Yayın",
                avatarHex = "#10B981",
                title = "YouTube'da Canlı Yayındayız! 🎙️🔴",
                caption = "Yağız Sabuncuoğlu ve Uğur Karakullukçu ile haftanın transfer gündemini konuşuyoruz.",
                sceneType = "TRANSFER",
                dayIndex = day
            ),
            StoryItem(
                id = "ST_LIV_NOCONTEXT",
                authorName = "nocontextsuperlig",
                authorRole = "Futbol Mizah",
                avatarHex = "#EAB308",
                title = "Anlık Deplasman Tribünü 😂🔥",
                caption = "Maç 0-0 iken 90+2'de kazanılan kornerde stadyumdaki tansiyon seviyesi!",
                sceneType = "STADIUM",
                dayIndex = day
            ),
            StoryItem(
                id = "ST_LIV_NIKE",
                authorName = "niketurkiye",
                authorRole = "Resmi Sponsor",
                avatarHex = "#111827",
                title = "Yeni Sezon Krampon Koleksiyonu ⚡👟",
                caption = "Mercurial Vapor 16 ile sahada hız sınırlarını zorla.",
                sceneType = "TRAINING",
                dayIndex = day
            ),
            StoryItem(
                id = "ST_LIV_ELVIN",
                authorName = "elvinyildirim",
                authorRole = "Lifestyle & Moda",
                avatarHex = "#E1306C",
                title = "Maç Günü Kombini ✨☕",
                caption = "Bu akşam locadan maç izlemeye hazırız! Bakalım kim kazanacak? 👀⚽",
                sceneType = "STADIUM",
                dayIndex = day
            ),
            StoryItem(
                id = "ST_LIV_HAKAN",
                authorName = "hakancalhanoglu",
                authorRole = "Inter • #20",
                avatarHex = "#1D4ED8",
                title = "San Siro Gece Işıkları ⚫🔵",
                caption = "Büyük maçlara hazırız. Forza Inter! 🇹🇷",
                sceneType = "STADIUM",
                dayIndex = day
            )
        )

        val existingIds = state.stories.map { it.id }.toSet()
        val combined = state.stories + extraStories.filter { it.id !in existingIds }

        return combined.map { st ->
            st.copy(
                viewed = viewedStoryIds[st.id] ?: st.viewed,
                reacted = reactedStoryIds[st.id] ?: st.reacted
            )
        }
    }

    fun markStoryViewedOrReactedLocal(storyId: String, react: Boolean) {
        viewedStoryIds[storyId] = true
        if (react) reactedStoryIds[storyId] = true
    }

    fun getInstaNotes(state: CareerSaveState): List<InstaNoteItem> {
        val p = state.player
        val club = state.currentClub
        val captain = club.keySquadMembers.find { it.isCaptain } ?: club.keySquadMembers.firstOrNull()
        val userNote = customUserNotes[state.saveSlotId] ?: "Maç günü odaklıyız ⚽🔥"

        return listOf(
            InstaNoteItem(
                id = "NOTE_USER",
                handle = "@${p.firstName.lowercase()}${p.kitNumber}",
                authorName = "Notun",
                avatarHex = club.primaryColorHex,
                noteText = userNote,
                isVerified = true,
                dmThreadId = null
            ),
            InstaNoteItem(
                id = "NOTE_ARDA",
                handle = "@ardaguler",
                authorName = "Arda Güler",
                avatarHex = "#F59E0B",
                noteText = "Hala Madrid! 🤍✨",
                isVerified = true,
                dmThreadId = "IDM_ARDA"
            ),
            InstaNoteItem(
                id = "NOTE_ELVIN",
                handle = "@elvinyildirim",
                authorName = "Elvin",
                avatarHex = "#E1306C",
                noteText = "Akşam kahvesi? ☕✨",
                isVerified = true,
                dmThreadId = "IDM_REQ_INFLUENCER"
            ),
            InstaNoteItem(
                id = "NOTE_CAPTAIN",
                handle = "@${(captain?.name ?: "kaptan").lowercase().replace(" ", "_")}",
                authorName = captain?.name?.substringBefore(" ") ?: "Kaptan",
                avatarHex = "#20CF79",
                noteText = "3 puan bizimdir 💪⚽",
                isVerified = true,
                dmThreadId = "IDM_CLUB_MEDIA"
            ),
            InstaNoteItem(
                id = "NOTE_FAB",
                handle = "@fabrizioromano",
                authorName = "Fabrizio",
                avatarHex = "#1D9BF0",
                noteText = "Here we go soon... ⏳🚨",
                isVerified = true,
                dmThreadId = "IDM_REQ_FABRIZIO"
            ),
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

    fun updateUserInstaNote(slotId: Int, text: String) {
        if (text.isNotBlank()) {
            customUserNotes[slotId] = text.trim()
        }
    }

    fun getOrCreateDmThreadForAccount(account: SocialAccountProfile, state: CareerSaveState): InstaDmThread {
        val clean = account.cleanHandle.lowercase()
        state.instaDmThreads.find {
            it.senderHandle.removePrefix("@").lowercase() == clean
        }?.let { return it }

        extraDmThreads[clean]?.let { return it }

        val newThread = InstaDmThread(
            id = "IDM_CUSTOM_${clean.uppercase()}",
            senderName = account.displayName,
            senderHandle = account.handle,
            senderRole = account.category.labelTr,
            avatarHex = account.avatarHex,
            isVerified = account.isVerified,
            followersLabel = "${account.followersCountText} takipçi",
            isPendingRequest = false,
            unreadCount = 0,
            messages = listOf(
                ChatMessage(
                    id = "IDM_INIT_${clean}_${System.currentTimeMillis()}",
                    senderName = account.displayName,
                    isFromUser = false,
                    text = "Selam ${state.player.firstName}! Instagram üzerinden mesajın için teşekkürler 👋⚽",
                    dayIndex = state.currentDayIndex,
                    timeLabel = state.formattedClockTime
                )
            )
        )
        extraDmThreads[clean] = newThread
        return newThread
    }

    fun getAllInstaDmThreads(state: CareerSaveState): List<InstaDmThread> {
        val existingHandles = state.instaDmThreads.map { it.senderHandle.removePrefix("@").lowercase() }.toSet()
        val extras = extraDmThreads.values.filter {
            it.senderHandle.removePrefix("@").lowercase() !in existingHandles
        }
        return state.instaDmThreads + extras
    }

    fun appendLocalDmMessageIfExtra(threadId: String, state: CareerSaveState, text: String): Boolean {
        val entry = extraDmThreads.entries.find { it.value.id == threadId } ?: return false
        val th = entry.value
        val userMsg = ChatMessage(
            id = "IDM_LOC_U_${System.currentTimeMillis()}",
            senderName = state.player.fullName,
            isFromUser = true,
            text = text,
            dayIndex = state.currentDayIndex,
            timeLabel = state.formattedClockTime
        )
        val replyMsg = ChatMessage(
            id = "IDM_LOC_R_${System.currentTimeMillis() + 1}",
            senderName = th.senderName,
            isFromUser = false,
            text = "Harikasın ${state.player.firstName}! Desteğin ve mesajın için çok teşekkürler, sahada başarılar! 🔥⚽",
            dayIndex = state.currentDayIndex,
            timeLabel = state.formattedClockTime,
            impactSummary = "Moral +2 • +60 Takipçi"
        )
        extraDmThreads[entry.key] = th.copy(messages = th.messages + userMsg + replyMsg)
        return true
    }

    // =========================================================================
    // 5. LIVING X (TWITTER) TIMELINE, QUOTES, REPLIES & TRENDS
    // =========================================================================

    fun getLivingXFeed(state: CareerSaveState): List<XPostItem> {
        val p = state.player
        val club = state.currentClub
        val day = state.currentDayIndex
        val rivalClub = state.clubs.firstOrNull { it.id != club.id && it.leagueId == club.leagueId }
            ?: state.clubs.lastOrNull() ?: club
        val leader = state.standings.firstOrNull()
        val bottomTeam = state.standings.lastOrNull()
        val topScorer = state.topScorers.firstOrNull()
        val playedFixtures = state.fixtures.filter { it.played }.sortedByDescending { it.dayIndex }
        val otherMatch = playedFixtures.firstOrNull { it.homeClubId != club.id && it.awayClubId != club.id }
        val nextMatch = state.nextUnplayedUserFixture

        val generatedTweets = mutableListOf<XPostItem>()

        // 1. Yağız Sabuncuoğlu — Breaking Transfer / Scout Tweet
        val yagizId = "XP_LIV_YAGIZ_D$day"
        generatedTweets.add(
            XPostItem(
                id = yagizId,
                dayIndex = day,
                dateLabel = "35d",
                authorName = "Yağız Sabuncuoğlu",
                handle = "@yagosabuncuoglu",
                category = "GAZETECİ",
                isVerified = true,
                text = "ÖZEL | Avrupa'dan iki kulübün Doğu Avrupa ve Türkiye bölge scoutu, ${club.name} forması giyen ${p.fullName} (#${p.kitNumber}) için canlı maç raporu hazırladı. Oyuncunun sezon sonuna kadar gelişimi yakından takip edilecek. 🔎🇹🇷",
                likes = 8_940,
                reposts = 910
            )
        )

        // 2. Uğur Karakullukçu — Quoting Yağız Sabuncuoğlu!
        val ugurQuoteId = "XP_LIV_UGUR_QUOTE_D$day"
        quotedPostEmbeds[ugurQuoteId] = XQuotedPostEmbed(
            originalPostId = yagizId,
            authorName = "Yağız Sabuncuoğlu",
            handle = "@yagosabuncuoglu",
            avatarHex = "#0EA5E9",
            isVerified = true,
            timeLabel = "35d",
            text = "ÖZEL | Avrupa'dan iki kulübün Doğu Avrupa ve Türkiye bölge scoutu, ${club.name} forması giyen ${p.fullName} için canlı maç raporu hazırladı."
        )
        generatedTweets.add(
            XPostItem(
                id = ugurQuoteId,
                dayIndex = day,
                dateLabel = "22d",
                authorName = "Uğur Karakullukçu",
                handle = "@ugurkarakullukcu",
                category = "YORUMCU",
                isVerified = true,
                text = "Şaşırtıcı değil. ${p.age} yaşında bu fizik kalite ve top tekniğiyle ${club.leagueName}'de fark yaratmaması imkansızdı. Tek eksiği maç içi süreklilik, onu da ${club.managerName} ile çözerse sezon sonu burada tutamazlar.",
                likes = 4_320,
                reposts = 380
            )
        )

        // 3. Mackolik — League Standings Update (World / League Context)
        if (leader != null && bottomTeam != null) {
            generatedTweets.add(
                XPostItem(
                    id = "XP_LIV_MACKOLIK_TABLE_D$day",
                    dayIndex = day,
                    dateLabel = "1s",
                    authorName = "Mackolik",
                    handle = "@mackolik",
                    category = "MEDYA",
                    isVerified = true,
                    text = "📊 ${club.leagueName}'de güncel zirve ve alt sıra hattı:\n\n1️⃣ ${leader.clubName} — ${leader.points} Puan (Av: ${leader.goalDifference})\n🔥 ${club.name} — ${state.standings.find { it.clubId == club.id }?.points ?: 0} Puan\n⚠️ ${bottomTeam.clubName} — ${bottomTeam.points} Puan\n\nŞampiyonluk yarışında favoriniz hangi takım? 👇⚽",
                    likes = 6_180,
                    reposts = 540
                )
            )
        }

        // 4. No Context Türk Futbolu — Referee / VAR / League Humor
        generatedTweets.add(
            XPostItem(
                id = "XP_LIV_NOCONTEXT_VAR",
                dayIndex = day,
                dateLabel = "1s",
                authorName = "No Context Türk Futbolu",
                handle = "@nocontextsuperlig",
                category = "MİZAH",
                isVerified = true,
                text = "VAR monitörüne çağrılan hakemin ekrana 4 saniye bakıp kendi kararını verdiği o anki özgüven seviyesine ihtiyacım var 😂🖥️⚽",
                likes = 19_450,
                reposts = 2_120
            )
        )

        // 5. Other League Match Score & Drama
        if (otherMatch != null) {
            val otherMatchTweetId = "XP_LIV_OTHER_${otherMatch.id}"
            generatedTweets.add(
                XPostItem(
                    id = otherMatchTweetId,
                    dayIndex = otherMatch.dayIndex,
                    dateLabel = "2s",
                    authorName = "beIN SPORTS Türkiye",
                    handle = "@beinsportstr",
                    category = "MEDYA",
                    isVerified = true,
                    text = "🏁 MAÇ SONUCU | ${otherMatch.homeClubName} ${otherMatch.homeScore}-${otherMatch.awayScore} ${otherMatch.awayClubName}\n\n${club.leagueName}'de haftanın en kritik maçlarından birinde heyecan son düdüğe kadar sürdü! 📺⚽",
                    likes = 3_410,
                    reposts = 290
                )
            )
        }

        // 6. Ertan Süzgün — League Transfer Rumor (Other clubs!)
        generatedTweets.add(
            XPostItem(
                id = "XP_LIV_ERTAN_LEAGUE",
                dayIndex = day,
                dateLabel = "2s",
                authorName = "Ertan Süzgün",
                handle = "@ertansuzgun",
                category = "GAZETECİ",
                isVerified = true,
                text = "🔥 ÖZEL | ${rivalClub.name}, hücum hattını güçlendirmek için listedeki 2 yabancı kanat oyuncusuyla temasa geçti. Teknik heyetin raporu doğrultusunda hafta içi sıcak saatler yaşanacak.",
                likes = 2_980,
                reposts = 315
            )
        )

        // 7. OptaCan — League / Player Analytical Stat
        generatedTweets.add(
            XPostItem(
                id = "XP_LIV_OPTACAN_STAT_D$day",
                dayIndex = day,
                dateLabel = "3s",
                authorName = "OptaCan",
                handle = "@OptaCan",
                category = "YORUMCU",
                isVerified = true,
                text = "${p.attributes.cardDribbling} - ${club.leagueName}'de bu sezon 21 yaş altı oyuncular arasında hücum üçüncü bölgesinde top taşıma ve şut pası üretme potansiyeli en yüksek isimlerden biri ${p.fullName} (${club.shortName}). Dinamik.",
                likes = 4_120,
                reposts = 485
            )
        )

        // 8. Krampon Sports — Fan Banter & Poll
        generatedTweets.add(
            XPostItem(
                id = "XP_LIV_KRAMPON_POLL",
                dayIndex = day,
                dateLabel = "3s",
                authorName = "Krampon Sports",
                handle = "@kramponsports",
                category = "MİZAH",
                isVerified = true,
                text = "Soru net: Maçın 90+3. dakikası, skor 1-1, kazanılan frikikte topun başına kim geçmeli? 🎯⚽\n\nA) Takımın 35 yaşındaki tecrübeli kaptanı\nB) Özgüveni tavan yapmış genç yıldız ${p.fullName} (#${p.kitNumber})",
                likes = 7_640,
                reposts = 620
            )
        )

        // 9. Arda Güler — Champions League / Real Madrid Tweet
        generatedTweets.add(
            XPostItem(
                id = "XP_LIV_ARDA_UCL",
                dayIndex = day,
                dateLabel = "4s",
                authorName = "Arda Güler",
                handle = "@ardaguler",
                category = "OYUNCU",
                isVerified = true,
                text = "Bernabéu geceleri her zaman özeldir. 🤍✨ Desteğiniz için teşekkürler! #HalaMadrid 🇹🇷",
                likes = 142_000,
                reposts = 18_400
            )
        )

        // 10. Sokrates Dergi — European & Turkish Football Essay
        generatedTweets.add(
            XPostItem(
                id = "XP_LIV_SOKRATES_ESSAY",
                dayIndex = day,
                dateLabel = "5s",
                authorName = "Sokrates Dergi",
                handle = "@sokratesdergi",
                category = "MEDYA",
                isVerified = true,
                text = "🎙️ Yeni bölüm yayında! Avrupa'da forma giyen milli yıldızlarımızın yükselişi, ${club.leagueName}'deki zirve yarışı ve altyapıdan çıkan yeni jenerasyonun şifrelerini konuştuk.",
                likes = 2_890,
                reposts = 240
            )
        )

        // 11. Next Match Preview by Club Official X Account
        if (nextMatch != null) {
            val clubFixtureTweetId = "XP_LIV_NEXT_FIX_${nextMatch.id}"
            generatedTweets.add(
                XPostItem(
                    id = clubFixtureTweetId,
                    dayIndex = day,
                    dateLabel = "5s",
                    authorName = "${club.name} SK",
                    handle = "@${club.shortName.lowercase()}resmi",
                    category = "KULÜP",
                    isVerified = true,
                    text = "🗓️ SIRADAKİ MAÇIMIZ | ${club.leagueName} ${nextMatch.week}. Hafta\n⚽ ${nextMatch.homeClubName} vs ${nextMatch.awayClubName}\n🏟️ ${nextMatch.stadiumName} • ⏰ ${nextMatch.kickOffTime}\n\n#Saldır${club.shortName} 💪🔥",
                    likes = 5_420,
                    reposts = 780
                )
            )

            // 12. Ultras Quoting Club Fixture Tweet!
            val ultrasQuoteId = "XP_LIV_ULTRAS_QUOTE_${nextMatch.id}"
            quotedPostEmbeds[ultrasQuoteId] = XQuotedPostEmbed(
                originalPostId = clubFixtureTweetId,
                authorName = "${club.name} SK",
                handle = "@${club.shortName.lowercase()}resmi",
                avatarHex = club.primaryColorHex,
                isVerified = true,
                timeLabel = "5s",
                text = "🗓️ SIRADAKİ MAÇIMIZ | ${nextMatch.homeClubName} vs ${nextMatch.awayClubName} • 🏟️ ${nextMatch.stadiumName}"
            )
            generatedTweets.add(
                XPostItem(
                    id = ultrasQuoteId,
                    dayIndex = day,
                    dateLabel = "4s",
                    authorName = "${club.shortName} Tribün & Ultras",
                    handle = "@${club.shortName.lowercase()}_ultras",
                    category = "TARAFTAR",
                    isVerified = false,
                    text = "TÜM TARAFTARIMIZA ÇAĞRIDIR! 🧣🔥 Maç günü stadyum etrafında saat 17:00'de toplanıyoruz. 90 dakika boyunca susmak yok, o 3 puan buraya gelecek! @${p.firstName.lowercase()}${p.kitNumber} golünü bekliyoruz koçum!",
                    likes = 1_940,
                    reposts = 340
                )
            )
        }

        // 13. Rival Fan Account complaining / banter
        generatedTweets.add(
            XPostItem(
                id = "XP_LIV_RIVAL_FAN_BANTER",
                dayIndex = day,
                dateLabel = "6s",
                authorName = "${rivalClub.shortName} Cephesi",
                handle = "@${rivalClub.shortName.lowercase()}_cephesi",
                category = "TARAFTAR",
                isVerified = false,
                text = "Medya yine ${club.name}'un genç oyuncusu ${p.fullName}'i abartmaya başlamış. İki maç iyi oynadı diye hemen Avrupa'ya gönderdiler, bizim altyapıdaki çocukların neyi eksik?! 🙄",
                likes = 640,
                reposts = 58
            )
        )

        // 14. Transfermarkt TR X Tweet
        generatedTweets.add(
            XPostItem(
                id = "XP_LIV_TM_TR_VAL",
                dayIndex = day,
                dateLabel = "7s",
                authorName = "Transfermarkt Türkiye",
                handle = "@transfermarkt_tr",
                category = "YORUMCU",
                isVerified = true,
                text = "📈 ${club.leagueName} Piyasa Değeri Raporu:\n• ${p.fullName} (${club.shortName}): ${FLFormatters.formatMarketValue(p.marketValueEur)}\n• Gol Krallığı Lideri: ${topScorer?.playerName ?: p.fullName} (${topScorer?.goals ?: 0} Gol)\n\nAra transfer dönemi yaklaşırken gözlemci raporlarında en çok aranan profil: Hızlı ve teknik hücumcular.",
                likes = 3_750,
                reposts = 410
            )
        )

        // Merge prependedXPosts + userPublishedXPosts + state.xPosts + generatedTweets + appendedXPosts
        val baseIds = (userPublishedXPosts.map { it.id } + state.xPosts.map { it.id }).toSet()
        val combined = prependedXPosts + userPublishedXPosts + state.xPosts + generatedTweets.filter { it.id !in baseIds } + appendedXPosts

        return combined.map { xp ->
            val likedOv = extraXLikes[xp.id]
            val rpOv = extraXReposts[xp.id]
            val finalLiked = likedOv ?: xp.likedByUser
            val finalRp = rpOv ?: xp.repostedByUser
            val likeDelta = when {
                likedOv == true && !xp.likedByUser -> 1
                likedOv == false && xp.likedByUser -> -1
                else -> 0
            }
            val rpDelta = when {
                rpOv == true && !xp.repostedByUser -> 1
                rpOv == false && xp.repostedByUser -> -1
                else -> 0
            }
            xp.copy(
                likedByUser = finalLiked,
                repostedByUser = finalRp,
                likes = (xp.likes + likeDelta).coerceAtLeast(0),
                reposts = (xp.reposts + rpDelta).coerceAtLeast(0)
            )
        }
    }

    fun getQuotedEmbedForPost(postId: String): XQuotedPostEmbed? = quotedPostEmbeds[postId]

    fun isXPostBookmarked(postId: String): Boolean = extraXBookmarks[postId] == true

    fun toggleXPostBookmark(postId: String) {
        extraXBookmarks[postId] = !(extraXBookmarks[postId] ?: false)
    }

    fun toggleXPostLikeLocal(postId: String, currentLiked: Boolean) {
        extraXLikes[postId] = !currentLiked
    }

    fun toggleXPostRepostLocal(postId: String, currentReposted: Boolean) {
        extraXReposts[postId] = !currentReposted
    }

    fun getXRepliesForPost(post: XPostItem, state: CareerSaveState): List<XReplyItem> {
        val p = state.player
        val club = state.currentClub
        val rivalClub = state.clubs.firstOrNull { it.id != club.id } ?: club
        val userAdded = extraXReplies[post.id].orEmpty()

        val defaultReplies = when {
            post.handle.lowercase().contains("yagosabuncuoglu") || post.handle.lowercase().contains("fabrizio") -> listOf(
                XReplyItem(
                    id = "XREP_${post.id}_1",
                    parentPostId = post.id,
                    authorName = "${club.shortName} Tribün & Ultras",
                    handle = "@${club.shortName.lowercase()}_ultras",
                    avatarHex = club.primaryColorHex,
                    isVerified = false,
                    timeLabel = "20d",
                    text = "Sakın ucuza bırakmayın yönetimi uyarın! En az 10 Milyon € + sonraki satıştan %20 pay! 💎🔥",
                    likes = 342,
                    reposts = 18
                ),
                XReplyItem(
                    id = "XREP_${post.id}_2",
                    parentPostId = post.id,
                    authorName = "Krampon Sports",
                    handle = "@kramponsports",
                    avatarHex = "#22C55E",
                    isVerified = true,
                    timeLabel = "15d",
                    text = "Sabah akşam tesislerin önünde kamp kuran Avrupa scoutları temsili 👀🔭",
                    likes = 189,
                    reposts = 9
                ),
                XReplyItem(
                    id = "XREP_${post.id}_3",
                    parentPostId = post.id,
                    authorName = "OptaCan",
                    handle = "@OptaCan",
                    avatarHex = "#6366F1",
                    isVerified = true,
                    badgeType = VerifiedBadgeType.GOLD_ORG,
                    timeLabel = "12d",
                    text = "Potansiyel (${p.potential}) ve yaş (${p.age}) eğrisi Avrupa'nın 5 büyük ligi için ideal profilde. Mercek.",
                    likes = 145,
                    reposts = 11
                )
            )
            post.category == "MİZAH" -> listOf(
                XReplyItem(
                    id = "XREP_${post.id}_M1",
                    parentPostId = post.id,
                    authorName = "Futbol Mizah TR",
                    handle = "@futbolmizahtr",
                    avatarHex = "#F97316",
                    isVerified = false,
                    timeLabel = "30d",
                    text = "Her hafta sonu aynı senaryoyu yaşayıp yine de televizyon karşısına geçmemiz şaka mı 😂",
                    likes = 512,
                    reposts = 24
                ),
                XReplyItem(
                    id = "XREP_${post.id}_M2",
                    parentPostId = post.id,
                    authorName = "${club.shortName} Ruhu",
                    handle = "@${club.shortName.lowercase()}_ruhu",
                    avatarHex = club.primaryColorHex,
                    isVerified = false,
                    timeLabel = "18d",
                    text = "B şıkkı diyenler beğensin sayımızı bilelim: Tabii ki ${p.fullName} (#${p.kitNumber}) vurur! 🎯🔥",
                    likes = 276,
                    reposts = 14
                )
            )
            post.handle.lowercase().contains(rivalClub.shortName.lowercase()) -> listOf(
                XReplyItem(
                    id = "XREP_${post.id}_R1",
                    parentPostId = post.id,
                    authorName = "${club.shortName} Tribün & Ultras",
                    handle = "@${club.shortName.lowercase()}_ultras",
                    avatarHex = club.primaryColorHex,
                    isVerified = false,
                    timeLabel = "40d",
                    text = "Ağlama duvarı yan tarafta kardeşim, ${p.fullName} size gol atınca da böyle tweet atacak mısın bakalım? 😂🤫",
                    likes = 820,
                    reposts = 64
                ),
                XReplyItem(
                    id = "XREP_${post.id}_R2",
                    parentPostId = post.id,
                    authorName = "No Context Türk Futbolu",
                    handle = "@nocontextsuperlig",
                    avatarHex = "#EAB308",
                    isVerified = true,
                    timeLabel = "25d",
                    text = "Ratio + puan tablosuna tekrar bak 😂📊",
                    likes = 1_190,
                    reposts = 95
                )
            )
            else -> listOf(
                XReplyItem(
                    id = "XREP_${post.id}_G1",
                    parentPostId = post.id,
                    authorName = "Sports Digitale",
                    handle = "@sportsdigitale",
                    avatarHex = "#10B981",
                    isVerified = true,
                    badgeType = VerifiedBadgeType.GOLD_ORG,
                    timeLabel = "28d",
                    text = "Detayları akşam canlı yayınımızda masaya yatırıyoruz! 🎙️⚽",
                    likes = 94,
                    reposts = 6
                ),
                XReplyItem(
                    id = "XREP_${post.id}_G2",
                    parentPostId = post.id,
                    authorName = "${club.shortName} Sevdalısı",
                    handle = "@${club.shortName.lowercase()}_taraftar",
                    avatarHex = club.primaryColorHex,
                    isVerified = false,
                    timeLabel = "14d",
                    text = "Bu sezon o kupa ${club.stadiumName}'a gelecek inşallah! 🔥👏",
                    likes = 68,
                    reposts = 4
                )
            )
        }

        return (userAdded + defaultReplies).map { rep ->
            val liked = extraReplyXLikes[rep.id] ?: rep.likedByUser
            val delta = when {
                extraReplyXLikes[rep.id] == true && !rep.likedByUser -> 1
                extraReplyXLikes[rep.id] == false && rep.likedByUser -> -1
                else -> 0
            }
            val baseLikes = dynamicCommentLikeCounts[rep.id] ?: rep.likes
            rep.copy(likedByUser = liked, likes = (baseLikes + delta).coerceAtLeast(0))
        }
    }

    fun addXReply(state: CareerSaveState, parentPost: XPostItem, replyText: String) {
        val clean = replyText.trim()
        if (clean.isEmpty()) return
        val p = state.player
        val club = state.currentClub
        val rivalClub = state.clubs.firstOrNull { it.id != club.id } ?: club
        val captain = club.keySquadMembers.find { it.isCaptain } ?: club.keySquadMembers.firstOrNull()
        val userHandle = "@${p.firstName.lowercase()}${p.kitNumber}"
        val initialPlayerLikes = calculatePlayerCommentLikes(state)
        val initialPlayerReposts = (initialPlayerLikes / 8).coerceAtLeast(1)

        val userReplyId = "XREP_USR_${System.currentTimeMillis()}"
        dynamicCommentLikeCounts[userReplyId] = initialPlayerLikes

        val userReply = XReplyItem(
            id = userReplyId,
            parentPostId = parentPost.id,
            authorName = p.fullName,
            handle = userHandle,
            avatarHex = club.primaryColorHex,
            isVerified = true,
            badgeType = VerifiedBadgeType.BLUE,
            timeLabel = "Şimdi",
            text = clean,
            likes = initialPlayerLikes,
            reposts = initialPlayerReposts
        )

        // Generate 4 varied, realistic replies reacting directly to player's tweet
        val fanReplies = listOf(
            "$userHandle Kral sonuna kadar haklısın! Bu ligde senin vizyonunu anlayacak futbol aklı çok az 👏🔥",
            "$userHandle Sahadaki mücadelenle herkese cevap vereceksin aslanım, arkandayız!",
            "$userHandle Formanın hakkını veren tek adamsın, şampiyonluk seninle gelecek 🦁⚽",
            "$userHandle İşte görmek istediğimiz aidiyet ve karakter! Helal olsun!"
        )
        val fanReply = XReplyItem(
            id = "XREP_FAN_${System.currentTimeMillis() + 1}",
            parentPostId = parentPost.id,
            authorName = "${club.shortName} Ultras",
            handle = "@${club.shortName.lowercase()}_ultras",
            avatarHex = club.primaryColorHex,
            isVerified = false,
            reactionType = CommentReactionType.FAN_SUPPORT,
            timeLabel = "Şimdi",
            text = fanReplies.random(),
            likes = (initialPlayerLikes * 0.45).toInt().coerceAtLeast(3),
            reposts = (initialPlayerReposts * 0.4).toInt().coerceAtLeast(1)
        )

        val trollReplies = listOf(
            "$userHandle Sahada 90 dakika yürü, sonra Twitter'da klavye şov yap. Önce maçı kazan da konuşalım birader 😂",
            "$userHandle Bizim stadyuma gelince kaleyi tutturamazsın ama burada şairsin haha",
            "$userHandle Balon patladı! 3 haftadır golün asistin yok boş yapma 🤣",
            "$userHandle Halı sahada bile yedek kulübesinde üşürsün gelmiş laf yetiştiriyor"
        )
        val trollReply = XReplyItem(
            id = "XREP_TRL_${System.currentTimeMillis() + 2}",
            parentPostId = parentPost.id,
            authorName = "Tribün Şamatası",
            handle = "@tribun_samata",
            avatarHex = rivalClub.primaryColorHex,
            isVerified = false,
            reactionType = CommentReactionType.RIVAL_TROLL,
            timeLabel = "Şimdi",
            text = trollReplies.random(),
            likes = (initialPlayerLikes * 0.22).toInt().coerceAtLeast(2),
            reposts = (initialPlayerReposts * 0.2).toInt().coerceAtLeast(0)
        )

        val memeReplies = listOf(
            "$userHandle Tweeti silmeden önce ekran görüntüsü aldım bile sksksk arkada Müslüm Baba çalıyor 🍿🔥",
            "$userHandle Hocan bunu görürse idmanda 20 tur koşturur seni, yerimizi alalım rez 😂",
            "$userHandle Kramponları çıkarıp direkt klavyeye geçmiş kral saygılar 😂🔥",
            "$userHandle Admin bunu görünce hesabın şifresini değiştirdi kesin sksksk"
        )
        val memeReply = XReplyItem(
            id = "XREP_MEME_${System.currentTimeMillis() + 3}",
            parentPostId = parentPost.id,
            authorName = "No Context Türk Futbolu",
            handle = "@nocontextsuperlig",
            avatarHex = "#EAB308",
            isVerified = true,
            reactionType = CommentReactionType.MEME_CULTURE,
            timeLabel = "Şimdi",
            text = memeReplies.random(),
            likes = (initialPlayerLikes * 0.35).toInt().coerceAtLeast(2),
            reposts = (initialPlayerReposts * 0.3).toInt().coerceAtLeast(1)
        )

        val peerReply = XReplyItem(
            id = "XREP_PEER_${System.currentTimeMillis() + 4}",
            parentPostId = parentPost.id,
            authorName = captain?.name ?: "Yağız Sabuncuoğlu",
            handle = if (captain != null) "@${captain.name.lowercase().replace(" ", "_")}" else "@yagosabuncuoglu",
            avatarHex = captain?.let { club.primaryColorHex } ?: "#0EA5E9",
            isVerified = true,
            badgeType = VerifiedBadgeType.BLUE,
            reactionType = if (captain != null) CommentReactionType.TEAMMATE else CommentReactionType.PRESS_CRITIC,
            timeLabel = "Şimdi",
            text = if (captain != null) "$userHandle Kardeşim yarın sabah taktik idmanı var, yorumlarda mesaiyi bırak dinlen haha 💪⚽"
                   else "$userHandle Bu yanıt Süper Lig kulislerinde ve spor bültenlerinde flaş haber olarak geçiyor 🔎🇹🇷",
            likes = (initialPlayerLikes * 0.60).toInt().coerceAtLeast(8),
            reposts = (initialPlayerReposts * 0.5).toInt().coerceAtLeast(2)
        )

        val current = extraXReplies[parentPost.id].orEmpty()
        extraXReplies[parentPost.id] = listOf(userReply, fanReply, trollReply, memeReply, peerReply) + current

        // Add notifications
        extraNotifications.add(
            0,
            SocialNotificationItem(
                id = "NOTIF_X_REP_${System.currentTimeMillis()}",
                platform = "X",
                type = "REPLY",
                actorName = "${club.shortName} Ultras",
                actorHandle = "@${club.shortName.lowercase()}_ultras",
                actorAvatarHex = club.primaryColorHex,
                actorVerified = false,
                text = "tweetinize yanıt verdi: 'Kral sonuna kadar haklısın!'",
                timeLabel = "Şimdi",
                targetPostId = parentPost.id,
                dayIndex = state.currentDayIndex
            )
        )
        extraNotifications.add(
            0,
            SocialNotificationItem(
                id = "NOTIF_X_ENG_${System.currentTimeMillis() + 1}",
                platform = "X",
                type = "REPOST",
                actorName = "Gündem Bildirimi",
                actorHandle = "@trendler",
                actorAvatarHex = "#1D9BF0",
                actorVerified = true,
                text = "yanıtınız ${FLFormatters.formatCompactNumber(initialPlayerLikes.toLong())} beğeni ve ${initialPlayerReposts} retweet ile gündeme girdi! ⚡",
                timeLabel = "Şimdi",
                targetPostId = parentPost.id,
                dayIndex = state.currentDayIndex
            )
        )
    }

    fun toggleXReplyLikeLocal(replyId: String, currentLiked: Boolean) {
        extraReplyXLikes[replyId] = !currentLiked
    }

    fun publishQuoteXPost(
        state: CareerSaveState,
        quotedPost: XPostItem,
        commentary: String,
        onDelegatePublish: (String) -> Unit
    ) {
        val clean = commentary.trim().ifEmpty { "🔥⚽" }
        val p = state.player
        val (scaledLikes, scaledReposts) = calculatePlayerPostEngagement(state)
        val newId = "XP_QUOTE_USR_${System.currentTimeMillis()}"
        quotedPostEmbeds[newId] = XQuotedPostEmbed(
            originalPostId = quotedPost.id,
            authorName = quotedPost.authorName,
            handle = quotedPost.handle,
            avatarHex = resolveAccount(quotedPost.handle, state).avatarHex,
            isVerified = quotedPost.isVerified,
            timeLabel = quotedPost.dateLabel,
            text = quotedPost.text
        )
        val newPost = XPostItem(
            id = newId,
            dayIndex = state.currentDayIndex,
            dateLabel = "Şimdi",
            authorName = p.fullName,
            handle = "@${p.firstName.lowercase()}${p.kitNumber}",
            category = "OYUNCU",
            isVerified = true,
            text = clean,
            likes = scaledLikes,
            reposts = scaledReposts
        )
        userPublishedXPosts.add(0, newPost)
        onDelegatePublish("$clean [Alıntı: ${quotedPost.handle}]")
    }

    fun getXTrendingTopics(state: CareerSaveState): List<XTrendTopic> {
        val p = state.player
        val club = state.currentClub
        val rivalClub = state.clubs.firstOrNull { it.id != club.id && it.leagueId == club.leagueId }
            ?: state.clubs.lastOrNull() ?: club
        val topScorer = state.topScorers.firstOrNull()

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
                liveContextSubtitle = "Avrupa scoutları ve @yagosabuncuoglu özel haberi sonrası gündemde",
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

    // =========================================================================
    // 6. NOTIFICATIONS (INSTAGRAM & X)
    // =========================================================================

    fun getNotifications(state: CareerSaveState, platform: String): List<SocialNotificationItem> {
        val p = state.player
        val club = state.currentClub
        val day = state.currentDayIndex
        val userHandle = "@${p.firstName.lowercase()}${p.kitNumber}"

        val baseList = if (platform == "INSTAGRAM") {
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
                    targetPostId = "SOC_FABRIZIO_1",
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
                    targetPostId = "SOC_433_1",
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
                    targetPostId = "LIV_IG_TM_UPDATE_D$day",
                    dayIndex = day
                ),
                SocialNotificationItem(
                    id = "NOTIF_IG_6",
                    platform = "INSTAGRAM",
                    type = "LIKE",
                    actorName = "${club.shortName} Tribün & Ultras",
                    actorHandle = "@${club.shortName.lowercase()}_ultras",
                    actorAvatarHex = club.primaryColorHex,
                    actorVerified = false,
                    text = "ve 1.420 diğer kişi fotoğrafını beğendi.",
                    timeLabel = "5s",
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
                    targetPostId = "XP_LIV_YAGIZ_D$day",
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
                    targetPostId = "XP_LIV_UGUR_QUOTE_D$day",
                    dayIndex = day
                ),
                SocialNotificationItem(
                    id = "NOTIF_X_3",
                    platform = "X",
                    type = "MENTION",
                    actorName = "OptaCan",
                    actorHandle = "@OptaCan",
                    actorAvatarHex = "#6366F1",
                    actorVerified = true,
                    text = "istatistik analizinde senden bahsetti: \"Hücum üçüncü bölgesinde en yüksek verimlilik: ${p.fullName}. Dinamik.\"",
                    timeLabel = "3s",
                    targetPostId = "XP_LIV_OPTACAN_STAT_D$day",
                    dayIndex = day
                ),
                SocialNotificationItem(
                    id = "NOTIF_X_4",
                    platform = "X",
                    type = "REPOST",
                    actorName = "Krampon Sports",
                    actorHandle = "@kramponsports",
                    actorAvatarHex = "#22C55E",
                    actorVerified = true,
                    text = "anketinde seni aday gösterdi: \"90+3'te frikikte topun başına kim geçmeli? 🎯\"",
                    timeLabel = "3s",
                    targetPostId = "XP_LIV_KRAMPON_POLL",
                    dayIndex = day
                ),
                SocialNotificationItem(
                    id = "NOTIF_X_5",
                    platform = "X",
                    type = "FOLLOW",
                    actorName = "Sokrates Dergi",
                    actorHandle = "@sokratesdergi",
                    actorAvatarHex = "#EC4899",
                    actorVerified = true,
                    text = "seni takip etmeye başladı.",
                    timeLabel = "5s",
                    dayIndex = day
                )
            )
        }
        val extras = extraNotifications.filter { it.platform.equals(platform, ignoreCase = true) }
        return extras + baseList
    }

    // =========================================================================
    // 6.5. DYNAMIC INFINITE SCROLL & PULL-TO-REFRESH FEED ENGINE
    // =========================================================================

    fun loadMoreInstaPosts(state: CareerSaveState): Int {
        currentInstaPage.value++
        val page = currentInstaPage.value
        val newPosts = generateDynamicInstaBatch(state, page)
        appendedInstaPosts.addAll(newPosts)
        return newPosts.size
    }

    fun refreshInstaFeed(state: CareerSaveState): Int {
        val freshBatch = generateBreakingInstaBatch(state)
        prependedInstaPosts.addAll(0, freshBatch)
        return freshBatch.size
    }

    fun loadMoreXPosts(state: CareerSaveState): Int {
        currentXPage.value++
        val page = currentXPage.value
        val newTweets = generateDynamicXBatch(state, page)
        appendedXPosts.addAll(newTweets)
        return newTweets.size
    }

    fun refreshXFeed(state: CareerSaveState): Int {
        val freshTweets = generateBreakingXBatch(state)
        prependedXPosts.addAll(0, freshTweets)
        return freshTweets.size
    }

    private fun generateDynamicInstaBatch(state: CareerSaveState, page: Int): List<SocialMediaPost> {
        val p = state.player
        val club = state.currentClub
        val day = state.currentDayIndex
        val rivalClub = state.clubs.firstOrNull { it.id != club.id } ?: club
        val fameTier = calculatePlayerFameTier(state)
        val posts = mutableListOf<SocialMediaPost>()

        when (page % 4) {
            1 -> {
                // 1. European Champions League Clash (433)
                posts.add(
                    SocialMediaPost(
                        id = "DYN_IG_UCL_P${page}_1",
                        dayIndex = day,
                        dateLabel = "2 saat önce",
                        authorName = "433",
                        authorHandle = "@433",
                        isUserPost = false,
                        content = "WHAT A NIGHT OF CHAMPIONS LEAGUE FOOTBALL! 🤯⚽ Real Madrid 3-2 Manchester City! Vinicius Jr 88. dakikada maçı koparan golü attı. Bernabéu yine büyülü bir geceye tanıklık etti! Sizce bu sezon kupayı kim kaldırır? 🏆✨",
                        likesCount = 1_240_000,
                        commentsCount = 18_450,
                        sentiment = "Destekleyici",
                        instaComments = listOf(
                            InstaCommentItem(id = "C_UCL_1", authorHandle = "ardaguler", authorName = "Arda Güler", isVerified = true, avatarHex = "#F59E0B", text = "¡Hasta el final! 🤍✨", timeLabel = "1s", likesCount = 45_200),
                            InstaCommentItem(id = "C_UCL_2", authorHandle = "kramponsports", authorName = "Krampon Sports", isVerified = true, avatarHex = "#22C55E", text = "Futbolun zirvesi gerçekten bambaşka bir seviye...", timeLabel = "50d", likesCount = 3_120)
                        ),
                        isVerifiedAuthor = true,
                        locationLabel = "Santiago Bernabéu, Madrid",
                        imageSceneType = "STADIUM"
                    )
                )

                // 2. Turkish League Banter & Controversy (beIN SPORTS)
                posts.add(
                    SocialMediaPost(
                        id = "DYN_IG_VAR_P${page}_2",
                        dayIndex = day,
                        dateLabel = "4 saat önce",
                        authorName = "beIN SPORTS Türkiye",
                        authorHandle = "@beinsportstr",
                        isUserPost = false,
                        content = "📺 TRIO | ${club.leagueName}'de 88. dakikadaki penaltı kararı doğru muydu? Eski hakemler Deniz Çoban, Bülent Yıldırım ve Bahattin Duran canlı yayında değerlendirdi: 'Pozisyonda temas yetersiz, VAR müdahalesi hatalı!' 🎙️⚽",
                        likesCount = 48_900,
                        commentsCount = 2_890,
                        sentiment = "Eleştirel",
                        instaComments = listOf(
                            InstaCommentItem(id = "C_VAR_1", authorHandle = "${club.shortName.lowercase()}_ultras", authorName = "${club.shortName} Tribün", isVerified = false, avatarHex = club.primaryColorHex, text = "Her hafta aynı senaryo, emeğimize yazık!", timeLabel = "3s", likesCount = 412),
                            InstaCommentItem(id = "C_VAR_2", authorHandle = "nocontextsuperlig", authorName = "No Context Türk Futbolu", isVerified = true, avatarHex = "#EAB308", text = "VAR monitörünü izlerken kahve içen hakem efsanesi sksksk", timeLabel = "2s", likesCount = 1_840)
                        ),
                        isVerifiedAuthor = true,
                        locationLabel = "beIN SPORTS Stüdyoları, İstanbul",
                        imageSceneType = "LOCKER"
                    )
                )

                // 3. Coach Press Quote (Sports Digitale)
                posts.add(
                    SocialMediaPost(
                        id = "DYN_IG_COACH_P${page}_3",
                        dayIndex = day,
                        dateLabel = "6 saat önce",
                        authorName = "Sports Digitale",
                        authorHandle = "@sportsdigitale",
                        isUserPost = false,
                        content = "🗣️ Jose Mourinho basın toplantısında konuştu:\n\n\"Eğer hakem ve taktik kararları hakkında gerçek hislerimi söylersem federasyon bana 6 maç ceza verir. O yüzden sadece rakibi tebrik ediyorum ve susuyorum.\" 🤫🔥",
                        likesCount = 92_400,
                        commentsCount = 4_100,
                        sentiment = "Tarafsız",
                        instaComments = listOf(
                            InstaCommentItem(id = "C_MOU_1", authorHandle = "optacan", authorName = "OptaCan", isVerified = true, avatarHex = "#6366F1", text = "Zihin oyunları. Klasik.", timeLabel = "5s", likesCount = 890)
                        ),
                        isVerifiedAuthor = true,
                        locationLabel = "Basın Odası",
                        imageSceneType = "TRAINING"
                    )
                )

                // 4. Player Fame Recognition / Tagging (Mackolik)
                val mackolikCaption = if (fameTier.ordinal >= PlayerFameTier.RISING_STAR.ordinal) {
                    "🌟 HAFTANIN PARLAYAN YILDIZI | ${p.fullName} (@${p.firstName.lowercase()}${p.kitNumber})\n\n${club.name} formasıyla bu sezon ${club.leagueName}'de çıktığı maçlarda hem taraftarın sevgilisi oldu hem de scout raporlarında zirveye yerleşti! Geleceğin A Milli Takım 10 numarası mı? 👇🇹🇷"
                } else {
                    "⚽ ALT LİGLERİN GİZLİ CEVHERLERİ | TFF liglerinde altyapıdan yetişip parlayan genç yeteneklerin mücadelesi devam ediyor! Sizin favori genç oyuncunuz kim? 👇"
                }
                posts.add(
                    SocialMediaPost(
                        id = "DYN_IG_MACK_P${page}_4",
                        dayIndex = day,
                        dateLabel = "7 saat önce",
                        authorName = "Mackolik",
                        authorHandle = "@mackolik",
                        isUserPost = false,
                        content = mackolikCaption,
                        likesCount = if (fameTier.ordinal >= PlayerFameTier.RISING_STAR.ordinal) 38_200 else 14_500,
                        commentsCount = 940,
                        sentiment = "Destekleyici",
                        instaComments = listOf(
                            InstaCommentItem(id = "C_MACK_1", authorHandle = "${club.shortName.lowercase()}_fanatik", authorName = "${club.shortName} Fanatik", isVerified = false, avatarHex = club.primaryColorHex, text = "Kralımıza sahip çıkalım, geleceğimiz bu çocuk! 👏🔥", timeLabel = "6s", likesCount = 280)
                        ),
                        isVerifiedAuthor = true,
                        locationLabel = club.city,
                        imageSceneType = "STADIUM"
                    )
                )
            }
            2 -> {
                // 1. Premier League Derby Action (beIN SPORTS)
                posts.add(
                    SocialMediaPost(
                        id = "DYN_IG_EPL_P${page}_1",
                        dayIndex = day,
                        dateLabel = "1 saat önce",
                        authorName = "beIN SPORTS Türkiye",
                        authorHandle = "@beinsportstr",
                        isUserPost = false,
                        content = "🔥 PREMIER LEAGUE | Arsenal 2-2 Manchester City! Son saniye kornerinde gelen eşitlik golü Emirates'i yıktı geçti. Şampiyonluk yarışında puan farkı 1'e indi! 🏴󠁧󠁢󠁥󠁮󠁧󠁿⚽",
                        likesCount = 67_200,
                        commentsCount = 3_120,
                        sentiment = "Tarafsız",
                        instaComments = listOf(
                            InstaCommentItem(id = "C_EPL_1", authorHandle = "optacan", authorName = "OptaCan", isVerified = true, avatarHex = "#6366F1", text = "Premier League tarihinin en yüksek xG üretilen 5 maçından biri. Tempo.", timeLabel = "40d", likesCount = 420)
                        ),
                        isVerifiedAuthor = true,
                        locationLabel = "Emirates Stadium, Londra",
                        imageSceneType = "STADIUM"
                    )
                )

                // 2. Transfer Rumor (Yağız Sabuncuoğlu)
                posts.add(
                    SocialMediaPost(
                        id = "DYN_IG_YAGIZ_P${page}_2",
                        dayIndex = day,
                        dateLabel = "3 saat önce",
                        authorName = "Yağız Sabuncuoğlu",
                        authorHandle = "@yagosabuncuoglu",
                        isUserPost = false,
                        content = "🚨 ÖZEL | Süper Lig devinden sürpriz transfer harekatı! Yurt dışından scout edilen genç Türk futbolcu için kulüpler prensip anlaşmasına vardı. Oyuncunun menajeri bu gece sözleşme detayları için İstanbul'a geliyor. ✈️🇹🇷",
                        likesCount = 84_500,
                        commentsCount = 4_200,
                        sentiment = "Destekleyici",
                        instaComments = listOf(
                            InstaCommentItem(id = "C_YAG_1", authorHandle = "ertansuzgun", authorName = "Ertan Süzgün", isVerified = true, avatarHex = "#F59E0B", text = "Hayırlı olsun, doğru proje 👏", timeLabel = "2s", likesCount = 1_410)
                        ),
                        isVerifiedAuthor = true,
                        locationLabel = "İstanbul",
                        imageSceneType = "TRANSFER"
                    )
                )

                // 3. Turkish Football Meme (No Context Süper Lig)
                posts.add(
                    SocialMediaPost(
                        id = "DYN_IG_MEME_P${page}_3",
                        dayIndex = day,
                        dateLabel = "5 saat önce",
                        authorName = "No Context Türk Futbolu",
                        authorHandle = "@nocontextsuperlig",
                        isUserPost = false,
                        content = "Halı sahada 7-1 geriye düşünce 'Beyler hadi basit oynayalım, ayağa pas' diyen takım kaptanı temsili 😂⚽🏃‍♂️",
                        likesCount = 112_000,
                        commentsCount = 2_450,
                        sentiment = "Tarafsız",
                        instaComments = listOf(
                            InstaCommentItem(id = "C_MEM_1", authorHandle = "futbolmizahtr", authorName = "Futbol Mizah TR", isVerified = false, avatarHex = "#F97316", text = "Sonra ilk topu taca atan yine aynı kaptan sksksk", timeLabel = "4s", likesCount = 3_210)
                        ),
                        isVerifiedAuthor = true,
                        locationLabel = "Halı Saha Tesisleri",
                        imageSceneType = "TRAINING"
                    )
                )

                // 4. Player Lifestyle / Boots Drop (Nike Türkiye)
                posts.add(
                    SocialMediaPost(
                        id = "DYN_IG_NIKE_P${page}_4",
                        dayIndex = day,
                        dateLabel = "8 saat önce",
                        authorName = "Nike Football Türkiye",
                        authorHandle = "@niketurkiye",
                        isUserPost = false,
                        content = "Sahada fark yaratanlar sıradan kurallara uymaz. Yeni Nike Phantom GX2 Elite ile topu hisset, oyunu yönet. ⚡👟 Türk futbolunun yeni jenerasyonu sahneye çıkıyor. ✔️⚽",
                        likesCount = 74_200,
                        commentsCount = 1_180,
                        sentiment = "Destekleyici",
                        instaComments = listOf(
                            InstaCommentItem(id = "C_NK_1", authorHandle = "semihkilicsoy", authorName = "Semih Kılıçsoy", isVerified = true, avatarHex = "#27272A", text = "⚡🔥", timeLabel = "7s", likesCount = 890)
                        ),
                        isVerifiedAuthor = true,
                        locationLabel = "İstanbul / Riva Tesisleri",
                        imageSceneType = "TRAINING"
                    )
                )
            }
            3 -> {
                // 1. El Clasico Hype (433)
                posts.add(
                    SocialMediaPost(
                        id = "DYN_IG_CLASICO_P${page}_1",
                        dayIndex = day,
                        dateLabel = "2 saat önce",
                        authorName = "433",
                        authorHandle = "@433",
                        isUserPost = false,
                        content = "EL CLÁSICO WEEK IS HERE! 🇪🇸🔥 Real Madrid vs Barcelona at Spotify Camp Nou. The greatest rivalry in club football returns. Drop your score predictions below! 👇⚽",
                        likesCount = 1_650_000,
                        commentsCount = 32_100,
                        sentiment = "Destekleyici",
                        instaComments = listOf(
                            InstaCommentItem(id = "C_CLAS_1", authorHandle = "ardaguler", authorName = "Arda Güler", isVerified = true, avatarHex = "#F59E0B", text = "Ready. 🤍", timeLabel = "1s", likesCount = 52_400)
                        ),
                        isVerifiedAuthor = true,
                        locationLabel = "Camp Nou, Barcelona",
                        imageSceneType = "STADIUM"
                    )
                )

                // 2. Sergen Yalçın Legendary Quote (Sokrates Dergi)
                posts.add(
                    SocialMediaPost(
                        id = "DYN_IG_SERGEN_P${page}_2",
                        dayIndex = day,
                        dateLabel = "4 saat önce",
                        authorName = "Sokrates Dergi",
                        authorHandle = "@sokratesdergi",
                        isUserPost = false,
                        content = "🎙️ Sergen Yalçın'dan unutulmaz sözler:\n\n\"Bizim zamanımızda 35 metreden serbest vuruş olunca kaleci baraj kurmazdı, nereye atacağımı tahmin etmeye çalışırdı. Şimdi bakıyorum adamlar 5 metre ötedeki arkadaşına pas veremiyor.\" 😂⚽",
                        likesCount = 89_200,
                        commentsCount = 3_400,
                        sentiment = "Tarafsız",
                        instaComments = listOf(
                            InstaCommentItem(id = "C_SRG_1", authorHandle = "kramponsports", authorName = "Krampon Sports", isVerified = true, avatarHex = "#22C55E", text = "Sergen Hoca haklı dağılın 😂👏", timeLabel = "3s", likesCount = 2_140)
                        ),
                        isVerifiedAuthor = true,
                        locationLabel = "Sokrates Stüdyosu",
                        imageSceneType = "LOCKER"
                    )
                )

                // 3. Local Turkish Derby Atmosphere (Tribün Dergi)
                posts.add(
                    SocialMediaPost(
                        id = "DYN_IG_TRIB_P${page}_3",
                        dayIndex = day,
                        dateLabel = "6 saat önce",
                        authorName = "Mackolik",
                        authorHandle = "@mackolik",
                        isUserPost = false,
                        content = "🏟️ ANADOLU DERBİSİNDE MEŞALE ŞOV! Stadyumu dolduran 28 bin taraftar maç boyu susmadı. Türk futbolunun kalbi tribünlerde atıyor! 🧣🔥",
                        likesCount = 54_800,
                        commentsCount = 1_820,
                        sentiment = "Destekleyici",
                        instaComments = listOf(
                            InstaCommentItem(id = "C_TRB_1", authorHandle = "${rivalClub.shortName.lowercase()}_fan", authorName = "${rivalClub.shortName} Tribünü", isVerified = false, avatarHex = rivalClub.primaryColorHex, text = "Deplasman tribününden selamlar!", timeLabel = "5s", likesCount = 140)
                        ),
                        isVerifiedAuthor = true,
                        locationLabel = "${club.stadiumName}",
                        imageSceneType = "STADIUM"
                    )
                )

                // 4. Kenan Yıldız Juventus Highlight
                posts.add(
                    SocialMediaPost(
                        id = "DYN_IG_KENAN_P${page}_4",
                        dayIndex = day,
                        dateLabel = "9 saat önce",
                        authorName = "Kenan Yıldız",
                        authorHandle = "@kenanyildiz_official",
                        isUserPost = false,
                        content = "Torino'da 10 numaranın sorumluluğunu sahada terimin son damlasına kadar taşımaya devam. ⚪⚫👅 Destekleyen tüm Türk ve Juventus taraftarlarına teşekkürler! 🇹🇷🇮🇹",
                        likesCount = 412_000,
                        commentsCount = 8_200,
                        sentiment = "Destekleyici",
                        instaComments = listOf(
                            InstaCommentItem(id = "C_KN_1", authorHandle = "hakancalhanoglu", authorName = "Hakan Çalhanoğlu", isVerified = true, avatarHex = "#1D4ED8", text = "Aynen devam kardeşim! 👏", timeLabel = "8s", likesCount = 12_400)
                        ),
                        isVerifiedAuthor = true,
                        locationLabel = "Allianz Stadium, Torino",
                        imageSceneType = "STADIUM"
                    )
                )
            }
            else -> {
                // 1. Champions League Draw / Stage (beIN SPORTS)
                posts.add(
                    SocialMediaPost(
                        id = "DYN_IG_UCL_DRAW_P${page}_1",
                        dayIndex = day,
                        dateLabel = "3 saat önce",
                        authorName = "beIN SPORTS Türkiye",
                        authorHandle = "@beinsportstr",
                        isUserPost = false,
                        content = "🏆 Şampiyonlar Ligi'nde çeyrek final eşleşmeleri belli oldu! Bayern Münih - PSG, Real Madrid - Arsenal, Inter - Barcelona! Erken final niteliğindeki maçlar nefes kesecek. 🌟⚽",
                        likesCount = 76_400,
                        commentsCount = 3_890,
                        sentiment = "Destekleyici",
                        instaComments = listOf(
                            InstaCommentItem(id = "C_DRW_1", authorHandle = "optacan", authorName = "OptaCan", isVerified = true, avatarHex = "#6366F1", text = "Tarihin en dengeli 8 takımı. Zirve.", timeLabel = "2s", likesCount = 670)
                        ),
                        isVerifiedAuthor = true,
                        locationLabel = "Nyon, İsviçre",
                        imageSceneType = "STADIUM"
                    )
                )

                // 2. Transfer Deadline Rumor (Fabrizio Romano)
                posts.add(
                    SocialMediaPost(
                        id = "DYN_IG_FAB_P${page}_2",
                        dayIndex = day,
                        dateLabel = "5 saat önce",
                        authorName = "Fabrizio Romano",
                        authorHandle = "@fabrizioromano",
                        isUserPost = false,
                        content = "HERE WE GO! 🚨 Borussia Dortmund have completed the signing of 19yo talent on a 5-year contract. €18M fee package agreed. Medicals booked for Wednesday. 🟡⚫✍️",
                        likesCount = 890_000,
                        commentsCount = 14_100,
                        sentiment = "Destekleyici",
                        instaComments = listOf(
                            InstaCommentItem(id = "C_FAB_1", authorHandle = "transfermarkt_tr", authorName = "Transfermarkt TR", isVerified = true, avatarHex = "#00193F", text = "Dortmund scout ekibi yine erkenden bitirdi 📈", timeLabel = "4s", likesCount = 2_450)
                        ),
                        isVerifiedAuthor = true,
                        locationLabel = "Milano, Italia",
                        imageSceneType = "TRANSFER"
                    )
                )

                // 3. Turkish Football Culture (Krampon Sports)
                posts.add(
                    SocialMediaPost(
                        id = "DYN_IG_CULT_P${page}_3",
                        dayIndex = day,
                        dateLabel = "7 saat önce",
                        authorName = "Krampon Sports",
                        authorHandle = "@kramponsports",
                        isUserPost = false,
                        content = "Deplasman otobüsünde sabah 06:00'da mola verip sıcak çorba içen taraftarın huzuru kimsede yok... Türk tribün kültürü bambaşka bir tutku! 🧣☕⚽",
                        likesCount = 98_400,
                        commentsCount = 2_150,
                        sentiment = "Destekleyici",
                        instaComments = listOf(
                            InstaCommentItem(id = "C_CLT_1", authorHandle = "nocontextsuperlig", authorName = "No Context Türk Futbolu", isVerified = true, avatarHex = "#EAB308", text = "Yanında da tulum peynirli simit varsa şampiyonluk gelmiş demektir sksksk", timeLabel = "6s", likesCount = 3_450)
                        ),
                        isVerifiedAuthor = true,
                        locationLabel = "Bolu Dağı Dinlenme Tesisleri",
                        imageSceneType = "LOCKER"
                    )
                )

                // 4. Media Feature of our Player (Sports Digitale)
                val mediaTitle = if (fameTier.ordinal >= PlayerFameTier.RISING_STAR.ordinal) {
                    "🎙️ ÖZEL DOSYA | ${p.fullName} (#${p.kitNumber}) Süper Lig'in parlayan yıldızı! Taktik analistler genç yıldızın hücumdaki etkinliğini mercek altına aldı: 'Gelecek sezon Avrupa kapıları ardına kadar açılabilir.' 🇹🇷📊"
                } else {
                    "🎙️ AKADEMİ DOSYASI | Altyapı akademilerinden A takıma yükselme sürecindeki en büyük engeller ve gençlerin kariyer yönetimi. ⚽📈"
                }
                posts.add(
                    SocialMediaPost(
                        id = "DYN_IG_MEDIA_P${page}_4",
                        dayIndex = day,
                        dateLabel = "9 saat önce",
                        authorName = "Sports Digitale",
                        authorHandle = "@sportsdigitale",
                        isUserPost = false,
                        content = mediaTitle,
                        likesCount = if (fameTier.ordinal >= PlayerFameTier.RISING_STAR.ordinal) 42_100 else 16_800,
                        commentsCount = 1_120,
                        sentiment = "Destekleyici",
                        instaComments = listOf(
                            InstaCommentItem(id = "C_MED_1", authorHandle = "${club.shortName.lowercase()}_ultras", authorName = "${club.shortName} Tribün", isVerified = false, avatarHex = club.primaryColorHex, text = "Bizim evladımız, kimseye vermeyiz! 🦁🔥", timeLabel = "8s", likesCount = 390)
                        ),
                        isVerifiedAuthor = true,
                        locationLabel = "Sports Digitale Stüdyoları",
                        imageSceneType = "TRAINING"
                    )
                )
            }
        }

        return posts
    }

    private fun generateBreakingInstaBatch(state: CareerSaveState): List<SocialMediaPost> {
        val p = state.player
        val club = state.currentClub
        val day = state.currentDayIndex

        return listOf(
            SocialMediaPost(
                id = "BREAK_IG_${System.currentTimeMillis()}_1",
                dayIndex = day,
                dateLabel = "Az önce",
                authorName = "beIN SPORTS Türkiye",
                authorHandle = "@beinsportstr",
                isUserPost = false,
                content = "🚨 SON DAKİKA | TFF Kulüpler Birliği toplantısı sona erdi: Yeni yabancı kuralı, hakem atama algoritması ve alt liglerin yayın gelirleri hakkında tarihi kararlar alındı! Canlı yayın bültenimiz başlıyor 📺🇹🇷",
                likesCount = 31_200,
                commentsCount = 1_450,
                sentiment = "Tarafsız",
                instaComments = listOf(
                    InstaCommentItem(id = "C_BRK_1", authorHandle = "optacan", authorName = "OptaCan", isVerified = true, avatarHex = "#6366F1", text = "Detaylar yayın gelirinde gizli. Kritik.", timeLabel = "Şimdi", likesCount = 190)
                ),
                isVerifiedAuthor = true,
                locationLabel = "Riva Hasan Doğan Tesisleri",
                imageSceneType = "TRANSFER"
            ),
            SocialMediaPost(
                id = "BREAK_IG_${System.currentTimeMillis()}_2",
                dayIndex = day,
                dateLabel = "2 dakika önce",
                authorName = "Yağız Sabuncuoğlu",
                authorHandle = "@yagosabuncuoglu",
                isUserPost = false,
                content = "⚡ FLAŞ | Avrupa kulüplerinin Türkiye transfer komitesi başkanı bu akşam resmi görüşmeler için Riva'da. Masada 3 genç Türk yeteneğin bonservis teklifi var! 🔎🇹🇷",
                likesCount = 44_900,
                commentsCount = 2_100,
                sentiment = "Destekleyici",
                instaComments = listOf(
                    InstaCommentItem(id = "C_BRK_2", authorHandle = "kramponsports", authorName = "Krampon Sports", isVerified = true, avatarHex = "#22C55E", text = "Burada yine sıcak saatler başlıyor 👀🔥", timeLabel = "Şimdi", likesCount = 512)
                ),
                isVerifiedAuthor = true,
                locationLabel = "İstanbul",
                imageSceneType = "TRANSFER"
            )
        )
    }

    private fun generateDynamicXBatch(state: CareerSaveState, page: Int): List<XPostItem> {
        val p = state.player
        val club = state.currentClub
        val day = state.currentDayIndex
        val rivalClub = state.clubs.firstOrNull { it.id != club.id } ?: club
        val fameTier = calculatePlayerFameTier(state)
        val tweets = mutableListOf<XPostItem>()

        when (page % 4) {
            1 -> {
                tweets.add(
                    XPostItem(
                        id = "DYN_XP_P${page}_1",
                        dayIndex = day,
                        dateLabel = "15d",
                        authorName = "Fabrizio Romano",
                        handle = "@fabrizioromano",
                        category = "GAZETECİ",
                        isVerified = true,
                        text = "Real Madrid are preparing their contract extension proposal for Arda Güler until June 2030. Key part of future project alongside Bellingham, Vinicius and Mbappe. 🤍🇹🇷",
                        likes = 68_400,
                        reposts = 8_200
                    )
                )
                tweets.add(
                    XPostItem(
                        id = "DYN_XP_P${page}_2",
                        dayIndex = day,
                        dateLabel = "32d",
                        authorName = "No Context Türk Futbolu",
                        handle = "@nocontextsuperlig",
                        category = "MİZAH",
                        isVerified = true,
                        text = "Hakem 90+6'da VAR ekranına koşarken iki takımın teknik direktörünün aynı anda 4. hakeme sarılma anı 😂📺 Türk futbolu gerçekten dizi gibi.",
                        likes = 24_500,
                        reposts = 3_400
                    )
                )
                tweets.add(
                    XPostItem(
                        id = "DYN_XP_P${page}_3",
                        dayIndex = day,
                        dateLabel = "1s",
                        authorName = "OptaCan",
                        handle = "@optacan",
                        category = "YORUMCU",
                        isVerified = true,
                        text = "14.2 - ${club.name} bu sezon ${club.leagueName}'de maç başına 14.2 şut çekerek ligin en üretken takımlarından biri konumunda. Üretkenlik.",
                        likes = 5_800,
                        reposts = 420
                    )
                )
                if (fameTier.ordinal >= PlayerFameTier.RISING_STAR.ordinal) {
                    tweets.add(
                        XPostItem(
                            id = "DYN_XP_P${page}_4",
                            dayIndex = day,
                            dateLabel = "1s",
                            authorName = "Yağız Sabuncuoğlu",
                            handle = "@yagosabuncuoglu",
                            category = "GAZETECİ",
                            isVerified = true,
                            text = "ÖZEL | Süper Lig devleri ve İtalyan kulüpleri, ${club.name} forması giyen ${p.fullName} (@${p.firstName.lowercase()}${p.kitNumber}) için resmi nabız yokladı. Oyuncunun sözleşme serbest kalma maddesi araştırılıyor. 🔎🇮🇹",
                            likes = 14_200,
                            reposts = 1_850
                        )
                    )
                }
            }
            2 -> {
                tweets.add(
                    XPostItem(
                        id = "DYN_XP_P${page}_1",
                        dayIndex = day,
                        dateLabel = "22d",
                        authorName = "Ertan Süzgün",
                        handle = "@ertansuzgun",
                        category = "GAZETECİ",
                        isVerified = true,
                        text = "TFF 1. Lig ve Süper Lig kulüpleri arasında sürpriz takas görüşmeleri başladı. Ara transfer dönemi öncesi hareketlilik çok yüksek. 📞⚽",
                        likes = 8_900,
                        reposts = 710
                    )
                )
                tweets.add(
                    XPostItem(
                        id = "DYN_XP_P${page}_2",
                        dayIndex = day,
                        dateLabel = "45d",
                        authorName = "Krampon Sports",
                        handle = "@kramponsports",
                        category = "MİZAH",
                        isVerified = true,
                        text = "Teknik direktörün 'Bugün taktik idmanı yok, çift kale maç yapacağız' dediği an futbolcuların gözlerindeki ışıltı paha biçilemez 😂🔥",
                        likes = 31_200,
                        reposts = 2_890
                    )
                )
                tweets.add(
                    XPostItem(
                        id = "DYN_XP_P${page}_3",
                        dayIndex = day,
                        dateLabel = "2s",
                        authorName = "Sports Digitale",
                        handle = "@sportsdigitale",
                        category = "MEDYA",
                        isVerified = true,
                        text = "🎙️ Uğur Karakullukçu: \"${club.name} doğru bir oyun planı buldu. Genç oyuncuların cesareti takımın hücum dinamiğini tamamen değiştirdi.\" 📺⚽",
                        likes = 9_400,
                        reposts = 890
                    )
                )
            }
            3 -> {
                tweets.add(
                    XPostItem(
                        id = "DYN_XP_P${page}_1",
                        dayIndex = day,
                        dateLabel = "10d",
                        authorName = "Mackolik",
                        handle = "@mackolik",
                        category = "MEDYA",
                        isVerified = true,
                        text = "📊 Şampiyonlar Ligi'nde bu sezon en çok dripling yapan Türk futbolcular:\n1️⃣ Arda Güler (18)\n2️⃣ Kenan Yıldız (16)\n3️⃣ Kerem Aktürkoğlu (14)\n\nGurur tablosu! 🇹🇷✨",
                        likes = 29_400,
                        reposts = 2_450
                    )
                )
                tweets.add(
                    XPostItem(
                        id = "DYN_XP_P${page}_2",
                        dayIndex = day,
                        dateLabel = "50d",
                        authorName = "Tribün Şamatası",
                        handle = "@tribun_samata",
                        category = "TARAFTAR",
                        isVerified = false,
                        text = "${rivalClub.shortName} taraftarları yine hakem konuşmaya başlamış. Skorboarda bakın kardeşim ağlamayı bırakın 😂🤫",
                        likes = 3_120,
                        reposts = 480
                    )
                )
            }
            else -> {
                tweets.add(
                    XPostItem(
                        id = "DYN_XP_P${page}_1",
                        dayIndex = day,
                        dateLabel = "18d",
                        authorName = "Transfermarkt Türkiye",
                        handle = "@transfermarkt_tr",
                        category = "YORUMCU",
                        isVerified = true,
                        text = "📈 Süper Lig ara piyasa değeri güncellemesi hazırlıkları başladı! Form grafiklerine göre en az 15 oyuncunun değerinde çift haneli artış bekleniyor. 📊🇹🇷",
                        likes = 12_800,
                        reposts = 1_120
                    )
                )
                tweets.add(
                    XPostItem(
                        id = "DYN_XP_P${page}_2",
                        dayIndex = day,
                        dateLabel = "1s",
                        authorName = "Sokrates Dergi",
                        handle = "@sokratesdergi",
                        category = "MEDYA",
                        isVerified = true,
                        text = "Avrupa futbolunda kanat forvetlerin evrimi ve Türkiye'den çıkan modern hücum profilleri üzerine derin bir analiz. Yazı sitemizde. 📚⚽",
                        likes = 6_450,
                        reposts = 510
                    )
                )
            }
        }
        return tweets
    }

    private fun generateBreakingXBatch(state: CareerSaveState): List<XPostItem> {
        val p = state.player
        val club = state.currentClub
        val day = state.currentDayIndex

        return listOf(
            XPostItem(
                id = "BREAK_XP_${System.currentTimeMillis()}_1",
                dayIndex = day,
                dateLabel = "Az önce",
                authorName = "Yağız Sabuncuoğlu",
                handle = "@yagosabuncuoglu",
                category = "GAZETECİ",
                isVerified = true,
                text = "⚡ SON DAKİKA | TFF Yönetim Kurulu, MHK hakem değerlendirme raporunu onayladı. 2 hakem dinlendirilecek. Kulisler alev aldı! 🔎🇹🇷",
                likes = 28_400,
                reposts = 3_890
            ),
            XPostItem(
                id = "BREAK_XP_${System.currentTimeMillis()}_2",
                dayIndex = day,
                dateLabel = "2d",
                authorName = "Fabrizio Romano",
                handle = "@fabrizioromano",
                category = "GAZETECİ",
                isVerified = true,
                text = "🚨 BREAKING | Major European clubs are preparing official scout delegations for upcoming Turkish league fixtures. Transfer window heating up! 🇹🇷✈️",
                likes = 45_100,
                reposts = 6_200
            )
        )
    }

    // =========================================================================
    // 7. WHATSAPP STATUS UPDATES & TRANSFERMARKT RUMOR MILL
    // =========================================================================

    fun getWhatsAppStatuses(state: CareerSaveState): List<WhatsAppStatusItem> {
        val p = state.player
        val club = state.currentClub
        return state.npcs.mapIndexed { idx, npc ->
            val caption = when {
                npc.roleTitle.contains("Teknik", ignoreCase = true) ->
                    "Taktik tahtasında hafta sonu planımız hazır. Disiplin, tempo ve 3 puan! 📋⚽ #${club.shortName}"
                npc.roleTitle.contains("Menajer", ignoreCase = true) ->
                    "İstanbul -> Frankfurt uçuşu ✈️ Oyuncularımız için yeni sezon görüşmeleri tam gaz devam ediyor 💼"
                npc.roleTitle.contains("Aile", ignoreCase = true) ->
                    "Evlatlarımızla gurur duyuyoruz, dualarımız seninle oğlum 🙏❤️⚽"
                npc.id == "NPC_TEAM_GROUP" || npc.roleTitle.contains("Kaptan", ignoreCase = true) ->
                    "Tesislerde idman bitti, şimdi dinlenme vakti! Hafta sonu stadyumu dolduralım 💪🔥"
                npc.roleTitle.contains("Basın", ignoreCase = true) || npc.roleTitle.contains("Gazeteci", ignoreCase = true) ->
                    "Stüdyoda yayın hazırlığı 🎙️ Bu akşam özel transfer kulislerini açıklıyorum!"
                else ->
                    "${club.city}'da harika bir akşam ✨☕"
            }
            val scene = when (idx % 4) {
                0 -> "TRAINING"
                1 -> "TRANSFER"
                2 -> "LOCKER"
                else -> "STADIUM"
            }
            val id = "WA_ST_${npc.id}_D${state.currentDayIndex}"
            WhatsAppStatusItem(
                id = id,
                npcId = npc.id,
                authorName = npc.name,
                authorRole = npc.roleTitle,
                avatarHex = npc.avatarColorHex,
                timeLabel = "Bugün ${10 + (idx % 9)}:${(idx * 15 % 60).toString().padStart(2, '0')}",
                caption = caption,
                bgHex = npc.avatarColorHex,
                sceneType = scene,
                viewed = viewedWaStatusIds[id] == true
            )
        }
    }

    fun markWaStatusViewed(statusId: String) {
        viewedWaStatusIds[statusId] = true
    }

    fun getTransfermarktRumors(state: CareerSaveState): List<RumorMillEntry> {
        val p = state.player
        val club = state.currentClub
        val rivalClub = state.clubs.firstOrNull { it.id != club.id } ?: club
        val topTierClub = state.clubs.minByOrNull { it.tier } ?: rivalClub

        val userOfferRumors = state.transferOffers.mapIndexed { idx, offer ->
            RumorMillEntry(
                id = "RMR_OFF_${offer.id}",
                playerName = p.fullName,
                playerPosition = p.primaryPosition.code,
                age = p.age,
                fromClubName = club.shortName,
                toClubName = offer.clubName,
                toClubHex = offer.primaryColorHex,
                marketValueEur = p.marketValueEur,
                probabilityPct = (62 + idx * 11).coerceAtMost(89),
                sourceHandle = "@yagosabuncuoglu",
                isUserPlayer = true
            )
        }

        val worldRumors = listOf(
            RumorMillEntry(
                id = "RMR_USER_SCOUT",
                playerName = p.fullName,
                playerPosition = p.primaryPosition.code,
                age = p.age,
                fromClubName = club.shortName,
                toClubName = if (topTierClub.id != club.id) topTierClub.name else "Eintracht Frankfurt",
                toClubHex = topTierClub.primaryColorHex,
                marketValueEur = p.marketValueEur,
                probabilityPct = (42 + (p.overallRating - 55).coerceAtLeast(0)).coerceIn(35, 84),
                sourceHandle = "@fabrizioromano",
                isUserPlayer = true
            ),
            RumorMillEntry(
                id = "RMR_W_1",
                playerName = "Semih Kılıçsoy",
                playerPosition = "SNT",
                age = 20,
                fromClubName = "BJK",
                toClubName = "Aston Villa",
                toClubHex = "#7A003C",
                marketValueEur = 15_000_000L,
                probabilityPct = 58,
                sourceHandle = "@fabrizioromano",
                isUserPlayer = false
            ),
            RumorMillEntry(
                id = "RMR_W_2",
                playerName = "Barış Alper Yılmaz",
                playerPosition = "KNT",
                age = 25,
                fromClubName = "GS",
                toClubName = "Brighton & Hove Albion",
                toClubHex = "#0057B8",
                marketValueEur = 21_000_000L,
                probabilityPct = 64,
                sourceHandle = "@yagosabuncuoglu",
                isUserPlayer = false
            ),
            RumorMillEntry(
                id = "RMR_W_3",
                playerName = state.topScorers.firstOrNull()?.playerName ?: "Eren Aydın",
                playerPosition = "SNT",
                age = 23,
                fromClubName = state.topScorers.firstOrNull()?.clubName ?: rivalClub.shortName,
                toClubName = rivalClub.name,
                toClubHex = rivalClub.primaryColorHex,
                marketValueEur = (p.marketValueEur * 3 / 2).coerceAtLeast(350_000L),
                probabilityPct = 49,
                sourceHandle = "@ertansuzgun",
                isUserPlayer = false
            )
        )

        return (userOfferRumors + worldRumors).distinctBy { "${it.playerName}_${it.toClubName}" }
    }

    /**
     * Pure helper that can also be called from CareerEngine / ViewModel to persist
     * daily living world posts directly into CareerSaveState across days.
     */
    fun ensureDailySocialWorld(state: CareerSaveState): CareerSaveState {
        val livingInsta = getLivingInstaFeed(state)
        val livingX = getLivingXFeed(state)
        val livingStories = getLivingStories(state)
        val livingDms = getAllInstaDmThreads(state)
        return state.copy(
            socialPosts = livingInsta.take(30),
            xPosts = livingX.take(40),
            stories = livingStories.take(15),
            instaDmThreads = livingDms
        )
    }

    fun formatCompactCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0).replace('.', ',')
            count >= 10_000 -> String.format("%.1fB", count / 1_000.0).replace('.', ',')
            count >= 1_000 -> "${count / 1000}.${(count % 1000) / 100}B"
            else -> "$count"
        }
    }
}
