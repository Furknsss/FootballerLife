package com.example.engine.social

import com.example.model.PlayerProfile

/**
 * İlişki Aşamaları: Tanışma -> Flört -> Ciddi İlişki / Sevgililik -> Nişanlılık
 */
enum class RelationshipStage(
    val titleTr: String,
    val iconEmoji: String,
    val levelOrder: Int,
    val minAffectionForNext: Int
) {
    MEETING(
        titleTr = "Tanışma / İlk Temas",
        iconEmoji = "💬",
        levelOrder = 1,
        minAffectionForNext = 40
    ),
    FLIRTING(
        titleTr = "Flört & Buluşmalar",
        iconEmoji = "✨",
        levelOrder = 2,
        minAffectionForNext = 75
    ),
    IN_RELATIONSHIP(
        titleTr = "Ciddi İlişki & Sevgililik",
        iconEmoji = "❤️",
        levelOrder = 3,
        minAffectionForNext = 95
    ),
    ENGAGED(
        titleTr = "Nişanlılık & Evlilik Yolunda",
        iconEmoji = "💍",
        levelOrder = 4,
        minAffectionForNext = 100
    )
}

/**
 * Kız Arkadaş Arketipi & Mesleği
 */
enum class PartnerArchetype(
    val labelTr: String,
    val emoji: String,
    val defaultPrimaryHex: String,
    val defaultSecondaryHex: String,
    val defaultMinOverall: Int,
    val basePaparazziInterest: Int // 0..100
) {
    SUPERMODEL(
        labelTr = "Uluslararası Podyum Süpermodeli",
        emoji = "💃",
        defaultPrimaryHex = "#E11D48",
        defaultSecondaryHex = "#4C0519",
        defaultMinOverall = 80,
        basePaparazziInterest = 85
    ),
    ACTRESS(
        labelTr = "Dizi & Sinema Başrol Oyuncusu",
        emoji = "🎬",
        defaultPrimaryHex = "#9333EA",
        defaultSecondaryHex = "#3B0764",
        defaultMinOverall = 74,
        basePaparazziInterest = 75
    ),
    FASHION_INFLUENCER(
        labelTr = "Lüks Moda & Stil İkonu",
        emoji = "👠",
        defaultPrimaryHex = "#EC4899",
        defaultSecondaryHex = "#831843",
        defaultMinOverall = 68,
        basePaparazziInterest = 60
    ),
    SPORTS_PRESENTER(
        labelTr = "Spor Medyası & Canlı Yayın Sunucusu",
        emoji = "🎙️",
        defaultPrimaryHex = "#2563EB",
        defaultSecondaryHex = "#1E3A8A",
        defaultMinOverall = 62,
        basePaparazziInterest = 50
    ),
    MUSICIAN(
        labelTr = "Pop Sanatçısı & DJ",
        emoji = "🎵",
        defaultPrimaryHex = "#F59E0B",
        defaultSecondaryHex = "#78350F",
        defaultMinOverall = 70,
        basePaparazziInterest = 70
    ),
    FITNESS_CREATOR(
        labelTr = "Fitness & Yaşam Koçu",
        emoji = "🧘‍♀️",
        defaultPrimaryHex = "#10B981",
        defaultSecondaryHex = "#064E3B",
        defaultMinOverall = 55,
        basePaparazziInterest = 35
    )
}

/**
 * İlişki Etkileşim Kategorileri
 */
enum class DatingInteractionCategory(val titleTr: String, val iconEmoji: String) {
    DINNER("Romantik Akşam Yemeği", "🍽️"),
    GIFT("Lüks & Özel Hediye", "🎁"),
    VACATION("Rüya Gibi Tatil", "✈️"),
    MATCH_INVITE("VIP Maç Daveti", "🎟️"),
    NIGHT_OUT("Gece Hayatı & Eğlence", "🍾")
}

/**
 * Özel Paylaşılan Anlar & Hatıralar
 */
data class RomanceMoment(
    val id: String,
    val dayIndex: Int,
    val title: String,
    val description: String,
    val emoji: String,
    val location: String
)

/**
 * Sevgili / Partner Profili
 */
data class GirlfriendProfile(
    val id: String,
    val fullName: String,
    val handle: String, // e.g. "@melisvural"
    val archetype: PartnerArchetype,
    val professionTr: String,
    val age: Int,
    val city: String,
    val avatarHex: String,
    val secondaryHex: String,
    val bio: String,
    val followersCount: Long,
    val minFameTier: PlayerFameTier,
    val minCharisma: Int = 50,
    val stage: RelationshipStage = RelationshipStage.MEETING,
    val affection: Int = 25, // 0..100
    val dramaLevel: Int = 10, // 0..100
    val anniversaryDayIndex: Int? = null,
    val isActivePartner: Boolean = false,
    val isAttendingNextMatch: Boolean = false, // Tribün desteği bonusu
    val lastInteractionDayIndex: Int = 0,
    val statusQuote: String = "",
    val sharedMoments: List<RomanceMoment> = emptyList()
) {
    val isEligibleForMarriage: Boolean
        get() = stage == RelationshipStage.IN_RELATIONSHIP && affection >= 90

    val affectionPercentage: Float
        get() = (affection.coerceIn(0, 100) / 100f)

    val dramaPercentage: Float
        get() = (dramaLevel.coerceIn(0, 100) / 100f)
}

/**
 * Yapılabilecek Buluşma / Hediye / Etkileşim Seçeneği
 */
data class DatingActionItem(
    val id: String,
    val category: DatingInteractionCategory,
    val titleTr: String,
    val descriptionTr: String,
    val iconEmoji: String,
    val costEur: Long,
    val energyCost: Int,
    val minStage: RelationshipStage,
    val affectionGain: Int,
    val dramaChange: Int,
    val paparazziRiskPct: Int,
    val matchBonusMorale: Int = 0,
    val matchBonusFinishing: Int = 0
)

/**
 * Magazin / Paparazzi Olayı & Skandalı
 */
data class PaparazziDramaIncident(
    val id: String,
    val dayIndex: Int,
    val partnerName: String,
    val headline: String,
    val mediaOutlet: String, // "Snob Magazin", "Paparazzi Hattı", "Hürriyet Kelebek", "Akşam Magazin"
    val incidentType: String,
    val detailsText: String,
    val managerReaction: String,
    val fanReaction: String,
    val moralePenalty: Int,
    val formPenalty: Int,
    val fameGain: Int,
    val isResolved: Boolean = false
)

/**
 * Etkileşim Sonucu
 */
data class RomanceInteractionOutcome(
    val actionId: String,
    val actionTitle: String,
    val partnerId: String,
    val partnerName: String,
    val success: Boolean,
    val resultStoryText: String,
    val affectionChange: Int,
    val dramaChange: Int,
    val costEur: Long,
    val energyCost: Int,
    val previousStage: RelationshipStage,
    val currentStage: RelationshipStage,
    val stageAdvanced: Boolean,
    val paparazziIncident: PaparazziDramaIncident? = null,
    val matchBonusActivated: Boolean = false,
    val playerMoraleBonus: Int = 0,
    val playerFormBonus: Int = 0
)

/**
 * Instagram DM Tanışma İsteği
 */
data class RomanceDmInvitation(
    val id: String,
    val candidate: GirlfriendProfile,
    val openingMessage: String,
    val receivedDayIndex: Int,
    val isAccepted: Boolean = false,
    val isDismissed: Boolean = false
)

/**
 * Genel Sevgililik ve Magazin Sistemi Durumu
 */
data class RomanceSystemState(
    val activePartner: GirlfriendProfile? = null,
    val candidatePartners: List<GirlfriendProfile> = emptyList(),
    val inboundDmRequests: List<RomanceDmInvitation> = emptyList(),
    val pastMoments: List<RomanceMoment> = emptyList(),
    val activeDramaIncidents: List<PaparazziDramaIncident> = emptyList(),
    val isMatchBonusActive: Boolean = false,
    val matchBonusDescription: String = ""
)
