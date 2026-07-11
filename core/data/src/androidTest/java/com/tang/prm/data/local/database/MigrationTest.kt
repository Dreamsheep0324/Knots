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
        MIGRATION_37_38, MIGRATION_38_39, MIGRATION_39_40
    )

    @Test
    fun migrate1To40() {
        val db = helper.createDatabase(testDbName, 1)
        db.execSQL("INSERT INTO contacts (id, name, phone) VALUES (1, '测试联系人', '13800138000')")
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 40, true, *allMigrations)

        val cursor = migratedDb.query("SELECT * FROM contacts WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals("测试联系人", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migrate32To40() {
        val db = helper.createDatabase(testDbName, 32)
        val values = ContentValues().apply {
            put("id", 1L)
            put("name", "测试")
            put("phone", "13800138000")
            put("relationship", "friend")
        }
        db.insert("contacts", SQLiteDatabase.CONFLICT_REPLACE, values)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 40, true,
            MIGRATION_32_33, MIGRATION_33_34, MIGRATION_34_35,
            MIGRATION_35_36, MIGRATION_36_37, MIGRATION_37_38,
            MIGRATION_38_39, MIGRATION_39_40
        )
        migratedDb.close()
    }

    @Test
    fun migrate35To40_photosCountBackfilled() {
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

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 40, true,
            MIGRATION_35_36, MIGRATION_36_37, MIGRATION_37_38,
            MIGRATION_38_39, MIGRATION_39_40
        )

        val cursor = migratedDb.query("SELECT photos_count FROM gifts WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals(2, cursor.getInt(0))
        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migrate36To40_timezoneAdded() {
        val db = helper.createDatabase(testDbName, 36)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 40, true,
            MIGRATION_36_37, MIGRATION_37_38, MIGRATION_38_39, MIGRATION_39_40
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
    fun migrate38To39_leapMonthAdded() {
        val db = helper.createDatabase(testDbName, 38)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 39, true, MIGRATION_38_39)

        // anniversaries 表新增 isLeapMonth 字段
        val anniversaryCursor = migratedDb.query("PRAGMA table_info(anniversaries)")
        var hasIsLeapMonth = false
        while (anniversaryCursor.moveToNext()) {
            if (anniversaryCursor.getString(anniversaryCursor.getColumnIndexOrThrow("name")) == "isLeapMonth") {
                hasIsLeapMonth = true
            }
        }
        assertTrue(hasIsLeapMonth)
        anniversaryCursor.close()

        // contacts 表新增 isLeapMonthBirthday 字段
        val contactCursor = migratedDb.query("PRAGMA table_info(contacts)")
        var hasIsLeapMonthBirthday = false
        while (contactCursor.moveToNext()) {
            if (contactCursor.getString(contactCursor.getColumnIndexOrThrow("name")) == "isLeapMonthBirthday") {
                hasIsLeapMonthBirthday = true
            }
        }
        assertTrue(hasIsLeapMonthBirthday)
        contactCursor.close()
        migratedDb.close()
    }

    @Test
    fun migrate39To40_foreignKeysAdded() {
        val db = helper.createDatabase(testDbName, 39)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 40, true, MIGRATION_39_40)

        // 验证 contacts 表有 groupId 外键
        val contactsFkCursor = migratedDb.query("PRAGMA foreign_key_list(contacts)")
        assertTrue("contacts 表应有 groupId 外键", contactsFkCursor.moveToFirst())
        contactsFkCursor.close()

        // 验证 todo_items 表有外键
        val todoFkCursor = migratedDb.query("PRAGMA foreign_key_list(todo_items)")
        assertTrue("todo_items 表应有外键", todoFkCursor.moveToFirst())
        todoFkCursor.close()

        // 验证 reminders 表有外键
        val reminderFkCursor = migratedDb.query("PRAGMA foreign_key_list(reminders)")
        assertTrue("reminders 表应有外键", reminderFkCursor.moveToFirst())
        reminderFkCursor.close()

        // 验证 contact_tag_cross_ref 表有外键
        val crossRefFkCursor = migratedDb.query("PRAGMA foreign_key_list(contact_tag_cross_ref)")
        assertTrue("contact_tag_cross_ref 表应有外键", crossRefFkCursor.moveToFirst())
        crossRefFkCursor.close()

        // 验证 circles 表有外键
        val circlesFkCursor = migratedDb.query("PRAGMA foreign_key_list(circles)")
        assertTrue("circles 表应有外键", circlesFkCursor.moveToFirst())
        circlesFkCursor.close()

        migratedDb.close()
    }

    @Test
    fun allMigrationsCoverGap() {
        val db = helper.createDatabase(testDbName, 1)
        db.close()
        // 如果缺少任何中间迁移，此调用会抛异常
        helper.runMigrationsAndValidate(testDbName, 40, true, *allMigrations)
    }
}
