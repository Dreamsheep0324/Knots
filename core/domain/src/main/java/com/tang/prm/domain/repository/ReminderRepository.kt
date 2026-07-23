package com.tang.prm.domain.repository

import com.tang.prm.domain.model.Reminder
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun observeActiveReminders(): Flow<List<Reminder>>
    fun getRemindersByContact(contactId: Long): Flow<List<Reminder>>
    suspend fun insertReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun markReminderCompleted(id: Long)
    suspend fun markReminderIgnored(id: Long)
    suspend fun deleteReminder(id: Long)

    /**
     * A-14 修复：补充 KDoc。
     *
     * 与 [observeActiveReminders]（返回 Flow，响应式订阅）不同，此方法是 **suspend + 一次性快照**，
     * 用于需要立即拿到当前活跃提醒列表的非响应式场景（如 WorkManager 后台任务构造通知）。
     *
     * 默认参数 [currentTime] 允许调用方注入时间（测试可固定时间避免 flaky），
     * 不传则取 [System.currentTimeMillis]。
     *
     * @param currentTime 用于判定"活跃"的基准时间戳（epoch millis）
     * @return 当前活跃的提醒列表（已过期未完成的也视为活跃，等待补通知）
     */
    suspend fun getActiveReminders(currentTime: Long = System.currentTimeMillis()): List<Reminder>
}
