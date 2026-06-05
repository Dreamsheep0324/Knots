package com.tang.prm.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {
    private lateinit var database: TangDatabase
    private lateinit var dao: FavoriteDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TangDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.favoriteDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAll() = runBlocking {
        val favorite = FavoriteEntity(sourceType = "EVENT", sourceId = 1L, title = "收藏")
        dao.insertFavorite(favorite)
        val result = dao.getAllFavorites().first()
        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("收藏")
        assertThat(result[0].sourceType).isEqualTo("EVENT")
    }

    @Test
    fun deleteFavorite() = runBlocking {
        dao.insertFavorite(FavoriteEntity(sourceType = "EVENT", sourceId = 1L, title = "收藏"))
        dao.deleteFavoriteBySource("EVENT", 1L)
        val result = dao.getAllFavorites().first()
        assertThat(result).isEmpty()
    }
}
