package com.tang.prm.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.CircleEntity
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

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TangDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.circleDao()
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
    fun deleteCircle() = runBlocking {
        val id = dao.insertCircle(CircleEntity(name = "朋友圈"))
        dao.deleteCircleById(id)
        val result = dao.getAllCircles().first()
        assertThat(result).isEmpty()
    }
}
