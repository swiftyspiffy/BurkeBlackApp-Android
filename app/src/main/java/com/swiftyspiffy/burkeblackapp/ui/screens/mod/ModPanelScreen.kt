package com.swiftyspiffy.burkeblackapp.ui.screens.mod

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.util.AppLogger

enum class ModScreen {
    MENU, VIEWER_LOOKUP, COMMANDS, TIMED_MESSAGES, TIMEOUT_WORDS, SPOILER_WORDS,
    LINKS, SUBMISSIONS, ADD_GIVEAWAY, HIDDEN_SUBMISSIONS, GIVEAWAY_HISTORY,
    SB_LIBRARY, SB_CREDITS, SB_HISTORY, NOTIF_SEND, NOTIF_HISTORY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModPanelScreen(
    viewModel: ModPanelViewModel,
    username: String = "",
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) { AppLogger.log("ModPanel: appeared") }
    val permissions by viewModel.permissions.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentScreen by remember { mutableStateOf(ModScreen.MENU) }
    var showAlertBurke by remember { mutableStateOf(false) }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Sub-screens
    if (currentScreen != ModScreen.MENU) {
        ModSubScreen(
            screen = currentScreen,
            viewModel = viewModel,
            onBack = { currentScreen = ModScreen.MENU }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mod Panel", color = Color.White) },
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PirateTheme.accentColor)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Quick Actions - horizontal buttons like iOS
                if (permissions.hasAdmin || permissions.hasSettings) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            if (permissions.hasAdmin) {
                                QuickActionButton(
                                    icon = Icons.Default.Refresh,
                                    label = "Restart\nBot",
                                    color = Color(0xFFEF5350),
                                    onClick = { viewModel.restartBot() }
                                )
                            }
                            if (permissions.hasSettings) {
                                val sbEnabled = settings?.soundbytesEnabled == true
                                QuickActionButton(
                                    icon = if (sbEnabled) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                    label = if (sbEnabled) "Disable\nSoundbytes" else "Enable\nSoundbytes",
                                    color = PirateTheme.accentColor,
                                    onClick = { viewModel.toggleSoundbytes() }
                                )
                                val ceEnabled = settings?.enforcePunishments == true
                                QuickActionButton(
                                    icon = if (ceEnabled) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    label = if (ceEnabled) "Disable\nEnforcements" else "Enable\nEnforcements",
                                    color = PirateTheme.accentColor,
                                    onClick = { viewModel.toggleEnforcements() }
                                )
                            }
                            QuickActionButton(
                                icon = Icons.Default.Warning,
                                label = "Alert\nBurke",
                                color = Color(0xFFFF9800),
                                onClick = { showAlertBurke = true }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Moderator Tools
                if (permissions.hasChat || permissions.hasLinks) {
                    SectionHeader("Moderator Tools")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            if (permissions.hasChat) {
                                NavRow("Viewer Lookup", Icons.Default.Search) { currentScreen = ModScreen.VIEWER_LOOKUP }
                            }
                            if (permissions.hasLinks) {
                                NavRow("Shorten Links", Icons.Default.Link) { currentScreen = ModScreen.LINKS }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Chat
                if (permissions.hasCommands || permissions.hasTimed || permissions.hasTimeout || permissions.hasSpoiler) {
                    SectionHeader("Chat")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            if (permissions.hasCommands) NavRow("Commands", Icons.AutoMirrored.Filled.Chat) { currentScreen = ModScreen.COMMANDS }
                            if (permissions.hasTimed) NavRow("Timed Messages", Icons.Default.Timer) { currentScreen = ModScreen.TIMED_MESSAGES }
                            if (permissions.hasTimeout) NavRow("Timeout Words", Icons.Default.Warning) { currentScreen = ModScreen.TIMEOUT_WORDS }
                            if (permissions.hasSpoiler) NavRow("Spoiler Words", Icons.Default.VisibilityOff) { currentScreen = ModScreen.SPOILER_WORDS }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Giveaways
                if (permissions.hasGiveaways) {
                    SectionHeader("Giveaways")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            NavRow("Submissions", Icons.Default.CardGiftcard) { currentScreen = ModScreen.SUBMISSIONS }
                            NavRow("Add Giveaway", Icons.Default.Add) { currentScreen = ModScreen.ADD_GIVEAWAY }
                            NavRow("Unhide Submission", Icons.Default.Visibility) { currentScreen = ModScreen.HIDDEN_SUBMISSIONS }
                            NavRow("Giveaway History", Icons.Default.History) { currentScreen = ModScreen.GIVEAWAY_HISTORY }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Soundbytes
                if (permissions.hasAudio) {
                    SectionHeader("Soundbytes")
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            NavRow("Library", Icons.Default.MusicNote) { currentScreen = ModScreen.SB_LIBRARY }
                            NavRow("Credits", Icons.Default.People) { currentScreen = ModScreen.SB_CREDITS }
                            NavRow("History", Icons.Default.History) { currentScreen = ModScreen.SB_HISTORY }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Notifications
                SectionHeader("Notifications")
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        NavRow("Send Announcement", Icons.Default.Notifications) { currentScreen = ModScreen.NOTIF_SEND }
                        NavRow("Notification History", Icons.Default.History) { currentScreen = ModScreen.NOTIF_HISTORY }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Alert Burke dialog
    if (showAlertBurke) {
        var alertMessage by remember { mutableStateOf("") }
        var isSending by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSending) showAlertBurke = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Alert Burke", color = Color.White) },
            text = {
                Column {
                    Text("From", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = username,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PirateTheme.accentColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White.copy(alpha = 0.6f),
                            disabledTextColor = Color.White.copy(alpha = 0.6f),
                            disabledBorderColor = Color.White.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = false
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Message", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = alertMessage,
                        onValueChange = { alertMessage = it },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
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
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (alertMessage.isNotBlank()) {
                            isSending = true
                            viewModel.alertBurke(alertMessage)
                            showAlertBurke = false
                        }
                    },
                    enabled = alertMessage.isNotBlank() && !isSending
                ) {
                    Text("Send", color = if (alertMessage.isNotBlank()) PirateTheme.accentColor else Color.White.copy(alpha = 0.3f))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlertBurke = false }, enabled = !isSending) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
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
private fun NavRow(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.let {
                Icon(it, contentDescription = label, tint = PirateTheme.accentColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(label, color = Color.White)
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Navigate",
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

