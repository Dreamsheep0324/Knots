package com.tang.prm.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 联系人关系实体。
 *
 * - (contactIdA, contactIdB) 唯一索引，约定 contactIdA < contactIdB 防止双向重复
 * - 联系人删除时级联删除（FK ON DELETE CASCADE），保证无孤儿 relation
 * - relationTypeId 关联 custom_types（CATEGORY=RELATIONSHIP）
 */
@Entity(
    tableName = "contact_relations",
    indices = [
        Index(value = ["contactIdA", "contactIdB"], unique = true),
        Index("contactIdB"),
        Index("relationTypeId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactIdA"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactIdB"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CustomTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["relationTypeId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ContactRelationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactIdA: Long,
    val contactIdB: Long,
    val relationTypeId: Long,
    val note: String?,
    val source: String,
    val createdAt: Long,
    val updatedAt: Long
)
