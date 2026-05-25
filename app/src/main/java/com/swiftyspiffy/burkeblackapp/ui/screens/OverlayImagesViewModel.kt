package com.swiftyspiffy.burkeblackapp.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.OverlayCategory
import com.swiftyspiffy.burkeblackapp.data.models.OverlayImage
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ModeDimension(
    val name: String,
    val width: Int,
    val height: Int,
    val credit: Int
)

data class ImageGroup(
    val category: OverlayCategory,
    val images: List<OverlayImage>
)

class OverlayImagesViewModel(private val token: String) : ViewModel() {
    private val _categories = MutableStateFlow<List<OverlayCategory>>(emptyList())
    val categories: StateFlow<List<OverlayCategory>> = _categories.asStateFlow()

    private val _images = MutableStateFlow<List<OverlayImage>>(emptyList())
    val images: StateFlow<List<OverlayImage>> = _images.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val filteredImages: StateFlow<List<OverlayImage>> = combine(_images, _selectedCategory) { images, category ->
        if (category == "All") images
        else {
            val catId = _categories.value.firstOrNull { it.name == category }?.id
            if (catId != null) images.filter { it.categoryId == catId } else images
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val groupedImages: StateFlow<List<ImageGroup>> = combine(filteredImages, _categories) { filtered, cats ->
        cats.mapNotNull { cat ->
            val catImages = filtered.filter { it.categoryId == cat.id }
            if (catImages.isNotEmpty()) ImageGroup(cat, catImages) else null
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun loadImages() {
        if (_images.value.isNotEmpty()) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.api.fetchOverlayImages("Bearer $token")
                if (response.success && response.data != null) {
                    _categories.value = response.data.categories
                    _images.value = response.data.images
                    AppLogger.log("OverlayImages: loaded ${_images.value.size} images in ${_categories.value.size} categories")
                }
            } catch (e: Exception) {
                AppLogger.log("OverlayImages: load failed - ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun getStreamThumbnailUrl(): String {
        val ts = System.currentTimeMillis() / 1000
        return "https://static-cdn.jtvnw.net/previews-ttv/live_user_burkeblack-640x360.jpg?_=$ts"
    }

    fun modesForImage(image: OverlayImage): List<ModeDimension> {
        val modes = mutableListOf<ModeDimension>()
        image.modes?.large?.let { m ->
            modes.add(ModeDimension("large", m.width, m.height, image.credits?.large ?: 3))
        }
        image.modes?.medium?.let { m ->
            modes.add(ModeDimension("medium", m.width, m.height, image.credits?.medium ?: 2))
        }
        image.modes?.small?.let { m ->
            modes.add(ModeDimension("small", m.width, m.height, image.credits?.small ?: 1))
        }
        image.modes?.bounce?.let { m ->
            modes.add(ModeDimension("bounce", m.width, m.height, image.credits?.bounce ?: 3))
        }
        if (modes.isEmpty()) {
            modes.add(ModeDimension("medium", 300, 300, 2))
        }
        return modes
    }

    fun bounceCountForImage(image: OverlayImage): Int {
        return image.modes?.bounce?.count ?: 1
    }
}
