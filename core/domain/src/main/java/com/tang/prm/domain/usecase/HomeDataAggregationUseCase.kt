package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.*
import javax.inject.Inject

/**
 * 首页数据聚合结果
 */
data class HomeAggregateData(
    val frequentContacts: List<Contact>,
    val recentEvents: List<Event>,
    val allEvents: List<Event>,
    val upcomingAnniversaries: List<Anniversary>,
    val allAnniversaries: List<Anniversary>,
    val pendingTodos: List<TodoItem>,
    val todayReminders: List<Reminder>,
)

/**
 * 首页数据聚合 UseCase
 *
 * 将 HomeViewModel 中的多 Repository 数据聚合逻辑提取到此 UseCase，
 * 使 ViewModel 只关心 UI 状态组装，不关心数据来源。
 */
class HomeDataAggregationUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val eventRepository: EventRepository,
    private val anniversaryRepository: AnniversaryRepository,
    private val todoRepository: TodoRepository,
    private val reminderRepository: ReminderRepository
) {
    fun getAggregateData(): Flow<HomeAggregateData> = combine(
        contactRepository.getRecentContacts(5).distinctUntilChanged(),
        eventRepository.getAllEvents().distinctUntilChanged(),
        anniversaryRepository.getUpcomingAnniversaries(10).distinctUntilChanged(),
        anniversaryRepository.getAllAnniversaries().distinctUntilChanged(),
        todoRepository.getActiveTodos().distinctUntilChanged(),
        reminderRepository.getActiveReminders().distinctUntilChanged()
    ) { args: Array<Any> ->
        @Suppress("UNCHECKED_CAST")
        val contacts = args[0] as List<Contact>
        val allEvents = args[1] as List<Event>
        val upcomingAnniversaries = args[2] as List<Anniversary>
        val allAnniversaries = args[3] as List<Anniversary>
        val todos = args[4] as List<TodoItem>
        val reminders = args[5] as List<Reminder>

        val today = Calendar.getInstance()
        val todayReminders = reminders.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.time }
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        }
        HomeAggregateData(
            frequentContacts = contacts,
            recentEvents = allEvents.take(5),
            allEvents = allEvents,
            upcomingAnniversaries = upcomingAnniversaries,
            allAnniversaries = allAnniversaries,
            pendingTodos = todos,
            todayReminders = todayReminders
        )
    }
}
