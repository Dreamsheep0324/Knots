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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.feature.graph.graph.SelectedEventInfo
import com.tang.prm.ui.theme.getEventTypeStyle
import com.tang.prm.ui.theme.resolveEventAccentColor
import com.tang.prm.ui.theme.resolveEventIcon
import com.tang.prm.ui.theme.TextGray

/**
 * 选中事件节点的底部信息卡。
 *
 * 视觉设计（清透卡片风格）：
 * - 白色卡片背景 + 左侧类型色条 + 轻阴影
 * - 头部：图标徽章 + 标题 + 类型胶囊/时间/地点 + 关闭按钮
 * - 内容区：描述摘要 + 照片缩略图 + 参与者标签
 * - 底部：描边查看详情按钮
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventInfoCard(
    info: SelectedEventInfo?,
    eventTypes: List<CustomType> = emptyList(),
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
        val eventStyle = getEventTypeStyle(info.type)
        val typeColor = resolveEventAccentColor(info.type, info.customTypeName, eventTypes)
        val lightColor = eventStyle.lightColor
        val eventIcon = resolveEventIcon(info.type, info.customTypeName, info.title, eventTypes)

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
                    // 头部行
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 事件类型图标徽章（轻色背景 + 彩色图标）
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(lightColor.copy(alpha = 0.4f))
                                .border(1.dp, typeColor.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = eventIcon,
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(23.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = info.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TypeCapsule(
                                    text = info.type.displayName,
                                    color = typeColor
                                )
                                MetaInfo(
                                    icon = Icons.Default.CalendarMonth,
                                    text = DateUtils.formatMonthDayWeekday(info.time)
                                )
                            }
                            if (!info.location.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(3.dp))
                                MetaInfo(
                                    icon = Icons.Default.LocationOn,
                                    text = info.location
                                )
                            }
                        }

                        // 关闭按钮
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
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // 描述摘要
                    if (!info.description.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = info.description,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    // 照片缩略图行
                    if (info.photos.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            info.photos.take(4).forEach { uri ->
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFF1F5F9))
                                )
                            }
                            if (info.photos.size > 4) {
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${info.photos.size - 4}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextGray
                                    )
                                }
                            }
                        }
                    }

                    // 参与者标签
                    if (info.participants.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = TextGray.copy(alpha = 0.7f),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                info.participants.forEach { name ->
                                    ParticipantChip(name = name)
                                }
                            }
                        }
                    }

                    // 底部：查看详情按钮（描边样式）
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White)
                            .border(1.dp, typeColor.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .clickable { onNavigateDetail(info.eventId) }
                            .padding(vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "查看事件详情",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = typeColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeCapsule(
    text: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 7.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun MetaInfo(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextGray.copy(alpha = 0.7f),
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = TextGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ParticipantChip(name: String) {
    Text(
        text = name,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFF1F5F9))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
