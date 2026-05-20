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

    private val _selectedContactId = MutableStateFlow<Long?>(null)
    private val _filterEventType = MutableStateFlow<String?>(null)
    private val _selectedYear = MutableStateFlow<Int?>(null)
    private val _isTimelineView = MutableStateFlow(true)

    private val rawData = combine(
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
                    eventType = event.type.name,
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
    }

    val uiState: StateFlow<FootprintsUiState> = combine(
        rawData,
        _selectedContactId,
        _filterEventType,
        _selectedYear,
        _isTimelineView
    ) { (allFootprints, contacts, eventTypes), selectedContactId, filterEventType, selectedYear, isTimelineView ->
        val totalFootprintCount = allFootprints.size
        val totalCityCount = allFootprints
            .mapNotNull { it.location.split("·").firstOrNull() }
            .distinct()
            .size
        val totalContactCount = allFootprints.mapNotNull { it.contactId }.distinct().size
        val availableYears = allFootprints.map {
            java.util.Calendar.getInstance().apply { timeInMillis = it.date }.get(java.util.Calendar.YEAR)
        }.distinct().sortedDescending()

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

        FootprintsUiState(
            footprints = filtered,
            allContacts = contacts,
            eventTypes = eventTypes,
            selectedContactId = selectedContactId,
            filterEventType = filterEventType,
            isLoading = false,
            totalFootprintCount = totalFootprintCount,
            totalCityCount = totalCityCount,
            totalContactCount = totalContactCount,
            isTimelineView = isTimelineView,
            selectedYear = selectedYear,
            availableYears = availableYears
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FootprintsUiState())

    fun filterByContact(contactId: Long?) {
        _selectedContactId.value = contactId
    }

    fun filterByEventType(eventType: String?) {
        _filterEventType.value = eventType
    }

    fun clearFilters() {
        _selectedContactId.value = null
        _filterEventType.value = null
        _selectedYear.value = null
    }

    fun toggleView() {
        _isTimelineView.value = !_isTimelineView.value
    }

    fun selectYear(year: Int?) {
        _selectedYear.value = year
    }
}
