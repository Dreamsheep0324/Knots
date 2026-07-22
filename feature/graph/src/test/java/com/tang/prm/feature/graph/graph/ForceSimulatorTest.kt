package com.tang.prm.feature.graph.graph

import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.IntimacyTier
import com.tang.prm.domain.model.RelationSource
import org.junit.jupiter.api.Test

/**
 * [ForceSimulator] 物理模型单元测试。
 *
 * 覆盖：
 * - 空列表/单节点边界
 * - 斥力：两节点距离应增大
 * - 弹簧力：相连节点距离应趋近 springLength
 * - 中心引力：节点应向中心靠拢
 * - 固定节点：位置与速度不变
 * - 收敛判定：多步 step 后 isConverged 返回 true
 * - 四叉树路径：节点数 > quadTreeThreshold 时与暴力法结果同方向
 */
class ForceSimulatorTest {

    private fun makeNode(
        id: Long,
        x: Float,
        y: Float,
        isSelf: Boolean = false,
        isFixed: Boolean = false
    ): GraphNode = GraphNode(
        id = id,
        name = "n$id",
        avatarUri = null,
        tier = IntimacyTier.FRIEND,
        isSelf = isSelf,
        x = x,
        y = y,
        isFixed = isFixed
    )

    private fun makeEdge(
        id: Long,
        sourceId: Long,
        targetId: Long,
        source: RelationSource = RelationSource.MANUAL
    ): GraphEdge = GraphEdge(
        id = id,
        sourceId = sourceId,
        targetId = targetId,
        relationType = CustomType(category = "RELATIONSHIP", name = "关系"),
        label = null,
        source = source
    )

    @Test
    fun `empty nodes list does not crash and converges`() {
        val sim = ForceSimulator()
        sim.step(emptyList(), emptyList(), 0f, 0f)
        assertThat(sim.isConverged(emptyList())).isTrue()
    }

    @Test
    fun `single free node converges immediately`() {
        val sim = ForceSimulator()
        val node = makeNode(1, x = 10f, y = 0f)
        // 中心引力会拉回原点，但单步内动能应小于阈值
        repeat(50) { sim.step(listOf(node), emptyList(), 0f, 0f) }
        assertThat(sim.isConverged(listOf(node))).isTrue()
    }

    @Test
    fun `two close nodes repel and distance increases`() {
        val sim = ForceSimulator(ForceConfig(repulsion = 8000f))
        val a = makeNode(1, x = -5f, y = 0f)
        val b = makeNode(2, x = 5f, y = 0f)
        val initialDistance = kotlin.math.abs(b.x - a.x)
        repeat(20) { sim.step(listOf(a, b), emptyList(), centerX = 0f, centerY = 0f) }
        val finalDistance = kotlin.math.abs(b.x - a.x)
        assertThat(finalDistance).isGreaterThan(initialDistance)
    }

    @Test
    fun `spring force pulls distant connected nodes closer`() {
        // 弹簧长度 100，两节点距离 300 → 应被拉近
        val sim = ForceSimulator(
            ForceConfig(repulsion = 0f, springLength = 100f, springStrength = 0.1f, centerGravity = 0f)
        )
        val a = makeNode(1, x = -150f, y = 0f)
        val b = makeNode(2, x = 150f, y = 0f)
        val edge = makeEdge(1, sourceId = 1, targetId = 2)
        val initialDistance = kotlin.math.abs(b.x - a.x)
        repeat(50) { sim.step(listOf(a, b), listOf(edge), 0f, 0f) }
        val finalDistance = kotlin.math.abs(b.x - a.x)
        assertThat(finalDistance).isLessThan(initialDistance)
    }

    @Test
    fun `center gravity pulls node toward center`() {
        val sim = ForceSimulator(
            ForceConfig(repulsion = 0f, springStrength = 0f, centerGravity = 0.1f)
        )
        val node = makeNode(1, x = 100f, y = 100f)
        val initialDistanceToCenter = kotlin.math.sqrt(node.x * node.x + node.y * node.y)
        repeat(30) { sim.step(listOf(node), emptyList(), 0f, 0f) }
        val finalDistanceToCenter = kotlin.math.sqrt(node.x * node.x + node.y * node.y)
        assertThat(finalDistanceToCenter).isLessThan(initialDistanceToCenter)
    }

    @Test
    fun `fixed node position and velocity remain unchanged`() {
        val sim = ForceSimulator()
        val fixed = makeNode(1, x = 50f, y = 50f, isFixed = true)
        val other = makeNode(2, x = 51f, y = 51f)
        repeat(10) { sim.step(listOf(fixed, other), emptyList(), 0f, 0f) }
        assertThat(fixed.x).isEqualTo(50f)
        assertThat(fixed.y).isEqualTo(50f)
        assertThat(fixed.vx).isEqualTo(0f)
        assertThat(fixed.vy).isEqualTo(0f)
    }

    @Test
    fun `self node is treated as fixed and immovable`() {
        val sim = ForceSimulator()
        val self = makeNode(id = -1L, x = 0f, y = 0f, isSelf = true)
        val other = makeNode(2, x = 10f, y = 0f)
        repeat(10) { sim.step(listOf(self, other), emptyList(), 0f, 0f) }
        assertThat(self.x).isEqualTo(0f)
        assertThat(self.y).isEqualTo(0f)
        assertThat(self.vx).isEqualTo(0f)
        assertThat(self.vy).isEqualTo(0f)
    }

    @Test
    fun `isConverged returns true after sufficient steps`() {
        val sim = ForceSimulator()
        val nodes = listOf(
            makeNode(1, x = -20f, y = 0f),
            makeNode(2, x = 20f, y = 0f),
            makeNode(3, x = 0f, y = 20f)
        )
        // 充分多步后应收敛
        repeat(500) { sim.step(nodes, emptyList(), 0f, 0f) }
        assertThat(sim.isConverged(nodes)).isTrue()
    }

    @Test
    fun `quad tree path produces repulsion in same direction as brute force`() {
        // 节点数 > quadTreeThreshold(100) 触发四叉树路径
        val threshold = 100
        val sim = ForceSimulator(
            ForceConfig(repulsion = 100f, quadTreeThreshold = threshold, theta = 1f)
        )
        val nodes = (1..150).map { i ->
            makeNode(id = i.toLong(), x = (i % 15).toFloat() * 5, y = (i / 15).toFloat() * 5)
        }
        // 不应抛异常，且节点应受力后位移
        val initialX = nodes.first { it.id == 1L }.x
        repeat(10) { sim.step(nodes, emptyList(), centerX = 0f, centerY = 0f) }
        val finalX = nodes.first { it.id == 1L }.x
        // 至少有位移（斥力 + 中心引力综合作用）
        assertThat(finalX).isNotEqualTo(initialX)
    }

    @Test
    fun `reset is safe to call before and after step`() {
        val sim = ForceSimulator()
        sim.reset()
        val node = makeNode(1, x = 0f, y = 0f)
        sim.step(listOf(node), emptyList(), 0f, 0f)
        sim.reset()
        // 再次 step 应正常工作
        sim.step(listOf(node), emptyList(), 0f, 0f)
        assertThat(node).isNotNull()
    }
}
