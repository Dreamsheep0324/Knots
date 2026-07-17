@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.DialogDefaults

/**
 * 表单区块卡片：白底 + 16dp 圆角 + outline 边框 + 阴影，内含图标+标题+内容。
 * 适用于事件、菜谱等新增/编辑表单。
 */
@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(14.dp))
            content()
        }
    }
}

/**
 * 带图标的渐变分割线区块标题：圆形图标背景 + 标题 + 渐变线 + 可选操作按钮。
 * 适用于列表/详情页的区块分隔。
 */
@Composable
fun IconGradientSectionHeader(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    action: String?,
    onActionClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(12.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(iconColor.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )
        if (action != null && onActionClick != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = iconColor.copy(alpha = AnimationTokens.Alpha.faint)
            ) {
                Text(
                    action,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = iconColor,
                    modifier = Modifier
                        .clickable { onActionClick() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun AppDatePicker(
    show: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit,
    initialDate: Long? = null,
    confirmText: String = "确定",
    dismissText: String = "取消",
    confirmColor: Color = MaterialTheme.colorScheme.primary,
    dismissColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    if (show) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate
        )
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    onDismiss()
                }) { Text(confirmText, color = confirmColor) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(dismissText, color = dismissColor) }
            },
            colors = DatePickerDefaults.colors(
                containerColor = DialogDefaults.containerColor
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = DialogDefaults.containerColor,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    headlineContentColor = MaterialTheme.colorScheme.onSurface,
                    weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    currentYearContentColor = MaterialTheme.colorScheme.primary,
                    selectedYearContentColor = Color.White,
                    selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                    dayContentColor = MaterialTheme.colorScheme.onSurface,
                    selectedDayContentColor = Color.White,
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    todayContentColor = MaterialTheme.colorScheme.primary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun FormScreenScaffold(
    title: String,
    onSaveClick: () -> Unit,
    saveEnabled: Boolean = true,
    onBackClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    TextButton(onClick = onSaveClick, enabled = saveEnabled) {
                        Text("保存", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = containerColor,
        content = content
    )
}

@Composable
fun DetailSection(
    title: String,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.width(3.dp).height(18.dp).background(accentColor, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 0.5.dp, color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun DetailInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    maxLines: Int = 1
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(modifier = Modifier.size(40.dp).background(iconColor.copy(alpha = AnimationTokens.Alpha.faint), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = maxLines, overflow = TextOverflow.Ellipsis, color = valueColor)
        }
    }
}
