package com.swiftyspiffy.burkeblackapp.util

import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

object MainStreamSchedule {
    private val eastern = ZoneId.of("America/New_York")

    fun nextStream(after: Instant): Instant {
        var date = after.atZone(eastern).toLocalDate()
        while (true) {
            val candidate = date.atTime(22, 0).atZone(eastern).toInstant()
            if (date.dayOfWeek != DayOfWeek.SUNDAY && candidate > after) return candidate
            date = date.plusDays(1)
        }
    }

    fun countdownText(now: Instant, includesSeconds: Boolean): String {
        val remaining = Duration.between(now, nextStream(now)).seconds.coerceAtLeast(0)
        val hours = remaining / 3600
        val minutes = remaining % 3600 / 60
        val seconds = remaining % 60
        return when {
            includesSeconds -> String.format(Locale.US, "%dh %02dm %02ds", hours, minutes, seconds)
            hours > 0 -> String.format(Locale.US, "%dh %02dm", hours, minutes)
            else -> "${minutes}m"
        }
    }
}
