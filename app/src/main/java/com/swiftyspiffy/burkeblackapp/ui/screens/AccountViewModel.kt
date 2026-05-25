package com.swiftyspiffy.burkeblackapp.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swiftyspiffy.burkeblackapp.auth.AuthCallbackData
import com.swiftyspiffy.burkeblackapp.auth.SessionManager
import com.swiftyspiffy.burkeblackapp.data.FeatureFlagService
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.DashboardData
import com.swiftyspiffy.burkeblackapp.data.models.FavSoundbyteData
import com.swiftyspiffy.burkeblackapp.data.models.LastSoundbyteData
import com.swiftyspiffy.burkeblackapp.data.models.LatestSubData
import com.swiftyspiffy.burkeblackapp.data.models.UserStatus
import com.swiftyspiffy.burkeblackapp.data.websocket.GiveawayWebSocketManager
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.swiftyspiffy.burkeblackapp.push.PushNotificationManager
import com.swiftyspiffy.burkeblackapp.widget.CrewStatsWidget
import com.swiftyspiffy.burkeblackapp.widget.WidgetDataStore
import androidx.glance.appwidget.updateAll
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    val sessionManager = SessionManager(application)

    val isLoggedIn = sessionManager.isLoggedIn.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val username = sessionManager.username.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val avatarUrl = sessionManager.avatarUrl.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val isModerator = sessionManager.isModerator.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Dashboard data
    private val _doubloons = MutableStateFlow(0)
    val doubloons: StateFlow<Int> = _doubloons

    private val _donations = MutableStateFlow(0.0)
    val donations: StateFlow<Double> = _donations

    private val _eventsDonations = MutableStateFlow(0.0)
    val eventsDonations: StateFlow<Double> = _eventsDonations

    private val _totalBits = MutableStateFlow(0)
    val totalBits: StateFlow<Int> = _totalBits

    private val _soundbyteCredits = MutableStateFlow(0)
    val soundbyteCredits: StateFlow<Int> = _soundbyteCredits

    private val _soundbyteSends = MutableStateFlow(0)
    val soundbyteSends: StateFlow<Int> = _soundbyteSends

    private val _giveawaysEntered = MutableStateFlow(0)
    val giveawaysEntered: StateFlow<Int> = _giveawaysEntered

    private val _giveawaysWon = MutableStateFlow(0)
    val giveawaysWon: StateFlow<Int> = _giveawaysWon

    private val _giveawaysDonated = MutableStateFlow(0)
    val giveawaysDonated: StateFlow<Int> = _giveawaysDonated

    private val _latestSub = MutableStateFlow<LatestSubData?>(null)
    val latestSub: StateFlow<LatestSubData?> = _latestSub

    private val _favSoundbyte = MutableStateFlow<FavSoundbyteData?>(null)
    val favSoundbyte: StateFlow<FavSoundbyteData?> = _favSoundbyte

    private val _lastSoundbyte = MutableStateFlow<LastSoundbyteData?>(null)
    val lastSoundbyte: StateFlow<LastSoundbyteData?> = _lastSoundbyte

    // User status
    private val _follows = MutableStateFlow(false)
    val follows: StateFlow<Boolean> = _follows

    private val _subscribed = MutableStateFlow(false)
    val subscribed: StateFlow<Boolean> = _subscribed

    private val _subTier = MutableStateFlow<String?>(null)
    val subTier: StateFlow<String?> = _subTier

    private val _userRole = MutableStateFlow("")
    val userRole: StateFlow<String> = _userRole

    private val _isSubGifter = MutableStateFlow(false)
    val isSubGifter: StateFlow<Boolean> = _isSubGifter

    private val _isBitsSender = MutableStateFlow(false)
    val isBitsSender: StateFlow<Boolean> = _isBitsSender

    private val _isDonator = MutableStateFlow(false)
    val isDonator: StateFlow<Boolean> = _isDonator

    private val _followedAt = MutableStateFlow<String?>(null)
    val followedAt: StateFlow<String?> = _followedAt

    init {
        viewModelScope.launch {
            sessionManager.isLoggedIn.collect { loggedIn ->
                if (loggedIn) {
                    refreshDashboard()
                    val token = sessionManager.token.first()
                    if (token != null) {
                        // Load feature flags
                        FeatureFlagService.load(token)
                        // Connect WebSocket if not already connected
                        val name = username.value
                        if (!GiveawayWebSocketManager.instance.isConnected.value) {
                            AppLogger.log("WebSocket: auto-connecting on session restore")
                            GiveawayWebSocketManager.instance.connect(token, name)
                        }
                    }
                }
            }
        }
    }

    fun handleAuthCallback(data: AuthCallbackData) {
        viewModelScope.launch {
            _isLoading.value = true
            AppLogger.logSensitive("Auth", "callback received for user: ${data.username}")
            try {
                sessionManager.saveSession(
                    token = data.token,
                    userId = data.userId,
                    username = data.username,
                    avatarUrl = data.avatarUrl
                )
                sessionManager.clearForceVerify()
                refreshDashboard()

                // Load feature flags
                FeatureFlagService.load(data.token)

                // Connect WebSocket
                AppLogger.log("WebSocket connecting after auth")
                GiveawayWebSocketManager.instance.connect(data.token, data.username)

                // Register FCM token for push notifications
                val fcmToken = PushNotificationManager.getFcmToken()
                if (fcmToken != null) {
                    PushNotificationManager.registerWithBackend(data.token, fcmToken)
                }
            } catch (e: Exception) {
                AppLogger.log("Auth callback failed: ${e.message}")
                _errorMessage.value = e.message
            }
            _isLoading.value = false
        }
    }

    fun handleReviewerLogin(token: String, username: String, avatarUrl: String?, isModerator: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            AppLogger.logSensitive("Auth", "Reviewer login: $username, isMod=$isModerator")
            try {
                sessionManager.saveSession(
                    token = token,
                    userId = "",
                    username = username,
                    avatarUrl = avatarUrl,
                    isModerator = isModerator
                )
                sessionManager.clearForceVerify()
                refreshDashboard()
                GiveawayWebSocketManager.instance.connect(token, username)
            } catch (e: Exception) {
                AppLogger.log("Reviewer login failed: ${e.message}")
                _errorMessage.value = e.message
            }
            _isLoading.value = false
        }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            val token = sessionManager.getToken() ?: return@launch
            AppLogger.log("Refreshing dashboard")
            try {
                val dashResponse = ApiClient.api.fetchDashboard("Bearer $token")
                if (dashResponse.success && dashResponse.data != null) {
                    updateDashboard(dashResponse.data)
                }

                val statusResponse = ApiClient.api.fetchUserStatus("Bearer $token")
                if (statusResponse.success && statusResponse.data != null) {
                    updateUserStatus(statusResponse.data)
                }
                AppLogger.log("Dashboard refreshed successfully")
            } catch (e: Exception) {
                AppLogger.log("Dashboard refresh failed: ${e.message}")
            }
        }
    }

    private fun updateDashboard(data: DashboardData) {
        _doubloons.value = data.doubloons
        _donations.value = data.donations
        _eventsDonations.value = data.eventsDonations ?: 0.0
        _totalBits.value = data.totalBits ?: 0
        _soundbyteCredits.value = data.soundbyteCredits
        _soundbyteSends.value = data.soundbyteSends ?: 0
        _giveawaysEntered.value = data.giveawaysEntered
        _giveawaysWon.value = data.giveawaysWon
        _giveawaysDonated.value = data.giveawaysDonated
        _latestSub.value = data.latestSub
        _favSoundbyte.value = data.favSoundbyte
        _lastSoundbyte.value = data.lastSoundbyte

        data.avatarUrl?.let {
            viewModelScope.launch { sessionManager.updateAvatarUrl(it) }
        }

        val context = getApplication<Application>()
        val followMonths = data.followDate?.let {
            try {
                val followInstant = Instant.parse(it)
                val now = Instant.now()
                ChronoUnit.MONTHS.between(
                    followInstant.atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1),
                    now.atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1)
                ).toInt().coerceAtLeast(0)
            } catch (_: Exception) { 0 }
        } ?: 0
        val subMonths = data.latestSub?.cumulativeMonths ?: 0
        WidgetDataStore.setCrewStats(context, data.doubloons, data.soundbyteCredits, followMonths, subMonths)
        viewModelScope.launch { CrewStatsWidget().updateAll(context) }
    }

    private suspend fun updateUserStatus(status: UserStatus) {
        _follows.value = status.follows
        _subscribed.value = status.subscribed
        _subTier.value = status.subTier
        _userRole.value = status.userRole
        _isSubGifter.value = status.isSubGifter ?: false
        _isBitsSender.value = status.isBitsSender ?: false
        _isDonator.value = _donations.value > 0
        _followedAt.value = status.followedAt

        val isMod = status.isModerator ?: false
        sessionManager.updateModerator(isMod)
    }

    fun updateSoundbyteCredits(newCredits: Int) {
        _soundbyteCredits.value = newCredits
    }

    fun logout() {
        viewModelScope.launch {
            AppLogger.log("User logged out")
            // Unregister push token before clearing session
            val token = sessionManager.token.first()
            val fcmToken = PushNotificationManager.getFcmToken()
            if (token != null && fcmToken != null) {
                PushNotificationManager.unregisterFromBackend(token, fcmToken)
            }
            GiveawayWebSocketManager.instance.disconnect()
            FeatureFlagService.clear()
            WidgetDataStore.clearCrewStats(getApplication())
            viewModelScope.launch { CrewStatsWidget().updateAll(getApplication()) }
            sessionManager.clearSession()
            // Reset all state
            _doubloons.value = 0
            _donations.value = 0.0
            _totalBits.value = 0
            _soundbyteCredits.value = 0
            _giveawaysEntered.value = 0
            _giveawaysWon.value = 0
            _giveawaysDonated.value = 0
            _latestSub.value = null
            _favSoundbyte.value = null
            _lastSoundbyte.value = null
            _follows.value = false
            _subscribed.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
