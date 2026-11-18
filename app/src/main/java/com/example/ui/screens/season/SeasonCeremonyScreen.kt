package com.example.ui.screens.season

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.progression.AwardRecipient
import com.example.engine.progression.BallonDorCandidate
import com.example.engine.progression.SeasonEndSummary
import com.example.ui.components.ClubCrestBadge
import com.example.ui.components.FLPrimaryButton
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.BrightGold
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Negative
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StrokePremium
import com.example.ui.theme.StrokePremiumStrong
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class CeremonyTab(val labelTr: String, val icon: String) {
    CHAMPIONSHIP("Şampiyonluk & Kupa", "🏆"),
    GOLDEN_BOOT("Altın Ayakkabı", "👟"),
    YOUNG_PLAYER("Genç Yetenek", "⭐"),
    BALLON_DOR("Ballon d'Or Galası", "🌟"),
    HIERARCHY("Düşme & Çıkma", "🔄")
}

@Composable
fun SeasonCeremonyScreen(
    summary: SeasonEndSummary,
    onTransitionToNextSeason: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(CeremonyTab.CHAMPIONSHIP) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .testTag("season_ceremony_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 84.dp)
        ) {
            // 1. Ceremony Broadcast Header
            CeremonyTopBar(
                summary = summary,
                onClose = onClose
            )

            // 2. Interactive Navigation Tabs
            CeremonyTabBar(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it }
            )

            // 3. Tab Body Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp)
            ) {
                when (selectedTab) {
                    CeremonyTab.CHAMPIONSHIP -> {
                        item { ChampionshipSection(summary) }
                    }
                    CeremonyTab.GOLDEN_BOOT -> {
                        item { GoldenBootSection(summary) }
                    }
                    CeremonyTab.YOUNG_PLAYER -> {
                        item { YoungPlayerSection(summary) }
                    }
                    CeremonyTab.BALLON_DOR -> {
                        item { BallonDorSection(summary) }
                    }
                    CeremonyTab.HIERARCHY -> {
                        item { PromotionRelegationSection(summary) }
                    }
                }
            }
        }

        // 4. Fixed Bottom Action Bar: Transition to Next Season (+1 Age)
        Surface(
            color = SurfacePrimary.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, StrokePremiumStrong),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FLPrimaryButton(
                    text = "YENİ SEZONU BAŞLAT (+1 YAŞ) ⚡",
                    onClick = onTransitionToNextSeason,
                    icon = Icons.Default.ArrowForward,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "btn_start_new_season"
                )
                Text(
                    text = "Yaşın 1 artacak, sezon istatistiklerin arşive kaydedilecek ve yeni lig fikstürü çekilecektir.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CeremonyTopBar(
    summary: SeasonEndSummary,
    onClose: () -> Unit
) {
    Surface(
        color = SurfacePrimary,
        border = BorderStroke(1.dp, StrokePremium),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🏆 SEZON SONU GALA TÖRENİ",
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        letterSpacing = 0.8.sp,
                        color = BrightGold
                    )
                    Surface(
                        color = Color(0x33FFD700),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, Color(0x66FFD700))
                    ) {
                        Text(
                            text = "${summary.seasonYear}/${summary.seasonYear + 1}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = BrightGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "${summary.leagueName} • Büyük Kupa & Ödül Dağıtım Galası",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(36.dp)
                    .background(BackgroundSecondary, CircleShape)
                    .border(1.dp, StrokePremium, CircleShape)
                    .testTag("btn_close_ceremony")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun CeremonyTabBar(
    selectedTab: CeremonyTab,
    onSelectTab: (CeremonyTab) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CeremonyTab.entries.forEach { tab ->
            val active = selectedTab == tab
            Surface(
                color = if (active) AccentPrimary else SurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (active) AccentBright else StrokePremium),
                modifier = Modifier
                    .clickable { onSelectTab(tab) }
                    .testTag("ceremony_tab_${tab.name}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = tab.icon, fontSize = 14.sp)
                    Text(
                        text = tab.labelTr,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (active) BackgroundPrimary else TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChampionshipSection(summary: SeasonEndSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Hero Championship Card
        Surface(
            color = SurfacePrimary,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, if (summary.isUserClubChampion) Color(0xFFFFD700) else StrokePremiumStrong),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                if (summary.isUserClubChampion) Color(0x33FFD700) else Color(0x221ED777),
                                SurfacePrimary,
                                BackgroundSecondary
                            )
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (summary.isUserClubChampion) "🏆 ŞAMPİYON KULÜP: BİZİZ! 🏆" else "🏆 LİG ŞAMPİYONU",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (summary.isUserClubChampion) BrightGold else AccentBright,
                    letterSpacing = 1.sp
                )

                ClubCrestBadge(
                    shortName = summary.championClub.shortName,
                    primaryHex = summary.championClub.primaryColorHex,
                    secondaryHex = summary.championClub.secondaryColorHex,
                    size = 72.dp
                )

                Text(
                    text = summary.championClub.name.uppercase(),
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    letterSpacing = 1.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "${summary.championClub.leagueName} • ${summary.seasonYear}/${summary.seasonYear + 1} Şampiyonu",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                if (summary.isUserClubChampion) {
                    Surface(
                        color = Color(0x40FFD700),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0x80FFD700)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "KUPA SENİN ELLERİNDE YÜKSELDİ!",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = BrightGold
                            )
                            Text(
                                text = summary.managerCelebrationQuote,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Season Summary Stats Card
        Surface(
            color = SurfacePrimary,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, StrokePremium),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SEZON PERFORMANS ÖZETİN",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AccentBright
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatPill("Lig Sırası", "#${summary.userLeagueRank} / ${summary.totalTeamsInLeague}")
                    StatPill("Maç", "${summary.userSeasonStats.appearances}")
                    StatPill("Gol", "${summary.userSeasonStats.goals}")
                    StatPill("Asist", "${summary.userSeasonStats.assists}")
                    StatPill("Ort. Reyting", "${summary.userSeasonStats.averageRating}")
                }

                Surface(
                    color = BackgroundSecondary,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, StrokePremium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "📰 BASIN MANŞETİ",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = summary.pressCelebrationHeadline,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GoldenBootSection(summary: SeasonEndSummary) {
    val winner = summary.goldenBootWinner
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        AwardHeroCard(
            badgeTitle = "GOL KRALLIĞI",
            trophyEmoji = "👟",
            awardName = winner.awardTitle,
            recipient = winner,
            isUserWinner = summary.userWonGoldenBoot,
            userWinningTitle = "ALTIN AYAKKABI SENİN! ⚽🔥",
            congratsBody = "Sezon boyunca attığın kritik gollerle ligin zirvesine oturdun ve resmi Altın Ayakkabı ödülünü kazandın!"
        )

        Surface(
            color = SurfacePrimary,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, StrokePremium),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "GOL KRALLIĞI TABLOSU",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AccentBright
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("1. ${winner.playerName} (${winner.clubName})", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text(winner.statSummary, color = BrightGold, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
private fun YoungPlayerSection(summary: SeasonEndSummary) {
    val winner = summary.youngPlayerOfYear
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        AwardHeroCard(
            badgeTitle = "GELECEĞİN YILDIZI",
            trophyEmoji = "⭐",
            awardName = winner.awardTitle,
            recipient = winner,
            isUserWinner = summary.userWonYoungPlayer,
            userWinningTitle = "YILIN GENÇ FUTBOLCUSU SEÇİLDİN! 🌟",
            congratsBody = "Gelişimin, sahadaki liderliğin ve olgun oyununla Türkiye'nin ve ligin en parlak genç yeteneği seçildin!"
        )
    }
}

@Composable
private fun BallonDorSection(summary: SeasonEndSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Hero Ballon d'Or Card
        Surface(
            color = SurfacePrimary,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(2.dp, Color(0xFFFFD700)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x44FFD700), SurfacePrimary, BackgroundSecondary)
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "🌟 BALLON D'OR GALASI 🌟",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = BrightGold,
                    letterSpacing = 1.2.sp
                )
                Text(text = "👑", fontSize = 48.sp)
                Text(
                    text = summary.ballonDorWinner.playerName.uppercase(),
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${summary.ballonDorWinner.clubName} • Dünyanın En İyi Futbolcusu",
                    fontFamily = OutfitFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                if (summary.userWonBallonDor) {
                    Surface(
                        color = Color(0x33FFD700),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0x66FFD700))
                    ) {
                        Text(
                            text = "TARİH YAZDIN! ALTIN TOP ÖDÜLÜ SENİN ELLERİNDE! 🏆✨",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp,
                            color = BrightGold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Podium Finalists List
        Surface(
            color = SurfacePrimary,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, StrokePremium),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "BALLON D'OR FİNAL OYLAMA SIRALAMASI",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = BrightGold
                )

                summary.ballonDorTop3.forEach { candidate ->
                    BallonDorCandidateRow(candidate)
                }
            }
        }
    }
}

@Composable
private fun BallonDorCandidateRow(candidate: BallonDorCandidate) {
    val rankBadgeColor = when (candidate.rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> TextSecondary
    }

    Surface(
        color = if (candidate.isUser) SurfaceElevated else BackgroundSecondary,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (candidate.isUser) AccentPrimary else StrokePremium),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = rankBadgeColor.copy(alpha = 0.2f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, rankBadgeColor),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${candidate.rank}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = rankBadgeColor
                        )
                    }
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = candidate.playerName,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (candidate.isUser) AccentBright else TextPrimary
                        )
                        if (candidate.isUser) {
                            Surface(
                                color = AccentPrimary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "SEN",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = BackgroundPrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${candidate.clubName} • ${candidate.goalsAndAssists}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = "%${candidate.votesPercent}",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = if (candidate.rank == 1) BrightGold else TextSecondary
            )
        }
    }
}

@Composable
private fun PromotionRelegationSection(summary: SeasonEndSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Promoted Clubs Card
        Surface(
            color = SurfacePrimary,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, StrokePremiumStrong),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("⬆️", fontSize = 16.sp)
                    Text(
                        text = "ÜST LİGE YÜKSELEN KULÜPLER",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AccentBright
                    )
                }

                if (summary.promotedClubs.isEmpty()) {
                    Text(
                        text = "Süper Lig en üst lig seviyesidir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                } else {
                    summary.promotedClubs.forEach { club ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BackgroundSecondary, RoundedCornerShape(10.dp))
                                .border(1.dp, StrokePremium, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ClubCrestBadge(shortName = club.shortName, primaryHex = club.primaryColorHex, secondaryHex = club.secondaryColorHex, size = 32.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(club.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("${club.city} • Yükseldi", style = MaterialTheme.typography.labelSmall, color = AccentPrimary)
                            }
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = AccentPrimary)
                        }
                    }
                }
            }
        }

        // Relegated Clubs Card
        Surface(
            color = SurfacePrimary,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, StrokePremiumStrong),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("⬇️", fontSize = 16.sp)
                    Text(
                        text = "ALT LİGE DÜŞEN KULÜPLER",
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Negative
                    )
                }

                if (summary.relegatedClubs.isEmpty()) {
                    Text(
                        text = "Düşme hattında takım bulunmuyor.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                } else {
                    summary.relegatedClubs.forEach { club ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BackgroundSecondary, RoundedCornerShape(10.dp))
                                .border(1.dp, StrokePremium, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ClubCrestBadge(shortName = club.shortName, primaryHex = club.primaryColorHex, secondaryHex = club.secondaryColorHex, size = 32.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(club.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("${club.city} • Küme Düştü", style = MaterialTheme.typography.labelSmall, color = Negative)
                            }
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Negative)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AwardHeroCard(
    badgeTitle: String,
    trophyEmoji: String,
    awardName: String,
    recipient: AwardRecipient,
    isUserWinner: Boolean,
    userWinningTitle: String,
    congratsBody: String
) {
    Surface(
        color = SurfacePrimary,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.5.dp, if (isUserWinner) Color(0xFFFFD700) else StrokePremiumStrong),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (isUserWinner) Color(0x33FFD700) else Color(0x221ED777),
                            SurfacePrimary,
                            BackgroundSecondary
                        )
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "$trophyEmoji $badgeTitle $trophyEmoji",
                fontFamily = SpaceGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isUserWinner) BrightGold else AccentBright,
                letterSpacing = 1.sp
            )

            Text(
                text = recipient.playerName.uppercase(),
                fontFamily = BarlowCondensedFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "${recipient.clubName} • ${recipient.statSummary}",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 13.sp,
                color = TextSecondary
            )

            if (isUserWinner) {
                Surface(
                    color = Color(0x33FFD700),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0x66FFD700))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = userWinningTitle,
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = BrightGold
                        )
                        Text(
                            text = congratsBody,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(
            text = value,
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = TextPrimary
        )
    }
}
