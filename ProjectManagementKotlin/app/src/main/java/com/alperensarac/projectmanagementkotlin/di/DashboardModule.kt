package com.alperensarac.projectmanagementkotlin.di

import com.alperensarac.projectmanagementkotlin.data.repository.DashboardRepositoryImpl
import com.alperensarac.projectmanagementkotlin.domain.repository.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dashboard repository bağımlılığını Hilt dependency graph'a bağlar.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DashboardModule {

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        implementation: DashboardRepositoryImpl
    ): DashboardRepository
}