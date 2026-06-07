package com.tang.prm.data.mapper

import com.tang.prm.data.local.entity.GiftEntity
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.model.GiftType

fun GiftEntity.toDomain() = Gift(
    id = id,
    contactId = contactId,
    giftName = giftName,
    giftType = giftType.toEnumByKeyOrDefault(GiftType::key, GiftType.OTHER),
    date = date,
    isSent = isSent,
    amount = amount,
    occasion = occasion,
    description = description,
    location = location,
    photos = photos,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Gift.toEntity() = GiftEntity(
    id = id,
    contactId = contactId,
    giftName = giftName,
    giftType = giftType.key,
    date = date,
    isSent = isSent,
    amount = amount,
    occasion = occasion,
    description = description,
    location = location,
    photos = photos,
    photosCount = photos.size,
    createdAt = createdAt,
    updatedAt = updatedAt
)
