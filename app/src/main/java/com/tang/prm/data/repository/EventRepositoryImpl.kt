package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.EventDao
import com.tang.prm.data.local.dao.TodoDao
import com.tang.prm.data.local.dao.ReminderDao
import com.tang.prm.data.local.dao.FavoriteDao
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.EventParticipantCrossRef
import com.tang.prm.data.local.entity.EventWithParticipants
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import androidx.room.withTransaction
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventTypes
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.util.escapeSqlWildcards
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val todoDao: TodoDao,
    private val reminderDao: ReminderDao,
    private val favoriteDao: FavoriteDao,
    private val database: TangDatabase
) : EventRepository {

    private fun EventWithParticipants.toDomain() = Event(
        id = event.id,
        type = event.type,
        title = event.title,
        description = event.description,
        time = event.time,
        endTime = event.endTime,
        location = event.location,
        latitude = event.latitude,
        longitude = event.longitude,
        photos = event.photos,
        emotion = event.emotion,
        weather = event.weather,
        amount = event.amount,
        remarks = event.remarks,
        promise = event.promise,
        conversationSummary = event.conversationSummary,
        giftName = event.giftName,
        participants = participants.map { it.toDomain() },
        createdAt = event.createdAt,
        updatedAt = event.updatedAt
    )

    private fun List<EventWithParticipants>.toDomainList() = map { it.toDomain() }

    override fun getAllEvents(): Flow<List<Event>> =
        eventDao.getAllEventsWithParticipants().map { list ->
            list.filter { it.event.type != EventTypes.CONVERSATION }.toDomainList()
        }

    override fun getAllEventsIncludingConversations(): Flow<List<Event>> =
        eventDao.getAllEventsWithParticipants().map { it.toDomainList() }

    override fun getEventById(id: Long): Flow<Event?> =
        eventDao.getEventByIdWithParticipants(id).map { it?.toDomain() }

    override fun getRecentEvents(limit: Int): Flow<List<Event>> =
        eventDao.getRecentEventsWithParticipants(limit).map { list ->
            list.filter { it.event.type != EventTypes.CONVERSATION }.toDomainList()
        }

    override fun getEventsByType(type: String): Flow<List<Event>> =
        eventDao.getEventsByTypeWithParticipants(type).map { it.toDomainList() }

    override fun searchEvents(keyword: String?): Flow<List<Event>> {
        val escapedKeyword = keyword?.escapeSqlWildcards()
        return eventDao.searchNonConversationEvents(escapedKeyword).map { it.toDomainList() }
    }

    override fun searchEventsByType(type: String, keyword: String?): Flow<List<Event>> {
        val escapedKeyword = keyword?.escapeSqlWildcards()
        return eventDao.searchEventsByTypeWithParticipants(type, escapedKeyword).map { it.toDomainList() }
    }

    override fun getConversationEvents(): Flow<List<Event>> =
        eventDao.getConversationEventsWithParticipants().map { it.toDomainList() }

    override fun getEventsWithLocation(): Flow<List<Event>> =
        eventDao.getEventsWithLocation().map { it.toDomainList() }

    override fun getParticipantIdsForEvent(eventId: Long): Flow<List<Long>> =
        eventDao.getParticipantIdsForEvent(eventId)

    override fun getEventsByContact(contactId: Long): Flow<List<Event>> =
        eventDao.getEventsByContactWithParticipants(contactId).map { it.toDomainList() }

    override suspend fun insertEvent(event: Event): Long =
        eventDao.insertEvent(event.toEntity())

    override suspend fun updateEvent(event: Event) =
        eventDao.updateEvent(event.toEntity())

    override suspend fun deleteEvent(id: Long) = database.withTransaction {
        favoriteDao.deleteEventFavorites(id)
        todoDao.deleteTodosByEvent(id)
        reminderDao.deleteRemindersByEvent(id)
        eventDao.deleteEventById(id)
    }

    override suspend fun insertEventParticipant(eventId: Long, contactId: Long) =
        eventDao.insertEventParticipant(EventParticipantCrossRef(eventId, contactId))

    override suspend fun deleteEventParticipant(eventId: Long, contactId: Long) =
        eventDao.deleteEventParticipant(EventParticipantCrossRef(eventId, contactId))

    override suspend fun insertEventWithParticipants(event: Event, participantIds: List<Long>): Long {
        return database.withTransaction {
            val eventId = eventDao.insertEvent(event.toEntity())
            participantIds.forEach { contactId ->
                eventDao.insertEventParticipant(EventParticipantCrossRef(eventId, contactId))
            }
            eventId
        }
    }

    override suspend fun updateEventWithParticipants(event: Event, participantIds: List<Long>) {
        database.withTransaction {
            eventDao.updateEvent(event.toEntity())
            eventDao.deleteParticipantsByEvent(event.id)
            participantIds.forEach { contactId ->
                eventDao.insertEventParticipant(EventParticipantCrossRef(event.id, contactId))
            }
        }
    }

    override fun getEventCount(): Flow<Int> = eventDao.getEventCount()

    override fun getConversationCount(): Flow<Int> = eventDao.getConversationCount()

    override fun getPhotoCount(): Flow<Int> = eventDao.getPhotoCount()

    override fun getFootprintCount(): Flow<Int> = eventDao.getFootprintCount()
}
