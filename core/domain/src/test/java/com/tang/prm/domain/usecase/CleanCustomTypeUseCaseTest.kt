package com.tang.prm.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.tang.prm.domain.model.Contact
import com.tang.prm.domain.model.CustomCategories
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

    /**
     * 辅助：捕获传入 updateContacts 的列表
     */
    private fun mockUpdateContacts(): MutableList<List<Contact>> {
        val captured = mutableListOf<List<Contact>>()
        coEvery { contactRepository.updateContacts(capture(captured)) } returns Unit
        return captured
    }

    @Nested
    @DisplayName("removeFromListFieldAll")
    inner class RemoveFromListFieldAllTest {

        @Test
        fun `removes value from hobby json array`() = runTest {
            val contact = Contact(id = 1L, name = "Alice", hobby = """["阅读","游泳","音乐"]""")
            coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))
            val captured = mockUpdateContacts()

            useCase.removeFromListFieldAll(CustomCategories.HOBBY, "游泳")

            val updated = captured.single().single()
            assertThat(updated.hobby).isNotNull()
            assertThat(updated.hobby).contains("阅读")
            assertThat(updated.hobby).doesNotContain("游泳")
        }

        @Test
        fun `removes last item returns null`() = runTest {
            val contact = Contact(id = 1L, name = "Alice", hobby = """["阅读"]""")
            coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))
            val captured = mockUpdateContacts()

            useCase.removeFromListFieldAll(CustomCategories.HOBBY, "阅读")

            assertThat(captured.single().single().hobby).isNull()
        }

        @Test
        fun `value not in array no update`() = runTest {
            val contact = Contact(id = 1L, name = "Alice", hobby = """["阅读","音乐"]""")
            coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))
            mockUpdateContacts()

            useCase.removeFromListFieldAll(CustomCategories.HOBBY, "游泳")

            // 无变更，不应调用 updateContacts
            coVerify(exactly = 0) { contactRepository.updateContacts(any()) }
        }

        @Test
        fun `null fields not updated`() = runTest {
            val contact = Contact(id = 1L, name = "Alice", hobby = null, habit = null, diet = null, skill = null)
            coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))
            mockUpdateContacts()

            useCase.removeFromListFieldAll(CustomCategories.HOBBY, "阅读")

            coVerify(exactly = 0) { contactRepository.updateContacts(any()) }
        }

        @Test
        fun `comma separated fallback removes value`() = runTest {
            // Non-JSON format: comma-separated
            val contact = Contact(id = 1L, name = "Alice", hobby = "阅读,游泳,音乐")
            coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))
            val captured = mockUpdateContacts()

            useCase.removeFromListFieldAll(CustomCategories.HOBBY, "游泳")

            val updated = captured.single().single()
            assertThat(updated.hobby).isNotNull()
            assertThat(updated.hobby).doesNotContain("游泳")
        }

        @Test
        fun `multiple contacts updated in batch`() = runTest {
            // P-5 修复后：多个联系人变更通过一次 updateContacts 调用提交
            val contact1 = Contact(id = 1L, name = "Alice", hobby = """["阅读","游泳"]""")
            val contact2 = Contact(id = 2L, name = "Bob", hobby = """["游泳","音乐"]""")
            coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact1, contact2))
            val captured = mockUpdateContacts()

            useCase.removeFromListFieldAll(CustomCategories.HOBBY, "游泳")

            assertThat(captured.single()).hasSize(2)
            assertThat(captured.single().map { it.id }).containsExactly(1L, 2L)
        }

        @Test
        fun `does not pollute other fields when removing hobby`() = runTest {
            // T-9 回归测试：删除 HOBBY 类别的"音乐"，不应影响 skill 字段
            val contact = Contact(
                id = 1L,
                name = "Alice",
                hobby = """["阅读","音乐"]""",
                skill = """["音乐","编程"]"""
            )
            coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))
            val captured = mockUpdateContacts()

            useCase.removeFromListFieldAll(CustomCategories.HOBBY, "音乐")

            val updated = captured.single().single()
            assertThat(updated.hobby).contains("阅读")
            assertThat(updated.hobby).doesNotContain("音乐")
            // skill 字段不应被污染
            assertThat(updated.skill).contains("音乐")
            assertThat(updated.skill).contains("编程")
        }

        @Test
        fun `does not pollute other fields when removing skill`() = runTest {
            // T-9 回归测试：删除 SKILL 类别的"音乐"，不应影响 hobby 字段
            val contact = Contact(
                id = 1L,
                name = "Alice",
                hobby = """["阅读","音乐"]""",
                skill = """["音乐","编程"]"""
            )
            coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))
            val captured = mockUpdateContacts()

            useCase.removeFromListFieldAll(CustomCategories.SKILL, "音乐")

            val updated = captured.single().single()
            assertThat(updated.skill).contains("编程")
            assertThat(updated.skill).doesNotContain("音乐")
            // hobby 字段不应被污染
            assertThat(updated.hobby).contains("音乐")
            assertThat(updated.hobby).contains("阅读")
        }

        @Test
        fun `does not pollute other fields when removing diet`() = runTest {
            // T-9 回归测试：删除 DIET 类别的"咖啡"，不应影响 hobby/habit/skill
            val contact = Contact(
                id = 1L,
                name = "Alice",
                hobby = """["咖啡品鉴"]""",
                habit = """["喝咖啡"]""",
                diet = """["咖啡","辣"]""",
                skill = """["拉花"]"""
            )
            coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))
            val captured = mockUpdateContacts()

            useCase.removeFromListFieldAll(CustomCategories.DIET, "咖啡")

            val updated = captured.single().single()
            assertThat(updated.diet).contains("辣")
            assertThat(updated.diet).doesNotContain("咖啡")
            assertThat(updated.hobby).isEqualTo("""["咖啡品鉴"]""")
            assertThat(updated.habit).isEqualTo("""["喝咖啡"]""")
            assertThat(updated.skill).isEqualTo("""["拉花"]""")
        }

        @Test
        fun `does not pollute other fields when removing habit`() = runTest {
            // T-9 回归测试：删除 HABIT 类别的"游泳"，不应影响 hobby/diet/skill
            val contact = Contact(
                id = 1L,
                name = "Alice",
                hobby = """["游泳"]""",
                habit = """["游泳","早起"]""",
                diet = """["清淡"]""",
                skill = """["游泳救生"]"""
            )
            coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))
            val captured = mockUpdateContacts()

            useCase.removeFromListFieldAll(CustomCategories.HABIT, "游泳")

            val updated = captured.single().single()
            assertThat(updated.habit).contains("早起")
            assertThat(updated.habit).doesNotContain("游泳")
            assertThat(updated.hobby).isEqualTo("""["游泳"]""")
            assertThat(updated.diet).isEqualTo("""["清淡"]""")
            assertThat(updated.skill).isEqualTo("""["游泳救生"]""")
        }

        @Test
        fun `unknown field no update`() = runTest {
            // 传入未知 field 应直接返回，不更新任何联系人
            val contact = Contact(id = 1L, name = "Alice", hobby = """["阅读"]""")
            coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact))
            mockUpdateContacts()

            useCase.removeFromListFieldAll("UNKNOWN_CATEGORY", "阅读")

            coVerify(exactly = 0) { contactRepository.updateContacts(any()) }
        }
    }
}
