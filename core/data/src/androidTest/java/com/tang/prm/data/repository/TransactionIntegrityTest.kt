package com.tang.prm.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tang.prm.data.local.dao.*
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.*
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransactionIntegrityTest {

    private lateinit var db: TangDatabase
    private lateinit var contactDao: ContactDao
    private lateinit var eventDao: EventDao
    private lateinit var giftDao: GiftDao
    private lateinit var anniversaryDao: AnniversaryDao
    private lateinit var thoughtDao: ThoughtDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, TangDatabase::class.java).build()
        contactDao = db.contactDao()
        eventDao = db.eventDao()
        giftDao = db.giftDao()
        anniversaryDao = db.anniversaryDao()
        thoughtDao = db.thoughtDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun deleteContactCascadesToEvents() = runTest {
        contactDao.insertContact(ContactEntity(id = 1, name = "测试", phone = "138"))
        eventDao.insertEvent(EventEntity(id = 1, contactId = 1, title = "事件"))

        contactDao.deleteContactById(1)

        val events = eventDao.getEventsByContactOnce(1)
        assertThat(events).isEmpty()
    }

    @Test
    fun deleteContactCascadesToGifts() = runTest {
        contactDao.insertContact(ContactEntity(id = 1, name = "测试", phone = "138"))
        giftDao.insertGift(GiftEntity(id = 1, contactId = 1, name = "礼物"))

        contactDao.deleteContactById(1)

        val gifts = giftDao.getGiftsByContactIdOnce(1)
        assertThat(gifts).isEmpty()
    }

    @Test
    fun deleteContactCascadesToAnniversaries() = runTest {
        contactDao.insertContact(ContactEntity(id = 1, name = "测试", phone = "138"))
        anniversaryDao.insertAnniversary(AnniversaryEntity(id = 1, contactId = 1, title = "纪念日"))

        contactDao.deleteContactById(1)

        val anniversaries = anniversaryDao.getAnniversariesByContactOnce(1)
        assertThat(anniversaries).isEmpty()
    }

    @Test
    fun deleteContactCascadesToThoughts() = runTest {
        contactDao.insertContact(ContactEntity(id = 1, name = "测试", phone = "138"))
        thoughtDao.insertThought(ThoughtEntity(id = 1, contactId = 1, content = "感悟"))

        contactDao.deleteContactById(1)

        val thoughts = thoughtDao.getThoughtsByContactOnce(1)
        assertThat(thoughts).isEmpty()
    }

    @Test
    fun deleteContactCascadesToAllRelatedData() = runTest {
        contactDao.insertContact(ContactEntity(id = 1, name = "测试", phone = "138"))
        eventDao.insertEvent(EventEntity(id = 1, contactId = 1, title = "事件"))
        giftDao.insertGift(GiftEntity(id = 1, contactId = 1, name = "礼物"))
        anniversaryDao.insertAnniversary(AnniversaryEntity(id = 1, contactId = 1, title = "纪念日"))
        thoughtDao.insertThought(ThoughtEntity(id = 1, contactId = 1, content = "感悟"))

        contactDao.deleteContactById(1)

        assertThat(eventDao.getEventsByContactOnce(1)).isEmpty()
        assertThat(giftDao.getGiftsByContactIdOnce(1)).isEmpty()
        assertThat(anniversaryDao.getAnniversariesByContactOnce(1)).isEmpty()
        assertThat(thoughtDao.getThoughtsByContactOnce(1)).isEmpty()
    }
}
