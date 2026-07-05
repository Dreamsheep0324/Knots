package com.tang.prm.feature.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.IntimacyTier
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.photo.PhotoPickerConfig
import com.tang.prm.ui.components.photo.rememberPhotoPickerLauncher
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalCoral
import com.tang.prm.ui.theme.SignalGold
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky
import java.io.File
import java.util.Calendar
import kotlin.math.PI
import kotlin.math.sin

// ── 日记风格平板首页 ──────────────────────────────────────────────

@Composable
internal fun JournalTabletHome(
    uiState: HomeUiState,
    channels: List<ChannelDef>,
    signalStrengths: Map<Any, Int>,
    onChannelClick: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. 日期横幅
        JournalDateBanner(
            greeting = uiState.greeting,
            pendingTodoCount = uiState.pendingTodos.size,
            upcomingAnniversaryCount = uiState.upcomingAnniversaries.size
        )

        // 2. 中间区域：时间线 + 右栏（关系概览 + 装饰卡片）
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            JournalTimelineCard(
                recentEvents = uiState.recentEvents,
                upcomingAnniversaries = uiState.upcomingAnniversaries,
                modifier = Modifier.weight(0.5f).fillMaxHeight()
            )
            Column(
                modifier = Modifier.weight(0.5f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                JournalRelationshipOverview(
                    tierDistribution = uiState.tierDistribution,
                    totalContactCount = uiState.contactCount
                )
                JournalDecorCard(modifier = Modifier.fillMaxWidth().height(120.dp))
            }
        }

        // 3. 底部区域：快捷入口 + 数据一览 + 装饰组件（三列对齐，填满剩余高度）
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            JournalQuickAccess(
                channels = channels,
                onChannelClick = onChannelClick,
                modifier = Modifier.weight(0.33f).fillMaxHeight()
            )
            JournalStatsOverview(
                contactCount = uiState.contactCount,
                eventCount = uiState.eventCount,
                giftCount = uiState.giftCount,
                anniversaryCount = uiState.anniversaryCount,
                conversationCount = uiState.conversationCount,
                modifier = Modifier.weight(0.34f).fillMaxHeight()
            )
            JournalPhotoCard(
                modifier = Modifier.weight(0.33f).fillMaxHeight()
            )
        }
    }
}

// ── 日期横幅 ──────────────────────────────────────────────────────

@Composable
private fun JournalDateBanner(
    greeting: String,
    pendingTodoCount: Int,
    upcomingAnniversaryCount: Int,
    modifier: Modifier = Modifier
) {
    val today = Calendar.getInstance()
    val day = today.get(Calendar.DAY_OF_MONTH)
    val monthNames = listOf("一月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "十一月", "十二月")
    val weekNames = listOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
    val monthName = monthNames[today.get(Calendar.MONTH)]
    val weekName = weekNames[today.get(Calendar.DAY_OF_WEEK) - 1]

    val accentGradient = Brush.verticalGradient(colors = listOf(SignalGreen, SignalSky))

    AppCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = day.toString(),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraLight,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 64.sp
                )
                Column {
                    Text(
                        text = monthName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = weekName,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentGradient)
                    .padding(start = 20.dp)
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = greeting.ifBlank { "你好" },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "「但今天只是今天，未来也只是今天的未来」",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(SignalGreen)
                    )
                    Text(
                        text = "${pendingTodoCount}项待办 · ${upcomingAnniversaryCount}个纪念临近",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ── 今日时光时间线 ─────────────────────────────────────────────────

private data class TimelineEntry(
    val timeLabel: String,
    val title: String,
    val subtitle: String,
    val dotColor: Color,
    val badgeText: String,
    val badgeColor: Color
)

@Composable
private fun JournalTimelineCard(
    recentEvents: List<Event>,
    upcomingAnniversaries: List<Anniversary>,
    modifier: Modifier = Modifier
) {
    val entries = remember(recentEvents, upcomingAnniversaries) {
        buildList {
            recentEvents.take(4).forEachIndexed { _, event ->
                val cal = Calendar.getInstance().apply { timeInMillis = event.time }
                val isToday = run {
                    val now = Calendar.getInstance()
                    cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                            cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
                }
                val timeLabel = if (isToday) {
                    String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                } else {
                    "${((System.currentTimeMillis() - event.time) / 86400000).toInt()}天前"
                }
                add(TimelineEntry(
                    timeLabel = timeLabel,
                    title = event.title.ifBlank { event.type.displayName },
                    subtitle = if (isToday) "今天" else timeLabel,
                    dotColor = SignalSky,
                    badgeText = "事件",
                    badgeColor = SignalGreen
                ))
            }
            upcomingAnniversaries.take(2).forEach { ann ->
                add(TimelineEntry(
                    timeLabel = "纪念",
                    title = ann.name,
                    subtitle = ann.type.displayName,
                    dotColor = SignalPurple,
                    badgeText = "纪念",
                    badgeColor = SignalPurple
                ))
            }
        }
    }

    AppCard(modifier = modifier) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(8.dp).clip(CircleShape).background(SignalGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "今日时光",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            entries.forEachIndexed { index, entry ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = entry.timeLabel,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.width(52.dp)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(24.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(10.dp).clip(CircleShape).background(entry.dotColor)
                        )
                        if (index < entries.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(1.5.dp)
                                    .height(32.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = entry.subtitle,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = entry.badgeColor.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = entry.badgeText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = entry.badgeColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                if (index < entries.size - 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

// ── 关系概览 ──────────────────────────────────────────────────────

private data class TierInfo(
    val label: String,
    val color: Color,
    val count: Int
)

@Composable
private fun JournalRelationshipOverview(
    tierDistribution: Map<IntimacyTier, Int>,
    totalContactCount: Int,
    modifier: Modifier = Modifier
) {
    val tierData = remember(tierDistribution) {
        listOf(
            TierInfo("至亲", SignalGold, tierDistribution[IntimacyTier.FAMILY] ?: 0),
            TierInfo("密友", SignalCoral, tierDistribution[IntimacyTier.CLOSE] ?: 0),
            TierInfo("朋友", SignalPurple, tierDistribution[IntimacyTier.FRIEND] ?: 0),
            TierInfo("泛交", SignalSky, tierDistribution[IntimacyTier.ACQUAINTANCE] ?: 0),
            TierInfo("初识", Color(0xFF94A3B8), tierDistribution[IntimacyTier.NEW] ?: 0)
        )
    }

    val maxCount = remember(tierData) { tierData.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1 }

    AppCard(modifier = modifier) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SignalPurple))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "关系概览",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            tierData.forEach { tier ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(8.dp).clip(CircleShape).background(tier.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tier.label,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(36.dp)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        val progress = if (maxCount > 0) tier.count.toFloat() / maxCount else 0f
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            tier.color.copy(alpha = 0.8f),
                                            tier.color.copy(alpha = 0.4f)
                                        )
                                    )
                                )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${tier.count}人",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${totalContactCount}位伙伴",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── 快捷入口 — 精致卡片式设计 ──────────────────────────────────────

@Composable
private fun JournalQuickAccess(
    channels: List<ChannelDef>,
    onChannelClick: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp)
        ) {
            // 标题
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SignalAmber))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "快捷入口",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2列4行网格，行均分高度
            channels.chunked(2).forEachIndexed { rowIndex, row ->
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    row.forEach { channel ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(channel.color.copy(alpha = 0.06f))
                                .border(
                                    width = 1.dp,
                                    color = channel.color.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onChannelClick(channel.route) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // 图标圆
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(channel.color.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (channel.textIcon != null) {
                                    Text(
                                        text = channel.textIcon,
                                        fontSize = 16.sp,
                                        color = channel.color
                                    )
                                } else if (channel.icon != null) {
                                    Icon(
                                        channel.icon,
                                        contentDescription = channel.name,
                                        tint = channel.color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = channel.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (row.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (rowIndex < 3) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

// ── 数据一览 — 简洁列表式 ──────────────────────────────────────────

private data class StatItem(
    val label: String,
    val count: Int,
    val color: Color,
    val icon: ImageVector
)

@Composable
private fun JournalStatsOverview(
    contactCount: Int,
    eventCount: Int,
    giftCount: Int,
    anniversaryCount: Int,
    conversationCount: Int,
    modifier: Modifier = Modifier
) {
    val stats = remember(contactCount, eventCount, giftCount, anniversaryCount, conversationCount) {
        listOf(
            StatItem("人物", contactCount, SignalSky, Icons.Default.People),
            StatItem("事件", eventCount, SignalGreen, Icons.Default.Event),
            StatItem("礼物", giftCount, SignalCoral, Icons.Default.CardGiftcard),
            StatItem("纪念日", anniversaryCount, SignalPurple, Icons.Default.Favorite),
            StatItem("对话", conversationCount, SignalAmber, Icons.Default.People)
        )
    }

    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp)
        ) {
            // 标题
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SignalSky))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "数据一览",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 每行均分高度
            stats.forEachIndexed { index, stat ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 左侧彩色竖线
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(stat.color)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // 图标
                    Icon(
                        stat.icon,
                        contentDescription = stat.label,
                        tint = stat.color,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // 标签
                    Text(
                        text = stat.label,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(44.dp)
                    )

                    // 数字
                    Text(
                        text = stat.count.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // 渐变进度条
                    val maxRef = listOf(50, 50, 30, 20, 50)
                    val progress = (stat.count.toFloat() / maxRef[index]).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            stat.color,
                                            stat.color.copy(alpha = 0.3f)
                                        )
                                    )
                                )
                        )
                    }
                }

                if (index < stats.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 15.dp)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.08f))
                    )
                }
            }
        }
    }
}

// ── 装饰卡片：极光 ──────────────────────────────────────────────

@Composable
private fun JournalDecorCard(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "auroraPhase1"
    )
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "auroraPhase2"
    )
    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "auroraPhase3"
    )

    AppCard(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // 底部微渐变
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                SignalSky.copy(alpha = 0.03f)
                            )
                        )
                    )
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 三条极光色带
                val bands = listOf(
                    Triple(0.30f, SignalGreen, phase1),
                    Triple(0.45f, SignalSky, phase2),
                    Triple(0.58f, SignalPurple, phase3),
                )

                bands.forEach { (yRatio, color, phase) ->
                    val baseY = h * yRatio
                    val bandWidth = h * 0.22f
                    val path = Path()

                    // 上边
                    path.moveTo(0f, baseY - bandWidth / 2)
                    var x = 0f
                    while (x <= w) {
                        val nx = x / w
                        val wave = sin(nx * PI.toFloat() * 1.5f + phase * 2f * PI.toFloat()) * h * 0.06f +
                                sin(nx * PI.toFloat() * 3f + phase * 4f * PI.toFloat()) * h * 0.02f
                        path.lineTo(x, baseY - bandWidth / 2 + wave)
                        x += 3f
                    }
                    // 下边 (反向)
                    x = w
                    while (x >= 0f) {
                        val nx = x / w
                        val wave = sin(nx * PI.toFloat() * 1.5f + phase * 2f * PI.toFloat()) * h * 0.06f +
                                sin(nx * PI.toFloat() * 3f + phase * 4f * PI.toFloat()) * h * 0.02f
                        path.lineTo(x, baseY + bandWidth / 2 + wave)
                        x -= 3f
                    }
                    path.close()

                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                color.copy(alpha = 0f),
                                color.copy(alpha = 0.05f),
                                color.copy(alpha = 0.05f),
                                color.copy(alpha = 0f)
                            ),
                            startY = baseY - bandWidth,
                            endY = baseY + bandWidth
                        )
                    )
                }
            }
        }
    }
}

// ── 照片展示卡片 — 拍立得风格 ──────────────────────────────────────

private const val PREFS_NAME = "home_decor"
private const val KEY_PHOTO_PATH = "photo_path"

@Composable
private fun JournalPhotoCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, 0) }
    var photoPath by remember { mutableStateOf(prefs.getString(KEY_PHOTO_PATH, null)) }

    val launcher = rememberPhotoPickerLauncher(
        config = PhotoPickerConfig(maxCount = 1, prefix = "home_photo")
    ) { result ->
        val path = result.localPaths.firstOrNull()
        if (path != null) {
            prefs.edit().putString(KEY_PHOTO_PATH, path).apply()
            photoPath = path
        }
    }

    AppCard(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (photoPath != null) {
                // 拍立得风格 — 照片居中，白色边框，底部留白更宽
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 照片区域
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF5F5F5))
                            .clickable { launcher.launch() }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(photoPath!!))
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // 点击更换提示
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = "更换照片",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    // 底部留白（拍立得特征）
                    Spacer(modifier = Modifier.height(20.dp))
                }
            } else {
                // 无照片 — 拍立得占位，点击上传
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 空白照片区域
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF8F8F8))
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE8E8E8),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { launcher.launch() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = "添加照片",
                                tint = Color(0xFFBBBBBB),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "点击添加照片",
                                fontSize = 12.sp,
                                color = Color(0xFFBBBBBB)
                            )
                        }
                    }
                    // 底部留白（拍立得特征）
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
