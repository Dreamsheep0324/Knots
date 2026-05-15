package com.tang.prm.data.local.dao

import androidx.room.*
import com.tang.prm.data.local.entity.ThoughtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThoughtDao {
    @Query("SELECT * FROM thoughts ORDER BY createdAt DESC")
    fun getAllThoughts(): Flow<List<ThoughtEntity>>

    @Query("SELECT * FROM thoughts WHERE contactId = :contactId ORDER BY createdAt DESC")
    fun getThoughtsByContact(contactId: Long): Flow<List<ThoughtEntity>>

    @Query("SELECT * FROM thoughts WHERE type = :type ORDER BY createdAt DESC")
    fun getThoughtsByType(type: String): Flow<List<ThoughtEntity>>

    @Query("SELECT * FROM thoughts WHERE isTodo = 1 ORDER BY dueDate ASC, createdAt DESC")
    fun getTodoThoughts(): Flow<List<ThoughtEntity>>

    @Query("SELECT * FROM thoughts WHERE id = :id")
    fun getThoughtById(id: Long): Flow<ThoughtEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThought(thought: ThoughtEntity): Long

    @Update
    suspend fun updateThought(thought: ThoughtEntity)

    @Query("DELETE FROM thoughts WHERE id = :id")
    suspend fun deleteThoughtById(id: Long)

    @Query("SELECT COUNT(*) FROM thoughts")
    fun getThoughtCount(): Flow<Int>

    @Query("DELETE FROM thoughts WHERE contactId = :contactId")
    suspend fun deleteThoughtsByContact(contactId: Long)
}
