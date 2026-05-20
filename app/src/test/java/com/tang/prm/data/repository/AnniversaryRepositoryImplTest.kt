package com.tang.prm.data.repository

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.dao.AnniversaryDao
import com.tang.prm.data.local.entity.AnniversaryEntity
import com.tang.prm.data.local.entity.AnniversaryWithContact
import com.tang.prm.data.local.entity.ContactEntity
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
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
class AnniversaryRepositoryImplTest {

    @MockK
    private lateinit var anniversaryDao: AnniversaryDao

    private lateinit var repository: AnniversaryRepositoryImpl

    private val anniversaryEntity = AnniversaryEntity(id = 1, contactId = 10, name = "Birthday", type = "BIRTHDAY", date = 1000L)
    private val contactEntity = ContactEntity(id = 10, name = "Alice")
    private val withContact = AnniversaryWithContact(anniversary = anniversaryEntity, contact = contactEntity)
    private val domain = Anniversary(id = 1, contactId = 10, name = "Birthday", type = AnniversaryType.BIRTHDAY, date = 1000L)

    @BeforeEach
    fun setUp() {
        mockkStatic("com.tang.prm.data.mapper.AnniversaryMapperKt")
        repository = AnniversaryRepositoryImpl(anniversaryDao)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("com.tang.prm.data.mapper.AnniversaryMapperKt")
    }

    @Test
    fun getAllAnniversaries_returnsMappedList() = runTest {
        every { anniversaryDao.getAllAnniversariesWithContact() } returns flowOf(listOf(withContact))
        every { any<AnniversaryEntity>().toDomain(any(), any()) } returns domain

        val result = repository.getAllAnniversaries().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Birthday")
    }

    @Test
    fun getByContact_returnsMappedList() = runTest {
        every { anniversaryDao.getAnniversariesByContactWithContact(10L) } returns flowOf(listOf(withContact))
        every { any<AnniversaryEntity>().toDomain(any(), any()) } returns domain

        val result = repository.getAnniversariesByContact(10L).first()

        assertThat(result).hasSize(1)
        assertThat(result[0].contactId).isEqualTo(10L)
    }

    @Test
    fun insertAnniversary_callsDaoWithEntity() = runTest {
        coEvery { anniversaryDao.insertAnniversary(any()) } returns 1L
        every { any<Anniversary>().toEntity() } returns anniversaryEntity

        val result = repository.insertAnniversary(domain)

        assertThat(result).isEqualTo(1L)
        coVerify { anniversaryDao.insertAnniversary(anniversaryEntity) }
    }

    @Test
    fun deleteAnniversary_callsDao() = runTest {
        coEvery { anniversaryDao.deleteAnniversaryById(1L) } returns Unit

        repository.deleteAnniversary(1L)

        coVerify { anniversaryDao.deleteAnniversaryById(1L) }
    }
}
