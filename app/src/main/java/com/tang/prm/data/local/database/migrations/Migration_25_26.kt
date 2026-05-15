package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_25_26 = object : Migration(25, 26) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.beginTransaction()
        try {
            db.execSQL("DELETE FROM favorites WHERE id NOT IN (SELECT MIN(id) FROM favorites GROUP BY sourceType, sourceId)")
            db.execSQL("CREATE TABLE favorites_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, sourceType TEXT NOT NULL, sourceId INTEGER NOT NULL, title TEXT NOT NULL, description TEXT, createdAt INTEGER NOT NULL)")
            db.execSQL("INSERT INTO favorites_new SELECT * FROM favorites")
            db.execSQL("DROP TABLE favorites")
            db.execSQL("ALTER TABLE favorites_new RENAME TO favorites")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_favorites_sourceType_sourceId ON favorites(sourceType, sourceId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_favorites_createdAt ON favorites(createdAt)")
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
