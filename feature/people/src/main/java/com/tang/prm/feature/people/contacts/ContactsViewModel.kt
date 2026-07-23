package com.tang.prm.feature.people.contacts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.ContactGroup
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.repository.ContactGroupRepository
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.domain.util.filterBy
import com.tang.prm.ui.common.SearchState
import com.tang.prm.ui.common.SearchStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

data class ContactsDataState(
    val contacts: List<Contact> = emptyList(),
    val groups: List<ContactGroup> = emptyList(),
    val relationships: List<CustomType> = emptyList(),
    val selectedRelationship: String? = null,
    val viewMode: Int = 0,
    val isReorderMode: Boolean = false,
    val isLoading: Boolean = false
)

data class ContactsDialogState(
    val selectedCardId: Long? = null,
    val flippedCardId: Long? = null
)

data class ContactsUiState(
    val data: ContactsDataState = ContactsDataState(),
    val dialog: ContactsDialogState = ContactsDialogState()
)

private data class FilterParams(
    val query: String,
    val relationship: String?
)

private data class SelectionState(
    val relationship: String?,
    val viewMode: Int,
    val isReorderMode: Boolean
)

private data class DataBundle(
    val contacts: List<Contact>,
    val groups: List<ContactGroup>,
    val relationships: List<CustomType>
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val groupRepository: ContactGroupRepository,
    private val customTypeRepository: CustomTypeRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ContactsViewModel"
        private const val DEBOUNCE_MS = 300L
    }

    private val searchManager = SearchStateManager()
    val searchState: StateFlow<SearchState> = searchManager.state

    private val _selectedRelationship = MutableStateFlow<String?>(null)
    private val _viewMode = MutableStateFlow(0)
    private val _isReorderMode = MutableStateFlow(false)
    private val _dialogState = MutableStateFlow(ContactsDialogState())
    private val _manualOrder = MutableStateFlow<List<Long>?>(null)

    private val filterState = combine(
        searchManager.state
            .map { it.query }
            .debounce { query -> if (query.isBlank()) 0L else DEBOUNCE_MS },
        _selectedRelationship
    ) { query, relationship ->
        FilterParams(query, relationship)
    }

    private val filteredContacts = filterState.flatMapLatest { params ->
        val keyword = params.query.ifBlank { null }
        contactRepository.getContactListItems().map { contacts ->
            contacts.filterBy(keyword = keyword, relationship = params.relationship)
        }
    }

    val uiState: StateFlow<ContactsUiState> = combine(
        combine(
            filteredContacts,
            groupRepository.getAllGroups(),
            customTypeRepository.getTypesByCategory(CustomCategories.RELATIONSHIP)
        ) { contacts, groups, relationships ->
            DataBundle(contacts, groups, relationships)
        },
        combine(
            _selectedRelationship,
            _viewMode,
            _isReorderMode
        ) { relationship, viewMode, isReorderMode ->
            SelectionState(relationship, viewMode, isReorderMode)
        },
        _dialogState,
        _manualOrder
    ) { dataBundle, selection, dialog, manualOrder ->
        // 非排序模式：按亲密度降序；排序模式：优先使用用户手动调整的顺序，未调整时沿用当前显示顺序（避免进入排序模式时顺序突变）
        val sorted = if (selection.isReorderMode) {
            if (manualOrder != null) {
                val orderMap = manualOrder.withIndex().associate { it.value to it.index }
                dataBundle.contacts.sortedBy { orderMap[it.id] ?: Int.MAX_VALUE }
            } else {
                dataBundle.contacts.sortedByDescending { it.intimacyScore }
            }
        } else {
            dataBundle.contacts.sortedByDescending { it.intimacyScore }
        }

        ContactsUiState(
            data = ContactsDataState(
                contacts = sorted,
                groups = dataBundle.groups,
                relationships = dataBundle.relationships,
                selectedRelationship = selection.relationship,
                viewMode = selection.viewMode,
                isReorderMode = selection.isReorderMode,
                isLoading = false
            ),
            dialog = dialog
        )
    }.catch { e ->
        Log.e(TAG, "联系人数据流异常", e)
        emit(ContactsUiState())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ContactsUiState())

    fun onSearchQueryChange(query: String) = searchManager.onQueryChange(query)

    fun onRelationshipSelected(relationship: String?) {
        _selectedRelationship.value = relationship
    }

    fun onViewModeChange(mode: Int) {
        _viewMode.value = mode
        _isReorderMode.value = false
        _manualOrder.value = null
        _dialogState.value = ContactsDialogState()
    }

    fun toggleReorderMode() {
        _isReorderMode.value = !_isReorderMode.value
        // 保留 _manualOrder，让用户再次进入排序模式时仍能看到上次调整的顺序；
        // 仅在切换视图模式（onViewModeChange）时清空。
    }

    fun moveContact(fromIndex: Int, toIndex: Int) {
        val current = uiState.value.data.contacts
        if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
            val reordered = current.toMutableList()
            val item = reordered.removeAt(fromIndex)
            reordered.add(toIndex, item)
            _manualOrder.value = reordered.map { it.id }
        }
    }

    fun selectCard(contactId: Long?) {
        _dialogState.value = _dialogState.value.copy(selectedCardId = contactId, flippedCardId = null)
    }

    fun toggleCardFlip(contactId: Long) {
        val current = _dialogState.value
        val newFlippedId = if (current.flippedCardId == contactId) null else contactId
        _dialogState.value = current.copy(flippedCardId = newFlippedId)
    }

}
