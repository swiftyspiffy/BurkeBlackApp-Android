package com.swiftyspiffy.burkeblackapp.util

import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class MainStreamScheduleTest {
    @Test
    fun countsDownToTenPmEastern() {
        val now = Instant.parse("2026-10-30T00:00:00Z")
        assertEquals(Instant.parse("2026-10-30T02:00:00Z"), MainStreamSchedule.nextStream(now))
        assertEquals("2h 00m 00s", MainStreamSchedule.countdownText(now, true))
        assertEquals("2h 00m", MainStreamSchedule.countdownText(now, false))
    }

    @Test
    fun skipsSundayAndHandlesTheEndOfDaylightSavingTime() {
        // Saturday's stream has just started; the next scheduled stream is Monday EST.
        val now = Instant.parse("2026-11-01T02:00:00Z")
        assertEquals(Instant.parse("2026-11-03T03:00:00Z"), MainStreamSchedule.nextStream(now))
        assertEquals("49h 00m", MainStreamSchedule.countdownText(now, false))
    }

    @Test
    fun handlesTheStartOfDaylightSavingTime() {
        val now = Instant.parse("2026-03-08T03:00:00Z")
        assertEquals(Instant.parse("2026-03-10T02:00:00Z"), MainStreamSchedule.nextStream(now))
        assertEquals("47h 00m", MainStreamSchedule.countdownText(now, false))
    }

    @Test
    fun compactCountdownOmitsHoursWhenLessThanAnHourRemains() {
        assertEquals(
            "4m",
            MainStreamSchedule.countdownText(Instant.parse("2026-10-30T01:55:30Z"), false),
        )
    }
}
