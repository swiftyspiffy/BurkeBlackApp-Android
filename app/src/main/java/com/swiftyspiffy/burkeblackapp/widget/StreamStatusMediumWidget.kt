package com.swiftyspiffy.burkeblackapp.widget

import android.content.Context
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

class StreamStatusMediumWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isLive = WidgetDataStore.isStreamLive(context)
        val title = WidgetDataStore.getStreamTitle(context)
        val gameName = WidgetDataStore.getStreamGameName(context)
        val viewerCount = WidgetDataStore.getStreamViewerCount(context)
        val startedAt = WidgetDataStore.getStreamStartedAt(context)

        provideContent {
            GlanceTheme {
                MediumStatusContent(isLive, title, gameName, viewerCount, startedAt)
            }
        }
    }
}

@Composable
private fun MediumStatusContent(
    isLive: Boolean,
    title: String?,
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
            Column(modifier = GlanceModifier.fillMaxSize()) {
                // Top row: LIVE badge + runtime + viewers
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

                    if (startedAt != null) {
                        val runtime = formatRuntime(startedAt)
                        if (runtime != null) {
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            Text(
                                "⏱ $runtime",
                                style = TextStyle(color = WidgetColors.mutedProvider, fontSize = 11.sp)
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    Text(
                        "👁 ${formatViewers(viewerCount)}",
                        style = TextStyle(color = WidgetColors.whiteProvider, fontSize = 12.sp)
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Game name
                gameName?.let {
                    Text(
                        it,
                        style = TextStyle(color = WidgetColors.goldProvider, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        maxLines = 1
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                }

                // Stream title
                title?.let {
                    Text(
                        it,
                        style = TextStyle(color = WidgetColors.whiteProvider, fontSize = 12.sp),
                        maxLines = 2
                    )
                }
            }
        } else {
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📺", style = TextStyle(fontSize = 32.sp))
                Spacer(modifier = GlanceModifier.width(12.dp))
                Column {
                    Text(
                        "Stream Offline",
                        style = TextStyle(color = WidgetColors.mutedProvider, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    )
                    val nextStream = getNextStreamText()
                    if (nextStream != null) {
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            "⏰ $nextStream",
                            style = TextStyle(color = WidgetColors.goldProvider, fontSize = 12.sp)
                        )
                    }
                }
            }
        }
    }
}

class StreamStatusMediumWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreamStatusMediumWidget()
}
