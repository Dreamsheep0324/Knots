package com.tang.prm.feature.divination.liuyao

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.engine.divination.model.LiuyaoData
import com.tang.prm.feature.divination.detail.positionName
import com.tang.prm.ui.theme.SignalGreen

@Composable
internal fun YaoDetailTable(liuyaoData: LiuyaoData) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outline = MaterialTheme.colorScheme.outline

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                TableHeaderCell("位", 0.7f)
                TableHeaderCell("型", 0.7f)
                TableHeaderCell("支", 1f)
                TableHeaderCell("行", 0.7f)
                TableHeaderCell("亲", 1f)
                TableHeaderCell("神", 1f)
                TableHeaderCell("世应", 0.8f)
                TableHeaderCell("空", 0.5f)
                TableHeaderCell("动", 0.5f)
            }

            liuyaoData.yaosDetail.forEachIndexed { index, detail ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(positionName(detail.position), 0.7f, onSurface)
                    TableCell(detail.yaoType, 0.7f, onSurface)
                    TableCell(detail.najiaDizhi, 1f, onSurface)
                    TableCell(detail.wuxing, 0.7f, onSurfaceVariant)
                    TableCell(detail.sixRelative, 1f, onSurfaceVariant)
                    TableCell(detail.sixGod, 1f, onSurfaceVariant)
                    TableCell(
                        when {
                            detail.isWorld -> "世"
                            detail.isResponse -> "应"
                            else -> ""
                        },
                        0.8f,
                        if (detail.isWorld || detail.isResponse) SignalGreen else onSurface
                    )
                    TableCell(
                        if (detail.isVoid) "空" else "",
                        0.5f,
                        if (detail.isVoid) Color(0xFFEF4444) else onSurface
                    )
                    if (detail.isChanging) {
                        Box(
                            modifier = Modifier.weight(0.5f),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = RoundedCornerShape(3.dp),
                                color = SignalGreen.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "动",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SignalGreen,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(0.5f))
                    }
                }

                if (index < liuyaoData.yaosDetail.size - 1) {
                    HorizontalDivider(
                        color = outline.copy(alpha = 0.2f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.TableHeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        fontFamily = FontFamily.Monospace,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.weight(weight),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    color: Color
) {
    Text(
        text = text,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        color = color,
        modifier = Modifier.weight(weight),
        textAlign = TextAlign.Center
    )
}
