package com.tang.prm.ui.home

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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.Event
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.theme.GridLine
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalCoral
import com.tang.prm.ui.theme.SignalElectric
import com.tang.prm.ui.theme.SignalGreen
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.SignalSky
import com.tang.prm.ui.theme.TextGray
import com.tang.prm.util.DateUtils
import com.tang.prm.ui.animation.core.AnimationTokens
import com.tang.prm.ui.animation.core.rememberPausableInfiniteFloatLoop
import com.tang.prm.ui.animation.primitives.rememberContinuousRotation
import com.tang.prm.ui.animation.primitives.rememberFadePulse
import com.tang.prm.ui.theme.Dimens
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

private val bagua = listOf("坎","艮","震","巽","离","坤","兑","乾")

@Composable
internal fun OrbitalCalendar(
    anniversaries: List<Anniversary>,
    events: List<Event>
) {
    val todayCal = Calendar.getInstance()
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayYear = todayCal.get(Calendar.YEAR)

    var displayMonth by remember { mutableStateOf(todayMonth) }
    var displayYear by remember { mutableStateOf(todayYear) }

    val displayCal = remember(displayYear, displayMonth) {
        Calendar.getInstance().apply { set(displayYear, displayMonth, 1) }
    }
    val daysInMonth = displayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val isCurrentMonth = displayYear == todayYear && displayMonth == todayMonth

    val monthProgress = if (isCurrentMonth) todayDay.toFloat() / daysInMonth else 0f

    val daySignalMap = remember(events, anniversaries, displayYear, displayMonth) {
        buildMap<Int, Int> {
            events.forEach { event ->
                val cal = Calendar.getInstance().apply { timeInMillis = event.time }
                if (cal.get(Calendar.YEAR) == displayYear && cal.get(Calendar.MONTH) == displayMonth) {
                    val day = cal.get(Calendar.DAY_OF_MONTH)
                    this[day] = (this[day] ?: 0) + 1
                }
            }
            anniversaries.forEach { ann ->
                val cal = Calendar.getInstance().apply { timeInMillis = ann.date }
                if (cal.get(Calendar.YEAR) == displayYear || ann.isRepeat) {
                    if (cal.get(Calendar.MONTH) == displayMonth) {
                        val day = cal.get(Calendar.DAY_OF_MONTH)
                        this[day] = (this[day] ?: 0) + 1
                    }
                }
            }
        }
    }

    val dayOfWeekMap = remember(displayYear, displayMonth, daysInMonth) {
        (1..daysInMonth).associateWith { day ->
            val cal = Calendar.getInstance().apply { set(displayYear, displayMonth, day) }
            cal.get(Calendar.DAY_OF_WEEK) - 1
        }
    }

    val todayEvents = remember(events, anniversaries) {
        val evts = events.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.time }
            cal.get(Calendar.YEAR) == todayYear && cal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
        }
        val anns = anniversaries.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.date }
            cal.get(Calendar.MONTH) == todayMonth && cal.get(Calendar.DAY_OF_MONTH) == todayDay
        }
        evts.map { it.title.ifBlank { it.type } } + anns.map { it.name }
    }

    val upcomingEvents = remember(events) {
        val now = System.currentTimeMillis()
        val threeDaysLater = now + 3 * 24 * 60 * 60 * 1000L
        events.filter { it.time in now..threeDaysLater }
            .sortedBy { it.time }
            .take(3)
            .map { it.title.ifBlank { it.type } to DateUtils.formatMonthDay(it.time) }
    }

    val nextEventCountdown = remember(events) {
        val now = System.currentTimeMillis()
        events.filter { it.time > now }
            .minByOrNull { it.time }
            ?.let {
                val diffMs = it.time - now
                val days = (diffMs / (24 * 60 * 60 * 1000)).toInt()
                val hours = ((diffMs % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000)).toInt()
                it.title.ifBlank { it.type } to (days to hours)
            }
    }

    val pulsePhase by rememberFadePulse(cycleDuration = 2000)
    val scanAngle by rememberContinuousRotation(cycleDuration = 10000)
    val rotateAngle by rememberContinuousRotation(cycleDuration = 60000)

    val textMeasurer = rememberTextMeasurer()
    val weekLabels = remember { listOf("日","一","二","三","四","五","六") }
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

    val dayTextMeasures = remember(daysInMonth, isCurrentMonth, todayDay, daySignalMap) {
        (1..daysInMonth).map { day ->
            val isToday = isCurrentMonth && day == todayDay
            val signalCount = daySignalMap[day] ?: 0
            val style = when { isToday -> dayTodayStyle; signalCount > 0 -> dayEventStyle; else -> dayTextStyle }
            day to textMeasurer.measure(day.toString(), style)
        }
    }

    val weekDayMeasures = remember(daysInMonth, dayOfWeekMap) {
        (1..daysInMonth).map { day ->
            val dow = dayOfWeekMap[day] ?: 0
            val isWEnd = dow == 0 || dow == 6
            val style = if (isWEnd) weekEndLabelStyle else weekLabelStyle
            day to textMeasurer.measure(weekLabels[dow], style)
        }
    }

    val baguaMeasures = remember { bagua.map { textMeasurer.measure(it, baguaStyle) } }
    val centerDayMeasured = remember(isCurrentMonth, todayDay) {
        textMeasurer.measure(if (isCurrentMonth) todayDay.toString() else "1", centerDayStyle)
    }

    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingCard)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("轨道罗盘", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(8.dp))
                    if (isCurrentMonth && todayEvents.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier
                                .background(SignalElectric.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalElectric))
                            Text("${todayEvents.size}项今日", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = SignalElectric)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        val c = Calendar.getInstance().apply { set(displayYear, displayMonth, 1); add(Calendar.MONTH, -1) }
                        displayMonth = c.get(Calendar.MONTH); displayYear = c.get(Calendar.YEAR)
                    }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "上月", tint = TextGray, modifier = Modifier.size(16.dp))
                    }
                    Text("${displayYear}.${String.format("%02d", displayMonth + 1)}",
                        fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = {
                        val c = Calendar.getInstance().apply { set(displayYear, displayMonth, 1); add(Calendar.MONTH, 1) }
                        displayMonth = c.get(Calendar.MONTH); displayYear = c.get(Calendar.YEAR)
                    }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "下月", tint = TextGray, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Canvas(modifier = Modifier.fillMaxWidth().height(242.dp)) {
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

                drawCircle(color = SignalElectric.copy(alpha = 0.45f), radius = outerR2, center = center, style = Stroke(width = 2f))
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

                if (monthProgress > 0f && monthProgress < 1f) {
                    drawArc(color = SignalGreen.copy(alpha = 0.7f), startAngle = -90f, sweepAngle = 360f * monthProgress,
                        useCenter = false, style = Stroke(width = 4f, cap = StrokeCap.Round),
                        topLeft = Offset(center.x - outerR2, center.y - outerR2), size = Size(outerR2 * 2, outerR2 * 2))
                } else if (monthProgress >= 1f) {
                    drawCircle(color = SignalGreen.copy(alpha = 0.7f), radius = outerR2, center = center, style = Stroke(width = 4f))
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

                if (isCurrentMonth) {
                    val todayAngle = ((todayDay - 1).toFloat() / daysInMonth) * 360f - 90f
                    val todayRad = Math.toRadians(todayAngle.toDouble())
                    drawLine(color = SignalGreen.copy(alpha = 0.65f),
                        start = Offset(center.x + innerDecorR * cos(todayRad).toFloat(), center.y + innerDecorR * sin(todayRad).toFloat()),
                        end = Offset(center.x + outerR * cos(todayRad).toFloat(), center.y + outerR * sin(todayRad).toFloat()),
                        strokeWidth = 3f)
                    val arrowR = outerR + 8f
                    val ax = center.x + arrowR * cos(todayRad).toFloat()
                    val ay = center.y + arrowR * sin(todayRad).toFloat()
                    drawCircle(color = SignalGreen.copy(alpha = 0.85f), radius = 4f, center = Offset(ax, ay))
                }

                val scanRad = Math.toRadians(scanAngle.toDouble())
                drawLine(color = SignalSky.copy(alpha = 0.4f), start = center,
                    end = Offset(center.x + outerR2 * cos(scanRad).toFloat(), center.y + outerR2 * sin(scanRad).toFloat()),
                    strokeWidth = 1f)
                for (a in 0..20) {
                    val frac = a / 20f
                    val rad = Math.toRadians((scanAngle - 30f * frac).toDouble())
                    val tailColor = if (a < 10) SignalSky else SignalElectric
                    drawLine(color = tailColor.copy(alpha = 0.12f * (1 - frac)), start = center,
                        end = Offset(center.x + outerR2 * cos(rad).toFloat(), center.y + outerR2 * sin(rad).toFloat()),
                        strokeWidth = 1f)
                }

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

                for (day in 1..daysInMonth) {
                    val angleDeg = ((day - 1).toFloat() / daysInMonth) * 360f - 90f
                    val rad = Math.toRadians(angleDeg.toDouble())
                    val x = center.x + mainR * cos(rad).toFloat()
                    val y = center.y + mainR * sin(rad).toFloat()
                    val isToday = isCurrentMonth && day == todayDay
                    val signalCount = daySignalMap[day] ?: 0
                    val dow = dayOfWeekMap[day] ?: 0
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
                    val angleDeg = ((day - 1).toFloat() / daysInMonth) * 360f - 90f
                    val rad = Math.toRadians(angleDeg.toDouble())
                    val tx = center.x + dateTextR * cos(rad).toFloat()
                    val ty = center.y + dateTextR * sin(rad).toFloat()
                    drawText(textLayoutResult = measured, topLeft = Offset(tx - measured.size.width / 2f, ty - measured.size.height / 2f))
                }

                for ((day, wMeasured) in weekDayMeasures) {
                    val angleDeg = ((day - 1).toFloat() / daysInMonth) * 360f - 90f
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

                drawLine(color = SignalElectric.copy(alpha = 0.3f), start = Offset(center.x - 12f, center.y), end = Offset(center.x + 12f, center.y), strokeWidth = 1f)
                drawLine(color = SignalSky.copy(alpha = 0.3f), start = Offset(center.x, center.y - 12f), end = Offset(center.x, center.y + 12f), strokeWidth = 1f)
                drawCircle(color = SignalElectric.copy(alpha = 0.6f), radius = 2.5f, center = center)
                drawCircle(color = Color.White, radius = 1.2f, center = center)

                drawText(textLayoutResult = centerDayMeasured,
                    topLeft = Offset(center.x - centerDayMeasured.size.width / 2f, center.y - centerDayMeasured.size.height / 2f))
            }

            if (isCurrentMonth && todayEvents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth().height(1.dp).background(GridLine.copy(alpha = AnimationTokens.Alpha.half))) {}
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalElectric))
                    Text("今日", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AnimationTokens.Alpha.half))
                }
                Spacer(modifier = Modifier.height(4.dp))
                todayEvents.take(3).forEach { title ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalElectric.copy(alpha = 0.5f)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    }
                }
            }

            if (upcomingEvents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalAmber))
                    Text("即将到来", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                }
                Spacer(modifier = Modifier.height(4.dp))
                upcomingEvents.forEach { (title, date) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalAmber.copy(alpha = 0.5f)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Text(date, fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                }
            }

            if (nextEventCountdown != null) {
                val (title, daysHours) = nextEventCountdown
                val (days, hours) = daysHours
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalPurple))
                    Text("NEXT", fontFamily = FontFamily.Monospace, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SignalElectric)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalPurple.copy(alpha = AnimationTokens.Alpha.half)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text(if (days > 0) "${days}D ${hours}H" else "${hours}H",
                        fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SignalElectric)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(SignalElectric))
                    Text("今天", fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(SignalElectric.copy(alpha = 0.6f)))
                    Text("事件", fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(SignalCoral.copy(alpha = 0.6f)))
                    Text("周末", fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(SignalPurple.copy(alpha = 0.6f)))
                    Text("古历", fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
                Spacer(modifier = Modifier.width(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(SignalSky.copy(alpha = AnimationTokens.Alpha.visible)))
                    Text("生肖", fontFamily = FontFamily.Monospace, fontSize = 7.sp, color = SignalSky.copy(alpha = 0.7f))
                }
            }
        }
    }
}
