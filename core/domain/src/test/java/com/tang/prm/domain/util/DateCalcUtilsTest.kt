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
        fun normalDate_returnsSame() {
            val date = DateCalcUtils.safeDate(2024, 6, 15)
            assertThat(date).isEqualTo(LocalDate.of(2024, 6, 15))
        }

        @Test
        fun feb29InLeapYear_returnsFeb29() {
            val date = DateCalcUtils.safeDate(2024, 2, 29)
            assertThat(date).isEqualTo(LocalDate.of(2024, 2, 29))
        }

        @Test
        fun feb29InNonLeapYear_fallsBackToFeb28() {
            val date = DateCalcUtils.safeDate(2023, 2, 29)
            assertThat(date).isEqualTo(LocalDate.of(2023, 2, 28))
        }

        @Test
        fun feb28InNonLeapYear_returnsFeb28() {
            val date = DateCalcUtils.safeDate(2023, 2, 28)
            assertThat(date).isEqualTo(LocalDate.of(2023, 2, 28))
        }
    }

    @Nested
    @DisplayName("safeDate 非法输入（T-6 修复）")
    inner class SafeDateInvalidInputTest {

        @Test
        fun month13_throwsException() {
            // safeDate 透传给 LocalDate.of，非法月份抛 DateTimeException
            org.junit.jupiter.api.assertThrows<java.time.DateTimeException> {
                DateCalcUtils.safeDate(2024, 13, 1)
            }
        }

        @Test
        fun month0_throwsException() {
            org.junit.jupiter.api.assertThrows<java.time.DateTimeException> {
                DateCalcUtils.safeDate(2024, 0, 1)
            }
        }

        @Test
        fun day32_throwsException() {
            org.junit.jupiter.api.assertThrows<java.time.DateTimeException> {
                DateCalcUtils.safeDate(2024, 1, 32)
            }
        }

        @Test
        fun day0_throwsException() {
            org.junit.jupiter.api.assertThrows<java.time.DateTimeException> {
                DateCalcUtils.safeDate(2024, 1, 0)
            }
        }
    }

    @Nested
    @DisplayName("calculateDaysInfo")
    inner class CalculateDaysInfoTest {

        @Test
        fun futureDate_isNotPast() {
            val today = LocalDate.now(zoneId)
            val target = today.plusDays(60)
            val targetMillis = target.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val info = DateCalcUtils.calculateDaysInfo(targetMillis)
            assertThat(info.isPast).isFalse()
            assertThat(info.daysUntil).isGreaterThan(0)
        }

        @Test
        fun pastDate_isPast() {
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
        fun birthdayToday_returnsToday() {
            val today = LocalDate.now(zoneId)
            val birthMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val next = DateCalcUtils.getNextBirthdayDate(birthMillis)
            val expected = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
            assertThat(next).isEqualTo(expected)
        }

        @Test
        fun pastBirthdayThisYear_returnsNextYear() {
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
        fun futureDateThisYear_returnsThisYear() {
            val today = LocalDate.now(zoneId)
            val target = today.plusMonths(3)
            val targetMillis = target.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val next = DateCalcUtils.getNextRepeatDate(targetMillis)
            val nextDate = java.time.Instant.ofEpochMilli(next).atZone(zoneId).toLocalDate()
            assertThat(nextDate.monthValue).isEqualTo(target.monthValue)
            assertThat(nextDate.dayOfMonth).isEqualTo(target.dayOfMonth)
        }

        @Test
        fun pastDateThisYear_returnsNextYear() {
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
        fun returnsMidnightToday() {
            val todayStart = DateCalcUtils.getTodayStart()
            val today = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant().toEpochMilli()
            assertThat(todayStart).isEqualTo(today)
        }
    }
}
