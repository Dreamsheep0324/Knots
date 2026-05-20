package com.tang.prm.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.EventEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventDaoTest {
    private lateinit var database: TangDatabase
    private lateinit var dao: EventDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TangDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.eventDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAll() = runBlocking {
        val event = EventEntity(type = "meeting", title = "会议", time = 1000L)
        dao.insertEvent(event)
        val result = dao.getAllEventsWithParticipants().first()
        assertThat(result).hasSize(1)
        assertThat(result[0].event.title).isEqualTo("会议")
        assertThat(result[0].event.type).isEqualTo("meeting")
    }

    @Test
    fun deleteEvent() = runBlocking {
        val id = dao.insertEvent(EventEntity(type = "meeting", title = "会议", time = 1000L))
        dao.deleteEventById(id)
        val result = dao.getAllEventsWithParticipants().first()
        assertThat(result).isEmpty()
    }
}
