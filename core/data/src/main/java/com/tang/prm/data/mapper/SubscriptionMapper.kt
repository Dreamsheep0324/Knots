package com.tang.prm.data.mapper

import com.tang.prm.data.local.entity.SubscriptionEntity
import com.tang.prm.data.mapper.toEnumOrDefault
import com.tang.prm.domain.model.Subscription
import com.tang.prm.domain.model.SubscriptionCycle
import com.tang.prm.domain.model.SubscriptionStatus

fun SubscriptionEntity.toDomain() = Subscription(
    id = id,
    name = name,
    icon = icon,
    category = category,
    price = price,
    currency = currency,
    cycle = cycle.toEnumOrDefault(SubscriptionCycle.MONTHLY),
    startDate = startDate,
    nextBillingDate = nextBillingDate,
    status = status.toEnumOrDefault(SubscriptionStatus.ACTIVE),
    reminderDays = reminderDays,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    timezone = timezone
)

fun Subscription.toEntity() = SubscriptionEntity(
    id = id,
    name = name,
    icon = icon,
    category = category,
    price = price,
    currency = currency,
    cycle = cycle.name,
    startDate = startDate,
    nextBillingDate = nextBillingDate,
    status = status.name,
    reminderDays = reminderDays,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    timezone = timezone
)
