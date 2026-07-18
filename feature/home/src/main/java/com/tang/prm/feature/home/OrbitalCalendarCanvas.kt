package com.tang.prm.feature.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.animation.primitives.rememberBreathingPulse
import com.tang.prm.ui.animation.primitives.rememberFadePulse
import com.tang.prm.ui.animation.primitives.rememberOrbitalRotations
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalCoral
import com.tang.prm.ui.theme.SignalElectric
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val bagua = listOf("坎", "艮", "震", "巽", "离", "坤", "兑", "乾")

private data class ParticleState(
    val angle: Float,
    val radiusOffset: Float,
    val size: Float,
    val alpha: Float,
    val color: Color,
    val speed: Float,
    val breathPhase: Float
)

/**
 * 轨道日历画布所有绘制半径与中心点的集合（Q-10 修复）。
 *
 * 把原本散落在主函数内的 10+ 个几何常量集中到一个 data class，
 * 让每个 [DrawScope] 扩展函数只需接收一个 [OrbitalRadii] 即可定位，
 * 不再需要逐函数罗列 9 个 Float 参数。
 */
private data class OrbitalRadii(
    val center: Offset,
    val innerDecorR: Float,
    val innerR: Float,
    val weekR: Float,
    val mainR: Float,
    val dateTextR: Float,
    val weekTextR: Float,
    val outerR: Float,
    val outerR2: Float,
    val particleR: Float
) {
    companion object {
        fun from(size: Size): OrbitalRadii {
            val minDim = minOf(size.width, size.height) * 1.05f
            return OrbitalRadii(
                center = Offset(size.width / 2, size.height / 2),
                innerDecorR = minDim * 0.10f,
                innerR = minDim * 0.15f,
                weekR = minDim * 0.21f,
                mainR = minDim * 0.26f,
                dateTextR = minDim * 0.31f,
                weekTextR = minDim * 0.35f,
                outerR = minDim * 0.41f,
                outerR2 = minDim * 0.44f,
                particleR = minDim * 0.48f
            )
        }
    }
}

@Composable
internal fun OrbitalCalendarCanvas(
    data: OrbitalCalendarData,
    todayDay: Int,
    modifier: Modifier = Modifier.fillMaxWidth().height(242.dp)
) {
    val pulsePhase by rememberFadePulse(cycleDuration = 2000)
    val rotations by rememberOrbitalRotations(
        scanCycleDuration = 10000,
        rotateCycleDuration = 60000,
        particleCycleDuration = 90000,
        crosshairCycleDuration = 120000
    )
    val breathAlpha by rememberBreathingPulse(minAlpha = 0.3f, maxAlpha = 0.7f, cycleDuration = 3000)
    val ripplePhase by rememberFadePulse(cycleDuration = 3000)

    val particles = remember {
        // P-2 修复：用固定种子 Random，确保每次进程启动粒子初始位置一致，视觉稳定性好
        val rng = Random(42L)
        val colors = listOf(SignalElectric, SignalSky, SignalPurple, SignalGreen)
        (0 until 25).map {
            ParticleState(
                angle = rng.nextFloat() * 360f,
                radiusOffset = rng.nextFloat() * 6f - 3f,
                size = rng.nextFloat() * 2f + 1f,
                alpha = rng.nextFloat() * 0.4f + 0.2f,
                color = colors[rng.nextInt(colors.size)],
                speed = rng.nextFloat() * 0.3f + 0.85f,
                breathPhase = rng.nextFloat() * 2f
            )
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val weekLabels = remember { listOf("日", "一", "二", "三", "四", "五", "六") }
    val darkText = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
    val weekDayColor = SignalElectric.copy(alpha = 0.9f)
    val weekEndColor = SignalCoral.copy(alpha = 0.9f)

    val dayTextStyle = remember { TextStyle(fontFamily = FontFamily.Monospace, fontSize = 8.sp, fontWeight = FontWeight.Bold) }
    val dayTodayStyle = remember { TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White) }
    val dayEventStyle = remember { TextStyle(fontFamily = FontFamily.Monospace, fontSize = 8.sp, fontWeight = FontWeight.Bold) }
    val centerDayStyle = remember { TextStyle(fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SignalElectric) }
    val baguaStyle = remember { TextStyle(fontFamily = FontFamily.Monospace, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = SignalPurple.copy(alpha = 0.6f)) }
    val weekLabelStyle = remember { TextStyle(fontFamily = FontFamily.Monospace, fontSize = 6.sp, fontWeight = FontWeight.Bold) }
    val weekEndLabelStyle = remember { TextStyle(fontFamily = FontFamily.Monospace, fontSize = 6.sp, fontWeight = FontWeight.Bold) }

    // 将 draw 阶段不变的颜色列表提升到 remember，避免每帧创建新对象
    val gridColors = remember {
        listOf(
            SignalElectric.copy(alpha = 0.04f),
            SignalSky.copy(alpha = 0.03f),
            SignalElectric.copy(alpha = 0.04f),
            SignalSky.copy(alpha = 0.03f),
            SignalElectric.copy(alpha = 0.03f),
            SignalSky.copy(alpha = 0.03f),
            SignalElectric.copy(alpha = 0.04f)
        )
    }
    val rayColors = remember {
        listOf(SignalElectric, SignalSky, SignalGreen, SignalPurple, SignalCoral, SignalElectric, SignalSky, SignalGreen)
    }
    val dashPathEffect = remember { PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f) }
    val sortedSignalKeys = remember(data.daySignalMap) { data.daySignalMap.keys.sorted() }

    val dayTextMeasures = remember(data.daysInMonth, data.isCurrentMonth, todayDay, data.daySignalMap) {
        (1..data.daysInMonth).map { day ->
            val isToday = data.isCurrentMonth && day == todayDay
            val signalCount = data.daySignalMap[day] ?: 0
            val style = when {
                isToday -> dayTodayStyle
                signalCount > 0 -> dayEventStyle.copy(color = SignalElectric.copy(alpha = 0.95f))
                else -> dayTextStyle.copy(color = darkText)
            }
            day to textMeasurer.measure(day.toString(), style)
        }
    }

    val weekDayMeasures = remember(data.daysInMonth, data.dayOfWeekMap) {
        (1..data.daysInMonth).map { day ->
            val dow = data.dayOfWeekMap[day] ?: 0
            val isWEnd = dow == 0 || dow == 6
            val style = if (isWEnd) weekEndLabelStyle.copy(color = weekEndColor) else weekLabelStyle.copy(color = weekDayColor)
            day to textMeasurer.measure(weekLabels[dow], style)
        }
    }

    val baguaMeasures = remember { bagua.map { textMeasurer.measure(it, baguaStyle) } }
    val centerDayMeasured = remember(data.isCurrentMonth, todayDay) {
        textMeasurer.measure(if (data.isCurrentMonth) todayDay.toString() else "1", centerDayStyle)
    }

    // Q-10 修复：Canvas 主块按视觉层顺序调用各 DrawScope 扩展函数，
    // 单个绘制单元独立可读、可测试，主函数只负责按顺序组合。
    Canvas(modifier = modifier) {
        val radii = OrbitalRadii.from(size)
        drawOrbitalGrid(radii, gridColors, rayColors)
        drawOuterHaloAndTicks(radii, breathAlpha, dashPathEffect)
        drawMonthProgressArc(radii, data, todayDay)
        drawMiddleRings(radii)
        drawInnerDecorLines(radii)
        drawBagua(radii, baguaMeasures)
        drawEventArcs(radii, data, sortedSignalKeys)
        if (data.isCurrentMonth) {
            drawTodayIndicator(radii, data, todayDay, ripplePhase)
        }
        drawScanLine(radii, rotations.scanAngle)
        drawRotatingDots(radii, rotations.rotateAngle)
        drawParticles(radii, particles, rotations.particleAngle, ripplePhase)
        drawDayDots(radii, data, todayDay, pulsePhase)
        drawDayTexts(radii, data, dayTextMeasures)
        drawWeekTexts(radii, data, weekDayMeasures)
        drawCenterDecorations(radii, rotations.crosshairAngle, centerDayMeasured)
    }
}

// ── 以下为 Q-10 拆分出的私有 DrawScope 扩展函数 ──
// 按视觉层级从外到内、从底到顶排列，每个函数只负责一个独立绘制单元。

/** 绘制 7 圈网格圆环 + 8 条放射线。 */
private fun DrawScope.drawOrbitalGrid(
    radii: OrbitalRadii,
    gridColors: List<Color>,
    rayColors: List<Color>
) {
    val center = radii.center
    for ((idx, r) in listOf(
        radii.innerR, radii.weekR, radii.mainR,
        radii.dateTextR, radii.weekTextR, radii.outerR, radii.outerR2
    ).withIndex()) {
        drawCircle(color = gridColors[idx], radius = r, center = center, style = Stroke(width = 0.5f))
    }
    for (i in 0 until 8) {
        val a = Math.toRadians(i * 45.0 - 90.0)
        drawLine(
            color = rayColors[i].copy(alpha = 0.04f),
            start = Offset(center.x + radii.innerDecorR * cos(a).toFloat(), center.y + radii.innerDecorR * sin(a).toFloat()),
            end = Offset(center.x + radii.outerR2 * cos(a).toFloat(), center.y + radii.outerR2 * sin(a).toFloat()),
            strokeWidth = 0.5f
        )
    }
}

/** 绘制外圈光晕（紫色 halo + 电光虚线 + 天蓝细线）+ 360° 刻度点。 */
private fun DrawScope.drawOuterHaloAndTicks(
    radii: OrbitalRadii,
    breathAlpha: Float,
    dashPathEffect: PathEffect
) {
    val center = radii.center
    val outerR2 = radii.outerR2
    drawCircle(color = SignalPurple.copy(alpha = 0.1f), radius = outerR2 + 4f, center = center, style = Stroke(width = 0.5f))
    drawCircle(color = SignalElectric.copy(alpha = breathAlpha), radius = outerR2, center = center, style = Stroke(width = 2f, pathEffect = dashPathEffect))
    drawCircle(color = SignalSky.copy(alpha = 0.25f), radius = radii.outerR, center = center, style = Stroke(width = 1f))

    for (deg in 0 until 360 step 5) {
        val rad = Math.toRadians((deg - 90).toDouble())
        val px = center.x + outerR2 * cos(rad).toFloat()
        val py = center.y + outerR2 * sin(rad).toFloat()
        val (alpha, r) = when {
            deg % 30 == 0 -> 0.75f to 3f
            deg % 15 == 0 -> 0.5f to 2f
            else -> 0.25f to 1f
        }
        val tickColor = when {
            deg % 90 == 0 -> SignalPurple
            deg % 30 == 0 -> SignalSky
            else -> SignalElectric
        }
        drawCircle(color = tickColor.copy(alpha = alpha), radius = r, center = Offset(px, py))
    }
}

/** 绘制月份进度弧（当日所在月的已过进度）。 */
private fun DrawScope.drawMonthProgressArc(
    radii: OrbitalRadii,
    data: OrbitalCalendarData,
    todayDay: Int
) {
    val center = radii.center
    val outerR2 = radii.outerR2
    if (data.monthProgress > 0f && data.monthProgress < 1f) {
        val arcSweep = ((todayDay - 1).toFloat() / data.daysInMonth) * 360f + 360f / data.daysInMonth * 0.5f
        drawArc(color = SignalGreen.copy(alpha = 0.15f), startAngle = -90f, sweepAngle = arcSweep,
            useCenter = false, style = Stroke(width = 8f, cap = StrokeCap.Round),
            topLeft = Offset(center.x - outerR2, center.y - outerR2), size = Size(outerR2 * 2, outerR2 * 2))
        drawArc(brush = Brush.linearGradient(listOf(SignalGreen, SignalSky), start = Offset(center.x - outerR2, center.y), end = Offset(center.x + outerR2, center.y)),
            startAngle = -90f, sweepAngle = arcSweep,
            useCenter = false, style = Stroke(width = 3f, cap = StrokeCap.Round),
            topLeft = Offset(center.x - outerR2, center.y - outerR2), size = Size(outerR2 * 2, outerR2 * 2))
    } else if (data.monthProgress >= 1f) {
        drawCircle(color = SignalGreen.copy(alpha = 0.15f), radius = outerR2, center = center, style = Stroke(width = 8f))
        drawCircle(color = SignalGreen.copy(alpha = 0.7f), radius = outerR2, center = center, style = Stroke(width = 3f))
    }
}

/** 绘制中层 4 圈环（星期圈 / 主圈 / 内圈 / 最内装饰圈）。 */
private fun DrawScope.drawMiddleRings(radii: OrbitalRadii) {
    val center = radii.center
    drawCircle(color = SignalSky.copy(alpha = 0.45f), radius = radii.weekR, center = center, style = Stroke(width = 1.5f))
    drawCircle(color = SignalElectric.copy(alpha = 0.55f), radius = radii.mainR, center = center, style = Stroke(width = 2f))
    drawCircle(color = SignalElectric.copy(alpha = 0.3f), radius = radii.innerR, center = center, style = Stroke(width = 1f))
    drawCircle(color = SignalSky.copy(alpha = 0.2f), radius = radii.innerDecorR, center = center, style = Stroke(width = 0.5f))
}

/** 绘制内圈 12 条装饰线 + 端点圆点。 */
private fun DrawScope.drawInnerDecorLines(radii: OrbitalRadii) {
    val center = radii.center
    val innerDecorR = radii.innerDecorR
    val innerR = radii.innerR
    for (i in 0 until 12) {
        val a = (i.toFloat() / 12f) * 360f - 90f
        val r = Math.toRadians(a.toDouble())
        val dx = center.x + innerDecorR * cos(r).toFloat()
        val dy = center.y + innerDecorR * sin(r).toFloat()
        val dx2 = center.x + innerR * cos(r).toFloat()
        val dy2 = center.y + innerR * sin(r).toFloat()
        val decorColor = if (i % 3 == 0) SignalPurple else SignalSky
        drawLine(color = decorColor.copy(alpha = 0.25f), start = Offset(dx, dy), end = Offset(dx2, dy2), strokeWidth = 0.8f)
        drawCircle(color = decorColor.copy(alpha = 0.4f), radius = 2f, center = Offset(dx2, dy2))
    }
}

/** 绘制八卦文字（8 个方位）。 */
private fun DrawScope.drawBagua(radii: OrbitalRadii, baguaMeasures: List<TextLayoutResult>) {
    val center = radii.center
    val innerDecorR = radii.innerDecorR
    for (i in 0 until 8) {
        val a = i * 45f - 90f + 22.5f
        val r = Math.toRadians(a.toDouble())
        val bx = center.x + (innerDecorR - 8f) * cos(r).toFloat()
        val by = center.y + (innerDecorR - 8f) * sin(r).toFloat()
        val m = baguaMeasures[i]
        drawText(textLayoutResult = m, topLeft = Offset(bx - m.size.width / 2f, by - m.size.height / 2f))
    }
}

/** 绘制相邻事件日之间的连接弧（间隔 ≤3 天才连线）。 */
private fun DrawScope.drawEventArcs(
    radii: OrbitalRadii,
    data: OrbitalCalendarData,
    sortedSignalKeys: List<Int>
) {
    val center = radii.center
    val mainR = radii.mainR
    val eventDays = sortedSignalKeys
    for (idx in 0 until eventDays.size - 1) {
        val day1 = eventDays[idx]
        val day2 = eventDays[idx + 1]
        if (day2 - day1 <= 3) {
            val startAngleDeg = ((day1 - 1).toFloat() / data.daysInMonth) * 360f - 90f
            val endAngleDeg = ((day2 - 1).toFloat() / data.daysInMonth) * 360f - 90f
            val sweepAngle = endAngleDeg - startAngleDeg
            drawArc(color = SignalElectric.copy(alpha = 0.2f), startAngle = startAngleDeg, sweepAngle = sweepAngle,
                useCenter = false, style = Stroke(width = 1f, cap = StrokeCap.Round),
                topLeft = Offset(center.x - mainR, center.y - mainR), size = Size(mainR * 2, mainR * 2))
        }
    }
}

/** 绘制今日指示线 + 箭头 + 涟漪扩散（仅当前月调用）。 */
private fun DrawScope.drawTodayIndicator(
    radii: OrbitalRadii,
    data: OrbitalCalendarData,
    todayDay: Int,
    ripplePhase: Float
) {
    val center = radii.center
    val mainR = radii.mainR
    val outerR = radii.outerR
    val innerDecorR = radii.innerDecorR
    val todayAngle = ((todayDay - 1).toFloat() / data.daysInMonth) * 360f - 90f
    val todayRad = Math.toRadians(todayAngle.toDouble())
    drawLine(color = SignalGreen.copy(alpha = 0.65f),
        start = Offset(center.x + innerDecorR * cos(todayRad).toFloat(), center.y + innerDecorR * sin(todayRad).toFloat()),
        end = Offset(center.x + outerR * cos(todayRad).toFloat(), center.y + outerR * sin(todayRad).toFloat()),
        strokeWidth = 3f)
    val arrowR = outerR + 8f
    val ax = center.x + arrowR * cos(todayRad).toFloat()
    val ay = center.y + arrowR * sin(todayRad).toFloat()
    drawCircle(color = SignalGreen.copy(alpha = 0.85f), radius = 4f, center = Offset(ax, ay))

    val rippleR = mainR + (outerR - mainR) * ripplePhase
    val rippleAlpha = 0.5f * (1f - ripplePhase)
    val todayX = center.x + mainR * cos(todayRad).toFloat()
    val todayY = center.y + mainR * sin(todayRad).toFloat()
    drawCircle(color = SignalGreen.copy(alpha = rippleAlpha), radius = rippleR - mainR + 10f, center = Offset(todayX, todayY), style = Stroke(width = 1.5f))
}

/** 绘制扫描线 + 45° 拖尾。 */
private fun DrawScope.drawScanLine(radii: OrbitalRadii, scanAngle: Float) {
    val center = radii.center
    val outerR2 = radii.outerR2
    val scanRad = Math.toRadians(scanAngle.toDouble())
    drawLine(color = SignalSky.copy(alpha = 0.5f), start = center,
        end = Offset(center.x + outerR2 * cos(scanRad).toFloat(), center.y + outerR2 * sin(scanRad).toFloat()),
        strokeWidth = 1.5f)
    for (a in 0..30) {
        val frac = a / 30f
        val rad = Math.toRadians((scanAngle - 45f * frac).toDouble())
        val tColor = when {
            frac < 0.33f -> SignalSky
            frac < 0.66f -> SignalPurple
            else -> SignalElectric
        }
        drawLine(color = tColor.copy(alpha = 0.15f * (1 - frac)), start = center,
            end = Offset(center.x + outerR2 * cos(rad).toFloat(), center.y + outerR2 * sin(rad).toFloat()),
            strokeWidth = 1f)
    }
}

/** 绘制外圈 24 个旋转点（6 色循环）。 */
private fun DrawScope.drawRotatingDots(radii: OrbitalRadii, rotateAngle: Float) {
    val center = radii.center
    val outerR = radii.outerR
    for (i in 0 until 24) {
        val a = (i.toFloat() / 24f) * 360f + rotateAngle - 90f
        val r = Math.toRadians(a.toDouble())
        val rx = center.x + (outerR + 4f) * cos(r).toFloat()
        val ry = center.y + (outerR + 4f) * sin(r).toFloat()
        val rotColor = when (i % 6) {
            0 -> SignalElectric
            1 -> SignalSky
            2 -> SignalGreen
            3 -> SignalPurple
            4 -> SignalElectric
            else -> SignalSky
        }
        val alpha = if (i % 6 == 0) 0.55f else 0.25f
        drawCircle(color = rotColor.copy(alpha = alpha), radius = if (i % 6 == 0) 2f else 1f, center = Offset(rx, ry))
    }
}

/** 绘制 25 个粒子（带呼吸偏移）。 */
private fun DrawScope.drawParticles(
    radii: OrbitalRadii,
    particles: List<ParticleState>,
    particleAngle: Float,
    ripplePhase: Float
) {
    val center = radii.center
    val particleR = radii.particleR
    for (p in particles) {
        val pAngle = p.angle + particleAngle * p.speed
        val pRad = Math.toRadians(pAngle.toDouble())
        val breathOffset = 3f * sin((ripplePhase + p.breathPhase) * Math.PI).toFloat()
        val pR = particleR + p.radiusOffset + breathOffset
        val px = center.x + pR * cos(pRad).toFloat()
        val py = center.y + pR * sin(pRad).toFloat()
        drawCircle(color = p.color.copy(alpha = p.alpha), radius = p.size, center = Offset(px, py))
    }
}

/** 绘制主圈上的日期点（今日十字、事件日热点、普通日小圆点 + 星期点）。 */
private fun DrawScope.drawDayDots(
    radii: OrbitalRadii,
    data: OrbitalCalendarData,
    todayDay: Int,
    pulsePhase: Float
) {
    val center = radii.center
    val mainR = radii.mainR
    val weekR = radii.weekR
    for (day in 1..data.daysInMonth) {
        val angleDeg = ((day - 1).toFloat() / data.daysInMonth) * 360f - 90f
        val rad = Math.toRadians(angleDeg.toDouble())
        val x = center.x + mainR * cos(rad).toFloat()
        val y = center.y + mainR * sin(rad).toFloat()
        val isToday = data.isCurrentMonth && day == todayDay
        val signalCount = data.daySignalMap[day] ?: 0
        val dow = data.dayOfWeekMap[day] ?: 0
        val isWEnd = dow == 0 || dow == 6

        if (isToday) {
            val pulseR = 12f + 4f * sin(pulsePhase * Math.PI).toFloat()
            drawCircle(color = SignalGreen.copy(alpha = 0.3f), radius = pulseR, center = Offset(x, y))
            drawCircle(color = SignalGreen, radius = 10f, center = Offset(x, y))
            drawLine(color = Color.White, start = Offset(x - 5f, y), end = Offset(x + 5f, y), strokeWidth = 1.5f)
            drawLine(color = Color.White, start = Offset(x, y - 5f), end = Offset(x, y + 5f), strokeWidth = 1.5f)
        } else if (signalCount > 0) {
            val heatAlpha = (0.5f + 0.15f * minOf(signalCount, 4)).coerceAtMost(1f)
            drawCircle(color = SignalElectric.copy(alpha = heatAlpha * 0.4f), radius = 8f, center = Offset(x, y))
            drawCircle(color = SignalElectric.copy(alpha = heatAlpha), radius = 6.5f, center = Offset(x, y))
            for (s in 0 until minOf(signalCount, 3)) {
                val dotAngle = Math.toRadians((angleDeg + s * 20 - 10).toDouble())
                drawCircle(color = SignalAmber.copy(alpha = 0.9f), radius = 2f,
                    center = Offset(x + 11f * cos(dotAngle).toFloat(), y + 11f * sin(dotAngle).toFloat()))
            }
        } else {
            val dotColor = if (isWEnd) SignalCoral.copy(alpha = 0.65f) else SignalSky.copy(alpha = 0.45f)
            drawCircle(color = dotColor, radius = 3.5f, center = Offset(x, y))
        }

        val wdx = center.x + weekR * cos(rad).toFloat()
        val wdy = center.y + weekR * sin(rad).toFloat()
        val weekDotColor = if (isWEnd) SignalCoral.copy(alpha = 0.75f) else SignalSky.copy(alpha = 0.55f)
        drawCircle(color = weekDotColor, radius = 2f, center = Offset(wdx, wdy))
    }
}

/** 绘制日期数字文字（外圈 dateTextR 处）。 */
private fun DrawScope.drawDayTexts(
    radii: OrbitalRadii,
    data: OrbitalCalendarData,
    dayTextMeasures: List<Pair<Int, TextLayoutResult>>
) {
    val center = radii.center
    val dateTextR = radii.dateTextR
    for ((day, measured) in dayTextMeasures) {
        val angleDeg = ((day - 1).toFloat() / data.daysInMonth) * 360f - 90f
        val rad = Math.toRadians(angleDeg.toDouble())
        val tx = center.x + dateTextR * cos(rad).toFloat()
        val ty = center.y + dateTextR * sin(rad).toFloat()
        drawText(textLayoutResult = measured, topLeft = Offset(tx - measured.size.width / 2f, ty - measured.size.height / 2f))
    }
}

/** 绘制星期文字（外圈 weekTextR 处）。 */
private fun DrawScope.drawWeekTexts(
    radii: OrbitalRadii,
    data: OrbitalCalendarData,
    weekDayMeasures: List<Pair<Int, TextLayoutResult>>
) {
    val center = radii.center
    val weekTextR = radii.weekTextR
    for ((day, wMeasured) in weekDayMeasures) {
        val angleDeg = ((day - 1).toFloat() / data.daysInMonth) * 360f - 90f
        val rad = Math.toRadians(angleDeg.toDouble())
        val wx = center.x + weekTextR * cos(rad).toFloat()
        val wy = center.y + weekTextR * sin(rad).toFloat()
        drawText(textLayoutResult = wMeasured, topLeft = Offset(wx - wMeasured.size.width / 2f, wy - wMeasured.size.height / 2f))
    }
}

/** 绘制中心装饰圆 + 十字准星 + 中心点 + 中心日数字。 */
private fun DrawScope.drawCenterDecorations(
    radii: OrbitalRadii,
    crosshairAngle: Float,
    centerDayMeasured: TextLayoutResult
) {
    val center = radii.center
    drawCircle(color = SignalElectric.copy(alpha = 0.06f), radius = 28f, center = center)
    drawCircle(color = SignalSky.copy(alpha = 0.10f), radius = 24f, center = center)
    drawCircle(color = SignalGreen.copy(alpha = 0.05f), radius = 20f, center = center)
    drawCircle(color = SignalElectric.copy(alpha = 0.4f), radius = 28f, center = center, style = Stroke(width = 1f))
    drawCircle(color = SignalSky.copy(alpha = 0.25f), radius = 24f, center = center, style = Stroke(width = 0.8f))
    drawCircle(color = SignalGreen.copy(alpha = 0.15f), radius = 20f, center = center, style = Stroke(width = 0.5f))

    val crossRad = Math.toRadians(crosshairAngle.toDouble())
    val armLen = 14f
    for (i in 0 until 4) {
        val a = crossRad + Math.toRadians((i * 90.0))
        val endX = center.x + armLen * cos(a).toFloat()
        val endY = center.y + armLen * sin(a).toFloat()
        val lineColor = if (i % 2 == 0) SignalElectric.copy(alpha = 0.35f) else SignalSky.copy(alpha = 0.35f)
        drawLine(color = lineColor, start = center, end = Offset(endX, endY), strokeWidth = 1f)
        val diamondSize = 3f
        val dx1 = endX + diamondSize * cos(a + Math.toRadians(45.0)).toFloat()
        val dy1 = endY + diamondSize * sin(a + Math.toRadians(45.0)).toFloat()
        val dx2 = endX + diamondSize * cos(a - Math.toRadians(45.0)).toFloat()
        val dy2 = endY + diamondSize * sin(a - Math.toRadians(45.0)).toFloat()
        drawLine(color = lineColor, start = Offset(dx1, dy1), end = Offset(dx2, dy2), strokeWidth = 1f)
    }

    drawCircle(color = SignalElectric.copy(alpha = 0.6f), radius = 2.5f, center = center)
    drawCircle(color = Color.White, radius = 1.2f, center = center)

    drawText(textLayoutResult = centerDayMeasured,
        topLeft = Offset(center.x - centerDayMeasured.size.width / 2f, center.y - centerDayMeasured.size.height / 2f))
}
