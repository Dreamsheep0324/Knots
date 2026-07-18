package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v41 → v42：
 * 1. subscriptions 表 timezone 列默认值从 'UTC' 改为 'Asia/Shanghai'（MAP-B-4）。
 *    SQLite 不支持 ALTER COLUMN，需重建表。
 * 2. contacts 表新增 updatedAt 索引，优化最高频查询 getAllContacts 的 ORDER BY updatedAt DESC（DAO-A-2）。
 */
val MIGRATION_41_42 = object : Migration(41, 42) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. 重建 subscriptions 表，仅修改 timezone 列默认值
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `subscriptions_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `icon` TEXT,
                `category` TEXT,
                `price` REAL NOT NULL,
                `currency` TEXT NOT NULL,
                `cycle` TEXT NOT NULL,
                `startDate` INTEGER NOT NULL,
                `nextBillingDate` INTEGER NOT NULL,
                `status` TEXT NOT NULL,
                `reminderDays` INTEGER NOT NULL,
                `notes` TEXT,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `timezone` TEXT NOT NULL DEFAULT 'Asia/Shanghai'
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `subscriptions_new` (`id`, `name`, `icon`, `category`, `price`, `currency`,
                `cycle`, `startDate`, `nextBillingDate`, `status`, `reminderDays`, `notes`,
                `createdAt`, `updatedAt`, `timezone`)
            SELECT `id`, `name`, `icon`, `category`, `price`, `currency`,
                `cycle`, `startDate`, `nextBillingDate`, `status`, `reminderDays`, `notes`,
                `createdAt`, `updatedAt`, `timezone`
            FROM `subscriptions`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `subscriptions`")
        db.execSQL("ALTER TABLE `subscriptions_new` RENAME TO `subscriptions`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_subscriptions_nextBillingDate` ON `subscriptions` (`nextBillingDate`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_subscriptions_status` ON `subscriptions` (`status`)")

        // 2. contacts 表新增 updatedAt 索引
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_updatedAt` ON `contacts` (`updatedAt`)")
    }
}
