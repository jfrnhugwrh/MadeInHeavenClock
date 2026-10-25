package com.madeinheaven.clock.model

/** Small pure formatting helpers (kept free of Android so they can be unit tested). */
object ClockFormatters {
    private const val MS_PER_SECOND = 1_000L
    private const val MS_PER_MINUTE = 60 * MS_PER_SECOND
    private const val MS_PER_HOUR = 60 * MS_PER_MINUTE
    private const val MS_PER_DAY = 24 * MS_PER_HOUR
    private const val MS_PER_YEAR = 31_556_952_000L // 365.2425 days (Gregorian average)

    /**
     * Formats how much time has been skipped, e.g. `+14y 212d 03h`, `+12m 05s` or `+05s`.
     * The unit granularity adapts so the readout is always informative.
     */
    fun formatSkipped(millis: Long): String {
        val ms = millis.coerceAtLeast(0L)
        return when {
            ms >= MS_PER_YEAR -> {
                val years = ms / MS_PER_YEAR
                val remainder = ms % MS_PER_YEAR // time left over after whole years
                val days = remainder / MS_PER_DAY
                val hours = (remainder % MS_PER_DAY) / MS_PER_HOUR
                "+${years}y ${days}d ${hours.pad2()}h"
            }
            ms >= MS_PER_DAY -> {
                val days = ms / MS_PER_DAY
                val hours = (ms % MS_PER_DAY) / MS_PER_HOUR
                "+${days}d ${hours.pad2()}h"
            }
            ms >= MS_PER_HOUR -> {
                val hours = ms / MS_PER_HOUR
                val minutes = (ms % MS_PER_HOUR) / MS_PER_MINUTE
                "+${hours}h ${minutes.pad2()}m"
            }
            ms >= MS_PER_MINUTE -> {
                val minutes = ms / MS_PER_MINUTE
                val seconds = (ms % MS_PER_MINUTE) / MS_PER_SECOND
                "+${minutes}m ${seconds.pad2()}s"
            }
            else -> "+${(ms / MS_PER_SECOND).pad2()}s"
        }
    }

    private fun Long.pad2(): String = toString().padStart(2, '0')
}
