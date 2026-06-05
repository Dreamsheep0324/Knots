package com.tang.prm.feature.divination.meihua

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.tang.prm.engine.divination.model.MeihuaData
import com.tang.prm.engine.divination.model.MeihuaYaoDetail
import com.tang.prm.ui.theme.SignalAmber
import com.tang.prm.ui.theme.SignalCoral
import com.tang.prm.ui.theme.SignalGreen

@Composable
internal fun YaoDetailTable(yaos: List<MeihuaYaoDetail>, data: MeihuaData) {
    val positionNames = listOf("初", "二", "三", "四", "五", "上")
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
                MeihuaTableHeaderCell("位", 1f)
                MeihuaTableHeaderCell("型", 1f)
                MeihuaTableHeaderCell("卦", 1f)
                MeihuaTableHeaderCell("五行", 1f)
                MeihuaTableHeaderCell("体用", 1f)
                MeihuaTableHeaderCell("旺衰", 1f)
                MeihuaTableHeaderCell("动", 0.6f)
            }

            yaos.forEachIndexed { index, yao ->
                val trigramName = if (yao.position <= 3) data.mainHexagram.lower else data.mainHexagram.upper
                val element = if (yao.tiYong == "体") data.tiGua.element else data.yongGua.element
                val seasonState = if (yao.tiYong == "体") data.analysis.tiSeasonState else data.analysis.yongSeasonState

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MeihuaTableCell(positionNames.getOrElse(index) { "${index + 1}" }, 1f, onSurface)
                    MeihuaTableCell(yao.yaoType, 1f, onSurface)
                    MeihuaTableCell(trigramName, 1f, onSurfaceVariant)
                    MeihuaTableCell(element, 1f, onSurfaceVariant)
                    MeihuaTableCell(yao.tiYong, 1f, if (yao.tiYong == "体") SignalGreen else SignalAmber)
                    MeihuaTableCell(seasonState, 1f, onSurfaceVariant)
                    MeihuaTableCell(if (yao.isChanging) "●" else "", 0.6f, if (yao.isChanging) SignalCoral else onSurfaceVariant)
                }

                if (index < yaos.size - 1) {
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
private fun RowScope.MeihuaTableHeaderCell(text: String, weight: Float) {
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
private fun RowScope.MeihuaTableCell(text: String, weight: Float, color: Color) {
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
internal fun RelationMap(data: MeihuaData) {
    val relations = listOf(
        Triple("始", "用·${data.yongGua.name}", data.analysis.tiYongRelation),
        Triple("中", "互·${data.mainHexagram.lower}", data.analysis.inter1Relation),
        Triple("中", "互·${data.mainHexagram.upper}", data.analysis.inter2Relation),
        Triple("终", "变·${data.changedName}", data.analysis.changedRelation)
    )

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

                MeihuaRelationTag(relation)
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

@Composable
private fun MeihuaRelationTag(relation: String) {
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
