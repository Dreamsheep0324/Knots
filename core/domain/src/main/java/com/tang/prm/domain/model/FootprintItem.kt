package com.tang.prm.domain.model

data class FootprintItem(
    val id: Long,
    val location: String,
    val date: Long,
    val eventType: String,
    val eventTitle: String,
    val contactId: Long?,
    val contactName: String?,
    val contactAvatar: String?,
    val description: String?,
    val weather: String?,
    val emotion: String?,
    val photoCount: Int
)
