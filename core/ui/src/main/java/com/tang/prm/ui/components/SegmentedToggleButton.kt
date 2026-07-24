package com.tang.prm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tang.prm.ui.theme.Dimens

/**
 * 分段切换按钮（用于视图模式切换等场景）。
 *
 * 统一了 EventsScreen / ContactsScreen / ChatScreen 中重复的
 * 36dp Box + 8dp RoundedCornerShape + primary/surfaceVariant 配色模式。
 *
 * @param options 选项列表，每项含图标、可选文字标签、唯一键
 * @param selectedKey 当前选中的键
 * @param onSelectionChange 选中项变更回调
 * @param modifier 修饰符
 * @param showLabel 是否显示文字标签（默认仅图标）
 */
@Composable
fun <T> SegmentedToggleButton(
    options: List<SegmentedOption<T>>,
    selectedKey: T,
    onSelectionChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingXs)
    ) {
        options.forEach { option ->
            val selected = option.key == selectedKey
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onSelectionChange(option.key) },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(Dimens.iconLarge)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(Dimens.cornerSmall)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (showLabel && option.label != null) {
                        Text(
                            text = option.label,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    } else {
                        Icon(
                            option.icon,
                            contentDescription = option.label,
                            tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/** 分段切换选项 */
data class SegmentedOption<T>(
    val key: T,
    val icon: ImageVector,
    val label: String? = null
)
