package com.tang.prm.domain.model

enum class RecipeDifficulty(val displayName: String) {
    EASY("简单"), MEDIUM("中等"), HARD("困难")
}

/** 食材分组类型 */
enum class IngredientGroupType(val displayName: String) {
    MAIN("主料"),
    SUB("辅料"),
    SEASONING("调料")
}

data class Ingredient(
    val name: String,
    val amount: String = "",
    val unit: String = "",
    val groupType: IngredientGroupType = IngredientGroupType.MAIN,
    val isScalable: Boolean = true
)

data class CookingStep(
    val order: Int,
    val description: String,
    val image: String? = null,
    val timerSeconds: Int? = null
)

data class Recipe(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val cuisine: String? = null,
    val difficulty: RecipeDifficulty = RecipeDifficulty.EASY,
    val cookingTime: Int? = null,
    val servings: Int? = null,
    val ingredients: List<Ingredient> = emptyList(),
    val steps: List<CookingStep> = emptyList(),
    val photos: List<String> = emptyList(),
    val notes: String? = null,
    val rating: Int = 0,
    val isFavorite: Boolean = false,
    val likedByContactIds: List<Long> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class RecipeTag(
    val id: Long = 0,
    val name: String,
    val color: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
