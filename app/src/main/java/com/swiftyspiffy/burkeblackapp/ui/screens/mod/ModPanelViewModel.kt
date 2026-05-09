package com.swiftyspiffy.burkeblackapp.ui.screens.mod

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.*
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ModPanelViewModel(val token: String) : ViewModel() {
    private val auth get() = "Bearer $token"

    // Permissions & Settings
    private val _permissions = MutableStateFlow(ModPermissions())
    val permissions: StateFlow<ModPermissions> = _permissions

    private val _settings = MutableStateFlow<ModSettings?>(null)
    val settings: StateFlow<ModSettings?> = _settings

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    // Commands
    private val _commands = MutableStateFlow<List<ModCommand>>(emptyList())
    val commands: StateFlow<List<ModCommand>> = _commands

    // Timed Messages
    private val _timedMessages = MutableStateFlow<List<ModTimedMessage>>(emptyList())
    val timedMessages: StateFlow<List<ModTimedMessage>> = _timedMessages

    // Links
    private val _links = MutableStateFlow<List<ModLink>>(emptyList())
    val links: StateFlow<List<ModLink>> = _links

    // Timeout Words
    private val _timeoutWords = MutableStateFlow<List<ModTimeoutWord>>(emptyList())
    val timeoutWords: StateFlow<List<ModTimeoutWord>> = _timeoutWords
    private val _toCategories = MutableStateFlow<List<ModTOCategory>>(emptyList())
    val toCategories: StateFlow<List<ModTOCategory>> = _toCategories

    // Spoiler Words
    private val _spoilerWords = MutableStateFlow<List<ModSpoilerWord>>(emptyList())
    val spoilerWords: StateFlow<List<ModSpoilerWord>> = _spoilerWords

    // Viewer Lookup
    private val _viewerResult = MutableStateFlow<ViewerLookupResult?>(null)
    val viewerResult: StateFlow<ViewerLookupResult?> = _viewerResult

    // Giveaway Submissions
    private val _submissions = MutableStateFlow<List<GiveawaySubmission>>(emptyList())
    val submissions: StateFlow<List<GiveawaySubmission>> = _submissions

    private val _hiddenSubmissions = MutableStateFlow<List<GiveawaySubmission>>(emptyList())
    val hiddenSubmissions: StateFlow<List<GiveawaySubmission>> = _hiddenSubmissions

    private val _giveawayHistory = MutableStateFlow<List<GiveawayHistoryItem>>(emptyList())
    val giveawayHistory: StateFlow<List<GiveawayHistoryItem>> = _giveawayHistory

    // Soundbyte Library
    private val _sbLibrary = MutableStateFlow<List<ModSoundbyte>>(emptyList())
    val sbLibrary: StateFlow<List<ModSoundbyte>> = _sbLibrary
    private val _sbGenres = MutableStateFlow<List<SoundbyteGenre>>(emptyList())
    val sbGenres: StateFlow<List<SoundbyteGenre>> = _sbGenres

    private val _sbCredits = MutableStateFlow<List<ModSBCredit>>(emptyList())
    val sbCredits: StateFlow<List<ModSBCredit>> = _sbCredits

    private val _sbHistory = MutableStateFlow<List<ModSBHistoryItem>>(emptyList())
    val sbHistory: StateFlow<List<ModSBHistoryItem>> = _sbHistory

    init {
        loadInitial()
    }

    private fun loadInitial() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val permResp = ApiClient.api.fetchModPermissions(auth)
                if (permResp.success && permResp.data != null) {
                    _permissions.value = permResp.data
                    AppLogger.log("Mod: permissions loaded")
                }

                val settingsResp = ApiClient.api.fetchModSettings(auth)
                if (settingsResp.success && settingsResp.data != null) _settings.value = settingsResp.data
            } catch (e: Exception) {
                AppLogger.log("Mod: initial load failed: ${e.message}")
            }
            _isLoading.value = false
        }
    }

    fun clearMessage() { _message.value = null }

    // Quick Actions
    fun toggleSoundbytes() {
        viewModelScope.launch {
            try {
                val resp = ApiClient.api.toggleSoundbytes(auth)
                if (resp.success && resp.data != null) {
                    _settings.value = _settings.value?.copy(soundbytesEnabled = resp.data.soundbytesEnabled)
                    val state = if (resp.data.soundbytesEnabled) "enabled" else "disabled"
                    AppLogger.log("Mod: toggled soundbytes -> $state")
                    _message.value = if (resp.data.soundbytesEnabled) "Soundbytes enabled" else "Soundbytes disabled"
                }
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    fun toggleEnforcements() {
        viewModelScope.launch {
            try {
                val resp = ApiClient.api.toggleEnforcements(auth)
                if (resp.success && resp.data != null) {
                    _settings.value = _settings.value?.copy(enforcePunishments = resp.data.enforcePunishments)
                    val state = if (resp.data.enforcePunishments) "enabled" else "disabled"
                    AppLogger.log("Mod: toggled enforcements -> $state")
                    _message.value = if (resp.data.enforcePunishments) "Enforcements enabled" else "Enforcements disabled"
                }
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    fun alertBurke(message: String) {
        viewModelScope.launch {
            try {
                AppLogger.log("Mod: sending alert to Burke")
                val body = buildJsonObject {
                    put("message", message)
                }
                val resp = ApiClient.api.alertBurke(auth, body)
                if (resp.success && resp.data != null) {
                    _message.value = resp.data.message
                    AppLogger.log("Mod: alert sent successfully")
                } else {
                    _message.value = resp.error ?: "Failed to send alert"
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                _message.value = try {
                    val lenientJson = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                    val parsed = lenientJson.decodeFromString<com.swiftyspiffy.burkeblackapp.data.models.ApiErrorOrSuccess>(errorBody ?: "")
                    parsed.error ?: "Failed to send alert"
                } catch (_: Exception) {
                    errorBody ?: "Failed to send alert"
                }
            } catch (e: Exception) {
                _message.value = e.message ?: "Failed to send alert"
            }
        }
    }

    fun restartBot() {
        viewModelScope.launch {
            AppLogger.log("Mod: restart bot requested")
            try {
                val resp = ApiClient.api.restartBot(auth)
                if (resp.success && resp.data != null) _message.value = resp.data.message
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    // Viewer Lookup
    fun lookupViewer(username: String) {
        viewModelScope.launch {
            _viewerResult.value = null
            AppLogger.logSensitive("Mod", "viewer lookup: $username")
            try {
                val resp = ApiClient.api.viewerLookup(auth, username)
                if (resp.success && resp.data != null) _viewerResult.value = resp.data
                else _message.value = resp.error ?: "Viewer not found"
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    // Commands CRUD
    fun loadCommands() {
        viewModelScope.launch {
            try {
                val resp = ApiClient.api.fetchCommands(auth)
                if (resp.success && resp.data != null) _commands.value = resp.data.commands
            } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
        }
    }

    fun createCommand(command: String, data: String, cooldown: Int, tier: String, tags: String) {
        viewModelScope.launch {
            AppLogger.log("Mod: create command '$command'")
            try {
                val body = buildJsonObject {
                    put("command", command); put("data", data)
                    put("cooldown", "$cooldown"); put("tier", tier); put("tags", tags)
                }
                val resp = ApiClient.api.createCommand(auth, body)
                if (resp.success) { _message.value = "Command created"; loadCommands() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    fun updateCommand(id: Int, data: String, cooldown: Int, tier: String) {
        viewModelScope.launch {
            AppLogger.log("Mod: update command id=$id")
            try {
                val body = buildJsonObject {
                    put("id", "$id"); put("data", data); put("cooldown", "$cooldown"); put("tier", tier)
                }
                val resp = ApiClient.api.updateCommand(auth, body)
                if (resp.success) { _message.value = "Command updated"; loadCommands() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    fun deleteCommand(id: Int) {
        viewModelScope.launch {
            AppLogger.log("Mod: delete command id=$id")
            try {
                val body = buildJsonObject { put("id", "$id") }
                val resp = ApiClient.api.deleteCommand(auth, body)
                if (resp.success) { _message.value = "Command deleted"; loadCommands() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    // Timed Messages CRUD
    fun loadTimedMessages() {
        viewModelScope.launch {
            try {
                val resp = ApiClient.api.fetchTimedMessages(auth)
                if (resp.success && resp.data != null) _timedMessages.value = resp.data.messages
            } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
        }
    }

    fun createTimedMessage(name: String, message: String, interval: Int, dedicated: Int, disabled: Int) {
        viewModelScope.launch {
            AppLogger.log("Mod: create timed message '$name'")
            try {
                val body = buildJsonObject {
                    put("name", name); put("message", message); put("interval", "$interval")
                    put("dedicated", "$dedicated"); put("disabled", "$disabled")
                }
                val resp = ApiClient.api.createTimedMessage(auth, body)
                if (resp.success) { _message.value = "Timed message created"; loadTimedMessages() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    fun updateTimedMessage(id: Int, name: String, message: String, interval: Int, dedicated: Int, disabled: Int) {
        viewModelScope.launch {
            AppLogger.log("Mod: update timed message id=$id")
            try {
                val body = buildJsonObject {
                    put("id", "$id"); put("name", name); put("message", message)
                    put("interval", "$interval"); put("dedicated", "$dedicated"); put("disabled", "$disabled")
                }
                val resp = ApiClient.api.updateTimedMessage(auth, body)
                if (resp.success) { _message.value = "Timed message updated"; loadTimedMessages() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    fun deleteTimedMessage(id: Int) {
        viewModelScope.launch {
            AppLogger.log("Mod: delete timed message id=$id")
            try {
                val body = buildJsonObject { put("id", "$id") }
                val resp = ApiClient.api.deleteTimedMessage(auth, body)
                if (resp.success) { _message.value = "Timed message deleted"; loadTimedMessages() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    // Links CRUD
    fun loadLinks() {
        viewModelScope.launch {
            try {
                val resp = ApiClient.api.fetchLinks(auth)
                if (resp.success && resp.data != null) _links.value = resp.data.links
            } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
        }
    }

    fun createLink(customName: String, longUrl: String) {
        viewModelScope.launch {
            AppLogger.log("Mod: create link '$customName'")
            try {
                val body = buildJsonObject { put("custom_name", customName); put("long_url", longUrl) }
                val resp = ApiClient.api.createLink(auth, body)
                if (resp.success) { _message.value = "Link created"; loadLinks() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    fun updateLink(id: Int, longUrl: String) {
        viewModelScope.launch {
            AppLogger.log("Mod: update link id=$id")
            try {
                val body = buildJsonObject { put("id", "$id"); put("long_url", longUrl) }
                val resp = ApiClient.api.updateLink(auth, body)
                if (resp.success) { _message.value = "Link updated"; loadLinks() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    fun deleteLink(id: Int) {
        viewModelScope.launch {
            AppLogger.log("Mod: delete link id=$id")
            try {
                val body = buildJsonObject { put("id", "$id") }
                val resp = ApiClient.api.deleteLink(auth, body)
                if (resp.success) { _message.value = "Link deleted"; loadLinks() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    // Timeout Words CRUD
    fun loadTimeoutWords() {
        viewModelScope.launch {
            try {
                val resp = ApiClient.api.fetchTimeoutWords(auth)
                if (resp.success && resp.data != null) {
                    _timeoutWords.value = resp.data.words
                    _toCategories.value = resp.data.categories
                }
            } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
        }
    }

    fun createTimeoutWord(word: String, category: String, silent: Int, partOf: Int) {
        viewModelScope.launch {
            AppLogger.log("Mod: create timeout word '$word'")
            try {
                val body = buildJsonObject {
                    put("word", word); put("category", category); put("silent", "$silent"); put("part_of", "$partOf")
                }
                val resp = ApiClient.api.createTimeoutWord(auth, body)
                if (resp.success) { _message.value = "Timeout word added"; loadTimeoutWords() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    fun toggleTimeoutWord(id: Int, enabled: Int) {
        viewModelScope.launch {
            AppLogger.log("Mod: toggle timeout word id=$id -> ${if (enabled == 1) "enabled" else "disabled"}")
            try {
                val body = buildJsonObject { put("id", "$id"); put("enabled", "$enabled") }
                val resp = ApiClient.api.toggleTimeoutWord(auth, body)
                if (resp.success) loadTimeoutWords()
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    fun deleteTimeoutWord(id: Int) {
        viewModelScope.launch {
            AppLogger.log("Mod: delete timeout word id=$id")
            try {
                val body = buildJsonObject { put("id", "$id") }
                val resp = ApiClient.api.deleteTimeoutWord(auth, body)
                if (resp.success) { _message.value = "Timeout word deleted"; loadTimeoutWords() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    // Spoiler Words CRUD
    fun loadSpoilerWords() {
        viewModelScope.launch {
            try {
                val resp = ApiClient.api.fetchSpoilerWords(auth)
                if (resp.success && resp.data != null) _spoilerWords.value = resp.data.words
            } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
        }
    }

    fun createSpoilerWord(word: String, silent: Int, partOf: Int) {
        viewModelScope.launch {
            AppLogger.log("Mod: create spoiler word '$word'")
            try {
                val body = buildJsonObject { put("word", word); put("silent", "$silent"); put("part_of", "$partOf") }
                val resp = ApiClient.api.createSpoilerWord(auth, body)
                if (resp.success) { _message.value = "Spoiler word added"; loadSpoilerWords() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    fun toggleSpoilerWord(id: Int, enabled: Int) {
        viewModelScope.launch {
            AppLogger.log("Mod: toggle spoiler word id=$id -> ${if (enabled == 1) "enabled" else "disabled"}")
            try {
                val body = buildJsonObject { put("id", "$id"); put("enabled", "$enabled") }
                val resp = ApiClient.api.toggleSpoilerWord(auth, body)
                if (resp.success) loadSpoilerWords()
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    fun deleteSpoilerWord(id: Int) {
        viewModelScope.launch {
            AppLogger.log("Mod: delete spoiler word id=$id")
            try {
                val body = buildJsonObject { put("id", "$id") }
                val resp = ApiClient.api.deleteSpoilerWord(auth, body)
                if (resp.success) { _message.value = "Spoiler word deleted"; loadSpoilerWords() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    // Giveaway Management
    fun loadSubmissions() {
        viewModelScope.launch {
            try {
                val resp = ApiClient.api.fetchGiveawaySubmissions(auth)
                if (resp.success && resp.data != null) _submissions.value = resp.data.submissions
            } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
        }
    }

    fun hideSubmission(id: Int) {
        viewModelScope.launch {
            AppLogger.log("Mod: hide giveaway submission id=$id")
            try {
                val body = buildJsonObject { put("id", "$id") }
                ApiClient.api.hideGiveawaySubmission(auth, body)
                loadSubmissions()
            } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
        }
    }

    fun loadHiddenSubmissions() {
        viewModelScope.launch {
            try {
                val resp = ApiClient.api.fetchHiddenSubmissions(auth)
                if (resp.success && resp.data != null) _hiddenSubmissions.value = resp.data.submissions
            } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
        }
    }

    fun unhideSubmission(id: Int) {
        viewModelScope.launch {
            AppLogger.log("Mod: unhide giveaway submission id=$id")
            try {
                val body = buildJsonObject { put("id", "$id") }
                ApiClient.api.unhideGiveawaySubmission(auth, body)
                loadHiddenSubmissions()
            } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
        }
    }

    fun addGiveaway(name: String, donator: String, type: String, keys: List<String>, extra: String) {
        viewModelScope.launch {
            AppLogger.log("Mod: add giveaway '$name' from $donator")
            try {
                val body = buildJsonObject {
                    put("name", name); put("donator", donator); put("type", type)
                    put("keys", kotlinx.serialization.json.JsonArray(keys.map { kotlinx.serialization.json.JsonPrimitive(it) }))
                    put("extra", extra)
                }
                val resp = ApiClient.api.addGiveaway(auth, body)
                if (resp.success) { _message.value = "Giveaway added"; loadSubmissions() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    fun sendToKraken(submissionId: Int, name: String, donator: String, type: String, prize: String,
                     filter: String, filterAmount: String, entryDuration: String, claimDuration: String) {
        viewModelScope.launch {
            AppLogger.log("Mod: send giveaway to kraken '$name'")
            try {
                val body = buildJsonObject {
                    put("submission_id", "$submissionId"); put("name", name); put("donator", donator)
                    put("type", type); put("prize", prize); put("filter", filter)
                    put("filter_amount", filterAmount); put("entry_duration", entryDuration)
                    put("claim_duration", claimDuration)
                }
                val resp = ApiClient.api.sendGiveawayToKraken(auth, body)
                if (resp.success) { _message.value = resp.data?.message ?: "Sent to Kraken"; loadSubmissions() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    fun loadGiveawayHistory() {
        viewModelScope.launch {
            try {
                val resp = ApiClient.api.fetchGiveawayHistory(auth)
                if (resp.success && resp.data != null) _giveawayHistory.value = resp.data.giveaways
            } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
        }
    }

    // Soundbyte Library
    fun loadSBLibrary() {
        viewModelScope.launch {
            try {
                val resp = ApiClient.api.fetchSoundbyteLibrary(auth)
                if (resp.success && resp.data != null) {
                    _sbLibrary.value = resp.data.soundbytes
                    _sbGenres.value = resp.data.genres
                }
            } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
        }
    }

    fun updateSoundbyte(id: Int, name: String, genre: String, approved: Int, horrorNight: Int, creditCost: Int) {
        viewModelScope.launch {
            AppLogger.log("Mod: update soundbyte '$name' id=$id")
            try {
                val body = buildJsonObject {
                    put("id", "$id"); put("name", name); put("genre", genre)
                    put("approved", "$approved"); put("horror_night", "$horrorNight"); put("credit_cost", "$creditCost")
                }
                val resp = ApiClient.api.updateSoundbyte(auth, body)
                if (resp.success) { _message.value = "Soundbyte updated"; loadSBLibrary() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    fun deleteSoundbyte(id: Int) {
        viewModelScope.launch {
            AppLogger.log("Mod: delete soundbyte id=$id")
            try {
                val body = buildJsonObject {
                    put("id", "$id"); put("name", ""); put("genre", "")
                    put("approved", "-1"); put("horror_night", "0"); put("credit_cost", "1")
                }
                val resp = ApiClient.api.updateSoundbyte(auth, body)
                if (resp.success) { _message.value = "Soundbyte deleted"; loadSBLibrary() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    fun loadSBCredits() {
        viewModelScope.launch {
            try {
                val resp = ApiClient.api.fetchSoundbyteCreditsList(auth)
                if (resp.success && resp.data != null) _sbCredits.value = resp.data.credits
            } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
        }
    }

    fun modifySBCredits(userId: String, amount: Int, direction: String) {
        viewModelScope.launch {
            AppLogger.log("Mod: modify soundbyte credits user=$userId amount=$amount direction=$direction")
            try {
                val body = buildJsonObject {
                    put("user_id", userId); put("amount", "$amount"); put("direction", direction)
                }
                val resp = ApiClient.api.modifySoundbyteCredits(auth, body)
                if (resp.success) { _message.value = "Credits modified"; loadSBCredits() }
                else _message.value = resp.error
            } catch (e: Exception) { _message.value = e.message }
        }
    }

    fun loadSBHistory() {
        viewModelScope.launch {
            try {
                val resp = ApiClient.api.fetchSoundbyteFullHistory(auth)
                if (resp.success && resp.data != null) _sbHistory.value = resp.data.history
            } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
        }
    }
}
