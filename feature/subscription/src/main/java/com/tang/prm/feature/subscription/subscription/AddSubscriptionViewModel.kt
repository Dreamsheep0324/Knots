package com.tang.prm.feature.subscription.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Subscription
import com.tang.prm.domain.model.SubscriptionCycle
import com.tang.prm.domain.model.SubscriptionStatus
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.domain.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddSubscriptionUiState(
    val name: String = "",
    val category: String? = null,
    val categoryOptions: List<CustomType> = emptyList(),
    val price: String = "",
    val currency: String = "CNY",
    val cycle: SubscriptionCycle = SubscriptionCycle.MONTHLY,
    val startDate: Long = System.currentTimeMillis(),
    val nextBillingDate: Long = 0L,
    val notes: String = "",
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val isSaved: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val isEdit: Boolean = false,
    val subscriptionId: Long = 0,
    val timezone: String = java.util.TimeZone.getDefault().id
)

@HiltViewModel
class AddSubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val customTypeRepository: CustomTypeRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddSubscriptionUiState(
        nextBillingDate = computeNextBillingDate(System.currentTimeMillis(), SubscriptionCycle.MONTHLY, java.util.TimeZone.getDefault().id)
    ))
    val uiState: StateFlow<AddSubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadCategoryOptions()
    }

    private fun loadCategoryOptions() {
        viewModelScope.launch {
            val types = customTypeRepository.getTypesByCategory(CustomCategories.SUBSCRIPTION_CATEGORY).first()
            _uiState.update { it.copy(categoryOptions = types) }
        }
    }

    fun initForEdit(subscriptionId: Long) {
        if (subscriptionId <= 0) return
        viewModelScope.launch {
            subscriptionRepository.getSubscriptionById(subscriptionId)?.let { sub ->
                _uiState.update { it.copy(
                    name = sub.name,
                    category = sub.category,
                    price = sub.price.toString(),
                    currency = sub.currency,
                    cycle = sub.cycle,
                    startDate = sub.startDate,
                    nextBillingDate = sub.nextBillingDate,
                    notes = sub.notes ?: "",
                    status = sub.status,
                    isEdit = true,
                    subscriptionId = subscriptionId,
                    timezone = sub.timezone
                )}
            }
        }
    }

    fun saveSubscription() {
        viewModelScope.launch {
            val state = _uiState.value
            val nextBillingDate = if (state.isEdit) state.nextBillingDate
                else computeNextBillingDate(state.startDate, state.cycle, state.timezone)
            val subscription = Subscription(
                id = if (state.isEdit) state.subscriptionId else 0,
                name = state.name,
                category = state.category,
                price = state.price.toDoubleOrNull() ?: 0.0,
                currency = state.currency,
                cycle = state.cycle,
                startDate = state.startDate,
                nextBillingDate = nextBillingDate,
                status = if (state.isEdit) state.status else SubscriptionStatus.ACTIVE,
                notes = state.notes.ifBlank { null },
                timezone = state.timezone
            )
            if (state.isEdit) {
                subscriptionRepository.updateSubscription(subscription)
            } else {
                subscriptionRepository.insertSubscription(subscription)
            }
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun updateName(name: String) { _uiState.update { it.copy(name = name, hasUnsavedChanges = true) } }
    fun updateCategory(category: String?) { _uiState.update { it.copy(category = category, hasUnsavedChanges = true) } }
    fun updatePrice(price: String) { _uiState.update { it.copy(price = price, hasUnsavedChanges = true) } }
    fun updateCurrency(currency: String) { _uiState.update { it.copy(currency = currency, hasUnsavedChanges = true) } }
    fun updateCycle(cycle: SubscriptionCycle) {
        _uiState.update {
            it.copy(
                cycle = cycle,
                nextBillingDate = computeNextBillingDate(it.startDate, cycle, it.timezone),
                hasUnsavedChanges = true
            )
        }
    }
    fun updateStartDate(date: Long) {
        _uiState.update {
            it.copy(
                startDate = date,
                nextBillingDate = computeNextBillingDate(date, it.cycle, it.timezone),
                hasUnsavedChanges = true
            )
        }
    }
    fun updateNotes(notes: String) { _uiState.update { it.copy(notes = notes, hasUnsavedChanges = true) } }

    fun addCustomType(name: String, color: String?, icon: String?) {
        viewModelScope.launch {
            val sortOrder = _uiState.value.categoryOptions.size
            customTypeRepository.insertType(CustomType(
                category = CustomCategories.SUBSCRIPTION_CATEGORY,
                name = name,
                color = color,
                icon = icon,
                sortOrder = sortOrder
            ))
        }
    }

    fun deleteCustomType(type: CustomType) {
        viewModelScope.launch {
            customTypeRepository.deleteTypeById(type.id)
            if (_uiState.value.category == type.name) {
                _uiState.update { it.copy(category = null) }
            }
        }
    }

    private fun computeNextBillingDate(startDate: Long, cycle: SubscriptionCycle, timezone: String): Long {
        if (cycle == SubscriptionCycle.ONE_TIME) return startDate
        val zoneId = try { java.time.ZoneId.of(timezone) } catch (_: Exception) { java.time.ZoneId.systemDefault() }
        val startInstant = java.time.Instant.ofEpochMilli(startDate)
        val startZoned = startInstant.atZone(zoneId)

        val nextZoned = when (cycle) {
            SubscriptionCycle.WEEKLY -> startZoned.plusWeeks(1)
            SubscriptionCycle.MONTHLY -> startZoned.plusMonths(1)
            SubscriptionCycle.QUARTERLY -> startZoned.plusMonths(3)
            SubscriptionCycle.YEARLY -> startZoned.plusYears(1)
            SubscriptionCycle.ONE_TIME -> startZoned
        }
        return nextZoned.toInstant().toEpochMilli()
    }
}
