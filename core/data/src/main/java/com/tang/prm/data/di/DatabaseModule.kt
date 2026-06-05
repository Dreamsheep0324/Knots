package com.tang.prm.data.di

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tang.prm.data.local.dao.*
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.database.migrations.MIGRATION_1_32
import com.tang.prm.data.local.database.migrations.MIGRATION_31_33
import com.tang.prm.data.local.database.migrations.MIGRATION_32_33
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
            .addMigrations(MIGRATION_1_32, MIGRATION_31_33, MIGRATION_32_33)
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
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore

    @Provides
    @Singleton
    fun provideEncryptedSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            "secret_settings",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
