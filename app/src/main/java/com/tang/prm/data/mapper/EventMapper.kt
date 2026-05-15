package com.tang.prm.data.mapper

// Alignment checklist for Event ↔ EventEntity:
// When adding/removing fields, ensure ALL of the following are updated:
// 1. Event (domain model)
// 2. EventEntity (Room entity)
// 3. EventMapper (toEntity / toDomain)
// 4. EventDao (queries)
// 5. Room migration (if entity schema changes)
// 6. UI forms (AddEventScreen, EventDetailScreen)

import com.tang.prm.data.local.entity.EventEntity
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.Contact

fun EventEntity.toDomain(participants: List<Contact> = emptyList()) = Event(
    id = id, type = type, title = title, description = description,
    time = time, endTime = endTime, location = location, latitude = latitude, longitude = longitude,
    photos = photos,
    emotion = emotion, weather = weather, amount = amount, remarks = remarks, promise = promise,
    conversationSummary = conversationSummary, giftName = giftName,
    participants = participants, createdAt = createdAt, updatedAt = updatedAt
)

fun Event.toEntity() = EventEntity(
    id = id, type = type, title = title, description = description,
    time = time, endTime = endTime, location = location, latitude = latitude, longitude = longitude,
    photos = photos, emotion = emotion, weather = weather, amount = amount, remarks = remarks,
    promise = promise, conversationSummary = conversationSummary, giftName = giftName,
    createdAt = createdAt, updatedAt = updatedAt
)
