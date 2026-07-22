package com.tang.prm.domain.model

import com.tang.prm.domain.util.DateCalcUtils

/**
 * 计算纪念日的下次生效日期。
 *
 * 行为说明：
 * - [Anniversary.type] 为 BIRTHDAY 时：取下次生日（当年或次年）。
 * - [Anniversary.isRepeat] 为 true 时：取下次重复日期（当年或次年）。
 * - 否则（一次性纪念日）：直接返回 [Anniversary.date]。
 *
 * @return 下次生效日期的 epoch 毫秒（公历）
 */
fun Anniversary.effectiveDate(): Long = when {
    type == AnniversaryType.BIRTHDAY -> DateCalcUtils.getNextBirthdayDate(date)
    isRepeat -> DateCalcUtils.getNextRepeatDate(date)
    else -> date
}
