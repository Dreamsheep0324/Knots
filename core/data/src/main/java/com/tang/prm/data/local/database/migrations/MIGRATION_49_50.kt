package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v49 → v50：
 * 移除 contact_relations 表的 source 列。
 *
 * RelationSource 枚举仅有 MANUAL 一个值（AUTO_CIRCLE/AUTO_EVENT 已在 v49 迁移清理），
 * source 列对所有行恒为 'MANUAL'，属于冗余字段。SQLite 不支持 DROP COLUMN（Room 兼容版本），
 * 因此采用 CREATE+COPY+DROP+RENAME 重建表。
 */
val MIGRATION_49_50 = object : Migration(49, 50) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE contact_relations_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                contactIdA INTEGER NOT NULL,
                contactIdB INTEGER NOT NULL,
                relationTypeId INTEGER NOT NULL,
                note TEXT,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                FOREIGN KEY(contactIdA) REFERENCES contacts(id) ON DELETE CASCADE,
                FOREIGN KEY(contactIdB) REFERENCES contacts(id) ON DELETE CASCADE,
                FOREIGN KEY(relationTypeId) REFERENCES custom_types(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO contact_relations_new (id, contactIdA, contactIdB, relationTypeId, note, createdAt, updatedAt)
            SELECT id, contactIdA, contactIdB, relationTypeId, note, createdAt, updatedAt
            FROM contact_relations
            """.trimIndent()
        )
        db.execSQL("DROP TABLE contact_relations")
        db.execSQL("ALTER TABLE contact_relations_new RENAME TO contact_relations")
        db.execSQL("CREATE UNIQUE INDEX index_contact_relations_contactIdA_contactIdB ON contact_relations(contactIdA, contactIdB)")
        db.execSQL("CREATE INDEX index_contact_relations_contactIdB ON contact_relations(contactIdB)")
        db.execSQL("CREATE INDEX index_contact_relations_relationTypeId ON contact_relations(relationTypeId)")
    }
}
