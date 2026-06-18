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

    @MockK private lateinit var contactRepository: ContactRepository
    @MockK private lateinit var eventRepository: EventRepository
    @MockK private lateinit var anniversaryRepository: AnniversaryRepository
    @MockK private lateinit var todoRepository: TodoRepository
    @MockK private lateinit var reminderRepository: ReminderRepository

    private lateinit var useCase: HomeDataAggregationUseCase

    @BeforeEach
    fun setUp() {
        useCase = HomeDataAggregationUseCase(
            contactRepository, eventRepository, anniversaryRepository,
            todoRepository, reminderRepository
        )
    }

    private fun setupMocks(
        contacts: List<Contact> = emptyList(),
        events: List<Event> = emptyList(),
        upcomingAnniversaries: List<Anniversary> = emptyList(),
        allAnniversaries: List<Anniversary> = emptyList(),
        todos: List<TodoItem> = emptyList(),
        reminders: List<Reminder> = emptyList()
    ) {
        every { contactRepository.getRecentContacts(5) } returns flowOf(contacts)
        every { eventRepository.getAllEvents() } returns flowOf(events)
        every { anniversaryRepository.getUpcomingAnniversaries(10) } returns flowOf(upcomingAnniversaries)
        every { anniversaryRepository.getAllAnniversaries() } returns flowOf(allAnniversaries)
        every { todoRepository.getActiveTodos() } returns flowOf(todos)
        every { reminderRepository.getActiveReminders() } returns flowOf(reminders)
    }

    @Test
    fun aggregatesAllData() = runTest {
        val contacts = listOf(Contact(id = 1L, name = "Alice"))
        val events = listOf(Event(id = 1, title = "Meetup", type = EventType.MEETUP, time = 1000L))
        val anniversaries = listOf(
            Anniversary(id = 1, name = "Birthday", type = AnniversaryType.BIRTHDAY, date = 1000L, isRepeat = true)
        )
        val todos = listOf(TodoItem(id = 1L, title = "Task"))
        val reminders = listOf(Reminder(id = 1L, type = "event", title = "Remind", content = "", time = System.currentTimeMillis()))

        setupMocks(
            contacts = contacts, events = events,
            upcomingAnniversaries = anniversaries, allAnniversaries = anniversaries,
            todos = todos, reminders = reminders
        )

        useCase.getAggregateData().test {
            val data = awaitItem()
            assertThat(data.frequentContacts).hasSize(1)
            assertThat(data.allEvents).hasSize(1)
            assertThat(data.recentEvents).hasSize(1)
            assertThat(data.upcomingAnniversaries).hasSize(1)
            assertThat(data.allAnniversaries).hasSize(1)
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
                assertThat(data.allEvents).hasSize(8)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("todayReminders 筛选")
    inner class TodayRemindersTest {

        @Test
        fun filtersRemindersForToday() = runTest {
            val today = System.currentTimeMillis()
            val reminders = listOf(
                Reminder(id = 1L, type = "event", title = "Today", content = "", time = today),
                Reminder(id = 2L, type = "event", title = "Tomorrow", content = "", time = today + 86_400_000L)
            )
            setupMocks(reminders = reminders)

            useCase.getAggregateData().test {
                val data = awaitItem()
                assertThat(data.todayReminders).hasSize(1)
                assertThat(data.todayReminders[0].title).isEqualTo("Today")
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun emptyData_returnsEmptyLists() = runTest {
        setupMocks()

        useCase.getAggregateData().test {
            val data = awaitItem()
            assertThat(data.frequentContacts).isEmpty()
            assertThat(data.allEvents).isEmpty()
            assertThat(data.pendingTodos).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
