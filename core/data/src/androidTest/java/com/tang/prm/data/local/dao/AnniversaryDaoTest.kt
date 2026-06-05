package com.tang.prm.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.AnniversaryEntity
import com.tang.prm.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnniversaryDaoTest {
    private lateinit var database: TangDatabase
    private lateinit var dao: AnniversaryDao
    private lateinit var contactDao: ContactDao
    private var contactId: Long = 0

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TangDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.anniversaryDao()
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
        val anniversary = AnniversaryEntity(
            contactId = contactId,
            name = "生日",
            type = "birthday",
            date = 1000L
        )
        dao.insertAnniversary(anniversary)
        val result = dao.getAllAnniversariesWithContact().first()
        assertThat(result).hasSize(1)
        assertThat(result[0].anniversary.name).isEqualTo("生日")
        assertThat(result[0].anniversary.contactId).isEqualTo(contactId)
    }

    @Test
    fun insertWithNullContactId() = runBlocking {
        val anniversary = AnniversaryEntity(
            contactId = null,
            name = "国庆节",
            type = "holiday",
            date = 1000L
        )
        dao.insertAnniversary(anniversary)
        val result = dao.getAllAnniversariesWithContact().first()
        assertThat(result).hasSize(1)
        assertThat(result[0].anniversary.name).isEqualTo("国庆节")
        assertThat(result[0].anniversary.contactId).isNull()
    }

    @Test
    fun getByContactId() = runBlocking {
        dao.insertAnniversary(AnniversaryEntity(contactId = contactId, name = "生日", type = "birthday", date = 1000L))
        val result = dao.getAnniversariesByContactWithContact(contactId).first()
        assertThat(result).hasSize(1)
        assertThat(result[0].anniversary.name).isEqualTo("生日")
    }

    @Test
    fun deleteAnniversary() = runBlocking {
        val id = dao.insertAnniversary(AnniversaryEntity(contactId = contactId, name = "生日", type = "birthday", date = 1000L))
        dao.deleteAnniversaryById(id)
        val result = dao.getAllAnniversariesWithContact().first()
        assertThat(result).isEmpty()
    }
}
