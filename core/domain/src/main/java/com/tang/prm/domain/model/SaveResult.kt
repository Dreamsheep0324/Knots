package com.tang.prm.domain.model

/**
 * 保存实体（含照片）的结果（R-4：替代裸 Pair<Long, Int>）
 *
 * @param id 新建或更新的实体 ID
 * @param failedPhotoCount 保存失败的照片数量（如源 URI 无法读取等）
 */
data class SaveResult(
    val id: Long,
    val failedPhotoCount: Int
)
