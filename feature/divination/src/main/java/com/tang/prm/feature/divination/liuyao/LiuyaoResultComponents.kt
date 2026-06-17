@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.divination.liuyao

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
import com.tang.prm.engine.divination.model.LiuyaoData
import com.tang.prm.engine.divination.data.HexagramData
import com.tang.prm.ui.components.SectionHeader
import com.tang.prm.feature.divination.detail.positionName
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SignalGreen
import java.io.File
import java.io.FileOutputStream

@Composable
internal fun TagsRow(liuyaoData: LiuyaoData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        InfoTag(
            text = "${liuyaoData.palace.name}宫·${liuyaoData.palace.wuxing}",
            color = SignalGreen
        )
        Spacer(modifier = Modifier.width(8.dp))
        InfoTag(
            text = "变 ${liuyaoData.changedName}",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        InfoTag(
            text = "互 ${liuyaoData.interName}",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (liuyaoData.voidBranches.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            InfoTag(
                text = "空 ${liuyaoData.voidBranches.joinToString("")}",
                color = Color(0xFFEF4444)
            )
        }
    }
}

@Composable
private fun InfoTag(text: String, color: Color) {
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
internal fun InfoRows(liuyaoData: LiuyaoData) {
    Column(modifier = Modifier.fillMaxWidth()) {
        InfoRow(label = "宫位", value = "${liuyaoData.palace.name}宫·${liuyaoData.palace.wuxing}")
        InfoRow(label = "变卦", value = liuyaoData.changedName)
        InfoRow(label = "互卦", value = liuyaoData.interName)

        val changingDesc = if (liuyaoData.changingYaos.isEmpty()) {
            "静卦（无动爻）"
        } else {
            liuyaoData.changingYaos.joinToString("、") {
                "${positionName(it.position)}爻·${it.type}"
            }
        }
        InfoRow(label = "动爻", value = changingDesc)

        if (liuyaoData.voidBranches.isNotEmpty()) {
            InfoRow(label = "空亡", value = liuyaoData.voidBranches.joinToString("、"))
        }

        val ganzhi = liuyaoData.ganzhi
        InfoRow(
            label = "干支",
            value = "${ganzhi.year} ${ganzhi.month} ${ganzhi.day} ${ganzhi.hour}"
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(40.dp)
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
internal fun InterpretationSection(liuyaoData: LiuyaoData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            SectionHeader(title = "卦辞")

            Spacer(modifier = Modifier.height(8.dp))

            val desc = getHexagramDescription(liuyaoData)
            if (desc.isNotEmpty()) {
                Text(
                    text = desc,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
            }

            val meaning = getHexagramGuaciMeaning(liuyaoData)
            if (meaning.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = meaning,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            val hexMeaning = getHexagramMeaning(liuyaoData)
            if (hexMeaning.isNotEmpty()) {
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
                    text = hexMeaning,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            if (liuyaoData.changingYaos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "动爻提示：",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SignalGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                liuyaoData.changingYaos.forEach { changingYao ->
                    Text(
                        text = "${positionName(changingYao.position)}爻·${changingYao.type}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                thickness = 0.5.dp
            )
            Spacer(modifier = Modifier.height(8.dp))

            liuyaoData.yaosDetail.forEach { detail ->
                val line = buildString {
                    append("${positionName(detail.position)}爻 ")
                    append("${detail.sixGod} ${detail.sixRelative} ${detail.najiaDizhi}${detail.wuxing}")
                    if (detail.isWorld) append(" [世]")
                    if (detail.isResponse) append(" [应]")
                    if (detail.isVoid) append(" [空]")
                    if (detail.isChanging) {
                        append(" → 变 ${detail.changedYao?.dizhi ?: ""}${detail.changedYao?.wuxing ?: ""}")
                        if (detail.changedYao?.isVoid == true) append(" [空]")
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

            if (liuyaoData.specialPattern != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "特殊卦式：${liuyaoData.specialPattern}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SignalGreen
                )
            }

            liuyaoData.specialAdvice?.let { advice ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = advice,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            if (liuyaoData.isChaotic) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "⚠ 乱动卦：${liuyaoData.chaoticReason ?: ""}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(8.dp),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

internal fun buildChangingNote(liuyaoData: LiuyaoData): String {
    if (liuyaoData.changingYaos.isEmpty()) return ""
    val yaoNames = liuyaoData.changingYaos.joinToString("·") { "${positionName(it.position)}爻" }
    return "$yaoNames 动 → ${liuyaoData.changedName}"
}

internal fun getHexagramDescription(liuyaoData: LiuyaoData): String {
    return HexagramData.findByName(liuyaoData.originalName)?.description ?: ""
}

internal fun getHexagramGuaciMeaning(liuyaoData: LiuyaoData): String {
    return HexagramData.findByName(liuyaoData.originalName)?.guaciMeaning ?: ""
}

internal fun getHexagramMeaning(liuyaoData: LiuyaoData): String {
    return HexagramData.findByName(liuyaoData.originalName)?.hexagramMeaning ?: ""
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
internal fun LiuyaoHexagramInfoSection(liuyaoData: LiuyaoData) {
    Text(
        text = liuyaoData.originalName,
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraLight,
        letterSpacing = 8.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    val desc = getHexagramDescription(liuyaoData)
    if (desc.isNotEmpty()) {
        Text(
            text = desc,
            fontSize = 11.sp,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    TagsRow(liuyaoData = liuyaoData)

    Spacer(modifier = Modifier.height(16.dp))

    InfoRows(liuyaoData = liuyaoData)
}
