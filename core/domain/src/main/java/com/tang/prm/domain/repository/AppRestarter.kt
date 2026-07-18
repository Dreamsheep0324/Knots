package com.tang.prm.domain.repository

/**
 * 应用进程重启器。
 *
 * 备份恢复将数据库文件整体覆盖后，现有 SQLite 连接失效，
 * 必须进程级重启以重建数据库连接池。
 *
 * 抽象为接口使 data 层不直接决定应用生命周期，
 * 由 app 层提供具体实现（AlarmManager + System.exit）。
 */
interface AppRestarter {
    /**
     * 强制重启应用进程。
     *
     * 成功路径：注册 AlarmManager 延迟 1 秒拉起启动 Intent，随后 System.exit(0) 杀死当前进程。
     * 失败路径：当 [android.content.pm.PackageManager.getLaunchIntentForPackage] 返回 null
     * （如 launcher activity 被禁用、厂商 ROM 定制、restricted bucket 等）时，
     * 放弃 System.exit 以避免应用直接消失无法被拉起，返回 false 让调用方走降级路径
     * （如提示用户手动重启）。
     *
     * @return true 表示已成功注册重启 PendingIntent 并 exit；false 表示放弃重启，调用方应降级处理
     */
    suspend fun restart(): Boolean
}
