package com.tang.prm.data.local.dao

import androidx.room.*
import com.tang.prm.data.local.entity.TodoItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    // DAO-B-3 修复：NULL dueDate 排在末尾（dueDate IS NULL 先判定），让有 deadline 的待办优先展示。
    @Query("SELECT * FROM todo_items WHERE isCompleted = 0 ORDER BY priority DESC, dueDate IS NULL, dueDate ASC")
    fun getActiveTodos(): Flow<List<TodoItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodo(todo: TodoItemEntity): Long

    @Update
    suspend fun updateTodo(todo: TodoItemEntity)

    @Query("UPDATE todo_items SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateTodoCompletion(id: Long, isCompleted: Boolean)

    @Query("DELETE FROM todo_items WHERE id = :id")
    suspend fun deleteTodoById(id: Long)

    @Query("DELETE FROM todo_items WHERE contactId = :contactId")
    suspend fun deleteTodosByContact(contactId: Long)

    @Query("DELETE FROM todo_items WHERE eventId = :eventId")
    suspend fun deleteTodosByEvent(eventId: Long)
}
