package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.CommunityServer
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommunityServersViewModel(private val token: String?) : ViewModel() {
    private val _servers = MutableStateFlow<List<CommunityServer>>(emptyList())
    val servers: StateFlow<List<CommunityServer>> = _servers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    var selectedServer: CommunityServer? = null

    init {
        fetchServers()
    }

    fun fetchServers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                AppLogger.log("Servers: fetching community servers")
                val auth = if (!token.isNullOrEmpty()) "Bearer $token" else ""
                val response = ApiClient.api.fetchCommunityServers(auth)
                if (response.success && response.data != null) {
                    AppLogger.log("Servers: loaded ${response.data.servers.size} servers")
                    _servers.value = response.data.servers
                } else {
                    _error.value = response.error ?: "Failed to load servers"
                }
            } catch (e: Exception) {
                AppLogger.log("Error fetching community servers: ${e.message}")
                _error.value = "Could not reach the server"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
