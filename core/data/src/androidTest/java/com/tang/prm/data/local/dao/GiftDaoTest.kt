package com.tang.prm.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.ContactEntity
import com.tang.prm.data.local.entity.GiftEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GiftDaoTest {
    private lateinit var database: TangDatabase
    private lateinit var dao: GiftDao
    private lateinit var contactDao: ContactDao
    private var contactId: Long = 0

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TangDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.giftDao()
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
        val gift = GiftEntity(
            contactId = contactId,
            giftName = "礼物",
            giftType = "physical",
            date = 1000L,
            isSent = true,
            occasion = null,
            description = null,
            location = null
        )
        dao.insertGift(gift)
        val result = dao.getAllGifts().first()
        assertThat(result).hasSize(1)
        assertThat(result[0].giftName).isEqualTo("礼物")
    }

    @Test
    fun getByContactId() = runBlocking {
        dao.insertGift(GiftEntity(contactId = contactId, giftName = "礼物", giftType = "physical", date = 1000L, isSent = true, occasion = null, description = null, location = null))
        val result = dao.getGiftsByContactId(contactId).first()
        assertThat(result).hasSize(1)
        assertThat(result[0].giftName).isEqualTo("礼物")
    }

    @Test
    fun getGiftByIdOnce() = runBlocking {
        val id = dao.insertGift(GiftEntity(contactId = contactId, giftName = "礼物", giftType = "physical", date = 1000L, isSent = true, occasion = null, description = null, location = null))
        val result = dao.getGiftByIdOnce(id)
        assertThat(result).isNotNull()
        assertThat(result!!.giftName).isEqualTo("礼物")
    }

    @Test
    fun getGiftByIdOnce_notFound() = runBlocking {
        val result = dao.getGiftByIdOnce(999L)
        assertThat(result).isNull()
    }

    @Test
    fun getGiftsByContactIdOnce() = runBlocking {
        dao.insertGift(GiftEntity(contactId = contactId, giftName = "礼物1", giftType = "physical", date = 1000L, isSent = true, occasion = null, description = null, location = null))
        dao.insertGift(GiftEntity(contactId = contactId, giftName = "礼物2", giftType = "physical", date = 2000L, isSent = false, occasion = null, description = null, location = null))
        val result = dao.getGiftsByContactIdOnce(contactId)
        assertThat(result).hasSize(2)
    }

    @Test
    fun deleteGift() = runBlocking {
        val id = dao.insertGift(GiftEntity(contactId = contactId, giftName = "礼物", giftType = "physical", date = 1000L, isSent = true, occasion = null, description = null, location = null))
        dao.deleteGiftById(id)
        val result = dao.getAllGifts().first()
        assertThat(result).isEmpty()
    }

    @Test
    fun deleteGiftsByContactId() = runBlocking {
        dao.insertGift(GiftEntity(contactId = contactId, giftName = "礼物", giftType = "physical", date = 1000L, isSent = true, occasion = null, description = null, location = null))
        dao.deleteGiftsByContactId(contactId)
        val result = dao.getAllGifts().first()
        assertThat(result).isEmpty()
    }
}
