package com.tang.prm.data.repository

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.dao.ContactGroupDao
import com.tang.prm.data.local.entity.ContactGroupEntity
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.ContactGroup
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
class ContactGroupRepositoryImplTest {

    @MockK
    private lateinit var groupDao: ContactGroupDao

    private lateinit var repository: ContactGroupRepositoryImpl

    private val entity = ContactGroupEntity(id = 1, name = "Work", color = "#0000FF", sortOrder = 0)
    private val domain = ContactGroup(id = 1, name = "Work", color = "#0000FF", sortOrder = 0)

    @BeforeEach
    fun setUp() {
        mockkStatic("com.tang.prm.data.mapper.ContactMapperKt")
        repository = ContactGroupRepositoryImpl(groupDao)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("com.tang.prm.data.mapper.ContactMapperKt")
    }

    @Test
    fun getAllGroups_returnsMappedList() = runTest {
        every { groupDao.getAllGroups() } returns flowOf(listOf(entity))
        every { entity.toDomain() } returns domain

        val result = repository.getAllGroups().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Work")
    }

    @Test
    fun insertGroup_callsDaoWithEntity() = runTest {
        coEvery { groupDao.insertGroup(any()) } returns 1L
        every { domain.toEntity() } returns entity

        val result = repository.insertGroup(domain)

        assertThat(result).isEqualTo(1L)
        coVerify { groupDao.insertGroup(entity) }
    }

    @Test
    fun deleteGroupById_callsDao() = runTest {
        coEvery { groupDao.deleteGroupById(1L) } returns Unit

        repository.deleteGroupById(1L)

        coVerify { groupDao.deleteGroupById(1L) }
    }
}
