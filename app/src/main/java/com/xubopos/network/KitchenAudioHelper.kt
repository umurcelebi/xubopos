package com.xubopos.network

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object KitchenAudioHelper {
    private val isPlaying = AtomicBoolean(false)
    private val audioScope = CoroutineScope(Dispatchers.Default)

    /**
     * Mutfak ekranına yeni sipariş düştüğünde yüksek sesli restoran zilini çalar ve titreşir.
     */
    fun playKitchenAlert(context: Context) {
        vibrateShort(context)

        audioScope.launch {
            if (!isPlaying.compareAndSet(false, true)) {
                return@launch
            }
            try {
                // 1. Öncelik: Dahili sentezlenen iki tonlu net restoran zili (Ding-Dong)
                val played = playSynthesizedChime()
                if (!played) {
                    playToneGeneratorFallback()
                }
            } catch (_: Exception) {
                playToneGeneratorFallback()
            } finally {
                isPlaying.set(false)
            }
        }
    }

    private fun playSynthesizedChime(): Boolean {
        return try {
            val sampleRate = 44100
            val freq1 = 1046.5 // C6 (High C)
            val freq2 = 1318.5 // E6 (High E)

            val duration1 = 0.28 // saniye
            val gap = 0.05       // saniye
            val duration2 = 0.45 // saniye
            val totalDuration = duration1 + gap + duration2
            val totalSamples = (sampleRate * totalDuration).toInt()
            val audioBuffer = ShortArray(totalSamples)

            val samples1 = (sampleRate * duration1).toInt()
            val gapSamples = (sampleRate * gap).toInt()
            val samples2 = (sampleRate * duration2).toInt()

            // 1. Ton: Ding (C6 + 2. harmonik)
            for (i in 0 until samples1) {
                val t = i.toDouble() / sampleRate
                val decay = exp(-t * 9.0)
                val sample = (0.75 * sin(2.0 * PI * freq1 * t) +
                        0.25 * sin(4.0 * PI * freq1 * t)) * decay
                audioBuffer[i] = (sample * Short.MAX_VALUE * 0.95).toInt().toShort()
            }

            // Ara boşluk zaten 0 (sessiz)

            // 2. Ton: Dong (E6 + 2. harmonik)
            val offset2 = samples1 + gapSamples
            for (i in 0 until samples2) {
                val t = i.toDouble() / sampleRate
                val decay = exp(-t * 6.5)
                val sample = (0.75 * sin(2.0 * PI * freq2 * t) +
                        0.25 * sin(4.0 * PI * freq2 * t)) * decay
                val idx = offset2 + i
                if (idx < totalSamples) {
                    audioBuffer[idx] = (sample * Short.MAX_VALUE * 0.95).toInt().toShort()
                }
            }

            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val format = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize, audioBuffer.size * 2)

            val track = AudioTrack.Builder()
                .setAudioAttributes(attributes)
                .setAudioFormat(format)
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(audioBuffer, 0, audioBuffer.size)
            track.play()

            Thread.sleep(((totalDuration + 0.1) * 1000).toLong())

            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun playToneGeneratorFallback() {
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
            Thread.sleep(400)
            toneGenerator.release()
        } catch (_: Exception) {
            try {
                val toneGenAlarm = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                toneGenAlarm.startTone(ToneGenerator.TONE_CDMA_ALERT_NETWORK_LITE, 400)
                Thread.sleep(450)
                toneGenAlarm.release()
            } catch (_: Exception) {}
        }
    }

    private fun vibrateShort(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(350, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(350)
            }
        } catch (_: Exception) {}
    }
}
