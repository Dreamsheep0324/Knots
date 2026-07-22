package com.tang.prm.data.local.dao

import androidx.room.*
import com.tang.prm.data.local.entity.EventEntity
import com.tang.prm.data.local.entity.EventLocationItemWithParticipants
import com.tang.prm.data.local.entity.EventParticipantCrossRef
import com.tang.prm.data.local.entity.EventWithParticipants
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Transaction
    @Query("SELECT * FROM events WHERE type != 'CONVERSATION' ORDER BY time DESC")
    fun getAllEventsWithParticipants(): Flow<List<EventWithParticipants>>

    @Transaction
    @Query("SELECT * FROM events WHERE id = :id")
    fun getEventByIdWithParticipants(id: Long): Flow<EventWithParticipants?>

    @Transaction
    @Query("SELECT * FROM events WHERE type != 'CONVERSATION' ORDER BY time DESC LIMIT :limit")
    fun getRecentEventsWithParticipants(limit: Int): Flow<List<EventWithParticipants>>

    @Transaction
    @Query("SELECT * FROM events WHERE type != 'CONVERSATION' AND (:keyword IS NULL OR title LIKE '%' || :keyword || '%' ESCAPE '\\' OR description LIKE '%' || :keyword || '%' ESCAPE '\\' OR location LIKE '%' || :keyword || '%' ESCAPE '\\') ORDER BY time DESC")
    fun searchNonConversationEvents(keyword: String?): Flow<List<EventWithParticipants>>

    @Transaction
    @Query("SELECT * FROM events WHERE type = :type ORDER BY time DESC")
    fun getEventsByTypeWithParticipants(type: String): Flow<List<EventWithParticipants>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvent(event: EventEntity): Long

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventByIdOnce(id: Long): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEventParticipant(crossRef: EventParticipantCrossRef)

    @Delete
    suspend fun deleteEventParticipant(crossRef: EventParticipantCrossRef)

    @Query("SELECT contactId FROM event_participants WHERE eventId = :eventId")
    fun getParticipantIdsForEvent(eventId: Long): Flow<List<Long>>

    @Transaction
    @Query("SELECT e.* FROM events e INNER JOIN event_participants ep ON e.id = ep.eventId WHERE ep.contactId = :contactId ORDER BY e.time DESC")
    fun getEventsByContactWithParticipants(contactId: Long): Flow<List<EventWithParticipants>>

    @Query("SELECT contactId FROM event_participants WHERE eventId = :eventId")
    suspend fun getParticipantIdsForEventOnce(eventId: Long): List<Long>

    @Query("DELETE FROM event_participants WHERE eventId = :eventId")
    suspend fun deleteParticipantsByEvent(eventId: Long)

    @Query("SELECT COUNT(*) FROM events WHERE type != 'CONVERSATION'")
    fun getEventCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM events WHERE type = :type")
    fun getEventCountByType(type: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM events WHERE location IS NOT NULL AND location != ''")
    fun getEventCountWithLocation(): Flow<Int>

    /**
     * Lightweight projection of events with location, for footprint aggregation.
     * Avoids reading photos JSON, remarks, promise, conversationSummary, giftName,
     * amount, latitude, longitude, endTime; participants use [ContactListItemEntity] projection.
     */
    @Transaction
    @Query("SELECT id, type, title, customTypeName, description, time, location, weather, emotion, photos_count, createdAt, updatedAt FROM events WHERE location IS NOT NULL AND location != '' ORDER BY time DESC")
    fun getEventLocationItemsWithParticipants(): Flow<List<EventLocationItemWithParticipants>>

    @Query("SELECT COALESCE(SUM(photos_count), 0) FROM events")
    fun getPhotoCount(): Flow<Int>

    /** 仅查询非空 photos 字段，用于清理孤儿图片时避免全表扫描 */
    @Query("SELECT photos FROM events WHERE photos IS NOT NULL AND photos != '' AND photos != '[]'")
    suspend fun getReferencedPhotoPaths(): List<String>
}
