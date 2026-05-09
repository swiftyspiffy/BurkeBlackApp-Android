package com.swiftyspiffy.burkeblackapp.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftyspiffy.burkeblackapp.R
import com.swiftyspiffy.burkeblackapp.BurkeBlackApplication
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.push.PushNotificationManager
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    token: String?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appSettings = (context.applicationContext as BurkeBlackApplication).appSettings

    var notificationsAuthorized by remember { mutableStateOf(PushNotificationManager.hasNotificationPermission(context)) }

    // Re-check permission when returning from system settings
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
        notificationsAuthorized = PushNotificationManager.hasNotificationPermission(context)
        onPauseOrDispose { }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsAuthorized = granted
        AppLogger.log("NotificationSettings: permission ${if (granted) "granted" else "denied"}")
    }

    // Collect all settings
    val burkeStreamEnabled by appSettings.burkeStreamEnabledFlow.collectAsState(initial = true)
    val burkeStreamAllDay by appSettings.burkeStreamAllDayFlow.collectAsState(initial = true)
    val burkeStreamDays by appSettings.burkeStreamDaysFlow.collectAsState(initial = "0,1,2,3,4,5,6")
    val burkeStreamFromHour by appSettings.burkeStreamFromHourFlow.collectAsState(initial = 9)
    val burkeStreamFromMinute by appSettings.burkeStreamFromMinuteFlow.collectAsState(initial = 0)
    val burkeStreamToHour by appSettings.burkeStreamToHourFlow.collectAsState(initial = 23)
    val burkeStreamToMinute by appSettings.burkeStreamToMinuteFlow.collectAsState(initial = 0)

    val burke40kEnabled by appSettings.burke40kEnabledFlow.collectAsState(initial = true)
    val burke40kAllDay by appSettings.burke40kAllDayFlow.collectAsState(initial = true)
    val burke40kDays by appSettings.burke40kDaysFlow.collectAsState(initial = "0,1,2,3,4,5,6")
    val burke40kFromHour by appSettings.burke40kFromHourFlow.collectAsState(initial = 9)
    val burke40kFromMinute by appSettings.burke40kFromMinuteFlow.collectAsState(initial = 0)
    val burke40kToHour by appSettings.burke40kToHourFlow.collectAsState(initial = 23)
    val burke40kToMinute by appSettings.burke40kToMinuteFlow.collectAsState(initial = 0)

    val burkeAnnouncements by appSettings.burkeAnnouncementsEnabledFlow.collectAsState(initial = true)
    val modAnnouncements by appSettings.modAnnouncementsEnabledFlow.collectAsState(initial = true)
    val specialEvents by appSettings.specialEventsEnabledFlow.collectAsState(initial = true)
    val tidings by appSettings.tidingsEnabledFlow.collectAsState(initial = true)
    val youtubeVideos by appSettings.youtubeVideosEnabledFlow.collectAsState(initial = true)
    val youtubeShorts by appSettings.youtubeShortsEnabledFlow.collectAsState(initial = true)
    val tiktokVideos by appSettings.tiktokVideosEnabledFlow.collectAsState(initial = true)
    val twitterPosts by appSettings.twitterPostsEnabledFlow.collectAsState(initial = true)

    var burkeStreamExpanded by remember { mutableStateOf(false) }
    var burke40kExpanded by remember { mutableStateOf(false) }

    val enabled = notificationsAuthorized
    val alpha = if (enabled) 1f else 0.4f

    LaunchedEffect(Unit) {
        AppLogger.log("NotificationSettings: appeared")
    }

    // Sync preferences to backend after any change
    suspend fun syncPrefsToBackend() {
        if (token == null) return
        try {
            val body = appSettings.buildPreferencesJson()
            ApiClient.api.updateNotificationPreferences("Bearer $token", body)
            AppLogger.log("NotificationSettings: preferences synced to backend")
        } catch (e: Exception) {
            AppLogger.log("NotificationSettings: sync failed: ${e.message}")
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
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
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = PirateTheme.accentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Notifications",
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 22.sp,
                        color = PirateTheme.accentColor
                    )
                    Text(
                        text = "Choose which signal flags fly from yer mast",
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {

            // Permission status banner
            if (!notificationsAuthorized) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3A1A1A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                                    "Notifications Be Disabled!"
                                else
                                    "Notifications Be Disabled!",
                                color = Color(0xFFEF5350),
                                fontFamily = PirateTheme.fontFamily,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PirateTheme.accentColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enable Notifications", color = Color.Black, fontFamily = PirateTheme.fontFamily, fontSize = 16.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Stream Lookouts
            SectionLabel("Stream Lookouts")

            // BurkeBlack Stream
            NotifCardToggle(
                icon = Icons.Default.Circle,
                iconTint = Color.White,
                iconBgColor = Color(0xFFFF0000),
                title = "BurkeBlack Stream Start",
                subtitle = "More reliable than Twitch's crow's nest",
                scheduleSummary = formatScheduleSummary(burkeStreamAllDay, burkeStreamDays, burkeStreamFromHour, burkeStreamFromMinute, burkeStreamToHour, burkeStreamToMinute),
                checked = burkeStreamEnabled,
                enabled = enabled,
                onToggle = { scope.launch { appSettings.setBurkeStreamEnabled(it); syncPrefsToBackend() } },
                expanded = burkeStreamExpanded,
                onExpandToggle = { burkeStreamExpanded = !burkeStreamExpanded },
                modifier = Modifier.alpha(alpha)
            )

            AnimatedVisibility(burkeStreamExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                ScheduleConfig(
                    allDay = burkeStreamAllDay,
                    days = burkeStreamDays,
                    fromHour = burkeStreamFromHour,
                    fromMinute = burkeStreamFromMinute,
                    toHour = burkeStreamToHour,
                    toMinute = burkeStreamToMinute,
                    enabled = enabled && burkeStreamEnabled,
                    onAllDayChange = { scope.launch { appSettings.setBurkeStreamAllDay(it); syncPrefsToBackend() } },
                    onDaysChange = { scope.launch { appSettings.setBurkeStreamDays(it); syncPrefsToBackend() } },
                    onFromHourChange = { scope.launch { appSettings.setBurkeStreamFromHour(it); syncPrefsToBackend() } },
                    onFromMinuteChange = { scope.launch { appSettings.setBurkeStreamFromMinute(it) } },
                    onToHourChange = { scope.launch { appSettings.setBurkeStreamToHour(it); syncPrefsToBackend() } },
                    onToMinuteChange = { scope.launch { appSettings.setBurkeStreamToMinute(it) } }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Burke40k Stream
            NotifCardToggle(
                icon = Icons.Default.TableRestaurant,
                iconTint = Color.White,
                iconBgColor = Color(0xFF5D4037),
                title = "Burke40k Stream Start",
                subtitle = "The Captain's war table goes live",
                scheduleSummary = formatScheduleSummary(burke40kAllDay, burke40kDays, burke40kFromHour, burke40kFromMinute, burke40kToHour, burke40kToMinute),
                checked = burke40kEnabled,
                enabled = enabled,
                onToggle = { scope.launch { appSettings.setBurke40kEnabled(it); syncPrefsToBackend() } },
                expanded = burke40kExpanded,
                onExpandToggle = { burke40kExpanded = !burke40kExpanded },
                modifier = Modifier.alpha(alpha)
            )

            AnimatedVisibility(burke40kExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                ScheduleConfig(
                    allDay = burke40kAllDay,
                    days = burke40kDays,
                    fromHour = burke40kFromHour,
                    fromMinute = burke40kFromMinute,
                    toHour = burke40kToHour,
                    toMinute = burke40kToMinute,
                    enabled = enabled && burke40kEnabled,
                    onAllDayChange = { scope.launch { appSettings.setBurke40kAllDay(it); syncPrefsToBackend() } },
                    onDaysChange = { scope.launch { appSettings.setBurke40kDays(it); syncPrefsToBackend() } },
                    onFromHourChange = { scope.launch { appSettings.setBurke40kFromHour(it); syncPrefsToBackend() } },
                    onFromMinuteChange = { scope.launch { appSettings.setBurke40kFromMinute(it) } },
                    onToHourChange = { scope.launch { appSettings.setBurke40kToHour(it); syncPrefsToBackend() } },
                    onToMinuteChange = { scope.launch { appSettings.setBurke40kToMinute(it) } }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dispatches
            SectionLabel("Dispatches")

            NotifCardToggle(
                iconRes = R.drawable.ic_burke_captain,
                iconBgColor = PirateTheme.accentColor,
                title = "Burke Announcements",
                subtitle = "Word from the Captain himself",
                checked = burkeAnnouncements,
                enabled = enabled,
                onToggle = { scope.launch { appSettings.setBurkeAnnouncementsEnabled(it); syncPrefsToBackend() } },
                modifier = Modifier.alpha(alpha)
            )
            Spacer(modifier = Modifier.height(12.dp))
            NotifCardToggle(
                iconRes = R.drawable.ic_mod_badge,
                iconBgColor = Color(0xFF4CAF50),
                title = "Moderator Announcements",
                subtitle = "Orders from the Captain's officers",
                checked = modAnnouncements,
                enabled = enabled,
                onToggle = { scope.launch { appSettings.setModAnnouncementsEnabled(it); syncPrefsToBackend() } },
                modifier = Modifier.alpha(alpha)
            )
            Spacer(modifier = Modifier.height(12.dp))
            NotifCardToggle(
                icon = Icons.Default.Star,
                iconTint = Color.White,
                iconBgColor = Color(0xFFFF9800),
                title = "Special Events",
                subtitle = "One-off voyages, Burkies, TwitchCon & more",
                checked = specialEvents,
                enabled = enabled,
                onToggle = { scope.launch { appSettings.setSpecialEventsEnabled(it); syncPrefsToBackend() } },
                modifier = Modifier.alpha(alpha)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tidings
            SectionLabel("Tidings")

            NotifCardToggle(
                icon = Icons.Default.Newspaper,
                iconTint = Color.White,
                iconBgColor = PirateTheme.accentColor,
                title = "Channel Tidings",
                subtitle = "News and updates from the ship's log",
                checked = tidings,
                enabled = enabled,
                onToggle = { scope.launch { appSettings.setTidingsEnabled(it); syncPrefsToBackend() } },
                modifier = Modifier.alpha(alpha)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Shore Leave Posts
            SectionLabel("Shore Leave Posts")

            NotifCardToggle(
                icon = Icons.Default.OndemandVideo,
                iconTint = Color.White,
                iconBgColor = Color(0xFFFF0000),
                title = "YouTube Videos",
                subtitle = "New videos from the Captain's channel",
                checked = youtubeVideos,
                enabled = enabled,
                onToggle = { scope.launch { appSettings.setYoutubeVideosEnabled(it); syncPrefsToBackend() } },
                modifier = Modifier.alpha(alpha)
            )
            Spacer(modifier = Modifier.height(12.dp))
            NotifCardToggle(
                icon = Icons.Default.VideoLibrary,
                iconTint = Color.White,
                iconBgColor = Color(0xFFFF0000),
                title = "YouTube Shorts",
                subtitle = "Quick clips from the Captain",
                checked = youtubeShorts,
                enabled = enabled,
                onToggle = { scope.launch { appSettings.setYoutubeShortsEnabled(it); syncPrefsToBackend() } },
                modifier = Modifier.alpha(alpha)
            )
            Spacer(modifier = Modifier.height(12.dp))
            NotifCardToggle(
                iconRes = R.drawable.ic_tiktok,
                iconBgColor = Color(0xFF010101),
                title = "TikTok Videos",
                subtitle = "New TikToks from the Captain",
                checked = tiktokVideos,
                enabled = enabled,
                onToggle = { scope.launch { appSettings.setTiktokVideosEnabled(it); syncPrefsToBackend() } },
                modifier = Modifier.alpha(alpha)
            )
            Spacer(modifier = Modifier.height(12.dp))
            NotifCardToggle(
                iconRes = R.drawable.ic_x,
                iconBgColor = Color.Black,
                title = "X Posts",
                subtitle = "New posts from the Captain on X",
                checked = twitterPosts,
                enabled = enabled,
                onToggle = { scope.launch { appSettings.setTwitterPostsEnabled(it); syncPrefsToBackend() } },
                modifier = Modifier.alpha(alpha)
            )

            Spacer(modifier = Modifier.height(24.dp))
            } // end inner Column
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontFamily = PirateTheme.fontFamily,
        fontSize = 16.sp,
        color = PirateTheme.accentColor,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun NotifCardToggle(
    icon: ImageVector? = null,
    iconRes: Int? = null,
    iconTint: Color = Color.White,
    iconBgColor: Color,
    title: String,
    subtitle: String? = null,
    scheduleSummary: String? = null,
    checked: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    expanded: Boolean = false,
    onExpandToggle: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    PirateTheme.cardGradient,
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colored icon background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                if (iconRes != null) {
                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                } else if (icon != null) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = PirateTheme.accentColor, fontFamily = PirateTheme.fontFamily, fontSize = 16.sp)
                if (subtitle != null) {
                    Text(subtitle, color = Color.White.copy(alpha = 0.4f), fontFamily = PirateTheme.fontFamily, fontSize = 12.sp)
                }
                if (scheduleSummary != null) {
                    Text(scheduleSummary, color = PirateTheme.accentColor.copy(alpha = 0.7f), fontFamily = PirateTheme.fontFamily, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }
            if (onExpandToggle != null) {
                IconButton(onClick = onExpandToggle, modifier = Modifier.size(24.dp)) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Schedule",
                        tint = PirateTheme.accentColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Switch(
                checked = checked,
                onCheckedChange = onToggle,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PirateTheme.accentColor
                )
            )
        }
    }
}

private fun formatScheduleSummary(
    allDay: Boolean,
    days: String,
    fromHour: Int,
    fromMinute: Int,
    toHour: Int,
    toMinute: Int
): String {
    if (allDay && days == "0,1,2,3,4,5,6") return "Alerting at all hours"
    if (allDay) {
        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val selectedDays = days.split(",").mapNotNull { it.trim().toIntOrNull() }
        val dayStr = selectedDays.joinToString(", ") { dayNames.getOrElse(it) { "" } }
        return "$dayStr, all hours"
    }
    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val selectedDays = days.split(",").mapNotNull { it.trim().toIntOrNull() }
    val dayStr = selectedDays.joinToString(", ") { dayNames.getOrElse(it) { "" } }
    fun formatTime(h: Int, m: Int): String {
        val amPm = if (h < 12) "AM" else "PM"
        val h12 = if (h == 0) 12 else if (h > 12) h - 12 else h
        return if (m == 0) "$h12 $amPm" else "$h12:%02d $amPm".format(m)
    }
    return "$dayStr, ${formatTime(fromHour, fromMinute)} - ${formatTime(toHour, toMinute)}"
}

@Composable
private fun ScheduleConfig(
    allDay: Boolean,
    days: String,
    fromHour: Int,
    fromMinute: Int,
    toHour: Int,
    toMinute: Int,
    enabled: Boolean,
    onAllDayChange: (Boolean) -> Unit,
    onDaysChange: (String) -> Unit,
    onFromHourChange: (Int) -> Unit,
    onFromMinuteChange: (Int) -> Unit,
    onToHourChange: (Int) -> Unit,
    onToMinuteChange: (Int) -> Unit
) {
    val context = LocalContext.current
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
    val selectedDays = days.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    val alpha = if (enabled) 1f else 0.4f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .alpha(alpha)
    ) {
        // All day toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Alert at all hours", color = Color.White.copy(alpha = 0.7f), fontFamily = PirateTheme.fontFamily, fontSize = 16.sp)
            Switch(
                checked = allDay,
                onCheckedChange = onAllDayChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PirateTheme.accentColor
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Day of week picker
        Text("Which days to keep watch", color = Color.White.copy(alpha = 0.5f), fontFamily = PirateTheme.fontFamily, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayLabels.forEachIndexed { index, label ->
                val isSelected = index in selectedDays
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) PirateTheme.accentColor else Color.White.copy(alpha = 0.1f))
                        .clickable(enabled = enabled) {
                            val newDays = if (isSelected) {
                                selectedDays - index
                            } else {
                                selectedDays + index
                            }
                            onDaysChange(newDays.sorted().joinToString(","))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // Time range (only when not all day)
        if (!allDay) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Watch hours", color = Color.White.copy(alpha = 0.5f), fontFamily = PirateTheme.fontFamily, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // From time picker
                Column {
                    Text("From", color = Color.White.copy(alpha = 0.4f), fontFamily = PirateTheme.fontFamily, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable(enabled = enabled) {
                                android.app.TimePickerDialog(
                                    context,
                                    android.R.style.Theme_Holo_Dialog,
                                    { _, h, m ->
                                        onFromHourChange(h)
                                        onFromMinuteChange(m)
                                    },
                                    fromHour,
                                    fromMinute,
                                    true
                                ).show()
                            }
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            "%02d:%02d".format(fromHour, fromMinute),
                            color = Color.White,
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))
                Text("\u2192", color = PirateTheme.accentColor, fontFamily = PirateTheme.fontFamily, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(16.dp))

                // Until time picker
                Column {
                    Text("Until", color = Color.White.copy(alpha = 0.4f), fontFamily = PirateTheme.fontFamily, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable(enabled = enabled) {
                                android.app.TimePickerDialog(
                                    context,
                                    android.R.style.Theme_Holo_Dialog,
                                    { _, h, m ->
                                        onToHourChange(h)
                                        onToMinuteChange(m)
                                    },
                                    toHour,
                                    toMinute,
                                    true
                                ).show()
                            }
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            "%02d:%02d".format(toHour, toMinute),
                            color = Color.White,
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}
