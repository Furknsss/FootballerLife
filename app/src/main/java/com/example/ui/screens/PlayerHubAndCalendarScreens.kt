package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.CareerEngine
import com.example.engine.TrainingDrillDefinition
import com.example.engine.WorldDataCatalog
import com.example.model.CareerSaveState
import com.example.model.DynamicStoryChoice
import com.example.model.StartingStatus
import com.example.ui.components.BroadcastPlayerCard
import com.example.ui.components.ClubCrestBadge
import com.example.ui.components.FLFormatters
import com.example.ui.components.FLPrimaryButton
import com.example.ui.components.FLSecondaryButton
import com.example.ui.components.FLSectionHeader
import com.example.ui.components.FLSegmentedTabs
import com.example.ui.components.FootballerAvatarCanvas
import com.example.ui.components.StatusMeterPill
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
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MatchDrawAmber
import com.example.ui.theme.MatchLossRed
import com.example.ui.theme.MatchWinGreen
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PrimaryWhite
import com.example.ui.theme.SecondaryMintGray
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StrokePremium
import com.example.ui.theme.StrokePremiumStrong
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePrimary
import com.example.ui.theme.TekoFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VibrantFootballGreen

@Composable
fun PlayerHubScreen(
    career: CareerSaveState,
    onOpenTraining: () -> Unit,
    onOpenSocialLife: () -> Unit,
    onOpenTransfers: () -> Unit,
    onOpenProfileCard: () -> Unit,
    onOpenPhone: () -> Unit,
    onOpenLeague: () -> Unit = {},
    onOpenCoachMeeting: () -> Unit = {},
    onOpenVoiceCall: (String?) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onPlayNextMatch: () -> Unit,
    onAdvanceNextDay: () -> Unit,
    onAdvanceToMatchDay: () -> Unit,
    onResolveDilemma: (DynamicStoryChoice) -> Unit
) {
    val player = career.player
    val club = career.currentClub
    val nextFixture = career.todayUserFixture ?: career.nextUnplayedUserFixture
    val isMatchToday = career.todayUserFixture != null
    val shortDateStr = CareerEngine.formatShortDate(career.currentDayIndex)
    val clockStr = career.formattedClockTime

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("player_hub_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // =====================================================================
        // 1. ÜST PROFİL KARTI + SAĞ ÜSTTE SENKRONİZE TARİH, SAAT VE "İLERLE" TUŞU
        // =====================================================================
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = SurfacePrimary,
                border = BorderStroke(1.dp, StrokePremiumStrong),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_stadium_hero),
                        contentDescription = "Stadyum Atmosferi",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(228.dp)
                            .alpha(0.28f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(228.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        BackgroundPrimary.copy(alpha = 0.35f),
                                        SurfacePrimary.copy(alpha = 0.90f),
                                        BackgroundSecondary
                                    )
                                )
                            )
                    )

                    Column(modifier = Modifier.padding(14.dp)) {
                        // Top Bar: Left = Balance & Market Value | Right = Date + Clock + İLERLE button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = BackgroundPrimary.copy(alpha = 0.88f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, StrokePremium)
                                ) {
                                    Text(
                                        text = FLFormatters.formatMarketValue(player.marketValueEur),
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = AccentBright,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                                Surface(
                                    color = BackgroundPrimary.copy(alpha = 0.88f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, StrokePremium)
                                ) {
                                    Text(
                                        text = FLFormatters.formatCurrency(career.balanceEur),
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            // SAĞ ÜST: TARİH + SAAT + İLERLE TUŞU
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    color = BackgroundPrimary.copy(alpha = 0.92f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, StrokePremiumStrong)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = shortDateStr,
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(AccentBright)
                                        )
                                        Text(
                                            text = clockStr,
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }

                                Surface(
                                    color = AccentPrimary,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, AccentBright),
                                    modifier = Modifier
                                        .clickable { onAdvanceNextDay() }
                                        .testTag("btn_advance_next_day")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "İLERLE",
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF04150D)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Saati İlerle",
                                            tint = Color(0xFF04150D),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Surface(
                                    color = BackgroundPrimary.copy(alpha = 0.92f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, StrokePremiumStrong),
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { onOpenSettings() }
                                        .testTag("btn_hub_open_settings")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = "Ayarlar & Gemini AI",
                                            tint = AccentBright,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Player Portrait + Name + OVR Shield
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenProfileCard() }
                            ) {
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    Surface(
                                        color = BackgroundPrimary.copy(alpha = 0.65f),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, StrokePremiumStrong),
                                        modifier = Modifier.size(76.dp)
                                    ) {
                                        FootballerAvatarCanvas(
                                            appearance = player.appearance,
                                            clubPrimaryHex = club.primaryColorHex,
                                            clubSecondaryHex = club.secondaryColorHex,
                                            kitNumber = player.kitNumber,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    ClubCrestBadge(
                                        shortName = club.shortName,
                                        primaryHex = club.primaryColorHex,
                                        secondaryHex = club.secondaryColorHex,
                                        size = 26.dp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = AccentPrimary,
                                            shape = RoundedCornerShape(5.dp)
                                        ) {
                                            Text(
                                                text = player.primaryPosition.code,
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = BackgroundPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "#${player.kitNumber} • ${club.name}",
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.5.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = player.fullName,
                                        fontFamily = BarlowCondensedFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 28.sp,
                                        lineHeight = 28.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.testTag("hub_player_name")
                                    )
                                    Text(
                                        text = "${career.nextMatchStartingStatus.labelTr} • ${player.currentSeasonStats.appearances} Maç ${player.currentSeasonStats.goals}G ${player.currentSeasonStats.assists}A",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 11.sp,
                                        color = AccentBright
                                    )
                                }
                            }

                            Surface(
                                color = BackgroundPrimary.copy(alpha = 0.90f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, StrokePremiumStrong),
                                modifier = Modifier.clickable { onOpenProfileCard() }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "GEN",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = player.overallRating.toString(),
                                        fontFamily = BarlowCondensedFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 32.sp,
                                        lineHeight = 30.sp,
                                        color = TextPrimary,
                                        modifier = Modifier.testTag("hub_player_ovr")
                                    )
                                    Text(
                                        text = "POT ${player.potential}",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp,
                                        color = AccentBright
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 4 Status Meters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StatusMeterPill(
                                label = "ENERJİ",
                                value = player.energy,
                                accentColor = if (player.energy >= 60) AccentPrimary else MatchDrawAmber,
                                modifier = Modifier.weight(1f)
                            )
                            StatusMeterPill(
                                label = "MORAL",
                                value = player.morale,
                                accentColor = AccentBright,
                                modifier = Modifier.weight(1f)
                            )
                            StatusMeterPill(
                                label = "FORM",
                                value = player.form,
                                accentColor = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            StatusMeterPill(
                                label = "GÜVEN",
                                value = career.managerTrust,
                                accentColor = AccentPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 2. ORTA BÖLÜM: YAN YANA 2 KUTU (SOL: LİG PUAN DURUMU | SAĞ: SONRAKİ MAÇ)
        // =====================================================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                // SOL KUTU: LİG PUAN DURUMU (KOMPAKT TABLO)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfacePrimary,
                    border = BorderStroke(1.dp, StrokePremiumStrong),
                    modifier = Modifier
                        .weight(1f)
                        .height(214.dp)
                        .clickable { onOpenLeague() }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PUAN DURUMU",
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = club.leagueName.take(12),
                                fontFamily = OutfitFontFamily,
                                fontSize = 10.sp,
                                color = AccentBright,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("#  KULÜP", fontFamily = SpaceGroteskFontFamily, fontSize = 9.5.sp, color = TextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("O", fontFamily = SpaceGroteskFontFamily, fontSize = 9.5.sp, color = TextSecondary)
                                Text("P", fontFamily = SpaceGroteskFontFamily, fontSize = 9.5.sp, color = AccentBright, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Show 5 rows around user's club or top 5
                        val userRankIdx = career.standings.indexOfFirst { it.clubId == club.id }.coerceAtLeast(0)
                        val startIdx = (userRankIdx - 2).coerceIn(0, (career.standings.size - 5).coerceAtLeast(0))
                        val visibleRows = career.standings.subList(startIdx, (startIdx + 5).coerceAtMost(career.standings.size))

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            visibleRows.forEach { row ->
                                val rank = career.standings.indexOf(row) + 1
                                val isUserClub = row.clubId == club.id
                                Surface(
                                    color = if (isUserClub) AccentPrimary.copy(alpha = 0.18f) else BackgroundPrimary.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = if (isUserClub) BorderStroke(0.8.dp, AccentPrimary) else null,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 6.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = "$rank.",
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.5.sp,
                                                color = if (isUserClub) AccentBright else TextSecondary,
                                                modifier = Modifier.width(18.dp)
                                            )
                                            Text(
                                                text = row.clubName,
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = if (isUserClub) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.sp,
                                                color = if (isUserClub) AccentBright else TextPrimary,
                                                maxLines = 1
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(
                                                text = "${row.played}",
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontSize = 10.5.sp,
                                                color = TextSecondary
                                            )
                                            Text(
                                                text = "${row.points}",
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (isUserClub) AccentBright else TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SAĞ KUTU: SONRAKİ MAÇ (KOMPAKT KART)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = SurfacePrimary,
                    border = BorderStroke(
                        width = if (isMatchToday) 1.5.dp else 1.dp,
                        color = if (isMatchToday) AccentPrimary else StrokePremiumStrong
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(214.dp)
                ) {
                    if (nextFixture != null) {
                        val homeClub = career.clubs.find { it.id == nextFixture.homeClubId } ?: club
                        val awayClub = career.clubs.find { it.id == nextFixture.awayClubId } ?: career.clubs.last()
                        val daysLeft = (nextFixture.dayIndex - career.currentDayIndex).coerceAtLeast(0)

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isMatchToday) "BUGÜN MAÇ VAR" else "SONRAKİ MAÇ",
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = if (isMatchToday) AccentBright else TextPrimary
                                )
                                Surface(
                                    color = BackgroundPrimary,
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.8.dp, StrokePremium)
                                ) {
                                    Text(
                                        text = "H${nextFixture.week}",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = AccentBright,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Club Crests VS Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    ClubCrestBadge(
                                        shortName = homeClub.shortName,
                                        primaryHex = homeClub.primaryColorHex,
                                        secondaryHex = homeClub.secondaryColorHex,
                                        size = 42.dp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = homeClub.shortName,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = TextPrimary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "VS",
                                        fontFamily = BarlowCondensedFontFamily,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        color = AccentBright
                                    )
                                    Text(
                                        text = nextFixture.kickOffTime,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    ClubCrestBadge(
                                        shortName = awayClub.shortName,
                                        primaryHex = awayClub.primaryColorHex,
                                        secondaryHex = awayClub.secondaryColorHex,
                                        size = 42.dp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = awayClub.shortName,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = TextPrimary
                                    )
                                }
                            }

                            Text(
                                text = "${nextFixture.dateLabel} • ${nextFixture.stadiumName}",
                                fontFamily = OutfitFontFamily,
                                fontSize = 10.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )

                            if (isMatchToday) {
                                Surface(
                                    color = AccentPrimary,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, AccentBright),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onPlayNextMatch() }
                                        .testTag("btn_play_match_now")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SportsSoccer,
                                            contentDescription = null,
                                            tint = Color(0xFF04150D),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "MAÇA ÇIK (${career.nextMatchStartingStatus.labelTr})",
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF04150D)
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    color = BackgroundPrimary.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, StrokePremium),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "$daysLeft Gün Kaldı • Kadro: ${career.nextMatchStartingStatus.labelTr}",
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.5.sp,
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Sezon Tamamlandı", color = TextSecondary)
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 3. BİLDİRİMLER & GÜNLÜK YAPILACAKLAR (TOPLANTI, ANTRENMAN, TRANSFER)
        // =====================================================================
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = SurfacePrimary,
                border = BorderStroke(1.dp, StrokePremiumStrong),
                modifier = Modifier.fillMaxWidth()
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = AccentBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BİLDİRİMLER & YAPILACAKLAR",
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Saat $clockStr",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.sp,
                            color = AccentBright,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Active Dilemma / Meeting notification if any
                    career.activeDilemma?.let { dilemma ->
                        Surface(
                            color = SurfaceElevated,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, AccentBright),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dilemma_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "KRİTİK KARAR • ${dilemma.speakerName} (${dilemma.speakerRole})",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    color = AccentBright
                                )
                                Text(dilemma.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text(dilemma.narrative, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                dilemma.choices.forEach { choice ->
                                    Surface(
                                        color = BackgroundPrimary,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, StrokePremiumStrong),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onResolveDilemma(choice) }
                                            .testTag("dilemma_choice_${choice.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(choice.label, style = MaterialTheme.typography.labelMedium, color = AccentBright, fontWeight = FontWeight.Bold)
                                            Text("Seç ▸", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 1. Antrenman Bildirimi
                    HubNotificationRow(
                        icon = Icons.Default.FitnessCenter,
                        timeTag = "11:00",
                        title = if (career.trainedToday) "Günlük Antrenman Tamamlandı ✓" else "${club.name} Tesisleri • Antrenman Programı",
                        subtitle = if (career.trainedToday) "Teknik heyet bugünkü çalışmanı kaydetti." else "Bireysel ve taktik gelişim çalışması için tesislere git.",
                        badgeText = if (career.trainedToday) "TAMAM" else "BEKLİYOR",
                        badgeColor = if (career.trainedToday) TextSecondary else AccentBright,
                        onClick = onOpenTraining,
                        testTag = "btn_open_training"
                    )

                    // 2. Menajer & Transfer Toplantısı Bildirimi
                    HubNotificationRow(
                        icon = Icons.Default.SwapHoriz,
                        timeTag = "15:00",
                        title = if (career.transferOffers.isNotEmpty()) "Menajer Toplantısı: ${career.transferOffers.size} Resmi Teklif Var!" else "Sözleşme & Menajerlik Dosyası",
                        subtitle = "Menajerin ${career.contract.agentName} ile transfer ve maaş (${FLFormatters.formatWage(career.contract.weeklyWageEur)}) gündemi.",
                        badgeText = if (career.transferOffers.isNotEmpty()) "${career.transferOffers.size} TEKLİF" else "GÖRÜŞ",
                        badgeColor = if (career.transferOffers.isNotEmpty()) GoldPrimary else TextPrimary,
                        onClick = onOpenTransfers,
                        testTag = "btn_open_transfers"
                    )

                    // 2b. Teknik Direktör Odası (Canlı AI Görüşme & Kadro/Taktik Tartışması)
                    HubNotificationRow(
                        icon = Icons.Default.Groups,
                        timeTag = "16:30",
                        title = "Teknik Direktör Odası • ${club.managerName}",
                        subtitle = "İlk 11 talebi, taktik sorumluluk veya hoca ile birebir serbest diyalog başlat.",
                        badgeText = "ODAYA GİT",
                        badgeColor = AccentBright,
                        onClick = onOpenCoachMeeting,
                        testTag = "btn_open_coach_meeting"
                    )

                    // 2c. Canlı Sesli Telefon Görüşmesi (Menajer, Aile, Başkan, Hoca, Basın)
                    val pendingCall = career.callRecords.firstOrNull { !it.completed }
                    HubNotificationRow(
                        icon = Icons.Default.Call,
                        timeTag = "18:00",
                        title = if (pendingCall != null) {
                            "Gelen Çağrı: ${pendingCall.callerName} (${pendingCall.callerRole})"
                        } else {
                            "Canlı Telefon Görüşmesi • Menajer, Aile, Başkan & Hoca"
                        },
                        subtitle = pendingCall?.topic
                            ?: "Menajerin, ailen, kulüp başkanı veya teknik direktör ile sesli konuş.",
                        badgeText = if (pendingCall != null) "YANITLA" else "SESLİ ARA",
                        badgeColor = if (pendingCall != null) GoldPrimary else AccentBright,
                        onClick = { onOpenVoiceCall(pendingCall?.id) },
                        testTag = "btn_open_voice_call"
                    )

                    // 3. Sosyal Yaşam & Transfermarkt Hızlı Erişim Satırı
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = BackgroundPrimary.copy(alpha = 0.78f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, StrokePremium),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenSocialLife() }
                                .testTag("btn_open_social_life")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.LocalCafe, contentDescription = null, tint = AccentBright, modifier = Modifier.size(16.dp))
                                Column {
                                    Text("Sosyal Hayat", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                                    Text("${2 - career.socialActionCountToday} Aktivite Hakkı", fontFamily = OutfitFontFamily, fontSize = 10.sp, color = TextSecondary)
                                }
                            }
                        }

                        Surface(
                            color = BackgroundPrimary.copy(alpha = 0.78f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, StrokePremium),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onOpenProfileCard() }
                                .testTag("btn_open_profile_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF00A6FF), modifier = Modifier.size(16.dp))
                                Column {
                                    Text("Transfermarkt", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                                    Text("Oyuncu Profili", fontFamily = OutfitFontFamily, fontSize = 10.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 4. ALT BÖLÜM: GELEN MESAJLAR (WHATSAPP & INSTAGRAM DM BİLDİRİMLERİ)
        // =====================================================================
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = SurfacePrimary,
                border = BorderStroke(1.dp, StrokePremiumStrong),
                modifier = Modifier.fillMaxWidth()
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = null,
                                tint = AccentBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GELEN MESAJLAR",
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                        }
                        Text(
                            text = "Tümünü Aç ▸",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = AccentBright,
                            modifier = Modifier.clickable { onOpenPhone() }
                        )
                    }

                    // Gather latest WhatsApp messages & Instagram DM requests/messages
                    val recentWhatsApp = career.npcs
                        .filter { it.messages.isNotEmpty() }
                        .sortedByDescending { it.unreadCount }
                        .take(3)

                    recentWhatsApp.forEach { npc ->
                        val lastMsg = npc.messages.last()
                        Surface(
                            color = BackgroundPrimary.copy(alpha = 0.82f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (npc.unreadCount > 0) Color(0xFF25D366).copy(alpha = 0.65f) else StrokePremium
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPhone() }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
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
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${npc.name} (${npc.roleTitle})",
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = lastMsg.timeLabel,
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 10.sp,
                                            color = Color(0xFF25D366)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = lastMsg.text,
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 11.5.sp,
                                        color = if (npc.unreadCount > 0) TextPrimary else TextSecondary,
                                        maxLines = 1
                                    )
                                    if (!lastMsg.impactSummary.isNullOrBlank()) {
                                        Text(
                                            text = "Etki: ${lastMsg.impactSummary}",
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentBright
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Show latest Instagram DM / Pending Request notification
                    val latestInstaDm = career.instaDmThreads.firstOrNull { it.isPendingRequest }
                        ?: career.instaDmThreads.firstOrNull()
                    if (latestInstaDm != null) {
                        val lastInstaMsg = latestInstaDm.messages.lastOrNull()
                        Surface(
                            color = BackgroundPrimary.copy(alpha = 0.82f),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFFE1306C).copy(alpha = 0.55f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenPhone() }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
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
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${latestInstaDm.senderHandle} • ${if (latestInstaDm.isPendingRequest) "Mesaj İsteği" else "Instagram Direct"}",
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = lastInstaMsg?.timeLabel ?: "Şimdi",
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 10.sp,
                                            color = Color(0xFFE1306C)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = lastInstaMsg?.text ?: "Yeni mesaj isteği",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 11.5.sp,
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                    if (!lastInstaMsg?.impactSummary.isNullOrBlank()) {
                                        Text(
                                            text = "Etki: ${lastInstaMsg?.impactSummary}",
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF3797F0)
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
}

@Composable
private fun HubNotificationRow(
    icon: ImageVector,
    timeTag: String,
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = BackgroundPrimary.copy(alpha = 0.80f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, StrokePremium),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, StrokePremium)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = badgeColor,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = timeTag,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = AccentBright
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = subtitle,
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(0.8.dp, badgeColor.copy(alpha = 0.6f))
            ) {
                Text(
                    text = badgeText,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.5.sp,
                    color = badgeColor,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun TrainingCenterScreen(
    career: CareerSaveState,
    onSelectDrill: (TrainingDrillDefinition) -> Unit,
    onBack: () -> Unit
) {
    val p = career.player
    val attr = p.attributes
    var selectedCategory by remember { mutableStateOf("TÜMÜ") }
    val categories = listOf("TÜMÜ", "HÜCUM", "TEKNİK", "FİZİKSEL", "SAVUNMA", "TAKTİK", "ÖZEL", "DİNLENME")

    val filteredDrills = remember(selectedCategory) {
        if (selectedCategory == "TÜMÜ") {
            WorldDataCatalog.trainingDrills
        } else {
            WorldDataCatalog.trainingDrills.filter { it.category == selectedCategory }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPitchSurface)
            .padding(16.dp)
            .testTag("training_center_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Facility Hero Header
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DeepStadiumGreen,
                border = BorderStroke(1.dp, CrispWhiteBorderStrong),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_training_ground),
                        contentDescription = "Antrenman Tesisleri",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .alpha(0.45f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, DeepStadiumGreen)
                                )
                            )
                    )
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_from_training")) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = PrimaryWhite)
                                }
                                Column {
                                    Text(
                                        text = "${career.currentClub.name.uppercase()} TESİSLERİ",
                                        fontFamily = TekoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        color = PrimaryWhite
                                    )
                                    Text(
                                        text = "Tesis Kalitesi: ${career.currentClub.facilityQuality}/5 Yıldız • Genel Reyting: ${p.overallRating} (Potansiyel: ${p.potential})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VibrantFootballGreen
                                    )
                                }
                            }
                            Surface(
                                color = DarkPitchSurface.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CrispWhiteBorder)
                            ) {
                                Text(
                                    text = "ENERJİ %${p.energy}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (p.energy >= 55) BrightNeonGreen else MatchDrawAmber,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Current 13 Detailed Sub-Attributes Summary Strip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MiniSubStatBadge("Şut", attr.shooting)
                            MiniSubStatBadge("Pas", attr.passing)
                            MiniSubStatBadge("Dribbling", attr.dribbling)
                            MiniSubStatBadge("İlk Kontrol", attr.firstTouch)
                            MiniSubStatBadge("Hız", attr.pace)
                            MiniSubStatBadge("Çeviklik", attr.agility)
                            MiniSubStatBadge("Dayanıklılık", attr.stamina)
                            MiniSubStatBadge("Fizik Güç", attr.strength)
                            MiniSubStatBadge("Savunma", attr.defending)
                            MiniSubStatBadge("Taktik", attr.tactics)
                            MiniSubStatBadge("Görüş", attr.vision)
                            MiniSubStatBadge("Duran Top", attr.setPieces)
                        }
                    }
                }
            }
        }

        // Category Filter Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VibrantFootballGreen,
                            selectedLabelColor = DarkPitchSurface
                        )
                    )
                }
            }
        }

        items(filteredDrills, key = { it.id }) { drill ->
            val isRecommended = p.primaryPosition in drill.recommendedPositions
            Surface(
                color = DeepStadiumGreen,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    if (isRecommended) VibrantFootballGreen else CrispWhiteBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = drill.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PrimaryWhite
                                )
                                if (isRecommended) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = VibrantFootballGreen.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "MEVKİ UYUMLU",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BrightNeonGreen,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = drill.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryMintGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Gelişim: +${drill.primaryAttributeName} & +${drill.secondaryAttributeName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = ChampionshipGold
                            )
                            Text(
                                text = if (drill.energyCost >= 0) {
                                    "Kondisyon Tüketimi: -%${drill.energyCost} • Sakatlık Riski: %${drill.injuryRiskPct}"
                                } else {
                                    "Enerji Yenileme: +%${-drill.energyCost} • Sıfır Sakatlık Riski"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryMintGray
                            )
                        }

                        Button(
                            onClick = { onSelectDrill(drill) },
                            enabled = !career.trainedToday,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VibrantFootballGreen,
                                contentColor = DarkPitchSurface
                            ),
                            modifier = Modifier.testTag("btn_drill_${drill.id}")
                        ) {
                            Text(
                                text = if (career.trainedToday) "TAMAMLANDI" else "UYGULA",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniSubStatBadge(label: String, value: Int) {
    Surface(
        color = DarkPitchSurface.copy(alpha = 0.85f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, CrispWhiteBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
            Text(
                text = value.toString(),
                fontFamily = TekoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = if (value >= 75) BrightNeonGreen else PrimaryWhite
            )
        }
    }
}

@Composable
fun CalendarScreen(
    career: CareerSaveState,
    onAdvanceNextDay: () -> Unit,
    onAdvanceToMatchDay: () -> Unit,
    onPlayMatchToday: () -> Unit,
    onOpenTraining: () -> Unit = {}
) {
    var viewMode by remember { mutableStateOf("AYLIK") } // Default: AYLIK (7-column FC calendar)
    var selectedMonthIdx by remember(career.currentDayIndex) {
        mutableStateOf(CareerEngine.getMonthIndexForDay(career.currentDayIndex))
    }
    var selectedDayIndex by remember(career.currentDayIndex) {
        mutableStateOf(career.currentDayIndex)
    }

    val isMatchToday = career.todayUserFixture != null
    val monthGrid = remember(career, selectedMonthIdx) {
        CareerEngine.getMonthCalendarGrid(career, selectedMonthIdx)
    }

    val selectedDayEvents = remember(career.calendarEvents, selectedDayIndex) {
        career.calendarEvents.filter { it.dayIndex == selectedDayIndex }
    }
    val selectedDayFixture = remember(career.fixtures, selectedDayIndex, career.contract.clubId) {
        career.fixtures.find {
            it.dayIndex == selectedDayIndex &&
                (it.homeClubId == career.contract.clubId || it.awayClubId == career.contract.clubId)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(16.dp)
            .testTag("calendar_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Coated Schedule Header + Custom Segmented AYLIK / HAFTALIK Switcher
        item {
            Surface(
                color = SurfacePrimary,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, StrokePremiumStrong),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(SurfaceElevated.copy(alpha = 0.65f), SurfacePrimary)
                            )
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "KARİYER TAKVİMİ",
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 30.sp,
                                letterSpacing = 0.6.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "GÜN ${career.currentDayIndex} • ${CareerEngine.formatGameDate(career.currentDayIndex, career.currentSeasonYear)}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.5.sp,
                                color = AccentBright
                            )
                        }

                        // Custom Segmented Toggle Pill
                        Surface(
                            color = BackgroundPrimary.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, StrokePremium)
                        ) {
                            Row(modifier = Modifier.padding(3.dp)) {
                                listOf("AYLIK", "HAFTALIK").forEach { mode ->
                                    val active = viewMode == mode
                                    Surface(
                                        color = if (active) AccentPrimary else Color.Transparent,
                                        shape = RoundedCornerShape(9.dp),
                                        modifier = Modifier
                                            .clickable { viewMode = mode }
                                            .testTag("calendar_mode_$mode")
                                    ) {
                                        Text(
                                            text = mode,
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (active) BackgroundPrimary else TextSecondary,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Time advancement control bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isMatchToday) {
                            FLPrimaryButton(
                                text = "BUGÜNKÜ MAÇA ÇIK",
                                onClick = onPlayMatchToday,
                                icon = Icons.Default.SportsSoccer,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "btn_calendar_play_today"
                            )
                        } else {
                            FLPrimaryButton(
                                text = "SONRAKİ GÜN",
                                onClick = onAdvanceNextDay,
                                icon = Icons.Default.SkipNext,
                                modifier = Modifier.weight(1f),
                                testTag = "btn_calendar_next_day"
                            )
                            FLSecondaryButton(
                                text = "MAÇ GÜNÜNE İLERLE",
                                onClick = onAdvanceToMatchDay,
                                icon = Icons.Default.FastForward,
                                borderColor = StrokePremiumStrong,
                                textColor = AccentBright,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_calendar_to_match_day")
                            )
                        }
                    }
                }
            }
        }

        if (viewMode == "AYLIK") {
            // 2. REAL 7-COLUMN MONTHLY GRID (Pzt - Sal - Çar - Per - Cum - Cmt - Paz)
            item {
                Surface(
                    color = SurfacePrimary,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, StrokePremiumStrong),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("monthly_calendar_grid_card")
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        SurfaceElevated.copy(alpha = 0.55f),
                                        SurfacePrimary,
                                        BackgroundSecondary
                                    )
                                )
                            )
                            .padding(14.dp)
                    ) {
                        // Month Navigation Bar (< AĞUSTOS 2026 >)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = BackgroundPrimary.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, StrokePremium),
                                modifier = Modifier.clickable(enabled = selectedMonthIdx > 0) {
                                    selectedMonthIdx = (selectedMonthIdx - 1).coerceAtLeast(0)
                                }
                            ) {
                                Text(
                                    text = "‹ ÖNCEKİ",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (selectedMonthIdx > 0) TextPrimary else TextSecondary.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = monthGrid.monthTitleTr.uppercase(),
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 26.sp,
                                    letterSpacing = 0.8.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.testTag("calendar_month_title")
                                )
                                Text(
                                    text = "${career.currentClub.leagueName} Takvimi",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                            Surface(
                                color = BackgroundPrimary.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, StrokePremium),
                                modifier = Modifier.clickable(enabled = selectedMonthIdx < 9) {
                                    selectedMonthIdx = (selectedMonthIdx + 1).coerceAtMost(9)
                                }
                            ) {
                                Text(
                                    text = "SONRAKİ ›",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (selectedMonthIdx < 9) TextPrimary else TextSecondary.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 7-Column Weekday Header Row
                        val weekDays = listOf("PZT", "SAL", "ÇAR", "PER", "CUM", "CMT", "PAZ")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(BackgroundPrimary.copy(alpha = 0.65f))
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            weekDays.forEach { wd ->
                                Text(
                                    text = wd,
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 10.5.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 6 Weeks x 7 Columns Grid
                        val weeks = monthGrid.cells.chunked(7)
                        weeks.forEach { weekRow ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                weekRow.forEach { cell ->
                                    val isSelected = cell.dayIndex != null && cell.dayIndex == selectedDayIndex
                                    val cellBg = when {
                                        cell.dayOfMonth == 0 -> Color.Transparent
                                        cell.isCurrentDay -> AccentPrimary.copy(alpha = 0.24f)
                                        isSelected -> SurfaceElevated
                                        cell.isMatchDay -> SurfaceElevated.copy(alpha = 0.85f)
                                        else -> BackgroundPrimary.copy(alpha = 0.68f)
                                    }
                                    val cellBorder = when {
                                        cell.dayOfMonth == 0 -> Color.Transparent
                                        cell.isCurrentDay -> AccentBright
                                        isSelected -> TextPrimary
                                        cell.isMatchDay -> AccentPrimary.copy(alpha = 0.7f)
                                        else -> StrokePremium
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(cellBg)
                                            .clickable(enabled = cell.dayIndex != null) {
                                                cell.dayIndex?.let { selectedDayIndex = it }
                                            }
                                            .let { mod ->
                                                if (cell.dayIndex != null) mod.testTag("cal_day_${cell.dayIndex}") else mod
                                            },
                                        contentAlignment = Alignment.TopCenter
                                    ) {
                                        if (cell.dayOfMonth > 0) {
                                            Surface(
                                                color = Color.Transparent,
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(
                                                    if (cell.isCurrentDay || isSelected) 1.4.dp else 0.8.dp,
                                                    cellBorder
                                                ),
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(4.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = "${cell.dayOfMonth}",
                                                        fontFamily = SpaceGroteskFontFamily,
                                                        fontSize = 11.sp,
                                                        fontWeight = if (cell.isCurrentDay || cell.isMatchDay) FontWeight.Bold else FontWeight.Medium,
                                                        color = when {
                                                            cell.dayIndex == null -> TextSecondary.copy(alpha = 0.30f)
                                                            cell.isCurrentDay -> AccentBright
                                                            cell.isMatchDay -> TextPrimary
                                                            else -> TextSecondary
                                                        }
                                                    )

                                                    // Event Marker Badge / Micro Tag
                                                    if (cell.isMatchDay) {
                                                        val fx = cell.fixture
                                                        val oppShort = if (fx != null) {
                                                            if (fx.homeClubId == career.contract.clubId) fx.awayClubName.take(3).uppercase()
                                                            else fx.homeClubName.take(3).uppercase()
                                                        } else "MAÇ"
                                                        Surface(
                                                            color = if (fx?.played == true) SurfacePrimary else AccentPrimary,
                                                            shape = RoundedCornerShape(4.dp),
                                                            border = if (fx?.played == true) BorderStroke(0.8.dp, AccentBright) else null
                                                        ) {
                                                            Text(
                                                                text = if (fx?.played == true) "${fx.homeScore}-${fx.awayScore}" else oppShort,
                                                                fontFamily = SpaceGroteskFontFamily,
                                                                fontSize = 8.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (fx?.played == true) AccentBright else BackgroundPrimary,
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    } else if (cell.isTrainingDay) {
                                                        Box(
                                                            modifier = Modifier
                                                                .width(16.dp)
                                                                .height(4.dp)
                                                                .clip(CircleShape)
                                                                .background(AccentPrimary.copy(alpha = 0.85f))
                                                        )
                                                    } else if (cell.isSocialOrTransferDay) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .clip(CircleShape)
                                                                .background(GoldPrimary)
                                                        )
                                                    } else {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Calendar Legend Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CalendarLegendItem(color = AccentBright, label = "Bugün")
                            CalendarLegendItem(color = AccentPrimary, label = "Maç Günü")
                            CalendarLegendItem(color = AccentPrimary.copy(alpha = 0.65f), label = "Antrenman")
                            CalendarLegendItem(color = GoldPrimary, label = "Etkinlik")
                        }
                    }
                }
            }

            // 3. SEÇİLİ GÜN DETAYLARI PANELİ (Coated Dossier Panel)
            item {
                val isSelectedToday = selectedDayIndex == career.currentDayIndex
                val isSelectedFuture = selectedDayIndex > career.currentDayIndex
                Surface(
                    color = SurfacePrimary,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        1.2.dp,
                        if (isSelectedToday) AccentPrimary else StrokePremiumStrong
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selected_day_detail_panel")
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.verticalGradient(
                                    listOf(SurfaceElevated.copy(alpha = 0.60f), SurfacePrimary)
                                )
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SEÇİLİ GÜN PROGRAMI",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    color = AccentBright
                                )
                                Text(
                                    text = "GÜN $selectedDayIndex • ${CareerEngine.formatGameDate(selectedDayIndex, career.currentSeasonYear)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (isSelectedToday) {
                                Surface(
                                    color = AccentPrimary,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "BUGÜN",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 10.5.sp,
                                        color = BackgroundPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        if (selectedDayFixture != null) {
                            val homeClub = career.clubs.find { it.id == selectedDayFixture.homeClubId } ?: career.currentClub
                            val awayClub = career.clubs.find { it.id == selectedDayFixture.awayClubId } ?: career.clubs.last()
                            Surface(
                                color = BackgroundPrimary.copy(alpha = 0.82f),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, StrokePremiumStrong),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "HAFTA ${selectedDayFixture.week} KARŞILAŞMASI",
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = AccentBright
                                        )
                                        Text(
                                            text = "${selectedDayFixture.kickOffTime} • ${selectedDayFixture.stadiumName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            ClubCrestBadge(
                                                shortName = homeClub.shortName,
                                                primaryHex = homeClub.primaryColorHex,
                                                secondaryHex = homeClub.secondaryColorHex,
                                                size = 38.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = selectedDayFixture.homeClubName,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = TextPrimary
                                            )
                                        }
                                        Surface(
                                            color = SurfaceElevated,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, StrokePremium)
                                        ) {
                                            Text(
                                                text = if (selectedDayFixture.played) {
                                                    "${selectedDayFixture.homeScore} - ${selectedDayFixture.awayScore}"
                                                } else {
                                                    "VS"
                                                },
                                                fontFamily = BarlowCondensedFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp,
                                                color = if (selectedDayFixture.played) AccentBright else TextPrimary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = selectedDayFixture.awayClubName,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = TextPrimary,
                                                textAlign = TextAlign.End
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            ClubCrestBadge(
                                                shortName = awayClub.shortName,
                                                primaryHex = awayClub.primaryColorHex,
                                                secondaryHex = awayClub.secondaryColorHex,
                                                size = 38.dp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    if (selectedDayFixture.played) {
                                        Text(
                                            text = "Karşılaşma tamamlandı • Maç Skoru: ${selectedDayFixture.homeScore} - ${selectedDayFixture.awayScore}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = AccentBright
                                        )
                                    } else if (isSelectedToday) {
                                        FLPrimaryButton(
                                            text = "MAÇA ÇIK (${career.nextMatchStartingStatus.labelTr})",
                                            onClick = onPlayMatchToday,
                                            icon = Icons.Default.SportsSoccer,
                                            modifier = Modifier.fillMaxWidth(),
                                            testTag = "btn_selected_day_play_match"
                                        )
                                    } else if (isSelectedFuture) {
                                        Text(
                                            text = "Bu karşılaşma Gün $selectedDayIndex tarihinde oynanacak. Oynamak için takvimi ilerletin.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        selectedDayEvents.forEach { ev ->
                            Surface(
                                color = BackgroundPrimary.copy(alpha = 0.78f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, StrokePremium),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${ev.eventType} • ${ev.timeLabel}",
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.5.sp,
                                            color = AccentBright
                                        )
                                        Text(
                                            text = ev.location,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ev.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = ev.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    ev.outcomeSummary?.let { out ->
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Rapor: $out",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AccentBright
                                        )
                                    }
                                }
                            }
                        }

                        if (isSelectedToday && !career.trainedToday && selectedDayFixture == null) {
                            FLSecondaryButton(
                                text = "BUGÜNKÜ ANTRENMAN TESİSLERİNE GİT",
                                onClick = onOpenTraining,
                                icon = Icons.Default.FitnessCenter,
                                borderColor = AccentPrimary,
                                textColor = AccentBright,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        } else {
            // HAFTALIK GÖRÜNÜM LİSTESİ
            val weeklyEvents = career.calendarEvents.filter {
                it.dayIndex in career.currentDayIndex..(career.currentDayIndex + 10)
            }
            items(weeklyEvents, key = { it.id }) { ev ->
                val isToday = ev.dayIndex == career.currentDayIndex
                val isMatch = ev.eventType == "MAÇ"
                Surface(
                    color = if (isToday) SurfaceElevated else SurfacePrimary,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = if (isToday || isMatch) 1.2.dp else 1.dp,
                        color = when {
                            isToday -> AccentBright
                            isMatch -> AccentPrimary
                            else -> StrokePremium
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "GÜN ${ev.dayIndex} • ${ev.dateLabel} • ${ev.eventType}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isMatch) AccentBright else TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(ev.title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(ev.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
fun PlayerProfileArchiveScreen(
    career: CareerSaveState,
    onBack: () -> Unit
) {
    val p = career.player
    val club = career.currentClub
    val attr = p.attributes
    val stats = p.currentSeasonStats

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPitchSurface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = PrimaryWhite)
                }
                Column {
                    Text(
                        text = "OYUNCU PROFİLİ VE KARİYER İSTATİSTİKLERİ",
                        fontFamily = TekoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = PrimaryWhite
                    )
                    Text(
                        text = p.dataSourceInfo,
                        style = MaterialTheme.typography.labelSmall,
                        color = VibrantFootballGreen
                    )
                }
            }
        }

        item {
            BroadcastPlayerCard(
                fullName = p.fullName,
                kitName = p.kitName,
                kitNumber = p.kitNumber,
                position = p.primaryPosition,
                overallRating = p.overallRating,
                potential = p.potential,
                clubName = club.name,
                clubShortName = club.shortName,
                clubPrimaryHex = club.primaryColorHex,
                clubSecondaryHex = club.secondaryColorHex,
                nationality = p.nationality,
                attributes = attr,
                appearance = p.appearance
            )
        }

        // Contract & Bio Summary
        item {
            Surface(
                color = DeepStadiumGreen,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CrispWhiteBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("KİŞİSEL KİMLİK & SÖZLEŞME", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
                    Text("Yaş: ${p.age} (${p.birthDate}) • Doğum Yeri: ${p.city}, ${p.country}", style = MaterialTheme.typography.bodySmall, color = PrimaryWhite)
                    Text("Boy / Kilo: ${p.heightCm} cm / ${p.weightKg} kg • Baskın Ayak: ${p.preferredFoot.labelTr}", style = MaterialTheme.typography.bodySmall, color = PrimaryWhite)
                    Text("Piyasa Değeri: ${p.marketValueEur} € • Maaş: ${career.contract.weeklyWageEur} €/Hafta", style = MaterialTheme.typography.bodySmall, color = BrightNeonGreen)
                    Text("Sözleşme: ${career.contract.startSeasonYear} - ${career.contract.endSeasonYear} (${career.contract.role.labelTr})", style = MaterialTheme.typography.bodySmall, color = SecondaryMintGray)
                    Text("Geçmiş: ${p.backgroundStory}", style = MaterialTheme.typography.bodySmall, color = SecondaryMintGray)
                }
            }
        }

        // Current Season Stats
        item {
            Surface(
                color = DeepStadiumGreen,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GoldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${career.currentSeasonYear}/${career.currentSeasonYear + 1} SEZONU İSTATİSTİKLERİ", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatBox("MAÇ", stats.appearances.toString())
                        StatBox("İLK 11", stats.starts.toString())
                        StatBox("DAKİKA", stats.minutesPlayed.toString())
                        StatBox("GOL", stats.goals.toString())
                        StatBox("ASİST", stats.assists.toString())
                        StatBox("ORT. PUAN", stats.averageRating.toString())
                    }
                }
            }
        }

        // Detailed 13 Attributes Breakdown
        item {
            Surface(
                color = DeepStadiumGreen,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CrispWhiteBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("DETAYLI ALT ÖZELLİKLER", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
                    AttributeBarRow("Şut / Bitiricilik", attr.shooting)
                    AttributeBarRow("Pas İsabeti", attr.passing)
                    AttributeBarRow("Dribbling / Çalım", attr.dribbling)
                    AttributeBarRow("İlk Kontrol", attr.firstTouch)
                    AttributeBarRow("Hız / Sprint", attr.pace)
                    AttributeBarRow("Çeviklik", attr.agility)
                    AttributeBarRow("Dayanıklılık", attr.stamina)
                    AttributeBarRow("Fiziksel Güç", attr.strength)
                    AttributeBarRow("Savunma / Müdahale", attr.defending)
                    AttributeBarRow("Taktik Disiplin", attr.tactics)
                    AttributeBarRow("Oyun Görüşü", attr.vision)
                    AttributeBarRow("Duran Toplar", attr.setPieces)
                }
            }
        }

        // Past Seasons Archive
        if (p.careerArchive.isNotEmpty()) {
            item {
                Surface(
                    color = DeepStadiumGreen,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CrispWhiteBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("KARİYER ARŞİVİ VE KUPALAR", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
                        p.careerArchive.forEach { rec ->
                            Surface(
                                color = DarkPitchSurface,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "${rec.seasonYear}/${rec.seasonYear + 1} • ${rec.clubName} (${rec.leagueName})",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = PrimaryWhite
                                    )
                                    Text(
                                        text = "${rec.appearances} Maç (${rec.starts} İlk 11) • ${rec.goals} Gol • ${rec.assists} Asist • Ort: ${rec.averageRating}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VibrantFootballGreen
                                    )
                                    if (rec.trophiesWon.isNotEmpty()) {
                                        Text(
                                            text = "Kupalar: ${rec.trophiesWon.joinToString(", ")}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BrightGold
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
}

@Composable
private fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
        Text(
            text = value,
            fontFamily = TekoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = BrightNeonGreen
        )
    }
}

@Composable
private fun AttributeBarRow(name: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = PrimaryWhite,
            modifier = Modifier.width(130.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (value.coerceIn(1, 99) / 100f))
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            value >= 80 -> BrightNeonGreen
                            value >= 65 -> VibrantFootballGreen
                            else -> ChampionshipGold
                        }
                    )
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = PrimaryWhite,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.End
        )
    }
}
