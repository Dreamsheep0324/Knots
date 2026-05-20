package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_28_29 = object : Migration(28, 29) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE circle_member_cross_ref_new (" +
                "circleId INTEGER NOT NULL," +
                "contactId INTEGER NOT NULL," +
                "PRIMARY KEY(circleId, contactId)," +
                "FOREIGN KEY(circleId) REFERENCES circles(id) ON DELETE CASCADE," +
                "FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE)")
        db.execSQL("INSERT OR IGNORE INTO circle_member_cross_ref_new (circleId, contactId) SELECT circleId, contactId FROM circle_member_cross_ref")
        db.execSQL("DROP TABLE circle_member_cross_ref")
        db.execSQL("ALTER TABLE circle_member_cross_ref_new RENAME TO circle_member_cross_ref")
        db.execSQL("CREATE INDEX index_circle_member_cross_ref_contactId ON circle_member_cross_ref(contactId)")
    }
}
