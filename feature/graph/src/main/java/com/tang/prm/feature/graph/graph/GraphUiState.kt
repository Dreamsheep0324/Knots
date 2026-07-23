package com.tang.prm.feature.graph.graph

import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.model.IntimacyTier

/**
 * 图谱 UI 状态。
 *
 * 拆分为三部分以遵循项目 UiState 约定（参照 ContactsViewModel）：
 * - [data]：图谱数据（节点/边/类型/加载状态），由 UseCase Flow 驱动
 * - [view]：视图模式（编辑/选中/筛选/自动边/统计），由用户交互驱动
 * - [dialog]：弹窗状态（关系类型选择 sheet），由编辑流程驱动
 *
 * 拆分目的：避免数据流重组时丢失视图状态（如选中节点、编辑模式），
 * 同时让弹窗状态独立于数据，防止弹窗因数据变更被意外关闭。
 */
data class GraphUiState(
    val data: GraphDataState = GraphDataState(),
    val view: GraphViewState = GraphViewState(),
    val dialog: GraphDialogState = GraphDialogState()
)

/**
 * 图谱数据状态。
 *
 * [nodes] 与 [edges] 是物理模拟的输入，节点对象在 ForceSimulator 中被 in-place 修改位置。
 * 当数据源（contacts/relations）变化时，UseCase 重新发射，ViewModel 重建节点/边列表。
 *
 * [eventTypes]：EVENT_TYPE 自定义类型列表，用于事件节点的图标/颜色解析。
 */
data class GraphDataState(
    val isLoading: Boolean = false,
    val nodes: List<GraphNode> = emptyList(),
    val edges: List<GraphEdge> = emptyList(),
    val relationTypes: List<CustomType> = emptyList(),
    val eventTypes: List<CustomType> = emptyList(),
    val errorMessage: String? = null
)

/**
 * 图谱视图状态。
 *
 * - [isEditMode]：编辑模式下可拖拽连线添加关系、长按边删除
 * - [selectedNodeId]：当前选中节点，null 表示无选中；选中时非邻居节点变暗
 * - [activeFilter]：关系类型筛选；[RelationFilter.All] 显示全部
 * - [stats]：底部统计条数据
 * - [viewMode]：视图模式（力导向 FORCE / 星系 GALAXY）
 * - [showEventNodes]：是否显示事件节点和事件↔人物连线（仅力导向视图有效）
 */
data class GraphViewState(
    val isEditMode: Boolean = false,
    val selectedNodeId: Long? = null,
    val selectedEdgeId: Long? = null,
    val activeFilter: RelationFilter = RelationFilter.All,
    val stats: GraphStats = GraphStats(),
    val viewMode: GraphViewMode = GraphViewMode.FORCE,
    val showEventNodes: Boolean = true
)

/**
 * 图谱视图模式。
 *
 * - [FORCE]：力导向布局，节点在弹簧力/斥力/中心引力作用下自由分布
 * - [GALAXY]：星系视图，"我"为中心，其他节点按亲密度分层围绕"我"旋转（像太阳系）
 */
enum class GraphViewMode {
    FORCE,
    GALAXY
}

/**
 * 图谱弹窗状态。
 *
 * - [relationSheet]：编辑模式下选择关系类型的底部 sheet
 * - [pendingEdgeSource]：编辑模式拖拽连线时的起点节点 ID（仅用于实时反馈连线绘制）
 */
data class GraphDialogState(
    val relationSheet: RelationSheetState = RelationSheetState.Hidden,
    val pendingEdgeSource: Long? = null
)

/**
 * 图谱底部统计。
 */
data class GraphStats(
    val totalContacts: Int = 0,
    val totalRelations: Int = 0,
    val totalCircles: Int = 0
)

/**
 * 关系筛选。
 *
 * - [All]：显示全部关系
 * - [ByType]：仅显示指定类型的关系，非该类型的边变暗
 */
sealed interface RelationFilter {
    data object All : RelationFilter
    data class ByType(val typeId: Long) : RelationFilter
}

/**
 * 关系类型选择 sheet 状态。
 *
 * 编辑模式下拖拽连线后弹出，让用户选择关系类型。
 * - [Hidden]：不显示
 * - [SelectType]：显示并携带源/目标节点信息
 */
sealed interface RelationSheetState {
    data object Hidden : RelationSheetState
    data class SelectType(
        val sourceId: Long,
        val targetId: Long,
        val sourceName: String,
        val targetName: String,
        val selectedTypeId: Long?
    ) : RelationSheetState
}

/**
 * 选中节点的展示信息（用于底部信息卡 NodeInfoCard）。
 *
 * - [id] / [name] / [avatarUri]：节点基本信息
 * - [tier]：亲密度档位，决定卡片左侧色条颜色
 * - [tierLabel]：亲密度文案（如"密友"）
 * - [neighborCount]：直接关系数
 * - [topRelations]：关联最密切的若干条关系（最多 3 条），用于卡片中预览
 * - [isSelf]：是否为"我"节点；true 时不显示"查看详情"按钮
 */
data class SelectedNodeInfo(
    val id: Long,
    val name: String,
    val avatarUri: String?,
    val tier: IntimacyTier,
    val tierLabel: String,
    val neighborCount: Int,
    val topRelations: List<NeighborRelation>,
    val isSelf: Boolean = false
)

/**
 * 邻居关系摘要（用于 [SelectedNodeInfo.topRelations]）。
 */
data class NeighborRelation(
    val otherName: String,
    val relationTypeName: String,
    val relationTypeColor: String?
)

/**
 * 选中边的展示信息（用于 [EdgeInfoCard]）。
 *
 * - [id]：关系记录 ID（虚拟"我→联系人"边为负数）
 * - [sourceName] / [targetName]：两端节点名
 * - [relationTypeName] / [relationTypeColor]：关系类型
 * - [label]：可选备注（如"大学室友"）
 * - [sourceLabel]：来源标识文案（如"手动添加"/"亲密度等级"）
 */
data class SelectedEdgeInfo(
    val id: Long,
    val sourceName: String,
    val targetName: String,
    val relationTypeName: String,
    val relationTypeColor: String?,
    val label: String?,
    val sourceLabel: String
)

/**
 * 选中事件节点的展示信息（用于 [EventInfoCard]）。
 *
 * - [id]：事件节点 ID（EVENT_NODE_ID_OFFSET - eventId）
 * - [eventId]：事件原始 ID（用于跳转事件详情）
 * - [title]：事件标题
 * - [type]：事件类型
 * - [customTypeName]：自定义类型名（可选，用于智能图标匹配）
 * - [time]：事件时间戳
 * - [location]：事件地点（可选）
 * - [description]：事件描述（可选）
 * - [photos]：事件照片 URI 列表（最多取若干张用于缩略图预览）
 * - [participants]：参与者名称列表
 */
data class SelectedEventInfo(
    val id: Long,
    val eventId: Long,
    val title: String,
    val type: EventType,
    val customTypeName: String?,
    val time: Long,
    val location: String?,
    val description: String?,
    val photos: List<String>,
    val participants: List<String>
)
