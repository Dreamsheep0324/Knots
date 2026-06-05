package com.tang.prm.feature.divination.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.theme.SignalCoral
import com.tang.prm.ui.theme.SignalGreen

@Composable
internal fun LiuyaoTableSection(
    yaosDetail: List<LiuyaoYaoRecord>
) {
    Spacer(modifier = Modifier.height(20.dp))

    RecordSectionHeader("六爻详情")

    Spacer(modifier = Modifier.height(8.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
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

            yaosDetail.forEachIndexed { index, detail ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCell(positionName(detail.position), 0.7f, MaterialTheme.colorScheme.onSurface)
                    TableCell(detail.yaoType, 0.7f, MaterialTheme.colorScheme.onSurface)
                    TableCell(detail.najiaDizhi, 1f, MaterialTheme.colorScheme.onSurface)
                    TableCell(detail.wuxing, 0.7f, MaterialTheme.colorScheme.onSurfaceVariant)
                    TableCell(detail.sixRelative, 1f, MaterialTheme.colorScheme.onSurfaceVariant)
                    TableCell(detail.sixGod, 1f, MaterialTheme.colorScheme.onSurfaceVariant)
                    TableCell(
                        when {
                            detail.isWorld -> "世"
                            detail.isResponse -> "应"
                            else -> ""
                        },
                        0.8f,
                        if (detail.isWorld || detail.isResponse) SignalGreen else MaterialTheme.colorScheme.onSurface
                    )
                    TableCell(
                        if (detail.isVoid) "空" else "",
                        0.5f,
                        if (detail.isVoid) SignalCoral else MaterialTheme.colorScheme.onSurface
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

                if (index < yaosDetail.size - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun LiuyaoYaoDetailSection(
    yaosDetail: List<LiuyaoYaoRecord>,
    specialPattern: String?,
    specialAdvice: String?,
    isChaotic: Boolean,
    chaoticReason: String
) {
    Spacer(modifier = Modifier.height(16.dp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            RecordSectionHeader("爻辞详注")

            Spacer(modifier = Modifier.height(8.dp))

            yaosDetail.forEach { detail ->
                val line = buildString {
                    append("${positionName(detail.position)}爻 ")
                    append("${detail.sixGod} ${detail.sixRelative} ${detail.najiaDizhi}${detail.wuxing}")
                    if (detail.isWorld) append(" [世]")
                    if (detail.isResponse) append(" [应]")
                    if (detail.isVoid) append(" [空]")
                    if (detail.isChanging) {
                        append(" → 变 ${detail.changedDizhi}${detail.changedWuxing}")
                        if (detail.changedIsVoid) append(" [空]")
                    }
                }
                Text(
                    text = line,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }

            if (specialPattern != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "特殊卦式：$specialPattern",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SignalGreen
                )
            }

            if (specialAdvice != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = specialAdvice,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            if (isChaotic) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SignalCoral.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "⚠ 乱动卦：${chaoticReason}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = SignalCoral,
                        modifier = Modifier.padding(8.dp),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
