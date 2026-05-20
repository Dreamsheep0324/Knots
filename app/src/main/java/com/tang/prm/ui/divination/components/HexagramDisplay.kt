package com.tang.prm.ui.divination.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class YaoDisplayData(
    val position: Int,
    val isYang: Boolean,
    val isChanging: Boolean,
    val leftLabel: String,
    val rightInfo: String
)

@Composable
fun HexagramDisplay(
    yaoData: List<YaoDisplayData>,
    modifier: Modifier = Modifier
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val strokeWidth = 4.dp
    val lineCanvasWidth = 140.dp
    val leftLabelWidth = 16.dp
    val lineSpacing = 12.dp
    val verticalPadding = 4.dp
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
    val labelStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 9.sp,
        color = onSurfaceVariantColor
    )
    val rightInfoStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 9.sp,
        color = onSurfaceVariantColor
    )

    val reversed = yaoData.sortedByDescending { it.position }

    Canvas(
        modifier = modifier
            .width(leftLabelWidth + lineCanvasWidth)
            .height(verticalPadding * 2 + strokeWidth * 6 + lineSpacing * 5)
    ) {
        val strokePx = strokeWidth.toPx()
        val spacingPx = lineSpacing.toPx()
        val leftLabelPx = leftLabelWidth.toPx()
        val canvasLineW = lineCanvasWidth.toPx()
        val verticalPaddingPx = verticalPadding.toPx()

        reversed.forEachIndexed { index, yao ->
            val y = verticalPaddingPx + index * (strokePx + spacingPx) + strokePx / 2
            val lineColor = if (yao.isChanging) onSurfaceVariantColor else onSurfaceColor
            val pathEffect = if (yao.isChanging) dashEffect else null

            val startX = leftLabelPx
            val endX = leftLabelPx + canvasLineW

            if (yao.isYang) {
                drawLine(
                    color = lineColor,
                    start = Offset(startX, y),
                    end = Offset(endX, y),
                    strokeWidth = strokePx,
                    cap = StrokeCap.Round,
                    pathEffect = pathEffect
                )
            } else {
                val gapWidth = canvasLineW * 0.2f
                val segmentWidth = canvasLineW * 0.4f
                val leftEnd = startX + segmentWidth
                val rightStart = leftEnd + gapWidth

                drawLine(
                    color = lineColor,
                    start = Offset(startX, y),
                    end = Offset(leftEnd, y),
                    strokeWidth = strokePx,
                    cap = StrokeCap.Round,
                    pathEffect = pathEffect
                )
                drawLine(
                    color = lineColor,
                    start = Offset(rightStart, y),
                    end = Offset(endX, y),
                    strokeWidth = strokePx,
                    cap = StrokeCap.Round,
                    pathEffect = pathEffect
                )
            }

            drawText(
                textMeasurer = textMeasurer,
                text = yao.leftLabel,
                topLeft = Offset(0f, y - strokePx / 2),
                style = labelStyle
            )

            if (yao.rightInfo.isNotEmpty()) {
                val rightX = endX + 4.dp.toPx()
                val availableWidth = (size.width - rightX).toInt().coerceAtLeast(0)
                drawText(
                    textMeasurer = textMeasurer,
                    text = yao.rightInfo,
                    topLeft = Offset(rightX, y - strokePx / 2),
                    style = rightInfoStyle,
                    size = androidx.compose.ui.geometry.Size(availableWidth.toFloat(), strokePx * 2)
                )
            }
        }
    }
}

@Composable
fun ResultHexagramDisplay(
    yaoData: List<YaoDisplayData>,
    changingNote: String,
    modifier: Modifier = Modifier
) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val strokeWidth = 5.dp
    val lineCanvasWidth = 160.dp
    val lineSpacing = 12.dp
    val verticalPadding = 10.dp
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
    val labelStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 9.sp,
        color = onSurfaceVariantColor
    )
    val rightInfoStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = 9.sp,
        color = onSurfaceVariantColor
    )

    val reversed = yaoData.sortedByDescending { it.position }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(verticalPadding * 2 + strokeWidth * 6 + lineSpacing * 5)
        ) {
            val strokePx = strokeWidth.toPx()
            val spacingPx = lineSpacing.toPx()
            val canvasLineW = lineCanvasWidth.toPx()
            val verticalPaddingPx = verticalPadding.toPx()

            val lineStartX = (size.width - canvasLineW) / 2
            val lineEndX = lineStartX + canvasLineW

            reversed.forEachIndexed { index, yao ->
                val y = verticalPaddingPx + index * (strokePx + spacingPx) + strokePx / 2
                val lineColor = if (yao.isChanging) onSurfaceVariantColor else onSurfaceColor
                val pathEffect = if (yao.isChanging) dashEffect else null

                if (yao.isYang) {
                    drawLine(
                        color = lineColor,
                        start = Offset(lineStartX, y),
                        end = Offset(lineEndX, y),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round,
                        pathEffect = pathEffect
                    )
                } else {
                    val gapWidth = canvasLineW * 0.2f
                    val segmentWidth = canvasLineW * 0.4f
                    val leftEnd = lineStartX + segmentWidth
                    val rightStart = leftEnd + gapWidth

                    drawLine(
                        color = lineColor,
                        start = Offset(lineStartX, y),
                        end = Offset(leftEnd, y),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round,
                        pathEffect = pathEffect
                    )
                    drawLine(
                        color = lineColor,
                        start = Offset(rightStart, y),
                        end = Offset(lineEndX, y),
                        strokeWidth = strokePx,
                        cap = StrokeCap.Round,
                        pathEffect = pathEffect
                    )
                }

                val labelLayout = textMeasurer.measure(yao.leftLabel, labelStyle)
                drawText(
                    textLayoutResult = labelLayout,
                    topLeft = Offset(
                        lineStartX - labelLayout.size.width - 4.dp.toPx(),
                        y - labelLayout.size.height / 2
                    )
                )

                if (yao.rightInfo.isNotEmpty()) {
                    val rightX = lineEndX + 4.dp.toPx()
                    val availableWidth = (size.width - rightX).coerceAtLeast(0f)
                    val rightLayout = textMeasurer.measure(
                        yao.rightInfo, rightInfoStyle,
                        constraints = Constraints(maxWidth = availableWidth.toInt())
                    )
                    drawText(
                        textLayoutResult = rightLayout,
                        topLeft = Offset(rightX, y - rightLayout.size.height / 2)
                    )
                }
            }
        }

        if (changingNote.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = changingNote,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = onSurfaceVariantColor
            )
        }
    }
}
