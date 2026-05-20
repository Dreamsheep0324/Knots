package com.tang.prm.domain.model

data class TodoItem(
    val id: Long = 0,
    val contactId: Long? = null,
    val eventId: Long? = null,
    val title: String,
    val isCompleted: Boolean = false,
    val priority: Int = 0,
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
