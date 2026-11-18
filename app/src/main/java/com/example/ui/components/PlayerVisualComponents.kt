package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlayerAppearance
import com.example.model.PlayerAttributes
import com.example.model.Position
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.BrightGold
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.StrokePremium
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

fun parseHexColor(hex: String, fallback: Color = AccentPrimary): Color {
    return runCatching {
        Color(android.graphics.Color.parseColor(hex))
    }.getOrDefault(fallback)
}

fun parseSafeHexColor(hex: String, fallback: Color = AccentPrimary): Color =
    parseHexColor(hex, fallback)

@Composable
fun ClubBadge(
    shortName: String,
    primaryHex: String,
    secondaryHex: String,
    size: Dp = 46.dp,
    modifier: Modifier = Modifier
) {
    ClubCrestBadge(
        shortName = shortName,
        primaryHex = primaryHex,
        secondaryHex = secondaryHex,
        size = size,
        modifier = modifier
    )
}

/**
 * Football Life 2.0 Layered 2.5D Athletic Portrait Renderer.
 * Renders a sculpted pro footballer bust with realistic jaw/cheekbone shading,
 * athletic neck/shoulders, bespoke club kit with crest & number, and stadium rim light.
 */
@Composable
fun FootballerAvatarCanvas(
    appearance: PlayerAppearance,
    clubPrimaryHex: String,
    clubSecondaryHex: String,
    kitNumber: Int = 10,
    modifier: Modifier = Modifier
) {
    val skinBase = parseHexColor(appearance.skinToneHex, Color(0xFFD9A17C))
    val skinShadow = Color(
        red = (skinBase.red * 0.76f).coerceIn(0f, 1f),
        green = (skinBase.green * 0.74f).coerceIn(0f, 1f),
        blue = (skinBase.blue * 0.72f).coerceIn(0f, 1f),
        alpha = 1f
    )
    val skinHighlight = Color(
        red = (skinBase.red * 1.10f).coerceIn(0f, 1f),
        green = (skinBase.green * 1.08f).coerceIn(0f, 1f),
        blue = (skinBase.blue * 1.06f).coerceIn(0f, 1f),
        alpha = 1f
    )
    val hairColor = parseHexColor(appearance.hairColorHex, Color(0xFF1F1B18))
    val kitPrimary = parseHexColor(clubPrimaryHex, AccentPrimary)
    val kitSecondary = parseHexColor(clubSecondaryHex, TextPrimary)

    val infinite = rememberInfiniteTransition(label = "portrait_motion")
    val dy by infinite.animateFloat(
        initialValue = -1.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "portrait_dy"
    )

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w * 0.5f

            // 1. Stadium floodlight halo behind athlete
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        kitPrimary.copy(alpha = 0.40f),
                        AccentPrimary.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    center = Offset(cx, h * 0.42f),
                    radius = w * 0.54f
                ),
                center = Offset(cx, h * 0.42f),
                radius = w * 0.54f
            )

            // 2. Athletic Trapezius & Neck
            val neckPath = Path().apply {
                moveTo(cx - w * 0.10f, h * 0.45f + dy)
                lineTo(cx + w * 0.10f, h * 0.45f + dy)
                lineTo(cx + w * 0.16f, h * 0.62f + dy)
                lineTo(cx - w * 0.16f, h * 0.62f + dy)
                close()
            }
            drawPath(
                path = neckPath,
                brush = Brush.verticalGradient(listOf(skinShadow, skinBase))
            )

            // 3. Pro Match Jersey & Shoulders
            val torsoPath = Path().apply {
                moveTo(cx - w * 0.44f, h * 0.99f)
                cubicTo(
                    cx - w * 0.43f, h * 0.66f + dy,
                    cx - w * 0.30f, h * 0.57f + dy,
                    cx - w * 0.13f, h * 0.55f + dy
                )
                lineTo(cx, h * 0.65f + dy)
                lineTo(cx + w * 0.13f, h * 0.55f + dy)
                cubicTo(
                    cx + w * 0.30f, h * 0.57f + dy,
                    cx + w * 0.43f, h * 0.66f + dy,
                    cx + w * 0.44f, h * 0.99f
                )
                close()
            }
            drawPath(
                path = torsoPath,
                brush = Brush.verticalGradient(
                    colors = listOf(kitPrimary, kitPrimary.copy(alpha = 0.82f), BackgroundPrimary)
                )
            )

            // Center Jersey Panel & Shoulder Piping
            val stripePath = Path().apply {
                moveTo(cx - w * 0.12f, h * 0.62f + dy)
                lineTo(cx + w * 0.12f, h * 0.62f + dy)
                lineTo(cx + w * 0.15f, h * 0.99f)
                lineTo(cx - w * 0.15f, h * 0.99f)
                close()
            }
            drawPath(
                path = stripePath,
                color = kitSecondary.copy(alpha = 0.18f)
            )

            // Shoulder stripes
            drawLine(
                color = kitSecondary.copy(alpha = 0.88f),
                start = Offset(cx - w * 0.15f, h * 0.56f + dy),
                end = Offset(cx - w * 0.36f, h * 0.68f + dy),
                strokeWidth = w * 0.032f
            )
            drawLine(
                color = kitSecondary.copy(alpha = 0.88f),
                start = Offset(cx + w * 0.15f, h * 0.56f + dy),
                end = Offset(cx + w * 0.36f, h * 0.68f + dy),
                strokeWidth = w * 0.032f
            )

            // V-Neck Collar Trim
            val collarPath = Path().apply {
                moveTo(cx - w * 0.13f, h * 0.55f + dy)
                lineTo(cx, h * 0.66f + dy)
                lineTo(cx + w * 0.13f, h * 0.55f + dy)
            }
            drawPath(
                path = collarPath,
                color = kitSecondary,
                style = Stroke(width = w * 0.028f)
            )

            // Small Club Badge on Left Chest
            drawCircle(
                color = GoldPrimary,
                radius = w * 0.038f,
                center = Offset(cx + w * 0.14f, h * 0.72f + dy)
            )
            drawCircle(
                color = kitPrimary,
                radius = w * 0.026f,
                center = Offset(cx + w * 0.14f, h * 0.72f + dy)
            )

            // 4. Ears
            drawOval(
                color = skinShadow,
                topLeft = Offset(cx - w * 0.205f, h * 0.28f + dy),
                size = Size(w * 0.045f, h * 0.09f)
            )
            drawOval(
                color = skinShadow,
                topLeft = Offset(cx + w * 0.16f, h * 0.28f + dy),
                size = Size(w * 0.045f, h * 0.09f)
            )

            // 5. Sculpted Head & Jawline
            val headPath = Path().apply {
                moveTo(cx, h * 0.14f + dy)
                cubicTo(
                    cx + w * 0.19f, h * 0.14f + dy,
                    cx + w * 0.19f, h * 0.32f + dy,
                    cx + w * 0.155f, h * 0.41f + dy
                )
                cubicTo(
                    cx + w * 0.12f, h * 0.47f + dy,
                    cx + w * 0.05f, h * 0.50f + dy,
                    cx, h * 0.505f + dy
                )
                cubicTo(
                    cx - w * 0.05f, h * 0.50f + dy,
                    cx - w * 0.12f, h * 0.47f + dy,
                    cx - w * 0.155f, h * 0.41f + dy
                )
                cubicTo(
                    cx - w * 0.19f, h * 0.32f + dy,
                    cx - w * 0.19f, h * 0.14f + dy,
                    cx, h * 0.14f + dy
                )
                close()
            }
            drawPath(
                path = headPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(skinShadow, skinBase, skinHighlight, skinBase)
                )
            )

            // Athletic stubble / jawline contour shadow
            val beardAlpha = if (appearance.facialPreset.contains("Genç")) 0.08f else 0.18f
            val jawShadowPath = Path().apply {
                moveTo(cx - w * 0.145f, h * 0.37f + dy)
                quadraticTo(cx, h * 0.49f + dy, cx + w * 0.145f, h * 0.37f + dy)
                cubicTo(
                    cx + w * 0.12f, h * 0.47f + dy,
                    cx + w * 0.05f, h * 0.50f + dy,
                    cx, h * 0.505f + dy
                )
                cubicTo(
                    cx - w * 0.05f, h * 0.50f + dy,
                    cx - w * 0.12f, h * 0.47f + dy,
                    cx - w * 0.145f, h * 0.37f + dy
                )
                close()
            }
            drawPath(
                path = jawShadowPath,
                color = hairColor.copy(alpha = beardAlpha)
            )

            // Eyebrows & Eyes (confident athletic gaze)
            drawRoundRect(
                color = hairColor.copy(alpha = 0.9f),
                topLeft = Offset(cx - w * 0.115f, h * 0.285f + dy),
                size = Size(w * 0.075f, h * 0.017f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            drawRoundRect(
                color = hairColor.copy(alpha = 0.9f),
                topLeft = Offset(cx + w * 0.04f, h * 0.285f + dy),
                size = Size(w * 0.075f, h * 0.017f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Eye whites + iris
            drawOval(
                color = Color(0xFFF3F6F4),
                topLeft = Offset(cx - w * 0.105f, h * 0.312f + dy),
                size = Size(w * 0.058f, h * 0.026f)
            )
            drawOval(
                color = Color(0xFFF3F6F4),
                topLeft = Offset(cx + w * 0.047f, h * 0.312f + dy),
                size = Size(w * 0.058f, h * 0.026f)
            )
            drawCircle(
                color = Color(0xFF1D1814),
                radius = w * 0.014f,
                center = Offset(cx - w * 0.076f, h * 0.325f + dy)
            )
            drawCircle(
                color = Color(0xFF1D1814),
                radius = w * 0.014f,
                center = Offset(cx + w * 0.076f, h * 0.325f + dy)
            )

            // Nose bridge shadow
            drawLine(
                color = skinShadow,
                start = Offset(cx, h * 0.31f + dy),
                end = Offset(cx - w * 0.012f, h * 0.385f + dy),
                strokeWidth = w * 0.015f
            )
            drawLine(
                color = skinShadow,
                start = Offset(cx - w * 0.025f, h * 0.39f + dy),
                end = Offset(cx + w * 0.025f, h * 0.39f + dy),
                strokeWidth = w * 0.014f
            )

            // Mouth / Composed athletic expression
            drawLine(
                color = Color(0xFF7D4B36),
                start = Offset(cx - w * 0.048f, h * 0.432f + dy),
                end = Offset(cx + w * 0.048f, h * 0.432f + dy),
                strokeWidth = w * 0.018f
            )

            // 6. Sculpted Hairstyle Volume
            val hairPath = Path().apply {
                moveTo(cx - w * 0.175f, h * 0.27f + dy)
                cubicTo(
                    cx - w * 0.19f, h * 0.13f + dy,
                    cx - w * 0.08f, h * 0.09f + dy,
                    cx, h * 0.095f + dy
                )
                cubicTo(
                    cx + w * 0.09f, h * 0.09f + dy,
                    cx + w * 0.19f, h * 0.13f + dy,
                    cx + w * 0.175f, h * 0.27f + dy
                )
                quadraticTo(cx, h * 0.185f + dy, cx - w * 0.175f, h * 0.27f + dy)
                close()
            }
            drawPath(
                path = hairPath,
                brush = Brush.verticalGradient(
                    listOf(
                        hairColor.copy(alpha = 0.95f),
                        hairColor,
                        Color.Black.copy(alpha = 0.85f)
                    )
                )
            )
        }

        // Kit Number Pill on Lower Chest
        Surface(
            color = BackgroundPrimary.copy(alpha = 0.82f),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(1.dp, GoldBorder),
            modifier = Modifier.padding(bottom = 6.dp)
        ) {
            Text(
                text = "#$kitNumber",
                fontFamily = BarlowCondensedFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = GoldPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
fun BroadcastPlayerCard(
    fullName: String,
    kitName: String,
    kitNumber: Int,
    position: Position,
    overallRating: Int,
    potential: Int,
    clubName: String,
    clubShortName: String,
    clubPrimaryHex: String,
    clubSecondaryHex: String,
    nationality: String,
    attributes: PlayerAttributes,
    appearance: PlayerAppearance,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfacePrimary,
        border = BorderStroke(
            width = 1.4.dp,
            brush = Brush.linearGradient(
                listOf(GoldPrimary, AccentPrimary, StrokePremium)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            SurfaceElevated.copy(alpha = 0.9f),
                            SurfacePrimary,
                            BackgroundPrimary
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left column: OVR, POS, Club Crest
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = overallRating.toString(),
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 52.sp,
                        lineHeight = 50.sp,
                        color = BrightGold
                    )
                    Surface(
                        color = AccentPrimary.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, AccentPrimary)
                    ) {
                        Text(
                            text = position.code,
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = AccentBright,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 1.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ClubCrestBadge(
                        shortName = clubShortName,
                        primaryHex = clubPrimaryHex,
                        secondaryHex = clubSecondaryHex,
                        size = 42.dp
                    )
                }

                // Center: Layered 2.5D Footballer Portrait
                FootballerAvatarCanvas(
                    appearance = appearance,
                    clubPrimaryHex = clubPrimaryHex,
                    clubSecondaryHex = clubSecondaryHex,
                    kitNumber = kitNumber,
                    modifier = Modifier.size(134.dp)
                )

                // Right: Potential, Nationality, Club
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = BackgroundPrimary.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GoldBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "POTANSİYEL",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Text(
                                text = potential.toString(),
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp,
                                color = GoldPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = nationality.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = clubName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Name Plate
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BackgroundPrimary.copy(alpha = 0.78f))
                    .border(1.dp, StrokePremium, RoundedCornerShape(10.dp))
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${fullName.uppercase()}  •  #$kitNumber $kitName",
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 23.sp,
                    letterSpacing = 0.8.sp,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6 Broadcast Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CardStatPill("HIZ", attributes.cardPace)
                CardStatPill("ŞUT", attributes.cardShooting)
                CardStatPill("PAS", attributes.cardPassing)
                CardStatPill("DRİ", attributes.cardDribbling)
                CardStatPill("SAV", attributes.cardDefending)
                CardStatPill("FİZ", attributes.cardPhysical)
            }
        }
    }
}

@Composable
private fun CardStatPill(label: String, value: Int) {
    val statColor = when {
        value >= 80 -> AccentBright
        value >= 68 -> GoldPrimary
        else -> TextPrimary
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BackgroundPrimary.copy(alpha = 0.72f))
            .border(1.dp, StrokePremium, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Text(
            text = value.toString(),
            fontFamily = BarlowCondensedFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 21.sp,
            color = statColor
        )
    }
}

@Composable
fun StatusMeterPill(
    label: String,
    value: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = BackgroundPrimary.copy(alpha = 0.82f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, StrokePremium)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = FLFormatters.formatPercent(value),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
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
