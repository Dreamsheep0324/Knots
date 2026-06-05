package com.tang.prm.feature.encounter.events

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.domain.repository.EventRepository
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
class EventsViewModelTest {

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var contactRepository: ContactRepository

    @MockK
    private lateinit var customTypeRepository: CustomTypeRepository

    private lateinit var viewModel: EventsViewModel

    private val testEvents = listOf(
        Event(id = 1, title = "Meetup", type = EventType.MEETUP, time = 2000L),
        Event(id = 2, title = "Dinner", type = EventType.DINING, time = 1000L)
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { eventRepository.getAllEvents() } returns flowOf(testEvents)
        every { contactRepository.getAllContacts() } returns flowOf(emptyList<Contact>())
        every { customTypeRepository.getTypesByCategory(CustomCategories.EVENT_TYPE) } returns flowOf(emptyList<CustomType>())

        viewModel = EventsViewModel(eventRepository, contactRepository, customTypeRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsEvents() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.events).hasSize(2)
            assertThat(state.data.isLoading).isFalse()
        }
    }

    @Test
    fun init_sortsEventsByTimeDescending() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.events[0].title).isEqualTo("Meetup")
            assertThat(state.data.events[1].title).isEqualTo("Dinner")
        }
    }

    @Test
    fun selectType_updatesState() = runTest {
        every { eventRepository.getEventsByType("MEETUP") } returns flowOf(listOf(testEvents[0]))

        viewModel.selectType("MEETUP")

        assertThat(viewModel.uiState.value.data.selectedType).isEqualTo("MEETUP")
    }

    @Test
    fun selectType_null_showsAllEvents() = runTest {
        viewModel.selectType(null)

        assertThat(viewModel.uiState.value.data.selectedType).isNull()
    }

    @Test
    fun onSearchQueryChange_updatesState() = runTest {
        viewModel.onSearchQueryChange("Meet")

        assertThat(viewModel.uiState.value.data.searchQuery).isEqualTo("Meet")
    }

    @Test
    fun deleteEvent_callsRepository() = runTest {
        coEvery { eventRepository.deleteEvent(1L) } returns Unit

        viewModel.deleteEvent(1L)

        coVerify { eventRepository.deleteEvent(1L) }
    }

    @Test
    fun onViewModeChange_updatesState() = runTest {
        viewModel.onViewModeChange("grid")

        assertThat(viewModel.uiState.value.data.viewMode).isEqualTo("grid")
    }

    @Test
    fun clearError_resetsError() = runTest {
        viewModel.clearError()

        assertThat(viewModel.uiState.value.data.error).isNull()
    }
}
