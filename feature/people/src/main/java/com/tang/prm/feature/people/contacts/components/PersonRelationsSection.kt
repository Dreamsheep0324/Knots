package com.tang.prm.feature.people.contacts.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tang.prm.domain.model.PersonRelation

/**
 * 方案 B · 极简行式：人物关系区块容器。
 *
 * 新增界面与详情界面复用同一容器，通过 [mode] 区分行为：
 * - [PersonRelationsMode.EDITOR]：每行带删除按钮，无点击跳转
 * - [PersonRelationsMode.VIEWER]：App 联系人显示箭头并可点击跳转；外部人物无操作
 *
 * 关系类型信息通过 [typeInfoMap] 传入（id → name + color），由调用方从 CustomType 列表构建。
 */
@Composable
fun PersonRelationsSection(
    relations: List<PersonRelation>,
    typeInfoMap: Map<Long, PersonRelationTypeInfo>,
    mode: PersonRelationsMode,
    onRelationClick: (PersonRelation) -> Unit,
    onRelationDelete: (PersonRelation) -> Unit,
    modifier: Modifier = Modifier
) {
    if (relations.isEmpty()) {
        Text(
            text = "暂无人物关系，点击新增添加",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        relations.forEach { relation ->
            val typeInfo = relation.relationTypeId?.let { typeInfoMap[it] }
            PersonRelationRow(
                relation = relation,
                typeName = typeInfo?.name,
                typeColor = typeInfo?.color,
                mode = when (mode) {
                    PersonRelationsMode.EDITOR -> PersonRelationRowMode.EDITOR
                    PersonRelationsMode.VIEWER -> PersonRelationRowMode.VIEWER
                },
                onClick = { onRelationClick(relation) },
                onDelete = { onRelationDelete(relation) }
            )
        }
    }
}

enum class PersonRelationsMode { EDITOR, VIEWER }

/** 关系类型信息（id → name + color），由 CustomType 列表构建。 */
data class PersonRelationTypeInfo(
    val name: String,
    val color: androidx.compose.ui.graphics.Color?
)
