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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomPlayerCreatorScreen(
    draft: CustomPlayerDraft,
    onUpdateDraft: ((CustomPlayerDraft) -> CustomPlayerDraft) -> Unit,
    onBack: () -> Unit,
    onProceedToClubSelect: () -> Unit
) {
    val previewAttrs = remember(draft.primaryPosition, draft.backgroundStory) {
        CareerEngine.createBalancedCustomAttributes(draft.primaryPosition, 5, draft.backgroundStory)
    }
    val previewOvr = previewAttrs.calculateOverall(draft.primaryPosition)
    val age = (2026 - draft.birthYear).coerceIn(16, 35)
    val previewPot = (previewOvr + (28 - age).coerceIn(6, 24)).coerceAtMost(92)

    val skinTones = listOf("#F2C9AC", "#E5B592", "#D9A17C", "#B87D56", "#8C583A", "#5C3824")
    val hairStyles = listOf("Modern Kısa", "Dalgalı Modern", "Kıvırcık Kısa", "Kısa Klasik", "Modern Sarı Röfle")
    val hairColors = listOf("#181513", "#2A2018", "#5C3A21", "#C29B48", "#8C2D19")
    val bootColors = listOf("#20C773", "#FFD166", "#FF4D5E", "#5DADE2", "#F7FAF8")
    val backgrounds = listOf(
        "Altyapı Akademisi Çıkışlı",
        "Sokak Futbolu Yeteneği",
        "Atletik Gelişim Merkezi"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPitchSurface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = PrimaryWhite)
                    }
                    Column {
                        Text(
                            text = "KENDİ FUTBOLCUNU OLUŞTUR",
                            fontFamily = TekoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            color = PrimaryWhite
                        )
                        Text(
                            text = "Kariyer ve karakter kimliğini özelleştir",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryMintGray
                        )
                    }
                }
                Button(
                    onClick = onProceedToClubSelect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VibrantFootballGreen,
                        contentColor = DarkPitchSurface
                    ),
                    modifier = Modifier.testTag("btn_proceed_to_club_picker")
                ) {
                    Text("KULÜP SEÇİMİNE GEÇ →", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Live Broadcast Card Preview
        item {
            BroadcastPlayerCard(
                fullName = "${draft.firstName} ${draft.lastName}",
                kitName = draft.kitName,
                kitNumber = draft.kitNumber,
                position = draft.primaryPosition,
                overallRating = previewOvr,
                potential = previewPot,
                clubName = "Kulüp Seçilecek",
                clubShortName = "FL",
                clubPrimaryHex = "#103E2D",
                clubSecondaryHex = "#20C773",
                nationality = draft.nationality,
                attributes = previewAttrs,
                appearance = draft.appearance
            )
        }

        // Personal Info Section
        item {
            Surface(
                color = DeepStadiumGreen,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CrispWhiteBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("KİMLİK VE FORMA BİLGİLERİ", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = draft.firstName,
                            onValueChange = { v -> onUpdateDraft { it.copy(firstName = v) } },
                            label = { Text("İsim") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_first_name")
                        )
                        OutlinedTextField(
                            value = draft.lastName,
                            onValueChange = { v ->
                                onUpdateDraft {
                                    it.copy(
                                        lastName = v,
                                        kitName = v.uppercase().take(12)
                                    )
                                }
                            },
                            label = { Text("Soyisim") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_last_name")
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = draft.kitName,
                            onValueChange = { v -> onUpdateDraft { it.copy(kitName = v.uppercase().take(14)) } },
                            label = { Text("Forma Adı") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = draft.city,
                            onValueChange = { v -> onUpdateDraft { it.copy(city = v) } },
                            label = { Text("Doğum Şehri") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Kit Number & Birth Year Quick Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Forma Numarası: #${draft.kitNumber}", style = MaterialTheme.typography.bodyMedium, color = PrimaryWhite)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(7, 8, 9, 10, 11, 19, 21, 53, 99).forEach { num ->
                                val selected = draft.kitNumber == num
                                Surface(
                                    color = if (selected) VibrantFootballGreen else DarkPitchSurface,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, CrispWhiteBorder),
                                    modifier = Modifier.clickable { onUpdateDraft { it.copy(kitNumber = num) } }
                                ) {
                                    Text(
                                        text = num.toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) DarkPitchSurface else PrimaryWhite,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Position & Foot Section
        item {
            Surface(
                color = DeepStadiumGreen,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CrispWhiteBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ANA MEVKİ VE BASKIN AYAK", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Position.entries.forEach { pos ->
                            val selected = draft.primaryPosition == pos
                            FilterChip(
                                selected = selected,
                                onClick = { onUpdateDraft { it.copy(primaryPosition = pos) } },
                                label = { Text("${pos.code} - ${pos.titleTr}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VibrantFootballGreen,
                                    selectedLabelColor = DarkPitchSurface
                                ),
                                modifier = Modifier.testTag("chip_pos_${pos.name}")
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PreferredFoot.entries.forEach { foot ->
                            val selected = draft.preferredFoot == foot
                            FilterChip(
                                selected = selected,
                                onClick = { onUpdateDraft { it.copy(preferredFoot = foot) } },
                                label = { Text("Ayak: ${foot.labelTr}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ChampionshipGold,
                                    selectedLabelColor = DarkPitchSurface
                                )
                            )
                        }
                    }
                }
            }
        }

        // Visual Appearance & Background Story Section
        item {
            Surface(
                color = DeepStadiumGreen,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CrispWhiteBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("GÖRÜNÜM VE FUTBOL GEÇMİŞİ", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)

                    Text("Ten Tonu", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        skinTones.forEach { hex ->
                            val isSelected = draft.appearance.skinToneHex == hex
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(hex))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) BrightNeonGreen else CrispWhiteBorder,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onUpdateDraft {
                                            it.copy(appearance = it.appearance.copy(skinToneHex = hex))
                                        }
                                    }
                            )
                        }
                    }

                    Text("Saç Modeli", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        hairStyles.forEach { styleName ->
                            val selected = draft.appearance.hairStyle == styleName
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    onUpdateDraft {
                                        it.copy(appearance = it.appearance.copy(hairStyle = styleName))
                                    }
                                },
                                label = { Text(styleName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VibrantFootballGreen,
                                    selectedLabelColor = DarkPitchSurface
                                )
                            )
                        }
                    }

                    Text("Saç Rengi ve Krampon", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        hairColors.forEach { hex ->
                            val isSelected = draft.appearance.hairColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(hex))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) ChampionshipGold else CrispWhiteBorder,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        onUpdateDraft {
                                            it.copy(appearance = it.appearance.copy(hairColorHex = hex))
                                        }
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        bootColors.forEach { hex ->
                            val isSelected = draft.appearance.bootColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(parseHexColor(hex))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) PrimaryWhite else CrispWhiteBorder,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        onUpdateDraft {
                                            it.copy(appearance = it.appearance.copy(bootColorHex = hex))
                                        }
                                    }
                            )
                        }
                    }

                    Text("Başlangıç Futbol Geçmişi", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        backgrounds.forEach { bg ->
                            val selected = draft.backgroundStory == bg
                            Surface(
                                color = if (selected) DarkTurfGreen else DarkPitchSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (selected) VibrantFootballGreen else CrispWhiteBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onUpdateDraft { it.copy(backgroundStory = bg) } }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(bg, style = MaterialTheme.typography.bodyMedium, color = PrimaryWhite)
                                    if (selected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrightNeonGreen)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onProceedToClubSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibrantFootballGreen,
                    contentColor = DarkPitchSurface
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(Icons.Default.SportsSoccer, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LİG VE KULÜP SEÇİMİNE DEVAM ET",
                    fontFamily = TekoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Composable
fun LeagueAndClubPickerScreen(
    selectedLeagueId: String,
    onSelectLeague: (String) -> Unit,
    onSignWithClub: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val leagues = WorldDataCatalog.leagues
    val activeLeague = leagues.find { it.id == selectedLeagueId } ?: leagues.first()
    val clubs = remember(selectedLeagueId) {
        WorldDataCatalog.clubsForLeague(selectedLeagueId)
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
                    text = "FUTBOL DÜNYASI & KULÜP SEÇİMİ",
                    fontFamily = TekoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = PrimaryWhite
                )
                Text(
                    text = "Bölgesel Amatör Lig (BAL) ve alt liglerden Avrupa devlerine kadar kulübünü seç",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryMintGray
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Horizontal League Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            leagues.forEach { league ->
                val selected = league.id == selectedLeagueId
                FilterChip(
                    selected = selected,
                    onClick = { onSelectLeague(league.id) },
                    label = {
                        Text(
                            text = "${league.name} (Kademe ${league.tier})",
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VibrantFootballGreen,
                        selectedLabelColor = DarkPitchSurface
                    ),
                    modifier = Modifier.testTag("chip_league_${league.id}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = DarkTurfGreen.copy(alpha = 0.65f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CrispWhiteBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "${activeLeague.name} • ${activeLeague.country}",
                    style = MaterialTheme.typography.titleSmall,
                    color = ChampionshipGold
                )
                Text(
                    text = activeLeague.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryWhite
                )
                Text(
                    text = activeLeague.dataLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = SecondaryMintGray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(clubs, key = { it.id }) { club ->
                Surface(
                    color = DeepStadiumGreen,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CrispWhiteBorderStrong),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                    shortName = club.shortName,
                                    primaryHex = club.primaryColorHex,
                                    secondaryHex = club.secondaryColorHex,
                                    size = 50.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = club.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = PrimaryWhite
                                        )
                                        if (club.isDemoClub) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = DarkPitchSurface,
                                                shape = RoundedCornerShape(4.dp),
                                                border = BorderStroke(1.dp, GoldBorder)
                                            ) {
                                                Text(
                                                    text = "ALT LİG AKADEMİ",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = ChampionshipGold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${club.city} • Stadyum: ${club.stadiumName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SecondaryMintGray
                                    )
                                    Text(
                                        text = "Teknik Direktör: ${club.managerName} (${club.managerTacticalStyle})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VibrantFootballGreen
                                    )
                                }
                            }

                            Surface(
                                color = DarkPitchSurface,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, GoldBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("GÜÇ", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                                    Text(
                                        text = club.teamStrength.toString(),
                                        fontFamily = TekoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        color = BrightGold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onSignWithClub(activeLeague.id, club.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VibrantFootballGreen,
                                contentColor = DarkPitchSurface
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_sign_club_${club.id}")
                        ) {
                            Text(
                                text = "${club.name.uppercase()} İLE SÖZLEŞME İMZALA",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
