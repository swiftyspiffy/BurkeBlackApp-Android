package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class EmoteItem(val id: String, val name: String, val url: String)
data class EmoteSection(val title: String, val emotes: List<EmoteItem>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmotesGalleryScreen(
    token: String?,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var sections by remember { mutableStateOf<List<EmoteSection>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var fullscreenEmotes by remember { mutableStateOf<List<EmoteItem>?>(null) }
    var fullscreenIndex by remember { mutableIntStateOf(0) }

    fun loadEmotes() {
        if (token == null) return
        isLoading = true
        errorMessage = null
        scope.launch {
            try {
                AppLogger.log("EmotesGallery: loading emotes")
                val tokenResp = ApiClient.api.fetchTwitchToken("Bearer $token")
                if (!tokenResp.success || tokenResp.data == null) {
                    errorMessage = "No Wind in the Sails"
                    isLoading = false
                    return@launch
                }

                val twitchToken = tokenResp.data.accessToken
                val clientId = tokenResp.data.clientId

                val allSections = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                // Fetch emotes via backend proxy or direct Twitch API
                // Using OkHttp since Retrofit is set up for our API base URL
                val client = okhttp3.OkHttpClient()
                val broadcasterId = "44338537"

                // Emotes
                val emotesRequest = okhttp3.Request.Builder()
                    .url("https://api.twitch.tv/helix/chat/emotes?broadcaster_id=$broadcasterId")
                    .addHeader("Authorization", "Bearer $twitchToken")
                    .addHeader("Client-Id", clientId)
                    .build()

                val emotesResponse = client.newCall(emotesRequest).execute()
                val emotesJson = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                    .parseToJsonElement(emotesResponse.body?.string() ?: "{}").jsonObject
                val emotesData = emotesJson["data"]?.jsonArray ?: JsonArray(emptyList())

                // Group emotes by tier
                val emotesByTier = mutableMapOf<String, MutableList<EmoteItem>>()
                // Track sort order: tier number for sub tiers, high numbers for bits/follower
                val tierSortOrder = mutableMapOf<String, Int>()
                emotesData.forEach { emote ->
                    val obj = emote.jsonObject
                    val id = obj["id"]?.jsonPrimitive?.content ?: return@forEach
                    val name = obj["name"]?.jsonPrimitive?.content ?: return@forEach
                    val tier = obj["tier"]?.jsonPrimitive?.content ?: "other"
                    val emoteType = obj["emote_type"]?.jsonPrimitive?.content ?: ""
                    val format = if (obj["format"]?.jsonArray?.any { it.jsonPrimitive.content == "animated" } == true) "animated" else "static"
                    val url = "https://static-cdn.jtvnw.net/emoticons/v2/$id/$format/dark/3.0"

                    val tierNum = when (tier) {
                        "1000" -> 1; "2000" -> 2; "3000" -> 3
                        else -> tier.toIntOrNull() ?: 0
                    }
                    val sectionKey = when (emoteType) {
                        "subscriptions" -> "Tier $tierNum"
                        "bitstier" -> "Bits Tier"
                        "follower" -> "Follower"
                        else -> "Other"
                    }
                    val sortVal = when (emoteType) {
                        "subscriptions" -> tierNum
                        "bitstier" -> 100
                        "follower" -> 200
                        else -> 300
                    }
                    tierSortOrder[sectionKey] = sortVal
                    emotesByTier.getOrPut(sectionKey) { mutableListOf() }.add(EmoteItem(id, name, url))
                }

                // Sort by tier number, then bits, then follower
                val sortedSections = emotesByTier.entries
                    .sortedBy { tierSortOrder[it.key] ?: 999 }
                    .map { EmoteSection("${it.key} (${it.value.size})", it.value) }

                // Badges
                val badgesRequest = okhttp3.Request.Builder()
                    .url("https://api.twitch.tv/helix/chat/badges?broadcaster_id=$broadcasterId")
                    .addHeader("Authorization", "Bearer $twitchToken")
                    .addHeader("Client-Id", clientId)
                    .build()

                val badgesResponse = client.newCall(badgesRequest).execute()
                val badgesJson = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                    .parseToJsonElement(badgesResponse.body?.string() ?: "{}").jsonObject
                val badgesData = badgesJson["data"]?.jsonArray ?: JsonArray(emptyList())

                val badgesBySet = mutableMapOf<String, MutableList<EmoteItem>>()
                badgesData.forEach { set ->
                    val setObj = set.jsonObject
                    val setId = setObj["set_id"]?.jsonPrimitive?.content ?: return@forEach
                    if (setId == "subscriber" || setId == "bits") {
                        val displayName = when (setId) {
                            "bits" -> "Cheer"
                            "subscriber" -> "Subscriber"
                            else -> setId
                        }
                        setObj["versions"]?.jsonArray?.forEach { version ->
                            val vObj = version.jsonObject
                            val vId = vObj["id"]?.jsonPrimitive?.content ?: return@forEach
                            val vTitle = vObj["title"]?.jsonPrimitive?.content ?: "$displayName $vId"
                            val url4x = vObj["image_url_4x"]?.jsonPrimitive?.content ?: return@forEach
                            badgesBySet.getOrPut(displayName) { mutableListOf() }
                                .add(EmoteItem("$setId-$vId", vTitle, url4x))
                        }
                    }
                }

                // Cheermotes
                val cheermotesRequest = okhttp3.Request.Builder()
                    .url("https://api.twitch.tv/helix/bits/cheermotes?broadcaster_id=$broadcasterId")
                    .addHeader("Authorization", "Bearer $twitchToken")
                    .addHeader("Client-Id", clientId)
                    .build()

                val cheermotesByPrefix = mutableMapOf<String, MutableList<EmoteItem>>()
                try {
                    val cheermotesResponse = client.newCall(cheermotesRequest).execute()
                    val cheermotesJson = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                        .parseToJsonElement(cheermotesResponse.body?.string() ?: "{}").jsonObject
                    val cheermotesData = cheermotesJson["data"]?.jsonArray ?: JsonArray(emptyList())

                    cheermotesData.forEach { cheermote ->
                        val obj = cheermote.jsonObject
                        val prefix = obj["prefix"]?.jsonPrimitive?.content ?: return@forEach
                        val cheermoteType = obj["type"]?.jsonPrimitive?.content ?: ""
                        // Only include channel custom and default "Cheer"
                        if (cheermoteType != "channel_custom" && prefix != "Cheer") return@forEach

                        obj["tiers"]?.jsonArray?.forEach { tier ->
                            val tierObj = tier.jsonObject
                            val tierId = tierObj["id"]?.jsonPrimitive?.content ?: return@forEach
                            val darkAnimated = tierObj["images"]?.jsonObject
                                ?.get("dark")?.jsonObject
                                ?.get("animated")?.jsonObject
                                ?.get("4")?.jsonPrimitive?.content
                            if (darkAnimated != null) {
                                cheermotesByPrefix.getOrPut(prefix) { mutableListOf() }
                                    .add(EmoteItem("cheer-$prefix-$tierId", "$prefix $tierId", darkAnimated))
                            }
                        }
                    }
                } catch (_: Exception) {
                    AppLogger.log("EmotesGallery: cheermotes fetch failed (non-fatal)")
                }

                val result = sortedSections.toMutableList()
                // Add Badges section header + sub-sections
                badgesBySet.entries.forEach { (name, items) ->
                    result.add(EmoteSection("badge:$name (${items.size})", items))
                }
                // Add Cheermotes section header + sub-sections
                cheermotesByPrefix.entries.forEach { (prefix, items) ->
                    result.add(EmoteSection("cheermote:$prefix (${items.size})", items))
                }
                result.toList()
                } // end withContext

                sections = allSections
                AppLogger.log("EmotesGallery: loaded ${sections.sumOf { it.emotes.size }} emotes across ${sections.size} sections")
            } catch (e: Exception) {
                errorMessage = "No Wind in the Sails"
                AppLogger.log("EmotesGallery: load failed: ${e.message}")
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadEmotes() }

    // Not logged in
    if (token == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(com.swiftyspiffy.burkeblackapp.R.drawable.ic_burke_emote),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Emotes, Bits, Badges & Cheermotes", fontFamily = PirateTheme.fontFamily, color = PirateTheme.accentColor)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Crew Members Only", fontFamily = PirateTheme.fontFamily, fontSize = 20.sp, color = PirateTheme.accentColor)
                    Text("Login to view channel emotes", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp)
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emotes, Bits, Badges & Cheermotes", fontFamily = PirateTheme.fontFamily, color = PirateTheme.accentColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PirateTheme.accentColor)
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(errorMessage!!, fontFamily = PirateTheme.fontFamily, fontSize = 18.sp, color = Color.White.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { loadEmotes() },
                            colors = ButtonDefaults.buttonColors(containerColor = PirateTheme.accentColor)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Try Again", color = Color.Black, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Group sections by type with headers
                    var lastGroup = ""
                    sections.forEach { section ->
                        val group = when {
                            section.title.startsWith("badge:") -> "Badges"
                            section.title.startsWith("cheermote:") -> "Cheermotes"
                            else -> "Emotes"
                        }
                        if (group != lastGroup) {
                            Text(
                                text = group,
                                fontFamily = PirateTheme.fontFamily,
                                fontSize = 24.sp,
                                color = PirateTheme.accentColor,
                                modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp)
                            )
                            lastGroup = group
                        }
                        val displayTitle = section.title
                            .removePrefix("badge:")
                            .removePrefix("cheermote:")
                        Text(
                            text = displayTitle,
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 16.sp,
                            color = PirateTheme.accentColor.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(section.emotes) { emote ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(80.dp)
                                        .clickable {
                                            fullscreenEmotes = section.emotes
                                            fullscreenIndex = section.emotes.indexOf(emote)
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(74.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.05f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = emote.url,
                                            contentDescription = emote.name,
                                            modifier = Modifier.size(56.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        emote.name,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Fullscreen viewer
    fullscreenEmotes?.let { emotes ->
        Dialog(
            onDismissRequest = { fullscreenEmotes = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { fullscreenEmotes = null },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val listState = rememberLazyListState(initialFirstVisibleItemIndex = fullscreenIndex)
                    LazyRow(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(40.dp)
                    ) {
                        items(emotes) { emote ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                AsyncImage(
                                    model = emote.url,
                                    contentDescription = emote.name,
                                    modifier = Modifier.size(200.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    emote.name,
                                    fontFamily = PirateTheme.fontFamily,
                                    color = PirateTheme.accentColor,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = { fullscreenEmotes = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}
