package com.tang.prm.domain.repository

import com.tang.prm.domain.model.BackupResult
import com.tang.prm.domain.model.ClearDataResult
import com.tang.prm.domain.model.RestoreResult
import kotlinx.coroutines.flow.Flow

interface BackupRepositoryInterface {
    suspend fun backupToUri(uri: String): Flow<BackupResult>
    suspend fun restoreFromUri(uri: String): Flow<RestoreResult>
    fun generateBackupFileName(): String
    suspend fun clearAllData(): ClearDataResult
}
