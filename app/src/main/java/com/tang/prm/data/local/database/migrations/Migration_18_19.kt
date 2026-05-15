package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS favorites (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "sourceType TEXT NOT NULL," +
                "sourceId INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "createdAt INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_favorites_sourceType_sourceId ON favorites(sourceType, sourceId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_favorites_createdAt ON favorites(createdAt)")
    }
}
