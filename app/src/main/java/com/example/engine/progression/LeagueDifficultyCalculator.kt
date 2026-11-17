package com.example.engine.progression

import com.example.model.Club
import kotlin.math.roundToInt

/**
 * Türkiye ligleri piramidinin (Süper Lig'den Bölgesel Amatör ve Süper Amatör'e kadar)
 * gerçekçi lig zorluk, görünürlük, sertlik, maaş skalası ve gelişim tavanı hesaplayıcısı.
 */
object LeagueDifficultyCalculator {

    data class LeagueTierProfile(
        val tier: Int,
        val tierNameTr: String,
        val scoutVisibilityFactor: Double, // 1.0 (tam vitrin) -> 0.03 (gözden uzak yerel amatör)
        val physicalBrutalityFactor: Double, // Alt liglerdeki sert ikili mücadele ve hakem toleransı
        val injuryRiskMultiplier: Double, // Bozuk zemin, suni çim ve sert faul riski
        val staminaDrainMultiplier: Double, // Ağır zemin ve yetersiz kondisyon altyapısı
        val defaultFacilityQuality: Int, // Tesis kalitesi (1-5 yıldız)
        val weeklyWageMinEur: Long,
        val weeklyWageMaxEur: Long,
        val marketValueMinEur: Long,
        val marketValueMaxEur: Long,
        val maxOvrDevelopmentCeiling: Int, // Bu ligde kalınarak ulaşılabilecek maksimum OVR tavanı
        val contractBuyoutMultiplier: Double // Kulübün bonservis talep katılığı
    )

    private val tierProfiles: Map<Int, LeagueTierProfile> = mapOf(
        1 to LeagueTierProfile(
            tier = 1,
            tierNameTr = "Süper Lig",
            scoutVisibilityFactor = 1.0,
            physicalBrutalityFactor = 1.0,
            injuryRiskMultiplier = 1.0,
            staminaDrainMultiplier = 1.0,
            defaultFacilityQuality = 5,
            weeklyWageMinEur = 5_000L,
            weeklyWageMaxEur = 90_000L,
            marketValueMinEur = 2_000_000L,
            marketValueMaxEur = 60_000_000L,
            maxOvrDevelopmentCeiling = 99,
            contractBuyoutMultiplier = 1.2
        ),
        2 to LeagueTierProfile(
            tier = 2,
            tierNameTr = "1. Lig",
            scoutVisibilityFactor = 0.65,
            physicalBrutalityFactor = 1.15,
            injuryRiskMultiplier = 1.10,
            staminaDrainMultiplier = 1.05,
            defaultFacilityQuality = 4,
            weeklyWageMinEur = 1_500L,
            weeklyWageMaxEur = 7_500L,
            marketValueMinEur = 400_000L,
            marketValueMaxEur = 3_000_000L,
            maxOvrDevelopmentCeiling = 82,
            contractBuyoutMultiplier = 1.35
        ),
        3 to LeagueTierProfile(
            tier = 3,
            tierNameTr = "2. Lig (Beyaz / Kırmızı)",
            scoutVisibilityFactor = 0.32,
            physicalBrutalityFactor = 1.30,
            injuryRiskMultiplier = 1.25,
            staminaDrainMultiplier = 1.12,
            defaultFacilityQuality = 3,
            weeklyWageMinEur = 450L,
            weeklyWageMaxEur = 1_800L,
            marketValueMinEur = 120_000L,
            marketValueMaxEur = 650_000L,
            maxOvrDevelopmentCeiling = 75,
            contractBuyoutMultiplier = 1.5
        ),
        4 to LeagueTierProfile(
            tier = 4,
            tierNameTr = "3. Lig (Gruplar)",
            scoutVisibilityFactor = 0.16,
            physicalBrutalityFactor = 1.45,
            injuryRiskMultiplier = 1.38,
            staminaDrainMultiplier = 1.18,
            defaultFacilityQuality = 2,
            weeklyWageMinEur = 180L,
            weeklyWageMaxEur = 600L,
            marketValueMinEur = 40_000L,
            marketValueMaxEur = 220_000L,
            maxOvrDevelopmentCeiling = 69,
            contractBuyoutMultiplier = 1.7
        ),
        5 to LeagueTierProfile(
            tier = 5,
            tierNameTr = "Bölgesel Amatör Lig (BAL)",
            scoutVisibilityFactor = 0.07,
            physicalBrutalityFactor = 1.65,
            injuryRiskMultiplier = 1.55,
            staminaDrainMultiplier = 1.25,
            defaultFacilityQuality = 1,
            weeklyWageMinEur = 60L,
            weeklyWageMaxEur = 240L,
            marketValueMinEur = 12_000L,
            marketValueMaxEur = 65_000L,
            maxOvrDevelopmentCeiling = 63,
            contractBuyoutMultiplier = 1.9
        ),
        6 to LeagueTierProfile(
            tier = 6,
            tierNameTr = "Süper Amatör / İl Amatör",
            scoutVisibilityFactor = 0.025,
            physicalBrutalityFactor = 1.85,
            injuryRiskMultiplier = 1.75,
            staminaDrainMultiplier = 1.35,
            defaultFacilityQuality = 1,
            weeklyWageMinEur = 20L,
            weeklyWageMaxEur = 90L,
            marketValueMinEur = 4_000L,
            marketValueMaxEur = 25_000L,
            maxOvrDevelopmentCeiling = 57,
            contractBuyoutMultiplier = 2.2
        )
    )

    fun getProfileForTier(tier: Int): LeagueTierProfile {
        return tierProfiles[tier.coerceIn(1, 6)] ?: tierProfiles.getValue(1)
    }

    fun getProfileForClub(club: Club): LeagueTierProfile {
        return getProfileForTier(club.tier)
    }

    /**
     * Lig tier'ına ve hava durumuna göre antrenman ve maç sakatlık risk katsayısını hesaplar.
     * Alt liglerdeki (BAL, Süper Amatör) bozuk sahalar sakatlık riskini %50-75 artırır.
     */
    fun calculateInjuryRisk(baseRiskPct: Int, tier: Int, isMuddyOrArtificialTurf: Boolean = false): Int {
        val profile = getProfileForTier(tier)
        var risk = baseRiskPct * profile.injuryRiskMultiplier
        if (isMuddyOrArtificialTurf) {
            risk *= 1.25
        }
        return risk.roundToInt().coerceIn(1, 95)
    }

    /**
     * Alt liglerin ağır saha koşullarında harcanan enerji maliyeti.
     */
    fun calculateAdjustedEnergyCost(baseEnergyCost: Int, tier: Int): Int {
        val profile = getProfileForTier(tier)
        return (baseEnergyCost * profile.staminaDrainMultiplier).roundToInt().coerceAtLeast(1)
    }

    /**
     * Bir oyuncunun mevcut liginde kalırken ulaşabileceği maksimum OVR tavanı.
     * BAL veya 3. Lig'de oynayan bir futbolcu Süper Lig seviyesine (78+) sıçrayamaz;
     * önce üst lige transfer olarak rekabet seviyesini artırmalıdır.
     */
    fun getDevelopmentOvrCeiling(tier: Int, rawPotential: Int): Int {
        val profile = getProfileForTier(tier)
        return minOf(rawPotential, profile.maxOvrDevelopmentCeiling)
    }

    /**
     * Scout dikkat eşiği: Üst liglerin gözlemcilerinin alt ligdeki bir oyuncuyu
     * radarına alması için gereken asgari son maçlar ortalama puanı.
     * Tier 5/6'da oynayan bir oyuncu sıradan performanslarla (6.5-7.0) asla scout çekemez;
     * en az 7.4+ maç puanı serisi yakalamalı veya yaş avantajına (U21) sahip olmalıdır.
     */
    fun getScoutAttentionRatingThreshold(tier: Int, playerAge: Int): Double {
        val baseThreshold = when (tier) {
            6 -> 7.60 // Süper Amatör'den scout çekmek için olağanüstü fark yaratmak şart
            5 -> 7.45 // BAL'da en az 7.45 ortalama
            4 -> 7.25 // 3. Lig
            3 -> 7.10 // 2. Lig
            2 -> 6.95 // 1. Lig
            else -> 6.80 // Süper Lig
        }
        // Genç yetenek indirimi (U20 scoutların ilgisini daha erken çeker)
        val ageDiscount = when {
            playerAge <= 19 -> 0.20
            playerAge <= 21 -> 0.10
            playerAge <= 23 -> 0.05
            playerAge >= 28 -> -0.25 // Yaşlı alt lig oyuncusunu üst ligler ancak inanılmaz performansla radarına alır
            else -> 0.0
        }
        return (baseThreshold - ageDiscount).coerceIn(6.5, 8.5)
    }

    /**
     * Gerçekçi haftalık maaş hesaplayıcı (alt liglerin acı gerçekleri göz önünde bulundurularak).
     */
    fun computeRealisticStartingWage(tier: Int, ovr: Int): Long {
        val profile = getProfileForTier(tier)
        val minWage = profile.weeklyWageMinEur
        val maxWage = profile.weeklyWageMaxEur
        val ratio = ((ovr - 45).toDouble() / 40.0).coerceIn(0.0, 1.0)
        return (minWage + (maxWage - minWage) * ratio).toLong()
    }

    /**
     * Gerçekçi başlangıç piyasa değeri hesaplayıcı.
     */
    fun computeRealisticMarketValue(tier: Int, ovr: Int, age: Int): Long {
        val profile = getProfileForTier(tier)
        val minVal = profile.marketValueMinEur
        val maxVal = profile.marketValueMaxEur
        val ovrRatio = ((ovr - 40).toDouble() / 45.0).coerceIn(0.05, 1.0)
        val ageMultiplier = when {
            age <= 19 -> 1.40
            age <= 22 -> 1.20
            age <= 26 -> 1.00
            age <= 30 -> 0.85
            else -> 0.60
        }
        return ((minVal + (maxVal - minVal) * ovrRatio) * ageMultiplier).toLong()
            .coerceIn(minVal, maxVal * 2)
    }
}
