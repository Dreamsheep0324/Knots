package com.tang.prm.domain.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class DateCalcUtilsTest {

    // T-5 修复：与 DateCalcUtils 内部时区保持一致（北京时区），避免 CI 跨时区 flaky
    private val zoneId = ZoneId.of("Asia/Shanghai")

    @Nested
    @DisplayName("safeDate")
    inner class SafeDateTest {

        @Test
        fun `normal date returns same`() {
            val date = DateCalcUtils.safeDate(2024, 6, 15)
            assertThat(date).isEqualTo(LocalDate.of(2024, 6, 15))
        }

        @Test
        fun `feb29 in leap year returns feb29`() {
            val date = DateCalcUtils.safeDate(2024, 2, 29)
            assertThat(date).isEqualTo(LocalDate.of(2024, 2, 29))
        }

        @Test
        fun `feb29 in non leap year falls back to feb28`() {
            val date = DateCalcUtils.safeDate(2023, 2, 29)
            assertThat(date).isEqualTo(LocalDate.of(2023, 2, 28))
        }

        @Test
        fun `feb28 in non leap year returns feb28`() {
            val date = DateCalcUtils.safeDate(2023, 2, 28)
            assertThat(date).isEqualTo(LocalDate.of(2023, 2, 28))
        }
    }

    @Nested
    @DisplayName("safeDate 非法输入（T-6 修复）")
    inner class SafeDateInvalidInputTest {

        @Test
        fun `month13 throws exception`() {
            // safeDate 透传给 LocalDate.of，非法月份抛 DateTimeException
            org.junit.jupiter.api.assertThrows<java.time.DateTimeException> {
                DateCalcUtils.safeDate(2024, 13, 1)
            }
        }

        @Test
        fun `month0 throws exception`() {
            org.junit.jupiter.api.assertThrows<java.time.DateTimeException> {
                DateCalcUtils.safeDate(2024, 0, 1)
            }
        }

        @Test
        fun `day32 throws exception`() {
            org.junit.jupiter.api.assertThrows<java.time.DateTimeException> {
                DateCalcUtils.safeDate(2024, 1, 32)
            }
        }

        @Test
        fun `day0 throws exception`() {
            org.junit.jupiter.api.assertThrows<java.time.DateTimeException> {
                DateCalcUtils.safeDate(2024, 1, 0)
            }
        }
    }

    @Nested
    @DisplayName("calculateDaysInfo")
    inner class CalculateDaysInfoTest {

        @Test
        fun `future date is not past`() {
            val today = LocalDate.now(zoneId)
            val target = today.plusDays(60)
            val targetMillis = target.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val info = DateCalcUtils.calculateDaysInfo(targetMillis)
            assertThat(info.isPast).isFalse()
            assertThat(info.daysUntil).isGreaterThan(0)
        }

        @Test
        fun `past date is past`() {
            val today = LocalDate.now(zoneId)
            val target = today.minusDays(60)
            val targetMillis = target.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val info = DateCalcUtils.calculateDaysInfo(targetMillis)
            assertThat(info.isPast).isTrue()
            assertThat(info.daysPassed).isGreaterThan(0)
        }
    }

    @Nested
    @DisplayName("getNextBirthdayDate")
    inner class GetNextBirthdayDateTest {

        @Test
        fun `birthday today returns today`() {
            val today = LocalDate.now(zoneId)
            val birthMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val next = DateCalcUtils.getNextBirthdayDate(birthMillis)
            val expected = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
            assertThat(next).isEqualTo(expected)
        }

        @Test
        fun `past birthday this year returns next year`() {
            val today = LocalDate.now(zoneId)
            // Use a date that has definitely passed this year (Jan 1)
            val pastBirthday = LocalDate.of(today.year, 1, 1)
            val birthMillis = pastBirthday.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val next = DateCalcUtils.getNextBirthdayDate(birthMillis)
            val nextDate = java.time.Instant.ofEpochMilli(next).atZone(zoneId).toLocalDate()
            assertThat(nextDate.year).isEqualTo(today.year + 1)
        }
    }

    @Nested
    @DisplayName("getNextRepeatDate")
    inner class GetNextRepeatDateTest {

        @Test
        fun `future date this year returns this year`() {
            val today = LocalDate.now(zoneId)
            val target = today.plusMonths(3)
            val targetMillis = target.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val next = DateCalcUtils.getNextRepeatDate(targetMillis)
            val nextDate = java.time.Instant.ofEpochMilli(next).atZone(zoneId).toLocalDate()
            assertThat(nextDate.monthValue).isEqualTo(target.monthValue)
            assertThat(nextDate.dayOfMonth).isEqualTo(target.dayOfMonth)
        }

        @Test
        fun `past date this year returns next year`() {
            val today = LocalDate.now(zoneId)
            val target = today.minusMonths(3)
            val targetMillis = target.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val next = DateCalcUtils.getNextRepeatDate(targetMillis)
            val nextDate = java.time.Instant.ofEpochMilli(next).atZone(zoneId).toLocalDate()
            assertThat(nextDate.year).isEqualTo(today.year + 1)
        }
    }

    @Nested
    @DisplayName("getTodayStart")
    inner class GetTodayStartTest {

        @Test
        fun `returns midnight today`() {
            val todayStart = DateCalcUtils.getTodayStart()
            val today = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant().toEpochMilli()
            assertThat(todayStart).isEqualTo(today)
        }
    }
}
