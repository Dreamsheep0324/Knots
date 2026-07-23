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
        MIGRATION_41_42, MIGRATION_42_43, MIGRATION_43_44, MIGRATION_44_45, MIGRATION_45_46,
        MIGRATION_46_47, MIGRATION_47_48, MIGRATION_48_49, MIGRATION_49_50
    )

    @Test
    fun migrate1To46() {
        val db = helper.createDatabase(testDbName, 1)
        db.execSQL("INSERT INTO contacts (id, name, phone) VALUES (1, '测试联系人', '13800138000')")
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 46, true, *allMigrations)

        val cursor = migratedDb.query("SELECT * FROM contacts WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals("测试联系人", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migrate32To46() {
        val db = helper.createDatabase(testDbName, 32)
        val values = ContentValues().apply {
            put("id", 1L)
            put("name", "测试")
            put("phone", "13800138000")
            put("relationship", "friend")
        }
        db.insert("contacts", SQLiteDatabase.CONFLICT_REPLACE, values)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 46, true,
            MIGRATION_32_33, MIGRATION_33_34, MIGRATION_34_35,
            MIGRATION_35_36, MIGRATION_36_37, MIGRATION_37_38,
            MIGRATION_38_39, MIGRATION_39_40, MIGRATION_40_41, MIGRATION_41_42,
            MIGRATION_42_43, MIGRATION_43_44, MIGRATION_44_45, MIGRATION_45_46
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
    fun migrate35To46_photosCountBackfilled() {
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

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 46, true,
            MIGRATION_35_36, MIGRATION_36_37, MIGRATION_37_38,
            MIGRATION_38_39, MIGRATION_39_40, MIGRATION_40_41, MIGRATION_41_42,
            MIGRATION_42_43, MIGRATION_43_44, MIGRATION_44_45, MIGRATION_45_46
        )

        val cursor = migratedDb.query("SELECT photos_count FROM gifts WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals(2, cursor.getInt(0))
        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migrate36To46_timezoneAdded() {
        val db = helper.createDatabase(testDbName, 36)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 46, true,
            MIGRATION_36_37, MIGRATION_37_38, MIGRATION_38_39, MIGRATION_39_40,
            MIGRATION_40_41, MIGRATION_41_42, MIGRATION_42_43, MIGRATION_43_44,
            MIGRATION_44_45, MIGRATION_45_46
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

    /**
     * 验证 MIGRATION_42_43：
     * 1. anniversaries 表删除 isLunar、isLeapMonth 列。
     * 2. contacts 表删除 isLunarBirthday、isLeapMonthBirthday 列。
     * 3. date 字段保留并保持原值（公历 epoch millis，无转换）。
     * 4. contacts 索引完整重建（intimacyScore、lastInteractionTime、groupId、relationship、name、updatedAt）。
     */
    @Test
    fun migrate42To43_lunarColumnsDropped() {
        val db = helper.createDatabase(testDbName, 42)
        // 先插 contact，再插 anniversary，依赖 contactId 外键
        db.execSQL(
            """
            INSERT INTO contacts (id, name, gender, isLunarBirthday, isLeapMonthBirthday,
                childrenCount, relationshipLevel, intimacyScore, createdAt, updatedAt)
            VALUES (1, '测试联系人', 0, 1, 0, 0, 0, 50, 0, 0)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO anniversaries (id, contactId, name, type, date, isLunar, isLeapMonth,
                isRepeat, reminderDays, createdAt, updatedAt)
            VALUES (1, 1, '生日', 'BIRTHDAY', 1234567890, 1, 0, 1, 1, 0, 0)
            """.trimIndent()
        )
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 43, true, MIGRATION_42_43)

        // 验证 anniversaries 表已无 isLunar / isLeapMonth 列
        val annCursor = migratedDb.query("PRAGMA table_info(anniversaries)")
        val annColumns = mutableListOf<String>()
        while (annCursor.moveToNext()) {
            annColumns.add(annCursor.getString(annCursor.getColumnIndexOrThrow("name")))
        }
        annCursor.close()
        assertFalse("anniversaries 表不应再有 isLunar 列", "isLunar" in annColumns)
        assertFalse("anniversaries 表不应再有 isLeapMonth 列", "isLeapMonth" in annColumns)
        assertTrue("anniversaries 表应保留 date 列", "date" in annColumns)

        // 验证 contacts 表已无 isLunarBirthday / isLeapMonthBirthday 列
        val contactCursor = migratedDb.query("PRAGMA table_info(contacts)")
        val contactColumns = mutableListOf<String>()
        while (contactCursor.moveToNext()) {
            contactColumns.add(contactCursor.getString(contactCursor.getColumnIndexOrThrow("name")))
        }
        contactCursor.close()
        assertFalse("contacts 表不应再有 isLunarBirthday 列", "isLunarBirthday" in contactColumns)
        assertFalse("contacts 表不应再有 isLeapMonthBirthday 列", "isLeapMonthBirthday" in contactColumns)

        // 验证 date 数据保留（不转换）
        val dateCursor = migratedDb.query("SELECT date FROM anniversaries WHERE id = 1")
        assertTrue(dateCursor.moveToFirst())
        assertEquals(1234567890L, dateCursor.getLong(0))
        dateCursor.close()

        // 验证 contacts 6 个索引全部存在
        val indexCursor = migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='contacts'"
        )
        val indexNames = mutableListOf<String>()
        while (indexCursor.moveToNext()) {
            indexNames.add(indexCursor.getString(0))
        }
        indexCursor.close()
        listOf(
            "index_contacts_intimacyScore",
            "index_contacts_lastInteractionTime",
            "index_contacts_groupId",
            "index_contacts_relationship",
            "index_contacts_name",
            "index_contacts_updatedAt"
        ).forEach {
            assertTrue("contacts 表应有 $it 索引", it in indexNames)
        }

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
        helper.runMigrationsAndValidate(testDbName, 50, true, *allMigrations)
    }

    /**
     * 验证 MIGRATION_43_44：
     * 1. events 表删除 amount 列。
     * 2. gifts 表删除 amount 列。
     * 3. 其余列数据保留（events.title / gifts.giftName 不丢失）。
     * 4. events/gifts 索引完整重建（events: time/type；gifts: contactId/date）。
     */
    @Test
    fun migrate43To44_amountColumnsDropped() {
        val db = helper.createDatabase(testDbName, 43)
        // 先插 contact 满足 gifts 的 contactId 外键
        db.execSQL(
            """
            INSERT INTO contacts (id, name, gender, childrenCount, relationshipLevel,
                intimacyScore, createdAt, updatedAt)
            VALUES (1, '测试联系人', 0, 0, 0, 50, 0, 0)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO events (id, type, title, time, photos, photos_count, amount,
                emotion, weather, remarks, createdAt, updatedAt)
            VALUES (1, 'MEETING', '测试事件', 1234567890, '[]', 0, 99.5,
                'happy', 'sunny', '备注', 0, 0)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO gifts (id, contactId, giftName, giftType, date, isSent, amount,
                occasion, photos, photos_count, createdAt, updatedAt)
            VALUES (1, 1, '测试礼物', 'GENERAL', 1234567890, 0, 88.8,
                '生日', '[]', 0, 0, 0)
            """.trimIndent()
        )
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 44, true, MIGRATION_43_44)

        // 验证 events 表已无 amount 列
        val eventCursor = migratedDb.query("PRAGMA table_info(events)")
        val eventColumns = mutableListOf<String>()
        while (eventCursor.moveToNext()) {
            eventColumns.add(eventCursor.getString(eventCursor.getColumnIndexOrThrow("name")))
        }
        eventCursor.close()
        assertFalse("events 表不应再有 amount 列", "amount" in eventColumns)
        assertTrue("events 表应保留 title 列", "title" in eventColumns)
        assertTrue("events 表应保留 remarks 列", "remarks" in eventColumns)

        // 验证 gifts 表已无 amount 列
        val giftCursor = migratedDb.query("PRAGMA table_info(gifts)")
        val giftColumns = mutableListOf<String>()
        while (giftCursor.moveToNext()) {
            giftColumns.add(giftCursor.getString(giftCursor.getColumnIndexOrThrow("name")))
        }
        giftCursor.close()
        assertFalse("gifts 表不应再有 amount 列", "amount" in giftColumns)
        assertTrue("gifts 表应保留 occasion 列", "occasion" in giftColumns)

        // 验证非 amount 数据保留
        val eventTitleCursor = migratedDb.query("SELECT title, remarks FROM events WHERE id = 1")
        assertTrue(eventTitleCursor.moveToFirst())
        assertEquals("测试事件", eventTitleCursor.getString(0))
        assertEquals("备注", eventTitleCursor.getString(1))
        eventTitleCursor.close()

        val giftNameCursor = migratedDb.query("SELECT giftName, occasion FROM gifts WHERE id = 1")
        assertTrue(giftNameCursor.moveToFirst())
        assertEquals("测试礼物", giftNameCursor.getString(0))
        assertEquals("生日", giftNameCursor.getString(1))
        giftNameCursor.close()

        // 验证索引完整重建
        val eventIndexCursor = migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='events'"
        )
        val eventIndexNames = mutableListOf<String>()
        while (eventIndexCursor.moveToNext()) {
            eventIndexNames.add(eventIndexCursor.getString(0))
        }
        eventIndexCursor.close()
        listOf("index_events_time", "index_events_type").forEach {
            assertTrue("events 表应有 $it 索引", it in eventIndexNames)
        }

        val giftIndexCursor = migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='gifts'"
        )
        val giftIndexNames = mutableListOf<String>()
        while (giftIndexCursor.moveToNext()) {
            giftIndexNames.add(giftIndexCursor.getString(0))
        }
        giftIndexCursor.close()
        listOf("index_gifts_contactId", "index_gifts_date").forEach {
            assertTrue("gifts 表应有 $it 索引", it in giftIndexNames)
        }

        migratedDb.close()
    }

    /**
     * 验证 MIGRATION_44_45：
     * 1. contacts 表删除 relationshipLevel 列。
     * 2. 其余列数据保留（name / intimacyScore / relationship 不丢失）。
     * 3. 6 个索引完整重建（intimacyScore/lastInteractionTime/groupId/relationship/name/updatedAt）。
     */
    @Test
    fun migrate44To45_relationshipLevelDropped() {
        val db = helper.createDatabase(testDbName, 44)
        db.execSQL(
            """
            INSERT INTO contacts (id, name, gender, childrenCount, relationshipLevel,
                intimacyScore, relationship, createdAt, updatedAt)
            VALUES (1, '测试联系人', 0, 0, 7, 80, '朋友', 0, 0)
            """.trimIndent()
        )
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 45, true, MIGRATION_44_45)

        // 验证 contacts 表已无 relationshipLevel 列
        val contactCursor = migratedDb.query("PRAGMA table_info(contacts)")
        val contactColumns = mutableListOf<String>()
        while (contactCursor.moveToNext()) {
            contactColumns.add(contactCursor.getString(contactCursor.getColumnIndexOrThrow("name")))
        }
        contactCursor.close()
        assertFalse("contacts 表不应再有 relationshipLevel 列", "relationshipLevel" in contactColumns)
        assertTrue("contacts 表应保留 name 列", "name" in contactColumns)
        assertTrue("contacts 表应保留 intimacyScore 列", "intimacyScore" in contactColumns)
        assertTrue("contacts 表应保留 relationship 列", "relationship" in contactColumns)

        // 验证非 relationshipLevel 数据保留
        val dataCursor = migratedDb.query("SELECT name, intimacyScore, relationship FROM contacts WHERE id = 1")
        assertTrue(dataCursor.moveToFirst())
        assertEquals("测试联系人", dataCursor.getString(0))
        assertEquals(80, dataCursor.getInt(1))
        assertEquals("朋友", dataCursor.getString(2))
        dataCursor.close()

        // 验证 6 个索引完整重建
        val indexCursor = migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='contacts'"
        )
        val indexNames = mutableListOf<String>()
        while (indexCursor.moveToNext()) {
            indexNames.add(indexCursor.getString(0))
        }
        indexCursor.close()
        listOf(
            "index_contacts_intimacyScore",
            "index_contacts_lastInteractionTime",
            "index_contacts_groupId",
            "index_contacts_relationship",
            "index_contacts_name",
            "index_contacts_updatedAt"
        ).forEach {
            assertTrue("contacts 表应有 $it 索引", it in indexNames)
        }

        migratedDb.close()
    }

    /**
     * 验证 MIGRATION_45_46（FF8）：
     * 1. 新建 contact_relations 表，schema 与 ContactRelationEntity 一致。
     * 2. 创建 3 个索引：唯一索引 (contactIdA, contactIdB) + contactIdB + relationTypeId。
     * 3. 3 个外键均 ON DELETE CASCADE。
     * 4. custom_types 表中插入 6 个预设关系类型（家人/伴侣/朋友/同事/同学/其他）。
     * 5. 幂等性：若 RELATIONSHIP 类别已有数据，不重复插入。
     */
    @Test
    fun migrate45To46_contactRelationsAndSeedData() {
        val db = helper.createDatabase(testDbName, 45)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 46, true, MIGRATION_45_46)

        // 1. 验证 contact_relations 表已创建
        val tableCursor = migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='contact_relations'"
        )
        assertTrue("应创建 contact_relations 表", tableCursor.moveToFirst())
        tableCursor.close()

        // 2. 验证表结构（字段名 + 类型 + NOT NULL 约束）
        val columnCursor = migratedDb.query("PRAGMA table_info(contact_relations)")
        val columns = mutableMapOf<String, String>()
        while (columnCursor.moveToNext()) {
            val name = columnCursor.getString(columnCursor.getColumnIndexOrThrow("name"))
            val type = columnCursor.getString(columnCursor.getColumnIndexOrThrow("type"))
            columns[name] = type
        }
        columnCursor.close()
        assertEquals("INTEGER", columns["id"])
        assertEquals("INTEGER", columns["contactIdA"])
        assertEquals("INTEGER", columns["contactIdB"])
        assertEquals("INTEGER", columns["relationTypeId"])
        assertEquals("TEXT", columns["note"])
        assertEquals("TEXT", columns["source"])
        assertEquals("INTEGER", columns["createdAt"])
        assertEquals("INTEGER", columns["updatedAt"])

        // 3. 验证 3 个索引
        val indexCursor = migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='contact_relations'"
        )
        val indexNames = mutableListOf<String>()
        while (indexCursor.moveToNext()) {
            indexNames.add(indexCursor.getString(0))
        }
        indexCursor.close()
        assertTrue("应有唯一索引 (contactIdA, contactIdB)", "index_contact_relations_contactIdA_contactIdB" in indexNames)
        assertTrue("应有 contactIdB 索引", "index_contact_relations_contactIdB" in indexNames)
        assertTrue("应有 relationTypeId 索引", "index_contact_relations_relationTypeId" in indexNames)

        // 4. 验证外键 ON DELETE CASCADE
        val fkCursor = migratedDb.query("PRAGMA foreign_key_list(contact_relations)")
        val fkActions = mutableListOf<String>()
        while (fkCursor.moveToNext()) {
            // PRAGMA foreign_key_list 列序：id, seq, table, from, to, on_update, on_delete, match
            val onDelete = fkCursor.getString(fkCursor.getColumnIndexOrThrow("on_delete"))
            fkActions.add(onDelete)
        }
        fkCursor.close()
        assertEquals("应有 3 个外键", 3, fkActions.size)
        assertTrue("所有外键应 ON DELETE CASCADE", fkActions.all { it == "CASCADE" })

        // 5. 验证 6 个预设关系类型种子数据
        val seedCursor = migratedDb.query(
            "SELECT name, color, icon FROM custom_types WHERE category = 'RELATIONSHIP' ORDER BY sortOrder ASC"
        )
        val presets = mutableListOf<Triple<String, String, String>>()
        while (seedCursor.moveToNext()) {
            presets.add(
                Triple(
                    seedCursor.getString(0),
                    seedCursor.getString(1),
                    seedCursor.getString(2)
                )
            )
        }
        seedCursor.close()
        assertEquals("应插入 6 个预设关系类型", 6, presets.size)
        assertEquals("家人", presets[0].first)
        assertEquals("#ef4444", presets[0].second)
        assertEquals("家", presets[0].third)
        assertEquals("伴侣", presets[1].first)
        assertEquals("#ec4899", presets[1].second)
        assertEquals("侣", presets[1].third)
        assertEquals("朋友", presets[2].first)
        assertEquals("#8b5cf6", presets[2].second)
        assertEquals("友", presets[2].third)
        assertEquals("同事", presets[3].first)
        assertEquals("#10b981", presets[3].second)
        assertEquals("事", presets[3].third)
        assertEquals("同学", presets[4].first)
        assertEquals("#f59e0b", presets[4].second)
        assertEquals("学", presets[4].third)
        assertEquals("其他", presets[5].first)
        assertEquals("#6b7280", presets[5].second)
        assertEquals("他", presets[5].third)

        migratedDb.close()
    }

    /**
     * 验证 MIGRATION_45_46 的幂等性：若 RELATIONSHIP 类别已存在数据，不重复插入种子。
     *
     * 模拟场景：用户在 v45 之前已有自定义 RELATIONSHIP 类型（虽然 v45 之前并无此 category），
     * 实际上 v45 schema 不含 RELATIONSHIP 类别，此测试主要保证未来若用户手动添加了
     * RELATIONSHIP 类型后再次升级不会重复。
     */
    @Test
    fun migrate45To46_seedDataIdempotent() {
        val db = helper.createDatabase(testDbName, 45)
        // 预插入一条 RELATIONSHIP 类型，模拟已有数据
        db.execSQL(
            """
            INSERT INTO custom_types (category, name, key, color, icon, sortOrder, isDefault, createdAt)
            VALUES ('RELATIONSHIP', '旧友', '旧友', '#aaaaaa', '旧', 0, 0, 0)
            """.trimIndent()
        )
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 46, true, MIGRATION_45_46)

        val cursor = migratedDb.query(
            "SELECT COUNT(*) FROM custom_types WHERE category = 'RELATIONSHIP'"
        )
        assertTrue(cursor.moveToFirst())
        // 已有 1 条，迁移不应再插入 6 条预设，总数应为 1
        assertEquals("幂等：已有 RELATIONSHIP 数据时不重复插入种子", 1, cursor.getInt(0))
        cursor.close()
        migratedDb.close()
    }

    /**
     * 验证 MIGRATION_46_47：
     * 1. 新建 person_relations 表，schema 与 PersonRelationEntity 一致。
     * 2. 创建 3 个索引：ownerContactId + targetContactId + relationTypeId。
     * 3. 3 个外键：ownerContactId CASCADE / targetContactId SET NULL / relationTypeId SET NULL。
     */
    @Test
    fun migrate46To47_personRelationsCreated() {
        val db = helper.createDatabase(testDbName, 46)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 47, true, MIGRATION_46_47)

        // 1. 验证 person_relations 表已创建
        val tableCursor = migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='person_relations'"
        )
        assertTrue("应创建 person_relations 表", tableCursor.moveToFirst())
        tableCursor.close()

        // 2. 验证表结构（字段名 + 类型 + NOT NULL 约束）
        val columnCursor = migratedDb.query("PRAGMA table_info(person_relations)")
        val columns = mutableMapOf<String, String>()
        while (columnCursor.moveToNext()) {
            val name = columnCursor.getString(columnCursor.getColumnIndexOrThrow("name"))
            val type = columnCursor.getString(columnCursor.getColumnIndexOrThrow("type"))
            columns[name] = type
        }
        columnCursor.close()
        assertEquals("INTEGER", columns["id"])
        assertEquals("INTEGER", columns["ownerContactId"])
        assertEquals("INTEGER", columns["targetContactId"])
        assertEquals("TEXT", columns["targetName"])
        assertEquals("TEXT", columns["targetAvatar"])
        assertEquals("INTEGER", columns["relationTypeId"])
        assertEquals("TEXT", columns["customLabel"])
        assertEquals("TEXT", columns["note"])
        assertEquals("INTEGER", columns["createdAt"])
        assertEquals("INTEGER", columns["updatedAt"])

        // 3. 验证 3 个索引
        val indexCursor = migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='person_relations'"
        )
        val indexNames = mutableListOf<String>()
        while (indexCursor.moveToNext()) {
            indexNames.add(indexCursor.getString(0))
        }
        indexCursor.close()
        assertTrue("应有 ownerContactId 索引", "index_person_relations_ownerContactId" in indexNames)
        assertTrue("应有 targetContactId 索引", "index_person_relations_targetContactId" in indexNames)
        assertTrue("应有 relationTypeId 索引", "index_person_relations_relationTypeId" in indexNames)

        // 4. 验证外键 ON DELETE 行为
        val fkCursor = migratedDb.query("PRAGMA foreign_key_list(person_relations)")
        val fkPairs = mutableListOf<Pair<String, String>>() // (from, on_delete)
        while (fkCursor.moveToNext()) {
            val from = fkCursor.getString(fkCursor.getColumnIndexOrThrow("from"))
            val onDelete = fkCursor.getString(fkCursor.getColumnIndexOrThrow("on_delete"))
            fkPairs.add(from to onDelete)
        }
        fkCursor.close()
        assertEquals("应有 3 个外键", 3, fkPairs.size)
        assertTrue("ownerContactId 应 CASCADE",
            fkPairs.any { it.first == "ownerContactId" && it.second == "CASCADE" })
        assertTrue("targetContactId 应 SET NULL",
            fkPairs.any { it.first == "targetContactId" && it.second == "SET NULL" })
        assertTrue("relationTypeId 应 SET NULL",
            fkPairs.any { it.first == "relationTypeId" && it.second == "SET NULL" })

        migratedDb.close()
    }

    /**
     * 验证 MIGRATION_47_48：
     * 清理 isDefault=1 的 PERSON_RELATION 类型，保留用户自建（isDefault=0）的类型。
     * 非 PERSON_RELATION 类别不受影响。
     */
    @Test
    fun migrate47To48_personRelationDefaultsCleaned() {
        val db = helper.createDatabase(testDbName, 47)
        // 插入 1 条 isDefault=1 的 PERSON_RELATION（应被清理）
        db.execSQL(
            """
            INSERT INTO custom_types (category, name, key, color, icon, sortOrder, isDefault, createdAt)
            VALUES ('PERSON_RELATION', '预设关系', 'preset', '#aaaaaa', '预', 0, 1, 0)
            """.trimIndent()
        )
        // 插入 1 条 isDefault=0 的 PERSON_RELATION（应保留）
        db.execSQL(
            """
            INSERT INTO custom_types (category, name, key, color, icon, sortOrder, isDefault, createdAt)
            VALUES ('PERSON_RELATION', '自建关系', 'custom', '#bbbbbb', '自', 1, 0, 0)
            """.trimIndent()
        )
        // 插入 1 条 RELATIONSHIP 类别（应保留，不受影响）
        db.execSQL(
            """
            INSERT INTO custom_types (category, name, key, color, icon, sortOrder, isDefault, createdAt)
            VALUES ('RELATIONSHIP', '家人', 'family', '#ef4444', '家', 0, 1, 0)
            """.trimIndent()
        )
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 48, true, MIGRATION_47_48)

        // 验证 isDefault=1 的 PERSON_RELATION 已删除
        val defaultCursor = migratedDb.query(
            "SELECT COUNT(*) FROM custom_types WHERE category = 'PERSON_RELATION' AND isDefault = 1"
        )
        assertTrue(defaultCursor.moveToFirst())
        assertEquals("isDefault=1 的 PERSON_RELATION 应被清理", 0, defaultCursor.getInt(0))
        defaultCursor.close()

        // 验证 isDefault=0 的 PERSON_RELATION 保留
        val customCursor = migratedDb.query(
            "SELECT name FROM custom_types WHERE category = 'PERSON_RELATION' AND isDefault = 0"
        )
        assertTrue(customCursor.moveToFirst())
        assertEquals("自建关系", customCursor.getString(0))
        customCursor.close()

        // 验证非 PERSON_RELATION 类别不受影响
        val otherCursor = migratedDb.query(
            "SELECT COUNT(*) FROM custom_types WHERE category != 'PERSON_RELATION'"
        )
        assertTrue(otherCursor.moveToFirst())
        assertEquals("非 PERSON_RELATION 类别应保留", 1, otherCursor.getInt(0))
        otherCursor.close()

        migratedDb.close()
    }

    /**
     * 验证 MIGRATION_48_49：
     * 清理 contact_relations 中 source 为 AUTO_CIRCLE/AUTO_EVENT 的记录，保留 MANUAL。
     */
    @Test
    fun migrate48To49_autoRelationsCleaned() {
        val db = helper.createDatabase(testDbName, 48)
        // 先插 contacts 满足外键
        db.execSQL(
            """
            INSERT INTO contacts (id, name, gender, childrenCount, intimacyScore, createdAt, updatedAt)
            VALUES (1, 'A', 0, 0, 50, 0, 0)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO contacts (id, name, gender, childrenCount, intimacyScore, createdAt, updatedAt)
            VALUES (2, 'B', 0, 0, 50, 0, 0)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO contacts (id, name, gender, childrenCount, intimacyScore, createdAt, updatedAt)
            VALUES (3, 'C', 0, 0, 50, 0, 0)
            """.trimIndent()
        )
        // 插入 relationType 满足外键
        db.execSQL(
            """
            INSERT INTO custom_types (id, category, name, key, sortOrder, isDefault, createdAt)
            VALUES (1, 'RELATIONSHIP', '朋友', 'friend', 0, 1, 0)
            """.trimIndent()
        )
        // 插入 AUTO_CIRCLE 记录（应删除）
        db.execSQL(
            """
            INSERT INTO contact_relations (id, contactIdA, contactIdB, relationTypeId, source, note, createdAt, updatedAt)
            VALUES (1, 1, 2, 1, 'AUTO_CIRCLE', '自动', 0, 0)
            """.trimIndent()
        )
        // 插入 AUTO_EVENT 记录（应删除）
        db.execSQL(
            """
            INSERT INTO contact_relations (id, contactIdA, contactIdB, relationTypeId, source, note, createdAt, updatedAt)
            VALUES (2, 1, 3, 1, 'AUTO_EVENT', '自动', 0, 0)
            """.trimIndent()
        )
        // 插入 MANUAL 记录（应保留）
        db.execSQL(
            """
            INSERT INTO contact_relations (id, contactIdA, contactIdB, relationTypeId, source, note, createdAt, updatedAt)
            VALUES (3, 2, 3, 1, 'MANUAL', '手动', 0, 0)
            """.trimIndent()
        )
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 49, true, MIGRATION_48_49)

        // 验证 AUTO_* 已删除，仅剩 1 条 MANUAL
        val cursor = migratedDb.query(
            "SELECT id, source, note FROM contact_relations ORDER BY id ASC"
        )
        assertTrue(cursor.moveToFirst())
        assertEquals("仅剩 1 条 MANUAL 记录", 1, cursor.count)
        assertEquals(3L, cursor.getLong(0))
        assertEquals("MANUAL", cursor.getString(1))
        assertEquals("手动", cursor.getString(2))
        cursor.close()

        migratedDb.close()
    }

    /**
     * 验证 MIGRATION_49_50：
     * 1. contact_relations 表删除 source 列（CREATE+COPY+DROP+RENAME）。
     * 2. 数据保留：id/contactIdA/contactIdB/relationTypeId/note/createdAt/updatedAt 不丢失。
     * 3. 3 个索引完整重建。
     */
    @Test
    fun migrate49To50_sourceColumnDropped() {
        val db = helper.createDatabase(testDbName, 49)
        // 先插 contacts 满足外键
        db.execSQL(
            """
            INSERT INTO contacts (id, name, gender, childrenCount, intimacyScore, createdAt, updatedAt)
            VALUES (1, 'A', 0, 0, 50, 0, 0)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO contacts (id, name, gender, childrenCount, intimacyScore, createdAt, updatedAt)
            VALUES (2, 'B', 0, 0, 50, 0, 0)
            """.trimIndent()
        )
        // 插入 relationType 满足外键
        db.execSQL(
            """
            INSERT INTO custom_types (id, category, name, key, sortOrder, isDefault, createdAt)
            VALUES (1, 'RELATIONSHIP', '朋友', 'friend', 0, 1, 0)
            """.trimIndent()
        )
        // 插入 1 条 MANUAL 记录
        db.execSQL(
            """
            INSERT INTO contact_relations (id, contactIdA, contactIdB, relationTypeId, source, note, createdAt, updatedAt)
            VALUES (1, 1, 2, 1, 'MANUAL', '测试备注', 111, 222)
            """.trimIndent()
        )
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(testDbName, 50, true, MIGRATION_49_50)

        // 1. 验证 source 列已删除
        val columnCursor = migratedDb.query("PRAGMA table_info(contact_relations)")
        val columnNames = mutableListOf<String>()
        while (columnCursor.moveToNext()) {
            columnNames.add(columnCursor.getString(columnCursor.getColumnIndexOrThrow("name")))
        }
        columnCursor.close()
        assertFalse("contact_relations 不应再有 source 列", "source" in columnNames)
        assertTrue("应保留 id 列", "id" in columnNames)
        assertTrue("应保留 note 列", "note" in columnNames)

        // 2. 验证数据保留
        val dataCursor = migratedDb.query(
            "SELECT id, contactIdA, contactIdB, relationTypeId, note, createdAt, updatedAt FROM contact_relations WHERE id = 1"
        )
        assertTrue(dataCursor.moveToFirst())
        assertEquals(1L, dataCursor.getLong(0))
        assertEquals(1L, dataCursor.getLong(1))
        assertEquals(2L, dataCursor.getLong(2))
        assertEquals(1L, dataCursor.getLong(3))
        assertEquals("测试备注", dataCursor.getString(4))
        assertEquals(111L, dataCursor.getLong(5))
        assertEquals(222L, dataCursor.getLong(6))
        dataCursor.close()

        // 3. 验证 3 个索引完整重建
        val indexCursor = migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='contact_relations'"
        )
        val indexNames = mutableListOf<String>()
        while (indexCursor.moveToNext()) {
            indexNames.add(indexCursor.getString(0))
        }
        indexCursor.close()
        listOf(
            "index_contact_relations_contactIdA_contactIdB",
            "index_contact_relations_contactIdB",
            "index_contact_relations_relationTypeId"
        ).forEach {
            assertTrue("contact_relations 表应有 $it 索引", it in indexNames)
        }

        migratedDb.close()
    }
}
