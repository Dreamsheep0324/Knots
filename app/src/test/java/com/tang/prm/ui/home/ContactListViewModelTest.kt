package com.tang.prm.ui.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Circle
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.repository.CircleRepository
import com.tang.prm.domain.repository.ContactRepository
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
class ContactListViewModelTest {

    @MockK
    private lateinit var contactRepository: ContactRepository

    @MockK
    private lateinit var circleRepository: CircleRepository

    private lateinit var viewModel: ContactListViewModel

    private val testContact = Contact(id = 1, name = "Alice")
    private val testCircle = Circle(id = 1, name = "Friends", memberIds = listOf(1L))

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { contactRepository.getAllContacts() } returns flowOf(listOf(testContact))
        every { circleRepository.getAllCircles() } returns flowOf(listOf(testCircle))

        viewModel = ContactListViewModel(contactRepository, circleRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsCircles() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.circles).hasSize(1)
            assertThat(state.data.circles[0].circle.name).isEqualTo("Friends")
            assertThat(state.data.isLoading).isFalse()
        }
    }

    @Test
    fun init_loadsContacts() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.contacts).hasSize(1)
            assertThat(state.data.contacts[0].name).isEqualTo("Alice")
        }
    }

    @Test
    fun init_mapsCircleMembers() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.circles[0].members).hasSize(1)
            assertThat(state.data.circles[0].members[0].name).isEqualTo("Alice")
        }
    }

    @Test
    fun createCircle_callsRepository() = runTest {
        coEvery { circleRepository.insertCircle(any()) } returns 1L

        viewModel.createCircle("NewCircle", null, "#FF0000", "sine")

        coVerify { circleRepository.insertCircle(match { it.name == "NewCircle" && it.color == "#FF0000" && it.waveform == "sine" }) }
    }

    @Test
    fun createCircle_hidesCreateDialog() = runTest {
        coEvery { circleRepository.insertCircle(any()) } returns 1L

        viewModel.showCreateDialog()
        assertThat(viewModel.uiState.value.dialog.showCreate).isTrue()

        viewModel.createCircle("NewCircle", null, "#FF0000", "sine")

        assertThat(viewModel.uiState.value.dialog.showCreate).isFalse()
    }

    @Test
    fun deleteCircle_callsRepository() = runTest {
        coEvery { circleRepository.deleteCircleWithChildren(1L) } returns Unit

        viewModel.deleteCircle(1L)

        coVerify { circleRepository.deleteCircleWithChildren(1L) }
    }

    @Test
    fun toggleCircleExpand_updatesState() = runTest {
        viewModel.toggleCircleExpand(1L)

        assertThat(viewModel.uiState.value.expandedCircleId).isEqualTo(1L)

        viewModel.toggleCircleExpand(1L)

        assertThat(viewModel.uiState.value.expandedCircleId).isNull()
    }

    @Test
    fun toggleSearch_updatesSearchState() = runTest {
        viewModel.toggleSearch()

        assertThat(viewModel.uiState.value.data.isSearchActive).isTrue()

        viewModel.toggleSearch()

        assertThat(viewModel.uiState.value.data.isSearchActive).isFalse()
    }
}
