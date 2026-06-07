package com.tang.prm.data.local.database

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tang.prm.data.local.database.migrations.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDbName = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TangDatabase::class.java
    )

    private val allMigrations = arrayOf(
        MIGRATION_1_32, MIGRATION_24_31, MIGRATION_31_33, MIGRATION_32_33,
        MIGRATION_33_34, MIGRATION_34_35, MIGRATION_35_36, MIGRATION_36_37,
        MIGRATION_37_38
    )

    @Test
    fun migrate1To38() {
        val db = helper.createDatabase(testDbName, 1)
        db.execSQL("INSERT INTO contacts (id, name, phone) VALUES (1, '测试联系人', '13800138000')")
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 38, true, *allMigrations)

        val cursor = migratedDb.query("SELECT * FROM contacts WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals("测试联系人", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migrate32To38() {
        val db = helper.createDatabase(testDbName, 32)
        val values = ContentValues().apply {
            put("id", 1L)
            put("name", "测试")
            put("phone", "13800138000")
            put("relationship", "friend")
        }
        db.insert("contacts", SQLiteDatabase.CONFLICT_REPLACE, values)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 38, true,
            MIGRATION_32_33, MIGRATION_33_34, MIGRATION_34_35,
            MIGRATION_35_36, MIGRATION_36_37, MIGRATION_37_38
        )
        migratedDb.close()
    }

    @Test
    fun migrate35To38_photosCountBackfilled() {
        val db = helper.createDatabase(testDbName, 35)
        val values = ContentValues().apply {
            put("id", 1L)
            put("contact_id", 1L)
            put("title", "测试礼物")
            put("photos", "[\"/path1.jpg\",\"/path2.jpg\"]")
            put("photos_count", 0)
        }
        db.insert("gifts", SQLiteDatabase.CONFLICT_REPLACE, values)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 38, true,
            MIGRATION_35_36, MIGRATION_36_37, MIGRATION_37_38
        )

        val cursor = migratedDb.query("SELECT photos_count FROM gifts WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals(2, cursor.getInt(0))
        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migrate36To38_timezoneAdded() {
        val db = helper.createDatabase(testDbName, 36)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 38, true,
            MIGRATION_36_37, MIGRATION_37_38
        )

        val cursor = migratedDb.query("PRAGMA table_info(subscriptions)")
        var hasTimezone = false
        while (cursor.moveToNext()) {
            if (cursor.getString(cursor.getColumnIndexOrThrow("name")) == "timezone") {
                hasTimezone = true
            }
        }
        assertTrue(hasTimezone)
        cursor.close()
        migratedDb.close()
    }

    @Test
    fun allMigrationsCoverGap() {
        val db = helper.createDatabase(testDbName, 1)
        db.close()
        // 如果缺少任何中间迁移，此调用会抛异常
        helper.runMigrationsAndValidate(testDbName, 38, true, *allMigrations)
    }
}
