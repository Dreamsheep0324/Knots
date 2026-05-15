package com.tang.prm.ui.home

import com.tang.prm.ui.animation.primitives.rememberBreathingPulse
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CardRarity
import com.tang.prm.domain.model.getCardRarity
import com.tang.prm.ui.theme.*


@Composable
internal fun TerminalStatsPanel(
    circles: List<HologramCircle>,
    contacts: List<Contact>
) {
    val totalContacts = contacts.size
    val totalCircles = circles.size
    val avgIntimacy = if (contacts.isEmpty()) 0 else contacts.map { it.intimacyScore }.average().toInt()
    val urCount = contacts.count { getCardRarity(it.intimacyScore) == CardRarity.UR }
    val ssrCount = contacts.count { getCardRarity(it.intimacyScore) == CardRarity.SSR }
    val srCount = contacts.count { getCardRarity(it.intimacyScore) == CardRarity.SR }
    val rCount = contacts.count { getCardRarity(it.intimacyScore) == CardRarity.R }
    val nCount = contacts.count { getCardRarity(it.intimacyScore) == CardRarity.N }
    val highIntimacy = contacts.count { it.intimacyScore >= 70 }

    val panelColor = SignalPurple
    val statusAlpha by rememberBreathingPulse(
        minAlpha = 0.4f, maxAlpha = 1f, cycleDuration = 1200
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(2.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, panelColor.copy(alpha = 0.5f)),
            shadowElevation = 2.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "[SYS_STAT]",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = panelColor.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(panelColor.copy(alpha = statusAlpha))
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            "RUN",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = panelColor
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(2.dp))
                            .border(1.dp, panelColor.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height
                            val labelAreaHeight = 18f
                            val chartHeight = height - labelAreaHeight

                            val gridColor = panelColor.copy(alpha = 0.06f)
                            for (i in 0..4) {
                                val y = (chartHeight / 4) * i
                                drawLine(gridColor, Offset(0f, y), Offset(width, y), 0.5f)
                            }
                            for (i in 0..10) {
                                val x = (width / 10) * i
                                drawLine(gridColor, Offset(x, 0f), Offset(x, chartHeight), 0.5f)
                            }

                            val dataPoints = listOf(
                                nCount.toFloat(),
                                rCount.toFloat(),
                                srCount.toFloat(),
                                ssrCount.toFloat(),
                                urCount.toFloat()
                            )
                            val maxValue = dataPoints.maxOrNull()?.coerceAtLeast(1f) ?: 1f
                            val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)

                            val points = dataPoints.mapIndexed { index, value ->
                                Offset(
                                    x = index * stepX.toFloat(),
                                    y = chartHeight - (value / maxValue) * chartHeight * 0.85f - 4f
                                )
                            }

                            if (points.size >= 2) {
                                val linePath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(points.first().x, points.first().y)
                                    for (i in 1 until points.size) {
                                        val prev = points[i - 1]
                                        val curr = points[i]
                                        val cx1 = prev.x + (curr.x - prev.x) * 0.5f
                                        val cx2 = prev.x + (curr.x - prev.x) * 0.5f
                                        cubicTo(cx1, prev.y, cx2, curr.y, curr.x, curr.y)
                                    }
                                }
                                drawPath(
                                    path = linePath,
                                    color = panelColor,
                                    style = Stroke(width = 1.5f)
                                )
                            }

                            points.forEach { point ->
                                drawCircle(panelColor, radius = 3f, center = point)
                            }

                            val labels = listOf("N", "R", "SR", "SSR", "UR")
                            labels.forEachIndexed { index, label ->
                                val x = index * stepX.toFloat()
                                drawContext.canvas.nativeCanvas.drawText(
                                    label,
                                    x,
                                    height - 2f,
                                    android.graphics.Paint().apply {
                                        color = android.graphics.Color.parseColor("#64748B")
                                        textSize = 20f
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        typeface = android.graphics.Typeface.MONOSPACE
                                    }
                                )
                            }
                        }
                    }

                    StatsPanelCornerBrackets(panelColor.copy(alpha = 0.4f))
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TerminalStatCard(
                index = "01",
                label = "总联系人",
                value = String.format("%03d", totalContacts),
                accentColor = SignalPurple,
                modifier = Modifier.weight(1f)
            )
            TerminalStatCard(
                index = "02",
                label = "平均亲密度",
                value = String.format("%02d%%", avgIntimacy),
                accentColor = SignalGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TerminalStatCard(
                index = "03",
                label = "圈子数量",
                value = String.format("%02d", totalCircles),
                accentColor = SignalGold,
                modifier = Modifier.weight(1f)
            )
            TerminalStatCard(
                index = "04",
                label = "高亲密度",
                value = String.format("%02d", highIntimacy),
                accentColor = CardRarity.UR.color,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TerminalStatCard(
    index: String,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val statusAlpha by rememberBreathingPulse(
        minAlpha = 0.4f, maxAlpha = 1f, cycleDuration = 1200
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(2.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "[STAT_$index]",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            color = TerminalTextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = statusAlpha))
                            )
                            Text(
                                "OK",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 8.sp,
                                color = accentColor
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        label,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = TerminalTextDim
                    )

                    Spacer(Modifier.height(2.dp))

                    Text(
                        value,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }

            MiniCardCornerBrackets(accentColor.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun StatsPanelCornerBrackets(color: Color) {
    val length = 10.dp
    val thickness = 1.5f
    Canvas(modifier = Modifier.fillMaxSize()) {
        val len = length.toPx()
        val w = size.width
        val h = size.height
        drawLine(color, Offset(0f, 0f), Offset(len, 0f), thickness)
        drawLine(color, Offset(0f, 0f), Offset(0f, len), thickness)
        drawLine(color, Offset(w, 0f), Offset(w - len, 0f), thickness)
        drawLine(color, Offset(w, 0f), Offset(w, len), thickness)
        drawLine(color, Offset(0f, h), Offset(len, h), thickness)
        drawLine(color, Offset(0f, h), Offset(0f, h - len), thickness)
        drawLine(color, Offset(w, h), Offset(w - len, h), thickness)
        drawLine(color, Offset(w, h), Offset(w, h - len), thickness)
    }
}
