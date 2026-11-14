package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import com.example.engine.MatchRenderInterpolation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.LiveMatchUiState
import com.example.engine.MatchPhase
import com.example.engine.TacticalDecisionOption
import com.example.model.CareerSaveState
import com.example.model.GraphicsQuality
import com.example.ui.components.ClubCrestBadge
import com.example.ui.components.FLFormatters
import com.example.ui.components.parseHexColor
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.BrightGold
import com.example.ui.theme.BrightNeonGreen
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.CrispWhiteBorder
import com.example.ui.theme.CrispWhiteBorderStrong
import com.example.ui.theme.DarkPitchSurface
import com.example.ui.theme.DarkTurfGreen
import com.example.ui.theme.DeepStadiumGreen
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.MatchDrawAmber
import com.example.ui.theme.MatchLossRed
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PrimaryWhite
import com.example.ui.theme.SecondaryMintGray
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StrokePremium
import com.example.ui.theme.StrokePremiumStrong
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePrimary
import com.example.ui.theme.TekoFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VibrantFootballGreen

@Composable
fun MatchCenterScreen(
    career: CareerSaveState,
    liveMatch: LiveMatchUiState?,
    graphicsQuality: GraphicsQuality,
    onPrepareMatch: () -> Unit,
    onStartKickOff: () -> Unit,
    onResumeSecondHalf: () -> Unit,
    onChangeSpeed: (Int) -> Unit,
    onChooseDecisionOption: (TacticalDecisionOption) -> Unit,
    onFinishMatch: () -> Unit
) {
    if (liveMatch == null) {
        val nextFx = career.todayUserFixture ?: career.nextUnplayedUserFixture
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(BackgroundSecondary, BackgroundPrimary))
                )
                .padding(16.dp)
                .testTag("match_pre_lobby_screen"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = SurfacePrimary,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, StrokePremiumStrong),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = AccentBright,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "MAÇ MERKEZİ & HİBRİT 2D SAHA",
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        color = TextPrimary
                    )
                    if (nextFx != null) {
                        Text(
                            text = "HAFTA ${nextFx.week} • ${nextFx.homeClubName} vs ${nextFx.awayClubName}",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AccentBright,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Stadyum: ${nextFx.stadiumName} • Kadro Rolün: ${career.nextMatchStartingStatus.labelTr}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onPrepareMatch,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPrimary,
                                contentColor = Color(0xFF04140C)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("btn_enter_2d_match")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "STADYUMA GİR VE 2D MAÇI BAŞLAT",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    } else {
                        Text(
                            text = "Sezonun tüm fikstürleri tamamlandı!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
        return
    }

    val homeColor = parseHexColor(liveMatch.homeClub.primaryColorHex, Color(0xFFE74C3C))
    val awayColor = parseHexColor(liveMatch.awayClub.primaryColorHex, Color(0xFF3498DB))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BackgroundSecondary, BackgroundPrimary))
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("live_match_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. V3 Broadcast Live Scorebug Header + Momentum Bar + Player Telemetry
        item {
            Surface(
                color = SurfacePrimary,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, StrokePremiumStrong),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Home Team Block
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(homeColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            ClubCrestBadge(
                                shortName = liveMatch.homeClub.shortName,
                                primaryHex = liveMatch.homeClub.primaryColorHex,
                                secondaryHex = liveMatch.homeClub.secondaryColorHex,
                                size = 38.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = liveMatch.homeClub.shortName,
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "%${liveMatch.homePossessionPct} TOP",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Center Broadcast Scoreboard & Live Clock
                        Surface(
                            color = BackgroundPrimary,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, StrokePremiumStrong)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = liveMatch.homeScore.toString(),
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.testTag("match_home_score")
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        color = AccentPrimary.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, AccentBright.copy(alpha = 0.6f))
                                    ) {
                                        Text(
                                            text = "${liveMatch.minute}'",
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = AccentBright,
                                            modifier = Modifier
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                                .testTag("match_minute_label")
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = when (liveMatch.phase) {
                                            MatchPhase.PRE_MATCH -> "BAŞLIYOR"
                                            MatchPhase.HALF_TIME -> "DEVRE"
                                            MatchPhase.FULL_TIME -> "MAÇ SONU"
                                            MatchPhase.CRITICAL_DECISION -> "KARAR ANI"
                                            MatchPhase.GOAL_CELEBRATION -> "GOOOL!"
                                            else -> "CANLI"
                                        },
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        letterSpacing = 0.6.sp,
                                        color = TextSecondary
                                    )
                                }
                                Text(
                                    text = liveMatch.awayScore.toString(),
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.testTag("match_away_score")
                                )
                            }
                        }

                        // Away Team Block
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = liveMatch.awayClub.shortName,
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "%${100 - liveMatch.homePossessionPct} TOP",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            ClubCrestBadge(
                                shortName = liveMatch.awayClub.shortName,
                                primaryHex = liveMatch.awayClub.primaryColorHex,
                                secondaryHex = liveMatch.awayClub.secondaryColorHex,
                                size = 38.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(36.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(awayColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Possession & Momentum Split Bar
                    val homeRatio = (liveMatch.homePossessionPct / 100f).coerceIn(0.15f, 0.85f)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(BackgroundPrimary)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(homeRatio)
                                .fillMaxSize()
                                .background(homeColor)
                        )
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .fillMaxSize()
                                .background(Color.White)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f - homeRatio)
                                .fillMaxSize()
                                .background(awayColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Player Live Telemetry Pill & Speed Multiplier Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = BackgroundPrimary,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, StrokePremium)
                            ) {
                                Text(
                                    text = "RTG ${FLFormatters.formatRating(liveMatch.userLiveRating)}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = AccentBright,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "${career.player.kitName} • ${liveMatch.userGoals}G ${liveMatch.userAssists}A • ENJ %${liveMatch.userLiveEnergy}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(1, 2, 3).forEach { spd ->
                                val active = liveMatch.speedMultiplier == spd
                                Surface(
                                    color = if (active) AccentPrimary else BackgroundPrimary,
                                    shape = RoundedCornerShape(7.dp),
                                    border = BorderStroke(1.dp, if (active) AccentBright else StrokePremium),
                                    modifier = Modifier
                                        .clickable { onChangeSpeed(spd) }
                                        .testTag("btn_speed_${spd}x")
                                ) {
                                    Text(
                                        text = "${spd}x",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (active) Color(0xFF04140C) else TextPrimary,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Real-time 2D Football Pitch Canvas (22 moving players + ball + camera zoom)
        item {
            Surface(
                color = BackgroundPrimary,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, StrokePremiumStrong),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.48f)
                        .clip(RoundedCornerShape(20.dp))
                        .testTag("pitch_2d_canvas")
                ) {
                    HybridPitch2DCanvas(
                        matchState = liveMatch,
                        graphicsQuality = graphicsQuality,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Goal Celebration Broadcast Overlay
                    AnimatedVisibility(
                        visible = liveMatch.goalScorerBanner != null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Surface(
                            color = BackgroundPrimary.copy(alpha = 0.94f),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.5.dp, AccentBright)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 26.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = liveMatch.goalScorerBanner ?: "GOOOL!",
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 38.sp,
                                    color = AccentBright
                                )
                                Text(
                                    text = "${liveMatch.homeClub.shortName} ${liveMatch.homeScore} - ${liveMatch.awayScore} ${liveMatch.awayClub.shortName}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Live Broadcast Commentary Ticker Banner
        item {
            Surface(
                color = SurfacePrimary,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, StrokePremium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AccentBright)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = liveMatch.latestBannerText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }

        // 4. Phase Control Panel OR Compact Broadcast Critical Decision Cards
        item {
            when (liveMatch.phase) {
                MatchPhase.PRE_MATCH -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            color = SurfacePrimary,
                            shape = RoundedCornerShape(16.dp),
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
                                Column {
                                    Text(
                                        text = "${liveMatch.homeClub.name} (4-3-3)",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "TD: ${liveMatch.homeClub.managerName} • Güç: ${liveMatch.homeClub.teamStrength}",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${liveMatch.awayClub.name} (4-3-3)",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "TD: ${liveMatch.awayClub.managerName} • Güç: ${liveMatch.awayClub.teamStrength}",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onStartKickOff,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentPrimary,
                                contentColor = Color(0xFF04140C)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("btn_kickoff_match")
                        ) {
                            Icon(Icons.Default.SportsSoccer, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BAŞLAMA VURUŞUNU YAP (1. DEVRE)",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                MatchPhase.CRITICAL_DECISION -> {
                    val decision = liveMatch.activeDecision
                    if (decision != null) {
                        Surface(
                            color = SurfacePrimary,
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.5.dp, AccentBright),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("critical_decision_panel")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = AccentPrimary,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "KRİTİK KARAR ANI • TAKTİK KAMERA",
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            letterSpacing = 0.6.sp,
                                            color = Color(0xFF04140C),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Text(
                                        text = "${decision.minute}' DAKİKA",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = AccentBright
                                    )
                                }

                                Text(
                                    text = decision.title,
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = decision.situationDescription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )

                                decision.options.forEachIndexed { idx, opt ->
                                    val probColor = when {
                                        opt.successProbabilityPct >= 70 -> AccentBright
                                        opt.successProbabilityPct >= 50 -> ChampionshipGold
                                        else -> MatchDrawAmber
                                    }
                                    Surface(
                                        color = BackgroundSecondary,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, StrokePremiumStrong),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onChooseDecisionOption(opt) }
                                            .testTag("btn_decision_opt_$idx")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                // Numbered tactical badge
                                                Surface(
                                                    color = SurfaceElevated,
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, AccentBright.copy(alpha = 0.6f)),
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = "${idx + 1}",
                                                            fontFamily = SpaceGroteskFontFamily,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = AccentBright
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = opt.title,
                                                        fontFamily = OutfitFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = TextPrimary
                                                    )
                                                    Text(
                                                        text = opt.subtitle,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = TextSecondary,
                                                        maxLines = 1
                                                    )
                                                    Spacer(modifier = Modifier.height(3.dp))
                                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Text(
                                                            text = "${opt.keyAttributeLabel}: ${opt.keyAttributeValue}",
                                                            fontFamily = SpaceGroteskFontFamily,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.sp,
                                                            color = AccentBright
                                                        )
                                                        Text(
                                                            text = "• ${opt.riskLevel}",
                                                            fontFamily = OutfitFontFamily,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 10.sp,
                                                            color = TextMuted
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Surface(
                                                color = SurfacePrimary,
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(1.dp, probColor.copy(alpha = 0.7f))
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = "%${opt.successProbabilityPct}",
                                                        fontFamily = SpaceGroteskFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp,
                                                        color = probColor
                                                    )
                                                    Text(
                                                        text = "BAŞARI",
                                                        fontFamily = OutfitFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 8.sp,
                                                        color = TextMuted
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

                MatchPhase.HALF_TIME -> {
                    Surface(
                        color = SurfacePrimary,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, StrokePremiumStrong),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "DEVRE ARASI SOYUNMA ODASI",
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = AccentBright
                            )
                            Text(
                                text = "Şutlar: ${liveMatch.homeShots} - ${liveMatch.awayShots} (İsabet: ${liveMatch.homeShotsOnTarget} - ${liveMatch.awayShotsOnTarget})",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onResumeSecondHalf,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentPrimary,
                                    contentColor = Color(0xFF04140C)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_start_second_half")
                            ) {
                                Text(
                                    text = "İKİNCİ YARIYA BAŞLA (46')",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                MatchPhase.FULL_TIME -> {
                    Button(
                        onClick = onFinishMatch,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentPrimary,
                            contentColor = Color(0xFF04140C)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("btn_finish_match_to_locker")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MAÇI TAMAMLA VE SOYUNMA ODASINA GEÇ",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                else -> Unit
            }
        }

        // 5. Match Statistics & Live Event Timeline
        item {
            Surface(
                color = SurfacePrimary,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, StrokePremium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CANLI MAÇ İSTATİSTİKLERİ & OLAY AKIŞI",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AccentBright
                        )
                        Text(
                            text = "Şut ${liveMatch.homeShots}-${liveMatch.awayShots} • Korner ${liveMatch.homeCorners}-${liveMatch.awayCorners}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    liveMatch.commentaryFeed.take(6).forEach { ev ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${ev.description}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (ev.involvesUser) AccentBright else TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HybridPitch2DCanvas(
    matchState: LiveMatchUiState,
    graphicsQuality: GraphicsQuality,
    modifier: Modifier = Modifier
) {
    var previousFrame by remember { mutableStateOf(matchState) }
    var renderFrom by remember { mutableStateOf(matchState) }
    val interpolation = remember { Animatable(1f) }
    val priorPlayers = remember(renderFrom) { renderFrom.players.associateBy { it.id } }
    LaunchedEffect(matchState) {
        renderFrom = previousFrame
        previousFrame = matchState
        interpolation.snapTo(0f)
        interpolation.animateTo(1f, tween(MatchRenderInterpolation.SIMULATION_STEP_MS, easing = LinearEasing))
    }
    val homePrimary = parseHexColor(matchState.homeClub.primaryColorHex, Color(0xFFE74C3C))
    val awayPrimary = parseHexColor(matchState.awayClub.primaryColorHex, Color(0xFF3498DB))

    val labelPaint = remember {
        android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = 21f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
    }

    Canvas(modifier = modifier) {
        val fraction = interpolation.value
        val w = size.width
        val h = size.height

        val zoom = matchState.cameraZoom.coerceIn(1.0f, 1.5f)
        val focusPxX = (matchState.cameraFocusX / 100f) * w
        val focusPxY = (matchState.cameraFocusY / 100f) * h

        withTransform({
            if (zoom > 1.01f) {
                scale(zoom, zoom, pivot = Offset(focusPxX, focusPxY))
            }
        }) {
            // 1. 12-Stripe Rich Mowed Stadium Turf
            val stripeCount = 12
            val stripeW = w / stripeCount
            for (i in 0 until stripeCount) {
                drawRect(
                    color = if (i % 2 == 0) Color(0xFF0B3525) else Color(0xFF0F422E),
                    topLeft = Offset(i * stripeW, 0f),
                    size = Size(stripeW + 1f, h)
                )
            }

            // Subtle stadium floodlight radial sheen
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x185BF5A3),
                        Color.Transparent,
                        Color(0x38020806)
                    ),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.65f
                ),
                topLeft = Offset.Zero,
                size = Size(w, h)
            )

            val pad = w * 0.04f
            val pitchW = w - pad * 2
            val pitchH = h - pad * 2
            val lineStroke = Stroke(width = 2.4f)
            val lineColor = Color.White.copy(alpha = 0.82f)

            // Outer boundary
            drawRect(
                color = lineColor,
                topLeft = Offset(pad, pad),
                size = Size(pitchW, pitchH),
                style = lineStroke
            )

            // Halfway line & Center circle
            drawLine(
                color = lineColor,
                start = Offset(w * 0.5f, pad),
                end = Offset(w * 0.5f, h - pad),
                strokeWidth = 2.4f
            )
            drawCircle(
                color = lineColor,
                radius = pitchH * 0.18f,
                center = Offset(w * 0.5f, h * 0.5f),
                style = lineStroke
            )
            drawCircle(
                color = lineColor,
                radius = 4.5f,
                center = Offset(w * 0.5f, h * 0.5f)
            )

            // Left & Right Penalty Boxes
            val boxW = pitchW * 0.16f
            val boxH = pitchH * 0.54f
            val boxTop = (h - boxH) * 0.5f
            drawRect(
                color = lineColor,
                topLeft = Offset(pad, boxTop),
                size = Size(boxW, boxH),
                style = lineStroke
            )
            drawRect(
                color = lineColor,
                topLeft = Offset(w - pad - boxW, boxTop),
                size = Size(boxW, boxH),
                style = lineStroke
            )

            // Penalty Spots & Penalty Box D-Arcs
            val leftPenSpotX = pad + boxW * 0.68f
            val rightPenSpotX = w - pad - boxW * 0.68f
            drawCircle(color = lineColor, radius = 3.5f, center = Offset(leftPenSpotX, h * 0.5f))
            drawCircle(color = lineColor, radius = 3.5f, center = Offset(rightPenSpotX, h * 0.5f))

            val dArcRadius = pitchH * 0.14f
            drawArc(
                color = lineColor,
                startAngle = -52f,
                sweepAngle = 104f,
                useCenter = false,
                topLeft = Offset(pad + boxW - dArcRadius * 0.6f, h * 0.5f - dArcRadius),
                size = Size(dArcRadius * 1.2f, dArcRadius * 2f),
                style = lineStroke
            )
            drawArc(
                color = lineColor,
                startAngle = 128f,
                sweepAngle = 104f,
                useCenter = false,
                topLeft = Offset(w - pad - boxW - dArcRadius * 0.6f, h * 0.5f - dArcRadius),
                size = Size(dArcRadius * 1.2f, dArcRadius * 2f),
                style = lineStroke
            )

            // Left & Right 6-yard Goal Areas & Goals
            val sixW = pitchW * 0.06f
            val sixH = pitchH * 0.26f
            val sixTop = (h - sixH) * 0.5f
            drawRect(
                color = lineColor,
                topLeft = Offset(pad, sixTop),
                size = Size(sixW, sixH),
                style = lineStroke
            )
            drawRect(
                color = lineColor,
                topLeft = Offset(w - pad - sixW, sixTop),
                size = Size(sixW, sixH),
                style = lineStroke
            )

            // Goals with net fill
            val goalH = pitchH * 0.14f
            val goalTop = (h - goalH) * 0.5f
            drawRect(
                color = Color.White.copy(alpha = 0.18f),
                topLeft = Offset(pad * 0.35f, goalTop),
                size = Size(pad * 0.65f, goalH)
            )
            drawRect(
                color = Color.White.copy(alpha = 0.92f),
                topLeft = Offset(pad * 0.35f, goalTop),
                size = Size(pad * 0.65f, goalH),
                style = Stroke(width = 3f)
            )
            drawRect(
                color = Color.White.copy(alpha = 0.18f),
                topLeft = Offset(w - pad, goalTop),
                size = Size(pad * 0.65f, goalH)
            )
            drawRect(
                color = Color.White.copy(alpha = 0.92f),
                topLeft = Offset(w - pad, goalTop),
                size = Size(pad * 0.65f, goalH),
                style = Stroke(width = 3f)
            )

            // If in Critical Decision, draw tactical target lane lines from ball
            if (matchState.phase == MatchPhase.CRITICAL_DECISION) {
                val bx = pad + (matchState.ball.x / 100f) * pitchW
                val by = pad + (matchState.ball.y / 100f) * pitchH
                val goalX = if (matchState.userIsHome) w - pad else pad
                drawLine(
                    color = AccentBright.copy(alpha = 0.85f),
                    start = Offset(bx, by),
                    end = Offset(goalX, h * 0.44f),
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.70f),
                    start = Offset(bx, by),
                    end = Offset(goalX, h * 0.56f),
                    strokeWidth = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            }

            // 2. Draw all 22 Players with 3D-shaded markers & clear team colors
            val playerRadius = (pitchH * 0.036f).coerceIn(11f, 20f)
            matchState.players.forEach { p ->
                val prior = priorPlayers[p.id] ?: p
                val px = pad + (MatchRenderInterpolation.coordinate(prior.x, p.x, fraction) / 100f) * pitchW
                val py = pad + (MatchRenderInterpolation.coordinate(prior.y, p.y, fraction) / 100f) * pitchH
                val fill = if (p.isHome) homePrimary else awayPrimary

                // User Highlight Halo
                if (p.isUser) {
                    drawCircle(
                        color = AccentBright.copy(alpha = 0.30f),
                        radius = playerRadius * 1.95f,
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = AccentBright,
                        radius = playerRadius * 1.48f,
                        center = Offset(px, py),
                        style = Stroke(width = 2.8f)
                    )
                }

                // Ball Carrier Ring
                if (p.hasBall) {
                    drawCircle(
                        color = Color.White,
                        radius = playerRadius * 1.32f,
                        center = Offset(px, py),
                        style = Stroke(width = 2.4f)
                    )
                }

                // Player Shadow
                drawCircle(
                    color = Color.Black.copy(alpha = 0.52f),
                    radius = playerRadius,
                    center = Offset(px + 2f, py + 3.5f)
                )

                // Player Disc + Top Specular Highlight
                drawCircle(
                    color = fill,
                    radius = playerRadius,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.22f),
                    radius = playerRadius * 0.55f,
                    center = Offset(px - playerRadius * 0.22f, py - playerRadius * 0.22f)
                )
                drawCircle(
                    color = if (p.isUser) AccentBright else Color.White,
                    radius = playerRadius,
                    center = Offset(px, py),
                    style = Stroke(width = 2.2f)
                )

                // Jersey Number
                drawContext.canvas.nativeCanvas.drawText(
                    p.number.toString(),
                    px,
                    py + playerRadius * 0.36f,
                    labelPaint
                )
            }

            // 3. Draw High-Visibility Ball with subtle halo
            val ballPxX = pad + (MatchRenderInterpolation.coordinate(renderFrom.ball.x, matchState.ball.x, fraction) / 100f) * pitchW
            val ballPxY = pad + (MatchRenderInterpolation.coordinate(renderFrom.ball.y, matchState.ball.y, fraction) / 100f) * pitchH
            val ballRadius = playerRadius * 0.58f

            drawCircle(
                color = AccentBright.copy(alpha = 0.32f),
                radius = ballRadius * 1.75f,
                center = Offset(ballPxX, ballPxY)
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.55f),
                radius = ballRadius,
                center = Offset(ballPxX + 2f, ballPxY + 3f)
            )
            drawCircle(
                color = Color.White,
                radius = ballRadius,
                center = Offset(ballPxX, ballPxY)
            )
            drawCircle(
                color = Color(0xFF0A1711),
                radius = ballRadius,
                center = Offset(ballPxX, ballPxY),
                style = Stroke(width = 1.8f)
            )

            // 4. Goal Celebration Particles
            if (matchState.phase == MatchPhase.GOAL_CELEBRATION) {
                val count = graphicsQuality.particleCount
                for (i in 0 until count) {
                    val angle = (i.toFloat() / count) * 6.283f
                    val dist = (18 - matchState.resolutionTicksRemaining).coerceAtLeast(1) * 8f
                    val cx = ballPxX + kotlin.math.cos(angle) * dist
                    val cy = ballPxY + kotlin.math.sin(angle) * dist
                    drawCircle(
                        color = if (i % 2 == 0) Color.White else AccentBright,
                        radius = 4.5f,
                        center = Offset(cx.coerceIn(0f, w), cy.coerceIn(0f, h))
                    )
                }
            }
        }
    }
}

@Composable
fun PostMatchLockerRoomScreen(
    career: CareerSaveState,
    onSubmitPressStatement: (String, String) -> Unit,
    onContinueToCareerHub: () -> Unit
) {
    val report = career.lastMatchReport
    var customPressText by remember { mutableStateOf("") }

    if (report == null) {
        onContinueToCareerHub()
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPitchSurface)
            .padding(16.dp)
            .testTag("post_match_locker_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Locker Room Header
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DeepStadiumGreen,
                border = BorderStroke(1.5.dp, ChampionshipGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_locker_room),
                        contentDescription = "Soyunma Odası",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .alpha(0.45f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, DeepStadiumGreen)
                                )
                            )
                    )
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "MAÇ SONU • SOYUNMA ODASI & BASIN",
                            style = MaterialTheme.typography.labelMedium,
                            color = ChampionshipGold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${report.homeClubName}  ${report.homeScore} - ${report.awayScore}  ${report.awayClubName}",
                            fontFamily = TekoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = PrimaryWhite,
                            textAlign = TextAlign.Center
                        )
                        Surface(
                            color = DarkPitchSurface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, VibrantFootballGreen)
                        ) {
                            Text(
                                text = "MAÇ REYTİNGİN: ${FLFormatters.formatRating(report.userRating)}  •  ${report.userGoals} GOL  •  ${report.userAssists} ASİST",
                                style = MaterialTheme.typography.labelLarge,
                                color = BrightNeonGreen,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Manager & Teammate Reactions + Match Bonus
        item {
            Surface(
                color = DeepStadiumGreen,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CrispWhiteBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("TEKNİK DİREKTÖR VE TAKIM DEĞERLENDİRMESİ", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
                    Text(report.managerFeedback, style = MaterialTheme.typography.bodyMedium, color = PrimaryWhite)
                    Text(report.teammateReaction, style = MaterialTheme.typography.bodySmall, color = VibrantFootballGreen)
                    Text(
                        text = "Kazanılan Maç & Gol Primi: +${FLFormatters.formatEuroFull(report.wageAndBonusEarnedEur)} • Harcanan Enerji: -%${report.energySpent}",
                        style = MaterialTheme.typography.labelMedium,
                        color = BrightGold
                    )
                }
            }
        }

        // Press Interview Section (Preset & Custom Free-Text)
        item {
            Surface(
                color = DeepStadiumGreen,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GoldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = ChampionshipGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MAÇ SONU BASIN RÖPORTAJI",
                            style = MaterialTheme.typography.labelLarge,
                            color = ChampionshipGold
                        )
                    }

                    if (!career.pressCompletedForLastMatch) {
                        Text(
                            text = "Muhabir: \"Bugünkü ${report.homeScore}-${report.awayScore} biten karşılaşma ve kendi performansın hakkında taraftarlara ne söylemek istersin?\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryMintGray
                        )

                        Button(
                            onClick = {
                                onSubmitPressStatement(
                                    "Takım arkadaşlarımla birlikte sahada son dakikaya kadar mücadele ettik. Taraftarımıza armağan olsun!",
                                    "MÜTEVAZI_TAKIMCI"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkTurfGreen, contentColor = PrimaryWhite),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Mütevazı: \"Takım olarak sonuna kadar savaştık, taraftarımıza teşekkürler.\"")
                        }

                        Button(
                            onClick = {
                                onSubmitPressStatement(
                                    "Sahada kalitemi göstermeye devam edeceğim. Hedefim bu ligin en iyisi olmak!",
                                    "İDDİALI_ÖZGÜVENLİ"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkTurfGreen, contentColor = BrightGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("İddialı: \"Sahada fark yaratmak için buradayım, daha yeni başlıyorum!\"")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customPressText,
                                onValueChange = { customPressText = it },
                                placeholder = { Text("Kendi cümleni basına söyle...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (customPressText.isNotBlank()) {
                                        onSubmitPressStatement(customPressText, "İDDİALI_ÖZGÜVENLİ")
                                        customPressText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VibrantFootballGreen,
                                    contentColor = DarkPitchSurface
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder")
                            }
                        }
                    } else {
                        Text(
                            text = "Basın açıklaman kaydedildi ve spor haberlerinde manşete taşındı!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BrightNeonGreen
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = onContinueToCareerHub,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibrantFootballGreen,
                    contentColor = DarkPitchSurface
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_return_hub_after_match")
            ) {
                Text(
                    text = "KARİYER MERKEZİNE DÖN",
                    fontFamily = TekoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }
        }
    }
}
