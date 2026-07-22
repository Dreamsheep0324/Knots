package com.tang.prm.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.*
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class HomeDataAggregationUseCaseTest {

    @MockK private lateinit var eventRepository: EventRepository
    @MockK private lateinit var anniversaryRepository: AnniversaryRepository
    @MockK private lateinit var todoRepository: TodoRepository

    private lateinit var useCase: HomeDataAggregationUseCase

    @BeforeEach
    fun setUp() {
        useCase = HomeDataAggregationUseCase(
            eventRepository, anniversaryRepository, todoRepository
        )
    }

    private fun setupMocks(
        events: List<Event> = emptyList(),
        upcomingAnniversaries: List<Anniversary> = emptyList(),
        todos: List<TodoItem> = emptyList()
    ) {
        // P-2 修复后：UseCase 改用 getRecentEvents(5) 替代 getAllEvents()
        every { eventRepository.getRecentEvents(any()) } returns flowOf(events)
        every { anniversaryRepository.getUpcomingAnniversaries(any()) } returns flowOf(upcomingAnniversaries)
        every { todoRepository.getActiveTodos() } returns flowOf(todos)
    }

    @Test
    fun aggregatesAllData() = runTest {
        val events = listOf(Event(id = 1, title = "Meetup", type = EventType.MEETUP, time = 1000L))
        val anniversaries = listOf(
            Anniversary(id = 1, name = "Birthday", type = AnniversaryType.BIRTHDAY, date = 1000L, isRepeat = true)
        )
        val todos = listOf(TodoItem(id = 1L, title = "Task"))

        setupMocks(
            events = events,
            upcomingAnniversaries = anniversaries,
            todos = todos
        )

        useCase.getAggregateData().test {
            val data = awaitItem()
            assertThat(data.recentEvents).hasSize(1)
            assertThat(data.upcomingAnniversaries).hasSize(1)
            assertThat(data.pendingTodos).hasSize(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Nested
    @DisplayName("recentEvents 透传 getRecentEvents 结果")
    inner class RecentEventsTest {

        @Test
        fun passesThroughRecentEventsFromRepository() = runTest {
            // P-2 修复后：LIMIT 在 SQL 层完成，UseCase 直接透传 Repository 返回的事件列表
            val events = (1..5).map {
                Event(id = it.toLong(), title = "E$it", type = EventType.OTHER, time = it.toLong())
            }
            setupMocks(events = events)

            useCase.getAggregateData().test {
                val data = awaitItem()
                assertThat(data.recentEvents).hasSize(5)
                assertThat(data.recentEvents.map { it.id }).containsExactly(1L, 2L, 3L, 4L, 5L).inOrder()
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun emptyData_returnsEmptyLists() = runTest {
        setupMocks()

        useCase.getAggregateData().test {
            val data = awaitItem()
            assertThat(data.recentEvents).isEmpty()
            assertThat(data.pendingTodos).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
