package com.tang.prm.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.DivinationRecordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DivinationRecordDaoTest {
    private lateinit var database: TangDatabase
    private lateinit var dao: DivinationRecordDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TangDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.divinationRecordDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAll() = runBlocking {
        val record = DivinationRecordEntity(
            method = "liuyao",
            question = "问题",
            resultJson = "{}",
            createdAt = 1000L
        )
        dao.insert(record)
        val result = dao.getAll().first()
        assertThat(result).hasSize(1)
        assertThat(result[0].method).isEqualTo("liuyao")
        assertThat(result[0].question).isEqualTo("问题")
    }

    @Test
    fun deleteRecord() = runBlocking {
        val id = dao.insert(DivinationRecordEntity(
            method = "liuyao",
            question = "问题",
            resultJson = "{}",
            createdAt = 1000L
        ))
        dao.deleteById(id)
        val result = dao.getAll().first()
        assertThat(result).isEmpty()
    }
}
