package com.tang.prm.data.local.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_34_35 = object : Migration(34, 35) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // ContactEntity indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_intimacyScore` ON `contacts` (`intimacyScore`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_lastInteractionTime` ON `contacts` (`lastInteractionTime`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_groupId` ON `contacts` (`groupId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_relationship` ON `contacts` (`relationship`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contacts_name` ON `contacts` (`name`)")

        // EventEntity indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_events_type` ON `events` (`type`)")

        // TodoItemEntity indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_todo_items_contactId` ON `todo_items` (`contactId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_todo_items_isCompleted` ON `todo_items` (`isCompleted`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_todo_items_dueDate` ON `todo_items` (`dueDate`)")

        // ReminderEntity indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_contactId` ON `reminders` (`contactId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_time` ON `reminders` (`time`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_reminders_isCompleted` ON `reminders` (`isCompleted`)")

        // GiftEntity indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_gifts_date` ON `gifts` (`date`)")

        // ThoughtEntity indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_thoughts_type` ON `thoughts` (`type`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_thoughts_isTodo` ON `thoughts` (`isTodo`)")
    }
}
