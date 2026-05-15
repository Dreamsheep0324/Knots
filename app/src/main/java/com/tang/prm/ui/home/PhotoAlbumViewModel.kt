package com.tang.prm.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.model.EventTypes
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.repository.FavoriteRepository
import com.tang.prm.domain.repository.GiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumPhoto(
    val id: String,
    val uri: String,
    val sourceType: String,
    val sourceId: Long,
    val sourceTitle: String,
    val contactId: Long?,
    val contactName: String?,
    val contactAvatar: String?,
    val date: Long,
    val location: String?
) {
    val stableId: Long
        get() = id.toList().fold(0L) { acc, c -> acc * 31L + c.code.toLong() }
}

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

private data class PhotoData(
    val allPhotos: List<AlbumPhoto>,
    val contacts: List<Contact>
)

private data class FilteredData(
    val photos: List<AlbumPhoto>,
    val totalPhotoCount: Int,
    val totalContactCount: Int,
    val totalLocationCount: Int
)

@HiltViewModel
class PhotoAlbumViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val giftRepository: GiftRepository,
    private val contactRepository: ContactRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _selectedContactId = MutableStateFlow<Long?>(null)
    private val _filterSourceType = MutableStateFlow<String?>(null)
    private val _favoritePhotoIds = MutableStateFlow<Set<Long>>(emptySet())

    private val photoData = combine(
        eventRepository.getAllEventsIncludingConversations(),
        giftRepository.getAllGifts(),
        contactRepository.getAllContacts()
    ) { events, gifts, contacts ->
        val contactMap = contacts.associateBy { it.id }
        val allPhotos = mutableListOf<AlbumPhoto>()

        events.forEach { event ->
            event.photos.forEachIndexed { photoIndex, photoUri ->
                val participant = event.participants.firstOrNull()
                val sourceType = if (event.type == EventTypes.CONVERSATION) SourceTypes.ALBUM_CHAT else SourceTypes.ALBUM_EVENT
                allPhotos.add(
                    AlbumPhoto(
                        id = "${sourceType}_${event.id}_${photoIndex}",
                        uri = photoUri,
                        sourceType = sourceType,
                        sourceId = event.id,
                        sourceTitle = event.title,
                        contactId = participant?.id,
                        contactName = participant?.name,
                        contactAvatar = participant?.avatar,
                        date = event.time,
                        location = event.location
                    )
                )
            }
        }

        gifts.forEach { gift ->
            gift.photos.forEachIndexed { photoIndex, pathString ->
                val file = java.io.File(pathString.trim())
                if (file.exists()) {
                    allPhotos.add(
                        AlbumPhoto(
                            id = "gift_${gift.id}_${photoIndex}",
                            uri = android.net.Uri.fromFile(file).toString(),
                            sourceType = SourceTypes.ALBUM_GIFT,
                            sourceId = gift.id,
                            sourceTitle = gift.giftName,
                            contactId = gift.contactId,
                            contactName = contactMap[gift.contactId]?.name,
                            contactAvatar = contactMap[gift.contactId]?.avatar,
                            date = gift.date,
                            location = gift.location
                        )
                    )
                }
            }
        }

        allPhotos.sortByDescending { it.date }
        PhotoData(allPhotos, contacts)
    }

    private val filteredData = combine(
        photoData,
        _selectedContactId,
        _filterSourceType
    ) { data, selectedContactId, filterSourceType ->
        val totalPhotoCount = data.allPhotos.size
        val totalContactCount = data.allPhotos.mapNotNull { it.contactId }.distinct().size
        val totalLocationCount = data.allPhotos.mapNotNull { it.location }.distinct().size

        var filtered = data.allPhotos
        if (selectedContactId != null) {
            filtered = filtered.filter { it.contactId == selectedContactId }
        }
        if (filterSourceType != null) {
            filtered = filtered.filter { it.sourceType == filterSourceType }
        }

        FilteredData(filtered, totalPhotoCount, totalContactCount, totalLocationCount)
    }

    init {
        viewModelScope.launch {
            favoriteRepository.getFavoritesByType(SourceTypes.PHOTO).collect { favList ->
                _favoritePhotoIds.value = favList.map { it.sourceId }.toSet()
            }
        }
    }

    val uiState: StateFlow<PhotoAlbumUiState> = combine(
        filteredData,
        photoData.map { it.contacts },
        _favoritePhotoIds,
        _selectedContactId,
        _filterSourceType
    ) { filtered, contacts, favoritePhotoIds, selectedContactId, filterSourceType ->
        PhotoAlbumUiState(
            photos = filtered.photos,
            allContacts = contacts,
            selectedContactId = selectedContactId,
            filterSourceType = filterSourceType,
            isLoading = false,
            totalPhotoCount = filtered.totalPhotoCount,
            totalContactCount = filtered.totalContactCount,
            totalLocationCount = filtered.totalLocationCount,
            favoritePhotoIds = favoritePhotoIds
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PhotoAlbumUiState())

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
                    favoriteRepository.deleteFavoriteBySource(SourceTypes.PHOTO, photoId)
                } else {
                    favoriteRepository.toggleFavorite(
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
