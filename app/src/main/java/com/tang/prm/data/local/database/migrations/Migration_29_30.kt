package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_29_30 = object : Migration(29, 30) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `anniversaries_tmp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `contactId` INTEGER, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `date` INTEGER NOT NULL, `isLunar` INTEGER NOT NULL, `isRepeat` INTEGER NOT NULL, `reminderDays` INTEGER NOT NULL, `remarks` TEXT, `icon` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`contactId`) REFERENCES `contacts`(`id`) ON DELETE CASCADE)")
        db.execSQL("INSERT INTO `anniversaries_tmp` SELECT * FROM `anniversaries`")
        db.execSQL("DROP TABLE `anniversaries`")
        db.execSQL("ALTER TABLE `anniversaries_tmp` RENAME TO `anniversaries`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_anniversaries_contactId` ON `anniversaries` (`contactId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_anniversaries_date` ON `anniversaries` (`date`)")
    }
}
