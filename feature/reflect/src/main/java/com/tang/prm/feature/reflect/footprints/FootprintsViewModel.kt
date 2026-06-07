package com.tang.prm.feature.reflect.footprints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.FootprintItem
import com.tang.prm.domain.usecase.filterBy
import com.tang.prm.domain.usecase.FootprintAggregationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

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
    private val footprintAggregationUseCase: FootprintAggregationUseCase
) : ViewModel() {

    private val _selectedContactId = MutableStateFlow<Long?>(null)
    private val _filterEventType = MutableStateFlow<String?>(null)
    private val _selectedYear = MutableStateFlow<Int?>(null)
    private val _isTimelineView = MutableStateFlow(true)

    val uiState: StateFlow<FootprintsUiState> = combine(
        footprintAggregationUseCase.getAggregateData(),
        _selectedContactId,
        _filterEventType,
        _selectedYear,
        _isTimelineView
    ) { data, selectedContactId, filterEventType, selectedYear, isTimelineView ->
        val totalFootprintCount = data.footprints.size
        val totalCityCount = data.footprints
            .mapNotNull { it.location.split("·").firstOrNull() }
            .distinct()
            .size
        val totalContactCount = data.footprints.mapNotNull { it.contactId }.distinct().size
        val availableYears = data.footprints.map {
            java.util.Calendar.getInstance().apply { timeInMillis = it.date }.get(java.util.Calendar.YEAR)
        }.distinct().sortedDescending()

        val filtered = data.footprints.filterBy(
            selectedContactId = selectedContactId,
            filterEventType = filterEventType,
            selectedYear = selectedYear
        )

        FootprintsUiState(
            footprints = filtered,
            allContacts = data.contacts,
            eventTypes = data.eventTypes,
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
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(10_000), FootprintsUiState())

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
