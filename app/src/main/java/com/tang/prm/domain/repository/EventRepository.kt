package com.tang.prm.domain.repository

import com.tang.prm.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getAllEvents(): Flow<List<Event>>
    fun getAllEventsIncludingConversations(): Flow<List<Event>>
    fun getEventById(id: Long): Flow<Event?>
    fun getRecentEvents(limit: Int): Flow<List<Event>>
    fun getEventsByType(type: String): Flow<List<Event>>
    fun searchEvents(keyword: String?): Flow<List<Event>>
    fun searchEventsByType(type: String, keyword: String?): Flow<List<Event>>
    fun getConversationEvents(): Flow<List<Event>>
    fun getEventsWithLocation(): Flow<List<Event>>
    fun getParticipantIdsForEvent(eventId: Long): Flow<List<Long>>
    fun getEventsByContact(contactId: Long): Flow<List<Event>>
    suspend fun insertEvent(event: Event): Long
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(id: Long)
    suspend fun insertEventParticipant(eventId: Long, contactId: Long)
    suspend fun deleteEventParticipant(eventId: Long, contactId: Long)
    suspend fun insertEventWithParticipants(event: Event, participantIds: List<Long>): Long
    suspend fun updateEventWithParticipants(event: Event, participantIds: List<Long>)

    fun getEventCount(): Flow<Int>
    fun getConversationCount(): Flow<Int>
    fun getPhotoCount(): Flow<Int>
    fun getFootprintCount(): Flow<Int>
}
