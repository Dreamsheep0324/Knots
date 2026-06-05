package com.tang.prm.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.CircleEntity
import com.tang.prm.data.local.entity.CircleMemberCrossRef
import com.tang.prm.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CircleDaoTest {
    private lateinit var database: TangDatabase
    private lateinit var dao: CircleDao
    private lateinit var contactDao: ContactDao
    private var contactId: Long = 0

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TangDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.circleDao()
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
        val circle = CircleEntity(name = "朋友圈")
        dao.insertCircle(circle)
        val result = dao.getAllCircles().first()
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("朋友圈")
    }

    @Test
    fun insertCircleWithMembers() = runBlocking {
        val circle = CircleEntity(name = "朋友圈")
        val id = dao.insertCircleWithMembers(circle, listOf(contactId))
        val result = dao.getAllCirclesWithMembers().first()
        assertThat(result).hasSize(1)
        assertThat(result[0].circle.name).isEqualTo("朋友圈")
        assertThat(result[0].members).hasSize(1)
        assertThat(result[0].members[0].contactId).isEqualTo(contactId)
    }

    @Test
    fun insertMemberCrossRef() = runBlocking {
        val circleId = dao.insertCircle(CircleEntity(name = "朋友圈"))
        dao.insertMemberCrossRef(CircleMemberCrossRef(circleId = circleId, contactId = contactId))
        val memberIds = dao.getMemberIdsForCircle(circleId).first()
        assertThat(memberIds).containsExactly(contactId)
    }

    @Test
    fun deleteMemberCrossRef() = runBlocking {
        val circleId = dao.insertCircle(CircleEntity(name = "朋友圈"))
        dao.insertMemberCrossRef(CircleMemberCrossRef(circleId = circleId, contactId = contactId))
        dao.deleteMemberCrossRef(circleId, contactId)
        val memberIds = dao.getMemberIdsForCircle(circleId).first()
        assertThat(memberIds).isEmpty()
    }

    @Test
    fun deleteCircle() = runBlocking {
        val id = dao.insertCircle(CircleEntity(name = "朋友圈"))
        dao.deleteCircleById(id)
        val result = dao.getAllCircles().first()
        assertThat(result).isEmpty()
    }

    @Test
    fun deleteCircle_cascadeDeletesMembers() = runBlocking {
        val circleId = dao.insertCircle(CircleEntity(name = "朋友圈"))
        dao.insertMemberCrossRef(CircleMemberCrossRef(circleId = circleId, contactId = contactId))
        dao.deleteCircleById(circleId)
        val memberIds = dao.getMemberIdsForCircle(circleId).first()
        assertThat(memberIds).isEmpty()
    }

    @Test
    fun getCirclesForContact() = runBlocking {
        val circleId = dao.insertCircle(CircleEntity(name = "朋友圈"))
        dao.insertMemberCrossRef(CircleMemberCrossRef(circleId = circleId, contactId = contactId))
        val result = dao.getCirclesForContact(contactId).first()
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("朋友圈")
    }

    @Test
    fun updateCircleWithMembers() = runBlocking {
        val circleId = dao.insertCircleWithMembers(CircleEntity(name = "朋友圈"), listOf(contactId))
        val circle = dao.getAllCircles().first()[0]
        val secondContactId = contactDao.insertContact(ContactEntity(name = "李四"))
        dao.updateCircleWithMembers(circle.copy(name = "好友圈"), listOf(contactId, secondContactId))
        val result = dao.getAllCirclesWithMembers().first()
        assertThat(result[0].circle.name).isEqualTo("好友圈")
        assertThat(result[0].members).hasSize(2)
    }
}
