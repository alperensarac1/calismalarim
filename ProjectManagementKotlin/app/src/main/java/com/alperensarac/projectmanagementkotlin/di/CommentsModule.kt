package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.data.repository.TaskCommentsRepositoryImpl
import com.alperensarac.projectmanagementkotlin.domain.repository.TaskCommentsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Comment repository binding.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CommentsModule {

    @Binds
    @Singleton
    abstract fun bindTaskCommentsRepository(
        implementation: TaskCommentsRepositoryImpl
    ): TaskCommentsRepository
}