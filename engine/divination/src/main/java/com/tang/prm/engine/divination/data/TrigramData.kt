package com.tang.prm.engine.divination.data

import com.tang.prm.engine.divination.model.TrigramInfo

object TrigramData {
    val byIndex = mapOf(
        1 to TrigramInfo("乾", "☰", "天", "金", listOf(1, 1, 1)),
        2 to TrigramInfo("兑", "☱", "泽", "金", listOf(1, 1, 0)),
        3 to TrigramInfo("离", "☲", "火", "火", listOf(1, 0, 1)),
        4 to TrigramInfo("震", "☳", "雷", "木", listOf(1, 0, 0)),
        5 to TrigramInfo("巽", "☴", "风", "木", listOf(0, 1, 1)),
        6 to TrigramInfo("坎", "☵", "水", "水", listOf(0, 1, 0)),
        7 to TrigramInfo("艮", "☶", "山", "土", listOf(0, 0, 1)),
        8 to TrigramInfo("坤", "☷", "地", "土", listOf(0, 0, 0))
    )

    fun findBySymbol(symbol: String): TrigramInfo? = byIndex.values.find { it.symbol == symbol }

    fun findByLines(lines: List<Int>): Pair<Int, TrigramInfo>? =
        byIndex.entries.find { (_, trigram) -> trigram.lines == lines }?.let { it.key to it.value }
}
