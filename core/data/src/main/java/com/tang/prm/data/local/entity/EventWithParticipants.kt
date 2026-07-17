package com.tang.prm.data.local.entity

import androidx.room.ColumnInfo
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

/**
 * Lightweight column-projection POJO for events with location (footprint list).
 *
 * Avoids reading large/unused columns (photos JSON, remarks, promise,
 * conversationSummary, giftName, amount, latitude, longitude, endTime) that
 * the footprint aggregation does not need. [photosCount] replaces [EventEntity.photos]
 * for photo-count display.
 */
data class EventLocationItemEntity(
    val id: Long = 0,
    val type: String,
    val title: String,
    val customTypeName: String? = null,
    val description: String? = null,
    val time: Long,
    val location: String? = null,
    val weather: String? = null,
    val emotion: String? = null,
    @ColumnInfo(name = "photos_count") val photosCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * [EventLocationItemEntity] with lightweight participant projection ([ContactListItemEntity]),
 * avoiding full [ContactEntity] load (notes, customFields, hobby, habit, etc.).
 */
data class EventLocationItemWithParticipants(
    @Embedded val event: EventLocationItemEntity,
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
    val participants: List<com.tang.prm.data.local.dao.ContactListItemEntity>
)
