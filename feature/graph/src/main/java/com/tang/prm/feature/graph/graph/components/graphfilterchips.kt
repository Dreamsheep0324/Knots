@file:OptIn(ExperimentalLayoutApi::class)

package com.tang.prm.feature.graph.graph.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.CustomType
import com.tang.prm.feature.graph.graph.RelationFilter
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.TextGray
import com.tang.prm.ui.theme.toComposeColor

/**
 * 关系类型筛选条。
 *
 * - 第一项：全部（高亮 = RelationFilter.All）
 * - 后续：每个关系类型一个 chip，点击切换 ByType 筛选
 * - 末尾：事件节点显示/隐藏开关 chip（图标 + 文案，激活时高亮）
 *
 * 调用方应仅传入"我对人物的标签"（RELATIONSHIP 类别），不传入人物之间的关系类型。
 *
 * @param relationTypes 关系类型列表（来自 custom_types.RELATIONSHIP）
 * @param activeFilter 当前筛选
 * @param showEventNodes 是否显示事件节点
 * @param onSelectFilter 选择筛选
 * @param onToggleEventNodes 事件节点显示/隐藏切换
 */
@Composable
fun GraphFilterChips(
    relationTypes: List<CustomType>,
    activeFilter: RelationFilter,
    showEventNodes: Boolean,
    onSelectFilter: (RelationFilter) -> Unit,
    onToggleEventNodes: () -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterChip(
            label = "全部",
            selected = activeFilter is RelationFilter.All,
            color = SignalPurple,
            onClick = { onSelectFilter(RelationFilter.All) }
        )
        relationTypes.forEach { type ->
            val selected = (activeFilter as? RelationFilter.ByType)?.typeId == type.id
            FilterChip(
                label = type.name,
                selected = selected,
                color = type.color.toComposeColor(SignalPurple),
                onClick = { onSelectFilter(RelationFilter.ByType(type.id)) }
            )
        }
        // 事件节点显示/隐藏开关
        EventToggleChip(
            showEventNodes = showEventNodes,
            onClick = onToggleEventNodes
        )
    }
}

/**
 * 事件节点显示/隐藏开关 chip。
 *
 * 样式与普通筛选 chip 一致，但左侧用事件图标代替色点。
 * 激活时高亮（紫色），关闭时灰色。
 */
@Composable
private fun EventToggleChip(
    showEventNodes: Boolean,
    onClick: () -> Unit
) {
    val color = if (showEventNodes) SignalPurple else TextGray
    val bg = if (showEventNodes) color.copy(alpha = 0.18f) else Color.Transparent
    val border = if (showEventNodes) color else MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Event,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "事件",
            fontSize = 13.sp,
            fontWeight = if (showEventNodes) FontWeight.SemiBold else FontWeight.Normal,
            color = if (showEventNodes) color else TextGray
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    val bg = if (selected) color.copy(alpha = 0.18f) else Color.Transparent
    val border = if (selected) color else MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) color else TextGray
        )
    }
}
