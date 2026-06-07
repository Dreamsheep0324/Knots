@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.divination.detail

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.domain.divination.model.DivinationRecord
import com.tang.prm.domain.util.DateUtils
import com.tang.prm.engine.divination.model.LiuyaoData
import com.tang.prm.engine.divination.model.MeihuaData
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SignalGreen
import kotlinx.serialization.json.Json

@Composable
fun RecordDetailScreen(
    record: DivinationRecord,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    // 类型化反序列化替代 Gson JsonObject 手动解析
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

    val isMeihua = record.method == "meihua"
    val methodLabel = if (isMeihua) "梅花" else "六爻"
    val methodColor = if (isMeihua) SignalGreen else MaterialTheme.colorScheme.onSurface

    val originalName = liuyaoData?.originalName ?: meihuaData?.originalName ?: "—"
    val changedName = liuyaoData?.changedName ?: meihuaData?.changedName ?: ""
    val interName = liuyaoData?.interName ?: meihuaData?.interName ?: ""
    val calculationMethod = meihuaData?.calculation?.methodKey ?: ""

    val ganzhi = liuyaoData?.ganzhi ?: meihuaData?.ganzhi
    val ganzhiStr = if (ganzhi != null) {
        "${ganzhi.year} ${ganzhi.month} ${ganzhi.day} ${ganzhi.hour}"
    } else ""

    val dateStr = DateUtils.formatYearMonthDaySlashTime(record.createdAt)

    val mainHexagram = meihuaData?.mainHexagram
    val description = mainHexagram?.description ?: liuyaoData?.palace?.let { "${it.name}（${it.wuxing}）" } ?: ""
    val guaciMeaning = mainHexagram?.guaciMeaning ?: ""
    val hexagramMeaning = mainHexagram?.hexagramMeaning ?: ""

    val analysis = meihuaData?.analysis
    val tiYongRelation = analysis?.tiYongRelation ?: ""
    val season = analysis?.season ?: ""
    val tiSeasonState = analysis?.tiSeasonState ?: ""
    val yongSeasonState = analysis?.yongSeasonState ?: ""
    val inter1Relation = analysis?.inter1Relation ?: ""
    val inter2Relation = analysis?.inter2Relation ?: ""
    val changedRelation = analysis?.changedRelation ?: ""

    val tiGua = meihuaData?.tiGua
    val yongGua = meihuaData?.yongGua
    val tiGuaName = tiGua?.name ?: ""
    val tiGuaElement = tiGua?.element ?: ""
    val tiGuaNature = tiGua?.nature ?: ""
    val yongGuaName = yongGua?.name ?: ""
    val yongGuaElement = yongGua?.element ?: ""
    val yongGuaNature = yongGua?.nature ?: ""

    val movingYao = meihuaData?.movingYao
    val movingYaoName = movingYao?.yaoName ?: ""
    val movingYaoDesc = movingYao?.description ?: ""

    val palace = liuyaoData?.palace
    val palaceName = palace?.name ?: ""
    val palaceWuxing = palace?.wuxing ?: ""

    val voidBranches = liuyaoData?.voidBranches ?: emptyList()
    val specialPattern = liuyaoData?.specialPattern
    val specialAdvice = liuyaoData?.specialAdvice
    val isChaotic = liuyaoData?.isChaotic ?: false
    val chaoticReason = liuyaoData?.chaoticReason ?: ""

    val changingYaos = liuyaoData?.changingYaos?.map { Triple(it.position, it.isChanging, it.type) } ?: emptyList()

    val yaosDetail = liuyaoData?.yaosDetail?.map { yao ->
        LiuyaoYaoRecord(
            position = yao.position,
            yaoType = yao.yaoType,
            isChanging = yao.isChanging,
            sixGod = yao.sixGod,
            sixRelative = yao.sixRelative,
            najiaDizhi = yao.najiaDizhi,
            wuxing = yao.wuxing,
            isWorld = yao.isWorld,
            isResponse = yao.isResponse,
            isVoid = yao.isVoid,
            changedDizhi = yao.changedYao?.dizhi ?: "",
            changedWuxing = yao.changedYao?.wuxing ?: "",
            changedLiuqin = yao.changedYao?.liuqin ?: "",
            changedIsVoid = yao.changedYao?.isVoid ?: false
        )
    } ?: emptyList()

    val lowerName = mainHexagram?.lower ?: ""
    val upperName = mainHexagram?.upper ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    if (dragAmount > 50f) {
                        onBack()
                    }
                }
            }
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "记录详情",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
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
                .verticalScroll(rememberScrollState())
        ) {
            RecordDetailHeader(
                originalName = originalName,
                description = description,
                isMeihua = isMeihua,
                methodLabel = methodLabel,
                methodColor = methodColor,
                calculationMethod = calculationMethod,
                palaceName = palaceName,
                palaceWuxing = palaceWuxing,
                changedName = changedName,
                interName = interName,
                voidBranches = voidBranches,
                ganzhiStr = ganzhiStr,
                season = season,
                tiSeasonState = tiSeasonState,
                yongSeasonState = yongSeasonState,
                movingYaoName = movingYaoName,
                movingYaoDesc = movingYaoDesc,
                changingYaos = changingYaos,
                dateStr = dateStr
            )

            GuaciSection(
                description = description,
                guaciMeaning = guaciMeaning,
                hexagramMeaning = hexagramMeaning,
                isMeihua = isMeihua,
                changingYaos = changingYaos
            )

            if (isMeihua && tiGuaName.isNotEmpty()) {
                MeihuaTiYongSection(
                    tiGuaName = tiGuaName,
                    tiGuaElement = tiGuaElement,
                    tiGuaNature = tiGuaNature,
                    tiSeasonState = tiSeasonState,
                    yongGuaName = yongGuaName,
                    yongGuaElement = yongGuaElement,
                    yongGuaNature = yongGuaNature,
                    yongSeasonState = yongSeasonState,
                    tiYongRelation = tiYongRelation,
                    interName = interName,
                    inter1Relation = inter1Relation,
                    inter2Relation = inter2Relation,
                    changedName = changedName,
                    changedRelation = changedRelation
                )
            }

            if (isMeihua && tiYongRelation.isNotEmpty()) {
                MeihuaRelationsSection(
                    tiYongRelation = tiYongRelation,
                    yongGuaName = yongGuaName,
                    lowerName = lowerName,
                    upperName = upperName,
                    inter1Relation = inter1Relation,
                    inter2Relation = inter2Relation,
                    changedName = changedName,
                    changedRelation = changedRelation
                )
            }

            if (!isMeihua && yaosDetail.isNotEmpty()) {
                LiuyaoTableSection(yaosDetail = yaosDetail)
                LiuyaoYaoDetailSection(
                    yaosDetail = yaosDetail,
                    specialPattern = specialPattern,
                    specialAdvice = specialAdvice,
                    isChaotic = isChaotic,
                    chaoticReason = chaoticReason
                )
            }

            RecordDetailFooter(
                aiAnalysis = record.aiAnalysis,
                onDelete = onDelete
            )
        }
    }
}
