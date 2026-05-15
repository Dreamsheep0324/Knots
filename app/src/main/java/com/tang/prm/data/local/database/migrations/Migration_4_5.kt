package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE events_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "title TEXT NOT NULL," +
                "start_time INTEGER NOT NULL," +
                "end_time INTEGER NOT NULL," +
                "location TEXT," +
                "description TEXT," +
                "is_all_day INTEGER NOT NULL," +
                "remind_time INTEGER," +
                "is_repeat INTEGER NOT NULL," +
                "repeat_rule TEXT," +
                "color TEXT," +
                "created_at INTEGER NOT NULL," +
                "updated_at INTEGER NOT NULL" +
                ")")
        db.execSQL("INSERT INTO events_new (id, title, start_time, end_time, location, description, is_all_day, remind_time, is_repeat, repeat_rule, color, created_at, updated_at) " +
                "SELECT id, title, start_time, end_time, location, description, is_all_day, remind_time, is_repeat, repeat_rule, color, created_at, updated_at FROM events")
        db.execSQL("DROP TABLE events")
        db.execSQL("ALTER TABLE events_new RENAME TO events")
    }
}
