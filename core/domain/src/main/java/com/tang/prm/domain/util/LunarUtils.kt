package com.tang.prm.domain.util

import com.nlf.calendar.Lunar
import com.nlf.calendar.LunarYear
import com.nlf.calendar.Solar
import java.util.Calendar

object LunarUtils {
    data class SolarDate(val year: Int, val month: Int, val day: Int)

    fun lunarToSolar(year: Int, lunarMonth: Int, lunarDay: Int, isLeap: Boolean = false): Triple<Int, Int, Int>? {
        return try {
            val lunar = Lunar.fromYmd(year, lunarMonth, lunarDay)
            val solar = lunar.solar
            Triple(solar.year, solar.month, solar.day)
        } catch (e: Exception) {
            null
        }
    }

    fun solarToLunar(year: Int, month: Int, day: Int): SolarDate? {
        return try {
            val solar = Solar.fromYmd(year, month, day)
            val lunar = solar.lunar
            SolarDate(lunar.year, lunar.month, lunar.day)
        } catch (e: Exception) {
            null
        }
    }

    fun getJieQi(year: Int): List<Pair<String, Long>> {
        return try {
            val lunarYear = LunarYear.fromYear(year)
            val jieQiJulianDays = lunarYear.jieQiJulianDays
            val jieQiNames = Lunar.JIE_QI_IN_USE

            jieQiNames.indices.mapNotNull { i ->
                val julianDay = jieQiJulianDays.getOrNull(i) ?: return@mapNotNull null
                val name = jieQiNames[i]
                if (name.isNotEmpty()) {
                    val solar = Solar.fromJulianDay(julianDay)
                    val cal = Calendar.getInstance().apply {
                        set(solar.year, solar.month - 1, solar.day, 0, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    name to cal.timeInMillis
                } else null
            }.sortedBy { it.second }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getLunarDateDescription(timestamp: Long): String {
        return try {
            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
            val solar = Solar.fromYmd(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
            val lunar = solar.lunar
            "${lunar.monthInChinese}月${lunar.dayInChinese}"
        } catch (e: Exception) {
            ""
        }
    }
}
