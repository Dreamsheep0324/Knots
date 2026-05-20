package com.tang.prm.ui.profile

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.repository.BackupInfo
import com.tang.prm.data.repository.BackupRepository
import com.tang.prm.data.repository.BackupResult
import com.tang.prm.data.repository.ClearDataResult
import com.tang.prm.data.repository.RestoreResult
import io.mockk.coEvery
import io.mockk.every
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
class BackupViewModelTest {

    private lateinit var backupRepository: BackupRepository
    private lateinit var viewModel: BackupViewModel

    private val testUri = mockk<android.net.Uri>()
    private val testBackupInfo = BackupInfo(
        fileName = "backup.zip",
        fileSize = 1024L,
        timestamp = System.currentTimeMillis()
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        backupRepository = mockk()

        coEvery { backupRepository.backupToUri(any()) } returns flowOf(BackupResult.Success(testBackupInfo))
        coEvery { backupRepository.restoreFromUri(any()) } returns flowOf(RestoreResult.Success)
        coEvery { backupRepository.clearAllData() } returns ClearDataResult.Success
        every { backupRepository.generateBackupFileName() } returns "tang_backup_test.zip"

        viewModel = BackupViewModel(backupRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun backupCallsRepository() = runTest {
        viewModel.createBackup(testUri)
        viewModel.state.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(BackupState.BackupSuccess::class.java)
            assertThat((state as BackupState.BackupSuccess).info).isEqualTo(testBackupInfo)
        }
    }

    @Test
    fun restoreCallsRepository() = runTest {
        viewModel.restoreBackup(testUri)
        viewModel.state.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(BackupState.RestoreSuccess::class.java)
        }
    }

    @Test
    fun clearDataCallsRepository() = runTest {
        viewModel.clearAllData()
        viewModel.state.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(BackupState.ClearSuccess::class.java)
        }
    }

    @Test
    fun resetStateSetsIdle() = runTest {
        viewModel.clearAllData()
        viewModel.resetState()
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(BackupState.Idle)
        }
    }
}
