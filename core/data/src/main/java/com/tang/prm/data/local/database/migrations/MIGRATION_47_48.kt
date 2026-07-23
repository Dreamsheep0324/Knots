package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v47 → v48：
 * 防御性清理 PERSON_RELATION 类型的预设记录。
 *
 * MIGRATION_46_47 实际只建表与索引，从未 INSERT 任何预设类型；
 * 此处 DELETE 是对历史上可能存在的旧版本残留预设做兜底清理，
 * 正常路径下为空操作（无数据可删），仅在不规范的升级路径下生效。
 *
 * 关系类型标签由用户自行在 App 内创建，不预设任何默认值。
 */
val MIGRATION_47_48 = object : Migration(47, 48) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "DELETE FROM custom_types WHERE category = 'PERSON_RELATION' AND isDefault = 1"
        )
    }
}
