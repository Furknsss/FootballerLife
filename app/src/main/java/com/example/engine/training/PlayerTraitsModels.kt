package com.example.engine.training

import com.example.model.PlayerAttributes
import com.example.model.PlayerProfile
import com.example.model.Position
import com.squareup.moshi.JsonClass

/**
 * Football Life - Yetenek Ağacı ve Pasif Vasıflar (Traits) Sistemi
 * Oyuncunun antrenman mini oyunlarından kazandığı Yetenek Puanları (TP) ile
 * açabildiği maç içi pasif etki bonusları ve özel vasıflar.
 */

enum class TraitBranch(
    val code: String,
    val titleTr: String,
    val subtitleTr: String,
    val accentColorHex: String
) {
    ATTACKING("ATTACK", "Şut & Bitiricilik", "Gol koklama, falso ve ölümcül vuruşlar", "#EF4444"),
    PLAYMAKING("PLAYMAKING", "Pas & Vizyon", "Saha görüşü, ara pasları ve oyun kurma", "#3B82F6"),
    TECHNIQUE("TECHNIQUE", "Teknik & Çeviklik", "Bire bir çalımlar, ilk kontrol ve akrobasi", "#10B981"),
    PHYSICAL_MENTAL("PHYSICAL", "Fizik & Karakter", "Dayanıklılık, hava hakimiyeti ve liderlik", "#F59E0B")
}

@JsonClass(generateAdapter = true)
data class PlayerTrait(
    val id: String,
    val nameTr: String,
    val subtitleTr: String,
    val branch: TraitBranch,
    val tier: Int, // 1: Temel, 2: İleri, 3: Usta Seviye
    val descriptionTr: String,
    val matchEffectSummary: String,
    val pointCost: Int, // Yetenek Puanı (TP)
    val requiredAttributes: Map<String, Int>,
    val prerequisiteTraitId: String? = null,
    val iconKey: String,
    val rarityColorHex: String = "#20C773"
)

data class TraitUnlockCheckResult(
    val canUnlock: Boolean,
    val missingRequirements: List<String> = emptyList(),
    val pointsNeeded: Int = 0
)

data class TraitBonusModifiers(
    val freeKickCurveBonus: Float = 0f,        // +0.25f (%25 eğim/isabet artışı)
    val freeKickGuideLineActive: Boolean = false,
    val longShotPowerMultiplier: Float = 1.0f, // 1.20f (%20 füze şut gücü)
    val clutchBonusMultiplier: Float = 1.0f,   // Derbi/büyük maç/son 15 dk tüm istatistik çarpanı
    val aerialJumpBonus: Float = 0f,           // Kafa ve hava topu sıçrama bonusu
    val dribbleSuccessBonus: Float = 0f,       // 1v1 çalım başarı artışı
    val staminaDecayReduction: Float = 0f,     // Kondisyon erimesi düşüşü (örn: 0.30f)
    val keyPassAccuracyBonus: Float = 0f,      // Ara pası başarı bonusu
    val finishingConversionBonus: Float = 0f,  // 1v1 kaleci bitiricilik bonusu
    val poacherReboundBonus: Float = 0f,       // Dönen top ve fırsatçılık bonusu
    val sprintBurstBonus: Float = 0f,          // Açık alan patlayıcı hız bonusu
    val teamMoraleShield: Boolean = false,      // Skor gerideyken moral çöküntüsünü önleme
    val tackleSuccessBonus: Float = 0f         // Temiz top kapma bonusu
)

object PlayerTraitsCatalog {

    val TRAIT_FREE_KICK_MAESTRO = PlayerTrait(
        id = "TRAIT_FREE_KICK_MAESTRO",
        nameTr = "Frikik Cambazı",
        subtitleTr = "Baraj üstü kavisli frikik dehası",
        branch = TraitBranch.ATTACKING,
        tier = 2,
        descriptionTr = "Topa istenen falsoyu ve yüksekliği verme yeteneği. Barajın üstünden süzülen toplar tam çatala yönelir.",
        matchEffectSummary = "Frikiklerde top kavisi +%25, vuruş kılavuz çizgisi netleşir ve hedef vurma şansı yükselir.",
        pointCost = 2,
        requiredAttributes = mapOf("setPieces" to 68, "shooting" to 65),
        prerequisiteTraitId = null,
        iconKey = "TARGET_FREE_KICK",
        rarityColorHex = "#F59E0B"
    )

    val TRAIT_LONG_RANGE_ROCKET = PlayerTrait(
        id = "TRAIT_LONG_RANGE_ROCKET",
        nameTr = "Uzaktan Füze",
        subtitleTr = "30 metreden kaleci avcısı",
        branch = TraitBranch.ATTACKING,
        tier = 2,
        descriptionTr = "Ceza sahası dışından sert ve kararlı mermiler çıkarma içgüdüsü. Kaleciler parmak uçlarıyla dahi çelemez.",
        matchEffectSummary = "Ceza sahası dışından çekilen şutlarda top hızı +%20, kaleci kurtarma reaksiyonu -%15 zayıflar.",
        pointCost = 2,
        requiredAttributes = mapOf("shooting" to 72, "strength" to 64),
        prerequisiteTraitId = null,
        iconKey = "ROCKET_SHOT",
        rarityColorHex = "#EF4444"
    )

    val TRAIT_CLINICAL_FINISHER = PlayerTrait(
        id = "TRAIT_CLINICAL_FINISHER",
        nameTr = "Soğukkanlı Bitirici",
        subtitleTr = "Kaleciyle bire birde sıfır panik",
        branch = TraitBranch.ATTACKING,
        tier = 3,
        descriptionTr = "Gözü kalecinin ağırlık merkezinde olan forvet sezgisi. Ayak içi plaseyle topu filelerin en ücra köşesine bırakır.",
        matchEffectSummary = "Kaleciyle karşı karşıya pozisyonlarda gol vuruşu kalitesi +%30 artar.",
        pointCost = 3,
        requiredAttributes = mapOf("shooting" to 74, "firstTouch" to 68),
        prerequisiteTraitId = "TRAIT_LONG_RANGE_ROCKET",
        iconKey = "COLD_FINISHER",
        rarityColorHex = "#EC4899"
    )

    val TRAIT_POACHER_REFLEX = PlayerTrait(
        id = "TRAIT_POACHER_REFLEX",
        nameTr = "Pusu Kurucu (Tilki)",
        subtitleTr = "Dönen topları ve ofsayt hattını koklama",
        branch = TraitBranch.ATTACKING,
        tier = 1,
        descriptionTr = "Defansın en ufak dalgınlığını affetmeyen ceza sahası tilkisi. Dönen toplara ve seken meşin yuvarlağa herkesten önce hamle yapar.",
        matchEffectSummary = "Ofsayt tuzağını bozma şansı +%25, seken topları tamamlama refleksi +%30.",
        pointCost = 1,
        requiredAttributes = mapOf("pace" to 66, "shooting" to 64),
        prerequisiteTraitId = null,
        iconKey = "POACHER_FOX",
        rarityColorHex = "#F97316"
    )

    val TRAIT_VISIONARY_PLAYMAKER = PlayerTrait(
        id = "TRAIT_VISIONARY_PLAYMAKER",
        nameTr = "Görüş Dehası",
        subtitleTr = "Sahanın röntgenini çeken pasör",
        branch = TraitBranch.PLAYMAKING,
        tier = 2,
        descriptionTr = "Kimsenin görmediği pas koridorlarını milisaniyede fark eder ve santimetrik ara paslarıyla savunma kilitlerini açar.",
        matchEffectSummary = "Kritik ara pası başarı oranı +%25, maç anında en uygun takım arkadaşı belirginleşir.",
        pointCost = 2,
        requiredAttributes = mapOf("vision" to 70, "passing" to 68),
        prerequisiteTraitId = null,
        iconKey = "VISION_EYE",
        rarityColorHex = "#3B82F6"
    )

    val TRAIT_THREAD_THE_NEEDLE = PlayerTrait(
        id = "TRAIT_THREAD_THE_NEEDLE",
        nameTr = "İğne Deliği Pasörü",
        subtitleTr = "Üç rakip arasından milimetrik pas",
        branch = TraitBranch.PLAYMAKING,
        tier = 3,
        descriptionTr = "Daracık savunma blokları arasında meşin yuvarlağı adeta bir ip gibi geçirerek asiste dönüştürür.",
        matchEffectSummary = "Asist üretme şansı +%30, pas hatası oranı yarı yarıya düşer.",
        pointCost = 3,
        requiredAttributes = mapOf("passing" to 75, "vision" to 74),
        prerequisiteTraitId = "TRAIT_VISIONARY_PLAYMAKER",
        iconKey = "NEEDLE_PASS",
        rarityColorHex = "#8B5CF6"
    )

    val TRAIT_DRIBBLE_TRICKSTER = PlayerTrait(
        id = "TRAIT_DRIBBLE_TRICKSTER",
        nameTr = "Trikster / Çalım Ustası",
        subtitleTr = "Bire birde savunmayı pazara gönderme",
        branch = TraitBranch.TECHNIQUE,
        tier = 2,
        descriptionTr = "Topu ayağına yapıştıran bilek hareketleri ve ani ağırlık merkezi değişimleri. Rakip savunma dengesini kaybeder.",
        matchEffectSummary = "Bire bir çalım başarı oranı +%25, faul alma ve kart aldırma olasılığı yükselir.",
        pointCost = 2,
        requiredAttributes = mapOf("dribbling" to 72, "agility" to 70),
        prerequisiteTraitId = null,
        iconKey = "DRIBBLE_BOOT",
        rarityColorHex = "#10B981"
    )

    val TRAIT_FIRST_TOUCH_MAGIC = PlayerTrait(
        id = "TRAIT_FIRST_TOUCH_MAGIC",
        nameTr = "Kusursuz İlk Dokunuş",
        subtitleTr = "Göğüs, diz veya ayak üstü mıknatıs kontrol",
        branch = TraitBranch.TECHNIQUE,
        tier = 1,
        descriptionTr = "50 metreden gelen süzülen topları dahi kadife dokunuşla yere indirip hücum yönüne akıtır.",
        matchEffectSummary = "Top kontrolü kayıpları sıfıra iner, kontrol anında ekstra hızlanma sağlar.",
        pointCost = 1,
        requiredAttributes = mapOf("firstTouch" to 68, "agility" to 64),
        prerequisiteTraitId = null,
        iconKey = "TOUCH_FEATHER",
        rarityColorHex = "#06B6D4"
    )

    val TRAIT_BIG_GAME_CLUTCH = PlayerTrait(
        id = "TRAIT_BIG_GAME_CLUTCH",
        nameTr = "Büyük Maç Lideri",
        subtitleTr = "Derbi ve finallerde alev alan karakter",
        branch = TraitBranch.PHYSICAL_MENTAL,
        tier = 3,
        descriptionTr = "Baskı altında dağılmak yerine devleşen yıldız futbolcu kumaşı. Tribünlerin uğultusu enerjisine dönüşür.",
        matchEffectSummary = "Derbi maçlarında, kupa finallerinde ve 75. dk sonrası skor gerideyken tüm nitelikler +%10 çarpanla devleşir.",
        pointCost = 3,
        requiredAttributes = mapOf("tactics" to 66, "stamina" to 66),
        prerequisiteTraitId = null,
        iconKey = "CLUTCH_CROWN",
        rarityColorHex = "#F59E0B"
    )

    val TRAIT_AERIAL_DOMINANCE = PlayerTrait(
        id = "TRAIT_AERIAL_DOMINANCE",
        nameTr = "Hava Hakimi",
        subtitleTr = "Ceza sahasında havada asılı kalma",
        branch = TraitBranch.PHYSICAL_MENTAL,
        tier = 2,
        descriptionTr = "Zamanlama harikası sıçrama ve güçlü boyun vuruşları. Kornerlerde ve yan ortalarda durdurulamaz bir kule.",
        matchEffectSummary = "Kafa vuruşu isabeti +%25, duran toplarda hava topu kazanma üstünlüğü.",
        pointCost = 2,
        requiredAttributes = mapOf("strength" to 68, "tactics" to 62),
        prerequisiteTraitId = null,
        iconKey = "AERIAL_WINGS",
        rarityColorHex = "#6366F1"
    )

    val TRAIT_ENDURANCE_BEAST = PlayerTrait(
        id = "TRAIT_ENDURANCE_BEAST",
        nameTr = "Kondisyon Canavarı",
        subtitleTr = "90 dakika dinmeyen pres makinesi",
        branch = TraitBranch.PHYSICAL_MENTAL,
        tier = 1,
        descriptionTr = "Ciğerleri hiç bitmeyen tükenmez bir enerji deposu. Maçın son dakikalarında bile ilk dakikanın temposuyla depar atar.",
        matchEffectSummary = "Maç içi kondisyon (stamina) erimesi %30 yavaşlar, antrenman yorgunluğu daha hızlı temizlenir.",
        pointCost = 2,
        requiredAttributes = mapOf("stamina" to 72),
        prerequisiteTraitId = null,
        iconKey = "STAMINA_HEART",
        rarityColorHex = "#14B8A6"
    )

    val TRAIT_SPEED_DEMON = PlayerTrait(
        id = "TRAIT_SPEED_DEMON",
        nameTr = "Rüzgarın Oğlu",
        subtitleTr = "Savunma arkasına patlayıcı depar",
        branch = TraitBranch.TECHNIQUE,
        tier = 2,
        descriptionTr = "Fotoselli sprint hızı. Topu önüne attığında hiçbir savunma oyuncusunun yetişemeyeceği ivme üretir.",
        matchEffectSummary = "Açık alan deparlarında hızlanma +%20, kontrataklarda rakip savunmayı geride bırakma şansı.",
        pointCost = 2,
        requiredAttributes = mapOf("pace" to 74, "agility" to 68),
        prerequisiteTraitId = null,
        iconKey = "LIGHTNING_SPEED",
        rarityColorHex = "#EAB308"
    )

    val allTraits: List<PlayerTrait> = listOf(
        TRAIT_FREE_KICK_MAESTRO,
        TRAIT_LONG_RANGE_ROCKET,
        TRAIT_CLINICAL_FINISHER,
        TRAIT_POACHER_REFLEX,
        TRAIT_VISIONARY_PLAYMAKER,
        TRAIT_THREAD_THE_NEEDLE,
        TRAIT_DRIBBLE_TRICKSTER,
        TRAIT_FIRST_TOUCH_MAGIC,
        TRAIT_SPEED_DEMON,
        TRAIT_BIG_GAME_CLUTCH,
        TRAIT_AERIAL_DOMINANCE,
        TRAIT_ENDURANCE_BEAST
    )

    fun getTraitById(id: String): PlayerTrait? = allTraits.find { it.id == id }

    fun getTraitsByBranch(branch: TraitBranch): List<PlayerTrait> =
        allTraits.filter { it.branch == branch }.sortedBy { it.tier }

    /**
     * Oyuncunun antrenmanlar ve seviye atlamalarla kazandığı toplam Yetenek Puanı (TP).
     * Formülü: Başarılan antrenman sayısı / 2 + (Oyuncu seviyesi / potansiyel ilerlemesi) - harcanan puanlar.
     */
    fun calculateAvailableSkillPoints(
        player: PlayerProfile,
        totalTrainingPointsEarned: Int
    ): Int {
        val spentPoints = player.unlockedTraits.mapNotNull { getTraitById(it)?.pointCost }.sum()
        return (totalTrainingPointsEarned - spentPoints).coerceAtLeast(0)
    }

    /**
     * Vasıf kilit açma uygunluk kontrolü
     */
    fun checkUnlockEligibility(
        player: PlayerProfile,
        trait: PlayerTrait,
        availablePoints: Int
    ): TraitUnlockCheckResult {
        if (player.unlockedTraits.contains(trait.id)) {
            return TraitUnlockCheckResult(canUnlock = false, listOf("Bu vasıf zaten açıldı!"))
        }

        val missing = mutableListOf<String>()

        // 1. Yetenek puanı kontrolü
        if (availablePoints < trait.pointCost) {
            missing.add("${trait.pointCost} TP gerekiyor (Mevcut: $availablePoints TP)")
        }

        // 2. Önkoşul vasıf kontrolü
        trait.prerequisiteTraitId?.let { preId ->
            if (!player.unlockedTraits.contains(preId)) {
                val preTrait = getTraitById(preId)
                missing.add("Önce '${preTrait?.nameTr ?: preId}' vasfını açmalısın")
            }
        }

        // 3. Nitelik gereksinimleri kontrolü
        val attrs = player.attributes
        trait.requiredAttributes.forEach { (statKey, requiredValue) ->
            val currentValue = when (statKey) {
                "shooting" -> attrs.shooting
                "passing" -> attrs.passing
                "dribbling" -> attrs.dribbling
                "firstTouch" -> attrs.firstTouch
                "pace" -> attrs.pace
                "agility" -> attrs.agility
                "stamina" -> attrs.stamina
                "strength" -> attrs.strength
                "defending" -> attrs.defending
                "tactics" -> attrs.tactics
                "vision" -> attrs.vision
                "setPieces" -> attrs.setPieces
                "goalkeeping" -> attrs.goalkeeping
                else -> 0
            }
            val statNameTr = getStatNameTr(statKey)
            if (currentValue < requiredValue) {
                missing.add("$statNameTr $requiredValue gerekiyor (Senin: $currentValue)")
            }
        }

        return TraitUnlockCheckResult(
            canUnlock = missing.isEmpty(),
            missingRequirements = missing,
            pointsNeeded = (trait.pointCost - availablePoints).coerceAtLeast(0)
        )
    }

    private fun getStatNameTr(statKey: String): String = when (statKey) {
        "shooting" -> "Şut"
        "passing" -> "Pas"
        "dribbling" -> "Dribbling"
        "firstTouch" -> "İlk Kontrol"
        "pace" -> "Hız"
        "agility" -> "Çeviklik"
        "stamina" -> "Dayanıklılık"
        "strength" -> "Güç"
        "defending" -> "Savunma"
        "tactics" -> "Taktik"
        "vision" -> "Oyun Görüşü"
        "setPieces" -> "Duran Toplar"
        else -> statKey
    }

    /**
     * Sahadaki simülasyon ve interaktif anlarda kullanılacak aktif etki çarpanları
     */
    fun calculateActiveModifiers(
        unlockedTraitIds: List<String>,
        isDerbyOrFinal: Boolean = false,
        matchMinute: Int = 45,
        isTeamLosing: Boolean = false
    ): TraitBonusModifiers {
        var freeKickCurve = 0f
        var freeKickGuide = false
        var longShotPower = 1.0f
        var clutchMultiplier = 1.0f
        var aerialJump = 0f
        var dribbleBonus = 0f
        var staminaDecayRed = 0f
        var keyPassAcc = 0f
        var finishingConv = 0f
        var poacherReb = 0f
        var sprintBurst = 0f
        var moraleShield = false

        unlockedTraitIds.forEach { traitId ->
            when (traitId) {
                TRAIT_FREE_KICK_MAESTRO.id -> {
                    freeKickCurve += 0.25f
                    freeKickGuide = true
                }
                TRAIT_LONG_RANGE_ROCKET.id -> {
                    longShotPower += 0.20f
                }
                TRAIT_CLINICAL_FINISHER.id -> {
                    finishingConv += 0.30f
                }
                TRAIT_POACHER_REFLEX.id -> {
                    poacherReb += 0.30f
                }
                TRAIT_VISIONARY_PLAYMAKER.id -> {
                    keyPassAcc += 0.25f
                }
                TRAIT_THREAD_THE_NEEDLE.id -> {
                    keyPassAcc += 0.15f
                }
                TRAIT_DRIBBLE_TRICKSTER.id -> {
                    dribbleBonus += 0.25f
                }
                TRAIT_FIRST_TOUCH_MAGIC.id -> {
                    dribbleBonus += 0.10f
                }
                TRAIT_SPEED_DEMON.id -> {
                    sprintBurst += 0.20f
                }
                TRAIT_BIG_GAME_CLUTCH.id -> {
                    if (isDerbyOrFinal || (isTeamLosing && matchMinute >= 75)) {
                        clutchMultiplier += 0.12f
                    }
                    moraleShield = true
                }
                TRAIT_AERIAL_DOMINANCE.id -> {
                    aerialJump += 0.25f
                }
                TRAIT_ENDURANCE_BEAST.id -> {
                    staminaDecayRed += 0.30f
                }
            }
        }

        return TraitBonusModifiers(
            freeKickCurveBonus = freeKickCurve,
            freeKickGuideLineActive = freeKickGuide,
            longShotPowerMultiplier = longShotPower,
            clutchBonusMultiplier = clutchMultiplier,
            aerialJumpBonus = aerialJump,
            dribbleSuccessBonus = dribbleBonus,
            staminaDecayReduction = staminaDecayRed,
            keyPassAccuracyBonus = keyPassAcc,
            finishingConversionBonus = finishingConv,
            poacherReboundBonus = poacherReb,
            sprintBurstBonus = sprintBurst,
            teamMoraleShield = moraleShield
        )
    }
}
