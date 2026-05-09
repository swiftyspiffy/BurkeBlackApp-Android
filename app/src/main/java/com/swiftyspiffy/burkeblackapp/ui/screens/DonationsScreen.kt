package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.Donation
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationsScreen(
    token: String,
    onBack: () -> Unit
) {
    var donations by remember { mutableStateOf<List<Donation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = ApiClient.api.fetchDonations("Bearer $token")
                if (response.success && response.data != null) {
                    donations = response.data.donations
                }
            } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Donations", color = Color.White) },
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
        } else if (donations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No donations yet", color = Color.White.copy(alpha = 0.4f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                items(donations) { donation ->
                    DonationItem(donation)
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                }
            }
        }
    }
}

@Composable
private fun DonationItem(donation: Donation) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Top row: amount ... date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDonationAmount(donation.amount, donation.currency),
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = formatDonationDate(donation.date),
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 12.sp
            )
        }

        // Message below
        if (donation.message.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = donation.message,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 14.sp,
                maxLines = 3
            )
        }
    }
}

private fun formatDonationAmount(amount: Double, currency: String): String {
    return try {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        format.currency = Currency.getInstance(currency.uppercase())
        format.format(amount)
    } catch (e: Exception) {
        AppLogger.log("Error: ${e.message}")
        "$${String.format("%.2f", amount)}"
    }
}

private fun formatDonationDate(dateStr: String): String {
    // Try as epoch first
    val epoch = dateStr.toDoubleOrNull()
    if (epoch != null) {
        val format = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        return format.format(Date(epoch.toLong() * 1000))
    }
    // Try ISO format
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val date = isoFormat.parse(dateStr)
        val outFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        if (date != null) outFormat.format(date) else dateStr
    } catch (e: Exception) {
        AppLogger.log("Error: ${e.message}")
        dateStr
    }
}
