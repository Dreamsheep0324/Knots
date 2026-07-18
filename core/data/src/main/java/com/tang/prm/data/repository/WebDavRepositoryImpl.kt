package com.tang.prm.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.remote.WebDavClient
import com.tang.prm.domain.model.CloudBackupVersion
import com.tang.prm.domain.model.ConnectionTestResult
import com.tang.prm.domain.model.FileEntry
import com.tang.prm.domain.model.RestoreResult
import com.tang.prm.domain.model.SyncManifest
import com.tang.prm.domain.model.SyncResult
import com.tang.prm.domain.model.WebDavConfig
import com.tang.prm.domain.repository.BackupRepositoryInterface
import com.tang.prm.domain.repository.WebDavRepository
import com.tang.prm.domain.util.DateUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebDavRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val webDavClient: WebDavClient,
    private val backupRepository: BackupRepositoryInterface,
    private val configStore: WebDavConfigStore,
    private val fileDiffCalculator: SyncFileDiffCalculator
) : WebDavRepository {

    companion object {
        private const val TAG = "WebDavRepository"
    }

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override fun getConfig(): Flow<WebDavConfig> = configStore.configCache

    override suspend fun saveConfig(config: WebDavConfig) {
        configStore.saveConfig(config)
    }

    override suspend fun testConnection(): ConnectionTestResult {
        val config = configStore.readConfig()
        if (config.serverUrl.isBlank()) {
            return ConnectionTestResult.Error("未配置 WebDAV 服务器地址")
        }
        return webDavClient.testConnection(config)
    }

    override suspend fun listRemoteBackups(): List<CloudBackupVersion> {
        val config = configStore.readConfig()
        if (config.serverUrl.isBlank()) return emptyList()

        // 尝试读取增量清单
        val manifest = readRemoteManifest()
        if (manifest != null) {
            val imageCount = manifest.images.size + manifest.giftPhotos.size
            val dbDisplayName = try {
                val timePart = manifest.dbBackup.removePrefix("tang_backup_").removeSuffix(".zip")
                DateUtils.parseBackupTimestamp(timePart)
                    ?.let { DateUtils.formatDateTimeHyphen(it) }
                    ?: manifest.dbBackup
            } catch (e: Exception) {
                Log.w(TAG, "解析远程清单日期失败，使用原始文件名", e)
                manifest.dbBackup
            }

            // 获取远端数据库文件大小
            val dbFileSize = if (manifest.dbBackup.isNotBlank()) {
                try { webDavClient.getSubDirFileSize(config, "db", manifest.dbBackup) } catch (e: Exception) {
                    Log.w(TAG, "获取远程数据库文件大小失败，显示为 0", e)
                    0L
                }
            } else 0L

            return listOf(CloudBackupVersion(
                fileName = manifest.dbBackup,
                fileSize = dbFileSize,
                lastModified = "",
                displayName = dbDisplayName,
                imageCount = imageCount,
                isIncremental = true
            ))
        }

        // 降级：旧版全量 ZIP 列表
        return webDavClient.listFiles(config)
    }

    // ===== 增量上传 =====

    override suspend fun uploadBackup(): Flow<SyncResult> = flow {
        val config = configStore.readConfig()
        if (config.serverUrl.isBlank()) {
            emit(SyncResult.Error("未配置 WebDAV 服务器"))
            return@flow
        }

        try {
            webDavClient.ensureRemoteDir(config)
            webDavClient.ensureRemoteSubDir(config, "db")
            webDavClient.ensureRemoteSubDir(config, "images")
            webDavClient.ensureRemoteSubDir(config, "gift_photos")
        } catch (e: Exception) {
            emit(SyncResult.Error("创建远程目录失败：${e.message}"))
            return@flow
        }

        // 1. 读取远端清单
        emit(SyncResult.UploadProgress("准备中", 0, 0, "读取云端清单..."))
        val remoteManifest = readRemoteManifest()

        // 2. 扫描本地文件（只包含数据库引用的图片，过滤孤立文件）
        val referencedNames = backupRepository.getReferencedImageFileNames()
        val localImages = backupRepository.listLocalImageFiles("app_images")
            .filter { it.name in referencedNames }
        val localGiftPhotos = backupRepository.listLocalImageFiles("gift_photos")

        // 3. 计算差异
        val (imagesToUpload, imagesToDeleteRemote) = fileDiffCalculator.computeUploadDiff(localImages, remoteManifest?.images ?: emptyMap())
        val (giftToUpload, giftToDeleteRemote) = fileDiffCalculator.computeUploadDiff(localGiftPhotos, remoteManifest?.giftPhotos ?: emptyMap())
        val dbChanged = isDbChanged(remoteManifest)

        val totalSteps = (if (dbChanged) 1 else 0) + imagesToUpload.size + giftToUpload.size +
                         imagesToDeleteRemote.size + giftToDeleteRemote.size + 1 // manifest
        var currentStep = 0

        if (totalSteps == 1 && !dbChanged) {
            emit(SyncResult.UploadProgress("完成", 1, 1, "数据已是最新，无需同步"))
            emit(SyncResult.UploadSuccess("", 0, localImages.size + localGiftPhotos.size))
            return@flow
        }

        // 4. 上传数据库
        var dbFileName = remoteManifest?.dbBackup ?: ""
        if (dbChanged) {
            currentStep++
            emit(SyncResult.UploadProgress("上传数据库", currentStep, totalSteps, "正在上传数据库..."))
            val tempFile = File(context.cacheDir, "webdav_db_${System.currentTimeMillis()}.zip")
            try {
                val info = backupRepository.backupDbOnly(tempFile)
                webDavClient.uploadDbFile(config, info.fileName, tempFile)
                // 删除远端旧数据库文件
                if (remoteManifest != null && remoteManifest.dbBackup.isNotBlank() && remoteManifest.dbBackup != info.fileName) {
                    try {
                        webDavClient.deleteRemoteFile(config, "db", remoteManifest.dbBackup)
                    } catch (e: Exception) {
                        Log.w(TAG, "删除远端旧数据库文件失败", e)
                    }
                }
                dbFileName = info.fileName
            } finally {
                withContext(Dispatchers.IO) { tempFile.delete() }
            }
        }

        // 5. 上传图片
        var uploadFailures = 0
        val uploadStartTime = System.currentTimeMillis()
        val uploadedImageEntries = mutableMapOf<String, FileEntry>()
        for ((index, entry) in imagesToUpload.withIndex()) {
            currentStep++
            emit(SyncResult.UploadProgress("上传图片", currentStep, totalSteps,
                "正在上传图片 ${index + 1}/${imagesToUpload.size}"))
            val file = backupRepository.getLocalImageFile("app_images", entry.name) ?: continue
            try {
                webDavClient.uploadImageFile(config, "images", entry.name, file)
                uploadedImageEntries[entry.name] = entry.copy(uploadedAt = uploadStartTime)
            } catch (e: Exception) {
                android.util.Log.w(TAG, "图片上传失败: ${entry.name}", e)
                uploadFailures++
            }
        }

        val uploadedGiftEntries = mutableMapOf<String, FileEntry>()
        for ((index, entry) in giftToUpload.withIndex()) {
            currentStep++
            emit(SyncResult.UploadProgress("上传图片", currentStep, totalSteps,
                "正在上传礼物照片 ${index + 1}/${giftToUpload.size}"))
            val file = backupRepository.getLocalImageFile("gift_photos", entry.name) ?: continue
            try {
                webDavClient.uploadImageFile(config, "gift_photos", entry.name, file)
                uploadedGiftEntries[entry.name] = entry.copy(uploadedAt = uploadStartTime)
            } catch (e: Exception) {
                android.util.Log.w(TAG, "礼物照片上传失败: ${entry.name}", e)
                uploadFailures++
            }
        }

        // 6. 清理远端多余文件
        for (name in imagesToDeleteRemote) {
            currentStep++
            emit(SyncResult.UploadProgress("清理", currentStep, totalSteps, "正在清理远端文件..."))
            webDavClient.deleteRemoteFile(config, "images", name)
        }
        for (name in giftToDeleteRemote) {
            currentStep++
            emit(SyncResult.UploadProgress("清理", currentStep, totalSteps, "正在清理远端文件..."))
            webDavClient.deleteRemoteFile(config, "gift_photos", name)
        }

        // 7. 更新并上传 manifest（仅包含成功上传的文件，保证 manifest 与实际一致）
        currentStep++
        emit(SyncResult.UploadProgress("上传清单", currentStep, totalSteps, "正在更新同步清单..."))
        val now = System.currentTimeMillis()
        // 未上传的图片保留远端旧条目（若存在），避免 manifest 丢失已上传文件记录；
        // 同时排除已删除的远端文件，避免 manifest 残留孤儿条目导致下次同步误判
        val imageEntries = (remoteManifest?.images ?: emptyMap())
            .filterKeys { it !in imagesToDeleteRemote }
            .toMutableMap()
            .apply { putAll(uploadedImageEntries) }
        val giftEntries = (remoteManifest?.giftPhotos ?: emptyMap())
            .filterKeys { it !in giftToDeleteRemote }
            .toMutableMap()
            .apply { putAll(uploadedGiftEntries) }

        val newManifest = SyncManifest(
            version = 1,
            lastSyncTime = now,
            dbBackup = dbFileName,
            dbTimestamp = now,
            images = imageEntries,
            giftPhotos = giftEntries
        )
        writeRemoteManifest(newManifest)

        configStore.saveLastSyncTime(now, "upload")
        val totalUploaded = imagesToUpload.size + giftToUpload.size - uploadFailures
        val totalSkipped = (localImages.size + localGiftPhotos.size) - imagesToUpload.size - giftToUpload.size

        if (uploadFailures > 0) {
            emit(SyncResult.PartialSuccess(
                dbFileName,
                succeeded = totalUploaded,
                failed = uploadFailures,
                skipped = totalSkipped
            ))
        } else {
            emit(SyncResult.UploadSuccess(dbFileName, totalUploaded, totalSkipped))
        }
    }

    // ===== 增量下载 =====

    override suspend fun downloadBackup(fileName: String): Flow<SyncResult> = flow {
        val config = configStore.readConfig()
        if (config.serverUrl.isBlank()) {
            emit(SyncResult.Error("未配置 WebDAV 服务器"))
            return@flow
        }

        // 1. 读取远端清单
        emit(SyncResult.DownloadProgress("准备中", 0, 0, "读取云端清单..."))
        val remoteManifest = readRemoteManifest()
        if (remoteManifest == null) {
            // 无清单，尝试旧版全量下载
            emit(SyncResult.DownloadProgress("下载中", 0, 1, "正在下载全量备份..."))
            val tempFile = File(context.cacheDir, "webdav_download_${System.currentTimeMillis()}.zip")
            try {
                webDavClient.downloadDbFile(config, fileName, tempFile)
                val uri = Uri.fromFile(tempFile).toString()
                backupRepository.restoreFromUri(uri).collect { result ->
                    when (result) {
                        is RestoreResult.Success -> {
                            configStore.saveLastSyncTime(System.currentTimeMillis(), "download")
                            emit(SyncResult.DownloadSuccess(fileName, 0, 0))
                        }
                        is RestoreResult.Error -> emit(SyncResult.Error(result.message))
                        is RestoreResult.FatalError -> emit(SyncResult.Error(result.message))
                    }
                }
            } catch (e: Exception) {
                emit(SyncResult.Error("下载失败：${e.message}"))
            } finally {
                withContext(Dispatchers.IO) { tempFile.delete() }
            }
            return@flow
        }

        // 2. 扫描本地文件（只包含数据库引用的图片，过滤孤立文件）
        val referencedNames = backupRepository.getReferencedImageFileNames()
        val localImages = backupRepository.listLocalImageFiles("app_images")
            .filter { it.name in referencedNames }
        val localGiftPhotos = backupRepository.listLocalImageFiles("gift_photos")

        // 3. 计算差异
        val (imagesToDownload, imagesToDeleteLocal) = fileDiffCalculator.computeDownloadDiff(
            localImages, remoteManifest.images
        )
        val (giftToDownload, giftToDeleteLocal) = fileDiffCalculator.computeDownloadDiff(
            localGiftPhotos, remoteManifest.giftPhotos
        )
        val dbChanged = true // 恢复时总是下载最新数据库

        val totalSteps = imagesToDownload.size + giftToDownload.size +
                         imagesToDeleteLocal.size + giftToDeleteLocal.size +
                         (if (dbChanged) 1 else 0)
        var currentStep = 0

        // 4. 先下载图片（数据库恢复会重启进程，必须放在最后）
        var downloadFailures = 0
        for ((index, entry) in imagesToDownload.withIndex()) {
            currentStep++
            emit(SyncResult.DownloadProgress("下载图片", currentStep, totalSteps,
                "正在下载图片 ${index + 1}/${imagesToDownload.size}"))
            val targetFile = File(File(context.filesDir, "app_images"), entry.name)
            try {
                webDavClient.downloadImageFile(config, "images", entry.name, targetFile)
            } catch (e: Exception) {
                android.util.Log.w(TAG, "图片下载失败: ${entry.name}", e)
                downloadFailures++
            }
        }

        for ((index, entry) in giftToDownload.withIndex()) {
            currentStep++
            emit(SyncResult.DownloadProgress("下载图片", currentStep, totalSteps,
                "正在下载礼物照片 ${index + 1}/${giftToDownload.size}"))
            val targetFile = File(File(context.filesDir, "gift_photos"), entry.name)
            try {
                webDavClient.downloadImageFile(config, "gift_photos", entry.name, targetFile)
            } catch (e: Exception) {
                android.util.Log.w(TAG, "礼物照片下载失败: ${entry.name}", e)
                downloadFailures++
            }
        }

        // 5. 清理本地多余图片（在数据库恢复前执行，因为恢复后会重启进程）
        for (name in imagesToDeleteLocal) {
            currentStep++
            emit(SyncResult.DownloadProgress("清理", currentStep, totalSteps, "正在清理本地文件..."))
            backupRepository.deleteLocalImageFile("app_images", name)
        }
        for (name in giftToDeleteLocal) {
            currentStep++
            emit(SyncResult.DownloadProgress("清理", currentStep, totalSteps, "正在清理本地文件..."))
            backupRepository.deleteLocalImageFile("gift_photos", name)
        }

        // 6. 最后恢复数据库（会重启进程，必须放在所有操作之后）
        if (dbChanged && remoteManifest.dbBackup.isNotBlank()) {
            currentStep++
            emit(SyncResult.DownloadProgress("恢复数据库", currentStep, totalSteps, "正在恢复数据库..."))
            val tempFile = File(context.cacheDir, "webdav_db_${System.currentTimeMillis()}.zip")
            try {
                webDavClient.downloadDbFile(config, remoteManifest.dbBackup, tempFile)
                val uri = Uri.fromFile(tempFile).toString()
                var dbRestoreFailed = false
                backupRepository.restoreDbOnly(uri).collect { result ->
                    when (result) {
                        is RestoreResult.Success -> {}
                        is RestoreResult.Error -> {
                            dbRestoreFailed = true
                            emit(SyncResult.Error(result.message))
                        }
                        is RestoreResult.FatalError -> {
                            dbRestoreFailed = true
                            emit(SyncResult.Error(result.message))
                        }
                    }
                }
                if (dbRestoreFailed) {
                    return@flow
                }
            } finally {
                withContext(Dispatchers.IO) { tempFile.delete() }
            }
        }

        // 7. 保存同步时间并报告结果
        //    注意：如果数据库恢复成功，restartApp() 已杀死进程，以下代码不会执行。
        //    但如果 dbChanged=false（不需要恢复数据库），则需要在这里报告结果。
        configStore.saveLastSyncTime(System.currentTimeMillis(), "download")
        val totalDownloaded = imagesToDownload.size + giftToDownload.size - downloadFailures
        val totalSkipped = (localImages.size + localGiftPhotos.size) - imagesToDownload.size - giftToDownload.size

        if (downloadFailures > 0) {
            emit(SyncResult.PartialSuccess(
                remoteManifest.dbBackup,
                succeeded = totalDownloaded,
                failed = downloadFailures,
                skipped = totalSkipped
            ))
        } else {
            emit(SyncResult.DownloadSuccess(remoteManifest.dbBackup, totalDownloaded, totalSkipped))
        }
    }

    override suspend fun deleteRemoteBackup(fileName: String): Boolean {
        val config = configStore.readConfig()
        if (config.serverUrl.isBlank()) return false
        return webDavClient.deleteFile(config, fileName)
    }

    // ===== 增量同步核心方法 =====

    private suspend fun readRemoteManifest(): SyncManifest? {
        return try {
            val config = configStore.readConfig()
            val content = webDavClient.downloadSmallFile(config, "manifest.json")
            if (content != null) {
                json.decodeFromString<SyncManifest>(content)
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "读取远程清单失败", e)
            null
        }
    }

    private suspend fun writeRemoteManifest(manifest: SyncManifest) {
        val config = configStore.readConfig()
        val content = json.encodeToString(SyncManifest.serializer(), manifest)
        webDavClient.uploadSmallFile(config, "manifest.json", content)
    }

    private fun isDbChanged(remoteManifest: SyncManifest?): Boolean {
        if (remoteManifest == null) return true
        // REP-C-1 修复：使用 TangDatabase.DB_NAME 常量替代字符串字面量。
        val dbFile = context.getDatabasePath(TangDatabase.DB_NAME)
        val walFile = File(dbFile.parent, "${TangDatabase.DB_NAME}-wal")
        val localDbModified = maxOf(
            if (dbFile.exists()) dbFile.lastModified() else 0L,
            if (walFile.exists()) walFile.lastModified() else 0L
        )
        return localDbModified > remoteManifest.dbTimestamp
    }
}
