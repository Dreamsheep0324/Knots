package com.tang.prm.domain.model

/**
 * 独立待办事项（A-10 文档化）。
 *
 * ## 角色定位
 * 天然的待办事项，不依赖时间触发，用于用户主动记录的待办清单。
 *
 * ## 聚合边界
 * 持有 `contactId/eventId` 两个可空外键，归属 Contact/Event 聚合。
 */
data class TodoItem(
    val id: Long = 0,
    val contactId: Long? = null,
    val eventId: Long? = null,
    val title: String,
    val isCompleted: Boolean = false,
    val priority: Int = 0,
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
