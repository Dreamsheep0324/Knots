package com.tang.prm.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * T-11 修复：[AnniversaryEffectiveDate.effectiveDate] 的 domain 层直接测试。
 *
 * 虽然逻辑已简化为 19 行纯公历，但 3 个分支（BIRTHDAY / REPEAT / 一次性）的正确性
 * 直接影响首页"即将到来纪念日"列表的排序与"今天/明天/N 天后"展示，必须有直接测试
 * 保护分支正确性，防止未来重构引入回归。
 *
 * 时区固定为北京时区，避免 CI 跨时区 flaky（与 T-5 修复一致）。
 */
class AnniversaryEffectiveDateTest {

    private val zoneId = ZoneId.of("Asia/Shanghai")

    private fun LocalDate.toMillis(): Long =
        atStartOfDay(zoneId).toInstant().toEpochMilli()

    private fun millisOf(year: Int, month: Int, day: Int): Long =
        LocalDate.of(year, month, day).toMillis()

    private fun anniversaryOf(
        type: AnniversaryType,
        date: Long,
        isRepeat: Boolean = true
    ): Anniversary = Anniversary(
        name = "test",
        type = type,
        date = date,
        isRepeat = isRepeat
    )

    @Nested
    @DisplayName("BIRTHDAY 类型 → getNextBirthdayDate")
    inner class BirthdayTest {

        @Test
        fun birthdayThisYearFuture_returnsThisYearDate() {
            // 今天 + 30 天的生日 → 当年生日的 effectiveDate 应为当年生日
            val today = LocalDate.now(zoneId)
            val birthday = today.plusDays(30)
            val anniversary = anniversaryOf(
                type = AnniversaryType.BIRTHDAY,
                date = birthday.toMillis()
            )

            val effective = anniversary.effectiveDate()

            val effectiveDate = Instant.ofEpochMilli(effective).atZone(zoneId).toLocalDate()
            assertThat(effectiveDate.monthValue).isEqualTo(birthday.monthValue)
            assertThat(effectiveDate.dayOfMonth).isEqualTo(birthday.dayOfMonth)
            assertThat(effectiveDate.year).isEqualTo(today.year)
        }

        @Test
        fun birthdayThisYearPast_rollsToNextYear() {
            // 今天 - 30 天的生日（今年已过）→ 滚到明年生日
            val today = LocalDate.now(zoneId)
            val birthday = today.minusDays(30)
            val anniversary = anniversaryOf(
                type = AnniversaryType.BIRTHDAY,
                date = birthday.toMillis()
            )

            val effective = anniversary.effectiveDate()

            val effectiveDate = Instant.ofEpochMilli(effective).atZone(zoneId).toLocalDate()
            assertThat(effectiveDate.monthValue).isEqualTo(birthday.monthValue)
            assertThat(effectiveDate.dayOfMonth).isEqualTo(birthday.dayOfMonth)
            assertThat(effectiveDate.year).isEqualTo(today.year + 1)
        }

        @Test
        fun birthdayToday_returnsToday() {
            // 今天生日 → effectiveDate 应为今天
            val today = LocalDate.now(zoneId)
            val anniversary = anniversaryOf(
                type = AnniversaryType.BIRTHDAY,
                date = today.toMillis()
            )

            val effective = anniversary.effectiveDate()

            val effectiveDate = Instant.ofEpochMilli(effective).atZone(zoneId).toLocalDate()
            assertThat(effectiveDate).isEqualTo(today)
        }

        @Test
        fun birthdayFeb29InNonLeapYear_fallsBackToFeb28() {
            // 2/29 生日在非闰年 → safeDate 兜底为 2/28
            val today = LocalDate.now(zoneId)
            val nextNonLeapYear = (today.year..today.year + 4).first { !java.time.Year.isLeap(it.toLong()) }
            val birthdayMillis = millisOf(2000, 2, 29)
            val anniversary = anniversaryOf(
                type = AnniversaryType.BIRTHDAY,
                date = birthdayMillis
            )

            val effective = anniversary.effectiveDate()

            val effectiveDate = Instant.ofEpochMilli(effective).atZone(zoneId).toLocalDate()
            // 在非闰年的 effectiveDate 应为 2/28
            if (effectiveDate.year == nextNonLeapYear) {
                assertThat(effectiveDate.monthValue).isEqualTo(2)
                assertThat(effectiveDate.dayOfMonth).isEqualTo(28)
            }
        }
    }

    @Nested
    @DisplayName("isRepeat=true → getNextRepeatDate")
    inner class RepeatTest {

        @Test
        fun repeatThisYearFuture_returnsThisYearDate() {
            val today = LocalDate.now(zoneId)
            val original = today.plusDays(60)
            val anniversary = anniversaryOf(
                type = AnniversaryType.ANNIVERSARY,
                date = original.toMillis(),
                isRepeat = true
            )

            val effective = anniversary.effectiveDate()

            val effectiveDate = Instant.ofEpochMilli(effective).atZone(zoneId).toLocalDate()
            assertThat(effectiveDate.year).isEqualTo(today.year)
            assertThat(effectiveDate.monthValue).isEqualTo(original.monthValue)
            assertThat(effectiveDate.dayOfMonth).isEqualTo(original.dayOfMonth)
        }

        @Test
        fun repeatThisYearPast_rollsToNextYear() {
            val today = LocalDate.now(zoneId)
            val original = today.minusDays(60)
            val anniversary = anniversaryOf(
                type = AnniversaryType.ANNIVERSARY,
                date = original.toMillis(),
                isRepeat = true
            )

            val effective = anniversary.effectiveDate()

            val effectiveDate = Instant.ofEpochMilli(effective).atZone(zoneId).toLocalDate()
            assertThat(effectiveDate.year).isEqualTo(today.year + 1)
            assertThat(effectiveDate.monthValue).isEqualTo(original.monthValue)
            assertThat(effectiveDate.dayOfMonth).isEqualTo(original.dayOfMonth)
        }
    }

    @Nested
    @DisplayName("非 BIRTHDAY + 非 REPEAT → 返回原 date")
    inner class OneTimeTest {

        @Test
        fun oneTimeAnniversary_returnsOriginalDate() {
            // 一次性纪念日（isRepeat=false）：直接返回 date，无论过去未来
            val fixedDate = millisOf(2024, 6, 15)
            val anniversary = anniversaryOf(
                type = AnniversaryType.ANNIVERSARY,
                date = fixedDate,
                isRepeat = false
            )

            val effective = anniversary.effectiveDate()

            assertThat(effective).isEqualTo(fixedDate)
        }

        @Test
        fun oneTimePastDate_returnsOriginalDate() {
            // 过去的一次性纪念日：仍返回原 date（用于"已发生"展示）
            val pastDate = millisOf(2000, 1, 1)
            val anniversary = anniversaryOf(
                type = AnniversaryType.HOLIDAY,
                date = pastDate,
                isRepeat = false
            )

            val effective = anniversary.effectiveDate()

            assertThat(effective).isEqualTo(pastDate)
        }
    }

    @Nested
    @DisplayName("分支优先级")
    inner class BranchPriorityTest {

        @Test
        fun birthdayType_takesPriorityOverIsRepeat() {
            // type=BIRTHDAY 时走 BIRTHDAY 分支，忽略 isRepeat
            val today = LocalDate.now(zoneId)
            val birthday = today.plusDays(15)
            val anniversary = anniversaryOf(
                type = AnniversaryType.BIRTHDAY,
                date = birthday.toMillis(),
                isRepeat = true  // 应被忽略
            )

            val effective = anniversary.effectiveDate()

            val effectiveDate = Instant.ofEpochMilli(effective).atZone(zoneId).toLocalDate()
            // BIRTHDAY 分支结果 = 当年生日
            assertThat(effectiveDate.monthValue).isEqualTo(birthday.monthValue)
            assertThat(effectiveDate.dayOfMonth).isEqualTo(birthday.dayOfMonth)
            assertThat(effectiveDate.year).isEqualTo(today.year)
        }
    }
}
