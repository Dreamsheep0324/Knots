package com.tang.prm.data.repository

import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * 备份 ZIP 工具：压缩 / 解压 / 路径遍历防护。
 * 纯文件操作，不依赖 Context 或数据库。
 */
object BackupZipUtils {
    private const val TAG = "BackupZipUtils"
    const val BUFFER_SIZE = 256 * 1024 // 256KB

    const val ENTRY_DATABASE = "tang_database.db"
    const val ENTRY_DATASTORE = "settings.preferences_pb"
    const val ENTRY_WAL = "tang_database.db-wal"
    const val ENTRY_SHM = "tang_database.db-shm"
    const val ENTRY_IMAGES_DIR = "app_images/"
    const val ENTRY_GIFT_PHOTOS_DIR = "gift_photos/"

    data class ExtractDirs(
        val dbFile: File, val walFile: File, val shmFile: File,
        val datastoreDir: File, val datastoreFile: File,
        val imagesDir: File, val giftPhotosDir: File
    )

    fun addFileToZip(zipOut: ZipOutputStream, entryName: String, file: File) {
        if (!file.exists()) return
        zipOut.putNextEntry(ZipEntry(entryName))
        BufferedInputStream(FileInputStream(file), BUFFER_SIZE).use { fis ->
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
     * @param zipIn ZIP 输入流
     * @param targetFile 目标文件
     * @param allowedDir 允许写入的根目录（canonicalPath 校验）
     */
    fun writeFileFromZip(zipIn: ZipInputStream, targetFile: File, allowedDir: File) {
        val canonicalTarget = targetFile.canonicalPath
        val canonicalAllowed = allowedDir.canonicalPath
        if (!canonicalTarget.startsWith(canonicalAllowed + File.separator) &&
            canonicalTarget != canonicalAllowed) {
            Log.w(TAG, "路径遍历拦截: $canonicalTarget 不在 $canonicalAllowed 内")
            return
        }
        targetFile.parentFile?.mkdirs()
        BufferedOutputStream(FileOutputStream(targetFile), BUFFER_SIZE).use { bos ->
            val buffer = ByteArray(BUFFER_SIZE)
            var len: Int
            while (zipIn.read(buffer).also { len = it } > 0) {
                bos.write(buffer, 0, len)
            }
        }
    }

    fun extractFromStream(inputStream: java.io.InputStream, dirs: ExtractDirs) {
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

    fun writeDbOnlyZip(
        file: File,
        dbFile: File,
        walFile: File,
        shmFile: File,
        datastoreFile: File
    ) {
        FileOutputStream(file).use { outputStream ->
            ZipOutputStream(outputStream).use { zipOut ->
                zipOut.setLevel(Deflater.BEST_SPEED)
                addFileToZip(zipOut, ENTRY_DATABASE, dbFile)
                if (walFile.exists()) addFileToZip(zipOut, ENTRY_WAL, walFile)
                if (shmFile.exists()) addFileToZip(zipOut, ENTRY_SHM, shmFile)
                if (datastoreFile.exists()) addFileToZip(zipOut, ENTRY_DATASTORE, datastoreFile)
            }
        }
    }
}
