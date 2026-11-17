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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.social.CommentReactionType
import com.example.engine.social.PlayerFameTier
import com.example.engine.social.SocialAccountProfile
import com.example.engine.social.SocialWorldGenerator
import com.example.engine.social.VerifiedBadgeType
import com.example.model.CareerSaveState
import com.example.model.InstaCommentItem
import com.example.model.SocialMediaPost
import com.example.model.StoryItem
import com.example.ui.components.FLPrimaryButton
import com.example.ui.components.FootballerAvatarCanvas
import com.example.ui.components.parseSafeHexColor
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Negative
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstagramAppScreen(
    state: CareerSaveState,
    activeStoryId: String?,
    typingChatIds: Set<String> = emptySet(),
    onOpenStory: (String) -> Unit,
    onCloseStory: () -> Unit,
    onViewReactStory: (String, Boolean) -> Unit,
    onToggleLike: (String) -> Unit,
    onToggleSave: (String) -> Unit,
    onComment: (String, String) -> Unit,
    onToggleLikeComment: (String, String) -> Unit,
    onAcceptDmRequest: (String) -> Unit,
    onDeleteDmRequest: (String) -> Unit,
    onSendDmMessage: (String, String) -> Unit,
    onShareFromGallery: (String, String) -> Unit,
    onPublishPost: (String, String) -> Unit,
    onBackHome: () -> Unit
) {
    val igBlue = Color(0xFF0095F6)
    val igRed = Color(0xFFFF3040)
    val igGray = Color(0xFFA8A8A8)
    val igDarkBorder = Color(0xFF262626)

    // 0: Feed, 1: Explore, 2: Share (+), 3: Activity (♥), 4: Profile
    var tabIndex by rememberSaveable { mutableStateOf(0) }
    var bottomSheetPostId by rememberSaveable { mutableStateOf<String?>(null) }
    var commentDraftText by rememberSaveable { mutableStateOf("") }
    var replyingToCommentId by rememberSaveable { mutableStateOf<String?>(null) }
    var newPostCaption by rememberSaveable { mutableStateOf("") }
    var exploreQuery by rememberSaveable { mutableStateOf("") }
    var exploreCategoryIdx by rememberSaveable { mutableStateOf(0) }
    var profileSubTab by rememberSaveable { mutableStateOf(0) } // 0: Grid, 1: Etiketlenenler, 2: Kaydedilenler

    // Inspected account profile & inspected single post modal
    var inspectedAccountHandle by rememberSaveable { mutableStateOf<String?>(null) }
    var inspectedSinglePostId by rememberSaveable { mutableStateOf<String?>(null) }

    // Instagram Direct (DM) navigation states
    var isDmInboxOpen by rememberSaveable { mutableStateOf(false) }
    var dmTabIndex by rememberSaveable { mutableStateOf(0) } // 0: Mesajlar, 1: İstekler
    var activeDmThreadId by rememberSaveable { mutableStateOf<String?>(null) }
    var dmDraftText by rememberSaveable { mutableStateOf("") }
    var showNoteEditor by rememberSaveable { mutableStateOf(false) }
    var noteDraftText by rememberSaveable { mutableStateOf("") }
    var storyReplyDraft by rememberSaveable { mutableStateOf("") }

    val expandedReplyThreads = remember { mutableStateMapOf<String, Boolean>() }

    val livingStories = SocialWorldGenerator.getLivingStories(state)
    val livingFeed = SocialWorldGenerator.getLivingInstaFeed(state)
    val allDmThreads = SocialWorldGenerator.getAllInstaDmThreads(state)
    val instaNotes = SocialWorldGenerator.getInstaNotes(state)
    val notifications = SocialWorldGenerator.getNotifications(state, "INSTAGRAM")

    val activeStory = livingStories.find { it.id == activeStoryId }
    val sheetPost = livingFeed.find { it.id == bottomSheetPostId }
    val activeDmThread = allDmThreads.find { it.id == activeDmThreadId }
    val inspectedPost = livingFeed.find { it.id == inspectedSinglePostId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("phone_instapitch_app")
    ) {
        when {
            isDmInboxOpen -> {
                // ================================================================
                // INSTAGRAM DIRECT (DM), NOTLAR & BEKLEYEN İSTEKLER EKRANI
                // ================================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .testTag("insta_dm_screen")
                ) {
                    if (activeDmThread == null) {
                        // DM Inbox Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black)
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { isDmInboxOpen = false }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Geri",
                                        tint = Color.White
                                    )
                                }
                                Text(
                                    text = "${state.player.firstName.lowercase()}${state.player.kitNumber}",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = null,
                                    tint = igBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Instagram Direct",
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                color = igGray,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }

                        // Instagram Notes ("Notlar") Horizontal Tray
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            instaNotes.forEach { note ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(78.dp)
                                        .clickable {
                                            if (note.id == "NOTE_USER") {
                                                showNoteEditor = !showNoteEditor
                                            } else if (note.dmThreadId != null) {
                                                activeDmThreadId = note.dmThreadId
                                            } else {
                                                inspectedAccountHandle = note.handle
                                                isDmInboxOpen = false
                                            }
                                        }
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF262626),
                                        border = BorderStroke(0.6.dp, Color(0xFF3E3E3E))
                                    ) {
                                        Text(
                                            text = note.noteText,
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 10.sp,
                                            color = Color.White,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(parseSafeHexColor(note.avatarHex, Color(0xFF262626))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = note.authorName.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 18.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = note.authorName,
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 11.sp,
                                        color = igGray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        if (showNoteEditor) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = noteDraftText,
                                    onValueChange = { noteDraftText = it },
                                    placeholder = { Text("Yeni bir not paylaş (60 karakter)...", color = igGray, fontSize = 12.sp) },
                                    singleLine = true,
                                    shape = CircleShape,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF262626),
                                        unfocusedContainerColor = Color(0xFF262626),
                                        focusedBorderColor = igBlue,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        SocialWorldGenerator.updateUserInstaNote(state.saveSlotId, noteDraftText)
                                        noteDraftText = ""
                                        showNoteEditor = false
                                    },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = igBlue)
                                ) {
                                    Text("Paylaş", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        val primaryThreads = allDmThreads.filter { !it.isPendingRequest }
                        val requestThreads = allDmThreads.filter { it.isPendingRequest }

                        // Primary vs Message Requests Tabs ("Mesajlar" | "İstekler (X)")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(
                                "Mesajlar (${primaryThreads.size})" to 0,
                                "İstekler (${requestThreads.size})" to 1
                            ).forEach { (label, idx) ->
                                val selected = dmTabIndex == idx
                                Surface(
                                    shape = CircleShape,
                                    color = if (selected) Color.White else Color(0xFF262626),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { dmTabIndex = idx }
                                        .testTag("insta_dm_tab_$idx")
                                ) {
                                    Text(
                                        text = label,
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (selected) Color.Black else Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        if (dmTabIndex == 1 && requestThreads.isNotEmpty()) {
                            Text(
                                text = "Şöhretin arttıkça markalar, gazeteciler ve fenomenlerden mesaj istekleri gelir. Sohbet etmek için isteği kabul et.",
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                color = igGray,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }

                        val displayedThreads = if (dmTabIndex == 0) primaryThreads else requestThreads

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            items(displayedThreads, key = { it.id }) { thread ->
                                val lastMsg = thread.messages.lastOrNull()
                                val isTyping = thread.id in typingChatIds
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { activeDmThreadId = thread.id }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                        .testTag("insta_dm_row_${thread.id}"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFFFEDA77), Color(0xFFDD2A7B), Color(0xFF8134AF))
                                                )
                                            )
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                            .background(parseSafeHexColor(thread.avatarHex, Color(0xFF262626))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = thread.authorName.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = thread.authorName,
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                            if (thread.isVerified) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Filled.Verified,
                                                    contentDescription = null,
                                                    tint = igBlue,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "• ${thread.followerCountLabel}",
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 11.sp,
                                                color = igGray
                                            )
                                        }
                                        Text(
                                            text = "${thread.handle} • ${thread.roleLabel}",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 11.sp,
                                            color = igBlue
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isTyping) "yazıyor..." else (lastMsg?.text ?: ""),
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 13.sp,
                                            color = if (isTyping) igBlue else if (thread.unreadCount > 0 || thread.isPendingRequest) Color.White else igGray,
                                            fontWeight = if (isTyping || thread.unreadCount > 0 || thread.isPendingRequest) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (thread.isPendingRequest) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = igBlue.copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, igBlue)
                                        ) {
                                            Text(
                                                text = "İstek",
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = igBlue,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else if (thread.unreadCount > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(igBlue)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Active Instagram Direct Conversation Thread
                        val isThreadTyping = activeDmThread.id in typingChatIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF121212))
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { activeDmThreadId = null }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Geri",
                                    tint = Color.White
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        inspectedAccountHandle = activeDmThread.handle
                                        activeDmThreadId = null
                                        isDmInboxOpen = false
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(parseSafeHexColor(activeDmThread.avatarHex, Color(0xFF262626))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = activeDmThread.authorName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = activeDmThread.authorName,
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                        if (activeDmThread.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Filled.Verified,
                                                contentDescription = null,
                                                tint = igBlue,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (isThreadTyping) "yazıyor..." else "${activeDmThread.handle} • ${activeDmThread.roleLabel}",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 11.sp,
                                        color = if (isThreadTyping) igBlue else igGray
                                    )
                                }
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color.Black),
                            contentPadding = PaddingValues(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                // Instagram DM Top Profile Summary Card
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                            .background(parseSafeHexColor(activeDmThread.avatarHex, Color(0xFF262626))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = activeDmThread.authorName.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 26.sp,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = activeDmThread.authorName,
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${activeDmThread.handle} • Instagram • ${activeDmThread.followerCountLabel}",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 12.sp,
                                        color = igGray
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF262626),
                                        modifier = Modifier.clickable {
                                            inspectedAccountHandle = activeDmThread.handle
                                            activeDmThreadId = null
                                            isDmInboxOpen = false
                                        }
                                    ) {
                                        Text(
                                            text = "Profili Gör",
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            items(activeDmThread.messages, key = { it.id }) { msg ->
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = if (msg.isFromUser) Alignment.End else Alignment.Start
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (msg.isFromUser) Color(0xFF3797F0) else Color(0xFF262626),
                                        modifier = Modifier.fillMaxWidth(0.82f)
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                            Text(
                                                text = msg.text,
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                            if (!msg.impactSummary.isNullOrBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Surface(
                                                    color = igBlue.copy(alpha = 0.22f),
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(0.8.dp, Color(0xFF3797F0))
                                                ) {
                                                    Text(
                                                        text = msg.impactSummary,
                                                        fontFamily = SpaceGroteskFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.5.sp,
                                                        color = Color(0xFF70C0FF),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = if (msg.isFromUser) "${msg.timeLabel} • Görüldü" else msg.timeLabel,
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontSize = 10.sp,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }

                            if (isThreadTyping) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(0xFF262626)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text("•••", color = igBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text("${activeDmThread.authorName} yazıyor...", color = igGray, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        if (activeDmThread.isPendingRequest) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF121212))
                                    .padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${activeDmThread.authorName} (${activeDmThread.handle}) sana mesaj göndermek istiyor. Kabul edersen mesaj yazabilirsin.",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 12.sp,
                                    color = igGray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val idToDelete = activeDmThread.id
                                            activeDmThreadId = null
                                            onDeleteDmRequest(idToDelete)
                                        },
                                        border = BorderStroke(1.dp, Negative),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("insta_dm_delete_${activeDmThread.id}")
                                    ) {
                                        Text("Sil", color = Negative, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            onAcceptDmRequest(activeDmThread.id)
                                            dmTabIndex = 0
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = igBlue),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("insta_dm_accept_${activeDmThread.id}")
                                    ) {
                                        Text("Kabul Et", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF121212))
                            ) {
                                val quickSuggestions = remember(activeDmThread.id) {
                                    when {
                                        activeDmThread.roleLabel.contains("Marka", ignoreCase = true) ||
                                            activeDmThread.handle.contains("nike", ignoreCase = true) ||
                                            activeDmThread.handle.contains("puma", ignoreCase = true) -> listOf(
                                            "Teklifinizi kabul ediyorum, anlaşalım 🤝",
                                            "Detayları ve bütçeyi konuşalım 💬",
                                            "Menajerimle görüşebilirsiniz 📋"
                                        )
                                        activeDmThread.roleLabel.contains("Gazeteci", ignoreCase = true) ||
                                            activeDmThread.roleLabel.contains("Scout", ignoreCase = true) -> listOf(
                                            "Şu an tek odağım kulübüm ve maçlarım ⚽",
                                            "Avrupa hedefim her zaman var 🔥",
                                            "Sezon sonu neler olacağını göreceğiz 👀"
                                        )
                                        else -> listOf(
                                            "Çok teşekkür ederim, desteğiniz güç veriyor! 🙏⚽",
                                            "Sıradaki maçta görüşürüz! 🔥",
                                            "İyi ki varsınız 🙌"
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    quickSuggestions.forEach { suggestion ->
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF262626),
                                            border = BorderStroke(0.8.dp, Color(0xFF3797F0).copy(alpha = 0.6f)),
                                            modifier = Modifier.clickable {
                                                if (!SocialWorldGenerator.appendLocalDmMessageIfExtra(activeDmThread.id, state, suggestion)) {
                                                    onSendDmMessage(activeDmThread.id, suggestion)
                                                }
                                            }
                                        ) {
                                            Text(
                                                text = suggestion,
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 11.5.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = dmDraftText,
                                        onValueChange = { dmDraftText = it },
                                        placeholder = { Text("Mesaj...", color = igGray, fontSize = 14.sp) },
                                        singleLine = true,
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("insta_dm_input"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF262626),
                                            unfocusedContainerColor = Color(0xFF262626),
                                            focusedBorderColor = Color(0xFF3797F0),
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (dmDraftText.isNotBlank()) {
                                                if (!SocialWorldGenerator.appendLocalDmMessageIfExtra(activeDmThread.id, state, dmDraftText)) {
                                                    onSendDmMessage(activeDmThread.id, dmDraftText)
                                                }
                                                dmDraftText = ""
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = igBlue),
                                        shape = CircleShape,
                                        modifier = Modifier.testTag("insta_dm_send_btn")
                                    ) {
                                        Text("Gönder", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            inspectedAccountHandle != null -> {
                // ================================================================
                // UNIVERSAL INSTAGRAM ACCOUNT PROFILE PAGE (CLICKED ANY USER/CLUB)
                // ================================================================
                val account = SocialWorldGenerator.resolveAccount(inspectedAccountHandle!!, state)
                val accountPosts = livingFeed.filter {
                    it.authorHandle.removePrefix("@").equals(account.cleanHandle, ignoreCase = true) ||
                        it.authorName.equals(account.displayName, ignoreCase = true)
                }.ifEmpty { livingFeed.take(6) }

                InstagramAccountProfileView(
                    account = account,
                    state = state,
                    posts = accountPosts,
                    isOwnProfile = account.id == "ACC_USER",
                    profileSubTab = profileSubTab,
                    onSelectSubTab = { profileSubTab = it },
                    onBack = { inspectedAccountHandle = null },
                    onOpenPost = { inspectedSinglePostId = it },
                    onMessageAccount = {
                        val thread = SocialWorldGenerator.getOrCreateDmThreadForAccount(account, state)
                        inspectedAccountHandle = null
                        isDmInboxOpen = true
                        activeDmThreadId = thread.id
                    }
                )
            }

            else -> {
                // ================================================================
                // INSTAGRAM MAIN APP (TOP BAR + 5 TABS + BOTTOM NAV)
                // ================================================================
                Column(modifier = Modifier.fillMaxSize()) {
                    // Authentic Instagram Dark Mode Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
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
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(7.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.logo_instagram),
                                    contentDescription = "Instagram",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .scale(1.24f)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instagram",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                color = Color.White
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { tabIndex = 2 }) {
                                Icon(
                                    imageVector = Icons.Filled.AddCircleOutline,
                                    contentDescription = "Yeni Gönderi",
                                    tint = Color.White
                                )
                            }
                            // Notifications Heart Icon
                            Box(contentAlignment = Alignment.TopEnd) {
                                IconButton(onClick = { tabIndex = 3 }) {
                                    Icon(
                                        imageVector = if (tabIndex == 3) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = "Etkinlik",
                                        tint = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .padding(top = 8.dp, end = 8.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(igRed)
                                )
                            }
                            // Direct Messages Paper Plane Icon
                            Box(contentAlignment = Alignment.TopEnd) {
                                IconButton(
                                    onClick = { isDmInboxOpen = true },
                                    modifier = Modifier.testTag("insta_open_dm_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Direct Mesajlar",
                                        tint = Color.White
                                    )
                                }
                                val dmBadge = state.totalUnreadInstaDms + state.totalPendingInstaRequests
                                if (dmBadge > 0) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp, end = 4.dp)
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(igRed),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$dmBadge",
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = igDarkBorder, thickness = 0.6.dp)

                    // Main Content Area
                    Box(modifier = Modifier.weight(1f)) {
                        when (tabIndex) {
                            0 -> {
                                // Instagram Home Feed: Stories Tray + Long Scrolling Living Feed
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    item {
                                        Column {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState())
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                                            ) {
                                                // "Hikayen" (Player's own story slot)
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier
                                                        .width(72.dp)
                                                        .clickable { tabIndex = 2 }
                                                ) {
                                                    Box(contentAlignment = Alignment.BottomEnd) {
                                                        FootballerAvatarCanvas(
                                                            appearance = state.player.appearance,
                                                            clubPrimaryHex = state.currentClub.primaryColorHex,
                                                            clubSecondaryHex = state.currentClub.secondaryColorHex,
                                                            kitNumber = state.player.kitNumber,
                                                            modifier = Modifier
                                                                .size(64.dp)
                                                                .clip(CircleShape)
                                                        )
                                                        Box(
                                                            modifier = Modifier
                                                                .size(20.dp)
                                                                .clip(CircleShape)
                                                                .background(igBlue)
                                                                .border(2.dp, Color.Black, CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Hikayen",
                                                        fontFamily = OutfitFontFamily,
                                                        fontSize = 11.sp,
                                                        color = Color.White,
                                                        maxLines = 1
                                                    )
                                                }

                                                livingStories.forEach { story ->
                                                    StoryRingAvatarItem(
                                                        story = story,
                                                        onClick = {
                                                            SocialWorldGenerator.markStoryViewedOrReactedLocal(story.id, false)
                                                            onViewReactStory(story.id, false)
                                                            onOpenStory(story.id)
                                                        }
                                                    )
                                                }
                                            }
                                            HorizontalDivider(color = igDarkBorder, thickness = 0.6.dp)
                                        }
                                    }

                                    // Pull-To-Refresh Top Action Banner
                                    item {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = Color(0xFF16181C),
                                            border = BorderStroke(0.6.dp, Color(0xFF2F3336)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 4.dp)
                                                .clickable {
                                                    SocialWorldGenerator.refreshInstaFeed(state)
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Refresh,
                                                    contentDescription = null,
                                                    tint = igBlue,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "⚡ Yeni Gönderileri Yükle (Akışı Yenile)",
                                                    fontFamily = OutfitFontFamily,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFFE0E0E0)
                                                )
                                            }
                                        }
                                    }

                                    items(livingFeed, key = { it.id }) { post ->
                                        InstaFeedPostCard(
                                            post = post,
                                            state = state,
                                            onOpenAuthorProfile = { inspectedAccountHandle = post.authorHandle },
                                            onOpenCommentsSheet = { bottomSheetPostId = post.id },
                                            onToggleLike = {
                                                SocialWorldGenerator.toggleInstaPostLikeLocal(post.id, post.likedByUser)
                                                onToggleLike(post.id)
                                            },
                                            onToggleSave = {
                                                SocialWorldGenerator.toggleInstaPostSaveLocal(post.id, post.savedByUser)
                                                onToggleSave(post.id)
                                            }
                                        )
                                    }

                                    // Infinite Scroll Pagination Loader Card
                                    item {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF121417),
                                            border = BorderStroke(0.6.dp, Color(0xFF262626)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                                .clickable {
                                                    SocialWorldGenerator.loadMoreInstaPosts(state)
                                                }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.AddCircleOutline,
                                                    contentDescription = null,
                                                    tint = igBlue,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Daha Fazla Gönderi Yükle (Sonsuz Akış) ⚡",
                                                    fontFamily = OutfitFontFamily,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> {
                                // Keşfet (Instagram Explore Search + Categories + Accounts + 3-Col Mosaic)
                                val categories = listOf("Sana Özel", "Maç Gündemi", "Transfer", "Mizah & Caps", "Avrupa", "Sponsorlar")
                                val allAccounts = SocialWorldGenerator.getAccounts(state)
                                val matchingAccounts = allAccounts.filter {
                                    exploreQuery.isBlank() ||
                                        it.displayName.contains(exploreQuery, ignoreCase = true) ||
                                        it.handle.contains(exploreQuery, ignoreCase = true) ||
                                        it.bio.contains(exploreQuery, ignoreCase = true)
                                }
                                val filteredExplorePosts = livingFeed.filter { post ->
                                    val matchesQ = exploreQuery.isBlank() ||
                                        post.content.contains(exploreQuery, ignoreCase = true) ||
                                        post.authorName.contains(exploreQuery, ignoreCase = true) ||
                                        post.authorHandle.contains(exploreQuery, ignoreCase = true)
                                    val matchesCat = when (exploreCategoryIdx) {
                                        1 -> post.imageSceneType == "STADIUM" || post.authorHandle.contains("mackolik") || post.authorHandle.contains("sportsdigitale")
                                        2 -> post.imageSceneType == "TRANSFER" || post.authorHandle.contains("fabrizio") || post.authorHandle.contains("transfermarkt")
                                        3 -> post.authorHandle.contains("nocontext") || post.authorHandle.contains("krampon") || post.authorHandle.contains("mizah")
                                        4 -> post.authorHandle.contains("arda") || post.authorHandle.contains("433") || post.authorHandle.contains("hakan")
                                        5 -> post.authorHandle.contains("nike") || post.authorHandle.contains("puma") || post.authorHandle.contains("elvin")
                                        else -> true
                                    }
                                    matchesQ && matchesCat
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    item {
                                        OutlinedTextField(
                                            value = exploreQuery,
                                            onValueChange = { exploreQuery = it },
                                            placeholder = { Text("Hesap, futbolcu, kulüp veya etiket ara...", color = igGray, fontSize = 13.sp) },
                                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = igGray) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = Color(0xFF1C1C1E),
                                                unfocusedContainerColor = Color(0xFF1C1C1E),
                                                focusedBorderColor = igBlue,
                                                unfocusedBorderColor = Color.Transparent,
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )
                                    }

                                    item {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            categories.forEachIndexed { idx, label ->
                                                val sel = exploreCategoryIdx == idx
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = if (sel) Color.White else Color(0xFF1C1C1E),
                                                    border = BorderStroke(1.dp, if (sel) Color.White else igDarkBorder),
                                                    modifier = Modifier.clickable { exploreCategoryIdx = idx }
                                                ) {
                                                    Text(
                                                        text = label,
                                                        fontFamily = OutfitFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = if (sel) Color.Black else Color.White,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    item {
                                        Text(
                                            text = "ÖNE ÇIKAN FUTBOL HESAPLARI",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = igGray
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            matchingAccounts.filter { it.id != "ACC_USER" }.take(12).forEach { acc ->
                                                val following = SocialWorldGenerator.isFollowingAccount(acc.handle, state)
                                                Surface(
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = Color(0xFF121212),
                                                    border = BorderStroke(1.dp, igDarkBorder),
                                                    modifier = Modifier
                                                        .width(136.dp)
                                                        .clickable { inspectedAccountHandle = acc.handle }
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        modifier = Modifier.padding(10.dp)
                                                    ) {
                                                        AccountAvatarCircle(acc = acc, sizeDp = 46)
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = acc.displayName,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            if (acc.isVerified) {
                                                                Spacer(modifier = Modifier.width(3.dp))
                                                                Icon(
                                                                    imageVector = Icons.Filled.Verified,
                                                                    contentDescription = null,
                                                                    tint = if (acc.badgeType == VerifiedBadgeType.GOLD_ORG) GoldPrimary else igBlue,
                                                                    modifier = Modifier.size(12.dp)
                                                                )
                                                            }
                                                        }
                                                        Text(
                                                            text = "${acc.followersCountText} takipçi",
                                                            fontSize = 10.sp,
                                                            color = igGray
                                                        )
                                                        Spacer(modifier = Modifier.height(6.dp))
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = if (following) Color(0xFF262626) else igBlue,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clickable {
                                                                    SocialWorldGenerator.toggleFollowAccount(acc.handle, state)
                                                                }
                                                        ) {
                                                            Text(
                                                                text = if (following) "Takiptesin" else "Takip Et",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White,
                                                                textAlign = TextAlign.Center,
                                                                modifier = Modifier.padding(vertical = 5.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    item {
                                        Text(
                                            text = "KEŞFET AKIŞI (${filteredExplorePosts.size} GÖNDERİ)",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = igGray
                                        )
                                    }

                                    val gridRows = filteredExplorePosts.chunked(3)
                                    items(gridRows) { rowPosts ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            rowPosts.forEach { post ->
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(122.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .clickable { inspectedSinglePostId = post.id }
                                                ) {
                                                    Image(
                                                        painter = painterResource(id = resolveInstaSceneDrawable(post.imageSceneType)),
                                                        contentDescription = post.content,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(
                                                                Brush.verticalGradient(
                                                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))
                                                                )
                                                            )
                                                    )
                                                    Column(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomStart)
                                                            .padding(6.dp)
                                                    ) {
                                                        Text(
                                                            text = post.authorHandle,
                                                            fontSize = 9.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White,
                                                            maxLines = 1
                                                        )
                                                        Text(
                                                            text = "❤️ ${SocialWorldGenerator.formatCompactCount(post.likesCount)}",
                                                            fontSize = 9.sp,
                                                            color = Color(0xFFE0E0E0)
                                                        )
                                                    }
                                                }
                                            }
                                            repeat(3 - rowPosts.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }

                            2 -> {
                                // Paylaş: Yeni Gönderi Paylaş (Takipçi & Şöhret Etkili)
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    item {
                                        Card(
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                                            border = BorderStroke(1.dp, igDarkBorder)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Text(
                                                    text = "YENİ INSTAGRAM GÖNDERİSİ",
                                                    fontFamily = BarlowCondensedFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "Şöhretin (%${state.player.fame}) ve ${state.player.followersCount} takipçin gönderinin alacağı beğeni ve yorum sayısını belirler.",
                                                    fontFamily = OutfitFontFamily,
                                                    fontSize = 11.sp,
                                                    color = igGray
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                OutlinedTextField(
                                                    value = newPostCaption,
                                                    onValueChange = { newPostCaption = it },
                                                    placeholder = { Text("Açıklama, @etiket veya #hashtag yaz...", color = igGray) },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = igBlue,
                                                        unfocusedBorderColor = igDarkBorder,
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                // Quick caption templates
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .horizontalScroll(rememberScrollState()),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    listOf(
                                                        "Maç gününe hazırız! 🔥⚽ #${state.currentClub.shortName}",
                                                        "Tesislerde ekstra mesai 💪🎯",
                                                        "Taraftarımızın desteği her şeydir 🙏❤️",
                                                        "Hedef zirve, durmak yok! 🚀"
                                                    ).forEach { tpl ->
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = Color(0xFF262626),
                                                            modifier = Modifier.clickable { newPostCaption = tpl }
                                                        ) {
                                                            Text(
                                                                text = tpl,
                                                                fontSize = 11.sp,
                                                                color = Color.White,
                                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    FLPrimaryButton(
                                                        text = "Mütevazı Paylaş",
                                                        onClick = {
                                                            onPublishPost(
                                                                newPostCaption.ifBlank { "Takım olarak sıkı çalışmaya devam ediyoruz! ⚽💪" },
                                                                "MÜTEVAZI_TAKIMCI"
                                                            )
                                                            newPostCaption = ""
                                                            tabIndex = 0
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        testTag = "insta_publish_modest_btn"
                                                    )
                                                    FLPrimaryButton(
                                                        text = "İddialı Paylaş",
                                                        onClick = {
                                                            onPublishPost(
                                                                newPostCaption.ifBlank { "Sahada sözümüzü söyleyeceğiz! 🔥" },
                                                                "İDDİALI_ÖZGÜVENLİ"
                                                            )
                                                            newPostCaption = ""
                                                            tabIndex = 0
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        testTag = "insta_publish_bold_btn"
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    item {
                                        Text(
                                            text = "GALERİDEN FOTOĞRAF SEÇ & PAYLAŞ",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = igGray
                                        )
                                    }

                                    items(state.galleryPhotos, key = { it.id }) { photo ->
                                        Card(
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                                            border = BorderStroke(1.dp, igDarkBorder),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Image(
                                                    painter = painterResource(id = resolveInstaSceneDrawable(photo.sceneDrawableType)),
                                                    contentDescription = photo.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(64.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(photo.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                    Text(photo.caption, color = igGray, fontSize = 12.sp, maxLines = 2)
                                                }
                                                Button(
                                                    onClick = {
                                                        onShareFromGallery(photo.id, newPostCaption.ifBlank { photo.caption })
                                                        tabIndex = 0
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = igBlue)
                                                ) {
                                                    Text("Paylaş", color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            3 -> {
                                // Etkinlik / Bildirimler (Instagram Activity Heart Tab)
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    item {
                                        Text(
                                            text = "BİLDİRİMLER VE ETKİLEŞİMLER",
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White
                                        )
                                    }
                                    items(notifications, key = { it.id }) { notif ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (notif.targetPostId != null) {
                                                        inspectedSinglePostId = notif.targetPostId
                                                    } else {
                                                        inspectedAccountHandle = notif.actorHandle
                                                    }
                                                }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(CircleShape)
                                                    .background(parseSafeHexColor(notif.actorAvatarHex, Color(0xFF262626))),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = notif.actorName.take(1).uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = buildAnnotatedString {
                                                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                                                            append("${notif.actorName} ")
                                                        }
                                                        withStyle(SpanStyle(color = Color(0xFFE0E0E0))) {
                                                            append(notif.text)
                                                        }
                                                        withStyle(SpanStyle(color = igGray)) {
                                                            append("  ${notif.timeLabel}")
                                                        }
                                                    },
                                                    fontFamily = OutfitFontFamily,
                                                    fontSize = 13.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            if (notif.type == "FOLLOW") {
                                                val following = SocialWorldGenerator.isFollowingAccount(notif.actorHandle, state)
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (following) Color(0xFF262626) else igBlue,
                                                    modifier = Modifier.clickable {
                                                        SocialWorldGenerator.toggleFollowAccount(notif.actorHandle, state)
                                                    }
                                                ) {
                                                    Text(
                                                        text = if (following) "Takiptesin" else "Takip Et",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            else -> {
                                // Instagram Profil Sekmesi (User's own profile)
                                val ownAcc = SocialWorldGenerator.getAccounts(state).first()
                                val ownPosts = when (profileSubTab) {
                                    1 -> livingFeed.filter {
                                        it.content.contains(state.player.lastName, ignoreCase = true) ||
                                            it.content.contains(state.player.firstName, ignoreCase = true)
                                    }
                                    2 -> livingFeed.filter { it.savedByUser }
                                    else -> livingFeed.filter { it.isUserPost }
                                }
                                InstagramAccountProfileView(
                                    account = ownAcc,
                                    state = state,
                                    posts = ownPosts,
                                    isOwnProfile = true,
                                    profileSubTab = profileSubTab,
                                    onSelectSubTab = { profileSubTab = it },
                                    onBack = null,
                                    onOpenPost = { inspectedSinglePostId = it },
                                    onMessageAccount = { isDmInboxOpen = true }
                                )
                            }
                        }
                    }

                    // Instagram Bottom Navigation Bar (Home, Explore, Add+, Activity, Profile)
                    HorizontalDivider(color = igDarkBorder, thickness = 0.6.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            Triple(0, "Ana Sayfa", Icons.Filled.Home),
                            Triple(1, "Keşfet", Icons.Filled.Search),
                            Triple(2, "Paylaş", Icons.Filled.AddCircleOutline),
                            Triple(3, "Etkinlik", Icons.Filled.FavoriteBorder),
                            Triple(4, "Profil", Icons.Filled.Person)
                        ).forEach { (idx, label, icon) ->
                            val active = tabIndex == idx
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (active) Color.White else Color(0xFF8E8E8E),
                                modifier = Modifier
                                    .size(26.dp)
                                    .clickable { tabIndex = idx }
                            )
                        }
                    }
                }
            }
        }

        // ====================================================================
        // SINGLE POST DETAIL OVERLAY (WHEN CLICKED FROM EXPLORE OR PROFILE GRID)
        // ====================================================================
        if (inspectedPost != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF121212))
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { inspectedSinglePostId = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                        }
                        Text(
                            text = "Gönderi Detayı",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            InstaFeedPostCard(
                                post = inspectedPost,
                                state = state,
                                onOpenAuthorProfile = {
                                    inspectedSinglePostId = null
                                    inspectedAccountHandle = inspectedPost.authorHandle
                                },
                                onOpenCommentsSheet = { bottomSheetPostId = inspectedPost.id },
                                onToggleLike = {
                                    SocialWorldGenerator.toggleInstaPostLikeLocal(inspectedPost.id, inspectedPost.likedByUser)
                                    onToggleLike(inspectedPost.id)
                                },
                                onToggleSave = {
                                    SocialWorldGenerator.toggleInstaPostSaveLocal(inspectedPost.id, inspectedPost.savedByUser)
                                    onToggleSave(inspectedPost.id)
                                }
                            )
                        }
                    }
                }
            }
        }

        // ====================================================================
        // INSTAGRAM SLIDE-UP COMMENTS BOTTOM SHEET WITH NESTED REPLIES
        // ====================================================================
        if (sheetPost != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val structuredComments = if (sheetPost.instaComments.isNotEmpty()) {
                sheetPost.instaComments
            } else {
                sheetPost.topFanComments.mapIndexed { idx, raw ->
                    val parts = raw.split(":", limit = 2)
                    val handle = parts.firstOrNull()?.trim()?.removePrefix("@") ?: "taraftar_$idx"
                    val body = parts.getOrNull(1)?.trim() ?: raw
                    InstaCommentItem(
                        id = "${sheetPost.id}_fallback_$idx",
                        authorHandle = "@$handle",
                        authorName = handle,
                        text = body,
                        timeLabel = "${idx + 1}s",
                        likesCount = 12 + idx * 7,
                        isVerified = handle.contains("fabrizio") || handle.contains("433")
                    )
                }
            }
            val replyingToObj = structuredComments.find { it.id == replyingToCommentId }

            ModalBottomSheet(
                onDismissRequest = {
                    bottomSheetPostId = null
                    replyingToCommentId = null
                },
                sheetState = sheetState,
                containerColor = Color(0xFF1C1C1E),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 400.dp, max = 580.dp)
                        .testTag("insta_comments_bottom_sheet")
                ) {
                    Text(
                        text = "Yorumlar (${sheetPost.commentsCount})",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    )
                    HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.7.dp)

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(structuredComments, key = { it.id }) { cmt ->
                            val replies = SocialWorldGenerator.getRepliesForComment(sheetPost.id, cmt, state)
                            val isExpanded = expandedReplyThreads[cmt.id] ?: true

                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(parseSafeHexColor(cmt.avatarHex, Color(0xFF2C2C2E)))
                                            .clickable {
                                                bottomSheetPostId = null
                                                inspectedAccountHandle = cmt.authorHandle
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cmt.authorHandle.removePrefix("@").take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = cmt.authorHandle.removePrefix("@"),
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.White,
                                                modifier = Modifier.clickable {
                                                    bottomSheetPostId = null
                                                    inspectedAccountHandle = cmt.authorHandle
                                                }
                                            )
                                            if (cmt.isVerified) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Filled.Verified,
                                                    contentDescription = null,
                                                    tint = igBlue,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                            val isPlayerCmt = cmt.authorHandle.contains(state.player.firstName.lowercase()) || cmt.id.startsWith("IC_LIV_USR_")
                                            if (isPlayerCmt) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = GoldPrimary.copy(alpha = 0.2f),
                                                    border = BorderStroke(0.6.dp, GoldPrimary)
                                                ) {
                                                    Text(
                                                        text = "Sen · Oyuncu",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GoldPrimary,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = cmt.timeLabel,
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontSize = 11.sp,
                                                color = Color(0xFF8E8E93)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = cmt.text,
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 13.sp,
                                            color = Color(0xFFEAEAEA)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Yanıtla",
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF8E8E93),
                                                modifier = Modifier.clickable {
                                                    replyingToCommentId = cmt.id
                                                    commentDraftText = "${cmt.authorHandle} "
                                                }
                                            )
                                            if (replies.isNotEmpty()) {
                                                Text(
                                                    text = if (isExpanded) "─── Yanıtları gizle (${replies.size})" else "─── ${replies.size} yanıtı gör",
                                                    fontFamily = OutfitFontFamily,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = igBlue,
                                                    modifier = Modifier.clickable {
                                                        expandedReplyThreads[cmt.id] = !isExpanded
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clickable {
                                                SocialWorldGenerator.toggleInstaCommentLikeLocal(cmt.id, cmt.likedByUser)
                                                onToggleLikeComment(sheetPost.id, cmt.id)
                                            }
                                            .padding(start = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (cmt.likedByUser) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                            contentDescription = "Yorumu Beğen",
                                            tint = if (cmt.likedByUser) igRed else Color(0xFF8E8E93),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "${cmt.likesCount}",
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 10.sp,
                                            color = Color(0xFF8E8E93)
                                        )
                                    }
                                }

                                // Nested Replies List
                                if (isExpanded && replies.isNotEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 42.dp, top = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        replies.forEach { rep ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(parseSafeHexColor(rep.avatarHex, Color(0xFF3A3A3C)))
                                                        .clickable {
                                                            bottomSheetPostId = null
                                                            inspectedAccountHandle = rep.authorHandle
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = rep.authorHandle.removePrefix("@").take(1).uppercase(),
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = rep.authorHandle.removePrefix("@"),
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 11.5.sp,
                                                            color = Color.White
                                                        )
                                                        if (rep.isVerified) {
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Icon(
                                                                imageVector = Icons.Filled.Verified,
                                                                contentDescription = null,
                                                                tint = igBlue,
                                                                modifier = Modifier.size(11.dp)
                                                            )
                                                        }
                                                        if (rep.reactionType != null) {
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            val badgeColor = parseSafeHexColor(rep.reactionType.badgeColorHex, Color(0xFF10B981))
                                                            Surface(
                                                                shape = RoundedCornerShape(4.dp),
                                                                color = badgeColor.copy(alpha = 0.2f),
                                                                border = BorderStroke(0.5.dp, badgeColor)
                                                            ) {
                                                                Text(
                                                                    text = rep.reactionType.labelTr,
                                                                    fontSize = 8.5.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = badgeColor,
                                                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                                                )
                                                            }
                                                        }
                                                        val isPlayerRep = rep.authorHandle.contains(state.player.firstName.lowercase()) || rep.id.startsWith("REP_USR_")
                                                        if (isPlayerRep) {
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Surface(
                                                                shape = RoundedCornerShape(4.dp),
                                                                color = GoldPrimary.copy(alpha = 0.2f),
                                                                border = BorderStroke(0.5.dp, GoldPrimary)
                                                            ) {
                                                                Text(
                                                                    text = "Sen",
                                                                    fontSize = 8.5.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = GoldPrimary,
                                                                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 0.5.dp)
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.width(5.dp))
                                                        Text(rep.timeLabel, fontSize = 10.sp, color = Color(0xFF8E8E93))
                                                    }
                                                    Text(
                                                        text = rep.text,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFFD4D4D8)
                                                    )
                                                }
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.clickable {
                                                        SocialWorldGenerator.toggleInstaReplyLikeLocal(rep.id, rep.likedByUser)
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (rep.likedByUser) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                                        contentDescription = null,
                                                        tint = if (rep.likedByUser) igRed else Color(0xFF8E8E93),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Text("${rep.likesCount}", fontSize = 9.sp, color = Color(0xFF8E8E93))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.7.dp)

                    if (replyingToObj != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2C2C2E))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${replyingToObj.authorHandle} kişisine yanıt veriliyor",
                                fontSize = 11.5.sp,
                                color = igBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "İptal ✕",
                                fontSize = 11.sp,
                                color = igGray,
                                modifier = Modifier.clickable { replyingToCommentId = null }
                            )
                        }
                    }

                    // Instagram Quick Emoji Reaction Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("❤️", "🙌", "🔥", "👏", "😢", "😍", "😮", "😂").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 20.sp,
                                modifier = Modifier.clickable {
                                    commentDraftText = if (commentDraftText.isBlank()) emoji else "$commentDraftText $emoji"
                                }
                            )
                        }
                    }

                    // Bottom Comment Composer Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FootballerAvatarCanvas(
                            appearance = state.player.appearance,
                            clubPrimaryHex = state.currentClub.primaryColorHex,
                            clubSecondaryHex = state.currentClub.secondaryColorHex,
                            kitNumber = state.player.kitNumber,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = commentDraftText,
                            onValueChange = { commentDraftText = it },
                            placeholder = {
                                Text(
                                    text = if (replyingToObj != null) "${replyingToObj.authorHandle} yorumuna yanıt yaz..." else "${sheetPost.authorHandle} için yorum ekle...",
                                    color = Color(0xFF8E8E93),
                                    fontSize = 13.sp
                                )
                            },
                            singleLine = true,
                            shape = CircleShape,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("insta_sheet_comment_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF2C2C2E),
                                unfocusedContainerColor = Color(0xFF2C2C2E),
                                focusedBorderColor = igBlue,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Paylaş",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (commentDraftText.isNotBlank()) igBlue else igBlue.copy(alpha = 0.4f),
                            modifier = Modifier
                                .clickable {
                                    if (commentDraftText.isNotBlank()) {
                                        SocialWorldGenerator.addInstaCommentOrReply(
                                            state = state,
                                            postId = sheetPost.id,
                                            text = commentDraftText,
                                            replyToComment = replyingToObj
                                        )
                                        onComment(sheetPost.id, commentDraftText)
                                        commentDraftText = ""
                                        replyingToCommentId = null
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("insta_sheet_comment_send")
                        )
                    }
                }
            }
        }

        // ====================================================================
        // FULL-SCREEN STORY VIEWER OVERLAY (WITH NEXT/PREV & DIRECT REPLY)
        // ====================================================================
        if (activeStory != null) {
            val currentStoryIndex = livingStories.indexOfFirst { it.id == activeStory.id }.coerceAtLeast(0)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.96f))
                    .testTag("insta_story_viewer")
            ) {
                Image(
                    painter = painterResource(id = resolveInstaSceneDrawable(activeStory.sceneType)),
                    contentDescription = activeStory.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.58f
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Segmented Story Progress Bars
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            livingStories.take(8).forEachIndexed { idx, _ ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (idx <= currentStoryIndex) Color.White else Color.White.copy(alpha = 0.35f)
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    val handle = activeStory.authorName
                                    onCloseStory()
                                    inspectedAccountHandle = handle
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(parseSafeHexColor(activeStory.avatarHex, Color(0xFF262626))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = activeStory.authorName.take(2).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = activeStory.authorName,
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 15.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = activeStory.authorRole,
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFFE0E0E0)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (currentStoryIndex < livingStories.lastIndex) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.2f),
                                        modifier = Modifier.clickable {
                                            val next = livingStories[currentStoryIndex + 1]
                                            SocialWorldGenerator.markStoryViewedOrReactedLocal(next.id, false)
                                            onOpenStory(next.id)
                                        }
                                    ) {
                                        Text(
                                            text = "Sonraki ➔",
                                            fontSize = 11.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                IconButton(onClick = onCloseStory) {
                                    Icon(Icons.Filled.Close, contentDescription = "Kapat", tint = Color.White)
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121212).copy(alpha = 0.92f)),
                            border = BorderStroke(1.dp, igDarkBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = activeStory.title,
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = GoldPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = activeStory.caption,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                FLPrimaryButton(
                                    text = if (activeStory.reacted) "🔥 Tepki Gönderildi" else "🔥 Hikayeye Ateş & Alkış Gönder",
                                    onClick = {
                                        SocialWorldGenerator.markStoryViewedOrReactedLocal(activeStory.id, true)
                                        onViewReactStory(activeStory.id, true)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Story Direct Message Reply Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = storyReplyDraft,
                                onValueChange = { storyReplyDraft = it },
                                placeholder = { Text("${activeStory.authorName} hikayesine yanıt gönder...", color = igGray, fontSize = 12.sp) },
                                singleLine = true,
                                shape = CircleShape,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF1C1C1E),
                                    unfocusedContainerColor = Color(0xFF1C1C1E),
                                    focusedBorderColor = igBlue,
                                    unfocusedBorderColor = igDarkBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    val acc = SocialWorldGenerator.resolveAccount(activeStory.authorName, state)
                                    val th = SocialWorldGenerator.getOrCreateDmThreadForAccount(acc, state)
                                    val msg = storyReplyDraft.ifBlank { "🔥👏" }
                                    if (!SocialWorldGenerator.appendLocalDmMessageIfExtra(th.id, state, "[Hikaye Yanıtı] $msg")) {
                                        onSendDmMessage(th.id, "[Hikaye Yanıtı] $msg")
                                    }
                                    storyReplyDraft = ""
                                    onCloseStory()
                                    isDmInboxOpen = true
                                    activeDmThreadId = th.id
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(igBlue)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Yanıtla", tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InstagramAccountProfileView(
    account: SocialAccountProfile,
    state: CareerSaveState,
    posts: List<SocialMediaPost>,
    isOwnProfile: Boolean,
    profileSubTab: Int,
    onSelectSubTab: (Int) -> Unit,
    onBack: (() -> Unit)?,
    onOpenPost: (String) -> Unit,
    onMessageAccount: () -> Unit
) {
    val igBlue = Color(0xFF0095F6)
    val igGray = Color(0xFFA8A8A8)
    val isFollowing = SocialWorldGenerator.isFollowingAccount(account.handle, state)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (onBack != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = account.cleanHandle,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    if (account.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = null,
                            tint = if (account.badgeType == VerifiedBadgeType.GOLD_ORG) GoldPrimary else igBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(78.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFEDA77), Color(0xFFDD2A7B), Color(0xFF8134AF))
                                )
                            )
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isOwnProfile) {
                            FootballerAvatarCanvas(
                                appearance = state.player.appearance,
                                clubPrimaryHex = state.currentClub.primaryColorHex,
                                clubSecondaryHex = state.currentClub.secondaryColorHex,
                                kitNumber = state.player.kitNumber,
                                modifier = Modifier.size(68.dp)
                            )
                        } else {
                            AccountAvatarCircle(acc = account, sizeDp = 68)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InstaProfileStatMetric("${account.postsCount}", "gönderi")
                        InstaProfileStatMetric(account.followersCountText, "takipçi")
                        InstaProfileStatMetric("${account.followingCount}", "takip")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = account.displayName,
                        fontFamily = OutfitFontFamily,
                        fontSize = 15.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (account.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = null,
                            tint = if (account.badgeType == VerifiedBadgeType.GOLD_ORG) GoldPrimary else igBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = account.category.labelTr,
                    fontFamily = OutfitFontFamily,
                    fontSize = 12.sp,
                    color = igBlue
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = account.bio,
                    fontFamily = OutfitFontFamily,
                    fontSize = 12.sp,
                    color = Color(0xFFE5E5E5)
                )
                if (account.website.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "🔗 ${account.website}",
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.5.sp,
                        color = Color(0xFF70C0FF),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = account.mutualFollowersText,
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.sp,
                    color = igGray
                )

                Spacer(modifier = Modifier.height(10.dp))
                if (!isOwnProfile) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { SocialWorldGenerator.toggleFollowAccount(account.handle, state) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) Color(0xFF262626) else igBlue
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isFollowing) "Takiptesin ✓" else "Takip Et",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Button(
                            onClick = onMessageAccount,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262626)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Mesaj Gönder",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Story Highlights Row
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    account.storyHighlights.forEach { (emoji, label) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color(0xFF3A3A3C), CircleShape)
                                    .background(Color(0xFF161618)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = label, fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        if (isOwnProfile) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(
                        "Gönderiler" to 0,
                        "Etiketlenenler" to 1,
                        "Kaydedilenler" to 2
                    ).forEach { (title, idx) ->
                        val active = profileSubTab == idx
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectSubTab(idx) }
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = title,
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                color = if (active) Color.White else igGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.5.dp)
                                    .background(if (active) Color.White else Color(0xFF262626))
                            )
                        }
                    }
                }
            }
        } else {
            item {
                HorizontalDivider(color = Color(0xFF262626))
            }
        }

        if (isOwnProfile && profileSubTab == 0 && state.galleryPhotos.isNotEmpty() && posts.isEmpty()) {
            val gridRows = state.galleryPhotos.chunked(3)
            items(gridRows) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    rowItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(112.dp)
                        ) {
                            Image(
                                painter = painterResource(id = resolveInstaSceneDrawable(item.sceneDrawableType)),
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        } else {
            val gridRows = posts.chunked(3)
            items(gridRows) { rowPosts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    rowPosts.forEach { post ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(112.dp)
                                .clickable { onOpenPost(post.id) }
                        ) {
                            Image(
                                painter = painterResource(id = resolveInstaSceneDrawable(post.imageSceneType)),
                                contentDescription = post.content,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.25f))
                            )
                            Text(
                                text = "❤️ ${SocialWorldGenerator.formatCompactCount(post.likesCount)}",
                                fontSize = 9.5.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(5.dp)
                            )
                        }
                    }
                    repeat(3 - rowPosts.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountAvatarCircle(acc: SocialAccountProfile, sizeDp: Int) {
    if (acc.customLogoRes != null) {
        Image(
            painter = painterResource(id = acc.customLogoRes),
            contentDescription = acc.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(CircleShape)
                .background(parseSafeHexColor(acc.avatarHex, Color(0xFF262626))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = acc.displayName.take(2).uppercase(),
                fontFamily = BarlowCondensedFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (sizeDp * 0.36f).sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun StoryRingAvatarItem(
    story: StoryItem,
    onClick: () -> Unit
) {
    val ringBrush = if (!story.viewed) {
        Brush.linearGradient(
            listOf(
                Color(0xFFFEDA77),
                Color(0xFFF58529),
                Color(0xFFDD2A7B),
                Color(0xFF8134AF),
                Color(0xFF515BD4)
            )
        )
    } else {
        Brush.linearGradient(listOf(Color(0xFF3A3A3C), Color(0xFF3A3A3C)))
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
            .testTag("story_ring_${story.id}")
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(ringBrush)
                .padding(2.5.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .border(2.dp, Color.Black, CircleShape)
                    .background(parseSafeHexColor(story.avatarHex, Color(0xFF262626))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = story.authorName.take(2).uppercase(),
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = story.authorName,
            fontFamily = OutfitFontFamily,
            fontSize = 11.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InstaFeedPostCard(
    post: SocialMediaPost,
    state: CareerSaveState,
    onOpenAuthorProfile: () -> Unit,
    onOpenCommentsSheet: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleSave: () -> Unit
) {
    val account = SocialWorldGenerator.resolveAccount(post.authorHandle, state)
    val isFollowing = SocialWorldGenerator.isFollowingAccount(post.authorHandle, state)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        // Post Header (Instagram rainbow ring avatar + handle + verified + Follow pill + 3 dots)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenAuthorProfile)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFEDA77), Color(0xFFDD2A7B), Color(0xFF8134AF))
                            )
                        )
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E1E)),
                    contentAlignment = Alignment.Center
                ) {
                    AccountAvatarCircle(acc = account, sizeDp = 34)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorHandle.removePrefix("@"),
                            fontFamily = OutfitFontFamily,
                            fontSize = 13.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (post.isVerifiedAuthor) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = if (account.badgeType == VerifiedBadgeType.GOLD_ORG) GoldPrimary else Color(0xFF0095F6),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${post.dateLabel}",
                            fontSize = 11.sp,
                            color = Color(0xFFA8A8A8)
                        )
                    }
                    Text(
                        text = if (post.locationLabel.isNotBlank()) "${post.authorName} • ${post.locationLabel}" else post.authorName,
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFFA8A8A8)
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!post.isUserPost && !isFollowing) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF262626),
                        modifier = Modifier
                            .clickable {
                                SocialWorldGenerator.toggleFollowAccount(post.authorHandle, state)
                            }
                            .padding(end = 6.dp)
                    ) {
                        Text(
                            text = "Takip Et",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Full-bleed Post Photo with Contextual Broadcast Graphic Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(235.dp)
        ) {
            Image(
                painter = painterResource(id = resolveInstaSceneDrawable(post.imageSceneType)),
                contentDescription = post.content,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.15f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.78f)
                            )
                        )
                    )
            )
            // Category / Source Pill on top-left of photo
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.68f),
                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.2f)),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = account.category.labelTr.uppercase(),
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            // Headline / Graphic Overlay on bottom of photo
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = post.content.lineSequence().firstOrNull()?.take(75) ?: post.authorName,
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Action Bar + Likes + Caption + "X yorumun tümünü gör"
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(onClick = onToggleLike)
                            .testTag("insta_like_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (post.likedByUser) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Beğen",
                            tint = if (post.likedByUser) Color(0xFFFF3040) else Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(onClick = onOpenCommentsSheet)
                            .testTag("insta_comment_btn_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Yorumlar",
                            tint = Color.White,
                            modifier = Modifier.size(23.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${post.commentsCount}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Paylaş",
                        tint = Color.White,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable(onClick = onOpenCommentsSheet)
                    )
                }

                Icon(
                    imageVector = if (post.savedByUser) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "Kaydet",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onToggleSave)
                        .testTag("insta_save_${post.id}")
                )
            }

            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = "${post.likesCount} beğenme",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                        append("${post.authorHandle.removePrefix("@")} ")
                    }
                    withStyle(SpanStyle(color = Color(0xFFEAEAEA))) {
                        append(post.content)
                    }
                },
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp
            )

            // Preview top 2 comments directly below caption
            val previewComments = post.instaComments.take(2)
            if (previewComments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(5.dp))
                previewComments.forEach { cmt ->
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                                append("${cmt.authorHandle.removePrefix("@")} ")
                            }
                            withStyle(SpanStyle(color = Color(0xFFD4D4D4))) {
                                append(cmt.text)
                            }
                        },
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable(onClick = onOpenCommentsSheet)
                    )
                }
            }

            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "${post.commentsCount} yorumun tümünü gör",
                fontFamily = OutfitFontFamily,
                fontSize = 12.sp,
                color = Color(0xFFA8A8A8),
                modifier = Modifier
                    .clickable(onClick = onOpenCommentsSheet)
                    .testTag("insta_view_all_comments_${post.id}")
            )
        }
    }
}

@Composable
private fun InstaProfileStatMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 17.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            fontFamily = OutfitFontFamily,
            fontSize = 11.sp,
            color = Color(0xFFA8A8A8)
        )
    }
}

private fun resolveInstaSceneDrawable(sceneType: String): Int {
    return when (sceneType.uppercase()) {
        "TRAINING" -> R.drawable.img_training_ground
        "LOCKER" -> R.drawable.img_locker_room
        "TRANSFER" -> R.drawable.img_transfer_lounge
        else -> R.drawable.img_stadium_hero
    }
}
