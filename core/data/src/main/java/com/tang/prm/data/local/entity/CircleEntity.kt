package com.tang.prm.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "circles",
    indices = [Index("parentCircleId")],
    foreignKeys = [
        ForeignKey(
            entity = CircleEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentCircleId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class CircleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val color: String = "#6366F1",
    val icon: String = "people",
    val waveform: String = "sine",
    val parentCircleId: Long? = null,
    val intimacyThreshold: Int = 0,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
