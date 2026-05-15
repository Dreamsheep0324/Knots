package com.tang.prm.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "circle_member_cross_ref",
    foreignKeys = [
        ForeignKey(entity = CircleEntity::class, parentColumns = ["id"], childColumns = ["circleId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ContactEntity::class, parentColumns = ["id"], childColumns = ["contactId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("circleId"), Index("contactId")]
)
data class CircleMemberCrossRef(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val circleId: Long,
    val contactId: Long
)
