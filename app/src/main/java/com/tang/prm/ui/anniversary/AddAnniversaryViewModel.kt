package com.tang.prm.ui.anniversary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.repository.AnniversaryRepository
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddAnniversaryViewModel @Inject constructor(
    private val anniversaryRepository: AnniversaryRepository,
    private val contactRepository: ContactRepository,
    private val customTypeRepository: CustomTypeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val anniversaryId: Long = savedStateHandle.get<Long>("anniversaryId") ?: 0L

    private val _uiState = MutableStateFlow(AddAnniversaryUiState())
    val uiState: StateFlow<AddAnniversaryUiState> = _uiState.asStateFlow()

    init {
        loadContacts()
        loadAnniversaryTypes()
        if (anniversaryId != 0L) {
            loadAnniversary(anniversaryId)
        }
    }

    private fun loadContacts() {
        viewModelScope.launch {
            contactRepository.getAllContacts().collect { contacts ->
                _uiState.update { it.copy(contacts = contacts) }
            }
        }
    }

    private fun loadAnniversaryTypes() {
        viewModelScope.launch {
            customTypeRepository.getTypesByCategory(CustomCategories.ANNIVERSARY_TYPE).collect { types ->
                _uiState.update { it.copy(anniversaryTypes = types) }
            }
        }
    }

    private fun loadAnniversary(id: Long) {
        viewModelScope.launch {
            anniversaryRepository.getAnniversaryById(id).collect { anniversary ->
                anniversary?.let {
                    _uiState.update { state ->
                        state.copy(
                            id = it.id,
                            name = it.name,
                            contactId = if (it.contactId > 0) it.contactId else null,
                            contactName = it.contactName,
                            selectedType = it.type,
                            selectedIcon = it.icon ?: "Cake",
                            date = it.date,
                            dateText = DateUtils.formatDate(it.date),
                            isLunar = it.isLunar,
                            isRepeat = it.isRepeat,
                            remarks = it.remarks ?: "",
                            isEditing = true,
                            createdAt = it.createdAt
                        )
                    }
                }
            }
        }
    }

    fun updateName(value: String) = _uiState.update { it.copy(name = value, hasUnsavedChanges = true) }
    fun updateContact(contact: Contact?) = _uiState.update {
        it.copy(contactId = contact?.id, contactName = contact?.name, hasUnsavedChanges = true)
    }
    fun updateSelectedType(value: AnniversaryType) = _uiState.update { it.copy(selectedType = value, hasUnsavedChanges = true) }
    fun updateTypeName(value: String) = _uiState.update { it.copy(typeName = value, hasUnsavedChanges = true) }
    fun updateSelectedIcon(value: String) = _uiState.update { it.copy(selectedIcon = value, hasUnsavedChanges = true) }
    fun updateDate(millis: Long) {
        val dateText = DateUtils.formatDate(millis)
        _uiState.update { it.copy(date = millis, dateText = dateText, hasUnsavedChanges = true) }
    }
    fun updateIsLunar(value: Boolean) = _uiState.update { it.copy(isLunar = value, hasUnsavedChanges = true) }
    fun updateIsRepeat(value: Boolean) = _uiState.update { it.copy(isRepeat = value, hasUnsavedChanges = true) }
    fun updateRemarks(value: String) = _uiState.update { it.copy(remarks = value, hasUnsavedChanges = true) }

    fun addAnniversaryType(name: String) {
        viewModelScope.launch {
            val newType = CustomType(
                category = CustomCategories.ANNIVERSARY_TYPE,
                name = name,
                sortOrder = _uiState.value.anniversaryTypes.size
            )
            customTypeRepository.insertType(newType)
        }
    }

    fun deleteAnniversaryType(type: CustomType) {
        viewModelScope.launch {
            customTypeRepository.deleteTypeById(type.id)
        }
    }

    fun saveAnniversary() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.name.isBlank()) return@launch

            val anniversary = Anniversary(
                id = if (state.isEditing) state.id else 0,
                name = state.name,
                contactId = state.contactId ?: 0,
                type = state.selectedType,
                date = state.date,
                isLunar = state.isLunar,
                isRepeat = state.isRepeat,
                reminderDays = 1,
                remarks = state.remarks.ifBlank { null },
                contactName = state.contactName,
                icon = state.selectedIcon,
                createdAt = if (state.isEditing) state.createdAt else System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            if (state.isEditing) {
                anniversaryRepository.updateAnniversary(anniversary)
            } else {
                anniversaryRepository.insertAnniversary(anniversary)
            }

            _uiState.update { it.copy(isSaved = true) }
        }
    }
}
