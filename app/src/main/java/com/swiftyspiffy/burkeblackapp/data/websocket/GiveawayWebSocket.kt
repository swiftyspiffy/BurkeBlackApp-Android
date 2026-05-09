package com.swiftyspiffy.burkeblackapp.data.websocket

import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import com.swiftyspiffy.burkeblackapp.data.AppSettings

data class ActiveGiveaway(
    var id: Int? = null,
    val name: String,
    val donator: String,
    var isEntered: Boolean = false,
    var phase: GiveawayPhase = GiveawayPhase.ENTRY,
    var winner: String? = null,
    var totalEntries: String? = null,
    var timeRemaining: Int? = null,
    var countdownStartMillis: Long? = null
)

enum class GiveawayPhase {
    ENTRY, WINNER, CLAIMED
}

class GiveawayWebSocketManager private constructor() {
    companion object {
        val instance = GiveawayWebSocketManager()
        private const val WS_URL = "wss://socketserver.burkeblack.tv/giveaway"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var webSocket: WebSocket? = null
    private var isIntentionalDisconnect = false
    private var token: String? = null
    private var username: String? = null
    private var isReconnecting = false
    var appSettings: AppSettings? = null
        set(value) {
            field = value
            // Observe the popups enabled setting
            value?.let { settings ->
                scope.launch {
                    settings.giveawayPopupsEnabledFlow.collect { enabled ->
                        _popupsEnabled = enabled
                    }
                }
            }
        }
    private var _popupsEnabled = true

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _activeGiveaway = MutableStateFlow<ActiveGiveaway?>(null)
    val activeGiveaway: StateFlow<ActiveGiveaway?> = _activeGiveaway

    // Track if user dismissed the popup so they can bring it back
    private val _isDismissed = MutableStateFlow(false)
    val isDismissed: StateFlow<Boolean> = _isDismissed

    // Message log for debugging
    data class WsMessage(val timestamp: Long, val text: String)
    private val _messageLog = MutableStateFlow<List<WsMessage>>(emptyList())
    val messageLog: StateFlow<List<WsMessage>> = _messageLog
    private val maxMessages = 100

    private fun logMessage(text: String) {
        val redacted = text.replace("socketserver.burkeblack.tv", "[redacted-host]")
        val current = _messageLog.value.toMutableList()
        current.add(0, WsMessage(System.currentTimeMillis(), redacted))
        if (current.size > maxMessages) current.removeAt(current.lastIndex)
        _messageLog.value = current
    }

    fun connect(token: String, username: String? = null) {
        this.token = token
        this.username = username
        isIntentionalDisconnect = false
        AppLogger.log("WebSocket connecting")
        doConnect()
    }

    fun reconnectIfNeeded() {
        if (_isConnected.value || isReconnecting) return
        val t = token ?: return
        AppLogger.log("WebSocket: reconnecting")
        isIntentionalDisconnect = false
        doConnect()
    }

    fun disconnect() {
        isIntentionalDisconnect = true
        AppLogger.log("WebSocket disconnecting")
        webSocket?.close(1000, "Going away")
        webSocket = null
        _isConnected.value = false
    }

    private fun doConnect() {
        isReconnecting = true
        // Cancel old socket — use cancel() instead of close() to avoid onClosed callback
        val old = webSocket
        webSocket = null
        old?.cancel()

        val client = OkHttpClient.Builder()
            .pingInterval(30, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder().url(WS_URL).build()

        val newSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                scope.launch {
                    isReconnecting = false
                    _isConnected.value = true
                    logMessage("[CONNECTED]")
                    AppLogger.log("WebSocket connected")
                }
                scope.launch { checkForActiveGiveaway() }
            }

            override fun onMessage(ws: WebSocket, text: String) {
                scope.launch {
                    logMessage(text)
                    handleMessage(text)
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                scope.launch {
                    // Ignore callbacks from stale sockets
                    if (ws != webSocket) return@launch
                    isReconnecting = false
                    _isConnected.value = false
                    logMessage("[ERROR] ${t.message}")
                    AppLogger.log("WebSocket error: ${t.message}")
                    if (!isIntentionalDisconnect) {
                        scheduleReconnect()
                    }
                }
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                scope.launch {
                    // Ignore callbacks from stale sockets
                    if (ws != webSocket) return@launch
                    _isConnected.value = false
                    logMessage("[CLOSED] $reason")
                    AppLogger.log("WebSocket closed: $reason")
                    if (!isIntentionalDisconnect) {
                        scheduleReconnect()
                    }
                }
            }
        })
        webSocket = newSocket
    }

    private fun scheduleReconnect() {
        scope.launch {
            AppLogger.log("WebSocket reconnecting in 5s")
            delay(5000)
            if (!isIntentionalDisconnect) {
                doConnect()
            }
        }
    }

    private fun handleMessage(text: String) {
        try {
            val json = JSONObject(text)
            val event = json.optString("event", "")

            // Check if giveaway popups are disabled for newraffle events
            if (event == "newraffle" && !_popupsEnabled) {
                AppLogger.log("Giveaway: popup suppressed (disabled in settings)")
                return
            }

            // New giveaway and winner events resurface the popup
            // But NOT raffleclaim — once claimed, if dismissed it should stay dismissed
            if (event in listOf("newraffle", "rafflewinner")) {
                _isDismissed.value = false
            }

            when (event) {
                "newraffle" -> {
                    val name = json.optString("name", "Giveaway")
                    val donator = json.optString("donator", "Unknown")
                    AppLogger.log("Giveaway: new raffle '$name' from $donator")
                    _activeGiveaway.value = ActiveGiveaway(
                        name = name,
                        donator = donator,
                        phase = GiveawayPhase.ENTRY
                    )
                    scope.launch { fetchActiveGiveawayId() }
                }
                "giveawayupdate" -> {
                    val entries = json.optString("totalEntries", null)
                    _activeGiveaway.value = _activeGiveaway.value?.copy(totalEntries = entries)
                }
                "rafflewinner" -> {
                    val winner = json.optString("winner", "").ifEmpty { null }
                    val entries = json.optString("totalEntries", "").ifEmpty { null }
                    AppLogger.log("Giveaway: winner is $winner")
                    val current = _activeGiveaway.value
                    if (current != null) {
                        _activeGiveaway.value = current.copy(
                            phase = GiveawayPhase.WINNER,
                            winner = winner,
                            totalEntries = entries ?: current.totalEntries
                        )
                    } else {
                        // Winner event arrived without a prior newraffle (e.g. after reconnect)
                        val name = json.optString("name", "Giveaway")
                        _activeGiveaway.value = ActiveGiveaway(
                            name = name,
                            donator = "",
                            phase = GiveawayPhase.WINNER,
                            winner = winner,
                            totalEntries = entries
                        )
                        scope.launch { checkForActiveGiveaway() }
                    }
                }
                "raffleclaim" -> {
                    val winner = json.optString("winner", null)
                    AppLogger.log("Giveaway: claimed by $winner")
                    _activeGiveaway.value = _activeGiveaway.value?.copy(
                        phase = GiveawayPhase.CLAIMED,
                        winner = winner
                    )
                }
                "raffleerror" -> {
                    AppLogger.log("Giveaway: raffle error")
                    _activeGiveaway.value = null
                }
            }
        } catch (e: Exception) {
            AppLogger.log("WebSocket message parse error: ${e.message}")
        }
    }

    private suspend fun checkForActiveGiveaway() {
        val token = this.token ?: return
        try {
            val response = ApiClient.api.fetchActiveGiveaway("Bearer $token")
            val data = response.data ?: return
            val id = data["id"]?.jsonPrimitive?.int ?: return
            val state = data["state"]?.jsonPrimitive?.content ?: ""
            val name = data["name"]?.jsonPrimitive?.content ?: "Giveaway"
            val donator = data["donator"]?.jsonPrimitive?.content ?: ""
            val isEntered = data["is_entered"]?.jsonPrimitive?.content?.toBooleanStrictOrNull() ?: false
            val winner = data["winner"]?.jsonPrimitive?.content
            val timeRemaining = data["time_remaining"]?.jsonPrimitive?.content?.toIntOrNull()
            val claimDuration = data["claim_duration"]?.jsonPrimitive?.content?.toIntOrNull()

            // Only show if giveaway is actively running
            val isActive = when (state) {
                "entry" -> true
                "claim" -> true // Claim window open — show so winner can claim/pass
                "winner" -> timeRemaining != null && timeRemaining > 0
                else -> false
            }

            if (isActive) {
                val phase = when (state) {
                    "entry" -> GiveawayPhase.ENTRY
                    "claim" -> GiveawayPhase.WINNER // "claim" state = winner drawn, waiting for claim
                    "claimed" -> GiveawayPhase.CLAIMED
                    else -> GiveawayPhase.WINNER
                }
                // For claim state, use claim_duration as the countdown
                val countdown = when (state) {
                    "claim" -> claimDuration
                    else -> timeRemaining
                }
                _activeGiveaway.value = ActiveGiveaway(
                    id = id,
                    name = name,
                    donator = donator,
                    isEntered = isEntered,
                    phase = phase,
                    winner = winner,
                    timeRemaining = if (countdown != null && countdown > 0) countdown else null,
                    countdownStartMillis = if (countdown != null && countdown > 0) System.currentTimeMillis() else null
                )
                AppLogger.log("Active giveaway found: $name ($state), winner=$winner")
            }
        } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
    }

    private suspend fun fetchActiveGiveawayId() {
        checkForActiveGiveaway()
    }

    suspend fun enterGiveaway() {
        val token = this.token ?: return
        val id = _activeGiveaway.value?.id ?: return
        try {
            val body = buildJsonObject { put("giveaway_id", id) }
            ApiClient.api.enterGiveaway("Bearer $token", body)
            _activeGiveaway.value = _activeGiveaway.value?.copy(isEntered = true)
        } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
    }

    suspend fun leaveGiveaway() {
        val token = this.token ?: return
        val id = _activeGiveaway.value?.id ?: return
        try {
            val body = buildJsonObject { put("giveaway_id", id) }
            ApiClient.api.leaveGiveaway("Bearer $token", body)
            _activeGiveaway.value = _activeGiveaway.value?.copy(isEntered = false)
        } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
    }

    private val _claimError = MutableStateFlow<String?>(null)
    val claimError: StateFlow<String?> = _claimError

    suspend fun claimGiveaway() {
        val token = this.token ?: return
        val id = _activeGiveaway.value?.id ?: return
        _claimError.value = null
        try {
            val body = buildJsonObject { put("giveaway_id", id) }
            val response = ApiClient.api.claimGiveaway("Bearer $token", body)
            if (response.success) {
                AppLogger.log("Giveaway: claimed giveaway $id")
                _activeGiveaway.value = _activeGiveaway.value?.copy(phase = GiveawayPhase.CLAIMED)
            } else {
                val error = response.error ?: "Claim failed"
                AppLogger.log("Giveaway claim error: $error")
                _claimError.value = error
            }
        } catch (e: Exception) {
            AppLogger.log("Error claiming giveaway: ${e.message}")
            _claimError.value = "Could not reach the server"
        }
    }

    suspend fun passGiveaway() {
        val token = this.token ?: return
        val id = _activeGiveaway.value?.id ?: return
        try {
            val body = buildJsonObject { put("giveaway_id", id) }
            val response = ApiClient.api.passGiveaway("Bearer $token", body)
            if (response.success) {
                AppLogger.log("Giveaway: passed on giveaway $id")
                // Bot will redraw — clear giveaway and wait for new rafflewinner event
                _activeGiveaway.value = _activeGiveaway.value?.copy(
                    phase = GiveawayPhase.ENTRY,
                    winner = null
                )
            } else {
                AppLogger.log("Giveaway pass error: ${response.error}")
            }
        } catch (e: Exception) { AppLogger.log("Error passing giveaway: ${e.message}") }
    }

    fun isCurrentUserWinner(): Boolean {
        val winner = _activeGiveaway.value?.winner ?: return false
        val user = username ?: return false
        return winner.equals(user, ignoreCase = true)
    }

    fun dismissGiveaway() {
        _isDismissed.value = true
    }

    fun showGiveaway() {
        _isDismissed.value = false
    }

    fun hasActiveGiveaway(): Boolean {
        return _activeGiveaway.value != null
    }
}
