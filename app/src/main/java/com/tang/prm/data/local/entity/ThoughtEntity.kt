package com.tang.prm.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "thoughts",
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("contactId")]
)
data class ThoughtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long?,
    val content: String,
    val type: String = "murmur",
    val isPrivate: Boolean = false,
    val isTodo: Boolean = false,
    val isDone: Boolean = false,
    val dueDate: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)
