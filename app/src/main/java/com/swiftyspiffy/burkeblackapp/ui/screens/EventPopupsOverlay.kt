package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swiftyspiffy.burkeblackapp.data.EventPopup
import com.swiftyspiffy.burkeblackapp.data.captains.CaptainsRepository
import com.swiftyspiffy.burkeblackapp.data.websocket.GiveawayPhase
import com.swiftyspiffy.burkeblackapp.data.websocket.GiveawayWebSocketManager

@Composable
fun EventPopupsOverlay(
    repository: CaptainsRepository,
    authenticated: Boolean,
    onSignIn: () -> Unit,
) {
    val presentation by repository.popups.state.collectAsStateWithLifecycle()
    val state by repository.state.collectAsStateWithLifecycle()
    val giveaway by GiveawayWebSocketManager.instance.activeGiveaway.collectAsStateWithLifecycle()
    val pageState = rememberSaveableStateHolder()
    val now = captainsClock()
    BackHandler(enabled = presentation.expanded == EventPopup.CAPTAINS) {
        repository.popups.minimize(EventPopup.CAPTAINS)
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        when (presentation.expanded) {
            EventPopup.GIVEAWAY ->
                Box(
                    Modifier.align(Alignment.TopCenter)
                        .heightIn(max = maxHeight * 0.78f)
                        .verticalScroll(rememberScrollState())
                ) {
                    GiveawayPopupOverlay(isAuthenticated = authenticated)
                }
            EventPopup.CAPTAINS ->
                pageState.SaveableStateProvider("captains") {
                    CaptainsScreen(
                        repository,
                        onBack = { repository.popups.minimize(EventPopup.CAPTAINS) },
                        onSignIn = onSignIn,
                    )
                }
            null -> Unit
        }
        if (
            giveaway != null &&
                giveaway?.phase != GiveawayPhase.CLAIMED &&
                presentation.expanded != EventPopup.GIVEAWAY
        ) {
            AssistChip(
                onClick = { GiveawayWebSocketManager.instance.showGiveaway() },
                label = { Text("Giveaway Active") },
                modifier =
                    Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 8.dp),
                colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF12110F)),
                leadingIcon = { Icon(Icons.Default.CardGiftcard, null, Modifier.size(16.dp)) },
            )
        }
        if (
            !presentation.captainsScreenVisible &&
                presentation.expanded != EventPopup.CAPTAINS &&
                state.active(now)
        ) {
            DraggableCaptainButton { repository.popups.present(EventPopup.CAPTAINS) }
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
