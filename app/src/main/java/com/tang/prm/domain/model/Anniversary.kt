package com.tang.prm.domain.model

enum class AnniversaryType(val displayName: String) {
    BIRTHDAY("生日"),
    ANNIVERSARY("纪念日"),
    HOLIDAY("节日")
}

data class Anniversary(
    val id: Long = 0,
    val contactId: Long? = null,
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
