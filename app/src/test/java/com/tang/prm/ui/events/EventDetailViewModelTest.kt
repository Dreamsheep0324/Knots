package com.tang.prm.ui.events

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.repository.FavoriteRepository
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
class EventDetailViewModelTest {

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var favoriteRepository: FavoriteRepository

    private lateinit var viewModel: EventDetailViewModel

    private val testEvent = Event(id = 1, title = "Meetup", type = "MEETUP", time = 1000L)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { eventRepository.getEventById(1L) } returns flowOf(testEvent)
        every { favoriteRepository.isFavorite(SourceTypes.EVENT, 1L) } returns flowOf(false)

        viewModel = EventDetailViewModel(eventRepository, favoriteRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadEvent_loadsEventDetails() = runTest {
        viewModel.loadEvent(1L)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.event).isNotNull()
            assertThat(state.event!!.title).isEqualTo("Meetup")
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun loadEvent_zeroId_returnsEmptyState() = runTest {
        viewModel.loadEvent(0L)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.event).isNull()
        }
    }

    @Test
    fun loadEvent_isFavorite_reflectsState() = runTest {
        every { favoriteRepository.isFavorite(SourceTypes.EVENT, 1L) } returns flowOf(true)

        viewModel.loadEvent(1L)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isFavorite).isTrue()
        }
    }

    @Test
    fun deleteEvent_callsRepository() = runTest {
        coEvery { eventRepository.deleteEvent(1L) } returns Unit
        viewModel.uiState.test {
            viewModel.loadEvent(1L)
            var state = awaitItem()
            while (state.event == null) {
                state = awaitItem()
            }
            viewModel.deleteEvent()
        }
        coVerify { eventRepository.deleteEvent(1L) }
    }

    @Test
    fun toggleFavorite_callsRepository() = runTest {
        coEvery { favoriteRepository.toggleFavorite(any<String>(), any<Long>(), any<String>(), any()) } returns true
        viewModel.uiState.test {
            viewModel.loadEvent(1L)
            var state = awaitItem()
            while (state.event == null) {
                state = awaitItem()
            }
            viewModel.toggleFavorite()
        }
        coVerify { favoriteRepository.toggleFavorite(SourceTypes.EVENT, 1L, "Meetup", null) }
    }

    @Test
    fun updateRemarks_callsRepository() = runTest {
        coEvery { eventRepository.updateEvent(any<Event>()) } returns Unit
        viewModel.uiState.test {
            viewModel.loadEvent(1L)
            var state = awaitItem()
            while (state.event == null) {
                state = awaitItem()
            }
            viewModel.updateRemarks("New remarks")
            awaitItem()
        }
        coVerify { eventRepository.updateEvent(match { it.remarks == "New remarks" }) }
    }

    @Test
    fun consumeRemarkSaved_resetsFlag() = runTest {
        coEvery { eventRepository.updateEvent(any<Event>()) } returns Unit
        viewModel.uiState.test {
            viewModel.loadEvent(1L)
            var state = awaitItem()
            while (state.event == null) {
                state = awaitItem()
            }
            viewModel.updateRemarks("Test")
            state = awaitItem()
            assertThat(state.isRemarkSaved).isTrue()
            viewModel.consumeRemarkSaved()
            state = awaitItem()
            assertThat(state.isRemarkSaved).isFalse()
        }
    }
}
