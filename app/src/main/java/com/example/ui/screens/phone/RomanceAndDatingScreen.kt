package com.example.ui.screens.phone

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.PartyMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.social.DatingActionItem
import com.example.engine.social.DatingInteractionCategory
import com.example.engine.social.GirlfriendProfile
import com.example.engine.social.PaparazziDramaIncident
import com.example.engine.social.PlayerFameTier
import com.example.engine.social.RelationshipStage
import com.example.engine.social.RomanceAndDatingEngine
import com.example.engine.social.RomanceDmInvitation
import com.example.engine.social.RomanceInteractionOutcome
import com.example.model.CareerSaveState
import com.example.ui.components.FLFormatters
import com.example.ui.components.parseSafeHexColor
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

/**
 * Kız Arkadaş, Şöhret/Cazibe ve Magazin/Sevgililik Hayatı Ekranı.
 * - Oyuncunun şöhret kademesine (PlayerFameTier) göre ünlü manken, oyuncu ve fenomenlerden gelen Instagram DM'leri
 * - İlişki Aşamaları: Tanışma -> Flört -> Sevgililik -> Nişanlılık
 * - Romantik akşam yemeği, özel hediyeler (mücevher, çanta, çiçek), Bodrum/Maldivler tatili, VIP maç daveti (tribün desteği bonusu)
 * - Magazin basını, paparazzi haberleri, hoca tepkisi ve form/moral dengeleri
 */
@Composable
fun RomanceAndDatingScreen(
    state: CareerSaveState,
    initialActivePartner: GirlfriendProfile? = null,
    onBackHome: () -> Unit,
    onPartnerUpdated: (GirlfriendProfile) -> Unit = {},
    onDatingOutcome: (RomanceInteractionOutcome) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val fameTier = remember(state.player) {
        RomanceAndDatingEngine.resolvePlayerFameTier(state.player)
    }

    // Aktif partner state
    var activePartner by remember {
        mutableStateOf(
            initialActivePartner ?: RomanceAndDatingEngine.INITIAL_CANDIDATES.firstOrNull { it.id == "partner_derin_karahan" }
        )
    }

    // Aday havuzu ve gelen DM istekleri
    val dmRequests = remember {
        val initialDms = RomanceAndDatingEngine.checkAndGenerateInboundDms(
            state = state,
            existingCandidates = RomanceAndDatingEngine.INITIAL_CANDIDATES,
            currentInboundDms = emptyList()
        )
        mutableStateListOf<RomanceDmInvitation>().apply { addAll(initialDms) }
    }

    // Magazin ve Paparazzi Olayları
    val paparazziIncidents = remember {
        mutableStateListOf<PaparazziDramaIncident>()
    }

    var selectedTab by remember { mutableStateOf("PARTNER") } // "PARTNER", "DMS", "MAGAZINE"
    var selectedActionForConfirm by remember { mutableStateOf<DatingActionItem?>(null) }
    var currentOutcomeDialog by remember { mutableStateOf<RomanceInteractionOutcome?>(null) }
    var isTribunBonusActive by remember { mutableStateOf(activePartner?.isAttendingNextMatch == true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1A0914),
                        BackgroundPrimary,
                        Color(0xFF0F0A1C)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Üst Başlık & Şöhret Kademesi Barı
            RomanceTopHeader(
                fameTier = fameTier,
                activePartnerName = activePartner?.fullName,
                onBack = onBackHome
            )

            // Sekme Seçici
            RomanceNavigationTabs(
                selectedTab = selectedTab,
                dmsCount = dmRequests.size,
                dramaCount = paparazziIncidents.size,
                onSelectTab = { selectedTab = it }
            )

            // Ana İçerik
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    "PARTNER" -> {
                        val currentPartner = activePartner
                        if (currentPartner == null) {
                            NoPartnerActiveView(
                                onExploreDms = { selectedTab = "DMS" }
                            )
                        } else {
                            ActivePartnerView(
                                partner = currentPartner,
                                isTribunBonusActive = isTribunBonusActive,
                                onSelectAction = { action ->
                                    selectedActionForConfirm = action
                                },
                                onResolveDrama = { type ->
                                    val (updated, note) = RomanceAndDatingEngine.resolveDramaAction(currentPartner, type)
                                    activePartner = updated
                                    onPartnerUpdated(updated)
                                }
                            )
                        }
                    }
                    "DMS" -> {
                        DmRequestsView(
                            dmRequests = dmRequests,
                            fameTier = fameTier,
                            onAcceptDm = { dm ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val newPartner = dm.candidate.copy(
                                    isActivePartner = true,
                                    stage = RelationshipStage.MEETING,
                                    affection = 30
                                )
                                activePartner = newPartner
                                dmRequests.remove(dm)
                                onPartnerUpdated(newPartner)
                                selectedTab = "PARTNER"
                            },
                            onDismissDm = { dm ->
                                dmRequests.remove(dm)
                            }
                        )
                    }
                    "MAGAZINE" -> {
                        MagazineRadarView(
                            incidents = paparazziIncidents,
                            fameTier = fameTier,
                            activePartner = activePartner
                        )
                    }
                }
            }
        }

        // Etkileşim Onay Dialogu
        selectedActionForConfirm?.let { action ->
            val partner = activePartner
            if (partner != null) {
                ActionConfirmationDialog(
                    action = action,
                    partner = partner,
                    onDismiss = { selectedActionForConfirm = null },
                    onConfirm = {
                        selectedActionForConfirm = null
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val outcome = RomanceAndDatingEngine.executeInteraction(state, partner, action)

                        val updatedPartner = partner.copy(
                            stage = outcome.currentStage,
                            affection = (partner.affection + outcome.affectionChange).coerceIn(0, 100),
                            dramaLevel = (partner.dramaLevel + outcome.dramaChange).coerceIn(0, 100),
                            isAttendingNextMatch = outcome.matchBonusActivated || partner.isAttendingNextMatch
                        )
                        activePartner = updatedPartner
                        if (outcome.matchBonusActivated) {
                            isTribunBonusActive = true
                        }
                        outcome.paparazziIncident?.let { scandal ->
                            paparazziIncidents.add(0, scandal)
                        }

                        onPartnerUpdated(updatedPartner)
                        onDatingOutcome(outcome)
                        currentOutcomeDialog = outcome
                    }
                )
            }
        }

        // Etkileşim Sonuç Bildirimi
        currentOutcomeDialog?.let { outcome ->
            OutcomeResultDialog(
                outcome = outcome,
                onDismiss = { currentOutcomeDialog = null }
            )
        }
    }
}

/**
 * Üst Başlık & Şöhret Barı
 */
@Composable
private fun RomanceTopHeader(
    fameTier: PlayerFameTier,
    activePartnerName: String?,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceElevated.copy(alpha = 0.90f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Aşk & Magazin Hayatı",
                        fontFamily = BarlowCondensedFontFamily,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "❤️",
                        fontSize = 15.sp
                    )
                }
                Text(
                    text = activePartnerName?.let { "Partner: $it" } ?: "İlişki durumu: Bekar",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        // Şöhret Rozeti
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE11D48).copy(alpha = 0.18f),
            border = BorderStroke(1.dp, Color(0xFFE11D48).copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = fameTier.badgeSymbol, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = fameTier.titleTr,
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFDA4AF)
                )
            }
        }
    }
}

/**
 * Sekme Seçici
 */
@Composable
private fun RomanceNavigationTabs(
    selectedTab: String,
    dmsCount: Int,
    dramaCount: Int,
    onSelectTab: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val tabs = listOf(
            Triple("PARTNER", "İlişkim", Icons.Filled.Favorite),
            Triple("DMS", "Flört & DM ($dmsCount)", Icons.Filled.Mail),
            Triple("MAGAZINE", "Magazin Radarı ($dramaCount)", Icons.Filled.Newspaper)
        )

        tabs.forEach { (id, title, icon) ->
            val isSelected = selectedTab == id
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) Color(0xFFE11D48) else SurfacePrimary,
                border = BorderStroke(1.dp, if (isSelected) Color(0xFFFDA4AF) else StrokePremium),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelectTab(id) }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = title,
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Aktif Partner Görünümü
 */
@Composable
private fun ActivePartnerView(
    partner: GirlfriendProfile,
    isTribunBonusActive: Boolean,
    onSelectAction: (DatingActionItem) -> Unit,
    onResolveDrama: (String) -> Unit
) {
    val partnerColor = parseSafeHexColor(partner.avatarHex, Color(0xFFEC4899))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 28.dp)
    ) {
        // 1. Partner Profil Kartı
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePrimary.copy(alpha = 0.95f)),
                border = BorderStroke(1.dp, partnerColor.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(partnerColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = partner.archetype.emoji,
                                    fontSize = 26.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = partner.fullName,
                                        fontFamily = BarlowCondensedFontFamily,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(${partner.age})",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 13.sp,
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = "${partner.handle} • ${partner.professionTr}",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFFFDA4AF)
                                )
                                Text(
                                    text = "${FLFormatters.formatFollowers(partner.followersCount.toInt())} Takipçi • ${partner.city}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Aşama Rozeti
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = partnerColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, partnerColor)
                        ) {
                            Text(
                                text = "${partner.stage.iconEmoji} ${partner.stage.titleTr}",
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bio ve Alıntı
                    Text(
                        text = "\"${partner.bio}\"",
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = StrokePremium)
                    Spacer(modifier = Modifier.height(14.dp))

                    // İlişki Aşamaları İlerleme Çubuğu
                    RelationshipStageStepIndicator(currentStage = partner.stage)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sevgi & Drama Göstergeleri
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Sevgi Barı
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Sevgi & Bağlılık",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "%${partner.affection}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF43F5E)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { partner.affectionPercentage },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFFF43F5E),
                                trackColor = SurfaceElevated
                            )
                        }

                        // Magazin & Drama Barı
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Magazin / Drama Riski",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "%${partner.dramaLevel}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (partner.dramaLevel > 50) Negative else GoldPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { partner.dramaPercentage },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (partner.dramaLevel > 50) Negative else GoldPrimary,
                                trackColor = SurfaceElevated
                            )
                        }
                    }
                }
            }
        }

        // 2. VIP Tribün Desteği Rozeti (Aktifse)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isTribunBonusActive) AccentPrimary.copy(alpha = 0.15f) else SurfaceElevated
                ),
                border = BorderStroke(1.dp, if (isTribunBonusActive) AccentBright else StrokePremium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isTribunBonusActive) AccentBright else SurfacePrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SportsSoccer,
                            contentDescription = null,
                            tint = if (isTribunBonusActive) Color(0xFF04150D) else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTribunBonusActive) "Tribün Desteği Aktif! 🎟️" else "VIP Maç Daveti Bekleniyor",
                            fontFamily = BarlowCondensedFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTribunBonusActive) AccentBright else TextPrimary
                        )
                        Text(
                            text = if (isTribunBonusActive)
                                "${partner.fullName} bir sonraki maçta VIP locada seni izleyecek! Sahada +5 Moral, +3 Bitiricilik ve özel gol sevinci bonusu kazandın."
                            else
                                "Sevgilini maça davet ederek tribünde seni izlemesini sağla, maçta motivasyon patlaması yaşa!",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // 3. Yüksek Drama Kriz Çözümü Banner'ı (Eğer drama > 50 ise)
        if (partner.dramaLevel >= 50) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Negative.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, Negative.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = Negative,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Magazin Basını & Ayrılık Krizi Uyarısı!",
                                fontFamily = OutfitFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Negative
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Son çıkan haberler ve ilgisizlik ilişkinizi yıpratıyor. Bir an önce durumu telafi etmelisin.",
                            fontFamily = OutfitFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onResolveDrama("FLOWERS") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Çiçek Gönder 🌸", fontSize = 11.sp)
                            }
                            Button(
                                onClick = { onResolveDrama("APOLOGY_DINNER") },
                                colors = ButtonDefaults.buttonColors(containerColor = Negative),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Özür Yemeği 🍷", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // 4. Etkileşim Seçenekleri Başlığı
        item {
            Text(
                text = "İlişki Etkileşimleri & Buluşmalar",
                fontFamily = BarlowCondensedFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // 5. Etkileşim Kartları
        val availableActions = RomanceAndDatingEngine.getAvailableActions(partner)
        items(availableActions, key = { it.id }) { action ->
            DatingActionCard(
                action = action,
                onClick = { onSelectAction(action) }
            )
        }
    }
}

/**
 * İlişki Aşamaları Adım Göstergesi (Tanışma -> Flört -> Sevgili -> Nişanlı)
 */
@Composable
private fun RelationshipStageStepIndicator(currentStage: RelationshipStage) {
    val stages = RelationshipStage.entries

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        stages.forEachIndexed { index, stage ->
            val isPassedOrCurrent = stage.levelOrder <= currentStage.levelOrder
            val isCurrent = stage == currentStage

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isPassedOrCurrent) Color(0xFFE11D48) else SurfaceElevated)
                        .then(
                            if (isCurrent) Modifier.background(
                                Brush.radialGradient(listOf(Color(0xFFFDA4AF), Color(0xFFE11D48)))
                            ) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stage.iconEmoji,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (stage) {
                        RelationshipStage.MEETING -> "Tanışma"
                        RelationshipStage.FLIRTING -> "Flört"
                        RelationshipStage.IN_RELATIONSHIP -> "Sevgililik"
                        RelationshipStage.ENGAGED -> "Nişanlılık"
                    },
                    fontFamily = OutfitFontFamily,
                    fontSize = 10.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isPassedOrCurrent) TextPrimary else TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Yapılabilecek Etkileşim Kartı
 */
@Composable
private fun DatingActionCard(
    action: DatingActionItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePrimary.copy(alpha = 0.90f)),
        border = BorderStroke(1.dp, StrokePremium),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Text(text = action.iconEmoji, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = action.titleTr,
                    fontFamily = OutfitFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = action.descriptionTr,
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Maliyet: ${FLFormatters.formatEur(action.costEur)}",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Text(
                        text = "• ${action.energyCost} Enerji",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "• +${action.affectionGain} Sevgi",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = Color(0xFFF43F5E)
                    )
                }
            }
        }
    }
}

/**
 * Gelen Instagram DM Tanışma İstekleri Görünümü
 */
@Composable
private fun DmRequestsView(
    dmRequests: List<RomanceDmInvitation>,
    fameTier: PlayerFameTier,
    onAcceptDm: (RomanceDmInvitation) -> Unit,
    onDismissDm: (RomanceDmInvitation) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated.copy(alpha = 0.85f)),
                border = BorderStroke(1.dp, StrokePremium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Stars,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Şöhret & Cazibe Radarı",
                            fontFamily = BarlowCondensedFontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Şöhret kademen: ${fameTier.titleTr} (${fameTier.badgeSymbol}). Sahadaki başarın, piyasa değerin ve takipçi sayın arttıkça ünlü modellerden ve aktrislerden DM tanışma istekleri gelir.",
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        if (dmRequests.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "💌", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Bekleyen DM İsteği Yok",
                            fontFamily = BarlowCondensedFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Maçlarda goller atıp şöhretini artırdıkça yeni ünlü modeller sana ulaşacak!",
                            fontFamily = OutfitFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(dmRequests, key = { it.id }) { dm ->
                DmInvitationCard(
                    dm = dm,
                    onAccept = { onAcceptDm(dm) },
                    onDismiss = { onDismissDm(dm) }
                )
            }
        }
    }
}

/**
 * DM İstek Kartı
 */
@Composable
private fun DmInvitationCard(
    dm: RomanceDmInvitation,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    val candidate = dm.candidate
    val cardColor = parseSafeHexColor(candidate.avatarHex, Color(0xFFE11D48))

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePrimary.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, cardColor.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(cardColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = candidate.archetype.emoji, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = candidate.fullName,
                            fontFamily = BarlowCondensedFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${candidate.handle} • ${candidate.professionTr}",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            color = Color(0xFFFDA4AF)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceElevated
                ) {
                    Text(
                        text = "${FLFormatters.formatFollowers(candidate.followersCount.toInt())} Takipçi",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // DM Mesajı
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceElevated.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\"${dm.openingMessage}\"",
                    fontFamily = OutfitFontFamily,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(10.dp),
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Aksiyon Butonları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reddet", fontSize = 12.sp)
                }

                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tanış & Yanıtla", fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Magazin Radarı & Paparazzi Haberleri Görünümü
 */
@Composable
private fun MagazineRadarView(
    incidents: List<PaparazziDramaIncident>,
    fameTier: PlayerFameTier,
    activePartner: GirlfriendProfile?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceElevated.copy(alpha = 0.85f)),
                border = BorderStroke(1.dp, StrokePremium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📸", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Magazin Basını & Paparazzi Radarı",
                            fontFamily = BarlowCondensedFontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Gece kulübü çıkışları, lüks tatiller ve hediyeler basının radarında! Dikkat et, aşırı magazin ilgisi teknik direktörün sabrını zorlayabilir.",
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        if (incidents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📰", fontSize = 42.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Henüz Magazin Skandalı Yok",
                            fontFamily = BarlowCondensedFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "İlişkiniz şu anda basından uzak ve sakin ilerliyor.",
                            fontFamily = OutfitFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(incidents, key = { it.id }) { scandal ->
                MagazineScandalCard(scandal = scandal)
            }
        }
    }
}

/**
 * Magazin Skandalı Kartı
 */
@Composable
private fun MagazineScandalCard(scandal: PaparazziDramaIncident) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfacePrimary.copy(alpha = 0.94f)),
        border = BorderStroke(1.dp, Negative.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Negative.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "MANŞET • ${scandal.mediaOutlet}",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Negative,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "${scandal.dayIndex}. Gün",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = scandal.headline,
                fontFamily = BarlowCondensedFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = scandal.detailsText,
                fontFamily = OutfitFontFamily,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Hoca Tepkisi Kutusu
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SurfaceElevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Hoca Uyarısı:",
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Text(
                        text = scandal.managerReaction,
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

/**
 * Henüz Aktif Partner Yoksa Gösterilen Ekran
 */
@Composable
private fun NoPartnerActiveView(onExploreDms: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "💔", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Şu Anda Bir İlişkin Yok",
                fontFamily = BarlowCondensedFontFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Şöhret kademen ve karizman yükseldikçe gelen Instagram DM'lerinden tanışma isteklerini kabul edebilirsin.",
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onExploreDms,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
            ) {
                Text("Gelen DM İsteklerini Gör", fontSize = 13.sp)
            }
        }
    }
}

/**
 * Aksiyon Onay Dialogu
 */
@Composable
private fun ActionConfirmationDialog(
    action: DatingActionItem,
    partner: GirlfriendProfile,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = action.iconEmoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = action.titleTr,
                    fontFamily = BarlowCondensedFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = action.descriptionTr,
                    fontFamily = OutfitFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = StrokePremium)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Ücret:", fontFamily = OutfitFontFamily, fontSize = 12.sp, color = TextSecondary)
                    Text(text = FLFormatters.formatEur(action.costEur), fontFamily = SpaceGroteskFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Enerji Tüketimi:", fontFamily = OutfitFontFamily, fontSize = 12.sp, color = TextSecondary)
                    Text(text = "${action.energyCost} Enerji", fontFamily = SpaceGroteskFontFamily, fontSize = 12.sp, color = TextPrimary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Paparazzi Riski:", fontFamily = OutfitFontFamily, fontSize = 12.sp, color = TextSecondary)
                    Text(text = "%${action.paparazziRiskPct}", fontFamily = SpaceGroteskFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (action.paparazziRiskPct > 40) Negative else GoldPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
            ) {
                Text("Gerçekleştir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        }
    )
}

/**
 * Sonuç Bildirim Dialogu
 */
@Composable
private fun OutcomeResultDialog(
    outcome: RomanceInteractionOutcome,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Celebration,
                    contentDescription = null,
                    tint = AccentBright,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = outcome.actionTitle,
                    fontFamily = BarlowCondensedFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                Text(
                    text = outcome.resultStoryText,
                    fontFamily = OutfitFontFamily,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = StrokePremium)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "• Sevgi: +${outcome.affectionChange}",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 12.sp,
                    color = Color(0xFFF43F5E),
                    fontWeight = FontWeight.Bold
                )
                if (outcome.matchBonusActivated) {
                    Text(
                        text = "• VIP Tribün Desteği Aktif (+${outcome.playerMoraleBonus} Moral Bonusu)",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        color = AccentBright,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Tamam")
            }
        }
    )
}
