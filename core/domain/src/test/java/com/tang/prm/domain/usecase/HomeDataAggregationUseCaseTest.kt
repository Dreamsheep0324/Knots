package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.*
import com.tang.prm.domain.repository.*
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class HomeDataAggregationUseCaseTest {
    private lateinit var useCase: HomeDataAggregationUseCase
    private val contactRepository: ContactRepository = mockk(relaxed = true)
    private val eventRepository: EventRepository = mockk(relaxed = true)
    private val anniversaryRepository: AnniversaryRepository = mockk(relaxed = true)
    private val todoRepository: TodoRepository = mockk(relaxed = true)
    private val reminderRepository: ReminderRepository = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        useCase = HomeDataAggregationUseCase(
            contactRepository,
            eventRepository,
            anniversaryRepository,
            todoRepository,
            reminderRepository
        )
    }

    @Test
    fun `getAggregateData combines all sources`() = runTest {
        val contacts = listOf(Contact(id = 1, name = "Alice"))
        val events = listOf(Event(id = 1, title = "Meetup", time = 1000L))
        val upcomingAnniversaries = listOf(
            Anniversary(id = 1, name = "Birthday", type = AnniversaryType.BIRTHDAY, date = 2000L)
        )
        val allAnniversaries = listOf(
            Anniversary(id = 2, name = "Holiday", type = AnniversaryType.HOLIDAY, date = 3000L)
        )
        val todos = listOf(TodoItem(id = 1, title = "Buy gift"))
        val reminders = listOf(
            Reminder(id = 1, type = "anniversary", title = "Remind", content = "Don't forget", time = System.currentTimeMillis())
        )

        every { contactRepository.getRecentContacts(5) } returns flowOf(contacts)
        every { eventRepository.getAllEvents() } returns flowOf(events)
        every { anniversaryRepository.getUpcomingAnniversaries(10) } returns flowOf(upcomingAnniversaries)
        every { anniversaryRepository.getAllAnniversaries() } returns flowOf(allAnniversaries)
        every { todoRepository.getActiveTodos() } returns flowOf(todos)
        every { reminderRepository.getActiveReminders() } returns flowOf(reminders)

        val result = useCase.getAggregateData().first()

        assertThat(result.frequentContacts).isEqualTo(contacts)
        assertThat(result.recentEvents).isEqualTo(events.take(5))
        assertThat(result.allEvents).isEqualTo(events)
        assertThat(result.upcomingAnniversaries).isEqualTo(upcomingAnniversaries)
        assertThat(result.allAnniversaries).isEqualTo(allAnniversaries)
        assertThat(result.pendingTodos).isEqualTo(todos)
    }

    @Test
    fun `getAggregateData filters today reminders`() = runTest {
        val today = Calendar.getInstance()
        val todayTime = today.timeInMillis

        // Create a reminder for yesterday
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayReminder = Reminder(
            id = 1, type = "anniversary", title = "Yesterday",
            content = "Past", time = yesterdayCal.timeInMillis
        )

        // Create a reminder for today
        val todayReminder = Reminder(
            id = 2, type = "anniversary", title = "Today",
            content = "Current", time = todayTime
        )

        // Create a reminder for tomorrow
        val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowReminder = Reminder(
            id = 3, type = "anniversary", title = "Tomorrow",
            content = "Future", time = tomorrowCal.timeInMillis
        )

        val allReminders = listOf(yesterdayReminder, todayReminder, tomorrowReminder)

        every { contactRepository.getRecentContacts(5) } returns flowOf(emptyList())
        every { eventRepository.getAllEvents() } returns flowOf(emptyList())
        every { anniversaryRepository.getUpcomingAnniversaries(10) } returns flowOf(emptyList())
        every { anniversaryRepository.getAllAnniversaries() } returns flowOf(emptyList())
        every { todoRepository.getActiveTodos() } returns flowOf(emptyList())
        every { reminderRepository.getActiveReminders() } returns flowOf(allReminders)

        val result = useCase.getAggregateData().first()

        assertThat(result.todayReminders).hasSize(1)
        assertThat(result.todayReminders[0].id).isEqualTo(2L)
        assertThat(result.todayReminders[0].title).isEqualTo("Today")
    }
}
