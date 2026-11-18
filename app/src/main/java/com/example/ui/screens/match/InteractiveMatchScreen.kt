package com.example.ui.screens.match

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.LiveMatchUiState
import com.example.engine.MatchPhase
import com.example.engine.PitchPlayerNode
import com.example.engine.PlayerVisualPose
import com.example.engine.TacticalDecisionOption
import com.example.engine.match.GestureActionKind
import com.example.engine.match.GestureTrajectoryInput
import com.example.engine.match.PitchPoint
import com.example.engine.match.TrajectoryAnalyzer
import com.example.model.CareerSaveState
import com.example.model.GraphicsQuality
import com.example.model.Position
import com.example.ui.components.ClubCrestBadge
import com.example.ui.components.FLFormatters
import com.example.ui.components.parseHexColor
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.MatchDrawAmber
import com.example.ui.theme.MatchLossRed
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StrokePremium
import com.example.ui.theme.StrokePremiumStrong
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Real-time playable 2D/2.5D Match Screen inspired by New Star Soccer (NSS) and Soccer Superstar.
 * Features:
 * - Behind/above player 2.5D perspective camera looking toward the attacking goal + optional TV Broadcast view
 * - Slow-motion playable moments when the ball reaches our player
 * - Finger-drawn trajectory controls: direction, curve/spin (Magnus effect), power, loft height
 * - Real-time 60fps Bezier ball flight, shadow, spin, diving goalkeeper, lunging defenders, and teammate runs
 * - Minute skipping between moments ("SONRAKİ POZİSYONA ATLA") + 1x/2x/3x speed controls
 */
@Composable
fun InteractiveMatchScreen(
    career: CareerSaveState,
    liveMatch: LiveMatchUiState?,
    graphicsQuality: GraphicsQuality,
    onPrepareMatch: () -> Unit,
    onStartKickOff: () -> Unit,
    onResumeSecondHalf: () -> Unit,
    onChangeSpeed: (Int) -> Unit,
    onChooseDecisionOption: (TacticalDecisionOption) -> Unit,
    onFinishMatch: () -> Unit,
    onSkipToNextMoment: () -> Unit = {}
) {
    if (liveMatch == null) {
        PreMatchLobbyView(
            career = career,
            onPrepareMatch = onPrepareMatch
        )
        return
    }

    val haptic = LocalHapticFeedback.current
    val homeColor = parseHexColor(liveMatch.homeClub.primaryColorHex, Color(0xFFE74C3C))
    val awayColor = parseHexColor(liveMatch.awayClub.primaryColorHex, Color(0xFF3498DB))

    // Camera mode: true = Soccer Superstar 2.5D Perspective (default), false = Full Pitch TV Radar
    var preferPerspectiveCamera by remember { mutableStateOf(true) }
    val isPlayableOrResolution = liveMatch.phase in listOf(
        MatchPhase.CRITICAL_DECISION,
        MatchPhase.ACTION_RESOLUTION,
        MatchPhase.GOAL_CELEBRATION
    )
    // Automatically use 2.5D Perspective during playable moments and shot/pass resolution
    val usePerspectiveView = preferPerspectiveCamera || isPlayableOrResolution

    // Smooth 60fps animation clock
    var animTimeSec by remember { mutableFloatStateOf(0f) }
    var ballSpinRotationDeg by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        val startNs = System.nanoTime()
        var lastNs = startNs
        while (true) {
            withFrameNanos { nowNs ->
                val dt = ((nowNs - lastNs) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNs = nowNs
                animTimeSec = (nowNs - startNs) / 1_000_000_000f
                val spinSpeed = if (liveMatch.ball.isShotInFlight) {
                    240f + abs(liveMatch.ball.curveSpin) * 14f
                } else {
                    45f
                }
                ballSpinRotationDeg = (ballSpinRotationDeg + spinSpeed * dt) % 360f
            }
        }
    }

    // Haptic feedback on Goal Celebration or Playable Moment entry
    LaunchedEffect(liveMatch.phase, liveMatch.minute) {
        when (liveMatch.phase) {
            MatchPhase.CRITICAL_DECISION -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            MatchPhase.GOAL_CELEBRATION -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            else -> Unit
        }
    }

    // Gesture state for drawing the ball trajectory in CRITICAL_DECISION
    val drawnPitchPoints = remember { mutableStateListOf<PitchPoint>() }
    var activeGesturePreview by remember { mutableStateOf<GestureTrajectoryInput?>(null) }
    var dragStartTimeMs by remember { mutableLongStateOf(0L) }
    var dragScreenDistancePx by remember { mutableFloatStateOf(0f) }
    var lastDragScreenPos by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(IntSize(1, 1)) }
    var gestureHintOverride by remember { mutableStateOf<String?>(null) }

    // Clear gesture state whenever phase or decision changes
    LaunchedEffect(liveMatch.phase, liveMatch.activeDecision?.id) {
        drawnPitchPoints.clear()
        activeGesturePreview = null
        gestureHintOverride = null
    }

    // Smoothly animated camera zoom factor
    val perspectiveZoom by animateFloatAsState(
        targetValue = when (liveMatch.phase) {
            MatchPhase.CRITICAL_DECISION -> liveMatch.cameraZoom.coerceIn(1.15f, 1.45f)
            MatchPhase.ACTION_RESOLUTION -> (liveMatch.cameraZoom * 0.95f).coerceIn(1.08f, 1.36f)
            MatchPhase.GOAL_CELEBRATION -> 1.28f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "cameraZoom"
    )
    val currentZoom by rememberUpdatedState(perspectiveZoom)
    val currentMatchState by rememberUpdatedState(liveMatch)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundSecondary, BackgroundPrimary)))
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("live_match_screen"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Compact Broadcast Scoreboard, Clock, Player Telemetry & Speed / Skip Bar
        BroadcastScorebugHeader(
            liveMatch = liveMatch,
            playerKitName = career.player.kitName,
            homeColor = homeColor,
            awayColor = awayColor,
            usePerspectiveView = usePerspectiveView,
            onToggleCamera = { preferPerspectiveCamera = !preferPerspectiveCamera },
            onChangeSpeed = onChangeSpeed,
            onSkipToNextMoment = onSkipToNextMoment
        )

        // 2. Main Interactive 2.5D Pitch Canvas (Takes majority of screen height, zero scroll conflict!)
        Surface(
            color = BackgroundPrimary,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                width = if (liveMatch.phase == MatchPhase.CRITICAL_DECISION) 2.dp else 1.5.dp,
                color = if (liveMatch.phase == MatchPhase.CRITICAL_DECISION) AccentBright else StrokePremiumStrong
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(liveMatch.phase, liveMatch.activeDecision?.id, usePerspectiveView) {
                        if (liveMatch.phase != MatchPhase.CRITICAL_DECISION) return@pointerInput
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                val ms = currentMatchState
                                val cw = size.width.toFloat()
                                val ch = size.height.toFloat()
                                val pivot = computeCameraPivot(ms, cw, ch, ms.userIsHome, usePerspectiveView)
                                val unzoomedStart = unzoomScreenOffset(startOffset, pivot, currentZoom)

                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                drawnPitchPoints.clear()
                                dragStartTimeMs = System.currentTimeMillis()
                                dragScreenDistancePx = 0f
                                lastDragScreenPos = startOffset
                                gestureHintOverride = null

                                // Start trajectory at the ball's current pitch position
                                val startBall = PitchPoint(ms.ball.x, ms.ball.y, 0f)
                                drawnPitchPoints.add(startBall)

                                val touchedPitch = screenToPitchCoord(
                                    screenOffset = unzoomedStart,
                                    canvasW = cw,
                                    canvasH = ch,
                                    attackingRight = ms.userIsHome,
                                    usePerspective = usePerspectiveView
                                )
                                if (hypot(touchedPitch.x - startBall.x, touchedPitch.y - startBall.y) > 2.5f) {
                                    drawnPitchPoints.add(touchedPitch)
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val ms = currentMatchState
                                val cw = size.width.toFloat()
                                val ch = size.height.toFloat()
                                val pivot = computeCameraPivot(ms, cw, ch, ms.userIsHome, usePerspectiveView)
                                val unzoomedPos = unzoomScreenOffset(change.position, pivot, currentZoom)

                                val stepPx = hypot(dragAmount.x, dragAmount.y)
                                dragScreenDistancePx += stepPx
                                lastDragScreenPos = change.position

                                val pt = screenToPitchCoord(
                                    screenOffset = unzoomedPos,
                                    canvasW = cw,
                                    canvasH = ch,
                                    attackingRight = ms.userIsHome,
                                    usePerspective = usePerspectiveView
                                )
                                val lastPt = drawnPitchPoints.lastOrNull()
                                if (lastPt == null || hypot(pt.x - lastPt.x, pt.y - lastPt.y) >= 1.2f) {
                                    drawnPitchPoints.add(pt)
                                }

                                val elapsed = (System.currentTimeMillis() - dragStartTimeMs).coerceAtLeast(40L)
                                activeGesturePreview = TrajectoryAnalyzer.analyzeDrawnPath(
                                    rawPoints = drawnPitchPoints.toList(),
                                    ballX = ms.ball.x,
                                    ballY = ms.ball.y,
                                    swipeDurationMs = elapsed,
                                    screenArcLengthPx = dragScreenDistancePx,
                                    attackingRight = ms.userIsHome,
                                    isDefensiveMoment = ms.activeDecision?.isDefensive == true,
                                    players = ms.players,
                                    userIsHome = ms.userIsHome
                                )
                            },
                            onDragEnd = {
                                val ms = currentMatchState
                                val elapsed = (System.currentTimeMillis() - dragStartTimeMs).coerceAtLeast(45L)
                                val finalGesture = TrajectoryAnalyzer.analyzeDrawnPath(
                                    rawPoints = drawnPitchPoints.toList(),
                                    ballX = ms.ball.x,
                                    ballY = ms.ball.y,
                                    swipeDurationMs = elapsed,
                                    screenArcLengthPx = dragScreenDistancePx,
                                    attackingRight = ms.userIsHome,
                                    isDefensiveMoment = ms.activeDecision?.isDefensive == true,
                                    players = ms.players,
                                    userIsHome = ms.userIsHome
                                ) ?: activeGesturePreview

                                if (finalGesture != null) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    val option = TacticalDecisionOption.fromGesture(finalGesture)
                                    drawnPitchPoints.clear()
                                    activeGesturePreview = null
                                    onChooseDecisionOption(option)
                                } else {
                                    drawnPitchPoints.clear()
                                    activeGesturePreview = null
                                    gestureHintOverride = "Çizgi çok kısa kaldı! Topun gideceği yöne doğru parmağınla daha uzun bir kavis çiz."
                                }
                            },
                            onDragCancel = {
                                drawnPitchPoints.clear()
                                activeGesturePreview = null
                            }
                        )
                    }
                    .testTag("pitch_2d_canvas")
            ) {
                SoccerSuperstarPitchCanvas(
                    matchState = liveMatch,
                    graphicsQuality = graphicsQuality,
                    usePerspective = usePerspectiveView,
                    cameraZoom = perspectiveZoom,
                    animTimeSec = animTimeSec,
                    ballSpinDeg = ballSpinRotationDeg,
                    activeGesturePreview = activeGesturePreview,
                    rawDrawnPoints = drawnPitchPoints,
                    modifier = Modifier.fillMaxSize()
                )

                // Top-of-Pitch Slow-Mo & Scenario HUD Overlay during Playable Moment
                if (liveMatch.phase == MatchPhase.CRITICAL_DECISION && liveMatch.activeDecision != null) {
                    val decision = liveMatch.activeDecision
                    PlayableMomentTopOverlay(
                        decisionTitle = decision.title,
                        scenarioType = decision.scenarioType,
                        windSpeedKmh = decision.windSpeedKmh,
                        windDirectionDeg = decision.windDirectionDeg,
                        chainStep = decision.chainStep,
                        activePreview = activeGesturePreview,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }

                // Bottom-of-Pitch Gesture Instruction Banner during Playable Moment
                if (liveMatch.phase == MatchPhase.CRITICAL_DECISION && liveMatch.activeDecision != null) {
                    val decision = liveMatch.activeDecision
                    PlayableMomentBottomPrompt(
                        hintText = gestureHintOverride ?: decision.hintTextTr,
                        situationDescription = decision.situationDescription,
                        activePreview = activeGesturePreview,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("critical_decision_panel")
                    )
                }

                // Goal Celebration Broadcast Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = liveMatch.goalScorerBanner != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Surface(
                        color = BackgroundPrimary.copy(alpha = 0.94f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(2.dp, AccentBright)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = liveMatch.goalScorerBanner ?: "GOOOL!",
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp,
                                color = AccentBright,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "${liveMatch.homeClub.shortName} ${liveMatch.homeScore} - ${liveMatch.awayScore} ${liveMatch.awayClub.shortName}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 3. Bottom Phase Controls & Live Commentary Ticker
        BottomMatchControlDeck(
            liveMatch = liveMatch,
            onStartKickOff = onStartKickOff,
            onResumeSecondHalf = onResumeSecondHalf,
            onFinishMatch = onFinishMatch,
            onSkipToNextMoment = onSkipToNextMoment
        )
    }
}

@Composable
private fun PreMatchLobbyView(
    career: CareerSaveState,
    onPrepareMatch: () -> Unit
) {
    val nextFx = career.todayUserFixture ?: career.nextUnplayedUserFixture
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundSecondary, BackgroundPrimary)))
            .padding(16.dp)
            .testTag("match_pre_lobby_screen"),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = SurfacePrimary,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, StrokePremiumStrong),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.SportsSoccer,
                    contentDescription = null,
                    tint = AccentBright,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "MAÇ MERKEZİ & 2D AKILLI SAHA",
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    color = TextPrimary
                )
                if (nextFx != null) {
                    Text(
                        text = "HAFTA ${nextFx.week} • ${nextFx.homeClubName} vs ${nextFx.awayClubName}",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AccentBright,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Stadyum: ${nextFx.stadiumName} • Kadro Rolün: ${career.nextMatchStartingStatus.labelTr}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = BackgroundSecondary,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, StrokePremium)
                    ) {
                        Text(
                            text = "🎯 YENİ NESİL KONTROL: Top sana geldiğinde zaman yavaşlar (Slow-Mo). Parmağınla ekranda çizgi çekerek falsolu şut, ara pas, orta veya çalım yap!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onPrepareMatch,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentPrimary,
                            contentColor = Color(0xFF04140C)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("btn_enter_2d_match")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STADYUMA GİR VE 2D MAÇI BAŞLAT",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                } else {
                    Text(
                        text = "Sezonun tüm fikstürleri tamamlandı!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun BroadcastScorebugHeader(
    liveMatch: LiveMatchUiState,
    playerKitName: String,
    homeColor: Color,
    awayColor: Color,
    usePerspectiveView: Boolean,
    onToggleCamera: () -> Unit,
    onChangeSpeed: (Int) -> Unit,
    onSkipToNextMoment: () -> Unit
) {
    Surface(
        color = SurfacePrimary,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, StrokePremiumStrong),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Home Team
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(30.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(homeColor)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    ClubCrestBadge(
                        shortName = liveMatch.homeClub.shortName,
                        primaryHex = liveMatch.homeClub.primaryColorHex,
                        secondaryHex = liveMatch.homeClub.secondaryColorHex,
                        size = 32.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = liveMatch.homeClub.shortName,
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "%${liveMatch.homePossessionPct} TOP",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Center Score & Clock
                Surface(
                    color = BackgroundPrimary,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, StrokePremiumStrong)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = liveMatch.homeScore.toString(),
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            color = TextPrimary,
                            modifier = Modifier.testTag("match_home_score")
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                color = if (liveMatch.phase == MatchPhase.CRITICAL_DECISION) {
                                    ChampionshipGold.copy(alpha = 0.24f)
                                } else {
                                    AccentPrimary.copy(alpha = 0.18f)
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (liveMatch.phase == MatchPhase.CRITICAL_DECISION) ChampionshipGold else AccentBright.copy(alpha = 0.6f)
                                )
                            ) {
                                Text(
                                    text = "${liveMatch.minute}'",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (liveMatch.phase == MatchPhase.CRITICAL_DECISION) ChampionshipGold else AccentBright,
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                        .testTag("match_minute_label")
                                )
                            }
                            Text(
                                text = when (liveMatch.phase) {
                                    MatchPhase.PRE_MATCH -> "BAŞLIYOR"
                                    MatchPhase.HALF_TIME -> "DEVRE"
                                    MatchPhase.FULL_TIME -> "MAÇ SONU"
                                    MatchPhase.CRITICAL_DECISION -> "SLOW-MO"
                                    MatchPhase.GOAL_CELEBRATION -> "GOOOL!"
                                    MatchPhase.ACTION_RESOLUTION -> "POZİSYON"
                                    else -> "CANLI"
                                },
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                color = TextSecondary
                            )
                        }
                        Text(
                            text = liveMatch.awayScore.toString(),
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            color = TextPrimary,
                            modifier = Modifier.testTag("match_away_score")
                        )
                    }
                }

                // Away Team
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = liveMatch.awayClub.shortName,
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "%${100 - liveMatch.homePossessionPct} TOP",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    ClubCrestBadge(
                        shortName = liveMatch.awayClub.shortName,
                        primaryHex = liveMatch.awayClub.primaryColorHex,
                        secondaryHex = liveMatch.awayClub.secondaryColorHex,
                        size = 32.dp
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(30.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(awayColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Player Rating / Energy + Camera Toggle + Speed (1x/2x/3x) + Skip to Next Moment
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = BackgroundPrimary,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, StrokePremium)
                    ) {
                        Text(
                            text = "RTG ${FLFormatters.formatRating(liveMatch.userLiveRating)}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = AccentBright,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = "$playerKitName • ${liveMatch.userGoals}G ${liveMatch.userAssists}A • ENJ %${liveMatch.userLiveEnergy}",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Camera Perspective Toggle
                    Surface(
                        color = if (usePerspectiveView) SurfaceElevated else BackgroundPrimary,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, if (usePerspectiveView) AccentBright.copy(alpha = 0.6f) else StrokePremium),
                        modifier = Modifier.clickable { onToggleCamera() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Kamera Açısı",
                                tint = AccentBright,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (usePerspectiveView) "3D AÇI" else "2D TV",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = TextPrimary
                            )
                        }
                    }

                    // 1x / 2x / 3x Speed
                    listOf(1, 2, 3).forEach { spd ->
                        val active = liveMatch.speedMultiplier == spd
                        Surface(
                            color = if (active) AccentPrimary else BackgroundPrimary,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (active) AccentBright else StrokePremium),
                            modifier = Modifier
                                .clickable { onChangeSpeed(spd) }
                                .testTag("btn_speed_${spd}x")
                        ) {
                            Text(
                                text = "${spd}x",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (active) Color(0xFF04140C) else TextPrimary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Skip to Next Playable Moment button (active during LIVE_PLAY)
                    if (liveMatch.phase == MatchPhase.LIVE_PLAY) {
                        Surface(
                            color = ChampionshipGold.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, ChampionshipGold),
                            modifier = Modifier
                                .clickable { onSkipToNextMoment() }
                                .testTag("btn_skip_to_moment")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = "Pozisyona Atla",
                                    tint = ChampionshipGold,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "ATLA",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = ChampionshipGold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayableMomentTopOverlay(
    decisionTitle: String,
    scenarioType: String,
    windSpeedKmh: Int,
    windDirectionDeg: Float,
    chainStep: Int,
    activePreview: GestureTrajectoryInput?,
    modifier: Modifier = Modifier
) {
    Surface(
        color = BackgroundPrimary.copy(alpha = 0.88f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (activePreview != null) AccentBright else ChampionshipGold),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = if (chainStep > 1) ChampionshipGold else AccentPrimary,
                        shape = RoundedCornerShape(5.dp)
                    ) {
                        Text(
                            text = if (chainStep > 1) "ZİNCİRLEME FİNAL ANI • SLOW-MO" else "OYNANABİLİR POZİSYON • SLOW-MO",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color(0xFF04140C),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (windSpeedKmh >= 4) {
                        val normDeg = ((windDirectionDeg % 360f) + 360f) % 360f
                        val windArrow = when {
                            normDeg < 22.5f || normDeg >= 337.5f -> "↑"
                            normDeg < 67.5f -> "↗"
                            normDeg < 112.5f -> "→"
                            normDeg < 157.5f -> "↘"
                            normDeg < 202.5f -> "↓"
                            normDeg < 247.5f -> "↙"
                            normDeg < 292.5f -> "←"
                            else -> "↖"
                        }
                        Text(
                            text = "💨 $windSpeedKmh km/s $windArrow",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = ChampionshipGold
                        )
                    }
                }

                Text(
                    text = when (scenarioType) {
                        "FREE_KICK_CURVE" -> "SERBEST VURUŞ"
                        "ONE_ON_ONE" -> "KALECİYLE BİRE BİR"
                        "WING_CROSS_OR_CUT_IN" -> "KANAT ORGANİZASYONU"
                        "DEFENSIVE_COUNTER_STOP" -> "SAVUNMA MÜDAHALESİ"
                        else -> "HÜCUM KARARI"
                    },
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = AccentBright
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = decisionTitle,
                fontFamily = BarlowCondensedFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary
            )

            // Live Gesture Telemetry Bar when user is dragging finger on the pitch!
            if (activePreview != null) {
                Spacer(modifier = Modifier.height(4.dp))
                val badgeColor = parseHexColor(activePreview.inferredKind.badgeColorHex, AccentBright)
                val spinDir = when {
                    activePreview.curveSpinDegrees > 3f -> "${activePreview.curveSpinDegrees.roundToInt()}° SAĞ FALSO ↻"
                    activePreview.curveSpinDegrees < -3f -> "${abs(activePreview.curveSpinDegrees).roundToInt()}° SOL FALSO ↺"
                    else -> "DÜZ ROTA"
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = badgeColor.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, badgeColor)
                        ) {
                            Text(
                                text = activePreview.inferredKind.labelTr +
                                    (activePreview.targetTeammateName?.let { " ($it)" } ?: ""),
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = spinDir,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = if (activePreview.blockedByDefenderWarning) {
                            "⚠ STOPER KAPATIYOR (FALSO VER!)"
                        } else {
                            "GÜÇ: %${activePreview.powerPct}"
                        },
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = if (activePreview.blockedByDefenderWarning) MatchLossRed else ChampionshipGold
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayableMomentBottomPrompt(
    hintText: String,
    situationDescription: String,
    activePreview: GestureTrajectoryInput?,
    modifier: Modifier = Modifier
) {
    Surface(
        color = BackgroundPrimary.copy(alpha = 0.90f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AccentBright.copy(alpha = 0.8f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = AccentPrimary.copy(alpha = 0.22f),
                shape = CircleShape,
                border = BorderStroke(1.dp, AccentBright),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = AccentBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (activePreview != null) {
                        "PARMAĞINI BIRAKTIĞIN ANDA VURUŞ YAPILACAK!"
                    } else {
                        hintText
                    },
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = AccentBright
                )
                Text(
                    text = situationDescription,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun BottomMatchControlDeck(
    liveMatch: LiveMatchUiState,
    onStartKickOff: () -> Unit,
    onResumeSecondHalf: () -> Unit,
    onFinishMatch: () -> Unit,
    onSkipToNextMoment: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Live Commentary & Match Stats Strip
        Surface(
            color = SurfacePrimary,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, StrokePremium),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(AccentBright)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = liveMatch.latestBannerText.replace("(SCORE_PLACEHOLDER)", "(${liveMatch.homeScore}-${liveMatch.awayScore})"),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Şut ${liveMatch.homeShots}-${liveMatch.awayShots}",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }

        // Phase Action Buttons (Kick-Off, Half-Time, Full-Time)
        when (liveMatch.phase) {
            MatchPhase.PRE_MATCH -> {
                Button(
                    onClick = onStartKickOff,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPrimary,
                        contentColor = Color(0xFF04140C)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_kickoff_match")
                ) {
                    Icon(Icons.Default.SportsSoccer, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BAŞLAMA VURUŞUNU YAP (1. DEVRE)",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            MatchPhase.HALF_TIME -> {
                Surface(
                    color = SurfacePrimary,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, StrokePremiumStrong),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DEVRE ARASI • ${liveMatch.homeScore} - ${liveMatch.awayScore}",
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = AccentBright
                            )
                            Text(
                                text = "İsabetli Şut: ${liveMatch.homeShotsOnTarget}-${liveMatch.awayShotsOnTarget} • Korner: ${liveMatch.homeCorners}-${liveMatch.awayCorners}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                        Button(
                            onClick = onResumeSecondHalf,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPrimary,
                                contentColor = Color(0xFF04140C)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_start_second_half")
                        ) {
                            Text(
                                text = "2. YARIYA BAŞLA (46')",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            MatchPhase.FULL_TIME -> {
                Button(
                    onClick = onFinishMatch,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPrimary,
                        contentColor = Color(0xFF04140C)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("btn_finish_match_to_locker")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MAÇI TAMAMLA VE SOYUNMA ODASINA GEÇ",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            else -> Unit
        }
    }
}

// ================================================================================================
// 2.5D PERSPECTIVE & 2D PROJECTION MATH (Soccer Superstar / New Star Soccer Camera Angle)
// ================================================================================================

/**
 * Projects a 3D pitch coordinate (x: 0..100, y: 0..100, z: 0..60) onto Canvas screen coordinates.
 * When [usePerspective] is true:
 * - Renders from behind/above the attacking team looking UP toward the opponent's goal at the top of the canvas!
 * - Near edge (bottom of canvas) is wide; far edge (goal line at top of canvas) converges with trapezoidal foreshortening.
 * - [z] lifts the ball/player vertically upward in screen space and scales with perspective depth.
 */
private data class ProjectedPoint(
    val screenX: Float,
    val screenY: Float,
    val groundY: Float,
    val depthScale: Float
)

private fun projectPitchToScreen(
    pitchX: Float,
    pitchY: Float,
    pitchZ: Float,
    canvasW: Float,
    canvasH: Float,
    attackingRight: Boolean,
    usePerspective: Boolean
): ProjectedPoint {
    if (!usePerspective) {
        val padX = canvasW * 0.04f
        val padY = canvasH * 0.05f
        val pw = canvasW - padX * 2f
        val ph = canvasH - padY * 2f
        val sx = padX + (pitchX / 100f) * pw
        val groundY = padY + (pitchY / 100f) * ph
        val sy = groundY - pitchZ * 0.45f
        return ProjectedPoint(sx, sy, groundY, 1.0f)
    }

    // Forward progress u in 0..1 toward the attacking goal (u=0 is own goal, u=1 is opponent goal at top)
    val rawU = if (attackingRight) (pitchX / 100f) else ((100f - pitchX) / 100f)
    val u = rawU.coerceIn(-0.05f, 1.05f)

    // Lateral coordinate v in -0.5..+0.5 across pitch width
    val rawV = if (attackingRight) ((pitchY - 50f) / 100f) else ((50f - pitchY) / 100f)

    // Trapezoidal perspective parameters:
    // Top of pitch (u=1, opponent goal line) sits at y = 0.16 * canvasH
    // Bottom of pitch (u=0, own half / behind player) sits at y = 0.95 * canvasH
    val topGoalLineY = canvasH * 0.165f
    val bottomNearY = canvasH * 0.945f
    val depthCurve = u.coerceIn(0f, 1f).pow(0.84f)
    val groundY = bottomNearY - depthCurve * (bottomNearY - topGoalLineY)

    // Pitch width narrows from 94% of canvasW at the bottom to 58% of canvasW at the far goal line
    val nearHalfWidth = canvasW * 0.47f
    val farHalfWidth = canvasW * 0.29f
    val halfWidthAtDepth = nearHalfWidth - depthCurve * (nearHalfWidth - farHalfWidth)
    val screenX = (canvasW * 0.5f) + rawV * (halfWidthAtDepth * 2f)

    // Depth scale for player radius & ball Z height (1.15 near camera -> 0.72 at far goal)
    val depthScale = (1.14f - depthCurve * 0.40f).coerceIn(0.68f, 1.20f)
    val zOffsetPx = pitchZ * (canvasH * 0.0042f) * depthScale
    val screenY = groundY - zOffsetPx

    return ProjectedPoint(
        screenX = screenX,
        screenY = screenY,
        groundY = groundY,
        depthScale = depthScale
    )
}

/**
 * Inverse projection: converts a user's finger touch/drag [screenOffset] on the Canvas
 * directly into pitch coordinates (0..100, 0..100).
 */
private fun screenToPitchCoord(
    screenOffset: Offset,
    canvasW: Float,
    canvasH: Float,
    attackingRight: Boolean,
    usePerspective: Boolean
): PitchPoint {
    if (!usePerspective) {
        val padX = canvasW * 0.04f
        val padY = canvasH * 0.05f
        val pw = (canvasW - padX * 2f).coerceAtLeast(1f)
        val ph = (canvasH - padY * 2f).coerceAtLeast(1f)
        val px = (((screenOffset.x - padX) / pw) * 100f).coerceIn(1f, 99f)
        val py = (((screenOffset.y - padY) / ph) * 100f).coerceIn(1f, 99f)
        return PitchPoint(px, py, 0f)
    }

    val topGoalLineY = canvasH * 0.165f
    val bottomNearY = canvasH * 0.945f
    val normY = ((bottomNearY - screenOffset.y) / (bottomNearY - topGoalLineY)).coerceIn(0f, 1.03f)
    val u = normY.pow(1f / 0.84f).coerceIn(0.01f, 0.99f)

    val nearHalfWidth = canvasW * 0.47f
    val farHalfWidth = canvasW * 0.29f
    val halfWidthAtDepth = nearHalfWidth - normY.coerceIn(0f, 1f) * (nearHalfWidth - farHalfWidth)
    val rawV = ((screenOffset.x - canvasW * 0.5f) / (halfWidthAtDepth * 2f)).coerceIn(-0.49f, 0.49f)

    val pitchX = if (attackingRight) (u * 100f) else ((1f - u) * 100f)
    val pitchY = if (attackingRight) (50f + rawV * 100f) else (50f - rawV * 100f)
    return PitchPoint(pitchX.coerceIn(1f, 99f), pitchY.coerceIn(2f, 98f), 0f)
}

private fun computeCameraPivot(
    matchState: LiveMatchUiState,
    canvasW: Float,
    canvasH: Float,
    attackingRight: Boolean,
    usePerspective: Boolean
): Offset {
    val focusProj = projectPitchToScreen(
        pitchX = matchState.cameraFocusX,
        pitchY = matchState.cameraFocusY,
        pitchZ = 0f,
        canvasW = canvasW,
        canvasH = canvasH,
        attackingRight = attackingRight,
        usePerspective = usePerspective
    )
    val baseCenterX = canvasW * 0.5f
    val baseCenterY = if (usePerspective) canvasH * 0.38f else canvasH * 0.5f
    val pivotX = (baseCenterX + (focusProj.screenX - baseCenterX) * 0.55f).coerceIn(0f, canvasW)
    val pivotY = (baseCenterY + (focusProj.screenY - baseCenterY) * 0.55f).coerceIn(0f, canvasH)
    return Offset(pivotX, pivotY)
}

private fun unzoomScreenOffset(
    screenOffset: Offset,
    pivot: Offset,
    zoom: Float
): Offset {
    if (zoom <= 1.002f) return screenOffset
    return Offset(
        x = pivot.x + (screenOffset.x - pivot.x) / zoom,
        y = pivot.y + (screenOffset.y - pivot.y) / zoom
    )
}

// ================================================================================================
// 60FPS SOCCER SUPERSTAR / NSS 2.5D PITCH CANVAS RENDERER
// ================================================================================================

@Composable
private fun SoccerSuperstarPitchCanvas(
    matchState: LiveMatchUiState,
    graphicsQuality: GraphicsQuality,
    usePerspective: Boolean,
    cameraZoom: Float,
    animTimeSec: Float,
    ballSpinDeg: Float,
    activeGesturePreview: GestureTrajectoryInput?,
    rawDrawnPoints: List<PitchPoint>,
    modifier: Modifier = Modifier
) {
    val homePrimary = parseHexColor(matchState.homeClub.primaryColorHex, Color(0xFFE74C3C))
    val awayPrimary = parseHexColor(matchState.awayClub.primaryColorHex, Color(0xFF3498DB))

    val numberPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = 20f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }
    val tagPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = 16f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val attackingRight = matchState.userIsHome
        val effectiveZoom = cameraZoom.coerceIn(1.0f, 1.35f)
        val cameraPivot = computeCameraPivot(matchState, w, h, attackingRight, usePerspective)

        withTransform({
            if (effectiveZoom > 1.002f) {
                scale(effectiveZoom, effectiveZoom, pivot = cameraPivot)
            }
        }) {
            // 1. Stadium Background & Floodlight Atmosphere
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF040E0A),
                        Color(0xFF082218),
                        Color(0xFF061A12)
                    )
                ),
                topLeft = Offset.Zero,
                size = Size(w, h)
            )

            if (usePerspective) {
                drawPerspectiveStadiumAndPitch(
                    w = w,
                    h = h,
                    attackingRight = attackingRight,
                    matchState = matchState,
                    animTimeSec = animTimeSec
                )
            } else {
                drawFlat2DPitch(w = w, h = h)
            }

            // 2. Teammate Forward Run Lanes & Target Rings (during Playable Moment)
            if (matchState.phase == MatchPhase.CRITICAL_DECISION) {
                drawPlayableMomentTacticalGuides(
                    matchState = matchState,
                    w = w,
                    h = h,
                    attackingRight = attackingRight,
                    usePerspective = usePerspective,
                    animTimeSec = animTimeSec
                )
            }

            // 3. Real-Time Finger-Drawn Trajectory Preview (Curve + Ground Shadow + Loft + Chevrons + Target Reticle)
            if (matchState.phase == MatchPhase.CRITICAL_DECISION) {
                if (activeGesturePreview != null) {
                    drawActiveTrajectoryPreview(
                        preview = activeGesturePreview,
                        w = w,
                        h = h,
                        attackingRight = attackingRight,
                        usePerspective = usePerspective,
                        animTimeSec = animTimeSec
                    )
                } else if (rawDrawnPoints.size >= 2) {
                    // Draw glowing raw path while starting drag
                    val rawPath = Path()
                    rawDrawnPoints.forEachIndexed { idx, pt ->
                        val proj = projectPitchToScreen(pt.x, pt.y, 0f, w, h, attackingRight, usePerspective)
                        if (idx == 0) rawPath.moveTo(proj.screenX, proj.screenY)
                        else rawPath.lineTo(proj.screenX, proj.screenY)
                    }
                    drawPath(
                        path = rawPath,
                        color = Color.White.copy(alpha = 0.90f),
                        style = Stroke(width = 5f, cap = StrokeCap.Round)
                    )
                } else {
                    // Animated 3D Gesture Hand Guide & Banana Curve Hint (Soccer Superstar Style - Photo 2)
                    drawGestureGuideHandAndHint(
                        matchState = matchState,
                        w = w,
                        h = h,
                        attackingRight = attackingRight,
                        usePerspective = usePerspective,
                        animTimeSec = animTimeSec
                    )
                }
            }

            // 4. Ball Flight Trail Ribbon (during ACTION_RESOLUTION / GOAL_CELEBRATION)
            if (matchState.ball.trailPoints.size >= 2) {
                drawBallFlightTrail(
                    trail = matchState.ball.trailPoints,
                    curveSpin = matchState.ball.curveSpin,
                    w = w,
                    h = h,
                    attackingRight = attackingRight,
                    usePerspective = usePerspective
                )
            }

            // 5. Render All 22 Players sorted back-to-front by screen Y so nearer players overlap farther ones naturally!
            val slowMoDrift = if (matchState.phase == MatchPhase.CRITICAL_DECISION) {
                sin(animTimeSec * 2.4f) * 0.45f
            } else {
                0f
            }

            val sortedPlayers = matchState.players.map { p ->
                val driftX = if (p.isMakingRun && matchState.phase == MatchPhase.CRITICAL_DECISION) {
                    (p.runTargetX - p.x) * (0.12f + 0.08f * sin(animTimeSec * 3.2f))
                } else {
                    0f
                }
                val driftY = if (p.position == Position.GK && matchState.phase == MatchPhase.CRITICAL_DECISION) {
                    slowMoDrift * 1.4f
                } else {
                    0f
                }
                val proj = projectPitchToScreen(
                    pitchX = p.x + driftX,
                    pitchY = p.y + driftY,
                    pitchZ = 0f,
                    canvasW = w,
                    canvasH = h,
                    attackingRight = attackingRight,
                    usePerspective = usePerspective
                )
                p to proj
            }.sortedBy { it.second.groundY }

            val baseRadius = (h * 0.028f).coerceIn(10f, 18f)
            sortedPlayers.forEach { (p, proj) ->
                drawPitchPlayerMarker(
                    player = p,
                    proj = proj,
                    baseRadius = baseRadius,
                    homeColor = homePrimary,
                    awayColor = awayPrimary,
                    animTimeSec = animTimeSec,
                    isPlayableMoment = matchState.phase == MatchPhase.CRITICAL_DECISION,
                    numberPaint = numberPaint,
                    tagPaint = tagPaint
                )
            }

            // 6. Render 3D Ball with Ground Shadow, Loft Elevation & Spinning Pentagon Pattern
            val ballProj = projectPitchToScreen(
                pitchX = matchState.ball.x,
                pitchY = matchState.ball.y,
                pitchZ = matchState.ball.z,
                canvasW = w,
                canvasH = h,
                attackingRight = attackingRight,
                usePerspective = usePerspective
            )
            draw3DSoccerBall(
                ballProj = ballProj,
                ballZ = matchState.ball.z,
                baseRadius = baseRadius * 0.62f,
                spinDeg = ballSpinDeg,
                isPlayableMoment = matchState.phase == MatchPhase.CRITICAL_DECISION,
                animTimeSec = animTimeSec
            )

            // 7. Goal Celebration Confetti & Net Ripple Particles
            if (matchState.phase == MatchPhase.GOAL_CELEBRATION) {
                val count = graphicsQuality.particleCount
                for (i in 0 until count) {
                    val angle = (i.toFloat() / count) * (2f * PI.toFloat()) + animTimeSec * 1.5f
                    val dist = (16 - matchState.resolutionTicksRemaining).coerceAtLeast(1) * 11f
                    val cx = ballProj.screenX + cos(angle) * dist
                    val cy = ballProj.screenY + sin(angle) * dist * 0.7f
                    drawCircle(
                        color = when (i % 3) {
                            0 -> AccentBright
                            1 -> ChampionshipGold
                            else -> Color.White
                        },
                        radius = 4.5f,
                        center = Offset(cx.coerceIn(0f, w), cy.coerceIn(0f, h))
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawPerspectiveStadiumAndPitch(
    w: Float,
    h: Float,
    attackingRight: Boolean,
    matchState: LiveMatchUiState,
    animTimeSec: Float
) {
    val homeColor = parseHexColor(matchState.homeClub.primaryColorHex, Color(0xFFDC2626))
    val awayColor = parseHexColor(matchState.awayClub.primaryColorHex, Color(0xFF2563EB))
    val excitement = matchState.crowdExcitementLevel.coerceIn(0.2f, 1.0f)

    // ============================================================================================
    // 1. MULTI-TIERED PACKED STADIUM GRANDSTANDS & ATMOSPHERE (Top 16.5% of canvas - Photos 1 & 3)
    // ============================================================================================
    val standHeight = h * 0.165f

    // Concrete bleachers tiered backdrop
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF091017),
                Color(0xFF131D28),
                Color(0xFF1E2B37)
            )
        ),
        topLeft = Offset(0f, 0f),
        size = Size(w, standHeight)
    )

    // Concrete tiered horizontal risers
    val tierCount = 6
    val tierH = standHeight / tierCount
    for (t in 0 until tierCount) {
        val ty = t * tierH
        drawLine(
            color = Color(0xFF334155).copy(alpha = 0.50f),
            start = Offset(0f, ty),
            end = Offset(w, ty),
            strokeWidth = 1.2f
        )
    }

    // 4 vertical concrete stair aisles dividing crowd bays
    val aisleXs = listOf(w * 0.22f, w * 0.42f, w * 0.62f, w * 0.82f)
    aisleXs.forEach { ax ->
        drawRect(
            color = Color(0xFF94A3B8).copy(alpha = 0.28f),
            topLeft = Offset(ax - 6f, 0f),
            size = Size(12f, standHeight)
        )
        for (t in 0 until tierCount) {
            drawLine(
                color = Color.White.copy(alpha = 0.45f),
                start = Offset(ax - 5f, t * tierH + tierH * 0.5f),
                end = Offset(ax + 5f, t * tierH + tierH * 0.5f),
                strokeWidth = 1.0f
            )
        }
    }

    // Densely packed spectators in color-blocked club bays
    val crowdRows = 6
    val crowdCols = 38
    for (r in 0 until crowdRows) {
        val ry = (standHeight / (crowdRows + 1)) * (r + 1f)
        for (c in 0 until crowdCols) {
            val cx = (w / crowdCols) * (c + 0.5f)
            // Skip spectator if standing directly on an aisle
            if (aisleXs.any { abs(it - cx) < 8f }) continue

            val bounce = sin(animTimeSec * (3.5f + excitement * 5.2f) + c * 0.65f + r * 1.35f) * (2.8f * excitement)
            val bay = when {
                cx < w * 0.38f -> 0 // Home Fan Bay (Red / Maroon)
                cx < w * 0.65f -> 1 // Neutral / VIP Bay (White / Gold / Emerald)
                else -> 2           // Away / Rival Fan Bay (Blue / Navy)
            }
            val shirtColor = when (bay) {
                0 -> if ((r + c) % 3 == 0) Color.White.copy(alpha = 0.85f) else homeColor.copy(alpha = 0.90f)
                1 -> when ((r + c) % 4) {
                    0 -> ChampionshipGold.copy(alpha = 0.85f)
                    1 -> Color.White.copy(alpha = 0.85f)
                    2 -> AccentBright.copy(alpha = 0.80f)
                    else -> Color(0xFF38BDF8).copy(alpha = 0.80f)
                }
                else -> if ((r + c) % 3 == 0) Color.White.copy(alpha = 0.85f) else awayColor.copy(alpha = 0.90f)
            }

            // Spectator Torso
            drawRect(
                color = shirtColor,
                topLeft = Offset(cx - 2.8f, ry + bounce),
                size = Size(5.6f, 4.2f)
            )
            // Spectator Head
            val skinColor = when ((c * 7 + r * 13) % 4) {
                0 -> Color(0xFFF6D8B8)
                1 -> Color(0xFFE0AC69)
                2 -> Color(0xFF8D5524)
                else -> Color(0xFFC68642)
            }
            drawCircle(
                color = skinColor,
                radius = 2.0f,
                center = Offset(cx, ry + bounce - 2.4f)
            )
        }
    }

    // Animated Supporter Flags waving in both bays
    val flagDefs = listOf(
        Triple(w * 0.12f, standHeight * 0.35f, homeColor),
        Triple(w * 0.28f, standHeight * 0.50f, homeColor),
        Triple(w * 0.72f, standHeight * 0.40f, awayColor),
        Triple(w * 0.90f, standHeight * 0.55f, awayColor)
    )
    flagDefs.forEach { (fx, fy, col) ->
        val flagWave = sin(animTimeSec * 5.5f + fx) * 4f
        drawLine(
            color = Color.White.copy(alpha = 0.75f),
            start = Offset(fx, fy + 12f),
            end = Offset(fx, fy - 6f),
            strokeWidth = 1.2f
        )
        val flagPath = Path().apply {
            moveTo(fx, fy - 6f)
            lineTo(fx + 14f + flagWave, fy - 3f)
            lineTo(fx + 12f + flagWave, fy + 5f)
            lineTo(fx, fy + 2f)
            close()
        }
        drawPath(flagPath, color = col.copy(alpha = 0.88f))
    }

    // Camera flash bulbs popping randomly across the crowd
    for (i in 0..4) {
        val flashSeed = (animTimeSec * 4.2f + i * 1.9f).toInt()
        val flashPhase = (animTimeSec * 4.2f + i * 1.9f) - flashSeed
        if (flashPhase < 0.15f) {
            val fx = ((flashSeed * 173) % 94) / 100f * w + w * 0.03f
            val fy = ((flashSeed * 269) % 85) / 100f * standHeight + 4f
            val flashAlpha = 1f - (flashPhase / 0.15f)
            drawCircle(color = Color.White.copy(alpha = flashAlpha), radius = 4.5f, center = Offset(fx, fy))
            drawLine(
                color = Color.White.copy(alpha = flashAlpha * 0.8f),
                start = Offset(fx - 7f, fy),
                end = Offset(fx + 7f, fy),
                strokeWidth = 1.2f
            )
        }
    }

    // TV Camera Broadcast Crane in the corner (Seen in Photos 1 & 3)
    val craneX = w * 0.10f
    val craneY = standHeight - 4f
    // Tripod Base
    drawLine(color = Color(0xFF1E293B), start = Offset(craneX, craneY), end = Offset(craneX - 10f, craneY + 12f), strokeWidth = 2.4f)
    drawLine(color = Color(0xFF1E293B), start = Offset(craneX, craneY), end = Offset(craneX + 10f, craneY + 12f), strokeWidth = 2.4f)
    drawLine(color = Color(0xFF334155), start = Offset(craneX, craneY), end = Offset(craneX, craneY - 14f), strokeWidth = 2.8f)
    // Articulated Jib Arm
    val armEndX = craneX + 22f
    val armEndY = craneY - 26f
    drawLine(color = Color(0xFFE2E8F0), start = Offset(craneX - 6f, craneY - 8f), end = Offset(armEndX, armEndY), strokeWidth = 2.0f)
    // Counterweight
    drawRect(color = Color(0xFFDC2626), topLeft = Offset(craneX - 10f, craneY - 11f), size = Size(6f, 6f))
    // Camera Head & Lens Hood
    drawRect(color = Color(0xFF0F172A), topLeft = Offset(armEndX - 2f, armEndY - 4f), size = Size(10f, 7f))
    drawCircle(color = Color(0xFFEF4444), radius = 1.5f, center = Offset(armEndX + 8f, armEndY - 2f)) // Red Tally Light!

    // Stadium Perimeter Digital LED Advertising Ribbon (Photos 1, 2, 3, 4)
    val ledTop = standHeight - 2f
    val ledH = h * 0.026f
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF02170E),
                Color(0xFF063321),
                Color(0xFF032215),
                Color(0xFF063321),
                Color(0xFF02170E)
            )
        ),
        topLeft = Offset(w * 0.08f, ledTop),
        size = Size(w * 0.84f, ledH)
    )
    // Glowing animated neon LED border
    val ledBorderColor = when (((animTimeSec * 1.5f).toInt()) % 3) {
        0 -> AccentBright
        1 -> ChampionshipGold
        else -> Color(0xFF38BDF8)
    }
    drawRoundRect(
        color = ledBorderColor.copy(alpha = 0.85f),
        topLeft = Offset(w * 0.08f, ledTop),
        size = Size(w * 0.84f, ledH),
        cornerRadius = CornerRadius(3f, 3f),
        style = Stroke(width = 1.8f)
    )
    // Subtle digital LED sponsor notches
    for (b in 1..7) {
        val bx = w * 0.08f + (w * 0.84f / 8f) * b
        drawLine(
            color = Color.White.copy(alpha = 0.45f),
            start = Offset(bx, ledTop + 2f),
            end = Offset(bx, ledTop + ledH - 2f),
            strokeWidth = 1f
        )
    }

    // ============================================================================================
    // 2. VIBRANT LUSH SOCCER TURF & PERSPECTIVE MOWING BANDS (Photos 1, 3, 4)
    // ============================================================================================
    val bandCount = 14
    val goalU = if (attackingRight) 100f else 0f
    val ownU = if (attackingRight) 0f else 100f

    for (i in 0 until bandCount) {
        val u0 = i / bandCount.toFloat()
        val u1 = (i + 1) / bandCount.toFloat()
        val px0 = ownU + (goalU - ownU) * u0
        val px1 = ownU + (goalU - ownU) * u1

        val bl = projectPitchToScreen(px0, if (attackingRight) 0f else 100f, 0f, w, h, attackingRight, true)
        val br = projectPitchToScreen(px0, if (attackingRight) 100f else 0f, 0f, w, h, attackingRight, true)
        val tl = projectPitchToScreen(px1, if (attackingRight) 0f else 100f, 0f, w, h, attackingRight, true)
        val tr = projectPitchToScreen(px1, if (attackingRight) 100f else 0f, 0f, w, h, attackingRight, true)

        val bandPath = Path().apply {
            moveTo(bl.screenX, bl.groundY)
            lineTo(br.screenX, br.groundY)
            lineTo(tr.screenX, tr.groundY)
            lineTo(tl.screenX, tl.groundY)
            close()
        }
        // Alternating vibrant fresh lawn shades matching the photos!
        val grassColor = if (i % 2 == 0) Color(0xFF27913A) else Color(0xFF1E7A2E)
        drawPath(path = bandPath, color = grassColor)

        // Micro-mowing striation lines across turf bands for authentic pitch texture
        val midY = (bl.groundY + tl.groundY) * 0.5f
        val midLeftX = (bl.screenX + tl.screenX) * 0.5f
        val midRightX = (br.screenX + tr.screenX) * 0.5f
        drawLine(
            color = Color.White.copy(alpha = 0.045f),
            start = Offset(midLeftX, midY),
            end = Offset(midRightX, midY),
            strokeWidth = 1.0f
        )
    }

    // ============================================================================================
    // 3. PERSPECTIVE PITCH BOUNDARY LINES & MARKINGS IN CRISP WHITE
    // ============================================================================================
    val lineColor = Color.White.copy(alpha = 0.88f)

    fun drawPitchSegment(x1: Float, y1: Float, x2: Float, y2: Float, strokeW: Float = 2.6f, color: Color = lineColor) {
        val p1 = projectPitchToScreen(x1, y1, 0f, w, h, attackingRight, true)
        val p2 = projectPitchToScreen(x2, y2, 0f, w, h, attackingRight, true)
        drawLine(color = color, start = Offset(p1.screenX, p1.groundY), end = Offset(p2.screenX, p2.groundY), strokeWidth = strokeW, cap = StrokeCap.Round)
    }

    // Outer touchlines & goal lines
    drawPitchSegment(0f, 0f, 100f, 0f)
    drawPitchSegment(0f, 100f, 100f, 100f)
    drawPitchSegment(0f, 0f, 0f, 100f)
    drawPitchSegment(100f, 0f, 100f, 100f)

    // Halfway line & perspective center circle
    drawPitchSegment(50f, 0f, 50f, 100f, strokeW = 2.2f)
    val centerCirclePath = Path()
    for (deg in 0..360 step 10) {
        val rad = deg * (PI / 180.0)
        val cx = 50f + cos(rad).toFloat() * 10f
        val cy = 50f + sin(rad).toFloat() * 14f
        val p = projectPitchToScreen(cx, cy, 0f, w, h, attackingRight, true)
        if (deg == 0) centerCirclePath.moveTo(p.screenX, p.groundY)
        else centerCirclePath.lineTo(p.screenX, p.groundY)
    }
    drawPath(centerCirclePath, color = lineColor, style = Stroke(width = 2.2f))

    // Attacking Penalty Box & Goalmouth (at top of perspective pitch)
    val attGoalX = if (attackingRight) 100f else 0f
    val attBoxEdgeX = if (attackingRight) 83f else 17f
    val attSixEdgeX = if (attackingRight) 94f else 6f
    val attPenSpotX = if (attackingRight) 88.5f else 11.5f

    // 18-yard box
    drawPitchSegment(attGoalX, 21f, attBoxEdgeX, 21f)
    drawPitchSegment(attGoalX, 79f, attBoxEdgeX, 79f)
    drawPitchSegment(attBoxEdgeX, 21f, attBoxEdgeX, 79f)

    // 6-yard goal area
    drawPitchSegment(attGoalX, 34f, attSixEdgeX, 34f)
    drawPitchSegment(attGoalX, 66f, attSixEdgeX, 66f)
    drawPitchSegment(attSixEdgeX, 34f, attSixEdgeX, 66f)

    // Penalty Spot
    val penSpotProj = projectPitchToScreen(attPenSpotX, 50f, 0f, w, h, attackingRight, true)
    drawCircle(color = Color.White, radius = 3.6f, center = Offset(penSpotProj.screenX, penSpotProj.groundY))

    // Penalty D-Arc
    val dArcPath = Path()
    val arcDir = if (attackingRight) -1f else 1f
    for (step in -6..6) {
        val angle = (step / 6f) * 0.95f
        val ax = attBoxEdgeX + cos(angle) * 5.2f * arcDir
        val ay = 50f + sin(angle) * 12f
        val p = projectPitchToScreen(ax, ay, 0f, w, h, attackingRight, true)
        if (step == -6) dArcPath.moveTo(p.screenX, p.groundY)
        else dArcPath.lineTo(p.screenX, p.groundY)
    }
    drawPath(dArcPath, color = lineColor, style = Stroke(width = 2.2f))

    // Corner Arcs at Attacking Corners
    val corner1 = projectPitchToScreen(attGoalX, 0f, 0f, w, h, attackingRight, true)
    drawCircle(color = lineColor, radius = 9f * corner1.depthScale, center = Offset(corner1.screenX, corner1.groundY), style = Stroke(width = 2.0f))
    val corner2 = projectPitchToScreen(attGoalX, 100f, 0f, w, h, attackingRight, true)
    drawCircle(color = lineColor, radius = 9f * corner2.depthScale, center = Offset(corner2.screenX, corner2.groundY), style = Stroke(width = 2.0f))

    // Defending Penalty Box (at bottom of perspective pitch)
    val defGoalX = if (attackingRight) 0f else 100f
    val defBoxEdgeX = if (attackingRight) 17f else 83f
    drawPitchSegment(defGoalX, 21f, defBoxEdgeX, 21f, strokeW = 1.8f, color = lineColor.copy(alpha = 0.55f))
    drawPitchSegment(defGoalX, 79f, defBoxEdgeX, 79f, strokeW = 1.8f, color = lineColor.copy(alpha = 0.55f))
    drawPitchSegment(defBoxEdgeX, 21f, defBoxEdgeX, 79f, strokeW = 1.8f, color = lineColor.copy(alpha = 0.55f))

    // ============================================================================================
    // 4. 3D REALISTIC WHITE GOALPOSTS, CROSSBAR & VOLUMETRIC NET (Photos 1, 2, 3)
    // ============================================================================================
    val leftPostBase = projectPitchToScreen(attGoalX, if (attackingRight) 36.8f else 63.2f, 0f, w, h, attackingRight, true)
    val rightPostBase = projectPitchToScreen(attGoalX, if (attackingRight) 63.2f else 36.8f, 0f, w, h, attackingRight, true)
    val leftPostTop = projectPitchToScreen(attGoalX, if (attackingRight) 36.8f else 63.2f, 48f, w, h, attackingRight, true)
    val rightPostTop = projectPitchToScreen(attGoalX, if (attackingRight) 63.2f else 36.8f, 48f, w, h, attackingRight, true)

    val netDepthPx = h * 0.046f
    val netRipple = if (matchState.phase == MatchPhase.GOAL_CELEBRATION) {
        sin(animTimeSec * 22f) * 6.5f
    } else {
        0f
    }

    // Top horizontal net support poles extending back
    val rearLeftTop = Offset(leftPostTop.screenX + 4f, leftPostTop.screenY - netDepthPx * 0.42f)
    val rearRightTop = Offset(rightPostTop.screenX - 4f, rightPostTop.screenY - netDepthPx * 0.42f)
    val rearLeftBase = Offset(leftPostBase.screenX + 6f, leftPostBase.groundY - netDepthPx + netRipple)
    val rearRightBase = Offset(rightPostBase.screenX - 6f, rightPostBase.groundY - netDepthPx + netRipple)

    // Shaded interior volume inside the net (gives authentic 3D spatial depth!)
    val netVolPath = Path().apply {
        moveTo(leftPostTop.screenX, leftPostTop.screenY)
        lineTo(rightPostTop.screenX, rightPostTop.screenY)
        lineTo(rearRightBase.x, rearRightBase.y)
        lineTo(rearLeftBase.x, rearLeftBase.y)
        close()
    }
    drawPath(netVolPath, color = Color(0x28000000))

    // Back netting fill & mesh
    val netPath = Path().apply {
        moveTo(leftPostTop.screenX, leftPostTop.screenY)
        lineTo(rearLeftTop.x, rearLeftTop.y)
        lineTo(rearRightTop.x, rearRightTop.y)
        lineTo(rightPostTop.screenX, rightPostTop.screenY)
        lineTo(rearRightBase.x, rearRightBase.y)
        lineTo(rearLeftBase.x, rearLeftBase.y)
        close()
    }
    drawPath(netPath, color = Color.White.copy(alpha = 0.18f))

    // High-density perspective net mesh grid (Hexagonal / Diamond look)
    val netCols = 14
    for (c in 0..netCols) {
        val ratio = c / netCols.toFloat()
        val topX = leftPostTop.screenX + (rightPostTop.screenX - leftPostTop.screenX) * ratio
        val topY = leftPostTop.screenY
        val botX = rearLeftBase.x + (rearRightBase.x - rearLeftBase.x) * ratio
        val botY = rearLeftBase.y
        drawLine(
            color = Color.White.copy(alpha = 0.30f),
            start = Offset(topX, topY),
            end = Offset(botX, botY),
            strokeWidth = 1.0f
        )
    }
    for (r in 1..5) {
        val ratio = r / 6f
        val yRow = leftPostTop.screenY + (rearLeftBase.y - leftPostTop.screenY) * ratio + netRipple * 0.35f
        val xL = leftPostTop.screenX + (rearLeftBase.x - leftPostTop.screenX) * ratio
        val xR = rightPostTop.screenX + (rearRightBase.x - rightPostTop.screenX) * ratio
        drawLine(
            color = Color.White.copy(alpha = 0.28f),
            start = Offset(xL, yRow),
            end = Offset(xR, yRow),
            strokeWidth = 1.0f
        )
    }

    // 3D Cylindrical Goalposts & Crossbar in Brilliant White with Specular Highlight
    // Left Post
    drawLine(color = Color(0xFFCBD5E1), start = Offset(leftPostBase.screenX - 1f, leftPostBase.groundY), end = Offset(leftPostTop.screenX - 1f, leftPostTop.screenY), strokeWidth = 5.8f, cap = StrokeCap.Round)
    drawLine(color = Color.White, start = Offset(leftPostBase.screenX, leftPostBase.groundY), end = Offset(leftPostTop.screenX, leftPostTop.screenY), strokeWidth = 4.8f, cap = StrokeCap.Round)
    // Right Post
    drawLine(color = Color(0xFFCBD5E1), start = Offset(rightPostBase.screenX + 1f, rightPostBase.groundY), end = Offset(rightPostTop.screenX + 1f, rightPostTop.screenY), strokeWidth = 5.8f, cap = StrokeCap.Round)
    drawLine(color = Color.White, start = Offset(rightPostBase.screenX, rightPostBase.groundY), end = Offset(rightPostTop.screenX, rightPostTop.screenY), strokeWidth = 4.8f, cap = StrokeCap.Round)
    // Crossbar
    drawLine(color = Color(0xFFCBD5E1), start = Offset(leftPostTop.screenX, leftPostTop.screenY + 1f), end = Offset(rightPostTop.screenX, rightPostTop.screenY + 1f), strokeWidth = 5.8f, cap = StrokeCap.Round)
    drawLine(color = Color.White, start = Offset(leftPostTop.screenX, leftPostTop.screenY), end = Offset(rightPostTop.screenX, rightPostTop.screenY), strokeWidth = 4.8f, cap = StrokeCap.Round)

    // ============================================================================================
    // 5. GLOWING GREEN TARGET RINGS ("90'A ASMA" TARGET RETICLE - Photo 2)
    // ============================================================================================
    if (matchState.phase == MatchPhase.CRITICAL_DECISION && matchState.activeDecision?.isDefensive == false) {
        val goalW = rightPostTop.screenX - leftPostTop.screenX
        val goalH = leftPostBase.groundY - leftPostTop.screenY
        val ringR = (goalW * 0.11f).coerceIn(12f, 24f)
        val pulse = 0.55f + 0.35f * sin(animTimeSec * 6.5f)
        val rotDeg = animTimeSec * 60f

        // Top-Left 90 Corner
        val tlCenter = Offset(leftPostTop.screenX + ringR * 1.25f, leftPostTop.screenY + ringR * 1.15f)
        drawSegmentedGreenTargetRing(center = tlCenter, radius = ringR, pulse = pulse, rotDeg = rotDeg)

        // Top-Right 90 Corner
        val trCenter = Offset(rightPostTop.screenX - ringR * 1.25f, rightPostTop.screenY + ringR * 1.15f)
        drawSegmentedGreenTargetRing(center = trCenter, radius = ringR, pulse = pulse, rotDeg = -rotDeg)
    }
}

/**
 * Draws the signature Soccer Superstar / NSS bright green segmented target ring (Photo 2).
 */
private fun DrawScope.drawSegmentedGreenTargetRing(
    center: Offset,
    radius: Float,
    pulse: Float,
    rotDeg: Float
) {
    val greenGlow = Color(0xFF22C55E)
    // Ambient soft glow circle
    drawCircle(color = greenGlow.copy(alpha = pulse * 0.28f), radius = radius * 1.35f, center = center)

    // 4 Rotating segmented outer arcs with gaps (identical to Photo 2!)
    val arcLen = 65f
    val gapLen = 25f
    for (i in 0..3) {
        val startAngle = rotDeg + i * (arcLen + gapLen)
        drawArc(
            color = greenGlow,
            startAngle = startAngle,
            sweepAngle = arcLen,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = 3.2f, cap = StrokeCap.Round)
        )
    }

    // Inner glowing ring & center bullseye
    drawCircle(color = Color.White.copy(alpha = 0.85f), radius = radius * 0.42f, center = center, style = Stroke(width = 1.6f))
    drawCircle(color = Color.White, radius = 2.8f, center = center)
}

private fun DrawScope.drawFlat2DPitch(w: Float, h: Float) {
    val stripeCount = 12
    val stripeW = w / stripeCount
    for (i in 0 until stripeCount) {
        drawRect(
            color = if (i % 2 == 0) Color(0xFF0B3525) else Color(0xFF0F422E),
            topLeft = Offset(i * stripeW, 0f),
            size = Size(stripeW + 1f, h)
        )
    }
    val padX = w * 0.04f
    val padY = h * 0.05f
    val pitchW = w - padX * 2f
    val pitchH = h - padY * 2f
    val lineStroke = Stroke(width = 2.2f)
    val lineColor = Color.White.copy(alpha = 0.82f)

    drawRect(color = lineColor, topLeft = Offset(padX, padY), size = Size(pitchW, pitchH), style = lineStroke)
    drawLine(color = lineColor, start = Offset(w * 0.5f, padY), end = Offset(w * 0.5f, h - padY), strokeWidth = 2.2f)
    drawCircle(color = lineColor, radius = pitchH * 0.18f, center = Offset(w * 0.5f, h * 0.5f), style = lineStroke)

    val boxW = pitchW * 0.16f
    val boxH = pitchH * 0.54f
    val boxTop = (h - boxH) * 0.5f
    drawRect(color = lineColor, topLeft = Offset(padX, boxTop), size = Size(boxW, boxH), style = lineStroke)
    drawRect(color = lineColor, topLeft = Offset(w - padX - boxW, boxTop), size = Size(boxW, boxH), style = lineStroke)
}

private fun DrawScope.drawPlayableMomentTacticalGuides(
    matchState: LiveMatchUiState,
    w: Float,
    h: Float,
    attackingRight: Boolean,
    usePerspective: Boolean,
    animTimeSec: Float
) {
    val pulse = 0.45f + 0.35f * sin(animTimeSec * 5.5f)

    // Draw glowing forward run arrows for teammates making runs (Photo 4)
    matchState.players.filter { it.isMakingRun }.forEach { runner ->
        val isTeammate = runner.isHome == matchState.userIsHome
        val guideColor = if (isTeammate) ChampionshipGold else MatchLossRed
        val startP = projectPitchToScreen(runner.x, runner.y, 0f, w, h, attackingRight, usePerspective)
        val endP = projectPitchToScreen(runner.runTargetX, runner.runTargetY, 0f, w, h, attackingRight, usePerspective)

        drawLine(
            color = guideColor.copy(alpha = 0.85f),
            start = Offset(startP.screenX, startP.groundY),
            end = Offset(endP.screenX, endP.groundY),
            strokeWidth = 3.6f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), phase = -animTimeSec * 45f),
            cap = StrokeCap.Round
        )

        // Arrowhead at the run target
        val dx = endP.screenX - startP.screenX
        val dy = endP.groundY - startP.groundY
        val angle = atan2(dy, dx)
        val arrowLen = 14f * endP.depthScale
        val a1x = endP.screenX - cos(angle - 0.45f) * arrowLen
        val a1y = endP.groundY - sin(angle - 0.45f) * arrowLen
        val a2x = endP.screenX - cos(angle + 0.45f) * arrowLen
        val a2y = endP.groundY - sin(angle + 0.45f) * arrowLen
        val arrowPath = Path().apply {
            moveTo(endP.screenX, endP.groundY)
            lineTo(a1x, a1y)
            lineTo(a2x, a2y)
            close()
        }
        drawPath(arrowPath, color = guideColor)

        // Pulsing pass/intercept target ring
        drawCircle(
            color = guideColor.copy(alpha = pulse * 0.45f),
            radius = 18f * endP.depthScale,
            center = Offset(endP.screenX, endP.groundY)
        )
        drawCircle(
            color = guideColor,
            radius = 14f * endP.depthScale,
            center = Offset(endP.screenX, endP.groundY),
            style = Stroke(width = 2.4f)
        )
    }
}

/**
 * Draws the Soccer Superstar animated tutorial swipe gesture guide (Photo 2).
 * Shows a stylish 3D hand and banana-curve trajectory demonstrating the swipe.
 */
private fun DrawScope.drawGestureGuideHandAndHint(
    matchState: LiveMatchUiState,
    w: Float,
    h: Float,
    attackingRight: Boolean,
    usePerspective: Boolean,
    animTimeSec: Float
) {
    val ballProj = projectPitchToScreen(matchState.ball.x, matchState.ball.y, 0f, w, h, attackingRight, usePerspective)
    val attGoalX = if (attackingRight) 97.2f else 2.8f
    val targetCornerY = if (matchState.ball.y < 50f) 61f else 39f
    val cornerProj = projectPitchToScreen(attGoalX, targetCornerY, 38f, w, h, attackingRight, usePerspective)

    // Control point arching around the wall
    val midPitchX = (matchState.ball.x + attGoalX) * 0.5f
    val curveDirY = if (matchState.ball.y < 50f) matchState.ball.y - 18f else matchState.ball.y + 18f
    val ctrlProj = projectPitchToScreen(midPitchX, curveDirY, 26f, w, h, attackingRight, usePerspective)

    // Draw dashed banana hint arc
    val guideArc = Path().apply {
        moveTo(ballProj.screenX, ballProj.screenY)
        quadraticBezierTo(ctrlProj.screenX, ctrlProj.screenY, cornerProj.screenX, cornerProj.screenY)
    }
    drawPath(
        path = guideArc,
        color = Color.White.copy(alpha = 0.45f),
        style = Stroke(width = 3.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), phase = -animTimeSec * 35f), cap = StrokeCap.Round)
    )

    // Animated 3D white guide hand gliding from ball to corner in a loop (0..1)
    val loopT = (animTimeSec * 0.75f) % 1.0f
    val u = 1f - loopT
    val handX = (1f - u) * (1f - u) * ballProj.screenX + 2f * (1f - u) * u * ctrlProj.screenX + u * u * cornerProj.screenX
    val handY = (1f - u) * (1f - u) * ballProj.screenY + 2f * (1f - u) * u * ctrlProj.screenY + u * u * cornerProj.screenY
    val handAlpha = when {
        loopT < 0.15f -> loopT / 0.15f
        loopT > 0.82f -> (1f - loopT) / 0.18f
        else -> 1.0f
    }

    // Hand Icon: Stylized pointing index finger and palm
    val handColor = Color.White.copy(alpha = handAlpha * 0.95f)
    val handShadow = Color.Black.copy(alpha = handAlpha * 0.35f)
    // Soft shadow underneath hand
    drawCircle(color = handShadow, radius = 12f, center = Offset(handX + 3f, handY + 4f))
    // Palm
    drawRoundRect(
        color = handColor,
        topLeft = Offset(handX - 9f, handY - 6f),
        size = Size(18f, 20f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    // Pointing Index Finger
    drawRoundRect(
        color = handColor,
        topLeft = Offset(handX - 4f, handY - 22f),
        size = Size(8f, 20f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Ring border
    drawCircle(
        color = AccentBright.copy(alpha = handAlpha * 0.75f),
        radius = 8f,
        center = Offset(handX, handY - 22f),
        style = Stroke(width = 1.8f)
    )
}

/**
 * Draws the finger-drawn curved trajectory arc with luminous bloom and 3D turf shadow (Photos 1, 2, 3).
 */
private fun DrawScope.drawActiveTrajectoryPreview(
    preview: GestureTrajectoryInput,
    w: Float,
    h: Float,
    attackingRight: Boolean,
    usePerspective: Boolean,
    animTimeSec: Float
) {
    val isPass = preview.inferredKind in listOf(
        GestureActionKind.THROUGH_PASS,
        GestureActionKind.CROSS_PASS,
        GestureActionKind.SHORT_PASS
    )
    val primaryColor = when {
        preview.blockedByDefenderWarning -> MatchLossRed
        isPass -> ChampionshipGold // Yellow passing arrow as in Photo 4!
        else -> Color.White // Brilliant luminous white curve as in Photos 1, 2, 3!
    }

    // 1. 3D Ground Shadow along the turf
    val groundShadowPath = Path()
    val elevatedCurvePath = Path()

    preview.pitchPoints.forEachIndexed { idx, pt ->
        val groundProj = projectPitchToScreen(pt.x, pt.y, 0f, w, h, attackingRight, usePerspective)
        val airProj = projectPitchToScreen(pt.x, pt.y, pt.z, w, h, attackingRight, usePerspective)
        if (idx == 0) {
            groundShadowPath.moveTo(groundProj.screenX, groundProj.groundY)
            elevatedCurvePath.moveTo(airProj.screenX, airProj.screenY)
        } else {
            groundShadowPath.lineTo(groundProj.screenX, groundProj.groundY)
            elevatedCurvePath.lineTo(airProj.screenX, airProj.screenY)
        }
    }

    drawPath(
        path = groundShadowPath,
        color = Color.Black.copy(alpha = 0.45f),
        style = Stroke(
            width = 3.6f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        )
    )

    // 2. Soft Outer Ambient Bloom
    drawPath(
        path = elevatedCurvePath,
        color = (if (isPass) ChampionshipGold else Color.White).copy(alpha = 0.28f),
        style = Stroke(width = 22f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // 3. Medium Vibrant Beam
    drawPath(
        path = elevatedCurvePath,
        color = (if (isPass) ChampionshipGold else Color(0xFFE2E8F0)).copy(alpha = 0.70f),
        style = Stroke(width = 11f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // 4. Solid Brilliant White / Yellow Core with Rapid Animated Light Flow
    drawPath(
        path = elevatedCurvePath,
        color = primaryColor,
        style = Stroke(
            width = 4.8f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 12f), phase = -animTimeSec * 110f)
        )
    )

    // 5. Target Reticle or Passing Arrowhead at Endpoint
    val endPt = preview.pitchPoints.last()
    val endProj = projectPitchToScreen(endPt.x, endPt.y, endPt.z, w, h, attackingRight, usePerspective)
    val reticleRadius = 16f * endProj.depthScale

    if (isPass) {
        // Yellow passing arrowhead (Photo 4)
        val pPrev = preview.pitchPoints[preview.pitchPoints.size - 2]
        val prevProj = projectPitchToScreen(pPrev.x, pPrev.y, pPrev.z, w, h, attackingRight, usePerspective)
        val dx = endProj.screenX - prevProj.screenX
        val dy = endProj.screenY - prevProj.screenY
        val angle = atan2(dy, dx)
        val arrowLen = 18f * endProj.depthScale
        val a1x = endProj.screenX - cos(angle - 0.45f) * arrowLen
        val a1y = endProj.screenY - sin(angle - 0.45f) * arrowLen
        val a2x = endProj.screenX - cos(angle + 0.45f) * arrowLen
        val a2y = endProj.screenY - sin(angle + 0.45f) * arrowLen
        val arrowPath = Path().apply {
            moveTo(endProj.screenX, endProj.screenY)
            lineTo(a1x, a1y)
            lineTo(a2x, a2y)
            close()
        }
        drawPath(arrowPath, color = ChampionshipGold)
    } else {
        // Segmented green/white reticle for shot
        drawCircle(color = AccentBright.copy(alpha = 0.30f), radius = reticleRadius * 1.45f, center = Offset(endProj.screenX, endProj.screenY))
        drawCircle(color = Color.White, radius = reticleRadius, center = Offset(endProj.screenX, endProj.screenY), style = Stroke(width = 3.0f))
        drawCircle(color = AccentBright, radius = 4.5f, center = Offset(endProj.screenX, endProj.screenY))
    }
}

private fun DrawScope.drawBallFlightTrail(
    trail: List<PitchPoint>,
    curveSpin: Float,
    w: Float,
    h: Float,
    attackingRight: Boolean,
    usePerspective: Boolean
) {
    val trailColor = if (abs(curveSpin) >= 12f) AccentBright else Color.White
    for (i in 1 until trail.size) {
        val p0 = projectPitchToScreen(trail[i - 1].x, trail[i - 1].y, trail[i - 1].z, w, h, attackingRight, usePerspective)
        val p1 = projectPitchToScreen(trail[i].x, trail[i].y, trail[i].z, w, h, attackingRight, usePerspective)
        val progress = i / trail.size.toFloat()
        drawLine(
            color = trailColor.copy(alpha = progress * 0.80f),
            start = Offset(p0.screenX, p0.screenY),
            end = Offset(p1.screenX, p1.screenY),
            strokeWidth = (progress * 9f).coerceAtLeast(1.8f),
            cap = StrokeCap.Round
        )
    }
}

/**
 * High-fidelity 2.5D Player Renderer with stylized human anatomy:
 * Head, hair, skin tones, team jerseys with sleeves, numbers, shorts, socks, boots,
 * dynamic poses (defensive wall, wall jump, goalkeeper dive, bicycle kick, striker strike)
 * and realistic directional turf cast shadows (Photos 1, 2, 3, 4).
 */
private fun DrawScope.drawPitchPlayerMarker(
    player: PitchPlayerNode,
    proj: ProjectedPoint,
    baseRadius: Float,
    homeColor: Color,
    awayColor: Color,
    animTimeSec: Float,
    isPlayableMoment: Boolean,
    numberPaint: android.graphics.Paint,
    tagPaint: android.graphics.Paint
) {
    val r = (baseRadius * proj.depthScale).coerceIn(8.5f, 22f)
    val fill = if (player.isHome) homeColor else awayColor
    val kitShorts = if (player.isHome) Color.White else Color(0xFF1E293B)
    val elevPx = player.elevationZ * proj.depthScale * 1.5f

    val px = proj.screenX + (if (player.isDivingOrSliding) player.diveDirectionY * r * 1.1f else 0f)
    val py = proj.screenY - elevPx

    // 1. Realistic Soft Directional Turf Cast Shadow
    val shadowScale = (1.0f - (player.elevationZ / 45f)).coerceIn(0.40f, 1.0f)
    drawOval(
        color = Color.Black.copy(alpha = 0.52f * shadowScale),
        topLeft = Offset(px - r * 1.05f * shadowScale + r * 0.35f, proj.groundY - r * 0.25f * shadowScale + r * 0.40f),
        size = Size(r * 2.1f * shadowScale, r * 0.85f * shadowScale)
    )

    // 2. User Player Glowing Ring & Halo (Immediate tracking!)
    if (player.isUser) {
        val haloPulse = 1.65f + 0.22f * sin(animTimeSec * 6f)
        drawCircle(
            color = AccentBright.copy(alpha = 0.30f),
            radius = r * haloPulse,
            center = Offset(px, proj.groundY)
        )
        drawCircle(
            color = AccentBright,
            radius = r * 1.38f,
            center = Offset(px, proj.groundY),
            style = Stroke(width = 2.8f)
        )
    }

    // 3. Ball Carrier Gold Ring
    if (player.hasBall) {
        drawCircle(
            color = ChampionshipGold,
            radius = r * 1.32f,
            center = Offset(px, proj.groundY),
            style = Stroke(width = 2.4f)
        )
    }

    // 4. Stylized Anatomy & Kit by Visual Pose
    val skinTone = if (player.isUser) Color(0xFFF6D8B8) else when (player.number % 3) {
        0 -> Color(0xFFE0AC69)
        1 -> Color(0xFF8D5524)
        else -> Color(0xFFF6D8B8)
    }
    // Hair color (Player #7 or user gets stylish pink hair as in Photos 1 & 3!)
    val hairColor = if (player.number == 7 || (player.isUser && player.hairColorHex.isEmpty())) {
        Color(0xFFF43F5E) // Bright Neon Pink Hair! (Photos 1 & 3)
    } else when (player.number % 4) {
        0 -> Color(0xFFEAB308) // Blond
        1 -> Color(0xFF1E293B) // Dark Black
        2 -> Color(0xFF78350F) // Brown
        else -> Color(0xFF475569) // Charcoal Buzzcut
    }

    when (player.visualPose) {
        // ----------------------------------------------------------------------------------------
        // DEFENSIVE WALL / WALL JUMP (Photo 3: 5 Defenders standing side-by-side with arms folded)
        // ----------------------------------------------------------------------------------------
        PlayerVisualPose.DEFENSIVE_WALL, PlayerVisualPose.WALL_JUMP -> {
            // Legs & Socks
            drawRect(color = Color.White, topLeft = Offset(px - r * 0.55f, py + r * 0.45f), size = Size(r * 0.40f, r * 0.75f))
            drawRect(color = Color.White, topLeft = Offset(px + r * 0.15f, py + r * 0.45f), size = Size(r * 0.40f, r * 0.75f))
            // Boots
            drawRect(color = Color(0xFF0F172A), topLeft = Offset(px - r * 0.60f, py + r * 1.15f), size = Size(r * 0.48f, r * 0.30f))
            drawRect(color = Color(0xFF0F172A), topLeft = Offset(px + r * 0.12f, py + r * 1.15f), size = Size(r * 0.48f, r * 0.30f))
            // Shorts
            drawRoundRect(color = kitShorts, topLeft = Offset(px - r * 0.75f, py + r * 0.15f), size = Size(r * 1.50f, r * 0.55f), cornerRadius = CornerRadius(3f, 3f))
            // Torso & Jersey
            drawRoundRect(color = fill, topLeft = Offset(px - r * 0.85f, py - r * 0.75f), size = Size(r * 1.70f, r * 1.0f), cornerRadius = CornerRadius(5f, 5f))
            // Folded Arms protecting chest/groin (Wall stance!)
            drawRoundRect(color = skinTone, topLeft = Offset(px - r * 0.65f, py - r * 0.15f), size = Size(r * 1.30f, r * 0.40f), cornerRadius = CornerRadius(3f, 3f))
            // Head & Hair
            drawCircle(color = skinTone, radius = r * 0.48f, center = Offset(px, py - r * 1.15f))
            drawArc(
                color = hairColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(px - r * 0.50f, py - r * 1.65f),
                size = Size(r * 1.0f, r * 0.95f)
            )
        }

        // ----------------------------------------------------------------------------------------
        // GOALKEEPER READY & HORIZONTAL AIRBORNE DIVE (Photos 1, 3, 4)
        // ----------------------------------------------------------------------------------------
        PlayerVisualPose.GK_READY -> {
            val gkKit = Color(0xFF22C55E) // Bright Fluorescent Lime GK Kit!
            // Legs spread wide
            drawRect(color = Color(0xFF166534), topLeft = Offset(px - r * 0.85f, py + r * 0.40f), size = Size(r * 0.42f, r * 0.70f))
            drawRect(color = Color(0xFF166534), topLeft = Offset(px + r * 0.43f, py + r * 0.40f), size = Size(r * 0.42f, r * 0.70f))
            // Shorts
            drawRoundRect(color = Color(0xFF14532D), topLeft = Offset(px - r * 0.85f, py + r * 0.15f), size = Size(r * 1.70f, r * 0.50f), cornerRadius = CornerRadius(3f, 3f))
            // Torso
            drawRoundRect(color = gkKit, topLeft = Offset(px - r * 0.88f, py - r * 0.75f), size = Size(r * 1.76f, r * 0.98f), cornerRadius = CornerRadius(5f, 5f))
            // Outstretched Arms with White Goalkeeper Gloves!
            drawCircle(color = Color.White, radius = r * 0.32f, center = Offset(px - r * 1.15f, py - r * 0.15f))
            drawCircle(color = Color.White, radius = r * 0.32f, center = Offset(px + r * 1.15f, py - r * 0.15f))
            // Head
            drawCircle(color = skinTone, radius = r * 0.48f, center = Offset(px, py - r * 1.15f))
            drawArc(color = hairColor, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(px - r * 0.50f, py - r * 1.65f), size = Size(r * 1.0f, r * 0.95f))
        }

        PlayerVisualPose.GK_DIVE -> {
            val gkKit = Color(0xFF22C55E)
            val diveDir = player.diveDirectionY.coerceIn(-1f, 1f)
            // Horizontally stretched diving capsule
            drawRoundRect(
                color = gkKit,
                topLeft = Offset(px - r * 1.65f, py - r * 0.65f),
                size = Size(r * 3.3f, r * 1.30f),
                cornerRadius = CornerRadius(r * 0.65f, r * 0.65f)
            )
            // Stretched Arms & Gloves reaching toward goal corner!
            drawCircle(color = Color.White, radius = r * 0.40f, center = Offset(px + diveDir * r * 1.85f, py - r * 0.20f))
            // Head
            drawCircle(color = skinTone, radius = r * 0.48f, center = Offset(px + diveDir * r * 1.15f, py - r * 0.55f))
        }

        // ----------------------------------------------------------------------------------------
        // ACROBATIC BICYCLE KICK / RÖVEŞATA (Photo 1)
        // ----------------------------------------------------------------------------------------
        PlayerVisualPose.BICYCLE_KICK -> {
            // Body tilted mid-air upside down!
            val angleDeg = -55f
            // Striking Leg raised high toward ball
            drawRoundRect(color = Color.White, topLeft = Offset(px - r * 0.35f, py - r * 1.65f), size = Size(r * 0.45f, r * 1.10f), cornerRadius = CornerRadius(3f, 3f))
            // Boot striking high
            drawRect(color = Color(0xFF0F172A), topLeft = Offset(px - r * 0.45f, py - r * 1.85f), size = Size(r * 0.65f, r * 0.35f))
            // Torso angled
            drawRoundRect(color = fill, topLeft = Offset(px - r * 0.85f, py - r * 0.55f), size = Size(r * 1.70f, r * 1.10f), cornerRadius = CornerRadius(6f, 6f))
            // Trailing leg
            drawRoundRect(color = Color.White, topLeft = Offset(px + r * 0.35f, py + r * 0.15f), size = Size(r * 0.45f, r * 0.85f), cornerRadius = CornerRadius(3f, 3f))
            // Head with Pink Hair (Photo 1)
            drawCircle(color = skinTone, radius = r * 0.50f, center = Offset(px - r * 0.85f, py + r * 0.55f))
            drawCircle(color = hairColor, radius = r * 0.52f, center = Offset(px - r * 0.95f, py + r * 0.50f))
        }

        // ----------------------------------------------------------------------------------------
        // KICK / STRIKER SHOT POSE (Photos 2 & 3: #7 Kicking stance)
        // ----------------------------------------------------------------------------------------
        PlayerVisualPose.KICK_STRIKE -> {
            // Plant foot firmly down
            drawRect(color = Color.White, topLeft = Offset(px - r * 0.65f, py + r * 0.35f), size = Size(r * 0.38f, r * 0.80f))
            drawRect(color = Color(0xFF0F172A), topLeft = Offset(px - r * 0.70f, py + r * 1.10f), size = Size(r * 0.46f, r * 0.28f))
            // Kicking leg swung back / in follow-through
            drawRect(color = Color.White, topLeft = Offset(px + r * 0.35f, py + r * 0.20f), size = Size(r * 0.42f, r * 0.75f))
            drawRect(color = Color(0xFF0F172A), topLeft = Offset(px + r * 0.45f, py + r * 0.90f), size = Size(r * 0.50f, r * 0.28f))
            // Shorts
            drawRoundRect(color = kitShorts, topLeft = Offset(px - r * 0.80f, py + r * 0.10f), size = Size(r * 1.60f, r * 0.52f), cornerRadius = CornerRadius(3f, 3f))
            // Torso & Jersey
            drawRoundRect(color = fill, topLeft = Offset(px - r * 0.88f, py - r * 0.80f), size = Size(r * 1.76f, r * 1.02f), cornerRadius = CornerRadius(6f, 6f))
            // Balanced Arms
            drawRoundRect(color = skinTone, topLeft = Offset(px - r * 1.15f, py - r * 0.65f), size = Size(r * 0.36f, r * 0.75f), cornerRadius = CornerRadius(3f, 3f))
            drawRoundRect(color = skinTone, topLeft = Offset(px + r * 0.80f, py - r * 0.65f), size = Size(r * 0.36f, r * 0.75f), cornerRadius = CornerRadius(3f, 3f))
            // Head & Hair
            drawCircle(color = skinTone, radius = r * 0.50f, center = Offset(px, py - r * 1.20f))
            drawArc(color = hairColor, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(px - r * 0.52f, py - r * 1.72f), size = Size(r * 1.04f, r * 0.98f))
        }

        // ----------------------------------------------------------------------------------------
        // NORMAL ATHLETIC RUNNING / STANDING POSE
        // ----------------------------------------------------------------------------------------
        PlayerVisualPose.NORMAL -> {
            // Running Legs & Socks
            val legSwing = if (player.isMakingRun) sin(animTimeSec * 12f) * r * 0.25f else 0f
            drawRect(color = Color.White, topLeft = Offset(px - r * 0.52f, py + r * 0.38f + legSwing), size = Size(r * 0.38f, r * 0.75f))
            drawRect(color = Color.White, topLeft = Offset(px + r * 0.14f, py + r * 0.38f - legSwing), size = Size(r * 0.38f, r * 0.75f))
            // Boots
            drawRect(color = Color(0xFF0F172A), topLeft = Offset(px - r * 0.56f, py + r * 1.10f + legSwing), size = Size(r * 0.44f, r * 0.28f))
            drawRect(color = Color(0xFF0F172A), topLeft = Offset(px + r * 0.10f, py + r * 1.10f - legSwing), size = Size(r * 0.44f, r * 0.28f))
            // Shorts
            drawRoundRect(color = kitShorts, topLeft = Offset(px - r * 0.78f, py + r * 0.12f), size = Size(r * 1.56f, r * 0.50f), cornerRadius = CornerRadius(3f, 3f))
            // Torso & Jersey
            drawRoundRect(color = fill, topLeft = Offset(px - r * 0.85f, py - r * 0.78f), size = Size(r * 1.70f, r * 0.98f), cornerRadius = CornerRadius(5f, 5f))
            // Specular Jersey Highlight
            drawCircle(color = Color.White.copy(alpha = 0.20f), radius = r * 0.42f, center = Offset(px - r * 0.25f, py - r * 0.40f))
            // Head & Hair
            drawCircle(color = skinTone, radius = r * 0.48f, center = Offset(px, py - r * 1.18f))
            drawArc(color = hairColor, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(px - r * 0.50f, py - r * 1.68f), size = Size(r * 1.0f, r * 0.95f))
        }
        else -> {}
    }

    // 5. Squad Number on Jersey Back (Crisp athletic typography!)
    if (player.visualPose != PlayerVisualPose.GK_DIVE) {
        numberPaint.textSize = (r * 0.92f).coerceIn(12f, 22f)
        numberPaint.color = if (player.isUser) android.graphics.Color.WHITE else android.graphics.Color.WHITE
        drawContext.canvas.nativeCanvas.drawText(
            player.number.toString(),
            px,
            py - r * 0.08f,
            numberPaint
        )
    }

    // 6. Floating Role / Run Tag above key players during Playable Moments
    if (isPlayableMoment && player.roleTag.isNotEmpty()) {
        val tagText = if (player.isMakingRun) "${player.name} (${player.roleTag})" else player.roleTag
        tagPaint.textSize = (r * 0.75f).coerceIn(11f, 17f)
        tagPaint.color = when {
            player.isUser -> android.graphics.Color.rgb(40, 240, 146)
            player.isMakingRun -> android.graphics.Color.rgb(255, 214, 64)
            else -> android.graphics.Color.WHITE
        }
        drawContext.canvas.nativeCanvas.drawText(
            tagText,
            px,
            py - r * 1.85f,
            tagPaint
        )
    }
}

/**
 * 3D Soccer Ball with realistic pentagon pattern, loft elevation, and ground shadow (Photo 1).
 */
private fun DrawScope.draw3DSoccerBall(
    ballProj: ProjectedPoint,
    ballZ: Float,
    baseRadius: Float,
    spinDeg: Float,
    isPlayableMoment: Boolean,
    animTimeSec: Float
) {
    val loftScale = 1f + (ballZ / 110f).coerceIn(0f, 0.45f)
    val ballR = (baseRadius * ballProj.depthScale * loftScale).coerceIn(6.5f, 15f)

    // 1. Ground shadow directly on the turf at (ballProj.screenX, ballProj.groundY)
    val shadowScale = (1f - (ballZ / 95f)).coerceIn(0.35f, 1f)
    drawOval(
        color = Color.Black.copy(alpha = 0.55f * shadowScale),
        topLeft = Offset(ballProj.screenX - ballR * shadowScale, ballProj.groundY - ballR * 0.4f * shadowScale + 3f),
        size = Size(ballR * 2f * shadowScale, ballR * 0.9f * shadowScale)
    )

    // 2. Pulsing Touch Halo when waiting for user's finger gesture in Slow-Mo
    if (isPlayableMoment) {
        val haloPulse = 1.9f + 0.45f * sin(animTimeSec * 7f)
        drawCircle(
            color = AccentBright.copy(alpha = 0.32f),
            radius = ballR * haloPulse,
            center = Offset(ballProj.screenX, ballProj.screenY)
        )
        drawCircle(
            color = ChampionshipGold.copy(alpha = 0.85f),
            radius = ballR * 1.55f,
            center = Offset(ballProj.screenX, ballProj.screenY),
            style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), animTimeSec * 25f))
        )
    }

    // 3. Ball Sphere Body
    drawCircle(
        color = Color.White,
        radius = ballR,
        center = Offset(ballProj.screenX, ballProj.screenY)
    )

    // 4. Spinning Pentagon Seam Pattern (rotates with spinDeg!)
    val rad = spinDeg * (PI.toFloat() / 180f)
    val seamOffsetX = cos(rad) * ballR * 0.36f
    val seamOffsetY = sin(rad) * ballR * 0.36f
    drawCircle(
        color = Color(0xFF111827),
        radius = ballR * 0.34f,
        center = Offset(ballProj.screenX + seamOffsetX, ballProj.screenY + seamOffsetY)
    )
    drawCircle(
        color = Color(0xFF111827),
        radius = ballR * 0.28f,
        center = Offset(ballProj.screenX - seamOffsetX * 0.8f, ballProj.screenY - seamOffsetY * 0.8f)
    )

    // Outer Ball Rim
    drawCircle(
        color = Color(0xFF0A1711),
        radius = ballR,
        center = Offset(ballProj.screenX, ballProj.screenY),
        style = Stroke(width = 1.8f)
    )
}
