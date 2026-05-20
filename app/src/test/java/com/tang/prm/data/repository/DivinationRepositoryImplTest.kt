package com.tang.prm.data.repository

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.dao.DivinationRecordDao
import com.tang.prm.data.local.entity.DivinationRecordEntity
import com.tang.prm.data.mapper.DivinationMapper
import com.tang.prm.domain.divination.model.DivinationRecord
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class DivinationRepositoryImplTest {

    @MockK
    private lateinit var dao: DivinationRecordDao

    private lateinit var repository: DivinationRepositoryImpl

    private val entity = DivinationRecordEntity(
        id = 1, method = "tarot", question = "Will I?", resultJson = "{}",
        createdAt = 1000L, aiAnalysis = "Yes"
    )
    private val domain = DivinationRecord(
        id = 1, method = "tarot", question = "Will I?", resultJson = "{}",
        createdAt = 1000L, aiAnalysis = "Yes"
    )

    @BeforeEach
    fun setUp() {
        mockkObject(DivinationMapper)
        repository = DivinationRepositoryImpl(dao)
    }

    @AfterEach
    fun tearDown() {
        unmockkObject(DivinationMapper)
    }

    @Test
    fun getAllRecords_returnsMappedList() = runTest {
        every { dao.getAll() } returns flowOf(listOf(entity))
        every { DivinationMapper.toDomain(entity) } returns domain

        val result = repository.getAllRecords().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].method).isEqualTo("tarot")
    }

    @Test
    fun saveRecord_callsDaoWithEntity() = runTest {
        coEvery { dao.insert(any()) } returns 1L
        every { DivinationMapper.toEntity(domain) } returns entity

        val result = repository.saveRecord(domain)

        assertThat(result).isEqualTo(1L)
        coVerify { dao.insert(entity) }
    }

    @Test
    fun deleteRecord_callsDaoWithEntity() = runTest {
        coEvery { dao.delete(any()) } returns Unit
        every { DivinationMapper.toEntity(domain) } returns entity

        repository.deleteRecord(domain)

        coVerify { dao.delete(entity) }
    }
}
