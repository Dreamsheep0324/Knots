package com.tang.prm.data.local.dao

import androidx.room.ColumnInfo

/**
 * person_relations LEFT JOIN contacts 的查询结果。
 *
 * - 原始字段（pr.*）：来自 person_relations 表，保留 targetContactId / targetName / targetAvatar 原值
 * - JOIN 字段（c.name / c.avatar）：来自 contacts 表，仅在 targetContactId 非空且联系人存在时有值
 *
 * 合并规则（由 [com.tang.prm.data.repository.PersonRelationRepositoryImpl] 处理）：
 * - targetContactId 非空且 contactName 非空：使用 contactName / contactAvatar
 * - targetContactId 非空但 contactName 为空（联系人已删除）：兜底为"已删除联系人"
 * - targetContactId 为空（外部人物）：使用 targetName / targetAvatar
 */
data class PersonRelationWithTarget(
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "ownerContactId") val ownerContactId: Long,
    @ColumnInfo(name = "targetContactId") val targetContactId: Long?,
    @ColumnInfo(name = "targetName") val targetName: String?,
    @ColumnInfo(name = "targetAvatar") val targetAvatar: String?,
    @ColumnInfo(name = "relationTypeId") val relationTypeId: Long?,
    @ColumnInfo(name = "customLabel") val customLabel: String?,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long,
    @ColumnInfo(name = "contactName") val contactName: String?,
    @ColumnInfo(name = "contactAvatar") val contactAvatar: String?
)
