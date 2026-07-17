package com.tang.prm.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.*
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.domain.usecase.ObserveEventsAggregateUseCase
import com.tang.prm.domain.usecase.filterBy
import com.tang.prm.ui.common.SearchState
import com.tang.prm.ui.common.SearchStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.FlowPreview
import java.util.Calendar
import javax.inject.Inject

data class CalendarStats(
    val eventCount: Int = 0,
    val participantCount: Int = 0
)

data class EventsDataState(
    val events: List<Event> = emptyList(),
    val displayEvents: List<Event> = emptyList(),
    val eventTypes: List<CustomType> = emptyList(),
    val selectedType: String? = null,
    val availableContacts: List<Contact> = emptyList(),
    val isLoading: Boolean = true,
    val viewMode: String = "list",
    val calendarMonthOffset: Int = 0,
    val selectedCalendarDate: Long = System.currentTimeMillis(),
    val calendarEvents: List<Event> = emptyList(),
    val selectedDateEvents: List<Event> = emptyList(),
    val calendarStats: CalendarStats = CalendarStats()
)

class EventsDialogState

private data class UiSelections(
    val selectedType: String?,
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

@OptIn(FlowPreview::class)
@HiltViewModel
class EventsViewModel @Inject constructor(
    private val observeEventsAggregateUseCase: ObserveEventsAggregateUseCase
) : ViewModel() {

    private val _selectedType = MutableStateFlow<String?>(null)
    private val searchManager = SearchStateManager()
    val searchState: StateFlow<SearchState> = searchManager.state

    private val _dialogState = MutableStateFlow(EventsDialogState())
    private val _viewMode = MutableStateFlow("list")
    private val _calendarMonthOffset = MutableStateFlow(0)
    private val _selectedCalendarDate = MutableStateFlow(System.currentTimeMillis())

    private val filterState = combine(
        _selectedType,
        searchManager.state
            .map { it.query }
            .debounce { query -> if (query.isBlank()) 0L else DEBOUNCE_MS }
    ) { type: String?, query: String ->
        type to query
    }

    private val dataFlow = combine(
        observeEventsAggregateUseCase.invoke(),
        filterState
    ) { aggregate, (type, query) ->
        Triple(aggregate.contacts, aggregate.eventTypes, aggregate.events.filterBy(null, type, query))
    }

    private val uiSelections = combine(
        _selectedType, _viewMode, _dialogState
    ) { type, mode, dialog ->
        UiSelections(type, mode, dialog)
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
        val selectedDateEnd = selectedDateStart + DateUtils.MILLIS_PER_DAY
        val selectedDateEvents = filteredEvents.filter { event ->
            event.time >= selectedDateStart && event.time < selectedDateEnd
        }.sortedBy { it.time }

        val stats = CalendarStats(
            eventCount = calendarEvents.size,
            participantCount = calendarEvents.flatMap { it.participants }.distinctBy { it.id }.size
        )

        EventsUiState(
            data = EventsDataState(
                events = filteredEvents,
                displayEvents = filteredEvents,
                eventTypes = eventTypes,
                selectedType = ui.selectedType,
                availableContacts = contacts,
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventsUiState())

    fun selectType(type: String?) {
        _selectedType.value = type
    }

    fun onSearchQueryChange(query: String) = searchManager.onQueryChange(query)

    fun onViewModeChange(mode: String) {
        _viewMode.value = mode
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
