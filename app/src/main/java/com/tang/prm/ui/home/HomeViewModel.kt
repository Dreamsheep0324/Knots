package com.tang.prm.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.ReminderRepository
import com.tang.prm.domain.repository.SettingsRepository
import com.tang.prm.domain.repository.TodoRepository
import com.tang.prm.domain.usecase.HomeAggregateData
import com.tang.prm.domain.usecase.HomeDataAggregationUseCase
import com.tang.prm.domain.usecase.HomeStats
import com.tang.prm.domain.usecase.HomeStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class HomeUiState(
    val greeting: String = "",
    val userName: String = "用户",
    val todayReminders: List<Reminder> = emptyList(),
    val upcomingAnniversaries: List<Anniversary> = emptyList(),
    val allAnniversaries: List<Anniversary> = emptyList(),
    val frequentContacts: List<Contact> = emptyList(),
    val recentEvents: List<Event> = emptyList(),
    val allEvents: List<Event> = emptyList(),
    val pendingTodos: List<TodoItem> = emptyList(),
    val giftCount: Int = 0,
    val contactCount: Int = 0,
    val photoCount: Int = 0,
    val footprintCount: Int = 0,
    val thoughtCount: Int = 0,
    val favoriteCount: Int = 0,
    val circleCount: Int = 0,
    val anniversaryCount: Int = 0,
    val eventCount: Int = 0,
    val conversationCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeDataUseCase: HomeDataAggregationUseCase,
    private val homeStatsUseCase: HomeStatsUseCase,
    private val settingsRepository: SettingsRepository,
    private val todoRepository: TodoRepository,
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val greeting: String = run {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "早上好"
            hour < 18 -> "下午好"
            else -> "晚上好"
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        flowOf(greeting),
        settingsRepository.userName.distinctUntilChanged(),
        homeDataUseCase.getAggregateData().distinctUntilChanged(),
        homeStatsUseCase.getStats().distinctUntilChanged()
    ) { greeting, userName, data: HomeAggregateData, stats: HomeStats ->
        HomeUiState(
            greeting = greeting,
            userName = userName,
            frequentContacts = data.frequentContacts,
            recentEvents = data.recentEvents,
            allEvents = data.allEvents,
            upcomingAnniversaries = data.upcomingAnniversaries,
            allAnniversaries = data.allAnniversaries,
            pendingTodos = data.pendingTodos,
            todayReminders = data.todayReminders,
            giftCount = stats.giftCount,
            contactCount = stats.contactCount,
            photoCount = stats.photoCount,
            footprintCount = stats.footprintCount,
            thoughtCount = stats.thoughtCount,
            favoriteCount = stats.favoriteCount,
            circleCount = stats.circleCount,
            anniversaryCount = stats.anniversaryCount,
            eventCount = stats.eventCount,
            conversationCount = stats.conversationCount,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState(isLoading = true))

    val currentTimeFlow: StateFlow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(30_000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), System.currentTimeMillis())

    fun toggleTodoCompletion(todoId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            todoRepository.updateTodoCompletion(todoId, isCompleted)
        }
    }

    fun completeReminder(id: Long) {
        viewModelScope.launch {
            reminderRepository.markReminderCompleted(id)
        }
    }
}
