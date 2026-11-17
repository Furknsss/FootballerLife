package com.example.ui.screens.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.WorldDataCatalog
import com.example.engine.data.AmateurLeaguesData
import com.example.engine.data.SuperLigData
import com.example.engine.data.TFF1LigData
import com.example.engine.data.TFF2LigData
import com.example.engine.data.TFF3LigData
import com.example.engine.data.TurkishLeaguesDatabase
import com.example.model.Club
import com.example.ui.components.ClubCrestBadge
import com.example.ui.theme.AccentBright
import com.example.ui.theme.BrightGold
import com.example.ui.theme.BrightNeonGreen
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.CrispWhiteBorder
import com.example.ui.theme.CrispWhiteBorderStrong
import com.example.ui.theme.DarkPitchSurface
import com.example.ui.theme.DeepStadiumGreen
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.PrimaryWhite
import com.example.ui.theme.SecondaryMintGray
import com.example.ui.theme.TekoFontFamily
import com.example.ui.theme.VibrantFootballGreen

/**
 * Filter categories for Turkish pyramid and international leagues.
 */
enum class LeaguePyramidTier(val label: String, val tierNumber: Int?) {
    ALL("Tüm Ligler", null),
    SUPER_LIG("Süper Lig", 1),
    TFF_1_LIG("1. Lig", 2),
    TFF_2_LIG("2. Lig (Beyaz / Kırmızı)", 3),
    TFF_3_LIG("3. Lig (Gruplar)", 4),
    BAL("Bölgesel Amatör (BAL)", 5),
    AMATOR("Süper Amatör Küme", 6),
    EUROPE("Avrupa Ligleri", 99)
}

enum class SubGroupFilter(val titleTr: String, val specificLeagueId: String?) {
    ALL_GROUPS("Tümü", null),
    TFF_2_BEYAZ("Beyaz Grup", TFF2LigData.LEAGUE_ID_BEYAZ),
    TFF_2_KIRMIZI("Kırmızı Grup", TFF2LigData.LEAGUE_ID_KIRMIZI),
    TFF_3_G1("1. Grup (Bursa & Karşıyaka)", TFF3LigData.LEAGUE_ID_G1),
    TFF_3_G2("2. Grup (Balıkesir/Uşak)", TFF3LigData.LEAGUE_ID_G2),
    TFF_3_G3("3. Grup (Sinop/Ordu)", TFF3LigData.LEAGUE_ID_G3),
    TFF_3_G4("4. Grup (Sebat/Zonguldak)", TFF3LigData.LEAGUE_ID_G4)
}

enum class ClubSortOption(val titleTr: String) {
    STRENGTH_DESC("Kadro Gücü (Yüksekten Düşüğe)"),
    BUDGET_DESC("Bütçe (En Yüksek)"),
    FACILITIES_DESC("Tesis Kalitesi"),
    NAME_ASC("İsim (A-Z)"),
    CITY_ASC("Şehir (A-Z)")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TurkishLeagueClubPickerScreen(
    selectedLeagueId: String,
    onSelectLeague: (String) -> Unit,
    onSignWithClub: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedPyramidTier by remember { mutableStateOf(LeaguePyramidTier.ALL) }
    var selectedSubGroup by remember { mutableStateOf<SubGroupFilter?>(null) }
    var selectedSortOption by remember { mutableStateOf(ClubSortOption.STRENGTH_DESC) }
    var expandedClubId by remember { mutableStateOf<String?>(null) }
    var signingConfirmationClub by remember { mutableStateOf<Club?>(null) }

    // Aggregate clubs: full Turkish Pyramid from TurkishLeaguesDatabase + European clubs from WorldDataCatalog
    val allClubs = remember {
        val turkish = TurkishLeaguesDatabase.getAllTurkishClubs()
        val european = WorldDataCatalog.leagues
            .filter { it.country != "Türkiye" }
            .flatMap { WorldDataCatalog.clubsForLeague(it.id) }
        (turkish + european).distinctBy { it.id }
    }

    // Filter and sort clubs
    val filteredClubs = remember(allClubs, searchQuery, selectedPyramidTier, selectedSubGroup, selectedSortOption) {
        var list = allClubs.asSequence()

        // 1. Pyramid Tier filtering
        list = when (selectedPyramidTier) {
            LeaguePyramidTier.ALL -> list
            LeaguePyramidTier.SUPER_LIG -> list.filter { it.tier == 1 && it.country == "Türkiye" }
            LeaguePyramidTier.TFF_1_LIG -> list.filter { it.tier == 2 && it.country == "Türkiye" }
            LeaguePyramidTier.TFF_2_LIG -> {
                if (selectedSubGroup?.specificLeagueId != null) {
                    list.filter { it.leagueId == selectedSubGroup?.specificLeagueId }
                } else {
                    list.filter { it.tier == 3 && it.country == "Türkiye" }
                }
            }
            LeaguePyramidTier.TFF_3_LIG -> {
                if (selectedSubGroup?.specificLeagueId != null) {
                    list.filter { it.leagueId == selectedSubGroup?.specificLeagueId }
                } else {
                    list.filter { it.tier == 4 && it.country == "Türkiye" }
                }
            }
            LeaguePyramidTier.BAL -> list.filter { it.tier == 5 && it.country == "Türkiye" }
            LeaguePyramidTier.AMATOR -> list.filter { it.tier == 6 && it.country == "Türkiye" }
            LeaguePyramidTier.EUROPE -> list.filter { it.country != "Türkiye" }
        }

        // 2. Search query filtering
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter { club ->
                club.name.lowercase().contains(q) ||
                    club.shortName.lowercase().contains(q) ||
                    club.city.lowercase().contains(q) ||
                    club.stadiumName.lowercase().contains(q) ||
                    club.managerName.lowercase().contains(q) ||
                    club.leagueName.lowercase().contains(q)
            }
        }

        // 3. Sorting
        val sorted = when (selectedSortOption) {
            ClubSortOption.STRENGTH_DESC -> list.sortedByDescending { it.teamStrength }
            ClubSortOption.BUDGET_DESC -> list.sortedByDescending { it.budgetEur }
            ClubSortOption.FACILITIES_DESC -> list.sortedByDescending { it.facilityQuality }
            ClubSortOption.NAME_ASC -> list.sortedBy { it.name }
            ClubSortOption.CITY_ASC -> list.sortedBy { it.city }
        }

        sorted.toList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPitchSurface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                        text = "TÜRKİYE LİGLERİ & KULÜP SEÇİMİ",
                        fontFamily = TekoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = PrimaryWhite
                    )
                    Text(
                        text = "Süper Lig'den Bölgesel Amatör Lig ve Süper Amatör'e Gerçekçi Piramit",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryMintGray
                    )
                }
            }

            Surface(
                color = DeepStadiumGreen,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, GoldBorder)
            ) {
                Text(
                    text = "${filteredClubs.size} Kulüp",
                    style = MaterialTheme.typography.labelSmall,
                    color = BrightGold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = VibrantFootballGreen)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Temizle", tint = SecondaryMintGray)
                    }
                }
            },
            placeholder = { Text("Kulüp, şehir, stadyum veya teknik direktör ara...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_search_club")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Pyramid Tier Horizontal Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LeaguePyramidTier.entries.forEach { tier ->
                val isSelected = selectedPyramidTier == tier
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedPyramidTier = tier
                        selectedSubGroup = null
                    },
                    label = {
                        Text(
                            text = tier.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VibrantFootballGreen,
                        selectedLabelColor = DarkPitchSurface
                    ),
                    modifier = Modifier.testTag("chip_tier_${tier.name}")
                )
            }
        }

        // Optional Sub-Group Selector when 2. Lig or 3. Lig is selected
        if (selectedPyramidTier == LeaguePyramidTier.TFF_2_LIG) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    SubGroupFilter.ALL_GROUPS,
                    SubGroupFilter.TFF_2_BEYAZ,
                    SubGroupFilter.TFF_2_KIRMIZI
                ).forEach { sg ->
                    val isSgSelected = (selectedSubGroup == sg) || (selectedSubGroup == null && sg == SubGroupFilter.ALL_GROUPS)
                    FilterChip(
                        selected = isSgSelected,
                        onClick = { selectedSubGroup = if (sg == SubGroupFilter.ALL_GROUPS) null else sg },
                        label = { Text(sg.titleTr, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChampionshipGold,
                            selectedLabelColor = DarkPitchSurface
                        )
                    )
                }
            }
        } else if (selectedPyramidTier == LeaguePyramidTier.TFF_3_LIG) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    SubGroupFilter.ALL_GROUPS,
                    SubGroupFilter.TFF_3_G1,
                    SubGroupFilter.TFF_3_G2,
                    SubGroupFilter.TFF_3_G3,
                    SubGroupFilter.TFF_3_G4
                ).forEach { sg ->
                    val isSgSelected = (selectedSubGroup == sg) || (selectedSubGroup == null && sg == SubGroupFilter.ALL_GROUPS)
                    FilterChip(
                        selected = isSgSelected,
                        onClick = { selectedSubGroup = if (sg == SubGroupFilter.ALL_GROUPS) null else sg },
                        label = { Text(sg.titleTr, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChampionshipGold,
                            selectedLabelColor = DarkPitchSurface
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sort Options Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sırala:", style = MaterialTheme.typography.labelSmall, color = SecondaryMintGray)
            ClubSortOption.entries.forEach { opt ->
                val isSelected = selectedSortOption == opt
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedSortOption = opt },
                    label = { Text(opt.titleTr, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = VibrantFootballGreen,
                        selectedLabelColor = DarkPitchSurface
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Club List
        if (filteredClubs.isEmpty()) {
            Surface(
                color = DeepStadiumGreen,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CrispWhiteBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "\"$searchQuery\" aramasına uygun kulüp bulunamadı.",
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Filtreleri veya arama kriterlerini sıfırlayarak tüm Türkiye liglerini inceleyebilirsiniz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryMintGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            searchQuery = ""
                            selectedPyramidTier = LeaguePyramidTier.ALL
                            selectedSubGroup = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantFootballGreen, contentColor = DarkPitchSurface)
                    ) {
                        Text("Filtreleri Temizle")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredClubs, key = { it.id }) { club ->
                    val isExpanded = expandedClubId == club.id
                    RichClubCard(
                        club = club,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedClubId = if (isExpanded) null else club.id
                        },
                        onInitiateSign = {
                            signingConfirmationClub = club
                        }
                    )
                }
            }
        }
    }

    // Signing Confirmation Dialog
    signingConfirmationClub?.let { club ->
        AlertDialog(
            onDismissRequest = { signingConfirmationClub = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ClubCrestBadge(
                        shortName = club.shortName,
                        primaryHex = club.primaryColorHex,
                        secondaryHex = club.secondaryColorHex,
                        size = 36.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Sözleşme İmza Onayı", color = PrimaryWhite, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${club.name} ile resmi sözleşme imzalamak üzeresiniz.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryWhite
                    )
                    Surface(
                        color = DarkPitchSurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• Lig: ${club.leagueName} (Kademe ${club.tier})", style = MaterialTheme.typography.bodySmall, color = SecondaryMintGray)
                            Text("• Sezon Hedefi: ${club.seasonObjective}", style = MaterialTheme.typography.bodySmall, color = VibrantFootballGreen)
                            Text("• Stadyum: ${club.stadiumName} (${club.stadiumCapacity} kişi)", style = MaterialTheme.typography.bodySmall, color = SecondaryMintGray)
                            Text("• Tesis Kalitesi: ${"★".repeat(club.facilityQuality.coerceIn(1, 5))}", style = MaterialTheme.typography.bodySmall, color = BrightGold)
                            Text("• Teknik Direktör: ${club.managerName} (${club.managerTacticalStyle})", style = MaterialTheme.typography.bodySmall, color = SecondaryMintGray)
                        }
                    }
                    Text(
                        text = "İmza sonrasında kariyeriniz bu kulübün antrenman tesislerinde başlayacaktır.",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryMintGray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetClub = club
                        signingConfirmationClub = null
                        onSelectLeague(targetClub.leagueId)
                        onSignWithClub(targetClub.leagueId, targetClub.id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VibrantFootballGreen,
                        contentColor = DarkPitchSurface
                    ),
                    modifier = Modifier.testTag("btn_confirm_sign_${club.id}")
                ) {
                    Text("RESMİ İMZAYI AT ✍️", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { signingConfirmationClub = null }) {
                    Text("Vazgeç", color = SecondaryMintGray)
                }
            },
            containerColor = DeepStadiumGreen,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun RichClubCard(
    club: Club,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onInitiateSign: () -> Unit
) {
    val tierLabel = when (club.tier) {
        1 -> "1. KADEME • SÜPER LİG"
        2 -> "2. KADEME • 1. LİG"
        3 -> "3. KADEME • 2. LİG"
        4 -> "4. KADEME • 3. LİG"
        5 -> "5. KADEME • BAL LİGİ"
        6 -> "6. KADEME • AMATÖR"
        else -> "KADEME ${club.tier}"
    }

    val tierBadgeColor = when (club.tier) {
        1 -> ChampionshipGold
        2 -> VibrantFootballGreen
        3 -> BrightGold
        4 -> SecondaryMintGray
        5 -> AccentBright
        else -> SecondaryMintGray
    }

    Surface(
        color = DeepStadiumGreen,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, if (isExpanded) VibrantFootballGreen else CrispWhiteBorderStrong),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_club_${club.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Main Top Row
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
                        size = 54.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = club.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryWhite,
                                fontSize = 17.sp
                            )
                            if (club.isDemoClub) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = DarkPitchSurface,
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(1.dp, GoldBorder)
                                ) {
                                    Text(
                                        text = "AKADEMİ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ChampionshipGold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = tierBadgeColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = tierLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tierBadgeColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• Kur: ${club.foundationYear}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryMintGray
                            )
                        }

                        Text(
                            text = "${club.city} • ${club.stadiumName} (${club.stadiumCapacity} kişi)",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryMintGray,
                            fontSize = 12.sp
                        )
                    }
                }

                // Team Strength Badge
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
                            fontSize = 26.sp,
                            color = if (club.teamStrength >= 75) BrightGold else PrimaryWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Club Spec Pills Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Season Objective
                Surface(
                    color = DarkPitchSurface,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, CrispWhiteBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Hedef: ${club.seasonObjective}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryWhite
                        )
                    }
                }

                // Facilities
                Surface(
                    color = DarkPitchSurface,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, CrispWhiteBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Apartment, contentDescription = null, tint = VibrantFootballGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Tesis: ${"★".repeat(club.facilityQuality.coerceIn(1, 5))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = BrightGold
                        )
                    }
                }

                // Transfer Budget
                Surface(
                    color = DarkPitchSurface,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, CrispWhiteBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Euro, contentDescription = null, tint = VibrantFootballGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Bütçe: ${formatEuro(club.budgetEur)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryWhite
                        )
                    }
                }

                // Manager
                Surface(
                    color = DarkPitchSurface,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, CrispWhiteBorder)
                ) {
                    Text(
                        text = "TD: ${club.managerName} (${club.managerTacticalStyle})",
                        style = MaterialTheme.typography.labelSmall,
                        color = SecondaryMintGray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Expandable Squad Depth Section
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = CrispWhiteBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "KADRO DERİNLİĞİ & ÖNE ÇIKAN OYUNCULAR (${club.keySquadMembers.size})",
                            style = MaterialTheme.typography.labelSmall,
                            color = ChampionshipGold,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (club.keySquadMembers.isEmpty()) {
                        Text(
                            text = "Alt lig kadro derinliği antrenman hazırlık kampında simüle edilmektedir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryMintGray
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            club.keySquadMembers.take(8).forEach { member ->
                                Surface(
                                    color = DarkPitchSurface,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, CrispWhiteBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = DeepStadiumGreen,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = member.position.code,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = VibrantFootballGreen,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "#${member.kitNumber} ${member.name}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = PrimaryWhite,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            if (member.isCaptain) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = ChampionshipGold,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "K",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = DarkPitchSurface,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${member.age} Yaş",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SecondaryMintGray
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = member.overall.toString(),
                                                fontFamily = TekoFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 20.sp,
                                                color = if (member.overall >= 80) BrightGold else PrimaryWhite
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onToggleExpand,
                    border = BorderStroke(1.dp, CrispWhiteBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryWhite),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isExpanded) "Detayları Gizle" else "Kadro Derinliği (${club.keySquadMembers.size})",
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onInitiateSign,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VibrantFootballGreen,
                        contentColor = DarkPitchSurface
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("btn_sign_club_${club.id}")
                ) {
                    Icon(Icons.Default.SportsSoccer, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SÖZLEŞME İMZALA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

private fun formatEuro(amount: Long): String {
    return when {
        amount >= 1_000_000 -> "${amount / 1_000_000}M €"
        amount >= 1_000 -> "${amount / 1_000}K €"
        else -> "$amount €"
    }
}
