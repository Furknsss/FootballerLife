package com.example.engine.social

import com.example.R

enum class SocialAccountCategory(val labelTr: String) {
    PLAYER("Sporcu"),
    CLUB("Resmi Futbol Kulübü"),
    JOURNALIST("Spor Gazetecisi"),
    MEDIA("Spor Medyası"),
    ANALYST("Futbol Analiz & Veri"),
    MEME_PAGE("Futbol Mizah & Kültür"),
    FAN_ULTRAS("Taraftar Grubu"),
    SPONSOR("Resmi Marka & Sponsor"),
    INFLUENCER("Dijital İçerik Üreticisi"),
    AGENT("Menajerlik & Kariyer")
}

enum class VerifiedBadgeType {
    BLUE,       // Mavi Tik (Oyuncu, Gazeteci, Fenomen)
    GOLD_ORG,   // Altın Tik (Resmi Kulüp, Medya Kuruluşu, Marka)
    GRAY_GOV,   // Gri Tik (Federasyon / Resmi Kurum)
    NONE
}

enum class PlayerFameTier(
    val titleTr: String,
    val badgeSymbol: String,
    val minOverall: Int,
    val minFollowers: Long,
    val minLikes: Int,
    val maxLikes: Int,
    val minReposts: Int,
    val maxReposts: Int,
    val verifiedReactionChancePct: Int,
    val trollReactionChancePct: Int
) {
    AMATEUR(
        titleTr = "Amatör / Bölgesel Lig",
        badgeSymbol = "🌱",
        minOverall = 0,
        minFollowers = 100L,
        minLikes = 18,
        maxLikes = 95,
        minReposts = 1,
        maxReposts = 8,
        verifiedReactionChancePct = 0,
        trollReactionChancePct = 20
    ),
    SEMI_PRO(
        titleTr = "TFF 2./3. Lig Çaylağı",
        badgeSymbol = "⚡",
        minOverall = 55,
        minFollowers = 2_500L,
        minLikes = 120,
        maxLikes = 850,
        minReposts = 10,
        maxReposts = 65,
        verifiedReactionChancePct = 8,
        trollReactionChancePct = 30
    ),
    RISING_STAR(
        titleTr = "TFF 1. Lig / Genç Yetenek",
        badgeSymbol = "🌟",
        minOverall = 66,
        minFollowers = 25_000L,
        minLikes = 1_200,
        maxLikes = 6_500,
        minReposts = 90,
        maxReposts = 550,
        verifiedReactionChancePct = 25,
        trollReactionChancePct = 40
    ),
    SUPER_LIG_STAR(
        titleTr = "Trendyol Süper Lig Yıldızı",
        badgeSymbol = "🔥",
        minOverall = 74,
        minFollowers = 150_000L,
        minLikes = 12_000,
        maxLikes = 65_000,
        minReposts = 900,
        maxReposts = 5_200,
        verifiedReactionChancePct = 60,
        trollReactionChancePct = 50
    ),
    NATIONAL_HERO(
        titleTr = "A Milli Takım Yıldızı",
        badgeSymbol = "🇹🇷",
        minOverall = 80,
        minFollowers = 800_000L,
        minLikes = 65_000,
        maxLikes = 280_000,
        minReposts = 4_500,
        maxReposts = 22_000,
        verifiedReactionChancePct = 85,
        trollReactionChancePct = 45
    ),
    GLOBAL_SUPERSTAR(
        titleTr = "Dünya Çapında Süperstar",
        badgeSymbol = "👑",
        minOverall = 86,
        minFollowers = 4_000_000L,
        minLikes = 350_000,
        maxLikes = 1_850_000,
        minReposts = 25_000,
        maxReposts = 140_000,
        verifiedReactionChancePct = 98,
        trollReactionChancePct = 40
    )
}

enum class CommentReactionType(val labelTr: String, val badgeColorHex: String) {
    FAN_SUPPORT("Sadık Taraftar", "#10B981"),
    RIVAL_TROLL("Rakip Trolü", "#EF4444"),
    MEME_CULTURE("Mizah & Caps", "#F59E0B"),
    TEAMMATE("Takım Arkadaşı", "#3B82F6"),
    PRESS_CRITIC("Basın & Analiz", "#8B5CF6")
}

data class SocialAccountProfile(
    val id: String,
    val handle: String, // e.g., "@fabrizioromano"
    val displayName: String,
    val category: SocialAccountCategory,
    val isVerified: Boolean,
    val badgeType: VerifiedBadgeType = if (isVerified) VerifiedBadgeType.BLUE else VerifiedBadgeType.NONE,
    val avatarHex: String,
    val bannerHex: String = "#16181C",
    val customLogoRes: Int? = null,
    val bio: String,
    val location: String = "Türkiye",
    val website: String = "",
    val followersCountText: String,
    val followingCount: Int,
    val postsCount: Int,
    val isFollowedByDefault: Boolean = true,
    val mutualFollowersText: String = "433, ardaguler ve 18 diğer kişi takip ediyor",
    val storyHighlights: List<Pair<String, String>> = listOf(
        "⚽" to "Maç Günü",
        "🔥" to "Öne Çıkanlar",
        "🎙️" to "Basın",
        "🏆" to "Sezon"
    )
) {
    val cleanHandle: String get() = handle.removePrefix("@")
}

data class InstaCommentReply(
    val id: String,
    val parentCommentId: String,
    val postId: String,
    val authorHandle: String,
    val authorName: String,
    val isVerified: Boolean = false,
    val avatarHex: String = "#3797F0",
    val text: String,
    val timeLabel: String = "1s",
    val likesCount: Int = 4,
    val likedByUser: Boolean = false,
    val reactionType: CommentReactionType? = null
)

data class XReplyItem(
    val id: String,
    val parentPostId: String,
    val authorName: String,
    val handle: String,
    val avatarHex: String,
    val isVerified: Boolean,
    val badgeType: VerifiedBadgeType = if (isVerified) VerifiedBadgeType.BLUE else VerifiedBadgeType.NONE,
    val timeLabel: String,
    val text: String,
    val likes: Int,
    val reposts: Int = 0,
    val likedByUser: Boolean = false,
    val reactionType: CommentReactionType? = null
)

data class XQuotedPostEmbed(
    val originalPostId: String,
    val authorName: String,
    val handle: String,
    val avatarHex: String,
    val isVerified: Boolean,
    val timeLabel: String,
    val text: String
)

data class XTrendTopic(
    val id: String,
    val rank: Int,
    val categoryLabel: String,
    val topicTitle: String,
    val postsCountLabel: String,
    val liveContextSubtitle: String,
    val filterKeyword: String
)

data class SocialNotificationItem(
    val id: String,
    val platform: String, // "INSTAGRAM" or "X"
    val type: String,     // "LIKE", "COMMENT", "REPLY", "MENTION", "FOLLOW", "REPOST", "QUOTE"
    val actorName: String,
    val actorHandle: String,
    val actorAvatarHex: String,
    val actorVerified: Boolean,
    val text: String,
    val timeLabel: String,
    val targetPostId: String? = null,
    val dayIndex: Int = 1,
    val isUnread: Boolean = true
)

data class WhatsAppStatusItem(
    val id: String,
    val npcId: String,
    val authorName: String,
    val authorRole: String,
    val avatarHex: String,
    val timeLabel: String,
    val caption: String,
    val bgHex: String,
    val sceneType: String,
    val viewed: Boolean = false
)

data class InstaNoteItem(
    val id: String,
    val handle: String,
    val authorName: String,
    val avatarHex: String,
    val noteText: String,
    val isVerified: Boolean = true,
    val dmThreadId: String? = null
)

data class RumorMillEntry(
    val id: String,
    val playerName: String,
    val playerPosition: String,
    val age: Int,
    val fromClubName: String,
    val toClubName: String,
    val toClubHex: String,
    val marketValueEur: Long,
    val probabilityPct: Int,
    val sourceHandle: String,
    val isUserPlayer: Boolean = false
)
