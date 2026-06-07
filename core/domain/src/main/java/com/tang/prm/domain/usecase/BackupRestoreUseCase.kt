package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.BackupFileInfo
import com.tang.prm.domain.model.BackupImageQuality
import com.tang.prm.domain.model.BackupInfo
import com.tang.prm.domain.model.BackupResult
import com.tang.prm.domain.model.ClearDataResult
import com.tang.prm.domain.model.RestoreResult
import com.tang.prm.domain.repository.BackupRepositoryInterface
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class BackupRestoreUseCase @Inject constructor(
    private val backupRepository: BackupRepositoryInterface
) {
    suspend fun backupToUri(uri: String, imageQuality: BackupImageQuality = BackupImageQuality.STANDARD): Flow<BackupResult> =
        backupRepository.backupToUri(uri, imageQuality)
    suspend fun restoreFromUri(uri: String): Flow<RestoreResult> = backupRepository.restoreFromUri(uri)
    suspend fun clearAllData(): ClearDataResult = backupRepository.clearAllData()
    fun generateBackupFileName(): String = backupRepository.generateBackupFileName()
    suspend fun backupToFile(file: File, imageQuality: BackupImageQuality = BackupImageQuality.ORIGINAL): BackupInfo =
        backupRepository.backupToFile(file, imageQuality)

    // SAF 外部目录备份管理
    fun getBackupDirUri(): String? = backupRepository.getBackupDirUri()
    fun setBackupDirUri(uri: String) = backupRepository.setBackupDirUri(uri)
    fun hasBackupDir(): Boolean = backupRepository.hasBackupDir()
    fun getBackupDirName(): String = backupRepository.getBackupDirName()
    suspend fun backupToDir(imageQuality: BackupImageQuality): BackupInfo =
        backupRepository.backupToDir(imageQuality)
    suspend fun listBackups(): List<BackupFileInfo> = backupRepository.listBackups()
    suspend fun deleteBackup(fileName: String): Boolean = backupRepository.deleteBackup(fileName)
    suspend fun restoreBackup(fileName: String): Flow<RestoreResult> =
        backupRepository.restoreBackup(fileName)
    suspend fun computeDataFingerprint(): String =
        backupRepository.computeDataFingerprint()
}
