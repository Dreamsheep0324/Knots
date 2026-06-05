package com.tang.prm.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateCalcUtils {

    private val defaultZoneId = ZoneId.systemDefault()
    private val shortDateFormatter = DateTimeFormatter.ofPattern("MM-dd", Locale.getDefault())

    fun safeDate(year: Int, month: Int, day: Int): LocalDate {
        if (month == 2 && day == 29 && !java.time.Year.isLeap(year.toLong())) {
            return LocalDate.of(year, 2, 28)
        }
        return LocalDate.of(year, month, day)
    }

    data class DaysInfo(
        val daysPassed: Int,
        val daysUntil: Int,
        val isPast: Boolean
    )

    data class BirthdayInfo(
        val thisYearDate: String,
        val daysUntil: Int,
        val isPast: Boolean,
        val displayText: String
    )

    fun getTodayStart(): Long =
        LocalDate.now(defaultZoneId).atStartOfDay(defaultZoneId).toInstant().toEpochMilli()

    fun calculateDaysUntil(targetMillis: Long): Int {
        val today = LocalDate.now(defaultZoneId)
        val targetDate = Instant.ofEpochMilli(targetMillis).atZone(defaultZoneId).toLocalDate()
        val targetMonth = targetDate.monthValue
        val targetDay = targetDate.dayOfMonth
        val currentYear = today.year

        var nextDate = safeDate(currentYear, targetMonth, targetDay)
        if (nextDate.isBefore(today)) {
            nextDate = safeDate(currentYear + 1, targetMonth, targetDay)
        }

        return java.time.temporal.ChronoUnit.DAYS.between(today, nextDate).toInt()
    }

    fun calculateDaysInfo(targetMillis: Long): DaysInfo {
        val today = LocalDate.now(defaultZoneId)
        val targetDate = Instant.ofEpochMilli(targetMillis).atZone(defaultZoneId).toLocalDate()
        val targetMonth = targetDate.monthValue
        val targetDay = targetDate.dayOfMonth
        val currentYear = today.year

        val thisYearAnniversary = safeDate(currentYear, targetMonth, targetDay)
        val isPast = thisYearAnniversary.isBefore(today)

        val lastAnniversary = if (isPast) {
            thisYearAnniversary
        } else {
            safeDate(currentYear - 1, targetMonth, targetDay)
        }

        val daysPassed = java.time.temporal.ChronoUnit.DAYS.between(lastAnniversary, today).toInt()

        val nextAnniversary = if (isPast) {
            safeDate(currentYear + 1, targetMonth, targetDay)
        } else {
            thisYearAnniversary
        }

        val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, nextAnniversary).toInt()

        return DaysInfo(
            daysPassed = daysPassed,
            daysUntil = daysUntil,
            isPast = isPast
        )
    }

    fun calculateBirthdayInfo(birthdayMillis: Long): BirthdayInfo {
        val today = LocalDate.now(defaultZoneId)
        val birthDate = Instant.ofEpochMilli(birthdayMillis).atZone(defaultZoneId).toLocalDate()
        val birthMonth = birthDate.monthValue
        val birthDay = birthDate.dayOfMonth
        val thisYear = today.year

        var thisYearBirthday = safeDate(thisYear, birthMonth, birthDay)
        if (thisYearBirthday.isBefore(today)) {
            thisYearBirthday = safeDate(thisYear + 1, birthMonth, birthDay)
        }

        val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, thisYearBirthday).toInt()
        val thisYearDate = thisYearBirthday.atStartOfDay(defaultZoneId).toInstant().toEpochMilli()
            .let { Instant.ofEpochMilli(it).atZone(defaultZoneId).format(shortDateFormatter) }

        val displayText = when {
            daysUntil == 0 -> "今天"
            daysUntil == 1 -> "明天"
            daysUntil <= 7 -> "${daysUntil}天后"
            else -> "${daysUntil}天"
        }

        return BirthdayInfo(
            thisYearDate = thisYearDate,
            daysUntil = daysUntil,
            isPast = thisYearBirthday.isBefore(today),
            displayText = displayText
        )
    }

    fun getNextBirthdayDate(birthdayMillis: Long): Long {
        val today = LocalDate.now(defaultZoneId)
        val birthDate = Instant.ofEpochMilli(birthdayMillis).atZone(defaultZoneId).toLocalDate()
        val birthMonth = birthDate.monthValue
        val birthDay = birthDate.dayOfMonth
        val thisYear = today.year

        var nextBirthday = safeDate(thisYear, birthMonth, birthDay)
        if (nextBirthday.isBefore(today)) {
            nextBirthday = safeDate(thisYear + 1, birthMonth, birthDay)
        }

        return nextBirthday.atStartOfDay(defaultZoneId).toInstant().toEpochMilli()
    }

    fun getNextRepeatDate(originalMillis: Long): Long {
        val today = LocalDate.now(defaultZoneId)
        val origDate = Instant.ofEpochMilli(originalMillis).atZone(defaultZoneId).toLocalDate()
        val origMonth = origDate.monthValue
        val origDay = origDate.dayOfMonth
        val thisYear = today.year

        var thisYearDate = safeDate(thisYear, origMonth, origDay)
        if (thisYearDate.isBefore(today)) {
            thisYearDate = safeDate(thisYear + 1, origMonth, origDay)
        }

        return thisYearDate.atStartOfDay(defaultZoneId).toInstant().toEpochMilli()
    }
}
