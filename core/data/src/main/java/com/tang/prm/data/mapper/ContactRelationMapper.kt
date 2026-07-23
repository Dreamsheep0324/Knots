package com.tang.prm.data.mapper

import com.tang.prm.data.local.entity.ContactRelationEntity
import com.tang.prm.domain.model.ContactRelation

/**
 * ContactRelation entity ↔ domain 转换。
 */
fun ContactRelationEntity.toDomain(): ContactRelation = ContactRelation(
    id = id,
    contactIdA = contactIdA,
    contactIdB = contactIdB,
    relationTypeId = relationTypeId,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ContactRelation.toEntity(): ContactRelationEntity = ContactRelationEntity(
    id = id,
    contactIdA = contactIdA,
    contactIdB = contactIdB,
    relationTypeId = relationTypeId,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt
)
