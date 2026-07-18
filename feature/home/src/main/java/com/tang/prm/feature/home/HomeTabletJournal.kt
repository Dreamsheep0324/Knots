package com.tang.prm.feature.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import com.tang.prm.ui.animation.core.rememberPausableInfiniteFloatLoop
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
import androidx.compose.material.icons.filled.Chat
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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.domain.usecase.HomeStats
import com.tang.prm.ui.animation.primitives.staggeredAppear
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.SignalProgress
import com.tang.prm.ui.components.SignalProgressStyle
import com.tang.prm.ui.components.photo.PhotoPickerConfig
import com.tang.prm.ui.components.photo.rememberPhotoPickerLauncher
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalCoral
import com.tang.prm.ui.theme.SignalGold
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.ui.theme.LocalIntimacyColors
import java.io.File
import java.util.Calendar
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

// ── 日记风格平板首页 ──────────────────────────────────────────────

@Composable
internal fun JournalTabletHome(
    uiState: HomeUiState,
    channels: List<ChannelDef>,
    onChannelClick: (Any) -> Unit,
    onDecorPhotoPathChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    // 每次进入界面生成新的触发键，让 staggeredAppear 重新播放
    val appearKey = remember { Any() }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. 日期横幅
        JournalDateBanner(
            greeting = uiState.data.greeting,
            pendingTodoCount = uiState.data.pendingTodos.size,
            upcomingAnniversaryCount = uiState.data.upcomingAnniversaries.size,
            modifier = Modifier.staggeredAppear(index = 0, triggerKey = appearKey)
        )

        // 2. 中间区域：时间线 + 右栏（关系概览 + 装饰卡片）
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).staggeredAppear(index = 1, triggerKey = appearKey),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            JournalTimelineCard(
                recentEvents = uiState.data.recentEvents,
                upcomingAnniversaries = uiState.data.upcomingAnniversaries,
                modifier = Modifier.weight(0.5f).fillMaxHeight()
            )
            Column(
                modifier = Modifier.weight(0.5f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                JournalRelationshipOverview(
                    tierDistribution = uiState.data.stats.tierDistribution,
                    totalContactCount = uiState.data.stats.contactCount
                )
                JournalDecorCard(modifier = Modifier.fillMaxWidth().height(120.dp))
            }
        }

        // 3. 底部区域：快捷入口 + 数据一览 + 装饰组件（三列对齐，填满剩余高度）
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f).staggeredAppear(index = 2, triggerKey = appearKey),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            JournalQuickAccess(
                channels = channels,
                onChannelClick = onChannelClick,
                modifier = Modifier.weight(0.33f).fillMaxHeight()
            )
            JournalStatsOverview(
                stats = uiState.data.stats,
                modifier = Modifier.weight(0.34f).fillMaxHeight()
            )
            JournalPhotoCard(
                photoPath = uiState.data.decorPhotoPath,
                onPhotoPathChange = onDecorPhotoPathChange,
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
    // Q-5/C-6 修复：用 DateUtils.formatMonthName/formatWeekdayShortName 替代硬编码中文数组，
    // 消除与 DateUtils 的重复，且天然支持 i18n
    val monthName = remember(today) { DateUtils.formatMonthName(today.timeInMillis) }
    val weekName = remember(today) { DateUtils.formatWeekdayShortName(today.timeInMillis) }

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
                // N-9 修复：删除 .padding(start = 20.dp)——在 3dp 宽的 Box 上加 20dp start padding 是死代码，
                // 内容区被压缩到 0，无视觉效果；下方 Spacer(20.dp) 已提供右侧间距
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
                    // Q-6/C-1 修复：品牌文案统一引用 HOME_TAGLINE 常量
                    text = "「$HOME_TAGLINE」",
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
                    // B-6 修复：显式传 Locale.US，避免 ar/tr 等 Locale 下数字格式异常
                    String.format(Locale.US, "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                } else {
                    // Q-8 修复：用 DateUtils.formatRelativeTime 替代魔法数字 86400000 与硬编码"X天前"，
                    // 自动处理未来时间、跨小时、跨天等边界
                    DateUtils.formatRelativeTime(event.time)
                }
                add(TimelineEntry(
                    timeLabel = timeLabel,
                    title = event.title.ifBlank { event.type.displayName },
                    // N-6 修复：非今日事件 subtitle 改为 event.type.displayName，避免与 timeLabel 重复显示"3天前"
                    subtitle = if (isToday) "今天" else event.type.displayName,
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
    // B-1 修复：颜色统一走 LocalIntimacyColors.current.forTier，与人物卡片/终端卡等所有模块保持一致
    // 顺序从高到低：FAMILY→CLOSE→FRIEND→ACQUAINTANCE→NEW（enum 声明顺序 reversed）
    val intimacyColors = LocalIntimacyColors.current
    val tierData = remember(tierDistribution, intimacyColors) {
        IntimacyTier.values().reversed().map { tier ->
            TierInfo(tier.label, intimacyColors.forTier(tier), tierDistribution[tier] ?: 0)
        }
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
                            // C-5 修复：频道图标圆委托 ChannelIcon 共享组件
                            ChannelIcon(channel = channel, diameter = 34.dp)
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

@Immutable
private data class StatItem(
    val label: String,
    val count: Int,
    val color: Color,
    val icon: ImageVector,
    val max: Int
)

@Composable
private fun JournalStatsOverview(
    stats: HomeStats,
    modifier: Modifier = Modifier
) {
    // C-2 修复：用 HomeStatDef.entries 构造显示列表，label/color/icon/max 全部来自单一来源
    val statItems = remember(stats) {
        HomeStatDef.entries.map { def ->
            StatItem(def.label, def.statProvider(stats), def.color, def.icon, def.max)
        }
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
            statItems.forEachIndexed { index, stat ->
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

                    // C-3 修复：渐变进度条委托 SignalProgress Linear 样式，消除与 HomeChannelList 的重复
                    SignalProgress(
                        value = stat.count,
                        maxValue = stat.max,
                        color = stat.color,
                        modifier = Modifier.weight(1f).height(4.dp),
                        style = SignalProgressStyle.Linear
                    )
                }

                if (index < statItems.size - 1) {
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
    val phase1 by rememberPausableInfiniteFloatLoop(
        targetValue = 1f,
        durationMillis = 8000,
        easing = LinearEasing,
        repeatMode = RepeatMode.Restart,
        label = "auroraPhase1"
    )
    val phase2 by rememberPausableInfiniteFloatLoop(
        targetValue = 1f,
        durationMillis = 12000,
        easing = LinearEasing,
        repeatMode = RepeatMode.Restart,
        label = "auroraPhase2"
    )
    val phase3 by rememberPausableInfiniteFloatLoop(
        targetValue = 1f,
        durationMillis = 6000,
        easing = LinearEasing,
        repeatMode = RepeatMode.Restart,
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

@Composable
private fun JournalPhotoCard(
    photoPath: String?,
    onPhotoPathChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val launcher = rememberPhotoPickerLauncher(
        config = PhotoPickerConfig(maxCount = 1, prefix = "home_photo")
    ) { result ->
        val path = result.localPaths.firstOrNull()
        if (path != null) {
            onPhotoPathChange(path)
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
            val currentPath = photoPath
            if (currentPath != null) {
                // 拍立得风格 — 照片居中，白色边框，底部留白更宽
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // 照片区域
                    // U-8 修复：clickable 容器加 semantics(mergeDescendants=true) 合并子节点语义，
                    // 读屏点击大块照片区域时作为单一节点播报"更换照片"，避免焦点跳跃
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            // 硬编码颜色修复：用 surfaceVariant 替代 Color(0xFFF5F5F5)，跟随主题
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { launcher.launch() }
                            .semantics(mergeDescendants = true) {
                                contentDescription = "更换照片"
                            }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(currentPath))
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
                                // 硬编码颜色修复：用 onSurface 替代 Color.Black，跟随主题（深色模式自动反色）
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = null,
                                // 硬编码颜色修复：用 surface 替代 Color.White，跟随主题
                                tint = MaterialTheme.colorScheme.surface,
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
                    // U-8 修复：clickable 容器加 semantics 合并语义，读屏作为单一节点播报"添加照片"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            // 硬编码颜色修复：用 surfaceVariant 替代 Color(0xFFF8F8F8)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                width = 1.dp,
                                // 硬编码颜色修复：用 outlineVariant 替代 Color(0xFFE8E8E8)
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { launcher.launch() }
                            .semantics(mergeDescendants = true) {
                                contentDescription = "添加照片"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = null,
                                // 硬编码颜色修复：用 onSurfaceVariant 替代 Color(0xFFBBBBBB)
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "点击添加照片",
                                fontSize = 12.sp,
                                // 硬编码颜色修复：用 onSurfaceVariant 替代 Color(0xFFBBBBBB)
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
