package com.tang.prm.ui.anniversary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.*
import com.tang.prm.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class AnniversariesUiState(
    val allAnniversaries: List<Anniversary> = emptyList(),
    val upcomingAnniversaries: List<Anniversary> = emptyList(),
    val pastAnniversaries: List<Anniversary> = emptyList(),
    val searchQuery: String = "",
    val selectedTab: Int = 0,
    val displayList: List<Anniversary> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class AnniversariesViewModel @Inject constructor(
    private val anniversaryRepository: AnniversaryRepository,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnniversariesUiState())
    val uiState: StateFlow<AnniversariesUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTab = MutableStateFlow(0)

    private val _allAnniversaries = MutableStateFlow<List<Anniversary>>(emptyList())

    companion object {
        private const val DEBOUNCE_MS = 300L
    }

    init {
        loadAnniversaries()
        observeDisplayList()
    }

    private fun loadAnniversaries() {
        viewModelScope.launch {
            anniversaryRepository.getAllAnniversaries().collect { allList ->
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val categorized = allList.map { anniversary ->
                    val effectiveDate = when {
                        anniversary.type == AnniversaryType.BIRTHDAY -> DateUtils.getNextBirthdayDate(anniversary.date)
                        anniversary.isRepeat -> DateUtils.getNextRepeatDate(anniversary.date)
                        else -> anniversary.date
                    }
                    anniversary to effectiveDate
                }

                val upcoming = categorized
                    .filter { it.second >= today }
                    .sortedBy { it.second }
                    .map { it.first }

                val past = categorized
                    .filter { it.second < today }
                    .sortedByDescending { it.second }
                    .map { it.first }

                _allAnniversaries.value = allList.sortedBy { ann ->
                    when {
                        ann.type == AnniversaryType.BIRTHDAY -> DateUtils.getNextBirthdayDate(ann.date)
                        ann.isRepeat -> DateUtils.getNextRepeatDate(ann.date)
                        else -> ann.date
                    }
                }

                _uiState.update { it.copy(
                    allAnniversaries = _allAnniversaries.value,
                    upcomingAnniversaries = upcoming,
                    pastAnniversaries = past,
                    isLoading = false
                ) }
            }
        }
    }

    private fun observeDisplayList() {
        viewModelScope.launch {
            combine(
                _selectedTab,
                _searchQuery
                    .debounce { query -> if (query.isBlank()) 0L else DEBOUNCE_MS },
                _allAnniversaries
            ) { tab, query, allList ->
                val source = when (tab) {
                    1 -> _uiState.value.upcomingAnniversaries
                    2 -> _uiState.value.pastAnniversaries
                    else -> allList
                }
                val filtered = if (query.isNotBlank()) {
                    source.filter { anniversary ->
                        anniversary.name.contains(query, ignoreCase = true) ||
                            anniversary.contactName?.contains(query, ignoreCase = true) == true
                    }
                } else {
                    source
                }
                filtered.sortedBy { anniversary ->
                    DateUtils.calculateDaysInfo(anniversary.date).daysUntil
                }
            }.collect { displayList ->
                _uiState.update { it.copy(displayList = displayList) }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onTabSelected(tab: Int) {
        _selectedTab.value = tab
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun deleteAnniversary(id: Long) {
        viewModelScope.launch {
            anniversaryRepository.deleteAnniversary(id)
        }
    }
}
