package com.swiftyspiffy.burkeblackapp.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class EventPopup {
    GIVEAWAY,
    CAPTAINS,
}

data class EventPopupState(
    val expanded: EventPopup? = null,
    val available: Set<EventPopup> = emptySet(),
    val captainsScreenVisible: Boolean = false,
)

/** Presentation only: election and giveaway state remain owned by their existing services. */
class EventPopupCoordinator {
    companion object {
        val instance = EventPopupCoordinator()
    }

    private val _state = MutableStateFlow(EventPopupState())
    val state = _state.asStateFlow()

    fun available(kind: EventPopup, available: Boolean) {
        _state.update {
            it.copy(
                available = if (available) it.available + kind else it.available - kind,
                // Captains remains a public browsing page after an election closes.
                expanded =
                    if (!available && kind == EventPopup.GIVEAWAY && it.expanded == kind) null
                    else it.expanded,
            )
        }
    }

    /** Only a new alert or an explicit user action calls this, never a tally refresh. */
    fun present(kind: EventPopup) {
        _state.update {
            if (kind == EventPopup.CAPTAINS && it.captainsScreenVisible) return@update it
            it.copy(available = it.available + kind, expanded = kind)
        }
    }

    fun minimize(kind: EventPopup) {
        _state.update { if (it.expanded == kind) it.copy(expanded = null) else it }
    }

    fun captainsScreen(visible: Boolean) {
        _state.update { it.copy(captainsScreenVisible = visible) }
    }
}
