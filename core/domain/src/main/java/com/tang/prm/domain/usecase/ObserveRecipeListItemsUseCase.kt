package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.Recipe
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

/**
 * 食谱列表项（领域聚合模型）。
 *
 * 将 [Recipe] 与其点赞联系人名称聚合，供 UI 直接展示。
 */
data class RecipeListItem(
    val recipe: Recipe,
    val contactNames: List<String>
) {
    val id get() = recipe.id
    val title get() = recipe.title
    val description get() = recipe.description
    val cuisine get() = recipe.cuisine
    val cookingTime get() = recipe.cookingTime
    val servings get() = recipe.servings
    val photos get() = recipe.photos
    val tags get() = recipe.tags
    val rating get() = recipe.rating
}

/**
 * 食谱列表聚合数据。
 *
 * @param items 已关联联系人名称的食谱列表
 * @param contactMap 联系人 ID 到 [Contact] 的映射，供 UI 展示
 * @param availableTags 所有食谱去重排序后的标签集合
 */
data class RecipeListAggregateData(
    val items: List<RecipeListItem>,
    val contactMap: Map<Long, Contact>,
    val availableTags: List<String>
)

/**
 * 观察食谱列表项聚合数据。
 *
 * C-1 修复：将 RecipesViewModel 中的多 Repository 数据聚合逻辑（recipe + contact）
 * 提取到此 UseCase，使 ViewModel 不再直接依赖 Repository 的观察方法。
 *
 * 合并 2 个 Repository 的流：recipes、contacts，任一上游变更都会重新发射聚合结果。
 * 上游 Flow 加 [distinctUntilChanged] 遵循 Q-3 约定，避免无关变更触发重复聚合。
 */
class ObserveRecipeListItemsUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val contactRepository: ContactRepository
) {
    operator fun invoke(): Flow<RecipeListAggregateData> = combine(
        recipeRepository.getRecipeListItems().distinctUntilChanged(),
        contactRepository.getAllContacts().distinctUntilChanged()
    ) { recipes, contacts ->
        val contactMap = contacts.associateBy { it.id }
        val items = recipes.map { recipe ->
            RecipeListItem(
                recipe = recipe,
                contactNames = recipe.likedByContactIds.mapNotNull { id ->
                    contactMap[id]?.name
                }
            )
        }
        val availableTags = items.flatMap { it.tags }.distinct().sorted()
        RecipeListAggregateData(items, contactMap, availableTags)
    }
}
