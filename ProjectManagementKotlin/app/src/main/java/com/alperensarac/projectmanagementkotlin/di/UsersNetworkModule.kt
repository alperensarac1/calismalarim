package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.data.remote.api.UsersApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

/**
 * UsersApi provider'ını ana NetworkModule'dan ayrı tutuyoruz.
 *
 * Authenticated Retrofit zaten ana network katmanı tarafından oluşturuluyor.
 */
@Module
@InstallIn(SingletonComponent::class)
object UsersNetworkModule {

    @Provides
    @Singleton
    fun provideUsersApi(
        retrofit: Retrofit
    ): UsersApi {

        return retrofit.create(
            UsersApi::class.java
        )
    }
}