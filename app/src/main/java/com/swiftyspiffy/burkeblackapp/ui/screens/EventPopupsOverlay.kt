package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swiftyspiffy.burkeblackapp.data.EventPopup
import com.swiftyspiffy.burkeblackapp.data.captains.CaptainsRepository
import com.swiftyspiffy.burkeblackapp.data.websocket.GiveawayPhase
import com.swiftyspiffy.burkeblackapp.data.websocket.GiveawayWebSocketManager
import com.swiftyspiffy.burkeblackapp.ui.components.PirateCard
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventPopupsOverlay(
    repository: CaptainsRepository,
    authenticated: Boolean,
    onOpenCaptains: () -> Unit,
    onSignIn: () -> Unit,
) {
    val presentation by repository.popups.state.collectAsStateWithLifecycle()
    val state by repository.state.collectAsStateWithLifecycle()
    val giveaway by GiveawayWebSocketManager.instance.activeGiveaway.collectAsStateWithLifecycle()
    val now = captainsClock()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val popupHeight = maxHeight * 0.78f
        FlowRow(
            Modifier.align(Alignment.BottomEnd)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            if (
                giveaway != null &&
                    giveaway?.phase != GiveawayPhase.CLAIMED &&
                    presentation.expanded != EventPopup.GIVEAWAY
            ) {
                AssistChip(
                    onClick = { GiveawayWebSocketManager.instance.showGiveaway() },
                    label = { Text("Giveaway Active") },
                    colors =
                        AssistChipDefaults.assistChipColors(containerColor = Color(0xFF12110F)),
                    leadingIcon = { Icon(Icons.Default.CardGiftcard, null, Modifier.size(16.dp)) },
                )
            }
            if (
                EventPopup.CAPTAINS in presentation.available &&
                    !presentation.captainsScreenVisible &&
                    presentation.expanded != EventPopup.CAPTAINS &&
                    state.active(now)
            ) {
                AssistChip(
                    onClick = { repository.popups.present(EventPopup.CAPTAINS) },
                    label = { Text("Captain voting") },
                    colors =
                        AssistChipDefaults.assistChipColors(containerColor = Color(0xFF12110F)),
                    leadingIcon = { Icon(Icons.Default.HowToVote, null, Modifier.size(16.dp)) },
                )
            }
        }
        when (presentation.expanded) {
            EventPopup.GIVEAWAY ->
                Box(
                    Modifier.align(Alignment.TopCenter)
                        .heightIn(max = popupHeight)
                        .verticalScroll(rememberScrollState())
                ) {
                    GiveawayPopupOverlay(isAuthenticated = authenticated)
                }
            EventPopup.CAPTAINS ->
                if (state.active(now) && !presentation.captainsScreenVisible) {
                    PirateCard(
                        Modifier.align(Alignment.TopCenter)
                            .padding(16.dp)
                            .widthIn(max = 600.dp)
                            .fillMaxWidth()
                            .heightIn(max = popupHeight),
                        highlighted = true,
                        cornerRadius = 20.dp,
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.HowToVote, null, tint = PirateTheme.accentColor)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Captain voting",
                                color = Color.White,
                                fontFamily = PirateTheme.fontFamily,
                                fontSize = 26.sp,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = { repository.popups.minimize(EventPopup.CAPTAINS) }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    "Minimize captain voting",
                                    tint = PirateTheme.accentColor,
                                )
                            }
                        }
                        Column(
                            Modifier.verticalScroll(rememberScrollState())
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            ElectionDeadline(state, now)
                            Text(
                                if (state.balance != null)
                                    "${state.balance} voting tokens available"
                                else if (state.signedIn && !state.authRequired)
                                    "Your voting tokens are loading…"
                                else "Sign in with Twitch to use your voting tokens.",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f),
                            )
                            if (!state.signedIn || state.authRequired)
                                TextButton(onClick = onSignIn) { Text("Sign in with Twitch") }
                            state.candidates.forEach { captain ->
                                CaptainCandidateCard(captain, state, compact = true) {
                                    repository.chooseCaptain(captain)
                                }
                            }
                            CaptainsMessages(state, repository, onSignIn)
                            OutlinedButton(
                                onClick = onOpenCaptains,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("Open Captains")
                            }
                        }
                    }
                }
            null -> Unit
        }
    }
    if (
        state.confirmationOpen &&
            presentation.expanded != EventPopup.GIVEAWAY &&
            (presentation.expanded == EventPopup.CAPTAINS || presentation.captainsScreenVisible)
    ) {
        CaptainVoteDialog(repository, state, onSignIn)
    }
}
