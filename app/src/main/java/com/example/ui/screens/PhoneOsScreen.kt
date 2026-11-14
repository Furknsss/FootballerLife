package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.data.UserSettings
import com.example.engine.CareerEngine
import com.example.engine.WorldDataCatalog
import com.example.model.CareerSaveState
import com.example.model.GalleryPhotoItem
import com.example.model.GraphicsQuality
import com.example.model.InstaCommentItem
import com.example.model.InstaDmThread
import com.example.model.LifestylePurchaseItem
import com.example.model.NewsEvent
import com.example.model.PhoneCallRecord
import com.example.model.SocialMediaPost
import com.example.model.StoryItem
import com.example.ui.components.FLFormatters
import com.example.ui.components.FLNotificationBadge
import com.example.ui.components.FLPrimaryButton
import com.example.ui.components.FLSegmentedTabs
import com.example.ui.components.FLStatBar
import com.example.ui.components.FootballerAvatarCanvas
import com.example.ui.components.parseSafeHexColor
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.GoldPrimary
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

enum class PhoneAppId(
    val titleTr: String,
    val icon: ImageVector,
    val primaryHex: String,
    val secondaryHex: String,
    val customLogoRes: Int? = null
) {
    MESSAGES("WhatsApp", Icons.AutoMirrored.Filled.Chat, "#25D366", "#128C7E", R.drawable.logo_whatsapp),
    INSTAPITCH("Instagram", Icons.Filled.PhotoCamera, "#E1306C", "#833AB4", R.drawable.logo_instagram),
    PITCHX("X", Icons.Filled.Public, "#000000", "#16181C", R.drawable.logo_x),
    NEWS("Haberler", Icons.Filled.Newspaper, "#2563EB", "#1E3A8A"),
    BANK("Banka", Icons.Filled.AccountBalance, "#10B981", "#064E3B"),
    GALLERY("Galeri", Icons.Filled.PhotoLibrary, "#8B5CF6", "#4C1D95"),
    CAMERA("Kamera", Icons.Filled.CameraAlt, "#334155", "#0F172A"),
    CONTACTS("Kişiler", Icons.Filled.Contacts, "#0EA5E9", "#0C4A6E"),
    CALLS("Aramalar", Icons.Filled.Call, "#22C55E", "#14532D"),
    SHOP("Alışveriş", Icons.Filled.ShoppingBag, "#F59E0B", "#78350F"),
    SETTINGS("Ayarlar", Icons.Filled.Settings, "#475569", "#1E293B")
}

@Composable
fun PhoneOsScreen(
    state: CareerSaveState,
    userSettings: UserSettings,
    onSendMessageToNpc: (String, String) -> Unit,
    onPublishSocialOrPress: (String, String) -> Unit,
    onToggleLikeInsta: (String) -> Unit,
    onToggleSaveInsta: (String) -> Unit,
    onCommentInsta: (String, String) -> Unit,
    onToggleLikeInstaComment: (String, String) -> Unit = { _, _ -> },
    onAcceptInstaDmRequest: (String) -> Unit = {},
    onDeleteInstaDmRequest: (String) -> Unit = {},
    onSendInstaDmMessage: (String, String) -> Unit = { _, _ -> },
    onViewReactStory: (String, Boolean) -> Unit,
    onSharePhotoToInsta: (String, String) -> Unit,
    onCaptureCameraPhoto: (String, String, String, String) -> Unit,
    onToggleLikeXPost: (String) -> Unit,
    onToggleRepostXPost: (String) -> Unit,
    onPublishXPost: (String) -> Unit,
    onCompleteCall: (String, String) -> Unit,
    onInitiateCallWithNpc: (String) -> Unit,
    onPurchaseItem: (LifestylePurchaseItem) -> Unit,
    onChangeWallpaper: (String) -> Unit,
    onUpdateAmbience: (Boolean) -> Unit,
    onUpdateSfx: (Boolean) -> Unit,
    onUpdateHaptics: (Boolean) -> Unit,
    onUpdateGraphics: (GraphicsQuality) -> Unit,
    onBackToHub: () -> Unit
) {
    var currentApp by rememberSaveable { mutableStateOf<PhoneAppId?>(null) }
    var selectedChatNpcId by rememberSaveable { mutableStateOf<String?>(null) }
    var activeStoryId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedNewsArticleId by rememberSaveable { mutableStateOf<String?>(null) }
    var activeCallRecordId by rememberSaveable { mutableStateOf<String?>(null) }

    // Hierarchical Back Navigation: Sub-screen -> App Home -> Phone Home -> Hub
    BackHandler {
        when {
            activeStoryId != null -> activeStoryId = null
            selectedChatNpcId != null -> selectedChatNpcId = null
            selectedNewsArticleId != null -> selectedNewsArticleId = null
            activeCallRecordId != null -> activeCallRecordId = null
            currentApp != null -> currentApp = null
            else -> onBackToHub()
        }
    }

    val clubPrimaryColor = parseSafeHexColor(state.currentClub.primaryColorHex, AccentPrimary)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .testTag("phone_os_root")
    ) {
        // Dynamic Wallpaper Layer when on Home Screen
        if (currentApp == null) {
            PhoneWallpaperBackground(
                wallpaperId = state.phoneWallpaperId,
                state = state,
                clubColor = clubPrimaryColor
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Smartphone Status Bar (Synchronized Clock, Date, 5G, Battery)
            SmartphoneStatusBar(
                clockTime = state.formattedClockTime,
                dateShort = CareerEngine.formatShortDate(state.currentDayIndex),
                onHomeClick = {
                    activeStoryId = null
                    selectedChatNpcId = null
                    selectedNewsArticleId = null
                    activeCallRecordId = null
                    currentApp = null
                },
                isInsideApp = currentApp != null
            )

            // Main Content Area
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(targetState = currentApp, label = "phone_app_switch") { app ->
                    when (app) {
                        null -> PhoneHomeScreenContent(
                            state = state,
                            onOpenApp = { targetApp -> currentApp = targetApp },
                            onQuickCallOpen = { callId ->
                                currentApp = PhoneAppId.CALLS
                                activeCallRecordId = callId
                            }
                        )
                        PhoneAppId.MESSAGES -> PhoneMessagesApp(
                            state = state,
                            selectedNpcId = selectedChatNpcId,
                            onSelectNpc = { selectedChatNpcId = it },
                            onSendMessage = onSendMessageToNpc,
                            onStartCall = { npcId ->
                                onInitiateCallWithNpc(npcId)
                                currentApp = PhoneAppId.CALLS
                            },
                            onBackHome = { currentApp = null }
                        )
                        PhoneAppId.INSTAPITCH -> InstaPitchApp(
                            state = state,
                            activeStoryId = activeStoryId,
                            onOpenStory = { activeStoryId = it },
                            onCloseStory = { activeStoryId = null },
                            onViewReactStory = onViewReactStory,
                            onToggleLike = onToggleLikeInsta,
                            onToggleSave = onToggleSaveInsta,
                            onComment = onCommentInsta,
                            onToggleLikeComment = onToggleLikeInstaComment,
                            onAcceptDmRequest = onAcceptInstaDmRequest,
                            onDeleteDmRequest = onDeleteInstaDmRequest,
                            onSendDmMessage = onSendInstaDmMessage,
                            onShareFromGallery = onSharePhotoToInsta,
                            onPublishPost = onPublishSocialOrPress,
                            onBackHome = { currentApp = null }
                        )
                        PhoneAppId.PITCHX -> PitchXApp(
                            state = state,
                            onToggleLike = onToggleLikeXPost,
                            onToggleRepost = onToggleRepostXPost,
                            onPublishXPost = onPublishXPost,
                            onBackHome = { currentApp = null }
                        )
                        PhoneAppId.NEWS -> FootballNewsApp(
                            state = state,
                            selectedArticleId = selectedNewsArticleId,
                            onSelectArticle = { selectedNewsArticleId = it },
                            onBackHome = { currentApp = null }
                        )
                        PhoneAppId.BANK -> BankApp(
                            state = state,
                            onBackHome = { currentApp = null }
                        )
                        PhoneAppId.GALLERY -> GalleryApp(
                            state = state,
                            onSharePhotoToInsta = { photoId, caption ->
                                onSharePhotoToInsta(photoId, caption)
                                currentApp = PhoneAppId.INSTAPITCH
                            },
                            onOpenCamera = { currentApp = PhoneAppId.CAMERA },
                            onBackHome = { currentApp = null }
                        )
                        PhoneAppId.CAMERA -> CameraApp(
                            state = state,
                            onCapturePhoto = { scene, album, title, caption ->
                                onCaptureCameraPhoto(scene, album, title, caption)
                                currentApp = PhoneAppId.GALLERY
                            },
                            onBackHome = { currentApp = null }
                        )
                        PhoneAppId.CONTACTS -> ContactsApp(
                            state = state,
                            onMessageNpc = { npcId ->
                                selectedChatNpcId = npcId
                                currentApp = PhoneAppId.MESSAGES
                            },
                            onCallNpc = { npcId ->
                                onInitiateCallWithNpc(npcId)
                                currentApp = PhoneAppId.CALLS
                            },
                            onBackHome = { currentApp = null }
                        )
                        PhoneAppId.CALLS -> CallsApp(
                            state = state,
                            activeCallId = activeCallRecordId,
                            onSelectCall = { activeCallRecordId = it },
                            onCompleteCall = { callId, option ->
                                onCompleteCall(callId, option)
                                activeCallRecordId = null
                            },
                            onInitiateCallWithNpc = onInitiateCallWithNpc,
                            onBackHome = { currentApp = null }
                        )
                        PhoneAppId.SHOP -> ShoppingStoreApp(
                            state = state,
                            onPurchaseItem = onPurchaseItem,
                            onBackHome = { currentApp = null }
                        )
                        PhoneAppId.SETTINGS -> PhoneSettingsApp(
                            state = state,
                            userSettings = userSettings,
                            onChangeWallpaper = onChangeWallpaper,
                            onUpdateAmbience = onUpdateAmbience,
                            onUpdateSfx = onUpdateSfx,
                            onUpdateHaptics = onUpdateHaptics,
                            onUpdateGraphics = onUpdateGraphics,
                            onBackHome = { currentApp = null }
                        )
                    }
                }
            }

            // Bottom Smartphone Home Indicator Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundPrimary.copy(alpha = 0.88f))
                    .clickable {
                        activeStoryId = null
                        selectedChatNpcId = null
                        selectedNewsArticleId = null
                        activeCallRecordId = null
                        currentApp = null
                    }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(128.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.55f))
                )
            }
        }
    }
}

@Composable
private fun PhoneWallpaperBackground(
    wallpaperId: String,
    state: CareerSaveState,
    clubColor: Color
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (wallpaperId) {
            "STADIUM_NIGHT" -> {
                Image(
                    painter = painterResource(id = R.drawable.img_stadium_hero),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.35f
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    BackgroundPrimary.copy(alpha = 0.65f),
                                    BackgroundSecondary.copy(alpha = 0.82f),
                                    BackgroundPrimary.copy(alpha = 0.96f)
                                )
                            )
                        )
                )
            }
            "CLUB_THEME" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    clubColor.copy(alpha = 0.45f),
                                    BackgroundSecondary,
                                    BackgroundPrimary
                                )
                            )
                        )
                )
            }
            "PLAYER_PORTRAIT" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    AccentPrimary.copy(alpha = 0.25f),
                                    BackgroundSecondary,
                                    BackgroundPrimary
                                )
                            )
                        )
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF0B2219),
                                    Color(0xFF07120E),
                                    Color(0xFF040B08)
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun SmartphoneStatusBar(
    clockTime: String,
    dateShort: String,
    onHomeClick: () -> Unit,
    isInsideApp: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundPrimary.copy(alpha = 0.86f))
            .padding(horizontal = 16.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = clockTime,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 13.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "• $dateShort",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        if (isInsideApp) {
            Surface(
                shape = CircleShape,
                color = SurfaceElevated,
                border = BorderStroke(1.dp, StrokePremiumStrong),
                modifier = Modifier
                    .clickable(onClick = onHomeClick)
                    .testTag("phone_home_pill_btn")
            ) {
                Text(
                    text = "Ana Ekran",
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.sp,
                    color = AccentBright,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        } else {
            // Dynamic Island Camera Pill
            Box(
                modifier = Modifier
                    .width(74.dp)
                    .height(16.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF020604))
                    .border(0.8.dp, Color(0x28FFFFFF), CircleShape)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "5G",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 11.sp,
                color = AccentBright,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(5.dp),
                color = AccentPrimary.copy(alpha = 0.22f),
                border = BorderStroke(1.dp, AccentBright.copy(alpha = 0.55f))
            ) {
                Text(
                    text = "%94",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun PhoneHomeScreenContent(
    state: CareerSaveState,
    onOpenApp: (PhoneAppId) -> Unit,
    onQuickCallOpen: (String) -> Unit
) {
    val nextMatch = state.nextUnplayedUserFixture
    val pendingCall = state.callRecords.firstOrNull { !it.completed }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Top Clock & Weather/Match Frosted Glass Smart Widget
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePrimary.copy(alpha = 0.90f)),
                border = BorderStroke(1.dp, StrokePremiumStrong),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, Color.White.copy(alpha = 0.35f), Color.Transparent)
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = state.formattedClockTime,
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 32.sp,
                                    color = TextPrimary
                                )
                                Surface(
                                    color = BackgroundPrimary.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, StrokePremium)
                                ) {
                                    Text(
                                        text = CareerEngine.formatGameDate(state.currentDayIndex, state.currentSeasonYear),
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 10.sp,
                                        color = AccentBright,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${state.currentClub.city} • 19°C Stadyum Atmosferi • ${FLFormatters.formatCurrency(state.balanceEur)}",
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (nextMatch != null) {
                                    "Sıradaki Maç: ${nextMatch.homeClubName} vs ${nextMatch.awayClubName}"
                                } else {
                                    "Sezon Fikstürü Tamamlandı"
                                },
                                fontFamily = OutfitFontFamily,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        FootballerAvatarCanvas(
                            appearance = state.player.appearance,
                            clubPrimaryHex = state.currentClub.primaryColorHex,
                            clubSecondaryHex = state.currentClub.secondaryColorHex,
                            kitNumber = state.player.kitNumber,
                            modifier = Modifier.size(62.dp)
                        )
                    }
                }
            }

            // Pending Call Banner if any unread call exists
            if (pendingCall != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    border = BorderStroke(1.dp, AccentBright),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onQuickCallOpen(pendingCall.id) }
                        .testTag("pending_call_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AccentPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Call, contentDescription = null, tint = Color(0xFF04150D), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Bekleyen Arama: ${pendingCall.callerName}",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = pendingCall.topic,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AccentBright
                                )
                            }
                        }
                        Text(
                            text = "YANITLA",
                            fontFamily = OutfitFontFamily,
                            fontSize = 12.sp,
                            color = AccentBright,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Icon Grid (4 columns x 3 rows)
            val gridApps = PhoneAppId.entries
            val rows = gridApps.chunked(4)
            rows.forEach { rowApps ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowApps.forEach { app ->
                        val badgeCount = when (app) {
                            PhoneAppId.MESSAGES -> state.totalUnreadMessages
                            PhoneAppId.INSTAPITCH -> state.totalUnreadStories + state.totalUnreadInstaDms + state.totalPendingInstaRequests
                            PhoneAppId.CALLS -> state.totalPendingCalls
                            PhoneAppId.NEWS -> 1
                            else -> 0
                        }
                        PhoneAppIconButton(
                            app = app,
                            badgeCount = badgeCount,
                            onClick = { onOpenApp(app) }
                        )
                    }
                    // Fill empty slots if row has < 4 items
                    repeat(4 - rowApps.size) {
                        Spacer(modifier = Modifier.width(72.dp))
                    }
                }
            }
        }

        // Bottom Frosted Dock (4 Favorite Apps: WhatsApp, Instagram, X, Banka)
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = SurfacePrimary.copy(alpha = 0.92f)),
            border = BorderStroke(1.dp, StrokePremiumStrong),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    PhoneAppId.MESSAGES to state.totalUnreadMessages,
                    PhoneAppId.INSTAPITCH to (state.totalUnreadStories + state.totalUnreadInstaDms + state.totalPendingInstaRequests),
                    PhoneAppId.PITCHX to 0,
                    PhoneAppId.BANK to 0
                ).forEach { (dockApp, badge) ->
                    PhoneAppIconButton(
                        app = dockApp,
                        badgeCount = badge,
                        showLabel = false,
                        onClick = { onOpenApp(dockApp) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PhoneAppIconButton(
    app: PhoneAppId,
    badgeCount: Int,
    showLabel: Boolean = true,
    onClick: () -> Unit
) {
    val c1 = parseSafeHexColor(app.primaryHex, AccentPrimary)
    val c2 = parseSafeHexColor(app.secondaryHex, SurfaceElevated)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
            .testTag("phone_app_${app.name.lowercase()}")
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            if (app.customLogoRes != null) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(17.dp))
                ) {
                    Image(
                        painter = painterResource(id = app.customLogoRes),
                        contentDescription = app.titleTr,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(1.24f)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(Brush.linearGradient(listOf(c1, c2)))
                        .border(1.dp, Color.White.copy(alpha = 0.30f), RoundedCornerShape(17.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = app.icon,
                            contentDescription = app.titleTr,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            if (badgeCount > 0) {
                Box(modifier = Modifier.padding(top = 1.dp, end = 1.dp)) {
                    FLNotificationBadge(count = badgeCount)
                }
            }
        }
        if (showLabel) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = app.titleTr,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PhoneAppHeaderBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    accentColor: Color = AccentBright
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfacePrimary)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("phone_app_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 23.sp,
                    letterSpacing = 0.5.sp,
                    color = TextPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = accentColor
                    )
                }
            }
        }
        HorizontalDivider(color = StrokePremium)
    }
}

// ============================================================================
// 5.2 WHATSAPP (1:1 DARK MODE MESSAGING REPLICA)
// ============================================================================

@Composable
private fun PhoneMessagesApp(
    state: CareerSaveState,
    selectedNpcId: String?,
    onSelectNpc: (String?) -> Unit,
    onSendMessage: (String, String) -> Unit,
    onStartCall: (String) -> Unit,
    onBackHome: () -> Unit
) {
    val waBgDark = Color(0xFF0B141A)
    val waHeaderDark = Color(0xFF1F2C34)
    val waIncomingBubble = Color(0xFF202C33)
    val waOutgoingBubble = Color(0xFF005C4B)
    val waAccentGreen = Color(0xFF00A884)
    val waBlueTick = Color(0xFF53BDEB)
    val waMutedText = Color(0xFF8696A0)

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var customInput by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf(0) } // 0: Tümü, 1: Okunmamış, 2: Gruplar
    var waBottomTab by rememberSaveable { mutableStateOf(0) } // 0: Sohbetler, 1: Güncellemeler, 2: Aramalar

    val activeNpc = state.npcs.find { it.id == selectedNpcId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(waBgDark)
            .testTag("phone_messages_app")
    ) {
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

                    // Filter Chips: Tümü | Okunmamış | Gruplar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Tümü" to 0,
                            "Okunmamış (${state.totalUnreadMessages})" to 1,
                            "Gruplar" to 2
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
                                    color = if (active) Color(0xFF25D366) else waMutedText,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    val filteredNpcs = state.npcs.filter { npc ->
                        val matchesQuery = searchQuery.isBlank() ||
                            npc.name.contains(searchQuery, ignoreCase = true) ||
                            npc.roleTitle.contains(searchQuery, ignoreCase = true)
                        val matchesTab = when (selectedFilter) {
                            1 -> npc.unreadCount > 0
                            2 -> npc.id == "NPC_TEAM_GROUP" || npc.name.contains("Odası", ignoreCase = true)
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
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectNpc(npc.id) }
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
                                                color = if (npc.unreadCount > 0) Color(0xFF25D366) else waMutedText,
                                                fontWeight = if (npc.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                        Text(
                                            text = "${npc.roleTitle} • Yakınlık %${npc.affinity}",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFF25D366)
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
                                            if (npc.unreadCount > 0) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF25D366)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "${npc.unreadCount}",
                                                        fontFamily = SpaceGroteskFontFamily,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0B141A)
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
                                text = "DURUM GÜNCELLEMELERİ",
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = waMutedText
                            )
                        }
                        items(state.npcs) { npc ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = waHeaderDark,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectNpc(npc.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .border(2.dp, Color(0xFF25D366), CircleShape)
                                            .padding(3.dp)
                                            .clip(CircleShape)
                                            .background(parseSafeHexColor(npc.avatarColorHex, waAccentGreen)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = npc.name.take(1).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(npc.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${npc.roleTitle} • Bugün ${state.formattedClockTime}", color = waMutedText, fontSize = 12.sp)
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
                        items(state.npcs) { npc ->
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                            Text(npc.roleTitle, color = waMutedText, fontSize = 12.sp)
                                        }
                                    }
                                    IconButton(onClick = { onStartCall(npc.id) }) {
                                        Icon(Icons.Filled.Call, contentDescription = "Ara", tint = Color(0xFF25D366))
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
                                tint = if (selected) Color(0xFF25D366) else waMutedText,
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
            // WhatsApp Active Conversation Screen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(waHeaderDark)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
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
                    Column {
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
                            text = "çevrimiçi • ${activeNpc.roleTitle} (Güven %${activeNpc.trust})",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            color = Color(0xFF25D366)
                        )
                    }
                }
                IconButton(onClick = { onStartCall(activeNpc.id) }) {
                    Icon(Icons.Filled.Call, contentDescription = "Ara", tint = Color.White)
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
                                text = "🔒 Mesajlar uçtan uca şifrelenmiştir.",
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
                                if (!msg.isFromUser && activeNpc.id == "NPC_TEAM_GROUP") {
                                    Text(
                                        text = msg.senderName,
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF25D366)
                                    )
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
                                if (msg.impactSummary != null) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = msg.impactSummary,
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 11.sp,
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
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
}

// ============================================================================
// 5.3 INSTAGRAM (1:1 REPLICA: STORIES, FEED, SLIDE-UP COMMENTS, DIRECT & REQUESTS)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstaPitchApp(
    state: CareerSaveState,
    activeStoryId: String?,
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
    var tabIndex by rememberSaveable { mutableStateOf(0) } // 0: Feed, 1: Explore, 2: Share (+), 3: Profile
    var bottomSheetPostId by rememberSaveable { mutableStateOf<String?>(null) }
    var commentDraftText by rememberSaveable { mutableStateOf("") }
    var newPostCaption by rememberSaveable { mutableStateOf("") }

    // Instagram Direct (DM) navigation states
    var isDmInboxOpen by rememberSaveable { mutableStateOf(false) }
    var dmTabIndex by rememberSaveable { mutableStateOf(0) } // 0: Mesajlar (Primary), 1: İstekler (Pending Requests)
    var activeDmThreadId by rememberSaveable { mutableStateOf<String?>(null) }
    var dmDraftText by rememberSaveable { mutableStateOf("") }

    val activeStory = state.stories.find { it.id == activeStoryId }
    val sheetPost = state.socialPosts.find { it.id == bottomSheetPostId }
    val activeDmThread = state.instaDmThreads.find { it.id == activeDmThreadId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("phone_instapitch_app")
    ) {
        if (isDmInboxOpen) {
            // ================================================================
            // INSTAGRAM DIRECT (DM) & BEKLEYEN İSTEKLER EKRANI
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
                                tint = Color(0xFF0095F6),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Instagram Direct",
                            fontFamily = OutfitFontFamily,
                            fontSize = 12.sp,
                            color = Color(0xFFA8A8A8),
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }

                    val primaryThreads = state.instaDmThreads.filter { !it.isPendingRequest }
                    val requestThreads = state.instaDmThreads.filter { it.isPendingRequest }

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
                            color = Color(0xFFA8A8A8),
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
                                                tint = Color(0xFF0095F6),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• ${thread.followerCountLabel} takipçi",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 11.sp,
                                            color = Color(0xFFA8A8A8)
                                        )
                                    }
                                    Text(
                                        text = "${thread.handle} • ${thread.roleLabel}",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF0095F6)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = lastMsg?.text ?: "",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 13.sp,
                                        color = if (thread.unreadCount > 0 || thread.isPendingRequest) Color.White else Color(0xFFA8A8A8),
                                        fontWeight = if (thread.unreadCount > 0 || thread.isPendingRequest) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (thread.isPendingRequest) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0095F6).copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, Color(0xFF0095F6))
                                    ) {
                                        Text(
                                            text = "İstek",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0095F6),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                } else if (thread.unreadCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0095F6))
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Active Instagram Direct Conversation Thread
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
                        Column(modifier = Modifier.weight(1f)) {
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
                                        tint = Color(0xFF0095F6),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${activeDmThread.handle} • ${activeDmThread.roleLabel}",
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFFA8A8A8)
                            )
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
                                    text = "${activeDmThread.handle} • Instagram • ${activeDmThread.followerCountLabel} takipçi",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFFA8A8A8)
                                )
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
                                                color = Color(0xFF0095F6).copy(alpha = 0.22f),
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
                    }

                    if (activeDmThread.isPendingRequest) {
                        // Authentic Instagram Pending Message Request Action Bar ("Sil" | "Kabul Et")
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
                                color = Color(0xFFA8A8A8),
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
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0095F6)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("insta_dm_accept_${activeDmThread.id}")
                                ) {
                                    Text("Kabul Et", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Quick Contextual Suggestions + Free-Text Message Input
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
                                            onSendDmMessage(activeDmThread.id, suggestion)
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
                                    placeholder = { Text("Mesaj...", color = Color(0xFFA8A8A8), fontSize = 14.sp) },
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
                                            onSendDmMessage(activeDmThread.id, dmDraftText)
                                            dmDraftText = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0095F6)),
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
        } else {
            // ================================================================
            // INSTAGRAM MAIN APP (TOP BAR + FEED/EXPLORE/SHARE/PROFILE + BOTTOM NAV)
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
                                        .background(Color(0xFFFF3040)),
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
                HorizontalDivider(color = Color(0xFF262626), thickness = 0.6.dp)

                // Main Content Area
                Box(modifier = Modifier.weight(1f)) {
                    when (tabIndex) {
                        0 -> {
                            // Instagram Home Feed: Stories Tray + Vertical Posts
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    // Authentic Instagram Stories Row ("Hikayen" + Rainbow Rings)
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
                                                            .background(Color(0xFF0095F6))
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

                                            state.stories.forEach { story ->
                                                StoryRingAvatar(
                                                    story = story,
                                                    onClick = {
                                                        onViewReactStory(story.id, false)
                                                        onOpenStory(story.id)
                                                    }
                                                )
                                            }
                                        }
                                        HorizontalDivider(color = Color(0xFF262626), thickness = 0.6.dp)
                                    }
                                }

                                items(state.socialPosts, key = { it.id }) { post ->
                                    InstaPostCard(
                                        post = post,
                                        onOpenCommentsSheet = { bottomSheetPostId = post.id },
                                        onToggleLike = { onToggleLike(post.id) },
                                        onToggleSave = { onToggleSave(post.id) }
                                    )
                                }
                            }
                        }
                        1 -> {
                            // Keşfet (Instagram Explore Grid)
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                item {
                                    Text(
                                        text = "KEŞFET • FUTBOL DÜNYASINDA TRENDLER",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFA8A8A8)
                                    )
                                }
                                items(state.galleryPhotos, key = { it.id }) { photo ->
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                                        border = BorderStroke(1.dp, Color(0xFF262626)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column {
                                            Image(
                                                painter = painterResource(id = resolveSceneDrawable(photo.sceneDrawableType)),
                                                contentDescription = photo.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(170.dp)
                                            )
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text(photo.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(photo.caption, color = Color(0xFFA8A8A8), fontSize = 12.sp)
                                            }
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
                                        border = BorderStroke(1.dp, Color(0xFF262626))
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
                                                color = Color(0xFFA8A8A8)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            OutlinedTextField(
                                                value = newPostCaption,
                                                onValueChange = { newPostCaption = it },
                                                placeholder = { Text("Açıklama yaz...", color = Color(0xFFA8A8A8)) },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Color(0xFF0095F6),
                                                    unfocusedBorderColor = Color(0xFF262626),
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                )
                                            )
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
                                        color = Color(0xFFA8A8A8)
                                    )
                                }

                                items(state.galleryPhotos, key = { it.id }) { photo ->
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                                        border = BorderStroke(1.dp, Color(0xFF262626)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Image(
                                                painter = painterResource(id = resolveSceneDrawable(photo.sceneDrawableType)),
                                                contentDescription = photo.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(64.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(photo.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(photo.caption, color = Color(0xFFA8A8A8), fontSize = 12.sp, maxLines = 2)
                                            }
                                            Button(
                                                onClick = {
                                                    onShareFromGallery(photo.id, newPostCaption.ifBlank { photo.caption })
                                                    tabIndex = 0
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0095F6))
                                            ) {
                                                Text("Paylaş", color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else -> {
                            // Instagram Profil Sekmesi
                            val userPosts = state.socialPosts.filter { it.isUserPost }
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(14.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
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
                                                FootballerAvatarCanvas(
                                                    appearance = state.player.appearance,
                                                    clubPrimaryHex = state.currentClub.primaryColorHex,
                                                    clubSecondaryHex = state.currentClub.secondaryColorHex,
                                                    kitNumber = state.player.kitNumber,
                                                    modifier = Modifier.size(68.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Row(
                                                modifier = Modifier.weight(1f),
                                                horizontalArrangement = Arrangement.SpaceEvenly
                                            ) {
                                                ProfileStatMetric("${state.galleryPhotos.size + userPosts.size}", "gönderi")
                                                ProfileStatMetric("${state.player.followersCount}", "takipçi")
                                                ProfileStatMetric("348", "takip")
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = state.player.fullName,
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 15.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Filled.Verified,
                                                contentDescription = null,
                                                tint = Color(0xFF0095F6),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = "Sporcu • ⚽ @${state.currentClub.shortName.lowercase()}resmi #${state.player.kitNumber}",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 12.sp,
                                            color = Color(0xFF0095F6)
                                        )
                                        Text(
                                            text = state.player.backgroundStory,
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 12.sp,
                                            color = Color(0xFFD4D4D4)
                                        )
                                    }
                                }

                                item {
                                    HorizontalDivider(color = Color(0xFF262626))
                                }

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
                                                    painter = painterResource(id = resolveSceneDrawable(item.sceneDrawableType)),
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
                            }
                        }
                    }
                }

                // Instagram Bottom Navigation Bar (Home, Search, Add+, DM, Profile)
                HorizontalDivider(color = Color(0xFF262626), thickness = 0.6.dp)
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
                        Triple(3, "Profil", Icons.Filled.Person)
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

        // ====================================================================
        // INSTAGRAM SLIDE-UP COMMENTS BOTTOM SHEET (ModalBottomSheet)
        // ====================================================================
        if (sheetPost != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { bottomSheetPostId = null },
                sheetState = sheetState,
                containerColor = Color(0xFF1C1C1E),
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 380.dp, max = 560.dp)
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

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(structuredComments, key = { it.id }) { cmt ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2C2C2E)),
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
                                            color = Color.White
                                        )
                                        if (cmt.isVerified) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Filled.Verified,
                                                contentDescription = null,
                                                tint = Color(0xFF0095F6),
                                                modifier = Modifier.size(13.dp)
                                            )
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
                                    Text(
                                        text = "Yanıtla",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF8E8E93),
                                        modifier = Modifier.clickable {
                                            commentDraftText = "${cmt.authorHandle} "
                                        }
                                    )
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { onToggleLikeComment(sheetPost.id, cmt.id) }
                                        .padding(start = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (cmt.likedByUser) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                        contentDescription = "Yorumu Beğen",
                                        tint = if (cmt.likedByUser) Color(0xFFFF3040) else Color(0xFF8E8E93),
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
                        }
                    }

                    HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.7.dp)

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
                                    text = "${sheetPost.authorHandle} için yorum ekle...",
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
                                focusedBorderColor = Color(0xFF0095F6),
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
                            color = if (commentDraftText.isNotBlank()) Color(0xFF0095F6) else Color(0xFF0095F6).copy(alpha = 0.4f),
                            modifier = Modifier
                                .clickable {
                                    if (commentDraftText.isNotBlank()) {
                                        onComment(sheetPost.id, commentDraftText)
                                        commentDraftText = ""
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("insta_sheet_comment_send")
                        )
                    }
                }
            }
        }

        // Full-Screen Story Viewer Overlay
        if (activeStory != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.96f))
                    .testTag("insta_story_viewer")
            ) {
                Image(
                    painter = painterResource(id = resolveSceneDrawable(activeStory.sceneType)),
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                            IconButton(onClick = onCloseStory) {
                                Icon(Icons.Filled.Close, contentDescription = "Kapat", tint = Color.White)
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212).copy(alpha = 0.92f)),
                        border = BorderStroke(1.dp, Color(0xFF262626))
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
                            Spacer(modifier = Modifier.height(14.dp))
                            FLPrimaryButton(
                                text = if (activeStory.reacted) "🔥 Tepki Gönderildi" else "🔥 Hikayeye Ateş & Alkış Gönder",
                                onClick = { onViewReactStory(activeStory.id, true) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryRingAvatar(
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
private fun InstaPostCard(
    post: SocialMediaPost,
    onOpenCommentsSheet: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        // Post Header (Instagram rainbow ring avatar + handle + verified + 3 dots)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
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
                    Text(
                        text = post.authorName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
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
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = null,
                            tint = Color(0xFF0095F6),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = post.authorName,
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFFA8A8A8)
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

        // Full-bleed Post Photo
        Image(
            painter = painterResource(id = resolveSceneDrawable(post.imageSceneType)),
            contentDescription = post.content,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )

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
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Paylaş",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Icon(
                    imageVector = if (post.savedByUser) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "Kaydet",
                    tint = Color.White,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onToggleSave)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
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
                    withStyle(SpanStyle(color = Color(0xFFEFEFEF))) {
                        append(post.content)
                    }
                },
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "${post.commentsCount} yorumun tümünü gör",
                fontFamily = OutfitFontFamily,
                fontSize = 12.sp,
                color = Color(0xFFA8A8A8),
                modifier = Modifier
                    .clickable(onClick = onOpenCommentsSheet)
                    .padding(vertical = 2.dp)
            )

            val previewComment = post.instaComments.firstOrNull()
            if (previewComment != null) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                            append("${previewComment.authorHandle.removePrefix("@")} ")
                        }
                        withStyle(SpanStyle(color = Color(0xFFD6D6D6))) {
                            append(previewComment.text)
                        }
                    },
                    fontFamily = OutfitFontFamily,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable(onClick = onOpenCommentsSheet)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = post.dateLabel,
                fontFamily = OutfitFontFamily,
                fontSize = 10.sp,
                color = Color(0xFF737373)
            )
        }
        HorizontalDivider(color = Color(0xFF1A1A1A), thickness = 0.6.dp)
    }
}

@Composable
private fun ProfileStatMetric(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.White
        )
        Text(
            text = label,
            fontFamily = OutfitFontFamily,
            fontSize = 12.sp,
            color = Color(0xFFA8A8A8)
        )
    }
}

// ============================================================================
// 5.4 X (1:1 PITCH-BLACK FUTBOL GÜNDEMİ & TRANSFER KULİSİ REPLICA)
// ============================================================================

@Composable
private fun PitchXApp(
    state: CareerSaveState,
    onToggleLike: (String) -> Unit,
    onToggleRepost: (String) -> Unit,
    onPublishXPost: (String) -> Unit,
    onBackHome: () -> Unit
) {
    val xBlue = Color(0xFF1D9BF0)
    val xGray = Color(0xFF71767B)
    val xBorder = Color(0xFF2F3336)

    var tabIndex by rememberSaveable { mutableStateOf(0) }
    var draftText by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("phone_pitchx_app")
    ) {
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
            FootballerAvatarCanvas(
                appearance = state.player.appearance,
                clubPrimaryHex = state.currentClub.primaryColorHex,
                clubSecondaryHex = state.currentClub.secondaryColorHex,
                kitNumber = state.player.kitNumber,
                modifier = Modifier.size(30.dp)
            )
        }

        // X Underline Tabs ("Sana Özel" | "Takip Edilenler" | "Gündem")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black)
        ) {
            listOf("Sana Özel", "Takip Edilenler", "Gündem").forEachIndexed { idx, title ->
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
                        fontSize = 14.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color.White else xGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(if (selected) xBlue else Color.Transparent)
                    )
                }
            }
        }
        HorizontalDivider(color = xBorder, thickness = 0.7.dp)

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
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
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("TÜRKİYE VE AVRUPA GÜNDEMİ", fontFamily = OutfitFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = xBlue)
                        Text("1. #${state.currentClub.shortName} • 28,4 B Gönderi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("2. ${state.player.fullName} • 14,9 B Gönderi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("3. Fabrizio Romano • 94,1 B Gönderi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("4. #Transfermarkt • 31,0 B Gönderi", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    HorizontalDivider(color = xBorder, thickness = 0.7.dp)
                }
            }

            val posts = when (tabIndex) {
                1 -> state.xPosts.filter { it.category == "GAZETECİ" || it.category == "YORUMCU" }
                else -> state.xPosts
            }

            items(posts, key = { it.id }) { xp ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF16181C))
                                .border(1.dp, xBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = xp.authorName.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
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
                                        tint = xBlue,
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
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = "Yanıtla",
                                        tint = xGray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${(xp.likes / 9).coerceAtLeast(3)}", fontSize = 12.sp, color = xGray)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onToggleRepost(xp.id) }
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { onToggleLike(xp.id) }
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
                                Text(
                                    text = "${(xp.likes * 18 / 1000).coerceAtLeast(4)}B görüntülenme",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.sp,
                                    color = xGray
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = xBorder, thickness = 0.6.dp)
                }
            }
        }
    }
}


// ============================================================================
// 5.5 FUTBOL HABERLERİ UYGULAMASI
// ============================================================================

@Composable
private fun FootballNewsApp(
    state: CareerSaveState,
    selectedArticleId: String?,
    onSelectArticle: (String?) -> Unit,
    onBackHome: () -> Unit
) {
    var catIndex by rememberSaveable { mutableStateOf(0) }
    val categories = listOf("Manşetler", "Kariyerim", "Maç Sonuçları", "Transfer")
    val activeArticle = state.newsEvents.find { it.id == selectedArticleId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .testTag("phone_news_app")
    ) {
        if (activeArticle == null) {
            PhoneAppHeaderBar(
                title = "FUTBOL HABERLERİ",
                subtitle = "${state.currentClub.leagueName} & Kariyer Basın Bülteni",
                onBack = onBackHome
            )

            FLSegmentedTabs(
                tabs = categories,
                selectedIndex = catIndex,
                onSelect = { catIndex = it },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )

            val filtered = state.newsEvents.filter { news ->
                when (catIndex) {
                    1 -> news.headline.contains(state.player.lastName, ignoreCase = true) || news.category == "RÖPORTAJ"
                    2 -> news.category == "MAÇ SONUCU"
                    3 -> news.category == "TRANSFER"
                    else -> true
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { item ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                        border = BorderStroke(1.dp, StrokePremium),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectArticle(item.id) }
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.category, style = MaterialTheme.typography.labelSmall, color = GoldPrimary, fontWeight = FontWeight.Bold)
                                Text(item.dateLabel, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.headline,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.body,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        } else {
            PhoneAppHeaderBar(
                title = activeArticle.category,
                subtitle = activeArticle.dateLabel,
                onBack = { onSelectArticle(null) }
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Image(
                        painter = painterResource(id = R.drawable.img_stadium_hero),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                }
                item {
                    Text(
                        text = activeArticle.headline,
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = TextPrimary
                    )
                }
                item {
                    Text(
                        text = "İlgili Kulüp: ${activeArticle.relatedClubName} • Taraftar Tepkisi: ${activeArticle.fanReaction}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentBright
                    )
                }
                item {
                    Text(
                        text = activeArticle.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

// ============================================================================
// 5.6 BANKA UYGULAMASI
// ============================================================================

@Composable
private fun BankApp(
    state: CareerSaveState,
    onBackHome: () -> Unit
) {
    var tabIdx by rememberSaveable { mutableStateOf(0) }
    val totalIncome = state.transactions.filter { it.amountEur > 0 }.sumOf { it.amountEur }
    val totalExpense = state.transactions.filter { it.amountEur < 0 }.sumOf { -it.amountEur }
    val nextSalaryDay = ((state.currentDayIndex / 7) + 1) * 7

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .testTag("phone_bank_app")
    ) {
        PhoneAppHeaderBar(
            title = "FL PRIVATE BANK",
            subtitle = "Futbolcu Varlık, Maaş & Prim Yönetimi",
            onBack = onBackHome,
            accentColor = GoldPrimary
        )

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.55f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("TOPLAM KULLANILABİLİR BAKİYE", style = MaterialTheme.typography.labelSmall, color = GoldPrimary)
                Text(
                    text = FLFormatters.formatCurrency(state.balanceEur),
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Haftalık Maaş", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(FLFormatters.formatWage(state.contract.weeklyWageEur), style = MaterialTheme.typography.titleSmall, color = AccentBright, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Maç / Gol Primi", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(
                            "${FLFormatters.formatCurrency(state.contract.matchAppearanceBonusEur)} / ${FLFormatters.formatCurrency(state.contract.goalBonusEur)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Sonraki Maaş", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("Gün $nextSalaryDay", style = MaterialTheme.typography.titleSmall, color = GoldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        FLSegmentedTabs(
            tabs = listOf("İşlemler", "Gelirler", "Giderler", "İstatistikler"),
            selectedIndex = tabIdx,
            onSelect = { tabIdx = it },
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )

        val filteredTx = when (tabIdx) {
            1 -> state.transactions.filter { it.amountEur > 0 }
            2 -> state.transactions.filter { it.amountEur < 0 }
            else -> state.transactions
        }

        if (tabIdx == 3) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                    border = BorderStroke(1.dp, StrokePremium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("FİNANSAL ÖZET", style = MaterialTheme.typography.labelLarge, color = GoldPrimary)
                        Text("Toplam Kariyer Geliri: ${FLFormatters.formatCurrency(totalIncome)}", color = AccentBright, fontWeight = FontWeight.Bold)
                        Text("Toplam Harcama & Yatırım: ${FLFormatters.formatCurrency(totalExpense)}", color = Negative, fontWeight = FontWeight.Bold)
                        Text("Sahip Olunan Varlık Sayısı: ${state.ownedLifestyleItemIds.size}", color = TextPrimary)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTx, key = { it.id }) { tx ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                        border = BorderStroke(1.dp, StrokePremium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tx.description, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text("${tx.category} • ${tx.dateLabel}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Text(
                                text = FLFormatters.formatSignedCurrency(tx.amountEur),
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = if (tx.amountEur >= 0) AccentBright else Negative
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 5.7 GALERİ VE KAMERA UYGULAMALARI
// ============================================================================

@Composable
private fun GalleryApp(
    state: CareerSaveState,
    onSharePhotoToInsta: (String, String) -> Unit,
    onOpenCamera: () -> Unit,
    onBackHome: () -> Unit
) {
    var albumIdx by rememberSaveable { mutableStateOf(0) }
    val albums = listOf("Tümü", "Maçlar", "Kutlamalar", "Antrenman")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .testTag("phone_gallery_app")
    ) {
        PhoneAppHeaderBar(
            title = "GALERİ",
            subtitle = "${state.galleryPhotos.size} Kayıtlı Anı & Maç Karesi",
            onBack = onBackHome
        )

        FLSegmentedTabs(
            tabs = albums,
            selectedIndex = albumIdx,
            onSelect = { albumIdx = it },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )

        val photos = state.galleryPhotos.filter {
            albumIdx == 0 || it.album.equals(albums[albumIdx], ignoreCase = true)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FLPrimaryButton(
                    text = "Yeni Sahne Fotoğrafı Çek (Kamera)",
                    onClick = onOpenCamera,
                    icon = Icons.Filled.CameraAlt
                )
            }
            items(photos, key = { it.id }) { photo ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                    border = BorderStroke(1.dp, StrokePremium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Image(
                            painter = painterResource(id = resolveSceneDrawable(photo.sceneDrawableType)),
                            contentDescription = photo.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(165.dp)
                        )
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(photo.album.uppercase(), style = MaterialTheme.typography.labelSmall, color = AccentBright)
                                Text(photo.dateLabel, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(photo.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text(photo.caption, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Spacer(modifier = Modifier.height(10.dp))
                            FLPrimaryButton(
                                text = if (photo.sharedOnSocial) "InstaPitch'te Tekrar Paylaş" else "InstaPitch'te Paylaş (+Takipçi)",
                                onClick = { onSharePhotoToInsta(photo.id, photo.caption) },
                                icon = Icons.Filled.Share
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraApp(
    state: CareerSaveState,
    onCapturePhoto: (String, String, String, String) -> Unit,
    onBackHome: () -> Unit
) {
    var selectedSceneIdx by rememberSaveable { mutableStateOf(0) }
    var customTitle by rememberSaveable { mutableStateOf("") }
    var customCaption by rememberSaveable { mutableStateOf("") }

    val scenes = listOf(
        Triple("STADIUM", "Maçlar", "${state.currentClub.stadiumName} Stadyum Atmosferi"),
        Triple("TRAINING", "Antrenman", "${state.currentClub.shortName} Tesislerinde Antrenman"),
        Triple("LOCKER", "Kutlamalar", "Soyunma Odası & Takım Ruhu"),
        Triple("TRANSFER", "Kutlamalar", "Basın & Medya Günü")
    )
    val currentScene = scenes[selectedSceneIdx]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .testTag("phone_camera_app")
    ) {
        PhoneAppHeaderBar(
            title = "KAMERA",
            subtitle = "Oyun İçi Sahne Yakalayıcı",
            onBack = onBackHome
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Viewfinder Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(2.dp, AccentBright, RoundedCornerShape(22.dp))
            ) {
                Image(
                    painter = painterResource(id = resolveSceneDrawable(currentScene.first)),
                    contentDescription = currentScene.third,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BackgroundPrimary.copy(alpha = 0.8f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "VİZÖR: ${currentScene.third}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentBright,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            FLSegmentedTabs(
                tabs = listOf("Stadyum", "Antrenman", "Soyunma Odası", "Basın"),
                selectedIndex = selectedSceneIdx,
                onSelect = { selectedSceneIdx = it }
            )

            OutlinedTextField(
                value = customTitle,
                onValueChange = { customTitle = it },
                placeholder = { Text("Fotoğraf başlığı (isteğe bağlı)...", color = TextSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = customCaption,
                onValueChange = { customCaption = it },
                placeholder = { Text("Alt yazı / açıklama...", color = TextSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            FLPrimaryButton(
                text = "FOTOĞRAFI ÇEK VE GALERİYE KAYDET",
                onClick = {
                    onCapturePhoto(
                        currentScene.first,
                        currentScene.second,
                        customTitle.ifBlank { currentScene.third },
                        customCaption.ifBlank { "${state.player.fullName} • ${currentScene.third}" }
                    )
                },
                icon = Icons.Filled.CameraAlt,
                testTag = "camera_shutter_btn"
            )
        }
    }
}

// ============================================================================
// 5.8 KİŞİLER VE ARAMALAR UYGULAMALARI
// ============================================================================

@Composable
private fun ContactsApp(
    state: CareerSaveState,
    onMessageNpc: (String) -> Unit,
    onCallNpc: (String) -> Unit,
    onBackHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .testTag("phone_contacts_app")
    ) {
        PhoneAppHeaderBar(
            title = "KİŞİLER",
            subtitle = "Futbol Çevresi, Teknik Heyet, Menajer & Aile",
            onBack = onBackHome
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.npcs, key = { it.id }) { npc ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                    border = BorderStroke(1.dp, StrokePremium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(parseSafeHexColor(npc.avatarColorHex, AccentPrimary)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = npc.name.take(1).uppercase(),
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color(0xFF04150D)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(npc.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text("${npc.roleTitle} • ${npc.personality}", style = MaterialTheme.typography.bodySmall, color = AccentBright)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        FLStatBar(label = "Yakınlık (Affinity)", value = npc.affinity, modifier = Modifier.padding(vertical = 2.dp))
                        FLStatBar(label = "Güven (Trust)", value = npc.trust, modifier = Modifier.padding(vertical = 2.dp))

                        if (npc.importantMemories.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Son Anı: ${npc.importantMemories.first()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FLPrimaryButton(
                                text = "Mesaj Gönder",
                                onClick = { onMessageNpc(npc.id) },
                                icon = Icons.AutoMirrored.Filled.Chat,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedButton(
                                onClick = { onCallNpc(npc.id) },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(1.dp, AccentBright)
                            ) {
                                Icon(Icons.Filled.Call, contentDescription = null, tint = AccentBright, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sesli Ara", color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CallsApp(
    state: CareerSaveState,
    activeCallId: String?,
    onSelectCall: (String?) -> Unit,
    onCompleteCall: (String, String) -> Unit,
    onInitiateCallWithNpc: (String) -> Unit,
    onBackHome: () -> Unit
) {
    val activeCall = state.callRecords.find { it.id == activeCallId }
        ?: state.callRecords.firstOrNull { !it.completed }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .testTag("phone_calls_app")
    ) {
        PhoneAppHeaderBar(
            title = "ARAMALAR",
            subtitle = "Sesli Görüşmeler & Gelen Çağrılar",
            onBack = onBackHome
        )

        if (activeCall != null && !activeCall.completed) {
            // Active / Incoming Call Screen
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                border = BorderStroke(1.5.dp, AccentBright),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AccentPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = null, tint = Color(0xFF04150D), modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = activeCall.callerName,
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "${activeCall.callerRole} • ${activeCall.topic}",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentBright
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfacePrimary,
                        border = BorderStroke(1.dp, StrokePremium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${activeCall.transcript}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "TELEFONDA YANITINI SEÇ:",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    activeCall.options.forEachIndexed { idx, opt ->
                        Button(
                            onClick = { onCompleteCall(activeCall.id, opt) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("call_option_$idx")
                        ) {
                            Text(
                                text = opt,
                                color = Color(0xFF04150D),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("HIZLI ARAMA BAŞLAT", style = MaterialTheme.typography.labelLarge, color = AccentBright)
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.npcs.forEach { npc ->
                            OutlinedButton(
                                onClick = { onInitiateCallWithNpc(npc.id) },
                                border = BorderStroke(1.dp, AccentPrimary)
                            ) {
                                Icon(Icons.Filled.Call, contentDescription = null, tint = AccentBright, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(npc.roleTitle, color = TextPrimary)
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("GEÇMİŞ ARAMA KAYITLARI", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                }
                items(state.callRecords, key = { it.id }) { call ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                        border = BorderStroke(1.dp, StrokePremium),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectCall(call.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(call.callerName, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text("${call.topic} • ${call.dateLabel}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Icon(
                                imageVector = if (call.completed) Icons.Filled.CheckCircle else Icons.Filled.Call,
                                contentDescription = null,
                                tint = if (call.completed) AccentPrimary else GoldPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 5.9 ALIŞVERİŞ / YAŞAM TARZI MAĞAZASI UYGULAMASI
// ============================================================================

@Composable
private fun ShoppingStoreApp(
    state: CareerSaveState,
    onPurchaseItem: (LifestylePurchaseItem) -> Unit,
    onBackHome: () -> Unit
) {
    var catIdx by rememberSaveable { mutableStateOf(0) }
    val categories = listOf("Tümü", "Krampon", "Kıyafet", "Ekipman", "Elektronik", "Araç", "Ev & Yatırım")
    val allItems = WorldDataCatalog.lifestylePurchaseItems

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .testTag("phone_shop_app")
    ) {
        PhoneAppHeaderBar(
            title = "FL LUXURY & PRO STORE",
            subtitle = "Bakiye: ${FLFormatters.formatCurrency(state.balanceEur)}",
            onBack = onBackHome,
            accentColor = GoldPrimary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEachIndexed { idx, label ->
                val selected = catIdx == idx
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) AccentPrimary else SurfacePrimary,
                    border = BorderStroke(1.dp, if (selected) AccentBright else StrokePremium),
                    modifier = Modifier.clickable { catIdx = idx }
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) Color(0xFF04150D) else TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        val filteredItems = allItems.filter { item ->
            when (catIdx) {
                1 -> item.category == "KRAMPON"
                2 -> item.category == "KIYAFET"
                3 -> item.category == "EKİPMAN"
                4 -> item.category == "ELEKTRONİK"
                5 -> item.category == "ARAÇ"
                6 -> item.category == "EV" || item.category == "YATIRIM"
                else -> true
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredItems, key = { it.id }) { item ->
                val isOwned = item.id in state.ownedLifestyleItemIds
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                    border = BorderStroke(1.dp, if (isOwned) AccentPrimary else StrokePremium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.category, style = MaterialTheme.typography.labelSmall, color = GoldPrimary, fontWeight = FontWeight.Bold)
                            Text(
                                text = FLFormatters.formatCurrency(item.priceEur),
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = AccentBright
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(item.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = buildString {
                                append("Etki: Moral +${item.passiveMoraleBonus}")
                                if (item.passiveEnergyRecoveryBonus > 0) append(" • Günlük Enerji +${item.passiveEnergyRecoveryBonus}")
                                if (item.weeklyDividendEur > 0) append(" • Haftalık Gelir +${FLFormatters.formatCurrency(item.weeklyDividendEur)}")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentBright
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FLPrimaryButton(
                            text = if (isOwned) "SAHİP OLUNDU" else "SATIN AL (${FLFormatters.formatCurrency(item.priceEur)})",
                            onClick = { onPurchaseItem(item) },
                            enabled = !isOwned && state.balanceEur >= item.priceEur,
                            testTag = "buy_item_${item.id}"
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// TELEFON AYARLARI & DUVAR KAĞIDI SEÇİCİ
// ============================================================================

@Composable
private fun PhoneSettingsApp(
    state: CareerSaveState,
    userSettings: UserSettings,
    onChangeWallpaper: (String) -> Unit,
    onUpdateAmbience: (Boolean) -> Unit,
    onUpdateSfx: (Boolean) -> Unit,
    onUpdateHaptics: (Boolean) -> Unit,
    onUpdateGraphics: (GraphicsQuality) -> Unit,
    onBackHome: () -> Unit
) {
    val wallpapers = listOf(
        "STADIUM_NIGHT" to "Stadyum Gece Işıkları",
        "MINIMAL_DARK" to "Minimal Koyu Futbol",
        "PLAYER_PORTRAIT" to "Oyuncu Portresi Vurgusu",
        "CLUB_THEME" to "${state.currentClub.shortName} Kulüp Teması"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .testTag("phone_settings_app")
    ) {
        PhoneAppHeaderBar(
            title = "TELEFON & OYUN AYARLARI",
            subtitle = "Duvar Kağıdı, Ses, Titreşim & Grafik Kalitesi",
            onBack = onBackHome
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                    border = BorderStroke(1.dp, StrokePremium)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("TELEFON DUVAR KAĞIDI", style = MaterialTheme.typography.labelLarge, color = GoldPrimary)
                        wallpapers.forEach { (id, title) ->
                            val selected = state.phoneWallpaperId == id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) SurfaceElevated else Color.Transparent)
                                    .clickable { onChangeWallpaper(id) }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(title, color = TextPrimary, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                                if (selected) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AccentBright)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                    border = BorderStroke(1.dp, StrokePremium)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("SES & GERİ BİLDİRİM", style = MaterialTheme.typography.labelLarge, color = AccentBright)
                        SettingToggleRow("Stadyum Atmosfer Sesi", userSettings.stadiumAmbienceEnabled, onUpdateAmbience)
                        SettingToggleRow("Maç ve Menü Ses Efektleri", userSettings.soundEffectsEnabled, onUpdateSfx)
                        SettingToggleRow("Dokunsal Titreşim (Haptics)", userSettings.hapticsEnabled, onUpdateHaptics)
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                    border = BorderStroke(1.dp, StrokePremium)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("GRAFİK KALİTESİ", style = MaterialTheme.typography.labelLarge, color = AccentBright)
                        GraphicsQuality.entries.forEach { q ->
                            val selected = userSettings.graphicsQuality == q
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) SurfaceElevated else Color.Transparent)
                                    .clickable { onUpdateGraphics(q) }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(q.labelTr, color = TextPrimary)
                                if (selected) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AccentBright)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF04150D),
                checkedTrackColor = AccentPrimary
            )
        )
    }
}

private fun resolveSceneDrawable(sceneType: String): Int {
    return when (sceneType.uppercase()) {
        "TRAINING" -> R.drawable.img_training_ground
        "LOCKER" -> R.drawable.img_locker_room
        "TRANSFER" -> R.drawable.img_transfer_lounge
        else -> R.drawable.img_stadium_hero
    }
}
