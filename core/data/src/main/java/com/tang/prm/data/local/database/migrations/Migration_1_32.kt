package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 统一迁移快捷路径：v1 → v32
 *
 * 合并了原始 MIGRATION_1_24 和 MIGRATION_24_31 的全部操作，一步到位。
 * 新安装用户直接创建 v32 数据库，无需执行任何迁移。
 *
 * 优化点（继承自原始合并迁移）：
 * - gifts 表直接以最终 schema（含外键）创建，跳过中间重建
 * - event_participants 表直接以最终 schema（含外键）重建
 * - thoughts 表直接以最终 schema 创建，包含所有列
 * - circles 表直接以最终 schema 创建，包含所有列
 * - circle_member_cross_ref 直接以最终 schema 创建（复合主键），跳过中间版本
 * - divination_records 直接以最终 schema 创建（含 aiAnalysis 列）
 * - anniversaries 直接以最终 schema 创建（含外键）
 * - contacts 表一次性添加所有新列
 * - custom_types 表一次性添加所有新列
 */
val MIGRATION_1_32 = object : Migration(1, 32) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ===== v1→v10 =====

        db.execSQL("ALTER TABLE custom_types ADD COLUMN icon TEXT")
        db.execSQL("ALTER TABLE anniversaries ADD COLUMN icon TEXT")

        db.execSQL("CREATE TABLE events_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "title TEXT NOT NULL," +
                "start_time INTEGER NOT NULL," +
                "end_time INTEGER NOT NULL," +
                "location TEXT," +
                "description TEXT," +
                "is_all_day INTEGER NOT NULL," +
                "remind_time INTEGER," +
                "is_repeat INTEGER NOT NULL," +
                "repeat_rule TEXT," +
                "color TEXT," +
                "created_at INTEGER NOT NULL," +
                "updated_at INTEGER NOT NULL" +
                ")")
        db.execSQL("INSERT INTO events_new (id, title, start_time, end_time, location, description, is_all_day, remind_time, is_repeat, repeat_rule, color, created_at, updated_at) " +
                "SELECT id, title, start_time, end_time, location, description, is_all_day, remind_time, is_repeat, repeat_rule, color, created_at, updated_at FROM events")
        db.execSQL("DROP TABLE events")
        db.execSQL("ALTER TABLE events_new RENAME TO events")
        db.execSQL("ALTER TABLE events ADD COLUMN weather TEXT")

        db.execSQL("DELETE FROM custom_types WHERE category IN ('EVENT_TYPE', 'EMOTION', 'WEATHER')")

        db.execSQL("ALTER TABLE contacts ADD COLUMN relationship TEXT")
        db.execSQL("DELETE FROM custom_types WHERE category = 'RELATIONSHIP'")
        db.execSQL("ALTER TABLE contacts ADD COLUMN city TEXT")
        db.execSQL("ALTER TABLE contacts ADD COLUMN education TEXT")

        // ===== v10→v20 =====

        db.execSQL("CREATE TABLE IF NOT EXISTS gifts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "contactId INTEGER NOT NULL," +
                "giftName TEXT NOT NULL," +
                "giftType TEXT NOT NULL," +
                "date INTEGER NOT NULL," +
                "isSent INTEGER NOT NULL," +
                "amount REAL," +
                "occasion TEXT," +
                "description TEXT," +
                "location TEXT," +
                "photos TEXT NOT NULL," +
                "createdAt INTEGER NOT NULL," +
                "updatedAt INTEGER NOT NULL," +
                "FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE" +
                ")")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_gifts_contactId ON gifts(contactId)")

        db.execSQL("CREATE TABLE IF NOT EXISTS thoughts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "contactId INTEGER," +
                "content TEXT NOT NULL," +
                "mood TEXT," +
                "type TEXT NOT NULL DEFAULT 'murmur'," +
                "isPrivate INTEGER NOT NULL," +
                "isTodo INTEGER NOT NULL DEFAULT 0," +
                "isDone INTEGER NOT NULL DEFAULT 0," +
                "dueDate INTEGER," +
                "category TEXT," +
                "createdAt INTEGER NOT NULL," +
                "updatedAt INTEGER NOT NULL," +
                "FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE SET NULL" +
                ")")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_thoughts_contactId ON thoughts(contactId)")

        db.execSQL("ALTER TABLE contacts ADD COLUMN knowingDate INTEGER")

        db.execSQL("CREATE TABLE IF NOT EXISTS circles (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "name TEXT NOT NULL," +
                "description TEXT," +
                "color TEXT NOT NULL DEFAULT '#6366F1'," +
                "icon TEXT NOT NULL DEFAULT 'people'," +
                "waveform TEXT NOT NULL DEFAULT 'sine'," +
                "memberIds TEXT NOT NULL DEFAULT ''," +
                "parentCircleId INTEGER," +
                "intimacyThreshold INTEGER NOT NULL DEFAULT 0," +
                "sortOrder INTEGER NOT NULL DEFAULT 0," +
                "createdAt INTEGER NOT NULL," +
                "updatedAt INTEGER NOT NULL" +
                ")")

        db.execSQL("DELETE FROM custom_types WHERE category = 'EVENT_TYPE' AND isDefault = 1")

        db.execSQL("UPDATE thoughts SET type = CASE WHEN contactId IS NOT NULL THEN 'friend' WHEN isPrivate = 1 THEN 'private' ELSE 'murmur' END")

        db.execSQL("CREATE TABLE IF NOT EXISTS favorites (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "sourceType TEXT NOT NULL," +
                "sourceId INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "createdAt INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_favorites_sourceType_sourceId ON favorites(sourceType, sourceId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_favorites_createdAt ON favorites(createdAt)")

        db.execSQL("UPDATE favorites SET title = REPLACE(title, CHAR(55357) || CHAR(56825) || ' ', '') WHERE sourceType = 'PHOTO'")

        // ===== v20→v24 =====

        db.execSQL("DELETE FROM event_participants WHERE contactId NOT IN (SELECT id FROM contacts)")
        db.execSQL("DELETE FROM event_participants WHERE eventId NOT IN (SELECT id FROM events)")
        db.execSQL("CREATE TABLE event_participants_new (eventId INTEGER NOT NULL, contactId INTEGER NOT NULL, PRIMARY KEY(eventId, contactId), FOREIGN KEY(eventId) REFERENCES events(id) ON DELETE CASCADE, FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE)")
        db.execSQL("INSERT INTO event_participants_new SELECT * FROM event_participants")
        db.execSQL("DROP TABLE event_participants")
        db.execSQL("ALTER TABLE event_participants_new RENAME TO event_participants")
        db.execSQL("CREATE INDEX index_event_participants_contactId ON event_participants(contactId)")

        db.execSQL("DELETE FROM gifts WHERE contactId NOT IN (SELECT id FROM contacts)")

        db.execSQL("ALTER TABLE custom_types ADD COLUMN `key` TEXT NOT NULL DEFAULT ''")

        db.execSQL("UPDATE custom_types SET `key` = 'MEETUP' WHERE category = 'EVENT_TYPE' AND name = '见面' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'DINING' WHERE category = 'EVENT_TYPE' AND name = '聚餐' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'TRAVEL' WHERE category = 'EVENT_TYPE' AND name = '旅游' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'CALL' WHERE category = 'EVENT_TYPE' AND name = '通话' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'GIFT_SENT' WHERE category = 'EVENT_TYPE' AND name = '送礼' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'GIFT_RECEIVED' WHERE category = 'EVENT_TYPE' AND name = '收礼' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'MONEY_LEND' WHERE category = 'EVENT_TYPE' AND name = '借出' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'MONEY_BORROW' WHERE category = 'EVENT_TYPE' AND name = '借入' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'CONVERSATION' WHERE category = 'EVENT_TYPE' AND name = '对话记录' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'OTHER' WHERE category = 'EVENT_TYPE' AND name = '其他' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'HAPPY' WHERE category = 'EMOTION' AND name = '开心' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'GRATEFUL' WHERE category = 'EMOTION' AND name = '感恩' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'CALM' WHERE category = 'EMOTION' AND name = '平静' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'EXCITED' WHERE category = 'EMOTION' AND name = '激动' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'SAD' WHERE category = 'EMOTION' AND name = '难过' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'ANGRY' WHERE category = 'EMOTION' AND name = '生气' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'ANXIOUS' WHERE category = 'EMOTION' AND name = '焦虑' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'SUNNY' WHERE category = 'WEATHER' AND name = '晴天' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'CLOUDY' WHERE category = 'WEATHER' AND name = '多云' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'RAINY' WHERE category = 'WEATHER' AND name = '雨天' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'SNOWY' WHERE category = 'WEATHER' AND name = '雪天' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'BIRTHDAY' WHERE category = 'ANNIVERSARY_TYPE' AND name = '生日' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'ANNIVERSARY' WHERE category = 'ANNIVERSARY_TYPE' AND name = '纪念日' AND `key` = ''")
        db.execSQL("UPDATE custom_types SET `key` = 'HOLIDAY' WHERE category = 'ANNIVERSARY_TYPE' AND name = '节日' AND `key` = ''")

        // ===== v24→v25: circle_member_cross_ref + circles 去 memberIds =====

        db.execSQL("CREATE TABLE IF NOT EXISTS circle_member_cross_ref (" +
                "circleId INTEGER NOT NULL," +
                "contactId INTEGER NOT NULL," +
                "PRIMARY KEY(circleId, contactId)," +
                "FOREIGN KEY(circleId) REFERENCES circles(id) ON DELETE CASCADE," +
                "FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_circle_member_cross_ref_circleId ON circle_member_cross_ref(circleId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_circle_member_cross_ref_contactId ON circle_member_cross_ref(contactId)")

        val cursor = db.query("SELECT id, memberIds FROM circles WHERE memberIds IS NOT NULL AND memberIds != ''")
        if (cursor.moveToFirst()) {
            do {
                val circleId = cursor.getLong(cursor.getColumnIndexOrThrow("id"))
                val memberIdsStr = cursor.getString(cursor.getColumnIndexOrThrow("memberIds"))
                if (memberIdsStr != null) {
                    memberIdsStr.split(",").mapNotNull { it.trim().toLongOrNull() }.forEach { contactId ->
                        db.execSQL("INSERT OR IGNORE INTO circle_member_cross_ref (circleId, contactId) VALUES (?, ?)", arrayOf(circleId, contactId))
                    }
                }
            } while (cursor.moveToNext())
        }
        cursor.close()

        db.execSQL("CREATE TABLE circles_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, description TEXT, color TEXT NOT NULL DEFAULT '#6366F1', icon TEXT NOT NULL DEFAULT 'people', waveform TEXT NOT NULL DEFAULT 'sine', parentCircleId INTEGER, intimacyThreshold INTEGER NOT NULL DEFAULT 0, sortOrder INTEGER NOT NULL DEFAULT 0, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
        db.execSQL("INSERT INTO circles_new SELECT id, name, description, color, icon, waveform, parentCircleId, intimacyThreshold, sortOrder, createdAt, updatedAt FROM circles")
        db.execSQL("DROP TABLE circles")
        db.execSQL("ALTER TABLE circles_new RENAME TO circles")

        // ===== v25→v26: favorites 唯一索引 =====

        db.execSQL("DELETE FROM favorites WHERE id NOT IN (SELECT MIN(id) FROM favorites GROUP BY sourceType, sourceId)")
        db.execSQL("CREATE TABLE favorites_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, sourceType TEXT NOT NULL, sourceId INTEGER NOT NULL, title TEXT NOT NULL, description TEXT, createdAt INTEGER NOT NULL)")
        db.execSQL("INSERT INTO favorites_new SELECT * FROM favorites")
        db.execSQL("DROP TABLE favorites")
        db.execSQL("ALTER TABLE favorites_new RENAME TO favorites")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_favorites_sourceType_sourceId ON favorites(sourceType, sourceId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_favorites_createdAt ON favorites(createdAt)")

        // ===== v26→v28: divination_records =====

        db.execSQL("CREATE TABLE IF NOT EXISTS divination_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "method TEXT NOT NULL," +
                "question TEXT NOT NULL," +
                "resultJson TEXT NOT NULL," +
                "aiAnalysis TEXT NOT NULL DEFAULT ''," +
                "createdAt INTEGER NOT NULL" +
                ")")

        // ===== v29→v30: anniversaries 外键 =====

        db.execSQL("CREATE TABLE IF NOT EXISTS `anniversaries_tmp` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `contactId` INTEGER, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `date` INTEGER NOT NULL, `isLunar` INTEGER NOT NULL, `isRepeat` INTEGER NOT NULL, `reminderDays` INTEGER NOT NULL, `remarks` TEXT, `icon` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, FOREIGN KEY(`contactId`) REFERENCES `contacts`(`id`) ON DELETE CASCADE)")
        db.execSQL("INSERT INTO `anniversaries_tmp` SELECT * FROM `anniversaries`")
        db.execSQL("DROP TABLE `anniversaries`")
        db.execSQL("ALTER TABLE `anniversaries_tmp` RENAME TO `anniversaries`")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_anniversaries_contactId` ON `anniversaries` (`contactId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_anniversaries_date` ON `anniversaries` (`date`)")

        // ===== v30→v31: events customTypeName =====

        db.execSQL("ALTER TABLE events ADD COLUMN customTypeName TEXT DEFAULT NULL")

        // ===== v31→v32: 无 schema 变更，仅版本号统一 =====
    }
}
