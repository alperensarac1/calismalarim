package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.data.repository.UsersRepositoryImpl
import com.alperensarac.projectmanagementkotlin.domain.repository.UsersRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * UsersRepository dependency binding.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class UsersModule {

    @Binds
    @Singleton
    abstract fun bindUsersRepository(
        implementation: UsersRepositoryImpl
    ): UsersRepository
}