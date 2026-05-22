package com.tang.prm.data.mapper

import com.tang.prm.data.local.entity.EventEntity
import com.tang.prm.data.local.entity.EventWithParticipants
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.model.Contact

fun EventEntity.toDomain(participants: List<Contact> = emptyList()) = Event(
    id = id, type = EventType.entries.find { it.name == type } ?: EventType.OTHER,
    customTypeName = if (EventType.entries.none { it.name == type }) type else customTypeName,
    title = title, description = description,
    time = time, endTime = endTime, location = location, latitude = latitude, longitude = longitude,
    photos = photos,
    emotion = emotion, weather = weather, amount = amount, remarks = remarks, promise = promise,
    conversationSummary = conversationSummary, giftName = giftName,
    participants = participants, createdAt = createdAt, updatedAt = updatedAt
)

fun Event.toEntity() = EventEntity(
    id = id, type = customTypeName ?: type.name, title = title, description = description,
    customTypeName = customTypeName,
    time = time, endTime = endTime, location = location, latitude = latitude, longitude = longitude,
    photos = photos, emotion = emotion, weather = weather, amount = amount, remarks = remarks,
    promise = promise, conversationSummary = conversationSummary, giftName = giftName,
    createdAt = createdAt, updatedAt = updatedAt
)

fun EventWithParticipants.toDomain() = Event(
    id = event.id,
    type = EventType.entries.find { it.name == event.type } ?: EventType.OTHER,
    customTypeName = if (EventType.entries.none { it.name == event.type }) event.type else event.customTypeName,
    title = event.title,
    description = event.description,
    time = event.time,
    endTime = event.endTime,
    location = event.location,
    latitude = event.latitude,
    longitude = event.longitude,
    photos = event.photos,
    emotion = event.emotion,
    weather = event.weather,
    amount = event.amount,
    remarks = event.remarks,
    promise = event.promise,
    conversationSummary = event.conversationSummary,
    giftName = event.giftName,
    participants = participants.map { it.toDomain() },
    createdAt = event.createdAt,
    updatedAt = event.updatedAt
)

fun List<EventWithParticipants>.toDomainList() = map { it.toDomain() }
