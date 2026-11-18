package com.example.engine.social

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.example.engine.data.TurkishLeaguesDatabase
import com.example.model.CareerSaveState
import com.example.model.Club
import com.example.model.InstaCommentItem
import com.example.model.SocialMediaPost
import com.example.model.XPostItem
import kotlin.math.abs
import kotlin.random.Random

/**
 * Dynamic & Infinite Social Media Feed Engine.
 *
 * Produces an authentic, living Turkish football feed using:
 * - 137 real Turkish clubs from [TurkishLeaguesDatabase] (Süper Lig, 1. Lig, 2. Lig, 3. Lig, BAL).
 * - Hundreds of realistic supporter, regional journalist, analyst, and meme accounts.
 * - Non-repetitive dynamic posts on every day/week advance, pull-to-refresh, and pagination.
 * - Deep tier/league context: lower-tier players see intense regional rivalries, muddy pitch memes,
 *   away trip bus stories, referee disputes without unrealistic superstar mentions.
 */
object DynamicFeedGenerator {

    // Dynamic state holders for infinite scroll and pull-to-refresh
    private val prependedXPosts = mutableStateListOf<XPostItem>()
    private val appendedXPosts = mutableStateListOf<XPostItem>()
    private val currentXPage = mutableStateOf(0)
    private val refreshXCount = mutableStateOf(0)

    private val prependedInstaPosts = mutableStateListOf<SocialMediaPost>()
    private val appendedInstaPosts = mutableStateListOf<SocialMediaPost>()
    private val currentInstaPage = mutableStateOf(0)
    private val refreshInstaCount = mutableStateOf(0)

    // Interaction overlay maps
    private val quotedPostEmbeds = mutableStateMapOf<String, XQuotedPostEmbed>()
    private val dynamicXReplies = mutableStateMapOf<String, List<XReplyItem>>()
    private val dynamicInstaReplies = mutableStateMapOf<String, List<InstaCommentReply>>()
    private val registeredDynamicAccounts = mutableStateMapOf<String, SocialAccountProfile>()

    // Last observed dayIndex to trigger automatic fresh updates when game advances
    private var lastObservedDayIndex: Int = -1

    // =========================================================================
    // 1. ACCOUNT GENERATION (HUNDREDS OF REALISTIC TURKISH FOOTBALL ACCOUNTS)
    // =========================================================================

    private val dedicatedNationalMediaAccounts = listOf(
        SocialAccountProfile(
            id = "ACC_ALTLIG_SESI",
            handle = "@altliglerin_sesi",
            displayName = "Alt Liglerin Sesi",
            category = SocialAccountCategory.MEDIA,
            isVerified = true,
            badgeType = VerifiedBadgeType.BLUE,
            avatarHex = "#10B981",
            bannerHex = "#064E3B",
            bio = "TFF 1. Lig, 2. Lig, 3. Lig ve BAL'ın nabzı burada atıyor! 📢⚽ Tarafsız haber, maç özetleri ve alt lig transfer kulisleri.",
            location = "Anadolu / İstanbul",
            followersCountText = "284B",
            followingCount = 420,
            postsCount = 14_200,
            isFollowedByDefault = true
        ),
        SocialAccountProfile(
            id = "ACC_TFF1LIG_GUNLUGU",
            handle = "@tff1lig_gunlugu",
            displayName = "TFF 1. Lig Günlüğü",
            category = SocialAccountCategory.MEDIA,
            isVerified = true,
            badgeType = VerifiedBadgeType.BLUE,
            avatarHex = "#3B82F6",
            bannerHex = "#1E3A8A",
            bio = "Trendyol 1. Lig'de şampiyonluk ve play-off yarışının en sıcak gelişmeleri! ⚡ Canlı skorlar ve hakem analizleri.",
            location = "Türkiye",
            followersCountText = "195B",
            followingCount = 310,
            postsCount = 9_850,
            isFollowedByDefault = true
        ),
        SocialAccountProfile(
            id = "ACC_ANADOLU_FUTBOLU",
            handle = "@anadolu_futbolu",
            displayName = "Anadolu Futbolu & Tribünler",
            category = SocialAccountCategory.FAN_ULTRAS,
            isVerified = true,
            badgeType = VerifiedBadgeType.BLUE,
            avatarHex = "#EF4444",
            bannerHex = "#7F1D1D",
            bio = "Anadolu'nun köklü çınarları, efsane şehir kulüpleri ve cefakar deplasman tribünleri! 🏟️💚❤️ Cefası bizim sefası kimin?",
            location = "Anadolu",
            followersCountText = "165B",
            followingCount = 540,
            postsCount = 11_300,
            isFollowedByDefault = true
        ),
        SocialAccountProfile(
            id = "ACC_TFF2_3LIG_RAPORU",
            handle = "@tff2_3lig_raporu",
            displayName = "2. ve 3. Lig Raporu",
            category = SocialAccountCategory.JOURNALIST,
            isVerified = true,
            badgeType = VerifiedBadgeType.BLUE,
            avatarHex = "#F59E0B",
            bannerHex = "#78350F",
            bio = "Nesine 2. Lig ve 3. Lig haftalık panorama, gol krallığı, hakem raporları ve genç yetenek takibi. 📊⚽",
            location = "Ankara",
            followersCountText = "112B",
            followingCount = 290,
            postsCount = 8_400,
            isFollowedByDefault = true
        ),
        SocialAccountProfile(
            id = "ACC_BAL_LIGI_TUTKUSU",
            handle = "@bal_ligi_tutkusu",
            displayName = "BAL Ligi Ruhu",
            category = SocialAccountCategory.FAN_ULTRAS,
            isVerified = true,
            badgeType = VerifiedBadgeType.BLUE,
            avatarHex = "#8B5CF6",
            bannerHex = "#4C1D95",
            bio = "Bölgesel Amatör Lig: Gerçek futbol sevdalılarının ligi! 🏆 Köy statları, çamurlu formalar ve bitmeyen tutku.",
            location = "Türkiye",
            followersCountText = "88B",
            followingCount = 210,
            postsCount = 6_700,
            isFollowedByDefault = false
        ),
        SocialAccountProfile(
            id = "ACC_ALTLIG_CAPS",
            handle = "@altlig_caps",
            displayName = "Alt Lig Mizah & Caps",
            category = SocialAccountCategory.MEME_PAGE,
            isVerified = true,
            badgeType = VerifiedBadgeType.BLUE,
            avatarHex = "#EC4899",
            bannerHex = "#831843",
            bio = "Türk futbolunun eşsiz mizahı, alt lig absürtlükleri ve maç sonu geyikleri 😂 Her şey mizah amaçlıdır.",
            location = "Mizah Kazanı",
            followersCountText = "340B",
            followingCount = 180,
            postsCount = 16_900,
            isFollowedByDefault = true
        ),
        SocialAccountProfile(
            id = "ACC_TRIBUN_REPLIKLERI",
            handle = "@tribun_replikleri",
            displayName = "Tribün Replikleri & Besteler",
            category = SocialAccountCategory.FAN_ULTRAS,
            isVerified = true,
            badgeType = VerifiedBadgeType.BLUE,
            avatarHex = "#14B8A6",
            bannerHex = "#134E4A",
            bio = "Besteler, pankartlar, deplasman otobüsleri ve meşaleli geceler 🥁 Deplasman otobüsünde çalan o marş...",
            location = "Tribünler",
            followersCountText = "225B",
            followingCount = 450,
            postsCount = 12_100,
            isFollowedByDefault = true
        ),
        SocialAccountProfile(
            id = "ACC_TFF_ANALIZ_ODASI",
            handle = "@tff_analiz_odasi",
            displayName = "Alt Lig Taktik & Veri Odası",
            category = SocialAccountCategory.ANALYST,
            isVerified = true,
            badgeType = VerifiedBadgeType.BLUE,
            avatarHex = "#6366F1",
            bannerHex = "#312E81",
            bio = "xG verileri, pres haritaları, geçiş hücumları ve alt lig teknik direktör taktikleri. 📈🧠",
            location = "İzmir / İstanbul",
            followersCountText = "76B",
            followingCount = 140,
            postsCount = 4_900,
            isFollowedByDefault = true
        ),
        SocialAccountProfile(
            id = "ACC_KOY_SANDIGI",
            handle = "@koy_sandigi_futbol",
            displayName = "Köy Sandığı Futbol Caps",
            category = SocialAccountCategory.MEME_PAGE,
            isVerified = true,
            badgeType = VerifiedBadgeType.BLUE,
            avatarHex = "#EAB308",
            bannerHex = "#713F12",
            bio = "Patates tarlasında tiki-taka, battaniyeli 3. kaleci ve yedek kulübesinde çay keyfi 🥔☕",
            location = "Köy Sahası",
            followersCountText = "142B",
            followingCount = 95,
            postsCount = 7_200,
            isFollowedByDefault = false
        ),
        SocialAccountProfile(
            id = "ACC_HALI_SAHA_EFSANELERI",
            handle = "@hali_saha_efsaneleri",
            displayName = "Halı Saha Efsaneleri",
            category = SocialAccountCategory.MEME_PAGE,
            isVerified = true,
            badgeType = VerifiedBadgeType.BLUE,
            avatarHex = "#06B6D4",
            bannerHex = "#164E63",
            bio = "Sol ayağını kullanan adamın yarattığı heyecan ve 'abi parasını verip gelmeyenler' kulübü ⚽😂",
            location = "Halı Saha",
            followersCountText = "180B",
            followingCount = 120,
            postsCount = 9_300,
            isFollowedByDefault = false
        ),
        SocialAccountProfile(
            id = "ACC_SCOUT_ALTLIG",
            handle = "@scout_turkiye_altlig",
            displayName = "Türk Futbolu Genç Scout",
            category = SocialAccountCategory.ANALYST,
            isVerified = true,
            badgeType = VerifiedBadgeType.BLUE,
            avatarHex = "#059669",
            bannerHex = "#064E3B",
            bio = "Alt liglerden Süper Lig'e ve Avrupa'ya aday genç yeteneklerin potansiyel raporları 🔍🇹🇷",
            location = "Türkiye",
            followersCountText = "94B",
            followingCount = 680,
            postsCount = 5_800,
            isFollowedByDefault = true
        )
    )

    init {
        dedicatedNationalMediaAccounts.forEach {
            registeredDynamicAccounts[it.cleanHandle.lowercase()] = it
            registeredDynamicAccounts[it.handle.lowercase()] = it
        }
    }

    /**
     * Resolves an account dynamically by handle or clean name.
     * Generates a fully fleshed out profile if not already cached.
     */
    fun resolveAccount(handleOrName: String, state: CareerSaveState): SocialAccountProfile {
        val clean = handleOrName.trim().removePrefix("@").lowercase()
        registeredDynamicAccounts[clean]?.let { return it }

        // Check if handle matches a club in database
        val allClubs = TurkishLeaguesDatabase.getAllTurkishClubs()
        val matchingClub = allClubs.find { club ->
            val cName = club.shortName.lowercase().replace(" ", "")
            val fName = club.name.lowercase().replace(" ", "")
            clean.contains(cName) || clean.contains(fName) || clean.startsWith(club.city.lowercase())
        } ?: state.currentClub

        val isFan = clean.contains("fan") || clean.contains("tribun") || clean.contains("maraton") ||
                clean.contains("tayfa") || clean.contains("genc") || clean.contains("sevdali")
        val isMedia = clean.contains("posta") || clean.contains("spor") || clean.contains("ajans") ||
                clean.contains("haber") || clean.contains("bulten") || clean.contains("gazete")
        val isMeme = clean.contains("caps") || clean.contains("mizah") || clean.contains("replik") || clean.contains("koy")

        val category = when {
            isMeme -> SocialAccountCategory.MEME_PAGE
            isMedia -> SocialAccountCategory.MEDIA
            isFan -> SocialAccountCategory.FAN_ULTRAS
            else -> SocialAccountCategory.FAN_ULTRAS
        }

        val displayName = when {
            isMeme -> "${matchingClub.shortName} Mizah & Caps"
            isMedia -> "${matchingClub.city} Spor Postası"
            clean.contains("tribun") -> "${matchingClub.shortName} Tribün Sayfası"
            clean.contains("maraton") -> "${matchingClub.shortName} Maraton Grubu"
            else -> "${clean.replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }} | ${matchingClub.shortName}"
        }

        val profile = SocialAccountProfile(
            id = "DYN_ACC_$clean",
            handle = "@$clean",
            displayName = displayName,
            category = category,
            isVerified = isMedia || isMeme,
            badgeType = if (isMedia) VerifiedBadgeType.BLUE else VerifiedBadgeType.NONE,
            avatarHex = matchingClub.primaryColorHex,
            bannerHex = matchingClub.secondaryColorHex,
            bio = "⚽ ${matchingClub.name} sevdalısı • ${matchingClub.leagueName} takipçisi. 🏟️ ${matchingClub.stadiumName}",
            location = matchingClub.city,
            followersCountText = "${(abs(clean.hashCode()) % 85 + 3)},${abs(clean.hashCode()) % 9}B",
            followingCount = 180 + (abs(clean.hashCode()) % 320),
            postsCount = 80 + (abs(clean.hashCode()) % 450),
            isFollowedByDefault = false
        )

        registeredDynamicAccounts[clean] = profile
        registeredDynamicAccounts["@$clean"] = profile
        return profile
    }

    fun getDynamicAccounts(state: CareerSaveState): List<SocialAccountProfile> {
        val list = mutableListOf<SocialAccountProfile>()
        list.addAll(dedicatedNationalMediaAccounts)

        val leagueClubs = getLeagueClubs(state)
        leagueClubs.take(12).forEach { club ->
            val fanHandle = "${club.shortName.lowercase().replace(" ", "")}_tribun"
            list.add(resolveAccount(fanHandle, state))
        }
        return list
    }

    // =========================================================================
    // 2. LEAGUE CLUBS & CONTEXT ENGINE
    // =========================================================================

    private fun getLeagueClubs(state: CareerSaveState): List<Club> {
        val club = state.currentClub
        val dbClubs = TurkishLeaguesDatabase.getClubsForLeague(club.leagueId)
        if (dbClubs.isNotEmpty()) return dbClubs

        val tierClubs = TurkishLeaguesDatabase.getClubsByTier(club.tier)
        if (tierClubs.isNotEmpty()) return tierClubs

        return if (state.clubs.isNotEmpty()) state.clubs else listOf(club)
    }

    private fun isRestrictedGlobalContentInLowerTier(
        text: String,
        handle: String,
        authorName: String,
        id: String
    ): Boolean {
        val cleanHandle = handle.removePrefix("@").lowercase().trim()
        if (SocialRealismEngine.RESTRICTED_GLOBAL_HANDLES.contains(cleanHandle)) {
            return true
        }

        val lowerText = text.lowercase()
        val lowerHandle = handle.lowercase()
        val lowerAuthor = authorName.lowercase()
        val lowerId = id.lowercase()

        val restrictedKeywords = listOf(
            "fabrizio",
            "romano",
            "arda güler",
            "arda guler",
            "ardaguler",
            "real madrid",
            "realmadrid",
            "chloé",
            "chloe",
            "dubois",
            "mbappe",
            "bellingham",
            "vinicius",
            "433",
            "yagosabuncuoglu",
            "beinsports"
        )

        return restrictedKeywords.any { kw ->
            lowerText.contains(kw) ||
                    lowerHandle.contains(kw) ||
                    lowerAuthor.contains(kw) ||
                    lowerId.contains(kw)
        }
    }

    private fun isLowerTier(state: CareerSaveState): Boolean {
        return SocialRealismEngine.isLowerTier(state.currentClub.leagueId) || state.currentClub.tier >= 2
    }

    // =========================================================================
    // 3. X (TWITTER) LIVING & INFINITE FEED ENGINE
    // =========================================================================

    /**
     * Returns the comprehensive, dynamic living feed for X.
     * Merges prepended breaking updates, base dynamic batches, state posts, and appended paginations.
     */
    fun getLivingXFeed(state: CareerSaveState): List<XPostItem> {
        // Check if day changed: auto-inject fresh breaking match news for the new day
        if (lastObservedDayIndex != -1 && lastObservedDayIndex != state.currentDayIndex) {
            val freshBatch = generateBreakingXTweets(state, isDayAdvance = true)
            prependedXPosts.addAll(0, freshBatch)
        }
        lastObservedDayIndex = state.currentDayIndex

        val baseDynamic = generateBaseLeagueXTweets(state)
        val combined = mutableListOf<XPostItem>()
        val seenIds = mutableSetOf<String>()

        // 1. Fresh prepended posts (from pull-to-refresh & day advances)
        prependedXPosts.forEach {
            if (seenIds.add(it.id)) combined.add(it)
        }

        // 2. Base dynamic tweets for today
        baseDynamic.forEach {
            if (seenIds.add(it.id)) combined.add(it)
        }

        // 3. Any saved state xPosts that are appropriate for tier
        state.xPosts.forEach { xp ->
            val isRestricted = isLowerTier(state) && isRestrictedGlobalContentInLowerTier(
                text = xp.text,
                handle = xp.handle,
                authorName = xp.authorName,
                id = xp.id
            )
            if (!isRestricted && seenIds.add(xp.id)) {
                combined.add(xp)
            }
        }

        // 4. Appended posts (from infinite scroll pagination)
        appendedXPosts.forEach {
            if (seenIds.add(it.id)) combined.add(it)
        }

        return combined
    }

    /**
     * Pull-to-refresh: Generates brand new breaking news, match debates, fan arguments.
     */
    fun refreshXFeed(state: CareerSaveState): Int {
        refreshXCount.value++
        val freshBatch = generateBreakingXTweets(state, isDayAdvance = false)
        prependedXPosts.addAll(0, freshBatch)
        return freshBatch.size
    }

    /**
     * Infinite scroll pagination: Loads a new, non-repeating batch of tweets.
     */
    fun loadMoreXPosts(state: CareerSaveState): Int {
        currentXPage.value++
        val page = currentXPage.value
        val newTweets = generatePaginatedXTweets(state, page)
        appendedXPosts.addAll(newTweets)
        return newTweets.size
    }

    // =========================================================================
    // 4. INSTAGRAM LIVING & INFINITE FEED ENGINE
    // =========================================================================

    /**
     * Returns the comprehensive, dynamic living feed for Instagram.
     */
    fun getLivingInstaFeed(state: CareerSaveState): List<SocialMediaPost> {
        val baseDynamic = generateBaseLeagueInstaPosts(state)
        val combined = mutableListOf<SocialMediaPost>()
        val seenIds = mutableSetOf<String>()

        prependedInstaPosts.forEach {
            if (seenIds.add(it.id)) combined.add(it)
        }

        baseDynamic.forEach {
            if (seenIds.add(it.id)) combined.add(it)
        }

        state.socialPosts.forEach { post ->
            val isRestricted = isLowerTier(state) && isRestrictedGlobalContentInLowerTier(
                text = post.content,
                handle = post.authorHandle,
                authorName = post.authorName,
                id = post.id
            )
            if (!isRestricted && seenIds.add(post.id)) {
                combined.add(post)
            }
        }

        appendedInstaPosts.forEach {
            if (seenIds.add(it.id)) combined.add(it)
        }

        return combined
    }

    /**
     * Pull-to-refresh for Instagram.
     */
    fun refreshInstaFeed(state: CareerSaveState): Int {
        refreshInstaCount.value++
        val freshBatch = generateBreakingInstaPosts(state)
        prependedInstaPosts.addAll(0, freshBatch)
        return freshBatch.size
    }

    /**
     * Infinite scroll pagination for Instagram.
     */
    fun loadMoreInstaPosts(state: CareerSaveState): Int {
        currentInstaPage.value++
        val page = currentInstaPage.value
        val newBatch = generatePaginatedInstaPosts(state, page)
        appendedInstaPosts.addAll(newBatch)
        return newBatch.size
    }

    // =========================================================================
    // 5. RICH DYNAMIC CONTENT GENERATION HELPERS
    // =========================================================================

    private fun generateBaseLeagueXTweets(state: CareerSaveState): List<XPostItem> {
        val p = state.player
        val club = state.currentClub
        val day = state.currentDayIndex
        val leagueClubs = getLeagueClubs(state)
        val otherClubs = leagueClubs.filter { it.id != club.id }.shuffled(Random(day * 31 + 7))

        val rival1 = otherClubs.getOrNull(0) ?: club
        val rival2 = otherClubs.getOrNull(1) ?: club
        val rival3 = otherClubs.getOrNull(2) ?: club
        val rival4 = otherClubs.getOrNull(3) ?: club

        val tweets = mutableListOf<XPostItem>()

        // 1. Other Match Debate in the League (Mackolik / Alt Liglerin Sesi)
        val matchResultScore1 = (abs(day * 7 + rival1.name.hashCode()) % 3)
        val matchResultScore2 = (abs(day * 13 + rival2.name.hashCode()) % 3)
        tweets.add(
            XPostItem(
                id = "DYN_XP_BASE_MATCH_${day}_${rival1.id}_${rival2.id}",
                dayIndex = day,
                dateLabel = "42d",
                authorName = "Alt Liglerin Sesi",
                handle = "@altliglerin_sesi",
                category = "MEDYA",
                isVerified = true,
                text = "🏁 MAÇ SONUCU | ${club.leagueName}'de haftanın en kritik randevusu nefes kesti!\n\n⚽ ${rival1.name} $matchResultScore1 - $matchResultScore2 ${rival2.name}\n🏟️ ${rival1.stadiumName}\n\n90+4'te verilmeyen penaltı kararı ortalığı karıştırdı. Sizce hakem pozisyonda haklı mıydı? 👇🎙️",
                likes = 4_230,
                reposts = 390
            )
        )

        // 2. Fan Account Banter between Rival Clubs (e.g. Sakarya - Kocaeli, Bursa - Karşıyaka)
        val fanHandle1 = "@${rival1.shortName.lowercase().replace(" ", "")}_tribun"
        tweets.add(
            XPostItem(
                id = "DYN_XP_BASE_FAN_${day}_${rival1.id}",
                dayIndex = day,
                dateLabel = "1s",
                authorName = "${rival1.shortName} Tribün Sevdası",
                handle = fanHandle1,
                category = "TARAFTAR",
                isVerified = false,
                text = "${rival2.name} deplasmanına 45 otobüs gittik, 90 dakika stadı inlettik! Skor ne olursa olsun bu armaya can feda! Ama o hakem o düdüğü assın, bu lig böyle yönetilemez! 🟢⚫🔥",
                likes = 1_840,
                reposts = 210
            )
        )

        // 3. Humor / Meme Post about Pitch or Culture
        val memeTemplates = listOf(
            "Zemine bakınca futbol maçı mı yoksa pirinç tarlası mı belli değil 😂 Top zıplamıyor, çamura saplanıyor sksksk",
            "Yedek kulübesinde battaniyeye sarılıp çay içen 3. kaleci huzuru... Dünyanın en rahat mesleği yemin ederim 😂☕",
            "89. dakikada skor 1-0'ken top toplayıcı çocuğun topu stadyum dışına fırlatması... Safi alt lig zekası ahahah 🚜",
            "Teknik direktörün 82'de taktik tahtasına sadece 'ÇIRPININ' yazması... İşte Anadolu futbolu taktik zekası 😂🔥",
            "Hafta sonu halı sahada defanstan çalımla çıkmaya çalışıp topu kaptıran arkadaşın yarattığı kriz > Şampiyonlar Ligi sksksk"
        )
        val selectedMeme = memeTemplates[abs(day * 17) % memeTemplates.size]
        tweets.add(
            XPostItem(
                id = "DYN_XP_BASE_MEME_${day}",
                dayIndex = day,
                dateLabel = "2s",
                authorName = "Alt Lig Mizah & Caps",
                handle = "@altlig_caps",
                category = "MİZAH",
                isVerified = true,
                text = "$selectedMeme 🎭⚽",
                likes = 12_800,
                reposts = 1_920
            )
        )

        // 4. Regional Transfer / Squad Crisis News (Realistic for current league!)
        tweets.add(
            XPostItem(
                id = "DYN_XP_BASE_RUMOR_${day}_${rival3.id}",
                dayIndex = day,
                dateLabel = "3s",
                authorName = "Alt Lig Transfer & Kulis",
                handle = "@altlig_transfer",
                category = "GAZETECİ",
                isVerified = true,
                text = "KULİS | ${rival3.name} yönetimi, ${rival4.name}'nin sözleşmesi sezon sonu bitecek olan dinamo orta sahası için nabız yokladı. İki kulüp başkanının İstanbul'da yemekte bir araya geldiği öğrenildi. 🔎⚽",
                likes = 3_120,
                reposts = 280
            )
        )

        // 5. Tactical Analysis or League Standings Tweet
        tweets.add(
            XPostItem(
                id = "DYN_XP_BASE_ANALYSIS_${day}",
                dayIndex = day,
                dateLabel = "4s",
                authorName = "Alt Lig Taktik & Veri Odası",
                handle = "@tff_analiz_odasi",
                category = "ANALİZ",
                isVerified = true,
                text = "📊 ${club.leagueName} Haftalık Veri Raporu:\n\n• En çok ikili mücadele kazanan: ${rival1.shortName} (%58.4)\n• Ceza sahasında en çok topla buluşan: ${club.name} (24.1)\n• Rakip sahada en hızlı top kazanan: ${rival2.shortName} (6.2 sn)\n\nLigde makas giderek daralıyor, taktik disiplin şampiyonu belirleyecek.",
                likes = 2_650,
                reposts = 310
            )
        )

        // 6. User Player Realistic Spotlight (Tier-appropriate: NO Arda Güler or Real Madrid in lower tiers!)
        if (isLowerTier(state)) {
            val localFanHandle = "@${club.shortName.lowercase().replace(" ", "")}_tayfa"
            tweets.add(
                XPostItem(
                    id = "DYN_XP_BASE_PLAYER_LOCAL_${day}",
                    dayIndex = day,
                    dateLabel = "5s",
                    authorName = "${club.shortName} Sevdalıları",
                    handle = localFanHandle,
                    category = "TARAFTAR",
                    isVerified = false,
                    text = "Bizim genç çocuk ${p.fullName} (#${p.kitNumber}) son idmanlarda müthiş çalışıyor. ${club.managerName} bu çocuğu daha fazla oynatmalı, kumaşı bu ligin çok üstünde! 👏🔥",
                    likes = 1_420,
                    reposts = 185
                )
            )
        } else {
            // Super Lig / National Hero level spotlight
            tweets.add(
                XPostItem(
                    id = "DYN_XP_BASE_PLAYER_SUPER_${day}",
                    dayIndex = day,
                    dateLabel = "5s",
                    authorName = "Süper Lig Nabzı",
                    handle = "@superlignabzi",
                    category = "GAZETECİ",
                    isVerified = true,
                    text = "Süper Lig'de haftanın en çok konuşulan ismi ${club.name}'den ${p.fullName}. Genç yıldızın hücumdaki üretkenliği A Milli Takım teknik heyetinin de dikkatini çekmiş durumda. 🇹🇷🔥",
                    likes = 18_400,
                    reposts = 2_150
                )
            )
        }

        return tweets
    }

    private fun generateBreakingXTweets(state: CareerSaveState, isDayAdvance: Boolean): List<XPostItem> {
        val club = state.currentClub
        val day = state.currentDayIndex
        val seed = refreshXCount.value * 73 + day * 19 + if (isDayAdvance) 5000 else 100
        val rng = Random(seed)
        val leagueClubs = getLeagueClubs(state).shuffled(rng)
        val c1 = leagueClubs.getOrNull(0) ?: club
        val c2 = leagueClubs.getOrNull(1) ?: club
        val c3 = leagueClubs.getOrNull(2) ?: club

        val count = refreshXCount.value
        val list = mutableListOf<XPostItem>()

        // Breaking Referee Scandal
        list.add(
            XPostItem(
                id = "BRK_XP_REF_${day}_${count}_${c1.id}",
                dayIndex = day,
                dateLabel = "Şimdi",
                authorName = "Alt Liglerin Sesi",
                handle = "@altliglerin_sesi",
                category = "SON DAKİKA",
                isVerified = true,
                text = "⚡ SON DAKİKA | TFF Merkez Hakem Kurulu, dün oynanan ${c1.name} - ${c2.name} maçındaki tartışmalı kararlar nedeniyle maçın orta hakemine 4 hafta dinlendirme kararı verdi! 🚨⚖️",
                likes = 7_450 + rng.nextInt(2000),
                reposts = 980 + rng.nextInt(400)
            )
        )

        // Viral Fan Argument
        val fanName = "@${c1.shortName.lowercase().replace(" ", "")}_fan${(1920 + rng.nextInt(85))}"
        list.add(
            XPostItem(
                id = "BRK_XP_FAN_${day}_${count}_${c2.id}",
                dayIndex = day,
                dateLabel = "3d",
                authorName = "${c1.shortName} Fanatik",
                handle = fanName,
                category = "TARAFTAR",
                isVerified = false,
                text = "Hakem hakkımızı yese de, federasyon ceza yağdırsa da bu şehir boyun eğmez! Pazar günü ${c1.stadiumName}'nda cehennemi yaşayacaksınız! Biletler 15 dakikada tükendi! 🔥🏟️",
                likes = 2_890 + rng.nextInt(1200),
                reposts = 340 + rng.nextInt(200)
            )
        )

        // Fresh Culture / Away trip story
        list.add(
            XPostItem(
                id = "BRK_XP_BUS_${day}_${count}_${c3.id}",
                dayIndex = day,
                dateLabel = "8d",
                authorName = "Tribün Replikleri & Besteler",
                handle = "@tribun_replikleri",
                category = "TRİBÜN",
                isVerified = true,
                text = "Otobüsümüz Bolu Dağı virajlarında arıza yaptı, eksi 2 derecede yol kenarında meşale yakıp marş söylüyoruz 😂 ${c3.name} arması için 900 kilometre geldik, bu sevda bitmez! 🚌🎺🥁",
                likes = 9_820 + rng.nextInt(3000),
                reposts = 1_450 + rng.nextInt(600)
            )
        )

        return list
    }

    private fun generatePaginatedXTweets(state: CareerSaveState, page: Int): List<XPostItem> {
        val p = state.player
        val club = state.currentClub
        val day = state.currentDayIndex
        val seed = page * 101 + day * 47
        val rng = Random(seed)
        val leagueClubs = getLeagueClubs(state).shuffled(rng)

        val teamA = leagueClubs.getOrNull(0) ?: club
        val teamB = leagueClubs.getOrNull(1) ?: club
        val teamC = leagueClubs.getOrNull(2) ?: club
        val teamD = leagueClubs.getOrNull(3) ?: club

        val list = mutableListOf<XPostItem>()

        // 1. Tactical debate or manager quote
        list.add(
            XPostItem(
                id = "PAG_XP_${page}_COACH_${teamA.id}",
                dayIndex = day,
                dateLabel = "${page * 2}s",
                authorName = "${teamA.city} Spor Postası",
                handle = "@${teamA.city.lowercase().replace(" ", "")}_spor_ajansi",
                category = "BASIN",
                isVerified = true,
                text = "🗣️ ${teamA.managerName} basın toplantısında patladı:\n\"Kısıtlı bütçeyle dev bütçeli takımlara kafa tutuyoruz. Kimse bize şans vermiyordu ama sahada aslanlar gibi mücadele ediyoruz. Bu takımı küçümseyenler yanılacak!\" 🎙️⚽",
                likes = 3_400 + rng.nextInt(1500),
                reposts = 410 + rng.nextInt(200)
            )
        )

        // 2. Supporter Banter between Real League Clubs
        val fanHandle = "@${teamB.shortName.lowercase().replace(" ", "")}_genc"
        list.add(
            XPostItem(
                id = "PAG_XP_${page}_FAN_${teamB.id}_${teamC.id}",
                dayIndex = day,
                dateLabel = "${page * 2 + 1}s",
                authorName = "${teamB.shortName} Gençlik Grubu",
                handle = fanHandle,
                category = "TARAFTAR",
                isVerified = false,
                text = "${teamC.name} taraftarı bize 'seyirci' demiş ahaha 😂 Siz kendi sahanızda 800 kişiye oynarken biz deplasmana 3000 kişi gidiyorduk! Tribün dersi almak isteyenler gelsin!",
                likes = 2_150 + rng.nextInt(1100),
                reposts = 320 + rng.nextInt(150)
            )
        )

        // 3. Funny meme / caps
        val memeOptions = listOf(
            "TFF alt liglerinde deplasman soyunma odası duşundan sadece buz gibi su akması klasiği sksksk",
            "Maçın 85. dakikasında köşe gönderi bayrağını tutarak zaman geçirmeye çalışan kaptan çakallığı 😂",
            "İkinci yarı başlarken sahaya giren ve hakemin kovalamaktan yorulduğu sokak köpeği efsanesi sksksk 🐕⚽",
            "Yedek kulübesinde 90 dakika taktik dinleyip uzatmanın son saniyesinde 'hadi aslanım kurtar maçı' diye oyuna sokulan forvet hüznü 😭",
            "Top toplayıcı çocuğun deplasman takımına topu eliyle vermek yerine 40 metre uzağa yuvarlama profesyonelliği ahahah"
        )
        val selectedMeme = memeOptions[abs(seed) % memeOptions.size]
        list.add(
            XPostItem(
                id = "PAG_XP_${page}_MEME",
                dayIndex = day,
                dateLabel = "${page * 3}s",
                authorName = "Alt Lig Mizah & Caps",
                handle = "@altlig_caps",
                category = "MİZAH",
                isVerified = true,
                text = "$selectedMeme 🎭",
                likes = 14_200 + rng.nextInt(5000),
                reposts = 2_300 + rng.nextInt(800)
            )
        )

        // 4. League Standings / Top Scorer Race
        list.add(
            XPostItem(
                id = "PAG_XP_${page}_STANDINGS",
                dayIndex = day,
                dateLabel = "${page * 3 + 1}s",
                authorName = "2. ve 3. Lig Raporu",
                handle = "@tff2_3lig_raporu",
                category = "VERİ",
                isVerified = true,
                text = "📈 ${club.leagueName}'de zirve alev alev! Lider ile 5. takım arasında sadece 3 puan var. Kalan haftalarda evinde maç kaybetmeyen ipi göğüsler. Şampiyonluk favoriniz kim? 🏆",
                likes = 5_600 + rng.nextInt(2000),
                reposts = 640 + rng.nextInt(250)
            )
        )

        // 5. Discussion on user player or user club
        val fanClub = "@${club.shortName.lowercase().replace(" ", "")}_maraton"
        list.add(
            XPostItem(
                id = "PAG_XP_${page}_CLUB_DISCUSS_${club.id}",
                dayIndex = day,
                dateLabel = "${page * 4}s",
                authorName = "${club.shortName} Maraton",
                handle = fanClub,
                category = "TARAFTAR",
                isVerified = false,
                text = "${club.name} bu sezon savunmada çok daha derli toplu. Gençlerin hırsı ve takımın birbirine kenetlenmesi bize bu sezon şampiyonluğu getirecek! İnanın çocuklar! 🏆🔥",
                likes = 1_890 + rng.nextInt(900),
                reposts = 210 + rng.nextInt(120)
            )
        )

        return list
    }

    // =========================================================================
    // 6. INSTAGRAM DYNAMIC POSTS & COMMENTS GENERATOR
    // =========================================================================

    private fun generateBaseLeagueInstaPosts(state: CareerSaveState): List<SocialMediaPost> {
        val p = state.player
        val club = state.currentClub
        val day = state.currentDayIndex
        val leagueClubs = getLeagueClubs(state)
        val otherClubs = leagueClubs.filter { it.id != club.id }.shuffled(Random(day * 43 + 11))

        val r1 = otherClubs.getOrNull(0) ?: club
        val r2 = otherClubs.getOrNull(1) ?: club
        val r3 = otherClubs.getOrNull(2) ?: club

        val posts = mutableListOf<SocialMediaPost>()

        // 1. Other Match Highlight (Mackolik / Alt Liglerin Sesi)
        posts.add(
            SocialMediaPost(
                id = "DYN_IG_BASE_MATCH_${day}_${r1.id}",
                dayIndex = day,
                dateLabel = "2 saat önce",
                authorName = "Alt Liglerin Sesi",
                authorHandle = "@altliglerin_sesi",
                isUserPost = false,
                content = "🏁 MAÇ SONUCU | ${club.leagueName} derbisinde ${r1.name} ile ${r2.name} kozlarını paylaştı! ⚽\n\nNefes kesen 90 dakika 1-1 sona erdi. 88. dakikadaki tartışmalı ofsayt kararı tribünleri ayağa kaldırdı!\n\nSizce haftanın en iyi takımı kimdi? Yorumlara yazın! 👇🎙️",
                likesCount = 19_400,
                commentsCount = 680,
                sentiment = "Tarafsız",
                instaComments = listOf(
                    InstaCommentItem(
                        id = "IG_C_1_${day}",
                        authorHandle = "${r1.shortName.lowercase()}_sevdalisi",
                        authorName = "${r1.shortName} Sevdalısı",
                        isVerified = false,
                        avatarHex = r1.primaryColorHex,
                        text = "Hakem resmen gözümüzün önünde 3 puanımızı çaldı! VAR acilen alt liglere de gelmeli!",
                        timeLabel = "1s",
                        likesCount = 284
                    ),
                    InstaCommentItem(
                        id = "IG_C_2_${day}",
                        authorHandle = "${r2.shortName.lowercase()}_tribun",
                        authorName = "${r2.shortName} Tribün",
                        isVerified = false,
                        avatarHex = r2.primaryColorHex,
                        text = "Oynadığınız futbol ortada, hakeme sığınmayın kardeşim. Net ofsayttı!",
                        timeLabel = "50d",
                        likesCount = 192
                    ),
                    InstaCommentItem(
                        id = "IG_C_3_${day}",
                        authorHandle = "altlig_caps",
                        authorName = "Alt Lig Mizah",
                        isVerified = true,
                        avatarHex = "#EC4899",
                        text = "Hakemin ofsayt çizgisini göz kararıyla çizerken yaşadığı kafa karışıklığı sksksk",
                        timeLabel = "25d",
                        likesCount = 650
                    )
                ),
                isVerifiedAuthor = true,
                locationLabel = "${r1.stadiumName}, ${r1.city}",
                imageSceneType = "STADIUM"
            )
        )

        // 2. Stadium Choreography / Fan Atmosphere
        posts.add(
            SocialMediaPost(
                id = "DYN_IG_BASE_FAN_${day}_${r1.id}",
                dayIndex = day,
                dateLabel = "4 saat önce",
                authorName = "Tribün Replikleri & Besteler",
                authorHandle = "@tribun_replikleri",
                isUserPost = false,
                content = "Cefakar Anadolu tribünleri yine büyüledi! 🏟️🔥\n\n${r1.name} taraftarının maç öncesi açtığı dev pankart ve görsel şov tüm Türkiye'de konuşuluyor. \"Bu sevda bitmez!\" 💚🖤\n\nSiz bu koreografiye 10 üzerinden kaç verirsiniz? 👇",
                likesCount = 34_800,
                commentsCount = 1_120,
                sentiment = "Destekleyici",
                instaComments = listOf(
                    InstaCommentItem(
                        id = "IG_FAN_C1_${day}",
                        authorHandle = "anadolu_futbolu",
                        authorName = "Anadolu Futbolu",
                        isVerified = true,
                        avatarHex = "#EF4444",
                        text = "Anadolu kulüplerinin bu tutkusu olmasa Türk futbolunda heyecan kalmazdı. Tebrikler!",
                        timeLabel = "3s",
                        likesCount = 890
                    )
                ),
                isVerifiedAuthor = true,
                locationLabel = "${r1.city} Şehir Stadyumu",
                imageSceneType = "STADIUM"
            )
        )

        // 3. User Club / Player Realistic Training & Locker Room Photo
        posts.add(
            SocialMediaPost(
                id = "DYN_IG_BASE_USER_CLUB_${day}",
                dayIndex = day,
                dateLabel = "6 saat önce",
                authorName = "${club.name} Resmi Medya",
                authorHandle = "@${club.shortName.lowercase().replace(" ", "")}_resmi",
                isUserPost = false,
                content = "📸 Takımımız hafta sonu oynanacak kritik maçın hazırlıklarını bu akşam yaptığı taktik idmanıyla tamamladı! 🔥⚽\n\nOdaklanmış, hırslı ve 3 puana kilitlenmiş bir takım sahada olacak! 👊\n\n#${club.shortName} #MaçGünü #HedefŞampiyonluk",
                likesCount = 8_900,
                commentsCount = 310,
                sentiment = "Destekleyici",
                instaComments = listOf(
                    InstaCommentItem(
                        id = "IG_CLUB_C1_${day}",
                        authorHandle = "${club.shortName.lowercase()}_fan",
                        authorName = "${club.shortName} Aşıkları",
                        isVerified = false,
                        avatarHex = club.primaryColorHex,
                        text = "Genç çocuk ${p.fullName}'i mutlaka 11'de görmek istiyoruz hocam! Başarılar takım!",
                        timeLabel = "4s",
                        likesCount = 145
                    )
                ),
                isVerifiedAuthor = true,
                locationLabel = "${club.city} Tesisleri",
                imageSceneType = "TRAINING"
            )
        )

        return posts
    }

    private fun generateBreakingInstaPosts(state: CareerSaveState): List<SocialMediaPost> {
        val club = state.currentClub
        val day = state.currentDayIndex
        val seed = refreshInstaCount.value * 89 + day * 23
        val rng = Random(seed)
        val leagueClubs = getLeagueClubs(state).shuffled(rng)
        val c1 = leagueClubs.getOrNull(0) ?: club
        val c2 = leagueClubs.getOrNull(1) ?: club

        val count = refreshInstaCount.value
        val list = mutableListOf<SocialMediaPost>()

        list.add(
            SocialMediaPost(
                id = "BRK_IG_${day}_${count}_${c1.id}",
                dayIndex = day,
                dateLabel = "Az önce",
                authorName = "Alt Liglerin Sesi",
                authorHandle = "@altliglerin_sesi",
                isUserPost = false,
                content = "🚨 FLAŞ GELİŞME | ${c1.name} yönetim kurulu olağanüstü toplandı! Alınan kararla teknik heyetle yollar ayrıldı ve yeni teknik direktör arayışları başladı! ⚽📋\n\nSizce takımın başına hangi yerli hoca gelmeli? 👇",
                likesCount = 14_200 + rng.nextInt(3000),
                commentsCount = 740 + rng.nextInt(300),
                sentiment = "Eleştirel",
                instaComments = listOf(
                    InstaCommentItem(
                        id = "BRK_IG_C1_${day}_${count}",
                        authorHandle = "${c1.shortName.lowercase()}_taraftar",
                        authorName = "${c1.shortName} Tribün",
                        isVerified = false,
                        avatarHex = c1.primaryColorHex,
                        text = "Geç bile kalınmış bir karardı. Takımda ne oyun vardı ne hırs!",
                        timeLabel = "5d",
                        likesCount = 210
                    )
                ),
                isVerifiedAuthor = true,
                locationLabel = "${c1.city}",
                imageSceneType = "PRESS"
            )
        )

        return list
    }

    private fun generatePaginatedInstaPosts(state: CareerSaveState, page: Int): List<SocialMediaPost> {
        val club = state.currentClub
        val day = state.currentDayIndex
        val seed = page * 137 + day * 53
        val rng = Random(seed)
        val leagueClubs = getLeagueClubs(state).shuffled(rng)
        val c1 = leagueClubs.getOrNull(0) ?: club
        val c2 = leagueClubs.getOrNull(1) ?: club

        val list = mutableListOf<SocialMediaPost>()

        // 1. Away match journey / fans
        list.add(
            SocialMediaPost(
                id = "PAG_IG_${page}_AWAY_${c1.id}",
                dayIndex = day,
                dateLabel = "${page * 2} saat önce",
                authorName = "Anadolu Futbolu & Tribünler",
                authorHandle = "@anadolu_futbolu",
                isUserPost = false,
                content = "Deplasman otobüsünde bir gece... 🚌🌧️\n\n${c1.name} taraftarı 12 saatlik yolculuğun ardından sabahın ilk ışıklarıyla stada vardı. Futbol sadece 90 dakikadan ibaret değildir; bu sevda, bu yolculuk paha biçilemez! 🥁🔥",
                likesCount = 22_500 + rng.nextInt(6000),
                commentsCount = 820 + rng.nextInt(250),
                sentiment = "Destekleyici",
                instaComments = listOf(
                    InstaCommentItem(
                        id = "PAG_IG_C_${page}_1",
                        authorHandle = "tribun_replikleri",
                        authorName = "Tribün Replikleri",
                        isVerified = true,
                        avatarHex = "#14B8A6",
                        text = "Yol kenarında içilen o çayın lezzeti hiçbir yerde yok...",
                        timeLabel = "1s",
                        likesCount = 420
                    )
                ),
                isVerifiedAuthor = true,
                locationLabel = "${c1.city} Deplasman Yolu",
                imageSceneType = "CITY"
            )
        )

        // 2. Funny culture caps
        list.add(
            SocialMediaPost(
                id = "PAG_IG_${page}_MEME",
                dayIndex = day,
                dateLabel = "${page * 3} saat önce",
                authorName = "Alt Lig Mizah & Caps",
                authorHandle = "@altlig_caps",
                isUserPost = false,
                content = "Halı sahada maça geç kalıp ısınmadan oyuna giren ve ilk deparında arka adalesini tutan o arkadaş 😂👇 Etiketleyin de yüzleşsin sksksk",
                likesCount = 45_600 + rng.nextInt(12000),
                commentsCount = 1_850 + rng.nextInt(500),
                sentiment = "Mizah",
                instaComments = listOf(
                    InstaCommentItem(
                        id = "PAG_IG_C_${page}_2",
                        authorHandle = "hali_saha_efsaneleri",
                        authorName = "Halı Saha Efsaneleri",
                        isVerified = true,
                        avatarHex = "#06B6D4",
                        text = "Bunu yaşamayan erkek yoktur yemin ederim sksksk",
                        timeLabel = "2s",
                        likesCount = 980
                    )
                ),
                isVerifiedAuthor = true,
                locationLabel = "Halı Saha",
                imageSceneType = "LOCKER"
            )
        )

        return list
    }

    // =========================================================================
    // 7. TRENDING TOPICS (LEAGUE & TURKISH FOOTBALL DYNAMIC TRENDS)
    // =========================================================================

    fun getDynamicTrendingTopics(state: CareerSaveState): List<XTrendTopic> {
        val club = state.currentClub
        val leagueClubs = getLeagueClubs(state)
        val r1 = leagueClubs.firstOrNull { it.id != club.id } ?: club
        val r2 = leagueClubs.filter { it.id != club.id && it.id != r1.id }.firstOrNull() ?: r1

        return listOf(
            XTrendTopic(
                id = "TR_1",
                rank = 1,
                categoryLabel = "${club.leagueName} • Gündem",
                topicTitle = "#${club.shortName.uppercase()}Geliyor",
                postsCountLabel = "42,8 B Gönderi",
                liveContextSubtitle = "${club.name} taraftarı şampiyonluk yürüyüşü için kenetlendi.",
                filterKeyword = club.shortName
            ),
            XTrendTopic(
                id = "TR_2",
                rank = 2,
                categoryLabel = "Hakem Kararları • Trend",
                topicTitle = "#VARRezaleti",
                postsCountLabel = "31,4 B Gönderi",
                liveContextSubtitle = "Son haftalardaki tartışmalı kararlar sosyal medyayı ayağa kaldırdı.",
                filterKeyword = "hakem"
            ),
            XTrendTopic(
                id = "TR_3",
                rank = 3,
                categoryLabel = "Tribün Kültürü • Gündem",
                topicTitle = "#DeplasmanHakkımız",
                postsCountLabel = "25,1 B Gönderi",
                liveContextSubtitle = "${r1.shortName} taraftarlarına uygulanan deplasman yasağı tepki çekiyor.",
                filterKeyword = "deplasman"
            ),
            XTrendTopic(
                id = "TR_4",
                rank = 4,
                categoryLabel = "Türk Futbolu • Trend",
                topicTitle = "#AltLiglerinSesi",
                postsCountLabel = "18,9 B Gönderi",
                liveContextSubtitle = "Anadolu kulüplerinin yayın geliri ve saha zeminleri tartışılıyor.",
                filterKeyword = "Alt Lig"
            ),
            XTrendTopic(
                id = "TR_5",
                rank = 5,
                categoryLabel = "${club.leagueName} • Maç Günü",
                topicTitle = "#${r1.shortName}v${r2.shortName}",
                postsCountLabel = "14,3 B Gönderi",
                liveContextSubtitle = "Haftanın kritik randevusunda nefesler tutuldu.",
                filterKeyword = r1.shortName
            )
        )
    }

    fun getQuotedEmbedForPost(postId: String): XQuotedPostEmbed? = quotedPostEmbeds[postId]

    fun getXRepliesForPost(post: XPostItem, state: CareerSaveState): List<XReplyItem> {
        dynamicXReplies[post.id]?.let { return it }

        val club = state.currentClub
        val leagueClubs = getLeagueClubs(state)
        val r1 = leagueClubs.firstOrNull { it.id != club.id } ?: club
        val fanName1 = "${r1.shortName} Aşıkları"
        val fanHandle1 = "@${r1.shortName.lowercase().replace(" ", "")}_fan"

        val defaultReplies = listOf(
            XReplyItem(
                id = "X_REP_1_${post.id}",
                parentPostId = post.id,
                authorName = fanName1,
                handle = fanHandle1,
                avatarHex = r1.primaryColorHex,
                isVerified = false,
                badgeType = VerifiedBadgeType.NONE,
                timeLabel = "15d",
                text = "Sonuna kadar katılıyorum! Bu ligde kimsenin hakkı yenmesin, adalet istiyoruz!",
                likes = 42,
                reposts = 3,
                reactionType = CommentReactionType.FAN_SUPPORT
            ),
            XReplyItem(
                id = "X_REP_2_${post.id}",
                parentPostId = post.id,
                authorName = "Alt Lig Mizah",
                handle = "@altlig_caps",
                avatarHex = "#EC4899",
                isVerified = true,
                badgeType = VerifiedBadgeType.BLUE,
                timeLabel = "8d",
                text = "Hakem maçtan sonra VAR monitörünü evine götürmüş diyorlar sksksk",
                likes = 184,
                reposts = 12,
                reactionType = CommentReactionType.MEME_CULTURE
            )
        )
        dynamicXReplies[post.id] = defaultReplies
        return defaultReplies
    }

    fun getRepliesForComment(postId: String, comment: InstaCommentItem, state: CareerSaveState): List<InstaCommentReply> {
        dynamicInstaReplies[comment.id]?.let { return it }

        val replies = listOf(
            InstaCommentReply(
                id = "IG_REP_1_${comment.id}",
                parentCommentId = comment.id,
                postId = postId,
                authorHandle = "anadolu_futbolu",
                authorName = "Anadolu Futbolu",
                isVerified = true,
                avatarHex = "#EF4444",
                text = "Aynen öyle kardeşim, bu ligin çilesini çeken bilir!",
                timeLabel = "35d",
                likesCount = 28,
                reactionType = CommentReactionType.FAN_SUPPORT
            )
        )
        dynamicInstaReplies[comment.id] = replies
        return replies
    }
}
