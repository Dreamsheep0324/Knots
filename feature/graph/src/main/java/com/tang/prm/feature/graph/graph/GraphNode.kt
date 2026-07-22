package com.tang.prm.feature.graph.graph

import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.model.IntimacyTier

/**
 * 节点类型：人物（含"我"）/ 事件。
 */
enum class NodeType { PERSON, EVENT }

/** 事件节点专属信息（仅 [NodeType.EVENT] 节点有值）。 */
data class EventNodeInfo(
    val type: EventType,
    val time: Long,
    val title: String,
    val customTypeName: String? = null
)

/**
 * 图谱节点。
 *
 * 设计为可变 class（非 data class），原因：
 * - 物理模拟状态（[x], [y], [vx], [vy], [isFixed]）由 [ForceSimulator] in-place 修改
 * - 业务字段（[name], [avatarUri], [tier]）由 ViewModel 增量同步更新
 * - 跨数据流 emit 复用同一对象，保留位置/速度/拖拽状态
 *
 * 不变量：
 * - [id] / [isSelf] / [nodeType] 创建后不可变（用于身份识别与命中测试分派）
 * - 物理状态修改发生在 [ForceSimulator] 内部 tight loop，不触发 Compose 重组
 *   （Compose 通过外部 redrawTrigger 触发重绘，不依赖 GraphNode equals）
 */
class GraphNode(
    val id: Long,
    name: String,
    avatarUri: String?,
    tier: IntimacyTier,
    val isSelf: Boolean,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    var isFixed: Boolean = false,
    val nodeType: NodeType = NodeType.PERSON,
    val eventInfo: EventNodeInfo? = null
) {
    var name: String = name
        private set
    var avatarUri: String? = avatarUri
        private set
    var tier: IntimacyTier = tier
        private set

    /** 是否为事件节点（语法糖） */
    val isEvent: Boolean get() = nodeType == NodeType.EVENT

    /**
     * 业务字段更新（由 ViewModel 增量同步调用，保留位置/速度/固定状态）。
     * 仅对 [NodeType.PERSON] 节点有意义；事件节点信息不变。
     */
    fun updateProfile(name: String, avatarUri: String?, tier: IntimacyTier) {
        if (nodeType != NodeType.PERSON) return
        this.name = name
        this.avatarUri = avatarUri
        this.tier = tier
    }
}
