package com.tang.prm.feature.chat

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.usecase.ConversationItem
import com.tang.prm.domain.usecase.ObserveConversationsUseCase
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
class ChatViewModelTest {

    @MockK
    private lateinit var observeConversationsUseCase: ObserveConversationsUseCase

    private lateinit var viewModel: ChatViewModel

    private val conversation1 = ConversationItem(
        eventId = 1,
        contactId = 1L,
        contactName = "张三",
        avatar = "/avatar.jpg",
        title = "与张三的对话",
        lastMessage = "今天聊了天气",
        lastMessageTime = "1日"
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
        val older = conversation1.copy(eventId = 1)
        val newer = conversation1.copy(eventId = 2, lastMessage = "新对话")
        every { observeConversationsUseCase() } returns flowOf(listOf(newer, older))

        viewModel = ChatViewModel(observeConversationsUseCase)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.isLoading).isFalse()
            assertThat(state.data.conversations).hasSize(2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState maps event to conversation model`() = runTest {
        every { observeConversationsUseCase() } returns flowOf(listOf(conversation1))

        viewModel = ChatViewModel(observeConversationsUseCase)

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
    fun `empty events list shows no conversations`() = runTest {
        every { observeConversationsUseCase() } returns flowOf(emptyList())

        viewModel = ChatViewModel(observeConversationsUseCase)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.conversations).isEmpty()
            assertThat(state.data.isLoading).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `event without participants uses title as contactName`() = runTest {
        val noContact = conversation1.copy(contactId = null, contactName = "独立对话", title = "独立对话")
        every { observeConversationsUseCase() } returns flowOf(listOf(noContact))

        viewModel = ChatViewModel(observeConversationsUseCase)

        viewModel.uiState.test {
            val state = awaitItem()
            val conversation = state.data.conversations.first()
            assertThat(conversation.contactName).isEqualTo("独立对话")
            assertThat(conversation.contactId).isNull()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
