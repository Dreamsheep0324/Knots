package com.tang.prm.data.repository

import androidx.room.withTransaction
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.dao.*
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.ContactEntity
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Contact
import com.tang.prm.util.escapeSqlWildcards
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
class ContactRepositoryImplTest {

    @MockK
    private lateinit var contactDao: ContactDao

    @MockK
    private lateinit var anniversaryDao: AnniversaryDao

    @MockK
    private lateinit var giftDao: GiftDao

    @MockK
    private lateinit var thoughtDao: ThoughtDao

    @MockK
    private lateinit var circleDao: CircleDao

    @MockK
    private lateinit var todoDao: TodoDao

    @MockK
    private lateinit var reminderDao: ReminderDao

    @MockK
    private lateinit var database: TangDatabase

    private lateinit var repository: ContactRepositoryImpl

    private val entity = ContactEntity(id = 1, name = "Alice", phone = "123", createdAt = 0, updatedAt = 0)
    private val domain = Contact(id = 1, name = "Alice", phone = "123", createdAt = 0, updatedAt = 0)

    @BeforeEach
    fun setUp() {
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { any<androidx.room.RoomDatabase>().withTransaction(any<suspend () -> Any>()) } coAnswers {
            secondArg<suspend () -> Any>().invoke()
        }
        mockkStatic("com.tang.prm.util.SqlUtilsKt")
        every { any<String>().escapeSqlWildcards() } answers { firstArg() }
        repository = ContactRepositoryImpl(
            contactDao, anniversaryDao, giftDao, thoughtDao,
            circleDao, todoDao, reminderDao, database
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
        unmockkStatic("com.tang.prm.util.SqlUtilsKt")
    }

    @Test
    fun getAllContacts_returnsMappedList() = runTest {
        every { contactDao.getAllContacts() } returns flowOf(listOf(entity))

        val result = repository.getAllContacts().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Alice")
    }

    @Test
    fun searchContacts_callsDaoWithKeyword() = runTest {
        every { contactDao.searchContacts("test") } returns flowOf(listOf(entity))

        val result = repository.searchContacts("test").first()

        assertThat(result).hasSize(1)
        coVerify { contactDao.searchContacts("test") }
    }

    @Test
    fun insertContact_callsDaoWithEntity() = runTest {
        coEvery { contactDao.insertContact(any()) } returns 1L

        val result = repository.insertContact(domain)

        assertThat(result).isEqualTo(1L)
        coVerify { contactDao.insertContact(entity) }
    }

    @Test
    fun deleteContact_callsDaoDeleteById() = runTest {
        coEvery { anniversaryDao.deleteAnniversariesByContact(1L) } returns Unit
        coEvery { giftDao.deleteGiftsByContactId(1L) } returns Unit
        coEvery { thoughtDao.deleteThoughtsByContact(1L) } returns Unit
        coEvery { todoDao.deleteTodosByContact(1L) } returns Unit
        coEvery { reminderDao.deleteRemindersByContact(1L) } returns Unit
        coEvery { contactDao.deleteCrossRefsByContact(1L) } returns Unit
        coEvery { circleDao.deleteMemberRefsByContact(1L) } returns Unit
        coEvery { contactDao.deleteContactById(1L) } returns Unit

        repository.deleteContact(1L)

        coVerify { contactDao.deleteContactById(1L) }
    }
}
