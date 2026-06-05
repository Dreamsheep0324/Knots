package com.tang.prm.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactDaoTest {
    private lateinit var database: TangDatabase
    private lateinit var dao: ContactDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TangDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.contactDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAllContacts() = runBlocking {
        val contact = ContactEntity(name = "张三", phone = "13800138000")
        dao.insertContact(contact)
        val result = dao.getAllContacts().first()
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("张三")
        assertThat(result[0].phone).isEqualTo("13800138000")
    }

    @Test
    fun searchContacts() = runBlocking {
        dao.insertContact(ContactEntity(name = "张三", phone = "13800138000"))
        dao.insertContact(ContactEntity(name = "李四", phone = "13900139000"))
        val result = dao.searchContacts("张").first()
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("张三")
    }

    @Test
    fun updateContact() = runBlocking {
        val id = dao.insertContact(ContactEntity(name = "张三", phone = "13800138000"))
        val inserted = dao.getAllContacts().first()[0]
        dao.updateContact(inserted.copy(name = "李四"))
        val result = dao.getAllContacts().first()
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("李四")
    }

    @Test
    fun deleteContact() = runBlocking {
        val id = dao.insertContact(ContactEntity(name = "张三", phone = "13800138000"))
        dao.deleteContactById(id)
        val result = dao.getAllContacts().first()
        assertThat(result).isEmpty()
    }
}
