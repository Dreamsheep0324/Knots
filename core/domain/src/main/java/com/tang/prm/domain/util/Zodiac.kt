package com.tang.prm.domain.util

enum class Zodiac(
    val iconName: String,
    val displayName: String,
    val colorValue: Long,
    val startMonth: Int,
    val startDay: Int,
    val endMonth: Int,
    val endDay: Int
) {
    ARIES("aries", "白羊座", 0xFFEF4444, 3, 21, 4, 19),
    TAURUS("taurus", "金牛座", 0xFF10B981, 4, 20, 5, 20),
    GEMINI("gemini", "双子座", 0xFF3B82F6, 5, 21, 6, 21),
    CANCER("cancer", "巨蟹座", 0xFF6366F1, 6, 22, 7, 22),
    LEO("leo", "狮子座", 0xFFF97316, 7, 23, 8, 22),
    VIRGO("virgo", "处女座", 0xFF14B8A6, 8, 23, 9, 22),
    LIBRA("libra", "天秤座", 0xFF8B5CF6, 9, 23, 10, 23),
    SCORPIO("scorpio", "天蝎座", 0xFF7C3AED, 10, 24, 11, 22),
    SAGITTARIUS("sagittarius", "射手座", 0xFFF59E0B, 11, 23, 12, 21),
    CAPRICORN("capricorn", "摩羯座", 0xFF64748B, 12, 22, 1, 19),
    AQUARIUS("aquarius", "水瓶座", 0xFF0EA5E9, 1, 20, 2, 18),
    PISCES("pisces", "双鱼座", 0xFF06B6D4, 2, 19, 3, 20);

    val tagColorValue: Long
        get() = colorValue and 0x00FFFFFF or 0x1F000000.toLong()
}
