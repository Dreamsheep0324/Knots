package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.IntimacyTier
import com.tang.prm.domain.util.DateUtils
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
        val tier = IntimacyTier.of(contact.intimacyScore)
        return ContactDisplayInfo(zodiac, daysKnownText, tier.label, tier.colorValue)
    }

    private fun calculateDaysKnown(knowingDate: Long): String {
        val diff = System.currentTimeMillis() - knowingDate
        val days = (diff / DateUtils.MILLIS_PER_DAY).toInt().coerceAtLeast(0)
        val years = days / 365
        val remainDays = days % 365
        return if (years > 0) "${years}年${remainDays}天" else "${days}天"
    }
}
