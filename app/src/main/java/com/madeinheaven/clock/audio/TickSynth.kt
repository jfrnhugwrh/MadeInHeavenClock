package com.madeinheaven.clock.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Procedural clock-tick synthesizer. No audio assets are required.
 *
 * A short decaying sine "blip" is generated on every tick. Both the tick *rate* and the blip
 * *pitch* are driven by [intensity] (0..1, the log-scaled time speed):
 *
 *  - 0.0  ->  a gentle 1 Hz tick / tock at ~700 Hz
 *  - 0.5  ->  ~15 ticks per second, noticeably higher pitch
 *  - 1.0  ->  60 ticks per second at ~3.5 kHz: the blips fuse into a rising, shrieking whine
 *
 * Rendering happens on a background coroutine writing PCM to a streaming [AudioTrack].
 * [intensity] and [muted] are volatile, so the UI thread can update them freely.
 */
class TickSynth {

    @Volatile
    var intensity: Float = 0f

    @Volatile
    var muted: Boolean = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    /** Starts rendering. Safe to call repeatedly. */
    fun start() {
        if (job?.isActive == true) return
        job = scope.launch { render() }
    }

    /** Stops rendering and frees the audio device. */
    fun stop() {
        job?.cancel()
        job = null
    }

    /** Stops rendering and permanently shuts the synthesizer down. */
    fun release() {
        stop()
        scope.cancel()
    }

    private suspend fun render() {
        val track = createTrack() ?: return
        try {
            track.play()
            var playing = true

            val buffer = ShortArray(CHUNK_SAMPLES)
            var tickPhase = 0.0
            var tonePhase = 0.0
            var envelope = 0.0
            var gain = 0.0
            var tickCount = 0L

            while (currentCoroutineContext().isActive) {
                // Fully faded out and muted: park the track instead of streaming silence.
                if (muted && gain < SILENCE_THRESHOLD) {
                    if (playing) {
                        track.pause()
                        playing = false
                    }
                    delay(IDLE_POLL_MS)
                    continue
                }
                if (!playing) {
                    track.play()
                    playing = true
                }

                val level = intensity.coerceIn(0f, 1f).toDouble()
                val tickHz = 1.0 + 59.0 * level * level
                val baseFrequency = 700.0 + 2_800.0 * level
                val targetGain = if (muted) 0.0 else MAX_GAIN * (0.6 + 0.4 * level)

                for (i in buffer.indices) {
                    gain += (targetGain - gain) * GAIN_SMOOTHING

                    tickPhase += tickHz / SAMPLE_RATE
                    if (tickPhase >= 1.0) {
                        tickPhase -= 1.0
                        envelope = 1.0
                        tonePhase = 0.0
                        tickCount++
                    }

                    // Alternate "tick" and a lower "tock" like a real clock.
                    val frequency = if (tickCount % 2L == 0L) baseFrequency else baseFrequency * 0.8
                    tonePhase += TWO_PI * frequency / SAMPLE_RATE

                    val sample = (sin(tonePhase) + 0.35 * sin(2.0 * tonePhase)) * envelope * gain
                    buffer[i] = (sample.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
                    envelope *= DECAY_PER_SAMPLE
                }

                // Blocking write paces this loop at real-time; negative means the track died.
                if (track.write(buffer, 0, buffer.size) < 0) break
            }
        } finally {
            runCatching { track.stop() }
            track.release()
        }
    }

    private fun createTrack(): AudioTrack? = runCatching {
        val minBufferBytes = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setBufferSizeInBytes(maxOf(minBufferBytes, CHUNK_SAMPLES * 2 * 2))
            .build()
    }.getOrNull()

    private companion object {
        const val SAMPLE_RATE = 22_050
        const val CHUNK_SAMPLES = 512
        const val MAX_GAIN = 0.30
        const val GAIN_SMOOTHING = 0.002
        const val SILENCE_THRESHOLD = 1e-4
        const val IDLE_POLL_MS = 50L
        const val TWO_PI = 2.0 * PI

        /** Envelope time constant of ~12 ms. */
        val DECAY_PER_SAMPLE = exp(-1.0 / (0.012 * SAMPLE_RATE))
    }
}
