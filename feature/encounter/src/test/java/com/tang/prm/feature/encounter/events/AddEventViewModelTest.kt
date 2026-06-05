package com.tang.prm.feature.encounter.events

import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.usecase.EventManageUseCase
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
class AddEventViewModelTest {

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var contactRepository: ContactRepository

    @MockK
    private lateinit var customTypeRepository: CustomTypeRepository

    @MockK
    private lateinit var eventManageUseCase: EventManageUseCase

    private lateinit var viewModel: AddEventViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { contactRepository.getAllContacts() } returns flowOf(emptyList<Contact>())
        every { customTypeRepository.getTypesByCategory(CustomCategories.EVENT_TYPE) } returns flowOf(emptyList<CustomType>())
        every { customTypeRepository.getTypesByCategory(CustomCategories.EMOTION) } returns flowOf(emptyList<CustomType>())
        every { customTypeRepository.getTypesByCategory(CustomCategories.WEATHER) } returns flowOf(emptyList<CustomType>())
        coEvery { customTypeRepository.getTypeCountByCategory(CustomCategories.WEATHER) } returns 1
        coEvery { customTypeRepository.getTypeCountByCategory(CustomCategories.EMOTION) } returns 1

        viewModel = AddEventViewModel(eventRepository, contactRepository, customTypeRepository, eventManageUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveEvent_callsInsertWhenTitleAndTypeProvided() = runTest {
        coEvery { eventManageUseCase.insertEventWithParticipants(any(), any()) } returns 1L

        viewModel.updateTitle("Meetup")
        viewModel.updateType("MEETUP")
        viewModel.saveEvent()

        coVerify { eventManageUseCase.insertEventWithParticipants(match { it.title == "Meetup" && it.type == EventType.MEETUP }, emptyList()) }
    }

    @Test
    fun saveEvent_doesNotCallInsertWhenTitleBlank() = runTest {
        coEvery { eventManageUseCase.insertEventWithParticipants(any(), any()) } returns 1L

        viewModel.updateTitle("")
        viewModel.updateType("MEETUP")
        viewModel.saveEvent()

        coVerify(exactly = 0) { eventManageUseCase.insertEventWithParticipants(any(), any()) }
    }

    @Test
    fun saveEvent_doesNotCallInsertWhenTypeBlank() = runTest {
        coEvery { eventManageUseCase.insertEventWithParticipants(any(), any()) } returns 1L

        viewModel.updateTitle("Meetup")
        viewModel.updateType("")
        viewModel.saveEvent()

        coVerify(exactly = 0) { eventManageUseCase.insertEventWithParticipants(any(), any()) }
    }

    @Test
    fun saveEvent_updatesIsSavedFlag() = runTest {
        coEvery { eventManageUseCase.insertEventWithParticipants(any(), any()) } returns 1L

        viewModel.updateTitle("Meetup")
        viewModel.updateType("MEETUP")
        viewModel.saveEvent()

        assertThat(viewModel.uiState.value.isSaved).isTrue()
    }

    @Test
    fun updateTitle_updatesState() = runTest {
        viewModel.updateTitle("Dinner")

        assertThat(viewModel.uiState.value.title).isEqualTo("Dinner")
        assertThat(viewModel.uiState.value.hasUnsavedChanges).isTrue()
    }

    @Test
    fun updateType_updatesState() = runTest {
        viewModel.updateType("DINING")

        assertThat(viewModel.uiState.value.type).isEqualTo("DINING")
        assertThat(viewModel.uiState.value.hasUnsavedChanges).isTrue()
    }

    @Test
    fun addParticipant_addsToState() = runTest {
        val contact = Contact(id = 1, name = "Alice")

        viewModel.addParticipant(contact)

        assertThat(viewModel.uiState.value.participants).hasSize(1)
        assertThat(viewModel.uiState.value.participants[0].name).isEqualTo("Alice")
    }

    @Test
    fun addParticipant_doesNotAddDuplicate() = runTest {
        val contact = Contact(id = 1, name = "Alice")

        viewModel.addParticipant(contact)
        viewModel.addParticipant(contact)

        assertThat(viewModel.uiState.value.participants).hasSize(1)
    }

    @Test
    fun removeParticipant_removesFromState() = runTest {
        val contact = Contact(id = 1, name = "Alice")

        viewModel.addParticipant(contact)
        viewModel.removeParticipant(contact)

        assertThat(viewModel.uiState.value.participants).isEmpty()
    }
}
