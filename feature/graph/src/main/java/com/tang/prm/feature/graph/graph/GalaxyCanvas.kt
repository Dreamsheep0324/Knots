package com.tang.prm.feature.graph.graph

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.IntimacyTier
import com.tang.prm.ui.theme.SignalPurple
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 星系视图画布（P2 增强版）。
 *
 * 设计语义（参照太阳系）：
 * - "我"节点是恒星，固定在中心，带辉光
 * - 普通节点按 [IntimacyTier] 分层围绕"我"旋转，亲密度越高轨道越近
 * - 每条轨道是同心圆（虚线 + 呼吸光效），节点按开普勒第三定律近似旋转
 * - 不绘制连线，仅靠轨道圆圈和节点位置传达关系
 *
 * P2 增强：
 * 1. 星点背景：随机闪烁的星点，营造宇宙氛围
 * 2. 轨道呼吸光效：轨道圆圈 alpha 随时间正弦波动
 * 3. 节点拖尾粒子：节点运动方向留下渐隐尾迹
 * 4. 图例可点击筛选：点击 tier 图例高亮对应轨道
 */
@Suppress("LongParameterList")
@Composable
fun GalaxyCanvas(
    nodes: List<GraphNode>,
    @Suppress("UNUSED_PARAMETER") edges: List<GraphEdge>,
    viewport: GraphViewport,
    canvasSize: IntSize,
    selectedNodeId: Long?,
    @Suppress("UNUSED_PARAMETER") eventTypes: List<CustomType> = emptyList(),
    onCanvasReady: () -> Unit,
    onNodeClick: (Long) -> Unit,
    onCanvasPan: (Float, Float) -> Unit,
    onCanvasZoom: (Float, Float, Float) -> Unit,
    onCanvasClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 每个节点的初始相位
    val phases = remember(nodes) {
        nodes.associate { it.id to ((it.id * 37) % 360).toFloat() }
    }
    var tick by remember { mutableLongStateOf(0L) }
    var globalAngle by remember { mutableStateOf(0f) }
    var hasCentered by remember { mutableStateOf(false) }

    // 星点背景：稳定随机位置 + 闪烁相位
    val stars = remember(canvasSize) {
        if (canvasSize.width <= 0 || canvasSize.height <= 0) emptyList()
        else List(60) { i ->
            StarPoint(
                x = (i * 137.5f % canvasSize.width),
                y = (i * 271.3f % canvasSize.height),
                baseAlpha = 0.15f + (i % 5) * 0.08f,
                phaseOffset = (i * 53) % 360
            )
        }
    }

    // 节点轨迹历史（用于绘制拖尾粒子）
    val trailHistory = remember { mutableMapOf<Long, MutableList<Offset>>() }

    // 图例筛选：选中的 tier（null=全部）
    var selectedTier by remember { mutableStateOf<IntimacyTier?>(null) }

    LaunchedEffect(canvasSize) {
        if (!hasCentered && canvasSize.width > 0 && canvasSize.height > 0) {
            viewport.centerOn(
                viewWidth = canvasSize.width.toFloat(),
                viewHeight = canvasSize.height.toFloat(),
                targetWorldX = 0f,
                targetWorldY = 0f,
                targetScale = 1f
            )
            hasCentered = true
            onCanvasReady()
            tick++
        }
    }

    // 旋转主循环
    LaunchedEffect(nodes) {
        while (true) {
            withFrameNanos { nano ->
                globalAngle += 0.0025f
                tick = nano
            }
        }
    }

    val density = LocalDensity.current
    val nodeRadiusPx = with(density) { 20.dp.toPx() }
    val selfRadiusPx = with(density) { 18.dp.toPx() }
    val orbitStrokePx = with(density) { 1.dp.toPx() }
    val centerGlowRadiusPx = with(density) { 120.dp.toPx() }

    // 计算每个节点当前的世界坐标
    val currentTick = tick
    val nodePositions = remember(nodes, currentTick, globalAngle) {
        val map = HashMap<Long, Offset>(nodes.size)
        nodes.forEach { node ->
            map[node.id] = if (node.isSelf) {
                Offset(0f, 0f)
            } else {
                val tier = node.tier
                val orbitRadius = galaxyOrbitRadius(tier)
                val angularSpeed = orbitAngularSpeed(tier)
                val phase = phases[node.id] ?: 0f
                val angleRad = Math.toRadians(phase.toDouble()).toFloat() + globalAngle * angularSpeed
                Offset(
                    x = orbitRadius * cos(angleRad),
                    y = orbitRadius * sin(angleRad)
                )
            }
        }
        map
    }

    // 更新拖尾历史（仅非"我"节点）
    nodes.forEach { node ->
        if (node.isSelf) return@forEach
        val pos = nodePositions[node.id] ?: return@forEach
        val history = trailHistory.getOrPut(node.id) { mutableListOf() }
        if (history.isEmpty() || kotlin.math.hypot(pos.x - history.last().x, pos.y - history.last().y) > 3f) {
            history.add(pos)
            if (history.size > 8) history.removeAt(0)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 底层 Canvas：星点背景 + 中心辉光 + 轨道圆圈 + 拖尾
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, _, zoom, _ ->
                        if (zoom != 1f) {
                            onCanvasZoom(centroid.x, centroid.y, viewport.scale * zoom)
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onCanvasClick() }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onCanvasPan(dragAmount.x, dragAmount.y)
                        }
                    )
                }
        ) {
            val centerScreenX = viewport.worldToScreenX(0f)
            val centerScreenY = viewport.worldToScreenY(0f)

            // 1. 星点背景（随机闪烁）
            val starPhase = (currentTick / 100L) % 360L
            stars.forEach { star ->
                val twinkle = (0.5f + 0.5f * kotlin.math.sin(Math.toRadians((starPhase + star.phaseOffset).toDouble()).toFloat()))
                drawCircle(
                    color = Color.White.copy(alpha = star.baseAlpha * twinkle),
                    radius = 1f + (star.phaseOffset % 3) * 0.3f,
                    center = Offset(star.x, star.y)
                )
            }

            // 2. 中心辉光
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFBBF24).copy(alpha = 0.35f),
                        Color(0xFFF59E0B).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(centerScreenX, centerScreenY),
                    radius = centerGlowRadiusPx
                ),
                center = Offset(centerScreenX, centerScreenY),
                radius = centerGlowRadiusPx
            )

            // 3. 轨道圆圈（虚线 + 呼吸光效）
            val orbitTiers = listOf(
                IntimacyTier.FAMILY,
                IntimacyTier.CLOSE,
                IntimacyTier.FRIEND,
                IntimacyTier.ACQUAINTANCE,
                IntimacyTier.NEW
            )
            val breathPhase = (currentTick / 50L) % 360L
            val breathValue = 0.3f + 0.1f * kotlin.math.sin(Math.toRadians(breathPhase.toDouble()).toFloat())
            for (tier in orbitTiers) {
                val r = galaxyOrbitRadius(tier)
                val radiusScreen = r * viewport.scale
                if (radiusScreen < 8f) continue
                val isHighlighted = selectedTier == null || selectedTier == tier
                val alpha = if (isHighlighted) breathValue else 0.08f
                drawCircle(
                    color = tierOrbitColor(tier).copy(alpha = alpha),
                    radius = radiusScreen,
                    center = Offset(centerScreenX, centerScreenY),
                    style = Stroke(
                        width = orbitStrokePx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), phase = 0f)
                    )
                )
            }

            // 4. 节点拖尾粒子（渐隐尾迹）
            nodes.forEach { node ->
                if (node.isSelf) return@forEach
                val history = trailHistory[node.id] ?: return@forEach
                val tierColor = tierOrbitColor(node.tier)
                history.forEachIndexed { index, pos ->
                    val sx = viewport.worldToScreenX(pos.x)
                    val sy = viewport.worldToScreenY(pos.y)
                    val trailAlpha = (index.toFloat() / history.size) * 0.3f
                    drawCircle(
                        color = tierColor.copy(alpha = trailAlpha),
                        radius = 3f,
                        center = Offset(sx, sy)
                    )
                }
            }
        }

        // 顶层：节点 Composable
        nodes.forEach { node ->
            val pos = nodePositions[node.id] ?: return@forEach
            // 筛选态：非选中 tier 的节点变暗
            val isDimmedByFilter = selectedTier != null && !node.isSelf && node.tier != selectedTier
            val radiusDp: Dp = if (node.isSelf) 18.dp else 20.dp
            val sx = viewport.worldToScreenX(pos.x)
            val sy = viewport.worldToScreenY(pos.y)
            val radiusPx = with(density) { radiusDp.toPx() }
            GalaxyNodeComposable(
                node = node,
                radiusDp = radiusDp,
                radiusPx = radiusPx,
                screenX = sx,
                screenY = sy,
                isSelected = selectedNodeId == node.id,
                isDimmed = (selectedNodeId != null && selectedNodeId != node.id) || isDimmedByFilter,
                onClick = { onNodeClick(node.id) }
            )
        }

        // 底部亲密度图例（可点击筛选）
        GalaxyLegend(
            selectedTier = selectedTier,
            onTierClick = { tier ->
                selectedTier = if (selectedTier == tier) null else tier
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
        )
    }
}

/** 星点数据。 */
private data class StarPoint(
    val x: Float,
    val y: Float,
    val baseAlpha: Float,
    val phaseOffset: Int
)

/**
 * 亲密度等级 → 星系轨道半径（世界坐标单位）。
 */
private fun galaxyOrbitRadius(tier: IntimacyTier): Float = when (tier) {
    IntimacyTier.FAMILY -> 180f
    IntimacyTier.CLOSE -> 300f
    IntimacyTier.FRIEND -> 420f
    IntimacyTier.ACQUAINTANCE -> 540f
    IntimacyTier.NEW -> 660f
}

/**
 * 亲密度等级 → 轨道角速度（开普勒第三定律近似：ω ∝ r^(-1.5)）。
 */
private fun orbitAngularSpeed(tier: IntimacyTier): Float {
    val baseRadius = 420f
    val r = galaxyOrbitRadius(tier)
    val ratio = baseRadius / r
    val raw = Math.pow(ratio.toDouble(), 1.5).toFloat()
    return raw.coerceIn(0.4f, 2.0f)
}

/**
 * 亲密度等级 → 轨道圆圈颜色。
 */
private fun tierOrbitColor(tier: IntimacyTier): Color = when (tier) {
    IntimacyTier.FAMILY -> Color(0xFFF59E0B)
    IntimacyTier.CLOSE -> Color(0xFFEF4444)
    IntimacyTier.FRIEND -> Color(0xFF8B5CF6)
    IntimacyTier.ACQUAINTANCE -> Color(0xFF3B82F6)
    IntimacyTier.NEW -> Color(0xFF94A3B8)
}

/**
 * 星系视图的节点 Composable（带辉光效果）。
 */
@Composable
private fun GalaxyNodeComposable(
    node: GraphNode,
    radiusDp: Dp,
    radiusPx: Float,
    screenX: Float,
    screenY: Float,
    isSelected: Boolean,
    isDimmed: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val tierColor = Color(node.tier.colorValue)
    val selfGoldColor = Color(0xFFFBBF24)
    val placeholderText = if (node.isSelf) "我" else node.name.firstOrNull()?.toString() ?: "·"
    val alpha = if (isDimmed) 0.3f else 1f
    val isHighTier = node.tier == IntimacyTier.FAMILY || node.tier == IntimacyTier.CLOSE

    // ── 节点光晕系统：与 GraphCanvas.NodeComposable 一致的 4 类动画 ──
    // 1. 高 tier(FAMILY/CLOSE) 呼吸光晕
    val breathTransition = rememberInfiniteTransition(label = "galaxy_breath")
    val breathAlpha by breathTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "galaxy_breath_alpha"
    )

    // 2. "我"节点金色脉冲光环（外扩动画）
    val pulseTransition = rememberInfiniteTransition(label = "galaxy_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "galaxy_pulse_scale"
    )
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "galaxy_pulse_alpha"
    )

    // 3. 选中节点旋转双色光环
    val ringTransition = rememberInfiniteTransition(label = "galaxy_ring")
    val ringRotation by ringTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "galaxy_ring_rotation"
    )

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (screenX - radiusPx).roundToInt(),
                    y = (screenY - radiusPx).roundToInt()
                )
            }
            .size(radiusDp * 2)
            .alpha(alpha)
            .pointerInput(node.id) {
                detectTapGestures(onTap = { onClick() })
            }
    ) {
        // ── 光晕层（节点主体下方，可向外扩展）──

        // 1. 高 tier 呼吸光晕：FAMILY/CLOSE 节点外层柔光
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
                                tierColor.copy(alpha = breathAlpha * 0.55f),
                                tierColor.copy(alpha = breathAlpha * 0.2f),
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
                        drawArc(
                            color = SignalPurple,
                            startAngle = -90f,
                            sweepAngle = 110f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokePx, cap = StrokeCap.Round)
                        )
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

        // "我"节点：外层辉光（金色光晕，保留原静态辉光作为基底）
        if (node.isSelf) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                selfGoldColor.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        // 选中节点：外层紫色辉光（保留原静态辉光作为基底）
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                SignalPurple.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        // 主节点圆形
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(
                    elevation = if (node.isSelf) 10.dp else 4.dp,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .background(tierColor.copy(alpha = 0.9f))
                // 节点边框双层：外层柔光（更宽、半透明）+ 内层实色
                .border(
                    width = (if (node.isSelf) 3.dp else 2.dp) + 1.dp,
                    color = (if (node.isSelf) selfGoldColor else tierColor).copy(alpha = 0.35f),
                    shape = CircleShape
                )
                .border(
                    width = if (node.isSelf) 3.dp else 2.dp,
                    color = if (node.isSelf) selfGoldColor else tierColor,
                    shape = CircleShape
                )
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(node.avatarUri)
                    .crossfade(true)
                    .build(),
                contentDescription = node.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize().background(tierColor.copy(alpha = 0.6f)),
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
                    Box(
                        modifier = Modifier.fillMaxSize().background(tierColor.copy(alpha = 0.7f)),
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

            // 节点边框双层：内层细白边（增加层次感）
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.5.dp)
                    .border(0.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            )
        }
    }
}

/**
 * 星系视图底部亲密度图例（可点击筛选）。
 *
 * P2 增强：点击图例项可高亮对应轨道，再次点击取消。
 */
@Composable
private fun GalaxyLegend(
    selectedTier: IntimacyTier?,
    onTierClick: (IntimacyTier) -> Unit,
    modifier: Modifier = Modifier
) {
    val tiers = listOf(
        IntimacyTier.FAMILY to "至亲",
        IntimacyTier.CLOSE to "密友",
        IntimacyTier.FRIEND to "朋友",
        IntimacyTier.ACQUAINTANCE to "泛交",
        IntimacyTier.NEW to "初识"
    )
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.92f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tiers.forEach { (tier, label) ->
            val isSelected = selectedTier == tier
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onTierClick(tier) }
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(tierOrbitColor(tier))
                        .then(
                            if (isSelected) Modifier.border(1.5.dp, Color.Black, CircleShape)
                            else Modifier
                        )
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = if (isSelected) Color(0xFF1F2937) else Color(0xFF4A4A4A),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
