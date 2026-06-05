package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.util.Zodiac
import com.tang.prm.domain.util.ZodiacUtils
import javax.inject.Inject

data class ContactDisplayInfo(
    val zodiac: Zodiac?,
    val daysKnownText: String?,
    val intimacyLevel: String,
    val intimacyColorValue: Long
)

class GetContactDisplayInfoUseCase @Inject constructor() {
    operator fun invoke(contact: Contact): ContactDisplayInfo {
        val zodiac = ZodiacUtils.fromBirthday(contact.birthday)
        val daysKnownText = contact.knowingDate?.let { calculateDaysKnown(it) }
        val (level, colorValue) = calculateIntimacy(contact.intimacyScore)
        return ContactDisplayInfo(zodiac, daysKnownText, level, colorValue)
    }

    private fun calculateDaysKnown(knowingDate: Long): String {
        val diff = System.currentTimeMillis() - knowingDate
        val days = (diff / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        val years = days / 365
        val remainDays = days % 365
        return if (years > 0) "${years}年${remainDays}天" else "${days}天"
    }

    private fun calculateIntimacy(score: Int): Pair<String, Long> {
        return when {
            score <= 20 -> "初识" to 0xFF64748B
            score <= 40 -> "相识" to 0xFF3B82F6
            score <= 60 -> "朋友" to 0xFF10B981
            score <= 80 -> "密友" to 0xFFF97316
            else -> "至亲" to 0xFFEF4444
        }
    }
}
