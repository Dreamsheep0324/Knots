package com.tang.prm.domain.repository

import com.tang.prm.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getAllEvents(): Flow<List<Event>>
    fun getEventById(id: Long): Flow<Event?>
    fun getRecentEvents(limit: Int): Flow<List<Event>>
    fun getEventsByType(type: String): Flow<List<Event>>
    fun searchEvents(keyword: String?): Flow<List<Event>>
    fun getEventsWithLocation(): Flow<List<Event>>
    fun getParticipantIdsForEvent(eventId: Long): Flow<List<Long>>
    fun getEventsByContact(contactId: Long): Flow<List<Event>>
    suspend fun insertEvent(event: Event): Long
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(id: Long)
    suspend fun insertEventParticipant(eventId: Long, contactId: Long)
    suspend fun deleteEventParticipant(eventId: Long, contactId: Long)

    /**
     * B-8 修复：在单一 Room 事务中插入事件及其参与者。
     *
     * @param event 待插入的事件（id 通常为 0，由 DB 自增）
     * @param participantIds 参与者联系人 ID 列表
     * @return 新事件 ID
     */
    suspend fun insertEventWithParticipants(event: Event, participantIds: List<Long>): Long

    /**
     * B-8 修复：在单一 Room 事务中更新事件并同步参与者 diff。
     *
     * @param event 待更新的事件
     * @param participantIds 期望的最终参与者列表（diff 后增/删）
     */
    suspend fun updateEventWithParticipants(event: Event, participantIds: List<Long>)

    fun getEventCount(): Flow<Int>
    fun getEventCountByType(type: String): Flow<Int>
    fun getEventCountWithLocation(): Flow<Int>
    fun getPhotoCount(): Flow<Int>
}
