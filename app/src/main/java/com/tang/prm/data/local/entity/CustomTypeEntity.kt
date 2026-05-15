package com.tang.prm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CustomCategory(val value: String) {
    EVENT_TYPE("EVENT_TYPE"),
    EMOTION("EMOTION"),
    RELATIONSHIP("RELATIONSHIP"),
    ANNIVERSARY_TYPE("ANNIVERSARY_TYPE")
}

@Entity(tableName = "custom_types")
data class CustomTypeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val name: String,
    val key: String = "",
    val color: String? = null,
    val icon: String? = null,
    val sortOrder: Int = 0,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)