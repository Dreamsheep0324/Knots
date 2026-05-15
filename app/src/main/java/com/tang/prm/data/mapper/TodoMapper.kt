package com.tang.prm.data.mapper

import com.tang.prm.data.local.entity.TodoItemEntity
import com.tang.prm.domain.model.TodoItem

fun TodoItemEntity.toDomain() = TodoItem(
    id = id, contactId = contactId, eventId = eventId, title = title,
    isCompleted = isCompleted, priority = priority, dueDate = dueDate, createdAt = createdAt
)

fun TodoItem.toEntity() = TodoItemEntity(
    id = id, contactId = contactId, eventId = eventId, title = title,
    isCompleted = isCompleted, priority = priority, dueDate = dueDate, createdAt = createdAt
)
