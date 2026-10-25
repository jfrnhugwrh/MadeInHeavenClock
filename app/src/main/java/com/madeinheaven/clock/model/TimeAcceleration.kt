package com.madeinheaven.clock.model

import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.log10
import kotlin.math.pow

/**
 * Pure (Android-free, unit-testable) maths for the Made in Heaven time-acceleration curve.
 *
 * The speed multiplier grows exponentially: it is 1x at t = 0 and reaches [MAX_SPEED] after
 * [RAMP_SECONDS]. Because the curve is exponential in *time*, the *logarithm* of the speed
 * ([intensityFor]) rises linearly, which is what drives every visual and audio effect.
 *
 * With the defaults, the 10 second ramp alone skips roughly 15 years, and every second spent
 * at the cap skips about 31.7 more.
 */
object TimeAcceleration {
    const val MIN_SPEED = 1.0
    const val MAX_SPEED = 1_000_000_000.0

    /** Seconds needed to go from [MIN_SPEED] to [MAX_SPEED]. */
    const val RAMP_SECONDS = 10.0

    /** Duration of the "universe reset" rewind animation. */
    const val RESET_SECONDS = 1.8

    /** When the in-world calendar passes this year, the universe resets by itself (the loop). */
    const val MAX_YEAR = 9999

    private const val MAX_EXPONENT = 9.0 // log10(MAX_SPEED)

    /** Speed multiplier after [elapsedSeconds] of acceleration. */
    fun speedAt(elapsedSeconds: Double): Double {
        val t = (elapsedSeconds / RAMP_SECONDS).coerceIn(0.0, 1.0)
        return 10.0.pow(MAX_EXPONENT * t)
    }

    /** Normalised 0..1 "how far into Heaven are we" value derived from a speed multiplier. */
    fun intensityFor(speed: Double): Float =
        (log10(speed.coerceIn(MIN_SPEED, MAX_SPEED)) / MAX_EXPONENT).toFloat()

    /** Inverse of [intensityFor]. */
    fun speedFor(intensity: Float): Double =
        10.0.pow(MAX_EXPONENT * intensity.coerceIn(0f, 1f))

    /**
     * Interpolates from [from] back to [to] with an ease-out curve, used to visually rewind
     * the clock during the universe reset.
     */
    fun rewind(from: Double, to: Double, progress: Float): Double {
        val p = progress.coerceIn(0f, 1f).toDouble()
        val eased = 1.0 - (1.0 - p).pow(3)
        return from + (to - from) * eased
    }

    /** Epoch millis of the last second of [MAX_YEAR] in [zone]. */
    fun endOfTimeMillis(zone: ZoneId): Long =
        LocalDateTime.of(MAX_YEAR, 12, 31, 23, 59, 59)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
}
