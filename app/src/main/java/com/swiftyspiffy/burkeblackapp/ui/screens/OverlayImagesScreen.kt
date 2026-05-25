package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoodBad
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swiftyspiffy.burkeblackapp.data.models.OverlayImage
import com.swiftyspiffy.burkeblackapp.data.models.KlipyGifResult
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayImagesScreen(
    overlayImagesVM: OverlayImagesViewModel,
    klipyVM: KlipyViewModel,
    onBack: () -> Unit,
    onOverlaySelected: (PositionerData) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        overlayImagesVM.loadImages()
        klipyVM.loadGifSettings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Images & GIFs", fontFamily = PirateTheme.fontFamily) },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PirateTheme.accentColor
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Library") },
                    selectedContentColor = PirateTheme.accentColor,
                    unselectedContentColor = Color.White.copy(alpha = 0.5f)
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("GIF") },
                    selectedContentColor = PirateTheme.accentColor,
                    unselectedContentColor = Color.White.copy(alpha = 0.5f)
                )
            }

            when (selectedTab) {
                0 -> LibraryTab(overlayImagesVM, onOverlaySelected)
                1 -> KlipyTab(klipyVM, onOverlaySelected)
            }
        }
    }
}

@Composable
private fun LibraryTab(
    viewModel: OverlayImagesViewModel,
    onImageTap: (PositionerData) -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val groupedImages by viewModel.groupedImages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Category filter chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == "All",
                    onClick = { viewModel.selectCategory("All") },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PirateTheme.accentColor,
                        selectedLabelColor = Color.Black
                    )
                )
            }
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat.name,
                    onClick = { viewModel.selectCategory(cat.name) },
                    label = { Text(cat.name) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PirateTheme.accentColor,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PirateTheme.accentColor)
            }
        } else if (groupedImages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No images available", color = Color.White.copy(alpha = 0.5f))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedImages.forEach { group ->
                    item(span = { GridItemSpan(3) }) {
                        Text(
                            group.category.name,
                            color = PirateTheme.accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                        )
                    }
                    items(group.images, key = { it.id }) { image ->
                        ImageCell(image) {
                            val modes = viewModel.modesForImage(image)
                            val data = PositionerData(
                                imageURL = image.thumbnailUrl,
                                isGif = false,
                                modes = modes,
                                name = image.name,
                                imageId = image.id,
                                gifToken = null,
                                bounceCount = viewModel.bounceCountForImage(image)
                            )
                            onImageTap(data)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageCell(image: OverlayImage, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AsyncImage(
                model = image.thumbnailUrl,
                contentDescription = image.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun KlipyTab(
    viewModel: KlipyViewModel,
    onGifTap: (PositionerData) -> Unit
) {
    val searchText by viewModel.searchText.collectAsState()
    val results by viewModel.results.collectAsState()
    val decryptedURLs by viewModel.decryptedURLs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isShowingResults by viewModel.isShowingResults.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val searchError by viewModel.searchError.collectAsState()
    val gifCredits by viewModel.gifCredits.collectAsState()
    val gridState = rememberLazyGridState()

    // Infinite scroll trigger
    LaunchedEffect(gridState) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= totalItems - 4
        }.collect { shouldLoad ->
            if (shouldLoad && !isLoading && hasMore && isShowingResults) {
                viewModel.loadMore()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchText,
            onValueChange = { viewModel.updateSearchText(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search KLIPY") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
            },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.White.copy(alpha = 0.5f))
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.search() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PirateTheme.accentColor,
                cursorColor = PirateTheme.accentColor,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = Color.White.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (isShowingResults) {
            // Search results
            if (results.isEmpty() && !isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (searchError != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Search failed", color = Color(0xFFEF5350))
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.search() }) {
                                Text("Retry", color = PirateTheme.accentColor)
                            }
                        }
                    } else {
                        Text("No results found", color = Color.White.copy(alpha = 0.5f))
                    }
                }
            } else {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(results, key = { it.token }) { gif ->
                        GifCell(gif, decryptedURLs[gif.token]) {
                            val credits = gifCredits ?: emptyMap()
                            val data = PositionerData(
                                imageURL = decryptedURLs[gif.token],
                                isGif = true,
                                modes = listOf(
                                    ModeDimension("large", 400, 400, credits["large"] ?: 3),
                                    ModeDimension("medium", 300, 300, credits["medium"] ?: 2),
                                    ModeDimension("small", 150, 150, credits["small"] ?: 1),
                                    ModeDimension("bounce", 100, 100, credits["bounce"] ?: 3)
                                ),
                                name = gif.title,
                                imageId = null,
                                gifToken = gif.token,
                                bounceCount = 3
                            )
                            onGifTap(data)
                        }
                    }

                    if (isLoading) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = PirateTheme.accentColor, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }

        } else {
            // Default category grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(KlipyViewModel.defaultCategories) { category ->
                    CategoryChip(category) {
                        viewModel.searchByCategory(category)
                    }
                }
            }
        }

        KlipyAttribution()
    }
}

@Composable
private fun GifCell(gif: KlipyGifResult, previewUrl: String?, onClick: () -> Unit) {
    Card(
        modifier = Modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(8.dp),
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (previewUrl != null) {
                AsyncImage(
                    model = previewUrl,
                    contentDescription = gif.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                CircularProgressIndicator(
                    color = PirateTheme.accentColor,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(name: String, onClick: () -> Unit) {
    val icon = categoryIcon(name)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = name, tint = PirateTheme.accentColor, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                name,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun KlipyAttribution() {
    val uriHandler = LocalUriHandler.current
    val annotatedText = buildAnnotatedString {
        append("GIF search provided by ")
        pushStringAnnotation(tag = "URL", annotation = "https://klipy.com")
        withStyle(SpanStyle(color = PirateTheme.accentColor, textDecoration = TextDecoration.Underline)) {
            append("KLIPY")
        }
        pop()
    }
    ClickableText(
        text = annotatedText,
        style = androidx.compose.ui.text.TextStyle(
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { uriHandler.openUri(it.item) }
        }
    )
}

private fun categoryIcon(name: String): ImageVector = when (name) {
    "Trending" -> Icons.Default.TrendingUp
    "Reactions" -> Icons.Default.EmojiEmotions
    "Memes" -> Icons.Default.SentimentVerySatisfied
    "Funny" -> Icons.Default.SentimentVerySatisfied
    "Anime" -> Icons.Default.AutoAwesome
    "Gaming" -> Icons.Default.SportsEsports
    "Love" -> Icons.Default.Favorite
    "Sad" -> Icons.Default.SentimentDissatisfied
    "Happy" -> Icons.Default.SentimentSatisfied
    "Angry" -> Icons.Default.MoodBad
    "Dance" -> Icons.Default.MusicNote
    "Celebrate" -> Icons.Default.Celebration
    else -> Icons.Default.Search
}
