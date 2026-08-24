package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.data.repository.ProjectsRepositoryImpl
import com.alperensarac.projectmanagementkotlin.domain.repository.ProjectsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Projects repository interface ve implementasyonunu Hilt'e bağlar.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProjectsModule {

    @Binds
    @Singleton
    abstract fun bindProjectsRepository(
        implementation: ProjectsRepositoryImpl
    ): ProjectsRepository
}