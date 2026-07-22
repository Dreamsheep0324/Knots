package com.tang.prm.domain.usecase

import com.tang.prm.domain.repository.FavoriteRepository
import javax.inject.Inject

/**
 * 收藏写操作 UseCase：单一职责——写入收藏状态。
 *
 * A-5 修复：原 [FavoriteToggleUseCase] 承担 toggle + getFavoriteIds + isFavorite + deleteFavoriteBySource
 * 四项职责，违反接口隔离。读路径（getFavoriteIds / isFavorite）已下沉到 [ObserveFavoritesUseCase]，
 * 写路径（toggle / deleteFavoriteBySource）保留在此 UseCase。
 *
 * - [invoke] 切换收藏状态（已收藏→取消，未收藏→添加）
 * - [deleteFavoriteBySource] 按 sourceType + sourceId 删除收藏（用于相册等需要直接取消的场景）
 *
 * 收敛后调用方按读/写职责分别依赖对应 UseCase，依赖最小化。
 */
class FavoriteToggleUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    suspend operator fun invoke(type: String, sourceId: Long, title: String, description: String?): Boolean {
        return favoriteRepository.toggleFavorite(type, sourceId, title, description)
    }

    suspend fun deleteFavoriteBySource(type: String, sourceId: Long) {
        favoriteRepository.deleteFavoriteBySource(type, sourceId)
    }
}
