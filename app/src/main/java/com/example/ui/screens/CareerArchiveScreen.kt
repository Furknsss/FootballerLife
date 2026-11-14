package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.CareerEngine
import com.example.model.CareerSaveState
import com.example.model.MarketValuePoint
import com.example.model.Position
import com.example.model.SeasonStatsRecord
import com.example.model.TransferOffer
import com.example.ui.components.ClubBadge
import com.example.ui.components.FLChartCard
import com.example.ui.components.FLFormatters
import com.example.ui.components.FLPrimaryButton
import com.example.ui.components.FLRatingBadge
import com.example.ui.components.FLSectionHeader
import com.example.ui.components.FLSegmentedTabs
import com.example.ui.components.FLStatBar
import com.example.ui.components.FootballerAvatarCanvas
import com.example.ui.components.parseSafeHexColor
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BarlowCondensedFontFamily
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

@Composable
fun CareerArchiveScreen(
    state: CareerSaveState,
    onRequestAgentTransferSearch: () -> Unit,
    onNegotiateOffer: (String) -> Unit,
    onAcceptOffer: (TransferOffer) -> Unit,
    onExtendContract: () -> Unit
) {
    val tmNavyDark = Color(0xFF001935)
    val tmNavyHeader = Color(0xFF1A3151)
    val tmCyan = Color(0xFF00A6FF)
    val tmCyanBright = Color(0xFF5CE1E6)

    var selectedSection by rememberSaveable { mutableStateOf(0) }
    val sections = listOf(
        "Profil & Veri",
        "Transfer Geçmişi",
        "Performans Verileri",
        "Sözleşme & Teklif",
        "Başarı & Sakatlık"
    )

    val p = state.player
    val club = state.currentClub
    val peakMarketValue = maxOf(
        p.marketValueEur,
        state.marketValueHistory.maxOfOrNull { it.valueEur } ?: p.marketValueEur
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(tmNavyDark, BackgroundPrimary)
                )
            )
            .testTag("career_archive_screen"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ====================================================================
        // 7.1 1:1 TRANSFERMARKT PLAYER PROFILE HEADER CARD
        // ====================================================================
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = tmNavyDark),
                border = BorderStroke(1.dp, tmCyan.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Transfermarkt Top Brand Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(tmNavyHeader)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_transfermarkt),
                                contentDescription = "Transfermarkt",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(5.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TRANSFERMARKT • OYUNCU PROFİLİ",
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 0.8.sp,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "ID: #${104820 + p.kitNumber}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 10.sp,
                            color = tmCyanBright
                        )
                    }

                    // Main Player Identity + Club Info + Cyan Market Value Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Player Name & Shirt Number Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#${p.kitNumber}",
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = tmCyanBright
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = p.fullName,
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = null,
                                    tint = tmCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Surface(
                                color = tmNavyHeader,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, tmCyan.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "OVR ${p.overallRating} / POT ${p.potential}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = tmCyanBright,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Portrait frame (Transfermarkt style)
                            Box(
                                modifier = Modifier
                                    .size(86.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF092547))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                FootballerAvatarCanvas(
                                    appearance = p.appearance,
                                    clubPrimaryHex = club.primaryColorHex,
                                    clubSecondaryHex = club.secondaryColorHex,
                                    kitNumber = p.kitNumber,
                                    modifier = Modifier.size(78.dp)
                                )
                            }

                            // Current Club Dossier Box
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    ClubBadge(
                                        shortName = club.shortName,
                                        primaryHex = club.primaryColorHex,
                                        secondaryHex = club.secondaryColorHex,
                                        size = 26.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = club.name,
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "🏆 ${club.leagueName}",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 11.sp,
                                            color = tmCyanBright
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Kadro Rolü: ${state.contract.role.labelTr}",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFFB8C7D9)
                                )
                                Text(
                                    text = "Sözleşme sonu: 30.06.${state.contract.endSeasonYear}",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFFB8C7D9)
                                )
                            }

                            // Authentic Transfermarkt Cyan Market Value Box
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = tmCyan,
                                modifier = Modifier.width(124.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "GÜNCEL PİYASA DEĞERİ",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                        color = tmNavyDark
                                    )
                                    Text(
                                        text = FLFormatters.formatMarketValue(p.marketValueEur),
                                        fontFamily = BarlowCondensedFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        color = Color.White
                                    )
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.35f), thickness = 0.6.dp)
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "En yüksek PD: ${FLFormatters.formatMarketValue(peakMarketValue)}",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = tmNavyDark,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bottom Quick Summary Strip (Yaş, Boy, Uyruk, Mevki, Maç/Gol)
                        Surface(
                            color = tmNavyHeader.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DossierBioCell(label = "DOĞUM / YAŞ", value = "${p.birthDate} (${p.age})")
                                DossierBioCell(label = "BOY / AYAK", value = "${p.heightCm}cm / ${p.preferredFoot.labelTr}")
                                DossierBioCell(label = "UYRUK", value = p.nationality)
                                DossierBioCell(label = "BU SEZON", value = "${p.currentSeasonStats.appearances} Maç • ${p.currentSeasonStats.goals} Gol")
                            }
                        }
                    }
                }
            }
        }

        // Transfermarkt Sub-navigation Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sections.forEachIndexed { idx, title ->
                    val selected = selectedSection == idx
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) tmCyan else tmNavyHeader,
                        border = BorderStroke(1.dp, if (selected) tmCyanBright else Color.White.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .clickable { selectedSection = idx }
                            .testTag("career_subtab_$idx")
                    ) {
                        Text(
                            text = title,
                            fontFamily = OutfitFontFamily,
                            fontSize = 12.sp,
                            color = if (selected) tmNavyDark else Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        )
                    }
                }
            }
        }

        when (selectedSection) {
            0 -> {
                // ============================================================
                // 7.1B TRANSFERMARKT "OYUNCU VERİLERİ" & "MEVKİ DETAYI" (2D SAHA)
                // ============================================================
                item {
                    TransfermarktPlayerDataAndPitchCard(
                        state = state
                    )
                }

                // 7.2 MARKET VALUE INTERACTIVE CHART (ZORUNLU)
                item {
                    FLMarketValueChartSection(
                        points = state.marketValueHistory,
                        currentValueEur = p.marketValueEur
                    )
                }

                // 7.5 RATING & ATTRIBUTE PROGRESSION CHART
                item {
                    FLChartCard(
                        title = "ÖZELLİK & REYTİNG GELİŞİM MERKEZİ",
                        subtitle = "Mevcut Sezon Ortalaması: ${FLFormatters.formatRating(p.currentSeasonStats.averageRating)} • Potansiyel: ${p.potential}",
                        badgeText = "OVR ${p.overallRating}"
                    ) {
                        FLStatBar("Hız & Patlayıcılık (PAC)", p.attributes.cardPace, modifier = Modifier.padding(vertical = 3.dp))
                        FLStatBar("Bitiricilik & Şut (SHO)", p.attributes.cardShooting, modifier = Modifier.padding(vertical = 3.dp))
                        FLStatBar("Pas & Oyun Görüşü (PAS)", p.attributes.cardPassing, modifier = Modifier.padding(vertical = 3.dp))
                        FLStatBar("Top Sürme & İlk Kontrol (DRI)", p.attributes.cardDribbling, modifier = Modifier.padding(vertical = 3.dp))
                        FLStatBar("Savunma & Taktik (DEF)", p.attributes.cardDefending, modifier = Modifier.padding(vertical = 3.dp))
                        FLStatBar("Fiziksel Güç & Dayanıklılık (PHY)", p.attributes.cardPhysical, modifier = Modifier.padding(vertical = 3.dp))

                        if (state.attributeHistory.size > 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "SON GELİŞİM KAYITLARI",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentBright,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            state.attributeHistory.takeLast(4).reversed().forEach { pt ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(pt.dateLabel, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(
                                        "OVR ${pt.overall} • Hız ${pt.pace} • Şut ${pt.shooting} • Pas ${pt.passing}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    FLRatingBadge(rating = pt.averageMatchRating)
                                }
                            }
                        }
                    }
                }
            }


            1 -> {
                // 7.3 TRANSFER HISTORY TIMELINE
                item {
                    FLSectionHeader(
                        title = "TRANSFER GEÇMİŞİ ZAMAN ÇİZELGESİ",
                        subtitle = "${state.transferHistory.size} Kayıt"
                    )
                }

                items(state.transferHistory, key = { it.id }) { tr ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                        border = BorderStroke(1.dp, StrokePremium),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transfer_history_row_${tr.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SurfaceElevated
                                ) {
                                    Text(
                                        text = "${tr.seasonLabel} • ${tr.dateLabel}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = GoldPrimary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = tr.transferType,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AccentBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("ÖNCEKİ KULÜP", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text(tr.fromClubName, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                                Icon(
                                    imageVector = Icons.Filled.SwapHoriz,
                                    contentDescription = null,
                                    tint = AccentBright,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text("YENİ KULÜP", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text(tr.toClubName, style = MaterialTheme.typography.titleSmall, color = AccentBright, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = StrokePremium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Bonservis / Bedel: ${if (tr.feeEur > 0) FLFormatters.formatMarketValue(tr.feeEur) else "Bedelsiz / Akademi"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Piyasa Değeri: ${FLFormatters.formatMarketValue(tr.marketValueAtTransferEur)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // 7.4 SEASONAL PERFORMANCE TABLE
                val allSeasons: List<SeasonStatsRecord> = listOf(p.currentSeasonStats) + p.careerArchive.reversed()
                val totalApps = allSeasons.sumOf { it.appearances }
                val totalGoals = allSeasons.sumOf { it.goals }
                val totalAssists = allSeasons.sumOf { it.assists }
                val totalMins = allSeasons.sumOf { it.minutesPlayed }

                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                        border = BorderStroke(1.dp, AccentPrimary.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CareerKeyMetricBox("TOPLAM MAÇ", "$totalApps", TextPrimary)
                            CareerKeyMetricBox("TOPLAM GOL", "$totalGoals", AccentBright)
                            CareerKeyMetricBox("TOPLAM ASİST", "$totalAssists", GoldPrimary)
                            CareerKeyMetricBox("DAKİKA", "$totalMins'", TextSecondary)
                        }
                    }
                }

                items(allSeasons, key = { "${it.seasonYear}_${it.clubId}_${it.appearances}" }) { rec ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                        border = BorderStroke(1.dp, StrokePremium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${rec.seasonYear}/${(rec.seasonYear + 1) % 100} • ${rec.clubName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = rec.leagueName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                                FLRatingBadge(rating = rec.averageRating)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SeasonStatMiniCol("Maç", "${rec.appearances}")
                                SeasonStatMiniCol("İlk 11", "${rec.starts}")
                                SeasonStatMiniCol("Dakika", "${rec.minutesPlayed}'")
                                SeasonStatMiniCol("Gol", "${rec.goals}")
                                SeasonStatMiniCol("Asist", "${rec.assists}")
                                SeasonStatMiniCol("Kart", "${rec.yellowCards}S/${rec.redCards}K")
                            }
                        }
                    }
                }
            }

            3 -> {
                // 7.7 CONTRACT CENTER & TRANSFER OFFERS
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.55f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "MEVCUT SÖZLEŞME MERKEZİ",
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = GoldPrimary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Kulüp:", color = TextSecondary)
                                Text(state.contract.clubName, color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Haftalık Maaş:", color = TextSecondary)
                                Text(FLFormatters.formatWage(state.contract.weeklyWageEur), color = AccentBright, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Maç Başı / Gol Primi:", color = TextSecondary)
                                Text(
                                    "${FLFormatters.formatCurrency(state.contract.matchAppearanceBonusEur)} / ${FLFormatters.formatCurrency(state.contract.goalBonusEur)}",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Kadro Rolü & Süre:", color = TextSecondary)
                                Text("${state.contract.role.labelTr} (${state.contract.startSeasonYear} - ${state.contract.endSeasonYear})", color = TextPrimary)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Menajer:", color = TextSecondary)
                                Text(state.contract.agentName, color = GoldPrimary, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                FLPrimaryButton(
                                    text = "Sözleşme Uzat (+%15 Maaş)",
                                    onClick = onExtendContract,
                                    modifier = Modifier.weight(1f),
                                    testTag = "career_extend_contract_btn"
                                )
                                OutlinedButton(
                                    onClick = onRequestAgentTransferSearch,
                                    border = BorderStroke(1.dp, AccentBright),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("career_agent_search_btn")
                                ) {
                                    Text("Teklif Ara", color = TextPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                item {
                    FLSectionHeader(
                        title = "GELEN RESMİ TRANSFER TEKLİFLERİ",
                        subtitle = "${state.transferOffers.size} Aktif Teklif"
                    )
                }

                if (state.transferOffers.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                            border = BorderStroke(1.dp, StrokePremium),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Şu an masada aktif bir resmi teklif bulunmuyor.",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Maçlarda yüksek reyting alarak veya menajerine 'Teklif Ara' talimatı vererek kulüplerin ilgisini çekebilirsin.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(state.transferOffers, key = { it.id }) { offer ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                            border = BorderStroke(1.dp, AccentPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(offer.clubName, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                                        Text("${offer.leagueName} • Güç: ${offer.clubStrength}", style = MaterialTheme.typography.bodySmall, color = AccentBright)
                                    }
                                    Text(
                                        text = FLFormatters.formatWage(offer.offeredWeeklyWageEur),
                                        fontFamily = BarlowCondensedFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = GoldPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Vaat Edilen Rol: ${offer.promisedRole.labelTr} • Süre: ${offer.contractYears} Yıl • Gol Primi: ${FLFormatters.formatCurrency(offer.offeredGoalBonusEur)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedButton(
                                        onClick = { onNegotiateOffer(offer.id) },
                                        border = BorderStroke(1.dp, GoldPrimary),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Maaş Pazarlığı Yap", color = GoldPrimary, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = { onAcceptOffer(offer) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("accept_offer_${offer.id}")
                                    ) {
                                        Text("İmzala", color = Color(0xFF04150D), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                // 7.6 CAREER ACHIEVEMENTS & 7.8 INJURY HISTORY
                item {
                    FLSectionHeader(
                        title = "KAZANILAN BAŞARILAR & KUPALAR",
                        subtitle = "${state.achievements.size} Açık Başarı"
                    )
                }

                items(state.achievements, key = { it.id }) { ach ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = GoldPrimary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ach.title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text(ach.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                Text(ach.dateLabel, style = MaterialTheme.typography.labelSmall, color = GoldPrimary)
                            }
                        }
                    }
                }

                // Locked Target Milestones (Honest progression - locked until earned)
                val lockedGoals = listOf(
                    "Sezon Şampiyonluğu Kupası" to "Lig sezonunu 1. sırada tamamla",
                    "Gol Kralı Ödülü (Altın Krampon)" to "Ligi en çok gol atan oyuncu olarak bitir",
                    "10 Milyon € Piyasa Değeri Barajı" to "Performans ve transferlerle değerini €10 Mn üstüne çıkar"
                )
                items(lockedGoals, key = { it.first }) { (title, desc) ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePrimary.copy(alpha = 0.55f)),
                        border = BorderStroke(1.dp, StrokePremium),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Lock, contentDescription = null, tint = TextSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(title, style = MaterialTheme.typography.titleSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Text("$desc (Henüz Kazanılmadı)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    FLSectionHeader(
                        title = "SAKATLIK GEÇMİŞİ",
                        subtitle = if (state.injuryHistory.isEmpty()) "Temiz Sağlık Raporu" else "${state.injuryHistory.size} Kayıt"
                    )
                }

                if (state.injuryHistory.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                            border = BorderStroke(1.dp, StrokePremium),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Healing, contentDescription = null, tint = AccentBright)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Kariyerinde şu ana kadar ciddi bir sakatlık kaydı bulunmuyor.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                } else {
                    items(state.injuryHistory, key = { it.id }) { inj ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfacePrimary),
                            border = BorderStroke(1.dp, Negative.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(inj.injuryType, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                                    Text(inj.dateLabel, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                }
                                Text("${inj.recoveryDays} Gün Tedavi", style = MaterialTheme.typography.labelMedium, color = Negative, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FLMarketValueChartSection(
    points: List<MarketValuePoint>,
    currentValueEur: Long
) {
    val safePoints = if (points.isEmpty()) {
        listOf(
            MarketValuePoint(0, "Tem 2026", (currentValueEur * 0.85).toLong(), "Kulüp", "Başlangıç"),
            MarketValuePoint(1, "Ağu 2026", currentValueEur, "Kulüp", "Güncel")
        )
    } else points

    var selectedIdx by remember(safePoints.size) { mutableStateOf(safePoints.lastIndex) }
    val activePoint = safePoints.getOrElse(selectedIdx) { safePoints.last() }

    FLChartCard(
        title = "PİYASA DEĞERİ GRAFİĞİ (TRANSFERMARKT PRO)",
        subtitle = "Noktalara dokunarak dönemsel değer değişimini inceleyin",
        badgeText = FLFormatters.formatMarketValue(activePoint.valueEur),
        modifier = Modifier.testTag("market_value_chart_card")
    ) {
        // Selected Point Detail Banner
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SurfaceElevated,
            border = BorderStroke(1.dp, StrokePremiumStrong),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${activePoint.dateLabel.uppercase()} • ${activePoint.clubName}",
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        color = AccentBright,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = activePoint.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Text(
                    text = FLFormatters.formatMarketValue(activePoint.valueEur),
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AccentBright
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val minVal = (safePoints.minOf { it.valueEur } * 0.90).toLong().coerceAtLeast(10_000L)
        val maxVal = (safePoints.maxOf { it.valueEur } * 1.08).toLong().coerceAtLeast(minVal + 10_000L)
        val midVal = (minVal + maxVal) / 2L
        val range = (maxVal - minVal).coerceAtLeast(1L).toFloat()

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Y-axis reference scale column
            Column(
                modifier = Modifier
                    .height(175.dp)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = FLFormatters.formatMarketValue(maxVal),
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Text(
                    text = FLFormatters.formatMarketValue(midVal),
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Text(
                    text = FLFormatters.formatMarketValue(minVal),
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(175.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BackgroundSecondary)
                    .padding(14.dp)
                    .testTag("fl_market_value_canvas")
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(safePoints) {
                            detectTapGestures { tapOffset ->
                                if (safePoints.size == 1) {
                                    selectedIdx = 0
                                } else {
                                    val stepX = size.width.toFloat() / (safePoints.size - 1).coerceAtLeast(1)
                                    val nearest = ((tapOffset.x / stepX).toInt()).coerceIn(0, safePoints.lastIndex)
                                    selectedIdx = nearest
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // Horizontal reference grid lines
                    for (i in 0..3) {
                        val y = h * (i / 3f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.09f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val coords = safePoints.mapIndexed { idx, pt ->
                        val x = if (safePoints.size <= 1) w / 2f else (idx.toFloat() / (safePoints.size - 1)) * w
                        val normY = ((pt.valueEur - minVal).toFloat() / range).coerceIn(0f, 1f)
                        val y = h - (normY * (h * 0.82f) + h * 0.09f)
                        Offset(x, y)
                    }

                    if (coords.size >= 2) {
                        val linePath = Path().apply {
                            moveTo(coords.first().x, coords.first().y)
                            coords.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        val areaPath = Path().apply {
                            moveTo(coords.first().x, h)
                            coords.forEach { lineTo(it.x, it.y) }
                            lineTo(coords.last().x, h)
                            close()
                        }
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                listOf(
                                    AccentPrimary.copy(alpha = 0.42f),
                                    AccentPrimary.copy(alpha = 0.02f)
                                )
                            )
                        )
                        drawPath(
                            path = linePath,
                            color = AccentBright,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    coords.forEachIndexed { idx, offset ->
                        val isSelected = idx == selectedIdx
                        if (isSelected) {
                            drawLine(
                                color = AccentBright.copy(alpha = 0.35f),
                                start = Offset(offset.x, 0f),
                                end = Offset(offset.x, h),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        drawCircle(
                            color = if (isSelected) Color.White else AccentPrimary,
                            radius = if (isSelected) 7.dp.toPx() else 4.5.dp.toPx(),
                            center = offset
                        )
                        drawCircle(
                            color = if (isSelected) AccentPrimary else BackgroundPrimary,
                            radius = if (isSelected) 3.5.dp.toPx() else 2.dp.toPx(),
                            center = offset
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Taban: ${FLFormatters.formatMarketValue(safePoints.minOf { it.valueEur })}",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 11.sp,
                color = TextSecondary
            )
            Text(
                text = "Zirve: ${FLFormatters.formatMarketValue(safePoints.maxOf { it.valueEur })}",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 11.sp,
                color = AccentBright,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DossierBioCell(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 0.6.sp,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = TextPrimary
        )
    }
}

@Composable
private fun CareerKeyMetricBox(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = value,
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = valueColor
        )
    }
}

@Composable
private fun SeasonStatMiniCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontFamily = OutfitFontFamily,
            fontSize = 11.sp,
            color = TextMuted
        )
        Text(
            text = value,
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TextPrimary
        )
    }
}

@Composable
private fun TransfermarktPlayerDataAndPitchCard(
    state: CareerSaveState
) {
    val p = state.player
    val club = state.currentClub
    val tmNavyDark = Color(0xFF001935)
    val tmNavyHeader = Color(0xFF1A3151)
    val tmCyan = Color(0xFF00A6FF)
    val tmCyanBright = Color(0xFF5CE1E6)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = tmNavyDark),
        border = BorderStroke(1.dp, tmCyan.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transfermarkt_player_data_card")
    ) {
        Column {
            // Transfermarkt Section Header Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(tmNavyHeader)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "OYUNCU VERİLERİ & MEVKİ DETAYI",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.6.sp,
                    color = Color.White
                )
                Text(
                    text = "TRANSFERMARKT VERİTABANI",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 10.sp,
                    color = tmCyanBright
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Left Column: Structured Transfermarkt "OYUNCU VERİLERİ" Table
                Column(
                    modifier = Modifier.weight(1.35f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TransfermarktDataRow("Anavatandaki isim:", p.fullName)
                    TransfermarktDataRow("Doğum tarihi/Yaş:", "${p.birthDate} (${p.age})")
                    TransfermarktDataRow("Uyruk:", p.nationality)
                    TransfermarktDataRow("Boy / Kilo:", "${p.heightCm} cm / ${p.weightKg} kg")
                    TransfermarktDataRow("Mevki:", "${p.primaryPosition.category} - ${p.primaryPosition.titleTr}")
                    TransfermarktDataRow("Ayak:", p.preferredFoot.labelTr)
                    TransfermarktDataRow("Temsilci:", state.contract.agentName)
                    TransfermarktDataRow("Güncel kulüp:", club.name)
                    TransfermarktDataRow("Sözleşme sonu:", "30 Haz ${state.contract.endSeasonYear}")
                    TransfermarktDataRow("Maaş (Haftalık):", FLFormatters.formatCurrency(state.contract.weeklyWageEur))
                }

                // Right Column: 2D Pitch Position Diagram (Baş Mevki & Yan Mevki)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0A223D),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Baş mevki:",
                                fontFamily = OutfitFontFamily,
                                fontSize = 10.sp,
                                color = Color(0xFFA5B8CC)
                            )
                            Text(
                                text = "${p.primaryPosition.titleTr} (${p.primaryPosition.code})",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AccentBright,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Yan mevki:",
                                fontFamily = OutfitFontFamily,
                                fontSize = 10.sp,
                                color = Color(0xFFA5B8CC)
                            )
                            Text(
                                text = p.secondaryPositions.joinToString(", ") { "${it.titleTr} (${it.code})" }.ifBlank { "-" },
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 2D Football Pitch Canvas with Main & Secondary Position Dots
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(148.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF145A32))
                                    .border(1.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height
                                    val lineColor = Color.White.copy(alpha = 0.45f)

                                    // Turf stripes
                                    for (i in 0..5) {
                                        if (i % 2 == 0) {
                                            drawRect(
                                                color = Color(0xFF196F3D).copy(alpha = 0.45f),
                                                topLeft = Offset(0f, i * (h / 6f)),
                                                size = Size(w, h / 6f)
                                            )
                                        }
                                    }

                                    // Halfway line & center circle
                                    drawLine(
                                        color = lineColor,
                                        start = Offset(0f, h / 2f),
                                        end = Offset(w, h / 2f),
                                        strokeWidth = 1.2.dp.toPx()
                                    )
                                    drawCircle(
                                        color = lineColor,
                                        radius = w * 0.18f,
                                        center = Offset(w / 2f, h / 2f),
                                        style = Stroke(width = 1.2.dp.toPx())
                                    )

                                    // Top & Bottom Penalty Boxes
                                    drawRect(
                                        color = lineColor,
                                        topLeft = Offset(w * 0.22f, 0f),
                                        size = Size(w * 0.56f, h * 0.16f),
                                        style = Stroke(width = 1.2.dp.toPx())
                                    )
                                    drawRect(
                                        color = lineColor,
                                        topLeft = Offset(w * 0.22f, h * 0.84f),
                                        size = Size(w * 0.56f, h * 0.16f),
                                        style = Stroke(width = 1.2.dp.toPx())
                                    )

                                    // Secondary Positions (Yan Mevki) -> Cyan/Yellow smaller circles
                                    p.secondaryPositions.forEach { secPos ->
                                        val (nx, ny) = resolvePositionPitchCoordinates(secPos, isSecondary = true)
                                        val pt = Offset(w * nx, h * ny)
                                        drawCircle(
                                            color = Color.White,
                                            radius = 7.dp.toPx(),
                                            center = pt
                                        )
                                        drawCircle(
                                            color = Color(0xFF5CE1E6),
                                            radius = 5.dp.toPx(),
                                            center = pt
                                        )
                                    }

                                    // Primary Position (Baş Mevki) -> Glowing Green large circle
                                    val (px, py) = resolvePositionPitchCoordinates(p.primaryPosition, isSecondary = false)
                                    val mainPt = Offset(w * px, h * py)
                                    drawCircle(
                                        color = Color(0xFF20F587).copy(alpha = 0.35f),
                                        radius = 13.dp.toPx(),
                                        center = mainPt
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 9.dp.toPx(),
                                        center = mainPt
                                    )
                                    drawCircle(
                                        color = Color(0xFF15C868),
                                        radius = 6.5.dp.toPx(),
                                        center = mainPt
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

@Composable
private fun TransfermarktDataRow(label: String, value: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = OutfitFontFamily,
                fontSize = 11.sp,
                color = Color(0xFF9AB0C7),
                modifier = Modifier.weight(0.95f)
            )
            Text(
                text = value,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1.05f)
            )
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
    }
}

private fun resolvePositionPitchCoordinates(pos: Position, isSecondary: Boolean): Pair<Float, Float> {
    return when (pos) {
        Position.GK -> 0.50f to 0.90f
        Position.CB -> if (isSecondary) 0.36f to 0.76f else 0.50f to 0.76f
        Position.FB -> if (isSecondary) 0.18f to 0.72f else 0.82f to 0.72f
        Position.WB -> if (isSecondary) 0.16f to 0.58f else 0.84f to 0.58f
        Position.DM -> 0.50f to 0.62f
        Position.CM -> 0.50f to 0.48f
        Position.AM -> 0.50f to 0.34f
        Position.WG -> if (isSecondary) 0.20f to 0.26f else 0.80f to 0.26f
        Position.ST -> 0.50f to 0.16f
    }
}


