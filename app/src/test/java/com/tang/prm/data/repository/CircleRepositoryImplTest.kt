package com.tang.prm.data.repository

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.dao.CircleDao
import com.tang.prm.data.local.entity.CircleEntity
import com.tang.prm.data.local.entity.CircleMemberCrossRef
import com.tang.prm.data.local.entity.CircleWithMembers
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Circle
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
class CircleRepositoryImplTest {

    @MockK
    private lateinit var circleDao: CircleDao

    private lateinit var repository: CircleRepositoryImpl

    private val entity = CircleEntity(id = 1, name = "Friends", sortOrder = 0)
    private val domain = Circle(id = 1, name = "Friends", memberIds = listOf(10L, 20L), sortOrder = 0)
    private val circleWithMembers = CircleWithMembers(
        circle = entity,
        members = listOf(
            CircleMemberCrossRef(circleId = 1, contactId = 10L),
            CircleMemberCrossRef(circleId = 1, contactId = 20L)
        )
    )

    @BeforeEach
    fun setUp() {
        mockkStatic("com.tang.prm.data.mapper.CircleMapperKt")
        repository = CircleRepositoryImpl(circleDao)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("com.tang.prm.data.mapper.CircleMapperKt")
    }

    @Test
    fun getAllCircles_returnsMappedListWithMemberIds() = runTest {
        every { circleDao.getAllCirclesWithMembers() } returns flowOf(listOf(circleWithMembers))
        every { circleWithMembers.toDomain() } returns domain

        val result = repository.getAllCircles().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Friends")
        assertThat(result[0].memberIds).containsExactly(10L, 20L).inOrder()
    }

    @Test
    fun insertCircle_callsDaoWithEntityAndMembers() = runTest {
        coEvery { circleDao.insertCircleWithMembers(any(), any()) } returns 1L
        every { domain.toEntity() } returns entity

        val result = repository.insertCircle(domain)

        assertThat(result).isEqualTo(1L)
        coVerify { circleDao.insertCircleWithMembers(entity, listOf(10L, 20L)) }
    }

    @Test
    fun deleteCircle_callsDao() = runTest {
        coEvery { circleDao.deleteCircleById(1L) } returns Unit

        repository.deleteCircle(1L)

        coVerify { circleDao.deleteCircleById(1L) }
    }
}
