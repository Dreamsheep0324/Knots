package com.tang.prm.data.mapper

import com.tang.prm.data.local.entity.CustomTypeEntity
import com.tang.prm.domain.model.CustomType

fun CustomTypeEntity.toDomain() = CustomType(
    id = id,
    category = category,
    name = name,
    key = key,
    color = color,
    icon = icon,
    sortOrder = sortOrder,
    isDefault = isDefault
)

fun CustomType.toEntity() = CustomTypeEntity(
    id = id,
    category = category,
    name = name,
    key = key,
    color = color,
    icon = icon,
    sortOrder = sortOrder,
    isDefault = isDefault
)
