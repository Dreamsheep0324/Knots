package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.ReminderDao
import com.tang.prm.data.mapper.mapList
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Reminder
import com.tang.prm.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : ReminderRepository {
    override fun observeActiveReminders(): Flow<List<Reminder>> =
        reminderDao.getActiveReminders().mapList { it.toDomain() }

    override fun getRemindersByContact(contactId: Long): Flow<List<Reminder>> =
        reminderDao.getRemindersByContact(contactId).mapList { it.toDomain() }

    override suspend fun insertReminder(reminder: Reminder): Long =
        reminderDao.insertReminder(reminder.toEntity())

    override suspend fun updateReminder(reminder: Reminder) =
        reminderDao.updateReminder(reminder.toEntity())

    override suspend fun markReminderCompleted(id: Long) =
        reminderDao.markReminderCompleted(id)

    override suspend fun markReminderIgnored(id: Long) =
        reminderDao.markReminderIgnored(id)

    override suspend fun deleteReminder(id: Long) =
        reminderDao.deleteReminderById(id)

    override suspend fun getActiveReminders(currentTime: Long): List<Reminder> =
        reminderDao.getActiveRemindersSync(currentTime).map { it.toDomain() }
}
