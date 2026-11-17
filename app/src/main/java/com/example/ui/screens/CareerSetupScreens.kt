package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.engine.CareerEngine
import com.example.engine.RealPlayerPreset
import com.example.engine.WorldDataCatalog
import com.example.model.Position
import com.example.model.PreferredFoot
import com.example.ui.components.BroadcastPlayerCard
import com.example.ui.components.ClubCrestBadge
import com.example.ui.components.parseHexColor
import com.example.ui.screens.setup.DeepPlayerCreatorScreen
import com.example.ui.screens.setup.TurkishLeagueClubPickerScreen
import com.example.ui.theme.BrightGold
import com.example.ui.theme.BrightNeonGreen
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.CrispWhiteBorder
import com.example.ui.theme.CrispWhiteBorderStrong
import com.example.ui.theme.DarkPitchSurface
import com.example.ui.theme.DarkTurfGreen
import com.example.ui.theme.DeepStadiumGreen
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.PrimaryWhite
import com.example.ui.theme.SecondaryMintGray
import com.example.ui.theme.TekoFontFamily
import com.example.ui.theme.VibrantFootballGreen
import com.example.viewmodel.CustomPlayerDraft

@Composable
fun CareerPathChoiceScreen(
    slotId: Int,
    onBack: () -> Unit,
    onSelectRealPlayerPath: () -> Unit,
    onSelectCustomPlayerPath: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPitchSurface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = PrimaryWhite)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "YENİ KARİYER YOLUNU SEÇ (YUVA #$slotId)",
                    fontFamily = TekoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = PrimaryWhite
                )
                Text(
                    text = "Kendi efsaneni sıfırdan yarat veya doğrulanmış gerçek bir yıldızla devam et",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryMintGray
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            // Option B: Create Custom Player (Primary Hero Card)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = DeepStadiumGreen,
                border = BorderStroke(2.dp, VibrantFootballGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable { onSelectCustomPlayerPath() }
                    .testTag("btn_path_custom_player")
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_training_ground),
                        contentDescription = "Kendi Futbolcunu Oluştur",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.38f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        DeepStadiumGreen.copy(alpha = 0.45f),
                                        DarkPitchSurface.copy(alpha = 0.94f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = VibrantFootballGreen,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "ÖNERİLEN • SIFIRDAN EFSANEYE",
                                style = MaterialTheme.typography.labelMedium,
                                color = DarkPitchSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "KENDİ FUTBOLCUNU OLUŞTUR",
                                fontFamily = TekoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 38.sp,
                                color = PrimaryWhite
                            )
                            Text(
                                text = "İsmini, mevkini, görünümünü ve geçmişini belirle. Amatör kümeden veya BAL liginden başlayıp dünya devlerine yüksel!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SecondaryMintGray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onSelectCustomPlayerPath,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VibrantFootballGreen,
                                    contentColor = DarkPitchSurface
                                )
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("KARAKTER OLUŞTURUCUYU AÇ", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Option A: Real Player Path
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = DeepStadiumGreen,
                border = BorderStroke(1.5.dp, ChampionshipGold),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable { onSelectRealPlayerPath() }
                    .testTag("btn_path_real_player")
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_stadium_hero),
                        contentDescription = "Gerçek Futbolcuyla Devam Et",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.38f)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        DeepStadiumGreen.copy(alpha = 0.45f),
                                        DarkPitchSurface.copy(alpha = 0.94f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = ChampionshipGold,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "GERÇEK FUTBOLCU VERİ ADAPTÖRÜ",
                                style = MaterialTheme.typography.labelMedium,
                                color = DarkPitchSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "GERÇEK FUTBOLCUYLA DEVAM ET",
                                fontFamily = TekoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 38.sp,
                                color = PrimaryWhite
                            )
                            Text(
                                text = "Mason Greenwood, Arda Güler, Kenan Yıldız, Semih Kılıçsoy, Barış Alper Yılmaz, Ferdi Kadıoğlu veya Hakan Çalhanoğlu ile gerçek kariyer verilerinden devam et.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SecondaryMintGray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onSelectRealPlayerPath,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ChampionshipGold,
                                    contentColor = DarkPitchSurface
                                )
                            ) {
                                Icon(Icons.Default.PersonSearch, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("FUTBOLCU KATALOĞUNU ARA", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RealPlayerPickerScreen(
    onBack: () -> Unit,
    onStartAtCurrentClub: (RealPlayerPreset) -> Unit,
    onPickCustomClubForPreset: (RealPlayerPreset) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val presets = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            WorldDataCatalog.realPlayerPresets
        } else {
            val q = searchQuery.trim().lowercase()
            WorldDataCatalog.realPlayerPresets.filter {
                "${it.firstName} ${it.lastName}".lowercase().contains(q) ||
                    it.currentClubDisplayName.lowercase().contains(q) ||
                    it.nationality.lowercase().contains(q) ||
                    it.primaryPosition.titleTr.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPitchSurface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = PrimaryWhite)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "GERÇEK FUTBOLCU VERİ TABANI",
                    fontFamily = TekoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = PrimaryWhite
                )
                Text(
                    text = "Doğrulanmış 2024/25 - 2026/27 Sezonu Oyuncu Profilleri",
                    style = MaterialTheme.typography.bodySmall,
                    color = VibrantFootballGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VibrantFootballGreen) },
            placeholder = { Text("Örn: Mason Greenwood, Arda Güler, Kenan Yıldız...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_search_real_player")
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (presets.isEmpty()) {
            Surface(
                color = DeepStadiumGreen,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CrispWhiteBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "\"$searchQuery\" için doğrulanmış katalog kaydı bulunamadı.",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Football Life kuralı gereği doğrulanmayan futbolcu istatistikleri uydurulmaz. Katalogdaki doğrulanmış yıldızlardan birini seçebilir veya Kendi Futbolcunu Oluştur ekranından dilediğin profili tanımlayabilirsin.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryMintGray
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(presets, key = { it.id }) { preset ->
                    val ovr = preset.attributes.calculateOverall(preset.primaryPosition)
                    Surface(
                        color = DeepStadiumGreen,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, GoldBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            BroadcastPlayerCard(
                                fullName = "${preset.firstName} ${preset.lastName}",
                                kitName = preset.kitName,
                                kitNumber = preset.kitNumber,
                                position = preset.primaryPosition,
                                overallRating = ovr,
                                potential = preset.potential,
                                clubName = preset.currentClubDisplayName,
                                clubShortName = preset.currentClubDisplayName.take(3).uppercase(),
                                clubPrimaryHex = "#1B4F72",
                                clubSecondaryHex = "#F7FAF8",
                                nationality = preset.nationality,
                                attributes = preset.attributes,
                                appearance = preset.appearance
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = VibrantFootballGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = preset.verifiedSeasonSource,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VibrantFootballGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = preset.careerSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryMintGray
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Yaş: ${preset.age} • Boy: ${preset.heightCm} cm • Ayak: ${preset.preferredFoot.labelTr} • Sözleşme Bitiş: ${preset.contractEndYear} • Maaş: ${preset.weeklyWageEur} €/Hafta",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryWhite
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onStartAtCurrentClub(preset) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VibrantFootballGreen,
                                        contentColor = DarkPitchSurface
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_start_real_${preset.id}")
                                ) {
                                    Text(
                                        text = "${preset.currentClubDisplayName.uppercase()} İLE BAŞLA",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                OutlinedButton(
                                    onClick = { onPickCustomClubForPreset(preset) },
                                    border = BorderStroke(1.dp, ChampionshipGold),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ChampionshipGold)
                                ) {
                                    Text("Farklı Kulüp Seç")
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
fun CustomPlayerCreatorScreen(
    draft: CustomPlayerDraft,
    onUpdateDraft: ((CustomPlayerDraft) -> CustomPlayerDraft) -> Unit,
    onBack: () -> Unit,
    onProceedToClubSelect: () -> Unit
) {
    DeepPlayerCreatorScreen(
        draft = draft,
        onUpdateDraft = onUpdateDraft,
        onBack = onBack,
        onProceedToClubSelect = onProceedToClubSelect
    )
}

@Composable
fun LeagueAndClubPickerScreen(
    selectedLeagueId: String,
    onSelectLeague: (String) -> Unit,
    onSignWithClub: (String, String) -> Unit,
    onBack: () -> Unit
) {
    TurkishLeagueClubPickerScreen(
        selectedLeagueId = selectedLeagueId,
        onSelectLeague = onSelectLeague,
        onSignWithClub = onSignWithClub,
        onBack = onBack
    )
}
