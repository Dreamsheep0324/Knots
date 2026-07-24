package com.tang.prm.feature.graph.graph

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.tang.prm.domain.model.IntimacyTier
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.model.CustomType
import com.tang.prm.ui.theme.SignalPurple
import com.tang.prm.ui.theme.resolveEventIcon
import com.tang.prm.ui.theme.resolveEventAccentColor
import com.tang.prm.ui.theme.toComposeColor

/**
 * 图谱画布 Composable。
 *
 * 架构（参考 graph-mobile-preview.html）：
 * 1. [BackgroundCanvas]：Canvas 绘制边 + 响应画布平移/缩放
 * 2. [NodeLayer]：每个节点一个 Composable，AsyncImage 加载头像 + 响应节点拖拽/点击
 *
 * 手势分派（关键）：
 * - 节点层在 BackgroundCanvas 之上（Box 后添加的子项在上方）
 * - 节点用 detectDragGestures + detectTapGestures 两个 pointerInput
 *   Compose 自动协调：touchSlop 内不消费 down，事件可同时被两个 pointerInput 接收
 * - 节点 detectDragGestures 的 onDrag 回调中 consume change，阻止 BackgroundCanvas 接管
 * - 空白区域 down 不到节点，事件传到 BackgroundCanvas，触发画布平移
 *
 * 重绘驱动：
 * - [frameTick] mutableLongStateOf，物理模拟每帧 bump
 * - viewport 的 scale/offsetX/offsetY 是 mutableStateOf，自动触发依赖它的 Composable 重组
 */
@Suppress("LongParameterList")
@Composable
fun GraphCanvas(
    nodes: List<GraphNode>,
    edges: List<GraphEdge>,
    viewport: GraphViewport,
    simulator: ForceSimulator,
    canvasSize: IntSize,
    isEditMode: Boolean,
    selectedNodeId: Long?,
    selectedEdgeId: Long?,
    dimmedNodeIds: Set<Long>,
    externalRedrawTrigger: Long,
    eventTypes: List<CustomType> = emptyList(),
    onCanvasReady: () -> Unit,
    onNodeClick: (Long) -> Unit,
    onNodeLongPress: (Long) -> Unit,
    onNodeDrag: (Long, Float, Float) -> Unit,
    onNodeDragEnd: (Long) -> Unit,
    onCanvasPan: (Float, Float) -> Unit,
    onCanvasZoom: (Float, Float, Float) -> Unit,
    onEdgeTap: (Long) -> Unit,
    onEdgeLongPress: (Long) -> Unit,
    onCanvasClick: () -> Unit = {},
    showAmbientBackground: Boolean = true,
    autoCenterOnReady: Boolean = true,
    modifier: Modifier = Modifier
) {
    var frameTick by remember { mutableLongStateOf(0L) }
    var hasCentered by remember { mutableStateOf(false) }
    // 选中边的流动动画相位（每帧递增，驱动 dashPhase 动画）
    var flowPhase by remember { mutableStateOf(0f) }

    // 首次拿到有效尺寸时把"我"(0,0)居中到屏幕中央
    // autoCenterOnReady=false 时跳过（由调用方自行控制 viewport 初始化，避免"先放大再缩小"的割裂感）
    LaunchedEffect(canvasSize) {
        if (!hasCentered && canvasSize.width > 0 && canvasSize.height > 0) {
            if (autoCenterOnReady) {
                viewport.centerOn(
                    viewWidth = canvasSize.width.toFloat(),
                    viewHeight = canvasSize.height.toFloat(),
                    targetWorldX = 0f,
                    targetWorldY = 0f,
                    targetScale = 1f
                )
            }
            hasCentered = true
            onCanvasReady()
            frameTick++
        }
    }

    // 物理模拟主循环：每帧 step
    LaunchedEffect(nodes, edges) {
        while (true) {
            withFrameNanos { nano ->
                simulator.step(nodes, edges, centerX = 0f, centerY = 0f)
                val converged = simulator.isConverged(nodes)
                // P0-2 修复：收敛且无选中边时跳过 frameTick 写入，
                // 节点位置不再每帧重组（节点坐标未变，无需触发 BackgroundCanvas + NodeLayer 全量重组）
                if (!converged || selectedEdgeId != null) {
                    frameTick = nano
                }
                // flowPhase 持续递增：驱动所有边的流动光粒子 + 人物关系虚线脉搏波
                // 选中边时递增更快（-2.5f），增强选中态视觉冲击
                flowPhase = if (selectedEdgeId != null) {
                    (flowPhase - 2.5f) % 1000f
                } else {
                    (flowPhase - 1.2f) % 1000f
                }
            }
            if (simulator.isConverged(nodes) && selectedEdgeId == null) {
                // P0-2 修复：收敛后慢轮询，从 50ms 降到 200ms（约 5fps），
                // 仅维持边的流光动画，不触发节点位置重组
                kotlinx.coroutines.delay(200)
            }
        }
    }

    val handleNodeDrag: (Long, Float, Float) -> Unit = { id, dx, dy ->
        onNodeDrag(id, dx, dy)
        frameTick++
    }
    val handleNodeDragEnd: (Long) -> Unit = { id ->
        onNodeDragEnd(id)
        frameTick++
    }
    val handleCanvasPan: (Float, Float) -> Unit = { dx, dy ->
        onCanvasPan(dx, dy)
        frameTick++
    }
    val handleCanvasZoom: (Float, Float, Float) -> Unit = { fx, fy, s ->
        onCanvasZoom(fx, fy, s)
        frameTick++
    }

    val tick = frameTick  // 读取建立状态依赖

    Box(modifier = modifier.fillMaxSize()) {
        BackgroundCanvas(
            nodes = nodes,
            edges = edges,
            viewport = viewport,
            selectedNodeId = selectedNodeId,
            selectedEdgeId = selectedEdgeId,
            flowPhase = flowPhase,
            frameTick = tick,
            showAmbientBackground = showAmbientBackground,
            onCanvasPan = handleCanvasPan,
            onCanvasZoom = handleCanvasZoom,
            onEdgeTap = onEdgeTap,
            onEdgeLongPress = onEdgeLongPress,
            onCanvasClick = onCanvasClick
        )
        NodeLayer(
            nodes = nodes,
            viewport = viewport,
            selectedNodeId = selectedNodeId,
            dimmedNodeIds = dimmedNodeIds,
            isEditMode = isEditMode,
            frameTick = tick,
            eventTypes = eventTypes,
            onNodeClick = onNodeClick,
            onNodeLongPress = onNodeLongPress,
            onNodeDrag = handleNodeDrag,
            onNodeDragEnd = handleNodeDragEnd
        )
    }
}

@Composable
private fun BackgroundCanvas(
    nodes: List<GraphNode>,
    edges: List<GraphEdge>,
    viewport: GraphViewport,
    selectedNodeId: Long?,
    selectedEdgeId: Long?,
    flowPhase: Float,
    frameTick: Long,
    showAmbientBackground: Boolean = true,
    onCanvasPan: (Float, Float) -> Unit,
    onCanvasZoom: (Float, Float, Float) -> Unit,
    onEdgeTap: (Long) -> Unit,
    onEdgeLongPress: (Long) -> Unit,
    onCanvasClick: () -> Unit = {}
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    // 命中测试阈值：点击点到线段距离 < 14dp 视为点击该边
    val hitThresholdPx = with(density) { 14.dp.toPx() }
    // 节点半径（px）：必须与 NodeLayer 中 NodeComposable 的 size(radiusDp * 2) 一致，
    // 否则连线收缩位置与节点视觉尺寸不匹配，导致连线穿入节点或悬空。
    // 普通节点 20dp，"我"节点 17dp（更小，凸显星系中心），事件节点 18dp（略小，凸显人物为主）。
    val nodeRadiusPx = with(density) { 20.dp.toPx() }
    val selfNodeRadiusPx = with(density) { 17.dp.toPx() }
    val eventNodeRadiusPx = with(density) { 18.dp.toPx() }
    // 关键：读取 frameTick 建立状态依赖。
    // GraphNode.x 是普通 var Float，物理引擎 in-place 修改不触发重组。
    // 若不读 frameTick，Compose 会因参数（nodes/edges/viewport 对象引用）不变跳过重组，
    // 导致 draw lambda 不重新执行，边停留在旧位置——与节点视觉分离。
    val tick = frameTick

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            // 统一手势处理：detectTransformGestures 同时处理单指拖拽（pan）和双指缩放+平移
            // 避免 detectDragGestures 与 detectTransformGestures 共存时的手势消费冲突
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    if (zoom != 1f) {
                        onCanvasZoom(centroid.x, centroid.y, viewport.scale * zoom)
                    }
                    if (pan != Offset.Zero) {
                        onCanvasPan(pan.x, pan.y)
                    }
                }
            }
            .pointerInput(edges, viewport, hitThresholdPx) {
                detectTapGestures(
                    onTap = { offset ->
                        val hit = findHitEdge(offset, edges, nodes, viewport, hitThresholdPx)
                        if (hit != null) {
                            onEdgeTap(hit.id)
                        } else {
                            // 点击空白区域：清除选中
                            onCanvasClick()
                        }
                    },
                    onLongPress = { offset ->
                        val hit = findHitEdge(offset, edges, nodes, viewport, hitThresholdPx)
                        if (hit != null) onEdgeLongPress(hit.id)
                    }
                )
            }
    ) {
        val nodeIndex = nodes.associateBy { it.id }
        // 引用 tick 确保 draw lambda 随 frameTick 变化重新执行（读取最新 node.x/y）
        val currentTick = tick

        // ── 背景氛围层：径向渐变（中心柔光）──
        // showAmbientBackground=false 时跳过，让背景保持纯白（首页预览用）
        if (showAmbientBackground) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val maxRadius = kotlin.math.max(size.width, size.height)
            drawRect(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC).copy(alpha = 0.6f),
                        Color(0xFFEEF2F7).copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(centerX, centerY),
                    radius = maxRadius * 0.7f
                ),
                size = size
            )

            // ── 背景氛围层：点阵纹理（随缩放密度变化）──
            drawDotGrid(viewport, size)
        }

        // 渲染所有边（选中边由 drawSelectedEdgeGlow 单独绘制，drawEdgeLine 内部会跳过）
        // 渲染顺序：虚拟"我→联系人"边（底层）→ 真实关系边（上层）
        for (edge in edges) {
            if (!edge.isVirtual) continue
            drawEdgeLine(edge, nodeIndex, viewport, selectedNodeId, selectedEdgeId, flowPhase,
                nodeRadiusPx = nodeRadiusPx, selfNodeRadiusPx = selfNodeRadiusPx,
                eventNodeRadiusPx = eventNodeRadiusPx)
        }
        for (edge in edges) {
            if (edge.isVirtual) continue
            drawEdgeLine(edge, nodeIndex, viewport, selectedNodeId, selectedEdgeId, flowPhase,
                nodeRadiusPx = nodeRadiusPx, selfNodeRadiusPx = selfNodeRadiusPx,
                eventNodeRadiusPx = eventNodeRadiusPx)
        }
        // 选中边的发光层 + 流动动画 + 标签（最上层）
        if (currentTick >= 0L) {
            for (edge in edges) {
                if (edge.id != selectedEdgeId) continue
                drawSelectedEdgeGlow(edge, nodeIndex, viewport, flowPhase,
                    nodeRadiusPx = nodeRadiusPx, selfNodeRadiusPx = selfNodeRadiusPx,
                    eventNodeRadiusPx = eventNodeRadiusPx)
                drawEdgeLabelIfHighlighted(edge, nodeIndex, viewport, selectedNodeId ?: edge.sourceId, textMeasurer)
            }
        }
    }
}

/**
 * 命中测试：找到距离点击位置最近的边（距离 < threshold）。
 *
 * 算法：计算点 P 到线段 AB 的最短距离。
 * - 先将节点世界坐标转为屏幕坐标
 * - 若 P 在 AB 的垂足落在线段内，距离 = |AP·垂直方向|
 * - 否则距离 = min(|PA|, |PB|)
 *
 * @return 命中的边，或 null
 */
private fun findHitEdge(
    point: Offset,
    edges: List<GraphEdge>,
    nodes: List<GraphNode>,
    viewport: GraphViewport,
    threshold: Float
): GraphEdge? {
    val nodeIndex = nodes.associateBy { it.id }
    var bestEdge: GraphEdge? = null
    var bestDist = threshold
    for (edge in edges) {
        val a = nodeIndex[edge.sourceId] ?: continue
        val b = nodeIndex[edge.targetId] ?: continue
        val ax = viewport.worldToScreenX(a.x)
        val ay = viewport.worldToScreenY(a.y)
        val bx = viewport.worldToScreenX(b.x)
        val by = viewport.worldToScreenY(b.y)
        val dist = distancePointToSegment(point.x, point.y, ax, ay, bx, by)
        if (dist < bestDist) {
            bestDist = dist
            bestEdge = edge
        }
    }
    return bestEdge
}

/**
 * 计算点 P(px,py) 到线段 AB(ax,ay)-(bx,by) 的最短距离。
 */
private fun distancePointToSegment(
    px: Float, py: Float,
    ax: Float, ay: Float,
    bx: Float, by: Float
): Float {
    val dx = bx - ax
    val dy = by - ay
    val lenSq = dx * dx + dy * dy
    if (lenSq < 1e-5f) {
        // A、B 重合，退化到点到点距离
        return kotlin.math.hypot(px - ax, py - ay)
    }
    // 投影参数 t ∈ [0,1] 表示垂足在线段上的位置
    var t = ((px - ax) * dx + (py - ay) * dy) / lenSq
    t = t.coerceIn(0f, 1f)
    val projX = ax + t * dx
    val projY = ay + t * dy
    return kotlin.math.hypot(px - projX, py - projY)
}

/**
 * 绘制单条边线段（无标签）。
 *
 * 视觉规则（P1 增强版：渐变色 + 宽度梯度）：
 * - 虚拟"我→联系人"边（id<0 或 sourceId=SELF_NODE_ID）：
 *   实线，宽度按目标 tier 递减（FAMILY 3.0 → NEW 1.6），
 *   渐变色（金色 → tier 色），opacity 0.8
 * - 真实手动边（isManual）：实线，关系类型色渐变，opacity 1.0
 * - 真实自动边：虚线 1.8px，tier 色渐变，opacity 0.5（替代灰色，让关系强弱可感知）
 * - 选中节点相关：opacity 1.0，宽度 +1px
 * - 选中边：跳过（由 drawSelectedEdgeGlow 单独绘制）
 * - 选中节点/边时非相关边：opacity 0.08
 */
private fun DrawScope.drawEdgeLine(
    edge: GraphEdge,
    nodeIndex: Map<Long, GraphNode>,
    viewport: GraphViewport,
    selectedNodeId: Long?,
    selectedEdgeId: Long?,
    flowPhase: Float,
    nodeRadiusPx: Float,
    selfNodeRadiusPx: Float,
    eventNodeRadiusPx: Float
) {
    // 选中边由 drawSelectedEdgeGlow 单独绘制，此处跳过避免重叠
    if (edge.id == selectedEdgeId) return

    val a = nodeIndex[edge.sourceId] ?: return
    val b = nodeIndex[edge.targetId] ?: return
    if (!viewport.isVisible(a.x, a.y, size.width, size.height) &&
        !viewport.isVisible(b.x, b.y, size.width, size.height)) return

    val sx1 = viewport.worldToScreenX(a.x)
    val sy1 = viewport.worldToScreenY(a.y)
    val sx2 = viewport.worldToScreenX(b.x)
    val sy2 = viewport.worldToScreenY(b.y)

    // 边起点/终点收缩到节点边缘（事件节点用更小的半径）
    val r1 = when {
        a.isSelf -> selfNodeRadiusPx
        a.isEvent -> eventNodeRadiusPx
        else -> nodeRadiusPx
    }
    val r2 = when {
        b.isSelf -> selfNodeRadiusPx
        b.isEvent -> eventNodeRadiusPx
        else -> nodeRadiusPx
    }
    val (start, end) = shrinkLineToNodeEdges(sx1, sy1, sx2, sy2, r1, r2)

    val isRelated = selectedNodeId != null &&
        (edge.sourceId == selectedNodeId || edge.targetId == selectedNodeId)
    val isDimmed = (selectedNodeId != null && !isRelated) || (selectedEdgeId != null)

    // 边类型判定：
    // - isVirtual：虚拟"我→联系人"边（基于 Contact.relationship 标签），实线金色渐变
    // - isPersonRelation：人物关系边（PersonRelation），虚线关系色
    // - isEventRelation：事件↔人物边，虚线事件类型色 + 流光
    // - 其他：ContactRelation 真实关系边，实线关系色
    val isSelfEdge = edge.isVirtual

    // 关系类型色（fallback 到紫/灰）
    val relationColor = edge.relationType.color.toComposeColor(Color(0xFF8B5CF6L))

    // 端点颜色：用于渐变（"我"端金色，联系人端 tier 色，事件端事件类型色）
    val selfGold = Color(0xFFFBBF24)
    val tierColorA = Color(a.tier.colorValue)
    val tierColorB = Color(b.tier.colorValue)

    // 边宽度按 tier 递减（亲密度越高边越粗）；事件边统一较细，凸显人物关系主导
    val tierWidth = tierEdgeWidth(if (a.isSelf) b.tier else if (b.isSelf) a.tier else a.tier)

    val alpha = when {
        isDimmed -> 0.08f
        isRelated -> 1.0f
        edge.isEventRelation -> 0.7f  // 事件边默认略淡，避免抢人物关系的视觉重心
        else -> 0.85f
    }
    val strokeWidth = when {
        isRelated -> tierWidth + 1f
        isSelfEdge -> tierWidth
        edge.isPersonRelation -> 2.0f
        edge.isEventRelation -> 1.6f  // 事件边更细
        else -> 2.5f
    }

    // 渐变色端点：
    // - isSelfEdge：金色→tier 色
    // - isEventRelation：事件类型色（两端同色，因事件边只连事件↔人物）
    // - 其他：关系色渐变
    val (colorA, colorB) = when {
        isSelfEdge -> if (a.isSelf) selfGold to tierColorB else tierColorA to selfGold
        edge.isEventRelation -> relationColor to relationColor
        else -> relationColor to relationColor
    }

    // 高 tier 边光晕扩散：FAMILY/CLOSE 边的外层光晕加粗
    val tier = if (a.isSelf) b.tier else if (b.isSelf) a.tier else a.tier
    val isHighTier = !edge.isEventRelation && (tier == com.tang.prm.domain.model.IntimacyTier.FAMILY ||
        tier == com.tang.prm.domain.model.IntimacyTier.CLOSE)
    val haloExtra = if (isHighTier) 5f else 3f

    // 人物关系边 + 事件边使用虚线 + 脉搏波，其他边使用实线
    val useDash = edge.isPersonRelation || edge.isEventRelation
    if (useDash) {
        // 虚线边：dashPhase 随 flowPhase 变化形成脉搏波流动
        val dashPhase = flowPhase * 1.2f
        // 事件边用更短的 dash 节奏，与人物关系边区分
        val dashPattern = if (edge.isEventRelation) floatArrayOf(4f, 4f) else floatArrayOf(6f, 5f)
        drawLine(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(colorA.copy(alpha = alpha), colorB.copy(alpha = alpha)),
                start = start,
                end = end
            ),
            start = start,
            end = end,
            strokeWidth = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(dashPattern, phase = dashPhase),
            cap = StrokeCap.Round
        )
    } else {
        // 实线边：先画柔和光晕（高 tier 加粗），再画主线（渐变色）
        if (alpha > 0.5f) {
            drawLine(
                color = colorA.copy(alpha = alpha * 0.22f),
                start = start,
                end = end,
                strokeWidth = strokeWidth + haloExtra,
                cap = StrokeCap.Round
            )
        }
        drawLine(
            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                colors = listOf(colorA.copy(alpha = alpha), colorB.copy(alpha = alpha)),
                start = start,
                end = end
            ),
            start = start,
            end = end,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // ── 边流光系统：所有边持续流动光粒子 ──
    // 选中态相关边：3 个强光粒子（alpha 0.9）
    // 普通显示边：1 个弱光粒子（alpha 0.5），营造"能量流动"氛围
    // 事件边粒子使用事件类型色（而非白色），强化事件类型识别
    if (alpha > 0.3f && !isDimmed) {
        val particleCount = if (isRelated) 3 else 1
        val particleAlpha = if (isRelated) 0.9f else 0.5f
        val particleRadius = if (isRelated) 2.8f else 2.0f
        // 事件边粒子用事件类型色，人物关系边/真实边用白色
        val particleColor = if (edge.isEventRelation) relationColor else Color.White
        // flowPhase 范围 [-1000, 0]，取模映射到 [0, 1] 进度
        val baseProgress = ((-flowPhase % 1000f) / 1000f).coerceIn(0f, 1f)
        for (i in 0 until particleCount) {
            val t = ((baseProgress + i.toFloat() / particleCount) % 1f)
            val px = start.x + (end.x - start.x) * t
            val py = start.y + (end.y - start.y) * t
            // 外层柔光
            drawCircle(
                color = particleColor.copy(alpha = particleAlpha * 0.3f),
                radius = particleRadius * 2.2f,
                center = Offset(px, py)
            )
            // 内层亮点
            drawCircle(
                color = particleColor.copy(alpha = particleAlpha),
                radius = particleRadius,
                center = Offset(px, py)
            )
        }
    }
}

/**
 * 亲密度 tier → 边宽度（px）。亲密度越高边越粗，关系越紧密。
 */
private fun tierEdgeWidth(tier: com.tang.prm.domain.model.IntimacyTier): Float = when (tier) {
    com.tang.prm.domain.model.IntimacyTier.FAMILY -> 3.0f
    com.tang.prm.domain.model.IntimacyTier.CLOSE -> 2.7f
    com.tang.prm.domain.model.IntimacyTier.FRIEND -> 2.4f
    com.tang.prm.domain.model.IntimacyTier.ACQUAINTANCE -> 2.0f
    com.tang.prm.domain.model.IntimacyTier.NEW -> 1.6f
}

/**
 * 绘制点阵背景纹理。
 *
 * 视觉效果：世界坐标中的点阵网格，随缩放密度变化，营造空间深度感。
 * - 点阵间距：世界坐标 60f 单位
 * - 点半径：1.2px，alpha 0.18，颜色浅灰
 * - 仅绘制屏幕可见范围内的点阵（性能优化）
 */
private fun DrawScope.drawDotGrid(
    viewport: GraphViewport,
    canvasSize: androidx.compose.ui.geometry.Size
) {
    // 屏幕像素恒定方案：无论缩放多少，屏幕上网格点间距视觉一致（约 48px）
    // 世界间距 = 屏幕间距 / scale，缩小时世界间距变大（覆盖更大范围），放大时变小（更精细）
    val screenSpacing = 48f
    val gridSpacing = (screenSpacing / viewport.scale).coerceIn(30f, 240f)
    // 点半径固定屏幕像素，不随缩放变化
    val dotRadius = 1.4f
    // alpha 随缩放微调：极度缩小时更淡（避免密集），极度放大时稍清晰
    val baseAlpha = 0.22f
    val alphaScale = when {
        viewport.scale < 0.5f -> 0.6f
        viewport.scale > 2.5f -> 1.2f
        else -> 1f
    }
    val dotColor = Color(0xFF94A3B8).copy(alpha = (baseAlpha * alphaScale).coerceAtMost(0.35f))

    // 计算屏幕可见范围对应的世界坐标范围
    val worldLeft = viewport.offsetX - (canvasSize.width / 2f) / viewport.scale
    val worldRight = viewport.offsetX + (canvasSize.width / 2f) / viewport.scale
    val worldTop = viewport.offsetY - (canvasSize.height / 2f) / viewport.scale
    val worldBottom = viewport.offsetY + (canvasSize.height / 2f) / viewport.scale

    // 对齐到网格
    val startX = kotlin.math.floor(worldLeft / gridSpacing) * gridSpacing
    val startY = kotlin.math.floor(worldTop / gridSpacing) * gridSpacing

    var wx = startX
    while (wx <= worldRight) {
        var wy = startY
        while (wy <= worldBottom) {
            val sx = viewport.worldToScreenX(wx)
            val sy = viewport.worldToScreenY(wy)
            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = Offset(sx, sy)
            )
            wy += gridSpacing
        }
        wx += gridSpacing
    }
}

/**
 * 将线段两端沿连线方向收缩到节点边缘。
 *
 * 视觉上让边"从节点边缘出发，到节点边缘结束"，而不是从中心被圆形遮挡。
 *
 * @param r1 起点节点半径（像素）
 * @param r2 终点节点半径（像素）
 * @return 收缩后的 (start, end)
 */
private fun shrinkLineToNodeEdges(
    x1: Float, y1: Float,
    x2: Float, y2: Float,
    r1: Float, r2: Float
): Pair<Offset, Offset> {
    val dx = x2 - x1
    val dy = y2 - y1
    val dist = kotlin.math.hypot(dx, dy).coerceAtLeast(0.001f)
    val ux = dx / dist  // 单位向量 x
    val uy = dy / dist  // 单位向量 y
    val startX = x1 + ux * r1
    val startY = y1 + uy * r1
    val endX = x2 - ux * r2
    val endY = y2 - uy * r2
    return Offset(startX, startY) to Offset(endX, endY)
}

/**
 * 绘制选中边的发光效果 + 流动虚线 + 流动光粒子。
 *
 * 视觉创新设计（四层叠加）：
 * 1. 外层光晕：14px 宽，关系色 alpha 0.18，营造发光感
 * 2. 中层主线：5px 宽，关系色渐变 alpha 1.0
 * 3. 顶层流动虚线：白色 alpha 0.85，dashPathEffect 动画，营造"能量流动"
 * 4. 流动光粒子：3 个发光小圆沿边路径运动，增强能量感
 *
 * @param phase 流动相位（每帧递减，负方向流动）
 */
private fun DrawScope.drawSelectedEdgeGlow(
    edge: GraphEdge,
    nodeIndex: Map<Long, GraphNode>,
    viewport: GraphViewport,
    phase: Float,
    nodeRadiusPx: Float,
    selfNodeRadiusPx: Float,
    eventNodeRadiusPx: Float
) {
    val a = nodeIndex[edge.sourceId] ?: return
    val b = nodeIndex[edge.targetId] ?: return
    val sx1 = viewport.worldToScreenX(a.x)
    val sy1 = viewport.worldToScreenY(a.y)
    val sx2 = viewport.worldToScreenX(b.x)
    val sy2 = viewport.worldToScreenY(b.y)

    val r1 = when {
        a.isSelf -> selfNodeRadiusPx
        a.isEvent -> eventNodeRadiusPx
        else -> nodeRadiusPx
    }
    val r2 = when {
        b.isSelf -> selfNodeRadiusPx
        b.isEvent -> eventNodeRadiusPx
        else -> nodeRadiusPx
    }
    val (start, end) = shrinkLineToNodeEdges(sx1, sy1, sx2, sy2, r1, r2)

    val isSelfEdge = edge.id < 0L || edge.sourceId == -1L || edge.targetId == -1L
    val relationColor = edge.relationType.color.toComposeColor(Color(0xFF8B5CF6L))
    val selfGold = Color(0xFFFBBF24)
    val tierColorA = Color(a.tier.colorValue)
    val tierColorB = Color(b.tier.colorValue)

    // 渐变色端点：事件边用事件类型色两端同色
    val (colorA, colorB) = when {
        isSelfEdge -> if (a.isSelf) selfGold to tierColorB else tierColorA to selfGold
        edge.isEventRelation -> relationColor to relationColor
        else -> relationColor to relationColor
    }

    // 1. 外层光晕（更宽更柔）
    drawLine(
        color = colorA.copy(alpha = 0.18f),
        start = start,
        end = end,
        strokeWidth = 14f,
        cap = StrokeCap.Round
    )
    // 2. 中层主线（渐变色）
    drawLine(
        brush = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(colorA, colorB),
            start = start,
            end = end
        ),
        start = start,
        end = end,
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )
    // 3. 顶层流动虚线（白色能量流；事件边用更细更柔的虚线，凸显其"轻量"属性）
    val dashWidth = when {
        edge.isEventRelation -> 6f
        else -> 12f
    }
    val dashGap = when {
        edge.isEventRelation -> 5f
        else -> 8f
    }
    val flowWidth = when {
        edge.isEventRelation -> 1.4f
        else -> 2.2f
    }
    val flowAlpha = when {
        edge.isEventRelation -> 0.75f
        else -> 0.85f
    }
    drawLine(
        color = Color.White.copy(alpha = flowAlpha),
        start = start,
        end = end,
        strokeWidth = flowWidth,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashWidth, dashGap), phase = phase),
        cap = StrokeCap.Round
    )

    // 4. 流动光粒子：3 个发光小圆沿边路径运动
    // 事件边粒子用事件类型色，强化类型识别
    // phase 范围 [-1000, 0]，映射到 [0, 1] 的粒子位置 t
    val particleColor = if (edge.isEventRelation) relationColor else Color.White
    val particleProgress = ((-phase % 1000f) / 1000f).coerceIn(0f, 1f)
    val particleCount = 3
    for (i in 0 until particleCount) {
        val t = ((particleProgress + i.toFloat() / particleCount) % 1f)
        val px = start.x + (end.x - start.x) * t
        val py = start.y + (end.y - start.y) * t
        // 粒子发光（外层柔光 + 内层亮点）
        drawCircle(
            color = particleColor.copy(alpha = 0.25f),
            radius = 6f,
            center = Offset(px, py)
        )
        drawCircle(
            color = particleColor.copy(alpha = 0.9f),
            radius = 2.5f,
            center = Offset(px, py)
        )
    }
}

/**
 * 高亮状态下绘制边标签。
 * 仅当 selectedNodeId 与该边相关 且 edge 为 manual 时绘制。
 *
 * 标签样式（对齐 HTML）：
 * - 白色圆角背景（rx=8，alpha 0.92）
 * - 文字 9sp（约 18px @2x），#4A4A4A
 * - 居中于边中点
 */
private fun DrawScope.drawEdgeLabelIfHighlighted(
    edge: GraphEdge,
    nodeIndex: Map<Long, GraphNode>,
    viewport: GraphViewport,
    selectedNodeId: Long?,
    textMeasurer: TextMeasurer
) {
    val sid = selectedNodeId ?: return
    if (edge.sourceId != sid && edge.targetId != sid) return
    val a = nodeIndex[edge.sourceId] ?: return
    val b = nodeIndex[edge.targetId] ?: return
    val label = edge.label?.takeIf { it.isNotBlank() } ?: edge.relationType.name.takeIf { it.isNotBlank() } ?: return

    val midX = (viewport.worldToScreenX(a.x) + viewport.worldToScreenX(b.x)) / 2f
    val midY = (viewport.worldToScreenY(a.y) + viewport.worldToScreenY(b.y)) / 2f

    val textResult = textMeasurer.measure(
        text = AnnotatedString(label),
        style = androidx.compose.ui.text.TextStyle(
            color = Color(0xFF4A4A4AL),
            fontSize = 11.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
    )
    val textWidth = textResult.size.width.toFloat()
    val textHeight = textResult.size.height.toFloat()
    val padX = 8f
    val padY = 3f
    val bgLeft = midX - textWidth / 2f - padX
    val bgTop = midY - textHeight / 2f - padY
    val bgWidth = textWidth + padX * 2
    val bgHeight = textHeight + padY * 2

    drawRoundRect(
        color = Color.White.copy(alpha = 0.92f),
        topLeft = Offset(bgLeft, bgTop),
        size = androidx.compose.ui.geometry.Size(bgWidth, bgHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
    )
    drawText(
        textLayoutResult = textResult,
        topLeft = Offset(midX - textWidth / 2f, midY - textHeight / 2f)
    )
}

@Composable
private fun NodeLayer(
    nodes: List<GraphNode>,
    viewport: GraphViewport,
    selectedNodeId: Long?,
    dimmedNodeIds: Set<Long>,
    isEditMode: Boolean,
    frameTick: Long,
    eventTypes: List<CustomType>,
    onNodeClick: (Long) -> Unit,
    onNodeLongPress: (Long) -> Unit,
    onNodeDrag: (Long, Float, Float) -> Unit,
    onNodeDragEnd: (Long) -> Unit
) {
    val scale = viewport.scale
    val offsetX = viewport.offsetX
    val offsetY = viewport.offsetY
    val density = LocalDensity.current
    val nodeRadiusDp = 20.dp
    val selfNodeRadiusDp = 17.dp
    val eventNodeRadiusDp = 18.dp
    // 关键：radiusPx 必须用 density 把 dp 转 px，与 Box.size(radiusDp * 2) 保持一致。
    // 之前写死 26f（px），但 Box 尺寸是 26.dp（≈67px @density=2.6），
    // 导致 offset(sx - 26) 把节点中心放偏了 41px，与连线错位。
    val nodeRadiusPx = with(density) { nodeRadiusDp.toPx() }
    val selfNodeRadiusPx = with(density) { selfNodeRadiusDp.toPx() }
    val eventNodeRadiusPx = with(density) { eventNodeRadiusDp.toPx() }

    // 读取 frameTick 建立状态依赖：物理引擎每帧 in-place 修改 node.x/y，
    // 但 GraphNode.x 是普通 var Float 不触发重组，所以需要 frameTick 驱动 NodeLayer 重组
    val tick = frameTick

    Box(modifier = Modifier.fillMaxSize()) {
        nodes.forEach { node ->
            val radiusDp = when {
                node.isSelf -> selfNodeRadiusDp
                node.isEvent -> eventNodeRadiusDp
                else -> nodeRadiusDp
            }
            val radiusPx = when {
                node.isSelf -> selfNodeRadiusPx
                node.isEvent -> eventNodeRadiusPx
                else -> nodeRadiusPx
            }
            val sx = (node.x - offsetX) * scale
            val sy = (node.y - offsetY) * scale

            NodeComposable(
                node = node,
                sx = sx,
                sy = sy,
                radiusDp = radiusDp,
                radiusPx = radiusPx,
                scale = scale,
                isSelected = node.id == selectedNodeId,
                isDimmed = node.id in dimmedNodeIds,
                eventTypes = eventTypes,
                onNodeClick = onNodeClick,
                onNodeLongPress = onNodeLongPress,
                onNodeDrag = onNodeDrag,
                onNodeDragEnd = onNodeDragEnd
            )
        }
    }
}

/**
 * 单个节点 Composable。
 *
 * 视觉层次（从外到内）：
 * 1. 拖拽放大发光（isDragging 时 scale 1.15 + 阴影增强 + 金色辉光）
 * 2. 松手回弹（animateFloatAsState + spring，从放大状态回弹到 1.0）
 * 3. 选中态光环（animated ring，仅 isSelected 时显示）
 * 4. 阴影（柔和投影，"我"节点更深）
 * 5. 外边框（tier 色，2dp，"我"节点为金色 3dp）
 * 6. 内边框（白色 1.5dp，增加层次感）
 * 7. 头像（AsyncImage）或 placeholder（tier 色背景 + 首字母/"我"）
 *
 * "我"节点特殊处理：
 * - 外边框为金色（0xFFF59E0B）
 * - 阴影更深（elevation 8dp）
 * - 头像 placeholder 显示"我"字
 * - 边框宽度 3dp（普通节点 2dp）
 *
 * 手势策略：
 * - detectDragGestures + detectTapGestures 两个 pointerInput
 * - onDrag 中 change.consume() 阻止 BackgroundCanvas 抢事件
 * - 空白区域事件传到 BackgroundCanvas 触发画布平移
 */
@Composable
private fun NodeComposable(
    node: GraphNode,
    sx: Float,
    sy: Float,
    radiusDp: androidx.compose.ui.unit.Dp,
    radiusPx: Float,
    scale: Float,
    isSelected: Boolean,
    isDimmed: Boolean,
    eventTypes: List<CustomType>,
    onNodeClick: (Long) -> Unit,
    onNodeLongPress: (Long) -> Unit,
    onNodeDrag: (Long, Float, Float) -> Unit,
    onNodeDragEnd: (Long) -> Unit
) {
    val alpha = if (isDimmed) 0.35f else 1f
    val context = LocalContext.current
    val tierColor = Color(node.tier.colorValue)
    val selfGoldColor = Color(0xFFF59E0B)
    // 事件节点：用事件类型主色 + 智能匹配的事件类型图标
    // 注意：getEventTypeStyle 是 @Composable 函数，不能在 if 条件分支中调用
    // （Compose 调用位置追踪在条件分支中行为不一致），因此始终调用，仅在 isEvent 时使用返回值
    val eventType = node.eventInfo?.type ?: EventType.OTHER
    // 智能图标：优先使用用户自定义类型的 icon 字段，其次按关键词匹配
    val eventIcon = resolveEventIcon(
        type = eventType,
        customTypeName = node.eventInfo?.customTypeName,
        title = node.eventInfo?.title,
        eventTypes = eventTypes
    )
    // 事件节点颜色：优先使用用户自定义类型的 color 字段
    val eventColor = resolveEventAccentColor(eventType, node.eventInfo?.customTypeName, eventTypes)
    val nodeColor = when {
        node.isSelf -> selfGoldColor
        node.isEvent -> eventColor
        else -> tierColor
    }
    val borderColor = nodeColor
    val borderWidth = if (node.isSelf) 3.dp else if (isSelected) 2.5.dp else 2.dp
    val elevation = if (isSelected) 10.dp else if (node.isSelf) 8.dp else 4.dp

    // 节点名称首字（用于 placeholder）
    val placeholderText = if (node.isSelf) "我" else node.name.firstOrNull()?.toString() ?: "?"
    // 事件节点始终显示呼吸光晕；人物节点仅高 tier(FAMILY/CLOSE) 显示
    val isHighTier = node.isEvent ||
        node.tier == IntimacyTier.FAMILY || node.tier == IntimacyTier.CLOSE

    // ── 节点光晕系统：4 类无限动画 ──
    // 1. 高 tier(FAMILY/CLOSE) 呼吸光晕：alpha 在 0.2~0.55 之间正弦呼吸
    val breathTransition = rememberInfiniteTransition(label = "node_breath")
    val breathAlpha by breathTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_alpha"
    )

    // 2. "我"节点金色脉冲光环：从节点边缘向外扩散并渐隐
    val pulseTransition = rememberInfiniteTransition(label = "self_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    // 3. 选中节点旋转双色光环：双色弧线绕节点旋转
    val ringTransition = rememberInfiniteTransition(label = "select_ring")
    val ringRotation by ringTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    // ── P2-2 交互反馈：拖拽状态追踪 ──
    // isDragging 在 onDragStart 时置 true，onDragEnd/onDragCancel 时置 false
    // 配合 animateFloatAsState + spring 实现松手回弹动画
    var isDragging by remember { mutableStateOf(false) }
    val dragScale by animateFloatAsState(
        targetValue = if (isDragging) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "node_drag_scale"
    )
    val dragGlowAlpha by animateFloatAsState(
        targetValue = if (isDragging) 0.55f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "node_drag_glow"
    )

    // 外层 Box：承载定位 + 光晕层（光晕可超出节点边界）
    Box(
        modifier = Modifier
            .size(radiusDp * 2)
            .offset { IntOffset((sx - radiusPx).toInt(), (sy - radiusPx).toInt()) }
    ) {
        // ── 光晕层（节点主体下方，可向外扩展）──

        // 1. 高 tier 呼吸光晕：FAMILY/CLOSE 节点 + 事件节点外层柔光
        if (isHighTier && !isDimmed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.6f
                        scaleY = 1.6f
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                nodeColor.copy(alpha = breathAlpha * alpha * 0.55f),
                                nodeColor.copy(alpha = breathAlpha * alpha * 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // 2. "我"节点金色脉冲光环：外扩动画
        if (node.isSelf) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        this.alpha = pulseAlpha
                    }
                    .border(
                        width = 2.dp,
                        color = selfGoldColor,
                        shape = CircleShape
                    )
            )
        }

        // 3. 选中节点旋转双色光环（Canvas 绘制两段弧线）
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = 1.25f
                        scaleY = 1.25f
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokePx = 2.dp.toPx()
                    val diameter = size.minDimension
                    // 关键：drawArc 的 stroke 居中绘制（一半在外接矩形外，一半在内）。
                    // 要让弧线视觉中心精确对齐 canvas 中心且 stroke 不被裁剪：
                    // - 外接矩形 size = diameter - strokePx（留出 stroke 半宽的空间）
                    // - topLeft = (size - arcSize) / 2，即 strokePx/2（居中放置）
                    val arcDiameter = diameter - strokePx
                    val topLeft = Offset(
                        (size.width - arcDiameter) / 2f,
                        (size.height - arcDiameter) / 2f
                    )
                    val arcSize = androidx.compose.ui.geometry.Size(arcDiameter, arcDiameter)
                    rotate(degrees = ringRotation) {
                        // 紫色弧（上半）
                        drawArc(
                            color = SignalPurple,
                            startAngle = -90f,
                            sweepAngle = 110f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                        // 金色弧（下半，180° 对称）
                        drawArc(
                            color = selfGoldColor,
                            startAngle = 90f,
                            sweepAngle = 110f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }

        // ── 节点圆形主体 ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                // P2-2: 拖拽放大 + 松手回弹
                .graphicsLayer {
                    scaleX = dragScale
                    scaleY = dragScale
                }
                .shadow(
                    elevation = if (isDragging) 14.dp else elevation,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = if (node.isSelf) 0.35f else 0.25f),
                    spotColor = Color.Black.copy(alpha = if (node.isSelf) 0.45f else 0.35f)
                )
                .clip(CircleShape)
                // P2-2: 拖拽时的金色辉光（绘制在背景层，透过半透明 tier 色显现）
                .background(
                    color = if (dragGlowAlpha > 0.01f) {
                        selfGoldColor.copy(alpha = dragGlowAlpha)
                    } else {
                        Color.Transparent
                    }
                )
                .background(nodeColor.copy(alpha = 0.85f * alpha))
                // 节点边框双层：外层柔光（更宽、半透明）+ 内层实色
                .border(width = borderWidth + 1.dp, color = borderColor.copy(alpha = alpha * 0.35f), shape = CircleShape)
                .border(width = borderWidth, color = borderColor.copy(alpha = alpha), shape = CircleShape)
                .pointerInput(node.id) {
                    detectDragGestures(
                        onDragStart = {
                            // 拖拽开始：标记 dragging 状态触发放大动画
                            // ViewModel 内 onNodeDrag 会设 isFixed=true
                            isDragging = true
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val worldDx = dragAmount.x / scale
                            val worldDy = dragAmount.y / scale
                            onNodeDrag(node.id, worldDx, worldDy)
                        },
                        onDragEnd = {
                            isDragging = false
                            onNodeDragEnd(node.id)
                        },
                        onDragCancel = {
                            isDragging = false
                            onNodeDragEnd(node.id)
                        }
                    )
                }
                .pointerInput(node.id) {
                    detectTapGestures(
                        onTap = { onNodeClick(node.id) },
                        onLongPress = { onNodeLongPress(node.id) }
                    )
                }
        ) {
            if (node.isEvent) {
                // 事件节点：事件类型图标（满铺节点，白色图标 + 类型色背景）
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(nodeColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = eventIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size((radiusDp.value * 0.85f).dp)
                    )
                }
            } else {
                // 人物节点：头像 + placeholder
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(node.avatarUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = node.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        // 加载中：tier 色背景 + 首字母
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(nodeColor.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = placeholderText,
                                color = Color.White,
                                fontSize = (radiusDp.value * 0.9f).sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    },
                    error = {
                        // 加载失败/无头像：tier 色背景 + 首字母
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(nodeColor.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = placeholderText,
                                color = Color.White,
                                fontSize = (radiusDp.value * 0.9f).sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                )
            }

            // 节点边框双层：内层细白边（增加层次感）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.5.dp)
                    .border(0.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            )

            // 选中态内层光环（沿边框内侧绘制，与外层旋转光环呼应）
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .border(
                            width = 1.5.dp,
                            color = SignalPurple.copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                )
            }

            // "我"节点中心标识（金色小圆点）
            if (node.isSelf) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(1.dp)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(selfGoldColor)
                        .border(1.dp, Color.White, CircleShape)
                )
            }
        }
    }
}
