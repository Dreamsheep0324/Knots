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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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

/**
 * 在 [DrawScope] 中绘制一条水平扫描线，位于高度比例 [scanLineOffset] 处。
 *
 * 抽取自 [HoloScanLine] 与 [CardScanLineOverlay] 的公共绘制逻辑，
 * 两者仅在 [color]、[alpha] 与 [strokeWidth] 上有差异。
 */
private fun DrawScope.drawScanLine(
    scanLineOffset: Float,
    color: Color,
    alpha: Float,
    strokeWidth: Float = 2f
) {
    val lineY = size.height * scanLineOffset
    drawLine(
        color = color.copy(alpha = alpha),
        start = Offset(0f, lineY),
        end = Offset(size.width, lineY),
        strokeWidth = strokeWidth
    )
}

/**
 * 在 [DrawScope] 中绘制水平纹理线（每隔 [step] 像素一条）。
 *
 * 抽取自 [HoloScanLineTexture] 与 [CardScanLineOverlay] 的公共绘制逻辑，
 * 两者仅在 [color]、[alpha] 上有差异。
 */
private fun DrawScope.drawHorizontalLinesTexture(
    color: Color,
    alpha: Float,
    step: Int = 4,
    strokeWidth: Float = 1f
) {
    val lineColor = color.copy(alpha = alpha)
    for (y in 0 until size.height.toInt() step step) {
        drawLine(
            color = lineColor,
            start = Offset(0f, y.toFloat()),
            end = Offset(size.width, y.toFloat()),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
fun HoloScanLineTexture(tintColor: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawHorizontalLinesTexture(tintColor, alpha = 0.018f)
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
        drawScanLine(scanLineOffset, tintColor, alpha = 0.1f)
    }
}

@Composable
fun HoloCornerMarks(tintColor: Color) {
    CornerMarks(
        length = 12.dp,
        thickness = 1.5.dp,
        offset = 4.dp,
        color = tintColor.copy(alpha = 0.2f)
    )
}

/**
 * 卡片扫描线叠加层。
 *
 * 在 [HoloScanLine] 的扫描线逻辑上额外叠加水平纹理线；配色（绿色扫描线 + 黑色纹理）
 * 与 [HoloScanLineTexture]（主题色纹理 + 顶/底渐变带）不同，故保留独立 Composable。
 */
@Composable
fun CardScanLineOverlay(scanLineOffset: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawScanLine(scanLineOffset, Color(0xFF00FF00), alpha = 0.06f)
        drawHorizontalLinesTexture(Color.Black, alpha = 0.02f)
    }
}

@Composable
fun CardCornerBrackets(color: Color) {
    CornerMarks(
        length = 16.dp,
        thickness = 2.dp,
        offset = 0.dp,
        color = color
    )
}

@Composable
private fun CornerMarks(length: Dp, thickness: Dp, offset: Dp, color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val len = length.toPx()
        val thick = thickness.toPx()
        val w = size.width
        val h = size.height
        val o = offset.toPx()

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
