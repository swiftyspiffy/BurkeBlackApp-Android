package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.swiftyspiffy.burkeblackapp.R
import com.swiftyspiffy.burkeblackapp.auth.TwitchAuthManager
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import com.swiftyspiffy.burkeblackapp.ui.components.BadgeChip
import com.swiftyspiffy.burkeblackapp.ui.components.DateUtils
import com.swiftyspiffy.burkeblackapp.ui.components.StatFormatter
import com.swiftyspiffy.burkeblackapp.ui.components.StatRow
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import com.swiftyspiffy.burkeblackapp.ui.theme.TwitchPurple
import com.swiftyspiffy.burkeblackapp.util.AppLogger

@Composable
fun AccountScreen(
    viewModel: AccountViewModel,
    onBack: (() -> Unit)? = null,
    onNavigateToSoundbytes: () -> Unit,
    onNavigateToGiveaways: () -> Unit,
    onNavigateToBits: () -> Unit,
    onNavigateToDonations: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    onNavigateToModPanel: () -> Unit = {},
    onNavigateToCaptainsDispatch: (() -> Unit)? = null,
    onNavigateToCrewDispatch: (() -> Unit)? = null
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    androidx.compose.runtime.LaunchedEffect(Unit) { AppLogger.log("Account: appeared (loggedIn=$isLoggedIn)") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = PirateTheme.accentColor,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (isLoggedIn) {
            LoggedInView(
                viewModel = viewModel,
                onNavigateToSoundbytes = onNavigateToSoundbytes,
                onNavigateToGiveaways = onNavigateToGiveaways,
                onNavigateToBits = onNavigateToBits,
                onNavigateToDonations = onNavigateToDonations,
                onNavigateToFeedback = onNavigateToFeedback,
                onNavigateToModPanel = onNavigateToModPanel,
                onNavigateToCaptainsDispatch = onNavigateToCaptainsDispatch,
                onNavigateToCrewDispatch = onNavigateToCrewDispatch
            )
        } else {
            LoggedOutView(viewModel = viewModel)
        }

        // Back button when navigated from another screen
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }
    }
}

@Composable
private fun LoggedOutView(viewModel: AccountViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showReviewerDialog by remember { mutableStateOf(false) }
    var reviewerUsername by remember { mutableStateOf("") }
    var reviewerPassword by remember { mutableStateOf("") }
    var reviewerError by remember { mutableStateOf<String?>(null) }
    var isReviewerLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User icon
        Icon(
            Icons.Default.AccountCircle,
            contentDescription = "Account",
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sign in to see your stats, giveaways, and send soundbytes!",
            color = Color.White.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        var suppressTap by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(50))
                .background(TwitchPurple)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            suppressTap = false
                            val startTime = System.currentTimeMillis()
                            val released = tryAwaitRelease()
                            val holdTime = System.currentTimeMillis() - startTime
                            if (released && holdTime >= 5000) {
                                suppressTap = true
                                showReviewerDialog = true
                            }
                        },
                        onTap = {
                            if (!suppressTap) {
                                scope.launch {
                                    val forceVerify = viewModel.sessionManager.getForceVerify()
                                    TwitchAuthManager.launchAuth(context, forceVerify = forceVerify)
                                }
                            }
                            suppressTap = false
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_twitch_white),
                    contentDescription = "Twitch logo",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Login with Twitch", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }

    // Reviewer Access Dialog
    if (showReviewerDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isReviewerLoading) {
                    showReviewerDialog = false
                    reviewerUsername = ""
                    reviewerPassword = ""
                    reviewerError = null
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Reviewer Access", color = Color.White) },
            text = {
                Column {
                    Text(
                        "Enter reviewer credentials",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = reviewerUsername,
                        onValueChange = { reviewerUsername = it },
                        placeholder = { Text("Username", color = Color.White.copy(alpha = 0.3f)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TwitchPurple,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = TwitchPurple
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reviewerPassword,
                        onValueChange = { reviewerPassword = it },
                        placeholder = { Text("Password", color = Color.White.copy(alpha = 0.3f)) },
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TwitchPurple,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = TwitchPurple
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    reviewerError?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = Color(0xFFEF5350), fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (reviewerUsername.isBlank() || reviewerPassword.isBlank()) return@TextButton
                        isReviewerLoading = true
                        reviewerError = null
                        scope.launch {
                            try {
                                val body = buildJsonObject {
                                    put("username", reviewerUsername)
                                    put("password", reviewerPassword)
                                }
                                val response = ApiClient.api.reviewerLogin(body)
                                if (response.success && response.data != null) {
                                    viewModel.handleReviewerLogin(
                                        token = response.data.token,
                                        username = response.data.username,
                                        avatarUrl = response.data.avatarUrl,
                                        isModerator = response.data.isModerator ?: false
                                    )
                                    showReviewerDialog = false
                                    reviewerUsername = ""
                                    reviewerPassword = ""
                                } else {
                                    reviewerError = response.error ?: "Invalid credentials"
                                }
                            } catch (e: retrofit2.HttpException) {
                                val errorBody = e.response()?.errorBody()?.string()
                                reviewerError = try {
                                    val json = kotlinx.serialization.json.Json.decodeFromString<com.swiftyspiffy.burkeblackapp.data.models.ApiErrorOrSuccess>(errorBody ?: "")
                                    json.error ?: "Invalid credentials"
                                } catch (_: Exception) {
                                    "Invalid credentials"
                                }
                            } catch (e: Exception) {
                                reviewerError = e.message ?: "Login failed"
                            }
                            isReviewerLoading = false
                        }
                    },
                    enabled = !isReviewerLoading
                ) {
                    if (isReviewerLoading) {
                        CircularProgressIndicator(color = PirateTheme.accentColor, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Login", color = PirateTheme.accentColor)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showReviewerDialog = false
                        reviewerUsername = ""
                        reviewerPassword = ""
                        reviewerError = null
                    },
                    enabled = !isReviewerLoading
                ) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.5f))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LoggedInView(
    viewModel: AccountViewModel,
    onNavigateToSoundbytes: () -> Unit,
    onNavigateToGiveaways: () -> Unit,
    onNavigateToBits: () -> Unit,
    onNavigateToDonations: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    onNavigateToModPanel: () -> Unit,
    onNavigateToCaptainsDispatch: (() -> Unit)? = null,
    onNavigateToCrewDispatch: (() -> Unit)? = null
) {
    val username by viewModel.username.collectAsState()
    val avatarUrl by viewModel.avatarUrl.collectAsState()
    val isModerator by viewModel.isModerator.collectAsState()
    val doubloons by viewModel.doubloons.collectAsState()
    val donations by viewModel.donations.collectAsState()
    val totalBits by viewModel.totalBits.collectAsState()
    val soundbyteCredits by viewModel.soundbyteCredits.collectAsState()
    val soundbyteSends by viewModel.soundbyteSends.collectAsState()
    val giveawaysEntered by viewModel.giveawaysEntered.collectAsState()
    val giveawaysWon by viewModel.giveawaysWon.collectAsState()
    val giveawaysDonated by viewModel.giveawaysDonated.collectAsState()
    val follows by viewModel.follows.collectAsState()
    val subscribed by viewModel.subscribed.collectAsState()
    val subTier by viewModel.subTier.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isSubGifter by viewModel.isSubGifter.collectAsState()
    val isBitsSender by viewModel.isBitsSender.collectAsState()
    val isDonator by viewModel.isDonator.collectAsState()
    val followedAt by viewModel.followedAt.collectAsState()
    val latestSub by viewModel.latestSub.collectAsState()
    val favSoundbyte by viewModel.favSoundbyte.collectAsState()
    val lastSoundbyte by viewModel.lastSoundbyte.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Profile card - horizontal layout like iOS
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar on left
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(avatarUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = username,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Username + role
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = username,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    if (userRole.isNotBlank()) {
                        Text(
                            text = userRole,
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }
                    if (subscribed && subTier != null) {
                        Text(
                            text = subTier!!,
                            color = Color(0xFF2196F3),
                            fontSize = 12.sp
                        )
                    }
                }

                // Badges stacked on right
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (follows) BadgeChip("Follower", TwitchPurple)
                    if (subscribed) BadgeChip("Subscriber", Color(0xFF2196F3))
                    if (isModerator) BadgeChip("Moderator", Color(0xFF00BCD4))
                    if (isSubGifter) BadgeChip("Gifter", Color(0xFFE040FB))
                    if (isBitsSender) {
                        val cheerRes = when {
                            totalBits >= 10000 -> R.drawable.cheer_10000
                            totalBits >= 5000 -> R.drawable.cheer_5000
                            totalBits >= 100 -> R.drawable.cheer_100
                            else -> R.drawable.cheer_1
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFFFF9800).copy(alpha = 0.15f), RoundedCornerShape(50))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Image(
                                painter = painterResource(cheerRes),
                                contentDescription = "Bits badge",
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Bits", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFFF9800))
                        }
                    }
                    if (isDonator) BadgeChip("Donator", Color(0xFF4CAF50))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                icon = Icons.Default.CardGiftcard,
                label = "Giveaways",
                onClick = onNavigateToGiveaways,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                icon = Icons.Default.MusicNote,
                label = "Soundbytes",
                onClick = onNavigateToSoundbytes,
                modifier = Modifier.weight(1f)
            )
            if (isModerator) {
                ActionButton(
                    icon = Icons.Default.Shield,
                    label = "Mod Panel",
                    onClick = onNavigateToModPanel,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Captain's Dispatch / Crew Dispatch buttons
        if (onNavigateToCaptainsDispatch != null || onNavigateToCrewDispatch != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                onNavigateToCaptainsDispatch?.let {
                    ActionButton(
                        icon = Icons.Default.Campaign,
                        label = "Captain's Dispatch",
                        onClick = it,
                        modifier = Modifier.weight(1f)
                    )
                }
                onNavigateToCrewDispatch?.let {
                    ActionButton(
                        icon = Icons.Default.Campaign,
                        label = "Crew Dispatch",
                        onClick = it,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats
        SectionCard("Your Stats") {
            StatRow("Doubloons", StatFormatter.integer(doubloons))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            ClickableStatRow("Donations", StatFormatter.currency(donations), onClick = onNavigateToDonations)
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            ClickableStatRow("Bits Cheered", StatFormatter.integer(totalBits), onClick = onNavigateToBits)
            followedAt?.let {
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                StatRow("Following Since", DateUtils.formatFollowDate(it))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Subscription
        if (subscribed) {
            SectionCard("Subscription") {
                subTier?.let { StatRow("Tier", it) }
                latestSub?.let { sub ->
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    StatRow("Months", "${sub.cumulativeMonths}")
                    if (sub.streakMonths > 0) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        StatRow("Streak", "${sub.streakMonths}")
                    }
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    StatRow("Recent Subscription", DateUtils.formatFollowDate(sub.date))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    StatRow("Subscribed Since", DateUtils.subscribedSince(sub.cumulativeMonths))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Giveaways
        SectionCard("Giveaways") {
            StatRow("Entered", StatFormatter.integer(giveawaysEntered))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            StatRow("Won", StatFormatter.integer(giveawaysWon))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            StatRow("Donated", StatFormatter.integer(giveawaysDonated))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Soundbytes
        SectionCard("Soundbytes") {
            StatRow("Credits", StatFormatter.integer(soundbyteCredits))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            StatRow("Sends", StatFormatter.integer(soundbyteSends))
            favSoundbyte?.let {
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                StatRow("Favorite", "${it.name} (${it.count}x)")
            }
            lastSoundbyte?.let {
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                StatRow("Last Sent", "${it.name} (${DateUtils.formatEpochSeconds(it.date)})")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Send Feedback - full width card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            onClick = onNavigateToFeedback
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Email, contentDescription = "Send feedback", tint = PirateTheme.accentColor, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Send Feedback", color = PirateTheme.accentColor, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sign Out - full width card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            onClick = { viewModel.logout() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sign Out", color = Color(0xFFEF5350), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(64.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, tint = PirateTheme.accentColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, color = Color.White)
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = PirateTheme.accentColor,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun ClickableStatRow(label: String, value: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White)
            Row {
                Text(value, color = Color.White.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.width(4.dp))
                Text(">", color = Color.White.copy(alpha = 0.3f))
            }
        }
    }
}
