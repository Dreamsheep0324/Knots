package com.tang.prm.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.animation.core.AnimationTokens

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

@Composable
internal fun TerminalWaveformMonitor(
    color: Color,
    wavePhase: Float,
    waveformType: String = "sine"
) {
    val waveformLabel = ContactListViewModel.WaveformTypes.find { it.first == waveformType }?.second ?: "正弦波"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(2.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerY = height / 2

            for (i in 0..4) {
                val y = (height / 4) * i
                drawLine(
                    color = color.copy(alpha = 0.06f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 0.5f
                )
            }
            for (i in 0..10) {
                val x = (width / 10) * i
                drawLine(
                    color = color.copy(alpha = 0.06f),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 0.5f
                )
            }

            drawLine(
                color = color.copy(alpha = 0.2f),
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = 0.8f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f), 0f)
            )

            val path1 = Path()
            for (x in 0..width.toInt()) {
                val t = x / width
                val y = centerY + computeWaveform(waveformType, t * 3f, wavePhase) * (height * 0.32f)
                if (x == 0) path1.moveTo(x.toFloat(), y)
                else path1.lineTo(x.toFloat(), y)
            }
            drawPath(
                path = path1,
                color = color.copy(alpha = AnimationTokens.Alpha.subtle),
                style = Stroke(width = 8f)
            )
            drawPath(
                path = path1,
                color = color.copy(alpha = 0.85f),
                style = Stroke(width = 2f)
            )

            val secondaryType = when (waveformType) {
                "sine" -> "cosine"
                "cosine" -> "sine"
                "square" -> "triangle"
                "sawtooth" -> "triangle"
                "triangle" -> "sawtooth"
                "pulse" -> "step"
                "noise" -> "damped"
                "heartbeat" -> "pulse"
                "exponential" -> "damped"
                "damped" -> "exponential"
                "step" -> "square"
                "compound" -> "noise"
                else -> "cosine"
            }
            val path2 = Path()
            for (x in 0..width.toInt()) {
                val t = x / width
                val y = centerY + computeWaveform(secondaryType, t * 5f, wavePhase * 1.5f) * (height * 0.14f)
                if (x == 0) path2.moveTo(x.toFloat(), y)
                else path2.lineTo(x.toFloat(), y)
            }
            drawPath(
                path = path2,
                color = color.copy(alpha = 0.3f),
                style = Stroke(width = 1f)
            )

            val path3 = Path()
            for (x in 0..width.toInt()) {
                val t = x / width
                val y = centerY + computeWaveform("noise", t * 12f, wavePhase * 2f) * (height * 0.04f)
                if (x == 0) path3.moveTo(x.toFloat(), y)
                else path3.lineTo(x.toFloat(), y)
            }
            drawPath(
                path = path3,
                color = color.copy(alpha = 0.18f),
                style = Stroke(width = 0.8f)
            )

            val scanX = ((wavePhase / (2 * 3.14159f)) % 1f) * width
            drawLine(
                color = color.copy(alpha = 0.25f),
                start = Offset(scanX, 0f),
                end = Offset(scanX, height),
                strokeWidth = 1f
            )

            val peakX = width * 0.25f
            val peakY = centerY + computeWaveform(waveformType, 0.25f * 3f, wavePhase) * (height * 0.32f)
            drawCircle(
                color = color.copy(alpha = 0.15f),
                radius = 6f,
                center = Offset(peakX, peakY)
            )
            drawCircle(
                color = color.copy(alpha = 0.7f),
                radius = 2.5f,
                center = Offset(peakX, peakY)
            )
        }

        Text(
            waveformLabel,
            fontFamily = FontFamily.Monospace,
            fontSize = 7.sp,
            color = color.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

@Composable
internal fun TerminalSystemParam(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(2.dp))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            color = TerminalTextMuted
        )
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

internal fun computeWaveform(type: String, t: Float, phase: Float): Float {
    val p = t * 2f * 3.14159f + phase
    return when (type) {
        "sine" -> sin(p)
        "cosine" -> cos(p)
        "square" -> {
            val x = sin(p)
            if (x >= 0f) 0.9f else -0.9f
        }
        "sawtooth" -> {
            val x = ((t + phase / (2f * 3.14159f)) % 1f + 1f) % 1f
            2f * x - 1f
        }
        "triangle" -> {
            val x = ((t + phase / (2f * 3.14159f)) % 1f + 1f) % 1f
            4f * abs(x - 0.5f) - 1f
        }
        "pulse" -> {
            val x = ((t * 3f + phase / (2f * 3.14159f)) % 1f + 1f) % 1f
            if (x < 0.08f) 1f else 0f
        }
        "noise" -> {
            sin(p * 7.3f) * 0.4f + sin(p * 13.7f) * 0.35f + sin(p * 23.1f) * 0.25f
        }
        "heartbeat" -> {
            val x = ((t * 1.5f + phase / (2f * 3.14159f)) % 1f + 1f) % 1f
            when {
                x < 0.06f -> sin(x / 0.06f * 3.14159f) * 0.25f
                x < 0.12f -> -sin((x - 0.06f) / 0.06f * 3.14159f) * 0.15f
                x < 0.28f -> sin((x - 0.12f) / 0.16f * 3.14159f) * 1f
                x < 0.38f -> -sin((x - 0.28f) / 0.1f * 3.14159f) * 0.6f
                x < 0.48f -> sin((x - 0.38f) / 0.1f * 3.14159f) * 0.3f
                else -> 0f
            }
        }
        "exponential" -> {
            val x = ((t * 2f + phase / (2f * 3.14159f)) % 1f + 1f) % 1f
            2f * exp(-x * 4f) - 1f
        }
        "damped" -> {
            sin(p * 2f) * exp(-((t * 2f + phase / (2f * 3.14159f)) % 1f + 1f) % 1f * 3.5f)
        }
        "step" -> {
            val x = ((t * 3f + phase / (2f * 3.14159f)) % 1f + 1f) % 1f
            when {
                x < 0.25f -> -0.75f
                x < 0.5f -> 0f
                x < 0.75f -> 0.5f
                else -> 1f
            }
        }
        "compound" -> {
            sin(p) * 0.4f + sin(p * 2.5f) * 0.3f + cos(p * 0.5f) * 0.3f
        }
        else -> sin(p)
    }
}
