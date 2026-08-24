package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.data.remote.api.TaskHistoriesApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

/**
 * TaskHistoriesApi Hilt provider'ı.
 *
 * Endpoint [Authorize] kullandığı için AuthenticatedRetrofit kullanıyoruz.
 */
@Module
@InstallIn(SingletonComponent::class)
object TaskHistoriesNetworkModule {

    @Provides
    @Singleton
    fun provideTaskHistoriesApi(
        retrofit: Retrofit
    ): TaskHistoriesApi {

        return retrofit.create(
            TaskHistoriesApi::class.java
        )
    }
}