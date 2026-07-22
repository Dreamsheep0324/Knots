package com.tang.prm.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import com.tang.prm.feature.graph.graph.GraphCanvas
import com.tang.prm.feature.graph.graph.GraphNode
import com.tang.prm.feature.graph.graph.GraphViewModel
import com.tang.prm.ui.components.AppCard
import com.tang.prm.ui.theme.SignalPurple
import kotlin.math.max
import kotlin.math.min

/**
 * 首页力导向图预览（只读）。
 *
 * - 固定高度，复用 [GraphCanvas] 渲染关系图谱
 * - 只读模式：不渲染顶部栏 / 筛选 chips / 编辑模式 / 缩放控件 / 底部信息卡
 * - 隐藏事件节点与事件连线，仅展示人物关系
 * - 点击人物节点直接跳转人物详情（不显示底部信息卡，不进入"选中→再点跳转"两段式）
 * - "我"节点点击不跳转
 * - 自适应缩放：根据人物节点包围盒计算合适的 scale，确保所有节点在画布内
 * - 用 [AppCard] 包裹，顶部标题栏 + 画布 + 底部图例，与首页整体风格协调
 */
@Composable
fun HomeForceGraphPreview(
    onContactClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GraphViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val redrawTrigger by viewModel.redrawTrigger.collectAsStateWithLifecycle()

    val data = uiState.data

    // 过滤事件节点和事件边，仅保留人物节点与人物关系边
    val personNodes = remember(data.nodes) {
        data.nodes.filterNot { it.isEvent }
    }
    val personEdges = remember(data.edges, personNodes) {
        val personIds = personNodes.mapTo(HashSet()) { it.id }
        data.edges.filter { edge ->
            !edge.isEventRelation &&
                edge.sourceId in personIds &&
                edge.targetId in personIds
        }
    }

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var hasFit by remember { mutableStateOf(false) }
    val density = LocalDensity.current.density

    // fit 逻辑：用 LaunchedEffect 监听 canvasSize 和 personNodes.size，
    // 加 delay(80) 确保晚于 GraphCanvas 内部的 centerOn(scale=1) 执行，
    // 这样我们的 fit 是最后设置的，不会被覆盖。
    // 首次进入时数据还在加载（personNodes 为空），等数据加载后 personNodes 非空才 fit；
    // 切回首页时 ViewModel 有缓存，personNodes 立即非空，delay 后 fit。
    // hasFit 标志确保只 fit 一次（每次 Composable 进入 composition 时 remember 重置为 false）。
    LaunchedEffect(canvasSize, personNodes.size, hasFit) {
        if (!hasFit && canvasSize.width > 0 && canvasSize.height > 0 && personNodes.isNotEmpty()) {
            delay(80)
            resetToDeterministicCircle(personNodes)
            fitNodesToView(
                nodes = personNodes,
                viewWidth = canvasSize.width.toFloat(),
                viewHeight = canvasSize.height.toFloat(),
                viewport = viewModel.viewport,
                nodeRadiusPx = 20f * density
            )
            hasFit = true
        }
    }

    // 人物总数（不含"我"节点）
    val contactCount = remember(personNodes) {
        personNodes.count { !it.isSelf }
    }
    // 关系连线总数
    val relationCount = personEdges.size

    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .height(420.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // 顶部标题栏：图标 + 标题 + 统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SignalPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountTree,
                            contentDescription = null,
                            tint = SignalPurple,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "关系图谱",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$contactCount 人",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outline))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "$relationCount 关系",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 画布区域：自适应缩放后的力导向图
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.White)
                    .onSizeChanged { canvasSize = it }
            ) {
                GraphCanvas(
                    nodes = personNodes,
                    edges = personEdges,
                    viewport = viewModel.viewport,
                    simulator = viewModel.simulator,
                    canvasSize = canvasSize,
                    isEditMode = false,
                    selectedNodeId = null,
                    selectedEdgeId = null,
                    dimmedNodeIds = emptySet(),
                    externalRedrawTrigger = redrawTrigger,
                    eventTypes = data.eventTypes,
                    onCanvasReady = { },
                    showAmbientBackground = false,
                    onNodeClick = { id ->
                        // "我"节点不跳转；其他人物节点直接跳转详情
                        if (id != GraphViewModel.SELF_NODE_ID) {
                            onContactClick(id)
                        }
                    },
                    onNodeLongPress = { },
                    onNodeDrag = { id, dx, dy -> viewModel.onNodeDrag(id, dx, dy) },
                    onNodeDragEnd = { id -> viewModel.onNodeDragEnd(id) },
                    onCanvasPan = { dx, dy -> viewModel.onCanvasPan(dx, dy) },
                    onCanvasZoom = { focusX, focusY, newScale ->
                        viewModel.onCanvasZoom(focusX, focusY, newScale)
                    },
                    onEdgeTap = { },
                    onEdgeLongPress = { },
                    onCanvasClick = { },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 底部提示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(SignalPurple))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "点击节点查看详情",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 将节点重置为确定性圆环分布（无随机），与 GraphViewModel 首次构建节点时的公式一致：
 * angle = 2π · index / size，半径 = INITIAL_RADIUS(560f)。
 * "我"节点保持 (0,0)。确保每次进入首页时图谱起点完全相同。
 */
private fun resetToDeterministicCircle(nodes: List<GraphNode>) {
    val personNodes = nodes.filter { !it.isSelf }
    personNodes.forEachIndexed { index, node ->
        val angle = 2.0 * Math.PI * index / personNodes.size.coerceAtLeast(1)
        node.x = (560f * Math.cos(angle)).toFloat()
        node.y = (560f * Math.sin(angle)).toFloat()
        node.vx = 0f
        node.vy = 0f
        node.isFixed = false
    }
}

/**
 * 根据节点包围盒自适应缩放并居中视图。
 *
 * 计算所有节点的世界坐标包围盒，先用粗略 scale 把节点半径（px）换算为世界坐标加入包围盒，
 * 再算出能让节点完整显示在画布内的最大 scale，最后调用 centerOn 居中。
 */
private fun fitNodesToView(
    nodes: List<GraphNode>,
    viewWidth: Float,
    viewHeight: Float,
    viewport: com.tang.prm.feature.graph.graph.GraphViewport,
    nodeRadiusPx: Float
) {
    if (nodes.isEmpty()) return
    var minX = Float.MAX_VALUE
    var minY = Float.MAX_VALUE
    var maxX = Float.MIN_VALUE
    var maxY = Float.MIN_VALUE
    nodes.forEach { node ->
        minX = min(minX, node.x)
        minY = min(minY, node.y)
        maxX = max(maxX, node.x)
        maxY = max(maxY, node.y)
    }
    val centerX = (minX + maxX) / 2f
    val centerY = (minY + maxY) / 2f
    val rawContentWidth = (maxX - minX).coerceAtLeast(1f)
    val rawContentHeight = (maxY - minY).coerceAtLeast(1f)
    // 用粗略 scale 把节点半径 px 换算为世界坐标
    val roughScale = min(viewWidth / rawContentWidth, viewHeight / rawContentHeight) * 0.6f
    val nodeRadiusWorld = nodeRadiusPx / roughScale.coerceAtLeast(0.1f)
    // 包围盒扩展节点半径，留 10% 额外边距
    val paddingRatio = 0.9f
    val contentWidth = (rawContentWidth + nodeRadiusWorld * 2) / paddingRatio
    val contentHeight = (rawContentHeight + nodeRadiusWorld * 2) / paddingRatio
    val scaleByWidth = viewWidth / contentWidth
    val scaleByHeight = viewHeight / contentHeight
    val targetScale = min(scaleByWidth, scaleByHeight).coerceIn(viewport.minScale, viewport.maxScale)
    viewport.centerOn(viewWidth, viewHeight, centerX, centerY, targetScale)
}
