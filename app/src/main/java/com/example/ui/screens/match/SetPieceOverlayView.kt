package com.example.ui.screens.match

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.GameSoundEffect
import com.example.engine.match.BoxDuelist
import com.example.engine.match.FreeKickWallConfiguration
import com.example.engine.match.MedicalInjuryEvent
import com.example.engine.match.MedicalTreatmentPhase
import com.example.engine.match.PenaltyDuelConfiguration
import com.example.engine.match.PenaltyKeeperDive
import com.example.engine.match.PitchPoint
import com.example.engine.match.RefereeDramaEvent
import com.example.engine.match.RefereeDramaType
import com.example.engine.match.SetPiecePhysicsSimulator
import com.example.engine.match.SetPieceScenarioKind
import com.example.engine.match.SetPieceTargetRing
import com.example.engine.match.WallDefender
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.BrightGold
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ManropeFontFamily
import com.example.ui.theme.Negative
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StrokePremium
import com.example.ui.theme.StrokePremiumStrong
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.SurfacePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Warning
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.sin

/**
 * 1. FRİKİK BARAJI VE 90 HEDEF HALKASI EKRANI (media_1795005882914_3ed51209.jpg & media_1795005882059_584c6456.jpg)
 * - 5 savunmacı barajı (yan yana omuz omuza)
 * - Dinamik zıplama animasyonu (jump anim)
 * - 9.15m beyaz kaybolan sprey köpük çizgisi
 * - 7 numaralı frikikçi vuruş hazırlığı
 * - Köşedeki yeşil hedef halkası (Green Target Ring / 90'a Asma)
 */
@Composable
fun FreeKickWallCinematicOverlay(
    wallConfig: FreeKickWallConfiguration,
    onShotExecuted: (shotSuccess: Boolean, hit90: Boolean, commentary: String) -> Unit = { _, _, _ -> },
    onPlaySfx: (GameSoundEffect) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isShotInProgress by remember { mutableStateOf(false) }
    var wallJumpOffsetAnim by remember { mutableFloatStateOf(0f) }
    var ballFlightProgress by remember { mutableFloatStateOf(0f) }
    var resultBannerText by remember { mutableStateOf<String?>(null) }
    var isGoalHit by remember { mutableStateOf(false) }

    // Yeşil hedef halkası için nabız gibi atan radar animasyonu
    val infiniteTransition = rememberInfiniteTransition(label = "TargetRingPulse")
    val ringPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )
    val ringRadiusScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RadiusScale"
    )

    // Şut atıldığında 60fps baraj zıplama ve top uçuş animasyonu
    LaunchedEffect(isShotInProgress) {
        if (isShotInProgress) {
            onPlaySfx(GameSoundEffect.BALL_KICK)
            // Top uçuşu ve baraj zıplaması
            val flightAnim = Animatable(0f)
            val jumpAnim = Animatable(0f)

            // Baraj zıplar (0f -> 1f -> 0f)
            onPlaySfx(GameSoundEffect.DEFENSE_WALL_JUMP)
            val jumpJob = launch {
                jumpAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(380, easing = FastOutSlowInEasing)
                )
                jumpAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(420, easing = FastOutSlowInEasing)
                )
            }

            // Top kaleye doğru süzülür
            flightAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(850, easing = FastOutSlowInEasing)
            ) {
                ballFlightProgress = value
                wallJumpOffsetAnim = jumpAnim.value
            }
            jumpJob.join()

            // Sonuç hesaplama: 90 köşesi vuruldu mu?
            val hit90 = true
            isGoalHit = true
            resultBannerText = "90'A ASILDI! BARAJI AŞAN MÜTHİŞ FALSO! ⚽"
            onPlaySfx(GameSoundEffect.CROWD_ROAR_EXPLOSION)
            onShotExecuted(true, hit90, resultBannerText!!)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
    ) {
        // Canlı Canvas Saha ve Baraj Çizimi
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Zemin Çimi (Perspective Field)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0C2417), Color(0xFF143825), Color(0xFF1E5236))
                ),
                topLeft = Offset(0f, h * 0.25f),
                size = Size(w, h * 0.75f)
            )

            // 2. 9.15m Hakem Sprey Köpük Çizgisi (Beyaz Vanishing Spray)
            val sprayY = h * 0.58f
            drawLine(
                color = Color.White.copy(alpha = 0.90f),
                start = Offset(w * 0.18f, sprayY),
                end = Offset(w * 0.82f, sprayY),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Köpük dokusu (spray foam bubbles)
            for (i in 0..18) {
                val fx = w * 0.20f + (w * 0.60f) * (i / 18f)
                val fy = sprayY + sin(i.toDouble() * 1.8).toFloat() * 2.5f
                drawCircle(
                    color = Color.White.copy(alpha = 0.75f),
                    radius = 3.dp.toPx(),
                    center = Offset(fx, fy)
                )
            }

            // 3. Karşıdaki 3D Fileli Kale Çizgisi
            val goalLeft = w * 0.15f
            val goalRight = w * 0.85f
            val goalTop = h * 0.12f
            val goalCrossbarY = h * 0.28f

            // Kale Direkleri (Beyaz Çelik)
            val postColor = Color(0xFFF0F4F2)
            drawLine(postColor, Offset(goalLeft, goalTop), Offset(goalLeft, goalCrossbarY + 60f), 7.dp.toPx())
            drawLine(postColor, Offset(goalRight, goalTop), Offset(goalRight, goalCrossbarY + 60f), 7.dp.toPx())
            drawLine(postColor, Offset(goalLeft, goalTop), Offset(goalRight, goalTop), 8.dp.toPx())

            // File Izgaraları (Net Meshes)
            for (step in 1..9) {
                val ny = goalTop + (goalCrossbarY - goalTop) * (step / 9f)
                drawLine(Color.White.copy(alpha = 0.25f), Offset(goalLeft, ny), Offset(goalRight, ny), 1.5.dp.toPx())
            }
            for (step in 1..14) {
                val nx = goalLeft + (goalRight - goalLeft) * (step / 14f)
                drawLine(Color.White.copy(alpha = 0.25f), Offset(nx, goalTop), Offset(nx, goalCrossbarY + 60f), 1.5.dp.toPx())
            }

            // 4. Yeşil Hedef Halkaları (Sol 90 ve Sağ 90 Köşesi - media_1795005882059_584c6456.jpg)
            val left90Center = Offset(goalLeft + 38.dp.toPx(), goalTop + 32.dp.toPx())
            val right90Center = Offset(goalRight - 38.dp.toPx(), goalTop + 32.dp.toPx())

            listOf(left90Center, right90Center).forEach { centerPt ->
                // Dış Halka Radar Nabzı
                drawCircle(
                    color = AccentBright.copy(alpha = ringPulseAlpha * 0.35f),
                    radius = 28.dp.toPx() * ringRadiusScale,
                    center = centerPt
                )
                // Parlak Yeşil Ana Halka
                drawCircle(
                    color = AccentBright,
                    radius = 18.dp.toPx(),
                    center = centerPt,
                    style = Stroke(width = 3.5.dp.toPx())
                )
                // Hedef Artı Artı (Crosshair)
                drawLine(
                    color = AccentBright,
                    start = Offset(centerPt.x - 12.dp.toPx(), centerPt.y),
                    end = Offset(centerPt.x + 12.dp.toPx(), centerPt.y),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = AccentBright,
                    start = Offset(centerPt.x, centerPt.y - 12.dp.toPx()),
                    end = Offset(centerPt.x, centerPt.y + 12.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 5. 5 Savunmacı Barajı (Omuz Omuza, Zıplama Animasyonlu)
            val wallBaselineY = sprayY - 14.dp.toPx()
            val wallDefenderSpacing = w * 0.115f
            val wallStartX = w * 0.27f

            val currentJumpPx = wallJumpOffsetAnim * 42.dp.toPx()

            for (idx in 0 until 5) {
                val defX = wallStartX + idx * wallDefenderSpacing
                val defY = wallBaselineY - currentJumpPx

                // Savunmacı Gölgesi (Zıpladıkça küçülür ve solar)
                val shadowAlpha = (0.50f - wallJumpOffsetAnim * 0.30f).coerceAtLeast(0.1f)
                val shadowWidth = (22.dp.toPx() * (1f - wallJumpOffsetAnim * 0.35f)).coerceAtLeast(8f)
                drawOval(
                    color = Color.Black.copy(alpha = shadowAlpha),
                    topLeft = Offset(defX - shadowWidth / 2f, wallBaselineY + 12.dp.toPx()),
                    size = Size(shadowWidth, 8.dp.toPx())
                )

                // Savunmacı Vücudu (Forma, Bacaklar, Kafa)
                // Bacaklar
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(defX - 5.dp.toPx(), defY - 10.dp.toPx()),
                    end = Offset(defX - 5.dp.toPx(), defY + 12.dp.toPx()),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(defX + 5.dp.toPx(), defY - 10.dp.toPx()),
                    end = Offset(defX + 5.dp.toPx(), defY + 12.dp.toPx()),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Gövde & Forma (Koyu Lacivert / Kırmızı Savunma Forması)
                drawRoundRect(
                    color = if (idx % 2 == 0) Color(0xFF1E3A8A) else Color(0xFFB91C1C),
                    topLeft = Offset(defX - 12.dp.toPx(), defY - 48.dp.toPx()),
                    size = Size(24.dp.toPx(), 40.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Göğüste çapraz kollar (Barajda elleriyle korunan savunmacı duruşu)
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(defX - 9.dp.toPx(), defY - 26.dp.toPx()),
                    end = Offset(defX + 9.dp.toPx(), defY - 26.dp.toPx()),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Kafa
                drawCircle(
                    color = Color(0xFFE2C9A5), // Ten rengi
                    radius = 9.dp.toPx(),
                    center = Offset(defX, defY - 58.dp.toPx())
                )
                // Saç
                drawArc(
                    color = Color(0xFF1C1917),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(defX - 9.dp.toPx(), defY - 67.dp.toPx()),
                    size = Size(18.dp.toPx(), 18.dp.toPx())
                )
            }

            // 6. 7 Numaralı Frikikçi (Topun Arkasında Hazır Duruş)
            val shooterX = w * 0.50f
            val shooterY = h * 0.86f
            val ballSpotX = w * 0.50f
            val ballSpotY = h * 0.77f

            // Frikikçi Sırtı (7 Numaralı Forma - Yıldız Oyuncu)
            drawCircle(
                color = Color(0xFFE2C9A5),
                radius = 11.dp.toPx(),
                center = Offset(shooterX, shooterY - 60.dp.toPx())
            )
            // Forma (Kendi Takım Forması - Canlı Zümrüt Yeşil & Altın Yaldız)
            drawRoundRect(
                color = AccentPrimary,
                topLeft = Offset(shooterX - 16.dp.toPx(), shooterY - 48.dp.toPx()),
                size = Size(32.dp.toPx(), 46.dp.toPx()),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )
            // Sırt Numarası "7" Çizimi
            drawLine(
                color = ChampionshipGold,
                start = Offset(shooterX - 5.dp.toPx(), shooterY - 40.dp.toPx()),
                end = Offset(shooterX + 5.dp.toPx(), shooterY - 40.dp.toPx()),
                strokeWidth = 3.dp.toPx()
            )
            drawLine(
                color = ChampionshipGold,
                start = Offset(shooterX + 5.dp.toPx(), shooterY - 40.dp.toPx()),
                end = Offset(shooterX - 3.dp.toPx(), shooterY - 24.dp.toPx()),
                strokeWidth = 3.dp.toPx()
            )

            // 7. Futbol Topu ve Kavisli Uçuş Rotası
            val target90X = right90Center.x
            val target90Y = right90Center.y

            if (!isShotInProgress) {
                // Hazırlık: Top çimde duruyor
                drawCircle(Color.Black.copy(alpha = 0.5f), 10.dp.toPx(), Offset(ballSpotX, ballSpotY + 5.dp.toPx()))
                drawCircle(Color.White, 9.dp.toPx(), Offset(ballSpotX, ballSpotY))
                drawCircle(Color(0xFF0F172A), 4.dp.toPx(), Offset(ballSpotX, ballSpotY))

                // Kullanıcıya rehber parmak kavis çizgisi (media_1795005882914_3ed51209.jpg beyaz kavisli çizgi)
                val curvePath = Path().apply {
                    moveTo(ballSpotX, ballSpotY)
                    quadraticBezierTo(
                        w * 0.76f, h * 0.44f, // Barajın sağından falso kontrol noktası
                        target90X, target90Y  // Sağ 90 köşesi
                    )
                }
                drawPath(
                    path = curvePath,
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(
                        width = 4.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f),
                        cap = StrokeCap.Round
                    )
                )
            } else {
                // Şut Anı: Top kavisli Beziér eğrisi üzerinde uçar
                val t = ballFlightProgress
                val u = 1f - t
                val ctrlX = w * 0.76f
                val ctrlY = h * 0.44f
                val currentBallX = u * u * ballSpotX + 2f * u * t * ctrlX + t * t * target90X
                val currentBallY = u * u * ballSpotY + 2f * u * t * ctrlY + t * t * target90Y

                // Top İzi (Motion Blur Kuyruğu)
                drawCircle(
                    color = AccentBright.copy(alpha = 0.45f),
                    radius = 11.dp.toPx(),
                    center = Offset(currentBallX, currentBallY)
                )
                // Futbol Topu
                drawCircle(
                    color = Color.White,
                    radius = 9.dp.toPx(),
                    center = Offset(currentBallX, currentBallY)
                )
                drawCircle(
                    color = Color(0xFF0F172A),
                    radius = 3.5.dp.toPx(),
                    center = Offset(currentBallX, currentBallY)
                )
            }
        }

        // Üst Başlık & Senaryo Bilgi Kartı
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = SurfaceElevated.copy(alpha = 0.92f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, StrokePremiumStrong)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SportsSoccer,
                        contentDescription = null,
                        tint = AccentBright,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "FRİKİK BARAJI (5 SAVUNMACI) • 24M SERBEST VURUŞ",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "BARAJIN ÜSTÜNDEN 90'A FALSOLU PLASE VUR!",
                fontFamily = BarlowCondensedFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = BrightGold
            )
        }

        // Alt Aksiyon Butonu / Şut Kontrol Paneli
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (resultBannerText != null) {
                Surface(
                    color = if (isGoalHit) Color(0xFF065F46) else Color(0xFF7F1D1D),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, if (isGoalHit) AccentBright else Negative),
                    modifier = Modifier.fillMaxWidth(0.92f)
                ) {
                    Text(
                        text = resultBannerText!!,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (!isShotInProgress) {
                Button(
                    onClick = { isShotInProgress = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color(0xFF04150D))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FALSOLU VURUŞU YAP (90'A AS)",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF04150D)
                    )
                }
            }
        }
    }
}

/**
 * 2. PENALTI DÜELLOSU VE KALECİ UZANMA ANİMASYONU EKRANI
 * - 11m penaltı noktası
 * - Kaleci sağa/sola uzanma hamlesi (keeper diving animation)
 * - Kalp atışı gerilim nabzı (138 BPM Heartbeat Sound & HUD)
 * - Direk çınlaması ve ağ sarsıntısı
 */
@Composable
fun PenaltyDuelCinematicOverlay(
    penaltyConfig: PenaltyDuelConfiguration,
    onPenaltyResolved: (isGoal: Boolean, isPostHit: Boolean, commentary: String) -> Unit = { _, _, _ -> },
    onPlaySfx: (GameSoundEffect) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isShotTaken by remember { mutableStateOf(false) }
    var selectedDiveSide by remember { mutableStateOf(PenaltyKeeperDive.BOTTOM_RIGHT) }
    var keeperDiveAnimProgress by remember { mutableFloatStateOf(0f) }
    var shotProgress by remember { mutableFloatStateOf(0f) }
    var penaltyResult by remember { mutableStateOf<String?>(null) }

    // Düello gerilimi: periyodik kalp atışı sesi
    LaunchedEffect(Unit) {
        onPlaySfx(GameSoundEffect.PENALTY_HEARTBEAT)
    }

    LaunchedEffect(isShotTaken) {
        if (isShotTaken) {
            onPlaySfx(GameSoundEffect.BALL_KICK)
            val keeperAnim = Animatable(0f)
            val ballAnim = Animatable(0f)

            // Kaleci köşeye atlar
            val keeperJob = launch {
                keeperAnim.animateTo(1f, animationSpec = tween(650, easing = FastOutSlowInEasing))
            }
            ballAnim.animateTo(1f, animationSpec = tween(600, easing = LinearEasing)) {
                shotProgress = value
                keeperDiveAnimProgress = keeperAnim.value
            }
            keeperJob.join()

            // Çözümleme
            val (isGoal, isCrossbar, comm) = SetPiecePhysicsSimulator.resolvePenaltyShootout(
                shotTargetY = 54.0f,
                shotTargetZ = 1.9f,
                keeperDive = selectedDiveSide,
                shotPowerPct = 88
            )
            penaltyResult = comm

            if (isCrossbar) {
                onPlaySfx(GameSoundEffect.CROSSBAR_METALLIC_HIT)
            } else if (isGoal) {
                onPlaySfx(GameSoundEffect.CROWD_ROAR_EXPLOSION)
            }
            onPenaltyResolved(isGoal, isCrossbar, comm)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Kale Çizgisi ve Ağ
            val goalLeft = w * 0.12f
            val goalRight = w * 0.88f
            val goalCrossbarY = h * 0.22f
            val goalBottomY = h * 0.50f

            // Çim Zemin
            drawRect(
                brush = Brush.verticalGradient(listOf(Color(0xFF0F3120), Color(0xFF1B4E33))),
                topLeft = Offset(0f, goalBottomY - 10f),
                size = Size(w, h - goalBottomY + 10f)
            )

            // Beyaz Kale Çerçevesi
            drawLine(Color.White, Offset(goalLeft, goalCrossbarY), Offset(goalRight, goalCrossbarY), 8.dp.toPx())
            drawLine(Color.White, Offset(goalLeft, goalCrossbarY), Offset(goalLeft, goalBottomY), 8.dp.toPx())
            drawLine(Color.White, Offset(goalRight, goalCrossbarY), Offset(goalRight, goalBottomY), 8.dp.toPx())

            // File Izgarası
            for (step in 1..8) {
                val ny = goalCrossbarY + (goalBottomY - goalCrossbarY) * (step / 8f)
                drawLine(Color.White.copy(alpha = 0.22f), Offset(goalLeft, ny), Offset(goalRight, ny), 1.5.dp.toPx())
            }

            // Kaleci (Çizgide duran ve köşeye uzanan kaleci)
            val keeperCenterBaseX = w * 0.50f
            val keeperCenterBaseY = goalBottomY - 24.dp.toPx()

            // Kaleci Uzanış Hamlesi (Diving translation)
            val diveDirSign = if (selectedDiveSide in listOf(PenaltyKeeperDive.TOP_RIGHT, PenaltyKeeperDive.BOTTOM_RIGHT)) 1f else -1f
            val diveDistanceX = keeperDiveAnimProgress * (w * 0.26f) * diveDirSign
            val diveDistanceY = if (selectedDiveSide in listOf(PenaltyKeeperDive.TOP_LEFT, PenaltyKeeperDive.TOP_RIGHT)) {
                -keeperDiveAnimProgress * 30.dp.toPx()
            } else {
                keeperDiveAnimProgress * 12.dp.toPx()
            }

            val currentKeeperX = keeperCenterBaseX + diveDistanceX
            val currentKeeperY = keeperCenterBaseY + diveDistanceY

            // Kaleci Eldivenleri & Kolları (Uzanmış vaziyette)
            drawLine(
                color = Warning, // Sarı kaleci forması
                start = Offset(currentKeeperX - 18.dp.toPx(), currentKeeperY - 14.dp.toPx()),
                end = Offset(currentKeeperX + 18.dp.toPx(), currentKeeperY - 14.dp.toPx()),
                strokeWidth = 8.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Kaleci Gövdesi
            drawRoundRect(
                color = Warning,
                topLeft = Offset(currentKeeperX - 12.dp.toPx(), currentKeeperY - 28.dp.toPx()),
                size = Size(24.dp.toPx(), 36.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )
            // Kaleci Kafası
            drawCircle(Color(0xFFE2C9A5), 10.dp.toPx(), Offset(currentKeeperX, currentKeeperY - 38.dp.toPx()))

            // 11 Metre Penaltı Noktası (Beyaz Spot)
            val penaltySpotX = w * 0.50f
            val penaltySpotY = h * 0.82f
            drawCircle(Color.White, 8.dp.toPx(), Offset(penaltySpotX, penaltySpotY))

            // Şut Hedefi ve Top Uçuşu
            val targetCornerX = w * 0.78f
            val targetCornerY = goalCrossbarY + 32.dp.toPx()

            if (!isShotTaken) {
                // Penaltı Noktasında Top
                drawCircle(Color.White, 9.dp.toPx(), Offset(penaltySpotX, penaltySpotY))
                drawCircle(Color.Black, 3.5.dp.toPx(), Offset(penaltySpotX, penaltySpotY))
            } else {
                val t = shotProgress
                val currentBallX = (1f - t) * penaltySpotX + t * targetCornerX
                val currentBallY = (1f - t) * penaltySpotY + t * targetCornerY
                drawCircle(AccentBright.copy(alpha = 0.5f), 12.dp.toPx(), Offset(currentBallX, currentBallY))
                drawCircle(Color.White, 9.dp.toPx(), Offset(currentBallX, currentBallY))
            }
        }

        // Kalp Atışı Nabız Göstergesi (Penalty Pressure HUD)
        Surface(
            color = SurfaceGlass,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Negative.copy(alpha = 0.8f)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "💓", fontSize = 16.sp)
                Text(
                    text = "BASKI NABZI: ${penaltyConfig.heartRateBpm} BPM (KALP ATIŞI)",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Negative
                )
            }
        }

        // Sonuç Banner'ı
        if (penaltyResult != null) {
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, AccentBright),
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.88f)
                    .padding(16.dp)
            ) {
                Text(
                    text = penaltyResult!!,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (!isShotTaken) {
            Button(
                onClick = { isShotTaken = true },
                colors = ButtonDefaults.buttonColors(containerColor = AccentBright),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.85f)
                    .padding(bottom = 24.dp)
                    .height(48.dp)
            ) {
                Text(
                    text = "PENALTI ŞUTUNU ÇEK (KÖŞEYE FÜZE)",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF04150D)
                )
            }
        }
    }
}

/**
 * 3. KORNER CEZA SAHASI KARAMBOL VE KAFA VURUŞU EKRANI
 * - Muz orta kavisli yörünge
 * - Ceza sahası karambolü (box scramble)
 * - Havada kafa vuruşu mücadelesi (aerial duel)
 */
@Composable
fun CornerScrambleCinematicOverlay(
    onDuelResolved: (isGoal: Boolean, comm: String) -> Unit = { _, _ -> },
    onPlaySfx: (GameSoundEffect) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isCornerKicked by remember { mutableStateOf(false) }
    var duelResult by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isCornerKicked) {
        if (isCornerKicked) {
            onPlaySfx(GameSoundEffect.BALL_KICK)
            kotlinx.coroutines.delay(650L)
            onPlaySfx(GameSoundEffect.CORNER_AERIAL_DUEL)
            val (isGoal, comm) = SetPiecePhysicsSimulator.resolveCornerScramble(emptyList(), userAerialPower = 88)
            duelResult = comm
            if (isGoal) {
                onPlaySfx(GameSoundEffect.CROWD_ROAR_EXPLOSION)
            }
            onDuelResolved(isGoal, comm)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Ceza Sahası Üstten / İzometrik Çizimi
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF144D30), Color(0xFF092416)),
                    center = Offset(w * 0.5f, h * 0.45f),
                    radius = w * 0.8f
                )
            )

            // Ceza Sahası Çizgisi
            drawRect(
                color = Color.White.copy(alpha = 0.6f),
                topLeft = Offset(w * 0.15f, h * 0.15f),
                size = Size(w * 0.70f, h * 0.45f),
                style = Stroke(3.dp.toPx())
            )
            // Altıpas Çizgisi
            drawRect(
                color = Color.White.copy(alpha = 0.6f),
                topLeft = Offset(w * 0.30f, h * 0.15f),
                size = Size(w * 0.40f, h * 0.18f),
                style = Stroke(2.5.dp.toPx())
            )

            // Köşe Bayrağı
            val cornerX = w * 0.92f
            val cornerY = h * 0.80f
            drawCircle(AccentBright, 8.dp.toPx(), Offset(cornerX, cornerY))
            drawLine(Color.White, Offset(cornerX, cornerY), Offset(cornerX, cornerY - 20.dp.toPx()), 3.dp.toPx())
            drawRect(Negative, Offset(cornerX, cornerY - 20.dp.toPx()), Size(14.dp.toPx(), 9.dp.toPx()))

            // Ceza Sahası İçi Karambol Oyuncuları (Hücumcular ve Savunmacılar Birbirini Tutuyor)
            val duelists = listOf(
                Offset(w * 0.42f, h * 0.28f) to AccentPrimary,
                Offset(w * 0.44f, h * 0.29f) to Negative,
                Offset(w * 0.50f, h * 0.26f) to AccentPrimary, // Kafa vuran forvet
                Offset(w * 0.52f, h * 0.27f) to Negative,      // Savunma stoperi
                Offset(w * 0.58f, h * 0.31f) to AccentPrimary,
                Offset(w * 0.50f, h * 0.18f) to Warning       // Kaleci
            )

            duelists.forEach { (pos, col) ->
                drawCircle(Color.Black.copy(alpha = 0.4f), 12.dp.toPx(), Offset(pos.x, pos.y + 4.dp.toPx()))
                drawCircle(col, 10.dp.toPx(), pos)
            }

            // Kavisli Muz Orta Rotası (Köşe Bayrağından Altıpas Önüne)
            val arcPath = Path().apply {
                moveTo(cornerX, cornerY)
                quadraticBezierTo(w * 0.85f, h * 0.35f, w * 0.50f, h * 0.26f)
            }
            drawPath(
                path = arcPath,
                color = Color.White.copy(alpha = 0.85f),
                style = Stroke(
                    width = 4.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f),
                    cap = StrokeCap.Round
                )
            )
        }

        // Başlık
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, StrokePremiumStrong)
            ) {
                Text(
                    text = "KORNER CEZA SAHASI KARAMBOLÜ & KAFA VURUŞU",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = AccentBright,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Sonuç ve Buton
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (duelResult != null) {
                Surface(
                    color = SurfacePrimary,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, AccentBright),
                    modifier = Modifier.fillMaxWidth(0.92f)
                ) {
                    Text(
                        text = duelResult!!,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (!isCornerKicked) {
                Button(
                    onClick = { isCornerKicked = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "KORNERİ KULLAN (ÖN DİREĞE KAVİSLİ ORTA)",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF04150D)
                    )
                }
            }
        }
    }
}

/**
 * 4. HAKEM KART DRAMA EKRANI
 * - Hakemin düdük çalıp koşması (REFEREE_WHISTLE_FOUL & REFEREE_WHISTLE_CARD)
 * - Havaya kalkan Sarı / Kırmızı Kart
 * - Tribün yuhalama ve uğultu atmosferi
 */
@Composable
fun RefereeCardCinematicOverlay(
    dramaEvent: RefereeDramaEvent,
    onDismiss: () -> Unit = {},
    onPlaySfx: (GameSoundEffect) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isRed = dramaEvent.type in listOf(RefereeDramaType.RED_CARD_DIRECT, RefereeDramaType.SECOND_YELLOW_RED)
    val cardColor = if (isRed) Color(0xFFDC2626) else Color(0xFFFACC15)
    val cardTitle = if (isRed) "KIRMIZI KART • OYUNDAN İHRAÇ" else "SARI KART • UYARI"

    LaunchedEffect(Unit) {
        onPlaySfx(GameSoundEffect.REFEREE_WHISTLE_CARD)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .padding(16.dp)
        ) {
            // Hakem Düdük İkonu ve Dalgası
            Text(text = "📢", fontSize = 38.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "HAKEM DÜDÜĞÜNÜ ÇALDI! • ${dramaEvent.refereeName.uppercase()}",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Dramatik Parlayan Sarı / Kırmızı Kart
            Surface(
                color = cardColor,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 18.dp,
                modifier = Modifier
                    .size(width = 110.dp, height = 150.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isRed) "🟥" else "🟨",
                        fontSize = 48.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, cardColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = cardTitle,
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = cardColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${dramaEvent.targetPlayerNumber} NUMARA • ${dramaEvent.targetPlayerName.uppercase()}",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sebep: ${dramaEvent.foulDescriptionTr}",
                        fontFamily = ManropeFontFamily,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Kapatmak için ekrana dokunun",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 11.sp,
                color = TextMuted
            )
        }
    }
}

/**
 * 5. SAKATLIK, SEDYE VE SAĞLIK EKİBİ MÜDAHALE EKRANI
 * - Çimde yatan oyuncu (acı çeken nabız halkası)
 * - Sağlık ekibi ve sedye sahaya girer
 * - Soğutucu sprey partikül animasyonu
 * - Sedye ile kenara alma veya ayağa kalkıp oyuna dönme
 */
@Composable
fun MedicalStretcherCinematicOverlay(
    injuryEvent: MedicalInjuryEvent,
    onTreatmentCompleted: (returnedToMatch: Boolean) -> Unit = {},
    onPlaySfx: (GameSoundEffect) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var treatmentStep by remember { mutableIntStateOf(1) } // 1: Müdahale, 2: Soğutucu Sprey, 3: Karar
    var isDone by remember { mutableStateOf(false) }

    LaunchedEffect(treatmentStep) {
        when (treatmentStep) {
            1 -> onPlaySfx(GameSoundEffect.INJURY_STRETCHER_HORN)
            2 -> onPlaySfx(GameSoundEffect.DEFENSE_WALL_JUMP)
            3 -> {
                if (injuryEvent.canReturnToMatch) {
                    onPlaySfx(GameSoundEffect.STADIUM_CHANT_AMBIENCE)
                } else {
                    onPlaySfx(GameSoundEffect.REFEREE_WHISTLE_FOUL)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .padding(16.dp)
        ) {
            // Sağlık Ekibi & Sedye İkonu
            Text(text = "🚑", fontSize = 44.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "SAĞLIK EKİBİ VE SEDYE SAHADA!",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Negative
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.5.dp, StrokePremiumStrong),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${injuryEvent.injuredJerseyNumber} NUMARA • ${injuryEvent.injuredPlayerName.uppercase()}",
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Sakatlanan Bölge: ${injuryEvent.bodyPartTr}",
                        fontFamily = OutfitFontFamily,
                        fontSize = 14.sp,
                        color = Warning
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tedavi Aşamaları Çubuğu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🚨", fontSize = 20.sp)
                            Text(
                                text = "Müdahale",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 10.sp,
                                color = if (treatmentStep >= 1) AccentBright else TextMuted
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "❄️", fontSize = 20.sp)
                            Text(
                                text = "Soğutucu Sprey",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 10.sp,
                                color = if (treatmentStep >= 2) AccentBright else TextMuted
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = if (injuryEvent.canReturnToMatch) "✅" else "🚑", fontSize = 20.sp)
                            Text(
                                text = if (injuryEvent.canReturnToMatch) "Oyuna Dönüş" else "Sedye Tahliyesi",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 10.sp,
                                color = if (treatmentStep >= 3) AccentBright else TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val statusMsg = when (treatmentStep) {
                        1 -> "Doktorlar ve sedye görevlileri sahaya koştu. İlk kontrol yapılıyor..."
                        2 -> "Bölgeye ağrı dindirici soğutucu sprey (mist) uygulandı ❄️"
                        else -> if (injuryEvent.canReturnToMatch) {
                            "Oyuncu ayağa kalktı! Taraftarların alkışları eşliğinde sahaya dönüyor ✅"
                        } else {
                            "Durumu ciddi! Sedye açılarak kenara taşındı, zorunlu oyuncu değişikliği 🚑"
                        }
                    }

                    Text(
                        text = statusMsg,
                        fontFamily = ManropeFontFamily,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (treatmentStep < 3) {
                Button(
                    onClick = { treatmentStep++ },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Text(
                        text = if (treatmentStep == 1) "SOĞUTUCU SPREY SIK" else "KARARI GÖRÜNTÜLE",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF04150D)
                    )
                }
            } else {
                Button(
                    onClick = { onTreatmentCompleted(injuryEvent.canReturnToMatch) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (injuryEvent.canReturnToMatch) AccentBright else Negative),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Text(
                        text = "MAÇA DEVAM ET",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF04150D)
                    )
                }
            }
        }
    }
}


/**
 * 6. TEK NOKTADAN TÜM SAHNELERİ YÖNETEN HOST BİLEŞENİ (SetPieceDramaHostOverlay)
 * @motor-gorsel ve @sef'in doğrudan InteractiveMatchScreen veya MainActivity'ye
 * tek satırla bağlayabilmesi için tasarlanmıştır.
 */
enum class SetPieceOverlayMode {
    NONE,
    FREE_KICK_WALL,
    PENALTY_DUEL,
    CORNER_SCRAMBLE,
    REFEREE_CARD,
    MEDICAL_STRETCHER
}

@Composable
fun SetPieceDramaHostOverlay(
    mode: SetPieceOverlayMode,
    freeKickConfig: FreeKickWallConfiguration = FreeKickWallConfiguration(
        wallCount = 5,
        defenders = (0..4).map { idx ->
            WallDefender(
                id = "DEF_$idx",
                index = idx,
                name = "Savunmacı ${idx + 1}",
                jerseyNumber = idx + 2,
                basePitchX = 90.0f,
                basePitchY = 46.5f + idx * 1.75f
            )
        },
        sprayLineDistanceM = 9.15f,
        sprayLineStartX = 90.0f,
        sprayLineStartY = 45.0f,
        sprayLineEndX = 90.0f,
        sprayLineEndY = 55.0f,
        shooterJerseyNumber = 7,
        ballPosition = PitchPoint(78f, 50f, 0f)
    ),
    penaltyConfig: PenaltyDuelConfiguration = PenaltyDuelConfiguration(),
    refereeDrama: RefereeDramaEvent = RefereeDramaEvent(
        type = RefereeDramaType.YELLOW_CARD,
        refereeName = "Halil Umut Meler",
        targetPlayerName = "Oyuncu",
        targetPlayerNumber = 7,
        foulDescriptionTr = "Taktik Faul / Formadan Çekme",
        minute = 54
    ),
    medicalInjury: MedicalInjuryEvent = MedicalInjuryEvent(
        injuredPlayerName = "Yıldız Oyuncu",
        injuredJerseyNumber = 7,
        injuredClubName = "Kulüp",
        bodyPartTr = "Sağ Ayak Bileği",
        requiresStretcher = true,
        canReturnToMatch = true
    ),
    onDismiss: () -> Unit = {},
    onPlaySfx: (GameSoundEffect) -> Unit = {}
) {
    when (mode) {
        SetPieceOverlayMode.NONE -> Unit
        SetPieceOverlayMode.FREE_KICK_WALL -> {
            FreeKickWallCinematicOverlay(
                wallConfig = freeKickConfig,
                onShotExecuted = { _, _, _ -> },
                onPlaySfx = onPlaySfx
            )
        }
        SetPieceOverlayMode.PENALTY_DUEL -> {
            PenaltyDuelCinematicOverlay(
                penaltyConfig = penaltyConfig,
                onPenaltyResolved = { _, _, _ -> },
                onPlaySfx = onPlaySfx
            )
        }
        SetPieceOverlayMode.CORNER_SCRAMBLE -> {
            CornerScrambleCinematicOverlay(
                onDuelResolved = { _, _ -> },
                onPlaySfx = onPlaySfx
            )
        }
        SetPieceOverlayMode.REFEREE_CARD -> {
            RefereeCardCinematicOverlay(
                dramaEvent = refereeDrama,
                onDismiss = onDismiss,
                onPlaySfx = onPlaySfx
            )
        }
        SetPieceOverlayMode.MEDICAL_STRETCHER -> {
            MedicalStretcherCinematicOverlay(
                injuryEvent = medicalInjury,
                onTreatmentCompleted = { _ -> onDismiss() },
                onPlaySfx = onPlaySfx
            )
        }
    }
}
