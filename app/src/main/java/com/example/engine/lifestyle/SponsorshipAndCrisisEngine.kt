package com.example.engine.lifestyle

import com.example.engine.social.PlayerFameTier
import com.example.model.Club
import com.example.model.FinancialTransaction
import com.example.model.NewsEvent
import com.example.model.PlayerProfile
import com.example.model.SocialMediaPost
import com.example.model.StartingStatus
import com.example.model.XPostItem
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * FOOTBALL LIFE 2.0 — SPONSORSHIP, BRAND DEALS & CLUB CRISES ENGINE
 * Manages boots, global advertising sponsorships, weekly bonuses, and club/fan crises.
 */
object SponsorshipAndCrisisEngine {

    // =========================================================================
    // MASTER BOOT CATALOG
    // =========================================================================

    val masterBootCatalog: List<BootModel> = listOf(
        // Grassroots & Local
        BootModel(
            id = "BOOT_LOCAL_HERO",
            brand = SponsorshipBrand.LOCAL_BOOTMAKER,
            modelName = "Anadolu Deri Klasik",
            seriesName = "Usta İşi 6 Vidalı",
            tier = BootTier.GRASSROOTS,
            primaryColorHex = "#1E293B",
            secondaryColorHex = "#F59E0B",
            accentColorHex = "#FFFFFF",
            weightGrams = 230,
            upperMaterial = "Hakiki Dana Derisi",
            description = "Bozuk alt lig zeminlerine uygun, bileği koruyan ağır ve dayanıklı yerli krampon.",
            attributeBonusPace = 0,
            attributeBonusShooting = 1,
            attributeBonusPassing = 0,
            attributeBonusDribbling = 0,
            isCustomColorUnlocked = false
        ),
        // Nike Fleet
        BootModel(
            id = "BOOT_NIKE_TIEMPO_10",
            brand = SponsorshipBrand.NIKE,
            modelName = "Tiempo Legend 10 Elite",
            seriesName = "FlyTouch Plus",
            tier = BootTier.CLUB_PRO,
            primaryColorHex = "#0284C7",
            secondaryColorHex = "#FFFFFF",
            accentColorHex = "#0EA5E9",
            weightGrams = 195,
            upperMaterial = "FlyTouch Plus Yumuşak Sentetik",
            description = "Mükemmel top kontrolü ve milimetrik paslar için tasarlanan usta orta saha kramponu.",
            attributeBonusPace = 1,
            attributeBonusShooting = 0,
            attributeBonusPassing = 2,
            attributeBonusDribbling = 1,
            isCustomColorUnlocked = false
        ),
        BootModel(
            id = "BOOT_NIKE_PHANTOM_GX2",
            brand = SponsorshipBrand.NIKE,
            modelName = "Phantom GX 2 Elite",
            seriesName = "Gripknit Precision",
            tier = BootTier.ELITE_SERIES,
            primaryColorHex = "#E11D48",
            secondaryColorHex = "#18181B",
            accentColorHex = "#FB7185",
            weightGrams = 188,
            upperMaterial = "Devrimsel Nike Gripknit Yapışkan Doku",
            description = "Falso alan vuruşlar, sert plase şutlar ve kilit paslar için rakipsiz yüzey tutuşu.",
            attributeBonusPace = 1,
            attributeBonusShooting = 3,
            attributeBonusPassing = 2,
            attributeBonusDribbling = 1,
            isCustomColorUnlocked = true
        ),
        BootModel(
            id = "BOOT_NIKE_MERCURIAL_SUPERFLY",
            brand = SponsorshipBrand.NIKE,
            modelName = "Mercurial Superfly 10 Elite",
            seriesName = "Air Zoom Safari Signature",
            tier = BootTier.SIGNATURE_SUPERSTAR,
            primaryColorHex = "#E02424",
            secondaryColorHex = "#FACC15",
            accentColorHex = "#000000",
            weightGrams = 175,
            upperMaterial = "Atomknit & 3/4 Boy Air Zoom Karbon Taban",
            description = "Patlayıcı deparlar, saf sprint sürati ve ceza sahası çevikliğinde dünya standardı.",
            attributeBonusPace = 4,
            attributeBonusShooting = 3,
            attributeBonusPassing = 1,
            attributeBonusDribbling = 3,
            isCustomColorUnlocked = true
        ),
        // Adidas Fleet
        BootModel(
            id = "BOOT_ADI_COPA_PURE2",
            brand = SponsorshipBrand.ADIDAS,
            modelName = "Copa Pure 2 Elite",
            seriesName = "Fusionskin Touch",
            tier = BootTier.CLUB_PRO,
            primaryColorHex = "#F3F4F6",
            secondaryColorHex = "#111827",
            accentColorHex = "#EF4444",
            weightGrams = 205,
            upperMaterial = "Fusionskin Dikişsiz Kanguru Derisi Hissi",
            description = "Klasik futbol romantizmi, kadife ilk dokunuş ve oyun kurucu dengesi.",
            attributeBonusPace = 0,
            attributeBonusShooting = 1,
            attributeBonusPassing = 2,
            attributeBonusDribbling = 1,
            isCustomColorUnlocked = false
        ),
        BootModel(
            id = "BOOT_ADI_PREDATOR_24",
            brand = SponsorshipBrand.ADIDAS,
            modelName = "Predator 24 FT Elite",
            seriesName = "Fold-Over Tongue Classic",
            tier = BootTier.ELITE_SERIES,
            primaryColorHex = "#000000",
            secondaryColorHex = "#DC2626",
            accentColorHex = "#FFFFFF",
            weightGrams = 190,
            upperMaterial = "Strikeskin Kauçuk Şut Bıçakları",
            description = "Doksana giden frikikler, sert vuruşlar ve katlanabilir klasik efsanevi dil tasarımı.",
            attributeBonusPace = 1,
            attributeBonusShooting = 4,
            attributeBonusPassing = 1,
            attributeBonusDribbling = 1,
            isCustomColorUnlocked = true
        ),
        BootModel(
            id = "BOOT_ADI_F50_MESSI",
            brand = SponsorshipBrand.ADIDAS,
            modelName = "F50 Elite Triunfo Dorado",
            seriesName = "Sprintframe 360 Leo Signature",
            tier = BootTier.ICON_BALLON_DOR,
            primaryColorHex = "#EAB308",
            secondaryColorHex = "#000000",
            accentColorHex = "#FFFFFF",
            weightGrams = 168,
            upperMaterial = "Fibertouch Ultra Hafif Karbon",
            description = "Altın Top efsanesinin ayağına özel üretilen, sıfır yerçekimi hissi veren başyapıt.",
            attributeBonusPace = 4,
            attributeBonusShooting = 4,
            attributeBonusPassing = 3,
            attributeBonusDribbling = 4,
            isCustomColorUnlocked = true
        ),
        // Puma Fleet
        BootModel(
            id = "BOOT_PUMA_FUTURE_7",
            brand = SponsorshipBrand.PUMA,
            modelName = "Future 7 Ultimate",
            seriesName = "Fuzionfit360 Dynamic",
            tier = BootTier.ELITE_SERIES,
            primaryColorHex = "#10B981",
            secondaryColorHex = "#F43F5E",
            accentColorHex = "#111827",
            weightGrams = 182,
            upperMaterial = "Fuzionfit360 Çift Kat Esnek Örgü",
            description = "Bileksiz kilitlenme, öngörülemez çalımlar ve modern playmaker vizyonu.",
            attributeBonusPace = 2,
            attributeBonusShooting = 2,
            attributeBonusPassing = 2,
            attributeBonusDribbling = 3,
            isCustomColorUnlocked = true
        ),
        BootModel(
            id = "BOOT_PUMA_ULTRA_5",
            brand = SponsorshipBrand.PUMA,
            modelName = "Ultra 5 Carbon Ultimate",
            seriesName = "SpeedSystem Outsole",
            tier = BootTier.SIGNATURE_SUPERSTAR,
            primaryColorHex = "#84CC16",
            secondaryColorHex = "#0F172A",
            accentColorHex = "#E11D48",
            weightGrams = 165,
            upperMaterial = "ULTRAWEAVE Karbon Fiber Dokuma",
            description = "F1 teknolojisiyle tasarlanmış dünyanın en hafif hız kramponlarından biri.",
            attributeBonusPace = 4,
            attributeBonusShooting = 2,
            attributeBonusPassing = 1,
            attributeBonusDribbling = 2,
            isCustomColorUnlocked = true
        ),
        // Mizuno Fleet
        BootModel(
            id = "BOOT_MIZUNO_MORELIA_NEO4",
            brand = SponsorshipBrand.MIZUNO,
            modelName = "Morelia Neo IV Beta Japan",
            seriesName = "Made in Japan Handcrafted",
            tier = BootTier.ELITE_SERIES,
            primaryColorHex = "#2563EB",
            secondaryColorHex = "#F8FAFC",
            accentColorHex = "#D97706",
            weightGrams = 178,
            upperMaterial = "Japonya'da El İşi Hakiki Deri & Örgü",
            description = "Futbol gurmelerinin tercihi, çıplak ayak hissi ve eşsiz top temas hissiyatı.",
            attributeBonusPace = 2,
            attributeBonusShooting = 2,
            attributeBonusPassing = 3,
            attributeBonusDribbling = 2,
            isCustomColorUnlocked = false
        )
    )

    // =========================================================================
    // MASTER COMMERCIAL ADVERTISING DEALS CATALOG
    // =========================================================================

    val masterCommercialCatalog: List<CommercialDealOffer> = listOf(
        CommercialDealOffer(
            id = "COMM_PAPARA_YILDIZ",
            brandName = "Papara",
            dealTitle = "Geleceğin Yıldızı Kampanyası",
            category = CommercialDealCategory.FINTECH_BANKING,
            campaignNarrative = "Yeni nesil dijital finans uygulamasının genç sporculara yönelik sosyal medya reklam yüzü ol.",
            brandColorHex = "#9333EA",
            upfrontAdvancePayoutEur = 4_500L,
            weeklyCampaignPayoutEur = 650L,
            durationWeeks = 26,
            weeksRemaining = 26,
            requiredFameTier = PlayerFameTier.SEMI_PRO,
            requiredFollowers = 2_000L,
            requiredMinOverall = 52,
            followerGrowthMultiplierPct = 12,
            passiveMoraleBonus = 4
        ),
        CommercialDealOffer(
            id = "COMM_GILLETTE_DERBY",
            brandName = "Gillette",
            dealTitle = "Derbi Tıraşı & Maç Günü Özgüveni",
            category = CommercialDealCategory.GROOMING_HAIRCARE,
            campaignNarrative = "Derbi maçları öncesi 'Sinekkaydı Özgüven' TV ve Instagram reklam spotunun başrolü.",
            brandColorHex = "#1D4ED8",
            upfrontAdvancePayoutEur = 15_000L,
            weeklyCampaignPayoutEur = 1_850L,
            durationWeeks = 30,
            weeksRemaining = 30,
            requiredFameTier = PlayerFameTier.RISING_STAR,
            requiredFollowers = 20_000L,
            requiredMinOverall = 64,
            followerGrowthMultiplierPct = 18,
            passiveMoraleBonus = 6
        ),
        CommercialDealOffer(
            id = "COMM_PLAYSTATION_AMBASSADOR",
            brandName = "PlayStation & EA FC",
            dealTitle = "PlayStation 5 Pro & E-Spor Elçisi",
            category = CommercialDealCategory.GAMING_ESPORTS,
            campaignNarrative = "EA FC Ultimate Team özel kapak ve lansman turnuvalarında Türkiye yüzü.",
            brandColorHex = "#003791",
            upfrontAdvancePayoutEur = 35_000L,
            weeklyCampaignPayoutEur = 3_750L,
            durationWeeks = 38,
            weeksRemaining = 38,
            requiredFameTier = PlayerFameTier.SUPER_LIG_STAR,
            requiredFollowers = 100_000L,
            requiredMinOverall = 72,
            followerGrowthMultiplierPct = 25,
            passiveMoraleBonus = 8
        ),
        CommercialDealOffer(
            id = "COMM_REDBULL_ATHLETE",
            brandName = "Red Bull",
            dealTitle = "Red Bull Global Kanatlar Ailesi",
            category = CommercialDealCategory.BEVERAGE_ENERGY,
            campaignNarrative = "Özel Red Bull kaskı/şapkasıyla ekstrem antrenman belgeseli ve küresel marka elçiliği.",
            brandColorHex = "#DB0040",
            upfrontAdvancePayoutEur = 60_000L,
            weeklyCampaignPayoutEur = 6_500L,
            durationWeeks = 52,
            weeksRemaining = 52,
            requiredFameTier = PlayerFameTier.SUPER_LIG_STAR,
            requiredFollowers = 250_000L,
            requiredMinOverall = 76,
            followerGrowthMultiplierPct = 32,
            passiveMoraleBonus = 10
        ),
        CommercialDealOffer(
            id = "COMM_HUGO_BOSS_TAILORED",
            brandName = "BOSS",
            dealTitle = "BOSS Men #BeYourOwnBOSS Global Elçisi",
            category = CommercialDealCategory.FASHION_STREETWEAR,
            campaignNarrative = "Milano Moda Haftası ve Şampiyonlar Ligi deplasman seyahatlerinde özel dikim takım elbise yüzü.",
            brandColorHex = "#18181B",
            upfrontAdvancePayoutEur = 85_000L,
            weeklyCampaignPayoutEur = 9_000L,
            durationWeeks = 40,
            weeksRemaining = 40,
            requiredFameTier = PlayerFameTier.NATIONAL_HERO,
            requiredFollowers = 600_000L,
            requiredMinOverall = 80,
            followerGrowthMultiplierPct = 40,
            passiveMoraleBonus = 12
        ),
        CommercialDealOffer(
            id = "COMM_HUBLOT_BIGBANG",
            brandName = "Hublot",
            dealTitle = "Hublot Loves Football Efsane Serisi",
            category = CommercialDealCategory.LUXURY_WATCH,
            campaignNarrative = "Dünyanın en prestijli lüks saat markasının adına özel sınırlı sayıda üreteceği kronometre serisi.",
            brandColorHex = "#0A0A0A",
            upfrontAdvancePayoutEur = 150_000L,
            weeklyCampaignPayoutEur = 16_000L,
            durationWeeks = 52,
            weeksRemaining = 52,
            requiredFameTier = PlayerFameTier.NATIONAL_HERO,
            requiredFollowers = 1_000_000L,
            requiredMinOverall = 82,
            followerGrowthMultiplierPct = 45,
            passiveMoraleBonus = 15
        ),
        CommercialDealOffer(
            id = "COMM_PORSCHE_PARTNER",
            brandName = "Porsche",
            dealTitle = "Porsche Taycan Turbo GT Küresel İkonu",
            category = CommercialDealCategory.AUTOMOTIVE_SUPERCARS,
            campaignNarrative = "Saf hız, lüks ve elektrik gücünün simgesi Porsche ile dünya çapında reklam kampanyası.",
            brandColorHex = "#C2410C",
            upfrontAdvancePayoutEur = 300_000L,
            weeklyCampaignPayoutEur = 32_000L,
            durationWeeks = 52,
            weeksRemaining = 52,
            requiredFameTier = PlayerFameTier.GLOBAL_SUPERSTAR,
            requiredFollowers = 3_000_000L,
            requiredMinOverall = 86,
            followerGrowthMultiplierPct = 60,
            passiveMoraleBonus = 20
        )
    )

    // =========================================================================
    // INITIALIZATION & OFFER GENERATION
    // =========================================================================

    fun buildInitialState(player: PlayerProfile, club: Club): SponsorshipAndCrisisState {
        val initialBoot = if (club.tier >= 5) {
            masterBootCatalog.first { it.id == "BOOT_LOCAL_HERO" }
        } else {
            masterBootCatalog.first { it.id == "BOOT_NIKE_TIEMPO_10" }
        }

        val initialOffers = generateBootOffers(player, club, currentDayIndex = 1)
        val initialCommercials = generateCommercialOffers(player, currentDayIndex = 1)

        return SponsorshipAndCrisisState(
            equippedBoot = initialBoot,
            activeBootDeal = null,
            availableBootOffers = initialOffers,
            activeCommercialDeals = emptyList(),
            availableCommercialOffers = initialCommercials,
            activeCrises = emptyList(),
            resolvedCrisesArchive = emptyList(),
            cumulativeSponsorshipEarningsEur = 0L,
            currentFanAngerPressure = 15,
            customBootColorHex = initialBoot.primaryColorHex,
            lastWeeklyPayoutDayIndex = 1
        )
    }

    fun generateBootOffers(
        player: PlayerProfile,
        club: Club,
        currentDayIndex: Int
    ): List<BootContractOffer> {
        val fameTier = calculatePlayerFameTier(player)
        val ovr = player.overallRating
        val goals = player.currentSeasonStats.goals
        val followers = player.followersCount.toLong()

        val offers = mutableListOf<BootContractOffer>()

        // 1. Nike Offer
        val nikeBoot = when {
            fameTier >= PlayerFameTier.SUPER_LIG_STAR -> masterBootCatalog.first { it.id == "BOOT_NIKE_MERCURIAL_SUPERFLY" }
            fameTier >= PlayerFameTier.RISING_STAR -> masterBootCatalog.first { it.id == "BOOT_NIKE_PHANTOM_GX2" }
            else -> masterBootCatalog.first { it.id == "BOOT_NIKE_TIEMPO_10" }
        }
        val nikeWeekly = calculateBootWage(nikeBoot, ovr, club.tier, fameTier, followers)
        val nikeGoalBonus = calculateGoalBonus(nikeWeekly, club.tier)
        offers.add(
            BootContractOffer(
                id = "OFFER_NIKE_${currentDayIndex}_${UUID.randomUUID().toString().take(4)}",
                brand = SponsorshipBrand.NIKE,
                bootModel = nikeBoot,
                contractTitle = "${nikeBoot.modelName} Resmi Sponsorluk Sözleşmesi",
                weeklySponsorshipWageEur = nikeWeekly,
                goalBonusEur = nikeGoalBonus,
                assistBonusEur = (nikeGoalBonus * 0.55).toLong(),
                cleanSheetBonusEur = (nikeGoalBonus * 0.40).toLong(),
                derbyWinBonusEur = (nikeWeekly * 1.5).toLong(),
                signingBonusEur = (nikeWeekly * 3),
                contractWeeks = 38,
                weeksRemaining = 38,
                minMatchesPerMonthRequirement = 2,
                minFameTierRequired = nikeBoot.tier.minFameTier,
                minFollowersRequired = getMinFollowersForTier(nikeBoot.tier.minFameTier),
                minOverallRequired = max(45, ovr - 4),
                penaltyForBreachEur = nikeWeekly * 10,
                offerStatus = ContractOfferStatus.OFFERED,
                isCustomEditionGranted = fameTier >= PlayerFameTier.SUPER_LIG_STAR
            )
        )

        // 2. Adidas Offer
        val adidasBoot = when {
            fameTier >= PlayerFameTier.GLOBAL_SUPERSTAR -> masterBootCatalog.first { it.id == "BOOT_ADI_F50_MESSI" }
            fameTier >= PlayerFameTier.RISING_STAR -> masterBootCatalog.first { it.id == "BOOT_ADI_PREDATOR_24" }
            else -> masterBootCatalog.first { it.id == "BOOT_ADI_COPA_PURE2" }
        }
        val adiWeekly = (calculateBootWage(adidasBoot, ovr, club.tier, fameTier, followers) * 1.05).toLong()
        val adiGoalBonus = calculateGoalBonus(adiWeekly, club.tier)
        offers.add(
            BootContractOffer(
                id = "OFFER_ADIDAS_${currentDayIndex}_${UUID.randomUUID().toString().take(4)}",
                brand = SponsorshipBrand.ADIDAS,
                bootModel = adidasBoot,
                contractTitle = "${adidasBoot.modelName} Üç Çizgi Sponsorluğu",
                weeklySponsorshipWageEur = adiWeekly,
                goalBonusEur = adiGoalBonus,
                assistBonusEur = (adiGoalBonus * 0.60).toLong(),
                cleanSheetBonusEur = (adiGoalBonus * 0.45).toLong(),
                derbyWinBonusEur = (adiWeekly * 1.6).toLong(),
                signingBonusEur = (adiWeekly * 4),
                contractWeeks = 38,
                weeksRemaining = 38,
                minMatchesPerMonthRequirement = 2,
                minFameTierRequired = adidasBoot.tier.minFameTier,
                minFollowersRequired = getMinFollowersForTier(adidasBoot.tier.minFameTier),
                minOverallRequired = max(45, ovr - 3),
                penaltyForBreachEur = adiWeekly * 10,
                offerStatus = ContractOfferStatus.OFFERED,
                isCustomEditionGranted = fameTier >= PlayerFameTier.SUPER_LIG_STAR
            )
        )

        // 3. Puma Offer
        val pumaBoot = when {
            fameTier >= PlayerFameTier.SUPER_LIG_STAR -> masterBootCatalog.first { it.id == "BOOT_PUMA_ULTRA_5" }
            else -> masterBootCatalog.first { it.id == "BOOT_PUMA_FUTURE_7" }
        }
        val pumaWeekly = (calculateBootWage(pumaBoot, ovr, club.tier, fameTier, followers) * 0.98).toLong()
        val pumaGoalBonus = (calculateGoalBonus(pumaWeekly, club.tier) * 1.15).toLong()
        offers.add(
            BootContractOffer(
                id = "OFFER_PUMA_${currentDayIndex}_${UUID.randomUUID().toString().take(4)}",
                brand = SponsorshipBrand.PUMA,
                bootModel = pumaBoot,
                contractTitle = "${pumaBoot.modelName} Forever Faster Kontratı",
                weeklySponsorshipWageEur = pumaWeekly,
                goalBonusEur = pumaGoalBonus,
                assistBonusEur = (pumaGoalBonus * 0.50).toLong(),
                cleanSheetBonusEur = (pumaGoalBonus * 0.35).toLong(),
                derbyWinBonusEur = (pumaWeekly * 1.8).toLong(),
                signingBonusEur = (pumaWeekly * 2),
                contractWeeks = 30,
                weeksRemaining = 30,
                minMatchesPerMonthRequirement = 2,
                minFameTierRequired = pumaBoot.tier.minFameTier,
                minFollowersRequired = getMinFollowersForTier(pumaBoot.tier.minFameTier),
                minOverallRequired = max(45, ovr - 5),
                penaltyForBreachEur = pumaWeekly * 8,
                offerStatus = ContractOfferStatus.OFFERED,
                isCustomEditionGranted = fameTier >= PlayerFameTier.RISING_STAR
            )
        )

        // 4. Mizuno Offer (for technical players)
        if (fameTier >= PlayerFameTier.RISING_STAR) {
            val mizunoBoot = masterBootCatalog.first { it.id == "BOOT_MIZUNO_MORELIA_NEO4" }
            val mizunoWeekly = (calculateBootWage(mizunoBoot, ovr, club.tier, fameTier, followers) * 1.12).toLong()
            offers.add(
                BootContractOffer(
                    id = "OFFER_MIZUNO_${currentDayIndex}_${UUID.randomUUID().toString().take(4)}",
                    brand = SponsorshipBrand.MIZUNO,
                    bootModel = mizunoBoot,
                    contractTitle = "Mizuno Made in Japan Özel Sporcu Anlaşması",
                    weeklySponsorshipWageEur = mizunoWeekly,
                    goalBonusEur = (mizunoWeekly * 0.7).toLong(),
                    assistBonusEur = (mizunoWeekly * 0.9).toLong(), // Big bonus for playmaker assists
                    cleanSheetBonusEur = (mizunoWeekly * 0.5).toLong(),
                    derbyWinBonusEur = (mizunoWeekly * 2.0).toLong(),
                    signingBonusEur = (mizunoWeekly * 5),
                    contractWeeks = 45,
                    weeksRemaining = 45,
                    minMatchesPerMonthRequirement = 1,
                    minFameTierRequired = PlayerFameTier.RISING_STAR,
                    minFollowersRequired = 30_000L,
                    minOverallRequired = 68,
                    penaltyForBreachEur = mizunoWeekly * 12,
                    offerStatus = ContractOfferStatus.OFFERED,
                    isCustomEditionGranted = false
                )
            )
        }

        return offers
    }

    fun generateCommercialOffers(
        player: PlayerProfile,
        currentDayIndex: Int
    ): List<CommercialDealOffer> {
        val fameTier = calculatePlayerFameTier(player)
        val followers = player.followersCount.toLong()
        val ovr = player.overallRating

        return masterCommercialCatalog.filter { deal ->
            fameTier >= deal.requiredFameTier &&
                followers >= (deal.requiredFollowers * 0.70) &&
                ovr >= (deal.requiredMinOverall - 3)
        }
    }

    // =========================================================================
    // SIGNING & ACTIONS
    // =========================================================================

    data class BootSigningResult(
        val success: Boolean,
        val message: String,
        val updatedState: SponsorshipAndCrisisState,
        val transaction: FinancialTransaction?,
        val announcementPost: SocialMediaPost?,
        val newsEvent: NewsEvent?
    )

    fun signBootDeal(
        currentState: SponsorshipAndCrisisState,
        player: PlayerProfile,
        offerId: String,
        currentDayIndex: Int,
        dateLabel: String
    ): BootSigningResult {
        val offer = currentState.availableBootOffers.find { it.id == offerId }
            ?: return BootSigningResult(
                success = false,
                message = "Teklif bulunamadı veya süresi dolmuş.",
                updatedState = currentState,
                transaction = null,
                announcementPost = null,
                newsEvent = null
            )

        val fameTier = calculatePlayerFameTier(player)
        if (fameTier < offer.minFameTierRequired) {
            return BootSigningResult(
                success = false,
                message = "Bu sözleşme için gereken şöhret kademesine (${offer.minFameTierRequired.titleTr}) henüz ulaşmadın.",
                updatedState = currentState,
                transaction = null,
                announcementPost = null,
                newsEvent = null
            )
        }

        val signedOffer = offer.copy(
            offerStatus = ContractOfferStatus.SIGNED_ACTIVE,
            signedDayIndex = currentDayIndex,
            weeksRemaining = offer.contractWeeks
        )

        // Equips the boot automatically upon signing
        val equippedBoot = offer.bootModel

        val transaction = if (offer.signingBonusEur > 0L) {
            FinancialTransaction(
                id = "TX_BOOT_SIGN_${UUID.randomUUID().toString().take(6)}",
                dayIndex = currentDayIndex,
                dateLabel = dateLabel,
                category = "SPONSORLUK",
                amountEur = offer.signingBonusEur,
                description = "${offer.brand.brandName} İmza Parası ve Sponsorluk Avansı",
                balanceAfterEur = 0L // ViewModel updates actual balance
            )
        } else null

        val announcementPost = SocialMediaPost(
            id = "POST_BOOT_DEAL_${UUID.randomUUID().toString().take(6)}",
            dayIndex = currentDayIndex,
            dateLabel = dateLabel,
            authorName = player.fullName,
            authorHandle = "@${player.kitName.lowercase()}_official",
            isUserPost = true,
            content = "Yeni sezonda adımlarım ${offer.brand.brandName} ile daha sağlam! Sahada ${equippedBoot.modelName} giyeceğim. Güvenleri için teşekkürler! ${offer.brand.slogan} #NewBootDay #Sponsored",
            likesCount = calculateInitialLikes(fameTier),
            commentsCount = (calculateInitialLikes(fameTier) * 0.12).toInt(),
            sentiment = "Destekleyici",
            topFanComments = listOf(
                "Bu kramponlarla gol krallığı gelir 🔥",
                "Renk uyumu formayla efsane oldu!",
                "Krampon ateş ediyor bol gollü olsun reis 🚀"
            ),
            isVerifiedAuthor = true,
            locationLabel = "Sponsorluk İmza Töreni",
            imageSceneType = "TRANSFER",
            likedByUser = false,
            savedByUser = false
        )

        val newsEvent = NewsEvent(
            id = "NEWS_BOOT_${UUID.randomUUID().toString().take(6)}",
            dayIndex = currentDayIndex,
            dateLabel = dateLabel,
            category = "KULÜP",
            headline = "${player.fullName}, ${offer.brand.brandName} İle Dev Sponsorluk Anlaşmasına İmza Attı!",
            body = "Yıldız oyuncu ${player.fullName}, global spor devi ${offer.brand.brandName} ile resmi krampon sponsorluğu imzaladı. Haftalık sponsorluk primi ve özel krampon tahsisini içeren sözleşme kamuoyunda büyük yankı uyandırdı.",
            relatedClubName = player.kitName,
            sourceGameEventId = offer.id,
            fanReaction = "Destekleyici"
        )

        val remainingOffers = currentState.availableBootOffers.filter { it.id != offerId }
        val updatedHistory = currentState.bootContractsHistory + signedOffer

        val updatedState = currentState.copy(
            equippedBoot = equippedBoot,
            activeBootDeal = signedOffer,
            availableBootOffers = remainingOffers,
            bootContractsHistory = updatedHistory,
            cumulativeSponsorshipEarningsEur = currentState.cumulativeSponsorshipEarningsEur + offer.signingBonusEur,
            customBootColorHex = equippedBoot.primaryColorHex
        )

        return BootSigningResult(
            success = true,
            message = "${offer.brand.brandName} ile sözleşme imzalandı! ${equippedBoot.modelName} maç kramponun olarak belirlendi.",
            updatedState = updatedState,
            transaction = transaction,
            announcementPost = announcementPost,
            newsEvent = newsEvent
        )
    }

    data class CommercialSigningResult(
        val success: Boolean,
        val message: String,
        val updatedState: SponsorshipAndCrisisState,
        val transaction: FinancialTransaction?,
        val announcementPost: SocialMediaPost?
    )

    fun signCommercialDeal(
        currentState: SponsorshipAndCrisisState,
        player: PlayerProfile,
        dealId: String,
        currentDayIndex: Int,
        dateLabel: String
    ): CommercialSigningResult {
        val deal = currentState.availableCommercialOffers.find { it.id == dealId }
            ?: return CommercialSigningResult(
                success = false,
                message = "Reklam anlaşması teklifi bulunamadı.",
                updatedState = currentState,
                transaction = null,
                announcementPost = null
            )

        val fameTier = calculatePlayerFameTier(player)
        if (fameTier < deal.requiredFameTier) {
            return CommercialSigningResult(
                success = false,
                message = "Bu global marka anlaşması için gereken şöhret kademesine (${deal.requiredFameTier.titleTr}) henüz ulaşmadın.",
                updatedState = currentState,
                transaction = null,
                announcementPost = null
            )
        }

        val signedDeal = deal.copy(
            isSigned = true,
            signedDayIndex = currentDayIndex,
            weeksRemaining = deal.durationWeeks
        )

        val transaction = if (deal.upfrontAdvancePayoutEur > 0L) {
            FinancialTransaction(
                id = "TX_COMM_SIGN_${UUID.randomUUID().toString().take(6)}",
                dayIndex = currentDayIndex,
                dateLabel = dateLabel,
                category = "SPONSORLUK",
                amountEur = deal.upfrontAdvancePayoutEur,
                description = "${deal.brandName} Kampanya Peşinatı ve Reklam Avansı",
                balanceAfterEur = 0L
            )
        } else null

        val announcementPost = SocialMediaPost(
            id = "POST_COMM_DEAL_${UUID.randomUUID().toString().take(6)}",
            dayIndex = currentDayIndex,
            dateLabel = dateLabel,
            authorName = player.fullName,
            authorHandle = "@${player.kitName.lowercase()}_official",
            isUserPost = true,
            content = "${deal.brandName} ailesinin yeni yüzü olmaktan gurur duyuyorum! Yakında ekranlarda ve panolarda sürprizlerimiz var. Takipte kalın! #Partnership #${deal.brandName.replace(" ", "")}",
            likesCount = (calculateInitialLikes(fameTier) * 1.35).toInt(),
            commentsCount = (calculateInitialLikes(fameTier) * 0.15).toInt(),
            sentiment = "Destekleyici",
            topFanComments = listOf(
                "Süperstar doğuyor! Hak ettiğin sponsorluklar geliyor 🌟",
                "Gurur duyuyoruz seninle kaptan!",
                "Reklamı sabırsızlıkla bekliyoruz 👏"
            ),
            isVerifiedAuthor = true,
            locationLabel = "${deal.brandName} Genel Merkezi",
            imageSceneType = "TRANSFER",
            likedByUser = false,
            savedByUser = false
        )

        val remainingAvailable = currentState.availableCommercialOffers.filter { it.id != dealId }
        val updatedActive = currentState.activeCommercialDeals + signedDeal

        val updatedState = currentState.copy(
            activeCommercialDeals = updatedActive,
            availableCommercialOffers = remainingAvailable,
            cumulativeSponsorshipEarningsEur = currentState.cumulativeSponsorshipEarningsEur + deal.upfrontAdvancePayoutEur
        )

        return CommercialSigningResult(
            success = true,
            message = "${deal.brandName} reklam sözleşmesi imzalandı! €${deal.upfrontAdvancePayoutEur} hesabına yattı.",
            updatedState = updatedState,
            transaction = transaction,
            announcementPost = announcementPost
        )
    }

    fun customizeEquippedBootColor(
        currentState: SponsorshipAndCrisisState,
        newColorHex: String
    ): SponsorshipAndCrisisState {
        val updatedBoot = currentState.equippedBoot.copy(
            primaryColorHex = newColorHex
        )
        return currentState.copy(
            equippedBoot = updatedBoot,
            customBootColorHex = newColorHex
        )
    }

    fun equipBootModel(
        currentState: SponsorshipAndCrisisState,
        bootModel: BootModel
    ): SponsorshipAndCrisisState {
        return currentState.copy(
            equippedBoot = bootModel,
            customBootColorHex = bootModel.primaryColorHex
        )
    }

    // =========================================================================
    // WEEKLY SPONSORSHIP FINANCIAL SETTLEMENT
    // =========================================================================

    data class WeeklySettlementResult(
        val updatedState: SponsorshipAndCrisisState,
        val totalEarnedEur: Long,
        val transaction: FinancialTransaction?
    )

    fun processWeeklySponsorshipSettlement(
        currentState: SponsorshipAndCrisisState,
        dayIndex: Int,
        dateLabel: String,
        goalsScoredThisWeek: Int = 0,
        assistsThisWeek: Int = 0,
        derbyWonThisWeek: Boolean = false
    ): WeeklySettlementResult {
        // Only run once every 7 days
        if (dayIndex - currentState.lastWeeklyPayoutDayIndex < 7 && currentState.lastWeeklyPayoutDayIndex != 0) {
            return WeeklySettlementResult(currentState, 0L, null)
        }

        var totalEarned = 0L

        // Boot deal payout
        val activeBoot = currentState.activeBootDeal
        val bootWage = activeBoot?.weeklySponsorshipWageEur ?: 0L
        val bootGoalBonuses = (activeBoot?.goalBonusEur ?: 0L) * goalsScoredThisWeek
        val bootAssistBonuses = (activeBoot?.assistBonusEur ?: 0L) * assistsThisWeek
        val derbyBonus = if (derbyWonThisWeek) (activeBoot?.derbyWinBonusEur ?: 0L) else 0L

        val totalBootThisWeek = bootWage + bootGoalBonuses + bootAssistBonuses + derbyBonus
        totalEarned += totalBootThisWeek

        // Commercial deals payout
        val commercialTotal = currentState.activeCommercialDeals.sumOf { it.weeklyCampaignPayoutEur }
        totalEarned += commercialTotal

        // Decrement remaining weeks
        val updatedBootDeal = activeBoot?.let {
            val remaining = it.weeksRemaining - 1
            if (remaining <= 0) it.copy(weeksRemaining = 0, offerStatus = ContractOfferStatus.EXPIRED)
            else it.copy(weeksRemaining = remaining)
        }

        val updatedCommercials = currentState.activeCommercialDeals.mapNotNull { deal ->
            val remaining = deal.weeksRemaining - 1
            if (remaining <= 0) null else deal.copy(weeksRemaining = remaining)
        }

        val transaction = if (totalEarned > 0L) {
            FinancialTransaction(
                id = "TX_SPONSOR_WEEKLY_${UUID.randomUUID().toString().take(6)}",
                dayIndex = dayIndex,
                dateLabel = dateLabel,
                category = "SPONSORLUK",
                amountEur = totalEarned,
                description = "Haftalık Sponsorluk Hak Edişi (Krampon Primi & Reklam Sözleşmeleri)",
                balanceAfterEur = 0L
            )
        } else null

        val updatedState = currentState.copy(
            activeBootDeal = updatedBootDeal,
            activeCommercialDeals = updatedCommercials,
            cumulativeSponsorshipEarningsEur = currentState.cumulativeSponsorshipEarningsEur + totalEarned,
            lastWeeklyPayoutDayIndex = dayIndex
        )

        return WeeklySettlementResult(
            updatedState = updatedState,
            totalEarnedEur = totalEarned,
            transaction = transaction
        )
    }

    // =========================================================================
    // KULÜP KRİZLERİ & TARAFTAR TEPKİLERİ SİMÜLASYONU
    // =========================================================================

    /**
     * Evaluates match performance, team atmosphere and fan sentiment to trigger realistic crises.
     */
    fun evaluateAndGenerateCrisis(
        player: PlayerProfile,
        club: Club,
        managerTrust: Int,
        fanSupport: Int,
        currentDayIndex: Int,
        lastMatchRating: Double,
        isDerbyMatch: Boolean,
        teamLostMatch: Boolean,
        playerMissedPenalty: Boolean = false,
        playerReceivedRedCard: Boolean = false
    ): ActiveClubCrisis? {
        val random = Random(currentDayIndex * 31 + player.kitNumber)

        return when {
            // 1. Derby Missed Penalty Crisis
            playerMissedPenalty && isDerbyMatch -> {
                buildMissedPenaltyCrisis(player, club, currentDayIndex)
            }
            // 2. Red Card Derby Disaster
            playerReceivedRedCard && isDerbyMatch -> {
                buildRedCardCrisis(player, club, currentDayIndex)
            }
            // 3. Heavy Derby Defeat & Fans Raid Training Ground
            isDerbyMatch && teamLostMatch && lastMatchRating < 6.0 && fanSupport < 48 -> {
                buildTrainingGroundRaidCrisis(player, club, currentDayIndex)
            }
            // 4. Slump in Form & Goal Drought
            lastMatchRating < 5.8 && player.form < 50 && player.morale < 52 -> {
                buildFormSlumpCrisis(player, club, currentDayIndex)
            }
            // 5. Manager Tactical Clash in Locker Room
            managerTrust < 32 && random.nextInt(100) < 65 -> {
                buildCoachClashCrisis(player, club, currentDayIndex)
            }
            // 6. Social Media Shitstorm
            fanSupport < 30 && random.nextInt(100) < 55 -> {
                buildSocialMediaLynchCrisis(player, club, currentDayIndex)
            }
            else -> null
        }
    }

    private fun buildMissedPenaltyCrisis(
        player: PlayerProfile,
        club: Club,
        dayIndex: Int
    ): ActiveClubCrisis {
        val hashtag = "#${player.kitName}PenaltıyıNasılKaçırır"
        return ActiveClubCrisis(
            id = "CRISIS_PENALTY_$dayIndex",
            crisisType = CrisisType.MISSED_DERBY_PENALTY,
            severity = CrisisSeverity.HIGH,
            triggeredDayIndex = dayIndex,
            headline = "Derbide Kaçan Penaltı Şoku: Tribünler Ayakta, Basın Hedefe Koydu!",
            contextNarrative = "Kritik derbi maçının uzatma dakikalarında kullandığın penaltı direkten döndü veya kalecide kaldı. Maç sonrası stat çıkışında öfkeli taraftarlar otobüsü durdurdu, sosyal medyada ise binlerce tweet atıldı. Basın toplantısında muhabirler teknik direktöre 'Neden penaltıyı ona attırdınız?' diye hesap soruyor.",
            trendingHashtags = listOf(hashtag, "#DerbiFiyaskosu", "#${club.shortName}HesapVer"),
            fanAngerScore = 88,
            coachPatienceScore = 40,
            choices = listOf(
                CrisisChoiceOption(
                    id = "CHOICE_APOLOGIZE",
                    actionType = CrisisChoiceActionType.APOLOGIZE_PUBLICLY,
                    buttonTitle = "Samimi Bir Şekilde Özür Dile",
                    playerSpokenQuote = "Bu kulübün taraftarına yaşattığım acı için yüreğim kan ağlıyor. Sorumluluk tamamen benimdir. Bu hatamı şampiyonluk yolunda telafi edeceğime yemin ederim.",
                    detailedDescription = "Sosyal medyada ve kulüp televizyonunda başın öne eğik samimi bir açıklama yaparsın. Taraftarın bir kısmı yumuşar, teknik direktör sorumluluk almanı takdir eder.",
                    managerTrustDelta = +4,
                    fanSupportDelta = +6,
                    teamChemistryDelta = +2,
                    moraleDelta = -5,
                    energyDelta = 0,
                    financialCostEur = 0L,
                    followersDelta = +850,
                    outOfSquadRiskPct = 10,
                    willProvokePress = false,
                    outcomeSummary = "Özrün takdir topladı. Taraftar öfkesi yatıştı, hoca güven tazeledi."
                ),
                CrisisChoiceOption(
                    id = "CHOICE_COUNTER_ATTACK",
                    actionType = CrisisChoiceActionType.HARD_COUNTER_ATTACK,
                    buttonTitle = "Basına Sert Yanıt Ver & Meydan Oku",
                    playerSpokenQuote = "Büyük futbolcular penaltı kaçırır! Baggio da kaçırdı, Messi de. İki gün önce beni övenler bugün sırtımdan bıçaklıyor. Sahada göreceksiniz!",
                    detailedDescription = "Basın toplantısında mikrofona vurarak sert çıkış yaparsın. Manşetlere bomba gibi düşer. Taraftar ikiye bölünür, hoca disiplinsizlikten öfkelenir.",
                    managerTrustDelta = -14,
                    fanSupportDelta = -10,
                    teamChemistryDelta = -8,
                    moraleDelta = +6,
                    energyDelta = 0,
                    financialCostEur = 0L,
                    followersDelta = +4_200, // Virality
                    outOfSquadRiskPct = 55,
                    willProvokePress = true,
                    outcomeSummary = "Açıklaman fırtına kopardı! Hoca seni ilk 11'den kesme uyarısı verdi."
                ),
                CrisisChoiceOption(
                    id = "CHOICE_AGENT_DELEGATE",
                    actionType = CrisisChoiceActionType.DELEGATE_TO_AGENT,
                    buttonTitle = "Menajere Devret (Profesyonel Kalkan)",
                    playerSpokenQuote = "Konuyla ilgili resmi ve hukuki açıklama menajerlik ajansım tarafından kulüp yönetimiyle koordineli şekilde yapılacaktır.",
                    detailedDescription = "Menajerin Kemal Soylu devreye girer. Profesyonel, soğukkanlı ve tansiyonu düşüren bir basın bülteni yayımlar.",
                    managerTrustDelta = +1,
                    fanSupportDelta = -2,
                    teamChemistryDelta = +1,
                    moraleDelta = 0,
                    energyDelta = 0,
                    financialCostEur = 1_500L,
                    followersDelta = +100,
                    outOfSquadRiskPct = 5,
                    willProvokePress = false,
                    outcomeSummary = "Menajerin profesyonelce krizi soğuttu. Basın ilgisini başka yöne çevirdi."
                ),
                CrisisChoiceOption(
                    id = "CHOICE_CHARITY",
                    actionType = CrisisChoiceActionType.CHARITY_DONATION,
                    buttonTitle = "Kulüp Altyapısına Bağış Yap & Özür Yemeği",
                    playerSpokenQuote = "Bu hafta hak ettiğim tüm maç primlerimi kulübün altyapı tesislerine ve taraftar derneği burs fonuna bağışlıyorum.",
                    detailedDescription = "Cebinden harcama yaparak altyapı gençlerine krampon ve burs sağlarsın. Taraftar öfkesi yerini alkışa bırakır.",
                    managerTrustDelta = +8,
                    fanSupportDelta = +15,
                    teamChemistryDelta = +6,
                    moraleDelta = +5,
                    energyDelta = 0,
                    financialCostEur = 7_500L,
                    followersDelta = +2_500,
                    outOfSquadRiskPct = 0,
                    willProvokePress = false,
                    outcomeSummary = "Alçakgönüllü ve asil hareketin taraftarın kalbini yeniden kazandı."
                )
            )
        )
    }

    private fun buildTrainingGroundRaidCrisis(
        player: PlayerProfile,
        club: Club,
        dayIndex: Int
    ): ActiveClubCrisis {
        return ActiveClubCrisis(
            id = "CRISIS_RAID_$dayIndex",
            crisisType = CrisisType.TRAINING_GROUND_RAID,
            severity = CrisisSeverity.CRITICAL,
            triggeredDayIndex = dayIndex,
            headline = "Tesisler Kuşatıldı: Öfkeli Taraftar İdmanı Bastı, 'Formayı Çıkarın' Tezahüratları!",
            contextNarrative = "Ağır mağlubiyet sonrası 500'den fazla taraftar meşaleler ve siyah çelenklerle kulüp tesislerinin demir kapılarını zorladı. 'Ruhsuzlar', 'Bu forma kutsaldır nasip olmaz herkese' sloganları atılıyor. Güvenlik güçleri barikat kurdu. Kaptanla birlikte taraftar liderlerinin önüne çıkıp konuşman bekleniyor.",
            trendingHashtags = listOf("#TesislerBasıldı", "#${club.shortName}İsyan", "#Ruhsuzlar"),
            fanAngerScore = 95,
            coachPatienceScore = 30,
            choices = listOf(
                CrisisChoiceOption(
                    id = "CHOICE_FACE_FANS",
                    actionType = CrisisChoiceActionType.APOLOGIZE_PUBLICLY,
                    buttonTitle = "Taraftarın Önüne Çık & Dinle",
                    playerSpokenQuote = "Bağırın, hakkınızdır! Ama kaçmıyoruz, buradayız. Bu armanın büyüklüğünü biliyoruz ve bu durumu kanımızın son damlasına kadar düzelteceğiz!",
                    detailedDescription = "Korkmadan tellerin önüne gidip megafonu eline alırsın. Gözlerinin içine bakarak konuşursun. Liderlik duruşun saygı uyandırır.",
                    managerTrustDelta = +10,
                    fanSupportDelta = +12,
                    teamChemistryDelta = +8,
                    moraleDelta = -2,
                    energyDelta = -5,
                    financialCostEur = 0L,
                    followersDelta = +3_800,
                    outOfSquadRiskPct = 0,
                    willProvokePress = false,
                    outcomeSummary = "Taraftar liderleri cesaretini alkışladı. Gergin hava yerini kenetlenmeye bıraktı."
                ),
                CrisisChoiceOption(
                    id = "CHOICE_AVOID_FANS",
                    actionType = CrisisChoiceActionType.SHUT_DOWN_SOCIAL_MEDIA,
                    buttonTitle = "Arka Kapıdan Ayrıl & Sessiz Kal",
                    playerSpokenQuote = "...",
                    detailedDescription = "Tesisin arka kapısından minibüse binip uzaklaşırsın. Güvenliğini sağlarsın ancak taraftar bunu 'korkaklık ve umursamazlık' olarak yorumlar.",
                    managerTrustDelta = -8,
                    fanSupportDelta = -18,
                    teamChemistryDelta = -5,
                    moraleDelta = -8,
                    energyDelta = 0,
                    financialCostEur = 0L,
                    followersDelta = -1_200,
                    outOfSquadRiskPct = 30,
                    willProvokePress = true,
                    outcomeSummary = "Taraftar 'Sahada da kaçıyorlardı tesislerden de kaçtılar' diyerek tepkisini ikiye katladı."
                ),
                CrisisChoiceOption(
                    id = "CHOICE_HARD_STANCE",
                    actionType = CrisisChoiceActionType.HARD_COUNTER_ATTACK,
                    buttonTitle = "Güvenlik Çağır & Tesis Baskınını Kına",
                    playerSpokenQuote = "Burası spor sahası, dağ başı değil! Kimse futbolcuları tehdit edemez. Bu vandallığı kabul etmiyorum.",
                    detailedDescription = "Polisin müdahale etmesini istersin. Basına 'Baskıyla futbol oynanmaz' açıklaması yaparsın.",
                    managerTrustDelta = -15,
                    fanSupportDelta = -28,
                    teamChemistryDelta = -10,
                    moraleDelta = -12,
                    energyDelta = 0,
                    financialCostEur = 0L,
                    followersDelta = -3_500,
                    outOfSquadRiskPct = 70,
                    willProvokePress = true,
                    outcomeSummary = "Taraftar grubu seni 'İstenmeyen Adam' (Persona Non Grata) ilan etti. Kadro dışı tehlikesi kapıda!"
                )
            )
        )
    }

    private fun buildFormSlumpCrisis(
        player: PlayerProfile,
        club: Club,
        dayIndex: Int
    ): ActiveClubCrisis {
        return ActiveClubCrisis(
            id = "CRISIS_SLUMP_$dayIndex",
            crisisType = CrisisType.POOR_FORM_SLUMP,
            severity = CrisisSeverity.MEDIUM,
            triggeredDayIndex = dayIndex,
            headline = "${player.fullName} Neden Düşüşte? Gol Kısırlığı ve Yedek Kulübesi Tehlikesi",
            contextNarrative = "Son haftalarda sahada varlık gösteremiyorsun. Pas isabetin düştü, kaleye çektiğin şutlar çerçeveyi bulmuyor. Gazetelerin spor sayfalarında 'Büyük yetenek saman alevi miydi?' manşetleri atılıyor. Teknik direktör antrenman çıkışı seni yanına çağırdı.",
            trendingHashtags = listOf("#${player.kitName}YedekKalsın", "#FormsuzYıldız", "#${club.shortName}"),
            fanAngerScore = 60,
            coachPatienceScore = 50,
            choices = listOf(
                CrisisChoiceOption(
                    id = "CHOICE_EXTRA_TRAIN",
                    actionType = CrisisChoiceActionType.APOLOGIZE_PUBLICLY,
                    buttonTitle = "Tesislerde Yatıp Kalk & Ekstra Çalış",
                    playerSpokenQuote = "Hocam haklısınız, formumun zirvesinde değilim. İzin günlerimi iptal ediyorum, günde çift idmanla eski halime döneceğim.",
                    detailedDescription = "Tesislerde kalarak şut ve kondisyon antrenörüyle özel mesai yaparsın. Yorgun düşersin ama hocanın saygısını kazanırsın.",
                    managerTrustDelta = +9,
                    fanSupportDelta = +4,
                    teamChemistryDelta = +3,
                    moraleDelta = +4,
                    energyDelta = -18,
                    financialCostEur = 0L,
                    followersDelta = +400,
                    outOfSquadRiskPct = 0,
                    willProvokePress = false,
                    outcomeSummary = "Teknik direktör çalışma azmini çok beğendi ve 'Formayı savaşarak alacaksın' dedi."
                ),
                CrisisChoiceOption(
                    id = "CHOICE_BLAME_TACTICS",
                    actionType = CrisisChoiceActionType.HARD_COUNTER_ATTACK,
                    buttonTitle = "Taktiksel Dizilişi ve Arkadaşlarını Eleştir",
                    playerSpokenQuote = "Bana top gelmiyor ki! Takım defansif oynuyor, forvette yapayalnız kalıyorum. Sorun bende değil, oyun planında.",
                    detailedDescription = "Teknik direktörün yüzüne sistemin seni kısıtladığını söylersin. Soyunma odasında soğuk rüzgarlar eser.",
                    managerTrustDelta = -18,
                    fanSupportDelta = -8,
                    teamChemistryDelta = -14,
                    moraleDelta = -6,
                    energyDelta = 0,
                    financialCostEur = 0L,
                    followersDelta = +600,
                    outOfSquadRiskPct = 65,
                    willProvokePress = true,
                    outcomeSummary = "Teknik direktör küplere bindi! Bir sonraki maçta kadroya alınmama ihtimalin tavan yaptı."
                ),
                CrisisChoiceOption(
                    id = "CHOICE_SILENCE",
                    actionType = CrisisChoiceActionType.SHUT_DOWN_SOCIAL_MEDIA,
                    buttonTitle = "Sosyal Medyayı Kapat & Sessiz Kal",
                    playerSpokenQuote = "Eleştirilere kulağımı tıkadım. Sadece işime odaklanıyorum.",
                    detailedDescription = "Telefonunu kapatır, Instagram ve X bildirimlerini dondurursun. Mental olarak rahatlarsın.",
                    managerTrustDelta = +2,
                    fanSupportDelta = 0,
                    teamChemistryDelta = +1,
                    moraleDelta = +8,
                    energyDelta = +5,
                    financialCostEur = 0L,
                    followersDelta = -200,
                    outOfSquadRiskPct = 15,
                    willProvokePress = false,
                    outcomeSummary = "Zihnini temizledin. Basının ve sosyal medyanın zehirli baskısından uzaklaştın."
                )
            )
        )
    }

    private fun buildCoachClashCrisis(
        player: PlayerProfile,
        club: Club,
        dayIndex: Int
    ): ActiveClubCrisis {
        return ActiveClubCrisis(
            id = "CRISIS_COACH_$dayIndex",
            crisisType = CrisisType.COACH_TACTICAL_CLASH,
            severity = CrisisSeverity.CRITICAL,
            triggeredDayIndex = dayIndex,
            headline = "Soyunma Odasında Deprem: ${player.fullName} İle Teknik Direktör Birbirine Girdi!",
            contextNarrative = "Devre arasında oyundan alınmanın ardından soyunma odasında su şişesini tekmeledin. Teknik direktörün üzerine yürüdüğü, kaptanın araya girdiği iddia ediliyor. Kulüp başkanı acil disiplin kurulu toplantısı talep etti. Kadro dışı kalma tehlikesiyle burun burunasın.",
            trendingHashtags = listOf("#KadroDışıKalsın", "#HocanınYanındayız", "#DisiplinŞart"),
            fanAngerScore = 75,
            coachPatienceScore = 15,
            choices = listOf(
                CrisisChoiceOption(
                    id = "CHOICE_COACH_APOLOGY",
                    actionType = CrisisChoiceActionType.APOLOGIZE_PUBLICLY,
                    buttonTitle = "Hocanın Odasına Git & Erkekçe Özür Dile",
                    playerSpokenQuote = "Hocam, kazanma hırsım ve adrenalin gözümü kör etti. Tavrım tamamen yanlıştı, siz bu takımın liderisiniz. Takım arkadaşlarımın önünde de özür dilemeye hazırım.",
                    detailedDescription = "Egolarını bir kenara bırakıp teknik direktörün odasına gidersin. Samimi pişmanlığın hocanın öfkesini dindirir.",
                    managerTrustDelta = +14,
                    fanSupportDelta = +5,
                    teamChemistryDelta = +7,
                    moraleDelta = +2,
                    energyDelta = 0,
                    financialCostEur = 0L,
                    followersDelta = +300,
                    outOfSquadRiskPct = 0,
                    willProvokePress = false,
                    outcomeSummary = "Teknik direktör elini sıktı: 'Bu olgunluğu göstermen önemliydi. Şimdi sahaya çık ve cevabını ver.'"
                ),
                CrisisChoiceOption(
                    id = "CHOICE_AGENT_WAR",
                    actionType = CrisisChoiceActionType.DELEGATE_TO_AGENT,
                    buttonTitle = "Menajeri Devreye Sokup Yönetime Baskı Kur",
                    playerSpokenQuote = "Menajerim başkanla konuşacak. Ya bu hoca bana saygı duyacak ya da devre arasında transfer tekliflerini değerlendiririm.",
                    detailedDescription = "Menajerin yönetimle görüşür. Kulüp içinde güç savaşı başlar. Hoca seni bir sonraki maçta yedek bırakır.",
                    managerTrustDelta = -22,
                    fanSupportDelta = -12,
                    teamChemistryDelta = -10,
                    moraleDelta = -5,
                    energyDelta = 0,
                    financialCostEur = 2_000L,
                    followersDelta = +1_500,
                    outOfSquadRiskPct = 80,
                    willProvokePress = true,
                    outcomeSummary = "Yönetim hocanın arkasında durdu. Kadro dışı kalma cezasıyla karşı karşıyasın!"
                )
            )
        )
    }

    private fun buildSocialMediaLynchCrisis(
        player: PlayerProfile,
        club: Club,
        dayIndex: Int
    ): ActiveClubCrisis {
        val hashtag = "#${player.kitName}İstifa"
        return ActiveClubCrisis(
            id = "CRISIS_SOCIAL_$dayIndex",
            crisisType = CrisisType.SOCIAL_MEDIA_SHITSTORM,
            severity = CrisisSeverity.HIGH,
            triggeredDayIndex = dayIndex,
            headline = "X ve Instagram'da Büyük İsyan: ${hashtag} Türkiye Gündeminde 1 Numara!",
            contextNarrative = "Kötü gidişatın faturası sana kesildi. Rakip taraftarlar ve kulübünün radikal taraftar sayfaları sosyal medyada örgütlendi. Gönderilerinin altı 20 binden fazla istifa ve tepki yorumuyla doldu taştı. Menajerin acil kriz iletişimi yapman gerektiği konusunda seni arıyor.",
            trendingHashtags = listOf(hashtag, "#FormayıHakEtmiyorsun", "#GözdenDüşenYıldız"),
            fanAngerScore = 85,
            coachPatienceScore = 45,
            choices = listOf(
                CrisisChoiceOption(
                    id = "CHOICE_SOCIAL_VIDEO",
                    actionType = CrisisChoiceActionType.APOLOGIZE_PUBLICLY,
                    buttonTitle = "Duygusal Bir Video Paylaşımı Yap",
                    playerSpokenQuote = "Biliyorum çok kırgınsınız, haklısınız. Ama ben bu armayı kalbimde taşıyorum. Lütfen inancınızı kaybetmeyin, birlikte ayağa kalkacağız.",
                    detailedDescription = "Sade bir tişörtle kamera karşısına geçip içten bir video yayınlarsın. Gerçek duyguların binlerce beğeni alır, lincin yönü değişir.",
                    managerTrustDelta = +4,
                    fanSupportDelta = +14,
                    teamChemistryDelta = +4,
                    moraleDelta = +5,
                    energyDelta = 0,
                    financialCostEur = 0L,
                    followersDelta = +6_500,
                    outOfSquadRiskPct = 0,
                    willProvokePress = false,
                    outcomeSummary = "Videon viral oldu! '#Yanındayız${player.kitName}' hashtag'i trendlere girdi."
                ),
                CrisisChoiceOption(
                    id = "CHOICE_CLOSE_COMMENTS",
                    actionType = CrisisChoiceActionType.SHUT_DOWN_SOCIAL_MEDIA,
                    buttonTitle = "Yorumları Kapat & Hesapları Dondur",
                    playerSpokenQuote = "Klavye başındakilerin gürültüsüne ayıracak vaktim yok.",
                    detailedDescription = "Gönderilerini yoruma kapatırsın ve bir süre hiçbir paylaşım yapmazsın. Troller başka hedeflere yönelir.",
                    managerTrustDelta = +2,
                    fanSupportDelta = -4,
                    teamChemistryDelta = 0,
                    moraleDelta = +6,
                    energyDelta = 0,
                    financialCostEur = 0L,
                    followersDelta = -800,
                    outOfSquadRiskPct = 5,
                    willProvokePress = false,
                    outcomeSummary = "Gürültü kesildi, fırtına dinene kadar kafanı dinledin."
                ),
                CrisisChoiceOption(
                    id = "CHOICE_CLAPBACK",
                    actionType = CrisisChoiceActionType.HARD_COUNTER_ATTACK,
                    buttonTitle = "Caps Paylaş & Eleştirileri Tiye Al",
                    playerSpokenQuote = "Gülüp geçiyorum... Rüzgar kayadan ancak toz alır 😉",
                    detailedDescription = "Alaycı bir hikaye ve emoji paylaşırsın. Sosyal medyayı yangın yerine çevirirsin.",
                    managerTrustDelta = -12,
                    fanSupportDelta = -20,
                    teamChemistryDelta = -8,
                    moraleDelta = -10,
                    energyDelta = 0,
                    financialCostEur = 0L,
                    followersDelta = +8_000,
                    outOfSquadRiskPct = 40,
                    willProvokePress = true,
                    outcomeSummary = "Büyük skandal patlak verdi! Yönetim 'saygısızlık' nedeniyle para cezası kesti."
                )
            )
        )
    }

    private fun buildRedCardCrisis(
        player: PlayerProfile,
        club: Club,
        dayIndex: Int
    ): ActiveClubCrisis {
        return ActiveClubCrisis(
            id = "CRISIS_REDCARD_$dayIndex",
            crisisType = CrisisType.RED_CARD_DERBY_SUSPENSION,
            severity = CrisisSeverity.HIGH,
            triggeredDayIndex = dayIndex,
            headline = "Derbide Akıl Almaz Kırmızı Kart: Takımı 10 Kişi Bıraktı, Yönetimden Para Cezası!",
            contextNarrative = "Rakip oyuncunun provokasyonuna kapılarak gördüğün doğrudan kırmızı kart takımı 10 kişi bıraktı ve derbi kaybedildi. Maç sonu soyunma odasında kaptan seninle sert bir tartışma yaşadı. TFF Disiplin Kurulu'nun en az 2-3 maç ceza vermesi bekleniyor.",
            trendingHashtags = listOf("#İhanet", "#KırmızıKartFiyaskosu", "#${player.kitName}"),
            fanAngerScore = 90,
            coachPatienceScore = 25,
            choices = listOf(
                CrisisChoiceOption(
                    id = "CHOICE_TEAM_MEAL",
                    actionType = CrisisChoiceActionType.CHARITY_DONATION,
                    buttonTitle = "Takıma Özür Yemeği Ismarla & Cezayı Kabul Et",
                    playerSpokenQuote = "Takımı yalnız bıraktığım için hepinizden özür dilerim. Kulübün keseceği her kuruş cezayı kabul ediyorum. Bu hafta tüm takımın yemeği benden.",
                    detailedDescription = "Takımı lüks bir et restoranında yemeğe götürürsün. Kaptanla arandaki buzlar erir.",
                    managerTrustDelta = +6,
                    fanSupportDelta = +8,
                    teamChemistryDelta = +10,
                    moraleDelta = +2,
                    energyDelta = 0,
                    financialCostEur = 3_500L,
                    followersDelta = +500,
                    outOfSquadRiskPct = 10,
                    willProvokePress = false,
                    outcomeSummary = "Takım arkadaşların özrünü kabul etti. Soyunma odasında birlik sağlandı."
                ),
                CrisisChoiceOption(
                    id = "CHOICE_BLAME_REFEREE",
                    actionType = CrisisChoiceActionType.HARD_COUNTER_ATTACK,
                    buttonTitle = "Hakemi ve TFF'yi Suçla: 'Operasyon Çektiler'",
                    playerSpokenQuote = "Hakem maçı katletti! Rakip küfrettiğinde görmeyen hakem bana kart çıkardı. Bu açık bir operasyondur.",
                    detailedDescription = "Sosyal medyada hakemi hedef alırsın. TFF ekstra ceza uyarısı yayınlar, yönetim zor durumda kalır.",
                    managerTrustDelta = -10,
                    fanSupportDelta = +12, // Fans love bashing referees
                    teamChemistryDelta = -2,
                    moraleDelta = -4,
                    energyDelta = 0,
                    financialCostEur = 5_000L,
                    followersDelta = +5_500,
                    outOfSquadRiskPct = 35,
                    willProvokePress = true,
                    outcomeSummary = "Taraftar arkanda durdu ancak federasyon ve kulüp yönetimi sana ceza kesti."
                )
            )
        )
    }

    // =========================================================================
    // RESOLVE CRISIS SELECTION
    // =========================================================================

    data class CrisisResolutionResult(
        val updatedState: SponsorshipAndCrisisState,
        val choice: CrisisChoiceOption,
        val managerTrustDelta: Int,
        val fanSupportDelta: Int,
        val teamChemistryDelta: Int,
        val moraleDelta: Int,
        val energyDelta: Int,
        val financialCostEur: Long,
        val followersDelta: Int,
        val resultingStartingStatus: StartingStatus?,
        val breakingNews: NewsEvent,
        val trendingSocialPost: SocialMediaPost
    )

    fun resolveCrisis(
        currentState: SponsorshipAndCrisisState,
        player: PlayerProfile,
        crisisId: String,
        choiceId: String,
        currentDayIndex: Int,
        dateLabel: String
    ): CrisisResolutionResult? {
        val crisis = currentState.activeCrises.find { it.id == crisisId } ?: return null
        val choice = crisis.choices.find { it.id == choiceId } ?: return null

        val random = Random(currentDayIndex * 17 + choiceId.hashCode())
        val isSentToBench = (choice.outOfSquadRiskPct > 0) && (random.nextInt(100) < choice.outOfSquadRiskPct)
        val resultingStartingStatus = if (isSentToBench) {
            if (choice.outOfSquadRiskPct >= 60) StartingStatus.OUT_OF_SQUAD else StartingStatus.BENCH
        } else null

        val resolvedCrisis = crisis.copy(
            isResolved = true,
            selectedChoiceId = choiceId,
            resolutionHeadline = choice.buttonTitle,
            resolutionOutcomeText = choice.outcomeSummary,
            resultedInBenchOrReserve = isSentToBench
        )

        val updatedActiveCrises = currentState.activeCrises.filter { it.id != crisisId }
        val updatedArchive = currentState.resolvedCrisesArchive + resolvedCrisis
        val newFanPressure = max(5, currentState.currentFanAngerPressure - (choice.fanSupportDelta * 2))

        val updatedState = currentState.copy(
            activeCrises = updatedActiveCrises,
            resolvedCrisesArchive = updatedArchive,
            currentFanAngerPressure = newFanPressure
        )

        val newsEvent = NewsEvent(
            id = "NEWS_CRISIS_RES_${UUID.randomUUID().toString().take(6)}",
            dayIndex = currentDayIndex,
            dateLabel = dateLabel,
            category = "KULÜP",
            headline = "Krizde Sıcak Gelişme: ${player.fullName} Konuştu!",
            body = "${choice.playerSpokenQuote}\n\n${choice.outcomeSummary}",
            relatedClubName = player.kitName,
            sourceGameEventId = crisis.id,
            fanReaction = if (choice.fanSupportDelta >= 0) "Destekleyici" else "Eleştirel"
        )

        val socialPost = SocialMediaPost(
            id = "POST_CRISIS_${UUID.randomUUID().toString().take(6)}",
            dayIndex = currentDayIndex,
            dateLabel = dateLabel,
            authorName = "Futbol Gündemi",
            authorHandle = "@futbol_gundem",
            isUserPost = false,
            content = "SON DAKİKA: ${player.fullName}'den kriz açıklaması!\n\"${choice.playerSpokenQuote.take(120)}...\"\n${crisis.trendingHashtags.joinToString(" ")}",
            likesCount = 14_500 + random.nextInt(8_000),
            commentsCount = 1_850 + random.nextInt(1_200),
            sentiment = if (choice.fanSupportDelta >= 0) "Destekleyici" else "Eleştirel",
            topFanComments = listOf(
                if (choice.fanSupportDelta >= 0) "Adamsın, bu formaya sahip çık yeter!" else "Bu açıklamalar yakışmadı, acil toparlanmalı.",
                "Sahada gereken cevabı bekliyoruz.",
                "Teknik direktörün kararına saygı duyulmalı."
            ),
            isVerifiedAuthor = true,
            locationLabel = "Basın Odası",
            imageSceneType = "LOCKER",
            likedByUser = false,
            savedByUser = false
        )

        return CrisisResolutionResult(
            updatedState = updatedState,
            choice = choice,
            managerTrustDelta = choice.managerTrustDelta,
            fanSupportDelta = choice.fanSupportDelta,
            teamChemistryDelta = choice.teamChemistryDelta,
            moraleDelta = choice.moraleDelta,
            energyDelta = choice.energyDelta,
            financialCostEur = choice.financialCostEur,
            followersDelta = choice.followersDelta,
            resultingStartingStatus = resultingStartingStatus,
            breakingNews = newsEvent,
            trendingSocialPost = socialPost
        )
    }

    // =========================================================================
    // HELPER CALCULATIONS
    // =========================================================================

    fun calculatePlayerFameTier(player: PlayerProfile): PlayerFameTier {
        val followers = player.followersCount.toLong()
        val ovr = player.overallRating

        return when {
            followers >= 4_000_000L || ovr >= 86 -> PlayerFameTier.GLOBAL_SUPERSTAR
            followers >= 800_000L || ovr >= 80 -> PlayerFameTier.NATIONAL_HERO
            followers >= 150_000L || ovr >= 74 -> PlayerFameTier.SUPER_LIG_STAR
            followers >= 25_000L || ovr >= 66 -> PlayerFameTier.RISING_STAR
            followers >= 2_500L || ovr >= 55 -> PlayerFameTier.SEMI_PRO
            else -> PlayerFameTier.AMATEUR
        }
    }

    private fun calculateBootWage(
        boot: BootModel,
        ovr: Int,
        clubTier: Int,
        fameTier: PlayerFameTier,
        followers: Long
    ): Long {
        val baseTierWage = when (fameTier) {
            PlayerFameTier.AMATEUR -> 80L
            PlayerFameTier.SEMI_PRO -> 350L
            PlayerFameTier.RISING_STAR -> 2_200L
            PlayerFameTier.SUPER_LIG_STAR -> 8_500L
            PlayerFameTier.NATIONAL_HERO -> 28_000L
            PlayerFameTier.GLOBAL_SUPERSTAR -> 95_000L
        }
        val ovrMultiplier = (ovr / 70.0).coerceIn(0.6, 2.5)
        val prestigeBonus = boot.brand.prestigeScore / 100.0
        return (baseTierWage * ovrMultiplier * prestigeBonus).toLong()
    }

    private fun calculateGoalBonus(weeklyWage: Long, clubTier: Int): Long {
        return (weeklyWage * 0.35).coerceAtLeast(50.0).toLong()
    }

    private fun getMinFollowersForTier(tier: PlayerFameTier): Long {
        return when (tier) {
            PlayerFameTier.AMATEUR -> 100L
            PlayerFameTier.SEMI_PRO -> 2_000L
            PlayerFameTier.RISING_STAR -> 20_000L
            PlayerFameTier.SUPER_LIG_STAR -> 120_000L
            PlayerFameTier.NATIONAL_HERO -> 700_000L
            PlayerFameTier.GLOBAL_SUPERSTAR -> 3_500_000L
        }
    }

    private fun calculateInitialLikes(tier: PlayerFameTier): Int {
        return when (tier) {
            PlayerFameTier.AMATEUR -> 45
            PlayerFameTier.SEMI_PRO -> 380
            PlayerFameTier.RISING_STAR -> 2_800
            PlayerFameTier.SUPER_LIG_STAR -> 24_000
            PlayerFameTier.NATIONAL_HERO -> 110_000
            PlayerFameTier.GLOBAL_SUPERSTAR -> 650_000
        }
    }
}
