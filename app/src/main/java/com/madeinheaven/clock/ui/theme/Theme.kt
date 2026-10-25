package com.madeinheaven.clock.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val HeavenColorScheme = darkColorScheme(
    primary = HeavenCyan,
    onPrimary = HeavenVoid,
    secondary = HeavenViolet,
    tertiary = HeavenGold,
    background = HeavenVoid,
    onBackground = HeavenIce,
    surface = HeavenIndigo,
    onSurface = HeavenIce,
)

/** Static Material theme. Per-frame effect colors come from [heavenPalette]. */
@Composable
fun MadeInHeavenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HeavenColorScheme,
        typography = HeavenTypography,
        content = content,
    )
}
