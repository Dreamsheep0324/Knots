package com.tang.prm.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.CustomTypeEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CustomTypeDaoTest {
    private lateinit var database: TangDatabase
    private lateinit var dao: CustomTypeDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TangDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.customTypeDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAll() = runBlocking {
        val type = CustomTypeEntity(category = "EVENT_TYPE", name = "聚会")
        dao.insertType(type)
        val result = dao.getAllTypes().first()
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("聚会")
        assertThat(result[0].category).isEqualTo("EVENT_TYPE")
    }

    @Test
    fun getByCategory() = runBlocking {
        dao.insertType(CustomTypeEntity(category = "EVENT_TYPE", name = "聚会"))
        dao.insertType(CustomTypeEntity(category = "EMOTION", name = "开心"))
        val result = dao.getTypesByCategory("EVENT_TYPE").first()
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("聚会")
    }

    @Test
    fun deleteCustomType() = runBlocking {
        val id = dao.insertType(CustomTypeEntity(category = "EVENT_TYPE", name = "聚会", isDefault = false))
        dao.deleteTypeById(id)
        val result = dao.getAllTypes().first()
        assertThat(result).isEmpty()
    }
}
