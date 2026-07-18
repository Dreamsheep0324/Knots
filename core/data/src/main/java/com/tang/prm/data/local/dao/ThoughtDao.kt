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

    // DAO-B-3 修复：NULL dueDate 排在末尾，让有 deadline 的待办优先展示。
    @Query("SELECT * FROM thoughts WHERE isTodo = 1 ORDER BY dueDate IS NULL, dueDate ASC, createdAt DESC")
    fun getTodoThoughts(): Flow<List<ThoughtEntity>>

    // DAO-D-7 修复：移除零调用的 getThoughtById Flow 变体。
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThought(thought: ThoughtEntity): Long

    @Update
    suspend fun updateThought(thought: ThoughtEntity)

    @Query("DELETE FROM thoughts WHERE id = :id")
    suspend fun deleteThoughtById(id: Long)

    @Query("SELECT COUNT(*) FROM thoughts")
    fun getThoughtCount(): Flow<Int>

    // 保留 getThoughtsByContactOnce：仅 androidTest 验证 FK CASCADE 使用，是合理的测试辅助方法。
    @Query("SELECT * FROM thoughts WHERE contactId = :contactId")
    suspend fun getThoughtsByContactOnce(contactId: Long): List<ThoughtEntity>
}
