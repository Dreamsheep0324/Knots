package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_32_33 = object : Migration(32, 33) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create the new contact_attributes table
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
        // hobby, habit, diet, skill columns contain JSON arrays like ["篮球","足球"]
        // We need to parse them and insert as rows
        // Since we can't parse JSON in SQLite, we'll use a workaround:
        // For each column, read the data in a cursor and insert rows

        val columns = mapOf("hobby" to "HOBBY", "habit" to "HABIT", "diet" to "DIET", "skill" to "SKILL")

        for ((column, category) in columns) {
            val cursor = db.query("SELECT id, $column FROM contacts WHERE $column IS NOT NULL AND $column != '[]' AND $column != ''")
            if (cursor.moveToFirst()) {
                do {
                    val contactId = cursor.getLong(0)
                    val jsonStr = cursor.getString(1) ?: continue

                    // Simple JSON array parsing: remove [ ] and split by ,
                    val items = jsonStr
                        .trim()
                        .removeSurrounding("[", "]")
                        .split(",")
                        .map { it.trim().removeSurrounding("\"") }
                        .filter { it.isNotBlank() }

                    for (item in items) {
                        db.execSQL(
                            "INSERT INTO contact_attributes (contactId, category, value) VALUES (?, ?, ?)",
                            arrayOf<Any>(contactId, category, item)
                        )
                    }
                } while (cursor.moveToNext())
            }
            cursor.close()
        }

        // Keep the original JSON columns for backward compatibility during transition
        // They will be removed in a future migration after confirming the new table works
    }
}
