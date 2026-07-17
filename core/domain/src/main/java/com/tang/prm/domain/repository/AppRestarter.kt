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
     * 强制重启应用进程。调用后进程将被杀死，1 秒后由 AlarmManager 重新拉起。
     * 此方法不会返回。
     */
    fun restart()
}
