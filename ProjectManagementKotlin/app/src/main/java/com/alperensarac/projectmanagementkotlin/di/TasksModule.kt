package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.data.repository.TasksRepositoryImpl
import com.alperensarac.projectmanagementkotlin.domain.repository.TasksRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Tasks repository dependency binding.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TasksModule {

    @Binds
    @Singleton
    abstract fun bindTasksRepository(
        implementation: TasksRepositoryImpl
    ): TasksRepository
}