package com.tang.prm.domain.divination.model

/**
 * Domain-level divination record model.
 * Independent of the engine module, following Dependency Inversion Principle.
 * The data layer maps between this and [com.tang.prm.engine.divination.model.DivinationRecord].
 */
data class DivinationRecord(
    val id: Long = 0,
    val method: String,
    val question: String,
    val resultJson: String,
    val createdAt: Long,
    val aiAnalysis: String = ""
)
