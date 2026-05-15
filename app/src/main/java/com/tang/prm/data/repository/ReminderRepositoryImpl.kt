package com.tang.prm.data.repository

import com.tang.prm.data.local.dao.ReminderDao
import com.tang.prm.data.mapper.toDomain
import com.tang.prm.data.mapper.toEntity
import com.tang.prm.domain.model.Reminder
import com.tang.prm.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : ReminderRepository {
    override fun getActiveReminders(): Flow<List<Reminder>> =
        reminderDao.getActiveReminders().map { entities -> entities.map { it.toDomain() } }

    override fun getRemindersInRange(startTime: Long, endTime: Long): Flow<List<Reminder>> =
        reminderDao.getRemindersInRange(startTime, endTime).map { entities -> entities.map { it.toDomain() } }

    override fun getRemindersByContact(contactId: Long): Flow<List<Reminder>> =
        reminderDao.getRemindersByContact(contactId).map { entities -> entities.map { it.toDomain() } }

    override fun getRemindersByAnniversary(anniversaryId: Long): Flow<List<Reminder>> =
        reminderDao.getRemindersByAnniversary(anniversaryId).map { entities -> entities.map { it.toDomain() } }

    override fun getReminderById(id: Long): Flow<Reminder?> =
        reminderDao.getReminderById(id).map { it?.toDomain() }

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
}
