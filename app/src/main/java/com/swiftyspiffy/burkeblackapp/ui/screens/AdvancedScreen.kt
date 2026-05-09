package com.swiftyspiffy.burkeblackapp.ui.screens

import android.os.Build
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftyspiffy.burkeblackapp.BurkeBlackApplication
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.websocket.GiveawayWebSocketManager
import com.swiftyspiffy.burkeblackapp.push.PushNotificationManager
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedScreen(
    token: String?,
    username: String = "",
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val appSettings = (context.applicationContext as BurkeBlackApplication).appSettings
    val wsManager = GiveawayWebSocketManager.instance
    val wsConnected by wsManager.isConnected.collectAsState()
    val activeGiveaway by wsManager.activeGiveaway.collectAsState()
    val hasToken = !token.isNullOrEmpty()

    var authCheckLoading by remember { mutableStateOf(false) }
    var authCheckResult by remember { mutableStateOf<String?>(null) }
    var authCheckSuccess by remember { mutableStateOf<Boolean?>(null) }

    // Notification debug info
    val notifPermission = PushNotificationManager.hasNotificationPermission(context)
    var fcmToken by remember { mutableStateOf<String?>(null) }
    var fcmRegistered by remember { mutableStateOf<Boolean?>(null) }
    val pushHasAsked by appSettings.pushHasAskedPermissionFlow.collectAsState(initial = false)

    // Settings flows for notification channels view
    val presentationMode by appSettings.presentationModeFlow.collectAsState(initial = false)
    val showCaptainsDispatch by appSettings.showCaptainsDispatchFlow.collectAsState(initial = false)

    // Notification settings for channel list
    val burkeStreamEnabled by appSettings.burkeStreamEnabledFlow.collectAsState(initial = true)
    val burke40kEnabled by appSettings.burke40kEnabledFlow.collectAsState(initial = true)
    val burkeAnnouncements by appSettings.burkeAnnouncementsEnabledFlow.collectAsState(initial = true)
    val modAnnouncements by appSettings.modAnnouncementsEnabledFlow.collectAsState(initial = true)
    val specialEvents by appSettings.specialEventsEnabledFlow.collectAsState(initial = true)
    val tidings by appSettings.tidingsEnabledFlow.collectAsState(initial = true)
    val youtubeVideos by appSettings.youtubeVideosEnabledFlow.collectAsState(initial = true)
    val youtubeShorts by appSettings.youtubeShortsEnabledFlow.collectAsState(initial = true)
    val burkeStreamDays by appSettings.burkeStreamDaysFlow.collectAsState(initial = "0,1,2,3,4,5,6")
    val burkeStreamAllDay by appSettings.burkeStreamAllDayFlow.collectAsState(initial = true)
    val burkeStreamFromHour by appSettings.burkeStreamFromHourFlow.collectAsState(initial = 9)
    val burkeStreamToHour by appSettings.burkeStreamToHourFlow.collectAsState(initial = 23)
    val burke40kAllDay by appSettings.burke40kAllDayFlow.collectAsState(initial = true)

    var showNotifChannels by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        AppLogger.log("Advanced: appeared")
        fcmToken = PushNotificationManager.getFcmToken()
        fcmRegistered = fcmToken != null && hasToken
    }

    // Notification Channels sub-screen
    if (showNotifChannels) {
        NotificationChannelsScreen(
            burkeStreamEnabled = burkeStreamEnabled,
            burke40kEnabled = burke40kEnabled,
            burkeAnnouncements = burkeAnnouncements,
            modAnnouncements = modAnnouncements,
            specialEvents = specialEvents,
            tidings = tidings,
            youtubeVideos = youtubeVideos,
            youtubeShorts = youtubeShorts,
            burkeStreamSchedule = formatScheduleBrief(burkeStreamAllDay, burkeStreamDays, burkeStreamFromHour, burkeStreamToHour),
            burke40kSchedule = formatScheduleBrief(burke40kAllDay, "0,1,2,3,4,5,6", 9, 23),
            onBack = { showNotifChannels = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced", color = Color.White) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
                // Authentication section
                SectionHeader("Authentication")
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        InfoRow("Status", if (hasToken) "Authenticated" else "Not authenticated",
                            valueColor = if (hasToken) Color(0xFF4CAF50) else Color(0xFFEF5350))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        InfoRow("Username", username.ifBlank { "-" })
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        InfoRow("Token", if (hasToken) "${token!!.take(12)}..." else "-")
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        // Check Authentication button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !authCheckLoading) {
                                    if (hasToken) {
                                        scope.launch {
                                            authCheckLoading = true
                                            authCheckResult = null
                                            authCheckSuccess = null
                                            try {
                                                val response = ApiClient.api.fetchUserStatus("Bearer $token")
                                                if (response.success && response.data != null) {
                                                    authCheckSuccess = true
                                                    authCheckResult = "Role: ${response.data.userRole} | Follows: ${response.data.follows} | Sub: ${response.data.subscribed}"
                                                } else {
                                                    authCheckSuccess = false
                                                    authCheckResult = response.error ?: "Unknown error"
                                                }
                                            } catch (e: Exception) {
                                                authCheckSuccess = false
                                                authCheckResult = e.message ?: "Connection failed"
                                            }
                                            authCheckLoading = false
                                        }
                                    } else {
                                        authCheckSuccess = false
                                        authCheckResult = "No token available"
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (authCheckLoading) {
                                CircularProgressIndicator(color = PirateTheme.accentColor, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PirateTheme.accentColor, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Check Authentication", color = PirateTheme.accentColor, fontSize = 14.sp)
                        }
                    }
                }

                if (authCheckResult != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        authCheckResult!!,
                        fontSize = 12.sp,
                        color = if (authCheckSuccess == true) Color(0xFF4CAF50) else Color(0xFFEF5350),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (authCheckSuccess == true) Color(0xFF4CAF50).copy(alpha = 0.1f)
                                else Color(0xFFEF5350).copy(alpha = 0.1f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(8.dp)
                    )
                }

                // Notifications section
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Notifications")
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        InfoRow("Enabled", if (notifPermission) "Yes" else "No",
                            valueColor = if (notifPermission) Color(0xFF4CAF50) else Color(0xFFEF5350))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        InfoRow("System Status", if (notifPermission) "Authorized" else "Denied")
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        InfoRow("Registered with Backend", if (fcmRegistered == true) "Yes" else "No",
                            valueColor = if (fcmRegistered == true) Color(0xFF4CAF50) else Color(0xFFEF5350))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        InfoRow("Device Token", fcmToken?.let { "${it.take(18)}..." } ?: "-")
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        InfoRow("Has Been Asked", if (pushHasAsked) "Yes" else "No")
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        // Notification Channels nav row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showNotifChannels = true }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("\uD83D\uDD14", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Notification Channels", color = Color.White, fontSize = 14.sp)
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // WebSocket section
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("WebSocket")
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Connection", color = Color.White, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (wsConnected) Color(0xFF4CAF50) else Color(0xFFEF5350))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (wsConnected) "Connected" else "Disconnected",
                                    color = if (wsConnected) Color(0xFF4CAF50) else Color(0xFFEF5350),
                                    fontSize = 14.sp
                                )
                            }
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        InfoRow("Active Giveaway", if (activeGiveaway != null) "Yes" else "No")
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                        // Disconnect/Reconnect button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (wsConnected) wsManager.disconnect()
                                    else wsManager.reconnectIfNeeded()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (wsConnected) "Disconnect" else "Reconnect",
                                color = if (wsConnected) Color(0xFFEF5350) else PirateTheme.accentColor,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // WebSocket Messages
                val messages by wsManager.messageLog.collectAsState()
                val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }

                if (messages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            messages.forEach { msg ->
                                val time = timeFormat.format(Date(msg.timestamp))
                                Text(
                                    text = "[$time] ${msg.text}",
                                    fontSize = 11.sp,
                                    color = when {
                                        msg.text.startsWith("[CONNECTED]") -> Color(0xFF4CAF50)
                                        msg.text.startsWith("[ERROR]") -> Color(0xFFEF5350)
                                        msg.text.startsWith("[CLOSED]") -> Color(0xFFFF9800)
                                        else -> Color.White.copy(alpha = 0.6f)
                                    },
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                            }
                        }
                    }
                }

                // UI section
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("UI")
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Presentation Mode", color = Color.White, fontSize = 14.sp)
                                Text("Hides Dispatch, Mod Panel for screenshots", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
                            }
                            Switch(
                                checked = presentationMode,
                                onCheckedChange = { scope.launch { appSettings.setPresentationMode(it) } },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PirateTheme.accentColor)
                            )
                        }
                    }
                }

                // UI Debug section
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("UI Debug")
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Captain's Dispatch", color = Color.White, fontSize = 14.sp)
                            Switch(
                                checked = showCaptainsDispatch,
                                onCheckedChange = { scope.launch { appSettings.setShowCaptainsDispatch(it) } },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PirateTheme.accentColor)
                            )
                        }
                    }
                }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = PirateTheme.accentColor,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color = Color.White.copy(alpha = 0.6f)) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 14.sp)
        Text(value, color = valueColor, fontSize = 14.sp)
    }
}

// --- Notification Channels Sub-screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationChannelsScreen(
    burkeStreamEnabled: Boolean,
    burke40kEnabled: Boolean,
    burkeAnnouncements: Boolean,
    modAnnouncements: Boolean,
    specialEvents: Boolean,
    tidings: Boolean,
    youtubeVideos: Boolean,
    youtubeShorts: Boolean,
    burkeStreamSchedule: String,
    burke40kSchedule: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Channels", color = Color.White) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Local Settings
            SectionHeader("Local Settings")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    ChannelRow("BurkeBlack Stream", burkeStreamEnabled, burkeStreamSchedule)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    ChannelRow("Burke40k Stream", burke40kEnabled, burke40kSchedule)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    ChannelRow("Burke Announcements", burkeAnnouncements)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    ChannelRow("Mod Announcements", modAnnouncements)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    ChannelRow("Special Events", specialEvents)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    ChannelRow("Channel Tidings", tidings)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    ChannelRow("YouTube Videos", youtubeVideos)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    ChannelRow("YouTube Shorts", youtubeShorts)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    ChannelRow("Twitter Posts", false, "Disabled (not wired)", isDisabledType = true)
                }
            }

            // Info
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader("Info")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    InfoRow("Device Timezone", TimeZone.getDefault().id)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ChannelRow(name: String, enabled: Boolean, subtitle: String? = null, isDisabledType: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (enabled) Color(0xFF4CAF50) else Color(0xFFEF5350))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, color = if (isDisabledType) Color(0xFFFF9800) else Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
            }
        }
        Text(
            if (enabled) "Enabled" else "Disabled",
            color = if (enabled) Color(0xFF4CAF50) else Color(0xFFEF5350),
            fontSize = 13.sp
        )
    }
}

private fun formatScheduleBrief(allDay: Boolean, days: String, fromHour: Int, toHour: Int): String {
    if (allDay && days == "0,1,2,3,4,5,6") return "All day, every day"
    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val selectedDays = days.split(",").mapNotNull { it.trim().toIntOrNull() }
    val dayStr = selectedDays.joinToString(", ") { dayNames.getOrElse(it) { "" } }
    fun fmt(h: Int): String {
        val amPm = if (h < 12) "AM" else "PM"
        val h12 = if (h == 0) 12 else if (h > 12) h - 12 else h
        return "$h12 $amPm"
    }
    return if (allDay) "$dayStr, all hours" else "$dayStr, ${fmt(fromHour)}-${fmt(toHour)}"
}
