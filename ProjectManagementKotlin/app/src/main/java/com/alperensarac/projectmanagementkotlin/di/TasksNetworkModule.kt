package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.data.remote.api.TasksApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

/**
 * TasksApi provider.
 */
@Module
@InstallIn(SingletonComponent::class)
object TasksNetworkModule {

    @Provides
    @Singleton
    fun provideTasksApi(
        retrofit: Retrofit
    ): TasksApi {

        return retrofit.create(
            TasksApi::class.java
        )
    }
}