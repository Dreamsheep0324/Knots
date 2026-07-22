package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v45 → v46：
 * 新增 contact_relations 表，存储联系人之间的关系（用于关系图谱功能）。
 *
 * 设计要点：
 * - (contactIdA, contactIdB) 唯一索引，约定 contactIdA < contactIdB 防止双向重复
 * - 双外键关联 contacts（CASCADE 删除）和 custom_types（CASCADE 删除）
 * - source 字段区分手动添加（MANUAL）与自动推断（AUTO_CIRCLE/AUTO_EVENT）
 *
 * 同时插入 6 个预设关系类型到 custom_types 表（CATEGORY=RELATIONSHIP），
 * 用户可在自定义类型管理中删除/修改这些预设项。
 */
val MIGRATION_45_46 = object : Migration(45, 46) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. 创建 contact_relations 表
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `contact_relations` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `contactIdA` INTEGER NOT NULL,
                `contactIdB` INTEGER NOT NULL,
                `relationTypeId` INTEGER NOT NULL,
                `note` TEXT,
                `source` TEXT NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                FOREIGN KEY(`contactIdA`) REFERENCES `contacts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`contactIdB`) REFERENCES `contacts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`relationTypeId`) REFERENCES `custom_types`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_contact_relations_contactIdA_contactIdB` ON `contact_relations` (`contactIdA`, `contactIdB`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contact_relations_contactIdB` ON `contact_relations` (`contactIdB`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contact_relations_relationTypeId` ON `contact_relations` (`relationTypeId`)")

        // 2. 插入 6 个预设关系类型（仅当 RELATIONSHIP 类别尚无数据时）
        val cursor = db.query("SELECT COUNT(*) FROM custom_types WHERE category = 'RELATIONSHIP'")
        cursor.use {
            it.moveToFirst()
            val existingCount = it.getInt(0)
            if (existingCount > 0) return@use
            val now = System.currentTimeMillis()
            val presets = listOf(
                Triple<String, String, String>("家人", "#ef4444", "家"),
                Triple("伴侣", "#ec4899", "侣"),
                Triple("朋友", "#8b5cf6", "友"),
                Triple("同事", "#10b981", "事"),
                Triple("同学", "#f59e0b", "学"),
                Triple("其他", "#6b7280", "他")
            )
            presets.forEachIndexed { index, (name, color, icon) ->
                db.execSQL(
                    "INSERT INTO custom_types(category, name, key, color, icon, sortOrder, isDefault, createdAt) VALUES(?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf("RELATIONSHIP", name, name, color, icon, index, 1, now)
                )
            }
        }
    }
}
