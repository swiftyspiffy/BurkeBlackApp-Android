package com.swiftyspiffy.burkeblackapp.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class StreamStatusSmallWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isLive = WidgetDataStore.isStreamLive(context)
        val gameName = WidgetDataStore.getStreamGameName(context)
        val viewerCount = WidgetDataStore.getStreamViewerCount(context)
        val startedAt = WidgetDataStore.getStreamStartedAt(context)

        provideContent {
            GlanceTheme {
                SmallStatusContent(isLive, gameName, viewerCount, startedAt)
            }
        }
    }
}

@Composable
private fun SmallStatusContent(
    isLive: Boolean,
    gameName: String?,
    viewerCount: Int,
    startedAt: String?
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.darkBrown)
            .clickable(actionStartActivity<com.swiftyspiffy.burkeblackapp.MainActivity>())
            .padding(12.dp)
    ) {
        if (isLive) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = GlanceModifier
                            .background(WidgetColors.liveRed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "LIVE",
                            style = TextStyle(color = WidgetColors.whiteProvider, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        )
                    }
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        "👁 ${formatViewers(viewerCount)}",
                        style = TextStyle(color = WidgetColors.whiteProvider, fontSize = 12.sp)
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                if (startedAt != null) {
                    val runtime = formatRuntime(startedAt)
                    if (runtime != null) {
                        Text(
                            "⏱ $runtime",
                            style = TextStyle(color = WidgetColors.mutedProvider, fontSize = 11.sp)
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                    }
                }

                gameName?.let {
                    Text(
                        it,
                        style = TextStyle(color = WidgetColors.goldProvider, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        maxLines = 2
                    )
                }
            }
        } else {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📺", style = TextStyle(fontSize = 28.sp))
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    "Stream Offline",
                    style = TextStyle(color = WidgetColors.mutedProvider, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                )
                val nextStream = getNextStreamText()
                if (nextStream != null) {
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        "⏰ $nextStream",
                        style = TextStyle(color = WidgetColors.goldProvider, fontSize = 10.sp)
                    )
                }
            }
        }
    }
}

internal fun formatViewers(count: Int): String = when {
    count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
    else -> "$count"
}

internal fun formatRuntime(startedAt: String): String? {
    return try {
        val started = java.time.Instant.parse(startedAt)
        val now = java.time.Instant.now()
        val minutes = ChronoUnit.MINUTES.between(started, now)
        when {
            minutes < 60 -> "${minutes}m"
            else -> "${minutes / 60}h ${minutes % 60}m"
        }
    } catch (_: Exception) {
        null
    }
}

internal fun getNextStreamText(): String? {
    return try {
        val eastern = ZoneId.of("America/New_York")
        var next = ZonedDateTime.now(eastern).withHour(22).withMinute(0).withSecond(0)
        if (next.isBefore(ZonedDateTime.now(eastern))) {
            next = next.plusDays(1)
        }
        // Skip Sundays (DayOfWeek.SUNDAY = 7)
        for (i in 0..7) {
            if (next.dayOfWeek.value != 7) break
            next = next.plusDays(1)
        }
        val hoursUntil = ChronoUnit.HOURS.between(ZonedDateTime.now(eastern), next)
        when {
            hoursUntil < 1 -> "Next stream soon"
            hoursUntil < 24 -> "Next stream in ${hoursUntil}h"
            else -> "Next stream in ${hoursUntil / 24}d"
        }
    } catch (_: Exception) {
        null
    }
}

class StreamStatusSmallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreamStatusSmallWidget()
}
