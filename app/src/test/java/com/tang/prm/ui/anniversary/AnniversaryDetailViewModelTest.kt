package com.tang.prm.ui.anniversary

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.repository.AnniversaryRepository
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AnniversaryDetailViewModelTest {

    private lateinit var anniversaryRepository: AnniversaryRepository
    private lateinit var viewModel: AnniversaryDetailViewModel

    private val testAnniversary = Anniversary(
        id = 1L,
        contactId = 1L,
        name = "Test Anniversary",
        type = AnniversaryType.ANNIVERSARY,
        date = System.currentTimeMillis()
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        anniversaryRepository = mockk()
        coEvery { anniversaryRepository.getAnniversaryById(any()) } returns flowOf(testAnniversary)
        coEvery { anniversaryRepository.deleteAnniversary(any()) } returns Unit
        viewModel = AnniversaryDetailViewModel(anniversaryRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadAnniversaryUpdatesUiState() = runTest {
        viewModel.loadAnniversary(1L)
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.anniversary).isEqualTo(testAnniversary)
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun deleteAnniversaryCallsRepository() = runTest {
        viewModel.loadAnniversary(1L)
        viewModel.uiState.test {
            awaitItem()
        }
        viewModel.deleteAnniversary()
        coVerify { anniversaryRepository.deleteAnniversary(1L) }
    }
}
