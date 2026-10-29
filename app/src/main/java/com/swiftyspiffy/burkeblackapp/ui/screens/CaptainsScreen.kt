package com.swiftyspiffy.burkeblackapp.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.swiftyspiffy.burkeblackapp.BurkeBlackApplication
import com.swiftyspiffy.burkeblackapp.R
import com.swiftyspiffy.burkeblackapp.data.captains.CaptainsRepository
import com.swiftyspiffy.burkeblackapp.data.captains.CaptainsState
import com.swiftyspiffy.burkeblackapp.data.models.*
import com.swiftyspiffy.burkeblackapp.ui.components.*
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Allows deterministic previews and UI fixtures without a continuously advancing countdown. */
val LocalCaptainsTime = staticCompositionLocalOf<(() -> Long)?> { null }

@Composable
fun captainsClock(): Long {
    LocalCaptainsTime.current?.let {
        return it()
    }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }
    return now
}

private fun captainDeadline(state: CaptainsState): String =
    state.schedule?.let {
        runCatching {
                "Closes " +
                    DateTimeFormatter.ofPattern("EEE, MMM d · h:mm a z", Locale.getDefault())
                        .withZone(ZoneId.of("America/New_York"))
                        .format(Instant.parse(it.end))
            }
            .getOrNull()
    } ?: "Weekly captain election"

@Composable
fun CaptainPortrait(captain: Captain, modifier: Modifier = Modifier) {
    val accent = PirateTheme.accentColor
    Box(
        modifier.background(
            Brush.radialGradient(listOf(accent.copy(alpha = 0.18f), Color.Transparent))
        ),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = captain.portrait?.let { CAPTAINS_BASE + "api/v1/assets/" + Uri.encode(it) },
            contentDescription = null,
            contentScale = ContentScale.Fit,
            placeholder = painterResource(R.drawable.app_icon),
            error = painterResource(R.drawable.app_icon),
            fallback = painterResource(R.drawable.app_icon),
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun CaptainsScreen(repository: CaptainsRepository, onBack: () -> Unit, onSignIn: () -> Unit) {
    val state by repository.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    DisposableEffect(repository) {
        repository.setScreenVisible(true)
        onDispose { repository.setScreenVisible(false) }
    }
    Box(Modifier.fillMaxSize().pirateScreenBackground(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 960.dp).fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        "Back",
                        tint = PirateTheme.accentColor,
                    )
                }
                Text("Back to the crew", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = repository::retry) {
                    Icon(Icons.Default.Refresh, "Refresh Captains", tint = PirateTheme.accentColor)
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(320.dp),
                modifier = Modifier.fillMaxSize().testTag("captains-content"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    PiratePageHeader(
                        "Captains",
                        Icons.Default.HowToVote,
                        "Choose who leads the crew.",
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) { CaptainWallet(state, onSignIn) }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf("Vote", "The crew", "Rankings").forEachIndexed { index, label ->
                            FilterChip(
                                selected = tab == index,
                                onClick = { tab = index },
                                modifier = Modifier.weight(1f),
                                label = {
                                    Text(
                                        label,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                    )
                                },
                            )
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    CaptainsMessages(state, repository, onSignIn)
                }
                when (tab) {
                    0 -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ElectionDeadline(state, captainsClock())
                        }
                        if (state.schedule == null || state.candidates.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                CaptainsEmpty(
                                    state.loading,
                                    "The next election is being prepared.",
                                    repository::retry,
                                )
                            }
                        }
                        items(state.candidates, key = { it.id }) { captain ->
                            CaptainCandidateCard(
                                captain,
                                state,
                                onClick = { repository.chooseCaptain(captain) },
                            )
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            state.winner?.let { winner ->
                                PirateCard(Modifier.fillMaxWidth()) {
                                    Row(
                                        Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    ) {
                                        CaptainPortrait(
                                            winner,
                                            Modifier.size(76.dp).clip(CircleShape),
                                        )
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                "CURRENT CAPTAIN",
                                                color = PirateTheme.accentColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                            Text(
                                                "${winner.name} ${winner.title}",
                                                color = Color.White,
                                                fontFamily = PirateTheme.fontFamily,
                                                fontSize = 24.sp,
                                            )
                                        }
                                    }
                                }
                            }
                                ?: Text(
                                    "A new crew. The first captain is yours to choose.",
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontSize = 14.sp,
                                )
                        }
                        item(span = { GridItemSpan(maxLineSpan) }) { CaptainTokenRules() }
                    }
                    1 ->
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            if (state.crew.isEmpty())
                                CaptainsEmpty(
                                    !state.detailsLoaded && state.detailsError == null,
                                    "The crew is unavailable right now.",
                                    repository::retry,
                                )
                            else CaptainCrewGallery(state, repository)
                        }
                    2 ->
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            CaptainRankings(state, repository::retry)
                        }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (state.connected) Icons.Default.CloudDone
                            else Icons.Default.CloudOff,
                            null,
                            tint = PirateTheme.accentColor,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (state.connected) "Live updates" else "Updates reconnecting",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                        )
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = repository::retry, enabled = !state.loading) {
                            Text(if (state.loading) "Refreshing…" else "Refresh")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptainWallet(state: CaptainsState, onSignIn: () -> Unit) {
    PirateCard(Modifier.fillMaxWidth(), highlighted = true) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                Icons.Default.Toll,
                null,
                tint = PirateTheme.accentColor,
                modifier = Modifier.size(32.dp),
            )
            Column(Modifier.weight(1f)) {
                Text("YOUR VOTING TOKENS", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                Text(
                    state.balance?.let { java.text.NumberFormat.getIntegerInstance().format(it) }
                        ?: "—",
                    fontFamily =
                        if (state.balance == null) androidx.compose.ui.text.font.FontFamily.Default
                        else PirateTheme.fontFamily,
                    fontSize = 32.sp,
                    color = PirateTheme.accentColor,
                )
            }
            if (!state.signedIn || state.authRequired)
                OutlinedButton(onClick = onSignIn) { Text("Sign in") }
        }
    }
}

@Composable
fun CaptainsMessages(state: CaptainsState, repository: CaptainsRepository, onSignIn: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOfNotNull(
                state.error,
                state.detailsError,
                state.balanceError,
                state.storageError,
                state.notice,
            )
            .distinct()
            .forEach { message ->
                Text(
                    message,
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    modifier =
                        Modifier.fillMaxWidth()
                            .background(
                                PirateTheme.accentColor.copy(alpha = 0.1f),
                                RoundedCornerShape(10.dp),
                            )
                            .padding(12.dp),
                )
            }
        state.pending?.let { pending ->
            PirateCard(Modifier.fillMaxWidth(), highlighted = true) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Your last vote needs confirmation",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                    val name =
                        (state.candidates + state.crew).find { it.id == pending.captain }?.name
                            ?: "your captain"
                    Text(
                        "${pending.amount} tokens for $name. Check the original request before casting another vote.",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 13.sp,
                    )
                    Button(
                        onClick =
                            if (state.authRequired || !state.signedIn) onSignIn
                            else repository::submitVote,
                        enabled = !state.sending,
                    ) {
                        Text(
                            if (state.sending) "Checking…"
                            else if (state.authRequired || !state.signedIn) "Sign in to check vote"
                            else "Check vote"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ElectionDeadline(state: CaptainsState, now: Long) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(captainDeadline(state), fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
        val seconds = state.remainingMillis(now).coerceAtLeast(0) / 1_000
        if (state.schedule != null)
            Text(
                if (seconds > 0) {
                    val days = seconds / 86400
                    val hours = seconds % 86400 / 3600
                    val minutes = seconds % 3600 / 60
                    "${if (days > 0) "${days}d " else ""}${hours}h ${minutes}m remaining"
                } else "Refreshing the next election…",
                fontSize = 12.sp,
                color = PirateTheme.accentColor,
                style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
            )
    }
}

@Composable
fun CaptainCandidateCard(
    captain: Captain,
    state: CaptainsState,
    compact: Boolean = false,
    onClick: () -> Unit,
) {
    val total = state.candidates.sumOf { it.votes.toDouble() }
    val share = if (total > 0) (captain.votes / total).toFloat().coerceIn(0f, 1f) else 0f
    PirateCard(
        Modifier.fillMaxWidth(),
        onClick = if (state.pending == null && !state.sending) onClick else null,
        textureIntensity = if (compact) 0.02f else 0.045f,
    ) {
        Column(
            Modifier.padding(if (compact) 10.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CaptainPortrait(
                    captain,
                    Modifier.size(if (compact) 44.dp else 68.dp).clip(CircleShape),
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        captain.name,
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = if (compact) 21.sp else 25.sp,
                        color = PirateTheme.accentColor,
                    )
                    Text(captain.title, fontSize = 12.sp, color = Color.White.copy(alpha = 0.65f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        java.text.NumberFormat.getIntegerInstance().format(captain.votes),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                    Text("votes", color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
                }
            }
            LinearProgressIndicator(
                progress = { share },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = PirateTheme.accentColor,
                trackColor = Color.White.copy(alpha = 0.08f),
            )
        }
    }
}

@Composable
private fun CaptainCrewGallery(state: CaptainsState, repository: CaptainsRepository) {
    val crew = remember(state.crew) { state.crew.sortedBy { it.order } }
    val pager = rememberPagerState(pageCount = { crew.size })
    val scope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                onClick = {
                    scope.launch {
                        pager.animateScrollToPage((pager.currentPage - 1 + crew.size) % crew.size)
                    }
                }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    "Previous captain",
                    tint = PirateTheme.accentColor,
                )
            }
            Text("${pager.currentPage + 1} / ${crew.size}", color = Color.White.copy(alpha = 0.6f))
            IconButton(
                onClick = {
                    scope.launch { pager.animateScrollToPage((pager.currentPage + 1) % crew.size) }
                }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    "Next captain",
                    tint = PirateTheme.accentColor,
                )
            }
        }
        HorizontalPager(state = pager, modifier = Modifier.fillMaxWidth()) { page ->
            val captain = crew[page]
            PirateCard(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CaptainPortrait(captain, Modifier.fillMaxWidth().height(240.dp))
                    Text(
                        captain.name,
                        color = Color.White,
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 32.sp,
                    )
                    Text(captain.title, color = PirateTheme.accentColor, fontSize = 16.sp)
                    Text(
                        captain.lore,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 15.sp,
                        lineHeight = 23.sp,
                    )
                    val candidate = state.candidates.find { it.id == captain.id }
                    Button(
                        onClick = { candidate?.let(repository::chooseCaptain) },
                        enabled = candidate != null && state.pending == null && !state.sending,
                    ) {
                        Text(
                            if (candidate == null) "Not in this election"
                            else "Vote for this captain"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptainRankings(state: CaptainsState, retry: () -> Unit) {
    var lifetime by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = !lifetime,
                onClick = { lifetime = false },
                label = { Text("This election") },
            )
            FilterChip(
                selected = lifetime,
                onClick = { lifetime = true },
                label = { Text("All time") },
            )
        }
        Text(
            "The crew behind the captains",
            color = Color.White,
            fontFamily = PirateTheme.fontFamily,
            fontSize = 26.sp,
        )
        Text(
            "Top voters · tokens spent, not tokens earned",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp,
        )
        val rows = if (lifetime) state.lifetimeRanking else state.currentRanking
        if (rows.isEmpty())
            CaptainsEmpty(
                !state.detailsLoaded && state.detailsError == null,
                "No votes cast yet. Make the first one count.",
                retry,
            )
        rows.forEachIndexed { index, row ->
            PirateCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        "${row.rank.takeIf { it > 0 } ?: index + 1}",
                        color = PirateTheme.accentColor,
                        fontFamily = PirateTheme.fontFamily,
                        fontSize = 28.sp,
                    )
                    Text(
                        row.name?.takeIf { it.isNotBlank() } ?: "Crew member",
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        java.text.NumberFormat.getIntegerInstance().format(row.votes),
                        color = PirateTheme.accentColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun CaptainsEmpty(loading: Boolean, text: String, retry: () -> Unit) {
    PirateCard(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (loading)
                CircularProgressIndicator(Modifier.size(24.dp), color = PirateTheme.accentColor)
            Text(
                if (loading) "Bringing the crew aboard…" else text,
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
            )
            if (!loading) TextButton(onClick = retry) { Text("Refresh") }
        }
    }
}

@Composable
fun CaptainTokenRules() {
    var open by rememberSaveable { mutableStateOf(false) }
    PirateCard(Modifier.fillMaxWidth(), onClick = { open = !open }) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "How do I earn voting tokens?",
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 20.sp,
                    color = PirateTheme.accentColor,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    if (open) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null,
                    tint = PirateTheme.accentColor,
                )
            }
            if (open) {
                Text(
                    "Redeem Pirate’s Vote for 5,000 channel points to earn 1 token after fulfillment. Subscriptions and gifted subs earn 1 / 2 / 6 tokens for Tier 1 / 2 / 3; gift tokens go to the gifter.",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                )
                Text(
                    "Unspent tokens carry over. Shared resub messages count; silent renewals don’t. Pirate’s Vote is limited to 200 accepted redemptions per month across the channel; excess redemptions are refunded.",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}

@Composable
fun CaptainVoteDialog(repository: CaptainsRepository, state: CaptainsState, onSignIn: () -> Unit) {
    val captain = state.selected ?: return
    val maxHeight = LocalConfiguration.current.screenHeightDp.dp * 0.85f
    Dialog(onDismissRequest = repository::closeConfirmation) {
        PirateCard(
            Modifier.widthIn(max = 440.dp).fillMaxWidth().height(maxHeight),
            highlighted = true,
            cornerRadius = 20.dp,
        ) {
            Row(
                Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Back ${captain.name}",
                    color = Color.White,
                    fontFamily = PirateTheme.fontFamily,
                    fontSize = 27.sp,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = repository::closeConfirmation) {
                    Icon(
                        Icons.Default.Close,
                        "Close vote confirmation",
                        tint = PirateTheme.accentColor,
                    )
                }
            }
            Column(
                Modifier.weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CaptainPortrait(captain, Modifier.size(116.dp))
                Text(captain.title, color = PirateTheme.accentColor)
                when {
                    !state.signedIn || state.authRequired -> {
                        Text(
                            "Sign in with Twitch to use your voting tokens.",
                            color = Color.White.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center,
                        )
                        Button(onClick = onSignIn) { Text("Sign in with Twitch") }
                    }
                    state.pending != null -> {
                        Text(
                            "Your ${state.pending.amount}-token vote is saved. Check its status before casting another vote.",
                            color = Color.White.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center,
                        )
                    }
                    state.balance == null -> {
                        Text(
                            state.balanceError ?: "Loading your voting tokens…",
                            color = Color.White.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                        )
                        TextButton(onClick = repository::retry) { Text("Refresh") }
                    }
                    state.balance == 0L -> {
                        Text("You have no voting tokens yet.", color = Color.White)
                        Text(
                            "Redeem Pirate’s Vote for 5,000 channel points, or subscribe / gift subs to earn voting tokens.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                        )
                    }
                    else -> {
                        Text(
                            "You have ${state.balance} tokens available.",
                            color = Color.White.copy(alpha = 0.75f),
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    repository.amount(
                                        ((state.amount.toLongOrNull() ?: 1) - 1)
                                            .coerceAtLeast(1)
                                            .toString()
                                    )
                                },
                                enabled = !state.sending && state.pending == null,
                            ) {
                                Icon(
                                    Icons.Default.Remove,
                                    "Spend one fewer token",
                                    tint = PirateTheme.accentColor,
                                )
                            }
                            OutlinedTextField(
                                value = state.amount,
                                onValueChange = repository::amount,
                                label = { Text("Tokens to spend") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions =
                                    KeyboardOptions(keyboardType = KeyboardType.Number),
                                enabled = !state.sending && state.pending == null,
                                isError = state.validAmount() == null && state.pending == null,
                            )
                            IconButton(
                                onClick = {
                                    repository.amount(
                                        ((state.amount.toLongOrNull() ?: 0).coerceAtMost(
                                                MAX_CAPTAIN_VOTES
                                            ) + 1)
                                            .coerceAtMost(minOf(state.balance, MAX_CAPTAIN_VOTES))
                                            .toString()
                                    )
                                },
                                enabled = !state.sending && state.pending == null,
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    "Spend one more token",
                                    tint = PirateTheme.accentColor,
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { repository.amount("1") },
                                enabled = !state.sending && state.pending == null,
                            ) {
                                Text("1 token")
                            }
                            OutlinedButton(
                                onClick = {
                                    repository.amount(
                                        minOf(state.balance, MAX_CAPTAIN_VOTES).toString()
                                    )
                                },
                                enabled = !state.sending && state.pending == null,
                            ) {
                                Text("All tokens")
                            }
                        }
                        if (state.pending == null) {
                            Text(
                                "Each token adds one vote. Cast votes can’t be taken back.",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
                CaptainsMessages(state.copy(pending = null), repository, onSignIn)
            }
            if (
                state.signedIn &&
                    !state.authRequired &&
                    (state.pending != null || (state.balance ?: 0) > 0)
            ) {
                Button(
                    onClick = repository::submitVote,
                    modifier =
                        Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp).fillMaxWidth(),
                    enabled =
                        !state.sending &&
                            state.pendingLoaded &&
                            (state.pending != null ||
                                (state.validAmount() != null && state.active(captainsClock()))),
                ) {
                    Text(
                        if (state.pending != null) {
                            if (state.sending) "Checking…" else "Check vote"
                        } else if (state.sending) "Casting…"
                        else
                            "Cast ${state.validAmount() ?: 0} ${if (state.validAmount() == 1L) "vote" else "votes"}"
                    )
                }
            }
        }
    }
}

@Composable
fun CaptainsSettingsScreen(onBack: () -> Unit) {
    val settings = (LocalContext.current.applicationContext as BurkeBlackApplication).appSettings
    val enabled by
        settings.captainPopupsEnabledFlow.collectAsStateWithLifecycle(initialValue = true)
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize().pirateScreenBackground(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 720.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = PirateTheme.accentColor)
            }
            PiratePageHeader(
                "Captains",
                Icons.Default.HowToVote,
                "Election alerts & voting preferences",
            )
            PirateCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Show election popups",
                            fontFamily = PirateTheme.fontFamily,
                            fontSize = 22.sp,
                            color = PirateTheme.accentColor,
                        )
                        Text(
                            "Automatically show each new election once.",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.65f),
                        )
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { scope.launch { settings.setCaptainPopupsEnabled(it) } },
                    )
                }
            }
            Text(
                "Minimize an election to keep browsing. Open Captains anytime from The Crew, even with automatic popups turned off.",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 14.sp,
                lineHeight = 21.sp,
            )
        }
    }
}
