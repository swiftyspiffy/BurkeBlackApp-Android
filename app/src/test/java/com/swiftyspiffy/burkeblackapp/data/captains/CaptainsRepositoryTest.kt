package com.swiftyspiffy.burkeblackapp.data.captains

import com.swiftyspiffy.burkeblackapp.auth.SessionIdentity
import com.swiftyspiffy.burkeblackapp.data.EventPopup
import com.swiftyspiffy.burkeblackapp.data.EventPopupCoordinator
import com.swiftyspiffy.burkeblackapp.data.models.*
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CaptainsRepositoryTest {
    private val epoch = Instant.parse("2026-10-29T06:00:00Z").toEpochMilli()
    private val viewer = SessionIdentity("123", "synthetic-one")
    private val other = SessionIdentity("456", "synthetic-two")

    private class Store : CaptainsStore {
        var presented: String? = null
        var presentations = 0
        val pending = mutableMapOf<String, CaptainVoteRequest>()
        var failWrites = false

        override suspend fun lastCaptainElection() = presented

        override suspend fun markCaptainElection(key: String) {
            presented = key
            presentations++
        }

        override suspend fun pendingCaptainVote(userId: String) = pending[userId]

        override suspend fun saveCaptainVote(userId: String, request: CaptainVoteRequest?) {
            if (failWrites) throw IOException("Fixture storage failure")
            if (request == null) pending.remove(userId) else pending[userId] = request
        }
    }

    private inner class Source(private val now: () -> Long) : CaptainsDataSource {
        val captain = Captain("captain-one", "Captain", votes = 0, rank = 1)
        var start = epoch - 60_000
        var end = epoch + 3_600_000
        var serverOffset = 0L
        val balances = mutableMapOf("123" to 7L, "456" to 11L)
        val requests = mutableListOf<Pair<String, CaptainVoteRequest>>()
        private val receipts = mutableMapOf<String, CaptainVoteRequest>()
        var publicFailure = false
        var denyBalance = false
        var dropResponse = false
        var reject = false
        var gate: CompletableDeferred<Unit>? = null
        var elections = 0

        override suspend fun election(): CaptainElection {
            elections++
            if (publicFailure) throw IOException()
            return CaptainElection(
                listOf(captain),
                CaptainSchedule(
                    Instant.ofEpochMilli(start).toString(),
                    Instant.ofEpochMilli(end).toString(),
                    Instant.ofEpochMilli(now() + serverOffset).toString(),
                ),
            )
        }

        override suspend fun details() =
            CaptainDetails(listOf(captain), null, emptyList(), emptyList())

        override suspend fun renew(session: SessionIdentity) {}

        override suspend fun balance(session: SessionIdentity): Long {
            if (denyBalance) throw CaptainsAuthenticationException()
            return balances.getValue(session.userId)
        }

        override suspend fun vote(
            session: SessionIdentity,
            request: CaptainVoteRequest,
        ): CaptainVoteResult {
            requests.add(session.userId to request)
            gate?.await()
            if (reject) return CaptainVoteResult("insufficient_tokens")
            val key = session.userId + ":" + request.request_id
            if (key !in receipts) {
                receipts[key] = request
                balances[session.userId] = balances.getValue(session.userId) - request.amount
            } else check(receipts[key] == request)
            if (dropResponse) throw IOException("Fixture response lost after commit")
            return CaptainVoteResult("success")
        }
    }

    private class Socket : CaptainsSocket {
        val messages = MutableSharedFlow<CaptainSocketEvent>()
        val identities = mutableListOf<SessionIdentity?>()

        override fun events(session: SessionIdentity?) = flow {
            identities.add(session)
            emit(CaptainSocketEvent.CONNECTED)
            emitAll(messages)
        }
    }

    @Test
    fun doubleTapSubmitsOnceAndConfirmedVotesRefreshTheBalance() = runTest {
        val clock = { epoch + testScheduler.currentTime }
        val source = Source(clock)
        val store = Store()
        val socket = Socket()
        val repo =
            CaptainsRepository(
                source,
                store,
                socket,
                backgroundScope,
                EventPopupCoordinator(),
                clock,
            )
        repo.setSession(viewer)
        repo.start()
        runCurrent()
        source.gate = CompletableDeferred()
        repo.chooseCaptain(source.captain)
        repo.amount("2")
        repo.submitVote()
        repo.submitVote()
        runCurrent()
        assertEquals(1, source.requests.size)
        assertNotNull(store.pending[viewer.userId])
        source.gate!!.complete(Unit)
        runCurrent()
        assertEquals(5L, repo.state.value.balance)
        assertNull(repo.state.value.pending)
        assertNull(store.pending[viewer.userId])
        assertFalse(repo.state.value.confirmationOpen)
        repo.stop()
    }

    @Test
    fun ambiguousVotesSurviveRestartAndManualRecoveryUsesExactlyTheSameReceipt() = runTest {
        val clock = { epoch + testScheduler.currentTime }
        val source = Source(clock)
        val store = Store()
        var repo =
            CaptainsRepository(
                source,
                store,
                Socket(),
                backgroundScope,
                EventPopupCoordinator(),
                clock,
            )
        repo.setSession(viewer)
        repo.start()
        runCurrent()
        source.dropResponse = true
        repo.chooseCaptain(source.captain)
        repo.amount("2")
        repo.submitVote()
        runCurrent()
        val pending = requireNotNull(store.pending[viewer.userId])
        assertEquals(1, source.requests.size)
        assertEquals(5L, source.balances[viewer.userId])
        advanceTimeBy(31_000)
        runCurrent()
        assertEquals(1, source.requests.size) // Polling and reconnection cannot retry a write.
        repo.stop()
        repo =
            CaptainsRepository(
                source,
                store,
                Socket(),
                backgroundScope,
                EventPopupCoordinator(),
                clock,
            )
        repo.setSession(viewer)
        repo.start()
        runCurrent()
        assertEquals(pending, repo.state.value.pending)
        repo.chooseCaptain(source.captain)
        repo.amount("1")
        source.dropResponse = false
        repo.submitVote()
        runCurrent()
        assertEquals(2, source.requests.size)
        assertEquals(source.requests[0], source.requests[1])
        assertEquals(5L, repo.state.value.balance)
        assertNull(store.pending[viewer.userId])
        repo.stop()
    }

    @Test
    fun accountChangesCannotApplyOldBalancesOrVoteResponsesToTheNewViewer() = runTest {
        val clock = { epoch + testScheduler.currentTime }
        val source = Source(clock)
        val store = Store()
        val repo =
            CaptainsRepository(
                source,
                store,
                Socket(),
                backgroundScope,
                EventPopupCoordinator(),
                clock,
            )
        repo.setSession(viewer)
        repo.start()
        runCurrent()
        source.gate = CompletableDeferred()
        repo.chooseCaptain(source.captain)
        repo.submitVote()
        runCurrent()
        repo.setSession(other)
        runCurrent()
        assertEquals(11L, repo.state.value.balance)
        assertNull(repo.state.value.pending)
        source.gate!!.complete(Unit)
        runCurrent()
        assertEquals(11L, repo.state.value.balance)
        assertNull(repo.state.value.notice)
        assertNull(store.pending[viewer.userId])
        repo.setSession(null)
        runCurrent()
        assertFalse(repo.state.value.signedIn)
        assertNull(repo.state.value.balance)
        assertTrue(repo.state.value.candidates.isNotEmpty())
        repo.stop()
    }

    @Test
    fun popupIsShownOncePerElectionAcrossMinimizeRestartAndRollover() = runTest {
        val clock = { epoch + testScheduler.currentTime }
        val source = Source(clock)
        val store = Store()
        val popups = EventPopupCoordinator()
        var repo = CaptainsRepository(source, store, Socket(), backgroundScope, popups, clock)
        repo.setPopupsEnabled(true)
        repo.start()
        runCurrent()
        assertEquals(EventPopup.CAPTAINS, popups.state.value.expanded)
        popups.minimize(EventPopup.CAPTAINS)
        repo.refresh()
        runCurrent()
        assertNull(popups.state.value.expanded)
        assertEquals(1, store.presentations)
        repo.stop()
        repo = CaptainsRepository(source, store, Socket(), backgroundScope, popups, clock)
        repo.setPopupsEnabled(true)
        repo.start()
        runCurrent()
        assertNull(popups.state.value.expanded)
        assertTrue(EventPopup.CAPTAINS in popups.state.value.available)
        source.start += 1_000
        source.end += 1_000
        repo.refresh()
        runCurrent()
        assertEquals(EventPopup.CAPTAINS, popups.state.value.expanded)
        assertEquals(2, store.presentations)
        repo.stop()
    }

    @Test
    fun disabledPopupsAndFullScreenBrowsingStillAllowManualAccess() = runTest {
        val clock = { epoch + testScheduler.currentTime }
        val source = Source(clock)
        val popups = EventPopupCoordinator()
        val store = Store()
        val repo = CaptainsRepository(source, store, Socket(), backgroundScope, popups, clock)
        repo.setPopupsEnabled(false)
        repo.start()
        runCurrent()
        assertNull(popups.state.value.expanded)
        assertTrue(EventPopup.CAPTAINS in popups.state.value.available)
        popups.present(EventPopup.CAPTAINS)
        assertEquals(EventPopup.CAPTAINS, popups.state.value.expanded)
        repo.setScreenVisible(true)
        repo.setPopupsEnabled(true)
        runCurrent()
        assertNull(popups.state.value.expanded)
        assertTrue(repo.state.value.detailsLoaded)
        assertEquals(1, store.presentations)
        repo.setScreenVisible(false)
        repo.refresh()
        runCurrent()
        assertNull(popups.state.value.expanded)
        repo.stop()
    }

    @Test
    fun validationRejectionsAndUnavailableStorageNeverSpendUnexpectedTokens() = runTest {
        val clock = { epoch + testScheduler.currentTime }
        val source = Source(clock)
        val store = Store()
        val repo =
            CaptainsRepository(
                source,
                store,
                Socket(),
                backgroundScope,
                EventPopupCoordinator(),
                clock,
            )
        repo.setSession(viewer)
        repo.start()
        runCurrent()
        repo.chooseCaptain(source.captain)
        for (amount in listOf("0", "-1", "1.5", "8", "1000001", "bad", "")) {
            repo.amount(amount)
            repo.submitVote()
            runCurrent()
        }
        assertTrue(source.requests.isEmpty())
        store.failWrites = true
        repo.amount("1")
        repo.submitVote()
        runCurrent()
        assertTrue(source.requests.isEmpty())
        store.failWrites = false
        source.reject = true
        repo.submitVote()
        runCurrent()
        assertEquals(1, source.requests.size)
        assertEquals(7L, repo.state.value.balance)
        assertNull(repo.state.value.pending)
        repo.stop()
    }

    @Test
    fun publicFailuresKeepTheLastSnapshotAndPrivateFailuresFallBackToPublicUpdates() = runTest {
        val clock = { epoch + testScheduler.currentTime }
        val source = Source(clock)
        val socket = Socket()
        val repo =
            CaptainsRepository(
                source,
                Store(),
                socket,
                backgroundScope,
                EventPopupCoordinator(),
                clock,
            )
        repo.setSession(viewer)
        repo.start()
        runCurrent()
        source.publicFailure = true
        source.denyBalance = true
        repo.refresh()
        runCurrent()
        assertTrue(repo.state.value.candidates.isNotEmpty())
        assertNotNull(repo.state.value.error)
        assertTrue(repo.state.value.authRequired)
        assertNull(repo.state.value.balance)
        assertNull(socket.identities.last())
        repo.stop()
        runCurrent()
        assertEquals(0, socket.messages.subscriptionCount.value)
        val before = source.elections
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(before, source.elections)
    }

    @Test
    fun newestAlertsPreemptEachOtherWithoutOrdinaryRefreshesStealingFocus() = runTest {
        val clock = { epoch + testScheduler.currentTime }
        val source = Source(clock)
        val popups = EventPopupCoordinator()
        val repo = CaptainsRepository(source, Store(), Socket(), backgroundScope, popups, clock)
        popups.present(EventPopup.GIVEAWAY)
        repo.setPopupsEnabled(true)
        repo.start()
        runCurrent()
        assertEquals(EventPopup.CAPTAINS, popups.state.value.expanded)
        assertEquals(setOf(EventPopup.GIVEAWAY, EventPopup.CAPTAINS), popups.state.value.available)
        popups.present(EventPopup.GIVEAWAY)
        repo.refresh()
        runCurrent()
        assertEquals(EventPopup.GIVEAWAY, popups.state.value.expanded)
        popups.present(EventPopup.CAPTAINS)
        repo.setScreenVisible(true)
        assertNull(popups.state.value.expanded)
        repo.stop()
    }

    @Test
    fun serverTimeControlsExpiryAndPollingFindsTheNextElection() = runTest {
        val clock = { epoch + testScheduler.currentTime }
        val source = Source(clock)
        source.serverOffset = 600_000
        source.start = epoch + source.serverOffset - 60_000
        source.end = epoch + source.serverOffset + 3_000
        val popups = EventPopupCoordinator()
        val store = Store()
        val repo = CaptainsRepository(source, store, Socket(), backgroundScope, popups, clock)
        repo.setPopupsEnabled(true)
        repo.start()
        runCurrent()
        assertTrue(repo.state.value.active(clock()))
        assertEquals(3_000L, repo.state.value.remainingMillis(clock()))
        advanceTimeBy(4_000)
        runCurrent()
        assertFalse(repo.state.value.active(clock()))
        assertFalse(EventPopup.CAPTAINS in popups.state.value.available)
        source.start = source.end
        source.end += 3_600_000
        advanceTimeBy(3_000)
        runCurrent()
        assertEquals(EventPopup.CAPTAINS, popups.state.value.expanded)
        assertEquals(2, store.presentations)
        repo.stop()
    }
}
