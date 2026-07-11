package com.tang.prm.feature.chat

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.repository.EventRepository
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
class ChatViewModelTest {

    @MockK
    private lateinit var eventRepository: EventRepository

    private lateinit var viewModel: ChatViewModel

    private val testContact = Contact(id = 1, name = "张三", avatar = "/avatar.jpg")
    private val testEvent = Event(
        id = 1,
        type = EventType.CONVERSATION,
        title = "与张三的对话",
        description = "今天聊了天气",
        time = 1000L,
        conversationSummary = "今天聊了天气",
        createdAt = 1000L,
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

    @Test
    fun `uiState shows conversations sorted by createdAt descending`() = runTest {
        val olderEvent = testEvent.copy(id = 1, createdAt = 1000L)
        val newerEvent = testEvent.copy(id = 2, createdAt = 2000L, conversationSummary = "新对话")
        every { eventRepository.getEventsByType(EventType.CONVERSATION.name) } returns flowOf(listOf(olderEvent, newerEvent))

        viewModel = ChatViewModel(eventRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.isLoading).isFalse()
            assertThat(state.data.conversations).hasSize(2)
            // 新的在前
            assertThat(state.data.conversations[0].eventId).isEqualTo(2L)
            assertThat(state.data.conversations[1].eventId).isEqualTo(1L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState maps event to conversation model`() = runTest {
        every { eventRepository.getEventsByType(EventType.CONVERSATION.name) } returns flowOf(listOf(testEvent))

        viewModel = ChatViewModel(eventRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            val conversation = state.data.conversations.first()
            assertThat(conversation.eventId).isEqualTo(1L)
            assertThat(conversation.contactId).isEqualTo(1L)
            assertThat(conversation.contactName).isEqualTo("张三")
            assertThat(conversation.avatar).isEqualTo("/avatar.jpg")
            assertThat(conversation.lastMessage).isEqualTo("今天聊了天气")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `showDeleteConfirm updates dialog state`() = runTest {
        every { eventRepository.getEventsByType(EventType.CONVERSATION.name) } returns flowOf(emptyList())
        viewModel = ChatViewModel(eventRepository)

        val job = launch { viewModel.uiState.collect { } }
        advanceUntilIdle()

        viewModel.showDeleteConfirm(5L)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.dialog.showDeleteConfirm).isEqualTo(5L)
        job.cancel()
    }

    @Test
    fun `showDeleteConfirm with null clears dialog`() = runTest {
        every { eventRepository.getEventsByType(EventType.CONVERSATION.name) } returns flowOf(emptyList())
        viewModel = ChatViewModel(eventRepository)

        viewModel.showDeleteConfirm(5L)
        viewModel.showDeleteConfirm(null)

        assertThat(viewModel.uiState.value.dialog.showDeleteConfirm).isNull()
    }

    @Test
    fun `empty events list shows no conversations`() = runTest {
        every { eventRepository.getEventsByType(EventType.CONVERSATION.name) } returns flowOf(emptyList())

        viewModel = ChatViewModel(eventRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.conversations).isEmpty()
            assertThat(state.data.isLoading).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `event without participants uses title as contactName`() = runTest {
        val eventNoContact = testEvent.copy(participants = emptyList(), title = "独立对话")
        every { eventRepository.getEventsByType(EventType.CONVERSATION.name) } returns flowOf(listOf(eventNoContact))

        viewModel = ChatViewModel(eventRepository)

        viewModel.uiState.test {
            val state = awaitItem()
            val conversation = state.data.conversations.first()
            assertThat(conversation.contactName).isEqualTo("独立对话")
            assertThat(conversation.contactId).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
