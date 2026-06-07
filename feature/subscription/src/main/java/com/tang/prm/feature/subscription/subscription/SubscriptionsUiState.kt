package com.tang.prm.feature.subscription.subscription

import com.tang.prm.domain.model.Subscription
import com.tang.prm.domain.usecase.SubscriptionStatsUseCase

data class SubscriptionsDataState(
    val allSubscriptions: List<Subscription> = emptyList(),
    val displayList: List<Subscription> = emptyList(),
    val groupedByCategory: Map<String, List<Subscription>> = emptyMap(),
    val selectedTab: Int = 0,
    val stats: SubscriptionStatsUseCase.SubscriptionStats? = null,
    val isLoading: Boolean = false
)

data class SubscriptionsDialogState(
    val showDeleteConfirm: Boolean = false,
    val deleteTargetId: Long? = null
)

data class SubscriptionsUiState(
    val data: SubscriptionsDataState = SubscriptionsDataState(),
    val dialog: SubscriptionsDialogState = SubscriptionsDialogState()
)
