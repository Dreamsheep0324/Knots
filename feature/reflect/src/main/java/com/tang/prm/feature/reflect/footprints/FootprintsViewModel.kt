package com.tang.prm.feature.reflect.footprints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.FootprintItem
import com.tang.prm.domain.util.filterBy
import com.tang.prm.domain.usecase.FootprintAggregationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class FootprintsUiState(
    val footprints: List<FootprintItem> = emptyList(),
    val eventTypes: List<CustomType> = emptyList(),
    val isLoading: Boolean = true,
    val totalFootprintCount: Int = 0,
    val totalCityCount: Int = 0,
    val totalContactCount: Int = 0,
    val isTimelineView: Boolean = true,
    val selectedYear: Int? = null,
    val availableYears: List<Int> = emptyList(),
    val totalFootprintsByYear: Map<Int, Int> = emptyMap()
)

@HiltViewModel
class FootprintsViewModel @Inject constructor(
    private val footprintAggregationUseCase: FootprintAggregationUseCase
) : ViewModel() {

    // D-2 修复：删除 _selectedContactId / _filterEventType（无产线消费方）及对应的 filter 方法
    private val _selectedYear = MutableStateFlow<Int?>(null)
    private val _isTimelineView = MutableStateFlow(true)

    val uiState: StateFlow<FootprintsUiState> = combine(
        footprintAggregationUseCase(),
        _selectedYear,
        _isTimelineView
    ) { data, selectedYear, isTimelineView ->
        val totalFootprintCount = data.footprints.size
        val totalCityCount = data.footprints
            .mapNotNull { it.location.split("·").firstOrNull() }
            .distinct()
            .size
        val totalContactCount = data.footprints.mapNotNull { it.contactId }.distinct().size

        // 一次 groupBy 同时产出 availableYears 和 totalFootprintsByYear，避免遍历两次
        val byYear = data.footprints.groupBy {
            Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).year
        }
        val availableYears = byYear.keys.sortedDescending()
        val totalFootprintsByYear = byYear.mapValues { it.value.size }

        val filtered = data.footprints.filterBy(
            selectedYear = selectedYear
        )

        FootprintsUiState(
            footprints = filtered,
            eventTypes = data.eventTypes,
            isLoading = false,
            totalFootprintCount = totalFootprintCount,
            totalCityCount = totalCityCount,
            totalContactCount = totalContactCount,
            isTimelineView = isTimelineView,
            selectedYear = selectedYear,
            availableYears = availableYears,
            totalFootprintsByYear = totalFootprintsByYear
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FootprintsUiState())

    fun toggleView() {
        _isTimelineView.value = !_isTimelineView.value
    }

    fun selectYear(year: Int?) {
        _selectedYear.value = year
    }
}
