package com.tang.prm.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.SignalProgress
import com.tang.prm.ui.components.SignalProgressStyle
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.primitives.rememberBreathingPulse
import com.tang.prm.ui.animation.primitives.rememberShimmerPhase
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun ChannelList(
    channels: List<ChannelDef>,
    signalStrengths: Map<Any, Int>,
    onChannelClick: (Any) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        channels.forEach { channel ->
            val strength = signalStrengths[channel.route] ?: 0
            GlassChannelItem(
                channel = channel,
                signalStrength = strength,
                onClick = { onChannelClick(channel.route) }
            )
        }
    }
}

/**
 * 频道图标圆共享组件（C-5 修复）。
 *
 * 消除 [GlassChannelItem] 与 HomeTabletJournal [JournalQuickAccess] 两处"频道图标圆"的重复。
 * - [withShimmer] = true：带径向渐变光晕 + shimmer 旋转点（频道卡场景）
 * - [withShimmer] = false：纯色圆 + 图标（快捷入口场景）
 *
 * 内圆背景统一用 [AnimationTokens.Alpha.subtle]（= 0.12f），与原两处实现一致，
 * JournalQuickAccess 侧无视觉差异；GlassChannelItem 侧保持原有 halo + shimmer 效果。
 *
 * 注意：参数命名为 [diameter] 而非 size，避免在 Canvas lambda 内遮蔽
 * DrawScope.size（Size 类型）导致 size.width / size.height 解析失败。
 *
 * @param channel 频道定义（取 color / icon / textIcon / name）
 * @param diameter 内圆直径，图标尺寸按 0.52 比例缩放
 * @param withShimmer 是否带光晕 + shimmer 动画
 * @param breathAlpha 呼吸动画 alpha（由调用方传入，避免无条件创建动画状态）
 * @param shimmerPhase shimmer 相位（同上）
 */
@Composable
internal fun ChannelIcon(
    channel: ChannelDef,
    modifier: Modifier = Modifier,
    diameter: Dp = 42.dp,
    withShimmer: Boolean = false,
    breathAlpha: Float = 1f,
    shimmerPhase: Float = 0f
) {
    val iconSize = (diameter.value * 0.52f).dp
    val textIconSize = (diameter.value * 0.48f).sp
    // N-8 修复：outerSize 仅 withShimmer 时 +10dp 预留 halo/shimmer 空间；
    // JournalQuickAccess 调用 withShimmer=false（默认），不再多占 10dp×10dp 布局空间
    val outerSize = if (withShimmer) diameter + 10.dp else diameter
    Box(modifier = modifier.size(outerSize), contentAlignment = Alignment.Center) {
        if (withShimmer) {
            Box(
                modifier = Modifier
                    .size(outerSize)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                channel.color.copy(alpha = 0.2f * breathAlpha),
                                channel.color.copy(alpha = 0.05f)
                            )
                        ),
                        CircleShape
                    )
            )
        }
        Box(
            modifier = Modifier
                .size(diameter)
                .background(channel.color.copy(alpha = AnimationTokens.Alpha.subtle), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (channel.textIcon != null) {
                Text(channel.textIcon, fontSize = textIconSize, color = channel.color)
            } else if (channel.icon != null) {
                Icon(
                    channel.icon,
                    contentDescription = channel.name,
                    tint = channel.color,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
        if (withShimmer) {
            Canvas(modifier = Modifier.size(outerSize)) {
                // 此处 size 指向 DrawScope.size（Size 类型），直径参数已重命名为 diameter 避免遮蔽
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.minDimension / 2f
                val angle = shimmerPhase * 2f * Math.PI.toFloat()
                val dotX = cx + r * 0.85f * cos(angle)
                val dotY = cy + r * 0.85f * sin(angle)
                drawCircle(
                    color = channel.color.copy(alpha = 0.5f * breathAlpha),
                    radius = 2f,
                    center = Offset(dotX, dotY)
                )
            }
        }
    }
}

/** 频道信号横向进度条（Q-15 修复），委托 SignalProgress Linear 样式。 */
@Composable
private fun ChannelProgressBar(channel: ChannelDef, signalStrength: Int) {
    SignalProgress(
        value = signalStrength,
        maxValue = MAX_SIGNAL_STRENGTH,
        color = channel.color,
        modifier = Modifier.width(80.dp).height(4.dp),
        style = SignalProgressStyle.Linear
    )
}

/** 频道信号 4 格条（Q-15 修复），委托 SignalProgress Bars 样式。 */
@Composable
private fun ChannelSignalBars(channel: ChannelDef, signalStrength: Int) {
    SignalProgress(
        value = signalStrength,
        maxValue = MAX_SIGNAL_STRENGTH,
        color = channel.color,
        style = SignalProgressStyle.Bars
    )
}

@Composable
private fun GlassChannelItem(
    channel: ChannelDef,
    signalStrength: Int,
    onClick: () -> Unit
) {
    val breathAlpha by rememberBreathingPulse(
        minAlpha = 0.5f,
        maxAlpha = 0.9f,
        cycleDuration = 2500
    )
    val shimmerPhase by rememberShimmerPhase(cycleDuration = 3000)

    // Q-12 修复：AppCard 支持 onClick 参数，内部用 Surface.clickable 统一 ripple 语义；
    // U-9 修复：mergeDescendants 合并卡片内多个 Text 为单一语义节点，读屏一次播报完整信息
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "${channel.name}，信号强度 ${signalStrength}，${channel.desc}"
            },
        onClick = onClick
    ) {
        // Q-15 修复：主函数只做组合，图标 / 进度条 / 格条分别委托独立 Composable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChannelIcon(
                channel = channel,
                diameter = 42.dp,
                withShimmer = true,
                breathAlpha = breathAlpha,
                shimmerPhase = shimmerPhase
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        channel.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = channel.color.copy(alpha = 0.1f)
                    ) {
                        Text(
                            signalStrength.toString(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = channel.color,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    channel.desc,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ChannelProgressBar(channel = channel, signalStrength = signalStrength)
                    Spacer(modifier = Modifier.width(8.dp))
                    ChannelSignalBars(channel = channel, signalStrength = signalStrength)
                }
            }

            Box(
                modifier = Modifier
                    // U-5 修复：ChevronRight 容器从 32dp 缩为 24dp，与整体卡片视觉比例更协调
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.06f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = channel.color.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
