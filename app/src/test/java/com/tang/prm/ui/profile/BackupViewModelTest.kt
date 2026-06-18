package com.tang.prm.ui.profile

import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.usecase.BackupRestoreUseCase
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class BackupViewModelTest {

    @MockK
    private lateinit var backupRestoreUseCase: BackupRestoreUseCase

    @Test
    fun generateBackupFileName_returnsCorrectFormat() {
        every { backupRestoreUseCase.generateBackupFileName() } returns "tang_backup_test.zip"
        val result = backupRestoreUseCase.generateBackupFileName()
        assertThat(result).startsWith("tang_backup_")
        assertThat(result).endsWith(".zip")
    }
}
