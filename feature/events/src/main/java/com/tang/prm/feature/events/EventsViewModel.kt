package com.tang.prm.feature.events

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
import java.util.Calendar
import javax.inject.Inject

data class CalendarStats(
    val eventCount: Int = 0,
    val participantCount: Int = 0,
    val totalSpending: Double = 0.0
)

data class EventsDataState(
    val events: List<Event> = emptyList(),
    val displayEvents: List<Event> = emptyList(),
    val eventTypes: List<CustomType> = emptyList(),
    val selectedType: String? = null,
    val selectedContact: Contact? = null,
    val availableContacts: List<Contact> = emptyList(),
    val selectedDate: Long = System.currentTimeMillis(),
    val isLoading: Boolean = true,
    val viewMode: String = "list",
    val calendarMonthOffset: Int = 0,
    val selectedCalendarDate: Long = System.currentTimeMillis(),
    val calendarEvents: List<Event> = emptyList(),
    val selectedDateEvents: List<Event> = emptyList(),
    val calendarStats: CalendarStats = CalendarStats()
)

data class EventsDialogState(
    val showDeleteConfirm: Long? = null
)

private data class UiSelections(
    val selectedType: String?,
    val selectedContact: Contact?,
    val selectedDate: Long,
    val viewMode: String,
    val dialog: EventsDialogState
)

private data class CalendarSelectionState(
    val monthOffset: Int,
    val selectedDate: Long
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

    private val _selectedType = MutableStateFlow<String?>(null)
    private val _selectedContact = MutableStateFlow<Contact?>(null)
    private val searchManager = SearchStateManager()
    val searchState: StateFlow<SearchState> = searchManager.state

    private val _dialogState = MutableStateFlow(EventsDialogState())
    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    private val _viewMode = MutableStateFlow("list")
    private val _calendarMonthOffset = MutableStateFlow(0)
    private val _selectedCalendarDate = MutableStateFlow(System.currentTimeMillis())

    private val filterState = combine(
        _selectedType,
        _selectedContact,
        searchManager.state
            .map { it.query }
            .debounce { query -> if (query.isBlank()) 0L else DEBOUNCE_MS }
    ) { type: String?, contact: Contact?, query: String ->
        Triple(type, contact, query)
    }

    private val dataFlow = combine(
        contactRepository.getAllContacts().distinctUntilChanged(),
        customTypeRepository.getTypesByCategory(CustomCategories.EVENT_TYPE).distinctUntilChanged(),
        filterState.flatMapLatest { (type, contact, query) ->
            eventRepository.getAllEvents().map { events ->
                events.filterBy(contact, type, query)
            }
        }
    ) { contacts, eventTypes, filteredEvents ->
        Triple(contacts, eventTypes, filteredEvents)
    }

    private val uiSelections = combine(
        _selectedType, _selectedContact, _selectedDate, _viewMode, _dialogState
    ) { type, contact, date, mode, dialog ->
        UiSelections(type, contact, date, mode, dialog)
    }

    private val calendarSelection = combine(
        _calendarMonthOffset, _selectedCalendarDate
    ) { offset, selectedDate ->
        CalendarSelectionState(offset, selectedDate)
    }

    val uiState: StateFlow<EventsUiState> = combine(dataFlow, uiSelections, calendarSelection) { data, ui, cal ->
        val (contacts, eventTypes, filteredEvents) = data

        val monthRange = getMonthRange(cal.monthOffset)
        val calendarEvents = filteredEvents.filter { event ->
            event.time >= monthRange.first && event.time < monthRange.second
        }

        val selectedDateStart = getDayStart(cal.selectedDate)
        val selectedDateEnd = selectedDateStart + MILLIS_PER_DAY
        val selectedDateEvents = filteredEvents.filter { event ->
            event.time >= selectedDateStart && event.time < selectedDateEnd
        }.sortedBy { it.time }

        val stats = CalendarStats(
            eventCount = calendarEvents.size,
            participantCount = calendarEvents.flatMap { it.participants }.distinctBy { it.id }.size,
            totalSpending = calendarEvents.sumOf { it.amount ?: 0.0 }
        )

        EventsUiState(
            data = EventsDataState(
                events = filteredEvents,
                displayEvents = filteredEvents,
                eventTypes = eventTypes,
                selectedType = ui.selectedType,
                selectedContact = ui.selectedContact,
                availableContacts = contacts,
                selectedDate = ui.selectedDate,
                isLoading = false,
                viewMode = ui.viewMode,
                calendarMonthOffset = cal.monthOffset,
                selectedCalendarDate = cal.selectedDate,
                calendarEvents = calendarEvents,
                selectedDateEvents = selectedDateEvents,
                calendarStats = stats
            ),
            dialog = ui.dialog
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(30_000), EventsUiState())

    fun selectType(type: String?) {
        _selectedType.value = type
    }

    fun selectContact(contact: Contact?) {
        _selectedContact.value = contact
    }

    fun selectDate(dateMillis: Long) {
        _selectedDate.value = dateMillis
    }

    fun onSearchQueryChange(query: String) = searchManager.onQueryChange(query)

    fun deleteEvent(id: Long) {
        viewModelScope.launch {
            eventRepository.deleteEvent(id)
        }
    }

    fun onViewModeChange(mode: String) {
        _viewMode.value = mode
    }

    fun showDeleteConfirm(id: Long?) {
        _dialogState.value = EventsDialogState(showDeleteConfirm = id)
    }

    fun onPreviousMonth() {
        _calendarMonthOffset.value--
    }

    fun onNextMonth() {
        _calendarMonthOffset.value++
    }

    fun onTodayClick() {
        _calendarMonthOffset.value = 0
        _selectedCalendarDate.value = System.currentTimeMillis()
    }

    fun onCalendarDateSelected(dateMillis: Long) {
        _selectedCalendarDate.value = dateMillis
    }

    companion object {
        private const val DEBOUNCE_MS = 300L
        private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000

        fun getMonthRange(monthOffset: Int): Pair<Long, Long> {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, monthOffset)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val start = cal.timeInMillis
            cal.add(Calendar.MONTH, 1)
            val end = cal.timeInMillis
            return start to end
        }

        fun getDayStart(timestamp: Long): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = timestamp
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }
    }
}
