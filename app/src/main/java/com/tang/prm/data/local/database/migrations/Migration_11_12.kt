package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS thoughts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "contactId INTEGER," +
                "content TEXT NOT NULL," +
                "mood TEXT," +
                "isPrivate INTEGER NOT NULL," +
                "createdAt INTEGER NOT NULL," +
                "updatedAt INTEGER NOT NULL" +
                ")")
    }
}
