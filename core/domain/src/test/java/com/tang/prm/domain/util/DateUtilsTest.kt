package com.tang.prm.domain.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.Locale

class DateUtilsTest {

    @Test
    fun `formatRelativeTime justNow returns 刚刚`() {
        val now = System.currentTimeMillis()
        val result = DateUtils.formatRelativeTime(now)
        assertThat(result).isEqualTo("刚刚")
    }

    @Test
    fun `formatRelativeTime 5minAgo returns 5分钟前`() {
        val now = System.currentTimeMillis()
        val result = DateUtils.formatRelativeTime(now - 5 * 60 * 1000)
        assertThat(result).isEqualTo("5分钟前")
    }

    @Test
    fun `formatRelativeTime 3hoursAgo returns 3小时前`() {
        val now = System.currentTimeMillis()
        val result = DateUtils.formatRelativeTime(now - 3 * 60 * 60 * 1000)
        assertThat(result).isEqualTo("3小时前")
    }

    @Test
    fun `formatRelativeTime 7daysAgo returns chinese date`() {
        val now = System.currentTimeMillis()
        val result = DateUtils.formatRelativeTime(now - 7 * 24 * 60 * 60 * 1000L)
        assertThat(result).matches("\\d{4}年\\d{1,2}月\\d{1,2}日")
    }

    @Test
    fun `formatRelativeTime future returns 即将`() {
        val now = System.currentTimeMillis()
        val result = DateUtils.formatRelativeTime(now + 1000)
        assertThat(result).isEqualTo("即将")
    }

    @Test
    fun `formatDate knownTimestamp returns formatted date`() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timestamp = System.currentTimeMillis()
        val expected = sdf.format(timestamp)
        val result = DateUtils.formatDate(timestamp)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `formatTime knownTimestamp returns formatted time`() {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timestamp = System.currentTimeMillis()
        val expected = sdf.format(timestamp)
        val result = DateUtils.formatTime(timestamp)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `parseDateToMillis validDate returns millis`() {
        val result = DateUtils.parseDateToMillis("2024-06-15")
        assertThat(result).isNotNull()
    }

    @Test
    fun `parseDateToMillis invalidDate returns null`() {
        val result = DateUtils.parseDateToMillis("not-a-date")
        assertThat(result).isNull()
    }

    @Test
    fun `calculateDaysInfo futureDate isPast false`() {
        val future = System.currentTimeMillis() + 10L * 24 * 60 * 60 * 1000
        val result = DateCalcUtils.calculateDaysInfo(future)
        assertThat(result.isPast).isFalse()
    }

    @Test
    fun `getTodayStart returns midnight timestamp`() {
        val todayStart = DateCalcUtils.getTodayStart()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formatted = sdf.format(todayStart)
        assertThat(formatted).endsWith("00:00:00")
    }

    @Test
    fun `getTodayStart is before now`() {
        val todayStart = DateCalcUtils.getTodayStart()
        assertThat(todayStart).isLessThan(System.currentTimeMillis())
    }

    @Test
    fun `getTodayStart is within today`() {
        val todayStart = DateCalcUtils.getTodayStart()
        val dayMs = 24 * 60 * 60 * 1000L
        assertThat(System.currentTimeMillis() - todayStart).isLessThan(dayMs)
    }

    // Q-5/C-6：新增 DateUtils.formatMonthName / formatWeekdayShortName 测试

    @Test
    fun `formatMonthName returns chinese full name`() {
        // 2026-07-15 00:00（系统时区），月名应为「七月」
        val ts = java.time.LocalDate.of(2026, 7, 15)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val result = DateUtils.formatMonthName(ts)
        assertThat(result).isEqualTo("七月")
    }

    @Test
    fun `formatMonthName non null non empty`() {
        val result = DateUtils.formatMonthName(System.currentTimeMillis())
        assertThat(result).isNotEmpty()
    }

    @Test
    fun `formatWeekdayShortName returns chinese short name`() {
        // 2026-07-15 是周三
        val ts = java.time.LocalDate.of(2026, 7, 15)
            .atStartOfDay(java.time.ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val result = DateUtils.formatWeekdayShortName(ts)
        assertThat(result).isEqualTo("周三")
    }

    @Test
    fun `formatWeekdayShortName non null non empty`() {
        val result = DateUtils.formatWeekdayShortName(System.currentTimeMillis())
        assertThat(result).isNotEmpty()
    }
}
