package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS gifts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "contactId INTEGER NOT NULL," +
                "giftName TEXT NOT NULL," +
                "giftType TEXT NOT NULL," +
                "date INTEGER NOT NULL," +
                "isSent INTEGER NOT NULL," +
                "amount REAL," +
                "occasion TEXT," +
                "description TEXT," +
                "location TEXT," +
                "photos TEXT," +
                "createdAt INTEGER NOT NULL," +
                "updatedAt INTEGER NOT NULL" +
                ")")
    }
}
