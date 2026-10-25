package com.madeinheaven.clock.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.madeinheaven.clock.R
import com.madeinheaven.clock.model.ClockFormatters
import com.madeinheaven.clock.model.ClockUiState
import com.madeinheaven.clock.model.TimePhase
import com.madeinheaven.clock.ui.theme.HeavenPalette
import java.text.NumberFormat
import kotlin.math.roundToLong

/**
 * The time-speed counter (1x -> 1,000,000,000x), a progress bar, a stage caption and the
 * total amount of time skipped so far.
 */
@Composable
fun SpeedMeter(
    state: ClockUiState,
    palette: HeavenPalette,
    modifier: Modifier = Modifier,
) {
    val numberFormat = remember { NumberFormat.getIntegerInstance() }
    val speedText = "×" + numberFormat.format(state.speedMultiplier.roundToLong())
    val fraction = state.intensity.coerceIn(0f, 1f)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(stageLabel(state)),
            style = MaterialTheme.typography.labelLarge,
            color = palette.accent,
            maxLines = 1,
        )
        Text(
            text = speedText,
            style = MaterialTheme.typography.headlineMedium,
            color = palette.foreground,
            maxLines = 1,
            softWrap = false,
        )

        // Hand-drawn bar: avoids relying on progress-indicator APIs that vary between versions.
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(palette.foreground.copy(alpha = 0.15f)),
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(Brush.horizontalGradient(listOf(palette.accent, palette.accentSecondary))),
            )
        }

        Text(
            text = stringResource(R.string.time_skipped, ClockFormatters.formatSkipped(state.skippedMillis)),
            style = MaterialTheme.typography.labelSmall,
            color = palette.foreground.copy(alpha = 0.8f),
            // Keep the layout height stable by hiding (not removing) the line at 1x.
            modifier = Modifier.alpha(if (state.skippedMillis > 0L) 1f else 0f),
        )
    }
}

@StringRes
private fun stageLabel(state: ClockUiState): Int = when (state.phase) {
    TimePhase.NORMAL -> R.string.stage_normal
    TimePhase.UNIVERSE_RESET -> R.string.stage_reset
    TimePhase.ACCELERATING -> when {
        state.intensity < 0.30f -> R.string.stage_accelerating
        state.intensity < 0.60f -> R.string.stage_beyond
        state.intensity < 0.95f -> R.string.stage_catching_up
        else -> R.string.stage_heaven
    }
}
