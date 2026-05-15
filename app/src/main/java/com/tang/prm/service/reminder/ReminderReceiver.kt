package com.tang.prm.service.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.tang.prm.MainActivity
import com.tang.prm.R
import com.tang.prm.data.local.dao.ReminderDao
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "提醒"
        val content = intent.getStringExtra("content") ?: "您有一条新提醒"
        val reminderId = intent.getLongExtra("reminderId", -1)

        ensureNotificationChannel(context)
        showNotification(context, title, content, reminderId)
    }

    private fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.getNotificationChannel(CHANNEL_ID) != null) return
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(context: Context, title: String, content: String, reminderId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notifyId = if (reminderId > 0) reminderId.toInt() else System.currentTimeMillis().toInt()
        notificationManager.notify(notifyId, notification)
    }

    companion object {
        const val CHANNEL_ID = "tang_reminder_channel"
    }
}

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleReminders(context)
        }
    }

    private fun rescheduleReminders(context: Context) {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                ReminderEntryPoint::class.java
            )
            val reminderDao = entryPoint.reminderDao()

            val pendingResult = goAsync()
            val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
                Log.w("BootReceiver", "重新调度提醒异常", throwable)
            }
            CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler).launch {
                try {
                    val reminders = reminderDao.getActiveRemindersSync()
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                    for (reminder in reminders) {
                        if (reminder.time > System.currentTimeMillis()) {
                            val scheduleIntent = Intent(context, ReminderReceiver::class.java).apply {
                                putExtra("title", reminder.title)
                                putExtra("content", reminder.content)
                                putExtra("reminderId", reminder.id)
                            }
                            val pendingIntent = PendingIntent.getBroadcast(
                                context,
                                reminder.id.toInt(),
                                scheduleIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            scheduleAlarm(alarmManager, reminder.time, pendingIntent)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        } catch (e: Exception) {
            Log.w("BootReceiver", "重新调度提醒失败", e)
        }
    }

    private fun scheduleAlarm(
        alarmManager: android.app.AlarmManager,
        time: Long,
        pendingIntent: PendingIntent
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    time,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                time,
                pendingIntent
            )
        }
    }

    @dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
    @dagger.hilt.EntryPoint
    interface ReminderEntryPoint {
        fun reminderDao(): ReminderDao
    }
}
