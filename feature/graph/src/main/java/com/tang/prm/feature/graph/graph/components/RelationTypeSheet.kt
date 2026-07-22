@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.graph.graph.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.CustomType
import com.tang.prm.feature.graph.graph.RelationSheetState
import com.tang.prm.ui.theme.OnSurface
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.TextGray
import com.tang.prm.ui.theme.toComposeColor

/**
 * 关系类型选择底部对话框。
 *
 * 编辑模式下用户拖拽连线后弹出，让用户选择关系类型。
 *
 * @param state 当前 sheet 状态（Hidden 时不显示）
 * @param relationTypes 可选关系类型列表
 * @param onSelectType 选择类型回调
 * @param onDismiss 关闭回调
 */
@Composable
fun RelationTypeSheet(
    state: RelationSheetState,
    relationTypes: List<CustomType>,
    onSelectType: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    if (state !is RelationSheetState.SelectType) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "选择关系类型",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${state.sourceName} → ${state.targetName}",
                    fontSize = 13.sp,
                    color = TextGray
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(relationTypes, key = { it.id }) { type ->
                    RelationTypeOption(
                        type = type,
                        selected = type.id == state.selectedTypeId,
                        onClick = { onSelectType(type.id) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = TextGray)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun RelationTypeOption(
    type: CustomType,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = type.color.toComposeColor(SignalPurple)
    val borderColor = if (selected) color else MaterialTheme.colorScheme.outlineVariant
    val bgColor = if (selected) color.copy(alpha = 0.10f) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = type.icon ?: type.name.take(1),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = type.name,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = OnSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
