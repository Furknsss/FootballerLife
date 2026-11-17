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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
import com.example.model.XPostItem
import com.example.ui.components.FootballerAvatarCanvas
import com.example.ui.components.parseSafeHexColor
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XAppScreen(
    state: CareerSaveState,
    onToggleLike: (String) -> Unit,
    onToggleRepost: (String) -> Unit,
    onPublishXPost: (String) -> Unit,
    onBackHome: () -> Unit
) {
    val xBlue = Color(0xFF1D9BF0)
    val xGray = Color(0xFF71767B)
    val xBorder = Color(0xFF2F3336)
    val xPink = Color(0xFFF91880)
    val xGreen = Color(0xFF00BA7C)

    // 0: Sana Özel, 1: Takip Edilenler, 2: Gündem, 3: Bildirimler
    var tabIndex by rememberSaveable { mutableStateOf(0) }
    var draftText by rememberSaveable { mutableStateOf("") }
    var activeTopicFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // Sub-screens: Tweet Detail & Reply Thread, Inspected X Profile, Quote Tweet Modal
    var selectedThreadPostId by rememberSaveable { mutableStateOf<String?>(null) }
    var replyDraftText by rememberSaveable { mutableStateOf("") }
    var inspectedXHandle by rememberSaveable { mutableStateOf<String?>(null) }
    var quotingPostId by rememberSaveable { mutableStateOf<String?>(null) }
    var quoteDraftText by rememberSaveable { mutableStateOf("") }

    val livingFeed = SocialWorldGenerator.getLivingXFeed(state)
    val trendingTopics = SocialWorldGenerator.getXTrendingTopics(state)
    val notifications = SocialWorldGenerator.getNotifications(state, "X")

    val selectedThreadPost = livingFeed.find { it.id == selectedThreadPostId }
    val quotingPost = livingFeed.find { it.id == quotingPostId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("phone_pitchx_app")
    ) {
        when {
            selectedThreadPost != null -> {
                // ============================================================
                // X POST DETAIL & CONVERSATION REPLY THREAD SCREEN
                // ============================================================
                val replies = SocialWorldGenerator.getXRepliesForPost(selectedThreadPost, state)
                val quotedEmbed = SocialWorldGenerator.getQuotedEmbedForPost(selectedThreadPost.id)
                val authorAcc = SocialWorldGenerator.resolveAccount(selectedThreadPost.handle, state)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { selectedThreadPostId = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Gönderi",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                    HorizontalDivider(color = xBorder, thickness = 0.6.dp)

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val h = selectedThreadPost.handle
                                            selectedThreadPostId = null
                                            inspectedXHandle = h
                                        }
                                ) {
                                    XAvatarBox(acc = authorAcc, sizeDp = 44)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = selectedThreadPost.authorName,
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color.White
                                            )
                                            if (selectedThreadPost.isVerified) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Filled.Verified,
                                                    contentDescription = null,
                                                    tint = if (authorAcc.badgeType == VerifiedBadgeType.GOLD_ORG) GoldPrimary else xBlue,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = selectedThreadPost.handle,
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 13.sp,
                                            color = xGray
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = selectedThreadPost.text,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 16.sp,
                                    color = Color(0xFFE7E9EA)
                                )

                                if (quotedEmbed != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    QuotedTweetCardView(embed = quotedEmbed)
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "${selectedThreadPost.dateLabel} · ${state.formattedClockTime} · ${(selectedThreadPost.likes * 18 / 1000).coerceAtLeast(4)},2B Görüntülenme",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 12.sp,
                                    color = xGray
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = xBorder, thickness = 0.6.dp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                                ) {
                                    Text(
                                        text = "${selectedThreadPost.reposts} Yeniden Gönderi",
                                        fontSize = 12.5.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${replies.size} Yanıt",
                                        fontSize = 12.5.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${selectedThreadPost.likes} Beğeni",
                                        fontSize = 12.5.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                HorizontalDivider(color = xBorder, thickness = 0.6.dp)
                            }
                        }

                        items(replies, key = { it.id }) { rep ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(parseSafeHexColor(rep.avatarHex, Color(0xFF16181C)))
                                            .clickable {
                                                selectedThreadPostId = null
                                                inspectedXHandle = rep.handle
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = rep.authorName.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = rep.authorName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                color = Color.White,
                                                modifier = Modifier.clickable {
                                                    selectedThreadPostId = null
                                                    inspectedXHandle = rep.handle
                                                }
                                            )
                                            if (rep.isVerified) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(
                                                    imageVector = Icons.Filled.Verified,
                                                    contentDescription = null,
                                                    tint = if (rep.badgeType == VerifiedBadgeType.GOLD_ORG) GoldPrimary else xBlue,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            if (rep.reactionType != null) {
                                                Spacer(modifier = Modifier.width(5.dp))
                                                val badgeColor = parseSafeHexColor(rep.reactionType.badgeColorHex, Color(0xFF10B981))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = badgeColor.copy(alpha = 0.2f),
                                                    border = BorderStroke(0.5.dp, badgeColor)
                                                ) {
                                                    Text(
                                                        text = rep.reactionType.labelTr,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = badgeColor,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            val isUserReply = rep.handle.contains(state.player.firstName.lowercase()) || rep.id.startsWith("XREP_USR_")
                                            if (isUserReply) {
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = GoldPrimary.copy(alpha = 0.2f),
                                                    border = BorderStroke(0.5.dp, GoldPrimary)
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
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "${rep.handle} · ${rep.timeLabel}",
                                                fontSize = 12.sp,
                                                color = xGray,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = "${selectedThreadPost.handle} adlı kişiye yanıt olarak",
                                            fontSize = 11.sp,
                                            color = xBlue
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = rep.text,
                                            fontSize = 13.5.sp,
                                            color = Color(0xFFE7E9EA)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.clickable {
                                                    SocialWorldGenerator.toggleXReplyLikeLocal(rep.id, rep.likedByUser)
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (rep.likedByUser) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                                    contentDescription = null,
                                                    tint = if (rep.likedByUser) xPink else xGray,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${rep.likes}",
                                                    fontSize = 11.sp,
                                                    color = if (rep.likedByUser) xPink else xGray
                                                )
                                            }
                                        }
                                    }
                                }
                                HorizontalDivider(color = xBorder, thickness = 0.5.dp)
                            }
                        }
                    }

                    // Bottom X Reply Composer Bar
                    HorizontalDivider(color = xBorder, thickness = 0.6.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replyDraftText,
                            onValueChange = { replyDraftText = it },
                            placeholder = { Text("Yanıtını gönder...", color = xGray, fontSize = 13.sp) },
                            singleLine = true,
                            shape = CircleShape,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF16181C),
                                unfocusedContainerColor = Color(0xFF16181C),
                                focusedBorderColor = xBlue,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (replyDraftText.isNotBlank()) {
                                    SocialWorldGenerator.addXReply(state, selectedThreadPost, replyDraftText)
                                    replyDraftText = ""
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = xBlue)
                        ) {
                            Text("Yanıtla", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            inspectedXHandle != null -> {
                // ============================================================
                // X ACCOUNT PROFILE PAGE (BANNER + BIO + FOLLOW + TWEETS)
                // ============================================================
                val acc = SocialWorldGenerator.resolveAccount(inspectedXHandle!!, state)
                val isFollowing = SocialWorldGenerator.isFollowingAccount(acc.handle, state)
                val accTweets = livingFeed.filter {
                    it.handle.removePrefix("@").equals(acc.cleanHandle, ignoreCase = true) ||
                        it.authorName.equals(acc.displayName, ignoreCase = true)
                }.ifEmpty { livingFeed.take(5) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { inspectedXHandle = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = acc.displayName,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                if (acc.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = null,
                                        tint = if (acc.badgeType == VerifiedBadgeType.GOLD_ORG) GoldPrimary else xBlue,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Text("${acc.postsCount} gönderi", fontSize = 11.sp, color = xGray)
                        }
                    }

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            // Profile Banner + Avatar + Follow Button
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(84.dp)
                                        .background(parseSafeHexColor(acc.bannerHex, Color(0xFF1D9BF0)))
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 52.dp, start = 14.dp, end = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(66.dp)
                                            .clip(CircleShape)
                                            .border(3.dp, Color.Black, CircleShape)
                                    ) {
                                        XAvatarBox(acc = acc, sizeDp = 66)
                                    }
                                    if (acc.id != "ACC_USER") {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isFollowing) Color.Black else Color.White,
                                            border = BorderStroke(1.dp, if (isFollowing) xBorder else Color.White),
                                            modifier = Modifier.clickable {
                                                SocialWorldGenerator.toggleFollowAccount(acc.handle, state)
                                            }
                                        ) {
                                            Text(
                                                text = if (isFollowing) "Takip Ediliyor" else "Takip Et",
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isFollowing) Color.White else Color.Black,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = acc.displayName,
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                    if (acc.isVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Filled.Verified,
                                            contentDescription = null,
                                            tint = if (acc.badgeType == VerifiedBadgeType.GOLD_ORG) GoldPrimary else xBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(text = acc.handle, fontSize = 13.sp, color = xGray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = acc.bio, fontSize = 13.sp, color = Color(0xFFE7E9EA))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "📍 ${acc.location}  ·  ${acc.category.labelTr}",
                                    fontSize = 12.sp,
                                    color = xGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text(
                                        text = "${acc.followingCount} Takip edilen",
                                        fontSize = 12.5.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${acc.followersCountText} Takipçi",
                                        fontSize = 12.5.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            HorizontalDivider(color = xBorder, thickness = 0.7.dp)
                        }

                        items(accTweets, key = { it.id }) { xp ->
                            XTweetCardItem(
                                xp = xp,
                                state = state,
                                onOpenThread = { selectedThreadPostId = xp.id },
                                onOpenProfile = { inspectedXHandle = xp.handle },
                                onToggleLike = {
                                    SocialWorldGenerator.toggleXPostLikeLocal(xp.id, xp.likedByUser)
                                    onToggleLike(xp.id)
                                },
                                onToggleRepost = {
                                    SocialWorldGenerator.toggleXPostRepostLocal(xp.id, xp.repostedByUser)
                                    onToggleRepost(xp.id)
                                },
                                onQuoteTweet = { quotingPostId = xp.id }
                            )
                        }
                    }
                }
            }

            else -> {
                // ============================================================
                // MAIN X TIMELINE (SANA ÖZEL | TAKİP EDİLENLER | GÜNDEM | BİLDİRİMLER)
                // ============================================================
                Column(modifier = Modifier.fillMaxSize()) {
                    // Authentic X Pitch-Black Top Bar with Centered X Logo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
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
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_x),
                                contentDescription = "X",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(1.24f)
                            )
                        }
                        val fameTier = SocialWorldGenerator.calculatePlayerFameTier(state)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF16181C),
                            border = BorderStroke(0.6.dp, Color(0xFF2F3336))
                        ) {
                            Text(
                                text = "${fameTier.badgeSymbol} ${fameTier.titleTr}",
                                fontFamily = OutfitFontFamily,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE7E9EA),
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Box(
                            modifier = Modifier.clickable {
                                inspectedXHandle = "@${state.player.firstName.lowercase()}${state.player.kitNumber}"
                            }
                        ) {
                            FootballerAvatarCanvas(
                                appearance = state.player.appearance,
                                clubPrimaryHex = state.currentClub.primaryColorHex,
                                clubSecondaryHex = state.currentClub.secondaryColorHex,
                                kitNumber = state.player.kitNumber,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    // X Underline Tabs ("Sana Özel" | "Takip Edilenler" | "Gündem" | "Bildirimler")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black)
                    ) {
                        listOf("Sana Özel", "Takip Edilenler", "Gündem", "Bildirimler").forEachIndexed { idx, title ->
                            val selected = tabIndex == idx
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { tabIndex = idx }
                                    .padding(top = 10.dp)
                            ) {
                                Text(
                                    text = title,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) Color.White else xGray,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .width(52.dp)
                                        .height(3.dp)
                                        .clip(CircleShape)
                                        .background(if (selected) xBlue else Color.Transparent)
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = xBorder, thickness = 0.7.dp)

                    if (activeTopicFilter != null && (tabIndex == 0 || tabIndex == 1)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF16181C))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Gündem Filtresi: $activeTopicFilter",
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = xBlue
                            )
                            Text(
                                text = "Filtreyi Temizle ✕",
                                fontSize = 11.sp,
                                color = Color.White,
                                modifier = Modifier.clickable { activeTopicFilter = null }
                            )
                        }
                    }

                    when (tabIndex) {
                        3 -> {
                            // Bildirimler & Bahsedenler Sekmesi
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(notifications, key = { it.id }) { notif ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (notif.targetPostId != null) {
                                                    selectedThreadPostId = notif.targetPostId
                                                } else {
                                                    inspectedXHandle = notif.actorHandle
                                                }
                                            }
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(parseSafeHexColor(notif.actorAvatarHex, Color(0xFF16181C))),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(notif.actorName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = notif.actorName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = Color.White
                                                    )
                                                    if (notif.actorVerified) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Icon(Icons.Filled.Verified, contentDescription = null, tint = xBlue, modifier = Modifier.size(14.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "${notif.actorHandle} · ${notif.timeLabel}",
                                                        fontSize = 12.sp,
                                                        color = xGray
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(3.dp))
                                                Text(
                                                    text = notif.text,
                                                    fontSize = 13.5.sp,
                                                    color = Color(0xFFE7E9EA)
                                                )
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = xBorder, thickness = 0.6.dp)
                                }
                            }
                        }

                        else -> {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    // X Post Composer Box ("Neler oluyor?!")
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.Black)
                                            .padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.Top) {
                                            FootballerAvatarCanvas(
                                                appearance = state.player.appearance,
                                                clubPrimaryHex = state.currentClub.primaryColorHex,
                                                clubSecondaryHex = state.currentClub.secondaryColorHex,
                                                kitNumber = state.player.kitNumber,
                                                modifier = Modifier.size(38.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                OutlinedTextField(
                                                    value = draftText,
                                                    onValueChange = { draftText = it },
                                                    placeholder = { Text("Neler oluyor?!", color = xGray) },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("pitchx_compose_input"),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = xBlue,
                                                        unfocusedBorderColor = xBorder,
                                                        focusedTextColor = Color.White,
                                                        unfocusedTextColor = Color.White
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                // Quick Hashtag / Statement Pills
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .horizontalScroll(rememberScrollState()),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    listOf(
                                                        "Odak noktamız 3 puan! ⚽🔥 #${state.currentClub.shortName}",
                                                        "Taraftarımıza sonsuz teşekkürler 🙏❤️",
                                                        "Çalışmaya ve gelişmeye devam 💪🎯"
                                                    ).forEach { chip ->
                                                        Surface(
                                                            shape = CircleShape,
                                                            color = Color(0xFF16181C),
                                                            border = BorderStroke(0.6.dp, xBorder),
                                                            modifier = Modifier.clickable { draftText = chip }
                                                        ) {
                                                            Text(
                                                                text = chip,
                                                                fontSize = 11.sp,
                                                                color = xGray,
                                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            if (draftText.isNotBlank()) {
                                                                onPublishXPost(draftText)
                                                                draftText = ""
                                                            }
                                                        },
                                                        shape = CircleShape,
                                                        colors = ButtonDefaults.buttonColors(containerColor = xBlue),
                                                        modifier = Modifier.testTag("pitchx_post_btn")
                                                    ) {
                                                        Text("Gönder", color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = xBorder, thickness = 0.7.dp)
                                }

                                if (tabIndex == 2) {
                                    // Gündem (Trending Topics & Search)
                                    item {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = searchQuery,
                                                onValueChange = { searchQuery = it },
                                                placeholder = { Text("X'te gündem, gazeteci veya etiket ara...", color = xGray, fontSize = 13.sp) },
                                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = xGray) },
                                                singleLine = true,
                                                shape = CircleShape,
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = Color(0xFF16181C),
                                                    unfocusedContainerColor = Color(0xFF16181C),
                                                    focusedBorderColor = xBlue,
                                                    unfocusedBorderColor = Color.Transparent,
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                )
                                            )
                                            Text(
                                                text = "TÜRKİYE VE AVRUPA GÜNDEMİ (DOKUN VE FİLTRELE)",
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = xBlue
                                            )
                                            trendingTopics.forEach { trend ->
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            activeTopicFilter = trend.filterKeyword
                                                            tabIndex = 0
                                                        }
                                                        .padding(vertical = 4.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = "${trend.rank} · ${trend.categoryLabel}",
                                                            fontSize = 11.sp,
                                                            color = xGray
                                                        )
                                                        Text(
                                                            text = trend.postsCountLabel,
                                                            fontSize = 11.sp,
                                                            color = xGray
                                                        )
                                                    }
                                                    Text(
                                                        text = trend.topicTitle,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp
                                                    )
                                                    Text(
                                                        text = trend.liveContextSubtitle,
                                                        color = xGray,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }
                                        HorizontalDivider(color = xBorder, thickness = 0.7.dp)
                                    }
                                }

                                val posts = livingFeed.filter { xp ->
                                    val matchesTab = when (tabIndex) {
                                        1 -> SocialWorldGenerator.isFollowingAccount(xp.handle, state) ||
                                            xp.category == "GAZETECİ" || xp.category == "YORUMCU"
                                        else -> true
                                    }
                                    val matchesTopic = activeTopicFilter == null ||
                                        xp.text.contains(activeTopicFilter!!, ignoreCase = true) ||
                                        xp.authorName.contains(activeTopicFilter!!, ignoreCase = true) ||
                                        xp.handle.contains(activeTopicFilter!!, ignoreCase = true)
                                    val matchesSearch = tabIndex != 2 || searchQuery.isBlank() ||
                                        xp.text.contains(searchQuery, ignoreCase = true) ||
                                        xp.authorName.contains(searchQuery, ignoreCase = true)
                                    matchesTab && matchesTopic && matchesSearch
                                }

                                // Pull-to-Refresh Top Banner
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFF16181C),
                                        border = BorderStroke(0.6.dp, xBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                            .clickable {
                                                SocialWorldGenerator.refreshXFeed(state)
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
                                                tint = xBlue,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "⚡ Yeni Tweetleri Yükle (Akışı Yenile)",
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFFE7E9EA)
                                            )
                                        }
                                    }
                                }

                                items(posts, key = { it.id }) { xp ->
                                    XTweetCardItem(
                                        xp = xp,
                                        state = state,
                                        onOpenThread = { selectedThreadPostId = xp.id },
                                        onOpenProfile = { inspectedXHandle = xp.handle },
                                        onToggleLike = {
                                            SocialWorldGenerator.toggleXPostLikeLocal(xp.id, xp.likedByUser)
                                            onToggleLike(xp.id)
                                        },
                                        onToggleRepost = {
                                            SocialWorldGenerator.toggleXPostRepostLocal(xp.id, xp.repostedByUser)
                                            onToggleRepost(xp.id)
                                        },
                                        onQuoteTweet = { quotingPostId = xp.id }
                                    )
                                }

                                // Infinite Scroll Loader Card
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF16181C),
                                        border = BorderStroke(0.6.dp, xBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                            .clickable {
                                                SocialWorldGenerator.loadMoreXPosts(state)
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Repeat,
                                                contentDescription = null,
                                                tint = xBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Daha Fazla Tweet Yükle (Sonsuz Akış) ⚡",
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
                    }
                }
            }
        }

        // ====================================================================
        // QUOTE TWEET ("GÖNDERİYİ ALINTILA") MODAL BOTTOM SHEET
        // ====================================================================
        if (quotingPost != null) {
            ModalBottomSheet(
                onDismissRequest = { quotingPostId = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color(0xFF16181C),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gönderiyi Alıntıla (Quote)",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        IconButton(onClick = { quotingPostId = null }) {
                            Icon(Icons.Filled.Close, contentDescription = "Kapat", tint = Color.White)
                        }
                    }

                    OutlinedTextField(
                        value = quoteDraftText,
                        onValueChange = { quoteDraftText = it },
                        placeholder = { Text("Yorumunu ekle...", color = xGray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = xBlue,
                            unfocusedBorderColor = xBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Black,
                        border = BorderStroke(1.dp, xBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "${quotingPost.authorName} (${quotingPost.handle}) · ${quotingPost.dateLabel}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = quotingPost.text,
                                fontSize = 12.5.sp,
                                color = Color(0xFFE7E9EA),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Button(
                        onClick = {
                            SocialWorldGenerator.publishQuoteXPost(
                                state = state,
                                quotedPost = quotingPost,
                                commentary = quoteDraftText,
                                onDelegatePublish = onPublishXPost
                            )
                            quoteDraftText = ""
                            quotingPostId = null
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = xBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Alıntılayarak Paylaş", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun XTweetCardItem(
    xp: XPostItem,
    state: CareerSaveState,
    onOpenThread: () -> Unit,
    onOpenProfile: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleRepost: () -> Unit,
    onQuoteTweet: () -> Unit
) {
    val xBlue = Color(0xFF1D9BF0)
    val xGray = Color(0xFF71767B)
    val xBorder = Color(0xFF2F3336)
    val account = SocialWorldGenerator.resolveAccount(xp.handle, state)
    val quotedEmbed = SocialWorldGenerator.getQuotedEmbedForPost(xp.id)
    val repliesCount = SocialWorldGenerator.getXRepliesForPost(xp, state).size
    val bookmarked = SocialWorldGenerator.isXPostBookmarked(xp.id)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .clickable(onClick = onOpenThread)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(modifier = Modifier.clickable(onClick = onOpenProfile)) {
                XAvatarBox(acc = account, sizeDp = 40)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onOpenProfile)
                ) {
                    Text(
                        text = xp.authorName,
                        fontFamily = OutfitFontFamily,
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (xp.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = null,
                            tint = if (account.badgeType == VerifiedBadgeType.GOLD_ORG) GoldPrimary else xBlue,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${xp.handle} · ${xp.dateLabel}",
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        color = xGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = xp.text,
                    fontFamily = OutfitFontFamily,
                    fontSize = 14.sp,
                    color = Color(0xFFE7E9EA)
                )

                if (quotedEmbed != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    QuotedTweetCardView(embed = quotedEmbed)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reply button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onOpenThread)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Yanıtla",
                            tint = xGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("$repliesCount", fontSize = 12.sp, color = xGray)
                    }
                    // Repost button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onToggleRepost)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Repeat,
                            contentDescription = "Repost",
                            tint = if (xp.repostedByUser) Color(0xFF00BA7C) else xGray,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${xp.reposts}",
                            fontSize = 12.sp,
                            color = if (xp.repostedByUser) Color(0xFF00BA7C) else xGray
                        )
                    }
                    // Quote Tweet pill
                    Text(
                        text = "❝ Alıntıla",
                        fontSize = 11.5.sp,
                        color = xGray,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClick = onQuoteTweet)
                    )
                    // Like button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onToggleLike)
                    ) {
                        Icon(
                            imageVector = if (xp.likedByUser) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Beğen",
                            tint = if (xp.likedByUser) Color(0xFFF91880) else xGray,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${xp.likes}",
                            fontSize = 12.sp,
                            color = if (xp.likedByUser) Color(0xFFF91880) else xGray
                        )
                    }
                    // Bookmark & Views
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Kaydet",
                            tint = if (bookmarked) xBlue else xGray,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { SocialWorldGenerator.toggleXPostBookmark(xp.id) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${(xp.likes * 18 / 1000).coerceAtLeast(4)}B",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.sp,
                            color = xGray
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = xBorder, thickness = 0.6.dp)
    }
}

@Composable
private fun QuotedTweetCardView(embed: com.example.engine.social.XQuotedPostEmbed) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0B0D10),
        border = BorderStroke(1.dp, Color(0xFF2F3336)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(parseSafeHexColor(embed.avatarHex, Color(0xFF1D9BF0))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(embed.authorName.take(1).uppercase(), fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = embed.authorName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
                if (embed.isVerified) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF1D9BF0), modifier = Modifier.size(12.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${embed.handle} · ${embed.timeLabel}",
                    fontSize = 11.sp,
                    color = Color(0xFF71767B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = embed.text,
                fontSize = 12.5.sp,
                color = Color(0xFFD6D9DB)
            )
        }
    }
}

@Composable
private fun XAvatarBox(acc: SocialAccountProfile, sizeDp: Int) {
    if (acc.customLogoRes != null) {
        Image(
            painter = painterResource(id = acc.customLogoRes),
            contentDescription = acc.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(CircleShape)
                .border(1.dp, Color(0xFF2F3336), CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(CircleShape)
                .background(parseSafeHexColor(acc.avatarHex, Color(0xFF16181C)))
                .border(1.dp, Color(0xFF2F3336), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = acc.displayName.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = (sizeDp * 0.4f).sp
            )
        }
    }
}
