package com.swiftyspiffy.burkeblackapp.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swiftyspiffy.burkeblackapp.R
import com.swiftyspiffy.burkeblackapp.data.models.StreamDeckChannel
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.util.MainStreamSchedule
import java.time.Instant
import kotlinx.coroutines.delay

@Composable
fun ChannelProfileImage(channel: StreamDeckChannel, size: Dp, modifier: Modifier = Modifier) {
    AsyncImage(
        model = channel.profileImageUrl,
        contentDescription = null,
        placeholder = painterResource(R.drawable.app_icon),
        error = painterResource(R.drawable.app_icon),
        fallback = painterResource(R.drawable.app_icon),
        contentScale = ContentScale.Crop,
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .border(1.dp, PirateTheme.accentColor.copy(alpha = 0.7f), CircleShape),
    )
}

@Composable
fun StreamThumbnail(channel: StreamDeckChannel, refreshId: String?, modifier: Modifier = Modifier) {
    val model =
        remember(channel.thumbnailUrl, refreshId) {
            channel.thumbnailUrl?.let { url ->
                Uri.parse(url)
                    .buildUpon()
                    .apply { if (refreshId != null) appendQueryParameter("refresh", refreshId) }
                    .build()
                    .toString()
            }
        }
    Box(
        modifier.background(Color.White.copy(alpha = 0.035f)),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = model,
            contentDescription = null,
            placeholder = painterResource(R.drawable.app_icon),
            error = painterResource(R.drawable.app_icon),
            fallback = painterResource(R.drawable.app_icon),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier.fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.12f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.42f),
                        )
                    )
                )
        )
    }
}

@Composable
fun StreamWatchButton(
    title: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    outlined: Boolean = false,
) {
    val accent = PirateTheme.accentColor
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        border =
            androidx.compose.foundation.BorderStroke(
                1.dp,
                accent.copy(alpha = if (enabled) 0.55f else 0.12f),
            ),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = if (outlined) accent.copy(alpha = 0.11f) else accent,
                contentColor = if (outlined) accent else Color.Black,
                disabledContainerColor = Color.White.copy(alpha = 0.07f),
                disabledContentColor = Color.White.copy(alpha = 0.5f),
            ),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
    ) {
        Icon(
            if (enabled) Icons.Default.PlayArrow else Icons.Default.Schedule,
            null,
            Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            fontFamily = PirateTheme.fontFamily,
            fontSize = 19.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun MainStreamCountdown(compact: Boolean = false) {
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(compact) {
        while (true) {
            now = Instant.now()
            delay(if (compact) 30_000 else 1_000)
        }
    }
    Text(
        text =
            (if (compact) "Next in " else "The next voyage begins in ") +
                MainStreamSchedule.countdownText(now, !compact),
        color = Color.White.copy(alpha = if (compact) 0.5f else 0.6f),
        fontSize = if (compact) 10.sp else 14.sp,
        style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
        textAlign = TextAlign.Center,
    )
}

@Composable
fun ClassicsFeatureCard(
    channel: StreamDeckChannel,
    refreshId: String?,
    statusKnown: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    PirateCard(modifier.fillMaxWidth(), cornerRadius = 18.dp, highlighted = true) {
        Box(
            Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button) {
                uriHandler.openUri(channel.twitchUrl)
            }
        ) {
            Box(
                Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                contentAlignment = Alignment.Center,
            ) {
                if (channel.isLive) StreamThumbnail(channel, refreshId, Modifier.fillMaxSize())
                else ChannelProfileImage(channel, 112.dp)
                Row(
                    Modifier.align(Alignment.TopStart)
                        .padding(12.dp)
                        .background(
                            if (channel.isLive) Color(0xFFC82828)
                            else Color.Black.copy(alpha = 0.68f),
                            RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        Modifier.size(8.dp)
                            .background(
                                if (channel.isLive) Color.White else Color.Gray,
                                CircleShape,
                            )
                    )
                    Text(
                        if (!statusKnown) {
                            if (isLoading) "CHECKING STATUS" else "STATUS UNAVAILABLE"
                        } else if (channel.isLive) "LIVE" else "OFFLINE",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ChannelProfileImage(channel, 64.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    "The 24/7 Archive",
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 25.sp,
                    color = Color.White,
                )
                Text(
                    "Classic BurkeBlack playthroughs, always on.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                )
                StreamWatchButton(
                    "Watch on Twitch",
                    { uriHandler.openUri(channel.twitchUrl) },
                    outlined = true,
                )
            }
        }
    }
}
