package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.util.DateCalcUtils
import javax.inject.Inject

data class AnniversaryDisplayInfo(
    val anniversary: Anniversary,
    val effectiveDate: Long,
    val daysUntil: Int
)

class GetAnniversaryDisplayUseCase @Inject constructor() {
    operator fun invoke(anniversary: Anniversary): AnniversaryDisplayInfo {
        val effectiveDate = when {
            anniversary.type == AnniversaryType.BIRTHDAY -> DateCalcUtils.getNextBirthdayDate(anniversary.date)
            anniversary.isRepeat -> DateCalcUtils.getNextRepeatDate(anniversary.date)
            else -> anniversary.date
        }
        val daysInfo = DateCalcUtils.calculateDaysInfo(anniversary.date)
        return AnniversaryDisplayInfo(anniversary, effectiveDate, daysInfo.daysUntil)
    }

    fun categorizeAnniversaries(anniversaries: List<Anniversary>): Triple<List<Anniversary>, List<Anniversary>, List<Anniversary>> {
        val today = DateCalcUtils.getTodayStart()
        val withEffectiveDate = anniversaries.map { ann ->
            ann to invoke(ann).effectiveDate
        }
        val all = withEffectiveDate.sortedBy { it.second }.map { it.first }
        val upcoming = withEffectiveDate.filter { it.second >= today }.sortedBy { it.second }.map { it.first }
        val past = withEffectiveDate.filter { it.second < today }.sortedByDescending { it.second }.map { it.first }
        return Triple(all, upcoming, past)
    }
}
