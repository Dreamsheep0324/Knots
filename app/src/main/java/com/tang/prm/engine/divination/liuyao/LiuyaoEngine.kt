package com.tang.prm.engine.divination.liuyao

import com.tang.prm.domain.divination.model.*
import com.tang.prm.engine.divination.core.GanZhiCalculator
import com.tang.prm.engine.divination.core.WuXingHelper
import com.tang.prm.engine.divination.data.HexagramData
import com.tang.prm.engine.divination.data.NaJiaData
import com.tang.prm.engine.divination.data.PalaceData
import java.util.Calendar
import java.util.Date

object LiuyaoEngine {

    fun generate(yaoArray: List<Int>? = null, customDate: Date? = null): LiuyaoData {
        val targetDate = customDate ?: Date()
        val calendar = Calendar.getInstance().apply { time = targetDate }

        val ganzhi = GanZhiCalculator.fromSolar(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )
        val timestamp = targetDate.time

        val rawYaos = yaoArray ?: generateYaosByTime(timestamp, 6)
        val mainYaos = rawYaos.map { if (it == 7 || it == 9) "阳" else "阴" }
        val changedYaos = rawYaos.mapIndexed { index, yao ->
            when (yao) {
                6 -> "阳"
                9 -> "阴"
                else -> mainYaos[index]
            }
        }

        val mainBinary = mainYaos.map { if (it == "阳") "1" else "0" }.reversed().joinToString("")
        val changedBinary = changedYaos.map { if (it == "阳") "1" else "0" }.reversed().joinToString("")

        val interYaos = getInterHexagram(mainYaos)
        val interBinary = interYaos.map { if (it == "阳") "1" else "0" }.reversed().joinToString("")

        val mainHexagram = HexagramData.findByBinary(mainBinary)!!
        val changedHexagram = HexagramData.findByBinary(changedBinary)!!
        val interHexagram = HexagramData.findByBinary(interBinary)!!

        val palaceName = PalaceData.hexagramPalaceMap[mainHexagram.name]!!
        val palace = PalaceData.palaces[palaceName]!!

        val yaosInfo = getNaJiaAndLiuQin(mainHexagram.name, palace)
        val shiYing = getShiYing(mainHexagram.name, palaceName)
        val worldAndResponse = getWorldAndResponseArray(shiYing)

        val dayGan = ganzhi.day.substring(0, 1)
        val animals = WuXingHelper.getSixAnimals(dayGan)

        val voids = WuXingHelper.getVoidBranches(ganzhi.day)

        val changedYaosInfo = getNaJiaAndLiuQin(changedHexagram.name, palace)

        val changingYaos = rawYaos.mapIndexed { index, yao ->
            ChangingYao(
                position = index + 1,
                isChanging = yao == 6 || yao == 9,
                type = when (yao) { 6 -> "老阴"; 9 -> "老阳"; else -> "静爻" }
            )
        }.filter { it.isChanging }

        val special = getSpecialPattern(changingYaos.size, mainHexagram.name)

        val yaosDetail = yaosInfo.mapIndexed { index, info ->
            val isChanging = rawYaos[index] == 6 || rawYaos[index] == 9
            val changedInfo = if (isChanging) changedYaosInfo[index] else null
            LiuyaoYaoDetail(
                position = index + 1,
                rawValue = rawYaos[index],
                yaoType = mainYaos[index],
                isChanging = isChanging,
                changeType = when (rawYaos[index]) { 6 -> "老阴"; 9 -> "老阳"; else -> "静爻" },
                sixGod = animals[index],
                sixRelative = info.liuqin,
                najiaDizhi = info.dizhi,
                wuxing = info.wuxing,
                isWorld = shiYing.shi == index + 1,
                isResponse = shiYing.ying == index + 1,
                isVoid = voids.contains(info.dizhi),
                changedYao = changedInfo?.let {
                    ChangedYaoInfo(it.dizhi, it.wuxing, it.liuqin, voids.contains(it.dizhi))
                }
            )
        }

        return LiuyaoData(
            originalName = mainHexagram.name,
            changedName = changedHexagram.name,
            interName = interHexagram.name,
            ganzhi = ganzhi,
            timestamp = timestamp,
            yaoArray = rawYaos,
            changingYaos = changingYaos,
            sixGods = animals,
            sixRelatives = yaosInfo.map { it.liuqin },
            najiaDizhi = yaosInfo.map { it.dizhi },
            wuxing = yaosInfo.map { it.wuxing },
            worldAndResponse = worldAndResponse,
            voidBranches = voids,
            palace = PalaceInfo(palace.name, palace.wuxing),
            yaosDetail = yaosDetail,
            specialPattern = special.first,
            specialAdvice = special.second,
            isChaotic = special.third,
            chaoticReason = special.fourth
        )
    }

    private fun generateYaosByTime(timestamp: Long, count: Int): List<Int> {
        return (0 until count).map { i ->
            var total = 0
            for (coinIndex in 0..2) {
                val seed = (timestamp + i * 1000L + coinIndex * 97L) % 2147483647
                val bit = ((seed * 1664525L + 1013904223L) % 2147483647L) % 2
                total += if (bit == 0L) 2 else 3
            }
            total
        }
    }

    private fun getInterHexagram(mainYaos: List<String>): List<String> {
        val interLower = mainYaos.slice(1..3)
        val interUpper = mainYaos.slice(2..4)
        return interLower + interUpper
    }

    private fun getNaJiaAndLiuQin(hexagramName: String, palace: PalaceData.Palace): List<YaoInfo> {
        val najiaDizhiArray = NaJiaData.hexagramNaJia[hexagramName]!!
        return najiaDizhiArray.map { dizhi ->
            val yaoWuxing = WuXingHelper.getWuXing(dizhi)
            val liuqin = WuXingHelper.getLiuQin(palace.wuxing, yaoWuxing)
            YaoInfo(dizhi, yaoWuxing, liuqin)
        }
    }

    private data class YaoInfo(val dizhi: String, val wuxing: String, val liuqin: String)

    private data class ShiYing(val shi: Int, val ying: Int)

    private fun getShiYing(hexagramName: String, palaceName: String): ShiYing {
        val shiYaoMap = mapOf(0 to 6, 1 to 1, 2 to 2, 3 to 3, 4 to 4, 5 to 5, 6 to 4, 7 to 3)
        val hexagramsInPalace = PalaceData.palaceHexagrams[palaceName]!!
        val generation = hexagramsInPalace.indexOf(hexagramName)
        val shiYao = shiYaoMap[generation] ?: 6
        val yingYao = if (shiYao + 3 > 6) shiYao - 3 else shiYao + 3
        return ShiYing(shiYao, yingYao)
    }

    private fun getWorldAndResponseArray(shiYing: ShiYing): List<String> {
        return (1..6).map { i ->
            when (i) {
                shiYing.shi -> "世"
                shiYing.ying -> "应"
                else -> ""
            }
        }
    }

    private data class SpecialResult(
        val first: String?,
        val second: String?,
        val third: Boolean,
        val fourth: String?
    )

    private fun getSpecialPattern(changingCount: Int, mainHexagramName: String): SpecialResult {
        return when {
            changingCount == 0 -> SpecialResult("静卦", "六爻安静，以本卦卦意和世应用神为主，不取变爻之象。", false, null)
            changingCount == 5 -> SpecialResult("独静卦", "五爻俱动，一爻独静。常见取法以独静爻为关键，同时兼看变卦所示趋势。", false, null)
            changingCount == 6 && mainHexagramName == "乾为天" ->
                SpecialResult("乾卦用九", "乾卦六爻皆动，宜以用九\"见群龙无首，吉\"为主，兼参之卦总势，不按常规逐爻细断。", false, null)
            changingCount == 6 && mainHexagramName == "坤为地" ->
                SpecialResult("坤卦用六", "坤卦六爻皆动，宜以用六\"利永贞\"为主，兼参之卦总势，不按常规逐爻细断。", false, null)
            changingCount == 6 ->
                SpecialResult("全动卦", "六爻全动，宜总观本卦与变卦气势，不宜按常规逐爻细碎分断。", true, "六爻全动，属于乱动卦。传统上此类卦不宜按常规多爻细断，宜另取用神旺衰总观。")
            else -> SpecialResult(null, null, false, null)
        }
    }
}
