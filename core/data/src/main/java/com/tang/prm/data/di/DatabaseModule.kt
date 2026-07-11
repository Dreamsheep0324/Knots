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
            "tang_database"
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
                MIGRATION_39_40
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
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                "secret_settings",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("DatabaseModule", "EncryptedSharedPreferences unavailable! Sensitive data will NOT be encrypted.", e)
            com.tang.prm.domain.util.EncryptionStatus.markDegraded()
            context.getSharedPreferences("secret_settings_fallback", Context.MODE_PRIVATE).edit()
                .putBoolean("encryption_degraded", true).apply()
            context.getSharedPreferences("secret_settings_fallback", Context.MODE_PRIVATE)
        }
    }

    @Provides
    @Singleton
    @Named("webdav")
    fun provideWebDavEncryptedPrefs(@ApplicationContext context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                "webdav_secret",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e("DatabaseModule", "EncryptedSharedPreferences unavailable! Sensitive data will NOT be encrypted.", e)
            com.tang.prm.domain.util.EncryptionStatus.markDegraded()
            context.getSharedPreferences("webdav_secret_fallback", Context.MODE_PRIVATE).edit()
                .putBoolean("encryption_degraded", true).apply()
            context.getSharedPreferences("webdav_secret_fallback", Context.MODE_PRIVATE)
        }
    }
}
