package com.tang.prm.feature.reflect.album

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.AlbumPhoto
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.usecase.FavoriteToggleUseCase
import com.tang.prm.domain.usecase.filterBy
import com.tang.prm.domain.usecase.PhotoAlbumAggregationUseCase
import com.tang.prm.domain.usecase.PhotoAlbumAggregateData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PhotoAlbumUiState(
    val photos: List<AlbumPhoto> = emptyList(),
    val allContacts: List<Contact> = emptyList(),
    val selectedContactId: Long? = null,
    val filterSourceType: String? = null,
    val isLoading: Boolean = true,
    val totalPhotoCount: Int = 0,
    val totalContactCount: Int = 0,
    val totalLocationCount: Int = 0,
    val favoritePhotoIds: Set<Long> = emptySet()
)

@HiltViewModel
class PhotoAlbumViewModel @Inject constructor(
    private val photoAlbumAggregationUseCase: PhotoAlbumAggregationUseCase,
    private val favoriteToggleUseCase: FavoriteToggleUseCase
) : ViewModel() {

    private val _selectedContactId = MutableStateFlow<Long?>(null)
    private val _filterSourceType = MutableStateFlow<String?>(null)
    private val _favoritePhotoIds = MutableStateFlow<Set<Long>>(emptySet())

    /** 单一聚合数据源，避免 getAggregateData() 被重复调用导致双重订阅 */
    private val aggregateData = photoAlbumAggregationUseCase.getAggregateData()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(10_000), PhotoAlbumAggregateData(emptyList(), emptyList()))

    init {
        viewModelScope.launch {
            favoriteToggleUseCase.getFavoriteIds(SourceTypes.PHOTO).collect { ids ->
                _favoritePhotoIds.value = ids
            }
        }
    }

    val uiState: StateFlow<PhotoAlbumUiState> = combine(
        aggregateData,
        _favoritePhotoIds,
        _selectedContactId,
        _filterSourceType
    ) { data, favoritePhotoIds, selectedContactId, filterSourceType ->
        val totalPhotoCount = data.allPhotos.size
        val totalContactCount = data.allPhotos.mapNotNull { it.contactId }.distinct().size
        val totalLocationCount = data.allPhotos.mapNotNull { it.location }.distinct().size

        val filtered = data.allPhotos.filterBy(
            selectedContactId = selectedContactId,
            filterSourceType = filterSourceType
        )

        PhotoAlbumUiState(
            photos = filtered,
            allContacts = data.contacts,
            selectedContactId = selectedContactId,
            filterSourceType = filterSourceType,
            isLoading = false,
            totalPhotoCount = totalPhotoCount,
            totalContactCount = totalContactCount,
            totalLocationCount = totalLocationCount,
            favoritePhotoIds = favoritePhotoIds
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(10_000), PhotoAlbumUiState())

    fun filterByContact(contactId: Long?) {
        _selectedContactId.value = contactId
    }

    fun filterBySourceType(sourceType: String?) {
        _filterSourceType.value = sourceType
    }

    fun clearFilters() {
        _selectedContactId.value = null
        _filterSourceType.value = null
    }

    fun toggleFavorite(photo: AlbumPhoto) {
        val photoId = photo.stableId
        val previousIds = _favoritePhotoIds.value
        val wasFavorite = photoId in previousIds
        val newIds = if (wasFavorite) previousIds - photoId else previousIds + photoId
        _favoritePhotoIds.value = newIds

        viewModelScope.launch {
            try {
                val sourceLabel = when (photo.sourceType) {
                    SourceTypes.ALBUM_EVENT -> "事件"
                    SourceTypes.ALBUM_GIFT -> "礼物"
                    SourceTypes.ALBUM_CHAT -> "对话"
                    else -> "相册"
                }
                if (wasFavorite) {
                    favoriteToggleUseCase.deleteFavoriteBySource(SourceTypes.PHOTO, photoId)
                } else {
                    favoriteToggleUseCase(
                        type = SourceTypes.PHOTO,
                        sourceId = photoId,
                        title = "图片 · ${photo.sourceTitle}",
                        description = "$sourceLabel · ${photo.sourceTitle}"
                    )
                }
            } catch (e: Exception) {
                _favoritePhotoIds.value = previousIds
            }
        }
    }

    fun isPhotoFavorite(photoId: String): Boolean {
        return _favoritePhotoIds.value.contains(
            photoId.toList().fold(0L) { acc, c -> acc * 31L + c.code.toLong() }
        )
    }
}
