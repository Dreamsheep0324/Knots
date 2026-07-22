package com.tang.prm.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteCustomTypeUseCaseTest {
    private lateinit var useCase: DeleteCustomTypeUseCase
    private val customTypeRepository: CustomTypeRepository = mockk(relaxed = true)
    private val contactRepository: ContactRepository = mockk(relaxed = true)
    private val cleanCustomTypeUseCase: CleanCustomTypeUseCase = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        useCase = DeleteCustomTypeUseCase(customTypeRepository, contactRepository, cleanCustomTypeUseCase)
    }

    @Test
    fun `delete relationship type clears from all matching contacts in batch`() = runTest {
        // B-9 修复：RELATIONSHIP 统一走内存过滤 + updateContacts 批量事务
        val type = CustomType(id = 1, name = "朋友", category = CustomCategories.RELATIONSHIP, color = "#FF0000")
        val contact1 = Contact(id = 10, name = "张三", relationship = "朋友")
        val contact2 = Contact(id = 11, name = "李四", relationship = "朋友")
        val contact3 = Contact(id = 12, name = "王五", relationship = "同事")  // 不匹配，不应被更新
        coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact1, contact2, contact3))
        val captured = slot<List<Contact>>()
        coEvery { contactRepository.updateContacts(capture(captured)) } returns Unit

        useCase(type)

        coVerify { customTypeRepository.deleteTypeById(1) }
        assertThat(captured.captured).hasSize(2)
        assertThat(captured.captured.map { it.id }).containsExactly(10L, 11L)
        assertThat(captured.captured.all { it.relationship == null }).isTrue()
    }

    @Test
    fun `delete education type clears from all matching contacts in batch`() = runTest {
        // B-9 修复：EDUCATION 与 RELATIONSHIP 走同一清理路径
        val type = CustomType(id = 2, name = "本科", category = CustomCategories.EDUCATION, color = "#00FF00")
        val contact = Contact(id = 20, name = "李四", education = "本科")
        coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))
        val captured = slot<List<Contact>>()
        coEvery { contactRepository.updateContacts(capture(captured)) } returns Unit

        useCase(type)

        coVerify { customTypeRepository.deleteTypeById(2) }
        assertThat(captured.captured).hasSize(1)
        assertThat(captured.captured.single().education).isNull()
    }

    @Test
    fun `delete hobby type delegates to cleanCustomTypeUseCase`() = runTest {
        val type = CustomType(id = 3, name = "游泳", category = CustomCategories.HOBBY, color = "#0000FF")

        useCase(type)

        coVerify { customTypeRepository.deleteTypeById(3) }
        coVerify { cleanCustomTypeUseCase.removeFromListFieldAll(CustomCategories.HOBBY, "游泳") }
    }

    @Test
    fun `no matching contacts does not call updateContacts`() = runTest {
        val type = CustomType(id = 4, name = "博士", category = CustomCategories.EDUCATION, color = "#000000")
        val contact = Contact(id = 30, name = "赵六", education = "本科")  // 不匹配
        coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))

        useCase(type)

        coVerify { customTypeRepository.deleteTypeById(4) }
        coVerify(exactly = 0) { contactRepository.updateContacts(any()) }
    }
}
