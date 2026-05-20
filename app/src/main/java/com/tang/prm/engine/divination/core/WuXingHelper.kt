package com.tang.prm.engine.divination.core

object WuXingHelper {

    private val WUXING_MAP = mapOf(
        "金" to listOf("申", "酉"),
        "木" to listOf("寅", "卯"),
        "水" to listOf("子", "亥"),
        "火" to listOf("巳", "午"),
        "土" to listOf("辰", "戌", "丑", "未")
    )

    private val LIUQIN_MAP = mapOf(
        "金" to mapOf("金" to "兄弟", "木" to "妻财", "水" to "子孙", "火" to "官鬼", "土" to "父母"),
        "木" to mapOf("木" to "兄弟", "土" to "妻财", "火" to "子孙", "金" to "官鬼", "水" to "父母"),
        "水" to mapOf("水" to "兄弟", "火" to "妻财", "木" to "子孙", "土" to "官鬼", "金" to "父母"),
        "火" to mapOf("火" to "兄弟", "金" to "妻财", "土" to "子孙", "水" to "官鬼", "木" to "父母"),
        "土" to mapOf("土" to "兄弟", "水" to "妻财", "金" to "子孙", "木" to "官鬼", "火" to "父母")
    )

    private val SHENG_MAP = mapOf("金" to "水", "水" to "木", "木" to "火", "火" to "土", "土" to "金")
    private val KE_MAP = mapOf("金" to "木", "木" to "土", "土" to "水", "水" to "火", "火" to "金")

    private val SIX_ANIMALS_START = mapOf(
        "甲" to "青龙", "乙" to "青龙",
        "丙" to "朱雀", "丁" to "朱雀",
        "戊" to "勾陈", "己" to "螣蛇",
        "庚" to "白虎", "辛" to "白虎",
        "壬" to "玄武", "癸" to "玄武"
    )

    private val VOID_MAP = mapOf(
        "甲子" to listOf("戌", "亥"), "乙丑" to listOf("戌", "亥"), "丙寅" to listOf("戌", "亥"),
        "丁卯" to listOf("戌", "亥"), "戊辰" to listOf("戌", "亥"), "己巳" to listOf("戌", "亥"),
        "庚午" to listOf("戌", "亥"), "辛未" to listOf("戌", "亥"), "壬申" to listOf("戌", "亥"),
        "癸酉" to listOf("戌", "亥"),
        "甲戌" to listOf("申", "酉"), "乙亥" to listOf("申", "酉"), "丙子" to listOf("申", "酉"),
        "丁丑" to listOf("申", "酉"), "戊寅" to listOf("申", "酉"), "己卯" to listOf("申", "酉"),
        "庚辰" to listOf("申", "酉"), "辛巳" to listOf("申", "酉"), "壬午" to listOf("申", "酉"),
        "癸未" to listOf("申", "酉"),
        "甲申" to listOf("午", "未"), "乙酉" to listOf("午", "未"), "丙戌" to listOf("午", "未"),
        "丁亥" to listOf("午", "未"), "戊子" to listOf("午", "未"), "己丑" to listOf("午", "未"),
        "庚寅" to listOf("午", "未"), "辛卯" to listOf("午", "未"), "壬辰" to listOf("午", "未"),
        "癸巳" to listOf("午", "未"),
        "甲午" to listOf("辰", "巳"), "乙未" to listOf("辰", "巳"), "丙申" to listOf("辰", "巳"),
        "丁酉" to listOf("辰", "巳"), "戊戌" to listOf("辰", "巳"), "己亥" to listOf("辰", "巳"),
        "庚子" to listOf("辰", "巳"), "辛丑" to listOf("辰", "巳"), "壬寅" to listOf("辰", "巳"),
        "癸卯" to listOf("辰", "巳"),
        "甲辰" to listOf("寅", "卯"), "乙巳" to listOf("寅", "卯"), "丙午" to listOf("寅", "卯"),
        "丁未" to listOf("寅", "卯"), "戊申" to listOf("寅", "卯"), "己酉" to listOf("寅", "卯"),
        "庚戌" to listOf("寅", "卯"), "辛亥" to listOf("寅", "卯"), "壬子" to listOf("寅", "卯"),
        "癸丑" to listOf("寅", "卯"),
        "甲寅" to listOf("子", "丑"), "乙卯" to listOf("子", "丑"), "丙辰" to listOf("子", "丑"),
        "丁巳" to listOf("子", "丑"), "戊午" to listOf("子", "丑"), "己未" to listOf("子", "丑"),
        "庚申" to listOf("子", "丑"), "辛酉" to listOf("子", "丑"), "壬戌" to listOf("子", "丑"),
        "癸亥" to listOf("子", "丑")
    )

    fun getWuXing(dizhi: String): String {
        return WUXING_MAP.entries.find { (_, branches) -> dizhi in branches }?.key ?: "未知"
    }

    fun getLiuQin(palaceWuXing: String, yaoWuXing: String): String {
        return LIUQIN_MAP[palaceWuXing]?.get(yaoWuXing) ?: "未知"
    }

    fun getSixAnimals(dayGan: String): List<String> {
        val animals = listOf("青龙", "朱雀", "勾陈", "螣蛇", "白虎", "玄武")
        val startAnimal = SIX_ANIMALS_START[dayGan] ?: "青龙"
        val startIndex = animals.indexOf(startAnimal)
        return (0 until 6).map { i -> animals[(startIndex + i) % 6] }
    }

    fun getVoidBranches(dayGanZhi: String): List<String> {
        return VOID_MAP[dayGanZhi] ?: emptyList()
    }

    fun getElementRelation(yongElement: String, tiElement: String): String {
        if (yongElement.isEmpty() || tiElement.isEmpty()) return "未知"
        if (yongElement == tiElement) return "体用比和"
        if (SHENG_MAP[yongElement] == tiElement) return "用生体"
        if (SHENG_MAP[tiElement] == yongElement) return "体生用"
        if (KE_MAP[yongElement] == tiElement) return "用克体"
        if (KE_MAP[tiElement] == yongElement) return "体克用"
        return "未知"
    }

    fun getElementSeasonState(element: String, season: String): String {
        val states = mapOf(
            "春" to mapOf("木" to "旺", "火" to "相", "水" to "休", "金" to "囚", "土" to "死"),
            "夏" to mapOf("火" to "旺", "土" to "相", "木" to "休", "水" to "囚", "金" to "死"),
            "秋" to mapOf("金" to "旺", "水" to "相", "土" to "休", "火" to "囚", "木" to "死"),
            "冬" to mapOf("水" to "旺", "木" to "相", "金" to "休", "土" to "囚", "火" to "死")
        )
        return states[season]?.get(element) ?: "未知"
    }

    fun getSeasonByMonth(month: Int): String = when (month) {
        in 1..3 -> "春"
        in 4..6 -> "夏"
        in 7..9 -> "秋"
        else -> "冬"
    }

    private val JIEQI_SEASON = mapOf(
        "立春" to "春", "雨水" to "春", "惊蛰" to "春", "春分" to "春", "清明" to "春", "谷雨" to "春",
        "立夏" to "夏", "小满" to "夏", "芒种" to "夏", "夏至" to "夏", "小暑" to "夏", "大暑" to "夏",
        "立秋" to "秋", "处暑" to "秋", "白露" to "秋", "秋分" to "秋", "寒露" to "秋", "霜降" to "秋",
        "立冬" to "冬", "小雪" to "冬", "大雪" to "冬", "冬至" to "冬", "小寒" to "冬", "大寒" to "冬"
    )

    fun getSeasonByJieQi(calendar: java.util.Calendar): String {
        return try {
            val solar = com.nlf.calendar.Solar.fromYmd(
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH) + 1,
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
            )
            val lunar = solar.lunar
            val jieQi = lunar.jieQi
            val season = JIEQI_SEASON[jieQi]
            if (season != null) season else {
                val month = kotlin.math.abs(lunar.month)
                getSeasonByMonth(month)
            }
        } catch (e: Exception) {
            val month = calendar.get(java.util.Calendar.MONTH) + 1
            getSeasonByMonth(month)
        }
    }
}
