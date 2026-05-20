@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.ui.divination

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.tang.prm.domain.divination.model.DivinationRecord
import com.tang.prm.ui.theme.Dimens
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalCoral
import com.tang.prm.ui.theme.SignalGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DivinationHistoryScreen(
    navController: NavController,
    viewModel: DivinationHistoryViewModel = hiltViewModel()
) {
    val records by viewModel.records.collectAsState()
    var selectedRecord by remember { mutableStateOf<DivinationRecord?>(null) }
    var lastRecord by remember { mutableStateOf<DivinationRecord?>(null) }

    LaunchedEffect(selectedRecord) {
        if (selectedRecord != null) {
            lastRecord = selectedRecord
        }
    }

    BackHandler(enabled = selectedRecord != null) {
        selectedRecord = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "占卜记录",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedRecord != null) {
                            selectedRecord = null
                        } else {
                            navController.popBackStack()
                        }
                    }) {
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

            if (records.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.paddingPage),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "暂无记录",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "起卦后点击保存即可记录",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.paddingPage),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(records, key = { it.id }) { record ->
                        RecordItem(
                            record = record,
                            onClick = { selectedRecord = record },
                            onDelete = { viewModel.deleteRecord(record) }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = selectedRecord != null,
            enter = fadeIn(tween(450, delayMillis = 50, easing = FastOutSlowInEasing)) +
                scaleIn(
                    animationSpec = tween(450, delayMillis = 50, easing = FastOutSlowInEasing),
                    initialScale = 0.96f
                ),
            exit = fadeOut(tween(200)) +
                scaleOut(
                    animationSpec = tween(200),
                    targetScale = 0.96f
                )
        ) {
            val record = lastRecord
            if (record != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    RecordDetailScreen(
                        record = record,
                        onBack = { selectedRecord = null },
                        onDelete = {
                            viewModel.deleteRecord(record)
                            selectedRecord = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordItem(
    record: DivinationRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val gson = remember { Gson() }
    val json = remember(record.id) {
        try {
            gson.fromJson(record.resultJson, JsonObject::class.java)
        } catch (_: Exception) {
            null
        }
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
    val hexagramName = json?.get("originalName")?.asString ?: "—"
    val changedName = json?.get("changedName")?.asString ?: ""
    val methodKey = json?.getAsJsonObject("calculation")?.get("method")?.asString ?: ""

    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(record.createdAt))

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

@Composable
private fun RecordDetailScreen(
    record: DivinationRecord,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    val gson = remember { Gson() }
    val json = remember(record.id) {
        try {
            gson.fromJson(record.resultJson, JsonObject::class.java)
        } catch (_: Exception) {
            null
        }
    }

    val isMeihua = record.method == "meihua"
    val methodLabel = if (isMeihua) "梅花" else "六爻"
    val methodColor = if (isMeihua) SignalGreen else MaterialTheme.colorScheme.onSurface

    val originalName = json?.get("originalName")?.asString ?: "—"
    val changedName = json?.get("changedName")?.asString ?: ""
    val interName = json?.get("interName")?.asString ?: ""
    val calculationMethod = json?.getAsJsonObject("calculation")?.get("method")?.asString ?: ""

    val ganzhi = json?.getAsJsonObject("ganzhi")
    val ganzhiStr = if (ganzhi != null) {
        "${ganzhi.get("year")?.asString ?: ""} ${ganzhi.get("month")?.asString ?: ""} ${ganzhi.get("day")?.asString ?: ""} ${ganzhi.get("hour")?.asString ?: ""}"
    } else ""

    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(record.createdAt))

    val mainHexagram = json?.getAsJsonObject("mainHexagram")
    val description = mainHexagram?.get("description")?.asString ?: ""
    val guaciMeaning = mainHexagram?.get("guaciMeaning")?.asString ?: ""
    val hexagramMeaning = mainHexagram?.get("hexagramMeaning")?.asString ?: ""

    val analysis = json?.getAsJsonObject("analysis")
    val tiYongRelation = analysis?.get("tiYongRelation")?.asString ?: ""
    val season = analysis?.get("season")?.asString ?: ""
    val tiSeasonState = analysis?.get("tiSeasonState")?.asString ?: ""
    val yongSeasonState = analysis?.get("yongSeasonState")?.asString ?: ""
    val inter1Relation = analysis?.get("inter1Relation")?.asString ?: ""
    val inter2Relation = analysis?.get("inter2Relation")?.asString ?: ""
    val changedRelation = analysis?.get("changedRelation")?.asString ?: ""

    val tiGua = json?.getAsJsonObject("tiGua")
    val yongGua = json?.getAsJsonObject("yongGua")
    val tiGuaName = tiGua?.get("name")?.asString ?: ""
    val tiGuaElement = tiGua?.get("element")?.asString ?: ""
    val tiGuaNature = tiGua?.get("nature")?.asString ?: ""
    val yongGuaName = yongGua?.get("name")?.asString ?: ""
    val yongGuaElement = yongGua?.get("element")?.asString ?: ""
    val yongGuaNature = yongGua?.get("nature")?.asString ?: ""

    val movingYao = json?.getAsJsonObject("movingYao")
    val movingYaoName = movingYao?.get("yaoName")?.asString ?: ""
    val movingYaoDesc = movingYao?.get("description")?.asString ?: ""

    val palace = json?.getAsJsonObject("palace")
    val palaceName = palace?.get("name")?.asString ?: ""
    val palaceWuxing = palace?.get("wuxing")?.asString ?: ""

    val voidBranches = json?.getAsJsonArray("voidBranches")?.map { it.asString } ?: emptyList()
    val specialPattern = json?.get("specialPattern")?.asString
    val specialAdvice = json?.get("specialAdvice")?.asString
    val isChaotic = json?.get("isChaotic")?.asBoolean ?: false
    val chaoticReason = json?.get("chaoticReason")?.asString ?: ""

    val changingYaos = json?.getAsJsonArray("changingYaos")?.mapNotNull { elem ->
        val obj = elem.asJsonObject
        Triple(obj.get("position")?.asInt ?: 0, obj.get("isChanging")?.asBoolean ?: false, obj.get("type")?.asString ?: "")
    } ?: emptyList()

    val yaosDetail = json?.getAsJsonArray("yaosDetail")?.mapNotNull { elem ->
        val obj = elem.asJsonObject
        val changedObj = obj.getAsJsonObject("changedYao")
        LiuyaoYaoRecord(
            position = obj.get("position")?.asInt ?: 0,
            yaoType = obj.get("yaoType")?.asString ?: "",
            isChanging = obj.get("isChanging")?.asBoolean ?: false,
            sixGod = obj.get("sixGod")?.asString ?: "",
            sixRelative = obj.get("sixRelative")?.asString ?: "",
            najiaDizhi = obj.get("najiaDizhi")?.asString ?: "",
            wuxing = obj.get("wuxing")?.asString ?: "",
            isWorld = obj.get("isWorld")?.asBoolean ?: false,
            isResponse = obj.get("isResponse")?.asBoolean ?: false,
            isVoid = obj.get("isVoid")?.asBoolean ?: false,
            changedDizhi = changedObj?.get("dizhi")?.asString ?: "",
            changedWuxing = changedObj?.get("wuxing")?.asString ?: "",
            changedLiuqin = changedObj?.get("liuqin")?.asString ?: "",
            changedIsVoid = changedObj?.get("isVoid")?.asBoolean ?: false
        )
    } ?: emptyList()

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

            if (description.isNotEmpty() || guaciMeaning.isNotEmpty() || hexagramMeaning.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SectionHeader("卦辞")

                        Spacer(modifier = Modifier.height(8.dp))

                        if (description.isNotEmpty()) {
                            Text(
                                text = description,
                                fontSize = 12.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (guaciMeaning.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = guaciMeaning,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }

                        if (hexagramMeaning.isNotEmpty()) {
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
                                text = hexagramMeaning,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }

                        if (!isMeihua && changingYaos.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "动爻提示",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SignalGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            changingYaos.forEach { (pos, _, type) ->
                                Text(
                                    text = "${positionName(pos)}爻·$type",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            if (isMeihua && tiGuaName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                SectionHeader("体用总断")

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    Text(
                        text = "体卦：${tiGuaName}（${tiGuaElement}·${tiGuaNature}）$tiSeasonState",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "用卦：${yongGuaName}（${yongGuaElement}·${yongGuaNature}）$yongSeasonState",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                    if (tiYongRelation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "体用关系：$tiYongRelation",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                    if (interName.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "互卦：$interName — 互下$inter1Relation，互上$inter2Relation",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                    if (changedName.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "变卦：$changedName — $changedRelation",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (isMeihua && tiYongRelation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                SectionHeader("事之始终")

                Spacer(modifier = Modifier.height(8.dp))

                val relations = buildList {
                    if (yongGuaName.isNotEmpty()) add(Triple("始", "用·$yongGuaName", tiYongRelation))
                    val lowerName = mainHexagram?.get("lower")?.asString ?: ""
                    val upperName = mainHexagram?.get("upper")?.asString ?: ""
                    if (lowerName.isNotEmpty() && inter1Relation.isNotEmpty()) add(Triple("中", "互·$lowerName", inter1Relation))
                    if (upperName.isNotEmpty() && inter2Relation.isNotEmpty()) add(Triple("中", "互·$upperName", inter2Relation))
                    if (changedName.isNotEmpty() && changedRelation.isNotEmpty()) add(Triple("终", "变·$changedName", changedRelation))
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    relations.forEachIndexed { index, (stage, name, relation) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = stage,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            RelationTag(relation)
                        }
                        if (index < relations.size - 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 1.dp, bottom = 1.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(8.dp)
                                        .background(MaterialTheme.colorScheme.outline)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "↓",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (!isMeihua && yaosDetail.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))

                SectionHeader("六爻详情")

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

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SectionHeader("爻辞详注")

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

            Spacer(modifier = Modifier.height(24.dp))

            if (record.aiAnalysis.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SectionHeader("AI解读")

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = record.aiAnalysis,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = SignalCoral
                ),
                border = BorderStroke(1.dp, SignalCoral.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "删除此记录",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private data class LiuyaoYaoRecord(
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

private fun positionName(position: Int): String {
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
private fun RelationTag(relation: String) {
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
private fun InfoRow(label: String, value: String) {
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
