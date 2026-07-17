package com.tang.prm.data.repository

import android.util.Log
import com.tang.prm.data.local.dao.ContactDao
import com.tang.prm.data.local.dao.EventDao
import com.tang.prm.data.local.dao.GiftDao
import java.io.File

/**
 * 图片引用查询：统一查询数据库中引用的图片文件名，用于备份过滤和孤儿图片清理。
 */
class ImageReferenceQuery(
    private val contactDao: ContactDao,
    private val giftDao: GiftDao,
    private val eventDao: EventDao
) {
    suspend fun queryReferencedImageFileNames(): Set<String> {
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

    private fun extractImageFileName(path: String?): String? {
        if (path.isNullOrBlank()) return null
        val filePath = if (path.startsWith("file://")) path.removePrefix("file://") else path
        if (!filePath.contains("app_images")) return null
        return File(filePath).name
    }

    private fun parsePhotoPaths(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = org.json.JSONArray(json)
            (0 until arr.length()).mapNotNull { arr.optString(it) }
        } catch (e: Exception) {
            Log.w("ImageReferenceQuery", "解析图片路径JSON失败", e)
            emptyList()
        }
    }
}
