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
    val likedByUser: Boolean = false
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
    val likedByUser: Boolean = false
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
