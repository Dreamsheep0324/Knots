package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_36_37 = object : Migration(36, 37) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE subscriptions ADD COLUMN timezone TEXT NOT NULL DEFAULT 'UTC'")
    }
}
