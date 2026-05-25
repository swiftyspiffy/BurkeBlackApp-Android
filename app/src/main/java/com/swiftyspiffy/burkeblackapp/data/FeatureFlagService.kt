package com.swiftyspiffy.burkeblackapp.data

import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FeatureFlagService {
    private val _flags = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val flags: StateFlow<Map<String, Boolean>> = _flags.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    fun isEnabled(key: String): Boolean = _flags.value[key] == true

    suspend fun load(token: String? = null) {
        try {
            val auth = token?.let { "Bearer $it" } ?: ""
            val response = ApiClient.api.fetchFeatureFlags(auth)
            if (response.success && response.data != null) {
                _flags.value = response.data.flags
            }
            _loaded.value = true
            AppLogger.log("FeatureFlags: loaded ${_flags.value.size} flags")
        } catch (e: Exception) {
            _loaded.value = true
            AppLogger.log("FeatureFlags: load failed - ${e.message}")
        }
    }

    fun clear() {
        _flags.value = emptyMap()
        _loaded.value = false
    }
}
