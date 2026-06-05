package com.tang.prm.domain.util

import com.tang.prm.domain.util.LunarUtils.lunarToSolar
import com.tang.prm.domain.util.LunarUtils.solarToLunar
import com.tang.prm.domain.util.LunarUtils.getLunarDateDescription
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object LunarDateUtils {

    private val defaultZoneId = ZoneId.systemDefault()

    fun getNextLunarBirthdayDate(lunarMonth: Int, lunarDay: Int, isLeap: Boolean = false): Long {
        val today = LocalDate.now(defaultZoneId)
        val thisYear = today.year

        val thisYearSolar = lunarToSolar(thisYear, lunarMonth, lunarDay, isLeap)
        if (thisYearSolar != null) {
            val thisYearDate = LocalDate.of(thisYearSolar.first, thisYearSolar.second, thisYearSolar.third)
            if (!thisYearDate.isBefore(today)) {
                return thisYearDate.atStartOfDay(defaultZoneId).toInstant().toEpochMilli()
            }
        }

        val nextYearSolar = lunarToSolar(thisYear + 1, lunarMonth, lunarDay, isLeap)
        if (nextYearSolar != null) {
            val nextYearDate = LocalDate.of(nextYearSolar.first, nextYearSolar.second, nextYearSolar.third)
            return nextYearDate.atStartOfDay(defaultZoneId).toInstant().toEpochMilli()
        }

        return 0L
    }

    fun formatLunarDate(timestamp: Long): String {
        val lunarDesc = getLunarDateDescription(timestamp)
        val targetDate = Instant.ofEpochMilli(timestamp).atZone(defaultZoneId).toLocalDate()
        val lunarDate = solarToLunar(targetDate.year, targetDate.monthValue, targetDate.dayOfMonth)
        return if (lunarDate != null && lunarDesc.isNotEmpty()) {
            "农历${lunarDate.year}年${lunarDesc}"
        } else {
            DateUtils.formatDate(timestamp)
        }
    }

    fun formatLunarDateShort(timestamp: Long): String {
        val lunarDesc = getLunarDateDescription(timestamp)
        return if (lunarDesc.isNotEmpty()) {
            "农历$lunarDesc"
        } else {
            DateUtils.formatDate(timestamp)
        }
    }

    fun calculateLunarDaysInfo(targetMillis: Long): DateCalcUtils.DaysInfo {
        val today = LocalDate.now(defaultZoneId)
        val targetDate = Instant.ofEpochMilli(targetMillis).atZone(defaultZoneId).toLocalDate()

        val lunarDate = solarToLunar(targetDate.year, targetDate.monthValue, targetDate.dayOfMonth)
            ?: return DateCalcUtils.calculateDaysInfo(targetMillis)

        val lunarMonth = lunarDate.month
        val lunarDay = lunarDate.day

        val thisYearSolar = lunarToSolar(today.year, lunarMonth, lunarDay)
        val thisYearDate = thisYearSolar?.let { DateCalcUtils.safeDate(it.first, it.second, it.third) }

        val lastYearSolar = lunarToSolar(today.year - 1, lunarMonth, lunarDay)
        val lastYearDate = lastYearSolar?.let { DateCalcUtils.safeDate(it.first, it.second, it.third) }

        val nextYearSolar = lunarToSolar(today.year + 1, lunarMonth, lunarDay)
        val nextYearDate = nextYearSolar?.let { DateCalcUtils.safeDate(it.first, it.second, it.third) }

        val isPast = thisYearDate?.isBefore(today) ?: true

        val lastAnniversary = if (isPast) thisYearDate else lastYearDate
        val nextAnniversary = if (isPast) nextYearDate else thisYearDate

        val daysPassed = lastAnniversary?.let {
            java.time.temporal.ChronoUnit.DAYS.between(it, today).toInt()
        } ?: 0

        val daysUntil = nextAnniversary?.let {
            java.time.temporal.ChronoUnit.DAYS.between(today, it).toInt()
        } ?: 0

        return DateCalcUtils.DaysInfo(
            daysPassed = daysPassed,
            daysUntil = daysUntil,
            isPast = isPast
        )
    }
}
