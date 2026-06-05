package com.tang.prm.engine.divination.data

object PalaceData {
    data class Palace(val name: String, val wuxing: String)

    val palaces = mapOf(
        "乾" to Palace("乾", "金"),
        "兑" to Palace("兑", "金"),
        "离" to Palace("离", "火"),
        "震" to Palace("震", "木"),
        "巽" to Palace("巽", "木"),
        "坎" to Palace("坎", "水"),
        "艮" to Palace("艮", "土"),
        "坤" to Palace("坤", "土")
    )

    val palaceHexagrams = mapOf(
        "乾" to listOf("乾为天", "天风姤", "天山遁", "天地否", "风地观", "山地剥", "火地晋", "火天大有"),
        "坎" to listOf("坎为水", "水泽节", "水雷屯", "水火既济", "泽火革", "雷火丰", "地火明夷", "地水师"),
        "艮" to listOf("艮为山", "山火贲", "山天大畜", "山泽损", "火泽睽", "天泽履", "风泽中孚", "风山渐"),
        "震" to listOf("震为雷", "雷地豫", "雷水解", "雷风恒", "地风升", "水风井", "泽风大过", "泽雷随"),
        "巽" to listOf("巽为风", "风天小畜", "风火家人", "风雷益", "天雷无妄", "火雷噬嗑", "山雷颐", "山风蛊"),
        "离" to listOf("离为火", "火山旅", "火风鼎", "火水未济", "山水蒙", "风水涣", "天水讼", "天火同人"),
        "坤" to listOf("坤为地", "地雷复", "地泽临", "地天泰", "雷天大壮", "泽天夬", "水天需", "水地比"),
        "兑" to listOf("兑为泽", "泽水困", "泽地萃", "泽山咸", "水山蹇", "地山谦", "雷山小过", "雷泽归妹")
    )

    val hexagramPalaceMap: Map<String, String> = palaceHexagrams.entries
        .flatMap { (palace, hexagrams) -> hexagrams.map { it to palace } }
        .toMap()
}
