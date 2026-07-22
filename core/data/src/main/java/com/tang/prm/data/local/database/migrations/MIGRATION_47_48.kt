package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v47 → v48：
 * 清理 v46→v47 迁移中预设插入的 7 个 PERSON_RELATION 类型。
 *
 * 关系类型标签由用户自行在 App 内创建，不预设任何默认值。
 * 仅删除 isDefault=1 的 PERSON_RELATION 记录，保留用户已创建的类型。
 */
val MIGRATION_47_48 = object : Migration(47, 48) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "DELETE FROM custom_types WHERE category = 'PERSON_RELATION' AND isDefault = 1"
        )
    }
}
