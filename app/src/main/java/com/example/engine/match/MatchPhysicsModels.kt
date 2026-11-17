package com.example.engine.match

import com.example.engine.PitchPlayerNode
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 3D pitch coordinate where:
 * - [x] is 0..100 along pitch length (Home attacks 0 -> 100, Away attacks 100 -> 0)
 * - [y] is 0..100 across pitch width (0 = top touchline, 50 = center, 100 = bottom touchline)
 * - [z] is 0..100 ball loft height above the turf
 */
data class PitchPoint(
    val x: Float,
    val y: Float,
    val z: Float = 0f
)

enum class GestureActionKind(
    val labelTr: String,
    val badgeColorHex: String,
    val legacyActionType: String
) {
    CURVED_FINESSE_SHOT("FALSOLU PLASE ŞUT", "#28F092", "FINESSE_SHOT"),
    DRIVEN_POWER_SHOT("SERT FÜZE ŞUT", "#FFBE3D", "POWER_SHOT"),
    CHIP_SHOT("AŞIRTMA VURUŞ", "#58C4FF", "FINESSE_SHOT"),
    THROUGH_PASS("ARA PAS", "#28F092", "THROUGH_PASS"),
    CROSS_PASS("CEZA SAHASINA ORTA", "#58C4FF", "CROSS"),
    SHORT_PASS("PAS & VER-KAÇ", "#20C773", "THROUGH_PASS"),
    DRIBBLE_RUN("ÇALIM & TOP SÜRME", "#E879F9", "DRIBBLE_KEEPER"),
    SLIDE_TACKLE("KAYARAK MÜDAHALE", "#FF6B6B", "TACKLE")
}

/**
 * Encapsulates a finger-drawn trajectory on the 2D/2.5D pitch in the style of
 * New Star Soccer (NSS) and Soccer Superstar.
 */
data class GestureTrajectoryInput(
    val pitchPoints: List<PitchPoint>,
    val startX: Float,
    val startY: Float,
    val controlX: Float,
    val controlY: Float,
    val endX: Float,
    val endY: Float,
    val curveSpinDegrees: Float, // -45..+45 (Magnus effect lateral spin)
    val powerPct: Int, // 15..100
    val loftHeight: Float, // 0f..1f peak height factor
    val swipeVelocityPxPerSec: Float,
    val inferredKind: GestureActionKind,
    val targetTeammateId: String? = null,
    val targetTeammateName: String? = null,
    val blockedByDefenderWarning: Boolean = false
)

object TrajectoryAnalyzer {

    /**
     * Evaluates a quadratic Bezier point B(t) = (1-t)^2 * P0 + 2(1-t)t * P1 + t^2 * P2
     */
    fun evaluateBezier(
        t: Float,
        startX: Float,
        startY: Float,
        controlX: Float,
        controlY: Float,
        endX: Float,
        endY: Float
    ): Pair<Float, Float> {
        val u = (1f - t).coerceIn(0f, 1f)
        val tt = t.coerceIn(0f, 1f)
        val bx = u * u * startX + 2f * u * tt * controlX + tt * tt * endX
        val by = u * u * startY + 2f * u * tt * controlY + tt * tt * endY
        return bx to by
    }

    /**
     * Evaluates 3D ball height Z (0..65) along the flight progress [t] in 0..1.
     * Includes a secondary low bounce after landing for ground/driven trajectories.
     */
    fun evaluateBallHeightZ(
        t: Float,
        peakHeightZ: Float
    ): Float {
        val clamped = t.coerceIn(0f, 1f)
        if (peakHeightZ <= 1f) return 0f
        return if (clamped <= 0.88f) {
            val norm = clamped / 0.88f
            val parabola = 4f * norm * (1f - norm)
            (peakHeightZ * parabola).coerceAtLeast(0f)
        } else {
            val bounceNorm = (clamped - 0.88f) / 0.12f
            val bounceParabola = 4f * bounceNorm * (1f - bounceNorm)
            (peakHeightZ * 0.22f * bounceParabola).coerceAtLeast(0f)
        }
    }

    /**
     * Analyzes raw pitch points drawn by the user's finger and constructs a complete
     * [GestureTrajectoryInput] with curve spin, power, loft, target detection, and action classification.
     */
    fun analyzeDrawnPath(
        rawPoints: List<PitchPoint>,
        ballX: Float,
        ballY: Float,
        swipeDurationMs: Long,
        screenArcLengthPx: Float,
        attackingRight: Boolean,
        isDefensiveMoment: Boolean,
        players: List<PitchPlayerNode>,
        userIsHome: Boolean
    ): GestureTrajectoryInput? {
        if (rawPoints.size < 2) return null

        // Anchor trajectory start at the ball position so the ball always launches cleanly from the player's feet
        val startX = ballX
        val startY = ballY
        val lastPt = rawPoints.last()
        val endX = lastPt.x.coerceIn(1f, 99f)
        val endY = lastPt.y.coerceIn(2f, 98f)

        val chordDx = endX - startX
        val chordDy = endY - startY
        val chordLen = hypot(chordDx, chordDy)
        if (chordLen < 3.2f) return null

        // Measure maximum signed perpendicular distance from the drawn points to the straight chord
        val ux = chordDx / chordLen
        val uy = chordDy / chordLen
        // Normal vector (-uy, ux)
        val nx = -uy
        val ny = ux

        var maxSignedDeviation = 0f
        var peakPointX = (startX + endX) * 0.5f
        var peakPointY = (startY + endY) * 0.5f

        for (i in 1 until rawPoints.size - 1) {
            val pt = rawPoints[i]
            val vx = pt.x - startX
            val vy = pt.y - startY
            val projAlong = vx * ux + vy * uy
            if (projAlong in (chordLen * 0.10f)..(chordLen * 0.90f)) {
                val signedPerp = vx * nx + vy * ny
                if (abs(signedPerp) > abs(maxSignedDeviation)) {
                    maxSignedDeviation = signedPerp
                    peakPointX = pt.x
                    peakPointY = pt.y
                }
            }
        }

        // For a quadratic Bezier to pass near peakPoint at t=0.5:
        // B(0.5) = 0.25*P0 + 0.5*P1 + 0.25*P2 => P1 = 2*B(0.5) - 0.5*(P0 + P2)
        val midX = (startX + endX) * 0.5f
        val midY = (startY + endY) * 0.5f
        val clampedDev = maxSignedDeviation.coerceIn(-28f, 28f)
        val controlX = (midX + nx * clampedDev * 1.85f).coerceIn(0f, 100f)
        val controlY = (midY + ny * clampedDev * 1.85f).coerceIn(0f, 100f)

        // Convert lateral deviation ratio into spin degrees (-45..+45)
        val spinRatio = (clampedDev / chordLen.coerceAtLeast(10f)).coerceIn(-0.65f, 0.65f)
        val curveSpinDegrees = (spinRatio * 68f).coerceIn(-45f, 45f)

        // Calculate swipe velocity and power (15..100)
        val safeDurationSec = (swipeDurationMs.coerceIn(45L, 1400L)) / 1000f
        val velocityPxPerSec = (screenArcLengthPx / safeDurationSec).coerceIn(120f, 4200f)
        val distanceFactor = ((chordLen / 46f) * 62f).coerceIn(12f, 68f)
        val speedFactor = ((velocityPxPerSec / 2800f) * 38f).coerceIn(5f, 36f)
        val powerPct = (distanceFactor + speedFactor).roundToInt().coerceIn(18, 100)

        // Determine if aiming toward the opponent goalmouth
        val goalX = if (attackingRight) 96.5f else 3.5f
        val movingTowardGoal = if (attackingRight) endX > startX + 4f else endX < startX - 4f
        val distToGoalFromEnd = hypot(goalX - endX, 50f - endY)

        // Project whether the gesture ends near or crosses the goal line within goal Y range (24..76)
        val aimsAtGoalMouth = movingTowardGoal &&
            ((attackingRight && endX >= 85f) || (!attackingRight && endX <= 15f)) &&
            endY in 24f..76f

        // Check if aiming near a teammate (or slightly ahead of a teammate's run target)
        val teammates = players.filter { it.isHome == userIsHome && !it.isUser }
        var bestTeammate: PitchPlayerNode? = null
        var bestTeammateDist = Float.MAX_VALUE
        for (tm in teammates) {
            val dCurrent = hypot(tm.x - endX, tm.y - endY)
            // Also check slightly ahead of teammate toward goal (lead pass / through ball)
            val leadX = if (attackingRight) (tm.x + 8f).coerceAtMost(93f) else (tm.x - 8f).coerceAtLeast(7f)
            val dLead = hypot(leadX - endX, tm.y - endY)
            val dRun = hypot(tm.runTargetX - endX, tm.runTargetY - endY)
            val minD = minOf(dCurrent, dLead, dRun)
            if (minD < bestTeammateDist) {
                bestTeammateDist = minD
                bestTeammate = tm
            }
        }

        val inferredKind: GestureActionKind
        var matchedTeammate: PitchPlayerNode? = null

        if (isDefensiveMoment) {
            inferredKind = GestureActionKind.SLIDE_TACKLE
        } else if (aimsAtGoalMouth && (bestTeammateDist > 7.5f || endX >= 90f || endX <= 10f)) {
            inferredKind = when {
                abs(curveSpinDegrees) >= 11f -> GestureActionKind.CURVED_FINESSE_SHOT
                powerPct <= 54 && hypot(goalX - startX, 50f - startY) < 28f -> GestureActionKind.CHIP_SHOT
                else -> GestureActionKind.DRIVEN_POWER_SHOT
            }
        } else if (bestTeammate != null && bestTeammateDist <= 13.5f) {
            matchedTeammate = bestTeammate
            val isWideCross = (startY < 26f || startY > 74f) &&
                endY in 30f..70f &&
                ((attackingRight && endX >= 72f) || (!attackingRight && endX <= 28f))
            val isThroughBall = (attackingRight && endX > bestTeammate.x + 3.5f) ||
                (!attackingRight && endX < bestTeammate.x - 3.5f)
            inferredKind = when {
                isWideCross -> GestureActionKind.CROSS_PASS
                isThroughBall -> GestureActionKind.THROUGH_PASS
                else -> GestureActionKind.SHORT_PASS
            }
        } else if (chordLen <= 21f && !aimsAtGoalMouth) {
            inferredKind = GestureActionKind.DRIBBLE_RUN
        } else if (movingTowardGoal && distToGoalFromEnd <= 24f) {
            inferredKind = if (abs(curveSpinDegrees) >= 11f) {
                GestureActionKind.CURVED_FINESSE_SHOT
            } else {
                GestureActionKind.DRIVEN_POWER_SHOT
            }
        } else {
            // Open space pass / through ball
            if (bestTeammate != null && bestTeammateDist <= 22f) {
                matchedTeammate = bestTeammate
            }
            inferredKind = if (startY < 25f || startY > 75f) {
                GestureActionKind.CROSS_PASS
            } else {
                GestureActionKind.THROUGH_PASS
            }
        }

        // For shots, project the final Bezier path all the way to the goal line (goalX)
        // so preview, defender/wall intersection checks, and actual ball flight use the exact same curve.
        val isShot = inferredKind in listOf(
            GestureActionKind.CURVED_FINESSE_SHOT,
            GestureActionKind.DRIVEN_POWER_SHOT,
            GestureActionKind.CHIP_SHOT
        )
        val finalEndX: Float
        val finalEndY: Float
        val finalControlX: Float
        val finalControlY: Float
        if (isShot) {
            val targetGoalLineX = if (attackingRight) 97.2f else 2.8f
            val dx = endX - startX
            val needsExtension = if (attackingRight) endX < targetGoalLineX else endX > targetGoalLineX
            if (needsExtension && abs(dx) > 2f) {
                val scale = ((targetGoalLineX - startX) / dx).coerceIn(1.0f, 1.55f)
                finalEndX = targetGoalLineX
                finalEndY = (startY + (endY - startY) * scale).coerceIn(18f, 82f)
                finalControlX = (startX + (controlX - startX) * scale).coerceIn(0f, 100f)
                finalControlY = (startY + (controlY - startY) * scale).coerceIn(2f, 98f)
            } else {
                finalEndX = targetGoalLineX
                finalEndY = endY.coerceIn(18f, 82f)
                finalControlX = controlX
                finalControlY = controlY
            }
        } else {
            finalEndX = endX
            finalEndY = endY
            finalControlX = controlX
            finalControlY = controlY
        }

        val loftHeight = when (inferredKind) {
            GestureActionKind.CHIP_SHOT -> 0.85f
            GestureActionKind.CROSS_PASS -> 0.78f
            GestureActionKind.CURVED_FINESSE_SHOT -> (0.42f + (powerPct / 220f)).coerceIn(0.40f, 0.78f)
            GestureActionKind.DRIVEN_POWER_SHOT -> (0.30f + (powerPct / 200f)).coerceIn(0.28f, 0.72f)
            GestureActionKind.THROUGH_PASS -> if (chordLen > 28f) 0.45f else 0.14f
            GestureActionKind.SHORT_PASS -> 0.08f
            GestureActionKind.DRIBBLE_RUN, GestureActionKind.SLIDE_TACKLE -> 0.03f
        }

        // Check if any opponent outfield defender is standing directly on the low trajectory path
        val opponents = players.filter { it.isHome != userIsHome && it.position != com.example.model.Position.GK }
        var defenderBlocking = false
        if (inferredKind != GestureActionKind.SLIDE_TACKLE && inferredKind != GestureActionKind.DRIBBLE_RUN) {
            val peakZ = loftHeight * 54f
            for (step in 2..14) {
                val t = step / 16f
                val (bx, by) = evaluateBezier(t, startX, startY, finalControlX, finalControlY, finalEndX, finalEndY)
                val bz = evaluateBallHeightZ(t, peakZ)
                if (bz < 24f) {
                    val blocker = opponents.any { opp -> hypot(opp.x - bx, opp.y - by) < 3.4f }
                    if (blocker) {
                        defenderBlocking = true
                        break
                    }
                }
            }
        }

        // Sample smooth Bezier points for rendering
        val sampledBezier = (0..20).map { idx ->
            val t = idx / 20f
            val (bx, by) = evaluateBezier(t, startX, startY, finalControlX, finalControlY, finalEndX, finalEndY)
            val bz = evaluateBallHeightZ(t, loftHeight * 54f)
            PitchPoint(bx, by, bz)
        }

        return GestureTrajectoryInput(
            pitchPoints = sampledBezier,
            startX = startX,
            startY = startY,
            controlX = finalControlX,
            controlY = finalControlY,
            endX = finalEndX,
            endY = finalEndY,
            curveSpinDegrees = curveSpinDegrees,
            powerPct = powerPct,
            loftHeight = loftHeight,
            swipeVelocityPxPerSec = velocityPxPerSec,
            inferredKind = inferredKind,
            targetTeammateId = matchedTeammate?.id,
            targetTeammateName = matchedTeammate?.let { "#${it.number} ${it.name}" },
            blockedByDefenderWarning = defenderBlocking
        )
    }
}
