package com.tang.prm.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.*
import com.tang.prm.domain.usecase.HomeStatsUseCase
import com.tang.prm.domain.usecase.HomeStats
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val contactRepository: ContactRepository,
    private val eventRepository: EventRepository,
    private val anniversaryRepository: AnniversaryRepository,
    private val todoRepository: TodoRepository,
    private val reminderRepository: ReminderRepository,
    private val homeStatsUseCase: HomeStatsUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val greeting: String = run {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "早上好"
            hour < 18 -> "下午好"
            else -> "晚上好"
        }
    }

    private data class CoreData(
        val contacts: List<Contact>,
        val allEvents: List<Event>,
        val todos: List<TodoItem>,
        val reminders: List<Reminder>
    )

    private data class AnniversaryData(
        val upcoming: List<Anniversary>,
        val all: List<Anniversary>
    )

    private val coreFlow = combine(
        contactRepository.getRecentContacts(5),
        eventRepository.getAllEvents(),
        todoRepository.getActiveTodos(),
        reminderRepository.getActiveReminders()
    ) { contacts, allEvents, todos, reminders ->
        CoreData(contacts, allEvents, todos, reminders)
    }

    private val anniversaryFlow = combine(
        anniversaryRepository.getUpcomingAnniversaries(10),
        anniversaryRepository.getAllAnniversaries()
    ) { upcoming, all ->
        AnniversaryData(upcoming, all)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        flowOf(greeting),
        settingsRepository.userName,
        coreFlow,
        anniversaryFlow,
        homeStatsUseCase.getStats()
    ) { greeting, userName, core, anniversaries, stats ->
        val today = Calendar.getInstance()
        val todayReminders = core.reminders.filter {
            val reminderCal = Calendar.getInstance().apply { timeInMillis = it.time }
            reminderCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    reminderCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        }

        HomeUiState(
            greeting = greeting,
            userName = userName,
            frequentContacts = core.contacts,
            recentEvents = core.allEvents.take(5),
            allEvents = core.allEvents,
            upcomingAnniversaries = anniversaries.upcoming,
            allAnniversaries = anniversaries.all,
            pendingTodos = core.todos,
            todayReminders = todayReminders,
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
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeUiState(isLoading = false))

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
