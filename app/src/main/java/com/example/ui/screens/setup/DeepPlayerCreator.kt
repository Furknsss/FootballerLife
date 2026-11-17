package com.example.ui.screens.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlayerAppearance
import com.example.model.PlayerAttributes
import com.example.model.Position
import com.example.model.PreferredFoot
import com.example.ui.components.BroadcastPlayerCard
import com.example.ui.components.FootballerAvatarCanvas
import com.example.ui.components.parseHexColor
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSecondary
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
import com.example.ui.theme.StrokePremium
import com.example.ui.theme.StrokePremiumStrong
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePrimary
import com.example.ui.theme.TekoFontFamily
import com.example.ui.theme.TextMuted
import com.example.ui.theme.VibrantFootballGreen
import com.example.viewmodel.CustomPlayerDraft

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeepPlayerCreatorScreen(
    draft: CustomPlayerDraft,
    onUpdateDraft: ((CustomPlayerDraft) -> CustomPlayerDraft) -> Unit,
    onBack: () -> Unit,
    onProceedToClubSelect: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("1. Kimlik & Köken", "2. Mevki & Rol", "3. Fiziksel", "4. Yetenekler", "5. Görünüm")

    val archetypes = remember(draft.primaryPosition) {
        PlayerCreatorCatalog.getArchetypes(draft.primaryPosition)
    }

    val selectedRole = remember(draft.primaryPosition, draft.tacticalRole) {
        archetypes.find { it.name == draft.tacticalRole } ?: archetypes.first()
    }

    // Keep draft.tacticalRole in sync if position changed and role not in position archetypes
    LaunchedEffect(draft.primaryPosition) {
        if (archetypes.none { it.name == draft.tacticalRole }) {
            val fallback = archetypes.first()
            onUpdateDraft { it.copy(tacticalRole = fallback.name) }
        }
    }

    val selectedOrigin = remember(draft.backgroundStory) {
        PlayerCreatorCatalog.origins.find { it.title == draft.backgroundStory }
            ?: PlayerCreatorCatalog.origins.first()
    }

    // Stat points allocations: Category -> allocated extra points (0..12 each)
    val statAllocations = remember { mutableStateMapOf<AllocatableStat, Int>() }
    val totalAllocated = remember {
        derivedStateOf { statAllocations.values.sum() }
    }
    val remainingPoints = remember {
        derivedStateOf { PlayerCreatorCatalog.STARTING_POINTS_BUDGET - totalAllocated.value }
    }

    // Compute live final PlayerAttributes combining base archetype + origin modifier + allocated stat points
    val finalAttributes = remember(selectedRole, selectedOrigin, totalAllocated.value) {
        val base = selectedOrigin.attributeModifier(selectedRole.baseAttributes)
        val withAllocations = base.copy(
            shooting = (base.shooting + (statAllocations[AllocatableStat.SHOOTING] ?: 0)).coerceAtMost(PlayerCreatorCatalog.MAX_STAT_CAP),
            passing = (base.passing + (statAllocations[AllocatableStat.PASSING] ?: 0)).coerceAtMost(PlayerCreatorCatalog.MAX_STAT_CAP),
            dribbling = (base.dribbling + (statAllocations[AllocatableStat.DRIBBLING] ?: 0)).coerceAtMost(PlayerCreatorCatalog.MAX_STAT_CAP),
            firstTouch = (base.firstTouch + (statAllocations[AllocatableStat.FIRST_TOUCH] ?: 0)).coerceAtMost(PlayerCreatorCatalog.MAX_STAT_CAP),
            pace = (base.pace + (statAllocations[AllocatableStat.PACE] ?: 0)).coerceAtMost(PlayerCreatorCatalog.MAX_STAT_CAP),
            agility = (base.agility + (statAllocations[AllocatableStat.AGILITY] ?: 0)).coerceAtMost(PlayerCreatorCatalog.MAX_STAT_CAP),
            stamina = (base.stamina + (statAllocations[AllocatableStat.STAMINA] ?: 0)).coerceAtMost(PlayerCreatorCatalog.MAX_STAT_CAP),
            strength = (base.strength + (statAllocations[AllocatableStat.STRENGTH] ?: 0)).coerceAtMost(PlayerCreatorCatalog.MAX_STAT_CAP),
            defending = (base.defending + (statAllocations[AllocatableStat.DEFENDING] ?: 0)).coerceAtMost(PlayerCreatorCatalog.MAX_STAT_CAP),
            tactics = (base.tactics + (statAllocations[AllocatableStat.TACTICS] ?: 0)).coerceAtMost(PlayerCreatorCatalog.MAX_STAT_CAP),
            vision = (base.vision + (statAllocations[AllocatableStat.VISION] ?: 0)).coerceAtMost(PlayerCreatorCatalog.MAX_STAT_CAP)
        )
        withAllocations
    }

    // Keep draft.customAttributes updated so finalizeNewCareerWithClub preserves the tailored attributes
    LaunchedEffect(finalAttributes, selectedRole.name) {
        onUpdateDraft {
            it.copy(
                customAttributes = finalAttributes,
                tacticalRole = selectedRole.name
            )
        }
    }

    val previewOvr = remember(finalAttributes, draft.primaryPosition) {
        finalAttributes.calculateOverall(draft.primaryPosition)
    }
    val age = (2026 - draft.birthYear).coerceIn(16, 35)
    val previewPot = remember(previewOvr, age) {
        (previewOvr + (28 - age).coerceIn(6, 24)).coerceIn(previewOvr + 4, 94)
    }

    val skinTones = listOf("#F2C9AC", "#E5B592", "#D9A17C", "#B87D56", "#8C583A", "#5C3824")
    val hairStyles = listOf("Modern Kısa", "Fade & Yanlar Sıfır", "Dalgalı Modern", "Kıvırcık Kısa", "Kısa Klasik", "Modern Sarı Röfle")
    val hairColors = listOf("#181513", "#2A2018", "#5C3A21", "#C29B48", "#8C2D19")
    val bootColors = listOf("#20C773", "#FFD166", "#FF4D5E", "#5DADE2", "#F7FAF8")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPitchSurface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = PrimaryWhite)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "KENDİ FUTBOLCUNU OLUŞTUR",
                        fontFamily = TekoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = PrimaryWhite
                    )
                    Text(
                        text = "${draft.firstName} ${draft.lastName} • ${draft.primaryPosition.titleTr} (${selectedRole.name})",
                        style = MaterialTheme.typography.labelSmall,
                        color = VibrantFootballGreen
                    )
                }
            }

            Button(
                onClick = onProceedToClubSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibrantFootballGreen,
                    contentColor = DarkPitchSurface
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_proceed_to_club_picker")
            ) {
                Text("KULÜP SEÇİMİNE GEÇ →", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Section Tabs
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = DeepStadiumGreen,
            contentColor = PrimaryWhite,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = VibrantFootballGreen
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeTab == index) VibrantFootballGreen else SecondaryMintGray
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Broadcast Preview & Mini Spec Card
            item {
                Surface(
                    color = DeepStadiumGreen,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, GoldBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FootballerAvatarCanvas(
                                    appearance = draft.appearance,
                                    clubPrimaryHex = "#103E2D",
                                    clubSecondaryHex = "#20C773",
                                    kitNumber = draft.kitNumber,
                                    modifier = Modifier.size(76.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${draft.firstName} ${draft.lastName}".uppercase(),
                                        fontFamily = TekoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 26.sp,
                                        color = PrimaryWhite
                                    )
                                    Text(
                                        text = "${draft.primaryPosition.code} • ${selectedRole.name} • #${draft.kitNumber}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ChampionshipGold,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${selectedOrigin.iconEmoji} ${selectedOrigin.title} • ${draft.city}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SecondaryMintGray
                                    )
                                }
                            }

                            // Rating badges
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    color = DarkPitchSurface,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, VibrantFootballGreen)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("GEN", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                                        Text(
                                            text = previewOvr.toString(),
                                            fontFamily = TekoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp,
                                            color = VibrantFootballGreen
                                        )
                                    }
                                }
                                Surface(
                                    color = DarkPitchSurface,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, ChampionshipGold)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("POT", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                                        Text(
                                            text = previewPot.toString(),
                                            fontFamily = TekoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp,
                                            color = BrightGold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            when (activeTab) {
                0 -> {
                    // TAB 0: Identity & Lore
                    item {
                        Surface(
                            color = DeepStadiumGreen,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CrispWhiteBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("TEMEL KİMLİK BİLGİLERİ", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
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

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Başlangıç Yaşı: $age (${draft.birthYear})", style = MaterialTheme.typography.bodyMedium, color = PrimaryWhite)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(2008 to 18, 2007 to 19, 2006 to 20, 2005 to 21).forEach { (year, playerAge) ->
                                            val selected = draft.birthYear == year
                                            FilterChip(
                                                selected = selected,
                                                onClick = { onUpdateDraft { it.copy(birthYear = year) } },
                                                label = { Text("$playerAge Yaş") },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = VibrantFootballGreen,
                                                    selectedLabelColor = DarkPitchSurface
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            color = DeepStadiumGreen,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CrispWhiteBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("FUTBOL KÖKENİ & GEÇMİŞ HİKAYESİ", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
                                Text(
                                    text = "Kökeniniz, başlangıç niteliklerinizi ve antrenman yatkınlığınızı kalıcı olarak etkiler.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryMintGray
                                )

                                PlayerCreatorCatalog.origins.forEach { origin ->
                                    val isSelected = draft.backgroundStory == origin.title
                                    Surface(
                                        color = if (isSelected) DarkTurfGreen else DarkPitchSurface,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.5.dp, if (isSelected) VibrantFootballGreen else CrispWhiteBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onUpdateDraft { it.copy(backgroundStory = origin.title) }
                                            }
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(origin.iconEmoji, fontSize = 22.sp)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(origin.title, fontWeight = FontWeight.Bold, color = PrimaryWhite, fontSize = 15.sp)
                                                        Text(origin.subtitle, style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                                                    }
                                                }
                                                if (isSelected) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrightNeonGreen)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(origin.lore, style = MaterialTheme.typography.bodySmall, color = PrimaryWhite)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Surface(
                                                color = VibrantFootballGreen.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "Bonus: ${origin.perkDescription}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = VibrantFootballGreen,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Position & Tactical Archetype Role
                    item {
                        Surface(
                            color = DeepStadiumGreen,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CrispWhiteBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("ANA SAHA MEVKİİ", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
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
                            }
                        }
                    }

                    item {
                        Surface(
                            color = DeepStadiumGreen,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CrispWhiteBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("TAKTİKSEL ROL VE OYUN ARKETİPİ", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
                                    Surface(
                                        color = DarkPitchSurface,
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, GoldBorder)
                                    ) {
                                        Text(
                                            text = "${archetypes.size} Rol Mevcut",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BrightGold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                archetypes.forEach { role ->
                                    val isSelected = selectedRole.id == role.id
                                    Surface(
                                        color = if (isSelected) DarkTurfGreen else DarkPitchSurface,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.5.dp, if (isSelected) VibrantFootballGreen else CrispWhiteBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onUpdateDraft { it.copy(tacticalRole = role.name) }
                                            }
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        color = if (isSelected) VibrantFootballGreen else DeepStadiumGreen,
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text(
                                                            text = role.badge,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = if (isSelected) DarkPitchSurface else PrimaryWhite,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = role.name,
                                                        fontWeight = FontWeight.Bold,
                                                        color = PrimaryWhite,
                                                        fontSize = 15.sp
                                                    )
                                                }
                                                if (isSelected) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrightNeonGreen)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(role.description, style = MaterialTheme.typography.bodySmall, color = PrimaryWhite)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                role.keyStrengths.forEach { str ->
                                                    Surface(
                                                        color = DeepStadiumGreen,
                                                        shape = RoundedCornerShape(4.dp),
                                                        border = BorderStroke(1.dp, CrispWhiteBorder)
                                                    ) {
                                                        Text(
                                                            text = "★ $str",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = SecondaryMintGray,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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

                2 -> {
                    // TAB 2: Physical Attributes
                    item {
                        Surface(
                            color = DeepStadiumGreen,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CrispWhiteBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("FİZİKSEL PROFİL VE ANATOMİ", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)

                                // Height Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Boy: ${draft.heightCm} cm", fontWeight = FontWeight.Bold, color = PrimaryWhite)
                                        Text("Min: 160 • Max: 202 cm", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                                    }
                                    Slider(
                                        value = draft.heightCm.toFloat(),
                                        onValueChange = { onUpdateDraft { d -> d.copy(heightCm = it.toInt()) } },
                                        valueRange = 160f..202f,
                                        steps = 41,
                                        colors = SliderDefaults.colors(
                                            thumbColor = VibrantFootballGreen,
                                            activeTrackColor = VibrantFootballGreen,
                                            inactiveTrackColor = DarkPitchSurface
                                        )
                                    )
                                }

                                // Weight Slider
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Kilo: ${draft.weightKg} kg", fontWeight = FontWeight.Bold, color = PrimaryWhite)
                                        Text("Min: 55 • Max: 98 kg", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                                    }
                                    Slider(
                                        value = draft.weightKg.toFloat(),
                                        onValueChange = { onUpdateDraft { d -> d.copy(weightKg = it.toInt()) } },
                                        valueRange = 55f..98f,
                                        steps = 42,
                                        colors = SliderDefaults.colors(
                                            thumbColor = ChampionshipGold,
                                            activeTrackColor = ChampionshipGold,
                                            inactiveTrackColor = DarkPitchSurface
                                        )
                                    )
                                }

                                // Athletic build analysis
                                val (buildTitle, buildDesc) = PlayerCreatorCatalog.getAthleticBuildLabel(draft.heightCm, draft.weightKg)
                                Surface(
                                    color = DarkPitchSurface,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, GoldBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = VibrantFootballGreen)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Fizik Tipi: $buildTitle",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = PrimaryWhite
                                            )
                                            Text(
                                                text = buildDesc,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SecondaryMintGray
                                            )
                                        }
                                    }
                                }

                                // Preferred Foot & Kit Number
                                Text("GÜÇLÜ AYAK SEÇİMİ", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    PreferredFoot.entries.forEach { foot ->
                                        val isSelected = draft.preferredFoot == foot
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { onUpdateDraft { it.copy(preferredFoot = foot) } },
                                            label = { Text("Ayak: ${foot.labelTr}") },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = VibrantFootballGreen,
                                                selectedLabelColor = DarkPitchSurface
                                            )
                                        )
                                    }
                                }

                                Text("FORMA NUMARASI SEÇİMİ", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(7, 8, 9, 10, 11, 14, 19, 21, 23, 53, 99).forEach { num ->
                                        val isSelected = draft.kitNumber == num
                                        Surface(
                                            color = if (isSelected) VibrantFootballGreen else DarkPitchSurface,
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, CrispWhiteBorder),
                                            modifier = Modifier.clickable { onUpdateDraft { it.copy(kitNumber = num) } }
                                        ) {
                                            Text(
                                                text = "#$num",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (isSelected) DarkPitchSurface else PrimaryWhite,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: Stat Points Allocation
                    item {
                        Surface(
                            color = DeepStadiumGreen,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CrispWhiteBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("YETENEK PUANI DAĞITIMI", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
                                        Text(
                                            text = "Kalan Dağıtılabilir Puan: ${remainingPoints.value}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (remainingPoints.value > 0) BrightNeonGreen else SecondaryMintGray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                // Auto allocate remaining points to key archetype strengths
                                                var rem = remainingPoints.value
                                                val stats = AllocatableStat.entries.shuffled()
                                                for (s in stats) {
                                                    if (rem <= 0) break
                                                    val curr = statAllocations[s] ?: 0
                                                    val add = (1..3).random().coerceAtMost(rem)
                                                    statAllocations[s] = (curr + add).coerceAtMost(12)
                                                    rem -= add
                                                }
                                            },
                                            border = BorderStroke(1.dp, VibrantFootballGreen),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantFootballGreen)
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Otomatik", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { statAllocations.clear() },
                                            border = BorderStroke(1.dp, CrispWhiteBorder),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryWhite)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Sıfırla", fontSize = 12.sp)
                                        }
                                    }
                                }

                                // Attribute steppers
                                AllocatableStat.entries.forEach { stat ->
                                    val currentVal = when (stat) {
                                        AllocatableStat.SHOOTING -> finalAttributes.shooting
                                        AllocatableStat.PASSING -> finalAttributes.passing
                                        AllocatableStat.DRIBBLING -> finalAttributes.dribbling
                                        AllocatableStat.FIRST_TOUCH -> finalAttributes.firstTouch
                                        AllocatableStat.PACE -> finalAttributes.pace
                                        AllocatableStat.AGILITY -> finalAttributes.agility
                                        AllocatableStat.STAMINA -> finalAttributes.stamina
                                        AllocatableStat.STRENGTH -> finalAttributes.strength
                                        AllocatableStat.DEFENDING -> finalAttributes.defending
                                        AllocatableStat.TACTICS -> finalAttributes.tactics
                                        AllocatableStat.VISION -> finalAttributes.vision
                                    }
                                    val allocated = statAllocations[stat] ?: 0

                                    Surface(
                                        color = DarkPitchSurface,
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, if (allocated > 0) VibrantFootballGreen.copy(alpha = 0.5f) else CrispWhiteBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = stat.titleTr,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = PrimaryWhite,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    if (allocated > 0) {
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "(+$allocated)",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = BrightNeonGreen,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = stat.description,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = SecondaryMintGray,
                                                    fontSize = 11.sp
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LinearProgressIndicator(
                                                    progress = { currentVal / 99f },
                                                    modifier = Modifier
                                                        .fillMaxWidth(0.9f)
                                                        .height(4.dp)
                                                        .clip(RoundedCornerShape(2.dp)),
                                                    color = if (currentVal >= 70) VibrantFootballGreen else ChampionshipGold,
                                                    trackColor = DeepStadiumGreen
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        if (allocated > 0) {
                                                            statAllocations[stat] = allocated - 1
                                                        }
                                                    },
                                                    enabled = allocated > 0,
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Remove, contentDescription = "Eksilt", tint = if (allocated > 0) PrimaryWhite else TextMuted)
                                                }

                                                Text(
                                                    text = currentVal.toString(),
                                                    fontFamily = TekoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 24.sp,
                                                    color = if (allocated > 0) BrightNeonGreen else PrimaryWhite,
                                                    modifier = Modifier.width(32.dp)
                                                )

                                                IconButton(
                                                    onClick = {
                                                        if (remainingPoints.value > 0 && currentVal < PlayerCreatorCatalog.MAX_STAT_CAP) {
                                                            statAllocations[stat] = allocated + 1
                                                        }
                                                    },
                                                    enabled = remainingPoints.value > 0 && currentVal < PlayerCreatorCatalog.MAX_STAT_CAP,
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = "Arttır", tint = if (remainingPoints.value > 0) VibrantFootballGreen else TextMuted)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // TAB 4: Visual Appearance & Kit
                    item {
                        Surface(
                            color = DeepStadiumGreen,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, CrispWhiteBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("GÖRÜNÜM VE EKİPMAN ÖZELLEŞTİRME", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)

                                Text("Ten Tonu", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    skinTones.forEach { hex ->
                                        val isSelected = draft.appearance.skinToneHex == hex
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
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

                                Text("Saç Rengi", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    hairColors.forEach { hex ->
                                        val isSelected = draft.appearance.hairColorHex == hex
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(parseHexColor(hex))
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
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
                                }

                                Text("Krampon Rengi", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    bootColors.forEach { hex ->
                                        val isSelected = draft.appearance.bootColorHex == hex
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(parseHexColor(hex))
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
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
                            }
                        }
                    }
                }
            }

            // Bottom Continue Button
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
                        text = "LİG VE KULÜP SEÇİMİNE DEVAM ET →",
                        fontFamily = TekoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}
