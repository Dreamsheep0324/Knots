package com.tang.prm.domain.model

/**
 * 联系人之间的关系。
 *
 * 一对联系人之间最多一条 [ContactRelation]，由 (contactIdA, contactIdB) 唯一标识，
 * 约定 contactIdA < contactIdB 防止双向重复。
 *
 * 关系均为用户手动添加（图谱编辑模式拖拽连线）。
 */
data class ContactRelation(
    val id: Long,
    val contactIdA: Long,
    val contactIdB: Long,
    val relationTypeId: Long,
    val note: String?,
    val source: RelationSource,
    val createdAt: Long,
    val updatedAt: Long
)

enum class RelationSource { MANUAL }
