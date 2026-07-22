@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.graph.graph.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tang.prm.feature.graph.graph.GraphViewMode
import com.tang.prm.ui.components.SegmentedOption
import com.tang.prm.ui.components.SegmentedToggleButton
import com.tang.prm.ui.theme.OnSurface
import com.tang.prm.ui.theme.SignalPurple

/**
 * 图谱顶部栏。
 *
 * - 左侧：返回按钮
 * - 中间：标题"图谱"（样式与 EventsScreen / ContactsScreen 一致）
 * - 右侧：视图切换（力导向 / 星系）+ 重置布局 + 编辑模式切换
 *
 * 视图切换使用 [SegmentedToggleButton]，样式与 EventsScreen / ContactsScreen 完全一致。
 *
 * @param onBack 返回回调
 * @param isEditMode 当前是否编辑模式（影响编辑按钮色调）
 * @param viewMode 当前视图模式
 * @param onViewModeChange 视图模式切换回调
 * @param onResetLayout 重置布局回调
 * @param onToggleEditMode 切换编辑模式回调
 */
@Composable
fun GraphAppBar(
    onBack: () -> Unit,
    isEditMode: Boolean,
    viewMode: GraphViewMode,
    onViewModeChange: (GraphViewMode) -> Unit,
    onResetLayout: () -> Unit,
    onToggleEditMode: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                "图谱",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = OnSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        actions = {
            // 视图切换：力导向 / 星系（样式与 EventsScreen / ContactsScreen 一致）
            SegmentedToggleButton(
                options = listOf(
                    SegmentedOption(GraphViewMode.FORCE, Icons.Default.BubbleChart, "力导向"),
                    SegmentedOption(GraphViewMode.GALAXY, Icons.Default.AutoAwesome, "星系")
                ),
                selectedKey = viewMode,
                onSelectionChange = onViewModeChange
            )
            IconButton(onClick = onResetLayout) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "重置布局",
                    tint = if (isEditMode) SignalPurple else OnSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(onClick = onToggleEditMode) {
                val tint = if (isEditMode) SignalPurple else OnSurface
                val bg = if (isEditMode) SignalPurple.copy(alpha = 0.12f) else Color.Transparent
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(bg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isEditMode) Icons.Default.Tune else Icons.Default.Edit,
                        contentDescription = if (isEditMode) "退出编辑" else "编辑",
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}
