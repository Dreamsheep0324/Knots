package com.tang.prm.feature.subscription.subscription

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {
    private val subscriptionId: Long = savedStateHandle["subscriptionId"] ?: 0L
    private val _dialogState = MutableStateFlow(SubscriptionDetailDialogState())

    val uiState: StateFlow<SubscriptionDetailUiState> = if (subscriptionId == 0L) {
        combine(flowOf(SubscriptionDetailDataState()), _dialogState) { data, dialog ->
            SubscriptionDetailUiState(data = data, dialog = dialog)
        }
    } else {
        val dataState = subscriptionRepository.getSubscriptionById(subscriptionId).map { sub ->
            SubscriptionDetailDataState(subscription = sub, isLoading = false)
        }
        combine(dataState, _dialogState) { data, dialog ->
            SubscriptionDetailUiState(data = data, dialog = dialog)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubscriptionDetailUiState())

    fun deleteSubscription() {
        viewModelScope.launch {
            subscriptionRepository.deleteSubscription(subscriptionId)
        }
    }

    fun showDeleteConfirm() { _dialogState.update { it.copy(showDeleteConfirm = true) } }
    fun hideDeleteConfirm() { _dialogState.update { it.copy(showDeleteConfirm = false) } }
}
