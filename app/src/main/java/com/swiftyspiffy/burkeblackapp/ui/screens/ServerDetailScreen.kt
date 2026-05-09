package com.swiftyspiffy.burkeblackapp.ui.screens

import java.text.SimpleDateFormat
import java.util.Locale
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swiftyspiffy.burkeblackapp.data.models.CommunityServer
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ServerDetailScreen(
    server: CommunityServer,
    onBack: () -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    LaunchedEffect(Unit) { AppLogger.log("ServerDetail: appeared") }
    val context = LocalContext.current
    val bgColor = MaterialTheme.colorScheme.background

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Banner with overlaid back button
        Box {
            if (server.bannerUrl != null) {
                AsyncImage(
                    model = server.bannerUrl,
                    contentDescription = server.serverName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop
                )
                // Bottom fade
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Transparent,
                                    bgColor.copy(alpha = 0.6f),
                                    bgColor
                                )
                            )
                        )
                )
            }

            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(start = 4.dp, top = 16.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Game badge
            Text(
                text = server.gameName,
                fontFamily = PirateTheme.fontFamily,
                fontSize = 14.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 16.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Server name
            Text(
                text = server.serverName,
                fontFamily = PirateTheme.fontFamily,
                fontSize = 28.sp,
                color = PirateTheme.accentColor
            )

            // Created date
            if (!server.createdAt.isNullOrEmpty()) {
                val formatted = try {
                    val parsed = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(server.createdAt)
                    parsed?.let { SimpleDateFormat("MMMM d, yyyy", Locale.US).format(it) }
                } catch (_: Exception) { null }
                if (formatted != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Est. $formatted",
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.35f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Requirement badges
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (server.requireSub) {
                    DetailBadge("Subscriber Required", Color(0xFF7B1FA2))
                }
                if (server.requireFollow) {
                    DetailBadge("Follower Required", Color(0xFF1565C0))
                }
                if (server.requireAllowlist) {
                    DetailBadge("Allowlist Required", Color(0xFFE65100))
                }
            }

            // Server info
            if (!server.info.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Ship's Log",
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 18.sp,
                            color = PirateTheme.accentColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = server.info,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.75f),
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Access section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (server.hasAccess) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Access granted header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFF1B5E20).copy(alpha = 0.2f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ye Have Access, Pirate!",
                                fontFamily = PirateTheme.fontFamily,
                                fontSize = 18.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }

                        // Connection details
                        if (server.ipAddress != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Server Address",
                                fontFamily = PirateTheme.fontFamily,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            CopyableField(
                                value = server.ipAddress,
                                context = context
                            )
                        }

                        if (server.password != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Password",
                                fontFamily = PirateTheme.fontFamily,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            CopyableField(
                                value = server.password,
                                context = context
                            )
                        }
                    }
                } else {
                    // No access
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = PirateTheme.accentColor.copy(alpha = 0.3f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Access Denied",
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 22.sp,
                            color = PirateTheme.accentColor.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ye haven't earned yer sea legs yet, pirate! Meet the requirements above to board this vessel.",
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.35f),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    } // PullToRefreshBox
}

@Composable
private fun DetailBadge(label: String, color: Color) {
    Text(
        text = label,
        fontFamily = PirateTheme.fontFamily,
        fontSize = 13.sp,
        color = Color.White,
        modifier = Modifier
            .background(color.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    )
}

@Composable
private fun CopyableField(value: String, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(alpha = 0.05f),
                RoundedCornerShape(10.dp)
            )
            .padding(start = 14.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = value,
            fontFamily = PirateTheme.fontFamily,
            fontSize = 18.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Server detail", value))
                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
        ) {
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = PirateTheme.accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
