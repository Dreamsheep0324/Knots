package com.tang.prm.domain.repository

import com.tang.prm.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getActiveReminders(): Flow<List<Reminder>>
    fun getRemindersByContact(contactId: Long): Flow<List<Reminder>>
    suspend fun insertReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun markReminderCompleted(id: Long)
    suspend fun markReminderIgnored(id: Long)
    suspend fun deleteReminder(id: Long)
    suspend fun getActiveRemindersSync(currentTime: Long = System.currentTimeMillis()): List<Reminder>
}
