package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.data.repository.MailboxRepositoryImpl
import com.alperensarac.projectmanagementkotlin.domain.repository.MailboxRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MailboxModule {

    @Binds
    @Singleton
    abstract fun bindMailboxRepository(
        implementation: MailboxRepositoryImpl
    ): MailboxRepository
}