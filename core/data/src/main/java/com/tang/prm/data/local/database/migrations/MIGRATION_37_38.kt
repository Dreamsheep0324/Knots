package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * DB-D-1 修复：历史上此处重复了 Migration_35_36 的 photos_count 回填逻辑。
 * 经核查 Migration_35_36 已正确回填，此迁移为 no-op，保留版本号占位以维持迁移链完整。
 */
val MIGRATION_37_38 = object : Migration(37, 38) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // no-op
    }
}
