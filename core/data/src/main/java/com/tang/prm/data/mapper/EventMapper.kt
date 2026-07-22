package com.tang.prm.data.mapper

import com.tang.prm.data.local.entity.EventEntity
import com.tang.prm.data.local.entity.EventLocationItemWithParticipants
import com.tang.prm.data.local.entity.EventWithParticipants
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType

fun Event.toEntity() = EventEntity(
    id = id, type = customTypeName ?: type.name, title = title, description = description,
    customTypeName = customTypeName,
    time = time, endTime = endTime, location = location, latitude = latitude, longitude = longitude,
    photos = photos, photosCount = photos.size,
    emotion = emotion, weather = weather, remarks = remarks,
    promise = promise, conversationSummary = conversationSummary, giftName = giftName,
    createdAt = createdAt, updatedAt = updatedAt
)

/**
 * MAP-Q-1 修复：抽取 type/customTypeName 推断为公共扩展函数。
 *
 * EventEntity.type 列既存枚举名又存自定义名，靠"是否匹配枚举"反推：
 * - 若 type 匹配某个 EventType 枚举名 → 该枚举 + customTypeName（可能为 null）
 * - 若 type 不匹配任何枚举名 → EventType.OTHER + type（作为自定义类型名）
 */
private fun String.toEventTypePair(customTypeName: String?): Pair<EventType, String?> =
    EventType.entries.find { it.name == this }?.let { it to customTypeName }
        ?: (EventType.OTHER to this)

fun EventWithParticipants.toDomain(): Event {
    val (eventType, customName) = event.type.toEventTypePair(event.customTypeName)
    return Event(
        id = event.id,
        type = eventType,
        customTypeName = customName,
        title = event.title,
        description = event.description,
        time = event.time,
        endTime = event.endTime,
        location = event.location,
        latitude = event.latitude,
        longitude = event.longitude,
        photos = event.photos, photosCount = event.photosCount,
        emotion = event.emotion,
        weather = event.weather,
        remarks = event.remarks,
        promise = event.promise,
        conversationSummary = event.conversationSummary,
        giftName = event.giftName,
        participants = participants.map { it.toDomainWithAttributes(emptyList()) },
        createdAt = event.createdAt,
        updatedAt = event.updatedAt
    )
}

fun List<EventWithParticipants>.toDomainList() = map { it.toDomain() }

/**
 * Map lightweight [EventLocationItemWithParticipants] (footprint projection) to domain [Event].
 * photos is empty (not loaded); use [Event.photosCount] for photo-count display.
 */
fun EventLocationItemWithParticipants.toDomain(): Event {
    val (eventType, customName) = event.type.toEventTypePair(event.customTypeName)
    return Event(
        id = event.id,
        type = eventType,
        customTypeName = customName,
        title = event.title,
        description = event.description,
        time = event.time,
        location = event.location,
        photos = emptyList(),
        photosCount = event.photosCount,
        emotion = event.emotion,
        weather = event.weather,
        participants = participants.map { it.toDomain() },
        createdAt = event.createdAt,
        updatedAt = event.updatedAt
    )
}
