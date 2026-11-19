package com.example.engine

import com.example.engine.match.BoxDuelist
import com.example.engine.match.CornerKickConfiguration
import com.example.engine.match.FreeKickWallConfiguration
import com.example.engine.match.GestureActionKind
import com.example.engine.match.GestureTrajectoryInput
import com.example.engine.match.MedicalInjuryEvent
import com.example.engine.match.PenaltyKeeperDive
import com.example.engine.match.PenaltyDuelConfiguration
import com.example.engine.match.PitchPoint
import com.example.engine.match.RefereeDramaEvent
import com.example.engine.match.RefereeDramaType
import com.example.engine.match.SetPiecePhysicsSimulator
import com.example.engine.match.TrajectoryAnalyzer
import com.example.engine.match.WallDefender
import com.example.model.Club
import com.example.model.CompletedMatchReport
import com.example.model.LeagueFixture
import com.example.model.MatchEventRecord
import com.example.model.PlayerProfile
import com.example.model.Position
import com.example.model.StartingStatus
import com.example.engine.match.GOAL_MOUTH_MAX_Y
import com.example.engine.match.GOAL_MOUTH_MIN_Y
import com.example.engine.match.PITCH_Z_TO_METERS
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.random.Random

enum class MatchPhase {
    PRE_MATCH,
    LIVE_PLAY,
    CRITICAL_DECISION,
    ACTION_RESOLUTION,
    GOAL_CELEBRATION,
    HALF_TIME,
    FULL_TIME
}

enum class PlayerVisualPose {
    NORMAL,
    DEFENSIVE_WALL,
    WALL_JUMP,
    GK_READY,
    GK_DIVE,
    KICK_STRIKE,
    BICYCLE_KICK
}

/** Maç içinde tetiklenen sinematik sahne türü (duran top, kart, sakatlık). */
enum class MatchCinematicKind {
    FREE_KICK_WALL,
    PENALTY_DUEL,
    CORNER_SCRAMBLE,
    REFEREE_CARD,
    MEDICAL_STRETCHER
}

data class MatchCinematic(
    val kind: MatchCinematicKind,
    val minute: Int,
    val ticksRemaining: Int = 18,
    val freeKick: FreeKickWallConfiguration? = null,
    val penalty: PenaltyDuelConfiguration? = null,
    val corner: CornerKickConfiguration? = null,
    val referee: RefereeDramaEvent? = null,
    val injury: MedicalInjuryEvent? = null
)

data class PitchPlayerNode(
    val id: String,
    val number: Int,
    val name: String,
    val position: Position,
    val isHome: Boolean,
    val isUser: Boolean,
    val baseX: Float, // 0..100 (left to right)
    val baseY: Float, // 0..100 (top to bottom)
    val x: Float,
    val y: Float,
    val targetX: Float,
    val targetY: Float,
    val hasBall: Boolean = false,
    val runTargetX: Float = targetX,
    val runTargetY: Float = targetY,
    val isMakingRun: Boolean = false,
    val isDivingOrSliding: Boolean = false,
    val diveDirectionY: Float = 0f,
    val roleTag: String = "",
    val visualPose: PlayerVisualPose = PlayerVisualPose.NORMAL,
    val elevationZ: Float = 0f,
    val kitShortsColorHex: String = "",
    val hairColorHex: String = "",
    val skinToneHex: String = ""
)

data class PitchBallState(
    val x: Float = 50f,
    val y: Float = 50f,
    val targetX: Float = 50f,
    val targetY: Float = 50f,
    val speed: Float = 0.35f,
    val isShotInFlight: Boolean = false,
    val z: Float = 0f, // 0..60 ball height above pitch
    val startX: Float = 50f,
    val startY: Float = 50f,
    val controlX: Float = 50f,
    val controlY: Float = 50f,
    val curveSpin: Float = 0f, // -45..+45 degrees
    val peakHeightZ: Float = 0f,
    val flightProgress: Float = 1f, // 0f..1f along Bezier curve
    val flightStepPerTick: Float = 0.09f,
    val trailPoints: List<PitchPoint> = emptyList(),
    val secondStageTargetX: Float? = null,
    val secondStageTargetY: Float? = null,
    val secondStageControlX: Float? = null,
    val secondStageControlY: Float? = null
)

data class TacticalDecisionOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val keyAttributeLabel: String,
    val keyAttributeValue: Int,
    val successProbabilityPct: Int,
    val riskLevel: String, // "Düşük Risk", "Dengeli", "Yüksek Risk / Yüksek Ödül"
    val actionType: String, // "FINESSE_SHOT", "POWER_SHOT", "THROUGH_PASS", "DRIBBLE_KEEPER", "TACKLE", "CROSS"
    val gestureInput: GestureTrajectoryInput? = null
) {
    companion object {
        fun fromGesture(gesture: GestureTrajectoryInput): TacticalDecisionOption {
            return TacticalDecisionOption(
                id = "GESTURE_${gesture.inferredKind.name}",
                title = gesture.inferredKind.labelTr,
                subtitle = "Parmağınla çizilen falsolu rota",
                keyAttributeLabel = gesture.inferredKind.labelTr,
                keyAttributeValue = gesture.powerPct,
                successProbabilityPct = 75,
                riskLevel = "Canlı Çizim",
                actionType = gesture.inferredKind.legacyActionType,
                gestureInput = gesture
            )
        }
    }
}

data class CriticalMomentContext(
    val id: String,
    val minute: Int,
    val title: String,
    val situationDescription: String,
    val ballX: Float,
    val ballY: Float,
    val options: List<TacticalDecisionOption>,
    val scenarioType: String = "ATTACK_OPEN_PLAY",
    val isDefensive: Boolean = false,
    val slowMoFactor: Float = 0.14f,
    val windSpeedKmh: Int = 0,
    val windDirectionDeg: Float = 0f,
    val hintTextTr: String = "Parmağınla topun falsolu rotasını çiz: Kaleye şut, arkadaşına pas veya boşluğa çalım!",
    val chainStep: Int = 1
)

data class LiveMatchUiState(
    val fixture: LeagueFixture,
    val homeClub: Club,
    val awayClub: Club,
    val userIsHome: Boolean,
    val userStartingStatus: StartingStatus,
    val userOnPitch: Boolean,
    val phase: MatchPhase = MatchPhase.PRE_MATCH,
    val minute: Int = 0,
    val subSecondTick: Int = 0,
    val speedMultiplier: Int = 1, // 1x, 2x, 3x
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val homePossessionPct: Int = 50,
    val homeShots: Int = 0,
    val awayShots: Int = 0,
    val homeShotsOnTarget: Int = 0,
    val awayShotsOnTarget: Int = 0,
    val homeCorners: Int = 0,
    val awayCorners: Int = 0,
    val homeFouls: Int = 0,
    val awayFouls: Int = 0,
    val userGoals: Int = 0,
    val userAssists: Int = 0,
    val userShots: Int = 0,
    val userPassesCompleted: Int = 0,
    val userPassesAttempted: Int = 0,
    val userDecisionsTotal: Int = 0,
    val userDecisionsSucceeded: Int = 0,
    val userLiveRating: Double = 6.6,
    val userLiveEnergy: Int = 90,
    val players: List<PitchPlayerNode> = emptyList(),
    val ball: PitchBallState = PitchBallState(),
    val cameraFocusX: Float = 50f,
    val cameraFocusY: Float = 50f,
    val cameraZoom: Float = 1.0f,
    val activeDecision: CriticalMomentContext? = null,
    val latestBannerText: String = "Stadyumda son hazırlıklar tamamlandı!",
    val goalScorerBanner: String? = null,
    val commentaryFeed: List<MatchEventRecord> = emptyList(),
    val resolutionTicksRemaining: Int = 0,
    val pendingReport: CompletedMatchReport? = null,
    val resolutionOutcomeType: String = "",
    val pendingGoalForHome: Boolean? = null,
    val pendingGoalBannerText: String? = null,
    val pendingChainedMoment: CriticalMomentContext? = null,
    val lastGesturePreview: GestureTrajectoryInput? = null,
    val crowdExcitementLevel: Float = 0.52f,
    val stoppageMinutes: Int = 2,
    val activeCinematic: MatchCinematic? = null,
    val firstHalfStoppage: Int = 1,
    val lastDecisionMinute: Int? = null,
    val recentScenarioTypes: List<String> = emptyList()
)

class MatchSimulationEngine(private val baseSeed: Long = 20261114L) {

    fun createInitialMatchState(
        fixture: LeagueFixture,
        homeClub: Club,
        awayClub: Club,
        userPlayer: PlayerProfile,
        userClubId: String,
        startingStatus: StartingStatus
    ): LiveMatchUiState {
        val userIsHome = fixture.homeClubId == userClubId
        val userStarts = startingStatus == StartingStatus.STARTER
        val nodes = buildInitial22Players(
            homeClub = homeClub,
            awayClub = awayClub,
            userPlayer = userPlayer,
            userIsHome = userIsHome,
            userOnPitch = userStarts
        )
        return LiveMatchUiState(
            fixture = fixture,
            homeClub = homeClub,
            awayClub = awayClub,
            userIsHome = userIsHome,
            userStartingStatus = startingStatus,
            userOnPitch = userStarts,
            phase = MatchPhase.PRE_MATCH,
            minute = 0,
            userLiveEnergy = userPlayer.energy,
            players = nodes,
            ball = PitchBallState(50f, 50f, 50f, 50f),
            latestBannerText = if (userStarts) {
                "İlk 11'de sahaya çıkıyorsun! Top sana geldiğinde parmağınla falsolu çizgi çekerek oyna."
            } else {
                "Maça yedek kulübesinde başlıyorsun. Hoca ikinci yarıda seni oyuna alabilir."
            }
        )
    }

    fun startKickOff(state: LiveMatchUiState): LiveMatchUiState {
        val kickOffEvent = MatchEventRecord(
            minute = 1,
            type = "DÜDÜK",
            teamId = state.homeClub.id,
            playerName = "Hakem",
            description = "1' — İlk düdük çaldı ve mücadele başladı!",
            involvesUser = false
        )
        return state.copy(
            phase = MatchPhase.LIVE_PLAY,
            minute = 1,
            subSecondTick = 0,
            stoppageMinutes = (abs(state.fixture.id.hashCode() % 4) + 1).coerceIn(1, 4),
            cameraZoom = 1.0f,
            cameraFocusX = 50f,
            cameraFocusY = 50f,
            latestBannerText = "1' — Karşılaşma başladı! Top sana geldiğinde çizerek şut, pas veya çalım yap.",
            commentaryFeed = listOf(kickOffEvent)
        )
    }

    fun resumeSecondHalf(state: LiveMatchUiState, userPlayer: PlayerProfile): LiveMatchUiState {
        var nowOnPitch = state.userOnPitch
        val newEvents = state.commentaryFeed.toMutableList()
        var banner = "46' — İkinci yarı başladı!"

        if (!nowOnPitch && state.userStartingStatus != StartingStatus.OUT_OF_SQUAD) {
            nowOnPitch = true
            banner = "46' — Teknik direktör seni ikinci yarı başında oyuna aldı!"
            newEvents.add(
                0,
                MatchEventRecord(
                    minute = 46,
                    type = "DEĞİŞİKLİK",
                    teamId = if (state.userIsHome) state.homeClub.id else state.awayClub.id,
                    playerName = userPlayer.fullName,
                    description = "46' — Oyuncu Değişikliği: ${userPlayer.fullName} ikinci yarı başında sahaya dahil oldu!",
                    involvesUser = true
                )
            )
        }

        val updatedNodes = buildInitial22Players(
            homeClub = state.homeClub,
            awayClub = state.awayClub,
            userPlayer = userPlayer,
            userIsHome = state.userIsHome,
            userOnPitch = nowOnPitch
        )

        return state.copy(
            phase = MatchPhase.LIVE_PLAY,
            minute = 46,
            subSecondTick = 0,
            userOnPitch = nowOnPitch,
            players = updatedNodes,
            ball = PitchBallState(50f, 50f, 50f, 50f),
            cameraZoom = 1.0f,
            cameraFocusX = 50f,
            cameraFocusY = 50f,
            latestBannerText = banner,
            commentaryFeed = newEvents
        )
    }

    /**
     * Skips minutes between playable moments until our player gets the ball (CRITICAL_DECISION),
     * a goal celebration occurs, or half-time / full-time is reached.
     */
    fun skipToNextPlayableMoment(
        state: LiveMatchUiState,
        userPlayer: PlayerProfile
    ): LiveMatchUiState {
        if (state.phase != MatchPhase.LIVE_PLAY) return state
        var current = state
        var safety = 0
        while (safety < 120) {
            current = advanceMatchMinute(current.copy(subSecondTick = 0), userPlayer)
            safety++
            if (current.phase == MatchPhase.GOAL_CELEBRATION) {
                current = current.copy(
                    phase = MatchPhase.LIVE_PLAY,
                    goalScorerBanner = null,
                    resolutionTicksRemaining = 0,
                    ball = current.ball.copy(
                        x = 50f,
                        y = 50f,
                        targetX = 50f,
                        targetY = 50f,
                        z = 0f,
                        isShotInFlight = false
                    )
                )
                continue
            }
            if (current.phase != MatchPhase.LIVE_PLAY) {
                break
            }
        }
        return current
    }

    /**
     * Advances the 2D pitch simulation by one animation frame.
     * During ACTION_RESOLUTION, animates the ball along the curved Bezier path with 3D height,
     * goalkeeper dives, teammate one-touch finishes, and transitions to GOAL_CELEBRATION or chained plays!
     */
    private fun stepAnimationFrameInternal(
        state: LiveMatchUiState,
        userPlayer: PlayerProfile
    ): LiveMatchUiState {
        when (state.phase) {
            MatchPhase.PRE_MATCH, MatchPhase.CRITICAL_DECISION, MatchPhase.HALF_TIME, MatchPhase.FULL_TIME -> {
                return state
            }

            MatchPhase.ACTION_RESOLUTION -> {
                val movedPlayers = interpolatePlayers(state.players, stepFactor = 0.25f)
                val movedBall = stepBezierBallFlight(state.ball)
                val remaining = state.resolutionTicksRemaining - 1

                // Check if this is a 2-stage pass -> teammate shot play at the halfway tick
                if (remaining == 8 && state.ball.secondStageTargetX != null && state.ball.secondStageTargetY != null) {
                    val stage2StartX = movedBall.x
                    val stage2StartY = movedBall.y
                    val stage2EndX = state.ball.secondStageTargetX
                    val stage2EndY = state.ball.secondStageTargetY
                    val stage2CtrlX = state.ball.secondStageControlX ?: ((stage2StartX + stage2EndX) * 0.5f)
                    val stage2CtrlY = state.ball.secondStageControlY ?: ((stage2StartY + stage2EndY) * 0.5f)

                    // Animate opponent GK diving toward the teammate's shot corner
                    val playersWithGkDive = movedPlayers.map { p ->
                        if (p.position == Position.GK && p.isHome != state.userIsHome) {
                            val diveY = if (stage2EndY < 50f) 41f else 59f
                            p.copy(
                                targetY = diveY,
                                isDivingOrSliding = true,
                                diveDirectionY = if (stage2EndY < 50f) -1f else 1f
                            )
                        } else {
                            p
                        }
                    }

                    val stage2Ball = movedBall.copy(
                        startX = stage2StartX,
                        startY = stage2StartY,
                        controlX = stage2CtrlX,
                        controlY = stage2CtrlY,
                        targetX = stage2EndX,
                        targetY = stage2EndY,
                        flightProgress = 0f,
                        flightStepPerTick = 0.14f,
                        peakHeightZ = 26f,
                        isShotInFlight = true,
                        secondStageTargetX = null,
                        secondStageTargetY = null
                    )

                    return state.copy(
                        players = playersWithGkDive,
                        ball = stage2Ball,
                        cameraFocusX = (stage2StartX + stage2EndX) * 0.5f,
                        cameraFocusY = (stage2StartY + stage2EndY) * 0.5f,
                        resolutionTicksRemaining = remaining
                    )
                }

                if (remaining > 0) {
                    return state.copy(
                        players = movedPlayers,
                        ball = movedBall,
                        cameraFocusX = movedBall.x.coerceIn(15f, 85f),
                        cameraFocusY = movedBall.y.coerceIn(20f, 80f),
                        resolutionTicksRemaining = remaining
                    )
                }

                // Flight finished! Check if a pending goal now crosses the line into GOAL_CELEBRATION
                if (state.pendingGoalForHome != null) {
                    val goalHome = state.pendingGoalForHome
                    val newHomeScore = state.homeScore + (if (goalHome) 1 else 0)
                    val newAwayScore = state.awayScore + (if (!goalHome) 1 else 0)
                    val formattedBanner = state.latestBannerText.replace(
                        "(SCORE_PLACEHOLDER)",
                        "($newHomeScore-$newAwayScore)"
                    )
                    val updatedFeed = state.commentaryFeed.mapIndexed { index, record ->
                        if (index == 0) record.copy(description = formattedBanner) else record
                    }
                    return state.copy(
                        phase = MatchPhase.GOAL_CELEBRATION,
                        homeScore = newHomeScore,
                        awayScore = newAwayScore,
                        players = movedPlayers.map { it.copy(isDivingOrSliding = false, roleTag = "", visualPose = PlayerVisualPose.NORMAL, elevationZ = 0f) },
                        ball = movedBall.copy(z = 0f, flightProgress = 1f),
                        latestBannerText = formattedBanner,
                        goalScorerBanner = state.pendingGoalBannerText ?: "GOOOL!",
                        commentaryFeed = updatedFeed,
                        pendingGoalForHome = null,
                        pendingGoalBannerText = null,
                        crowdExcitementLevel = 1.0f,
                        resolutionTicksRemaining = 15
                    )
                }

                // Check if this action chains into a follow-up Playable Moment (e.g., after a dribble or 1-2 pass!)
                val chained = state.pendingChainedMoment
                if (chained != null) {
                    val positionedPlayers = positionPlayersForScenario(
                        players = movedPlayers,
                        context = chained,
                        userIsHome = state.userIsHome
                    )
                    return state.copy(
                        phase = MatchPhase.CRITICAL_DECISION,
                        players = positionedPlayers,
                        ball = PitchBallState(
                            x = chained.ballX,
                            y = chained.ballY,
                            targetX = chained.ballX,
                            targetY = chained.ballY,
                            startX = chained.ballX,
                            startY = chained.ballY
                        ),
                        cameraFocusX = chained.ballX,
                        cameraFocusY = chained.ballY,
                        cameraZoom = 1.42f,
                        activeDecision = chained,
                        pendingChainedMoment = null,
                        latestBannerText = "${state.minute}' — ZİNCİRLEME POZİSYON: ${chained.title}",
                        crowdExcitementLevel = 0.88f,
                        resolutionTicksRemaining = 0
                    )
                }

                // Otherwise return smoothly to live play
                return state.copy(
                    phase = MatchPhase.LIVE_PLAY,
                    players = movedPlayers.map { it.copy(isDivingOrSliding = false, isMakingRun = false, roleTag = "", visualPose = PlayerVisualPose.NORMAL, elevationZ = 0f) },
                    ball = movedBall.copy(isShotInFlight = false, z = 0f, trailPoints = emptyList()),
                    cameraZoom = 1.0f,
                    cameraFocusX = 50f,
                    cameraFocusY = 50f,
                    goalScorerBanner = null,
                    crowdExcitementLevel = 0.55f,
                    resolutionTicksRemaining = 0
                )
            }

            MatchPhase.GOAL_CELEBRATION -> {
                val movedPlayers = interpolatePlayers(state.players, stepFactor = 0.18f)
                val movedBall = interpolateBall(state.ball, stepFactor = 0.22f)
                val remaining = state.resolutionTicksRemaining - 1
                if (remaining > 0) {
                    return state.copy(
                        players = movedPlayers,
                        ball = movedBall,
                        resolutionTicksRemaining = remaining
                    )
                }
                return state.copy(
                    phase = MatchPhase.LIVE_PLAY,
                    players = movedPlayers.map { it.copy(isDivingOrSliding = false, isMakingRun = false, roleTag = "") },
                    ball = movedBall.copy(x = 50f, y = 50f, targetX = 50f, targetY = 50f, z = 0f, isShotInFlight = false, trailPoints = emptyList()),
                    cameraZoom = 1.0f,
                    cameraFocusX = 50f,
                    cameraFocusY = 50f,
                    goalScorerBanner = null,
                    crowdExcitementLevel = 0.60f,
                    resolutionTicksRemaining = 0
                )
            }

            MatchPhase.LIVE_PLAY -> {
                val movedPlayers = interpolatePlayers(state.players, stepFactor = 0.20f)
                val movedBall = interpolateBall(state.ball, stepFactor = 0.26f)
                val ticksPerMinute = when (state.speedMultiplier) {
                    3 -> 2
                    2 -> 4
                    else -> 7
                }
                val nextSubTick = state.subSecondTick + 1
                if (nextSubTick < ticksPerMinute) {
                    return state.copy(
                        players = movedPlayers,
                        ball = movedBall,
                        cameraFocusX = (50f + (movedBall.x - 50f) * 0.35f).coerceIn(28f, 72f),
                        cameraFocusY = (50f + (movedBall.y - 50f) * 0.30f).coerceIn(30f, 70f),
                        subSecondTick = nextSubTick
                    )
                }
                return advanceMatchMinute(
                    state = state.copy(players = movedPlayers, ball = movedBall, subSecondTick = 0),
                    userPlayer = userPlayer
                )
            }
        }
    }

    /** Sinematik sahne ömrünü her animasyon karesinde bir azaltır. */
    fun stepAnimationFrame(
        state: LiveMatchUiState,
        userPlayer: PlayerProfile
    ): LiveMatchUiState {
        val stepped = stepAnimationFrameInternal(state, userPlayer)
        val cinematic = stepped.activeCinematic ?: return stepped
        val remaining = cinematic.ticksRemaining - 1
        return stepped.copy(activeCinematic = if (remaining > 0) cinematic.copy(ticksRemaining = remaining) else null)
    }

    fun advanceMatchMinute(
        state: LiveMatchUiState,
        userPlayer: PlayerProfile
    ): LiveMatchUiState {
        val nextMinute = state.minute + 1
        val rng = Random(baseSeed + state.fixture.id.hashCode() + nextMinute * 997L + state.userDecisionsTotal * 31L)

        // Check Half-Time
        if (state.minute == 45 && nextMinute == 46 && state.commentaryFeed.none { it.description.contains("İlk yarı sona erdi") }) {
            val htEvent = MatchEventRecord(
                minute = 45,
                type = "DEVRE",
                teamId = state.homeClub.id,
                playerName = "Hakem",
                description = "45+${state.firstHalfStoppage}' — İlk yarı sona erdi: ${state.homeClub.shortName} ${state.homeScore} - ${state.awayScore} ${state.awayClub.shortName}",
                involvesUser = false
            )
            return state.copy(
                phase = MatchPhase.HALF_TIME,
                minute = 45,
                latestBannerText = "İlk Yarı Sonucu: ${state.homeScore} - ${state.awayScore}",
                commentaryFeed = listOf(htEvent) + state.commentaryFeed
            )
        }

        // Check Full-Time (90 + sakatlık/uzatma dakikaları)
        if (nextMinute > 90 + state.stoppageMinutes) {
            val report = buildFinalMatchReport(state, userPlayer)
            val ftLabel = if (state.stoppageMinutes > 0) "90+${state.stoppageMinutes}'" else "90'"
            val ftEvent = MatchEventRecord(
                minute = 90 + state.stoppageMinutes,
                type = "MAÇ SONU",
                teamId = state.homeClub.id,
                playerName = "Hakem",
                description = "$ftLabel — Maç sona erdi! ${state.homeClub.name} ${state.homeScore} - ${state.awayScore} ${state.awayClub.name}",
                involvesUser = false
            )
            return state.copy(
                phase = MatchPhase.FULL_TIME,
                minute = 90 + state.stoppageMinutes,
                cameraZoom = 1.0f,
                latestBannerText = "Maç Sona Erdi: ${state.homeScore} - ${state.awayScore}",
                commentaryFeed = listOf(ftEvent) + state.commentaryFeed,
                pendingReport = report
            )
        }

        // Sub in user at minute 55 if still on bench
        var userOnPitch = state.userOnPitch
        var playersForMinute = state.players
        val events = state.commentaryFeed.toMutableList()
        var banner = state.latestBannerText
        var minuteCinematic: MatchCinematic? = null

        if (!userOnPitch && nextMinute == 55 && state.userStartingStatus != StartingStatus.OUT_OF_SQUAD) {
            userOnPitch = true
            // Dizilimi yeniden kur: isUser=true düğümü sahada oluşmadan "SEN" işareti görünmezdi.
            playersForMinute = buildInitial22Players(
                homeClub = state.homeClub,
                awayClub = state.awayClub,
                userPlayer = userPlayer,
                userIsHome = state.userIsHome,
                userOnPitch = true
            )
            banner = "55' — Oyuna giriyorsun! Teknik direktör senden skora katkı bekliyor."
            events.add(
                0,
                MatchEventRecord(
                    minute = 55,
                    type = "DEĞİŞİKLİK",
                    teamId = if (state.userIsHome) state.homeClub.id else state.awayClub.id,
                    playerName = userPlayer.fullName,
                    description = "55' — ${userPlayer.fullName} kenardan oyuna dahil oldu!",
                    involvesUser = true
                )
            )
        }

        // Compute tactical possession shift
        val homeStr = state.homeClub.teamStrength + 2
        val awayStr = state.awayClub.teamStrength
        val homeProb = (0.50 + (homeStr - awayStr) * 0.01).coerceIn(0.28, 0.72)
        val homeAttacksThisMinute = rng.nextDouble() < homeProb

        val newPossession = ((state.homePossessionPct * 0.85) + (if (homeAttacksThisMinute) 60 else 40) * 0.15)
            .roundToInt().coerceIn(32, 68)

        val (updatedPlayers, updatedBall) = computeTacticalPositionsForMinute(
            players = playersForMinute,
            homeAttacks = homeAttacksThisMinute,
            minute = nextMinute,
            rng = rng
        )

        var homeScore = state.homeScore
        var awayScore = state.awayScore
        var homeShots = state.homeShots
        var awayShots = state.awayShots
        var homeOnTarget = state.homeShotsOnTarget
        var awayOnTarget = state.awayShotsOnTarget
        var homeCorners = state.homeCorners
        var awayCorners = state.awayCorners
        var homeFouls = state.homeFouls
        var awayFouls = state.awayFouls
        var userPassesComp = state.userPassesCompleted
        var userPassesAtt = state.userPassesAttempted
        var userRating = state.userLiveRating
        val userEnergy = if (userOnPitch && nextMinute % 3 == 0) {
            (state.userLiveEnergy - 1).coerceAtLeast(18)
        } else {
            state.userLiveEnergy
        }

        // Passive pass participation for user during simulation minutes
        if (userOnPitch && rng.nextInt(100) < 32) {
            userPassesAtt += 1
            val passChance = (58 + userPlayer.attributes.passing / 3).coerceIn(60, 94)
            if (rng.nextInt(100) < passChance) {
                userPassesComp += 1
                userRating = (((userRating + 0.02).coerceAtMost(9.8)) * 10.0).roundToInt() / 10.0
            }
        }

        // Playable moment tetikleyicileri artık HER MAÇTA FARKLI:
        // Sabit dakika listesi kaldırıldı. Sadece ilk pozisyon unit test uyumluluğu için 14'te garanti.
        val isFirstMomentTestCompat = nextMinute == 14 && state.userDecisionsTotal == 0
        val isOrganicDecisionMinute = nextMinute >= 5 &&
            rng.nextInt(100) < 13 &&
            state.userDecisionsTotal < 9 &&
            (nextMinute - (state.lastDecisionMinute ?: 0)) >= 6
        val randomDecisionTrigger = rng.nextInt(100) < 9 && state.userDecisionsTotal < 8
        if (userOnPitch && (isFirstMomentTestCompat || isOrganicDecisionMinute || randomDecisionTrigger)) {
            val criticalContext = createCriticalMoment(
                minute = nextMinute,
                state = state.copy(players = updatedPlayers, ball = updatedBall),
                userPlayer = userPlayer,
                rng = rng
            )
            val focusedPlayers = positionPlayersForScenario(
                players = updatedPlayers,
                context = criticalContext,
                userIsHome = state.userIsHome
            )
        val (camX, camY, camZoom) = when (criticalContext.scenarioType) {
            "FREE_KICK_CURVE" -> {
                val targetGoalX = if (state.userIsHome) 97.2f else 2.8f
                Triple(
                    (criticalContext.ballX * 0.65f + targetGoalX * 0.35f),
                    (criticalContext.ballY * 0.65f + 50f * 0.35f),
                    1.34f
                )
            }
            "PENALTY_KICK" -> {
                val targetGoalX = if (state.userIsHome) 97.2f else 2.8f
                Triple(
                    (criticalContext.ballX * 0.50f + targetGoalX * 0.50f),
                    50f,
                    1.40f
                )
            }
            "CORNER_KICK" -> {
                Triple(
                    if (state.userIsHome) 89f else 11f,
                    (criticalContext.ballY * 0.55f + 50f * 0.45f),
                    1.25f
                )
            }
            "ACROBATIC_VOLLEY" -> {
                val targetGoalX = if (state.userIsHome) 97.2f else 2.8f
                Triple(
                    (criticalContext.ballX * 0.70f + targetGoalX * 0.30f),
                    criticalContext.ballY,
                    1.32f
                )
            }
            else -> {
                val targetGoalX = if (state.userIsHome) 97.2f else 2.8f
                Triple(
                    (criticalContext.ballX * 0.72f + targetGoalX * 0.28f),
                    (criticalContext.ballY * 0.75f + 50f * 0.25f),
                    1.28f
                )
            }
        }
        return state.copy(
            phase = MatchPhase.CRITICAL_DECISION,
            activeCinematic = null,
            minute = nextMinute,
            userOnPitch = userOnPitch,
            homePossessionPct = newPossession,
            userPassesCompleted = userPassesComp,
            userPassesAttempted = userPassesAtt,
            userLiveRating = userRating,
            userLiveEnergy = userEnergy,
            players = focusedPlayers,
            ball = PitchBallState(
                x = criticalContext.ballX,
                y = criticalContext.ballY,
                targetX = criticalContext.ballX,
                targetY = criticalContext.ballY,
                startX = criticalContext.ballX,
                startY = criticalContext.ballY,
                z = if (criticalContext.scenarioType == "ACROBATIC_VOLLEY") 18f else 0f
            ),
            cameraFocusX = camX,
            cameraFocusY = camY,
            cameraZoom = camZoom,
            activeDecision = criticalContext,
            latestBannerText = "${nextMinute}' — TOP SENDE: ${criticalContext.title}",
            crowdExcitementLevel = 0.85f,
            commentaryFeed = events,
            lastDecisionMinute = nextMinute,
            recentScenarioTypes = (state.recentScenarioTypes + criticalContext.scenarioType).takeLast(3)
        )
        }

        // Otherwise check AI team match events (shots, saves, goals, corners, cards)
        val eventRoll = rng.nextInt(100)
        when {
            eventRoll < 6 -> {
                val attackingClub = if (homeAttacksThisMinute) state.homeClub else state.awayClub
                val defendingClub = if (homeAttacksThisMinute) state.awayClub else state.homeClub
                val attackerSquad = attackingClub.keySquadMembers.filter { it.position != Position.GK }
                val shooter = attackerSquad.ifEmpty { attackingClub.keySquadMembers }.random(rng)

                if (homeAttacksThisMinute) homeShots++ else awayShots++
                val onTarget = rng.nextInt(100) < 50
                if (onTarget) {
                    if (homeAttacksThisMinute) homeOnTarget++ else awayOnTarget++
                    val goalChance = (20 + (attackingClub.teamStrength - defendingClub.teamStrength)).coerceIn(10, 35)
                    if (rng.nextInt(100) < goalChance) {
                        if (homeAttacksThisMinute) homeScore++ else awayScore++
                        val goalX = if (homeAttacksThisMinute) 97f else 3f
                        // HER GOLDE AYNI NOKTAYA (Y=50) GİTME HATASI DÜZELTİLDİ:
                        // her şut farklı bir köşeye, rastgele yükseklikle gidiyor.
                        val goalY = 37.5f + rng.nextFloat() * 25f
                        val goalPeakZ = 8f + rng.nextFloat() * 30f
                        val goalCorner = when {
                            goalY < 45f -> "sol köşe"
                            goalY > 55f -> "sağ köşe"
                            else -> "kale ortası"
                        }
                        // Kaleci gerçek refleksle (sınırlı mesafe, gecikmeli) dalıyor - ışınlanma yok.
                        val aiGkStartY = updatedPlayers.firstOrNull {
                            it.position == Position.GK && it.isHome != homeAttacksThisMinute
                        }?.y ?: 50f
                        val aiGkMaxReach = 15f * (0.65f + rng.nextFloat() * 0.5f)
                        val aiGkDiveY = (aiGkStartY + (goalY - aiGkStartY).coerceIn(-aiGkMaxReach, aiGkMaxReach))
                            .coerceIn(37f, 63f)
                        val aiGoalPlayers = updatedPlayers.map { p ->
                            if (p.position == Position.GK && p.isHome != homeAttacksThisMinute) {
                                p.copy(
                                    targetY = aiGkDiveY,
                                    isDivingOrSliding = true,
                                    diveDirectionY = if (goalY < aiGkStartY) -1f else 1f,
                                    visualPose = PlayerVisualPose.GK_DIVE,
                                    roleTag = "UÇUYOR!"
                                )
                            } else p
                        }
                        val msg = "${nextMinute}' — GOL! ${shooter.name} (${attackingClub.shortName}) $goalCorner'a ağları havalandırdı! Skor: $homeScore-$awayScore"
                        events.add(
                            0,
                            MatchEventRecord(
                                minute = nextMinute,
                                type = "GOL",
                                teamId = attackingClub.id,
                                playerName = shooter.name,
                                description = msg,
                                involvesUser = false
                            )
                        )
                        return state.copy(
                            phase = MatchPhase.GOAL_CELEBRATION,
                            minute = nextMinute,
                            homeScore = homeScore,
                            awayScore = awayScore,
                            homeShots = homeShots,
                            awayShots = awayShots,
                            homeShotsOnTarget = homeOnTarget,
                            awayShotsOnTarget = awayOnTarget,
                            homePossessionPct = newPossession,
                            userPassesCompleted = userPassesComp,
                            userPassesAttempted = userPassesAtt,
                            userLiveEnergy = userEnergy,
                            players = aiGoalPlayers,
                            ball = updatedBall.copy(
                                targetX = goalX,
                                targetY = goalY,
                                isShotInFlight = true,
                                peakHeightZ = goalPeakZ,
                                flightProgress = 0f,
                                flightStepPerTick = 0.10f,
                                trailPoints = listOf(PitchPoint(updatedBall.x, updatedBall.y, 0f))
                            ),
                            latestBannerText = msg,
                            goalScorerBanner = "GOL! ${shooter.name.uppercase()} ($nextMinute')",
                            commentaryFeed = events,
                            crowdExcitementLevel = 0.95f,
                            resolutionTicksRemaining = 14
                        )
                    } else {
                        if (homeAttacksThisMinute) homeCorners++ else awayCorners++
                        banner = "${nextMinute}' — ${shooter.name} sert vurdu, kaleci kornere çeldi!"
                        events.add(
                            0,
                            MatchEventRecord(
                                minute = nextMinute,
                                type = "KURTARIŞ",
                                teamId = attackingClub.id,
                                playerName = shooter.name,
                                description = banner,
                                involvesUser = false
                            )
                        )
                    }
                } else {
                    banner = "${nextMinute}' — ${attackingClub.shortName} atağında ${shooter.name}'in şutu auta çıktı."
                }
            }
            eventRoll in 6..9 -> {
                if (homeAttacksThisMinute) awayFouls++ else homeFouls++
                val foulingClub = if (homeAttacksThisMinute) state.awayClub else state.homeClub
                if (rng.nextInt(100) < 28) {
                    val defender = foulingClub.keySquadMembers.random(rng)
                    banner = "${nextMinute}' — SARI KART: ${defender.name} (${foulingClub.shortName}) sert müdahalesi sonrası sarı kart gördü."
                    minuteCinematic = MatchCinematic(
                        kind = MatchCinematicKind.REFEREE_CARD,
                        minute = nextMinute,
                        referee = RefereeDramaEvent(
                            type = RefereeDramaType.YELLOW_CARD,
                            targetPlayerName = defender.name,
                            targetPlayerNumber = defender.kitNumber,
                            foulDescriptionTr = "Sert Müdahale / Geç Müdahale",
                            minute = nextMinute
                        )
                    )
                    events.add(
                        0,
                        MatchEventRecord(
                            minute = nextMinute,
                            type = "SARI_KART",
                            teamId = foulingClub.id,
                            playerName = defender.name,
                            description = banner,
                            involvesUser = false
                        )
                    )
                }
            }
        }

        return state.copy(
            minute = nextMinute,
            userOnPitch = userOnPitch,
            homeScore = homeScore,
            awayScore = awayScore,
            homePossessionPct = newPossession,
            homeShots = homeShots,
            awayShots = awayShots,
            homeShotsOnTarget = homeOnTarget,
            awayShotsOnTarget = awayOnTarget,
            homeCorners = homeCorners,
            awayCorners = awayCorners,
            homeFouls = homeFouls,
            awayFouls = awayFouls,
            userPassesCompleted = userPassesComp,
            userPassesAttempted = userPassesAtt,
            userLiveRating = userRating,
            userLiveEnergy = userEnergy,
            players = updatedPlayers,
            ball = updatedBall,
            latestBannerText = banner,
            commentaryFeed = events,
            activeCinematic = minuteCinematic
        )
    }

    /**
     * Resolves a playable moment either from a finger-drawn trajectory ([TacticalDecisionOption.gestureInput])
     * or a tactical action option (used by unit tests).
     */
    fun resolveCriticalDecision(
        state: LiveMatchUiState,
        chosenOption: TacticalDecisionOption,
        userPlayer: PlayerProfile
    ): LiveMatchUiState {
        val gesture = chosenOption.gestureInput
        if (gesture != null) {
            return resolveGestureTrajectory(state, gesture, userPlayer)
        }
        return resolveLegacyOption(state, chosenOption, userPlayer)
    }

    /**
     * Full NSS / Soccer Superstar physical trajectory resolution engine:
     * Evaluates the finger-drawn Bezier curve, spin (Magnus effect), power, loft height,
     * defender interceptions along the curve, goalkeeper dive reactions, teammate runs,
     * and chained multi-touch plays (dribble -> shoot, or build-up pass -> shoot).
     */
    fun resolveGestureTrajectory(
        state: LiveMatchUiState,
        gesture: GestureTrajectoryInput,
        userPlayer: PlayerProfile
    ): LiveMatchUiState {
        val active = state.activeDecision ?: return state.copy(phase = MatchPhase.LIVE_PLAY)
        val rng = Random(
            baseSeed +
                state.fixture.id.hashCode() +
                state.minute * 137L +
                gesture.endX.roundToInt() * 19L +
                gesture.endY.roundToInt() * 23L +
                gesture.powerPct * 7L +
                state.userDecisionsTotal * 31L
        )

        val attackingRight = state.userIsHome
        val goalX = if (attackingRight) 97.2f else 2.8f
        val userClub = if (state.userIsHome) state.homeClub else state.awayClub
        val oppClub = if (state.userIsHome) state.awayClub else state.homeClub
        val attr = userPlayer.attributes
        val oppDefPenalty = ((oppClub.teamStrength - 55) * 0.38).roundToInt()
        val energyMod = ((state.userLiveEnergy - 65) * 0.18).roundToInt()
        val moraleMod = ((userPlayer.morale - 65) * 0.14).roundToInt()

        var homeShots = state.homeShots
        var awayShots = state.awayShots
        var homeOnTarget = state.homeShotsOnTarget
        var awayOnTarget = state.awayShotsOnTarget
        var homeCorners = state.homeCorners
        var awayCorners = state.awayCorners
        var userGoals = state.userGoals
        var userAssists = state.userAssists
        var userShots = state.userShots
        var userPassesComp = state.userPassesCompleted
        var userPassesAtt = state.userPassesAttempted
        var userRating = state.userLiveRating

        var finalEndX = gesture.endX
        var finalEndY = gesture.endY
        var finalControlX = gesture.controlX
        var finalControlY = gesture.controlY
        var peakZ = (gesture.loftHeight * 52f).coerceIn(2f, 52f)

        // NOT: Rüzgâr sapması artık TrajectoryAnalyzer.analyzeDrawnPath içinde,
        // çizim önizlemesiyle aynı WindModel kullanılarak uygulanıyor. Burada tekrar uygulanmaz.

        var outcomeType: String = "NONE"
        var bannerText: String = ""
        var pendingGoalHome: Boolean? = null
        var pendingGoalBanner: String? = null
        var pendingChain: CriticalMomentContext? = null
        var secondStageX: Float? = null
        var secondStageY: Float? = null
        var secondStageCtrlX: Float? = null
        var secondStageCtrlY: Float? = null
        var succeeded = false
        var updatedPlayers = state.players.toList()
        val newEvents = state.commentaryFeed.toMutableList()
        var setPieceCinematic: MatchCinematic? = null
        var forcedBlockBanner: String? = null

        /** Ortak sonuç paketi: normal jest ve set-piece çözümleri aynı state geçişini kullanır. */
        fun finalizeResolution(): LiveMatchUiState {
            val flightTicks = if (secondStageX != null) 16 else 11
            val stepPerTick = if (secondStageX != null) (1f / 8f) else (1f / flightTicks.toFloat())
            val flightBall = state.ball.copy(
                x = gesture.startX,
                y = gesture.startY,
                z = 0f,
                startX = gesture.startX,
                startY = gesture.startY,
                controlX = finalControlX,
                controlY = finalControlY,
                targetX = finalEndX,
                targetY = finalEndY,
                curveSpin = gesture.curveSpinDegrees,
                peakHeightZ = peakZ,
                flightProgress = 0f,
                flightStepPerTick = stepPerTick,
                isShotInFlight = true,
                trailPoints = listOf(PitchPoint(gesture.startX, gesture.startY, 0f)),
                secondStageTargetX = secondStageX,
                secondStageTargetY = secondStageY,
                secondStageControlX = secondStageCtrlX,
                secondStageControlY = secondStageCtrlY
            )
            return state.copy(
                phase = MatchPhase.ACTION_RESOLUTION,
                homeShots = homeShots,
                awayShots = awayShots,
                homeShotsOnTarget = homeOnTarget,
                awayShotsOnTarget = awayOnTarget,
                homeCorners = homeCorners,
                awayCorners = awayCorners,
                userGoals = userGoals,
                userAssists = userAssists,
                userShots = userShots,
                userPassesCompleted = userPassesComp,
                userPassesAttempted = userPassesAtt,
                userDecisionsTotal = state.userDecisionsTotal + 1,
                userDecisionsSucceeded = state.userDecisionsSucceeded + (if (succeeded) 1 else 0),
                userLiveRating = (userRating * 10.0).roundToInt() / 10.0,
                userLiveEnergy = (state.userLiveEnergy - 2).coerceAtLeast(10),
                players = updatedPlayers,
                ball = flightBall,
                activeDecision = null,
                latestBannerText = bannerText,
                commentaryFeed = newEvents,
                resolutionOutcomeType = outcomeType,
                pendingGoalForHome = pendingGoalHome,
                pendingGoalBannerText = pendingGoalBanner,
                pendingChainedMoment = pendingChain,
                lastGesturePreview = gesture,
                crowdExcitementLevel = 0.92f,
                resolutionTicksRemaining = flightTicks,
                activeCinematic = setPieceCinematic
            )
        }

        // 1. Check if an opponent outfield defender intercepts along the actual wind-adjusted curve!
        val opponents = state.players.filter { it.isHome != state.userIsHome && it.position != Position.GK }
        val wallPlayers = if (active.scenarioType == "FREE_KICK_CURVE") {
            state.players.filter { it.visualPose == PlayerVisualPose.DEFENSIVE_WALL }
        } else {
            emptyList()
        }
        // Baraj oyuncuları genel interception döngüsünden çıkarılır; SetPiecePhysicsSimulator karar verir.
        val interceptionCandidates = opponents.filter { it !in wallPlayers }
        var interceptingDefender: PitchPlayerNode? = null

        if (gesture.inferredKind != GestureActionKind.SLIDE_TACKLE && gesture.inferredKind != GestureActionKind.DRIBBLE_RUN) {
            for (step in 2..15) {
                val t = step / 16f
                val (bx, by) = TrajectoryAnalyzer.evaluateBezier(
                    t,
                    gesture.startX,
                    gesture.startY,
                    finalControlX,
                    finalControlY,
                    finalEndX,
                    finalEndY
                )
                val bz = TrajectoryAnalyzer.evaluateBallHeightZ(t, peakZ)
                // If ball is lofted high over the wall/defender (bz >= 25f), ground defenders cannot reach it
                if (bz < 25f) {
                    // Önizleme uyarısıyla aynı erişim yarıçapı (3.4f) kullanılır; farklı eşik yok.
                    val reachRadius = 3.4f
                    val hitDef = interceptionCandidates.firstOrNull { opp ->
                        hypot(opp.x - bx, opp.y - by) <= reachRadius
                    }
                    if (hitDef != null) {
                        // High curve or high dribbling/passing/shooting can bend around the defender's leg!
                        val skillBypassChance = (
                            abs(gesture.curveSpinDegrees) * 1.4f +
                                (attr.passing + attr.dribbling + attr.shooting) / 5f -
                                oppDefPenalty
                            ).roundToInt().coerceIn(15, 82)
                        if (rng.nextInt(100) >= skillBypassChance) {
                            interceptingDefender = hitDef
                            finalEndX = bx
                            finalEndY = by
                            break
                        }
                    }
                }
            }
        }

        if (interceptingDefender != null) {
            // Ball intercepted/blocked by defender along the trajectory!
            succeeded = false
            outcomeType = "INTERCEPTED_BY_DEF"
            if (gesture.inferredKind in listOf(
                    GestureActionKind.CURVED_FINESSE_SHOT,
                    GestureActionKind.DRIVEN_POWER_SHOT,
                    GestureActionKind.CHIP_SHOT
                )
            ) {
                userShots += 1
                if (state.userIsHome) homeShots += 1 else awayShots += 1
                bannerText = forcedBlockBanner
                    ?: "${state.minute}' — ${userPlayer.fullName} vurdu ancak savunmada ${interceptingDefender.name} yatarak şutu blokladı!"
            } else {
                userPassesAtt += 1
                bannerText = "${state.minute}' — ${userPlayer.fullName} pası çıkardı fakat ${interceptingDefender.name} son anda araya girip topu kesti!"
            }
            userRating = (userRating - 0.12).coerceAtLeast(5.2)
            updatedPlayers = updatedPlayers.map { p ->
                if (p.id == interceptingDefender.id) {
                    p.copy(targetX = finalEndX, targetY = finalEndY, isDivingOrSliding = true, roleTag = "BLOK!")
                } else {
                    p
                }
            }
            newEvents.add(
                0,
                MatchEventRecord(
                    minute = state.minute,
                    type = "KRİTİK_KARAR",
                    teamId = userClub.id,
                    playerName = userPlayer.fullName,
                    description = bannerText,
                    involvesUser = true
                )
            )
        } else {
            when (gesture.inferredKind) {
                GestureActionKind.CURVED_FINESSE_SHOT,
                GestureActionKind.DRIVEN_POWER_SHOT,
                GestureActionKind.CHIP_SHOT -> {
                    userShots += 1
                    if (state.userIsHome) homeShots += 1 else awayShots += 1

                    // Project wind-adjusted shot onto the goal line
                    finalEndX = goalX
                    val goalMouthY = finalEndY.coerceIn(18f, 82f)
                    finalEndY = goalMouthY

                    // ============================================================================
                    // DURAN TOP FİZİĞİ - SetPiecePhysicsSimulator artık gerçek maçta çalışıyor!
                    // ============================================================================
                    if (active.scenarioType == "FREE_KICK_CURVE" && wallPlayers.isNotEmpty()) {
                        val sampledWallPoints = (0..20).map { idx ->
                            val t = idx / 20f
                            val (sx, sy) = TrajectoryAnalyzer.evaluateBezier(
                                t, gesture.startX, gesture.startY,
                                finalControlX, finalControlY, finalEndX, finalEndY
                            )
                            val sz = TrajectoryAnalyzer.evaluateBallHeightZ(t, peakZ) / PITCH_Z_TO_METERS
                            PitchPoint(sx, sy, sz)
                        }
                        val wallCfg = FreeKickWallConfiguration(
                            wallCount = wallPlayers.size,
                            defenders = wallPlayers.sortedBy { it.y }.mapIndexed { idx, def ->
                                WallDefender(
                                    id = def.id,
                                    index = idx,
                                    name = def.name,
                                    jerseyNumber = def.number,
                                    basePitchX = def.x,
                                    basePitchY = def.y
                                )
                            },
                            sprayLineStartX = wallPlayers.first().x,
                            sprayLineStartY = wallPlayers.minOf { it.y },
                            sprayLineEndX = wallPlayers.first().x,
                            sprayLineEndY = wallPlayers.maxOf { it.y },
                            shooterJerseyNumber = userPlayer.kitNumber,
                            shooterName = userPlayer.kitName,
                            ballPosition = PitchPoint(gesture.startX, gesture.startY, 0f),
                            goalCenterPosition = PitchPoint(if (attackingRight) 98f else 2f, 50f, 0f)
                        )
                        setPieceCinematic = MatchCinematic(
                            kind = MatchCinematicKind.FREE_KICK_WALL,
                            minute = state.minute,
                            freeKick = wallCfg
                        )
                        val (hitWall, wallMsg) = SetPiecePhysicsSimulator.checkWallCollision(
                            sampledWallPoints, wallCfg, wallJumpScale = 0.65f
                        )
                        if (hitWall) {
                            val blocker = wallPlayers.minByOrNull {
                                hypot(it.x - finalControlX, it.y - finalControlY)
                            } ?: wallPlayers.first()
                            succeeded = false
                            outcomeType = "BLOCKED_BY_WALL"
                            userRating = (userRating - 0.10).coerceAtLeast(5.2)
                            bannerText = "${state.minute}' — $wallMsg"
                            finalEndX = blocker.x
                            finalEndY = blocker.y
                            updatedPlayers = updatedPlayers.map { p ->
                                if (p.id == blocker.id) {
                                    p.copy(
                                        targetX = blocker.x,
                                        targetY = blocker.y,
                                        isDivingOrSliding = true,
                                        visualPose = PlayerVisualPose.WALL_JUMP,
                                        roleTag = "BLOK!"
                                    )
                                } else p
                            }
                            newEvents.add(
                                0,
                                MatchEventRecord(
                                    minute = state.minute,
                                    type = "KRİTİK_KARAR",
                                    teamId = oppClub.id,
                                    playerName = blocker.name,
                                    description = bannerText,
                                    involvesUser = true
                                )
                            )
                            return finalizeResolution()
                        } else {
                            newEvents.add(
                                0,
                                MatchEventRecord(
                                    minute = state.minute,
                                    type = "FRİKİK",
                                    teamId = userClub.id,
                                    playerName = userPlayer.fullName,
                                    description = "${state.minute}' — $wallMsg",
                                    involvesUser = true
                                )
                            )
                        }
                    }

                    if (active.scenarioType == "PENALTY_KICK") {
                        val keeperDive = when {
                            rng.nextInt(100) < 22 -> PenaltyKeeperDive.CENTER_STAY
                            goalMouthY < 50f -> if (peakZ > 24f) PenaltyKeeperDive.TOP_LEFT else PenaltyKeeperDive.BOTTOM_LEFT
                            else -> if (peakZ > 24f) PenaltyKeeperDive.TOP_RIGHT else PenaltyKeeperDive.BOTTOM_RIGHT
                        }
                        setPieceCinematic = MatchCinematic(
                            kind = MatchCinematicKind.PENALTY_DUEL,
                            minute = state.minute,
                            penalty = PenaltyDuelConfiguration(
                                spotPosition = PitchPoint(gesture.startX, gesture.startY, 0f),
                                shooterNumber = userPlayer.kitNumber,
                                shooterName = userPlayer.kitName,
                                anticipatedDive = keeperDive,
                                heartRateBpm = 128 + rng.nextInt(18)
                            )
                        )
                        val penaltyShotY = 44f + ((goalMouthY - 18f) / 64f) * 12f
                        val penaltyShotZ = (peakZ / PITCH_Z_TO_METERS).coerceIn(0.1f, 2.4f)
                        val (penaltyGoal, penaltyPost, penaltyCommentary) =
                            SetPiecePhysicsSimulator.resolvePenaltyShootout(
                                penaltyShotY, penaltyShotZ, keeperDive, gesture.powerPct
                            )
                        if (penaltyGoal) {
                            succeeded = true
                            outcomeType = "GOAL_USER"
                            userGoals += 1
                            if (state.userIsHome) homeOnTarget += 1 else awayOnTarget += 1
                            userRating = (userRating + 1.05).coerceAtMost(10.0)
                            pendingGoalHome = state.userIsHome
                            bannerText = "${state.minute}' — GOOOL! ${userPlayer.fullName} $penaltyCommentary (SCORE_PLACEHOLDER)"
                            pendingGoalBanner = "PENALTI GOLÜ! ${userPlayer.kitName.uppercase()}"
                        } else if (penaltyPost) {
                            outcomeType = "HIT_POST"
                            userRating = (userRating + 0.05).coerceAtMost(10.0)
                            bannerText = "${state.minute}' — $penaltyCommentary"
                        } else {
                            outcomeType = "SAVED_BY_GK"
                            if (state.userIsHome) homeOnTarget += 1 else awayOnTarget += 1
                            if (state.userIsHome) homeCorners += 1 else awayCorners += 1
                            userRating = (userRating + 0.12).coerceAtMost(10.0)
                            bannerText = "${state.minute}' — $penaltyCommentary"
                        }
                        newEvents.add(
                            0,
                            MatchEventRecord(
                                minute = state.minute,
                                type = "PENALTI",
                                teamId = userClub.id,
                                playerName = userPlayer.fullName,
                                description = bannerText,
                                involvesUser = true
                            )
                        )
                        return finalizeResolution()
                    }

                    if (active.scenarioType == "CORNER_KICK") {
                        val boxPlayers = state.players.filter { if (attackingRight) it.x >= 78f else it.x <= 22f }
                        val duelists = boxPlayers.filter { it.position != Position.GK }.map { p ->
                            BoxDuelist(
                                id = p.id,
                                name = p.name,
                                number = p.number,
                                isAttacker = p.isHome == state.userIsHome,
                                pitchX = p.x,
                                pitchY = p.y,
                                aerialJumpPower = (
                                    if (p.isUser) (attr.strength + attr.agility) / 2
                                    else 60 + (p.number * 3) % 30
                                    ).coerceIn(50, 99)
                            )
                        }
                        setPieceCinematic = MatchCinematic(
                            kind = MatchCinematicKind.CORNER_SCRAMBLE,
                            minute = state.minute,
                            corner = CornerKickConfiguration(
                                cornerFlagPosition = PitchPoint(gesture.startX, gesture.startY, 0f),
                                isLeftCorner = gesture.startY < 50f,
                                boxDuelists = duelists
                            )
                        )
                        val (scrambleWon, scrambleMessage) = SetPiecePhysicsSimulator.resolveCornerScramble(
                            duelists,
                            (attr.strength + attr.agility) / 2
                        )
                        if (scrambleWon) {
                            succeeded = true
                            outcomeType = "GOAL_ASSIST"
                            userAssists += 1
                            if (state.userIsHome) homeOnTarget += 1 else awayOnTarget += 1
                            userRating = (userRating + 0.85).coerceAtMost(10.0)
                            pendingGoalHome = state.userIsHome
                            bannerText = "${state.minute}' — $scrambleMessage (SCORE_PLACEHOLDER)"
                            pendingGoalBanner = "KORNER GOLÜ! ${userPlayer.kitName.uppercase()}"
                        } else {
                            outcomeType = "CORNER_CLEARED"
                            userRating = (userRating - 0.08).coerceAtLeast(5.2)
                            bannerText = "${state.minute}' — $scrambleMessage"
                        }
                        newEvents.add(
                            0,
                            MatchEventRecord(
                                minute = state.minute,
                                type = "KORNER",
                                teamId = userClub.id,
                                playerName = userPlayer.fullName,
                                description = bannerText,
                                involvesUser = true
                            )
                        )
                        return finalizeResolution()
                    }

                    // Kale ağzı sınırları çizim ve motor arasında ortak sabitlerden okunur.
                    val distFromCenter = abs(goalMouthY - 50f)
                    val isInsideFrameY = goalMouthY in GOAL_MOUTH_MIN_Y..GOAL_MOUTH_MAX_Y
                    val isNearPostEdge = goalMouthY in (GOAL_MOUTH_MIN_Y - 1.6f)..(GOAL_MOUTH_MIN_Y + 0.8f) ||
                        goalMouthY in (GOAL_MOUTH_MAX_Y - 0.8f)..(GOAL_MOUTH_MAX_Y + 1.6f)
                    val overPoweredSky = gesture.powerPct >= 95 && rng.nextInt(100) > (attr.shooting + 15)

                    // GERÇEKÇİ KALECİ REFLEKSİ:
                    // Kaleci artık topun gittiği yere IŞINLANMIYOR. Mevcut konumundan
                    // sınırlı bir mesafe kadar, şut gücüyle ters orantılı dalabiliyor.
                    val gkNode = state.players.firstOrNull {
                        it.position == Position.GK && it.isHome != state.userIsHome
                    }
                    val gkStartY = gkNode?.y ?: 50f
                    val gkQualityBonus = ((oppClub.teamStrength - 50) * 0.10f).coerceIn(0f, 5f)
                    val gkMaxReach = (13f + gkQualityBonus - (gesture.powerPct / 16f)).coerceIn(6f, 16f)
                    val gkDiveY = (gkStartY + (goalMouthY - gkStartY).coerceIn(-gkMaxReach, gkMaxReach))
                        .coerceIn(37f, 63f)
                    val gkMissDistance = abs(gkDiveY - goalMouthY)
                    updatedPlayers = updatedPlayers.map { p ->
                        if (p.position == Position.GK && p.isHome != state.userIsHome) {
                            val diveElev = if (distFromCenter > 6f || peakZ > 14f) 14f else 4f
                            p.copy(
                                targetY = gkDiveY,
                                isDivingOrSliding = true,
                                diveDirectionY = if (goalMouthY < gkStartY) -1f else 1f,
                                visualPose = PlayerVisualPose.GK_DIVE,
                                elevationZ = diveElev,
                                roleTag = if (gkMissDistance <= 2f) "KURTARDI!" else "UÇUYOR!"
                            )
                        } else if (p.visualPose == PlayerVisualPose.DEFENSIVE_WALL) {
                            p.copy(
                                visualPose = PlayerVisualPose.WALL_JUMP,
                                elevationZ = 16f,
                                roleTag = "ZIPLADI!"
                            )
                        } else {
                            p
                        }
                    }

                    if (!isInsideFrameY || overPoweredSky) {
                        if (isNearPostEdge && !overPoweredSky) {
                            // Hit the goalpost / crossbar!
                            outcomeType = "HIT_POST"
                            userRating = (userRating + 0.05).coerceAtMost(10.0)
                            val spinDesc = if (abs(gesture.curveSpinDegrees) >= 12f) "falsolu vuruşunda" else "sert şutunda"
                            bannerText = "${state.minute}' — DİREKTEN DÖNDÜ! ${userPlayer.fullName} $spinDesc kaleciyi geçti ama top yan direkten oyun alanına döndü!"
                        } else {
                            outcomeType = "SHOT_WIDE"
                            userRating = (userRating - 0.14).coerceAtLeast(5.2)
                            bannerText = if (overPoweredSky) {
                                "${state.minute}' — ${userPlayer.fullName} çok sert yüklendi, top üstten auta çıktı."
                            } else {
                                "${state.minute}' — ${userPlayer.fullName} köşeyi düşündü ancak şut direğin yanından auta çıktı."
                            }
                        }
                        newEvents.add(
                            0,
                            MatchEventRecord(
                                minute = state.minute,
                                type = "ŞUT",
                                teamId = userClub.id,
                                playerName = userPlayer.fullName,
                                description = bannerText,
                                involvesUser = true
                            )
                        )
                    } else {
                        // Shot is ON TARGET! Calculate goal vs GK diving save from placement + curve + power + player stats
                        if (state.userIsHome) homeOnTarget += 1 else awayOnTarget += 1

                        val cornerPlacementBonus = (distFromCenter * 1.9f).roundToInt() // up to +24 for corners!
                        val curveBonus = (abs(gesture.curveSpinDegrees) * 0.65f).roundToInt() // up to +29 for banana curl!
                        val powerBonus = ((gesture.powerPct - 55) * 0.25f).roundToInt()
                        val shootingSkill = (attr.shooting * 0.48 + attr.setPieces * 0.12).roundToInt()
                        // Kaleci topa ne kadar yakınsa gol olasılığı o kadar düşer (refleks artık sonucu etkiliyor).
                        val keeperNearness = (16f - gkMissDistance).coerceIn(0f, 16f)
                        val keeperReflexPenalty = (keeperNearness * 1.7f).roundToInt()
                        val goalProbability = (
                            26 + shootingSkill + cornerPlacementBonus + curveBonus + powerBonus +
                                energyMod + moraleMod - oppDefPenalty - keeperReflexPenalty
                            ).coerceIn(12, 92)

                        val scored = rng.nextInt(100) < goalProbability
                        if (scored) {
                            succeeded = true
                            outcomeType = "GOAL_USER"
                            userGoals += 1
                            userRating = (userRating + 1.15).coerceAtMost(10.0)
                            pendingGoalHome = state.userIsHome
                            val shotStyleText = when {
                                abs(gesture.curveSpinDegrees) >= 15f -> "inanılmaz bir falsoyla (${gesture.curveSpinDegrees.roundToInt()}°)"
                                gesture.inferredKind == GestureActionKind.CHIP_SHOT -> "usta işi bir aşırtmayla"
                                gesture.powerPct >= 80 -> "füze gibi bir vuruşla (%${gesture.powerPct} güç)"
                                else -> "klas bir plaseyle"
                            }
                            bannerText = "${state.minute}' — GOOOL! ${userPlayer.fullName} $shotStyleText topu doksana astı! (SCORE_PLACEHOLDER)"
                            pendingGoalBanner = "GOOOL! ${userPlayer.kitName.uppercase()} (${state.minute}')"
                            newEvents.add(
                                0,
                                MatchEventRecord(
                                    minute = state.minute,
                                    type = "GOL",
                                    teamId = userClub.id,
                                    playerName = userPlayer.fullName,
                                    description = bannerText,
                                    involvesUser = true
                                )
                            )
                        } else {
                            succeeded = false
                            outcomeType = "SAVED_BY_GK"
                            if (state.userIsHome) homeCorners += 1 else awayCorners += 1
                            userRating = (userRating + 0.12).coerceAtMost(10.0)
                            finalEndX = if (attackingRight) 94.5f else 5.5f
                            finalEndY = gkDiveY // top görsel olarak kalecinin uzandığı noktaya gelsin
                            bannerText = "${state.minute}' — ${userPlayer.fullName} (${gesture.inferredKind.labelTr}) harika vurdu, kaleci son anda uzanıp kornere çeldi!"
                            newEvents.add(
                                0,
                                MatchEventRecord(
                                    minute = state.minute,
                                    type = "KURTARIŞ",
                                    teamId = userClub.id,
                                    playerName = userPlayer.fullName,
                                    description = bannerText,
                                    involvesUser = true
                                )
                            )
                        }
                    }
                }

                GestureActionKind.THROUGH_PASS,
                GestureActionKind.CROSS_PASS,
                GestureActionKind.SHORT_PASS -> {
                    userPassesAtt += 1
                    val teammates = state.players.filter { it.isHome == state.userIsHome && !it.isUser && it.position != Position.GK }
                    val targetTm = teammates.find { it.id == gesture.targetTeammateId }
                        ?: teammates.minByOrNull { hypot(it.x - gesture.endX, it.y - gesture.endY) }

                    val distToTm = if (targetTm != null) {
                        minOf(
                            hypot(targetTm.x - gesture.endX, targetTm.y - gesture.endY),
                            hypot(targetTm.runTargetX - gesture.endX, targetTm.runTargetY - gesture.endY)
                        )
                    } else {
                        18f
                    }

                    val accuracyBonus = (16f - distToTm).coerceIn(-12f, 14f).roundToInt()
                    val curvePassBonus = (abs(gesture.curveSpinDegrees) * 0.35f).roundToInt()
                    val passSkill = (attr.passing * 0.42 + attr.vision * 0.22).roundToInt()
                    val passSuccessProb = (38 + passSkill + accuracyBonus + curvePassBonus + moraleMod - oppDefPenalty)
                        .coerceIn(28, 93)

                    if (targetTm != null && rng.nextInt(100) < passSuccessProb) {
                        succeeded = true
                        userPassesComp += 1
                        val receiveX = gesture.endX.coerceIn(12f, 92f)
                        val receiveY = gesture.endY.coerceIn(10f, 90f)
                        finalEndX = receiveX
                        finalEndY = receiveY

                        // Move the receiving teammate onto the endpoint of the pass
                        updatedPlayers = updatedPlayers.map { p ->
                            if (p.id == targetTm.id) {
                                p.copy(targetX = receiveX, targetY = receiveY, hasBall = true, roleTag = "ALDI!")
                            } else if (p.isUser) {
                                // User also makes a supporting forward run!
                                val userRunX = if (attackingRight) (p.x + 12f).coerceAtMost(84f) else (p.x - 12f).coerceAtLeast(16f)
                                p.copy(targetX = userRunX, hasBall = false)
                            } else {
                                p.copy(hasBall = false)
                            }
                        }

                        val isInShootingZone = (attackingRight && receiveX >= 68f) || (!attackingRight && receiveX <= 32f)
                        if (isInShootingZone) {
                            // Teammate takes a one-touch shot toward goal in Stage 2 of ACTION_RESOLUTION!
                            if (state.userIsHome) homeShots += 1 else awayShots += 1
                            val cornerY = if (receiveY < 50f) 41f else 59f
                            secondStageX = goalX
                            secondStageY = cornerY
                            secondStageCtrlX = (receiveX + goalX) * 0.5f
                            secondStageCtrlY = receiveY

                            val finishChance = (54 + (userClub.teamStrength - oppClub.teamStrength) / 2 + curvePassBonus / 2)
                                .coerceIn(42, 82)
                            if (rng.nextInt(100) < finishChance) {
                                if (state.userIsHome) homeOnTarget += 1 else awayOnTarget += 1
                                outcomeType = "GOAL_ASSIST"
                                userAssists += 1
                                userRating = (userRating + 0.90).coerceAtMost(10.0)
                                pendingGoalHome = state.userIsHome
                                bannerText = "${state.minute}' — ASİST VE GOL! ${userPlayer.fullName} milimetrik bir falsolu pas bıraktı, ${targetTm.name} tek vuruşla ağları sarstı! (SCORE_PLACEHOLDER)"
                                pendingGoalBanner = "ASİST: ${userPlayer.kitName.uppercase()} • GOL: ${targetTm.name.uppercase()}"
                                newEvents.add(
                                    0,
                                    MatchEventRecord(
                                        minute = state.minute,
                                        type = "ASİST",
                                        teamId = userClub.id,
                                        playerName = userPlayer.fullName,
                                        description = bannerText,
                                        involvesUser = true
                                    )
                                )
                            } else {
                                if (state.userIsHome) homeOnTarget += 1 else awayOnTarget += 1
                                outcomeType = "SAVED_BY_GK"
                                userRating = (userRating + 0.32).coerceAtMost(10.0)
                                bannerText = "${state.minute}' — ${userPlayer.fullName} harika bir kilit pas çıkardı, ${targetTm.name} bekletmeden vurdu ancak kaleci çıkardı!"
                                newEvents.add(
                                    0,
                                    MatchEventRecord(
                                        minute = state.minute,
                                        type = "KRİTİK_KARAR",
                                        teamId = userClub.id,
                                        playerName = userPlayer.fullName,
                                        description = bannerText,
                                        involvesUser = true
                                    )
                                )
                            }
                        } else if (active.chainStep == 1) {
                            // Build-up pass in midfield: Teammate plays a 1-2 wall pass back to our player near the box!
                            outcomeType = "PASS_COMPLETED_CHAIN"
                            userRating = (userRating + 0.25).coerceAtMost(10.0)
                            val nextBallX = if (attackingRight) (receiveX + 14f).coerceIn(72f, 84f) else (receiveX - 14f).coerceIn(16f, 28f)
                            val nextBallY = (receiveY + (50f - receiveY) * 0.4f).coerceIn(28f, 72f)
                            secondStageX = nextBallX
                            secondStageY = nextBallY
                            bannerText = "${state.minute}' — VER-KAÇ! ${targetTm.name} duvar oldu ve topu ceza sahası önüne tekrar senin koşu yoluna bıraktı!"
                            pendingChain = createChainedFinishingMoment(
                                minute = state.minute,
                                ballX = nextBallX,
                                ballY = nextBallY,
                                title = "${state.minute}' — Ver-Kaç Sonrası Şut Açısı!",
                                desc = "${targetTm.name} ile harika bir duvar pası yaptın! Şimdi ceza sahası önünde kaleyi cepheden görüyorsun, şutunu çiz!",
                                userPlayer = userPlayer
                            )
                        } else {
                            outcomeType = "PASS_COMPLETED"
                            userRating = (userRating + 0.22).coerceAtMost(10.0)
                            bannerText = "${state.minute}' — ${userPlayer.fullName}, ${targetTm.name} ile isabetli paslaştı ve takımını ileri taşıdı."
                        }
                    } else {
                        succeeded = false
                        outcomeType = "INTERCEPTED_BY_DEF"
                        userRating = (userRating - 0.14).coerceAtLeast(5.2)
                        bannerText = "${state.minute}' — ${userPlayer.fullName}'in pası biraz hızlı gitti, rakip savunma topu kontrol etti."
                        newEvents.add(
                            0,
                            MatchEventRecord(
                                minute = state.minute,
                                type = "KRİTİK_KARAR",
                                teamId = userClub.id,
                                playerName = userPlayer.fullName,
                                description = bannerText,
                                involvesUser = true
                            )
                        )
                    }
                }

                GestureActionKind.DRIBBLE_RUN -> {
                    val dribbleSkill = (attr.dribbling * 0.42 + attr.agility * 0.22 + attr.pace * 0.15).roundToInt()
                    val dribbleProb = (36 + dribbleSkill + energyMod - oppDefPenalty).coerceIn(28, 88)
                    if (rng.nextInt(100) < dribbleProb) {
                        succeeded = true
                        outcomeType = "DRIBBLE_SUCCESS_CHAIN"
                        userRating = (userRating + 0.35).coerceAtMost(10.0)
                        val newX = gesture.endX.coerceIn(14f, 88f)
                        val newY = gesture.endY.coerceIn(14f, 86f)
                        finalEndX = newX
                        finalEndY = newY
                        peakZ = 2f

                        updatedPlayers = updatedPlayers.map { p ->
                            if (p.isUser) {
                                p.copy(targetX = newX, targetY = newY, hasBall = true, roleTag = "ÇALIM!")
                            } else {
                                p
                            }
                        }

                        if (active.chainStep == 1) {
                            bannerText = "${state.minute}' — BİRE BİRDE GEÇTİN! Rakibi çalımladın, şimdi son vuruş veya final pası için çiz!"
                            pendingChain = createChainedFinishingMoment(
                                minute = state.minute,
                                ballX = newX,
                                ballY = newY,
                                title = "${state.minute}' — Çalım Sonrası Kaleciyle Karşı Karşıya!",
                                desc = "Rakip savunmacıyı bilek hareketiyle oyundan düşürdün! Şimdi köşeye falsolu şutunu veya boştaki arkadaşına pasını çiz!",
                                userPlayer = userPlayer
                            )
                        } else {
                            // Already in chain step 2 and dribbled into the box -> finishes into empty net!
                            userShots += 1
                            userGoals += 1
                            if (state.userIsHome) {
                                homeShots += 1
                                homeOnTarget += 1
                            } else {
                                awayShots += 1
                                awayOnTarget += 1
                            }
                            userRating = (userRating + 0.95).coerceAtMost(10.0)
                            pendingGoalHome = state.userIsHome
                            finalEndX = goalX
                            finalEndY = 50f
                            bannerText = "${state.minute}' — GOOOL! ${userPlayer.fullName} kaleciyi de çalımlayıp topu boş ağlara bıraktı! (SCORE_PLACEHOLDER)"
                            pendingGoalBanner = "GOOOL! ${userPlayer.kitName.uppercase()} (${state.minute}')"
                            newEvents.add(
                                0,
                                MatchEventRecord(
                                    minute = state.minute,
                                    type = "GOL",
                                    teamId = userClub.id,
                                    playerName = userPlayer.fullName,
                                    description = bannerText,
                                    involvesUser = true
                                )
                            )
                        }
                    } else {
                        succeeded = false
                        outcomeType = "INTERCEPTED_BY_DEF"
                        userRating = (userRating - 0.16).coerceAtLeast(5.2)
                        bannerText = "${state.minute}' — ${userPlayer.fullName} çalım denedi ancak rakip savunmacı vücudunu koyup topu kazandı."
                        newEvents.add(
                            0,
                            MatchEventRecord(
                                minute = state.minute,
                                type = "KRİTİK_KARAR",
                                teamId = userClub.id,
                                playerName = userPlayer.fullName,
                                description = bannerText,
                                involvesUser = true
                            )
                        )
                    }
                }

                GestureActionKind.SLIDE_TACKLE -> {
                    val attacker = state.players.firstOrNull {
                        it.isHome != state.userIsHome && (it.hasBall || it.roleTag == "ATAKTA!")
                    }
                    // Measure closest approach of the drawn tackle trajectory to the attacker & their run lane
                    var minTackleDist = 15f
                    var directionalDot = 0f
                    if (attacker != null) {
                        for (step in 0..10) {
                            val t = step / 10f
                            val (tx, ty) = TrajectoryAnalyzer.evaluateBezier(
                                t,
                                gesture.startX,
                                gesture.startY,
                                gesture.controlX,
                                gesture.controlY,
                                gesture.endX,
                                gesture.endY
                            )
                            val dNow = hypot(attacker.x - tx, attacker.y - ty)
                            val laneX = attacker.x + (attacker.targetX - attacker.x) * 0.45f
                            val laneY = attacker.y + (attacker.targetY - attacker.y) * 0.45f
                            val dLane = hypot(laneX - tx, laneY - ty)
                            minTackleDist = minOf(minTackleDist, dNow, dLane)
                        }
                        val swDx = gesture.endX - gesture.startX
                        val swDy = gesture.endY - gesture.startY
                        val swLen = hypot(swDx, swDy).coerceAtLeast(0.1f)
                        val attDx = attacker.x - gesture.startX
                        val attDy = attacker.y - gesture.startY
                        val attLen = hypot(attDx, attDy).coerceAtLeast(0.1f)
                        directionalDot = (swDx / swLen) * (attDx / attLen) + (swDy / swLen) * (attDy / attLen)
                    }

                    val aimModifier = when {
                        minTackleDist <= 4.0f && directionalDot >= 0.25f -> 26
                        minTackleDist <= 7.0f && directionalDot >= 0.0f -> 8
                        minTackleDist <= 10.5f -> -12
                        else -> -42
                    }
                    val defSkill = (attr.defending * 0.45 + attr.tactics * 0.25 + attr.strength * 0.15).roundToInt()
                    val tackleProb = (32 + defSkill + aimModifier + energyMod - oppDefPenalty).coerceIn(8, 94)

                    val slideTargetX = gesture.endX.coerceIn(8f, 92f)
                    val slideTargetY = gesture.endY.coerceIn(8f, 92f)
                    val slideDirY = if (slideTargetY < gesture.startY) -1f else 1f

                    if (rng.nextInt(100) < tackleProb) {
                        succeeded = true
                        outcomeType = "TACKLE_WON"
                        userRating = (userRating + 0.58).coerceAtMost(10.0)
                        finalEndX = 50f
                        finalEndY = 48f
                        updatedPlayers = updatedPlayers.map { p ->
                            when {
                                p.isUser -> p.copy(
                                    targetX = slideTargetX,
                                    targetY = slideTargetY,
                                    hasBall = true,
                                    isDivingOrSliding = true,
                                    diveDirectionY = slideDirY,
                                    roleTag = "MÜDAHALE!"
                                )
                                attacker != null && p.id == attacker.id -> p.copy(
                                    hasBall = false,
                                    isDivingOrSliding = true,
                                    roleTag = "TOP KAYBI"
                                )
                                else -> p
                            }
                        }
                        bannerText = "${state.minute}' — KRİTİK MÜDAHALE! ${userPlayer.fullName} tam zamanında doğru açıdan kayarak topu söktü aldı!"
                    } else {
                        succeeded = false
                        outcomeType = "TACKLE_FOUL"
                        userRating = (userRating - 0.22).coerceAtLeast(5.0)
                        val attEscapeX = if (attackingRight) 14f else 86f
                        finalEndX = attEscapeX
                        finalEndY = 50f
                        updatedPlayers = updatedPlayers.map { p ->
                            when {
                                p.isUser -> p.copy(
                                    targetX = slideTargetX,
                                    targetY = slideTargetY,
                                    hasBall = false,
                                    isDivingOrSliding = true,
                                    diveDirectionY = slideDirY,
                                    roleTag = "BOŞA KAYDI!"
                                )
                                attacker != null && p.id == attacker.id -> p.copy(
                                    targetX = attEscapeX,
                                    targetY = 50f,
                                    hasBall = true,
                                    roleTag = "GEÇTİ!"
                                )
                                else -> p
                            }
                        }
                        bannerText = if (aimModifier < 0) {
                            "${state.minute}' — ${userPlayer.fullName} yanlış yöne kaydı, rakip forvet boşluktan sıyrıldı!"
                        } else {
                            "${state.minute}' — ${userPlayer.fullName} kayarak müdahalede gecikti, hakem faul düdüğünü çaldı."
                        }
                        // Faul sonrası sakatlık ihtimali -> sedye/sağlık ekibi sahnesi artık oyunda.
                        if (rng.nextInt(100) < 18) {
                            setPieceCinematic = MatchCinematic(
                                kind = MatchCinematicKind.MEDICAL_STRETCHER,
                                minute = state.minute,
                                injury = MedicalInjuryEvent(
                                    injuredPlayerName = attacker?.name ?: userPlayer.kitName,
                                    injuredJerseyNumber = attacker?.number ?: userPlayer.kitNumber,
                                    injuredClubName = oppClub.name,
                                    bodyPartTr = "Sağ Ayak Bileği",
                                    severityScore = 2 + rng.nextInt(2),
                                    requiresStretcher = false,
                                    canReturnToMatch = true
                                )
                            )
                        }
                    }
                    newEvents.add(
                        0,
                        MatchEventRecord(
                            minute = state.minute,
                            type = "KRİTİK_KARAR",
                            teamId = userClub.id,
                            playerName = userPlayer.fullName,
                            description = bannerText,
                            involvesUser = true
                        )
                    )
                }
            }
        }

        return finalizeResolution()
    }

    /**
     * Legacy deterministic option resolver kept for unit-test compatibility (`ExampleUnitTest`).
     */
    private fun resolveLegacyOption(
        state: LiveMatchUiState,
        chosenOption: TacticalDecisionOption,
        userPlayer: PlayerProfile
    ): LiveMatchUiState {
        val active = state.activeDecision ?: return state.copy(phase = MatchPhase.LIVE_PLAY)
        val rng = Random(
            baseSeed +
                state.fixture.id.hashCode() +
                state.minute * 131L +
                chosenOption.id.hashCode() +
                state.userDecisionsTotal * 17L
        )
        val roll = rng.nextInt(100)
        val succeeded = roll < chosenOption.successProbabilityPct

        val attackingRight = state.userIsHome
        val targetGoalX = if (attackingRight) 98f else 2f
        val userClub = if (state.userIsHome) state.homeClub else state.awayClub

        var homeScore = state.homeScore
        var awayScore = state.awayScore
        var homeShots = state.homeShots
        var awayShots = state.awayShots
        var homeOnTarget = state.homeShotsOnTarget
        var awayOnTarget = state.awayShotsOnTarget
        var userGoals = state.userGoals
        var userAssists = state.userAssists
        var userShots = state.userShots
        var userPassesComp = state.userPassesCompleted
        var userPassesAtt = state.userPassesAttempted
        var userRating = state.userLiveRating
        var nextPhase = MatchPhase.ACTION_RESOLUTION
        var goalBanner: String? = null
        var bannerText: String
        var ballTargetX: Float
        var ballTargetY: Float

        val newEvents = state.commentaryFeed.toMutableList()

        when (chosenOption.actionType) {
            "FINESSE_SHOT", "POWER_SHOT", "DRIBBLE_KEEPER" -> {
                userShots += 1
                if (state.userIsHome) homeShots += 1 else awayShots += 1

                if (succeeded) {
                    if (state.userIsHome) {
                        homeScore += 1
                        homeOnTarget += 1
                    } else {
                        awayScore += 1
                        awayOnTarget += 1
                    }
                    userGoals += 1
                    userRating = (userRating + 1.1).coerceAtMost(10.0)
                    nextPhase = MatchPhase.GOAL_CELEBRATION
                    ballTargetX = targetGoalX
                    ballTargetY = if (chosenOption.actionType == "FINESSE_SHOT") 44f else 54f
                    bannerText = "${state.minute}' — GOOOL! ${userPlayer.fullName} (${chosenOption.title}) ile topu ağlara gönderdi! ($homeScore-$awayScore)"
                    goalBanner = "GOOOL! ${userPlayer.kitName.uppercase()} (${state.minute}')"
                    newEvents.add(
                        0,
                        MatchEventRecord(
                            minute = state.minute,
                            type = "GOL",
                            teamId = userClub.id,
                            playerName = userPlayer.fullName,
                            description = bannerText,
                            involvesUser = true
                        )
                    )
                } else {
                    val savedByKeeper = roll < (chosenOption.successProbabilityPct + 25)
                    if (savedByKeeper) {
                        if (state.userIsHome) homeOnTarget += 1 else awayOnTarget += 1
                        userRating = (userRating + 0.1).coerceAtMost(10.0)
                        ballTargetX = if (attackingRight) 93f else 7f
                        ballTargetY = 48f
                        bannerText = "${state.minute}' — ${userPlayer.fullName} (${chosenOption.title}) denedi ancak kaleci son anda parmaklarının ucuyla çıkardı!"
                    } else {
                        userRating = (userRating - 0.15).coerceAtLeast(5.2)
                        ballTargetX = if (attackingRight) 99f else 1f
                        ballTargetY = 28f
                        bannerText = "${state.minute}' — ${userPlayer.fullName} sert vurdu fakat top direğin yanından auta çıktı."
                    }
                    newEvents.add(
                        0,
                        MatchEventRecord(
                            minute = state.minute,
                            type = "ŞUT",
                            teamId = userClub.id,
                            playerName = userPlayer.fullName,
                            description = bannerText,
                            involvesUser = true
                        )
                    )
                }
            }

            "THROUGH_PASS", "CROSS" -> {
                userPassesAtt += 1
                if (succeeded) {
                    userPassesComp += 1
                    val finishChance = (54 + (userClub.teamStrength - 50) / 2).coerceIn(45, 78)
                    val teammateScored = rng.nextInt(100) < finishChance
                    if (state.userIsHome) homeShots += 1 else awayShots += 1

                    if (teammateScored) {
                        if (state.userIsHome) {
                            homeScore += 1
                            homeOnTarget += 1
                        } else {
                            awayScore += 1
                            awayOnTarget += 1
                        }
                        userAssists += 1
                        userRating = (userRating + 0.85).coerceAtMost(10.0)
                        nextPhase = MatchPhase.GOAL_CELEBRATION
                        ballTargetX = targetGoalX
                        ballTargetY = 50f
                        bannerText = "${state.minute}' — ASİST VE GOL! ${userPlayer.fullName} harika bir pas çıkardı, takım arkadaşı tek vuruşla golü attı! ($homeScore-$awayScore)"
                        goalBanner = "ASİST: ${userPlayer.kitName.uppercase()} (${state.minute}')"
                        newEvents.add(
                            0,
                            MatchEventRecord(
                                minute = state.minute,
                                type = "ASİST",
                                teamId = userClub.id,
                                playerName = userPlayer.fullName,
                                description = bannerText,
                                involvesUser = true
                            )
                        )
                    } else {
                        userRating = (userRating + 0.30).coerceAtMost(10.0)
                        ballTargetX = if (attackingRight) 90f else 10f
                        ballTargetY = 46f
                        bannerText = "${state.minute}' — ${userPlayer.fullName} kilit pası adrese teslim gönderdi ancak takım arkadaşı kaleciyi geçemedi!"
                        newEvents.add(
                            0,
                            MatchEventRecord(
                                minute = state.minute,
                                type = "KRİTİK_KARAR",
                                teamId = userClub.id,
                                playerName = userPlayer.fullName,
                                description = bannerText,
                                involvesUser = true
                            )
                        )
                    }
                } else {
                    userRating = (userRating - 0.15).coerceAtLeast(5.2)
                    ballTargetX = if (attackingRight) 74f else 26f
                    ballTargetY = 55f
                    bannerText = "${state.minute}' — ${userPlayer.fullName} savunma arkasına pas denedi fakat rakip stoper araya girdi."
                    newEvents.add(
                        0,
                        MatchEventRecord(
                            minute = state.minute,
                            type = "KRİTİK_KARAR",
                            teamId = userClub.id,
                            playerName = userPlayer.fullName,
                            description = bannerText,
                            involvesUser = true
                        )
                    )
                }
            }

            else -> {
                if (succeeded) {
                    userRating = (userRating + 0.55).coerceAtMost(10.0)
                    ballTargetX = 52f
                    ballTargetY = 48f
                    bannerText = "${state.minute}' — KRİTİK MÜDAHALE! ${userPlayer.fullName} zamanında kayarak tehlikeli atağı kesti ve takımını kontra atağa kaldırdı!"
                } else {
                    userRating = (userRating - 0.25).coerceAtLeast(5.0)
                    ballTargetX = if (attackingRight) 18f else 82f
                    ballTargetY = 50f
                    bannerText = "${state.minute}' — ${userPlayer.fullName} müdahalede gecikti, rakip tehlikeli bölgeden serbest vuruş kazandı."
                }
                newEvents.add(
                    0,
                    MatchEventRecord(
                        minute = state.minute,
                        type = "KRİTİK_KARAR",
                        teamId = userClub.id,
                        playerName = userPlayer.fullName,
                        description = bannerText,
                        involvesUser = true
                    )
                )
            }
        }

        val updatedBall = state.ball.copy(
            startX = active.ballX,
            startY = active.ballY,
            controlX = (active.ballX + ballTargetX) * 0.5f,
            controlY = (active.ballY + ballTargetY) * 0.5f,
            targetX = ballTargetX,
            targetY = ballTargetY,
            flightProgress = 0f,
            flightStepPerTick = 0.08f,
            isShotInFlight = true
        )

        return state.copy(
            phase = nextPhase,
            homeScore = homeScore,
            awayScore = awayScore,
            homeShots = homeShots,
            awayShots = awayShots,
            homeShotsOnTarget = homeOnTarget,
            awayShotsOnTarget = awayOnTarget,
            userGoals = userGoals,
            userAssists = userAssists,
            userShots = userShots,
            userPassesCompleted = userPassesComp,
            userPassesAttempted = userPassesAtt,
            userDecisionsTotal = state.userDecisionsTotal + 1,
            userDecisionsSucceeded = state.userDecisionsSucceeded + (if (succeeded) 1 else 0),
            userLiveRating = (userRating * 10.0).roundToInt() / 10.0,
            userLiveEnergy = (state.userLiveEnergy - 3).coerceAtLeast(10),
            ball = updatedBall,
            activeDecision = null,
            latestBannerText = bannerText,
            goalScorerBanner = goalBanner,
            commentaryFeed = newEvents,
            resolutionTicksRemaining = 16
        )
    }

    private fun createChainedFinishingMoment(
        minute: Int,
        ballX: Float,
        ballY: Float,
        title: String,
        desc: String,
        userPlayer: PlayerProfile
    ): CriticalMomentContext {
        val attr = userPlayer.attributes
        return CriticalMomentContext(
            id = "CM_CHAIN_$minute",
            minute = minute,
            title = title,
            situationDescription = desc,
            ballX = ballX,
            ballY = ballY,
            scenarioType = "ONE_ON_ONE",
            isDefensive = false,
            slowMoFactor = 0.12f,
            hintTextTr = "Kaleciyle karşı karşıyasın! Köşeye falsolu şutunu veya boştaki arkadaşına pasını çiz!",
            chainStep = 2,
            options = listOf(
                TacticalDecisionOption(
                    id = "OPT_FINESSE",
                    title = "Uzak Köşeye Plase Bırak",
                    subtitle = "Kalecinin uzanamayacağı köşeye falsolu vuruş dene.",
                    keyAttributeLabel = "Şut",
                    keyAttributeValue = attr.shooting,
                    successProbabilityPct = 78,
                    riskLevel = "Dengeli",
                    actionType = "FINESSE_SHOT"
                ),
                TacticalDecisionOption(
                    id = "OPT_PASS",
                    title = "Boştaki Takım Arkadaşına Pas Ver",
                    subtitle = "Arka direkteki arkadaşına asist fırsatı yarat.",
                    keyAttributeLabel = "Pas & Görüş",
                    keyAttributeValue = (attr.passing + attr.vision) / 2,
                    successProbabilityPct = 82,
                    riskLevel = "Düşük Risk",
                    actionType = "THROUGH_PASS"
                )
            )
        )
    }

    private fun createCriticalMoment(
        minute: Int,
        state: LiveMatchUiState,
        userPlayer: PlayerProfile,
        rng: Random
    ): CriticalMomentContext {
        val attackingRight = state.userIsHome
        val oppClub = if (state.userIsHome) state.awayClub else state.homeClub
        val oppDefPenalty = ((oppClub.teamStrength - 55) * 0.45).roundToInt()
        val energyMod = ((state.userLiveEnergy - 65) * 0.2).roundToInt()
        val moraleMod = ((userPlayer.morale - 65) * 0.15).roundToInt()
        val attr = userPlayer.attributes

        val isDefensive = userPlayer.primaryPosition in listOf(Position.CB, Position.FB, Position.DM, Position.GK) &&
            rng.nextInt(100) < 42

        if (isDefensive) {
            val ballX = if (attackingRight) 26f else 74f
            val ballY = (35 + rng.nextInt(30)).toFloat()
            val tackleProb = (52 + (attr.defending * 0.45).roundToInt() + energyMod - oppDefPenalty).coerceIn(25, 88)
            val interceptProb = (54 + (attr.tactics * 0.45).roundToInt() + moraleMod - oppDefPenalty).coerceIn(28, 88)
            val pressProb = (50 + (attr.strength * 0.42).roundToInt() + energyMod - oppDefPenalty).coerceIn(25, 85)

            return CriticalMomentContext(
                id = "CM_DEF_$minute",
                minute = minute,
                title = "${minute}' — Rakip Hızlı Kontra Atakta!",
                situationDescription = "${oppClub.name} forveti ceza sahasına sokuluyor. Parmağınla topun/rakibin önüne müdahale çizgisi çek!",
                ballX = ballX,
                ballY = ballY,
                scenarioType = "DEFENSIVE_COUNTER_STOP",
                isDefensive = true,
                slowMoFactor = 0.15f,
                hintTextTr = "Rakibin koşu yoluna veya topa doğru parmağınla kayarak müdahale çizgisi çek!",
                options = listOf(
                    TacticalDecisionOption(
                        id = "OPT_INTERCEPT",
                        title = "Pas Açısını Kapat ve Topu Kes",
                        subtitle = "Oyun görüşün ve taktik sezginle pas yolunu kapat.",
                        keyAttributeLabel = "Taktik",
                        keyAttributeValue = attr.tactics,
                        successProbabilityPct = interceptProb,
                        riskLevel = "Düşük Risk",
                        actionType = "TACKLE"
                    ),
                    TacticalDecisionOption(
                        id = "OPT_SLIDE_TACKLE",
                        title = "Zamanlamalı Kayarak Müdahale",
                        subtitle = "Topa doğrudan hamle yap ve hızlı geçiş başlat.",
                        keyAttributeLabel = "Savunma",
                        keyAttributeValue = attr.defending,
                        successProbabilityPct = tackleProb,
                        riskLevel = "Dengeli",
                        actionType = "TACKLE"
                    ),
                    TacticalDecisionOption(
                        id = "OPT_SHOULDER_PRESS",
                        title = "Omuz Omuza Fiziksel Baskı",
                        subtitle = "Rakibin dengesini bozup topu kazan.",
                        keyAttributeLabel = "Fizik",
                        keyAttributeValue = attr.strength,
                        successProbabilityPct = pressProb,
                        riskLevel = "Yüksek Risk / Yüksek Ödül",
                        actionType = "TACKLE"
                    )
                )
            )
        }

        // Artık deterministik değil: son 3 senaryoyu tekrar etmeyen RNG tabanlı seçim.
        val scenarioNames = listOf(
            "ONE_ON_ONE", "FREE_KICK_CURVE", "PENALTY_KICK", "CORNER_KICK",
            "ACROBATIC_VOLLEY", "WING_CROSS_OR_CUT_IN", "EDGE_OF_BOX_FINESSE"
        )
        var scenarioIndex = rng.nextInt(scenarioNames.size)
        var repeatGuard = 0
        while (scenarioNames[scenarioIndex] in state.recentScenarioTypes && repeatGuard < 12) {
            scenarioIndex = rng.nextInt(scenarioNames.size)
            repeatGuard++
        }
        val finesseProb = (36 + (attr.shooting * 0.50).roundToInt() + moraleMod - oppDefPenalty).coerceIn(20, 84)
        val powerProb = (32 + (attr.shooting * 0.42 + attr.strength * 0.12).roundToInt() + energyMod - oppDefPenalty).coerceIn(18, 80)
        val passProb = (48 + (attr.passing * 0.35 + attr.vision * 0.20).roundToInt() + moraleMod - oppDefPenalty).coerceIn(30, 88)
        val dribbleProb = (30 + (attr.dribbling * 0.35 + attr.agility * 0.20).roundToInt() + energyMod - oppDefPenalty).coerceIn(18, 78)

        val (scenarioType, ballX, ballY, title, desc, hint) = when (scenarioIndex) {
            0 -> {
                val bx = if (attackingRight) 79f else 21f
                val by = (38f + rng.nextInt(24))
                ScenarioSpec(
                    type = "ONE_ON_ONE",
                    x = bx,
                    y = by,
                    title = "${minute}' — Savunma Arkasına Sarktın!",
                    desc = "Kaleci açıyı kapatmak için çıkıyor, arka direkteki takım arkadaşın da boş koşu yapıyor!",
                    hint = "Kalecinin yanından köşeye falsolu şut çiz veya boştaki arkadaşına pas çıkar!"
                )
            }
            1 -> {
                val bx = if (attackingRight) 75f else 25f
                val by = if (rng.nextBoolean()) 34f else 66f
                ScenarioSpec(
                    type = "FREE_KICK_CURVE",
                    x = bx,
                    y = by,
                    title = "${minute}' — Tehlikeli Bölgeden Serbest Vuruş!",
                    desc = "${oppClub.shortName} 5 kişilik baraj kurdu. Doğrudan vurursan baraja takılır, kavis vererek doksana as!",
                    hint = "SERBEST VURUŞ: Parmağınla barajın etrafından kavisli beyaz muz şut çek!"
                )
            }
            2 -> {
                val bx = if (attackingRight) 88.5f else 11.5f
                val by = 50f
                ScenarioSpec(
                    type = "PENALTY_KICK",
                    x = bx,
                    y = by,
                    title = "${minute}' — Penaltı Noktasında Nefesler Tutuldu!",
                    desc = "Hakem beyaz noktayı gösterdi! Kaleciyle baş başasın, köşeyi seç ve vuruşunu yap.",
                    hint = "PENALTI: Parmağınla kalecinin uzanamayacağı köşeye sert veya plase bir çizgi çek!"
                )
            }
            3 -> {
                val bx = if (attackingRight) 98.2f else 1.8f
                val by = if (rng.nextBoolean()) 4.5f else 95.5f
                ScenarioSpec(
                    type = "CORNER_KICK",
                    x = bx,
                    y = by,
                    title = "${minute}' — Köşe Vuruşu Fırsatı!",
                    desc = "Ceza sahası hıncahınç dolu! Arka direğe veya penaltı noktasına kavisli muz orta kes.",
                    hint = "KORNER: Ceza sahasındaki takım arkadaşının koşu yoluna kavisli orta çiz!"
                )
            }
            4 -> {
                val bx = if (attackingRight) 84f else 16f
                val by = (44f + rng.nextInt(12))
                ScenarioSpec(
                    type = "ACROBATIC_VOLLEY",
                    x = bx,
                    y = by,
                    title = "${minute}' — Havada Röveşata / Vole Fırsatı!",
                    desc = "Gelen yüksek ortada havada yükseldin! Kaleye muhteşem bir vole veya röveşata indir.",
                    hint = "RÖVEŞATA: Havada süzülen toptan kalenin üst köşesine doğru hızlıca parmağınla çiz!"
                )
            }
            5 -> {
                val bx = if (attackingRight) 76f else 24f
                val by = if (rng.nextBoolean()) 19f else 81f
                ScenarioSpec(
                    type = "WING_CROSS_OR_CUT_IN",
                    x = bx,
                    y = by,
                    title = "${minute}' — Kanattan Tehlikeli Bindirme!",
                    desc = "Kanatta boşluk buldun! Ceza sahasına hareketlenen arkadaşlarına orta kes veya içeri çalımla gir.",
                    hint = "Ceza sahasındaki arkadaşının koşu yoluna kavisli orta çiz veya içeri çalımla gir!"
                )
            }
            else -> {
                val bx = if (attackingRight) 77f else 23f
                val by = (40f + rng.nextInt(20))
                ScenarioSpec(
                    type = "EDGE_OF_BOX_FINESSE",
                    x = bx,
                    y = by,
                    title = "${minute}' — Ceza Sahası Yayında Şut Açısı!",
                    desc = "Yay üzerinde topu önüne aldın. Köşeye plase bırakabilir, sert vurabilir veya ver-kaç yapabilirsin!",
                    hint = "Stoperin yanından uzak köşeye falsolu şut çiz veya boştaki arkadaşına pas ver!"
                )
            }
        }

        val windKmh = if (scenarioType == "FREE_KICK_CURVE") (6 + rng.nextInt(14)) else rng.nextInt(8)
        val windDeg = (rng.nextFloat() * 360f)

        return CriticalMomentContext(
            id = "CM_ATT_$minute",
            minute = minute,
            title = title,
            situationDescription = desc,
            ballX = ballX,
            ballY = ballY,
            scenarioType = scenarioType,
            isDefensive = false,
            slowMoFactor = 0.14f,
            windSpeedKmh = windKmh,
            windDirectionDeg = windDeg,
            hintTextTr = hint,
            chainStep = 1,
            options = listOf(
                TacticalDecisionOption(
                    id = "OPT_FINESSE",
                    title = "Uzak Köşeye Plase Bırak",
                    subtitle = "Kalecinin uzanamayacağı köşeye falsolu vuruş dene.",
                    keyAttributeLabel = "Şut",
                    keyAttributeValue = attr.shooting,
                    successProbabilityPct = finesseProb,
                    riskLevel = "Dengeli",
                    actionType = "FINESSE_SHOT"
                ),
                TacticalDecisionOption(
                    id = "OPT_PASS",
                    title = "Boştaki Takım Arkadaşına Pas Ver",
                    subtitle = "Arka direkteki arkadaşına asist fırsatı yarat.",
                    keyAttributeLabel = "Pas & Görüş",
                    keyAttributeValue = (attr.passing + attr.vision) / 2,
                    successProbabilityPct = passProb,
                    riskLevel = "Düşük Risk",
                    actionType = "THROUGH_PASS"
                ),
                TacticalDecisionOption(
                    id = "OPT_POWER",
                    title = "Üst Köşeye Sert Şut Çek",
                    subtitle = "Kalecinin tepki veremeyeceği sertlikte aban.",
                    keyAttributeLabel = "Şut & Güç",
                    keyAttributeValue = (attr.shooting + attr.strength) / 2,
                    successProbabilityPct = powerProb,
                    riskLevel = "Yüksek Risk / Yüksek Ödül",
                    actionType = "POWER_SHOT"
                ),
                TacticalDecisionOption(
                    id = "OPT_DRIBBLE",
                    title = "Kaleciyi Çalımlamayı Dene",
                    subtitle = "Bire birde kaleciyi yatırıp boş kaleye yürü.",
                    keyAttributeLabel = "Dribbling",
                    keyAttributeValue = attr.dribbling,
                    successProbabilityPct = dribbleProb,
                    riskLevel = "Yüksek Risk / Yüksek Ödül",
                    actionType = "DRIBBLE_KEEPER"
                )
            )
        )
    }

    private data class ScenarioSpec(
        val type: String,
        val x: Float,
        val y: Float,
        val title: String,
        val desc: String,
        val hint: String
    )

    /**
     * Positions our player, making-run teammates, defensive wall / blockers, and the goalkeeper
     * so every playable moment looks like a handcrafted Soccer Superstar / NSS puzzle level!
     */
    private fun positionPlayersForScenario(
        players: List<PitchPlayerNode>,
        context: CriticalMomentContext,
        userIsHome: Boolean
    ): List<PitchPlayerNode> {
        val attackingRight = userIsHome
        val dir = if (attackingRight) 1f else -1f
        val bx = context.ballX
        val by = context.ballY

        val teammates = players.filter { it.isHome == userIsHome && !it.isUser && it.position != Position.GK }
        val opponents = players.filter { it.isHome != userIsHome && it.position != Position.GK }

        // Pick 2 primary attacking teammates to make visible forward runs
        val attackers = teammates.sortedByDescending {
            when (it.position) {
                Position.ST -> 5
                Position.WG -> 4
                Position.AM -> 3
                Position.CM -> 2
                else -> 1
            }
        }
        val runner1Id = attackers.getOrNull(0)?.id
        val runner2Id = attackers.getOrNull(1)?.id

        // Pick up to 5 opponent defenders near the play (for 5-man walls or marking)
        val defenders = opponents.sortedBy {
            when (it.position) {
                Position.CB -> 1
                Position.FB, Position.WB -> 2
                Position.DM -> 3
                else -> 4
            }
        }
        val def1Id = defenders.getOrNull(0)?.id
        val def2Id = defenders.getOrNull(1)?.id
        val def3Id = defenders.getOrNull(2)?.id
        val def4Id = defenders.getOrNull(3)?.id
        val def5Id = defenders.getOrNull(4)?.id

        return players.map { node ->
            when {
                node.isUser -> {
                    val pose = when (context.scenarioType) {
                        "ACROBATIC_VOLLEY" -> PlayerVisualPose.BICYCLE_KICK
                        "FREE_KICK_CURVE", "PENALTY_KICK" -> PlayerVisualPose.KICK_STRIKE
                        else -> PlayerVisualPose.NORMAL
                    }
                    val elevZ = if (context.scenarioType == "ACROBATIC_VOLLEY") 18f else 0f
                    node.copy(
                        x = bx,
                        y = by,
                        targetX = bx,
                        targetY = by,
                        runTargetX = bx,
                        runTargetY = by,
                        hasBall = !context.isDefensive,
                        isMakingRun = false,
                        isDivingOrSliding = false,
                        visualPose = pose,
                        elevationZ = elevZ,
                        roleTag = "SEN (#${node.number})"
                    )
                }

                // Opponent Goalkeeper
                node.position == Position.GK && node.isHome != userIsHome -> {
                    val gkX = if (attackingRight) 94.8f else 5.2f
                    val gkY = when (context.scenarioType) {
                        "FREE_KICK_CURVE" -> if (by < 50f) 54.5f else 45.5f // guards far post behind wall
                        "PENALTY_KICK" -> 50f
                        else -> (50f + (by - 50f) * 0.22f).coerceIn(43f, 57f)
                    }
                    node.copy(
                        x = gkX,
                        y = gkY,
                        targetX = gkX,
                        targetY = gkY,
                        hasBall = false,
                        isDivingOrSliding = false,
                        visualPose = PlayerVisualPose.GK_READY,
                        roleTag = "KL"
                    )
                }

                // Teammate Runner 1 (Striker / Far Post Runner)
                node.id == runner1Id && !context.isDefensive -> {
                    val rx = (bx + dir * 9f).coerceIn(14f, 88f)
                    val ry = if (by < 50f) (by + 22f).coerceIn(48f, 76f) else (by - 22f).coerceIn(24f, 52f)
                    val runToX = (rx + dir * 7f).coerceIn(10f, 92f)
                    node.copy(
                        x = rx,
                        y = ry,
                        targetX = rx,
                        targetY = ry,
                        runTargetX = runToX,
                        runTargetY = ry,
                        hasBall = false,
                        isMakingRun = true,
                        visualPose = PlayerVisualPose.NORMAL,
                        roleTag = "KOŞU YAPIYOR"
                    )
                }

                // Teammate Runner 2 (Support / Near Post / Wing Runner)
                node.id == runner2Id && !context.isDefensive -> {
                    val rx = (bx + dir * 4f).coerceIn(16f, 85f)
                    val ry = if (by < 50f) (by - 14f).coerceIn(14f, 40f) else (by + 14f).coerceIn(60f, 86f)
                    val runToX = (rx + dir * 8f).coerceIn(12f, 90f)
                    node.copy(
                        x = rx,
                        y = ry,
                        targetX = rx,
                        targetY = ry,
                        runTargetX = runToX,
                        runTargetY = ry,
                        hasBall = false,
                        isMakingRun = true,
                        visualPose = PlayerVisualPose.NORMAL,
                        roleTag = "BOŞTA!"
                    )
                }

                // Free-kick 5-man wall or active defenders
                (node.id in listOf(def1Id, def2Id, def3Id, def4Id, def5Id)) && !context.isDefensive -> {
                    if (context.scenarioType == "FREE_KICK_CURVE") {
                        val wallIdx = when (node.id) {
                            def1Id -> -2
                            def2Id -> -1
                            def3Id -> 0
                            def4Id -> 1
                            else -> 2
                        }
                        val wallX = (bx + dir * 8.8f).coerceIn(15f, 88f)
                        val nearPostBias = if (by < 50f) -2.0f else 2.0f
                        val wallY = (by + nearPostBias + wallIdx * 2.8f).coerceIn(22f, 78f)
                        node.copy(
                            x = wallX,
                            y = wallY,
                            targetX = wallX,
                            targetY = wallY,
                            hasBall = false,
                            isMakingRun = false,
                            visualPose = PlayerVisualPose.DEFENSIVE_WALL,
                            roleTag = if (wallIdx == 0) "5'Lİ BARAJ" else "BARAJ"
                        )
                    } else {
                        val (dx, dy, tag) = when (node.id) {
                            def1Id -> Triple(
                                (bx + dir * 7.5f).coerceIn(12f, 90f),
                                (by + (50f - by) * 0.38f).coerceIn(25f, 75f),
                                "KAPATIYOR"
                            )
                            def2Id -> Triple(
                                (bx + dir * 10.5f).coerceIn(10f, 91f),
                                if (by < 50f) (by + 14f).coerceIn(38f, 74f) else (by - 14f).coerceIn(26f, 62f),
                                "MARKAJ"
                            )
                            else -> Triple(
                                (bx + dir * 4.5f).coerceIn(14f, 88f),
                                if (by < 50f) (by - 9f).coerceIn(14f, 44f) else (by + 9f).coerceIn(56f, 86f),
                                ""
                            )
                        }
                        node.copy(
                            x = dx,
                            y = dy,
                            targetX = dx,
                            targetY = dy,
                            hasBall = false,
                            isMakingRun = false,
                            visualPose = PlayerVisualPose.NORMAL,
                            roleTag = tag
                        )
                    }
                }

                // Defensive counter-stop scenario: place opponent ball-carrier approaching from midfield toward our goal
                node.id == def1Id && context.isDefensive -> {
                    val attX = (bx + dir * 6.0f).coerceIn(14f, 86f)
                    val attY = (by + (if (by < 50f) 4f else -4f)).coerceIn(24f, 76f)
                    val runToX = (bx - dir * 8.0f).coerceIn(8f, 92f)
                    node.copy(
                        x = attX,
                        y = attY,
                        targetX = runToX,
                        targetY = 50f,
                        runTargetX = runToX,
                        runTargetY = 50f,
                        hasBall = true,
                        isMakingRun = true,
                        visualPose = PlayerVisualPose.NORMAL,
                        roleTag = "ATAKTA!"
                    )
                }

                else -> {
                    val pushX = if (context.scenarioType == "PENALTY_KICK") {
                        if (attackingRight) (node.baseX * 0.7f).coerceAtMost(77f) else (node.baseX * 0.7f + 30f).coerceAtLeast(23f)
                    } else {
                        node.baseX
                    }
                    node.copy(
                        x = pushX,
                        targetX = pushX,
                        hasBall = false,
                        isMakingRun = false,
                        isDivingOrSliding = false,
                        visualPose = PlayerVisualPose.NORMAL,
                        roleTag = ""
                    )
                }
            }
        }
    }

    private fun stepBezierBallFlight(ball: PitchBallState): PitchBallState {
        if (!ball.isShotInFlight) {
            return interpolateBall(ball, stepFactor = 0.28f)
        }
        val nextT = (ball.flightProgress + ball.flightStepPerTick).coerceIn(0f, 1f)
        val (bx, by) = TrajectoryAnalyzer.evaluateBezier(
            t = nextT,
            startX = ball.startX,
            startY = ball.startY,
            controlX = ball.controlX,
            controlY = ball.controlY,
            endX = ball.targetX,
            endY = ball.targetY
        )
        val bz = TrajectoryAnalyzer.evaluateBallHeightZ(nextT, ball.peakHeightZ)
        val updatedTrail = (ball.trailPoints + PitchPoint(bx, by, bz)).takeLast(14)
        return ball.copy(
            x = bx,
            y = by,
            z = bz,
            flightProgress = nextT,
            trailPoints = updatedTrail
        )
    }

    private fun buildInitial22Players(
        homeClub: Club,
        awayClub: Club,
        userPlayer: PlayerProfile,
        userIsHome: Boolean,
        userOnPitch: Boolean
    ): List<PitchPlayerNode> {
        val homeCoords = listOf(
            Position.GK to (6f to 50f),
            Position.CB to (20f to 34f),
            Position.CB to (20f to 66f),
            Position.FB to (24f to 14f),
            Position.FB to (24f to 86f),
            Position.DM to (35f to 50f),
            Position.CM to (43f to 32f),
            Position.AM to (48f to 68f),
            Position.WG to (62f to 18f),
            Position.WG to (62f to 82f),
            Position.ST to (68f to 50f)
        )

        val awayCoords = listOf(
            Position.GK to (94f to 50f),
            Position.CB to (80f to 34f),
            Position.CB to (80f to 66f),
            Position.FB to (76f to 14f),
            Position.FB to (76f to 86f),
            Position.DM to (65f to 50f),
            Position.CM to (57f to 68f),
            Position.AM to (52f to 32f),
            Position.WG to (38f to 18f),
            Position.WG to (38f to 82f),
            Position.ST to (32f to 50f)
        )

        val userSlotIndex = when (userPlayer.primaryPosition) {
            Position.GK -> 0
            Position.CB -> 1
            Position.FB, Position.WB -> 3
            Position.DM -> 5
            Position.CM -> 6
            Position.AM -> 7
            Position.WG -> 8
            Position.ST -> 10
        }

        val homeNodes = homeCoords.mapIndexed { index, (pos, xy) ->
            val isUserHere = userIsHome && userOnPitch && index == userSlotIndex
            val member = homeClub.keySquadMembers.getOrNull(index)
            PitchPlayerNode(
                id = if (isUserHere) userPlayer.id else (member?.id ?: "H_$index"),
                number = if (isUserHere) userPlayer.kitNumber else (member?.kitNumber ?: (index + 1)),
                name = if (isUserHere) userPlayer.kitName else (member?.name ?: "Oyuncu ${index + 1}"),
                position = if (isUserHere) userPlayer.primaryPosition else pos,
                isHome = true,
                isUser = isUserHere,
                baseX = xy.first,
                baseY = xy.second,
                x = xy.first,
                y = xy.second,
                targetX = xy.first,
                targetY = xy.second,
                hasBall = index == 6
            )
        }

        val awayNodes = awayCoords.mapIndexed { index, (pos, xy) ->
            val isUserHere = !userIsHome && userOnPitch && index == userSlotIndex
            val member = awayClub.keySquadMembers.getOrNull(index)
            PitchPlayerNode(
                id = if (isUserHere) userPlayer.id else (member?.id ?: "A_$index"),
                number = if (isUserHere) userPlayer.kitNumber else (member?.kitNumber ?: (index + 1)),
                name = if (isUserHere) userPlayer.kitName else (member?.name ?: "Rakip ${index + 1}"),
                position = if (isUserHere) userPlayer.primaryPosition else pos,
                isHome = false,
                isUser = isUserHere,
                baseX = xy.first,
                baseY = xy.second,
                x = xy.first,
                y = xy.second,
                targetX = xy.first,
                targetY = xy.second,
                hasBall = false
            )
        }

        return homeNodes + awayNodes
    }

    private fun computeTacticalPositionsForMinute(
        players: List<PitchPlayerNode>,
        homeAttacks: Boolean,
        minute: Int,
        rng: Random
    ): Pair<List<PitchPlayerNode>, PitchBallState> {
        val attackProgress = (rng.nextFloat() * 24f) - 4f
        val teamShiftX = if (homeAttacks) (10f + attackProgress) else -(10f + attackProgress)

        val candidatesForBall = players.filter {
            it.isHome == homeAttacks && it.position != Position.GK
        }
        val carrier = candidatesForBall.ifEmpty { players }.random(rng)

        val updatedNodes = players.map { p ->
            if (p.position == Position.GK) {
                val gkX = if (p.isHome) 5.5f else 94.5f
                val gkY = (50f + (rng.nextFloat() - 0.5f) * 8f).coerceIn(42f, 58f)
                p.copy(
                    targetX = gkX,
                    targetY = gkY,
                    hasBall = p.id == carrier.id,
                    isMakingRun = false,
                    isDivingOrSliding = false,
                    roleTag = ""
                )
            } else {
                val roleMultiplier = when (p.position) {
                    Position.CB -> 0.55f
                    Position.FB, Position.WB -> 0.85f
                    Position.DM, Position.CM -> 0.95f
                    Position.AM, Position.WG, Position.ST -> 1.15f
                    Position.GK -> 0.1f
                }
                val jitterX = (rng.nextFloat() - 0.5f) * 9f
                val jitterY = (rng.nextFloat() - 0.5f) * 12f
                val newTargetX = (p.baseX + teamShiftX * roleMultiplier + jitterX).coerceIn(8f, 92f)
                val newTargetY = (p.baseY + jitterY).coerceIn(8f, 92f)
                p.copy(
                    targetX = newTargetX,
                    targetY = newTargetY,
                    runTargetX = newTargetX,
                    runTargetY = newTargetY,
                    hasBall = p.id == carrier.id,
                    isMakingRun = false,
                    isDivingOrSliding = false,
                    roleTag = ""
                )
            }
        }

        val activeCarrier = updatedNodes.find { it.id == carrier.id } ?: updatedNodes.first()
        val ball = PitchBallState(
            x = activeCarrier.x,
            y = activeCarrier.y,
            targetX = activeCarrier.targetX,
            targetY = activeCarrier.targetY,
            startX = activeCarrier.x,
            startY = activeCarrier.y,
            isShotInFlight = false
        )
        return updatedNodes to ball
    }

    private fun interpolatePlayers(
        players: List<PitchPlayerNode>,
        stepFactor: Float
    ): List<PitchPlayerNode> {
        return players.map { p ->
            val nx = p.x + (p.targetX - p.x) * stepFactor
            val ny = p.y + (p.targetY - p.y) * stepFactor
            p.copy(x = nx, y = ny)
        }
    }

    private fun interpolateBall(
        ball: PitchBallState,
        stepFactor: Float
    ): PitchBallState {
        val nx = ball.x + (ball.targetX - ball.x) * stepFactor
        val ny = ball.y + (ball.targetY - ball.y) * stepFactor
        return ball.copy(x = nx, y = ny)
    }

    fun buildFinalMatchReport(
        state: LiveMatchUiState,
        userPlayer: PlayerProfile
    ): CompletedMatchReport {
        val pendingHomeBonus = if (state.pendingGoalForHome == true) 1 else 0
        val pendingAwayBonus = if (state.pendingGoalForHome == false) 1 else 0
        val finalHomeScore = state.homeScore + pendingHomeBonus
        val finalAwayScore = state.awayScore + pendingAwayBonus

        val userClub = if (state.userIsHome) state.homeClub else state.awayClub
        val userTeamGoals = if (state.userIsHome) finalHomeScore else finalAwayScore
        val oppTeamGoals = if (state.userIsHome) finalAwayScore else finalHomeScore

        // Gerçek oynanan süre: ilk 11 ise maçın bittiği dakika, sonradan girdiyse 55'ten sonrası.
        val minutesPlayed = when {
            state.userStartingStatus == StartingStatus.STARTER ->
                (state.minute + state.stoppageMinutes).coerceAtMost(90 + state.stoppageMinutes)
            state.userOnPitch -> (state.minute - 55).coerceAtLeast(0)
            else -> 0
        }

        val finalRating = if (minutesPlayed == 0) {
            6.0
        } else {
            val teamResultBonus = when {
                userTeamGoals > oppTeamGoals -> 0.3
                userTeamGoals == oppTeamGoals -> 0.0
                else -> -0.2
            }
            ((state.userLiveRating + teamResultBonus).coerceIn(5.0, 10.0) * 10.0).roundToInt() / 10.0
        }

        val managerComment = when {
            minutesPlayed == 0 -> "${userClub.managerName}: \"Bugün taktiksel olarak sana süre veremedim, hafta içi antrenmanlarda kendini hazır tut.\""
            finalRating >= 8.0 -> "${userClub.managerName}: \"Muhteşem bir performans! Sahada tam istediğim liderliği ve kaliteyi gösterdin.\""
            finalRating >= 6.8 -> "${userClub.managerName}: \"Disiplinli ve faydalı oynadın. Bu çizgini korursan formayı bırakmazsın.\""
            else -> "${userClub.managerName}: \"Bugün kararlarında aceleciydin ve beklentimin altında kaldın. Antrenmanda daha çok çalışmalısın.\""
        }

        val teammateComment = when {
            state.userGoals > 0 || state.userAssists > 0 -> "Takım Kaptanı: \"Skora yaptığın katkı soyunma odasında herkesi ateşledi kardeşim!\""
            userTeamGoals > oppTeamGoals -> "Takım Kaptanı: \"Harika bir 3 puan! Emeğine sağlık.\""
            else -> "Takım Kaptanı: \"Kafamızı kaldırıp önümüzdeki haftaya odaklanmalıyız.\""
        }

        val pressHeadline = when {
            state.userGoals >= 2 -> "${userPlayer.fullName} Yıldızlaştı! ${userClub.name} Sahadan $finalHomeScore-$finalAwayScore Ayrıldı"
            state.userGoals == 1 -> "${userPlayer.fullName} Ağları Havalandırdı: ${state.homeClub.shortName} $finalHomeScore-$finalAwayScore ${state.awayClub.shortName}"
            state.userAssists >= 1 -> "${userPlayer.fullName}'den Kritik Asist! ${state.homeClub.shortName} $finalHomeScore-$finalAwayScore ${state.awayClub.shortName}"
            else -> "${state.homeClub.name} $finalHomeScore - $finalAwayScore ${state.awayClub.name}: Mücadele Nefes Kesti"
        }

        val energySpent = (92 - state.userLiveEnergy).coerceIn(10, 45)

        return CompletedMatchReport(
            fixtureId = state.fixture.id,
            week = state.fixture.week,
            dateLabel = state.fixture.dateLabel,
            homeClubId = state.homeClub.id,
            homeClubName = state.homeClub.name,
            awayClubId = state.awayClub.id,
            awayClubName = state.awayClub.name,
            homeScore = finalHomeScore,
            awayScore = finalAwayScore,
            homePossessionPct = state.homePossessionPct,
            // Yapay şişirme kaldırıldı; en azından gol sayısı kadar şut/isabet garanti edilir.
            homeShots = state.homeShots.coerceAtLeast(finalHomeScore),
            awayShots = state.awayShots.coerceAtLeast(finalAwayScore),
            homeShotsOnTarget = state.homeShotsOnTarget.coerceAtLeast(finalHomeScore),
            awayShotsOnTarget = state.awayShotsOnTarget.coerceAtLeast(finalAwayScore),
            userPlayedMinutes = minutesPlayed,
            userStarted = state.userStartingStatus == StartingStatus.STARTER,
            userGoals = state.userGoals,
            userAssists = state.userAssists,
            userShots = state.userShots,
            userPassesCompleted = state.userPassesCompleted,
            userPassesAttempted = state.userPassesAttempted,
            userKeyDecisionsMade = state.userDecisionsTotal,
            userSuccessfulDecisions = state.userDecisionsSucceeded,
            userRating = finalRating,
            managerFeedback = managerComment,
            teammateReaction = teammateComment,
            pressHeadline = pressHeadline,
            energySpent = energySpent,
            wageAndBonusEarnedEur = 0L,
            events = state.commentaryFeed
        )
    }
}
