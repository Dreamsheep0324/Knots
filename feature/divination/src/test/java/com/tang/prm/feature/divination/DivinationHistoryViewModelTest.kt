package com.tang.prm.feature.divination

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.divination.model.DivinationRecord
import com.tang.prm.domain.divination.repository.DivinationRepository
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
class DivinationHistoryViewModelTest {

    private lateinit var repository: DivinationRepository
    private lateinit var viewModel: DivinationHistoryViewModel

    private val testRecord = DivinationRecord(
        id = 1L,
        method = "meihua",
        question = "Test question",
        resultJson = "{}",
        createdAt = System.currentTimeMillis()
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = mockk()
        coEvery { repository.getAllRecords() } returns flowOf(listOf(testRecord))
        coEvery { repository.deleteRecord(any()) } returns Unit

        viewModel = DivinationHistoryViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initLoadsRecords() = runTest {
        viewModel.records.test {
            val records = awaitItem()
            assertThat(records).hasSize(1)
            assertThat(records.first()).isEqualTo(testRecord)
        }
    }

    @Test
    fun deleteRecordCallsRepository() = runTest {
        viewModel.deleteRecord(testRecord)
        coVerify { repository.deleteRecord(testRecord) }
    }
}
