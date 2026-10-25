package com.madeinheaven.clock.ui.components

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.madeinheaven.clock.ui.theme.HeavenPalette
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Digital hours:minutes:seconds plus the full, localized date (including the year). */
@Composable
fun DigitalReadout(
    epochMillis: Long,
    zoneId: ZoneId,
    palette: HeavenPalette,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val is24Hour = DateFormat.is24HourFormat(context)

    val timeFormatter = remember(locale, is24Hour) {
        DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm:ss" else "hh:mm:ss", locale)
    }
    val periodFormatter = remember(locale) { DateTimeFormatter.ofPattern("a", locale) }
    val dateFormatter = remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)
    }

    val zoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zoneId)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = zoned.format(timeFormatter),
                style = MaterialTheme.typography.displayLarge,
                color = palette.foreground,
                maxLines = 1,
                softWrap = false,
            )
            if (!is24Hour) {
                Text(
                    text = zoned.format(periodFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.accent,
                    modifier = Modifier.padding(start = 6.dp, bottom = 8.dp),
                )
            }
        }
        Text(
            text = zoned.format(dateFormatter),
            style = MaterialTheme.typography.titleMedium,
            color = palette.foreground.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
        )
    }
}
