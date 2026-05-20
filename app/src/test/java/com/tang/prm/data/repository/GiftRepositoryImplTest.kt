package com.tang.prm.data.repository

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.dao.GiftDao
import com.tang.prm.data.local.entity.GiftEntity
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Gift
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

    private lateinit var repository: GiftRepositoryImpl

    private val entity = GiftEntity(
        id = 1, contactId = 10, giftName = "Book", giftType = "BOOKS",
        date = 1000L, isSent = true, amount = null,
        occasion = null, description = null, location = null
    )
    private val domain = Gift(
        id = 1, contactId = 10, giftName = "Book", giftType = "BOOKS",
        date = 1000L, isSent = true, amount = null,
        occasion = null, description = null, location = null
    )

    @BeforeEach
    fun setUp() {
        mockkStatic("com.tang.prm.data.mapper.GiftMapperKt")
        repository = GiftRepositoryImpl(giftDao)
    }

    @AfterEach
    fun tearDown() {
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
        coEvery { giftDao.deleteGiftById(1L) } returns Unit

        repository.deleteGiftById(1L)

        coVerify { giftDao.deleteGiftById(1L) }
    }
}
