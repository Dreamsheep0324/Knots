package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v44 → v45：
 * 删除 contacts 表的 relationshipLevel 列。
 *
 * 审查报告 A-11：relationshipLevel 字段无 UI/UseCase 引用，仅在 Mapper/Migration 中透传，
 * 与 intimacyScore 形成双轨制冗余。果断删除以消除语义混淆。
 *
 * SQLite 旧版本不支持 ALTER TABLE DROP COLUMN，需重建表。
 * 6 个索引（intimacyScore/lastInteractionTime/groupId/relationship/name/updatedAt）
 * 随表重建一并重建，与 ContactEntity @Entity indices 声明一致。
 */
val MIGRATION_44_45 = object : Migration(44, 45) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. 重建 contacts 表（去掉 relationshipLevel）
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `contacts_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `avatar` TEXT,
                `nickname` TEXT,
                `gender` INTEGER NOT NULL,
                `birthday` INTEGER,
                `knowingDate` INTEGER,
                `phone` TEXT,
                `email` TEXT,
                `city` TEXT,
                `address` TEXT,
                `education` TEXT,
                `company` TEXT,
                `jobTitle` TEXT,
                `industry` TEXT,
                `hobby` TEXT,
                `habit` TEXT,
                `diet` TEXT,
                `skill` TEXT,
                `mbti` TEXT,
                `spouseName` TEXT,
                `childrenCount` INTEGER NOT NULL,
                `childrenNames` TEXT,
                `introducer` TEXT,
                `relationship` TEXT,
                `groupId` INTEGER,
                `intimacyScore` INTEGER NOT NULL,
                `lastInteractionTime` INTEGER,
                `customFields` TEXT,
                `notes` TEXT,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                FOREIGN KEY(`groupId`) REFERENCES `contact_groups`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `contacts_new` (
                `id`, `name`, `avatar`, `nickname`, `gender`, `birthday`, `knowingDate`,
                `phone`, `email`, `city`, `address`, `education`, `company`, `jobTitle`,
                `industry`, `hobby`, `habit`, `diet`, `skill`, `mbti`, `spouseName`,
                `childrenCount`, `childrenNames`, `introducer`, `relationship`, `groupId`,
                `intimacyScore`, `lastInteractionTime`, `customFields`, `notes`,
                `createdAt`, `updatedAt`
            )
            SELECT
                `id`, `name`, `avatar`, `nickname`, `gender`, `birthday`, `knowingDate`,
                `phone`, `email`, `city`, `address`, `education`, `company`, `jobTitle`,
                `industry`, `hobby`, `habit`, `diet`, `skill`, `mbti`, `spouseName`,
                `childrenCount`, `childrenNames`, `introducer`, `relationship`, `groupId`,
                `intimacyScore`, `lastInteractionTime`, `customFields`, `notes`,
                `createdAt`, `updatedAt`
            FROM `contacts`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `contacts`")
        db.execSQL("ALTER TABLE `contacts_new` RENAME TO `contacts`")

        // 2. 重建 6 个索引（与 ContactEntity @Entity indices 声明一致）
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_intimacyScore` ON `contacts` (`intimacyScore`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_lastInteractionTime` ON `contacts` (`lastInteractionTime`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_groupId` ON `contacts` (`groupId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_relationship` ON `contacts` (`relationship`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_name` ON `contacts` (`name`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_updatedAt` ON `contacts` (`updatedAt`)")
    }
}
