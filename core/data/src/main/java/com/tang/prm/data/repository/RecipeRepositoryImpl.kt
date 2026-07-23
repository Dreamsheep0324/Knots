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
import com.tang.prm.data.util.ImageFileManager
import com.tang.prm.data.util.computeRemovedPhotos
import com.tang.prm.domain.model.Recipe
import com.tang.prm.domain.model.SaveResult
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

    override fun getRecipeListItems(): Flow<List<Recipe>> =
        recipeDao.getRecipeListItems().mapList { it.toDomain() }

    override fun observeRecipeById(id: Long): Flow<Recipe?> =
        recipeDao.getRecipeById(id).mapNullable { it.toDomain() }

    override fun getRecipesByContactId(contactId: Long): Flow<List<Recipe>> =
        recipeDao.getRecipesByContactId(contactId).mapList { it.toDomain() }

    override suspend fun getRecipeById(id: Long): Recipe? {
        val entity = recipeDao.getRecipeByIdOnce(id) ?: return null
        return entity.toDomain()
    }

    override suspend fun insertRecipe(recipe: Recipe): Long {
        val recipeId = database.withTransaction {
            val id = recipeDao.insertRecipe(recipe.toEntity())
            if (id > 0) {
                insertContactCrossRefs(id, recipe.likedByContactIds)
                insertTagCrossRefs(id, recipe.tags)
            }
            id
        }
        return recipeId
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        val removedPhotos = database.withTransaction {
            val oldEntity = recipeDao.getRecipeByIdOnce(recipe.id)
            // REP-Q-5 修复：复用 computeRemovedPhotos 统一 photos 差集计算逻辑。
            val removed = computeRemovedPhotos(oldEntity, recipe.photos) { it.photos }
            recipeDao.updateRecipe(recipe.toEntity())
            recipeDao.deleteRecipeContactCrossRefs(recipe.id)
            recipeDao.deleteRecipeTagCrossRefs(recipe.id)
            insertContactCrossRefs(recipe.id, recipe.likedByContactIds)
            insertTagCrossRefs(recipe.id, recipe.tags)
            removed
        }
        removedPhotos?.let { ImageFileManager.deleteLocalPhotos(context, it.toList()) }
    }

    override suspend fun deleteRecipe(id: Long) {
        val photosToDelete = database.withTransaction {
            val photos = recipeDao.getRecipeByIdOnce(id)?.photos ?: emptyList()
            recipeDao.deleteRecipeById(id)
            photos
        }
        ImageFileManager.deleteLocalPhotos(context, photosToDelete)
    }

    override suspend fun saveRecipeWithPhotos(
        recipe: Recipe,
        photoUris: List<String>
    ): SaveResult {
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
            if (recipeId > 0) {
                insertContactCrossRefs(recipeId, recipe.likedByContactIds)
                insertTagCrossRefs(recipeId, recipe.tags)
            }
            recipeId
        }
        return SaveResult(id = id, failedPhotoCount = failedCount)
    }

    override fun getRecipeCount(): Flow<Int> = recipeDao.getRecipeCount()

    /**
     * 写入菜谱-联系人交叉引用。空列表时跳过，避免无意义 DELETE+INSERT。
     * 调用前需确保 recipeId 已落库。
     */
    private suspend fun insertContactCrossRefs(recipeId: Long, contactIds: List<Long>) {
        if (contactIds.isEmpty()) return
        recipeDao.insertRecipeContactCrossRefs(
            contactIds.map { contactId ->
                RecipeContactCrossRef(recipeId = recipeId, contactId = contactId)
            }
        )
    }

    /**
     * 写入菜谱-标签交叉引用。空列表时跳过。
     *
     * Recipe.tags 是 List<String>（标签名称），需先通过 RecipeTagDao.getTagsByNames
     * 解析为 tagId 再写入 cross_ref 表。未匹配名称的标签会被静默跳过——
     * 标签应先通过 insertTag 创建，再在菜谱中引用。
     */
    private suspend fun insertTagCrossRefs(recipeId: Long, tagNames: List<String>) {
        if (tagNames.isEmpty()) return
        val tagEntities = recipeTagDao.getTagsByNames(tagNames)
        if (tagEntities.isEmpty()) return
        recipeDao.insertRecipeTagCrossRefs(
            tagEntities.map { tag ->
                RecipeTagCrossRef(recipeId = recipeId, tagId = tag.id)
            }
        )
    }
}
