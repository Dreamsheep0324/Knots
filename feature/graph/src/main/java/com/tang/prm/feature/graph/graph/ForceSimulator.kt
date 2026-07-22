package com.tang.prm.feature.graph.graph

import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 力导向布局物理模拟引擎。
 *
 * 纯 Kotlin 实现，无 Android 依赖，便于单测。
 *
 * 物理模型（每帧 step）：
 * 1. 斥力（Coulomb-like）：每对节点间 F = repulsion / d²，方向沿连线推离
 * 2. 弹簧力（Hooke's law）：相连节点 F = (d - springLength) × springStrength，
 *    距离 > springLength 时吸引，< springLength 时排斥
 * 3. 中心引力：所有节点向 (centerX, centerY) 微弱拉力，防止漂散
 * 4. 速度更新：v = (v + a) × damping，受 maxSpeed 上限
 * 5. 位置更新：p = p + v（固定节点与"我"节点跳过）
 *
 * 性能优化：
 * - 节点数 > [ForceConfig.quadTreeThreshold] 时启用 Barnes-Hut 四叉树（O(n log n)）
 * - 否则 O(n²) 直接计算（小规模更快，无建树开销）
 * - 收敛后 [isConverged] 返回 true，调用方停止 step 节省电量
 *
 * 使用方式：
 * ```
 * while (!simulator.isConverged(nodes) && frameCount < maxFrames) {
 *     simulator.step(nodes, edges, centerX, centerY)
 * }
 * ```
 */
class ForceSimulator(
    private val config: ForceConfig = ForceConfig()
) {

    /**
     * 执行一帧物理模拟，直接修改 [nodes] 的 x/y/vx/vy。
     *
     * @param nodes 节点列表（in/out）
     * @param edges 边列表（只读，用于弹簧力）
     * @param centerX 画布中心 X
     * @param centerY 画布中心 Y
     */
    fun step(
        nodes: List<GraphNode>,
        edges: List<GraphEdge>,
        centerX: Float,
        centerY: Float
    ) {
        if (nodes.isEmpty()) return

        // 1. 斥力
        applyRepulsion(nodes)

        // 2. 弹簧力（边）
        applySpringForces(nodes, edges)

        // 3. 中心引力
        applyCenterGravity(nodes, centerX, centerY)

        // 4. 速度阻尼 + 位置更新
        integrate(nodes)
    }

    /**
     * 判断是否已收敛（总动能 < 阈值）。
     * 收敛后调用方可停止 step，仅交互时重新启动。
     */
    fun isConverged(nodes: List<GraphNode>): Boolean {
        if (nodes.isEmpty()) return true
        var totalKineticEnergy = 0f
        for (node in nodes) {
            if (node.isFixed) continue
            totalKineticEnergy += node.vx * node.vx + node.vy * node.vy
        }
        return totalKineticEnergy < config.convergenceThreshold
    }

    /**
     * 重置内部状态（如四叉树缓存）。
     * 节点位置/速度的重置由调用方负责。
     */
    fun reset() {
        // 当前实现无跨帧内部状态，保留 API 以备未来扩展（如热启动缓存）
    }

    // ---------- 斥力 ----------

    private fun applyRepulsion(nodes: List<GraphNode>) {
        if (nodes.size > config.quadTreeThreshold && config.theta > 0f) {
            applyRepulsionWithQuadTree(nodes)
        } else {
            applyRepulsionBruteForce(nodes)
        }
    }

    /**
     * O(n²) 直接两两计算斥力。
     * 适用于小规模（≤ quadTreeThreshold）场景。
     *
     * 所有非固定节点（含"我"节点）都参与斥力计算。
     * 拖拽中的节点（isFixed=true）跳过——避免拖拽时被斥力推走。
     */
    private fun applyRepulsionBruteForce(nodes: List<GraphNode>) {
        for (i in nodes.indices) {
            val a = nodes[i]
            if (a.isFixed) continue
            for (j in (i + 1) until nodes.size) {
                val b = nodes[j]
                if (b.isFixed) continue
                applyPairwiseRepulsion(a, b)
            }
        }
    }

    /**
     * O(n log n) Barnes-Hut 四叉树加速。
     * 适用于大规模（> quadTreeThreshold）场景。
     */
    private fun applyRepulsionWithQuadTree(nodes: List<GraphNode>) {
        val tree = buildQuadTree(nodes)
        for (node in nodes) {
            if (node.isFixed) continue
            computeRepulsionFromQuadTree(node, tree)
        }
    }

    /**
     * 计算一对节点间的斥力并施加到双方速度。
     * 距离极小时夹一个最小值，防止除零导致数值爆炸。
     *
     * 事件节点（[GraphNode.isEvent]）斥力适度衰减，模拟"质量较轻"，
     * 让人物节点占据主导空间，事件节点自然融入人物之间。
     */
    private fun applyPairwiseRepulsion(a: GraphNode, b: GraphNode) {
        val dx = a.x - b.x
        val dy = a.y - b.y
        val distSq = dx * dx + dy * dy
        val dist = sqrt(distSq.coerceAtLeast(MIN_DIST_SQ))
        if (dist < 1e-5f) {
            // 完全重合：给一个微小随机偏移避免奇点
            a.vx += 0.5f
            a.vy -= 0.5f
            if (!b.isFixed) {
                b.vx -= 0.5f
                b.vy += 0.5f
            }
            return
        }
        // F = repulsion / d²，分解到 x/y 方向：Fx = F × (dx/d)
        // 事件节点斥力适度衰减（模拟质量轻），让人物节点占据主导空间
        // 事件节点间斥力更弱，让事件可以靠近共同参与者形成自然聚类
        val massScale = when {
            a.isEvent && b.isEvent -> 0.3f
            a.isEvent || b.isEvent -> 0.45f
            else -> 1f
        }
        val force = config.repulsion * massScale / (distSq.coerceAtLeast(MIN_DIST_SQ))
        val fx = force * dx / dist
        val fy = force * dy / dist
        a.vx += fx
        a.vy += fy
        if (!b.isFixed) {
            b.vx -= fx
            b.vy -= fy
        }
    }

    // ---------- 弹簧力 ----------

    @Suppress("LoopWithTooManyJumpStatements")
    private fun applySpringForces(nodes: List<GraphNode>, edges: List<GraphEdge>) {
        if (edges.isEmpty()) return
        val nodeIndex = nodes.associateBy { it.id }
        for (edge in edges) {
            val a = nodeIndex[edge.sourceId] ?: continue
            val b = nodeIndex[edge.targetId] ?: continue
            // 所有非固定节点（含"我"）都参与弹簧力。
            // "我"节点会因连接所有联系人而受力，但合力通常较小（来自四面八方的拉力互相抵消）。

            val dx = b.x - a.x
            val dy = b.y - a.y
            val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
            // 根据两端节点的亲密度等级决定弹簧理想长度：
            // 亲密度越高（FAMILY）→ 距离越近；亲密度越低（NEW）→ 距离越远
            // "我"节点（isSelf）的 tier=FAMILY 不参与计算，使用另一端 tier
            val springLength = desiredSpringLength(a, b)
            // F = (d - L) × k，距离 > L 时吸引（拉回），< L 时排斥（推开）
            val displacement = dist - springLength
            val force = displacement * config.springStrength
            val fx = force * dx / dist
            val fy = force * dy / dist
            if (!a.isFixed) {
                a.vx += fx
                a.vy += fy
            }
            if (!b.isFixed) {
                b.vx -= fx
                b.vy -= fy
            }
        }
    }

    /**
     * 根据两端节点的 [IntimacyTier] 计算期望弹簧长度。
     *
     * 视觉语义：亲密度越高（至亲/密友）→ 连线越短，节点越靠近"我"；
     * 亲密度越低（初识/泛交）→ 连线越长，节点越远离"我"。
     *
     * "我"节点（isSelf=true）的 tier=FAMILY 不参与计算，使用另一端节点的 tier。
     * 若两端都不是"我"，取两者 tier 对应长度的平均值。
     *
     * 事件节点特殊处理：事件节点的弹簧长度基于参与者 tier × 0.85，
     * 让事件贴近其参与者自然分布，不形成独立圈层。高亲密度参与者的事件靠内圈，
     * 低亲密度参与者的事件靠外圈，事件与人物混合分布。
     *
     * 注意：边的颜色/标签由 [GraphEdge.relationType]（关系类型标签）决定，
     * 但边的长度仍由节点亲密度决定——亲密度高的人离"我"更近，符合直觉。
     */
    private fun desiredSpringLength(a: GraphNode, b: GraphNode): Float {
        // 事件节点：基于参与者 tier 计算弹簧长度，贴近参与者自然分布
        if (a.isEvent || b.isEvent) {
            val participant = if (a.isEvent) b else a
            val participantLen = if (participant.isSelf) null else tierSpringLength(participant.tier)
            return (participantLen ?: config.springLength) * 0.85f
        }
        val lenA = if (a.isSelf) null else tierSpringLength(a.tier)
        val lenB = if (b.isSelf) null else tierSpringLength(b.tier)
        return when {
            lenA != null && lenB != null -> (lenA + lenB) / 2f
            lenA != null -> lenA
            lenB != null -> lenB
            else -> config.springLength
        }
    }

    /**
     * 亲密度等级 → 弹簧长度映射。
     *
     * 采用渐进式间距，内圈紧凑、外圈舒展，强化"远近疏离"层次感：
     * - FAMILY（至亲）：260（最近，紧贴"我"）
     * - CLOSE（密友）：400
     * - FRIEND（朋友）：540
     * - ACQUAINTANCE（泛交）：680
     * - NEW（初识）：820（最远，分布在画布外圈）
     *
     * 视觉效果：至亲形成内圈、密友次圈、朋友中圈、泛交外圈、初识最外圈，
     * 各圈层间距均匀（140），一眼能看出亲疏关系。
     */
    private fun tierSpringLength(tier: com.tang.prm.domain.model.IntimacyTier): Float = when (tier) {
        com.tang.prm.domain.model.IntimacyTier.FAMILY -> 260f
        com.tang.prm.domain.model.IntimacyTier.CLOSE -> 400f
        com.tang.prm.domain.model.IntimacyTier.FRIEND -> 540f
        com.tang.prm.domain.model.IntimacyTier.ACQUAINTANCE -> 680f
        com.tang.prm.domain.model.IntimacyTier.NEW -> 820f
    }

    // ---------- 中心引力 ----------

    private fun applyCenterGravity(nodes: List<GraphNode>, centerX: Float, centerY: Float) {
        for (node in nodes) {
            if (node.isFixed) continue
            // 所有节点（含"我"）使用相同的中心引力，让"我"也能自由移动
            node.vx += (centerX - node.x) * config.centerGravity
            node.vy += (centerY - node.y) * config.centerGravity
        }
    }

    // ---------- 积分（速度阻尼 + 位置更新） ----------

    private fun integrate(nodes: List<GraphNode>) {
        for (node in nodes) {
            if (node.isFixed) {
                // 固定节点速度清零，位置不变
                node.vx = 0f
                node.vy = 0f
                continue
            }
            // 阻尼
            node.vx *= config.damping
            node.vy *= config.damping
            // 速度上限
            val speed = sqrt(node.vx * node.vx + node.vy * node.vy)
            if (speed > config.maxSpeed) {
                val scale = config.maxSpeed / speed
                node.vx *= scale
                node.vy *= scale
            }
            // 位置更新
            node.x += node.vx
            node.y += node.vy
        }
    }

    // ---------- Barnes-Hut 四叉树 ----------

    /**
     * 四叉树节点。
     *
     * - 叶子节点：[children] 为 null，[point] 持有单个节点（或为空）
     * - 内部节点：[children] 4 个象限，[mass] 与 [centerOfMass] 为子树聚合
     */
    private class QuadTreeNode(
        val bounds: Bounds
    ) {
        var mass: Float = 0f
        var centerOfMassX: Float = 0f
        var centerOfMassY: Float = 0f
        var point: GraphNode? = null
        var children: Array<QuadTreeNode?>? = null

        val isLeaf: Boolean get() = children == null
    }

    private class Bounds(
        val cx: Float,
        val cy: Float,
        val halfSize: Float
    ) {
        fun contains(x: Float, y: Float): Boolean =
            abs(x - cx) <= halfSize && abs(y - cy) <= halfSize

        fun quadrantIndex(x: Float, y: Float): Int = when {
            x >= cx && y < cy -> 0 // 右上
            x < cx && y < cy -> 1  // 左上
            x < cx && y >= cy -> 2 // 左下
            else -> 3              // 右下
        }

        fun quadrant(qi: Int): Bounds {
            val newHalf = halfSize / 2f
            val dx = if (qi == 0 || qi == 3) newHalf else -newHalf
            val dy = if (qi == 0 || qi == 1) -newHalf else newHalf
            return Bounds(cx + dx, cy + dy, newHalf)
        }
    }

    /**
     * 构建 Barnes-Hut 四叉树。
     * 包围盒根据节点位置自动计算，留 10% 边距防止边界节点掉出。
     */
    private fun buildQuadTree(nodes: List<GraphNode>): QuadTreeNode {
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE
        for (n in nodes) {
            if (n.x < minX) minX = n.x
            if (n.y < minY) minY = n.y
            if (n.x > maxX) maxX = n.x
            if (n.y > maxY) maxY = n.y
        }
        val padding = ((maxX - minX).coerceAtLeast(maxY - minY)) * 0.1f + 1f
        val cx = (minX + maxX) / 2f
        val cy = (minY + maxY) / 2f
        val halfSize = ((maxX - minX) / 2f).coerceAtLeast((maxY - minY) / 2f) + padding
        val root = QuadTreeNode(Bounds(cx, cy, halfSize))
        for (node in nodes) {
            insertIntoQuadTree(root, node)
        }
        return root
    }

    private fun insertIntoQuadTree(node: QuadTreeNode, point: GraphNode) {
        if (node.mass == 0f) {
            // 空叶子节点：直接放置
            node.point = point
            node.mass = 1f
            node.centerOfMassX = point.x
            node.centerOfMassY = point.y
            return
        }
        if (node.isLeaf && node.point != null) {
            // 已有单点的叶子节点：细分为 4 象限，将旧点和新点分别下沉
            val existing = node.point!!
            node.point = null
            val children = arrayOfNulls<QuadTreeNode>(4)
            node.children = children
            // 重新插入旧点（会下沉到对应象限）
            insertIntoChild(node, existing, children)
            // 插入新点
            insertIntoChild(node, point, children)
            // 更新质心
            updateCenterOfMass(node, point)
        } else {
            // 内部节点：直接下沉到对应象限
            val children = node.children!!
            insertIntoChild(node, point, children)
            updateCenterOfMass(node, point)
        }
    }

    private fun insertIntoChild(
        parent: QuadTreeNode,
        point: GraphNode,
        children: Array<QuadTreeNode?>
    ) {
        val qi = parent.bounds.quadrantIndex(point.x, point.y)
        if (children[qi] == null) {
            children[qi] = QuadTreeNode(parent.bounds.quadrant(qi))
        }
        insertIntoQuadTree(children[qi]!!, point)
    }

    private fun updateCenterOfMass(node: QuadTreeNode, point: GraphNode) {
        val newMass = node.mass + 1f
        node.centerOfMassX = (node.centerOfMassX * node.mass + point.x) / newMass
        node.centerOfMassY = (node.centerOfMassY * node.mass + point.y) / newMass
        node.mass = newMass
    }

    /**
     * 从四叉树计算 [target] 节点受到的斥力。
     * Barnes-Hut 准则：若 s/d < theta（s = 象限边长，d = 节点到质心距离），
     * 视该子树为单点近似计算；否则递归进入子象限。
     */
    private fun computeRepulsionFromQuadTree(target: GraphNode, tree: QuadTreeNode) {
        if (tree.mass == 0f) return
        val dx = target.x - tree.centerOfMassX
        val dy = target.y - tree.centerOfMassY
        val distSq = dx * dx + dy * dy
        val dist = sqrt(distSq.coerceAtLeast(MIN_DIST_SQ))
        val size = tree.bounds.halfSize * 2f
        // s/d < theta → 近似为单点
        if (tree.isLeaf || size / dist < config.theta) {
            if (tree.point === target) return // 不受自身斥力
            val force = config.repulsion * tree.mass / distSq.coerceAtLeast(MIN_DIST_SQ)
            target.vx += force * dx / dist
            target.vy += force * dy / dist
        } else {
            // 递归 4 个象限
            tree.children?.forEach { child ->
                if (child != null) computeRepulsionFromQuadTree(target, child)
            }
        }
    }

    private companion object {
        const val MIN_DIST_SQ = 0.01f
    }
}
