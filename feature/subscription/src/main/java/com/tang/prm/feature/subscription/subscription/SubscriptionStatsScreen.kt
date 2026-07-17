package com.tang.prm.feature.subscription.subscription

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.domain.model.Subscription
import com.tang.prm.domain.model.SubscriptionCycle
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.FormSectionLabel
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.ui.theme.SubscriptionChartPalette
import java.util.Calendar
import kotlin.math.min

data class SubscriptionStatsUiState(
    val monthlyTotal: Double = 0.0,
    val yearlyTotal: Double = 0.0,
    val monthlyAverage: Double = 0.0,
    val dailyAverage: Double = 0.0,
    val activeCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val byCategory: Map<String, Double> = emptyMap(),
    val byCategorySubscriptions: Map<String, List<Subscription>> = emptyMap(),
    val categoryPercentages: Map<String, Float> = emptyMap(),
    val yearlyProjection: Double = 0.0,
    val activeSubscriptions: List<Subscription> = emptyList(),
    val isLoading: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionStatsScreen(
    onBack: () -> Unit,
    viewModel: SubscriptionStatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("订阅统计", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", modifier = Modifier.size(22.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { YearlyProjectionCard(uiState) }
                item { KeyMetricsGrid(uiState) }
                item { CategoryBreakdownCard(uiState) }
                item { BillingCalendarCard(uiState) }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

// ── 年度预估卡片 ──
@Composable
private fun YearlyProjectionCard(state: SubscriptionStatsUiState) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            FormSectionLabel(icon = Icons.Default.TrendingUp, label = "年度预估", color = SignalGreen)
            Spacer(modifier = Modifier.height(16.dp))
            Text("¥${formatPriceValue(state.yearlyProjection)}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                ProjectionSubItem("月均", "¥${formatPriceValue(state.monthlyAverage)}")
                ProjectionSubItem("日均", "¥${formatPriceValue(state.dailyAverage)}")
            }
        }
    }
}

@Composable
private fun ProjectionSubItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
    }
}

// ── 关键指标 2x2 网格 ──
@Composable
private fun KeyMetricsGrid(state: SubscriptionStatsUiState) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricCard(Modifier.weight(1f), Icons.Default.Payments, "月度支出", "¥${formatPriceValue(state.monthlyTotal)}", MaterialTheme.colorScheme.primary)
        MetricCard(Modifier.weight(1f), Icons.Default.Today, "日均支出", "¥${formatPriceValue(state.dailyAverage)}", SignalSky)
    }
    Spacer(modifier = Modifier.height(4.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MetricCard(Modifier.weight(1f), Icons.Default.NotificationsActive, "7天内到期", "${state.expiringSoonCount}", SignalAmber)
        MetricCard(Modifier.weight(1f), Icons.Default.CalendarMonth, "活跃订阅", "${state.activeCount}", SignalGreen)
    }
}

@Composable
private fun MetricCard(modifier: Modifier = Modifier, icon: ImageVector, title: String, value: String, color: Color) {
    AppCard(modifier = modifier) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.size(32.dp).background(color.copy(alpha = AnimationTokens.Alpha.faint), CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace)
        }
    }
}

// ── 分类占比卡片（圆环图） ──
private val categoryColors = SubscriptionChartPalette

@Composable
private fun CategoryBreakdownCard(state: SubscriptionStatsUiState) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = AnimationTokens.Alpha.faint)), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("分类占比", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (state.byCategory.isEmpty()) {
                Text("暂无数据", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                // 圆环图 + 中心信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // 圆环 Canvas
                        val entries = state.byCategory.entries.toList()
                        val totalAmount = entries.sumOf { it.value }
                        val trackColor = MaterialTheme.colorScheme.surfaceVariant
                        Canvas(modifier = Modifier.size(150.dp)) {
                            val strokeWidth = 20.dp.toPx()
                            val arcRadius = (size.minDimension - strokeWidth) / 2f
                            val arcSize = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2)
                            val arcOffset = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f)
                            var startAngle = -90f

                            // 背景圆环
                            drawArc(
                                color = trackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                topLeft = arcOffset,
                                size = arcSize,
                                style = Stroke(width = strokeWidth)
                            )

                            // 各分类弧段
                            entries.forEachIndexed { index, (_, amount) ->
                                val sweepAngle = if (totalAmount > 0) (amount / totalAmount * 360f).toFloat() else 0f
                                val color = categoryColors[index % categoryColors.size]
                                if (sweepAngle > 0f) {
                                    drawArc(
                                        color = color,
                                        startAngle = startAngle,
                                        sweepAngle = sweepAngle,
                                        useCenter = false,
                                        topLeft = arcOffset,
                                        size = arcSize,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                                    )
                                    startAngle += sweepAngle
                                }
                            }
                        }

                        // 中心文字
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("¥${formatPriceValue(totalAmount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                            Text("年度总计", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 分类图例 + 详情
                state.byCategory.entries.forEachIndexed { index, (category, amount) ->
                    val percentage = state.categoryPercentages[category] ?: 0f
                    val color = categoryColors[index % categoryColors.size]
                    val subs = state.byCategorySubscriptions[category] ?: emptyList()

                    // 分类标题行
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(category, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("${subs.size}项", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${"%.1f".format(percentage)}%", style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("¥${formatPriceValue(amount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
                    }

                    // 该分类下的订阅列表
                    if (subs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        subs.forEach { sub ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, top = 2.dp, bottom = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(color.copy(alpha = 0.5f)))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(sub.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "¥${formatPriceValue(sub.price)}/${sub.cycle.displayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (index < state.byCategory.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

// ── 扣费日历卡片 ──
@Composable
private fun BillingCalendarCard(state: SubscriptionStatsUiState) {
    if (state.activeSubscriptions.isEmpty()) return

    val now = Calendar.getInstance()
    val currentYear = now.get(Calendar.YEAR)
    val currentMonth = now.get(Calendar.MONTH)
    val today = now.get(Calendar.DAY_OF_MONTH)
    val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun

    // 计算每个日期有哪些订阅扣费
    val billingDays = mutableMapOf<Int, MutableList<Pair<String, Color>>>()
    val categoryColorMap = state.byCategory.keys.mapIndexed { index, cat -> cat to categoryColors[index % categoryColors.size] }.toMap()

    state.activeSubscriptions.forEach { sub ->
        val billingCal = Calendar.getInstance().apply { timeInMillis = sub.nextBillingDate }
        if (billingCal.get(Calendar.YEAR) == currentYear && billingCal.get(Calendar.MONTH) == currentMonth) {
            val day = billingCal.get(Calendar.DAY_OF_MONTH)
            val color = categoryColorMap[sub.category ?: "未分类"] ?: MaterialTheme.colorScheme.primary
            billingDays.getOrPut(day) { mutableListOf() }.add(sub.name to color)
        }
        // 对于月度订阅，也标记startDate同一天
        if (sub.cycle == SubscriptionCycle.MONTHLY || sub.cycle == SubscriptionCycle.QUARTERLY) {
            val startCal = Calendar.getInstance().apply { timeInMillis = sub.startDate }
            val startDay = startCal.get(Calendar.DAY_OF_MONTH)
            if (startDay != billingDays.keys.firstOrNull()) {
                val color = categoryColorMap[sub.category ?: "未分类"] ?: MaterialTheme.colorScheme.primary
                if (billingDays[startDay]?.any { it.first == sub.name } != true) {
                    billingDays.getOrPut(startDay) { mutableListOf() }.add(sub.name to color)
                }
            }
        }
    }

    // 本月总扣费
    val monthBillingTotal = billingDays.values.flatten().map { it.first }.toSet().size
    val monthLabel = "${currentMonth + 1}月"

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            // 标题行
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(SignalAmber.copy(alpha = AnimationTokens.Alpha.faint)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = SignalAmber, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("扣费日历", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(8.dp), color = SignalAmber.copy(alpha = AnimationTokens.Alpha.faint)) {
                    Text("${monthLabel}${monthBillingTotal}次扣费", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = SignalAmber, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            // 月份标题
            Text("${currentYear}年${monthLabel}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            // 星期标题行
            val weekLabels = listOf("日", "一", "二", "三", "四", "五", "六")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                weekLabels.forEach { label ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))

            // 日历网格
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - firstDayOfWeek + 1
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            if (day in 1..daysInMonth) {
                                val isToday = day == today
                                val billings = billingDays[day]
                                val hasBilling = billings != null

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(if (isToday) 28.dp else 24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isToday && hasBilling -> MaterialTheme.colorScheme.primary
                                                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = AnimationTokens.Alpha.faint)
                                                    hasBilling -> SignalAmber.copy(alpha = AnimationTokens.Alpha.faint)
                                                    else -> Color.Transparent
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "$day",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isToday || hasBilling) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isToday && hasBilling -> Color.White
                                                isToday -> MaterialTheme.colorScheme.primary
                                                hasBilling -> SignalAmber
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                    // 扣费指示点
                                    if (hasBilling && billings.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            billings.take(3).forEach { (_, color) ->
                                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(color))
                                            }
                                            if (billings.size > 3) {
                                                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)))
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(30.dp))
                            }
                        }
                    }
                }
            }

            // 图例
            if (billingDays.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("今天", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SignalAmber.copy(alpha = AnimationTokens.Alpha.faint)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("扣费日", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

