package com.tang.prm.feature.divination.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.theme.SignalCoral
import com.tang.prm.ui.theme.SignalGreen

@Composable
internal fun RecordDetailHeader(
    originalName: String,
    description: String,
    isMeihua: Boolean,
    methodLabel: String,
    methodColor: Color,
    calculationMethod: String,
    palaceName: String,
    palaceWuxing: String,
    changedName: String,
    interName: String,
    voidBranches: List<String>,
    ganzhiStr: String,
    season: String,
    tiSeasonState: String,
    yongSeasonState: String,
    movingYaoName: String,
    movingYaoDesc: String,
    changingYaos: List<Triple<Int, Boolean, String>>,
    dateStr: String
) {
    Text(
        text = originalName,
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraLight,
        letterSpacing = 8.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )

    if (description.isNotEmpty()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = description,
            fontSize = 11.sp,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        InfoTag(methodLabel, methodColor)
        if (isMeihua && calculationMethod.isNotEmpty()) {
            Spacer(modifier = Modifier.width(6.dp))
            InfoTag(calculationMethod, methodColor)
        }
        if (!isMeihua && palaceName.isNotEmpty()) {
            Spacer(modifier = Modifier.width(6.dp))
            InfoTag("${palaceName}宫·${palaceWuxing}", methodColor)
        }
        if (changedName.isNotEmpty()) {
            Spacer(modifier = Modifier.width(6.dp))
            InfoTag("变 $changedName", MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (interName.isNotEmpty()) {
            Spacer(modifier = Modifier.width(6.dp))
            InfoTag("互 $interName", MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!isMeihua && voidBranches.isNotEmpty()) {
            Spacer(modifier = Modifier.width(6.dp))
            InfoTag("空 ${voidBranches.joinToString("")}", SignalCoral)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (isMeihua) {
        InfoRow("起卦", calculationMethod.ifEmpty { methodLabel })
        if (changedName.isNotEmpty()) InfoRow("变卦", changedName)
        if (interName.isNotEmpty()) InfoRow("互卦", interName)
        if (movingYaoName.isNotEmpty()) InfoRow("动爻", movingYaoDesc.ifEmpty { movingYaoName })
        if (ganzhiStr.isNotEmpty()) InfoRow("干支", ganzhiStr)
        if (season.isNotEmpty()) InfoRow("时令", "$season · 体$tiSeasonState · 用$yongSeasonState")
    } else {
        if (palaceName.isNotEmpty()) InfoRow("宫位", "${palaceName}宫·${palaceWuxing}")
        if (changedName.isNotEmpty()) InfoRow("变卦", changedName)
        if (interName.isNotEmpty()) InfoRow("互卦", interName)
        if (changingYaos.isNotEmpty()) {
            val changingDesc = if (changingYaos.isEmpty()) "静卦（无动爻）"
            else changingYaos.joinToString("、") { "${positionName(it.first)}爻·${it.third}" }
            InfoRow("动爻", changingDesc)
        } else {
            InfoRow("动爻", "静卦（无动爻）")
        }
        if (voidBranches.isNotEmpty()) InfoRow("空亡", voidBranches.joinToString("、"))
        if (ganzhiStr.isNotEmpty()) InfoRow("干支", ganzhiStr)
    }
    InfoRow("时间", dateStr)
}
