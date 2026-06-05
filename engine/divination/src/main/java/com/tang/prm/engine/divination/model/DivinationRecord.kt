package com.tang.prm.engine.divination.model

data class DivinationRecord(
    val id: Long = 0,
    val method: String,
    val question: String,
    val resultJson: String,
    val createdAt: Long,
    val aiAnalysis: String = ""
)
