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
 *
 * 替代 FavoritesViewModel 直接依赖 FavoriteRepository 的反模式（A-2 修复）。
 */
class ObserveFavoritesUseCase @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) {
    fun observe(sourceType: String? = null): Flow<List<Favorite>> {
        val all = favoriteRepository.getAllFavorites()
        return if (sourceType.isNullOrBlank()) {
            all
        } else {
            all.map { list -> list.filter { it.sourceType == sourceType } }
        }
    }
}
