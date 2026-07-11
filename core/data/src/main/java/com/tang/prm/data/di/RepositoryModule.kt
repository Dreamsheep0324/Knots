package com.tang.prm.data.di

import com.tang.prm.data.repository.*
import com.tang.prm.domain.divination.repository.DivinationRepository
import com.tang.prm.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds
    @Singleton
    abstract fun bindContactGroupRepository(impl: ContactGroupRepositoryImpl): ContactGroupRepository

    @Binds
    @Singleton
    abstract fun bindContactTagRepository(impl: ContactTagRepositoryImpl): ContactTagRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(impl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindAnniversaryRepository(impl: AnniversaryRepositoryImpl): AnniversaryRepository

    @Binds
    @Singleton
    abstract fun bindTodoRepository(impl: TodoRepositoryImpl): TodoRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository

    @Binds
    @Singleton
    abstract fun bindCustomTypeRepository(impl: CustomTypeRepositoryImpl): CustomTypeRepository

    @Binds
    @Singleton
    abstract fun bindGiftRepository(impl: GiftRepositoryImpl): GiftRepository

    @Binds
    @Singleton
    abstract fun bindThoughtRepository(impl: ThoughtRepositoryImpl): ThoughtRepository

    @Binds
    @Singleton
    abstract fun bindCircleRepository(impl: CircleRepositoryImpl): CircleRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindDivinationRepository(impl: DivinationRepositoryImpl): DivinationRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(impl: AiRepositoryImpl): AiRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: BackupRepository): BackupRepositoryInterface

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(impl: SubscriptionRepositoryImpl): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindWebDavRepository(impl: WebDavRepositoryImpl): WebDavRepository

    @Binds
    @Singleton
    abstract fun bindUpdateRepository(impl: UpdateRepositoryImpl): UpdateRepository
}
