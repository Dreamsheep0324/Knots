package com.tang.prm.data.mapper

import com.tang.prm.data.local.entity.RecipeEntity
import com.tang.prm.data.local.entity.RecipeListItemWithRelations
import com.tang.prm.data.local.entity.RecipeTagEntity
import com.tang.prm.data.local.entity.RecipeWithContactsAndTags
import com.tang.prm.data.local.database.RecipeDataConverter
import com.tang.prm.domain.model.Ingredient
import com.tang.prm.domain.model.CookingStep
import com.tang.prm.domain.model.Recipe
import com.tang.prm.domain.model.RecipeDifficulty
import com.tang.prm.domain.model.RecipeTag

private val converter = RecipeDataConverter()

fun RecipeEntity.toDomain(
    contactIds: List<Long> = emptyList(),
    tagNames: List<String> = emptyList()
): Recipe = Recipe(
    id = id,
    title = title,
    description = description,
    cuisine = cuisine,
    difficulty = difficulty.toEnumOrDefault(RecipeDifficulty.EASY),
    cookingTime = cookingTime,
    servings = servings,
    ingredients = converter.toIngredientList(ingredients),
    steps = converter.toStepList(steps),
    photos = photos,
    notes = notes,
    rating = rating,
    isFavorite = isFavorite,
    likedByContactIds = contactIds,
    tags = tagNames,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Recipe.toEntity(): RecipeEntity = RecipeEntity(
    id = id,
    title = title,
    description = description,
    cuisine = cuisine,
    difficulty = difficulty.name,
    cookingTime = cookingTime,
    servings = servings,
    ingredients = converter.fromIngredientList(ingredients),
    steps = converter.fromStepList(steps),
    photos = photos,
    photosCount = photos.size,
    notes = notes,
    rating = rating,
    isFavorite = isFavorite,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun RecipeWithContactsAndTags.toDomain(): Recipe = recipe.toDomain(
    contactIds = contacts.map { it.id },
    tagNames = tags.map { it.name }
)

fun List<RecipeWithContactsAndTags>.toRecipeDomainList(): List<Recipe> = map { it.toDomain() }

/**
 * 列表页轻量投影 → Domain：ingredients/steps/notes 不在列表查询中加载，
 * 设为空值。列表页 UI 不消费这些字段，详情页通过 [RecipeRepository.getRecipeById] 重新加载完整数据。
 */
fun RecipeListItemWithRelations.toDomain(): Recipe = Recipe(
    id = recipe.id,
    title = recipe.title,
    description = recipe.description,
    cuisine = recipe.cuisine,
    difficulty = recipe.difficulty.toEnumOrDefault(RecipeDifficulty.EASY),
    cookingTime = recipe.cookingTime,
    servings = recipe.servings,
    ingredients = emptyList(),
    steps = emptyList(),
    photos = recipe.photos,
    notes = null,
    rating = recipe.rating,
    isFavorite = recipe.isFavorite,
    likedByContactIds = contacts.map { it.id },
    tags = tags.map { it.name },
    createdAt = recipe.createdAt,
    updatedAt = 0L
)

fun RecipeTagEntity.toDomain(): RecipeTag = RecipeTag(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt
)

fun RecipeTag.toEntity(): RecipeTagEntity = RecipeTagEntity(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt
)
