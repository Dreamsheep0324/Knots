package com.tang.prm.feature.remember.anniversary

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.repository.AnniversaryRepository
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.usecase.GetAnniversaryDisplayUseCase
import com.tang.prm.domain.util.DateCalcUtils
import com.tang.prm.domain.util.DateUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
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
class AnniversariesViewModelTest {

    private lateinit var anniversaryRepository: AnniversaryRepository
    private lateinit var contactRepository: ContactRepository
    private lateinit var getAnniversaryDisplayUseCase: GetAnniversaryDisplayUseCase
    private lateinit var viewModel: AnniversariesViewModel

    private val testAnniversary = Anniversary(
        id = 1L,
        contactId = 1L,
        name = "Test Birthday",
        type = AnniversaryType.BIRTHDAY,
        date = System.currentTimeMillis() + 86400000L,
        isRepeat = true
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockkObject(DateUtils)
        mockkObject(DateCalcUtils)
        every { DateCalcUtils.getNextBirthdayDate(any()) } returns System.currentTimeMillis() + 86400000L
        every { DateCalcUtils.getNextRepeatDate(any()) } returns System.currentTimeMillis() + 86400000L
        every { DateCalcUtils.calculateDaysInfo(any()) } returns DateCalcUtils.DaysInfo(daysPassed = 0, daysUntil = 1, isPast = false)
        every { DateCalcUtils.getTodayStart() } returns System.currentTimeMillis()

        anniversaryRepository = mockk()
        contactRepository = mockk()
        getAnniversaryDisplayUseCase = GetAnniversaryDisplayUseCase()
        every { anniversaryRepository.getAllAnniversaries() } returns flowOf(listOf(testAnniversary))
        every { contactRepository.getAllContacts() } returns flowOf(emptyList())

        viewModel = AnniversariesViewModel(anniversaryRepository, contactRepository, getAnniversaryDisplayUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(DateUtils)
        unmockkObject(DateCalcUtils)
    }

    @Test
    fun initLoadsAnniversaries() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.allAnniversaries).contains(testAnniversary)
            assertThat(state.data.isLoading).isFalse()
        }
    }

    @Test
    fun onTabSelectedUpdatesSelectedTab() = runTest {
        viewModel.onTabSelected(1)
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.data.selectedTab).isEqualTo(1)
        }
    }

    @Test
    fun onSearchQueryChangeUpdatesSearchState() = runTest {
        viewModel.onSearchQueryChange("Test")
        viewModel.searchState.test {
            val state = awaitItem()
            assertThat(state.query).isEqualTo("Test")
        }
    }

    @Test
    fun deleteAnniversaryCallsRepository() = runTest {
        coEvery { anniversaryRepository.deleteAnniversary(any()) } returns Unit
        viewModel.deleteAnniversary(1L)
        coVerify { anniversaryRepository.deleteAnniversary(1L) }
    }
}
