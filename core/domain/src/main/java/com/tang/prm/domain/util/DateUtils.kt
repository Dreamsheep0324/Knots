package com.tang.prm.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.DayOfWeek
import java.util.Locale

object DateUtils {

    const val MILLIS_PER_DAY = 86_400_000L
    const val MILLIS_PER_HOUR = 3_600_000L
    const val MILLIS_PER_MINUTE = 60_000L

    private enum class DateFormat(pattern: String) {
        DATE_TIME("yyyy年MM月dd日 HH:mm"),
        DATE("yyyy-MM-dd"),
        SHORT_DATE("MM-dd"),
        MONTH_DAY("MM/dd"),
        YEAR_MONTH_DAY("yyyy/MM/dd"),
        MONTH_DAY_CHINESE("M月d日"),
        YEAR_MONTH_DAY_CHINESE("yyyy年M月d日"),
        TIME("HH:mm"),
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
        MONTH_DAY_DOT_TIME("MM.dd HH:mm"),
        MONTH_DAY_SLASH_TIME("MM/dd HH:mm"),
        YEAR_MONTH_DAY_SLASH_TIME("yyyy/MM/dd HH:mm"),
        BACKUP_TIMESTAMP("yyyyMMdd_HHmmss");

        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    }

    private val defaultZoneId = ZoneId.of("Asia/Shanghai")

    private fun formatWith(timestamp: Long, format: DateFormat): String =
        Instant.ofEpochMilli(timestamp).atZone(defaultZoneId).format(format.formatter)

    /**
     * 格式化为相对时间（"刚刚"/"X分钟前"/"X小时后" 等）。
     *
     * Q-2 修复：原实现嵌套 when + 重复 `diff >= 0` 条件 4 次 + 魔法数字 `60 * 1000` / `60 * 60 * 1000`。
     * 重构为「过去/未来」两大分支，各自内联 when；魔法数字复用 [MILLIS_PER_MINUTE] / [MILLIS_PER_HOUR] / [MILLIS_PER_DAY] 常量。
     */
    fun formatRelativeTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp

        return when {
            diff >= 0 -> {
                // 过去时间
                val minutes = diff / MILLIS_PER_MINUTE
                val hours = diff / MILLIS_PER_HOUR
                val days = diff / MILLIS_PER_DAY
                when {
                    minutes < 1 -> "刚刚"
                    minutes < 60 -> "${minutes}分钟前"
                    hours < 24 -> "${hours}小时前"
                    days < 7 -> "${days}天前"
                    else -> formatYearMonthDayChinese(timestamp)
                }
            }
            else -> {
                // 未来时间
                val futureDiff = -diff
                val futureMinutes = futureDiff / MILLIS_PER_MINUTE
                val futureHours = futureDiff / MILLIS_PER_HOUR
                val futureDays = futureDiff / MILLIS_PER_DAY
                when {
                    futureMinutes < 1 -> "即将"
                    futureHours < 1 -> "${futureMinutes}分钟后"
                    futureDays < 1 -> "${futureHours}小时后"
                    futureDays < 7 -> "${futureDays}天后"
                    else -> formatYearMonthDayChinese(timestamp)
                }
            }
        }
    }

    fun formatDateTime(timestamp: Long): String = formatWith(timestamp, DateFormat.DATE_TIME)

    fun formatDate(timestamp: Long): String = formatWith(timestamp, DateFormat.DATE)

    fun formatMonthDay(timestamp: Long): String = formatWith(timestamp, DateFormat.MONTH_DAY)

    fun formatYearMonthDay(timestamp: Long): String = formatWith(timestamp, DateFormat.YEAR_MONTH_DAY)

    fun formatMonthDayChinese(timestamp: Long): String = formatWith(timestamp, DateFormat.MONTH_DAY_CHINESE)

    fun formatYearMonthDayChinese(timestamp: Long): String = formatWith(timestamp, DateFormat.YEAR_MONTH_DAY_CHINESE)

    fun formatTime(timestamp: Long): String = formatWith(timestamp, DateFormat.TIME)

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

    fun formatMonthDaySlashTime(timestamp: Long): String = formatWith(timestamp, DateFormat.MONTH_DAY_SLASH_TIME)

    fun formatYearMonthDaySlashTime(timestamp: Long): String = formatWith(timestamp, DateFormat.YEAR_MONTH_DAY_SLASH_TIME)

    /**
     * 返回月份的中文全称，如「七月」（Q-5/C-6）。
     *
     * 用 [Month.getDisplayName] 替代硬编码中文数组，单一来源且天然支持 i18n。
     * TextStyle.FULL 在 Locale.CHINESE 下产生「七月」；切到 Locale.ENGLISH 自动变为「July」。
     */
    fun formatMonthName(timestamp: Long): String {
        val month = Instant.ofEpochMilli(timestamp).atZone(defaultZoneId).month
        return month.getDisplayName(TextStyle.FULL, Locale.CHINESE)
    }

    /**
     * 返回星期的中文短称，如「周一」（Q-5/C-6）。
     *
     * 用 [DayOfWeek.getDisplayName] 替代硬编码中文数组，单一来源且天然支持 i18n。
     * TextStyle.SHORT 在 Locale.CHINESE 下产生「周一」；切到 Locale.ENGLISH 自动变为「Mon」。
     */
    fun formatWeekdayShortName(timestamp: Long): String {
        val dayOfWeek = Instant.ofEpochMilli(timestamp).atZone(defaultZoneId).dayOfWeek
        return dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
    }

    fun formatShortDate(timestamp: Long): String = formatWith(timestamp, DateFormat.SHORT_DATE)

    /** 格式化为备份文件名时间戳：yyyyMMdd_HHmmss */
    fun formatBackupTimestamp(timestamp: Long): String = formatWith(timestamp, DateFormat.BACKUP_TIMESTAMP)

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

    /** 解析备份文件名时间戳（yyyyMMdd_HHmmss）为 epoch millis，失败返回 null */
    fun parseBackupTimestamp(timeStr: String): Long? {
        return try {
            java.time.LocalDateTime.parse(timeStr, DateFormat.BACKUP_TIMESTAMP.formatter)
                .atZone(defaultZoneId)
                .toInstant()
                .toEpochMilli()
        } catch (e: Exception) {
            null
        }
    }
}
