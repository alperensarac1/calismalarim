package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.data.remote.api.MailboxApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

/**
 * Mailbox endpointleri [Authorize] altında olduğu için
 * AuthenticatedRetrofit kullanılır.
 */
@Module
@InstallIn(SingletonComponent::class)
object MailboxNetworkModule {

    @Provides
    @Singleton
    fun provideMailboxApi(
        retrofit: Retrofit
    ): MailboxApi {

        return retrofit.create(
            MailboxApi::class.java
        )
    }
}