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
    VERTICAL_STRIPES,   // FB, BJK, TS, Sakaryaspor, Kocaelispor, Karşıyaka, Porto, Atletico
    DIAGONAL_SASH,      // GS, Göztepe, Eskişehirspor, Real Madrid
    BIPARTITE_HALF,     // Samsunspor, Sivasspor, Antalyaspor, Kayserispor, Bandırma
    ROUNDEL_RING,       // Başakşehir, Adana Demirspor, Eyüpspor, Man City, Chelsea, Bayern, Dortmund, Inter, PSG
    HORIZONTAL_CHEVRON, // Çaykur Rizespor, Alanyaspor
    QUARTERED,          // Klasik 4 parçalı bölmeli
    SOLID_WITH_EMBLEM,  // Arsenal, Liverpool, Man Utd, Juventus, Benfica, Vefa
    HORIZONTAL_STRIPES, // Sporting CP, Celtic
    CROSS_CANTON,       // Barcelona (St George cross + Senyera + Blaugrana)
    OVAL_STRIPES        // AC Milan
}

/**
 * Kulüp Kimlik Bilgisi ve Görsel Kuralları
 */
data class ClubVisualIdentity(
    val normalizedId: String,
    val pattern: CrestPattern,
    val isRoundel: Boolean = false,
    val isOval: Boolean = false,
    val starsCount: Int = 0,
    val starColor: Color = BrightGold,
    val specialEmblem: String? = null,
    val foundationYear: Int? = null
)

object TurkishClubsCrestRegistry {

    fun resolveIdentity(clubId: String?, shortName: String?, clubName: String?): ClubVisualIdentity {
        val cid = (clubId ?: "").uppercase().trim()
        val sName = (shortName ?: "").uppercase().trim()
        val cName = (clubName ?: "").uppercase().trim()

        return when {
            // =================================================================
            // AVRUPA DEVLERİ & ŞAMPİYONLAR LİGİ KULÜPLERİ
            // =================================================================
            // REAL MADRID (Taç, Beyaz/Altın Zemin, Mor Çapraz Bant)
            cid.contains("RMA") || sName == "RMA" || cName.contains("REAL MADRID") -> ClubVisualIdentity(
                normalizedId = "ESP_RMA",
                pattern = CrestPattern.DIAGONAL_SASH,
                isRoundel = true,
                specialEmblem = "CROWN",
                foundationYear = 1902
            )
            // FC BARCELONA (St George Haçı, Katalan Bayrağı, Blaugrana Şeritler)
            cid.contains("BAR") || sName == "BAR" || cName.contains("BARCELONA") -> ClubVisualIdentity(
                normalizedId = "ESP_BAR",
                pattern = CrestPattern.CROSS_CANTON,
                specialEmblem = "BLAUGRANA_CROSS",
                foundationYear = 1899
            )
            // MANCHESTER CITY (Gök Mavi Yuvarlak, Altın Gemi, Lancashire Gülü)
            cid.contains("MCI") || sName == "MCI" || cName.contains("MANCHESTER CITY") -> ClubVisualIdentity(
                normalizedId = "ENG_MCI",
                pattern = CrestPattern.ROUNDEL_RING,
                isRoundel = true,
                specialEmblem = "SHIP",
                foundationYear = 1894
            )
            // ARSENAL (Kırmızı Kalkan, Altın Top Arabası / Cannon)
            cid.contains("ARS") || sName == "ARS" || cName.contains("ARSENAL") -> ClubVisualIdentity(
                normalizedId = "ENG_ARS",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                specialEmblem = "CANNON",
                foundationYear = 1886
            )
            // LIVERPOOL (Kırmızı Kalkan, Altın Liverbird Kuşu)
            cid.contains("LIV") || sName == "LIV" || cName.contains("LIVERPOOL") -> ClubVisualIdentity(
                normalizedId = "ENG_LIV",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                specialEmblem = "LIVERBIRD",
                foundationYear = 1892
            )
            // MANCHESTER UNITED (Kırmızı Kalkan, Üç Başlı Mızraklı Kırmızı Şeytan)
            cid.contains("MUN") || sName == "MUN" || cName.contains("MANCHESTER UNITED") -> ClubVisualIdentity(
                normalizedId = "ENG_MUN",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                specialEmblem = "RED_DEVIL",
                foundationYear = 1878
            )
            // CHELSEA (Kraliyet Mavisi Yuvarlak, Asa Tutan Altın Aslan)
            cid.contains("CHE") || sName == "CHE" || cName.contains("CHELSEA") -> ClubVisualIdentity(
                normalizedId = "ENG_CHE",
                pattern = CrestPattern.ROUNDEL_RING,
                isRoundel = true,
                specialEmblem = "LION_RAMPANT",
                foundationYear = 1905
            )
            // BAYERN MÜNCHEN (Bavyera Mavi-Beyaz Eşkenar Dama Deseni, 5 Yıldız)
            cid.contains("BAY") || sName == "BAY" || cName.contains("BAYERN") -> ClubVisualIdentity(
                normalizedId = "GER_BAY",
                pattern = CrestPattern.ROUNDEL_RING,
                isRoundel = true,
                starsCount = 5,
                specialEmblem = "BAVARIAN_CHECKERS",
                foundationYear = 1900
            )
            // BORUSSIA DORTMUND (Sarı Yuvarlak, Siyah Halka, BVB 09 Monogramı, 2 Yıldız)
            cid.contains("BVB") || sName == "BVB" || cName.contains("DORTMUND") -> ClubVisualIdentity(
                normalizedId = "GER_BVB",
                pattern = CrestPattern.ROUNDEL_RING,
                isRoundel = true,
                starsCount = 2,
                specialEmblem = "BVB_ROUNDEL",
                foundationYear = 1909
            )
            // BAYER LEVERKUSEN (Kırmızı-Siyah Çift Aslan ve Haç)
            cid.contains("B04") || sName == "B04" || cName.contains("LEVERKUSEN") -> ClubVisualIdentity(
                normalizedId = "GER_B04",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                specialEmblem = "TWIN_LIONS",
                foundationYear = 1904
            )
            // RB LEIPZIG (Beyaz-Kırmızı Zemin, Karşılıklı Kırmızı Boğalar)
            cid.contains("RBL") || sName == "RBL" || cName.contains("LEIPZIG") -> ClubVisualIdentity(
                normalizedId = "GER_RBL",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                specialEmblem = "RED_BULLS",
                foundationYear = 2009
            )
            // INTER MILANO (Modern Mavi-Siyah Yuvarlak, IM Monogramı, 2 Yıldız)
            cid.contains("INT") || sName == "INT" || cName.contains("INTER") -> ClubVisualIdentity(
                normalizedId = "ITA_INT",
                pattern = CrestPattern.ROUNDEL_RING,
                isRoundel = true,
                starsCount = 2,
                specialEmblem = "IM_MONOGRAM",
                foundationYear = 1908
            )
            // AC MILAN (Kırmızı-Siyah Çubuklu + Milano Kırmızı Haçı Oval Kalkan, 1 Yıldız)
            cid.contains("MIL") || sName == "MIL" || cName.contains("MILAN") -> ClubVisualIdentity(
                normalizedId = "ITA_MIL",
                pattern = CrestPattern.OVAL_STRIPES,
                isOval = true,
                starsCount = 1,
                specialEmblem = "MILAN_CROSS",
                foundationYear = 1899
            )
            // JUVENTUS (Siyah-Beyaz Minimalist İkonik Çift "J" Kalkanı, 3 Yıldız)
            cid.contains("JUV") || sName == "JUV" || cName.contains("JUVENTUS") -> ClubVisualIdentity(
                normalizedId = "ITA_JUV",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                starsCount = 3,
                specialEmblem = "JUVENTUS_J",
                foundationYear = 1897
            )
            // PARIS SAINT-GERMAIN (Lacivert Yuvarlak, Kırmızı Eyfel Kulesi, Altın Zambak)
            cid.contains("PSG") || sName == "PSG" || cName.contains("PARIS") -> ClubVisualIdentity(
                normalizedId = "FRA_PSG",
                pattern = CrestPattern.ROUNDEL_RING,
                isRoundel = true,
                specialEmblem = "EIFFEL_TOWER",
                foundationYear = 1970
            )
            // SL BENFICA (Kırmızı Kalkan, Bisiklet Tekerleği Üstünde Kartal, 3 Yıldız)
            cid.contains("SLB") || sName == "SLB" || cName.contains("BENFICA") -> ClubVisualIdentity(
                normalizedId = "POR_SLB",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                starsCount = 3,
                specialEmblem = "EAGLE_WHEEL",
                foundationYear = 1904
            )
            // SPORTING CP (Yeşil-Beyaz Yatay Çubuklu Kalkan, Altın Şahlanan Aslan)
            cid.contains("SCP") || sName == "SCP" || cName.contains("SPORTING") -> ClubVisualIdentity(
                normalizedId = "POR_SCP",
                pattern = CrestPattern.HORIZONTAL_STRIPES,
                specialEmblem = "LION_RAMPANT",
                foundationYear = 1906
            )
            // FC PORTO (Mavi-Beyaz Dikey Çubuklu, Şehir Arması ve Mavi Ejderha)
            cid.contains("FCP") || sName == "FCP" || cName.contains("PORTO") -> ClubVisualIdentity(
                normalizedId = "POR_FCP",
                pattern = CrestPattern.VERTICAL_STRIPES,
                specialEmblem = "DRAGON",
                foundationYear = 1893
            )
            // ATLÉTICO MADRID (Kırmızı-Beyaz Çubuklu, Mavi Çerçevede 7 Yıldız ve Ayı)
            cid.contains("ATM") || sName == "ATM" || cName.contains("ATLÉTICO") || cName.contains("ATLETICO") -> ClubVisualIdentity(
                normalizedId = "ESP_ATM",
                pattern = CrestPattern.VERTICAL_STRIPES,
                specialEmblem = "BEAR_STARS",
                foundationYear = 1903
            )

            // =================================================================
            // TÜRKİYE SÜPER LİG & ALT LİGLER TÜM KULÜPLER
            // =================================================================
            // GALATASARAY (4 Yıldız, Sarı-Kırmızı Çapraz Kuşak, Kükreyen Aslan)
            cid.contains("TSL_GS") || sName == "GS" || cName.contains("GALATASARAY") -> ClubVisualIdentity(
                normalizedId = "TSL_GS",
                pattern = CrestPattern.DIAGONAL_SASH,
                starsCount = 4,
                specialEmblem = "LION",
                foundationYear = 1905
            )
            // FENERBAHÇE (5 Yıldız, Sarı-Lacivert Çubuklu, Palamut Dalı Yaprağı)
            cid.contains("TSL_FB") || sName == "FB" || cName.contains("FENERBAHÇE") || cName.contains("FENERBAHCE") -> ClubVisualIdentity(
                normalizedId = "TSL_FB",
                pattern = CrestPattern.VERTICAL_STRIPES,
                starsCount = 5,
                specialEmblem = "OAK_LEAF",
                foundationYear = 1907
            )
            // BEŞİKTAŞ (3 Yıldız, Siyah-Beyaz Çubuklu, Orijinal Türk Bayrağı)
            cid.contains("TSL_BJK") || sName == "BJK" || cName.contains("BEŞİKTAŞ") || cName.contains("BESIKTAS") -> ClubVisualIdentity(
                normalizedId = "TSL_BJK",
                pattern = CrestPattern.VERTICAL_STRIPES,
                starsCount = 3,
                specialEmblem = "TURKISH_FLAG",
                foundationYear = 1903
            )
            // TRABZONSPOR (1 Yıldız, Bordo-Mavi Çubuklu, Karadeniz Dalgası)
            cid.contains("TSL_TS") || sName == "TS" || cName.contains("TRABZONSPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_TS",
                pattern = CrestPattern.VERTICAL_STRIPES,
                starsCount = 1,
                specialEmblem = "TS_WAVE",
                foundationYear = 1967
            )
            // BURSASPOR (1 Şampiyonluk Yıldızı, Yeşil-Beyaz Çubuklu, Timsah / Yıldız)
            cid.contains("BUR") || sName == "BUR" || cName.contains("BURSASPOR") -> ClubVisualIdentity(
                normalizedId = "TFF3_BUR",
                pattern = CrestPattern.VERTICAL_STRIPES,
                starsCount = 1,
                specialEmblem = "CROCODILE",
                foundationYear = 1963
            )
            // SAMSUNSPOR (Kırmızı-Beyaz Bölmeli, Şahlanan At Üzerinde Atatürk Silüeti)
            cid.contains("SAM") || sName == "SAM" || cName.contains("SAMSUNSPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_SAM",
                pattern = CrestPattern.BIPARTITE_HALF,
                specialEmblem = "ATATURK",
                foundationYear = 1965
            )
            // GÖZTEPE (Sarı-Kırmızı Çapraz Damalı Kuşak)
            cid.contains("GOZ") || sName == "GOZ" || cName.contains("GÖZTEPE") || cName.contains("GOZTEPE") -> ClubVisualIdentity(
                normalizedId = "TSL_GOZ",
                pattern = CrestPattern.DIAGONAL_SASH,
                specialEmblem = "STAR",
                foundationYear = 1925
            )
            // KOCAELİSPOR (Körfez Devı, Yeşil-Siyah Çubuklu)
            cid.contains("KOC") || sName == "KOC" || cName.contains("KOCAELİSPOR") || cName.contains("KOCAELISPOR") -> ClubVisualIdentity(
                normalizedId = "TFF1_KOC",
                pattern = CrestPattern.VERTICAL_STRIPES,
                foundationYear = 1966
            )
            // SAKARYASPOR (Yeşil-Siyah Çubuklu, Çark Deresi)
            cid.contains("SAK") || sName == "SAK" || cName.contains("SAKARYASPOR") -> ClubVisualIdentity(
                normalizedId = "TFF1_SAK",
                pattern = CrestPattern.VERTICAL_STRIPES,
                specialEmblem = "TS_WAVE",
                foundationYear = 1965
            )
            // ESKİŞEHİRSPOR (Kırmızı Şimşekler, Siyah-Kırmızı Çapraz Kuşak)
            cid.contains("ESK") || sName == "ESK" || cName.contains("ESKİŞEHİRSPOR") || cName.contains("ESKISEHIRSPOR") -> ClubVisualIdentity(
                normalizedId = "BAL_ESK",
                pattern = CrestPattern.DIAGONAL_SASH,
                specialEmblem = "STAR",
                foundationYear = 1965
            )
            // MKE ANKARAGÜCÜ (Sarı-Lacivert, Güneş Işınları / İmalat-ı Harbiye)
            cid.contains("ANK") || sName == "ANK" || cName.contains("ANKARAGÜCÜ") || cName.contains("ANKARAGUCU") -> ClubVisualIdentity(
                normalizedId = "TFF1_ANK",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                specialEmblem = "SUNBURST",
                foundationYear = 1910
            )
            // KONYASPOR (Yeşil-Beyaz, Çift Başlı Selçuklu Kartalı)
            cid.contains("KON") || sName == "KON" || cName.contains("KONYASPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_KON",
                pattern = CrestPattern.BIPARTITE_HALF,
                specialEmblem = "SELJUK_EAGLE",
                foundationYear = 1922
            )
            // ANTALYASPOR (Kırmızı-Beyaz, Yivli Minare Silüeti)
            cid.contains("ANT") || sName == "ANT" || cName.contains("ANTALYASPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_ANT",
                pattern = CrestPattern.BIPARTITE_HALF,
                specialEmblem = "MINARET",
                foundationYear = 1966
            )
            // ADANA DEMİRSPOR (Mavi Şimşekler, Buharlı Lokomotif ve Kanatlar)
            cid.contains("ADS") || sName == "ADS" || cName.contains("ADANA DEMİR") || cName.contains("ADANA DEMIR") -> ClubVisualIdentity(
                normalizedId = "TSL_ADS",
                pattern = CrestPattern.ROUNDEL_RING,
                isRoundel = true,
                specialEmblem = "LOCOMOTIVE",
                foundationYear = 1940
            )
            // BAŞAKŞEHİR FK (Turuncu-Lacivert Yuvarlak, Baykuş Silüeti)
            cid.contains("IBFK") || sName == "IBFK" || cName.contains("BAŞAKŞEHİR") || cName.contains("BASAKSEHIR") -> ClubVisualIdentity(
                normalizedId = "TSL_IBFK",
                pattern = CrestPattern.ROUNDEL_RING,
                isRoundel = true,
                specialEmblem = "OWL",
                foundationYear = 1990
            )
            // EYÜPSPOR (Eflatun-Sarı Tarihi Yuvarlak Arma)
            cid.contains("EYU") || sName == "EYU" || cName.contains("EYÜPSPOR") || cName.contains("EYUPSPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_EYU",
                pattern = CrestPattern.ROUNDEL_RING,
                isRoundel = true,
                foundationYear = 1919
            )
            // KASIMPAŞA (Lacivert-Beyaz, Tarihi Denizci Çapası)
            cid.contains("KAS") || sName == "KAS" || cName.contains("KASIMPAŞA") || cName.contains("KASIMPASA") -> ClubVisualIdentity(
                normalizedId = "TSL_KAS",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                specialEmblem = "ANCHOR",
                foundationYear = 1921
            )
            // SİVASSPOR (Kırmızı-Beyaz Bölmeli, 3 Yıldız)
            cid.contains("SIV") || sName == "SIV" || cName.contains("SİVASSPOR") || cName.contains("SIVASSPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_SIV",
                pattern = CrestPattern.BIPARTITE_HALF,
                specialEmblem = "STAR",
                foundationYear = 1967
            )
            // KAYSERİSPOR (Sarı-Kırmızı Bölmeli, Erciyes Dağı Silüeti)
            cid.contains("KAY") || sName == "KAY" || cName.contains("KAYSERİSPOR") || cName.contains("KAYSERISPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_KAY",
                pattern = CrestPattern.BIPARTITE_HALF,
                specialEmblem = "MOUNTAIN",
                foundationYear = 1966
            )
            // ÇAYKUR RİZESPOR (Yeşil-Mavi Çift Şerit, Çay Yaprağı)
            cid.contains("CRS") || sName == "CRS" || cName.contains("RİZESPOR") || cName.contains("RIZESPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_CRS",
                pattern = CrestPattern.HORIZONTAL_CHEVRON,
                specialEmblem = "LEAF",
                foundationYear = 1953
            )
            // ALANYASPOR (Turuncu-Yeşil, Tarihi Kızılkule Kalesi)
            cid.contains("ALN") || sName == "ALN" || cName.contains("ALANYASPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_ALN",
                pattern = CrestPattern.HORIZONTAL_CHEVRON,
                specialEmblem = "TOWER",
                foundationYear = 1948
            )
            // GAZİANTEP FK (Kırmızı-Siyah Çubuklu, Şahin/Kartal)
            cid.contains("GFK") || sName == "GFK" || cName.contains("GAZİANTEP") || cName.contains("GAZIANTEP") -> ClubVisualIdentity(
                normalizedId = "TSL_GFK",
                pattern = CrestPattern.VERTICAL_STRIPES,
                specialEmblem = "EAGLE",
                foundationYear = 1988
            )
            // BODRUM FK (Yeşil-Beyaz Ege Yuvarlak Arması)
            cid.contains("BOD") || sName == "BOD" || cName.contains("BODRUM") -> ClubVisualIdentity(
                normalizedId = "TSL_BOD",
                pattern = CrestPattern.ROUNDEL_RING,
                isRoundel = true,
                foundationYear = 1931
            )
            // HATAYSPOR (Bordo-Beyaz, Defne Yaprağı)
            cid.contains("HAT") || sName == "HAT" || cName.contains("HATAYSPOR") -> ClubVisualIdentity(
                normalizedId = "TSL_HAT",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                specialEmblem = "LEAF",
                foundationYear = 1967
            )
            // KARŞIYAKA (Yeşil-Kırmızı Çubuklu, Orijinal Ay-Yıldız Taşıma Beratı)
            cid.contains("KSK") || sName == "KSK" || cName.contains("KARŞIYAKA") || cName.contains("KARSIYAKA") -> ClubVisualIdentity(
                normalizedId = "TFF3_KSK",
                pattern = CrestPattern.VERTICAL_STRIPES,
                specialEmblem = "TURKISH_FLAG",
                foundationYear = 1912
            )
            // AMED SK (Yeşil-Kırmızı-Beyaz Yuvarlak, Mezopotamya Güneşi)
            cid.contains("AMD") || sName == "AMD" || cName.contains("AMED") -> ClubVisualIdentity(
                normalizedId = "TFF1_AMD",
                pattern = CrestPattern.ROUNDEL_RING,
                isRoundel = true,
                specialEmblem = "SUNBURST",
                foundationYear = 1990
            )
            // GENÇLERBİRLİĞİ (Kırmızı-Siyah Asırlık Başkent Çınarı, Ankara Yıldızı)
            cid.contains("GEN") || sName == "GEN" || cName.contains("GENÇLERBİRLİĞİ") || cName.contains("GENCLERBIRLIGI") -> ClubVisualIdentity(
                normalizedId = "TFF1_GEN",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                specialEmblem = "STAR",
                foundationYear = 1923
            )
            // FATİH KARAGÜMRÜK (Kırmızı-Siyah Çubuklu)
            cid.contains("FKG") || sName == "FKG" || cName.contains("KARAGÜMRÜK") || cName.contains("KARAGUMRUK") -> ClubVisualIdentity(
                normalizedId = "TFF1_FKG",
                pattern = CrestPattern.VERTICAL_STRIPES,
                foundationYear = 1926
            )
            // BANDIRMASPOR (Bordo-Beyaz Bölmeli)
            cid.contains("BAN") || sName == "BAN" || cName.contains("BANDIRMA") -> ClubVisualIdentity(
                normalizedId = "TFF1_BAN",
                pattern = CrestPattern.BIPARTITE_HALF,
                foundationYear = 1965
            )
            // ÇORUM FK (Kırmızı-Siyah Çubuklu)
            cid.contains("ÇOR") || cid.contains("COR") || sName == "ÇOR" || sName == "COR" || cName.contains("ÇORUM") || cName.contains("CORUM") -> ClubVisualIdentity(
                normalizedId = "TFF1_COR",
                pattern = CrestPattern.VERTICAL_STRIPES,
                foundationYear = 1967
            )
            // ERZURUMSPOR FK (Mavi-Beyaz Çubuklu, Çift Başlı Kartal)
            cid.contains("ERZ") || sName == "ERZ" || cName.contains("ERZURUM") -> ClubVisualIdentity(
                normalizedId = "TFF1_ERZ",
                pattern = CrestPattern.VERTICAL_STRIPES,
                specialEmblem = "SELJUK_EAGLE",
                foundationYear = 1968
            )
            // ADANASPOR (Turuncu-Beyaz Çubuklu)
            cid.contains("ADN") || sName == "ADN" || cName.contains("ADANASPOR") -> ClubVisualIdentity(
                normalizedId = "TFF1_ADN",
                pattern = CrestPattern.VERTICAL_STRIPES,
                foundationYear = 1954
            )
            // BOLUSPOR (Kırmızı-Beyaz Çubuklu)
            cid.contains("BOL") || sName == "BOL" || cName.contains("BOLUSPOR") -> ClubVisualIdentity(
                normalizedId = "TFF1_BOL",
                pattern = CrestPattern.VERTICAL_STRIPES,
                foundationYear = 1965
            )
            // ŞANLIURFASPOR (Sarı-Yeşil Bölmeli)
            cid.contains("URF") || sName == "URF" || cName.contains("ŞANLIURFA") || cName.contains("SANLIURFA") -> ClubVisualIdentity(
                normalizedId = "TFF1_URF",
                pattern = CrestPattern.BIPARTITE_HALF,
                foundationYear = 1969
            )
            // BATMAN PETROLSPOR (Kırmızı-Beyaz, Petrol Sondaj Kulesi)
            cid.contains("BTM") || sName == "BTM" || cName.contains("BATMAN") -> ClubVisualIdentity(
                normalizedId = "TFF2_BTM",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                specialEmblem = "DERRICK",
                foundationYear = 1960
            )
            // ELAZIĞSPOR (Bordo-Beyaz Bölmeli)
            cid.contains("ELZ") || sName == "ELZ" || cName.contains("ELAZIĞ") || cName.contains("ELAZIG") -> ClubVisualIdentity(
                normalizedId = "TFF2_ELZ",
                pattern = CrestPattern.BIPARTITE_HALF,
                foundationYear = 1967
            )
            // SARIYER SK (Lacivert-Beyaz Çubuklu, Boğazın Martısı)
            cid.contains("SRY") || sName == "SRY" || cName.contains("SARIYER") -> ClubVisualIdentity(
                normalizedId = "TFF2_SRY",
                pattern = CrestPattern.VERTICAL_STRIPES,
                specialEmblem = "SEAGULL",
                foundationYear = 1940
            )
            // ALTAY İZMİR (Siyah-Beyaz Çubuklu Büyük Altay)
            cid.contains("ALT") || sName == "ALT" || cName.contains("ALTAY") -> ClubVisualIdentity(
                normalizedId = "TFF2_ALT",
                pattern = CrestPattern.VERTICAL_STRIPES,
                foundationYear = 1914
            )
            // VEFA 1908 (Yeşil-Beyaz Asırlık SAL Efsanesi)
            cid.contains("VEF") || sName == "VEF" || cName.contains("VEFA") -> ClubVisualIdentity(
                normalizedId = "SAL_VEF",
                pattern = CrestPattern.SOLID_WITH_EMBLEM,
                foundationYear = 1908
            )
            // FERİKÖY SK (Kırmızı-Beyaz)
            cid.contains("FER") || sName == "FER" || cName.contains("FERİKÖY") || cName.contains("FERIKOY") -> ClubVisualIdentity(
                normalizedId = "SAL_FER",
                pattern = CrestPattern.VERTICAL_STRIPES,
                foundationYear = 1927
            )
            // İZMİRSPOR (Lacivert-Beyaz Şimşekler)
            cid.contains("IZM") || sName == "IZM" || cName.contains("İZMİRSPOR") || cName.contains("IZMIRSPOR") -> ClubVisualIdentity(
                normalizedId = "SAL_IZM",
                pattern = CrestPattern.VERTICAL_STRIPES,
                foundationYear = 1923
            )
            // DİĞER TÜM KULÜPLER İÇİN DETERMINİSTİK HERALDİK MODELLEME
            else -> {
                val seed = abs((cid + sName + cName).hashCode())
                val pattern = when (seed % 6) {
                    0 -> CrestPattern.VERTICAL_STRIPES
                    1 -> CrestPattern.DIAGONAL_SASH
                    2 -> CrestPattern.BIPARTITE_HALF
                    3 -> CrestPattern.HORIZONTAL_CHEVRON
                    4 -> CrestPattern.QUARTERED
                    else -> CrestPattern.SOLID_WITH_EMBLEM
                }
                val isRound = (seed % 5) == 0
                ClubVisualIdentity(
                    normalizedId = cid.ifBlank { sName },
                    pattern = if (isRound) CrestPattern.ROUNDEL_RING else pattern,
                    isRoundel = isRound,
                    specialEmblem = if (seed % 3 == 0) "STAR" else null,
                    foundationYear = 1920 + (seed % 75)
                )
            }
        }
    }
}

/**
 * 137 Türk Kulübü ve Dünya / Şampiyonlar Ligi Devleri İçin Yüksek Çözünürlüklü Vektör Arma Bileşeni
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
            } else if (identity.isOval) {
                drawOvalCrest(
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
                            fontSize = (size.value * 0.16f).coerceIn(6f, 14f).sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = (size.value * 0.16f).coerceIn(6f, 14f).sp
                        )
                    }
                }
            }

            // Kulüp Kısa Adı (Monogram)
            Text(
                text = displayLetters,
                fontFamily = BarlowCondensedFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = when {
                    displayLetters.length >= 4 -> (size.value * 0.22f).coerceIn(7f, 16f).sp
                    displayLetters.length == 3 -> (size.value * 0.27f).coerceIn(8f, 18f).sp
                    else -> (size.value * 0.32f).coerceIn(9f, 22f).sp
                },
                letterSpacing = 0.4.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            // Kuruluş Yılı (Büyük armalarda)
            if (size >= 52.dp && identity.foundationYear != null) {
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
    // Gerçek kulüp logosu varsa vektör çizim yerine doğrudan gerçek PNG gösterilir.
    val realCrestRes = RealClubCrestRegistry.resolve(club.id, club.shortName, club.name)
    if (realCrestRes != null) {
        Image(
            painter = painterResource(id = realCrestRes),
            contentDescription = club.name,
            modifier = modifier.size(size),
            contentScale = ContentScale.Fit
        )
        return
    }
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

    // 1. Zemin Gölgelendirmesi
    drawPath(
        path = shieldPath,
        brush = Brush.verticalGradient(
            listOf(primary, primary.copy(alpha = 0.9f), BackgroundPrimary.copy(alpha = 0.95f))
        )
    )

    // 2. Heraldik Desen
    when (identity.pattern) {
        CrestPattern.VERTICAL_STRIPES -> {
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
        CrestPattern.HORIZONTAL_STRIPES -> {
            // Sporting CP yeşil-beyaz yatay çizgiler
            val stripeH = h * 0.14f
            for (i in 0..4) {
                if (i % 2 == 1) {
                    drawRect(
                        color = secondary.copy(alpha = 0.92f),
                        topLeft = Offset(w * 0.12f, h * 0.15f + i * stripeH),
                        size = Size(w * 0.76f, stripeH * 0.75f)
                    )
                }
            }
        }
        CrestPattern.CROSS_CANTON -> {
            // Barcelona: Sol üstte St George Haçı, Sağ üstte Katalan şeritleri, altta blaugrana şeritler
            drawRect(
                color = Color.White,
                topLeft = Offset(w * 0.12f, h * 0.12f),
                size = Size(w * 0.36f, h * 0.32f)
            )
            // Kırmızı haç
            drawRect(
                color = Color(0xFFC0392B),
                topLeft = Offset(w * 0.27f, h * 0.12f),
                size = Size(w * 0.06f, h * 0.32f)
            )
            drawRect(
                color = Color(0xFFC0392B),
                topLeft = Offset(w * 0.12f, h * 0.25f),
                size = Size(w * 0.36f, h * 0.06f)
            )
            // Katalan Senyera (Sağ üst)
            drawRect(
                color = Color(0xFFF1C40F),
                topLeft = Offset(w * 0.52f, h * 0.12f),
                size = Size(w * 0.36f, h * 0.32f)
            )
            for (k in 0..2) {
                drawRect(
                    color = Color(0xFFC0392B),
                    topLeft = Offset(w * 0.56f + k * w * 0.10f, h * 0.12f),
                    size = Size(w * 0.04f, h * 0.32f)
                )
            }
            // Alt kısım blaugrana dikey şeritler
            for (k in 0..3) {
                drawRect(
                    color = if (k % 2 == 0) Color(0xFF1B4F72) else Color(0xFF922B21),
                    topLeft = Offset(w * 0.15f + k * w * 0.18f, h * 0.48f),
                    size = Size(w * 0.18f, h * 0.45f)
                )
            }
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
            drawCircle(
                color = secondary.copy(alpha = 0.22f),
                radius = w * 0.32f,
                center = Offset(w * 0.5f, h * 0.48f)
            )
        }
    }

    // 3. Özel Mini Sembol Çizimi
    drawSpecialEmblem(w = w, h = h, emblem = identity.specialEmblem, isRoundel = false)

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

    // 5. Metalik Kenarlık (Gold/Platinum Rim)
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

    // Özel Bavyera Dama Deseni (Bayern Münih)
    if (identity.specialEmblem == "BAVARIAN_CHECKERS") {
        drawCircle(color = Color(0xFFC0392B), radius = radius * 0.92f, center = center)
        drawCircle(color = Color.White, radius = radius * 0.65f, center = center)
        // Mavi/Beyaz eşkenar dama efekti
        val checkerRadius = radius * 0.62f
        drawCircle(color = Color(0xFF1E88E5), radius = checkerRadius, center = center)
        for (row in -2..2) {
            val yOffset = row * checkerRadius * 0.4f
            drawLine(
                color = Color.White,
                start = Offset(center.x - checkerRadius * 0.8f, center.y + yOffset),
                end = Offset(center.x + checkerRadius * 0.8f, center.y + yOffset),
                strokeWidth = checkerRadius * 0.18f
            )
        }
    } else {
        // Standart İç Çember
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
    }

    // Özel Sembol
    drawSpecialEmblem(w = w, h = h, emblem = identity.specialEmblem, isRoundel = true)

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
 * Oval Arma Çizimi (AC Milan vb.)
 */
private fun DrawScope.drawOvalCrest(
    w: Float,
    h: Float,
    primary: Color,
    secondary: Color,
    identity: ClubVisualIdentity
) {
    val center = Offset(w * 0.5f, h * 0.5f)
    val ovalPath = Path().apply {
        moveTo(w * 0.5f, h * 0.04f)
        cubicTo(w * 0.90f, h * 0.04f, w * 0.90f, h * 0.96f, w * 0.5f, h * 0.96f)
        cubicTo(w * 0.10f, h * 0.96f, w * 0.10f, h * 0.04f, w * 0.5f, h * 0.04f)
        close()
    }

    drawPath(path = ovalPath, color = primary)

    // Sol taraf kırmızı-siyah şeritler
    for (i in 0..2) {
        drawRect(
            color = Color(0xFFC0392B),
            topLeft = Offset(w * 0.16f + i * w * 0.10f, h * 0.20f),
            size = Size(w * 0.05f, h * 0.60f)
        )
    }

    // Sağ taraf beyaz zemin üstüne kırmızı Milano haçı
    drawRect(
        color = Color.White,
        topLeft = Offset(w * 0.52f, h * 0.20f),
        size = Size(w * 0.32f, h * 0.60f)
    )
    drawRect(
        color = Color(0xFFC0392B),
        topLeft = Offset(w * 0.65f, h * 0.20f),
        size = Size(w * 0.06f, h * 0.60f)
    )
    drawRect(
        color = Color(0xFFC0392B),
        topLeft = Offset(w * 0.52f, h * 0.46f),
        size = Size(w * 0.32f, h * 0.08f)
    )

    // Altın kenarlık
    drawPath(
        path = ovalPath,
        color = BrightGold,
        style = Stroke(width = (w * 0.05f).coerceAtLeast(1.5f))
    )
}

/**
 * Kulüplere Özel Sembol Çizim Fonksiyonu
 */
private fun DrawScope.drawSpecialEmblem(
    w: Float,
    h: Float,
    emblem: String?,
    isRoundel: Boolean
) {
    if (emblem == null) return
    val cy = if (isRoundel) h * 0.5f else h * 0.28f
    val cx = w * 0.5f

    when (emblem) {
        "CROWN" -> {
            // Real Madrid Kraliyet Tacı
            val crownPath = Path().apply {
                moveTo(cx - w * 0.24f, cy - h * 0.08f)
                lineTo(cx - w * 0.16f, cy + h * 0.06f)
                lineTo(cx + w * 0.16f, cy + h * 0.06f)
                lineTo(cx + w * 0.24f, cy - h * 0.08f)
                lineTo(cx + w * 0.08f, cy - h * 0.02f)
                lineTo(cx, cy - h * 0.14f)
                lineTo(cx - w * 0.08f, cy - h * 0.02f)
                close()
            }
            drawPath(path = crownPath, color = BrightGold)
            drawCircle(color = Color(0xFFE74C3C), radius = w * 0.03f, center = Offset(cx, cy - h * 0.14f))
            drawCircle(color = Color(0xFF3498DB), radius = w * 0.025f, center = Offset(cx - w * 0.24f, cy - h * 0.08f))
            drawCircle(color = Color(0xFF3498DB), radius = w * 0.025f, center = Offset(cx + w * 0.24f, cy - h * 0.08f))
        }
        "CANNON" -> {
            // Arsenal Top Arabası
            drawRect(
                color = BrightGold,
                topLeft = Offset(cx - w * 0.22f, cy - h * 0.04f),
                size = Size(w * 0.44f, h * 0.08f)
            )
            drawCircle(color = Color.DarkGray, radius = w * 0.08f, center = Offset(cx - w * 0.08f, cy + h * 0.06f))
            drawCircle(color = BrightGold, radius = w * 0.04f, center = Offset(cx - w * 0.08f, cy + h * 0.06f))
        }
        "LIVERBIRD" -> {
            // Liverpool Kuşu
            val birdPath = Path().apply {
                moveTo(cx, cy - h * 0.10f)
                cubicTo(cx + w * 0.12f, cy - h * 0.04f, cx + w * 0.14f, cy + h * 0.08f, cx, cy + h * 0.12f)
                cubicTo(cx - w * 0.14f, cy + h * 0.08f, cx - w * 0.12f, cy - h * 0.04f, cx, cy - h * 0.10f)
                close()
            }
            drawPath(path = birdPath, color = BrightGold)
        }
        "RED_DEVIL" -> {
            // Manchester United Kırmızı Şeytan
            drawCircle(color = Color(0xFFC0392B), radius = w * 0.08f, center = Offset(cx, cy - h * 0.04f))
            drawLine(
                color = BrightGold,
                start = Offset(cx + w * 0.10f, cy - h * 0.12f),
                end = Offset(cx + w * 0.10f, cy + h * 0.12f),
                strokeWidth = w * 0.03f
            )
        }
        "SHIP" -> {
            // Man City Altın Gemi
            val shipPath = Path().apply {
                moveTo(cx - w * 0.18f, cy + h * 0.02f)
                lineTo(cx + w * 0.18f, cy + h * 0.02f)
                lineTo(cx + w * 0.12f, cy + h * 0.08f)
                lineTo(cx - w * 0.12f, cy + h * 0.08f)
                close()
            }
            drawPath(path = shipPath, color = BrightGold)
            // Yelken
            val sailPath = Path().apply {
                moveTo(cx, cy - h * 0.10f)
                lineTo(cx + w * 0.14f, cy)
                lineTo(cx, cy)
                close()
            }
            drawPath(path = sailPath, color = Color.White)
        }
        "LION_RAMPANT" -> {
            // Chelsea / Sporting CP Şahlanan Aslan
            drawCircle(color = BrightGold, radius = w * 0.09f, center = Offset(cx, cy))
        }
        "EIFFEL_TOWER" -> {
            // PSG Kırmızı Eyfel Kulesi
            val towerPath = Path().apply {
                moveTo(cx, cy - h * 0.14f)
                lineTo(cx + w * 0.14f, cy + h * 0.12f)
                lineTo(cx + w * 0.08f, cy + h * 0.12f)
                lineTo(cx, cy - h * 0.02f)
                lineTo(cx - w * 0.08f, cy + h * 0.12f)
                lineTo(cx - w * 0.14f, cy + h * 0.12f)
                close()
            }
            drawPath(path = towerPath, color = Color(0xFFC0392B))
        }
        "JUVENTUS_J" -> {
            // Juventus Çift J
            drawLine(
                color = Color.White,
                start = Offset(cx - w * 0.08f, cy - h * 0.12f),
                end = Offset(cx - w * 0.08f, cy + h * 0.08f),
                strokeWidth = w * 0.06f
            )
            drawLine(
                color = Color.White,
                start = Offset(cx + w * 0.06f, cy - h * 0.12f),
                end = Offset(cx + w * 0.06f, cy + h * 0.12f),
                strokeWidth = w * 0.06f
            )
        }
        "TURKISH_FLAG" -> {
            // Beşiktaş ve Karşıyaka için Ay-Yıldız
            val flagCenter = Offset(cx, cy)
            val flagRadius = w * 0.14f
            drawCircle(color = Color(0xFFE74C3C), radius = flagRadius, center = flagCenter)
            drawCircle(color = Color.White, radius = flagRadius * 0.65f, center = Offset(flagCenter.x - flagRadius * 0.15f, flagCenter.y))
            drawCircle(color = Color(0xFFE74C3C), radius = flagRadius * 0.55f, center = Offset(flagCenter.x - flagRadius * 0.05f, flagCenter.y))
            drawCircle(color = Color.White, radius = flagRadius * 0.20f, center = Offset(flagCenter.x + flagRadius * 0.32f, flagCenter.y))
        }
        "LION" -> {
            // Galatasaray Aslanı
            drawCircle(color = BrightGold.copy(alpha = 0.85f), radius = w * 0.10f, center = Offset(cx, cy))
        }
        "OAK_LEAF" -> {
            // Fenerbahçe Palamut Dalı Yaprağı
            val leafPath = Path().apply {
                moveTo(cx, cy - h * 0.10f)
                cubicTo(cx + w * 0.12f, cy - h * 0.04f, cx + w * 0.10f, cy + h * 0.08f, cx, cy + h * 0.10f)
                cubicTo(cx - w * 0.10f, cy + h * 0.08f, cx - w * 0.12f, cy - h * 0.04f, cx, cy - h * 0.10f)
                close()
            }
            drawPath(path = leafPath, color = Color(0xFF27AE60))
        }
        "TS_WAVE" -> {
            // Trabzonspor Dalgası
            val wavePath = Path().apply {
                moveTo(cx - w * 0.20f, cy)
                cubicTo(cx - w * 0.10f, cy - h * 0.08f, cx + w * 0.10f, cy + h * 0.08f, cx + w * 0.20f, cy)
            }
            drawPath(path = wavePath, color = Color(0xFF5DADE2), style = Stroke(width = w * 0.05f))
        }
        "ATATURK" -> {
            // Samsunspor Atatürk Silüeti
            drawCircle(color = Color.White, radius = w * 0.10f, center = Offset(cx, cy))
            drawCircle(color = Color(0xFFC0392B), radius = w * 0.06f, center = Offset(cx, cy))
        }
        "SELJUK_EAGLE" -> {
            // Konyaspor & Erzurumspor Çift Başlı Selçuklu Kartalı
            drawLine(
                color = Color.White,
                start = Offset(cx - w * 0.18f, cy - h * 0.05f),
                end = Offset(cx + w * 0.18f, cy - h * 0.05f),
                strokeWidth = w * 0.04f
            )
            drawCircle(color = Color.White, radius = w * 0.07f, center = Offset(cx, cy))
        }
        "LOCOMOTIVE" -> {
            // Adana Demirspor Buharlı Lokomotif
            drawRect(
                color = Color(0xFF3498DB),
                topLeft = Offset(cx - w * 0.14f, cy - h * 0.05f),
                size = Size(w * 0.28f, h * 0.10f)
            )
            drawCircle(color = BrightGold, radius = w * 0.05f, center = Offset(cx - w * 0.08f, cy + h * 0.05f))
            drawCircle(color = BrightGold, radius = w * 0.05f, center = Offset(cx + w * 0.08f, cy + h * 0.05f))
        }
        "DERRICK" -> {
            // Batman Petrolspor Petrol Sondaj Kulesi
            val derrickPath = Path().apply {
                moveTo(cx, cy - h * 0.12f)
                lineTo(cx + w * 0.14f, cy + h * 0.12f)
                lineTo(cx - w * 0.14f, cy + h * 0.12f)
                close()
            }
            drawPath(path = derrickPath, color = Color.White, style = Stroke(width = w * 0.03f))
        }
        "SEAGULL" -> {
            // Sarıyer Martı Silüeti
            val seagullPath = Path().apply {
                moveTo(cx - w * 0.20f, cy)
                cubicTo(cx - w * 0.10f, cy - h * 0.08f, cx, cy, cx, cy)
                cubicTo(cx, cy, cx + w * 0.10f, cy - h * 0.08f, cx + w * 0.20f, cy)
            }
            drawPath(path = seagullPath, color = Color.White, style = Stroke(width = w * 0.04f))
        }
        "CROCODILE" -> {
            // Bursaspor Timsah
            drawRect(
                color = Color(0xFF27AE60),
                topLeft = Offset(cx - w * 0.18f, cy - h * 0.03f),
                size = Size(w * 0.36f, h * 0.06f)
            )
        }
        "STAR" -> {
            drawCircle(color = BrightGold.copy(alpha = 0.85f), radius = w * 0.08f, center = Offset(cx, cy))
        }
        "MINARET" -> {
            drawRect(
                color = Color.White,
                topLeft = Offset(cx - w * 0.04f, cy - h * 0.12f),
                size = Size(w * 0.08f, h * 0.24f)
            )
        }
        "SUNBURST" -> {
            drawCircle(color = BrightGold, radius = w * 0.09f, center = Offset(cx, cy))
        }
        "ANCHOR" -> {
            drawLine(
                color = Color.White,
                start = Offset(cx, cy - h * 0.10f),
                end = Offset(cx, cy + h * 0.10f),
                strokeWidth = w * 0.04f
            )
        }
        "LEAF" -> {
            drawCircle(color = Color(0xFF2ECC71), radius = w * 0.08f, center = Offset(cx, cy))
        }
        "TOWER" -> {
            drawRect(
                color = BrightGold,
                topLeft = Offset(cx - w * 0.08f, cy - h * 0.08f),
                size = Size(w * 0.16f, h * 0.16f)
            )
        }
        "MOUNTAIN" -> {
            val mountainPath = Path().apply {
                moveTo(cx, cy - h * 0.10f)
                lineTo(cx + w * 0.18f, cy + h * 0.08f)
                lineTo(cx - w * 0.18f, cy + h * 0.08f)
                close()
            }
            drawPath(path = mountainPath, color = Color.White)
        }
        else -> {
            drawCircle(color = BrightGold.copy(alpha = 0.5f), radius = w * 0.06f, center = Offset(cx, cy))
        }
    }
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
            Image(
                painter = painterResource(id = R.drawable.logo_super_lig),
                contentDescription = "Trendyol Süper Lig",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Fit
            )
        } else {
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
            LeagueCrestBadge(leagueId = leagueId, leagueName = dateLabel, size = size)
        }
    }
}
