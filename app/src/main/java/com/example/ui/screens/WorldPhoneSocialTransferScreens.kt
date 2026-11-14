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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.WorldDataCatalog
import com.example.model.CareerSaveState
import com.example.model.Club
import com.example.model.LifestylePurchaseItem
import com.example.model.LifestyleVenueAction
import com.example.model.TransferOffer
import com.example.ui.components.ClubCrestBadge
import com.example.ui.components.parseHexColor
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
import com.example.ui.theme.MatchWinGreen
import com.example.ui.theme.PrimaryWhite
import com.example.ui.theme.SecondaryMintGray
import com.example.ui.theme.TekoFontFamily
import com.example.ui.theme.VibrantFootballGreen

import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StrokePremium
import com.example.ui.theme.StrokePremiumStrong
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LeagueAndWorldScreen(
    career: CareerSaveState,
    onOpenTransfers: () -> Unit,
    onOpenProfileArchive: () -> Unit,
    onPlayUserMatch: () -> Unit = {}
) {
    var section by remember { mutableStateOf("PUAN DURUMU") }
    var inspectedClub by remember { mutableStateOf<Club?>(null) }
    var playerSearchQuery by remember { mutableStateOf("") }
    var showSummaryDeck by remember { mutableStateOf(true) }

    val sections = listOf(
        "PUAN DURUMU",
        "FİKSTÜR & SONUÇLAR",
        "GOL KRALLIĞI",
        "ASİST KRALLIĞI",
        "KULÜPLER",
        "OYUNCULAR",
        "TRANSFERLER",
        "FORM TABLOSU",
        "LİG HABERLERİ"
    )

    val clubsById = remember(career.clubs) { career.clubs.associateBy { it.id } }
    val userStandingIdx = career.standings.indexOfFirst { it.clubId == career.contract.clubId }
    val userRank = if (userStandingIdx >= 0) userStandingIdx + 1 else 1
    val leaderRow = career.standings.firstOrNull()
    val topScorer = career.topScorers.maxByOrNull { it.goals }
    val topAssister = career.topScorers.maxByOrNull { it.assists }
    val nextFixture = career.todayUserFixture ?: career.nextUnplayedUserFixture

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BackgroundSecondary, BackgroundPrimary)
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("league_world_screen")
    ) {
        // Coated League Header Bar
        Surface(
            color = SurfacePrimary,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, StrokePremiumStrong),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        ClubCrestBadge(
                            shortName = career.currentClub.shortName,
                            primaryHex = career.currentClub.primaryColorHex,
                            secondaryHex = career.currentClub.secondaryColorHex,
                            size = 44.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = career.currentClub.leagueName.uppercase(),
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                letterSpacing = 0.6.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "${career.currentSeasonYear}/${career.currentSeasonYear + 1} SEZONU • SIRA #$userRank",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp,
                                color = AccentBright
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = if (showSummaryDeck) AccentPrimary.copy(alpha = 0.2f) else SurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (showSummaryDeck) AccentBright else StrokePremium),
                            modifier = Modifier.clickable { showSummaryDeck = !showSummaryDeck }
                        ) {
                            Text(
                                text = if (showSummaryDeck) "Özeti Gizle" else "Lig Özeti",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                        Surface(
                            color = SurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, StrokePremiumStrong),
                            modifier = Modifier.clickable(onClick = onOpenTransfers)
                        ) {
                            Text(
                                text = "Transfer",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = AccentBright,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // 6 Interactive League Summary Cards (Standings, Scorers, Assists, Upcoming Fixtures, Form, Club Spotlight)
                if (showSummaryDeck && inspectedClub == null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LeagueSummaryMiniCard(
                            tag = "PUAN DURUMU",
                            title = leaderRow?.clubName ?: career.currentClub.name,
                            metric = "${leaderRow?.points ?: 0} Puan (Lider)",
                            subMetric = "Sen: #$userRank. Sıra",
                            isActive = section == "PUAN DURUMU",
                            onClick = { section = "PUAN DURUMU" }
                        )
                        LeagueSummaryMiniCard(
                            tag = "GOL KRALLIĞI",
                            title = topScorer?.playerName ?: career.player.fullName,
                            metric = "${topScorer?.goals ?: 0} Gol",
                            subMetric = topScorer?.clubName ?: career.currentClub.name,
                            isActive = section == "GOL KRALLIĞI",
                            onClick = { section = "GOL KRALLIĞI" }
                        )
                        LeagueSummaryMiniCard(
                            tag = "ASİST KRALLIĞI",
                            title = topAssister?.playerName ?: career.player.fullName,
                            metric = "${topAssister?.assists ?: 0} Asist",
                            subMetric = topAssister?.clubName ?: career.currentClub.name,
                            isActive = section == "ASİST KRALLIĞI",
                            onClick = { section = "ASİST KRALLIĞI" }
                        )
                        LeagueSummaryMiniCard(
                            tag = "SIRADAKİ FİKSTÜR",
                            title = if (nextFixture != null) "${nextFixture.homeClubName} - ${nextFixture.awayClubName}" else "Sezon Tamamlandı",
                            metric = if (nextFixture != null) "Hafta ${nextFixture.week} • ${nextFixture.kickOffTime}" else "-",
                            subMetric = nextFixture?.stadiumName ?: "",
                            isActive = section == "FİKSTÜR & SONUÇLAR",
                            onClick = { section = "FİKSTÜR & SONUÇLAR" }
                        )
                        LeagueSummaryMiniCard(
                            tag = "FORM TABLOSU",
                            title = career.currentClub.name,
                            metric = (career.standings.find { it.clubId == career.contract.clubId }?.recentForm ?: listOf("G", "B", "G")).joinToString(" "),
                            subMetric = "Son 5 Maç Formu",
                            isActive = section == "FORM TABLOSU",
                            onClick = { section = "FORM TABLOSU" }
                        )
                        LeagueSummaryMiniCard(
                            tag = "KULÜP VİTRİNİ",
                            title = "${career.clubs.size} Profesyonel Kulüp",
                            metric = "Güç Ort. ${career.clubs.map { it.teamStrength }.average().toInt()}",
                            subMetric = "Kadroları İncele",
                            isActive = section == "KULÜPLER",
                            onClick = { section = "KULÜPLER" }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Pill Section Navigation Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sections.forEach { tab ->
                val isSelected = section == tab
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        section = tab
                        inspectedClub = null
                    },
                    label = {
                        Text(
                            text = tab,
                            fontFamily = OutfitFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentPrimary,
                        selectedLabelColor = Color(0xFF04140C),
                        containerColor = SurfacePrimary,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = StrokePremium,
                        selectedBorderColor = AccentBright
                    ),
                    modifier = Modifier.testTag("league_tab_$tab")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (inspectedClub != null) {
            val club = inspectedClub!!
            val standingRank = career.standings.indexOfFirst { it.clubId == club.id } + 1
            val clubFixtures = career.fixtures.filter { it.homeClubId == club.id || it.awayClubId == club.id }.take(5)
            Surface(
                color = SurfacePrimary,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, StrokePremiumStrong),
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { inspectedClub = null }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                                }
                                ClubCrestBadge(
                                    shortName = club.shortName,
                                    primaryHex = club.primaryColorHex,
                                    secondaryHex = club.secondaryColorHex,
                                    size = 50.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = club.name,
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 20.sp,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "LİG SIRASI: #$standingRank • TAKIM GÜCÜ: ${club.teamStrength}",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 12.sp,
                                        color = AccentBright,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Surface(
                            color = SurfaceElevated,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, StrokePremium),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Teknik Direktör: ${club.managerName} (${club.managerTacticalStyle})\nStadyum: ${club.stadiumName} (${club.stadiumCapacity} Kapasite)\nTesis Kalitesi: ${club.facilityQuality}/5 • Bütçe: ${com.example.ui.components.FLFormatters.formatMarketValue(club.budgetEur)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    item {
                        Text(
                            text = "KULÜP FİKSTÜRÜ",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AccentBright
                        )
                    }
                    items(clubFixtures, key = { "cfx_${it.id}" }) { fx ->
                        Surface(
                            color = BackgroundSecondary,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, StrokePremium),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Hafta ${fx.week}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = "${fx.homeClubName} ${if (fx.played) "${fx.homeScore} - ${fx.awayScore}" else "vs"} ${fx.awayClubName}",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                    item {
                        Text(
                            text = "KADRO OYUNCULARI",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ChampionshipGold
                        )
                    }
                    items(club.keySquadMembers, key = { it.id }) { member ->
                        Surface(
                            color = BackgroundSecondary,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, StrokePremium),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${member.kitNumber} ${member.name} (${member.position.code})${if (member.isCaptain) " [K]" else ""}",
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "GEN ${member.overall} • YAŞ ${member.age}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = AccentBright
                                )
                            }
                        }
                    }
                }
            }
            return
        }

        when (section) {
            "PUAN DURUMU" -> {
                Surface(
                    color = SurfacePrimary,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, StrokePremiumStrong),
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        item {
                            Surface(
                                color = BackgroundSecondary,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, StrokePremium),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("#", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(24.dp))
                                    Text("KULÜP", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextMuted, modifier = Modifier.weight(1f))
                                    Text("O", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                                    Text("G", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                                    Text("B", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                                    Text("M", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                                    Text("AG", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(22.dp), textAlign = TextAlign.Center)
                                    Text("YG", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(22.dp), textAlign = TextAlign.Center)
                                    Text("AV", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                                    Text("P", fontFamily = SpaceGroteskFontFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AccentBright, modifier = Modifier.width(28.dp), textAlign = TextAlign.End)
                                }
                            }
                        }
                        itemsIndexed(career.standings, key = { _, row -> row.clubId }) { index, row ->
                            val isUserClub = row.clubId == career.contract.clubId
                            val clubObj = clubsById[row.clubId]
                            val rankColor = when {
                                index == 0 -> AccentBright
                                index == 1 -> AccentPrimary
                                index >= career.standings.size - 2 -> MatchLossRed
                                else -> TextSecondary
                            }
                            Surface(
                                color = if (isUserClub) SurfaceElevated else Color.Transparent,
                                shape = RoundedCornerShape(12.dp),
                                border = if (isUserClub) BorderStroke(1.dp, AccentBright) else BorderStroke(0.5.dp, Color(0x12FFFFFF)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        inspectedClub = clubObj
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Rank zone bar + rank number
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.width(24.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(16.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(rankColor)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${index + 1}",
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isUserClub) AccentBright else TextPrimary
                                        )
                                    }

                                    // Club Logo + Team Name
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        ClubCrestBadge(
                                            shortName = clubObj?.shortName ?: row.clubName.take(3).uppercase(),
                                            primaryHex = clubObj?.primaryColorHex ?: "#1ED777",
                                            secondaryHex = clubObj?.secondaryColorHex ?: "#FFFFFF",
                                            size = 24.dp
                                        )
                                        Spacer(modifier = Modifier.width(7.dp))
                                        Text(
                                            text = row.clubName,
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = if (isUserClub) FontWeight.Bold else FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = if (isUserClub) AccentBright else TextPrimary,
                                            maxLines = 1
                                        )
                                    }

                                    Text("${row.played}", fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                                    Text("${row.won}", fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                                    Text("${row.drawn}", fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                                    Text("${row.lost}", fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp, color = TextSecondary, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                                    Text("${row.goalsFor}", fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(22.dp), textAlign = TextAlign.Center)
                                    Text("${row.goalsAgainst}", fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp, color = TextMuted, modifier = Modifier.width(22.dp), textAlign = TextAlign.Center)
                                    Text(
                                        text = if (row.goalDifference > 0) "+${row.goalDifference}" else "${row.goalDifference}",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        color = if (row.goalDifference > 0) AccentBright else TextSecondary,
                                        modifier = Modifier.width(26.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "${row.points}",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isUserClub) AccentBright else TextPrimary,
                                        modifier = Modifier.width(28.dp),
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }
                }
            }

            "FİKSTÜR & SONUÇLAR" -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(career.fixtures, key = { it.id }) { fx ->
                        val involvesUser = fx.homeClubId == career.contract.clubId || fx.awayClubId == career.contract.clubId
                        val isPlayableToday = involvesUser && !fx.played && fx.dayIndex == career.currentDayIndex
                        val homeClubObj = clubsById[fx.homeClubId]
                        val awayClubObj = clubsById[fx.awayClubId]
                        Surface(
                            color = if (involvesUser) SurfaceElevated else SurfacePrimary,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isPlayableToday -> AccentBright
                                    involvesUser -> StrokePremiumStrong
                                    else -> StrokePremium
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isPlayableToday) { onPlayUserMatch() }
                                .testTag("fixture_row_${fx.id}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "HAFTA ${fx.week} • ${fx.dateLabel.uppercase()}",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = AccentBright
                                    )
                                    Text(
                                        text = fx.stadiumName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = fx.homeClubName,
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextPrimary,
                                            textAlign = TextAlign.End
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        ClubCrestBadge(
                                            shortName = homeClubObj?.shortName ?: fx.homeClubName.take(3).uppercase(),
                                            primaryHex = homeClubObj?.primaryColorHex ?: "#1ED777",
                                            secondaryHex = homeClubObj?.secondaryColorHex ?: "#FFFFFF",
                                            size = 28.dp
                                        )
                                    }
                                    Surface(
                                        color = BackgroundPrimary,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, StrokePremium),
                                        modifier = Modifier.padding(horizontal = 10.dp)
                                    ) {
                                        Text(
                                            text = if (fx.played) "${fx.homeScore} - ${fx.awayScore}" else fx.kickOffTime,
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 13.sp,
                                            color = if (fx.played) AccentBright else TextSecondary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        ClubCrestBadge(
                                            shortName = awayClubObj?.shortName ?: fx.awayClubName.take(3).uppercase(),
                                            primaryHex = awayClubObj?.primaryColorHex ?: "#2563EB",
                                            secondaryHex = awayClubObj?.secondaryColorHex ?: "#FFFFFF",
                                            size = 28.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = fx.awayClubName,
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                                if (isPlayableToday) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = onPlayUserMatch,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AccentPrimary,
                                            contentColor = Color(0xFF04140C)
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("btn_fixture_play_match")
                                    ) {
                                        Text(
                                            text = "BUGÜN MAÇ GÜNÜ • MAÇA ÇIK",
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "GOL KRALLIĞI" -> {
                val sortedGoals = career.topScorers.sortedWith(
                    compareByDescending<com.example.model.LeagueScorerEntry> { it.goals }
                        .thenByDescending { it.assists }
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(sortedGoals, key = { _, s -> "goal_${s.playerId}" }) { idx, scorer ->
                        val clubObj = clubsById[scorer.clubId]
                        Surface(
                            color = if (scorer.isUserPlayer) SurfaceElevated else SurfacePrimary,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (scorer.isUserPlayer) AccentBright else StrokePremium),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "#${idx + 1}",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (idx == 0) AccentBright else TextMuted,
                                        modifier = Modifier.width(30.dp)
                                    )
                                    ClubCrestBadge(
                                        shortName = clubObj?.shortName ?: scorer.clubName.take(3).uppercase(),
                                        primaryHex = clubObj?.primaryColorHex ?: "#1ED777",
                                        secondaryHex = clubObj?.secondaryColorHex ?: "#FFFFFF",
                                        size = 32.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = scorer.playerName,
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 15.sp,
                                            color = if (scorer.isUserPlayer) AccentBright else TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${scorer.clubName} • ${scorer.matches} Maç • Ort. ${com.example.ui.components.FLFormatters.formatRating(scorer.averageRating)}",
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Surface(
                                    color = BackgroundPrimary,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, StrokePremium)
                                ) {
                                    Text(
                                        text = "${scorer.goals} GOL",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 14.sp,
                                        color = AccentBright,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            "ASİST KRALLIĞI" -> {
                val sortedAssists = career.topScorers.sortedWith(
                    compareByDescending<com.example.model.LeagueScorerEntry> { it.assists }
                        .thenByDescending { it.goals }
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(sortedAssists, key = { _, s -> "ast_${s.playerId}" }) { idx, scorer ->
                        val clubObj = clubsById[scorer.clubId]
                        Surface(
                            color = if (scorer.isUserPlayer) SurfaceElevated else SurfacePrimary,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, if (scorer.isUserPlayer) AccentBright else StrokePremium),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "#${idx + 1}",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (idx == 0) AccentBright else TextMuted,
                                        modifier = Modifier.width(30.dp)
                                    )
                                    ClubCrestBadge(
                                        shortName = clubObj?.shortName ?: scorer.clubName.take(3).uppercase(),
                                        primaryHex = clubObj?.primaryColorHex ?: "#1ED777",
                                        secondaryHex = clubObj?.secondaryColorHex ?: "#FFFFFF",
                                        size = 32.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = scorer.playerName,
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 15.sp,
                                            color = if (scorer.isUserPlayer) AccentBright else TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${scorer.clubName} • ${scorer.matches} Maç • Ort. ${com.example.ui.components.FLFormatters.formatRating(scorer.averageRating)}",
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Surface(
                                    color = BackgroundPrimary,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, StrokePremium)
                                ) {
                                    Text(
                                        text = "${scorer.assists} ASİST",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 14.sp,
                                        color = ChampionshipGold,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            "KULÜPLER" -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(career.clubs, key = { it.id }) { club ->
                        Surface(
                            color = SurfacePrimary,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, StrokePremium),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { inspectedClub = club }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ClubCrestBadge(
                                        shortName = club.shortName,
                                        primaryHex = club.primaryColorHex,
                                        secondaryHex = club.secondaryColorHex,
                                        size = 44.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = club.name,
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 16.sp,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "TD: ${club.managerName} • ${club.stadiumName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                                Surface(
                                    color = BackgroundPrimary,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, StrokePremium)
                                ) {
                                    Text(
                                        text = "GÜÇ ${club.teamStrength}",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = AccentBright,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            "OYUNCULAR" -> {
                val allLeaguePlayers = remember(career.clubs, career.player) {
                    val userEntry = Triple(
                        career.player.fullName,
                        career.currentClub.name,
                        "${career.player.primaryPosition.code} • OVR ${career.player.overallRating} • Yaş ${career.player.age} • ${com.example.ui.components.FLFormatters.formatMarketValue(career.player.marketValueEur)}"
                    )
                    val squadEntries = career.clubs.flatMap { c ->
                        c.keySquadMembers.map { m ->
                            Triple(
                                m.name,
                                c.name,
                                "${m.position.code} • OVR ${m.overall} • Yaş ${m.age} • #${m.kitNumber}"
                            )
                        }
                    }
                    listOf(userEntry) + squadEntries
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = playerSearchQuery,
                        onValueChange = { playerSearchQuery = it },
                        placeholder = { Text("Oyuncu, kulüp veya mevki ara...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val filteredPlayers = allLeaguePlayers.filter { (name, clubName, meta) ->
                        playerSearchQuery.isBlank() ||
                            name.contains(playerSearchQuery, ignoreCase = true) ||
                            clubName.contains(playerSearchQuery, ignoreCase = true) ||
                            meta.contains(playerSearchQuery, ignoreCase = true)
                    }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(filteredPlayers) { idx, (name, clubName, meta) ->
                            val isUser = idx == 0 && name == career.player.fullName
                            Surface(
                                color = if (isUser) SurfaceElevated else SurfacePrimary,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, if (isUser) AccentBright else StrokePremium),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = name,
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 15.sp,
                                            color = if (isUser) AccentBright else TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(clubName, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    Text(
                                        text = meta,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = ChampionshipGold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            "TRANSFERLER" -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = "TAMAMLANAN LİG & KARİYER TRANSFERLERİ",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ChampionshipGold
                        )
                    }
                    items(career.transferHistory, key = { "l_tr_${it.id}" }) { tr ->
                        Surface(
                            color = SurfacePrimary,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, StrokePremium),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "${career.player.fullName}: ${tr.fromClubName} ➔ ${tr.toClubName}",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 15.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${tr.dateLabel} • ${tr.transferType} • Değer: ${com.example.ui.components.FLFormatters.formatMarketValue(tr.marketValueAtTransferEur)}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 12.sp,
                                    color = AccentBright
                                )
                            }
                        }
                    }
                    if (career.transferOffers.isNotEmpty()) {
                        item {
                            Text(
                                text = "GÜNCEL TRANSFER SÖYLENTİLERİ & TEKLİFLER",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AccentBright
                            )
                        }
                        items(career.transferOffers, key = { "l_off_${it.id}" }) { off ->
                            Surface(
                                color = SurfaceElevated,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, StrokePremiumStrong),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "${off.clubName}, ${career.player.fullName} için resmi teklif sundu!",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 15.sp,
                                        color = AccentBright,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Önerilen Maaş: ${com.example.ui.components.FLFormatters.formatWage(off.offeredWeeklyWageEur)} • Rol: ${off.promisedRole.labelTr}",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            "FORM TABLOSU" -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(career.standings, key = { _, r -> "form_${r.clubId}" }) { idx, row ->
                        val clubObj = clubsById[row.clubId]
                        Surface(
                            color = SurfacePrimary,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, StrokePremium),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "#${idx + 1}",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TextMuted,
                                        modifier = Modifier.width(28.dp)
                                    )
                                    ClubCrestBadge(
                                        shortName = clubObj?.shortName ?: row.clubName.take(3).uppercase(),
                                        primaryHex = clubObj?.primaryColorHex ?: "#1ED777",
                                        secondaryHex = clubObj?.secondaryColorHex ?: "#FFFFFF",
                                        size = 28.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = row.clubName,
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 14.sp,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val formList = row.recentForm.ifEmpty { listOf("-", "-", "-", "-", "-") }
                                    formList.forEach { res ->
                                        val bg = when (res) {
                                            "G" -> MatchWinGreen
                                            "B" -> MatchDrawAmber
                                            "M" -> MatchLossRed
                                            else -> BackgroundPrimary
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(25.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(bg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = res,
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontSize = 11.sp,
                                                color = if (res == "-") TextMuted else Color(0xFF04140C),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                // LİG HABERLERİ
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(career.newsEvents, key = { "lnews_${it.id}" }) { news ->
                        Surface(
                            color = SurfacePrimary,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, StrokePremium),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = news.category.uppercase(),
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 11.sp,
                                        color = AccentBright,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = news.dateLabel,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = news.headline,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 16.sp,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(news.body, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeagueSummaryMiniCard(
    tag: String,
    title: String,
    metric: String,
    subMetric: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isActive) SurfaceElevated else BackgroundSecondary,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (isActive) AccentBright else StrokePremium),
        modifier = Modifier
            .width(168.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = tag,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                letterSpacing = 0.6.sp,
                color = if (isActive) AccentBright else TextMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextPrimary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = metric,
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = AccentBright,
                maxLines = 1
            )
            Text(
                text = subMetric,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}


@Composable
fun SmartphoneScreen(
    career: CareerSaveState,
    onSendNpcMessage: (String, String) -> Unit,
    onMarkNpcRead: (String) -> Unit,
    onPublishSocialPost: (String) -> Unit
) {
    var activeApp by remember { mutableStateOf("MESAJLAR") } // MESAJLAR, HABERLER, SOSYAL, BANKA, KİŞİLER
    var selectedNpcId by remember { mutableStateOf(career.npcs.firstOrNull()?.id ?: "NPC_MANAGER") }
    var customChatInput by remember { mutableStateOf("") }
    var customSocialInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPitchSurface)
            .padding(14.dp)
            .testTag("smartphone_screen")
    ) {
        // Smartphone Frame Header + App Bar
        Surface(
            color = DeepStadiumGreen,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.5.dp, CrispWhiteBorderStrong),
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Status Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("FL-OS 5G • 20:45", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                    Text("${career.player.followersCount} Takipçi • ${career.balanceEur} €", style = MaterialTheme.typography.labelSmall, color = BrightGold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Phone App Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "MESAJLAR" to Icons.AutoMirrored.Filled.Chat,
                        "HABERLER" to Icons.Default.Newspaper,
                        "SOSYAL" to Icons.Default.Tag,
                        "BANKA" to Icons.Default.AccountBalance,
                        "KİŞİLER" to Icons.Default.Contacts
                    ).forEach { (appId, icon) ->
                        val selected = activeApp == appId
                        FilterChip(
                            selected = selected,
                            onClick = { activeApp = appId },
                            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text(appId) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VibrantFootballGreen,
                                selectedLabelColor = DarkPitchSurface
                            ),
                            modifier = Modifier.testTag("phone_tab_$appId")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                when (activeApp) {
                    "MESAJLAR" -> {
                        val activeNpc = career.npcs.find { it.id == selectedNpcId } ?: career.npcs.first()
                        // NPC Horizontal Selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            career.npcs.forEach { npc ->
                                val isSel = npc.id == activeNpc.id
                                Surface(
                                    color = if (isSel) DarkTurfGreen else DarkPitchSurface,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (isSel) VibrantFootballGreen else CrispWhiteBorder),
                                    modifier = Modifier
                                        .clickable {
                                            selectedNpcId = npc.id
                                            onMarkNpcRead(npc.id)
                                        }
                                        .testTag("npc_chat_chip_${npc.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${npc.roleTitle}: ${npc.name.take(14)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PrimaryWhite
                                        )
                                        if (npc.unreadCount > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(MatchLossRed)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Messages Feed
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(activeNpc.messages, key = { it.id }) { msg ->
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = if (msg.isFromUser) Alignment.End else Alignment.Start
                                ) {
                                    Surface(
                                        color = if (msg.isFromUser) VibrantFootballGreen else DarkPitchSurface,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, CrispWhiteBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "${msg.senderName} • ${msg.timeLabel}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (msg.isFromUser) DarkPitchSurface else ChampionshipGold
                                            )
                                            Text(
                                                text = msg.text,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (msg.isFromUser) DarkPitchSurface else PrimaryWhite
                                            )
                                            msg.impactSummary?.let { imp ->
                                                Text(
                                                    text = imp,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = BrightNeonGreen
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Preset Replies
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            activeNpc.quickReplyOptions.forEachIndexed { idx, opt ->
                                OutlinedButton(
                                    onClick = { onSendNpcMessage(activeNpc.id, opt) },
                                    border = BorderStroke(1.dp, VibrantFootballGreen),
                                    modifier = Modifier.testTag("btn_quick_reply_$idx")
                                ) {
                                    Text(opt, style = MaterialTheme.typography.labelSmall, color = BrightNeonGreen)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Free-text Custom Dialogue Input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customChatInput,
                                onValueChange = { customChatInput = it },
                                placeholder = { Text("${activeNpc.name} kişisine kendi cümleni yaz...") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_custom_npc_message")
                            )
                            Button(
                                onClick = {
                                    if (customChatInput.isNotBlank()) {
                                        onSendNpcMessage(activeNpc.id, customChatInput)
                                        customChatInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VibrantFootballGreen,
                                    contentColor = DarkPitchSurface
                                ),
                                modifier = Modifier.testTag("btn_send_custom_npc_message")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Gönder")
                            }
                        }
                    }

                    "HABERLER" -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(career.newsEvents, key = { it.id }) { news ->
                                Surface(
                                    color = DarkPitchSurface,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, GoldBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(news.category, style = MaterialTheme.typography.labelSmall, color = ChampionshipGold)
                                            Text(news.dateLabel, style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(news.headline, style = MaterialTheme.typography.titleMedium, color = PrimaryWhite)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(news.body, style = MaterialTheme.typography.bodySmall, color = SecondaryMintGray)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Taraftar Tepkisi: ${news.fanReaction}", style = MaterialTheme.typography.labelSmall, color = BrightNeonGreen)
                                    }
                                }
                            }
                        }
                    }

                    "SOSYAL" -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customSocialInput,
                                    onValueChange = { customSocialInput = it },
                                    placeholder = { Text("Taraftarlara paylaşım yap...") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = {
                                        if (customSocialInput.isNotBlank()) {
                                            onPublishSocialPost(customSocialInput)
                                            customSocialInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VibrantFootballGreen,
                                        contentColor = DarkPitchSurface
                                    )
                                ) {
                                    Text("Paylaş", fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(career.socialPosts, key = { it.id }) { post ->
                                    Surface(
                                        color = DarkPitchSurface,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, CrispWhiteBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("${post.authorName} (${post.authorHandle})", style = MaterialTheme.typography.titleSmall, color = BrightGold)
                                            Text(post.content, style = MaterialTheme.typography.bodySmall, color = PrimaryWhite)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "❤️ ${post.likesCount} Beğeni • 💬 ${post.commentsCount} Yorum • ${post.sentiment}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = BrightNeonGreen
                                            )
                                            post.topFanComments.forEach { cmt ->
                                                Text("• \"$cmt\"", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "BANKA" -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Surface(
                                color = DarkTurfGreen,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, ChampionshipGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("MEVCUT BANKA BAKİYESİ", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                                    Text(
                                        text = "${career.balanceEur} €",
                                        fontFamily = TekoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 38.sp,
                                        color = BrightGold
                                    )
                                    Text(
                                        text = "Haftalık Maaş: ${career.contract.weeklyWageEur} € • Maç Başı: ${career.contract.matchAppearanceBonusEur} € • Gol Primi: ${career.contract.goalBonusEur} €",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PrimaryWhite
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(career.transactions, key = { it.id }) { tx ->
                                    Surface(
                                        color = DarkPitchSurface,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, CrispWhiteBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(tx.description, style = MaterialTheme.typography.bodySmall, color = PrimaryWhite)
                                                Text("${tx.dateLabel} • ${tx.category}", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                                            }
                                            Text(
                                                text = if (tx.amountEur >= 0) "+${tx.amountEur} €" else "${tx.amountEur} €",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (tx.amountEur >= 0) BrightNeonGreen else MatchLossRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        // KİŞİLER (NPC İlişki Ağı)
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(career.npcs, key = { it.id }) { npc ->
                                Surface(
                                    color = DarkPitchSurface,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, CrispWhiteBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("${npc.name} (${npc.roleTitle})", style = MaterialTheme.typography.titleSmall, color = BrightGold)
                                        Text("Kişilik: ${npc.personality} • Ruh Hâli: ${npc.currentMood}", style = MaterialTheme.typography.bodySmall, color = SecondaryMintGray)
                                        Text("Yakınlık: %${npc.affinity} • Güven: %${npc.trust}", style = MaterialTheme.typography.labelMedium, color = BrightNeonGreen)
                                        Text("Hedef: ${npc.shortTermGoal}", style = MaterialTheme.typography.labelSmall, color = PrimaryWhite)
                                        if (npc.importantMemories.isNotEmpty()) {
                                            Text("Hatırladığı Olay: ${npc.importantMemories.first()}", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
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
}

@Composable
fun SocialLifeAndStoreScreen(
    career: CareerSaveState,
    onExecuteVenueAction: (LifestyleVenueAction) -> Unit,
    onBuyItem: (LifestylePurchaseItem) -> Unit,
    onBack: () -> Unit
) {
    var tab by remember { mutableStateOf("YAŞAM ALANLARI") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPitchSurface)
            .padding(16.dp)
            .testTag("social_life_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_from_social")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = PrimaryWhite)
                }
                Column {
                    Text(
                        text = "SOSYAL HAYAT VE YAŞAM TARZI",
                        fontFamily = TekoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = PrimaryWhite
                    )
                    Text(
                        text = "Bakiye: ${career.balanceEur} € • Enerji: %${career.player.energy} • Moral: %${career.player.morale}",
                        style = MaterialTheme.typography.bodySmall,
                        color = VibrantFootballGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("YAŞAM ALANLARI", "VARLIKLAR & YATIRIM").forEach { t ->
                FilterChip(
                    selected = tab == t,
                    onClick = { tab = t },
                    label = { Text(t) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VibrantFootballGreen,
                        selectedLabelColor = DarkPitchSurface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (tab == "YAŞAM ALANLARI") {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(WorldDataCatalog.lifestyleVenueActions, key = { it.id }) { action ->
                    Surface(
                        color = DeepStadiumGreen,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CrispWhiteBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${action.venueName.uppercase()} • ${action.title}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = PrimaryWhite
                                )
                                Text(
                                    text = if (action.costEur == 0L) "ÜCRETSİZ" else "${action.costEur} €",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = BrightGold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(action.subtitle, style = MaterialTheme.typography.bodySmall, color = SecondaryMintGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Enerji: ${if (action.energyDelta >= 0) "+${action.energyDelta}" else "${action.energyDelta}"} • Moral: +${action.moraleDelta} • Hoca Güveni: ${if (action.managerTrustDelta >= 0) "+${action.managerTrustDelta}" else "${action.managerTrustDelta}"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrightNeonGreen
                                )
                                Button(
                                    onClick = { onExecuteVenueAction(action) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VibrantFootballGreen,
                                        contentColor = DarkPitchSurface
                                    ),
                                    modifier = Modifier.testTag("btn_venue_${action.id}")
                                ) {
                                    Text("ZİYARET ET", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(WorldDataCatalog.lifestylePurchaseItems, key = { it.id }) { item ->
                    val owned = item.id in career.ownedLifestyleItemIds
                    Surface(
                        color = DeepStadiumGreen,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (owned) BrightGold else CrispWhiteBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${item.category} • ${item.name}", style = MaterialTheme.typography.titleSmall, color = PrimaryWhite)
                                Text("${item.priceEur} €", style = MaterialTheme.typography.labelMedium, color = BrightGold)
                            }
                            Text(item.description, style = MaterialTheme.typography.bodySmall, color = SecondaryMintGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onBuyItem(item) },
                                enabled = !owned,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ChampionshipGold,
                                    contentColor = DarkPitchSurface
                                ),
                                modifier = Modifier.testTag("btn_buy_${item.id}")
                            ) {
                                Text(if (owned) "SAHİPSİN ✓" else "SATIN AL (${item.priceEur} €)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransferAndContractScreen(
    career: CareerSaveState,
    onAskAgentForOffers: () -> Unit,
    onNegotiateOffer: (String) -> Unit,
    onAcceptOffer: (TransferOffer) -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPitchSurface)
            .padding(16.dp)
            .testTag("transfer_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DeepStadiumGreen,
                border = BorderStroke(1.5.dp, ChampionshipGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_transfer_lounge),
                        contentDescription = "Transfer Merkezi",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(165.dp)
                            .alpha(0.45f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(165.dp)
                            .background(
                                Brush.verticalGradient(listOf(Color.Transparent, DeepStadiumGreen))
                            )
                    )
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_from_transfers")) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = PrimaryWhite)
                            }
                            Column {
                                Text(
                                    text = "TRANSFER, MENAJER VE SÖZLEŞME MASASI",
                                    fontFamily = TekoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 30.sp,
                                    color = PrimaryWhite
                                )
                                Text(
                                    text = "Piyasa Değeri: ${career.player.marketValueEur} € • Menajer: ${career.contract.agentName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = BrightGold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Mevcut Kulüp: ${career.contract.clubName} (${career.contract.role.labelTr})\nMaaş: ${career.contract.weeklyWageEur} €/Hafta • Maç Başı: ${career.contract.matchAppearanceBonusEur} € • Gol Primi: ${career.contract.goalBonusEur} €",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryWhite
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = onAskAgentForOffers,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibrantFootballGreen,
                    contentColor = DarkPitchSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_agent_search_offers")
            ) {
                Icon(Icons.Default.Handshake, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("MENAJERİNDEN KULÜPLERLE GÖRÜŞMESİNİ İSTE", fontWeight = FontWeight.Bold)
            }
        }

        if (career.transferOffers.isEmpty()) {
            item {
                Surface(
                    color = DeepStadiumGreen,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CrispWhiteBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Şu anda masada resmi bir transfer teklifi bulunmuyor. Maçlarda göstereceğin yüksek performans veya menajerinin girişimleriyle yeni teklifler alabilirsin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryMintGray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(career.transferOffers, key = { it.id }) { offer ->
                Surface(
                    color = DeepStadiumGreen,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, ChampionshipGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ClubCrestBadge(
                                    shortName = offer.clubName.take(3).uppercase(),
                                    primaryHex = offer.primaryColorHex,
                                    secondaryHex = "#F7FAF8",
                                    size = 44.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(offer.clubName, style = MaterialTheme.typography.titleMedium, color = PrimaryWhite)
                                    Text("${offer.leagueName} • Güç: ${offer.clubStrength}", style = MaterialTheme.typography.bodySmall, color = VibrantFootballGreen)
                                }
                            }
                            Surface(color = ChampionshipGold, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = offer.transferType,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DarkPitchSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = "Teklif Edilen Maaş: ${offer.offeredWeeklyWageEur} €/Hafta • Gol Primi: ${offer.offeredGoalBonusEur} €\nVaat Edilen Rol: ${offer.promisedRole.labelTr} • Süre: ${offer.contractYears} Yıl",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryWhite
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onNegotiateOffer(offer.id) },
                                border = BorderStroke(1.dp, ChampionshipGold),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ChampionshipGold),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_negotiate_${offer.id}")
                            ) {
                                Text("MAAŞ PAZARLIĞI YAP")
                            }
                            Button(
                                onClick = { onAcceptOffer(offer) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VibrantFootballGreen,
                                    contentColor = DarkPitchSurface
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_accept_offer_${offer.id}")
                            ) {
                                Text("SÖZLEŞMEYİ İMZALA", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
