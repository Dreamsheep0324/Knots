package com.tang.prm.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.tang.prm.data.local.dao.RecipeDao
import com.tang.prm.data.local.dao.RecipeTagDao
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.RecipeContactCrossRef
import com.tang.prm.data.local.entity.RecipeTagCrossRef
import com.tang.prm.data.mapper.mapList
import com.tang.prm.data.mapper.mapNullable
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.data.mapper.toRecipeDomainList
import com.tang.prm.data.util.ImageFileManager
import com.tang.prm.domain.model.Recipe
import com.tang.prm.domain.model.RecipeTag
import com.tang.prm.domain.repository.RecipeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val recipeDao: RecipeDao,
    private val recipeTagDao: RecipeTagDao,
    private val database: TangDatabase,
    @ApplicationContext private val context: Context
) : RecipeRepository {

    override fun getAllRecipes(): Flow<List<Recipe>> =
        recipeDao.getAllRecipes().mapList { it.toDomain() }

    override fun getRecipeById(id: Long): Flow<Recipe?> =
        recipeDao.getRecipeById(id).mapNullable { it.toDomain() }

    override fun getRecipesByContactId(contactId: Long): Flow<List<Recipe>> =
        recipeDao.getRecipesByContactId(contactId).mapList { it.toDomain() }

    override suspend fun getRecipeByIdOnce(id: Long): Recipe? {
        val entity = recipeDao.getRecipeByIdOnce(id) ?: return null
        return entity.toDomain()
    }

    override suspend fun insertRecipe(recipe: Recipe): Long =
        recipeDao.insertRecipe(recipe.toEntity())

    override suspend fun updateRecipe(recipe: Recipe) {
        val removedPhotos = database.withTransaction {
            val oldEntity = recipeDao.getRecipeByIdOnce(recipe.id)
            val removed = oldEntity?.let { old ->
                (old.photos.toSet() - recipe.photos.toSet()).takeIf { it.isNotEmpty() }
            }
            recipeDao.updateRecipe(recipe.toEntity())
            recipeDao.deleteRecipeContactCrossRefs(recipe.id)
            recipeDao.deleteRecipeTagCrossRefs(recipe.id)
            if (recipe.likedByContactIds.isNotEmpty()) {
                recipeDao.insertRecipeContactCrossRefs(
                    recipe.likedByContactIds.map { contactId ->
                        RecipeContactCrossRef(recipeId = recipe.id, contactId = contactId)
                    }
                )
            }
            removed
        }
        removedPhotos?.let { deletePhotoFiles(it.toList()) }
    }

    override suspend fun deleteRecipe(id: Long) {
        val photosToDelete = database.withTransaction {
            val photos = recipeDao.getRecipeByIdOnce(id)?.photos ?: emptyList()
            recipeDao.deleteRecipeById(id)
            photos
        }
        deletePhotoFiles(photosToDelete)
    }

    override suspend fun saveRecipeWithPhotos(
        recipe: Recipe,
        photoUris: List<String>
    ): Pair<Long, Int> {
        // 并发复制图片，缩短用户保存等待时间
        val copyResults = coroutineScope {
            photoUris.map { uriString ->
                async {
                    val uri = Uri.parse(uriString)
                    ImageFileManager.copyToInternalStorage(context, uri, "recipe")
                }
            }.awaitAll()
        }
        val savedPaths = copyResults.mapNotNull { it }
        val failedCount = copyResults.count { it == null }

        val newRecipe = recipe.copy(
            id = 0,
            photos = savedPaths,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val id = database.withTransaction {
            val recipeId = recipeDao.insertRecipe(newRecipe.toEntity())
            if (recipeId > 0 && recipe.likedByContactIds.isNotEmpty()) {
                recipeDao.insertRecipeContactCrossRefs(
                    recipe.likedByContactIds.map { contactId ->
                        RecipeContactCrossRef(recipeId = recipeId, contactId = contactId)
                    }
                )
            }
            recipeId
        }
        return id to failedCount
    }

    override fun getRecipeCount(): Flow<Int> = recipeDao.getRecipeCount()

    override fun getPhotoCount(): Flow<Int> = recipeDao.getPhotoCount()

    override fun getAllTags(): Flow<List<RecipeTag>> =
        recipeTagDao.getAllTags().mapList { it.toDomain() }

    override suspend fun insertTag(tag: RecipeTag): Long =
        recipeTagDao.insertTag(tag.toEntity())

    override suspend fun deleteTag(id: Long) = recipeTagDao.deleteTagById(id)

    override suspend fun getReferencedPhotoPaths(): List<String> =
        recipeDao.getReferencedPhotoPaths()

    private suspend fun deletePhotoFiles(photos: List<String>) =
        ImageFileManager.deleteLocalPhotos(photos)
}
