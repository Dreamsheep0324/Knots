package com.tang.prm.data.repository

import android.content.Context
import com.tang.prm.data.local.dao.EventDao
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
import com.tang.prm.domain.repository.FavoriteRepository
import com.tang.prm.data.util.ImageFileManager
import com.tang.prm.data.util.computeRemovedPhotos
import com.tang.prm.util.escapeSqlWildcards
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val favoriteRepository: FavoriteRepository,
    private val database: TangDatabase,
    @ApplicationContext private val context: Context
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
            // REP-Q-5 修复：复用 computeRemovedPhotos 统一 photos 差集计算逻辑。
            val removed = computeRemovedPhotos(oldEntity, event.photos) { it.photos }
            eventDao.updateEvent(event.toEntity())
            removed
        }
        // 事务外删除文件（文件 I/O 不阻塞数据库事务）
        removedPhotos?.let { ImageFileManager.deleteLocalPhotos(context, it.toList()) }
    }

    override suspend fun deleteEvent(id: Long) {
        // REP-A-2 修复：移除 todoDao/reminderDao（FK CASCADE 自动处理），favoriteDao 替换为 favoriteRepository
        val photosToDelete = database.withTransaction {
            val photos = eventDao.getEventByIdOnce(id)?.photos ?: emptyList()
            favoriteRepository.deleteFavoriteBySource(SourceTypes.EVENT, id)
            // todoDao/reminderDao 删除由 FK CASCADE 自动处理
            eventDao.deleteEventById(id)
            photos
        }
        // 事务外删除文件
        ImageFileManager.deleteLocalPhotos(context, photosToDelete)
    }

    override suspend fun insertEventParticipant(eventId: Long, contactId: Long) =
        eventDao.insertEventParticipant(com.tang.prm.data.local.entity.EventParticipantCrossRef(eventId, contactId))

    override suspend fun deleteEventParticipant(eventId: Long, contactId: Long) =
        eventDao.deleteEventParticipant(com.tang.prm.data.local.entity.EventParticipantCrossRef(eventId, contactId))

    override suspend fun insertEventWithParticipants(
        event: Event,
        participantIds: List<Long>
    ): Long = database.withTransaction {
        // B-8 修复：单一事务包裹事件插入 + 参与者批量插入
        val eventId = eventDao.insertEvent(event.toEntity())
        participantIds.forEach { contactId ->
            eventDao.insertEventParticipant(
                com.tang.prm.data.local.entity.EventParticipantCrossRef(eventId, contactId)
            )
        }
        eventId
    }

    override suspend fun updateEventWithParticipants(
        event: Event,
        participantIds: List<Long>
    ) {
        // B-8 修复：单一事务包裹事件更新 + 参与者 diff
        val removedPhotos = database.withTransaction {
            val oldEntity = eventDao.getEventByIdOnce(event.id)
            val removed = computeRemovedPhotos(oldEntity, event.photos) { it.photos }
            eventDao.updateEvent(event.toEntity())
            val currentParticipants = eventDao.getParticipantIdsForEventOnce(event.id)
            val toAdd = participantIds - currentParticipants.toSet()
            val toRemove = currentParticipants - participantIds.toSet()
            toAdd.forEach { contactId ->
                eventDao.insertEventParticipant(
                    com.tang.prm.data.local.entity.EventParticipantCrossRef(event.id, contactId)
                )
            }
            toRemove.forEach { contactId ->
                eventDao.deleteEventParticipant(
                    com.tang.prm.data.local.entity.EventParticipantCrossRef(event.id, contactId)
                )
            }
            removed
        }
        // 事务外删除文件
        removedPhotos?.let { ImageFileManager.deleteLocalPhotos(context, it.toList()) }
    }

    override fun getEventCount(): Flow<Int> = eventDao.getEventCount()

    override fun getEventCountByType(type: String): Flow<Int> = eventDao.getEventCountByType(type)

    override fun getEventCountWithLocation(): Flow<Int> = eventDao.getEventCountWithLocation()

    override fun getPhotoCount(): Flow<Int> = eventDao.getPhotoCount()
}
