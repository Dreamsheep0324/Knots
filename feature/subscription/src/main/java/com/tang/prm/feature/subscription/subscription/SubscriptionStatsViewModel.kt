package com.tang.prm.feature.subscription.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.usecase.SubscriptionStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionStatsViewModel @Inject constructor(
    private val subscriptionStatsUseCase: SubscriptionStatsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SubscriptionStatsUiState(isLoading = true))
    val uiState: StateFlow<SubscriptionStatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            subscriptionStatsUseCase.getStats().collect { stats ->
                val total = stats.yearlyTotal
                _uiState.update { it.copy(
                    monthlyTotal = stats.monthlyTotal,
                    yearlyTotal = stats.yearlyTotal,
                    monthlyAverage = stats.yearlyTotal / 12,
                    dailyAverage = stats.yearlyTotal / 365,
                    activeCount = stats.activeCount,
                    expiringSoonCount = stats.expiringSoon.size,
                    byCategory = stats.byCategory,
                    byCategorySubscriptions = stats.byCategorySubscriptions,
                    categoryPercentages = stats.byCategory.mapValues { (_, v) ->
                        if (total > 0) (v / total * 100).toFloat() else 0f
                    },
                    yearlyProjection = stats.yearlyTotal,
                    activeSubscriptions = stats.activeSubscriptions,
                    isLoading = false
                )}
            }
        }
    }
}
