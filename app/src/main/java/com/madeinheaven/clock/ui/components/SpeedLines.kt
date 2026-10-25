package com.madeinheaven.clock.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private const val STREAK_COUNT = 90
private val TWO_PI = (2.0 * PI).toFloat()

private data class Streak(
    val angle: Float,
    val offset: Float,
    val length: Float,
    val thickness: Float,
)

/**
 * Radial "time is rushing past you" speed lines. Streaks fly outward from the centre; the
 * number, length, brightness and velocity all scale with [intensity] (0..1).
 *
 * @param timeSeconds a monotonic animation clock, supplied by the caller so this composable
 *                    stays stateless and previewable.
 */
@Composable
fun SpeedLines(
    intensity: Float,
    timeSeconds: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    // Fixed seed -> the same streak layout on every launch, and stable across recompositions.
    val streaks = remember {
        val random = Random(seed = 42)
        List(STREAK_COUNT) {
            Streak(
                angle = random.nextFloat() * TWO_PI,
                offset = random.nextFloat(),
                length = 0.4f + random.nextFloat() * 0.6f,
                thickness = 1f + random.nextFloat() * 2.5f,
            )
        }
    }

    Canvas(modifier) {
        if (intensity < 0.03f) return@Canvas

        val maxRadius = hypot(size.width, size.height) / 2f
        val visibleCount = (STREAK_COUNT * intensity).roundToInt().coerceIn(0, STREAK_COUNT)
        val flow = timeSeconds * (0.3f + 2.5f * intensity)

        for (i in 0 until visibleCount) {
            val streak = streaks[i]

            // 0 = just spawned near the centre, 1 = left the screen; then wraps around.
            val progress = (streak.offset + flow * (0.6f + streak.length)) % 1f

            val inner = maxRadius * (0.12f + 0.88f * progress)
            val outer = inner + maxRadius * 0.35f * intensity * streak.length * (0.3f + progress)
            val dx = cos(streak.angle)
            val dy = sin(streak.angle)

            drawLine(
                color = color.copy(alpha = ((0.15f + 0.55f * progress) * intensity).coerceIn(0f, 1f)),
                start = Offset(center.x + dx * inner, center.y + dy * inner),
                end = Offset(center.x + dx * outer, center.y + dy * outer),
                strokeWidth = streak.thickness * density * (0.6f + 1.6f * progress),
                cap = StrokeCap.Round,
            )
        }
    }
}
