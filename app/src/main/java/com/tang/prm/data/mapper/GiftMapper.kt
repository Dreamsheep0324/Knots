package com.tang.prm.data.mapper

import com.tang.prm.data.local.entity.GiftEntity
import com.tang.prm.domain.model.Gift

fun GiftEntity.toDomain() = Gift(
    id = id,
    contactId = contactId,
    giftName = giftName,
    giftType = giftType,
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
    giftType = giftType,
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
