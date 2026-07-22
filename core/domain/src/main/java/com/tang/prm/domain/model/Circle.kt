package com.tang.prm.domain.model

import com.tang.prm.domain.constant.CircleConstants

data class Circle(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val color: String = CircleConstants.DEFAULT_CIRCLE_COLOR,
    val icon: String = "people",
    val waveform: String = "sine",
    val memberIds: List<Long> = emptyList(),
    val parentCircleId: Long? = null,
    val intimacyThreshold: Int = 0,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
