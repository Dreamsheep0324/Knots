package com.tang.prm.feature.subscription.subscription

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Subscription
import com.tang.prm.domain.model.SubscriptionStatus
import com.tang.prm.domain.model.computedStatus
import com.tang.prm.domain.repository.SubscriptionRepository
import com.tang.prm.domain.usecase.SubscriptionStatsUseCase
import com.tang.prm.domain.usecase.filterBy
import com.tang.prm.ui.common.SearchStateManager
import com.tang.prm.ui.common.SearchState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val subscriptionStatsUseCase: SubscriptionStatsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SubscriptionsUiState())
    val uiState: StateFlow<SubscriptionsUiState> = _uiState.asStateFlow()

    private val searchManager = SearchStateManager()
    val searchState: StateFlow<SearchState> = searchManager.state

    private val _selectedTab = MutableStateFlow(0)

    private fun launchWithErrorHandling(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }.onFailure { Log.e(TAG, "操作失败", it) }
        }
    }

    init {
        observeSubscriptions()
        observeStats()
    }

    private fun observeSubscriptions() {
        viewModelScope.launch {
            combine(
                subscriptionRepository.getAllSubscriptions(),
                _selectedTab,
                searchManager.state
            ) { subs, tab, search -> SubsSnapshot(subs, tab, search) }
            .collect { (subs, tab, search) ->
                val keyword = if (search.isActive && search.query.isNotBlank()) search.query else null
                val filtered = subs.filterBy(status = tabToStatus(tab), keyword = keyword)
                val grouped = filtered
                    .filter { it.computedStatus() != SubscriptionStatus.EXPIRED || tab == 3 }
                    .groupBy { it.category ?: "未分类" }
                _uiState.update { it.copy(data = it.data.copy(
                    allSubscriptions = subs,
                    displayList = filtered,
                    groupedByCategory = grouped,
                    selectedTab = tab
                ))}
            }
        }
    }

    /** combine 中间快照，纯数据，副作用在 collect 块中执行 */
    private data class SubsSnapshot(
        val subs: List<Subscription>,
        val tab: Int,
        val search: SearchState
    )

    private fun observeStats() {
        viewModelScope.launch {
            subscriptionStatsUseCase.getStats().collect { stats ->
                _uiState.update { it.copy(data = it.data.copy(stats = stats)) }
            }
        }
    }

    fun onTabSelected(tab: Int) { _selectedTab.value = tab }

    fun onSearchQueryChange(query: String) = searchManager.onQueryChange(query)
    fun onSearchActiveChange(active: Boolean) {
        if (active) searchManager.toggleSearch() else searchManager.deactivate()
    }

    fun deleteSubscription(id: Long) {
        launchWithErrorHandling { subscriptionRepository.deleteSubscription(id) }
    }

    private fun tabToStatus(tab: Int): SubscriptionStatus? = when (tab) {
        1 -> SubscriptionStatus.ACTIVE
        2 -> SubscriptionStatus.CANCELLED
        3 -> SubscriptionStatus.EXPIRED
        else -> null
    }

    private companion object {
        const val TAG = "SubscriptionsViewModel"
    }
}
