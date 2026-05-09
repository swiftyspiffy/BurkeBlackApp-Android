package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swiftyspiffy.burkeblackapp.R
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.ui.theme.TwitchPurple
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToAccount: () -> Unit = {}
) {
    val streamStatus by viewModel.streamStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uriHandler = LocalUriHandler.current
    val isLive = streamStatus?.isLive == true

    LaunchedEffect(Unit) { AppLogger.log("Helm: appeared") }

    // Re-check stream status when app returns from background
    LifecycleResumeEffect(Unit) {
        viewModel.checkStreamStatus()
        onPauseOrDispose { }
    }

    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { viewModel.checkStreamStatus(force = true) },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Stream status at top
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

        if (isLive) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { uriHandler.openUri("https://twitch.tv/burkeblack") }
            ) {
                // LIVE indicator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF0000))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "LIVE",
                        color = Color(0xFFFF0000),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Stream title
                streamStatus?.title?.let {
                    Text(
                        text = it,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                // Game + viewers
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    streamStatus?.gameName?.let {
                        Text(text = it, color = PirateTheme.accentColor, fontSize = 12.sp)
                    }
                    streamStatus?.viewerCount?.let {
                        Text(
                            text = "$it viewers",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else if (!isLoading) {
            // Offline indicator + countdown
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF666666))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Stream Offline", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                StreamCountdown()
            }
        }

        } // End stream status Box

        // Centered profile image + text group
        Spacer(modifier = Modifier.height(4.dp))

        AnimatedGlowProfileImage(onTap = { uriHandler.openUri("https://twitch.tv/burkeblack") })

        Spacer(modifier = Modifier.height(16.dp))

        // Title and subtitle
        Text(
            text = "The Dirty Skull",
            fontFamily = PirateTheme.fontFamily,
            fontSize = 34.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        // "Home of the Pirates on Twitch and YouTube!"
        Row {
            Text("Home of the Pirates on ", fontFamily = PirateTheme.fontFamily, fontSize = 16.sp, color = Color.White)
            Text(
                "Twitch", fontFamily = PirateTheme.fontFamily, fontSize = 16.sp, color = TwitchPurple,
                modifier = Modifier.clickable { uriHandler.openUri("https://twitch.tv/burkeblack") }
            )
            Text(" and ", fontFamily = PirateTheme.fontFamily, fontSize = 16.sp, color = Color.White)
            Text(
                "YouTube", fontFamily = PirateTheme.fontFamily, fontSize = 16.sp, color = Color(0xFFFF0000),
                modifier = Modifier.clickable { uriHandler.openUri("https://youtube.com/@BurkeBlack") }
            )
            Text("!", fontFamily = PirateTheme.fontFamily, fontSize = 16.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Captain's Quarters button
        Button(
            onClick = onNavigateToAccount,
            colors = ButtonDefaults.buttonColors(
                containerColor = PirateTheme.accentColor.copy(alpha = 0.15f),
                contentColor = PirateTheme.accentColor
            ),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PirateTheme.accentColor.copy(alpha = 0.5f)),
            modifier = Modifier.wrapContentWidth()
        ) {
            Text(
                text = "\u2693 Captain's Quarters",
                fontFamily = PirateTheme.fontFamily,
                fontSize = 20.sp,
                color = PirateTheme.accentColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
    } // End PullToRefreshBox
}

@Composable
private fun AnimatedGlowProfileImage(onTap: () -> Unit = {}) {
    val transition = rememberInfiniteTransition(label = "glow")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glowAngle"
    )

    val accentColor = PirateTheme.accentColor
    val glowColors = listOf(
        accentColor,
        accentColor.copy(alpha = 0.4f),
        Color(0xFF482934),
        Color(0xFF482934).copy(alpha = 0.4f),
        accentColor
    )

    Box(
        modifier = Modifier.fillMaxWidth().height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow halo - large and bright like iOS
        Canvas(modifier = Modifier.fillMaxSize()) {
            val glowRadius = size.maxDimension * 0.55f
            rotate(angle, pivot = center) {
                // Outermost glow - very wide
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.5f),
                            accentColor.copy(alpha = 0.2f),
                            accentColor.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = center
                )
                // Mid glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.6f),
                            accentColor.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = glowRadius * 0.7f
                    ),
                    radius = glowRadius * 0.7f,
                    center = center
                )
                // Inner hot glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.7f),
                            accentColor.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = glowRadius * 0.5f
                    ),
                    radius = glowRadius * 0.5f,
                    center = center
                )
            }
        }

        // Ring + image container
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            // Rotating glow ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                rotate(angle, pivot = center) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = glowColors,
                            center = center
                        ),
                        radius = size.minDimension / 2f,
                        center = center
                    )
                    drawCircle(
                        color = Color(0xFF121212),
                        radius = size.minDimension / 2f - 8f,
                        center = center
                    )
                }
            }

            // Profile image - tappable to open Twitch
            Image(
                painter = painterResource(R.drawable.app_icon),
                contentDescription = "The Dirty Skull",
                modifier = Modifier
                    .size(184.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onTap),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun StreamCountdown() {
    var timeUntilStream by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            timeUntilStream = calculateTimeUntilNextStream()
            delay(1000)
        }
    }

    if (timeUntilStream > 0) {
        val hours = timeUntilStream / 3600
        val minutes = (timeUntilStream % 3600) / 60
        val seconds = timeUntilStream % 60
        Text(
            text = "Next stream in ${hours}h %02dm %02ds".format(minutes, seconds),
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun CountdownUnit(value: Long, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = String.format("%02d", value),
            fontFamily = PirateTheme.fontFamily,
            fontSize = 32.sp,
            color = PirateTheme.accentColor
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.Bold
        )
    }
}

private fun calculateTimeUntilNextStream(): Long {
    val est = TimeZone.getTimeZone("America/New_York")
    val now = Calendar.getInstance(est)
    val target = Calendar.getInstance(est).apply {
        set(Calendar.HOUR_OF_DAY, 22)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // If past 10 PM or Sunday, move to next valid day
    if (now.after(target)) {
        target.add(Calendar.DAY_OF_YEAR, 1)
    }
    // Skip Sundays
    while (target.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
        target.add(Calendar.DAY_OF_YEAR, 1)
    }

    return (target.timeInMillis - now.timeInMillis) / 1000
}
