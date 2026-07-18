package com.tang.prm.data.repository

import android.util.Log
import com.tang.prm.data.local.dao.ContactDao
import com.tang.prm.data.local.dao.EventDao
import com.tang.prm.data.local.dao.GiftDao
import com.tang.prm.data.local.dao.RecipeDao
import com.tang.prm.data.util.ImageFileManager
import java.io.File

/**
 * 图片引用查询：统一查询数据库中引用的图片文件名，用于备份过滤和孤儿图片清理。
 *
 * 覆盖所有持有图片引用的聚合：联系人头像、礼物照片、事件照片、菜谱照片。
 * 新增图片持有聚合时必须在此处接入，否则其图片会被孤儿清理误删且不参与备份恢复。
 */
class ImageReferenceQuery(
    private val contactDao: ContactDao,
    private val giftDao: GiftDao,
    private val eventDao: EventDao,
    private val recipeDao: RecipeDao
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

        // 菜谱照片以 List<String> 直接存储（非 JSON），直接取路径
        recipeDao.getReferencedPhotoPaths().forEach { path ->
            extractImageFileName(path)?.let { referencedNames.add(it) }
        }

        return referencedNames
    }

    private fun extractImageFileName(path: String?): String? {
        if (path.isNullOrBlank()) return null
        // REP-C-2 修复：复用 ImageFileManager.toFilePath 统一 file:// 前缀剥离逻辑。
        val filePath = ImageFileManager.toFilePath(path)
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
