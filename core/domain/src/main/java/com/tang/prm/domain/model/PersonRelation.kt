package com.tang.prm.domain.model

/**
 * 某联系人与其他人之间的关系。
 *
 * 与 [ContactRelation] 的差异：
 * - 语义：本类是"某人的社交关系"（单向，owner → target），ContactRelation 是"联系人之间"（双向对称）
 * - 客体可为外部人物：[targetContactId] 与 [targetName] 互斥（一空一非空）
 * - 用途：人物详情/编辑界面的社交关系展示，不参与图谱可视化
 *
 * 字段互斥约束：
 * - [targetContactId] 非空时，目标为 App 内联系人，[targetName] / [targetAvatar] 应为空
 * - [targetName] 非空时，目标为外部人物，[targetContactId] 应为空
 *
 * 关系词渲染优先级：[customLabel] > [relationTypeId] 对应的 CustomType.name > "其他"
 *
 * @property ownerContactId 主体联系人 ID（必填）
 * @property targetContactId 客体联系人 ID（与 targetName 互斥）
 * @property targetName 客体姓名（外部人物时填；App 联系人时由 JOIN 填充）
 * @property targetAvatar 客体头像路径（外部人物可选；App 联系人时由 JOIN 填充）
 * @property relationTypeId 关系类型 ID（关联 custom_types，CATEGORY=PERSON_RELATION）
 * @property customLabel 自由文本关系词，非空时优先于 relationTypeId 渲染
 * @property note 备注（如"本科同班四年"）
 */
data class PersonRelation(
    val id: Long,
    val ownerContactId: Long,
    val targetContactId: Long?,
    val targetName: String?,
    val targetAvatar: String?,
    val relationTypeId: Long?,
    val customLabel: String?,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long
) {
    /** 渲染时使用的关系词：customLabel 优先，其次 typeName，最后兜底"其他" */
    fun resolveLabel(typeName: String?): String =
        customLabel?.takeIf { it.isNotBlank() } ?: typeName ?: "其他"

    /** 是否为 App 内联系人 */
    val isAppContact: Boolean get() = targetContactId != null

    /** 渲染时使用的姓名：targetName 兜底"未知" */
    fun resolveDisplayName(): String = targetName?.takeIf { it.isNotBlank() } ?: "未知"
}
