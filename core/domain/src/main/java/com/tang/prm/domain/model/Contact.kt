package com.tang.prm.domain.model

import com.tang.prm.domain.util.Zodiac
import com.tang.prm.domain.util.ZodiacUtils

const val DEFAULT_INTIMACY_SCORE = 50

enum class Gender(val value: Int) {
    UNKNOWN(0), MALE(1), FEMALE(2);
    companion object {
        fun fromValue(value: Int) = entries.find { it.value == value } ?: UNKNOWN
    }
}

data class Contact(
    val id: Long = 0,
    val name: String,
    val avatar: String? = null,
    val nickname: String? = null,
    val gender: Gender = Gender.UNKNOWN,
    val birthday: Long? = null,
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
    val relationship: String? = null,
    val groupId: Long? = null,
    val intimacyScore: Int = DEFAULT_INTIMACY_SCORE,
    val lastInteractionTime: Long? = null,
    val customFields: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // M-1 富模型：将散落在 util 与 UI 层的内在行为内聚到模型本身
    val intimacyTier: IntimacyTier get() = IntimacyTier.of(intimacyScore)
    val isDefaultIntimacy: Boolean get() = intimacyScore == DEFAULT_INTIMACY_SCORE
    val zodiac: Zodiac? get() = ZodiacUtils.fromBirthday(birthday)
    val hasAvatar: Boolean get() = !avatar.isNullOrBlank()
}

data class ContactGroup(
    val id: Long = 0,
    val name: String,
    val color: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class ContactTag(
    val id: Long = 0,
    val name: String,
    val color: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
