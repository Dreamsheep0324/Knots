package com.tang.prm.engine.divination.model

import kotlinx.serialization.Serializable

@Serializable
data class TrigramInfo(
    val name: String,
    val symbol: String,
    val nature: String,
    val element: String,
    val lines: List<Int>
)
