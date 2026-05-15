package com.tang.prm.data.mapper

// Alignment checklist for Circle ↔ CircleEntity:
// When adding/removing fields, ensure ALL of the following are updated:
// 1. Circle (domain model)
// 2. CircleEntity (Room entity)
// 3. CircleMapper (toEntity / toDomain)
// 4. CircleDao (queries)
// 5. Room migration (if entity schema changes)
// 6. UI forms (CircleScreen, CircleDetailScreen)

import com.tang.prm.data.local.entity.CircleEntity
import com.tang.prm.domain.model.Circle

fun CircleEntity.toDomain(memberIds: List<Long> = emptyList()) = Circle(
    id = id,
    name = name,
    description = description,
    color = color,
    icon = icon,
    waveform = waveform,
    memberIds = memberIds,
    parentCircleId = parentCircleId,
    intimacyThreshold = intimacyThreshold,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Circle.toEntity() = CircleEntity(
    id = id,
    name = name,
    description = description,
    color = color,
    icon = icon,
    waveform = waveform,
    parentCircleId = parentCircleId,
    intimacyThreshold = intimacyThreshold,
    sortOrder = sortOrder,
    createdAt = createdAt,
    updatedAt = updatedAt
)
