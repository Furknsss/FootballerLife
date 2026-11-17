package com.example.ui.screens.dialogue

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.DialogueEngine
import com.example.model.CareerSaveState
import com.example.model.ChatMessage
import com.example.ui.components.parseSafeHexColor
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MatchLossRed
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StrokePremium
import com.example.ui.theme.StrokePremiumStrong
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

/**
 * Realistic full-screen Voice Phone Call Screen (`HubOverlayScreen.ACTIVE_PHONE_CALL`).
 *
 * Supports incoming and outgoing calls with:
 * - Agent (`NPC_AGENT`), Head Coach (`NPC_MANAGER`), Family (`NPC_FAMILY`),
 *   Club President (`NPC_PRESIDENT`), Team Captain (`NPC_CAPTAIN`), and Journalists (`NPC_PRESS`).
 * - Live call duration timer & animated voice wave pulse.
 * - Multi-turn spoken transcript powered by Gemini AI (`DialogueEngine`) + offline Turkish fallback.
 * - Free-text spoken input ("Telefonda ne söylemek istersin?") + quick response chips.
 */
@Composable
fun PhoneCallScreen(
    career: CareerSaveState,
    activeCallId: String?,
    isAiGenerating: Boolean,
    onSendSpokenLine: (callId: String, npcId: String, text: String) -> Unit,
    onSwitchCallTargetNpc: (npcId: String) -> Unit,
    onEndCall: (callId: String?) -> Unit
) {
    val enrichedCareer = remember(career) { DialogueEngine.ensureExtendedNpcs(career) }
    val activeCall = enrichedCareer.callRecords.find { it.id == activeCallId }
        ?: enrichedCareer.callRecords.firstOrNull { !it.completed }
        ?: enrichedCareer.callRecords.firstOrNull()

    val currentNpcId = activeCall?.npcId ?: "NPC_AGENT"
    val callerNpc = enrichedCareer.npcs.find { it.id == currentNpcId }
        ?: enrichedCareer.npcs.first()

    var callSeconds by remember { mutableIntStateOf(1) }
    var spokenDraft by rememberSaveable { mutableStateOf("") }
    var speakerOn by rememberSaveable { mutableStateOf(true) }
    val listState = rememberLazyListState()

    LaunchedEffect(activeCall?.id) {
        callSeconds = 1
        while (true) {
            delay(1000L)
            callSeconds++
        }
    }

    // Build spoken turns: opening transcript + recent conversation messages with this NPC
    val openingTurn = remember(activeCall?.id, activeCall?.transcript) {
        ChatMessage(
            id = "CALL_OPEN_${activeCall?.id ?: "INIT"}",
            senderName = activeCall?.callerName ?: callerNpc.name,
            isFromUser = false,
            text = activeCall?.transcript
                ?: "Alo ${enrichedCareer.player.firstName}, müsait misin? Seninle önemli bir konuyu konuşmak istedim.",
            dayIndex = enrichedCareer.currentDayIndex,
            timeLabel = "00:01",
            emotionTag = callerNpc.currentMood
        )
    }

    val conversationTurns = remember(activeCall, callerNpc.messages) {
        val recentNpcMsgs = callerNpc.messages.takeLast(6)
        if (recentNpcMsgs.isEmpty()) {
            listOf(openingTurn)
        } else {
            listOf(openingTurn) + recentNpcMsgs
        }
    }

    LaunchedEffect(conversationTurns.size, isAiGenerating) {
        if (conversationTurns.isNotEmpty()) {
            listState.animateScrollToItem(conversationTurns.size - 1)
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "call_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_pulse"
    )

    val mins = (callSeconds / 60).toString().padStart(2, '0')
    val secs = (callSeconds % 60).toString().padStart(2, '0')

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF061610),
                        BackgroundPrimary,
                        Color(0xFF030806)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("phone_call_screen"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Caller Identity & Quick Contact Switcher (Coach, Agent, Family, President, Press, Captain)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                enrichedCareer.npcs.forEach { npc ->
                    val selected = npc.id == callerNpc.id
                    Surface(
                        color = if (selected) AccentPrimary else SurfacePrimary,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, if (selected) AccentBright else StrokePremium),
                        modifier = Modifier
                            .clickable { onSwitchCallTargetNpc(npc.id) }
                            .testTag("call_switch_npc_${npc.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = if (selected) BackgroundPrimary else AccentBright,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = npc.roleTitle,
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (selected) BackgroundPrimary else TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pulsing Caller Avatar
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(AccentPrimary.copy(alpha = 0.18f))
                )
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(parseSafeHexColor(callerNpc.avatarColorHex, AccentPrimary))
                        .border(2.dp, AccentBright, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = callerNpc.name.take(1).uppercase(),
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = callerNpc.name,
                fontFamily = BarlowCondensedFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = TextPrimary
            )
            Text(
                text = "${callerNpc.roleTitle} • ${activeCall?.topic ?: "Canlı Telefon Görüşmesi"}",
                fontFamily = OutfitFontFamily,
                fontSize = 12.5.sp,
                color = AccentBright
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Call Timer & Audio Wave Indicator
            Surface(
                color = SurfacePrimary,
                shape = CircleShape,
                border = BorderStroke(1.dp, StrokePremiumStrong)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = AccentBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HD SES • $mins:$secs • Güven %${callerNpc.trust} • Yakınlık %${callerNpc.affinity}",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Live Call Spoken Transcript Feed
        Surface(
            color = BackgroundSecondary.copy(alpha = 0.92f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, StrokePremiumStrong),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(conversationTurns, key = { it.id }) { turn ->
                    val isUser = turn.isFromUser
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            color = if (isUser) AccentPrimary.copy(alpha = 0.22f) else SurfaceElevated,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isUser) AccentBright.copy(alpha = 0.65f) else StrokePremium
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
                                        text = if (isUser) "🎙️ Sen (${enrichedCareer.player.firstName})" else "📞 ${callerNpc.name}",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = if (isUser) AccentBright else ChampionshipGold
                                    )
                                    if (!isUser && turn.emotionTag.isNotBlank()) {
                                        Text(
                                            text = turn.emotionTag,
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 10.sp,
                                            color = AccentBright
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${turn.text}\"",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    lineHeight = 20.sp
                                )
                                if (!turn.impactSummary.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(5.dp))
                                    HorizontalDivider(color = StrokePremium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = turn.impactSummary,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.5.sp,
                                        color = GoldPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                if (isAiGenerating) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(8.dp)
                                .testTag("call_ai_speaking_indicator")
                        ) {
                            CircularProgressIndicator(
                                color = AccentBright,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${callerNpc.name} hattın diğer ucunda konuşuyor...",
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                color = AccentBright
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Spoken Options + Free-Text Voice Reply Composer + Hang Up Controls
        Column(modifier = Modifier.fillMaxWidth()) {
            val quickOptions = (activeCall?.options?.takeIf { it.isNotEmpty() }
                ?: callerNpc.quickReplyOptions).take(3)

            if (quickOptions.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickOptions.forEachIndexed { idx, opt ->
                        Surface(
                            color = SurfacePrimary,
                            shape = CircleShape,
                            border = BorderStroke(1.dp, AccentPrimary.copy(alpha = 0.55f)),
                            modifier = Modifier
                                .clickable(enabled = !isAiGenerating) {
                                    onSendSpokenLine(activeCall?.id ?: "", callerNpc.id, opt)
                                }
                                .testTag("call_quick_reply_$idx")
                        ) {
                            Text(
                                text = opt,
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Free-text spoken input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = spokenDraft,
                    onValueChange = { spokenDraft = it },
                    placeholder = {
                        Text(
                            text = "Telefonda kendi cümleni söyle...",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = AccentBright)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp),
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
                        .testTag("call_free_text_input")
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val clean = spokenDraft.trim()
                        if (clean.isNotEmpty() && !isAiGenerating) {
                            onSendSpokenLine(activeCall?.id ?: "", callerNpc.id, clean)
                            spokenDraft = ""
                        }
                    },
                    enabled = spokenDraft.isNotBlank() && !isAiGenerating,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (spokenDraft.isNotBlank() && !isAiGenerating) AccentPrimary else SurfaceElevated)
                        .testTag("btn_send_call_line")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Söyle",
                        tint = if (spokenDraft.isNotBlank() && !isAiGenerating) BackgroundPrimary else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Speaker & Red Hang Up Button Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (speakerOn) SurfaceElevated else SurfacePrimary,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (speakerOn) AccentBright else StrokePremium),
                    modifier = Modifier
                        .weight(0.42f)
                        .height(48.dp)
                        .clickable { speakerOn = !speakerOn }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Hoparlör",
                            tint = if (speakerOn) AccentBright else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (speakerOn) "Hoparlör Açık" else "Ahize Modu",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }
                }

                Button(
                    onClick = { onEndCall(activeCall?.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MatchLossRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(0.58f)
                        .height(48.dp)
                        .testTag("btn_end_phone_call")
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ARAMAYI SONLANDIR",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
