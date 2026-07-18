package com.tang.prm.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.tang.prm.data.local.entity.RecipeContactCrossRef
import com.tang.prm.data.local.entity.RecipeEntity
import com.tang.prm.data.local.entity.RecipeListItemWithRelations
import com.tang.prm.data.local.entity.RecipeTagCrossRef
import com.tang.prm.data.local.entity.RecipeWithContactsAndTags
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Transaction
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun getAllRecipes(): Flow<List<RecipeWithContactsAndTags>>

    /**
     * 菜谱列表页轻量投影：仅查询列表展示所需列，跳过 ingredients/steps/notes 等大 TEXT 列。
     * 关联的 contacts 也投影为 [com.tang.prm.data.local.dao.ContactListItemEntity]。
     */
    @Transaction
    @Query(
        """
        SELECT id, title, description, cuisine, difficulty, cookingTime, servings,
               photos, photos_count, rating, isFavorite, createdAt
        FROM recipes ORDER BY createdAt DESC
        """
    )
    fun getRecipeListItems(): Flow<List<RecipeListItemWithRelations>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getRecipeById(id: Long): Flow<RecipeWithContactsAndTags?>

    @Transaction
    @Query(
        """
        SELECT * FROM recipes WHERE id IN (
            SELECT recipeId FROM recipe_contact_cross_ref WHERE contactId = :contactId
        ) ORDER BY createdAt DESC
        """
    )
    fun getRecipesByContactId(contactId: Long): Flow<List<RecipeWithContactsAndTags>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeByIdOnce(id: Long): RecipeEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: Long)

    // DAO-D-7 修复：移除零调用的 singular insertRecipeContactCrossRef，统一使用 plural 版本。
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipeContactCrossRefs(crossRefs: List<RecipeContactCrossRef>)

    @Query("DELETE FROM recipe_contact_cross_ref WHERE recipeId = :recipeId")
    suspend fun deleteRecipeContactCrossRefs(recipeId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipeTagCrossRef(crossRef: RecipeTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipeTagCrossRefs(crossRefs: List<RecipeTagCrossRef>)

    @Query("DELETE FROM recipe_tag_cross_ref WHERE recipeId = :recipeId")
    suspend fun deleteRecipeTagCrossRefs(recipeId: Long)

    @Query("SELECT COUNT(*) FROM recipes")
    fun getRecipeCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(photos_count), 0) FROM recipes")
    fun getPhotoCount(): Flow<Int>

    @Query("SELECT photos FROM recipes WHERE photos IS NOT NULL AND photos != '' AND photos != '[]'")
    suspend fun getReferencedPhotoPaths(): List<String>
}
