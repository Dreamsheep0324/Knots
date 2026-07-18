package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

/**
 * 首页数据聚合结果
 *
 * D-2~D-5 修复：删除 UI 从未读取的字段（frequentContacts/allAnniversaries/allEvents/todayReminders），
 * 同步移除对应 flow 订阅与计算，减少无谓重组与 filter 开销。
 */
data class HomeAggregateData(
    val recentEvents: List<Event>,
    val upcomingAnniversaries: List<Anniversary>,
    val pendingTodos: List<TodoItem>,
)

/**
 * 首页数据聚合 UseCase
 *
 * 将 HomeViewModel 中的多 Repository 数据聚合逻辑提取到此 UseCase，
 * 使 ViewModel 只关心 UI 状态组装，不关心数据来源。
 */
class HomeDataAggregationUseCase @Inject constructor(
    private val eventRepository: EventRepository,
    private val anniversaryRepository: AnniversaryRepository,
    private val todoRepository: TodoRepository
) {
    fun getAggregateData(): Flow<HomeAggregateData> {
        // D-5 修复：移除 frequentContacts（getRecentContacts）订阅，UI 从不读取
        // D-4 修复：allEvents 字段移除，但 eventsFlow 仍需订阅以派生 recentEvents
        // D-3 修复：移除 allAnniversariesFlow 订阅，upcomingAnniversaries 由独立 flow 计算
        // D-2 修复：移除 remindersFlow 订阅与 todayReminders filter 计算，UI 从不读取
        val eventsFlow = eventRepository.getAllEvents().distinctUntilChanged()
        val upcomingAnniversariesFlow = anniversaryRepository.getUpcomingAnniversaries(10).distinctUntilChanged()
        val todosFlow = todoRepository.getActiveTodos().distinctUntilChanged()

        return combine(
            eventsFlow,
            upcomingAnniversariesFlow,
            todosFlow
        ) { allEvents, upcomingAnniversaries, todos ->
            HomeAggregateData(
                recentEvents = allEvents.take(5),
                upcomingAnniversaries = upcomingAnniversaries,
                pendingTodos = todos
            )
        }
    }
}
