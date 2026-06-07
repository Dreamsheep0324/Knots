package com.tang.prm.feature.events

import app.cash.turbine.test
import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.usecase.FavoriteToggleUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
class EventDetailViewModelTest {

    private lateinit var eventRepository: EventRepository
    private lateinit var favoriteToggleUseCase: FavoriteToggleUseCase
    private lateinit var viewModel: EventDetailViewModel

    private val testEvent = Event(id = 1, contactId = 1, title = "测试事件", type = EventType.MEETING)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        eventRepository = mockk()
        favoriteToggleUseCase = mockk()

        every { eventRepository.getEventById(1L) } returns flowOf(testEvent)
        every { favoriteToggleUseCase.isFavorite(SourceTypes.EVENT, 1L) } returns flowOf(false)
        coEvery { favoriteToggleUseCase(any(), any(), any(), any()) } returns Unit
        coEvery { eventRepository.updateEvent(any()) } returns Unit
        coEvery { eventRepository.deleteEvent(any()) } returns Unit

        val savedStateHandle = SavedStateHandle(mapOf("eventId" to 1L))
        viewModel = EventDetailViewModel(savedStateHandle, eventRepository, favoriteToggleUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState loads event and favorite status`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.event).isEqualTo(testEvent)
            assertThat(state.data.isLoading).isFalse()
            assertThat(state.data.isFavorite).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleFavorite calls useCase with event data`() = runTest {
        viewModel.toggleFavorite()
        coVerify { favoriteToggleUseCase(SourceTypes.EVENT, 1L, "测试事件", any()) }
    }

    @Test
    fun `updateRemarks saves and sets isRemarkSaved`() = runTest {
        viewModel.updateRemarks("新备注")
        coVerify { eventRepository.updateEvent(match { it.remarks == "新备注" }) }
        viewModel.uiState.test {
            assertThat(awaitItem().dialog.isRemarkSaved).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `consumeRemarkSaved resets flag`() = runTest {
        viewModel.updateRemarks("备注")
        viewModel.consumeRemarkSaved()
        viewModel.uiState.test {
            assertThat(awaitItem().dialog.isRemarkSaved).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteEvent calls repository`() = runTest {
        viewModel.deleteEvent()
        coVerify { eventRepository.deleteEvent(1L) }
    }

    @Test
    fun `invalid eventId shows loading state`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("eventId" to 0L))
        val vm = EventDetailViewModel(savedStateHandle, eventRepository, favoriteToggleUseCase)
        vm.uiState.test {
            val state = awaitItem()
            assertThat(state.data.event).isNull()
            assertThat(state.data.isLoading).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
