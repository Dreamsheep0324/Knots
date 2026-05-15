package com.tang.prm.data.local.dao

import androidx.room.*
import com.tang.prm.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isCompleted = 0 AND isIgnored = 0 ORDER BY time ASC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE time BETWEEN :startTime AND :endTime AND isCompleted = 0 AND isIgnored = 0")
    fun getRemindersInRange(startTime: Long, endTime: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE contactId = :contactId AND isCompleted = 0 AND isIgnored = 0 ORDER BY time ASC")
    fun getRemindersByContact(contactId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE anniversaryId = :anniversaryId AND isCompleted = 0 AND isIgnored = 0 ORDER BY time ASC")
    fun getRemindersByAnniversary(anniversaryId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getReminderById(id: Long): Flow<ReminderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isCompleted = 1 WHERE id = :id")
    suspend fun markReminderCompleted(id: Long)

    @Query("UPDATE reminders SET isIgnored = 1 WHERE id = :id")
    suspend fun markReminderIgnored(id: Long)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 AND isIgnored = 0 AND time > :currentTime")
    suspend fun getActiveRemindersSync(currentTime: Long = System.currentTimeMillis()): List<ReminderEntity>

    @Query("DELETE FROM reminders WHERE contactId = :contactId")
    suspend fun deleteRemindersByContact(contactId: Long)

    @Query("DELETE FROM reminders WHERE eventId = :eventId")
    suspend fun deleteRemindersByEvent(eventId: Long)
}