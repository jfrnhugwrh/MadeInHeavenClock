package com.madeinheaven.clock.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.madeinheaven.clock.ui.theme.HeavenPalette
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val DEG_TO_RAD = (PI / 180.0).toFloat()

/**
 * Analog clock face. The hands are derived from [epochMillis], so at high acceleration they
 * spin as wildly as the in-world time moves.
 */
@Composable
fun AnalogClockFace(
    epochMillis: Long,
    zoneId: ZoneId,
    intensity: Float,
    palette: HeavenPalette,
    modifier: Modifier = Modifier,
) {
    val time = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zoneId)
    val seconds = time.second + time.nano / 1_000_000_000f
    val minutes = time.minute + seconds / 60f
    val hours = (time.hour % 12) + minutes / 60f

    Canvas(modifier) {
        val radius = size.minDimension / 2f

        // Halo that swells with the acceleration.
        if (intensity > 0.05f) {
            drawCircle(
                color = palette.accent.copy(alpha = 0.10f + 0.25f * intensity),
                radius = radius,
                center = center,
            )
        }

        // Face and rim.
        drawCircle(
            color = palette.accentSecondary.copy(alpha = 0.22f),
            radius = radius * 0.94f,
            center = center,
        )
        drawCircle(
            color = palette.foreground,
            radius = radius * 0.94f,
            center = center,
            style = Stroke(width = radius * 0.03f),
        )

        // 60 ticks, hour ticks are longer and thicker.
        for (i in 0 until 60) {
            val isHour = i % 5 == 0
            val angle = (i * 6f - 90f) * DEG_TO_RAD
            val outer = radius * 0.86f
            val inner = outer - radius * if (isHour) 0.10f else 0.04f
            drawLine(
                color = if (isHour) palette.foreground else palette.foreground.copy(alpha = 0.5f),
                start = polar(center, inner, angle),
                end = polar(center, outer, angle),
                strokeWidth = radius * if (isHour) 0.025f else 0.01f,
                cap = StrokeCap.Round,
            )
        }

        drawHand(degrees = hours * 30f, length = 0.50f, width = 0.045f, color = palette.foreground)
        drawHand(degrees = minutes * 6f, length = 0.72f, width = 0.030f, color = palette.foreground)
        drawHand(degrees = seconds * 6f, length = 0.82f, width = 0.015f, color = palette.accent)

        drawCircle(color = palette.accent, radius = radius * 0.04f, center = center)
    }
}

private fun DrawScope.drawHand(degrees: Float, length: Float, width: Float, color: Color) {
    val radius = size.minDimension / 2f
    val angle = (degrees - 90f) * DEG_TO_RAD
    drawLine(
        color = color,
        start = center,
        end = polar(center, radius * length, angle),
        strokeWidth = radius * width,
        cap = StrokeCap.Round,
    )
}

private fun polar(center: Offset, radius: Float, angleRadians: Float) = Offset(
    x = center.x + radius * cos(angleRadians),
    y = center.y + radius * sin(angleRadians),
)
