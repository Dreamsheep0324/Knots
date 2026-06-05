package com.tang.prm.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CircleWithMembers(
    @Embedded val circle: CircleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "circleId"
    )
    val members: List<CircleMemberCrossRef>
)
