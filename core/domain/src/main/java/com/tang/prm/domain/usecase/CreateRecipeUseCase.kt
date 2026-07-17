package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.CookingStep
import com.tang.prm.domain.model.Ingredient
import com.tang.prm.domain.model.Recipe
import com.tang.prm.domain.model.RecipeDifficulty
import com.tang.prm.domain.repository.RecipeRepository
import javax.inject.Inject

/**
 * 食谱草稿：跨 UI 层传递的食谱输入数据
 *
 * 由 AddRecipeViewModel 从 AddRecipeUiState 转换而来，
 * 供 CreateRecipeUseCase 构造 Recipe 并持久化。
 */
data class RecipeDraft(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val cuisine: String = "",
    val difficulty: RecipeDifficulty = RecipeDifficulty.EASY,
    val cookingTime: String = "",
    val servings: String = "",
    val mainIngredients: List<Ingredient> = emptyList(),
    val subIngredients: List<Ingredient> = emptyList(),
    val seasoningIngredients: List<Ingredient> = emptyList(),
    val steps: List<CookingStep> = emptyList(),
    val photos: List<String> = emptyList(),
    val notes: String = "",
    val rating: Int = 0,
    val selectedContactIds: Set<Long> = emptySet(),
    val selectedTagNames: List<String> = emptyList()
)

/**
 * 创建/更新食谱 UseCase
 *
 * 将 AddRecipeViewModel.save() 中的业务逻辑下沉到 Domain 层：
 * - 合并三组食材（主料/辅料/调料）并过滤空行
 * - 过滤空白步骤
 * - 根据 id 判断新建（saveRecipeWithPhotos）或更新（updateRecipe）
 */
class CreateRecipeUseCase @Inject constructor(
    private val recipeRepository: RecipeRepository
) {
    /**
     * 保存食谱：新建时附带照片，编辑时更新 updatedAt
     */
    suspend fun save(draft: RecipeDraft) {
        val recipe = buildRecipe(draft)
        if (draft.id == 0L) {
            recipeRepository.saveRecipeWithPhotos(recipe, draft.photos)
        } else {
            recipeRepository.updateRecipe(recipe.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    /**
     * 从草稿构造 Recipe：合并食材、过滤空行、清理空字符串
     */
    private fun buildRecipe(draft: RecipeDraft): Recipe {
        val allIngredients = (draft.mainIngredients + draft.subIngredients + draft.seasoningIngredients)
            .filter { it.name.isNotBlank() }
        return Recipe(
            id = draft.id,
            title = draft.title.trim(),
            description = draft.description.ifBlank { null },
            cuisine = draft.cuisine.ifBlank { null },
            difficulty = draft.difficulty,
            cookingTime = draft.cookingTime.toIntOrNull(),
            servings = draft.servings.toIntOrNull(),
            ingredients = allIngredients,
            steps = draft.steps.filter { it.description.isNotBlank() },
            photos = draft.photos,
            notes = draft.notes.ifBlank { null },
            rating = draft.rating,
            likedByContactIds = draft.selectedContactIds.toList(),
            tags = draft.selectedTagNames
        )
    }
}
