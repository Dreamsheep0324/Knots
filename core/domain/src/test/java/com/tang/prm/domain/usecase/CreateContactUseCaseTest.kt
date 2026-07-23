package com.tang.prm.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Anniversary
import com.tang.prm.domain.model.AnniversaryType
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.repository.AnniversaryRepository
import com.tang.prm.domain.repository.ContactRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CreateContactUseCaseTest {

    private lateinit var contactRepository: ContactRepository
    private lateinit var anniversaryRepository: AnniversaryRepository
    private lateinit var useCase: CreateContactUseCase

    private val birthday = 946684800000L // 2000-01-01

    @BeforeEach
    fun setUp() {
        contactRepository = mockk()
        anniversaryRepository = mockk()
        useCase = CreateContactUseCase(contactRepository, anniversaryRepository)
    }

    @Test
    fun `createContact with birthday creates anniversary`() = runTest {
        coEvery { contactRepository.insertContactWithAnniversaries(any(), any()) } returns 1L

        val contact = Contact(name = "张三", birthday = birthday)
        val id = useCase.createContact(contact)

        assertThat(id).isEqualTo(1L)
        coVerify {
            contactRepository.insertContactWithAnniversaries(
                match { it.name == "张三" },
                match { anniversaries ->
                    anniversaries.size == 1 &&
                    anniversaries[0].type == AnniversaryType.BIRTHDAY &&
                    anniversaries[0].date == birthday &&
                    anniversaries[0].isRepeat &&
                    anniversaries[0].contactName == "张三"
                }
            )
        }
    }

    @Test
    fun `createContact without birthday creates no anniversaries`() = runTest {
        coEvery { contactRepository.insertContactWithAnniversaries(any(), any()) } returns 1L

        val contact = Contact(name = "张三", birthday = null)
        useCase.createContact(contact)

        coVerify {
            contactRepository.insertContactWithAnniversaries(
                any(),
                match { it.isEmpty() }
            )
        }
    }

    @Test
    fun `updateContact with birthday syncs birthday anniversary`() = runTest {
        val existingAnniversary = Anniversary(
            id = 100, contactId = 1, name = "生日",
            type = AnniversaryType.BIRTHDAY, date = 0L, isRepeat = true,
            contactName = "旧名", contactAvatar = null
        )
        coEvery { contactRepository.updateContact(any()) } returns Unit
        every { anniversaryRepository.getAnniversariesByContact(1L) } returns flowOf(listOf(existingAnniversary))
        coEvery { anniversaryRepository.updateAnniversary(any()) } returns Unit

        val contact = Contact(id = 1, name = "新名", birthday = birthday)
        useCase.updateContact(contact)

        coVerify { contactRepository.updateContact(match { it.id == 1L }) }
        coVerify {
            anniversaryRepository.updateAnniversary(match {
                it.id == 100L && it.date == birthday && it.contactName == "新名"
            })
        }
    }

    @Test
    fun `updateContact clearing birthday deletes birthday anniversaries`() = runTest {
        val existingAnniversary = Anniversary(
            id = 100, contactId = 1, name = "生日",
            type = AnniversaryType.BIRTHDAY, date = birthday, isRepeat = true
        )
        coEvery { contactRepository.updateContact(any()) } returns Unit
        every { anniversaryRepository.getAnniversariesByContact(1L) } returns flowOf(listOf(existingAnniversary))
        coEvery { anniversaryRepository.deleteAnniversary(any()) } returns Unit

        val contact = Contact(id = 1, name = "张三", birthday = null)
        useCase.updateContact(contact)

        coVerify { anniversaryRepository.deleteAnniversary(100L) }
    }

    @Test
    fun `updateContact with birthday but no existing anniversary creates new birthday anniversary`() = runTest {
        // L-2 修复：原测试反向锁定 H-1 bug（断言既不 update 也不 insert），
        // 现改为断言正确行为——首次设置生日应创建新的 BIRTHDAY 纪念日。
        coEvery { contactRepository.updateContact(any()) } returns Unit
        every { anniversaryRepository.getAnniversariesByContact(1L) } returns flowOf(emptyList())
        coEvery { anniversaryRepository.insertAnniversary(any()) } returns 1L

        val contact = Contact(id = 1, name = "张三", birthday = birthday)
        useCase.updateContact(contact)

        coVerify(exactly = 0) { anniversaryRepository.updateAnniversary(any()) }
        coVerify {
            anniversaryRepository.insertAnniversary(match {
                it.type == AnniversaryType.BIRTHDAY &&
                it.date == birthday &&
                it.contactId == 1L &&
                it.contactName == "张三" &&
                it.isRepeat
            })
        }
    }
}
