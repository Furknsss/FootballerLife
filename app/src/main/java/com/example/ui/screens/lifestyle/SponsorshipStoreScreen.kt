package com.example.ui.screens.lifestyle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.lifestyle.ActiveClubCrisis
import com.example.engine.lifestyle.BootContractOffer
import com.example.engine.lifestyle.BootModel
import com.example.engine.lifestyle.CommercialDealOffer
import com.example.engine.lifestyle.ContractOfferStatus
import com.example.engine.lifestyle.CrisisChoiceOption
import com.example.engine.lifestyle.CrisisSeverity
import com.example.engine.lifestyle.SponsorshipAndCrisisEngine
import com.example.engine.lifestyle.SponsorshipAndCrisisState
import com.example.engine.lifestyle.SponsorshipBrand
import com.example.engine.social.PlayerFameTier
import com.example.model.Club
import com.example.model.PlayerProfile
import com.example.ui.components.ClubCrestBadge
import com.example.ui.components.FLFormatters
import com.example.ui.components.FLTopBar
import com.example.ui.components.parseHexColor
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.BrightGold
import com.example.ui.theme.GoldBorder
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
 * FOOTBALL LIFE 2.0 — SPONSORSHIP STORE & CRISIS MANAGEMENT SCREEN
 */
@Composable
fun SponsorshipStoreScreen(
    player: PlayerProfile,
    club: Club,
    state: SponsorshipAndCrisisState,
    managerTrust: Int,
    fanSupport: Int,
    onBack: () -> Unit,
    onSignBootDeal: (offerId: String) -> Unit,
    onEquipBoot: (boot: BootModel) -> Unit,
    onCustomizeBootColor: (colorHex: String) -> Unit,
    onSignCommercialDeal: (dealId: String) -> Unit,
    onResolveCrisis: (crisisId: String, choiceId: String) -> Unit,
    onSimulateCrisisDebug: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val fameTier = SponsorshipAndCrisisEngine.calculatePlayerFameTier(player)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("fl_screen_sponsorship_store")
    ) {
        // Top Navigation Bar
        FLTopBar(
            title = "SPONSORLUK & KRİZ MERKEZİ",
            subtitle = "${fameTier.badgeSymbol} ${fameTier.titleTr} • ${FLFormatters.formatCompactNumber(player.followersCount.toLong())} Takipçi",
            onBack = onBack,
            trailingContent = {
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GoldBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Paid,
                            contentDescription = null,
                            tint = BrightGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = FLFormatters.formatCurrency(state.cumulativeSponsorshipEarningsEur),
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = BrightGold
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Screen Segment Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val hasActiveCrisis = state.activeCrises.isNotEmpty()
            val tabs = listOf(
                "👟 Krampon & Markalar",
                "🌍 Reklam Anlaşmaları",
                if (hasActiveCrisis) "🚨 Kriz Masası (${state.activeCrises.size})" else "🛡️ Kriz Masası"
            )

            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                val badgeColor = if (index == 2 && hasActiveCrisis) Negative else AccentPrimary

                Surface(
                    onClick = { selectedTab = index },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) badgeColor.copy(alpha = 0.22f) else SurfacePrimary,
                    border = BorderStroke(
                        1.2.dp,
                        if (isSelected) badgeColor else StrokePremium
                    ),
                    modifier = Modifier.testTag("fl_tab_sponsorship_$index")
                ) {
                    Text(
                        text = title,
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> BootsAndBrandsTab(
                    player = player,
                    club = club,
                    state = state,
                    fameTier = fameTier,
                    onSignBootDeal = onSignBootDeal,
                    onEquipBoot = onEquipBoot,
                    onCustomizeBootColor = onCustomizeBootColor
                )
                1 -> CommercialDealsTab(
                    player = player,
                    state = state,
                    fameTier = fameTier,
                    onSignCommercialDeal = onSignCommercialDeal
                )
                2 -> CrisisManagementTab(
                    player = player,
                    club = club,
                    state = state,
                    managerTrust = managerTrust,
                    fanSupport = fanSupport,
                    onResolveCrisis = onResolveCrisis,
                    onSimulateCrisisDebug = onSimulateCrisisDebug
                )
            }
        }
    }
}

// =========================================================================
// TAB 0: KRAMPON & DEV SPONSORLAR
// =========================================================================

@Composable
private fun BootsAndBrandsTab(
    player: PlayerProfile,
    club: Club,
    state: SponsorshipAndCrisisState,
    fameTier: PlayerFameTier,
    onSignBootDeal: (offerId: String) -> Unit,
    onEquipBoot: (boot: BootModel) -> Unit,
    onCustomizeBootColor: (colorHex: String) -> Unit
) {
    val equippedBoot = state.equippedBoot
    val activeDeal = state.activeBootDeal

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. ACTIVE EQUIPPED BOOT SHOWCASE
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfacePrimary,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.4.dp, GoldBorder)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    SurfaceElevated,
                                    SurfacePrimary,
                                    BackgroundPrimary.copy(alpha = 0.95f)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = parseHexColor(equippedBoot.brand.primaryColorHex).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, parseHexColor(equippedBoot.brand.primaryColorHex))
                            ) {
                                Text(
                                    text = equippedBoot.brand.logoSymbol,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = parseHexColor(equippedBoot.brand.primaryColorHex),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "SAHADA GİYİLEN KRAMPON",
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.8.sp,
                                    color = BrightGold
                                )
                                Text(
                                    text = equippedBoot.modelName,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 19.sp,
                                    color = TextPrimary
                                )
                            }
                        }

                        Surface(
                            color = AccentPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, AccentPrimary)
                        ) {
                            Text(
                                text = equippedBoot.tier.labelTr,
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AccentBright,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2D/2.5D Custom Football Boot Vector Graphic
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BackgroundPrimary.copy(alpha = 0.75f))
                            .border(1.dp, StrokePremium, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        FootballBootCanvas(
                            primaryHex = state.customBootColorHex ?: equippedBoot.primaryColorHex,
                            secondaryHex = equippedBoot.secondaryColorHex,
                            accentHex = equippedBoot.accentColorHex,
                            brand = equippedBoot.brand,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Upper Material & Stat Boost Summary
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${equippedBoot.upperMaterial} • ${equippedBoot.weightGrams}g",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Surface(
                            color = BackgroundPrimary.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, GoldBorder)
                        ) {
                            Text(
                                text = equippedBoot.statBoostSummary,
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = BrightGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Active Contract Info Pill
                    if (activeDeal != null && activeDeal.offerStatus == ContractOfferStatus.SIGNED_ACTIVE) {
                        Surface(
                            color = SurfaceElevated.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, StrokePremiumStrong),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Aktif Sponsorluk: ${activeDeal.brand.brandName}",
                                        fontFamily = BarlowCondensedFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = AccentBright
                                    )
                                    Text(
                                        text = "Kalan: ${activeDeal.weeksRemaining} Hafta",
                                        fontFamily = BarlowCondensedFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = BrightGold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    ContractStatChip("Haftalık Prim", FLFormatters.formatCurrency(activeDeal.weeklySponsorshipWageEur))
                                    ContractStatChip("Gol Bonusu", "+${FLFormatters.formatCurrency(activeDeal.goalBonusEur)}")
                                    ContractStatChip("Derbi Galibiyeti", "+${FLFormatters.formatCurrency(activeDeal.derbyWinBonusEur)}")
                                }
                            }
                        }
                    } else {
                        Surface(
                            color = Negative.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Negative.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Negative,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Aktif marka sözleşmen yok. Kendi kramponunla oynuyorsun. Aşağıdaki dev markalardan birini imzala!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    // Color Customization Swatches (if unlocked)
                    if (equippedBoot.isCustomColorUnlocked || (activeDeal?.isCustomEditionGranted == true)) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Özel Krampon Renk Paleti (İmzalı Sürüm):",
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val colors = listOf(
                                "#E02424" to "Ateş Kırmızı",
                                "#10B981" to "Neon Yeşil",
                                "#FACC15" to "Altın Sarısı",
                                "#0284C7" to "Okyanus Mavi",
                                "#18181B" to "Gece Siyahı",
                                "#FFFFFF" to "Saf Beyaz",
                                "#E11D48" to "Fuşya Gül"
                            )
                            colors.forEach { (hex, name) ->
                                val isSelected = (state.customBootColorHex ?: equippedBoot.primaryColorHex).equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(parseHexColor(hex))
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) BrightGold else StrokePremium,
                                            shape = CircleShape
                                        )
                                        .clickable { onCustomizeBootColor(hex) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = name,
                                            tint = if (hex == "#FFFFFF" || hex == "#FACC15") Color.Black else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. BRAND SPONSORSHIP CONTRACT OFFERS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GELEN MARKA TEKLİFLERİ",
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp,
                    color = TextPrimary
                )
                Text(
                    text = "${state.availableBootOffers.size} Teklif Hazır",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentBright
                )
            }
        }

        items(state.availableBootOffers, key = { it.id }) { offer ->
            BootOfferCard(
                offer = offer,
                player = player,
                fameTier = fameTier,
                onSign = { onSignBootDeal(offer.id) }
            )
        }

        if (state.availableBootOffers.isEmpty()) {
            item {
                Surface(
                    color = SurfacePrimary,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, StrokePremium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checkroom,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Şu anda yeni krampon teklifi bulunmuyor.",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Maçlarda gol atarak ve takipçi sayını artırarak dev markaların dikkatini çek!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BootOfferCard(
    offer: BootContractOffer,
    player: PlayerProfile,
    fameTier: PlayerFameTier,
    onSign: () -> Unit
) {
    val brand = offer.brand
    val boot = offer.bootModel
    val isEligible = fameTier >= offer.minFameTierRequired && player.overallRating >= offer.minOverallRequired

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfacePrimary,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.2.dp,
            if (isEligible) parseHexColor(brand.primaryColorHex).copy(alpha = 0.7f) else StrokePremium
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Brand & Boot Model Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = parseHexColor(brand.primaryColorHex).copy(alpha = 0.18f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, parseHexColor(brand.primaryColorHex))
                    ) {
                        Text(
                            text = brand.logoSymbol,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = parseHexColor(brand.primaryColorHex),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = brand.brandName,
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = boot.modelName,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = BrightGold
                        )
                    }
                }

                Surface(
                    color = BackgroundPrimary,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, StrokePremium)
                ) {
                    Text(
                        text = "Prestij: %${brand.prestigeScore}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stat Boost Pill & Boot Description
            Text(
                text = boot.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    color = BackgroundPrimary,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, GoldBorder)
                ) {
                    Text(
                        text = boot.statBoostSummary,
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BrightGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                if (offer.isCustomEditionGranted) {
                    Surface(
                        color = AccentPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, AccentPrimary)
                    ) {
                        Text(
                            text = "🎨 Özel Renk İzni",
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AccentBright,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Breakdown Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BackgroundPrimary.copy(alpha = 0.65f))
                    .border(1.dp, StrokePremium, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Haftalık Prim", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = FLFormatters.formatCurrency(offer.weeklySponsorshipWageEur),
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AccentBright
                    )
                }
                Column {
                    Text(text = "Gol Başı Prim", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = "+${FLFormatters.formatCurrency(offer.goalBonusEur)}",
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = BrightGold
                    )
                }
                Column {
                    Text(text = "İmza Parası", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = FLFormatters.formatCurrency(offer.signingBonusEur),
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                }
                Column {
                    Text(text = "Süre", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = "${offer.contractWeeks} Hafta",
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button
            if (isEligible) {
                Button(
                    onClick = onSign,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = parseHexColor(brand.primaryColorHex)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${brand.brandName} İle İmzala ve Sahaya Çık",
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            } else {
                Surface(
                    color = BackgroundSecondary,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, StrokePremium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kilitli: En az ${offer.minFameTierRequired.titleTr} kademesi ve ${offer.minOverallRequired} OVR gerekli",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 1: GLOBAL REKLAM ANLAŞMALARI
// =========================================================================

@Composable
private fun CommercialDealsTab(
    player: PlayerProfile,
    state: SponsorshipAndCrisisState,
    fameTier: PlayerFameTier,
    onSignCommercialDeal: (dealId: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Player Fame & Follower Status Banner
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfacePrimary,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.2.dp, GoldBorder)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(SurfaceElevated, SurfacePrimary)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ŞÖHRET VE MEDYA GÜCÜ",
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BrightGold
                            )
                            Text(
                                text = "${fameTier.badgeSymbol} ${fameTier.titleTr}",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            color = BackgroundPrimary,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, StrokePremium)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(text = "Toplam Takipçi", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                Text(
                                    text = FLFormatters.formatCompactNumber(player.followersCount.toLong()),
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = AccentBright
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Global markalar sosyal medyadaki erişimine ve sahada sergilediğin performansa göre teklif sunar. Yüksek takipçi sayısı, dev reklam anlaşmalarının kapısını açar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Active Commercial Deals
        if (state.activeCommercialDeals.isNotEmpty()) {
            item {
                Text(
                    text = "YÜRÜRLÜKTEKİ REKLAM ANLAŞMALARIN (${state.activeCommercialDeals.size})",
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = AccentBright
                )
            }

            items(state.activeCommercialDeals, key = { it.id }) { deal ->
                ActiveDealCard(deal = deal)
            }
        }

        // Available Deals
        item {
            Text(
                text = "YENİ REKLAM VE MARKA ELÇİLİĞİ FIRSATLARI",
                fontFamily = BarlowCondensedFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = TextPrimary
            )
        }

        items(state.availableCommercialOffers, key = { it.id }) { deal ->
            CommercialOfferCard(
                deal = deal,
                player = player,
                fameTier = fameTier,
                onSign = { onSignCommercialDeal(deal.id) }
            )
        }

        if (state.availableCommercialOffers.isEmpty()) {
            item {
                Surface(
                    color = SurfacePrimary,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, StrokePremium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Şu anda uygun yeni reklam teklifi bulunmuyor.",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Daha büyük bir şöhret kademesine yükseldiğinde global reklam ajansları seninle iletişime geçecek.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveDealCard(deal: CommercialDealOffer) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfacePrimary,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.2.dp, parseHexColor(deal.brandColorHex))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = deal.category.iconSymbol, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = deal.brandName,
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = deal.dealTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = BrightGold
                        )
                    }
                }

                Surface(
                    color = BackgroundPrimary,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, StrokePremium)
                ) {
                    Text(
                        text = "Kalan: ${deal.weeksRemaining} Hafta",
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BrightGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Haftalık Ödeme: ${FLFormatters.formatCurrency(deal.weeklyCampaignPayoutEur)}",
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AccentBright
                )
                Text(
                    text = "Takipçi Artış Çarpanı: +%${deal.followerGrowthMultiplierPct}",
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BrightGold
                )
            }
        }
    }
}

@Composable
private fun CommercialOfferCard(
    deal: CommercialDealOffer,
    player: PlayerProfile,
    fameTier: PlayerFameTier,
    onSign: () -> Unit
) {
    val isEligible = fameTier >= deal.requiredFameTier && player.followersCount >= (deal.requiredFollowers * 0.7)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfacePrimary,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.2.dp,
            if (isEligible) parseHexColor(deal.brandColorHex).copy(alpha = 0.7f) else StrokePremium
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = deal.category.iconSymbol, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = deal.brandName,
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = deal.dealTitle,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = BrightGold
                        )
                    }
                }

                Surface(
                    color = BackgroundPrimary,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, StrokePremium)
                ) {
                    Text(
                        text = deal.category.labelTr,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = deal.campaignNarrative,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Deal Economics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BackgroundPrimary.copy(alpha = 0.65f))
                    .border(1.dp, StrokePremium, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Peşin Avans", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = FLFormatters.formatCurrency(deal.upfrontAdvancePayoutEur),
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = BrightGold
                    )
                }
                Column {
                    Text(text = "Haftalık Reklam", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = FLFormatters.formatCurrency(deal.weeklyCampaignPayoutEur),
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AccentBright
                    )
                }
                Column {
                    Text(text = "Büyüme Desteği", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text(
                        text = "+%${deal.followerGrowthMultiplierPct}",
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isEligible) {
                Button(
                    onClick = onSign,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = parseHexColor(deal.brandColorHex)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${deal.brandName} Reklam Sözleşmesini İmzala",
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            } else {
                Surface(
                    color = BackgroundSecondary,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, StrokePremium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kilitli: En az ${deal.requiredFameTier.titleTr} ve ${FLFormatters.formatCompactNumber(deal.requiredFollowers)} Takipçi",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 2: KRİZ MASASI & TARAFTAR TEPKİLERİ
// =========================================================================

@Composable
private fun CrisisManagementTab(
    player: PlayerProfile,
    club: Club,
    state: SponsorshipAndCrisisState,
    managerTrust: Int,
    fanSupport: Int,
    onResolveCrisis: (crisisId: String, choiceId: String) -> Unit,
    onSimulateCrisisDebug: (() -> Unit)? = null
) {
    val activeCrisis = state.activeCrises.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Club & Fan Pressure Atmosphere Barometer
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfacePrimary,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.2.dp, if (activeCrisis != null) Negative else StrokePremium)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(SurfaceElevated, SurfacePrimary)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "KULÜP & TARAFTAR BAROMETRESİ",
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BrightGold
                            )
                            Text(
                                text = if (activeCrisis != null) "🚨 KRİZ MODU DEVREDE" else "🛡️ KULÜPTE DURUM SAKİN",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp,
                                color = if (activeCrisis != null) Negative else AccentBright
                            )
                        }

                        ClubCrestBadge(
                            shortName = club.shortName,
                            primaryHex = club.primaryColorHex,
                            secondaryHex = club.secondaryColorHex,
                            size = 42.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Gauges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PressureGaugePill(
                            title = "Teknik Direktör Güveni",
                            value = managerTrust,
                            accentColor = if (managerTrust > 50) AccentBright else Negative
                        )
                        PressureGaugePill(
                            title = "Tribün & Taraftar Desteği",
                            value = fanSupport,
                            accentColor = if (fanSupport > 50) AccentBright else Negative
                        )
                    }

                    // Debug Simulation Button (For immediate testing)
                    if (activeCrisis == null && onSimulateCrisisDebug != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = onSimulateCrisisDebug,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Negative.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ Kriz Senaryosu Tetikle (Simülasyon)",
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Negative
                            )
                        }
                    }
                }
            }
        }

        // Active Crisis Section (If ongoing)
        if (activeCrisis != null) {
            item {
                ActiveCrisisDilemmaCard(
                    crisis = activeCrisis,
                    onSelectChoice = { choiceId -> onResolveCrisis(activeCrisis.id, choiceId) }
                )
            }
        }

        // Resolved Crises Archive
        if (state.resolvedCrisesArchive.isNotEmpty()) {
            item {
                Text(
                    text = "GEÇMİŞ KRİZLER & ÇÖZÜM ARŞİVİ (${state.resolvedCrisesArchive.size})",
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
            }

            items(state.resolvedCrisesArchive.reversed(), key = { it.id }) { crisis ->
                ResolvedCrisisCard(crisis = crisis)
            }
        }
    }
}

@Composable
private fun ActiveCrisisDilemmaCard(
    crisis: ActiveClubCrisis,
    onSelectChoice: (choiceId: String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfacePrimary,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(2.dp, Negative)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Negative.copy(alpha = 0.18f),
                            SurfacePrimary,
                            BackgroundPrimary
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Crisis Severity Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = parseHexColor(crisis.severity.colorHex).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, parseHexColor(crisis.severity.colorHex))
                ) {
                    Text(
                        text = "${crisis.crisisType.iconEmoji} ${crisis.severity.labelTr.uppercase()}",
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = parseHexColor(crisis.severity.colorHex),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = "Öfke Endeksi: %${crisis.fanAngerScore}",
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Negative
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = crisis.headline,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 19.sp,
                lineHeight = 24.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = crisis.contextNarrative,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Trending Hashtags
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                crisis.trendingHashtags.forEach { tag ->
                    Surface(
                        color = BackgroundPrimary,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, StrokePremium)
                    ) {
                        Text(
                            text = tag,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AccentBright,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "KRİZ MÜDAHALE SEÇENEĞİNİ BELİRLE:",
                fontFamily = BarlowCondensedFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = BrightGold
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Choices
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                crisis.choices.forEach { choice ->
                    CrisisChoiceButtonCard(
                        choice = choice,
                        onClick = { onSelectChoice(choice.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CrisisChoiceButtonCard(
    choice: CrisisChoiceOption,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = SurfaceElevated,
        border = BorderStroke(1.2.dp, StrokePremiumStrong),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = choice.buttonTitle,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "\"${choice.playerSpokenQuote}\"",
                style = MaterialTheme.typography.bodySmall,
                color = AccentBright
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = choice.detailedDescription,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Impact Pills Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (choice.managerTrustDelta != 0) {
                    DeltaStatPill("Hoca", choice.managerTrustDelta)
                }
                if (choice.fanSupportDelta != 0) {
                    DeltaStatPill("Taraftar", choice.fanSupportDelta)
                }
                if (choice.teamChemistryDelta != 0) {
                    DeltaStatPill("Takım", choice.teamChemistryDelta)
                }
                if (choice.outOfSquadRiskPct > 0) {
                    Surface(
                        color = Negative.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Negative)
                    ) {
                        Text(
                            text = "Kadro Dışı Riski: %${choice.outOfSquadRiskPct}",
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Negative,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeltaStatPill(label: String, delta: Int) {
    val isPositive = delta > 0
    val color = if (isPositive) AccentBright else Negative
    val sign = if (isPositive) "+$delta" else "$delta"

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = "$label: $sign",
            fontFamily = BarlowCondensedFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun ResolvedCrisisCard(crisis: ActiveClubCrisis) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SurfacePrimary,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, StrokePremium)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = crisis.crisisType.titleTr,
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = BrightGold
                )
                Text(
                    text = "Çözüldü ✅",
                    style = MaterialTheme.typography.labelSmall,
                    color = AccentBright
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = crisis.headline,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = crisis.resolutionOutcomeText ?: "Kriz yatıştırıldı.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun PressureGaugePill(
    title: String,
    value: Int,
    accentColor: Color
) {
    Surface(
        color = BackgroundPrimary,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, StrokePremium),
        modifier = Modifier.width(160.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "%${value.coerceIn(0, 100)}",
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = accentColor
                )
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = (value.coerceIn(0, 100) / 100f))
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(accentColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun ContractStatChip(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(
            text = value,
            fontFamily = BarlowCondensedFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = BrightGold
        )
    }
}

// =========================================================================
// CUSTOM 2.5D VECTOR SOCCER BOOT CANVAS
// =========================================================================

@Composable
fun FootballBootCanvas(
    primaryHex: String,
    secondaryHex: String,
    accentHex: String,
    brand: SponsorshipBrand,
    modifier: Modifier = Modifier
) {
    val primaryColor = parseHexColor(primaryHex, Color(0xFFE02424))
    val secondaryColor = parseHexColor(secondaryHex, Color(0xFF111827))
    val accentColor = parseHexColor(accentHex, Color(0xFFFACC15))

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val cx = w * 0.5f
        val cy = h * 0.52f

        // Sole Plate / Studs (Outsole with Carbon / Plastic Studs)
        val solePath = Path().apply {
            moveTo(cx - w * 0.38f, cy + h * 0.22f)
            cubicTo(
                cx - w * 0.15f, cy + h * 0.25f,
                cx + w * 0.25f, cy + h * 0.20f,
                cx + w * 0.42f, cy + h * 0.08f
            )
            lineTo(cx + w * 0.40f, cy + h * 0.15f)
            cubicTo(
                cx + w * 0.22f, cy + h * 0.28f,
                cx - w * 0.15f, cy + h * 0.32f,
                cx - w * 0.38f, cy + h * 0.28f
            )
            close()
        }
        drawPath(
            path = solePath,
            brush = Brush.linearGradient(
                listOf(Color(0xFF18181B), Color(0xFF3F3F46), Color(0xFF09090B))
            )
        )

        // Studs (Cleats)
        val studColor = accentColor
        val studPositions = listOf(
            Offset(cx - w * 0.33f, cy + h * 0.28f),
            Offset(cx - w * 0.24f, cy + h * 0.30f),
            Offset(cx + w * 0.05f, cy + h * 0.27f),
            Offset(cx + w * 0.18f, cy + h * 0.24f),
            Offset(cx + w * 0.32f, cy + h * 0.18f)
        )
        studPositions.forEach { pos ->
            drawRoundRect(
                color = studColor,
                topLeft = pos,
                size = Size(w * 0.035f, h * 0.14f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }

        // Boot Upper Silhouette
        val upperPath = Path().apply {
            moveTo(cx - w * 0.38f, cy + h * 0.22f)
            // Heel curve up
            cubicTo(
                cx - w * 0.40f, cy + h * 0.02f,
                cx - w * 0.35f, cy - h * 0.18f,
                cx - w * 0.22f, cy - h * 0.28f
            )
            // Collar / Sockliner opening
            cubicTo(
                cx - w * 0.10f, cy - h * 0.22f,
                cx - w * 0.02f, cy - h * 0.10f,
                cx + w * 0.08f, cy - h * 0.02f
            )
            // Instep down to toe box
            cubicTo(
                cx + w * 0.22f, cy + h * 0.03f,
                cx + w * 0.36f, cy + h * 0.05f,
                cx + w * 0.42f, cy + h * 0.08f
            )
            // Sole connection back to heel
            cubicTo(
                cx + w * 0.25f, cy + h * 0.20f,
                cx - w * 0.15f, cy + h * 0.25f,
                cx - w * 0.38f, cy + h * 0.22f
            )
            close()
        }

        drawPath(
            path = upperPath,
            brush = Brush.horizontalGradient(
                listOf(
                    primaryColor.copy(alpha = 0.88f),
                    primaryColor,
                    primaryColor.copy(alpha = 0.95f)
                )
            )
        )

        // Boot Dynamic Brand Graphics
        when (brand) {
            SponsorshipBrand.NIKE -> {
                // Swoosh mark
                val swoosh = Path().apply {
                    moveTo(cx - w * 0.12f, cy + h * 0.06f)
                    cubicTo(
                        cx + w * 0.08f, cy + h * 0.10f,
                        cx + w * 0.24f, cy + h * 0.06f,
                        cx + w * 0.34f, cy - h * 0.06f
                    )
                    cubicTo(
                        cx + w * 0.26f, cy + h * 0.02f,
                        cx + w * 0.06f, cy + h * 0.04f,
                        cx - w * 0.12f, cy + h * 0.06f
                    )
                    close()
                }
                drawPath(path = swoosh, color = accentColor)
            }
            SponsorshipBrand.ADIDAS -> {
                // 3 Iconic diagonal stripes
                for (i in 0..2) {
                    val offset = i * (w * 0.05f)
                    drawLine(
                        color = accentColor,
                        start = Offset(cx - w * 0.08f + offset, cy - h * 0.04f),
                        end = Offset(cx + w * 0.02f + offset, cy + h * 0.16f),
                        strokeWidth = w * 0.025f
                    )
                }
            }
            SponsorshipBrand.PUMA -> {
                // Puma Formstrip curve
                val formstrip = Path().apply {
                    moveTo(cx - w * 0.28f, cy + h * 0.08f)
                    cubicTo(
                        cx - w * 0.10f, cy + h * 0.02f,
                        cx + w * 0.12f, cy + h * 0.08f,
                        cx + w * 0.32f, cy + h * 0.12f
                    )
                }
                drawPath(
                    path = formstrip,
                    color = accentColor,
                    style = Stroke(width = w * 0.038f)
                )
            }
            else -> {
                // Elegant racing stripes
                drawLine(
                    color = accentColor,
                    start = Offset(cx - w * 0.20f, cy + h * 0.02f),
                    end = Offset(cx + w * 0.25f, cy + h * 0.10f),
                    strokeWidth = w * 0.022f
                )
            }
        }

        // Lacing Eyelets & Texture
        val eyelets = listOf(
            Offset(cx + w * 0.02f, cy - h * 0.02f),
            Offset(cx + w * 0.08f, cy + h * 0.01f),
            Offset(cx + w * 0.14f, cy + h * 0.03f),
            Offset(cx + w * 0.20f, cy + h * 0.05f)
        )
        eyelets.forEach { pt ->
            drawCircle(color = secondaryColor, radius = w * 0.012f, center = pt)
            drawCircle(color = Color.White, radius = w * 0.006f, center = pt)
        }

        // Subtle upper highlight stroke
        drawPath(
            path = upperPath,
            color = Color.White.copy(alpha = 0.25f),
            style = Stroke(width = 2f)
        )
    }
}
