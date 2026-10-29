package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swiftyspiffy.burkeblackapp.data.models.StreamDeckChannel
import com.swiftyspiffy.burkeblackapp.ui.components.*
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel, onNavigateToAccount: () -> Unit = {}) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PullToRefreshBox(
        isRefreshing = state.isLoading && state.fetchedAt != null,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize().pirateScreenBackground(),
    ) {
        Box(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                Modifier.widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                PiratePageHeader("Helm", Icons.Default.Home)
                Row(
                    Modifier.fillMaxWidth().selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    state.channels.forEach { channel ->
                        ChannelSelectorCard(
                            channel,
                            state.selectedChannel.key == channel.key,
                            state.fetchedAt != null,
                            state.isLoading,
                            Modifier.weight(1f),
                        ) {
                            viewModel.selectChannel(channel)
                        }
                    }
                }
                if (state.fetchedAt == null) {
                    PirateCard(Modifier.fillMaxWidth(), highlighted = true, cornerRadius = 20.dp) {
                        Column(
                            Modifier.fillMaxWidth().padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            ChannelProfileImage(state.selectedChannel, 112.dp)
                            Text(
                                if (state.isLoading) "Checking the horizon…" else "Lost at sea!",
                                fontFamily = PirateTheme.fontFamily,
                                fontSize = 28.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                            )
                            if (state.isLoading)
                                CircularProgressIndicator(
                                    color = PirateTheme.accentColor,
                                    modifier = Modifier.size(24.dp),
                                )
                            else {
                                Text(
                                    "Twitch status is temporarily unavailable.",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                )
                                Button(onClick = { viewModel.refresh() }) {
                                    Text("Try Again")
                                }
                            }
                        }
                    }
                } else {
                    StreamHeroCard(state.selectedChannel, state.allChannelsOffline, state.fetchedAt)
                }
                if (state.fetchedAt != null && (state.isStale || state.hasError)) {
                    Text(
                        "Showing the latest saved Twitch status.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier =
                            Modifier.background(Color.White.copy(alpha = 0.045f), CircleShape)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
                OutlinedButton(
                    onClick = onNavigateToAccount,
                    shape = CircleShape,
                    border =
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            PirateTheme.accentColor.copy(alpha = 0.55f),
                        ),
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = PirateTheme.accentColor.copy(alpha = 0.12f)
                        ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                ) {
                    Icon(Icons.Default.Person, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Captain’s Quarters",
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 19.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChannelSelectorCard(
    channel: StreamDeckChannel,
    isSelected: Boolean,
    statusKnown: Boolean,
    isLoading: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val accent = PirateTheme.accentColor
    PirateCard(
        modifier.semantics {
            selected = isSelected
            role = Role.RadioButton
        },
        onClick = onClick,
        highlighted = isSelected,
        textureIntensity = if (isSelected) 0.1f else 0.06f,
    ) {
        Column(
            Modifier.fillMaxWidth()
                .heightIn(min = 166.dp)
                .padding(horizontal = 6.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            ChannelProfileImage(channel, 58.dp)
            Text(
                channel.cardName,
                fontFamily = PirateTheme.fontFamily,
                fontSize = 18.sp,
                color = if (isSelected) accent else Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(
                    Modifier.size(8.dp)
                        .background(
                            if (statusKnown && channel.isLive) Color(0xFFE53935) else Color.Gray,
                            CircleShape,
                        )
                )
                Text(
                    if (!statusKnown) {
                        if (isLoading) "Checking…" else "Unavailable"
                    } else if (channel.isLive) "Live" else "Offline",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = if (channel.isLive) 1f else 0.6f),
                )
            }
            if (channel.key == "main" && !channel.isLive && statusKnown)
                MainStreamCountdown(compact = true)
            else Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun StreamHeroCard(
    channel: StreamDeckChannel,
    allChannelsOffline: Boolean,
    refreshId: String?,
) {
    val uriHandler = LocalUriHandler.current
    PirateCard(
        Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        highlighted = true,
        textureIntensity = 0.07f,
    ) {
        if (channel.isLive) {
            Box(Modifier.clickable(role = Role.Button) { uriHandler.openUri(channel.twitchUrl) }) {
                Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                    StreamThumbnail(channel, refreshId, Modifier.fillMaxSize())
                    Box(
                        Modifier.align(Alignment.Center)
                            .size(72.dp)
                            .background(Color.Black.copy(alpha = 0.68f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            "Watch ${channel.cardName}",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        if (!channel.gameName.isNullOrBlank())
                            MetadataBadge(
                                channel.gameName,
                                Icons.Default.SportsEsports,
                                Modifier.weight(1f, fill = false).padding(end = 8.dp),
                            )
                        else Spacer(Modifier.weight(1f))
                        channel.viewerCount?.let {
                            MetadataBadge(
                                "${StatFormatter.integer(it)} viewers",
                                Icons.Default.Visibility,
                            )
                        }
                    }
                }
            }
        }
        Column(
            Modifier.fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = if (channel.isLive) 20.dp else 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            if (!channel.isLive) ChannelProfileImage(channel, 126.dp)
            Text(
                if (channel.isLive) channel.heroTitle
                else if (allChannelsOffline && channel.key == "main") "All Channels Offline"
                else "${channel.cardName} is Offline",
                fontFamily = PirateTheme.fontFamily,
                fontSize = 29.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            if (channel.isLive && channel.key == "classics") {
                Text(
                    "Classic BurkeBlack playthroughs, always on.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
            } else if (!channel.isLive && channel.key == "main") MainStreamCountdown()
            else if (!channel.isLive)
                Text(
                    "This channel is currently offline.",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                )
            StreamWatchButton(
                if (channel.isLive) channel.watchButtonTitle else "Currently Offline",
                { uriHandler.openUri(channel.twitchUrl) },
                enabled = channel.isLive,
            )
        }
    }
}

@Composable
private fun MetadataBadge(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .background(
                Color.Black.copy(alpha = 0.72f),
                androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            )
            .padding(horizontal = 9.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(12.dp))
        Text(
            text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        )
    }
}
