package com.swiftyspiffy.burkeblackapp.data.captains

import com.swiftyspiffy.burkeblackapp.auth.SessionIdentity
import com.swiftyspiffy.burkeblackapp.data.EventPopup
import com.swiftyspiffy.burkeblackapp.data.EventPopupCoordinator
import com.swiftyspiffy.burkeblackapp.data.models.*
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class CaptainsState(
    val candidates: List<Captain> = emptyList(),
    val schedule: CaptainSchedule? = null,
    val crew: List<Captain> = emptyList(),
    val winner: Captain? = null,
    val currentRanking: List<CaptainRanking> = emptyList(),
    val lifetimeRanking: List<CaptainRanking> = emptyList(),
    val balance: Long? = null,
    val signedIn: Boolean = false,
    val authRequired: Boolean = false,
    val loading: Boolean = true,
    val detailsLoaded: Boolean = false,
    val connected: Boolean = false,
    val error: String? = null,
    val detailsError: String? = null,
    val balanceError: String? = null,
    val storageError: String? = null,
    val notice: String? = null,
    val serverOffsetMillis: Long = 0,
    val pending: CaptainVoteRequest? = null,
    val pendingLoaded: Boolean = true,
    val sending: Boolean = false,
    val selectedId: String? = null,
    val amount: String = "1",
    val confirmationOpen: Boolean = false,
) {
    fun remainingMillis(now: Long): Long =
        schedule?.let { Instant.parse(it.end).toEpochMilli() - (now + serverOffsetMillis) } ?: 0

    fun active(now: Long): Boolean =
        candidates.isNotEmpty() &&
            remainingMillis(now) > 0 &&
            schedule?.let { now + serverOffsetMillis >= Instant.parse(it.start).toEpochMilli() } ==
                true

    fun validAmount(): Long? =
        amount.toLongOrNull()?.takeIf { it in 1..MAX_CAPTAIN_VOTES && it <= (balance ?: 0) }

    val selected: Captain?
        get() = candidates.find { it.id == selectedId }
}

/** Activity-owned state, shared by the full screen and global popup. Methods run on Main. */
class CaptainsRepository(
    private val source: CaptainsDataSource,
    private val store: CaptainsStore,
    private val socket: CaptainsSocket,
    private val scope: CoroutineScope,
    val popups: EventPopupCoordinator,
    private val clock: () -> Long = System::currentTimeMillis,
    private val requestId: () -> String = { UUID.randomUUID().toString() },
) {
    private val _state = MutableStateFlow(CaptainsState())
    val state = _state.asStateFlow()
    private var session: SessionIdentity? = null
    private var generation = 0
    private var foreground = false
    private var screenVisible = false
    private var popupsEnabled = false
    private var submitting = false
    private var refreshJob: Job? = null
    private var socketJob: Job? = null
    private var pollingJob: Job? = null
    private var debounceJob: Job? = null
    private var pendingJob: Job? = null
    private var refreshQueued = false
    private var lastRefresh = 0L
    private val presentation = Mutex()

    fun start() {
        if (foreground) return
        foreground = true
        refresh()
        connect()
        pollingJob =
            scope.launch {
                while (isActive) {
                    delay(1_000)
                    val state = _state.value
                    val due = clock() - lastRefresh >= 30_000
                    val expired = state.schedule != null && state.remainingMillis(clock()) <= 0
                    if (expired) popups.available(EventPopup.CAPTAINS, false)
                    val deadline = expired && clock() - lastRefresh >= 3_000
                    if (
                        deadline ||
                            (due &&
                                (screenVisible ||
                                    popups.state.value.expanded == EventPopup.CAPTAINS ||
                                    !state.connected))
                    )
                        refresh()
                }
            }
    }

    fun stop() {
        foreground = false
        refreshJob?.cancel()
        socketJob?.cancel()
        pollingJob?.cancel()
        debounceJob?.cancel()
        refreshQueued = false
        _state.update { it.copy(loading = false, connected = false) }
        // A dispatched vote continues in the ViewModel scope; its persisted receipt survives
        // process death.
    }

    fun setSession(value: SessionIdentity?) {
        if (value == session) return
        generation++
        session = value
        refreshJob?.cancel()
        socketJob?.cancel()
        pendingJob?.cancel()
        refreshQueued = false
        _state.update {
            it.copy(
                balance = null,
                signedIn = value != null,
                authRequired = value != null && !value.hasUserId,
                pending = null,
                pendingLoaded = value?.hasUserId != true,
                sending = submitting,
                balanceError = null,
                storageError = null,
                notice = null,
                selectedId = null,
                amount = "1",
                confirmationOpen = false,
            )
        }
        restorePending()
        if (foreground) {
            refresh()
            connect()
        }
    }

    private fun restorePending() {
        val who = session?.takeIf { it.hasUserId } ?: return
        if (pendingJob?.isActive == true) return
        val owner = generation
        pendingJob =
            scope.launch {
                try {
                    val pending = store.pendingCaptainVote(who.userId)
                    if (owner == generation)
                        _state.update {
                            it.copy(pending = pending, pendingLoaded = true, storageError = null)
                        }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    if (owner == generation)
                        _state.update {
                            it.copy(
                                storageError = "Couldn’t restore your previous vote. Try Refresh."
                            )
                        }
                }
            }
    }

    fun setScreenVisible(visible: Boolean) {
        screenVisible = visible
        popups.captainsScreen(visible)
        if (visible) refresh()
    }

    fun setPopupsEnabled(enabled: Boolean) {
        if (popupsEnabled == enabled) return
        popupsEnabled = enabled
        if (!enabled) popups.minimize(EventPopup.CAPTAINS) else considerPopup()
    }

    fun retry() {
        if (!_state.value.pendingLoaded) restorePending()
        if (_state.value.authRequired && session?.hasUserId == true) {
            _state.update { it.copy(authRequired = false) }
            if (foreground) connect()
        }
        refresh()
    }

    fun refresh() {
        if (!foreground) return
        if (refreshJob?.isActive == true) {
            refreshQueued = true
            return
        }
        refreshJob =
            scope.launch {
                do {
                    refreshQueued = false
                    val owner = generation
                    val who = session
                    lastRefresh = clock()
                    _state.update { it.copy(loading = true) }
                    supervisorScope {
                        launch {
                            try {
                                val election = source.election()
                                if (owner != generation || !foreground) return@launch
                                _state.update {
                                    it.copy(
                                        candidates = election.candidates,
                                        schedule = election.schedule,
                                        serverOffsetMillis =
                                            Instant.parse(election.schedule.now).toEpochMilli() -
                                                clock(),
                                        error = null,
                                    )
                                }
                                val active = _state.value.active(clock())
                                popups.available(EventPopup.CAPTAINS, active)
                                if (active) considerPopup()
                            } catch (e: CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                if (owner == generation && foreground)
                                    _state.update {
                                        it.copy(
                                            error = "Couldn’t refresh the election. Try Refresh."
                                        )
                                    }
                            }
                        }
                        if (who?.hasUserId == true && !_state.value.authRequired)
                            launch {
                                try {
                                    val balance = source.balance(who)
                                    if (owner == generation && foreground)
                                        _state.update {
                                            it.copy(balance = balance, balanceError = null)
                                        }
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (_: CaptainsAuthenticationException) {
                                    if (owner == generation) authenticationRequired()
                                } catch (_: Exception) {
                                    if (owner == generation && foreground)
                                        _state.update {
                                            it.copy(
                                                balance = null,
                                                balanceError =
                                                    "Couldn’t load your tokens. Try Refresh.",
                                            )
                                        }
                                }
                            }
                        if (screenVisible)
                            launch {
                                try {
                                    val details = source.details()
                                    if (owner == generation && foreground)
                                        _state.update {
                                            it.copy(
                                                crew = details.crew,
                                                winner = details.winner,
                                                currentRanking = details.current,
                                                lifetimeRanking = details.lifetime,
                                                detailsLoaded = true,
                                                detailsError = null,
                                            )
                                        }
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (_: Exception) {
                                    if (owner == generation && foreground)
                                        _state.update {
                                            it.copy(
                                                detailsError =
                                                    "Couldn’t refresh the crew and rankings. Try Refresh."
                                            )
                                        }
                                }
                            }
                    }
                    if (owner == generation) _state.update { it.copy(loading = false) }
                } while (refreshQueued && foreground)
            }
    }

    private fun considerPopup() {
        val key = _state.value.schedule?.key ?: return
        scope.launch {
            presentation.withLock {
                if (
                    !foreground ||
                        !popupsEnabled ||
                        !_state.value.active(clock()) ||
                        _state.value.schedule?.key != key
                )
                    return@withLock
                try {
                    if (store.lastCaptainElection() == key) return@withLock
                    store.markCaptainElection(key)
                    if (foreground && popupsEnabled && _state.value.schedule?.key == key)
                        popups.present(EventPopup.CAPTAINS)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    /* Keep manual access when the preference store is unavailable. */
                }
            }
        }
    }

    private fun authenticationRequired() {
        val reconnect = !_state.value.authRequired
        _state.update {
            it.copy(
                authRequired = true,
                balance = null,
                balanceError = "Sign in with Twitch to use your voting tokens.",
            )
        }
        if (reconnect && foreground) scope.launch { connect() }
    }

    private fun connect() {
        socketJob?.cancel()
        _state.update { it.copy(connected = false) }
        val owner = generation
        socketJob =
            scope.launch {
                var retryDelay = 2_000L
                while (isActive && foreground && owner == generation) {
                    try {
                        var who = session?.takeIf { it.hasUserId && !_state.value.authRequired }
                        if (who != null) {
                            try {
                                source.renew(who)
                            } catch (_: CaptainsAuthenticationException) {
                                authenticationRequired()
                                who = null
                            }
                        }
                        var connected = false
                        socket.events(who).collect { event ->
                            if (owner != generation || !foreground) return@collect
                            _state.update { it.copy(connected = true) }
                            retryDelay = 2_000
                            if (!connected || event == CaptainSocketEvent.REFRESH) {
                                connected = true
                                if (debounceJob?.isActive != true)
                                    debounceJob =
                                        scope.launch {
                                            delay(300)
                                            refresh()
                                        }
                            }
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: CaptainsAuthenticationException) {
                        authenticationRequired()
                    } catch (_: Exception) {
                        /* Reconnect and use REST polling meanwhile. */
                    }
                    if (owner != generation || !foreground) return@launch
                    _state.update { it.copy(connected = false) }
                    delay(retryDelay)
                    retryDelay = (retryDelay * 2).coerceAtMost(30_000)
                }
            }
    }

    fun chooseCaptain(captain: Captain) {
        if (
            submitting ||
                _state.value.pending != null ||
                _state.value.candidates.none { it.id == captain.id }
        )
            return
        _state.update {
            it.copy(
                selectedId = captain.id,
                amount = if (it.selectedId == captain.id) it.amount else "1",
                confirmationOpen = true,
                notice = null,
            )
        }
    }

    fun amount(value: String) {
        if (!submitting && _state.value.pending == null) _state.update { it.copy(amount = value) }
    }

    fun closeConfirmation() {
        _state.update { it.copy(confirmationOpen = false) }
    }

    fun submitVote() {
        if (submitting) return
        val who = session?.takeIf { it.hasUserId } ?: return
        val current = _state.value
        if (!current.pendingLoaded || current.authRequired) return
        val request =
            current.pending
                ?: run {
                    val amount = current.validAmount() ?: return
                    if (!current.active(clock()) || current.selected == null) return
                    CaptainVoteRequest(current.selectedId!!, amount, requestId())
                }
        val owner = generation
        submitting = true
        _state.update { it.copy(sending = true, notice = null) }
        scope.launch {
            try {
                // Persist before dispatch. Neither HTTP nor lifecycle reconnection retries a vote.
                store.saveCaptainVote(who.userId, request)
                if (owner != generation) return@launch
                _state.update { it.copy(pending = request, storageError = null) }
                val result = source.vote(who, request)
                if (result.result !in setOf("success", "insufficient_tokens", "rejected"))
                    error("Unconfirmed vote")
                store.saveCaptainVote(who.userId, null)
                if (owner != generation) return@launch
                _state.update {
                    it.copy(
                        pending = null,
                        confirmationOpen = false,
                        selectedId = null,
                        amount = "1",
                        notice =
                            if (result.result == "success")
                                "${request.amount} ${if (request.amount == 1L) "vote" else "votes"} cast!"
                            else "Your balance or the election changed. Refresh and choose again.",
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: CaptainsAuthenticationException) {
                if (owner == generation) authenticationRequired()
            } catch (_: Exception) {
                if (owner == generation)
                    _state.update {
                        it.copy(
                            notice =
                                if (it.pending != null)
                                    "Vote status unconfirmed. Use Check vote to safely recover this request."
                                else "Couldn’t save your vote request. Nothing was sent. Try again."
                        )
                    }
            } finally {
                submitting = false
                _state.update { it.copy(sending = false) }
                if (owner == generation) refresh()
            }
        }
    }
}
