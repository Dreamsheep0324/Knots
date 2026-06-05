package com.tang.prm.domain.repository

import com.tang.prm.domain.model.Favorite
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun getAllFavorites(): Flow<List<Favorite>>
    fun getFavoritesByType(type: String): Flow<List<Favorite>>
    fun isFavorite(type: String, sourceId: Long): Flow<Boolean>
    suspend fun toggleFavorite(type: String, sourceId: Long, title: String, description: String?): Boolean
    suspend fun insertFavorite(favorite: Favorite): Long
    suspend fun deleteFavoriteBySource(type: String, sourceId: Long)
    fun getCountByType(type: String): Flow<Int>
    fun getTotalCount(): Flow<Int>

    fun getFavoriteCount(): Flow<Int>
}
