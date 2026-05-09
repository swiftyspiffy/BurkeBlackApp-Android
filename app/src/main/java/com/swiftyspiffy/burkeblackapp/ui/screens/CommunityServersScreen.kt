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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swiftyspiffy.burkeblackapp.data.models.CommunityServer
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityServersScreen(
    viewModel: CommunityServersViewModel,
    onBack: () -> Unit,
    onServerClick: (CommunityServer) -> Unit
) {
    LaunchedEffect(Unit) { AppLogger.log("CommunityServers: appeared") }
    val servers by viewModel.servers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

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
                Icons.Default.SportsEsports,
                contentDescription = null,
                tint = PirateTheme.accentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Community Game Servers",
                fontFamily = PirateTheme.fontFamily,
                fontSize = 22.sp,
                color = PirateTheme.accentColor
            )
        }

        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.fetchServers() },
            modifier = Modifier.fillMaxSize()
        ) {
        when {
            isLoading && servers.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PirateTheme.accentColor)
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Shipwrecked!",
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 24.sp,
                            color = PirateTheme.accentColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error ?: "",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }
            servers.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Text(
                            text = "\u2693",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "All Ports Are Quiet",
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 24.sp,
                            color = PirateTheme.accentColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No community servers are sailin' the seas right now. Check back later, matey!",
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 15.sp,
                            color = Color.White.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    servers.forEach { server ->
                        ServerCard(
                            server = server,
                            onClick = { onServerClick(server) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        } // PullToRefreshBox
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServerCard(server: CommunityServer, onClick: () -> Unit) {
    val alpha = if (server.hasAccess) 1f else 0.45f

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box {
        Column(modifier = Modifier.graphicsLayer { this.alpha = alpha }) {
            // Banner image
            if (server.bannerUrl != null) {
                Box {
                    AsyncImage(
                        model = server.bannerUrl,
                        contentDescription = server.serverName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 7f)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 7f)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surface
                                    ),
                                    startY = 150f
                                )
                            )
                    )
                    // Game name badge
                    Text(
                        text = server.gameName,
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 13.sp,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                Color.Black.copy(alpha = 0.6f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Server name
                    Text(
                        text = server.serverName,
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 20.sp,
                        color = PirateTheme.accentColor
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Requirements badges + access status
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (server.requireSub) {
                            RequirementBadge("Sub", Color(0xFF7B1FA2))
                        }
                        if (server.requireFollow) {
                            RequirementBadge("Follow", Color(0xFF1565C0))
                        }
                        if (server.requireAllowlist) {
                            RequirementBadge("Allowlist", Color(0xFFE65100))
                        }
                        if (server.hasAccess) {
                            RequirementBadge("\u2714 Access", Color(0xFF2E7D32))
                        }
                    }
                }

                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View details",
                    tint = PirateTheme.accentColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Lock overlay for no-access servers
        if (!server.hasAccess) {
            Box(
                modifier = Modifier
                    .matchParentSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "No access",
                    tint = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        } // Box
    }
}

@Composable
private fun RequirementBadge(label: String, color: Color) {
    Text(
        text = label,
        fontFamily = PirateTheme.fontFamily,
        fontSize = 11.sp,
        color = Color.White,
        modifier = Modifier
            .background(color.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}
