package com.tang.prm.domain.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class DateCalcUtilsTest {

    private val zoneId = ZoneId.systemDefault()

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
    @DisplayName("calculateDaysUntil")
    inner class CalculateDaysUntilTest {

        @Test
        fun futureDateThisYear_returnsCorrectDays() {
            val today = LocalDate.now(zoneId)
            val target = today.plusDays(30)
            val targetMillis = target.atStartOfDay(zoneId).toInstant().toEpochMilli()
            assertThat(DateCalcUtils.calculateDaysUntil(targetMillis)).isEqualTo(30)
        }

        @Test
        fun pastDateThisYear_rollsToNextYear() {
            val today = LocalDate.now(zoneId)
            val target = today.minusDays(10)
            val targetMillis = target.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val expected = ChronoUnit.DAYS.between(today, target.plusYears(1)).toInt()
            assertThat(DateCalcUtils.calculateDaysUntil(targetMillis)).isEqualTo(expected)
        }

        @Test
        fun today_returns0() {
            val todayMillis = LocalDate.now(zoneId)
                .atStartOfDay(zoneId).toInstant().toEpochMilli()
            assertThat(DateCalcUtils.calculateDaysUntil(todayMillis)).isEqualTo(0)
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
    @DisplayName("calculateBirthdayInfo")
    inner class CalculateBirthdayInfoTest {

        @Test
        fun birthdayToday_displayTextIsToday() {
            val today = LocalDate.now(zoneId)
            val birthMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val info = DateCalcUtils.calculateBirthdayInfo(birthMillis)
            assertThat(info.daysUntil).isEqualTo(0)
            assertThat(info.displayText).isEqualTo("今天")
        }

        @Test
        fun birthdayTomorrow_displayTextIsTomorrow() {
            val today = LocalDate.now(zoneId)
            val tomorrow = today.plusDays(1)
            val birthMillis = tomorrow.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val info = DateCalcUtils.calculateBirthdayInfo(birthMillis)
            assertThat(info.daysUntil).isEqualTo(1)
            assertThat(info.displayText).isEqualTo("明天")
        }

        @Test
        fun birthdayIn3Days_displayTextContainsDays() {
            val today = LocalDate.now(zoneId)
            val target = today.plusDays(3)
            val birthMillis = target.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val info = DateCalcUtils.calculateBirthdayInfo(birthMillis)
            assertThat(info.displayText).isEqualTo("3天后")
        }

        @Test
        fun birthdayIn10Days_displayTextIsDaysOnly() {
            val today = LocalDate.now(zoneId)
            val target = today.plusDays(10)
            val birthMillis = target.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val info = DateCalcUtils.calculateBirthdayInfo(birthMillis)
            assertThat(info.displayText).isEqualTo("10天")
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
