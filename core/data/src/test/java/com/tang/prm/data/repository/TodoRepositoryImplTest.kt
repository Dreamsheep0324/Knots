package com.tang.prm.data.repository

import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.dao.TodoDao
import com.tang.prm.data.local.entity.TodoItemEntity
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.TodoItem
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
class TodoRepositoryImplTest {

    @MockK
    private lateinit var todoDao: TodoDao

    private lateinit var repository: TodoRepositoryImpl

    private val entity = TodoItemEntity(id = 1, title = "Buy milk", isCompleted = false, priority = 1)
    private val domain = TodoItem(id = 1, title = "Buy milk", isCompleted = false, priority = 1)

    @BeforeEach
    fun setUp() {
        mockkStatic("com.tang.prm.data.mapper.TodoMapperKt")
        repository = TodoRepositoryImpl(todoDao)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic("com.tang.prm.data.mapper.TodoMapperKt")
    }

    @Test
    fun getActiveTodos_returnsMappedList() = runTest {
        every { todoDao.getActiveTodos() } returns flowOf(listOf(entity))
        every { entity.toDomain() } returns domain

        val result = repository.getActiveTodos().first()

        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("Buy milk")
    }

    @Test
    fun updateTodoCompletion_callsDao() = runTest {
        coEvery { todoDao.updateTodoCompletion(1L, true) } returns Unit

        repository.updateTodoCompletion(1L, true)

        coVerify { todoDao.updateTodoCompletion(1L, true) }
    }

    @Test
    fun deleteTodo_callsDao() = runTest {
        coEvery { todoDao.deleteTodoById(1L) } returns Unit

        repository.deleteTodo(1L)

        coVerify { todoDao.deleteTodoById(1L) }
    }
}
