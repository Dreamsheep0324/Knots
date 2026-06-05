@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.divination.meihua

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.tang.prm.engine.divination.model.MeihuaData
import com.tang.prm.feature.divination.components.YaoDisplayData
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SignalGreen
import java.io.File
import java.io.FileOutputStream

@Composable
internal fun InterpretationSection(data: MeihuaData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            MeihuaSectionHeader(title = "卦辞")

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = data.mainHexagram.description,
                fontSize = 12.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (data.mainHexagram.guaciMeaning.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = data.mainHexagram.guaciMeaning,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            if (data.mainHexagram.hexagramMeaning.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "核心象义",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SignalGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = data.mainHexagram.hexagramMeaning,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "体用总断",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SignalGreen
            )

            Spacer(modifier = Modifier.height(6.dp))

            val tiYongSummary = buildTiYongSummary(data)
            Text(
                text = tiYongSummary,
                fontSize = 12.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(8.dp))

            val deepAnalysis = buildDeepAnalysis(data)
            Text(
                text = deepAnalysis,
                fontSize = 12.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

internal fun buildMeihuaYaoDisplayData(data: MeihuaData): List<YaoDisplayData> {
    val positionNames = listOf("初", "二", "三", "四", "五", "上")

    return data.yaosDetail.map { yao ->
        val trigramName = if (yao.position <= 3) data.mainHexagram.lower else data.mainHexagram.upper
        val element = if (yao.tiYong == "体") data.tiGua.element else data.yongGua.element

        val rightInfo = if (yao.tiYong == "体" || yao.tiYong == "用") {
            "${yao.tiYong} $trigramName·$element"
        } else {
            ""
        }

        YaoDisplayData(
            position = yao.position,
            isYang = yao.yaoType == "阳",
            isChanging = yao.isChanging,
            leftLabel = positionNames.getOrElse(yao.position - 1) { "${yao.position}" },
            rightInfo = rightInfo
        )
    }
}

internal fun buildTiYongSummary(data: MeihuaData): String {
    val lines = mutableListOf<String>()

    lines.add("体卦：${data.tiGua.name}（${data.tiGua.element}·${data.tiGua.nature}）${data.analysis.tiSeasonState}")
    lines.add("用卦：${data.yongGua.name}（${data.yongGua.element}·${data.yongGua.nature}）${data.analysis.yongSeasonState}")
    lines.add("体用关系：${data.analysis.tiYongRelation}")

    if (data.interName.isNotEmpty()) {
        lines.add("互卦：${data.interName} — 互下${data.analysis.inter1Relation}，互上${data.analysis.inter2Relation}")
    }

    if (data.changedName.isNotEmpty()) {
        lines.add("变卦：${data.changedName} — ${data.analysis.changedRelation}")
    }

    return lines.joinToString("\n")
}

internal fun buildDeepAnalysis(data: MeihuaData): String {
    val lines = mutableListOf<String>()

    lines.add("【体用旺衰】")
    lines.add("体卦${data.tiGua.element}于${data.analysis.season}${data.analysis.tiSeasonState}，用卦${data.yongGua.element}于${data.analysis.season}${data.analysis.yongSeasonState}。")

    lines.add("")
    lines.add("【生克链路】")
    lines.add("用→体：${data.analysis.tiYongRelation}")
    lines.add("互下→体：${data.analysis.inter1Relation}")
    lines.add("互上→体：${data.analysis.inter2Relation}")
    lines.add("变→体：${data.analysis.changedRelation}")

    lines.add("")
    lines.add("【动爻】")
    lines.add("${data.movingYao.yaoName}动，${data.movingYao.description}")

    if (data.changedHexagram != null) {
        lines.add("")
        lines.add("【变卦体用】")
        val changedTi = data.changedTiGua
        val changedYong = data.changedYongGua
        if (changedTi != null && changedYong != null) {
            lines.add("变体：${changedTi.name}（${changedTi.element}），变用：${changedYong.name}（${changedYong.element}）")
            lines.add("变卦体用关系：${data.analysis.changedTiYongRelation}")
        }
    }

    return lines.joinToString("\n")
}

@Composable
internal fun MeihuaSectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        )
    }
}

@Composable
internal fun MeihuaInfoTag(text: String, color: Color) {
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
internal fun MeihuaInfoRow(label: String, value: String) {
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
internal fun StepIndicator(currentStep: Int, totalSteps: Int = 3) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        for (step in 1..totalSteps) {
            val isActive = step == currentStep
            val isCompleted = step < currentStep
            val color = when {
                isActive -> MaterialTheme.colorScheme.onSurface
                isCompleted -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.outline
            }
            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(color))
            if (step < totalSteps) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(modifier = Modifier.width(16.dp).height(1.dp).background(
                    if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                ))
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
internal fun BottomNav(onReset: () -> Unit, onShare: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text(
                text = "← 重新起卦",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }

        OutlinedButton(
            onClick = onShare,
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface)
        ) {
            Text(
                text = "分享 →",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }
    }
}

internal fun shareScreenCapture(activity: Activity) {
    val decorView = activity.window.decorView
    val bitmap = Bitmap.createBitmap(decorView.width, decorView.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    decorView.draw(canvas)

    val file = File(activity.cacheDir, "divination_share.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    bitmap.recycle()

    val uri: Uri = FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    activity.startActivity(Intent.createChooser(shareIntent, "分享卦象"))
}

@Composable
internal fun MeihuaHexagramInfoSection(data: MeihuaData) {
    Text(
        text = data.originalName,
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraLight,
        letterSpacing = 8.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
        text = data.mainHexagram.description,
        fontSize = 11.sp,
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MeihuaInfoTag(
            text = data.calculation?.method ?: "起卦",
            color = SignalGreen
        )
        Spacer(modifier = Modifier.width(8.dp))
        MeihuaInfoTag(
            text = "变 ${data.changedName}",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        MeihuaInfoTag(
            text = "互 ${data.interName}",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        MeihuaInfoTag(
            text = "${data.analysis.season}·${data.analysis.tiSeasonState}",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    MeihuaInfoRow(label = "起卦", value = data.calculation?.method ?: "—")
    MeihuaInfoRow(label = "变卦", value = data.changedName)
    MeihuaInfoRow(label = "互卦", value = data.interName)
    MeihuaInfoRow(label = "动爻", value = data.movingYao.description)
    MeihuaInfoRow(label = "干支", value = "${data.ganzhi.year} ${data.ganzhi.month} ${data.ganzhi.day} ${data.ganzhi.hour}")
    MeihuaInfoRow(label = "时令", value = "${data.analysis.season} · 体${data.analysis.tiSeasonState} · 用${data.analysis.yongSeasonState}")
}
