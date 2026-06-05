package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.BackupInfo
import com.tang.prm.domain.model.BackupResult
import com.tang.prm.domain.model.ClearDataResult
import com.tang.prm.domain.repository.BackupRepositoryInterface
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.google.common.truth.Truth.assertThat

class BackupRestoreUseCaseTest {
    private lateinit var useCase: BackupRestoreUseCase
    private val repository: BackupRepositoryInterface = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        useCase = BackupRestoreUseCase(repository)
    }

    @Test
    fun `backupToUri delegates to repository`() = runTest {
        coEvery { repository.backupToUri(any()) } returns flowOf(BackupResult.Success(BackupInfo("test.json", 0L, 0L)))
        val result = useCase.backupToUri("content://test").first()
        assertThat(result).isInstanceOf(BackupResult.Success::class.java)
    }

    @Test
    fun `clearAllData delegates to repository`() = runTest {
        coEvery { repository.clearAllData() } returns ClearDataResult.Success
        val result = useCase.clearAllData()
        assertThat(result).isInstanceOf(ClearDataResult.Success::class.java)
    }

    @Test
    fun `generateBackupFileName delegates to repository`() {
        coEvery { repository.generateBackupFileName() } returns "backup_20260603.json"
        val result = useCase.generateBackupFileName()
        assertThat(result).contains("backup")
    }
}
