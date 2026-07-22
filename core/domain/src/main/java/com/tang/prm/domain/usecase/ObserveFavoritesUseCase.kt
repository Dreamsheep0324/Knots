package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Favorite
import com.tang.prm.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 收藏列表观察 UseCase：统一封装收藏数据访问与筛选逻辑。
 *
 * - [observe] 不传参数时返回全部收藏
 * - [observe] 传入 sourceType 时按类型筛选
 * - [getFavoriteIds] 返回指定类型的收藏 sourceId 集合（用于 UI 标记"已收藏"状态）
 * - [isFavorite] 观察单条资源的收藏状态（Flow<Boolean>）
 *
 * 替代 FavoritesViewModel 直接依赖 FavoriteRepository 的反模式（A-2 修复）。
 * A-5 修复：原 [FavoriteToggleUseCase] 承担 toggle + getFavoriteIds + isFavorite + deleteFavoriteBySource
 * 四项职责，违反接口隔离。读路径（getFavoriteIds / isFavorite）下沉到此 UseCase，
 * 写路径（toggle / deleteFavoriteBySource）保留在 [FavoriteToggleUseCase]。
 *
 * 调用方依据读/写职责分别依赖对应 UseCase，依赖最小化。
 */
class ObserveFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    fun observe(sourceType: String? = null): Flow<List<Favorite>> {
        // P-3 修复：sourceType 非空时走 DB 层过滤（getFavoritesByType 走 SQL WHERE），
        // 替代原 getAllFavorites + Kotlin filter 的全表加载模式。
        return if (sourceType.isNullOrBlank()) {
            favoriteRepository.getAllFavorites()
        } else {
            favoriteRepository.getFavoritesByType(sourceType)
        }
    }

    /**
     * 返回指定 sourceType 下所有已收藏的 sourceId 集合。
     */
    fun getFavoriteIds(sourceType: String): Flow<Set<Long>> {
        return favoriteRepository.getFavoritesByType(sourceType).map { favList ->
            favList.map { it.sourceId }.toSet()
        }
    }

    /**
     * 观察单条资源的收藏状态。
     */
    fun isFavorite(sourceType: String, sourceId: Long): Flow<Boolean> {
        return favoriteRepository.isFavorite(sourceType, sourceId)
    }
}
