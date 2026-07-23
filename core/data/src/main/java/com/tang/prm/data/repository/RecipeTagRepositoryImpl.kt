package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.RecipeTagDao
import com.tang.prm.data.mapper.mapList
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.RecipeTag
import com.tang.prm.domain.repository.RecipeTagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeTagRepositoryImpl @Inject constructor(
    private val recipeTagDao: RecipeTagDao
) : RecipeTagRepository {
    override fun getAllTags(): Flow<List<RecipeTag>> =
        recipeTagDao.getAllTags().mapList { it.toDomain() }

    override suspend fun insertTag(tag: RecipeTag): Long =
        recipeTagDao.insertTag(tag.toEntity())

    override suspend fun deleteTag(id: Long) =
        recipeTagDao.deleteTagById(id)
}
