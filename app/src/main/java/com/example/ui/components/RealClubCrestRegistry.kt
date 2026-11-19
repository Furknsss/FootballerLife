package com.example.ui.components

import androidx.annotation.DrawableRes
import com.example.R

/**
 * Gerçek kulüp logoları (football-logos.cc / Wikimedia kaynaklı, 700x700 şeffaf PNG).
 *
 * Kulüp adı/kısa adı/ID üzerinden gerçek crest drawable'ını çözümler.
 * Eşleşme bulunamazsa null döner ve ClubCrestBadge vektör çizime düşer.
 */
object RealClubCrestRegistry {

    /**
     * Anahtar: küçük harfli, Türkçe karakterleri sadeleştirilmiş kulüp adı veya kısa ad.
     * Değer: drawable-nodpi içindeki gerçek logo kaynağı.
     */
    private val byName: Map<String, Int> = mapOf(
        // --- Süper Lig ---
        "galatasaray" to R.drawable.crest_galatasaray, "gs" to R.drawable.crest_galatasaray,
        "fenerbahçe" to R.drawable.crest_fenerbahce, "fenerbahce" to R.drawable.crest_fenerbahce, "fb" to R.drawable.crest_fenerbahce,
        "beşiktaş" to R.drawable.crest_besiktas, "besiktas" to R.drawable.crest_besiktas, "bjk" to R.drawable.crest_besiktas,
        "trabzonspor" to R.drawable.crest_trabzonspor, "ts" to R.drawable.crest_trabzonspor,
        "samsunspor" to R.drawable.crest_samsunspor,
        "eyüpspor" to R.drawable.crest_eyupspor, "eyupspor" to R.drawable.crest_eyupspor,
        "başakşehir" to R.drawable.crest_basaksehir, "basaksehir" to R.drawable.crest_basaksehir,
        "istanbul başakşehir" to R.drawable.crest_basaksehir,
        "kasımpaşa" to R.drawable.crest_kasimpasa, "kasimpasa" to R.drawable.crest_kasimpasa,
        "konyaspor" to R.drawable.crest_konyaspor,
        "rizespor" to R.drawable.crest_rizespor, "çaykur rizespor" to R.drawable.crest_rizespor,
        "alanyaspor" to R.drawable.crest_alanyaspor,
        "gaziantep" to R.drawable.crest_gaziantep, "gaziantep fk" to R.drawable.crest_gaziantep,
        "gençlerbirliği" to R.drawable.crest_genclerbirligi, "genclerbirligi" to R.drawable.crest_genclerbirligi,
        "göztepe" to R.drawable.crest_goztepe_izmir, "goztepe" to R.drawable.crest_goztepe_izmir,
        "kocaelispor" to R.drawable.crest_kocaelispor,
        "amed" to R.drawable.crest_amed, "amed sk" to R.drawable.crest_amed, "diyarbakır" to R.drawable.crest_amed,
        "çorum" to R.drawable.crest_corum, "corum" to R.drawable.crest_corum,
        "erzurumspor" to R.drawable.crest_erzurumspor,

        // --- 1. Lig ---
        "fatih karagümrük" to R.drawable.crest_fatih_karagumruk, "karagümrük" to R.drawable.crest_fatih_karagumruk,
        "sakaryaspor" to R.drawable.crest_sakaryaspor,
        "mke ankaragücü" to R.drawable.crest_ankaragucu, "ankragücü" to R.drawable.crest_ankaragucu, "ankargucu" to R.drawable.crest_ankaragucu,
        "ığdır" to R.drawable.crest_igdir, "igdir" to R.drawable.crest_igdir,
        "bandırmaspor" to R.drawable.crest_bandirmaspor, "bandirmaspor" to R.drawable.crest_bandirmaspor,
        "boluspor" to R.drawable.crest_boluspor,
        "manisa" to R.drawable.crest_manisa, "manisasp" to R.drawable.crest_manisa,
        "keçiörengücü" to R.drawable.crest_keciorengucu, "keciorengucu" to R.drawable.crest_keciorengucu,
        "pendikspor" to R.drawable.crest_pendikspor,
        "adanaspor" to R.drawable.crest_adanaspor,
        "adana demirspor" to R.drawable.crest_adana_demirspor,
        "şanlıurfaspor" to R.drawable.crest_sanliurfaspor, "sanliurfaspor" to R.drawable.crest_sanliurfaspor,
        "hatayspor" to R.drawable.crest_hatayspor,
        "kayserispor" to R.drawable.crest_kayserispor,
        "antalyaspor" to R.drawable.crest_antalyaspor,
        "sivasspor" to R.drawable.crest_sivasspor,
        "umraniyespor" to R.drawable.crest_umraniyespor, "Ümraniyespor" to R.drawable.crest_umraniyespor,
        "iskenderunspor" to R.drawable.crest_iskenderunspor,
        "iğdır fk" to R.drawable.crest_igdir, "igdir fk" to R.drawable.crest_igdir, "iğdır" to R.drawable.crest_igdir,
        "erokspor" to R.drawable.crest_esenler_erokspor,
        "istanbulspor" to R.drawable.crest_istanbulspor,
        "van spor fk" to R.drawable.crest_van_spor, "van spor" to R.drawable.crest_van_spor,
        "pendikspor" to R.drawable.crest_pendikspor,
        "mardin fosfatspor" to R.drawable.crest_mardin_1969_spor,
        "alagöz holding iğdır fk" to R.drawable.crest_igdir,
        "istanbulspor" to R.drawable.crest_istanbulspor, "i̇stanbulspor" to R.drawable.crest_istanbulspor,
        "siltaş yapı pendikspor" to R.drawable.crest_pendikspor,
        "erzincanspor" to R.drawable.crest_erzincanspor,
        "karabükspor" to R.drawable.crest_kardemir_karabukspor, "kardemir karabükspor" to R.drawable.crest_kardemir_karabukspor,
        "bursaspor" to R.drawable.crest_bursaspor,
        "karşıyaka" to R.drawable.crest_karsiyaka_sk, "karsiyaka" to R.drawable.crest_karsiyaka_sk,

        // --- 2./3. Lig & amatör ---
        "altay" to R.drawable.crest_altay_sk,
        "tuzlaspor" to R.drawable.crest_tuzlaspor,
        "sariyer" to R.drawable.crest_sariyer, "sarıyer" to R.drawable.crest_sariyer,
        "kastamonuspor" to R.drawable.crest_kastamonuspor,
        "menemen" to R.drawable.crest_menemen_fk,
        "erbaaspor" to R.drawable.crest_erbaaspor,
        "adiyamanspor" to R.drawable.crest_adiyamanspor,
        "batman petrolspor" to R.drawable.crest_batman_petrolspor,
        "belediyespor" to R.drawable.crest_68_aksaray_belediyespor,
        "bodrum" to R.drawable.crest_bodrum_fk,
        "karacabey" to R.drawable.crest_karacabey_belediye_spor,
        "serik" to R.drawable.crest_serik_belediyespor,
        "muğlaspor" to R.drawable.crest_muglaspor, "muglaspor" to R.drawable.crest_muglaspor,
        "kırklarelispor" to R.drawable.crest_kirklarelispor, "kirklarelispor" to R.drawable.crest_kirklarelispor,
        "inegolspor" to R.drawable.crest_inegolspor,
        "#12 bingölspor" to R.drawable.crest_12_bingolspor,
        "1461 trabzon" to R.drawable.crest_1461_trabzon,
        "52 orduspor" to R.drawable.crest_52_orduspor,
        "68 aksaray" to R.drawable.crest_68_aksaray_belediyespor,
    )

    /** Sponsorlu kulüp adlarındaki isim sponsoru öneklerini kaldırır. */
    private val sponsorPrefixes = listOf(
        "rams ", "tümosan ", "tumosan ", "corendon ", "onvo ", "bellona ", "sipay ",
        "alagöz holding ", "alagoz holding ", "esenler ", "net global ", "siltaş yapı ",
        "siltas yapi ", "mardin fosfat ", "ankara ", "teksüt ", "teksut ", "ahlatcı ",
        "ahlatci ", "central hospital ", "alagöz ", "alagoz ", "büyükşehir belediye "
    )

    /** Arama için Türkçe karakterleri, özel işaretleri ve sponsor öneklerini sadeleştirir. */
    private fun normalize(input: String): String {
        var s = input.lowercase()
            .replace('ı', 'i').replace('ğ', 'g').replace('ü', 'u')
            .replace('ş', 's').replace('ö', 'o').replace('ç', 'c')
            .replace("spor kulübü", "").replace("spor kulubu", "")
            .trim()
        for (p in sponsorPrefixes) {
            if (s.startsWith(p)) {
                s = s.removePrefix(p).trim()
                break
            }
        }
        return s
    }

    private val normalizedMap: Map<String, Int> by lazy {
        byName.mapKeys { normalize(it.key) }
    }

    @DrawableRes
    fun resolve(clubId: String?, shortName: String?, clubName: String?): Int? {
        val candidates = listOfNotNull(clubName, shortName)
        for (c in candidates) {
            val key = normalize(c)
            normalizedMap[key]?.let { return it }
            // "Fenerbahçe SK", "Galatasaray A.Ş." gibi ek ifadeleri kırp
            val firstWord = key.split(' ').firstOrNull()
            if (firstWord != null) normalizedMap[firstWord]?.let { return it }
        }
        // ID tabanlı özel eşleşmeler
        return when {
            clubId?.contains("GS", ignoreCase = true) == true -> R.drawable.crest_galatasaray
            clubId?.contains("FB", ignoreCase = true) == true -> R.drawable.crest_fenerbahce
            clubId?.contains("BJK", ignoreCase = true) == true -> R.drawable.crest_besiktas
            else -> null
        }
    }
}
