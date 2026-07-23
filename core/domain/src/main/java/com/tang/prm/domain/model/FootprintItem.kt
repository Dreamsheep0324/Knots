package com.tang.prm.domain.model

/**
 * 足迹项。
 *
 * B-4 修复：新增 [allContactIds] 保存全部参与者 ID，过滤时匹配任意参与者而非仅首参与者。
 * 原实现仅存 [contactId]（首参与者），导致按其他参与者过滤时丢失该事件。
 */
data class FootprintItem(
    val id: Long,
    val location: String,
    val date: Long,
    val eventType: String,
    val eventTitle: String,
    val contactId: Long?,
    val contactName: String?,
    val contactAvatar: String?,
    val description: String?,
    val weather: String?,
    val emotion: String?,
    val photoCount: Int,
    val allContactIds: List<Long> = emptyList()
)
