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
        every { eventRepository.getAllEvents() } returns flowOf(events)
        every { anniversaryRepository.getUpcomingAnniversaries(10) } returns flowOf(upcomingAnniversaries)
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
    @DisplayName("recentEvents 取前5条")
    inner class RecentEventsTest {

        @Test
        fun takesFirst5Events() = runTest {
            val events = (1..8).map {
                Event(id = it.toLong(), title = "E$it", type = EventType.OTHER, time = it.toLong())
            }
            setupMocks(events = events)

            useCase.getAggregateData().test {
                val data = awaitItem()
                assertThat(data.recentEvents).hasSize(5)
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
