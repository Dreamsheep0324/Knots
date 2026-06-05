package com.tang.prm.feature.reflect.thoughts

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector
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

internal data class FilterOption(val key: String, val label: String, val icon: ImageVector)

internal val filterOptions = listOf(
    FilterOption("all", "全部", Icons.Default.FilterList),
    FilterOption("friend", "伙伴", Icons.Default.Group),
    FilterOption("plan", "计划", Icons.Default.TaskAlt),
    FilterOption("murmur", "碎碎念", Icons.Default.Lightbulb),
    FilterOption("todo", "待办", Icons.Default.CheckBox)
)
