package com.tang.prm.feature.reflect.footprints

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.theme.CardBorder
import com.tang.prm.ui.theme.GridLine
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.ui.theme.TextGray
import com.tang.prm.domain.model.FootprintItem
import com.tang.prm.domain.util.DateUtils

/**
 * P-1 修复：timelineItems 从"单个 item 包装整个 Column"改为按月份分组懒加载。
 *
 * - 月份标题、足迹卡片、月份间距均为独立 item，LazyColumn 可按需回收
 * - 中线由每个 item 的 drawBehind 分段绘制，视觉上连续（items 紧邻排列）
 * - 用 itemsIndexed 的 index 计算 isLeft，O(1) 替代原 forEachIndexed + indexOf
 *
 * N-1 修复：TimelineCard 接收 displayName 参数，透传 resolveEventTypeStyle 的 style.displayName，
 * 移除内部 EventType.entries.find 重复查找（与 Q-8/ListCard 同类问题）。
 */
internal fun LazyListScope.timelineItems(
    footprints: List<FootprintItem>,
    eventTypes: List<com.tang.prm.domain.model.CustomType>,
    onFootprintClick: (FootprintItem) -> Unit = {}
) {
    val grouped = footprints.groupBy { DateUtils.formatYearMonthChinese(it.date) }

    grouped.entries.forEachIndexed { monthIndex, (monthLabel, monthFootprints) ->
        item(key = "month_header_$monthLabel") {
            MonthHeader(monthLabel = monthLabel)
        }

        itemsIndexed(
            items = monthFootprints,
            key = { _, fp -> "footprint_${fp.id}" }
        ) { index, footprint ->
            TimelineItem(
                footprint = footprint,
                isLeft = index % 2 == 0,
                eventTypes = eventTypes,
                onClick = { onFootprintClick(footprint) }
            )
        }

        if (monthIndex < grouped.size - 1) {
            item(key = "month_spacer_$monthIndex") {
                TimelineSpacer(height = 16.dp)
            }
        }
    }
}

@Composable
private fun MonthHeader(monthLabel: String) {
    val gridLineColor = GridLine
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val centerX = size.width / 2
                drawLine(
                    color = gridLineColor,
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, size.height),
                    strokeWidth = 4.dp.toPx()
                )
            }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SignalGreen
        ) {
            Text(
                text = monthLabel,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                color = Color.White,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TimelineSpacer(height: Dp) {
    val gridLineColor = GridLine
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .drawBehind {
                val centerX = size.width / 2
                drawLine(
                    color = gridLineColor,
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, size.height),
                    strokeWidth = 4.dp.toPx()
                )
            }
    )
}

@Composable
private fun TimelineItem(
    footprint: FootprintItem,
    isLeft: Boolean,
    eventTypes: List<com.tang.prm.domain.model.CustomType>,
    onClick: () -> Unit = {}
) {
    val style = resolveEventTypeStyle(footprint.eventType, eventTypes)
    val accentColor = style.accentColor
    val lightColor = style.lightColor
    val icon = style.icon
    val gridLineColor = GridLine

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val centerX = size.width / 2
                drawLine(
                    color = gridLineColor,
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, size.height),
                    strokeWidth = 4.dp.toPx()
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(0.42f),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (isLeft) {
                    TimelineCard(footprint, accentColor, lightColor, icon, style.displayName, onClick)
                }
            }

            Box(
                modifier = Modifier.weight(0.08f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(accentColor, CircleShape)
                )
            }

            Box(
                modifier = Modifier.weight(0.42f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (!isLeft) {
                    TimelineCard(footprint, accentColor, lightColor, icon, style.displayName, onClick)
                }
            }
        }
    }
}

@Composable
private fun TimelineCard(
    footprint: FootprintItem,
    accentColor: Color,
    lightColor: Color,
    icon: ImageVector,
    displayName: String,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorder),
        shadowElevation = 3.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(lightColor, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = footprint.eventTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = DateUtils.formatMonthDayChineseFull(footprint.date),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TextGray.copy(alpha = AnimationTokens.Alpha.half)
                        )
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            tint = SignalSky,
                            modifier = Modifier.size(9.dp)
                        )
                        Text(
                            text = footprint.location,
                            fontSize = 10.sp,
                            color = SignalSky,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = lightColor
                ) {
                    Text(
                        text = displayName,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
                footprint.weather?.takeIf { it.isNotBlank() }?.let { weather ->
                    // C-5 修复：缓存天气颜色，避免同 Composable 内重复解析
                    val weatherColor = getWeatherColorForFootprint(weather)
                    FootprintMetaTag(
                        icon = getWeatherIconForFootprint(weather),
                        text = weather,
                        bgColor = weatherColor.copy(alpha = AnimationTokens.Alpha.faint),
                        textColor = weatherColor
                    )
                }
            }
        }
    }
}
