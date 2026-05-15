package com.tang.prm.data.repository

import android.content.Context
import android.net.Uri
import com.tang.prm.data.local.database.TangDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import android.os.Process
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class BackupInfo(
    val fileName: String,
    val fileSize: Long,
    val timestamp: Long
)

sealed class BackupResult {
    data class Success(val info: BackupInfo) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

sealed class RestoreResult {
    data object Success : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}

sealed class ClearDataResult {
    data object Success : ClearDataResult()
    data class Error(val message: String) : ClearDataResult()
}

@Singleton
class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: TangDatabase
) {
    companion object {
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
    }

    private val dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.getDefault())

    suspend fun backupToUri(uri: Uri): Flow<BackupResult> = flow {
        emit(performBackup(uri))
    }

    suspend fun restoreFromUri(uri: Uri): Flow<RestoreResult> = flow {
        emit(performRestore(uri))
    }

    private suspend fun performBackup(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            database.checkpoint()

            val dbFile = context.getDatabasePath(DB_NAME)
            val walFile = File(dbFile.parent, "$DB_NAME-wal")
            val shmFile = File(dbFile.parent, "$DB_NAME-shm")
            val datastoreDir = File(context.filesDir.parent, "datastore")
            val datastoreFile = File(datastoreDir, "$DATASTORE_NAME.preferences_pb")
            val imagesDir = File(context.filesDir, "app_images")
            val giftPhotosDir = File(context.filesDir, "gift_photos")

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                ZipOutputStream(outputStream).use { zipOut ->
                    addFileToZip(zipOut, ENTRY_DATABASE, dbFile)

                    if (walFile.exists()) {
                        addFileToZip(zipOut, ENTRY_WAL, walFile)
                    }
                    if (shmFile.exists()) {
                        addFileToZip(zipOut, ENTRY_SHM, shmFile)
                    }
                    if (datastoreFile.exists()) {
                        addFileToZip(zipOut, ENTRY_DATASTORE, datastoreFile)
                    }

                    if (imagesDir.exists() && imagesDir.isDirectory) {
                        imagesDir.listFiles()?.forEach { file ->
                            if (file.isFile) {
                                addFileToZip(zipOut, "$ENTRY_IMAGES_DIR${file.name}", file)
                            }
                        }
                    }

                    if (giftPhotosDir.exists() && giftPhotosDir.isDirectory) {
                        giftPhotosDir.listFiles()?.forEach { file ->
                            if (file.isFile) {
                                addFileToZip(zipOut, "$ENTRY_GIFT_PHOTOS_DIR${file.name}", file)
                            }
                        }
                    }
                }
            } ?: return@withContext BackupResult.Error("无法打开输出流")

            val fileSize = getFileSizeFromUri(uri)
            val timestamp = System.currentTimeMillis()
            val fileName = extractFileName(uri) ?: "$BACKUP_PREFIX${dateFormat.format(LocalDateTime.now())}$BACKUP_EXTENSION"

            BackupResult.Success(BackupInfo(fileName = fileName, fileSize = fileSize, timestamp = timestamp))
        } catch (e: Exception) {
            BackupResult.Error("备份失败：${e.message}")
        }
    }

    private suspend fun performRestore(uri: Uri): RestoreResult = withContext(Dispatchers.IO) {
        try {
            database.close()

            val dbFile = context.getDatabasePath(DB_NAME)
            val walFile = File(dbFile.parent, "$DB_NAME-wal")
            val shmFile = File(dbFile.parent, "$DB_NAME-shm")
            val datastoreDir = File(context.filesDir.parent, "datastore")
            val datastoreFile = File(datastoreDir, "$DATASTORE_NAME.preferences_pb")
            val imagesDir = File(context.filesDir, "app_images")
            val giftPhotosDir = File(context.filesDir, "gift_photos")

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zipIn ->
                    var entry = zipIn.nextEntry
                    while (entry != null) {
                        when {
                            entry.name == ENTRY_DATABASE -> writeFileFromZip(zipIn, dbFile)
                            entry.name == ENTRY_WAL -> writeFileFromZip(zipIn, walFile)
                            entry.name == ENTRY_SHM -> writeFileFromZip(zipIn, shmFile)
                            entry.name == ENTRY_DATASTORE -> {
                                datastoreDir.mkdirs()
                                writeFileFromZip(zipIn, datastoreFile)
                            }
                            entry.name.startsWith(ENTRY_IMAGES_DIR) && !entry.isDirectory -> {
                                val fileName = entry.name.removePrefix(ENTRY_IMAGES_DIR)
                                if (fileName.isNotBlank()) {
                                    imagesDir.mkdirs()
                                    writeFileFromZip(zipIn, File(imagesDir, fileName))
                                }
                            }
                            entry.name.startsWith(ENTRY_GIFT_PHOTOS_DIR) && !entry.isDirectory -> {
                                val fileName = entry.name.removePrefix(ENTRY_GIFT_PHOTOS_DIR)
                                if (fileName.isNotBlank()) {
                                    giftPhotosDir.mkdirs()
                                    writeFileFromZip(zipIn, File(giftPhotosDir, fileName))
                                }
                            }
                        }
                        zipIn.closeEntry()
                        entry = zipIn.nextEntry
                    }
                }
            } ?: return@withContext RestoreResult.Error("无法打开备份文件")

            withContext(Dispatchers.IO) { Process.killProcess(Process.myPid()) }
            RestoreResult.Success
        } catch (e: Exception) {
            RestoreResult.Error("恢复失败：${e.message}")
        }
    }

    private fun addFileToZip(zipOut: ZipOutputStream, entryName: String, file: File) {
        if (!file.exists()) return
        zipOut.putNextEntry(ZipEntry(entryName))
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var len: Int
            while (fis.read(buffer).also { len = it } > 0) {
                zipOut.write(buffer, 0, len)
            }
        }
        zipOut.closeEntry()
    }

    private fun writeFileFromZip(zipIn: ZipInputStream, targetFile: File) {
        targetFile.parentFile?.mkdirs()
        FileOutputStream(targetFile).use { fos ->
            val buffer = ByteArray(8192)
            var len: Int
            while (zipIn.read(buffer).also { len = it } > 0) {
                fos.write(buffer, 0, len)
            }
        }
    }

    private fun getFileSizeFromUri(uri: Uri): Long {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (cursor.moveToFirst() && sizeIndex >= 0) cursor.getLong(sizeIndex) else 0L
        } ?: 0L
    }

    private fun extractFileName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
        }
    }

    fun generateBackupFileName(): String {
        return "$BACKUP_PREFIX${dateFormat.format(LocalDateTime.now())}$BACKUP_EXTENSION"
    }

    suspend fun clearAllData(): ClearDataResult = withContext(Dispatchers.IO) {
        try {
            database.checkpoint()
            database.close()

            val dbFile = context.getDatabasePath(DB_NAME)
            val walFile = File(dbFile.parent, "$DB_NAME-wal")
            val shmFile = File(dbFile.parent, "$DB_NAME-shm")

            if (dbFile.exists()) dbFile.delete()
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            val datastoreDir = File(context.filesDir.parent, "datastore")
            val datastoreFile = File(datastoreDir, "$DATASTORE_NAME.preferences_pb")
            if (datastoreFile.exists()) datastoreFile.delete()

            val imagesDir = File(context.filesDir, "app_images")
            if (imagesDir.exists() && imagesDir.isDirectory) {
                imagesDir.listFiles()?.forEach { it.delete() }
            }

            val giftPhotosDir = File(context.filesDir, "gift_photos")
            if (giftPhotosDir.exists() && giftPhotosDir.isDirectory) {
                giftPhotosDir.listFiles()?.forEach { it.delete() }
            }

            withContext(Dispatchers.IO) { Process.killProcess(Process.myPid()) }
            ClearDataResult.Success
        } catch (e: Exception) {
            ClearDataResult.Error("清空数据失败：${e.message}")
        }
    }
}
