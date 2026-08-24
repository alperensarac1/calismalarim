package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.data.remote.api.MailboxRecipientApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object MailboxRecipientNetworkModule {

    @Provides
    @Singleton
    fun provideMailboxRecipientApi(
        retrofit: Retrofit
    ): MailboxRecipientApi {

        return retrofit.create(
            MailboxRecipientApi::class.java
        )
    }
}