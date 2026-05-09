package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftyspiffy.burkeblackapp.R
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.BitCheer
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import com.swiftyspiffy.burkeblackapp.ui.components.DateUtils
import com.swiftyspiffy.burkeblackapp.ui.components.StatFormatter
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import kotlinx.coroutines.launch

private fun cheerImageRes(bits: Int): Int {
    return when {
        bits >= 10000 -> R.drawable.cheer_10000
        bits >= 5000 -> R.drawable.cheer_5000
        bits >= 100 -> R.drawable.cheer_100
        else -> R.drawable.cheer_1
    }
}

private fun cheerColor(bits: Int): Color {
    return when {
        bits >= 10000 -> Color(0xFFFF0000)
        bits >= 5000 -> Color(0xFF2196F3)
        bits >= 100 -> Color(0xFF9C27B0)
        else -> Color(0xFF9E9E9E)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BitsScreen(
    token: String,
    onBack: () -> Unit
) {
    var bits by remember { mutableStateOf<List<BitCheer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = ApiClient.api.fetchBits("Bearer $token")
                if (response.success && response.data != null) {
                    bits = response.data.bits
                }
            } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bits Cheered", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PirateTheme.accentColor)
            }
        } else if (bits.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No bits cheered yet", color = Color.White.copy(alpha = 0.4f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                items(bits) { cheer ->
                    BitCheerItem(cheer)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                }
            }
        }
    }
}

@Composable
private fun BitCheerItem(cheer: BitCheer) {
    val color = cheerColor(cheer.bits)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Top row: cheer icon + amount ... date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(cheerImageRes(cheer.bits)),
                    contentDescription = "Bits badge",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = StatFormatter.integer(cheer.bits),
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Text(
                text = DateUtils.formatBitDate(cheer.date),
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 12.sp
            )
        }

        // Message below
        if (cheer.message.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = cheer.message,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
    }
}
