package com.tang.prm.domain.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ZodiacUtilsTest {

    @Nested
    @DisplayName("fromMonthDay 星座交界日")
    inner class FromMonthDayTest {

        @ParameterizedTest
        @CsvSource(
            "3, 21, ARIES", "4, 19, ARIES",
            "4, 20, TAURUS", "5, 20, TAURUS",
            "5, 21, GEMINI", "6, 21, GEMINI",
            "6, 22, CANCER", "7, 22, CANCER",
            "7, 23, LEO", "8, 22, LEO",
            "8, 23, VIRGO", "9, 22, VIRGO",
            "9, 23, LIBRA", "10, 23, LIBRA",
            "10, 24, SCORPIO", "11, 22, SCORPIO",
            "11, 23, SAGITTARIUS", "12, 21, SAGITTARIUS",
            "12, 22, CAPRICORN", "1, 19, CAPRICORN",
            "1, 20, AQUARIUS", "2, 18, AQUARIUS",
            "2, 19, PISCES", "3, 20, PISCES"
        )
        fun boundaryDates(month: Int, day: Int, expected: Zodiac) {
            assertThat(ZodiacUtils.fromMonthDay(month, day)).isEqualTo(expected)
        }

        @Test
        fun midRangeDates() {
            assertThat(ZodiacUtils.fromMonthDay(4, 5)).isEqualTo(Zodiac.ARIES)
            assertThat(ZodiacUtils.fromMonthDay(7, 1)).isEqualTo(Zodiac.CANCER)
            assertThat(ZodiacUtils.fromMonthDay(12, 31)).isEqualTo(Zodiac.CAPRICORN)
            assertThat(ZodiacUtils.fromMonthDay(1, 1)).isEqualTo(Zodiac.CAPRICORN)
        }
    }

    @Nested
    @DisplayName("fromBirthday")
    inner class FromBirthdayTest {

        @Test
        fun nullBirthday_returnsNull() {
            assertThat(ZodiacUtils.fromBirthday(null)).isNull()
        }

        @Test
        fun validBirthday_returnsCorrectZodiac() {
            // March 25 -> ARIES
            val calendar = java.util.Calendar.getInstance().apply {
                set(2000, 2, 25, 0, 0, 0) // month is 0-based
                set(java.util.Calendar.MILLISECOND, 0)
            }
            assertThat(ZodiacUtils.fromBirthday(calendar.timeInMillis)).isEqualTo(Zodiac.ARIES)
        }
    }

    @Nested
    @DisplayName("Zodiac 枚举完整性")
    inner class ZodiacEnumTest {

        @Test
        fun has12Signs() {
            assertThat(Zodiac.entries).hasSize(12)
        }

        @Test
        fun eachSignHasRequiredFields() {
            Zodiac.entries.forEach { z ->
                assertThat(z.iconName).isNotEmpty()
                assertThat(z.displayName).isNotEmpty()
                assertThat(z.startMonth).isIn(1..12)
                assertThat(z.endMonth).isIn(1..12)
            }
        }
    }
}
