package com.tang.prm.feature.divination.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalCoral
import com.tang.prm.ui.theme.SignalGreen

internal data class LiuyaoYaoRecord(
    val position: Int,
    val yaoType: String,
    val isChanging: Boolean,
    val sixGod: String,
    val sixRelative: String,
    val najiaDizhi: String,
    val wuxing: String,
    val isWorld: Boolean,
    val isResponse: Boolean,
    val isVoid: Boolean,
    val changedDizhi: String,
    val changedWuxing: String,
    val changedLiuqin: String,
    val changedIsVoid: Boolean
)

internal fun positionName(position: Int): String {
    return when (position) {
        1 -> "初"
        2 -> "二"
        3 -> "三"
        4 -> "四"
        5 -> "五"
        6 -> "上"
        else -> position.toString()
    }
}

@Composable
internal fun RelationTag(relation: String) {
    val (bgColor, textColor) = when {
        relation.contains("生体") || relation.contains("比和") -> Pair(SignalGreen.copy(alpha = 0.15f), SignalGreen)
        relation.contains("克体") || relation.contains("泄体") -> Pair(SignalCoral.copy(alpha = 0.15f), SignalCoral)
        else -> Pair(SignalAmber.copy(alpha = 0.15f), SignalAmber)
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bgColor
    ) {
        Text(
            text = relation,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
internal fun InfoTag(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
internal fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(48.dp)
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
internal fun RowScope.TableHeaderCell(text: String, weight: Float) {
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
internal fun RowScope.TableCell(
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
