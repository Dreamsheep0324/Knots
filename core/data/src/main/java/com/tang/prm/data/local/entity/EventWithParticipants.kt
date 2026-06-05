package com.tang.prm.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class EventWithParticipants(
    @Embedded val event: EventEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        entity = ContactEntity::class,
        associateBy = Junction(
            value = EventParticipantCrossRef::class,
            parentColumn = "eventId",
            entityColumn = "contactId"
        )
    )
    val participants: List<ContactEntity>
)
