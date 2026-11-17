package com.example.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiClient
import com.example.ai.GeminiConnectionTestResult
import com.example.ai.GeminiModelOption
import com.example.data.UserSettings
import com.example.model.GraphicsQuality
import com.example.ui.components.FLHeroPanel
import com.example.ui.components.FLSectionHeader
import com.example.ui.components.FLTopBar
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.BrightGold
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.CrispWhiteBorder
import com.example.ui.theme.DarkPitchSurface
import com.example.ui.theme.DarkTurfGreen
import com.example.ui.theme.DeepStadiumGreen
import com.example.ui.theme.MatchLossRed
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PrimaryWhite
import com.example.ui.theme.SecondaryMintGray
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.StrokePremium
import com.example.ui.theme.StrokePremiumStrong
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VibrantFootballGreen
import com.example.ui.theme.Warning
import kotlinx.coroutines.launch

/**
 * Full-featured Settings Screen for Football Life.
 * Reachable both from the Main Menu ("AYARLAR" button) and from inside the Career Hub / Phone Settings.
 *
 * Includes:
 * 1. Gemini AI Dialogue Engine Configuration:
 *    - API Key input field (with show/hide toggle, secure local DataStore persistence, never logged)
 *    - Active Key Source indicator (User Settings vs `.env` BuildConfig vs Offline Scripted Fallback)
 *    - "Anahtarı Kaydet" & "Temizle" actions
 *    - "Bağlantıyı Test Et" live test button with latency & model verification banner
 *    - Gemini Model Selector (`gemini-2.5-flash`, `gemini-3.8-flash`, `gemini-2.5-pro`, `gemini-2.5-flash-lite`)
 *    - AI Dialogue Master Switch (with automatic offline Turkish fallback explanation)
 * 2. Audio, Haptics & Stadium Ambience Settings
 * 3. 60 FPS Graphics Quality Profile
 * 4. Career Account Display Name
 */
@Composable
fun SettingsScreen(
    settings: UserSettings,
    onBack: () -> Unit,
    onSaveGeminiApiKey: (String) -> Unit,
    onSelectGeminiModel: (String) -> Unit,
    onToggleAiDialogues: (Boolean) -> Unit,
    onToggleAmbience: (Boolean) -> Unit,
    onToggleSfx: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onSelectGraphics: (GraphicsQuality) -> Unit,
    onToggleSkipIntro: (Boolean) -> Unit,
    onSaveAccountName: (String) -> Unit,
    externalConnectionStatus: String? = null,
    onTestGeminiConnection: ((String, String) -> Unit)? = null
) {
    val coroutineScope = rememberCoroutineScope()

    var apiKeyDraft by rememberSaveable(settings.geminiApiKey) {
        mutableStateOf(settings.geminiApiKey)
    }
    var showApiKeyPlain by rememberSaveable { mutableStateOf(false) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<GeminiConnectionTestResult?>(null) }

    var profileInput by rememberSaveable(settings.accountDisplayName) {
        mutableStateOf(settings.accountDisplayName)
    }

    val hasActiveKey = GeminiClient.hasConfiguredApiKey(apiKeyDraft.ifBlank { settings.geminiApiKey })
    val keySourceLabel = GeminiClient.getApiKeySourceLabelTr(settings.geminiApiKey)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp)
            .testTag("settings_screen"),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
        item {
            FLTopBar(
                title = "OYUN & GEMINI AI AYARLARI",
                subtitle = "Yapay zeka diyalog motoru, API anahtarı, ses ve grafik performansı",
                onBack = onBack,
                backTestTag = "btn_back_from_settings"
            )
        }

        // =====================================================================
        // 1. GEMINI AI DİYALOG SİSTEMİ & API ANAHTARI YÖNETİMİ
        // =====================================================================
        item {
            FLSectionHeader(
                title = "GEMINI AI CANLI DİYALOG MOTORU",
                subtitle = "WhatsApp, Instagram DM, Teknik Direktör Odası ve Telefon Aramaları"
            )
        }

        item {
            FLHeroPanel(
                borderColor = if (hasActiveKey && settings.aiDialoguesEnabled) AccentBright else StrokePremiumStrong
            ) {
                // Status Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = if (hasActiveKey && settings.aiDialoguesEnabled) AccentPrimary.copy(alpha = 0.22f) else SurfaceElevated,
                            shape = CircleShape,
                            border = BorderStroke(1.dp, if (hasActiveKey) AccentBright else StrokePremium),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Gemini AI",
                                    tint = if (hasActiveKey) AccentBright else SecondaryMintGray,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "GERÇEKÇİ TÜRKÇE AI DİYALOGLAR",
                                fontFamily = BarlowCondensedFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = if (hasActiveKey && settings.aiDialoguesEnabled) {
                                    "Aktif • Karakterler serbest mesajlarına canlı yanıt verir"
                                } else {
                                    "Çevrimdışı Yedek Mod • Senaryo tabanlı yanıtlar aktif"
                                },
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.5.sp,
                                color = if (hasActiveKey && settings.aiDialoguesEnabled) AccentBright else Warning
                            )
                        }
                    }

                    Switch(
                        checked = settings.aiDialoguesEnabled,
                        onCheckedChange = onToggleAiDialogues,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BackgroundPrimary,
                            checkedTrackColor = AccentPrimary
                        ),
                        modifier = Modifier.testTag("switch_ai_dialogues")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = StrokePremium)
                Spacer(modifier = Modifier.height(12.dp))

                // Active API Key Source Badge
                Surface(
                    color = BackgroundPrimary.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, StrokePremium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = ChampionshipGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AKTİF ANAHTAR KAYNAĞI",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Text(
                                text = keySourceLabel,
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Koda dokunmadan kendi Google AI Studio Gemini API anahtarınızı aşağıya girebilirsiniz. Anahtarınız cihazda güvenli yerel DataStore içinde saklanır ve hiçbir log kaydına yazılmaz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // API Key Input Field
                OutlinedTextField(
                    value = apiKeyDraft,
                    onValueChange = { apiKeyDraft = it },
                    label = { Text("Gemini API Anahtarı (AIza...)") },
                    placeholder = { Text("AIzaSy... anahtarınızı buraya yapıştırın", color = TextSecondary.copy(alpha = 0.6f)) },
                    singleLine = true,
                    visualTransformation = if (showApiKeyPlain) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showApiKeyPlain = !showApiKeyPlain },
                                modifier = Modifier.testTag("btn_toggle_apikey_visibility")
                            ) {
                                Icon(
                                    imageVector = if (showApiKeyPlain) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showApiKeyPlain) "Gizle" else "Göster",
                                    tint = TextSecondary
                                )
                            }
                            if (apiKeyDraft.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        apiKeyDraft = ""
                                        onSaveGeminiApiKey("")
                                        testResult = null
                                    },
                                    modifier = Modifier.testTag("btn_clear_apikey")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Temizle",
                                        tint = MatchLossRed
                                    )
                                }
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BackgroundPrimary,
                        unfocusedContainerColor = BackgroundPrimary,
                        focusedBorderColor = AccentBright,
                        unfocusedBorderColor = StrokePremiumStrong,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentBright,
                        unfocusedLabelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_gemini_api_key")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: Save API Key & Test Connection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onSaveGeminiApiKey(apiKeyDraft.trim())
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentPrimary,
                            contentColor = BackgroundPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_save_gemini_key")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ANAHTARI KAYDET",
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val keyToTest = apiKeyDraft.trim()
                            if (keyToTest.isNotEmpty() && keyToTest != settings.geminiApiKey) {
                                onSaveGeminiApiKey(keyToTest)
                            }
                            isTestingConnection = true
                            testResult = null
                            coroutineScope.launch {
                                val res = GeminiClient.testConnection(
                                    apiKeyInput = keyToTest.ifBlank { settings.geminiApiKey },
                                    modelId = settings.geminiModel
                                )
                                isTestingConnection = false
                                testResult = res
                            }
                        },
                        enabled = !isTestingConnection,
                        border = BorderStroke(1.dp, AccentBright),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = SurfaceElevated,
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_test_gemini_connection")
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(
                                color = AccentBright,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TEST EDİLİYOR...",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        } else {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = AccentBright, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BAĞLANTIYI TEST ET",
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }

                // Connection Test Result Banner
                val currentResult = testResult
                val hasValidExternalStatus = !externalConnectionStatus.isNullOrBlank() &&
                    !externalConnectionStatus.contains("test ediliyor", ignoreCase = true)
                AnimatedVisibility(visible = !isTestingConnection && (currentResult != null || hasValidExternalStatus)) {
                    val isSuccess = currentResult?.success ?: (externalConnectionStatus?.contains("Başarılı", ignoreCase = true) == true)
                    val titleText = currentResult?.statusTitleTr ?: "Bağlantı Durumu"
                    val detailText = currentResult?.detailMessageTr ?: externalConnectionStatus.orEmpty()

                    Surface(
                        color = if (isSuccess) AccentPrimary.copy(alpha = 0.16f) else MatchLossRed.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isSuccess) AccentBright else MatchLossRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .testTag("gemini_test_result_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = if (isSuccess) AccentBright else MatchLossRed,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = titleText,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = if (isSuccess) AccentBright else MatchLossRed
                                )
                                Text(
                                    text = detailText,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = StrokePremium)
                Spacer(modifier = Modifier.height(12.dp))

                // Gemini Model Selector
                Text(
                    text = "GEMINI YAPAY ZEKA MODEL SEÇİMİ",
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ChampionshipGold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GeminiModelOption.entries.forEach { option ->
                        val selected = settings.geminiModel.equals(option.modelId, ignoreCase = true)
                        Surface(
                            color = if (selected) DarkTurfGreen else BackgroundPrimary.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) AccentBright else StrokePremium
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectGeminiModel(option.modelId) }
                                .testTag("model_option_${option.modelId}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = option.titleTr,
                                            fontFamily = OutfitFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = if (selected) AccentPrimary else SurfaceElevated,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = option.badgeTr,
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.5.sp,
                                                color = if (selected) BackgroundPrimary else AccentBright,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = option.descriptionTr,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "Model Kodu: ${option.modelId}",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 10.5.sp,
                                        color = AccentBright.copy(alpha = 0.85f)
                                    )
                                }
                                if (selected) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Seçili",
                                        tint = AccentBright,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 2. SES, AMBİYANS VE HAPTİK GERİ BİLDİRİM
        // =====================================================================
        item {
            FLSectionHeader(
                title = "SES VE HAPTİK GERİ BİLDİRİM",
                subtitle = "Stadyum atmosferi, düdük/top sesleri ve titreşim"
            )
        }

        item {
            Surface(
                color = DeepStadiumGreen,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, CrispWhiteBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STADYUM & MENÜ SESLERİ",
                            style = MaterialTheme.typography.labelLarge,
                            color = ChampionshipGold
                        )
                    }
                    SettingsToggleRow("Stadyum Atmosferi ve Ambiyans", settings.stadiumAmbienceEnabled, onToggleAmbience)
                    SettingsToggleRow("Maç Düdüğü, Top ve Menü Ses Efektleri", settings.soundEffectsEnabled, onToggleSfx)
                    SettingsToggleRow("Haptik Titreşim Geri Bildirimi", settings.hapticsEnabled, onToggleHaptics)
                    SettingsToggleRow("Açılış Stadyum Animasyonunu Otomatik Geç", settings.skipIntroAnimation, onToggleSkipIntro)
                }
            }
        }

        // =====================================================================
        // 3. GRAFİK VE 60 FPS PERFORMANS PROFİLİ
        // =====================================================================
        item {
            FLSectionHeader(
                title = "GRAFİK VE 60 FPS PERFORMANS PROFİLİ",
                subtitle = "2D interaktif maç motoru ve parçacık efekt yoğunluğu"
            )
        }

        item {
            Surface(
                color = DeepStadiumGreen,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, CrispWhiteBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MAÇ MOTORU GRAFİK KALİTESİ",
                            style = MaterialTheme.typography.labelLarge,
                            color = ChampionshipGold
                        )
                    }
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
                                    Text("AKTİF", style = MaterialTheme.typography.labelMedium, color = AccentBright)
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 4. KARİYER HESAP KİMLİĞİ
        // =====================================================================
        item {
            FLSectionHeader(
                title = "KARİYER HESAP KİMLİĞİ",
                subtitle = "Yerel kayıt profili ve görünür menajer/oyuncu hesabı"
            )
        }

        item {
            Surface(
                color = DeepStadiumGreen,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, CrispWhiteBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = ChampionshipGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "YEREL KARİYER PROFİLİ",
                            style = MaterialTheme.typography.labelLarge,
                            color = ChampionshipGold
                        )
                    }
                    Text(
                        text = "Kariyer verileriniz Room veritabanında otomatik yedeklemeli olarak saklanır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryMintGray
                    )
                    OutlinedTextField(
                        value = profileInput,
                        onValueChange = { profileInput = it },
                        label = { Text("Oyuncu / Hesap Görünür Adı") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkPitchSurface,
                            unfocusedContainerColor = DarkPitchSurface,
                            focusedBorderColor = VibrantFootballGreen,
                            unfocusedBorderColor = CrispWhiteBorder,
                            focusedTextColor = PrimaryWhite,
                            unfocusedTextColor = PrimaryWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onSaveAccountName(profileInput) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VibrantFootballGreen,
                            contentColor = DarkPitchSurface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("PROFİL ADINI KAYDET", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkPitchSurface.copy(alpha = 0.65f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
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
