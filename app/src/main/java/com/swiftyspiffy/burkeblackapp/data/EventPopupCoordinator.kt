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
                expanded = if (!available && it.expanded == kind) null else it.expanded,
            )
        }
    }

    /** Only a new alert or an explicit user action calls this, never a tally refresh. */
    fun present(kind: EventPopup) {
        _state.update {
            it.copy(
                available = it.available + kind,
                expanded =
                    if (kind == EventPopup.CAPTAINS && it.captainsScreenVisible) null else kind,
            )
        }
    }

    fun minimize(kind: EventPopup) {
        _state.update { if (it.expanded == kind) it.copy(expanded = null) else it }
    }

    fun captainsScreen(visible: Boolean) {
        _state.update {
            it.copy(
                captainsScreenVisible = visible,
                expanded = if (visible && it.expanded == EventPopup.CAPTAINS) null else it.expanded,
            )
        }
    }
}
