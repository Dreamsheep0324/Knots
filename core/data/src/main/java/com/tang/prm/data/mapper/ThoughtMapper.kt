package com.tang.prm.data.mapper

import com.tang.prm.data.local.entity.ThoughtEntity
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType

fun ThoughtEntity.toDomain() = Thought(
    id = id,
    contactId = contactId,
    content = content,
    type = ThoughtType.fromKey(type),
    isPrivate = isPrivate,
    isTodo = isTodo,
    isDone = isDone,
    dueDate = dueDate,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Thought.toEntity() = ThoughtEntity(
    id = id,
    contactId = contactId,
    content = content,
    type = type.key,
    isPrivate = isPrivate,
    isTodo = isTodo,
    isDone = isDone,
    dueDate = dueDate,
    createdAt = createdAt,
    updatedAt = updatedAt
)
