@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.home

import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.components.SignalProgress
import com.tang.prm.ui.components.SignalProgressStyle
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.core.rememberIsResumed
import com.tang.prm.ui.animation.core.rememberPausableInfiniteFloatLoop
import com.tang.prm.ui.animation.primitives.rememberBlinkingAlpha
import com.tang.prm.ui.animation.primitives.rememberBreathingPulse
import com.tang.prm.ui.theme.GridLine
import com.tang.prm.ui.theme.PixelFontFamily
import com.tang.prm.domain.usecase.HomeStats
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalElectric
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple

@Composable
internal fun IncomingSignalCard(
    dateStr: String,
    // C-2 修复：用 HomeStats 替代 5 个独立 count 参数，与 HomeStatDef 配合消除配置重复
    stats: HomeStats
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
    // N-12 修复：sharedDotAlpha 从 Row content lambda 中段提到 IncomingSignalCard 顶部，
    // 与其他动画状态（wavePhase/cursorAlpha/dotAlpha）声明位置一致，提升可读性
    // B-4 修复：3 个 dot 共享单一 alpha，节省 2 份动画状态
    val sharedDotAlpha by rememberPausableInfiniteFloatLoop(
        initialValue = 0.2f, targetValue = 1f,
        durationMillis = 600,
        easing = AnimationTokens.Easing.linear,
        repeatMode = RepeatMode.Reverse,
        label = "signalDots"
    )

    // Q-6/C-1 修复：品牌文案提取为 HOME_TAGLINE 常量，与 HomeTabletJournal 共用
    val fullText = HOME_TAGLINE
    var displayedText by remember { mutableStateOf("") }
    var textAlpha by remember { mutableStateOf(0f) }
    var typewriterDone by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // B-3 修复：用 pausableDelay 替代裸 delay，每个 delay 点都响应 isResumed，
        // App 切后台时打字机立即暂停，不再继续推进 ~5 秒浪费电量。
        // N-1 修复：isResumed 改为 lambda 传入，每次循环都读取最新 State.value，
        // 避免长延迟点（5000ms/800ms）执行期间切后台时仍继续推进的边界 bug。
        while (true) {
            if (!isResumed) { kotlinx.coroutines.delay(100); continue }
            displayedText = ""
            typewriterDone = false
            for (a in 0..10) { textAlpha = a / 10f; pausableDelay(30) { isResumed } }
            fullText.forEachIndexed { index, char ->
                val baseDelay = 160L
                val randomExtra = (0..120).random().toLong()
                // N-10 修复：直接使用 forEachIndexed 的 char 参数，无需 fullText[index] 二次取字符
                val charDelay = when {
                    char == '，' || char == '。' || char == '、' || char == '！' || char == '？' -> baseDelay + randomExtra + 200L
                    char == ' ' -> baseDelay + randomExtra + 80L
                    else -> baseDelay + randomExtra
                }
                pausableDelay(charDelay) { isResumed }
                displayedText = fullText.substring(0, index + 1)
            }
            pausableDelay(300) { isResumed }
            typewriterDone = true
            pausableDelay(5000) { isResumed }
            for (a in 10 downTo 0) { textAlpha = a / 10f; pausableDelay(50) { isResumed } }
            pausableDelay(800) { isResumed }
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
                    // N-12 修复：sharedDotAlpha 已提到 IncomingSignalCard 顶部，与其他动画状态声明位置一致
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(3) {
                            Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(SignalGreen.copy(alpha = sharedDotAlpha)))
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
                    modifier = Modifier
                        .weight(1f)
                        // U-6 修复：读屏读完整文案，不随打字机逐字更新触发 announce
                        .semantics {
                            contentDescription = fullText
                            liveRegion = LiveRegionMode.Polite
                        }
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
                // C-2 修复：用 HomeStatDef.entries 遍历，label/color/icon 全部来自单一来源
                HomeStatDef.entries.forEach { def ->
                    StatGauge(def.statProvider(stats), def.label, def.color, def.icon)
                }
            }
        }
    }
}

@Composable
private fun StatGauge(value: Int, label: String, color: Color, icon: ImageVector) {
    val gaugeSize = remember { 44.dp }
    val canvasSize = remember { 40.dp }
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
            // C-3 修复：环形进度委托 SignalProgress Radial 样式，消除手动 drawArc/drawCircle 重复
            SignalProgress(
                value = value,
                maxValue = MAX_SIGNAL_STRENGTH,
                color = color,
                modifier = Modifier.size(canvasSize),
                style = SignalProgressStyle.Radial,
                strokeWidth = 3f
            )
            Text(value.toString(), fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Icon(icon, contentDescription = null, tint = color.copy(alpha = 0.7f), modifier = Modifier.size(8.dp))
            Text(label, fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), letterSpacing = 0.5.sp)
        }
    }
}

/**
 * B-3 修复：可暂停的 delay——以 50ms 步长轮询 isResumed，App 切后台时立即暂停。
 * 替代裸 [kotlinx.coroutines.delay]，让打字机每个等待点都能响应生命周期。
 * 提为顶层函数避免增加 IncomingSignalCard 的圈复杂度。
 * internal 可见性让 HomeScreen 的 dateStr 跨日检查也能复用（P-6 修复）。
 *
 * N-1 修复：[isResumed] 改为 `() -> Boolean` lambda，每次循环都读取最新 State.value，
 * 避免长延迟点（如 5000ms 停留期）执行期间切后台时仍继续推进的边界 bug——
 * 原签名 `isResumed: Boolean` 在调用时被快照，函数内固定值无法响应后续状态变化。
 */
internal suspend fun pausableDelay(ms: Long, isResumed: () -> Boolean) {
    val step = 50L
    var remaining = ms
    while (remaining > 0) {
        if (!isResumed()) { kotlinx.coroutines.delay(100); continue }
        kotlinx.coroutines.delay(minOf(step, remaining))
        remaining -= step
    }
}


