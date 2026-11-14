package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.example.data.CareerSaveSummary
import com.example.data.UserSettings
import com.example.model.GraphicsQuality
import com.example.ui.theme.BrightGold
import com.example.ui.theme.BrightNeonGreen
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.CrispWhiteBorder
import com.example.ui.theme.CrispWhiteBorderStrong
import com.example.ui.theme.DarkPitchSurface
import com.example.ui.theme.DarkTurfGreen
import com.example.ui.theme.DeepStadiumGreen
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.MatchLossRed
import com.example.ui.theme.PrimaryWhite
import com.example.ui.theme.SecondaryMintGray
import com.example.ui.theme.TekoFontFamily
import com.example.ui.theme.VibrantFootballGreen
import kotlinx.coroutines.delay

@Composable
fun IntroSplashScreen(
    skipAutomatically: Boolean,
    onIntroFinished: () -> Unit
) {
    val logoAlpha = remember { Animatable(0f) }
    val floodlightAlpha = remember { Animatable(0f) }
    val stadiumAlpha = remember { Animatable(0f) }

    LaunchedEffect(skipAutomatically) {
        if (skipAutomatically) {
            onIntroFinished()
            return@LaunchedEffect
        }
        logoAlpha.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
        floodlightAlpha.animateTo(1f, tween(520, easing = FastOutSlowInEasing))
        stadiumAlpha.animateTo(1f, tween(550, easing = FastOutSlowInEasing))
        delay(450L)
        onIntroFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onIntroFinished() }
            .testTag("intro_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Stadium image emerging
        Image(
            painter = painterResource(id = R.drawable.img_stadium_hero),
            contentDescription = "Stadyum Atmosferi",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(stadiumAlpha.value * 0.65f)
        )

        // Stadium Floodlight Beams
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(floodlightAlpha.value)
        ) {
            val w = size.width
            val h = size.height
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PrimaryWhite.copy(alpha = 0.42f),
                        VibrantFootballGreen.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.18f, h * 0.12f),
                    radius = w * 0.75f
                ),
                center = Offset(w * 0.18f, h * 0.12f),
                radius = w * 0.75f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ChampionshipGold.copy(alpha = 0.35f),
                        VibrantFootballGreen.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.82f, h * 0.12f),
                    radius = w * 0.75f
                ),
                center = Offset(w * 0.82f, h * 0.12f),
                radius = w * 0.75f
            )
        }

        // Dark gradient vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.55f),
                            DeepStadiumGreen.copy(alpha = 0.65f),
                            DarkPitchSurface
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .padding(32.dp)
                .alpha(logoAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = DeepStadiumGreen,
                border = BorderStroke(2.dp, ChampionshipGold),
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = "Football Life",
                        tint = BrightGold,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "FOOTBALL LIFE",
                fontFamily = TekoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 62.sp,
                letterSpacing = 3.sp,
                color = PrimaryWhite
            )
            Text(
                text = "YOUR CAREER. YOUR LIFE. YOUR LEGACY.",
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = 2.2.sp,
                color = ChampionshipGold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Geçmek için ekrana dokunun",
                style = MaterialTheme.typography.bodySmall,
                color = SecondaryMintGray
            )
        }
    }
}

@Composable
fun MainMenuScreen(
    saveSummaries: List<CareerSaveSummary>,
    settings: UserSettings,
    onStartNewCareer: () -> Unit,
    onContinueCareer: () -> Unit,
    onOpenSaveSlots: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val hasSaves = saveSummaries.isNotEmpty()
    val mostRecent = saveSummaries.maxByOrNull { it.updatedAtMs }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPitchSurface)
            .testTag("main_menu_screen")
    ) {
        // Background Stadium Hero
        Image(
            painter = painterResource(id = R.drawable.img_stadium_hero),
            contentDescription = "Stadyum Arka Planı",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.48f)
        )

        // Cinematic Emerald & Pitch Vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            DarkPitchSurface.copy(alpha = 0.55f),
                            DeepStadiumGreen.copy(alpha = 0.82f),
                            DarkPitchSurface
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top bar: Account / Offline Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = DeepStadiumGreen.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, CrispWhiteBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Hesap",
                            tint = VibrantFootballGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = settings.accountDisplayName,
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryWhite
                        )
                    }
                }

                Surface(
                    color = DarkTurfGreen.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, GoldBorder)
                ) {
                    Text(
                        text = "2026/27 SEZONU",
                        style = MaterialTheme.typography.labelMedium,
                        color = BrightGold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Center Hero Identity
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = DeepStadiumGreen.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(2.dp, ChampionshipGold),
                    modifier = Modifier.size(84.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.SportsSoccer,
                            contentDescription = "Football Life",
                            tint = BrightNeonGreen,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "FOOTBALL LIFE",
                    fontFamily = TekoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 64.sp,
                    lineHeight = 60.sp,
                    letterSpacing = 2.5.sp,
                    color = PrimaryWhite
                )
                Text(
                    text = "YOUR CAREER. YOUR LIFE. YOUR LEGACY.",
                    style = MaterialTheme.typography.labelLarge,
                    letterSpacing = 2.sp,
                    color = ChampionshipGold,
                    textAlign = TextAlign.Center
                )

                if (mostRecent != null) {
                    Spacer(modifier = Modifier.height(18.dp))
                    Surface(
                        color = DeepStadiumGreen.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, CrispWhiteBorderStrong),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "SON AKTİF KARİYER (YUVA ${mostRecent.slotId})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VibrantFootballGreen
                                )
                                Text(
                                    text = "${mostRecent.playerName} • ${mostRecent.clubName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PrimaryWhite
                                )
                                Text(
                                    text = "${mostRecent.leagueName} • ${mostRecent.dateLabel}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryMintGray
                                )
                            }
                            Surface(
                                color = DarkTurfGreen,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, GoldBorder)
                            ) {
                                Text(
                                    text = "OVR ${mostRecent.overallRating}",
                                    fontFamily = TekoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = BrightGold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onStartNewCareer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VibrantFootballGreen,
                        contentColor = DarkPitchSurface
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_start_career")
                ) {
                    Icon(Icons.Default.AddCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "KARİYERE BAŞLA",
                        fontFamily = TekoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        letterSpacing = 1.sp
                    )
                }

                Button(
                    onClick = onContinueCareer,
                    enabled = hasSaves,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkTurfGreen,
                        contentColor = PrimaryWhite,
                        disabledContainerColor = DeepStadiumGreen.copy(alpha = 0.45f),
                        disabledContentColor = SecondaryMintGray.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, if (hasSaves) ChampionshipGold else CrispWhiteBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_continue_career")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (hasSaves) "KARİYERE DEVAM ET" else "KARİYERE DEVAM ET (KAYIT YOK)",
                        fontFamily = TekoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 23.sp,
                        letterSpacing = 1.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenSaveSlots,
                        border = BorderStroke(1.dp, CrispWhiteBorderStrong),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = DeepStadiumGreen.copy(alpha = 0.78f),
                            contentColor = PrimaryWhite
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("btn_career_saves")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "KARİYER KAYITLARI (${saveSummaries.size}/3)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenSettings,
                        border = BorderStroke(1.dp, CrispWhiteBorderStrong),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = DeepStadiumGreen.copy(alpha = 0.78f),
                            contentColor = PrimaryWhite
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(0.75f)
                            .height(52.dp)
                            .testTag("btn_settings")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AYARLAR",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SaveSlotsScreen(
    saveSummaries: List<CareerSaveSummary>,
    onBack: () -> Unit,
    onLoadSlot: (Int) -> Unit,
    onCreateInSlot: (Int) -> Unit,
    onDeleteSlot: (Int) -> Unit
) {
    val slotMap = saveSummaries.associateBy { it.slotId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPitchSurface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_from_saves")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = PrimaryWhite)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "KARİYER KAYIT DOSYALARI",
                    fontFamily = TekoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = PrimaryWhite
                )
                Text(
                    text = "3 bağımsız yerel kariyer yuvası • Otomatik & Sürümlü Kayıt Sistemi",
                    style = MaterialTheme.typography.bodySmall,
                    color = SecondaryMintGray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(listOf(1, 2, 3)) { slotId ->
                val summary = slotMap[slotId]
                Surface(
                    color = DeepStadiumGreen,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        1.5.dp,
                        if (summary != null) VibrantFootballGreen else CrispWhiteBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "KAYIT YUVASI #$slotId",
                                style = MaterialTheme.typography.labelMedium,
                                color = ChampionshipGold
                            )
                            Text(
                                text = if (summary != null) "Sürüm v${summary.saveVersion} • Aktif" else "Boş Yuva",
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryMintGray
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (summary != null) {
                            Text(
                                text = "${summary.playerName} (${summary.positionCode} • OVR ${summary.overallRating})",
                                style = MaterialTheme.typography.headlineSmall,
                                color = PrimaryWhite
                            )
                            Text(
                                text = "${summary.clubName} • ${summary.leagueName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = VibrantFootballGreen
                            )
                            Text(
                                text = "Tarih: ${summary.dateLabel} (Gün ${summary.currentDayIndex}) • Bakiye: ${summary.balanceEur} €",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryMintGray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { onLoadSlot(slotId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VibrantFootballGreen,
                                        contentColor = DarkPitchSurface
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_load_slot_$slotId")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("BU KARİYERE DEVAM ET", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { onDeleteSlot(slotId) },
                                    border = BorderStroke(1.dp, MatchLossRed),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MatchLossRed),
                                    modifier = Modifier.testTag("btn_delete_slot_$slotId")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Sil")
                                }
                            }
                        } else {
                            Text(
                                text = "Bu kayıt yuvasında henüz bir kariyer bulunmuyor.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SecondaryMintGray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onCreateInSlot(slotId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkTurfGreen,
                                    contentColor = PrimaryWhite
                                ),
                                border = BorderStroke(1.dp, VibrantFootballGreen),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_new_slot_$slotId")
                            ) {
                                Icon(Icons.Default.AddCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("YUVA #$slotId İÇİNDE YENİ KARİYER BAŞLAT")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsMenuScreen(
    settings: UserSettings,
    onBack: () -> Unit,
    onToggleAmbience: (Boolean) -> Unit,
    onToggleSfx: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onSelectGraphics: (GraphicsQuality) -> Unit,
    onToggleSkipIntro: (Boolean) -> Unit,
    onSaveAccountName: (String) -> Unit,
    onSelectFrameRate: (Int) -> Unit = {},
    onConnectDialogue: (String, String) -> Unit = { _, _ -> }
) {
    var profileInput by remember(settings.accountDisplayName) {
        mutableStateOf(settings.accountDisplayName)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkPitchSurface)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_from_settings")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = PrimaryWhite)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "OYUN VE SİSTEM AYARLARI",
                        fontFamily = TekoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = PrimaryWhite
                    )
                    Text(
                        text = "Ses, grafik performansı, titreşim ve yerel hesap yönetimi",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryMintGray
                    )
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("SES VE HAPTİK GERİ BİLDİRİM", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
                    SettingToggleRow("Stadyum Atmosferi ve Ambiyans", settings.stadiumAmbienceEnabled, onToggleAmbience)
                    SettingToggleRow("Maç Düdüğü, Top ve Menü Ses Efektleri", settings.soundEffectsEnabled, onToggleSfx)
                    SettingToggleRow("Haptik Titreşim Geri Bildirimi", settings.hapticsEnabled, onToggleHaptics)
                    SettingToggleRow("Açılış Stadyum Animasyonunu Otomatik Geç", settings.skipIntroAnimation, onToggleSkipIntro)
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("GRAFİK KALİTESİ", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
                    DialogueConnectionSettings(onConnectDialogue)
                    FrameRateSettings(settings.targetFrameRate, onSelectFrameRate)
                    GraphicsQuality.entries.forEach { quality ->
                        val selected = settings.graphicsQuality == quality
                        Surface(
                            color = if (selected) DarkTurfGreen else DarkPitchSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (selected) VibrantFootballGreen else CrispWhiteBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectGraphics(quality) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(quality.labelTr, style = MaterialTheme.typography.bodyMedium, color = PrimaryWhite)
                                if (selected) {
                                    Text("AKTİF", style = MaterialTheme.typography.labelMedium, color = BrightNeonGreen)
                                }
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("KARİYER HESAP KİMLİĞİ (ÇEVRİMDIŞI & YEREL MOD)", style = MaterialTheme.typography.labelLarge, color = ChampionshipGold)
                    Text(
                        text = "Çevrimdışı misafir modu ve yerel Room veritabanı tam kapasiteyle aktiftir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryMintGray
                    )
                    OutlinedTextField(
                        value = profileInput,
                        onValueChange = { profileInput = it },
                        label = { Text("Oyuncu / Hesap Görünür Adı") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onSaveAccountName(profileInput) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VibrantFootballGreen,
                            contentColor = DarkPitchSurface
                        )
                    ) {
                        Text("PROFİL ADINI KAYDET", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryWhite,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DarkPitchSurface,
                checkedTrackColor = VibrantFootballGreen
            )
        )
    }
}
