package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.StreamStatusResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _streamStatus = MutableStateFlow<StreamStatusResponse?>(null)
    val streamStatus: StateFlow<StreamStatusResponse?> = _streamStatus

    private val _profileImageUrl = MutableStateFlow<String?>(null)
    val profileImageUrl: StateFlow<String?> = _profileImageUrl

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var lastFetchTime: Long = 0L
    private val cacheDurationMs: Long = 3 * 60 * 1000L // 3 minutes

    init {
        AppLogger.log("App launched")
        checkStreamStatus()
        loadProfile()
    }

    fun checkStreamStatus(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && _streamStatus.value != null && (now - lastFetchTime) < cacheDurationMs) {
            AppLogger.log("Stream status cached, skipping fetch")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            AppLogger.log("Checking stream status")
            try {
                val response = ApiClient.api.fetchStreamStatus()
                if (response.success) {
                    _streamStatus.value = response.data
                    lastFetchTime = System.currentTimeMillis()
                    val status = if (response.data?.isLive == true) "LIVE" else "OFFLINE"
                    AppLogger.log("Stream status: $status")
                }
            } catch (_: Exception) {
                _streamStatus.value = StreamStatusResponse(isLive = false)
                AppLogger.log("Stream status check failed")
            }
            _isLoading.value = false
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.fetchProfile()
                if (response.success && response.data != null) {
                    _profileImageUrl.value = response.data.avatarURL
                }
            } catch (e: Exception) {
                AppLogger.log("Profile load failed: ${e.message}")
            }
        }
    }
}
