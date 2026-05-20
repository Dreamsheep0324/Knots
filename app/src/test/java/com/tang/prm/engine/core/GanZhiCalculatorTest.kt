package com.tang.prm.engine.core

import com.tang.prm.engine.divination.core.GanZhiCalculator
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class GanZhiCalculatorTest {

    @Test
    fun `fromSolar_2024Jan1_11am_returnsCorrectYearGanZhi`() {
        val result = GanZhiCalculator.fromSolar(2024, 1, 1, 11)
        assertThat(result.year).isEqualTo("癸卯")
    }

    @Test
    fun `fromSolar_2024Mar1_returnsCorrectMonthGanZhi`() {
        val result = GanZhiCalculator.fromSolar(2024, 3, 1, 12)
        assertThat(result.month).isEqualTo("丙寅")
    }

    @Test
    fun `fromSolar_2024Jan1_returnsCorrectDayGanZhi`() {
        val result = GanZhiCalculator.fromSolar(2024, 1, 1, 12)
        assertThat(result.day).isEqualTo("甲子")
    }

    @Test
    fun `getHourZhi_0h_returns子`() {
        assertThat(GanZhiCalculator.getHourZhi(0)).isEqualTo("子")
    }

    @Test
    fun `getHourZhi_12h_returns午`() {
        assertThat(GanZhiCalculator.getHourZhi(12)).isEqualTo("午")
    }

    @Test
    fun `getHourZhi_23h_returns子`() {
        assertThat(GanZhiCalculator.getHourZhi(23)).isEqualTo("子")
    }

    @Test
    fun `getHourZhiIndex_0h_returns1`() {
        assertThat(GanZhiCalculator.getHourZhiIndex(0)).isEqualTo(1)
    }

    @Test
    fun `getHourZhiIndex_12h_returns7`() {
        assertThat(GanZhiCalculator.getHourZhiIndex(12)).isEqualTo(7)
    }

    @Test
    fun `getYearZhiIndex_甲辰_returns5`() {
        assertThat(GanZhiCalculator.getYearZhiIndex("甲辰")).isEqualTo(5)
    }

    @Test
    fun `getTimeZhiIndex_午时_returns7`() {
        assertThat(GanZhiCalculator.getTimeZhiIndex("庚午")).isEqualTo(7)
    }

    @Test
    fun `getLunarMonthDay_2024Feb10_returnsLunarNewYear`() {
        val (month, day) = GanZhiCalculator.getLunarMonthDay(2024, 2, 10)
        assertThat(month).isEqualTo(1)
        assertThat(day).isEqualTo(1)
    }

    @Test
    fun `getDayGan_2024Jan1_returnsNonEmpty`() {
        assertThat(GanZhiCalculator.getDayGan(2024, 1, 1)).isNotEmpty()
    }

    @Test
    fun `getDayGanZhi_2024Jan1_returnsNonEmpty`() {
        assertThat(GanZhiCalculator.getDayGanZhi(2024, 1, 1)).isNotEmpty()
    }

    @Test
    fun `fromSolar_boundaryHour23_returnsValidGanZhi`() {
        val result = GanZhiCalculator.fromSolar(2024, 1, 1, 23)
        assertThat(result.year).isNotEmpty()
        assertThat(result.month).isNotEmpty()
        assertThat(result.day).isNotEmpty()
        assertThat(result.hour).isNotEmpty()
    }

    @Test
    fun `getHourZhi_allHours_returnsValidDizhi`() {
        val validDizhi = setOf("子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥")
        for (hour in 0..23) {
            assertThat(GanZhiCalculator.getHourZhi(hour)).isIn(validDizhi)
        }
    }
}
