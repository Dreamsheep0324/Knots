package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
    fun `delete relationship type clears from all contacts`() = runTest {
        val type = CustomType(id = 1, name = "朋友", category = CustomCategories.RELATIONSHIP, color = "#FF0000")
        val contact = Contact(id = 10, name = "张三", relationship = "朋友")
        coEvery { contactRepository.getFilteredContacts(null, null, "朋友") } returns flowOf(listOf(contact))

        useCase(type)

        coVerify { customTypeRepository.deleteTypeById(1) }
        coVerify { contactRepository.updateContact(match { it.relationship == null && it.id == 10L }) }
    }

    @Test
    fun `delete education type clears from all contacts`() = runTest {
        val type = CustomType(id = 2, name = "本科", category = CustomCategories.EDUCATION, color = "#00FF00")
        val contact = Contact(id = 20, name = "李四", education = "本科")
        coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))

        useCase(type)

        coVerify { customTypeRepository.deleteTypeById(2) }
        coVerify { contactRepository.updateContact(match { it.education == null && it.id == 20L }) }
    }

    @Test
    fun `delete hobby type delegates to cleanCustomTypeUseCase`() = runTest {
        val type = CustomType(id = 3, name = "游泳", category = CustomCategories.HOBBY, color = "#0000FF")

        useCase(type)

        coVerify { customTypeRepository.deleteTypeById(3) }
        coVerify { cleanCustomTypeUseCase.removeFromListFieldAll(CustomCategories.HOBBY, "游泳") }
    }
}
