package com.swiftyspiffy.burkeblackapp.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.swiftyspiffy.burkeblackapp.BuildConfig
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.FeedbackBody
import com.swiftyspiffy.burkeblackapp.ui.theme.LocalPirateTheme
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

private val topics = listOf("Stream", "App", "Website", "Extension", "General")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    token: String,
    username: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTopic by remember { mutableStateOf("Stream") }
    var message by remember { mutableStateOf("") }
    val selectedImages = remember { mutableStateListOf<Uri>() }
    var includeDiagnostics by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        val remaining = 5 - selectedImages.size
        selectedImages.addAll(uris.take(remaining))
    }
    LaunchedEffect(Unit) {
        AppLogger.log("Feedback: appeared")
    }

    var isSending by remember { mutableStateOf(false) }
    var topicMenuExpanded by remember { mutableStateOf(false) }
    var previewImageUri by remember { mutableStateOf<Uri?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = PirateTheme.accentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Message in a Bottle",
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 22.sp,
                        color = PirateTheme.accentColor
                    )
                    Text(
                        text = "Send word to the ship's crew",
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {

            // From row
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PirateTheme.cardGradient, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("From", color = Color.White.copy(alpha = 0.5f), fontFamily = PirateTheme.fontFamily, fontSize = 14.sp)
                    Text(
                        username.ifBlank { "Anonymous" },
                        color = PirateTheme.accentColor,
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Topic picker
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PirateTheme.cardGradient, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Topic", color = Color.White.copy(alpha = 0.5f), fontFamily = PirateTheme.fontFamily, fontSize = 14.sp)
                    TextButton(onClick = { topicMenuExpanded = true }) {
                        Text(selectedTopic, color = PirateTheme.accentColor, fontFamily = PirateTheme.fontFamily, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("\u25BE", color = PirateTheme.accentColor, fontSize = 12.sp)
                    }
                    DropdownMenu(
                        expanded = topicMenuExpanded,
                        onDismissRequest = { topicMenuExpanded = false }
                    ) {
                        topics.forEach { topic ->
                            DropdownMenuItem(
                                text = { Text(topic, fontFamily = PirateTheme.fontFamily) },
                                onClick = {
                                    selectedTopic = topic
                                    topicMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Message
            Text(
                "Yer Message",
                color = PirateTheme.accentColor.copy(alpha = 0.7f),
                fontFamily = PirateTheme.fontFamily,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PirateTheme.accentColor,
                    unfocusedBorderColor = PirateTheme.accentColor.copy(alpha = 0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = PirateTheme.accentColor,
                    focusedContainerColor = if (LocalPirateTheme.current) Color(0xFF1C1208) else MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = if (LocalPirateTheme.current) Color(0xFF1C1208) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Attachments
            Text(
                "Attachments",
                color = PirateTheme.accentColor.copy(alpha = 0.7f),
                fontFamily = PirateTheme.fontFamily,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = selectedImages.size < 5) {
                        imagePickerLauncher.launch("image/*")
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PirateTheme.cardGradient, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PirateTheme.iconBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "Attach images",
                            tint = PirateTheme.accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        if (selectedImages.isEmpty()) "Add Images"
                        else "${selectedImages.size}/5 image${if (selectedImages.size != 1) "s" else ""} selected",
                        color = PirateTheme.accentColor,
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 16.sp
                    )
                }
            }

            // Show selected image thumbnails
            if (selectedImages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedImages.forEachIndexed { index, uri ->
                        Box(modifier = Modifier.size(80.dp)) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(uri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Image ${index + 1}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { previewImageUri = uri },
                                contentScale = ContentScale.Crop,
                                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                                error = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .size(16.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                    .clickable { selectedImages.removeAt(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Include Diagnostics
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PirateTheme.cardGradient, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Include Ship's Log",
                            color = PirateTheme.accentColor,
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 16.sp
                        )
                        Text(
                            "May include identifying details like Twitch username and IP address. These logs will be used to help fix app issues.",
                            color = Color.White.copy(alpha = 0.4f),
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Switch(
                        checked = includeDiagnostics,
                        onCheckedChange = { includeDiagnostics = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PirateTheme.accentColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit
            Button(
                onClick = {
                    if (message.isBlank() && selectedImages.isEmpty() && !includeDiagnostics) return@Button
                    isSending = true
                    scope.launch {
                        try {
                            AppLogger.log("Feedback submission: topic=$selectedTopic diagnostics=$includeDiagnostics images=${selectedImages.size}")
                            val diagnostics = if (includeDiagnostics) {
                                AppLogger.getDiagnostics(username.ifBlank { null })
                            } else null

                            val imageStrings = if (selectedImages.isNotEmpty()) {
                                selectedImages.mapNotNull { uri ->
                                    try {
                                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                                        val bitmap = ImageDecoder.decodeBitmap(source)
                                        val stream = ByteArrayOutputStream()
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream)
                                        Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                                    } catch (e: Exception) {
                                        AppLogger.log("Feedback: image compression failed: ${e.message}")
                                        null
                                    }
                                }
                            } else null

                            val body = FeedbackBody(
                                username = username.ifBlank { "Anonymous" },
                                target = selectedTopic,
                                message = message.trim(),
                                images = imageStrings,
                                diagnostics = diagnostics
                            )
                            val response = if (token.isNotBlank()) {
                                ApiClient.api.submitFeedbackNoImages("Bearer $token", body)
                            } else {
                                ApiClient.api.submitFeedbackPublic(body)
                            }
                            if (response.success) {
                                AppLogger.log("Feedback sent successfully")
                                Toast.makeText(context, "Thank you for your feedback!", Toast.LENGTH_LONG).show()
                                onBack()
                            } else {
                                AppLogger.log("Feedback send failed: ${response.error}")
                                snackbarHostState.showSnackbar(response.error ?: "Failed to send feedback")
                            }
                        } catch (e: Exception) {
                            AppLogger.log("Feedback send error: ${e.message}")
                            snackbarHostState.showSnackbar(e.message ?: "Failed to send feedback")
                        }
                        isSending = false
                    }
                },
                enabled = (message.isNotBlank() || selectedImages.isNotEmpty() || includeDiagnostics) && !isSending,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PirateTheme.accentColor,
                    disabledContainerColor = PirateTheme.accentColor.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isSending) "Sending..." else "Cast into the Sea",
                    color = Color.Black,
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            } // end inner Column
        }
    }

    // Fullscreen image preview
    previewImageUri?.let { uri ->
        Dialog(
            onDismissRequest = { previewImageUri = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { previewImageUri = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(uri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { previewImageUri = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close preview",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
