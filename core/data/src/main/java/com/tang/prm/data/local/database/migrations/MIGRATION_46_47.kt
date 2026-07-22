package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v46 → v47：
 * 新增 person_relations 表，存储"某联系人与其他人之间的关系"（用于人物详情/编辑界面）。
 *
 * 设计要点：
 * - ownerContactId 必填，删除时 CASCADE（主体删除时其社交关系一并清除）
 * - targetContactId 与 targetName 互斥（一空一非空）：单表承载 App 联系人 + 外部人物两种来源
 * - targetContactId 删除时 SET NULL：保留记录并降级为外部人物，避免关系数据因联系人删除而丢失
 * - relationTypeId 删除时 SET NULL：保留记录，关系词降级为 customLabel 或"其他"
 *
 * 关系类型由用户自行在 App 内创建，不预设任何标签。
 */
val MIGRATION_46_47 = object : Migration(46, 47) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `person_relations` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `ownerContactId` INTEGER NOT NULL,
                `targetContactId` INTEGER,
                `targetName` TEXT,
                `targetAvatar` TEXT,
                `relationTypeId` INTEGER,
                `customLabel` TEXT,
                `note` TEXT,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                FOREIGN KEY(`ownerContactId`) REFERENCES `contacts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`targetContactId`) REFERENCES `contacts`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL,
                FOREIGN KEY(`relationTypeId`) REFERENCES `custom_types`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_person_relations_ownerContactId` ON `person_relations` (`ownerContactId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_person_relations_targetContactId` ON `person_relations` (`targetContactId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_person_relations_relationTypeId` ON `person_relations` (`relationTypeId`)")
    }
}
