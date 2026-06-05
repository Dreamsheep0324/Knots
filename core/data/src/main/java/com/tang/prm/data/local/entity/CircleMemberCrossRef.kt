package com.tang.prm.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "circle_member_cross_ref",
    primaryKeys = ["circleId", "contactId"],
    foreignKeys = [
        ForeignKey(entity = CircleEntity::class, parentColumns = ["id"], childColumns = ["circleId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ContactEntity::class, parentColumns = ["id"], childColumns = ["contactId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("contactId")]
)
data class CircleMemberCrossRef(
    val circleId: Long,
    val contactId: Long
)
