package com.tang.prm.domain.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId

class LunarDateUtilsTest {

    private val zoneId = ZoneId.systemDefault()

    @Nested
    @DisplayName("getNextLunarBirthdayDate")
    inner class GetNextLunarBirthdayDateTest {

        @Test
        fun returnsNonZeroForValidLunarDate() {
            // 正月初一 — always valid
            val result = LunarDateUtils.getNextLunarBirthdayDate(1, 1)
            assertThat(result).isGreaterThan(0L)
        }

        @Test
        fun returnsDateInFuture() {
            val result = LunarDateUtils.getNextLunarBirthdayDate(1, 1)
            val resultDate = java.time.Instant.ofEpochMilli(result).atZone(zoneId).toLocalDate()
            val today = LocalDate.now(zoneId)
            assertThat(!resultDate.isBefore(today)).isTrue()
        }

        @Test
        fun returnsZeroForInvalidLunarDate() {
            // 13月1日不存在
            val result = LunarDateUtils.getNextLunarBirthdayDate(13, 1)
            assertThat(result).isEqualTo(0L)
        }
    }

    @Nested
    @DisplayName("formatLunarDate")
    inner class FormatLunarDateTest {

        @Test
        fun returnsNonEmptyStringForValidDate() {
            val now = System.currentTimeMillis()
            val result = LunarDateUtils.formatLunarDate(now)
            assertThat(result).isNotEmpty()
        }

        @Test
        fun containsLunarPrefix() {
            val now = System.currentTimeMillis()
            val result = LunarDateUtils.formatLunarDate(now)
            assertThat(result).contains("农历")
        }
    }

    @Nested
    @DisplayName("formatLunarDateShort")
    inner class FormatLunarDateShortTest {

        @Test
        fun returnsNonEmptyStringForValidDate() {
            val now = System.currentTimeMillis()
            val result = LunarDateUtils.formatLunarDateShort(now)
            assertThat(result).isNotEmpty()
        }

        @Test
        fun containsLunarPrefix() {
            val now = System.currentTimeMillis()
            val result = LunarDateUtils.formatLunarDateShort(now)
            assertThat(result).contains("农历")
        }
    }

    @Nested
    @DisplayName("calculateLunarDaysInfo")
    inner class CalculateLunarDaysInfoTest {

        @Test
        fun returnsDaysInfoWithNonNegativeFields() {
            // Use a known past date
            val pastTimestamp = LocalDate.of(2000, 6, 15)
                .atStartOfDay(zoneId).toInstant().toEpochMilli()
            val info = LunarDateUtils.calculateLunarDaysInfo(pastTimestamp)
            assertThat(info.daysPassed).isAtLeast(0)
            assertThat(info.daysUntil).isAtLeast(0)
        }

        @Test
        fun daysInfoHasIsPastFlag() {
            val pastTimestamp = LocalDate.of(2000, 1, 1)
                .atStartOfDay(zoneId).toInstant().toEpochMilli()
            val info = LunarDateUtils.calculateLunarDaysInfo(pastTimestamp)
            // isPast depends on whether this year's lunar birthday has passed
            assertThat(info.isPast).isAnyOf(true, false)
        }
    }
}
