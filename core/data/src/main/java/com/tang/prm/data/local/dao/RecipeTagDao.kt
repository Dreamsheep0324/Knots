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
}
