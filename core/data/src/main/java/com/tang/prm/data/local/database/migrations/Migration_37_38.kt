package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_37_38 = object : Migration(37, 38) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 回填 photos_count：从 photos JSON 数组计算实际长度
        // 之前 Migration_35_36 添加了列但未回填已有数据，导致首页相册角标显示 0
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
