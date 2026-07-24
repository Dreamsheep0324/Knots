package com.tang.prm.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.tang.prm.ui.animation.core.AnimationTokens

private const val BAR_COUNT = 4
private const val TRACK_ALPHA = 0.1f
private const val BAR_ACTIVE_ALPHA = 0.7f
private const val RADIAL_PROGRESS_ALPHA = 0.85f
private const val CORNER_RADIUS_PX = 2f
private const val BAR_BASE_HEIGHT_DP = 3f
private const val BAR_HEIGHT_STEP_DP = 1.5f
private val BAR_WIDTH = 2.5.dp

/**
 * 信号进度共享组件（C-3 修复）。
 *
 * 统一首页三处"基于 value/maxValue 计算进度并绘制"的实现：
 * - [SignalProgressStyle.Linear]：横向进度条（HomeChannelList 频道卡 + HomeTabletJournal 数据一览）
 * - [SignalProgressStyle.Radial]：环形进度弧（HomeSignalCard 统计仪表）
 * - [SignalProgressStyle.Bars]：4 根信号格条（HomeChannelList 频道卡）
 *
 * 三种样式共用同一份 value/maxValue → progress 计算逻辑，魔法数字消除。
 * Radial 样式内置 [animateFloatAsState] 动画，与原 StatGauge 行为一致。
 *
 * @param value 当前进度值
 * @param maxValue 满格阈值
 * @param color 主题色（轨道、填充、渐变均从此派生）
 * @param modifier 布局修饰符
 * @param style 进度样式
 * @param strokeWidth 环形描边宽度（仅 Radial 生效）
 */
@Composable
fun SignalProgress(
    value: Int,
    maxValue: Int,
    color: Color,
    modifier: Modifier = Modifier,
    style: SignalProgressStyle = SignalProgressStyle.Linear,
    strokeWidth: Float = 3f
) {
    if (maxValue <= 0) {
        // 除零守卫：绘制空轨道并返回，避免 NaN 传入绘制管线
        when (style) {
            SignalProgressStyle.Linear -> DrawLinearProgress(0f, color, modifier)
            SignalProgressStyle.Radial -> DrawRadialProgress(0f, color, modifier, strokeWidth)
            SignalProgressStyle.Bars -> DrawBarsProgress(0, 1, color, modifier)
        }
        return
    }
    val progress = (value.toFloat() / maxValue).coerceIn(0f, 1f)
    when (style) {
        SignalProgressStyle.Linear -> DrawLinearProgress(progress, color, modifier)
        SignalProgressStyle.Radial -> DrawRadialProgress(progress, color, modifier, strokeWidth)
        SignalProgressStyle.Bars -> DrawBarsProgress(value, maxValue, color, modifier)
    }
}

enum class SignalProgressStyle { Linear, Radial, Bars }

@Composable
private fun DrawLinearProgress(progress: Float, color: Color, modifier: Modifier) {
    Canvas(modifier) {
        drawRoundRect(color = color.copy(alpha = TRACK_ALPHA), cornerRadius = CornerRadius(CORNER_RADIUS_PX))
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(color.copy(alpha = 0.6f), color.copy(alpha = 0.3f))
            ),
            size = Size(size.width * progress, size.height),
            cornerRadius = CornerRadius(CORNER_RADIUS_PX)
        )
    }
}

@Composable
private fun DrawRadialProgress(progress: Float, color: Color, modifier: Modifier, strokeWidth: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(AnimationTokens.Duration.dramatic),
        label = "signalProgressRadial"
    )
    Canvas(modifier) {
        val radius = (size.minDimension - strokeWidth) / 2
        val c = Offset(size.width / 2, size.height / 2)
        drawCircle(color = color.copy(alpha = TRACK_ALPHA), radius = radius, center = c, style = Stroke(width = strokeWidth))
        drawArc(
            color = color.copy(alpha = RADIAL_PROGRESS_ALPHA),
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            size = Size(radius * 2, radius * 2),
            topLeft = Offset(c.x - radius, c.y - radius)
        )
    }
}

@Composable
private fun DrawBarsProgress(value: Int, maxValue: Int, color: Color, modifier: Modifier) {
    val level = (value.toFloat() / maxValue * BAR_COUNT).toInt().coerceIn(0, BAR_COUNT)
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(1.dp), verticalAlignment = Alignment.Bottom) {
        repeat(BAR_COUNT) { i ->
            Box(
                modifier = Modifier
                    .width(BAR_WIDTH)
                    .height((BAR_BASE_HEIGHT_DP + i * BAR_HEIGHT_STEP_DP).dp)
                    .background(
                        if (i < level) color.copy(alpha = BAR_ACTIVE_ALPHA) else color.copy(alpha = TRACK_ALPHA),
                        RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}
