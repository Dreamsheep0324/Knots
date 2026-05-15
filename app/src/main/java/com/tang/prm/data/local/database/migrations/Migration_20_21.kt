package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM event_participants WHERE contactId NOT IN (SELECT id FROM contacts)")
        db.execSQL("DELETE FROM event_participants WHERE eventId NOT IN (SELECT id FROM events)")
        db.execSQL("DELETE FROM gifts WHERE contactId NOT IN (SELECT id FROM contacts)")

        db.execSQL("CREATE TABLE event_participants_new (eventId INTEGER NOT NULL, contactId INTEGER NOT NULL, PRIMARY KEY(eventId, contactId), FOREIGN KEY(eventId) REFERENCES events(id) ON DELETE CASCADE, FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE)")
        db.execSQL("INSERT INTO event_participants_new SELECT * FROM event_participants")
        db.execSQL("DROP TABLE event_participants")
        db.execSQL("ALTER TABLE event_participants_new RENAME TO event_participants")
        db.execSQL("CREATE INDEX index_event_participants_contactId ON event_participants(contactId)")

        db.execSQL("CREATE TABLE gifts_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, contactId INTEGER NOT NULL, giftName TEXT NOT NULL, giftType TEXT NOT NULL, date INTEGER NOT NULL, isSent INTEGER NOT NULL, amount REAL, occasion TEXT, description TEXT, location TEXT, photos TEXT, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE)")
        db.execSQL("INSERT INTO gifts_new SELECT * FROM gifts")
        db.execSQL("DROP TABLE gifts")
        db.execSQL("ALTER TABLE gifts_new RENAME TO gifts")
        db.execSQL("CREATE INDEX index_gifts_contactId ON gifts(contactId)")
    }
}
