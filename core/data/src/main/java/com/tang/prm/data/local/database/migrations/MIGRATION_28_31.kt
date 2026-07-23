package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 合并迁移快捷路径：v28 → v31，合并了 MIGRATION_29_30 ~ MIGRATION_30_31 的全部操作。
 *
 * 背景：v1.0.0 发布时数据库版本为 v28，v1.1.0 升级到 v31。
 * MIGRATION_24_31 覆盖了 v24→v31 的超集，本迁移是其子集，仅包含 v28 之后的变更。
 *
 * 优化点：
 * - anniversaries 直接以 v30 最终 schema 创建（含外键），跳过中间重建
 */
val MIGRATION_28_31 = object : Migration(28, 31) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ===== 来自 MIGRATION_29_30 (v29→v30): anniversaries 外键 =====

        db.execSQL("CREATE TABLE IF NOT EXISTS `anniversaries_tmp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `contactId` INTEGER, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `date` INTEGER NOT NULL, `isLunar` INTEGER NOT NULL, `isRepeat` INTEGER NOT NULL, `reminderDays` INTEGER NOT NULL, `remarks` TEXT, `icon` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`contactId`) REFERENCES `contacts`(`id`) ON DELETE CASCADE)")
        db.execSQL("INSERT INTO `anniversaries_tmp` SELECT * FROM `anniversaries`")
        db.execSQL("DROP TABLE `anniversaries`")
        db.execSQL("ALTER TABLE `anniversaries_tmp` RENAME TO `anniversaries`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_anniversaries_contactId` ON `anniversaries` (`contactId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_anniversaries_date` ON `anniversaries` (`date`)")

        // ===== 来自 MIGRATION_30_31 (v30→v31): events customTypeName =====

        db.execSQL("ALTER TABLE events ADD COLUMN customTypeName TEXT DEFAULT NULL")
    }
}
