package com.swiftyspiffy.burkeblackapp.ui.screens.mod

import com.swiftyspiffy.burkeblackapp.util.AppLogger
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.swiftyspiffy.burkeblackapp.ui.components.DateUtils
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme

@Composable
fun ModSubScreen(screen: ModScreen, viewModel: ModPanelViewModel, onBack: () -> Unit) {
    when (screen) {
        ModScreen.VIEWER_LOOKUP -> ViewerLookupScreen(viewModel, onBack)
        ModScreen.COMMANDS -> CommandsScreen(viewModel, onBack)
        ModScreen.TIMED_MESSAGES -> TimedMessagesScreen(viewModel, onBack)
        ModScreen.TIMEOUT_WORDS -> TimeoutWordsScreen(viewModel, onBack)
        ModScreen.SPOILER_WORDS -> SpoilerWordsScreen(viewModel, onBack)
        ModScreen.LINKS -> LinksScreen(viewModel, onBack)
        ModScreen.SUBMISSIONS -> SubmissionsScreen(viewModel, onBack)
        ModScreen.ADD_GIVEAWAY -> AddGiveawayScreen(viewModel, onBack)
        ModScreen.HIDDEN_SUBMISSIONS -> HiddenSubmissionsScreen(viewModel, onBack)
        ModScreen.GIVEAWAY_HISTORY -> GiveawayHistoryScreen(viewModel, onBack)
        ModScreen.SB_LIBRARY -> SBLibraryScreen(viewModel, onBack)
        ModScreen.SB_CREDITS -> SBCreditsScreen(viewModel, onBack)
        ModScreen.SB_HISTORY -> SBHistoryScreen(viewModel, onBack)
        ModScreen.NOTIF_SEND -> ModNotifSendScreen(viewModel, onBack)
        ModScreen.NOTIF_HISTORY -> ModNotifHistoryScreen(viewModel, onBack)
        else -> {}
    }
}

private val tfColors @Composable get() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PirateTheme.accentColor,
    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = PirateTheme.accentColor
)

// ─── VIEWER LOOKUP ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewerLookupScreen(vm: ModPanelViewModel, onBack: () -> Unit) {
    LaunchedEffect(Unit) { AppLogger.log("ModPanel: ViewerLookup appeared") }
    var search by remember { mutableStateOf("") }
    val result by vm.viewerResult.collectAsState()

    Scaffold(
        topBar = { ModTopBar("Viewer Lookup", onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                placeholder = { Text("Enter username", color = Color.White.copy(alpha = 0.3f)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.4f)) },
                modifier = Modifier.fillMaxWidth(), colors = tfColors, singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { if (search.isNotBlank()) vm.lookupViewer(search) }) {
                Text("Search", color = PirateTheme.accentColor)
            }

            result?.let { r ->
                Spacer(modifier = Modifier.height(16.dp))

                // Twitch section
                r.twitch?.let { t ->
                    SectionLabel("Twitch")
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                t.profileImageUrl?.let { url ->
                                    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(), contentDescription = "Profile picture", modifier = Modifier.size(48.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                    Spacer(modifier = Modifier.width(12.dp))
                                }
                                Column {
                                    Text(t.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text("ID: ${t.id}", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                                }
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 10.dp))
                            t.type?.let { InfoRow("Type", it) }
                            t.createdAt?.let {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                                InfoRow("Created", it)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats section
                SectionLabel("Stats")
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoRow("Doubloons", "%,d".format(r.doubloons))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                        InfoRow("Soundbyte Credits", "%,d".format(r.soundbyteCredits))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Giveaway Wins
                if (r.giveawayWins.isNotEmpty()) {
                    SectionLabel("Giveaway Wins (${r.giveawayWins.size})")
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            r.giveawayWins.forEachIndexed { index, win ->
                                Column {
                                    Text(win.name, color = Color.White, fontWeight = FontWeight.Medium)
                                    Text("by ${win.donator}", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                                }
                                if (index < r.giveawayWins.lastIndex) {
                                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── COMMANDS ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommandsScreen(vm: ModPanelViewModel, onBack: () -> Unit) {
    val commands by vm.commands.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editCmd by remember { mutableStateOf<com.swiftyspiffy.burkeblackapp.data.models.ModCommand?>(null) }
    var deleteConfirmId by remember { mutableStateOf<Int?>(null) }
    var search by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { AppLogger.log("ModPanel: Commands appeared"); vm.loadCommands() }

    val filtered = if (search.isBlank()) commands else commands.filter {
        it.command.contains(search, ignoreCase = true) || it.data.contains(search, ignoreCase = true)
    }

    Scaffold(
        topBar = { ModTopBar("Commands", onBack) },
        floatingActionButton = { Fab { showAdd = true } },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SearchBox(search) { search = it }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered) { cmd ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { editCmd = cmd }.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("!${cmd.command}", color = Color.White, fontWeight = FontWeight.Medium)
                            Text(cmd.data, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, maxLines = 2)
                            Text("Tier: ${cmd.tier} | CD: ${cmd.cooldown}s", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
                        }
                        IconButton(onClick = { deleteConfirmId = cmd.id }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        }
    }

    // Add dialog
    if (showAdd) {
        var command by remember { mutableStateOf("!") }
        var data by remember { mutableStateOf("") }
        var cooldown by remember { mutableStateOf("0") }
        var tier by remember { mutableStateOf("viewer") }
        FormDialog("Add Command", onDismiss = { showAdd = false }, onConfirm = {
            vm.createCommand(command, data, cooldown.toIntOrNull() ?: 0, tier, "")
            showAdd = false
        }) {
            FormField("Command", command) { command = it }
            FormField("Response", data, multiline = true) { data = it }
            FormField("Cooldown (seconds)", cooldown) { cooldown = it }
            FormField("Tier (viewer/subscriber/moderator)", tier) { tier = it }
        }
    }

    // Edit dialog
    editCmd?.let { cmd ->
        var data by remember { mutableStateOf(cmd.data) }
        var cooldown by remember { mutableStateOf("${cmd.cooldown}") }
        var tier by remember { mutableStateOf(cmd.tier) }
        FormDialog("Edit Command", onDismiss = { editCmd = null }, onConfirm = {
            cmd.id?.let { vm.updateCommand(it, data, cooldown.toIntOrNull() ?: 0, tier) }
            editCmd = null
        }) {
            // Command name (read-only)
            Text("!${cmd.command}", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
            FormField("Response", data, multiline = true) { data = it }
            FormField("Cooldown (seconds)", cooldown) { cooldown = it }
            FormField("Tier (viewer/subscriber/moderator)", tier) { tier = it }
        }
    }

    // Delete confirmation
    deleteConfirmId?.let { id ->
        ConfirmDeleteDialog(
            onConfirm = { vm.deleteCommand(id); deleteConfirmId = null },
            onDismiss = { deleteConfirmId = null }
        )
    }
}

// ─── TIMED MESSAGES ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimedMessagesScreen(vm: ModPanelViewModel, onBack: () -> Unit) {
    val messages by vm.timedMessages.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editMsg by remember { mutableStateOf<com.swiftyspiffy.burkeblackapp.data.models.ModTimedMessage?>(null) }
    var deleteConfirmId by remember { mutableStateOf<Int?>(null) }
    var search by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { AppLogger.log("ModPanel: TimedMessages appeared"); vm.loadTimedMessages() }

    val filtered = if (search.isBlank()) messages else messages.filter {
        it.name.contains(search, ignoreCase = true) || it.message.contains(search, ignoreCase = true)
    }

    Scaffold(
        topBar = { ModTopBar("Timed Messages", onBack) },
        floatingActionButton = { Fab { showAdd = true } },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SearchBox(search) { search = it }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { editMsg = msg }.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(msg.name, color = Color.White, fontWeight = FontWeight.Medium)
                            Text(msg.message, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, maxLines = 2)
                            Text("Every ${msg.interval} min${if (msg.disabled == 1) " (disabled)" else ""}", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
                        }
                        IconButton(onClick = { deleteConfirmId = msg.id }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        }
    }

    // Add dialog
    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        var interval by remember { mutableStateOf("15") }
        FormDialog("Add Timed Message", onDismiss = { showAdd = false }, onConfirm = {
            vm.createTimedMessage(name, message, interval.toIntOrNull() ?: 15, 0, 0)
            showAdd = false
        }) {
            FormField("Name", name) { name = it }
            FormField("Message", message, multiline = true) { message = it }
            FormField("Interval (seconds)", interval) { interval = it }
        }
    }

    // Edit dialog
    editMsg?.let { msg ->
        var name by remember { mutableStateOf(msg.name) }
        var message by remember { mutableStateOf(msg.message) }
        var interval by remember { mutableStateOf("${msg.interval}") }
        var dedicated by remember { mutableStateOf(msg.dedicated == 1) }
        var disabled by remember { mutableStateOf(msg.disabled == 1) }
        FormDialog("Edit Timed Message", onDismiss = { editMsg = null }, onConfirm = {
            msg.id?.let { vm.updateTimedMessage(it, name, message, interval.toIntOrNull() ?: 15, if (dedicated) 1 else 0, if (disabled) 1 else 0) }
            editMsg = null
        }) {
            FormField("Name", name) { name = it }
            FormField("Message", message, multiline = true) { message = it }
            FormField("Interval (seconds)", interval) { interval = it }
            Spacer(modifier = Modifier.height(8.dp))
            ToggleFormRow("Dedicated", dedicated) { dedicated = it }
            ToggleFormRow("Disabled", disabled) { disabled = it }
        }
    }

    // Delete confirmation
    deleteConfirmId?.let { id ->
        ConfirmDeleteDialog(
            onConfirm = { vm.deleteTimedMessage(id); deleteConfirmId = null },
            onDismiss = { deleteConfirmId = null }
        )
    }
}

// ─── TIMEOUT WORDS ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeoutWordsScreen(vm: ModPanelViewModel, onBack: () -> Unit) {
    val words by vm.timeoutWords.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var deleteConfirmId by remember { mutableStateOf<Int?>(null) }
    var search by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { AppLogger.log("ModPanel: TimeoutWords appeared"); vm.loadTimeoutWords() }

    val filtered = if (search.isBlank()) words else words.filter {
        it.word.contains(search, ignoreCase = true) || it.category.contains(search, ignoreCase = true)
    }

    Scaffold(
        topBar = { ModTopBar("Timeout Words", onBack) },
        floatingActionButton = { Fab { showAdd = true } },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SearchBox(search) { search = it }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered) { w ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { w.id?.let { vm.toggleTimeoutWord(it, if (w.enabled == 1) 0 else 1) } }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(w.word, color = Color.White, fontWeight = FontWeight.Medium)
                            Text(w.category, color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Silent indicator (orange speaker.slash)
                            if (w.silent == 1) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeOff,
                                    contentDescription = "Silent",
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            // Part-of indicator (blue magnifying glass)
                            if (w.partOf == 1) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Part-of match",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            // Enabled indicator (green/red circle)
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (w.enabled == 1) Color(0xFF4CAF50) else Color(0xFFEF5350))
                            )
                            // Delete button
                            IconButton(onClick = { deleteConfirmId = w.id }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        }
    }

    if (showAdd) {
        var word by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("") }
        var silent by remember { mutableStateOf(false) }
        var partOf by remember { mutableStateOf(false) }
        FormDialog("Add Timeout Word", onDismiss = { showAdd = false }, onConfirm = {
            vm.createTimeoutWord(word, category, if (silent) 1 else 0, if (partOf) 1 else 0)
            showAdd = false
        }) {
            FormField("Word", word) { word = it }
            FormField("Category", category) { category = it }
            Spacer(modifier = Modifier.height(8.dp))
            ToggleFormRow("Silent", silent) { silent = it }
            ToggleFormRow("Part-of match", partOf) { partOf = it }
        }
    }

    // Delete confirmation
    deleteConfirmId?.let { id ->
        ConfirmDeleteDialog(
            onConfirm = { vm.deleteTimeoutWord(id); deleteConfirmId = null },
            onDismiss = { deleteConfirmId = null }
        )
    }
}

// ─── SPOILER WORDS ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpoilerWordsScreen(vm: ModPanelViewModel, onBack: () -> Unit) {
    val words by vm.spoilerWords.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var deleteConfirmId by remember { mutableStateOf<Int?>(null) }
    var search by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { AppLogger.log("ModPanel: SpoilerWords appeared"); vm.loadSpoilerWords() }

    val filtered = if (search.isBlank()) words else words.filter {
        it.word.contains(search, ignoreCase = true)
    }

    Scaffold(
        topBar = { ModTopBar("Spoiler Words", onBack) },
        floatingActionButton = { Fab { showAdd = true } },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SearchBox(search) { search = it }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered) { w ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { w.id?.let { vm.toggleSpoilerWord(it, if (w.enabled == 1) 0 else 1) } }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(w.word, color = Color.White, fontWeight = FontWeight.Medium)
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Silent indicator (orange speaker.slash)
                            if (w.silent == 1) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeOff,
                                    contentDescription = "Silent",
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            // Part-of indicator (blue magnifying glass)
                            if (w.partOf == 1) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Part-of match",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            // Enabled indicator (green/red circle)
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (w.enabled == 1) Color(0xFF4CAF50) else Color(0xFFEF5350))
                            )
                            // Delete button
                            IconButton(onClick = { deleteConfirmId = w.id }) {
                                Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        }
    }

    if (showAdd) {
        var word by remember { mutableStateOf("") }
        var silent by remember { mutableStateOf(false) }
        var partOf by remember { mutableStateOf(false) }
        FormDialog("Add Spoiler Word", onDismiss = { showAdd = false }, onConfirm = {
            vm.createSpoilerWord(word, if (silent) 1 else 0, if (partOf) 1 else 0)
            showAdd = false
        }) {
            FormField("Word", word) { word = it }
            Spacer(modifier = Modifier.height(8.dp))
            ToggleFormRow("Silent", silent) { silent = it }
            ToggleFormRow("Part-of match", partOf) { partOf = it }
        }
    }

    // Delete confirmation
    deleteConfirmId?.let { id ->
        ConfirmDeleteDialog(
            onConfirm = { vm.deleteSpoilerWord(id); deleteConfirmId = null },
            onDismiss = { deleteConfirmId = null }
        )
    }
}

// ─── LINKS ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinksScreen(vm: ModPanelViewModel, onBack: () -> Unit) {
    val links by vm.links.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var editLink by remember { mutableStateOf<com.swiftyspiffy.burkeblackapp.data.models.ModLink?>(null) }
    var deleteConfirmId by remember { mutableStateOf<Int?>(null) }
    var search by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { AppLogger.log("ModPanel: Links appeared"); vm.loadLinks() }

    val filtered = if (search.isBlank()) links else links.filter {
        it.customName.contains(search, ignoreCase = true) || it.longUrl.contains(search, ignoreCase = true)
    }

    Scaffold(
        topBar = { ModTopBar("Shorten Links", onBack) },
        floatingActionButton = { Fab { showAdd = true } },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SearchBox(search) { search = it }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered) { link ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { editLink = link }.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("burke.black/${link.customName}", color = Color.White, fontWeight = FontWeight.Medium)
                            Text(link.longUrl, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, maxLines = 2)
                        }
                        IconButton(onClick = { deleteConfirmId = link.id }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var url by remember { mutableStateOf("") }
        FormDialog("Add Link", onDismiss = { showAdd = false }, onConfirm = {
            vm.createLink(name, url)
            showAdd = false
        }) {
            FormField("Custom Name", name) { name = it }
            FormField("Long URL", url) { url = it }
        }
    }

    editLink?.let { link ->
        var url by remember { mutableStateOf(link.longUrl) }
        FormDialog("Edit Link", onDismiss = { editLink = null }, onConfirm = {
            link.id?.let { vm.updateLink(it, url) }
            editLink = null
        }) {
            Text("burke.black/${link.customName}", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
            FormField("URL", url) { url = it }
        }
    }

    deleteConfirmId?.let { id ->
        ConfirmDeleteDialog(
            onConfirm = { vm.deleteLink(id); deleteConfirmId = null },
            onDismiss = { deleteConfirmId = null }
        )
    }
}

// ─── GIVEAWAY SUBMISSIONS ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubmissionsScreen(vm: ModPanelViewModel, onBack: () -> Unit) {
    val submissions by vm.submissions.collectAsState()
    var search by remember { mutableStateOf("") }
    var selectedSub by remember { mutableStateOf<com.swiftyspiffy.burkeblackapp.data.models.GiveawaySubmission?>(null) }
    var showSendForm by remember { mutableStateOf<com.swiftyspiffy.burkeblackapp.data.models.GiveawaySubmission?>(null) }
    var hideConfirmId by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(Unit) { AppLogger.log("ModPanel: Submissions appeared"); vm.loadSubmissions() }

    val filtered = if (search.isBlank()) submissions else submissions.filter {
        it.giveawayName.contains(search, ignoreCase = true) ||
            it.suggestedUser.contains(search, ignoreCase = true) ||
            it.giveawayType.contains(search, ignoreCase = true)
    }

    Scaffold(
        topBar = { ModTopBar("Submissions", onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SearchBox(search) { search = it }
            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No submissions", color = Color.White.copy(alpha = 0.4f))
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered) { sub ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSub = sub }
                                .padding(16.dp)
                        ) {
                            Text(sub.giveawayName, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("by ${sub.suggestedUser} (${sub.giveawayType})", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { hideConfirmId = sub.id }) {
                                    Text("Hide", color = Color(0xFFEF5350))
                                }
                            }
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    }
                }
            }
        }
    }

    hideConfirmId?.let { id ->
        ConfirmDialog(
            title = "Hide Submission?",
            message = "Are you sure you want to hide this submission?",
            confirmText = "Hide",
            onConfirm = { vm.hideSubmission(id); hideConfirmId = null },
            onDismiss = { hideConfirmId = null }
        )
    }

    // "Send to Kraken?" confirmation dialog
    selectedSub?.let { sub ->
        AlertDialog(
            onDismissRequest = { selectedSub = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Send to Kraken?", color = Color.White) },
            text = {
                Column {
                    Text(sub.giveawayName, color = Color.White, fontWeight = FontWeight.Bold)
                    Text("by ${sub.suggestedUser}", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showSendForm = sub
                    selectedSub = null
                }) {
                    Text("Review & Send", color = PirateTheme.accentColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSub = null }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }

    // Send to Kraken form screen
    showSendForm?.let { sub ->
        SendToKrakenFormScreen(
            submission = sub,
            vm = vm,
            onBack = { showSendForm = null }
        )
    }
}

// ─── SEND TO KRAKEN FORM ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SendToKrakenFormScreen(
    submission: com.swiftyspiffy.burkeblackapp.data.models.GiveawaySubmission,
    vm: ModPanelViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(submission.giveawayName) }
    var donator by remember { mutableStateOf(submission.suggestedUser.ifBlank { submission.realUser }) }
    var type by remember { mutableStateOf(submission.giveawayType.ifBlank { "code" }) }
    var typeExpanded by remember { mutableStateOf(false) }
    var keyCode by remember { mutableStateOf(submission.giveawayData) }
    var extraInfo by remember { mutableStateOf(submission.giveawayExtra) }
    var filter by remember { mutableStateOf("none") }
    var filterExpanded by remember { mutableStateOf(false) }
    var filterAmount by remember { mutableStateOf("0") }
    var entryDuration by remember { mutableStateOf("300") }
    var claimDuration by remember { mutableStateOf("120") }

    val typeOptions = listOf("code", "steam_trade", "steam_code", "steam_gift", "discord", "epicgames_code", "humblebundle", "logitech", "origin", "gog", "uplay", "soundbyte", "other")
    val filterOptions = listOf("none", "subscriber", "follower")

    Scaffold(
        topBar = { ModTopBar("Send to Kraken", onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Giveaway Details section
            SectionLabel("Giveaway Details")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FormField("Name", name) { name = it }
                    FormField("Donator", donator) { donator = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Type", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                    ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                        OutlinedTextField(
                            value = type,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = tfColors,
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) }
                        )
                        ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            typeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { type = option; typeExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prize section
            SectionLabel("Prize")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FormField("Key / Code", keyCode) { keyCode = it }
                    FormField("Extra info", extraInfo) { extraInfo = it }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filters section
            SectionLabel("Filters")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Filter", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                    ExposedDropdownMenuBox(expanded = filterExpanded, onExpandedChange = { filterExpanded = it }) {
                        OutlinedTextField(
                            value = filter,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = tfColors,
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterExpanded) }
                        )
                        ExposedDropdownMenu(expanded = filterExpanded, onDismissRequest = { filterExpanded = false }) {
                            filterOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { filter = option; filterExpanded = false }
                                )
                            }
                        }
                    }
                    if (filter != "none") {
                        FormField("Filter amount", filterAmount) { filterAmount = it }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timing section
            SectionLabel("Timing")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FormField("Entry duration (seconds)", entryDuration) { entryDuration = it }
                    FormField("Claim duration (seconds)", claimDuration) { claimDuration = it }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Send button
            Button(
                onClick = {
                    val entryMinutes = ((entryDuration.toIntOrNull() ?: 300) / 60).toString()
                    val claimMinutes = ((claimDuration.toIntOrNull() ?: 120) / 60).toString()
                    vm.sendToKraken(
                        submissionId = submission.id,
                        name = name,
                        donator = donator,
                        type = type,
                        prize = keyCode,
                        filter = filter,
                        filterAmount = filterAmount,
                        entryDuration = entryMinutes,
                        claimDuration = claimMinutes
                    )
                    onBack()
                },
                enabled = name.isNotBlank() && keyCode.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send to Kraken", tint = PirateTheme.accentColor, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send to Kraken", color = PirateTheme.accentColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─── ADD GIVEAWAY ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGiveawayScreen(vm: ModPanelViewModel, onBack: () -> Unit) {
    LaunchedEffect(Unit) { AppLogger.log("ModPanel: AddGiveaway appeared") }
    var isBulk by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var donator by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("code") }
    var typeExpanded by remember { mutableStateOf(false) }
    var singleKey by remember { mutableStateOf("") }
    var bulkKeys by remember { mutableStateOf("") }
    var extra by remember { mutableStateOf("") }

    val typeOptions = listOf("code", "steam_trade", "steam_code", "steam_gift", "discord", "epicgames_code", "humblebundle", "logitech", "origin", "gog", "uplay", "soundbyte", "other")

    Scaffold(
        topBar = { ModTopBar("Add Giveaway", onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            // Single / Bulk toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (!isBulk) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                            RoundedCornerShape(50)
                        )
                        .clickable { isBulk = false }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Single Key", color = Color.White, fontWeight = if (!isBulk) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isBulk) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                            RoundedCornerShape(50)
                        )
                        .clickable { isBulk = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Bulk Import", color = Color.White, fontWeight = if (isBulk) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Giveaway details
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FormField("Giveaway Name", name) { name = it }
                    FormField("Donator", donator) { donator = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Type", color = Color.White)
                        TextButton(onClick = { typeExpanded = true }) {
                            Text(type, color = PirateTheme.accentColor)
                            Text(" \u25BE", color = PirateTheme.accentColor, fontSize = 12.sp)
                        }
                        androidx.compose.material3.DropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false }
                        ) {
                            typeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = { type = option; typeExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Key input
            if (isBulk) {
                SectionLabel("Keys (one per line)")
                OutlinedTextField(
                    value = bulkKeys,
                    onValueChange = { bulkKeys = it },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
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
            } else {
                SectionLabel("Key / Code")
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        FormField("Enter key or code", singleKey) { singleKey = it }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Extra info
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FormField("Extra info (optional)", extra) { extra = it }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit
            val keyList = if (isBulk) {
                bulkKeys.lines().map { it.trim() }.filter { it.isNotBlank() }
            } else {
                if (singleKey.isNotBlank()) listOf(singleKey.trim()) else emptyList()
            }

            Button(
                onClick = {
                    vm.addGiveaway(name, donator, type, keyList, extra)
                    onBack()
                },
                enabled = name.isNotBlank() && donator.isNotBlank() && keyList.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Submit", color = if (name.isNotBlank() && keyList.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.3f), fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─── HIDDEN SUBMISSIONS ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HiddenSubmissionsScreen(vm: ModPanelViewModel, onBack: () -> Unit) {
    val hidden by vm.hiddenSubmissions.collectAsState()
    var unhideConfirmId by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(Unit) { AppLogger.log("ModPanel: HiddenSubmissions appeared"); vm.loadHiddenSubmissions() }

    Scaffold(
        topBar = { ModTopBar("Hidden Submissions", onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (hidden.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No hidden submissions", color = Color.White.copy(alpha = 0.4f))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(hidden) { sub ->
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sub.giveawayName, color = Color.White, fontWeight = FontWeight.Medium)
                            Text("by ${sub.suggestedUser}", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                        TextButton(onClick = { unhideConfirmId = sub.id }) {
                            Text("Unhide", color = PirateTheme.accentColor)
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        }
    }

    unhideConfirmId?.let { id ->
        ConfirmDialog(
            title = "Unhide Submission?",
            message = "Are you sure you want to unhide this submission?",
            confirmText = "Unhide",
            onConfirm = { vm.unhideSubmission(id); unhideConfirmId = null },
            onDismiss = { unhideConfirmId = null }
        )
    }
}

// ─── GIVEAWAY HISTORY ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GiveawayHistoryScreen(vm: ModPanelViewModel, onBack: () -> Unit) {
    val history by vm.giveawayHistory.collectAsState()
    LaunchedEffect(Unit) { AppLogger.log("ModPanel: GiveawayHistory appeared"); vm.loadGiveawayHistory() }

    Scaffold(
        topBar = { ModTopBar("Giveaway History", onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(history) { item ->
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(item.name, color = Color.White, fontWeight = FontWeight.Medium)
                        Text(item.state, color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                    }
                    Text("by ${item.donator}", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
            }
        }
    }
}

// ─── SOUNDBYTE LIBRARY ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SBLibraryScreen(vm: ModPanelViewModel, onBack: () -> Unit) {
    val library by vm.sbLibrary.collectAsState()
    val genres by vm.sbGenres.collectAsState()
    var search by remember { mutableStateOf("") }
    var editSb by remember { mutableStateOf<com.swiftyspiffy.burkeblackapp.data.models.ModSoundbyte?>(null) }
    var currentlyPlayingId by remember { mutableStateOf(-1) }
    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    LaunchedEffect(Unit) { AppLogger.log("ModPanel: SBLibrary appeared"); vm.loadSBLibrary() }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { mediaPlayer?.release(); mediaPlayer = null }
    }

    fun playPreview(sb: com.swiftyspiffy.burkeblackapp.data.models.ModSoundbyte) {
        mediaPlayer?.release(); mediaPlayer = null
        currentlyPlayingId = sb.id
        try {
            mediaPlayer = android.media.MediaPlayer().apply {
                setDataSource(sb.audioLocation)
                setOnPreparedListener { start() }
                setOnCompletionListener { currentlyPlayingId = -1; release(); mediaPlayer = null }
                prepareAsync()
            }
        } catch (_: Exception) { currentlyPlayingId = -1 }
    }

    fun stopPreview() {
        mediaPlayer?.release(); mediaPlayer = null; currentlyPlayingId = -1
    }

    val filtered = if (search.isBlank()) library else library.filter { it.audioName.contains(search, ignoreCase = true) }

    // Edit screen
    if (editSb != null) {
        SBEditScreen(sb = editSb!!, genres = genres, vm = vm, onBack = { editSb = null })
        return
    }

    Scaffold(
        topBar = { ModTopBar("Library (${library.size})", onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                placeholder = { Text("Search soundbytes", color = Color.White.copy(alpha = 0.3f)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.4f)) },
                modifier = Modifier.fillMaxWidth().padding(16.dp), colors = tfColors, singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            LazyColumn {
                items(filtered) { sb ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editSb = sb }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play button
                        IconButton(
                            onClick = {
                                if (currentlyPlayingId == sb.id) stopPreview()
                                else playPreview(sb)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                if (currentlyPlayingId == sb.id) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = PirateTheme.accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sb.audioName, color = PirateTheme.accentColor, fontWeight = FontWeight.Medium)
                            Text("${sb.genre}   by ${sb.uploadedBy}", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                        }
                        // Credit cost badge
                        Text(
                            "${sb.creditCost}",
                            color = PirateTheme.accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(PirateTheme.accentColor.copy(alpha = 0.15f), RoundedCornerShape(50))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Status dot
                        val statusColor = when (sb.approved) { 1 -> Color(0xFF4CAF50); 0 -> Color(0xFFEF5350); else -> PirateTheme.accentColor }
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(statusColor))
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SBEditScreen(
    sb: com.swiftyspiffy.burkeblackapp.data.models.ModSoundbyte,
    genres: List<com.swiftyspiffy.burkeblackapp.data.models.SoundbyteGenre>,
    vm: ModPanelViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(sb.audioName) }
    var genre by remember { mutableStateOf(sb.genre) }
    var genreExpanded by remember { mutableStateOf(false) }
    var approved by remember { mutableStateOf(sb.approved) }
    var approvedExpanded by remember { mutableStateOf(false) }
    var creditCost by remember { mutableStateOf("${sb.creditCost}") }
    var horrorNight by remember { mutableStateOf(sb.horrorNight == 1) }

    val statusOptions = listOf(1 to "Approved", 0 to "Denied", 2 to "Mod Only")

    Scaffold(
        topBar = { ModTopBar("Edit Soundbyte", onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Main edit card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FormField("Name", name) { name = it }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Genre picker
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Genre", color = Color.White)
                        TextButton(onClick = { genreExpanded = true }) {
                            Text(genre, color = PirateTheme.accentColor)
                            Text(" \u25BE", color = PirateTheme.accentColor, fontSize = 12.sp)
                        }
                        androidx.compose.material3.DropdownMenu(
                            expanded = genreExpanded,
                            onDismissRequest = { genreExpanded = false }
                        ) {
                            genres.forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g.genre) },
                                    onClick = { genre = g.genre; genreExpanded = false }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    // Status picker
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status", color = Color.White)
                        TextButton(onClick = { approvedExpanded = true }) {
                            val label = statusOptions.find { it.first == approved }?.second ?: "Unknown"
                            Text(label, color = PirateTheme.accentColor)
                            Text(" \u25BE", color = PirateTheme.accentColor, fontSize = 12.sp)
                        }
                        androidx.compose.material3.DropdownMenu(
                            expanded = approvedExpanded,
                            onDismissRequest = { approvedExpanded = false }
                        ) {
                            statusOptions.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = { approved = value; approvedExpanded = false }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    FormField("Credit Cost", creditCost) { creditCost = it }

                    Spacer(modifier = Modifier.height(8.dp))

                    ToggleFormRow("Horror Night", horrorNight) { horrorNight = it }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info card (read-only)
            SectionLabel("Info")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("ID", "${sb.id}")
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                    InfoRow("Uploaded by", sb.uploadedBy)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            Button(
                onClick = {
                    vm.updateSoundbyte(
                        id = sb.id,
                        name = name,
                        genre = genre,
                        approved = approved,
                        horrorNight = if (horrorNight) 1 else 0,
                        creditCost = creditCost.toIntOrNull() ?: 1
                    )
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Save", color = PirateTheme.accentColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─── SOUNDBYTE CREDITS ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SBCreditsScreen(vm: ModPanelViewModel, onBack: () -> Unit) {
    val credits by vm.sbCredits.collectAsState()
    var search by remember { mutableStateOf("") }
    var showModify by remember { mutableStateOf(false) }
    var modifyUserId by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { AppLogger.log("ModPanel: SBCredits appeared"); vm.loadSBCredits() }

    val filtered = if (search.isBlank()) credits else credits.filter {
        (it.username ?: it.userId).contains(search, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credits (${credits.size})", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { modifyUserId = ""; showModify = true }) {
                        Text("+/-", color = PirateTheme.accentColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                placeholder = { Text("Search by user ID", color = Color.White.copy(alpha = 0.3f)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.4f)) },
                modifier = Modifier.fillMaxWidth().padding(16.dp), colors = tfColors, singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            LazyColumn {
                items(filtered) { credit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { modifyUserId = credit.userId; showModify = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(credit.username ?: credit.userId, color = PirateTheme.accentColor)
                        Text("%,d".format(credit.creditCount), color = PirateTheme.accentColor, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        }
    }

    // Modify Credits dialog
    if (showModify) {
        ModifyCreditDialog(
            initialUserId = modifyUserId,
            onDismiss = { showModify = false },
            onSubmit = { userId, amount, direction ->
                vm.modifySBCredits(userId, amount, direction)
                showModify = false
            }
        )
    }
}

@Composable
private fun ModifyCreditDialog(
    initialUserId: String,
    onDismiss: () -> Unit,
    onSubmit: (String, Int, String) -> Unit
) {
    var userId by remember { mutableStateOf(initialUserId) }
    var amount by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf("add") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Modify Credits", color = Color.White) },
        text = {
            Column {
                FormField("User ID", userId) { userId = it }
                FormField("Amount", amount) { amount = it }
                Spacer(modifier = Modifier.height(12.dp))

                // Add / Deduct toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(50))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (direction == "add") Color.White.copy(alpha = 0.2f) else Color.Transparent,
                                RoundedCornerShape(50)
                            )
                            .clickable { direction = "add" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Add", color = Color.White, fontWeight = if (direction == "add") FontWeight.Bold else FontWeight.Normal)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (direction == "deduct") Color.White.copy(alpha = 0.2f) else Color.Transparent,
                                RoundedCornerShape(50)
                            )
                            .clickable { direction = "deduct" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Deduct", color = Color.White, fontWeight = if (direction == "deduct") FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amt = amount.toIntOrNull() ?: 0
                    if (userId.isNotBlank() && amt > 0) onSubmit(userId, amt, direction)
                }
            ) {
                Text("${direction.replaceFirstChar { it.uppercase() }} Credits", color = PirateTheme.accentColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White.copy(alpha = 0.5f)) }
        }
    )
}

// ─── SOUNDBYTE HISTORY ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SBHistoryScreen(vm: ModPanelViewModel, onBack: () -> Unit) {
    val history by vm.sbHistory.collectAsState()
    var search by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { AppLogger.log("ModPanel: SBHistory appeared"); vm.loadSBHistory() }

    val filtered = if (search.isBlank()) history else history.filter {
        (it.audioName ?: "").contains(search, ignoreCase = true) ||
        it.username.contains(search, ignoreCase = true)
    }

    Scaffold(
        topBar = { ModTopBar("History (${history.size})", onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                placeholder = { Text("Search history", color = Color.White.copy(alpha = 0.3f)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.4f)) },
                modifier = Modifier.fillMaxWidth().padding(16.dp), colors = tfColors, singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
            LazyColumn {
                items(filtered) { item ->
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                        // Top row: name ... platform badge
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Text(
                                item.audioName ?: "ID: ${item.sbId}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                item.platform,
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        // Bottom row: user icon + username ... announce icon + date
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.username, color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (item.announce == 1) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Announced",
                                        tint = PirateTheme.accentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(item.timestamp, color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
                            }
                        }
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                }
            }
        }
    }
}

// ─── SHARED COMPONENTS ───

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, color = Color.White) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
private fun Fab(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick, containerColor = PirateTheme.accentColor) {
        Icon(Icons.Default.Add, "Add", tint = Color.Black)
    }
}

@Composable
private fun SearchBox(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Search...", color = Color.White.copy(alpha = 0.3f)) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.4f)) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = tfColors,
        singleLine = true,
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun CrudRow(title: String, subtitle: String, extra: String? = null, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, maxLines = 2)
            extra?.let { Text(it, color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp) }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, "Delete", tint = Color(0xFFEF5350), modifier = Modifier.size(18.dp))
        }
    }
    HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White)
        Text(value, color = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
private fun FormDialog(title: String, onDismiss: () -> Unit, onConfirm: () -> Unit, content: @Composable () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title, color = Color.White) },
        text = { Column { content() } },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Save", color = PirateTheme.accentColor) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White.copy(alpha = 0.5f)) } }
    )
}

@Composable
private fun ConfirmDialog(title: String, message: String, confirmText: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text(title, color = Color.White) },
        text = { Text(message, color = Color.White.copy(alpha = 0.6f)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText, color = PirateTheme.accentColor) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White.copy(alpha = 0.5f)) } }
    )
}

@Composable
private fun ConfirmDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Confirm Delete", color = Color.White) },
        text = { Text("Are you sure you want to delete this?", color = Color.White.copy(alpha = 0.6f)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete", color = Color(0xFFEF5350)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White.copy(alpha = 0.5f)) } }
    )
}

@Composable
private fun ToggleFormRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = PirateTheme.accentColor)
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.4f),
        fontSize = 13.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun FormField(label: String, value: String, multiline: Boolean = false, onChange: (String) -> Unit) {
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.4f)) },
        modifier = Modifier.fillMaxWidth().then(if (multiline) Modifier.height(120.dp) else Modifier),
        colors = tfColors, singleLine = !multiline,
        shape = RoundedCornerShape(8.dp)
    )
}

// ─── MOD NOTIFICATION SEND ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModNotifSendScreen(viewModel: ModPanelViewModel, onBack: () -> Unit) {
    LaunchedEffect(Unit) { AppLogger.log("ModPanel: NotifSend appeared") }
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send Announcement", color = Color.White) },
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
            FormField("Title", title) { title = it }
            FormField("Message", message, multiline = true) { message = it }
            FormField("URL (optional)", url) { url = it }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (title.isBlank() || message.isBlank()) return@Button
                    isSending = true
                    scope.launch {
                        try {
                            val body = kotlinx.serialization.json.buildJsonObject {
                                put("title", kotlinx.serialization.json.JsonPrimitive(title))
                                put("message", kotlinx.serialization.json.JsonPrimitive(message))
                                if (url.isNotBlank()) put("url", kotlinx.serialization.json.JsonPrimitive(url))
                            }
                            val response = com.swiftyspiffy.burkeblackapp.data.api.ApiClient.api.sendModNotification("Bearer ${viewModel.token}", body)
                            if (response.success) {
                                snackbarHostState.showSnackbar("Notification sent!")
                                title = ""
                                message = ""
                                url = ""
                            } else {
                                snackbarHostState.showSnackbar(response.error ?: "Send failed")
                            }
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar(e.message ?: "Send failed")
                        }
                        isSending = false
                    }
                },
                enabled = title.isNotBlank() && message.isNotBlank() && !isSending,
                colors = ButtonDefaults.buttonColors(containerColor = PirateTheme.accentColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (isSending) "Sending..." else "Send Notification", color = Color.Black, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─── MOD NOTIFICATION HISTORY ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModNotifHistoryScreen(viewModel: ModPanelViewModel, onBack: () -> Unit) {
    var history by remember { mutableStateOf<kotlinx.serialization.json.JsonArray?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        AppLogger.log("ModPanel: NotifHistory appeared")
        try {
            val response = com.swiftyspiffy.burkeblackapp.data.api.ApiClient.api.getNotificationHistory("Bearer ${viewModel.token}")
            if (response.success && response.data != null) {
                history = response.data["history"]?.jsonArray
            }
        } catch (_: Exception) {}
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification History", color = Color.White) },
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PirateTheme.accentColor)
            }
        } else if (history.isNullOrEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No notifications sent yet", color = Color.White.copy(alpha = 0.3f))
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                items(history!!.toList()) { item ->
                    val obj = item.jsonObject
                    val title = obj["title"]?.jsonPrimitive?.content ?: ""
                    val msg = obj["message"]?.jsonPrimitive?.content ?: ""
                    val type = obj["type"]?.jsonPrimitive?.content ?: ""
                    val sender = obj["sender_username"]?.jsonPrimitive?.content ?: ""
                    val iosCount = obj["ios_count"]?.jsonPrimitive?.int ?: 0
                    val androidCount = obj["android_count"]?.jsonPrimitive?.int ?: 0
                    val createdAt = obj["created_at"]?.jsonPrimitive?.content ?: ""

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                Box(
                                    modifier = Modifier
                                        .background(PirateTheme.accentColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(type.replace("_", " "), color = PirateTheme.accentColor, fontSize = 10.sp)
                                }
                            }
                            if (msg.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(msg, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, maxLines = 2)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "$sender | iOS: $iosCount, Android: $androidCount | $createdAt",
                                color = Color.White.copy(alpha = 0.3f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
