package com.tang.prm.data.local.dao

import androidx.room.*
import com.tang.prm.data.local.entity.TodoItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_items WHERE isCompleted = 0 ORDER BY priority DESC, dueDate ASC")
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
