package com.tang.prm.domain.repository

import com.tang.prm.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * 图片卫生接口：清理孤儿图片、查询引用、本地图片文件操作。
 *
 * A-9 修复：从原 [BackupRepositoryInterface] 拆分出的子接口。
 * 调用方（如 WebDavSyncViewModel）只需图片清理能力时，应依赖此接口而非整个聚合接口，
 * 遵循接口隔离原则（ISP）。
 */
interface ImageOrphanCleaner {
    suspend fun getReferencedImageFileNames(): Set<String>
    suspend fun cleanOrphanedImages(): Int
    fun listLocalImageFiles(dir: String): List<FileEntry>
    fun getLocalImageFile(dir: String, fileName: String): File?
    fun deleteLocalImageFile(dir: String, fileName: String): Boolean
}

/**
 * 备份目录管理接口：SAF 备份目录 URI 管理。
 *
 * A-9 修复：从原 [BackupRepositoryInterface] 拆分出的子接口。
 */
interface BackupDirectoryManager {
    fun getBackupDirUri(): String?
    fun setBackupDirUri(uri: String)
    fun hasBackupDir(): Boolean
    fun getBackupDirName(): String
}

/**
 * 备份恢复接口：完整备份/恢复、DB-only 备份/恢复、清空数据、备份文件管理、指纹计算。
 *
 * A-9 修复：从原 [BackupRepositoryInterface] 拆分出的子接口。
 */
interface BackupRepository {
    suspend fun restoreFromUri(uri: String): Flow<RestoreResult>
    fun generateBackupFileName(): String
    suspend fun clearAllData(): ClearDataResult
    suspend fun backupToDir(imageQuality: BackupImageQuality): BackupInfo
    suspend fun listBackups(): List<BackupFileInfo>
    suspend fun deleteBackup(fileName: String): Boolean
    suspend fun restoreBackup(fileName: String): Flow<RestoreResult>
    suspend fun computeDataFingerprint(): String
    suspend fun backupDbOnly(file: File): BackupInfo
    suspend fun restoreDbOnly(uri: String): Flow<RestoreResult>
}

/**
 * 备份仓储聚合接口：继承 [BackupRepository] + [BackupDirectoryManager] + [ImageOrphanCleaner]。
 *
 * A-9 修复：原接口暴露 19 个方法违反 ISP。拆分为 3 个子接口后，本接口作为聚合接口保留，
 * 实现类（如 [com.tang.prm.data.repository.BackupRepositoryImpl]）实现本接口即同时实现 3 个子接口。
 * 调用方应按需依赖子接口（如 [WebDavSyncViewModel] 只需 [ImageOrphanCleaner]），
 * 仅当真正需要全部能力时才依赖本聚合接口。
 *
 * 命名保留 `Interface` 后缀以兼容现有 Hilt 绑定与引用，避免大范围重命名。
 */
interface BackupRepositoryInterface : BackupRepository, BackupDirectoryManager, ImageOrphanCleaner
