package com.tang.prm.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.animation.core.AnimationTokens

@Composable
fun HoloDataCell(
    label: String,
    value: String,
    valueColor: Color,
    tintColor: Color,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(tintColor.copy(alpha = 0.03f), RoundedCornerShape(3.dp))
            .border(1.dp, tintColor.copy(alpha = AnimationTokens.Alpha.faint), RoundedCornerShape(3.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            color = labelColor,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = valueColor,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun HoloScanLineTexture(tintColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineColor = tintColor.copy(alpha = 0.018f)
        for (y in 0 until size.height.toInt() step 4) {
            drawLine(
                color = lineColor,
                start = Offset(0f, y.toFloat()),
                end = Offset(size.width, y.toFloat()),
                strokeWidth = 1f
            )
        }
        drawRect(
            color = tintColor.copy(alpha = 0.03f),
            topLeft = Offset.Zero,
            size = Size(size.width, size.height * 0.2f)
        )
        drawRect(
            color = tintColor.copy(alpha = 0.02f),
            topLeft = Offset(0f, size.height * 0.8f),
            size = Size(size.width, size.height * 0.2f)
        )
    }
}

@Composable
fun HoloScanLine(scanLineOffset: Float, tintColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineY = size.height * scanLineOffset
        drawLine(
            color = tintColor.copy(alpha = 0.1f),
            start = Offset(0f, lineY),
            end = Offset(size.width, lineY),
            strokeWidth = 2f
        )
    }
}

@Composable
fun HoloCornerMarks(tintColor: Color) {
    val length = 12.dp
    val thickness = 1.5.dp
    val color = tintColor.copy(alpha = 0.2f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val len = length.toPx()
        val thick = thickness.toPx()
        val w = size.width
        val h = size.height
        val o = 4.dp.toPx()

        drawLine(color, Offset(o, o), Offset(o + len, o), thick)
        drawLine(color, Offset(o, o), Offset(o, o + len), thick)
        drawLine(color, Offset(w - o, o), Offset(w - o - len, o), thick)
        drawLine(color, Offset(w - o, o), Offset(w - o, o + len), thick)
        drawLine(color, Offset(o, h - o), Offset(o + len, h - o), thick)
        drawLine(color, Offset(o, h - o), Offset(o, h - o - len), thick)
        drawLine(color, Offset(w - o, h - o), Offset(w - o - len, h - o), thick)
        drawLine(color, Offset(w - o, h - o), Offset(w - o, h - o - len), thick)
    }
}

@Composable
fun CardScanLineOverlay(scanLineOffset: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineY = size.height * scanLineOffset
        drawLine(
            color = Color(0xFF00FF00).copy(alpha = 0.06f),
            start = Offset(0f, lineY),
            end = Offset(size.width, lineY),
            strokeWidth = 2f
        )

        for (y in 0 until size.height.toInt() step 4) {
            drawLine(
                color = Color.Black.copy(alpha = 0.02f),
                start = Offset(0f, y.toFloat()),
                end = Offset(size.width, y.toFloat()),
                strokeWidth = 1f
            )
        }
    }
}

@Composable
fun CardCornerBrackets(color: Color) {
    val length = 16.dp
    val thickness = 2.dp

    Canvas(modifier = Modifier.fillMaxSize()) {
        val len = length.toPx()
        val thick = thickness.toPx()
        val w = size.width
        val h = size.height

        drawLine(color, Offset(0f, 0f), Offset(len, 0f), thick)
        drawLine(color, Offset(0f, 0f), Offset(0f, len), thick)
        drawLine(color, Offset(w, 0f), Offset(w - len, 0f), thick)
        drawLine(color, Offset(w, 0f), Offset(w, len), thick)
        drawLine(color, Offset(0f, h), Offset(len, h), thick)
        drawLine(color, Offset(0f, h), Offset(0f, h - len), thick)
        drawLine(color, Offset(w, h), Offset(w - len, h), thick)
        drawLine(color, Offset(w, h), Offset(w, h - len), thick)
    }
}
