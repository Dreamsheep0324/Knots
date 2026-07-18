package com.tang.prm.data.repository

import androidx.room.withTransaction
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.dao.ThoughtDao
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.ThoughtEntity
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.SourceTypes
import com.tang.prm.domain.model.Thought
import com.tang.prm.domain.model.ThoughtType
import com.tang.prm.domain.repository.FavoriteRepository
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
class ThoughtRepositoryImplTest {

    @MockK
    private lateinit var thoughtDao: ThoughtDao

    @MockK
    private lateinit var favoriteRepository: FavoriteRepository

    @MockK
    private lateinit var database: TangDatabase

    private lateinit var repository: ThoughtRepositoryImpl

    private val entity = ThoughtEntity(
        id = 1, contactId = 10, content = "Hello", type = "murmur",
        createdAt = 1000L, updatedAt = 1000L
    )
    private val domain = Thought(
        id = 1, contactId = 10, content = "Hello", type = ThoughtType.MURMUR,
        createdAt = 1000L, updatedAt = 1000L
    )

    @BeforeEach
    fun setUp() {
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { any<androidx.room.RoomDatabase>().withTransaction(any<suspend () -> Any>()) } coAnswers {
            secondArg<suspend () -> Any>().invoke()
        }
        coEvery { favoriteRepository.deleteFavoriteBySource(any(), any()) } returns Unit
        mockkStatic("com.tang.prm.data.mapper.ThoughtMapperKt")
        repository = ThoughtRepositoryImpl(thoughtDao, favoriteRepository, database)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
        unmockkStatic("com.tang.prm.data.mapper.ThoughtMapperKt")
    }

    @Test
    fun getAllThoughts_returnsMappedList() = runTest {
        every { thoughtDao.getAllThoughts() } returns flowOf(listOf(entity))
        every { entity.toDomain() } returns domain

        val result = repository.getAllThoughts().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].content).isEqualTo("Hello")
    }

    @Test
    fun getThoughtsByContact_returnsMappedList() = runTest {
        every { thoughtDao.getThoughtsByContact(10L) } returns flowOf(listOf(entity))
        every { entity.toDomain() } returns domain

        val result = repository.getThoughtsByContact(10L).first()

        assertThat(result).hasSize(1)
        assertThat(result[0].contactId).isEqualTo(10L)
    }

    @Test
    fun insertThought_callsDaoWithEntity() = runTest {
        coEvery { thoughtDao.insertThought(any()) } returns 1L
        every { domain.toEntity() } returns entity

        val result = repository.insertThought(domain)

        assertThat(result).isEqualTo(1L)
        coVerify { thoughtDao.insertThought(entity) }
    }

    @Test
    fun deleteThought_callsDao() = runTest {
        coEvery { thoughtDao.deleteThoughtById(1L) } returns Unit

        repository.deleteThought(1L)

        coVerify { thoughtDao.deleteThoughtById(1L) }
    }

    @Test
    fun deleteThought_clearsFavoritesInTransaction() = runTest {
        coEvery { thoughtDao.deleteThoughtById(1L) } returns Unit

        repository.deleteThought(1L)

        coVerify { favoriteRepository.deleteFavoriteBySource(SourceTypes.THOUGHT, 1L) }
        coVerify { thoughtDao.deleteThoughtById(1L) }
    }
}
