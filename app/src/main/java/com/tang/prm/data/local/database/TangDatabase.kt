package com.tang.prm.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tang.prm.data.local.dao.*
import com.tang.prm.data.local.entity.*

@Database(
    entities = [
        ContactEntity::class,
        ContactGroupEntity::class,
        ContactTagEntity::class,
        ContactTagCrossRef::class,
        EventEntity::class,
        EventParticipantCrossRef::class,
        AnniversaryEntity::class,
        TodoItemEntity::class,
        ReminderEntity::class,
        CustomTypeEntity::class,
        GiftEntity::class,
        ThoughtEntity::class,
        CircleEntity::class,
        CircleMemberCrossRef::class,
        FavoriteEntity::class
    ],
    version = 26,
    exportSchema = true
)
@TypeConverters(ListStringConverter::class)
abstract class TangDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun contactGroupDao(): ContactGroupDao
    abstract fun contactTagDao(): ContactTagDao
    abstract fun eventDao(): EventDao
    abstract fun anniversaryDao(): AnniversaryDao
    abstract fun todoDao(): TodoDao
    abstract fun reminderDao(): ReminderDao
    abstract fun customTypeDao(): CustomTypeDao
    abstract fun giftDao(): GiftDao
    abstract fun thoughtDao(): ThoughtDao
    abstract fun circleDao(): CircleDao
    abstract fun favoriteDao(): FavoriteDao

    suspend fun checkpoint() {
        val db = openHelper.writableDatabase
        db.query("PRAGMA wal_checkpoint(TRUNCATE)").close()
    }
}
