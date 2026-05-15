package com.tang.prm.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "favorites",
    indices = [Index(value = ["sourceType", "sourceId"], unique = true), Index("createdAt")]
)
data class FavoriteEntity(
    @androidx.room.PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceType: String,
    val sourceId: Long,
    val title: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
