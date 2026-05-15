package com.tang.prm.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.*
import com.tang.prm.domain.model.AppStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IntimacyStats(
    val level: String,
    val count: Int
)

data class ContactsUiState(
    val contacts: List<Contact> = emptyList(),
    val groups: List<ContactGroup> = emptyList(),
    val relationships: List<CustomType> = emptyList(),
    val searchQuery: String = "",
    val selectedGroupId: Long? = null,
    val selectedRelationship: String? = null,
    val selectedIntimacy: String? = null,
    val viewMode: Int = 0,
    val selectedCardId: Long? = null,
    val flippedCardId: Long? = null,
    val isReorderMode: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val groupRepository: ContactGroupRepository,
    private val customTypeRepository: CustomTypeRepository
) : ViewModel() {

    companion object {
        val IntimacyLevels = listOf(AppStrings.Intimacy.NEW, AppStrings.Intimacy.ACQUAINTANCE, AppStrings.Intimacy.FRIEND, AppStrings.Intimacy.CLOSE, AppStrings.Intimacy.FAMILY)

        fun getIntimacyLevel(score: Int): String {
            return when {
                score <= 20 -> AppStrings.Intimacy.NEW
                score <= 40 -> AppStrings.Intimacy.ACQUAINTANCE
                score <= 60 -> AppStrings.Intimacy.FRIEND
                score <= 80 -> AppStrings.Intimacy.CLOSE
                else -> AppStrings.Intimacy.FAMILY
            }
        }

        private const val DEBOUNCE_MS = 300L
    }

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
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
                _searchQuery
                    .debounce { query -> if (query.isBlank()) 0L else DEBOUNCE_MS },
                _selectedGroupId,
                _selectedRelationship,
                _selectedIntimacy
            ) { query, groupId, relationship, intimacy ->
                FilterParams(query, groupId, relationship, intimacy)
            }.flatMapLatest { params ->
                val keyword = params.query.ifBlank { null }
                val filtered = contactRepository.getFilteredContacts(keyword, params.groupId, params.relationship)
                if (params.intimacy != null) {
                    filtered.map { contacts -> contacts.filter { getIntimacyLevel(it.intimacyScore) == params.intimacy } }
                } else {
                    filtered
                }
            }.collect { filteredContacts ->
                val sorted = if (_uiState.value.isReorderMode) {
                    filteredContacts
                } else {
                    filteredContacts.sortedByDescending { it.intimacyScore }
                }
                _uiState.update { it.copy(contacts = sorted, isLoading = false) }
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
                _uiState.update { it.copy(groups = groups) }
            }
        }
    }

    private fun loadRelationships() {
        viewModelScope.launch {
            customTypeRepository.getTypesByCategory(CustomCategories.RELATIONSHIP).collect { types ->
                _uiState.update { it.copy(relationships = types) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onGroupSelected(groupId: Long?) {
        _selectedGroupId.value = groupId
        _uiState.update { it.copy(selectedGroupId = groupId) }
    }

    fun onRelationshipSelected(relationship: String?) {
        _selectedRelationship.value = relationship
        _uiState.update { it.copy(selectedRelationship = relationship) }
    }

    fun onIntimacySelected(intimacy: String?) {
        _selectedIntimacy.value = intimacy
        _uiState.update { it.copy(selectedIntimacy = intimacy) }
    }

    fun onViewModeChange(mode: Int) {
        _uiState.update { it.copy(viewMode = mode, selectedCardId = null, flippedCardId = null, isReorderMode = false) }
    }

    fun toggleReorderMode() {
        val newMode = !_uiState.value.isReorderMode
        _uiState.update { it.copy(isReorderMode = newMode) }
    }

    fun moveContact(fromIndex: Int, toIndex: Int) {
        val current = _uiState.value.contacts.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _uiState.update { it.copy(contacts = current) }
        }
    }

    fun selectCard(contactId: Long?) {
        _uiState.update { it.copy(selectedCardId = contactId, flippedCardId = null) }
    }

    fun toggleCardFlip(contactId: Long) {
        _uiState.update { state ->
            val newFlippedId = if (state.flippedCardId == contactId) null else contactId
            state.copy(flippedCardId = newFlippedId)
        }
    }

    fun deleteContact(id: Long) {
        viewModelScope.launch {
            contactRepository.deleteContact(id)
        }
    }
}
