package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.swiftyspiffy.burkeblackapp.data.websocket.GiveawayPhase
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import com.swiftyspiffy.burkeblackapp.data.websocket.GiveawayWebSocketManager
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GiveawayPopupOverlay(isAuthenticated: Boolean = false) {
    LaunchedEffect(Unit) { AppLogger.log("GiveawayPopup: appeared") }
    val wsManager = GiveawayWebSocketManager.instance
    val giveaway by wsManager.activeGiveaway.collectAsState()
    val isDismissed by wsManager.isDismissed.collectAsState()
    val claimError by wsManager.claimError.collectAsState()
    val scope = rememberCoroutineScope()
    var showAuthError by remember { mutableStateOf(false) }

    // Main popup - visible when there's an active giveaway and not dismissed
    AnimatedVisibility(
        visible = giveaway != null && !isDismissed,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        giveaway?.let { active ->
            val isWinner = wsManager.isCurrentUserWinner()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isWinner && active.phase == GiveawayPhase.WINNER)
                            Color(0xFF1A2E1A)
                        else
                            Color(0xFF1A1A2E)
                    )
                    .padding(16.dp)
            ) {
                // Dismiss button
                IconButton(
                    onClick = { wsManager.dismissGiveaway() },
                    modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                }

                Column {
                    // Header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isWinner && active.phase == GiveawayPhase.WINNER)
                                Icons.Default.EmojiEvents
                            else
                                Icons.Default.CardGiftcard,
                            contentDescription = "Giveaway",
                            tint = PirateTheme.accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isWinner && active.phase == GiveawayPhase.WINNER)
                                "YE WON, CAPTAIN!"
                            else
                                "GIVEAWAY",
                            fontFamily = PirateTheme.fontFamily,
                            color = PirateTheme.accentColor,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = active.name,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Donated by ${active.donator}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )

                    active.totalEntries?.let {
                        Text(
                            text = "$it entries",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp
                        )
                    }

                    // Countdown timer
                    if (active.timeRemaining != null && active.countdownStartMillis != null) {
                        var remaining by remember { mutableLongStateOf(active.timeRemaining?.toLong() ?: 0L) }
                        LaunchedEffect(active.countdownStartMillis) {
                            while (remaining > 0) {
                                delay(1000)
                                val elapsed = (System.currentTimeMillis() - (active.countdownStartMillis ?: System.currentTimeMillis())) / 1000
                                remaining = maxOf(0, (active.timeRemaining?.toLong() ?: 0L) - elapsed)
                            }
                        }
                        if (remaining > 0) {
                            Text(
                                text = "${remaining}s remaining",
                                color = PirateTheme.accentColor,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when (active.phase) {
                        GiveawayPhase.ENTRY -> {
                            if (active.isEntered) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("You're entered!", color = Color(0xFF4CAF50), fontWeight = FontWeight.SemiBold)
                                    OutlinedButton(
                                        onClick = { scope.launch { wsManager.leaveGiveaway() } },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Leave", color = Color(0xFFEF5350), fontSize = 13.sp)
                                    }
                                }
                            } else {
                                // Auth error message
                                if (showAuthError) {
                                    Text(
                                        text = "Ye must visit the Captain's Quarters and login with yer Twitch account before entering, pirate!",
                                        color = Color(0xFFEF5350),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                Button(
                                    onClick = {
                                        if (isAuthenticated) {
                                            showAuthError = false
                                            scope.launch { wsManager.enterGiveaway() }
                                        } else {
                                            showAuthError = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAuthenticated) PirateTheme.accentColor else PirateTheme.accentColor.copy(alpha = 0.3f)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Enter Giveaway", color = if (isAuthenticated) Color.Black else Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        GiveawayPhase.WINNER -> {
                            if (isWinner) {
                                // Winner celebration
                                Text(
                                    text = "\u2620 The treasure be yers! \u2620",
                                    fontFamily = PirateTheme.fontFamily,
                                    color = PirateTheme.accentColor,
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Claim error
                                if (claimError != null) {
                                    Text(
                                        text = claimError!!,
                                        color = Color(0xFFEF5350),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }

                                // Claim and Pass buttons
                                Button(
                                    onClick = { scope.launch { wsManager.claimGiveaway() } },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Claim Yer Prize!", color = Color.White, fontFamily = PirateTheme.fontFamily, fontSize = 16.sp)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { scope.launch { wsManager.passGiveaway() } },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Pass", color = Color(0xFFEF5350), fontSize = 13.sp)
                                }
                            } else {
                                // Someone else won
                                Text(
                                    text = "Winner: ${active.winner ?: "Drawing..."}",
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        GiveawayPhase.CLAIMED -> {
                            if (isWinner) {
                                Text(
                                    text = "\u2620 Ye claimed the booty! Check yer giveaway wins for details.",
                                    fontFamily = PirateTheme.fontFamily,
                                    color = Color(0xFF4CAF50),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = "${active.winner} claimed the prize!",
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Mini restore button when dismissed but giveaway is still active (not after claimed)
    if (giveaway != null && isDismissed && giveaway?.phase != GiveawayPhase.CLAIMED) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, top = 16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A2E).copy(alpha = 0.9f))
                    .clickable { wsManager.showGiveaway() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CardGiftcard,
                    contentDescription = "Show giveaway",
                    tint = PirateTheme.accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Giveaway Active",
                    fontFamily = PirateTheme.fontFamily,
                    color = PirateTheme.accentColor,
                    fontSize = 12.sp
                )
            }
        }
    }
}
