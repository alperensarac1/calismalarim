package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.data.repository.AuthRepositoryImpl
import com.alperensarac.projectmanagementkotlin.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Authentication repository interface ve implementasyonunu Hilt'e tanıtır.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        implementation: AuthRepositoryImpl
    ): AuthRepository
}