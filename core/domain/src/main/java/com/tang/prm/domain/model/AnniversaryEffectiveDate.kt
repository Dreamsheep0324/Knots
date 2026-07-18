package com.tang.prm.domain.model

import com.tang.prm.domain.util.DateCalcUtils
import com.tang.prm.domain.util.LunarUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 计算纪念日的下次生效日期（B-5 修复）。
 *
 * 设计目的：
 * - 把原本藏在 `AnniversaryRepositoryImpl` 内的 private `effectiveDate()` 提到 domain 层，
 *   让 UI 层（如 `OrbitalCalendarState`）也能拿到正确的公历生效日期，避免农历纪念日
 *   用原始 `date` 字段被当作公历解析，导致在错误月份显示信号点。
 * - 同时消除 Repository 内的 private 实现，单一真相源。
 *
 * 行为说明：
 * - [Anniversary.type] 为 BIRTHDAY 时：取下次生日（当年或次年）。
 * - [Anniversary.isRepeat] 为 true 时：取下次重复日期（当年或次年）。
 * - 否则（一次性纪念日）：直接返回 [Anniversary.date]。
 * - 任意 isLunar=true 分支，先用 [lunarToNextSolarDate] 把农历月日转当年/次年公历，
 *   再走对应的 DateCalcUtils 计算；转换失败回退到原始 date，保证不抛异常。
 *
 * @return 下次生效日期的 epoch 毫秒（公历）
 */
fun Anniversary.effectiveDate(): Long = when {
    type == AnniversaryType.BIRTHDAY -> {
        if (isLunar) {
            val solarDate = lunarToNextSolarDate(date, isLeapMonth)
            if (solarDate != null) DateCalcUtils.getNextBirthdayDate(solarDate)
            else DateCalcUtils.getNextBirthdayDate(date)  // 转换失败 fallback
        } else {
            DateCalcUtils.getNextBirthdayDate(date)
        }
    }
    isRepeat -> {
        if (isLunar) {
            val solarDate = lunarToNextSolarDate(date, isLeapMonth)
            if (solarDate != null) DateCalcUtils.getNextRepeatDate(solarDate)
            else DateCalcUtils.getNextRepeatDate(date)
        } else {
            DateCalcUtils.getNextRepeatDate(date)
        }
    }
    else -> date
}

/**
 * 将存储的农历日期（epoch millis）转换为当年或次年的公历日期（epoch millis）。
 *
 * 从存储日期中提取农历月、日，分别尝试当年和次年转换，取第一个未过去的公历日期。
 *
 * @param lunarMillis 农历日期的 epoch 毫秒
 * @param isLeapMonth 是否为闰月
 * @return 公历日期的 epoch 毫秒，转换失败返回 null
 */
private fun lunarToNextSolarDate(lunarMillis: Long, isLeapMonth: Boolean): Long? {
    val zone = ZoneId.systemDefault()
    val lunarInstant = Instant.ofEpochMilli(lunarMillis).atZone(zone).toLocalDate()
    val lunarYear = lunarInstant.year
    val lunarMonth = lunarInstant.monthValue
    val lunarDay = lunarInstant.dayOfMonth
    val today = LocalDate.now()

    // 尝试当年农历转公历
    val solarThisYear = LunarUtils.lunarToSolar(lunarYear, lunarMonth, lunarDay, isLeapMonth)
    if (solarThisYear != null) {
        val solarDate = LocalDate.of(solarThisYear.first, solarThisYear.second, solarThisYear.third)
        if (!solarDate.isBefore(today)) {
            return solarDate.atStartOfDay(zone).toInstant().toEpochMilli()
        }
    }

    // 当年已过或无效，尝试明年
    val solarNextYear = LunarUtils.lunarToSolar(lunarYear + 1, lunarMonth, lunarDay, isLeapMonth)
    return solarNextYear?.let {
        val solarDate = LocalDate.of(it.first, it.second, it.third)
        solarDate.atStartOfDay(zone).toInstant().toEpochMilli()
    }
}
