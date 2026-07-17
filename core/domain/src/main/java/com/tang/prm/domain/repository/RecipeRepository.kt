package com.tang.prm.domain.repository

import com.tang.prm.domain.model.Recipe
import com.tang.prm.domain.model.RecipeTag
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getAllRecipes(): Flow<List<Recipe>>
    fun getRecipeById(id: Long): Flow<Recipe?>
    fun getRecipesByContactId(contactId: Long): Flow<List<Recipe>>
    suspend fun getRecipeByIdOnce(id: Long): Recipe?
    suspend fun insertRecipe(recipe: Recipe): Long
    suspend fun updateRecipe(recipe: Recipe)
    suspend fun deleteRecipe(id: Long)
    suspend fun saveRecipeWithPhotos(recipe: Recipe, photoUris: List<String>): Pair<Long, Int>
    fun getRecipeCount(): Flow<Int>
    fun getPhotoCount(): Flow<Int>
    fun getAllTags(): Flow<List<RecipeTag>>
    suspend fun insertTag(tag: RecipeTag): Long
    suspend fun deleteTag(id: Long)
    suspend fun getReferencedPhotoPaths(): List<String>
}
