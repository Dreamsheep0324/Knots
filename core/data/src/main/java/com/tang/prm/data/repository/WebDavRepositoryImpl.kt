package com.tang.prm.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Base64
import com.tang.prm.data.remote.WebDavClient
import com.tang.prm.data.util.safeDbCall
import com.tang.prm.domain.model.CloudBackupVersion
import com.tang.prm.domain.model.ConnectionTestResult
import com.tang.prm.domain.model.FileEntry
import com.tang.prm.domain.model.RestoreResult
import com.tang.prm.domain.model.SyncManifest
import com.tang.prm.domain.model.SyncResult
import com.tang.prm.domain.model.WebDavConfig
import com.tang.prm.domain.repository.BackupRepositoryInterface
import com.tang.prm.domain.repository.WebDavRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class WebDavRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val webDavClient: WebDavClient,
    private val backupRepository: BackupRepositoryInterface,
    @Named("webdav") private val encryptedPrefs: SharedPreferences
) : WebDavRepository {

    companion object {
        private const val PREFS_NAME = "webdav_config"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_REMOTE_PATH = "remote_path"
        private const val KEY_AUTO_SYNC_ON_LAUNCH = "auto_sync_on_launch"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_LAST_SYNC_DIRECTION = "last_sync_direction"
    }

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val configCache = MutableStateFlow(readConfig())

    override fun getConfig(): Flow<WebDavConfig> = configCache

    override suspend fun saveConfig(config: WebDavConfig) {
        safeDbCall {
            prefs.edit().apply {
                putString(KEY_SERVER_URL, config.serverUrl)
                putString(KEY_USERNAME, config.username)
                putString(KEY_REMOTE_PATH, config.remotePath)
                putBoolean(KEY_AUTO_SYNC_ON_LAUNCH, config.autoSyncOnLaunch)
                putLong(KEY_LAST_SYNC_TIME, config.lastSyncTime)
                putString(KEY_LAST_SYNC_DIRECTION, config.lastSyncDirection)
                apply()
            }
            savePassword(config.password)
            configCache.value = config
        }
    }

    override suspend fun testConnection(): ConnectionTestResult {
        val config = readConfig()
        if (config.serverUrl.isBlank()) {
            return ConnectionTestResult.Error("未配置 WebDAV 服务器地址")
        }
        return webDavClient.testConnection(config)
    }

    override suspend fun listRemoteBackups(): List<CloudBackupVersion> {
        val config = readConfig()
        if (config.serverUrl.isBlank()) return emptyList()

        // 尝试读取增量清单
        val manifest = readRemoteManifest()
        if (manifest != null) {
            val imageCount = manifest.images.size + manifest.giftPhotos.size
            val dbDisplayName = try {
                val timePart = manifest.dbBackup.removePrefix("tang_backup_").removeSuffix(".zip")
                java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).parse(timePart)
                    ?.let { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(it) }
                    ?: manifest.dbBackup
            } catch (_: Exception) { manifest.dbBackup }

            // 获取远端数据库文件大小
            val dbFileSize = if (manifest.dbBackup.isNotBlank()) {
                try { webDavClient.getSubDirFileSize(config, "db", manifest.dbBackup) } catch (_: Exception) { 0L }
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
        val config = readConfig()
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
        val (imagesToUpload, imagesToDeleteRemote) = computeFileDiff(localImages, remoteManifest?.images ?: emptyMap())
        val (giftToUpload, giftToDeleteRemote) = computeFileDiff(localGiftPhotos, remoteManifest?.giftPhotos ?: emptyMap())
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
                    } catch (_: Exception) {}
                }
                dbFileName = info.fileName
            } finally {
                withContext(Dispatchers.IO) { tempFile.delete() }
            }
        }

        // 5. 上传图片
        for ((index, entry) in imagesToUpload.withIndex()) {
            currentStep++
            emit(SyncResult.UploadProgress("上传图片", currentStep, totalSteps,
                "正在上传图片 ${index + 1}/${imagesToUpload.size}"))
            val file = backupRepository.getLocalImageFile("app_images", entry.name) ?: continue
            try {
                webDavClient.uploadImageFile(config, "images", entry.name, file)
            } catch (_: Exception) { continue }
        }

        for ((index, entry) in giftToUpload.withIndex()) {
            currentStep++
            emit(SyncResult.UploadProgress("上传图片", currentStep, totalSteps,
                "正在上传礼物照片 ${index + 1}/${giftToUpload.size}"))
            val file = backupRepository.getLocalImageFile("gift_photos", entry.name) ?: continue
            try {
                webDavClient.uploadImageFile(config, "gift_photos", entry.name, file)
            } catch (_: Exception) { continue }
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

        // 7. 更新并上传 manifest
        currentStep++
        emit(SyncResult.UploadProgress("上传清单", currentStep, totalSteps, "正在更新同步清单..."))
        val now = System.currentTimeMillis()
        val newManifest = SyncManifest(
            version = 1,
            lastSyncTime = now,
            dbBackup = dbFileName,
            dbTimestamp = now,
            images = localImages.associate { it.name to it.copy(uploadedAt = now) },
            giftPhotos = localGiftPhotos.associate { it.name to it.copy(uploadedAt = now) }
        )
        writeRemoteManifest(newManifest)

        saveLastSyncTime(now, "upload")
        val totalUploaded = imagesToUpload.size + giftToUpload.size
        val totalSkipped = (localImages.size + localGiftPhotos.size) - totalUploaded
        emit(SyncResult.UploadSuccess(dbFileName, totalUploaded, totalSkipped))
    }

    // ===== 增量下载 =====

    override suspend fun downloadBackup(fileName: String): Flow<SyncResult> = flow {
        val config = readConfig()
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
                            saveLastSyncTime(System.currentTimeMillis(), "download")
                            emit(SyncResult.DownloadSuccess(fileName, 0, 0))
                        }
                        is RestoreResult.Error -> emit(SyncResult.Error(result.message))
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
        val (imagesToDownload, imagesToDeleteLocal) = computeDownloadDiff(
            localImages, remoteManifest.images
        )
        val (giftToDownload, giftToDeleteLocal) = computeDownloadDiff(
            localGiftPhotos, remoteManifest.giftPhotos
        )
        val dbChanged = true // 恢复时总是下载最新数据库

        val totalSteps = imagesToDownload.size + giftToDownload.size +
                         imagesToDeleteLocal.size + giftToDeleteLocal.size +
                         (if (dbChanged) 1 else 0)
        var currentStep = 0

        // 4. 下载图片
        for ((index, entry) in imagesToDownload.withIndex()) {
            currentStep++
            emit(SyncResult.DownloadProgress("下载图片", currentStep, totalSteps,
                "正在下载图片 ${index + 1}/${imagesToDownload.size}"))
            val targetFile = File(File(context.filesDir, "app_images"), entry.name)
            try {
                webDavClient.downloadImageFile(config, "images", entry.name, targetFile)
            } catch (_: Exception) { continue }
        }

        for ((index, entry) in giftToDownload.withIndex()) {
            currentStep++
            emit(SyncResult.DownloadProgress("下载图片", currentStep, totalSteps,
                "正在下载礼物照片 ${index + 1}/${giftToDownload.size}"))
            val targetFile = File(File(context.filesDir, "gift_photos"), entry.name)
            try {
                webDavClient.downloadImageFile(config, "gift_photos", entry.name, targetFile)
            } catch (_: Exception) { continue }
        }

        // 5. 删除本地多余图片
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

        // 6. 恢复数据库
        if (dbChanged && remoteManifest.dbBackup.isNotBlank()) {
            currentStep++
            emit(SyncResult.DownloadProgress("恢复数据库", currentStep, totalSteps, "正在恢复数据库..."))
            val tempFile = File(context.cacheDir, "webdav_db_${System.currentTimeMillis()}.zip")
            try {
                webDavClient.downloadDbFile(config, remoteManifest.dbBackup, tempFile)
                val uri = Uri.fromFile(tempFile).toString()
                backupRepository.restoreDbOnly(uri).collect { result ->
                    when (result) {
                        is RestoreResult.Success -> {}
                        is RestoreResult.Error -> emit(SyncResult.Error(result.message))
                    }
                }
            } finally {
                withContext(Dispatchers.IO) { tempFile.delete() }
            }
        }

        saveLastSyncTime(System.currentTimeMillis(), "download")
        val totalDownloaded = imagesToDownload.size + giftToDownload.size
        val totalSkipped = (localImages.size + localGiftPhotos.size) - totalDownloaded
        emit(SyncResult.DownloadSuccess(remoteManifest.dbBackup, totalDownloaded, totalSkipped))
    }

    override suspend fun deleteRemoteBackup(fileName: String): Boolean {
        val config = readConfig()
        if (config.serverUrl.isBlank()) return false
        return webDavClient.deleteFile(config, fileName)
    }

    // ===== 增量同步核心方法 =====

    private suspend fun readRemoteManifest(): SyncManifest? {
        return try {
            val config = readConfig()
            val content = webDavClient.downloadSmallFile(config, "manifest.json")
            if (content != null) {
                json.decodeFromString<SyncManifest>(content)
            } else null
        } catch (_: Exception) { null }
    }

    private suspend fun writeRemoteManifest(manifest: SyncManifest) {
        val config = readConfig()
        val content = json.encodeToString(SyncManifest.serializer(), manifest)
        webDavClient.uploadSmallFile(config, "manifest.json", content)
    }

    private fun isDbChanged(remoteManifest: SyncManifest?): Boolean {
        if (remoteManifest == null) return true
        val dbFile = context.getDatabasePath("tang_database")
        val walFile = File(dbFile.parent, "tang_database-wal")
        val localDbModified = maxOf(
            if (dbFile.exists()) dbFile.lastModified() else 0L,
            if (walFile.exists()) walFile.lastModified() else 0L
        )
        return localDbModified > remoteManifest.dbTimestamp
    }

    /**
     * 计算上传差异
     * @return (需要上传的文件列表, 需要从远端删除的文件名列表)
     */
    private fun computeFileDiff(
        localFiles: List<FileEntry>,
        remoteEntries: Map<String, FileEntry>
    ): Pair<List<FileEntry>, List<String>> {
        val toUpload = mutableListOf<FileEntry>()
        val toDeleteRemote = mutableListOf<String>()

        val localFileNames = localFiles.map { it.name }.toSet()

        // 远端有但本地没有 → 删除远端
        for ((remoteName, _) in remoteEntries) {
            if (remoteName !in localFileNames) {
                toDeleteRemote.add(remoteName)
            }
        }

        // 本地文件与远端对比
        for (localFile in localFiles) {
            val remoteEntry = remoteEntries[localFile.name]
            if (remoteEntry == null) {
                toUpload.add(localFile)
            } else if (localFile.modified > remoteEntry.modified) {
                toUpload.add(localFile)
            }
        }

        return toUpload to toDeleteRemote
    }

    /**
     * 计算下载差异
     * @return (需要下载的文件列表, 需要从本地删除的文件名列表)
     */
    private fun computeDownloadDiff(
        localFiles: List<FileEntry>,
        remoteEntries: Map<String, FileEntry>
    ): Pair<List<FileEntry>, List<String>> {
        val toDownload = mutableListOf<FileEntry>()
        val toDeleteLocal = mutableListOf<String>()

        val localFileMap = localFiles.associateBy { it.name }

        // 远端有但本地没有 → 下载
        for ((remoteName, remoteEntry) in remoteEntries) {
            val localEntry = localFileMap[remoteName]
            if (localEntry == null) {
                toDownload.add(remoteEntry)
            } else if (remoteEntry.modified > localEntry.modified) {
                toDownload.add(remoteEntry)
            }
        }

        // 本地有但远端没有 → 删除本地
        for (localFile in localFiles) {
            if (localFile.name !in remoteEntries) {
                toDeleteLocal.add(localFile.name)
            }
        }

        return toDownload to toDeleteLocal
    }

    // ===== 配置管理 =====

    private fun readConfig(): WebDavConfig {
        val oldPassword = prefs.getString(KEY_PASSWORD, "") ?: ""
        val newPassword = encryptedPrefs.getString(KEY_PASSWORD, "") ?: ""

        val password = if (newPassword.isBlank() && oldPassword.isNotBlank()) {
            val decoded = decodePassword(oldPassword)
            savePassword(decoded)
            prefs.edit().remove(KEY_PASSWORD).apply()
            decoded
        } else {
            newPassword
        }

        return WebDavConfig(
            serverUrl = prefs.getString(KEY_SERVER_URL, "") ?: "",
            username = prefs.getString(KEY_USERNAME, "") ?: "",
            password = password,
            remotePath = prefs.getString(KEY_REMOTE_PATH, "/knots_backup/") ?: "/knots_backup/",
            autoSyncOnLaunch = prefs.getBoolean(KEY_AUTO_SYNC_ON_LAUNCH, false),
            lastSyncTime = prefs.getLong(KEY_LAST_SYNC_TIME, 0),
            lastSyncDirection = prefs.getString(KEY_LAST_SYNC_DIRECTION, "") ?: ""
        )
    }

    private suspend fun saveLastSyncTime(time: Long, direction: String) {
        prefs.edit().apply {
            putLong(KEY_LAST_SYNC_TIME, time)
            putString(KEY_LAST_SYNC_DIRECTION, direction)
            commit()
        }
        configCache.value = readConfig()
    }

    private fun savePassword(password: String) {
        encryptedPrefs.edit().putString(KEY_PASSWORD, password).apply()
    }

    private fun readPassword(): String {
        return encryptedPrefs.getString(KEY_PASSWORD, "") ?: ""
    }

    private fun decodePassword(encoded: String): String {
        if (encoded.isBlank()) return ""
        return try {
            String(Base64.decode(encoded, Base64.NO_WRAP), Charsets.UTF_8)
        } catch (_: Exception) {
            encoded
        }
    }
}
