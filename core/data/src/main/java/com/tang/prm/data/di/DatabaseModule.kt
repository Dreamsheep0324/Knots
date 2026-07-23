package com.tang.prm.data.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tang.prm.data.local.dao.*
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.domain.repository.EncryptionStatusProvider
import com.tang.prm.data.local.database.migrations.MIGRATION_1_32
import com.tang.prm.data.local.database.migrations.MIGRATION_24_31
import com.tang.prm.data.local.database.migrations.MIGRATION_28_31
import com.tang.prm.data.local.database.migrations.MIGRATION_31_33
import com.tang.prm.data.local.database.migrations.MIGRATION_32_33
import com.tang.prm.data.local.database.migrations.MIGRATION_33_34
import com.tang.prm.data.local.database.migrations.MIGRATION_34_35
import com.tang.prm.data.local.database.migrations.MIGRATION_35_36
import com.tang.prm.data.local.database.migrations.MIGRATION_36_37
import com.tang.prm.data.local.database.migrations.MIGRATION_37_38
import com.tang.prm.data.local.database.migrations.MIGRATION_38_39
import com.tang.prm.data.local.database.migrations.MIGRATION_39_40
import com.tang.prm.data.local.database.migrations.MIGRATION_40_41
import com.tang.prm.data.local.database.migrations.MIGRATION_41_42
import com.tang.prm.data.local.database.migrations.MIGRATION_42_43
import com.tang.prm.data.local.database.migrations.MIGRATION_43_44
import com.tang.prm.data.local.database.migrations.MIGRATION_44_45
import com.tang.prm.data.local.database.migrations.MIGRATION_45_46
import com.tang.prm.data.local.database.migrations.MIGRATION_46_47
import com.tang.prm.data.local.database.migrations.MIGRATION_47_48
import com.tang.prm.data.local.database.migrations.MIGRATION_48_49
import com.tang.prm.data.local.database.migrations.MIGRATION_49_50
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TangDatabase {
        return Room.databaseBuilder(
            context,
            TangDatabase::class.java,
            TangDatabase.DB_NAME
        )
            // 迁移策略说明：
            // v1-v23 已无用户，由 MIGRATION_1_32 一次性聚合
            // v25-v27 为 schema 重设计过程中的内部开发版本，未发布到生产，无需独立迁移路径
            // v24 用户通过 MIGRATION_24_31 升级，v28 用户通过 MIGRATION_28_31 升级
            .addMigrations(
                MIGRATION_1_32, MIGRATION_24_31, MIGRATION_28_31,
                MIGRATION_31_33, MIGRATION_32_33,
                MIGRATION_33_34, MIGRATION_34_35, MIGRATION_35_36,
                MIGRATION_36_37, MIGRATION_37_38, MIGRATION_38_39,
                MIGRATION_39_40, MIGRATION_40_41, MIGRATION_41_42,
                MIGRATION_42_43, MIGRATION_43_44, MIGRATION_44_45, MIGRATION_45_46,
                MIGRATION_46_47, MIGRATION_47_48, MIGRATION_48_49,
                MIGRATION_49_50
            )
            .build()
    }

    @Provides
    fun provideContactDao(database: TangDatabase): ContactDao = database.contactDao()

    @Provides
    fun provideContactGroupDao(database: TangDatabase): ContactGroupDao = database.contactGroupDao()

    @Provides
    fun provideContactTagDao(database: TangDatabase): ContactTagDao = database.contactTagDao()

    @Provides
    fun provideEventDao(database: TangDatabase): EventDao = database.eventDao()

    @Provides
    fun provideAnniversaryDao(database: TangDatabase): AnniversaryDao = database.anniversaryDao()

    @Provides
    fun provideTodoDao(database: TangDatabase): TodoDao = database.todoDao()

    @Provides
    fun provideReminderDao(database: TangDatabase): ReminderDao = database.reminderDao()

    @Provides
    fun provideCustomTypeDao(database: TangDatabase): CustomTypeDao = database.customTypeDao()

    @Provides
    fun provideGiftDao(database: TangDatabase): GiftDao = database.giftDao()

    @Provides
    fun provideThoughtDao(database: TangDatabase): ThoughtDao = database.thoughtDao()

    @Provides
    fun provideCircleDao(database: TangDatabase): CircleDao = database.circleDao()

    @Provides
    fun provideFavoriteDao(database: TangDatabase): FavoriteDao = database.favoriteDao()

    @Provides
    fun provideDivinationRecordDao(database: TangDatabase): DivinationRecordDao = database.divinationRecordDao()

    @Provides
    fun provideContactAttributeDao(database: TangDatabase): ContactAttributeDao = database.contactAttributeDao()

    @Provides
    fun provideSubscriptionDao(database: TangDatabase): SubscriptionDao = database.subscriptionDao()

    @Provides
    fun provideRecipeDao(database: TangDatabase): RecipeDao = database.recipeDao()

    @Provides
    fun provideRecipeTagDao(database: TangDatabase): RecipeTagDao = database.recipeTagDao()

    @Provides
    fun provideContactRelationDao(database: TangDatabase): ContactRelationDao =
        database.contactRelationDao()

    @Provides
    fun providePersonRelationDao(database: TangDatabase): PersonRelationDao =
        database.personRelationDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore

    // DI-Q-1 修复：抽取公共方法消除两个 EncryptedSharedPreferences @Provides 的代码重复。
    // DB-B-3 修复：加密失败时标记降级并抛异常，不回退明文。
    // A-2 修复：改用注入的 EncryptionStatusProvider 替代 domain 层全局单例。
    private fun createEncryptedPrefs(
        context: Context,
        fileName: String,
        encryptionStatusProvider: EncryptionStatusProvider
    ): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                fileName,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("DatabaseModule", "EncryptedSharedPreferences($fileName) 不可用，敏感数据将不会被保存", e)
            encryptionStatusProvider.markDegraded()
            throw e
        }
    }

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(
        @ApplicationContext context: Context,
        encryptionStatusProvider: EncryptionStatusProvider
    ): SharedPreferences = createEncryptedPrefs(context, "secret_settings", encryptionStatusProvider)

    @Provides
    @Singleton
    @Named("webdav")
    fun provideWebDavEncryptedPrefs(
        @ApplicationContext context: Context,
        encryptionStatusProvider: EncryptionStatusProvider
    ): SharedPreferences = createEncryptedPrefs(context, "webdav_secret", encryptionStatusProvider)
}
