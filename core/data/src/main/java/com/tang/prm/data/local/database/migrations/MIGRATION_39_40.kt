package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v39 → v40：为 5 个表补齐外键约束，删除父记录时自动清理孤儿数据。
 *
 * 背景：[com.tang.prm.data.local.entity.TodoItemEntity]、
 * [com.tang.prm.data.local.entity.ReminderEntity]、
 * [com.tang.prm.data.local.entity.ContactTagCrossRef]、
 * [com.tang.prm.data.local.entity.CircleEntity]、
 * [com.tang.prm.data.local.entity.ContactEntity] 之前均无 FK 约束，
 * 删除父记录（联系人/事件/纪念日/圈子/分组）时会产生孤儿数据。
 *
 * SQLite 不支持 ALTER TABLE ADD CONSTRAINT，需按标准模式重建表：
 * CREATE new → INSERT SELECT → DROP old → RENAME → CREATE INDEX。
 *
 * 列定义严格对应 [com.tang.prm.data.local.entity] 中各 Entity 的 schema 39 定义。
 */
val MIGRATION_39_40 = object : Migration(39, 40) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. contacts：添加 groupId → contact_groups(id) ON DELETE SET NULL
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `contacts_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `avatar` TEXT,
                `nickname` TEXT,
                `gender` INTEGER NOT NULL,
                `birthday` INTEGER,
                `isLunarBirthday` INTEGER NOT NULL,
                `isLeapMonthBirthday` INTEGER NOT NULL,
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
            "INSERT INTO `contacts_new` (`id`, `name`, `avatar`, `nickname`, `gender`, `birthday`, `isLunarBirthday`, `isLeapMonthBirthday`, `knowingDate`, `phone`, `email`, `city`, `address`, `education`, `company`, `jobTitle`, `industry`, `hobby`, `habit`, `diet`, `skill`, `mbti`, `spouseName`, `childrenCount`, `childrenNames`, `introducer`, `relationshipLevel`, `relationship`, `groupId`, `intimacyScore`, `lastInteractionTime`, `customFields`, `notes`, `createdAt`, `updatedAt`) SELECT `id`, `name`, `avatar`, `nickname`, `gender`, `birthday`, `isLunarBirthday`, `isLeapMonthBirthday`, `knowingDate`, `phone`, `email`, `city`, `address`, `education`, `company`, `jobTitle`, `industry`, `hobby`, `habit`, `diet`, `skill`, `mbti`, `spouseName`, `childrenCount`, `childrenNames`, `introducer`, `relationshipLevel`, `relationship`, `groupId`, `intimacyScore`, `lastInteractionTime`, `customFields`, `notes`, `createdAt`, `updatedAt` FROM `contacts`"
        )
        db.execSQL("DROP TABLE `contacts`")
        db.execSQL("ALTER TABLE `contacts_new` RENAME TO `contacts`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_intimacyScore` ON `contacts`(`intimacyScore`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_lastInteractionTime` ON `contacts`(`lastInteractionTime`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_groupId` ON `contacts`(`groupId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_relationship` ON `contacts`(`relationship`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_name` ON `contacts`(`name`)")

        // 2. todo_items：添加 contactId → contacts CASCADE, eventId → events CASCADE
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `todo_items_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `contactId` INTEGER,
                `eventId` INTEGER,
                `title` TEXT NOT NULL,
                `isCompleted` INTEGER NOT NULL,
                `priority` INTEGER NOT NULL,
                `dueDate` INTEGER,
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`contactId`) REFERENCES `contacts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`eventId`) REFERENCES `events`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            "INSERT INTO `todo_items_new` (`id`, `contactId`, `eventId`, `title`, `isCompleted`, `priority`, `dueDate`, `createdAt`) SELECT `id`, `contactId`, `eventId`, `title`, `isCompleted`, `priority`, `dueDate`, `createdAt` FROM `todo_items`"
        )
        db.execSQL("DROP TABLE `todo_items`")
        db.execSQL("ALTER TABLE `todo_items_new` RENAME TO `todo_items`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_todo_items_contactId` ON `todo_items`(`contactId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_todo_items_isCompleted` ON `todo_items`(`isCompleted`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_todo_items_dueDate` ON `todo_items`(`dueDate`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_todo_items_eventId` ON `todo_items`(`eventId`)")

        // 3. reminders：添加 contactId → contacts CASCADE, eventId → events CASCADE, anniversaryId → anniversaries CASCADE
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `reminders_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `contactId` INTEGER,
                `eventId` INTEGER,
                `anniversaryId` INTEGER,
                `type` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `content` TEXT NOT NULL,
                `time` INTEGER NOT NULL,
                `isCompleted` INTEGER NOT NULL,
                `isIgnored` INTEGER NOT NULL,
                `repeatInterval` INTEGER,
                `createdAt` INTEGER NOT NULL,
                FOREIGN KEY(`contactId`) REFERENCES `contacts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`eventId`) REFERENCES `events`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`anniversaryId`) REFERENCES `anniversaries`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            "INSERT INTO `reminders_new` (`id`, `contactId`, `eventId`, `anniversaryId`, `type`, `title`, `content`, `time`, `isCompleted`, `isIgnored`, `repeatInterval`, `createdAt`) SELECT `id`, `contactId`, `eventId`, `anniversaryId`, `type`, `title`, `content`, `time`, `isCompleted`, `isIgnored`, `repeatInterval`, `createdAt` FROM `reminders`"
        )
        db.execSQL("DROP TABLE `reminders`")
        db.execSQL("ALTER TABLE `reminders_new` RENAME TO `reminders`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_contactId` ON `reminders`(`contactId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_time` ON `reminders`(`time`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_isCompleted` ON `reminders`(`isCompleted`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_eventId` ON `reminders`(`eventId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_anniversaryId` ON `reminders`(`anniversaryId`)")

        // 4. contact_tag_cross_ref：添加 contactId → contacts CASCADE, tagId → contact_tags CASCADE
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `contact_tag_cross_ref_new` (
                `contactId` INTEGER NOT NULL,
                `tagId` INTEGER NOT NULL,
                PRIMARY KEY(`contactId`, `tagId`),
                FOREIGN KEY(`contactId`) REFERENCES `contacts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`tagId`) REFERENCES `contact_tags`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            "INSERT INTO `contact_tag_cross_ref_new` (`contactId`, `tagId`) SELECT `contactId`, `tagId` FROM `contact_tag_cross_ref`"
        )
        db.execSQL("DROP TABLE `contact_tag_cross_ref`")
        db.execSQL("ALTER TABLE `contact_tag_cross_ref_new` RENAME TO `contact_tag_cross_ref`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contact_tag_cross_ref_contactId` ON `contact_tag_cross_ref`(`contactId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contact_tag_cross_ref_tagId` ON `contact_tag_cross_ref`(`tagId`)")

        // 5. circles：添加 parentCircleId → circles(id) ON DELETE SET NULL（自引用）
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `circles_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT,
                `color` TEXT NOT NULL,
                `icon` TEXT NOT NULL,
                `waveform` TEXT NOT NULL,
                `parentCircleId` INTEGER,
                `intimacyThreshold` INTEGER NOT NULL,
                `sortOrder` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                FOREIGN KEY(`parentCircleId`) REFERENCES `circles`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            "INSERT INTO `circles_new` (`id`, `name`, `description`, `color`, `icon`, `waveform`, `parentCircleId`, `intimacyThreshold`, `sortOrder`, `createdAt`, `updatedAt`) SELECT `id`, `name`, `description`, `color`, `icon`, `waveform`, `parentCircleId`, `intimacyThreshold`, `sortOrder`, `createdAt`, `updatedAt` FROM `circles`"
        )
        db.execSQL("DROP TABLE `circles`")
        db.execSQL("ALTER TABLE `circles_new` RENAME TO `circles`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_circles_parentCircleId` ON `circles`(`parentCircleId`)")
    }
}
