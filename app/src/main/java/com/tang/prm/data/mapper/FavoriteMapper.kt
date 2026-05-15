package com.tang.prm.data.mapper

import com.tang.prm.data.local.entity.FavoriteEntity
import com.tang.prm.domain.model.Favorite

fun FavoriteEntity.toDomain() = Favorite(
    id = id,
    sourceType = sourceType,
    sourceId = sourceId,
    title = title,
    description = description,
    createdAt = createdAt
)

fun Favorite.toEntity() = FavoriteEntity(
    id = id,
    sourceType = sourceType,
    sourceId = sourceId,
    title = title,
    description = description,
    createdAt = createdAt
)
