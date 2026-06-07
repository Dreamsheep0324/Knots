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
    fun getAggregateData(): Flow<HomeAggregateData> {
        val contactsFlow = contactRepository.getRecentContacts(5).distinctUntilChanged()
        val eventsFlow = eventRepository.getAllEvents().distinctUntilChanged()
        val upcomingAnniversariesFlow = anniversaryRepository.getUpcomingAnniversaries(10).distinctUntilChanged()
        val allAnniversariesFlow = anniversaryRepository.getAllAnniversaries().distinctUntilChanged()
        val todosFlow = todoRepository.getActiveTodos().distinctUntilChanged()
        val remindersFlow = reminderRepository.getActiveReminders().distinctUntilChanged()

        return combine(
            combine(contactsFlow, eventsFlow, upcomingAnniversariesFlow) { contacts, events, upcoming ->
                Triple(contacts, events, upcoming)
            },
            combine(allAnniversariesFlow, todosFlow, remindersFlow) { allAnn, todos, reminders ->
                Triple(allAnn, todos, reminders)
            }
        ) { (contacts, allEvents, upcomingAnniversaries), (allAnniversaries, todos, reminders) ->
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
}
