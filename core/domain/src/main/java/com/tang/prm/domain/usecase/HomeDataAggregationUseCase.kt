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
        // P-2 修复：改用 getRecentEvents(RECENT_EVENT_LIMIT) 让 SQL 层 LIMIT 5，
        // 避免全量加载 N 条事件再内存取前 5
        val eventsFlow = eventRepository.getRecentEvents(RECENT_EVENT_LIMIT).distinctUntilChanged()
        val upcomingAnniversariesFlow = anniversaryRepository.getUpcomingAnniversaries(UPCOMING_ANNIVERSARY_LIMIT).distinctUntilChanged()
        val todosFlow = todoRepository.getActiveTodos().distinctUntilChanged()

        return combine(
            eventsFlow,
            upcomingAnniversariesFlow,
            todosFlow
        ) { recentEvents, upcomingAnniversaries, todos ->
            HomeAggregateData(
                recentEvents = recentEvents,
                upcomingAnniversaries = upcomingAnniversaries,
                pendingTodos = todos
            )
        }
    }

    companion object {
        // Q-5 修复：首页 UI 容量约束提取为常量，避免魔法数字
        private const val RECENT_EVENT_LIMIT = 5
        private const val UPCOMING_ANNIVERSARY_LIMIT = 10
    }
}
