package com.tang.prm.feature.encounter.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.*
import com.tang.prm.domain.usecase.filterBy
import com.tang.prm.ui.common.SearchState
import com.tang.prm.ui.common.SearchStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventsDataState(
    val events: List<Event> = emptyList(),
    val displayEvents: List<Event> = emptyList(),
    val eventTypes: List<CustomType> = emptyList(),
    val selectedType: String? = null,
    val selectedContact: Contact? = null,
    val availableContacts: List<Contact> = emptyList(),
    val selectedDate: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val viewMode: String = "list"
)

data class EventsDialogState(
    val showDeleteConfirm: Long? = null
)

data class EventsUiState(
    val data: EventsDataState = EventsDataState(),
    val dialog: EventsDialogState = EventsDialogState()
)

@OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val contactRepository: ContactRepository,
    private val customTypeRepository: CustomTypeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventsUiState())
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    private val _selectedType = MutableStateFlow<String?>(null)
    private val _selectedContact = MutableStateFlow<Contact?>(null)
    private val searchManager = SearchStateManager()
    val searchState: StateFlow<SearchState> = searchManager.state

    init {
        loadContacts()
        loadEventTypes()
        loadEvents()
    }

    private fun loadContacts() {
        viewModelScope.launch {
            contactRepository.getAllContacts().collect { contacts ->
                _uiState.update { it.copy(data = it.data.copy(availableContacts = contacts)) }
            }
        }
    }

    private fun loadEventTypes() {
        viewModelScope.launch {
            customTypeRepository.getTypesByCategory(CustomCategories.EVENT_TYPE).collect { types ->
                _uiState.update { it.copy(data = it.data.copy(eventTypes = types)) }
            }
        }
    }

    private fun loadEvents() {
        viewModelScope.launch {
            try {
                combine(
                    _selectedType,
                    _selectedContact,
                    searchManager.state
                        .map { it.query }
                        .debounce { query -> if (query.isBlank()) 0L else DEBOUNCE_MS }
                ) { type: String?, contact: Contact?, query: String ->
                    Triple(type, contact, query)
                }.flatMapLatest { (type, contact, query) ->
                    eventRepository.getAllEvents().map { events ->
                        events.filterBy(contact, type, query)
                    }
                }.collect { filteredEvents: List<Event> ->
                    _uiState.update { it.copy(data = it.data.copy(events = filteredEvents, displayEvents = filteredEvents, isLoading = false)) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(data = it.data.copy(error = e.message, isLoading = false)) }
            }
        }
    }

    fun selectType(type: String?) {
        _selectedType.value = type
        _uiState.update { it.copy(data = it.data.copy(selectedType = type)) }
    }

    fun selectContact(contact: Contact?) {
        _selectedContact.value = contact
        _uiState.update { it.copy(data = it.data.copy(selectedContact = contact)) }
    }

    fun selectDate(dateMillis: Long) {
        _uiState.update { it.copy(data = it.data.copy(selectedDate = dateMillis)) }
    }

    fun onSearchQueryChange(query: String) = searchManager.onQueryChange(query)

    fun deleteEvent(id: Long) {
        viewModelScope.launch {
            eventRepository.deleteEvent(id)
        }
    }

    fun onViewModeChange(mode: String) {
        _uiState.update { it.copy(data = it.data.copy(viewMode = mode)) }
    }

    fun clearError() {
        _uiState.update { it.copy(data = it.data.copy(error = null)) }
    }

    companion object {
        private const val DEBOUNCE_MS = 300L
    }
}
