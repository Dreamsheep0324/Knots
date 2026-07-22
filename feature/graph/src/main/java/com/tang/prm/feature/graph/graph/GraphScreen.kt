@file:OptIn(ExperimentalMaterial3Api::class)

package com.tang.prm.feature.graph.graph

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.feature.graph.graph.components.GraphAppBar
import com.tang.prm.feature.graph.graph.components.GraphEditBanner
import com.tang.prm.feature.graph.graph.components.EdgeInfoCard
import com.tang.prm.feature.graph.graph.components.EventInfoCard
import com.tang.prm.feature.graph.graph.components.GraphFilterChips
import com.tang.prm.feature.graph.graph.components.GraphZoomControls
import com.tang.prm.feature.graph.graph.components.NodeInfoCard
import com.tang.prm.feature.graph.graph.components.RelationTypeSheet
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.TextGray

/**
 * 图谱主屏幕入口。
 *
 * 组装：
 * - [GraphAppBar]：返回 / 重置布局 / 编辑模式切换
 * - [GraphFilterChips]：关系类型筛选 + 自动边开关
 * - [GraphCanvas]：力导向布局画布（核心渲染与手势）
 * - [GraphZoomControls]：浮动缩放 + 居中
 * - [GraphLegend]：亲密度图例
 * - [NodeInfoCard]：选中节点底部信息卡
 * - [GraphStatsBar]：底部统计
 * - [GraphEditBanner]：编辑模式提示
 * - [RelationTypeSheet]：关系类型选择对话框
 */
@Composable
fun GraphScreen(
    onBack: () -> Unit,
    onContactClick: (Long) -> Unit,
    onEventClick: (Long) -> Unit = {},
    onAddContact: () -> Unit = {},
    viewModel: GraphViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val redrawTrigger by viewModel.redrawTrigger.collectAsStateWithLifecycle()

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // 每次进入图谱都强制居中（不依赖 viewport 初始状态，避免旧视口状态导致不触发）
    var hasCentered by remember { mutableStateOf(false) }
    LaunchedEffect(canvasSize, hasCentered) {
        if (!hasCentered && canvasSize.width > 0 && canvasSize.height > 0) {
            viewModel.centerView(
                viewWidth = canvasSize.width.toFloat(),
                viewHeight = canvasSize.height.toFloat()
            )
            hasCentered = true
        }
    }

    Scaffold(
        topBar = {
            GraphAppBar(
                onBack = onBack,
                isEditMode = uiState.view.isEditMode,
                viewMode = uiState.view.viewMode,
                onViewModeChange = { viewModel.setViewMode(it) },
                onResetLayout = { viewModel.resetLayout() },
                onToggleEditMode = { viewModel.toggleEditMode() }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .onSizeChanged { canvasSize = it }
        ) {
            val data = uiState.data
            when {
                data.isLoading -> LoadingState()
                data.errorMessage != null -> ErrorState(message = data.errorMessage)
                else -> GraphContent(
                    uiState = uiState,
                    redrawTrigger = redrawTrigger,
                    canvasSize = canvasSize,
                    viewModel = viewModel,
                    onContactClick = onContactClick,
                    onEventClick = onEventClick,
                    onAddContact = onAddContact
                )
            }
        }
    }

    val sheet = uiState.dialog.relationSheet
    if (sheet is RelationSheetState.SelectType) {
        RelationTypeSheet(
            state = sheet,
            relationTypes = uiState.data.relationTypes.filter { it.category == CustomCategories.RELATIONSHIP },
            onSelectType = { typeId -> viewModel.confirmRelationSheet(typeId) },
            onDismiss = { viewModel.dismissRelationSheet() }
        )
    }
}

@Composable
private fun GraphContent(
    uiState: GraphUiState,
    redrawTrigger: Long,
    canvasSize: IntSize,
    viewModel: GraphViewModel,
    onContactClick: (Long) -> Unit,
    onEventClick: (Long) -> Unit,
    onAddContact: () -> Unit
) {
    val data = uiState.data
    val view = uiState.view

    val dimmedNodeIds = remember(data.nodes, data.edges, view.selectedNodeId) {
        viewModel.computeDimmedNodeIds()
    }
    // 注意：remember key 必须包含 view.showEventNodes 和 view.activeFilter，
    // 否则切换开关/筛选时 visibleEdges/visibleNodes 不会重新计算，导致开关无效
    val visibleEdges = remember(data.edges, view.activeFilter, view.showEventNodes) {
        viewModel.filterEdges(data.edges)
    }
    val visibleNodes = remember(data.nodes, view.showEventNodes) {
        viewModel.filterNodes(data.nodes, visibleEdges)
    }
    // 星系视图专用节点列表：强制过滤掉事件节点（星系视图不显示事件节点）
    val galaxyVisibleNodes = remember(visibleNodes) {
        visibleNodes.filter { !it.isEvent }
    }

    // 选中节点信息（驱动 NodeInfoCard；事件节点返回 null）
    val selectedInfo = remember(data.nodes, data.edges, view.selectedNodeId) {
        viewModel.computeSelectedNodeInfo()
    }
    // 选中事件信息（驱动 EventInfoCard；人物节点返回 null）
    val selectedEventInfo = remember(data.nodes, data.edges, view.selectedNodeId) {
        viewModel.computeSelectedEventInfo()
    }
    // 选中边信息（驱动 EdgeInfoCard）
    val selectedEdgeInfo = remember(data.edges, data.nodes, view.selectedEdgeId) {
        viewModel.computeSelectedEdgeInfo()
    }

    // 空状态：仅有"我"节点（无联系人）时显示引导
    val isEmpty = data.nodes.size <= 1

    Box(modifier = Modifier.fillMaxSize()) {
        when (view.viewMode) {
            GraphViewMode.FORCE -> GraphCanvas(
                nodes = visibleNodes,
                edges = visibleEdges,
                viewport = viewModel.viewport,
                simulator = viewModel.simulator,
                canvasSize = canvasSize,
                isEditMode = view.isEditMode,
                selectedNodeId = view.selectedNodeId,
                selectedEdgeId = view.selectedEdgeId,
                dimmedNodeIds = dimmedNodeIds,
                externalRedrawTrigger = redrawTrigger,
                eventTypes = data.eventTypes,
                onCanvasReady = { /* 视口已由 Canvas 内部自动 centerOn */ },
                onNodeClick = { id ->
                    val pendingSource = uiState.dialog.pendingEdgeSource
                    if (pendingSource != null) {
                        viewModel.completeDrawingEdge(id)
                    } else {
                        viewModel.selectNode(id, onContactClick)
                    }
                },
                onNodeLongPress = { id ->
                    if (view.isEditMode) {
                        viewModel.startDrawingEdge(id)
                    }
                },
                onNodeDrag = { id, dx, dy -> viewModel.onNodeDrag(id, dx, dy) },
                onNodeDragEnd = { id -> viewModel.onNodeDragEnd(id) },
                onCanvasPan = { dx, dy -> viewModel.onCanvasPan(dx, dy) },
                onCanvasZoom = { focusX, focusY, newScale ->
                    viewModel.onCanvasZoom(focusX, focusY, newScale)
                },
                onEdgeTap = { id -> viewModel.selectEdge(id) },
                onEdgeLongPress = { id -> viewModel.deleteRelation(id) },
                onCanvasClick = { viewModel.clearSelection() }
            )
            GraphViewMode.GALAXY -> GalaxyCanvas(
                nodes = galaxyVisibleNodes,
                edges = visibleEdges,
                viewport = viewModel.viewport,
                canvasSize = canvasSize,
                selectedNodeId = view.selectedNodeId,
                eventTypes = data.eventTypes,
                onCanvasReady = { /* 视口已由 Canvas 内部自动 centerOn */ },
                onNodeClick = { id -> viewModel.selectNode(id, onContactClick) },
                onCanvasPan = { dx, dy -> viewModel.onCanvasPan(dx, dy) },
                onCanvasZoom = { focusX, focusY, newScale ->
                    viewModel.onCanvasZoom(focusX, focusY, newScale)
                },
                onCanvasClick = { viewModel.clearSelection() }
            )
        }

        // 空状态引导：仅有"我"节点时显示
        if (isEmpty) {
            GraphEmptyState(
                onAddContact = onAddContact,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 筛选条仅力导向视图显示（星系视图无连线概念，不需要筛选）
        // 筛选条件只展示"我对人物的标签"（RELATIONSHIP 类别），不展示人物之间的关系类型
        if (view.viewMode == GraphViewMode.FORCE && !isEmpty) {
            GraphFilterChips(
                relationTypes = data.relationTypes.filter { it.category == CustomCategories.RELATIONSHIP },
                activeFilter = view.activeFilter,
                showEventNodes = view.showEventNodes,
                onSelectFilter = { viewModel.setFilter(it) },
                onToggleEventNodes = { viewModel.toggleEventNodes() },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        if (view.isEditMode && view.viewMode == GraphViewMode.FORCE) {
            GraphEditBanner(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp)
            )
        }

        GraphZoomControls(
            onZoomIn = {
                val newScale = viewModel.viewport.scale * 1.2f
                val cx = canvasSize.width / 2f
                val cy = canvasSize.height / 2f
                viewModel.onCanvasZoom(cx, cy, newScale)
            },
            onZoomOut = {
                val newScale = viewModel.viewport.scale / 1.2f
                val cx = canvasSize.width / 2f
                val cy = canvasSize.height / 2f
                viewModel.onCanvasZoom(cx, cy, newScale)
            },
            onCenter = {
                if (canvasSize != IntSize.Zero) {
                    viewModel.centerView(
                        viewWidth = canvasSize.width.toFloat(),
                        viewHeight = canvasSize.height.toFloat()
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        )

        // 选中节点/边信息卡
        // 力导向视图：人物卡片、事件卡片、边卡片均可显示
        // 星系视图：仅人物卡片（星系视图无连线/事件节点）
        if (view.viewMode == GraphViewMode.FORCE) {
            // 选中人物节点底部信息卡（事件节点返回 null，不显示此卡）
            NodeInfoCard(
                info = selectedInfo,
                onNavigateDetail = { id -> onContactClick(id) },
                onDismiss = { viewModel.selectNode(null) { } },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // 选中事件节点底部信息卡（与 NodeInfoCard/EdgeInfoCard 互斥显示）
            EventInfoCard(
                info = selectedEventInfo,
                eventTypes = data.eventTypes,
                onNavigateDetail = { eventId -> onEventClick(eventId) },
                onDismiss = { viewModel.selectNode(null) { } },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // 选中边底部信息卡（与节点信息卡互斥显示）
            EdgeInfoCard(
                info = selectedEdgeInfo,
                onDelete = { id ->
                    viewModel.deleteRelation(id)
                    viewModel.selectEdge(null)
                },
                onDismiss = { viewModel.selectEdge(null) },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        } else if (view.viewMode == GraphViewMode.GALAXY) {
            // 星系视图：仅人物节点信息卡
            NodeInfoCard(
                info = selectedInfo,
                onNavigateDetail = { id -> onContactClick(id) },
                onDismiss = { viewModel.selectNode(null) { } },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = SignalPurple)
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = TextGray,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * 空状态引导：仅有"我"节点（无联系人）时显示。
 *
 * 视觉设计：半透明卡片 + 关系网络插画（用 Canvas 绘制简化图谱）+ 引导文案 + 跳转按钮。
 */
@Composable
private fun GraphEmptyState(
    onAddContact: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .padding(horizontal = 32.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
    ) {
        // 简化图谱插画：中心金色"我"+ 虚线连接的空位
        Canvas(
            modifier = Modifier.size(120.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val goldColor = Color(0xFFFBBF24)
            val grayColor = Color(0xFF94A3B8)

            // 4 条虚线指向空位（暗示待添加的联系人）
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), phase = 0f)
            for (i in 0 until 4) {
                val angle = (Math.PI / 2 * i + Math.PI / 4).toFloat()
                val endX = center.x + 45f * kotlin.math.cos(angle)
                val endY = center.y + 45f * kotlin.math.sin(angle)
                drawLine(
                    color = grayColor.copy(alpha = 0.4f),
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = 1.5f,
                    pathEffect = dashEffect,
                    cap = StrokeCap.Round
                )
                // 空位小圆（虚线圆）
                drawCircle(
                    color = grayColor.copy(alpha = 0.3f),
                    radius = 10f,
                    center = Offset(endX, endY),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 1.2f,
                        pathEffect = dashEffect
                    )
                )
            }
            // 中心"我"节点（金色实心圆 + 辉光）
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(goldColor.copy(alpha = 0.4f), Color.Transparent),
                    center = center,
                    radius = 35f
                ),
                center = center,
                radius = 35f
            )
            drawCircle(
                color = goldColor,
                radius = 16f,
                center = center
            )
        }

        Text(
            text = "还没有联系人",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "添加第一位朋友，开启你的关系图谱",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        androidx.compose.material3.Button(
            onClick = onAddContact,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = SignalPurple
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            androidx.compose.foundation.layout.Spacer(Modifier.width(6.dp))
            Text("添加联系人", fontWeight = FontWeight.SemiBold)
        }
    }
}
