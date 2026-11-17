package com.example.ui.screens.setup

import com.example.model.PlayerAppearance
import com.example.model.PlayerAttributes
import com.example.model.Position
import com.example.model.PreferredFoot

/**
 * Tactical role archetype defining specific attribute weightings and tendencies.
 */
data class RoleArchetype(
    val id: String,
    val name: String,
    val position: Position,
    val badge: String,
    val description: String,
    val keyStrengths: List<String>,
    val baseAttributes: PlayerAttributes
)

/**
 * Origin and backstory defining player's football roots and passive tendencies.
 */
data class OriginBackstory(
    val id: String,
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val lore: String,
    val perkDescription: String,
    val attributeModifier: (PlayerAttributes) -> PlayerAttributes
)

/**
 * Allocatable attribute category for the creator points pool.
 */
enum class AllocatableStat(
    val code: String,
    val titleTr: String,
    val description: String
) {
    SHOOTING("SUT", "Şut & Bitiricilik", "Ceza sahası içi bitiricilik ve uzaktan şut tekniği"),
    PASSING("PAS", "Pas & Oyun Kurma", "Kısa/uzun pas isabeti, ara pasları ve oyun temposu"),
    DRIBBLING("DRP", "Dripling & Top Hakimiyeti", "Dar alanda çalım, top saklama ve topla hız"),
    FIRST_TOUCH("ILK", "İlk Dokunuş & Kontrol", "Gelen sert pasları yumuşatma ve pozisyon alma hızı"),
    PACE("HIZ", "Sürat & Patlayıcılık", "Maksimum depar hızı ve ani ivmelenme"),
    AGILITY("CVK", "Çeviklik & Denge", "Ani yön değiştirme ve vücut koordinasyonu"),
    STAMINA("KND", "Dayanıklılık & Pres", "90 dakika yüksek tempoda pres yapabilme enerjisi"),
    STRENGTH("GUC", "Fiziksel Güç & İkili Mücadele", "Omuz omuza mücadeleler ve sırtı dönük top koruma"),
    DEFENDING("SAV", "Müdahale & Top Kapma", "Ayaktan top alma, pas arası ve kademe anlayışı"),
    TACTICS("TAK", "Taktiksel Zeka & Pozisyon", "Sahayı okuma, doğru boşluklara kaçma ve savunma yerleşimi"),
    VISION("VIZ", "Görüş & Yaratıcılık", "Savunma arkasına kilit pas atabilme yeteneği")
}

object PlayerCreatorCatalog {

    const val STARTING_POINTS_BUDGET = 25
    const val MIN_STAT_FLOOR = 35
    const val MAX_STAT_CAP = 82

    val origins: List<OriginBackstory> = listOf(
        OriginBackstory(
            id = "ORIGIN_ACADEMY",
            title = "Süper Lig Altyapı Akademisi",
            subtitle = "Modern futbol eğitimi, taktiksel disiplin",
            iconEmoji = "🎓",
            lore = "Köklü bir Süper Lig kulübünün altyapı akademisinde yıllarca taktik, pas temposu ve pozisyon alma disipliniyle yoğruldun.",
            perkDescription = "+4 Taktik, +3 Pas & Vizyon, +2 İlk Dokunuş",
            attributeModifier = {
                it.copy(
                    tactics = (it.tactics + 4).coerceAtMost(MAX_STAT_CAP),
                    passing = (it.passing + 3).coerceAtMost(MAX_STAT_CAP),
                    vision = (it.vision + 3).coerceAtMost(MAX_STAT_CAP),
                    firstTouch = (it.firstTouch + 2).coerceAtMost(MAX_STAT_CAP)
                )
            }
        ),
        OriginBackstory(
            id = "ORIGIN_BAL_SCOUT",
            title = "BAL & Amatör Scout Keşfi",
            subtitle = "Sert zeminlerde bilenmiş hırs ve direnç",
            iconEmoji = "🔎",
            lore = "Amatör liglerin çamurlu sahalarında scoutların dikkatini çektin. Mücadele etmekten asla kaçmayan yılmaz bir karaktere sahipsin.",
            perkDescription = "+4 Dayanıklılık, +4 Güç, +3 Mücadele Azmi",
            attributeModifier = {
                it.copy(
                    stamina = (it.stamina + 4).coerceAtMost(MAX_STAT_CAP),
                    strength = (it.strength + 4).coerceAtMost(MAX_STAT_CAP),
                    defending = (it.defending + 2).coerceAtMost(MAX_STAT_CAP)
                )
            }
        ),
        OriginBackstory(
            id = "ORIGIN_STREET",
            title = "Sokak Futbolundan Yükseliş",
            subtitle = "Doğuştan saf yetenek, kıvrak çalımlar",
            iconEmoji = "🔥",
            lore = "Beton ve toprak semt sahalarında mahalle abileriyle oynayarak büyüdün. Top ayağına yapışıyor, beklenmedik anlarda sihir yaratıyorsun.",
            perkDescription = "+5 Dripling, +4 Çeviklik, +3 İlk Dokunuş",
            attributeModifier = {
                it.copy(
                    dribbling = (it.dribbling + 5).coerceAtMost(MAX_STAT_CAP),
                    agility = (it.agility + 4).coerceAtMost(MAX_STAT_CAP),
                    firstTouch = (it.firstTouch + 3).coerceAtMost(MAX_STAT_CAP)
                )
            }
        ),
        OriginBackstory(
            id = "ORIGIN_DIASPORA",
            title = "Avrupa Gurbetçi Akademisi",
            subtitle = "Almanya/Avrupa temposu, atletik kondisyon",
            iconEmoji = "🌍",
            lore = "Avrupa'da altyapı disiplini alıp Türkiye'ye dönen gurbetçi yetenek. Kondisyon seviyen, oyun temposuna uyumun ve profesyonel yaklaşımın üst düzey.",
            perkDescription = "+3 Sürat, +3 Kondisyon, +3 Taktik",
            attributeModifier = {
                it.copy(
                    pace = (it.pace + 3).coerceAtMost(MAX_STAT_CAP),
                    stamina = (it.stamina + 3).coerceAtMost(MAX_STAT_CAP),
                    tactics = (it.tactics + 3).coerceAtMost(MAX_STAT_CAP)
                )
            }
        )
    )

    val archetypesByPosition: Map<Position, List<RoleArchetype>> = mapOf(
        Position.ST to listOf(
            RoleArchetype(
                id = "ROLE_ST_PIVOT",
                name = "Pivot Santrafor",
                position = Position.ST,
                badge = "KULE",
                description = "Hava toplarında hakim, sırtı dönük top saklayıp kanatları oyuna sokan klasik 9 numara.",
                keyStrengths = listOf("Fiziksel Güç", "Kafa Vuruşu", "Bitiricilik"),
                baseAttributes = PlayerAttributes(
                    shooting = 64, passing = 52, dribbling = 50, firstTouch = 58,
                    pace = 54, agility = 48, stamina = 58, strength = 72,
                    defending = 36, tactics = 58, vision = 54, setPieces = 50
                )
            ),
            RoleArchetype(
                id = "ROLE_ST_POACHER",
                name = "Fırsatçı Ceza Sahası Golcüsü",
                position = Position.ST,
                badge = "GOLCÜ",
                description = "Ceza sahasında koklayan, tek vuruş ustası ve savunma arkasına sarkan ölümcül bitirici.",
                keyStrengths = listOf("Bitiricilik", "İvmelenme", "Pozisyon Alma"),
                baseAttributes = PlayerAttributes(
                    shooting = 68, passing = 50, dribbling = 58, firstTouch = 64,
                    pace = 65, agility = 64, stamina = 56, strength = 54,
                    defending = 32, tactics = 62, vision = 50, setPieces = 52
                )
            ),
            RoleArchetype(
                id = "ROLE_ST_FALSE9",
                name = "Sahte Dokuz (False 9)",
                position = Position.ST,
                badge = "TEKNİK",
                description = "Orta sahaya yanaşıp stoperleri üzerine çeken ve kanat koşucularına servis yapan bağlantı santrforu.",
                keyStrengths = listOf("Vizyon & Pas", "İlk Dokunuş", "Dripling"),
                baseAttributes = PlayerAttributes(
                    shooting = 60, passing = 64, dribbling = 65, firstTouch = 66,
                    pace = 60, agility = 62, stamina = 58, strength = 52,
                    defending = 35, tactics = 64, vision = 65, setPieces = 56
                )
            )
        ),
        Position.WG to listOf(
            RoleArchetype(
                id = "ROLE_WG_INSIDE_FWD",
                name = "Falso Kanat (İçeri Kat Eden)",
                position = Position.WG,
                badge = "TERS AYAK",
                description = "Kanattan içeri kat edip ters ayağıyla uzak köşeye falsolu şutlar çıkaran modern hücumcu.",
                keyStrengths = listOf("Plase Şut", "Topla Hız", "Çeviklik"),
                baseAttributes = PlayerAttributes(
                    shooting = 65, passing = 58, dribbling = 68, firstTouch = 64,
                    pace = 70, agility = 68, stamina = 58, strength = 50,
                    defending = 36, tactics = 56, vision = 58, setPieces = 58
                )
            ),
            RoleArchetype(
                id = "ROLE_WG_CLASSIC",
                name = "Klasik Çizgi Kanadı",
                position = Position.WG,
                badge = "ORTA",
                description = "Çizgiye inip adrese teslim kesme ortalar açan, rakip beki hızıyla ezen geleneksel kanat.",
                keyStrengths = listOf("Depar Hızı", "Orta Açma", "Dripling"),
                baseAttributes = PlayerAttributes(
                    shooting = 56, passing = 65, dribbling = 67, firstTouch = 62,
                    pace = 74, agility = 70, stamina = 62, strength = 48,
                    defending = 40, tactics = 54, vision = 60, setPieces = 62
                )
            ),
            RoleArchetype(
                id = "ROLE_WG_PLAYMAKER",
                name = "Kanat Oyun Kurucu",
                position = Position.WG,
                badge = "BEYİN",
                description = "Hücumu kanattan yöneten, ters kanada diyagonal uzun paslar ve kilit toplar atan oyun zekası.",
                keyStrengths = listOf("Vizyon", "Ara Pası", "Taktik Zeka"),
                baseAttributes = PlayerAttributes(
                    shooting = 58, passing = 68, dribbling = 64, firstTouch = 65,
                    pace = 62, agility = 62, stamina = 60, strength = 50,
                    defending = 42, tactics = 65, vision = 68, setPieces = 64
                )
            )
        ),
        Position.AM to listOf(
            RoleArchetype(
                id = "ROLE_AM_CLASSIC_10",
                name = "Klasik 10 Numara (Maestro)",
                position = Position.AM,
                badge = "MAESTRO",
                description = "Takımın beyni. Savunmayı tek pasla açan, serbest vuruş ustası estetik oyun yöneticisi.",
                keyStrengths = listOf("Kilit Pas", "Vizyon", "Duran Top"),
                baseAttributes = PlayerAttributes(
                    shooting = 62, passing = 70, dribbling = 67, firstTouch = 70,
                    pace = 58, agility = 64, stamina = 56, strength = 48,
                    defending = 36, tactics = 66, vision = 72, setPieces = 68
                )
            ),
            RoleArchetype(
                id = "ROLE_AM_SHADOW_ST",
                name = "Gölge Forvet",
                position = Position.AM,
                badge = "İKİNCİ FORVET",
                description = "Forvetin arkasından aniden ceza sahasına sızıp gol arayan dinamik ikinci forvet profili.",
                keyStrengths = listOf("Ceza Sahası Koşuları", "Bitiricilik", "İvmelenme"),
                baseAttributes = PlayerAttributes(
                    shooting = 66, passing = 62, dribbling = 64, firstTouch = 65,
                    pace = 66, agility = 65, stamina = 62, strength = 54,
                    defending = 38, tactics = 62, vision = 62, setPieces = 58
                )
            )
        ),
        Position.CM to listOf(
            RoleArchetype(
                id = "ROLE_CM_BOX_TO_BOX",
                name = "Box-to-Box (İki Yönlü Dinamo)",
                position = Position.CM,
                badge = "CİĞER",
                description = "Kendi ceza sahasından rakip ceza sahasına mekik dokuyan, hiç durmayan modern orta saha.",
                keyStrengths = listOf("Kondisyon", "Mücadele", "Dinamizm"),
                baseAttributes = PlayerAttributes(
                    shooting = 58, passing = 64, dribbling = 60, firstTouch = 62,
                    pace = 64, agility = 62, stamina = 74, strength = 64,
                    defending = 58, tactics = 64, vision = 62, setPieces = 54
                )
            ),
            RoleArchetype(
                id = "ROLE_CM_REGISTA",
                name = "Regista (Derin Oyun Kurucu)",
                position = Position.CM,
                badge = "PAS DAĞITICI",
                description = "Stoperlerin önünden oyunu yönlendiren, uzun metrajlı diyagonal toplarla oyunu açan usta.",
                keyStrengths = listOf("Uzun Pas", "Oyun Okuma", "Top Kontrolü"),
                baseAttributes = PlayerAttributes(
                    shooting = 56, passing = 72, dribbling = 62, firstTouch = 68,
                    pace = 56, agility = 58, stamina = 64, strength = 56,
                    defending = 52, tactics = 68, vision = 70, setPieces = 64
                )
            )
        ),
        Position.DM to listOf(
            RoleArchetype(
                id = "ROLE_DM_ANCHOR",
                name = "Kesici Çapa (Anchor Man)",
                position = Position.DM,
                badge = "SÜPÜRGE",
                description = "Savunmanın önünde kalkan kuran, rakip 10 numarayı kilitleyen sert ve disiplinli ön libero.",
                keyStrengths = listOf("Top Kapma", "Fiziksel Güç", "Pozisyon Alma"),
                baseAttributes = PlayerAttributes(
                    shooting = 46, passing = 62, dribbling = 54, firstTouch = 60,
                    pace = 58, agility = 56, stamina = 72, strength = 70,
                    defending = 70, tactics = 68, vision = 58, setPieces = 48
                )
            ),
            RoleArchetype(
                id = "ROLE_DM_VOLANTE",
                name = "Dinamik İki Yönlü Ön Libero",
                position = Position.DM,
                badge = "PRES",
                description = "Topu kaptıktan sonra hızla dikine çıkan, geçiş oyununda takımı atağa kaldıran mücadeleci.",
                keyStrengths = listOf("Agresif Pres", "Kondisyon", "Dikey Pas"),
                baseAttributes = PlayerAttributes(
                    shooting = 52, passing = 64, dribbling = 58, firstTouch = 62,
                    pace = 64, agility = 60, stamina = 72, strength = 66,
                    defending = 66, tactics = 64, vision = 62, setPieces = 50
                )
            )
        ),
        Position.CB to listOf(
            RoleArchetype(
                id = "ROLE_CB_BALL_PLAYING",
                name = "Pasör Stoper (Ball-Playing)",
                position = Position.CB,
                badge = "OYUN KURAN",
                description = "Baskı altında soğukkanlı kalan, dikine isabetli paslarla hücumu başlatan modern stoper.",
                keyStrengths = listOf("Ayak Kalitesi", "Oyun Kurma", "Müdahale"),
                baseAttributes = PlayerAttributes(
                    shooting = 38, passing = 65, dribbling = 54, firstTouch = 62,
                    pace = 60, agility = 56, stamina = 65, strength = 68,
                    defending = 68, tactics = 66, vision = 62, setPieces = 46
                )
            ),
            RoleArchetype(
                id = "ROLE_CB_STOPPER",
                name = "Hava Hakimi Çakılı Stoper",
                position = Position.CB,
                badge = "DUVAR",
                description = "Geçit vermeyen hava topu canavarı, ceza sahasını süpüren sert ve tavizsiz kaya savunmacı.",
                keyStrengths = listOf("Kafa Topu", "Omuz Omuza", "Kademeye Giriş"),
                baseAttributes = PlayerAttributes(
                    shooting = 34, passing = 52, dribbling = 46, firstTouch = 54,
                    pace = 56, agility = 50, stamina = 66, strength = 76,
                    defending = 72, tactics = 68, vision = 50, setPieces = 42
                )
            )
        ),
        Position.FB to listOf(
            RoleArchetype(
                id = "ROLE_FB_ATTACKING",
                name = "Hücumcu Kanat Beki",
                position = Position.FB,
                badge = "FİŞEK",
                description = "Boydan boya koridoru kullanan, bindirmelerle arka direkte gol arayan veya orta kesen bek.",
                keyStrengths = listOf("Sürat", "Bindirme", "Orta Açma"),
                baseAttributes = PlayerAttributes(
                    shooting = 50, passing = 62, dribbling = 63, firstTouch = 62,
                    pace = 74, agility = 68, stamina = 72, strength = 56,
                    defending = 60, tactics = 60, vision = 58, setPieces = 56
                )
            ),
            RoleArchetype(
                id = "ROLE_FB_INVERTED",
                name = "İçeri Kat Eden Ters Bek",
                position = Position.FB,
                badge = "İÇ BEK",
                description = "Top takımdaysa orta sahaya kayarak pas istasyonu oluşturan, taktiksel esneklik sağlayan bek.",
                keyStrengths = listOf("Pas Trafiği", "Taktik Zeka", "Top Saklama"),
                baseAttributes = PlayerAttributes(
                    shooting = 46, passing = 66, dribbling = 60, firstTouch = 64,
                    pace = 65, agility = 64, stamina = 68, strength = 58,
                    defending = 64, tactics = 68, vision = 64, setPieces = 52
                )
            )
        ),
        Position.WB to listOf(
            RoleArchetype(
                id = "ROLE_WB_WING_BACK",
                name = "Modern Kanat Bek",
                position = Position.WB,
                badge = "KORİDOR",
                description = "3'lü ve 5'li savunma sistemlerinin can damarı. Hem savunma hem hücumda kesintisiz tempo.",
                keyStrengths = listOf("Kondisyon", "Hız", "Bindirme"),
                baseAttributes = PlayerAttributes(
                    shooting = 52, passing = 63, dribbling = 64, firstTouch = 62,
                    pace = 73, agility = 68, stamina = 74, strength = 58,
                    defending = 61, tactics = 62, vision = 60, setPieces = 54
                )
            )
        ),
        Position.GK to listOf(
            RoleArchetype(
                id = "ROLE_GK_SWEEPER",
                name = "Süpürücü Kaleci (Sweeper Keeper)",
                position = Position.GK,
                badge = "AYAKLI KL",
                description = "Ceza sahası dışına çıkarak arkaya atılan topları süpüren, ayakları bir stoper kadar iyi kaleci.",
                keyStrengths = listOf("Ayak Tekniği", "Çıkış Zamanlaması", "Refleks"),
                baseAttributes = PlayerAttributes(
                    shooting = 20, passing = 60, dribbling = 35, firstTouch = 55,
                    pace = 58, agility = 60, stamina = 58, strength = 62,
                    defending = 45, tactics = 64, vision = 62, setPieces = 30,
                    goalkeeping = 68
                )
            ),
            RoleArchetype(
                id = "ROLE_GK_TRADITIONAL",
                name = "Geleneksel Çizgi Kalecisi",
                position = Position.GK,
                badge = "REFLEKS",
                description = "Kalesini terk etmeyen, çizgide inanılmaz refleksler ve uzanışlarla net golleri önleyen kaleci.",
                keyStrengths = listOf("Refleks", "Uzanış", "Bire Bir"),
                baseAttributes = PlayerAttributes(
                    shooting = 18, passing = 48, dribbling = 25, firstTouch = 48,
                    pace = 50, agility = 64, stamina = 56, strength = 66,
                    defending = 40, tactics = 62, vision = 50, setPieces = 25,
                    goalkeeping = 72
                )
            )
        )
    )

    fun getArchetypes(pos: Position): List<RoleArchetype> {
        return archetypesByPosition[pos] ?: archetypesByPosition[Position.AM]!!
    }

    fun calculateBmi(heightCm: Int, weightKg: Int): Float {
        val heightM = heightCm / 100f
        return if (heightM > 0f) weightKg / (heightM * heightM) else 22f
    }

    fun getAthleticBuildLabel(heightCm: Int, weightKg: Int): Pair<String, String> {
        val bmi = calculateBmi(heightCm, weightKg)
        return when {
            bmi < 21.0f -> "Çevik & Patlayıcı" to "Düşük ağırlık, yüksek ivmelenme ve ani yön değiştirme kabiliyeti."
            bmi in 21.0f..23.8f -> "Dengeli & Atletik" to "Modern elit futbolcu standardı. Hız ve dayanıklılık uyumu."
            else -> "Güçlü & Yıkılmaz" to "Yüksek kütle, ikili mücadelelerde yıkılmayan omuz üstünlüğü."
        }
    }
}
