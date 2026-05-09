package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftyspiffy.burkeblackapp.ui.theme.PirataOne
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateGold
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme

@Composable
fun RiggingScreen(
    onBack: () -> Unit = {},
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToGiveawaySettings: () -> Unit,
    onNavigateToAppearance: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        com.swiftyspiffy.burkeblackapp.util.AppLogger.log("Rigging: appeared")
    }

    val accent = PirateTheme.accentColor
    val font = PirateTheme.fontFamily
    val cardGradient = PirateTheme.cardGradient
    val iconBg = PirateTheme.iconBgColor

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rigging",
                    fontFamily = font,
                    fontSize = 28.sp,
                    color = accent
                )
            }

            SettingsCard(
                icon = Icons.Default.Notifications,
                iconBgColor = iconBg,
                title = "Notifications",
                subtitle = "Signal flags & stream lookouts",
                onClick = onNavigateToNotificationSettings,
                accentColor = accent,
                fontFamily = font,
                gradient = cardGradient
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsCard(
                icon = Icons.Default.CardGiftcard,
                iconBgColor = iconBg,
                title = "Giveaways",
                subtitle = "Plunder & treasure settings",
                onClick = onNavigateToGiveawaySettings,
                accentColor = accent,
                fontFamily = font,
                gradient = cardGradient
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsCard(
                icon = Icons.Default.Palette,
                iconBgColor = iconBg,
                title = "Appearance",
                subtitle = "Customize the look of yer app",
                onClick = onNavigateToAppearance,
                accentColor = accent,
                fontFamily = font,
                gradient = cardGradient
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsCard(
                icon = Icons.Default.Info,
                iconBgColor = iconBg,
                title = "About",
                subtitle = "The Dirty Skull app info",
                onClick = onNavigateToAbout,
                accentColor = accent,
                fontFamily = font,
                gradient = cardGradient
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsCard(
    icon: ImageVector,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    accentColor: Color = PirateGold,
    fontFamily: androidx.compose.ui.text.font.FontFamily = PirataOne,
    gradient: Brush = Brush.horizontalGradient(listOf(Color(0xFF1C1208), Color(0xFF2A1E10)))
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = accentColor, fontFamily = fontFamily, fontSize = 18.sp)
                Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontFamily = fontFamily, fontSize = 12.sp)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = accentColor.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
