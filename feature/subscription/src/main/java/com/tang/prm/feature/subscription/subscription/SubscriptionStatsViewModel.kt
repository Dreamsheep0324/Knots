package com.tang.prm.feature.subscription.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.usecase.SubscriptionStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SubscriptionStatsViewModel @Inject constructor(
    private val subscriptionStatsUseCase: SubscriptionStatsUseCase
) : ViewModel() {

    val uiState: StateFlow<SubscriptionStatsUiState> = subscriptionStatsUseCase.getStats()
        .map { stats ->
            val total = stats.yearlyTotal
            SubscriptionStatsUiState(
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
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubscriptionStatsUiState(isLoading = true))
}
