package com.tang.prm.feature.reflect.thoughts

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky

internal val ThoughtTypeColor = mapOf(
    ThoughtType.FRIEND to SignalAmber,
    ThoughtType.PLAN to SignalSky,
    ThoughtType.MURMUR to SignalPurple
)

internal val ThoughtTypeBg = mapOf(
    ThoughtType.FRIEND to SignalAmber.copy(alpha = AnimationTokens.Alpha.faint),
    ThoughtType.PLAN to SignalSky.copy(alpha = AnimationTokens.Alpha.faint),
    ThoughtType.MURMUR to SignalPurple.copy(alpha = AnimationTokens.Alpha.faint)
)

internal val ThoughtTypeIcon = mapOf(
    ThoughtType.FRIEND to Icons.Default.Group,
    ThoughtType.PLAN to Icons.Default.TaskAlt,
    ThoughtType.MURMUR to Icons.Default.Lightbulb
)

internal val ThoughtTypeLabel = mapOf(
    ThoughtType.FRIEND to "伙伴",
    ThoughtType.PLAN to "计划",
    ThoughtType.MURMUR to "碎碎念"
)

/**
 * C-2 修复：Thought 的颜色/背景/图标/标签一次性聚合返回，替代各调用方重复的 4 行 fallback。
 *
 * 封装为 data class 比扩展为 4 个独立属性更优——避免 4 次独立 Map 查找，
 * 且 fallback 值（SignalAmber / Lightbulb / "想法"）在单一位置维护。
 */
internal data class ThoughtStyle(
    val color: Color,
    val bg: Color,
    val icon: ImageVector,
    val label: String
)

internal val Thought.style: ThoughtStyle
    @Composable @ReadOnlyComposable get() = ThoughtStyle(
        color = ThoughtTypeColor[type] ?: SignalAmber,
        bg = ThoughtTypeBg[type] ?: SignalAmber.copy(alpha = AnimationTokens.Alpha.faint),
        icon = ThoughtTypeIcon[type] ?: Icons.Default.Lightbulb,
        label = ThoughtTypeLabel[type] ?: "想法"
    )

internal val ThoughtType.style: ThoughtStyle
    @Composable @ReadOnlyComposable get() = ThoughtStyle(
        color = ThoughtTypeColor[this] ?: SignalAmber,
        bg = ThoughtTypeBg[this] ?: SignalAmber.copy(alpha = AnimationTokens.Alpha.faint),
        icon = ThoughtTypeIcon[this] ?: Icons.Default.Lightbulb,
        label = ThoughtTypeLabel[this] ?: "想法"
    )

internal data class FilterOption(val key: String, val label: String, val icon: ImageVector)

internal val filterOptions = listOf(
    FilterOption("all", "全部", Icons.Default.FilterList),
    FilterOption("friend", "伙伴", Icons.Default.Group),
    FilterOption("plan", "计划", Icons.Default.TaskAlt),
    FilterOption("murmur", "碎碎念", Icons.Default.Lightbulb),
    FilterOption("todo", "待办", Icons.Default.CheckBox)
)
