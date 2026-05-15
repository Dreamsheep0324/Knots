package com.tang.prm.util

import com.tang.prm.util.LunarUtils.lunarToSolar
import com.tang.prm.util.LunarUtils.solarToLunar
import com.tang.prm.util.LunarUtils.getLunarDateDescription
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    private enum class DateFormat(pattern: String) {
        DATE_TIME("yyyy年MM月dd日 HH:mm"),
        DATE("yyyy-MM-dd"),
        SHORT_DATE("MM-dd"),
        MONTH_DAY("MM/dd"),
        YEAR_MONTH_DAY("yyyy/MM/dd"),
        MONTH_DAY_CHINESE("M月d日"),
        YEAR_MONTH_DAY_CHINESE("yyyy年M月d日"),
        TIME("HH:mm"),
        MONTH_DAY_TIME("M月d日 HH:mm"),
        FULL_DATE_TIME("yyyy年M月d日 HH:mm"),
        YEAR_MONTH_DAY_CHINESE_FULL("yyyy年MM月dd日"),
        YEAR_MONTH_DAY_DOT("yyyy.MM.dd"),
        YEAR_MONTH_CHINESE("yyyy年MM月"),
        MONTH_DAY_CHINESE_FULL("MM月dd日"),
        MONTH_DAY_TIME_CHINESE_FULL("MM月dd日 HH:mm"),
        MONTH_DAY_WEEKDAY("M月d日 EEEE"),
        MONTH_DAY_WEEKDAY_TIME("M月d日 EEEE HH:mm"),
        TIME_WITH_SECONDS("HH:mm:ss"),
        DATE_TIME_HYPHEN("yyyy-MM-dd HH:mm"),
        MONTH_DAY_DOT("MM.dd"),
        MONTH_DAY_DOT_TIME("MM.dd HH:mm");

        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    }

    private val defaultZoneId = ZoneId.systemDefault()

    private fun formatWith(timestamp: Long, format: DateFormat): String =
        Instant.ofEpochMilli(timestamp).atZone(defaultZoneId).format(format.formatter)

    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val minutes = diff / (60 * 1000)
        val hours = diff / (60 * 60 * 1000)
        val days = diff / (24 * 60 * 60 * 1000)

        return when {
            minutes < 1 && diff >= 0 -> "刚刚"
            minutes < 60 && diff >= 0 -> "${minutes}分钟前"
            hours < 24 && diff >= 0 -> "${hours}小时前"
            days < 7 && diff >= 0 -> "${days}天前"
            diff < 0 -> {
                val futureMinutes = -diff / (60 * 1000)
                val futureHours = -diff / (60 * 60 * 1000)
                val futureDays = -diff / (24 * 60 * 60 * 1000)
                when {
                    futureMinutes < 1 -> "即将"
                    futureHours < 1 -> "${futureMinutes}分钟后"
                    futureDays < 1 -> "${futureHours}小时后"
                    futureDays < 7 -> "${futureDays}天后"
                    else -> formatYearMonthDayChinese(timestamp)
                }
            }
            else -> formatYearMonthDayChinese(timestamp)
        }
    }

    fun formatDateTime(timestamp: Long): String = formatWith(timestamp, DateFormat.DATE_TIME)

    fun formatDate(timestamp: Long): String = formatWith(timestamp, DateFormat.DATE)

    fun formatMonthDay(timestamp: Long): String = formatWith(timestamp, DateFormat.MONTH_DAY)

    fun formatYearMonthDay(timestamp: Long): String = formatWith(timestamp, DateFormat.YEAR_MONTH_DAY)

    fun formatMonthDayChinese(timestamp: Long): String = formatWith(timestamp, DateFormat.MONTH_DAY_CHINESE)

    fun formatYearMonthDayChinese(timestamp: Long): String = formatWith(timestamp, DateFormat.YEAR_MONTH_DAY_CHINESE)

    fun formatTime(timestamp: Long): String = formatWith(timestamp, DateFormat.TIME)

    fun formatMonthDayTime(timestamp: Long): String = formatWith(timestamp, DateFormat.MONTH_DAY_TIME)

    fun formatFullDateTimeChinese(timestamp: Long): String = formatWith(timestamp, DateFormat.FULL_DATE_TIME)

    fun formatYearMonthDayChineseFull(timestamp: Long): String = formatWith(timestamp, DateFormat.YEAR_MONTH_DAY_CHINESE_FULL)

    fun formatYearMonthDayDot(timestamp: Long): String = formatWith(timestamp, DateFormat.YEAR_MONTH_DAY_DOT)

    fun formatYearMonthChinese(timestamp: Long): String = formatWith(timestamp, DateFormat.YEAR_MONTH_CHINESE)

    fun formatMonthDayChineseFull(timestamp: Long): String = formatWith(timestamp, DateFormat.MONTH_DAY_CHINESE_FULL)

    fun formatMonthDayTimeChineseFull(timestamp: Long): String = formatWith(timestamp, DateFormat.MONTH_DAY_TIME_CHINESE_FULL)

    fun formatMonthDayWeekday(timestamp: Long): String = formatWith(timestamp, DateFormat.MONTH_DAY_WEEKDAY)

    fun formatMonthDayWeekdayTime(timestamp: Long): String = formatWith(timestamp, DateFormat.MONTH_DAY_WEEKDAY_TIME)

    fun formatTimeWithSeconds(timestamp: Long): String = formatWith(timestamp, DateFormat.TIME_WITH_SECONDS)

    fun formatDateTimeHyphen(timestamp: Long): String = formatWith(timestamp, DateFormat.DATE_TIME_HYPHEN)

    fun formatMonthDayDot(timestamp: Long): String = formatWith(timestamp, DateFormat.MONTH_DAY_DOT)

    fun formatMonthDayDotTime(timestamp: Long): String = formatWith(timestamp, DateFormat.MONTH_DAY_DOT_TIME)

    fun formatShortDate(timestamp: Long): String = formatWith(timestamp, DateFormat.SHORT_DATE)

    fun parseDateToMillis(dateStr: String): Long? {
        return try {
            LocalDate.parse(dateStr, DateFormat.DATE.formatter)
                .atStartOfDay(defaultZoneId)
                .toInstant()
                .toEpochMilli()
        } catch (e: Exception) {
            null
        }
    }

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

    private fun safeDate(year: Int, month: Int, day: Int): LocalDate {
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

    data class BirthdayInfo(
        val thisYearDate: String,
        val daysUntil: Int,
        val isPast: Boolean,
        val displayText: String
    )

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
        val thisYearDate = thisYearBirthday.format(DateFormat.SHORT_DATE.formatter)

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
            formatDate(timestamp)
        }
    }

    fun formatLunarDateShort(timestamp: Long): String {
        val lunarDesc = getLunarDateDescription(timestamp)
        return if (lunarDesc.isNotEmpty()) {
            "农历$lunarDesc"
        } else {
            formatDate(timestamp)
        }
    }

    fun calculateLunarDaysInfo(targetMillis: Long): DaysInfo {
        val today = LocalDate.now(defaultZoneId)
        val targetDate = Instant.ofEpochMilli(targetMillis).atZone(defaultZoneId).toLocalDate()

        val lunarDate = solarToLunar(targetDate.year, targetDate.monthValue, targetDate.dayOfMonth)
            ?: return calculateDaysInfo(targetMillis)

        val lunarMonth = lunarDate.month
        val lunarDay = lunarDate.day

        val thisYearSolar = lunarToSolar(today.year, lunarMonth, lunarDay)
        val thisYearDate = thisYearSolar?.let { safeDate(it.first, it.second, it.third) }

        val lastYearSolar = lunarToSolar(today.year - 1, lunarMonth, lunarDay)
        val lastYearDate = lastYearSolar?.let { safeDate(it.first, it.second, it.third) }

        val nextYearSolar = lunarToSolar(today.year + 1, lunarMonth, lunarDay)
        val nextYearDate = nextYearSolar?.let { safeDate(it.first, it.second, it.third) }

        val isPast = thisYearDate?.isBefore(today) ?: true

        val lastAnniversary = if (isPast) thisYearDate else lastYearDate
        val nextAnniversary = if (isPast) nextYearDate else thisYearDate

        val daysPassed = lastAnniversary?.let {
            java.time.temporal.ChronoUnit.DAYS.between(it, today).toInt()
        } ?: 0

        val daysUntil = nextAnniversary?.let {
            java.time.temporal.ChronoUnit.DAYS.between(today, it).toInt()
        } ?: 0

        return DaysInfo(
            daysPassed = daysPassed,
            daysUntil = daysUntil,
            isPast = isPast
        )
    }
}
