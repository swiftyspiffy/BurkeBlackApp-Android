package com.swiftyspiffy.burkeblackapp.ui.screens

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swiftyspiffy.burkeblackapp.R
import com.swiftyspiffy.burkeblackapp.data.models.ClipVotingConfig
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import com.swiftyspiffy.burkeblackapp.data.models.VotingClip
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.ui.theme.TwitchPurple
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipVotingScreen(
    viewModel: ClipVotingViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) { AppLogger.log("ClipVoting: appeared") }
    val clips by viewModel.clips.collectAsState()
    val config by viewModel.config.collectAsState()
    val month by viewModel.month.collectAsState()
    val periodStart by viewModel.periodStart.collectAsState()
    val periodEnd by viewModel.periodEnd.collectAsState()
    val totalVoters by viewModel.totalVoters.collectAsState()
    val userHasVoted by viewModel.userHasVoted.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val error by viewModel.error.collectAsState()
    val selections by viewModel.selections.collectAsState()
    val submitSuccess by viewModel.submitSuccess.collectAsState()
    val context = LocalContext.current

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
                painter = painterResource(R.drawable.ic_twitch_clip),
                contentDescription = null,
                tint = PirateTheme.accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Monthly Clip Voting",
                fontFamily = PirateTheme.fontFamily,
                fontSize = 22.sp,
                color = PirateTheme.accentColor
            )
        }

        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { viewModel.fetchClips() },
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                isLoading && clips.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PirateTheme.accentColor)
                    }
                }
                error != null && clips.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Shipwrecked!", fontFamily = PirateTheme.fontFamily, fontSize = 24.sp, color = PirateTheme.accentColor)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                error ?: "", fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
                clips.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents, contentDescription = null,
                                tint = PirateTheme.accentColor.copy(alpha = 0.3f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No Clips This Month",
                                fontFamily = PirateTheme.fontFamily, fontSize = 24.sp,
                                color = PirateTheme.accentColor, textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "The treasure chest be empty, matey. Check back soon!",
                                fontFamily = PirateTheme.fontFamily, fontSize = 15.sp,
                                color = Color.White.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                else -> {
                    val canSubmit = viewModel.canSubmit()

                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                        ) {
                            // Period info card
                            PeriodHeader(
                                periodStart = periodStart,
                                periodEnd = periodEnd,
                                totalVoters = totalVoters,
                                config = config
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Voting instructions
                            if (config != null) {
                                VotingInstructions(config = config!!, userHasVoted = userHasVoted)
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Selected picks (for ranked mode, only while voting)
                            if (!userHasVoted && config?.votingMode == "multi" && config?.multiType == "ranked" && selections.isNotEmpty()) {
                                YourRankings(
                                    selections = selections,
                                    clips = clips,
                                    onMoveUp = { idx -> if (idx > 0) viewModel.moveSelection(idx, idx - 1) },
                                    onMoveDown = { idx -> if (idx < selections.size - 1) viewModel.moveSelection(idx, idx + 1) },
                                    onRemove = { clipId -> viewModel.toggleSelection(clipId) }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Sort clips: if user has voted in ranked mode, order voted clips first by rank
                            val isRankedAndVoted = userHasVoted && config?.votingMode == "multi" && config?.multiType == "ranked"
                            val displayClips = if (isRankedAndVoted) {
                                val voted = clips.filter { it.userRank != null }.sortedBy { it.userRank }
                                val unvoted = clips.filter { it.userRank == null }
                                voted + unvoted
                            } else {
                                clips
                            }

                            // Clip cards
                            val showViews = config?.showViewCounts == true
                            displayClips.forEach { clip ->
                                val selectionIndex = selections.indexOf(clip.clipId)
                                val isSelected = selectionIndex >= 0
                                ClipCard(
                                    clip = clip,
                                    isSelected = isSelected,
                                    selectionRank = if (isSelected) selectionIndex + 1 else null,
                                    config = config,
                                    showViewCount = showViews,
                                    locked = userHasVoted,
                                    onToggle = { if (!userHasVoted) viewModel.toggleSelection(clip.clipId) },
                                    onWatch = {
                                        CustomTabsIntent.Builder().build().launchUrl(
                                            context, Uri.parse(clip.clipUrl)
                                        )
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Inline submit (when not enough selections yet, and hasn't voted)
                            if (!userHasVoted && !canSubmit) {
                                Spacer(modifier = Modifier.height(8.dp))
                                SubmitSection(
                                    canSubmit = false,
                                    isSubmitting = isSubmitting,
                                    submitSuccess = submitSuccess,
                                    userHasVoted = userHasVoted,
                                    onSubmit = { viewModel.submitVote() },
                                    onDismissSuccess = { viewModel.clearSubmitSuccess() }
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        // Sticky submit button at bottom when ready (hide if already voted)
                        if (canSubmit && !userHasVoted) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                SubmitSection(
                                    canSubmit = true,
                                    isSubmitting = isSubmitting,
                                    submitSuccess = submitSuccess,
                                    userHasVoted = userHasVoted,
                                    onSubmit = { viewModel.submitVote() },
                                    onDismissSuccess = { viewModel.clearSubmitSuccess() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodHeader(
    periodStart: String,
    periodEnd: String,
    totalVoters: Int,
    config: ClipVotingConfig?
) {
    val formattedRange = try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val formatter = SimpleDateFormat("MMM d", Locale.US)
        val yearFormatter = SimpleDateFormat("MMM d, yyyy", Locale.US)
        val start = parser.parse(periodStart)
        val end = parser.parse(periodEnd)
        if (start != null && end != null) {
            "${formatter.format(start)} \u2013 ${yearFormatter.format(end)}"
        } else null
    } catch (_: Exception) { null }

    Card(
        colors = CardDefaults.cardColors(containerColor = TwitchPurple.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (formattedRange != null) {
                Text(
                    text = "Top clips from $formattedRange",
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.People, contentDescription = null,
                        tint = TwitchPurple, modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "$totalVoters voters",
                        fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f)
                    )
                }
                if (config != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.HowToVote, contentDescription = null,
                            tint = TwitchPurple, modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val modeText = when {
                            config.votingMode == "single" -> "Pick 1 clip"
                            config.multiType == "ranked" -> "Rank your top ${config.voteCount}"
                            else -> "Pick ${config.voteCount} clips"
                        }
                        Text(modeText, fontSize = 13.sp, color = Color.White.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

@Composable
private fun VotingInstructions(config: ClipVotingConfig, userHasVoted: Boolean) {
    val text = when {
        userHasVoted -> "Ye've already cast yer vote! Ye can change it anytime."
        config.votingMode == "single" -> "Pick yer favorite clip and cast yer vote, pirate!"
        config.multiType == "ranked" -> "Rank yer top ${config.voteCount} clips \u2014 #1 gets the most booty!"
        else -> "Pick yer ${config.voteCount} favorite clips to vote for!"
    }
    Text(
        text = text,
        fontFamily = PirateTheme.fontFamily,
        fontSize = 14.sp,
        color = Color.White.copy(alpha = 0.4f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun YourRankings(
    selections: List<String>,
    clips: List<VotingClip>,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onRemove: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Yer Rankings",
                fontFamily = PirateTheme.fontFamily,
                fontSize = 18.sp,
                color = PirateTheme.accentColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            selections.forEachIndexed { index, clipId ->
                val clip = clips.find { it.clipId == clipId } ?: return@forEachIndexed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rank number
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(PirateTheme.accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${index + 1}",
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        clip.title,
                        fontSize = 13.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    // Reorder buttons
                    IconButton(onClick = { onMoveUp(index) }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.KeyboardArrowUp, contentDescription = "Move up",
                            tint = if (index > 0) PirateTheme.accentColor else Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { onMoveDown(index) }, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.KeyboardArrowDown, contentDescription = "Move down",
                            tint = if (index < selections.size - 1) PirateTheme.accentColor else Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClipCard(
    clip: VotingClip,
    isSelected: Boolean,
    selectionRank: Int?,
    config: ClipVotingConfig?,
    showViewCount: Boolean = false,
    locked: Boolean = false,
    onToggle: () -> Unit,
    onWatch: () -> Unit
) {
    val borderColor = when {
        locked && clip.userRank != null -> PirateTheme.accentColor.copy(alpha = 0.7f)
        isSelected -> PirateTheme.accentColor
        clip.userRank != null -> TwitchPurple.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (borderColor != Color.Transparent)
                    Modifier.border(2.dp, borderColor, RoundedCornerShape(14.dp))
                else Modifier
            )
    ) {
        Column {
            // Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onWatch)
            ) {
                AsyncImage(
                    model = clip.thumbnailUrl,
                    contentDescription = clip.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(480f / 272f)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                    contentScale = ContentScale.Crop
                )
                // Duration badge
                Text(
                    text = formatDuration(clip.duration),
                    fontSize = 11.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                // Play overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(44.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow, contentDescription = "Watch",
                        tint = Color.White, modifier = Modifier.size(28.dp)
                    )
                }
                // Rank badge: show user's vote rank when locked, or selection rank when voting
                if (locked && clip.userRank != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .size(36.dp)
                            .background(PirateTheme.accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "#${clip.userRank}",
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                } else if (!locked && isSelected && selectionRank != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .size(32.dp)
                            .background(PirateTheme.accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "#$selectionRank",
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 13.sp,
                            color = Color.Black
                        )
                    }
                } else if (!locked && !isSelected && clip.userRank != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(TwitchPurple.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "Voted #${clip.userRank}",
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // Info section
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = clip.title,
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 16.sp,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Clipped by ${clip.creatorName}",
                        fontSize = 12.sp,
                        color = TwitchPurple
                    )
                    if (showViewCount) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Visibility, contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                formatViewCount(clip.viewCount),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                val hasStats = config?.showPoints == true || config?.showVoteCounts == true

                if (hasStats) {
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Vote stats + select button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (hasStats) Arrangement.SpaceBetween else Arrangement.End
                ) {
                    if (hasStats) {
                        // Points and votes (conditionally shown)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (config?.showPoints == true) {
                                Text(
                                    "${clip.totalPoints} pts",
                                    fontFamily = PirateTheme.fontFamily,
                                    fontSize = 14.sp,
                                    color = PirateTheme.accentColor.copy(alpha = 0.7f)
                                )
                            }
                            if (config?.showVoteCounts == true) {
                                Text(
                                    "${clip.voteCount} votes",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.35f)
                                )
                            }
                        }
                    }

                    // Select/deselect button or voted indicator
                    if (locked && clip.userRank != null) {
                        // Show "Yer Pick" badge for voted clips
                        Box(
                            modifier = Modifier
                                .background(PirateTheme.accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Check, contentDescription = null,
                                    tint = PirateTheme.accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Yer #${clip.userRank} Pick",
                                    fontFamily = PirateTheme.fontFamily,
                                    fontSize = 13.sp,
                                    color = PirateTheme.accentColor
                                )
                            }
                        }
                    } else if (!locked) {
                        val buttonText = when {
                            isSelected -> "Selected"
                            config?.votingMode == "single" -> "Vote"
                            else -> "Select"
                        }
                        Button(
                            onClick = onToggle,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) PirateTheme.accentColor else TwitchPurple.copy(alpha = 0.3f),
                                contentColor = if (isSelected) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check, contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                buttonText,
                                fontFamily = PirateTheme.fontFamily,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubmitSection(
    canSubmit: Boolean,
    isSubmitting: Boolean,
    submitSuccess: Boolean,
    userHasVoted: Boolean,
    onSubmit: () -> Unit,
    onDismissSuccess: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (submitSuccess) {
            // Confetti behind the card
            ConfettiOverlay()

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1B5E20).copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismissSuccess() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "\u2620",
                        fontSize = 36.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Yer vote has been cast, Captain!",
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 20.sp,
                        color = PirateTheme.accentColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "The crew salutes ye!",
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50).copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Button(
                onClick = onSubmit,
                enabled = canSubmit && !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PirateTheme.accentColor,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.White.copy(alpha = 0.1f),
                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.HowToVote, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (userHasVoted) "Update Yer Vote" else "Cast Yer Vote!",
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

private data class ConfettiPiece(
    val x: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val wobbleSpeed: Float,
    val wobbleAmount: Float,
    val startOffset: Float
)

@Composable
private fun ConfettiOverlay() {
    val accent = PirateTheme.accentColor
    val pieces = remember(accent) {
        val colors = listOf(
            accent,
            accent.copy(alpha = 0.7f),
            TwitchPurple,
            TwitchPurple.copy(alpha = 0.7f),
            Color(0xFF4CAF50),
            Color(0xFFFF6B35),
            Color.White
        )
        List(40) {
            ConfettiPiece(
                x = Random.nextFloat(),
                speed = 0.3f + Random.nextFloat() * 0.7f,
                size = 4f + Random.nextFloat() * 6f,
                color = colors[Random.nextInt(colors.size)],
                wobbleSpeed = 1f + Random.nextFloat() * 3f,
                wobbleAmount = 10f + Random.nextFloat() * 20f,
                startOffset = Random.nextFloat()
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "confetti")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "confettiProgress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        pieces.forEach { piece ->
            val t = (progress + piece.startOffset) % 1f
            val y = t * h * 1.2f - h * 0.1f
            val wobble = sin((t * piece.wobbleSpeed * Math.PI * 2).toFloat()) * piece.wobbleAmount
            val cx = piece.x * w + wobble
            drawCircle(
                color = piece.color.copy(alpha = (1f - t).coerceIn(0.2f, 0.9f)),
                radius = piece.size,
                center = Offset(cx, y)
            )
        }
    }
}

private fun formatDuration(seconds: Float): String {
    val mins = (seconds / 60).toInt()
    val secs = (seconds % 60).toInt()
    return "%d:%02d".format(mins, secs)
}

private fun formatViewCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "%.1fM".format(count / 1_000_000f)
        count >= 1_000 -> "%.1fK".format(count / 1_000f)
        else -> "$count"
    }
}
