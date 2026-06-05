package com.tang.prm.domain.model

data class Favorite(
    val id: Long = 0,
    val sourceType: String,
    val sourceId: Long,
    val title: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
