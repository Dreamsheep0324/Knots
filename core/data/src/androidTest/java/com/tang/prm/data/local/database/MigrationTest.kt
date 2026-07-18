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
        MIGRATION_1_32, MIGRATION_28_31, MIGRATION_24_31, MIGRATION_31_33, MIGRATION_32_33,
        MIGRATION_33_34, MIGRATION_34_35, MIGRATION_35_36, MIGRATION_36_37,
        MIGRATION_37_38, MIGRATION_38_39, MIGRATION_39_40, MIGRATION_40_41,
        MIGRATION_41_42
    )

    @Test
    fun migrate1To42() {
        val db = helper.createDatabase(testDbName, 1)
        db.execSQL("INSERT INTO contacts (id, name, phone) VALUES (1, '测试联系人', '13800138000')")
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 42, true, *allMigrations)

        val cursor = migratedDb.query("SELECT * FROM contacts WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals("测试联系人", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migrate32To42() {
        val db = helper.createDatabase(testDbName, 32)
        val values = ContentValues().apply {
            put("id", 1L)
            put("name", "测试")
            put("phone", "13800138000")
            put("relationship", "friend")
        }
        db.insert("contacts", SQLiteDatabase.CONFLICT_REPLACE, values)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 42, true,
            MIGRATION_32_33, MIGRATION_33_34, MIGRATION_34_35,
            MIGRATION_35_36, MIGRATION_36_37, MIGRATION_37_38,
            MIGRATION_38_39, MIGRATION_39_40, MIGRATION_40_41, MIGRATION_41_42
        )
        migratedDb.close()
    }

    /**
     * 验证 MIGRATION_35_36 的 photos_count 回填逻辑。
     *
     * v35 gifts 表列名：camelCase（contactId/giftName/giftType/isSent/date/photos/createdAt/updatedAt）
     * 无 photos_count 列（v35→v36 才 ALTER TABLE 添加），无 contact_id、无 title 列。
     * 测试插入时必须满足 NOT NULL 约束且先插 contacts 满足外键。
     */
    @Test
    fun migrate35To42_photosCountBackfilled() {
        val db = helper.createDatabase(testDbName, 35)

        // 先插 contacts 满足 gifts 的 contactId 外键约束
        val contactValues = ContentValues().apply {
            put("id", 1L)
            put("name", "测试联系人")
            put("gender", 0)
            put("isLunarBirthday", 0)
            put("childrenCount", 0)
            put("relationshipLevel", 0)
            put("intimacyScore", 0)
            put("createdAt", System.currentTimeMillis())
            put("updatedAt", System.currentTimeMillis())
        }
        db.insert("contacts", SQLiteDatabase.CONFLICT_REPLACE, contactValues)

        // v35 gifts 表：contactId/giftName/giftType/date/isSent/photos/createdAt/updatedAt 均为 NOT NULL
        // 不传 photos_count：v35 不存在该列，v35→v36 才添加
        val giftValues = ContentValues().apply {
            put("id", 1L)
            put("contactId", 1L)
            put("giftName", "测试礼物")
            put("giftType", "GENERAL")
            put("date", System.currentTimeMillis())
            put("isSent", 0)
            put("photos", "[\"/path1.jpg\",\"/path2.jpg\"]")
            put("createdAt", System.currentTimeMillis())
            put("updatedAt", System.currentTimeMillis())
        }
        db.insert("gifts", SQLiteDatabase.CONFLICT_REPLACE, giftValues)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 42, true,
            MIGRATION_35_36, MIGRATION_36_37, MIGRATION_37_38,
            MIGRATION_38_39, MIGRATION_39_40, MIGRATION_40_41, MIGRATION_41_42
        )

        val cursor = migratedDb.query("SELECT photos_count FROM gifts WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals(2, cursor.getInt(0))
        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migrate36To42_timezoneAdded() {
        val db = helper.createDatabase(testDbName, 36)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 42, true,
            MIGRATION_36_37, MIGRATION_37_38, MIGRATION_38_39, MIGRATION_39_40,
            MIGRATION_40_41, MIGRATION_41_42
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

    /**
     * 验证 MIGRATION_41_42：
     * 1. subscriptions 表 timezone 列默认值从 'UTC' 改为 'Asia/Shanghai'（MAP-B-4）。
     * 2. contacts 表新增 updatedAt 索引（DAO-A-2）。
     */
    @Test
    fun migrate41To42_timezoneDefaultAndContactsUpdatedAtIndex() {
        val db = helper.createDatabase(testDbName, 41)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 42, true, MIGRATION_41_42)

        // 验证 subscriptions.timezone 默认值为 'Asia/Shanghai'
        val subCursor = migratedDb.query("SELECT dflt_value FROM pragma_table_info('subscriptions') WHERE name = 'timezone'")
        assertTrue(subCursor.moveToFirst())
        assertEquals("'Asia/Shanghai'", subCursor.getString(0))
        subCursor.close()

        // 验证 contacts 表有 updatedAt 索引
        val indexCursor = migratedDb.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='contacts' AND name='index_contacts_updatedAt'")
        assertTrue("contacts 表应有 index_contacts_updatedAt 索引", indexCursor.moveToFirst())
        indexCursor.close()

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

    /**
     * 验证 MIGRATION_40_41 创建菜谱功能 4 张新表（recipes/recipe_tags/recipe_contact_cross_ref/recipe_tag_cross_ref）。
     */
    @Test
    fun migrate40To41_recipeTablesCreated() {
        val db = helper.createDatabase(testDbName, 40)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 41, true, MIGRATION_40_41)

        val tablesCursor = migratedDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name LIKE 'recipe%'")
        val tableNames = mutableListOf<String>()
        while (tablesCursor.moveToNext()) {
            tableNames.add(tablesCursor.getString(0))
        }
        tablesCursor.close()

        assertTrue("应创建 recipes 表", "recipes" in tableNames)
        assertTrue("应创建 recipe_tags 表", "recipe_tags" in tableNames)
        assertTrue("应创建 recipe_contact_cross_ref 表", "recipe_contact_cross_ref" in tableNames)
        assertTrue("应创建 recipe_tag_cross_ref 表", "recipe_tag_cross_ref" in tableNames)
        migratedDb.close()
    }

    @Test
    fun allMigrationsCoverGap() {
        val db = helper.createDatabase(testDbName, 1)
        db.close()
        // 如果缺少任何中间迁移，此调用会抛异常
        helper.runMigrationsAndValidate(testDbName, 42, true, *allMigrations)
    }
}
