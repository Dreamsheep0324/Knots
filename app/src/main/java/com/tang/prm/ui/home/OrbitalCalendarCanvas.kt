package com.tang.prm.ui.home

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
import androidx.compose.ui.graphics.drawscope.Stroke
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

@Composable
internal fun OrbitalCalendarCanvas(
    data: OrbitalCalendarData,
    todayDay: Int
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
        val colors = listOf(SignalElectric, SignalSky, SignalPurple, SignalGreen)
        (0 until 25).map {
            ParticleState(
                angle = Random.nextFloat() * 360f,
                radiusOffset = Random.nextFloat() * 6f - 3f,
                size = Random.nextFloat() * 2f + 1f,
                alpha = Random.nextFloat() * 0.4f + 0.2f,
                color = colors[Random.nextInt(colors.size)],
                speed = Random.nextFloat() * 0.3f + 0.85f,
                breathPhase = Random.nextFloat() * 2f
            )
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val weekLabels = remember { listOf("日", "一", "二", "三", "四", "五", "六") }
    val darkText = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
    val weekDayColor = SignalElectric.copy(alpha = 0.9f)
    val weekEndColor = SignalCoral.copy(alpha = 0.9f)

    val dayTextStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = darkText)
    val dayTodayStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
    val dayEventStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = SignalElectric.copy(alpha = 0.95f))
    val centerDayStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SignalElectric)
    val baguaStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = SignalPurple.copy(alpha = 0.6f))
    val weekLabelStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 6.sp, fontWeight = FontWeight.Bold, color = weekDayColor)
    val weekEndLabelStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 6.sp, fontWeight = FontWeight.Bold, color = weekEndColor)

    val dayTextMeasures = remember(data.daysInMonth, data.isCurrentMonth, todayDay, data.daySignalMap) {
        (1..data.daysInMonth).map { day ->
            val isToday = data.isCurrentMonth && day == todayDay
            val signalCount = data.daySignalMap[day] ?: 0
            val style = when { isToday -> dayTodayStyle; signalCount > 0 -> dayEventStyle; else -> dayTextStyle }
            day to textMeasurer.measure(day.toString(), style)
        }
    }

    val weekDayMeasures = remember(data.daysInMonth, data.dayOfWeekMap) {
        (1..data.daysInMonth).map { day ->
            val dow = data.dayOfWeekMap[day] ?: 0
            val isWEnd = dow == 0 || dow == 6
            val style = if (isWEnd) weekEndLabelStyle else weekLabelStyle
            day to textMeasurer.measure(weekLabels[dow], style)
        }
    }

    val baguaMeasures = remember { bagua.map { textMeasurer.measure(it, baguaStyle) } }
    val centerDayMeasured = remember(data.isCurrentMonth, todayDay) {
        textMeasurer.measure(if (data.isCurrentMonth) todayDay.toString() else "1", centerDayStyle)
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(242.dp)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val minDim = minOf(size.width, size.height) * 1.05f
        val innerDecorR = minDim * 0.10f
        val innerR = minDim * 0.15f
        val weekR = minDim * 0.21f
        val mainR = minDim * 0.26f
        val dateTextR = minDim * 0.31f
        val weekTextR = minDim * 0.35f
        val outerR = minDim * 0.41f
        val outerR2 = minDim * 0.44f
        val particleR = minDim * 0.48f

        val gridColors = listOf(
            SignalElectric.copy(alpha = 0.04f),
            SignalSky.copy(alpha = 0.03f),
            SignalElectric.copy(alpha = 0.04f),
            SignalSky.copy(alpha = 0.03f),
            SignalElectric.copy(alpha = 0.03f),
            SignalSky.copy(alpha = 0.03f),
            SignalElectric.copy(alpha = 0.04f)
        )
        for ((idx, r) in listOf(innerR, weekR, mainR, dateTextR, weekTextR, outerR, outerR2).withIndex()) {
            drawCircle(color = gridColors[idx], radius = r, center = center, style = Stroke(width = 0.5f))
        }
        val rayColors = listOf(SignalElectric, SignalSky, SignalGreen, SignalPurple, SignalCoral, SignalElectric, SignalSky, SignalGreen)
        for (i in 0 until 8) {
            val a = Math.toRadians(i * 45.0 - 90.0)
            drawLine(
                color = rayColors[i].copy(alpha = 0.04f),
                start = Offset(center.x + innerDecorR * cos(a).toFloat(), center.y + innerDecorR * sin(a).toFloat()),
                end = Offset(center.x + outerR2 * cos(a).toFloat(), center.y + outerR2 * sin(a).toFloat()),
                strokeWidth = 0.5f
            )
        }

        drawCircle(color = SignalPurple.copy(alpha = 0.1f), radius = outerR2 + 4f, center = center, style = Stroke(width = 0.5f))
        drawCircle(color = SignalElectric.copy(alpha = breathAlpha), radius = outerR2, center = center, style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f)))
        drawCircle(color = SignalSky.copy(alpha = 0.25f), radius = outerR, center = center, style = Stroke(width = 1f))

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

        drawCircle(color = SignalSky.copy(alpha = 0.45f), radius = weekR, center = center, style = Stroke(width = 1.5f))

        drawCircle(color = SignalElectric.copy(alpha = 0.55f), radius = mainR, center = center, style = Stroke(width = 2f))

        drawCircle(color = SignalElectric.copy(alpha = 0.3f), radius = innerR, center = center, style = Stroke(width = 1f))
        drawCircle(color = SignalSky.copy(alpha = 0.2f), radius = innerDecorR, center = center, style = Stroke(width = 0.5f))

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

        for (i in 0 until 8) {
            val a = i * 45f - 90f + 22.5f
            val r = Math.toRadians(a.toDouble())
            val bx = center.x + (innerDecorR - 8f) * cos(r).toFloat()
            val by = center.y + (innerDecorR - 8f) * sin(r).toFloat()
            val m = baguaMeasures[i]
            drawText(textLayoutResult = m, topLeft = Offset(bx - m.size.width / 2f, by - m.size.height / 2f))
        }

        val eventDays = data.daySignalMap.keys.sorted()
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

        if (data.isCurrentMonth) {
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

            val rippleProgress = ripplePhase
            val rippleR = mainR + (outerR - mainR) * rippleProgress
            val rippleAlpha = 0.5f * (1f - rippleProgress)
            val todayX = center.x + mainR * cos(todayRad).toFloat()
            val todayY = center.y + mainR * sin(todayRad).toFloat()
            drawCircle(color = SignalGreen.copy(alpha = rippleAlpha), radius = rippleR - mainR + 10f, center = Offset(todayX, todayY), style = Stroke(width = 1.5f))
        }

        val scanRad = Math.toRadians(rotations.scanAngle.toDouble())
        drawLine(color = SignalSky.copy(alpha = 0.5f), start = center,
            end = Offset(center.x + outerR2 * cos(scanRad).toFloat(), center.y + outerR2 * sin(scanRad).toFloat()),
            strokeWidth = 1.5f)
        for (a in 0..30) {
            val frac = a / 30f
            val rad = Math.toRadians((rotations.scanAngle - 45f * frac).toDouble())
            val tColor = when {
                frac < 0.33f -> SignalSky
                frac < 0.66f -> SignalPurple
                else -> SignalElectric
            }
            drawLine(color = tColor.copy(alpha = 0.15f * (1 - frac)), start = center,
                end = Offset(center.x + outerR2 * cos(rad).toFloat(), center.y + outerR2 * sin(rad).toFloat()),
                strokeWidth = 1f)
        }

        for (i in 0 until 24) {
            val a = (i.toFloat() / 24f) * 360f + rotations.rotateAngle - 90f
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

        for (p in particles) {
            val pAngle = p.angle + rotations.particleAngle * p.speed
            val pRad = Math.toRadians(pAngle.toDouble())
            val breathOffset = 3f * sin((ripplePhase + p.breathPhase) * Math.PI).toFloat()
            val pR = particleR + p.radiusOffset + breathOffset
            val px = center.x + pR * cos(pRad).toFloat()
            val py = center.y + pR * sin(pRad).toFloat()
            drawCircle(color = p.color.copy(alpha = p.alpha), radius = p.size, center = Offset(px, py))
        }

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

        for ((day, measured) in dayTextMeasures) {
            val angleDeg = ((day - 1).toFloat() / data.daysInMonth) * 360f - 90f
            val rad = Math.toRadians(angleDeg.toDouble())
            val tx = center.x + dateTextR * cos(rad).toFloat()
            val ty = center.y + dateTextR * sin(rad).toFloat()
            drawText(textLayoutResult = measured, topLeft = Offset(tx - measured.size.width / 2f, ty - measured.size.height / 2f))
        }

        for ((day, wMeasured) in weekDayMeasures) {
            val angleDeg = ((day - 1).toFloat() / data.daysInMonth) * 360f - 90f
            val rad = Math.toRadians(angleDeg.toDouble())
            val wx = center.x + weekTextR * cos(rad).toFloat()
            val wy = center.y + weekTextR * sin(rad).toFloat()
            drawText(textLayoutResult = wMeasured, topLeft = Offset(wx - wMeasured.size.width / 2f, wy - wMeasured.size.height / 2f))
        }

        drawCircle(color = SignalElectric.copy(alpha = 0.06f), radius = 28f, center = center)
        drawCircle(color = SignalSky.copy(alpha = 0.10f), radius = 24f, center = center)
        drawCircle(color = SignalGreen.copy(alpha = 0.05f), radius = 20f, center = center)
        drawCircle(color = SignalElectric.copy(alpha = 0.4f), radius = 28f, center = center, style = Stroke(width = 1f))
        drawCircle(color = SignalSky.copy(alpha = 0.25f), radius = 24f, center = center, style = Stroke(width = 0.8f))
        drawCircle(color = SignalGreen.copy(alpha = 0.15f), radius = 20f, center = center, style = Stroke(width = 0.5f))

        val crossRad = Math.toRadians(rotations.crosshairAngle.toDouble())
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
}
