package com.tang.prm.engine.divination.data

data class ExternalOmenOption(
    val name: String,
    val displayName: String,
    val trigramIndex: Int,
    val trigramName: String
)

object ExternalOmenData {

    val directionOptions = listOf(
        ExternalOmenOption("东", "东", 4, "震"),
        ExternalOmenOption("东南", "东南", 5, "巽"),
        ExternalOmenOption("南", "南", 3, "离"),
        ExternalOmenOption("西南", "西南", 8, "坤"),
        ExternalOmenOption("西", "西", 2, "兑"),
        ExternalOmenOption("西北", "西北", 1, "乾"),
        ExternalOmenOption("北", "北", 6, "坎"),
        ExternalOmenOption("东北", "东北", 7, "艮")
    )

    val personOptions = listOf(
        ExternalOmenOption("老父", "领导/决策者", 1, "乾"),
        ExternalOmenOption("老妇", "照护者/后勤型", 8, "坤"),
        ExternalOmenOption("长男", "行动派/执行型", 4, "震"),
        ExternalOmenOption("长女", "沟通者/策划型", 5, "巽"),
        ExternalOmenOption("中男", "通勤者/夜归型", 6, "坎"),
        ExternalOmenOption("中女", "表达者/创作者", 3, "离"),
        ExternalOmenOption("少男", "学生/安静型", 7, "艮"),
        ExternalOmenOption("少女", "社交者/轻快型", 2, "兑")
    )

    val animalOptions = listOf(
        ExternalOmenOption("马", "大型犬/速度型动物", 1, "乾"),
        ExternalOmenOption("牛", "温顺厚重型动物", 8, "坤"),
        ExternalOmenOption("龙", "爆发型/稀有感动物", 4, "震"),
        ExternalOmenOption("鸡", "轻快鸣叫型动物", 5, "巽"),
        ExternalOmenOption("猪", "水边/夜行型动物", 6, "坎"),
        ExternalOmenOption("雉", "高辨识度羽色动物", 3, "离"),
        ExternalOmenOption("狗", "守门/宅家型动物", 7, "艮"),
        ExternalOmenOption("羊", "温和亲近型动物", 2, "兑")
    )

    val objectOptions = listOf(
        ExternalOmenOption("金玉圆器", "金属设备/圆形物", 1, "乾"),
        ExternalOmenOption("布帛陶器", "收纳容器/家居用品", 8, "坤"),
        ExternalOmenOption("竹木乐器", "运动器材/木质装置", 4, "震"),
        ExternalOmenOption("绳索长木", "线缆/通讯配件", 5, "巽"),
        ExternalOmenOption("水器液体", "饮品/液体容器", 6, "坎"),
        ExternalOmenOption("火电文书", "屏幕/灯光/电子设备", 3, "离"),
        ExternalOmenOption("石块门板", "家具/门锁/硬质结构", 7, "艮"),
        ExternalOmenOption("刀剪口器", "饰品/美妆/锐器", 2, "兑")
    )

    val soundOptions = listOf(
        ExternalOmenOption("洪亮金石", "金属撞击/广播声", 1, "乾"),
        ExternalOmenOption("沉厚低缓", "低频轰鸣/底噪", 8, "坤"),
        ExternalOmenOption("雷鸣震动", "突发提醒/震动声", 4, "震"),
        ExternalOmenOption("风声呼啸", "风噪/通风声", 5, "巽"),
        ExternalOmenOption("流水滴答", "水流/滴水声", 6, "坎"),
        ExternalOmenOption("爆裂鸣叫", "铃声/提示音", 3, "离"),
        ExternalOmenOption("闷阻叩击", "敲门/施工闷响", 7, "艮"),
        ExternalOmenOption("清脆笑语", "聊天/笑声/音乐片段", 2, "兑")
    )

    val colorOptions = listOf(
        ExternalOmenOption("金白", "银白/灰白", 1, "乾"),
        ExternalOmenOption("土黄", "米黄/卡其", 8, "坤"),
        ExternalOmenOption("青碧", "青绿/亮绿", 4, "震"),
        ExternalOmenOption("青绿", "薄荷绿/蓝绿", 5, "巽"),
        ExternalOmenOption("黑蓝", "深蓝/黑色", 6, "坎"),
        ExternalOmenOption("赤紫", "红橙/亮紫", 3, "离"),
        ExternalOmenOption("棕黄", "棕褐/岩土色", 7, "艮"),
        ExternalOmenOption("银白", "粉白/亮银", 2, "兑")
    )

    data class OmenCategory(
        val key: String,
        val label: String,
        val options: List<ExternalOmenOption>
    )

    val categories = listOf(
        OmenCategory("direction", "方位", directionOptions),
        OmenCategory("person", "人物", personOptions),
        OmenCategory("animal", "动物", animalOptions),
        OmenCategory("object", "物件", objectOptions),
        OmenCategory("sound", "声音", soundOptions),
        OmenCategory("color", "颜色", colorOptions)
    )

    val priority = listOf("direction", "person", "animal", "object", "sound", "color")

    fun resolveOmens(selections: Map<String, ExternalOmenOption>, count: Int): Triple<Int, Int, Int> {
        val mapped = priority.mapNotNull { key ->
            selections[key]?.let { key to it }
        }

        require(mapped.size >= 2) { "外应起卦至少需要选择两项" }
        require(count > 0) { "请输入数量" }

        val upperTrigramIndex = mapped[0].second.trigramIndex
        val lowerTrigramIndex = mapped[1].second.trigramIndex
        val movingYaoIndex = mod6(count)

        return Triple(upperTrigramIndex, lowerTrigramIndex, movingYaoIndex)
    }

    fun buildSummary(selections: Map<String, ExternalOmenOption>, count: Int): String {
        val categoryLabels = mapOf(
            "direction" to "方位",
            "person" to "人物",
            "animal" to "动物",
            "object" to "物件",
            "sound" to "声音",
            "color" to "颜色"
        )
        return priority.mapNotNull { key ->
            selections[key]?.let { "${categoryLabels[key]}：${it.name}（${it.trigramName}）" }
        }.joinToString("；") + "；数量：$count"
    }

    private fun mod6(value: Int): Int {
        val r = value % 6
        return if (r == 0) 6 else r
    }
}
