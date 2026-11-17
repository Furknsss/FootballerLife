package com.example.engine.progression

import com.example.engine.TrainingDrillDefinition
import com.example.model.CareerSaveState
import com.example.model.Club
import com.example.model.CompletedMatchReport
import com.example.model.PlayerAttributes
import com.example.model.PlayerProfile
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Gerçekçi oyuncu gelişim motoru.
 * Alt liglerin kısıtlı imkanları, tesis kalitesi, yaş eğrisi, maç süresi ve
 * maç puanı ortalamasına dayalı kademeli gelişim hesaplamalarını yönetir.
 */
object RealisticProgressionEngine {

    data class ProgressionResult(
        val updatedAttributes: PlayerAttributes,
        val attributeGainedName: String?,
        val gainedAmount: Int,
        val energyCost: Int,
        val injured: Boolean,
        val injuryDays: Int,
        val feedbackSummary: String
    )

    /**
     * Yaş grubuna göre temel öğrenme / gelişim katsayısı.
     */
    fun getAgeGrowthMultiplier(age: Int): Double {
        return when {
            age <= 18 -> 1.45 // Altın gelişim çağı
            age <= 21 -> 1.30 // Yüksek gelişim potansiyeli
            age <= 24 -> 1.10 // Profesyonel olgunlaşma
            age <= 27 -> 0.90 // Fiziksel zirve, taktiksel gelişim
            age <= 30 -> 0.70 // Deneyim dönemi, fiziksel koruma
            age <= 33 -> 0.50 // Düşüşe direnç dönemi
            else -> 0.35 // Kıdemli veteran
        }
    }

    /**
     * Tesis kalitesine göre gelişim çarpanı (1 yıldız = %40, 5 yıldız = %110).
     */
    fun getFacilityGrowthMultiplier(facilityQuality: Int): Double {
        return when (facilityQuality.coerceIn(1, 5)) {
            1 -> 0.40 // BAL / Amatör: Çamurlu saha, temel ağırlıklar, fizyoterapist yok
            2 -> 0.60 // 3. Lig: Eski kondisyon aletleri, sınırlı analiz
            3 -> 0.80 // 2. Lig: Standart profesyonel tesisler
            4 -> 0.95 // 1. Lig: Modern antrenman sahası, uzman ekip
            else -> 1.15 // Süper Lig: Yüksek teknoloji, rejenerasyon havuzları, GPS analiz
        }
    }

    /**
     * Antrenman seansının sonucunu ve kademeli gelişimini çözer.
     * Ham +1 puanlar her gün dağıtılmaz; mevcut stat seviyesi, yaş, tesis ve lig tavanına
     * göre aşamalı olarak hak edilir.
     */
    fun resolveTrainingSession(
        state: CareerSaveState,
        drill: TrainingDrillDefinition,
        randomSeed: Long = System.currentTimeMillis()
    ): Pair<CareerSaveState, String> {
        val p = state.player
        val club = state.currentClub
        val tier = club.tier
        val facility = club.facilityQuality

        if (state.trainedToday) {
            return state to "Bugün zaten bir antrenman tamamladın. Kas gelişimi ve aşırı yüklenmeyi önlemek için dinlenmelisin."
        }
        if (p.injuryDaysRemaining > 0 && drill.id != "DRILL_RECOVERY") {
            return state to "Sakatlığın devam ederken (${p.injuryDaysRemaining} gün kaldı) yalnızca Rejenerasyon ve Fizyoterapi uygulayabilirsin."
        }

        val adjustedEnergyCost = LeagueDifficultyCalculator.calculateAdjustedEnergyCost(drill.energyCost, tier)
        if (p.energy < adjustedEnergyCost && drill.energyCost > 0) {
            return state to "Yetersiz enerji (%${p.energy}). Bu idman için en az %$adjustedEnergyCost kondisyon gerekiyor."
        }

        val rng = Random(randomSeed + state.currentDayIndex * 31L + p.overallRating)

        // 1. Sakatlık kontrolü (Alt liglerin sert ve bozuk zeminleri riski katlar)
        val isMuddyPitch = tier >= 5
        val adjustedInjuryRisk = LeagueDifficultyCalculator.calculateInjuryRisk(
            baseRiskPct = drill.injuryRiskPct,
            tier = tier,
            isMuddyOrArtificialTurf = isMuddyPitch
        )
        val injuryRoll = rng.nextInt(100)
        val isInjured = drill.id != "DRILL_RECOVERY" && injuryRoll < adjustedInjuryRisk

        if (isInjured) {
            val injuryDays = when {
                injuryRoll < adjustedInjuryRisk / 4 -> rng.nextInt(10, 21) // Ciddi kas yırtığı / burkulma
                else -> rng.nextInt(3, 8) // Hafif adale çekmesi
            }
            val injuredPlayer = p.copy(
                energy = max(10, p.energy - adjustedEnergyCost),
                injuryDaysRemaining = injuryDays,
                morale = max(20, p.morale - 8)
            )
            val pitchWarning = if (tier >= 5) " (${club.name} tesislerinin bozuk zemini ve çukurlar sakatlığı tetikledi!)" else ""
            return state.copy(
                updatedAtMs = System.currentTimeMillis(),
                player = injuredPlayer,
                trainedToday = true
            ) to "⚠️ İDMANDA TALİHSİZLİK! ${drill.title} sırasında adale sakatlığı yaşadın. $injuryDays gün sahalardan uzak kalacaksın.$pitchWarning"
        }

        // 2. Rejenerasyon İdmanı
        if (drill.id == "DRILL_RECOVERY") {
            val recoveryEnergyGain = when (facility) {
                1 -> rng.nextInt(12, 18) // Amatör tesis soğuk duş & hafif koşu
                2 -> rng.nextInt(15, 22)
                3 -> rng.nextInt(18, 26)
                4 -> rng.nextInt(22, 30)
                else -> rng.nextInt(26, 35) // Kriyoterapi & masaj
            }
            val recoveredPlayer = p.copy(
                energy = min(100, p.energy + recoveryEnergyGain),
                morale = min(100, p.morale + 3),
                injuryDaysRemaining = max(0, p.injuryDaysRemaining - 1)
            )
            return state.copy(
                updatedAtMs = System.currentTimeMillis(),
                player = recoveredPlayer,
                trainedToday = true
            ) to "Fizyoterapi ve aktif rejenerasyon tamamlandı. +%$recoveryEnergyGain enerji geri kazanıldı."
        }

        // 3. Gelişim hesaplaması
        val ageMult = getAgeGrowthMultiplier(p.age)
        val facilityMult = getFacilityGrowthMultiplier(facility)
        val tierCeiling = LeagueDifficultyCalculator.getDevelopmentOvrCeiling(tier, p.potential)
        val isAtOrAboveCeiling = p.overallRating >= tierCeiling

        // Hedeflenen istatistikler ve mevcut seviyeleri
        val attr = p.attributes
        val (targetAttrValue, isSecondaryEligible) = when (drill.id) {
            "DRILL_SHOOTING" -> attr.shooting to (p.age <= 25)
            "DRILL_PASSING" -> attr.passing to true
            "DRILL_DRIBBLING" -> attr.dribbling to (p.age <= 26)
            "DRILL_FIRST_TOUCH" -> attr.firstTouch to true
            "DRILL_PACE" -> attr.pace to (p.age <= 23) // 24 yaş üstü hızını idmanla zor artırır
            "DRILL_STAMINA" -> attr.stamina to true
            "DRILL_STRENGTH" -> attr.strength to true
            "DRILL_DEFENDING" -> attr.defending to true
            "DRILL_TACTICS" -> attr.tactics to true
            "DRILL_SET_PIECES" -> attr.setPieces to true
            "DRILL_GK" -> attr.goalkeeping to true
            else -> attr.shooting to false
        }

        // İstatistik ne kadar yüksekse artırmak o kadar zordur (60'tan 61'e çıkmakla 80'den 81'e çıkmak bir değildir)
        val difficultyThreshold = when {
            targetAttrValue < 55 -> 30
            targetAttrValue < 65 -> 45
            targetAttrValue < 75 -> 65
            targetAttrValue < 82 -> 80
            else -> 92
        }

        // Gelişim şansı hesaplama (Taşma engellendi, min %3 taban şans sağlandı)
        val successChance = ((100 - difficultyThreshold) * ageMult * facilityMult).roundToInt().coerceIn(3, 90)
        val successRoll = rng.nextInt(100)
        val hasGained = !isAtOrAboveCeiling && (successRoll < successChance)

        val updatedAttr = if (hasGained) {
            when (drill.id) {
                "DRILL_SHOOTING" -> attr.copy(shooting = min(99, attr.shooting + 1))
                "DRILL_PASSING" -> attr.copy(passing = min(99, attr.passing + 1))
                "DRILL_DRIBBLING" -> attr.copy(dribbling = min(99, attr.dribbling + 1))
                "DRILL_FIRST_TOUCH" -> attr.copy(firstTouch = min(99, attr.firstTouch + 1))
                "DRILL_PACE" -> attr.copy(pace = min(99, attr.pace + 1))
                "DRILL_STAMINA" -> attr.copy(stamina = min(99, attr.stamina + 1))
                "DRILL_STRENGTH" -> attr.copy(strength = min(99, attr.strength + 1))
                "DRILL_DEFENDING" -> attr.copy(defending = min(99, attr.defending + 1))
                "DRILL_TACTICS" -> attr.copy(tactics = min(99, attr.tactics + 1))
                "DRILL_SET_PIECES" -> attr.copy(setPieces = min(99, attr.setPieces + 1))
                "DRILL_GK" -> attr.copy(goalkeeping = min(99, attr.goalkeeping + 1))
                else -> attr
            }
        } else {
            attr
        }

        val updatedPlayer = p.copy(
            attributes = updatedAttr,
            energy = max(5, p.energy - adjustedEnergyCost),
            trainingCountThisWeek = p.trainingCountThisWeek + 1,
            morale = min(100, p.morale + (if (hasGained) 2 else 0))
        )

        val feedback = buildString {
            append("${drill.title} tamamlandı (-%$adjustedEnergyCost enerji). ")
            if (isAtOrAboveCeiling) {
                append("Mevcut lig seviyende (${club.leagueName}) potansiyel tavanına ($tierCeiling OVR) ulaştın. Daha fazla sıçrama yapmak için üst liglere transfer olmalısın.")
            } else if (hasGained) {
                append("İdmandaki odaklanman meyvesini verdi: ${drill.primaryAttributeName} +1 arttı! 📈")
            } else {
                append("Taktik ve fiziksel yükleme başarıyla depolandı. Sürekli antrenmanla istatistiklerin yükselecektir.")
            }
            if (facility <= 2) {
                append(" [Tesis: ${facility}★ - Düşük imkanlar gelişimi yavaşlatıyor]")
            }
        }

        return state.copy(
            updatedAtMs = System.currentTimeMillis(),
            player = updatedPlayer,
            trainedToday = true
        ) to feedback
    }

    /**
     * Maç performansı sonrasında oyuncunun tecrübe ve özellik gelişimini uygular.
     * Yüksek maç puanı (7.5+) ve 90 dakika oynamak en büyük gelişim kaynağıdır.
     */
    fun resolveMatchProgression(
        player: PlayerProfile,
        club: Club,
        report: CompletedMatchReport,
        randomSeed: Long = System.currentTimeMillis()
    ): Pair<PlayerProfile, String?> {
        if (report.userPlayedMinutes <= 0) {
            return player to null
        }

        val tier = club.tier
        val tierCeiling = LeagueDifficultyCalculator.getDevelopmentOvrCeiling(tier, player.potential)
        if (player.overallRating >= tierCeiling) {
            return player to "Lig gelişim tavanındasın ($tierCeiling OVR)."
        }

        val rng = Random(randomSeed + report.fixtureId.hashCode())
        val rating = report.userRating
        val minutes = report.userPlayedMinutes
        val attr = player.attributes

        var gainedAttrDesc: String? = null
        var newAttr = attr

        // Maç içi üstün performans ödülü
        if (rating >= 7.6 && minutes >= 60) {
            val roll = rng.nextInt(100)
            if (roll < 65) {
                // Pozisyona veya maç aksiyonuna göre özellik artışı
                if (report.userGoals >= 1 && attr.shooting < 99) {
                    newAttr = newAttr.copy(shooting = attr.shooting + 1)
                    gainedAttrDesc = "Bitiricilik (+1)"
                } else if (report.userAssists >= 1 && attr.vision < 99) {
                    newAttr = newAttr.copy(vision = attr.vision + 1)
                    gainedAttrDesc = "Oyun Görüşü (+1)"
                } else if (attr.tactics < 99) {
                    newAttr = newAttr.copy(tactics = attr.tactics + 1)
                    gainedAttrDesc = "Taktiksel Olgunluk (+1)"
                }
            }
        }

        // Maç kondisyonu ve maç tecrübesi (Form artışı)
        val formDelta = when {
            rating >= 8.0 -> 6
            rating >= 7.2 -> 3
            rating >= 6.5 -> 1
            rating <= 5.8 -> -4
            else -> 0
        }
        val newForm = (player.form + formDelta).coerceIn(20, 100)

        val updatedPlayer = player.copy(
            attributes = newAttr,
            form = newForm
        )

        return updatedPlayer to gainedAttrDesc
    }
}
