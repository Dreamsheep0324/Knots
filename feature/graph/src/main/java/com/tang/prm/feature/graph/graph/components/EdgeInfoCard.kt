package com.tang.prm.feature.graph.graph.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.feature.graph.graph.SelectedEdgeInfo
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.TextGray
import com.tang.prm.ui.theme.toComposeColor

/**
 * 选中边的信息卡（事件连线 / 人物关系连线）。
 *
 * 视觉设计：
 * - 白色卡片背景 + 左侧类型色条 + 轻阴影
 * - 头部：两端节点头像 + 中间关系类型胶囊
 * - 备注区（如有）以浅灰卡片展示
 * - 底部操作栏：删除/关闭按钮
 */
@Composable
fun EdgeInfoCard(
    info: SelectedEdgeInfo?,
    onDelete: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = info != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        if (info == null) return@AnimatedVisibility
        val typeColor = info.relationTypeColor.toComposeColor(SignalPurple)
        val canDelete = true

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 10.dp,
            tonalElevation = 0.dp
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                // 左侧类型色条
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .background(typeColor.copy(alpha = 0.8f))
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    // 关系链可视化区
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 源节点
                        NodeAvatar(name = info.sourceName, color = typeColor)

                        Spacer(modifier = Modifier.width(8.dp))

                        // 中间关系类型胶囊 + 来源
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            // 关系类型胶囊（色点 + 文字）
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(typeColor.copy(alpha = 0.1f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(typeColor)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = info.relationTypeName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = typeColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            // 箭头图标
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = typeColor.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // 来源标签
                            Text(
                                text = info.sourceLabel,
                                fontSize = 10.sp,
                                color = typeColor,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // 目标节点
                        NodeAvatar(name = info.targetName, color = typeColor)
                    }

                    // 备注（如有）
                    if (!info.label.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "备注",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextGray.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = info.label,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                                    lineHeight = 19.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // 底部操作栏
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (canDelete) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFFEF2F2))
                                    .clickable { onDelete(info.id) }
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "删除关系",
                                    tint = Color(0xFFE53935),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "删除",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE53935)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFFF1F5F9))
                                .clickable { onDismiss() }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭",
                                tint = TextGray,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "关闭",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextGray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NodeAvatar(
    name: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f))
                .border(1.dp, color.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(64.dp)
        )
    }
}
