package com.tang.prm.domain.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.Locale

class DateUtilsTest {

    @Test
    fun formatRelativeTime_justNow_returns刚刚() {
        val now = System.currentTimeMillis()
        val result = DateUtils.formatRelativeTime(now)
        assertThat(result).isEqualTo("刚刚")
    }

    @Test
    fun formatRelativeTime_5minAgo_returns5分钟前() {
        val now = System.currentTimeMillis()
        val result = DateUtils.formatRelativeTime(now - 5 * 60 * 1000)
        assertThat(result).isEqualTo("5分钟前")
    }

    @Test
    fun formatRelativeTime_3hoursAgo_returns3小时前() {
        val now = System.currentTimeMillis()
        val result = DateUtils.formatRelativeTime(now - 3 * 60 * 60 * 1000)
        assertThat(result).isEqualTo("3小时前")
    }

    @Test
    fun formatRelativeTime_7daysAgo_returnsChineseDate() {
        val now = System.currentTimeMillis()
        val result = DateUtils.formatRelativeTime(now - 7 * 24 * 60 * 60 * 1000L)
        assertThat(result).matches("\\d{4}年\\d{1,2}月\\d{1,2}日")
    }

    @Test
    fun formatRelativeTime_future_returns即将() {
        val now = System.currentTimeMillis()
        val result = DateUtils.formatRelativeTime(now + 1000)
        assertThat(result).isEqualTo("即将")
    }

    @Test
    fun formatDate_knownTimestamp_returnsFormattedDate() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timestamp = System.currentTimeMillis()
        val expected = sdf.format(timestamp)
        val result = DateUtils.formatDate(timestamp)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun formatTime_knownTimestamp_returnsFormattedTime() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timestamp = System.currentTimeMillis()
        val expected = sdf.format(timestamp)
        val result = DateUtils.formatTime(timestamp)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun parseDateToMillis_validDate_returnsMillis() {
        val result = DateUtils.parseDateToMillis("2024-06-15")
        assertThat(result).isNotNull()
    }

    @Test
    fun parseDateToMillis_invalidDate_returnsNull() {
        val result = DateUtils.parseDateToMillis("not-a-date")
        assertThat(result).isNull()
    }

    @Test
    fun calculateDaysUntil_futureDate_returnsPositiveDays() {
        val future = System.currentTimeMillis() + 10L * 24 * 60 * 60 * 1000
        val result = DateCalcUtils.calculateDaysUntil(future)
        assertThat(result).isGreaterThan(0)
    }

    @Test
    fun calculateDaysInfo_futureDate_isPastFalse() {
        val future = System.currentTimeMillis() + 10L * 24 * 60 * 60 * 1000
        val result = DateCalcUtils.calculateDaysInfo(future)
        assertThat(result.isPast).isFalse()
    }

    @Test
    fun calculateBirthdayInfo_returnsNonEmptyFields() {
        val birthday = System.currentTimeMillis() - 30L * 365 * 24 * 60 * 60 * 1000
        val result = DateCalcUtils.calculateBirthdayInfo(birthday)
        assertThat(result.toString()).isNotEmpty()
    }

    @Test
    fun getTodayStart_returnsMidnightTimestamp() {
        val todayStart = DateCalcUtils.getTodayStart()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formatted = sdf.format(todayStart)
        assertThat(formatted).endsWith("00:00:00")
    }

    @Test
    fun getTodayStart_isBeforeNow() {
        val todayStart = DateCalcUtils.getTodayStart()
        assertThat(todayStart).isLessThan(System.currentTimeMillis())
    }

    @Test
    fun getTodayStart_isWithinToday() {
        val todayStart = DateCalcUtils.getTodayStart()
        val dayMs = 24 * 60 * 60 * 1000L
        assertThat(System.currentTimeMillis() - todayStart).isLessThan(dayMs)
    }

    // Q-5/C-6：新增 DateUtils.formatMonthName / formatWeekdayShortName 测试

    @Test
    fun formatMonthName_returnsChineseFullName() {
        // 2026-07-15 00:00（系统时区），月名应为「七月」
        val ts = java.time.LocalDate.of(2026, 7, 15)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val result = DateUtils.formatMonthName(ts)
        assertThat(result).isEqualTo("七月")
    }

    @Test
    fun formatMonthName_nonNullNonEmpty() {
        val result = DateUtils.formatMonthName(System.currentTimeMillis())
        assertThat(result).isNotEmpty()
    }

    @Test
    fun formatWeekdayShortName_returnsChineseShortName() {
        // 2026-07-15 是周三
        val ts = java.time.LocalDate.of(2026, 7, 15)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val result = DateUtils.formatWeekdayShortName(ts)
        assertThat(result).isEqualTo("周三")
    }

    @Test
    fun formatWeekdayShortName_nonNullNonEmpty() {
        val result = DateUtils.formatWeekdayShortName(System.currentTimeMillis())
        assertThat(result).isNotEmpty()
    }
}
