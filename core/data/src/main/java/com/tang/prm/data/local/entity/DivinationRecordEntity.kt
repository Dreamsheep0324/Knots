package com.tang.prm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "divination_records")
data class DivinationRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val method: String,
    val question: String,
    val resultJson: String,
    val createdAt: Long,
    val aiAnalysis: String = ""
)
