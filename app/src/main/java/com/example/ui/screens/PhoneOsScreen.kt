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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.UserSettings
import com.example.engine.CareerEngine
import com.example.engine.WorldDataCatalog
import com.example.model.CareerSaveState
import com.example.model.GraphicsQuality
import com.example.model.LifestylePurchaseItem
import com.example.ui.components.FLFormatters
import com.example.ui.components.FLNotificationBadge
import com.example.ui.components.FLPrimaryButton
import com.example.ui.components.FLSegmentedTabs
import com.example.ui.components.FLStatBar
import com.example.ui.components.FootballerAvatarCanvas
import com.example.ui.components.parseSafeHexColor
import com.example.ui.screens.phone.InstagramAppScreen
import com.example.ui.screens.phone.TransfermarktAppScreen
import com.example.ui.screens.phone.WhatsAppScreen
import com.example.ui.screens.phone.XAppScreen
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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
    TRANSFERMARKT("Transfermarkt", Icons.Filled.TrendingUp, "#00193F", "#00A6FF", R.drawable.logo_transfermarkt),
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
    onBackToHub: () -> Unit,
    // Extended hooks for @diyalog Gemini AI engine & system settings
    typingChatIds: Set<String> = emptySet(),
    aiTypingTargetId: String? = null,
    onMarkNpcRead: (String) -> Unit = {},
    onOpenAiSettings: () -> Unit = {},
    onOpenSystemSettings: () -> Unit = {},
    onOpenCoachMeeting: () -> Unit = {},
    onOpenInteractiveCall: (String) -> Unit = {}
) {
    var currentApp by rememberSaveable { mutableStateOf<PhoneAppId?>(null) }
    var selectedChatNpcId by rememberSaveable { mutableStateOf<String?>(null) }
    var activeStoryId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedNewsArticleId by rememberSaveable { mutableStateOf<String?>(null) }
    var activeCallRecordId by rememberSaveable { mutableStateOf<String?>(null) }

    val effectiveTypingIds = remember(typingChatIds, aiTypingTargetId) {
        if (aiTypingTargetId != null) typingChatIds + aiTypingTargetId else typingChatIds
    }
    val effectivePrimaryTypingId = aiTypingTargetId ?: typingChatIds.firstOrNull()

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
                        PhoneAppId.MESSAGES -> WhatsAppScreen(
                            state = state,
                            selectedNpcId = selectedChatNpcId,
                            aiTypingTargetId = effectivePrimaryTypingId,
                            onSelectNpc = { selectedChatNpcId = it },
                            onMarkNpcRead = onMarkNpcRead,
                            onSendMessage = onSendMessageToNpc,
                            onStartCall = { npcId ->
                                onInitiateCallWithNpc(npcId)
                                currentApp = PhoneAppId.CALLS
                            },
                            onOpenCoachMeeting = onOpenCoachMeeting,
                            onBackHome = { currentApp = null }
                        )
                        PhoneAppId.INSTAPITCH -> InstagramAppScreen(
                            state = state,
                            activeStoryId = activeStoryId,
                            typingChatIds = effectiveTypingIds,
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
                        PhoneAppId.PITCHX -> XAppScreen(
                            state = state,
                            onToggleLike = onToggleLikeXPost,
                            onToggleRepost = onToggleRepostXPost,
                            onPublishXPost = onPublishXPost,
                            onBackHome = { currentApp = null }
                        )
                        PhoneAppId.TRANSFERMARKT -> TransfermarktAppScreen(
                            state = state,
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
                            isAiGenerating = effectivePrimaryTypingId != null,
                            onSelectCall = { activeCallRecordId = it },
                            onCompleteCall = { callId, option ->
                                onCompleteCall(callId, option)
                                activeCallRecordId = null
                            },
                            onInitiateCallWithNpc = onInitiateCallWithNpc,
                            onOpenInteractiveCall = onOpenInteractiveCall,
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
                            onOpenAiSettings = {
                                onOpenAiSettings()
                                onOpenSystemSettings()
                            },
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

            // App Icon Grid (4 columns x 3 rows = 12 Apps)
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
                    repeat(4 - rowApps.size) {
                        Spacer(modifier = Modifier.width(72.dp))
                    }
                }
            }
        }

        // Bottom Frosted Dock (4 Favorite Apps: WhatsApp, Instagram, X, Transfermarkt)
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
                    PhoneAppId.TRANSFERMARKT to 0
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
// FUTBOL HABERLERİ UYGULAMASI
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
// BANKA UYGULAMASI
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
// GALERİ VE KAMERA UYGULAMALARI
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
                                text = if (photo.sharedOnSocial) "Instagram'da Tekrar Paylaş" else "Instagram'da Paylaş (+Takipçi)",
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
// KİŞİLER VE ARAMALAR UYGULAMALARI
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
    isAiGenerating: Boolean = false,
    onSelectCall: (String?) -> Unit,
    onCompleteCall: (String, String) -> Unit,
    onInitiateCallWithNpc: (String) -> Unit,
    onOpenInteractiveCall: (String) -> Unit = {},
    onBackHome: () -> Unit
) {
    val activeCall = state.callRecords.find { it.id == activeCallId }
        ?: state.callRecords.firstOrNull { !it.completed }
    var customSpeechText by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .testTag("phone_calls_app")
    ) {
        PhoneAppHeaderBar(
            title = "ARAMALAR",
            subtitle = "Sesli Görüşmeler, AI Diyalog & Gelen Çağrılar",
            onBack = onBackHome
        )

        if (activeCall != null && !activeCall.completed) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                        border = BorderStroke(1.5.dp, AccentBright),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(AccentPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Call, contentDescription = null, tint = Color(0xFF04150D), modifier = Modifier.size(34.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = activeCall.callerName,
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "${activeCall.callerRole} • ${activeCall.topic}",
                                style = MaterialTheme.typography.labelMedium,
                                color = AccentBright
                            )
                            Spacer(modifier = Modifier.height(12.dp))
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

                            if (isAiGenerating) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "••• ${activeCall.callerName} yanıtlıyor...",
                                    color = AccentBright,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { onOpenInteractiveCall(activeCall.id) },
                                border = BorderStroke(1.dp, GoldPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Call, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tam Ekran Canlı Sesli Görüşme Ekranını Aç", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "SERBEST METİNLE TELEFONDA KONUŞ VEYA YANIT SEÇ:",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customSpeechText,
                                    onValueChange = { customSpeechText = it },
                                    placeholder = { Text("Telefonda kendi cümleni söyle...", color = TextSecondary, fontSize = 12.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AccentBright,
                                        unfocusedBorderColor = StrokePremium,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (customSpeechText.isNotBlank()) {
                                            onCompleteCall(activeCall.id, customSpeechText)
                                            customSpeechText = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Söyle", tint = Color(0xFF04150D))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            activeCall.options.forEachIndexed { idx, opt ->
                                Button(
                                    onClick = { onCompleteCall(activeCall.id, opt) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
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
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("HIZLI ARAMA BAŞLAT (CANLI AI GÖRÜŞME)", style = MaterialTheme.typography.labelLarge, color = AccentBright)
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
// ALIŞVERİŞ / YAŞAM TARZI MAĞAZASI UYGULAMASI
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
// TELEFON AYARLARI, GEMINI AI KISAYOLU & DUVAR KAĞIDI SEÇİCİ
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
    onOpenAiSettings: () -> Unit = {},
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
            subtitle = "Gemini AI, Duvar Kağıdı, Ses & Grafik Kalitesi",
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
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    border = BorderStroke(1.2.dp, AccentBright)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "🤖 YAPAY ZEKA DİYALOG & GEMINI API AYARLARI",
                            style = MaterialTheme.typography.labelLarge,
                            color = AccentBright,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "WhatsApp, Instagram DM, Teknik Direktör görüşmeleri ve telefon aramalarında gerçek zamanlı Gemini AI yanıtları için API anahtarınızı ve model seçimini buradan yönetebilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        FLPrimaryButton(
                            text = "Gemini API Anahtarı & AI Ayarlarını Aç",
                            onClick = onOpenAiSettings,
                            icon = Icons.Filled.Settings,
                            testTag = "phone_open_ai_settings_btn"
                        )
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
