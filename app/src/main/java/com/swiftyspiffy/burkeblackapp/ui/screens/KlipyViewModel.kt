package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.KlipyGifResult
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class KlipyViewModel(private val token: String) : ViewModel() {
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    private val _results = MutableStateFlow<List<KlipyGifResult>>(emptyList())
    val results: StateFlow<List<KlipyGifResult>> = _results.asStateFlow()

    private val _decryptedURLs = MutableStateFlow<Map<String, String>>(emptyMap())
    val decryptedURLs: StateFlow<Map<String, String>> = _decryptedURLs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _isShowingResults = MutableStateFlow(false)
    val isShowingResults: StateFlow<Boolean> = _isShowingResults.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    private val _gifCredits = MutableStateFlow<Map<String, Int>?>(null)
    val gifCredits: StateFlow<Map<String, Int>?> = _gifCredits.asStateFlow()

    private var currentPage = 1
    private val perPage = 20
    private var decryptKey: String? = null
    private var decryptKeyExpiry: Long = 0

    companion object {
        val defaultCategories = listOf(
            "Trending", "Reactions", "Memes", "Funny",
            "Anime", "Gaming", "Love", "Sad",
            "Happy", "Angry", "Dance", "Celebrate"
        )
    }

    fun updateSearchText(text: String) {
        _searchText.value = text
    }

    fun loadGifSettings() {
        viewModelScope.launch {
            try {
                val response = ApiClient.api.fetchGifSettings("Bearer $token")
                if (response.success && response.data != null) {
                    _gifCredits.value = response.data.credits
                }
            } catch (e: Exception) {
                AppLogger.log("Klipy: failed to load gif settings - ${e.message}")
            }
        }
    }

    fun searchByCategory(category: String) {
        _searchText.value = category
        search()
    }

    fun search() {
        val query = _searchText.value.trim()
        if (query.isEmpty()) {
            clearSearch()
            return
        }
        currentPage = 1
        _results.value = emptyList()
        _decryptedURLs.value = emptyMap()
        _hasMore.value = true
        _isShowingResults.value = true
        _searchError.value = null
        loadPage(query, 1)
    }

    fun loadMore() {
        if (_isLoading.value || !_hasMore.value) return
        val query = _searchText.value.trim()
        if (query.isEmpty()) return
        loadPage(query, currentPage + 1)
    }

    fun clearSearch() {
        _searchText.value = ""
        _results.value = emptyList()
        _decryptedURLs.value = emptyMap()
        _isShowingResults.value = false
        _searchError.value = null
        _hasMore.value = true
        currentPage = 1
    }

    private fun loadPage(query: String, page: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                ensureDecryptKey()
                val response = ApiClient.api.searchGifs("Bearer $token", query, page, perPage)
                if (response.success && response.data != null) {
                    val data = response.data
                    val newDecrypted = _decryptedURLs.value.toMutableMap()
                    for (gif in data.results) {
                        decryptURL(gif.encryptedPreviewUrl)?.let { url ->
                            newDecrypted[gif.token] = url
                        }
                    }
                    _decryptedURLs.value = newDecrypted

                    if (page == 1) {
                        _results.value = data.results
                    } else {
                        _results.value = _results.value + data.results
                    }
                    currentPage = data.page
                    _hasMore.value = data.hasNext
                    AppLogger.log("Klipy: got ${data.results.size} results, hasMore=${data.hasNext}")
                }
            } catch (e: Exception) {
                AppLogger.log("Klipy: search failed - ${e.message}")
                _searchError.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun ensureDecryptKey() {
        val now = System.currentTimeMillis() / 1000
        if (decryptKey != null && now < decryptKeyExpiry) return

        AppLogger.log("Klipy: fetching decrypt key")
        val response = ApiClient.api.fetchGifDecryptKey("Bearer $token")
        if (response.success && response.data != null) {
            decryptKey = response.data.gifDecryptKey
            decryptKeyExpiry = response.data.expiresAt
            AppLogger.log("Klipy: decrypt key obtained, expires at $decryptKeyExpiry")
        } else {
            throw Exception("Failed to obtain GIF decrypt key")
        }
    }

    fun decryptURL(encrypted: String): String? {
        val key = decryptKey ?: return null
        return try {
            val raw = Base64.getDecoder().decode(encrypted)
            if (raw.size < 17) return null
            val iv = raw.sliceArray(0 until 16)
            val ciphertext = raw.sliceArray(16 until raw.size)
            val keyBytes = MessageDigest.getInstance("SHA-256").digest(key.toByteArray(Charsets.UTF_8))
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(keyBytes, "AES"), IvParameterSpec(iv))
            val plaintext = cipher.doFinal(ciphertext)
            String(plaintext, Charsets.UTF_8)
        } catch (e: Exception) {
            AppLogger.log("Klipy: decrypt failed - ${e.message}")
            null
        }
    }

    fun getStreamThumbnailUrl(): String {
        val ts = System.currentTimeMillis() / 1000
        return "https://static-cdn.jtvnw.net/previews-ttv/live_user_burkeblack-640x360.jpg?_=$ts"
    }
}
