package com.tang.prm.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageCacheManager {

    private const val DIR_NAME = "app_images"

    private fun getImagesDir(context: Context): File {
        val dir = File(context.filesDir, DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun copyToInternalStorage(context: Context, uri: Uri, prefix: String = "img"): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                val fileName = "${prefix}_${UUID.randomUUID()}.jpg"
                val file = File(getImagesDir(context), fileName)
                inputStream.use { stream ->
                    FileOutputStream(file).use { outputStream ->
                        stream.copyTo(outputStream)
                    }
                }
                file.absolutePath
            } catch (e: Exception) {
                null
            }
        }
    }

    fun isLocalPath(path: String): Boolean {
        return path.startsWith("/") || path.startsWith("file://")
    }

    fun fileExists(path: String): Boolean {
        val filePath = if (path.startsWith("file://")) path.removePrefix("file://") else path
        return File(filePath).exists()
    }

    suspend fun deleteImage(path: String) {
        withContext(Dispatchers.IO) {
            val filePath = if (path.startsWith("file://")) path.removePrefix("file://") else path
            val file = File(filePath)
            if (file.exists() && file.absolutePath.contains(DIR_NAME)) {
                file.delete()
            }
        }
    }
}
