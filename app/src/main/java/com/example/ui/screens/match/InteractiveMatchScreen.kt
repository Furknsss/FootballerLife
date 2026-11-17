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
            MatchPhase.CRITICAL_DECISION -> 1.18f
            MatchPhase.ACTION_RESOLUTION -> 1.12f
            MatchPhase.GOAL_CELEBRATION -> 1.24f
            else -> 1.0f
        },
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
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
                AnimatedVisibility(
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
                    // Draw subtle raw path while starting drag
                    val rawPath = Path()
                    rawDrawnPoints.forEachIndexed { idx, pt ->
                        val proj = projectPitchToScreen(pt.x, pt.y, 0f, w, h, attackingRight, usePerspective)
                        if (idx == 0) rawPath.moveTo(proj.screenX, proj.screenY)
                        else rawPath.lineTo(proj.screenX, proj.screenY)
                    }
                    drawPath(
                        path = rawPath,
                        color = AccentBright.copy(alpha = 0.7f),
                        style = Stroke(width = 4f, cap = StrokeCap.Round)
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
    // 1. Stadium Crowd Stands & LED Ad Boards behind the attacking goal (top 16.5% of canvas)
    val standHeight = h * 0.125f
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF050C09), Color(0xFF0D1F18))
        ),
        topLeft = Offset(0f, 0f),
        size = Size(w, standHeight)
    )

    // Animated crowd dots cheering in the stands
    val crowdRows = 4
    val crowdCols = 28
    val excitement = matchState.crowdExcitementLevel
    for (r in 0 until crowdRows) {
        for (c in 0 until crowdCols) {
            val cx = (w / crowdCols) * (c + 0.5f)
            val bounce = sin(animTimeSec * (4f + excitement * 5f) + c * 0.7f + r * 1.3f) * (2.5f * excitement)
            val cy = (standHeight / (crowdRows + 1)) * (r + 1f) + bounce
            val dotColor = when ((r + c) % 4) {
                0 -> AccentBright.copy(alpha = 0.45f)
                1 -> Color.White.copy(alpha = 0.35f)
                2 -> ChampionshipGold.copy(alpha = 0.40f)
                else -> Color(0xFF38BDF8).copy(alpha = 0.30f)
            }
            drawCircle(
                color = dotColor,
                radius = 2.8f,
                center = Offset(cx, cy)
            )
        }
    }

    // Stadium LED Advertising Ribbon just behind the goal line
    val ledTop = h * 0.125f
    val ledH = h * 0.032f
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF07281B),
                Color(0xFF104932),
                Color(0xFF07281B)
            )
        ),
        topLeft = Offset(w * 0.14f, ledTop),
        size = Size(w * 0.72f, ledH),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = AccentBright.copy(alpha = 0.55f),
        topLeft = Offset(w * 0.14f, ledTop),
        size = Size(w * 0.72f, ledH),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 1.5f)
    )

    // 2. Trapezoidal Mowed Turf Bands (10 horizontal perspective bands from u=0 to u=1)
    val bandCount = 10
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
        drawPath(
            path = bandPath,
            color = if (i % 2 == 0) Color(0xFF0C3B28) else Color(0xFF104832)
        )
    }

    // 3. Perspective Pitch Boundary Lines, Halfway Line, Center Circle & Penalty Box
    val lineColor = Color.White.copy(alpha = 0.82f)
    val lineStroke = Stroke(width = 2.4f, cap = StrokeCap.Round, join = StrokeJoin.Round)

    fun drawPitchSegment(x1: Float, y1: Float, x2: Float, y2: Float, strokeW: Float = 2.4f, color: Color = lineColor) {
        val p1 = projectPitchToScreen(x1, y1, 0f, w, h, attackingRight, true)
        val p2 = projectPitchToScreen(x2, y2, 0f, w, h, attackingRight, true)
        drawLine(color = color, start = Offset(p1.screenX, p1.groundY), end = Offset(p2.screenX, p2.groundY), strokeWidth = strokeW)
    }

    // Outer touchlines & goal lines
    drawPitchSegment(0f, 0f, 100f, 0f)
    drawPitchSegment(0f, 100f, 100f, 100f)
    drawPitchSegment(0f, 0f, 0f, 100f)
    drawPitchSegment(100f, 0f, 100f, 100f)

    // Halfway line & perspective center oval
    drawPitchSegment(50f, 0f, 50f, 100f, strokeW = 2f)
    val centerCirclePath = Path()
    for (deg in 0..360 step 12) {
        val rad = deg * (PI / 180.0)
        val cx = 50f + cos(rad).toFloat() * 10f
        val cy = 50f + sin(rad).toFloat() * 14f
        val p = projectPitchToScreen(cx, cy, 0f, w, h, attackingRight, true)
        if (deg == 0) centerCirclePath.moveTo(p.screenX, p.groundY)
        else centerCirclePath.lineTo(p.screenX, p.groundY)
    }
    drawPath(centerCirclePath, color = lineColor, style = Stroke(width = 2f))

    // Attacking Penalty Box (at top of perspective pitch)
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
    drawCircle(color = Color.White, radius = 3.5f, center = Offset(penSpotProj.screenX, penSpotProj.groundY))

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

    // Defending Penalty Box (at bottom of perspective pitch)
    val defGoalX = if (attackingRight) 0f else 100f
    val defBoxEdgeX = if (attackingRight) 17f else 83f
    drawPitchSegment(defGoalX, 21f, defBoxEdgeX, 21f, strokeW = 1.8f, color = lineColor.copy(alpha = 0.55f))
    drawPitchSegment(defGoalX, 79f, defBoxEdgeX, 79f, strokeW = 1.8f, color = lineColor.copy(alpha = 0.55f))
    drawPitchSegment(defBoxEdgeX, 21f, defBoxEdgeX, 79f, strokeW = 1.8f, color = lineColor.copy(alpha = 0.55f))

    // 4. 3D Goalposts, Crossbar & Dynamic Netting at the Top Goal Line (y in 36.8..63.2, z in 0..48)
    val leftPostBase = projectPitchToScreen(attGoalX, if (attackingRight) 36.8f else 63.2f, 0f, w, h, attackingRight, true)
    val rightPostBase = projectPitchToScreen(attGoalX, if (attackingRight) 63.2f else 36.8f, 0f, w, h, attackingRight, true)
    val leftPostTop = projectPitchToScreen(attGoalX, if (attackingRight) 36.8f else 63.2f, 48f, w, h, attackingRight, true)
    val rightPostTop = projectPitchToScreen(attGoalX, if (attackingRight) 63.2f else 36.8f, 48f, w, h, attackingRight, true)

    val netDepthPx = h * 0.036f
    val netRipple = if (matchState.phase == MatchPhase.GOAL_CELEBRATION) {
        sin(animTimeSec * 22f) * 5.5f
    } else {
        0f
    }

    // Back netting fill & mesh
    val netPath = Path().apply {
        moveTo(leftPostTop.screenX, leftPostTop.screenY)
        lineTo(rightPostTop.screenX, rightPostTop.screenY)
        lineTo(rightPostBase.screenX - 6f, rightPostBase.groundY - netDepthPx + netRipple)
        lineTo(leftPostBase.screenX + 6f, leftPostBase.groundY - netDepthPx + netRipple)
        close()
    }
    drawPath(netPath, color = Color.White.copy(alpha = 0.16f))

    // Vertical & horizontal net mesh lines
    val netCols = 10
    for (c in 0..netCols) {
        val ratio = c / netCols.toFloat()
        val topX = leftPostTop.screenX + (rightPostTop.screenX - leftPostTop.screenX) * ratio
        val topY = leftPostTop.screenY
        val botX = leftPostBase.screenX + (rightPostBase.screenX - leftPostBase.screenX) * ratio
        val botY = leftPostBase.groundY
        drawLine(
            color = Color.White.copy(alpha = 0.28f),
            start = Offset(topX, topY),
            end = Offset(botX, botY),
            strokeWidth = 1f
        )
    }
    for (r in 1..4) {
        val ratio = r / 5f
        val yRow = leftPostTop.screenY + (leftPostBase.groundY - leftPostTop.screenY) * ratio + netRipple * 0.4f
        drawLine(
            color = Color.White.copy(alpha = 0.26f),
            start = Offset(leftPostTop.screenX, yRow),
            end = Offset(rightPostTop.screenX, yRow),
            strokeWidth = 1f
        )
    }

    // Highlighted Top-Corner Target Zones inside the Goal Frame during Playable Moments
    if (matchState.phase == MatchPhase.CRITICAL_DECISION && matchState.activeDecision?.isDefensive == false) {
        val pulse = 0.25f + 0.15f * sin(animTimeSec * 5f)
        val cornerW = (rightPostTop.screenX - leftPostTop.screenX) * 0.22f
        val cornerH = (leftPostBase.groundY - leftPostTop.screenY) * 0.45f
        // Left top corner target zone
        drawRoundRect(
            color = AccentBright.copy(alpha = pulse),
            topLeft = Offset(leftPostTop.screenX + 3f, leftPostTop.screenY + 3f),
            size = Size(cornerW, cornerH),
            cornerRadius = CornerRadius(6f, 6f)
        )
        // Right top corner target zone
        drawRoundRect(
            color = AccentBright.copy(alpha = pulse),
            topLeft = Offset(rightPostTop.screenX - cornerW - 3f, rightPostTop.screenY + 3f),
            size = Size(cornerW, cornerH),
            cornerRadius = CornerRadius(6f, 6f)
        )
    }

    // 3D Goalposts & Crossbar in crisp white
    drawLine(
        color = Color.White,
        start = Offset(leftPostBase.screenX, leftPostBase.groundY),
        end = Offset(leftPostTop.screenX, leftPostTop.screenY),
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White,
        start = Offset(rightPostBase.screenX, rightPostBase.groundY),
        end = Offset(rightPostTop.screenX, rightPostTop.screenY),
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White,
        start = Offset(leftPostTop.screenX, leftPostTop.screenY),
        end = Offset(rightPostTop.screenX, rightPostTop.screenY),
        strokeWidth = 5.2f,
        cap = StrokeCap.Round
    )
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

    // Draw glowing forward run arrows for teammates making runs (or opponent attacker run in red during defense)
    matchState.players.filter { it.isMakingRun }.forEach { runner ->
        val isTeammate = runner.isHome == matchState.userIsHome
        val guideColor = if (isTeammate) AccentBright else MatchLossRed
        val startP = projectPitchToScreen(runner.x, runner.y, 0f, w, h, attackingRight, usePerspective)
        val endP = projectPitchToScreen(runner.runTargetX, runner.runTargetY, 0f, w, h, attackingRight, usePerspective)

        drawLine(
            color = guideColor.copy(alpha = 0.78f),
            start = Offset(startP.screenX, startP.groundY),
            end = Offset(endP.screenX, endP.groundY),
            strokeWidth = 3.2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), phase = -animTimeSec * 40f)
        )

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
            style = Stroke(width = 2.2f)
        )
    }
}

private fun DrawScope.drawActiveTrajectoryPreview(
    preview: GestureTrajectoryInput,
    w: Float,
    h: Float,
    attackingRight: Boolean,
    usePerspective: Boolean,
    animTimeSec: Float
) {
    val primaryColor = if (preview.blockedByDefenderWarning) {
        MatchLossRed
    } else {
        parseHexColor(preview.inferredKind.badgeColorHex, AccentBright)
    }

    // 1. Ground shadow curve (z = 0) so the user sees both ground path and 3D loft!
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
            width = 3.5f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
        )
    )

    // Outer neon glow of the 3D Bezier curve
    drawPath(
        path = elevatedCurvePath,
        color = primaryColor.copy(alpha = 0.35f),
        style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Main crisp trajectory arc with animated dash flow
    drawPath(
        path = elevatedCurvePath,
        color = primaryColor,
        style = Stroke(
            width = 5.2f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f), phase = -animTimeSec * 85f)
        )
    )

    // Bright white core line
    drawPath(
        path = elevatedCurvePath,
        color = Color.White.copy(alpha = 0.88f),
        style = Stroke(width = 2.0f, cap = StrokeCap.Round)
    )

    // Target Reticle at endpoint
    val endPt = preview.pitchPoints.last()
    val endProj = projectPitchToScreen(endPt.x, endPt.y, endPt.z, w, h, attackingRight, usePerspective)
    val reticleRadius = 16f * endProj.depthScale

    drawCircle(
        color = primaryColor.copy(alpha = 0.28f),
        radius = reticleRadius * 1.45f,
        center = Offset(endProj.screenX, endProj.screenY)
    )
    drawCircle(
        color = primaryColor,
        radius = reticleRadius,
        center = Offset(endProj.screenX, endProj.screenY),
        style = Stroke(width = 3f)
    )
    drawCircle(
        color = Color.White,
        radius = 4.2f,
        center = Offset(endProj.screenX, endProj.screenY)
    )
}

private fun DrawScope.drawBallFlightTrail(
    trail: List<PitchPoint>,
    curveSpin: Float,
    w: Float,
    h: Float,
    attackingRight: Boolean,
    usePerspective: Boolean
) {
    val trailColor = if (abs(curveSpin) >= 12f) AccentBright else ChampionshipGold
    for (i in 1 until trail.size) {
        val p0 = projectPitchToScreen(trail[i - 1].x, trail[i - 1].y, trail[i - 1].z, w, h, attackingRight, usePerspective)
        val p1 = projectPitchToScreen(trail[i].x, trail[i].y, trail[i].z, w, h, attackingRight, usePerspective)
        val progress = i / trail.size.toFloat()
        drawLine(
            color = trailColor.copy(alpha = progress * 0.72f),
            start = Offset(p0.screenX, p0.screenY),
            end = Offset(p1.screenX, p1.screenY),
            strokeWidth = (progress * 8f).coerceAtLeast(1.5f),
            cap = StrokeCap.Round
        )
    }
}

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
    val px = proj.screenX + (if (player.isDivingOrSliding) player.diveDirectionY * r * 0.9f else 0f)
    val py = proj.screenY
    val fill = if (player.isHome) homeColor else awayColor

    // User Highlight Halo & Pulsing Slow-Mo Ring
    if (player.isUser) {
        val pulseScale = 1.65f + 0.22f * sin(animTimeSec * 6f)
        drawCircle(
            color = AccentBright.copy(alpha = 0.28f),
            radius = r * pulseScale,
            center = Offset(px, py)
        )
        drawCircle(
            color = AccentBright,
            radius = r * 1.42f,
            center = Offset(px, py),
            style = Stroke(width = 2.8f)
        )
    }

    // Ball Carrier Ring
    if (player.hasBall) {
        drawCircle(
            color = ChampionshipGold,
            radius = r * 1.28f,
            center = Offset(px, py),
            style = Stroke(width = 2.4f)
        )
    }

    // Ground Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.50f),
        topLeft = Offset(px - r * 0.95f, proj.groundY + r * 0.45f),
        size = Size(r * 1.9f, r * 0.75f)
    )

    // Diving Goalkeeper / Sliding Defender Stretched Body Capsule
    if (player.isDivingOrSliding) {
        drawRoundRect(
            color = fill,
            topLeft = Offset(px - r * 1.55f, py - r * 0.72f),
            size = Size(r * 3.1f, r * 1.44f),
            cornerRadius = CornerRadius(r * 0.7f, r * 0.7f)
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(px - r * 1.55f, py - r * 0.72f),
            size = Size(r * 3.1f, r * 1.44f),
            cornerRadius = CornerRadius(r * 0.7f, r * 0.7f),
            style = Stroke(width = 2f)
        )
    } else {
        // Player Shoulders & Jersey Disc
        drawCircle(
            color = fill,
            radius = r,
            center = Offset(px, py)
        )
        // Specular 3D Top Highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.24f),
            radius = r * 0.56f,
            center = Offset(px - r * 0.22f, py - r * 0.22f)
        )
        drawCircle(
            color = if (player.isUser) AccentBright else Color.White,
            radius = r,
            center = Offset(px, py),
            style = Stroke(width = 2.2f)
        )
    }

    // Jersey Number
    numberPaint.textSize = (r * 1.05f).coerceIn(14f, 24f)
    drawContext.canvas.nativeCanvas.drawText(
        player.number.toString(),
        px,
        py + r * 0.36f,
        numberPaint
    )

    // Floating Role / Run Tag above key players during Playable Moments
    if (isPlayableMoment && player.roleTag.isNotEmpty()) {
        val tagText = if (player.isMakingRun) "${player.name} (${player.roleTag})" else player.roleTag
        tagPaint.textSize = (r * 0.78f).coerceIn(12f, 18f)
        tagPaint.color = when {
            player.isUser -> android.graphics.Color.rgb(40, 240, 146)
            player.isMakingRun -> android.graphics.Color.rgb(255, 214, 64)
            else -> android.graphics.Color.WHITE
        }
        drawContext.canvas.nativeCanvas.drawText(
            tagText,
            px,
            py - r * 1.45f,
            tagPaint
        )
    }
}

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
