package com.tang.prm.domain.model

/**
 * 可作为待办事项聚合的领域模型统一契约。
 *
 * A-10 修复：[Reminder] / [TodoItem] / [Thought] 三处分散"待办"语义，抽取本接口作为
 * 统一契约，便于未来聚合"我的待办清单"时无需类型分支。
 *
 * ## 三处"待办"语义的角色分工
 *
 * - [Reminder]：提醒事项。天然是待办（无需 isTodo 标记），[isCompleted] 直接对应完成状态。
 *   持有 `contactId/eventId/anniversaryId` 三个可空外键，跨 3 个聚合根（未来计划改为
 *   `sourceType + sourceId` 与 [Favorite] 一致，见 core-domain 审查报告 A-10 长期方案）。
 * - [TodoItem]：独立待办事项。天然是待办（无需 isTodo 标记），[isCompleted] 直接对应完成状态。
 *   持有 `contactId/eventId` 两个可空外键，归属 Contact/Event 聚合。
 * - [Thought]：想法/计划/呢喃。仅当 `isTodo = true` 时才作为待办参与聚合，
 *   `isDone` 对应完成状态（仅在 isTodo=true 时有意义）。
 *
 * ## 接口设计说明
 *
 * - 仅统一"完成状态"语义（[isCompleted]），不强制统一"是否为待办"的判定：
 *   [Reminder]/[TodoItem] 天然是待办，[Thought] 需 `isTodo` 标记。调用方按类型自行判断是否纳入聚合。
 * - 命名采用 [Reminder]/[TodoItem] 已有的 `isCompleted`，[Thought] 通过
 *   `override val isCompleted get() = isDone` 桥接，不破坏现有 `isDone` 字段。
 */
interface Todoable {
    /**
     * 待办是否已完成。
     *
     * - [Reminder] / [TodoItem]：直接对应完成状态字段
     * - [Thought]：桥接 `isDone`，仅在 `isTodo = true` 时有意义
     */
    val isCompleted: Boolean
}
