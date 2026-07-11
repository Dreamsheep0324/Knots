package com.tang.prm.feature.subscription.subscription

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.tang.prm.domain.model.Subscription
import com.tang.prm.domain.model.SubscriptionCycle
import com.tang.prm.domain.model.SubscriptionStatus
import com.tang.prm.domain.model.computedStatus
import com.tang.prm.domain.model.monthlyEquivalent
import com.tang.prm.domain.model.yearlyEquivalent
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.DeleteConfirmDialog
import com.tang.prm.ui.navigation.EditSubscriptionRoute
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.Error
import com.tang.prm.ui.theme.SignalSky

data class SubscriptionDetailDataState(
    val subscription: Subscription? = null,
    val isLoading: Boolean = false
)

data class SubscriptionDetailDialogState(
    val showDeleteConfirm: Boolean = false
)

data class SubscriptionDetailUiState(
    val data: SubscriptionDetailDataState = SubscriptionDetailDataState(),
    val dialog: SubscriptionDetailDialogState = SubscriptionDetailDialogState()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailScreen(
    subscriptionId: Long,
    navController: NavController,
    viewModel: SubscriptionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.dialog.showDeleteConfirm) {
        DeleteConfirmDialog(
            title = "确认删除",
            message = "确定要删除这个订阅吗？删除后无法恢复。",
            onConfirm = {
                viewModel.hideDeleteConfirm()
                viewModel.deleteSubscription()
                navController.popBackStack()
            },
            onDismiss = { viewModel.hideDeleteConfirm() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        uiState.data.subscription?.let {
                            navController.navigate(EditSubscriptionRoute(it.id))
                        }
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = { viewModel.showDeleteConfirm() }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = Error, modifier = Modifier.size(22.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        val sub = uiState.data.subscription
        if (sub != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { SubscriptionHeroCard(sub) }
                item { CostConversionRow(sub) }
                item { SubscriptionTimelineCard(sub) }
                item { BillingProgressCard(sub) }
                item { DateInfoCard(sub) }
                item { SubscriptionCycleInfoCard(sub) }
                sub.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                    item { NotesCard(notes) }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        } else if (uiState.data.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ── Hero卡片 ──
@Composable
private fun SubscriptionHeroCard(sub: Subscription) {
    val status = sub.computedStatus()
    val (statusLabel, statusColor) = when (status) {
        SubscriptionStatus.ACTIVE -> "活跃" to SignalGreen
        SubscriptionStatus.CANCELLED -> "已取消" to MaterialTheme.colorScheme.onSurfaceVariant
        SubscriptionStatus.EXPIRED -> "已过期" to SignalAmber
    }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = AnimationTokens.Alpha.faint)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(sub.name.take(1), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(sub.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        sub.category?.let { category ->
                            Text(category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Surface(shape = RoundedCornerShape(12.dp), color = statusColor.copy(alpha = AnimationTokens.Alpha.faint)) {
                    Text(statusLabel, modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(formatCurrencySymbol(sub.currency), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 6.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(formatPriceValue(sub.price), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.width(6.dp))
                Text("/ ${sub.cycle.displayName}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
            }
        }
    }
}

// ── 扣费进度条卡片 ──
@Composable
private fun BillingProgressCard(sub: Subscription) {
    val now = System.currentTimeMillis()
    val cycleDuration = sub.nextBillingDate - sub.startDate
    val elapsed = (now - sub.startDate).coerceAtLeast(0)
    val progress = if (cycleDuration > 0) (elapsed.toFloat() / cycleDuration).coerceIn(0f, 1f) else 0f
    val daysUntilBilling = ((sub.nextBillingDate - now) / (1000 * 60 * 60 * 24)).toInt()
    val daysSinceStart = ((now - sub.startDate) / (1000 * 60 * 60 * 24)).toInt()

    val progressColor = when {
        daysUntilBilling <= 3 -> SignalAmber
        daysUntilBilling <= 7 -> SignalAmber.copy(alpha = 0.8f)
        else -> SignalGreen
    }

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("扣费周期", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (daysUntilBilling > 0) "${daysUntilBilling}" else "0", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (daysUntilBilling <= 7) SignalAmber else SignalGreen, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (daysUntilBilling > 0) "天后扣费" else "已到期", style = MaterialTheme.typography.labelMedium, color = if (daysUntilBilling <= 7) SignalAmber else SignalGreen, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(DateUtils.formatMonthDay(sub.startDate), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(DateUtils.formatMonthDay(sub.nextBillingDate), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ProgressStatItem("已订阅", "${daysSinceStart}天", SignalSky)
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)).align(Alignment.CenterVertically))
                ProgressStatItem("进度", "${(progress * 100).toInt()}%", MaterialTheme.colorScheme.primary)
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)).align(Alignment.CenterVertically))
                ProgressStatItem("剩余", "${daysUntilBilling}天", if (daysUntilBilling <= 7) SignalAmber else SignalGreen)
            }
        }
    }
}

@Composable
private fun ProgressStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = color)
    }
}

// ── 费用换算三栏 ──
@Composable
private fun CostConversionRow(sub: Subscription) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ConversionCard("日均", formatPriceWithSymbol(sub.yearlyEquivalent() / 365.0, sub.currency), SignalSky, Modifier.weight(1f))
        ConversionCard("月均", formatPriceWithSymbol(sub.monthlyEquivalent(), sub.currency), SignalPurple, Modifier.weight(1f))
        ConversionCard("年均", formatPriceWithSymbol(sub.yearlyEquivalent(), sub.currency), SignalAmber, Modifier.weight(1f))
    }
}

@Composable
private fun ConversionCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = AnimationTokens.Alpha.faint)) {
                Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ── 订阅时间线卡片 ──
@Composable
private fun SubscriptionTimelineCard(sub: Subscription) {
    val now = System.currentTimeMillis()
    val totalDays = ((now - sub.startDate) / (1000 * 60 * 60 * 24)).toInt()
    val totalMonths = totalDays / 30
    val totalYears = totalDays / 365

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(SignalPurple.copy(alpha = AnimationTokens.Alpha.faint)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = SignalPurple, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("订阅时长", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TimelineItem("年", totalYears.toString(), SignalAmber)
                TimelineItem("月", totalMonths.toString(), SignalPurple)
                TimelineItem("日", totalDays.toString(), SignalSky)
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun TimelineItem(unit: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(2.dp))
        Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── 日期信息卡片 ──
@Composable
private fun DateInfoCard(sub: Subscription) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            DateInfoRow(icon = Icons.Default.Payments, label = "开始日期", value = DateUtils.formatYearMonthDayChineseFull(sub.startDate), iconColor = SignalSky)
            Spacer(modifier = Modifier.height(14.dp))
            DateInfoRow(icon = Icons.Default.CalendarMonth, label = "下次扣费", value = DateUtils.formatYearMonthDayChineseFull(sub.nextBillingDate), iconColor = SignalAmber)
        }
    }
}

@Composable
private fun DateInfoRow(icon: ImageVector, label: String, value: String, iconColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(iconColor.copy(alpha = AnimationTokens.Alpha.faint)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── 周期信息卡片 ──
@Composable
private fun SubscriptionCycleInfoCard(sub: Subscription) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = AnimationTokens.Alpha.faint)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Repeat, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("周期信息", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(14.dp))

            CycleInfoRow("扣费周期", sub.cycle.displayName)
            Spacer(modifier = Modifier.height(8.dp))
            CycleInfoRow("货币", when (sub.currency) { "CNY" -> "人民币 (¥)"; "USD" -> "美元 ($)"; "EUR" -> "欧元 (€)"; "GBP" -> "英镑 (£)"; else -> sub.currency })
            if (sub.cycle != SubscriptionCycle.ONE_TIME) {
                Spacer(modifier = Modifier.height(8.dp))
                val cycleDays = when (sub.cycle) {
                    SubscriptionCycle.WEEKLY -> 7
                    SubscriptionCycle.MONTHLY -> 30
                    SubscriptionCycle.QUARTERLY -> 90
                    SubscriptionCycle.YEARLY -> 365
                    SubscriptionCycle.ONE_TIME -> 0
                }
                CycleInfoRow("周期天数", "${cycleDays}天")
                Spacer(modifier = Modifier.height(8.dp))
                CycleInfoRow("日均成本", formatPriceWithSymbol(sub.yearlyEquivalent() / 365.0, sub.currency))
            }
        }
    }
}

@Composable
private fun CycleInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

// ── 备注卡片 ──
@Composable
private fun NotesCard(notes: String) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text("备注", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Text(notes, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(14.dp), lineHeight = 22.sp)
            }
        }
    }
}

// ── 格式化工具 ──
private fun formatCurrencySymbol(currency: String): String = when (currency) { "CNY" -> "¥"; "USD" -> "$"; "EUR" -> "€"; "GBP" -> "£"; else -> currency }
private fun formatPriceValue(price: Double): String = if (price == price.toLong().toDouble()) "%.0f".format(price) else "%.2f".format(price)
private fun formatPriceWithSymbol(price: Double, currency: String): String = "${formatCurrencySymbol(currency)}${formatPriceValue(price)}"
