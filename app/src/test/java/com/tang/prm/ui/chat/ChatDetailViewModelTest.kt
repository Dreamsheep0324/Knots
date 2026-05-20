package com.tang.prm.ui.chat

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.repository.FavoriteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
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
class ChatDetailViewModelTest {

    private lateinit var eventRepository: EventRepository
    private lateinit var favoriteRepository: FavoriteRepository
    private lateinit var viewModel: ChatDetailViewModel

    private val testContact = Contact(id = 1L, name = "Alice")
    private val testEvent = Event(
        id = 1L,
        title = "Chat with Alice",
        time = System.currentTimeMillis(),
        participants = listOf(testContact)
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        eventRepository = mockk()
        favoriteRepository = mockk()

        coEvery { eventRepository.getEventById(any()) } returns flowOf(testEvent)
        coEvery { favoriteRepository.isFavorite(any(), any()) } returns flowOf(false)
        coEvery { eventRepository.deleteEvent(any()) } returns Unit

        viewModel = ChatDetailViewModel(eventRepository, favoriteRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadEventUpdatesUiState() = runTest {
        viewModel.loadEvent(1L)
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.event).isEqualTo(testEvent)
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun deleteEventCallsRepository() = runTest {
        viewModel.loadEvent(1L)
        viewModel.uiState.test {
            awaitItem()
        }
        viewModel.deleteEvent()
        coVerify { eventRepository.deleteEvent(1L) }
    }
}
