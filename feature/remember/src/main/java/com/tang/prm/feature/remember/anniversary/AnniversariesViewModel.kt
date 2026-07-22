package com.tang.prm.feature.remember.anniversary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.*
import com.tang.prm.domain.usecase.GetAnniversaryDisplayUseCase
import com.tang.prm.ui.common.SearchState
import com.tang.prm.ui.common.SearchStateManager
import com.tang.prm.domain.util.DateCalcUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnniversariesDataState(
    val allAnniversaries: List<Anniversary> = emptyList(),
    val upcomingAnniversaries: List<Anniversary> = emptyList(),
    val pastAnniversaries: List<Anniversary> = emptyList(),
    val selectedTab: Int = 0,
    val displayList: List<Anniversary> = emptyList(),
    val isLoading: Boolean = false
)

data class AnniversariesDialogState(
    val showDeleteConfirm: Boolean = false,
    val deleteTargetId: Long? = null
)

data class AnniversariesUiState(
    val data: AnniversariesDataState = AnniversariesDataState(),
    val dialog: AnniversariesDialogState = AnniversariesDialogState()
)

/** 分类结果，作为独立数据源参与 combine，避免读取 _uiState.value */
private data class CategorizedAnniversaries(
    val all: List<Anniversary> = emptyList(),
    val upcoming: List<Anniversary> = emptyList(),
    val past: List<Anniversary> = emptyList()
)

@HiltViewModel
class AnniversariesViewModel @Inject constructor(
    private val anniversaryRepository: AnniversaryRepository,
    private val getAnniversaryDisplayUseCase: GetAnniversaryDisplayUseCase
) : ViewModel() {

    private val searchManager = SearchStateManager()
    val searchState: StateFlow<SearchState> = searchManager.state

    private val _selectedTab = MutableStateFlow(0)
    private val _dialogState = MutableStateFlow(AnniversariesDialogState())

    companion object {
        private const val DEBOUNCE_MS = 300L
    }

    val uiState: StateFlow<AnniversariesUiState> = combine(
        anniversaryRepository.getAllAnniversaries()
            .map { allList ->
                getAnniversaryDisplayUseCase.categorizeAnniversaries(allList)
            },
        _selectedTab,
        searchManager.state
            .map { it.query }
            .debounce { query -> if (query.isBlank()) 0L else DEBOUNCE_MS },
        _dialogState
    ) { categorized, tab, query, dialog ->
        val source = when (tab) {
            1 -> categorized.upcoming
            2 -> categorized.past
            else -> categorized.all
        }
        val filtered = if (query.isNotBlank()) {
            source.filter { anniversary ->
                anniversary.name.contains(query, ignoreCase = true) ||
                    anniversary.contactName?.contains(query, ignoreCase = true) == true
            }
        } else {
            source
        }
        val displayList = filtered.sortedBy { anniversary ->
            DateCalcUtils.calculateDaysInfo(anniversary.date).daysUntil
        }
        AnniversariesUiState(
            data = AnniversariesDataState(
                allAnniversaries = categorized.all,
                upcomingAnniversaries = categorized.upcoming,
                pastAnniversaries = categorized.past,
                selectedTab = tab,
                displayList = displayList,
                isLoading = false
            ),
            dialog = dialog
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnniversariesUiState())

    fun onSearchQueryChange(query: String) = searchManager.onQueryChange(query)

    fun onTabSelected(tab: Int) {
        _selectedTab.value = tab
    }

    fun deleteAnniversary(id: Long) {
        viewModelScope.launch {
            anniversaryRepository.deleteAnniversary(id)
        }
    }
}
