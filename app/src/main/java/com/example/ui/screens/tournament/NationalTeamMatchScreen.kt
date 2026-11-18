package com.example.ui.screens.tournament

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.GameSoundEffect
import com.example.engine.tournament.CallUpStatus
import com.example.engine.tournament.CampDialogueItem
import com.example.engine.tournament.CupAndNationalTeamEngine
import com.example.engine.tournament.NationalMatchPerformance
import com.example.engine.tournament.NationalTeamCallUp
import com.example.model.CareerSaveState
import com.example.ui.components.FLBadge
import com.example.ui.components.FLCard
import com.example.ui.components.FLHeroPanel
import com.example.ui.components.FLPrimaryButton
import com.example.ui.components.FLSectionHeader
import com.example.ui.components.FLTopBar
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StrokePremium
import com.example.ui.theme.StrokePremiumStrong
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceGlass
import com.example.ui.theme.SurfacePrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val TurkishRed = Color(0xFFC0392B)
private val TurkishRedDark = Color(0xFF922B21)
private val TurkishWhite = Color(0xFFFDFEFE)
private val GoldHighlight = Color(0xFFF1C40F)

enum class NationalStage {
    OFFICIAL_INVITATION,
    RIVA_CAMP,
    ANTHEM_CEREMONY,
    MATCH_ACTION,
    POST_MATCH_GLORY
}

/**
 * A Millî Takım Davet, Riva Kampı, İstiklal Marşı Seremonisi ve Maç Ekranı
 */
@Composable
fun NationalTeamMatchScreen(
    career: CareerSaveState,
    onApplyPerformance: (NationalMatchPerformance, NationalTeamCallUp) -> Unit,
    onPlaySfx: (GameSoundEffect) -> Unit,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var currentStage by remember { mutableStateOf(NationalStage.OFFICIAL_INVITATION) }

    // Davet verisini oluştur veya mevcut olanı çek
    val initialCallUp = remember(career.player.id, career.currentDayIndex) {
        CupAndNationalTeamEngine.evaluateNationalTeamCallUp(career.player, career.currentDayIndex)
            ?: CupAndNationalTeamEngine.evaluateNationalTeamCallUp(
                career.player.copy(form = 90),
                career.currentDayIndex
            )!!
    }

    var callUp by remember { mutableStateOf(initialCallUp) }
    var selectedKitNumber by remember { mutableIntStateOf(callUp.assignedKitNumber) }
    var selectedTactics by remember { mutableStateOf("Hücum ve Yüksek Baskı") }
    var matchPerformance by remember { mutableStateOf<NationalMatchPerformance?>(null) }

    // Maç içi canlı dakika ve olay simülasyonu
    var simulatedMinute by remember { mutableIntStateOf(0) }
    var isSimulatingMatch by remember { mutableStateOf(false) }
    val matchLiveEvents = remember { mutableListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        TurkishRedDark,
                        BackgroundPrimary,
                        BackgroundSecondary
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Üst Bar
        FLTopBar(
            title = "A MİLLÎ TAKIM",
            subtitle = "Türkiye Futbol Federasyonu · Bizim Çocuklar 🇹🇷",
            onBack = {
                onPlaySfx(GameSoundEffect.MENU_TAP)
                onClose()
            },
            trailingContent = {
                FLBadge(
                    text = "A Millî",
                    containerColor = TurkishRed,
                    contentColor = TurkishWhite
                )
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Aşama İlerleme Göstergesi
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val stages = listOf(
                Pair(NationalStage.OFFICIAL_INVITATION, "1. Davet"),
                Pair(NationalStage.RIVA_CAMP, "2. Riva Kampı"),
                Pair(NationalStage.ANTHEM_CEREMONY, "3. İstiklal Marşı"),
                Pair(NationalStage.MATCH_ACTION, "4. Millî Maç"),
                Pair(NationalStage.POST_MATCH_GLORY, "5. Zafer")
            )

            stages.forEach { (stage, label) ->
                val isActive = currentStage == stage
                val isDone = currentStage.ordinal > stage.ordinal

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isActive -> GoldHighlight
                                    isDone -> TurkishRed
                                    else -> SurfaceElevated
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isDone) "✓" else "${stage.ordinal + 1}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isActive) BackgroundPrimary else TextPrimary
                        )
                    }
                    Text(
                        text = label,
                        fontFamily = OutfitFontFamily,
                        fontSize = 9.sp,
                        color = if (isActive) GoldHighlight else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Dinamik Aşama İçeriği
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentStage) {
                NationalStage.OFFICIAL_INVITATION -> {
                    OfficialInvitationView(
                        callUp = callUp,
                        onAccept = {
                            onPlaySfx(GameSoundEffect.PHONE_MESSAGE)
                            callUp = callUp.copy(isAccepted = true, status = CallUpStatus.IN_CAMP)
                            currentStage = NationalStage.RIVA_CAMP
                        }
                    )
                }

                NationalStage.RIVA_CAMP -> {
                    RivaCampView(
                        callUp = callUp,
                        userPlayer = career.player,
                        selectedKitNumber = selectedKitNumber,
                        onSelectKitNumber = { num ->
                            selectedKitNumber = num
                            onPlaySfx(GameSoundEffect.MENU_TAP)
                            callUp = callUp.copy(assignedKitNumber = num)
                        },
                        selectedTactics = selectedTactics,
                        onSelectTactics = { t ->
                            selectedTactics = t
                            onPlaySfx(GameSoundEffect.MENU_TAP)
                        },
                        onProceedToAnthem = {
                            onPlaySfx(GameSoundEffect.WHISTLE_KICKOFF)
                            currentStage = NationalStage.ANTHEM_CEREMONY
                        }
                    )
                }

                NationalStage.ANTHEM_CEREMONY -> {
                    AnthemCeremonyView(
                        callUp = callUp,
                        userKitNumber = selectedKitNumber,
                        onStartMatch = {
                            onPlaySfx(GameSoundEffect.WHISTLE_KICKOFF)
                            currentStage = NationalStage.MATCH_ACTION
                            isSimulatingMatch = true
                            simulatedMinute = 0
                            matchLiveEvents.clear()

                            // Maç simülasyonunu başlat
                            coroutineScope.launch {
                                matchLiveEvents.add("0' Hakem düdüğünü çaldı! Maç başladı!")
                                delay(600)
                                simulatedMinute = 15
                                matchLiveEvents.add("15' Hakan Çalhanoğlu ceza sahası yayından vurdu, kaleci güçlükle çeldi!")
                                delay(650)
                                simulatedMinute = 38
                                matchLiveEvents.add("38' Arda Güler çalımlarla içeri girdi, pası ${career.player.fullName} ile buluştu!")
                                delay(700)
                                simulatedMinute = 54
                                matchLiveEvents.add("54' Rakip baskısını kırdık! Kenan Yıldız sol kanattan bindirdi.")
                                delay(750)
                                simulatedMinute = 72
                                onPlaySfx(GameSoundEffect.GOAL_CELEBRATION)
                                matchLiveEvents.add("72' GOOOOOL! GOOOL! GOOOL! ${career.player.fullName} muhteşem bir şutla fileleri havalandırdı!")
                                delay(700)
                                simulatedMinute = 90
                                matchLiveEvents.add("90+3' Hakem son düdüğü çalıyor! Türkiye sahadan gururla ayrılıyor!")
                                onPlaySfx(GameSoundEffect.WHISTLE_FULLTIME)
                                isSimulatingMatch = false

                                val (perf, updatedFixture) = CupAndNationalTeamEngine.resolveNationalMatch(
                                    callUp = callUp,
                                    userPlayer = career.player,
                                    userSelectedTactics = selectedTactics
                                )
                                matchPerformance = perf
                                callUp = callUp.copy(nextMatch = updatedFixture, status = CallUpStatus.COMPLETED)
                                currentStage = NationalStage.POST_MATCH_GLORY
                            }
                        }
                    )
                }

                NationalStage.MATCH_ACTION -> {
                    MatchActionView(
                        callUp = callUp,
                        simulatedMinute = simulatedMinute,
                        isSimulating = isSimulatingMatch,
                        liveEvents = matchLiveEvents
                    )
                }

                NationalStage.POST_MATCH_GLORY -> {
                    matchPerformance?.let { perf ->
                        PostMatchGloryView(
                            performance = perf,
                            callUp = callUp,
                            onFinish = {
                                onPlaySfx(GameSoundEffect.TRANSFER_OFFER)
                                onApplyPerformance(perf, callUp)
                                onClose()
                            }
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 1. RESMİ DAVET MEKTUBU GÖRÜNÜMÜ
// =========================================================================

@Composable
private fun OfficialInvitationView(
    callUp: NationalTeamCallUp,
    onAccept: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        FLHeroPanel(
            borderColor = GoldHighlight
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = TurkishRed,
                    shape = CircleShape,
                    modifier = Modifier.size(54.dp),
                    border = BorderStroke(2.dp, GoldHighlight)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_milli_takim),
                            contentDescription = "A Millî Takım",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "TFF RESMİ TEBLİGATI",
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = GoldHighlight
                    )
                    Text(
                        text = "A Millî Futbol Takımı Aday Kadro Çağrısı",
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Antetli Mektup Alanı
            Surface(
                color = SurfaceElevated.copy(alpha = 0.9f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GoldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = callUp.invitationTitle,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TurkishWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = callUp.invitationBody,
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Kamp Yeri:",
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Riva Hasan Doğan Tesisleri",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AccentBright
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Hedef Karşılaşma:",
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = callUp.nextMatch.fixtureTitle,
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TurkishRed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            FLPrimaryButton(
                text = "DAVETİ GURURLA KABUL ET VE KAMPA KATIL 🇹🇷",
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// =========================================================================
// 2. RİVA KAMPI & DİYALOGLAR & FORMA SEÇİMİ GÖRÜNÜMÜ
// =========================================================================

@Composable
private fun RivaCampView(
    callUp: NationalTeamCallUp,
    userPlayer: com.example.model.PlayerProfile,
    selectedKitNumber: Int,
    onSelectKitNumber: (Int) -> Unit,
    selectedTactics: String,
    onSelectTactics: (String) -> Unit,
    onProceedToAnthem: () -> Unit
) {
    val scrollState = rememberScrollState()
    val availableNumbers = listOf(7, 8, 9, 10, 11, 14, 19, 20, 21, 23, 26)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        FLSectionHeader(
            title = "RİVA HASAN DOĞAN KAMPI",
            subtitle = "Teknik Direktör & Takım Arkadaşlarıyla Buluşma"
        )

        // Karakter Diyalogları
        callUp.campDialogues.forEach { dialog ->
            FLCard(modifier = Modifier.padding(vertical = 4.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(TurkishRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dialog.speakerName.take(1),
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = TurkishWhite
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = dialog.speakerName,
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = GoldHighlight
                            )
                            Text(
                                text = dialog.speakerRole,
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "\"${dialog.quoteText}\"",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = SurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Taktik",
                                tint = AccentBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = dialog.tacticalAdvice,
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                color = AccentBright
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Ay-Yıldızlı Forma Numarası Seçimi
        FLSectionHeader(
            title = "AY-YILDIZLI FORMA NUMARASI SEÇİMİ",
            subtitle = "Millî maçta sırtında taşımak istediğin numara"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableNumbers.forEach { num ->
                val isSelected = selectedKitNumber == num
                Surface(
                    color = if (isSelected) TurkishRed else SurfacePrimary,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isSelected) GoldHighlight else StrokePremium),
                    modifier = Modifier
                        .size(52.dp)
                        .clickable { onSelectKitNumber(num) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$num",
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = if (isSelected) TurkishWhite else TextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Taktik Yaklaşım Seçimi
        FLSectionHeader(
            title = "MİLLÎ MAÇ TAKTİK ROLÜ",
            subtitle = "Teknik heyetin belirlediği oyun planı"
        )

        val tacticalOptions = listOf(
            "Hücum ve Yüksek Baskı",
            "Dengeli ve Hızlı Geçişler",
            "Oyun Kurucu Serbest Rol"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tacticalOptions.forEach { option ->
                val isSelected = selectedTactics == option
                Surface(
                    color = if (isSelected) TurkishRedDark else SurfacePrimary,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isSelected) GoldHighlight else StrokePremium),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectTactics(option) }
                ) {
                    Text(
                        text = option,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (isSelected) TurkishWhite else TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        FLPrimaryButton(
            text = "SEREMONİYE VE MAÇA ÇIK 🇹🇷",
            onClick = onProceedToAnthem,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// =========================================================================
// 3. İSTİKLAL MARŞI SEREMONİSİ GÖRÜNÜMÜ
// =========================================================================

@Composable
private fun AnthemCeremonyView(
    callUp: NationalTeamCallUp,
    userKitNumber: Int,
    onStartMatch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FLHeroPanel(borderColor = TurkishRed) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🇹🇷 İSTİKLAL MARŞI SEREMONİSİ 🇹🇷",
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = GoldHighlight,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Atatürk Olimpiyat Stadyumu · 75.000 Tek Yürek",
                    fontFamily = OutfitFontFamily,
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Omuz Omuza Dizilen Takım Görseli
                Surface(
                    color = TurkishRedDark,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(2.dp, GoldHighlight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "🇹🇷 🇹🇷 🇹🇷 🇹🇷 🇹🇷",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Korkma, sönmez bu şafaklarda yüzen al sancak!\nSönmeden yurdumun üstünde tüten en son ocak.",
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TurkishWhite,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Oyuncu Ay-Yıldız Forma Kartı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = TurkishRed,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Forma No: ",
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                color = TurkishWhite
                            )
                            Text(
                                text = "#$userKitNumber",
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = GoldHighlight
                            )
                        }
                    }

                    Text(
                        text = callUp.nextMatch.fixtureTitle,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                FLPrimaryButton(
                    text = "DÜDÜK ÇALDI! MAÇI BAŞLAT ⚽",
                    onClick = onStartMatch,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// =========================================================================
// 4. CANLI MAÇ AKSİYONU GÖRÜNÜMÜ
// =========================================================================

@Composable
private fun MatchActionView(
    callUp: NationalTeamCallUp,
    simulatedMinute: Int,
    isSimulating: Boolean,
    liveEvents: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        FLHeroPanel(borderColor = TurkishRed) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CANLI MAÇ SİMÜLASYONU",
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = GoldHighlight
                )

                Text(
                    text = "$simulatedMinute'",
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = if (isSimulating) TurkishRed else AccentBright
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (simulatedMinute / 90f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = TurkishRed,
                trackColor = SurfaceElevated
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Canlı Skor Tabelası
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, StrokePremiumStrong),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🇹🇷", fontSize = 32.sp)
                        Text(
                            text = "TÜRKİYE",
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = if (simulatedMinute >= 72) "1 - 0" else "0 - 0",
                        fontFamily = BarlowCondensedFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = GoldHighlight
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = callUp.nextMatch.opponentFlagEmoji, fontSize = 32.sp)
                        Text(
                            text = callUp.nextMatch.opponentCountry.uppercase(),
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dakika Dakika Olay Akışı
            FLSectionHeader(title = "MAÇ AKIŞI VE DAKİKALAR")

            liveEvents.reversed().forEach { event ->
                val isGoal = event.contains("GOOOOOL")
                Surface(
                    color = if (isGoal) TurkishRedDark else SurfacePrimary,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isGoal) GoldHighlight else StrokePremium),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Text(
                        text = event,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = if (isGoal) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        color = if (isGoal) GoldHighlight else TextPrimary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// 5. MAÇ SONU ZAFER & MANŞETLER GÖRÜNÜMÜ
// =========================================================================

@Composable
private fun PostMatchGloryView(
    performance: NationalMatchPerformance,
    callUp: NationalTeamCallUp,
    onFinish: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        FLHeroPanel(borderColor = GoldHighlight) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🇹🇷 AY-YILDIZLI ZAFER 🇹🇷",
                    fontFamily = BarlowCondensedFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = GoldHighlight,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = performance.matchResultTitle,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TurkishWhite,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Skor ve İstatistikler
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GoldBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = performance.finalScore,
                            fontFamily = BarlowCondensedFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = TurkishWhite
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "GOL", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = "${performance.goals}",
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = AccentBright
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "ASİST", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = "${performance.assists}",
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = AccentBright
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "PUAN", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = "${performance.rating}",
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = GoldHighlight
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "MİLLİ PRİM", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = "+€${performance.matchBonusEur}",
                                    fontFamily = BarlowCondensedFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = TurkishWhite
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Gazete Manşeti
                Surface(
                    color = TurkishRedDark,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GoldHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "GAZETE MANŞETLERİ",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = GoldHighlight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = performance.pressHeadlineTr,
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TurkishWhite
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = performance.coachPraiseTr,
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                FLPrimaryButton(
                    text = "GURURLA KARİYERE DEVAM ET 🇹🇷",
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
