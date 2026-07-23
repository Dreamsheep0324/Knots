package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.util.DateCalcUtils
import javax.inject.Inject

data class AnniversaryDisplayInfo(
    val anniversary: Anniversary,
    val effectiveDate: Long,
    val daysUntil: Int
)

/**
 * Q-4 修复：categorizeAnniversaries 返回值由 [Triple] 改为命名 data class，
 * 避免调用方 `(all, upcoming, past)` 解构时语义混淆（Triple 三位类型相同易调换顺序）。
 */
data class CategorizedAnniversaries(
    val all: List<Anniversary>,
    val upcoming: List<Anniversary>,
    val past: List<Anniversary>
)

class GetAnniversaryDisplayUseCase @Inject constructor() {
    /**
     * B-3 修复：原实现用 [DateCalcUtils.calculateDaysInfo] 计算 daysUntil，
     * 但该方法内部会把已过去的日期滚动到次年，导致一次性且已过去的纪念日
     * 同时报告"生效日期已过"和"距今还有正 X 天"的语义矛盾。
     *
     * 现改用 [Anniversary.effectiveDate] 成员方法（M-3 修复：消除内联复制）
     * + [DateCalcUtils.daysUntilFromToday]（不滚动到次年，差值可为负）。
     */
    operator fun invoke(anniversary: Anniversary): AnniversaryDisplayInfo {
        val effectiveDate = anniversary.effectiveDate()
        val daysUntil = DateCalcUtils.daysUntilFromToday(effectiveDate)
        return AnniversaryDisplayInfo(anniversary, effectiveDate, daysUntil)
    }

    fun categorizeAnniversaries(anniversaries: List<Anniversary>): CategorizedAnniversaries {
        val today = DateCalcUtils.getTodayStart()
        // M-3 修复：直接调用 ann.effectiveDate() 而非 invoke(ann).effectiveDate，
        // 避免对每个纪念日无意义计算 daysUntil（daysUntilFromToday 涉及 LocalDate.now + atZone + between）。
        val withEffectiveDate = anniversaries.map { ann ->
            ann to ann.effectiveDate()
        }
        val all = withEffectiveDate.sortedBy { it.second }.map { it.first }
        val upcoming = withEffectiveDate.filter { it.second >= today }.sortedBy { it.second }.map { it.first }
        val past = withEffectiveDate.filter { it.second < today }.sortedByDescending { it.second }.map { it.first }
        return CategorizedAnniversaries(all, upcoming, past)
    }
}
