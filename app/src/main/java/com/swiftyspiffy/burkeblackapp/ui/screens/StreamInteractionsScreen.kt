package com.swiftyspiffy.burkeblackapp.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftyspiffy.burkeblackapp.data.FeatureFlagService
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamInteractionsScreen(
    viewModel: StreamInteractionsViewModel,
    onBack: () -> Unit,
    onNavigateToSoundbytes: () -> Unit,
    onNavigateToOverlayImages: () -> Unit
) {
    val soundbytePick by viewModel.soundbytePick.collectAsState()
    val overlayPick by viewModel.overlayPick.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val sendResult by viewModel.sendResult.collectAsState()
    val sendError by viewModel.sendError.collectAsState()
    val streamIsLive by viewModel.streamIsLive.collectAsState()
    val interactionsDisabled by viewModel.interactionsDisabled.collectAsState()
    val credits by viewModel.credits.collectAsState()
    val totalCost by viewModel.totalCost.collectAsState()
    val imagesEnabled = FeatureFlagService.isEnabled("stream_interactions_images")

    var showFeatureGatedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkStreamStatus()
        viewModel.checkInteractionsEnabled()
        viewModel.loadCredits()
    }

    if (sendResult != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSendResult() },
            title = { Text("Sent!", color = Color.White) },
            text = { Text(sendResult ?: "", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSendResult() }) {
                    Text("OK", color = PirateTheme.accentColor)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (sendError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSendError() },
            title = { Text("Error", color = Color(0xFFEF5350)) },
            text = { Text(sendError ?: "", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearSendError() }) {
                    Text("OK", color = PirateTheme.accentColor)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showFeatureGatedDialog) {
        AlertDialog(
            onDismissRequest = { showFeatureGatedDialog = false },
            title = { Text("Not Available Yet", color = PirateTheme.accentColor) },
            text = { Text("The captain hasn't opened this part of the ship yet! Check back soon.", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(onClick = { showFeatureGatedDialog = false }) {
                    Text("Aye Aye!", color = PirateTheme.accentColor)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stream Interactions", fontFamily = PirateTheme.fontFamily) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Offline warning
                if (!streamIsLive) {
                    WarningBanner(
                        text = "Stream is currently offline",
                        color = Color(0xFFEF5350)
                    )
                }

                // Interactions disabled warning
                if (interactionsDisabled) {
                    WarningBanner(
                        text = "Stream interactions are currently disabled",
                        color = Color(0xFFFF9800)
                    )
                }

                // Credits card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PirateTheme.accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Credits", color = Color.White, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "$credits",
                            color = PirateTheme.accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }

                // Soundbyte selection card
                InteractionCard(
                    icon = Icons.Default.MusicNote,
                    title = "Soundbytes",
                    subtitle = soundbytePick?.let {
                        "Selected: ${it.name} (${if (it.announce) "Announced" else "Quiet"})"
                    } ?: "Choose a Soundbyte",
                    isSelected = soundbytePick != null,
                    costText = if (soundbytePick != null) "1 credit" else null,
                    onClick = onNavigateToSoundbytes
                )

                // Overlay selection card
                InteractionCard(
                    icon = Icons.Default.Image,
                    title = "Image / GIF",
                    subtitle = if (overlayPick != null) {
                        "${overlayPick!!.name} (${overlayPick!!.mode})"
                    } else {
                        "Show an image or GIF on stream"
                    },
                    isSelected = overlayPick != null,
                    costText = overlayPick?.let { "${it.credit} credits" },
                    onClick = {
                        if (imagesEnabled) {
                            onNavigateToOverlayImages()
                        } else {
                            showFeatureGatedDialog = true
                        }
                    }
                )

                // Total cost card
                if (totalCost > 0) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PirateTheme.accentColor, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = PirateTheme.accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Total Cost", color = Color.White, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                "$totalCost credits",
                                color = PirateTheme.accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Send button (sticky at bottom)
            val canSend = totalCost > 0 && streamIsLive && !interactionsDisabled && !isSending
            Button(
                onClick = { viewModel.sendInteractions() },
                enabled = canSend,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canSend) Color(0xFF4CAF50) else Color.Gray,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else if (!streamIsLive) {
                    Icon(Icons.Default.CloudOff, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stream Offline")
                } else if (interactionsDisabled) {
                    Text("Interactions Disabled")
                } else {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send to Stream", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun WarningBanner(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = color, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
private fun InteractionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    costText: String?,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) Modifier.border(1.dp, PirateTheme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                else Modifier
            ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Radio indicator
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(
                        2.dp,
                        if (isSelected) PirateTheme.accentColor else Color.White.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(PirateTheme.accentColor, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected) PirateTheme.accentColor else Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    subtitle,
                    color = if (isSelected) PirateTheme.accentColor else Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            }

            costText?.let {
                Text(
                    it,
                    color = PirateTheme.accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
