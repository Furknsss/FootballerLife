package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin

enum class GameSoundEffect {
    MENU_TAP,
    WHISTLE_KICKOFF,
    WHISTLE_FULLTIME,
    BALL_KICK,
    GOAL_CELEBRATION,
    PHONE_MESSAGE,
    TRANSFER_OFFER,
    TRAINING_LEVEL_UP,
    // Özel Saha İçi Atmosfer ve Duran Top Efektleri
    REFEREE_WHISTLE_FOUL,
    REFEREE_WHISTLE_CARD,
    CROSSBAR_METALLIC_HIT,
    CROWD_ROAR_EXPLOSION,
    STADIUM_CHANT_AMBIENCE,
    DEFENSE_WALL_JUMP,
    PENALTY_HEARTBEAT,
    CORNER_AERIAL_DUEL,
    INJURY_STRETCHER_HORN
}

class SoundAndHapticsManager(private val context: Context) {
    private val audioScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var lastPlayedMs: Long = 0L

    fun playEffect(
        effect: GameSoundEffect,
        sfxEnabled: Boolean,
        hapticsEnabled: Boolean
    ) {
        val now = System.currentTimeMillis()
        if (now - lastPlayedMs < 55L && effect == GameSoundEffect.MENU_TAP) return
        lastPlayedMs = now

        if (hapticsEnabled) {
            triggerHaptic(effect)
        }
        if (sfxEnabled) {
            audioScope.launch {
                runCatching { synthesizeTone(effect) }
            }
        }
    }

    // Kolaylık metotları (Convenience helpers)
    fun playRefereeWhistle(sfxEnabled: Boolean = true, hapticsEnabled: Boolean = true) =
        playEffect(GameSoundEffect.REFEREE_WHISTLE_FOUL, sfxEnabled, hapticsEnabled)

    fun playCardShown(sfxEnabled: Boolean = true, hapticsEnabled: Boolean = true) =
        playEffect(GameSoundEffect.REFEREE_WHISTLE_CARD, sfxEnabled, hapticsEnabled)

    fun playCrossbarHit(sfxEnabled: Boolean = true, hapticsEnabled: Boolean = true) =
        playEffect(GameSoundEffect.CROSSBAR_METALLIC_HIT, sfxEnabled, hapticsEnabled)

    fun playCrowdRoar(sfxEnabled: Boolean = true, hapticsEnabled: Boolean = true) =
        playEffect(GameSoundEffect.CROWD_ROAR_EXPLOSION, sfxEnabled, hapticsEnabled)

    fun playStadiumChant(sfxEnabled: Boolean = true, hapticsEnabled: Boolean = true) =
        playEffect(GameSoundEffect.STADIUM_CHANT_AMBIENCE, sfxEnabled, hapticsEnabled)

    fun playWallJump(sfxEnabled: Boolean = true, hapticsEnabled: Boolean = true) =
        playEffect(GameSoundEffect.DEFENSE_WALL_JUMP, sfxEnabled, hapticsEnabled)

    fun playPenaltyHeartbeat(sfxEnabled: Boolean = true, hapticsEnabled: Boolean = true) =
        playEffect(GameSoundEffect.PENALTY_HEARTBEAT, sfxEnabled, hapticsEnabled)

    fun playCornerDuel(sfxEnabled: Boolean = true, hapticsEnabled: Boolean = true) =
        playEffect(GameSoundEffect.CORNER_AERIAL_DUEL, sfxEnabled, hapticsEnabled)

    fun playInjuryStretcher(sfxEnabled: Boolean = true, hapticsEnabled: Boolean = true) =
        playEffect(GameSoundEffect.INJURY_STRETCHER_HORN, sfxEnabled, hapticsEnabled)

    private fun triggerHaptic(effect: GameSoundEffect) {
        runCatching {
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() != true) return

            val durationMs = when (effect) {
                GameSoundEffect.MENU_TAP -> 14L
                GameSoundEffect.BALL_KICK -> 26L
                GameSoundEffect.WHISTLE_KICKOFF, GameSoundEffect.WHISTLE_FULLTIME -> 65L
                GameSoundEffect.GOAL_CELEBRATION -> 140L
                GameSoundEffect.PHONE_MESSAGE, GameSoundEffect.TRANSFER_OFFER -> 45L
                GameSoundEffect.TRAINING_LEVEL_UP -> 55L
                // Yeni haptikler
                GameSoundEffect.REFEREE_WHISTLE_FOUL -> 70L
                GameSoundEffect.REFEREE_WHISTLE_CARD -> 120L
                GameSoundEffect.CROSSBAR_METALLIC_HIT -> 110L
                GameSoundEffect.CROWD_ROAR_EXPLOSION -> 180L
                GameSoundEffect.STADIUM_CHANT_AMBIENCE -> 35L
                GameSoundEffect.DEFENSE_WALL_JUMP -> 30L
                GameSoundEffect.PENALTY_HEARTBEAT -> 55L
                GameSoundEffect.CORNER_AERIAL_DUEL -> 45L
                GameSoundEffect.INJURY_STRETCHER_HORN -> 80L
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        }
    }

    private fun synthesizeTone(effect: GameSoundEffect) {
        val sampleRate = 22050
        val durationSec = when (effect) {
            GameSoundEffect.MENU_TAP -> 0.045
            GameSoundEffect.BALL_KICK -> 0.08
            GameSoundEffect.WHISTLE_KICKOFF -> 0.24
            GameSoundEffect.WHISTLE_FULLTIME -> 0.36
            GameSoundEffect.GOAL_CELEBRATION -> 0.48
            GameSoundEffect.PHONE_MESSAGE -> 0.16
            GameSoundEffect.TRANSFER_OFFER -> 0.32
            GameSoundEffect.TRAINING_LEVEL_UP -> 0.28
            // Yeni ses süreleri
            GameSoundEffect.REFEREE_WHISTLE_FOUL -> 0.34
            GameSoundEffect.REFEREE_WHISTLE_CARD -> 0.46
            GameSoundEffect.CROSSBAR_METALLIC_HIT -> 0.65
            GameSoundEffect.CROWD_ROAR_EXPLOSION -> 0.85
            GameSoundEffect.STADIUM_CHANT_AMBIENCE -> 0.75
            GameSoundEffect.DEFENSE_WALL_JUMP -> 0.22
            GameSoundEffect.PENALTY_HEARTBEAT -> 0.42
            GameSoundEffect.CORNER_AERIAL_DUEL -> 0.26
            GameSoundEffect.INJURY_STRETCHER_HORN -> 0.38
        }
        val numSamples = (sampleRate * durationSec).toInt().coerceAtLeast(256)
        val pcm = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val progress = i.toDouble() / numSamples
            val envelope = exp(-3.5 * progress)

            val sampleValue = when (effect) {
                GameSoundEffect.MENU_TAP -> {
                    sin(2.0 * PI * 680.0 * t) * exp(-18.0 * progress) * 0.25
                }
                GameSoundEffect.BALL_KICK -> {
                    val freq = 140.0 * exp(-12.0 * progress) + 52.0
                    sin(2.0 * PI * freq * t) * exp(-10.0 * progress) * 0.55
                }
                GameSoundEffect.WHISTLE_KICKOFF, GameSoundEffect.WHISTLE_FULLTIME -> {
                    val trillo = if (((t * 36.0).toInt() % 2) == 0) 2880.0 else 2740.0
                    sin(2.0 * PI * trillo * t) * (1.0 - progress * 0.4) * 0.28
                }
                GameSoundEffect.GOAL_CELEBRATION -> {
                    val f1 = if (progress < 0.45) 440.0 else 587.33
                    val f2 = if (progress < 0.45) 554.37 else 880.0
                    (sin(2.0 * PI * f1 * t) + 0.7 * sin(2.0 * PI * f2 * t)) * envelope * 0.32
                }
                GameSoundEffect.PHONE_MESSAGE -> {
                    val freq = if (progress < 0.5) 784.0 else 1046.5
                    sin(2.0 * PI * freq * t) * envelope * 0.25
                }
                GameSoundEffect.TRANSFER_OFFER, GameSoundEffect.TRAINING_LEVEL_UP -> {
                    val freq = when {
                        progress < 0.33 -> 523.25
                        progress < 0.66 -> 659.25
                        else -> 783.99
                    }
                    sin(2.0 * PI * freq * t) * envelope * 0.28
                }
                // --- 1. Hakem Düdüğü (Foul / Sert Düdük) ---
                GameSoundEffect.REFEREE_WHISTLE_FOUL -> {
                    // İki hızlı keskin düdük tonu (çift trill)
                    val isFirstBlast = progress < 0.42
                    val isGap = progress in 0.42..0.52
                    if (isGap) {
                        0.0
                    } else {
                        val trillFreq = if (((t * 44.0).toInt() % 2) == 0) 3150.0 else 2960.0
                        val subHar = sin(2.0 * PI * (trillFreq * 0.5) * t) * 0.2
                        (sin(2.0 * PI * trillFreq * t) + subHar) * 0.34
                    }
                }
                // --- 2. Hakem Kart Düdüğü (Otoriter Uzun Düdük) ---
                GameSoundEffect.REFEREE_WHISTLE_CARD -> {
                    val trillFreq = if (((t * 50.0).toInt() % 2) == 0) 3320.0 else 3080.0
                    val tremolo = 0.85 + 0.15 * sin(2.0 * PI * 18.0 * t)
                    sin(2.0 * PI * trillFreq * t) * tremolo * (1.0 - progress * 0.25) * 0.36
                }
                // --- 3. Direkten Dönen Şut Metalik Yankı (Crossbar Metallic Clang & Echo) ---
                GameSoundEffect.CROSSBAR_METALLIC_HIT -> {
                    // Direğe çarpma anında tiz metalik 'çınlama' ve rezonans yankısı
                    val hitImpact = sin(2.0 * PI * 1840.0 * t) * exp(-14.0 * progress) * 0.60
                    val metalRing1 = sin(2.0 * PI * 880.0 * t) * exp(-3.2 * progress) * 0.38
                    val metalRing2 = sin(2.0 * PI * 1320.0 * t) * exp(-4.0 * progress) * 0.25
                    val subBassPunch = sin(2.0 * PI * 95.0 * t) * exp(-16.0 * progress) * 0.45
                    (hitImpact + metalRing1 + metalRing2 + subBassPunch) * 0.55
                }
                // --- 4. Gol Patlaması ve Seyirci Coşkusu (Crowd Roar Explosion) ---
                GameSoundEffect.CROWD_ROAR_EXPLOSION -> {
                    // Stadyumda binlerce kişinin aynı anda kükremesi (çoklu harmonik gürültü sentezi)
                    val roarSurge = (1.0 - exp(-7.0 * progress)) * exp(-1.1 * progress)
                    val n1 = sin(2.0 * PI * 115.0 * t) + 0.8 * sin(2.0 * PI * 185.0 * t)
                    val n2 = 0.6 * sin(2.0 * PI * 340.0 * t) + 0.45 * sin(2.0 * PI * 680.0 * t)
                    val n3 = 0.3 * sin(2.0 * PI * 1240.0 * t) + 0.2 * sin(2.0 * PI * 2180.0 * t)
                    // Hafif kaotik faz kaydırması
                    val crowdTexture = sin(2.0 * PI * (440.0 + 80.0 * sin(2.0 * PI * 5.0 * t)) * t)
                    (n1 + n2 + n3 + 0.35 * crowdTexture) * roarSurge * 0.30
                }
                // --- 5. Tribün Tezahürat Ambiyansı (Ritmik Tempo ve Alkış) ---
                GameSoundEffect.STADIUM_CHANT_AMBIENCE -> {
                    // 4 vuruşluk ritmik stadyum alkışı ve tezahürat nabzı
                    val beatTime = (t % 0.19) / 0.19
                    val clapPulse = exp(-22.0 * beatTime) * (sin(2.0 * PI * 920.0 * t) + sin(2.0 * PI * 1480.0 * t))
                    val backgroundHum = (sin(2.0 * PI * 160.0 * t) + 0.5 * sin(2.0 * PI * 240.0 * t)) * 0.15
                    (clapPulse * 0.42 + backgroundHum) * (1.0 - progress * 0.15) * 0.32
                }
                // --- 6. Savunma Baraj Zıplaması (Wall Jump Rüzgar & Çim Düşüşü) ---
                GameSoundEffect.DEFENSE_WALL_JUMP -> {
                    val whoosh = sin(2.0 * PI * (320.0 + 400.0 * progress) * t) * exp(-8.0 * progress) * 0.35
                    val landingThud = if (progress > 0.65) {
                        val lp = (progress - 0.65) / 0.35
                        sin(2.0 * PI * 90.0 * t) * exp(-12.0 * lp) * 0.45
                    } else 0.0
                    (whoosh + landingThud) * 0.40
                }
                // --- 7. Penaltı Kalp Atışı (Tense Heartbeat - Thump-Thump) ---
                GameSoundEffect.PENALTY_HEARTBEAT -> {
                    val beat1 = if (progress < 0.35) {
                        sin(2.0 * PI * 65.0 * t) * exp(-14.0 * (progress / 0.35)) * 0.65
                    } else 0.0
                    val beat2 = if (progress in 0.42..0.85) {
                        val p2 = (progress - 0.42) / 0.43
                        sin(2.0 * PI * 58.0 * t) * exp(-12.0 * p2) * 0.50
                    } else 0.0
                    (beat1 + beat2) * 0.50
                }
                // --- 8. Korner Hava Mücadelesi Kafa Vuruşu (Aerial Duel Impact) ---
                GameSoundEffect.CORNER_AERIAL_DUEL -> {
                    val headerImpact = sin(2.0 * PI * (130.0 * exp(-10.0 * progress) + 70.0) * t) * exp(-8.0 * progress) * 0.60
                    val airTurbulence = sin(2.0 * PI * 480.0 * t) * exp(-18.0 * progress) * 0.25
                    (headerImpact + airTurbulence) * 0.45
                }
                // --- 9. Sakatlık & Sedye Sağlık Düdüğü / Siren ---
                GameSoundEffect.INJURY_STRETCHER_HORN -> {
                    val hornFreq = if (progress < 0.5) 587.33 else 440.0 // D5 to A4 warning tones
                    sin(2.0 * PI * hornFreq * t) * envelope * 0.30
                }
            }
            pcm[i] = (sampleValue.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }

        val bufferSize = pcm.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        track.write(pcm, 0, pcm.size)
        track.play()
        Thread.sleep((durationSec * 1000).toLong() + 30L)
        track.stop()
        track.release()
    }
}
