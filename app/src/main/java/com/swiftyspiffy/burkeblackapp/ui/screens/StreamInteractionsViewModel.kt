package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftyspiffy.burkeblackapp.data.AppSettings
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.OverlayTriggerBody
import com.swiftyspiffy.burkeblackapp.data.models.SoundbyteSendBody
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SoundbytePick(
    val id: Int,
    val name: String,
    val announce: Boolean
)

data class OverlayPick(
    val imageId: Int? = null,
    val gifToken: String? = null,
    val name: String,
    val mode: String,
    val duration: Double = 10.0,
    val xPercent: Double = 0.5,
    val yPercent: Double = 0.5,
    val credit: Int = 1
)

data class PositionerData(
    val imageURL: String?,
    val isGif: Boolean,
    val modes: List<ModeDimension>,
    val name: String,
    val imageId: Int? = null,
    val gifToken: String? = null,
    val bounceCount: Int = 1
)

class StreamInteractionsViewModel(private val token: String, private val username: String, private val appSettings: AppSettings? = null) : ViewModel() {
    private val _soundbytePick = MutableStateFlow<SoundbytePick?>(null)
    val soundbytePick: StateFlow<SoundbytePick?> = _soundbytePick.asStateFlow()

    private val _overlayPick = MutableStateFlow<OverlayPick?>(null)
    val overlayPick: StateFlow<OverlayPick?> = _overlayPick.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _sendResult = MutableStateFlow<String?>(null)
    val sendResult: StateFlow<String?> = _sendResult.asStateFlow()

    private val _sendError = MutableStateFlow<String?>(null)
    val sendError: StateFlow<String?> = _sendError.asStateFlow()

    private val _streamIsLive = MutableStateFlow(true)
    val streamIsLive: StateFlow<Boolean> = _streamIsLive.asStateFlow()

    private val _interactionsDisabled = MutableStateFlow(false)
    val interactionsDisabled: StateFlow<Boolean> = _interactionsDisabled.asStateFlow()

    private val _credits = MutableStateFlow(0)
    val credits: StateFlow<Int> = _credits.asStateFlow()

    val totalCost: StateFlow<Int> = combine(_soundbytePick, _overlayPick) { sb, ov ->
        (if (sb != null) 1 else 0) + (ov?.credit ?: 0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun setSoundbytePick(pick: SoundbytePick?) { _soundbytePick.value = pick }
    fun setOverlayPick(pick: OverlayPick?) { _overlayPick.value = pick }
    fun clearSendResult() { _sendResult.value = null }
    fun clearSendError() { _sendError.value = null }

    fun checkStreamStatus() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.fetchStreamStatus()
                if (response.success && response.data != null) {
                    _streamIsLive.value = response.data.isLive
                }
            } catch (e: Exception) {
                AppLogger.log("StreamInteractions: stream status check failed - ${e.message}")
            }
        }
    }

    fun checkInteractionsEnabled() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.fetchSoundbytesStatus()
                if (response.success && response.data != null) {
                    val serverDisabled = !response.data.enabled
                    val overridden = appSettings?.getDebugOverrideInteractionsDisabled() == true
                    _interactionsDisabled.value = serverDisabled && !overridden
                }
            } catch (e: Exception) {
                AppLogger.log("StreamInteractions: soundbytes status check failed - ${e.message}")
            }
        }
    }

    fun loadCredits() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.fetchSoundbyteCredits("Bearer $token")
                if (response.success && response.data != null) {
                    _credits.value = response.data.soundbyteCredits
                }
            } catch (e: Exception) {
                AppLogger.log("StreamInteractions: credits load failed - ${e.message}")
            }
        }
    }

    fun sendInteractions() {
        viewModelScope.launch {
            _isSending.value = true
            val messages = mutableListOf<String>()
            try {
                _soundbytePick.value?.let { sb ->
                    val sbResponse = ApiClient.api.sendSoundbyte(
                        "Bearer $token",
                        SoundbyteSendBody(soundbyteId = sb.id, announce = if (sb.announce) 1 else 0)
                    )
                    if (sbResponse.success && sbResponse.data != null) {
                        messages.add(sbResponse.data.message)
                        _credits.value = sbResponse.data.creditsRemaining
                    } else {
                        throw Exception(sbResponse.error ?: "Soundbyte send failed")
                    }
                }

                _overlayPick.value?.let { ov ->
                    val useTest = appSettings?.getDebugUseTestOverlay() == true
                    val body = OverlayTriggerBody(
                        imageId = ov.imageId,
                        gifToken = ov.gifToken,
                        mode = ov.mode,
                        duration = ov.duration,
                        username = username,
                        source = "app_android",
                        xPercent = ov.xPercent,
                        yPercent = ov.yPercent,
                        test = if (useTest) true else null
                    )
                    val ovResponse = ApiClient.api.triggerOverlay("Bearer $token", body)
                    if (ovResponse.success && ovResponse.data != null) {
                        messages.add(ovResponse.data.message)
                        _credits.value = ovResponse.data.creditsRemaining
                    } else {
                        throw Exception(ovResponse.error ?: "Overlay trigger failed")
                    }
                }

                _soundbytePick.value = null
                _overlayPick.value = null
                _sendResult.value = messages.joinToString("\n")
            } catch (e: Exception) {
                AppLogger.log("StreamInteractions: send failed - ${e.message}")
                _sendError.value = e.message
            } finally {
                _isSending.value = false
            }
        }
    }
}
