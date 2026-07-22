package com.tang.prm.data.mapper

import com.tang.prm.data.local.entity.ContactRelationEntity
import com.tang.prm.domain.model.ContactRelation
import com.tang.prm.domain.model.RelationSource

/**
 * ContactRelation entity ↔ domain 转换。
 *
 * source 字段在 entity 中存储为 String（枚举 name），在 domain 中为 [RelationSource]。
 * 若数据库出现未知 source 值（理论不应发生，但防御性处理），回退为 [RelationSource.MANUAL]，
 * 保证 UI 永远拿到合法枚举，避免崩溃。
 */
fun ContactRelationEntity.toDomain(): ContactRelation = ContactRelation(
    id = id,
    contactIdA = contactIdA,
    contactIdB = contactIdB,
    relationTypeId = relationTypeId,
    note = note,
    source = runCatching { RelationSource.valueOf(source) }.getOrDefault(RelationSource.MANUAL),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ContactRelation.toEntity(): ContactRelationEntity = ContactRelationEntity(
    id = id,
    contactIdA = contactIdA,
    contactIdB = contactIdB,
    relationTypeId = relationTypeId,
    note = note,
    source = source.name,
    createdAt = createdAt,
    updatedAt = updatedAt
)
