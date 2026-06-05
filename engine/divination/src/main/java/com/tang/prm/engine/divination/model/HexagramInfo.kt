package com.tang.prm.engine.divination.model

import kotlinx.serialization.Serializable

@Serializable
data class HexagramInfo(
    val id: Int,
    val name: String,
    val symbol: String,
    val binarySymbol: String,
    val upper: String,
    val lower: String,
    val palace: String,
    val description: String,
    val guaciMeaning: String = "",
    val hexagramMeaning: String = ""
)
