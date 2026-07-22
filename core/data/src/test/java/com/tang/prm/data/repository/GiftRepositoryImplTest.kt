package com.tang.prm.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.dao.GiftDao
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.GiftEntity
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Gift
import com.tang.prm.domain.model.GiftType
import com.tang.prm.domain.model.SourceTypes
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
class GiftRepositoryImplTest {

    @MockK
    private lateinit var giftDao: GiftDao

    @MockK
    private lateinit var favoriteRepository: FavoriteRepository

    @MockK
    private lateinit var database: TangDatabase

    @MockK
    private lateinit var context: Context

    private lateinit var repository: GiftRepositoryImpl

    private val entity = GiftEntity(
        id = 1, contactId = 10, giftName = "Book", giftType = "BOOKS",
        date = 1000L, isSent = true,
        occasion = null, description = null, location = null
    )
    private val domain = Gift(
        id = 1, contactId = 10, giftName = "Book", giftType = GiftType.BOOKS,
        date = 1000L, isSent = true,
        occasion = null, description = null, location = null
    )

    @BeforeEach
    fun setUp() {
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { any<androidx.room.RoomDatabase>().withTransaction(any<suspend () -> Any>()) } coAnswers {
            secondArg<suspend () -> Any>().invoke()
        }
        coEvery { favoriteRepository.deleteFavoriteBySource(any(), any()) } returns Unit
        every { context.filesDir } returns java.io.File.createTempFile("test", "tmp").parentFile
        mockkStatic("com.tang.prm.data.mapper.GiftMapperKt")
        repository = GiftRepositoryImpl(giftDao, favoriteRepository, database, context)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
        unmockkStatic("com.tang.prm.data.mapper.GiftMapperKt")
    }

    @Test
    fun getAllGifts_returnsMappedList() = runTest {
        every { giftDao.getAllGifts() } returns flowOf(listOf(entity))
        every { entity.toDomain() } returns domain

        val result = repository.getAllGifts().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].giftName).isEqualTo("Book")
    }

    @Test
    fun getGiftsByContactId_returnsMappedList() = runTest {
        every { giftDao.getGiftsByContactId(10L) } returns flowOf(listOf(entity))
        every { entity.toDomain() } returns domain

        val result = repository.getGiftsByContactId(10L).first()

        assertThat(result).hasSize(1)
        assertThat(result[0].contactId).isEqualTo(10L)
    }

    @Test
    fun insertGift_callsDaoWithEntity() = runTest {
        coEvery { giftDao.insertGift(any()) } returns 1L
        every { domain.toEntity() } returns entity

        val result = repository.insertGift(domain)

        assertThat(result).isEqualTo(1L)
        coVerify { giftDao.insertGift(entity) }
    }

    @Test
    fun deleteGiftById_callsDao() = runTest {
        coEvery { giftDao.getGiftByIdOnce(1L) } returns null
        coEvery { giftDao.deleteGiftById(1L) } returns Unit

        repository.deleteGiftById(1L)

        coVerify { giftDao.deleteGiftById(1L) }
    }

    @Test
    fun deleteGiftById_deletesPhotoFiles() = runTest {
        val entityWithPhotos = entity.copy(photos = listOf("/data/gift/photo1.jpg"))
        coEvery { giftDao.getGiftByIdOnce(1L) } returns entityWithPhotos
        coEvery { giftDao.deleteGiftById(1L) } returns Unit

        repository.deleteGiftById(1L)

        coVerify { giftDao.getGiftByIdOnce(1L) }
        coVerify { giftDao.deleteGiftById(1L) }
    }

    @Test
    fun deleteGiftsByContactId_deletesPhotoFilesAndCallsDao() = runTest {
        val entityWithPhotos = entity.copy(photos = listOf("/data/gift/photo1.jpg"))
        coEvery { giftDao.getGiftsByContactIdOnce(10L) } returns listOf(entityWithPhotos)
        coEvery { giftDao.deleteGiftsByContactId(10L) } returns Unit

        repository.deleteGiftsByContactId(10L)

        coVerify { giftDao.getGiftsByContactIdOnce(10L) }
        coVerify { giftDao.deleteGiftsByContactId(10L) }
    }

    @Test
    fun deleteGiftById_clearsFavoritesInTransaction() = runTest {
        coEvery { giftDao.getGiftByIdOnce(1L) } returns entity
        coEvery { giftDao.deleteGiftById(1L) } returns Unit

        repository.deleteGiftById(1L)

        coVerify { favoriteRepository.deleteFavoriteBySource(SourceTypes.GIFT, 1L) }
        coVerify { giftDao.deleteGiftById(1L) }
    }

    @Test
    fun deleteGiftsByContactId_clearsFavoritesForEachGift() = runTest {
        val entity1 = entity.copy(id = 1, photos = listOf("/p1.jpg"))
        val entity2 = entity.copy(id = 2, photos = listOf("/p2.jpg"))
        coEvery { giftDao.getGiftsByContactIdOnce(10L) } returns listOf(entity1, entity2)
        coEvery { giftDao.deleteGiftsByContactId(10L) } returns Unit

        repository.deleteGiftsByContactId(10L)

        coVerify { favoriteRepository.deleteFavoriteBySource(SourceTypes.GIFT, 1L) }
        coVerify { favoriteRepository.deleteFavoriteBySource(SourceTypes.GIFT, 2L) }
    }

    @Test
    fun updateGift_wrapsReadAndUpdateInTransaction() = runTest {
        coEvery { giftDao.getGiftByIdOnce(1L) } returns entity
        coEvery { giftDao.updateGift(any()) } returns Unit
        every { domain.toEntity() } returns entity

        repository.updateGift(domain)

        coVerify { giftDao.getGiftByIdOnce(1L) }
        coVerify { giftDao.updateGift(entity) }
    }
}
