package com.example.engine.lifestyle

import com.example.engine.social.PlayerFameTier
import com.example.model.StartingStatus
import com.squareup.moshi.JsonClass

/**
 * FOOTBALL LIFE 2.0 — SPONSORSHIP, BRAND DEALS & CLUB CRISES DOMAIN MODELS
 */

enum class SponsorshipBrand(
    val brandName: String,
    val logoSymbol: String,
    val slogan: String,
    val primaryColorHex: String,
    val accentColorHex: String,
    val prestigeScore: Int // 1..100
) {
    NIKE(
        brandName = "Nike Football",
        logoSymbol = "✔",
        slogan = "Just Do It • Awakening The Phantom",
        primaryColorHex = "#E02424",
        accentColorHex = "#111827",
        prestigeScore = 98
    ),
    ADIDAS(
        brandName = "adidas",
        logoSymbol = "///",
        slogan = "Impossible Is Nothing • Predator Instinct",
        primaryColorHex = "#F59E0B",
        accentColorHex = "#000000",
        prestigeScore = 97
    ),
    PUMA(
        brandName = "Puma Football",
        logoSymbol = "🐾",
        slogan = "Forever Faster • Play Loud",
        primaryColorHex = "#10B981",
        accentColorHex = "#1F2937",
        prestigeScore = 92
    ),
    MIZUNO(
        brandName = "Mizuno",
        logoSymbol = "鳥",
        slogan = "Made In Japan • Pure Touch & Elegance",
        primaryColorHex = "#3B82F6",
        accentColorHex = "#F3F4F6",
        prestigeScore = 86
    ),
    UNDER_ARMOUR(
        brandName = "Under Armour",
        logoSymbol = "☊",
        slogan = "Protect This House • Unstoppable Speed",
        primaryColorHex = "#6366F1",
        accentColorHex = "#18181B",
        prestigeScore = 84
    ),
    LOCAL_BOOTMAKER(
        brandName = "Anadolu Krampon Ustası",
        logoSymbol = "⚒",
        slogan = "Hakiki Deri • El Yapımı Dayanıklılık",
        primaryColorHex = "#8B5CF6",
        accentColorHex = "#374151",
        prestigeScore = 60
    )
}

enum class BootTier(val labelTr: String, val minFameTier: PlayerFameTier) {
    GRASSROOTS("Altyapı & Amatör Özel", PlayerFameTier.AMATEUR),
    CLUB_PRO("Profesyonel Lig Standart", PlayerFameTier.SEMI_PRO),
    ELITE_SERIES("Elit Seri Karbon Taban", PlayerFameTier.RISING_STAR),
    SIGNATURE_SUPERSTAR("Özel Tasarım Süperstar", PlayerFameTier.SUPER_LIG_STAR),
    ICON_BALLON_DOR("Altın Top Efsane Sürüm", PlayerFameTier.GLOBAL_SUPERSTAR)
}

@JsonClass(generateAdapter = true)
data class BootModel(
    val id: String,
    val brand: SponsorshipBrand,
    val modelName: String,
    val seriesName: String,
    val tier: BootTier,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val accentColorHex: String,
    val weightGrams: Int = 185,
    val upperMaterial: String = "Gripknit Karbon Dokuma",
    val description: String,
    val attributeBonusPace: Int = 0,
    val attributeBonusShooting: Int = 0,
    val attributeBonusPassing: Int = 0,
    val attributeBonusDribbling: Int = 0,
    val isCustomColorUnlocked: Boolean = false
) {
    val statBoostSummary: String
        get() {
            val list = mutableListOf<String>()
            if (attributeBonusPace > 0) list.add("+$attributeBonusPace Hız")
            if (attributeBonusShooting > 0) list.add("+$attributeBonusShooting Şut")
            if (attributeBonusPassing > 0) list.add("+$attributeBonusPassing Pas")
            if (attributeBonusDribbling > 0) list.add("+$attributeBonusDribbling Dribling")
            return if (list.isEmpty()) "Standart Performans" else list.joinToString(" • ")
        }
}

enum class ContractOfferStatus {
    OFFERED,
    SIGNED_ACTIVE,
    EXPIRED,
    REJECTED,
    TERMINATED
}

@JsonClass(generateAdapter = true)
data class BootContractOffer(
    val id: String,
    val brand: SponsorshipBrand,
    val bootModel: BootModel,
    val contractTitle: String,
    val weeklySponsorshipWageEur: Long,
    val goalBonusEur: Long,
    val assistBonusEur: Long,
    val cleanSheetBonusEur: Long = 0L,
    val derbyWinBonusEur: Long = 0L,
    val signingBonusEur: Long = 0L,
    val contractWeeks: Int = 38,
    val weeksRemaining: Int = 38,
    val minMatchesPerMonthRequirement: Int = 2,
    val minFameTierRequired: PlayerFameTier = PlayerFameTier.AMATEUR,
    val minFollowersRequired: Long = 500L,
    val minOverallRequired: Int = 50,
    val penaltyForBreachEur: Long = 25_000L,
    val offerStatus: ContractOfferStatus = ContractOfferStatus.OFFERED,
    val signedDayIndex: Int? = null,
    val isCustomEditionGranted: Boolean = false
)

enum class CommercialDealCategory(val labelTr: String, val iconSymbol: String) {
    BEVERAGE_ENERGY("Enerji & Sporcu İçeceği", "⚡"),
    GAMING_ESPORTS("Oyun & Konsol Elçiliği", "🎮"),
    LUXURY_WATCH("Lüks Saat & Takı", "⌚"),
    AUTOMOTIVE_SUPERCARS("Otomotiv & Prestij", "🏎️"),
    FASHION_STREETWEAR("Moda & Sokak Stili", "👔"),
    GROOMING_HAIRCARE("Kişisel Bakım & Kozmetik", "✂️"),
    FINTECH_BANKING("Dijital Finans & Bankacılık", "💳")
}

@JsonClass(generateAdapter = true)
data class CommercialDealOffer(
    val id: String,
    val brandName: String,
    val dealTitle: String,
    val category: CommercialDealCategory,
    val campaignNarrative: String,
    val brandColorHex: String,
    val upfrontAdvancePayoutEur: Long,
    val weeklyCampaignPayoutEur: Long,
    val durationWeeks: Int,
    val weeksRemaining: Int,
    val requiredFameTier: PlayerFameTier,
    val requiredFollowers: Long,
    val requiredMinOverall: Int,
    val postObligationCount: Int = 2, // Aylık sponsorluk gönderi zorunluluğu
    val followerGrowthMultiplierPct: Int = 15,
    val passiveMoraleBonus: Int = 5,
    val isSigned: Boolean = false,
    val signedDayIndex: Int? = null
)

// =========================================================================
// KULÜP KRİZLERİ & TARAFTAR TEPKİLERİ MODELLERİ
// =========================================================================

enum class CrisisType(
    val titleTr: String,
    val iconEmoji: String,
    val baseSeverity: CrisisSeverity
) {
    POOR_FORM_SLUMP(
        titleTr = "Formsuzluk & Gol Kısırlığı",
        iconEmoji = "📉",
        baseSeverity = CrisisSeverity.MEDIUM
    ),
    MISSED_DERBY_PENALTY(
        titleTr = "Derbide Kaçan Hayati Penaltı",
        iconEmoji = "💔",
        baseSeverity = CrisisSeverity.HIGH
    ),
    DERBY_HEAVY_DEFEAT(
        titleTr = "Farklı Derbi Mağlubiyeti ve İsyan",
        iconEmoji = "🔥",
        baseSeverity = CrisisSeverity.HIGH
    ),
    TRAINING_GROUND_RAID(
        titleTr = "Öfkeli Taraftarların Tesisleri Basması",
        iconEmoji = "🚨",
        baseSeverity = CrisisSeverity.CRITICAL
    ),
    SOCIAL_MEDIA_SHITSTORM(
        titleTr = "Sosyal Medyada '#KadroDışı' Linci",
        iconEmoji = "📱",
        baseSeverity = CrisisSeverity.MEDIUM
    ),
    PRESS_CONFERENCE_GRILLING(
        titleTr = "Basın Toplantısında Muhabir Kuşatması",
        iconEmoji = "🎙️",
        baseSeverity = CrisisSeverity.MEDIUM
    ),
    COACH_TACTICAL_CLASH(
        titleTr = "Teknik Direktörle Taktik Kavgası",
        iconEmoji = "⚡",
        baseSeverity = CrisisSeverity.CRITICAL
    ),
    NIGHT_OUT_SCANDAL(
        titleTr = "Maç Öncesi Gece Hayatı Sızıntısı",
        iconEmoji = "📸",
        baseSeverity = CrisisSeverity.HIGH
    ),
    RED_CARD_DERBY_SUSPENSION(
        titleTr = "Gereksiz Kırmızı Kart ve Takımı Satma Suçlaması",
        iconEmoji = "🟥",
        baseSeverity = CrisisSeverity.HIGH
    )
}

enum class CrisisSeverity(
    val labelTr: String,
    val colorHex: String,
    val defaultAngerScore: Int
) {
    LOW("Hafif Gerginlik", "#3B82F6", 25),
    MEDIUM("Ciddi Uyarı", "#F59E0B", 55),
    HIGH("Ağır Kriz & Protesto", "#EF4444", 80),
    CRITICAL("Kırmızı Alarm & Tesis Kuşatması", "#991B1B", 95)
}

enum class CrisisChoiceActionType(val labelTr: String) {
    APOLOGIZE_PUBLICLY("Kamuoyundan & Taraftardan Samimi Özür Dile"),
    HARD_COUNTER_ATTACK("Meydan Oku: 'Sahada Cevap Vereceğim, Haddinizi Bilin'"),
    DELEGATE_TO_AGENT("Menajere Devret: 'Hukuki ve Profesyonel Açıklama'"),
    CHARITY_DONATION("Taraftar Vakfına Bağış Yap & Özür Yemeği Düzenle"),
    SHUT_DOWN_SOCIAL_MEDIA("Sosyal Medyayı Kapat & Sessizliğe Gömül")
}

@JsonClass(generateAdapter = true)
data class CrisisChoiceOption(
    val id: String,
    val actionType: CrisisChoiceActionType,
    val buttonTitle: String,
    val playerSpokenQuote: String,
    val detailedDescription: String,
    val managerTrustDelta: Int,
    val fanSupportDelta: Int,
    val teamChemistryDelta: Int,
    val moraleDelta: Int,
    val energyDelta: Int = 0,
    val financialCostEur: Long = 0L,
    val followersDelta: Int = 0,
    val outOfSquadRiskPct: Int = 0, // Kadro dışı kalma riski yüzdesi
    val willProvokePress: Boolean = false,
    val outcomeSummary: String
)

@JsonClass(generateAdapter = true)
data class ActiveClubCrisis(
    val id: String,
    val crisisType: CrisisType,
    val severity: CrisisSeverity,
    val triggeredDayIndex: Int,
    val headline: String,
    val contextNarrative: String,
    val trendingHashtags: List<String>,
    val fanAngerScore: Int, // 0..100
    val coachPatienceScore: Int, // 0..100
    val choices: List<CrisisChoiceOption>,
    val isResolved: Boolean = false,
    val selectedChoiceId: String? = null,
    val resolutionHeadline: String? = null,
    val resolutionOutcomeText: String? = null,
    val resultedInBenchOrReserve: Boolean = false
)

@JsonClass(generateAdapter = true)
data class SponsorshipAndCrisisState(
    val equippedBoot: BootModel,
    val activeBootDeal: BootContractOffer?,
    val availableBootOffers: List<BootContractOffer> = emptyList(),
    val bootContractsHistory: List<BootContractOffer> = emptyList(),
    val activeCommercialDeals: List<CommercialDealOffer> = emptyList(),
    val availableCommercialOffers: List<CommercialDealOffer> = emptyList(),
    val activeCrises: List<ActiveClubCrisis> = emptyList(),
    val resolvedCrisesArchive: List<ActiveClubCrisis> = emptyList(),
    val cumulativeSponsorshipEarningsEur: Long = 0L,
    val currentFanAngerPressure: Int = 0, // 0..100
    val customBootColorHex: String? = null,
    val lastWeeklyPayoutDayIndex: Int = 0
)
