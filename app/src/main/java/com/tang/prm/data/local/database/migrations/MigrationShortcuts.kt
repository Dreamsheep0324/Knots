package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * 合并迁移快捷路径：v1 → v10，合并了 MIGRATION_1_2 到 MIGRATION_9_10 的全部操作
 */
val MIGRATION_1_10 = object : Migration(1, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
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
    }
}

/**
 * 合并迁移快捷路径：v10 → v20，合并了 MIGRATION_10_11 到 MIGRATION_19_20 的全部操作
 */
val MIGRATION_10_20 = object : Migration(10, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
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
                "photos TEXT," +
                "createdAt INTEGER NOT NULL," +
                "updatedAt INTEGER NOT NULL" +
                ")")
        db.execSQL("CREATE TABLE IF NOT EXISTS thoughts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "contactId INTEGER," +
                "content TEXT NOT NULL," +
                "mood TEXT," +
                "isPrivate INTEGER NOT NULL," +
                "createdAt INTEGER NOT NULL," +
                "updatedAt INTEGER NOT NULL" +
                ")")
        db.execSQL("ALTER TABLE contacts ADD COLUMN knowingDate INTEGER")
        db.execSQL("CREATE TABLE IF NOT EXISTS circles (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "name TEXT NOT NULL," +
                "description TEXT," +
                "color TEXT NOT NULL DEFAULT '#6366F1'," +
                "icon TEXT NOT NULL DEFAULT 'people'," +
                "memberIds TEXT NOT NULL DEFAULT ''," +
                "intimacyThreshold INTEGER NOT NULL DEFAULT 0," +
                "sortOrder INTEGER NOT NULL DEFAULT 0," +
                "createdAt INTEGER NOT NULL," +
                "updatedAt INTEGER NOT NULL" +
                ")")
        db.execSQL("ALTER TABLE circles ADD COLUMN parentCircleId INTEGER")
        db.execSQL("DELETE FROM custom_types WHERE category = 'EVENT_TYPE' AND isDefault = 1")
        db.execSQL("ALTER TABLE circles ADD COLUMN waveform TEXT NOT NULL DEFAULT 'sine'")
        db.execSQL("ALTER TABLE thoughts ADD COLUMN type TEXT NOT NULL DEFAULT 'murmur'")
        db.execSQL("ALTER TABLE thoughts ADD COLUMN isTodo INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE thoughts ADD COLUMN isDone INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE thoughts ADD COLUMN dueDate INTEGER")
        db.execSQL("ALTER TABLE thoughts ADD COLUMN category TEXT")
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
    }
}

/**
 * 合并迁移快捷路径：v20 → v24，合并了 MIGRATION_20_21 到 MIGRATION_23_24 的全部操作
 */
val MIGRATION_20_24 = object : Migration(20, 24) {
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
        db.execSQL("ALTER TABLE custom_types ADD COLUMN `key` TEXT NOT NULL DEFAULT ''")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_thoughts_contactId` ON `thoughts` (`contactId`)")
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
    }
}
