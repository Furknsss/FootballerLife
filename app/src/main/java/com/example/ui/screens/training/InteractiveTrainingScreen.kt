package com.example.ui.screens.training

import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.audio.GameSoundEffect
import com.example.audio.SoundAndHapticsManager
import com.example.engine.training.DrillAttemptResult
import com.example.engine.training.DrillDifficulty
import com.example.engine.training.DrillMedalTier
import com.example.engine.training.DrillSessionSummary
import com.example.engine.training.InteractiveTrainingEngine
import com.example.engine.training.PlayerTrait
import com.example.engine.training.PlayerTraitsCatalog
import com.example.engine.training.TargetRingState
import com.example.engine.training.TraitBranch
import com.example.engine.training.TrainingMiniGameType
import com.example.engine.training.WallDummyState
import com.example.model.CareerSaveState
import com.example.model.PlayerProfile
import com.example.ui.components.FLBadge
import com.example.ui.components.FLCard
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Negative
import com.example.ui.theme.StrokePremium
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

enum class TrainingScreenTab(val titleTr: String) {
    MINI_GAMES("🎯 Antrenman Sahası"),
    SKILL_TREE("🌳 Yetenek Ağacı & Vasıflar")
}

@Composable
fun InteractiveTrainingScreen(
    career: CareerSaveState,
    onBack: () -> Unit,
    onSaveTrainingResult: (DrillSessionSummary) -> Unit = {},
    onUnlockTrait: (String) -> Unit = {}
) {
    InteractiveTrainingScreenContent(
        player = career.player,
        onBack = onBack,
        onSaveTrainingResult = onSaveTrainingResult,
        onUnlockTrait = onUnlockTrait
    )
}

@Composable
fun InteractiveTrainingScreen(
    playerProfile: PlayerProfile,
    onBack: () -> Unit,
    onSaveTrainingResult: (DrillSessionSummary) -> Unit = {},
    onUnlockTrait: (String) -> Unit = {}
) {
    InteractiveTrainingScreenContent(
        player = playerProfile,
        onBack = onBack,
        onSaveTrainingResult = onSaveTrainingResult,
        onUnlockTrait = onUnlockTrait
    )
}

@Composable
private fun InteractiveTrainingScreenContent(
    player: PlayerProfile,
    onBack: () -> Unit,
    onSaveTrainingResult: (DrillSessionSummary) -> Unit,
    onUnlockTrait: (String) -> Unit
) {
    val context = LocalContext.current
    val soundManager = remember { SoundAndHapticsManager(context) }
    var selectedTab by remember { mutableStateOf(TrainingScreenTab.MINI_GAMES) }

    // Yetenek puanı (Kullanıcının kilidini açtığı vasıflar ve antrenman performansına göre)
    val unlockedTraitsList = remember(player.unlockedTraits) { player.unlockedTraits.toMutableList() }
    var totalSkillPointsEarned by remember {
        mutableIntStateOf(3 + (player.careerArchive.size * 2) + (player.overallRating / 15))
    }
    val availableSkillPoints = remember(unlockedTraitsList.size, totalSkillPointsEarned) {
        PlayerTraitsCatalog.calculateAvailableSkillPoints(
            player.copy(unlockedTraits = unlockedTraitsList),
            totalSkillPointsEarned
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        // Üst Başlık Çubuğu
        TrainingHeaderBar(
            player = player,
            availablePoints = availableSkillPoints,
            onBack = onBack
        )

        // Sekme Seçimi
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = BackgroundSecondary,
            contentColor = AccentPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = AccentPrimary
                )
            }
        ) {
            TrainingScreenTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = {
                        soundManager.playEffect(GameSoundEffect.MENU_TAP, sfxEnabled = true, hapticsEnabled = true)
                        selectedTab = tab
                    },
                    text = {
                        Text(
                            text = tab.titleTr,
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == tab) AccentBright else TextSecondary
                        )
                    }
                )
            }
        }

        // Sekme İçerikleri
        when (selectedTab) {
            TrainingScreenTab.MINI_GAMES -> {
                MiniGamesDrillTabContent(
                    player = player,
                    unlockedTraits = unlockedTraitsList,
                    soundManager = soundManager,
                    onDrillCompleted = { summary ->
                        // Başarılı antrenman sonrası TP ödülü ekle
                        totalSkillPointsEarned += summary.skillPointsEarned
                        onSaveTrainingResult(summary)
                    }
                )
            }
            TrainingScreenTab.SKILL_TREE -> {
                SkillTreeTabContent(
                    player = player.copy(unlockedTraits = unlockedTraitsList),
                    availableSkillPoints = availableSkillPoints,
                    soundManager = soundManager,
                    onUnlockTraitRequest = { traitId ->
                        if (!unlockedTraitsList.contains(traitId)) {
                            unlockedTraitsList.add(traitId)
                            soundManager.playEffect(GameSoundEffect.TRAINING_LEVEL_UP, sfxEnabled = true, hapticsEnabled = true)
                            onUnlockTrait(traitId)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TrainingHeaderBar(
    player: PlayerProfile,
    availablePoints: Int,
    onBack: () -> Unit
) {
    Surface(
        color = BackgroundSecondary,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, StrokePremium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "ANTRENMAN & GELİŞİM",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${player.fullName} (${player.primaryPosition.code}) • OVR ${player.overallRating}",
                        fontSize = 12.sp,
                        color = AccentPrimary
                    )
                }
            }

            // Yetenek Puanı (TP) Rozeti
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceElevated,
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$availablePoints TP",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }
            }
        }
    }
}

// =========================================================================================
// 1. İNTERAKTİF ANTRENMAN MİNİ-OYUNLARI (NSS & Soccer Superstar Tarzı Baraj Üstü Hedef Vurma)
// =========================================================================================

@Composable
private fun MiniGamesDrillTabContent(
    player: PlayerProfile,
    unlockedTraits: List<String>,
    soundManager: SoundAndHapticsManager,
    onDrillCompleted: (DrillSessionSummary) -> Unit
) {
    var selectedMiniGame by remember { mutableStateOf(TrainingMiniGameType.FREE_KICK_TARGET) }
    var difficulty by remember { mutableStateOf(DrillDifficulty.SEMI_PRO) }
    var currentAttemptIndex by remember { mutableIntStateOf(1) }
    val totalAttempts = 5
    val attemptResults = remember { mutableStateListOf<DrillAttemptResult>() }
    var currentScore by remember { mutableIntStateOf(0) }
    var comboMultiplier by remember { mutableIntStateOf(1) }
    var lastFeedbackMessage by remember { mutableStateOf("Topa dokun ve falso çizerek 90'daki yeşil hedef halkasını vur!") }
    var showSummaryDialog by remember { mutableStateOf(false) }
    var completedSummary by remember { mutableStateOf<DrillSessionSummary?>(null) }

    // Çizilen parmak yolu (Swipe)
    val gesturePoints = remember { mutableStateListOf<Pair<Float, Float>>() }
    var isShotInProgress by remember { mutableStateOf(false) }

    // Hedef halkaları ve baraj mankenleri
    var targetRings by remember(difficulty) {
        mutableStateOf(InteractiveTrainingEngine.createDefaultTargetRings(difficulty))
    }
    val wallDummies by remember(difficulty) {
        mutableStateOf(InteractiveTrainingEngine.createWallDummies(difficulty))
    }

    // Animasyon değerleri
    val ballProgress = remember { Animatable(0f) }
    var activeTrajectory by remember { mutableStateOf<List<Triple<Float, Float, Float>>>(emptyList()) }
    var lastHitRingId by remember { mutableStateOf<Int?>(null) }
    val ringShockwave = remember { Animatable(0f) }

    val coroutineScope = rememberCoroutineScope()

    // Hedef halkası yanıp sönme animasyonu
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Oyun Türü Seçici
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(TrainingMiniGameType.entries) { gameType ->
                val isSelected = selectedMiniGame == gameType
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) AccentPrimary.copy(alpha = 0.25f) else SurfaceElevated,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) AccentPrimary else StrokePremium
                    ),
                    modifier = Modifier.clickable {
                        selectedMiniGame = gameType
                        // Sıfırla
                        currentAttemptIndex = 1
                        currentScore = 0
                        comboMultiplier = 1
                        attemptResults.clear()
                        lastFeedbackMessage = "Yeni antrenman seansı başladı!"
                    }
                ) {
                    Text(
                        text = gameType.titleTr,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) AccentBright else TextSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // HUD Bilgi Paneli: Skor, Kalan Hak, Combo
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, StrokePremium),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Kalan Hak (Top simgeleri)
                Column {
                    Text(text = "DENEME $currentAttemptIndex / $totalAttempts", fontSize = 11.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 1..totalAttempts) {
                            val isCompleted = i < currentAttemptIndex
                            val isCurrent = i == currentAttemptIndex
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCompleted -> AccentPrimary
                                            isCurrent -> GoldPrimary
                                            else -> Color.DarkGray
                                        }
                                    )
                            )
                        }
                    }
                }

                // Skor ve Kombo
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "TOPLAM PUAN", fontSize = 10.sp, color = TextSecondary)
                    Text(
                        text = "$currentScore",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                }

                // Kombo Çarpanı
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (comboMultiplier > 1) GoldPrimary.copy(alpha = 0.2f) else SurfaceElevated,
                    border = BorderStroke(1.dp, if (comboMultiplier > 1) GoldPrimary else StrokePremium)
                ) {
                    Text(
                        text = "x$comboMultiplier COMBO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (comboMultiplier > 1) GoldPrimary else TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Canlı Antrenör / Spiker Yorumu
        Text(
            text = lastFeedbackMessage,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (lastFeedbackMessage.contains("90'A") || lastFeedbackMessage.contains("BULLSEYE")) GoldPrimary else AccentBright,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        // =================================================================================
        // İNTERAKTİF ÇİZİM SAHASI (Interactive Canvas: Goal, Net, Wall Dummies, Green Rings)
        // =================================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, StrokePremium, RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F2615), // Koyu gece stadyumu üst kısmı
                            Color(0xFF144D25), // Çim sahası
                            Color(0xFF1B6331)  // Yakın çim
                        )
                    )
                )
                .pointerInput(isShotInProgress) {
                    if (isShotInProgress) return@pointerInput
                    detectDragGestures(
                        onDragStart = { offset ->
                            gesturePoints.clear()
                            gesturePoints.add(offset.x / size.width to offset.y / size.height)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            gesturePoints.add(change.position.x / size.width to change.position.y / size.height)
                        },
                        onDragEnd = {
                            if (gesturePoints.size >= 3 && !isShotInProgress) {
                                isShotInProgress = true
                                soundManager.playEffect(GameSoundEffect.BALL_KICK, sfxEnabled = true, hapticsEnabled = true)

                                val shotResult = InteractiveTrainingEngine.simulateFreeKickShot(
                                    swipePath = gesturePoints.toList(),
                                    shotPower = 0.85f,
                                    playerAttributes = player.attributes,
                                    unlockedTraits = unlockedTraits,
                                    targetRings = targetRings,
                                    wallDummies = wallDummies,
                                    difficulty = difficulty
                                )

                                activeTrajectory = shotResult.trajectoryPoints

                                coroutineScope.launch {
                                    // Topun uçuş animasyonu
                                    ballProgress.snapTo(0f)
                                    ballProgress.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(
                                            durationMillis = 620,
                                            easing = FastOutSlowInEasing
                                        )
                                    )

                                    // Vuruş değerlendirmesi
                                    attemptResults.add(shotResult)
                                    currentScore += shotResult.pointsAwarded * comboMultiplier
                                    lastFeedbackMessage = shotResult.commentaryTr

                                    if (shotResult.isTargetHit) {
                                        lastHitRingId = 1
                                        comboMultiplier = (comboMultiplier + 1).coerceAtMost(4)
                                        soundManager.playEffect(GameSoundEffect.TRAINING_LEVEL_UP, sfxEnabled = true, hapticsEnabled = true)
                                        ringShockwave.snapTo(0f)
                                        ringShockwave.animateTo(1f, tween(400))
                                    } else {
                                        comboMultiplier = 1
                                    }

                                    delay(900)
                                    isShotInProgress = false
                                    gesturePoints.clear()
                                    lastHitRingId = null

                                    if (currentAttemptIndex >= totalAttempts) {
                                        val summary = InteractiveTrainingEngine.finalizeSession(
                                            gameType = selectedMiniGame,
                                            difficulty = difficulty,
                                            totalAttempts = totalAttempts,
                                            results = attemptResults.toList(),
                                            playerProfile = player
                                        )
                                        completedSummary = summary
                                        showSummaryDialog = true
                                        onDrillCompleted(summary)
                                    } else {
                                        currentAttemptIndex++
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cw = size.width
                val ch = size.height

                // 1. Çim Şeritleri ve Saha Çizgileri
                drawPitchBackgroundAndMarkings(cw, ch)

                // 2. Kale Direkleri ve File (3D derinlik)
                val goalLeft = cw * 0.12f
                val goalRight = cw * 0.88f
                val goalTop = ch * 0.14f
                val goalBottom = ch * 0.46f

                drawRealisticGoalWithNet(goalLeft, goalRight, goalTop, goalBottom)

                // 3. Baraj Mankenleri (Wall Dummies)
                drawWallDummies(wallDummies, cw, ch)

                // 4. Yeşil Hedef Halkaları (Soccer Superstar & NSS tarzı yeşil neon halkalar)
                drawTargetRings(targetRings, goalLeft, goalRight, goalTop, goalBottom, ringPulse, lastHitRingId, ringShockwave.value)

                // 5. Oyuncunun Parmağıyla Çizdiği Kavisli Şut Rotası (Trajectory Line)
                if (gesturePoints.isNotEmpty()) {
                    drawCurvedSwipeTrajectory(gesturePoints, cw, ch)
                }

                // 6. Futbol Topu ve Uçuş Parabolü
                val startBall = Offset(cw * 0.50f, ch * 0.88f)
                if (!isShotInProgress) {
                    drawSoccerBall(startBall, radius = 18f)
                } else if (activeTrajectory.isNotEmpty()) {
                    val progress = ballProgress.value
                    val pointIndex = ((activeTrajectory.size - 1) * progress).toInt().coerceIn(0, activeTrajectory.size - 1)
                    val pt = activeTrajectory[pointIndex]
                    // Koordinat dönüştürme
                    val currentX = cw * pt.first
                    val currentY = ch * (0.88f - pt.second * 0.58f)
                    val ballElevation = pt.third * 16f
                    val currentBallPos = Offset(currentX, currentY - ballElevation)

                    // Top Gölgesi
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.35f),
                        radius = (16f * (1f - pt.third * 0.12f)).coerceAtLeast(6f),
                        center = Offset(currentX, currentY)
                    )

                    // Top
                    val ballSize = 18f + pt.third * 4f
                    drawSoccerBall(currentBallPos, radius = ballSize)
                }
            }

            // Parmak İzi Yönlendirme İpucu
            if (!isShotInProgress && gesturePoints.isEmpty()) {
                Text(
                    text = "👆 Parmağınla topun üstünden kaleye doğru kavis çiz!",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }

    // Antrenman Tamamlandı Özet Diyaloğu
    if (showSummaryDialog && completedSummary != null) {
        val summary = completedSummary!!
        Dialog(onDismissRequest = { showSummaryDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundSecondary),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, GoldPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ANTRENMAN TAMAMLANDI!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = summary.medalTier.titleTr,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(android.graphics.Color.parseColor(summary.medalTier.badgeColorHex))
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // İstatistikler Tablosu
                    Surface(
                        color = SurfacePrimary,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Toplam Skor:", color = TextSecondary, fontSize = 13.sp)
                                Text(text = "${summary.totalScore} Puan", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Hedef Vuruşu:", color = TextSecondary, fontSize = 13.sp)
                                Text(text = "${summary.targetHits} / ${summary.totalAttempts}", color = AccentBright, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Bullseye (Tam 12):", color = TextSecondary, fontSize = 13.sp)
                                Text(text = "${summary.bullseyes} Kez", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Kazanılan TP (Yetenek Puanı):", color = TextSecondary, fontSize = 13.sp)
                                Text(text = "+${summary.skillPointsEarned} TP", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Kazanılan Deneyim (XP):", color = TextSecondary, fontSize = 13.sp)
                                Text(text = "+${summary.xpEarned} XP", color = AccentPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = summary.coachNotesTr,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = {
                            showSummaryDialog = false
                            // Tekrar başlat
                            currentAttemptIndex = 1
                            currentScore = 0
                            comboMultiplier = 1
                            attemptResults.clear()
                            lastFeedbackMessage = "Yeni antrenman seansı hazır!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Gelişimi Kaydet & Tamamla", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =========================================================================================
// 2. YETENEK AĞACI VE PASİF VASIFLAR (Traits) EKRANI
// =========================================================================================

@Composable
private fun SkillTreeTabContent(
    player: PlayerProfile,
    availableSkillPoints: Int,
    soundManager: SoundAndHapticsManager,
    onUnlockTraitRequest: (String) -> Unit
) {
    var selectedBranch by remember { mutableStateOf<TraitBranch?>(null) }
    var selectedTraitForDetail by remember { mutableStateOf<PlayerTrait?>(null) }

    val filteredTraits = remember(selectedBranch) {
        if (selectedBranch == null) {
            PlayerTraitsCatalog.allTraits
        } else {
            PlayerTraitsCatalog.getTraitsByBranch(selectedBranch!!)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Üst Kategori Filtre Butonları
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    BranchFilterChip(
                        title = "Tümü (${PlayerTraitsCatalog.allTraits.size})",
                        isSelected = selectedBranch == null,
                        colorHex = "#20C773",
                        onClick = { selectedBranch = null }
                    )
                }
                items(TraitBranch.entries) { branch ->
                    BranchFilterChip(
                        title = branch.titleTr,
                        isSelected = selectedBranch == branch,
                        colorHex = branch.accentColorHex,
                        onClick = { selectedBranch = branch }
                    )
                }
            }
        }

        // Bilgilendirme Kartı
        item {
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, StrokePremium)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AccentPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Vasıflar maç içinde ve simülasyonda doğrudan pasif etki sağlar. Antrenman mini-oyunlarında altın madalya alarak Yetenek Puanı (TP) kazanabilirsin.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Vasıf Kartları Listesi
        items(filteredTraits) { trait ->
            val isUnlocked = player.unlockedTraits.contains(trait.id)
            val eligibility = PlayerTraitsCatalog.checkUnlockEligibility(player, trait, availableSkillPoints)

            TraitNodeCard(
                trait = trait,
                isUnlocked = isUnlocked,
                eligibility = eligibility,
                onUnlockClick = {
                    onUnlockTraitRequest(trait.id)
                },
                onCardClick = {
                    selectedTraitForDetail = trait
                }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Vasıf Detay / Onay Diyaloğu
    if (selectedTraitForDetail != null) {
        val trait = selectedTraitForDetail!!
        val isUnlocked = player.unlockedTraits.contains(trait.id)
        val eligibility = PlayerTraitsCatalog.checkUnlockEligibility(player, trait, availableSkillPoints)

        Dialog(onDismissRequest = { selectedTraitForDetail = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundSecondary),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(2.dp, Color(android.graphics.Color.parseColor(trait.branch.accentColorHex))),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = trait.nameTr,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(android.graphics.Color.parseColor(trait.branch.accentColorHex))
                    )
                    Text(
                        text = trait.subtitleTr,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = trait.descriptionTr,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = SurfacePrimary,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "⚡ MAÇ İÇİ PASİF ETKİ:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = trait.matchEffectSummary,
                                fontSize = 12.sp,
                                color = AccentBright
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isUnlocked) {
                        Surface(
                            color = AccentPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "BU VASIF AÇIK & KULLANIMDA ✓",
                                color = AccentBright,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    } else if (eligibility.canUnlock) {
                        Button(
                            onClick = {
                                onUnlockTraitRequest(trait.id)
                                selectedTraitForDetail = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Kilidi Aç (${trait.pointCost} TP)",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = eligibility.missingRequirements.joinToString("\n"),
                            fontSize = 11.sp,
                            color = Negative,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { selectedTraitForDetail = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Kapat", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun BranchFilterChip(
    title: String,
    isSelected: Boolean,
    colorHex: String,
    onClick: () -> Unit
) {
    val chipColor = Color(android.graphics.Color.parseColor(colorHex))
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) chipColor.copy(alpha = 0.25f) else SurfaceElevated,
        border = BorderStroke(1.dp, if (isSelected) chipColor else StrokePremium),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) chipColor else TextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun TraitNodeCard(
    trait: PlayerTrait,
    isUnlocked: Boolean,
    eligibility: com.example.engine.training.TraitUnlockCheckResult,
    onUnlockClick: () -> Unit,
    onCardClick: () -> Unit
) {
    val branchColor = Color(android.graphics.Color.parseColor(trait.branch.accentColorHex))

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) SurfaceElevated else SurfacePrimary
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (isUnlocked) branchColor else if (eligibility.canUnlock) GoldPrimary.copy(alpha = 0.6f) else StrokePremium
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Sol Rozet / İkon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(branchColor.copy(alpha = if (isUnlocked) 0.35f else 0.15f))
                        .border(1.dp, branchColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUnlocked) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = branchColor,
                            modifier = Modifier.size(22.dp)
                        )
                    } else if (!eligibility.canUnlock) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = trait.nameTr,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) TextPrimary else TextSecondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "KADEME ${trait.tier}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = branchColor
                        )
                    }
                    Text(
                        text = trait.subtitleTr,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "⚡ ${trait.matchEffectSummary}",
                        fontSize = 10.sp,
                        color = if (isUnlocked) AccentBright else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Sağ Aksiyon Butonu / Rozeti
            if (isUnlocked) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AccentPrimary.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, AccentPrimary)
                ) {
                    Text(
                        text = "AÇIK ✓",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBright,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            } else if (eligibility.canUnlock) {
                Button(
                    onClick = onUnlockClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "AÇ (${trait.pointCost} TP)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceElevated,
                    border = BorderStroke(1.dp, StrokePremium)
                ) {
                    Text(
                        text = "${trait.pointCost} TP",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================================
// CANVAS ÇİZİM YARDIMCILARI (Pitch, Goal, Dummies, Target Rings, Ball)
// =========================================================================================

private fun DrawScope.drawPitchBackgroundAndMarkings(cw: Float, ch: Float) {
    // Çim şeritleri
    val stripeCount = 9
    val stripeHeight = ch / stripeCount
    for (i in 0 until stripeCount) {
        val color = if (i % 2 == 0) Color(0xFF165228) else Color(0xFF1B6331)
        drawRect(
            color = color,
            topLeft = Offset(0f, i * stripeHeight),
            size = Size(cw, stripeHeight)
        )
    }

    // Ceza Sahası Çizgisi
    val boxTop = ch * 0.40f
    drawRect(
        color = Color.White.copy(alpha = 0.5f),
        topLeft = Offset(cw * 0.08f, boxTop),
        size = Size(cw * 0.84f, ch * 0.55f),
        style = Stroke(width = 3f)
    )

    // Penaltı Noktası
    drawCircle(
        color = Color.White.copy(alpha = 0.75f),
        radius = 5f,
        center = Offset(cw * 0.50f, ch * 0.65f)
    )

    // Penaltı Yayı
    drawArc(
        color = Color.White.copy(alpha = 0.45f),
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(cw * 0.35f, ch * 0.62f),
        size = Size(cw * 0.30f, ch * 0.12f),
        style = Stroke(width = 2.5f)
    )
}

private fun DrawScope.drawRealisticGoalWithNet(
    goalLeft: Float,
    goalRight: Float,
    goalTop: Float,
    goalBottom: Float
) {
    val goalWidth = goalRight - goalLeft
    val goalHeight = goalBottom - goalTop

    // Kale Ağları (Criss-Cross Net Mesh)
    val netCols = 16
    val netRows = 9
    val colSpacing = goalWidth / netCols
    val rowSpacing = goalHeight / netRows

    for (c in 0..netCols) {
        val x = goalLeft + c * colSpacing
        drawLine(
            color = Color.White.copy(alpha = 0.22f),
            start = Offset(x, goalTop),
            end = Offset(x, goalBottom),
            strokeWidth = 1.2f
        )
    }

    for (r in 0..netRows) {
        val y = goalTop + r * rowSpacing
        drawLine(
            color = Color.White.copy(alpha = 0.22f),
            start = Offset(goalLeft, y),
            end = Offset(goalRight, y),
            strokeWidth = 1.2f
        )
    }

    // 3D Arka Destek Direkleri
    drawLine(
        color = Color.LightGray.copy(alpha = 0.45f),
        start = Offset(goalLeft, goalTop),
        end = Offset(goalLeft - 18f, goalTop - 12f),
        strokeWidth = 4f
    )
    drawLine(
        color = Color.LightGray.copy(alpha = 0.45f),
        start = Offset(goalRight, goalTop),
        end = Offset(goalRight + 18f, goalTop - 12f),
        strokeWidth = 4f
    )

    // Ön Beyaz Kale Direkleri ve Üst Direk
    val postWidth = 9f
    // Sol Direk
    drawRect(
        color = Color.White,
        topLeft = Offset(goalLeft - postWidth / 2, goalTop),
        size = Size(postWidth, goalHeight)
    )
    // Sağ Direk
    drawRect(
        color = Color.White,
        topLeft = Offset(goalRight - postWidth / 2, goalTop),
        size = Size(postWidth, goalHeight)
    )
    // Üst Direk
    drawRect(
        color = Color.White,
        topLeft = Offset(goalLeft - postWidth / 2, goalTop - postWidth / 2),
        size = Size(goalWidth + postWidth, postWidth)
    )
}

private fun DrawScope.drawWallDummies(
    wallDummies: List<WallDummyState>,
    cw: Float,
    ch: Float
) {
    val wallY = ch * 0.60f
    wallDummies.forEach { dummy ->
        val dummyCenterX = cw * (0.50f + dummy.normX)
        val dummyWidth = cw * dummy.widthNorm
        val dummyHeight = 65f

        // Taban Ayak Plakası
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(dummyCenterX - dummyWidth * 0.45f, wallY + dummyHeight * 0.45f),
            size = Size(dummyWidth * 0.90f, 6f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // Bacak Direkleri
        drawLine(
            color = Color.DarkGray,
            start = Offset(dummyCenterX - dummyWidth * 0.20f, wallY + dummyHeight * 0.45f),
            end = Offset(dummyCenterX - dummyWidth * 0.20f, wallY),
            strokeWidth = 3f
        )
        drawLine(
            color = Color.DarkGray,
            start = Offset(dummyCenterX + dummyWidth * 0.20f, wallY + dummyHeight * 0.45f),
            end = Offset(dummyCenterX + dummyWidth * 0.20f, wallY),
            strokeWidth = 3f
        )

        // Gövde (Antrenman Manken Yeleği / Şablonu)
        val torsoGradient = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFE11D48), // Kırmızı antrenman manken yeleği
                Color(0xFF9F1239)
            )
        )
        drawRoundRect(
            brush = torsoGradient,
            topLeft = Offset(dummyCenterX - dummyWidth * 0.40f, wallY - dummyHeight * 0.40f),
            size = Size(dummyWidth * 0.80f, dummyHeight * 0.42f),
            cornerRadius = CornerRadius(6f, 6f)
        )

        // Kafa Silüeti
        drawCircle(
            color = Color(0xFFE2E8F0),
            radius = dummyWidth * 0.24f,
            center = Offset(dummyCenterX, wallY - dummyHeight * 0.50f)
        )
    }
}

private fun DrawScope.drawTargetRings(
    rings: List<TargetRingState>,
    goalLeft: Float,
    goalRight: Float,
    goalTop: Float,
    goalBottom: Float,
    pulseScale: Float,
    lastHitRingId: Int?,
    shockwaveProgress: Float
) {
    val goalWidth = goalRight - goalLeft
    val goalHeight = goalBottom - goalTop

    rings.forEach { ring ->
        val ringCenterX = goalLeft + ring.normX * goalWidth
        val ringCenterY = goalBottom - ring.normY * goalHeight
        val baseRadius = goalWidth * ring.radiusNorm
        val animatedRadius = baseRadius * (if (ring.isBonusRotating) pulseScale else 1.0f)

        val neonGreen = Color(0xFF22C55E)
        val neonGold = Color(0xFFFBBF24)
        val ringColor = if (ring.isBonusRotating) neonGold else neonGreen

        // 1. Dış Parlama / Glow Halesi
        drawCircle(
            color = ringColor.copy(alpha = 0.22f),
            radius = animatedRadius * 1.35f,
            center = Offset(ringCenterX, ringCenterY)
        )

        // 2. Ana Yeşil Neon Halka (Soccer Superstar tarzı yeşil halka)
        drawCircle(
            color = ringColor,
            radius = animatedRadius,
            center = Offset(ringCenterX, ringCenterY),
            style = Stroke(width = 5.5f)
        )

        // 3. İç Konsantrik İkinci İnce Halka
        drawCircle(
            color = ringColor.copy(alpha = 0.70f),
            radius = animatedRadius * 0.55f,
            center = Offset(ringCenterX, ringCenterY),
            style = Stroke(width = 2.5f)
        )

        // 4. Hedef Puanı Yazısı (Halkanın İçinde "150", "100")
        val paint = Paint().apply {
            color = Color.White.toArgb()
            textSize = (baseRadius * 0.75f).coerceIn(18f, 32f)
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            setShadowLayer(4f, 0f, 0f, ringColor.toArgb())
        }
        drawContext.canvas.nativeCanvas.drawText(
            "${ring.basePoints}",
            ringCenterX,
            ringCenterY + (paint.textSize / 3f),
            paint
        )

        // 5. Hedef Vurulduğunda Genişleyen Şok Dalgası Animasyonu
        if (lastHitRingId != null && shockwaveProgress > 0f) {
            drawCircle(
                color = GoldPrimary.copy(alpha = 1f - shockwaveProgress),
                radius = baseRadius + (shockwaveProgress * 55f),
                center = Offset(ringCenterX, ringCenterY),
                style = Stroke(width = 4f * (1f - shockwaveProgress))
            )
        }
    }
}

private fun DrawScope.drawCurvedSwipeTrajectory(
    points: List<Pair<Float, Float>>,
    cw: Float,
    ch: Float
) {
    if (points.size < 2) return

    val path = Path()
    val first = points.first()
    path.moveTo(cw * first.first, ch * first.second)

    for (i in 1 until points.size) {
        val pt = points[i]
        path.lineTo(cw * pt.first, ch * pt.second)
    }

    // Neon Kılavuz Çizgisi
    drawPath(
        path = path,
        color = Color(0xFFEAB308),
        style = Stroke(
            width = 6f,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f), 0f)
        )
    )

    // Bitiş hedef ucu oku / dairesi
    val last = points.last()
    drawCircle(
        color = Color(0xFF22C55E),
        radius = 8f,
        center = Offset(cw * last.first, ch * last.second)
    )
}

private fun DrawScope.drawSoccerBall(center: Offset, radius: Float) {
    // Top Gölgesi
    drawCircle(
        color = Color.Black.copy(alpha = 0.25f),
        radius = radius * 1.05f,
        center = Offset(center.x + 3f, center.y + 4f)
    )

    // Beyaz Top Gövdesi
    drawCircle(
        color = Color.White,
        radius = radius,
        center = center
    )

    // Klasik Siyah Beşgen Paneller
    val pentagonRadius = radius * 0.36f
    drawCircle(
        color = Color(0xFF1E293B),
        radius = pentagonRadius,
        center = center
    )

    // Kenar Dikişleri
    drawCircle(
        color = Color.LightGray,
        radius = radius,
        center = center,
        style = Stroke(width = 1.5f)
    )
}
