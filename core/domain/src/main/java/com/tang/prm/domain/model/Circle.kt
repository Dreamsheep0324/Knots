package com.tang.prm.domain.model

data class Circle(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val color: String = "#6366F1",
    val icon: String = "people",
    val waveform: String = "sine",
    val memberIds: List<Long> = emptyList(),
    val parentCircleId: Long? = null,
    val intimacyThreshold: Int = 0,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
