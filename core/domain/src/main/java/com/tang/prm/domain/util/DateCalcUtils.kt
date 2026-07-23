package com.tang.prm.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 日期计算工具。
 *
 * T-5 修复：时区固定为北京时区（[ZoneId.of]），替代原 [ZoneId.systemDefault]。
 * 原实现在跨时区 CI（如 GitHub Actions 美东时区）上 [LocalDate.now] 与开发者本机不同，
 * 导致 `calculateDaysUntil` 在生日跨日边界给出不同值，测试 flaky。
 * 项目硬约束要求所有时间使用北京时区，统一后避免时区漂移。
 */
object DateCalcUtils {

    private val defaultZoneId = ZoneId.of("Asia/Shanghai")

    fun safeDate(year: Int, month: Int, day: Int): LocalDate {
        if (month == 2 && day == 29 && !java.time.Year.isLeap(year.toLong())) {
            return LocalDate.of(year, 2, 28)
        }
        return LocalDate.of(year, month, day)
    }

    data class DaysInfo(
        val daysPassed: Int,
        val daysUntil: Int,
        val isPast: Boolean
    )

    fun getTodayStart(): Long =
        LocalDate.now(defaultZoneId).atStartOfDay(defaultZoneId).toInstant().toEpochMilli()

    /**
     * 计算从今天到目标日期的天数差（target - today，可为负）。
     *
     * 用于一次性事件（不滚到次年），与 [calculateDaysInfo] 的滚动逻辑互补。
     * B-3 修复：[GetAnniversaryDisplayUseCase] 原用 [calculateDaysInfo] 计算 daysUntil，
     * 但后者把已过去的日期滚动到次年，导致"生效日期已过"却"距今还有正 X 天"的语义矛盾。
     */
    fun daysUntilFromToday(targetMillis: Long): Int {
        val today = LocalDate.now(defaultZoneId)
        val targetDate = Instant.ofEpochMilli(targetMillis).atZone(defaultZoneId).toLocalDate()
        return java.time.temporal.ChronoUnit.DAYS.between(today, targetDate).toInt()
    }

    fun calculateDaysInfo(targetMillis: Long): DaysInfo {
        val today = LocalDate.now(defaultZoneId)
        val targetDate = Instant.ofEpochMilli(targetMillis).atZone(defaultZoneId).toLocalDate()
        val targetMonth = targetDate.monthValue
        val targetDay = targetDate.dayOfMonth
        val currentYear = today.year

        val thisYearAnniversary = safeDate(currentYear, targetMonth, targetDay)
        val isPast = thisYearAnniversary.isBefore(today)

        val lastAnniversary = if (isPast) {
            thisYearAnniversary
        } else {
            safeDate(currentYear - 1, targetMonth, targetDay)
        }

        val daysPassed = java.time.temporal.ChronoUnit.DAYS.between(lastAnniversary, today).toInt()

        // C-4 修复：复用 nextAnnualOccurrence 模板
        val nextAnniversary = nextAnnualOccurrence(targetMillis, today)

        val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, nextAnniversary).toInt()

        return DaysInfo(
            daysPassed = daysPassed,
            daysUntil = daysUntil,
            isPast = isPast
        )
    }

    fun getNextBirthdayDate(birthdayMillis: Long): Long {
        val today = LocalDate.now(defaultZoneId)
        // C-4 修复：复用 nextAnnualOccurrence 模板
        return nextAnnualOccurrence(birthdayMillis, today)
            .atStartOfDay(defaultZoneId)
            .toInstant()
            .toEpochMilli()
    }

    fun getNextRepeatDate(originalMillis: Long): Long {
        val today = LocalDate.now(defaultZoneId)
        // C-4 修复：复用 nextAnnualOccurrence 模板
        return nextAnnualOccurrence(originalMillis, today)
            .atStartOfDay(defaultZoneId)
            .toInstant()
            .toEpochMilli()
    }

    /**
     * C-4 修复：抽取"extract month/day → safeDate → roll to next year"模板。
     *
     * 原 5 个函数（calculateDaysUntil/calculateDaysInfo/getNextBirthdayDate/getNextRepeatDate
     * + 已删除的 calculateBirthdayInfo）都重复此模板。现统一为私有 helper，
     * 若 safeDate 行为变更只需修改一处。
     *
     * D-3 修复：calculateDaysUntil 已删除（无生产调用方，calculateDaysInfo 已覆盖此场景）。
     *
     * @param targetMillis 目标时间戳
     * @param today 当前日期（传入避免重复构造）
     * @return 从今天起算的下一个年度日期（今年未过则今年，已过则明年）
     */
    private fun nextAnnualOccurrence(targetMillis: Long, today: LocalDate): LocalDate {
        val targetDate = Instant.ofEpochMilli(targetMillis).atZone(defaultZoneId).toLocalDate()
        val targetMonth = targetDate.monthValue
        val targetDay = targetDate.dayOfMonth
        val currentYear = today.year

        var nextDate = safeDate(currentYear, targetMonth, targetDay)
        if (nextDate.isBefore(today)) {
            nextDate = safeDate(currentYear + 1, targetMonth, targetDay)
        }
        return nextDate
    }
}
