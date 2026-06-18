package com.tang.prm.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.repository.ContactRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CleanCustomTypeUseCaseTest {

    @MockK
    private lateinit var contactRepository: ContactRepository

    private lateinit var useCase: CleanCustomTypeUseCase

    @BeforeEach
    fun setUp() {
        useCase = CleanCustomTypeUseCase(contactRepository)
    }

    @Nested
    @DisplayName("removeFromListFieldAll")
    inner class RemoveFromListFieldAllTest {

        @Test
        fun removesValueFromHobbyJsonArray() = runTest {
            val contact = Contact(id = 1L, name = "Alice", hobby = """["阅读","游泳","音乐"]""")
            coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))
            coEvery { contactRepository.updateContact(any()) } returns Unit

            useCase.removeFromListFieldAll("hobby", "游泳")

            coVerify {
                contactRepository.updateContact(match { c ->
                    c.hobby != null && !c.hobby!!.contains("游泳") && c.hobby!!.contains("阅读")
                })
            }
        }

        @Test
        fun removesLastItem_returnsNull() = runTest {
            val contact = Contact(id = 1L, name = "Alice", hobby = """["阅读"]""")
            coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))
            coEvery { contactRepository.updateContact(any()) } returns Unit

            useCase.removeFromListFieldAll("hobby", "阅读")

            coVerify {
                contactRepository.updateContact(match { c -> c.hobby == null })
            }
        }

        @Test
        fun valueNotInArray_noUpdate() = runTest {
            val contact = Contact(id = 1L, name = "Alice", hobby = """["阅读","音乐"]""")
            coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))
            coEvery { contactRepository.updateContact(any()) } returns Unit

            useCase.removeFromListFieldAll("hobby", "游泳")

            // Should not call updateContact since nothing changed
            coVerify(exactly = 0) { contactRepository.updateContact(any()) }
        }

        @Test
        fun nullFields_notUpdated() = runTest {
            val contact = Contact(id = 1L, name = "Alice", hobby = null, habit = null, diet = null, skill = null)
            coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))
            coEvery { contactRepository.updateContact(any()) } returns Unit

            useCase.removeFromListFieldAll("hobby", "阅读")

            coVerify(exactly = 0) { contactRepository.updateContact(any()) }
        }

        @Test
        fun commaSeparatedFallback_removesValue() = runTest {
            // Non-JSON format: comma-separated
            val contact = Contact(id = 1L, name = "Alice", hobby = "阅读,游泳,音乐")
            coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))
            coEvery { contactRepository.updateContact(any()) } returns Unit

            useCase.removeFromListFieldAll("hobby", "游泳")

            coVerify {
                contactRepository.updateContact(match { c ->
                    c.hobby != null && !c.hobby!!.contains("游泳")
                })
            }
        }

        @Test
        fun multipleContacts_updatedIndependently() = runTest {
            val contact1 = Contact(id = 1L, name = "Alice", hobby = """["阅读","游泳"]""")
            val contact2 = Contact(id = 2L, name = "Bob", hobby = """["游泳","音乐"]""")
            coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact1, contact2))
            coEvery { contactRepository.updateContact(any()) } returns Unit

            useCase.removeFromListFieldAll("hobby", "游泳")

            coVerify(exactly = 2) { contactRepository.updateContact(any()) }
        }
    }
}
