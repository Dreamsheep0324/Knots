package com.tang.prm.ui.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import com.tang.prm.domain.repository.EventRepository
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
class FootprintsViewModelTest {

    @MockK
    private lateinit var eventRepository: EventRepository

    @MockK
    private lateinit var contactRepository: ContactRepository

    @MockK
    private lateinit var customTypeRepository: CustomTypeRepository

    private lateinit var viewModel: FootprintsViewModel

    private val testEvent = Event(
        id = 1,
        title = "Trip",
        type = "TRAVEL",
        time = 1000L,
        location = "Beijing",
        participants = listOf(Contact(id = 1, name = "Alice"))
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { eventRepository.getEventsWithLocation() } returns flowOf(listOf(testEvent))
        every { contactRepository.getAllContacts() } returns flowOf(emptyList<Contact>())
        every { customTypeRepository.getTypesByCategory(CustomCategories.EVENT_TYPE) } returns flowOf(emptyList<CustomType>())

        viewModel = FootprintsViewModel(eventRepository, contactRepository, customTypeRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsEvents() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.footprints).hasSize(1)
            assertThat(state.footprints[0].location).isEqualTo("Beijing")
            assertThat(state.footprints[0].eventTitle).isEqualTo("Trip")
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun init_computesStats() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.totalFootprintCount).isEqualTo(1)
            assertThat(state.totalContactCount).isEqualTo(1)
        }
    }

    @Test
    fun filterByContact_filtersFootprints() = runTest {
        viewModel.filterByContact(1L)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.selectedContactId).isEqualTo(1L)
        }
    }

    @Test
    fun filterByEventType_filtersFootprints() = runTest {
        viewModel.filterByEventType("TRAVEL")

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.filterEventType).isEqualTo("TRAVEL")
        }
    }

    @Test
    fun clearFilters_resetsFilters() = runTest {
        viewModel.filterByContact(1L)
        viewModel.filterByEventType("TRAVEL")
        viewModel.clearFilters()

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.selectedContactId).isNull()
            assertThat(state.filterEventType).isNull()
        }
    }

    @Test
    fun init_eventsWithoutLocation_areExcluded() = runTest {
        val eventNoLocation = Event(id = 2, title = "NoLoc", type = "MEETUP", time = 2000L, location = null)
        every { eventRepository.getEventsWithLocation() } returns flowOf(listOf(testEvent, eventNoLocation))

        val freshViewModel = FootprintsViewModel(eventRepository, contactRepository, customTypeRepository)

        freshViewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.footprints).hasSize(1)
            assertThat(state.footprints[0].eventTitle).isEqualTo("Trip")
        }
    }
}
