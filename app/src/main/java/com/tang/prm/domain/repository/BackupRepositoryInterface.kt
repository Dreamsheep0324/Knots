package com.tang.prm.domain.repository

import android.net.Uri
import com.tang.prm.data.repository.BackupInfo
import com.tang.prm.data.repository.BackupResult
import com.tang.prm.data.repository.ClearDataResult
import com.tang.prm.data.repository.RestoreResult
import kotlinx.coroutines.flow.Flow

interface BackupRepositoryInterface {
    suspend fun backupToUri(uri: Uri): Flow<BackupResult>
    suspend fun restoreFromUri(uri: Uri): Flow<RestoreResult>
    fun generateBackupFileName(): String
    suspend fun clearAllData(): ClearDataResult
}
