package com.tang.prm.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.ContactEntity
import com.tang.prm.data.local.entity.ThoughtEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThoughtDaoTest {
    private lateinit var database: TangDatabase
    private lateinit var dao: ThoughtDao
    private lateinit var contactDao: ContactDao
    private var contactId: Long = 0

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TangDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.thoughtDao()
        contactDao = database.contactDao()
        runBlocking {
            contactId = contactDao.insertContact(ContactEntity(name = "张三"))
        }
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAll() = runBlocking {
        val thought = ThoughtEntity(
            contactId = contactId,
            content = "想法",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        dao.insertThought(thought)
        val result = dao.getAllThoughts().first()
        assertThat(result).hasSize(1)
        assertThat(result[0].content).isEqualTo("想法")
    }

    @Test
    fun getByContactId() = runBlocking {
        dao.insertThought(ThoughtEntity(contactId = contactId, content = "想法", createdAt = 1000L, updatedAt = 1000L))
        val result = dao.getThoughtsByContact(contactId).first()
        assertThat(result).hasSize(1)
        assertThat(result[0].content).isEqualTo("想法")
    }

    @Test
    fun deleteThought() = runBlocking {
        val id = dao.insertThought(ThoughtEntity(contactId = contactId, content = "想法", createdAt = 1000L, updatedAt = 1000L))
        dao.deleteThoughtById(id)
        val result = dao.getAllThoughts().first()
        assertThat(result).isEmpty()
    }
}
