package com.tang.prm.ui.chat

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.util.DateUtils
import io.mockk.coEvery
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
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
class ChatViewModelTest {

    private lateinit var eventRepository: EventRepository
    private lateinit var viewModel: ChatViewModel

    private val testContact = Contact(id = 1L, name = "Alice")
    private val testEvent = Event(
        id = 1L,
        type = EventType.CONVERSATION,
        title = "Chat with Alice",
        time = System.currentTimeMillis(),
        conversationSummary = "Hello",
        participants = listOf(testContact)
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockkObject(DateUtils)
        every { DateUtils.formatShortDate(any()) } returns "Jan 1"

        eventRepository = mockk()
        every { eventRepository.getConversationEvents() } returns flowOf(listOf(testEvent))

        viewModel = ChatViewModel(eventRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(DateUtils)
    }

    @Test
    fun initLoadsChats() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.conversations).hasSize(1)
            assertThat(state.conversations.first().contactName).isEqualTo("Alice")
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun initLoadsEmptyListWhenNoEvents() = runTest {
        every { eventRepository.getConversationEvents() } returns flowOf(emptyList())
        val emptyViewModel = ChatViewModel(eventRepository)
        emptyViewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.conversations).isEmpty()
        }
    }
}
