package com.tang.prm.domain.repository

import com.tang.prm.domain.model.CloudBackupVersion
import com.tang.prm.domain.model.ConnectionTestResult
import com.tang.prm.domain.model.SyncResult
import com.tang.prm.domain.model.WebDavConfig
import kotlinx.coroutines.flow.Flow

interface WebDavRepository {
    fun getConfig(): Flow<WebDavConfig>
    suspend fun saveConfig(config: WebDavConfig)
    suspend fun testConnection(): ConnectionTestResult
    suspend fun listRemoteBackups(): List<CloudBackupVersion>
    suspend fun uploadBackup(): Flow<SyncResult>
    suspend fun downloadBackup(fileName: String): Flow<SyncResult>
    suspend fun deleteRemoteBackup(fileName: String): Boolean
}
