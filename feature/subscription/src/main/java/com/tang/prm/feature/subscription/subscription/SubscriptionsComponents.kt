package com.tang.prm.feature.subscription.subscription

import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
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
import com.tang.prm.domain.model.Subscription
import com.tang.prm.domain.model.SubscriptionCycle
import com.tang.prm.domain.model.SubscriptionStatus
import com.tang.prm.domain.model.computedStatus
import com.tang.prm.domain.model.monthlyEquivalent
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.theme.SemanticAmberBg
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SemanticCoralBg
import com.tang.prm.ui.theme.SemanticCoralText
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalCoral
import com.tang.prm.ui.theme.SignalGreen

private val SemanticGreenBg: Color
    @Composable get() = if (isSystemInDarkTheme()) Color(0xFF1A3A2E) else Color(0xFFD1FAE5)

@Composable
private fun isSystemInDarkTheme(): Boolean =
    androidx.compose.foundation.isSystemInDarkTheme()

@Composable
fun StatsSummaryCard(
    monthlyTotal: Double,
    yearlyTotal: Double,
    expiringSoonCount: Int,
    activeCount: Int,
    onViewStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "费用概览",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "查看统计 ›",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onViewStats)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                StatItem(
                    label = "月度支出",
                    value = "¥${"%.0f".format(monthlyTotal)}",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "年度支出",
                    value = "¥${"%.0f".format(yearlyTotal)}",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "即将到期",
                    value = "$expiringSoonCount",
                    valueColor = if (expiringSoonCount > 0) SignalAmber else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "活跃订阅",
                    value = "$activeCount",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}

@Composable
fun FilterTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("全部", "活跃", "已取消", "已过期")
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Surface(
                onClick = { onTabSelected(index) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CategoryGroupCard(
    category: String,
    subscriptions: List<Subscription>,
    onItemClick: (Subscription) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryMonthlyTotal = subscriptions.sumOf { it.monthlyEquivalent() }
    val dotColor = getCategoryColor(category)

    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "¥${"%.0f".format(categoryMonthlyTotal)}/月",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            subscriptions.forEachIndexed { index, subscription ->
                SubscriptionRowItem(
                    subscription = subscription,
                    onClick = { onItemClick(subscription) }
                )
                if (index < subscriptions.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun SubscriptionRowItem(
    subscription: Subscription,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = subscription.name.take(1),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subscription.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatPrice(subscription),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        CountdownBadge(subscription = subscription)
    }
}

@Composable
fun CountdownBadge(
    subscription: Subscription,
    modifier: Modifier = Modifier
) {
    val status = subscription.computedStatus()
    if (status == SubscriptionStatus.EXPIRED) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = "已过期",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else if (status == SubscriptionStatus.CANCELLED) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = "已取消",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else {
        val daysRemaining = ((subscription.nextBillingDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
        val (bgColor, textColor) = when {
            daysRemaining <= 3 -> SemanticCoralBg to SignalCoral
            daysRemaining <= 7 -> SemanticAmberBg to SignalAmber
            else -> SemanticGreenBg to SignalGreen
        }
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(10.dp),
            color = bgColor
        ) {
            Text(
                text = "${daysRemaining}天",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun formatPrice(subscription: Subscription): String {
    val symbol = when (subscription.currency) {
        "CNY" -> "¥"
        "USD" -> "$"
        "EUR" -> "€"
        else -> ""
    }
    val cycleLabel = subscription.cycle.displayName
    return "$symbol${subscription.price}/$cycleLabel"
}

private val categoryColors: List<Color>
    @Composable get() = listOf(
        SignalGreen, SignalAmber, SignalCoral, MaterialTheme.colorScheme.primary,
        Color(0xFF6366F1), Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFF14B8A6)
    )

@Composable
private fun getCategoryColor(category: String?): Color {
    if (category == null) return MaterialTheme.colorScheme.onSurfaceVariant
    val index = category.hashCode().let { if (it < 0) -it else it } % categoryColors.size
    return categoryColors[index]
}
