package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.CloudBackupVersion
import com.tang.prm.domain.model.ConnectionTestResult
import com.tang.prm.domain.model.SyncResult
import com.tang.prm.domain.model.WebDavConfig
import com.tang.prm.domain.repository.BackupRepositoryInterface
import com.tang.prm.domain.repository.WebDavRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WebDavSyncUseCase @Inject constructor(
    private val webDavRepository: WebDavRepository,
    private val backupRepository: BackupRepositoryInterface
) {
    fun getConfig(): Flow<WebDavConfig> = webDavRepository.getConfig()
    suspend fun saveConfig(config: WebDavConfig) = webDavRepository.saveConfig(config)
    suspend fun testConnection(): ConnectionTestResult = webDavRepository.testConnection()
    suspend fun listRemoteBackups(): List<CloudBackupVersion> = webDavRepository.listRemoteBackups()
    suspend fun uploadBackup(): Flow<SyncResult> = webDavRepository.uploadBackup()
    suspend fun downloadBackup(fileName: String): Flow<SyncResult> = webDavRepository.downloadBackup(fileName)
    suspend fun deleteRemoteBackup(fileName: String): Boolean = webDavRepository.deleteRemoteBackup(fileName)
    suspend fun cleanOrphanedImages(): Int = backupRepository.cleanOrphanedImages()
}
