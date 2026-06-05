package com.tang.prm.engine.divination.core

import com.tang.prm.engine.divination.model.GanZhiInfo
import com.nlf.calendar.Solar
import com.nlf.calendar.EightChar
import kotlin.math.abs

object GanZhiCalculator {

    private val DIZHI = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")

    fun fromSolar(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0): GanZhiInfo {
        val solar = Solar.fromYmdHms(year, month, day, hour, minute, 0)
        val lunar = solar.lunar
        val eightChar = EightChar.fromLunar(lunar)
        return GanZhiInfo(
            year = eightChar.year,
            month = eightChar.month,
            day = eightChar.day,
            hour = eightChar.time
        )
    }

    fun getLunarMonthDay(year: Int, month: Int, day: Int): Pair<Int, Int> {
        val solar = Solar.fromYmd(year, month, day)
        val lunar = solar.lunar
        return Pair(abs(lunar.month), lunar.day)
    }

    fun getDayGan(year: Int, month: Int, day: Int): String {
        val solar = Solar.fromYmd(year, month, day)
        val lunar = solar.lunar
        val eightChar = EightChar.fromLunar(lunar)
        return eightChar.dayGan
    }

    fun getDayGanZhi(year: Int, month: Int, day: Int): String {
        val solar = Solar.fromYmd(year, month, day)
        val lunar = solar.lunar
        val eightChar = EightChar.fromLunar(lunar)
        return eightChar.day
    }

    fun getHourZhi(hour: Int): String {
        return DIZHI[getHourZhiIndex0(hour)]
    }

    fun getHourZhiIndex(hour: Int): Int {
        return getHourZhiIndex0(hour) + 1
    }

    fun getYearZhiIndex(yearGanZhi: String): Int {
        val zhi = yearGanZhi.substring(1, 2)
        return DIZHI.indexOf(zhi) + 1
    }

    fun getTimeZhiIndex(ganZhiHour: String): Int {
        val zhi = ganZhiHour.substring(1, 2)
        return DIZHI.indexOf(zhi) + 1
    }

    private fun getHourZhiIndex0(hour: Int): Int = when (hour) {
        in 23..24, 0 -> 0
        in 1..2 -> 1
        in 3..4 -> 2
        in 5..6 -> 3
        in 7..8 -> 4
        in 9..10 -> 5
        in 11..12 -> 6
        in 13..14 -> 7
        in 15..16 -> 8
        in 17..18 -> 9
        in 19..20 -> 10
        in 21..22 -> 11
        else -> 0
    }
}
