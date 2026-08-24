package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.data.repository.TaskTimeLogsRepositoryImpl
import com.alperensarac.projectmanagementkotlin.domain.repository.TaskTimeLogsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TaskTimeLogsModule {

    @Binds
    @Singleton
    abstract fun bindTaskTimeLogsRepository(
        implementation: TaskTimeLogsRepositoryImpl
    ): TaskTimeLogsRepository
}