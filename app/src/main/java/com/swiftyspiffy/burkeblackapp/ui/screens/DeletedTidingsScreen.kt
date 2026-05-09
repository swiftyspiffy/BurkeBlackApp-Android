package com.swiftyspiffy.burkeblackapp.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Restore
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.NewsArticle
import com.swiftyspiffy.burkeblackapp.ui.components.DateUtils
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeletedTidingsScreen(
    token: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var articles by remember { mutableStateOf<List<NewsArticle>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val restoring = remember { mutableStateOf<Set<Int>>(emptySet()) }

    fun fetch() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val resp = ApiClient.api.fetchDeletedNewsArticles("Bearer $token")
                if (resp.success && resp.data != null) {
                    articles = resp.data.articles
                } else {
                    errorMessage = resp.error ?: "Failed to load"
                }
            } catch (e: Exception) {
                errorMessage = "Couldn't reach the ship's log"
                AppLogger.log("DeletedTidings: fetch error: ${e.message}")
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { fetch() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Deleted Tidings",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading && articles.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PirateTheme.accentColor)
                    }
                }
                errorMessage != null && articles.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                errorMessage!!,
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { fetch() },
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
                                Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Nothing in the bilge.",
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
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        items(articles, key = { it.id }) { article ->
                            DeletedArticleCard(
                                article = article,
                                isRestoring = restoring.value.contains(article.id),
                                onRestore = {
                                    val id = article.id
                                    if (restoring.value.contains(id)) return@DeletedArticleCard
                                    scope.launch {
                                        restoring.value = restoring.value + id
                                        try {
                                            val resp = ApiClient.api.restoreNewsArticle(
                                                auth = "Bearer $token",
                                                body = buildJsonObject { put("id", JsonPrimitive(id)) }
                                            )
                                            if (resp.success) {
                                                articles = articles.filterNot { it.id == id }
                                                AppLogger.log("DeletedTidings: restored $id")
                                            } else {
                                                snackbarHostState.showSnackbar(resp.error ?: "Failed to restore")
                                            }
                                        } catch (e: Exception) {
                                            AppLogger.log("DeletedTidings: restore error: ${e.message}")
                                            snackbarHostState.showSnackbar("Couldn't reach the ship's log")
                                        }
                                        restoring.value = restoring.value - id
                                    }
                                }
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
private fun DeletedArticleCard(
    article: NewsArticle,
    isRestoring: Boolean,
    onRestore: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PirateTheme.cardGradient, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = article.subject,
                color = PirateTheme.accentColor,
                fontFamily = PirateTheme.fontFamily,
                fontSize = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${article.createdByUsername} • ${DateUtils.formatRelativeTime(article.createdAt)}",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
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
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onRestore,
                    enabled = !isRestoring,
                    colors = ButtonDefaults.buttonColors(containerColor = PirateTheme.accentColor)
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.Restore,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Restore", color = Color.Black, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
