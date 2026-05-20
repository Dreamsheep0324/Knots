package com.tang.prm.data.repository

import androidx.room.withTransaction
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.dao.EventDao
import com.tang.prm.data.local.dao.FavoriteDao
import com.tang.prm.data.local.dao.ReminderDao
import com.tang.prm.data.local.dao.TodoDao
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.EventEntity
import com.tang.prm.data.local.entity.EventWithParticipants
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Event
import com.tang.prm.domain.model.EventType
import com.tang.prm.domain.model.SourceTypes
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class EventRepositoryImplTest {

    @MockK
    private lateinit var eventDao: EventDao

    @MockK
    private lateinit var todoDao: TodoDao

    @MockK
    private lateinit var reminderDao: ReminderDao

    @MockK
    private lateinit var favoriteDao: FavoriteDao

    @MockK
    private lateinit var database: TangDatabase

    private lateinit var repository: EventRepositoryImpl

    private val eventEntity = EventEntity(id = 1, type = EventType.MEETUP.name, title = "Meet", time = 1000L, createdAt = 0, updatedAt = 0)
    private val eventWithParticipants = EventWithParticipants(event = eventEntity, participants = emptyList())
    private val domain = Event(id = 1, type = EventType.MEETUP, title = "Meet", time = 1000L, createdAt = 0, updatedAt = 0)

    @BeforeEach
    fun setUp() {
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { any<androidx.room.RoomDatabase>().withTransaction(any<suspend () -> Any>()) } coAnswers {
            secondArg<suspend () -> Any>().invoke()
        }
        repository = EventRepositoryImpl(eventDao, todoDao, reminderDao, favoriteDao, database)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun getAllEvents_filtersOutConversations() = runTest {
        val convEntity = EventEntity(id = 2, type = EventType.CONVERSATION.name, title = "Chat", time = 2000L, createdAt = 0, updatedAt = 0)
        val convWithParticipants = EventWithParticipants(event = convEntity, participants = emptyList())
        every { eventDao.getAllEventsWithParticipants() } returns flowOf(listOf(eventWithParticipants, convWithParticipants))

        val result = repository.getAllEvents().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].type).isEqualTo(EventType.MEETUP)
    }

    @Test
    fun searchEvents_callsDao() = runTest {
        every { eventDao.searchNonConversationEvents(null) } returns flowOf(listOf(eventWithParticipants))

        val result = repository.searchEvents(null).first()

        assertThat(result).hasSize(1)
    }

    @Test
    fun insertEvent_callsDaoWithEntity() = runTest {
        coEvery { eventDao.insertEvent(any()) } returns 1L

        val result = repository.insertEvent(domain)

        assertThat(result).isEqualTo(1L)
        coVerify { eventDao.insertEvent(eventEntity) }
    }

    @Test
    fun deleteEvent_callsDaoDeleteById() = runTest {
        coEvery { eventDao.getEventByIdOnce(1L) } returns null
        coEvery { favoriteDao.deleteEventFavorites(1L, listOf(SourceTypes.EVENT)) } returns Unit
        coEvery { todoDao.deleteTodosByEvent(1L) } returns Unit
        coEvery { reminderDao.deleteRemindersByEvent(1L) } returns Unit
        coEvery { eventDao.deleteEventById(1L) } returns Unit

        repository.deleteEvent(1L)

        coVerify { eventDao.deleteEventById(1L) }
    }
}
