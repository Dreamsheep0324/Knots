package com.tang.prm.engine.divination.core

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class GanZhiCalculatorTest {

    private val tiangan = listOf("甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸")
    private val dizhi = listOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")

    @Nested
    @DisplayName("getHourZhi 时辰地支")
    inner class GetHourZhiTest {

        @ParameterizedTest
        @CsvSource(
            "0, 子", "23, 子", "24, 子",
            "1, 丑", "2, 丑",
            "3, 寅", "4, 寅",
            "5, 卯", "6, 卯",
            "7, 辰", "8, 辰",
            "9, 巳", "10, 巳",
            "11, 午", "12, 午",
            "13, 未", "14, 未",
            "15, 申", "16, 申",
            "17, 酉", "18, 酉",
            "19, 戌", "20, 戌",
            "21, 亥", "22, 亥"
        )
        fun mapsHourToZhi(hour: Int, expected: String) {
            assertThat(GanZhiCalculator.getHourZhi(hour)).isEqualTo(expected)
        }

        @Test
        fun outOfRange_defaultsToZi() {
            assertThat(GanZhiCalculator.getHourZhi(25)).isEqualTo("子")
            assertThat(GanZhiCalculator.getHourZhi(-1)).isEqualTo("子")
        }
    }

    @Nested
    @DisplayName("getHourZhiIndex 时辰索引（1-based）")
    inner class GetHourZhiIndexTest {

        @ParameterizedTest
        @CsvSource("0, 1", "23, 1", "1, 2", "3, 3", "5, 4", "7, 5", "9, 6", "11, 7", "13, 8", "15, 9", "17, 10", "19, 11", "21, 12")
        fun returnsOneBasedIndex(hour: Int, expected: Int) {
            assertThat(GanZhiCalculator.getHourZhiIndex(hour)).isEqualTo(expected)
        }
    }

    @Nested
    @DisplayName("getYearZhiIndex / getTimeZhiIndex")
    inner class ZhiIndexTest {

        @ParameterizedTest
        @CsvSource("甲子, 1", "甲丑, 2", "甲寅, 3", "甲卯, 4", "甲辰, 5", "甲巳, 6", "甲午, 7", "甲未, 8", "甲申, 9", "甲酉, 10", "甲戌, 11", "甲亥, 12")
        fun yearZhiIndex(ganZhi: String, expected: Int) {
            assertThat(GanZhiCalculator.getYearZhiIndex(ganZhi)).isEqualTo(expected)
        }

        @ParameterizedTest
        @CsvSource("甲子, 1", "丙丑, 2", "戊寅, 3", "庚亥, 12")
        fun timeZhiIndex(ganZhi: String, expected: Int) {
            assertThat(GanZhiCalculator.getTimeZhiIndex(ganZhi)).isEqualTo(expected)
        }

        @Test
        fun unknownZhi_returnsZero() {
            assertThat(GanZhiCalculator.getYearZhiIndex("甲X")).isEqualTo(0)
            assertThat(GanZhiCalculator.getTimeZhiIndex("甲X")).isEqualTo(0)
        }
    }

    @Nested
    @DisplayName("fromSolar / fromCalendar")
    inner class FromSolarTest {

        @Test
        fun returnsFourGanZhiStrings() {
            val info = GanZhiCalculator.fromSolar(2024, 6, 15, 14, 30)
            assertThat(info.year).hasLength(2)
            assertThat(info.month).hasLength(2)
            assertThat(info.day).hasLength(2)
            assertThat(info.hour).hasLength(2)
        }

        @Test
        fun yearGanIsFromTiangan() {
            val info = GanZhiCalculator.fromSolar(2024, 6, 15, 14, 30)
            assertThat(tiangan).contains(info.year.substring(0, 1))
        }

        @Test
        fun yearZhiIsFromDizhi() {
            val info = GanZhiCalculator.fromSolar(2024, 6, 15, 14, 30)
            assertThat(dizhi).contains(info.year.substring(1, 2))
        }

        @Test
        fun hourZhiMatchesHour() {
            val info = GanZhiCalculator.fromSolar(2024, 6, 15, 14, 30)
            assertThat(info.hour.substring(1, 2)).isEqualTo("未")
        }

        @Test
        fun fromCalendar_returnsSameAsFromSolar() {
            val calendar = java.util.Calendar.getInstance().apply {
                set(2024, 5, 15, 14, 30, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val fromCal = GanZhiCalculator.fromCalendar(calendar)
            val fromSolar = GanZhiCalculator.fromSolar(2024, 6, 15, 14, 30)
            assertThat(fromCal).isEqualTo(fromSolar)
        }
    }

    @Nested
    @DisplayName("getDayGan / getDayGanZhi")
    inner class GetDayGanTest {

        @Test
        fun dayGanIsSingleChar() {
            val gan = GanZhiCalculator.getDayGan(2024, 6, 15)
            assertThat(gan).hasLength(1)
            assertThat(tiangan).contains(gan)
        }

        @Test
        fun dayGanZhiIsTwoChars() {
            val ganzhi = GanZhiCalculator.getDayGanZhi(2024, 6, 15)
            assertThat(ganzhi).hasLength(2)
            assertThat(tiangan).contains(ganzhi.substring(0, 1))
            assertThat(dizhi).contains(ganzhi.substring(1, 2))
        }

        @Test
        fun sameDate_returnsSameResult() {
            val a = GanZhiCalculator.getDayGanZhi(2024, 6, 15)
            val b = GanZhiCalculator.getDayGanZhi(2024, 6, 15)
            assertThat(a).isEqualTo(b)
        }
    }

    @Nested
    @DisplayName("getLunarMonthDay")
    inner class GetLunarMonthDayTest {

        @Test
        fun returnsPositiveMonthAndDay() {
            val (month, day) = GanZhiCalculator.getLunarMonthDay(2024, 6, 15)
            assertThat(month).isAtLeast(1)
            assertThat(month).isAtMost(12)
            assertThat(day).isAtLeast(1)
            assertThat(day).isAtMost(30)
        }

        @Test
        fun sameDate_returnsSameResult() {
            val a = GanZhiCalculator.getLunarMonthDay(2024, 6, 15)
            val b = GanZhiCalculator.getLunarMonthDay(2024, 6, 15)
            assertThat(a).isEqualTo(b)
        }
    }
}
