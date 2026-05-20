package com.tang.prm.data.repository

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.dao.DivinationRecordDao
import com.tang.prm.data.local.entity.DivinationRecordEntity
import com.tang.prm.domain.divination.model.DivinationRecord
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
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
        repository = DivinationRepositoryImpl(dao)
    }

    @Test
    fun getAllRecords_returnsMappedList() = runTest {
        every { dao.getAll() } returns flowOf(listOf(entity))

        val result = repository.getAllRecords().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].method).isEqualTo("tarot")
    }

    @Test
    fun saveRecord_callsDaoWithEntity() = runTest {
        coEvery { dao.insert(any()) } returns 1L

        val result = repository.saveRecord(domain)

        assertThat(result).isEqualTo(1L)
        coVerify { dao.insert(match { it.method == "tarot" }) }
    }

    @Test
    fun deleteRecord_callsDaoWithEntity() = runTest {
        coEvery { dao.delete(any()) } returns Unit

        repository.deleteRecord(domain)

        coVerify { dao.delete(match { it.method == "tarot" }) }
    }
}
