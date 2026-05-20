package com.tang.prm.domain.divination.model

data class TrigramInfo(
    val name: String,
    val symbol: String,
    val nature: String,
    val element: String,
    val lines: List<Int>
)
