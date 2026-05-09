package com.swiftyspiffy.burkeblackapp.ui.screens

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LevelListDrawable
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView
import coil.imageLoader
import coil.request.ImageRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.NewsArticle
import com.swiftyspiffy.burkeblackapp.ui.components.DateUtils
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TidingsScreen(
    onBack: (() -> Unit)? = null,
    initialArticleId: Int? = null,
    token: String? = null,
    onNewTiding: (() -> Unit)? = null,
    onShowDeleted: (() -> Unit)? = null,
    onEditTiding: ((NewsArticle) -> Unit)? = null,
    refreshKey: Int = 0
) {
    val scope = rememberCoroutineScope()
    var articles by remember { mutableStateOf<List<NewsArticle>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedArticle by remember { mutableStateOf<NewsArticle?>(null) }
    var canCreateNews by remember { mutableStateOf(false) }

    /**
     * Resolve the user's news permission first; mods/Burke fetch via /mod/news
     * (every article, every platform). Everyone else stays on the public
     * /news?platform=android view.
     */
    fun fetchAll() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val isMod = if (token.isNullOrBlank()) false else {
                    val perms = ApiClient.api.fetchModPermissions("Bearer $token")
                    perms.success && perms.data?.news == "1"
                }
                canCreateNews = isMod

                val response = if (isMod && !token.isNullOrBlank()) {
                    ApiClient.api.fetchModNews("Bearer $token")
                } else {
                    ApiClient.api.fetchNews()
                }
                if (response.success && response.data != null) {
                    articles = response.data.articles
                    AppLogger.log("Tidings: loaded ${articles.size} articles (mod=$isMod)")
                    if (initialArticleId != null && selectedArticle == null) {
                        selectedArticle = articles.find { it.id == initialArticleId }
                    }
                } else {
                    errorMessage = response.error ?: "Failed to load"
                }
            } catch (e: Exception) {
                errorMessage = "Couldn't reach the ship's log"
                AppLogger.log("Tidings: fetch error: ${e.message}")
            }
            isLoading = false
        }
    }

    LaunchedEffect(refreshKey, token) { fetchAll() }

    // Detail view
    if (selectedArticle != null) {
        TidingsDetailScreen(
            article = selectedArticle!!,
            canDelete = canCreateNews && !token.isNullOrBlank(),
            onBack = { selectedArticle = null },
            onDeleted = {
                selectedArticle = null
                fetchAll()
            },
            onEdit = onEditTiding?.let { edit ->
                { article ->
                    selectedArticle = null
                    edit(article)
                }
            },
            token = token
        )
        return
    }

    val canRestore = canCreateNews && onShowDeleted != null
    val titleLongPressModifier: Modifier = rememberHoldToTriggerModifier(
        holdMs = 5000L,
        enabled = canRestore
    ) { onShowDeleted?.invoke() }

    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = {
                        Text(
                            "Tidings",
                            fontFamily = PirateTheme.fontFamily,
                            color = PirateTheme.accentColor,
                            modifier = titleLongPressModifier
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        },
        floatingActionButton = {
            if (canCreateNews && onNewTiding != null) {
                FloatingActionButton(
                    onClick = onNewTiding,
                    containerColor = PirateTheme.accentColor,
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Tiding")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { fetchAll() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading && articles.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = PirateTheme.accentColor)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Fetchin' the latest dispatches...",
                                fontFamily = PirateTheme.fontFamily,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                errorMessage != null && articles.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                errorMessage!!,
                                fontFamily = PirateTheme.fontFamily,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { fetchAll() },
                                colors = ButtonDefaults.buttonColors(containerColor = PirateTheme.accentColor)
                            ) {
                                Text("Try Again", color = Color.Black, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                articles.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Newspaper,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No tidings to report, Captain",
                                fontFamily = PirateTheme.fontFamily,
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (onBack == null) {
                            // Tab header
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = titleLongPressModifier
                                ) {
                                    Icon(Icons.Default.Newspaper, contentDescription = null, tint = PirateTheme.accentColor, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tidings", fontFamily = PirateTheme.fontFamily, fontSize = 28.sp, color = PirateTheme.accentColor)
                                }
                                Text(
                                    "News from across the seven seas",
                                    fontFamily = PirateTheme.fontFamily,
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            item { Spacer(modifier = Modifier.height(4.dp)) }
                        }
                        items(articles) { article ->
                            ArticleCard(
                                article = article,
                                showPlatformBadges = canCreateNews,
                                onClick = { selectedArticle = article }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArticleCard(
    article: NewsArticle,
    showPlatformBadges: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    PirateTheme.cardGradient,
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = article.subject,
                    color = PirateTheme.accentColor,
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (showPlatformBadges) {
                    Spacer(modifier = Modifier.width(8.dp))
                    PlatformBadges(article)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(
                    text = article.createdByUsername,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                Text(
                    text = " \u2022 ",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 12.sp
                )
                Text(
                    text = DateUtils.formatRelativeTime(article.createdAt),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
            // Preview of body (strip markdown)
            val preview = article.body
                .replace(Regex("#+ "), "")
                .replace(Regex("\\*\\*(.+?)\\*\\*"), "$1")
                .replace(Regex("\\[(.+?)\\]\\(.+?\\)"), "$1")
                .replace(Regex("!\\[.*?\\]\\(.*?\\)"), "")
                .replace("\n", " ")
                .take(120)
            if (preview.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = preview + if (article.body.length > 120) "..." else "",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PlatformBadges(article: NewsArticle) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PlatformBadge(Icons.Default.PhoneIphone, "iOS", article.isVisibleOnIos == 1)
        PlatformBadge(Icons.Default.PhoneAndroid, "Android", article.isVisibleOnAndroid == 1)
        PlatformBadge(Icons.Default.Public, "Website", article.isVisibleOnWebsite == 1)
    }
}

@Composable
private fun PlatformBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    on: Boolean
) {
    Icon(
        imageVector = icon,
        contentDescription = if (on) "$description visible" else "$description hidden",
        tint = if (on) PirateTheme.accentColor else Color.White.copy(alpha = 0.18f),
        modifier = Modifier.size(16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TidingsDetailScreen(
    article: NewsArticle,
    canDelete: Boolean,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    onEdit: ((NewsArticle) -> Unit)?,
    token: String?
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmDeleteOpen by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (canDelete && onEdit != null) {
                        IconButton(
                            onClick = { onEdit(article) },
                            enabled = !isDeleting
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = PirateTheme.accentColor
                            )
                        }
                    }
                    if (canDelete) {
                        IconButton(
                            onClick = { confirmDeleteOpen = true },
                            enabled = !isDeleting
                        ) {
                            if (isDeleting) {
                                CircularProgressIndicator(
                                    color = PirateTheme.accentColor,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFFF6B6B)
                                )
                            }
                        }
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
            Text(
                text = article.subject,
                fontFamily = PirateTheme.fontFamily,
                fontSize = 24.sp,
                color = PirateTheme.accentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "By ${article.createdByUsername} \u2022 ${DateUtils.formatRelativeTime(article.createdAt)}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Render markdown as HTML in a TextView
            val htmlContent = markdownToHtml(article.body)
            val goldColor = PirateTheme.accentColor.toArgb()
            val whiteColor = Color.White.copy(alpha = 0.8f).toArgb()

            AndroidView(
                factory = { context ->
                    TextView(context).apply {
                        setTextColor(whiteColor)
                        setLinkTextColor(goldColor)
                        textSize = 15f
                        movementMethod = LinkMovementMethod.getInstance()

                        val tv = this
                        val imageGetter = Html.ImageGetter { source ->
                            val drawable = LevelListDrawable()
                            val loader = context.imageLoader
                            val request = ImageRequest.Builder(context)
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
                                    tv.text = tv.text // trigger re-layout
                                    tv.invalidate()
                                }
                                .build()
                            loader.enqueue(request)
                            drawable
                        }
                        text = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_COMPACT, imageGetter, null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (confirmDeleteOpen) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) confirmDeleteOpen = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    "Delete this tiding?",
                    color = PirateTheme.accentColor,
                    fontFamily = PirateTheme.fontFamily
                )
            },
            text = {
                Text(
                    "It will be hidden from users immediately. You can restore it later via the Tidings deleted view.",
                    color = Color.White.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !isDeleting && !token.isNullOrBlank(),
                    onClick = {
                        val t = token ?: return@TextButton
                        scope.launch {
                            isDeleting = true
                            try {
                                val resp = ApiClient.api.deleteNewsArticle(
                                    auth = "Bearer $t",
                                    body = buildJsonObject { put("id", JsonPrimitive(article.id)) }
                                )
                                if (resp.success) {
                                    AppLogger.log("Tidings: deleted article ${article.id}")
                                    confirmDeleteOpen = false
                                    onDeleted()
                                } else {
                                    snackbarHostState.showSnackbar(resp.error ?: "Failed to delete")
                                }
                            } catch (e: Exception) {
                                AppLogger.log("Tidings: delete error: ${e.message}")
                                snackbarHostState.showSnackbar("Couldn't reach the ship's log")
                            }
                            isDeleting = false
                        }
                    }
                ) {
                    Text("Delete", color = Color(0xFFFF6B6B), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = { confirmDeleteOpen = false }
                ) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}

/**
 * Returns a Modifier that fires [onTriggered] when the user keeps a finger
 * pressed for at least [holdMs]. Releasing the finger before the timer fires
 * cancels it.
 */
@Composable
private fun rememberHoldToTriggerModifier(
    holdMs: Long,
    enabled: Boolean,
    onTriggered: () -> Unit
): Modifier {
    if (!enabled) return Modifier
    var pressed by remember { mutableStateOf(false) }
    LaunchedEffect(pressed) {
        if (pressed) {
            delay(holdMs)
            if (pressed) onTriggered()
        }
    }
    return Modifier.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            pressed = true
            try {
                while (true) {
                    val event = awaitPointerEvent()
                    if (event.changes.all { !it.pressed }) break
                }
            } finally {
                pressed = false
            }
        }
    }
}

private fun markdownToHtml(markdown: String): String {
    var html = markdown
    // Images
    html = html.replace(Regex("!\\[([^]]*)]\\(([^)]+)\\)"), "<br><img src=\"$2\"><br>")
    // Bold
    html = html.replace(Regex("\\*\\*(.+?)\\*\\*"), "<b>$1</b>")
    // Italic
    html = html.replace(Regex("\\*(.+?)\\*"), "<i>$1</i>")
    // Links
    html = html.replace(Regex("\\[([^]]+)]\\(([^)]+)\\)"), "<a href=\"$2\">$1</a>")
    // Headings
    html = html.replace(Regex("^### (.+)$", RegexOption.MULTILINE), "<br><b>$1</b><br>")
    html = html.replace(Regex("^## (.+)$", RegexOption.MULTILINE), "<br><big><b>$1</b></big><br>")
    html = html.replace(Regex("^# (.+)$", RegexOption.MULTILINE), "<br><big><big><b>$1</b></big></big><br>")
    // Bullet lists
    html = html.replace(Regex("^- (.+)$", RegexOption.MULTILINE), "\u2022 $1<br>")
    // Numbered lists
    html = html.replace(Regex("^(\\d+)\\. (.+)$", RegexOption.MULTILINE), "$1. $2<br>")
    // Newlines
    html = html.replace("\n\n", "<br><br>")
    html = html.replace("\n", "<br>")
    return html
}

