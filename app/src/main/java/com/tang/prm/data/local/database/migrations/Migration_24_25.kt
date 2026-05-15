package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_24_25 = object : Migration(24, 25) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS circle_member_cross_ref (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, circleId INTEGER NOT NULL, contactId INTEGER NOT NULL, FOREIGN KEY(circleId) REFERENCES circles(id) ON DELETE CASCADE, FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_circle_member_cross_ref_circleId ON circle_member_cross_ref(circleId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_circle_member_cross_ref_contactId ON circle_member_cross_ref(contactId)")

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

        db.execSQL("CREATE TABLE circles_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, description TEXT, color TEXT NOT NULL DEFAULT '#6366F1', icon TEXT NOT NULL DEFAULT 'people', waveform TEXT NOT NULL DEFAULT 'sine', parentCircleId INTEGER, intimacyThreshold INTEGER NOT NULL DEFAULT 0, sortOrder INTEGER NOT NULL DEFAULT 0, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
        db.execSQL("INSERT INTO circles_new SELECT id, name, description, color, icon, waveform, parentCircleId, intimacyThreshold, sortOrder, createdAt, updatedAt FROM circles")
        db.execSQL("DROP TABLE circles")
        db.execSQL("ALTER TABLE circles_new RENAME TO circles")
    }
}
