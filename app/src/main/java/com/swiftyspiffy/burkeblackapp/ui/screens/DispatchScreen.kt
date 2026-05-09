package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.ui.components.DateUtils
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

enum class DispatchMode { CAPTAIN, CREW }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DispatchScreen(
    token: String,
    mode: DispatchMode,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val screenTitle = if (mode == DispatchMode.CAPTAIN) "Captain's Dispatch" else "Crew Dispatch"
    val types = if (mode == DispatchMode.CAPTAIN) listOf("Burke Announcement", "Special Event")
    else listOf("Mod Announcement", "Special Event")
    val titlePresets = if (mode == DispatchMode.CAPTAIN) listOf(
        "Word from Captain Burke",
        "Dispatch from the Captain",
        "Message from BurkeBlack",
        "Announcement from The Dirty Skull"
    ) else listOf(
        "Word from the Officers",
        "Moderator Dispatch",
        "Crew Announcement",
        "Message from the Mod Squad"
    )

    var selectedTypeIndex by remember { mutableIntStateOf(0) }
    var selectedTitle by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Destination picker
    val destinations = listOf("Dirty Skull", "Twitch", "Website")
    var selectedDestIndex by remember { mutableIntStateOf(0) }
    var twitchChannel by remember { mutableStateOf("BurkeBlack") }
    var websiteUrl by remember { mutableStateOf("") }
    var isOtherChannel by remember { mutableStateOf(false) }

    // Twitch channel verification
    var verifyingChannel by remember { mutableStateOf(false) }
    var verifiedChannel by remember { mutableStateOf<JsonObject?>(null) }
    var verifiedFollowerCount by remember { mutableStateOf<Int?>(null) }
    var showChannelConfirm by remember { mutableStateOf(false) }

    // History
    var history by remember { mutableStateOf<JsonArray?>(null) }
    var historyLoading by remember { mutableStateOf(false) }

    fun loadHistory() {
        historyLoading = true
        scope.launch {
            try {
                val response = if (mode == DispatchMode.CAPTAIN) {
                    ApiClient.api.getCaptainNotificationHistory("Bearer $token")
                } else {
                    ApiClient.api.getNotificationHistory("Bearer $token")
                }
                if (response.success && response.data != null) {
                    history = response.data["history"]?.jsonArray
                }
            } catch (e: Exception) {
                AppLogger.log("Dispatch: history load failed: ${e.message}")
            }
            historyLoading = false
        }
    }

    LaunchedEffect(Unit) {
        AppLogger.log("$screenTitle: appeared")
        loadHistory()
    }

    fun buildNotifUrl(): String? {
        return when (selectedDestIndex) {
            0 -> null // Dirty Skull - opens app
            1 -> {
                val channel = if (isOtherChannel) twitchChannel else "burkeblack"
                "https://twitch.tv/$channel"
            }
            2 -> websiteUrl.ifBlank { null }
            else -> null
        }
    }

    fun sendNotification() {
        isSending = true
        scope.launch {
            try {
                val type = if (selectedTypeIndex == 0) {
                    if (mode == DispatchMode.CAPTAIN) "burke_announcement" else "mod_announcement"
                } else "special_event"

                val body = buildJsonObject {
                    put("type", type)
                    put("title", selectedTitle)
                    put("message", message)
                    buildNotifUrl()?.let { put("url", it) }
                }

                val response = if (mode == DispatchMode.CAPTAIN || selectedTypeIndex == 1) {
                    ApiClient.api.sendCaptainNotification("Bearer $token", body)
                } else {
                    ApiClient.api.sendModNotification("Bearer $token", body)
                }

                if (response.success) {
                    AppLogger.log("$screenTitle: notification sent successfully")
                    snackbarHostState.showSnackbar("Dispatch sent!")
                    message = ""
                    selectedTitle = ""
                    loadHistory()
                } else {
                    val error = response.error ?: "Send failed"
                    AppLogger.log("$screenTitle: send failed: $error")
                    snackbarHostState.showSnackbar(error)
                }
            } catch (e: Exception) {
                AppLogger.log("$screenTitle: send error: ${e.message}")
                snackbarHostState.showSnackbar(e.message ?: "Send failed")
            }
            isSending = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle, fontFamily = PirateTheme.fontFamily, color = PirateTheme.accentColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Type picker
            Text("Dispatch Type", color = PirateTheme.accentColor, fontFamily = PirateTheme.fontFamily, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                types.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = selectedTypeIndex == index,
                        onClick = { selectedTypeIndex = index },
                        shape = SegmentedButtonDefaults.itemShape(index, types.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = PirateTheme.accentColor.copy(alpha = 0.2f),
                            activeContentColor = PirateTheme.accentColor,
                            inactiveContainerColor = MaterialTheme.colorScheme.surface,
                            inactiveContentColor = Color.White.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(label, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title presets
            Text("Title", color = PirateTheme.accentColor, fontFamily = PirateTheme.fontFamily, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                titlePresets.forEach { preset ->
                    val isSelected = selectedTitle == preset
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) PirateTheme.accentColor else MaterialTheme.colorScheme.surface)
                            .clickable { selectedTitle = preset }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            preset,
                            color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Message
            Text("Message", color = PirateTheme.accentColor, fontFamily = PirateTheme.fontFamily, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PirateTheme.accentColor,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = PirateTheme.accentColor,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notification opens...
            Text("Notification Opens...", color = PirateTheme.accentColor, fontFamily = PirateTheme.fontFamily, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                destinations.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = selectedDestIndex == index,
                        onClick = { selectedDestIndex = index },
                        shape = SegmentedButtonDefaults.itemShape(index, destinations.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = PirateTheme.accentColor.copy(alpha = 0.2f),
                            activeContentColor = PirateTheme.accentColor,
                            inactiveContainerColor = MaterialTheme.colorScheme.surface,
                            inactiveContentColor = Color.White.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(label, fontSize = 13.sp)
                    }
                }
            }

            // Twitch channel picker
            if (selectedDestIndex == 1) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Other channel:", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Checkbox(
                        checked = isOtherChannel,
                        onCheckedChange = { isOtherChannel = it },
                        colors = androidx.compose.material3.CheckboxDefaults.colors(
                            checkedColor = PirateTheme.accentColor,
                            checkmarkColor = Color.Black
                        )
                    )
                }
                if (isOtherChannel) {
                    OutlinedTextField(
                        value = twitchChannel,
                        onValueChange = {
                            twitchChannel = it
                            verifiedChannel = null
                            verifiedFollowerCount = null
                        },
                        placeholder = { Text("Channel name", color = Color.White.copy(alpha = 0.3f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PirateTheme.accentColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = PirateTheme.accentColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Website URL
            if (selectedDestIndex == 2) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = websiteUrl,
                    onValueChange = { websiteUrl = it },
                    placeholder = { Text("https://...", color = Color.White.copy(alpha = 0.3f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PirateTheme.accentColor,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = PirateTheme.accentColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    isError = websiteUrl.isNotBlank() && !websiteUrl.startsWith("http://") && !websiteUrl.startsWith("https://")
                )
                if (websiteUrl.isNotBlank() && !websiteUrl.startsWith("http://") && !websiteUrl.startsWith("https://")) {
                    Text("URL must start with http:// or https://", color = Color(0xFFEF5350), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Send button
            val canSend = selectedTitle.isNotBlank() && message.isNotBlank() && !isSending &&
                (selectedDestIndex != 2 || websiteUrl.isBlank() || websiteUrl.startsWith("http://") || websiteUrl.startsWith("https://"))

            Button(
                onClick = {
                    // If other Twitch channel, verify first
                    if (selectedDestIndex == 1 && isOtherChannel && twitchChannel.isNotBlank() && verifiedChannel == null) {
                        verifyingChannel = true
                        scope.launch {
                            try {
                                val tokenResp = ApiClient.api.fetchTwitchToken("Bearer $token")
                                if (tokenResp.success && tokenResp.data != null) {
                                    val twitchAccessToken = tokenResp.data.accessToken
                                    val clientId = tokenResp.data.clientId
                                    // Look up the channel via Twitch API
                                    val users = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                        val client = okhttp3.OkHttpClient()
                                        val userRequest = okhttp3.Request.Builder()
                                            .url("https://api.twitch.tv/helix/users?login=${twitchChannel.trim()}")
                                            .addHeader("Authorization", "Bearer $twitchAccessToken")
                                            .addHeader("Client-Id", clientId)
                                            .build()
                                        val userResp = client.newCall(userRequest).execute()
                                        val userJson = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                                            .parseToJsonElement(userResp.body?.string() ?: "{}").jsonObject
                                        userJson["data"]?.jsonArray
                                    }
                                    if (!users.isNullOrEmpty()) {
                                        val userObj = users[0].jsonObject
                                        verifiedChannel = userObj
                                        // Fetch follower count
                                        val userId = userObj["id"]?.jsonPrimitive?.content
                                        if (userId != null) {
                                            try {
                                                val followerCount = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                    val client = okhttp3.OkHttpClient()
                                                    val followerRequest = okhttp3.Request.Builder()
                                                        .url("https://api.twitch.tv/helix/channels/followers?broadcaster_id=$userId&first=1")
                                                        .addHeader("Authorization", "Bearer $twitchAccessToken")
                                                        .addHeader("Client-Id", clientId)
                                                        .build()
                                                    val followerResp = client.newCall(followerRequest).execute()
                                                    val followerJson = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                                                        .parseToJsonElement(followerResp.body?.string() ?: "{}").jsonObject
                                                    followerJson["total"]?.jsonPrimitive?.int
                                                }
                                                verifiedFollowerCount = followerCount
                                            } catch (_: Exception) {
                                                verifiedFollowerCount = null
                                            }
                                        }
                                        showChannelConfirm = true
                                    } else {
                                        snackbarHostState.showSnackbar("Channel not found: ${twitchChannel.trim()}")
                                    }
                                } else {
                                    snackbarHostState.showSnackbar("Could not get Twitch credentials")
                                }
                            } catch (e: Exception) {
                                AppLogger.log("Dispatch: channel verify failed: ${e.message}")
                                snackbarHostState.showSnackbar("Could not verify channel")
                            }
                            verifyingChannel = false
                        }
                    } else {
                        showConfirmDialog = true
                    }
                },
                enabled = canSend,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PirateTheme.accentColor,
                    disabledContainerColor = PirateTheme.accentColor.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Dispatch", color = Color.Black, fontWeight = FontWeight.SemiBold, fontFamily = PirateTheme.fontFamily, fontSize = 16.sp)
                }
            }

            // History
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = PirateTheme.accentColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Recent Dispatches", fontFamily = PirateTheme.fontFamily, fontSize = 18.sp, color = PirateTheme.accentColor)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (historyLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PirateTheme.accentColor, modifier = Modifier.size(24.dp))
                }
            } else if (history.isNullOrEmpty()) {
                Text("No dispatches sent yet", color = Color.White.copy(alpha = 0.3f), fontSize = 14.sp, modifier = Modifier.padding(16.dp))
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        history?.forEachIndexed { index, item ->
                            val obj = item.jsonObject
                            val title = obj["title"]?.jsonPrimitive?.content ?: ""
                            val msg = obj["message"]?.jsonPrimitive?.content ?: ""
                            val type = obj["type"]?.jsonPrimitive?.content ?: ""
                            val sender = obj["sender_username"]?.jsonPrimitive?.content ?: ""
                            val iosCount = obj["ios_count"]?.jsonPrimitive?.int ?: 0
                            val androidCount = obj["android_count"]?.jsonPrimitive?.int ?: 0
                            val createdAt = obj["created_at"]?.jsonPrimitive?.content ?: ""

                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                    Text(
                                        type.replace("_", " "),
                                        color = PirateTheme.accentColor.copy(alpha = 0.6f),
                                        fontSize = 10.sp
                                    )
                                }
                                if (msg.isNotBlank()) {
                                    Text(msg, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, maxLines = 2)
                                }
                                Row {
                                    Text("$sender", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
                                    Text(" \u2022 iOS: $iosCount, Android: $androidCount", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
                                    if (createdAt.isNotBlank()) {
                                        Text(" \u2022 ${DateUtils.formatRelativeTime(createdAt)}", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
                                    }
                                }
                            }
                            if (index < (history?.size ?: 0) - 1) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Twitch channel verification dialog
    if (showChannelConfirm && verifiedChannel != null) {
        val channelObj = verifiedChannel!!
        val displayName = channelObj["display_name"]?.jsonPrimitive?.content ?: ""
        val profileImg = channelObj["profile_image_url"]?.jsonPrimitive?.content
        val createdAt = channelObj["created_at"]?.jsonPrimitive?.content ?: ""

        AlertDialog(
            onDismissRequest = {
                showChannelConfirm = false
                verifiedChannel = null
                verifiedFollowerCount = null
            },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Is this the right ship?", color = PirateTheme.accentColor, fontFamily = PirateTheme.fontFamily) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    if (profileImg != null) {
                        AsyncImage(
                            model = profileImg,
                            contentDescription = displayName,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Text(displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (createdAt.isNotBlank()) {
                        Text("Created: ${createdAt.take(10)}", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                    }
                    verifiedFollowerCount?.let { count ->
                        val formatted = when {
                            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
                            count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
                            else -> "$count"
                        }
                        Text("Followers: $formatted", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showChannelConfirm = false
                    showConfirmDialog = true
                }) {
                    Text("Aye, That Be the One!", color = PirateTheme.accentColor, fontFamily = PirateTheme.fontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showChannelConfirm = false
                    verifiedChannel = null
                    verifiedFollowerCount = null
                }) {
                    Text("Nay, Wrong Ship", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }

    // Confirmation dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Confirm Dispatch", color = PirateTheme.accentColor, fontFamily = PirateTheme.fontFamily) },
            text = {
                Column {
                    Text("Type: ${types[selectedTypeIndex]}", color = Color.White, fontSize = 14.sp)
                    Text("Title: $selectedTitle", color = Color.White, fontSize = 14.sp)
                    Text("Message: $message", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                    buildNotifUrl()?.let {
                        Text("Opens: $it", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("This will send to all subscribed users.", color = Color(0xFFFF9800), fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    sendNotification()
                }) {
                    Text("Aye, Send It!", color = PirateTheme.accentColor, fontFamily = PirateTheme.fontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }
}
