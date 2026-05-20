package com.tang.prm.ui.chat

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.EventRepository
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
import androidx.lifecycle.SavedStateHandle
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AddChatViewModelTest {

    private lateinit var eventRepository: EventRepository
    private lateinit var contactRepository: ContactRepository
    private lateinit var viewModel: AddChatViewModel

    private val testContact = Contact(id = 1L, name = "Alice")

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        eventRepository = mockk()
        contactRepository = mockk()

        coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(testContact))
        coEvery { eventRepository.insertEventWithParticipants(any(), any()) } returns 1L
        coEvery { contactRepository.updateContactInteraction(any(), any(), any()) } returns Unit

        val savedStateHandle = SavedStateHandle(mapOf("contactId" to 0L, "eventId" to 0L))
        viewModel = AddChatViewModel(eventRepository, contactRepository, savedStateHandle)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveChatCallsInsert() = runTest {
        viewModel.selectContact(testContact)
        viewModel.addDialogueLine(isMe = true)
        viewModel.updateDialogueLine(
            lineId = viewModel.uiState.value.dialogueLines.first().id,
            content = "Hello"
        )
        viewModel.saveChat()
        coVerify { eventRepository.insertEventWithParticipants(any(), listOf(1L)) }
    }

    @Test
    fun saveChatWithoutContactDoesNotCallInsert() = runTest {
        viewModel.saveChat()
        coVerify(exactly = 0) { eventRepository.insertEventWithParticipants(any(), any()) }
    }

    @Test
    fun selectContactUpdatesState() = runTest {
        viewModel.selectContact(testContact)
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.selectedContact).isEqualTo(testContact)
        }
    }
}
