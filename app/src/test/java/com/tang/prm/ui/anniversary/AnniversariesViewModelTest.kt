package com.tang.prm.ui.anniversary

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.repository.AnniversaryRepository
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.util.DateUtils
import io.mockk.coEvery
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
        every { DateUtils.getNextBirthdayDate(any()) } returns System.currentTimeMillis() + 86400000L
        every { DateUtils.getNextRepeatDate(any()) } returns System.currentTimeMillis() + 86400000L
        every { DateUtils.calculateDaysInfo(any()) } returns DateUtils.DaysInfo(daysPassed = 0, daysUntil = 1, isPast = false)

        anniversaryRepository = mockk()
        contactRepository = mockk()
        every { anniversaryRepository.getAllAnniversaries() } returns flowOf(listOf(testAnniversary))
        every { contactRepository.getAllContacts() } returns flowOf(emptyList())

        viewModel = AnniversariesViewModel(anniversaryRepository, contactRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkObject(DateUtils)
    }

    @Test
    fun initLoadsAnniversaries() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.allAnniversaries).contains(testAnniversary)
            assertThat(state.isLoading).isFalse()
        }
    }

    @Test
    fun onTabSelectedUpdatesSelectedTab() = runTest {
        viewModel.onTabSelected(1)
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.selectedTab).isEqualTo(1)
        }
    }

    @Test
    fun onSearchQueryChangeUpdatesSearchQuery() = runTest {
        viewModel.onSearchQueryChange("Test")
        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.searchQuery).isEqualTo("Test")
        }
    }

    @Test
    fun deleteAnniversaryCallsRepository() = runTest {
        coEvery { anniversaryRepository.deleteAnniversary(any()) } returns Unit
        viewModel.deleteAnniversary(1L)
        coEvery { anniversaryRepository.deleteAnniversary(1L) }
    }
}
