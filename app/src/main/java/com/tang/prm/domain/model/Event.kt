package com.tang.prm.domain.model

enum class EventType(val displayName: String) {
    MEETUP("见面"),
    DINING("聚餐"),
    TRAVEL("旅游"),
    CALL("通话"),
    GIFT_SENT("送礼"),
    GIFT_RECEIVED("收礼"),
    MONEY_LEND("借出"),
    MONEY_BORROW("借入"),
    CONVERSATION("对话记录"),
    OTHER("其他")
}

data class Event(
    val id: Long = 0,
    val type: EventType = EventType.OTHER,
    val customTypeName: String? = null,
    val title: String,
    val description: String? = null,
    val time: Long,
    val endTime: Long? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photos: List<String> = emptyList(),
    val emotion: String? = null,
    val weather: String? = null,
    val amount: Double? = null,
    val remarks: String? = null,
    val promise: String? = null,
    val conversationSummary: String? = null,
    val giftName: String? = null,
    val participants: List<Contact> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
