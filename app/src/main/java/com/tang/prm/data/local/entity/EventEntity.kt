package com.tang.prm.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    indices = [Index("time")]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val title: String,
    val description: String? = null,
    val time: Long,
    val endTime: Long? = null,
    val location: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photos: List<String> = emptyList(),
    val emotion: String? = null,
    val weather: String? = null,
    val amount: Double? = null,
    val remarks: String? = null,
    val promise: String? = null,
    val conversationSummary: String? = null,
    val giftName: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "event_participants",
    primaryKeys = ["eventId", "contactId"],
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contactId")]
)
data class EventParticipantCrossRef(
    val eventId: Long,
    val contactId: Long
)

@Entity(tableName = "todo_items")
data class TodoItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long? = null,
    val eventId: Long? = null,
    val title: String,
    val isCompleted: Boolean = false,
    val priority: Int = 0,
    val dueDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long? = null,
    val eventId: Long? = null,
    val anniversaryId: Long? = null,
    val type: String,
    val title: String,
    val content: String,
    val time: Long,
    val isCompleted: Boolean = false,
    val isIgnored: Boolean = false,
    val repeatInterval: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
