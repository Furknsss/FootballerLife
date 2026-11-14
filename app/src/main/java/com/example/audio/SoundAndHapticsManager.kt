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
    TRAINING_LEVEL_UP
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
