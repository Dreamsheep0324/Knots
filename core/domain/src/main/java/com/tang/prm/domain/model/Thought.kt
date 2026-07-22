package com.tang.prm.domain.model

enum class ThoughtType(val key: String) {
    FRIEND("friend"),
    PLAN("plan"),
    MURMUR("murmur");

    companion object {
        fun fromKey(key: String): ThoughtType =
            entries.find { it.key == key } ?: MURMUR
    }
}

/**
 * 想法/计划/呢喃（A-10 文档化）。
 *
 * ## 角色定位
 * 多用途记录：FRIEND（朋友相关）、PLAN（计划）、MURMUR（呢喃）。
 * 仅当 [isTodo] = true 时才作为待办事项参与聚合（实现 [Todoable]），
 * [isDone] 对应完成状态（仅在 [isTodo] = true 时有意义）。
 *
 * ## 聚合边界
 * 持有 `contactId` 可空外键，归属 Contact 聚合。
 *
 * @see Todoable
 */
data class Thought(
    val id: Long = 0,
    val contactId: Long? = null,
    val content: String,
    val type: ThoughtType = ThoughtType.MURMUR,
    val isPrivate: Boolean = false,
    val isTodo: Boolean = false,
    val isDone: Boolean = false,
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Todoable {
    /** A-10: 桥接 [Todoable] 契约，仅在 [isTodo] = true 时有意义。 */
    override val isCompleted: Boolean get() = isDone
}
