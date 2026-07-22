package com.tang.prm.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.dao.*
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.ContactAttributeEntity
import com.tang.prm.data.local.entity.ContactEntity
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
    private lateinit var contactAttributeDao: ContactAttributeDao

    @MockK
    private lateinit var anniversaryDao: AnniversaryDao

    @MockK
    private lateinit var database: TangDatabase

    @MockK
    private lateinit var context: Context

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
        every { context.filesDir } returns java.io.File.createTempFile("test", "tmp").parentFile
        repository = ContactRepositoryImpl(
            contactDao, contactAttributeDao, anniversaryDao, database, context
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
        every { contactAttributeDao.getAttributesForAllContacts() } returns flowOf<List<ContactAttributeEntity>>(emptyList())

        val result = repository.getAllContacts().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("Alice")
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
        // REP-A-1 修复后：deleteContact 不再依赖 giftDao/todoDao/reminderDao
        coEvery { contactDao.getContactByIdOnce(1L) } returns null
        coEvery { contactDao.deleteContactById(1L) } returns Unit

        repository.deleteContact(1L)

        coVerify { contactDao.deleteContactById(1L) }
    }

    @Test
    fun deleteContact_cleansUpAvatarWhenPresent() = runTest {
        val entityWithAvatar = entity.copy(avatar = "/data/app_images/avatar.jpg")
        coEvery { contactDao.getContactByIdOnce(1L) } returns entityWithAvatar
        coEvery { contactDao.deleteContactById(1L) } returns Unit

        repository.deleteContact(1L)

        coVerify { contactDao.getContactByIdOnce(1L) }
        coVerify { contactDao.deleteContactById(1L) }
    }

    @Test
    fun insertContactWithAnniversaries_insertsContactThenAnniversariesWithNewId() = runTest {
        // A-8 修复：跨聚合事务化写入
        val newId = 42L
        coEvery { contactDao.insertContact(any()) } returns newId
        coEvery { contactAttributeDao.insertAll(any()) } returns Unit
        coEvery { anniversaryDao.insertAnniversary(any()) } returns 1L

        val anniversary = com.tang.prm.domain.model.Anniversary(
            contactId = 0L, // 由 Repository 填充
            name = "生日",
            type = com.tang.prm.domain.model.AnniversaryType.BIRTHDAY,
            date = 1000L
        )
        val result = repository.insertContactWithAnniversaries(domain, listOf(anniversary))

        assertThat(result).isEqualTo(newId)
        // 验证 anniversary 的 contactId 被自动填充为新联系人 ID
        coVerify {
            anniversaryDao.insertAnniversary(match { it.contactId == newId })
        }
    }

    @Test
    fun insertContactWithAnniversaries_emptyAnniversaries_onlyInsertsContact() = runTest {
        coEvery { contactDao.insertContact(any()) } returns 1L
        coEvery { contactAttributeDao.insertAll(any()) } returns Unit

        val result = repository.insertContactWithAnniversaries(domain, emptyList())

        assertThat(result).isEqualTo(1L)
        coVerify(exactly = 0) { anniversaryDao.insertAnniversary(any()) }
    }

    @Test
    fun updateContacts_emptyList_noOp() = runTest {
        // P-5 修复：空列表直接返回，不开启事务
        repository.updateContacts(emptyList())

        coVerify(exactly = 0) { contactDao.updateContact(any()) }
    }

    @Test
    fun updateContacts_multipleContacts_allUpdatedInTransaction() = runTest {
        // P-5 修复：批量事务化更新
        val contacts = listOf(
            domain.copy(id = 1L, hobby = """["阅读"]"""),
            domain.copy(id = 2L, hobby = null)
        )
        coEvery { contactDao.updateContact(any()) } returns Unit
        coEvery { contactAttributeDao.deleteAllForContact(any()) } returns Unit
        coEvery { contactAttributeDao.insertAll(any()) } returns Unit

        repository.updateContacts(contacts)

        coVerify(exactly = 2) { contactDao.updateContact(any()) }
        coVerify(exactly = 2) { contactAttributeDao.deleteAllForContact(any()) }
    }
}
