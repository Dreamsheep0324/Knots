package com.tang.prm.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderDaoTest {
    private lateinit var database: TangDatabase
    private lateinit var dao: ReminderDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TangDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.reminderDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAll() = runBlocking {
        val reminder = ReminderEntity(
            type = "anniversary",
            title = "提醒",
            content = "内容",
            time = System.currentTimeMillis() + 10000L
        )
        dao.insertReminder(reminder)
        val result = dao.getActiveReminders().first()
        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("提醒")
    }

    @Test
    fun markCompleted() = runBlocking {
        val id = dao.insertReminder(ReminderEntity(
            type = "anniversary",
            title = "提醒",
            content = "内容",
            time = System.currentTimeMillis() + 10000L,
            isCompleted = false
        ))
        dao.markReminderCompleted(id)
        val result = dao.getActiveReminders().first()
        assertThat(result).isEmpty()
    }

    @Test
    fun deleteReminder() = runBlocking {
        val id = dao.insertReminder(ReminderEntity(
            type = "anniversary",
            title = "提醒",
            content = "内容",
            time = System.currentTimeMillis() + 10000L
        ))
        dao.deleteReminderById(id)
        val result = dao.getActiveReminders().first()
        assertThat(result).isEmpty()
    }
}
