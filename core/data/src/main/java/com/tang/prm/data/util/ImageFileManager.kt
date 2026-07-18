package com.tang.prm.data.util

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageFileManager {

    private const val TAG = "ImageFileManager"
    private const val DIR_NAME = "app_images"

    private fun getImagesDir(context: Context): File {
        val dir = File(context.filesDir, DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun copyToInternalStorage(context: Context, uri: Uri, prefix: String = "img"): String? {
        return withContext(Dispatchers.IO) {
            try {
                // UTIL-Q-1 修复：根据 ContentResolver 推断的 MIME 类型选择文件扩展名，
                // 避免 PNG/WEBP/GIF/HEIC 被误存为 .jpg 导致解码器误判、MIME 不一致、EXIF 错乱。
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val ext = when (mimeType) {
                    "image/png" -> "png"
                    "image/webp" -> "webp"
                    "image/gif" -> "gif"
                    "image/heic", "image/heif" -> "heic"
                    else -> "jpg"
                }
                val fileName = "${prefix}_${UUID.randomUUID()}.$ext"
                val file = File(getImagesDir(context), fileName)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    FileOutputStream(file).use { outputStream ->
                        stream.copyTo(outputStream)
                    }
                } ?: return@withContext null
                file.absolutePath
            } catch (e: Exception) {
                Log.w(TAG, "复制图片到内部存储失败: $uri", e)
                null
            }
        }
    }

    fun isLocalPath(path: String): Boolean =
        path.startsWith("/") || path.startsWith("file://")

    /**
     * 将 file:// URI 剥离前缀转换为文件系统路径。
     *
     * REP-C-2 修复：抽取公共函数，消除 ImageFileManager.deleteImage 与
     * ImageReferenceQuery.extractImageFileName 中的重复逻辑。
     */
    fun toFilePath(path: String): String =
        if (path.startsWith("file://")) path.removePrefix("file://") else path

    /**
     * 删除指定路径的图片文件。
     *
     * UTIL-B-1 修复：使用 canonical path 校验替代子串匹配 `contains(DIR_NAME)`，
     * 防止 `../` 路径遍历攻击绕过校验导致任意文件删除。
     */
    suspend fun deleteImage(context: Context, path: String) {
        withContext(Dispatchers.IO) {
            val filePath = toFilePath(path)
            val allowedRoot = getImagesDir(context).canonicalPath
            val file = try {
                File(filePath).canonicalFile
            } catch (e: Exception) {
                Log.w(TAG, "解析文件路径失败: $filePath", e)
                return@withContext
            }
            if (file.absolutePath.startsWith(allowedRoot + File.separator) && file.exists()) {
                file.delete()
            } else if (file.exists()) {
                Log.w(TAG, "拒绝删除目录外文件: $filePath (resolved: ${file.absolutePath})")
            }
        }
    }

    suspend fun deleteLocalPhotos(context: Context, photos: List<String>) {
        photos.forEach { path ->
            if (isLocalPath(path)) deleteImage(context, path)
        }
    }
}
