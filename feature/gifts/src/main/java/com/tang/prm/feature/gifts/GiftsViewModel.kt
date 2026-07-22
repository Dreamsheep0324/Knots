package com.tang.prm.feature.gifts

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.model.GiftType
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.GiftRepository
import com.tang.prm.domain.usecase.FavoriteToggleUseCase
import com.tang.prm.domain.usecase.ObserveFavoritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GiftRecord(
    val gift: Gift,
    val contactName: String,
    val contactAvatar: String?
) {
    val id get() = gift.id
    val contactId get() = gift.contactId
    val giftName get() = gift.giftName
    val giftType: GiftType get() = gift.giftType
    val date get() = gift.date
    val isSent get() = gift.isSent
    val occasion get() = gift.occasion
    val description get() = gift.description
    val location get() = gift.location
    val photos: List<Uri> get() = gift.photos.map { Uri.parse(it) }
    val createdAt get() = gift.createdAt
}

data class GiftsDataState(
    val gifts: List<GiftRecord> = emptyList(),
    val filterType: String = "all",
    val selectedContactId: Long? = null,
    val availableContacts: List<com.tang.prm.domain.model.Contact> = emptyList(),
    val isLoading: Boolean = false,
    val favoriteGiftIds: Set<Long> = emptySet()
)

data class GiftsDialogState(
    val photoSaveErrorCount: Int = 0
)

data class GiftsUiState(
    val data: GiftsDataState = GiftsDataState(),
    val dialog: GiftsDialogState = GiftsDialogState()
)

@HiltViewModel
class GiftsViewModel @Inject constructor(
    private val giftRepository: GiftRepository,
    private val contactRepository: ContactRepository,
    private val favoriteToggleUseCase: FavoriteToggleUseCase,
    private val observeFavoritesUseCase: ObserveFavoritesUseCase
) : ViewModel() {

    private val _filterType = MutableStateFlow("all")
    private val _selectedContactId = MutableStateFlow<Long?>(null)
    private val _dialogState = MutableStateFlow(GiftsDialogState())

    val uiState: StateFlow<GiftsUiState> = combine(
        combine(
            contactRepository.getAllContacts(),
            giftRepository.getAllGifts(),
            observeFavoritesUseCase.getFavoriteIds("GIFT"),
            _filterType,
            _selectedContactId
        ) { contacts, giftList, favoriteIds, filterType, selectedContactId ->
            val contactMap = contacts.associateBy { it.id }
            val gifts = giftList.map { gift ->
                GiftRecord(
                    gift = gift,
                    contactName = contactMap[gift.contactId]?.name ?: "未知人物",
                    contactAvatar = contactMap[gift.contactId]?.avatar
                )
            }
            GiftsDataState(
                gifts = gifts,
                filterType = filterType,
                selectedContactId = selectedContactId,
                availableContacts = contacts,
                isLoading = false,
                favoriteGiftIds = favoriteIds
            )
        },
        _dialogState
    ) { data, dialog ->
        GiftsUiState(data = data, dialog = dialog)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GiftsUiState())

    fun updateFilterType(type: String) {
        _filterType.value = type
    }

    fun filterByContact(contactId: Long?) {
        _selectedContactId.value = contactId
    }

    fun clearContactFilter() {
        _selectedContactId.value = null
    }

    fun addGift(gift: GiftRecord) {
        viewModelScope.launch {
            val (id, failedCount) = giftRepository.saveGiftWithPhotos(gift.gift, gift.photos.map { it.toString() })
            if (failedCount > 0) {
                _dialogState.value = _dialogState.value.copy(photoSaveErrorCount = failedCount)
            }
        }
    }

    fun clearPhotoSaveError() {
        _dialogState.value = _dialogState.value.copy(photoSaveErrorCount = 0)
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
        viewModelScope.launch {
            try {
                favoriteToggleUseCase(
                    type = SourceTypes.GIFT,
                    sourceId = giftId,
                    title = giftName,
                    description = "来自" + contactName + "的礼物"
                )
            } catch (e: Exception) {
                // Error handling - favoriteIds will auto-correct from the flow
            }
        }
    }
}
