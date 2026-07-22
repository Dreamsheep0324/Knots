package com.tang.prm.data.mapper

import com.tang.prm.data.local.dao.PersonRelationWithTarget
import com.tang.prm.data.local.entity.PersonRelationEntity
import com.tang.prm.domain.model.PersonRelation

/**
 * PersonRelation entity ↔ domain 转换。
 *
 * - [PersonRelationEntity.toDomain]：直转，不处理 JOIN 字段（targetName/targetAvatar 保持原值）
 * - [PersonRelationWithTarget.toDomain]：合并 JOIN 结果，处理联系人已删除的降级兜底
 */
fun PersonRelationEntity.toDomain(): PersonRelation = PersonRelation(
    id = id,
    ownerContactId = ownerContactId,
    targetContactId = targetContactId,
    targetName = targetName,
    targetAvatar = targetAvatar,
    relationTypeId = relationTypeId,
    customLabel = customLabel,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * 将 JOIN 查询结果转为 domain 模型，处理三种情况：
 * 1. 外部人物（targetContactId = null）：直接用 entity 的 targetName/targetAvatar
 * 2. App 联系人且存在（contactName 非空）：用 contactName/contactAvatar
 * 3. App 联系人但已删除（contactName 为空）：兜底为"已删除联系人"
 */
fun PersonRelationWithTarget.toDomain(): PersonRelation {
    val (resolvedName, resolvedAvatar) = if (targetContactId != null) {
        // App 联系人：优先用 JOIN 结果，联系人已删除时兜底
        (contactName ?: "已删除联系人") to contactAvatar
    } else {
        // 外部人物：用 entity 原值
        targetName to targetAvatar
    }
    return PersonRelation(
        id = id,
        ownerContactId = ownerContactId,
        targetContactId = targetContactId,
        targetName = resolvedName,
        targetAvatar = resolvedAvatar,
        relationTypeId = relationTypeId,
        customLabel = customLabel,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Domain → Entity 转换。
 *
 * 互斥约束：App 联系人（targetContactId != null）的 targetName/targetAvatar 必须为空，
 * 由数据库 LEFT JOIN contacts 在读取时填充。Domain 层的 targetName 可作为冗余缓存
 * 用于 UI 显示，但落库前必须清空以符合表设计约束。
 */
fun PersonRelation.toEntity(): PersonRelationEntity {
    val isAppContact = targetContactId != null
    return PersonRelationEntity(
        id = id,
        ownerContactId = ownerContactId,
        targetContactId = targetContactId,
        targetName = if (isAppContact) null else targetName,
        targetAvatar = if (isAppContact) null else targetAvatar,
        relationTypeId = relationTypeId,
        customLabel = customLabel,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
