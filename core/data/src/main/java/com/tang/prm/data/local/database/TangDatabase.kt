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
        FavoriteEntity::class,
        DivinationRecordEntity::class,
        ContactAttributeEntity::class,
        SubscriptionEntity::class,
        RecipeEntity::class,
        RecipeTagEntity::class,
        RecipeContactCrossRef::class,
        RecipeTagCrossRef::class,
        ContactRelationEntity::class,
        PersonRelationEntity::class
    ],
    version = 50,
    exportSchema = true
)
// DB-Q-1 修复：移除 RecipeDataConverter 注册——RecipeEntity.ingredients/steps 字段类型为 String，
// Room 不会触发该 converter；实际调用全部在 RecipeMapper 中手动执行。保留注册只会误导维护者。
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
    abstract fun divinationRecordDao(): DivinationRecordDao
    abstract fun contactAttributeDao(): ContactAttributeDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeTagDao(): RecipeTagDao
    abstract fun contactRelationDao(): ContactRelationDao
    abstract fun personRelationDao(): PersonRelationDao

    suspend fun checkpoint() {
        val db = openHelper.writableDatabase
        db.query("PRAGMA wal_checkpoint(TRUNCATE)").close()
    }

    companion object {
        /** REP-C-1 修复：数据库名单一真相源，所有引用方使用此常量。 */
        const val DB_NAME = "tang_database"
    }
}
