package com.tang.prm.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.domain.repository.EventRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FootprintItem(
    val id: Long,
    val location: String,
    val date: Long,
    val eventType: String,
    val eventTitle: String,
    val contactId: Long?,
    val contactName: String?,
    val contactAvatar: String?,
    val description: String?,
    val weather: String?,
    val emotion: String?,
    val photoCount: Int
)

data class FootprintsUiState(
    val footprints: List<FootprintItem> = emptyList(),
    val allContacts: List<Contact> = emptyList(),
    val eventTypes: List<CustomType> = emptyList(),
    val selectedContactId: Long? = null,
    val filterEventType: String? = null,
    val isLoading: Boolean = false,
    val totalFootprintCount: Int = 0,
    val totalCityCount: Int = 0,
    val totalContactCount: Int = 0,
    val isTimelineView: Boolean = true,
    val selectedYear: Int? = null,
    val availableYears: List<Int> = emptyList()
)

@HiltViewModel
class FootprintsViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val contactRepository: ContactRepository,
    private val customTypeRepository: CustomTypeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FootprintsUiState())
    val uiState: StateFlow<FootprintsUiState> = _uiState.asStateFlow()

    private var selectedContactId: Long? = null
    private var filterEventType: String? = null
    private var selectedYear: Int? = null
    private var isTimelineView: Boolean = true
    private var allFootprints: List<FootprintItem> = emptyList()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                eventRepository.getEventsWithLocation(),
                contactRepository.getAllContacts(),
                customTypeRepository.getTypesByCategory(CustomCategories.EVENT_TYPE)
            ) { events, contacts, eventTypes ->
                val footprints = events
                    .filter { !it.location.isNullOrBlank() }
                    .map { event ->
                        val participant = event.participants.firstOrNull()
                        FootprintItem(
                            id = event.id,
                            location = event.location ?: "",
                            date = event.time,
                            eventType = event.type,
                            eventTitle = event.title,
                            contactId = participant?.id,
                            contactName = participant?.name,
                            contactAvatar = participant?.avatar,
                            description = event.description,
                            weather = event.weather,
                            emotion = event.emotion,
                            photoCount = event.photos.size
                        )
                    }
                    .sortedByDescending { it.date }

                Triple(footprints, contacts, eventTypes)
            }.catch { _ ->
                _uiState.update { it.copy(isLoading = false) }
            }.collect { (footprintsList, contacts, eventTypes) ->
                allFootprints = footprintsList
                val totalFootprintCount = footprintsList.size
                val totalCityCount = footprintsList
                    .mapNotNull { it.location.split("·").firstOrNull() }
                    .distinct()
                    .size
                val totalContactCount = footprintsList.mapNotNull { it.contactId }.distinct().size
                val availableYears = footprintsList.map {
                    java.util.Calendar.getInstance().apply { timeInMillis = it.date }.get(java.util.Calendar.YEAR)
                }.distinct().sortedDescending()
                applyFilter()
                _uiState.update {
                    it.copy(
                        allContacts = contacts,
                        eventTypes = eventTypes,
                        isLoading = false,
                        totalFootprintCount = totalFootprintCount,
                        totalCityCount = totalCityCount,
                        totalContactCount = totalContactCount,
                        availableYears = availableYears
                    )
                }
            }
        }
    }

    private fun applyFilter() {
        var filtered = allFootprints

        if (selectedContactId != null) {
            filtered = filtered.filter { it.contactId == selectedContactId }
        }

        if (filterEventType != null) {
            filtered = filtered.filter { it.eventType == filterEventType }
        }

        if (selectedYear != null) {
            filtered = filtered.filter {
                val calendar = java.util.Calendar.getInstance().apply { timeInMillis = it.date }
                calendar.get(java.util.Calendar.YEAR) == selectedYear
            }
        }

        _uiState.update {
            it.copy(
                footprints = filtered,
                selectedContactId = selectedContactId,
                filterEventType = filterEventType,
                selectedYear = selectedYear,
                isTimelineView = isTimelineView
            )
        }
    }

    fun filterByContact(contactId: Long?) {
        selectedContactId = contactId
        applyFilter()
    }

    fun filterByEventType(eventType: String?) {
        filterEventType = eventType
        applyFilter()
    }

    fun clearFilters() {
        selectedContactId = null
        filterEventType = null
        selectedYear = null
        applyFilter()
    }

    fun toggleView() {
        isTimelineView = !isTimelineView
        applyFilter()
    }

    fun selectYear(year: Int?) {
        selectedYear = year
        applyFilter()
    }
}
