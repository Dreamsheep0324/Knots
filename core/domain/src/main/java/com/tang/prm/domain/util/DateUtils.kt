package com.tang.prm.domain.util

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
}
