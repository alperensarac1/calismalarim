package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.data.remote.api.TaskTimeLogsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

/**
 * TaskTimeLogsApi provider.
 */
@Module
@InstallIn(SingletonComponent::class)
object TaskTimeLogsNetworkModule {

    @Provides
    @Singleton
    fun provideTaskTimeLogsApi(
        retrofit: Retrofit
    ): TaskTimeLogsApi {

        return retrofit.create(
            TaskTimeLogsApi::class.java
        )
    }
}