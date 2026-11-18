package com.example.engine.match

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Duran Top (Set Piece) Senaryo Türü
 */
enum class SetPieceScenarioKind(
    val titleTr: String,
    val subtitleTr: String
) {
    DIRECT_FREE_KICK("SERBEST VURUŞ (FRİKİK)", "Barajın üzerinden 90 köşesine falsolu plase"),
    PENALTY_KICK("PENALTI VURUŞU", "11 metreden kaleciyle bire bir düello"),
    CORNER_KICK("KÖŞE VURUŞU (KORNER)", "Ceza sahası karambolüne kavisli orta"),
    INDIRECT_FREE_KICK("ÇİFT VURUŞ", "Paslaşarak kaleye sert şut organizasyonu")
}

/**
 * 90 Hedef Halkası Modeli (media_1795005882059_584c6456.jpg'deki yeşil hedef halkası)
 */
data class SetPieceTargetRing(
    val id: String,
    val labelTr: String,
    val targetPitchX: Float,
    val targetPitchY: Float,
    val targetPitchZ: Float, // Yükseklik (90 köşesi için yüksek Z)
    val ringRadiusM: Float = 0.85f,
    val scoreBonus: Int = 100,
    val isTopCorner90: Boolean = true
)

/**
 * Frikik Barajındaki Savunma Oyuncusu (5 Savunmacı)
 */
data class WallDefender(
    val id: String,
    val index: Int, // 0..4 (5 savunmacı barajı)
    val name: String,
    val jerseyNumber: Int,
    val heightCm: Int = 185,
    val basePitchX: Float,
    val basePitchY: Float,
    val jumpHeightCmMax: Float = 45f,
    val jumpProgress: Float = 0f, // 0f (yerde) .. 1f (en tepe) .. 0f (iniş)
    val isJumping: Boolean = false,
    val jumpReactionDelayMs: Long = 120L
)

/**
 * Frikik Baraj Kurulumu ve Çevre Modeli
 */
data class FreeKickWallConfiguration(
    val wallCount: Int = 5, // Kullanıcı görselindeki gibi 5 savunmacı
    val defenders: List<WallDefender>,
    val sprayLineDistanceM: Float = 9.15f, // Hakemin çizdiği beyaz kaybolan sprey köpüğü
    val sprayLineStartX: Float,
    val sprayLineStartY: Float,
    val sprayLineEndX: Float,
    val sprayLineEndY: Float,
    val shooterJerseyNumber: Int = 7, // Fotoğraftaki 7 numaralı frikik ustası
    val shooterName: String = "Yıldız Oyuncu",
    val ballPosition: PitchPoint,
    val goalCenterPosition: PitchPoint = PitchPoint(98f, 50f, 0f),
    val targetRings: List<SetPieceTargetRing> = listOf(
        SetPieceTargetRing("90_LEFT", "SOL 90", 98f, 44.5f, 2.2f, 0.75f, 150, true),
        SetPieceTargetRing("90_RIGHT", "SAĞ 90", 98f, 55.5f, 2.2f, 0.75f, 150, true),
        SetPieceTargetRing("LOW_CORNER", "ALT KÖŞE", 98f, 45.0f, 0.4f, 0.85f, 80, false)
    ),
    val goalkeeperName: String = "Kaleci",
    val goalkeeperPosition: PitchPoint = PitchPoint(97.5f, 50f, 0f),
    val goalkeeperDiveReach: Float = 2.4f
)

/**
 * Penaltı Düellosu Modeli
 */
enum class PenaltyKeeperDive {
    TOP_LEFT,
    BOTTOM_LEFT,
    CENTER_STAY,
    BOTTOM_RIGHT,
    TOP_RIGHT
}

data class PenaltyDuelConfiguration(
    val spotPosition: PitchPoint = PitchPoint(88.5f, 50f, 0f),
    val shooterNumber: Int = 7,
    val shooterName: String = "Kaptan",
    val goalkeeperName: String = "Rakip Kaleci",
    val goalkeeperStanceX: Float = 98.2f,
    val goalkeeperStanceY: Float = 50.0f,
    val anticipatedDive: PenaltyKeeperDive = PenaltyKeeperDive.BOTTOM_RIGHT,
    val keeperDiveProgress: Float = 0f,
    val heartRateBpm: Int = 138, // Gerilim kalp atışı nabzı
    val pressureMultiplier: Float = 1.15f
)

/**
 * Korner ve Ceza Sahası Karambol Modeli
 */
data class BoxDuelist(
    val id: String,
    val name: String,
    val number: Int,
    val isAttacker: Boolean,
    val pitchX: Float,
    val pitchY: Float,
    val aerialJumpPower: Int, // 50..99
    val markingOpponentId: String? = null,
    val isRisingForHeader: Boolean = false,
    val headerJumpProgress: Float = 0f
)

data class CornerKickConfiguration(
    val cornerFlagPosition: PitchPoint,
    val isLeftCorner: Boolean,
    val boxDuelists: List<BoxDuelist>,
    val targetNearPostArea: PitchPoint = PitchPoint(93f, 46f, 0f),
    val targetPenaltySpotArea: PitchPoint = PitchPoint(88.5f, 50f, 0f),
    val targetFarPostArea: PitchPoint = PitchPoint(93f, 54f, 0f)
)

/**
 * Hakem Kart ve Drama Olayları
 */
enum class RefereeDramaType {
    FOUL_WHISTLE,
    YELLOW_CARD,
    RED_CARD_DIRECT,
    SECOND_YELLOW_RED,
    VAR_CHECK_PENALTY,
    INJURY_STOPPAGE,
    STRETCHER_INTERVENTION
}

data class RefereeDramaEvent(
    val type: RefereeDramaType,
    val refereeName: String = "Halil Umut Meler",
    val targetPlayerName: String,
    val targetPlayerNumber: Int,
    val isUserPlayer: Boolean = false,
    val foulDescriptionTr: String,
    val minute: Int,
    val cardAnimationProgress: Float = 0f, // 0f -> elini cebine atma, 0.5f -> kartı havaya kaldırma, 1f -> sarı/kırmızı parlama
    val varReviewActive: Boolean = false,
    val isStretcherRequired: Boolean = false
)

/**
 * Sakatlık ve Sedye Sağlık Ekibi Müdahale Modeli
 */
enum class MedicalTreatmentPhase {
    PLAYER_DOWN_ON_TURF,        // Oyuncu çimde acı içinde bacağını tutuyor
    REFEREE_CALLS_MEDICAL_STAFF,// Hakem sağlık ekibini el işaretiyle acil sahaya çağırıyor
    MEDICS_SPRINTING_ON_PITCH,  // 2 sağlık görevlisi ve sedye ekibi sahaya koşuyor
    COOLING_SPRAY_APPLIED,      // Ağrı kesici soğutucu sprey sıkılıyor (mist partikülleri)
    STRETCHER_BOARDING,         // Oyuncu sedyeye alınıyor
    RECOVERY_AND_RESUME         // Ayağa kalkıp alkışlarla dönüyor veya sedyeyle ambulansa taşınıyor
}

data class MedicalInjuryEvent(
    val injuredPlayerName: String,
    val injuredJerseyNumber: Int,
    val injuredClubName: String,
    val bodyPartTr: String = "Sağ Ayak Bileği",
    val severityScore: Int = 3, // 1..5
    val currentPhase: MedicalTreatmentPhase = MedicalTreatmentPhase.PLAYER_DOWN_ON_TURF,
    val sprayParticlesProgress: Float = 0f,
    val requiresStretcher: Boolean = true,
    val canReturnToMatch: Boolean = false,
    val crowdApplauds: Boolean = true
)

/**
 * Fizik, Simülasyon ve Çarpışma Çözümleyicisi
 */
object SetPiecePhysicsSimulator {

    /**
     * Frikik Baraj Çarpışma Kontrolü:
     * Topun yörüngesi barajın x koordinatından geçerken (x ~ 89..91):
     * - Topun yüksekliği baraj oyuncularının boyu + zıplama yüksekliğini aşıyor mu?
     * - Aşıyorsa: "BARAJI AŞTI!"
     * - Çarparsa: "BARAJA ÇARPTI!"
     */
    fun checkWallCollision(
        ballPoints: List<PitchPoint>,
        wallConfig: FreeKickWallConfiguration,
        wallJumpScale: Float = 1.0f
    ): Pair<Boolean, String> {
        val wallX = wallConfig.sprayLineStartX
        // Barajın Y aralığı (5 savunmacı genişliği)
        val minY = wallConfig.defenders.minOfOrNull { it.basePitchY } ?: 46f
        val maxY = wallConfig.defenders.maxOfOrNull { it.basePitchY } ?: 54f

        for (pt in ballPoints) {
            if (abs(pt.x - wallX) <= 1.8f) {
                // Top baraj hizasında
                if (pt.y in (minY - 1.0f)..(maxY + 1.0f)) {
                    // Yükseklik kontrolü: Savunmacı ortalama boy + zıplama (norm Z ~ 1.85m + 0.45m * jump)
                    val wallDefendingZ = 1.85f + (0.45f * wallJumpScale)
                    if (pt.z < wallDefendingZ) {
                        return true to "BARAJA ÇARPTI! Savunma duvarı topu engelledi."
                    }
                }
            }
        }
        return false to "BARAJI AŞTI! Muhteşem kavisle barajın üzerinden süzüldü."
    }

    /**
     * 90 Hedef Halkası Vurulma Kontrolü (media_1795005882059_584c6456.jpg)
     */
    fun checkTargetRingHit(
        ballFinalPoint: PitchPoint,
        rings: List<SetPieceTargetRing>
    ): SetPieceTargetRing? {
        return rings.firstOrNull { ring ->
            val distY = abs(ballFinalPoint.y - ring.targetPitchY)
            val distZ = abs(ballFinalPoint.z - ring.targetPitchZ)
            val dist = hypot(distY, distZ)
            dist <= ring.ringRadiusM * 1.5f
        }
    }

    /**
     * Penaltı Şut - Kaleci Düellosu Çözümlemesi
     */
    fun resolvePenaltyShootout(
        shotTargetY: Float, // 45.0 (sol köşe) .. 55.0 (sağ köşe)
        shotTargetZ: Float, // 0.2 (yerden) .. 2.3 (90 köşesi)
        keeperDive: PenaltyKeeperDive,
        shotPowerPct: Int
    ): Triple<Boolean, Boolean, String> { // <isGoal, isCrossbarHit, commentary>
        // Direk kontrolü
        val isLeftPost = abs(shotTargetY - 44.5f) < 0.35f
        val isRightPost = abs(shotTargetY - 55.5f) < 0.35f
        val isCrossbar = abs(shotTargetZ - 2.44f) < 0.25f

        if (isCrossbar || (isLeftPost && shotTargetZ > 1.5f) || (isRightPost && shotTargetZ > 1.5f)) {
            return Triple(false, true, "DİREKTEN DÖNDÜ! Metalik ses tüm stadyumda yankılandı!")
        }

        // Kalecinin uzandığı bölge
        val keeperTargetY = when (keeperDive) {
            PenaltyKeeperDive.TOP_LEFT, PenaltyKeeperDive.BOTTOM_LEFT -> 46.2f
            PenaltyKeeperDive.TOP_RIGHT, PenaltyKeeperDive.BOTTOM_RIGHT -> 53.8f
            PenaltyKeeperDive.CENTER_STAY -> 50.0f
        }
        val keeperTargetZ = when (keeperDive) {
            PenaltyKeeperDive.TOP_LEFT, PenaltyKeeperDive.TOP_RIGHT -> 2.0f
            PenaltyKeeperDive.BOTTOM_LEFT, PenaltyKeeperDive.BOTTOM_RIGHT -> 0.4f
            PenaltyKeeperDive.CENTER_STAY -> 1.2f
        }

        val keeperDistance = hypot(shotTargetY - keeperTargetY, shotTargetZ - keeperTargetZ)
        val keeperReach = if (shotPowerPct > 85) 1.2f else 1.65f

        return if (keeperDistance <= keeperReach) {
            Triple(false, false, "KALECİ KURTARDI! Müthiş refleksle köşeye uzandı!")
        } else {
            Triple(true, false, "GOL! Kaleciyi ters köşeye yatırdı ve fileleri havalandırdı!")
        }
    }

    /**
     * Korner Ceza Sahası Karambol ve Kafa Vuruşu Çözümlemesi
     */
    fun resolveCornerScramble(
        boxDuelists: List<BoxDuelist>,
        userAerialPower: Int
    ): Pair<Boolean, String> {
        val attackers = boxDuelists.filter { it.isAttacker }
        val defenders = boxDuelists.filter { !it.isAttacker }

        val bestAttackerJump = (attackers.maxOfOrNull { it.aerialJumpPower } ?: userAerialPower) + Random.nextInt(-10, 15)
        val bestDefenderJump = (defenders.maxOfOrNull { it.aerialJumpPower } ?: 75) + Random.nextInt(-10, 15)

        return if (bestAttackerJump > bestDefenderJump + 8) {
            true to "MÜKEMMEL KAFA VURUŞU! Kalabalığın arasından adeta havada asılı kalarak kafayı vurdu ve GOL!"
        } else if (bestAttackerJump > bestDefenderJump) {
            true to "KARAMBOLDE DOKUNUŞ! Kafa vuruşu sonrası kaleci çeldi, dönen topta boş kaleye vuruş ve GOL!"
        } else {
            false to "SAVUNMA UZAKLAŞTIRDI! Stoperler hava topunda yükselerek tehlikeyi savuşturdu."
        }
    }
}
