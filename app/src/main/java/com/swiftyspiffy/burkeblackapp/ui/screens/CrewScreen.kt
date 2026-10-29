package com.swiftyspiffy.burkeblackapp.ui.screens

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.swiftyspiffy.burkeblackapp.R
import com.swiftyspiffy.burkeblackapp.ui.components.PirateActionCard
import com.swiftyspiffy.burkeblackapp.ui.components.PiratePageHeader
import com.swiftyspiffy.burkeblackapp.ui.components.pirateScreenBackground
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.util.AppLogger

@Composable
fun CrewScreen(
    username: String = "",
    onNavigateToCaptains: () -> Unit = {},
    onNavigateToClipVoting: () -> Unit = {},
    onNavigateToCommunityServers: () -> Unit = {},
    onNavigateToEmotes: () -> Unit = {},
    onNavigateToStudio: () -> Unit = {},
    onNavigateToFaq: () -> Unit = {},
    onNavigateToLateShift: () -> Unit = {},
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { AppLogger.log("Crew: appeared") }

    Box(Modifier.fillMaxSize().pirateScreenBackground(), contentAlignment = Alignment.TopCenter) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(300.dp),
            modifier = Modifier.widthIn(max = 960.dp).fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PiratePageHeader(
                    "The Crew",
                    Icons.Default.Groups,
                    "Gather, explore, and sail with the community.",
                )
            }
            item {
                PirateActionCard("Captains", "Choose who leads the crew", onNavigateToCaptains) {
                    Icon(Icons.Default.HowToVote, null, tint = PirateTheme.accentColor, modifier = Modifier.size(28.dp))
                }
            }
            item {
                PirateActionCard(
                    "Monthly Twitch Clip Voting",
                    "Vote fer the finest plunder of the month",
                    onNavigateToClipVoting,
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        null,
                        tint = PirateTheme.accentColor,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            item {
                PirateActionCard(
                    "Community Game Servers",
                    "Join yer fellow pirates on the high seas",
                    onNavigateToCommunityServers,
                ) {
                    Icon(
                        Icons.Default.SportsEsports,
                        null,
                        tint = PirateTheme.accentColor,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            item {
                PirateActionCard(
                    "Captain’s Studio",
                    "Explore the Captain’s creative works",
                    onNavigateToStudio,
                ) {
                    Image(painterResource(R.drawable.ic_burke_captain), null, Modifier.size(36.dp))
                }
            }
            item {
                PirateActionCard(
                    "The Late Shift",
                    "The Late Shift Twitch stream team",
                    onNavigateToLateShift,
                ) {
                    Image(painterResource(R.drawable.ic_lateshift), null, Modifier.size(36.dp))
                }
            }
            item {
                PirateActionCard(
                    "Emotes, Bits, Badges & Cheermotes",
                    "Browse the Captain’s treasure chest of emotes",
                    onNavigateToEmotes,
                ) {
                    Image(painterResource(R.drawable.ic_burke_emote), null, Modifier.size(36.dp))
                }
            }
            item {
                PirateActionCard(
                    "Information & FAQ",
                    "Charts and maps fer the lost sailor",
                    onNavigateToFaq,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.HelpOutline,
                        null,
                        tint = PirateTheme.accentColor,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            item {
                PirateActionCard(
                    "Apply to be Moderator",
                    "Defend The Dirty Skull from scallywags",
                    {
                        val formUrl =
                            "https://docs.google.com/forms/d/e/1FAIpQLSfSzrzgJLfLqXXPzVv7ejfUnV_x5abdNHd3tdV3H-Gjl7nqtg/viewform"
                        CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(formUrl))
                    },
                ) {
                    Image(painterResource(R.drawable.ic_mod_badge), null, Modifier.size(36.dp))
                }
            }
        }
    }
}
