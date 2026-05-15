package com.tang.prm.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.tang.prm.data.local.dao.*
import com.tang.prm.data.local.database.TangDatabase
import com.tang.prm.data.local.database.migrations.*
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
            .addMigrations(
                MIGRATION_1_10, MIGRATION_10_20, MIGRATION_20_24,
                MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
                MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
                MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17,
                MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21,
                MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25,
                MIGRATION_25_26
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
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore
}
