package com.tang.prm.data.repository

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import com.tang.prm.data.local.database.TangDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipInputStream
import com.tang.prm.domain.model.BackupFileInfo
import com.tang.prm.domain.model.FileEntry
import com.tang.prm.domain.model.BackupImageQuality
import com.tang.prm.domain.model.BackupInfo
import com.tang.prm.domain.model.ClearDataResult
import com.tang.prm.domain.model.RestoreResult
import com.tang.prm.domain.repository.AppRestarter
import com.tang.prm.domain.repository.BackupRepositoryInterface
import com.tang.prm.domain.util.DateUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: TangDatabase,
    private val appRestarter: AppRestarter
) : BackupRepositoryInterface {
    companion object {
        private const val TAG = "BackupRepositoryImpl"
        private const val DB_NAME = "tang_database"
        private const val DATASTORE_NAME = "settings"
        private const val BACKUP_PREFIX = "tang_backup_"
        private const val BACKUP_EXTENSION = ".zip"

        // 备份目录 URI 存储
        private const val PREFS_NAME = "backup_meta"
        private const val KEY_BACKUP_DIR_URI = "backup_dir_uri"
    }

    private val backupPrefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val safHelper by lazy { SafDocumentsHelper(context) }
    private val imageReferenceQuery by lazy {
        ImageReferenceQuery(database.contactDao(), database.giftDao(), database.eventDao())
    }

    // ===== SAF 备份目录管理 =====

    override fun getBackupDirUri(): String? {
        return backupPrefs.getString(KEY_BACKUP_DIR_URI, null)
    }

    override fun setBackupDirUri(uri: String) {
        backupPrefs.edit().putString(KEY_BACKUP_DIR_URI, uri).apply()
        try {
            val takeFlags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(Uri.parse(uri), takeFlags)
        } catch (e: Exception) {
            Log.w(TAG, "获取持久化URI权限失败", e)
        }
    }

    override fun hasBackupDir(): Boolean {
        return getBackupDirUri() != null
    }

    override fun getBackupDirName(): String {
        val uriStr = getBackupDirUri() ?: return ""
        return SafDocumentsHelper.getBackupDirNameFromUri(uriStr)
    }

    override suspend fun backupToDir(imageQuality: BackupImageQuality): BackupInfo {
        val dirUriStr = getBackupDirUri() ?: throw IllegalStateException("未设置备份目录")
        val treeUri = Uri.parse(dirUriStr)
        val docUri = safHelper.toDocumentUri(treeUri)
        val fileName = "$BACKUP_PREFIX${DateUtils.formatBackupTimestamp(System.currentTimeMillis())}$BACKUP_EXTENSION"

        return withContext(Dispatchers.IO) {
            val newFileUri = DocumentsContract.createDocument(
                context.contentResolver, docUri, "application/zip", fileName
            ) ?: throw IllegalStateException("无法创建备份文件")

            val tempFile = File(context.cacheDir, "backup_temp_${System.currentTimeMillis()}.zip")
            try {
                val info = backupToFileInternal(tempFile, imageQuality)
                tempFile.inputStream().use { input ->
                    context.contentResolver.openOutputStream(newFileUri)?.use { output ->
                        input.copyTo(output, BackupZipUtils.BUFFER_SIZE)
                    } ?: throw IllegalStateException("无法写入备份文件")
                }
                info.copy(fileName = fileName)
            } finally {
                tempFile.delete()
            }
        }
    }

    override suspend fun listBackups(): List<BackupFileInfo> {
        val dirUriStr = getBackupDirUri() ?: return emptyList()
        val dirUri = Uri.parse(dirUriStr)
        return withContext(Dispatchers.IO) {
            safHelper.listBackupFiles(dirUri, BACKUP_EXTENSION)
        }
    }

    override suspend fun deleteBackup(fileName: String): Boolean {
        val dirUriStr = getBackupDirUri() ?: return false
        val dirUri = Uri.parse(dirUriStr)

        return withContext(Dispatchers.IO) {
            try {
                val docUri = safHelper.findDocumentUriByName(dirUri, fileName) ?: return@withContext false
                DocumentsContract.deleteDocument(context.contentResolver, docUri)
            } catch (e: Exception) {
                Log.w(TAG, "删除备份文件失败", e)
                false
            }
        }
    }

    override suspend fun restoreBackup(fileName: String): Flow<RestoreResult> = flow {
        val dirUriStr = getBackupDirUri()
        if (dirUriStr == null) {
            emit(RestoreResult.Error("未设置备份目录"))
            return@flow
        }
        val dirUri = Uri.parse(dirUriStr)
        val docUri = withContext(Dispatchers.IO) { safHelper.findDocumentUriByName(dirUri, fileName) }
        if (docUri == null) {
            emit(RestoreResult.Error("备份文件不存在"))
            return@flow
        }

        val result = withContext(Dispatchers.IO) {
            restoreFromUriInternal(docUri)
        }
        emit(result)
        appRestarter.restart()
    }

    override suspend fun computeDataFingerprint(): String = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath(DB_NAME)
        val imagesDir = File(context.filesDir, "app_images")
        val giftPhotosDir = File(context.filesDir, "gift_photos")

        val dbSize = if (dbFile.exists()) dbFile.length() else 0L
        val dbModified = if (dbFile.exists()) dbFile.lastModified() else 0L
        val referencedNames = imageReferenceQuery.queryReferencedImageFileNames()
        val imageCount = imagesDir.listFiles()?.count { it.isFile && it.name in referencedNames } ?: 0
        val giftPhotoCount = giftPhotosDir.listFiles()?.count { it.isFile } ?: 0
        val latestImageModified = imagesDir.listFiles()
            ?.filter { it.isFile && it.name in referencedNames }
            ?.maxOfOrNull { it.lastModified() } ?: 0L
        val latestGiftModified = giftPhotosDir.listFiles()?.maxOfOrNull { it.lastModified() } ?: 0L

        "$dbSize:$dbModified:$imageCount:$giftPhotoCount:$latestImageModified:$latestGiftModified"
    }

    // ===== 增量同步支持 =====

    override fun listLocalImageFiles(dir: String): List<FileEntry> {
        val directory = File(context.filesDir, dir)
        if (!directory.exists() || !directory.isDirectory) return emptyList()
        return directory.listFiles()
            ?.filter { it.isFile }
            ?.map { FileEntry(name = it.name, size = it.length(), modified = it.lastModified(), uploadedAt = 0L) }
            ?: emptyList()
    }

    override fun getLocalImageFile(dir: String, fileName: String): File? {
        val file = File(File(context.filesDir, dir), fileName)
        return if (file.exists() && file.isFile) file else null
    }

    override suspend fun backupDbOnly(file: File): BackupInfo = withContext(Dispatchers.IO) {
        database.checkpoint()

        val dbFile = context.getDatabasePath(DB_NAME)
        val walFile = File(dbFile.parent, "$DB_NAME-wal")
        val shmFile = File(dbFile.parent, "$DB_NAME-shm")
        val datastoreFile = File(File(context.filesDir.parent, "datastore"), "$DATASTORE_NAME.preferences_pb")

        val fileName = "$BACKUP_PREFIX${DateUtils.formatBackupTimestamp(System.currentTimeMillis())}$BACKUP_EXTENSION"

        BackupZipUtils.writeDbOnlyZip(file, dbFile, walFile, shmFile, datastoreFile)

        BackupInfo(fileName = fileName, fileSize = file.length(), timestamp = System.currentTimeMillis())
    }

    override suspend fun restoreDbOnly(uri: String): Flow<RestoreResult> = flow {
        val parsedUri = Uri.parse(uri)
        val result = withContext(Dispatchers.IO) {
            val dbFile = context.getDatabasePath(DB_NAME)
            val backupSnapshot = File(dbFile.parent, "$DB_NAME.before_restore")
            try {
                database.checkpoint()
                database.close()
                if (dbFile.exists()) dbFile.copyTo(backupSnapshot, overwrite = true)
                val walFile = File(dbFile.parent, "$DB_NAME-wal")
                val shmFile = File(dbFile.parent, "$DB_NAME-shm")
                val datastoreDir = File(context.filesDir.parent, "datastore")
                val datastoreFile = File(datastoreDir, "$DATASTORE_NAME.preferences_pb")

                context.contentResolver.openInputStream(parsedUri)?.use { inputStream ->
                    ZipInputStream(inputStream).use { zipIn ->
                        var entry = zipIn.nextEntry
                        while (entry != null) {
                            when {
                                entry.name == BackupZipUtils.ENTRY_DATABASE -> BackupZipUtils.writeFileFromZip(zipIn, dbFile, dbFile.parentFile ?: dbFile)
                                entry.name == BackupZipUtils.ENTRY_WAL -> BackupZipUtils.writeFileFromZip(zipIn, walFile, walFile.parentFile ?: walFile)
                                entry.name == BackupZipUtils.ENTRY_SHM -> BackupZipUtils.writeFileFromZip(zipIn, shmFile, shmFile.parentFile ?: shmFile)
                                entry.name == BackupZipUtils.ENTRY_DATASTORE -> {
                                    datastoreDir.mkdirs()
                                    BackupZipUtils.writeFileFromZip(zipIn, datastoreFile, datastoreDir)
                                }
                            }
                            zipIn.closeEntry()
                            entry = zipIn.nextEntry
                        }
                    }
                } ?: return@withContext RestoreResult.Error("无法打开备份文件")
                RestoreResult.Success
            } catch (e: Exception) {
                val rolledBack = rollbackDatabase(dbFile, backupSnapshot)
                if (rolledBack) {
                    RestoreResult.Error("恢复失败：${e.message}")
                } else {
                    RestoreResult.FatalError(
                        message = "恢复失败且数据库回滚失败，应用可能无法正常启动，建议重新安装",
                        cause = e
                    )
                }
            }
        }
        emit(result)
        appRestarter.restart()
    }

    override fun deleteLocalImageFile(dir: String, fileName: String): Boolean {
        val file = File(File(context.filesDir, dir), fileName)
        return file.exists() && file.delete()
    }

    // ===== 图片引用过滤 =====

    override suspend fun getReferencedImageFileNames(): Set<String> = withContext(Dispatchers.IO) {
        imageReferenceQuery.queryReferencedImageFileNames()
    }

    override suspend fun cleanOrphanedImages(): Int = withContext(Dispatchers.IO) {
        val referencedNames = imageReferenceQuery.queryReferencedImageFileNames()
        val imagesDir = File(context.filesDir, "app_images")
        if (!imagesDir.exists() || !imagesDir.isDirectory) return@withContext 0

        var deletedCount = 0
        imagesDir.listFiles()?.forEach { file ->
            if (file.isFile && file.name !in referencedNames) {
                if (file.delete()) deletedCount++
            }
        }
        deletedCount
    }

    // ===== URI 恢复（从外部文件恢复） =====

    override suspend fun restoreFromUri(uri: String): Flow<RestoreResult> = flow {
        val parsedUri = Uri.parse(uri)
        val result = withContext(Dispatchers.IO) {
            restoreFromUriInternal(parsedUri)
        }
        emit(result)
        appRestarter.restart()
    }

    // ===== 核心备份逻辑 =====

    private suspend fun backupToFileInternal(file: File, imageQuality: BackupImageQuality): BackupInfo = withContext(Dispatchers.IO) {
        database.checkpoint()

        val dbFile = context.getDatabasePath(DB_NAME)
        val walFile = File(dbFile.parent, "$DB_NAME-wal")
        val shmFile = File(dbFile.parent, "$DB_NAME-shm")
        val datastoreFile = File(File(context.filesDir.parent, "datastore"), "$DATASTORE_NAME.preferences_pb")
        val imagesDir = File(context.filesDir, "app_images")
        val giftPhotosDir = File(context.filesDir, "gift_photos")

        val referencedNames = imageReferenceQuery.queryReferencedImageFileNames()

        java.io.FileOutputStream(file).use { outputStream ->
            java.util.zip.ZipOutputStream(outputStream).use { zipOut ->
                zipOut.setLevel(java.util.zip.Deflater.BEST_SPEED)

                BackupZipUtils.addFileToZip(zipOut, BackupZipUtils.ENTRY_DATABASE, dbFile)
                if (walFile.exists()) BackupZipUtils.addFileToZip(zipOut, BackupZipUtils.ENTRY_WAL, walFile)
                if (shmFile.exists()) BackupZipUtils.addFileToZip(zipOut, BackupZipUtils.ENTRY_SHM, shmFile)
                if (datastoreFile.exists()) BackupZipUtils.addFileToZip(zipOut, BackupZipUtils.ENTRY_DATASTORE, datastoreFile)

                if (imagesDir.exists() && imagesDir.isDirectory) {
                    imagesDir.listFiles()?.forEach { f ->
                        if (f.isFile && f.name in referencedNames) {
                            BackupZipUtils.addFileToZip(zipOut, "${BackupZipUtils.ENTRY_IMAGES_DIR}${f.name}", f)
                        }
                    }
                }
                if (giftPhotosDir.exists() && giftPhotosDir.isDirectory) {
                    giftPhotosDir.listFiles()?.forEach { f ->
                        if (f.isFile) BackupZipUtils.addFileToZip(zipOut, "${BackupZipUtils.ENTRY_GIFT_PHOTOS_DIR}${f.name}", f)
                    }
                }
            }
        }

        BackupInfo(fileName = "", fileSize = file.length(), timestamp = System.currentTimeMillis())
    }

    // ===== 恢复逻辑 =====

    private suspend fun restoreFromUriInternal(uri: Uri): RestoreResult {
        val dbFile = context.getDatabasePath(DB_NAME)
        val backupSnapshot = File(dbFile.parent, "$DB_NAME.before_restore")
        try {
            database.checkpoint()
            database.close()
            if (dbFile.exists()) dbFile.copyTo(backupSnapshot, overwrite = true)
            val dirs = getExtractDirs()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BackupZipUtils.extractFromStream(inputStream, dirs)
            } ?: return RestoreResult.Error("无法打开备份文件")
            return RestoreResult.Success
        } catch (e: Exception) {
            val rolledBack = rollbackDatabase(dbFile, backupSnapshot)
            return if (rolledBack) {
                RestoreResult.Error("恢复失败：${e.message}")
            } else {
                RestoreResult.FatalError(
                    message = "恢复失败且数据库回滚失败，应用可能无法正常启动，建议重新安装",
                    cause = e
                )
            }
        }
    }

    private fun getExtractDirs(): BackupZipUtils.ExtractDirs {
        val dbFile = context.getDatabasePath(DB_NAME)
        return BackupZipUtils.ExtractDirs(
            dbFile = dbFile,
            walFile = File(dbFile.parent, "$DB_NAME-wal"),
            shmFile = File(dbFile.parent, "$DB_NAME-shm"),
            datastoreDir = File(context.filesDir.parent, "datastore"),
            datastoreFile = File(File(context.filesDir.parent, "datastore"), "$DATASTORE_NAME.preferences_pb"),
            imagesDir = File(context.filesDir, "app_images"),
            giftPhotosDir = File(context.filesDir, "gift_photos")
        )
    }

    /**
     * 回滚数据库文件：恢复失败时将快照覆盖回数据库文件，并删除快照。
     * @return true 表示回滚成功；false 表示回滚失败，数据库文件可能已损坏。
     */
    private fun rollbackDatabase(dbFile: File, backupSnapshot: File): Boolean {
        if (!backupSnapshot.exists()) return true
        val copied = try {
            backupSnapshot.copyTo(dbFile, overwrite = true); true
        } catch (e: Exception) {
            Log.e(TAG, "回滚数据库文件失败，数据库可能已损坏", e); false
        }
        try { backupSnapshot.delete() } catch (e: Exception) { Log.e(TAG, "删除备份快照失败", e) }
        return copied
    }

    override fun generateBackupFileName(): String {
        return "$BACKUP_PREFIX${DateUtils.formatBackupTimestamp(System.currentTimeMillis())}$BACKUP_EXTENSION"
    }

    override suspend fun clearAllData(): ClearDataResult = withContext(Dispatchers.IO) {
        try {
            database.checkpoint()
            database.close()

            val dbFile = context.getDatabasePath(DB_NAME)
            val walFile = File(dbFile.parent, "$DB_NAME-wal")
            val shmFile = File(dbFile.parent, "$DB_NAME-shm")

            if (dbFile.exists()) dbFile.delete()
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            val datastoreFile = File(File(context.filesDir.parent, "datastore"), "$DATASTORE_NAME.preferences_pb")
            if (datastoreFile.exists()) datastoreFile.delete()

            val imagesDir = File(context.filesDir, "app_images")
            if (imagesDir.exists() && imagesDir.isDirectory) imagesDir.listFiles()?.forEach { it.delete() }

            val giftPhotosDir = File(context.filesDir, "gift_photos")
            if (giftPhotosDir.exists() && giftPhotosDir.isDirectory) giftPhotosDir.listFiles()?.forEach { it.delete() }

            appRestarter.restart()
            ClearDataResult.Success
        } catch (e: Exception) {
            ClearDataResult.Error("清空数据失败：${e.message}")
        }
    }
}
