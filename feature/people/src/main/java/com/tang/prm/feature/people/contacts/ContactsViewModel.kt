package com.tang.prm.feature.people.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.*
import com.tang.prm.domain.model.IntimacyTier
import com.tang.prm.domain.usecase.IntimacyLevels
import com.tang.prm.domain.usecase.filterBy
import com.tang.prm.ui.common.SearchState
import com.tang.prm.ui.common.SearchStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IntimacyStats(
    val level: String,
    val count: Int
)

data class ContactsDataState(
    val contacts: List<Contact> = emptyList(),
    val groups: List<ContactGroup> = emptyList(),
    val relationships: List<CustomType> = emptyList(),
    val selectedGroupId: Long? = null,
    val selectedRelationship: String? = null,
    val selectedIntimacy: String? = null,
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
    val groupId: Long?,
    val relationship: String?,
    val intimacy: String?
)

private data class SelectionState(
    val groupId: Long?,
    val relationship: String?,
    val intimacy: String?,
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
        private const val DEBOUNCE_MS = 300L
    }

    private val searchManager = SearchStateManager()
    val searchState: StateFlow<SearchState> = searchManager.state

    private val _selectedGroupId = MutableStateFlow<Long?>(null)
    private val _selectedRelationship = MutableStateFlow<String?>(null)
    private val _selectedIntimacy = MutableStateFlow<String?>(null)
    private val _viewMode = MutableStateFlow(0)
    private val _isReorderMode = MutableStateFlow(false)
    private val _dialogState = MutableStateFlow(ContactsDialogState())
    private val _manualOrder = MutableStateFlow<List<Long>?>(null)

    private val filterState = combine(
        searchManager.state
            .map { it.query }
            .debounce { query -> if (query.isBlank()) 0L else DEBOUNCE_MS },
        _selectedGroupId,
        _selectedRelationship,
        _selectedIntimacy
    ) { query, groupId, relationship, intimacy ->
        FilterParams(query, groupId, relationship, intimacy)
    }

    private val filteredContacts = filterState.flatMapLatest { params ->
        val keyword = params.query.ifBlank { null }
        contactRepository.getContactListItems().map { contacts ->
            contacts.filterBy(keyword, params.groupId, params.relationship, params.intimacy)
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
            _selectedGroupId,
            _selectedRelationship,
            _selectedIntimacy,
            _viewMode,
            _isReorderMode
        ) { groupId, relationship, intimacy, viewMode, isReorderMode ->
            SelectionState(groupId, relationship, intimacy, viewMode, isReorderMode)
        },
        _dialogState,
        _manualOrder
    ) { dataBundle, selection, dialog, manualOrder ->
        val sorted = if (selection.isReorderMode && manualOrder != null) {
            val orderMap = manualOrder.withIndex().associate { it.value to it.index }
            dataBundle.contacts.sortedBy { orderMap[it.id] ?: Int.MAX_VALUE }
        } else if (selection.isReorderMode) {
            dataBundle.contacts
        } else {
            dataBundle.contacts.sortedByDescending { it.intimacyScore }
        }

        ContactsUiState(
            data = ContactsDataState(
                contacts = sorted,
                groups = dataBundle.groups,
                relationships = dataBundle.relationships,
                selectedGroupId = selection.groupId,
                selectedRelationship = selection.relationship,
                selectedIntimacy = selection.intimacy,
                viewMode = selection.viewMode,
                isReorderMode = selection.isReorderMode,
                isLoading = false
            ),
            dialog = dialog
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ContactsUiState())

    fun getIntimacyStats(contacts: List<Contact>): List<IntimacyStats> {
        return IntimacyLevels.map { level ->
            val count = contacts.count { IntimacyTier.of(it.intimacyScore).label == level }
            IntimacyStats(level, count)
        }
    }

    fun getContactsGroupedByIntimacy(contacts: List<Contact>): Map<String, List<Contact>> {
        return contacts.groupBy { IntimacyTier.of(it.intimacyScore).label }
    }

    fun onSearchQueryChange(query: String) = searchManager.onQueryChange(query)

    fun onGroupSelected(groupId: Long?) {
        _selectedGroupId.value = groupId
    }

    fun onRelationshipSelected(relationship: String?) {
        _selectedRelationship.value = relationship
    }

    fun onIntimacySelected(intimacy: String?) {
        _selectedIntimacy.value = intimacy
    }

    fun onViewModeChange(mode: Int) {
        _viewMode.value = mode
        _isReorderMode.value = false
        _manualOrder.value = null
        _dialogState.value = ContactsDialogState()
    }

    fun toggleReorderMode() {
        val newMode = !_isReorderMode.value
        _isReorderMode.value = newMode
        if (!newMode) {
            _manualOrder.value = null
        }
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

    fun deleteContact(id: Long) {
        viewModelScope.launch {
            contactRepository.deleteContact(id)
        }
    }
}
