package com.tang.prm.ui.animation.composites

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

enum class WaveType {
    Sine,
    Square,
    Sawtooth,
    Triangle,
    Noise
}

@Suppress("UNUSED_PARAMETER")
@Composable
fun WaveformMonitor(
    waveType: WaveType = WaveType.Sine,
    isRunning: Boolean = true,
    modifier: Modifier = Modifier,
    lineColor: Color = Color.Green,
    gridColor: Color = Color.Unspecified,
    centerLineColor: Color = Color.Unspecified,
    glowColor: Color = Color.Unspecified,
    glowAlpha: Float = 0.12f,
    lineAlpha: Float = 0.85f,
    label: String = ""
) {
    var wavePhase by remember { mutableStateOf(0f) }
    var lastFrameTime by remember { mutableStateOf(0L) }
    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (true) {
                withFrameNanos { nanoTime ->
                    if (lastFrameTime > 0L) {
                        val deltaSeconds = (nanoTime - lastFrameTime) / 1_000_000_000f
                        wavePhase += deltaSeconds * (2f * PI.toFloat() / 3f)
                    }
                    lastFrameTime = nanoTime
                }
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        if (!isRunning) return@Canvas

        val width = size.width
        val height = size.height
        val centerY = height / 2

        val resolvedGridColor = if (gridColor != Color.Unspecified) gridColor else Color(0xFF0F172A).copy(alpha = 0.06f)
        val resolvedCenterColor = if (centerLineColor != Color.Unspecified) centerLineColor else Color(0xFF0F172A).copy(alpha = 0.2f)

        for (i in 0..4) {
            val y = (height / 4) * i
            drawLine(resolvedGridColor, Offset(0f, y), Offset(width, y), 0.5f)
        }
        for (i in 0..10) {
            val x = (width / 10) * i
            drawLine(resolvedGridColor, Offset(x, 0f), Offset(x, height), 0.5f)
        }

        drawLine(
            resolvedCenterColor,
            Offset(0f, centerY),
            Offset(width, centerY),
            0.8f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f), 0f)
        )

        val path = Path()
        for (x in 0..width.toInt()) {
            val t = x / width
            val waveValue = when (waveType) {
                WaveType.Sine -> sin(t * 6f * PI.toFloat() + wavePhase)
                WaveType.Square -> if (sin(t * 6f * PI.toFloat() + wavePhase) > 0) 1f else -1f
                WaveType.Sawtooth -> 2f * ((t * 3f + wavePhase / (2f * PI.toFloat())) % 1f) - 1f
                WaveType.Triangle -> {
                    val tt = (t * 3f + wavePhase / (2f * PI.toFloat()))
                    4f * kotlin.math.abs(tt - kotlin.math.floor(tt + 0.5f)) - 1f
                }
                WaveType.Noise -> kotlin.random.Random.nextFloat() * 2f - 1f
            }
            val y = centerY + waveValue * (height * 0.3f)
            if (x == 0) path.moveTo(x.toFloat(), y)
            else path.lineTo(x.toFloat(), y)
        }

        val resolvedGlowColor = if (glowColor != Color.Unspecified) glowColor else lineColor
        drawPath(path, resolvedGlowColor.copy(alpha = glowAlpha), style = Stroke(width = 6f))
        drawPath(path, lineColor.copy(alpha = lineAlpha), style = Stroke(width = 2f))
    }
}
