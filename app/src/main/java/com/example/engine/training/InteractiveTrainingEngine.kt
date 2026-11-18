package com.example.engine.training

import com.example.model.PlayerAttributes
import com.example.model.PlayerProfile
import com.example.model.Position
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * Football Life - İnteraktif Antrenman ve Mini-Oyun Motoru
 * NSS ve Soccer Superstar tarzı hedef halkası vurma, isabetli pas parkuru
 * ve çeviklik slalomu simülasyon ve puanlama mekaniği.
 */

enum class TrainingMiniGameType(
    val titleTr: String,
    val subtitleTr: String,
    val primaryStat: String,
    val secondaryStat: String,
    val iconKey: String
) {
    FREE_KICK_TARGET(
        titleTr = "Baraj Üstü Frikik & Yeşil Halka",
        subtitleTr = "Manken barajını aşıp 90'daki yeşil hedef halkasını vur!",
        primaryStat = "setPieces",
        secondaryStat = "shooting",
        iconKey = "TARGET_FREE_KICK"
    ),
    PRECISION_PASSING(
        titleTr = "İsabetli Pas & Koşu Yolu Parkuru",
        subtitleTr = "Hareketli savunma kuklalarını aşıp forvetin ayağına adrese teslim pas at!",
        primaryStat = "passing",
        secondaryStat = "vision",
        iconKey = "PASS_ACCURACY"
    ),
    AGILITY_DRIBBLE_SLALOM(
        titleTr = "Çeviklik & Top Sürme Slalomu",
        subtitleTr = "Hızlı yön değiştirmelerle konilerin arasından sıyrılarak zamanla yarış!",
        primaryStat = "dribbling",
        secondaryStat = "agility",
        iconKey = "DRIBBLE_SLALOM"
    )
}

enum class DrillDifficulty(
    val titleTr: String,
    val wallDummiesCount: Int,
    val ringMoveSpeed: Float,
    val scoreMultiplier: Float,
    val targetHitTolerance: Float
) {
    AMATEUR("Amatör", wallDummiesCount = 2, ringMoveSpeed = 0f, scoreMultiplier = 1.0f, targetHitTolerance = 0.12f),
    SEMI_PRO("Yarı Profesyonel", wallDummiesCount = 3, ringMoveSpeed = 0.5f, scoreMultiplier = 1.25f, targetHitTolerance = 0.09f),
    PRO("Süper Lig Seviyesi", wallDummiesCount = 4, ringMoveSpeed = 1.0f, scoreMultiplier = 1.5f, targetHitTolerance = 0.07f),
    WORLD_CLASS("Dünya Klası", wallDummiesCount = 5, ringMoveSpeed = 1.6f, scoreMultiplier = 2.0f, targetHitTolerance = 0.055f)
}

enum class DrillMedalTier(val titleTr: String, val badgeColorHex: String, val minScorePct: Int) {
    GOLD("Altın Madalya", "#F59E0B", 80),
    SILVER("Gümüş Madalya", "#94A3B8", 60),
    BRONZE("Bronz Madalya", "#D97706", 40),
    PARTICIPATION("Katılım Plaketi", "#6B7280", 0)
}

data class TargetRingState(
    val id: Int,
    val normX: Float, // 0.0 (sol direk) .. 1.0 (sağ direk)
    val normY: Float, // 0.0 (zemin) .. 1.0 (üst direk)
    val radiusNorm: Float = 0.08f,
    val basePoints: Int = 150,
    val isHit: Boolean = false,
    val isBullseye: Boolean = false,
    val isBonusRotating: Boolean = false,
    val oscillationPhase: Float = 0f,
    val isMoving: Boolean = false
)

data class WallDummyState(
    val id: Int,
    val normX: Float, // Kaleye göre x ekseni (-0.4f .. 0.4f)
    val depthNorm: Float = 0.55f, // Top ile kale arasındaki bağıl mesafe
    val heightMeters: Float = 1.88f,
    val widthNorm: Float = 0.11f,
    val isJumping: Boolean = false
)

data class SlalomGateState(
    val id: Int,
    val normX: Float,
    val normY: Float,
    val gateWidthNorm: Float = 0.22f,
    val isCleared: Boolean = false
)

data class DrillAttemptResult(
    val attemptNumber: Int,
    val isGoal: Boolean,
    val isTargetHit: Boolean,
    val isBullseye: Boolean,
    val wasBlockedByWall: Boolean,
    val hitPostOrCrossbar: Boolean,
    val pointsAwarded: Int,
    val shotSpeedKmH: Int,
    val commentaryTr: String,
    val trajectoryPoints: List<Triple<Float, Float, Float>> = emptyList() // x, y, z (height)
)

data class DrillSessionSummary(
    val gameType: TrainingMiniGameType,
    val difficulty: DrillDifficulty,
    val totalAttempts: Int,
    val successfulGoals: Int,
    val targetHits: Int,
    val bullseyes: Int,
    val totalScore: Int,
    val medalTier: DrillMedalTier,
    val xpEarned: Int,
    val skillPointsEarned: Int,
    val primaryStatGained: String,
    val primaryStatGainAmount: Int,
    val secondaryStatGained: String,
    val secondaryStatGainAmount: Int,
    val coachNotesTr: String
)

object InteractiveTrainingEngine {

    /**
     * Frikik hedef halkalarını oluştur (NSS / Soccer Superstar tarzı yeşil halkalar)
     */
    fun createDefaultTargetRings(difficulty: DrillDifficulty): List<TargetRingState> {
        val list = mutableListOf<TargetRingState>()
        // Sol üst çatal (90) halkası
        list.add(
            TargetRingState(
                id = 1,
                normX = 0.12f,
                normY = 0.84f,
                radiusNorm = difficulty.targetHitTolerance,
                basePoints = 150,
                isMoving = difficulty.ringMoveSpeed > 0f
            )
        )
        // Sağ üst çatal (90) halkası
        list.add(
            TargetRingState(
                id = 2,
                normX = 0.88f,
                normY = 0.84f,
                radiusNorm = difficulty.targetHitTolerance,
                basePoints = 150,
                isMoving = difficulty.ringMoveSpeed > 0f
            )
        )
        // Sol alt köşe halkası
        list.add(
            TargetRingState(
                id = 3,
                normX = 0.16f,
                normY = 0.18f,
                radiusNorm = difficulty.targetHitTolerance + 0.015f,
                basePoints = 100
            )
        )
        // Sağ alt köşe halkası
        list.add(
            TargetRingState(
                id = 4,
                normX = 0.84f,
                normY = 0.18f,
                radiusNorm = difficulty.targetHitTolerance + 0.015f,
                basePoints = 100
            )
        )
        // Zor seviyelerde üst orta sallanan bonus altın halka
        if (difficulty == DrillDifficulty.PRO || difficulty == DrillDifficulty.WORLD_CLASS) {
            list.add(
                TargetRingState(
                    id = 5,
                    normX = 0.50f,
                    normY = 0.90f,
                    radiusNorm = difficulty.targetHitTolerance * 0.9f,
                    basePoints = 250,
                    isBonusRotating = true,
                    isMoving = true
                )
            )
        }
        return list
    }

    /**
     * Baraj mankenlerini oluştur
     */
    fun createWallDummies(difficulty: DrillDifficulty, wallShiftSide: Float = 0f): List<WallDummyState> {
        val count = difficulty.wallDummiesCount
        val dummies = mutableListOf<WallDummyState>()
        val startX = -((count - 1) * 0.09f) / 2f + wallShiftSide
        for (i in 0 until count) {
            dummies.add(
                WallDummyState(
                    id = i,
                    normX = (startX + i * 0.095f).coerceIn(-0.45f, 0.45f),
                    depthNorm = 0.52f,
                    heightMeters = 1.86f + (if (i % 2 == 1) 0.04f else 0f),
                    widthNorm = 0.088f,
                    isJumping = (difficulty == DrillDifficulty.WORLD_CLASS && i == count / 2)
                )
            )
        }
        return dummies
    }

    /**
     * Pas parkuru hedef ve koşu kapılarını oluştur
     */
    fun createPassingGates(difficulty: DrillDifficulty): List<SlalomGateState> {
        return listOf(
            SlalomGateState(id = 1, normX = 0.35f, normY = 0.35f, gateWidthNorm = 0.20f),
            SlalomGateState(id = 2, normX = 0.65f, normY = 0.60f, gateWidthNorm = 0.18f),
            SlalomGateState(id = 3, normX = 0.45f, normY = 0.85f, gateWidthNorm = 0.16f)
        )
    }

    /**
     * Çeviklik slalom konilerini oluştur
     */
    fun createSlalomCones(): List<Pair<Float, Float>> {
        return listOf(
            0.50f to 0.15f,
            0.32f to 0.32f,
            0.68f to 0.48f,
            0.30f to 0.64f,
            0.70f to 0.80f,
            0.50f to 0.95f
        )
    }

    /**
     * Çizilen parmak kavisi (swipe) ile frikik şutunu simüle et
     * @param swipePath Kullanıcının ekranda çizdiği kavisli rota noktaları [(normX, normY)]
     * @param shotPower 0.0f .. 1.0f vuruş şiddeti
     * @param playerAttributes Oyuncunun nitelikleri
     * @param unlockedTraits Açılmış vasıflar
     * @param targetRings Hedef halkaları
     * @param wallDummies Baraj mankenleri
     */
    fun simulateFreeKickShot(
        swipePath: List<Pair<Float, Float>>,
        shotPower: Float,
        playerAttributes: PlayerAttributes,
        unlockedTraits: List<String>,
        targetRings: List<TargetRingState>,
        wallDummies: List<WallDummyState>,
        difficulty: DrillDifficulty
    ): DrillAttemptResult {
        if (swipePath.isEmpty()) {
            return DrillAttemptResult(
                attemptNumber = 1,
                isGoal = false,
                isTargetHit = false,
                isBullseye = false,
                wasBlockedByWall = false,
                hitPostOrCrossbar = false,
                pointsAwarded = 0,
                shotSpeedKmH = 0,
                commentaryTr = "Vuruş yapılamadı! Parmağınla kaleye doğru falso çizmelisin."
            )
        }

        val modifiers = PlayerTraitsCatalog.calculateActiveModifiers(unlockedTraits)
        val endPoint = swipePath.last()
        val midPoint = if (swipePath.size > 2) swipePath[swipePath.size / 2] else endPoint

        // Falso ve yön sapması
        val curveAmount = (endPoint.first - midPoint.first) * (1.0f + modifiers.freeKickCurveBonus)
        val finalTargetX = (endPoint.first + curveAmount * 0.35f).coerceIn(0.0f, 1.0f)
        val finalTargetY = (endPoint.second).coerceIn(0.0f, 1.0f)

        // Hız hesabı (km/h)
        val baseSpeed = 75 + (playerAttributes.shooting * 0.45f) + (shotPower * 30f)
        val actualSpeed = (baseSpeed * modifiers.longShotPowerMultiplier).roundToInt().coerceIn(60, 138)

        // Yükseklik eğrisi (parabolik z tepe noktası)
        val arcApexHeight = 1.95f + (shotPower * 0.8f) - (playerAttributes.setPieces * 0.005f)

        // 1. Baraj çarpışma kontrolü
        val midTrajectoryX = (midPoint.first - 0.5f)
        var blockedByWall = false
        wallDummies.forEach { dummy ->
            val dist = abs(midTrajectoryX - dummy.normX)
            if (dist < dummy.widthNorm && arcApexHeight < dummy.heightMeters) {
                blockedByWall = true
            }
        }

        if (blockedByWall) {
            return DrillAttemptResult(
                attemptNumber = 1,
                isGoal = false,
                isTargetHit = false,
                isBullseye = false,
                wasBlockedByWall = true,
                hitPostOrCrossbar = false,
                pointsAwarded = 10,
                shotSpeedKmH = actualSpeed,
                commentaryTr = "Top barajdaki mankene takıldı! Topun altından daha yüksek falso vermelisin.",
                trajectoryPoints = buildArcTrajectory(0.5f, 0.0f, midPoint.first, 0.5f, finalTargetX, 1.0f, blockedAt = 0.5f)
            )
        }

        // 2. Kale çerçevesi kontrolü (Kale genişliği: 0.05f .. 0.95f, Yükseklik: 0.0f .. 0.92f)
        val hitLeftPost = abs(finalTargetX - 0.05f) < 0.025f && finalTargetY <= 0.95f
        val hitRightPost = abs(finalTargetX - 0.95f) < 0.025f && finalTargetY <= 0.95f
        val hitCrossbar = abs(finalTargetY - 0.92f) < 0.035f && finalTargetX in 0.05f..0.95f

        if (hitLeftPost || hitRightPost || hitCrossbar) {
            val postName = if (hitCrossbar) "Üst Direk" else "Yan Direk"
            return DrillAttemptResult(
                attemptNumber = 1,
                isGoal = false,
                isTargetHit = false,
                isBullseye = false,
                wasBlockedByWall = false,
                hitPostOrCrossbar = true,
                pointsAwarded = 40,
                shotSpeedKmH = actualSpeed,
                commentaryTr = "$postName patladı! Santimlerle kaçtı, müthiş vuruş!",
                trajectoryPoints = buildArcTrajectory(0.5f, 0.0f, midPoint.first, 0.5f, finalTargetX, finalTargetY)
            )
        }

        // Avuta gidiş
        val isInsideGoal = finalTargetX in 0.06f..0.94f && finalTargetY in 0.04f..0.91f
        if (!isInsideGoal) {
            return DrillAttemptResult(
                attemptNumber = 1,
                isGoal = false,
                isTargetHit = false,
                isBullseye = false,
                wasBlockedByWall = false,
                hitPostOrCrossbar = false,
                pointsAwarded = 15,
                shotSpeedKmH = actualSpeed,
                commentaryTr = "Top az farkla auta gitti. Çerçeveyi bulamadın.",
                trajectoryPoints = buildArcTrajectory(0.5f, 0.0f, midPoint.first, 0.5f, finalTargetX, finalTargetY)
            )
        }

        // 3. Yeşil Hedef Halkası Çarpışma Kontrolü (Target Ring Hit)
        var hitRing: TargetRingState? = null
        var isBullseye = false
        var bestDist = Float.MAX_VALUE

        targetRings.forEach { ring ->
            val dist = hypot(finalTargetX - ring.normX, finalTargetY - ring.normY)
            if (dist <= ring.radiusNorm && dist < bestDist) {
                bestDist = dist
                hitRing = ring
                isBullseye = dist <= (ring.radiusNorm * 0.40f)
            }
        }

        val trajectory = buildArcTrajectory(0.5f, 0.0f, midPoint.first, 0.5f, finalTargetX, finalTargetY)

        if (hitRing != null) {
            val ring = hitRing!!
            val bullseyeMultiplier = if (isBullseye) 1.5f else 1.0f
            val points = ((ring.basePoints * bullseyeMultiplier) * difficulty.scoreMultiplier).roundToInt()
            val comment = when {
                isBullseye && ring.normY > 0.70f -> "İNANILMAZ! 90'DAKI HALKANIN TAM GÖBEĞİNDEN VURDU! ($actualSpeed km/h)"
                isBullseye -> "BULLSEYE! Tam on ikiden yeşil hedef halkasını delip geçti!"
                ring.normY > 0.70f -> "90'A TAKTI! Yeşil halka paramparça! Muhteşem gol!"
                else -> "KÖŞEYE MİLLİMETRİK! Hedef halkasını başarıyla vurdun!"
            }
            return DrillAttemptResult(
                attemptNumber = 1,
                isGoal = true,
                isTargetHit = true,
                isBullseye = isBullseye,
                wasBlockedByWall = false,
                hitPostOrCrossbar = false,
                pointsAwarded = points,
                shotSpeedKmH = actualSpeed,
                commentaryTr = comment,
                trajectoryPoints = trajectory
            )
        }

        // Halkaya değmedi ama temiz gol
        val cleanGoalPoints = (75 * difficulty.scoreMultiplier).roundToInt()
        return DrillAttemptResult(
            attemptNumber = 1,
            isGoal = true,
            isTargetHit = false,
            isBullseye = false,
            wasBlockedByWall = false,
            hitPostOrCrossbar = false,
            pointsAwarded = cleanGoalPoints,
            shotSpeedKmH = actualSpeed,
            commentaryTr = "Temiz bir gol! Ancak puan katlamak için yeşil halkaları nişan almalısın.",
            trajectoryPoints = trajectory
        )
    }

    /**
     * Pas parkuru vuruş simülasyonu
     */
    fun simulatePassingDrill(
        passTrajectory: List<Pair<Float, Float>>,
        teammatePos: Pair<Float, Float>,
        playerAttributes: PlayerAttributes,
        unlockedTraits: List<String>,
        difficulty: DrillDifficulty
    ): DrillAttemptResult {
        val modifiers = PlayerTraitsCatalog.calculateActiveModifiers(unlockedTraits)
        val end = passTrajectory.lastOrNull() ?: (0.5f to 0.8f)
        val distToTeammate = hypot(end.first - teammatePos.first, end.second - teammatePos.second)

        val tolerance = (0.10f + modifiers.keyPassAccuracyBonus * 0.05f) * (1.2f - difficulty.scoreMultiplier * 0.1f)
        val isAccurate = distToTeammate <= tolerance
        val isBullseye = distToTeammate <= tolerance * 0.35f

        val points = if (isBullseye) {
            (180 * difficulty.scoreMultiplier).roundToInt()
        } else if (isAccurate) {
            (120 * difficulty.scoreMultiplier).roundToInt()
        } else {
            30
        }

        val comment = when {
            isBullseye -> "ADRESE TESLİM! Takım arkadaşının doğrudan koşu yoluna santimetrik pas!"
            isAccurate -> "İsabetli ara pası! Hücumcu topu kontrol etti ve atağı sürdürdü."
            else -> "Pas hızı veya açısı hatalı, savunma topu kesti."
        }

        return DrillAttemptResult(
            attemptNumber = 1,
            isGoal = isAccurate,
            isTargetHit = isAccurate,
            isBullseye = isBullseye,
            wasBlockedByWall = !isAccurate,
            hitPostOrCrossbar = false,
            pointsAwarded = points,
            shotSpeedKmH = (60 + playerAttributes.passing * 0.3f).roundToInt(),
            commentaryTr = comment
        )
    }

    /**
     * Çeviklik & Top Sürme slalom simülasyonu
     */
    fun simulateDribbleGateCheck(
        currentX: Float,
        coneX: Float,
        coneY: Float,
        playerAttributes: PlayerAttributes,
        unlockedTraits: List<String>
    ): Pair<Boolean, String> {
        val modifiers = PlayerTraitsCatalog.calculateActiveModifiers(unlockedTraits)
        val lateralDist = abs(currentX - coneX)
        val clearance = 0.08f + (modifiers.dribbleSuccessBonus * 0.02f)
        val success = lateralDist in 0.05f..0.22f

        val msg = if (success) {
            "Koninin çevresinden zarif bir çalımla kıvrıldın! (+50)"
        } else if (lateralDist < 0.05f) {
            "Koniye çarptın! Dengen bozuldu."
        } else {
            "Çok açıktan döndün, zaman kaybettin."
        }

        return success to msg
    }

    /**
     * Antrenman seansı bitiş hesaplaması ve ödül dağıtımı
     */
    fun finalizeSession(
        gameType: TrainingMiniGameType,
        difficulty: DrillDifficulty,
        totalAttempts: Int,
        results: List<DrillAttemptResult>,
        playerProfile: PlayerProfile
    ): DrillSessionSummary {
        val totalScore = results.sumOf { it.pointsAwarded }
        val successfulGoals = results.count { it.isGoal }
        val targetHits = results.count { it.isTargetHit }
        val bullseyes = results.count { it.isBullseye }

        val maxPossible = totalAttempts * (200 * difficulty.scoreMultiplier).roundToInt()
        val scorePct = if (maxPossible > 0) ((totalScore.toFloat() / maxPossible) * 100).roundToInt() else 0

        val medal = when {
            scorePct >= DrillMedalTier.GOLD.minScorePct -> DrillMedalTier.GOLD
            scorePct >= DrillMedalTier.SILVER.minScorePct -> DrillMedalTier.SILVER
            scorePct >= DrillMedalTier.BRONZE.minScorePct -> DrillMedalTier.BRONZE
            else -> DrillMedalTier.PARTICIPATION
        }

        // Kazanılan XP ve Yetenek Puanı (TP)
        val xpAward = when (medal) {
            DrillMedalTier.GOLD -> 180
            DrillMedalTier.SILVER -> 120
            DrillMedalTier.BRONZE -> 80
            DrillMedalTier.PARTICIPATION -> 40
        }

        // Yetenek Puanı (TP): Altın madalyada kesin 1 TP, üstün başarıda ekstra
        val skillPointsAward = when {
            medal == DrillMedalTier.GOLD && bullseyes >= 2 -> 2
            medal == DrillMedalTier.GOLD || medal == DrillMedalTier.SILVER -> 1
            else -> 0
        }

        // Nitelik artışları
        val primaryGain = when (medal) {
            DrillMedalTier.GOLD -> 2
            DrillMedalTier.SILVER -> 1
            DrillMedalTier.BRONZE -> 1
            DrillMedalTier.PARTICIPATION -> 0
        }

        val secondaryGain = if (medal == DrillMedalTier.GOLD) 1 else 0

        val coachNotes = when (medal) {
            DrillMedalTier.GOLD -> "Teknik Direktör: 'Harika bir antrenman çıkardın! Baraj üstünden o yeşil halkaları vururkenki falso kaliten birinci sınıf.'"
            DrillMedalTier.SILVER -> "Teknik Direktör: 'Oldukça iyi bir seans. Birkaç vuruşta daha sakin kalsaydın tam 90'a asabilirdin.'"
            DrillMedalTier.BRONZE -> "Teknik Direktör: 'Fena değil ancak maçta bu pozisyonları kaçırmamalıyız. Tekrarlara devam etmelisin.'"
            DrillMedalTier.PARTICIPATION -> "Teknik Direktör: 'Bugün odaklanma problemi yaşadın. Dinlenip antrenmana daha motive gelmelisin.'"
        }

        return DrillSessionSummary(
            gameType = gameType,
            difficulty = difficulty,
            totalAttempts = totalAttempts,
            successfulGoals = successfulGoals,
            targetHits = targetHits,
            bullseyes = bullseyes,
            totalScore = totalScore,
            medalTier = medal,
            xpEarned = xpAward,
            skillPointsEarned = skillPointsAward,
            primaryStatGained = gameType.primaryStat,
            primaryStatGainAmount = primaryGain,
            secondaryStatGained = gameType.secondaryStat,
            secondaryStatGainAmount = secondaryGain,
            coachNotesTr = coachNotes
        )
    }

    private fun buildArcTrajectory(
        startX: Float, startY: Float,
        midX: Float, midY: Float,
        endX: Float, endY: Float,
        blockedAt: Float? = null
    ): List<Triple<Float, Float, Float>> {
        val points = mutableListOf<Triple<Float, Float, Float>>()
        val steps = 24
        val maxStep = if (blockedAt != null) (steps * blockedAt).toInt() else steps

        for (i in 0..maxStep) {
            val t = i.toFloat() / steps
            // Quadratic Bezier interpolation in 2D
            val x = (1 - t) * (1 - t) * startX + 2 * (1 - t) * t * midX + t * t * endX
            val y = (1 - t) * (1 - t) * startY + 2 * (1 - t) * t * midY + t * t * endY
            // Parabolic arc for height Z
            val z = sin(t * Math.PI.toFloat()) * 2.4f
            points.add(Triple(x, y, z))
        }
        return points
    }
}
