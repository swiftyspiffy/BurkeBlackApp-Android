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
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swiftyspiffy.burkeblackapp.R
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.TikTokItem
import com.swiftyspiffy.burkeblackapp.data.models.TwitterPost
import com.swiftyspiffy.burkeblackapp.data.models.YouTubeItem
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.ui.theme.TwitchPurple

private data class SocialLink(
    val name: String,
    val url: String,
    val color: Color,
    val iconRes: Int
)

private val socialLinks = listOf(
    SocialLink("Twitch", "https://twitch.tv/burkeblack", TwitchPurple, R.drawable.ic_twitch),
    SocialLink("YouTube", "https://youtube.com/@BurkeBlack", Color(0xFFFF0000), R.drawable.ic_youtube),
    SocialLink("X", "https://twitter.com/burkeblack", Color(0xFFFFFFFF), R.drawable.ic_x),
    SocialLink("Instagram", "https://www.instagram.com/burkeblack/", Color(0xFFE1306C), R.drawable.ic_instagram),
    SocialLink("TikTok", "https://tiktok.com/@burkeblack", Color(0xFFFFFFFF), R.drawable.ic_tiktok),
    SocialLink("Discord", "https://discord.com/channels/90892452679933952/973441118203166780", Color(0xFF5865F2), R.drawable.ic_discord),
    SocialLink("Website", "https://burkeblack.tv", Color(0xFF4CAF50), R.drawable.ic_globe),
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SocialsScreen() {
    LaunchedEffect(Unit) { AppLogger.log("Ports: appeared") }
    val uriHandler = LocalUriHandler.current

    // YouTube data
    var videos by remember { mutableStateOf<List<YouTubeItem>>(emptyList()) }
    var shorts by remember { mutableStateOf<List<YouTubeItem>>(emptyList()) }
    var videosLoading by remember { mutableStateOf(true) }
    var shortsLoading by remember { mutableStateOf(true) }
    var videosError by remember { mutableStateOf(false) }
    var shortsError by remember { mutableStateOf(false) }

    // TikTok data
    var tiktoks by remember { mutableStateOf<List<TikTokItem>>(emptyList()) }
    var tiktoksLoading by remember { mutableStateOf(true) }
    var tiktoksError by remember { mutableStateOf(false) }

    // Twitter / X data
    var tweets by remember { mutableStateOf<List<TwitterPost>>(emptyList()) }
    var tweetsLoading by remember { mutableStateOf(true) }
    var tweetsError by remember { mutableStateOf(false) }

    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        videosLoading = true
        shortsLoading = true
        tiktoksLoading = true
        tweetsLoading = true
        videosError = false
        shortsError = false
        tiktoksError = false
        tweetsError = false
        try {
            val response = ApiClient.api.fetchYouTubeVideos(limit = 10)
            if (response.success) videos = response.data?.items ?: emptyList()
        } catch (e: Exception) {
            AppLogger.log("YouTube videos error: ${e.message}")
            videosError = true
        }
        videosLoading = false
        try {
            val response = ApiClient.api.fetchYouTubeShorts(limit = 10)
            if (response.success) shorts = response.data?.items ?: emptyList()
        } catch (e: Exception) {
            AppLogger.log("YouTube shorts error: ${e.message}")
            shortsError = true
        }
        shortsLoading = false
        try {
            val response = ApiClient.api.fetchTikTokVideos(limit = 10)
            if (response.success) tiktoks = response.data?.items ?: emptyList()
        } catch (e: Exception) {
            AppLogger.log("TikTok videos error: ${e.message}")
            tiktoksError = true
        }
        tiktoksLoading = false
        try {
            val response = ApiClient.api.fetchTwitterPosts(limit = 10)
            if (response.success) tweets = response.data?.items ?: emptyList()
        } catch (e: Exception) {
            AppLogger.log("Twitter posts error: ${e.message}")
            tweetsError = true
        }
        tweetsLoading = false
    }

    val isRefreshing = videosLoading || shortsLoading || tiktoksLoading || tweetsLoading

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { refreshKey++ },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Icon(
                Icons.Default.DirectionsBoat,
                contentDescription = null,
                tint = PirateTheme.accentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ports",
                fontFamily = PirateTheme.fontFamily,
                fontSize = 28.sp,
                color = PirateTheme.accentColor
            )
        }

        // YouTube Videos gallery
        YouTubeGallerySection(
            title = "Latest YouTube Videos",
            items = videos,
            isLoading = videosLoading,
            hasError = videosError,
            isShorts = false
        )

        Spacer(modifier = Modifier.height(20.dp))

        // YouTube Shorts gallery
        YouTubeGallerySection(
            title = "Latest YouTube Shorts",
            items = shorts,
            isLoading = shortsLoading,
            hasError = shortsError,
            isShorts = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // TikTok Videos gallery
        TikTokGallerySection(
            title = "Latest TikToks",
            items = tiktoks,
            isLoading = tiktoksLoading,
            hasError = tiktoksError
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Twitter / X posts gallery
        TwitterGallerySection(
            title = "Latest Posts on X",
            items = tweets,
            isLoading = tweetsLoading,
            hasError = tweetsError
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Social links - horizontal icon row
        SocialLinksRow(socialLinks)

        Spacer(modifier = Modifier.height(16.dp))
    }
    } // end PullToRefreshBox
}

@Composable
private fun SocialLinksRow(links: List<SocialLink>) {
    val uriHandler = LocalUriHandler.current

    SectionHeader("Follow the Captain")

    Spacer(modifier = Modifier.height(12.dp))

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(links.size) { index ->
            val link = links[index]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { uriHandler.openUri(link.url) }
                    .width(64.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            Color.White.copy(alpha = 0.08f),
                            RoundedCornerShape(14.dp)
                        )
                        .border(
                            1.dp,
                            link.color.copy(alpha = 0.3f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(link.iconRes),
                        contentDescription = link.name,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = link.name,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontFamily = PirateTheme.fontFamily,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun YouTubeGallerySection(
    title: String,
    items: List<YouTubeItem>,
    isLoading: Boolean,
    hasError: Boolean = false,
    isShorts: Boolean
) {
    val uriHandler = LocalUriHandler.current

    SectionHeader(title)

    Spacer(modifier = Modifier.height(12.dp))

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isShorts) 220.dp else 160.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PirateTheme.accentColor, modifier = Modifier.size(24.dp))
        }
    } else if (hasError) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Lost at sea!",
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 18.sp,
                    color = PirateTheme.accentColor.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No signal from shore \u2014 check yer connection and pull down to try again",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No ${if (isShorts) "shorts" else "videos"} available",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 14.sp
            )
        }
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.videoId }) { item ->
                if (isShorts) {
                    YouTubeShortCard(item = item, onClick = { uriHandler.openUri(item.url) })
                } else {
                    YouTubeVideoCard(item = item, onClick = { uriHandler.openUri(item.url) })
                }
            }
        }
    }
}

@Composable
private fun YouTubeVideoCard(item: YouTubeItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Thumbnail with play overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            ) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Bottom gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )
                // Play icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .border(1.dp, PirateTheme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = PirateTheme.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                // View count badge
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "Views",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = formatViewCount(item.views),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }
            }

            // Title and date
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatUploadDate(item.uploadedAt),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun YouTubeShortCard(item: YouTubeItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Full gradient overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )
            // Play icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .border(1.dp, PirateTheme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = PirateTheme.accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            // Title at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "Views",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = formatViewCount(item.views),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TikTokGallerySection(
    title: String,
    items: List<TikTokItem>,
    isLoading: Boolean,
    hasError: Boolean = false
) {
    val uriHandler = LocalUriHandler.current

    SectionHeader(title)

    Spacer(modifier = Modifier.height(12.dp))

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PirateTheme.accentColor, modifier = Modifier.size(24.dp))
        }
    } else if (hasError) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Lost at sea!",
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 18.sp,
                    color = PirateTheme.accentColor.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No signal from shore \u2014 check yer connection and pull down to try again",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No TikToks available",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 14.sp
            )
        }
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.videoId }) { item ->
                TikTokVideoCard(item = item, onClick = { uriHandler.openUri(item.url) })
            }
        }
    }
}

@Composable
private fun TikTokVideoCard(item: TikTokItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = item.coverImageUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Bottom gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                        )
                    )
            )
            // Play icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(50))
                    .border(1.dp, PirateTheme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = PirateTheme.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            // Duration badge top-right
            if (item.duration > 0) {
                Text(
                    text = formatDuration(item.duration),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
            // Title and stats at bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = item.title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Views
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "Views",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = formatViewCount(item.viewCount),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }
                    // Likes
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Likes",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = formatViewCount(item.likeCount),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TwitterGallerySection(
    title: String,
    items: List<TwitterPost>,
    isLoading: Boolean,
    hasError: Boolean = false
) {
    val uriHandler = LocalUriHandler.current

    SectionHeader(title)

    Spacer(modifier = Modifier.height(12.dp))

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PirateTheme.accentColor, modifier = Modifier.size(24.dp))
        }
    } else if (hasError) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Lost at sea!",
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 18.sp,
                    color = PirateTheme.accentColor.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No signal from shore \u2014 check yer connection and pull down to try again",
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No posts available",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 14.sp
            )
        }
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.tweetId }) { item ->
                TwitterPostCard(item = item, onClick = { uriHandler.openUri(item.url) })
            }
        }
    }
}

@Composable
private fun TwitterPostCard(item: TwitterPost, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Author row
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = item.authorProfileImageUrl,
                    contentDescription = item.authorName,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(50)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.authorName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "@${item.authorUsername}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(R.drawable.ic_x),
                    contentDescription = "X",
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tweet text
            Text(
                text = item.text,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            // Media preview (first image if available)
            if (item.mediaUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = item.mediaUrls.first(),
                    contentDescription = "Media",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Likes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Likes",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = formatViewCount(item.likeCount),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
                // Retweets
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Repeat,
                        contentDescription = "Retweets",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = formatViewCount(item.retweetCount),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
                // Date
                Text(
                    text = formatUploadDate(item.publishedAt),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "${mins}:${String.format("%02d", secs)}"
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .height(2.dp)
                .weight(1f)
                .background(
                    Brush.horizontalGradient(listOf(Color.Transparent, PirateTheme.accentColor.copy(alpha = 0.6f)))
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontFamily = PirateTheme.fontFamily,
            fontSize = 20.sp,
            color = PirateTheme.accentColor
        )
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .weight(1f)
                .background(
                    Brush.horizontalGradient(listOf(PirateTheme.accentColor.copy(alpha = 0.6f), Color.Transparent))
                )
        )
    }
}

private fun formatViewCount(views: Int): String {
    return when {
        views >= 1_000_000 -> String.format("%.1fM", views / 1_000_000.0)
        views >= 1_000 -> String.format("%.1fK", views / 1_000.0)
        else -> "$views"
    }
}

private fun formatUploadDate(dateStr: String): String {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = sdf.parse(dateStr) ?: return dateStr
        val now = System.currentTimeMillis()
        val diff = now - date.time
        val days = diff / (1000 * 60 * 60 * 24)
        when {
            days < 1 -> "Today"
            days < 2 -> "Yesterday"
            days < 7 -> "${days.toInt()}d ago"
            days < 30 -> "${(days / 7).toInt()}w ago"
            days < 365 -> "${(days / 30).toInt()}mo ago"
            else -> "${(days / 365).toInt()}y ago"
        }
    } catch (_: Exception) {
        dateStr
    }
}
