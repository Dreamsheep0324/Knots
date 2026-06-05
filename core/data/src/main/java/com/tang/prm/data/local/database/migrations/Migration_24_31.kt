package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 合并迁移快捷路径：v24 → v31，合并了 MIGRATION_24_25 ~ MIGRATION_30_31 的全部操作。
 *
 * 优化点：
 * - circle_member_cross_ref 直接以 v28 最终 schema 创建（复合主键），跳过 v25 的自增 id 版本
 * - circles 表直接移除 memberIds 列，跳过中间步骤
 * - divination_records 直接以 v28 最终 schema 创建（含 aiAnalysis 列），跳过 v27→v28 的 ALTER TABLE
 * - anniversaries 直接以 v29 最终 schema 创建（含外键），跳过中间重建
 */
val MIGRATION_24_31 = object : Migration(24, 31) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ===== 来自 MIGRATION_24_25 (v24→v25): circle_member_cross_ref + circles 去 memberIds =====

        // 直接以 v28 最终 schema 创建 circle_member_cross_ref（复合主键，无自增 id）
        db.execSQL("CREATE TABLE IF NOT EXISTS circle_member_cross_ref (" +
                "circleId INTEGER NOT NULL," +
                "contactId INTEGER NOT NULL," +
                "PRIMARY KEY(circleId, contactId)," +
                "FOREIGN KEY(circleId) REFERENCES circles(id) ON DELETE CASCADE," +
                "FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_circle_member_cross_ref_circleId ON circle_member_cross_ref(circleId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_circle_member_cross_ref_contactId ON circle_member_cross_ref(contactId)")

        // 迁移 memberIds 数据到 cross_ref 表
        val cursor = db.query("SELECT id, memberIds FROM circles WHERE memberIds IS NOT NULL AND memberIds != ''")
        if (cursor.moveToFirst()) {
            do {
                val circleId = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val memberIdsStr = cursor.getString(cursor.getColumnIndexOrThrow("memberIds"))
                if (memberIdsStr != null) {
                    memberIdsStr.split(",").mapNotNull { it.trim().toLongOrNull() }.forEach { contactId ->
                        db.execSQL("INSERT OR IGNORE INTO circle_member_cross_ref (circleId, contactId) VALUES (?, ?)", arrayOf(circleId, contactId))
                    }
                }
            } while (cursor.moveToNext())
        }
        cursor.close()

        // 重建 circles 表，移除 memberIds 列
        db.execSQL("CREATE TABLE circles_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, description TEXT, color TEXT NOT NULL DEFAULT '#6366F1', icon TEXT NOT NULL DEFAULT 'people', waveform TEXT NOT NULL DEFAULT 'sine', parentCircleId INTEGER, intimacyThreshold INTEGER NOT NULL DEFAULT 0, sortOrder INTEGER NOT NULL DEFAULT 0, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
        db.execSQL("INSERT INTO circles_new SELECT id, name, description, color, icon, waveform, parentCircleId, intimacyThreshold, sortOrder, createdAt, updatedAt FROM circles")
        db.execSQL("DROP TABLE circles")
        db.execSQL("ALTER TABLE circles_new RENAME TO circles")

        // ===== 来自 MIGRATION_25_26 (v25→v26): favorites 唯一索引 =====

        db.execSQL("DELETE FROM favorites WHERE id NOT IN (SELECT MIN(id) FROM favorites GROUP BY sourceType, sourceId)")
        db.execSQL("CREATE TABLE favorites_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, sourceType TEXT NOT NULL, sourceId INTEGER NOT NULL, title TEXT NOT NULL, description TEXT, createdAt INTEGER NOT NULL)")
        db.execSQL("INSERT INTO favorites_new SELECT * FROM favorites")
        db.execSQL("DROP TABLE favorites")
        db.execSQL("ALTER TABLE favorites_new RENAME TO favorites")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_favorites_sourceType_sourceId ON favorites(sourceType, sourceId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_favorites_createdAt ON favorites(createdAt)")

        // ===== 来自 MIGRATION_26_27 + MIGRATION_27_28 (v26→v28): divination_records =====

        // 直接以 v28 最终 schema 创建（含 aiAnalysis 列），跳过 v27→v28 的 ALTER TABLE
        db.execSQL("CREATE TABLE IF NOT EXISTS divination_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "method TEXT NOT NULL," +
                "question TEXT NOT NULL," +
                "resultJson TEXT NOT NULL," +
                "aiAnalysis TEXT NOT NULL DEFAULT ''," +
                "createdAt INTEGER NOT NULL" +
                ")")

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
