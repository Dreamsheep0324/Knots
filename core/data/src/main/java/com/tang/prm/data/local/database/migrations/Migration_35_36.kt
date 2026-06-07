package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_35_36 = object : Migration(35, 36) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE events ADD COLUMN photos_count INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE gifts ADD COLUMN photos_count INTEGER NOT NULL DEFAULT 0")

        // 回填 photos_count：从 photos JSON 数组计算实际长度
        // Room 存储 List<String> 为 JSON 数组字符串，如 ["path1","path2"]
        db.execSQL(
            """UPDATE events SET photos_count = CASE
                WHEN photos IS NOT NULL AND photos != '' AND photos != '[]'
                THEN json_array_length(photos)
                ELSE 0 END
                WHERE photos_count = 0"""
        )
        db.execSQL(
            """UPDATE gifts SET photos_count = CASE
                WHEN photos IS NOT NULL AND photos != '' AND photos != '[]'
                THEN json_array_length(photos)
                ELSE 0 END
                WHERE photos_count = 0"""
        )
    }
}
