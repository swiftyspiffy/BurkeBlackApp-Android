package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.ClipVoteBody
import com.swiftyspiffy.burkeblackapp.data.models.ClipVotingConfig
import com.swiftyspiffy.burkeblackapp.data.models.VotingClip
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClipVotingViewModel(private val token: String?) : ViewModel() {
    private val _clips = MutableStateFlow<List<VotingClip>>(emptyList())
    val clips: StateFlow<List<VotingClip>> = _clips

    private val _config = MutableStateFlow<ClipVotingConfig?>(null)
    val config: StateFlow<ClipVotingConfig?> = _config

    private val _month = MutableStateFlow("")
    val month: StateFlow<String> = _month

    private val _periodStart = MutableStateFlow("")
    val periodStart: StateFlow<String> = _periodStart

    private val _periodEnd = MutableStateFlow("")
    val periodEnd: StateFlow<String> = _periodEnd

    private val _totalVoters = MutableStateFlow(0)
    val totalVoters: StateFlow<Int> = _totalVoters

    private val _userHasVoted = MutableStateFlow(false)
    val userHasVoted: StateFlow<Boolean> = _userHasVoted

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _submitSuccess = MutableStateFlow(false)
    val submitSuccess: StateFlow<Boolean> = _submitSuccess

    // User's selections — clip IDs in ranked order
    private val _selections = MutableStateFlow<List<String>>(emptyList())
    val selections: StateFlow<List<String>> = _selections

    init {
        fetchClips()
    }

    fun fetchClips() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                AppLogger.log("Clips: fetching (authenticated=${!token.isNullOrEmpty()})")
                val auth = if (!token.isNullOrEmpty()) "Bearer $token" else ""
                val response = ApiClient.api.fetchClips(auth)
                if (response.success && response.data != null) {
                    val data = response.data
                    _clips.value = data.clips.shuffled()
                    _config.value = data.config
                    AppLogger.log("Clips: loaded ${data.clips.size} clips for ${data.month}, mode=${data.config.votingMode}")
                    _month.value = data.month
                    _periodStart.value = data.periodStart
                    _periodEnd.value = data.periodEnd
                    _totalVoters.value = data.totalVoters
                    _userHasVoted.value = data.userHasVoted

                    // Pre-populate selections from existing votes
                    if (data.userHasVoted) {
                        val voted = data.clips
                            .filter { it.userRank != null }
                            .sortedBy { it.userRank }
                            .map { it.clipId }
                        _selections.value = voted
                    }
                } else {
                    _error.value = response.error ?: "Failed to load clips"
                }
            } catch (e: Exception) {
                AppLogger.log("Error fetching clips: ${e.message}")
                _error.value = "Could not reach the server"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleSelection(clipId: String) {
        val current = _selections.value.toMutableList()
        val cfg = _config.value ?: return
        val maxSelections = if (cfg.votingMode == "single") 1 else cfg.voteCount

        if (current.contains(clipId)) {
            current.remove(clipId)
        } else if (current.size < maxSelections) {
            current.add(clipId)
        }
        _selections.value = current
    }

    fun moveSelection(fromIndex: Int, toIndex: Int) {
        val current = _selections.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _selections.value = current
        }
    }

    fun canSubmit(): Boolean {
        val cfg = _config.value ?: return false
        val expected = if (cfg.votingMode == "single") 1 else cfg.voteCount
        return _selections.value.size == expected
    }

    fun submitVote() {
        if (!canSubmit() || token.isNullOrEmpty()) return
        viewModelScope.launch {
            _isSubmitting.value = true
            _submitSuccess.value = false
            try {
                AppLogger.log("Clips: submitting vote with ${_selections.value.size} selections")
                val response = ApiClient.api.submitClipVote(
                    auth = "Bearer $token",
                    body = ClipVoteBody(rankings = _selections.value)
                )
                if (response.success) {
                    AppLogger.log("Clips: vote submitted successfully")
                    _submitSuccess.value = true
                    delay(3000) // Let the confetti play
                    fetchClips() // Refresh to show updated scores
                } else {
                    _error.value = response.error ?: "Failed to submit vote"
                }
            } catch (e: Exception) {
                AppLogger.log("Error submitting vote: ${e.message}")
                _error.value = "Could not submit vote"
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun clearSubmitSuccess() {
        _submitSuccess.value = false
    }
}
