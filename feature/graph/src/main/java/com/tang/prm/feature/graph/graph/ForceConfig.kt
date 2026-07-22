package com.tang.prm.feature.graph.graph

/**
 * 力导向布局物理模拟引擎配置。
 *
 * 参数调优指南见实现文档附录 B。
 * 默认值经 100 节点测试数据验证：5 秒内收敛，无抖动。
 *
 * @property repulsion 斥力强度系数（F = k²/d）
 * @property springLength 弹簧理想长度（相连节点试图保持的距离）
 * @property springStrength 弹簧强度（拉回理想长度的力度）
 * @property centerGravity 中心引力（所有节点向画布中心的微弱拉力）
 * @property damping 阻尼（每帧速度衰减系数，0~1）
 * @property maxSpeed 速度上限（防抖动）
 * @property convergenceThreshold 收敛阈值：总动能低于此值视为收敛
 * @property quadTreeThreshold 启用四叉树的节点数阈值
 * @property theta Barnes-Hut 开角参数（越小越精确、越慢；0 = 关闭四叉树退化为 O(n²)）
 */
data class ForceConfig(
    // 斥力配合 springLength（240~800），确保节点间有足够间距，不过度拥挤也不过度稀疏
    val repulsion: Float = 38000f,
    // fallback 弹簧长度（实际由 desiredSpringLength 按 tier 计算，范围 240~800）
    val springLength: Float = 520f,
    // 弹簧强度适中，让连接节点保持合理距离又不过度拉扯
    val springStrength: Float = 0.04f,
    // 适度中心引力，让图形聚拢但不僵硬，"我"节点也能轻微漂移
    val centerGravity: Float = 0.0016f,
    // 阻尼较高，收敛平滑无抖动
    val damping: Float = 0.87f,
    // 速度上限适中，平衡收敛速度与稳定性
    val maxSpeed: Float = 16f,
    val convergenceThreshold: Float = 0.5f,
    val quadTreeThreshold: Int = 100,
    val theta: Float = 0.9f
)
