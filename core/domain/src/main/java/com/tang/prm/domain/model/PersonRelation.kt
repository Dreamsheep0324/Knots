package com.tang.prm.domain.model

/**
 * 某联系人与其他人之间的关系。
 *
 * 与 [ContactRelation] 的差异：
 * - 语义：本类是"某人的社交关系"（单向，owner → target），ContactRelation 是"联系人之间"（双向对称）
 * - 客体可为外部人物：[targetContactId] 与 [targetName] 互斥（一空一非空）
 * - 用途：人物详情/编辑界面的社交关系展示，不参与图谱可视化
 *
 * 字段互斥约束（两层校验）：
 * - **域层 init 校验**：[targetContactId] 与 [targetName] 至少一个非空（防止构造无目标的关系）
 * - **持久化层校验**：[com.tang.prm.data.mapper.PersonRelationMapper.toEntity] 会在落库前
 *   清空 App 联系人（[targetContactId] 非空）的 [targetName] / [targetAvatar]
 *   域模型的 [targetName] 可同时非空（JOIN 填充或 UI 层预填），仅持久化时强制互斥
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
    init {
        // B-7 修复：运行时校验。原仅靠 KDoc 约束，data 层误填两者皆空时无任何提示。
        // 注意：不强制严格 XOR——JOIN 结果与 AddContactViewModel 构造时会同时填充
        // targetContactId 和 targetName（域模型层允许 targetName 作为冗余显示字段）。
        // 仅校验"至少一个非空"，捕获真正无效的构造（两者皆空）。
        require(targetContactId != null || !targetName.isNullOrBlank()) {
            "targetContactId 与 targetName 至少需要一个非空"
        }
    }

    /** 渲染时使用的关系词：customLabel 优先，其次 typeName，最后兜底"其他" */
    fun resolveLabel(typeName: String?): String =
        customLabel?.takeIf { it.isNotBlank() } ?: typeName ?: "其他"

    /** 是否为 App 内联系人 */
    val isAppContact: Boolean get() = targetContactId != null

    /** 渲染时使用的姓名：targetName 兜底"未知" */
    fun resolveDisplayName(): String = targetName?.takeIf { it.isNotBlank() } ?: "未知"
}
