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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.feature.graph.graph.NeighborRelation
import com.tang.prm.feature.graph.graph.SelectedNodeInfo
import com.tang.prm.ui.components.ContactAvatar
import com.tang.prm.ui.theme.Divider
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.TextGray
import com.tang.prm.ui.theme.toComposeColor

/**
 * 选中节点底部信息卡。
 *
 * 显示节点头像 / 名字 / 亲密度档位 / 邻居数 / 前三条关系预览 / 查看详情入口。
 * 通过 [AnimatedVisibility] 做向上滑入/滑出过渡。
 *
 * @param info 选中节点信息，null 时隐藏
 * @param onNavigateDetail 点击"查看详情"回调（传入联系人 id）
 * @param onDismiss 点击关闭按钮回调
 */
@Composable
fun NodeInfoCard(
    info: SelectedNodeInfo?,
    onNavigateDetail: (Long) -> Unit,
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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            // 强制纯白背景，避免 Material3 tonalElevation 与 primary 色混合产生蓝色调
            color = Color.White,
            shadowElevation = 8.dp,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 左侧亲密度色条
                    Box(
                        modifier = Modifier
                            .size(width = 4.dp, height = 44.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(info.tier.colorValue))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    // 头像
                    ContactAvatar(
                        avatar = info.avatarUri,
                        name = info.name,
                        size = 44
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = info.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(info.tier.colorValue))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = info.tierLabel,
                                fontSize = 12.sp,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "·",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${info.neighborCount} 段关系",
                                fontSize = 12.sp,
                                color = TextGray
                            )
                        }
                    }
                    // 关闭
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = TextGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 邻居关系预览
                if (info.topRelations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Divider)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        info.topRelations.forEach { rel ->
                            NeighborRow(rel)
                        }
                        if (info.neighborCount > info.topRelations.size) {
                            Text(
                                text = "还有 ${info.neighborCount - info.topRelations.size} 段关系…",
                                fontSize = 11.sp,
                                color = TextGray,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }
                }

                // 查看详情入口（"我"节点不显示）
                if (!info.isSelf) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SignalPurple.copy(alpha = 0.08f))
                            .border(
                                width = 1.dp,
                                color = SignalPurple.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onNavigateDetail(info.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "查看联系人详情",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SignalPurple,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = SignalPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NeighborRow(rel: NeighborRelation) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 12.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(rel.relationTypeColor.toComposeColor(SignalPurple))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = rel.otherName,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = rel.relationTypeName,
            fontSize = 11.sp,
            color = TextGray
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (!rel.isManual) {
            Text(
                text = "推断",
                fontSize = 10.sp,
                color = TextGray.copy(alpha = 0.7f)
            )
        }
    }
}
