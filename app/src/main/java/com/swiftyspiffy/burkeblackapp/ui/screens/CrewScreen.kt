package com.swiftyspiffy.burkeblackapp.ui.screens

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.swiftyspiffy.burkeblackapp.R
import androidx.compose.ui.unit.sp
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.util.AppLogger

@Composable
fun CrewScreen(
    username: String = "",
    onNavigateToClipVoting: () -> Unit = {},
    onNavigateToCommunityServers: () -> Unit = {},
    onNavigateToEmotes: () -> Unit = {},
    onNavigateToStudio: () -> Unit = {},
    onNavigateToFaq: () -> Unit = {},
    onNavigateToLateShift: () -> Unit = {}
) {
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(Unit) { AppLogger.log("Crew: appeared") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "The Crew",
            fontFamily = PirateTheme.fontFamily,
            fontSize = 28.sp,
            color = PirateTheme.accentColor,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        val cardGradient = PirateTheme.cardGradient
        val cardIconBg = PirateTheme.iconBgColor

        // Monthly Twitch Clip Voting
        CrewActionCard(
            title = "Monthly Twitch Clip Voting",
            subtitle = "Vote fer the finest plunder of the month",
            iconPainter = painterResource(R.drawable.ic_twitch_clip),
            gradient = cardGradient,
            iconBackgroundColor = cardIconBg,
            onClick = onNavigateToClipVoting
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Community Game Servers
        CrewActionCard(
            title = "Community Game Servers",
            subtitle = "Join yer fellow pirates on the high seas",
            icon = Icons.Default.SportsEsports,
            gradient = cardGradient,
            iconBackgroundColor = cardIconBg,
            onClick = onNavigateToCommunityServers
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Captain's Studio
        CrewActionCard(
            title = "Captain's Studio",
            subtitle = "Explore the Captain's creative works",
            iconPainter = painterResource(R.drawable.ic_burke_captain),
            gradient = cardGradient,
            iconBackgroundColor = cardIconBg,
            onClick = onNavigateToStudio
        )

        Spacer(modifier = Modifier.height(16.dp))

        // The Late Shift
        CrewActionCard(
            title = "The Late Shift",
            subtitle = "The Late Shift Twitch stream team",
            iconPainter = painterResource(R.drawable.ic_lateshift),
            gradient = cardGradient,
            iconBackgroundColor = cardIconBg,
            onClick = onNavigateToLateShift
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Emotes Gallery
        CrewActionCard(
            title = "Emotes, Bits, Badges & Cheermotes",
            subtitle = "Browse the Captain's treasure chest of emotes",
            iconPainter = painterResource(R.drawable.ic_burke_emote),
            gradient = cardGradient,
            iconBackgroundColor = cardIconBg,
            onClick = onNavigateToEmotes
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Information & FAQ
        CrewActionCard(
            title = "Information & FAQ",
            subtitle = "Charts and maps fer the lost sailor",
            icon = Icons.Default.HelpOutline,
            gradient = cardGradient,
            iconBackgroundColor = cardIconBg,
            onClick = onNavigateToFaq
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Apply to be Moderator
        CrewActionCard(
            title = "Apply to be Moderator",
            subtitle = "Defend The Dirty Skull from scallywags",
            iconPainter = painterResource(R.drawable.ic_mod_badge),
            gradient = cardGradient,
            iconBackgroundColor = cardIconBg,
            onClick = {
                val formUrl = "https://docs.google.com/forms/d/e/1FAIpQLSfSzrzgJLfLqXXPzVv7ejfUnV_x5abdNHd3tdV3H-Gjl7nqtg/viewform"
                CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(formUrl))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CrewActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    iconPainter: Painter? = null,
    gradient: Brush,
    iconBackgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    gradient,
                    RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Icon circle
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                iconBackgroundColor.copy(alpha = 0.3f),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (iconPainter != null) {
                            Image(
                                painter = iconPainter,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp)
                            )
                        } else if (icon != null) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = PirateTheme.accentColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = title,
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 20.sp,
                            color = PirateTheme.accentColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }

                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Go",
                    tint = PirateTheme.accentColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
