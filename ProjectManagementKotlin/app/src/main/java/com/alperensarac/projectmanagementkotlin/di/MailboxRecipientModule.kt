package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.data.repository.MailboxRecipientRepositoryImpl
import com.alperensarac.projectmanagementkotlin.domain.repository.MailboxRecipientRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MailboxRecipientModule {

    @Binds
    @Singleton
    abstract fun bindMailboxRecipientRepository(
        implementation:
        MailboxRecipientRepositoryImpl
    ): MailboxRecipientRepository
}