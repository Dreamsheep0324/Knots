package com.tang.prm.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.entity.TodoItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TodoDaoTest {
    private lateinit var database: TangDatabase
    private lateinit var dao: TodoDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            TangDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.todoDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAll() = runBlocking {
        val todo = TodoItemEntity(title = "待办事项")
        dao.insertTodo(todo)
        val result = dao.getRecentTodos(10).first()
        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("待办事项")
    }

    @Test
    fun updateCompleted() = runBlocking {
        val id = dao.insertTodo(TodoItemEntity(title = "待办事项", isCompleted = false))
        dao.updateTodoCompletion(id, true)
        val result = dao.getRecentTodos(10).first()
        assertThat(result).hasSize(1)
        assertThat(result[0].isCompleted).isTrue()
    }

    @Test
    fun deleteTodo() = runBlocking {
        val id = dao.insertTodo(TodoItemEntity(title = "待办事项"))
        dao.deleteTodoById(id)
        val result = dao.getRecentTodos(10).first()
        assertThat(result).isEmpty()
    }
}
