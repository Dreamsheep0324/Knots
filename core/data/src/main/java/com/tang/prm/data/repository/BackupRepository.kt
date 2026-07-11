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
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import com.tang.prm.domain.model.BackupFileInfo
import com.tang.prm.domain.model.FileEntry
import com.tang.prm.domain.model.BackupImageQuality
import com.tang.prm.domain.model.BackupInfo
import com.tang.prm.domain.model.BackupResult
import com.tang.prm.domain.model.ClearDataResult
import com.tang.prm.domain.model.RestoreResult
import com.tang.prm.domain.repository.BackupRepositoryInterface
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: TangDatabase
) : BackupRepositoryInterface {
    companion object {
        private const val TAG = "BackupRepository"
        private const val DB_NAME = "tang_database"
        private const val DATASTORE_NAME = "settings"
        private const val BACKUP_PREFIX = "tang_backup_"
        private const val BACKUP_EXTENSION = ".zip"
        private const val ENTRY_DATABASE = "tang_database.db"
        private const val ENTRY_DATASTORE = "settings.preferences_pb"
        private const val ENTRY_WAL = "tang_database.db-wal"
        private const val ENTRY_SHM = "tang_database.db-shm"
        private const val ENTRY_IMAGES_DIR = "app_images/"
        private const val ENTRY_GIFT_PHOTOS_DIR = "gift_photos/"

        private const val BUFFER_SIZE = 256 * 1024 // 256KB

        // 备份目录 URI 存储
        private const val PREFS_NAME = "backup_meta"
        private const val KEY_BACKUP_DIR_URI = "backup_dir_uri"
    }

    private val dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.getDefault())

    private val backupPrefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val contactDao by lazy { database.contactDao() }
    private val giftDao by lazy { database.giftDao() }
    private val eventDao by lazy { database.eventDao() }

    // ===== SAF 备份目录管理 =====

    override fun getBackupDirUri(): String? {
        return backupPrefs.getString(KEY_BACKUP_DIR_URI, null)
    }

    override fun setBackupDirUri(uri: String) {
        backupPrefs.edit().putString(KEY_BACKUP_DIR_URI, uri).apply()
        // 持久化 URI 权限
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
        val uri = Uri.parse(uriStr)
        // 从 tree URI 中提取路径，如 primary:Download/jieshen → Download/jieshen
        val docId = DocumentsContract.getTreeDocumentId(uri)
        return when {
            docId.startsWith("primary:") -> docId.removePrefix("primary:")
            docId.startsWith("home:") -> docId.removePrefix("home:")
            else -> docId
        }
    }

    // 将 tree URI 转换为 document URI（SAF 操作需要 document URI）
    private fun toDocumentUri(treeUri: Uri): Uri {
        val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
        return DocumentsContract.buildDocumentUriUsingTree(treeUri, treeDocId)
    }

    override suspend fun backupToDir(imageQuality: BackupImageQuality): BackupInfo {
        val dirUriStr = getBackupDirUri() ?: throw IllegalStateException("未设置备份目录")
        val treeUri = Uri.parse(dirUriStr)
        val docUri = toDocumentUri(treeUri)
        val fileName = "$BACKUP_PREFIX${dateFormat.format(LocalDateTime.now())}$BACKUP_EXTENSION"

        return withContext(Dispatchers.IO) {
            // 在 SAF 目录中创建文件
            val newFileUri = DocumentsContract.createDocument(
                context.contentResolver, docUri, "application/zip", fileName
            ) ?: throw IllegalStateException("无法创建备份文件")

            val tempFile = File(context.cacheDir, "backup_temp_${System.currentTimeMillis()}.zip")
            try {
                // 先备份到临时文件
                val info = backupToFileInternal(tempFile, imageQuality)
                // 复制到 SAF 目录
                tempFile.inputStream().use { input ->
                    context.contentResolver.openOutputStream(newFileUri)?.use { output ->
                        input.copyTo(output, BUFFER_SIZE)
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
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                dirUri, DocumentsContract.getTreeDocumentId(dirUri)
            )
            val files = mutableListOf<BackupFileInfo>()

            try {
                context.contentResolver.query(
                    childrenUri,
                    arrayOf(
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_SIZE,
                        DocumentsContract.Document.COLUMN_LAST_MODIFIED
                    ),
                    null, null, "${DocumentsContract.Document.COLUMN_LAST_MODIFIED} DESC"
                )?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val nameCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val sizeCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
                    val modCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

                    while (cursor.moveToNext()) {
                        val name = cursor.getString(nameCol)
                        if (name != null && name.endsWith(BACKUP_EXTENSION)) {
                            files.add(BackupFileInfo(
                                fileName = name,
                                fileSize = if (cursor.isNull(sizeCol)) 0L else cursor.getLong(sizeCol),
                                timestamp = if (cursor.isNull(modCol)) 0L else cursor.getLong(modCol)
                            ))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "查询备份文件列表失败", e)
            }

            files.sortedByDescending { it.timestamp }
        }
    }

    override suspend fun deleteBackup(fileName: String): Boolean {
        val dirUriStr = getBackupDirUri() ?: return false
        val dirUri = Uri.parse(dirUriStr)

        return withContext(Dispatchers.IO) {
            try {
                val docUri = findDocumentUriByName(dirUri, fileName) ?: return@withContext false
                DocumentsContract.deleteDocument(context.contentResolver, docUri)
            } catch (_: Exception) {
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
        val docUri = withContext(Dispatchers.IO) { findDocumentUriByName(dirUri, fileName) }
        if (docUri == null) {
            emit(RestoreResult.Error("备份文件不存在"))
            return@flow
        }

        val result = withContext(Dispatchers.IO) {
            val dbFile = context.getDatabasePath(DB_NAME)
            val backupSnapshot = File(dbFile.parent, "$DB_NAME.before_restore")
            try {
                // 恢复前先 checkpoint 并关闭数据库，确保 WAL 内容写入主文件，
                // 避免覆盖后旧 WAL 残留导致数据不一致
                database.checkpoint()
                database.close()
                // 备份当前数据库文件，用于失败时回滚
                if (dbFile.exists()) dbFile.copyTo(backupSnapshot, overwrite = true)
                val extractDirs = getExtractDirs()
                context.contentResolver.openInputStream(docUri)?.use { inputStream ->
                    extractFromStream(inputStream, extractDirs)
                } ?: return@withContext RestoreResult.Error("无法打开备份文件")
                RestoreResult.Success
            } catch (e: Exception) {
                // 回滚数据库文件
                if (backupSnapshot.exists()) {
                    try { backupSnapshot.copyTo(dbFile, overwrite = true) } catch (_: Exception) {}
                    try { backupSnapshot.delete() } catch (_: Exception) {}
                }
                RestoreResult.Error("恢复失败：${e.message}")
            }
        }
        // 先通知 UI 结果，再重启进程（数据库已关闭，必须重启）
        emit(result)
        restartApp()
    }

    override suspend fun computeDataFingerprint(): String = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath(DB_NAME)
        val imagesDir = File(context.filesDir, "app_images")
        val giftPhotosDir = File(context.filesDir, "gift_photos")

        val dbSize = if (dbFile.exists()) dbFile.length() else 0L
        val dbModified = if (dbFile.exists()) dbFile.lastModified() else 0L
        // 只统计数据库引用的图片数量，避免孤立文件影响指纹
        val referencedNames = queryReferencedImageFileNames()
        val imageCount = imagesDir.listFiles()?.count { it.isFile && it.name in referencedNames } ?: 0
        val giftPhotoCount = giftPhotosDir.listFiles()?.count { it.isFile } ?: 0
        val latestImageModified = imagesDir.listFiles()
            ?.filter { it.isFile && it.name in referencedNames }
            ?.maxOfOrNull { it.lastModified() } ?: 0L
        val latestGiftModified = giftPhotosDir.listFiles()?.maxOfOrNull { it.lastModified() } ?: 0L

        "$dbSize:$dbModified:$imageCount:$giftPhotoCount:$latestImageModified:$latestGiftModified"
    }

    private fun findDocumentUriByName(treeUri: Uri, fileName: String): Uri? {
        val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocId)
        try {
            context.contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                ),
                null, null, null
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameCol) == fileName) {
                        val docId = cursor.getString(idCol)
                        return DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "查找文档URI失败: $fileName", e)
        }
        return null
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
        val datastoreDir = File(context.filesDir.parent, "datastore")
        val datastoreFile = File(datastoreDir, "$DATASTORE_NAME.preferences_pb")

        val fileName = "$BACKUP_PREFIX${dateFormat.format(LocalDateTime.now())}$BACKUP_EXTENSION"

        FileOutputStream(file).use { outputStream ->
            ZipOutputStream(outputStream).use { zipOut ->
                zipOut.setLevel(Deflater.BEST_SPEED)
                addFileToZip(zipOut, ENTRY_DATABASE, dbFile)
                if (walFile.exists()) addFileToZip(zipOut, ENTRY_WAL, walFile)
                if (shmFile.exists()) addFileToZip(zipOut, ENTRY_SHM, shmFile)
                if (datastoreFile.exists()) addFileToZip(zipOut, ENTRY_DATASTORE, datastoreFile)
            }
        }

        BackupInfo(fileName = fileName, fileSize = file.length(), timestamp = System.currentTimeMillis())
    }

    override suspend fun restoreDbOnly(uri: String): Flow<RestoreResult> = flow {
        val parsedUri = Uri.parse(uri)
        val result = withContext(Dispatchers.IO) {
            val dbFile = context.getDatabasePath(DB_NAME)
            val backupSnapshot = File(dbFile.parent, "$DB_NAME.before_restore")
            try {
                // 恢复前先 checkpoint 并关闭数据库，确保 WAL 内容写入主文件
                database.checkpoint()
                database.close()
                // 备份当前数据库文件，用于失败时回滚
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
                                entry.name == ENTRY_DATABASE -> writeFileFromZip(zipIn, dbFile, dbFile.parentFile ?: dbFile)
                                entry.name == ENTRY_WAL -> writeFileFromZip(zipIn, walFile, walFile.parentFile ?: walFile)
                                entry.name == ENTRY_SHM -> writeFileFromZip(zipIn, shmFile, shmFile.parentFile ?: shmFile)
                                entry.name == ENTRY_DATASTORE -> {
                                    datastoreDir.mkdirs()
                                    writeFileFromZip(zipIn, datastoreFile, datastoreDir)
                                }
                            }
                            zipIn.closeEntry()
                            entry = zipIn.nextEntry
                        }
                    }
                } ?: return@withContext RestoreResult.Error("无法打开备份文件")
                RestoreResult.Success
            } catch (e: Exception) {
                // 回滚数据库文件
                if (backupSnapshot.exists()) {
                    try { backupSnapshot.copyTo(dbFile, overwrite = true) } catch (_: Exception) {}
                    try { backupSnapshot.delete() } catch (_: Exception) {}
                }
                RestoreResult.Error("恢复失败：${e.message}")
            }
        }
        // 先通知 UI 结果，再重启进程（数据库已关闭，必须重启）
        emit(result)
        restartApp()
    }

    override fun deleteLocalImageFile(dir: String, fileName: String): Boolean {
        val file = File(File(context.filesDir, dir), fileName)
        return file.exists() && file.delete()
    }

    // ===== 图片引用过滤 =====

    override suspend fun getReferencedImageFileNames(): Set<String> = withContext(Dispatchers.IO) {
        queryReferencedImageFileNames()
    }

    override suspend fun cleanOrphanedImages(): Int = withContext(Dispatchers.IO) {
        val referencedNames = queryReferencedImageFileNames()
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

    private fun extractImageFileName(path: String?): String? {
        if (path.isNullOrBlank()) return null
        val filePath = if (path.startsWith("file://")) path.removePrefix("file://") else path
        // 只处理 app_images 目录下的文件
        if (!filePath.contains("app_images")) return null
        return File(filePath).name
    }

    private fun parsePhotoPaths(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = org.json.JSONArray(json)
            (0 until arr.length()).mapNotNull { arr.optString(it) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    /** 统一查询数据库中引用的图片文件名，消除 getReferencedImageFileNames / getReferencedImageFilesSync 重复 */
    private suspend fun queryReferencedImageFileNames(): Set<String> {
        val referencedNames = mutableSetOf<String>()

        contactDao.getReferencedAvatarPaths().forEach { path ->
            extractImageFileName(path)?.let { referencedNames.add(it) }
        }

        giftDao.getReferencedPhotoPaths().forEach { json ->
            parsePhotoPaths(json).forEach { path ->
                extractImageFileName(path)?.let { referencedNames.add(it) }
            }
        }

        eventDao.getReferencedPhotoPaths().forEach { json ->
            parsePhotoPaths(json).forEach { path ->
                extractImageFileName(path)?.let { referencedNames.add(it) }
            }
        }

        return referencedNames
    }

    // ===== URI 备份（保留兼容：从外部文件恢复） =====

    override suspend fun backupToUri(uri: String, imageQuality: BackupImageQuality): Flow<BackupResult> = flow {
        val parsedUri = Uri.parse(uri)
        val tempFile = File(context.cacheDir, "backup_temp_${System.currentTimeMillis()}.zip")
        try {
            val info = backupToFileInternal(tempFile, imageQuality)
            withContext(Dispatchers.IO) {
                tempFile.inputStream().use { input ->
                    context.contentResolver.openOutputStream(parsedUri)?.use { output ->
                        input.copyTo(output, BUFFER_SIZE)
                    } ?: throw IllegalStateException("无法打开输出流")
                }
            }
            emit(BackupResult.Success(info))
        } catch (e: Exception) {
            emit(BackupResult.Error("备份失败：${e.message}"))
        } finally {
            tempFile.delete()
        }
    }

    override suspend fun restoreFromUri(uri: String): Flow<RestoreResult> = flow {
        val parsedUri = Uri.parse(uri)
        val result = withContext(Dispatchers.IO) {
            val dbFile = context.getDatabasePath(DB_NAME)
            val backupSnapshot = File(dbFile.parent, "$DB_NAME.before_restore")
            try {
                // 恢复前先 checkpoint 并关闭数据库，确保 WAL 内容写入主文件
                database.checkpoint()
                database.close()
                // 备份当前数据库文件，用于失败时回滚
                if (dbFile.exists()) dbFile.copyTo(backupSnapshot, overwrite = true)
                val extractDirs = getExtractDirs()
                context.contentResolver.openInputStream(parsedUri)?.use { inputStream ->
                    extractFromStream(inputStream, extractDirs)
                } ?: return@withContext RestoreResult.Error("无法打开备份文件")
                RestoreResult.Success
            } catch (e: Exception) {
                // 回滚数据库文件
                if (backupSnapshot.exists()) {
                    try { backupSnapshot.copyTo(dbFile, overwrite = true) } catch (_: Exception) {}
                    try { backupSnapshot.delete() } catch (_: Exception) {}
                }
                RestoreResult.Error("恢复失败：${e.message}")
            }
        }
        // 先通知 UI 结果，再重启进程（数据库已关闭，必须重启）
        emit(result)
        restartApp()
    }

    // ===== 核心备份逻辑 =====

    override suspend fun backupToFile(file: File, imageQuality: BackupImageQuality): BackupInfo {
        val fileName = "$BACKUP_PREFIX${dateFormat.format(LocalDateTime.now())}$BACKUP_EXTENSION"
        return backupToFileInternal(file, imageQuality).copy(fileName = fileName)
    }

    private suspend fun backupToFileInternal(file: File, imageQuality: BackupImageQuality): BackupInfo = withContext(Dispatchers.IO) {
        database.checkpoint()

        val dbFile = context.getDatabasePath(DB_NAME)
        val walFile = File(dbFile.parent, "$DB_NAME-wal")
        val shmFile = File(dbFile.parent, "$DB_NAME-shm")
        val datastoreDir = File(context.filesDir.parent, "datastore")
        val datastoreFile = File(datastoreDir, "$DATASTORE_NAME.preferences_pb")
        val imagesDir = File(context.filesDir, "app_images")
        val giftPhotosDir = File(context.filesDir, "gift_photos")

        // 只备份数据库引用的图片，跳过孤立文件
        val referencedNames = queryReferencedImageFileNames()

        FileOutputStream(file).use { outputStream ->
            ZipOutputStream(outputStream).use { zipOut ->
                zipOut.setLevel(Deflater.BEST_SPEED)

                addFileToZip(zipOut, ENTRY_DATABASE, dbFile)
                if (walFile.exists()) addFileToZip(zipOut, ENTRY_WAL, walFile)
                if (shmFile.exists()) addFileToZip(zipOut, ENTRY_SHM, shmFile)
                if (datastoreFile.exists()) addFileToZip(zipOut, ENTRY_DATASTORE, datastoreFile)

                if (imagesDir.exists() && imagesDir.isDirectory) {
                    imagesDir.listFiles()?.forEach { f ->
                        if (f.isFile && f.name in referencedNames) {
                            addFileToZip(zipOut, "$ENTRY_IMAGES_DIR${f.name}", f)
                        }
                    }
                }
                if (giftPhotosDir.exists() && giftPhotosDir.isDirectory) {
                    giftPhotosDir.listFiles()?.forEach { f ->
                        if (f.isFile) addFileToZip(zipOut, "$ENTRY_GIFT_PHOTOS_DIR${f.name}", f)
                    }
                }
            }
        }

        BackupInfo(fileName = "", fileSize = file.length(), timestamp = System.currentTimeMillis())
    }

    // ===== 恢复逻辑 =====

    private data class ExtractDirs(
        val dbFile: File, val walFile: File, val shmFile: File,
        val datastoreDir: File, val datastoreFile: File,
        val imagesDir: File, val giftPhotosDir: File
    )

    private fun getExtractDirs(): ExtractDirs {
        val dbFile = context.getDatabasePath(DB_NAME)
        return ExtractDirs(
            dbFile = dbFile,
            walFile = File(dbFile.parent, "$DB_NAME-wal"),
            shmFile = File(dbFile.parent, "$DB_NAME-shm"),
            datastoreDir = File(context.filesDir.parent, "datastore"),
            datastoreFile = File(File(context.filesDir.parent, "datastore"), "$DATASTORE_NAME.preferences_pb"),
            imagesDir = File(context.filesDir, "app_images"),
            giftPhotosDir = File(context.filesDir, "gift_photos")
        )
    }

    private fun extractFromStream(inputStream: java.io.InputStream, dirs: ExtractDirs) {
        ZipInputStream(inputStream).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                when {
                    entry.name == ENTRY_DATABASE -> writeFileFromZip(zipIn, dirs.dbFile, dirs.dbFile.parentFile ?: dirs.dbFile)
                    entry.name == ENTRY_WAL -> writeFileFromZip(zipIn, dirs.walFile, dirs.walFile.parentFile ?: dirs.walFile)
                    entry.name == ENTRY_SHM -> writeFileFromZip(zipIn, dirs.shmFile, dirs.shmFile.parentFile ?: dirs.shmFile)
                    entry.name == ENTRY_DATASTORE -> {
                        dirs.datastoreDir.mkdirs()
                        writeFileFromZip(zipIn, dirs.datastoreFile, dirs.datastoreDir)
                    }
                    entry.name.startsWith(ENTRY_IMAGES_DIR) && !entry.isDirectory -> {
                        val name = entry.name.removePrefix(ENTRY_IMAGES_DIR)
                        if (name.isNotBlank()) {
                            dirs.imagesDir.mkdirs()
                            writeFileFromZip(zipIn, File(dirs.imagesDir, name), dirs.imagesDir)
                        }
                    }
                    entry.name.startsWith(ENTRY_GIFT_PHOTOS_DIR) && !entry.isDirectory -> {
                        val name = entry.name.removePrefix(ENTRY_GIFT_PHOTOS_DIR)
                        if (name.isNotBlank()) {
                            dirs.giftPhotosDir.mkdirs()
                            writeFileFromZip(zipIn, File(dirs.giftPhotosDir, name), dirs.giftPhotosDir)
                        }
                    }
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }
    }

    // ===== 通用工具 =====

    private fun addFileToZip(zipOut: ZipOutputStream, entryName: String, file: File) {
        if (!file.exists()) return
        zipOut.putNextEntry(ZipEntry(entryName))
        java.io.BufferedInputStream(FileInputStream(file), BUFFER_SIZE).use { fis ->
            val buffer = ByteArray(BUFFER_SIZE)
            var len: Int
            while (fis.read(buffer).also { len = it } > 0) {
                zipOut.write(buffer, 0, len)
            }
        }
        zipOut.closeEntry()
    }

    /**
     * 从 ZIP 流写入文件，严格校验目标路径在允许目录内，防止路径遍历攻击。
     *
     * 旧实现比较 [targetFile.canonicalPath] 与 [targetFile.parentFile.canonicalPath]，
     * 由于父目录必然是目标文件路径的前缀，该条件恒真，防护形同虚设。
     *
     * 新实现比较目标文件规范路径与允许目录的规范路径，使用 [File.separator] 作为分隔符
     * 防止前缀碰撞（如 /app_images_evil 不应匹配 /app_images）。
     *
     * @param zipIn ZIP 输入流
     * @param targetFile 目标文件
     * @param allowedDir 允许写入的根目录（canonicalPath 校验）
     */
    private fun writeFileFromZip(zipIn: ZipInputStream, targetFile: File, allowedDir: File) {
        val canonicalTarget = targetFile.canonicalPath
        val canonicalAllowed = allowedDir.canonicalPath
        if (!canonicalTarget.startsWith(canonicalAllowed + File.separator) &&
            canonicalTarget != canonicalAllowed) {
            android.util.Log.w("BackupRepository",
                "路径遍历拦截: $canonicalTarget 不在 $canonicalAllowed 内")
            return
        }
        targetFile.parentFile?.mkdirs()
        java.io.BufferedOutputStream(FileOutputStream(targetFile), BUFFER_SIZE).use { bos ->
            val buffer = ByteArray(BUFFER_SIZE)
            var len: Int
            while (zipIn.read(buffer).also { len = it } > 0) {
                bos.write(buffer, 0, len)
            }
        }
    }

    /**
     * 强制重启应用进程。
     *
     * **为什么必须使用 System.exit(0) 而非正常 Activity 重建？**
     * 备份恢复会将 Room 数据库文件（.db / .db-wal / .db-shm）整体覆盖，
     * 导致进程中所有已打开的 SQLite 连接指向的文件句柄失效——
     * Room 的 SQLiteDatabase 对象仍持有旧文件的 fd，后续任何读写都会抛出
     * "database disk image is malformed" 或 SQLITE_CORRUPT。
     * 由于 Room 的连接池由框架内部管理，无法可靠地使所有 DAO / Repository
     * 的引用失效并重建，因此必须杀死进程让系统重新创建 Application，
     * 从而重新打开数据库。
     *
     * **代价**：System.exit(0) 会跳过 Activity/Service 的 onDestroy 生命周期，
     * 未刷新的日志可能丢失。因此调用方必须在调用本方法之前完成 WAL checkpoint
     * 和关键日志的刷新。
     *
     * **1 秒延迟**：AlarmManager 设定 1 秒后启动新 Activity，这段窗口期
     * 用于确保 Log.i 输出被系统日志守护进程刷新到磁盘，避免重启后日志丢失。
     */
    private fun restartApp() {
        Log.i(TAG, "应用即将因备份恢复重启进程：Room 数据库文件已被覆盖，" +
                "现有连接失效，需要进程级重启以重建数据库连接池")

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
            val pendingIntent = android.app.PendingIntent.getActivity(
                context, 0, intent,
                android.app.PendingIntent.FLAG_ONE_SHOT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            // 1 秒延迟：给系统日志守护进程时间将 Log 输出刷新到磁盘
            alarmManager.set(android.app.AlarmManager.ELAPSED_REALTIME_WAKEUP,
                android.os.SystemClock.elapsedRealtime() + 1000, pendingIntent)
        }
        System.exit(0)
    }

    override fun generateBackupFileName(): String {
        return "$BACKUP_PREFIX${dateFormat.format(LocalDateTime.now())}$BACKUP_EXTENSION"
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

            restartApp()
            ClearDataResult.Success
        } catch (e: Exception) {
            ClearDataResult.Error("清空数据失败：${e.message}")
        }
    }
}
