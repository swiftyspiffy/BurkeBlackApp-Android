package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.Soundbyte
import com.swiftyspiffy.burkeblackapp.data.models.SoundbyteGenre
import com.swiftyspiffy.burkeblackapp.data.models.SoundbyteHistoryItem
import com.swiftyspiffy.burkeblackapp.data.models.SoundbyteSendBody
import com.swiftyspiffy.burkeblackapp.data.models.SoundbyteSendResponse
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SoundbytesViewModel(private val token: String) : ViewModel() {
    private val _soundbytes = MutableStateFlow<List<Soundbyte>>(emptyList())
    val soundbytes: StateFlow<List<Soundbyte>> = _soundbytes

    private val _genres = MutableStateFlow<List<SoundbyteGenre>>(emptyList())
    val genres: StateFlow<List<SoundbyteGenre>> = _genres

    private val _credits = MutableStateFlow(0)
    val credits: StateFlow<Int> = _credits

    private val _history = MutableStateFlow<List<SoundbyteHistoryItem>>(emptyList())
    val history: StateFlow<List<SoundbyteHistoryItem>> = _history

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchTerm = MutableStateFlow("")
    val searchTerm: StateFlow<String> = _searchTerm

    private val _selectedGenre = MutableStateFlow("All")
    val selectedGenre: StateFlow<String> = _selectedGenre

    private val _total = MutableStateFlow(0)
    val total: StateFlow<Int> = _total

    private val _sendResult = MutableStateFlow<SoundbyteSendResponse?>(null)
    val sendResult: StateFlow<SoundbyteSendResponse?> = _sendResult

    private val _sendError = MutableStateFlow<String?>(null)
    val sendError: StateFlow<String?> = _sendError

    private var currentOffset = 0
    private val pageSize = 25

    init {
        AppLogger.log("Soundbytes screen opened")
        loadGenres()
        loadCredits()
        loadSoundbytes(reset = true)
    }

    fun setSearchTerm(term: String) {
        _searchTerm.value = term
        AppLogger.log("Soundbytes search: $term")
        loadSoundbytes(reset = true)
    }

    fun setSelectedGenre(genre: String) {
        _selectedGenre.value = genre
        AppLogger.log("Soundbytes genre filter: $genre")
        loadSoundbytes(reset = true)
    }

    fun loadSoundbytes(reset: Boolean = false) {
        if (reset) currentOffset = 0

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val genre = _selectedGenre.value.let { if (it == "All") null else it }
                val search = _searchTerm.value.let { it.ifBlank { null } }

                val response = ApiClient.api.fetchSoundbytes(
                    auth = "Bearer $token",
                    offset = currentOffset,
                    amount = pageSize,
                    searchTerm = search,
                    genre = genre
                )
                if (response.success && response.data != null) {
                    if (reset) {
                        _soundbytes.value = response.data.soundbytes
                    } else {
                        _soundbytes.value = _soundbytes.value + response.data.soundbytes
                    }
                    _total.value = response.data.total
                    currentOffset = _soundbytes.value.size
                    AppLogger.log("Soundbytes loaded: ${_soundbytes.value.size}/${response.data.total}")
                }
            } catch (e: Exception) {
                AppLogger.log("Soundbytes load failed: ${e.message}")
            }
            _isLoading.value = false
        }
    }

    fun loadMore() {
        if (_soundbytes.value.size < _total.value) {
            loadSoundbytes(reset = false)
        }
    }

    private fun loadGenres() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.fetchSoundbyteGenres("Bearer $token")
                if (response.success && response.data != null) {
                    _genres.value = response.data.genres
                }
            } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
        }
    }

    fun loadCredits() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.fetchSoundbyteCredits("Bearer $token")
                if (response.success && response.data != null) {
                    _credits.value = response.data.soundbyteCredits
                }
            } catch (e: Exception) { AppLogger.log("Error: ${e.message}") }
        }
    }

    fun loadHistory() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.fetchSoundbyteHistory("Bearer $token")
                if (response.success && response.data != null) {
                    _history.value = response.data.history
                    AppLogger.log("Soundbyte history loaded: ${response.data.history.size} items")
                }
            } catch (e: Exception) {
                AppLogger.log("Soundbyte history load failed: ${e.message}")
            }
        }
    }

    fun sendSoundbyte(soundbyteId: Int, announce: Boolean) {
        viewModelScope.launch {
            try {
                val body = SoundbyteSendBody(
                    soundbyteId = soundbyteId,
                    announce = if (announce) 1 else 0
                )
                val response = ApiClient.api.sendSoundbyte("Bearer $token", body)
                if (response.success && response.data != null) {
                    _sendResult.value = response.data
                    _credits.value = response.data.creditsRemaining
                    AppLogger.log("Soundbyte sent: id=$soundbyteId, credits remaining: ${response.data.creditsRemaining}")
                } else {
                    AppLogger.log("Soundbyte send failed: ${response.error}")
                    _sendError.value = response.error ?: "Failed to send soundbyte"
                }
            } catch (e: Exception) {
                AppLogger.log("Soundbyte send error: ${e.message}")
                _sendError.value = e.message ?: "Failed to send soundbyte"
            }
        }
    }

    fun clearSendResult() {
        _sendResult.value = null
    }

    fun clearSendError() {
        _sendError.value = null
    }
}
