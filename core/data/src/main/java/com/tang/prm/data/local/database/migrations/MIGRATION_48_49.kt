package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v48 → v49：
 * 清理 contact_relations 表中所有自动推断的关系记录。
 *
 * 自动推断功能（AUTO_CIRCLE / AUTO_EVENT）已彻底移除：
 * - RegenerateAutoRelationsUseCase 已删除
 * - RelationSource 枚举仅保留 MANUAL
 * - ContactRelationDao.deleteBySource 已删除
 *
 * 老用户数据库中可能存在历史遗留的 AUTO_CIRCLE/AUTO_EVENT 记录，
 * 此迁移清理这些"幽灵数据"，避免它们被错误地当作 MANUAL 显示。
 */
val MIGRATION_48_49 = object : Migration(48, 49) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "DELETE FROM contact_relations WHERE source IN ('AUTO_CIRCLE', 'AUTO_EVENT')"
        )
    }
}
