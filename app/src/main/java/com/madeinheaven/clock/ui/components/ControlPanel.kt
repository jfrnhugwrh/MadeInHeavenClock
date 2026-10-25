package com.madeinheaven.clock.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.madeinheaven.clock.R
import com.madeinheaven.clock.model.TimePhase
import com.madeinheaven.clock.ui.theme.HeavenPalette

/** The main "Accelerate Time" / "Reset Universe" button plus the sound toggle. */
@Composable
fun ControlPanel(
    phase: TimePhase,
    isMuted: Boolean,
    palette: HeavenPalette,
    onToggleAcceleration: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionLabel = when (phase) {
        TimePhase.NORMAL -> R.string.action_accelerate
        TimePhase.ACCELERATING -> R.string.action_reset
        TimePhase.UNIVERSE_RESET -> R.string.action_resetting
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Button(
            onClick = onToggleAcceleration,
            enabled = phase != TimePhase.UNIVERSE_RESET,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.accent,
                contentColor = palette.onAccent,
                disabledContainerColor = palette.accent.copy(alpha = 0.4f),
                disabledContentColor = palette.onAccent.copy(alpha = 0.7f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(text = stringResource(actionLabel), style = MaterialTheme.typography.labelLarge)
        }

        TextButton(onClick = onToggleMute) {
            Text(
                text = stringResource(if (isMuted) R.string.sfx_off else R.string.sfx_on),
                style = MaterialTheme.typography.labelSmall,
                color = palette.foreground,
            )
        }
    }
}
