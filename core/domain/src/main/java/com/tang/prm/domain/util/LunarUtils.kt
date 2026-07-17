package com.tang.prm.domain.util

import com.nlf.calendar.Lunar
import com.nlf.calendar.Solar
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

object LunarUtils {
    data class SolarDate(val year: Int, val month: Int, val day: Int)

    // 缓存：同一天内 solarToLunar 结果不变，避免重复查表计算。
    // 使用 ConcurrentHashMap 保证多协程并发读写的线程安全；
    // 农历转换是确定性函数，getOrPut 偶发重复计算结果一致，无需 computeIfAbsent 的强原子。
    private val solarToLunarCache = ConcurrentHashMap<String, SolarDate?>()
    private val lunarToSolarCache = ConcurrentHashMap<String, Triple<Int, Int, Int>?>()

    /**
     * 农历转公历。
     *
     * @param year 农历年
     * @param lunarMonth 农历月（1-12）
     * @param lunarDay 农历日（1-30）
     * @param isLeap 是否为闰月。lunar-javascript 库约定闰月用负数表示（如 -2 表示闰二月），
     *               此处将 [lunarMonth] 取负后传入 [Lunar.fromYmd]。
     */
    fun lunarToSolar(year: Int, lunarMonth: Int, lunarDay: Int, isLeap: Boolean = false): Triple<Int, Int, Int>? {
        val key = "$year-$lunarMonth-$lunarDay-$isLeap"
        return lunarToSolarCache.getOrPut(key) {
            try {
                // 闰月用负数表示：lunar-javascript 库约定
                val effectiveMonth = if (isLeap) -lunarMonth else lunarMonth
                val lunar = Lunar.fromYmd(year, effectiveMonth, lunarDay)
                val solar = lunar.solar
                Triple(solar.year, solar.month, solar.day)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun solarToLunar(year: Int, month: Int, day: Int): SolarDate? {
        val key = "$year-$month-$day"
        return solarToLunarCache.getOrPut(key) {
            try {
                val solar = Solar.fromYmd(year, month, day)
                val lunar = solar.lunar
                SolarDate(lunar.year, lunar.month, lunar.day)
            } catch (e: Exception) {
                null
            }
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
