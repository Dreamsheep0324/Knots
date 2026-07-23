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
import com.tang.prm.domain.repository.BackupRepository
import com.tang.prm.domain.repository.ImageOrphanCleaner
import com.tang.prm.domain.repository.WebDavRepository
import com.tang.prm.domain.util.DateUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebDavRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val webDavClient: WebDavClient,
    // A-9 修复：按需依赖子接口而非聚合接口。本类只用图片文件操作 + DB 备份恢复，
    // 不需要目录管理能力，因此依赖 ImageOrphanCleaner + BackupRepository 两个子接口。
    private val imageOrphanCleaner: ImageOrphanCleaner,
    private val backupRepository: BackupRepository,
    private val configStore: WebDavConfigStore,
    private val fileDiffCalculator: SyncFileDiffCalculator
) : WebDavRepository {

    // 上传阶段数据
    private data class UploadFileScan(
        val referencedNames: Set<String>,
        val localImages: List<FileEntry>,
        val localGiftPhotos: List<FileEntry>
    )

    private data class UploadPlan(
        val imagesToUpload: List<FileEntry>,
        val imagesToDeleteRemote: Set<String>,
        val giftToUpload: List<FileEntry>,
        val giftToDeleteRemote: Set<String>,
        val dbChanged: Boolean,
        val totalSteps: Int
    )

    private data class FileUploadResult(
        val uploadedEntries: Map<String, FileEntry>,
        val failures: Int,
        val currentStep: Int
    )

    // 下载阶段数据
    private data class DownloadFileScan(
        val localImages: List<FileEntry>,
        val localGiftPhotos: List<FileEntry>
    )

    private data class DownloadPlan(
        val imagesToDownload: List<FileEntry>,
        val imagesToDeleteLocal: Set<String>,
        val giftToDownload: List<FileEntry>,
        val giftToDeleteLocal: Set<String>,
        val totalSteps: Int
    )

    private data class FileDownloadResult(
        val failures: Int,
        val currentStep: Int
    )

    // B-2 修复：隔离区模式，下载删除的本地文件先移入隔离区，DB 恢复失败时移回原位
    private data class QuarantinedFile(
        val originalFile: File,
        val quarantineFile: File
    )

    private data class FileQuarantineResult(
        val quarantined: List<QuarantinedFile>,
        val currentStep: Int
    )

    companion object {
        private const val TAG = "WebDavRepository"
        private const val QUARANTINE_PREFIX = "sync_quarantine_"

        // B-13 修复：远端文件名消毒，防止恶意/被篡改服务器通过 `../` 越界读写应用私有目录
        private fun requireSafeEntryName(name: String) {
            require(name.isNotBlank() && !name.contains('/') && !name.contains('\\') && name != "." && name != "..") {
                "非法的远端文件名: $name"
            }
        }
    }

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    // B-7/A-3 修复：仓库层互斥锁，串行化 upload/download 两条流水线。
    // 防止双击上传 / 上传中点下载导致 manifest 读-改-写竞态（后写者覆盖先写者已上传的文件条目）。
    private val syncMutex = Mutex()

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
        // B-7/A-3 修复：仓库层互斥锁，串行化 upload/download 流水线
        syncMutex.lock()
        try {
            emitAll(uploadBackupInternal())
        } finally {
            syncMutex.unlock()
        }
    }

    private fun uploadBackupInternal(): Flow<SyncResult> = flow {
        val config = configStore.readConfig()
        if (config.serverUrl.isBlank()) {
            emit(SyncResult.Error("未配置 WebDAV 服务器"))
            return@flow
        }

        prepareRemoteDirs(config)?.let { emit(it); return@flow }

        emit(SyncResult.UploadProgress("准备中", 0, 0, "读取云端清单..."))
        val remoteManifest = readRemoteManifest()

        // B-3 修复：新设备从未同步过时，禁止空库覆盖云端备份
        if (remoteManifest != null && config.lastSyncTime == 0L) {
            emit(SyncResult.Error("检测到云端已有备份，为避免覆盖请先下载恢复"))
            return@flow
        }

        val scan = scanLocalFilesForUpload()
        val plan = computeUploadPlan(scan, remoteManifest)

        if (plan.totalSteps == 1 && !plan.dbChanged) {
            emit(SyncResult.UploadProgress("完成", 1, 1, "数据已是最新，无需同步"))
            emit(SyncResult.UploadSuccess("", 0, scan.localImages.size + scan.localGiftPhotos.size))
            return@flow
        }

        var currentStep = 0
        var dbFileName = remoteManifest?.dbBackup ?: ""
        if (plan.dbChanged) {
            currentStep++
            emit(SyncResult.UploadProgress("上传数据库", currentStep, plan.totalSteps, "正在上传数据库..."))
            dbFileName = uploadDbIfNeeded(config, remoteManifest)
        }

        val imageResult = uploadFilesWithProgress(
            config, "app_images", "images", plan.imagesToUpload, plan.totalSteps, currentStep,
            progressMessage = "正在上传图片", failureTag = "图片上传失败"
        )
        currentStep = imageResult.currentStep

        val giftResult = uploadFilesWithProgress(
            config, "gift_photos", "gift_photos", plan.giftToUpload, plan.totalSteps, currentStep,
            progressMessage = "正在上传礼物照片", failureTag = "礼物照片上传失败"
        )
        currentStep = giftResult.currentStep

        currentStep = cleanRemoteFilesWithProgress(config, plan.imagesToDeleteRemote, plan.giftToDeleteRemote, plan.totalSteps, currentStep)

        currentStep++
        emit(SyncResult.UploadProgress("上传清单", currentStep, plan.totalSteps, "正在更新同步清单..."))
        val now = System.currentTimeMillis()
        val newManifest = buildUpdatedManifest(
            remoteManifest, dbFileName,
            imageResult.uploadedEntries, giftResult.uploadedEntries,
            plan.imagesToDeleteRemote, plan.giftToDeleteRemote, now
        )
        writeRemoteManifest(newManifest)

        // B-1 修复：manifest 写入成功后才删除旧 DB，避免中途失败导致 manifest 指向已删文件。
        // 原实现是"先删旧 DB → 后写 manifest"，manifest 写入失败会让云端清单指向已删除的 DB。
        // 现改为"先写 manifest → 后删旧 DB"：manifest 失败时云端清单仍指向旧 DB（仍可下载），
        // 新上传的 DB 文件成为孤儿（下次同步可被清理，风险可控）。
        val oldDbFileName = remoteManifest?.dbBackup
        if (plan.dbChanged && !oldDbFileName.isNullOrBlank() && oldDbFileName != dbFileName) {
            try {
                webDavClient.deleteRemoteFile(config, "db", oldDbFileName)
            } catch (e: Exception) {
                Log.w(TAG, "删除远端旧数据库文件失败（不影响同步结果，下次同步可清理）", e)
            }
        }

        configStore.saveLastSyncTime(now, "upload")
        val uploadFailures = imageResult.failures + giftResult.failures
        val totalUploaded = plan.imagesToUpload.size + plan.giftToUpload.size - uploadFailures
        val totalSkipped = (scan.localImages.size + scan.localGiftPhotos.size) - plan.imagesToUpload.size - plan.giftToUpload.size

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
        // B-7/A-3 修复：仓库层互斥锁，串行化 upload/download 流水线
        syncMutex.lock()
        try {
            emitAll(downloadBackupInternal(fileName))
        } finally {
            syncMutex.unlock()
        }
    }

    private fun downloadBackupInternal(fileName: String): Flow<SyncResult> = flow {
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
            // B-6 修复：listRemoteBackups 降级路径从根目录列出文件，下载也必须从根目录取
            emit(SyncResult.DownloadProgress("下载中", 0, 1, "正在下载全量备份..."))
            val tempFile = File(context.cacheDir, "webdav_download_${System.currentTimeMillis()}.zip")
            try {
                webDavClient.downloadLegacyFile(config, fileName, tempFile)
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

        val scan = scanLocalFilesForDownload()
        val plan = computeDownloadPlan(scan, remoteManifest)

        // B-2 修复：下载开始前清理上次同步遗留的隔离区目录（来自上次成功的恢复，或崩溃的恢复）
        cleanupStaleQuarantine()

        // 4. 先下载图片（数据库恢复会重启进程，必须放在最后）
        var currentStep = 0
        val imageResult = downloadFilesWithProgress(
            config, "app_images", "images", plan.imagesToDownload, plan.totalSteps, currentStep,
            progressMessage = "正在下载图片", failureTag = "图片下载失败"
        )
        currentStep = imageResult.currentStep

        val giftResult = downloadFilesWithProgress(
            config, "gift_photos", "gift_photos", plan.giftToDownload, plan.totalSteps, currentStep,
            progressMessage = "正在下载礼物照片", failureTag = "礼物照片下载失败"
        )
        currentStep = giftResult.currentStep

        // 5. 清理本地多余图片：B-2 修复——改用隔离区模式而非直接删除。
        // 原实现直接 delete 本地文件，若随后 DB 恢复失败并回滚到旧库，旧库引用的图片已被物理删除，
        // 导致图片引用永久悬空且无法通过再次同步找回（远端 manifest 也没有这些文件）。
        // 现改为移入 cacheDir/sync_quarantine_*：恢复成功 → 进程重启后由系统清理 cache；
        // 恢复失败 → 移回原位，引用无损。
        val quarantineResult = cleanLocalFilesWithProgress(
            plan.imagesToDeleteLocal, plan.giftToDeleteLocal, plan.totalSteps, currentStep
        )
        currentStep = quarantineResult.currentStep

        // 6. 最后恢复数据库（会重启进程，必须放在所有操作之后）
        val dbChanged = true // 恢复时总是下载最新数据库
        if (dbChanged && remoteManifest.dbBackup.isNotBlank()) {
            val success = restoreDbWithProgress(config, remoteManifest, plan.totalSteps, currentStep)
            if (!success) {
                // B-2 修复：DB 恢复失败，将隔离区文件移回原位，避免旧库引用悬空
                restoreQuarantinedFiles(quarantineResult.quarantined)
                return@flow
            }
        }

        // 7. 保存同步时间并报告结果
        //    注意：如果数据库恢复成功，restartApp() 已杀死进程，以下代码不会执行。
        //    但如果 dbChanged=false（不需要恢复数据库），则需要在这里报告结果。
        configStore.saveLastSyncTime(System.currentTimeMillis(), "download")
        val downloadFailures = imageResult.failures + giftResult.failures
        val totalDownloaded = plan.imagesToDownload.size + plan.giftToDownload.size - downloadFailures
        val totalSkipped = (scan.localImages.size + scan.localGiftPhotos.size) - plan.imagesToDownload.size - plan.giftToDownload.size

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

    // ===== 上传辅助方法 =====

    private suspend fun prepareRemoteDirs(config: WebDavConfig): SyncResult.Error? {
        return try {
            webDavClient.ensureRemoteDir(config)
            webDavClient.ensureRemoteSubDir(config, "db")
            webDavClient.ensureRemoteSubDir(config, "images")
            webDavClient.ensureRemoteSubDir(config, "gift_photos")
            null
        } catch (e: Exception) {
            SyncResult.Error("创建远程目录失败：${e.message}")
        }
    }

    private suspend fun scanLocalFilesForUpload(): UploadFileScan {
        val referencedNames = imageOrphanCleaner.getReferencedImageFileNames()
        val localImages = imageOrphanCleaner.listLocalImageFiles("app_images")
            .filter { it.name in referencedNames }
        val localGiftPhotos = imageOrphanCleaner.listLocalImageFiles("gift_photos")
        return UploadFileScan(referencedNames, localImages, localGiftPhotos)
    }

    private fun computeUploadPlan(
        scan: UploadFileScan,
        remoteManifest: SyncManifest?
    ): UploadPlan {
        val (imagesToUpload, imagesToDeleteRemote) = fileDiffCalculator.computeUploadDiff(
            scan.localImages, remoteManifest?.images ?: emptyMap()
        )
        val (giftToUpload, giftToDeleteRemote) = fileDiffCalculator.computeUploadDiff(
            scan.localGiftPhotos, remoteManifest?.giftPhotos ?: emptyMap()
        )
        val dbChanged = isDbChanged(remoteManifest)
        val totalSteps = (if (dbChanged) 1 else 0) + imagesToUpload.size + giftToUpload.size +
                         imagesToDeleteRemote.size + giftToDeleteRemote.size + 1
        return UploadPlan(
            imagesToUpload,
            imagesToDeleteRemote.toSet(),
            giftToUpload,
            giftToDeleteRemote.toSet(),
            dbChanged,
            totalSteps
        )
    }

    private suspend fun uploadDbIfNeeded(
        config: WebDavConfig,
        remoteManifest: SyncManifest?
    ): String {
        var dbFileName = remoteManifest?.dbBackup ?: ""
        val tempFile = File(context.cacheDir, "webdav_db_${System.currentTimeMillis()}.zip")
        try {
            val info = backupRepository.backupDbOnly(tempFile)
            webDavClient.uploadDbFile(config, info.fileName, tempFile)
            dbFileName = info.fileName
        } finally {
            withContext(Dispatchers.IO) { tempFile.delete() }
        }
        return dbFileName
    }

    private fun buildUpdatedManifest(
        remoteManifest: SyncManifest?,
        dbFileName: String,
        uploadedImageEntries: Map<String, FileEntry>,
        uploadedGiftEntries: Map<String, FileEntry>,
        imagesToDeleteRemote: Set<String>,
        giftToDeleteRemote: Set<String>,
        now: Long
    ): SyncManifest {
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
        return SyncManifest(
            version = 1,
            lastSyncTime = now,
            dbBackup = dbFileName,
            dbTimestamp = now,
            dbFingerprint = computeDbFingerprint(),
            images = imageEntries,
            giftPhotos = giftEntries
        )
    }

    /**
     * 统一的文件上传方法，消除 images / gift_photos 两套逐行复制的孪生代码。
     * 仅 localSubDir / remoteSubDir 与进度文案不同，行为完全一致。
     */
    private suspend fun FlowCollector<SyncResult>.uploadFilesWithProgress(
        config: WebDavConfig,
        localSubDir: String,
        remoteSubDir: String,
        filesToUpload: List<FileEntry>,
        totalSteps: Int,
        startStep: Int,
        progressMessage: String,
        failureTag: String
    ): FileUploadResult {
        var currentStep = startStep
        var failures = 0
        val uploaded = mutableMapOf<String, FileEntry>()
        val startTime = System.currentTimeMillis()
        for ((index, entry) in filesToUpload.withIndex()) {
            currentStep++
            emit(SyncResult.UploadProgress("上传图片", currentStep, totalSteps,
                "$progressMessage ${index + 1}/${filesToUpload.size}"))
            val file = imageOrphanCleaner.getLocalImageFile(localSubDir, entry.name) ?: continue
            try {
                webDavClient.uploadImageFile(config, remoteSubDir, entry.name, file)
                uploaded[entry.name] = entry.copy(uploadedAt = startTime)
            } catch (e: Exception) {
                Log.w(TAG, "$failureTag: ${entry.name}", e)
                failures++
            }
        }
        return FileUploadResult(uploaded, failures, currentStep)
    }

    private suspend fun FlowCollector<SyncResult>.cleanRemoteFilesWithProgress(
        config: WebDavConfig,
        imagesToDelete: Set<String>,
        giftToDelete: Set<String>,
        totalSteps: Int,
        startStep: Int
    ): Int {
        // images / gift_photos 两个目录的清理逻辑相同，仅 remoteSubDir 不同，统一委托给 cleanRemoteDirWithProgress
        var currentStep = cleanRemoteDirWithProgress(config, "images", imagesToDelete, totalSteps, startStep)
        currentStep = cleanRemoteDirWithProgress(config, "gift_photos", giftToDelete, totalSteps, currentStep)
        return currentStep
    }

    private suspend fun FlowCollector<SyncResult>.cleanRemoteDirWithProgress(
        config: WebDavConfig,
        remoteSubDir: String,
        names: Set<String>,
        totalSteps: Int,
        startStep: Int
    ): Int {
        var currentStep = startStep
        for (name in names) {
            currentStep++
            emit(SyncResult.UploadProgress("清理", currentStep, totalSteps, "正在清理远端文件..."))
            webDavClient.deleteRemoteFile(config, remoteSubDir, name)
        }
        return currentStep
    }

    // ===== 下载辅助方法 =====

    private suspend fun scanLocalFilesForDownload(): DownloadFileScan {
        val referencedNames = imageOrphanCleaner.getReferencedImageFileNames()
        val localImages = imageOrphanCleaner.listLocalImageFiles("app_images")
            .filter { it.name in referencedNames }
        val localGiftPhotos = imageOrphanCleaner.listLocalImageFiles("gift_photos")
        return DownloadFileScan(localImages, localGiftPhotos)
    }

    private fun computeDownloadPlan(
        scan: DownloadFileScan,
        remoteManifest: SyncManifest
    ): DownloadPlan {
        val (imagesToDownload, imagesToDeleteLocal) = fileDiffCalculator.computeDownloadDiff(
            scan.localImages, remoteManifest.images
        )
        val (giftToDownload, giftToDeleteLocal) = fileDiffCalculator.computeDownloadDiff(
            scan.localGiftPhotos, remoteManifest.giftPhotos
        )
        // dbChanged = true: 恢复时总是下载最新数据库，totalSteps 包含数据库恢复步骤
        val totalSteps = imagesToDownload.size + giftToDownload.size +
                         imagesToDeleteLocal.size + giftToDeleteLocal.size + 1
        return DownloadPlan(
            imagesToDownload,
            imagesToDeleteLocal.toSet(),
            giftToDownload,
            giftToDeleteLocal.toSet(),
            totalSteps
        )
    }

    /**
     * 统一的文件下载方法，消除 images / gift_photos 两套逐行复制的孪生代码。
     * 仅 localSubDir / remoteSubDir 与进度文案不同，行为完全一致；B-13（文件名消毒）/ B-9（mtime 对齐）保持原状。
     */
    private suspend fun FlowCollector<SyncResult>.downloadFilesWithProgress(
        config: WebDavConfig,
        localSubDir: String,
        remoteSubDir: String,
        filesToDownload: List<FileEntry>,
        totalSteps: Int,
        startStep: Int,
        progressMessage: String,
        failureTag: String
    ): FileDownloadResult {
        var currentStep = startStep
        var failures = 0
        for ((index, entry) in filesToDownload.withIndex()) {
            currentStep++
            emit(SyncResult.DownloadProgress("下载图片", currentStep, totalSteps,
                "$progressMessage ${index + 1}/${filesToDownload.size}"))
            // B-13 修复：远端文件名消毒，防止 `../` 越界
            requireSafeEntryName(entry.name)
            val targetFile = File(File(context.filesDir, localSubDir), entry.name)
            try {
                webDavClient.downloadImageFile(config, remoteSubDir, entry.name, targetFile)
                // B-9 修复：对齐远端 mtime，避免跨设备首次同步后所有图片被判定"需上传"而全量重传
                if (entry.modified > 0) targetFile.setLastModified(entry.modified)
            } catch (e: Exception) {
                Log.w(TAG, "$failureTag: ${entry.name}", e)
                failures++
            }
        }
        return FileDownloadResult(failures, currentStep)
    }

    private suspend fun FlowCollector<SyncResult>.cleanLocalFilesWithProgress(
        imagesToDelete: Set<String>,
        giftToDelete: Set<String>,
        totalSteps: Int,
        startStep: Int
    ): FileQuarantineResult {
        var currentStep = startStep
        val quarantined = mutableListOf<QuarantinedFile>()
        val quarantineDir = File(context.cacheDir, "$QUARANTINE_PREFIX${System.currentTimeMillis()}")
        quarantineDir.mkdirs()

        // B-2 修复：隔离区模式——images / gift_photos 两个目录逻辑相同，
        // 仅 localSubDir / quarantinePrefix / 文案不同，统一委托给 quarantineLocalDirWithProgress
        currentStep = quarantineLocalDirWithProgress(
            "app_images", "images", "图片",
            imagesToDelete, quarantineDir, quarantined, totalSteps, currentStep
        )
        currentStep = quarantineLocalDirWithProgress(
            "gift_photos", "gift", "礼物照片",
            giftToDelete, quarantineDir, quarantined, totalSteps, currentStep
        )
        return FileQuarantineResult(quarantined, currentStep)
    }

    private suspend fun FlowCollector<SyncResult>.quarantineLocalDirWithProgress(
        localSubDir: String,
        quarantinePrefix: String,
        logName: String,
        names: Set<String>,
        quarantineDir: File,
        quarantined: MutableList<QuarantinedFile>,
        totalSteps: Int,
        startStep: Int
    ): Int {
        var currentStep = startStep
        for (name in names) {
            currentStep++
            emit(SyncResult.DownloadProgress("清理", currentStep, totalSteps, "正在清理本地文件..."))
            val source = File(File(context.filesDir, localSubDir), name)
            if (source.exists()) {
                val target = File(quarantineDir, "${quarantinePrefix}_${name.takeLast(64).replace('/', '_')}")
                if (source.renameTo(target)) {
                    quarantined.add(QuarantinedFile(source, target))
                } else {
                    Log.w(TAG, "隔离${logName}失败（rename 失败）: $name")
                }
            }
        }
        return currentStep
    }

    // B-2 修复：DB 恢复失败时将隔离区文件移回原位
    private fun restoreQuarantinedFiles(quarantined: List<QuarantinedFile>) {
        for (q in quarantined) {
            try {
                if (q.quarantineFile.exists()) {
                    q.originalFile.parentFile?.mkdirs()
                    if (!q.quarantineFile.renameTo(q.originalFile)) {
                        // rename 失败（跨挂载点等），回退到 copy
                        q.quarantineFile.copyTo(q.originalFile, overwrite = true)
                        q.quarantineFile.delete()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "恢复隔离文件失败: ${q.originalFile.name}", e)
            }
        }
    }

    // B-2 修复：清理上次同步遗留的隔离区目录
    // 上次恢复成功 → 进程被杀，隔离区文件已无引用，可直接清理
    // 上次恢复失败 → 文件已由 restoreQuarantinedFiles 移回，隔离区为空或残留，可安全清理
    private fun cleanupStaleQuarantine() {
        try {
            context.cacheDir.listFiles()
                ?.filter { it.isDirectory && it.name.startsWith(QUARANTINE_PREFIX) }
                ?.forEach { dir ->
                    dir.listFiles()?.forEach { it.delete() }
                    dir.delete()
                }
        } catch (e: Exception) {
            Log.w(TAG, "清理遗留隔离区失败", e)
        }
    }

    private suspend fun FlowCollector<SyncResult>.restoreDbWithProgress(
        config: WebDavConfig,
        remoteManifest: SyncManifest,
        totalSteps: Int,
        startStep: Int
    ): Boolean {
        var currentStep = startStep
        currentStep++
        emit(SyncResult.DownloadProgress("恢复数据库", currentStep, totalSteps, "正在恢复数据库..."))
        val tempFile = File(context.cacheDir, "webdav_db_${System.currentTimeMillis()}.zip")
        return try {
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
            !dbRestoreFailed
        } finally {
            withContext(Dispatchers.IO) { tempFile.delete() }
        }
    }

    // ===== 增量同步核心方法 =====

    private suspend fun readRemoteManifest(): SyncManifest? {
        return try {
            val config = configStore.readConfig()
            val content = webDavClient.downloadSmallFile(config, "manifest.json")
            if (content != null) {
                json.decodeFromString<SyncManifest>(content)
            } else null
        } catch (e: kotlinx.coroutines.CancellationException) {
            // B-16 修复：取消信号必须向上传播
            throw e
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

    /**
     * 判断本地数据库是否与远端不同。
     *
     * B-4 修复：改用内容指纹（dbSize + dbModified）替代单纯的 mtime 比较。
     * 原实现仅比较 mtime，存在两类问题：
     * 1. 文件恢复/拷贝后 mtime 被重置 → 误判为"未变化"导致跳过上传
     * 2. WAL checkpoint 更新 mtime → 误判为"已变化"导致无意义的重复上传
     *
     * 内容指纹组合 size（内容信号）+ modified（辅助信号），
     * 比对方式从 "本地是否更新" 改为 "本地是否与远端一致"，更健壮。
     */
    private fun isDbChanged(remoteManifest: SyncManifest?): Boolean {
        if (remoteManifest == null) return true
        if (remoteManifest.dbFingerprint.isBlank()) return true // 旧版 manifest 兼容
        return computeDbFingerprint() != remoteManifest.dbFingerprint
    }

    private fun computeDbFingerprint(): String {
        val dbFile = context.getDatabasePath(TangDatabase.DB_NAME)
        val walFile = File(dbFile.parent, "${TangDatabase.DB_NAME}-wal")
        val dbSize = if (dbFile.exists()) dbFile.length() else 0L
        val dbModified = maxOf(
            if (dbFile.exists()) dbFile.lastModified() else 0L,
            if (walFile.exists()) walFile.lastModified() else 0L
        )
        return "$dbSize:$dbModified"
    }
}
