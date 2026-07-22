package com.tang.prm.feature.graph.graph

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.ContactRelation
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.model.IntimacyTier
import com.tang.prm.domain.model.PersonRelation
import com.tang.prm.domain.model.RelationSource
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.domain.repository.PersonRelationRepository
import com.tang.prm.domain.usecase.GraphData
import com.tang.prm.domain.usecase.ObserveGraphDataUseCase
import com.tang.prm.feature.graph.editing.RelationEditor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

/**
 * 图谱 ViewModel。
 *
 * 职责：
 * - 订阅 [ObserveGraphDataUseCase] 聚合数据，转换为 [GraphNode]/[GraphEdge] 渲染模型
 * - 维护视图状态（编辑模式、选中、筛选、自动边开关）
 * - 维护弹窗状态（关系类型选择 sheet）
 * - 协调编辑流程（拖拽连线 → sheet → 持久化），委托 [RelationEditor]
 * - 持有 [viewport]/[simulator] 实例供 Canvas 使用
 *
 * 不变量：
 * - "我"节点（id=[SELF_NODE_ID]）始终位于 (0,0) 且 isFixed=true，不参与物理模拟
 * - 节点位置由 [ForceSimulator] in-place 修改，ViewModel 不主动读位置
 * - 编辑模式下拖拽连线流程：startDrawingEdge → completeDrawingEdge（弹 sheet）→ confirmRelation（持久化）
 */
@HiltViewModel
class GraphViewModel @Inject constructor(
    private val observeGraphDataUseCase: ObserveGraphDataUseCase,
    private val relationEditor: RelationEditor,
    private val personRelationRepository: PersonRelationRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    companion object {
        private const val TAG = "GraphViewModel"
        /** "我"虚拟节点 ID（负数避免与真实 contact ID 冲突） */
        const val SELF_NODE_ID: Long = -1L
        /** 初始布局圆环半径（世界坐标单位）—— 配合 springLength 260~820 的分布范围 */
        private const val INITIAL_RADIUS: Float = 560f
        /**
         * PersonRelation 边的 ID 偏移量。
         *
         * GraphEdge.id 需要全局唯一以支持选中/删除：
         * - 虚拟"我→联系人"边：id = -contact.id（负数）
         * - ContactRelation 边：id = relation.id（正数，从 1 自增）
         * - PersonRelation 边：id = PERSON_RELATION_ID_OFFSET + relation.id（避免与 ContactRelation.id 冲突）
         * - 事件↔人物边：id = EVENT_EDGE_ID_OFFSET + seq（避免与上述冲突）
         */
        private const val PERSON_RELATION_ID_OFFSET: Long = 1_000_000L
        /** 事件↔人物边 ID 偏移量（正数，与 PERSON_RELATION_ID_OFFSET 区分） */
        private const val EVENT_EDGE_ID_OFFSET: Long = 2_000_000L
        /**
         * 事件节点 ID 偏移量（负数，避免与 contact.id 正数和 SELF_NODE_ID=-1 冲突）。
         * 事件节点 ID = EVENT_NODE_ID_OFFSET - eventId
         *  - eventId=1 → -100_001
         *  - eventId=50 → -100_050
         */
        private const val EVENT_NODE_ID_OFFSET: Long = -100_000L
        /** 默认加载最近事件条数（节流，避免节点爆炸） */
        private const val RECENT_EVENT_LIMIT: Int = 50
    }

    /** 视口实例（Canvas 与本 ViewModel 均可突变） */
    val viewport: GraphViewport = GraphViewport()

    /** 物理模拟引擎 */
    val simulator: ForceSimulator = ForceSimulator()

    private val _viewState = MutableStateFlow(GraphViewState())
    private val _dialogState = MutableStateFlow(GraphDialogState())
    private val _redrawTrigger = MutableStateFlow(0L)

    /** 外部重绘触发器：父组件突变 viewport 后 bump 此值 */
    val redrawTrigger: StateFlow<Long> = _redrawTrigger.asStateFlow()

    /**
     * 缓存的节点列表：跨 emit 复用同一组 GraphNode 对象，保留物理模拟/拖拽产生的位置。
     *
     * 关键设计：[observeGraphDataUseCase] 每次 emit 会触发 combine 重组，但若每次都重建
     * GraphNode 列表，节点位置会被重置为初始圆环位置，导致拖拽后位置丢失、物理模拟状态丢失。
     *
     * 策略：
     * - 首次：构建完整列表（"我"节点 + 联系人节点）
     * - 后续：按 contact id 增量同步——新增节点加入、已存在节点保留位置、删除节点剔除
     * - "我"节点恒为列表首个元素
     */
    private var cachedNodes: MutableList<GraphNode>? = null

    /**
     * 节点位置缓存：用于增量同步时保留已有节点的物理位置。
     * key = contact id（含 SELF_NODE_ID），value = GraphNode
     */
    private val nodeIndex: MutableMap<Long, GraphNode> = mutableMapOf()

    /**
     * 事件节点缓存：跨 emit 复用同一组 GraphNode 对象，保留事件节点的物理位置。
     * key = 事件节点 ID（EVENT_NODE_ID_OFFSET - event.id），value = GraphNode
     */
    private var cachedEventNodes: MutableList<GraphNode>? = null
    private val eventNodeIndex: MutableMap<Long, GraphNode> = mutableMapOf()

    /**
     * 最近事件原始对象缓存：用于 [computeSelectedEventInfo] 查找完整 Event（含 description/location/photos）。
     * 每次 uiState combine 时更新。
     */
    private var cachedRecentEvents: List<Event> = emptyList()

    val uiState: StateFlow<GraphUiState> = combine(
        observeGraphDataUseCase(),
        eventRepository.getRecentEvents(RECENT_EVENT_LIMIT),
        _viewState,
        _dialogState
    ) { data, recentEvents, view, dialog ->
        cachedRecentEvents = recentEvents
        val personNodes = data.toGraphNodesIncremental()
        val eventNodes = recentEvents.toEventNodesIncremental(personNodes)
        val allNodes = personNodes + eventNodes
        val edges = data.toGraphEdges(recentEvents)
        val stats = GraphStats(
            totalContacts = data.contacts.size,
            totalRelations = data.relations.size,
            totalCircles = data.contacts.mapNotNull { it.groupId }.distinct().size,
            manualRelations = data.relations.count { it.source == RelationSource.MANUAL }
        )
        GraphUiState(
            data = GraphDataState(
                nodes = allNodes,
                edges = edges,
                relationTypes = data.relationTypes,
                eventTypes = data.eventTypes,
                isLoading = false
            ),
            view = view.copy(stats = stats),
            dialog = dialog
        )
    }.catch { e ->
        Log.e(TAG, "图谱数据流异常", e)
        emit(GraphUiState(data = GraphDataState(errorMessage = e.message)))
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        GraphUiState(data = GraphDataState(isLoading = true))
    )

    // ---------- 视图状态变更 ----------

    fun toggleEditMode() {
        _viewState.value = _viewState.value.copy(
            isEditMode = !_viewState.value.isEditMode
        )
        // 退出编辑模式时清理拖拽连线状态
        _dialogState.value = _dialogState.value.copy(pendingEdgeSource = null)
    }

    /**
     * 切换视图模式（力导向 / 星系）。
     *
     * 切换到星系视图时清除节点/边选中状态（星系视图下不支持编辑连线）。
     */
    fun setViewMode(mode: GraphViewMode) {
        if (_viewState.value.viewMode == mode) return
        _viewState.value = _viewState.value.copy(
            viewMode = mode,
            selectedNodeId = null,
            selectedEdgeId = null
        )
    }

    /**
     * 节点点击。
     *
     * 行为：
     * - 已选中节点再次点击 → 触发 onContactClick 回调（由 Screen 处理导航）
     * - 点击新节点 → 选中并计算邻居高亮
     * - 点击"我"节点 → 仅选中，不触发导航
     * - 点击事件节点 → 仅选中（显示底部事件卡片），二次点击不导航（事件无独立详情页跳转）
     */
    fun selectNode(id: Long?, onContactClick: (Long) -> Unit) {
        val current = _viewState.value.selectedNodeId
        if (id == null) {
            _viewState.value = _viewState.value.copy(selectedNodeId = null)
            return
        }
        // 事件节点：仅选中，不导航（事件在图谱中通过底部卡片预览，无独立详情页跳转）
        val nodes = uiState.value.data.nodes
        val node = nodes.firstOrNull { it.id == id }
        val isEventNode = node?.isEvent == true
        if (current == id) {
            // 二次点击 → 导航（"我"节点和事件节点除外）
            if (id != SELF_NODE_ID && !isEventNode) {
                onContactClick(id)
            }
            return
        }
        _viewState.value = _viewState.value.copy(selectedNodeId = id, selectedEdgeId = null)
    }

    /**
     * 选中边。null 表示取消选中。
     * 选中边时同时清除节点选中，避免视觉干扰。
     */
    fun selectEdge(id: Long?) {
        _viewState.value = _viewState.value.copy(selectedEdgeId = id, selectedNodeId = null)
    }

    /**
     * 清除所有选中状态（节点和边）。
     *
     * 由点击画布空白区域触发，让用户无需点击关闭按钮即可回到全局视图。
     */
    fun clearSelection() {
        if (_viewState.value.selectedNodeId == null && _viewState.value.selectedEdgeId == null) return
        _viewState.value = _viewState.value.copy(selectedNodeId = null, selectedEdgeId = null)
    }

    /**
     * 计算选中边的展示信息（用于 EdgeInfoCard）。
     *
     * 虚拟"我→联系人"边（id < 0）显示来源标识为"亲密度等级"，
     * 真实手动边显示"手动添加"，真实自动边显示"自动推断"。
     */
    fun computeSelectedEdgeInfo(): SelectedEdgeInfo? {
        val state = uiState.value
        val edgeId = state.view.selectedEdgeId ?: return null
        val edge = state.data.edges.firstOrNull { it.id == edgeId } ?: return null
        val nodes = state.data.nodes
        val source = nodes.firstOrNull { it.id == edge.sourceId }
        val target = nodes.firstOrNull { it.id == edge.targetId }
        if (source == null || target == null) return null
        val sourceLabel = when {
            edge.isVirtual -> "亲密度等级"
            edge.isPersonRelation -> "人物关系"
            else -> "手动添加"
        }
        return SelectedEdgeInfo(
            id = edge.id,
            sourceName = if (source.isSelf) "我" else source.name,
            targetName = if (target.isSelf) "我" else target.name,
            relationTypeName = edge.relationType.name,
            relationTypeColor = edge.relationType.color,
            label = edge.label,
            isManual = edge.isManual,
            sourceLabel = sourceLabel
        )
    }

    fun setFilter(filter: RelationFilter) {
        _viewState.value = _viewState.value.copy(activeFilter = filter)
    }

    /**
     * 切换事件节点显示/隐藏。
     *
     * 隐藏时去除事件节点和事件↔人物连线，仅保留人物节点和人物间关系。
     * 仅对力导向视图有效（星系视图本身不显示事件节点）。
     */
    fun toggleEventNodes() {
        _viewState.value = _viewState.value.copy(showEventNodes = !_viewState.value.showEventNodes)
    }

    // ---------- 编辑流程 ----------

    /**
     * 开始拖拽连线（编辑模式下长按节点触发）。
     * 记录起点，等待 completeDrawingEdge 或 cancelDrawingEdge。
     */
    fun startDrawingEdge(sourceId: Long) {
        if (!_viewState.value.isEditMode) return
        _dialogState.value = _dialogState.value.copy(pendingEdgeSource = sourceId)
    }

    /**
     * 完成拖拽连线（在目标节点释放）。
     * 弹出关系类型选择 sheet。
     */
    fun completeDrawingEdge(targetId: Long) {
        val sourceId = _dialogState.value.pendingEdgeSource
        _dialogState.value = _dialogState.value.copy(pendingEdgeSource = null)
        if (sourceId == null || sourceId == targetId) return

        val nodes = uiState.value.data.nodes
        val source = nodes.firstOrNull { it.id == sourceId } ?: return
        val target = nodes.firstOrNull { it.id == targetId } ?: return
        // "我"节点不可作为关系端点（虚拟节点）
        if (source.isSelf || target.isSelf) return

        viewModelScope.launch {
            val sheet = relationEditor.prepareNewEdgeSheet(
                sourceId = sourceId,
                sourceName = source.name,
                targetId = targetId,
                targetName = target.name
            )
            if (sheet != null) {
                _dialogState.value = _dialogState.value.copy(relationSheet = sheet)
            }
        }
    }

    fun cancelDrawingEdge() {
        _dialogState.value = _dialogState.value.copy(pendingEdgeSource = null)
    }

    /**
     * 在关系类型 sheet 中确认选择。
     */
    fun confirmRelationSheet(typeId: Long, note: String? = null) {
        val sheet = _dialogState.value.relationSheet as? RelationSheetState.SelectType ?: return
        viewModelScope.launch {
            runCatching {
                relationEditor.confirmRelation(sheet, typeId, note)
            }.onFailure { Log.e(TAG, "保存关系失败", it) }
            _dialogState.value = _dialogState.value.copy(relationSheet = RelationSheetState.Hidden)
        }
    }

    fun dismissRelationSheet() {
        _dialogState.value = _dialogState.value.copy(relationSheet = RelationSheetState.Hidden)
    }

    /**
     * 删除关系（编辑模式下长按边触发）。
     *
     * ID 分段决定删除哪种关系：
     * - id < 0：虚拟"我→联系人"边，不可删除
     * - id >= [PERSON_RELATION_ID_OFFSET]：PersonRelation，调用人物关系仓库删除
     * - 其他：ContactRelation，通过 [RelationEditor] 删除
     */
    fun deleteRelation(id: Long) {
        when {
            id < 0L -> return  // 虚拟边不可删除
            id >= PERSON_RELATION_ID_OFFSET -> {
                val personRelationId = id - PERSON_RELATION_ID_OFFSET
                viewModelScope.launch {
                    runCatching { personRelationRepository.deleteById(personRelationId) }
                        .onFailure { Log.e(TAG, "删除人物关系失败", it) }
                }
            }
            else -> {
                viewModelScope.launch {
                    runCatching { relationEditor.deleteRelation(id) }
                        .onFailure { Log.e(TAG, "删除关系失败", it) }
                }
            }
        }
    }

    // ---------- 画布交互 ----------

    /**
     * 节点拖拽：直接修改节点位置（in-place）。
     * 拖拽中 isFixed=true，物理引擎不会反推回去（参考 graph-mobile-preview.html 实现）。
     * "我"节点也可拖动，拖动后由中心引力缓慢拉回中心。
     */
    fun onNodeDrag(id: Long, dxWorld: Float, dyWorld: Float) {
        val nodes = uiState.value.data.nodes
        val node = nodes.firstOrNull { it.id == id } ?: return
        node.isFixed = true
        node.vx = 0f
        node.vy = 0f
        node.x += dxWorld
        node.y += dyWorld
    }

    /**
     * 节点拖拽结束：解除临时固定，让物理引擎重新接管。
     */
    fun onNodeDragEnd(id: Long) {
        val nodes = uiState.value.data.nodes
        val node = nodes.firstOrNull { it.id == id } ?: return
        node.isFixed = false
    }

    fun onCanvasPan(dxScreen: Float, dyScreen: Float) {
        viewport.pan(dxScreen, dyScreen)
    }

    /**
     * 画布缩放：以 (focusX, focusY) 屏幕坐标为锚点，保持该点世界坐标不变。
     */
    fun onCanvasZoom(focusX: Float, focusY: Float, newScale: Float) {
        viewport.zoom(focusX, focusY, newScale)
    }

    /**
     * 重置视图：viewport 归位 + bump 重绘触发器。
     */
    fun resetView() {
        viewport.reset()
        _redrawTrigger.value++
    }

    /**
     * 居中到选中节点（若未选中则居中到原点）。
     */
    fun centerView(viewWidth: Float, viewHeight: Float) {
        val selectedId = _viewState.value.selectedNodeId
        val nodes = uiState.value.data.nodes
        val target = nodes.firstOrNull { it.id == selectedId }
        if (target != null) {
            viewport.centerOn(viewWidth, viewHeight, target.x, target.y, 1f)
        } else {
            viewport.centerOn(viewWidth, viewHeight, 0f, 0f, 1f)
        }
        _redrawTrigger.value++
    }

    /**
     * 重置布局：重新随机分布节点位置，触发物理引擎重新收敛。
     * 实现：人物节点放在初始圆环上，事件节点放在更远的外圈圆环（避免与人物节点重叠）。
     */
    fun resetLayout() {
        val nodes = uiState.value.data.nodes
        val personNodes = nodes.filter { !it.isSelf && !it.isEvent }
        val eventNodes = nodes.filter { it.isEvent }
        personNodes.forEachIndexed { index, node ->
            val angle = (2 * Math.PI * index / personNodes.size.coerceAtLeast(1)) + Math.random() * 0.3
            node.x = (INITIAL_RADIUS * cos(angle)).toFloat()
            node.y = (INITIAL_RADIUS * sin(angle)).toFloat()
            node.vx = 0f
            node.vy = 0f
            node.isFixed = false
        }
        // 事件节点在人物分布范围内随机分布，不形成独立外圈
        eventNodes.forEachIndexed { index, node ->
            val angle = (2 * Math.PI * index / eventNodes.size.coerceAtLeast(1)) + Math.random() * 0.3
            val radius = INITIAL_RADIUS * (0.7f + Math.random() * 0.6f)
            node.x = (radius * cos(angle)).toFloat()
            node.y = (radius * sin(angle)).toFloat()
            node.vx = 0f
            node.vy = 0f
            node.isFixed = false
        }
        // "我"节点归位
        nodes.firstOrNull { it.isSelf }?.let {
            it.x = 0f
            it.y = 0f
            it.vx = 0f
            it.vy = 0f
            it.isFixed = false
        }
        simulator.reset()
        _redrawTrigger.value++
    }

    /**
     * 计算变暗节点集合：选中节点时，非邻居节点变暗。
     */
    fun computeDimmedNodeIds(): Set<Long> {
        val state = uiState.value
        val selectedId = state.view.selectedNodeId ?: return emptySet()
        if (selectedId == SELF_NODE_ID) return emptySet()

        val neighborIds = mutableSetOf<Long>(selectedId, SELF_NODE_ID)
        state.data.edges.forEach { edge ->
            if (edge.sourceId == selectedId) neighborIds.add(edge.targetId)
            if (edge.targetId == selectedId) neighborIds.add(edge.sourceId)
        }
        return state.data.nodes.map { it.id }.filter { it !in neighborIds }.toSet()
    }

    /**
     * 计算选中节点的展示信息（用于底部 NodeInfoCard）。
     * 返回 null 表示无选中或选中"我"节点且无任何邻居。
     * 注意：事件节点不返回 [SelectedNodeInfo]，由 [computeSelectedEventInfo] 单独处理。
     */
    fun computeSelectedNodeInfo(): SelectedNodeInfo? {
        val state = uiState.value
        val selectedId = state.view.selectedNodeId ?: return null
        val nodes = state.data.nodes
        val node = nodes.firstOrNull { it.id == selectedId } ?: return null
        // 事件节点不显示 NodeInfoCard，改用 EventInfoCard
        if (node.isEvent) return null

        // 找出所有邻居 + 关系
        val neighbors = mutableListOf<NeighborRelation>()
        state.data.edges.forEach { edge ->
            val otherId = when {
                edge.sourceId == selectedId -> edge.targetId
                edge.targetId == selectedId -> edge.sourceId
                else -> return@forEach
            }
            val other = nodes.firstOrNull { it.id == otherId }
            if (other != null && other.id != SELF_NODE_ID) {
                neighbors.add(
                    NeighborRelation(
                        otherName = other.name,
                        relationTypeName = edge.relationType.name,
                        relationTypeColor = edge.relationType.color,
                        isManual = edge.isManual
                    )
                )
            }
        }

        return SelectedNodeInfo(
            id = node.id,
            name = node.name,
            avatarUri = node.avatarUri,
            tier = node.tier,
            tierLabel = node.tier.label,
            neighborCount = neighbors.size,
            topRelations = neighbors.take(3),
            isSelf = node.isSelf
        )
    }

    /**
     * 计算选中事件节点的展示信息（用于底部 EventInfoCard）。
     * 返回 null 表示无选中或选中的不是事件节点。
     *
     * 完整 Event 对象（含 description/location/photos）从 [cachedRecentEvents] 中反查。
     */
    fun computeSelectedEventInfo(): SelectedEventInfo? {
        val state = uiState.value
        val selectedId = state.view.selectedNodeId ?: return null
        val nodes = state.data.nodes
        val node = nodes.firstOrNull { it.id == selectedId } ?: return null
        if (!node.isEvent) return null
        val eventInfo = node.eventInfo ?: return null

        // 找出该事件的所有参与者（通过事件边反查）
        val participantIds = mutableSetOf<Long>()
        state.data.edges.forEach { edge ->
            if (edge.isEventRelation) {
                when {
                    edge.sourceId == selectedId -> participantIds.add(edge.targetId)
                    edge.targetId == selectedId -> participantIds.add(edge.sourceId)
                }
            }
        }
        val participantNames = nodes
            .filter { it.id in participantIds && !it.isSelf && !it.isEvent }
            .map { it.name }

        // 反查 eventId：节点 ID = EVENT_NODE_ID_OFFSET - eventId
        val eventId = EVENT_NODE_ID_OFFSET - node.id
        // 从缓存中获取完整 Event 对象（含 description/location/photos）
        val event = cachedRecentEvents.firstOrNull { it.id == eventId }

        return SelectedEventInfo(
            id = node.id,
            eventId = eventId,
            title = eventInfo.title,
            type = eventInfo.type,
            customTypeName = eventInfo.customTypeName,
            time = eventInfo.time,
            location = event?.location,
            description = event?.description,
            photos = event?.photos?.take(4) ?: emptyList(),
            participants = participantNames
        )
    }

    /**
     * 根据筛选条件过滤可见边。
     *
     * 展示策略：
     * - "我→联系人"虚拟边：受关系类型筛选影响
     * - "联系人↔联系人"真实边（ContactRelation + PersonRelation）：受关系类型筛选影响
     * - 事件↔人物边：始终展示（不参与关系类型筛选）
     *
     * 关系类型筛选（[RelationFilter]）：选中某个关系类型时，只展示该类型的人物关系边 + 全部事件边
     */
    fun filterEdges(edges: List<GraphEdge>): List<GraphEdge> {
        val view = _viewState.value
        // 先按事件节点显示开关过滤：隐藏时不显示任何事件相关边
        val eventFiltered = if (!view.showEventNodes) {
            edges.filter { !it.isEventRelation }
        } else {
            edges
        }
        return when (val f = view.activeFilter) {
            is RelationFilter.ByType -> eventFiltered.filter { edge ->
                edge.isEventRelation || edge.relationType.id == f.typeId
            }
            RelationFilter.All -> eventFiltered
        }
    }

    /**
     * 过滤可见节点。
     *
     * - [showEventNodes]=false 时去除所有事件节点，仅保留人物节点
     * - 星系视图本身不需要事件节点（调用方在 GraphScreen 已处理），但此函数也会兜底过滤
     */
    fun filterNodes(nodes: List<GraphNode>, @Suppress("UNUSED_PARAMETER") visibleEdges: List<GraphEdge>): List<GraphNode> {
        val view = _viewState.value
        // 隐藏事件节点开关：过滤所有事件节点
        if (!view.showEventNodes) {
            return nodes.filter { !it.isEvent }
        }
        return nodes
    }

    // ---------- 数据转换 ----------

    /**
     * 将 [GraphData] 增量同步为 [GraphNode] 列表。
     *
     * - 首次调用：构建完整列表（"我"节点 + 联系人节点）
     * - 后续调用：按 contact id 增量同步——新增节点加入圆环（按当前列表长度计算角度，避免与已有节点重叠），
     *   已存在节点保留位置/速度/固定状态，仅更新 name/avatarUri/tier；删除的节点从缓存中剔除
     * - "我"节点恒为列表首个元素
     *
     * "我"节点 isFixed=false，完全参与物理模拟（斥力/弹簧力/中心引力），
     * 但在 [ForceSimulator.integrate] 中使用更强阻尼 + [ForceSimulator.applyCenterGravity] 中更强引力，
     * 让它大致停留在画布中心，可被推动但不会漂走。
     *
     * 这样物理模拟状态与用户拖拽位置能跨数据流 emit 保留。
     */
    private fun GraphData.toGraphNodesIncremental(): List<GraphNode> {
        val cache = cachedNodes ?: run {
            // 首次构建
            val fresh = ArrayList<GraphNode>(contacts.size + 1)
            val selfNode = GraphNode(
                id = SELF_NODE_ID,
                name = "我",
                avatarUri = null,
                tier = IntimacyTier.FAMILY,
                isSelf = true,
                x = 0f,
                y = 0f,
                isFixed = false
            )
            fresh.add(selfNode)
            nodeIndex[SELF_NODE_ID] = selfNode
            contacts.forEachIndexed { index, contact ->
                val angle = 2 * Math.PI * index / contacts.size.coerceAtLeast(1)
                val node = GraphNode(
                    id = contact.id,
                    name = contact.name,
                    avatarUri = contact.avatar,
                    tier = IntimacyTier.of(contact.intimacyScore),
                    isSelf = false,
                    x = (INITIAL_RADIUS * cos(angle)).toFloat(),
                    y = (INITIAL_RADIUS * sin(angle)).toFloat()
                )
                fresh.add(node)
                nodeIndex[contact.id] = node
            }
            cachedNodes = fresh
            return fresh
        }

        // 增量同步：删除 + 更新 + 新增
        val currentIds = contacts.map { it.id }.toMutableSet()
        currentIds.add(SELF_NODE_ID)

        // 1. 删除已不存在的节点（反向遍历安全删除）
        val toRemove = cache.filter { it.id !in currentIds }
        for (node in toRemove) {
            cache.remove(node)
            nodeIndex.remove(node.id)
        }

        // 2. "我"节点始终存在，更新 tier（avatarUri/name 不变）
        nodeIndex[SELF_NODE_ID]?.let {
            it.updateProfile(name = "我", avatarUri = null, tier = IntimacyTier.FAMILY)
        }

        // 3. 更新已有节点 + 新增节点（保持顺序：先"我"，再按 contacts 顺序）
        val newCache = ArrayList<GraphNode>(contacts.size + 1)
        val selfNode = nodeIndex[SELF_NODE_ID] ?: return cache
        newCache.add(selfNode)
        contacts.forEachIndexed { index, contact ->
            val existing = nodeIndex[contact.id]
            if (existing != null) {
                // 仅更新业务字段，保留位置/速度/固定状态
                existing.updateProfile(
                    name = contact.name,
                    avatarUri = contact.avatar,
                    tier = IntimacyTier.of(contact.intimacyScore)
                )
                newCache.add(existing)
            } else {
                // 新增节点：放在外圈随机角度，避免与现有节点重叠
                val baseAngle = 2 * Math.PI * index / contacts.size.coerceAtLeast(1)
                val jitter = (Math.random() - 0.5) * 0.5
                val node = GraphNode(
                    id = contact.id,
                    name = contact.name,
                    avatarUri = contact.avatar,
                    tier = IntimacyTier.of(contact.intimacyScore),
                    isSelf = false,
                    x = (INITIAL_RADIUS * 1.2f * cos(baseAngle + jitter)).toFloat(),
                    y = (INITIAL_RADIUS * 1.2f * sin(baseAngle + jitter)).toFloat()
                )
                newCache.add(node)
                nodeIndex[contact.id] = node
            }
        }
        cachedNodes = newCache
        return newCache
    }

    /**
     * 将 [GraphData] 转换为 [GraphEdge] 列表。
     *
     * 当前图谱设计为"以我为中心的关系网络"，画布只展示"我→联系人"的星形连线。
     * 因此为每个联系人生成一条虚拟边：
     * - id = -(contact.id)，负数避免与真实 ContactRelation.id 冲突
     * - sourceId = [SELF_NODE_ID]（"我"节点）
     * - targetId = contact.id
     * - relationType 基于联系人的 [Contact.relationship] 字段匹配 [GraphData.relationTypes]：
     *   - 匹配成功：用该关系类型的颜色和名称（如"家人"=红色、"朋友"=紫色）
     *   - relationship 为空：用灰色"未标注"类型
     *   - 未匹配到：用灰色 + relationship 字符串作为名称
     * - source = [RelationSource.AUTO_EVENT]，标记为虚拟自动边（不可删除）
     *
     * 同时保留真实 relations 表的"联系人↔联系人"边数据，供 [computeSelectedNodeInfo]
     * 在选中联系人节点时展示该联系人的邻居关系（画布上不绘制）。
     *
     * 事件↔人物边（[recentEvents]）：每个事件的每个参与者生成一条边，
     * 连接事件节点（id = EVENT_NODE_ID_OFFSET - eventId）与人物节点。
     * 边的 relationType 用事件类型映射的伪 CustomType（颜色取事件类型主色）。
     */
    private fun GraphData.toGraphEdges(recentEvents: List<Event>): List<GraphEdge> {
        val typeMap = relationTypes.associateBy { it.id }
        val fallbackType = relationTypes.firstOrNull()
            ?: CustomType(category = "RELATIONSHIP", name = "关系", color = "#888888")
        // 关系类型按 name 索引，用于匹配 Contact.relationship 字段
        val typeByName = relationTypes.associateBy { it.name }
        // 1. ContactRelation 真实边（联系人↔联系人，双向对称）
        val realEdges = relations.map { relation ->
            val type = typeMap[relation.relationTypeId] ?: fallbackType
            GraphEdge(
                id = relation.id,
                sourceId = relation.contactIdA,
                targetId = relation.contactIdB,
                relationType = type,
                label = relation.note,
                source = relation.source
            )
        }
        // 2. PersonRelation 人物关系边（owner→target，单向）
        //    仅当 target 为 App 联系人（targetContactId != null）时才能作为图谱边，
        //    外部人物（targetName 非 null）无对应节点，跳过。
        //    ID 加 PERSON_RELATION_ID_OFFSET 偏移避免与 ContactRelation.id 冲突。
        //    label 显示关系类型名称（优先 customLabel，其次关系类型 name）
        val personRelationEdges = personRelations.mapNotNull { relation ->
            val targetId = relation.targetContactId ?: return@mapNotNull null
            val type = relation.relationTypeId?.let { typeMap[it] } ?: fallbackType
            // label 优先 customLabel（自由文本关系词），其次关系类型名称
            val label = relation.customLabel?.takeIf { it.isNotBlank() } ?: type.name
            GraphEdge(
                id = PERSON_RELATION_ID_OFFSET + relation.id,
                sourceId = relation.ownerContactId,
                targetId = targetId,
                relationType = type,
                label = label,
                source = RelationSource.MANUAL,
                isPersonRelation = true
            )
        }
        // 3. 虚拟"我→联系人"边（画布展示用，关系类型来自 Contact.relationship 标签）
        val selfEdges = contacts.map { contact ->
            GraphEdge(
                id = -(contact.id),
                sourceId = SELF_NODE_ID,
                targetId = contact.id,
                relationType = contactToRelationType(contact, typeByName),
                label = null,
                source = RelationSource.MANUAL,
                isVirtual = true
            )
        }
        // 4. 事件↔人物边：每个事件连接到其所有参与者
        //    仅当参与者存在于 contacts 列表时才生成边（避免连接到不存在的节点）
        //    事件类型色优先取用户自定义 EVENT_TYPE 类型的颜色，找不到时回退到默认事件类型色
        val contactIdSet = contacts.map { it.id }.toHashSet()
        val eventTypeByName = eventTypes.associateBy { it.name }
        val eventTypeByKey = eventTypes.associateBy { it.key }
        val eventEdges = mutableListOf<GraphEdge>()
        recentEvents.forEach { event ->
            val eventNodeId = EVENT_NODE_ID_OFFSET - event.id
            val eventType = eventTypeToCustomType(event, eventTypeByName, eventTypeByKey)
            event.participants.forEach { participant ->
                if (participant.id in contactIdSet) {
                    eventEdges.add(
                        GraphEdge(
                            id = EVENT_EDGE_ID_OFFSET + eventEdges.size.toLong(),
                            sourceId = eventNodeId,
                            targetId = participant.id,
                            relationType = eventType,
                            label = null,
                            source = RelationSource.MANUAL,
                            isEventRelation = true
                        )
                    )
                }
            }
        }
        return selfEdges + realEdges + personRelationEdges + eventEdges
    }

    /**
     * 将最近事件列表增量同步为事件 [GraphNode] 列表。
     *
     * 与 [toGraphNodesIncremental] 类似的增量策略：
     * - 首次：构建完整事件节点列表
     * - 后续：按 event id 增量同步——新增/更新/删除
     * - 事件节点初始位置：在联系人节点外圈随机角度，避免与人物节点重叠
     *
     * 事件节点不参与 [nodeIndex]（那是人物节点的索引），用独立的 [eventNodeIndex]。
     */
    private fun List<Event>.toEventNodesIncremental(personNodes: List<GraphNode>): List<GraphNode> {
        if (isEmpty()) {
            // 清理缓存
            val cached = cachedEventNodes
            if (cached != null) {
                cached.clear()
                eventNodeIndex.clear()
            }
            return emptyList()
        }
        val cache = cachedEventNodes ?: ArrayList<GraphNode>(size).also { cachedEventNodes = it }
        val currentIds = map { it.id }.toHashSet()

        // 1. 删除已不存在的事件节点
        // 节点 ID = EVENT_NODE_ID_OFFSET - event.id，反推 event.id = EVENT_NODE_ID_OFFSET - nodeId
        val toRemove = cache.filter { (EVENT_NODE_ID_OFFSET - it.id) !in currentIds }
        for (node in toRemove) {
            cache.remove(node)
            eventNodeIndex.remove(node.id)
        }

        // 2. 新增/更新
        val result = ArrayList<GraphNode>(size)
        forEach { event ->
            val nodeId = EVENT_NODE_ID_OFFSET - event.id
            val existing = eventNodeIndex[nodeId]
            if (existing != null) {
                // 事件节点信息不可变（构造时已传入），无需更新
                result.add(existing)
            } else {
                // 新增事件节点：在人物分布范围内随机分布，不形成独立外圈
                // 事件会通过弹簧力自然贴近其参与者
                val angle = Math.random() * 2 * Math.PI
                val radius = INITIAL_RADIUS * (0.6f + Math.random() * 0.8f).toFloat()
                val node = GraphNode(
                    id = nodeId,
                    name = event.title,
                    avatarUri = null,
                    tier = IntimacyTier.NEW, // 事件节点 tier 仅占位，不用于着色
                    isSelf = false,
                    x = (radius * cos(angle)).toFloat(),
                    y = (radius * sin(angle)).toFloat(),
                    nodeType = NodeType.EVENT,
                    eventInfo = EventNodeInfo(
                        type = event.type,
                        time = event.time,
                        title = event.title,
                        customTypeName = event.customTypeName
                    )
                )
                cache.add(node)
                eventNodeIndex[nodeId] = node
                result.add(node)
            }
        }
        return result
    }

    /**
     * 事件类型 → 伪 [CustomType]（用于事件边的着色）。
     *
     * 优先使用用户自定义的 EVENT_TYPE 类型（含用户设置的 color/icon），
     * 找不到时回退到 EventType 默认主色调（与 EventTypeStyle 对齐）。
     *
     * 匹配规则：
     * - 非 OTHER 类型：按 key == type.name 匹配，其次按 name == type.name 匹配
     * - OTHER 类型：按 customTypeName 匹配 name
     *
     * @param event 事件对象（提供 type 和 customTypeName）
     * @param typeByName 按 name 索引的用户自定义事件类型
     * @param typeByKey 按 key 索引的用户自定义事件类型
     */
    private fun eventTypeToCustomType(
        event: Event,
        typeByName: Map<String, CustomType>,
        typeByKey: Map<String, CustomType>
    ): CustomType {
        // 1. 优先查找用户自定义类型
        val custom = if (event.type != EventType.OTHER) {
            typeByKey[event.type.name] ?: typeByName[event.type.name]
        } else {
            event.customTypeName?.let { typeByName[it] }
        }
        if (custom != null) return custom

        // 2. 回退到默认事件类型色（与 EventTypeStyle 对齐）
        val (name, colorHex) = when (event.type) {
            EventType.MEETUP -> "见面" to "#22C55E"
            EventType.DINING -> "聚餐" to "#F59E0B"
            EventType.TRAVEL -> "旅游" to "#0EA5E9"
            EventType.CALL -> "通话" to "#8B5CF6"
            EventType.GIFT_SENT -> "送礼" to "#FB7185"
            EventType.GIFT_RECEIVED -> "收礼" to "#FB7185"
            EventType.CONVERSATION -> "对话记录" to "#6366F1"
            EventType.OTHER -> (event.customTypeName ?: "其他") to "#64748B"
        }
        return CustomType(
            id = -event.type.ordinal.toLong(),
            category = "EVENT",
            name = name,
            color = colorHex
        )
    }

    /**
     * 根据联系人的 [Contact.relationship] 字段匹配关系类型，用于"我→联系人"边的着色与标签。
     *
     * 匹配规则：
     * - relationship 非空且在 [typeByName] 中找到：返回匹配的 CustomType（使用其颜色和名称）
     * - relationship 非空但未找到：返回灰色 + relationship 字符串作为名称（保留用户标注）
     * - relationship 为空：返回灰色"未标注"类型
     */
    private fun contactToRelationType(
        contact: Contact,
        typeByName: Map<String, CustomType>
    ): CustomType {
        val rel = contact.relationship
        return when {
            rel.isNullOrBlank() -> CustomType(
                id = 0L,
                category = "RELATIONSHIP",
                name = "未标注",
                color = "#9CA3AF"
            )
            else -> typeByName[rel] ?: CustomType(
                id = 0L,
                category = "RELATIONSHIP",
                name = rel,
                color = "#9CA3AF"
            )
        }
    }
}
