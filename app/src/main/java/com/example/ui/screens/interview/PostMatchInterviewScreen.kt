package com.example.ui.screens.interview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.GameSoundEffect
import com.example.engine.interview.AnsweredInterviewItem
import com.example.engine.interview.InterviewAnswerOption
import com.example.engine.interview.InterviewEngine
import com.example.engine.interview.InterviewQuestion
import com.example.engine.interview.InterviewSessionResult
import com.example.engine.interview.InterviewTone
import com.example.engine.interview.TvReporterPersona
import com.example.model.CareerSaveState
import com.example.model.CompletedMatchReport
import com.example.ui.components.ClubCrestBadge
import com.example.ui.components.FLFormatters
import com.example.ui.components.FLPrimaryButton
import com.example.ui.components.FLRatingBadge
import com.example.ui.components.FLSectionHeader
import com.example.ui.components.FootballerAvatarCanvas
import com.example.ui.components.parseHexColor
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.BrightGold
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.InfoSky
import com.example.ui.theme.ManropeFontFamily
import com.example.ui.theme.Negative
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StrokePremium
import com.example.ui.theme.StrokePremiumStrong
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Warning
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun PostMatchInterviewScreen(
    career: CareerSaveState,
    onCompleteInterview: ((InterviewSessionResult) -> Unit)? = null,
    onSubmitPressStatement: (String, String) -> Unit = { _, _ -> },
    onPlaySfx: (GameSoundEffect) -> Unit = {},
    geminiApiKey: String? = null,
    geminiModel: String = "gemini-4-hydrogen",
    aiEnabled: Boolean = true,
    onContinueToCareerHub: () -> Unit
) {
    val report = career.lastMatchReport
    if (report == null) {
        LaunchedEffect(Unit) {
            onContinueToCareerHub()
        }
        return
    }

    var questions by remember(report.fixtureId) {
        mutableStateOf(InterviewEngine.buildOfflineQuestions(career, report))
    }
    var usedDynamicAi by remember(report.fixtureId) { mutableStateOf(false) }

    // Attempt optional dynamic Gemini question generation if hooked by @diyalog or GeminiClient has key
    LaunchedEffect(report.fixtureId, aiEnabled, geminiApiKey, geminiModel) {
        if (!career.pressCompletedForLastMatch && aiEnabled) {
            val (generated, isAi) = InterviewEngine.generateQuestionsWithOptionalAi(
                career = career,
                report = report,
                userApiKey = geminiApiKey,
                preferredModelId = geminiModel
            )
            if (isAi && generated.isNotEmpty()) {
                questions = generated
                usedDynamicAi = true
            }
        }
    }

    val answeredItems = remember(report.fixtureId) { mutableStateListOf<AnsweredInterviewItem>() }
    var currentQuestionIdx by remember(report.fixtureId) { mutableIntStateOf(0) }
    var pendingAnsweredItem by remember(report.fixtureId) { mutableStateOf<AnsweredInterviewItem?>(null) }
    var completedResult by remember(report.fixtureId) {
        mutableStateOf<InterviewSessionResult?>(
            if (career.pressCompletedForLastMatch) {
                InterviewEngine.lastCompletedSession?.takeIf { it.fixtureId == report.fixtureId }
            } else null
        )
    }

    var showLockerRoomSheet by remember { mutableStateOf(false) }
    var showCustomInputBar by remember { mutableStateOf(false) }
    var customInputText by remember { mutableStateOf("") }
    var customInputTone by remember { mutableStateOf(InterviewTone.CONFIDENT) }
    var flashBurstTrigger by remember { mutableIntStateOf(0) }

    val activeQuestion = questions.getOrNull(currentQuestionIdx)
    val isInterviewDone = career.pressCompletedForLastMatch || completedResult != null || activeQuestion == null

    // Typewriter subtitle state for the active reporter question
    var typedCharCount by remember(activeQuestion?.id) { mutableIntStateOf(0) }
    val fullQuestionText = activeQuestion?.questionText.orEmpty()
    val isTypingComplete = typedCharCount >= fullQuestionText.length

    LaunchedEffect(activeQuestion?.id, fullQuestionText) {
        if (activeQuestion != null && !isInterviewDone) {
            typedCharCount = 0
            flashBurstTrigger++
            while (typedCharCount < fullQuestionText.length) {
                delay(14L)
                typedCharCount = (typedCharCount + 2).coerceAtMost(fullQuestionText.length)
            }
        }
    }

    // Camera flash burst animation when a question starts or an answer is chosen
    var cameraFlashAlpha by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(flashBurstTrigger) {
        if (flashBurstTrigger > 0) {
            cameraFlashAlpha = 0.72f
            delay(65L)
            cameraFlashAlpha = 0.18f
            delay(55L)
            cameraFlashAlpha = 0.48f
            delay(80L)
            cameraFlashAlpha = 0f
        }
    }
    val animatedFlashAlpha by animateFloatAsState(
        targetValue = cameraFlashAlpha,
        animationSpec = tween(120),
        label = "camera_flash_alpha"
    )

    fun commitAnswerSelection(option: InterviewAnswerOption, customText: String? = null) {
        val q = activeQuestion ?: return
        onPlaySfx(GameSoundEffect.MENU_TAP)
        flashBurstTrigger++
        val answered = AnsweredInterviewItem(
            question = q,
            selectedOption = option,
            customSpokenOverride = customText
        )
        pendingAnsweredItem = answered
        showCustomInputBar = false
        customInputText = ""
    }

    fun proceedToNextQuestionOrFinish() {
        val justAnswered = pendingAnsweredItem ?: return
        answeredItems.add(justAnswered)
        pendingAnsweredItem = null

        if (currentQuestionIdx + 1 < questions.size) {
            onPlaySfx(GameSoundEffect.MENU_TAP)
            currentQuestionIdx += 1
        } else {
            val sessionSummary = InterviewEngine.summarizeSession(
                career = career,
                report = report,
                answeredItems = answeredItems.toList(),
                usedDynamicAi = usedDynamicAi
            )
            completedResult = sessionSummary
            if (onCompleteInterview != null) {
                onCompleteInterview(sessionSummary)
            } else {
                onSubmitPressStatement(sessionSummary.bestQuoteText, sessionSummary.dominantTone.code)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .testTag("post_match_locker_screen")
    ) {
        if (!isInterviewDone && activeQuestion != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. TOP BROADCAST SCOREBUG & PROGRESS BAR
                FlashInterviewTopBar(
                    report = report,
                    questionIndex = currentQuestionIdx + 1,
                    totalQuestions = questions.size,
                    usedDynamicAi = usedDynamicAi,
                    showLockerRoomSheet = showLockerRoomSheet,
                    onToggleLockerRoom = {
                        onPlaySfx(GameSoundEffect.MENU_TAP)
                        showLockerRoomSheet = !showLockerRoomSheet
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (showLockerRoomSheet) {
                    // Quick toggleable Locker Room & Coach Evaluation view during interview
                    LockerRoomEvaluationCard(
                        report = report,
                        modifier = Modifier.weight(1f),
                        onClose = { showLockerRoomSheet = false }
                    )
                } else {
                    // 2. VISUAL FLASH-INTERVIEW SCENE (Sponsor Backdrop, Spotlights, Player, Reporters & Mics, Flashes)
                    FlashInterviewStageScene(
                        career = career,
                        report = report,
                        activeQuestion = activeQuestion,
                        pendingAnsweredItem = pendingAnsweredItem,
                        cameraFlashOverlayAlpha = animatedFlashAlpha,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. SUBTITLE-STYLE REPORTER QUESTION & LIVE REACTION BOX
                ReporterSubtitlePanel(
                    activeQuestion = activeQuestion,
                    displayedQuestionText = fullQuestionText.take(typedCharCount),
                    isTypingComplete = isTypingComplete,
                    pendingAnsweredItem = pendingAnsweredItem,
                    playerFullName = career.player.fullName,
                    onInstantRevealText = { typedCharCount = fullQuestionText.length },
                    onNextQuestionClick = { proceedToNextQuestionOrFinish() },
                    isLastQuestion = currentQuestionIdx + 1 >= questions.size
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 4. SIDE-BY-SIDE 3 TONE ANSWER BOXES ALONG THE BOTTOM (Or Next Step Action)
                if (pendingAnsweredItem == null) {
                    SideBySideAnswerBoxesRow(
                        options = activeQuestion.options,
                        onSelectOption = { option ->
                            typedCharCount = fullQuestionText.length
                            commitAnswerSelection(option)
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Optional Custom Free-Text Microphone Input Toggle
                    CustomMicStatementSection(
                        expanded = showCustomInputBar,
                        customText = customInputText,
                        selectedTone = customInputTone,
                        onToggleExpand = { showCustomInputBar = !showCustomInputBar },
                        onTextChange = { customInputText = it },
                        onToneChange = { customInputTone = it },
                        onSubmitCustom = {
                            if (customInputText.isNotBlank()) {
                                typedCharCount = fullQuestionText.length
                                val customOpt = InterviewEngine.buildCustomAnswerOption(
                                    question = activeQuestion,
                                    customText = customInputText,
                                    selectedTone = customInputTone,
                                    playerFullName = career.player.fullName
                                )
                                commitAnswerSelection(customOpt, customInputText.trim())
                            }
                        }
                    )
                }
            }
        } else {
            // 5. POST-INTERVIEW BROADCAST HEADLINES, SOCIAL REACTION PREVIEW & LOCKER ROOM SUMMARY
            PostInterviewSummaryView(
                career = career,
                report = report,
                sessionResult = completedResult ?: InterviewEngine.lastCompletedSession,
                onContinueToCareerHub = onContinueToCareerHub
            )
        }
    }
}

@Composable
private fun FlashInterviewTopBar(
    report: CompletedMatchReport,
    questionIndex: Int,
    totalQuestions: Int,
    usedDynamicAi: Boolean,
    showLockerRoomSheet: Boolean,
    onToggleLockerRoom: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "live_dot")
    val liveDotAlpha by infinite.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_dot_alpha"
    )

    Surface(
        color = SurfacePrimary.copy(alpha = 0.94f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, StrokePremiumStrong),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: LIVE Broadcast Badge + Question Counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Negative.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(1.dp, Negative)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Negative.copy(alpha = liveDotAlpha))
                        )
                        Text(
                            text = "CANLI • MIXED ZONE",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.5.sp,
                            color = TextPrimary
                        )
                    }
                }

                Surface(
                    color = BackgroundPrimary.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(1.dp, AccentPrimary.copy(alpha = 0.7f))
                ) {
                    Text(
                        text = "SORU $questionIndex / $totalQuestions",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = AccentBright,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (usedDynamicAi) {
                    Surface(
                        color = InfoSky.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(7.dp),
                        border = BorderStroke(1.dp, InfoSky)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = InfoSky,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "AI",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = InfoSky
                            )
                        }
                    }
                }
            }

            // Center: Compact Match Scorebug
            Text(
                text = "${report.homeClubName} ${report.homeScore} - ${report.awayScore} ${report.awayClubName}",
                fontFamily = BarlowCondensedFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = BrightGold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )

            // Right: Locker Room Report Toggle Pill
            Surface(
                color = if (showLockerRoomSheet) AccentPrimary else BackgroundPrimary.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (showLockerRoomSheet) AccentBright else StrokePremiumStrong),
                modifier = Modifier.clickable { onToggleLockerRoom() }
            ) {
                Text(
                    text = if (showLockerRoomSheet) "SAHNEYE DÖN" else "MAÇ RAPORU",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    color = if (showLockerRoomSheet) BackgroundPrimary else GoldPrimary,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                )
            }
        }
    }
}

/**
 * Renders the Mixed Zone / Flash-Interview Backdrop (Sponsor Board with Club & Sponsor Logos),
 * Studio Spotlights, Camera Flashes, 2.5D Player Avatar, and Reporters Holding Branded TV Microphones.
 */
@Composable
private fun FlashInterviewStageScene(
    career: CareerSaveState,
    report: CompletedMatchReport,
    activeQuestion: InterviewQuestion,
    pendingAnsweredItem: AnsweredInterviewItem?,
    cameraFlashOverlayAlpha: Float,
    modifier: Modifier = Modifier
) {
    val userClub = career.currentClub
    val isHome = report.homeClubId == userClub.id
    val opponentClubId = if (isHome) report.awayClubId else report.homeClubId
    val opponentClub = career.clubs.find { it.id == opponentClubId }
    val oppShortName = opponentClub?.shortName ?: (if (isHome) report.awayClubName.take(3) else report.homeClubName.take(3)).uppercase()
    val oppPrimaryHex = opponentClub?.primaryColorHex ?: "#0284C7"

    val clubPrimaryColor = parseHexColor(userClub.primaryColorHex, AccentPrimary)
    val clubSecondaryColor = parseHexColor(userClub.secondaryColorHex, TextPrimary)
    val oppPrimaryColor = parseHexColor(oppPrimaryHex, InfoSky)

    val infinite = rememberInfiniteTransition(label = "interview_stage_anim")
    val animPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "stage_wave_phase"
    )
    val ambientFlashPulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_flash_pulse"
    )

    val allReporters = InterviewEngine.reportersCatalog
    val activeReporterId = activeQuestion.reporter.id

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = BackgroundSecondary,
        border = BorderStroke(1.2.dp, StrokePremiumStrong)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Layer 1: Stadium atmospheric image softly blended behind the mixed-zone sponsor wall
            Image(
                painter = painterResource(id = R.drawable.img_stadium_hero),
                contentDescription = "Stadyum Mixed Zone Arka Planı",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.22f)
            )

            // Layer 2: Sponsor Backdrop Wall + Studio Spotlights + Ambient Paparazzi Flashes
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawSponsorBackdropBoard(
                    clubShortName = userClub.shortName.uppercase(),
                    clubName = userClub.name,
                    leagueName = userClub.leagueName,
                    clubPrimary = clubPrimaryColor,
                    clubSecondary = clubSecondaryColor,
                    oppShortName = oppShortName,
                    oppPrimary = oppPrimaryColor,
                    animPhase = animPhase
                )

                drawStudioSpotlightsAndFlashes(
                    animPhase = animPhase,
                    ambientFlashPulse = ambientFlashPulse,
                    clubPrimary = clubPrimaryColor
                )
            }

            // Layer 3: Top-Left & Top-Right Broadcast Sponsor / League Header Ribbons on the Wall
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BackgroundPrimary.copy(alpha = 0.82f))
                        .border(1.dp, StrokePremium, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    ClubCrestBadge(
                        shortName = userClub.shortName,
                        primaryHex = userClub.primaryColorHex,
                        secondaryHex = userClub.secondaryColorHex,
                        size = 22.dp
                    )
                    Column {
                        Text(
                            text = "${userClub.name.uppercase()} • RESMİ BASIN DUVARI",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.5.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${userClub.stadiumName} • Flash Interview Zone",
                            fontFamily = ManropeFontFamily,
                            fontSize = 8.5.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Active TV Channel Broadcast Logo Bug
                val channelPrimary = parseHexColor(activeQuestion.reporter.primaryColorHex, AccentPrimary)
                Surface(
                    color = channelPrimary.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = activeQuestion.reporter.channelName,
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 0.6.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Layer 4: Player 2.5D Portrait standing in front of the Sponsor Wall
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.84f)
                    .fillMaxWidth(0.56f)
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                FootballerAvatarCanvas(
                    appearance = career.player.appearance,
                    clubPrimaryHex = userClub.primaryColorHex,
                    clubSecondaryHex = userClub.secondaryColorHex,
                    kitNumber = career.player.kitNumber,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Layer 5: Foreground Reporters & Branded TV Microphones angled toward the Player
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                drawReportersAndBrandedMicrophones(
                    reporters = allReporters,
                    activeReporterId = activeReporterId,
                    isPlayerSpeaking = pendingAnsweredItem != null,
                    animPhase = animPhase
                )
            }

            // Layer 6: Broadcast Lower-Third Nameplate & Live Delta Feedback Pills
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = pendingAnsweredItem != null,
                    enter = fadeIn(tween(180)),
                    exit = fadeOut(tween(150))
                ) {
                    pendingAnsweredItem?.let { answered ->
                        LiveAnswerImpactStrip(option = answered.selectedOption)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Lower-third broadcast bar
                Surface(
                    color = BackgroundPrimary.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GoldBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(18.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(AccentPrimary)
                        )
                        Text(
                            text = "${career.player.fullName.uppercase()} (#${career.player.kitNumber})",
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            letterSpacing = 0.5.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "•",
                            color = TextMuted
                        )
                        Text(
                            text = "REYTİNG ${FLFormatters.formatRating(report.userRating)}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = AccentBright
                        )
                        if (report.userGoals > 0 || report.userAssists > 0) {
                            Text(
                                text = "•  ${report.userGoals} GOL  ${report.userAssists} ASİST",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = BrightGold
                            )
                        }
                    }
                }
            }

            // Layer 7: Camera Flash Whiteout Burst Overlay
            if (cameraFlashOverlayAlpha > 0.01f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = cameraFlashOverlayAlpha))
                )
            }
        }
    }
}

/**
 * Draws the Mixed Zone Sponsor Backdrop Wall with alternating Club Crests, League Badges, and Sponsor Logos.
 */
private fun DrawScope.drawSponsorBackdropBoard(
    clubShortName: String,
    clubName: String,
    leagueName: String,
    clubPrimary: Color,
    clubSecondary: Color,
    oppShortName: String,
    oppPrimary: Color,
    animPhase: Float
) {
    val w = size.width
    val h = size.height

    // Backdrop board base gradient (deep studio emerald-obsidian panel)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A1914).copy(alpha = 0.92f),
                Color(0xFF07120E).copy(alpha = 0.95f),
                Color(0xFF040A08).copy(alpha = 0.98f)
            )
        )
    )

    // Subtle diagonal club-color ambient glow across the sponsor wall
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                clubPrimary.copy(alpha = 0.16f),
                Color.Transparent,
                oppPrimary.copy(alpha = 0.10f)
            ),
            start = Offset(0f, 0f),
            end = Offset(w, h)
        )
    )

    // Grid of Sponsor & Club Logo Tiles on the Mixed Zone Wall
    val cols = 5
    val rows = 4
    val padX = w * 0.04f
    val padTop = h * 0.15f
    val padBottom = h * 0.16f
    val gridW = w - padX * 2f
    val gridH = (h - padTop - padBottom).coerceAtLeast(60f)
    val cellW = gridW / cols
    val cellH = gridH / rows

    val sponsorLabels = listOf(
        clubShortName,
        "beIN SPORTS",
        "TURKCELL",
        leagueName.take(10).uppercase(),
        "NIKE PRO",
        "THY AIRLINES",
        oppShortName,
        "EA SPORTS FC",
        "PUMA",
        "SOCAR"
    )

    val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
    }

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val idx = (r * cols + c) % sponsorLabels.size
            val label = sponsorLabels[idx]
            val tileW = cellW * 0.82f
            val tileH = cellH * 0.64f
            val left = padX + c * cellW + (cellW - tileW) * 0.5f
            val top = padTop + r * cellH + (cellH - tileH) * 0.5f

            val isClubTile = label == clubShortName
            val isOppTile = label == oppShortName

            val tileBg = when {
                isClubTile -> clubPrimary.copy(alpha = 0.28f)
                isOppTile -> oppPrimary.copy(alpha = 0.22f)
                (r + c) % 2 == 0 -> Color.White.copy(alpha = 0.06f)
                else -> Color(0xFF153025).copy(alpha = 0.42f)
            }
            val tileBorder = when {
                isClubTile -> clubSecondary.copy(alpha = 0.45f)
                isOppTile -> oppPrimary.copy(alpha = 0.40f)
                else -> Color.White.copy(alpha = 0.15f)
            }

            drawRoundRect(
                color = tileBg,
                topLeft = Offset(left, top),
                size = Size(tileW, tileH),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = tileBorder,
                topLeft = Offset(left, top),
                size = Size(tileW, tileH),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 1.2f)
            )

            // Mini geometric emblem on the left of each sponsor tile
            val emblemRadius = (tileH * 0.24f).coerceIn(4f, 10f)
            val emblemCx = left + tileW * 0.20f
            val emblemCy = top + tileH * 0.5f
            val emblemColor = when {
                isClubTile -> clubPrimary
                isOppTile -> oppPrimary
                idx % 3 == 0 -> Color(0xFF1ED777)
                idx % 3 == 1 -> Color(0xFFE2D7C5)
                else -> Color(0xFF38BDF8)
            }
            drawCircle(
                color = emblemColor.copy(alpha = 0.85f),
                radius = emblemRadius,
                center = Offset(emblemCx, emblemCy)
            )

            // Draw sponsor/club brand text on nativeCanvas
            textPaint.color = android.graphics.Color.argb(215, 240, 247, 244)
            textPaint.textSize = (tileH * 0.34f).coerceIn(13f, 24f)
            val textX = left + tileW * 0.58f
            val textY = top + tileH * 0.5f - (textPaint.descent() + textPaint.ascent()) * 0.5f
            drawContext.canvas.nativeCanvas.drawText(
                label.take(11),
                textX,
                textY,
                textPaint
            )
        }
    }
}

/**
 * Draws overhead volumetric studio spotlights and periodic camera flashes on the stage.
 */
private fun DrawScope.drawStudioSpotlightsAndFlashes(
    animPhase: Float,
    ambientFlashPulse: Float,
    clubPrimary: Color
) {
    val w = size.width
    val h = size.height
    val sway = sin(animPhase) * (w * 0.03f)

    // Left overhead studio beam
    val leftBeam = Path().apply {
        moveTo(w * 0.18f, 0f)
        lineTo(w * 0.30f, 0f)
        lineTo(w * 0.68f + sway, h)
        lineTo(w * 0.32f + sway, h)
        close()
    }
    drawPath(
        path = leftBeam,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.13f),
                clubPrimary.copy(alpha = 0.05f),
                Color.Transparent
            )
        )
    )

    // Right overhead studio beam
    val rightBeam = Path().apply {
        moveTo(w * 0.70f, 0f)
        lineTo(w * 0.82f, 0f)
        lineTo(w * 0.68f - sway, h)
        lineTo(w * 0.32f - sway, h)
        close()
    }
    drawPath(
        path = rightBeam,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.11f),
                Color(0xFF38BDF8).copy(alpha = 0.05f),
                Color.Transparent
            )
        )
    )

    // Ambient paparazzi camera lens flashes in the background/sides
    val flashWindow1 = ((ambientFlashPulse * 10f).toInt() == 2)
    val flashWindow2 = ((ambientFlashPulse * 10f).toInt() == 7)
    if (flashWindow1) {
        val center = Offset(w * 0.16f, h * 0.62f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.75f),
                    Color(0xFF38BDF8).copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = center,
                radius = w * 0.18f
            ),
            radius = w * 0.18f,
            center = center
        )
    }
    if (flashWindow2) {
        val center = Offset(w * 0.84f, h * 0.58f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.70f),
                    Color(0xFFF4EFE6).copy(alpha = 0.22f),
                    Color.Transparent
                ),
                center = center,
                radius = w * 0.16f
            ),
            radius = w * 0.16f,
            center = center
        )
    }
}

/**
 * Draws the cluster of 5 branded TV channel microphones held by reporters toward the player,
 * plus reporter shoulder/camera silhouettes on the sides.
 * The active reporter's microphone is elevated, highlighted with a sound-wave ring, and animated.
 */
private fun DrawScope.drawReportersAndBrandedMicrophones(
    reporters: List<TvReporterPersona>,
    activeReporterId: String,
    isPlayerSpeaking: Boolean,
    animPhase: Float
) {
    val w = size.width
    val h = size.height

    // Left & Right foreground reporter / cameraman silhouettes (mixed-zone crowd framing)
    val silhouetteColor = Color(0xFF050B09).copy(alpha = 0.88f)
    // Left reporter shoulder & camera lens
    drawOval(
        color = silhouetteColor,
        topLeft = Offset(-w * 0.06f, h * 0.68f),
        size = Size(w * 0.24f, h * 0.42f)
    )
    drawCircle(
        color = silhouetteColor,
        radius = w * 0.055f,
        center = Offset(w * 0.06f, h * 0.63f)
    )
    // Right reporter shoulder & TV camera rig
    drawOval(
        color = silhouetteColor,
        topLeft = Offset(w * 0.82f, h * 0.66f),
        size = Size(w * 0.24f, h * 0.44f)
    )
    drawCircle(
        color = silhouetteColor,
        radius = w * 0.055f,
        center = Offset(w * 0.93f, h * 0.61f)
    )

    // 5 Branded Microphones angled toward the center player
    val micAnchorXFractions = listOf(0.22f, 0.35f, 0.50f, 0.65f, 0.78f)
    val micAnglesDeg = listOf(22f, 11f, -2f, -12f, -23f)

    val flagTextPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
    }

    reporters.take(5).forEachIndexed { index, reporter ->
        val isActive = reporter.id == activeReporterId
        val baseX = w * micAnchorXFractions[index]
        val bobOffset = sin(animPhase + index * 0.9f) * (if (isActive) 5.5f else 2.5f)
        val activeLift = if (isActive) -h * 0.075f else 0f
        val baseY = h * 0.77f + activeLift + bobOffset
        val angleDeg = micAnglesDeg[index]

        val primaryCol = parseHexColor(reporter.primaryColorHex, AccentPrimary)
        val secondaryCol = parseHexColor(reporter.secondaryColorHex, Color.White)
        val foamCol = parseHexColor(reporter.micFoamColorHex, primaryCol)

        withTransform({
            rotate(degrees = angleDeg, pivot = Offset(baseX, baseY + h * 0.14f))
        }) {
            val micWidth = (w * if (isActive) 0.082f else 0.068f).coerceIn(28f, 68f)
            val flagHeight = micWidth * 0.74f
            val foamRadius = micWidth * 0.44f

            // If this reporter is active, draw pulsing broadcast sound-wave rings around the mic head
            if (isActive) {
                val pulseRadius = foamRadius * (1.55f + 0.25f * sin(animPhase * 2f))
                val ringColor = if (isPlayerSpeaking) AccentBright else secondaryCol
                drawCircle(
                    color = ringColor.copy(alpha = 0.28f),
                    radius = pulseRadius,
                    center = Offset(baseX, baseY - flagHeight * 0.55f)
                )
                drawCircle(
                    color = ringColor.copy(alpha = 0.65f),
                    radius = pulseRadius * 0.82f,
                    center = Offset(baseX, baseY - flagHeight * 0.55f),
                    style = Stroke(width = 2f)
                )
            }

            // 1. Reporter Hand & Microphone Handle Shaft extending down off-screen
            val handleWidth = micWidth * 0.28f
            drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(baseX - handleWidth * 0.5f, baseY + flagHeight * 0.35f),
                size = Size(handleWidth, h * 0.28f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Reporter hand grip
            drawRoundRect(
                color = Color(0xFFC68B69),
                topLeft = Offset(baseX - handleWidth * 0.85f, baseY + flagHeight * 0.95f),
                size = Size(handleWidth * 1.7f, flagHeight * 0.65f),
                cornerRadius = CornerRadius(10f, 10f)
            )

            // 2. Acoustic Windscreen Foam (Mikrofon Süngeri) on top
            val foamCenterY = baseY - flagHeight * 0.52f
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(
                        foamCol.copy(alpha = 0.98f),
                        primaryCol,
                        Color.Black.copy(alpha = 0.85f)
                    ),
                    center = Offset(baseX - foamRadius * 0.2f, foamCenterY - foamRadius * 0.2f),
                    radius = foamRadius * 1.3f
                ),
                topLeft = Offset(baseX - foamRadius, foamCenterY - foamRadius * 1.15f),
                size = Size(foamRadius * 2f, foamRadius * 2.2f)
            )
            // Metallic collar ring between foam and cube flag
            drawRect(
                color = Color(0xFFCBD5E1),
                topLeft = Offset(baseX - micWidth * 0.32f, baseY - flagHeight * 0.12f),
                size = Size(micWidth * 0.64f, flagHeight * 0.10f)
            )

            // 3. Branded TV Channel Mic Flag Cube (Mikrofon Küpü)
            val flagLeft = baseX - micWidth * 0.5f
            val flagTop = baseY - flagHeight * 0.05f
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(primaryCol, primaryCol.copy(alpha = 0.88f), Color.Black.copy(alpha = 0.7f))
                ),
                topLeft = Offset(flagLeft, flagTop),
                size = Size(micWidth, flagHeight),
                cornerRadius = CornerRadius(7f, 7f)
            )
            // Top accent stripe on the mic cube
            drawRoundRect(
                color = secondaryCol,
                topLeft = Offset(flagLeft + 2f, flagTop + 2f),
                size = Size(micWidth - 4f, flagHeight * 0.18f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Crisp border on active mic cube
            drawRoundRect(
                color = if (isActive) Color.White else Color.White.copy(alpha = 0.38f),
                topLeft = Offset(flagLeft, flagTop),
                size = Size(micWidth, flagHeight),
                cornerRadius = CornerRadius(7f, 7f),
                style = Stroke(width = if (isActive) 2.4f else 1.2f)
            )

            // Channel Label on the Mic Cube
            flagTextPaint.color = android.graphics.Color.WHITE
            flagTextPaint.textSize = (micWidth * 0.27f).coerceIn(11f, 20f)
            val textY = flagTop + flagHeight * 0.62f - (flagTextPaint.descent() + flagTextPaint.ascent()) * 0.5f
            drawContext.canvas.nativeCanvas.drawText(
                reporter.micFlagLabel,
                baseX,
                textY,
                flagTextPaint
            )
        }
    }
}

@Composable
private fun LiveAnswerImpactStrip(option: InterviewAnswerOption) {
    Surface(
        color = SurfacePrimary.copy(alpha = 0.95f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, AccentBright)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ImpactDeltaChip("Moral", option.moraleDelta)
            ImpactDeltaChip("Taraftar", option.fanSupportDelta)
            ImpactDeltaChip("Hoca Güveni", option.managerTrustDelta)
            ImpactDeltaChip("Takipçi", option.followersDelta, isFollowers = true)
        }
    }
}

@Composable
private fun ImpactDeltaChip(
    label: String,
    delta: Int,
    isFollowers: Boolean = false
) {
    val color = when {
        delta > 0 -> AccentBright
        delta < 0 -> Negative
        else -> TextSecondary
    }
    val prefix = if (delta >= 0) "+" else ""
    Text(
        text = "$label $prefix$delta",
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = if (isFollowers) 11.sp else 10.5.sp,
        color = color
    )
}

/**
 * Subtitle-style box displaying the reporter's question (typed out in real time)
 * or the player's spoken response + reporter follow-up reaction.
 */
@Composable
private fun ReporterSubtitlePanel(
    activeQuestion: InterviewQuestion,
    displayedQuestionText: String,
    isTypingComplete: Boolean,
    pendingAnsweredItem: AnsweredInterviewItem?,
    playerFullName: String,
    onInstantRevealText: () -> Unit,
    onNextQuestionClick: () -> Unit,
    isLastQuestion: Boolean
) {
    val reporter = activeQuestion.reporter
    val channelColor = parseHexColor(reporter.primaryColorHex, AccentPrimary)

    Surface(
        color = SurfacePrimary.copy(alpha = 0.96f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.2.dp,
            color = if (pendingAnsweredItem != null) AccentBright else StrokePremiumStrong
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isTypingComplete && pendingAnsweredItem == null) {
                onInstantRevealText()
            }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Top Reporter & Topic Metadata Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // TV Channel Mic Pill
                    Surface(
                        color = channelColor,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.55f))
                    ) {
                        Text(
                            text = reporter.micFlagLabel,
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "${reporter.reporterName} • ${reporter.channelName}",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = activeQuestion.contextSubtitle,
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 10.5.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Topic Badge
                Surface(
                    color = BackgroundPrimary.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(7.dp),
                    border = BorderStroke(1.dp, StrokePremium)
                ) {
                    Text(
                        text = activeQuestion.topic.badgeTr,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = GoldPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (pendingAnsweredItem == null) {
                // Subtitle Question Text (Typewriter animation)
                Text(
                    text = "\"$displayedQuestionText${if (!isTypingComplete) "▋" else "\""}",
                    fontFamily = ManropeFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    color = BrightGold,
                    modifier = Modifier.heightIn(min = 42.dp)
                )
            } else {
                // Player's Spoken Answer Subtitle + Reporter Reaction + Next Button
                val opt = pendingAnsweredItem.selectedOption
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = "🎙️ $playerFullName (${opt.tone.shortBadgeTr}): \"${pendingAnsweredItem.effectiveQuote}\"",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = AccentBright
                    )
                    Text(
                        text = "📺 ${reporter.reporterName}: \"${opt.reporterReactionText}\"",
                        fontFamily = ManropeFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = TextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📰 Manşet: ${opt.headlinePreview}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.5.sp,
                            color = GoldPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        Button(
                            onClick = onNextQuestionClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPrimary,
                                contentColor = BackgroundPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("btn_next_interview_question")
                        ) {
                            Text(
                                text = if (isLastQuestion) "RÖPORTAJI TAMAMLA" else "SIRADAKİ SORU",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Renders the 3 side-by-side answer boxes along the bottom:
 * Humble (Mütevazı), Confident (Öz Güvenli), Provocative (Provokatif).
 */
@Composable
private fun SideBySideAnswerBoxesRow(
    options: List<InterviewAnswerOption>,
    onSelectOption: (InterviewAnswerOption) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.take(3).forEachIndexed { idx, option ->
            val (accentCol, badgeBg, iconVec) = when (option.tone) {
                InterviewTone.HUMBLE -> Triple(
                    AccentPrimary,
                    AccentPrimary.copy(alpha = 0.16f),
                    Icons.Default.Groups
                )
                InterviewTone.CONFIDENT -> Triple(
                    InfoSky,
                    InfoSky.copy(alpha = 0.16f),
                    Icons.Default.Bolt
                )
                InterviewTone.PROVOCATIVE -> Triple(
                    Warning,
                    Negative.copy(alpha = 0.18f),
                    Icons.Default.Whatshot
                )
            }

            Surface(
                color = SurfacePrimary.copy(alpha = 0.96f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.2.dp, accentCol.copy(alpha = 0.75f)),
                modifier = Modifier
                    .weight(1f)
                    .height(146.dp)
                    .clickable { onSelectOption(option) }
                    .testTag("interview_answer_box_$idx")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    badgeBg,
                                    SurfacePrimary,
                                    BackgroundSecondary
                                )
                            )
                        )
                        .padding(9.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Tone Header Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = iconVec,
                                contentDescription = null,
                                tint = accentCol,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = option.tone.shortBadgeTr,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = accentCol
                            )
                        }
                    }

                    // Short Intent Title + Spoken Quote Preview
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = option.shortTitle,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            lineHeight = 14.sp,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "\"${option.spokenText}\"",
                            fontFamily = ManropeFontFamily,
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            color = TextSecondary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Bottom Impact Preview Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val trustStr = if (option.managerTrustDelta >= 0) "Hoca +${option.managerTrustDelta}" else "Hoca ${option.managerTrustDelta}"
                        val fanStr = if (option.fanSupportDelta >= 0) "Trft +${option.fanSupportDelta}" else "Trft ${option.fanSupportDelta}"
                        Text(
                            text = "$trustStr • $fanStr",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = accentCol
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomMicStatementSection(
    expanded: Boolean,
    customText: String,
    selectedTone: InterviewTone,
    onToggleExpand: () -> Unit,
    onTextChange: (String) -> Unit,
    onToneChange: (InterviewTone) -> Unit,
    onSubmitCustom: () -> Unit
) {
    Surface(
        color = SurfacePrimary.copy(alpha = 0.90f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, StrokePremium),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "MİKROFONA KENDİ CÜMLENİ SÖYLE (SERBEST YANIT)",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        color = GoldPrimary
                    )
                }
                Text(
                    text = if (expanded) "KAPAT ▲" else "AÇ ▼",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = AccentBright
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InterviewTone.entries.forEach { tone ->
                            val selected = tone == selectedTone
                            Surface(
                                color = if (selected) AccentPrimary else BackgroundPrimary,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (selected) AccentBright else StrokePremium),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onToneChange(tone) }
                            ) {
                                Text(
                                    text = tone.shortBadgeTr,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (selected) BackgroundPrimary else TextPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customText,
                            onValueChange = onTextChange,
                            placeholder = {
                                Text(
                                    text = "Canlı yayında kendi cümleni söyle...",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentBright,
                                unfocusedBorderColor = StrokePremium,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = onSubmitCustom,
                            enabled = customText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPrimary,
                                contentColor = BackgroundPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Gönder",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LockerRoomEvaluationCard(
    report: CompletedMatchReport,
    modifier: Modifier = Modifier,
    onClose: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfacePrimary,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, GoldBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SOYUNMA ODASI & TEKNİK HEYET RAPORU",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GoldPrimary
                )
                FLRatingBadge(
                    rating = report.userRating,
                    label = "REYTİNG",
                    isGold = report.userRating >= 8.0
                )
            }

            Text(
                text = report.managerFeedback,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            Text(
                text = report.teammateReaction,
                style = MaterialTheme.typography.bodySmall,
                color = AccentBright
            )

            Surface(
                color = BackgroundPrimary.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, StrokePremium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Kazanılan Maç & Gol Primi: +${FLFormatters.formatEuroFull(report.wageAndBonusEarnedEur)}",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BrightGold
                    )
                    Text(
                        text = "Harcanan Enerji: -%${report.energySpent}  •  Süre: ${report.userPlayedMinutes}' dk  •  Pas: ${report.userPassesCompleted}/${report.userPassesAttempted}",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            FLPrimaryButton(
                text = "FLASH RÖPORTAJ SAHNESİNE DÖN",
                onClick = onClose,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Shown after all 3-5 interview questions are answered: displays the generated TV/Newspaper
 * Headline, cumulative relationship/morale/fan/follower deltas, Q&A summary, locker room report,
 * and the button to return to the Career Hub.
 */
@Composable
private fun PostInterviewSummaryView(
    career: CareerSaveState,
    report: CompletedMatchReport,
    sessionResult: InterviewSessionResult?,
    onContinueToCareerHub: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Broadcast Score & Locker Room Banner
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfacePrimary,
                border = BorderStroke(1.5.dp, GoldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_locker_room),
                        contentDescription = "Soyunma Odası",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(165.dp)
                            .alpha(0.40f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(165.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, SurfacePrimary)
                                )
                            )
                    )
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "MAÇ SONU FLASH RÖPORTAJ & SOYUNMA ODASI ÖZETİ",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${report.homeClubName}  ${report.homeScore} - ${report.awayScore}  ${report.awayClubName}",
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = BackgroundPrimary.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, AccentPrimary)
                        ) {
                            Text(
                                text = "MAÇ REYTİNGİN: ${FLFormatters.formatRating(report.userRating)}  •  ${report.userGoals} GOL  •  ${report.userAssists} ASİST",
                                style = MaterialTheme.typography.labelLarge,
                                color = AccentBright,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Main Media Headline & Cumulative Impact Card
        item {
            val headlineText = sessionResult?.mainHeadline ?: report.pressHeadline
            Surface(
                color = SurfacePrimary,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.2.dp, AccentPrimary.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Newspaper,
                            contentDescription = null,
                            tint = AccentBright
                        )
                        Text(
                            text = "SON DAKİKA MANŞETİ & MEDYA YANSIMASI",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = AccentBright
                        )
                    }

                    Text(
                        text = headlineText,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        color = TextPrimary
                    )

                    if (sessionResult != null) {
                        Text(
                            text = "Baskın Üslup: ${sessionResult.dominantTone.labelTr} • Sosyal medya (X ve Instagram) akışına flaş röportaj yansımaları düştü!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        // Cumulative Impact Badges Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SummaryImpactPill("MORAL", sessionResult.totalMoraleDelta)
                            SummaryImpactPill("TARAFTAR", sessionResult.totalFanSupportDelta)
                            SummaryImpactPill("HOCA GÜVENİ", sessionResult.totalManagerTrustDelta)
                            SummaryImpactPill("TAKİPÇİ", sessionResult.totalFollowersDelta)
                        }
                    }
                }
            }
        }

        // 3. Answered Q&A Transcript (if sessionResult available)
        if (sessionResult != null && sessionResult.answeredItems.isNotEmpty()) {
            item {
                FLSectionHeader(
                    title = "FLASH RÖPORTAJ DÖKÜMÜ (${sessionResult.answeredItems.size} SORU)",
                    subtitle = "Mixed Zone'da muhabirlere verdiğin yanıtlar"
                )
            }

            itemsIndexed(sessionResult.answeredItems) { idx, item ->
                Surface(
                    color = SurfacePrimary,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, StrokePremium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${idx + 1}. ${item.question.reporter.channelName} (${item.question.reporter.reporterName})",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = GoldPrimary
                            )
                            Text(
                                text = item.selectedOption.tone.shortBadgeTr,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                color = AccentBright
                            )
                        }
                        Text(
                            text = "Soru: \"${item.question.questionText}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "Yanıtın: \"${item.effectiveQuote}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // 4. Manager & Teammate Locker Room Feedback + Match Bonus
        item {
            Surface(
                color = SurfacePrimary,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, StrokePremiumStrong),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "TEKNİK DİREKTÖR VE TAKIM DEĞERLENDİRMESİ",
                        style = MaterialTheme.typography.labelLarge,
                        color = GoldPrimary
                    )
                    Text(
                        text = report.managerFeedback,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = report.teammateReaction,
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentBright
                    )
                    Text(
                        text = "Kazanılan Maç & Gol Primi: +${FLFormatters.formatEuroFull(report.wageAndBonusEarnedEur)} • Harcanan Enerji: -%${report.energySpent}",
                        style = MaterialTheme.typography.labelMedium,
                        color = BrightGold
                    )
                }
            }
        }

        // 5. Return to Career Hub Button
        item {
            Button(
                onClick = onContinueToCareerHub,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPrimary,
                    contentColor = BackgroundPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_return_hub_after_match")
            ) {
                Text(
                    text = "KARİYER MERKEZİNE DÖN",
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }
}

@Composable
private fun SummaryImpactPill(
    label: String,
    delta: Int
) {
    val col = when {
        delta > 0 -> AccentBright
        delta < 0 -> Negative
        else -> TextSecondary
    }
    val sign = if (delta >= 0) "+" else ""
    Surface(
        color = BackgroundPrimary.copy(alpha = 0.85f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, col.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 9.5.sp,
                color = TextSecondary
            )
            Text(
                text = "$sign$delta",
                fontFamily = BarlowCondensedFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = col
            )
        }
    }
}
