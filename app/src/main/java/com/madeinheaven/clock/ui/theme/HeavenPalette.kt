package com.madeinheaven.clock.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * The colors for one frame. Unlike a static theme, this is recomputed every frame from the
 * acceleration [intensity][heavenPalette] to give the calm-indigo -> blood-red/gold shift,
 * optionally colour-inverted.
 */
@Immutable
data class HeavenPalette(
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val foreground: Color,
    val accent: Color,
    val accentSecondary: Color,
    val speedLines: Color,
) {
    /** Readable content color for text/icons drawn on top of [accent]. */
    val onAccent: Color get() = backgroundTop

    fun inverted() = HeavenPalette(
        backgroundTop = backgroundTop.inverse(),
        backgroundBottom = backgroundBottom.inverse(),
        foreground = foreground.inverse(),
        accent = accent.inverse(),
        accentSecondary = accentSecondary.inverse(),
        speedLines = speedLines.inverse(),
    )
}

private fun Color.inverse() = Color(red = 1f - red, green = 1f - green, blue = 1f - blue, alpha = alpha)

private val CalmPalette = HeavenPalette(
    backgroundTop = HeavenVoid,
    backgroundBottom = HeavenIndigo,
    foreground = HeavenIce,
    accent = HeavenCyan,
    accentSecondary = HeavenViolet,
    speedLines = HeavenIce,
)

private val PeakPalette = HeavenPalette(
    backgroundTop = HeavenBlood,
    backgroundBottom = HeavenAbyss,
    foreground = Color.White,
    accent = HeavenGold,
    accentSecondary = HeavenCrimson,
    speedLines = HeavenGold,
)

/**
 * @param intensity 0 (normal time) .. 1 (maximum acceleration)
 * @param inverted  whether to flip every color (the "negative" flash)
 */
fun heavenPalette(intensity: Float, inverted: Boolean = false): HeavenPalette {
    val k = intensity.coerceIn(0f, 1f)
    val mixed = HeavenPalette(
        backgroundTop = lerp(CalmPalette.backgroundTop, PeakPalette.backgroundTop, k),
        backgroundBottom = lerp(CalmPalette.backgroundBottom, PeakPalette.backgroundBottom, k),
        foreground = lerp(CalmPalette.foreground, PeakPalette.foreground, k),
        accent = lerp(CalmPalette.accent, PeakPalette.accent, k),
        accentSecondary = lerp(CalmPalette.accentSecondary, PeakPalette.accentSecondary, k),
        speedLines = lerp(CalmPalette.speedLines, PeakPalette.speedLines, k),
    )
    return if (inverted) mixed.inverted() else mixed
}
