package com.tang.prm.domain.model

data class Reminder(
    val id: Long = 0,
    val contactId: Long? = null,
    val eventId: Long? = null,
    val anniversaryId: Long? = null,
    val type: String,
    val title: String,
    val content: String,
    val time: Long,
    val isCompleted: Boolean = false,
    val isIgnored: Boolean = false,
    val repeatInterval: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
