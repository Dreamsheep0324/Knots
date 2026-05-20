package com.tang.prm.ui.contacts

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.ContactGroup
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.repository.AnniversaryRepository
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
class AddContactViewModelTest {

    @MockK
    private lateinit var contactRepository: ContactRepository

    @MockK
    private lateinit var groupRepository: ContactGroupRepository

    @MockK
    private lateinit var customTypeRepository: CustomTypeRepository

    @MockK
    private lateinit var anniversaryRepository: AnniversaryRepository

    private lateinit var viewModel: AddContactViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        val savedStateHandle = SavedStateHandle(mapOf<String, Any?>())

        every { groupRepository.getAllGroups() } returns flowOf(emptyList<ContactGroup>())
        every { customTypeRepository.getTypesByCategory(CustomCategories.RELATIONSHIP) } returns flowOf(emptyList<CustomType>())
        every { customTypeRepository.getTypesByCategory(CustomCategories.EDUCATION) } returns flowOf(emptyList<CustomType>())
        every { customTypeRepository.getTypesByCategory(CustomCategories.HOBBY) } returns flowOf(emptyList<CustomType>())
        every { customTypeRepository.getTypesByCategory(CustomCategories.HABIT) } returns flowOf(emptyList<CustomType>())
        every { customTypeRepository.getTypesByCategory(CustomCategories.DIET) } returns flowOf(emptyList<CustomType>())
        every { customTypeRepository.getTypesByCategory(CustomCategories.SKILL) } returns flowOf(emptyList<CustomType>())

        viewModel = AddContactViewModel(contactRepository, groupRepository, customTypeRepository, anniversaryRepository, savedStateHandle)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveContact_callsInsertWhenNameProvided() = runTest {
        coEvery { contactRepository.insertContact(any()) } returns 1L

        viewModel.updateName("Alice")
        viewModel.updatePhone("123456")
        viewModel.saveContact()

        coVerify { contactRepository.insertContact(match { it.name == "Alice" && it.phone == "123456" }) }
    }

    @Test
    fun saveContact_doesNotCallInsertWhenNameEmpty() = runTest {
        coEvery { contactRepository.insertContact(any()) } returns 1L

        viewModel.updateName("")
        viewModel.saveContact()

        coVerify(exactly = 0) { contactRepository.insertContact(any()) }
    }

    @Test
    fun saveContact_doesNotCallInsertWhenNameBlank() = runTest {
        coEvery { contactRepository.insertContact(any()) } returns 1L

        viewModel.updateName("   ")
        viewModel.saveContact()

        coVerify(exactly = 0) { contactRepository.insertContact(any()) }
    }

    @Test
    fun updateName_updatesState() = runTest {
        viewModel.updateName("Bob")

        assertThat(viewModel.uiState.value.name).isEqualTo("Bob")
        assertThat(viewModel.uiState.value.hasUnsavedChanges).isTrue()
    }

    @Test
    fun updatePhone_updatesState() = runTest {
        viewModel.updatePhone("987654")

        assertThat(viewModel.uiState.value.phone).isEqualTo("987654")
        assertThat(viewModel.uiState.value.hasUnsavedChanges).isTrue()
    }

    @Test
    fun updateIntimacyScore_clampsToRange() = runTest {
        viewModel.updateIntimacyScore(150)

        assertThat(viewModel.uiState.value.intimacyScore).isEqualTo(100)

        viewModel.updateIntimacyScore(-10)

        assertThat(viewModel.uiState.value.intimacyScore).isEqualTo(0)
    }

    @Test
    fun saveContact_updatesIsSavedFlag() = runTest {
        coEvery { contactRepository.insertContact(any()) } returns 1L

        viewModel.updateName("Alice")
        viewModel.saveContact()

        assertThat(viewModel.uiState.value.isSaved).isTrue()
    }
}
