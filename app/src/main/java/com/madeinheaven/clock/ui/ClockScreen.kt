package com.madeinheaven.clock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.madeinheaven.clock.R
import com.madeinheaven.clock.model.ClockUiState
import com.madeinheaven.clock.model.TimePhase
import com.madeinheaven.clock.ui.components.AnalogClockFace
import com.madeinheaven.clock.ui.components.ControlPanel
import com.madeinheaven.clock.ui.components.DigitalReadout
import com.madeinheaven.clock.ui.components.SpeedLines
import com.madeinheaven.clock.ui.components.SpeedMeter
import com.madeinheaven.clock.ui.theme.HeavenPalette
import com.madeinheaven.clock.ui.theme.MadeInHeavenTheme
import com.madeinheaven.clock.ui.theme.heavenPalette
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/** One full inversion cycle lasts 1.2 s (< 1 Hz): intentionally slow, see README safety note. */
private const val INVERT_PERIOD_SECONDS = 1.2f

/**
 * Stateless root screen. Everything it needs arrives via [state]; every interaction leaves via
 * a callback, which keeps it trivially previewable and testable.
 */
@Composable
fun ClockScreen(
    state: ClockUiState,
    onToggleAcceleration: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inverted = state.phase == TimePhase.ACCELERATING &&
        shouldInvert(state.intensity, state.fxTimeSeconds)
    val palette = heavenPalette(state.intensity, inverted)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(palette.backgroundTop, palette.backgroundBottom))),
    ) {
        SpeedLines(
            intensity = state.intensity,
            timeSeconds = state.fxTimeSeconds,
            color = palette.speedLines,
            modifier = Modifier.fillMaxSize(),
        )

        // Menacing JoJo-style sound-effect glyphs creep in as time accelerates.
        if (state.intensity > 0.25f) {
            val alpha = ((state.intensity - 0.25f) / 0.75f).coerceIn(0f, 1f) * 0.85f
            MenacingText(
                alpha = alpha,
                color = palette.accentSecondary,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .systemBarsPadding()
                    .padding(start = 8.dp, top = 56.dp)
                    .rotate(-10f),
            )
            MenacingText(
                alpha = alpha,
                color = palette.accentSecondary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .systemBarsPadding()
                    .padding(end = 8.dp, bottom = 96.dp)
                    .rotate(8f),
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(16.dp)
                .graphicsLayer {
                    // The whole UI trembles as reality strains to keep up.
                    val strength = state.intensity * state.intensity
                    translationX = sin(state.fxTimeSeconds * 93f) * 6.dp.toPx() * strength
                    translationY = cos(state.fxTimeSeconds * 77f) * 4.dp.toPx() * strength
                },
        ) {
            if (maxWidth > maxHeight) {
                LandscapeLayout(state, palette, onToggleAcceleration, onToggleMute)
            } else {
                PortraitLayout(state, palette, onToggleAcceleration, onToggleMute)
            }
        }

        // Universe-reset flash: one single white burst that fades out (no strobing).
        if (state.phase == TimePhase.UNIVERSE_RESET) {
            val flash = (1f - state.resetProgress).pow(2) * 0.85f
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = flash)),
            )
        }
    }
}

@Composable
private fun PortraitLayout(
    state: ClockUiState,
    palette: HeavenPalette,
    onToggleAcceleration: () -> Unit,
    onToggleMute: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Header(palette)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            AnalogClockFace(
                epochMillis = state.epochMillis,
                zoneId = state.zoneId,
                intensity = state.intensity,
                palette = palette,
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(4.dp),
            )
        }

        DigitalReadout(state.epochMillis, state.zoneId, palette)
        SpeedMeter(state, palette)
        ControlPanel(state.phase, state.isMuted, palette, onToggleAcceleration, onToggleMute)
    }
}

@Composable
private fun LandscapeLayout(
    state: ClockUiState,
    palette: HeavenPalette,
    onToggleAcceleration: () -> Unit,
    onToggleMute: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            AnalogClockFace(
                epochMillis = state.epochMillis,
                zoneId = state.zoneId,
                intensity = state.intensity,
                palette = palette,
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(4.dp),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            Header(palette)
            DigitalReadout(state.epochMillis, state.zoneId, palette)
            SpeedMeter(state, palette)
            ControlPanel(state.phase, state.isMuted, palette, onToggleAcceleration, onToggleMute)
        }
    }
}

@Composable
private fun Header(palette: HeavenPalette, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.title_katakana),
            style = MaterialTheme.typography.labelSmall,
            color = palette.accent,
        )
        Text(
            text = stringResource(R.string.title),
            style = MaterialTheme.typography.titleMedium,
            color = palette.foreground,
        )
    }
}

@Composable
private fun MenacingText(alpha: Float, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = "ゴゴゴゴ",
        color = color.copy(alpha = alpha),
        fontSize = 44.sp,
        fontWeight = FontWeight.Black,
        modifier = modifier,
    )
}

/**
 * Brief colour-negative pulses once intensity passes 50%. The pulse frequency is deliberately
 * capped well under 3 Hz to avoid triggering photosensitive reactions.
 */
private fun shouldInvert(intensity: Float, timeSeconds: Float): Boolean {
    if (intensity < 0.5f) return false
    val dutyCycle = 0.15f + 0.35f * ((intensity - 0.5f) / 0.5f) // 15% .. 50% of each cycle
    return (timeSeconds % INVERT_PERIOD_SECONDS) < INVERT_PERIOD_SECONDS * dutyCycle
}

// region previews

@Preview(showBackground = true, widthDp = 380, heightDp = 780)
@Composable
private fun ClockScreenNormalPreview() {
    MadeInHeavenTheme {
        ClockScreen(state = ClockUiState(), onToggleAcceleration = {}, onToggleMute = {})
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 780)
@Composable
private fun ClockScreenHeavenPreview() {
    MadeInHeavenTheme {
        ClockScreen(
            state = ClockUiState(
                epochMillis = System.currentTimeMillis() + 90L * 365 * 24 * 3_600_000,
                phase = TimePhase.ACCELERATING,
                speedMultiplier = 3_000_000.0,
                intensity = 0.72f,
                skippedMillis = 90L * 365 * 24 * 3_600_000,
                fxTimeSeconds = 12.3f,
            ),
            onToggleAcceleration = {},
            onToggleMute = {},
        )
    }
}

// endregion
