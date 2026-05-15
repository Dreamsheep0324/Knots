package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE thoughts ADD COLUMN type TEXT NOT NULL DEFAULT 'murmur'")
        db.execSQL("ALTER TABLE thoughts ADD COLUMN isTodo INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE thoughts ADD COLUMN isDone INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE thoughts ADD COLUMN dueDate INTEGER")
        db.execSQL("ALTER TABLE thoughts ADD COLUMN category TEXT")
        db.execSQL("UPDATE thoughts SET type = CASE WHEN contactId IS NOT NULL THEN 'friend' WHEN isPrivate = 1 THEN 'private' ELSE 'murmur' END")
    }
}
