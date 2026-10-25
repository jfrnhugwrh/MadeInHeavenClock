package com.madeinheaven.clock.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneOffset

class TimeAccelerationTest {

    @Test
    fun speedStartsAtOne() {
        assertEquals(1.0, TimeAcceleration.speedAt(0.0), 1e-9)
    }

    @Test
    fun speedReachesMaxAfterRampAndStaysThere() {
        assertEquals(TimeAcceleration.MAX_SPEED, TimeAcceleration.speedAt(TimeAcceleration.RAMP_SECONDS), 1.0)
        assertEquals(TimeAcceleration.MAX_SPEED, TimeAcceleration.speedAt(TimeAcceleration.RAMP_SECONDS * 5), 1.0)
    }

    @Test
    fun speedIsStrictlyIncreasingDuringRamp() {
        var previous = 0.0
        var t = 0.0
        while (t <= TimeAcceleration.RAMP_SECONDS) {
            val speed = TimeAcceleration.speedAt(t)
            assertTrue("speed must grow at t=$t", speed > previous)
            previous = speed
            t += 0.25
        }
    }

    @Test
    fun intensityIsClampedAndInvertible() {
        assertEquals(0f, TimeAcceleration.intensityFor(1.0), 1e-6f)
        assertEquals(1f, TimeAcceleration.intensityFor(1e9), 1e-6f)
        assertEquals(1f, TimeAcceleration.intensityFor(1e12), 1e-6f)
        assertEquals(0f, TimeAcceleration.intensityFor(0.001), 1e-6f)

        val roundTrip = TimeAcceleration.intensityFor(TimeAcceleration.speedFor(0.42f))
        assertEquals(0.42f, roundTrip, 1e-5f)
    }

    @Test
    fun rewindHitsBothEndpoints() {
        assertEquals(1000.0, TimeAcceleration.rewind(1000.0, 10.0, 0f), 1e-9)
        assertEquals(10.0, TimeAcceleration.rewind(1000.0, 10.0, 1f), 1e-9)
    }

    @Test
    fun endOfTimeIsInYear9999() {
        val millis = TimeAcceleration.endOfTimeMillis(ZoneOffset.UTC)
        val year = java.time.Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).year
        assertEquals(TimeAcceleration.MAX_YEAR, year)
    }
}

class ClockFormattersTest {

    private val minute = 60_000L
    private val hour = 60 * minute
    private val day = 24 * hour
    private val year = 31_556_952_000L

    @Test
    fun formatsSecondsOnly() {
        assertEquals("+00s", ClockFormatters.formatSkipped(0))
        assertEquals("+05s", ClockFormatters.formatSkipped(5_400))
    }

    @Test
    fun formatsMinutes() {
        assertEquals("+12m 05s", ClockFormatters.formatSkipped(12 * minute + 5_000))
    }

    @Test
    fun formatsHoursAndDays() {
        assertEquals("+3h 07m", ClockFormatters.formatSkipped(3 * hour + 7 * minute))
        assertEquals("+2d 03h", ClockFormatters.formatSkipped(2 * day + 3 * hour))
    }

    @Test
    fun formatsYears() {
        assertEquals("+14y 10d 05h", ClockFormatters.formatSkipped(14 * year + 10 * day + 5 * hour))
    }

    @Test
    fun negativeValuesAreTreatedAsZero() {
        assertEquals("+00s", ClockFormatters.formatSkipped(-1234))
    }
}
