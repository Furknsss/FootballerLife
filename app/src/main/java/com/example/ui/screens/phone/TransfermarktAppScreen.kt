package com.example.ui.screens.phone

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.social.SocialWorldGenerator
import com.example.model.CareerSaveState
import com.example.ui.components.FLFormatters
import com.example.ui.components.FootballerAvatarCanvas
import com.example.ui.components.parseSafeHexColor
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun TransfermarktAppScreen(
    state: CareerSaveState,
    onBackHome: () -> Unit
) {
    val tmNavy = Color(0xFF00193F)
    val tmDarkBg = Color(0xFF071120)
    val tmCardBg = Color(0xFF0F213C)
    val tmCyan = Color(0xFF5CE1E6)
    val tmBlue = Color(0xFF00A6FF)
    val tmMuted = Color(0xFF94A3B8)

    var tabIndex by rememberSaveable { mutableStateOf(0) } // 0: Oyuncu Profilim, 1: Söylenti Değirmeni, 2: Lig & Krallık
    val p = state.player
    val club = state.currentClub
    val rumors = SocialWorldGenerator.getTransfermarktRumors(state)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(tmDarkBg)
            .testTag("phone_transfermarkt_app")
    ) {
        // Authentic Transfermarkt Header with logo_transfermarkt
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(tmNavy)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackHome,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("phone_app_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = Color.White
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.logo_transfermarkt),
                    contentDescription = "Transfermarkt",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "TRANSFERMARKT",
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Piyasa Değerleri, İstatistik & Söylenti Değirmeni",
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        color = tmCyan
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = tmBlue.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, tmCyan)
            ) {
                Text(
                    text = "TR • ${state.currentSeasonYear}",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = tmCyan,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Sub-navigation Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(tmNavy.copy(alpha = 0.7f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Oyuncu Kartım" to 0,
                "Söylenti Değirmeni (${rumors.size})" to 1,
                "Lig & Gol Krallığı" to 2
            ).forEach { (title, idx) ->
                val active = tabIndex == idx
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (active) tmBlue else tmCardBg,
                    border = BorderStroke(1.dp, if (active) tmCyan else Color.Transparent),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { tabIndex = idx }
                ) {
                    Text(
                        text = title,
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                }
            }
        }

        when (tabIndex) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Main Transfermarkt Player Value Hero Card
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = tmCardBg),
                            border = BorderStroke(1.dp, tmCyan.copy(alpha = 0.45f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FootballerAvatarCanvas(
                                        appearance = p.appearance,
                                        clubPrimaryHex = club.primaryColorHex,
                                        clubSecondaryHex = club.secondaryColorHex,
                                        kitNumber = p.kitNumber,
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .border(1.5.dp, tmCyan, RoundedCornerShape(14.dp))
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "#${p.kitNumber} ${p.fullName}",
                                                fontFamily = BarlowCondensedFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 24.sp,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Filled.Verified,
                                                contentDescription = null,
                                                tint = tmCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = "${club.name} • ${club.leagueName}",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 13.sp,
                                            color = tmCyan,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "Mevki: ${p.primaryPosition.titleTr} (${p.primaryPosition.code}) • Yaş: ${p.age} • Ayak: ${p.preferredFoot.labelTr}",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 11.5.sp,
                                            color = tmMuted
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = "GÜNCEL PİYASA DEĞERİ",
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = tmCyan
                                        )
                                        Text(
                                            text = FLFormatters.formatMarketValue(p.marketValueEur),
                                            fontFamily = BarlowCondensedFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 36.sp,
                                            color = Color.White
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Son Güncelleme: Gün ${state.currentDayIndex}",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 11.sp,
                                            color = tmMuted
                                        )
                                        Text(
                                            text = "OVR ${p.overallRating} • POT ${p.potential}",
                                            fontFamily = SpaceGroteskFontFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF22C55E)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        // Season Performance & Contract Details Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = tmCardBg),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "${state.currentSeasonYear}/${state.currentSeasonYear + 1} SEZON VERİLERİ & SÖZLEŞME",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tmCyan
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TmStatBox("Maç", "${p.currentSeasonStats.appearances}")
                                    TmStatBox("Gol", "${p.currentSeasonStats.goals}")
                                    TmStatBox("Asist", "${p.currentSeasonStats.assists}")
                                    TmStatBox("Ort. Puan", "${p.currentSeasonStats.averageRating}")
                                }
                                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                                Text(
                                    text = "Temsilci: ${state.contract.agentName} • Kadro Rolü: ${state.contract.role.labelTr} • Sözleşme Bitiş: 30.06.${state.contract.endSeasonYear}",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 12.sp,
                                    color = tmMuted
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "PİYASA DEĞERİ GEÇMİŞİ",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = tmCyan
                        )
                    }

                    items(state.marketValueHistory.reversed(), key = { "${it.dayIndex}_${it.valueEur}" }) { pt ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = tmCardBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(pt.reason, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${pt.clubName} • ${pt.dateLabel}", color = tmMuted, fontSize = 11.sp)
                                }
                                Text(
                                    text = FLFormatters.formatMarketValue(pt.valueEur),
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = tmCyan
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // Söylenti Değirmeni (Rumor Mill)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = "TRANSFERMARKT SÖYLENTİ DEĞİRMENİ (CANLI OLASILIKLAR)",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = tmCyan
                        )
                    }
                    items(rumors, key = { it.id }) { rm ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = tmCardBg),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (rm.isUserPlayer) tmCyan else Color.White.copy(alpha = 0.1f)
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
                                                text = rm.playerName,
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = tmBlue.copy(alpha = 0.25f)
                                            ) {
                                                Text(
                                                    text = "${rm.playerPosition} • ${rm.age} yaş",
                                                    fontSize = 10.sp,
                                                    color = tmCyan,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${rm.fromClubName}  ➔  ${rm.toClubName}",
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFFE2E8F0)
                                        )
                                        Text(
                                            text = "Piyasa Değeri: ${FLFormatters.formatMarketValue(rm.marketValueEur)} • Kaynak: ${rm.sourceHandle}",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 11.sp,
                                            color = tmMuted
                                        )
                                    }

                                    // Probability Badge
                                    val probColor = when {
                                        rm.probabilityPct >= 65 -> Color(0xFF22C55E)
                                        rm.probabilityPct >= 45 -> Color(0xFFF59E0B)
                                        else -> tmCyan
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = probColor.copy(alpha = 0.18f),
                                        border = BorderStroke(1.dp, probColor)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "%${rm.probabilityPct}",
                                                fontFamily = BarlowCondensedFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 22.sp,
                                                color = probColor
                                            )
                                            Text(
                                                text = "İhtimal",
                                                fontSize = 9.sp,
                                                color = Color.White
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
                // Lig & Gol Krallığı
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "${club.leagueName.uppercase()} • GOL KRALLIĞI VE FORM",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = tmCyan
                        )
                    }
                    items(state.topScorers.take(12), key = { it.playerId }) { entry ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (entry.isUserPlayer) tmBlue.copy(alpha = 0.24f) else tmCardBg,
                            border = BorderStroke(
                                1.dp,
                                if (entry.isUserPlayer) tmCyan else Color.White.copy(alpha = 0.06f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.playerName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (entry.isUserPlayer) tmCyan else Color.White
                                    )
                                    Text(
                                        text = "${entry.clubName} • ${entry.matches} Maç • Ort: ${entry.averageRating}",
                                        fontSize = 11.5.sp,
                                        color = tmMuted
                                    )
                                }
                                Text(
                                    text = "${entry.goals}G / ${entry.assists}A",
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TmStatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = BarlowCondensedFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.White
        )
        Text(
            text = label,
            fontFamily = OutfitFontFamily,
            fontSize = 11.sp,
            color = Color(0xFF94A3B8)
        )
    }
}
