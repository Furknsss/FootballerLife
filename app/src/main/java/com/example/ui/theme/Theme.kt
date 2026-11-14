package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FootballLifeColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = BackgroundPrimary,
    primaryContainer = SurfaceElevated,
    onPrimaryContainer = TextPrimary,
    secondary = GoldPrimary,
    onSecondary = BackgroundPrimary,
    secondaryContainer = SurfacePrimary,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentBright,
    onTertiary = BackgroundPrimary,
    background = BackgroundPrimary,
    onBackground = TextPrimary,
    surface = SurfacePrimary,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error = Negative,
    onError = TextPrimary,
    outline = StrokePremium,
    outlineVariant = StrokePremiumStrong
)

@Composable
fun FootballLifeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FootballLifeColorScheme,
        typography = Typography,
        content = content
    )
}
