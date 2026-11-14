package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Football Life V3 — Premium EA FC & Broadcast Emerald-Obsidian Palette
val BackgroundPrimary = Color(0xFF050D0A)      // Deep Night Stadium Obsidian
val BackgroundSecondary = Color(0xFF091712)    // Carbon Pitch Sub-surface
val SurfacePrimary = Color(0xFF0E221A)         // Coated Broadcast Emerald Panel
val SurfaceElevated = Color(0xFF153025)        // Elevated Tactical Module Surface
val SurfaceGlass = Color(0xD90E221A)           // Translucent Coated Glass

// Mint-Green Action & Active States
val AccentPrimary = Color(0xFF1ED777)          // Crisp Broadcast Mint-Emerald
val AccentBright = Color(0xFF5BF5A3)           // Luminous Mint Highlight
val AccentSubtle = Color(0x261ED777)           // Soft Mint Tint

// Brushed Platinum / Warm Silver-Champagne (Replaces artificial yellow-gold)
val GoldPrimary = Color(0xFFE2D7C5)            // Brushed Platinum Champagne
val BrightGold = Color(0xFFF4EFE6)             // Crisp Warm Silver-White

// Typography Hierarchy Colors
val TextPrimary = Color(0xFFF7FAF8)            // Pure Broadcast White
val TextSecondary = Color(0xFFB4C7BE)          // Crisp Silver-Mint Metadata
val TextMuted = Color(0xFF769185)              // Muted Tactical Label
val MutedPitchGray = TextMuted

// Crisp White / Near-White Thin Borders & Dividers
val StrokePremium = Color(0x24FFFFFF)          // Thin 14% Near-White Hairline Border
val StrokePremiumStrong = Color(0x45FFFFFF)    // Crisp 27% White Frame Border
val StrokeSpecular = Color(0x66FFFFFF)         // Top-edge 40% Specular Highlight

// Semantic Status Colors
val Negative = Color(0xFFFF4D5E)
val Warning = Color(0xFFFFB020)
val InfoSky = Color(0xFF38BDF8)

// Backward-compatible semantic aliases used across screens
val DeepStadiumGreen = SurfacePrimary
val DarkTurfGreen = SurfaceElevated
val VibrantFootballGreen = AccentPrimary
val BrightNeonGreen = AccentBright
val DarkPitchSurface = BackgroundPrimary
val ElevatedCardGreen = SurfacePrimary
val ElevatedCardDark = BackgroundSecondary

val PrimaryWhite = TextPrimary
val SecondaryMintGray = TextSecondary

val ChampionshipGold = GoldPrimary
val MatchWinGreen = AccentPrimary
val MatchDrawAmber = Warning
val MatchLossRed = Negative

val CrispWhiteBorder = StrokePremium
val CrispWhiteBorderStrong = StrokePremiumStrong
val GoldBorder = Color(0x36FFFFFF)             // Clean near-white frame border instead of yellow-gold
val GlassOverlayDark = Color(0xD9050D0A)
