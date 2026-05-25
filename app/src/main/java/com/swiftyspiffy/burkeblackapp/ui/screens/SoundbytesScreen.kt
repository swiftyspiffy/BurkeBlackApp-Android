package com.swiftyspiffy.burkeblackapp.ui.screens

import android.media.MediaPlayer
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftyspiffy.burkeblackapp.data.models.Soundbyte
import com.swiftyspiffy.burkeblackapp.data.models.SoundbyteHistoryItem
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import com.swiftyspiffy.burkeblackapp.ui.components.DateUtils
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundbytesScreen(
    viewModel: SoundbytesViewModel,
    onBack: () -> Unit,
    onSelect: ((Soundbyte, Boolean) -> Unit)? = null
) {
    val isSelectMode = onSelect != null
    LaunchedEffect(Unit) { AppLogger.log("Soundbytes: appeared") }
    val soundbytes by viewModel.soundbytes.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val credits by viewModel.credits.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchTerm by viewModel.searchTerm.collectAsState()
    val selectedGenre by viewModel.selectedGenre.collectAsState()
    val total by viewModel.total.collectAsState()
    val sendResult by viewModel.sendResult.collectAsState()
    val sendError by viewModel.sendError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var sendConfirmSoundbyte by remember { mutableStateOf<Soundbyte?>(null) }
    var selectConfirmSoundbyte by remember { mutableStateOf<Soundbyte?>(null) }
    var showHistory by remember { mutableStateOf(false) }
    var currentlyPlayingId by remember { mutableIntStateOf(-1) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Cleanup media player on dispose
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    fun playPreview(soundbyte: Soundbyte) {
        mediaPlayer?.release()
        mediaPlayer = null
        currentlyPlayingId = soundbyte.id
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(soundbyte.location)
                setOnPreparedListener { start() }
                setOnCompletionListener {
                    currentlyPlayingId = -1
                    release()
                    mediaPlayer = null
                }
                prepareAsync()
            }
        } catch (_: Exception) {
            currentlyPlayingId = -1
        }
    }

    fun stopPreview() {
        mediaPlayer?.release()
        mediaPlayer = null
        currentlyPlayingId = -1
    }

    LaunchedEffect(sendResult) {
        sendResult?.let {
            snackbarHostState.showSnackbar("Sent ${it.soundbyteName}!")
            viewModel.clearSendResult()
        }
    }
    LaunchedEffect(sendError) {
        sendError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSendError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Soundbytes", color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "(${credits} credits)",
                            color = PirateTheme.accentColor,
                            fontSize = 13.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        stopPreview()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.loadHistory()
                        showHistory = true
                    }) {
                        Icon(Icons.Default.History, contentDescription = "History", tint = PirateTheme.accentColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchTerm,
                onValueChange = { viewModel.setSearchTerm(it) },
                placeholder = { Text("Search name or author...", color = Color.White.copy(alpha = 0.4f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White.copy(alpha = 0.4f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PirateTheme.accentColor,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = PirateTheme.accentColor
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Genre filter chips
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedGenre == "All",
                        onClick = { viewModel.setSelectedGenre("All") },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PirateTheme.accentColor,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
                items(genres) { genre ->
                    FilterChip(
                        selected = selectedGenre == genre.genre,
                        onClick = { viewModel.setSelectedGenre(genre.genre) },
                        label = { Text(genre.genre) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PirateTheme.accentColor,
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Soundbyte list
            val listState = rememberLazyListState()
            val shouldLoadMore by remember {
                derivedStateOf {
                    val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    lastVisibleIndex >= soundbytes.size - 5
                }
            }
            LaunchedEffect(shouldLoadMore) {
                snapshotFlow { shouldLoadMore }.collect { load ->
                    if (load && !isLoading) viewModel.loadMore()
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(soundbytes, key = { it.id }) { soundbyte ->
                    SoundbyteRow(
                        soundbyte = soundbyte,
                        isPlaying = currentlyPlayingId == soundbyte.id,
                        isSelectMode = isSelectMode,
                        onListen = {
                            if (currentlyPlayingId == soundbyte.id) stopPreview()
                            else playPreview(soundbyte)
                        },
                        onSend = {
                            if (isSelectMode) selectConfirmSoundbyte = soundbyte
                            else sendConfirmSoundbyte = soundbyte
                        }
                    )
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PirateTheme.accentColor, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }

    // Send confirmation dialog
    sendConfirmSoundbyte?.let { soundbyte ->
        AlertDialog(
            onDismissRequest = { sendConfirmSoundbyte = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Send ${soundbyte.name}?", color = Color.White) },
            text = {
                Text(
                    "Cost: ${soundbyte.creditCost} credit(s). You have $credits.",
                    color = Color.White.copy(alpha = 0.6f)
                )
            },
            confirmButton = {
                Column {
                    TextButton(onClick = {
                        viewModel.sendSoundbyte(soundbyte.id, announce = true)
                        sendConfirmSoundbyte = null
                    }) {
                        Text("Send & Announce in Chat", color = PirateTheme.accentColor)
                    }
                    TextButton(onClick = {
                        viewModel.sendSoundbyte(soundbyte.id, announce = false)
                        sendConfirmSoundbyte = null
                    }) {
                        Text("Send Quietly", color = Color.White.copy(alpha = 0.6f))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { sendConfirmSoundbyte = null }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.4f))
                }
            }
        )
    }

    // Select confirmation dialog (for select mode)
    selectConfirmSoundbyte?.let { soundbyte ->
        AlertDialog(
            onDismissRequest = { selectConfirmSoundbyte = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Select ${soundbyte.name}?", color = Color.White) },
            text = {
                Text(
                    "Cost: ${soundbyte.creditCost} credit(s). You have $credits.",
                    color = Color.White.copy(alpha = 0.6f)
                )
            },
            confirmButton = {
                Column {
                    TextButton(onClick = {
                        onSelect?.invoke(soundbyte, true)
                        selectConfirmSoundbyte = null
                    }) {
                        Text("Announce in Chat", color = PirateTheme.accentColor)
                    }
                    TextButton(onClick = {
                        onSelect?.invoke(soundbyte, false)
                        selectConfirmSoundbyte = null
                    }) {
                        Text("Send Quietly", color = Color.White.copy(alpha = 0.6f))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectConfirmSoundbyte = null }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.4f))
                }
            }
        )
    }

    // History bottom sheet
    if (showHistory) {
        val history by viewModel.history.collectAsState()
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { showHistory = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    "Send History",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (history.isEmpty()) {
                    Text(
                        "No history yet",
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 32.dp).align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(modifier = Modifier.height(400.dp)) {
                        items(history) { item ->
                            HistoryRow(item)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SoundbyteRow(
    soundbyte: Soundbyte,
    isPlaying: Boolean,
    isSelectMode: Boolean = false,
    onListen: () -> Unit,
    onSend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top: name + credit cost badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = soundbyte.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = soundbyte.genre,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp
                )
            }
            if (soundbyte.creditCost > 0) {
                Box(
                    modifier = Modifier
                        .background(PirateTheme.accentColor.copy(alpha = 0.15f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${soundbyte.creditCost}",
                        color = PirateTheme.accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bottom: Listen + Send buttons
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onListen,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Stop" else "Play",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    if (isPlaying) "Stop" else "Listen",
                    fontSize = 13.sp,
                    color = Color.White
                )
            }

            Button(
                onClick = onSend,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PirateTheme.accentColor.copy(alpha = 0.15f),
                    contentColor = PirateTheme.accentColor
                )
            ) {
                Icon(
                    if (isSelectMode) Icons.Default.Check else Icons.Default.Send,
                    contentDescription = if (isSelectMode) "Select soundbyte" else "Send soundbyte",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isSelectMode) "Select" else "Send", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun HistoryRow(item: SoundbyteHistoryItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(item.name, color = Color.White, fontWeight = FontWeight.Medium)
            Text(
                DateUtils.formatEpochSecondsWithTime(item.date),
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 12.sp
            )
        }
        if (item.announced) {
            Text("Announced", color = PirateTheme.accentColor, fontSize = 11.sp)
        }
    }
}
