package com.tang.prm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gifts",
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("contactId"), Index("date")]
)
data class GiftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long,
    val giftName: String,
    val giftType: String,
    val date: Long,
    val isSent: Boolean,
    val amount: Double?,
    val occasion: String?,
    val description: String?,
    val location: String?,
    val photos: List<String> = emptyList(),
    @ColumnInfo(name = "photos_count", defaultValue = "0")
    val photosCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
