package com.tang.prm.domain.model

enum class AnniversaryType(val displayName: String) {
    BIRTHDAY("生日"),
    ANNIVERSARY("纪念日"),
    HOLIDAY("节日")
}

/**
 * 纪念日聚合根。
 *
 * B-16 修复（方案 B：文档化）：
 * [contactId] 为 null 表示「无关联联系人的纪念日」（如公共节日、个人里程碑）；
 * 非 null 表示绑定到具体联系人的纪念日（如某人生日）。
 * Repository 查询返回时必须保留 null 语义，**禁止默认填 0**——0L 会被 SQLite
 * 外键约束拒绝（core/data 审查 MAP-B-1 已暴露此坑）。
 *
 * C-8 修复：[reminderDays] 默认 1 天 = 纪念日提前 1 天提醒；与 Subscription 的
 * 3 天默认值不同（订阅金额较大需更早提醒），非 bug。
 */
data class Anniversary(
    val id: Long = 0,
    val contactId: Long? = null,
    val name: String,
    val type: AnniversaryType,
    val date: Long,
    val isRepeat: Boolean = true,
    val reminderDays: Int = DEFAULT_ANNIVERSARY_REMINDER_DAYS,
    val remarks: String? = null,
    val contactName: String? = null,
    val contactAvatar: String? = null,
    val icon: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        /** 纪念日提前提醒天数：1 天（与 Subscription 默认 3 天区分） */
        const val DEFAULT_ANNIVERSARY_REMINDER_DAYS = 1
    }
}
