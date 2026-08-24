package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.data.repository.TaskHistoriesRepositoryImpl
import com.alperensarac.projectmanagementkotlin.domain.repository.TaskHistoriesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * TaskHistoriesRepository dependency binding.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TaskHistoriesModule {

    @Binds
    @Singleton
    abstract fun bindTaskHistoriesRepository(
        implementation: TaskHistoriesRepositoryImpl
    ): TaskHistoriesRepository
}