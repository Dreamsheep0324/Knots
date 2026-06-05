package com.tang.prm.engine.divination.model

import kotlinx.serialization.Serializable

@Serializable
data class GanZhiInfo(
    val year: String,
    val month: String,
    val day: String,
    val hour: String
)
