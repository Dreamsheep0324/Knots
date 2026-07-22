package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v42 → v43：
 * 纪念日模块彻底移除农历功能：
 * 1. anniversaries 表删除 isLunar、isLeapMonth 列。
 * 2. contacts 表删除 isLunarBirthday、isLeapMonthBirthday 列。
 *
 * 用户决策：纪念日不再支持农历，date 字段始终为公历 epoch millis，
 * 无需做农历→公历的数据转换，直接重建表删除冗余列即可。
 *
 * SQLite 旧版本不支持 ALTER TABLE DROP COLUMN，需重建表。
 */
val MIGRATION_42_43 = object : Migration(42, 43) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. 重建 anniversaries 表（去掉 isLunar / isLeapMonth）
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `anniversaries_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `contactId` INTEGER,
                `name` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `date` INTEGER NOT NULL,
                `isRepeat` INTEGER NOT NULL,
                `reminderDays` INTEGER NOT NULL,
                `remarks` TEXT,
                `icon` TEXT,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                FOREIGN KEY(`contactId`) REFERENCES `contacts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO `anniversaries_new` (
                `id`, `contactId`, `name`, `type`, `date`, `isRepeat`, `reminderDays`,
                `remarks`, `icon`, `createdAt`, `updatedAt`
            )
            SELECT
                `id`, `contactId`, `name`, `type`, `date`, `isRepeat`, `reminderDays`,
                `remarks`, `icon`, `createdAt`, `updatedAt`
            FROM `anniversaries`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `anniversaries`")
        db.execSQL("ALTER TABLE `anniversaries_new` RENAME TO `anniversaries`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_anniversaries_contactId` ON `anniversaries` (`contactId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_anniversaries_date` ON `anniversaries` (`date`)")

        // 2. 重建 contacts 表（去掉 isLunarBirthday / isLeapMonthBirthday）
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
                `relationshipLevel` INTEGER NOT NULL,
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
                `childrenCount`, `childrenNames`, `introducer`, `relationshipLevel`,
                `relationship`, `groupId`, `intimacyScore`, `lastInteractionTime`,
                `customFields`, `notes`, `createdAt`, `updatedAt`
            )
            SELECT
                `id`, `name`, `avatar`, `nickname`, `gender`, `birthday`, `knowingDate`,
                `phone`, `email`, `city`, `address`, `education`, `company`, `jobTitle`,
                `industry`, `hobby`, `habit`, `diet`, `skill`, `mbti`, `spouseName`,
                `childrenCount`, `childrenNames`, `introducer`, `relationshipLevel`,
                `relationship`, `groupId`, `intimacyScore`, `lastInteractionTime`,
                `customFields`, `notes`, `createdAt`, `updatedAt`
            FROM `contacts`
            """.trimIndent()
        )
        db.execSQL("DROP TABLE `contacts`")
        db.execSQL("ALTER TABLE `contacts_new` RENAME TO `contacts`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_intimacyScore` ON `contacts` (`intimacyScore`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_lastInteractionTime` ON `contacts` (`lastInteractionTime`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_groupId` ON `contacts` (`groupId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_relationship` ON `contacts` (`relationship`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_name` ON `contacts` (`name`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_updatedAt` ON `contacts` (`updatedAt`)")
    }
}
