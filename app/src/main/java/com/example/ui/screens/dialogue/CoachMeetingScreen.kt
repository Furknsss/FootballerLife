package com.example.ui.screens.dialogue

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CareerSaveState
import com.example.model.ChatMessage
import com.example.ui.components.ClubCrestBadge
import com.example.ui.components.FLStatBar
import com.example.ui.components.FLTopBar
import com.example.ui.components.parseSafeHexColor
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MatchDrawAmber
import com.example.ui.theme.MatchLossRed
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StrokePremium
import com.example.ui.theme.StrokePremiumStrong
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Interactive face-to-face Coach Office Meeting Screen (`HubOverlayScreen.COACH_MEETING`).
 *
 * Allows the player to talk or argue with the club's Head Coach in Turkish using free-text
 * or contextual agenda topics (Starting XI demand, tactical role, set-piece duties, rest request,
 * or discipline dispute). Powered by Gemini AI (`DialogueEngine`) with full conversation memory
 * and live career context, updating Manager Trust, Morale, and Starting Status (`İlk 11` / `Yedek`).
 */
@Composable
fun CoachMeetingScreen(
    career: CareerSaveState,
    isAiGenerating: Boolean,
    onSendCoachMessage: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onStartVoiceCallWithCoach: () -> Unit = {},
    onBack: () -> Unit
) {
    val club = career.currentClub
    val player = career.player
    val managerNpc = career.npcs.find { it.id == "NPC_MANAGER" }
    val messages = managerNpc?.messages.orEmpty()
    val nextMatch = career.todayUserFixture ?: career.nextUnplayedUserFixture
    val nextOpponent = nextMatch?.let {
        if (it.homeClubId == club.id) it.awayClubName else it.homeClubName
    } ?: "sıradaki rakibimiz"

    var selectedTopicIdx by rememberSaveable { mutableStateOf(0) }
    var freeTextInput by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    val agendaTopics = listOf(
        "İlk 11 & Forma Talebi" to "Hocam, idmanlarda kendimi çok hazır hissediyorum. $nextOpponent maçında İlk 11'de forma şansı istiyorum.",
        "Taktik & Duran Top" to "Hocam, ${club.managerTacticalStyle} sisteminizde hücum organizasyonlarında ve duran toplarda sorumluluk almak istiyorum.",
        "Kondisyon & İzin" to "Hocam, son günlerde fiziksel olarak biraz yorgunum. Maç gününe zinde çıkmak için bugün özel rejenerasyon programı uygulayabilir miyiz?",
        "Sert İtiraz / Tartışma" to "Hocam, yedek kulübesinde bekletilmemi haksızlık olarak görüyorum! Sahada ne yapmam gerektiğini kanıtladım, neden ilk 11'de değilim?"
    )

    LaunchedEffect(messages.size, isAiGenerating) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("coach_meeting_screen")
    ) {
        FLTopBar(
            title = "TEKNİK DİREKTÖR ODASI",
            subtitle = "${club.name} Tesisleri • Birebir Taktik & Kadro Görüşmesi",
            onBack = onBack,
            backTestTag = "btn_back_from_coach_meeting",
            trailingContent = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = SurfacePrimary,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AccentPrimary),
                        modifier = Modifier
                            .clickable { onStartVoiceCallWithCoach() }
                            .testTag("btn_coach_start_voice_call")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Sesli Ara",
                                tint = AccentBright,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sesli Ara",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }

                    Surface(
                        color = SurfacePrimary,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, StrokePremiumStrong),
                        modifier = Modifier
                            .clickable { onOpenSettings() }
                            .testTag("btn_coach_open_ai_settings")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Ayarları",
                                tint = AccentBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AI Ayarları",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Coach Office Atmospheric Hero Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = SurfacePrimary,
            border = BorderStroke(1.dp, StrokePremiumStrong),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.img_training_ground),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(138.dp)
                        .alpha(0.30f)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(138.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    BackgroundPrimary.copy(alpha = 0.45f),
                                    SurfacePrimary.copy(alpha = 0.92f),
                                    BackgroundSecondary
                                )
                            )
                        )
                )

                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(parseSafeHexColor(club.primaryColorHex, AccentPrimary))
                                        .border(2.dp, ChampionshipGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = club.managerName.take(1).uppercase(),
                                        fontFamily = BarlowCondensedFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 26.sp,
                                        color = Color.White
                                    )
                                }
                                ClubCrestBadge(
                                    shortName = club.shortName,
                                    primaryHex = club.primaryColorHex,
                                    secondaryHex = club.secondaryColorHex,
                                    size = 22.dp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = club.managerName,
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Teknik Direktör • ${club.managerTacticalStyle}",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.5.sp,
                                    color = AccentBright
                                )
                                Text(
                                    text = "Ruh Hali: ${managerNpc?.currentMood ?: "Gözlemci"} • Sıradaki: $nextOpponent",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Current Squad Status Badge
                        val statusColor = when (career.nextMatchStartingStatus.name) {
                            "STARTER" -> AccentBright
                            "BENCH" -> MatchDrawAmber
                            else -> MatchLossRed
                        }
                        Surface(
                            color = BackgroundPrimary.copy(alpha = 0.88f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, statusColor)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "MAÇ KADROSU",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = career.nextMatchStartingStatus.labelTr,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.5.sp,
                                    color = statusColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FLStatBar(
                            label = "Hoca Güveni",
                            value = career.managerTrust,
                            accentColor = if (career.managerTrust >= 50) AccentPrimary else MatchDrawAmber,
                            modifier = Modifier.weight(1f)
                        )
                        FLStatBar(
                            label = "Yakınlık",
                            value = managerNpc?.affinity ?: 55,
                            accentColor = AccentBright,
                            modifier = Modifier.weight(1f)
                        )
                        FLStatBar(
                            label = "Kondisyon",
                            value = player.energy,
                            accentColor = if (player.energy >= 55) AccentPrimary else MatchDrawAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Agenda Topic Chips (Quick Scenario Starters)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            agendaTopics.forEachIndexed { idx, (label, promptText) ->
                val isSelected = selectedTopicIdx == idx
                val isArgument = idx == 3
                Surface(
                    color = when {
                        isSelected && isArgument -> MatchLossRed.copy(alpha = 0.25f)
                        isSelected -> AccentPrimary.copy(alpha = 0.22f)
                        else -> SurfacePrimary
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        1.dp,
                        when {
                            isSelected && isArgument -> MatchLossRed
                            isSelected -> AccentBright
                            else -> StrokePremium
                        }
                    ),
                    modifier = Modifier
                        .clickable {
                            selectedTopicIdx = idx
                            freeTextInput = promptText
                        }
                        .testTag("coach_topic_$idx")
                ) {
                    Text(
                        text = label,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.5.sp,
                        color = if (isArgument) MatchLossRed else TextPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Conversation Transcript Area
        Surface(
            color = BackgroundSecondary,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, StrokePremium),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(messages, key = { it.id }) { msg ->
                    CoachDialogueBubble(
                        message = msg,
                        coachName = club.managerName,
                        playerName = player.fullName
                    )
                }

                if (isAiGenerating) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("coach_ai_typing_indicator")
                        ) {
                            CircularProgressIndicator(
                                color = AccentBright,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${club.managerName} taktik tahtasının başında yanıtlıyor...",
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                color = AccentBright
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dynamic Quick Replies suggested by Gemini or Persona
        val quickReplies = managerNpc?.quickReplyOptions.orEmpty()
        if (quickReplies.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickReplies.forEachIndexed { idx, reply ->
                    Surface(
                        color = SurfaceElevated,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, AccentPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable(enabled = !isAiGenerating) {
                                onSendCoachMessage(reply)
                            }
                            .testTag("coach_quick_reply_$idx")
                    ) {
                        Text(
                            text = reply,
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Free-Text Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = freeTextInput,
                onValueChange = { freeTextInput = it },
                placeholder = {
                    Text(
                        text = "Teknik direktöre istediğin cümleyi yaz (savun, söz ver, tartış)...",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                },
                singleLine = false,
                maxLines = 3,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfacePrimary,
                    unfocusedContainerColor = SurfacePrimary,
                    focusedBorderColor = AccentBright,
                    unfocusedBorderColor = StrokePremiumStrong,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("coach_free_text_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    val trimmed = freeTextInput.trim()
                    if (trimmed.isNotEmpty() && !isAiGenerating) {
                        onSendCoachMessage(trimmed)
                        freeTextInput = ""
                    }
                },
                enabled = freeTextInput.isNotBlank() && !isAiGenerating,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (freeTextInput.isNotBlank() && !isAiGenerating) AccentPrimary else SurfaceElevated)
                    .testTag("btn_send_coach_message")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Gönder",
                    tint = if (freeTextInput.isNotBlank() && !isAiGenerating) BackgroundPrimary else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun CoachDialogueBubble(
    message: ChatMessage,
    coachName: String,
    playerName: String
) {
    val isUser = message.isFromUser
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isUser) AccentPrimary.copy(alpha = 0.22f) else SurfaceElevated,
            shape = RoundedCornerShape(
                topStart = if (isUser) 16.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            border = BorderStroke(
                1.dp,
                if (isUser) AccentBright.copy(alpha = 0.65f) else StrokePremiumStrong
            ),
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "$playerName (Sen)" else "$coachName • Teknik Direktör",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = if (isUser) AccentBright else ChampionshipGold
                    )
                    if (!isUser && message.emotionTag.isNotBlank()) {
                        Surface(
                            color = BackgroundPrimary.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = message.emotionTag,
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBright,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    fontFamily = OutfitFontFamily,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
                if (!message.impactSummary.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = StrokePremium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.impactSummary,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = GoldPrimary
                    )
                }
            }
        }
    }
}
