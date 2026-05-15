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
)
