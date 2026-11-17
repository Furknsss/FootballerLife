package com.example.ui.screens.phone

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.social.SocialWorldGenerator
import com.example.model.CareerSaveState
import com.example.ui.components.FLStatBar
import com.example.ui.components.parseSafeHexColor
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppScreen(
    state: CareerSaveState,
    selectedNpcId: String?,
    aiTypingTargetId: String? = null,
    onSelectNpc: (String?) -> Unit,
    onMarkNpcRead: (String) -> Unit = {},
    onSendMessage: (String, String) -> Unit,
    onStartCall: (String) -> Unit,
    onOpenCoachMeeting: () -> Unit = {},
    onBackHome: () -> Unit
) {
    val waBgDark = Color(0xFF0B141A)
    val waHeaderDark = Color(0xFF1F2C34)
    val waIncomingBubble = Color(0xFF202C33)
    val waOutgoingBubble = Color(0xFF005C4B)
    val waAccentGreen = Color(0xFF00A884)
    val waBrightGreen = Color(0xFF25D366)
    val waBlueTick = Color(0xFF53BDEB)
    val waMutedText = Color(0xFF8696A0)

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var customInput by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(0) } // 0: Tümü, 1: Okunmamış, 2: Gruplar, 3: Kulüp & Basın
    var waBottomTab by rememberSaveable { mutableStateOf(0) } // 0: Sohbetler, 1: Güncellemeler, 2: Aramalar
    var activeStatusId by rememberSaveable { mutableStateOf<String?>(null) }
    var statusReplyDraft by rememberSaveable { mutableStateOf("") }
    var showContactInfoSheet by rememberSaveable { mutableStateOf(false) }

    val activeNpc = state.npcs.find { it.id == selectedNpcId }
    val statuses = remember(state.currentDayIndex, state.npcs) {
        SocialWorldGenerator.getWhatsAppStatuses(state)
    }
    val activeStatus = statuses.find { it.id == activeStatusId }

    LaunchedEffect(activeNpc?.id) {
        if (activeNpc != null && activeNpc.unreadCount > 0) {
            onMarkNpcRead(activeNpc.id)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(waBgDark)
            .testTag("phone_messages_app")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (activeNpc == null) {
                // WhatsApp Authentic Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(waHeaderDark)
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBackHome,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("phone_app_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(7.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_whatsapp),
                                contentDescription = "WhatsApp",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(1.24f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WhatsApp",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .size(20.dp)
                        )
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(20.dp)
                        )
                    }
                }

                when (waBottomTab) {
                    0 -> {
                        // Search Bar (Meta / WhatsApp style pill)
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Meta AI'a sorun veya arayın", color = waMutedText, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = waMutedText) },
                            singleLine = true,
                            shape = CircleShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = waHeaderDark,
                                unfocusedContainerColor = waHeaderDark,
                                focusedBorderColor = waAccentGreen,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        // Filter Chips: Tümü | Okunmamış | Gruplar | Kulüp & Basın
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "Tümü" to 0,
                                "Okunmamış (${state.totalUnreadMessages})" to 1,
                                "Gruplar" to 2,
                                "Kulüp & Basın" to 3
                            ).forEach { (label, idx) ->
                                val active = selectedFilter == idx
                                Surface(
                                    shape = CircleShape,
                                    color = if (active) waAccentGreen.copy(alpha = 0.22f) else waHeaderDark,
                                    modifier = Modifier.clickable { selectedFilter = idx }
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (active) waBrightGreen else waMutedText,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        val filteredNpcs = state.npcs.filter { npc ->
                            val matchesQuery = searchQuery.isBlank() ||
                                npc.name.contains(searchQuery, ignoreCase = true) ||
                                npc.roleTitle.contains(searchQuery, ignoreCase = true) ||
                                npc.messages.lastOrNull()?.text?.contains(searchQuery, ignoreCase = true) == true
                            val matchesTab = when (selectedFilter) {
                                1 -> npc.unreadCount > 0
                                2 -> npc.id == "NPC_TEAM_GROUP" || npc.name.contains("Odası", ignoreCase = true)
                                3 -> npc.roleTitle.contains("Teknik", ignoreCase = true) ||
                                    npc.roleTitle.contains("Menajer", ignoreCase = true) ||
                                    npc.roleTitle.contains("Gazeteci", ignoreCase = true)
                                else -> true
                            }
                            matchesQuery && matchesTab
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(filteredNpcs, key = { it.id }) { npc ->
                                val lastMsg = npc.messages.lastOrNull()
                                val isTyping = aiTypingTargetId == npc.id
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onMarkNpcRead(npc.id)
                                            onSelectNpc(npc.id)
                                        }
                                        .testTag("chat_row_${npc.id}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 11.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(CircleShape)
                                                .background(parseSafeHexColor(npc.avatarColorHex, waAccentGreen)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = npc.name.take(1).uppercase(),
                                                fontFamily = BarlowCondensedFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 22.sp,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = npc.name,
                                                    fontFamily = OutfitFontFamily,
                                                    fontSize = 15.sp,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = lastMsg?.timeLabel ?: state.formattedClockTime,
                                                    fontFamily = SpaceGroteskFontFamily,
                                                    fontSize = 11.sp,
                                                    color = if (npc.unreadCount > 0) waBrightGreen else waMutedText,
                                                    fontWeight = if (npc.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                            Text(
                                                text = "${npc.roleTitle} • Yakınlık %${npc.affinity} • Güven %${npc.trust}",
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 11.sp,
                                                color = waBrightGreen
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    if (isTyping) {
                                                        Text(
                                                            text = "yazıyor...",
                                                            fontFamily = OutfitFontFamily,
                                                            fontSize = 13.sp,
                                                            color = waBrightGreen,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    } else {
                                                        if (lastMsg?.isFromUser == true) {
                                                            Icon(
                                                                imageVector = Icons.Filled.DoneAll,
                                                                contentDescription = null,
                                                                tint = waBlueTick,
                                                                modifier = Modifier.size(15.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                        }
                                                        Text(
                                                            text = lastMsg?.text ?: "Mesaj göndermek için dokunun",
                                                            fontFamily = OutfitFontFamily,
                                                            fontSize = 13.sp,
                                                            color = if (npc.unreadCount > 0) Color.White else waMutedText,
                                                            fontWeight = if (npc.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                                if (npc.unreadCount > 0) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .background(waBrightGreen),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "${npc.unreadCount}",
                                                            fontFamily = SpaceGroteskFontFamily,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = waBgDark
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    HorizontalDivider(
                                        color = Color(0xFF222D34),
                                        thickness = 0.6.dp,
                                        modifier = Modifier.padding(start = 76.dp)
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // Güncellemeler (WhatsApp Durum / Status)
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text(
                                    text = "DURUM GÜNCELLEMELERİ (DOKUN VE YANITLA)",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = waMutedText
                                )
                            }
                            items(statuses, key = { it.id }) { status ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = waHeaderDark,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            SocialWorldGenerator.markWaStatusViewed(status.id)
                                            activeStatusId = status.id
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    width = 2.dp,
                                                    color = if (status.viewed) waMutedText else waBrightGreen,
                                                    shape = CircleShape
                                                )
                                                .padding(3.dp)
                                                .clip(CircleShape)
                                                .background(parseSafeHexColor(status.avatarHex, waAccentGreen)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = status.authorName.take(1).uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(status.authorName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("${status.authorRole} • ${status.timeLabel}", color = waBrightGreen, fontSize = 11.sp)
                                            Text(
                                                text = status.caption,
                                                color = waMutedText,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        // Aramalar (WhatsApp Calls log)
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.npcs, key = { it.id }) { npc ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = waHeaderDark,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(parseSafeHexColor(npc.avatarColorHex, waAccentGreen)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(npc.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(npc.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text("${npc.roleTitle} • Sesli Arama", color = waMutedText, fontSize = 12.sp)
                                            }
                                        }
                                        IconButton(onClick = { onStartCall(npc.id) }) {
                                            Icon(Icons.Filled.Call, contentDescription = "Ara", tint = waBrightGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // WhatsApp Bottom Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(waHeaderDark)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        Triple(0, "Sohbetler", Icons.AutoMirrored.Filled.Chat),
                        Triple(1, "Güncellemeler", Icons.Filled.Public),
                        Triple(2, "Aramalar", Icons.Filled.Call)
                    ).forEach { (idx, title, icon) ->
                        val selected = waBottomTab == idx
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { waBottomTab = idx }
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (selected) waAccentGreen.copy(alpha = 0.25f) else Color.Transparent)
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (selected) waBrightGreen else waMutedText,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = title,
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Color.White else waMutedText
                            )
                        }
                    }
                }
            } else {
                // ============================================================
                // WHATSAPP ACTIVE CONVERSATION SCREEN
                // ============================================================
                val isNpcTyping = aiTypingTargetId == activeNpc.id

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(waHeaderDark)
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showContactInfoSheet = true }
                    ) {
                        IconButton(onClick = { onSelectNpc(null) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Sohbet Listesi", tint = Color.White)
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(parseSafeHexColor(activeNpc.avatarColorHex, waAccentGreen)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activeNpc.name.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeNpc.name,
                                fontFamily = OutfitFontFamily,
                                fontSize = 15.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isNpcTyping) {
                                    "yazıyor..."
                                } else {
                                    "çevrimiçi • ${activeNpc.roleTitle} (Güven %${activeNpc.trust})"
                                },
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                fontWeight = if (isNpcTyping) FontWeight.Bold else FontWeight.Normal,
                                color = waBrightGreen
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onStartCall(activeNpc.id) }) {
                            Icon(Icons.Filled.Call, contentDescription = "Ara", tint = Color.White)
                        }
                        IconButton(onClick = { showContactInfoSheet = true }) {
                            Icon(Icons.Filled.Info, contentDescription = "Kişi Bilgisi", tint = waMutedText)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(waBgDark),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF182229)
                            ) {
                                Text(
                                    text = "🔒 Mesajlar ve aramalar uçtan uca şifrelenmiştir.",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFFFFD279),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    items(activeNpc.messages, key = { it.id }) { msg ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (msg.isFromUser) Alignment.End else Alignment.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = if (msg.isFromUser) 12.dp else 2.dp,
                                    topEnd = if (msg.isFromUser) 2.dp else 12.dp,
                                    bottomStart = 12.dp,
                                    bottomEnd = 12.dp
                                ),
                                color = if (msg.isFromUser) waOutgoingBubble else waIncomingBubble,
                                modifier = Modifier.fillMaxWidth(0.84f)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                    if (!msg.isFromUser && (activeNpc.id == "NPC_TEAM_GROUP" || msg.emotionTag != "Doğal")) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = msg.senderName,
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = waBrightGreen
                                            )
                                            if (msg.emotionTag.isNotBlank() && msg.emotionTag != "Doğal") {
                                                Text(
                                                    text = "• ${msg.emotionTag}",
                                                    fontFamily = OutfitFontFamily,
                                                    fontSize = 10.sp,
                                                    color = waMutedText
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                    Text(
                                        text = msg.text,
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = msg.timeLabel,
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 10.sp,
                                            color = Color.White.copy(alpha = 0.65f)
                                        )
                                        if (msg.isFromUser) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Filled.DoneAll,
                                                contentDescription = "Görüldü",
                                                tint = waBlueTick,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    if (!msg.impactSummary.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.Black.copy(alpha = 0.25f)
                                        ) {
                                            Text(
                                                text = msg.impactSummary,
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 11.sp,
                                                color = GoldPrimary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // AI Typing Bubble Hook for @diyalog
                    if (isNpcTyping) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(topStart = 2.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp),
                                color = waIncomingBubble
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "•••",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = waBrightGreen
                                    )
                                    Text(
                                        text = "${activeNpc.name} yazıyor...",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 12.sp,
                                        color = waMutedText
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Reply Suggestions Row (Optional helper chips above free-text input)
                if (activeNpc.quickReplyOptions.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(waBgDark)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        activeNpc.quickReplyOptions.forEachIndexed { idx, opt ->
                            Surface(
                                shape = CircleShape,
                                color = waHeaderDark,
                                border = BorderStroke(1.dp, waAccentGreen.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .clickable { onSendMessage(activeNpc.id, opt) }
                                    .testTag("quick_reply_$idx")
                            ) {
                                Text(
                                    text = opt,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // WhatsApp Bottom Message Composer Bar (Full Free-Text Conversational Input)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(waBgDark)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customInput,
                        onValueChange = { customInput = it },
                        placeholder = { Text("Mesaj yazın", color = waMutedText, fontSize = 14.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_custom_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = waHeaderDark,
                            unfocusedContainerColor = waHeaderDark,
                            focusedBorderColor = waAccentGreen,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (customInput.isNotBlank()) {
                                onSendMessage(activeNpc.id, customInput)
                                customInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(waAccentGreen)
                            .testTag("chat_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Gönder",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Full-Screen WhatsApp Status Viewer Overlay
        if (activeStatus != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.96f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(CircleShape)
                                .background(waBrightGreen)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(parseSafeHexColor(activeStatus.avatarHex, waAccentGreen)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(activeStatus.authorName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(activeStatus.authorName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("${activeStatus.authorRole} • ${activeStatus.timeLabel}", color = waMutedText, fontSize = 11.sp)
                                }
                            }
                            IconButton(onClick = { activeStatusId = null }) {
                                Icon(Icons.Filled.Close, contentDescription = "Kapat", tint = Color.White)
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = waHeaderDark),
                        border = BorderStroke(1.dp, waBrightGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = activeStatus.caption,
                                fontFamily = OutfitFontFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = statusReplyDraft,
                            onValueChange = { statusReplyDraft = it },
                            placeholder = { Text("Duruma yanıt ver...", color = waMutedText) },
                            singleLine = true,
                            shape = CircleShape,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = waHeaderDark,
                                unfocusedContainerColor = waHeaderDark,
                                focusedBorderColor = waBrightGreen,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val reply = statusReplyDraft.ifBlank { "🔥👏" }
                                onSendMessage(activeStatus.npcId, "[Durum Yanıtı] $reply")
                                statusReplyDraft = ""
                                val targetNpc = activeStatus.npcId
                                activeStatusId = null
                                waBottomTab = 0
                                onSelectNpc(targetNpc)
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = waAccentGreen)
                        ) {
                            Text("Gönder", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Contact Info Bottom Sheet
        if (showContactInfoSheet && activeNpc != null) {
            ModalBottomSheet(
                onDismissRequest = { showContactInfoSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = waHeaderDark,
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "${activeNpc.name} • Kişi ve İlişki Bilgisi",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                    Text(
                        text = "${activeNpc.roleTitle} • Kişilik: ${activeNpc.personality} • Ruh Hâli: ${activeNpc.currentMood}",
                        fontSize = 13.sp,
                        color = waBrightGreen
                    )
                    FLStatBar(label = "Yakınlık (Affinity)", value = activeNpc.affinity)
                    FLStatBar(label = "Güven (Trust)", value = activeNpc.trust)
                    Text(
                        text = "Kısa Vadeli Hedef: ${activeNpc.shortTermGoal}",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                    if (activeNpc.importantMemories.isNotEmpty()) {
                        Text(
                            text = "Hatırladığı Anılar:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = waBrightGreen
                        )
                        activeNpc.importantMemories.take(4).forEach { mem ->
                            Text("• $mem", fontSize = 12.sp, color = waMutedText)
                        }
                    }
                    if (activeNpc.id == "NPC_MANAGER" || activeNpc.roleTitle.contains("Teknik", ignoreCase = true)) {
                        Button(
                            onClick = {
                                showContactInfoSheet = false
                                onOpenCoachMeeting()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = waAccentGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Teknik Direktör Odasında Yüz Yüze Görüş", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}
