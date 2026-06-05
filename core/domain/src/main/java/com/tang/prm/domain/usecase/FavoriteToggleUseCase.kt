package com.tang.prm.domain.usecase

import com.tang.prm.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoriteToggleUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(type: String, sourceId: Long, title: String, description: String?): Boolean {
        return favoriteRepository.toggleFavorite(type, sourceId, title, description)
    }

    fun getFavoriteIds(type: String): Flow<Set<Long>> {
        return favoriteRepository.getFavoritesByType(type).map { favList ->
            favList.map { it.sourceId }.toSet()
        }
    }

    fun isFavorite(type: String, sourceId: Long): Flow<Boolean> {
        return favoriteRepository.isFavorite(type, sourceId)
    }

    suspend fun deleteFavoriteBySource(type: String, sourceId: Long) {
        favoriteRepository.deleteFavoriteBySource(type, sourceId)
    }
}
