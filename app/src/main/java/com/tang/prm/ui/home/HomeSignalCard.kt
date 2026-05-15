@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.core.rememberIsResumed
import com.tang.prm.ui.animation.core.rememberPausableInfiniteFloatLoop
import com.tang.prm.ui.animation.primitives.rememberBlinkingAlpha
import com.tang.prm.ui.animation.primitives.rememberBreathingPulse
import com.tang.prm.ui.theme.GridLine
import com.tang.prm.ui.theme.PixelFontFamily
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalCoral
import com.tang.prm.ui.theme.SignalElectric
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky

@Composable
internal fun IncomingSignalCard(
    dateStr: String,
    contactCount: Int,
    eventCount: Int,
    giftCount: Int,
    anniversaryCount: Int,
    conversationCount: Int
) {
    val isResumed by rememberIsResumed()
    val wavePhase by rememberPausableInfiniteFloatLoop(
        initialValue = 0f, targetValue = 1f,
        durationMillis = 6000,
        easing = AnimationTokens.Easing.linear,
        repeatMode = RepeatMode.Restart,
        label = "wavePhase"
    )
    val cursorAlpha by rememberBlinkingAlpha(onDuration = 530, offDuration = 530)
    val dotAlpha by rememberBreathingPulse(minAlpha = 0.3f, maxAlpha = 1f, cycleDuration = 800)

    val fullText = "但今天只是今天，未来也只是今天的未来"
    var displayedText by remember { mutableStateOf("") }
    var textAlpha by remember { mutableStateOf(0f) }
    var typewriterDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            if (!isResumed) { kotlinx.coroutines.delay(100); continue }
            displayedText = ""
            typewriterDone = false
            for (a in 0..10) { textAlpha = a / 10f; kotlinx.coroutines.delay(30) }
            fullText.forEachIndexed { index, _ ->
                val baseDelay = 160L
                val randomExtra = (0..120).random().toLong()
                val char = fullText[index]
                val charDelay = when {
                    char == '，' || char == '。' || char == '、' || char == '！' || char == '？' -> baseDelay + randomExtra + 200L
                    char == ' ' -> baseDelay + randomExtra + 80L
                    else -> baseDelay + randomExtra
                }
                kotlinx.coroutines.delay(charDelay)
                displayedText = fullText.substring(0, index + 1)
            }
            kotlinx.coroutines.delay(300)
            typewriterDone = true
            kotlinx.coroutines.delay(5000)
            for (a in 10 downTo 0) { textAlpha = a / 10f; kotlinx.coroutines.delay(50) }
            kotlinx.coroutines.delay(800)
        }
    }

    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SignalGreen.copy(alpha = dotAlpha)))
                    Text("信号接收中", fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SignalGreen, letterSpacing = 1.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(3) { i ->
                            val delayAlpha by rememberPausableInfiniteFloatLoop(
                                initialValue = 0.2f, targetValue = 1f,
                                durationMillis = 600,
                                easing = AnimationTokens.Easing.linear,
                                repeatMode = RepeatMode.Reverse,
                                label = "dot$i"
                            )
                            Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(SignalGreen.copy(alpha = delayAlpha)))
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalAmber))
                    Text(dateStr, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.visible))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val barBaseColor = MaterialTheme.colorScheme.onSurface
            Canvas(modifier = Modifier.fillMaxWidth().height(18.dp)) {
                val barCount = 40
                val barWidth = size.width / barCount
                val centerH = size.height / 2
                for (i in 0 until barCount) {
                    val phase = wavePhase * 2 * Math.PI.toFloat()
                    val x = i * barWidth
                    val edgeFade = when {
                        i < 4 -> i / 4f
                        i > barCount - 4 -> (barCount - i) / 4f
                        else -> 1f
                    }
                    val amplitude = 16f * edgeFade
                    val waveL = (kotlin.math.sin(i * 0.35 + phase) * amplitude).toFloat()
                    val waveR = (kotlin.math.sin(i * 0.35 + phase + 1.5f) * amplitude * 0.7f).toFloat()
                    val barH = kotlin.math.abs(waveL) + kotlin.math.abs(waveR)
                    val alpha = ((0.3f + kotlin.math.abs(waveL) / 18f) * edgeFade).coerceIn(0f, 1f)
                    val barColor = barBaseColor
                    drawRoundRect(
                        color = barColor.copy(alpha = alpha),
                        topLeft = Offset(x + barWidth * 0.15f, centerH - barH / 2),
                        size = Size(barWidth * 0.7f, barH),
                        cornerRadius = CornerRadius(3f)
                    )
                }
                drawLine(color = SignalElectric.copy(alpha = 0.15f), start = Offset(0f, centerH), end = Offset(size.width, centerH), strokeWidth = 0.5f)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Text("「", fontFamily = PixelFontFamily, fontSize = 14.sp, color = SignalPurple.copy(alpha = textAlpha * 0.6f))
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    displayedText,
                    fontFamily = PixelFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = textAlpha),
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f)
                )
                if (!typewriterDone) {
                    Box(modifier = Modifier.width(2.dp).height(16.dp).background(SignalPurple.copy(alpha = cursorAlpha)))
                }
                Spacer(modifier = Modifier.width(2.dp))
                Text("」", fontFamily = PixelFontFamily, fontSize = 14.sp, color = SignalPurple.copy(alpha = textAlpha * 0.6f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(GridLine.copy(alpha = 0.4f)))
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalPurple.copy(alpha = 0.4f)))
                Spacer(modifier = Modifier.width(6.dp))
                Box(modifier = Modifier.weight(1f).height(1.dp).background(GridLine.copy(alpha = 0.4f)))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatGauge(contactCount, "人物", SignalSky, Icons.Default.People)
                StatGauge(eventCount, "事件", SignalGreen, Icons.Default.Event)
                StatGauge(giftCount, "礼物", SignalCoral, Icons.Default.CardGiftcard)
                StatGauge(anniversaryCount, "纪念日", SignalPurple, Icons.Default.Favorite)
                StatGauge(conversationCount, "对话", SignalAmber, Icons.AutoMirrored.Filled.Chat)
            }
        }
    }
}

@Composable
private fun StatGauge(value: Int, label: String, color: Color, icon: ImageVector) {
    val maxValue = remember { 50f }
    val strokeWidth = remember { 3f }
    val gaugeSize = remember { 44.dp }
    val canvasSize = remember { 40.dp }
    val progress = (value.toFloat() / maxValue).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(AnimationTokens.Duration.dramatic), label = "gaugeProgress")

    val pulseAlpha by rememberBreathingPulse(minAlpha = 0.4f, maxAlpha = 0.8f, cycleDuration = 2000)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(gaugeSize), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(gaugeSize)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(color.copy(alpha = 0.15f * pulseAlpha), Color.Transparent)
                        ),
                        CircleShape
                    )
            )
            Canvas(modifier = Modifier.size(canvasSize)) {
                val radius = (size.minDimension - strokeWidth) / 2
                val c = Offset(size.width / 2, size.height / 2)

                drawCircle(color = color.copy(alpha = 0.1f), radius = radius, center = c, style = Stroke(width = strokeWidth))

                drawArc(color = color.copy(alpha = 0.85f), startAngle = -90f, sweepAngle = 360f * animatedProgress,
                    useCenter = false, style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    size = Size(radius * 2, radius * 2), topLeft = Offset(c.x - radius, c.y - radius))
            }
            Text(value.toString(), fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Icon(icon, contentDescription = null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(8.dp))
            Text(label, fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), letterSpacing = 0.5.sp)
        }
    }
}

@Composable
internal fun SectionHeader(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    action: String?,
    onActionClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(12.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(iconColor.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )
        if (action != null && onActionClick != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = iconColor.copy(alpha = AnimationTokens.Alpha.faint)
            ) {
                Text(
                    action,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = iconColor,
                    modifier = Modifier
                        .clickable { onActionClick() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}
