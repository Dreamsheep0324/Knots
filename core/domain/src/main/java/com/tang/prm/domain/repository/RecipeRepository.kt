package com.tang.prm.domain.repository

import com.tang.prm.domain.model.Recipe
import com.tang.prm.domain.model.SaveResult
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getAllRecipes(): Flow<List<Recipe>>
    fun getRecipeListItems(): Flow<List<Recipe>>
    fun observeRecipeById(id: Long): Flow<Recipe?>
    fun getRecipesByContactId(contactId: Long): Flow<List<Recipe>>
    suspend fun getRecipeById(id: Long): Recipe?
    suspend fun insertRecipe(recipe: Recipe): Long
    suspend fun updateRecipe(recipe: Recipe)
    suspend fun deleteRecipe(id: Long)
    suspend fun saveRecipeWithPhotos(recipe: Recipe, photoUris: List<String>): SaveResult
    fun getRecipeCount(): Flow<Int>
}
