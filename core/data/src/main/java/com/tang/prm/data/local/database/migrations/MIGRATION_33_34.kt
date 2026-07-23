package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_33_34 = object : Migration(33, 34) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `subscriptions` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `icon` TEXT,
                `category` TEXT,
                `price` REAL NOT NULL,
                `currency` TEXT NOT NULL DEFAULT 'CNY',
                `cycle` TEXT NOT NULL,
                `startDate` INTEGER NOT NULL,
                `nextBillingDate` INTEGER NOT NULL,
                `status` TEXT NOT NULL DEFAULT 'ACTIVE',
                `reminderDays` INTEGER NOT NULL DEFAULT 3,
                `notes` TEXT,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_subscriptions_nextBillingDate` ON `subscriptions` (`nextBillingDate`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_subscriptions_status` ON `subscriptions` (`status`)")
    }
}
