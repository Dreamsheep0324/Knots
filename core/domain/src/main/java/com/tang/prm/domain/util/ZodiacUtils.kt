package com.tang.prm.domain.util

object ZodiacUtils {

    fun fromBirthday(birthday: Long?): Zodiac? {
        if (birthday == null) return null
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = birthday
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return fromMonthDay(month, day)
    }

    fun fromMonthDay(month: Int, day: Int): Zodiac {
        for (z in Zodiac.entries) {
            if (z.startMonth == z.endMonth) {
                if (month == z.startMonth && day in z.startDay..z.endDay) return z
            } else if (z.startMonth < z.endMonth) {
                if ((month == z.startMonth && day >= z.startDay) ||
                    (month == z.endMonth && day <= z.endDay)
                ) return z
            } else {
                if ((month == z.startMonth && day >= z.startDay) ||
                    (month == z.endMonth && day <= z.endDay)
                ) return z
            }
        }
        return Zodiac.CAPRICORN
    }
}
