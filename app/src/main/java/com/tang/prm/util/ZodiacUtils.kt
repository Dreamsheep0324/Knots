package com.tang.prm.util

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.tang.prm.R

enum class ZodiacElement { FIRE, EARTH, AIR, WATER }

enum class Zodiac(
    @DrawableRes val iconRes: Int,
    val displayName: String,
    val element: ZodiacElement,
    val color: Color,
    val startMonth: Int,
    val startDay: Int,
    val endMonth: Int,
    val endDay: Int
) {
    ARIES(R.drawable.ic_zodiac_aries, "白羊座", ZodiacElement.FIRE, Color(0xFFEF4444), 3, 21, 4, 19),
    TAURUS(R.drawable.ic_zodiac_taurus, "金牛座", ZodiacElement.EARTH, Color(0xFF10B981), 4, 20, 5, 20),
    GEMINI(R.drawable.ic_zodiac_gemini, "双子座", ZodiacElement.AIR, Color(0xFF3B82F6), 5, 21, 6, 21),
    CANCER(R.drawable.ic_zodiac_cancer, "巨蟹座", ZodiacElement.WATER, Color(0xFF6366F1), 6, 22, 7, 22),
    LEO(R.drawable.ic_zodiac_leo, "狮子座", ZodiacElement.FIRE, Color(0xFFF97316), 7, 23, 8, 22),
    VIRGO(R.drawable.ic_zodiac_virgo, "处女座", ZodiacElement.EARTH, Color(0xFF14B8A6), 8, 23, 9, 22),
    LIBRA(R.drawable.ic_zodiac_libra, "天秤座", ZodiacElement.AIR, Color(0xFF8B5CF6), 9, 23, 10, 23),
    SCORPIO(R.drawable.ic_zodiac_scorpio, "天蝎座", ZodiacElement.WATER, Color(0xFF7C3AED), 10, 24, 11, 22),
    SAGITTARIUS(R.drawable.ic_zodiac_sagittarius, "射手座", ZodiacElement.FIRE, Color(0xFFF59E0B), 11, 23, 12, 21),
    CAPRICORN(R.drawable.ic_zodiac_capricorn, "摩羯座", ZodiacElement.EARTH, Color(0xFF64748B), 12, 22, 1, 19),
    AQUARIUS(R.drawable.ic_zodiac_aquarius, "水瓶座", ZodiacElement.AIR, Color(0xFF0EA5E9), 1, 20, 2, 18),
    PISCES(R.drawable.ic_zodiac_pisces, "双鱼座", ZodiacElement.WATER, Color(0xFF06B6D4), 2, 19, 3, 20);

    val tagColor: Color
        get() = color.copy(alpha = 0.12f)
}

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
