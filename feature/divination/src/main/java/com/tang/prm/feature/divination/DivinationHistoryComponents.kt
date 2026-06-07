package com.tang.prm.feature.divination

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.divination.model.DivinationRecord
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.engine.divination.model.LiuyaoData
import com.tang.prm.engine.divination.model.MeihuaData
import com.tang.prm.ui.theme.SignalGreen
import kotlinx.serialization.json.Json

@Composable
internal fun RecordItem(
    record: DivinationRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    // 类型化反序列化替代 Gson JsonObject
    val liuyaoData = remember(record.id) {
        if (record.method == "liuyao") {
            try { Json.decodeFromString<LiuyaoData>(record.resultJson) } catch (_: Exception) { null }
        } else null
    }
    val meihuaData = remember(record.id) {
        if (record.method == "meihua") {
            try { Json.decodeFromString<MeihuaData>(record.resultJson) } catch (_: Exception) { null }
        } else null
    }

    val methodLabel = when (record.method) {
        "liuyao" -> "六爻"
        "meihua" -> "梅花"
        else -> record.method
    }
    val methodColor = when (record.method) {
        "liuyao" -> MaterialTheme.colorScheme.onSurface
        "meihua" -> SignalGreen
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val hexagramName = liuyaoData?.originalName ?: meihuaData?.originalName ?: "—"
    val changedName = liuyaoData?.changedName ?: meihuaData?.changedName ?: ""
    val methodKey = meihuaData?.calculation?.methodKey ?: ""

    val dateStr = DateUtils.formatMonthDaySlashTime(record.createdAt)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = methodColor.copy(alpha = 0.08f),
                border = BorderStroke(0.5.dp, methodColor.copy(alpha = 0.2f)),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = methodLabel,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = methodColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = hexagramName,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (changedName.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "→ $changedName",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (methodKey.isNotEmpty()) {
                        Text(
                            text = methodKey,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = dateStr,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
