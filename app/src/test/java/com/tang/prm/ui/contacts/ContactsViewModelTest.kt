package com.tang.prm.ui.contacts

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.ContactGroup
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.repository.ContactGroupRepository
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
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
class ContactsViewModelTest {

    @MockK
    private lateinit var contactRepository: ContactRepository

    @MockK
    private lateinit var groupRepository: ContactGroupRepository

    @MockK
    private lateinit var customTypeRepository: CustomTypeRepository

    private lateinit var viewModel: ContactsViewModel

    private val testContacts = listOf(
        Contact(id = 1, name = "Alice", intimacyScore = 80),
        Contact(id = 2, name = "Bob", intimacyScore = 30)
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { contactRepository.getFilteredContacts(any(), any(), any()) } returns flowOf(testContacts)
        every { groupRepository.getAllGroups() } returns flowOf(emptyList<ContactGroup>())
        every { customTypeRepository.getTypesByCategory(CustomCategories.RELATIONSHIP) } returns flowOf(emptyList<CustomType>())

        viewModel = ContactsViewModel(contactRepository, groupRepository, customTypeRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loadsContacts() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.contacts).hasSize(2)
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun init_sortsByIntimacyScore() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.contacts[0].name).isEqualTo("Alice")
            assertThat(state.contacts[1].name).isEqualTo("Bob")
        }
    }

    @Test
    fun onSearchQueryChange_updatesState() = runTest {
        every { contactRepository.getFilteredContacts("Ali", null, null) } returns flowOf(listOf(testContacts[0]))

        viewModel.onSearchQueryChange("Ali")

        assertThat(viewModel.uiState.value.searchQuery).isEqualTo("Ali")
    }

    @Test
    fun deleteContact_callsRepository() = runTest {
        coEvery { contactRepository.deleteContact(1L) } returns Unit

        viewModel.deleteContact(1L)

        coVerify { contactRepository.deleteContact(1L) }
    }

    @Test
    fun onGroupSelected_updatesState() = runTest {
        viewModel.onGroupSelected(5L)

        assertThat(viewModel.uiState.value.selectedGroupId).isEqualTo(5L)
    }

    @Test
    fun onIntimacySelected_updatesState() = runTest {
        viewModel.onIntimacySelected("密友")

        assertThat(viewModel.uiState.value.selectedIntimacy).isEqualTo("密友")
    }

    @Test
    fun onViewModeChange_updatesState() = runTest {
        viewModel.onViewModeChange(1)

        assertThat(viewModel.uiState.value.viewMode).isEqualTo(1)
    }

    @Test
    fun toggleReorderMode_togglesState() = runTest {
        assertThat(viewModel.uiState.value.isReorderMode).isFalse()

        viewModel.toggleReorderMode()
        assertThat(viewModel.uiState.value.isReorderMode).isTrue()

        viewModel.toggleReorderMode()
        assertThat(viewModel.uiState.value.isReorderMode).isFalse()
    }
}
