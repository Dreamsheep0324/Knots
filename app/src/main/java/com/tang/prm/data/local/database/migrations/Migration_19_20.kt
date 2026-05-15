package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 移除照片收藏标题中的 📷 前缀（emoji 的 UTF-16 代理对：U+D83D U+DCF9）
        db.execSQL("UPDATE favorites SET title = REPLACE(title, CHAR(55357) || CHAR(56825) || ' ', '') WHERE sourceType = 'PHOTO'")
    }
}
