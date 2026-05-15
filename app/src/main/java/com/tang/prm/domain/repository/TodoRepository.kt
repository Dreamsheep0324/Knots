package com.tang.prm.domain.repository

import com.tang.prm.domain.model.TodoItem
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    fun getActiveTodos(): Flow<List<TodoItem>>
    fun getTodosByContact(contactId: Long): Flow<List<TodoItem>>
    fun getTodosDueInRange(startTime: Long, endTime: Long): Flow<List<TodoItem>>
    fun getRecentTodos(limit: Int): Flow<List<TodoItem>>
    suspend fun insertTodo(todo: TodoItem): Long
    suspend fun updateTodo(todo: TodoItem)
    suspend fun updateTodoCompletion(id: Long, isCompleted: Boolean)
    suspend fun deleteTodo(id: Long)
}
