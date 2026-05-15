package com.tang.prm.data.repository

import androidx.room.withTransaction
import com.tang.prm.data.local.dao.FavoriteDao
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Favorite
import com.tang.prm.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val database: TangDatabase
) : FavoriteRepository {

    override fun getAllFavorites(): Flow<List<Favorite>> {
        return favoriteDao.getAllFavorites().map { it.map { e -> e.toDomain() } }
    }

    override fun getFavoritesByType(type: String): Flow<List<Favorite>> {
        return favoriteDao.getFavoritesByType(type).map { it.map { e -> e.toDomain() } }
    }

    override fun isFavorite(type: String, sourceId: Long): Flow<Boolean> {
        return favoriteDao.isFavorite(type, sourceId)
    }

    override suspend fun toggleFavorite(type: String, sourceId: Long, title: String, description: String?): Boolean {
        return database.withTransaction {
            val existing = favoriteDao.getFavoriteBySource(type, sourceId)
            if (existing != null) {
                favoriteDao.deleteFavoriteBySource(type, sourceId)
                false
            } else {
                favoriteDao.insertFavorite(
                    Favorite(
                        sourceType = type,
                        sourceId = sourceId,
                        title = title,
                        description = description
                    ).toEntity()
                )
                true
            }
        }
    }

    override suspend fun insertFavorite(favorite: Favorite): Long {
        return favoriteDao.insertFavorite(favorite.toEntity())
    }

    override suspend fun deleteFavoriteBySource(type: String, sourceId: Long) {
        favoriteDao.deleteFavoriteBySource(type, sourceId)
    }

    override fun getCountByType(type: String): Flow<Int> {
        return favoriteDao.getCountByType(type)
    }

    override fun getTotalCount(): Flow<Int> {
        return favoriteDao.getTotalCount()
    }

    override fun getFavoriteCount(): Flow<Int> = favoriteDao.getTotalCount()
}
