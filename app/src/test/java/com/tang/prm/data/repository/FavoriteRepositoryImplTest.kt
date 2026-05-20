package com.tang.prm.data.repository

import androidx.room.withTransaction
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.dao.FavoriteDao
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.FavoriteEntity
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Favorite
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class FavoriteRepositoryImplTest {

    @MockK
    private lateinit var favoriteDao: FavoriteDao

    @MockK
    private lateinit var database: TangDatabase

    private lateinit var repository: FavoriteRepositoryImpl

    private val entity = FavoriteEntity(id = 1, sourceType = "EVENT", sourceId = 100, title = "Fav", createdAt = 0)
    private val domain = Favorite(id = 1, sourceType = "EVENT", sourceId = 100, title = "Fav", createdAt = 0)

    @BeforeEach
    fun setUp() {
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { any<androidx.room.RoomDatabase>().withTransaction(any<suspend () -> Any>()) } coAnswers {
            secondArg<suspend () -> Any>().invoke()
        }
        repository = FavoriteRepositoryImpl(favoriteDao, database)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun getAllFavorites_returnsMappedList() = runTest {
        every { favoriteDao.getAllFavorites() } returns flowOf(listOf(entity))

        val result = repository.getAllFavorites().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("Fav")
    }

    @Test
    fun insertFavorite_callsDaoWithEntity() = runTest {
        coEvery { favoriteDao.insertFavorite(any()) } returns 1L

        val result = repository.insertFavorite(domain)

        assertThat(result).isEqualTo(1L)
        coVerify { favoriteDao.insertFavorite(entity) }
    }

    @Test
    fun deleteFavoriteBySource_callsDao() = runTest {
        coEvery { favoriteDao.deleteFavoriteBySource("EVENT", 100L) } returns Unit

        repository.deleteFavoriteBySource("EVENT", 100L)

        coVerify { favoriteDao.deleteFavoriteBySource("EVENT", 100L) }
    }

    @Test
    fun toggleFavorite_insertsWhenNotExisting() = runTest {
        coEvery { favoriteDao.getFavoriteBySource("EVENT", 100L) } returns null
        coEvery { favoriteDao.insertFavorite(any()) } returns 1L

        val result = repository.toggleFavorite("EVENT", 100L, "Title", null)

        assertThat(result).isTrue()
        coVerify { favoriteDao.insertFavorite(any()) }
    }

    @Test
    fun toggleFavorite_deletesWhenExisting() = runTest {
        coEvery { favoriteDao.getFavoriteBySource("EVENT", 100L) } returns entity
        coEvery { favoriteDao.deleteFavoriteBySource("EVENT", 100L) } returns Unit

        val result = repository.toggleFavorite("EVENT", 100L, "Title", null)

        assertThat(result).isFalse()
        coVerify { favoriteDao.deleteFavoriteBySource("EVENT", 100L) }
    }
}
