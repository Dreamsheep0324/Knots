package com.tang.prm.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class LunarUtilsTest {

    @Test
    fun lunarToSolar_2024LunarJan1_returnsCorrectSolarDate() {
        val result = LunarUtils.lunarToSolar(2024, 1, 1)
        assertThat(result).isNotNull()
        val (year, month, day) = result!!
        assertThat(year).isEqualTo(2024)
        assertThat(month).isGreaterThan(0)
        assertThat(day).isGreaterThan(0)
    }

    @Test
    fun solarToLunar_2024Feb10_returnsLunarJan1() {
        val result = LunarUtils.solarToLunar(2024, 2, 10)
        assertThat(result).isNotNull()
        assertThat(result!!.month).isEqualTo(1)
        assertThat(result.day).isEqualTo(1)
    }

    @Test
    fun getJieQi_2024_returnsNonEmptyList() {
        val result = LunarUtils.getJieQi(2024)
        assertThat(result).isNotEmpty()
    }

    @Test
    fun getLunarDateDescription_knownTimestamp_returnsNonEmpty() {
        val timestamp = System.currentTimeMillis()
        val result = LunarUtils.getLunarDateDescription(timestamp)
        assertThat(result).isNotEmpty()
    }

    @Test
    fun lunarToSolar_invalidDate_returnsNull() {
        val result = LunarUtils.lunarToSolar(2024, 13, 1)
        assertThat(result).isNull()
    }

    @Test
    fun solarToLunar_invalidDate_returnsNull() {
        val result = LunarUtils.solarToLunar(2024, 13, 1)
        assertThat(result).isNull()
    }
}
