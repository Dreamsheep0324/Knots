package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v38 → v39：为农历纪念日补齐闰月标记字段。
 *
 * 背景：[AnniversaryRepositoryImpl.effectiveDate] 之前完全忽略 [AnniversaryEntity.isLunar] 字段，
 * 将农历日期当作公历处理。修复后接入 [com.tang.prm.domain.util.LunarUtils.lunarToSolar]，
 * 但农历转公历需要知道是否为闰月，因此新增 isLeapMonth 字段。
 *
 * 同步为 [com.tang.prm.data.local.entity.ContactEntity] 添加 isLeapMonthBirthday，
 * 以便联系人生日提醒同样支持农历闰月。
 */
val MIGRATION_38_39 = object : Migration(38, 39) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE anniversaries ADD COLUMN isLeapMonth INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE contacts ADD COLUMN isLeapMonthBirthday INTEGER NOT NULL DEFAULT 0")
    }
}
