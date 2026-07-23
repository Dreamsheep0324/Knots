package com.tang.prm.domain.repository

import com.tang.prm.domain.model.RecipeTag
import kotlinx.coroutines.flow.Flow

/**
 * R-3 修复：从 RecipeRepository 拆分到独立文件。
 * Tag 是独立聚合根，菜谱标签管理职责单一化。
 */
interface RecipeTagRepository {
    fun getAllTags(): Flow<List<RecipeTag>>
    suspend fun insertTag(tag: RecipeTag): Long
    suspend fun deleteTag(id: Long)
}
