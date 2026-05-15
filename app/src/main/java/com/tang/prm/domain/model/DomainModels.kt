package com.tang.prm.domain.model

data class Contact(
    val id: Long = 0,
    val name: String,
    val avatar: String? = null,
    val nickname: String? = null,
    val gender: Int = 0,
    val birthday: Long? = null,
    val isLunarBirthday: Boolean = false,
    val knowingDate: Long? = null,
    val phone: String? = null,
    val email: String? = null,
    val city: String? = null,
    val address: String? = null,
    val education: String? = null,
    val company: String? = null,
    val jobTitle: String? = null,
    val industry: String? = null,
    val hobby: String? = null,
    val habit: String? = null,
    val diet: String? = null,
    val skill: String? = null,
    val mbti: String? = null,
    val spouseName: String? = null,
    val childrenCount: Int = 0,
    val childrenNames: String? = null,
    val introducer: String? = null,
    val relationshipLevel: Int = 0,
    val relationship: String? = null,
    val groupId: Long? = null,
    val intimacyScore: Int = 50,
    val lastInteractionTime: Long? = null,
    val customFields: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ContactGroup(
    val id: Long = 0,
    val name: String,
    val color: String? = null,
    val sortOrder: Int = 0
)

data class ContactTag(
    val id: Long = 0,
    val name: String,
    val color: String? = null
)

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
    val type: String = "",
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

enum class AnniversaryType(val displayName: String) {
    BIRTHDAY("生日"),
    ANNIVERSARY("纪念日"),
    HOLIDAY("节日")
}

data class Anniversary(
    val id: Long = 0,
    val contactId: Long,
    val name: String,
    val type: AnniversaryType,
    val date: Long,
    val isLunar: Boolean = false,
    val isRepeat: Boolean = true,
    val reminderDays: Int = 1,
    val remarks: String? = null,
    val contactName: String? = null,
    val contactAvatar: String? = null,
    val icon: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

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

data class Reminder(
    val id: Long = 0,
    val contactId: Long? = null,
    val eventId: Long? = null,
    val anniversaryId: Long? = null,
    val type: String,
    val title: String,
    val content: String,
    val time: Long,
    val isCompleted: Boolean = false,
    val isIgnored: Boolean = false,
    val repeatInterval: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class Favorite(
    val id: Long = 0,
    val sourceType: String,
    val sourceId: Long,
    val title: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
