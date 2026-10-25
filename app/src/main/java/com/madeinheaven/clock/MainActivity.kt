package com.madeinheaven.clock

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.madeinheaven.clock.audio.TickSynth
import com.madeinheaven.clock.model.ClockViewModel
import com.madeinheaven.clock.ui.ClockScreen
import com.madeinheaven.clock.ui.theme.MadeInHeavenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        // It's a clock: don't let the screen sleep while it is in the foreground.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            MadeInHeavenTheme {
                ClockApp()
            }
        }
    }
}

/**
 * Wires the [ClockViewModel] and the [TickSynth] to the Compose UI and the Activity lifecycle.
 */
@Composable
private fun ClockApp(viewModel: ClockViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // The time loop only runs while the Activity is STARTED; it is cancelled on stop and
    // relaunched on start, so we never burn battery in the background.
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.runClockLoop()
        }
    }

    // Audio follows the same lifecycle: silent and device-free when backgrounded.
    val tickSynth = remember { TickSynth() }
    DisposableEffect(tickSynth) {
        onDispose { tickSynth.release() }
    }
    LifecycleStartEffect(tickSynth) {
        tickSynth.start()
        onStopOrDispose { tickSynth.stop() }
    }
    SideEffect {
        tickSynth.intensity = state.intensity
        tickSynth.muted = state.isMuted
    }

    ClockScreen(
        state = state,
        onToggleAcceleration = viewModel::onAccelerationToggled,
        onToggleMute = viewModel::onMuteToggled,
    )
}
