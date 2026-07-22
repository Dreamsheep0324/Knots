package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v43 → v44：
 * 彻底移除"花费"功能：events 与 gifts 表的 amount 列删除。
 *
 * 用户决策：产品不再记录金钱往来金额，所有 UI 入口同步移除。
 * 直接重建表删除冗余列，amount 列允许 NULL，丢失即"无金额"，
 * 与新模型语义一致，无需做数据转换。
 *
 * SQLite 旧版本不支持 ALTER TABLE DROP COLUMN，需重建表。
 */
val MIGRATION_43_44 = object : Migration(43, 44) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. 重建 events 表（去掉 amount）
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `events_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `type` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `customTypeName` TEXT,
                `description` TEXT,
                `time` INTEGER NOT NULL,
                `endTime` INTEGER,
                `location` TEXT,
                `latitude` REAL,
                `longitude` REAL,
                `photos` TEXT NOT NULL,
                `photos_count` INTEGER NOT NULL DEFAULT 0,
                `emotion` TEXT,
                `weather` TEXT,
                `remarks` TEXT,
                `promise` TEXT,
                `conversationSummary` TEXT,
                `giftName` TEXT,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `events_new` (
                `id`, `type`, `title`, `customTypeName`, `description`, `time`, `endTime`,
                `location`, `latitude`, `longitude`, `photos`, `photos_count`,
                `emotion`, `weather`, `remarks`, `promise`, `conversationSummary`,
                `giftName`, `createdAt`, `updatedAt`
            )
            SELECT
                `id`, `type`, `title`, `customTypeName`, `description`, `time`, `endTime`,
                `location`, `latitude`, `longitude`, `photos`, `photos_count`,
                `emotion`, `weather`, `remarks`, `promise`, `conversationSummary`,
                `giftName`, `createdAt`, `updatedAt`
            FROM `events`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `events`")
        db.execSQL("ALTER TABLE `events_new` RENAME TO `events`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_events_time` ON `events` (`time`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_events_type` ON `events` (`type`)")

        // 2. 重建 gifts 表（去掉 amount）
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `gifts_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `contactId` INTEGER NOT NULL,
                `giftName` TEXT NOT NULL,
                `giftType` TEXT NOT NULL,
                `date` INTEGER NOT NULL,
                `isSent` INTEGER NOT NULL,
                `occasion` TEXT,
                `description` TEXT,
                `location` TEXT,
                `photos` TEXT NOT NULL,
                `photos_count` INTEGER NOT NULL DEFAULT 0,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                FOREIGN KEY(`contactId`) REFERENCES `contacts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `gifts_new` (
                `id`, `contactId`, `giftName`, `giftType`, `date`, `isSent`,
                `occasion`, `description`, `location`, `photos`, `photos_count`,
                `createdAt`, `updatedAt`
            )
            SELECT
                `id`, `contactId`, `giftName`, `giftType`, `date`, `isSent`,
                `occasion`, `description`, `location`, `photos`, `photos_count`,
                `createdAt`, `updatedAt`
            FROM `gifts`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `gifts`")
        db.execSQL("ALTER TABLE `gifts_new` RENAME TO `gifts`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_gifts_contactId` ON `gifts` (`contactId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_gifts_date` ON `gifts` (`date`)")
    }
}
