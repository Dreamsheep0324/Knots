package com.tang.prm.data.repository

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.dao.ContactTagDao
import com.tang.prm.data.local.entity.ContactTagEntity
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.ContactTag
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
class ContactTagRepositoryImplTest {

    @MockK
    private lateinit var tagDao: ContactTagDao

    private lateinit var repository: ContactTagRepositoryImpl

    private val entity = ContactTagEntity(id = 1, name = "VIP", color = "#FF0000")
    private val domain = ContactTag(id = 1, name = "VIP", color = "#FF0000")

    @BeforeEach
    fun setUp() {
        mockkStatic("com.tang.prm.data.mapper.ContactMapperKt")
        repository = ContactTagRepositoryImpl(tagDao)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("com.tang.prm.data.mapper.ContactMapperKt")
    }

    @Test
    fun getAllTags_returnsMappedList() = runTest {
        every { tagDao.getAllTags() } returns flowOf(listOf(entity))
        every { entity.toDomain() } returns domain

        val result = repository.getAllTags().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("VIP")
    }

    @Test
    fun insertTag_callsDaoWithEntity() = runTest {
        coEvery { tagDao.insertTag(any()) } returns 1L
        every { domain.toEntity() } returns entity

        val result = repository.insertTag(domain)

        assertThat(result).isEqualTo(1L)
        coVerify { tagDao.insertTag(entity) }
    }

    @Test
    fun deleteTagById_callsDao() = runTest {
        coEvery { tagDao.deleteTagById(1L) } returns Unit

        repository.deleteTagById(1L)

        coVerify { tagDao.deleteTagById(1L) }
    }
}
