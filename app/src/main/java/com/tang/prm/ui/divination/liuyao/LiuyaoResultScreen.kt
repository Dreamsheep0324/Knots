@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.divination.liuyao

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tang.prm.domain.divination.model.LiuyaoData
import com.tang.prm.domain.divination.model.LiuyaoYaoDetail
import com.tang.prm.engine.divination.data.HexagramData
import com.tang.prm.ui.divination.DivinationViewModel
import com.tang.prm.ui.divination.AiViewModel
import com.tang.prm.ui.divination.AiDeepSection
import com.tang.prm.ui.divination.components.ResultHexagramDisplay
import com.tang.prm.ui.divination.components.YaoDisplayData
import com.tang.prm.ui.navigation.Screen
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SignalGreen
import java.io.File
import java.io.FileOutputStream

@Composable
fun LiuyaoResultScreen(
    navController: NavController,
    viewModel: DivinationViewModel = hiltViewModel(
        viewModelStoreOwner = navController.getBackStackEntry(Screen.Divination.route)
    ),
    aiViewModel: AiViewModel = hiltViewModel()
) {
    val liuyaoData = viewModel.liuyaoData.collectAsState().value

    if (liuyaoData == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            CenterAlignedTopAppBar(
                title = { StepIndicator(currentStep = 3, totalSteps = 3) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Dimens.paddingPage),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "暂无卦象数据",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.onSurface,
                        contentColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface)
                ) {
                    Text(
                        text = "← 返回",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            }
        }
        return
    }

    LaunchedEffect(liuyaoData) {
        if (liuyaoData != null) {
            viewModel.saveResult()
        }
    }

    var showDeep by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CenterAlignedTopAppBar(
            title = { StepIndicator(currentStep = 3, totalSteps = 3) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            actions = {
                Spacer(modifier = Modifier.width(48.dp))
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Dimens.paddingPage)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                val yaoDataList = liuyaoData.yaosDetail.map { detail ->
                    val rightInfo = buildString {
                        append(detail.sixRelative)
                        append(" ")
                        append(detail.najiaDizhi)
                        if (detail.isWorld) append(" 世")
                        if (detail.isResponse) append(" 应")
                        if (detail.isChanging) append(" ○")
                    }
                    YaoDisplayData(
                        position = detail.position,
                        isYang = detail.yaoType == "阳",
                        isChanging = detail.isChanging,
                        leftLabel = positionName(detail.position),
                        rightInfo = rightInfo
                    )
                }

                val changingNote = buildChangingNote(liuyaoData)

                ResultHexagramDisplay(
                    yaoData = yaoDataList,
                    changingNote = changingNote
                )

                Spacer(modifier = Modifier.height(20.dp))

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

                Spacer(modifier = Modifier.height(20.dp))

                SectionHeader(title = "六爻详情")

                Spacer(modifier = Modifier.height(8.dp))

                YaoDetailTable(liuyaoData = liuyaoData)

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showDeep = false },
                            color = if (!showDeep) SignalGreen.copy(alpha = 0.15f) else Color.Transparent
                        ) {
                            Text(
                                text = "浅解",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!showDeep) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showDeep = true },
                            color = if (showDeep) SignalGreen.copy(alpha = 0.15f) else Color.Transparent
                        ) {
                            Text(
                                text = "AI深解",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showDeep) SignalGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                if (showDeep) {
                    AiDeepSection(
                        liuyaoData = liuyaoData,
                        viewModel = aiViewModel,
                        onQuestionChange = { viewModel.updateQuestion(it) },
                        onAnalysisComplete = { viewModel.saveAiAnalysis(it) }
                    )
                } else {
                    InterpretationSection(liuyaoData = liuyaoData)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            BottomNav(
                onReset = {
                    viewModel.resetLiuyao()
                    navController.navigate(Screen.LiuyaoCast.route) {
                        popUpTo(Screen.Divination.route) { inclusive = false }
                    }
                },
                onShare = {
                    val activity = navController.context as? Activity
                    if (activity != null) {
                        shareScreenCapture(activity)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int = 3) {
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
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
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
private fun TagsRow(liuyaoData: LiuyaoData) {
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
private fun InfoRows(liuyaoData: LiuyaoData) {
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
private fun YaoDetailTable(liuyaoData: LiuyaoData) {
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

@Composable
private fun InterpretationSection(liuyaoData: LiuyaoData) {
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

            if (liuyaoData.specialAdvice != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = liuyaoData.specialAdvice,
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

@Composable
private fun BottomNav(onReset: () -> Unit, onShare: () -> Unit) {
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

private fun positionName(position: Int): String = when (position) {
    1 -> "初"
    2 -> "二"
    3 -> "三"
    4 -> "四"
    5 -> "五"
    6 -> "上"
    else -> ""
}

private fun buildChangingNote(liuyaoData: LiuyaoData): String {
    if (liuyaoData.changingYaos.isEmpty()) return ""
    val yaoNames = liuyaoData.changingYaos.joinToString("·") { "${positionName(it.position)}爻" }
    return "$yaoNames 动 → ${liuyaoData.changedName}"
}

private fun getHexagramDescription(liuyaoData: LiuyaoData): String {
    return HexagramData.findByName(liuyaoData.originalName)?.description ?: ""
}

private fun getHexagramGuaciMeaning(liuyaoData: LiuyaoData): String {
    return HexagramData.findByName(liuyaoData.originalName)?.guaciMeaning ?: ""
}

private fun getHexagramMeaning(liuyaoData: LiuyaoData): String {
    return HexagramData.findByName(liuyaoData.originalName)?.hexagramMeaning ?: ""
}

private fun shareScreenCapture(activity: Activity) {
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
