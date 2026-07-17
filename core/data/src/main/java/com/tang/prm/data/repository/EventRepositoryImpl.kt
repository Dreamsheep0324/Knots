package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.EventDao
import com.tang.prm.data.local.dao.TodoDao
import com.tang.prm.data.local.dao.ReminderDao
import com.tang.prm.data.local.dao.FavoriteDao
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.mapper.mapList
import com.tang.prm.data.mapper.mapNullable
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import androidx.room.withTransaction
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.repository.EventRepository
import com.tang.prm.data.util.ImageFileManager
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

    override fun getAllEvents(): Flow<List<Event>> =
        eventDao.getAllEventsWithParticipants().mapList { it.toDomain() }

    override fun getEventById(id: Long): Flow<Event?> =
        eventDao.getEventByIdWithParticipants(id).mapNullable { it.toDomain() }

    override fun getRecentEvents(limit: Int): Flow<List<Event>> =
        eventDao.getRecentEventsWithParticipants(limit).mapList { it.toDomain() }

    override fun getEventsByType(type: String): Flow<List<Event>> =
        eventDao.getEventsByTypeWithParticipants(type).mapList { it.toDomain() }

    override fun searchEvents(keyword: String?): Flow<List<Event>> {
        val escapedKeyword = keyword?.escapeSqlWildcards()
        return eventDao.searchNonConversationEvents(escapedKeyword).mapList { it.toDomain() }
    }

    override fun searchEventsByType(type: String, keyword: String?): Flow<List<Event>> {
        val escapedKeyword = keyword?.escapeSqlWildcards()
        return eventDao.searchEventsByTypeWithParticipants(type, escapedKeyword).mapList { it.toDomain() }
    }

    override fun getEventsWithLocation(): Flow<List<Event>> =
        eventDao.getEventLocationItemsWithParticipants().mapList { it.toDomain() }

    override fun getParticipantIdsForEvent(eventId: Long): Flow<List<Long>> =
        eventDao.getParticipantIdsForEvent(eventId)

    override fun getEventsByContact(contactId: Long): Flow<List<Event>> =
        eventDao.getEventsByContactWithParticipants(contactId).mapList { it.toDomain() }

    override suspend fun insertEvent(event: Event): Long =
        eventDao.insertEvent(event.toEntity())

    override suspend fun updateEvent(event: Event) {
        // 读+写放在同一事务中，避免竞态导致照片文件误删/漏删
        val removedPhotos = database.withTransaction {
            val oldEntity = eventDao.getEventByIdOnce(event.id)
            val removed = oldEntity?.let { old ->
                (old.photos.toSet() - event.photos.toSet()).takeIf { it.isNotEmpty() }
            }
            eventDao.updateEvent(event.toEntity())
            removed
        }
        // 事务外删除文件（文件 I/O 不阻塞数据库事务）
        removedPhotos?.let { deletePhotoFiles(it.toList()) }
    }

    override suspend fun deleteEvent(id: Long) {
        // 先在事务内收集待删除文件路径 + 删除数据库记录
        val photosToDelete = database.withTransaction {
            val photos = eventDao.getEventByIdOnce(id)?.photos ?: emptyList()
            favoriteDao.deleteEventFavorites(id, listOf(SourceTypes.EVENT))
            todoDao.deleteTodosByEvent(id)
            reminderDao.deleteRemindersByEvent(id)
            eventDao.deleteEventById(id)
            photos
        }
        // 事务外删除文件
        deletePhotoFiles(photosToDelete)
    }

    override suspend fun insertEventParticipant(eventId: Long, contactId: Long) =
        eventDao.insertEventParticipant(com.tang.prm.data.local.entity.EventParticipantCrossRef(eventId, contactId))

    override suspend fun deleteEventParticipant(eventId: Long, contactId: Long) =
        eventDao.deleteEventParticipant(com.tang.prm.data.local.entity.EventParticipantCrossRef(eventId, contactId))

    override fun getEventCount(): Flow<Int> = eventDao.getEventCount()

    override fun getEventCountByType(type: String): Flow<Int> = eventDao.getEventCountByType(type)

    override fun getEventCountWithLocation(): Flow<Int> = eventDao.getEventCountWithLocation()

    override fun getPhotoCount(): Flow<Int> = eventDao.getPhotoCount()

    private suspend fun deletePhotoFiles(photos: List<String>) =
        ImageFileManager.deleteLocalPhotos(photos)
}
