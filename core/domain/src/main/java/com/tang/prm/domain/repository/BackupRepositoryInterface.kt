package com.tang.prm.domain.repository

import com.tang.prm.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.io.File

interface BackupRepositoryInterface {
    suspend fun backupToUri(uri: String, imageQuality: BackupImageQuality = BackupImageQuality.STANDARD): Flow<BackupResult>
    suspend fun restoreFromUri(uri: String): Flow<RestoreResult>
    fun generateBackupFileName(): String
    suspend fun clearAllData(): ClearDataResult
    suspend fun backupToFile(file: File, imageQuality: BackupImageQuality = BackupImageQuality.ORIGINAL): BackupInfo
    // SAF 外部目录备份管理
    fun getBackupDirUri(): String?
    fun setBackupDirUri(uri: String)
    fun hasBackupDir(): Boolean
    fun getBackupDirName(): String
    suspend fun backupToDir(imageQuality: BackupImageQuality): BackupInfo
    suspend fun listBackups(): List<BackupFileInfo>
    suspend fun deleteBackup(fileName: String): Boolean
    suspend fun restoreBackup(fileName: String): Flow<RestoreResult>
    suspend fun computeDataFingerprint(): String
    // 增量同步支持
    fun listLocalImageFiles(dir: String): List<FileEntry>
    fun getLocalImageFile(dir: String, fileName: String): File?
    suspend fun backupDbOnly(file: File): BackupInfo
    suspend fun restoreDbOnly(uri: String): Flow<RestoreResult>
    fun deleteLocalImageFile(dir: String, fileName: String): Boolean
    // 图片引用过滤
    suspend fun getReferencedImageFileNames(): Set<String>
    suspend fun cleanOrphanedImages(): Int
}
