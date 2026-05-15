package com.tang.prm.ui.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.FavoriteRepository
import com.tang.prm.domain.repository.GiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.tang.prm.util.ImageCacheManager
import javax.inject.Inject

data class GiftRecord(
    val gift: Gift,
    val contactName: String,
    val contactAvatar: String?
) {
    val id get() = gift.id
    val contactId get() = gift.contactId
    val giftName get() = gift.giftName
    val giftType get() = gift.giftType
    val date get() = gift.date
    val isSent get() = gift.isSent
    val amount get() = gift.amount
    val occasion get() = gift.occasion
    val description get() = gift.description
    val location get() = gift.location
    val photos: List<Uri> get() = gift.photos.map { Uri.parse(it) }
    val createdAt get() = gift.createdAt
}

data class GiftsUiState(
    val gifts: List<GiftRecord> = emptyList(),
    val filterType: String = "all",
    val selectedContactId: Long? = null,
    val availableContacts: List<com.tang.prm.domain.model.Contact> = emptyList(),
    val isLoading: Boolean = false,
    val favoriteGiftIds: Set<Long> = emptySet()
)

@HiltViewModel
class GiftsViewModel @Inject constructor(
    private val giftRepository: GiftRepository,
    private val contactRepository: ContactRepository,
    private val favoriteRepository: FavoriteRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(GiftsUiState())
    val uiState: StateFlow<GiftsUiState> = _uiState.asStateFlow()

    init {
        loadContactsAndGifts()
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            favoriteRepository.getFavoritesByType("GIFT").collect { favList ->
                _uiState.update { it.copy(favoriteGiftIds = favList.map { fav -> fav.sourceId }.toSet()) }
            }
        }
    }

    private fun loadContactsAndGifts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                contactRepository.getAllContacts(),
                giftRepository.getAllGifts()
            ) { contacts, giftList ->
                val contactMap = contacts.associateBy { it.id }
                val gifts = giftList.map { gift ->
                    GiftRecord(
                        gift = gift,
                        contactName = contactMap[gift.contactId]?.name ?: "未知人物",
                        contactAvatar = contactMap[gift.contactId]?.avatar
                    )
                }
                gifts to contacts
            }.collect { (gifts, contacts) ->
                _uiState.update { state ->
                    state.copy(
                        gifts = gifts,
                        availableContacts = contacts,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateFilterType(type: String) {
        _uiState.update { it.copy(filterType = type) }
    }

    fun filterByContact(contactId: Long?) {
        _uiState.update { it.copy(selectedContactId = contactId) }
    }

    fun clearContactFilter() {
        _uiState.update { it.copy(selectedContactId = null) }
    }

    fun addGift(gift: GiftRecord) {
        viewModelScope.launch {
            val savedPaths = mutableListOf<String>()
            gift.photos.forEach { uri ->
                val path = ImageCacheManager.copyToInternalStorage(context, uri, "gift")
                if (path != null) {
                    savedPaths.add(path)
                }
            }

            val newGift = gift.gift.copy(
                id = 0,
                photos = savedPaths,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            giftRepository.insertGift(newGift)
        }
    }

    fun deleteGift(giftId: Long) {
        viewModelScope.launch {
            giftRepository.deleteGiftById(giftId)
        }
    }

    fun updateGift(gift: GiftRecord) {
        viewModelScope.launch {
            giftRepository.updateGift(gift.gift.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun getGiftFlow(giftId: Long): Flow<Gift?> = giftRepository.getGiftById(giftId)

    fun toggleFavorite(giftId: Long, giftName: String, contactName: String) {
        val wasFavorite = _uiState.value.favoriteGiftIds.contains(giftId)
        val newSet = if (wasFavorite) _uiState.value.favoriteGiftIds - giftId else _uiState.value.favoriteGiftIds + giftId
        _uiState.update { it.copy(favoriteGiftIds = newSet) }
        viewModelScope.launch {
            try {
                favoriteRepository.toggleFavorite(
                    type = SourceTypes.GIFT,
                    sourceId = giftId,
                    title = giftName,
                    description = "来自" + contactName + "的礼物"
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(favoriteGiftIds = _uiState.value.favoriteGiftIds.let { if (wasFavorite) it + giftId else it - giftId }) }
            }
        }
    }
}
