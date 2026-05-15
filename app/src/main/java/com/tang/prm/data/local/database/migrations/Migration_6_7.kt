package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM custom_types WHERE category IN ('EVENT_TYPE', 'EMOTION', 'WEATHER')")
    }
}
