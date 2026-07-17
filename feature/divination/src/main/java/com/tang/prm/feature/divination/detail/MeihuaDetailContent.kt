package com.tang.prm.feature.divination.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tang.prm.ui.components.TerminalSectionHeader

@Composable
internal fun MeihuaTiYongSection(
    tiGuaName: String,
    tiGuaElement: String,
    tiGuaNature: String,
    tiSeasonState: String,
    yongGuaName: String,
    yongGuaElement: String,
    yongGuaNature: String,
    yongSeasonState: String,
    tiYongRelation: String,
    interName: String,
    inter1Relation: String,
    inter2Relation: String,
    changedName: String,
    changedRelation: String
) {
    Spacer(modifier = Modifier.height(20.dp))

    TerminalSectionHeader("体用总断")

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

@Composable
internal fun MeihuaRelationsSection(
    tiYongRelation: String,
    yongGuaName: String,
    lowerName: String,
    upperName: String,
    inter1Relation: String,
    inter2Relation: String,
    changedName: String,
    changedRelation: String
) {
    Spacer(modifier = Modifier.height(20.dp))

    TerminalSectionHeader("事之始终")

    Spacer(modifier = Modifier.height(8.dp))

    val relations = buildList {
        if (yongGuaName.isNotEmpty()) add(Triple("始", "用·$yongGuaName", tiYongRelation))
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
