package com.tang.prm.feature.home

import android.util.Log
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
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
    val subscriptionCount: Int = 0,
    val recipeCount: Int = 0,
    val tierDistribution: Map<IntimacyTier, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val decorPhotoPath: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeDataUseCase: HomeDataAggregationUseCase,
    private val homeStatsUseCase: HomeStatsUseCase,
    private val settingsRepository: SettingsRepository,
    private val todoRepository: TodoRepository,
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private fun launchWithErrorHandling(block: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { block() }.onFailure { Log.e(TAG, "操作失败", it) }
        }
    }

    /** 问候语随时间更新，每 30 分钟检查一次跨越早/中/晚分界 */
    private val greetingFlow: Flow<String> = flow {
        while (true) {
            emit(calculateGreeting())
            delay(30 * 60 * 1000L)
        }
    }

    private fun calculateGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "早上好"
            hour < 18 -> "下午好"
            else -> "晚上好"
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        greetingFlow,
        settingsRepository.userName.distinctUntilChanged(),
        homeDataUseCase.getAggregateData().distinctUntilChanged(),
        homeStatsUseCase.getStats().distinctUntilChanged(),
        settingsRepository.homeDecorPhotoPath.distinctUntilChanged()
    ) { greeting, userName, data: HomeAggregateData, stats: HomeStats, decorPhotoPath: String? ->
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
            subscriptionCount = stats.subscriptionCount,
            recipeCount = stats.recipeCount,
            tierDistribution = stats.tierDistribution,
            isLoading = false,
            decorPhotoPath = decorPhotoPath
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState(isLoading = true))

    fun toggleTodoCompletion(todoId: Long, isCompleted: Boolean) {
        launchWithErrorHandling { todoRepository.updateTodoCompletion(todoId, isCompleted) }
    }

    fun completeReminder(id: Long) {
        launchWithErrorHandling { reminderRepository.markReminderCompleted(id) }
    }

    fun setDecorPhotoPath(path: String?) {
        launchWithErrorHandling { settingsRepository.setHomeDecorPhotoPath(path) }
    }

    private companion object {
        const val TAG = "HomeViewModel"
    }
}
