package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BarlowCondensedFontFamily
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

@Composable
fun FLTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    backTestTag: String = "fl_btn_back",
    trailingContent: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (onBack != null) {
                Surface(
                    color = SurfacePrimary,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, StrokePremiumStrong),
                    modifier = Modifier.size(42.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .testTag(backTestTag)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = TextPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
            Column {
                Text(
                    text = title.uppercase(),
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    lineHeight = 30.sp,
                    letterSpacing = 0.6.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}

@Composable
fun FLSectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(3.5.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AccentPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title.uppercase(),
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 0.7.sp,
                    color = TextPrimary
                )
            }
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                )
            }
        }
        if (actionLabel != null && onActionClick != null) {
            Surface(
                color = SurfacePrimary,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, StrokePremium),
                modifier = Modifier.clickable { onActionClick() }
            ) {
                Text(
                    text = actionLabel,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = AccentBright,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
fun FLHeroPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = StrokePremiumStrong,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfacePrimary,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            SurfaceElevated.copy(alpha = 0.85f),
                            SurfacePrimary,
                            BackgroundSecondary
                        )
                    )
                )
        ) {
            // Subtle top coated-glass specular edge highlight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.22f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

@Composable
fun FLChartCard(
    title: String,
    subtitle: String? = null,
    rightBadgeText: String? = null,
    badgeText: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val resolvedBadge = rightBadgeText ?: badgeText
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SurfacePrimary,
        border = BorderStroke(1.dp, StrokePremiumStrong)
    ) {
        Column(
            modifier = Modifier.background(
                Brush.verticalGradient(
                    listOf(
                        SurfaceElevated.copy(alpha = 0.55f),
                        SurfacePrimary,
                        BackgroundSecondary.copy(alpha = 0.92f)
                    )
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.16f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title.uppercase(),
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 0.4.sp,
                            color = TextPrimary
                        )
                        if (!subtitle.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    if (!resolvedBadge.isNullOrBlank()) {
                        Surface(
                            color = BackgroundPrimary.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, StrokePremiumStrong)
                        ) {
                            Text(
                                text = resolvedBadge,
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = AccentBright,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                content()
            }
        }
    }
}

@Composable
fun FLSegmentedTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tagPrefix: String = "fl_tab"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { idx, tab ->
            val selected = idx == selectedIndex
            val bg by animateColorAsState(
                targetValue = if (selected) AccentPrimary else SurfacePrimary,
                animationSpec = tween(200),
                label = "seg_bg_$idx"
            )
            val borderCol by animateColorAsState(
                targetValue = if (selected) AccentBright else StrokePremium,
                animationSpec = tween(200),
                label = "seg_border_$idx"
            )
            Surface(
                color = bg,
                shape = RoundedCornerShape(11.dp),
                border = BorderStroke(width = 1.dp, color = borderCol),
                modifier = Modifier
                    .clickable { onSelect(idx) }
                    .testTag("${tagPrefix}_$idx")
            ) {
                Text(
                    text = tab,
                    fontFamily = OutfitFontFamily,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    letterSpacing = 0.2.sp,
                    color = if (selected) BackgroundPrimary else TextPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun FLSegmentedTabs(
    tabs: List<String>,
    selectedTab: String,
    onSelectTab: (String) -> Unit,
    modifier: Modifier = Modifier,
    tagPrefix: String = "fl_tab"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { tab ->
            val selected = tab == selectedTab
            val bg by animateColorAsState(
                targetValue = if (selected) AccentPrimary else SurfacePrimary,
                animationSpec = tween(200),
                label = "seg_str_bg_$tab"
            )
            val borderCol by animateColorAsState(
                targetValue = if (selected) AccentBright else StrokePremium,
                animationSpec = tween(200),
                label = "seg_str_border_$tab"
            )
            Surface(
                color = bg,
                shape = RoundedCornerShape(11.dp),
                border = BorderStroke(width = 1.dp, color = borderCol),
                modifier = Modifier
                    .clickable { onSelectTab(tab) }
                    .testTag("${tagPrefix}_$tab")
            ) {
                Text(
                    text = tab,
                    fontFamily = OutfitFontFamily,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp,
                    letterSpacing = 0.2.sp,
                    color = if (selected) BackgroundPrimary else TextPrimary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun FLPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    isAccentHighlight: Boolean = true,
    testTag: String? = null
) {
    val taggedModifier = if (!testTag.isNullOrBlank()) modifier.testTag(testTag) else modifier
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isAccentHighlight) AccentPrimary else SurfaceElevated,
            contentColor = if (isAccentHighlight) BackgroundPrimary else TextPrimary,
            disabledContainerColor = BackgroundSecondary,
            disabledContentColor = TextSecondary.copy(alpha = 0.45f)
        ),
        border = BorderStroke(
            1.dp,
            if (enabled && isAccentHighlight) AccentBright.copy(alpha = 0.65f) else StrokePremium
        ),
        shape = RoundedCornerShape(13.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        modifier = taggedModifier
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text.uppercase(),
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
fun FLSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    borderColor: Color = StrokePremiumStrong,
    textColor: Color = TextPrimary
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        border = BorderStroke(1.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = SurfacePrimary.copy(alpha = 0.90f),
            contentColor = textColor
        ),
        shape = RoundedCornerShape(13.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 11.dp),
        modifier = modifier
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.5.sp,
            color = textColor
        )
    }
}

@Composable
fun FLRatingBadge(
    rating: Double,
    label: String? = null,
    isGold: Boolean = false,
    modifier: Modifier = Modifier
) {
    FLRatingBadge(
        ratingText = FLFormatters.formatRating(rating),
        label = label,
        isGold = isGold,
        modifier = modifier
    )
}

@Composable
fun FLRatingBadge(
    ratingText: String,
    label: String? = null,
    isGold: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = BackgroundPrimary.copy(alpha = 0.92f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isGold) StrokePremiumStrong else AccentPrimary.copy(alpha = 0.75f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!label.isNullOrBlank()) {
                Text(
                    text = label.uppercase(),
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.5.sp,
                    letterSpacing = 0.5.sp,
                    color = TextSecondary
                )
            }
            Text(
                text = ratingText,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 22.sp,
                color = if (isGold) GoldPrimary else AccentBright
            )
        }
    }
}

@Composable
fun FLStatBar(
    label: String,
    value: Int,
    maxValue: Int = 100,
    valueText: String = value.toString(),
    accentColor: Color = AccentPrimary,
    modifier: Modifier = Modifier
) {
    val fraction by animateFloatAsState(
        targetValue = (value.toFloat() / maxValue.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = tween(350),
        label = "fl_stat_bar"
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextPrimary
            )
            Text(
                text = valueText,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = accentColor
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.09f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(accentColor.copy(alpha = 0.8f), accentColor)
                        )
                    )
            )
        }
    }
}

@Composable
fun FLNotificationBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    if (count <= 0) return
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Negative)
            .border(1.dp, TextPrimary, CircleShape)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = TextPrimary
        )
    }
}

@Composable
fun FLEmptyState(
    title: String,
    description: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfacePrimary,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, StrokePremium)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AccentBright,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}
