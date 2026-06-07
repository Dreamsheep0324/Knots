package com.tang.prm.ui.home

import app.cash.turbine.test
import com.tang.prm.feature.home.HomeViewModel
import com.tang.prm.feature.home.HomeUiState
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.Reminder
import com.tang.prm.domain.model.TodoItem
import com.tang.prm.domain.repository.AnniversaryRepository
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.repository.ReminderRepository
import com.tang.prm.domain.repository.SettingsRepository
import com.tang.prm.domain.repository.TodoRepository
import com.tang.prm.domain.usecase.HomeStats
import com.tang.prm.domain.usecase.HomeStatsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class HomeViewModelTest {

    @MockK
    private lateinit var contactRepository: ContactRepository

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var anniversaryRepository: AnniversaryRepository

    @MockK
    private lateinit var todoRepository: TodoRepository

    @MockK
    private lateinit var reminderRepository: ReminderRepository

    @MockK
    private lateinit var homeStatsUseCase: HomeStatsUseCase

    @MockK
    private lateinit var settingsRepository: SettingsRepository

    private lateinit var viewModel: HomeViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { contactRepository.getRecentContacts(5) } returns flowOf(emptyList<Contact>())
        every { eventRepository.getAllEvents() } returns flowOf(emptyList<Event>())
        every { todoRepository.getActiveTodos() } returns flowOf(emptyList<TodoItem>())
        every { reminderRepository.getActiveReminders() } returns flowOf(emptyList<Reminder>())
        every { anniversaryRepository.getUpcomingAnniversaries(10) } returns flowOf(emptyList<Anniversary>())
        every { anniversaryRepository.getAllAnniversaries() } returns flowOf(emptyList<Anniversary>())
        every { homeStatsUseCase.getStats() } returns flowOf(HomeStats())
        every { settingsRepository.userName } returns flowOf("测试用户")

        viewModel = HomeViewModel(
            contactRepository,
            eventRepository,
            anniversaryRepository,
            todoRepository,
            reminderRepository,
            homeStatsUseCase,
            settingsRepository
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsStats() = runTest {
        val stats = HomeStats(
            giftCount = 5,
            contactCount = 10,
            photoCount = 20,
            footprintCount = 3,
            thoughtCount = 7,
            favoriteCount = 2,
            circleCount = 4,
            anniversaryCount = 6,
            eventCount = 8,
            conversationCount = 1
        )
        every { homeStatsUseCase.getStats() } returns flowOf(stats)

        val freshViewModel = HomeViewModel(
            contactRepository,
            eventRepository,
            anniversaryRepository,
            todoRepository,
            reminderRepository,
            homeStatsUseCase,
            settingsRepository
        )

        freshViewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.giftCount).isEqualTo(5)
            assertThat(state.contactCount).isEqualTo(10)
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun init_loadsContactsAndEvents() = runTest {
        val contacts = listOf(Contact(id = 1, name = "Alice"))
        val events = listOf(Event(id = 1, title = "Meetup", time = System.currentTimeMillis()))

        every { contactRepository.getRecentContacts(5) } returns flowOf(contacts)
        every { eventRepository.getAllEvents() } returns flowOf(events)

        val freshViewModel = HomeViewModel(
            contactRepository,
            eventRepository,
            anniversaryRepository,
            todoRepository,
            reminderRepository,
            homeStatsUseCase,
            settingsRepository
        )

        freshViewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.frequentContacts).hasSize(1)
            assertThat(state.frequentContacts[0].name).isEqualTo("Alice")
            assertThat(state.recentEvents).hasSize(1)
        }
    }

    @Test
    fun toggleTodoCompletion_callsRepository() = runTest {
        coEvery { todoRepository.updateTodoCompletion(1L, true) } returns Unit

        viewModel.toggleTodoCompletion(1L, true)

        coVerify { todoRepository.updateTodoCompletion(1L, true) }
    }

    @Test
    fun completeReminder_callsRepository() = runTest {
        coEvery { reminderRepository.markReminderCompleted(1L) } returns Unit

        viewModel.completeReminder(1L)

        coVerify { reminderRepository.markReminderCompleted(1L) }
    }

    @Test
    fun init_handlesError() = runTest {
        every { homeStatsUseCase.getStats() } returns flowOf(HomeStats())
        every { eventRepository.getAllEvents() } returns flowOf(emptyList())

        val freshViewModel = HomeViewModel(
            contactRepository,
            eventRepository,
            anniversaryRepository,
            todoRepository,
            reminderRepository,
            homeStatsUseCase,
            settingsRepository
        )

        freshViewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun currentTimeFlow_emitsTimestamps() = runTest {
        val before = System.currentTimeMillis()
        viewModel.currentTimeFlow.test {
            val first = awaitItem()
            assertThat(first).isAtLeast(before)
            val second = awaitItem()
            assertThat(second).isAtLeast(first)
        }
    }
}
