package com.swiftyspiffy.burkeblackapp

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.swiftyspiffy.burkeblackapp.auth.SessionIdentity
import com.swiftyspiffy.burkeblackapp.data.EventPopup
import com.swiftyspiffy.burkeblackapp.data.EventPopupCoordinator
import com.swiftyspiffy.burkeblackapp.data.captains.*
import com.swiftyspiffy.burkeblackapp.data.models.*
import com.swiftyspiffy.burkeblackapp.data.websocket.GiveawayWebSocketManager
import com.swiftyspiffy.burkeblackapp.ui.screens.CaptainsScreen
import com.swiftyspiffy.burkeblackapp.ui.screens.EventPopupsOverlay
import com.swiftyspiffy.burkeblackapp.ui.screens.LocalCaptainsTime
import com.swiftyspiffy.burkeblackapp.ui.theme.LocalPirateTheme
import com.swiftyspiffy.burkeblackapp.ui.theme.PirateGold
import java.io.File
import java.io.IOException
import java.time.Instant
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * All balances and vote writes are local fixtures. Only public portrait assets may load remotely.
 */
@RunWith(AndroidJUnit4::class)
class CaptainsUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val popups = EventPopupCoordinator.instance
    private lateinit var repository: CaptainsRepository
    private val source = Source()
    private val fullScreenState = mutableStateOf(true)
    private val pirateState = mutableStateOf(true)
    private val fontState = mutableFloatStateOf(1f)
    private val tabletState = mutableStateOf(false)
    private val signedInState = mutableStateOf(false)

    private class Store : CaptainsStore {
        var shown: String? = null
        var pending: CaptainVoteRequest? = null

        override suspend fun lastCaptainElection() = shown

        override suspend fun markCaptainElection(key: String) {
            shown = key
        }

        override suspend fun pendingCaptainVote(userId: String) = pending

        override suspend fun saveCaptainVote(userId: String, request: CaptainVoteRequest?) {
            pending = request
        }
    }

    private class Source : CaptainsDataSource {
        val crew =
            listOf(
                Captain(
                    "captain-one",
                    "Captain",
                    "The Bold",
                    "The Captain leads this band of pirates into new adventures.",
                    "1664071d-0c3d-4052-90fd-42abcb3c29dc",
                    order = 1,
                    votes = 128,
                    rank = 1,
                ),
                Captain(
                    "captain-two",
                    "Salty",
                    "The Brute",
                    "Salty is the muscle and enforcer of the group.",
                    "4032b37a-a260-43fb-9dc2-f07104336370",
                    order = 2,
                    votes = 96,
                    rank = 2,
                ),
                Captain(
                    "captain-three",
                    "Lass",
                    "The Composed",
                    "Lass keeps the crew focused on the task at hand.",
                    "6be5f14d-2754-452d-8980-fce953d2e99d",
                    order = 3,
                    votes = 72,
                    rank = 3,
                ),
                Captain(
                    "captain-four",
                    "Pete",
                    "The Prankster",
                    "Pete is an undead parrot with a gift for pranks.",
                    "481942d7-ea8d-4d05-b53a-cffafa7db008",
                    order = 4,
                    votes = 48,
                    rank = 4,
                ),
            )
        private val start = Instant.now().minusSeconds(86_400).toString()
        private val end = Instant.now().plusSeconds(3 * 86_400).toString()
        var balance = 7L
        var drop = false
        var applied = 0
        val requests = mutableListOf<CaptainVoteRequest>()
        private val receipts = mutableSetOf<String>()

        override suspend fun election() =
            CaptainElection(crew, CaptainSchedule(start, end, Instant.now().toString()))

        override suspend fun details() =
            CaptainDetails(
                crew,
                crew[1],
                listOf(CaptainRanking("one", "Fixture sailor", 42, 1)),
                listOf(CaptainRanking("two", "Fixture quartermaster", 128, 1)),
            )

        override suspend fun renew(session: SessionIdentity) {}

        override suspend fun balance(session: SessionIdentity) = balance

        override suspend fun vote(
            session: SessionIdentity,
            request: CaptainVoteRequest,
        ): CaptainVoteResult {
            requests.add(request)
            if (receipts.add(request.request_id)) {
                applied++
                balance -= request.amount
            }
            if (drop) throw IOException("Fixture response lost")
            return CaptainVoteResult("success")
        }
    }

    private fun launch(
        signedIn: Boolean = true,
        fullScreen: Boolean = true,
        pirate: Boolean = true,
        fontScale: Float = 1f,
    ) {
        fullScreenState.value = fullScreen
        pirateState.value = pirate
        fontState.floatValue = fontScale
        signedInState.value = signedIn
        repository =
            CaptainsRepository(
                source,
                Store(),
                CaptainsSocket {
                    flow {
                        emit(CaptainSocketEvent.CONNECTED)
                        awaitCancellation()
                    }
                },
                scope,
                popups,
            )
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            repository.setSession(
                if (signedIn) SessionIdentity("123", "synthetic-ui-session") else null
            )
            repository.setPopupsEnabled(true)
            repository.start()
        }
        val fixtureNow = System.currentTimeMillis()
        compose.setContent {
            val density = if (tabletState.value) 1f else LocalDensity.current.density
            val configuration =
                Configuration(LocalConfiguration.current).apply {
                    if (tabletState.value) {
                        screenWidthDp = 720
                        screenHeightDp = 1280
                        smallestScreenWidthDp = 720
                    }
                }
            CompositionLocalProvider(
                LocalPirateTheme provides pirateState.value,
                LocalConfiguration provides configuration,
                LocalCaptainsTime provides { fixtureNow },
                LocalDensity provides Density(density, fontState.floatValue),
            ) {
                MaterialTheme(
                    colorScheme =
                        darkColorScheme(
                            primary =
                                if (pirateState.value) PirateGold
                                else androidx.compose.ui.graphics.Color(0xFF90CAF9),
                            onPrimary = androidx.compose.ui.graphics.Color.Black,
                            background = androidx.compose.ui.graphics.Color.Black,
                        )
                ) {
                    Box(
                        Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black)
                    ) {
                        if (fullScreenState.value)
                            CaptainsScreen(repository, onBack = {}, onSignIn = {})
                        else Text("The Helm", modifier = Modifier.padding(16.dp))
                        EventPopupsOverlay(
                            repository,
                            signedInState.value,
                            onOpenCaptains = {},
                            onSignIn = {},
                        )
                    }
                }
            }
        }
        compose.waitUntil(30_000) {
            repository.state.value.schedule != null &&
                (!fullScreen || repository.state.value.detailsLoaded)
        }
        compose.waitForIdle()
    }

    private fun screenshot(name: String) {
        compose.waitForIdle()
        val target =
            File(
                InstrumentationRegistry.getInstrumentation()
                    .targetContext
                    .getExternalFilesDir("captains-validation"),
                "$name.png",
            )
        target.outputStream().use {
            InstrumentationRegistry.getInstrumentation()
                .uiAutomation
                .takeScreenshot()
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    @After
    fun cleanup() {
        compose.runOnIdle {
            if (::repository.isInitialized) repository.stop()
            GiveawayWebSocketManager.instance.disconnect(clearSession = true)
            popups.captainsScreen(false)
            popups.available(EventPopup.CAPTAINS, false)
            scope.cancel()
        }
    }

    private fun publicNetworkReadsAndPortraitsWorkOnAndroid() = runBlocking {
        if (InstrumentationRegistry.getArguments().getString("captainsLiveRead") != "true")
            return@runBlocking
        // Opt-in deployment smoke check: every request is public and read-only.
        val source = CaptainsClient.live()
        val election = withContext(Dispatchers.IO) { source.election() }
        val details = withContext(Dispatchers.IO) { source.details() }
        assertTrue(election.candidates.isNotEmpty())
        assertTrue(details.crew.isNotEmpty())
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        for (captain in this@CaptainsUiTest.source.crew) {
            val portrait =
                withTimeout(25_000) {
                    coil.Coil.imageLoader(context)
                        .execute(
                            coil.request.ImageRequest.Builder(context)
                                .data(CAPTAINS_BASE + "api/v1/assets/" + captain.portrait)
                                .build()
                        )
                }
            assertTrue(
                "Portrait request failed: ${(portrait as? coil.request.ErrorResult)?.throwable}",
                portrait is coil.request.SuccessResult,
            )
        }
    }

    private fun publicBrowsingProfilesAndBothRankings() {
        compose.onNodeWithText("Sign in").assertIsDisplayed()
        screenshot("captains-vote-guest")
        compose.onNodeWithTag("captains-content").performScrollToIndex(4)
        screenshot("captains-standings")
        compose.onNodeWithTag("captains-content").performScrollToIndex(0)
        compose.onNodeWithText("The crew").performClick()
        compose.onNodeWithText("The Bold").assertExists()
        compose.onNodeWithTag("captains-content").performScrollToIndex(4)
        screenshot("captains-profile")
        compose.onNodeWithContentDescription("Next captain").performClick()
        compose.onNodeWithText("The Brute").assertExists()
        compose.onNodeWithTag("captains-content").performScrollToIndex(0)
        compose.onNodeWithText("Rankings").performClick()
        compose.onNodeWithText("Fixture sailor").assertExists()
        compose.onNodeWithText("All time").performClick()
        compose.onNodeWithText("Fixture quartermaster").assertExists()
        screenshot("captains-rankings")
        assertTrue(source.requests.isEmpty())
    }

    private fun confirmationAndLostResponseRecoverWithoutAnotherSpend() {
        compose.onNodeWithText("Vote").performClick()
        compose.onNodeWithText("Captain", useUnmergedTree = false).performClick()
        compose.onNodeWithContentDescription("Spend one more token").performClick()
        compose.onNodeWithText("Cast 2 votes").assertIsDisplayed().assertIsEnabled()
        screenshot("captains-confirmation")
        source.drop = true
        compose.onNodeWithText("Cast 2 votes").performClick()
        compose.waitUntil(10_000) {
            repository.state.value.pending != null && !repository.state.value.sending
        }
        val checkVote = compose.onNode(hasText("Check vote") and hasAnyAncestor(isDialog()))
        checkVote.assertIsDisplayed()
        screenshot("captains-check-vote")
        source.drop = false
        checkVote.performClick()
        compose.waitUntil(10_000) {
            repository.state.value.pending == null && !repository.state.value.sending
        }
        assertEquals(1, source.applied)
        assertEquals(5L, source.balance)
        assertEquals(2, source.requests.size)
        assertEquals(source.requests[0], source.requests[1])
    }

    private fun newestPopupWinsAndTheVoteDraftSurvivesPreemption() {
        compose.runOnIdle { fullScreenState.value = false }
        compose.runOnIdle { repository.popups.present(EventPopup.CAPTAINS) }
        compose.onNodeWithText("Open Captains").assertExists()
        compose.onNodeWithText("Captain").performClick()
        compose.onNodeWithContentDescription("Spend one more token").performClick()
        injectGiveaway("newraffle")
        compose.onNodeWithText("GIVEAWAY").assertExists()
        compose.onNodeWithText("Open Captains").assertDoesNotExist()
        compose.onNodeWithText("Captain voting").performClick()
        compose.onNodeWithText("Cast 2 votes").assertExists()
        compose.runOnIdle { repository.closeConfirmation() }
        screenshot("captains-popup")
        compose.onNodeWithContentDescription("Minimize captain voting").performClick()
        compose.onNodeWithText("Captain voting").assertExists()
        compose.onNodeWithText("Giveaway Active").assertExists()
        screenshot("captains-minimized")
        injectGiveaway("rafflewinner")
        compose.onNodeWithText("GIVEAWAY").assertExists()
        compose.runOnIdle { repository.refresh() }
        compose.onNodeWithText("Open Captains").assertDoesNotExist()
        assertEquals(2, source.requests.size)
    }

    private fun standardThemeAndLargerTextRemainUsable() {
        compose.runOnIdle {
            popups.minimize(EventPopup.GIVEAWAY)
            fullScreenState.value = true
            pirateState.value = false
            fontState.floatValue = 1.5f
        }
        compose.onNodeWithText("The crew").assertIsDisplayed().performClick()
        compose.onNodeWithText("The Bold").assertExists()
        compose.onNodeWithTag("captains-content").performScrollToIndex(4)
        screenshot("captains-large-text-standard")
    }

    @Test
    fun captainsJourneyAcrossPhoneAndTabletLayouts() {
        publicNetworkReadsAndPortraitsWorkOnAndroid()
        launch(signedIn = false)
        publicBrowsingProfilesAndBothRankings()
        compose.runOnIdle {
            signedInState.value = true
            repository.setSession(SessionIdentity("123", "synthetic-ui-session"))
        }
        confirmationAndLostResponseRecoverWithoutAnotherSpend()
        newestPopupWinsAndTheVoteDraftSurvivesPreemption()
        standardThemeAndLargerTextRemainUsable()
        compose.runOnIdle {
            tabletState.value = true
            pirateState.value = true
            fontState.floatValue = 1f
        }
        compose.onNodeWithTag("captains-content").performScrollToIndex(0)
        compose.onNodeWithText("Vote").performClick()
        screenshot("captains-tablet-vote")
        compose.onNodeWithText("The crew").performClick()
        screenshot("captains-tablet-profile")
    }

    private fun injectGiveaway(event: String) {
        compose.runOnIdle {
            val handler =
                GiveawayWebSocketManager::class
                    .java
                    .getDeclaredMethod("handleMessage", String::class.java)
                    .apply { isAccessible = true }
            handler.invoke(
                GiveawayWebSocketManager.instance,
                """{"event":"$event","name":"Fixture giveaway","donator":"Fixture crew","winner":"Fixture viewer"}""",
            )
        }
        compose.waitForIdle()
    }
}
