package com.madeinheaven.clock.model

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import java.time.ZoneId

/**
 * Owns the clock state machine: NORMAL -> ACCELERATING -> UNIVERSE_RESET -> NORMAL.
 *
 * Threading: [runClockLoop] is collected from the UI (main dispatcher) inside
 * `repeatOnLifecycle(STARTED)`, so the loop is cancelled automatically when the app is
 * backgrounded and restarted when it returns. All user events also arrive on the main thread,
 * therefore the mutable fields below never need synchronisation.
 *
 * Time model: while NORMAL, the displayed time is the real system time (no drift, ever).
 * While ACCELERATING, a virtual clock advances by `realDelta * speed` every frame, where
 * `speed` follows [TimeAcceleration.speedAt].
 */
class ClockViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ClockUiState())
    val uiState: StateFlow<ClockUiState> = _uiState.asStateFlow()

    private var zone: ZoneId = ZoneId.systemDefault()
    private var phase = TimePhase.NORMAL
    private var virtualMillis = System.currentTimeMillis().toDouble()
    private var speed = TimeAcceleration.MIN_SPEED
    private var intensity = 0f
    private var muted = false
    private var fxTime = 0.0

    private var accelerationElapsed = 0.0
    private var endOfTimeMillis = Long.MAX_VALUE

    private var resetElapsed = 0.0
    private var resetProgress = 0f
    private var resetFromMillis = 0.0
    private var resetFromIntensity = 0f

    /** Drives the clock until the calling coroutine is cancelled. */
    suspend fun runClockLoop() {
        zone = ZoneId.systemDefault() // pick up time-zone changes made while we were stopped
        var lastNanos = System.nanoTime()
        while (currentCoroutineContext().isActive) {
            delay(FRAME_INTERVAL_MS)
            val nowNanos = System.nanoTime()
            val deltaSeconds = (nowNanos - lastNanos) / NANOS_PER_SECOND
            lastNanos = nowNanos
            advance(deltaSeconds)
        }
    }

    /** "Accelerate Time" while normal, "Reset Universe" while accelerating, ignored mid-reset. */
    fun onAccelerationToggled() {
        when (phase) {
            TimePhase.NORMAL -> startAcceleration()
            TimePhase.ACCELERATING -> beginUniverseReset()
            TimePhase.UNIVERSE_RESET -> Unit
        }
        publish()
    }

    fun onMuteToggled() {
        muted = !muted
        publish()
    }

    // region state machine

    private fun startAcceleration() {
        phase = TimePhase.ACCELERATING
        accelerationElapsed = 0.0
        speed = TimeAcceleration.MIN_SPEED
        intensity = 0f
        virtualMillis = System.currentTimeMillis().toDouble()
        endOfTimeMillis = TimeAcceleration.endOfTimeMillis(zone)
    }

    private fun beginUniverseReset() {
        phase = TimePhase.UNIVERSE_RESET
        resetElapsed = 0.0
        resetProgress = 0f
        resetFromMillis = virtualMillis
        resetFromIntensity = intensity
    }

    private fun finishUniverseReset(realNowMillis: Long) {
        phase = TimePhase.NORMAL
        speed = TimeAcceleration.MIN_SPEED
        intensity = 0f
        resetProgress = 0f
        virtualMillis = realNowMillis.toDouble()
    }

    private fun advance(deltaSeconds: Double) {
        fxTime += deltaSeconds
        val realNow = System.currentTimeMillis()

        when (phase) {
            TimePhase.NORMAL -> {
                virtualMillis = realNow.toDouble()
            }

            TimePhase.ACCELERATING -> {
                accelerationElapsed += deltaSeconds
                speed = TimeAcceleration.speedAt(accelerationElapsed)
                intensity = TimeAcceleration.intensityFor(speed)
                virtualMillis += deltaSeconds * MILLIS_PER_SECOND * speed
                if (virtualMillis >= endOfTimeMillis) {
                    beginUniverseReset() // the loop: we hit the end of time, so start over
                }
            }

            TimePhase.UNIVERSE_RESET -> {
                resetElapsed += deltaSeconds
                resetProgress = (resetElapsed / TimeAcceleration.RESET_SECONDS)
                    .toFloat()
                    .coerceIn(0f, 1f)
                virtualMillis = TimeAcceleration.rewind(
                    from = resetFromMillis,
                    to = realNow.toDouble(),
                    progress = resetProgress,
                )
                intensity = resetFromIntensity * (1f - resetProgress)
                speed = TimeAcceleration.speedFor(intensity) // counter spins back down to 1x
                if (resetProgress >= 1f) finishUniverseReset(realNow)
            }
        }
        publish(realNow)
    }

    private fun publish(realNowMillis: Long = System.currentTimeMillis()) {
        val epoch = virtualMillis.toLong()
        _uiState.value = ClockUiState(
            epochMillis = epoch,
            zoneId = zone,
            phase = phase,
            speedMultiplier = speed,
            intensity = intensity,
            skippedMillis = (epoch - realNowMillis).coerceAtLeast(0L),
            fxTimeSeconds = (fxTime % FX_TIME_WRAP_SECONDS).toFloat(),
            isMuted = muted,
            resetProgress = resetProgress,
        )
    }

    // endregion

    private companion object {
        const val FRAME_INTERVAL_MS = 16L
        const val NANOS_PER_SECOND = 1_000_000_000.0
        const val MILLIS_PER_SECOND = 1_000.0
        const val FX_TIME_WRAP_SECONDS = 10_000.0
    }
}
