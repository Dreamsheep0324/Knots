package com.tang.prm.domain.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * 星座计算工具。
 *
 * B-4 修复：原实现用 [java.util.Calendar.getInstance]（设备默认时区）解析生日 millis，
 * 星座交界日（如 3/21 白羊起始）在跨时区场景下可能落到错误的月份/日期。
 * 统一改用 [ZoneId.of] 固定为北京时区，确保同一生日 millis 在任何设备上都返回同一星座。
 */
object ZodiacUtils {

    private val BEIJING_ZONE = ZoneId.of("Asia/Shanghai")

    fun fromBirthday(birthday: Long?): Zodiac? {
        if (birthday == null) return null
        val zdt: ZonedDateTime = Instant.ofEpochMilli(birthday).atZone(BEIJING_ZONE)
        return fromMonthDay(zdt.monthValue, zdt.dayOfMonth)
    }

    /**
     * 根据月日返回所属星座。
     *
     * D-4 修复：原实现三分支（startMonth==endMonth / startMonth<endMonth / else）中
     * 后两个分支体完全相同，且 fallback 不可达（12 星座闭区间连续覆盖全年）。
     * 合并为单一谓词 `firstOrNull ?: CAPRICORN`，既消除重复又保留防御性 fallback
     * （若未来 Zodiac enum 被误删一项，避免 `first` 抛 NoSuchElementException）。
     */
    fun fromMonthDay(month: Int, day: Int): Zodiac =
        Zodiac.entries.firstOrNull { z ->
            (month == z.startMonth && day >= z.startDay) ||
                (month == z.endMonth && day <= z.endDay)
        } ?: Zodiac.CAPRICORN
}
