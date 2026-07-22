package com.tang.prm.domain.model

/**
 * 提醒事项（A-10 文档化）。
 *
 * ## 角色定位
 * 天然的待办事项（实现 [Todoable]，无需 isTodo 标记），用于时间触发的提醒与待办。
 *
 * ## 聚合边界
 * 持有 `contactId/eventId/anniversaryId` 三个可空外键，跨 Contact/Event/Anniversary
 * 三个聚合根。这是历史设计，未来计划改为 `sourceType + sourceId` 与 [Favorite] 一致
 * （见 core-domain 审查报告 A-10 长期方案），当前保留以避免破坏 [com.tang.prm.domain.repository.ReminderReceiver]
 * 在 BroadcastReceiver 同步上下文中的查询。
 *
 * @see Todoable
 */
data class Reminder(
    val id: Long = 0,
    val contactId: Long? = null,
    val eventId: Long? = null,
    val anniversaryId: Long? = null,
    val type: String,
    val title: String,
    val content: String,
    val time: Long,
    override val isCompleted: Boolean = false,
    val isIgnored: Boolean = false,
    val repeatInterval: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) : Todoable
