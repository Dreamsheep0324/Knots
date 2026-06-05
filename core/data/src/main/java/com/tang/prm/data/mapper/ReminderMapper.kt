package com.tang.prm.data.mapper

import com.tang.prm.data.local.entity.ReminderEntity
import com.tang.prm.domain.model.Reminder

fun ReminderEntity.toDomain() = Reminder(
    id = id, contactId = contactId, eventId = eventId, anniversaryId = anniversaryId,
    type = type, title = title, content = content, time = time,
    isCompleted = isCompleted, isIgnored = isIgnored, repeatInterval = repeatInterval, createdAt = createdAt
)

fun Reminder.toEntity() = ReminderEntity(
    id = id, contactId = contactId, eventId = eventId, anniversaryId = anniversaryId,
    type = type, title = title, content = content, time = time,
    isCompleted = isCompleted, isIgnored = isIgnored, repeatInterval = repeatInterval, createdAt = createdAt
)
