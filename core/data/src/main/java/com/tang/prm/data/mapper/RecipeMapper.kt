package com.tang.prm.data.mapper

import com.tang.prm.data.local.entity.RecipeEntity
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
