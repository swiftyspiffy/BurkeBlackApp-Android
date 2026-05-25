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
import androidx.glance.unit.ColorProvider
import com.swiftyspiffy.burkeblackapp.MainActivity

class CrewStatsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isLoggedIn = WidgetDataStore.isLoggedIn(context)
        val doubloons = WidgetDataStore.getDoubloons(context)
        val soundbytes = WidgetDataStore.getSoundbyteCredits(context)
        val followMonths = WidgetDataStore.getFollowMonths(context)
        val subMonths = WidgetDataStore.getSubMonths(context)

        provideContent {
            GlanceTheme {
                CrewStatsContent(isLoggedIn, doubloons, soundbytes, followMonths, subMonths)
            }
        }
    }
}

@Composable
private fun CrewStatsContent(
    isLoggedIn: Boolean,
    doubloons: Int,
    soundbytes: Int,
    followMonths: Int,
    subMonths: Int
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetColors.darkBrown)
            .clickable(actionStartActivity<MainActivity>())
            .padding(12.dp)
    ) {
        if (!isLoggedIn) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "👤",
                    style = TextStyle(fontSize = 24.sp)
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    "Log In",
                    style = TextStyle(color = WidgetColors.goldProvider, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                )
                Text(
                    "Open app to see yer stats",
                    style = TextStyle(color = WidgetColors.mutedProvider, fontSize = 10.sp)
                )
            }
        } else {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Text(
                    "Crew Stats",
                    style = TextStyle(color = WidgetColors.goldProvider, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                    StatCell("⭐", formatNumber(doubloons), "Doubloons", GlanceModifier.defaultWeight())
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    StatCell("🔊", "$soundbytes", "Soundbytes", GlanceModifier.defaultWeight())
                }
                Spacer(modifier = GlanceModifier.height(6.dp))
                Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                    StatCell("❤️", "$followMonths", "Follow Mo.", GlanceModifier.defaultWeight())
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    StatCell("⭐", "$subMonths", "Sub Mo.", GlanceModifier.defaultWeight())
                }
            }
        }
    }
}

@Composable
private fun StatCell(icon: String, value: String, label: String, modifier: GlanceModifier) {
    Box(
        modifier = modifier
            .background(WidgetColors.medBrown)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, style = TextStyle(fontSize = 12.sp))
            Text(
                value,
                style = TextStyle(color = WidgetColors.whiteProvider, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            )
            Text(
                label,
                style = TextStyle(color = WidgetColors.mutedProvider, fontSize = 9.sp)
            )
        }
    }
}

private fun formatNumber(n: Int): String = when {
    n >= 1_000_000 -> String.format("%.1fM", n / 1_000_000.0)
    n >= 1_000 -> String.format("%.1fK", n / 1_000.0)
    else -> "$n"
}

class CrewStatsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CrewStatsWidget()
}
