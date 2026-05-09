package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request

private data class LateShiftStreamer(
    val login: String,
    val displayName: String = "",
    val profileImageUrl: String = "",
    val offlineImageUrl: String = "",
    val broadcasterId: String = "",
    val followerCount: Int = -1,
    val isLive: Boolean = false,
    val viewerCount: Int = 0,
    val streamTitle: String = "",
    val gameName: String = ""
)

private val LATE_SHIFT_LOGINS = listOf("crream", "gassymexican", "burkeblack", "cletusbueford")

@Composable
fun LateShiftScreen(
    token: String?,
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var streamers by remember { mutableStateOf<List<LateShiftStreamer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(token) {
        isLoading = true
        errorMessage = null
        try {
            val twitchCreds = if (token != null) {
                val resp = ApiClient.api.fetchTwitchToken("Bearer $token")
                if (resp.success && resp.data != null) resp.data else null
            } else null

            if (twitchCreds == null) {
                errorMessage = "Could not fetch Twitch credentials"
                isLoading = false
                return@LaunchedEffect
            }

            val twitchToken = twitchCreds.accessToken
            val clientId = twitchCreds.clientId

            val results = withContext(Dispatchers.IO) {
                val client = OkHttpClient()

                // Batch get users
                val usersQuery = LATE_SHIFT_LOGINS.joinToString("&") { "login=$it" }
                val usersRequest = Request.Builder()
                    .url("https://api.twitch.tv/helix/users?$usersQuery")
                    .addHeader("Authorization", "Bearer $twitchToken")
                    .addHeader("Client-Id", clientId)
                    .build()
                val usersResponse = client.newCall(usersRequest).execute()
                val usersJson = kotlinx.serialization.json.Json.parseToJsonElement(
                    usersResponse.body?.string() ?: "{}"
                ).jsonObject
                val usersData = usersJson["data"]?.jsonArray ?: kotlinx.serialization.json.JsonArray(emptyList())

                val userMap = mutableMapOf<String, LateShiftStreamer>()
                for (user in usersData) {
                    val obj = user.jsonObject
                    val login = obj["login"]?.jsonPrimitive?.content ?: continue
                    userMap[login.lowercase()] = LateShiftStreamer(
                        login = login,
                        displayName = obj["display_name"]?.jsonPrimitive?.content ?: login,
                        profileImageUrl = obj["profile_image_url"]?.jsonPrimitive?.content ?: "",
                        offlineImageUrl = obj["offline_image_url"]?.jsonPrimitive?.content ?: "",
                        broadcasterId = obj["id"]?.jsonPrimitive?.content ?: ""
                    )
                }

                // Fetch follower counts per user
                val followerMap = mutableMapOf<String, Int>()
                for ((login, streamer) in userMap) {
                    if (streamer.broadcasterId.isBlank()) continue
                    try {
                        val followersRequest = Request.Builder()
                            .url("https://api.twitch.tv/helix/channels/followers?broadcaster_id=${streamer.broadcasterId}&first=1")
                            .addHeader("Authorization", "Bearer $twitchToken")
                            .addHeader("Client-Id", clientId)
                            .build()
                        val followersResponse = client.newCall(followersRequest).execute()
                        val followersJson = kotlinx.serialization.json.Json.parseToJsonElement(
                            followersResponse.body?.string() ?: "{}"
                        ).jsonObject
                        val total = followersJson["total"]?.jsonPrimitive?.content?.toIntOrNull()
                        if (total != null) followerMap[login] = total
                    } catch (_: Exception) { }
                }

                // Batch get streams
                val streamsQuery = LATE_SHIFT_LOGINS.joinToString("&") { "user_login=$it" }
                val streamsRequest = Request.Builder()
                    .url("https://api.twitch.tv/helix/streams?$streamsQuery")
                    .addHeader("Authorization", "Bearer $twitchToken")
                    .addHeader("Client-Id", clientId)
                    .build()
                val streamsResponse = client.newCall(streamsRequest).execute()
                val streamsJson = kotlinx.serialization.json.Json.parseToJsonElement(
                    streamsResponse.body?.string() ?: "{}"
                ).jsonObject
                val streamsData = streamsJson["data"]?.jsonArray ?: kotlinx.serialization.json.JsonArray(emptyList())

                val liveMap = mutableMapOf<String, Triple<Int, String, String>>() // login -> (viewers, title, game)
                for (stream in streamsData) {
                    val obj = stream.jsonObject
                    val login = obj["user_login"]?.jsonPrimitive?.content?.lowercase() ?: continue
                    val viewers = obj["viewer_count"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                    val title = obj["title"]?.jsonPrimitive?.content ?: ""
                    val game = obj["game_name"]?.jsonPrimitive?.content ?: ""
                    liveMap[login] = Triple(viewers, title, game)
                }

                // Build final list in display order
                LATE_SHIFT_LOGINS.mapNotNull { login ->
                    val key = login.lowercase()
                    val user = userMap[key] ?: return@mapNotNull LateShiftStreamer(login = login, displayName = login)
                    val live = liveMap[key]
                    val followers = followerMap[key] ?: -1
                    val withFollowers = user.copy(followerCount = followers)
                    if (live != null) {
                        withFollowers.copy(isLive = true, viewerCount = live.first, streamTitle = live.second, gameName = live.third)
                    } else {
                        withFollowers
                    }
                }
            }

            streamers = results
            AppLogger.log("LateShift: loaded ${results.size} streamers, ${results.count { it.isLive }} live")
        } catch (e: Exception) {
            AppLogger.log("LateShift: error loading data: ${e.message}")
            errorMessage = "Failed to load streamer data"
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PirateTheme.accentColor
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = androidx.compose.ui.res.painterResource(com.swiftyspiffy.burkeblackapp.R.drawable.ic_lateshift),
                contentDescription = "The Late Shift",
                tint = Color.Unspecified,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "The Late Shift",
                fontFamily = PirateTheme.fontFamily,
                fontSize = 22.sp,
                color = PirateTheme.accentColor
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PirateTheme.accentColor, modifier = Modifier.size(32.dp))
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Lost at sea!",
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 20.sp,
                        color = PirateTheme.accentColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                streamers.forEach { streamer ->
                    StreamerSlice(
                        streamer = streamer,
                        modifier = Modifier.weight(1f),
                        onClick = { uriHandler.openUri("https://twitch.tv/${streamer.login}") }
                    )
                }
            }
        }
    }
}

@Composable
private fun StreamerSlice(
    streamer: LateShiftStreamer,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Pulsing animation for live indicator
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val liveAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        // Banner image (offline_image_url or fallback dark background)
        if (streamer.offlineImageUrl.isNotBlank()) {
            AsyncImage(
                model = streamer.offlineImageUrl,
                contentDescription = "${streamer.displayName} banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1A0D28), Color(0xFF2A1A3A))
                        )
                    )
            )
        }

        // Dark scrim over entire banner
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
        )

        // Gold border accent if live
        if (streamer.isLive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, PirateTheme.accentColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            )
        }

        // Content row with solid dark backing
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        if (streamer.isLive) PirateTheme.accentColor else Color.White.copy(alpha = 0.2f),
                        CircleShape
                    )
            ) {
                if (streamer.profileImageUrl.isNotBlank()) {
                    AsyncImage(
                        model = streamer.profileImageUrl,
                        contentDescription = streamer.displayName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Name + stream info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = streamer.displayName,
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 22.sp,
                    color = PirateTheme.accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (streamer.followerCount >= 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = "Followers",
                            tint = Color.White.copy(alpha = 0.45f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${formatLateShiftCount(streamer.followerCount)} followers",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 12.sp
                        )
                    }
                }
                if (streamer.isLive && streamer.gameName.isNotBlank()) {
                    Text(
                        text = streamer.gameName,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (streamer.isLive && streamer.streamTitle.isNotBlank()) {
                    Text(
                        text = streamer.streamTitle,
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Live indicator + viewer count, or Offline badge
            if (streamer.isLive) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pulsing red dot
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFCC0000).copy(alpha = 0.9f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Circle,
                            contentDescription = "Live",
                            tint = Color.Red.copy(alpha = liveAlpha),
                            modifier = Modifier.size(8.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "LIVE",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    // Viewer count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "Viewers",
                            tint = PirateTheme.accentColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatLateShiftCount(streamer.viewerCount),
                            color = PirateTheme.accentColor.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontFamily = PirateTheme.fontFamily
                        )
                    }
                }
            } else {
                Text(
                    text = "Offline",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 12.sp,
                    fontFamily = PirateTheme.fontFamily
                )
            }
        }
    }
}

private fun formatLateShiftCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 10_000 -> String.format("%.1fK", count / 1_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> "$count"
    }
}
