package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.json.JSONArray

/**
 * 迁移路径：v31 → v33
 *
 * v31→v32: 无 schema 变更，仅版本号统一
 * v32→v33: 新增 contact_attributes 关联表，迁移 hobby/habit/diet/skill JSON 数据
 */
val MIGRATION_31_33 = object : Migration(31, 33) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // v31→v32: 无 schema 变更

        // v32→v33: Create the new contact_attributes table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS contact_attributes (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                contactId INTEGER NOT NULL,
                category TEXT NOT NULL,
                value TEXT NOT NULL,
                FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE
            )
        """)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_contact_attributes_contactId ON contact_attributes(contactId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_contact_attributes_contactId_category_value ON contact_attributes(contactId, category, value)")

        // Migrate JSON data from contacts table
        val columns = mapOf("hobby" to "HOBBY", "habit" to "HABIT", "diet" to "DIET", "skill" to "SKILL")

        for ((column, category) in columns) {
            db.query("SELECT id, $column FROM contacts WHERE $column IS NOT NULL AND $column != '[]' AND $column != ''").use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val contactId = cursor.getLong(0)
                        val jsonStr = cursor.getString(1) ?: continue

                        val items = try {
                            val arr = JSONArray(jsonStr)
                            (0 until arr.length()).map { arr.getString(it) }
                        } catch (e: Exception) {
                            jsonStr.trim().removeSurrounding("[", "]")
                                .split(",")
                                .map { it.trim().removeSurrounding("\"") }
                                .filter { it.isNotBlank() }
                        }

                        for (item in items) {
                            db.execSQL(
                                "INSERT INTO contact_attributes (contactId, category, value) VALUES (?, ?, ?)",
                                arrayOf<Any>(contactId, category, item)
                            )
                        }
                    } while (cursor.moveToNext())
                }
            }
        }
    }
}
