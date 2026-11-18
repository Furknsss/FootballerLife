package com.example.ui.screens.phone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CareerSaveState
import com.example.ui.components.parseSafeHexColor
import com.example.ui.screens.PhoneAppId
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
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
import kotlinx.coroutines.launch

/**
 * Akıllı Telefon Bildirim Öğesi
 */
data class PhoneNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeFormatted: String,
    val appType: PhoneAppId,
    val category: String, // "WHATSAPP", "INSTAGRAM", "X", "ROMANCE", "CALL", "NEWS"
    val avatarHex: String = "#3B82F6",
    val secondaryText: String? = null,
    val actionPayload: String? = null,
    val isUrgent: Boolean = false
)

/**
 * Telefon Bildirim Paneli (Notification Shade):
 * Gerçek akıllı telefonlardaki gibi parmakla sağdan sola kaydırıldığında (Swipe-to-Dismiss)
 * pürüzsüz animasyon ve hafif titreşimle (haptic feedback) silinen ve okundu sayılan bildirim paneli.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneNotificationShade(
    state: CareerSaveState,
    initialNotifications: List<PhoneNotificationItem>? = null,
    onDismissNotification: (String) -> Unit = {},
    onClearAllNotifications: () -> Unit = {},
    onOpenNotificationTarget: (PhoneNotificationItem) -> Unit = {},
    onCloseShade: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Eğer dışarıdan liste verilmemişse CareerSaveState'ten dinamik üret
    val activeNotificationList = remember(state, initialNotifications) {
        val list = mutableStateListOf<PhoneNotificationItem>()
        if (initialNotifications != null) {
            list.addAll(initialNotifications)
        } else {
            list.addAll(generateNotificationsFromState(state))
        }
        list
    }

    var selectedTab by remember { mutableStateOf("ALL") }

    val filteredList = remember(activeNotificationList.toList(), selectedTab) {
        when (selectedTab) {
            "MESSAGES" -> activeNotificationList.filter { it.category == "WHATSAPP" }
            "SOCIAL" -> activeNotificationList.filter { it.category == "INSTAGRAM" || it.category == "X" }
            "ROMANCE" -> activeNotificationList.filter { it.category == "ROMANCE" }
            else -> activeNotificationList.toList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        BackgroundPrimary.copy(alpha = 0.98f),
                        Color(0xFF070B12).copy(alpha = 0.96f)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Üst Başlık & Kontrol Çubuğu
            NotificationShadeTopBar(
                clockTime = state.formattedClockTime,
                unreadCount = activeNotificationList.size,
                onClearAll = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    activeNotificationList.clear()
                    onClearAllNotifications()
                },
                onCloseShade = onCloseShade
            )

            // Filtre Sekmeleri
            NotificationCategoryFilterTabs(
                selectedTab = selectedTab,
                allCount = activeNotificationList.size,
                messagesCount = activeNotificationList.count { it.category == "WHATSAPP" },
                socialCount = activeNotificationList.count { it.category == "INSTAGRAM" || it.category == "X" },
                romanceCount = activeNotificationList.count { it.category == "ROMANCE" },
                onSelectTab = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bildirim Listesi veya Boş Durum
            if (filteredList.isEmpty()) {
                NotificationEmptyPlaceholder()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
                ) {
                    items(
                        items = filteredList,
                        key = { it.id }
                    ) { item ->
                        SwipeableNotificationCard(
                            notification = item,
                            onDismiss = { dismissedId ->
                                activeNotificationList.removeAll { it.id == dismissedId }
                                onDismissNotification(dismissedId)
                            },
                            onClick = { onOpenNotificationTarget(item) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Swipe-to-Dismiss (Kaydırarak Silme) Bildirim Kartı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableNotificationCard(
    notification: PhoneNotificationItem,
    onDismiss: (String) -> Unit,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isDismissedLocally by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                // Hafif titreşim ile kullanıcıya geri bildirim ver
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isDismissedLocally = true
                onDismiss(notification.id)
                true
            } else {
                false
            }
        }
    )

    AnimatedVisibility(
        visible = !isDismissedLocally,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = 280),
            shrinkTowards = Alignment.Top
        ) + fadeOut(animationSpec = tween(durationMillis = 220))
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                val isSwipingToDelete = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSwipingToDelete) Color(0xFFDC2626) else Color(0xFF7F1D1D),
                    label = "dismiss_bg_color"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(18.dp))
                        .background(backgroundColor)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Sil & Okundu Say",
                            fontFamily = OutfitFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Sil",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        ) {
            NotificationCardContent(
                notification = notification,
                onClick = onClick
            )
        }
    }
}

/**
 * Bildirim Kartı İçeriği
 */
@Composable
private fun NotificationCardContent(
    notification: PhoneNotificationItem,
    onClick: () -> Unit
) {
    val appHex = parseSafeHexColor(notification.avatarHex, AccentPrimary)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePrimary.copy(alpha = 0.94f)),
        border = BorderStroke(1.dp, if (notification.isUrgent) AccentBright.copy(alpha = 0.7f) else StrokePremium),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Üst Bilgi Satırı: Uygulama İkonu + Başlık + Saat
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(appHex),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = notification.appType.icon,
                            contentDescription = notification.appType.titleTr,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = notification.title,
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = notification.timeFormatted,
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Mesaj Metni
            Text(
                text = notification.message,
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            // İkincil Ek Bilgi (Varsa)
            if (notification.secondaryText != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (notification.isUrgent) Negative else GoldPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = notification.secondaryText,
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = if (notification.isUrgent) Negative else GoldPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Bildirim Paneli Üst Çubuğu
 */
@Composable
private fun NotificationShadeTopBar(
    clockTime: String,
    unreadCount: Int,
    onClearAll: () -> Unit,
    onCloseShade: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceElevated.copy(alpha = 0.85f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onCloseShade,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kapat",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = null,
                        tint = AccentBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Bildirim Merkezi",
                        fontFamily = BarlowCondensedFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = if (unreadCount > 0) "$unreadCount okunmamış bildirim • $clockTime" else "Bildirim yok • $clockTime",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        if (unreadCount > 0) {
            TextButton(
                onClick = onClearAll,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ClearAll,
                    contentDescription = null,
                    tint = Negative,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Tümünü Temizle",
                    fontFamily = OutfitFontFamily,
                    fontSize = 12.sp,
                    color = Negative,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Filtre Sekmeleri
 */
@Composable
private fun NotificationCategoryFilterTabs(
    selectedTab: String,
    allCount: Int,
    messagesCount: Int,
    socialCount: Int,
    romanceCount: Int,
    onSelectTab: (String) -> Unit
) {
    val tabs = listOf(
        Triple("ALL", "Tümü ($allCount)", Icons.Filled.Notifications),
        Triple("MESSAGES", "Mesajlar ($messagesCount)", Icons.AutoMirrored.Filled.Chat),
        Triple("SOCIAL", "Sosyal ($socialCount)", Icons.Filled.Public),
        Triple("ROMANCE", "Aşk & DM ($romanceCount)", Icons.Filled.Favorite)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { (id, label, icon) ->
            val isSelected = selectedTab == id
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) AccentPrimary else SurfacePrimary,
                border = BorderStroke(1.dp, if (isSelected) AccentBright else StrokePremium),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectTab(id) }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color(0xFF04150D) else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = label,
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFF04150D) else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Bildirim Yok Durumu
 */
@Composable
private fun NotificationEmptyPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DoneAll,
                    contentDescription = null,
                    tint = AccentBright,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tüm Bildirimler Temizlendi",
                fontFamily = BarlowCondensedFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Şu anda bekleyen yeni bir mesaj, DM veya haber bulunmuyor.",
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

/**
 * CareerSaveState'teki verilerden varsayılan bildirimleri oluşturur.
 */
fun generateNotificationsFromState(state: CareerSaveState): List<PhoneNotificationItem> {
    val list = mutableListOf<PhoneNotificationItem>()

    // Bekleyen aramalar
    state.callRecords.filter { !it.completed }.forEach { call ->
        list.add(
            PhoneNotificationItem(
                id = "notif_call_${call.id}",
                title = "Bekleyen Arama: ${call.callerName}",
                message = "${call.topic} - Görüşmek için araman bekleniyor.",
                timeFormatted = "Şimdi",
                appType = PhoneAppId.CALLS,
                category = "CALL",
                avatarHex = "#22C55E",
                isUrgent = true
            )
        )
    }

    // WhatsApp okunmamış mesajları
    state.npcs.filter { it.unreadCount > 0 }.forEach { npc ->
        list.add(
            PhoneNotificationItem(
                id = "notif_msg_${npc.id}",
                title = "WhatsApp: ${npc.name}",
                message = npc.messages.lastOrNull()?.text ?: "Yeni bir mesaj gönderdi.",
                timeFormatted = "5d",
                appType = PhoneAppId.MESSAGES,
                category = "WHATSAPP",
                avatarHex = "#25D366",
                secondaryText = "${npc.unreadCount} yeni mesaj"
            )
        )
    }

    // Instagram DM istekleri
    state.instaDmThreads.filter { it.unreadCount > 0 }.forEach { dm ->
        list.add(
            PhoneNotificationItem(
                id = "notif_dm_${dm.id}",
                title = "Instagram: ${dm.senderHandle}",
                message = dm.messages.lastOrNull()?.text ?: "Yeni bir mesaj gönderdi.",
                timeFormatted = "15d",
                appType = PhoneAppId.INSTAPITCH,
                category = "INSTAGRAM",
                avatarHex = "#E1306C",
                secondaryText = "Direkt Mesaj"
            )
        )
    }

    // Yaklaşan Maç Hatırlatıcısı
    val nextMatch = state.nextUnplayedUserFixture
    if (nextMatch != null) {
        list.add(
            PhoneNotificationItem(
                id = "notif_match_${nextMatch.id}",
                title = "Maç Günü Hatırlatıcısı",
                message = "${nextMatch.homeClubName} vs ${nextMatch.awayClubName} karşılaşması yaklaşıyor.",
                timeFormatted = "Bugün",
                appType = PhoneAppId.NEWS,
                category = "NEWS",
                avatarHex = "#2563EB",
                secondaryText = "Taktik ve kondisyonunu kontrol et!"
            )
        )
    }

    return list
}
