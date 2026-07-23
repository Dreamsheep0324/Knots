package com.tang.prm.data.repository

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.dao.ReminderDao
import com.tang.prm.data.local.entity.ReminderEntity
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Reminder
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
class ReminderRepositoryImplTest {

    @MockK
    private lateinit var reminderDao: ReminderDao

    private lateinit var repository: ReminderRepositoryImpl

    private val entity = ReminderEntity(
        id = 1, type = "event", title = "Remind", content = "Don't forget",
        time = 1000L, createdAt = 500L
    )
    private val domain = Reminder(
        id = 1, type = "event", title = "Remind", content = "Don't forget",
        time = 1000L, createdAt = 500L
    )

    @BeforeEach
    fun setUp() {
        mockkStatic("com.tang.prm.data.mapper.ReminderMapperKt")
        repository = ReminderRepositoryImpl(reminderDao)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("com.tang.prm.data.mapper.ReminderMapperKt")
    }

    @Test
    fun observeActiveReminders_returnsMappedList() = runTest {
        every { reminderDao.getActiveReminders() } returns flowOf(listOf(entity))
        every { entity.toDomain() } returns domain

        val result = repository.observeActiveReminders().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("Remind")
    }

    @Test
    fun getRemindersByContact_returnsMappedList() = runTest {
        every { reminderDao.getRemindersByContact(10L) } returns flowOf(listOf(entity))
        every { entity.toDomain() } returns domain

        val result = repository.getRemindersByContact(10L).first()

        assertThat(result).hasSize(1)
        assertThat(result[0].contactId).isNull()
    }

    @Test
    fun markReminderCompleted_callsDao() = runTest {
        coEvery { reminderDao.markReminderCompleted(1L) } returns Unit

        repository.markReminderCompleted(1L)

        coVerify { reminderDao.markReminderCompleted(1L) }
    }
}
