package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.StreamDeckChannel
import com.swiftyspiffy.burkeblackapp.data.models.orderedChannels
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StreamDeckState(
    val channels: List<StreamDeckChannel> = StreamDeckChannel.placeholders(),
    val selectedKey: String? = null,
    val isLoading: Boolean = true,
    val isStale: Boolean = false,
    val fetchedAt: String? = null,
    val hasError: Boolean = false,
) {
    val selectedChannel: StreamDeckChannel
        get() = channels.firstOrNull { it.key == selectedKey } ?: channels.first()

    val allChannelsOffline: Boolean
        get() = channels.none { it.isLive }

    val classics: StreamDeckChannel
        get() = channels.first { it.key == "classics" }
}

/** Shared by Helm and Ports; widgets retain their own single-channel API. */
class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(StreamDeckState())
    val state = _state.asStateFlow()
    private var fetching = false

    fun selectChannel(channel: StreamDeckChannel) {
        _state.update { it.copy(selectedKey = channel.key) }
    }

    fun refresh() {
        if (fetching) return
        fetching = true
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = ApiClient.api.fetchStreamDeck()
                val data =
                    response.data?.takeIf { response.success } ?: error("Stream deck unavailable")
                val channels = data.orderedChannels()
                _state.update { previous ->
                    previous.copy(
                        channels = channels,
                        selectedKey =
                            previous.selectedKey
                                ?: channels.firstOrNull { it.isLive }?.key
                                ?: "main",
                        isStale = data.isStale,
                        fetchedAt = data.fetchedAt,
                        hasError = false,
                    )
                }
                AppLogger.log("Stream deck: loaded ${channels.count { it.isLive }} live channels")
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Keep the last usable status instead of changing live channels to offline.
                _state.update { it.copy(hasError = true) }
                AppLogger.log("Stream deck: refresh failed")
            } finally {
                fetching = false
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
}
