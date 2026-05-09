package com.swiftyspiffy.burkeblackapp.ui.screens

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LevelListDrawable
import android.net.Uri
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.imageLoader
import coil.request.ImageRequest
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.CreateNewsBody
import com.swiftyspiffy.burkeblackapp.data.models.NewsArticle
import com.swiftyspiffy.burkeblackapp.data.models.UpdateNewsBody
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTidingScreen(
    token: String,
    onBack: () -> Unit,
    onCreated: () -> Unit,
    existing: NewsArticle? = null,
    existingVisibleIos: Boolean = true,
    existingVisibleAndroid: Boolean = true,
    existingVisibleWebsite: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isEdit = existing != null

    var title by remember { mutableStateOf(existing?.subject ?: "") }
    var body by remember { mutableStateOf(TextFieldValue(existing?.body ?: "")) }
    var sendPush by remember { mutableStateOf(true) }
    var visibleIos by remember { mutableStateOf(existingVisibleIos) }
    var visibleAndroid by remember { mutableStateOf(existingVisibleAndroid) }
    var visibleWebsite by remember { mutableStateOf(existingVisibleWebsite) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Edit, 1 = Preview
    var isSubmitting by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var linkDialogOpen by remember { mutableStateOf(false) }
    val anyPlatformVisible = visibleIos || visibleAndroid || visibleWebsite
    val pushReachable = visibleIos || visibleAndroid

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isUploadingImage = true
            val result = uploadImage(context, token, uri)
            isUploadingImage = false
            if (result == null) {
                snackbarHostState.showSnackbar("Image upload failed")
            } else {
                body = insertAtCursor(body, "![image](${result})")
            }
        }
    }

    fun submit() {
        if (title.isBlank() || body.text.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("Title and body are required") }
            return
        }
        if (!anyPlatformVisible) {
            scope.launch { snackbarHostState.showSnackbar("Pick at least one platform to publish to") }
            return
        }
        scope.launch {
            isSubmitting = true
            try {
                val resp = if (existing != null) {
                    ApiClient.api.updateNewsArticle(
                        auth = "Bearer $token",
                        body = UpdateNewsBody(
                            id = existing.id,
                            subject = title.trim(),
                            body = body.text,
                            isVisibleOnIos = if (visibleIos) 1 else 0,
                            isVisibleOnAndroid = if (visibleAndroid) 1 else 0,
                            isVisibleOnWebsite = if (visibleWebsite) 1 else 0
                        )
                    )
                } else {
                    ApiClient.api.createNewsArticle(
                        auth = "Bearer $token",
                        body = CreateNewsBody(
                            subject = title.trim(),
                            body = body.text,
                            isVisibleOnIos = if (visibleIos) 1 else 0,
                            isVisibleOnAndroid = if (visibleAndroid) 1 else 0,
                            isVisibleOnWebsite = if (visibleWebsite) 1 else 0,
                            sendPush = if (sendPush && pushReachable) 1 else 0
                        )
                    )
                }
                if (resp.success) {
                    AppLogger.log(
                        if (existing != null) "Tidings: updated article id=${existing.id}"
                        else "Tidings: created article push=$sendPush"
                    )
                    onCreated()
                } else {
                    snackbarHostState.showSnackbar(resp.error ?: "Save failed")
                }
            } catch (e: Exception) {
                AppLogger.log("Tidings: save error: ${e.message}")
                snackbarHostState.showSnackbar("Couldn't reach the ship's log")
            }
            isSubmitting = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) "Edit Tiding" else "New Tiding",
                        fontFamily = PirateTheme.fontFamily,
                        color = PirateTheme.accentColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { submit() },
                        enabled = !isSubmitting && title.isNotBlank() && body.text.isNotBlank()
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                color = PirateTheme.accentColor,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.Send,
                                contentDescription = "Post",
                                tint = if (title.isNotBlank() && body.text.isNotBlank())
                                    PirateTheme.accentColor
                                else
                                    Color.White.copy(alpha = 0.3f)
                            )
                        }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Title field
            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 255) title = it },
                label = { Text("Title", color = Color.White.copy(alpha = 0.6f)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = pirateFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Edit / Preview tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = PirateTheme.accentColor
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Edit", fontFamily = PirateTheme.fontFamily) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Preview", fontFamily = PirateTheme.fontFamily) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedTab == 0) {
                // Markdown helper toolbar
                MarkdownToolbar(
                    isUploadingImage = isUploadingImage,
                    onBold = { body = wrapSelection(body, "**", "**", placeholder = "bold") },
                    onItalic = { body = wrapSelection(body, "*", "*", placeholder = "italic") },
                    onHeading = { body = prefixLines(body, "## ") },
                    onList = { body = prefixLines(body, "- ") },
                    onLink = { linkDialogOpen = true },
                    onImage = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Body editor
                OutlinedTextField(
                    value = body,
                    onValueChange = { if (it.text.length <= 65000) body = it },
                    label = { Text("Body (markdown supported)", color = Color.White.copy(alpha = 0.6f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp),
                    colors = pirateFieldColors()
                )

                Text(
                    "Tip: Tap the toolbar buttons above to format. Use the image button to add a photo.",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            } else {
                MarkdownPreview(body.text)
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(12.dp))

            // Visibility checkboxes
            Text(
                "Visibility",
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Choose which platforms see this tiding.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VisibilityCheckbox("iOS", visibleIos) { visibleIos = it }
                VisibilityCheckbox("Android", visibleAndroid) { visibleAndroid = it }
                VisibilityCheckbox("Website", visibleWebsite) { visibleWebsite = it }
            }
            if (sendPush && !pushReachable) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Push needs iOS or Android visibility to reach anyone.",
                    color = Color(0xFFFFB347),
                    fontSize = 12.sp
                )
            }

            if (!isEdit) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                // Push notification toggle (creation only — edits never re-push)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Send push notification",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Notifies users with Channel Tidings notifications enabled.",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = sendPush,
                        onCheckedChange = { sendPush = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = PirateTheme.accentColor,
                            uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.15f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { submit() },
                enabled = !isSubmitting && title.isNotBlank() && body.text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PirateTheme.accentColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        if (isEdit) "Save Changes" else "Post Tiding",
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (linkDialogOpen) {
        LinkInsertDialog(
            currentSelection = body.text.substring(
                body.selection.min.coerceAtLeast(0).coerceAtMost(body.text.length),
                body.selection.max.coerceAtLeast(0).coerceAtMost(body.text.length)
            ),
            onDismiss = { linkDialogOpen = false },
            onConfirm = { displayText, url ->
                body = insertLink(body, displayText, url)
                linkDialogOpen = false
            }
        )
    }
}

@Composable
private fun MarkdownToolbar(
    isUploadingImage: Boolean,
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onHeading: () -> Unit,
    onList: () -> Unit,
    onLink: () -> Unit,
    onImage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToolbarButton(Icons.Default.FormatBold, "Bold", onBold)
        ToolbarButton(Icons.Default.FormatItalic, "Italic", onItalic)
        ToolbarButton(Icons.Default.Title, "Heading", onHeading)
        ToolbarButton(Icons.AutoMirrored.Filled.FormatListBulleted, "List", onList)
        ToolbarButton(Icons.Default.Link, "Link", onLink)
        if (isUploadingImage) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PirateTheme.accentColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            ToolbarButton(Icons.Default.Image, "Image", onImage)
        }
    }
}

@Composable
private fun VisibilityCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = PirateTheme.accentColor,
                uncheckedColor = Color.White.copy(alpha = 0.4f),
                checkmarkColor = Color.Black
            )
        )
        Text(label, color = Color.White)
    }
}

@Composable
private fun ToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
        Icon(icon, contentDescription = description, tint = PirateTheme.accentColor)
    }
}

@Composable
private fun LinkInsertDialog(
    currentSelection: String,
    onDismiss: () -> Unit,
    onConfirm: (displayText: String, url: String) -> Unit
) {
    var text by remember { mutableStateOf(currentSelection) }
    var url by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Insert Link", color = PirateTheme.accentColor, fontFamily = PirateTheme.fontFamily) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Display text", color = Color.White.copy(alpha = 0.6f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = pirateFieldColors()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL (https://...)", color = Color.White.copy(alpha = 0.6f)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = pirateFieldColors()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val finalText = text.ifBlank { url }
                    if (url.isNotBlank()) onConfirm(finalText, url)
                },
                enabled = url.isNotBlank()
            ) {
                Text("Insert", color = PirateTheme.accentColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}

@Composable
private fun MarkdownPreview(markdown: String) {
    if (markdown.isBlank()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Nothing to preview yet.",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 14.sp
            )
        }
        return
    }
    val htmlContent = markdownToHtml(markdown)
    val goldColor = PirateTheme.accentColor.toArgb()
    val whiteColor = Color.White.copy(alpha = 0.85f).toArgb()

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        factory = { ctx ->
            TextView(ctx).apply {
                setTextColor(whiteColor)
                setLinkTextColor(goldColor)
                textSize = 15f
                movementMethod = LinkMovementMethod.getInstance()
            }
        },
        update = { tv ->
            val imageGetter = Html.ImageGetter { source ->
                val drawable = LevelListDrawable()
                val loader = tv.context.imageLoader
                val request = ImageRequest.Builder(tv.context)
                    .data(source)
                    .target { result ->
                        val bitmap = (result as BitmapDrawable)
                        val width = tv.width.takeIf { it > 0 } ?: bitmap.intrinsicWidth
                        val ratio = width.toFloat() / bitmap.intrinsicWidth
                        val height = (bitmap.intrinsicHeight * ratio).toInt()
                        bitmap.setBounds(0, 0, width, height)
                        drawable.addLevel(0, 0, bitmap)
                        drawable.setBounds(0, 0, width, height)
                        drawable.level = 0
                        tv.text = tv.text
                        tv.invalidate()
                    }
                    .build()
                loader.enqueue(request)
                drawable
            }
            tv.text = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT, imageGetter, null)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun pirateFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = PirateTheme.accentColor,
    focusedBorderColor = PirateTheme.accentColor,
    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
    focusedLabelColor = PirateTheme.accentColor,
    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
)

// --- Markdown helpers ---

private fun wrapSelection(
    value: TextFieldValue,
    prefix: String,
    suffix: String,
    placeholder: String
): TextFieldValue {
    val sel = value.selection
    val text = value.text
    return if (sel.collapsed) {
        val insert = "$prefix$placeholder$suffix"
        val newText = text.substring(0, sel.start) + insert + text.substring(sel.start)
        val cursorStart = sel.start + prefix.length
        val cursorEnd = cursorStart + placeholder.length
        value.copy(text = newText, selection = TextRange(cursorStart, cursorEnd))
    } else {
        val selected = text.substring(sel.start, sel.end)
        val newText = text.substring(0, sel.start) + prefix + selected + suffix + text.substring(sel.end)
        val newEnd = sel.end + prefix.length + suffix.length
        value.copy(text = newText, selection = TextRange(newEnd, newEnd))
    }
}

private fun prefixLines(value: TextFieldValue, prefix: String): TextFieldValue {
    val text = value.text
    val sel = value.selection
    val lineStart = text.lastIndexOf('\n', (sel.start - 1).coerceAtLeast(0)).let {
        if (it < 0) 0 else it + 1
    }
    // Don't double-prefix
    val tail = text.substring(lineStart)
    if (tail.startsWith(prefix)) return value
    val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
    val newCursor = sel.end + prefix.length
    return value.copy(text = newText, selection = TextRange(newCursor, newCursor))
}

private fun insertAtCursor(value: TextFieldValue, insert: String): TextFieldValue {
    val sel = value.selection
    val text = value.text
    // If we're not at line start and previous char isn't newline, prepend newline
    val needLeadingNewline = sel.start > 0 && text[sel.start - 1] != '\n'
    val toInsert = (if (needLeadingNewline) "\n" else "") + insert + "\n"
    val newText = text.substring(0, sel.start) + toInsert + text.substring(sel.end)
    val newCursor = sel.start + toInsert.length
    return value.copy(text = newText, selection = TextRange(newCursor, newCursor))
}

private fun insertLink(value: TextFieldValue, displayText: String, url: String): TextFieldValue {
    val sel = value.selection
    val text = value.text
    val markdown = "[$displayText]($url)"
    val newText = text.substring(0, sel.start) + markdown + text.substring(sel.end)
    val newCursor = sel.start + markdown.length
    return value.copy(text = newText, selection = TextRange(newCursor, newCursor))
}

// --- Image upload ---

private suspend fun uploadImage(
    context: android.content.Context,
    token: String,
    uri: Uri
): String? = withContext(Dispatchers.IO) {
    try {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri) ?: "image/jpeg"
        val ext = when (mime) {
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return@withContext null
        if (bytes.size > 5 * 1024 * 1024) {
            AppLogger.log("Tidings: image too large (${bytes.size} bytes)")
            return@withContext null
        }
        val requestBody = bytes.toRequestBody(mime.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("image", "upload.$ext", requestBody)
        val resp = ApiClient.api.uploadNewsImage(auth = "Bearer $token", image = part)
        if (resp.success) resp.data?.url else {
            AppLogger.log("Tidings: image upload api error: ${resp.error}")
            null
        }
    } catch (e: Exception) {
        AppLogger.log("Tidings: image upload threw: ${e.message}")
        null
    }
}

// Markdown → HTML renderer (mirrors TidingsScreen's renderer so preview matches detail view)
private fun markdownToHtml(markdown: String): String {
    var html = markdown
    html = html.replace(Regex("!\\[([^]]*)]\\(([^)]+)\\)"), "<br><img src=\"$2\"><br>")
    html = html.replace(Regex("\\*\\*(.+?)\\*\\*"), "<b>$1</b>")
    html = html.replace(Regex("\\*(.+?)\\*"), "<i>$1</i>")
    html = html.replace(Regex("\\[([^]]+)]\\(([^)]+)\\)"), "<a href=\"$2\">$1</a>")
    html = html.replace(Regex("^### (.+)$", RegexOption.MULTILINE), "<br><b>$1</b><br>")
    html = html.replace(Regex("^## (.+)$", RegexOption.MULTILINE), "<br><big><b>$1</b></big><br>")
    html = html.replace(Regex("^# (.+)$", RegexOption.MULTILINE), "<br><big><big><b>$1</b></big></big><br>")
    html = html.replace(Regex("^- (.+)$", RegexOption.MULTILINE), "• $1<br>")
    html = html.replace(Regex("^(\\d+)\\. (.+)$", RegexOption.MULTILINE), "$1. $2<br>")
    html = html.replace("\n\n", "<br><br>")
    html = html.replace("\n", "<br>")
    return html
}
