package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.BackupResult
import com.tang.prm.domain.model.ClearDataResult
import com.tang.prm.domain.model.RestoreResult
import com.tang.prm.domain.repository.BackupRepositoryInterface
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BackupRestoreUseCase @Inject constructor(
    private val backupRepository: BackupRepositoryInterface
) {
    suspend fun backupToUri(uri: String): Flow<BackupResult> = backupRepository.backupToUri(uri)

    suspend fun restoreFromUri(uri: String): Flow<RestoreResult> = backupRepository.restoreFromUri(uri)

    suspend fun clearAllData(): ClearDataResult = backupRepository.clearAllData()

    fun generateBackupFileName(): String = backupRepository.generateBackupFileName()
}
