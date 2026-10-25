package com.madeinheaven.clock.model

import java.time.ZoneId

/** Which stage of the Made in Heaven cycle the clock is currently in. */
enum class TimePhase {
    /** Real system time, 1x. */
    NORMAL,

    /** Exponentially accelerating time. */
    ACCELERATING,

    /** The universe is resetting: time rewinds back to the real present. */
    UNIVERSE_RESET,
}

/** Immutable snapshot of everything the UI needs to draw one frame. */
data class ClockUiState(
    /** The (possibly accelerated) instant being displayed. */
    val epochMillis: Long = System.currentTimeMillis(),
    val zoneId: ZoneId = ZoneId.systemDefault(),
    val phase: TimePhase = TimePhase.NORMAL,
    /** Current speed multiplier, 1.0 .. 1,000,000,000.0. */
    val speedMultiplier: Double = 1.0,
    /** Normalised 0..1 effect strength (log-scaled speed). Drives colors, lines, shake, audio. */
    val intensity: Float = 0f,
    /** How far the displayed time is ahead of real time. */
    val skippedMillis: Long = 0L,
    /** Monotonic seconds used to animate effects. */
    val fxTimeSeconds: Float = 0f,
    val isMuted: Boolean = false,
    /** 0..1 progress of the universe-reset animation (0 outside of it). */
    val resetProgress: Float = 0f,
)
