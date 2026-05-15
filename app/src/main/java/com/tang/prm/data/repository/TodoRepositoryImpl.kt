package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.TodoDao
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.TodoItem
import com.tang.prm.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao
) : TodoRepository {
    override fun getActiveTodos(): Flow<List<TodoItem>> =
        todoDao.getActiveTodos().map { entities -> entities.map { it.toDomain() } }

    override fun getTodosByContact(contactId: Long): Flow<List<TodoItem>> =
        todoDao.getTodosByContact(contactId).map { entities -> entities.map { it.toDomain() } }

    override fun getTodosDueInRange(startTime: Long, endTime: Long): Flow<List<TodoItem>> =
        todoDao.getTodosDueInRange(startTime, endTime).map { entities -> entities.map { it.toDomain() } }

    override fun getRecentTodos(limit: Int): Flow<List<TodoItem>> =
        todoDao.getRecentTodos(limit).map { entities -> entities.map { it.toDomain() } }

    override suspend fun insertTodo(todo: TodoItem): Long =
        todoDao.insertTodo(todo.toEntity())

    override suspend fun updateTodo(todo: TodoItem) =
        todoDao.updateTodo(todo.toEntity())

    override suspend fun updateTodoCompletion(id: Long, isCompleted: Boolean) =
        todoDao.updateTodoCompletion(id, isCompleted)

    override suspend fun deleteTodo(id: Long) =
        todoDao.deleteTodoById(id)
}
