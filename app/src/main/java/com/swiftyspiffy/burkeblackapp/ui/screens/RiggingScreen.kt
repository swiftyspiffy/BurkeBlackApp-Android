package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.swiftyspiffy.burkeblackapp.ui.components.PirateActionCard
import com.swiftyspiffy.burkeblackapp.ui.components.PiratePageHeader
import com.swiftyspiffy.burkeblackapp.ui.components.pirateScreenBackground
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.util.AppLogger

@Composable
fun RiggingScreen(
    onBack: () -> Unit = {},
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToGiveawaySettings: () -> Unit,
    onNavigateToCaptainsSettings: () -> Unit = {},
    onNavigateToAppearance: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
) {
    LaunchedEffect(Unit) { AppLogger.log("Rigging: appeared") }
    Box(Modifier.fillMaxSize().pirateScreenBackground(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 720.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            PiratePageHeader("Rigging", Icons.Default.Settings, "Tune the app to suit your voyage.")
            PirateActionCard(
                "Notifications",
                "Signal flags & stream lookouts",
                onNavigateToNotificationSettings,
                iconBackground = Color(0xFF593516),
            ) {
                Icon(
                    Icons.Default.Notifications,
                    null,
                    tint = PirateTheme.accentColor,
                    modifier = Modifier.size(26.dp),
                )
            }
            PirateActionCard(
                "Giveaways",
                "Plunder & treasure settings",
                onNavigateToGiveawaySettings,
                iconBackground = Color(0xFF52283A),
            ) {
                Icon(
                    Icons.Default.CardGiftcard,
                    null,
                    tint = PirateTheme.accentColor,
                    modifier = Modifier.size(26.dp),
                )
            }
            PirateActionCard("Captains", "Election alerts & voting preferences", onNavigateToCaptainsSettings) {
                Icon(Icons.Default.HowToVote, null, tint = PirateTheme.accentColor, modifier = Modifier.size(26.dp))
            }
            PirateActionCard(
                "Appearance",
                "Theme & visual settings",
                onNavigateToAppearance,
                iconBackground = Color(0xFF3C2852),
            ) {
                Icon(
                    Icons.Default.Palette,
                    null,
                    tint = PirateTheme.accentColor,
                    modifier = Modifier.size(26.dp),
                )
            }
            PirateActionCard(
                "About",
                "The Dirty Skull app info",
                onNavigateToAbout,
                iconBackground = Color(0xFF203B59),
            ) {
                Icon(
                    Icons.Default.Info,
                    null,
                    tint = PirateTheme.accentColor,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}
