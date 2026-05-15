package com.tang.prm.ui.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.primitives.rememberBreathingPulse
import com.tang.prm.ui.animation.primitives.rememberShimmerPhase
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun ChannelGrid(
    channels: List<ChannelDef>,
    signalStrengths: Map<String, Int>,
    onChannelClick: (String) -> Unit
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

    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
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
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(channel.color.copy(alpha = AnimationTokens.Alpha.subtle), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            channel.icon,
                            contentDescription = channel.name,
                            tint = channel.color,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Canvas(modifier = Modifier.size(52.dp)) {
                        val cx = size.width / 2
                        val cy = size.height / 2
                        val r = size.minDimension / 2
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
                        Canvas(modifier = Modifier.width(80.dp).height(4.dp)) {
                            val progress = (signalStrength.toFloat() / 50f).coerceIn(0f, 1f)
                            drawRoundRect(
                                color = channel.color.copy(alpha = 0.1f),
                                cornerRadius = CornerRadius(2f)
                            )
                            drawRoundRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        channel.color.copy(alpha = 0.6f),
                                        channel.color.copy(alpha = 0.3f)
                                    )
                                ),
                                size = Size(size.width * progress, size.height),
                                cornerRadius = CornerRadius(2f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(1.dp), verticalAlignment = Alignment.Bottom) {
                            repeat(4) { i ->
                                val level = (signalStrength.toFloat() / 50f * 4).toInt().coerceIn(0, 4)
                                val barH = (3 + i * 1.5f).dp
                                Box(
                                    modifier = Modifier
                                        .width(2.5.dp)
                                        .height(barH)
                                        .background(
                                            if (i < level) channel.color.copy(alpha = 0.7f)
                                            else channel.color.copy(alpha = 0.1f),
                                            RoundedCornerShape(1.dp)
                                        )
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.06f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = channel.color.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
    }
}
