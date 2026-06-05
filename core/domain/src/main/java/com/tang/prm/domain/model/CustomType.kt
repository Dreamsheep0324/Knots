package com.tang.prm.domain.model

data class CustomType(
    val id: Long = 0,
    val category: String,
    val name: String,
    val key: String = "",
    val color: String? = null,
    val icon: String? = null,
    val sortOrder: Int = 0,
    val isDefault: Boolean = false
)

object CustomCategories {
    const val EVENT_TYPE = "EVENT_TYPE"
    const val EMOTION = "EMOTION"
    const val WEATHER = "WEATHER"
    const val RELATIONSHIP = "RELATIONSHIP"
    const val ANNIVERSARY_TYPE = "ANNIVERSARY_TYPE"
    const val EDUCATION = "EDUCATION"
    const val HOBBY = "HOBBY"
    const val HABIT = "HABIT"
    const val DIET = "DIET"
    const val SKILL = "SKILL"
}
