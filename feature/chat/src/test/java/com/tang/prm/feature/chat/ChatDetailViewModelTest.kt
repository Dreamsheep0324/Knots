package com.tang.prm.feature.chat

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.usecase.FavoriteToggleUseCase
import com.tang.prm.domain.usecase.ObserveFavoritesUseCase
import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ChatDetailViewModelTest {

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var favoriteToggleUseCase: FavoriteToggleUseCase

    @MockK
    private lateinit var observeFavoritesUseCase: ObserveFavoritesUseCase

    private val testContact = Contact(id = 1, name = "李四", avatar = "/avatar.png")
    private val testEvent = Event(
        id = 10L,
        type = EventType.CONVERSATION,
        title = "与李四的对话",
        description = "聊了工作",
        time = 5000L,
        createdAt = 5000L,
        participants = listOf(testContact)
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(eventId: Long): ChatDetailViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("eventId" to eventId))
        every { eventRepository.getEventById(eventId) } returns flowOf(testEvent.copy(id = eventId))
        every { observeFavoritesUseCase.isFavorite(SourceTypes.DIALOG, eventId) } returns flowOf(false)
        coEvery { favoriteToggleUseCase(any(), any(), any(), any()) } returns true
        return ChatDetailViewModel(savedStateHandle, eventRepository, favoriteToggleUseCase, observeFavoritesUseCase)
    }

    @Test
    fun `loads event detail from savedStateHandle eventId`() = runTest {
        val viewModel = createViewModel(10L)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.isLoading).isFalse()
            assertThat(state.data.event).isNotNull()
            assertThat(state.data.event?.id).isEqualTo(10L)
            assertThat(state.data.event?.title).isEqualTo("与李四的对话")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isFavorite reflects favorite state`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("eventId" to 10L))
        every { eventRepository.getEventById(10L) } returns flowOf(testEvent)
        every { observeFavoritesUseCase.isFavorite(SourceTypes.DIALOG, 10L) } returns flowOf(true)

        val viewModel = ChatDetailViewModel(savedStateHandle, eventRepository, favoriteToggleUseCase, observeFavoritesUseCase)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.isFavorite).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `setEventId updates loaded event`() = runTest {
        val savedStateHandle = SavedStateHandle(mapOf("eventId" to 10L))
        every { eventRepository.getEventById(10L) } returns flowOf(testEvent)
        every { eventRepository.getEventById(20L) } returns flowOf(testEvent.copy(id = 20L, title = "新对话"))
        every { observeFavoritesUseCase.isFavorite(SourceTypes.DIALOG, 10L) } returns flowOf(false)
        every { observeFavoritesUseCase.isFavorite(SourceTypes.DIALOG, 20L) } returns flowOf(false)

        val viewModel = ChatDetailViewModel(savedStateHandle, eventRepository, favoriteToggleUseCase, observeFavoritesUseCase)

        viewModel.uiState.test {
            // 初始事件
            assertThat(awaitItem().data.event?.id).isEqualTo(10L)
            // 切换到新事件
            viewModel.setEventId(20L)
            val newState = awaitItem()
            assertThat(newState.data.event?.id).isEqualTo(20L)
            assertThat(newState.data.event?.title).isEqualTo("新对话")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showDeleteConfirm sets dialog`() = runTest {
        val viewModel = createViewModel(10L)
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.showDeleteConfirm()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.dialog.showDeleteConfirm).isTrue()
        job.cancel()
    }

    @Test
    fun `hideDeleteConfirm clears dialog`() = runTest {
        val viewModel = createViewModel(10L)

        viewModel.showDeleteConfirm()
        viewModel.hideDeleteConfirm()

        assertThat(viewModel.uiState.value.dialog.showDeleteConfirm).isFalse()
    }

    @Test
    fun `toggleFavorite calls useCase`() = runTest {
        val viewModel = createViewModel(10L)
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.toggleFavorite()
        advanceUntilIdle()

        coVerify {
            favoriteToggleUseCase(
                type = SourceTypes.DIALOG,
                sourceId = 10L,
                title = match { it.contains("李四") },
                description = "聊了工作"
            )
        }
        job.cancel()
    }

    @Test
    fun `deleteEvent calls repository`() = runTest {
        coEvery { eventRepository.deleteEvent(any()) } returns Unit
        val viewModel = createViewModel(10L)
        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.deleteEvent()
        advanceUntilIdle()

        coVerify { eventRepository.deleteEvent(10L) }
        job.cancel()
    }
}
