package com.tang.prm.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 人物关系实体：记录某联系人与其他人之间的关系。
 *
 * 与 [ContactRelationEntity] 的差异：
 * - 语义：本表是"某人的社交关系"（单向），ContactRelationEntity 是"联系人之间"（双向对称）
 * - 客体可为外部人物：[targetContactId] 与 [targetName] 互斥（一空一非空）
 * - 外键策略：客体删除时 SET NULL（保留记录并降级为外部人物），而非 CASCADE
 * - 用途：人物详情/编辑界面的社交关系展示，不参与图谱可视化
 *
 * 字段互斥约束由 [com.tang.prm.data.repository.PersonRelationRepositoryImpl] 在写入前校验，
 * Room 无法直接表达 CHECK 约束。
 *
 * @property ownerContactId 主体联系人 ID（必填，删除时 CASCADE）
 * @property targetContactId 客体联系人 ID（与 targetName 互斥；删除时 SET NULL）
 * @property targetName 客体姓名（外部人物时填；App 联系人时为空，由 JOIN 提供）
 * @property targetAvatar 客体头像路径（仅外部人物使用）
 * @property relationTypeId 关系类型 ID（关联 custom_types，CATEGORY=PERSON_RELATION；删除时 SET NULL）
 * @property customLabel 自由文本关系词，非空时优先于 relationTypeId 渲染
 * @property note 备注
 */
@Entity(
    tableName = "person_relations",
    indices = [
        Index("ownerContactId"),
        Index("targetContactId"),
        Index("relationTypeId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerContactId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetContactId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = CustomTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["relationTypeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class PersonRelationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerContactId: Long,
    val targetContactId: Long?,
    val targetName: String?,
    val targetAvatar: String?,
    val relationTypeId: Long?,
    val customLabel: String?,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long
)
