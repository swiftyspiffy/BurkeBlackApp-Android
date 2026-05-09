package com.swiftyspiffy.burkeblackapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.White)
        Text(value, color = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
fun BadgeChip(text: String, color: Color) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(50))
            .padding(horizontal = 7.dp, vertical = 2.dp)
    )
}

object StatFormatter {
    private val numberFormat = NumberFormat.getIntegerInstance()
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    fun integer(value: Int): String = numberFormat.format(value)

    fun currency(value: Double): String = currencyFormat.format(value)
}

object DateUtils {
    fun formatFollowDate(dateStr: String): String {
        val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

        // Try ISO 8601
        try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(dateStr)
            if (date != null) return outputFormat.format(date)
        } catch (_: Exception) {}

        try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(dateStr)
            if (date != null) return outputFormat.format(date)
        } catch (_: Exception) {}

        // Try datetime
        try {
            val dtFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val date = dtFormat.parse(dateStr)
            if (date != null) return outputFormat.format(date)
        } catch (_: Exception) {}

        // Try date only
        try {
            val dFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = dFormat.parse(dateStr)
            if (date != null) return outputFormat.format(date)
        } catch (_: Exception) {}

        return dateStr
    }

    fun formatEpochSeconds(epoch: Long): String {
        val format = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return format.format(Date(epoch * 1000))
    }

    fun formatEpochSecondsWithTime(epoch: Long): String {
        val format = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        return format.format(Date(epoch * 1000))
    }

    fun subscribedSince(months: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, -months)
        val format = SimpleDateFormat("MMMM, yyyy", Locale.getDefault())
        return format.format(cal.time)
    }

    fun formatBitDate(dateStr: String): String {
        // Try ISO format first
        try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(dateStr)
            if (date != null) {
                val outFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
                return outFormat.format(date)
            }
        } catch (_: Exception) {}

        try {
            val dtFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val date = dtFormat.parse(dateStr)
            if (date != null) {
                val outFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
                return outFormat.format(date)
            }
        } catch (_: Exception) {}

        return dateStr
    }

    fun formatRelativeTime(dateString: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val date = format.parse(dateString) ?: return dateString
            val now = Date()
            val diffMs = now.time - date.time
            val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(diffMs)
            val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(diffMs)
            val days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffMs)

            when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "Posted ${minutes}m ago"
                hours < 24 -> "Posted ${hours}h ago"
                days < 7 -> "Posted ${days}d ago"
                days < 30 -> "Posted ${days / 7}w ago"
                else -> {
                    val displayFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
                    "Posted ${displayFormat.format(date)}"
                }
            }
        } catch (e: Exception) {
            dateString
        }
    }
}
