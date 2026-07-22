package com.tang.prm.domain.constant

object CircleConstants {

    /**
     * B-17 修复：[Circle.color] 默认值的唯一来源。
     *
     * 历史问题：[com.tang.prm.domain.model.Circle] 默认色 `#6366F1` 与 [PresetColors] 第一项 `#2196F3`
     * 不一致，新建 Circle 不传色得到 indigo，用户从调色板点第一项得到 blue，两套「默认」语义冲突。
     * 提取常量后，[com.tang.prm.domain.model.Circle] 与 [PresetColors] 都从此处派生，保证一致。
     */
    const val DEFAULT_CIRCLE_COLOR = "#6366F1"

    val PresetColors = listOf(
        DEFAULT_CIRCLE_COLOR to "靛紫",
        "#2196F3" to "科技蓝",
        "#00BCD4" to "青蓝",
        "#3F51B5" to "靛蓝",
        "#673AB7" to "深紫",
        "#9C27B0" to "紫色",
        "#E91E63" to "粉红",
        "#F44336" to "红色",
        "#FF5722" to "橙红",
        "#FF9800" to "橙色",
        "#FFC107" to "琥珀",
        "#8BC34A" to "浅绿",
        "#4CAF50" to "绿色"
    )

    val WaveformTypes = listOf(
        "sine" to "正弦波",
        "cosine" to "余弦波",
        "square" to "方波",
        "sawtooth" to "锯齿波",
        "triangle" to "三角波",
        "pulse" to "脉冲波",
        "noise" to "噪声波",
        "heartbeat" to "心跳波",
        "exponential" to "指数波",
        "damped" to "阻尼波",
        "step" to "阶梯波",
        "compound" to "复合波"
    )
}
