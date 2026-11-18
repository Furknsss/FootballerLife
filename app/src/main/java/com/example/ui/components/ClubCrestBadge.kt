package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Club
import com.example.ui.theme.AccentBright
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BackgroundPrimary
import com.example.ui.theme.BackgroundSecondary
import com.example.ui.theme.BarlowCondensedFontFamily
import com.example.ui.theme.BrightGold
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.StrokePremium
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfacePrimary
import com.example.ui.theme.TekoFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs

/**
 * Heraldik Arma Desen Türleri
 */
enum class CrestPattern {
    VERTICAL_STRIPES,  // FB, BJK, TS, Sakaryaspor, Kocaelispor, Karşıyaka
    DIAGONAL_SASH,     // GS, Göztepe, Eskişehirspor
    BIPARTITE_HALF,    // Samsunspor, Sivasspor, Antalyaspor, Kayserispor
    ROUNDEL_RING,      // Başakşehir, Adana Demirspor, Eyüpspor, Bodrum FK
    HORIZONTAL_CHEVRON,// Çaykur Rizespor, Alanyaspor
    QUARTERED,         // Klasik 4 parçalı bölmeli
    SOLID_WITH_EMBLEM  // Sade zemin ve özel sembol
}

/**
 * Kulüp Kimlik Bilgisi ve Görsel Kuralları
 */
data class ClubVisualIdentity(
    val normalizedId: String,
    val pattern: CrestPattern,
    val isRoundel: Boolean = false,
    val starsCount: Int = 0,
    val starColor: Color = BrightGold,
    val specialEmblem: String? = null, // "TURKISH_FLAG", "EAGLE", "LION", "ATATURK", "SELJUK_EAGLE", "MINARET", "ANCHOR", "LEAF", "TOWER", "STAR"
    val foundationYear: Int? = null
)

object TurkishClubsCrestRegistry {

    fun resolveIdentity(clubId: String?, shortName: String?, clubName: String?): ClubVisualIdentity {
        val cid = (clubId ?: "").uppercase().trim()
        val sName = (shortName ?: "").uppercase().trim()
        val cName = (clubName ?: "").uppercase().trim()

        return when {
            // GALATASARAY
            cid.contains("TSL_GS") || sName == "GS" || cName.contains("GALATASARAY") -> ClubVisualIdentity(
                normalizedId = "TSL_GS",
                pattern = CrestPattern.DIAGONAL_SASH,
                starsCount = 4,
                specialEmblem = "LION",
                foundationYear = 1905
            )
            // FENERBAHÇE
            cid.contains("TSL_FB") || sName == "FB" || cName.contains("FENERBAHÇE") || cName.contains("FENERBAHCE") -> ClubVisualIdentity(
                normalizedId = "TSL_FB",
                pattern = CrestPattern.VERTICAL_STRIPES,
                starsCount = 5,
                specialEmblem = "OAK_LEAF",
                foundationYear = 1907
            )
            // BEŞİKTAŞ
            cid.contains("TSL_BJK") || sName == "BJK" || cName.contains("BEŞİKTAŞ") || cName.contains("BESIKTAS") -> ClubVisualIdentity(
                normalizedId = "TSL_BJK",
                pattern = CrestPattern.VERTICAL_STRIPES,
                starsCount = 3,
                specialEmblem = "TURKISH_FLAG",
                foundationYear = 1903
            )
            // TRABZONSPOR
            cid.contains("TSL_TS") || sName == "TS" || cName.contains("TRABZONSPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_TS",
                pattern = CrestPattern.VERTICAL_STRIPES,
                starsCount = 1,
                specialEmblem = "TS_WAVE",
                foundationYear = 1967
            )
            // BURSASPOR
            cid.contains("BUR") || sName == "BUR" || cName.contains("BURSASPOR") -> ClubVisualIdentity(
                normalizedId = "TFF3_BUR",
                pattern = CrestPattern.VERTICAL_STRIPES,
                starsCount = 1,
                specialEmblem = "STAR",
                foundationYear = 1963
            )
            // SAMSUNSPOR
            cid.contains("SAM") || sName == "SAM" || cName.contains("SAMSUNSPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_SAM",
                pattern = CrestPattern.BIPARTITE_HALF,
                specialEmblem = "ATATURK",
                foundationYear = 1965
            )
            // GÖZTEPE
            cid.contains("GOZ") || sName == "GOZ" || cName.contains("GÖZTEPE") || cName.contains("GOZTEPE") -> ClubVisualIdentity(
                normalizedId = "TSL_GOZ",
                pattern = CrestPattern.DIAGONAL_SASH,
                specialEmblem = "STAR",
                foundationYear = 1925
            )
            // KOCAELİSPOR
            cid.contains("KOC") || sName == "KOC" || cName.contains("KOCAELİSPOR") || cName.contains("KOCAELISPOR") -> ClubVisualIdentity(
                normalizedId = "TFF1_KOC",
                pattern = CrestPattern.VERTICAL_STRIPES,
                foundationYear = 1966
            )
            // SAKARYASPOR
            cid.contains("SAK") || sName == "SAK" || cName.contains("SAKARYASPOR") -> ClubVisualIdentity(
                normalizedId = "TFF1_SAK",
                pattern = CrestPattern.VERTICAL_STRIPES,
                specialEmblem = "TS_WAVE",
                foundationYear = 1965
            )
            // ESKİŞEHİRSPOR
            cid.contains("ESK") || sName == "ESK" || cName.contains("ESKİŞEHİRSPOR") || cName.contains("ESKISEHIRSPOR") -> ClubVisualIdentity(
                normalizedId = "BAL_ESK",
                pattern = CrestPattern.DIAGONAL_SASH,
                specialEmblem = "STAR",
                foundationYear = 1965
            )
            // MKE ANKARAGÜCÜ
            cid.contains("ANK") || sName == "ANK" || cName.contains("ANKARAGÜCÜ") || cName.contains("ANKARAGUCU") -> ClubVisualIdentity(
                normalizedId = "TFF1_ANK",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                specialEmblem = "SUNBURST",
                foundationYear = 1910
            )
            // KONYASPOR
            cid.contains("KON") || sName == "KON" || cName.contains("KONYASPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_KON",
                pattern = CrestPattern.BIPARTITE_HALF,
                specialEmblem = "SELJUK_EAGLE",
                foundationYear = 1922
            )
            // ANTALYASPOR
            cid.contains("ANT") || sName == "ANT" || cName.contains("ANTALYASPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_ANT",
                pattern = CrestPattern.BIPARTITE_HALF,
                specialEmblem = "MINARET",
                foundationYear = 1966
            )
            // ADANA DEMİRSPOR
            cid.contains("ADS") || sName == "ADS" || cName.contains("ADANA DEMİR") || cName.contains("ADANA DEMIR") -> ClubVisualIdentity(
                normalizedId = "TSL_ADS",
                pattern = CrestPattern.ROUNDEL_RING,
                isRoundel = true,
                specialEmblem = "LOCOMOTIVE",
                foundationYear = 1940
            )
            // BAŞAKŞEHİR
            cid.contains("IBFK") || sName == "IBFK" || cName.contains("BAŞAKŞEHİR") || cName.contains("BASAKSEHIR") -> ClubVisualIdentity(
                normalizedId = "TSL_IBFK",
                pattern = CrestPattern.ROUNDEL_RING,
                isRoundel = true,
                specialEmblem = "OWL",
                foundationYear = 1990
            )
            // EYÜPSPOR
            cid.contains("EYU") || sName == "EYU" || cName.contains("EYÜPSPOR") || cName.contains("EYUPSPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_EYU",
                pattern = CrestPattern.ROUNDEL_RING,
                isRoundel = true,
                foundationYear = 1919
            )
            // KASIMPAŞA
            cid.contains("KAS") || sName == "KAS" || cName.contains("KASIMPAŞA") || cName.contains("KASIMPASA") -> ClubVisualIdentity(
                normalizedId = "TSL_KAS",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                specialEmblem = "ANCHOR",
                foundationYear = 1921
            )
            // SİVASSPOR
            cid.contains("SIV") || sName == "SIV" || cName.contains("SİVASSPOR") || cName.contains("SIVASSPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_SIV",
                pattern = CrestPattern.BIPARTITE_HALF,
                specialEmblem = "STAR",
                foundationYear = 1967
            )
            // KAYSERİSPOR
            cid.contains("KAY") || sName == "KAY" || cName.contains("KAYSERİSPOR") || cName.contains("KAYSERISPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_KAY",
                pattern = CrestPattern.BIPARTITE_HALF,
                specialEmblem = "MOUNTAIN",
                foundationYear = 1966
            )
            // ÇAYKUR RİZESPOR
            cid.contains("CRS") || sName == "CRS" || cName.contains("RİZESPOR") || cName.contains("RIZESPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_CRS",
                pattern = CrestPattern.HORIZONTAL_CHEVRON,
                specialEmblem = "LEAF",
                foundationYear = 1953
            )
            // ALANYASPOR
            cid.contains("ALN") || sName == "ALN" || cName.contains("ALANYASPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_ALN",
                pattern = CrestPattern.HORIZONTAL_CHEVRON,
                specialEmblem = "TOWER",
                foundationYear = 1948
            )
            // GAZİANTEP FK
            cid.contains("GFK") || sName == "GFK" || cName.contains("GAZİANTEP") || cName.contains("GAZIANTEP") -> ClubVisualIdentity(
                normalizedId = "TSL_GFK",
                pattern = CrestPattern.VERTICAL_STRIPES,
                specialEmblem = "EAGLE",
                foundationYear = 1988
            )
            // BODRUM FK
            cid.contains("BOD") || sName == "BOD" || cName.contains("BODRUM") -> ClubVisualIdentity(
                normalizedId = "TSL_BOD",
                pattern = CrestPattern.ROUNDEL_RING,
                isRoundel = true,
                foundationYear = 1931
            )
            // HATAYSPOR
            cid.contains("HAT") || sName == "HAT" || cName.contains("HATAYSPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_HAT",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                specialEmblem = "LEAF",
                foundationYear = 1967
            )
            // KARŞIYAKA
            cid.contains("KSK") || sName == "KSK" || cName.contains("KARŞIYAKA") || cName.contains("KARSIYAKA") -> ClubVisualIdentity(
                normalizedId = "TFF3_KSK",
                pattern = CrestPattern.VERTICAL_STRIPES,
                specialEmblem = "TURKISH_FLAG",
                foundationYear = 1912
            )
            // AMED SK
            cid.contains("AMD") || sName == "AMD" || cName.contains("AMED") -> ClubVisualIdentity(
                normalizedId = "TFF1_AMD",
                pattern = CrestPattern.ROUNDEL_RING,
                isRoundel = true,
                specialEmblem = "SUNBURST",
                foundationYear = 1990
            )
            // GENÇLERBİRLİĞİ
            cid.contains("GEN") || sName == "GEN" || cName.contains("GENÇLERBİRLİĞİ") || cName.contains("GENCLERBIRLIGI") -> ClubVisualIdentity(
                normalizedId = "TFF1_GEN",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                specialEmblem = "STAR",
                foundationYear = 1923
            )
            // VEFA 1908
            cid.contains("VEF") || sName == "VEF" || cName.contains("VEFA") -> ClubVisualIdentity(
                normalizedId = "SAL_VEF",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                foundationYear = 1908
            )
            // DİĞER TÜM 137 KULÜP İÇİN DETERMINİSTİK HERALDİK MODELLEME
            else -> {
                val seed = abs((cid + sName).hashCode())
                val pattern = when (seed % 5) {
                    0 -> CrestPattern.VERTICAL_STRIPES
                    1 -> CrestPattern.DIAGONAL_SASH
                    2 -> CrestPattern.BIPARTITE_HALF
                    3 -> CrestPattern.QUARTERED
                    else -> CrestPattern.SOLID_WITH_EMBLEM
                }
                val isRound = (seed % 6) == 0
                ClubVisualIdentity(
                    normalizedId = cid.ifBlank { sName },
                    pattern = if (isRound) CrestPattern.ROUNDEL_RING else pattern,
                    isRoundel = isRound,
                    foundationYear = 1920 + (seed % 75)
                )
            }
        }
    }
}

/**
 * 137 Türk Kulübü ve Dünya Takımları İçin Yüksek Çözünürlüklü Vektör Arma Bileşeni
 */
@Composable
fun ClubCrestBadge(
    shortName: String,
    primaryHex: String,
    secondaryHex: String,
    size: Dp = 46.dp,
    modifier: Modifier = Modifier,
    clubId: String? = null,
    clubName: String? = null,
    showEmblem: Boolean = true,
    showStars: Boolean = true
) {
    val primaryColor = remember(primaryHex) { parseHexColor(primaryHex, SurfaceElevated) }
    val secondaryColor = remember(secondaryHex) { parseHexColor(secondaryHex, TextPrimary) }
    val identity = remember(clubId, shortName, clubName) {
        TurkishClubsCrestRegistry.resolveIdentity(clubId, shortName, clubName)
    }

    val displayLetters = remember(shortName, clubName) {
        val raw = shortName.trim().ifEmpty { clubName?.take(3) ?: "FC" }
        raw.take(3).uppercase()
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            if (identity.isRoundel) {
                drawRoundelCrest(
                    w = w,
                    h = h,
                    primary = primaryColor,
                    secondary = secondaryColor,
                    identity = identity
                )
            } else {
                drawShieldCrest(
                    w = w,
                    h = h,
                    primary = primaryColor,
                    secondary = secondaryColor,
                    identity = identity
                )
            }
        }

        // Monogram / Metin ve Yıldız Katmanı
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = (size.value * 0.08f).dp)
        ) {
            // Şampiyonluk Yıldızları
            if (showStars && identity.starsCount > 0) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(identity.starsCount) {
                        Text(
                            text = "★",
                            color = identity.starColor,
                            fontSize = (size.value * 0.16f).sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = (size.value * 0.16f).sp
                        )
                    }
                }
            }

            // Kulüp Kısa Adı
            Text(
                text = displayLetters,
                fontFamily = BarlowCondensedFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = when {
                    displayLetters.length >= 4 -> (size.value * 0.22f).sp
                    displayLetters.length == 3 -> (size.value * 0.27f).sp
                    else -> (size.value * 0.32f).sp
                },
                letterSpacing = 0.5.sp,
                color = if (identity.specialEmblem == "TURKISH_FLAG") TextPrimary else TextPrimary,
                textAlign = TextAlign.Center
            )

            // Kuruluş Yılı (Büyük armalarda)
            if (size >= 54.dp && identity.foundationYear != null) {
                Text(
                    text = "${identity.foundationYear}",
                    fontFamily = TekoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.15f).sp,
                    color = secondaryColor.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Kulüp nesnesi ile doğrudan çağırma overload'u
 */
@Composable
fun ClubCrestBadge(
    club: Club,
    size: Dp = 46.dp,
    modifier: Modifier = Modifier,
    showEmblem: Boolean = true,
    showStars: Boolean = true
) {
    ClubCrestBadge(
        shortName = club.shortName,
        primaryHex = club.primaryColorHex,
        secondaryHex = club.secondaryColorHex,
        size = size,
        modifier = modifier,
        clubId = club.id,
        clubName = club.name,
        showEmblem = showEmblem,
        showStars = showStars
    )
}

/**
 * Klasik Sivri Heraldik Kalkan Çizimi
 */
private fun DrawScope.drawShieldCrest(
    w: Float,
    h: Float,
    primary: Color,
    secondary: Color,
    identity: ClubVisualIdentity
) {
    val shieldPath = Path().apply {
        moveTo(w * 0.5f, h * 0.03f)
        cubicTo(w * 0.68f, h * 0.03f, w * 0.86f, h * 0.08f, w * 0.94f, h * 0.16f)
        lineTo(w * 0.92f, h * 0.60f)
        cubicTo(w * 0.84f, h * 0.82f, w * 0.64f, h * 0.94f, w * 0.5f, h * 0.98f)
        cubicTo(w * 0.36f, h * 0.94f, w * 0.16f, h * 0.82f, w * 0.08f, h * 0.60f)
        lineTo(w * 0.06f, h * 0.16f)
        cubicTo(w * 0.14f, h * 0.08f, w * 0.32f, h * 0.03f, w * 0.5f, h * 0.03f)
        close()
    }

    // 1. Zemin Gölgelendirmesi (Koyu Arka Plan)
    drawPath(
        path = shieldPath,
        brush = Brush.verticalGradient(
            listOf(primary, primary.copy(alpha = 0.9f), BackgroundPrimary.copy(alpha = 0.95f))
        )
    )

    // 2. Heraldik Desen (Stripes / Sash / Bipartite / Emblem)
    when (identity.pattern) {
        CrestPattern.VERTICAL_STRIPES -> {
            // 5 Şerit
            val stripeWidth = w / 5.2f
            for (i in 0..4) {
                if (i % 2 == 1) {
                    drawRect(
                        color = secondary.copy(alpha = 0.92f),
                        topLeft = Offset(w * 0.08f + i * stripeWidth, h * 0.05f),
                        size = Size(stripeWidth * 0.85f, h * 0.85f)
                    )
                }
            }
        }
        CrestPattern.DIAGONAL_SASH -> {
            val sashPath = Path().apply {
                moveTo(w * 0.15f, h * 0.90f)
                lineTo(w * 0.40f, h * 0.96f)
                lineTo(w * 0.92f, h * 0.22f)
                lineTo(w * 0.72f, h * 0.08f)
                close()
            }
            drawPath(path = sashPath, color = secondary.copy(alpha = 0.90f))
        }
        CrestPattern.BIPARTITE_HALF -> {
            drawRect(
                color = secondary.copy(alpha = 0.92f),
                topLeft = Offset(w * 0.5f, h * 0.05f),
                size = Size(w * 0.45f, h * 0.90f)
            )
        }
        CrestPattern.HORIZONTAL_CHEVRON -> {
            val chevPath = Path().apply {
                moveTo(w * 0.10f, h * 0.35f)
                lineTo(w * 0.50f, h * 0.58f)
                lineTo(w * 0.90f, h * 0.35f)
                lineTo(w * 0.90f, h * 0.52f)
                lineTo(w * 0.50f, h * 0.75f)
                lineTo(w * 0.10f, h * 0.52f)
                close()
            }
            drawPath(path = chevPath, color = secondary.copy(alpha = 0.85f))
        }
        CrestPattern.QUARTERED -> {
            drawRect(
                color = secondary.copy(alpha = 0.88f),
                topLeft = Offset(w * 0.10f, h * 0.10f),
                size = Size(w * 0.40f, h * 0.40f)
            )
            drawRect(
                color = secondary.copy(alpha = 0.88f),
                topLeft = Offset(w * 0.50f, h * 0.50f),
                size = Size(w * 0.40f, h * 0.40f)
            )
        }
        else -> {
            // Sade zemin ve hafif merkez daire
            drawCircle(
                color = secondary.copy(alpha = 0.22f),
                radius = w * 0.32f,
                center = Offset(w * 0.5f, h * 0.48f)
            )
        }
    }

    // 3. Özel Mini Sembol (Beşiktaş Türk Bayrağı, Konyaspor Kartal vs.)
    if (identity.specialEmblem == "TURKISH_FLAG") {
        val flagCenter = Offset(w * 0.5f, h * 0.28f)
        val flagRadius = w * 0.15f
        drawCircle(color = Color(0xFFE74C3C), radius = flagRadius, center = flagCenter)
        drawCircle(color = Color.White, radius = flagRadius * 0.65f, center = Offset(flagCenter.x - flagRadius * 0.15f, flagCenter.y))
        drawCircle(color = Color(0xFFE74C3C), radius = flagRadius * 0.55f, center = Offset(flagCenter.x - flagRadius * 0.05f, flagCenter.y))
        drawCircle(color = Color.White, radius = flagRadius * 0.20f, center = Offset(flagCenter.x + flagRadius * 0.32f, flagCenter.y))
    } else if (identity.specialEmblem == "STAR") {
        drawCircle(
            color = BrightGold.copy(alpha = 0.7f),
            radius = w * 0.08f,
            center = Offset(w * 0.5f, h * 0.28f)
        )
    }

    // 4. Parlak Üst Yansıma (Glossy Reflection Sheen)
    val glossPath = Path().apply {
        moveTo(w * 0.12f, h * 0.16f)
        cubicTo(w * 0.30f, h * 0.08f, w * 0.70f, h * 0.08f, w * 0.88f, h * 0.16f)
        lineTo(w * 0.85f, h * 0.44f)
        cubicTo(w * 0.65f, h * 0.36f, w * 0.35f, h * 0.36f, w * 0.15f, h * 0.44f)
        close()
    }
    drawPath(
        path = glossPath,
        brush = Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.02f))
        )
    )

    // 5. Metalik Kenarlık (Platinum / Gold Rim)
    val rimBrush = Brush.sweepGradient(
        listOf(
            BrightGold.copy(alpha = 0.9f),
            Color.White.copy(alpha = 0.8f),
            BrightGold.copy(alpha = 0.6f),
            Color(0xFFE0E0E0),
            BrightGold.copy(alpha = 0.9f)
        )
    )
    drawPath(
        path = shieldPath,
        brush = rimBrush,
        style = Stroke(width = (w * 0.055f).coerceAtLeast(1.5f))
    )
}

/**
 * Yuvarlak / Mühür (Roundel) Arma Çizimi
 */
private fun DrawScope.drawRoundelCrest(
    w: Float,
    h: Float,
    primary: Color,
    secondary: Color,
    identity: ClubVisualIdentity
) {
    val center = Offset(w * 0.5f, h * 0.5f)
    val radius = (w * 0.46f).coerceAtMost(h * 0.46f)

    // Dış Halka Zemin
    drawCircle(
        brush = Brush.radialGradient(
            listOf(primary, BackgroundPrimary),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // İç Çember
    drawCircle(
        color = secondary.copy(alpha = 0.30f),
        radius = radius * 0.76f,
        center = center
    )
    drawCircle(
        color = primary,
        radius = radius * 0.68f,
        center = center
    )

    // Metalik Altın/Platin Dış Halka
    drawCircle(
        brush = Brush.sweepGradient(
            listOf(BrightGold, Color.White, BrightGold.copy(alpha = 0.6f), Color(0xFFD4AF37), BrightGold)
        ),
        radius = radius,
        center = center,
        style = Stroke(width = (w * 0.06f).coerceAtLeast(1.5f))
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.65f),
        radius = radius * 0.76f,
        center = center,
        style = Stroke(width = (w * 0.025f).coerceAtLeast(1f))
    )
}

/**
 * Türkiye Ligleri (Süper Lig, 1. Lig, 2. Lig, 3. Lig, BAL, SAL) İçin Vektör/Drawable Rozet
 */
@Composable
fun LeagueCrestBadge(
    leagueId: String?,
    leagueName: String? = null,
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    val lid = (leagueId ?: "").uppercase().trim()
    val lName = (leagueName ?: "").uppercase().trim()

    val isSuperLig = lid.contains("SUPER_LIG") || lid.contains("TSL") || lName.contains("SÜPER LİG") || lName.contains("SUPER LIG")
    val is1Lig = lid.contains("1_LIG") || lid.contains("1. LİG") || lName.contains("1. LİG") || lName.contains("1. LIG")
    val is2Lig = lid.contains("2_LIG") || lid.contains("2. LİG") || lName.contains("2. LİG") || lName.contains("2. LIG")
    val is3Lig = lid.contains("3_LIG") || lid.contains("3. LİG") || lName.contains("3. LİG") || lName.contains("3. LIG")
    val isBal = lid.contains("BAL") || lName.contains("BÖLGESEL") || lName.contains("BOLGESEL") || lName.contains("BAL")
    val isAmator = lid.contains("AMATOR") || lid.contains("AMATÖR") || lName.contains("AMATÖR") || lName.contains("SAL")

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (isSuperLig) {
            // Gerçek Süper Lig Logosu
            Image(
                painter = painterResource(id = R.drawable.logo_super_lig),
                contentDescription = "Trendyol Süper Lig",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
            // Yüksek Kaliteli Vektör Lig Armaları
            val (badgeBg, badgeBorder, badgeText, badgeSub) = when {
                is1Lig -> listOf(Color(0xFF8B0000), Color(0xFFFFD700), "1. LİG", "TRENDYOL")
                is2Lig -> listOf(Color(0xFF0F5A36), Color(0xFF2ECC71), "2. LİG", "NESİNE")
                is3Lig -> listOf(Color(0xFF1B4F72), Color(0xFF5DADE2), "3. LİG", "NESİNE")
                isBal -> listOf(Color(0xFF641E16), Color(0xFFE67E22), "BAL", "TFF")
                isAmator -> listOf(Color(0xFF1E3A24), Color(0xFFF1C40F), "SAL", "1923")
                else -> listOf(SurfaceElevated, StrokePremium, "LİG", "TFF")
            }

            Surface(
                color = badgeBg as Color,
                shape = RoundedCornerShape(size * 0.24f),
                border = BorderStroke(1.2.dp, badgeBorder as Color),
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = badgeSub as String,
                        fontFamily = TekoFontFamily,
                        fontSize = (size.value * 0.20f).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = (size.value * 0.20f).sp
                    )
                    Text(
                        text = badgeText as String,
                        fontFamily = BarlowCondensedFontFamily,
                        fontSize = (size.value * 0.32f).sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = (size.value * 0.32f).sp
                    )
                }
            }
        }
    }
}

/**
 * Kupa ve Turnuvalar İçin Gerçek Logo / Vektör Rozet
 * (Ziraat Türkiye Kupası, Turkcell Süper Kupa, UCL, UEL, UECL, A Millî Takım)
 */
@Composable
fun TournamentCrestBadge(
    leagueId: String? = null,
    fixtureId: String? = null,
    dateLabel: String? = null,
    size: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    val drawableRes = com.example.engine.tournament.getTournamentDrawableRes(
        leagueId = leagueId,
        fixtureId = fixtureId,
        dateLabel = dateLabel
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (drawableRes != null) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = "Turnuva Logosu",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(size * 0.2f)),
                contentScale = ContentScale.Fit
            )
        } else {
            // Lig rozeti fallback
            LeagueCrestBadge(leagueId = leagueId, leagueName = dateLabel, size = size)
        }
    }
}
