package com.tang.prm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tang.prm.data.local.entity.RecipeTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeTagDao {
    @Query("SELECT * FROM recipe_tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<RecipeTagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: RecipeTagEntity): Long

    @Query("DELETE FROM recipe_tags WHERE id = :id")
    suspend fun deleteTagById(id: Long)

    /**
     * 按名称批量查询标签：用于菜谱保存/更新时把 tagNames 解析为 tagId，
     * 进而写入 recipe_tag_cross_ref 表。未匹配的名称返回空（标签需先通过 insertTag 创建）。
     */
    @Query("SELECT * FROM recipe_tags WHERE name IN (:names)")
    suspend fun getTagsByNames(names: List<String>): List<RecipeTagEntity>
}
