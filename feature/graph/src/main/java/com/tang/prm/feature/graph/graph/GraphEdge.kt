package com.tang.prm.feature.graph.graph

import com.tang.prm.domain.model.CustomType

/**
 * 图谱边。
 *
 * @property id 关系记录 ID（对应 ContactRelation.id 或 PersonRelation.id + 偏移量；负数为虚拟"我→联系人"边）
 * @property sourceId 起点节点 ID（不保证方向语义，仅作渲染连接）
 * @property targetId 终点节点 ID
 * @property relationType 关系类型（含颜色/图标/名称），用于边的着色与标签
 * @property label 可选标签（如"大学室友"），显示在边中点；null 时不绘制
 * @property isVirtual 是否为虚拟"我→联系人"边（基于 Contact.relationship 标签生成，非真实关系记录）
 * @property isPersonRelation 是否为人物关系边（PersonRelation，单向 owner→target）
 * @property isEventRelation 是否为事件↔人物边（事件节点连接到其参与者）
 */
data class GraphEdge(
    val id: Long,
    val sourceId: Long,
    val targetId: Long,
    val relationType: CustomType,
    val label: String?,
    val isVirtual: Boolean = false,
    val isPersonRelation: Boolean = false,
    val isEventRelation: Boolean = false
)
