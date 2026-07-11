package com.tang.prm.feature.chat

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.usecase.EventManageUseCase
import com.tang.prm.domain.usecase.UpdateInteractionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
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
class AddChatViewModelTest {

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var contactRepository: ContactRepository

    @MockK
    private lateinit var eventManageUseCase: EventManageUseCase

    @MockK
    private lateinit var updateInteractionUseCase: UpdateInteractionUseCase

    private val dialogueLineManager = DialogueLineManager()

    private val testContact = Contact(id = 1, name = "王五", intimacyScore = 50)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { contactRepository.getAllContacts() } returns flowOf(listOf(testContact))
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        contactId: Long? = null,
        eventId: Long? = null
    ): AddChatViewModel {
        val savedStateHandle = SavedStateHandle(
            buildMap {
                contactId?.let { put("contactId", it) }
                eventId?.let { put("eventId", it) }
            }
        )
        return AddChatViewModel(
            eventRepository, contactRepository, dialogueLineManager,
            eventManageUseCase, updateInteractionUseCase, savedStateHandle
        )
    }

    @Test
    fun `initial state has default values`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.title).isEmpty()
            assertThat(state.dialogueLines).isEmpty()
            assertThat(state.remarks).isNull()
            assertThat(state.isSaved).isFalse()
            assertThat(state.showContactPicker).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads contacts list on init`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.contacts).hasSize(1)
        assertThat(viewModel.uiState.value.contacts[0].name).isEqualTo("王五")
    }

    @Test
    fun `selectContact updates selectedContact and closes picker`() = runTest {
        val viewModel = createViewModel()

        viewModel.showContactPicker()
        assertThat(viewModel.uiState.value.showContactPicker).isTrue()

        viewModel.selectContact(testContact)

        assertThat(viewModel.uiState.value.selectedContact).isEqualTo(testContact)
        assertThat(viewModel.uiState.value.showContactPicker).isFalse()
        assertThat(viewModel.uiState.value.hasUnsavedChanges).isTrue()
    }

    @Test
    fun `updateTitle sets hasUnsavedChanges`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateTitle("新对话标题")

        assertThat(viewModel.uiState.value.title).isEqualTo("新对话标题")
        assertThat(viewModel.uiState.value.hasUnsavedChanges).isTrue()
    }

    @Test
    fun `updateRemarks with blank sets null`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateRemarks("   ")

        assertThat(viewModel.uiState.value.remarks).isNull()
    }

    @Test
    fun `updateRemarks with content sets value`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateRemarks("备注内容")

        assertThat(viewModel.uiState.value.remarks).isEqualTo("备注内容")
    }

    @Test
    fun `addDialogueLine adds line with correct speaker`() = runTest {
        val viewModel = createViewModel()

        viewModel.addDialogueLine(isMe = true)
        viewModel.addDialogueLine(isMe = false)

        val lines = viewModel.uiState.value.dialogueLines
        assertThat(lines).hasSize(2)
        assertThat(lines[0].isMe).isTrue()
        assertThat(lines[1].isMe).isFalse()
    }

    @Test
    fun `removeDialogueLine removes by id`() = runTest {
        val viewModel = createViewModel()

        viewModel.addDialogueLine(isMe = true)
        viewModel.addDialogueLine(isMe = false)
        val firstLineId = viewModel.uiState.value.dialogueLines[0].id

        viewModel.removeDialogueLine(firstLineId)

        assertThat(viewModel.uiState.value.dialogueLines).hasSize(1)
    }

    @Test
    fun `saveChat inserts new event and marks saved`() = runTest {
        coEvery { eventManageUseCase.insertEventWithParticipants(any(), any()) } returns 1L
        coEvery { updateInteractionUseCase(any(), any(), any()) } returns Unit

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectContact(testContact)
        viewModel.updateTitle("标题")
        viewModel.addDialogueLine(isMe = true)
        viewModel.updateDialogueLine(
            viewModel.uiState.value.dialogueLines[0].id,
            "内容"
        )
        viewModel.saveChat()
        advanceUntilIdle()

        coVerify {
            eventManageUseCase.insertEventWithParticipants(
                match { it.type == EventType.CONVERSATION && it.title == "标题" },
                listOf(1L)
            )
        }
        coVerify { updateInteractionUseCase(1L, 50, any()) }
        assertThat(viewModel.uiState.value.isSaved).isTrue()
    }

    @Test
    fun `saveChat without contact does nothing`() = runTest {
        coEvery { eventManageUseCase.insertEventWithParticipants(any(), any()) } returns 1L

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.saveChat()
        advanceUntilIdle()

        coVerify(exactly = 0) {
            eventManageUseCase.insertEventWithParticipants(any(), any())
        }
        assertThat(viewModel.uiState.value.isSaved).isFalse()
    }
}
