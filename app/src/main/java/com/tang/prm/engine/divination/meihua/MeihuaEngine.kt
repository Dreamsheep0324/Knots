package com.tang.prm.engine.divination.meihua

import com.tang.prm.domain.divination.model.*
import com.tang.prm.engine.divination.core.GanZhiCalculator
import com.tang.prm.engine.divination.core.WuXingHelper
import com.tang.prm.engine.divination.data.ExternalOmenData
import com.tang.prm.engine.divination.data.ExternalOmenOption
import com.tang.prm.engine.divination.data.HexagramData
import com.tang.prm.engine.divination.data.TrigramData
import java.util.Calendar
import java.util.Date

object MeihuaEngine {

    enum class Method { TIME, NUMBER, RANDOM, EXTERNAL }

    private fun mod8(value: Int): Int {
        val r = value % 8
        return if (r == 0) 8 else r
    }

    private fun mod6(value: Int): Int {
        val r = value % 6
        return if (r == 0) 6 else r
    }

    fun generate(
        method: Method = Method.TIME,
        number: Int = 0,
        numberB: Int = 0,
        externalSelections: Map<String, ExternalOmenOption> = emptyMap(),
        externalCount: Int = 0,
        customDate: Date? = null
    ): MeihuaData {
        val targetDate = customDate ?: Date()
        val calendar = Calendar.getInstance().apply { time = targetDate }
        val timestamp = targetDate.time

        val ganzhi = GanZhiCalculator.fromSolar(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )
        val (lunarMonth, lunarDay) = GanZhiCalculator.getLunarMonthDay(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val methodResult = when (method) {
            Method.TIME -> resolveTimeMethod(ganzhi, lunarMonth, lunarDay)
            Method.NUMBER -> resolveNumberMethod(number, numberB, ganzhi)
            Method.RANDOM -> resolveRandomMethod()
            Method.EXTERNAL -> resolveExternalMethod(externalSelections, externalCount)
        }

        val upperTrigram = TrigramData.byIndex[methodResult.upperTrigramIndex]!!
        val lowerTrigram = TrigramData.byIndex[methodResult.lowerTrigramIndex]!!

        val mainHexagram = findHexagramByTrigrams(methodResult.upperTrigramIndex, methodResult.lowerTrigramIndex)

        val mainLines = lowerTrigram.lines + upperTrigram.lines
        val interLowerLines = mainLines.slice(1..3)
        val interUpperLines = mainLines.slice(2..4)
        val interLowerTrigram = TrigramData.findByLines(interLowerLines)
        val interUpperTrigram = TrigramData.findByLines(interUpperLines)
        val interHexagram = if (interLowerTrigram != null && interUpperTrigram != null) {
            findHexagramByTrigrams(interUpperTrigram.first, interLowerTrigram.first)
        } else null

        val changedLines = mainLines.toMutableList()
        changedLines[methodResult.movingYaoIndex - 1] = 1 - changedLines[methodResult.movingYaoIndex - 1]
        val changedLowerLines = changedLines.slice(0..2)
        val changedUpperLines = changedLines.slice(3..5)
        val changedLowerTrigram = TrigramData.findByLines(changedLowerLines)
        val changedUpperTrigram = TrigramData.findByLines(changedUpperLines)
        val changingHexagram = if (changedLowerTrigram != null && changedUpperTrigram != null) {
            findHexagramByTrigrams(changedUpperTrigram.first, changedLowerTrigram.first)
        } else null

        val tiYong = resolveTiYongByMovingYao(upperTrigram, lowerTrigram, methodResult.movingYaoIndex)
        val changedTiYong = if (changedUpperTrigram != null && changedLowerTrigram != null) {
            resolveTiYongByMovingYao(
                TrigramData.byIndex[changedUpperTrigram.first]!!,
                TrigramData.byIndex[changedLowerTrigram.first]!!,
                methodResult.movingYaoIndex
            )
        } else null

        val yaosDetail = mainLines.mapIndexed { index, line ->
            val trigram = if (index < 3) lowerTrigram else upperTrigram
            MeihuaYaoDetail(
                position = index + 1,
                yaoType = if (line == 1) "阳" else "阴",
                isChanging = index == methodResult.movingYaoIndex - 1,
                tiYong = if (trigram.name == tiYong.tiGua.name) "体" else "用"
            )
        }

        val season = WuXingHelper.getSeasonByJieQi(calendar)
        val tiSeasonState = WuXingHelper.getElementSeasonState(tiYong.tiGua.element, season)
        val yongSeasonState = WuXingHelper.getElementSeasonState(tiYong.yongGua.element, season)

        return MeihuaData(
            originalName = mainHexagram.name,
            changedName = changingHexagram?.name ?: "",
            interName = interHexagram?.name ?: "",
            ganzhi = ganzhi,
            timestamp = timestamp,
            tiGua = TiYongGuaInfo(tiYong.tiGua.name, tiYong.tiGua.element, tiYong.tiGua.nature),
            yongGua = TiYongGuaInfo(tiYong.yongGua.name, tiYong.yongGua.element, tiYong.yongGua.nature),
            changedTiGua = changedTiYong?.let { TiYongGuaInfo(it.tiGua.name, it.tiGua.element, it.tiGua.nature) },
            changedYongGua = changedTiYong?.let { TiYongGuaInfo(it.yongGua.name, it.yongGua.element, it.yongGua.nature) },
            movingYao = MovingYaoInfo(
                position = methodResult.movingYaoIndex,
                description = "第${methodResult.movingYaoIndex}爻动",
                yaoName = listOf("初爻", "二爻", "三爻", "四爻", "五爻", "上爻")[methodResult.movingYaoIndex - 1]
            ),
            analysis = MeihuaAnalysis(
                season = season,
                tiYongRelation = WuXingHelper.getElementRelation(tiYong.yongGua.element, tiYong.tiGua.element),
                tiSeasonState = tiSeasonState,
                yongSeasonState = yongSeasonState,
                inter1Relation = interLowerTrigram?.let {
                    WuXingHelper.getElementRelation(it.second.element, tiYong.tiGua.element)
                } ?: "无",
                inter2Relation = interUpperTrigram?.let {
                    WuXingHelper.getElementRelation(it.second.element, tiYong.tiGua.element)
                } ?: "无",
                changedRelation = changedTiYong?.let {
                    WuXingHelper.getElementRelation(it.yongGua.element, it.tiGua.element)
                } ?: "无变卦",
                changedTiYongRelation = changedTiYong?.let {
                    WuXingHelper.getElementRelation(it.yongGua.element, it.tiGua.element)
                } ?: "无变卦"
            ),
            mainHexagram = HexagramDetail(
                mainHexagram.name, mainHexagram.symbol,
                upperTrigram.name, lowerTrigram.name, mainHexagram.description,
                mainHexagram.guaciMeaning, mainHexagram.hexagramMeaning
            ),
            interHexagram = interHexagram?.let {
                HexagramDetail(
                    it.name, it.symbol,
                    interUpperTrigram?.second?.name ?: "", interLowerTrigram?.second?.name ?: "", it.description,
                    it.guaciMeaning, it.hexagramMeaning
                )
            },
            changedHexagram = changingHexagram?.let {
                HexagramDetail(
                    it.name, it.symbol,
                    changedUpperTrigram?.second?.name ?: "", changedLowerTrigram?.second?.name ?: "", it.description,
                    it.guaciMeaning, it.hexagramMeaning
                )
            },
            yaosDetail = yaosDetail,
            calculation = methodResult.calculation
        )
    }

    private data class MethodResult(
        val upperTrigramIndex: Int,
        val lowerTrigramIndex: Int,
        val movingYaoIndex: Int,
        val calculation: MeihuaCalculation
    )

    private data class TiYongResult(
        val tiGua: TrigramInfo,
        val yongGua: TrigramInfo
    )

    private fun resolveTimeMethod(ganzhi: GanZhiInfo, lunarMonth: Int, lunarDay: Int): MethodResult {
        val yearZhiIndex = GanZhiCalculator.getYearZhiIndex(ganzhi.year)
        val timeZhiIndex = GanZhiCalculator.getTimeZhiIndex(ganzhi.hour)

        val upperTrigramIndex = mod8(yearZhiIndex + lunarMonth + lunarDay)
        val lowerTrigramIndex = mod8(yearZhiIndex + lunarMonth + lunarDay + timeZhiIndex)
        val movingYaoIndex = mod6(yearZhiIndex + lunarMonth + lunarDay + timeZhiIndex)

        return MethodResult(
            upperTrigramIndex, lowerTrigramIndex, movingYaoIndex,
            MeihuaCalculation(
                method = "年月日时起卦法", methodKey = "time",
                yearZhi = ganzhi.year.substring(1, 2), yearZhiIndex = yearZhiIndex,
                month = lunarMonth, day = lunarDay,
                timeZhi = ganzhi.hour.substring(1, 2), timeZhiIndex = timeZhiIndex,
                upperTrigramIndex = upperTrigramIndex, lowerTrigramIndex = lowerTrigramIndex,
                movingYaoIndex = movingYaoIndex
            )
        )
    }

    private fun resolveNumberMethod(numberA: Int, numberB: Int, ganzhi: GanZhiInfo): MethodResult {
        require(numberA > 0) { "第一组数字必须为正整数" }
        require(numberB > 0) { "第二组数字必须为正整数" }

        val upperTrigramIndex = mod8(numberA)
        val lowerTrigramIndex = mod8(numberB)
        val movingYaoIndex = mod6(numberA + numberB)

        return MethodResult(
            upperTrigramIndex, lowerTrigramIndex, movingYaoIndex,
            MeihuaCalculation(
                method = "数字起卦法", methodKey = "number",
                number = numberA, numberB = numberB,
                upperTrigramIndex = upperTrigramIndex, lowerTrigramIndex = lowerTrigramIndex,
                movingYaoIndex = movingYaoIndex
            )
        )
    }

    private fun resolveRandomMethod(): MethodResult {
        val upperTrigramIndex = (1..8).random()
        val lowerTrigramIndex = (1..8).random()
        val movingYaoIndex = (1..6).random()

        return MethodResult(
            upperTrigramIndex, lowerTrigramIndex, movingYaoIndex,
            MeihuaCalculation(
                method = "随机起卦法", methodKey = "random",
                upperTrigramIndex = upperTrigramIndex, lowerTrigramIndex = lowerTrigramIndex,
                movingYaoIndex = movingYaoIndex
            )
        )
    }

    private fun resolveExternalMethod(
        selections: Map<String, ExternalOmenOption>,
        count: Int
    ): MethodResult {
        val (upperTrigramIndex, lowerTrigramIndex, movingYaoIndex) =
            ExternalOmenData.resolveOmens(selections, count)

        val summary = ExternalOmenData.buildSummary(selections, count)

        return MethodResult(
            upperTrigramIndex, lowerTrigramIndex, movingYaoIndex,
            MeihuaCalculation(
                method = "外应起卦法", methodKey = "external",
                externalSummary = summary,
                upperTrigramIndex = upperTrigramIndex, lowerTrigramIndex = lowerTrigramIndex,
                movingYaoIndex = movingYaoIndex
            )
        )
    }

    private fun resolveTiYongByMovingYao(
        upper: TrigramInfo,
        lower: TrigramInfo,
        movingYaoIndex: Int
    ): TiYongResult {
        return if (movingYaoIndex > 3) {
            TiYongResult(tiGua = lower, yongGua = upper)
        } else {
            TiYongResult(tiGua = upper, yongGua = lower)
        }
    }

    private fun findHexagramByTrigrams(upperIndex: Int, lowerIndex: Int): HexagramInfo {
        val upperTrigram = TrigramData.byIndex[upperIndex]!!
        val lowerTrigram = TrigramData.byIndex[lowerIndex]!!
        val symbol = "${upperTrigram.symbol}${lowerTrigram.symbol}"
        return HexagramData.all.find { it.symbol == symbol }!!
    }
}
