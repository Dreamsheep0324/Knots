package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {
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
