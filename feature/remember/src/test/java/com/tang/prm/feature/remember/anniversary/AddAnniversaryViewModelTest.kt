package com.tang.prm.feature.remember.anniversary

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.repository.AnniversaryRepository
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
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
class AddAnniversaryViewModelTest {

    private lateinit var anniversaryRepository: AnniversaryRepository
    private lateinit var contactRepository: ContactRepository
    private lateinit var customTypeRepository: CustomTypeRepository
    private lateinit var viewModel: AddAnniversaryViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        anniversaryRepository = mockk()
        contactRepository = mockk()
        customTypeRepository = mockk()

        coEvery { contactRepository.getAllContacts() } returns flowOf(emptyList())
        coEvery { customTypeRepository.getTypesByCategory(any()) } returns flowOf(emptyList())
        coEvery { anniversaryRepository.insertAnniversary(any()) } returns 1L
        coEvery { anniversaryRepository.updateAnniversary(any()) } returns Unit

        val savedStateHandle = SavedStateHandle(mapOf("anniversaryId" to 0L))
        viewModel = AddAnniversaryViewModel(
            anniversaryRepository,
            contactRepository,
            customTypeRepository,
            savedStateHandle
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveAnniversaryCallsInsert() = runTest {
        viewModel.updateName("Test Anniversary")
        viewModel.updateSelectedType(AnniversaryType.BIRTHDAY)
        viewModel.saveAnniversary()
        coVerify { anniversaryRepository.insertAnniversary(any()) }
    }

    @Test
    fun updateIsLunarChangesState() = runTest {
        viewModel.updateIsLunar(true)
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.isLunar).isTrue()
        }
    }

    @Test
    fun saveAnniversaryWithBlankNameDoesNotCallInsert() = runTest {
        viewModel.saveAnniversary()
        coVerify(exactly = 0) { anniversaryRepository.insertAnniversary(any()) }
    }
}
