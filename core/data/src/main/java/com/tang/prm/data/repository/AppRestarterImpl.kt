package com.tang.prm.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import com.tang.prm.domain.repository.AppRestarter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * [AppRestarter] 的默认实现：通过 AlarmManager 延迟 1 秒拉起启动 Intent，
 * 随后 System.exit(0) 杀死当前进程。
 */
class AppRestarterImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppRestarter {

    override fun restart() {
        Log.i(TAG, "应用即将重启进程：数据库文件已被覆盖，现有连接失效，需要进程级重启以重建连接池")

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1000,
                pendingIntent
            )
        }
        System.exit(0)
    }

    private companion object {
        const val TAG = "AppRestarter"
    }
}
