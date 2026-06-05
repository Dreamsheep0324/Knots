package com.tang.prm.feature.people.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.*
import com.tang.prm.domain.usecase.IntimacyLevels
import com.tang.prm.domain.usecase.getIntimacyLevel
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

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val groupRepository: ContactGroupRepository,
    private val customTypeRepository: CustomTypeRepository
) : ViewModel() {

    companion object {
        private const val DEBOUNCE_MS = 300L
    }

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    private val searchManager = SearchStateManager()
    val searchState: StateFlow<SearchState> = searchManager.state

    private val _selectedGroupId = MutableStateFlow<Long?>(null)
    private val _selectedRelationship = MutableStateFlow<String?>(null)
    private val _selectedIntimacy = MutableStateFlow<String?>(null)

    init {
        loadContacts()
        loadGroups()
        loadRelationships()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadContacts() {
        viewModelScope.launch {
            combine(
                searchManager.state
                    .map { it.query }
                    .debounce { query -> if (query.isBlank()) 0L else DEBOUNCE_MS },
                _selectedGroupId,
                _selectedRelationship,
                _selectedIntimacy
            ) { query, groupId, relationship, intimacy ->
                FilterParams(query, groupId, relationship, intimacy)
            }.flatMapLatest { params ->
                val keyword = params.query.ifBlank { null }
                contactRepository.getAllContacts().map { contacts ->
                    contacts.filterBy(keyword, params.groupId, params.relationship, params.intimacy)
                }
            }.collect { filteredContacts ->
                val sorted = if (_uiState.value.data.isReorderMode) {
                    filteredContacts
                } else {
                    filteredContacts.sortedByDescending { it.intimacyScore }
                }
                _uiState.update { it.copy(data = it.data.copy(contacts = sorted, isLoading = false)) }
            }
        }
    }

    private data class FilterParams(
        val query: String,
        val groupId: Long?,
        val relationship: String?,
        val intimacy: String?
    )

    fun getIntimacyStats(contacts: List<Contact>): List<IntimacyStats> {
        return IntimacyLevels.map { level ->
            val count = contacts.count { getIntimacyLevel(it.intimacyScore) == level }
            IntimacyStats(level, count)
        }
    }

    fun getContactsGroupedByIntimacy(contacts: List<Contact>): Map<String, List<Contact>> {
        return contacts.groupBy { getIntimacyLevel(it.intimacyScore) }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            groupRepository.getAllGroups().collect { groups ->
                _uiState.update { it.copy(data = it.data.copy(groups = groups)) }
            }
        }
    }

    private fun loadRelationships() {
        viewModelScope.launch {
            customTypeRepository.getTypesByCategory(CustomCategories.RELATIONSHIP).collect { types ->
                _uiState.update { it.copy(data = it.data.copy(relationships = types)) }
            }
        }
    }

    fun onSearchQueryChange(query: String) = searchManager.onQueryChange(query)

    fun onGroupSelected(groupId: Long?) {
        _selectedGroupId.value = groupId
        _uiState.update { it.copy(data = it.data.copy(selectedGroupId = groupId)) }
    }

    fun onRelationshipSelected(relationship: String?) {
        _selectedRelationship.value = relationship
        _uiState.update { it.copy(data = it.data.copy(selectedRelationship = relationship)) }
    }

    fun onIntimacySelected(intimacy: String?) {
        _selectedIntimacy.value = intimacy
        _uiState.update { it.copy(data = it.data.copy(selectedIntimacy = intimacy)) }
    }

    fun onViewModeChange(mode: Int) {
        _uiState.update { it.copy(data = it.data.copy(viewMode = mode, isReorderMode = false), dialog = ContactsDialogState()) }
    }

    fun toggleReorderMode() {
        val newMode = !_uiState.value.data.isReorderMode
        _uiState.update { it.copy(data = it.data.copy(isReorderMode = newMode)) }
    }

    fun moveContact(fromIndex: Int, toIndex: Int) {
        val current = _uiState.value.data.contacts.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _uiState.update { it.copy(data = it.data.copy(contacts = current)) }
        }
    }

    fun selectCard(contactId: Long?) {
        _uiState.update { it.copy(dialog = it.dialog.copy(selectedCardId = contactId, flippedCardId = null)) }
    }

    fun toggleCardFlip(contactId: Long) {
        _uiState.update { state ->
            val newFlippedId = if (state.dialog.flippedCardId == contactId) null else contactId
            state.copy(dialog = state.dialog.copy(flippedCardId = newFlippedId))
        }
    }

    fun deleteContact(id: Long) {
        viewModelScope.launch {
            contactRepository.deleteContact(id)
        }
    }
}
